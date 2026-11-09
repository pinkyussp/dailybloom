package com.example.dailyquotes.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.util.UUID

/** Photos are copied into the app's private storage so they stay with the entry. */
object PhotoStore {

    private const val MAX_STORED_SIZE = 2048

    private fun dir(context: Context): File =
        File(context.filesDir, "photos").apply { mkdirs() }

    fun file(context: Context, name: String): File = File(dir(context), name)

    /**
     * Copies the picked image into app storage, rotated upright and resized to a sensible
     * size. Returns the stored file name, or null if the file is not a readable image.
     */
    fun importFromUri(context: Context, uri: Uri): String? {
        val name = "${UUID.randomUUID()}.jpg"
        val target = file(context, name)
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return null
            input.use { stream ->
                target.outputStream().use { out -> stream.copyTo(out) }
            }
            val bitmap = decodeThumbnail(target.path, MAX_STORED_SIZE)
                ?: throw IllegalStateException("Not an image")
            target.outputStream().use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out) }
            bitmap.recycle()
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

    /** Decodes a downscaled bitmap, rotated to match the camera's orientation tag. */
    fun decodeThumbnail(path: String, maxSize: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sample = 1
        while (bounds.outWidth / (sample * 2) >= maxSize && bounds.outHeight / (sample * 2) >= maxSize) {
            sample *= 2
        }
        val decoded = BitmapFactory.decodeFile(
            path,
            BitmapFactory.Options().apply { inSampleSize = sample }
        ) ?: return null

        val matrix = Matrix()
        when (orientation(path)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return decoded
        }
        val rotated = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
        if (rotated != decoded) decoded.recycle()
        return rotated
    }

    private fun orientation(path: String): Int = try {
        ExifInterface(path).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
    } catch (e: Exception) {
        ExifInterface.ORIENTATION_NORMAL
    }
}
