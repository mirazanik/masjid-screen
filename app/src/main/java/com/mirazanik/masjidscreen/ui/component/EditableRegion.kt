package com.mirazanik.masjidscreen.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mirazanik.masjidscreen.data.model.ScreenLayout
import com.mirazanik.masjidscreen.ui.theme.LocalMosqueColors

data class DisplayEditActions(
    val onJamaatClick: () -> Unit,
    val onHadithClick: () -> Unit,
    val onNoticeClick: () -> Unit,
    val onHeaderClick: () -> Unit,
    val onLayoutChange: ((ScreenLayout) -> Unit)? = null,
    val onLayoutCommit: ((ScreenLayout) -> Unit)? = null,
)

@Composable
fun EditableRegion(
    enabled: Boolean,
    label: String,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    content: @Composable BoxScope.() -> Unit
) {
    if (!enabled) {
        Box(modifier = modifier.clip(shape), content = content)
        return
    }

    val c = LocalMosqueColors.current
    Box(
        modifier = modifier
            .clip(shape)
            .border(1.5.dp, c.primary.copy(alpha = 0.7f), shape)
            .clickable(onClick = onEdit)
    ) {
        content()
        EditBadge(
            label = label,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
        )
    }
}

@Composable
fun EditBadge(
    label: String,
    modifier: Modifier = Modifier
) {
    val c = LocalMosqueColors.current
    val s = LocalDisplayCanvasScale.current.coerceAtLeast(0.01f)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp * s))
            .background(c.primary)
            .padding(horizontal = 6.dp * s, vertical = 3.dp * s),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp * s)
    ) {
        Icon(
            Icons.Default.Edit,
            contentDescription = null,
            tint = c.textOnPrimary,
            modifier = Modifier.size(10.dp * s)
        )
        Text(
            text = label,
            color = c.textOnPrimary,
            fontSize = (9 * s).sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (0.4f * s).sp
        )
    }
}

@Composable
fun EmptyEditPlaceholder(
    text: String,
    modifier: Modifier = Modifier
) {
    val c = LocalMosqueColors.current
    Box(
        modifier = modifier
            .background(c.backgroundCard.copy(alpha = 0.65f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
