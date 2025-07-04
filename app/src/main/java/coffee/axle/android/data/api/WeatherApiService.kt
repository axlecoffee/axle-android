package coffee.axle.android.data.api

import coffee.axle.android.data.model.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET

/**
 * Axle Weather API service
 */
interface WeatherApiService {
	
	/**
	 * Get weather data for Ottawa (default location)
	 */
	@GET("weather")
	suspend fun getWeather(): Response<WeatherResponse>
}
