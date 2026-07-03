const scrub = (obj) => {
    if (!obj || typeof obj !== "object") return
    for (const key of Object.keys(obj)) {
        if (key.startsWith("$") || key.includes(".")) {
            delete obj[key]
        } else {
            scrub(obj[key])
        }
    }
}

export const sanitizeBody = (req, res, next) => {
    scrub(req.body)
    next()
}
