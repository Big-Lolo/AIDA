package com.example.aida

import android.content.Context
import android.icu.util.Calendar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aida.utils.AlarmDetails
import java.text.SimpleDateFormat
import java.util.Locale

class HorizontalAdapter(private val context: Context, private val dataList: List<AlarmDetails>) :
    RecyclerView.Adapter<HorizontalAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewHora: TextView = itemView.findViewById(R.id.Hora)
        val textViewFecha: TextView = itemView.findViewById(R.id.fechass)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_horizontal, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Configurar elementos de vista según los datos de dataList
        val alarm = dataList[position]
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, alarm.year)
        calendar.set(Calendar.MONTH, alarm.month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, alarm.day)
        calendar.set(Calendar.HOUR_OF_DAY, alarm.hour)
        calendar.set(Calendar.MINUTE, alarm.minute)
        val dateFormat = SimpleDateFormat("dd, MMM yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(calendar.time)
        val timeFormat2 = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formattedTime2 = timeFormat2.format(calendar.time)
        holder.textViewFecha.text = formattedDate
        holder.textViewHora.text = formattedTime2

        // Ejemplo: holder.itemView.findViewById<TextView>(R.id.textViewAlarmName).text = alarm.name
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}