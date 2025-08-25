package com.enach.client.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enach.client.data.models.ENachFormData
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ENachFormView(
    enachFormData: ENachFormData?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FFF0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with E-NACH Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color(0xFF4CAF50),
                            RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "E",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "E-NACH Form Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212529)
                    )
                    Text(
                        text = "Data extracted from mandate form",
                        fontSize = 12.sp,
                        color = Color(0xFF6C757D)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (enachFormData == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No E-NACH Form Uploaded",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6C757D)
                        )
                        Text(
                            text = "Upload an E-NACH form to see details here",
                            fontSize = 14.sp,
                            color = Color(0xFF6C757D)
                        )
                    }
                }
            } else {
                // Customer Information Section
                val customerItems = listOfNotNull(
                    enachFormData.customerName?.let { "Customer Name" to it },
                    enachFormData.customerEmail?.let { "Email" to it },
                    enachFormData.customerMobile?.let { "Mobile" to it }
                )
                
                if (customerItems.isNotEmpty()) {
                    DataSection(
                        title = "Customer Information",
                        items = customerItems,
                        backgroundColor = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Bank Account Information Section
                DataSection(
                    title = "Bank Account Details",
                    items = listOf(
                        "Account Holder" to enachFormData.accountHolderName,
                        "Account Number" to enachFormData.accountNumber,
                        "Account Type" to enachFormData.accountType?.replaceFirstChar { 
                            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() 
                        },
                        "Bank Name" to enachFormData.bankName,
                        "Branch" to enachFormData.bankBranch,
                        "IFSC Code" to enachFormData.ifscCode,
                        "MICR Code" to enachFormData.micrCode
                    ),
                    backgroundColor = Color.White
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Mandate Information Section
                DataSection(
                    title = "Mandate Details",
                    items = listOf(
                        "Amount" to enachFormData.mandateAmount?.let { "₹$it" },
                        "Frequency" to enachFormData.mandateFrequency?.replaceFirstChar { 
                            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() 
                        },
                        "Debit Type" to enachFormData.debitType,
                        "Start Date" to formatDate(enachFormData.mandateStartDate),
                        "End Date" to formatDate(enachFormData.mandateEndDate)
                    ),
                    backgroundColor = Color.White
                )
                
                // Additional Information Section
                val additionalItems = listOfNotNull(
                    enachFormData.umrn?.let { "UMRN" to it },
                    enachFormData.sponsorBank?.let { "Sponsor Bank" to it },
                    enachFormData.utilityCode?.let { "Utility Code" to it }
                )
                
                if (additionalItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    DataSection(
                        title = "Additional Information",
                        items = additionalItems,
                        backgroundColor = Color.White
                    )
                }
                
                // Document Quality and Signature Information
                Spacer(modifier = Modifier.height(12.dp))
                DocumentQualitySection(
                    signaturePresent = enachFormData.signaturePresent,
                    documentQuality = enachFormData.documentQuality,
                    documentType = enachFormData.documentType
                )
                
                // Issues if any
                if (!enachFormData.issues.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    IssuesSection(issues = enachFormData.issues)
                }
            }
        }
    }
}

@Composable
private fun DataSection(
    title: String,
    items: List<Pair<String, String?>>,
    backgroundColor: Color = Color.White
) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF495057),
        modifier = Modifier.padding(bottom = 8.dp)
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                backgroundColor,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        val itemsWithValues = items.filter { it.second != null }
        
        if (itemsWithValues.isEmpty()) {
            Text(
                text = "No information detected in this section",
                fontSize = 14.sp,
                color = Color(0xFF6C757D),
                modifier = Modifier.padding(8.dp)
            )
        } else {
            itemsWithValues.forEachIndexed { index, (label, value) ->
                if (index > 0) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color(0xFFE9ECEF)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$label:",
                        fontSize = 14.sp,
                        color = Color(0xFF6C757D),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = value!!,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF212529),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private fun formatDate(dateString: String?): String? {
    if (dateString == null) return null
    
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) }
    } catch (e: Exception) {
        try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) }
        } catch (e: Exception) {
            dateString // Return original if parsing fails
        }
    }
}

@Composable
private fun DocumentQualitySection(
    signaturePresent: Boolean?,
    documentQuality: String?,
    documentType: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = "Document Information",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF495057),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Signature Information
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Signature Present:",
                fontSize = 14.sp,
                color = Color(0xFF6C757D),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = when (signaturePresent) {
                    true -> "Yes"
                    false -> "No"
                    null -> "Not detected"
                },
                fontSize = 14.sp,
                fontWeight = when (signaturePresent) {
                    true -> FontWeight.Medium
                    false -> FontWeight.Medium
                    null -> FontWeight.Normal
                },
                color = when (signaturePresent) {
                    true -> Color(0xFF28A745)  // Green
                    false -> Color(0xFFDC3545)  // Red
                    null -> Color(0xFF6C757D)   // Gray
                },
                modifier = Modifier.weight(1f)
            )
        }
        
        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = Color(0xFFE9ECEF)
        )
        
        // Document Quality
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Document Quality:",
                fontSize = 14.sp,
                color = Color(0xFF6C757D),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = documentQuality ?: "Not detected",
                fontSize = 14.sp,
                fontWeight = if (documentQuality != null) FontWeight.Medium else FontWeight.Normal,
                color = when (documentQuality) {
                    "good" -> Color(0xFF28A745)  // Green
                    "poor", "bad" -> Color(0xFFDC3545)  // Red
                    null -> Color(0xFF6C757D)   // Gray
                    else -> Color(0xFF212529)   // Default
                },
                modifier = Modifier.weight(1f)
            )
        }
        
        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = Color(0xFFE9ECEF)
        )
        
        // Document Type
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Document Type:",
                fontSize = 14.sp,
                color = Color(0xFF6C757D),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = documentType?.replaceFirstChar { 
                    if (it.isLowerCase()) it.titlecase() else it.toString() 
                } ?: "Not detected",
                fontSize = 14.sp,
                fontWeight = if (documentType != null) FontWeight.Medium else FontWeight.Normal,
                color = if (documentType != null) Color(0xFF212529) else Color(0xFF6C757D),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun IssuesSection(issues: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFFFFF3CD),  // Light yellow background for warnings
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = Color(0xFFFFC107),  // Yellow icon
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Document Issues (${issues.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF495057)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        issues.forEachIndexed { index, issue ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(4.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(Color(0xFFFFC107), RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = issue,
                    fontSize = 12.sp,
                    color = Color(0xFF212529)
                )
            }
        }
    }
}