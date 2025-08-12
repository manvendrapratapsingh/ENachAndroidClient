package com.enach.client.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileUtils {
    
    fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = getFileName(context, uri)
            val tempFile = File(context.cacheDir, fileName)
            
            val outputStream = FileOutputStream(tempFile)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun getFileName(context: Context, uri: Uri): String {
        var fileName = "temp_file"
        
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex != -1) {
                fileName = cursor.getString(nameIndex)
            }
        }
        
        return fileName
    }
    
    fun getFileSize(context: Context, uri: Uri): Long {
        var size = 0L
        
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst() && sizeIndex != -1) {
                size = cursor.getLong(sizeIndex)
            }
        }
        
        return size
    }
    
    fun isFileSizeValid(context: Context, uri: Uri, maxSizeMB: Int = 10): Boolean {
        val size = getFileSize(context, uri)
        val maxSizeBytes = maxSizeMB * 1024 * 1024
        return size <= maxSizeBytes
    }
    
    fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast(".", "")
    }
    
    fun isImageFile(fileName: String): Boolean {
        val extension = getFileExtension(fileName).lowercase()
        return extension in listOf("jpg", "jpeg", "png", "bmp", "tiff")
    }
    
    fun isPdfFile(fileName: String): Boolean {
        val extension = getFileExtension(fileName).lowercase()
        return extension == "pdf"
    }
    
    fun cleanupTempFiles(context: Context) {
        try {
            context.cacheDir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
