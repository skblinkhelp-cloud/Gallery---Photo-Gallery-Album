package com.sk.gallery.photo.album

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sk.gallery.photo.album.model.MediaItem
import com.sk.gallery.photo.album.model.SmartCategory
import com.sk.gallery.photo.album.ui.GalleryScreen
import com.sk.gallery.photo.album.ui.GalleryViewModel
import com.sk.gallery.photo.album.ui.components.GalleryTopBar
import com.sk.gallery.photo.album.ui.components.MediaInfoBottomSheet
import com.sk.gallery.photo.album.ui.components.MoveCopyDialog
import com.sk.gallery.photo.album.ui.components.MultiSelectActionBar
import com.sk.gallery.photo.album.ui.components.NewFolderDialog
import com.sk.gallery.photo.album.ui.components.SettingsAndSyncDialog
import com.sk.gallery.photo.album.ui.components.StorageCleanerDialog
import com.sk.gallery.photo.album.ui.screens.FoldersTab
import com.sk.gallery.photo.album.ui.screens.OrganizeTab
import com.sk.gallery.photo.album.ui.screens.PhotoEditorScreen
import com.sk.gallery.photo.album.ui.screens.PhotoViewerScreen
import com.sk.gallery.photo.album.ui.screens.PhotosTab
import com.sk.gallery.photo.album.ui.screens.SplashScreen
import com.sk.gallery.photo.album.ui.theme.GalleryTheme
import com.sk.gallery.photo.album.util.AdManager
import com.sk.gallery.photo.album.util.NotificationHelper
import com.sk.gallery.photo.album.util.TestBannerAd

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    NotificationHelper.initChannels(this)
    AdManager.initialize(this)

    setContent {
      val viewModel: GalleryViewModel = viewModel()
      val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
      var showSplash by remember { mutableStateOf(true) }

      GalleryTheme(themeMode = themeMode) {
        AnimatedContent(
          targetState = showSplash,
          transitionSpec = { fadeIn() togetherWith fadeOut() },
          label = "splash_transition"
        ) { isSplash ->
          if (isSplash) {
            SplashScreen(onSplashFinished = { showSplash = false })
          } else {
            GalleryApp(viewModel = viewModel)
          }
        }
      }
    }
  }
}

@Composable
fun GalleryApp(viewModel: GalleryViewModel) {
  val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
  val mediaItems by viewModel.mediaItems.collectAsStateWithLifecycle()
  val filteredMediaItems by viewModel.filteredMediaItems.collectAsStateWithLifecycle()
  val folders by viewModel.folders.collectAsStateWithLifecycle()
  val storageStats by viewModel.storageStats.collectAsStateWithLifecycle()
  val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val selectedMediaIds by viewModel.selectedMediaIds.collectAsStateWithLifecycle()
  val currentViewerMediaId by viewModel.currentViewerMediaId.collectAsStateWithLifecycle()
  val gridColumns by viewModel.gridColumnCount.collectAsStateWithLifecycle()
  val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
  val folderStorageFilter by viewModel.folderStorageFilter.collectAsStateWithLifecycle()
  val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
  val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()
  val isAutoOrganizing by viewModel.isAutoOrganizing.collectAsStateWithLifecycle()
  val lastOrganizedInfo by viewModel.lastOrganizedInfo.collectAsStateWithLifecycle()
  val showOrganizeBanner by viewModel.showOrganizeBanner.collectAsStateWithLifecycle()
  val isScanningDevice by viewModel.isScanningDevice.collectAsStateWithLifecycle()

  // Dialog States
  val showNewFolderDialog by viewModel.showNewFolderDialog.collectAsStateWithLifecycle()
  val showMoveCopyDialog by viewModel.showMoveCopyDialog.collectAsStateWithLifecycle()
  val showSettingsDialog by viewModel.showSettingsDialog.collectAsStateWithLifecycle()
  val showMediaInfoSheet by viewModel.showMediaInfoSheet.collectAsStateWithLifecycle()
  val showCleanStorageDialog by viewModel.showCleanStorageDialog.collectAsStateWithLifecycle()

  val context = LocalContext.current
  val activity = context as? Activity

  // Notification Permission Request (Android 13+)
  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { _ -> }

  val requestNotificationPermissionIfNeeded = {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    }
  }

  // Media permissions for physical device photos & screenshots
  val mediaPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
    arrayOf(
      Manifest.permission.READ_MEDIA_IMAGES,
      Manifest.permission.READ_MEDIA_VIDEO,
      Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    )
  } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    arrayOf(
      Manifest.permission.READ_MEDIA_IMAGES,
      Manifest.permission.READ_MEDIA_VIDEO
    )
  } else {
    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
  }

  val mediaPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissionsMap ->
    val anyGranted = permissionsMap.values.any { it }
    if (anyGranted) {
      viewModel.scanDeviceMedia()
    }
    requestNotificationPermissionIfNeeded()
  }

  LaunchedEffect(Unit) {
    val anyGranted = mediaPermissions.any { perm ->
      ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
    }

    if (anyGranted) {
      viewModel.scanDeviceMedia()
      requestNotificationPermissionIfNeeded()
    } else {
      mediaPermissionLauncher.launch(mediaPermissions)
    }
  }

  // Zero-Permission Android Photo Picker for importing media
  val pickMediaLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia()
  ) { uris ->
    if (uris.isNotEmpty()) {
      viewModel.importMediaUris(uris.map { it.toString() })
    }
  }

  val onImportMedia = {
    pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
  }

  // Active viewer/editor media item
  val currentViewerItem = remember(currentViewerMediaId, mediaItems) {
    mediaItems.find { it.id == currentViewerMediaId }
  }

  // Back handling for screens
  if ((currentScreen != GalleryScreen.PHOTOS) && (currentScreen != GalleryScreen.VIEWER) && (currentScreen != GalleryScreen.EDITOR)) {
    BackHandler {
      viewModel.setScreen(GalleryScreen.PHOTOS)
    }
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val isExpanded = maxWidth >= 600.dp

    when (currentScreen) {
      GalleryScreen.VIEWER -> {
        PhotoViewerScreen(
          mediaItem = currentViewerItem,
          onBack = { viewModel.closeViewer() },
          onNext = { viewModel.nextViewerMedia() },
          onPrevious = { viewModel.previousViewerMedia() },
          onToggleFavorite = { viewModel.toggleFavorite(it) },
          onAutoEnhance = { viewModel.applyAutoEnhance(it) },
          onOpenEditor = { viewModel.openEditor(it) },
          onMoveToFolder = { viewModel.openMoveCopyDialog(isCopy = false) },
          onDelete = { viewModel.deleteViewerMedia(it) },
          onOpenInfo = { viewModel.openMediaInfoSheet() }
        )
      }

      GalleryScreen.EDITOR -> {
        PhotoEditorScreen(
          mediaItem = currentViewerItem,
          onBack = { viewModel.closeEditor() },
          onSave = { updated ->
            viewModel.repository.updateMedia(updated)
            viewModel.closeEditor()
          },
          onSaveCopy = { copy ->
            viewModel.repository.updateMedia(copy)
            viewModel.closeEditor()
          }
        )
      }

      else -> {
        // Main App Layout (Photos, Folders, Organize)
        Scaffold(
          modifier = Modifier.fillMaxSize(),
          topBar = {
            GalleryTopBar(
              searchQuery = searchQuery,
              onSearchQueryChange = { viewModel.setSearchQuery(it) },
              themeMode = themeMode,
              onToggleTheme = { viewModel.toggleThemeMode() },
              gridColumns = gridColumns,
              onToggleGridDensity = { viewModel.toggleGridDensity() },
              isSyncing = isSyncing,
              onSyncClick = { viewModel.triggerCloudSync() },
              onImportClick = onImportMedia,
              onSettingsClick = { viewModel.openSettingsDialog() }
            )
          },
          bottomBar = {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
            ) {
              // Testing AdMob Banner Ad
              TestBannerAd()

              if (viewModel.isSelectionMode) {
                MultiSelectActionBar(
                  selectedCount = selectedMediaIds.size,
                  totalCount = filteredMediaItems.size,
                  onClearSelection = { viewModel.clearSelection() },
                  onSelectAll = { viewModel.selectAll() },
                  onAutoEnhanceBatch = { viewModel.applyBatchAutoEnhance() },
                  onMoveToFolder = { viewModel.openMoveCopyDialog(isCopy = false) },
                  onCopyToFolder = { viewModel.openMoveCopyDialog(isCopy = true) },
                  onDeleteBatch = { viewModel.deleteSelectedMedia() },
                  onShareBatch = { viewModel.clearSelection() }
                )
              } else if (!isExpanded) {
                // Standard M3 Navigation Bar for Mobile
                NavigationBar(modifier = Modifier.testTag("gallery_navigation_bar")) {
                  NavigationBarItem(
                    selected = currentScreen == GalleryScreen.PHOTOS,
                    onClick = { viewModel.setScreen(GalleryScreen.PHOTOS) },
                    icon = {
                      Icon(
                        imageVector = if (currentScreen == GalleryScreen.PHOTOS) Icons.Filled.PhotoLibrary else Icons.Outlined.PhotoLibrary,
                        contentDescription = "Photos"
                      )
                    },
                    label = { Text("Photos") },
                    modifier = Modifier.testTag("nav_item_photos")
                  )
                  NavigationBarItem(
                    selected = currentScreen == GalleryScreen.FOLDERS,
                    onClick = { viewModel.setScreen(GalleryScreen.FOLDERS) },
                    icon = {
                      Icon(
                        imageVector = if (currentScreen == GalleryScreen.FOLDERS) Icons.Filled.Folder else Icons.Outlined.Folder,
                        contentDescription = "Folders"
                      )
                    },
                    label = { Text("Folders") },
                    modifier = Modifier.testTag("nav_item_folders")
                  )
                  NavigationBarItem(
                    selected = currentScreen == GalleryScreen.ORGANIZE,
                    onClick = { viewModel.setScreen(GalleryScreen.ORGANIZE) },
                    icon = {
                      Icon(
                        imageVector = if (currentScreen == GalleryScreen.ORGANIZE) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                        contentDescription = "Organize"
                      )
                    },
                    label = { Text("Organize") },
                    modifier = Modifier.testTag("nav_item_organize")
                  )
                }
              }
            }
          }
        ) { innerPadding ->
          Row(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
          ) {
            // Adaptive Navigation Rail for Tablets / Wide screens
            if (isExpanded && !viewModel.isSelectionMode) {
              NavigationRail(modifier = Modifier.testTag("gallery_navigation_rail")) {
                NavigationRailItem(
                  selected = currentScreen == GalleryScreen.PHOTOS,
                  onClick = { viewModel.setScreen(GalleryScreen.PHOTOS) },
                  icon = {
                    Icon(
                      imageVector = if (currentScreen == GalleryScreen.PHOTOS) Icons.Filled.PhotoLibrary else Icons.Outlined.PhotoLibrary,
                      contentDescription = "Photos"
                    )
                  },
                  label = { Text("Photos") }
                )
                NavigationRailItem(
                  selected = currentScreen == GalleryScreen.FOLDERS,
                  onClick = { viewModel.setScreen(GalleryScreen.FOLDERS) },
                  icon = {
                    Icon(
                      imageVector = if (currentScreen == GalleryScreen.FOLDERS) Icons.Filled.Folder else Icons.Outlined.Folder,
                      contentDescription = "Folders"
                    )
                  },
                  label = { Text("Folders") }
                )
                NavigationRailItem(
                  selected = currentScreen == GalleryScreen.ORGANIZE,
                  onClick = { viewModel.setScreen(GalleryScreen.ORGANIZE) },
                  icon = {
                    Icon(
                      imageVector = if (currentScreen == GalleryScreen.ORGANIZE) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                      contentDescription = "Organize"
                    )
                  },
                  label = { Text("Organize") }
                )
              }
            }

            // Screen Content
            AnimatedContent(
              targetState = currentScreen,
              transitionSpec = { fadeIn() togetherWith fadeOut() },
              label = "screen_content",
              modifier = Modifier.weight(1f)
            ) { targetScreen ->
              when (targetScreen) {
                GalleryScreen.PHOTOS -> {
                  PhotosTab(
                    mediaItems = filteredMediaItems,
                    allMediaItems = mediaItems,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { viewModel.selectCategory(it) },
                    selectedMediaIds = selectedMediaIds,
                    isSelectionMode = viewModel.isSelectionMode,
                    gridColumns = gridColumns,
                    isAutoOrganizing = isAutoOrganizing,
                    lastOrganizedInfo = lastOrganizedInfo,
                    onRunAutoOrganize = { viewModel.triggerNightlyOrganize() },
                    showOrganizeBanner = showOrganizeBanner,
                    onDismissOrganizeBanner = { viewModel.dismissOrganizeBanner() },
                    onMediaClick = { item ->
                      if (viewModel.isSelectionMode) {
                        viewModel.toggleSelection(item.id)
                      } else {
                        activity?.let {
                          AdManager.showInterstitialIfReady(it) {
                            viewModel.openViewer(item.id)
                          }
                        } ?: viewModel.openViewer(item.id)
                      }
                    },
                    onMediaLongClick = { item ->
                      viewModel.toggleSelection(item.id)
                    },
                    isScanningDevice = isScanningDevice,
                    onScanDevice = { viewModel.scanDeviceMedia() }
                  )
                }

                GalleryScreen.FOLDERS -> {
                  FoldersTab(
                    folders = folders,
                    storageStats = storageStats,
                    selectedStorageFilter = folderStorageFilter,
                    onSelectStorageFilter = { viewModel.setFolderStorageFilter(it) },
                    onFolderClick = { folder ->
                      viewModel.selectCategory(SmartCategory.ALL)
                      viewModel.setSearchQuery(folder.name)
                      viewModel.setScreen(GalleryScreen.PHOTOS)
                    },
                    onCreateFolderClick = { viewModel.openNewFolderDialog() },
                    onCleanStorageClick = { viewModel.openCleanStorageDialog() }
                  )
                }

                GalleryScreen.ORGANIZE -> {
                  OrganizeTab(
                    allMedia = mediaItems,
                    isAutoOrganizing = isAutoOrganizing,
                    lastOrganizedInfo = lastOrganizedInfo,
                    onRunAutoOrganize = { viewModel.triggerNightlyOrganize() },
                    onSelectCategory = { category ->
                      viewModel.selectCategory(category)
                      viewModel.setScreen(GalleryScreen.PHOTOS)
                    }
                  )
                }

                else -> {}
              }
            }
          }
        }
      }
    }

    // Dialogs & Sheets
    if (showNewFolderDialog) {
      NewFolderDialog(
        onDismiss = { viewModel.dismissNewFolderDialog() },
        onConfirm = { name, storage -> viewModel.createFolder(name, storage) }
      )
    }

    if (showMoveCopyDialog) {
      MoveCopyDialog(
        folders = folders,
        isCopy = viewModel.isCopyOperation,
        onDismiss = { viewModel.dismissMoveCopyDialog() },
        onConfirm = { folder, storage -> viewModel.executeMoveCopy(folder, storage) }
      )
    }

    if (showCleanStorageDialog) {
      StorageCleanerDialog(
        onDismiss = { viewModel.dismissCleanStorageDialog() },
        onCleanSuccess = { viewModel.triggerCloudSync() }
      )
    }

    if (showSettingsDialog) {
      SettingsAndSyncDialog(
        themeMode = themeMode,
        onThemeModeChange = { viewModel.setThemeMode(it) },
        isSyncing = isSyncing,
        lastSyncTime = lastSyncTime,
        onTriggerSync = { viewModel.triggerCloudSync() },
        onSimulateArrivals = { viewModel.simulateArrivals() },
        onDismiss = { viewModel.dismissSettingsDialog() }
      )
    }

    if (showMediaInfoSheet) {
      MediaInfoBottomSheet(
        item = currentViewerItem,
        onDismiss = { viewModel.dismissMediaInfoSheet() }
      )
    }
  }
}
