package hr.fer.projekt.taskmanager.model

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import hr.fer.projekt.taskmanager.alarm.AlarmBroadcastReceiver
import java.util.*


data class Obavijest(
    var idZadatak: String?,
    val datumVrijeme: String?,
    val napomena: String?,
    var zapoceto: Boolean = true,
    var alarmId: Int? = null,
    var naslov: String? = null,
    var kreirano: Long? = null,
    var idAlarm:String?=null
) {
    fun schedule(context: Context, date: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmBroadcastReceiver::class.java)
        intent.putExtra(AlarmBroadcastReceiver.RECURRING, false)
        intent.putExtra(AlarmBroadcastReceiver.TITLE, naslov)
        val alarmPendingIntent =
            PendingIntent.getBroadcast(
                context,
                alarmId!!,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar[Calendar.HOUR_OF_DAY] = datumVrijeme!!.split(" ")[1].split(":")[0].toInt()
        calendar[Calendar.MINUTE] = datumVrijeme.split(" ")[1].split(":")[1].toInt()
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        if (datumVrijeme != "") {
            calendar[Calendar.YEAR] = Integer.parseInt(datumVrijeme.split(" ")[0].split("/")[2])
            calendar[Calendar.MONTH] =
                Integer.parseInt(datumVrijeme.split(" ")[0].split("/")[0]) - 1
            calendar[Calendar.DAY_OF_MONTH] =
                Integer.parseInt(datumVrijeme.split(" ")[0].split("/")[1])
        } else {
            // if alarm time has already passed, increment day by 1
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar[Calendar.DAY_OF_MONTH] = calendar[Calendar.DAY_OF_MONTH] + 1
            }
        }

        /* String toastText = null;
        try {
            toastText = String.format("Alarm %s scheduled for %s at %02d:%02d", title, DayUtil.toDay(calendar.get(Calendar.DAY_OF_WEEK)), hour, minute, alarmId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();*/
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            alarmPendingIntent
        )

        this.zapoceto = true
    }

    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmBroadcastReceiver::class.java)
        val alarmPendingIntent =
            PendingIntent.getBroadcast(
                context, alarmId!!, intent, PendingIntent.FLAG_UPDATE_CURRENT
            )
        alarmManager.cancel(alarmPendingIntent)
        this.zapoceto = false

        /* String toastText = String.format("Alarm cancelled for %02d:%02d with id %d", hour, minute, alarmId);
        Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();*/
        //Log.i("cancel", toastText);
    }
}


var mapaVremena: Map<Int, List<String>> =
    mutableMapOf(
        1 to listOf("07:00"),
        2 to listOf("07:00", "23:00"),
        3 to listOf("07:00", "15:00", "23:00"),
        4 to listOf("07:00", "12:20", "17:40", "23:00"),
        5 to listOf("07:00", "11:00", "15:00", "19:00", "23:00"),
        6 to listOf("07:00", "10:10", "13:20", "16:35", "19:45", "23:00"),
        7 to listOf("07:00", "09:40", "12:20", "15:00", "17:40", "20:20", "23:00"),
        8 to listOf("07:00", "09:15", "11:30", "13:50", "16:05", "18:25", "20:40", "23:00"),
        9 to listOf(
            "07:00",
            "09:00",
            "11:00",
            "13:00",
            "15:00",
            "17:00",
            "19:00",
            "21:00",
            "23:00"
        ),
        10 to listOf(
            "07:00",
            "08:45",
            "10:30",
            "12:20",
            "14:05",
            "15:50",
            "17:40",
            "19:25",
            "21:10",
            "23:00"
        ),
        11 to listOf(
            "07:00",
            "08:35",
            "10:10",
            "11:45",
            "13:20",
            "15:00",
            "16:35",
            "18:10",
            "19:45",
            "21:20",
            "23:00"
        ),
        12 to listOf(
            "07:00",
            "08:25",
            "09:55",
            "11:20",
            "12:45",
            "14:15",
            "15:40",
            "17:10",
            "18:35",
            "20:05",
            "21:30",
            "23:00"
        ),
    )

