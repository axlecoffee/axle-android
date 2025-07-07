package coffee.axle.android.data.model

import com.google.gson.annotations.SerializedName

/**
 * Weather response from Axle API
 */
data class WeatherResponse(
	@SerializedName("location") val location: Location,
	@SerializedName("timestamp") val timestamp: String,
	@SerializedName("data") val data: WeatherData,
	@SerializedName("metadata") val metadata: Metadata
)

data class Location(
	@SerializedName("latitude") val latitude: Double,
	@SerializedName("longitude") val longitude: Double,
	@SerializedName("coordinates") val coordinates: String
)

data class WeatherData(
	@SerializedName("current") val current: List<CurrentWeather>,
	@SerializedName("hourly") val hourly: List<HourlyWeather>,
	@SerializedName("forecast") val forecast: Forecast,
	@SerializedName("alerts") val alerts: List<String>,
	@SerializedName("sources") val sources: Sources
)

data class CurrentWeather(
	@SerializedName("temperature")
	val temperature: Int,
	@SerializedName("temperatureUnit")
	val temperatureUnit: String,
	@SerializedName("feelsLike")
	val feelsLike: Int,
	@SerializedName("feelsLikeUnit")
	val feelsLikeUnit: String,
	@SerializedName("condition")
	val condition: String,
	@SerializedName("humidity")
	val humidity: Double,
	@SerializedName("humidityUnit")
	val humidityUnit: String,
	@SerializedName("windSpeed")
	val windSpeed: Double,
	@SerializedName("windDirection")
	val windDirection: String,
	@SerializedName("windSpeedUnit")
	val windSpeedUnit: String,
	@SerializedName("pressure")
	val pressure: Double,
	@SerializedName("pressureUnit")
	val pressureUnit: String,
	@SerializedName("visibility")
	val visibility: Int,
	@SerializedName("visibilityUnit")
	val visibilityUnit: String,
	@SerializedName("stationName")
	val stationName: String,
	@SerializedName("observationTime")
	val observationTime: String
)

data class HourlyWeather(
	@SerializedName("time")
	val time: String,
	@SerializedName("temperature")
	val temperature: Int,
	@SerializedName("temperatureUnit")
	val temperatureUnit: String,
	@SerializedName("condition")
	val condition: String,
	@SerializedName("humidity")
	val humidity: Int,
	@SerializedName("precipitationProbability")
	val precipitationProbability: Int,
	@SerializedName("windSpeed")
	val windSpeed: Double,
	@SerializedName("windDirection")
	val windDirection: Int
)

data class Forecast(
	@SerializedName("7day")
	val sevenDay: List<SevenDayForecast>,
	@SerializedName("14day")
	val fourteenDay: List<FourteenDayForecast>
)

data class SevenDayForecast(
	@SerializedName("period")
	val period: String,
	@SerializedName("temperature")
	val temperature: Int,
	@SerializedName("temperatureType")
	val temperatureType: String,
	@SerializedName("temperatureUnit")
	val temperatureUnit: String,
	@SerializedName("condition")
	val condition: String,
	@SerializedName("precipitationChance")
	val precipitationChance: Int?,
	@SerializedName("summary")
	val summary: String
)

data class FourteenDayForecast(
	@SerializedName("date")
	val date: String,
	@SerializedName("temperatureMax")
	val temperatureMax: Int,
	@SerializedName("temperatureMin")
	val temperatureMin: Int,
	@SerializedName("temperatureUnit")
	val temperatureUnit: String,
	@SerializedName("condition")
	val condition: String,
	@SerializedName("precipitationProbability")
	val precipitationProbability: Int,
	@SerializedName("windSpeedMax")
	val windSpeedMax: Double,
	@SerializedName("uvIndexMax")
	val uvIndexMax: Double
)

data class Sources(
	@SerializedName("primary")
	val primary: String,
	@SerializedName("secondary")
	val secondary: List<String>,
	@SerializedName("confidence")
	val confidence: Double
)

data class Metadata(
	@SerializedName("note")
	val note: String,
	@SerializedName("capabilities")
	val capabilities: Map<String, String>
)
