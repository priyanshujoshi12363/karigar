import jwt from "jsonwebtoken"

// Signs a JWT containing the user's id
export const generateToken = (userId, role = "user") => {
    return jwt.sign({ id: userId, role }, process.env.JWT_SECRET, {
        expiresIn: process.env.JWT_EXPIRY || "30d",
    })
}
