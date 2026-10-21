package com.sk.gallery.photo.album.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sk.gallery.photo.album.ui.theme.EnhanceGold

@Composable
fun MultiSelectActionBar(
  selectedCount: Int,
  totalCount: Int,
  onClearSelection: () -> Unit,
  onSelectAll: () -> Unit,
  onAutoEnhanceBatch: () -> Unit,
  onMoveToFolder: () -> Unit,
  onCopyToFolder: () -> Unit,
  onDeleteBatch: () -> Unit,
  onShareBatch: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.surfaceVariant,
    tonalElevation = 8.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onClearSelection,
            modifier = Modifier.testTag("clear_selection_button")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Cancel Selection"
            )
          }
          Text(
            text = "$selectedCount selected",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        TextButton(
          onClick = onSelectAll,
          modifier = Modifier.testTag("select_all_button")
        ) {
          Icon(
            imageVector = Icons.Default.SelectAll,
            contentDescription = "Select All",
            modifier = Modifier.size(18.dp)
          )
          Text(
            text = " Select All",
            style = MaterialTheme.typography.labelLarge
          )
        }
      }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Auto-Enhance Batch
        MultiSelectActionItem(
          icon = Icons.Default.AutoAwesome,
          label = "Enhance",
          tint = EnhanceGold,
          onClick = onAutoEnhanceBatch,
          modifier = Modifier.testTag("batch_enhance_button")
        )

        // Move to Folder / SD
        MultiSelectActionItem(
          icon = Icons.AutoMirrored.Filled.DriveFileMove,
          label = "Move",
          tint = MaterialTheme.colorScheme.primary,
          onClick = onMoveToFolder,
          modifier = Modifier.testTag("batch_move_button")
        )

        // Copy to Folder / SD
        MultiSelectActionItem(
          icon = Icons.Default.ContentCopy,
          label = "Copy",
          tint = MaterialTheme.colorScheme.secondary,
          onClick = onCopyToFolder,
          modifier = Modifier.testTag("batch_copy_button")
        )

        // Share
        MultiSelectActionItem(
          icon = Icons.Default.Share,
          label = "Share",
          tint = MaterialTheme.colorScheme.onSurface,
          onClick = onShareBatch,
          modifier = Modifier.testTag("batch_share_button")
        )

        // Delete
        MultiSelectActionItem(
          icon = Icons.Default.Delete,
          label = "Delete",
          tint = MaterialTheme.colorScheme.error,
          onClick = onDeleteBatch,
          modifier = Modifier.testTag("batch_delete_button")
        )
      }
    }
  }
}

@Composable
private fun MultiSelectActionItem(
  icon: ImageVector,
  label: String,
  tint: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 8.dp, vertical = 6.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = tint,
      modifier = Modifier.size(22.dp)
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      color = tint,
      maxLines = 1,
      softWrap = false
    )
  }
}
