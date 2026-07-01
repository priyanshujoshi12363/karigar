
const NOMINATIM_URL = "https://nominatim.openstreetmap.org/reverse"

const USER_AGENT = "KarigarApp/1.0 (support@karigar.app)"


export const reverseGeocode = async (lat, lng, opts = {}) => {
    const { timeoutMs = 7000 } = opts

    if (typeof lat !== "number" || typeof lng !== "number" || Number.isNaN(lat) || Number.isNaN(lng)) {
        throw new Error("reverseGeocode: lat and lng must be valid numbers")
    }
    if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
        throw new Error("reverseGeocode: lat/lng out of range")
    }

    const url =
        `${NOMINATIM_URL}?lat=${lat}&lon=${lng}` +
        `&format=jsonv2&addressdetails=1&accept-language=en`

    const controller = new AbortController()
    const timer = setTimeout(() => controller.abort(), timeoutMs)

    try {
        const res = await fetch(url, {
            headers: {
                "User-Agent": USER_AGENT,
                Accept: "application/json",
            },
            signal: controller.signal,
        })

        if (!res.ok) {
            throw new Error(`Nominatim responded with status ${res.status}`)
        }

        const data = await res.json()
        if (!data || data.error || !data.display_name) {
            return null
        }

        const a = data.address || {}

        return {
            formatted: data.display_name,
            city: a.city || a.town || a.village || a.suburb || a.county || "",
            state: a.state || "",
            postcode: a.postcode || "",
            country: a.country || "",
            raw: a,
        }
    } catch (err) {
        if (err.name === "AbortError") {
            throw new Error("reverseGeocode: request timed out")
        }
        throw err
    } finally {
        clearTimeout(timer)
    }
}

export default reverseGeocode
