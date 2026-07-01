import { Router } from "express"
import {
    explore,
    searchWorkers,
    getWorkerById,
    getHistory,
    clearHistory,
} from "../controllers/user.controller.js"
import {
    getQuote,
    createOrder,
    getUserOrders,
    getUserOrder,
    cancelOrder,
    payOrder,
} from "../controllers/order.controller.js"
import { protect } from "../middlewares/auth.middleware.js"

const router = Router()

router.get("/explore", protect, explore)
router.get("/search", protect, searchWorkers)
router.get("/history", protect, getHistory)
router.delete("/history", protect, clearHistory)

router.get("/orders/quote", protect, getQuote)
router.post("/orders", protect, createOrder)
router.get("/orders", protect, getUserOrders)
router.get("/orders/:id", protect, getUserOrder)
router.post("/orders/:id/cancel", protect, cancelOrder)
router.post("/orders/:id/pay", protect, payOrder)

router.get("/workers/:id", protect, getWorkerById)

export default router
