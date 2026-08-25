package com.mirazanik.masjidscreen.admin.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mirazanik.masjidscreen.ui.theme.banglaTypography

private val AuthGreen = Color(0xFF4CAF50)

private val AuthDarkColorScheme = darkColorScheme(
    primary = AuthGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1B5E20),
    onPrimaryContainer = Color(0xFFC8E6C9),
    secondary = Color(0xFF81C784),
    onSecondary = Color(0xFF003910),
    secondaryContainer = Color(0xFF1B5E20),
    onSecondaryContainer = Color(0xFFC8E6C9),
    tertiary = Color(0xFFA5D6A7),
    onTertiary = Color(0xFF003910),
    tertiaryContainer = Color(0xFF2E7D32),
    onTertiaryContainer = Color(0xFFE8F5E9),
    background = Color(0xFF0A0A0A),
    onBackground = Color(0xFFE8E8E8),
    surface = Color(0xFF121412),
    onSurface = Color(0xFFE8E8E8),
    surfaceVariant = Color(0xFF1E221E),
    onSurfaceVariant = Color(0xFFB5B5B5),
    surfaceContainer = Color(0xFF1A1C1A),
    surfaceContainerHigh = Color(0xFF222522),
    surfaceContainerHighest = Color(0xFF2A2E2A),
    outline = AuthGreen,
    outlineVariant = Color(0xFF3D4A3D),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = Color(0xFFE8E8E8),
    inverseOnSurface = Color(0xFF1A1A1A),
    inversePrimary = Color(0xFF2E7D32),
    scrim = Color(0xFF000000),
)

private val AdminDarkColorScheme = darkColorScheme(
    primary = Color(0xFFE9C46A),
    onPrimary = Color(0xFF3D2C00),
    primaryContainer = Color(0xFF59440A),
    onPrimaryContainer = Color(0xFFFFDF9B),
    secondary = Color(0xFFF4A261),
    onSecondary = Color(0xFF4A2800),
    secondaryContainer = Color(0xFF6A3C00),
    onSecondaryContainer = Color(0xFFFFDCBE),
    tertiary = Color(0xFF2A9D8F),
    onTertiary = Color(0xFF003731),
    tertiaryContainer = Color(0xFF005048),
    onTertiaryContainer = Color(0xFF70F7E8),
    background = Color(0xFF0F0F0F),
    onBackground = Color(0xFFE8E1D9),
    surface = Color(0xFF1A1A1A),
    onSurface = Color(0xFFE8E1D9),
    surfaceVariant = Color(0xFF262620),
    onSurfaceVariant = Color(0xFFCBC5BB),
    surfaceContainer = Color(0xFF1F1E1A),
    surfaceContainerHigh = Color(0xFF2A2924),
    surfaceContainerHighest = Color(0xFF35332D),
    outline = Color(0xFF958E83),
    outlineVariant = Color(0xFF4A4740),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = Color(0xFFE8E1D9),
    inverseOnSurface = Color(0xFF312F2A),
    inversePrimary = Color(0xFF7A5900),
    scrim = Color(0xFF000000),
)

private val AdminShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun AdminTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AdminDarkColorScheme,
        typography = banglaTypography(),
        shapes = AdminShapes,
        content = content
    )
}

@Composable
fun AuthTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AuthDarkColorScheme,
        typography = banglaTypography(),
        shapes = AdminShapes,
        content = content
    )
}
