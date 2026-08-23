package com.mirazanik.masjidscreen.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.mirazanik.masjidscreen.ui.theme.LocalMosqueColors

@Composable
fun splitHandleHitSize(): Dp {
    val scale = LocalDisplayCanvasScale.current.coerceAtLeast(0.01f)
    return 22.dp * scale
}

@Composable
fun SplitHandle(
    verticalBar: Boolean,
    onDeltaPx: (Float) -> Unit,
    onCommit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalMosqueColors.current
    val latestDelta = rememberUpdatedState(onDeltaPx)
    val latestCommit = rememberUpdatedState(onCommit)
    val scale = LocalDisplayCanvasScale.current.coerceAtLeast(0.01f)
    val hit = 22.dp * scale
    val bar = 4.dp * scale
    val barLen = 44.dp * scale
    Box(
        modifier = modifier
            .zIndex(4f)
            .then(
                if (verticalBar) Modifier
                    .width(hit)
                    .fillMaxHeight()
                else Modifier
                    .fillMaxWidth()
                    .height(hit)
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = { latestCommit.value() },
                    onDragCancel = { latestCommit.value() },
                ) { change, amount ->
                    change.consume()
                    latestDelta.value(if (verticalBar) amount.x else amount.y)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .then(
                    if (verticalBar) Modifier
                        .width(bar)
                        .height(barLen)
                    else Modifier
                        .width(barLen)
                        .height(bar)
                )
                .clip(RoundedCornerShape(2.dp * scale))
                .background(c.primary)
        )
    }
}
