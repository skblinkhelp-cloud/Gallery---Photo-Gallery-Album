package com.sk.gallery.photo.album.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sk.gallery.photo.album.model.AspectRatioOption
import com.sk.gallery.photo.album.model.MediaItem
import com.sk.gallery.photo.album.model.PhotoFilter
import com.sk.gallery.photo.album.ui.theme.EnhanceGold
import com.sk.gallery.photo.album.util.ColorFilterHelper

enum class EditorTab(val title: String) {
  FILTERS("Filters"),
  ADJUST("Adjust"),
  TRANSFORM("Crop & Rotate")
}

@Composable
fun PhotoEditorScreen(
  mediaItem: MediaItem?,
  onBack: () -> Unit,
  onSave: (MediaItem) -> Unit,
  onSaveCopy: (MediaItem) -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onBack() }

  if (mediaItem == null) {
    onBack()
    return
  }

  val context = LocalContext.current

  // Editor editable states initialized from mediaItem
  var autoEnhanced by remember { mutableStateOf(mediaItem.autoEnhanced) }
  var enhanceStrength by remember { mutableFloatStateOf(if (mediaItem.autoEnhanced) 1f else 0.8f) }
  var brightness by remember { mutableFloatStateOf(mediaItem.brightness) }
  var contrast by remember { mutableFloatStateOf(mediaItem.contrast) }
  var saturation by remember { mutableFloatStateOf(mediaItem.saturation) }
  var warmth by remember { mutableFloatStateOf(mediaItem.warmth) }
  var selectedFilter by remember { mutableStateOf(mediaItem.filter) }
  var rotationDegrees by remember { mutableIntStateOf(mediaItem.rotationDegrees) }
  var cropAspectRatio by remember { mutableStateOf(mediaItem.cropAspectRatio) }
  var selectedTab by remember { mutableStateOf(EditorTab.FILTERS) }

  // Compare mode: when true, renders original without adjustments
  var isComparingOriginal by remember { mutableStateOf(false) }

  val activeColorFilter = if (isComparingOriginal) {
    null
  } else {
    ColorFilterHelper.buildColorFilter(
      brightness = brightness,
      contrast = contrast,
      saturation = saturation,
      warmth = warmth,
      autoEnhanced = autoEnhanced,
      enhanceStrength = enhanceStrength,
      filter = selectedFilter
    )
  }

  val resetAll = {
    autoEnhanced = false
    enhanceStrength = 0f
    brightness = 0f
    contrast = 1f
    saturation = 1f
    warmth = 0f
    selectedFilter = PhotoFilter.ORIGINAL
    rotationDegrees = 0
    cropAspectRatio = AspectRatioOption.FREE
  }

  val buildUpdatedItem = {
    mediaItem.copy(
      autoEnhanced = autoEnhanced,
      enhanceStrength = enhanceStrength,
      brightness = brightness,
      contrast = contrast,
      saturation = saturation,
      warmth = warmth,
      filter = selectedFilter,
      rotationDegrees = rotationDegrees,
      cropAspectRatio = cropAspectRatio
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF141416))
      .testTag("photo_editor_screen")
  ) {
    // Top Bar: Cancel, Title, Reset, Save
    Surface(
      color = Color(0xFF1B1C20),
      modifier = Modifier.fillMaxWidth()
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
            modifier = Modifier
              .size(36.dp)
              .testTag("editor_cancel_button")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Cancel",
              tint = Color.White
            )
          }
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Edit",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          // Reset button
          IconButton(
            onClick = resetAll,
            modifier = Modifier
              .size(36.dp)
              .testTag("editor_reset_button")
          ) {
            Icon(
              imageVector = Icons.Default.RestartAlt,
              contentDescription = "Reset",
              tint = Color.White.copy(alpha = 0.8f),
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(6.dp))

          // Save Copy
          OutlinedButton(
            onClick = { onSaveCopy(buildUpdatedItem()) },
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            modifier = Modifier.testTag("editor_save_copy_button")
          ) {
            Text("Copy", fontSize = 12.sp, maxLines = 1, softWrap = false)
          }

          Spacer(modifier = Modifier.width(6.dp))

          // Save Overwrite
          Button(
            onClick = { onSave(buildUpdatedItem()) },
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.testTag("editor_save_button")
          ) {
            Text("Save", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
          }
        }
      }
    }

    // Main Canvas & 1-Tap Enhance Header
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .padding(12.dp),
      contentAlignment = Alignment.Center
    ) {
      val cropModifier = if (cropAspectRatio.ratio != null) {
        Modifier
          .aspectRatio(cropAspectRatio.ratio!!)
          .clip(RoundedCornerShape(8.dp))
      } else {
        Modifier.fillMaxSize()
      }

      val imageModel = mediaItem.customUri ?: mediaItem.drawableResId

      Box(
        modifier = cropModifier,
        contentAlignment = Alignment.Center
      ) {
        AsyncImage(
          model = ImageRequest.Builder(context)
            .data(imageModel)
            .crossfade(true)
            .build(),
          contentDescription = "Editing preview",
          contentScale = if (cropAspectRatio.ratio != null) ContentScale.Crop else ContentScale.Fit,
          colorFilter = activeColorFilter,
          modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(rotationZ = rotationDegrees.toFloat())
        )

        // Crop Guides Overlay (Rule of thirds grid) when in TRANSFORM tab
        if (selectedTab == EditorTab.TRANSFORM) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .border(2.dp, EnhanceGold, RoundedCornerShape(8.dp))
          ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              val w = size.width
              val h = size.height
              val gridColor = Color.White.copy(alpha = 0.5f)

              // Vertical lines
              drawLine(gridColor, Offset(w / 3f, 0f), Offset(w / 3f, h), strokeWidth = 2f)
              drawLine(gridColor, Offset(2 * w / 3f, 0f), Offset(2 * w / 3f, h), strokeWidth = 2f)

              // Horizontal lines
              drawLine(gridColor, Offset(0f, h / 3f), Offset(w, h / 3f), strokeWidth = 2f)
              drawLine(gridColor, Offset(0f, 2 * h / 3f), Offset(w, 2 * h / 3f), strokeWidth = 2f)
            }
          }
        }
      }

      // 1-Tap Auto-Enhance Floater
      Row(
        modifier = Modifier
          .align(Alignment.TopCenter)
          .padding(top = 10.dp)
          .clip(RoundedCornerShape(24.dp))
          .background(
            if (autoEnhanced) EnhanceGold.copy(alpha = 0.95f)
            else Color.Black.copy(alpha = 0.75f)
          )
          .clickable {
            autoEnhanced = !autoEnhanced
            if (autoEnhanced && enhanceStrength == 0f) {
              enhanceStrength = 1f
            }
          }
          .padding(horizontal = 16.dp, vertical = 8.dp)
          .testTag("editor_one_tap_enhance_toggle"),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = "1-Tap Auto Enhance",
          tint = if (autoEnhanced) Color.Black else EnhanceGold,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (autoEnhanced) "Auto-Enhanced (Active)" else "1-Tap Auto-Enhance",
          color = if (autoEnhanced) Color.Black else Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp
        )
      }

      // Compare Original Button
      Box(
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(12.dp)
          .clip(RoundedCornerShape(20.dp))
          .background(Color.Black.copy(alpha = 0.7f))
          .pointerInput(Unit) {
            detectTapGestures(
              onPress = {
                isComparingOriginal = true
                tryAwaitRelease()
                isComparingOriginal = false
              }
            )
          }
          .padding(horizontal = 14.dp, vertical = 8.dp)
          .testTag("editor_compare_button")
      ) {
        Text(
          text = if (isComparingOriginal) "Showing Original" else "Hold to Compare",
          color = Color.White,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold
        )
      }
    }

    // Bottom Controls & Adjustments
    Surface(
      color = Color(0xFF1B1C20),
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        // Tab Selector: Filters, Adjust, Transform
        TabRow(
          selectedTabIndex = selectedTab.ordinal,
          containerColor = Color(0xFF1B1C20),
          contentColor = MaterialTheme.colorScheme.primary
        ) {
          EditorTab.entries.forEach { tab ->
            Tab(
              selected = selectedTab == tab,
              onClick = { selectedTab = tab },
              text = { Text(tab.title, fontWeight = FontWeight.SemiBold) }
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tab Content
        when (selectedTab) {
          EditorTab.FILTERS -> {
            // Filter Presets Carousel
            val scrollState = rememberScrollState()
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
              PhotoFilter.entries.forEach { filter ->
                val isSelected = selectedFilter == filter
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  modifier = Modifier
                    .clickable { selectedFilter = filter }
                    .testTag("filter_preset_${filter.name.lowercase()}")
                ) {
                  Box(
                    modifier = Modifier
                      .size(64.dp)
                      .clip(RoundedCornerShape(12.dp))
                      .background(Color(0xFF282A2E))
                      .then(
                        if (isSelected) Modifier.border(2.dp, EnhanceGold, RoundedCornerShape(12.dp))
                        else Modifier
                      ),
                    contentAlignment = Alignment.Center
                  ) {
                    val filterSampleFilter = ColorFilterHelper.buildColorFilter(filter = filter)
                    if (mediaItem.drawableResId != null) {
                      AsyncImage(
                        model = mediaItem.drawableResId,
                        contentDescription = filter.displayName,
                        contentScale = ContentScale.Crop,
                        colorFilter = filterSampleFilter,
                        modifier = Modifier.fillMaxSize()
                      )
                    } else {
                      Text("✨", fontSize = 24.sp)
                    }
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = filter.displayName,
                    color = if (isSelected) EnhanceGold else Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                  )
                }
              }
            }
          }

          EditorTab.ADJUST -> {
            // Fine-Tuning Adjustment Sliders
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
              if (autoEnhanced) {
                // Enhance Strength
                SliderRow(
                  label = "Enhance Strength",
                  valueText = "${(enhanceStrength * 100).toInt()}%",
                  value = enhanceStrength,
                  range = 0f..1f,
                  onValueChange = { enhanceStrength = it }
                )
              }

              // Brightness
              SliderRow(
                label = "Brightness",
                valueText = "${(brightness * 100).toInt()}",
                value = brightness,
                range = -0.5f..0.5f,
                onValueChange = { brightness = it }
              )

              // Contrast
              SliderRow(
                label = "Contrast",
                valueText = "${((contrast - 1f) * 100).toInt()}",
                value = contrast,
                range = 0.5f..1.8f,
                onValueChange = { contrast = it }
              )

              // Saturation
              SliderRow(
                label = "Saturation",
                valueText = "${((saturation - 1f) * 100).toInt()}",
                value = saturation,
                range = 0f..2f,
                onValueChange = { saturation = it }
              )

              // Warmth
              SliderRow(
                label = "Warmth",
                valueText = "${(warmth * 100).toInt()}",
                value = warmth,
                range = -0.5f..0.5f,
                onValueChange = { warmth = it }
              )
            }
          }

          EditorTab.TRANSFORM -> {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
            ) {
              Text(
                text = "Crop Aspect Ratio",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
              )

              // Aspect Ratio Crop Presets Scrollable Row
              val scrollState = rememberScrollState()
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(scrollState)
                  .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                AspectRatioOption.entries.forEach { option ->
                  val isSelected = cropAspectRatio == option
                  Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) EnhanceGold else Color(0xFF282A2E),
                    modifier = Modifier
                      .clickable { cropAspectRatio = option }
                      .testTag("crop_option_${option.name.lowercase()}")
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Icon(
                        imageVector = Icons.Default.Crop,
                        contentDescription = null,
                        tint = if (isSelected) Color.Black else Color.White,
                        modifier = Modifier.size(14.dp)
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = option.displayName,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                      )
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Rotate & Reset Row
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
              ) {
                OutlinedButton(
                  onClick = { rotationDegrees = (rotationDegrees + 90) % 360 },
                  shape = RoundedCornerShape(12.dp),
                  contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                  modifier = Modifier.testTag("rotate_90_button")
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.RotateRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Rotate 90°", maxLines = 1, softWrap = false)
                }

                OutlinedButton(
                  onClick = {
                    cropAspectRatio = AspectRatioOption.FREE
                    rotationDegrees = 0
                  },
                  shape = RoundedCornerShape(12.dp),
                  contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                  Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Reset Crop", maxLines = 1, softWrap = false)
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))
      }
    }
  }
}

@Composable
private fun SliderRow(
  label: String,
  valueText: String,
  value: Float,
  range: ClosedFloatingPointRange<Float>,
  onValueChange: (Float) -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      color = Color.White.copy(alpha = 0.85f),
      fontSize = 12.sp,
      fontWeight = FontWeight.SemiBold
    )
    Text(
      text = valueText,
      color = EnhanceGold,
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold
    )
  }
  Slider(
    value = value,
    onValueChange = onValueChange,
    valueRange = range,
    colors = SliderDefaults.colors(
      thumbColor = EnhanceGold,
      activeTrackColor = EnhanceGold,
      inactiveTrackColor = Color.White.copy(alpha = 0.2f)
    ),
    modifier = Modifier.fillMaxWidth()
  )
}
