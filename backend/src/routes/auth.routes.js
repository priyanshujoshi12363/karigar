import { Router } from "express"
import { register, login, getProfile, updateAddress, verifyToken, savePushToken } from "../controllers/auth.controller.js"
import { protect } from "../middlewares/auth.middleware.js"

const router = Router()

router.post("/register", register)
router.post("/login", login)
router.get("/verify", protect, verifyToken)
router.get("/me", protect, getProfile)
router.patch("/address", protect, updateAddress)
router.patch("/push-token", protect, savePushToken)

export default router
