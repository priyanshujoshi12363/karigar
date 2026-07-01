import Worker from "../models/worker.model.js"

export const SEARCH_RADIUS_KM = 5
export const OFFER_TTL_SECONDS = Number(process.env.OFFER_TTL_SECONDS) || 60

export const buildCandidates = async (category, lng, lat) => {
    const workers = await Worker.aggregate([
        {
            $geoNear: {
                near: { type: "Point", coordinates: [lng, lat] },
                distanceField: "distance",
                maxDistance: SEARCH_RADIUS_KM * 1000,
                spherical: true,
                query: { categories: category },
            },
        },
        { $limit: 50 },
    ])

    return workers.map((w) => ({
        worker: w._id,
        distanceKm: Math.round((w.distance / 1000) * 100) / 100,
        status: "pending",
    }))
}

const notifyIndex = (order, i) => {
    order.currentIndex = i
    order.candidates[i].status = "notified"
    order.candidates[i].notifiedAt = new Date()
    order.offerExpiresAt = new Date(Date.now() + OFFER_TTL_SECONDS * 1000)
}

export const startDispatch = (order) => {
    if (!order.candidates.length) {
        order.status = "open"
        order.currentIndex = -1
        order.offerExpiresAt = null
        return order
    }
    order.status = "searching"
    notifyIndex(order, 0)
    return order
}

export const advance = (order, markStatus) => {
    const i = order.currentIndex
    if (i >= 0 && order.candidates[i]) {
        order.candidates[i].status = markStatus
        order.candidates[i].respondedAt = new Date()
    }

    const nextIdx = order.candidates.findIndex((c) => c.status === "pending")
    if (nextIdx === -1) {
        order.status = "open"
        order.currentIndex = -1
        order.offerExpiresAt = null
        return order
    }

    notifyIndex(order, nextIdx)
    return order
}

export const ensureProgress = (order) => {
    while (
        order.status === "searching" &&
        order.offerExpiresAt &&
        order.offerExpiresAt.getTime() < Date.now()
    ) {
        advance(order, "skipped")
    }
    return order
}
