package com.example.ai_kit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun Bitmap.getImprovedBitmap(): Bitmap {
    val stream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 100, stream)
    val byteArray = stream.toByteArray()
    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}
fun Bitmap.toUri(context: Context): Uri? {
    // Create a file to save the bitmap
    val file = File(context.cacheDir, "profileImage.jpg")
    try {
        val outputStream = FileOutputStream(file)
        compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        // Generate a content URI for the file using FileProvider
        return FileProvider.getUriForFile(context, "com.example.ai_kit.fileprovider", file)
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return null
}


fun Uri.toBitmap(context: Context): Bitmap? {
    return try {
        context.contentResolver.openInputStream(this)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}