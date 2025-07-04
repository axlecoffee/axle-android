package coffee.axle.android.widget

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import coffee.axle.android.data.model.WeatherResponse
import coffee.axle.android.data.repository.WeatherRepository
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Cache manager for weather data used by widgets
 */
object WeatherDataCache {
	
	private const val PREFS_NAME = "weather_cache"
	private const val KEY_WEATHER_DATA = "weather_data"
	private const val KEY_LAST_UPDATE = "last_update"
	private const val CACHE_DURATION_MINUTES = 30
	
	private val gson = Gson()
	
	/**
	 * Get cached weather data or fetch fresh data if cache is expired
	 */
	suspend fun getWeatherData(context: Context): WeatherResponse? {
		return try {
			val prefs = getPrefs(context)
			val cachedData = getCachedData(prefs)
			
			if (cachedData != null && !isCacheExpired(prefs)) {
				Log.d("WeatherDataCache", "Using cached weather data")
				cachedData
			} else {
				Log.d("WeatherDataCache", "Cache expired or missing, fetching fresh data")
				fetchAndCacheData(context, prefs)
			}
		} catch (e: Exception) {
			Log.e("WeatherDataCache", "Error getting weather data", e)
			null
		}
	}
	
	/**
	 * Force refresh weather data
	 */
	suspend fun refreshWeatherData(context: Context): WeatherResponse? {
		return try {
			val prefs = getPrefs(context)
			Log.d("WeatherDataCache", "Force refreshing weather data")
			fetchAndCacheData(context, prefs)
		} catch (e: Exception) {
			Log.e("WeatherDataCache", "Error refreshing weather data", e)
			null
		}
	}
	
	/**
	 * Clear cached weather data
	 */
	fun clearCache(context: Context) {
		try {
			val prefs = getPrefs(context)
			prefs.edit()
				.remove(KEY_WEATHER_DATA)
				.remove(KEY_LAST_UPDATE)
				.apply()
			Log.d("WeatherDataCache", "Cache cleared")
		} catch (e: Exception) {
			Log.e("WeatherDataCache", "Error clearing cache", e)
		}
	}
	
	private fun getPrefs(context: Context): SharedPreferences {
		return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
	}
	
	private fun getCachedData(prefs: SharedPreferences): WeatherResponse? {
		return try {
			val jsonData = prefs.getString(KEY_WEATHER_DATA, null)
			if (jsonData != null) {
				gson.fromJson(jsonData, WeatherResponse::class.java)
			} else {
				null
			}
		} catch (e: JsonSyntaxException) {
			Log.e("WeatherDataCache", "Error parsing cached weather data", e)
			null
		}
	}
	
	private fun isCacheExpired(prefs: SharedPreferences): Boolean {
		val lastUpdate = prefs.getLong(KEY_LAST_UPDATE, 0)
		val currentTime = System.currentTimeMillis()
		val cacheAge = currentTime - lastUpdate
		val cacheValidDuration = TimeUnit.MINUTES.toMillis(CACHE_DURATION_MINUTES.toLong())
		
		val expired = cacheAge > cacheValidDuration
		Log.d("WeatherDataCache", "Cache age: ${cacheAge / 1000}s, expired: $expired")
		return expired
	}
	
	private suspend fun fetchAndCacheData(context: Context, prefs: SharedPreferences): WeatherResponse? {
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
					Log.d("WeatherDataCache", "Weather data cached successfully")
				}
				
				result.getOrNull()
			} catch (e: Exception) {
				Log.e("WeatherDataCache", "Error fetching weather data", e)
				null
			}
		}
	}
}
