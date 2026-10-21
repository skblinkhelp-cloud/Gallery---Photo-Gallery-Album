package com.sk.gallery.photo.album.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationAdd
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sk.gallery.photo.album.model.FolderItem
import com.sk.gallery.photo.album.model.MediaItem
import com.sk.gallery.photo.album.model.StorageLocation
import com.sk.gallery.photo.album.ui.theme.NatureGreen
import com.sk.gallery.photo.album.ui.theme.SdCardTeal
import com.sk.gallery.photo.album.ui.theme.ThemeMode

@Composable
fun NewFolderDialog(
  onDismiss: () -> Unit,
  onConfirm: (name: String, storage: StorageLocation) -> Unit
) {
  var folderName by remember { mutableStateOf("") }
  var selectedStorage by remember { mutableStateOf(StorageLocation.INTERNAL) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Create New Folder",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = folderName,
          onValueChange = { folderName = it },
          label = { Text("Folder Name") },
          placeholder = { Text("e.g. Vacation 2026") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("new_folder_name_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Select Storage Destination:",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(6.dp))

        // Storage choices
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { selectedStorage = StorageLocation.INTERNAL }
            .padding(vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          RadioButton(
            selected = selectedStorage == StorageLocation.INTERNAL,
            onClick = { selectedStorage = StorageLocation.INTERNAL }
          )
          Spacer(modifier = Modifier.width(6.dp))
          Icon(
            imageVector = Icons.Default.PhoneAndroid,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Internal Storage")
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { selectedStorage = StorageLocation.SD_CARD }
            .padding(vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          RadioButton(
            selected = selectedStorage == StorageLocation.SD_CARD,
            onClick = { selectedStorage = StorageLocation.SD_CARD }
          )
          Spacer(modifier = Modifier.width(6.dp))
          Icon(
            imageVector = Icons.Default.SdCard,
            contentDescription = null,
            tint = SdCardTeal,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("SD Card Storage")
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (folderName.isNotBlank()) {
            onConfirm(folderName.trim(), selectedStorage)
          }
        },
        enabled = folderName.isNotBlank(),
        modifier = Modifier.testTag("confirm_create_folder_button")
      ) {
        Text("Create")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun MoveCopyDialog(
  folders: List<FolderItem>,
  isCopy: Boolean,
  onDismiss: () -> Unit,
  onConfirm: (targetFolder: String, targetStorage: StorageLocation) -> Unit
) {
  var selectedFolder by remember { mutableStateOf(folders.firstOrNull()?.name ?: "Camera") }
  var selectedStorage by remember {
    mutableStateOf(folders.firstOrNull()?.storageLocation ?: StorageLocation.INTERNAL)
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (isCopy) "Copy to Folder" else "Move to Folder",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .height(280.dp)
      ) {
        Text(
          text = "Choose destination folder:",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          items(folders) { folder ->
            val isSelected = (folder.name == selectedFolder && folder.storageLocation == selectedStorage)
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  selectedFolder = folder.name
                  selectedStorage = folder.storageLocation
                },
              colors = CardDefaults.cardColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
              ),
              shape = RoundedCornerShape(10.dp)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = if (folder.storageLocation == StorageLocation.SD_CARD) Icons.Default.SdCard
                  else Icons.Default.Folder,
                  contentDescription = null,
                  tint = if (folder.storageLocation == StorageLocation.SD_CARD) SdCardTeal
                  else MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = folder.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                  )
                  Text(
                    text = if (folder.storageLocation == StorageLocation.SD_CARD) "SD Card" else "Internal",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                if (isSelected) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                  )
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(selectedFolder, selectedStorage) },
        modifier = Modifier.testTag("confirm_move_copy_button")
      ) {
        Text(if (isCopy) "Copy" else "Move")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun StorageCleanerDialog(
  onDismiss: () -> Unit,
  onCleanSuccess: () -> Unit
) {
  var isCleaning by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(
        imageVector = Icons.Default.CleaningServices,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(32.dp)
      )
    },
    title = {
      Text(
        text = "Clean Up & Free Space",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Gallery analyzed your device storage. You can safely free up space by cleaning backed-up media and duplicates.",
          style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(14.dp))

        // Breakdown items
        StorageCleanRow(title = "Cloud Backed-up Photos", size = "850 MB", isCleanable = true)
        StorageCleanRow(title = "Duplicate Shots & Bursts", size = "240 MB", isCleanable = true)
        StorageCleanRow(title = "Thumbnail Cache & Temp Files", size = "150 MB", isCleanable = true)

        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = "Total Space Recoverable:", fontWeight = FontWeight.Bold)
          Text(text = "1.24 GB", fontWeight = FontWeight.Bold, color = NatureGreen)
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          isCleaning = true
          onCleanSuccess()
          onDismiss()
        },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier.testTag("execute_clean_storage_button")
      ) {
        Text("Free Up 1.24 GB")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Not Now")
      }
    }
  )
}

@Composable
private fun StorageCleanRow(title: String, size: String, isCleanable: Boolean) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurface
    )
    Text(
      text = size,
      style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.primary
    )
  }
}

@Composable
fun SettingsAndSyncDialog(
  themeMode: ThemeMode,
  onThemeModeChange: (ThemeMode) -> Unit,
  isSyncing: Boolean,
  lastSyncTime: String,
  onTriggerSync: () -> Unit,
  onSimulateArrivals: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Gallery Settings & Sync",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        // Dark Mode Options
        Text(
          text = "App Theme & Appearance",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          ThemeOptionChip(
            title = "System",
            isSelected = themeMode == ThemeMode.SYSTEM,
            icon = Icons.Default.SettingsBrightness,
            onClick = { onThemeModeChange(ThemeMode.SYSTEM) },
            modifier = Modifier.weight(1f)
          )
          ThemeOptionChip(
            title = "Light",
            isSelected = themeMode == ThemeMode.LIGHT,
            icon = Icons.Default.LightMode,
            onClick = { onThemeModeChange(ThemeMode.LIGHT) },
            modifier = Modifier.weight(1f)
          )
          ThemeOptionChip(
            title = "Dark",
            isSelected = themeMode == ThemeMode.DARK,
            icon = Icons.Default.DarkMode,
            onClick = { onThemeModeChange(ThemeMode.DARK) },
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Cloud Sync Settings
        Text(
          text = "Cloud Sync & Feed",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Last synced: $lastSyncTime",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = onTriggerSync,
            enabled = !isSyncing,
            modifier = Modifier
              .weight(1f)
              .testTag("dialog_sync_now_button")
          ) {
            Icon(
              imageVector = Icons.Default.CloudSync,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Sync Now", fontSize = 11.sp)
          }

          OutlinedButton(
            onClick = onSimulateArrivals,
            modifier = Modifier
              .weight(1f)
              .testTag("dialog_simulate_arrivals_button")
          ) {
            Icon(
              imageVector = Icons.Default.NotificationAdd,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("New Arrivals", fontSize = 11.sp)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Privacy Policy & Developer Info
        var showPrivacyModal by remember { mutableStateOf(false) }

        Text(
          text = "Privacy & Developer",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "• Version: 2.5 (code 8)\n• Developer: sk.blink.help@gmail.com\n• Package: com.sk.gallery.photo.album\n• 100% on-device private processing",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
          onClick = { showPrivacyModal = true },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("view_privacy_policy_button"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("View Full Privacy Policy", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }

        if (showPrivacyModal) {
          PrivacyPolicyDialog(onDismiss = { showPrivacyModal = false })
        }
      }
    },
    confirmButton = {
      Button(onClick = onDismiss) {
        Text("Done")
      }
    }
  )
}

@Composable
private fun ThemeOptionChip(
  title: String,
  isSelected: Boolean,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
    modifier = modifier.clickable(onClick = onClick)
  ) {
    Column(
      modifier = Modifier.padding(vertical = 10.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaInfoBottomSheet(
  item: MediaItem?,
  onDismiss: () -> Unit
) {
  if (item == null) return

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 24.dp, vertical = 12.dp)
        .testTag("media_info_sheet")
    ) {
      Text(
        text = item.title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "${item.dateFormatted} • ${item.location}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(16.dp))

      InfoRow(label = "Category", value = "${item.category.emoji} ${item.category.displayName}")
      InfoRow(label = "Storage", value = if (item.storageLocation == StorageLocation.SD_CARD) "SD Card Storage" else "Internal Storage")
      InfoRow(label = "Folder", value = item.folderName)
      InfoRow(label = "File Size", value = item.formattedSize)
      InfoRow(label = "Dimensions", value = "${item.width} × ${item.height} px")
      InfoRow(label = "Auto-Enhanced", value = if (item.autoEnhanced) "Yes (Smart tone & vibrancy)" else "No")
      InfoRow(label = "Cloud Backup", value = if (item.isCloudSynced) "Backed up to Cloud" else "Local Only")

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun InfoRow(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Privacy Policy",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
      )
    },
    text = {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .height(340.dp)
      ) {
        item {
          Text(
            text = "Effective Date: October 12, 2026\nDeveloper: sk.blink.help@gmail.com\nApplication: Gallery (com.sk.gallery.photo.album)\n\n" +
              "1. 100% On-Device & Offline Architecture\n" +
              "Gallery processes all photos, videos, and organization tasks locally on your device. We do not transmit or upload your photos, videos, or facial biometric data to any external servers or third-party AI models.\n\n" +
              "2. Media & Storage Access\n" +
              "We utilize standard zero-permission Android Photo Picker and standard MediaStore APIs for media access. Broad storage permissions are never requested. Folder management operations between internal storage and SD cards remain strictly local to your device.\n\n" +
              "3. Cloud Sync & Notifications\n" +
              "If you choose to use cloud backup, data travels directly and securely to your cloud account over HTTPS. Push notification permissions are requested solely to notify you of backup completion or local auto-organization results.\n\n" +
              "4. Zero Third-Party Tracking\n" +
              "Gallery contains no third-party advertising SDKs, analytics tracking, or data brokers. We never sell, monetize, or disclose your personal media to third parties.\n\n" +
              "5. Contact Information\n" +
              "For inquiries or questions regarding this Privacy Policy, please email sk.blink.help@gmail.com.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    },
    confirmButton = {
      Button(onClick = onDismiss) {
        Text("Close")
      }
    }
  )
}
