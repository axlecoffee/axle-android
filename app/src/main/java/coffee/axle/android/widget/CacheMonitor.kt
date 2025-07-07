package coffee.axle.android.widget

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Utility class for monitoring cache performance and debugging
 */
object CacheMonitor : LifecycleObserver {
	
	private val monitorScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
	
	/**
	 * Initialize monitoring
	 */
	fun initialize(context: Context) {
		ProcessLifecycleOwner.get().lifecycle.addObserver(this)
		
		// Monitor WorkManager status
		monitorWorkManager(context)
	}
	
	/**
	 * Monitor WorkManager background tasks
	 */
	private fun monitorWorkManager(context: Context) {
		monitorScope.launch {
			try {
				val workManager = WorkManager.getInstance(context)
				
				// Monitor weather cache refresh work
				val workInfos = workManager.getWorkInfosByTag("weather_cache_refresh")
				workInfos.observeForever { workInfoList ->
					for (workInfo in workInfoList) {
						when (workInfo.state) {
							WorkInfo.State.RUNNING -> {
								Log.d("CacheMonitor", "Cache refresh work is running")
							}
							WorkInfo.State.SUCCEEDED -> {
								Log.d("CacheMonitor", "Cache refresh work completed successfully")
							}
							WorkInfo.State.FAILED -> {
								Log.w("CacheMonitor", "Cache refresh work failed")
							}
							WorkInfo.State.CANCELLED -> {
								Log.d("CacheMonitor", "Cache refresh work was cancelled")
							}
							else -> {
								Log.d("CacheMonitor", "Cache refresh work state: ${workInfo.state}")
							}
						}
					}
				}
			} catch (e: Exception) {
				Log.e("CacheMonitor", "Error monitoring WorkManager", e)
			}
		}
	}
	
	/**
	 * Log cache statistics
	 */
	fun logCacheStats(context: Context) {
		try {
			val cacheManager = WeatherCacheManager.getInstance(context)
			val stats = cacheManager.getCacheStats()
			Log.d("CacheMonitor", stats)
		} catch (e: Exception) {
			Log.e("CacheMonitor", "Error getting cache stats", e)
		}
	}
	
	/**
	 * Perform cache health check
	 */
	fun performHealthCheck(context: Context) {
		monitorScope.launch {
			try {
				val cacheManager = WeatherCacheManager.getInstance(context)
				
				// Check if cache is working
				val hasData = cacheManager.hasCachedData()
				val isExpired = cacheManager.isCacheExpired()
				
				Log.d("CacheMonitor", "Cache health check:")
				Log.d("CacheMonitor", "  Has cached data: $hasData")
				Log.d("CacheMonitor", "  Is expired: $isExpired")
				
				if (!hasData || isExpired) {
					Log.d("CacheMonitor", "Cache needs refresh, triggering update")
					cacheManager.refreshWeatherData()
				}
			} catch (e: Exception) {
				Log.e("CacheMonitor", "Error performing health check", e)
			}
		}
	}
}
