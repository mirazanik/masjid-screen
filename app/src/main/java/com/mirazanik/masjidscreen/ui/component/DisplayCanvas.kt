package com.mirazanik.masjidscreen.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import com.mirazanik.masjidscreen.data.model.ScreenInfo
import kotlin.math.min
import kotlin.math.roundToInt

data class DisplayPreviewSpec(
    val widthPx: Int,
    val heightPx: Int,
    val density: Float,
    val fontScale: Float,
    val fromDevice: Boolean,
)

fun ScreenInfo.toPreviewSpec(): DisplayPreviewSpec {
    if (displayWidthPx > 80 && displayHeightPx > 80) {
        return DisplayPreviewSpec(
            widthPx = displayWidthPx,
            heightPx = displayHeightPx,
            density = displayDensity.takeIf { it > 0.1f } ?: 1f,
            fontScale = displayFontScale.takeIf { it > 0.1f } ?: 1f,
            fromDevice = true,
        )
    }
    if (displayWidthDp > 80f && displayHeightDp > 80f) {
        val d = displayDensity.takeIf { it > 0.1f } ?: 1f
        return DisplayPreviewSpec(
            widthPx = (displayWidthDp * d).roundToInt().coerceAtLeast(81),
            heightPx = (displayHeightDp * d).roundToInt().coerceAtLeast(81),
            density = d,
            fontScale = displayFontScale.takeIf { it > 0.1f } ?: 1f,
            fromDevice = true,
        )
    }
    return DisplayPreviewSpec(1920, 1080, 1f, 1f, fromDevice = false)
}

/**
 * Multiply live-edit chrome (handles, badges) by this so they stay finger-sized
 * after the preview canvas is scaled down to the phone.
 */
val LocalDisplayCanvasScale = compositionLocalOf { 1f }

/**
 * Admin-only: lays out [content] at the TV's real pixel size, density, and font
 * scale, then uniformly scales that picture into the phone preview.
 */
@Composable
fun ScaledDisplayCanvas(
    spec: DisplayPreviewSpec,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val designW = spec.widthPx.coerceAtLeast(81)
    val designH = spec.heightPx.coerceAtLeast(81)
    val designDensity = spec.density.coerceAtLeast(0.1f)
    val designFontScale = spec.fontScale.coerceAtLeast(0.1f)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        val parentDensity = LocalDensity.current.density
        val parentW = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val parentH = constraints.maxHeight.toFloat().coerceAtLeast(1f)
        val scale = min(parentW / designW, parentH / designH).coerceAtLeast(0.01f)
        val handleScale = (parentDensity / (designDensity * scale)).coerceAtLeast(0.01f)

        Layout(
            content = {
                CompositionLocalProvider(
                    LocalDensity provides Density(density = designDensity, fontScale = designFontScale),
                    LocalDisplayCanvasScale provides handleScale
                ) {
                    content()
                }
            }
        ) { measurables, _ ->
            val placeable = measurables.first().measure(Constraints.fixed(designW, designH))
            val w = (designW * scale).roundToInt().coerceAtLeast(1)
            val h = (designH * scale).roundToInt().coerceAtLeast(1)
            layout(w, h) {
                placeable.placeWithLayer(0, 0) {
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(0f, 0f)
                }
            }
        }
    }
}
