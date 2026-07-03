package com.karigar.app.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @GET("auth/verify")
    suspend fun verify(@Header("Authorization") bearer: String): VerifyResponse

    @GET("auth/me")
    suspend fun getMe(@Header("Authorization") bearer: String): VerifyResponse

    @PATCH("auth/push-token")
    suspend fun savePushToken(
        @Header("Authorization") bearer: String,
        @Body body: PushTokenRequest
    ): SimpleResponse

    @GET("worker/categories")
    suspend fun getCategories(
        @Query("type") type: String? = null,
        @Query("search") search: String? = null
    ): CategoriesResponse

    @POST("user/orders")
    suspend fun createOrder(
        @Header("Authorization") bearer: String,
        @Body body: CreateOrderRequest
    ): CreateOrderResponse

    @GET("user/orders")
    suspend fun getOrders(@Header("Authorization") bearer: String): OrdersResponse
}
