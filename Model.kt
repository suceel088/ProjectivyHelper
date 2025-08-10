package com.helper.watchbridge

data class MediaItem(
    val id: String,
    val title: String,
    val subtitle: String?,
    val appPackage: String?,
    val timestamp: Long,
    val playbackState: Int? = null  // 3=playing, 2=paused, null=unknown
)

data class BridgePayload(
    val nowPlaying: MediaItem?,
    val watchNext: List<MediaItem>
)
