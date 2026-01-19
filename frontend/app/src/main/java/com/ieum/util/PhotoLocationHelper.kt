package com.ieum.util

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object PhotoLocationHelper {

    fun getLatLngFromUri(contentResolver: ContentResolver, uri: Uri): Pair<Double, Double>? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val exif = ExifInterface(inputStream!!)
            val latLong = FloatArray(2)
            if (exif.getLatLong(latLong)) {
                Pair(latLong[0].toDouble(), latLong[1].toDouble())
            } else {
                getLatLngFromHeic(contentResolver, uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getLatLngFromHeic(contentResolver: ContentResolver, uri: Uri): Pair<Double, Double>? {
        val tempFile = createTempFileFromUri(contentResolver, uri)
        return try {
            val exif = ExifInterface(tempFile.absolutePath)
            val latLong = FloatArray(2)
            if (exif.getLatLong(latLong)) {
                Pair(latLong[0].toDouble(), latLong[1].toDouble())
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            tempFile.delete()
        }
    }

    private fun createTempFileFromUri(contentResolver: ContentResolver, uri: Uri): File {
        val tempFile = File.createTempFile("temp_heic", ".heic")
        tempFile.deleteOnExit()
        val inputStream = contentResolver.openInputStream(uri) as InputStream
        val outputStream = FileOutputStream(tempFile)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        return tempFile
    }
}
