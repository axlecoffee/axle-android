package coffee.axle.android.widget

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * Configuration data for weather widget
 */
data class WidgetConfiguration(
	val appWidgetId: Int,
	val temperatureTextSize: Float = 24f,
	val conditionTextSize: Float = 12f,
	val feelsLikeTextSize: Float = 14f,
	val temperatureOpacity: Float = 1f,
	val conditionOpacity: Float = 0.8f,
	val feelsLikeOpacity: Float = 0.7f,
	val textColor: Int = Color.White.toArgb(),
	val customColorHex: String = "#FFFFFF"
) {
	
	/**
	 * Convert configuration to Compose Color for preview
	 */
	fun getComposeColor(): Color {
		return Color(textColor)
	}
	
	/**
	 * Get effective text color as Android Color int
	 */
	fun getEffectiveTextColor(): Int {
		return try {
			if (customColorHex.isNotBlank() && customColorHex.startsWith("#")) {
				android.graphics.Color.parseColor(customColorHex)
			} else {
				textColor
			}
		} catch (e: Exception) {
			textColor
		}
	}
}
