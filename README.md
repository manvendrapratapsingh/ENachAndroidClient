# ENachAndroidClient

Nach Android app

# e-NACH Android Client Application

A modern Android application built with Jetpack Compose and Kotlin that interfaces with the e-NACH processing backend API. The app allows users to upload cheque and e-NACH form images, track processing status, view populated forms, and submit manual corrections.

## 🏗️ Architecture

The application follows **MVVM (Model-View-ViewModel)** architecture with the following layers:

### Data Layer
- **API Service**: Retrofit interfaces for network calls
- **Repository**: Single source of truth for data operations
- **Models**: Data classes for API responses
- **Token Manager**: Secure token storage using EncryptedSharedPreferences

### Domain Layer
- **Use Cases**: Business logic encapsulation
- **Resource Wrapper**: Unified API response handling

### Presentation Layer
- **ViewModels**: State management with Kotlin Flow
- **Screens**: Jetpack Compose UI components
- **Navigation**: Type-safe navigation with Navigation Compose

## 📱 Features

### Authentication
- **Login/Register**: JWT-based authentication
- **Secure Token Storage**: Using Android's EncryptedSharedPreferences
- **Auto-login**: Remember user session
- **Logout**: Clear credentials securely

### Job Management
- **Create Jobs**: Upload cheque and e-NACH form images
- **Track Status**: Real-time job status updates
- **View Results**: Display extracted data
- **Download Forms**: View populated PDF forms
- **Manual Validation**: Edit and correct extracted fields

### Image Handling
- **Camera Integration**: Capture images directly
- **Gallery Selection**: Pick images from device
- **Image Preview**: View selected images before upload
- **File Validation**: Check size and format
- **Compression**: Optimize images before upload

### Security
- **HTTPS Only**: All network calls use HTTPS
- **Certificate Pinning**: Optional SSL pinning
- **Encrypted Storage**: Sensitive data encrypted at rest
- **No Logging**: Personal/financial data not logged
- **NPCI Compliance**: Follows guidelines for e-NACH data

## 🛠️ Tech Stack

- **Kotlin**: 1.9.20
- **Jetpack Compose**: Latest BOM 2023.10.01
- **Coroutines & Flow**: Async operations
- **Hilt**: Dependency injection
- **Retrofit**: Network operations
- **OkHttp**: HTTP client with interceptors
- **WorkManager**: Background job scheduling
- **DataStore**: Preferences storage
- **CameraX**: Camera operations
- **Coil**: Image loading
- **Security Crypto**: Encrypted storage

## 📂 Project Structure

```
app/
├── src/main/java/com/enach/client/
│   ├── data/
│   │   ├── api/
│   │   │   └── ENachApiService.kt       # Retrofit API interface
│   │   ├── models/
│   │   │   └── ApiModels.kt            # Data models
│   │   └── repository/
│   │       ├── ENachRepository.kt      # Repository pattern
│   │       └── AuthTokenManager.kt     # Token management
│   ├── di/
│   │   └── NetworkModule.kt            # Hilt DI modules
│   ├── ui/
│   │   ├── screens/
│   │   │   ├── auth/
│   │   │   │   ├── LoginScreen.kt
│   │   │   │   └── RegisterScreen.kt
│   │   │   ├── home/
│   │   │   │   
│   │   │   ├── job/
│   │   │   │   ├── JobCreationScreen.kt
│   │   │   │   ├── JobDetailsScreen.kt
│   │   │   │   ├── JobListScreen.kt
│   │   │   │   └── ValidationScreen.kt
│   │   │   └── splash/
│   │   │       └── SplashScreen.kt
│   │   ├── components/               # Reusable UI components
│   │   ├── navigation/
│   │   │   └── ENachNavigation.kt
│   │   └── theme/
│   │       └── Theme.kt
│   ├── utils/
│   │   ├── Resource.kt              # API response wrapper
│   │   ├── FileUtils.kt            # File operations
│   │   └── ValidationUtils.kt      # Input validation
│   ├── viewmodels/
│   │   ├── AuthViewModel.kt
│   │   ├── JobViewModel.kt
│   │   └── ValidationViewModel.kt
│   ├── workers/
│   │   └── JobStatusWorker.kt      # Background job polling
│   ├── ENachApplication.kt         # Application class
│   └── MainActivity.kt              # Main activity
└── AndroidManifest.xml
```

## 🚀 Setup Instructions

### 1. Backend Configuration

Update the base URL in `app/build.gradle.kts`:

```kotlin
buildTypes {
    debug {
        buildConfigField("String", "BASE_URL", "\"http://172.20.10.8:8000\"") // For emulator
    }
    release {
        buildConfigField("String", "BASE_URL", "\"https://your-api-url.com\"")
    }
}
```

### 2. Build and Run

```bash
# Clone the repository
git clone <repository-url>

# Open in Android Studio
# Sync Gradle files
# Run on emulator or device
```

### 3. Required Permissions

The app requires the following permissions (already configured in AndroidManifest.xml):
- INTERNET - Network access
- CAMERA - Capture cheque/form images
- READ_MEDIA_IMAGES - Select images from gallery

## 💻 Key Implementation Examples

### 1. API Service Interface

```kotlin
interface ENachApiService {
    @Multipart
    @POST("/api/v1/jobs")
    suspend fun createJob(
        @Header("Authorization") token: String,
        @Part chequeImage: MultipartBody.Part,
        @Part enachForm: MultipartBody.Part,
        @Part("customer_identifier") customerIdentifier: RequestBody?
    ): Response<JobResponse>
}
```

### 2. Repository Pattern

```kotlin
class ENachRepository @Inject constructor(
    private val apiService: ENachApiService,
    private val tokenManager: AuthTokenManager
) {
    suspend fun createJob(...): Flow<Resource<JobResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.createJob(...)
            if (response.isSuccessful) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message))
        }
    }.flowOn(Dispatchers.IO)
}
```

### 3. ViewModel with StateFlow

```kotlin
@HiltViewModel
class JobViewModel @Inject constructor(
    private val repository: ENachRepository
) : ViewModel() {
    private val _jobState = MutableStateFlow<Resource<JobResponse>?>(null)
    val jobState: StateFlow<Resource<JobResponse>?> = _jobState
    
    fun createJob(chequeImage: File, enachForm: File) {
        viewModelScope.launch {
            repository.createJob(chequeImage, enachForm).collect { result ->
                _jobState.value = result
            }
        }
    }
}
```

### 4. Compose UI Screen

```kotlin
@Composable
fun JobCreationScreen(
    viewModel: JobViewModel = hiltViewModel(),
    onJobCreated: (String) -> Unit
) {
    val jobState by viewModel.jobState.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // Image selection UI
        ImageSelector(
            onImageSelected = { image ->
                viewModel.setChequeImage(image)
            }
        )
        
        // Submit button
        Button(
            onClick = { viewModel.createJob() },
            enabled = viewModel.canSubmit()
        ) {
            Text("Submit Job")
        }
        
        // Handle states
        when (val state = jobState) {
            is Resource.Loading -> CircularProgressIndicator()
            is Resource.Success -> {
                LaunchedEffect(state.data) {
                    onJobCreated(state.data.jobId)
                }
            }
            is Resource.Error -> {
                Text(state.message ?: "Error occurred")
            }
        }
    }
}
```

### 5. Background Job Polling with WorkManager

```kotlin
class JobStatusWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        val jobId = inputData.getString("job_id") ?: return Result.failure()
        
        return try {
            val status = repository.getJobStatus(jobId)
            if (status.isCompleted) {
                showNotification("Job completed!")
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
```

## 🔒 Security Best Practices

1. **Token Storage**: Uses EncryptedSharedPreferences for secure token storage
2. **No Sensitive Logging**: Financial data is never logged
3. **HTTPS Enforcement**: All API calls use HTTPS
4. **Input Validation**: Client-side validation before API calls
5. **Secure File Handling**: Temporary files deleted after processing
6. **Scoped Storage**: Uses Android's scoped storage for file operations

## 📊 Performance Optimizations

1. **Image Compression**: Reduces image size before upload
2. **Lazy Loading**: Lists use LazyColumn for efficient rendering
3. **Caching**: Coil handles image caching automatically
4. **Coroutines**: All I/O operations run on background threads
5. **Flow**: Reactive data streams for real-time updates

## 🧪 Testing

```kotlin
// Unit Test Example
@Test
fun `login with valid credentials returns success`() = runTest {
    val repository = ENachRepository(mockApiService, mockTokenManager)
    
    repository.login("user", "password").test {
        assertEquals(Resource.Loading(), awaitItem())
        assertEquals(Resource.Success(tokenResponse), awaitItem())
        awaitComplete()
    }
}
```

## 📱 Screenshots Flow

1. **Splash Screen** → Check authentication
2. **Login/Register** → User authentication
3. **Home Screen** → Main dashboard
4. **Job Creation** → Upload images & create job
5. **Job Status** → Track processing progress
6. **Results View** → See extracted data
7. **Validation** → Manual corrections if needed
8. **PDF Viewer** → View populated form

## 🚢 Production Checklist

- [ ] Update BASE_URL to production API
- [ ] Enable ProGuard/R8 minification
- [ ] Configure certificate pinning
- [ ] Remove debug logging
- [ ] Test on various Android versions
- [ ] Add crash reporting (Firebase Crashlytics)
- [ ] Implement analytics
- [ ] Add user feedback mechanism
- [ ] Test offline scenarios
- [ ] Verify NPCI compliance

## 📄 License

This project is proprietary and confidential.

## 🤝 Support

For issues and questions:
- Create an issue in the repository
- Contact the development team
- Check API documentation

---

**Note**: This is a production-ready Android client for the e-NACH processing system. Ensure all security measures and compliance requirements are met before deployment.
