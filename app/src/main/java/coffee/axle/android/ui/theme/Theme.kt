package coffee.axle.android.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Light Color Scheme
private val LightColorScheme = lightColorScheme(
	primary = Color(0xFF006C4C),
	onPrimary = Color(0xFFFFFFFF),
	primaryContainer = Color(0xFF89F8C7),
	onPrimaryContainer = Color(0xFF002114),
	secondary = Color(0xFF4D6357),
	onSecondary = Color(0xFFFFFFFF),
	secondaryContainer = Color(0xFFCFE9D9),
	onSecondaryContainer = Color(0xFF092016),
	tertiary = Color(0xFF3D6373),
	onTertiary = Color(0xFFFFFFFF),
	tertiaryContainer = Color(0xFFC1E8FB),
	onTertiaryContainer = Color(0xFF001F29),
	error = Color(0xFFBA1A1A),
	errorContainer = Color(0xFFFFDAD6),
	onError = Color(0xFFFFFFFF),
	onErrorContainer = Color(0xFF410002),
	background = Color(0xFFF6FFF8),
	onBackground = Color(0xFF171D1A),
	surface = Color(0xFFF6FFF8),
	onSurface = Color(0xFF171D1A),
	surfaceVariant = Color(0xFFDCE5DD),
	onSurfaceVariant = Color(0xFF404943),
	outline = Color(0xFF707973),
	inverseOnSurface = Color(0xFFEDF2EE),
	inverseSurface = Color(0xFF2C322F),
	inversePrimary = Color(0xFF6EDBAB)
)

// Dark Color Scheme
private val DarkColorScheme = darkColorScheme(
	primary = Color(0xFF6EDBAB),
	onPrimary = Color(0xFF003827),
	primaryContainer = Color(0xFF005139),
	onPrimaryContainer = Color(0xFF89F8C7),
	secondary = Color(0xFFB3CDBD),
	onSecondary = Color(0xFF1F352B),
	secondaryContainer = Color(0xFF354B40),
	onSecondaryContainer = Color(0xFFCFE9D9),
	tertiary = Color(0xFFA5CCDF),
	onTertiary = Color(0xFF073543),
	tertiaryContainer = Color(0xFF244C5A),
	onTertiaryContainer = Color(0xFFC1E8FB),
	error = Color(0xFFFFB4AB),
	errorContainer = Color(0xFF93000A),
	onError = Color(0xFF690005),
	onErrorContainer = Color(0xFFFFDAD6),
	background = Color(0xFF0F1512),
	onBackground = Color(0xFFE0E3DF),
	surface = Color(0xFF0F1512),
	onSurface = Color(0xFFE0E3DF),
	surfaceVariant = Color(0xFF404943),
	onSurfaceVariant = Color(0xFFC0C9C1),
	outline = Color(0xFF8A938D),
	inverseOnSurface = Color(0xFF0F1512),
	inverseSurface = Color(0xFFE0E3DF),
	inversePrimary = Color(0xFF006C4C)
)

@Composable
fun AxleWeatherTheme(
	darkTheme: Boolean = isSystemInDarkTheme(),
	dynamicColor: Boolean = true, // Dynamic color is available on Android 12+
	content: @Composable () -> Unit
) {
	val colorScheme = when {
		dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
			val context = LocalContext.current
			if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
		}
		
		darkTheme -> DarkColorScheme
		else -> LightColorScheme
	}
	
	val view = LocalView.current
	if (!view.isInEditMode) {
		SideEffect {
			val window = (view.context as Activity).window
			window.statusBarColor = colorScheme.primary.toArgb()
			WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
		}
	}
	
	MaterialTheme(
		colorScheme = colorScheme,
		typography = Typography,
		content = content
	)
}
