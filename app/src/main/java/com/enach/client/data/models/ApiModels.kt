package com.enach.client.data.models

import com.google.gson.annotations.SerializedName
import java.util.Date

// Authentication Models
data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val role: String = "api_user"
)

data class TokenResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("token_type")
    val tokenType: String,
    @SerializedName("expires_in")
    val expiresIn: Int
)

// Job Models
data class JobResponse(
    @SerializedName("job_id")
    val jobId: String,
    val status: String,  // Changed to String to match API response
    @SerializedName("created_at")
    val createdAt: String,
    val message: String? = null  // Made nullable to handle null responses
)

data class JobStatusResponse(
    @SerializedName("job_id")
    val jobId: String,
    val status: String,  // Changed to String to match API response
    @SerializedName("progress_percentage")
    val progressPercentage: Int = 0,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("started_at")
    val startedAt: String?,
    @SerializedName("completed_at")
    val completedAt: String?,
    @SerializedName("cheque_data")
    val chequeData: ChequeData?,
    @SerializedName("enach_form_data")
    val enachFormData: ENachFormData?,
    @SerializedName("validation_report")
    val validationReport: ValidationReport?,
    @SerializedName("populated_form_url")
    val populatedFormUrl: String?,
    @SerializedName("error_message")
    val errorMessage: String?,
    @SerializedName("retry_count")
    val retryCount: Int = 0,
    @SerializedName("customer_name")
    val customerName: String? = null,
    @SerializedName("customer_identifier")
    val customerIdentifier: String? = null,
    @SerializedName("extracted_data")
    val extractedData: ExtractedData? = null
)

data class ExtractedData(
    @SerializedName("account_number")
    val accountNumber: String?,
    @SerializedName("account_holder_name")
    val accountHolderName: String?,
    val amount: String?
)

enum class JobStatus {
    @SerializedName("pending")
    PENDING,
    @SerializedName("processing")
    PROCESSING,
    @SerializedName("completed")
    COMPLETED,
    @SerializedName("failed")
    FAILED,
    @SerializedName("validation_required")
    VALIDATION_REQUIRED,
    @SerializedName("validated")
    VALIDATED
}

data class ChequeData(
    @SerializedName("account_holder_name")
    val accountHolderName: String?,
    @SerializedName("account_number")
    val accountNumber: String?,
    @SerializedName("bank_name")
    val bankName: String?,
    @SerializedName("bank_branch")
    val bankBranch: String?,
    @SerializedName("ifsc_code")
    val ifscCode: String?,
    @SerializedName("micr_code")
    val micrCode: String?,
    @SerializedName("cheque_number")
    val chequeNumber: String?,
    @SerializedName("cheque_date")
    val chequeDate: String?,
    val amount: String?,
    @SerializedName("confidence_scores")
    val confidenceScores: Map<String, Double>,
    // New fields for quality and signature validation
    @SerializedName("signature_present")
    val signaturePresent: Boolean? = null,
    @SerializedName("document_quality")
    val documentQuality: String? = null,
    @SerializedName("document_type")
    val documentType: String? = null,
    @SerializedName("issues")
    val issues: List<String>? = null
)

data class ENachFormData(
    @SerializedName("account_holder_name")
    val accountHolderName: String?,
    @SerializedName("account_number")
    val accountNumber: String?,
    @SerializedName("account_type")
    val accountType: String?,
    @SerializedName("bank_name")
    val bankName: String?,
    @SerializedName("bank_branch")
    val bankBranch: String?,
    @SerializedName("ifsc_code")
    val ifscCode: String?,
    @SerializedName("micr_code")
    val micrCode: String?,
    @SerializedName("mandate_amount")
    val mandateAmount: String?,
    @SerializedName("mandate_frequency")
    val mandateFrequency: String?,
    @SerializedName("mandate_start_date")
    val mandateStartDate: String?,
    @SerializedName("mandate_end_date")
    val mandateEndDate: String?,
    @SerializedName("debit_type")
    val debitType: String? = "FIXED",
    @SerializedName("customer_email")
    val customerEmail: String?,
    @SerializedName("customer_mobile")
    val customerMobile: String?,
    @SerializedName("customer_name")
    val customerName: String?,
    val umrn: String?,
    @SerializedName("sponsor_bank")
    val sponsorBank: String?,
    @SerializedName("utility_code")
    val utilityCode: String?,
    // New fields for quality and signature validation
    @SerializedName("signature_present")
    val signaturePresent: Boolean? = null,
    @SerializedName("document_quality")
    val documentQuality: String? = null,
    @SerializedName("document_type")
    val documentType: String? = null,
    @SerializedName("issues")
    val issues: List<String>? = null
)

data class ValidationReport(
    @SerializedName("is_valid")
    val isValid: Boolean,
    val errors: List<ValidationError>,
    val warnings: List<ValidationError>,
    @SerializedName("confidence_score")
    val confidenceScore: Double,
    @SerializedName("requires_manual_review")
    val requiresManualReview: Boolean,
    @SerializedName("review_fields")
    val reviewFields: List<String>
)

data class ValidationError(
    val field: String,
    val error: String,
    val severity: String = "error",
    val suggestion: String?
)

// Validation Request Models
data class ValidationRequest(
    val corrections: List<FieldCorrection>,
    @SerializedName("reviewer_notes")
    val reviewerNotes: String?,
    val approve: Boolean = false
)

data class FieldCorrection(
    @SerializedName("field_name")
    val fieldName: String,
    @SerializedName("original_value")
    val originalValue: String?,
    @SerializedName("corrected_value")
    val correctedValue: String,
    val reason: String?
)

data class ValidationResponse(
    @SerializedName("job_id")
    val jobId: String,
    val status: String,
    val message: String,
    @SerializedName("updated_fields")
    val updatedFields: List<String>,
    @SerializedName("validation_timestamp")
    val validationTimestamp: String
)

// Error Response
data class ErrorResponse(
    val error: ErrorDetail,
    @SerializedName("request_id")
    val requestId: String,
    val timestamp: String
)

data class ErrorDetail(
    val code: String,
    val message: String,
    val field: String?,
    val details: Map<String, Any>?
)

// Health Check
data class HealthCheckResponse(
    val status: String,
    val version: String,
    val timestamp: String,
    val services: Map<String, Boolean>
)

// Job Results Response - optimized for mobile display
data class JobResultsResponse(
    @SerializedName("job_id")
    val jobId: String,
    @SerializedName("processing_status")
    val processingStatus: ProcessingStatus,
    @SerializedName("extracted_data")
    val extractedData: ExtractedDataMobile?,
    val validation: ValidationMobile?,
    @SerializedName("form_generation")
    val formGeneration: FormGenerationStatus?
)

data class ProcessingStatus(
    val status: String,
    val progress: Int,
    val message: String,
    val completed: Boolean
)

data class ExtractedDataMobile(
    @SerializedName("cheque_info")
    val chequeInfo: Map<String, String>,
    @SerializedName("form_info")
    val formInfo: Map<String, String>,
    @SerializedName("confidence_overall")
    val confidenceOverall: Double
)

data class ValidationMobile(
    @SerializedName("is_valid")
    val isValid: Boolean,
    @SerializedName("needs_review")
    val needsReview: Boolean,
    val errors: List<ValidationError>,
    val warnings: List<ValidationError>,
    @SerializedName("completeness_score")
    val completenessScore: Double
)

data class FormGenerationStatus(
    @SerializedName("pdf_generated")
    val pdfGenerated: Boolean,
    @SerializedName("download_url")
    val downloadUrl: String?,
    @SerializedName("ready_for_signing")
    val readyForSigning: Boolean
)

// Live Status Response for real-time updates
data class JobLiveStatusResponse(
    @SerializedName("job_id")
    val jobId: String,
    val status: String,
    @SerializedName("progress_percentage")
    val progressPercentage: Int,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("started_at")
    val startedAt: String?,
    @SerializedName("completed_at")
    val completedAt: String?,
    @SerializedName("processing_stage")
    val processingStage: String,
    @SerializedName("estimated_completion")
    val estimatedCompletion: String,
    @SerializedName("ai_enhanced")
    val aiEnhanced: Boolean,
    @SerializedName("results_available")
    val resultsAvailable: Boolean,
    val results: JobResultsData?,
    @SerializedName("error_info")
    val errorInfo: ErrorInfo?
)

data class JobResultsData(
    @SerializedName("cheque_data")
    val chequeData: Map<String, Any>?,
    @SerializedName("enach_form_data") 
    val enachFormData: Map<String, Any>?,
    @SerializedName("validation_report")
    val validationReport: Map<String, Any>?,
    @SerializedName("confidence_scores")
    val confidenceScores: Map<String, Double>?,
    @SerializedName("populated_form_available")
    val populatedFormAvailable: Boolean
)

data class ErrorInfo(
    @SerializedName("error_message")
    val errorMessage: String?,
    @SerializedName("retry_count")
    val retryCount: Int,
    @SerializedName("can_retry")
    val canRetry: Boolean
)
