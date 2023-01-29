package hr.fer.projekt.taskmanager.alarm

import androidx.room.PrimaryKey
import android.app.AlarmManager
import android.content.Intent
import hr.fer.projekt.taskmanager.alarm.AlarmBroadcastReceiver
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.widget.Toast
import hr.fer.projekt.taskmanager.alarm.AlarmService
import android.os.Build
import android.os.Vibrator
import android.media.RingtoneManager
import hr.fer.projekt.taskmanager.alarm.RingActivity
import hr.fer.projekt.taskmanager.alarm.App
import hr.fer.projekt.taskmanager.R
import android.os.IBinder
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.os.Bundle
import hr.fer.projekt.taskmanager.alarm.Alarm
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import java.util.*

class AlarmBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            val toastText = String.format("Alarm Reboot")
            Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
            //startRescheduleAlarmsService(context);
        } else {
            /*String toastText = String.format("Alarm Received");
            Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();*/
            if (!intent.getBooleanExtra(RECURRING, false)) {
                startAlarmService(context, intent)
            }
            run {
                if (alarmIsToday(intent)) {
                    startAlarmService(context, intent)
                }
            }
        }
    }

    private fun alarmIsToday(intent: Intent): Boolean {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val today = calendar[Calendar.DAY_OF_WEEK]
        when (today) {
            Calendar.MONDAY -> {
                return if (intent.getBooleanExtra(MONDAY, false)) true else false
            }
            Calendar.TUESDAY -> {
                return if (intent.getBooleanExtra(TUESDAY, false)) true else false
            }
            Calendar.WEDNESDAY -> {
                return if (intent.getBooleanExtra(WEDNESDAY, false)) true else false
            }
            Calendar.THURSDAY -> {
                return if (intent.getBooleanExtra(THURSDAY, false)) true else false
            }
            Calendar.FRIDAY -> {
                return if (intent.getBooleanExtra(FRIDAY, false)) true else false
            }
            Calendar.SATURDAY -> {
                return if (intent.getBooleanExtra(SATURDAY, false)) true else false
            }
            Calendar.SUNDAY -> {
                return if (intent.getBooleanExtra(SUNDAY, false)) true else false
            }
        }
        return false
    }

    private fun startAlarmService(context: Context, intent: Intent) {
        val intentService = Intent(context, AlarmService::class.java)
        intentService.putExtra(TITLE, intent.getStringExtra(TITLE))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intentService)
        } else {
            context.startService(intentService)
        }
    }

    companion object {
        const val MONDAY = "MONDAY"
        const val TUESDAY = "TUESDAY"
        const val WEDNESDAY = "WEDNESDAY"
        const val THURSDAY = "THURSDAY"
        const val FRIDAY = "FRIDAY"
        const val SATURDAY = "SATURDAY"
        const val SUNDAY = "SUNDAY"
        const val RECURRING = "RECURRING"
        const val TITLE = "TITLE"
    }
}