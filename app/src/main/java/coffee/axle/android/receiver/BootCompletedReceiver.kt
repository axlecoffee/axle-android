package coffee.axle.android.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import coffee.axle.android.widget.WeatherAppWidget
import coffee.axle.android.widget.WeatherCacheManager

/**
 * Boot completed receiver to restart cache management after device reboot
 */
class BootCompletedReceiver : BroadcastReceiver() {
	
	override fun onReceive(context: Context, intent: Intent) {
		when (intent.action) {
			Intent.ACTION_BOOT_COMPLETED,
			Intent.ACTION_MY_PACKAGE_REPLACED,
			Intent.ACTION_PACKAGE_REPLACED -> {
				Log.d("BootCompletedReceiver", "Device boot completed or app updated")
				
				try {
					// Reinitialize cache manager
					val cacheManager = WeatherCacheManager.getInstance(context)
					cacheManager.initialize()
					
					// Update all widgets
					WeatherAppWidget.updateAllWidgets(context)
					
					Log.d("BootCompletedReceiver", "Cache manager and widgets reinitialized")
				} catch (e: Exception) {
					Log.e("BootCompletedReceiver", "Error reinitializing after boot", e)
				}
			}
		}
	}
}
