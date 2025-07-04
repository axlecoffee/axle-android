package coffee.axle.android.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coffee.axle.android.data.model.WeatherResponse
import coffee.axle.android.data.repository.WeatherRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for weather data
 */
class WeatherViewModel : ViewModel() {
	
	private val repository = WeatherRepository()
	
	private val _weatherData = MutableLiveData<WeatherResponse?>()
	val weatherData: LiveData<WeatherResponse?> = _weatherData
	
	private val _isLoading = MutableLiveData<Boolean>()
	val isLoading: LiveData<Boolean> = _isLoading
	
	private val _error = MutableLiveData<String?>()
	val error: LiveData<String?> = _error
	
	private val _apiStatus = MutableLiveData<String>()
	val apiStatus: LiveData<String> = _apiStatus
	
	init {
		loadWeatherData()
	}
	
	/**
	 * Load weather data from API
	 */
	fun loadWeatherData() {
		viewModelScope.launch {
			_isLoading.value = true
			_error.value = null
			
			repository.getWeatherData()
				.onSuccess { data ->
					_weatherData.value = data
					_apiStatus.value = "API Connected - Ottawa weather loaded successfully"
				}
				.onFailure { exception ->
					_error.value = exception.message
					_apiStatus.value = "API Error: ${exception.message}"
				}
			
			_isLoading.value = false
		}
	}
	
	/**
	 * Refresh weather data
	 */
	fun refreshWeatherData() {
		loadWeatherData()
	}
}
