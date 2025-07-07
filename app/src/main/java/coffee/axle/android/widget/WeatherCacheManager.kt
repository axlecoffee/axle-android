package coffee.axle.android.widget

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coffee.axle.android.data.model.WeatherResponse
import coffee.axle.android.data.repository.WeatherRepository
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Production-ready weather cache manager following repository pattern
 * Handles automatic cache refresh, app restart scenarios, and background updates
 */
class WeatherCacheManager private constructor(private val context: Context) {
	
	companion object {
		private const val PREFS_NAME = "weather_cache_v2"
		private const val KEY_WEATHER_DATA = "weather_data"
		private const val KEY_LAST_UPDATE = "last_update"
		private const val KEY_CACHE_VERSION = "cache_version"
		private const val CACHE_DURATION_MINUTES = 30
		private const val BACKGROUND_REFRESH_INTERVAL_MINUTES = 15L
		private const val CACHE_VERSION = 1
		private const val WORK_TAG = "weather_cache_refresh"
		
		@Volatile
		private var INSTANCE: WeatherCacheManager? = null
		
		/**
		 * Get singleton instance of WeatherCacheManager
		 */
		fun getInstance(context: Context): WeatherCacheManager {
			return INSTANCE ?: synchronized(this) {
				INSTANCE ?: WeatherCacheManager(context.applicationContext).also { INSTANCE = it }
			}
		}
	}
	
	private val gson = Gson()
	private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
	private val workManager = WorkManager.getInstance(context)
	
	/**
	 * Initialize cache manager - call this on app startup
	 */
	fun initialize() {
		Log.d("WeatherCacheManager", "Initializing cache manager")
		
		// Check if cache version is current
		val currentVersion = prefs.getInt(KEY_CACHE_VERSION, 0)
		if (currentVersion < CACHE_VERSION) {
			Log.d("WeatherCacheManager", "Cache version outdated, clearing cache")
			clearCache()
			prefs.edit().putInt(KEY_CACHE_VERSION, CACHE_VERSION).apply()
		}
		
		// Start background refresh scheduling
		scheduleBackgroundRefresh()
		
		Log.d("WeatherCacheManager", "Cache manager initialized successfully")
	}
	
	/**
	 * Get cached weather data or fetch fresh data if cache is expired
	 */
	suspend fun getWeatherData(): WeatherResponse? {
		return try {
			val cachedData = getCachedData()
			
			if (cachedData != null && !isCacheExpired()) {
				Log.d("WeatherCacheManager", "Using cached weather data")
				cachedData
			} else {
				Log.d("WeatherCacheManager", "Cache expired or missing, fetching fresh data")
				fetchAndCacheData()
			}
		} catch (e: Exception) {
			Log.e("WeatherCacheManager", "Error getting weather data", e)
			getCachedData() // Return cached data even if expired in case of error
		}
	}
	
	/**
	 * Force refresh weather data
	 */
	suspend fun refreshWeatherData(): WeatherResponse? {
		return try {
			Log.d("WeatherCacheManager", "Force refreshing weather data")
			fetchAndCacheData()
		} catch (e: Exception) {
			Log.e("WeatherCacheManager", "Error refreshing weather data", e)
			getCachedData() // Return cached data as fallback
		}
	}
	
	/**
	 * Check if cached data is available
	 */
	fun hasCachedData(): Boolean {
		return getCachedData() != null
	}
	
	/**
	 * Check if cache is expired
	 */
	fun isCacheExpired(): Boolean {
		val lastUpdate = prefs.getLong(KEY_LAST_UPDATE, 0)
		if (lastUpdate == 0L) return true
		
		val currentTime = System.currentTimeMillis()
		val cacheAge = currentTime - lastUpdate
		val cacheValidDuration = TimeUnit.MINUTES.toMillis(CACHE_DURATION_MINUTES.toLong())
		
		val expired = cacheAge > cacheValidDuration
		Log.d("WeatherCacheManager", "Cache age: ${cacheAge / 1000}s, expired: $expired")
		return expired
	}
	
	/**
	 * Clear cached weather data
	 */
	fun clearCache() {
		try {
			prefs.edit()
				.remove(KEY_WEATHER_DATA)
				.remove(KEY_LAST_UPDATE)
				.apply()
			Log.d("WeatherCacheManager", "Cache cleared")
		} catch (e: Exception) {
			Log.e("WeatherCacheManager", "Error clearing cache", e)
		}
	}
	
	/**
	 * Schedule background refresh using WorkManager
	 */
	private fun scheduleBackgroundRefresh() {
		try {
			val constraints = Constraints.Builder()
				.setRequiredNetworkType(NetworkType.CONNECTED)
				.setRequiresBatteryNotLow(true)
				.build()
			
			val refreshWork = PeriodicWorkRequestBuilder<WeatherCacheRefreshWorker>(
				BACKGROUND_REFRESH_INTERVAL_MINUTES, TimeUnit.MINUTES
			)
				.setConstraints(constraints)
				.addTag(WORK_TAG)
				.build()
			
			workManager.enqueueUniquePeriodicWork(
				WORK_TAG,
				ExistingPeriodicWorkPolicy.KEEP,
				refreshWork
			)
			
			Log.d("WeatherCacheManager", "Background refresh scheduled")
		} catch (e: Exception) {
			Log.e("WeatherCacheManager", "Failed to schedule background refresh", e)
		}
	}
	
	/**
	 * Cancel background refresh
	 */
	fun cancelBackgroundRefresh() {
		try {
			workManager.cancelAllWorkByTag(WORK_TAG)
			Log.d("WeatherCacheManager", "Background refresh cancelled")
		} catch (e: Exception) {
			Log.e("WeatherCacheManager", "Failed to cancel background refresh", e)
		}
	}
	
	/**
	 * Get cached data from SharedPreferences
	 */
	private fun getCachedData(): WeatherResponse? {
		return try {
			val jsonData = prefs.getString(KEY_WEATHER_DATA, null)
			if (jsonData != null) {
				gson.fromJson(jsonData, WeatherResponse::class.java)
			} else {
				null
			}
		} catch (e: JsonSyntaxException) {
			Log.e("WeatherCacheManager", "Error parsing cached weather data", e)
			// Clear corrupted cache
			clearCache()
			null
		}
	}
	
	/**
	 * Fetch fresh data from repository and cache it
	 */
	private suspend fun fetchAndCacheData(): WeatherResponse? {
		return withContext(Dispatchers.IO) {
			try {
				val repository = WeatherRepository()
				val result = repository.getWeatherData()
				
				result.onSuccess { weatherData ->
					// Cache the data
					val jsonData = gson.toJson(weatherData)
					prefs.edit()
						.putString(KEY_WEATHER_DATA, jsonData)
						.putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
						.apply()
					Log.d("WeatherCacheManager", "Weather data cached successfully")
					
					// Update all widgets after successful cache update
					WeatherAppWidget.updateAllWidgets(context)
				}
				
				result.getOrNull()
			} catch (e: Exception) {
				Log.e("WeatherCacheManager", "Error fetching weather data", e)
				null
			}
		}
	}
	
	/**
	 * Get cache statistics for debugging
	 */
	fun getCacheStats(): String {
		val lastUpdate = prefs.getLong(KEY_LAST_UPDATE, 0)
		val hasData = hasCachedData()
		val expired = isCacheExpired()
		val cacheAge = if (lastUpdate > 0) (System.currentTimeMillis() - lastUpdate) / 1000 else 0
		
		return "Cache Stats: hasData=$hasData, expired=$expired, age=${cacheAge}s"
	}
}
