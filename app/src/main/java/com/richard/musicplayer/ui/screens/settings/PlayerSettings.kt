/*
 * Copyright (C) 2024 z-huang/InnerTune
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.richard.musicplayer.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.ClearAll
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.NoCell
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Gesture
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.SurroundSound
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.richard.musicplayer.LocalPlayerAwareWindowInsets
import com.richard.musicplayer.R
import com.richard.musicplayer.constants.AudioNormalizationKey
import com.richard.musicplayer.constants.AudioOffload
import com.richard.musicplayer.constants.AudioQuality
import com.richard.musicplayer.constants.AudioQualityKey
import com.richard.musicplayer.constants.AutoLoadMoreKey
import com.richard.musicplayer.constants.KeepAliveKey
import com.richard.musicplayer.constants.PersistentQueueKey
import com.richard.musicplayer.constants.SkipOnErrorKey
import com.richard.musicplayer.constants.StopMusicOnTaskClearKey
import com.richard.musicplayer.constants.minPlaybackDurKey
import com.richard.musicplayer.ui.component.CounterDialog
import com.richard.musicplayer.ui.component.EnumListPreference
import com.richard.musicplayer.ui.component.IconButton
import com.richard.musicplayer.ui.component.PreferenceEntry
import com.richard.musicplayer.ui.component.PreferenceGroupTitle
import com.richard.musicplayer.ui.component.SettingsClickToReveal
import com.richard.musicplayer.ui.component.SwitchPreference

import com.richard.musicplayer.ui.utils.backToMain
import com.richard.musicplayer.utils.rememberEnumPreference
import com.richard.musicplayer.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val (audioQuality, onAudioQualityChange) = rememberEnumPreference(
        key = AudioQualityKey,
        defaultValue = AudioQuality.HIGH
    )
    val (persistentQueue, onPersistentQueueChange) = rememberPreference(key = PersistentQueueKey, defaultValue = true)
    val (skipOnErrorKey, onSkipOnErrorChange) = rememberPreference(key = SkipOnErrorKey, defaultValue = false)
    val (audioNormalization, onAudioNormalizationChange) = rememberPreference(
        key = AudioNormalizationKey,
        defaultValue = true
    )
    val (autoLoadMore, onAutoLoadMoreChange) = rememberPreference(AutoLoadMoreKey, defaultValue = true)
    val (stopMusicOnTaskClear, onStopMusicOnTaskClearChange) = rememberPreference(
        key = StopMusicOnTaskClearKey,
        defaultValue = false
    )
    val (minPlaybackDur, onMinPlaybackDurChange) = rememberPreference(minPlaybackDurKey, defaultValue = 30)
    val (audioOffload, onAudioOffloadChange) = rememberPreference(key = AudioOffload, defaultValue = true)
    val (keepAlive, onKeepAliveChange) = rememberPreference(key = KeepAliveKey, defaultValue = true)

    var showMinPlaybackDur by remember {
        mutableStateOf(false)
    }


    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .verticalScroll(rememberScrollState())
    ) {
        PreferenceGroupTitle(title = stringResource(R.string.player))

        PreferenceGroupTitle(
            title = stringResource(R.string.grp_general)
        )
        SwitchPreference(
            title = { Text(stringResource(R.string.auto_load_more)) },
            description = stringResource(R.string.auto_load_more_desc),
            icon = { Icon(Icons.Rounded.Autorenew, null) },
            checked = autoLoadMore,
            onCheckedChange = onAutoLoadMoreChange
        )
        SwitchPreference(
            title = { Text(stringResource(R.string.stop_music_on_task_clear)) },
            icon = { Icon(Icons.Rounded.ClearAll, null) },
            checked = stopMusicOnTaskClear,
            onCheckedChange = onStopMusicOnTaskClearChange
        )

        PreferenceGroupTitle(
            title = stringResource(R.string.grp_audio)
        )
        EnumListPreference(
            title = { Text(stringResource(R.string.audio_quality)) },
            icon = { Icon(Icons.Rounded.GraphicEq, null) },
            selectedValue = audioQuality,
            onValueSelected = onAudioQualityChange,
            valueText = {
                when (it) {
                    AudioQuality.AUTO -> stringResource(R.string.audio_quality_auto)
                    AudioQuality.HIGH -> stringResource(R.string.audio_quality_high)
                    AudioQuality.LOW -> stringResource(R.string.audio_quality_low)
                }
            }
        )
        SwitchPreference(
            title = { Text(stringResource(R.string.audio_normalization)) },
            icon = { Icon(Icons.AutoMirrored.Rounded.VolumeUp, null) },
            checked = audioNormalization,
            onCheckedChange = onAudioNormalizationChange
        )
        SwitchPreference(
            title = { Text(stringResource(R.string.auto_skip_next_on_error)) },
            description = stringResource(R.string.auto_skip_next_on_error_desc),
            icon = { Icon(Icons.Rounded.FastForward, null) },
            checked = skipOnErrorKey,
            onCheckedChange = onSkipOnErrorChange
        )

        SettingsClickToReveal(stringResource(R.string.advanced)) {
            SwitchPreference(
                title = { Text(stringResource(R.string.persistent_queue)) },
                description = stringResource(R.string.persistent_queue_desc_ot),
                icon = { Icon(Icons.AutoMirrored.Rounded.QueueMusic, null) },
                checked = persistentQueue,
                onCheckedChange = onPersistentQueueChange
            )
            PreferenceEntry(
                title = { Text(stringResource(R.string.min_playback_duration)) },
                icon = { Icon(Icons.Rounded.Sync, null) },
                onClick = { showMinPlaybackDur = true }
            )
            SwitchPreference(
                title = { Text(stringResource(R.string.audio_offload)) },
                description = stringResource(R.string.audio_offload_description),
                icon = { Icon(Icons.Rounded.Bolt, null) },
                checked = audioOffload,
                onCheckedChange = onAudioOffloadChange
            )
            SwitchPreference(
                title = { Text(stringResource(R.string.keep_alive_title)) },
                description = stringResource(R.string.keep_alive_description),
                icon = { Icon(Icons.Rounded.NoCell, null) },
                checked = keepAlive,
                onCheckedChange = onKeepAliveChange
            )
        }

        Spacer(Modifier.height(16.dp))
    }


    /**
     * ---------------------------
     * Dialogs
     * ---------------------------
     */


    if (showMinPlaybackDur) {
        CounterDialog(
            title = stringResource(R.string.min_playback_duration),
            description = stringResource(R.string.min_playback_duration_description),
            initialValue = minPlaybackDur,
            upperBound = 100,
            lowerBound = 0,
            unitDisplay = "%",
            onDismiss = { showMinPlaybackDur = false },
            onConfirm = {
                showMinPlaybackDur = false
                onMinPlaybackDurChange(it)
            },
            onCancel = {
                showMinPlaybackDur = false
            }
        )
    }

    TopAppBar(
        title = { Text(stringResource(R.string.player_and_audio)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}
