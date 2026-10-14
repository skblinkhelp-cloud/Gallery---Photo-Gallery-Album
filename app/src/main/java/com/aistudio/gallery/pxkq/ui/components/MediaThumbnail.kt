package com.aistudio.gallery.pxkq.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.aistudio.gallery.pxkq.R
import com.aistudio.gallery.pxkq.model.MediaItem
import com.aistudio.gallery.pxkq.model.MediaType
import com.aistudio.gallery.pxkq.model.StorageLocation
import com.aistudio.gallery.pxkq.ui.theme.EnhanceGold
import com.aistudio.gallery.pxkq.ui.theme.SdCardTeal
import com.aistudio.gallery.pxkq.util.ColorFilterHelper

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaThumbnail(
  item: MediaItem,
  isSelected: Boolean,
  isSelectionMode: Boolean,
  onClick: () -> Unit,
  onLongClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val colorFilter = ColorFilterHelper.getColorFilterForMedia(item)

  val borderModifier = if (isSelected) {
    Modifier.border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
  } else {
    Modifier
  }

  val imageRequest = remember(item.customUri, item.drawableResId, item.mediaType) {
    val data = item.customUri ?: item.drawableResId
    ImageRequest.Builder(context)
      .data(data)
      .apply {
        if (item.mediaType == MediaType.VIDEO) {
          decoderFactory(VideoFrameDecoder.Factory())
          videoFrameMillis(1000)
        }
      }
      .crossfade(true)
      .build()
  }

  Box(
    modifier = modifier
      .aspectRatio(1f)
      .clip(RoundedCornerShape(8.dp))
      .then(borderModifier)
      .background(MaterialTheme.colorScheme.surfaceVariant)
      .combinedClickable(
        onClick = onClick,
        onLongClick = onLongClick
      )
      .testTag("media_item_${item.id}")
  ) {
    // Image / Video Frame thumbnail rendering
    if (item.customUri != null || item.drawableResId != null) {
      AsyncImage(
        model = imageRequest,
        contentDescription = item.title,
        contentScale = ContentScale.Crop,
        colorFilter = colorFilter,
        modifier = Modifier.fillMaxSize()
      )
    } else {
      // Fallback
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
      ) {
        Text(text = item.category.emoji, fontSize = 28.sp)
      }
    }

    // Top Row: SD Card badge, Auto-Enhanced badge, Favorite badge
    Row(
      modifier = Modifier
        .align(Alignment.TopStart)
        .padding(6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (item.storageLocation == StorageLocation.SD_CARD) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(SdCardTeal.copy(alpha = 0.9f))
            .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.SdCard,
              contentDescription = "SD Card",
              tint = Color.White,
              modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
              text = "SD",
              color = Color.White,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
        Spacer(modifier = Modifier.width(4.dp))
      }

      if (item.autoEnhanced) {
        Box(
          modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.6f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = "Auto-Enhanced",
            tint = EnhanceGold,
            modifier = Modifier.size(12.dp)
          )
        }
      }
    }

    // Top Right: Favorite heart
    if (item.isFavorite) {
      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(6.dp)
          .size(20.dp)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Favorite,
          contentDescription = "Favorite",
          tint = Color.Red,
          modifier = Modifier.size(12.dp)
        )
      }
    }

    // Bottom Badges: Video Duration
    if (item.mediaType == MediaType.VIDEO) {
      Box(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(6.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(Color.Black.copy(alpha = 0.7f))
          .padding(horizontal = 6.dp, vertical = 2.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play Video",
            tint = Color.White,
            modifier = Modifier.size(12.dp)
          )
          Spacer(modifier = Modifier.width(2.dp))
          Text(
            text = item.formattedDuration,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }

    // Multi-Selection Checkbox Overlay
    AnimatedVisibility(
      visible = isSelectionMode,
      enter = fadeIn(),
      exit = fadeOut(),
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(6.dp)
    ) {
      Box(
        modifier = Modifier
          .size(24.dp)
          .clip(CircleShape)
          .background(
            if (isSelected) MaterialTheme.colorScheme.primary
            else Color.Black.copy(alpha = 0.4f)
          )
          .border(
            width = 2.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
            shape = CircleShape
          ),
        contentAlignment = Alignment.Center
      ) {
        if (isSelected) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Selected",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(14.dp)
          )
        }
      }
    }
  }
}
