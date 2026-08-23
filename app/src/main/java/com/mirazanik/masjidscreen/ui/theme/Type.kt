package com.mirazanik.masjidscreen.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.mirazanik.masjidscreen.R

val Kalpurush = FontFamily(Font(R.font.kalpurush))

fun banglaTypography(): Typography {
    val base = Typography()
    return base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = Kalpurush),
        displayMedium = base.displayMedium.copy(fontFamily = Kalpurush),
        displaySmall = base.displaySmall.copy(fontFamily = Kalpurush),
        headlineLarge = base.headlineLarge.copy(fontFamily = Kalpurush),
        headlineMedium = base.headlineMedium.copy(fontFamily = Kalpurush),
        headlineSmall = base.headlineSmall.copy(fontFamily = Kalpurush),
        titleLarge = base.titleLarge.copy(fontFamily = Kalpurush),
        titleMedium = base.titleMedium.copy(fontFamily = Kalpurush),
        titleSmall = base.titleSmall.copy(fontFamily = Kalpurush),
        bodyLarge = base.bodyLarge.copy(fontFamily = Kalpurush),
        bodyMedium = base.bodyMedium.copy(fontFamily = Kalpurush),
        bodySmall = base.bodySmall.copy(fontFamily = Kalpurush),
        labelLarge = base.labelLarge.copy(fontFamily = Kalpurush),
        labelMedium = base.labelMedium.copy(fontFamily = Kalpurush),
        labelSmall = base.labelSmall.copy(fontFamily = Kalpurush),
    )
}
