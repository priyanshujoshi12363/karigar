export const serverError = (res, err) => {
    console.error(err)
    return res.status(500).json({
        success: false,
        message: "Something went wrong. Please try again.",
    })
}
