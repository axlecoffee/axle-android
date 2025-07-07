package coffee.axle.android.widget

import android.Manifest
import android.app.Activity
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coffee.axle.android.R
import coffee.axle.android.ui.theme.AxleWeatherTheme
import kotlinx.coroutines.launch

/**
 * ViewModel for weather widget configuration
 */
class WeatherWidgetConfigViewModel : ViewModel() {
	
	private val _configuration = MutableLiveData<WidgetConfiguration>()
	val configuration: LiveData<WidgetConfiguration> = _configuration
	
	private val _wallpaperBitmap = MutableLiveData<Bitmap?>()
	val wallpaperBitmap: LiveData<Bitmap?> = _wallpaperBitmap
	
	fun initialize(appWidgetId: Int, context: Context) {
		// Load existing configuration for this widget, or use defaults if none exists
		val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
		
		val existingConfig = WidgetConfiguration(
			appWidgetId = appWidgetId,
			temperatureTextSize = prefs.getFloat("${PREF_TEMP_SIZE}$appWidgetId", 24f),
			conditionTextSize = prefs.getFloat("${PREF_CONDITION_SIZE}$appWidgetId", 12f),
			feelsLikeTextSize = prefs.getFloat("${PREF_FEELS_LIKE_SIZE}$appWidgetId", 14f),
			temperatureOpacity = prefs.getFloat("${PREF_TEMP_OPACITY}$appWidgetId", 1f),
			conditionOpacity = prefs.getFloat("${PREF_CONDITION_OPACITY}$appWidgetId", 0.8f),
			feelsLikeOpacity = prefs.getFloat("${PREF_FEELS_LIKE_OPACITY}$appWidgetId", 0.7f),
			textColor = prefs.getInt("${PREF_COLOR}$appWidgetId", android.graphics.Color.WHITE),
			customColorHex = prefs.getString("${PREF_CUSTOM_COLOR}$appWidgetId", "#FFFFFF") ?: "#FFFFFF"
		)
		_configuration.value = existingConfig
		loadWallpaper(context)
	}
	
	fun loadWallpaper(context: Context) {
		viewModelScope.launch {
			try {
				Log.d("WeatherWidgetConfig", "Starting wallpaper load attempt")
				
				val wallpaperManager = WallpaperManager.getInstance(context)
				var wallpaperDrawable: android.graphics.drawable.Drawable? = null
				
				// Method 1: Try peekDrawable first (doesn't require permission on most devices)
				try {
					wallpaperDrawable = wallpaperManager.peekDrawable()
					Log.d("WeatherWidgetConfig", "peekDrawable result: ${wallpaperDrawable != null}")
					if (wallpaperDrawable != null) {
						Log.d("WeatherWidgetConfig", "peekDrawable type: ${wallpaperDrawable::class.java.simpleName}")
					}
				} catch (e: Exception) {
					Log.d("WeatherWidgetConfig", "peekDrawable failed: ${e.message}")
				}
				
				// Method 2: Try fastDrawable if peekDrawable failed
				if (wallpaperDrawable == null) {
					try {
						wallpaperDrawable = wallpaperManager.fastDrawable
						Log.d("WeatherWidgetConfig", "fastDrawable result: ${wallpaperDrawable != null}")
						if (wallpaperDrawable != null) {
							Log.d("WeatherWidgetConfig", "fastDrawable type: ${wallpaperDrawable::class.java.simpleName}")
						}
					} catch (e: Exception) {
						Log.d("WeatherWidgetConfig", "fastDrawable failed: ${e.message}")
					}
				}
				
				// Method 3: Check permissions and try regular drawable
				if (wallpaperDrawable == null) {
					val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
						ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
					} else {
						ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
					}
					
					Log.d("WeatherWidgetConfig", "Has wallpaper permission: $hasPermission (Android ${Build.VERSION.SDK_INT})")
					
					if (hasPermission) {
						try {
							wallpaperDrawable = wallpaperManager.drawable
							Log.d("WeatherWidgetConfig", "drawable result: ${wallpaperDrawable != null}")
							if (wallpaperDrawable != null) {
								Log.d("WeatherWidgetConfig", "drawable type: ${wallpaperDrawable::class.java.simpleName}")
							}
						} catch (e: Exception) {
							Log.d("WeatherWidgetConfig", "drawable failed: ${e.message}")
						}
					}
				}
				
				// Method 4: Try alternative approaches for Samsung/OneUI and modern Android
				if (wallpaperDrawable == null) {
					try {
						// Try getBuiltInDrawable for system wallpapers
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
							wallpaperDrawable = wallpaperManager.getBuiltInDrawable()
							Log.d("WeatherWidgetConfig", "getBuiltInDrawable result: ${wallpaperDrawable != null}")
						}
					} catch (e: Exception) {
						Log.d("WeatherWidgetConfig", "getBuiltInDrawable failed: ${e.message}")
					}
				}
				
				// Method 5: Try without permission check (some Samsung devices allow this)
				if (wallpaperDrawable == null) {
					try {
						wallpaperDrawable = wallpaperManager.drawable
						Log.d("WeatherWidgetConfig", "drawable (no permission check) result: ${wallpaperDrawable != null}")
					} catch (e: Exception) {
						Log.d("WeatherWidgetConfig", "drawable (no permission check) failed: ${e.message}")
					}
				}
				
				// Method 6: Last resort - try peekDrawable again with reflection (Samsung specific)
				if (wallpaperDrawable == null) {
					try {
						val method = wallpaperManager.javaClass.getMethod("peekDrawable")
						wallpaperDrawable = method.invoke(wallpaperManager) as? android.graphics.drawable.Drawable
						Log.d("WeatherWidgetConfig", "peekDrawable (reflection) result: ${wallpaperDrawable != null}")
					} catch (e: Exception) {
						Log.d("WeatherWidgetConfig", "peekDrawable (reflection) failed: ${e.message}")
					}
				}
				
				// Convert to bitmap if we got a drawable
				if (wallpaperDrawable != null) {
					when (wallpaperDrawable) {
						is BitmapDrawable -> {
							_wallpaperBitmap.value = wallpaperDrawable.bitmap
							Log.d("WeatherWidgetConfig", "Successfully loaded wallpaper bitmap from BitmapDrawable")
						}
						else -> {
							// Try to convert other drawable types to bitmap
							try {
								val bitmap = android.graphics.Bitmap.createBitmap(
									wallpaperDrawable.intrinsicWidth.takeIf { it > 0 } ?: 1080,
									wallpaperDrawable.intrinsicHeight.takeIf { it > 0 } ?: 1920,
									android.graphics.Bitmap.Config.ARGB_8888
								)
								val canvas = android.graphics.Canvas(bitmap)
								wallpaperDrawable.setBounds(0, 0, canvas.width, canvas.height)
								wallpaperDrawable.draw(canvas)
								_wallpaperBitmap.value = bitmap
								Log.d("WeatherWidgetConfig", "Successfully converted drawable to bitmap")
							} catch (e: Exception) {
								Log.e("WeatherWidgetConfig", "Failed to convert drawable to bitmap: ${e.message}")
								_wallpaperBitmap.value = null
							}
						}
					}
				} else {
					val deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.SDK_INT})"
					Log.d("WeatherWidgetConfig", "No wallpaper drawable available on $deviceInfo")
					
					// Specific message for Samsung devices
					if (Build.MANUFACTURER.equals("samsung", ignoreCase = true)) {
						Log.d("WeatherWidgetConfig", "Samsung device detected - OneUI may restrict wallpaper access even with READ_MEDIA_IMAGES permission")
					}
					
					_wallpaperBitmap.value = null
				}
			} catch (e: Exception) {
				val deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.SDK_INT})"
				Log.e("WeatherWidgetConfig", "Failed to load wallpaper on $deviceInfo: ${e.message}", e)
				_wallpaperBitmap.value = null
			}
		}
	}
	
	fun updateConfiguration(newConfig: WidgetConfiguration) {
		_configuration.value = newConfig
	}
	
	fun saveConfiguration(context: Context, config: WidgetConfiguration) {
		val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
		val editor = prefs.edit()
		
		Log.d("WidgetConfig", "Saving configuration for widget ${config.appWidgetId}:")
		Log.d("WidgetConfig", "  Temperature size: ${config.temperatureTextSize}")
		Log.d("WidgetConfig", "  Condition size: ${config.conditionTextSize}")
		Log.d("WidgetConfig", "  Feels like size: ${config.feelsLikeTextSize}")
		Log.d("WidgetConfig", "  Temperature opacity: ${config.temperatureOpacity}")
		Log.d("WidgetConfig", "  Condition opacity: ${config.conditionOpacity}")
		Log.d("WidgetConfig", "  Feels like opacity: ${config.feelsLikeOpacity}")
		Log.d("WidgetConfig", "  Text color: ${config.textColor}")
		Log.d("WidgetConfig", "  Custom color hex: ${config.customColorHex}")
		
		editor.putFloat(PREF_TEMP_SIZE + config.appWidgetId, config.temperatureTextSize)
		editor.putFloat(PREF_CONDITION_SIZE + config.appWidgetId, config.conditionTextSize)
		editor.putFloat(PREF_FEELS_LIKE_SIZE + config.appWidgetId, config.feelsLikeTextSize)
		editor.putFloat(PREF_TEMP_OPACITY + config.appWidgetId, config.temperatureOpacity)
		editor.putFloat(PREF_CONDITION_OPACITY + config.appWidgetId, config.conditionOpacity)
		editor.putFloat(PREF_FEELS_LIKE_OPACITY + config.appWidgetId, config.feelsLikeOpacity)
		editor.putInt(PREF_COLOR + config.appWidgetId, config.textColor)
		editor.putString(PREF_CUSTOM_COLOR + config.appWidgetId, config.customColorHex)
		
		// Store timestamp for identifying most recent configuration
		editor.putLong("config_time_${config.appWidgetId}", System.currentTimeMillis())
		editor.apply()
		
		Log.d("WidgetConfig", "Configuration saved successfully for widget ${config.appWidgetId}")
		
		// Verify the save by reading back
		val tempSize = prefs.getFloat(PREF_TEMP_SIZE + config.appWidgetId, -1f)
		Log.d("WidgetConfig", "Verification: Temperature size read back as: $tempSize")
	}
	
	companion object {
		const val PREFS_NAME = "weather_widget_prefs"
		const val PREF_TEMP_SIZE = "temp_size_"
		const val PREF_CONDITION_SIZE = "condition_size_"
		const val PREF_FEELS_LIKE_SIZE = "feels_like_size_"
		const val PREF_TEMP_OPACITY = "temp_opacity_"
		const val PREF_CONDITION_OPACITY = "condition_opacity_"
		const val PREF_FEELS_LIKE_OPACITY = "feels_like_opacity_"
		const val PREF_COLOR = "color_"
		const val PREF_CUSTOM_COLOR = "custom_color_"
		
		fun getTempSize(context: Context, appWidgetId: Int): Float {
			val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
			return prefs.getFloat(PREF_TEMP_SIZE + appWidgetId, 24f)
		}
		
		fun getConditionSize(context: Context, appWidgetId: Int): Float {
			val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
			return prefs.getFloat(PREF_CONDITION_SIZE + appWidgetId, 12f)
		}
		
		fun getFeelsLikeSize(context: Context, appWidgetId: Int): Float {
			val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
			return prefs.getFloat(PREF_FEELS_LIKE_SIZE + appWidgetId, 14f)
		}
		
		fun getTempOpacity(context: Context, appWidgetId: Int): Float {
			val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
			return prefs.getFloat(PREF_TEMP_OPACITY + appWidgetId, 1f)
		}
		
		fun getConditionOpacity(context: Context, appWidgetId: Int): Float {
			val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
			return prefs.getFloat(PREF_CONDITION_OPACITY + appWidgetId, 0.8f)
		}
		
		fun getFeelsLikeOpacity(context: Context, appWidgetId: Int): Float {
			val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
			return prefs.getFloat(PREF_FEELS_LIKE_OPACITY + appWidgetId, 0.7f)
		}
		
		fun getColor(context: Context, appWidgetId: Int): Int {
			val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
			return prefs.getInt(PREF_COLOR + appWidgetId, Color.White.toArgb())
		}
		
		fun getCustomColorHex(context: Context, appWidgetId: Int): String {
			val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
			return prefs.getString(PREF_CUSTOM_COLOR + appWidgetId, "#FFFFFF") ?: "#FFFFFF"
		}
		
		fun deletePrefs(context: Context, appWidgetId: Int) {
			val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
			val editor = prefs.edit()
			editor.remove(PREF_TEMP_SIZE + appWidgetId)
			editor.remove(PREF_CONDITION_SIZE + appWidgetId)
			editor.remove(PREF_FEELS_LIKE_SIZE + appWidgetId)
			editor.remove(PREF_TEMP_OPACITY + appWidgetId)
			editor.remove(PREF_CONDITION_OPACITY + appWidgetId)
			editor.remove(PREF_FEELS_LIKE_OPACITY + appWidgetId)
			editor.remove(PREF_COLOR + appWidgetId)
			editor.remove(PREF_CUSTOM_COLOR + appWidgetId)
			editor.remove("config_time_$appWidgetId")
			editor.apply()
			
			// Also clean up widget ID mapping
			val mappingPrefs = context.getSharedPreferences("widget_id_mapping", Context.MODE_PRIVATE)
			val mappingEditor = mappingPrefs.edit()
			
			// Clean up old mapping format
			mappingEditor.remove("appwidget_$appWidgetId")
			mappingEditor.remove("widget_for_id_$appWidgetId")
			
			// Clean up new bidirectional mapping format
			val glanceId = mappingPrefs.getString("widget_to_glance_$appWidgetId", null)
			if (glanceId != null) {
				mappingEditor.remove("glance_to_widget_$glanceId")
			}
			mappingEditor.remove("widget_to_glance_$appWidgetId")
			
			mappingEditor.apply()
			
			Log.d("WidgetConfig", "Deleted preferences and mappings for widget $appWidgetId")
		}
	}
}

/**
 * Modern Compose-based widget configuration activity
 */
class WeatherWidgetConfigActivity : ComponentActivity() {
	
	private val viewModel: WeatherWidgetConfigViewModel by viewModels()
	private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
	
	// Permission launcher for wallpaper access
	private val wallpaperPermissionLauncher = registerForActivityResult(
		ActivityResultContracts.RequestMultiplePermissions()
	) { permissions ->
		Log.d("WeatherWidgetConfig", "Permission results: $permissions")
		val granted = permissions.values.any { it }
		if (granted) {
			Log.d("WeatherWidgetConfig", "Permission granted, reloading wallpaper")
			// Reload wallpaper if permission was granted
			viewModel.loadWallpaper(this)
		} else {
			Log.d("WeatherWidgetConfig", "Permission denied")
		}
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		// Try to make the window show wallpaper in the background
		try {
			window.setFlags(
				android.view.WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER,
				android.view.WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER
			)
			Log.d("WeatherWidgetConfig", "Set FLAG_SHOW_WALLPAPER on window")
		} catch (e: Exception) {
			Log.d("WeatherWidgetConfig", "Failed to set FLAG_SHOW_WALLPAPER: ${e.message}")
		}
		
		setResult(Activity.RESULT_CANCELED)
		
		appWidgetId = intent?.extras?.getInt(
			AppWidgetManager.EXTRA_APPWIDGET_ID,
			AppWidgetManager.INVALID_APPWIDGET_ID
		) ?: AppWidgetManager.INVALID_APPWIDGET_ID
		
		if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish()
			return
		}
		
		// Request wallpaper permission if needed
		requestWallpaperPermissionIfNeeded()
		
		viewModel.initialize(appWidgetId, this)
		
		// Try immediate wallpaper load as well
		viewModel.loadWallpaper(this)
		
		setContent {
			AxleWeatherTheme {
				Surface(
					modifier = Modifier.fillMaxSize(),
					color = MaterialTheme.colorScheme.background
				) {
					WeatherWidgetConfigScreen(
						viewModel = viewModel,
						onConfigurationComplete = { config ->
							saveConfiguration(config)
						},
						onCancel = {
							finish()
						}
					)
				}
			}
		}
	}
	
	private fun requestWallpaperPermissionIfNeeded() {
		// Try a simpler approach - always try to load wallpaper first without permissions
		Log.d("WeatherWidgetConfig", "Trying wallpaper load without permission check first")
		viewModel.loadWallpaper(this)
		
		// Then also request permissions for future attempts
		val permissions = mutableListOf<String>()
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			// Android 13+
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
				permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
				Log.d("WeatherWidgetConfig", "Adding READ_MEDIA_IMAGES permission request")
			} else {
				Log.d("WeatherWidgetConfig", "READ_MEDIA_IMAGES already granted")
			}
		} else {
			// Android 12 and below
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
				Log.d("WeatherWidgetConfig", "Adding READ_EXTERNAL_STORAGE permission request")
			} else {
				Log.d("WeatherWidgetConfig", "READ_EXTERNAL_STORAGE already granted")
			}
		}
		
		if (permissions.isNotEmpty()) {
			Log.d("WeatherWidgetConfig", "Requesting permissions: $permissions")
			wallpaperPermissionLauncher.launch(permissions.toTypedArray())
		} else {
			Log.d("WeatherWidgetConfig", "All permissions already granted")
		}
	}
	
	private fun saveConfiguration(config: WidgetConfiguration) {
		viewModel.saveConfiguration(this, config)
		
		// Create widget ID mapping for Glance widget
		createWidgetIdMapping(appWidgetId)
		
		// Force update the widget to apply the new configuration
		updateWidgetWithConfiguration(appWidgetId)
		
		val resultValue = Intent().apply {
			putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
		}
		setResult(Activity.RESULT_OK, resultValue)
		finish()
	}
	
	private fun updateWidgetWithConfiguration(appWidgetId: Int) {
		// Trigger widget refresh to apply new configuration
		try {
			Log.d("WeatherWidgetConfig", "Triggering widget update for widget $appWidgetId")
			
			// Update specific widget
			val intent = Intent(this, WeatherAppWidget::class.java).apply {
				action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
				putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
			}
			sendBroadcast(intent)
		} catch (e: Exception) {
			Log.e("WeatherWidgetConfig", "Error updating widget", e)
		}
	}
	
	private fun createWidgetIdMapping(appWidgetId: Int) {
		// Store the widget ID for later reference by the Glance widget
		val mappingPrefs = getSharedPreferences("widget_id_mapping", MODE_PRIVATE)
		val editor = mappingPrefs.edit()
		
		// Store with a known key pattern - create a unique identifier
		editor.putInt("appwidget_$appWidgetId", appWidgetId)
		
		// Also store the reverse mapping for easier lookup
		editor.putInt("widget_for_id_$appWidgetId", appWidgetId)
		editor.apply()
		
		Log.d("WeatherWidgetConfig", "Created widget ID mapping for: $appWidgetId")
		
		// Debug: log all current mappings
		val allMappings = mappingPrefs.all
		Log.d("WeatherWidgetConfig", "All current widget mappings: $allMappings")
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherWidgetConfigScreen(
	viewModel: WeatherWidgetConfigViewModel,
	onConfigurationComplete: (WidgetConfiguration) -> Unit,
	onCancel: () -> Unit
) {
	val configuration by viewModel.configuration.observeAsState(
		initial = WidgetConfiguration(appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID)
	)
	val wallpaperBitmap by viewModel.wallpaperBitmap.observeAsState()
	
	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp)
			.verticalScroll(rememberScrollState())
	) {
		// Preview Section
		WidgetPreviewCard(
			configuration = configuration,
			wallpaperBitmap = wallpaperBitmap
		)
		
		Spacer(modifier = Modifier.height(16.dp))
		
		// Configuration Section
		ConfigurationOptionsCard(
			configuration = configuration,
			onConfigurationChange = { newConfig ->
				viewModel.updateConfiguration(newConfig)
			}
		)
		
		Spacer(modifier = Modifier.height(24.dp))
		
		// Action Buttons
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(12.dp)
		) {
			OutlinedButton(
				onClick = onCancel,
				modifier = Modifier.weight(1f)
			) {
				Text(stringResource(R.string.cancel))
			}
			
			FilledTonalButton(
				onClick = { onConfigurationComplete(configuration) },
				modifier = Modifier.weight(1f)
			) {
				Text(stringResource(R.string.save))
			}
		}
	}
}

@Composable
fun WidgetPreviewCard(
	configuration: WidgetConfiguration,
	wallpaperBitmap: Bitmap?
) {
	// Debug logging
	LaunchedEffect(wallpaperBitmap) {
		Log.d("WeatherWidgetConfig", "Preview received wallpaper bitmap: ${wallpaperBitmap != null}")
		if (wallpaperBitmap != null) {
			Log.d("WeatherWidgetConfig", "Bitmap size: ${wallpaperBitmap.width}x${wallpaperBitmap.height}")
		}
	}
	
	Card(
		modifier = Modifier.fillMaxWidth(),
		elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
	) {
		Column(
			modifier = Modifier.padding(16.dp)
		) {
			Text(
				text = stringResource(R.string.preview),
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.SemiBold,
				modifier = Modifier.padding(bottom = 12.dp)
			)
			
			// Widget Preview with Wallpaper
			WidgetPreview(
				configuration = configuration,
				wallpaperBitmap = wallpaperBitmap
			)
		}
	}
}

@Composable
fun WidgetPreview(
	configuration: WidgetConfiguration,
	wallpaperBitmap: Bitmap?
) {
	// Debug logging
	LaunchedEffect(wallpaperBitmap) {
		Log.d("WeatherWidgetConfig", "WidgetPreview - wallpaper bitmap: ${wallpaperBitmap != null}")
	}
	
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(140.dp)
			.padding(8.dp)
			.clip(MaterialTheme.shapes.medium),
		contentAlignment = Alignment.Center
	) {
		// Background - either wallpaper or fallback
		if (wallpaperBitmap != null) {
			Image(
				bitmap = wallpaperBitmap.asImageBitmap(),
				contentDescription = "Wallpaper preview",
				modifier = Modifier.fillMaxSize(),
				contentScale = ContentScale.Crop
			)
			Log.d("WeatherWidgetConfig", "Displaying wallpaper image")
		} else {
			// Fallback gradient background to simulate wallpaper
			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(
						androidx.compose.ui.graphics.Brush.verticalGradient(
							colors = listOf(
								Color(0xFF1976D2),
								Color(0xFF42A5F5),
								Color(0xFF90CAF9)
							)
						)
					)
			)
			Log.d("WeatherWidgetConfig", "Displaying fallback gradient")
		}
		
		// Widget content overlay
		Column(
			modifier = Modifier
				.padding(12.dp),
			verticalArrangement = Arrangement.Center,
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			// Main temperature
			Text(
				text = "22°",
				fontSize = configuration.temperatureTextSize.sp,
				color = Color(configuration.textColor).copy(alpha = configuration.temperatureOpacity),
				fontWeight = FontWeight.Bold
			)
			
			// Feels like temperature (centered under main temp)
			Text(
				text = "26°",
				fontSize = configuration.feelsLikeTextSize.sp,
				color = Color(configuration.textColor).copy(alpha = configuration.feelsLikeOpacity),
				fontWeight = FontWeight.Normal
			)
			
			// Weather condition
			Text(
				text = "Sunny",
				fontSize = configuration.conditionTextSize.sp,
				color = Color(configuration.textColor).copy(alpha = configuration.conditionOpacity)
			)
		}
		
		// Show a small debug indicator
		Card(
			modifier = Modifier
				.align(Alignment.TopEnd)
				.padding(4.dp),
			colors = CardDefaults.cardColors(
				containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
			)
		) {
			Text(
				text = if (wallpaperBitmap != null) "Live wallpaper" else "Preview mode",
				fontSize = 8.sp,
				color = MaterialTheme.colorScheme.onSurface,
				modifier = Modifier.padding(4.dp)
			)
		}
	}
}

@Composable
fun ConfigurationOptionsCard(
	configuration: WidgetConfiguration,
	onConfigurationChange: (WidgetConfiguration) -> Unit
) {
	Card(
		modifier = Modifier.fillMaxWidth(),
		elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
	) {
		Column(
			modifier = Modifier.padding(16.dp)
		) {
			Text(
				text = stringResource(R.string.configuration),
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.SemiBold,
				modifier = Modifier.padding(bottom = 16.dp)
			)
			
			// Temperature Text Size
			ConfigSlider(
				label = stringResource(R.string.temperature_text_size),
				value = configuration.temperatureTextSize,
				onValueChange = { value ->
					onConfigurationChange(configuration.copy(temperatureTextSize = value))
				},
				valueRange = 18f..40f
			)
			
			Spacer(modifier = Modifier.height(12.dp))
			
			// Temperature Opacity
			ConfigSlider(
				label = stringResource(R.string.temperature_opacity),
				value = configuration.temperatureOpacity,
				onValueChange = { value ->
					onConfigurationChange(configuration.copy(temperatureOpacity = value))
				},
				valueRange = 0.1f..1f
			)
			
			Spacer(modifier = Modifier.height(16.dp))
			
			// Feels Like Text Size
			ConfigSlider(
				label = stringResource(R.string.feels_like_text_size),
				value = configuration.feelsLikeTextSize,
				onValueChange = { value ->
					onConfigurationChange(configuration.copy(feelsLikeTextSize = value))
				},
				valueRange = 10f..24f
			)
			
			Spacer(modifier = Modifier.height(12.dp))
			
			// Feels Like Opacity
			ConfigSlider(
				label = stringResource(R.string.feels_like_opacity),
				value = configuration.feelsLikeOpacity,
				onValueChange = { value ->
					onConfigurationChange(configuration.copy(feelsLikeOpacity = value))
				},
				valueRange = 0.1f..1f
			)
			
			Spacer(modifier = Modifier.height(16.dp))
			
			// Condition Text Size
			ConfigSlider(
				label = stringResource(R.string.condition_text_size),
				value = configuration.conditionTextSize,
				onValueChange = { value ->
					onConfigurationChange(configuration.copy(conditionTextSize = value))
				},
				valueRange = 8f..20f
			)
			
			Spacer(modifier = Modifier.height(12.dp))
			
			// Condition Opacity
			ConfigSlider(
				label = stringResource(R.string.condition_opacity),
				value = configuration.conditionOpacity,
				onValueChange = { value ->
					onConfigurationChange(configuration.copy(conditionOpacity = value))
				},
				valueRange = 0.1f..1f
			)
			
			Spacer(modifier = Modifier.height(16.dp))
			
			// Color Section
			ColorPickerSection(
				selectedColor = Color(configuration.textColor),
				customColorHex = configuration.customColorHex,
				onColorSelected = { color ->
					onConfigurationChange(configuration.copy(textColor = color.toArgb()))
				},
				onCustomColorChanged = { hex ->
					try {
						val color = Color(android.graphics.Color.parseColor(hex))
						onConfigurationChange(
							configuration.copy(
								customColorHex = hex,
								textColor = color.toArgb()
							)
						)
					} catch (e: IllegalArgumentException) {
						// Invalid hex color, keep current
					}
				}
			)
		}
	}
}

@Composable
fun ConfigSlider(
	label: String,
	value: Float,
	onValueChange: (Float) -> Unit,
	valueRange: ClosedFloatingPointRange<Float>
) {
	Column {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Text(
				text = label,
				style = MaterialTheme.typography.bodyMedium
			)
			Text(
				text = String.format("%.1f", value),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
		
		Slider(
			value = value,
			onValueChange = onValueChange,
			valueRange = valueRange,
			modifier = Modifier.fillMaxWidth()
		)
	}
}

@Composable
fun ColorPickerSection(
	selectedColor: Color,
	customColorHex: String,
	onColorSelected: (Color) -> Unit,
	onCustomColorChanged: (String) -> Unit
) {
	Column {
		Text(
			text = stringResource(R.string.text_color),
			style = MaterialTheme.typography.bodyMedium,
			modifier = Modifier.padding(bottom = 8.dp)
		)
		
		// Preset Color Buttons
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(8.dp)
		) {
			val colors = listOf(
				Color.White,
				Color.Black,
				Color(0xFFFF5252),
				Color(0xFF4CAF50),
				Color(0xFF2196F3),
				Color(0xFFFFEB3B),
				Color(0xFF9C27B0),
				Color(0xFFFF9800)
			)
			
			colors.forEach { color ->
				ColorPickerItem(
					color = color,
					isSelected = color.toArgb() == selectedColor.toArgb(),
					onColorSelected = { onColorSelected(color) }
				)
			}
		}
		
		Spacer(modifier = Modifier.height(12.dp))
		
		// Custom Hex Color Input
		OutlinedTextField(
			value = customColorHex,
			onValueChange = onCustomColorChanged,
			label = { Text(stringResource(R.string.custom_color_hex)) },
			placeholder = { Text("#FFFFFF") },
			keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
			modifier = Modifier.fillMaxWidth(),
			leadingIcon = {
				Box(
					modifier = Modifier
						.size(24.dp)
						.clip(MaterialTheme.shapes.small)
						.background(selectedColor)
				)
			},
			supportingText = {
				Text(
					text = stringResource(R.string.hex_color_format),
					style = MaterialTheme.typography.bodySmall
				)
			}
		)
	}
}

@Composable
fun ColorPickerItem(
	color: Color,
	isSelected: Boolean,
	onColorSelected: () -> Unit
) {
	Card(
		modifier = Modifier.size(40.dp),
		colors = CardDefaults.cardColors(containerColor = color),
		border = if (isSelected) {
			BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
		} else null,
		onClick = onColorSelected
	) {}
}

@Preview(showBackground = true)
@Composable
fun WeatherWidgetConfigScreenPreview() {
	AxleWeatherTheme {
		WeatherWidgetConfigScreen(
			viewModel = WeatherWidgetConfigViewModel(),
			onConfigurationComplete = {},
			onCancel = {}
		)
	}
}
