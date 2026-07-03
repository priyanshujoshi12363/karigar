import mongoose from "mongoose"
import { encrypt, decrypt } from "../utils/crypto.js"

const previousAddressSchema = new mongoose.Schema(
    {
        address: {
            type: String,
            set: encrypt,
            get: decrypt,
        },
        location: {
            type: {
                type: String,
                enum: ["Point"],
                default: "Point",
            },
            coordinates: {
                type: [Number],
                default: [0, 0],
            },
        },
        changedAt: {
            type: Date,
            default: Date.now,
        },
    },
    { _id: false, toJSON: { getters: true }, toObject: { getters: true } }
)

const userSchema = new mongoose.Schema(
    {
        name: {
            type: String,
            trim: true,
        },
        phone: {
            type: String,
            required: true,
            unique: true,
            trim: true,
            index: true,
        },
        address: {
            type: String,
            set: encrypt,
            get: decrypt,
        },
        location: {
            type: {
                type: String,
                enum: ["Point"],
                default: "Point",
            },
            coordinates: {
                type: [Number], 
                default: [0, 0],
            },
        },
        addressHistory: {
            type: [previousAddressSchema],
            default: [],
        },
        searchHistory: {
            type: [
                {
                    _id: false,
                    query: { type: String, trim: true },
                    at: { type: Date, default: Date.now },
                },
            ],
            default: [],
        },
        recentlyViewed: {
            type: [
                {
                    _id: false,
                    worker: { type: mongoose.Schema.Types.ObjectId, ref: "Worker" },
                    at: { type: Date, default: Date.now },
                },
            ],
            default: [],
        },
        expoToken: {
            type: String,
            default: null,
        },
        fcmToken: {
            type: String,
            default: null,
        },
    },
    { timestamps: true, toJSON: { getters: true }, toObject: { getters: true } }
)
userSchema.index({ location: "2dsphere" })

const User = mongoose.model("User", userSchema)

export default User
