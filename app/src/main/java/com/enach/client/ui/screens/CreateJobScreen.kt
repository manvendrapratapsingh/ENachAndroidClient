package com.enach.client.ui.screens

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.enach.client.ui.components.ChequeDataView
import com.enach.client.ui.components.ENachFormView
import com.enach.client.ui.components.ValidationReportView
import com.enach.client.ui.viewmodels.JobViewModel
import com.enach.client.utils.FileUtils
import com.enach.client.utils.Resource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CreateJobScreen(
    navController: NavController,
    viewModel: JobViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    var chequeImageUri by remember { mutableStateOf<Uri?>(null) }
    var enachFormImageUri by remember { mutableStateOf<Uri?>(null) }
    var customerIdentifier by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var customerEmail by remember { mutableStateOf("") }
    var customerMobile by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var tempNachCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showCameraPermissionDialog by remember { mutableStateOf(false) }
    
    val jobCreationState by viewModel.jobCreationState.collectAsState()
    
    // Camera permission
    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    )
    
    // Create temporary file for camera capture
    fun createImageFile(prefix: String = "CHEQUE"): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.cacheDir
        return File.createTempFile(
            "${prefix}_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }
    
    // Image picker launcher for cheque from gallery
    val chequeImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { 
            if (FileUtils.isFileSizeValid(context, it)) {
                chequeImageUri = it
                errorMessage = null
            } else {
                errorMessage = "Cheque image exceeds 10MB size limit"
            }
        }
    }

    // Image picker launcher for NACH form from gallery
    val nachImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (FileUtils.isFileSizeValid(context, it)) {
                enachFormImageUri = it
                errorMessage = null
            } else {
                errorMessage = "NACH form image exceeds 10MB size limit"
            }
        }
    }

    // Camera launcher for cheque
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempCameraUri?.let {
                chequeImageUri = it
                errorMessage = null
            }
        } else {
            Toast.makeText(context, "Failed to capture image", Toast.LENGTH_SHORT).show()
        }
    }

    // Camera launcher for NACH form
    val nachCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempNachCameraUri?.let {
                enachFormImageUri = it
                errorMessage = null
            }
        } else {
            Toast.makeText(context, "Failed to capture image", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Request camera permission launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, launch camera
            val photoFile = createImageFile("CHEQUE")
            tempCameraUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            tempCameraUri?.let { cameraLauncher.launch(it) }
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    
    var jobResponse by remember { mutableStateOf<com.enach.client.data.models.JobStatusResponse?>(null) }

    LaunchedEffect(jobCreationState) {
        when (val state = jobCreationState) {
            is Resource.Loading -> {
                isLoading = true
                errorMessage = null
            }
            is Resource.Success -> {
                isLoading = false
                jobResponse = state.data
                errorMessage = null
            }
            is Resource.Error -> {
                isLoading = false
                errorMessage = state.message
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Job") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Only show upload sections when no job response
            if (jobResponse == null) {
                // Instructions Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Info",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Column {
                            Text(
                                text = "Upload Requirements",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• Cheque image: Clear photo or scan\n• NACH form image: Photo or scan (optional)\n• Maximum file size per file: 10MB",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                // Cheque Image Upload
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cheque Image *",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (chequeImageUri != null) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Uploaded",
                                tint = Color.Green
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (chequeImageUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(chequeImageUri),
                                contentDescription = "Cheque Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { chequeImageUri = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .background(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                        RoundedCornerShape(4.dp)
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .border(
                                    2.dp,
                                    MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(8.dp)
                                )
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { chequeImageLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = "Upload",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap to select image",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { chequeImageLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = "Gallery")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gallery")
                        }
                        OutlinedButton(
                            onClick = { 
                                when {
                                    cameraPermissionState.status.isGranted -> {
                                        // Permission already granted, launch camera
                                    val photoFile = createImageFile("CHEQUE")
                                    tempCameraUri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        photoFile
                                    )
                                    tempCameraUri?.let { cameraLauncher.launch(it) }
                                    }
                                    cameraPermissionState.status.shouldShowRationale -> {
                                        // Show rationale dialog
                                        showCameraPermissionDialog = true
                                    }
                                    else -> {
                                        // Request permission
                                        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Camera")
                        }
                    }
                }
            }

            // NACH Form Image Upload (Optional)
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NACH Form Image (Optional)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (enachFormImageUri != null) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Uploaded",
                                tint = Color.Green
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (enachFormImageUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(enachFormImageUri),
                                contentDescription = "NACH Form Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { enachFormImageUri = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .background(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                        RoundedCornerShape(4.dp)
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .border(
                                    2.dp,
                                    MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(8.dp)
                                )
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { nachImageLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = "Upload",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap to select NACH form image",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { nachImageLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = "Gallery")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gallery")
                        }
                        OutlinedButton(
                            onClick = {
                                when {
                                    cameraPermissionState.status.isGranted -> {
                                        val photoFile = createImageFile("NACH")
                                        tempNachCameraUri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            photoFile
                                        )
                                        tempNachCameraUri?.let { nachCameraLauncher.launch(it) }
                                    }
                                    cameraPermissionState.status.shouldShowRationale -> {
                                        showCameraPermissionDialog = true
                                    }
                                    else -> {
                                        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Camera")
                        }
                    }
                }
            }
            
            // Customer Information (Optional)
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Customer Information (Optional)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = customerIdentifier,
                        onValueChange = { customerIdentifier = it },
                        label = { Text("Customer ID") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = customerEmail,
                        onValueChange = { customerEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = customerMobile,
                        onValueChange = { customerMobile = it },
                        label = { Text("Mobile Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
            } // Closing brace for the jobResponse == null conditional
            
            // Success Response
            jobResponse?.let { response ->
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
                                Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = Color.Green,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Job Created Successfully!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Job ID: ${response.jobId} • Status: ${response.status.uppercase()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // NEW SEPARATED DATA COMPONENTS
                ChequeDataView(chequeData = response.chequeData)
                
                ENachFormView(enachFormData = response.enachFormData)
                
                ValidationReportView(validationReport = response.validationReport)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action Buttons Card
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Next Steps",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { 
                                    jobResponse = null
                                    viewModel.clearCreateJobState()
                                    chequeImageUri = null
                                    enachFormImageUri = null
                                    customerIdentifier = ""
                                    customerName = ""
                                    customerEmail = ""
                                    customerMobile = ""
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "New Job")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Create New Job")
                            }
                            
                            val blockNext = response.validationReport?.let { (!it.isValid) || it.requiresManualReview } ?: false
                            Button(
                                onClick = { navController.navigate("job_details/${response.jobId}") },
                                modifier = Modifier.weight(1f),
                                enabled = !blockNext
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = "View Details")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (blockNext) "Fix Issues to Proceed" else "View Details")
                            }
                        }
                    }
                }
            }
            
            // Error Message
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            // Submit Button (only show when no job response)
            if (jobResponse == null) {
                Button(
                    onClick = {
                        scope.launch {
                            if (chequeImageUri != null) {
                                val chequeFile = FileUtils.getFileFromUri(context, chequeImageUri!!)
                                val nachFile = enachFormImageUri?.let { FileUtils.getFileFromUri(context, it) }
                                
                                if (chequeFile != null) {
                                    viewModel.createJob(
                                        chequeImageFile = chequeFile,
                                        enachFormFile = nachFile,
                                        customerIdentifier = customerIdentifier.ifEmpty { null },
                                        customerName = customerName.ifEmpty { null },
                                        customerEmail = customerEmail.ifEmpty { null },
                                        customerMobile = customerMobile.ifEmpty { null }
                                    )
                                } else {
                                    errorMessage = "Failed to process cheque image. Please try again."
                                }
                            } else {
                                errorMessage = "Please select a cheque image"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading && chequeImageUri != null
                ) {
                    if (isLoading) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Processing...",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = "Submit")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Submit for Processing",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        // Full-screen loading overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable(enabled = false) { /* Block all clicks */ },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Animated loading indicator
                        LoadingAnimation()
                        
                        Text(
                            text = "Processing your documents...",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        // Progress steps
                        ProcessingSteps()
                        
                        Text(
                            text = "This may take a few moments",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
    }
}

@Composable
fun LoadingAnimation() {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = Modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer rotating circle
        CircularProgressIndicator(
            modifier = Modifier
                .size(70.dp)
                .graphicsLayer(rotationZ = rotation),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            strokeWidth = 6.dp
        )
        
        // Inner pulsing circle
        CircularProgressIndicator(
            modifier = Modifier
                .size(50.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 6.dp,
            strokeCap = StrokeCap.Round
        )
        
        // Central icon
        Icon(
            imageVector = Icons.Default.Description,
            contentDescription = "Processing",
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ProcessingSteps() {
    val steps = listOf(
        "Uploading documents" to true,
        "Analyzing cheque" to true,
        "Extracting eNACH data" to false,
        "Validating information" to false
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        steps.forEachIndexed { index, (step, completed) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Step indicator
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            if (completed) MaterialTheme.colorScheme.primary else Color.Gray,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (completed) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    } else {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Step text
                Text(
                    text = step,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Progress line (except for last item)
            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
                        .height(24.dp)
                        .width(2.dp)
                        .background(Color.Gray.copy(alpha = 0.3f))
                )
            }
        }
    }
}