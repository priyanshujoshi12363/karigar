export const RATE_PER_HOUR = 150
export const PLATFORM_FEE = 2
export const CURRENCY = "INR"

export const computeBill = (durationMinutes) => {
    const workAmount = Math.round((RATE_PER_HOUR * durationMinutes) / 60 * 100) / 100
    const total = Math.round((workAmount + PLATFORM_FEE) * 100) / 100
    return {
        ratePerHour: RATE_PER_HOUR,
        durationMinutes,
        workAmount,
        platformFee: PLATFORM_FEE,
        total,
        currency: CURRENCY,
    }
}
