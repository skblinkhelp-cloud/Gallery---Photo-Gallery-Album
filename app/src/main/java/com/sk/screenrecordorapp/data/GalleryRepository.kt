package com.sk.screenrecordorapp.data

import android.content.Context
import android.util.Log
import com.sk.screenrecordorapp.R
import com.sk.screenrecordorapp.model.FolderItem
import com.sk.screenrecordorapp.model.MediaItem
import com.sk.screenrecordorapp.model.MediaType
import com.sk.screenrecordorapp.model.PhotoFilter
import com.sk.screenrecordorapp.model.SmartCategory
import com.sk.screenrecordorapp.model.StorageLocation
import com.sk.screenrecordorapp.model.StorageStats
import com.sk.screenrecordorapp.util.MediaStoreHelper
import com.sk.screenrecordorapp.util.NotificationHelper
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

  private val _lastOrganizedInfo = MutableStateFlow("Auto-organized last night • 48 items grouped")
  val lastOrganizedInfo: StateFlow<String> = _lastOrganizedInfo.asStateFlow()

  private val _isScanningDevice = MutableStateFlow(false)
  val isScanningDevice: StateFlow<Boolean> = _isScanningDevice.asStateFlow()

  init {
    loadInitialData()
    scanDeviceMedia()
  }

  private fun loadInitialData() {
    val initialItems = listOf(
      MediaItem(
        id = "m_nature_1",
        title = "Alpine Sunset Mirror Lake",
        drawableResId = R.drawable.sample_nature_mountain,
        mediaType = MediaType.PHOTO,
        category = SmartCategory.NATURE,
        timestamp = System.currentTimeMillis() - 3_600_000L * 2,
        dateFormatted = "Today, 4:15 PM",
        timelineGroup = "Today",
        location = "Yosemite National Park, CA",
        isFavorite = true,
        storageLocation = StorageLocation.INTERNAL,
        folderName = "Camera",
        fileSizeBytes = 3_420_000L,
        width = 1920,
        height = 1440,
        isCloudSynced = true
      ),
      MediaItem(
        id = "m_people_1",
        title = "Elena Golden Hour Walk",
        drawableResId = R.drawable.sample_portrait_smile,
        mediaType = MediaType.PHOTO,
        category = SmartCategory.PEOPLE,
        timestamp = System.currentTimeMillis() - 3_600_000L * 5,
        dateFormatted = "Today, 1:20 PM",
        timelineGroup = "Today",
        location = "Dolores Park, San Francisco",
        isFavorite = true,
        storageLocation = StorageLocation.INTERNAL,
        folderName = "Camera",
        fileSizeBytes = 2_890_000L,
        width = 1440,
        height = 1920,
        isCloudSynced = true
      ),
      MediaItem(
        id = "m_animal_1",
        title = "Autumn Fox Kit in Snow",
        drawableResId = R.drawable.sample_animal_fox,
        mediaType = MediaType.PHOTO,
        category = SmartCategory.ANIMALS,
        timestamp = System.currentTimeMillis() - 86_400_000L,
        dateFormatted = "Yesterday, 3:45 PM",
        timelineGroup = "Yesterday",
        location = "Lake Tahoe Forest, NV",
        isFavorite = true,
        storageLocation = StorageLocation.SD_CARD,
        folderName = "Nature & Wildlife",
        fileSizeBytes = 3_150_000L,
        width = 1920,
        height = 1440,
        isCloudSynced = true
      ),
      MediaItem(
        id = "m_selfie_1",
        title = "Sunny Trailhead Selfie",
        drawableResId = R.drawable.sample_portrait_smile,
        mediaType = MediaType.PHOTO,
        category = SmartCategory.SELFIES,
        timestamp = System.currentTimeMillis() - 86_400_000L * 2,
        dateFormatted = "Oct 10, 2026",
        timelineGroup = "October 2026",
        location = "Marin Headlands, CA",
        isFavorite = false,
        storageLocation = StorageLocation.INTERNAL,
        folderName = "Camera",
        fileSizeBytes = 2_340_000L,
        width = 1440,
        height = 1920,
        isCloudSynced = true
      ),
      MediaItem(
        id = "m_nature_2",
        title = "Mountain Peaks Morning Fog",
        drawableResId = R.drawable.sample_nature_mountain,
        mediaType = MediaType.PHOTO,
        category = SmartCategory.NATURE,
        timestamp = System.currentTimeMillis() - 86_400_000L * 4,
        dateFormatted = "Oct 8, 2026",
        timelineGroup = "October 2026",
        location = "Sierra Nevada, CA",
        isFavorite = false,
        storageLocation = StorageLocation.SD_CARD,
        folderName = "SD Backup",
        fileSizeBytes = 4_100_000L,
        width = 1920,
        height = 1440,
        isCloudSynced = true
      ),
      MediaItem(
        id = "m_doc_1",
        title = "Park Permit & Trail Map",
        drawableResId = R.drawable.sample_nature_mountain,
        mediaType = MediaType.PHOTO,
        category = SmartCategory.DOCUMENTS,
        timestamp = System.currentTimeMillis() - 86_400_000L * 6,
        dateFormatted = "Oct 6, 2026",
        timelineGroup = "October 2026",
        location = "Visitor Center",
        isFavorite = false,
        storageLocation = StorageLocation.INTERNAL,
        folderName = "Documents",
        fileSizeBytes = 1_220_000L,
        width = 1080,
        height = 1920,
        isCloudSynced = true
      ),
      MediaItem(
        id = "m_video_1",
        title = "Crystal Lake Drone Loop",
        drawableResId = R.drawable.sample_nature_mountain,
        mediaType = MediaType.VIDEO,
        category = SmartCategory.VIDEOS,
        timestamp = System.currentTimeMillis() - 86_400_000L * 8,
        dateFormatted = "Oct 4, 2026",
        timelineGroup = "October 2026",
        location = "Alpine Lake Vista",
        isFavorite = true,
        storageLocation = StorageLocation.SD_CARD,
        folderName = "Trip 2026",
        fileSizeBytes = 48_500_000L,
        width = 1920,
        height = 1080,
        durationSeconds = 48,
        isCloudSynced = true
      ),
      MediaItem(
        id = "m_movie_1",
        title = "California Wilderness Reel",
        drawableResId = R.drawable.sample_nature_mountain,
        mediaType = MediaType.VIDEO,
        category = SmartCategory.MOVIES,
        timestamp = System.currentTimeMillis() - 86_400_000L * 15,
        dateFormatted = "Sep 27, 2026",
        timelineGroup = "September 2026",
        location = "Highway 1 Coastline",
        isFavorite = false,
        storageLocation = StorageLocation.INTERNAL,
        folderName = "Trip 2026",
        fileSizeBytes = 120_000_000L,
        width = 1920,
        height = 1080,
        durationSeconds = 142,
        isCloudSynced = true
      ),
      MediaItem(
        id = "m_animal_2",
        title = "Playful Puppy in Park",
        drawableResId = R.drawable.sample_animal_fox,
        mediaType = MediaType.PHOTO,
        category = SmartCategory.ANIMALS,
        timestamp = System.currentTimeMillis() - 86_400_000L * 18,
        dateFormatted = "Sep 24, 2026",
        timelineGroup = "September 2026",
        location = "Presidio, San Francisco",
        isFavorite = false,
        storageLocation = StorageLocation.INTERNAL,
        folderName = "Camera",
        fileSizeBytes = 2_700_000L,
        width = 1920,
        height = 1440,
        isCloudSynced = true
      ),
      MediaItem(
        id = "m_people_2",
        title = "Friends Picnic at Sunset",
        drawableResId = R.drawable.sample_portrait_smile,
        mediaType = MediaType.PHOTO,
        category = SmartCategory.PEOPLE,
        timestamp = System.currentTimeMillis() - 86_400_000L * 22,
        dateFormatted = "Sep 20, 2026",
        timelineGroup = "September 2026",
        location = "Bernal Heights, CA",
        isFavorite = false,
        storageLocation = StorageLocation.SD_CARD,
        folderName = "Trip 2026",
        fileSizeBytes = 3_800_000L,
        width = 1440,
        height = 1920,
        isCloudSynced = true
      ),
      MediaItem(
        id = "m_screenshot_1",
        title = "App Settings Screenshot",
        drawableResId = R.drawable.sample_nature_mountain,
        mediaType = MediaType.PHOTO,
        category = SmartCategory.DOCUMENTS,
        timestamp = System.currentTimeMillis() - 86_400_000L * 3,
        dateFormatted = "Oct 9, 2026",
        timelineGroup = "October 2026",
        location = "Device Screen",
        isFavorite = false,
        storageLocation = StorageLocation.INTERNAL,
        folderName = "Screenshots",
        fileSizeBytes = 1_850_000L,
        width = 1080,
        height = 2400,
        isCloudSynced = false
      )
    )

    _mediaItems.value = initialItems
    recalculateFolders(initialItems)
  }

  fun scanDeviceMedia() {
    coroutineScope.launch(Dispatchers.IO) {
      _isScanningDevice.value = true
      try {
        val deviceMedia = MediaStoreHelper.loadDeviceMedia(appContext)
        if (deviceMedia.isNotEmpty()) {
          _mediaItems.update { current ->
            val userCurrent = current.filterNot { it.id.startsWith("m_") }
            val existingIds = deviceMedia.map { it.id }.toSet()
            val filteredCurrent = userCurrent.filterNot { it.id in existingIds }
            (deviceMedia + filteredCurrent).sortedByDescending { it.timestamp }
          }
          recalculateFolders(_mediaItems.value)
        }
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
        coverResId = cover?.drawableResId ?: if (cover?.customUri != null) null else R.drawable.sample_nature_mountain,
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
      coverResId = R.drawable.sample_nature_mountain,
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
    val arrival = MediaItem(
      id = "arrival_${UUID.randomUUID()}",
      title = "New Arrival: Coastal Cliffs Sunset",
      drawableResId = R.drawable.sample_nature_mountain,
      mediaType = MediaType.PHOTO,
      category = SmartCategory.NATURE,
      timestamp = System.currentTimeMillis(),
      dateFormatted = "Just now",
      timelineGroup = "Today",
      location = "Big Sur Coast, CA",
      isFavorite = false,
      storageLocation = StorageLocation.INTERNAL,
      folderName = "Camera",
      fileSizeBytes = 3_750_000L,
      width = 1920,
      height = 1440,
      isCloudSynced = true
    )

    _mediaItems.update { listOf(arrival) + it }
    recalculateFolders(_mediaItems.value)
    NotificationHelper.notifyNewArrivals(appContext, 1, "Cloud Feed")
  }
}
