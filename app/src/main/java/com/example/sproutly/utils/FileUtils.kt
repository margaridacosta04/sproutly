package com.example.sproutly.utils

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    fun getMultipartFromUri(context: Context, uri: Uri, paramName: String): MultipartBody.Part? {
        return try {
            val contentResolver = context.contentResolver
            val tempFile = File(context.cacheDir, "upload_temp.jpg")

            val inputStream = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(tempFile)

            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            val reqFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(paramName, tempFile.name, reqFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}