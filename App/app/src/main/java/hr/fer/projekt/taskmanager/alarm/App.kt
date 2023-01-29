package hr.fer.projekt.taskmanager.alarm

import androidx.room.PrimaryKey
import android.content.Intent
import hr.fer.projekt.taskmanager.alarm.AlarmBroadcastReceiver
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
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.os.Bundle
import hr.fer.projekt.taskmanager.alarm.Alarm
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.*

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannnel()
    }

    private fun createNotificationChannnel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        const val CHANNEL_ID = "ALARM_SERVICE_CHANNEL"
    }
}