package com.sk.gallery.photo.album.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sk.gallery.photo.album.model.MediaItem
import com.sk.gallery.photo.album.model.SmartCategory
import com.sk.gallery.photo.album.ui.components.CategoryFilterRow
import com.sk.gallery.photo.album.ui.components.MediaThumbnail
import com.sk.gallery.photo.album.ui.theme.EnhanceGold

@Composable
fun PhotosTab(
  mediaItems: List<MediaItem>,
  allMediaItems: List<MediaItem>,
  selectedCategory: SmartCategory,
  onCategorySelected: (SmartCategory) -> Unit,
  selectedMediaIds: Set<String>,
  isSelectionMode: Boolean,
  gridColumns: Int,
  isAutoOrganizing: Boolean,
  lastOrganizedInfo: String,
  onRunAutoOrganize: () -> Unit,
  showOrganizeBanner: Boolean,
  onDismissOrganizeBanner: () -> Unit,
  onMediaClick: (MediaItem) -> Unit,
  onMediaLongClick: (MediaItem) -> Unit,
  isScanningDevice: Boolean = false,
  onScanDevice: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  // Group media by timeline header
  val groupedMedia = remember(mediaItems) {
    mediaItems.groupBy { it.timelineGroup }
  }

  Column(modifier = modifier.fillMaxSize()) {
    // Smart Categories Chip Row
    CategoryFilterRow(
      selectedCategory = selectedCategory,
      onCategorySelected = onCategorySelected,
      allItems = allMediaItems
    )

    // Nightly Auto-Organization Info Banner
    AnimatedVisibility(
      visible = showOrganizeBanner,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
          .padding(12.dp)
          .testTag("auto_organize_banner")
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(EnhanceGold.copy(alpha = 0.25f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = EnhanceGold,
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Smart Auto-Organization",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
              Text(
                text = lastOrganizedInfo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
              )
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            FilledTonalButton(
              onClick = onRunAutoOrganize,
              enabled = !isAutoOrganizing,
              colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
              ),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              modifier = Modifier.testTag("run_organize_button")
            ) {
              if (isAutoOrganizing) {
                CircularProgressIndicator(
                  modifier = Modifier.size(14.dp),
                  strokeWidth = 2.dp,
                  color = MaterialTheme.colorScheme.onPrimary
                )
              } else {
                Icon(
                  imageVector = Icons.Default.Refresh,
                  contentDescription = "Re-organize",
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Organize", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
            IconButton(
              onClick = onDismissOrganizeBanner,
              modifier = Modifier.size(24.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss",
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }
    }

    if (mediaItems.isEmpty()) {
      // Empty state
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
          )
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = "No Photos Found",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Try clearing filters, searching different keywords, or scan device photos.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(16.dp))
          FilledTonalButton(
            onClick = onScanDevice,
            enabled = !isScanningDevice,
            modifier = Modifier.testTag("scan_device_button")
          ) {
            if (isScanningDevice) {
              CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Scanning device...")
            } else {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Scan Device Photos & Screenshots")
            }
          }
        }
      }
    } else {
      // Timeline Photos Grid
      LazyVerticalGrid(
        columns = GridCells.Fixed(gridColumns),
        modifier = Modifier
          .fillMaxSize()
          .testTag("photos_grid"),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 96.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        groupedMedia.forEach { (timelineHeader, itemsInGroup) ->
          // Sticky date timeline header
          item(span = { GridItemSpan(gridColumns) }) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp, bottom = 6.dp, start = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = timelineHeader,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "${itemsInGroup.size} items",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          items(itemsInGroup, key = { it.id }) { item ->
            val isSelected = item.id in selectedMediaIds
            MediaThumbnail(
              item = item,
              isSelected = isSelected,
              isSelectionMode = isSelectionMode,
              onClick = { onMediaClick(item) },
              onLongClick = { onMediaLongClick(item) }
            )
          }
        }
      }
    }
  }
}
