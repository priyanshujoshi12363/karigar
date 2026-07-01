import reverseGeocode from "../utils/reverseGeocode.js"

export const reverseGeocodeAddress = async (req, res) => {
    try {
        const lat = parseFloat(req.query.lat)
        const lng = parseFloat(req.query.lng)

        if (Number.isNaN(lat) || Number.isNaN(lng)) {
            return res.status(400).json({
                success: false,
                message: "lat and lng query params are required and must be numbers",
            })
        }

        const address = await reverseGeocode(lat, lng)

        if (!address) {
            return res.status(404).json({
                success: false,
                message: "No address found for the given coordinates",
            })
        }

        return res.status(200).json({
            success: true,
            message: "Address resolved",
            address,
        })
    } catch (err) {
        return res.status(502).json({ success: false, message: err.message })
    }
}
