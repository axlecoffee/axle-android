package coffee.axle.android.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * API client for Axle Weather API
 */
object ApiClient {
	
	private const val BASE_URL = "https://api.axle.coffee/api/"
	
	private val loggingInterceptor = HttpLoggingInterceptor().apply {
		level = HttpLoggingInterceptor.Level.BODY
	}
	
	private val okHttpClient = OkHttpClient.Builder()
		.addInterceptor(loggingInterceptor)
		.connectTimeout(30, TimeUnit.SECONDS)
		.readTimeout(30, TimeUnit.SECONDS)
		.build()
	
	private val retrofit = Retrofit.Builder()
		.baseUrl(BASE_URL)
		.client(okHttpClient)
		.addConverterFactory(GsonConverterFactory.create())
		.build()
	
	val weatherService: WeatherApiService = retrofit.create(WeatherApiService::class.java)
}
