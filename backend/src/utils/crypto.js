import crypto from "crypto"

const ALGORITHM = "aes-256-gcm"

const getKey = () => {
    const hex = process.env.ENCRYPTION_KEY
    if (!hex) {
        throw new Error("ENCRYPTION_KEY is not defined in .env")
    }
    const key = Buffer.from(hex, "hex")
    if (key.length !== 32) {
        throw new Error("ENCRYPTION_KEY must be 32 bytes (64 hex characters)")
    }
    return key
}

export const encrypt = (value) => {
    if (value === null || value === undefined || value === "") {
        return value
    }

    const iv = crypto.randomBytes(12)
    const cipher = crypto.createCipheriv(ALGORITHM, getKey(), iv)

    let encrypted = cipher.update(String(value), "utf8", "hex")
    encrypted += cipher.final("hex")

    const authTag = cipher.getAuthTag().toString("hex")

    return `${iv.toString("hex")}:${authTag}:${encrypted}`
}

export const decrypt = (payload) => {
    if (payload === null || payload === undefined || payload === "") {
        return payload
    }

    if (typeof payload !== "string") {
        return payload
    }

    const parts = payload.split(":")
    if (parts.length !== 3) {
        return payload
    }

    try {
        const [ivHex, tagHex, dataHex] = parts
        const decipher = crypto.createDecipheriv(ALGORITHM, getKey(), Buffer.from(ivHex, "hex"))
        decipher.setAuthTag(Buffer.from(tagHex, "hex"))

        let decrypted = decipher.update(dataHex, "hex", "utf8")
        decrypted += decipher.final("utf8")

        return decrypted
    } catch {
        return payload
    }
}
