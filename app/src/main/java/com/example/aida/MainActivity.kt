package com.example.aida

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import com.example.aida.databinding.ActivityMainBinding
import com.example.aida.utils.AlarmDetails


class MainActivity : AppCompatActivity(), Home.OnHomeInteractionListener {

    private lateinit var binding : ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //val serviceIntent = Intent(this, VoiceRecognitionService::class.java)
        //ContextCompat.startForegroundService(this, serviceIntent)
        createNotificationChannel()

        //Para llamar al servicio background que tiene la activity de la desactivacion de alarma.




        Thread.sleep(2000)
        installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(Home())



        binding.bottomNavigationView.setOnItemSelectedListener {

            when(it.itemId){
                R.id.home -> replaceFragment(Home())
                R.id.chat -> replaceFragment(chat())
                R.id.logs -> replaceFragment(logs())
                R.id.settings -> replaceFragment(settings())
                else ->{
                }
            }

            true
        }
    }

    fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()

        // Verifica si el fragmento es una instancia de Home y luego llama a changeDate()
        if (fragment is Home) {
        }
    }


    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            val name : CharSequence = "Aida's Notification"
            val description = "Notification of AIDA"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("channelid", name, importance)
            channel.description = description
            val notificationManager = getSystemService(NotificationManager::class.java)

            notificationManager.createNotificationChannel(channel)
        }
    }
    override fun onAlarmButtonClicked(submenu: Boolean) {
        replaceFragment(AlarmFragment(submenu))
    }

    override fun onClockAlarmClicked(data: AlarmDetails) {
        replaceFragment(AlarmEditFragment(data))
    }

    override fun onReturn2Home() {
        replaceFragment(Home())
    }

    override fun openMusicSource() {
        replaceFragment(toneSelector())
    }

    override fun openMelodySelector(){
        replaceFragment(MelodySelectorIt())
    }



}

