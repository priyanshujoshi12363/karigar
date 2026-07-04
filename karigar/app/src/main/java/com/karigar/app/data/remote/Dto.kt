package com.karigar.app.data.remote

data class LoginRequest(
    val phone: String,
    val idToken: String? = null,
    val expoToken: String? = null
)

data class RegisterRequest(
    val phone: String,
    val name: String,
    val idToken: String? = null,
    val address: String? = null,
    val coordinates: List<Double>? = null,
    val expoToken: String? = null
)

data class UserDto(
    val id: String?,
    val name: String?,
    val phone: String?,
    val address: String?
)

data class AuthResponse(
    val success: Boolean,
    val message: String?,
    val token: String?,
    val user: UserDto?
)

data class VerifyResponse(
    val success: Boolean,
    val message: String?,
    val user: UserDto?
)

data class PushTokenRequest(val token: String)

data class SimpleResponse(val success: Boolean, val message: String?)

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

data class CategoryDto(
    val value: String,
    val label: String,
    val skill: String
)

data class CategoriesResponse(
    val success: Boolean,
    val count: Int,
    val categories: List<CategoryDto>
)

data class CreateOrderRequest(
    val category: String,
    val durationMinutes: Int,
    val coordinates: List<Double>,
    val address: String? = null,
    val notes: String? = null
)

data class BoostRequest(val addAmount: Int)

data class BillDto(
    val total: Double?,
    val workAmount: Double?,
    val platformFee: Double?
)

data class WorkerBriefDto(
    val id: String?,
    val name: String?,
    val phone: String?
)

data class OrderDto(
    val id: String?,
    val category: String?,
    val durationMinutes: Int?,
    val status: String?,
    val bill: BillDto?,
    val assignedWorker: WorkerBriefDto?,
    val startOtp: String?,
    val endOtp: String?,
    val createdAt: String?
)

data class OrdersResponse(
    val success: Boolean,
    val count: Int,
    val orders: List<OrderDto>
)

data class CreateOrderResponse(
    val success: Boolean,
    val message: String?,
    val order: OrderDto?
)
