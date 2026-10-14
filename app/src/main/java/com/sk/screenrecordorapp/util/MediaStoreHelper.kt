package com.sk.screenrecordorapp.util

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.sk.screenrecordorapp.model.MediaItem
import com.sk.screenrecordorapp.model.MediaType
import com.sk.screenrecordorapp.model.SmartCategory
import com.sk.screenrecordorapp.model.StorageLocation
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object MediaStoreHelper {

  private const val TAG = "MediaStoreHelper"

  fun loadDeviceMedia(context: Context): List<MediaItem> {
    val result = mutableListOf<MediaItem>()
    try {
      result.addAll(queryImages(context))
      result.addAll(queryVideos(context))
    } catch (e: Exception) {
      Log.e(TAG, "Error querying MediaStore", e)
    }
    return result.sortedByDescending { it.timestamp }
  }

  private fun queryImages(context: Context): List<MediaItem> {
    val items = mutableListOf<MediaItem>()
    val projection = mutableListOf(
      MediaStore.Images.Media._ID,
      MediaStore.Images.Media.DISPLAY_NAME,
      MediaStore.Images.Media.DATE_MODIFIED,
      MediaStore.Images.Media.SIZE,
      MediaStore.Images.Media.WIDTH,
      MediaStore.Images.Media.HEIGHT,
      MediaStore.Images.Media.BUCKET_DISPLAY_NAME
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      projection.add(MediaStore.Images.Media.RELATIVE_PATH)
    }

    val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"

    try {
      context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection.toTypedArray(),
        null,
        null,
        sortOrder
      )?.use { cursor ->
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
        val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
        val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
        val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
        val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
        val bucketCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        val relativePathCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          cursor.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH)
        } else -1

        while (cursor.moveToNext()) {
          val id = cursor.getLong(idCol)
          val name = cursor.getString(nameCol) ?: "Image_$id"
          val dateSec = cursor.getLong(dateCol)
          val timestamp = if (dateSec > 0) dateSec * 1000L else System.currentTimeMillis()
          val sizeBytes = cursor.getLong(sizeCol)
          val width = cursor.getInt(widthCol).let { if (it > 0) it else 1920 }
          val height = cursor.getInt(heightCol).let { if (it > 0) it else 1080 }
          val bucketName = cursor.getString(bucketCol) ?: "Photos"
          val relativePath = if (relativePathCol >= 0) cursor.getString(relativePathCol) ?: "" else ""

          val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

          val isScreenshot = bucketName.contains("screenshot", ignoreCase = true) ||
            name.contains("screenshot", ignoreCase = true) ||
            relativePath.contains("screenshot", ignoreCase = true)

          val folder = when {
            isScreenshot -> "Screenshots"
            bucketName.contains("camera", ignoreCase = true) || bucketName.contains("dcim", ignoreCase = true) -> "Camera"
            bucketName.contains("download", ignoreCase = true) -> "Downloads"
            else -> bucketName
          }

          val category = when {
            isScreenshot -> SmartCategory.DOCUMENTS
            name.contains("selfie", ignoreCase = true) -> SmartCategory.SELFIES
            name.contains("nature", ignoreCase = true) || name.contains("landscape", ignoreCase = true) -> SmartCategory.NATURE
            name.contains("cat", ignoreCase = true) || name.contains("dog", ignoreCase = true) || name.contains("pet", ignoreCase = true) -> SmartCategory.ANIMALS
            else -> SmartCategory.PEOPLE
          }

          val timelineGroup = getTimelineGroup(timestamp)
          val dateFormatted = formatDate(timestamp)

          items.add(
            MediaItem(
              id = "device_img_$id",
              title = name.substringBeforeLast("."),
              customUri = contentUri.toString(),
              mediaType = MediaType.PHOTO,
              category = category,
              timestamp = timestamp,
              dateFormatted = dateFormatted,
              timelineGroup = timelineGroup,
              location = if (isScreenshot) "Device Screen" else "Device Storage",
              isFavorite = false,
              storageLocation = StorageLocation.INTERNAL,
              folderName = folder,
              fileSizeBytes = if (sizeBytes > 0) sizeBytes else 1_500_000L,
              width = width,
              height = height,
              isCloudSynced = false
            )
          )
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error querying images", e)
    }

    return items
  }

  private fun queryVideos(context: Context): List<MediaItem> {
    val items = mutableListOf<MediaItem>()
    val projection = mutableListOf(
      MediaStore.Video.Media._ID,
      MediaStore.Video.Media.DISPLAY_NAME,
      MediaStore.Video.Media.DATE_MODIFIED,
      MediaStore.Video.Media.SIZE,
      MediaStore.Video.Media.WIDTH,
      MediaStore.Video.Media.HEIGHT,
      MediaStore.Video.Media.DURATION,
      MediaStore.Video.Media.BUCKET_DISPLAY_NAME
    )
    val sortOrder = "${MediaStore.Video.Media.DATE_MODIFIED} DESC"

    try {
      context.contentResolver.query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        projection.toTypedArray(),
        null,
        null,
        sortOrder
      )?.use { cursor ->
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
        val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
        val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
        val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
        val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
        val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
        val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
        val bucketCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

        while (cursor.moveToNext()) {
          val id = cursor.getLong(idCol)
          val name = cursor.getString(nameCol) ?: "Video_$id"
          val dateSec = cursor.getLong(dateCol)
          val timestamp = if (dateSec > 0) dateSec * 1000L else System.currentTimeMillis()
          val sizeBytes = cursor.getLong(sizeCol)
          val width = cursor.getInt(widthCol).let { if (it > 0) it else 1920 }
          val height = cursor.getInt(heightCol).let { if (it > 0) it else 1080 }
          val durationMs = cursor.getLong(durationCol)
          val bucketName = cursor.getString(bucketCol) ?: "Camera"

          val contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)

          items.add(
            MediaItem(
              id = "device_vid_$id",
              title = name.substringBeforeLast("."),
              customUri = contentUri.toString(),
              mediaType = MediaType.VIDEO,
              category = if (durationMs > 60_000L) SmartCategory.MOVIES else SmartCategory.VIDEOS,
              timestamp = timestamp,
              dateFormatted = formatDate(timestamp),
              timelineGroup = getTimelineGroup(timestamp),
              location = "Device Storage",
              isFavorite = false,
              storageLocation = StorageLocation.INTERNAL,
              folderName = bucketName,
              fileSizeBytes = if (sizeBytes > 0) sizeBytes else 15_000_000L,
              width = width,
              height = height,
              durationSeconds = (durationMs / 1000L).toInt(),
              isCloudSynced = false
            )
          )
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error querying videos", e)
    }

    return items
  }

  private fun getTimelineGroup(timestamp: Long): String {
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { timeInMillis = timestamp }

    return if (now.get(Calendar.YEAR) == then.get(Calendar.YEAR)) {
      if (now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)) {
        "Today"
      } else if (now.get(Calendar.DAY_OF_YEAR) - then.get(Calendar.DAY_OF_YEAR) == 1) {
        "Yesterday"
      } else {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(timestamp))
      }
    } else {
      SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(timestamp))
    }
  }

  private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(timestamp))
  }
}
