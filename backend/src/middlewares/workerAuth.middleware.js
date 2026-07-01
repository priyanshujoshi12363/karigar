import jwt from "jsonwebtoken"

export const protectWorker = (req, res, next) => {
    try {
        const header = req.headers.authorization

        if (!header || !header.startsWith("Bearer ")) {
            return res.status(401).json({ success: false, message: "Not authorized, token missing" })
        }

        const token = header.split(" ")[1]
        const decoded = jwt.verify(token, process.env.JWT_SECRET)

        if (decoded.role !== "worker") {
            return res.status(403).json({ success: false, message: "Not authorized as a worker" })
        }

        req.workerId = decoded.id
        next()
    } catch {
        return res.status(401).json({ success: false, message: "Not authorized, invalid token" })
    }
}
