package coffee.axle.android.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coffee.axle.android.data.model.HourlyWeather
import coffee.axle.android.databinding.ItemHourlyForecastBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for hourly weather forecast
 */
class HourlyForecastAdapter : RecyclerView.Adapter<HourlyForecastAdapter.ViewHolder>() {
	
	private var hourlyData = emptyList<HourlyWeather>()
	
	fun updateData(data: List<HourlyWeather>) {
		hourlyData = data.take(12)
		notifyDataSetChanged()
	}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val binding = ItemHourlyForecastBinding.inflate(
			LayoutInflater.from(parent.context),
			parent,
			false
		)
		return ViewHolder(binding)
	}
	
	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(hourlyData[position])
	}
	
	override fun getItemCount() = hourlyData.size
	
	class ViewHolder(private val binding: ItemHourlyForecastBinding) : RecyclerView.ViewHolder(binding.root) {
		
		fun bind(hourlyWeather: HourlyWeather) {
			// Format time
			val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
			val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
			
			try {
				val date = inputFormat.parse(hourlyWeather.time)
				binding.tvTime.text = timeFormat.format(date ?: Date())
			} catch (e: Exception) {
				binding.tvTime.text = hourlyWeather.time.takeLast(5) // Fallback
			}
			
			binding.tvHourlyTemp.text = "${hourlyWeather.temperature}°"
			binding.tvHourlyCondition.text = hourlyWeather.condition
		}
	}
}
