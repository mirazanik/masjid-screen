package com.mirazanik.masjidscreen.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun MosqueTheme(
    theme: AppTheme = AppTheme.NIGHT_NAVY,
    bangla: Boolean = false,
    content: @Composable () -> Unit
) {
    val c = theme.colors
    val colorScheme = darkColorScheme(
        primary = c.primary,
        onPrimary = c.textOnPrimary,
        secondary = c.tableHeaderMid,
        onSecondary = c.textPrimary,
        background = c.backgroundDeep,
        onBackground = c.textPrimary,
        surface = c.backgroundCard,
        onSurface = c.textPrimary,
        surfaceVariant = c.backgroundCardAlt,
        onSurfaceVariant = c.textSecondary,
        outline = c.divider
    )
    CompositionLocalProvider(LocalMosqueColors provides c) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = if (bangla) banglaTypography() else Typography(),
            content = content
        )
    }
}
