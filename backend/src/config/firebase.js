import admin from "firebase-admin"
import fs from "fs"
import path from "path"
import { fileURLToPath } from "url"

let app = null

const loadServiceAccount = () => {
    if (process.env.FIREBASE_SERVICE_ACCOUNT) {
        return JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT)
    }
    const __dirname = path.dirname(fileURLToPath(import.meta.url))
    const filePath =
        process.env.FIREBASE_SERVICE_ACCOUNT_PATH ||
        path.join(__dirname, "../../karigar-ff6f3-firebase-adminsdk-fbsvc-5dfccd68dc.json")
    if (fs.existsSync(filePath)) {
        return JSON.parse(fs.readFileSync(filePath, "utf8"))
    }
    return null
}

const ensureApp = () => {
    if (!app) {
        const serviceAccount = loadServiceAccount()
        if (!serviceAccount) {
            throw new Error("Firebase service account not configured")
        }
        app = admin.initializeApp({
            credential: admin.credential.cert(serviceAccount),
        })
    }
    return app
}

export const getMessaging = () => {
    ensureApp()
    return admin.messaging()
}

export const getAuth = () => {
    ensureApp()
    return admin.auth()
}
