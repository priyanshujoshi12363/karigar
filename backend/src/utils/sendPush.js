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
                notification: {
                    channelId: "karigar_jobs",
                    sound: "default",
                    notificationPriority: "PRIORITY_MAX",
                    defaultVibrateTimings: false,
                    vibrateTimings: ["0s", "0.4s", "0.2s", "0.4s"],
                },
            },
        })
    } catch (err) {
        console.error("sendPush failed:", err.message)
    }
}
