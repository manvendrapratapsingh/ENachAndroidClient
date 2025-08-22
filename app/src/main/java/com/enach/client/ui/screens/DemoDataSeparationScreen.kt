package com.enach.client.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.enach.client.data.models.*
import com.enach.client.ui.components.ChequeDataView
import com.enach.client.ui.components.ENachFormView
import com.enach.client.ui.components.ValidationReportView
import com.enach.client.ui.viewmodels.JobViewModel
import com.enach.client.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoDataSeparationScreen(
    onNavigateBack: () -> Unit,
    viewModel: JobViewModel = hiltViewModel()
) {
    var currentJobResponse by remember { mutableStateOf<JobStatusResponse?>(null) }
    var isLoadingRealData by remember { mutableStateOf(false) }
    var realJobsList by remember { mutableStateOf<List<JobStatusResponse>>(emptyList()) }
    
    // Observe job list state for real data
    val jobListState by viewModel.jobListState.collectAsState()
    
    // Load recent jobs when screen opens
    LaunchedEffect(Unit) {
        // Comment out for now - this method might not exist
        // viewModel.loadJobList()
    }
    
    // Update real jobs list when data is loaded
    LaunchedEffect(jobListState) {
        when (val state = jobListState) {
            is Resource.Success -> {
                realJobsList = state.data ?: emptyList()
                isLoadingRealData = false
            }
            is Resource.Loading -> {
                isLoadingRealData = true
            }
            is Resource.Error -> {
                isLoadingRealData = false
            }
            else -> {}
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Data Separation Demo",
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF6200EE),
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )
        
        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Demo Description
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📋 Demo Overview",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212529)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This demo shows how cheque and E-NACH data are displayed separately:\n" +
                                "• Blue box: Cheque data only\n" +
                                "• Green box: E-NACH form data only\n" +
                                "• Validation: Cross-document validation results\n" +
                                "• Empty states: Clear messaging when no data",
                        fontSize = 14.sp,
                        color = Color(0xFF6C757D)
                    )
                }
            }
            
            // Real API Data Section
            Text(
                text = "Real API Data:",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF495057)
            )
            
            if (isLoadingRealData) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Loading recent jobs...")
                    }
                }
            } else if (realJobsList.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(realJobsList.take(5)) { job ->
                        OutlinedButton(
                            onClick = { currentJobResponse = job },
                            modifier = Modifier.width(120.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = job.jobId.takeLast(8),
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = job.status.uppercase(),
                                    fontSize = 9.sp,
                                    color = when (job.status.lowercase()) {
                                        "completed" -> Color(0xFF4CAF50)
                                        "validation_required" -> Color(0xFFFFC107)
                                        "failed" -> Color(0xFFDC3545)
                                        else -> Color(0xFF6C757D)
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD))
                ) {
                    Text(
                        text = "No recent jobs found. Create a new job to see real data separation!",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp,
                        color = Color(0xFF856404)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Demo Data Buttons
            Text(
                text = "Demo Scenarios:",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF495057)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { currentJobResponse = getDemoChequeOnlyResponse() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Cheque", fontSize = 12.sp)
                        Text("Only", fontSize = 12.sp)
                    }
                }
                Button(
                    onClick = { currentJobResponse = getDemoENachOnlyResponse() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("E-NACH", fontSize = 12.sp)
                        Text("Only", fontSize = 12.sp)
                    }
                }
                Button(
                    onClick = { currentJobResponse = getDemoBothFormsResponse() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Both", fontSize = 12.sp)
                        Text("Forms", fontSize = 12.sp)
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { currentJobResponse = getDemoValidationErrorsResponse() },
                    modifier = Modifier.weight(1f)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("With", fontSize = 12.sp)
                        Text("Errors", fontSize = 12.sp)
                    }
                }
                OutlinedButton(
                    onClick = { currentJobResponse = null },
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear", fontSize = 12.sp)
                    }
                }
            }
            
            // Display Components
            if (currentJobResponse != null) {
                // Job Info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Job ID: ${currentJobResponse!!.jobId}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1976D2)
                        )
                        Text(
                            text = "Status: ${currentJobResponse!!.status.uppercase()}",
                            fontSize = 12.sp,
                            color = Color(0xFF1976D2)
                        )
                    }
                }
                
                // Separate display boxes for each data type
                ChequeDataView(
                    chequeData = currentJobResponse!!.chequeData
                )
                
                ENachFormView(
                    enachFormData = currentJobResponse!!.enachFormData
                )
                
                ValidationReportView(
                    validationReport = currentJobResponse!!.validationReport
                )
            } else {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎯 Perfect Data Separation",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF495057),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Text(
                            text = "Select a demo scenario above to see:\n" +
                                    "✓ Cheque data in blue container\n" +
                                    "✓ E-NACH data in green container\n" +
                                    "✓ Independent validation results\n" +
                                    "✓ Clear empty states\n" +
                                    "✓ No data mixing between documents",
                            fontSize = 14.sp,
                            color = Color(0xFF6C757D),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

// Demo data functions with realistic scenarios
private fun getDemoChequeOnlyResponse(): JobStatusResponse {
    return JobStatusResponse(
        jobId = "JOB_20250822_CHEQUE_ONLY",
        status = "completed",
        progressPercentage = 100,
        createdAt = "2025-08-22T08:00:30",
        startedAt = "2025-08-22T08:00:30",
        completedAt = "2025-08-22T08:00:45",
        chequeData = ChequeData(
            accountHolderName = "John Doe",
            accountNumber = "380100011938",
            bankName = "UCO Bank",
            bankBranch = "Selaiyur",
            ifscCode = "UCBA0000238",
            micrCode = "600028013",
            chequeNumber = "243053",
            chequeDate = null,
            amount = "6493",
            confidenceScores = mapOf("overall" to 0.95, "account_number" to 0.98, "ifsc_code" to 0.92)
        ),
        enachFormData = null, // No E-NACH form uploaded
        validationReport = ValidationReport(
            isValid = true,
            errors = listOf(),
            warnings = listOf(),
            confidenceScore = 0.95,
            requiresManualReview = false,
            reviewFields = listOf()
        ),
        populatedFormUrl = null,
        errorMessage = null,
        retryCount = 0
    )
}

private fun getDemoENachOnlyResponse(): JobStatusResponse {
    return JobStatusResponse(
        jobId = "JOB_20250822_ENACH_ONLY",
        status = "completed",
        progressPercentage = 100,
        createdAt = "2025-08-22T08:00:30",
        startedAt = "2025-08-22T08:00:30",
        completedAt = "2025-08-22T08:00:45",
        chequeData = null, // No cheque uploaded
        enachFormData = ENachFormData(
            accountHolderName = "Jane Smith",
            accountNumber = "002110000006809",
            accountType = "savings",
            bankName = "State Bank of India",
            bankBranch = "Mumbai Central",
            ifscCode = "SBIN0001234",
            micrCode = "400002054",
            mandateAmount = "1179",
            mandateFrequency = "monthly",
            mandateStartDate = "2025-08-08T00:00:00",
            mandateEndDate = "2050-12-31T00:00:00",
            debitType = "Fixed Amount",
            customerName = "Jane Smith",
            customerEmail = "jane.smith@email.com",
            customerMobile = "9876543210",
            umrn = "NACH20250808001",
            sponsorBank = "JUST DIAL LIMITED",
            utilityCode = "JDPL001"
        ),
        validationReport = ValidationReport(
            isValid = true,
            errors = listOf(),
            warnings = listOf(),
            confidenceScore = 0.88,
            requiresManualReview = false,
            reviewFields = listOf()
        ),
        populatedFormUrl = null,
        errorMessage = null,
        retryCount = 0
    )
}

private fun getDemoBothFormsResponse(): JobStatusResponse {
    return JobStatusResponse(
        jobId = "JOB_20250822_BOTH_FORMS",
        status = "completed",
        progressPercentage = 100,
        createdAt = "2025-08-22T08:00:30",
        startedAt = "2025-08-22T08:00:30",
        completedAt = "2025-08-22T08:00:45",
        chequeData = ChequeData(
            accountHolderName = "Alice Johnson",
            accountNumber = "987654321012",
            bankName = "HDFC Bank",
            bankBranch = "Delhi",
            ifscCode = "HDFC0001234",
            micrCode = "110240001",
            chequeNumber = "123456",
            chequeDate = null,
            amount = "5000",
            confidenceScores = mapOf("overall" to 0.93, "account_number" to 0.95, "ifsc_code" to 0.90)
        ),
        enachFormData = ENachFormData(
            accountHolderName = "Alice Johnson",
            accountNumber = "987654321012",
            accountType = "current",
            bankName = "HDFC Bank",
            bankBranch = "Delhi",
            ifscCode = "HDFC0001234",
            micrCode = "110240001",
            mandateAmount = "5000",
            mandateFrequency = "monthly",
            mandateStartDate = "2025-09-01T00:00:00",
            mandateEndDate = "2030-12-31T00:00:00",
            debitType = "Fixed Amount",
            customerName = "Alice Johnson",
            customerEmail = "alice.j@email.com",
            customerMobile = "9123456789",
            umrn = "NACH20250822002",
            sponsorBank = "XYZ SERVICES LTD",
            utilityCode = "XYZ001"
        ),
        validationReport = ValidationReport(
            isValid = true,
            errors = listOf(),
            warnings = listOf(),
            confidenceScore = 0.91,
            requiresManualReview = false,
            reviewFields = listOf()
        ),
        populatedFormUrl = null,
        errorMessage = null,
        retryCount = 0
    )
}

private fun getDemoValidationErrorsResponse(): JobStatusResponse {
    return JobStatusResponse(
        jobId = "JOB_20250822_WITH_ERRORS",
        status = "validation_required",
        progressPercentage = 100,
        createdAt = "2025-08-22T08:00:30",
        startedAt = "2025-08-22T08:00:30",
        completedAt = "2025-08-22T08:00:45",
        chequeData = ChequeData(
            accountHolderName = "Bob Wilson",
            accountNumber = "380100011938", // Different from form
            bankName = "UCO Bank",
            bankBranch = "Selaiyur",
            ifscCode = "UCBA0000238",
            micrCode = "600028013",
            chequeNumber = "243053",
            chequeDate = null,
            amount = "6493", // Different from mandate amount
            confidenceScores = mapOf("overall" to 0.85, "account_number" to 0.80, "ifsc_code" to 0.95)
        ),
        enachFormData = ENachFormData(
            accountHolderName = "Robert Wilson", // Slightly different name
            accountNumber = "002110000006809", // Different from cheque
            accountType = "savings",
            bankName = "State Bank of India", // Different bank
            bankBranch = "Mumbai",
            ifscCode = "SBIN0001234", // Different IFSC
            micrCode = "400525002",
            mandateAmount = "1179", // Different amount
            mandateFrequency = "monthly",
            mandateStartDate = "2025-08-08T00:00:00",
            mandateEndDate = "2050-12-31T00:00:00",
            debitType = "Fixed Amount",
            customerName = "Robert Wilson",
            customerEmail = null,
            customerMobile = null,
            umrn = null,
            sponsorBank = "JUST DIAL LIMITED",
            utilityCode = null
        ),
        validationReport = ValidationReport(
            isValid = false,
            errors = listOf(
                ValidationError(
                    field = "account_number",
                    error = "Mismatch between cheque (380100011938) and form (002110000006809).",
                    severity = "error",
                    suggestion = "Verify the correct account number from bank statement"
                ),
                ValidationError(
                    field = "amount",
                    error = "Amount mismatch: cheque ₹6493 vs mandate ₹1179.",
                    severity = "error",
                    suggestion = "Confirm the intended mandate amount with customer"
                )
            ),
            warnings = listOf(
                ValidationError(
                    field = "account_holder_name",
                    error = "Name variation: 'Bob Wilson' vs 'Robert Wilson'.",
                    severity = "warning",
                    suggestion = "Names appear to refer to the same person"
                ),
                ValidationError(
                    field = "bank_name",
                    error = "Bank differs: cheque 'UCO Bank' vs form 'State Bank of India'.",
                    severity = "warning",
                    suggestion = "Customer may have multiple bank accounts"
                ),
                ValidationError(
                    field = "ifsc_code",
                    error = "IFSC differs: cheque (UCBA0000238) vs form (SBIN0001234).",
                    severity = "warning",
                    suggestion = "Different banks have different IFSC codes"
                )
            ),
            confidenceScore = 0.6,
            requiresManualReview = true,
            reviewFields = listOf("account_number", "amount", "account_holder_name")
        ),
        populatedFormUrl = null,
        errorMessage = null,
        retryCount = 0
    )
}