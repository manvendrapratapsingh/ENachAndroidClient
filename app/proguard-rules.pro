
# Tink
-keepclassmembers class com.google.crypto.tink.** { *; }
-keep public class com.google.crypto.tink.**

# Error Prone annotations (transitive dependency of Tink)
-dontwarn com.google.errorprone.annotations.**
-keep class com.google.errorprone.annotations.** { *; }

# Retrofit & OkHttp
-dontwarn retrofit2.Platform$Java8
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}
-keepattributes Signature
-keepattributes *Annotation*

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.test.internal.TestMainDispatcherFactory { *; }
-keepclassmembers class kotlinx.coroutines.android.internal.MainDispatcherLoader {
    public static final kotlinx.coroutines.MainCoroutineDispatcher dispatcher;
}
-keepclassmembers class kotlinx.coroutines.scheduling.WorkQueue {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class java.util.concurrent.atomic.AtomicLongFieldUpdater {
    private volatile long value;
}
-keepclassmembers class java.util.concurrent.atomic.AtomicIntegerFieldUpdater {
    private volatile int value;
}
-keepclassmembers class java.util.concurrent.atomic.AtomicReferenceFieldUpdater {
    private volatile java.lang.Object value;
}

# Hilt
-dontwarn dagger.hilt.internal.processedrootsentinel.codegen.**
-keepclassmembers class **_HiltModules {
    @dagger.Module <methods>;
}

# Google API Client
-keep class com.google.api.client.http.** { *; }
-keep class org.joda.time.** { *; }
-dontwarn com.google.api.client.http.**
-dontwarn org.joda.time.**
