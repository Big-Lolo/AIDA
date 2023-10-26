import android.content.Context
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Intent


class MyAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // Manejar la acción de la alarma aquí
    }
}

fun setAlarm(context: Context, requestCode: Int, timeInMillis: Long) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, MyAlarmReceiver::class.java) // Reemplaza MyAlarmReceiver con tu receptor
    val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)

    // Programa la alarma
    alarmManager.set(AlarmManager.RTC, timeInMillis, pendingIntent)
}


fun cancelAlarm(context: Context, requestCode: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, MyAlarmReceiver::class.java) // Reemplaza MyAlarmReceiver con tu receptor
    val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)

    // Cancela la alarma
    alarmManager.cancel(pendingIntent)
}


//Con esto basicamente habra que crear un gestor de alarmas con una pantalla de desactivacion de alarmas en la misma app
//Se puede hacer que ese boton del + se vuelva un ajuste que te abra el fragment con el listado de alarmas.

//Con el calendario puede que no haga falta tanto por la api del google calendar