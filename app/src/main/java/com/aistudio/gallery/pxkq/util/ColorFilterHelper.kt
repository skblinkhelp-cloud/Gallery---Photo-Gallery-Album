package com.aistudio.gallery.pxkq.util

import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import com.aistudio.gallery.pxkq.model.MediaItem
import com.aistudio.gallery.pxkq.model.PhotoFilter

object ColorFilterHelper {

  fun buildColorFilter(
    brightness: Float = 0f,
    contrast: Float = 1f,
    saturation: Float = 1f,
    warmth: Float = 0f,
    autoEnhanced: Boolean = false,
    enhanceStrength: Float = 1f,
    filter: PhotoFilter = PhotoFilter.ORIGINAL
  ): ColorFilter? {
    // If original with neutral values, return null for zero overhead
    if (!autoEnhanced &&
      brightness == 0f &&
      contrast == 1f &&
      saturation == 1f &&
      warmth == 0f &&
      filter == PhotoFilter.ORIGINAL
    ) {
      return null
    }

    // Effective adjustments
    var effBrightness = brightness
    var effContrast = contrast
    var effSaturation = saturation
    var effWarmth = warmth

    if (autoEnhanced) {
      val strength = enhanceStrength.coerceIn(0f, 1f)
      effBrightness += 0.08f * strength
      effContrast *= (1f + 0.18f * strength)
      effSaturation *= (1f + 0.30f * strength)
      effWarmth += 0.06f * strength
    }

    when (filter) {
      PhotoFilter.ORIGINAL -> {}
      PhotoFilter.ENHANCE -> {
        effBrightness += 0.10f
        effContrast *= 1.20f
        effSaturation *= 1.35f
      }
      PhotoFilter.VIVID -> {
        effContrast *= 1.25f
        effSaturation *= 1.45f
      }
      PhotoFilter.WARM -> {
        effWarmth += 0.25f
        effSaturation *= 1.10f
      }
      PhotoFilter.COOL -> {
        effWarmth -= 0.25f
        effSaturation *= 1.05f
      }
      PhotoFilter.PUNCHY -> {
        effContrast *= 1.35f
        effBrightness += 0.05f
        effSaturation *= 1.25f
      }
      PhotoFilter.VINTAGE -> {
        effWarmth += 0.15f
        effSaturation *= 0.85f
        effContrast *= 0.95f
      }
      PhotoFilter.MONOCHROME -> {
        effSaturation = 0f
        effContrast *= 1.20f
      }
      PhotoFilter.CINEMA -> {
        effContrast *= 1.25f
        effWarmth -= 0.10f
        effSaturation *= 1.15f
      }
    }

    val matrix = ColorMatrix()

    // Apply saturation first
    if (effSaturation != 1f) {
      matrix.setToSaturation(effSaturation.coerceIn(0f, 3f))
    }

    // Build contrast & brightness & warmth matrix
    val c = effContrast.coerceIn(0.5f, 2.5f)
    val b = effBrightness * 255f
    val w = effWarmth * 30f

    // Formula for contrast: scale*(x - 128) + 128 + brightness
    // offset = 128 * (1 - c) + b
    val offset = 128f * (1f - c) + b
    val rOffset = offset + w
    val bOffset = offset - w

    val adjustMatrix = ColorMatrix(
      floatArrayOf(
        c, 0f, 0f, 0f, rOffset,
        0f, c, 0f, 0f, offset,
        0f, 0f, c, 0f, bOffset,
        0f, 0f, 0f, 1f, 0f
      )
    )

    matrix.timesAssign(adjustMatrix)
    return ColorFilter.colorMatrix(matrix)
  }

  fun getColorFilterForMedia(item: MediaItem): ColorFilter? {
    return buildColorFilter(
      brightness = item.brightness,
      contrast = item.contrast,
      saturation = item.saturation,
      warmth = item.warmth,
      autoEnhanced = item.autoEnhanced,
      enhanceStrength = item.enhanceStrength,
      filter = item.filter
    )
  }
}
