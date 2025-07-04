package coffee.axle.android.data.repository

import coffee.axle.android.data.api.ApiClient
import coffee.axle.android.data.model.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for weather data
 */
class WeatherRepository {
	
	private val weatherService = ApiClient.weatherService
	
	/**
	 * Fetch weather data from API
	 */
	suspend fun getWeatherData(): Result<WeatherResponse> = withContext(Dispatchers.IO) {
		try {
			val response = weatherService.getWeather()
			if (response.isSuccessful) {
				response.body()?.let { weatherData ->
					Result.success(weatherData)
				} ?: Result.failure(Exception("Empty response body"))
			} else {
				Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
			}
		} catch (e: Exception) {
			Result.failure(e)
		}
	}
}
