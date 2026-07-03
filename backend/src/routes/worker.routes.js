import { Router } from "express"
import {
    registerWorker,
    loginWorker,
    getWorkerProfile,
    getCategories,
    updateWorkerLocation,
    getNearbyWorkers,
    saveWorkerPushToken,
} from "../controllers/worker.controller.js"
import {
    getWorkerOffers,
    respondToOffer,
    getOpenJobs,
    pickJob,
    getWorkerOrders,
    startWork,
    finishWork,
    confirmPayment,
} from "../controllers/order.controller.js"
import { protectWorker } from "../middlewares/workerAuth.middleware.js"
import { protect } from "../middlewares/auth.middleware.js"
import { upload } from "../middlewares/upload.middleware.js"

const router = Router()

router.get("/categories", getCategories)
router.post("/register", upload.single("aadharPhoto"), registerWorker)
router.post("/login", loginWorker)
router.get("/me", protectWorker, getWorkerProfile)
router.patch("/location", protectWorker, updateWorkerLocation)
router.patch("/push-token", protectWorker, saveWorkerPushToken)
router.get("/nearby", protect, getNearbyWorkers)

router.get("/orders/offers", protectWorker, getWorkerOffers)
router.get("/orders/jobs", protectWorker, getOpenJobs)
router.get("/orders", protectWorker, getWorkerOrders)
router.post("/orders/:id/respond", protectWorker, respondToOffer)
router.post("/orders/:id/pick", protectWorker, pickJob)
router.post("/orders/:id/start", protectWorker, startWork)
router.post("/orders/:id/finish", protectWorker, finishWork)
router.post("/orders/:id/confirm-payment", protectWorker, confirmPayment)

export default router
