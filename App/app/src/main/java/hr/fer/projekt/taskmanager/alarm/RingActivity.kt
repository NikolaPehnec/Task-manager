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
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.ImageView
import java.util.*

class RingActivity : AppCompatActivity() {
    var dismiss: Button? = null
    var snooze: Button? = null
    var clock: ImageView? = null
    var lijek: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ring_activity)
        dismiss = findViewById(R.id.activity_ring_dismiss)
        snooze = findViewById(R.id.activity_ring_snooze)
        clock = findViewById(R.id.activity_ring_clock)
        lijek = findViewById(R.id.lijeknaziv)
        val lijekNaziv = intent.getStringExtra("lijek")
        lijek!!.text = lijekNaziv
        dismiss!!.setOnClickListener(View.OnClickListener {
            val intentService = Intent(applicationContext, AlarmService::class.java)
            applicationContext.stopService(intentService)
            finish()
        })
        snooze!!.setOnClickListener(View.OnClickListener {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.MINUTE, 10)
            val alarm = Alarm(
                Random().nextInt(Int.MAX_VALUE),
                calendar[Calendar.HOUR_OF_DAY],
                calendar[Calendar.MINUTE],
                0, 0, 0,
                "Snooze",
                System.currentTimeMillis(),
                true,
                false
            )
            alarm.schedule(applicationContext, "")
            val intentService = Intent(applicationContext, AlarmService::class.java)
            applicationContext.stopService(intentService)
            finish()
        })
        animateClock()
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return super.onCreateView(name, context, attrs)
    }

    private fun animateClock() {
        val rotateAnimation = ObjectAnimator.ofFloat(clock, "rotation", 0f, 20f, 0f, -20f, 0f)
        rotateAnimation.repeatCount = ValueAnimator.INFINITE
        rotateAnimation.duration = 800
        rotateAnimation.start()
    }
}