package org.hyperskill.phrases

import android.app.*

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import org.hyperskill.phrases.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

const val CHANNEL_ID = "org.hyperskill.phrases"

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        createNotificationChannel()

        val phraseList = mutableListOf(
            Phrase("Just bodies"),
            Phrase("What the hell are you!?"),
            Phrase("To strive, to seek, to find and not to yield")
        )

        val adapter = PhraseRecyclerAdapter(phraseList)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

       binding.someButton.setOnClickListener {
           val intent = Intent(this, Receiver::class.java)
           val pendingIntent = PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT)
           val am: AlarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
           am.set(AlarmManager.RTC_WAKEUP, Calendar.getInstance().timeInMillis, pendingIntent)
       }

        binding.reminderTextView.setOnClickListener {
            val formatter = SimpleDateFormat("HH:mm", Locale.ROOT) // enable formatting time in Millis into String
            val calendar = Calendar.getInstance() // instance of Calendar class to use with TimePicker

            // timepicker val is listener, that enabled when "OK" button inside timepicker dialog pressed
            val timepicker = TimePickerDialog.OnTimeSetListener { pickerDialog, hourOfDay, minute ->

                calendar.apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay) // set picked hours in our calendar instance
                    set(Calendar.MINUTE, minute) // set picked minutes in our calendar instance
                }

                if (calendar.timeInMillis < Calendar.getInstance().timeInMillis) { // case when picked time lower then current
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
                binding.reminderTextView.text = "Reminder set for ${formatter.format(calendar.time)}" // set required text with appropriate time in our "reminder text view"

                val intent = Intent(this, Receiver::class.java) // intent, that should trigger our Receiver class
                intent.putExtra("phrase", phraseList[kotlin.random.Random.nextInt(0,3)].phraseText) // putting random phrase of phrase list
                val pendingIntent = PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT) // pending intent that get broadcast
                val am: AlarmManager = getSystemService(ALARM_SERVICE) as AlarmManager // get alarm manager instance
                am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent) // setting repeating notifications via alarm manager

            }

            TimePickerDialog(this,timepicker, calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),true).show()
        }

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Phrase shower"
            val descriptionText = "Show the phrase of the day"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
