package coffee.axle.android

import android.app.Application
import android.util.Log
import coffee.axle.android.widget.CacheMonitor
import coffee.axle.android.widget.WeatherCacheManager

/**
 * Application class for global initialization
 */
class WeatherApplication : Application() {
	
	override fun onCreate() {
		super.onCreate()
		
		Log.d("WeatherApplication", "Application starting")
		
		// Initialize cache manager
		try {
			val cacheManager = WeatherCacheManager.getInstance(this)
			cacheManager.initialize()
			Log.d("WeatherApplication", "Cache manager initialized")
		} catch (e: Exception) {
			Log.e("WeatherApplication", "Failed to initialize cache manager", e)
		}
		
		// Initialize cache monitoring
		try {
			CacheMonitor.initialize(this)
			Log.d("WeatherApplication", "Cache monitor initialized")
		} catch (e: Exception) {
			Log.e("WeatherApplication", "Failed to initialize cache monitor", e)
		}
	}
}
