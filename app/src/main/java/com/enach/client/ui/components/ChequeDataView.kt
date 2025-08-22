package com.enach.client.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enach.client.data.models.ChequeData

@Composable
fun ChequeDataView(
    chequeData: ChequeData?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F8FF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with Cheque Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color(0xFF2196F3),
                            RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "₹",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Cheque Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212529)
                    )
                    Text(
                        text = "Data extracted from cheque image",
                        fontSize = 12.sp,
                        color = Color(0xFF6C757D)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (chequeData == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No Cheque Uploaded",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6C757D)
                        )
                        Text(
                            text = "Upload a cheque image to see details here",
                            fontSize = 14.sp,
                            color = Color(0xFF6C757D)
                        )
                    }
                }
            } else {
                // Account Information Section
                DataSection(
                    title = "Account Details",
                    items = listOf(
                        "Account Holder" to chequeData.accountHolderName,
                        "Account Number" to chequeData.accountNumber,
                        "Bank Name" to chequeData.bankName,
                        "Branch" to chequeData.bankBranch
                    ),
                    backgroundColor = Color.White
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Cheque Information Section
                DataSection(
                    title = "Cheque Details",
                    items = listOf(
                        "Cheque Number" to chequeData.chequeNumber,
                        "Amount" to chequeData.amount?.let { "₹$it" },
                        "IFSC Code" to chequeData.ifscCode,
                        "MICR Code" to chequeData.micrCode
                    ),
                    backgroundColor = Color.White
                )
                
                // Confidence Score if available
                if (chequeData.confidenceScores.isNotEmpty()) {
                    val overallConfidence = chequeData.confidenceScores["overall"] ?: 
                                          chequeData.confidenceScores.values.average()
                    Spacer(modifier = Modifier.height(12.dp))
                    ConfidenceIndicator(confidence = overallConfidence)
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
        items.forEachIndexed { index, (label, value) ->
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
                    text = value ?: "Not detected",
                    fontSize = 14.sp,
                    fontWeight = if (value != null) FontWeight.Medium else FontWeight.Normal,
                    color = if (value != null) Color(0xFF212529) else Color(0xFF6C757D),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ConfidenceIndicator(confidence: Double) {
    val confidencePercent = (confidence * 100).toInt()
    val color = when {
        confidence >= 0.8 -> Color(0xFF28A745)
        confidence >= 0.6 -> Color(0xFFFFC107)
        else -> Color(0xFFDC3545)
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = "Extraction Confidence:",
            fontSize = 12.sp,
            color = Color(0xFF6C757D)
        )
        Spacer(modifier = Modifier.width(8.dp))
        LinearProgressIndicator(
            progress = confidence.toFloat(),
            modifier = Modifier
                .height(6.dp)
                .weight(1f),
            color = color,
            trackColor = Color(0xFFE9ECEF)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$confidencePercent%",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}