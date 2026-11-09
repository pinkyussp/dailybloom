package com.example.dailyquotes.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.util.UUID

/** Photos are copied into the app's private storage so they stay with the entry. */
object PhotoStore {

    private fun dir(context: Context): File =
        File(context.filesDir, "photos").apply { mkdirs() }

    fun file(context: Context, name: String): File = File(dir(context), name)

    /** Copies the picked image into app storage. Returns the stored file name, or null on failure. */
    fun importFromUri(context: Context, uri: Uri): String? {
        val name = "${UUID.randomUUID()}.jpg"
        val target = file(context, name)
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return null
            input.use { stream ->
                target.outputStream().use { out -> stream.copyTo(out) }
            }
            name
        } catch (e: Exception) {
            target.delete()
            null
        }
    }

    fun delete(context: Context, name: String) {
        file(context, name).delete()
    }

    fun deleteAll(context: Context) {
        dir(context).deleteRecursively()
    }

    /** Decodes a downscaled bitmap so large photos don't use much memory. */
    fun decodeThumbnail(path: String, maxSize: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= maxSize && bounds.outHeight / (sample * 2) >= maxSize) {
            sample *= 2
        }
        return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
    }
}
