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
import com.enach.client.data.models.ValidationReport
import com.enach.client.data.models.ValidationError

@Composable
fun ValidationReportView(
    validationReport: ValidationReport?,
    modifier: Modifier = Modifier
) {
    val (containerColor, iconColor, statusText) = when {
        validationReport?.isValid == true -> Triple(
            Color(0xFFE8F5E8), 
            Color(0xFF28A745), 
            "Validation Passed"
        )
        validationReport?.requiresManualReview == true -> Triple(
            Color(0xFFFFF3CD), 
            Color(0xFFFFC107), 
            "Review Required"
        )
        validationReport != null -> Triple(
            Color(0xFFF8D7DA), 
            Color(0xFFDC3545), 
            "Validation Failed"
        )
        else -> Triple(
            Color(0xFFF8F9FA), 
            Color(0xFF6C757D), 
            "No Validation"
        )
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with Status Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val icon = when {
                    validationReport?.isValid == true -> "✓"
                    validationReport?.requiresManualReview == true -> "⚠"
                    validationReport != null -> "✗"
                    else -> "?"
                }
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(iconColor, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = icon,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = statusText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212529)
                    )
                    Text(
                        text = "Cross-validation between documents",
                        fontSize = 12.sp,
                        color = Color(0xFF6C757D)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (validationReport == null) {
                Text(
                    text = "No validation report available",
                    fontSize = 14.sp,
                    color = Color(0xFF6C757D)
                )
            } else {
                // Status Summary
                StatusSummary(validationReport)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Errors Section
                if (validationReport.errors.isNotEmpty()) {
                    ValidationSection(
                        title = "Errors (${validationReport.errors.size})",
                        items = validationReport.errors,
                        color = Color(0xFFDC3545),
                        description = "Critical issues that must be resolved"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Warnings Section
                if (validationReport.warnings.isNotEmpty()) {
                    ValidationSection(
                        title = "Warnings (${validationReport.warnings.size})",
                        items = validationReport.warnings,
                        color = Color(0xFFFFC107),
                        description = "Items that may need attention"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Review Fields
                if (validationReport.reviewFields.isNotEmpty()) {
                    ReviewFieldsSection(validationReport.reviewFields)
                }
                
                // Success message when everything is valid
                if (validationReport.isValid && validationReport.errors.isEmpty() && validationReport.warnings.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color.White.copy(alpha = 0.8f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✅ All validations passed successfully!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF28A745)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusSummary(validationReport: ValidationReport) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White.copy(alpha = 0.8f),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Overall Status",
                fontSize = 12.sp,
                color = Color(0xFF6C757D)
            )
            Text(
                text = when {
                    validationReport.isValid -> "✅ Valid"
                    validationReport.requiresManualReview -> "🔍 Needs Review"
                    else -> "❌ Invalid"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF212529)
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Confidence",
                fontSize = 12.sp,
                color = Color(0xFF6C757D)
            )
            val confidencePercent = (validationReport.confidenceScore * 100).toInt()
            Text(
                text = "$confidencePercent%",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = when {
                    validationReport.confidenceScore >= 0.8 -> Color(0xFF28A745)
                    validationReport.confidenceScore >= 0.6 -> Color(0xFFFFC107)
                    else -> Color(0xFFDC3545)
                }
            )
        }
    }
}

@Composable
private fun ValidationSection(
    title: String,
    items: List<ValidationError>,
    color: Color,
    description: String
) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier = Modifier.padding(bottom = 4.dp)
    )
    Text(
        text = description,
        fontSize = 12.sp,
        color = Color(0xFF6C757D),
        modifier = Modifier.padding(bottom = 8.dp)
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White.copy(alpha = 0.8f),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        items.forEachIndexed { index, error ->
            if (index > 0) {
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color(0xFFE9ECEF)
                )
            }
            
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(color, RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (!error.field.isNullOrBlank()) {
                        Text(
                            text = error.field,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = color
                        )
                    }
                }
                if (!error.field.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = error.error,
                    fontSize = 14.sp,
                    color = Color(0xFF212529),
                    modifier = Modifier.padding(start = 16.dp)
                )
                error.suggestion?.let { suggestion ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "💡 $suggestion",
                        fontSize = 12.sp,
                        color = Color(0xFF6C757D),
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewFieldsSection(reviewFields: List<String>) {
    Text(
        text = "Fields Requiring Review",
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF495057),
        modifier = Modifier.padding(bottom = 8.dp)
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White.copy(alpha = 0.8f),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        reviewFields.forEachIndexed { index, field ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(8.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            Color(0xFFFFC107),
                            RoundedCornerShape(3.dp)
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = field.replace("_", " ").replaceFirstChar { 
                        if (it.isLowerCase()) it.titlecase() else it.toString() 
                    },
                    fontSize = 14.sp,
                    color = Color(0xFF212529)
                )
            }
        }
    }
}