import { serverError } from "../utils/handleError.js"
import mongoose from "mongoose"
import Order from "../models/order.model.js"
import User from "../models/user.model.js"
import Worker from "../models/worker.model.js"
import { CATEGORY_VALUES } from "../constants/categories.js"
import { computeBill } from "../utils/pricing.js"
import { buildCandidates, startDispatch, advance, ensureProgress } from "../services/orderDispatch.js"
import { sendPush } from "../utils/sendPush.js"

const notifyCurrentCandidate = async (order) => {
    try {
        const i = order.currentIndex
        if (i < 0 || !order.candidates[i]) return
        const worker = await Worker.findById(order.candidates[i].worker)
        if (worker?.fcmToken) {
            await sendPush(
                worker.fcmToken,
                "New job request",
                `${order.category} · ₹${order.bill?.workAmount || 0} · tap to accept`,
                { orderId: String(order._id), type: "new_offer" }
            )
        }
    } catch (e) {
        console.error("notifyCurrentCandidate:", e.message)
    }
}

const notifyUser = async (order, title, body, type) => {
    try {
        const user = await User.findById(order.user)
        if (user?.fcmToken) {
            await sendPush(user.fcmToken, title, body, { orderId: String(order._id), type })
        }
    } catch (e) {
        console.error("notifyUser:", e.message)
    }
}

const notifyAssignedWorker = async (order, title, body, type) => {
    try {
        if (!order.assignedWorker) return
        const worker = await Worker.findById(order.assignedWorker)
        if (worker?.fcmToken) {
            await sendPush(worker.fcmToken, title, body, { orderId: String(order._id), type })
        }
    } catch (e) {
        console.error("notifyAssignedWorker:", e.message)
    }
}

const MIN_MINUTES = 15
const MAX_MINUTES = 480

const parseCoordinates = (raw) => {
    let coordinates = raw
    if (typeof coordinates === "string") {
        try {
            coordinates = JSON.parse(coordinates)
        } catch {
            coordinates = undefined
        }
    }
    return coordinates
}

const validCoords = (c) =>
    Array.isArray(c) && c.length === 2 && !(Number(c[0]) === 0 && Number(c[1]) === 0)

const generateOtp = () => String(Math.floor(1000 + Math.random() * 9000))

const formatUserOrder = (order) => {
    const notified = order.candidates.filter((c) => c.status !== "pending").length
    const assigned = order.assignedWorker && order.assignedWorker.name
    const base = {
        id: order._id,
        category: order.category,
        durationMinutes: order.durationMinutes,
        notes: order.notes,
        bill: order.bill,
        status: order.status,
        location: order.location,
        address: order.address,
        progress: { notified, total: order.candidates.length, currentIndex: order.currentIndex },
        assignedWorker: assigned
            ? {
                  id: order.assignedWorker._id,
                  name: order.assignedWorker.name,
                  phone: order.assignedWorker.phone,
                  categories: order.assignedWorker.categories,
                  experienceYears: order.assignedWorker.experienceYears,
              }
            : null,
        offerExpiresAt: order.offerExpiresAt,
        acceptedAt: order.acceptedAt,
        startedAt: order.startedAt,
        completedAt: order.completedAt,
        createdAt: order.createdAt,
    }

    if (order.status === "assigned") {
        base.startOtp = order.startOtp
    }
    if (order.status === "in_progress") {
        base.endOtp = order.endOtp
    }
    if (["awaiting_payment", "completed"].includes(order.status)) {
        base.payment = {
            status: order.payment?.status || "pending",
            amount: order.bill?.total,
            workerPayout: order.bill?.workAmount,
            platformFee: order.bill?.platformFee,
            method: order.payment?.method,
            transactionId: order.payment?.transactionId,
            paidAt: order.payment?.paidAt,
        }
        if (order.status === "awaiting_payment") {
            base.payment.upiLink = `upi://pay?pa=karigar@upi&pn=Karigar&am=${order.bill?.total}&cu=INR&tn=Order-${order._id}`
        }
    }

    return base
}

const formatOfferForWorker = (order, workerId) => {
    const candidate = order.candidates.find((c) => c.worker.equals(workerId))
    return {
        id: order._id,
        category: order.category,
        durationMinutes: order.durationMinutes,
        notes: order.notes,
        earning: order.bill?.workAmount,
        location: order.location,
        distanceKm: candidate ? candidate.distanceKm : undefined,
        offerExpiresAt: order.offerExpiresAt,
        createdAt: order.createdAt,
    }
}

const formatAssignedForWorker = (order) => {
    const base = {
        id: order._id,
        category: order.category,
        durationMinutes: order.durationMinutes,
        notes: order.notes,
        earning: order.bill?.workAmount,
        status: order.status,
        customer: {
            phone: order.userPhone,
            location: order.location,
            address: order.address,
        },
        acceptedAt: order.acceptedAt,
        startedAt: order.startedAt,
        completedAt: order.completedAt,
        createdAt: order.createdAt,
    }
    if (order.status === "awaiting_payment") {
        const payee = process.env.PAYEE_UPI || "karigar@upi"
        base.payment = {
            amount: order.bill?.total,
            workerPayout: order.bill?.workAmount,
            platformFee: order.bill?.platformFee,
            upiUri: `upi://pay?pa=${payee}&pn=Karigar&am=${order.bill?.total}&cu=INR&tn=Order-${order._id}`,
        }
    }
    return base
}

export const getQuote = (req, res) => {
    const durationMinutes = Number(req.query.durationMinutes)
    if (!durationMinutes || durationMinutes < MIN_MINUTES || durationMinutes > MAX_MINUTES) {
        return res.status(400).json({ success: false, message: `durationMinutes must be between ${MIN_MINUTES} and ${MAX_MINUTES}` })
    }
    return res.status(200).json({ success: true, bill: computeBill(durationMinutes) })
}

export const createOrder = async (req, res) => {
    try {
        const { category, notes } = req.body
        const durationMinutes = Number(req.body.durationMinutes)

        if (!category || !CATEGORY_VALUES.includes(category)) {
            return res.status(400).json({ success: false, message: "valid category is required" })
        }
        if (!durationMinutes || durationMinutes < MIN_MINUTES || durationMinutes > MAX_MINUTES) {
            return res.status(400).json({ success: false, message: `durationMinutes must be between ${MIN_MINUTES} and ${MAX_MINUTES}` })
        }

        const user = await User.findById(req.userId)
        if (!user) {
            return res.status(404).json({ success: false, message: "User not found" })
        }

        let coordinates = parseCoordinates(req.body.coordinates)
        if (!validCoords(coordinates)) {
            coordinates = user.location?.coordinates
        }
        if (!validCoords(coordinates)) {
            return res.status(400).json({ success: false, message: "location coordinates are required" })
        }

        const address = req.body.address || user.address
        const bill = computeBill(durationMinutes)

        const candidates = await buildCandidates(category, coordinates[0], coordinates[1])

        const order = new Order({
            user: user._id,
            userPhone: user.phone,
            category,
            durationMinutes,
            notes,
            location: { type: "Point", coordinates },
            address,
            bill,
            candidates,
        })

        startDispatch(order)
        await order.save()

        if (candidates.length) notifyCurrentCandidate(order)

        return res.status(201).json({
            success: true,
            message: candidates.length
                ? "Searching for nearby workers"
                : "No workers nearby, job listed in open pool",
            order: formatUserOrder(order),
        })
    } catch (err) {
        return serverError(res, err)
    }
}

export const getUserOrders = async (req, res) => {
    try {
        const orders = await Order.find({ user: req.userId })
            .sort({ createdAt: -1 })
            .populate("assignedWorker", "name phone categories experienceYears")
        return res.status(200).json({
            success: true,
            count: orders.length,
            orders: orders.map(formatUserOrder),
        })
    } catch (err) {
        return serverError(res, err)
    }
}

export const getUserOrder = async (req, res) => {
    try {
        const { id } = req.params
        if (!mongoose.Types.ObjectId.isValid(id)) {
            return res.status(400).json({ success: false, message: "invalid order id" })
        }

        const order = await Order.findOne({ _id: id, user: req.userId }).populate(
            "assignedWorker",
            "name phone categories experienceYears"
        )
        if (!order) {
            return res.status(404).json({ success: false, message: "Order not found" })
        }

        if (order.status === "searching") {
            ensureProgress(order)
            await order.save()
        }

        return res.status(200).json({ success: true, order: formatUserOrder(order) })
    } catch (err) {
        return serverError(res, err)
    }
}

export const cancelOrder = async (req, res) => {
    try {
        const { id } = req.params
        if (!mongoose.Types.ObjectId.isValid(id)) {
            return res.status(400).json({ success: false, message: "invalid order id" })
        }

        const order = await Order.findOne({ _id: id, user: req.userId })
        if (!order) {
            return res.status(404).json({ success: false, message: "Order not found" })
        }
        if (["completed", "cancelled"].includes(order.status)) {
            return res.status(409).json({ success: false, message: `order already ${order.status}` })
        }

        order.status = "cancelled"
        order.cancelledAt = new Date()
        order.offerExpiresAt = null
        await order.save()

        return res.status(200).json({ success: true, message: "Order cancelled" })
    } catch (err) {
        return serverError(res, err)
    }
}

export const payOrder = async (req, res) => {
    try {
        const { id } = req.params
        const { method } = req.body
        if (!mongoose.Types.ObjectId.isValid(id)) {
            return res.status(400).json({ success: false, message: "invalid order id" })
        }

        const order = await Order.findOne({ _id: id, user: req.userId })
        if (!order) {
            return res.status(404).json({ success: false, message: "Order not found" })
        }
        if (order.status !== "awaiting_payment") {
            return res.status(409).json({ success: false, message: "order is not awaiting payment" })
        }

        order.payment = {
            status: "paid",
            amount: order.bill.total,
            workerPayout: order.bill.workAmount,
            platformFee: order.bill.platformFee,
            method: method || "upi",
            transactionId: "TXN" + Date.now() + Math.floor(Math.random() * 1000),
            paidAt: new Date(),
        }
        order.status = "completed"
        order.completedAt = new Date()
        await order.save()

        return res.status(200).json({
            success: true,
            message: "Payment successful",
            payment: {
                amount: order.bill.total,
                workerPayout: order.bill.workAmount,
                platformFee: order.bill.platformFee,
                transactionId: order.payment.transactionId,
            },
        })
    } catch (err) {
        return serverError(res, err)
    }
}

export const startWork = async (req, res) => {
    try {
        const { id } = req.params
        const { otp } = req.body
        if (!mongoose.Types.ObjectId.isValid(id)) {
            return res.status(400).json({ success: false, message: "invalid order id" })
        }

        const order = await Order.findById(id)
        if (!order) {
            return res.status(404).json({ success: false, message: "Order not found" })
        }
        if (!order.assignedWorker || !order.assignedWorker.equals(req.workerId)) {
            return res.status(403).json({ success: false, message: "not your order" })
        }
        if (order.status !== "assigned") {
            return res.status(409).json({ success: false, message: "order cannot be started" })
        }
        if (!otp || String(otp) !== order.startOtp) {
            return res.status(400).json({ success: false, message: "invalid start OTP" })
        }

        order.status = "in_progress"
        order.startedAt = new Date()
        order.endOtp = generateOtp()
        await order.save()

        return res.status(200).json({ success: true, message: "Work started" })
    } catch (err) {
        return serverError(res, err)
    }
}

export const finishWork = async (req, res) => {
    try {
        const { id } = req.params
        const { otp } = req.body
        if (!mongoose.Types.ObjectId.isValid(id)) {
            return res.status(400).json({ success: false, message: "invalid order id" })
        }

        const order = await Order.findById(id)
        if (!order) {
            return res.status(404).json({ success: false, message: "Order not found" })
        }
        if (!order.assignedWorker || !order.assignedWorker.equals(req.workerId)) {
            return res.status(403).json({ success: false, message: "not your order" })
        }
        if (order.status !== "in_progress") {
            return res.status(409).json({ success: false, message: "work is not in progress" })
        }
        if (!otp || String(otp) !== order.endOtp) {
            return res.status(400).json({ success: false, message: "invalid end OTP" })
        }

        order.status = "awaiting_payment"
        await order.save()

        return res.status(200).json({ success: true, message: "Work completed, awaiting payment" })
    } catch (err) {
        return serverError(res, err)
    }
}

export const getWorkerOffers = async (req, res) => {
    try {
        const orders = await Order.find({
            status: "searching",
            "candidates.worker": req.workerId,
        })

        const offers = []
        for (const order of orders) {
            ensureProgress(order)
            if (order.isModified()) await order.save()

            const i = order.currentIndex
            if (
                order.status === "searching" &&
                i >= 0 &&
                order.candidates[i] &&
                order.candidates[i].worker.equals(req.workerId) &&
                order.candidates[i].status === "notified"
            ) {
                offers.push(formatOfferForWorker(order, req.workerId))
            }
        }

        return res.status(200).json({ success: true, count: offers.length, offers })
    } catch (err) {
        return serverError(res, err)
    }
}

export const respondToOffer = async (req, res) => {
    try {
        const { id } = req.params
        const { action } = req.body

        if (!mongoose.Types.ObjectId.isValid(id)) {
            return res.status(400).json({ success: false, message: "invalid order id" })
        }
        if (!["accept", "reject"].includes(action)) {
            return res.status(400).json({ success: false, message: "action must be accept or reject" })
        }

        const order = await Order.findById(id)
        if (!order) {
            return res.status(404).json({ success: false, message: "Order not found" })
        }

        ensureProgress(order)

        const i = order.currentIndex
        const isCurrent =
            order.status === "searching" &&
            i >= 0 &&
            order.candidates[i] &&
            order.candidates[i].worker.equals(req.workerId) &&
            order.candidates[i].status === "notified"

        if (!isCurrent) {
            await order.save()
            return res.status(409).json({ success: false, message: "offer is no longer available" })
        }

        if (action === "accept") {
            order.candidates[i].status = "accepted"
            order.candidates[i].respondedAt = new Date()
            order.status = "assigned"
            order.assignedWorker = req.workerId
            order.acceptedAt = new Date()
            order.offerExpiresAt = null
            order.startOtp = generateOtp()
            await order.save()
            notifyUser(order, "Worker on the way", "A worker accepted your request", "assigned")
            return res.status(200).json({
                success: true,
                message: "Order accepted",
                order: formatAssignedForWorker(order),
            })
        }

        advance(order, "rejected")
        await order.save()
        if (order.status === "searching") notifyCurrentCandidate(order)
        return res.status(200).json({ success: true, message: "Offer rejected" })
    } catch (err) {
        return serverError(res, err)
    }
}

export const getOpenJobs = async (req, res) => {
    try {
        const worker = await Worker.findById(req.workerId)
        if (!worker) {
            return res.status(404).json({ success: false, message: "Worker not found" })
        }

        const hasLocation =
            worker.location &&
            Array.isArray(worker.location.coordinates) &&
            !(worker.location.coordinates[0] === 0 && worker.location.coordinates[1] === 0)

        let jobs
        if (hasLocation) {
            jobs = await Order.aggregate([
                {
                    $geoNear: {
                        near: { type: "Point", coordinates: worker.location.coordinates },
                        distanceField: "distance",
                        spherical: true,
                        query: { status: "open", category: { $in: worker.categories } },
                    },
                },
                { $limit: 50 },
            ])
            jobs = jobs.map((o) => ({
                id: o._id,
                category: o.category,
                durationMinutes: o.durationMinutes,
                notes: o.notes,
                earning: o.bill?.workAmount,
                location: o.location,
                distanceKm: Math.round((o.distance / 1000) * 100) / 100,
                createdAt: o.createdAt,
            }))
        } else {
            const docs = await Order.find({ status: "open", category: { $in: worker.categories } }).sort({ createdAt: -1 })
            jobs = docs.map((o) => ({
                id: o._id,
                category: o.category,
                durationMinutes: o.durationMinutes,
                notes: o.notes,
                earning: o.bill?.workAmount,
                location: o.location,
                createdAt: o.createdAt,
            }))
        }

        return res.status(200).json({ success: true, count: jobs.length, jobs })
    } catch (err) {
        return serverError(res, err)
    }
}

export const pickJob = async (req, res) => {
    try {
        const { id } = req.params
        if (!mongoose.Types.ObjectId.isValid(id)) {
            return res.status(400).json({ success: false, message: "invalid order id" })
        }

        const worker = await Worker.findById(req.workerId)
        if (!worker) {
            return res.status(404).json({ success: false, message: "Worker not found" })
        }

        const order = await Order.findById(id)
        if (!order) {
            return res.status(404).json({ success: false, message: "Order not found" })
        }
        if (order.status !== "open") {
            return res.status(409).json({ success: false, message: "job is no longer open" })
        }
        if (!worker.categories.includes(order.category)) {
            return res.status(403).json({ success: false, message: "job category does not match your work" })
        }

        order.status = "assigned"
        order.assignedWorker = worker._id
        order.acceptedAt = new Date()
        order.startOtp = generateOtp()
        await order.save()

        notifyUser(order, "Worker on the way", "A worker picked your job", "assigned")

        return res.status(200).json({
            success: true,
            message: "Job picked",
            order: formatAssignedForWorker(order),
        })
    } catch (err) {
        return serverError(res, err)
    }
}

export const getWorkerOrders = async (req, res) => {
    try {
        const orders = await Order.find({ assignedWorker: req.workerId }).sort({ createdAt: -1 })
        return res.status(200).json({
            success: true,
            count: orders.length,
            orders: orders.map(formatAssignedForWorker),
        })
    } catch (err) {
        return serverError(res, err)
    }
}

export const confirmPayment = async (req, res) => {
    try {
        const { id } = req.params
        if (!mongoose.Types.ObjectId.isValid(id)) {
            return res.status(400).json({ success: false, message: "invalid order id" })
        }

        const order = await Order.findById(id)
        if (!order) {
            return res.status(404).json({ success: false, message: "Order not found" })
        }
        if (!order.assignedWorker || !order.assignedWorker.equals(req.workerId)) {
            return res.status(403).json({ success: false, message: "not your order" })
        }
        if (order.status !== "awaiting_payment") {
            return res.status(409).json({ success: false, message: "order is not awaiting payment" })
        }

        order.payment = {
            status: "paid",
            amount: order.bill.total,
            workerPayout: order.bill.workAmount,
            platformFee: order.bill.platformFee,
            method: "upi",
            transactionId: "TXN" + Date.now() + Math.floor(Math.random() * 1000),
            paidAt: new Date(),
        }
        order.status = "completed"
        order.completedAt = new Date()
        await order.save()

        notifyUser(order, "Payment received", "Your service is completed. Thank you!", "completed")
        notifyAssignedWorker(order, "Payment received", "Order completed successfully", "completed")

        return res.status(200).json({ success: true, message: "Payment confirmed, order completed" })
    } catch (err) {
        return serverError(res, err)
    }
}
