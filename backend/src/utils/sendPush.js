import { getMessaging } from "../config/firebase.js"

export const sendPush = async (token, title, body, data = {}, opts = {}) => {
    if (!token) return
    try {
        const messaging = getMessaging()
        const stringData = {}
        for (const [k, v] of Object.entries(data)) {
            stringData[k] = String(v)
        }

        const message = {
            token,
            data: stringData,
            android: { priority: "high" },
        }

        if (opts.dataOnly) {
            stringData.title = title
            stringData.body = body
            if (opts.ttlSeconds) message.android.ttl = opts.ttlSeconds * 1000
        } else {
            message.notification = { title, body }
            message.android.notification = {
                channelId: "karigar_jobs",
                sound: "default",
                notificationPriority: "PRIORITY_MAX",
                defaultVibrateTimings: false,
                vibrateTimings: ["0s", "0.4s", "0.2s", "0.4s"],
            }
        }

        await messaging.send(message)
    } catch (err) {
        console.error("sendPush failed:", err.message)
    }
}
