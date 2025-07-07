package coffee.axle.android.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import coffee.axle.android.R
import coffee.axle.android.data.model.WeatherResponse
import coffee.axle.android.ui.activity.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Traditional AppWidget implementation for weather display
 */
class WeatherAppWidget : AppWidgetProvider() {
	
	private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
	
	override fun onUpdate(
		context: Context,
		appWidgetManager: AppWidgetManager,
		appWidgetIds: IntArray
	) {
		for (appWidgetId in appWidgetIds) {
			updateAppWidget(context, appWidgetManager, appWidgetId)
		}
	}
	
	override fun onReceive(context: Context, intent: Intent) {
		super.onReceive(context, intent)
		
		when (intent.action) {
			AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
				val appWidgetManager = AppWidgetManager.getInstance(context)
				val appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
				if (appWidgetIds != null) {
					onUpdate(context, appWidgetManager, appWidgetIds)
				}
			}
		}
	}
	
	override fun onDeleted(context: Context, appWidgetIds: IntArray) {
		super.onDeleted(context, appWidgetIds)
		
		for (appWidgetId in appWidgetIds) {
			WeatherWidgetConfigViewModel.deletePrefs(context, appWidgetId)
		}
	}
	
	override fun onEnabled(context: Context) {
		super.onEnabled(context)
		
		val cacheManager = WeatherCacheManager.getInstance(context)
		cacheManager.initialize()
	}
	
	override fun onDisabled(context: Context) {
		super.onDisabled(context)
		
		val cacheManager = WeatherCacheManager.getInstance(context)
		cacheManager.cancelBackgroundRefresh()
	}
	
	private fun updateAppWidget(
		context: Context,
		appWidgetManager: AppWidgetManager,
		appWidgetId: Int
	) {
		val config = loadConfiguration(context, appWidgetId)
		val views = RemoteViews(context.packageName, R.layout.weather_widget)
		
		setupClickIntents(context, views, appWidgetId)
		updateWidgetViews(views, config, null)
		appWidgetManager.updateAppWidget(appWidgetId, views)
		
		widgetScope.launch {
			try {
				val weatherData = loadWeatherData(context)
				updateWidgetViews(views, config, weatherData)
				appWidgetManager.updateAppWidget(appWidgetId, views)
			} catch (e: Exception) {
				Log.e("WeatherAppWidget", "Failed to load weather data for widget $appWidgetId", e)
				updateWidgetViews(views, config, null)
				appWidgetManager.updateAppWidget(appWidgetId, views)
			}
		}
	}
	
	private fun loadConfiguration(context: Context, appWidgetId: Int): WidgetConfiguration {
		val prefs = context.getSharedPreferences(
			WeatherWidgetConfigViewModel.PREFS_NAME,
			Context.MODE_PRIVATE
		)
		
		return WidgetConfiguration(
			appWidgetId = appWidgetId,
			temperatureTextSize = prefs.getFloat(
				"${WeatherWidgetConfigViewModel.PREF_TEMP_SIZE}$appWidgetId",
				24f
			),
			conditionTextSize = prefs.getFloat(
				"${WeatherWidgetConfigViewModel.PREF_CONDITION_SIZE}$appWidgetId",
				12f
			),
			feelsLikeTextSize = prefs.getFloat(
				"${WeatherWidgetConfigViewModel.PREF_FEELS_LIKE_SIZE}$appWidgetId",
				14f
			),
			temperatureOpacity = prefs.getFloat(
				"${WeatherWidgetConfigViewModel.PREF_TEMP_OPACITY}$appWidgetId",
				1f
			),
			conditionOpacity = prefs.getFloat(
				"${WeatherWidgetConfigViewModel.PREF_CONDITION_OPACITY}$appWidgetId",
				0.8f
			),
			feelsLikeOpacity = prefs.getFloat(
				"${WeatherWidgetConfigViewModel.PREF_FEELS_LIKE_OPACITY}$appWidgetId",
				0.7f
			),
			textColor = prefs.getInt(
				"${WeatherWidgetConfigViewModel.PREF_COLOR}$appWidgetId",
				Color.WHITE
			),
			customColorHex = prefs.getString(
				"${WeatherWidgetConfigViewModel.PREF_CUSTOM_COLOR}$appWidgetId",
				"#FFFFFF"
			) ?: "#FFFFFF"
		)
	}
	
	private suspend fun loadWeatherData(context: Context): WeatherResponse? {
		return try {
			val cacheManager = WeatherCacheManager.getInstance(context)
			cacheManager.getWeatherData()
		} catch (e: Exception) {
			Log.e("WeatherAppWidget", "Error loading weather data", e)
			null
		}
	}
	
	private fun setupClickIntents(context: Context, views: RemoteViews, appWidgetId: Int) {
		val mainIntent = Intent(context, MainActivity::class.java)
		val mainPendingIntent = PendingIntent.getActivity(
			context,
			appWidgetId,
			mainIntent,
			getPendingIntentFlags()
		)
		views.setOnClickPendingIntent(R.id.widget_container, mainPendingIntent)
	}
	
	private fun updateWidgetViews(
		views: RemoteViews,
		config: WidgetConfiguration,
		weatherData: WeatherResponse?
	) {
		if (weatherData != null && weatherData.data.current.isNotEmpty()) {
			// Update with weather data
			val currentWeather = weatherData.data.current[0]
			
			// Temperature 
			views.setTextViewText(R.id.temperature_text, "${currentWeather.temperature}°")
			views.setTextViewTextSize(R.id.temperature_text, android.util.TypedValue.COMPLEX_UNIT_SP, config.temperatureTextSize)
			views.setTextColor(R.id.temperature_text, applyOpacity(config.textColor, config.temperatureOpacity))
			
			// Feels like AughGHGha
			views.setTextViewText(R.id.feels_like_text, "${currentWeather.feelsLike}°")
			views.setTextViewTextSize(R.id.feels_like_text, android.util.TypedValue.COMPLEX_UNIT_SP, config.feelsLikeTextSize)
			views.setTextColor(R.id.feels_like_text, applyOpacity(config.textColor, config.feelsLikeOpacity))
			
			// me when its sunny or smth diek
			views.setTextViewText(R.id.condition_text, currentWeather.condition)
			views.setTextViewTextSize(R.id.condition_text, android.util.TypedValue.COMPLEX_UNIT_SP, config.conditionTextSize)
			views.setTextColor(R.id.condition_text, applyOpacity(config.textColor, config.conditionOpacity))
			
		} else {
			views.setTextViewText(R.id.temperature_text, "Loading...")
			views.setTextViewTextSize(R.id.temperature_text, android.util.TypedValue.COMPLEX_UNIT_SP, config.temperatureTextSize)
			views.setTextColor(R.id.temperature_text, applyOpacity(config.textColor, config.temperatureOpacity))
			views.setTextViewText(R.id.feels_like_text, "")
			views.setTextViewText(R.id.condition_text, "Tap to open app")
			views.setTextViewTextSize(R.id.condition_text, android.util.TypedValue.COMPLEX_UNIT_SP, config.conditionTextSize)
			views.setTextColor(R.id.condition_text, applyOpacity(config.textColor, config.conditionOpacity))
		}
	}
	
	private fun applyOpacity(color: Int, opacity: Float): Int {
		val alpha = (opacity * 255).toInt().coerceIn(0, 255)
		return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
	}
	
	private fun getPendingIntentFlags(): Int {
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
		} else {
			PendingIntent.FLAG_UPDATE_CURRENT
		}
	}
	
	companion object {
		/**
		 * Update all widgets manually
		 */
		fun updateAllWidgets(context: Context) {
			try {
				val appWidgetManager = AppWidgetManager.getInstance(context)
				val componentName = ComponentName(context, WeatherAppWidget::class.java)
				val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
				
				val intent = Intent(context, WeatherAppWidget::class.java).apply {
					action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
					putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
				}
				
				context.sendBroadcast(intent)
			} catch (e: Exception) {
				Log.e("WeatherAppWidget", "Failed to update all widgets", e)
			}
		}
	}
}
