import mongoose from "mongoose"
import { encrypt, decrypt } from "../utils/crypto.js"
import { CATEGORY_VALUES } from "../constants/categories.js"

const candidateSchema = new mongoose.Schema(
    {
        worker: { type: mongoose.Schema.Types.ObjectId, ref: "Worker" },
        distanceKm: { type: Number, default: 0 },
        status: {
            type: String,
            enum: ["pending", "notified", "rejected", "accepted", "skipped"],
            default: "pending",
        },
        notifiedAt: { type: Date },
        respondedAt: { type: Date },
    },
    { _id: false }
)

const orderSchema = new mongoose.Schema(
    {
        user: { type: mongoose.Schema.Types.ObjectId, ref: "User", required: true, index: true },
        userPhone: { type: String },
        category: { type: String, enum: CATEGORY_VALUES, required: true, index: true },
        durationMinutes: { type: Number, required: true },
        notes: { type: String, trim: true },
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
        address: {
            type: String,
            set: encrypt,
            get: decrypt,
        },
        bill: {
            ratePerHour: Number,
            durationMinutes: Number,
            workAmount: Number,
            platformFee: Number,
            bonus: { type: Number, default: 0 },
            total: Number,
            currency: String,
        },
        status: {
            type: String,
            enum: ["searching", "assigned", "in_progress", "awaiting_payment", "open", "expired", "completed", "cancelled"],
            default: "searching",
            index: true,
        },
        assignedWorker: { type: mongoose.Schema.Types.ObjectId, ref: "Worker", index: true },
        candidates: { type: [candidateSchema], default: [] },
        currentIndex: { type: Number, default: -1 },
        offerExpiresAt: { type: Date },
        openExpiresAt: { type: Date },
        boostCount: { type: Number, default: 0 },
        noWorkersNotified: { type: Boolean, default: false },
        startOtp: { type: String },
        endOtp: { type: String },
        payment: {
            status: { type: String, enum: ["pending", "paid"], default: "pending" },
            amount: { type: Number },
            workerPayout: { type: Number },
            platformFee: { type: Number },
            method: { type: String },
            transactionId: { type: String },
            paidAt: { type: Date },
        },
        acceptedAt: { type: Date },
        startedAt: { type: Date },
        completedAt: { type: Date },
        cancelledAt: { type: Date },
    },
    { timestamps: true, toJSON: { getters: true }, toObject: { getters: true } }
)

orderSchema.index({ location: "2dsphere" })
orderSchema.index({ "candidates.worker": 1 })

const Order = mongoose.model("Order", orderSchema)

export default Order
