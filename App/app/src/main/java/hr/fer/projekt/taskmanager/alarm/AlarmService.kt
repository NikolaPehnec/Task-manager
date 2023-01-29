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
import android.media.MediaPlayer
import androidx.core.app.NotificationCompat

class AlarmService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    override fun onCreate() {
        super.onCreate()
        val defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        mediaPlayer = MediaPlayer.create(this, defaultRingtoneUri)
        mediaPlayer!!.isLooping = true
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val notificationIntent = Intent(this, RingActivity::class.java)
        notificationIntent.putExtra(
            "lijek",
            intent.getStringExtra(AlarmBroadcastReceiver.Companion.TITLE)
        )
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val alarmTitle =
            String.format("%s Alarm", intent.getStringExtra(AlarmBroadcastReceiver.Companion.TITLE))
        val notification: Notification = NotificationCompat.Builder(this, App.Companion.CHANNEL_ID)
            .setContentTitle(alarmTitle)
            .setContentText("Podsjetnik za zadatak: " + intent.getStringExtra(AlarmBroadcastReceiver.Companion.TITLE))
            .setSmallIcon(R.drawable.ic_alarm_black_24dp)
            .setContentIntent(pendingIntent)
            .build()

        // mediaPlayer.start();
        val pattern = longArrayOf(0, 100, 1000)
        vibrator!!.vibrate(pattern, 0)
        startForeground(1, notification)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer!!.stop()
        vibrator!!.cancel()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}