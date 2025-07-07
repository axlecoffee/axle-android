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
				val cacheManager = WeatherCacheManager.getInstance(applicationContext)
				
				if (!cacheManager.isCacheExpired()) {
					return@withContext Result.success()
				}
				
				val weatherData: WeatherResponse? = cacheManager.refreshWeatherData()
				
				if (weatherData != null) {
					WeatherAppWidget.updateAllWidgets(applicationContext)
					Result.success()
				} else {
					Result.retry()
				}
			} catch (e: Exception) {
				Log.e("WeatherCacheRefreshWorker", "Error during background refresh", e)
				Result.failure()
			}
		}
	}
}
