package coffee.axle.android.ui.activity

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import coffee.axle.android.databinding.ActivityMainBinding
import coffee.axle.android.ui.adapter.HourlyForecastAdapter
import coffee.axle.android.ui.viewmodel.WeatherViewModel
import coffee.axle.android.widget.WeatherAppWidget
import coffee.axle.android.widget.WeatherCacheManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Main activity displaying weather information
 */
class MainActivity : AppCompatActivity() {
	
	private lateinit var binding: ActivityMainBinding
	private val viewModel: WeatherViewModel by viewModels()
	private lateinit var hourlyAdapter: HourlyForecastAdapter
	private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		
		setupViews()
		observeViewModel()
		
		// Refresh weather cache when app opens
		refreshWeatherCache()
	}
	
	private fun setupViews() {
		// Setup RecyclerView
		hourlyAdapter = HourlyForecastAdapter()
		binding.rvHourlyForecast.apply {
			adapter = hourlyAdapter
			layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
		}
		
		// Setup swipe to refresh
		binding.swipeRefreshLayout.setOnRefreshListener {
			viewModel.refreshWeatherData()
		}
	}
	
	private fun observeViewModel() {
		viewModel.isLoading.observe(this) { isLoading ->
			binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
			binding.swipeRefreshLayout.isRefreshing = isLoading
		}
		
		viewModel.error.observe(this) { error ->
			binding.tvError.visibility = if (error != null) View.VISIBLE else View.GONE
			binding.tvError.text = error
		}
		
		viewModel.apiStatus.observe(this) { status ->
			binding.tvApiStatus.text = status
		}
		
		viewModel.weatherData.observe(this) { weatherResponse ->
			weatherResponse?.let { response ->
				updateUI(response)
			}
		}
	}
	
	private fun updateUI(weatherResponse: coffee.axle.android.data.model.WeatherResponse) {
		val currentWeather = weatherResponse.data.current.firstOrNull()
		
		currentWeather?.let { current ->
			binding.cardCurrentWeather.visibility = View.VISIBLE
			
			binding.tvTemperature.text = "${current.temperature}${current.temperatureUnit}"
			binding.tvCondition.text = current.condition
			binding.tvFeelsLike.text = "Feels like ${current.feelsLike}${current.feelsLikeUnit}"
			binding.tvHumidity.text = "${current.humidity.toInt()}${current.humidityUnit}"
			binding.tvWind.text = "${current.windSpeed} ${current.windSpeedUnit} ${current.windDirection}"
			binding.tvPressure.text = "${current.pressure} ${current.pressureUnit}"
		}
		
		// Update hourly forecast
		if (weatherResponse.data.hourly.isNotEmpty()) {
			binding.cardHourlyForecast.visibility = View.VISIBLE
			hourlyAdapter.updateData(weatherResponse.data.hourly)
		}
		
		// Hide error if data loaded successfully
		binding.tvError.visibility = View.GONE
	}
	
	private fun refreshWeatherCache() {
		mainScope.launch {
			try {
				val cacheManager = WeatherCacheManager.getInstance(this@MainActivity)
				cacheManager.refreshWeatherData()
				WeatherAppWidget.updateAllWidgets(this@MainActivity)
			} catch (e: Exception) {
				android.util.Log.e("MainActivity", "Failed to refresh weather cache", e)
			}
		}
	}
}
