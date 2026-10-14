package com.aistudio.gallery.pxkq.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aistudio.gallery.pxkq.model.MediaItem
import com.aistudio.gallery.pxkq.model.MediaType
import com.aistudio.gallery.pxkq.model.StorageLocation
import com.aistudio.gallery.pxkq.ui.theme.EnhanceGold
import com.aistudio.gallery.pxkq.ui.theme.SdCardTeal
import com.aistudio.gallery.pxkq.util.ColorFilterHelper

@Composable
fun PhotoViewerScreen(
  mediaItem: MediaItem?,
  onBack: () -> Unit,
  onNext: () -> Unit,
  onPrevious: () -> Unit,
  onToggleFavorite: (String) -> Unit,
  onAutoEnhance: (String) -> Unit,
  onOpenEditor: (String) -> Unit,
  onMoveToFolder: () -> Unit,
  onDelete: (String) -> Unit,
  onOpenInfo: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onBack() }

  if (mediaItem == null) {
    onBack()
    return
  }

  val context = LocalContext.current
  var isControlsVisible by remember { mutableStateOf(true) }

  // Gesture zoom & pan state
  var scale by remember { mutableFloatStateOf(1f) }
  var offsetX by remember { mutableFloatStateOf(0f) }
  var offsetY by remember { mutableFloatStateOf(0f) }

  val colorFilter = ColorFilterHelper.getColorFilterForMedia(mediaItem)
  val isVideo = mediaItem.mediaType == MediaType.VIDEO

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black)
      .testTag("photo_viewer_screen")
  ) {
    // Zoomable Image or Native Video Player
    Box(
      modifier = Modifier
        .fillMaxSize()
        .then(
          if (!isVideo) {
            Modifier
              .pointerInput(mediaItem.id) {
                detectTapGestures(
                  onDoubleTap = {
                    if (scale > 1f) {
                      scale = 1f
                      offsetX = 0f
                      offsetY = 0f
                    } else {
                      scale = 2.5f
                    }
                  },
                  onTap = {
                    isControlsVisible = !isControlsVisible
                  }
                )
              }
              .pointerInput(mediaItem.id) {
                detectTransformGestures { _, pan, zoom, _ ->
                  val newScale = (scale * zoom).coerceIn(1f, 5f)
                  scale = newScale
                  if (newScale > 1.05f) {
                    val maxX = (newScale - 1f) * 600f
                    val maxY = (newScale - 1f) * 800f
                    offsetX = (offsetX + pan.x).coerceIn(-maxX, maxX)
                    offsetY = (offsetY + pan.y).coerceIn(-maxY, maxY)
                  } else {
                    scale = 1f
                    offsetX = 0f
                    offsetY = 0f
                  }
                }
              }
          } else Modifier
        ),
      contentAlignment = Alignment.Center
    ) {
      if (isVideo) {
        val videoUri = when {
          mediaItem.customUri != null -> Uri.parse(mediaItem.customUri)
          mediaItem.drawableResId != null -> Uri.parse("android.resource://${context.packageName}/${mediaItem.drawableResId}")
          else -> null
        }

        if (videoUri != null) {
          VideoPlayer(
            uri = videoUri,
            modifier = Modifier.fillMaxSize()
          )
        }
      } else {
        val imageModifier = if (mediaItem.cropAspectRatio.ratio != null) {
          Modifier
            .aspectRatio(mediaItem.cropAspectRatio.ratio!!)
            .clip(RoundedCornerShape(8.dp))
        } else {
          Modifier.fillMaxSize()
        }

        Box(
          modifier = imageModifier,
          contentAlignment = Alignment.Center
        ) {
          if (mediaItem.customUri != null) {
            AsyncImage(
              model = ImageRequest.Builder(context)
                .data(mediaItem.customUri)
                .crossfade(true)
                .build(),
              contentDescription = mediaItem.title,
              contentScale = if (mediaItem.cropAspectRatio.ratio != null) ContentScale.Crop else ContentScale.Fit,
              colorFilter = colorFilter,
              modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                  scaleX = scale,
                  scaleY = scale,
                  translationX = offsetX,
                  translationY = offsetY,
                  rotationZ = mediaItem.rotationDegrees.toFloat()
                )
            )
          } else if (mediaItem.drawableResId != null) {
            Image(
              painter = painterResource(id = mediaItem.drawableResId),
              contentDescription = mediaItem.title,
              contentScale = if (mediaItem.cropAspectRatio.ratio != null) ContentScale.Crop else ContentScale.Fit,
              colorFilter = colorFilter,
              modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                  scaleX = scale,
                  scaleY = scale,
                  translationX = offsetX,
                  translationY = offsetY,
                  rotationZ = mediaItem.rotationDegrees.toFloat()
                )
            )
          }
        }
      }
    }

    // Top Bar (Animated Visibility)
    AnimatedVisibility(
      visible = isControlsVisible,
      enter = fadeIn() + slideInVertically { -it },
      exit = fadeOut() + slideOutVertically { -it },
      modifier = Modifier.align(Alignment.TopCenter)
    ) {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.65f)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = onBack,
              modifier = Modifier.testTag("viewer_back_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
              )
            }
            Column {
              Text(
                text = mediaItem.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 1
              )
              Text(
                text = "${mediaItem.dateFormatted} • ${mediaItem.folderName}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
              )
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            // Favorite toggle
            IconButton(
              onClick = { onToggleFavorite(mediaItem.id) },
              modifier = Modifier.testTag("viewer_favorite_button")
            ) {
              Icon(
                imageVector = if (mediaItem.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (mediaItem.isFavorite) Color.Red else Color.White
              )
            }

            // Info details sheet
            IconButton(
              onClick = onOpenInfo,
              modifier = Modifier.testTag("viewer_info_button")
            ) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Details",
                tint = Color.White
              )
            }
          }
        }
      }
    }

    // Left Navigation Arrow (Previous)
    AnimatedVisibility(
      visible = isControlsVisible,
      enter = fadeIn(),
      exit = fadeOut(),
      modifier = Modifier
        .align(Alignment.CenterStart)
        .padding(start = 12.dp)
    ) {
      IconButton(
        onClick = onPrevious,
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = 0.55f))
          .testTag("viewer_prev_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Previous photo",
          tint = Color.White
        )
      }
    }

    // Right Navigation Arrow (Next)
    AnimatedVisibility(
      visible = isControlsVisible,
      enter = fadeIn(),
      exit = fadeOut(),
      modifier = Modifier
        .align(Alignment.CenterEnd)
        .padding(end = 12.dp)
    ) {
      IconButton(
        onClick = onNext,
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = 0.55f))
          .testTag("viewer_next_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = "Next photo",
          tint = Color.White
        )
      }
    }

    // Bottom Action Bar (Animated Visibility)
    AnimatedVisibility(
      visible = isControlsVisible,
      enter = fadeIn() + slideInVertically { it },
      exit = fadeOut() + slideOutVertically { it },
      modifier = Modifier.align(Alignment.BottomCenter)
    ) {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.75f)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
          // Status chips (e.g. Storage Location, Auto-Enhanced status)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              val isSd = mediaItem.storageLocation == StorageLocation.SD_CARD
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (isSd) SdCardTeal else Color.White.copy(alpha = 0.2f))
                  .padding(horizontal = 8.dp, vertical = 3.dp)
              ) {
                Text(
                  text = if (isSd) "SD Card Storage" else "Internal Storage",
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }

              if (mediaItem.autoEnhanced) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(EnhanceGold.copy(alpha = 0.3f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.AutoAwesome,
                      contentDescription = null,
                      tint = EnhanceGold,
                      modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "Auto-Enhanced",
                      color = EnhanceGold,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }
            }

            Text(
              text = mediaItem.formattedSize,
              color = Color.White.copy(alpha = 0.7f),
              fontSize = 12.sp
            )
          }

          // Main Action Row
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Share
            ViewerActionItem(
              icon = Icons.Default.Share,
              label = "Share",
              onClick = {
                val videoUri = when {
                  mediaItem.customUri != null -> Uri.parse(mediaItem.customUri)
                  else -> null
                }
                val sendIntent = Intent().apply {
                  action = Intent.ACTION_SEND
                  if (videoUri != null) {
                    putExtra(Intent.EXTRA_STREAM, videoUri)
                    type = if (isVideo) "video/*" else "image/*"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                  } else {
                    putExtra(Intent.EXTRA_TEXT, "Sharing item: ${mediaItem.title}")
                    type = "text/plain"
                  }
                }
                context.startActivity(Intent.createChooser(sendIntent, "Share Media"))
              },
              modifier = Modifier.testTag("viewer_share_button")
            )

            if (isVideo) {
              // Open in External System Video Player
              ViewerActionItem(
                icon = Icons.Default.PlayArrow,
                label = "System Player",
                tint = EnhanceGold,
                onClick = {
                  val videoUri = when {
                    mediaItem.customUri != null -> Uri.parse(mediaItem.customUri)
                    mediaItem.drawableResId != null -> Uri.parse("android.resource://${context.packageName}/${mediaItem.drawableResId}")
                    else -> null
                  }
                  if (videoUri != null) {
                    val playIntent = Intent(Intent.ACTION_VIEW).apply {
                      setDataAndType(videoUri, "video/*")
                      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    try {
                      context.startActivity(playIntent)
                    } catch (e: Exception) {
                      Toast.makeText(context, "No external video player found", Toast.LENGTH_SHORT).show()
                    }
                  }
                },
                modifier = Modifier.testTag("viewer_external_player")
              )
            } else {
              // 1-Tap Auto-Enhance Button (Photos)
              ViewerActionItem(
                icon = Icons.Default.AutoAwesome,
                label = if (mediaItem.autoEnhanced) "Enhanced" else "Auto-Enhance",
                tint = if (mediaItem.autoEnhanced) EnhanceGold else Color.White,
                onClick = { onAutoEnhance(mediaItem.id) },
                modifier = Modifier.testTag("viewer_one_tap_enhance")
              )

              // Edit suite
              ViewerActionItem(
                icon = Icons.Default.Edit,
                label = "Edit",
                onClick = { onOpenEditor(mediaItem.id) },
                modifier = Modifier.testTag("viewer_edit_button")
              )
            }

            // Move / Transfer
            ViewerActionItem(
              icon = Icons.AutoMirrored.Filled.DriveFileMove,
              label = "Move",
              onClick = onMoveToFolder,
              modifier = Modifier.testTag("viewer_move_button")
            )

            // Delete
            ViewerActionItem(
              icon = Icons.Default.Delete,
              label = "Delete",
              tint = MaterialTheme.colorScheme.error,
              onClick = { onDelete(mediaItem.id) },
              modifier = Modifier.testTag("viewer_delete_button")
            )
          }
        }
      }
    }
  }
}

@Composable
fun VideoPlayer(
  uri: Uri,
  modifier: Modifier = Modifier
) {
  var videoViewRef by remember { mutableStateOf<VideoView?>(null) }

  DisposableEffect(uri) {
    onDispose {
      videoViewRef?.stopPlayback()
    }
  }

  AndroidView(
    factory = { ctx ->
      VideoView(ctx).apply {
        videoViewRef = this
        setVideoURI(uri)
        val controller = MediaController(ctx)
        controller.setAnchorView(this)
        setMediaController(controller)
        setOnPreparedListener { mp ->
          mp.isLooping = true
          start()
        }
        setOnErrorListener { _, _, _ ->
          true
        }
      }
    },
    update = { view ->
      videoViewRef = view
    },
    modifier = modifier
  )
}

@Composable
private fun ViewerActionItem(
  icon: ImageVector,
  label: String,
  tint: Color = Color.White,
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
      color = tint,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      maxLines = 1,
      softWrap = false
    )
  }
}
