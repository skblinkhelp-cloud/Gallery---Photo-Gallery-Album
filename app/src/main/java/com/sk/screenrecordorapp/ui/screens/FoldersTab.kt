package com.sk.screenrecordorapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.sk.screenrecordorapp.R
import com.sk.screenrecordorapp.model.FolderItem
import com.sk.screenrecordorapp.model.StorageLocation
import com.sk.screenrecordorapp.model.StorageStats
import com.sk.screenrecordorapp.ui.theme.SdCardTeal

@Composable
fun FoldersTab(
  folders: List<FolderItem>,
  storageStats: StorageStats,
  selectedStorageFilter: StorageLocation?,
  onSelectStorageFilter: (StorageLocation?) -> Unit,
  onFolderClick: (FolderItem) -> Unit,
  onCreateFolderClick: () -> Unit,
  onCleanStorageClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  val filteredFolders = folders.filter { folder ->
    selectedStorageFilter == null || folder.storageLocation == selectedStorageFilter
  }

  LazyVerticalGrid(
    columns = GridCells.Fixed(2),
    modifier = modifier
      .fillMaxSize()
      .testTag("folders_grid"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Storage Overview Card
    item(span = { GridItemSpan(2) }) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("storage_overview_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Storage & SD Card",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(12.dp))

          // Internal Storage Meter
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.PhoneAndroid,
                contentDescription = "Internal Storage",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Internal Storage",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
              )
            }
            Text(
              text = "${storageStats.internalUsedGb} GB / ${storageStats.internalTotalGb} GB",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          LinearProgressIndicator(
            progress = { storageStats.internalPercent },
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surface
          )

          Spacer(modifier = Modifier.height(16.dp))

          // SD Card Meter
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.SdCard,
                contentDescription = "SD Card",
                tint = SdCardTeal,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "SD Card",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(SdCardTeal.copy(alpha = 0.2f))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "Mounted",
                  fontSize = 10.sp,
                  color = SdCardTeal,
                  fontWeight = FontWeight.Bold
                )
              }
            }
            Text(
              text = "${storageStats.sdCardUsedGb} GB / ${storageStats.sdCardTotalGb} GB",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          LinearProgressIndicator(
            progress = { storageStats.sdCardPercent },
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(CircleShape),
            color = SdCardTeal,
            trackColor = MaterialTheme.colorScheme.surface
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Actions row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = onCleanStorageClick,
              modifier = Modifier
                .weight(1f)
                .testTag("clean_storage_button"),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
              )
            ) {
              Icon(
                imageVector = Icons.Default.CleaningServices,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Free Up Space", fontSize = 12.sp)
            }

            OutlinedButton(
              onClick = onCreateFolderClick,
              modifier = Modifier
                .weight(1f)
                .testTag("create_folder_button"),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("New Folder", fontSize = 12.sp)
            }
          }
        }
      }
    }

    // Storage Filter Chips
    item(span = { GridItemSpan(2) }) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        FilterChip(
          selected = selectedStorageFilter == null,
          onClick = { onSelectStorageFilter(null) },
          label = { Text("All Locations") },
          modifier = Modifier.testTag("filter_all_storage")
        )
        FilterChip(
          selected = selectedStorageFilter == StorageLocation.INTERNAL,
          onClick = { onSelectStorageFilter(StorageLocation.INTERNAL) },
          label = { Text("Internal Storage") },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.PhoneAndroid,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
          },
          modifier = Modifier.testTag("filter_internal_storage")
        )
        FilterChip(
          selected = selectedStorageFilter == StorageLocation.SD_CARD,
          onClick = { onSelectStorageFilter(StorageLocation.SD_CARD) },
          label = { Text("SD Card") },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.SdCard,
              contentDescription = null,
              tint = SdCardTeal,
              modifier = Modifier.size(16.dp)
            )
          },
          modifier = Modifier.testTag("filter_sd_card")
        )
      }
    }

    // Folders Grid items
    items(filteredFolders, key = { it.id }) { folder ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onFolderClick(folder) }
          .testTag("folder_card_${folder.name.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column {
          // Folder Cover Image
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(1.2f)
              .background(MaterialTheme.colorScheme.surfaceVariant)
          ) {
            if (folder.coverUri != null || folder.coverResId != null) {
              val isVideo = folder.coverUri?.contains("video", ignoreCase = true) == true
              val imageRequest = ImageRequest.Builder(context)
                .data(folder.coverUri ?: folder.coverResId)
                .apply {
                  if (isVideo) {
                    decoderFactory(VideoFrameDecoder.Factory())
                    videoFrameMillis(1000)
                  }
                }
                .crossfade(true)
                .build()

              AsyncImage(
                model = imageRequest,
                contentDescription = folder.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )
            } else {
              Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                  .size(48.dp)
                  .align(Alignment.Center)
              )
            }

            // Storage badge
            val isSd = folder.storageLocation == StorageLocation.SD_CARD
            Box(
              modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                  if (isSd) SdCardTeal.copy(alpha = 0.9f)
                  else Color.Black.copy(alpha = 0.6f)
                )
                .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
              Text(
                text = if (isSd) "SD Card" else "Internal",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          // Folder Details
          Column(modifier = Modifier.padding(10.dp)) {
            Text(
              text = folder.name,
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "${folder.itemCount} items",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              if (folder.sizeMb > 0) {
                Text(
                  text = String.format("%.1f MB", folder.sizeMb),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    }
  }
}
