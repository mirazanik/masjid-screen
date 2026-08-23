package com.mirazanik.masjidscreen.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mirazanik.masjidscreen.data.model.Notice
import com.mirazanik.masjidscreen.ui.theme.LocalMosqueColors

@Composable
fun NoticeTicker(notices: List<Notice>, language: String = "en", modifier: Modifier = Modifier) {
    if (notices.isEmpty()) return

    val c = LocalMosqueColors.current
    val combinedText = notices.joinToString("   ✦   ") { it.text }
    var textWidth by remember { mutableIntStateOf(0) }
    var containerWidth by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    val offsetX = remember { Animatable(0f) }
    var animationReady by remember { mutableStateOf(false) }

    LaunchedEffect(combinedText, textWidth, containerWidth) {
        if (textWidth > 0 && containerWidth > 0) {
            offsetX.snapTo(containerWidth.toFloat())
            animationReady = true
            offsetX.animateTo(
                targetValue = -textWidth.toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = maxOf((textWidth + containerWidth) * 12, 14000),
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val h = maxHeight.value.takeIf { it.isFinite() && it > 0f } ?: 32f
        val scale = (h / 32f).coerceIn(0.75f, 2.4f)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(c.tickerBg)
                .onGloballyPositioned { containerWidth = it.size.width },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(c.primary)
                    .padding(horizontal = (10 * scale).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = c.textOnPrimary,
                    modifier = Modifier.size((14 * scale).dp)
                )
                Spacer(Modifier.width((6 * scale).dp))
                Text(
                    text = if (language == "bn") "" else "NOTICE",
                    color = c.textOnPrimary,
                    fontSize = (11 * scale).sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clipToBounds()
                    .padding(horizontal = (10 * scale).dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = combinedText,
                    color = c.textPrimary,
                    fontSize = (13 * scale).sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier
                        .alpha(if (animationReady) 1f else 0f)
                        .offset(x = with(density) { offsetX.value.toDp() })
                        .wrapContentWidth(unbounded = true)
                        .onGloballyPositioned { textWidth = it.size.width }
                )
            }
        }
    }
}
