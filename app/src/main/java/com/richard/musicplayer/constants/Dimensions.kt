package com.richard.musicplayer.constants

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

const val CONTENT_TYPE_HEADER = 0
const val CONTENT_TYPE_LIST = 1
const val CONTENT_TYPE_SONG = 2
const val CONTENT_TYPE_ARTIST = 3
const val CONTENT_TYPE_ALBUM = 4
const val CONTENT_TYPE_PLAYLIST = 5
const val CONTENT_TYPE_FOLDER = 6

val NavigationBarHeight = 72.dp
val MiniPlayerHeight = 72.dp
val QueuePeekHeight = 72.dp
val AppBarHeight = 56.dp
val SearchBarHeight = 96.dp // SearchBar (56dp) + padding vertical (40dp)

val ListItemHeight = 72.dp
val SuggestionItemHeight = 64.dp
val SearchFilterHeight = 56.dp
val ListThumbnailSize = 56.dp
val GridThumbnailHeight = 160.dp
val AlbumThumbnailSize = 180.dp

val ThumbnailCornerRadius = 16.dp
val MenuCornerRadius = 24.dp
val DialogCornerRadius = 28.dp

val PlayerHorizontalPadding = 24.dp
val ContentHorizontalPadding = 16.dp
val ContentVerticalPadding = 12.dp

val NavigationBarAnimationSpec = spring<Dp>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessLow
)

val CardElevation = 4.dp
val MenuElevation = 8.dp
val DialogElevation = 24.dp
