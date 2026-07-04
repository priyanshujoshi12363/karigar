import mongoose from "mongoose"
import { encrypt, decrypt } from "../utils/crypto.js"
import { CATEGORY_VALUES } from "../constants/categories.js"

const workerSchema = new mongoose.Schema(
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
        aadhar: {
            number: {
                type: String,
                set: encrypt,
                get: decrypt,
            },
            photo: {
                type: String,
                set: encrypt,
                get: decrypt,
            },
        },
        categories: {
            type: [String],
            enum: CATEGORY_VALUES,
            default: [],
            index: true,
        },
        experienceYears: {
            type: Number,
            default: 0,
            min: 0,
        },
        workedWithCompany: {
            type: Boolean,
            default: false,
        },
        companyName: {
            type: String,
            trim: true,
        },
        bio: {
            type: String,
            trim: true,
        },
        isVerified: {
            type: Boolean,
            default: false,
        },
        isOnline: {
            type: Boolean,
            default: false,
            index: true,
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

workerSchema.index({ location: "2dsphere" })

const Worker = mongoose.model("Worker", workerSchema)

export default Worker
