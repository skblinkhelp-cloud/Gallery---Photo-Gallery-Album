package com.sk.gallery.photo.album.data

import android.content.Context
import android.util.Log
import com.sk.gallery.photo.album.model.FolderItem
import com.sk.gallery.photo.album.model.MediaItem
import com.sk.gallery.photo.album.model.MediaType
import com.sk.gallery.photo.album.model.SmartCategory
import com.sk.gallery.photo.album.model.StorageLocation
import com.sk.gallery.photo.album.model.StorageStats
import com.sk.gallery.photo.album.util.MediaStoreHelper
import com.sk.gallery.photo.album.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class GalleryRepository(private val appContext: Context) {

  private val coroutineScope = CoroutineScope(Dispatchers.Main)

  private val _mediaItems = MutableStateFlow<List<MediaItem>>(emptyList())
  val mediaItems: StateFlow<List<MediaItem>> = _mediaItems.asStateFlow()

  private val _folders = MutableStateFlow<List<FolderItem>>(emptyList())
  val folders: StateFlow<List<FolderItem>> = _folders.asStateFlow()

  private val _storageStats = MutableStateFlow(StorageStats())
  val storageStats: StateFlow<StorageStats> = _storageStats.asStateFlow()

  private val _isSyncing = MutableStateFlow(false)
  val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

  private val _lastSyncTime = MutableStateFlow("Just now")
  val lastSyncTime: StateFlow<String> = _lastSyncTime.asStateFlow()

  private val _isAutoOrganizing = MutableStateFlow(false)
  val isAutoOrganizing: StateFlow<Boolean> = _isAutoOrganizing.asStateFlow()

  private val _lastOrganizedInfo = MutableStateFlow("Auto-organized last night")
  val lastOrganizedInfo: StateFlow<String> = _lastOrganizedInfo.asStateFlow()

  private val _isScanningDevice = MutableStateFlow(false)
  val isScanningDevice: StateFlow<Boolean> = _isScanningDevice.asStateFlow()

  init {
    scanDeviceMedia()
  }

  fun scanDeviceMedia() {
    coroutineScope.launch(Dispatchers.IO) {
      _isScanningDevice.value = true
      try {
        val deviceMedia = MediaStoreHelper.loadDeviceMedia(appContext)
        _mediaItems.value = deviceMedia
        recalculateFolders(deviceMedia)
      } catch (e: Exception) {
        Log.e("GalleryRepository", "Failed to scan device media", e)
      } finally {
        _isScanningDevice.value = false
      }
    }
  }

  private fun recalculateFolders(items: List<MediaItem>) {
    val folderMap = mutableMapOf<Pair<String, StorageLocation>, MutableList<MediaItem>>()

    for (item in items) {
      val key = item.folderName to item.storageLocation
      folderMap.getOrPut(key) { mutableListOf() }.add(item)
    }

    val folderList = folderMap.map { (key, folderMedia) ->
      val (name, storage) = key
      val cover = folderMedia.firstOrNull()
      val totalBytes = folderMedia.sumOf { it.fileSizeBytes }
      val sizeMb = totalBytes / (1024.0 * 1024.0)

      FolderItem(
        id = "f_${storage.name.lowercase()}_${name.lowercase().replace(" ", "_")}",
        name = name,
        storageLocation = storage,
        coverResId = cover?.drawableResId,
        coverUri = cover?.customUri,
        itemCount = folderMedia.size,
        sizeMb = sizeMb
      )
    }.sortedWith(
      compareBy<FolderItem> { it.storageLocation }
        .thenByDescending { it.itemCount }
    )

    _folders.value = folderList
  }

  fun addImportedMedia(uris: List<String>) {
    if (uris.isEmpty()) return
    val newItems = uris.mapIndexed { index, uri ->
      MediaItem(
        id = "imported_${UUID.randomUUID()}",
        title = "Imported Photo ${index + 1}",
        customUri = uri,
        mediaType = if (uri.contains("video", ignoreCase = true)) MediaType.VIDEO else MediaType.PHOTO,
        category = SmartCategory.PEOPLE,
        timestamp = System.currentTimeMillis(),
        dateFormatted = "Just now",
        timelineGroup = "Today",
        location = "On Device",
        isFavorite = false,
        storageLocation = StorageLocation.INTERNAL,
        folderName = "Camera",
        fileSizeBytes = 3_200_000L,
        width = 1920,
        height = 1080,
        isCloudSynced = false
      )
    }

    _mediaItems.update { current -> newItems + current }
    recalculateFolders(_mediaItems.value)
    NotificationHelper.notifyNewArrivals(appContext, uris.size, "Photo Picker Import")
  }

  fun toggleFavorite(id: String) {
    _mediaItems.update { list ->
      list.map { if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it }
    }
  }

  fun updateMedia(updated: MediaItem) {
    _mediaItems.update { list ->
      list.map { if (it.id == updated.id) updated else it }
    }
    recalculateFolders(_mediaItems.value)
  }

  fun deleteMedia(ids: Set<String>) {
    _mediaItems.update { list ->
      list.filterNot { it.id in ids }
    }
    recalculateFolders(_mediaItems.value)
  }

  fun applyAutoEnhance(id: String) {
    _mediaItems.update { list ->
      list.map { item ->
        if (item.id == id) {
          val newEnhanced = !item.autoEnhanced
          item.copy(
            autoEnhanced = newEnhanced,
            enhanceStrength = if (newEnhanced) 1f else 0f
          )
        } else item
      }
    }
  }

  fun applyBatchAutoEnhance(ids: Set<String>) {
    _mediaItems.update { list ->
      list.map { item ->
        if (item.id in ids) {
          item.copy(autoEnhanced = true, enhanceStrength = 1f)
        } else item
      }
    }
  }

  fun moveToFolder(ids: Set<String>, targetFolder: String, targetStorage: StorageLocation) {
    _mediaItems.update { list ->
      list.map { item ->
        if (item.id in ids) {
          item.copy(folderName = targetFolder, storageLocation = targetStorage)
        } else item
      }
    }
    recalculateFolders(_mediaItems.value)
  }

  fun copyToFolder(ids: Set<String>, targetFolder: String, targetStorage: StorageLocation) {
    val itemsToCopy = _mediaItems.value.filter { it.id in ids }.map { item ->
      item.copy(
        id = "copy_${UUID.randomUUID()}",
        title = "${item.title} (Copy)",
        folderName = targetFolder,
        storageLocation = targetStorage
      )
    }
    _mediaItems.update { it + itemsToCopy }
    recalculateFolders(_mediaItems.value)
  }

  fun createFolder(name: String, storage: StorageLocation) {
    val newFolder = FolderItem(
      id = "f_${storage.name.lowercase()}_${name.lowercase().replace(" ", "_")}",
      name = name,
      storageLocation = storage,
      coverResId = null,
      itemCount = 0,
      sizeMb = 0.0
    )
    _folders.update { current ->
      if (current.any { it.name.equals(name, ignoreCase = true) && it.storageLocation == storage }) {
        current
      } else {
        listOf(newFolder) + current
      }
    }
  }

  fun runNightlyAutoOrganization() {
    if (_isAutoOrganizing.value) return
    _isAutoOrganizing.value = true

    coroutineScope.launch {
      delay(1200) // smooth processing feedback
      // Smart organization reclassification based on item features
      _mediaItems.update { list ->
        list.map { item ->
          val inferredCategory = when {
            item.title.contains("Fox", true) || item.title.contains("Puppy", true) || item.title.contains("Dog", true) -> SmartCategory.ANIMALS
            item.title.contains("Selfie", true) -> SmartCategory.SELFIES
            item.title.contains("Elena", true) || item.title.contains("Friend", true) || item.title.contains("Portrait", true) -> SmartCategory.PEOPLE
            item.title.contains("Permit", true) || item.title.contains("Doc", true) || item.title.contains("Receipt", true) -> SmartCategory.DOCUMENTS
            item.mediaType == MediaType.VIDEO && item.durationSeconds > 60 -> SmartCategory.MOVIES
            item.mediaType == MediaType.VIDEO -> SmartCategory.VIDEOS
            else -> SmartCategory.NATURE
          }
          item.copy(category = inferredCategory)
        }
      }

      val sortedCount = _mediaItems.value.size
      val catCount = _mediaItems.value.map { it.category }.distinct().size
      _lastOrganizedInfo.value = "Auto-organized just now • $sortedCount photos in $catCount categories"
      _isAutoOrganizing.value = false

      NotificationHelper.notifyAutoOrganizeComplete(appContext, sortedCount, catCount)
    }
  }

  fun triggerCloudSync() {
    if (_isSyncing.value) return
    _isSyncing.value = true

    coroutineScope.launch {
      delay(1500)
      _mediaItems.update { list ->
        list.map { it.copy(isCloudSynced = true) }
      }
      _lastSyncTime.value = "Just now"
      _isSyncing.value = false
      NotificationHelper.notifyNewArrivals(appContext, 3, "Google Cloud Sync")
    }
  }

  fun simulateNewArrivals() {
    scanDeviceMedia()
  }
}
