package com.enach.client.di

import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import android.content.Context
import com.enach.client.data.repository.ENachRepository
import com.enach.client.workers.JobStatusWorker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {
    
    @Provides
    @Singleton
    fun provideWorkerFactory(
        jobStatusWorkerProvider: Provider<JobStatusWorker.Factory>
    ): WorkerFactory {
        return ENachWorkerFactory(jobStatusWorkerProvider)
    }
}

class ENachWorkerFactory @Inject constructor(
    private val jobStatusWorkerProvider: Provider<JobStatusWorker.Factory>
) : WorkerFactory() {
    
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            JobStatusWorker::class.java.name -> {
                jobStatusWorkerProvider.get().create(appContext, workerParameters)
            }
            else -> null
        }
    }
}
