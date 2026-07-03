import { getMessaging } from "../config/firebase.js"

export const sendPush = async (token, title, body, data = {}) => {
    if (!token) return
    try {
        const messaging = getMessaging()
        const stringData = {}
        for (const [k, v] of Object.entries(data)) {
            stringData[k] = String(v)
        }
        await messaging.send({
            token,
            notification: { title, body },
            data: stringData,
            android: {
                priority: "high",
                notification: { channelId: "orders" },
            },
        })
    } catch (err) {
        console.error("sendPush failed:", err.message)
    }
}
