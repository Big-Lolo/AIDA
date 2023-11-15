package com.example.aida

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Locale


class AlarmActDisable : AppCompatActivity() {
    private var initialLeftMargin = 0
    private var initialTopMargin = 0
    @SuppressLint("ClickableViewAccessibility")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ACTIVIDAD_DISABLE", "Actividad ejecutandose11111")
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        keyguardManager.requestDismissKeyguard(this, null)

        setContentView(R.layout.activity_alarm_disable) // Asocia el layout XML a esta actividad


        val textViewFecha = findViewById<TextView>(R.id.fechass)
        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
        textViewFecha.text = dateFormat.format(currentDate)


        val buenasNochesInicio = 21
        val buenasNochesFin = 3
        val buenosDiasInicio = 4
        val buenosDiasFin = 12
        val buenasTardesInicio = 13
        val buenasTardesFin = 20
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val textViewSaludo = findViewById<TextView>(R.id.textView6)

        val saludo = when (currentHour) {
            in buenasNochesInicio..23, in 0..buenasNochesFin -> "Buenas noches!"
            in buenosDiasInicio..buenosDiasFin -> "Buenos días!"
            in buenasTardesInicio..buenasTardesFin -> "Hola buenas tardes!"
            else -> "Hola" // Manejo por defecto
        }
        textViewSaludo.text = saludo



        val slideButton = findViewById<Button>(R.id.slideButton)
        val miView = findViewById<View>(R.id.backgroundView)

        var initialRadius = slideButton.width / 2
        val maxRadius = resources.getDimensionPixelSize(R.dimen.max_radius)
        val minRadius = resources.getDimensionPixelSize(R.dimen.min_radius)

        slideButton.setOnTouchListener { _, event ->

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialRadius = slideButton.width / 2
                    miView.visibility = View.INVISIBLE
                    initialLeftMargin = slideButton.left
                    initialTopMargin = slideButton.top
                }
                MotionEvent.ACTION_MOVE -> {
                    val centerX = slideButton.width / 2
                    val centerY = slideButton.height / 2

                    val deltaX = event.x - centerX
                    val deltaY = event.y - centerY

                    val distance = Math.hypot(deltaX.toDouble(), deltaY.toDouble())
                    val growthFactor = 0.4 // Ajusta este valor según tu preferencia

                    val newRadius = (initialRadius + (distance * growthFactor)).toInt()
                        .coerceIn(minRadius, maxRadius)

                    val widthChange = newRadius * 2 - slideButton.width
                    val heightChange = newRadius * 2 - slideButton.height

                    val layoutParams = slideButton.layoutParams as FrameLayout.LayoutParams
                    layoutParams.width = newRadius * 2
                    layoutParams.height = newRadius * 2

                    val newLeftMargin = layoutParams.leftMargin - (widthChange / 2)
                    val newTopMargin = layoutParams.topMargin - (heightChange / 2)

                    layoutParams.leftMargin = newLeftMargin
                    layoutParams.topMargin = newTopMargin

                    slideButton.layoutParams = layoutParams
                    miView.visibility = View.VISIBLE
                }
                MotionEvent.ACTION_UP -> {
                    if (slideButton.width / 2 < maxRadius) {
                        val layoutParams = slideButton.layoutParams as FrameLayout.LayoutParams
                        layoutParams.width = minRadius * 2
                        layoutParams.height = minRadius * 2
                        layoutParams.leftMargin = initialLeftMargin
                        layoutParams.topMargin = initialTopMargin
                        slideButton.layoutParams = layoutParams
                        miView.visibility = View.INVISIBLE


                    }else{
                        val intent = Intent(this, AlarmReceiver::class.java)
                        intent.action = "TU_ACCION_DESACTIVAR"
                        sendBroadcast(intent)
                        this.finish()
                    }

                    initialRadius = slideButton.width / 2
                }
            }

            true
        }





        Log.d("ACTIVIDAD_DISABLE", "Actividad ejecutandose")

    }




}
