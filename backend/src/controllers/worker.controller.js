import { serverError } from "../utils/handleError.js"
import Worker from "../models/worker.model.js"
import Order from "../models/order.model.js"
import { generateToken } from "../utils/generateToken.js"
import { uploadToCloudinary } from "../utils/uploadToCloudinary.js"
import { WORKER_CATEGORIES, CATEGORY_VALUES, SKILL_TYPES } from "../constants/categories.js"
import { getAuth } from "../config/firebase.js"

const resolveVerifiedPhone = async (body) => {
    if (body.idToken) {
        const decoded = await getAuth().verifyIdToken(body.idToken)
        if (decoded.phone_number) return decoded.phone_number
    }
    return typeof body.phone === "string" ? body.phone : ""
}

const formatWorker = (worker) => ({
    id: worker._id,
    name: worker.name,
    phone: worker.phone,
    address: worker.address,
    location: worker.location,
    categories: worker.categories,
    experienceYears: worker.experienceYears,
    workedWithCompany: worker.workedWithCompany,
    companyName: worker.companyName,
    bio: worker.bio,
    aadhar: {
        number: worker.aadhar?.number,
        photo: worker.aadhar?.photo,
    },
    isVerified: worker.isVerified,
    isOnline: worker.isOnline,
    expoToken: worker.expoToken,
    createdAt: worker.createdAt,
})

const parseCategories = (raw) => {
    if (Array.isArray(raw)) return raw
    if (typeof raw === "string") {
        try {
            const parsed = JSON.parse(raw)
            if (Array.isArray(parsed)) return parsed
            return [parsed]
        } catch {
            return raw.split(",").map((s) => s.trim()).filter(Boolean)
        }
    }
    return []
}

export const getCategories = (req, res) => {
    const search = (req.query.search || "").trim().toLowerCase()
    const type = (req.query.type || "").trim().toLowerCase()

    if (type && !SKILL_TYPES.includes(type)) {
        return res.status(400).json({ success: false, message: `type must be one of ${SKILL_TYPES.join(", ")}` })
    }

    let list = WORKER_CATEGORIES

    if (type) {
        list = list.filter((c) => c.skill === type)
    }

    if (search) {
        list = list.filter(
            (c) =>
                c.label.toLowerCase().includes(search) ||
                c.value.includes(search) ||
                (c.keywords || []).some((k) => k.includes(search))
        )
    }

    return res.status(200).json({
        success: true,
        count: list.length,
        categories: list.map(({ value, label, skill }) => ({ value, label, skill })),
    })
}

export const registerWorker = async (req, res) => {
    try {
        const { name, address, aadharNumber, companyName, bio, expoToken } = req.body

        let phone
        try {
            phone = await resolveVerifiedPhone(req.body)
        } catch (e) {
            return res.status(401).json({ success: false, message: "Invalid or expired OTP verification" })
        }

        let coordinates = req.body.coordinates
        if (typeof coordinates === "string") {
            try {
                coordinates = JSON.parse(coordinates)
            } catch {
                coordinates = undefined
            }
        }

        const categories = parseCategories(req.body.categories)
        const experienceYears = Number(req.body.experienceYears) || 0
        const workedWithCompany =
            req.body.workedWithCompany === true || req.body.workedWithCompany === "true"

        if (!phone || typeof phone !== "string") {
            return res.status(400).json({ success: false, message: "phone is required" })
        }
        if (!name) {
            return res.status(400).json({ success: false, message: "name is required" })
        }
        if (!aadharNumber) {
            return res.status(400).json({ success: false, message: "aadharNumber is required" })
        }
        if (!req.file) {
            return res.status(400).json({ success: false, message: "aadharPhoto image is required" })
        }
        if (categories.length === 0) {
            return res.status(400).json({ success: false, message: "select at least one work category" })
        }

        const invalid = categories.filter((c) => !CATEGORY_VALUES.includes(c))
        if (invalid.length > 0) {
            return res.status(400).json({ success: false, message: `invalid categories: ${invalid.join(", ")}` })
        }

        if (workedWithCompany && !companyName) {
            return res.status(400).json({ success: false, message: "companyName is required when workedWithCompany is true" })
        }

        const existing = await Worker.findOne({ phone })
        if (existing) {
            return res.status(409).json({ success: false, message: "Worker already registered, please login" })
        }

        const aadharPhotoUrl = await uploadToCloudinary(req.file.buffer)

        const worker = await Worker.create({
            phone,
            name,
            address,
            location:
                Array.isArray(coordinates) && coordinates.length === 2
                    ? { type: "Point", coordinates }
                    : undefined,
            categories,
            experienceYears,
            workedWithCompany,
            companyName,
            bio,
            aadhar: {
                number: aadharNumber,
                photo: aadharPhotoUrl,
            },
            expoToken,
        })

        const token = generateToken(worker._id, "worker")

        return res.status(201).json({
            success: true,
            message: "Registered successfully",
            token,
            worker: formatWorker(worker),
        })
    } catch (err) {
        return serverError(res, err)
    }
}

export const loginWorker = async (req, res) => {
    try {
        const { expoToken } = req.body

        let phone
        try {
            phone = await resolveVerifiedPhone(req.body)
        } catch (e) {
            return res.status(401).json({ success: false, message: "Invalid or expired OTP verification" })
        }

        if (!phone || typeof phone !== "string") {
            return res.status(400).json({ success: false, message: "phone is required" })
        }

        const worker = await Worker.findOne({ phone })
        if (!worker) {
            return res.status(404).json({ success: false, message: "Worker not found, please register" })
        }

        if (expoToken && expoToken !== worker.expoToken) {
            worker.expoToken = expoToken
            await worker.save()
        }

        const token = generateToken(worker._id, "worker")

        return res.status(200).json({
            success: true,
            message: "Login successful",
            token,
            worker: formatWorker(worker),
        })
    } catch (err) {
        return serverError(res, err)
    }
}

export const saveWorkerPushToken = async (req, res) => {
    try {
        const { token } = req.body
        if (!token) {
            return res.status(400).json({ success: false, message: "token is required" })
        }
        await Worker.findByIdAndUpdate(req.workerId, { fcmToken: token })
        return res.status(200).json({ success: true, message: "Push token saved" })
    } catch (err) {
        return serverError(res, err)
    }
}

export const getWorkerProfile = async (req, res) => {
    try {
        const worker = await Worker.findById(req.workerId)
        if (!worker) {
            return res.status(404).json({ success: false, message: "Worker not found" })
        }

        return res.status(200).json({
            success: true,
            message: "Profile fetched",
            worker: formatWorker(worker),
        })
    } catch (err) {
        return serverError(res, err)
    }
}

export const getWorkerStats = async (req, res) => {
    try {
        const now = new Date()
        const startToday = new Date(now.getFullYear(), now.getMonth(), now.getDate())
        const startWeek = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)

        const orders = await Order.find({ assignedWorker: req.workerId })

        let completedJobs = 0
        let activeJobs = 0
        let cancelledJobs = 0
        let totalEarnings = 0
        let todayEarnings = 0
        let weekEarnings = 0

        for (const o of orders) {
            if (o.status === "completed") {
                completedJobs++
                const amt = o.bill?.total || 0
                totalEarnings += amt
                const when = o.completedAt || o.updatedAt
                if (when && when >= startToday) todayEarnings += amt
                if (when && when >= startWeek) weekEarnings += amt
            } else if (o.status === "cancelled") {
                cancelledJobs++
            } else if (["assigned", "in_progress", "awaiting_payment"].includes(o.status)) {
                activeJobs++
            }
        }

        return res.status(200).json({
            success: true,
            stats: {
                totalJobs: orders.length,
                completedJobs,
                activeJobs,
                cancelledJobs,
                totalEarnings,
                todayEarnings,
                weekEarnings,
            },
        })
    } catch (err) {
        return serverError(res, err)
    }
}

export const setWorkerAvailability = async (req, res) => {
    try {
        const isOnline = req.body.isOnline === true || req.body.isOnline === "true"
        const worker = await Worker.findByIdAndUpdate(
            req.workerId,
            { isOnline },
            { new: true }
        )
        if (!worker) {
            return res.status(404).json({ success: false, message: "Worker not found" })
        }
        return res.status(200).json({
            success: true,
            message: isOnline ? "You are online" : "You are offline",
            isOnline: worker.isOnline,
        })
    } catch (err) {
        return serverError(res, err)
    }
}

export const updateWorkerLocation = async (req, res) => {
    try {
        let coordinates = req.body.coordinates
        if (typeof coordinates === "string") {
            try {
                coordinates = JSON.parse(coordinates)
            } catch {
                coordinates = undefined
            }
        }

        if (!Array.isArray(coordinates) || coordinates.length !== 2) {
            return res.status(400).json({ success: false, message: "coordinates [lng, lat] are required" })
        }

        const worker = await Worker.findById(req.workerId)
        if (!worker) {
            return res.status(404).json({ success: false, message: "Worker not found" })
        }

        worker.location = { type: "Point", coordinates }
        if (typeof req.body.address === "string" && req.body.address.trim()) {
            worker.address = req.body.address.trim()
        }
        await worker.save()

        return res.status(200).json({
            success: true,
            message: "Location updated",
            location: worker.location,
        })
    } catch (err) {
        return serverError(res, err)
    }
}

export const getNearbyWorkers = async (req, res) => {
    try {
        const lat = parseFloat(req.query.lat)
        const lng = parseFloat(req.query.lng)
        const radiusKm = parseFloat(req.query.radius) || 5
        const category = req.query.category

        if (Number.isNaN(lat) || Number.isNaN(lng)) {
            return res.status(400).json({ success: false, message: "lat and lng query params are required" })
        }

        if (category && !CATEGORY_VALUES.includes(category)) {
            return res.status(400).json({ success: false, message: `invalid category: ${category}` })
        }

        const match = category ? { categories: category } : {}

        const workers = await Worker.aggregate([
            {
                $geoNear: {
                    near: { type: "Point", coordinates: [lng, lat] },
                    distanceField: "distance",
                    maxDistance: radiusKm * 1000,
                    spherical: true,
                    query: match,
                },
            },
            { $limit: 100 },
        ])

        const result = workers.map((w) => ({
            id: w._id,
            name: w.name,
            phone: w.phone,
            categories: w.categories,
            experienceYears: w.experienceYears,
            workedWithCompany: w.workedWithCompany,
            companyName: w.companyName,
            bio: w.bio,
            isVerified: w.isVerified,
            location: w.location,
            distanceKm: Math.round((w.distance / 1000) * 100) / 100,
        }))

        return res.status(200).json({
            success: true,
            count: result.length,
            radiusKm,
            workers: result,
        })
    } catch (err) {
        return serverError(res, err)
    }
}
