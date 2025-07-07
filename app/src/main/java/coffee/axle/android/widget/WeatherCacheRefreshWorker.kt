package coffee.axle.android.widget

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import coffee.axle.android.data.model.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Background worker for automatic weather cache refresh
 */
class WeatherCacheRefreshWorker(
	context: Context,
	workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
	
	override suspend fun doWork(): Result {
		return withContext(Dispatchers.IO) {
			try {
				Log.d("WeatherCacheRefreshWorker", "Starting background cache refresh")
				
				val cacheManager = WeatherCacheManager.getInstance(applicationContext)
				
				// Check if cache needs refresh
				if (!cacheManager.isCacheExpired()) {
					Log.d("WeatherCacheRefreshWorker", "Cache still valid, skipping refresh")
					return@withContext Result.success()
				}
				
				// Refresh cache
				val weatherData: WeatherResponse? = cacheManager.refreshWeatherData()
				
				if (weatherData != null) {
					Log.d("WeatherCacheRefreshWorker", "Cache refreshed successfully")
					
					// Update all widgets after successful refresh
					WeatherAppWidget.updateAllWidgets(applicationContext)
					
					Result.success()
				} else {
					Log.w("WeatherCacheRefreshWorker", "Failed to refresh cache data")
					Result.retry()
				}
			} catch (e: Exception) {
				Log.e("WeatherCacheRefreshWorker", "Error during background refresh", e)
				Result.failure()
			}
		}
	}
}
