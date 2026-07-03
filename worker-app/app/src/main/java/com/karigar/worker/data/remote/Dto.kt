package com.karigar.worker.data.remote

data class LoginRequest(val phone: String, val expoToken: String? = null)

data class AuthResponse(
    val success: Boolean,
    val message: String?,
    val token: String?
)

data class SimpleResponse(val success: Boolean, val message: String?)

data class OfferDto(
    val id: String?,
    val category: String?,
    val durationMinutes: Int?,
    val earning: Double?,
    val distanceKm: Double?
)

data class OffersResponse(val success: Boolean, val count: Int, val offers: List<OfferDto>)

data class JobDto(
    val id: String?,
    val category: String?,
    val durationMinutes: Int?,
    val earning: Double?,
    val distanceKm: Double?
)

data class JobsResponse(val success: Boolean, val count: Int, val jobs: List<JobDto>)

data class CustomerDto(val phone: String?, val address: String?)

data class WorkerPaymentDto(val amount: Double?, val upiUri: String?)

data class WorkerOrderDto(
    val id: String?,
    val category: String?,
    val durationMinutes: Int?,
    val status: String?,
    val earning: Double?,
    val customer: CustomerDto?,
    val payment: WorkerPaymentDto?,
    val createdAt: String?
)

data class WorkerOrdersResponse(val success: Boolean, val count: Int, val orders: List<WorkerOrderDto>)

data class OtpRequest(val otp: String)
data class RespondRequest(val action: String)
data class PushTokenRequest(val token: String)
