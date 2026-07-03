import "dotenv/config"

import express from "express"
import helmet from "helmet"
import cors from "cors"
import rateLimit from "express-rate-limit"
import connectDB from "./src/DB/index.js"
import { sanitizeBody } from "./src/middlewares/sanitize.middleware.js"
import authRoutes from "./src/routes/auth.routes.js"
import geoRoutes from "./src/routes/geo.routes.js"
import workerRoutes from "./src/routes/worker.routes.js"
import userRoutes from "./src/routes/user.routes.js"

const requiredEnv = ["MONGODB_URI", "JWT_SECRET", "ENCRYPTION_KEY"]
const missing = requiredEnv.filter((k) => !process.env[k])
if (missing.length) {
    console.error("Missing required environment variables:", missing.join(", "))
    process.exit(1)
}

const app = express()

app.set("trust proxy", 1)
app.use(helmet())

const allowedOrigins = (process.env.ALLOWED_ORIGINS || "")
    .split(",")
    .map((s) => s.trim())
    .filter(Boolean)
app.use(cors({ origin: allowedOrigins.length ? allowedOrigins : true }))

app.use(express.json({ limit: "1mb" }))
app.use(sanitizeBody)

const apiLimiter = rateLimit({
    windowMs: 15 * 60 * 1000,
    max: 300,
    standardHeaders: true,
    legacyHeaders: false,
    message: { success: false, message: "Too many requests, please try again later." },
})
const authLimiter = rateLimit({
    windowMs: 15 * 60 * 1000,
    max: 40,
    standardHeaders: true,
    legacyHeaders: false,
    message: { success: false, message: "Too many attempts, please try again later." },
})

app.get("/", (req, res) => res.send("Karigar API is running"))
app.get("/health", (req, res) => res.status(200).json({ status: "ok" }))

app.use("/api", apiLimiter)
app.use("/api/v1/auth", authLimiter, authRoutes)
app.use("/api/v1/geo", geoRoutes)
app.use("/api/v1/worker", workerRoutes)
app.use("/api/v1/user", userRoutes)

app.use((req, res) => {
    res.status(404).json({ success: false, message: "Route not found" })
})

app.use((err, req, res, next) => {
    if (err?.type === "entity.parse.failed") {
        return res.status(400).json({ success: false, message: "Invalid JSON body" })
    }
    if (err?.type === "entity.too.large") {
        return res.status(413).json({ success: false, message: "Payload too large" })
    }
    if (err?.name === "MulterError" || /images are allowed/.test(err?.message || "")) {
        return res.status(400).json({ success: false, message: err.message })
    }
    console.error(err)
    return res.status(500).json({ success: false, message: "Something went wrong. Please try again." })
})

const port = process.env.PORT || 4000

connectDB()
    .then(() => {
        app.listen(port, () => {
            console.log(`server is running on the port ${port}`)
        })
    })
    .catch((err) => {
        console.error("Failed to connect to MongoDB:", err.message)
        process.exit(1)
    })
