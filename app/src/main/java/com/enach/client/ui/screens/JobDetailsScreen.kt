package com.enach.client.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.enach.client.ui.viewmodels.JobViewModel
import com.enach.client.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailsScreen(
    navController: NavController,
    jobId: String,
    viewModel: JobViewModel = hiltViewModel()
) {
    val jobStatusState by viewModel.jobStatusState.collectAsState()
    val scrollState = rememberScrollState()
    
    // Load job details when screen starts
    LaunchedEffect(jobId) {
        viewModel.loadJobDetails(jobId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OCR Extraction Results") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val state = jobStatusState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is Resource.Success -> {
                    val jobData = state.data
                    if (jobData != null) {
                        // Job Info Header
                        JobInfoCard(jobData = jobData)
                        
                        // Cheque OCR Results
                        ChequeDataCard(chequeData = jobData.chequeData)
                        
                        // eNACH Form Results  
                        EnachFormDataCard(enachFormData = jobData.enachFormData)
                        
                        // Raw OCR Text Card
                        RawOCRTextCard(enachFormData = jobData.enachFormData)
                        
                        // Processing Summary (if available)
                        if (jobData.errorMessage != null) {
                            RawResultsCard(message = jobData.errorMessage)
                        }
                        
                        // Validation Info
                        ValidationCard(validationReport = jobData.validationReport)
                    }
                }
                
                is Resource.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Error loading job details",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = state.message ?: "Unknown error",
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                
                null -> {
                    Text("Loading job details...")
                }
            }
        }
    }
}

@Composable
fun JobInfoCard(jobData: com.enach.client.data.models.JobStatusResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Assignment,
                    contentDescription = "Job Info",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Job Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            InfoRow("Job ID", jobData.jobId)
            InfoRow("Status", jobData.status.uppercase())
            InfoRow("Progress", "${jobData.progressPercentage}%")
            InfoRow("Created At", jobData.createdAt)
            if (jobData.completedAt != null) {
                InfoRow("Completed At", jobData.completedAt)
            }
        }
    }
}

@Composable
fun ChequeDataCard(chequeData: com.enach.client.data.models.ChequeData?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AccountBalance,
                    contentDescription = "Cheque Data",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Cheque OCR Results",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (chequeData != null) {
                InfoRow("Bank Name", chequeData.bankName)
                InfoRow("Account Holder", chequeData.accountHolderName)
                InfoRow("Account Number", chequeData.accountNumber)
                InfoRow("IFSC Code", chequeData.ifscCode)
                InfoRow("MICR Code", chequeData.micrCode)
                InfoRow("Cheque Number", chequeData.chequeNumber)
                InfoRow("Amount", chequeData.amount?.let { "₹$it" })
                InfoRow("Cheque Date", chequeData.chequeDate)
                InfoRow("Bank Branch", chequeData.bankBranch)
                
                // Show confidence scores if available
                if (chequeData.confidenceScores.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "OCR Confidence Scores:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    chequeData.confidenceScores.forEach { (field, confidence) ->
                        InfoRow(field, "${(confidence * 100).toInt()}%")
                    }
                }
            } else {
                Text(
                    text = "No cheque data extracted",
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun EnachFormDataCard(enachFormData: com.enach.client.data.models.ENachFormData?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = "Form Data",
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "eNACH Form OCR Results",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (enachFormData != null) {
                InfoRow("Account Holder", enachFormData.accountHolderName)
                InfoRow("Account Number", enachFormData.accountNumber)
                InfoRow("Account Type", enachFormData.accountType)
                InfoRow("Bank Name", enachFormData.bankName)
                InfoRow("Bank Branch", enachFormData.bankBranch)
                InfoRow("IFSC Code", enachFormData.ifscCode)
                InfoRow("MICR Code", enachFormData.micrCode)
                InfoRow("Mandate Amount", enachFormData.mandateAmount?.let { "₹$it" })
                InfoRow("Frequency", enachFormData.mandateFrequency)
                InfoRow("Start Date", enachFormData.mandateStartDate)
                InfoRow("End Date", enachFormData.mandateEndDate)
                InfoRow("Debit Type", enachFormData.debitType)
                InfoRow("Customer Email", enachFormData.customerEmail)
                InfoRow("Customer Mobile", enachFormData.customerMobile)
                InfoRow("Customer Name", enachFormData.customerName)
                InfoRow("UMRN", enachFormData.umrn)
                InfoRow("Sponsor Bank", enachFormData.sponsorBank)
                InfoRow("Utility Code", enachFormData.utilityCode)
            } else {
                Text(
                    text = "No form data extracted",
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun RawResultsCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Analytics,
                    contentDescription = "Raw Results",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Processing Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ValidationCard(validationReport: com.enach.client.data.models.ValidationReport?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (validationReport?.isValid == true) 
                MaterialTheme.colorScheme.primaryContainer 
            else MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (validationReport?.isValid == true) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = "Validation",
                    tint = if (validationReport?.isValid == true) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "Validation Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (validationReport?.isValid == true) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else MaterialTheme.colorScheme.onErrorContainer
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (validationReport != null) {
                InfoRow("Is Valid", if (validationReport.isValid) "Yes" else "No")
                InfoRow("Confidence Score", "${(validationReport.confidenceScore * 100).toInt()}%")
                InfoRow("Manual Review Required", if (validationReport.requiresManualReview) "Yes" else "No")
                
                if (validationReport.errors.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Errors:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    validationReport.errors.forEach { error ->
                        Text("• ${error.field}: ${error.error}", style = MaterialTheme.typography.bodySmall)
                    }
                }
                
                if (validationReport.warnings.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Warnings:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    validationReport.warnings.forEach { warning ->
                        Text("• ${warning.field}: ${warning.error}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                Text("No validation information available")
            }
        }
    }
}

@Composable
fun RawOCRTextCard(enachFormData: com.enach.client.data.models.ENachFormData?) {
    var showRawText by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.TextFields,
                        contentDescription = "Raw OCR Text",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Raw OCR Text",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                TextButton(
                    onClick = { showRawText = !showRawText }
                ) {
                    Text(if (showRawText) "Hide" else "Show")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        if (showRawText) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (showRawText) "Hide" else "Show"
                    )
                }
            }
            
            if (showRawText) {
                Spacer(modifier = Modifier.height(12.dp))
                
                // Cheque Raw Text
                enachFormData?.let { data ->
                    if (data.sponsorBank != null && data.sponsorBank.contains("cheque_raw_text")) {
                        Text(
                            text = "Cheque OCR Raw Text:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = data.sponsorBank.substringAfter("cheque_raw_text:").take(500) + if (data.sponsorBank.length > 500) "..." else "",
                                modifier = Modifier
                                    .padding(12.dp)
                                    .horizontalScroll(rememberScrollState()),
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Show any field that contains "raw_text"
                    val fields = mapOf(
                        "Account Holder" to data.accountHolderName,
                        "Bank Name" to data.bankName,
                        "Customer Email" to data.customerEmail,
                        "Customer Name" to data.customerName,
                        "UMRN" to data.umrn
                    )
                    
                    fields.forEach { (fieldName, fieldValue) ->
                        if (fieldValue != null && fieldValue.length > 50) {
                            Text(
                                text = "$fieldName (Raw OCR):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(
                                    text = fieldValue.take(300) + if (fieldValue.length > 300) "..." else "",
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .horizontalScroll(rememberScrollState()),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                
                Text(
                    text = "💡 This shows the raw text extracted by OCR before processing into structured fields. Use this to check OCR accuracy.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value ?: "Not available",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            color = if (value == null) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
        )
    }
}
