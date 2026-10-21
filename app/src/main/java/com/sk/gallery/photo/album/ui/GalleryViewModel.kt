package com.sk.gallery.photo.album.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sk.gallery.photo.album.data.GalleryRepository
import com.sk.gallery.photo.album.model.FolderItem
import com.sk.gallery.photo.album.model.MediaItem
import com.sk.gallery.photo.album.model.SmartCategory
import com.sk.gallery.photo.album.model.StorageLocation
import com.sk.gallery.photo.album.model.StorageStats
import com.sk.gallery.photo.album.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

enum class GalleryScreen {
  PHOTOS,
  FOLDERS,
  ORGANIZE,
  VIEWER,
  EDITOR
}

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

  private val prefs = application.getSharedPreferences("gallery_prefs", Context.MODE_PRIVATE)

  val repository = GalleryRepository(application.applicationContext)

  val mediaItems: StateFlow<List<MediaItem>> = repository.mediaItems
  val folders: StateFlow<List<FolderItem>> = repository.folders
  val storageStats: StateFlow<StorageStats> = repository.storageStats
  val isSyncing: StateFlow<Boolean> = repository.isSyncing
  val lastSyncTime: StateFlow<String> = repository.lastSyncTime
  val isAutoOrganizing: StateFlow<Boolean> = repository.isAutoOrganizing
  val lastOrganizedInfo: StateFlow<String> = repository.lastOrganizedInfo

  // UI State
  private val _currentScreen = MutableStateFlow(GalleryScreen.PHOTOS)
  val currentScreen: StateFlow<GalleryScreen> = _currentScreen.asStateFlow()

  private val _selectedCategory = MutableStateFlow(SmartCategory.ALL)
  val selectedCategory: StateFlow<SmartCategory> = _selectedCategory.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _selectedMediaIds = MutableStateFlow<Set<String>>(emptySet())
  val selectedMediaIds: StateFlow<Set<String>> = _selectedMediaIds.asStateFlow()

  private val _currentViewerMediaId = MutableStateFlow<String?>(null)
  val currentViewerMediaId: StateFlow<String?> = _currentViewerMediaId.asStateFlow()

  private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
  val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

  private val _gridColumnCount = MutableStateFlow(3)
  val gridColumnCount: StateFlow<Int> = _gridColumnCount.asStateFlow()

  // Storage tab filter: ALL, INTERNAL, SD_CARD
  private val _folderStorageFilter = MutableStateFlow<StorageLocation?>(null)
  val folderStorageFilter: StateFlow<StorageLocation?> = _folderStorageFilter.asStateFlow()

  // Organize banner persistent state
  private val _showOrganizeBanner = MutableStateFlow(
    prefs.getBoolean("show_organize_banner", true)
  )
  val showOrganizeBanner: StateFlow<Boolean> = _showOrganizeBanner.asStateFlow()

  // Dialog & Sheet states
  private val _showNewFolderDialog = MutableStateFlow(false)
  val showNewFolderDialog: StateFlow<Boolean> = _showNewFolderDialog.asStateFlow()

  private val _showMoveCopyDialog = MutableStateFlow(false)
  val showMoveCopyDialog: StateFlow<Boolean> = _showMoveCopyDialog.asStateFlow()
  var isCopyOperation: Boolean = false
    private set

  private val _showSettingsDialog = MutableStateFlow(false)
  val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

  private val _showMediaInfoSheet = MutableStateFlow(false)
  val showMediaInfoSheet: StateFlow<Boolean> = _showMediaInfoSheet.asStateFlow()

  private val _showCleanStorageDialog = MutableStateFlow(false)
  val showCleanStorageDialog: StateFlow<Boolean> = _showCleanStorageDialog.asStateFlow()

  val isScanningDevice: StateFlow<Boolean> = repository.isScanningDevice

  // Filtered Media Flow
  val filteredMediaItems: StateFlow<List<MediaItem>> = combine(
    mediaItems,
    selectedCategory,
    searchQuery
  ) { list, category, query ->
    val cleanQuery = query.trim()
    list.filter { item ->
      val matchesCategory = (category == SmartCategory.ALL) || (item.category == category)
      val matchesQuery = cleanQuery.isBlank() ||
        item.title.contains(cleanQuery, ignoreCase = true) ||
        item.location.contains(cleanQuery, ignoreCase = true) ||
        item.folderName.equals(cleanQuery, ignoreCase = true) ||
        item.folderName.contains(cleanQuery, ignoreCase = true) ||
        item.timelineGroup.contains(cleanQuery, ignoreCase = true)
      matchesCategory && matchesQuery
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun setScreen(screen: GalleryScreen) {
    _currentScreen.value = screen
    if (screen != GalleryScreen.PHOTOS) {
      clearSelection()
    }
  }

  fun dismissOrganizeBanner() {
    _showOrganizeBanner.value = false
    prefs.edit().putBoolean("show_organize_banner", false).apply()
  }

  fun selectCategory(category: SmartCategory) {
    _selectedCategory.value = category
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun toggleThemeMode() {
    _themeMode.update { current ->
      when (current) {
        ThemeMode.SYSTEM -> ThemeMode.DARK
        ThemeMode.DARK -> ThemeMode.LIGHT
        ThemeMode.LIGHT -> ThemeMode.SYSTEM
      }
    }
  }

  fun setThemeMode(mode: ThemeMode) {
    _themeMode.value = mode
  }

  fun toggleGridDensity() {
    _gridColumnCount.update { current ->
      when (current) {
        2 -> 3
        3 -> 4
        else -> 2
      }
    }
  }

  fun setFolderStorageFilter(location: StorageLocation?) {
    _folderStorageFilter.value = location
  }

  // Selection
  val isSelectionMode: Boolean
    get() = _selectedMediaIds.value.isNotEmpty()

  fun toggleSelection(id: String) {
    _selectedMediaIds.update { current ->
      if (id in current) current - id else current + id
    }
  }

  fun selectAll() {
    _selectedMediaIds.value = filteredMediaItems.value.map { it.id }.toSet()
  }

  fun clearSelection() {
    _selectedMediaIds.value = emptySet()
  }

  // Viewer
  fun openViewer(id: String) {
    _currentViewerMediaId.value = id
    _currentScreen.value = GalleryScreen.VIEWER
  }

  fun closeViewer() {
    _currentScreen.value = GalleryScreen.PHOTOS
  }

  fun openEditor(id: String) {
    _currentViewerMediaId.value = id
    _currentScreen.value = GalleryScreen.EDITOR
  }

  fun closeEditor() {
    _currentScreen.value = GalleryScreen.VIEWER
  }

  fun nextViewerMedia() {
    val currentList = filteredMediaItems.value.ifEmpty { mediaItems.value }
    val currentId = _currentViewerMediaId.value ?: return
    val currentIndex = currentList.indexOfFirst { it.id == currentId }
    if (currentIndex in currentList.indices && currentIndex < currentList.lastIndex) {
      _currentViewerMediaId.value = currentList[currentIndex + 1].id
    }
  }

  fun previousViewerMedia() {
    val currentList = filteredMediaItems.value.ifEmpty { mediaItems.value }
    val currentId = _currentViewerMediaId.value ?: return
    val currentIndex = currentList.indexOfFirst { it.id == currentId }
    if (currentIndex > 0) {
      _currentViewerMediaId.value = currentList[currentIndex - 1].id
    }
  }

  // Media Actions
  fun toggleFavorite(id: String) {
    repository.toggleFavorite(id)
  }

  fun applyAutoEnhance(id: String) {
    repository.applyAutoEnhance(id)
  }

  fun applyBatchAutoEnhance() {
    val ids = _selectedMediaIds.value
    repository.applyBatchAutoEnhance(ids)
    clearSelection()
  }

  fun deleteSelectedMedia() {
    val ids = _selectedMediaIds.value
    repository.deleteMedia(ids)
    clearSelection()
  }

  fun deleteViewerMedia(id: String) {
    val currentList = filteredMediaItems.value
    val currentIndex = currentList.indexOfFirst { it.id == id }
    repository.deleteMedia(setOf(id))
    if (currentList.size <= 1) {
      closeViewer()
    } else {
      val nextIndex = if (currentIndex < currentList.lastIndex) currentIndex + 1 else currentIndex - 1
      if (nextIndex in currentList.indices) {
        _currentViewerMediaId.value = currentList[nextIndex].id
      } else {
        closeViewer()
      }
    }
  }

  fun openMoveCopyDialog(isCopy: Boolean) {
    isCopyOperation = isCopy
    _showMoveCopyDialog.value = true
  }

  fun dismissMoveCopyDialog() {
    _showMoveCopyDialog.value = false
  }

  fun executeMoveCopy(targetFolder: String, targetStorage: StorageLocation) {
    val ids = if (isSelectionMode) {
      _selectedMediaIds.value
    } else {
      _currentViewerMediaId.value?.let { setOf(it) } ?: emptySet()
    }

    if (ids.isNotEmpty()) {
      if (isCopyOperation) {
        repository.copyToFolder(ids, targetFolder, targetStorage)
      } else {
        repository.moveToFolder(ids, targetFolder, targetStorage)
      }
    }
    clearSelection()
    _showMoveCopyDialog.value = false
  }

  // Folder Actions
  fun openNewFolderDialog() {
    _showNewFolderDialog.value = true
  }

  fun dismissNewFolderDialog() {
    _showNewFolderDialog.value = false
  }

  fun createFolder(name: String, storage: StorageLocation) {
    repository.createFolder(name, storage)
    _showNewFolderDialog.value = false
  }

  // Settings & Sync
  fun openSettingsDialog() {
    _showSettingsDialog.value = true
  }

  fun dismissSettingsDialog() {
    _showSettingsDialog.value = false
  }

  fun openMediaInfoSheet() {
    _showMediaInfoSheet.value = true
  }

  fun dismissMediaInfoSheet() {
    _showMediaInfoSheet.value = false
  }

  fun openCleanStorageDialog() {
    _showCleanStorageDialog.value = true
  }

  fun dismissCleanStorageDialog() {
    _showCleanStorageDialog.value = false
  }

  fun triggerNightlyOrganize() {
    repository.runNightlyAutoOrganization()
  }

  fun triggerCloudSync() {
    repository.triggerCloudSync()
  }

  fun simulateArrivals() {
    repository.simulateNewArrivals()
  }

  fun importMediaUris(uris: List<String>) {
    repository.addImportedMedia(uris)
  }

  fun scanDeviceMedia() {
    repository.scanDeviceMedia()
  }
}
