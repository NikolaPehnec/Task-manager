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
import androidx.room.Entity
import java.util.*

@Entity(tableName = "alarm_table")
class Alarm(
    @field:PrimaryKey var alarmId: Int,
    val hour: Int,
    val minute: Int,
    val year: Int,
    val month: Int,
    val day: Int,
    val title: String,
    var created: Long,
    var isStarted: Boolean,
    val isRecurring: Boolean
) {

    fun schedule(context: Context, date: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmBroadcastReceiver::class.java)
        intent.putExtra(AlarmBroadcastReceiver.Companion.RECURRING, isRecurring)
        intent.putExtra(AlarmBroadcastReceiver.Companion.TITLE, title)
        val alarmPendingIntent =
            PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar[Calendar.HOUR_OF_DAY] = hour
        calendar[Calendar.MINUTE] = minute
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        if (date != "") {
            calendar[Calendar.YEAR] = year
            calendar[Calendar.MONTH] = month - 1
            calendar[Calendar.DAY_OF_MONTH] = day
        } else {
            // if alarm time has already passed, increment day by 1
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar[Calendar.DAY_OF_MONTH] = calendar[Calendar.DAY_OF_MONTH] + 1
            }
        }
        if (!isRecurring) {
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
        } else {
            /* String toastText = String.format("Recurring Alarm %s scheduled for %s at %02d:%02d", title, getRecurringDaysText(), hour, minute, alarmId);
            Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();*/
            val RUN_DAILY = (24 * 60 * 60 * 1000).toLong()
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                RUN_DAILY,
                alarmPendingIntent
            )
        }
        isStarted = true
    }

    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmBroadcastReceiver::class.java)
        val alarmPendingIntent =
            PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.cancel(alarmPendingIntent)
        isStarted = false

        /* String toastText = String.format("Alarm cancelled for %02d:%02d with id %d", hour, minute, alarmId);
        Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();*/
        //Log.i("cancel", toastText);
    }

    override fun toString(): String {
        return "Alarm{" +
                "alarmId=" + alarmId +
                '}'
    }
}