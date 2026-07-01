import jwt from "jsonwebtoken"

export const protect = (req, res, next) => {
    try {
        const header = req.headers.authorization

        if (!header || !header.startsWith("Bearer ")) {
            return res
                .status(401)
                .json({ success: false, message: "Not authorized, token missing" })
        }

        const token = header.split(" ")[1]
        const decoded = jwt.verify(token, process.env.JWT_SECRET)

        req.userId = decoded.id
        next()
    } catch (err) {
        return res.status(401).json({ success: false, message: "Not authorized, invalid token" })
    }
}
