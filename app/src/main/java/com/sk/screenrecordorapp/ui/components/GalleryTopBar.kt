package com.sk.screenrecordorapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sk.screenrecordorapp.model.MediaItem
import com.sk.screenrecordorapp.model.SmartCategory
import com.sk.screenrecordorapp.ui.theme.EnhanceGold
import com.sk.screenrecordorapp.ui.theme.ThemeMode

@Composable
fun GalleryTopBar(
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  themeMode: ThemeMode,
  onToggleTheme: () -> Unit,
  gridColumns: Int,
  onToggleGridDensity: () -> Unit,
  isSyncing: Boolean,
  onSyncClick: () -> Unit,
  onImportClick: () -> Unit,
  onSettingsClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // App Title & Logo Branding
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.testTag("app_logo_title")
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = "Gallery",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Gallery",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
              ),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Smart & Offline",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Action icons: Cloud Sync, Grid, Theme, Import, Settings
        Row(verticalAlignment = Alignment.CenterVertically) {
          // Cloud Sync button / status
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(
                if (isSyncing) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
              )
              .clickable(onClick = onSyncClick)
              .padding(horizontal = 10.dp, vertical = 6.dp)
              .testTag("cloud_sync_button"),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              if (isSyncing) {
                CircularProgressIndicator(
                  modifier = Modifier.size(16.dp),
                  strokeWidth = 2.dp,
                  color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Syncing",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.SemiBold
                )
              } else {
                Icon(
                  imageVector = Icons.Default.CloudDone,
                  contentDescription = "Cloud Synced",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Synced",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          Spacer(modifier = Modifier.width(4.dp))

          // Add / Import Media
          IconButton(
            onClick = onImportClick,
            modifier = Modifier
              .size(36.dp)
              .testTag("import_media_button")
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Import photos",
              tint = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(20.dp)
            )
          }

          // Grid density toggle
          IconButton(
            onClick = onToggleGridDensity,
            modifier = Modifier
              .size(36.dp)
              .testTag("grid_density_toggle")
          ) {
            Icon(
              imageVector = Icons.Default.GridView,
              contentDescription = "Toggle Grid (${gridColumns} columns)",
              tint = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(20.dp)
            )
          }

          // Theme toggle
          IconButton(
            onClick = onToggleTheme,
            modifier = Modifier
              .size(36.dp)
              .testTag("dark_mode_toggle")
          ) {
            Icon(
              imageVector = when (themeMode) {
                ThemeMode.DARK -> Icons.Default.LightMode
                ThemeMode.LIGHT -> Icons.Default.DarkMode
                ThemeMode.SYSTEM -> Icons.Default.DarkMode
              },
              contentDescription = "Toggle Dark Mode",
              tint = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(20.dp)
            )
          }

          // Settings
          IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
              .size(36.dp)
              .testTag("settings_button")
          ) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = "Gallery Settings",
              tint = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Search bar
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("gallery_search_input"),
        placeholder = {
          Text(
            text = "Search people, places, nature, documents...",
            style = MaterialTheme.typography.bodyMedium
          )
        },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        },
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
          unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
          focusedBorderColor = MaterialTheme.colorScheme.primary,
          unfocusedBorderColor = Color.Transparent
        )
      )
    }
  }
}

@Composable
fun CategoryFilterRow(
  selectedCategory: SmartCategory,
  onCategorySelected: (SmartCategory) -> Unit,
  allItems: List<MediaItem>,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()

  Row(
    modifier = modifier
      .fillMaxWidth()
      .horizontalScroll(scrollState)
      .padding(horizontal = 16.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    SmartCategory.entries.forEach { category ->
      val isSelected = category == selectedCategory
      val count = if (category == SmartCategory.ALL) {
        allItems.size
      } else {
        allItems.count { it.category == category }
      }

      val backgroundColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        label = "chip_bg"
      )

      val contentColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "chip_text"
      )

      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .background(backgroundColor)
          .clickable { onCategorySelected(category) }
          .padding(horizontal = 14.dp, vertical = 8.dp)
          .testTag("category_chip_${category.name.lowercase()}"),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = category.emoji, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = category.displayName,
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
          ),
          color = contentColor
        )
        if (count > 0) {
          Spacer(modifier = Modifier.width(6.dp))
          Box(
            modifier = Modifier
              .clip(CircleShape)
              .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
              )
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "$count",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
              color = contentColor,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}
