import { Router } from "express"
import { reverseGeocodeAddress } from "../controllers/geo.controller.js"

const router = Router()

router.get("/reverse", reverseGeocodeAddress)

export default router
