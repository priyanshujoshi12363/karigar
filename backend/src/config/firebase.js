import { initializeApp, cert, getApps } from "firebase-admin/app"
import { getAuth as adminGetAuth } from "firebase-admin/auth"
import { getMessaging as adminGetMessaging } from "firebase-admin/messaging"
import fs from "fs"
import path from "path"
import { fileURLToPath } from "url"

let app = null

const normalize = (parsed) => {
    if (parsed && typeof parsed.private_key === "string") {
        parsed.private_key = parsed.private_key.replace(/\\n/g, "\n")
    }
    return parsed
}

const loadServiceAccount = () => {
    if (process.env.FIREBASE_SERVICE_ACCOUNT_B64) {
        const json = Buffer.from(process.env.FIREBASE_SERVICE_ACCOUNT_B64, "base64").toString("utf8")
        return normalize(JSON.parse(json))
    }
    if (process.env.FIREBASE_SERVICE_ACCOUNT) {
        return normalize(JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT))
    }
    const __dirname = path.dirname(fileURLToPath(import.meta.url))
    const filePath =
        process.env.FIREBASE_SERVICE_ACCOUNT_PATH ||
        path.join(__dirname, "../../karigar-ff6f3-firebase-adminsdk-fbsvc-5dfccd68dc.json")
    if (fs.existsSync(filePath)) {
        return normalize(JSON.parse(fs.readFileSync(filePath, "utf8")))
    }
    return null
}

const ensureApp = () => {
    if (!app) {
        if (getApps().length) {
            app = getApps()[0]
            return app
        }
        const serviceAccount = loadServiceAccount()
        if (!serviceAccount) {
            throw new Error("Firebase service account not configured")
        }
        app = initializeApp({ credential: cert(serviceAccount) })
    }
    return app
}

export const getMessaging = () => adminGetMessaging(ensureApp())

export const getAuth = () => adminGetAuth(ensureApp())
