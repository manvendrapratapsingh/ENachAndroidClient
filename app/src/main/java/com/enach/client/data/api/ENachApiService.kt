package com.enach.client.data.api

import com.enach.client.data.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ENachApiService {
    
    // Authentication Endpoints
    @POST("/api/v1/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<TokenResponse>
    
    @POST("/api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<TokenResponse>
    
    // Health Check
    @GET("/health")
    suspend fun healthCheck(): Response<HealthCheckResponse>
    
    // Job Processing Endpoints
    @Multipart
    @POST("/api/v1/jobs")
    suspend fun createJob(
        @Header("Authorization") token: String,
        @Part chequeImage: MultipartBody.Part,
        @Part enachForm: MultipartBody.Part?,
        @Part("customer_identifier") customerIdentifier: RequestBody?,
        @Part("customer_name") customerName: RequestBody?,
        @Part("customer_email") customerEmail: RequestBody?,
        @Part("customer_mobile") customerMobile: RequestBody?
    ): Response<JobStatusResponse>
    
    @GET("/api/v1/jobs/{jobId}")
    suspend fun getJobStatus(
        @Header("Authorization") token: String,
        @Path("jobId") jobId: String
    ): Response<JobStatusResponse>
    
    @GET("/api/v1/jobs/{jobId}/form")
    @Streaming
    suspend fun downloadForm(
        @Header("Authorization") token: String,
        @Path("jobId") jobId: String
    ): Response<ResponseBody>
    
    @POST("/api/v1/jobs/{jobId}/validate")
    suspend fun validateJob(
        @Header("Authorization") token: String,
        @Path("jobId") jobId: String,
        @Body request: ValidationRequest
    ): Response<ValidationResponse>
    
    @GET("/api/v1/jobs")
    suspend fun listJobs(
        @Header("Authorization") token: String,
        @Query("status") status: String? = null,
        @Query("customer_identifier") customerIdentifier: String? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<List<JobStatusResponse>>
    
    // Real-time job results endpoint for better mobile UX
    @GET("/api/v1/jobs/{jobId}/results")
    suspend fun getJobResults(
        @Header("Authorization") token: String,
        @Path("jobId") jobId: String
    ): Response<JobResultsResponse>
    
    // Live status endpoint for real-time updates
    @GET("/api/v1/jobs/{jobId}/live")
    suspend fun getJobLiveStatus(
        @Header("Authorization") token: String,
        @Path("jobId") jobId: String
    ): Response<JobLiveStatusResponse>
}
