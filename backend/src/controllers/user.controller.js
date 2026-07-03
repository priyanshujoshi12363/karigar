import { serverError } from "../utils/handleError.js"
import mongoose from "mongoose"
import Worker from "../models/worker.model.js"
import User from "../models/user.model.js"
import { WORKER_CATEGORIES, CATEGORY_VALUES } from "../constants/categories.js"

const publicWorker = (w, withDistance = false) => {
    const data = {
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
    }
    if (withDistance && typeof w.distance === "number") {
        data.distanceKm = Math.round((w.distance / 1000) * 100) / 100
    }
    return data
}

const categoryList = () => WORKER_CATEGORIES.map(({ value, label }) => ({ value, label }))

const matchCategoriesByText = (q) => {
    const s = q.toLowerCase()
    return WORKER_CATEGORIES.filter(
        (c) =>
            c.label.toLowerCase().includes(s) ||
            c.value.includes(s) ||
            (c.keywords || []).some((k) => k.includes(s))
    ).map((c) => c.value)
}

export const explore = async (req, res) => {
    try {
        const lat = parseFloat(req.query.lat)
        const lng = parseFloat(req.query.lng)
        const radiusKm = parseFloat(req.query.radius) || 5

        let nearbyWorkers = []
        if (!Number.isNaN(lat) && !Number.isNaN(lng)) {
            const workers = await Worker.aggregate([
                {
                    $geoNear: {
                        near: { type: "Point", coordinates: [lng, lat] },
                        distanceField: "distance",
                        maxDistance: radiusKm * 1000,
                        spherical: true,
                    },
                },
                { $limit: 20 },
            ])
            nearbyWorkers = workers.map((w) => publicWorker(w, true))
        }

        return res.status(200).json({
            success: true,
            categories: categoryList(),
            nearbyWorkers,
        })
    } catch (err) {
        return serverError(res, err)
    }
}

export const searchWorkers = async (req, res) => {
    try {
        const q = (req.query.q || "").trim()
        const lat = parseFloat(req.query.lat)
        const lng = parseFloat(req.query.lng)
        const radiusKm = parseFloat(req.query.radius) || 10
        const category = req.query.category

        const filter = {}

        if (category) {
            if (!CATEGORY_VALUES.includes(category)) {
                return res.status(400).json({ success: false, message: `invalid category: ${category}` })
            }
            filter.categories = category
        } else if (q) {
            const matched = matchCategoriesByText(q)
            const or = [{ name: { $regex: q, $options: "i" } }]
            if (matched.length) or.push({ categories: { $in: matched } })
            filter.$or = or
        }

        let workers
        let withDistance = false

        if (!Number.isNaN(lat) && !Number.isNaN(lng)) {
            withDistance = true
            workers = await Worker.aggregate([
                {
                    $geoNear: {
                        near: { type: "Point", coordinates: [lng, lat] },
                        distanceField: "distance",
                        maxDistance: radiusKm * 1000,
                        spherical: true,
                        query: filter,
                    },
                },
                { $limit: 50 },
            ])
        } else {
            workers = await Worker.find(filter).limit(50).lean()
        }

        if (q) {
            await User.findByIdAndUpdate(req.userId, {
                $push: {
                    searchHistory: { $each: [{ query: q, at: new Date() }], $position: 0, $slice: 20 },
                },
            })
        }

        return res.status(200).json({
            success: true,
            count: workers.length,
            workers: workers.map((w) => publicWorker(w, withDistance)),
        })
    } catch (err) {
        return serverError(res, err)
    }
}

export const getWorkerById = async (req, res) => {
    try {
        const { id } = req.params
        if (!mongoose.Types.ObjectId.isValid(id)) {
            return res.status(400).json({ success: false, message: "invalid worker id" })
        }

        const worker = await Worker.findById(id)
        if (!worker) {
            return res.status(404).json({ success: false, message: "Worker not found" })
        }

        await User.findByIdAndUpdate(req.userId, {
            $push: {
                recentlyViewed: { $each: [{ worker: worker._id, at: new Date() }], $position: 0, $slice: 20 },
            },
        })

        return res.status(200).json({
            success: true,
            worker: publicWorker(worker),
        })
    } catch (err) {
        return serverError(res, err)
    }
}

export const getHistory = async (req, res) => {
    try {
        const user = await User.findById(req.userId).populate(
            "recentlyViewed.worker",
            "name categories experienceYears location isVerified"
        )
        if (!user) {
            return res.status(404).json({ success: false, message: "User not found" })
        }

        const recentlyViewed = (user.recentlyViewed || [])
            .filter((r) => r.worker)
            .map((r) => ({
                id: r.worker._id,
                name: r.worker.name,
                categories: r.worker.categories,
                experienceYears: r.worker.experienceYears,
                location: r.worker.location,
                isVerified: r.worker.isVerified,
                at: r.at,
            }))

        const addressHistory = (user.addressHistory || []).map((h) => ({
            address: h.address,
            location: h.location,
            changedAt: h.changedAt,
        }))

        return res.status(200).json({
            success: true,
            searchHistory: user.searchHistory || [],
            recentlyViewed,
            addressHistory,
        })
    } catch (err) {
        return serverError(res, err)
    }
}

export const clearHistory = async (req, res) => {
    try {
        await User.findByIdAndUpdate(req.userId, {
            $set: { searchHistory: [], recentlyViewed: [] },
        })
        return res.status(200).json({ success: true, message: "History cleared" })
    } catch (err) {
        return serverError(res, err)
    }
}
