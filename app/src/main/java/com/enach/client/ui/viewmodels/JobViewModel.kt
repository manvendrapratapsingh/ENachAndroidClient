package com.enach.client.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.enach.client.data.models.*
import com.enach.client.data.repository.ENachRepository
import com.enach.client.utils.Resource
import com.enach.client.utils.FileUtils
import com.enach.client.workers.JobStatusWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class JobViewModel @Inject constructor(
    private val repository: ENachRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    // Job creation state - renamed for consistency with CreateJobScreen
    private val _jobCreationState = MutableStateFlow<Resource<JobStatusResponse>?>(null)
    val jobCreationState: StateFlow<Resource<JobStatusResponse>?> = _jobCreationState.asStateFlow()
    
    private val _jobStatusState = MutableStateFlow<Resource<JobStatusResponse>?>(null)
    val jobStatusState: StateFlow<Resource<JobStatusResponse>?> = _jobStatusState.asStateFlow()
    
    private val _jobListState = MutableStateFlow<Resource<List<JobStatusResponse>>?>(null)
    val jobListState: StateFlow<Resource<List<JobStatusResponse>>?> = _jobListState.asStateFlow()
    
    private val _downloadFormState = MutableStateFlow<Resource<ResponseBody>?>(null)
    val downloadFormState: StateFlow<Resource<ResponseBody>?> = _downloadFormState.asStateFlow()
    
    private val _chequeImageUri = MutableStateFlow<Uri?>(null)
    val chequeImageUri: StateFlow<Uri?> = _chequeImageUri.asStateFlow()
    
    private val _enachFormUri = MutableStateFlow<Uri?>(null)
    val enachFormUri: StateFlow<Uri?> = _enachFormUri.asStateFlow()
    
    private val _customerIdentifier = MutableStateFlow("")
    val customerIdentifier: StateFlow<String> = _customerIdentifier.asStateFlow()
    
    private val _customerName = MutableStateFlow("")
    val customerName: StateFlow<String> = _customerName.asStateFlow()
    
    private val _customerEmail = MutableStateFlow("")
    val customerEmail: StateFlow<String> = _customerEmail.asStateFlow()
    
    private val _customerMobile = MutableStateFlow("")
    val customerMobile: StateFlow<String> = _customerMobile.asStateFlow()
    
    fun setChequeImage(uri: Uri) {
        _chequeImageUri.value = uri
    }
    
    fun setEnachForm(uri: Uri) {
        _enachFormUri.value = uri
    }
    
    fun updateCustomerIdentifier(value: String) {
        _customerIdentifier.value = value
    }
    
    fun updateCustomerName(value: String) {
        _customerName.value = value
    }
    
    fun updateCustomerEmail(value: String) {
        _customerEmail.value = value
    }
    
    fun updateCustomerMobile(value: String) {
        _customerMobile.value = value
    }
    
    fun canSubmit(): Boolean {
        return _chequeImageUri.value != null && _enachFormUri.value != null
    }
    
    // Overloaded createJob method for CreateJobScreen
    fun createJob(
        chequeImageFile: File,
        enachFormFile: File,
        customerIdentifier: String? = null,
        customerName: String? = null,
        customerEmail: String? = null,
        customerMobile: String? = null
    ) {
        viewModelScope.launch {
            repository.createJob(
                chequeImageFile = chequeImageFile,
                enachFormFile = enachFormFile,
                customerIdentifier = customerIdentifier,
                customerName = customerName,
                customerEmail = customerEmail,
                customerMobile = customerMobile
            ).collect { result ->
                _jobCreationState.value = result
                
                // Start background worker for status polling if job created successfully
                if (result is Resource.Success) {
                    result.data?.let { jobStatusResponse ->
                        startJobStatusWorker(jobStatusResponse.jobId)
                    }
                }
            }
        }
    }
    
    fun createJob() {
        val chequeUri = _chequeImageUri.value ?: return
        val enachUri = _enachFormUri.value ?: return
        
        viewModelScope.launch {
            try {
                // Convert URIs to Files
                val chequeFile = FileUtils.getFileFromUri(context, chequeUri)
                val enachFile = FileUtils.getFileFromUri(context, enachUri)
                
                if (chequeFile != null && enachFile != null) {
                    createJob(
                        chequeImageFile = chequeFile,
                        enachFormFile = enachFile,
                        customerIdentifier = _customerIdentifier.value.ifEmpty { null },
                        customerName = _customerName.value.ifEmpty { null },
                        customerEmail = _customerEmail.value.ifEmpty { null },
                        customerMobile = _customerMobile.value.ifEmpty { null }
                    )
                } else {
                    _jobCreationState.value = Resource.Error("Failed to process selected files")
                }
            } catch (e: Exception) {
                _jobCreationState.value = Resource.Error(e.message ?: "An error occurred")
            }
        }
    }
    
    fun getJobStatus(jobId: String) {
        viewModelScope.launch {
            repository.getJobStatus(jobId).collect { result ->
                _jobStatusState.value = result
            }
        }
    }
    
    fun getJobList(
        status: String? = null,
        customerIdentifier: String? = null,
        limit: Int = 20,
        offset: Int = 0
    ) {
        viewModelScope.launch {
            repository.listJobs(
                status = status,
                customerIdentifier = customerIdentifier,
                limit = limit,
                offset = offset
            ).collect { result ->
                _jobListState.value = result
            }
        }
    }
    
    fun downloadForm(jobId: String) {
        viewModelScope.launch {
            repository.downloadForm(jobId).collect { result ->
                _downloadFormState.value = result
            }
        }
    }
    
    private fun startJobStatusWorker(jobId: String) {
        val workRequest = PeriodicWorkRequestBuilder<JobStatusWorker>(
            15, TimeUnit.MINUTES
        )
            .setInputData(
                workDataOf("job_id" to jobId)
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag("job_status_$jobId")
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "job_status_$jobId",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
    }
    
    fun clearCreateJobState() {
        _jobCreationState.value = null
        _chequeImageUri.value = null
        _enachFormUri.value = null
        _customerIdentifier.value = ""
        _customerName.value = ""
        _customerEmail.value = ""
        _customerMobile.value = ""
    }
    
    fun clearJobStatusState() {
        _jobStatusState.value = null
    }
    
    // Load job details when navigating to details screen
    fun loadJobDetails(jobId: String) {
        getJobStatus(jobId)
    }
}
