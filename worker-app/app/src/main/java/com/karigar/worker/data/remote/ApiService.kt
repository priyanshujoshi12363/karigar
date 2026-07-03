package com.karigar.worker.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("worker/login")
    suspend fun workerLogin(@Body body: LoginRequest): AuthResponse

    @GET("worker/me")
    suspend fun workerMe(@Header("Authorization") bearer: String): SimpleResponse

    @PATCH("worker/push-token")
    suspend fun savePushToken(
        @Header("Authorization") bearer: String,
        @Body body: PushTokenRequest
    ): SimpleResponse

    @GET("worker/orders/offers")
    suspend fun getOffers(@Header("Authorization") bearer: String): OffersResponse

    @POST("worker/orders/{id}/respond")
    suspend fun respondOffer(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
        @Body body: RespondRequest
    ): SimpleResponse

    @GET("worker/orders/jobs")
    suspend fun getJobs(@Header("Authorization") bearer: String): JobsResponse

    @POST("worker/orders/{id}/pick")
    suspend fun pickJob(
        @Header("Authorization") bearer: String,
        @Path("id") id: String
    ): SimpleResponse

    @GET("worker/orders")
    suspend fun getWorkerOrders(@Header("Authorization") bearer: String): WorkerOrdersResponse

    @POST("worker/orders/{id}/start")
    suspend fun startWork(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
        @Body body: OtpRequest
    ): SimpleResponse

    @POST("worker/orders/{id}/finish")
    suspend fun finishWork(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
        @Body body: OtpRequest
    ): SimpleResponse

    @POST("worker/orders/{id}/confirm-payment")
    suspend fun confirmPayment(
        @Header("Authorization") bearer: String,
        @Path("id") id: String
    ): SimpleResponse
}
