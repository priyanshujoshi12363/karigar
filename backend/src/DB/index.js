import mongoose from "mongoose"

// Connects to MongoDB using the URI from .env
const connectDB = async () => {
    const uri = process.env.MONGODB_URI
    if (!uri) {
        throw new Error("MONGODB_URI is not defined in .env")
    }

    const conn = await mongoose.connect(uri)
    console.log(`MongoDB connected: ${conn.connection.host}`)
    return conn
}

export default connectDB
