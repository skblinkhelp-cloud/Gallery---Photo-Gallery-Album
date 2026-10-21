package com.sk.gallery.photo.album.util

import android.Manifest
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

object NotificationHelper {
  private const val CHANNEL_ARRIVALS_ID = "gallery_arrivals"
  private const val CHANNEL_ARRIVALS_NAME = "New Photos & Sync"
  private const val CHANNEL_ORGANIZE_ID = "gallery_organize"
  private const val CHANNEL_ORGANIZE_NAME = "Auto-Organization"

  fun initChannels(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

      val arrivalsChannel =
        NotificationChannel(
          CHANNEL_ARRIVALS_ID,
          CHANNEL_ARRIVALS_NAME,
          NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
          description = "Notifications when new photos arrive from camera or cloud sync"
        }

      val organizeChannel =
        NotificationChannel(
          CHANNEL_ORGANIZE_ID,
          CHANNEL_ORGANIZE_NAME,
          NotificationManager.IMPORTANCE_LOW
        ).apply {
          description = "Nightly smart photo organization updates"
        }

      notificationManager.createNotificationChannel(arrivalsChannel)
      notificationManager.createNotificationChannel(organizeChannel)
    }
  }

  fun notifyNewArrivals(context: Context, count: Int, source: String = "Cloud Sync") {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (
        ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        return
      }
    }

    val notificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val notification =
      NotificationCompat.Builder(context, CHANNEL_ARRIVALS_ID)
        .setSmallIcon(R.drawable.ic_menu_camera)
        .setContentTitle("New Photos Arrived")
        .setContentText("$count new photos and videos synced from $source")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(1001, notification)
  }

  fun notifyAutoOrganizeComplete(context: Context, itemsSorted: Int, categoriesCount: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (
        ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        return
      }
    }

    val notificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val notification =
      NotificationCompat.Builder(context, CHANNEL_ORGANIZE_ID)
        .setSmallIcon(R.drawable.ic_menu_sort_by_size)
        .setContentTitle("Nightly Organization Complete ✨")
        .setContentText("Organized $itemsSorted photos into $categoriesCount smart categories")
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(1002, notification)
  }
}
