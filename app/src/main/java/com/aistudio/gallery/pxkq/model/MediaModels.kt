package com.aistudio.gallery.pxkq.model

enum class MediaType {
  PHOTO,
  VIDEO
}

enum class StorageLocation {
  INTERNAL,
  SD_CARD
}

enum class SmartCategory(val displayName: String, val emoji: String, val description: String) {
  ALL("All", "✨", "All photos & videos"),
  PEOPLE("People", "👥", "Portraits and group photos"),
  SELFIES("Selfies", "🤳", "Front camera captures"),
  NATURE("Nature", "🏔️", "Landscapes, sunsets & plants"),
  ANIMALS("Animals", "🦊", "Pets & wildlife"),
  DOCUMENTS("Documents", "📄", "Receipts, notes & papers"),
  VIDEOS("Videos", "🎥", "Recorded video clips"),
  MOVIES("Movies", "🎬", "Edited & cinematic videos")
}

enum class PhotoFilter(val displayName: String) {
  ORIGINAL("Original"),
  ENHANCE("Enhance"),
  VIVID("Vivid"),
  WARM("Warm"),
  COOL("Cool"),
  PUNCHY("Punchy"),
  VINTAGE("Vintage"),
  MONOCHROME("B&W"),
  CINEMA("Cinema")
}

enum class AspectRatioOption(val displayName: String, val ratio: Float?) {
  FREE("Free", null),
  SQUARE("1:1", 1f),
  RATIO_4_3("4:3", 4f / 3f),
  RATIO_16_9("16:9", 16f / 9f),
  RATIO_9_16("9:16", 9f / 16f),
  RATIO_3_2("3:2", 3f / 2f),
  RATIO_2_3("2:3", 2f / 3f)
}

data class MediaItem(
  val id: String,
  val title: String,
  val drawableResId: Int? = null,
  val customUri: String? = null,
  val mediaType: MediaType = MediaType.PHOTO,
  val category: SmartCategory = SmartCategory.NATURE,
  val timestamp: Long = System.currentTimeMillis(),
  val dateFormatted: String = "Oct 12, 2026",
  val timelineGroup: String = "Today",
  val location: String = "San Francisco, CA",
  val isFavorite: Boolean = false,
  val storageLocation: StorageLocation = StorageLocation.INTERNAL,
  val folderName: String = "Camera",
  val fileSizeBytes: Long = 2_840_000L,
  val width: Int = 1920,
  val height: Int = 1440,
  val durationSeconds: Int = 0,
  val isCloudSynced: Boolean = true,
  // Editing & Enhancement attributes
  val autoEnhanced: Boolean = false,
  val enhanceStrength: Float = 0f,
  val brightness: Float = 0f,
  val contrast: Float = 1f,
  val saturation: Float = 1f,
  val warmth: Float = 0f,
  val vignette: Float = 0f,
  val filter: PhotoFilter = PhotoFilter.ORIGINAL,
  val rotationDegrees: Int = 0,
  val cropAspectRatio: AspectRatioOption = AspectRatioOption.FREE
) {
  val formattedSize: String
    get() {
      val mb = fileSizeBytes / (1024.0 * 1024.0)
      return String.format("%.1f MB", mb)
    }

  val formattedDuration: String
    get() {
      if (mediaType != MediaType.VIDEO && durationSeconds <= 0) return ""
      val minutes = durationSeconds / 60
      val seconds = durationSeconds % 60
      return String.format("%d:%02d", minutes, seconds)
    }
}

data class FolderItem(
  val id: String,
  val name: String,
  val storageLocation: StorageLocation = StorageLocation.INTERNAL,
  val coverResId: Int? = null,
  val coverUri: String? = null,
  val itemCount: Int = 0,
  val sizeMb: Double = 0.0
)

data class StorageStats(
  val internalUsedGb: Double = 42.8,
  val internalTotalGb: Double = 128.0,
  val sdCardUsedGb: Double = 18.4,
  val sdCardTotalGb: Double = 64.0,
  val isSdCardMounted: Boolean = true
) {
  val internalPercent: Float
    get() = (internalUsedGb / internalTotalGb).toFloat().coerceIn(0f, 1f)

  val sdCardPercent: Float
    get() = (sdCardUsedGb / sdCardTotalGb).toFloat().coerceIn(0f, 1f)
}
