package com.karigar.app.data.remote

data class LoginRequest(
    val phone: String,
    val expoToken: String? = null
)

data class RegisterRequest(
    val phone: String,
    val name: String,
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
