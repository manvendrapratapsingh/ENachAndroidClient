package com.enach.client.data.repository

import com.enach.client.data.api.ENachApiService
import com.enach.client.data.models.*
import com.enach.client.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ENachRepository @Inject constructor(
    private val apiService: ENachApiService,
    private val tokenManager: AuthTokenManager
) {
    
    // Authentication
    suspend fun login(username: String, password: String): Flow<Resource<TokenResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.login(LoginRequest(username, password))
            if (response.isSuccessful) {
                response.body()?.let { tokenResponse ->
                    tokenManager.saveToken(tokenResponse.accessToken)
                    emit(Resource.Success(tokenResponse))
                } ?: emit(Resource.Error("Empty response"))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Login failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }.flowOn(Dispatchers.IO)
    
    suspend fun register(
        username: String,
        email: String,
        password: String
    ): Flow<Resource<TokenResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.register(
                RegisterRequest(username, email, password)
            )
            if (response.isSuccessful) {
                response.body()?.let { tokenResponse ->
                    tokenManager.saveToken(tokenResponse.accessToken)
                    emit(Resource.Success(tokenResponse))
                } ?: emit(Resource.Error("Empty response"))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Registration failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }.flowOn(Dispatchers.IO)
    
    // Job Creation
    suspend fun createJob(
        chequeImageFile: File,
        enachFormFile: File,
        customerIdentifier: String? = null,
        customerName: String? = null,
        customerEmail: String? = null,
        customerMobile: String? = null
    ): Flow<Resource<JobStatusResponse>> = flow {
        emit(Resource.Loading())
        try {
            // Skip authentication for testing - use empty token
            val token = "Bearer test-token"
            
            // Prepare multipart data
            val chequeImagePart = MultipartBody.Part.createFormData(
                "cheque_image",
                chequeImageFile.name,
                chequeImageFile.asRequestBody("image/*".toMediaType())
            )
            
            val enachFormPart = MultipartBody.Part.createFormData(
                "enach_form",
                enachFormFile.name,
                enachFormFile.asRequestBody("application/pdf".toMediaType())
            )
            
            val response = apiService.createJob(
                token = token,
                chequeImage = chequeImagePart,
                enachForm = enachFormPart,
                customerIdentifier = customerIdentifier?.toRequestBody("text/plain".toMediaType()),
                customerName = customerName?.toRequestBody("text/plain".toMediaType()),
                customerEmail = customerEmail?.toRequestBody("text/plain".toMediaType()),
                customerMobile = customerMobile?.toRequestBody("text/plain".toMediaType())
            )
            
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(Resource.Success(it))
                } ?: emit(Resource.Error("Empty response"))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Job creation failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }.flowOn(Dispatchers.IO)
    
    // Get Job Status
    suspend fun getJobStatus(jobId: String): Flow<Resource<JobStatusResponse>> = flow {
        emit(Resource.Loading())
        try {
            // Skip authentication for testing - use empty token
            val token = "Bearer test-token"
            
            val response = apiService.getJobStatus(token, jobId)
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(Resource.Success(it))
                } ?: emit(Resource.Error("Empty response"))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Failed to get job status"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }.flowOn(Dispatchers.IO)
    
    // Download Form
    suspend fun downloadForm(jobId: String): Flow<Resource<ResponseBody>> = flow {
        emit(Resource.Loading())
        try {
            // Skip authentication for testing - use empty token
            val token = "Bearer test-token"
            
            val response = apiService.downloadForm(token, jobId)
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(Resource.Success(it))
                } ?: emit(Resource.Error("Empty response"))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Failed to download form"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }.flowOn(Dispatchers.IO)
    
    // Validate Job
    suspend fun validateJob(
        jobId: String,
        corrections: List<FieldCorrection>,
        reviewerNotes: String?,
        approve: Boolean
    ): Flow<Resource<ValidationResponse>> = flow {
        emit(Resource.Loading())
        try {
            // Skip authentication for testing - use empty token
            val token = "Bearer test-token"
            
            val request = ValidationRequest(
                corrections = corrections,
                reviewerNotes = reviewerNotes,
                approve = approve
            )
            
            val response = apiService.validateJob(token, jobId, request)
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(Resource.Success(it))
                } ?: emit(Resource.Error("Empty response"))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Validation failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }.flowOn(Dispatchers.IO)
    
    // List Jobs
    suspend fun listJobs(
        status: String? = null,
        customerIdentifier: String? = null,
        limit: Int = 20,
        offset: Int = 0
    ): Flow<Resource<List<JobStatusResponse>>> = flow {
        emit(Resource.Loading())
        try {
            // Skip authentication for testing - use empty token
            val token = "Bearer test-token"
            
            val response = apiService.listJobs(
                token = token,
                status = status,
                customerIdentifier = customerIdentifier,
                limit = limit,
                offset = offset
            )
            
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(Resource.Success(it))
                } ?: emit(Resource.Error("Empty response"))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Failed to list jobs"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }.flowOn(Dispatchers.IO)
    
    // Get Job Results - mobile-optimized endpoint
    suspend fun getJobResults(jobId: String): Flow<Resource<JobResultsResponse>> = flow {
        emit(Resource.Loading())
        try {
            val token = "Bearer test-token"
            
            val response = apiService.getJobResults(token, jobId)
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(Resource.Success(it))
                } ?: emit(Resource.Error("Empty response"))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Failed to get job results"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }.flowOn(Dispatchers.IO)
    
    // Get Live Job Status - for real-time updates
    suspend fun getJobLiveStatus(jobId: String): Flow<Resource<JobLiveStatusResponse>> = flow {
        emit(Resource.Loading())
        try {
            val token = "Bearer test-token"
            
            val response = apiService.getJobLiveStatus(token, jobId)
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(Resource.Success(it))
                } ?: emit(Resource.Error("Empty response"))
            } else {
                emit(Resource.Error(response.errorBody()?.string() ?: "Failed to get live status"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }.flowOn(Dispatchers.IO)

    // Health Check
    suspend fun healthCheck(): Flow<Resource<HealthCheckResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.healthCheck()
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(Resource.Success(it))
                } ?: emit(Resource.Error("Empty response"))
            } else {
                emit(Resource.Error("Service unavailable"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }.flowOn(Dispatchers.IO)
}
