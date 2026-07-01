import User from "../models/user.model.js"
import { generateToken } from "../utils/generateToken.js"
import { reverseGeocode } from "../utils/reverseGeocode.js"

const formatUser = (user) => ({
    id: user._id,
    name: user.name,
    phone: user.phone,
    address: user.address,
    location: user.location,
    addressHistory: (user.addressHistory || []).map((h) => ({
        address: h.address,
        location: h.location,
        changedAt: h.changedAt,
    })),
    expoToken: user.expoToken,
    createdAt: user.createdAt,
})

export const register = async (req, res) => {
    try {
        const { phone, name, address, coordinates, expoToken } = req.body

        if (!phone) {
            return res.status(400).json({ success: false, message: "phone is required" })
        }
        if (!name) {
            return res.status(400).json({ success: false, message: "name is required" })
        }

        const existing = await User.findOne({ phone })
        if (existing) {
            return res
                .status(409)
                .json({ success: false, message: "User already registered, please login" })
        }

        const user = await User.create({
            phone,
            name,
            address,
            location:
                Array.isArray(coordinates) && coordinates.length === 2
                    ? { type: "Point", coordinates }
                    : undefined,
            expoToken,
        })

        const token = generateToken(user._id)

        return res.status(201).json({
            success: true,
            message: "Registered successfully",
            token,
            user: formatUser(user),
        })
    } catch (err) {
        return res.status(500).json({ success: false, message: err.message })
    }
}

export const login = async (req, res) => {
    try {
        const { phone, expoToken } = req.body

        if (!phone) {
            return res.status(400).json({ success: false, message: "phone is required" })
        }

        const user = await User.findOne({ phone })
        if (!user) {
            return res
                .status(404)
                .json({ success: false, message: "User not found, please register" })
        }

        if (expoToken && expoToken !== user.expoToken) {
            user.expoToken = expoToken
            await user.save()
        }

        const token = generateToken(user._id)

        return res.status(200).json({
            success: true,
            message: "Login successful",
            token,
            user: formatUser(user),
        })
    } catch (err) {
        return res.status(500).json({ success: false, message: err.message })
    }
}

export const getProfile = async (req, res) => {
    try {
        const user = await User.findById(req.userId)
        if (!user) {
            return res.status(404).json({ success: false, message: "User not found" })
        }

        return res.status(200).json({
            success: true,
            message: "Profile fetched",
            user: formatUser(user),
        })
    } catch (err) {
        return res.status(500).json({ success: false, message: err.message })
    }
}

export const updateAddress = async (req, res) => {
    try {
        let { address } = req.body

        let coordinates = req.body.coordinates
        if (typeof coordinates === "string") {
            try {
                coordinates = JSON.parse(coordinates)
            } catch {
                coordinates = undefined
            }
        }

        const hasCoords = Array.isArray(coordinates) && coordinates.length === 2

        if (!address && !hasCoords) {
            return res.status(400).json({ success: false, message: "address or coordinates required" })
        }

        const user = await User.findById(req.userId)
        if (!user) {
            return res.status(404).json({ success: false, message: "User not found" })
        }

        if (!address && hasCoords) {
            try {
                const geo = await reverseGeocode(coordinates[1], coordinates[0])
                if (geo) address = geo.formatted
            } catch {
                address = undefined
            }
        }

        if (user.address || (user.location && user.location.coordinates)) {
            user.addressHistory.push({
                address: user.address,
                location: {
                    type: "Point",
                    coordinates: user.location?.coordinates || [0, 0],
                },
                changedAt: new Date(),
            })
        }

        if (address) {
            user.address = address
        }
        if (hasCoords) {
            user.location = { type: "Point", coordinates }
        }

        await user.save()

        return res.status(200).json({
            success: true,
            message: "Address updated",
            user: formatUser(user),
        })
    } catch (err) {
        return res.status(500).json({ success: false, message: err.message })
    }
}
