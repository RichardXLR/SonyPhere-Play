/*
 * Copyright (C) 2024 z-huang/InnerTune
 * Copyright (C) 2025 O﻿ute﻿rTu﻿ne Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */
package com.richard.musicplayer.ui.theme

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import com.google.material.color.dynamiccolor.DynamicScheme
import com.google.material.color.hct.Hct
import com.google.material.color.scheme.SchemeTonalSpot
import com.google.material.color.score.Score

val DefaultThemeColor = Color(0xFF6200EE)

// Adicionando cores adicionais para gradientes
val GradientColor1 = Color(0xFF6200EE)
val GradientColor2 = Color(0xFF3700B3)
val GradientColor3 = Color(0xFFBB86FC)
val AccentColor = Color(0xFF03DAC6)
val SecondaryAccentColor = Color(0xFFFF4081)

// Cores de superfície elevadas customizadas
val ElevatedSurface1 = Color(0xFF1E1E2E)
val ElevatedSurface2 = Color(0xFF2A2A3E)
val ElevatedSurface3 = Color(0xFF36364E)

@Composable
fun OuterTuneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    pureBlack: Boolean = false,
    themeColor: Color = DefaultThemeColor,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = remember(darkTheme, pureBlack, themeColor) {
        if (themeColor == DefaultThemeColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Tema dinâmico Material You - mantém as cores do sistema
            if (darkTheme) {
                dynamicDarkColorScheme(context).pureBlack(pureBlack)
            } else {
                dynamicLightColorScheme(context)
            }
        } else {
            // Tema personalizado com cores fixas
            SchemeTonalSpot(Hct.fromInt(themeColor.toArgb()), darkTheme, 0.0)
                .toColorScheme()
                .pureBlack(darkTheme && pureBlack)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SonsPhereTypography,
        content = content
    )
}

fun Bitmap.extractThemeColor(): Color {
    val colorsToPopulation = Palette.from(this)
        .maximumColorCount(8)
        .generate()
        .swatches
        .associate { it.rgb to it.population }
    val rankedColors = Score.score(colorsToPopulation)
    return Color(rankedColors.first())
}

fun Bitmap.extractGradientColors(): List<Color> {
    return try {
        val palette = Palette.from(this)
            .maximumColorCount(24) // Aumentado para mais cores
            .generate()

        val colors = mutableListOf<Color>()
        
        // Adicionar cores principais em ordem de prioridade
        palette.dominantSwatch?.rgb?.let { colors.add(Color(it)) }
        palette.vibrantSwatch?.rgb?.let { colors.add(Color(it)) }
        palette.mutedSwatch?.rgb?.let { colors.add(Color(it)) }
        palette.lightVibrantSwatch?.rgb?.let { colors.add(Color(it)) }
        palette.darkVibrantSwatch?.rgb?.let { colors.add(Color(it)) }
        palette.lightMutedSwatch?.rgb?.let { colors.add(Color(it)) }
        palette.darkMutedSwatch?.rgb?.let { colors.add(Color(it)) }
        
        // Se não temos cores suficientes, usar o Score para extrair mais
        if (colors.size < 3) {
            val extractedColors = palette.swatches
                .associate { it.rgb to it.population }
            val scoredColors = Score.score(extractedColors, 6, 0xff4285f4.toInt(), true)
            
            scoredColors.take(6).forEach { colorInt ->
                val color = Color(colorInt)
                if (!colors.any { existingColor -> 
                    kotlin.math.abs(existingColor.red - color.red) < 0.1f &&
                    kotlin.math.abs(existingColor.green - color.green) < 0.1f &&
                    kotlin.math.abs(existingColor.blue - color.blue) < 0.1f
                }) {
                    colors.add(color)
                }
            }
        }
        
        // Garantir pelo menos 3 cores para um bom gradiente
        if (colors.isEmpty()) {
            // Fallback para cores baseadas na cor dominante
            val dominantColor = palette.dominantSwatch?.rgb ?: 0xFF6200EE.toInt()
            val baseColor = Color(dominantColor)
            listOf(
                baseColor,
                baseColor.copy(alpha = 0.8f),
                baseColor.copy(alpha = 0.6f)
            )
        } else {
            // Retornar até 6 cores para gradientes ricos
            colors.distinct().take(6)
        }
        
    } catch (e: Exception) {
        // Fallback em caso de erro
        listOf(
            Color(0xFF6200EE),
            Color(0xFF3700B3),
            Color(0xFF03DAC6)
        )
    }
}

fun DynamicScheme.toColorScheme() = ColorScheme(
    primary = Color(primary),
    onPrimary = Color(onPrimary),
    primaryContainer = Color(primaryContainer),
    onPrimaryContainer = Color(onPrimaryContainer),
    inversePrimary = Color(inversePrimary),
    secondary = Color(secondary),
    onSecondary = Color(onSecondary),
    secondaryContainer = Color(secondaryContainer),
    onSecondaryContainer = Color(onSecondaryContainer),
    tertiary = Color(tertiary),
    onTertiary = Color(onTertiary),
    tertiaryContainer = Color(tertiaryContainer),
    onTertiaryContainer = Color(onTertiaryContainer),
    background = Color(background),
    onBackground = Color(onBackground),
    surface = Color(surface),
    onSurface = Color(onSurface),
    surfaceVariant = Color(surfaceVariant),
    onSurfaceVariant = Color(onSurfaceVariant),
    surfaceTint = Color(primary),
    inverseSurface = Color(inverseSurface),
    inverseOnSurface = Color(inverseOnSurface),
    error = Color(error),
    onError = Color(onError),
    errorContainer = Color(errorContainer),
    onErrorContainer = Color(onErrorContainer),
    outline = Color(outline),
    outlineVariant = Color(outlineVariant),
    scrim = Color(scrim),
    surfaceBright = Color(surfaceBright),
    surfaceDim = Color(surfaceDim),
    surfaceContainer = Color(surfaceContainer),
    surfaceContainerHigh = Color(surfaceContainerHigh),
    surfaceContainerHighest = Color(surfaceContainerHighest),
    surfaceContainerLow = Color(surfaceContainerLow),
    surfaceContainerLowest = Color(surfaceContainerLowest)
)

fun ColorScheme.pureBlack(apply: Boolean) =
    if (apply) copy(
        surface = Color.Black,
        background = Color.Black
    ) else this

val ColorSaver = object : Saver<Color, Int> {
    override fun restore(value: Int): Color = Color(value)
    override fun SaverScope.save(value: Color): Int = value.toArgb()
}
