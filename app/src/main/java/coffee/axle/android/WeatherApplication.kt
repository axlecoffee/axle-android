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
		
		try {
			val cacheManager = WeatherCacheManager.getInstance(this)
			cacheManager.initialize()
		} catch (e: Exception) {
			Log.e("WeatherApplication", "Failed to initialize cache manager", e)
		}
		
		try {
			CacheMonitor.initialize(this)
		} catch (e: Exception) {
			Log.e("WeatherApplication", "Failed to initialize cache monitor", e)
		}
	}
}
