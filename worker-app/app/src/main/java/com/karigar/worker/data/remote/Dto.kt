package com.karigar.worker.data.remote

data class LoginRequest(val phone: String, val idToken: String? = null, val expoToken: String? = null)

data class AuthResponse(
    val success: Boolean,
    val message: String?,
    val token: String?
)

data class SimpleResponse(val success: Boolean, val message: String?)

data class WorkerProfileDto(
    val id: String?,
    val name: String?,
    val phone: String?,
    val categories: List<String>?,
    val experienceYears: Int?,
    val workedWithCompany: Boolean?,
    val companyName: String?,
    val isVerified: Boolean?,
    val isOnline: Boolean?,
    val address: String?,
    val createdAt: String?
)

data class WorkerProfileResponse(val success: Boolean, val message: String?, val worker: WorkerProfileDto?)

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

data class GeoPointDto(val type: String?, val coordinates: List<Double>?)

data class CustomerDto(val phone: String?, val address: String?, val location: GeoPointDto?)

data class WorkerPaymentDto(
    val amount: Double?,
    val workerPayout: Double?,
    val platformFee: Double?,
    val upiUri: String?
)

data class WorkerOrderDto(
    val id: String?,
    val category: String?,
    val durationMinutes: Int?,
    val status: String?,
    val earning: Double?,
    val customer: CustomerDto?,
    val payment: WorkerPaymentDto?,
    val completedAt: String?,
    val createdAt: String?
)

data class WorkerStatsDto(
    val totalJobs: Int?,
    val completedJobs: Int?,
    val activeJobs: Int?,
    val cancelledJobs: Int?,
    val totalEarnings: Double?,
    val todayEarnings: Double?,
    val weekEarnings: Double?
)

data class WorkerStatsResponse(val success: Boolean, val stats: WorkerStatsDto?)

data class WorkerOrdersResponse(val success: Boolean, val count: Int, val orders: List<WorkerOrderDto>)

data class OtpRequest(val otp: String)
data class RespondRequest(val action: String)
data class PushTokenRequest(val token: String)

data class UpdateLocationRequest(val coordinates: List<Double>, val address: String? = null)

data class AddressDto(
    val formatted: String?,
    val city: String?,
    val state: String?,
    val postcode: String?
)

data class GeoResponse(
    val success: Boolean,
    val message: String?,
    val address: AddressDto?
)
