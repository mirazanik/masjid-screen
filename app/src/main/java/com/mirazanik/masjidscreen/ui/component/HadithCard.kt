package com.mirazanik.masjidscreen.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mirazanik.masjidscreen.data.model.Hadith
import com.mirazanik.masjidscreen.ui.theme.LocalMosqueColors

@Composable
fun HadithCard(hadith: Hadith, modifier: Modifier = Modifier) {
    val c = LocalMosqueColors.current
    val hasSource = hadith.source.isNotBlank() || hadith.narrator.isNotBlank()
    val reference = remember(hadith.source, hadith.narrator) {
        listOf(hadith.source, hadith.narrator).filter { it.isNotBlank() }.joinToString(" • ")
    }
    val quote = if (hadith.translation.isNotBlank()) "\"${hadith.translation}\"" else ""

    BoxWithConstraints(modifier = modifier) {
        val statusHeight = maxHeight.value
        val isShort = statusHeight < 90f
        val lineH = if (isShort) 1f else 1.2f

        val padV = (statusHeight * 0.06f).coerceIn(2f, 10f)
        val padH = 16f
        val barWidth = (statusHeight * 0.02f).coerceIn(3f, 5f)
        val barGap = 12f
        val quoteBase = (statusHeight * 0.16f).coerceIn(10f, 26f)
        val sourceFont = (statusHeight * 0.11f).coerceIn(9f, 16f)
        val heightKey = statusHeight.toInt()
        var quoteFont by remember(hadith.translation, heightKey) { mutableStateOf(quoteBase.sp) }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(c.backgroundCard),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(barWidth.dp)
                    .fillMaxHeight()
                    .background(c.primary)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = barGap.dp, end = padH.dp, top = padV.dp, bottom = padV.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                if (quote.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = quote,
                            color = c.textPrimary,
                            fontSize = quoteFont,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center,
                            lineHeight = (quoteFont.value * lineH).sp,
                            overflow = TextOverflow.Clip,
                            onTextLayout = { result ->
                                if (result.hasVisualOverflow && quoteFont > 8.sp) {
                                    quoteFont = (quoteFont.value * 0.9f).sp
                                }
                            }
                        )
                    }
                }
                if (hasSource) {
                    Text(
                        text = "— $reference",
                        color = c.textSecondary,
                        fontSize = sourceFont.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        lineHeight = (sourceFont * lineH).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
