import "dotenv/config"

import express from "express"
import connectDB from "./src/DB/index.js"
import authRoutes from "./src/routes/auth.routes.js"
import geoRoutes from "./src/routes/geo.routes.js"
import workerRoutes from "./src/routes/worker.routes.js"
import userRoutes from "./src/routes/user.routes.js"

const app = express()

app.use(express.json())

app.get("/", (req, res) => {
    res.send("Karigar API is running")
})
app.use("/api/v1/auth", authRoutes)
app.use("/api/v1/geo", geoRoutes)
app.use("/api/v1/worker", workerRoutes)
app.use("/api/v1/user", userRoutes)

app.use((err, req, res, next) => {
    return res.status(400).json({ success: false, message: err.message })
})

const port = process.env.PORT

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
