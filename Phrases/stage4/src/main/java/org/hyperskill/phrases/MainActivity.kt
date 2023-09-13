package org.hyperskill.phrases

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import org.hyperskill.phrases.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

const val CHANNEL_ID = "org.hyperskill.phrases"

class MainActivity : AppCompatActivity() {
    private lateinit var appDatabase: AppDatabase
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appDatabase = (application as AppDatabaseWrapper).database

        createNotificationChannel()

        val adapter = PhraseRecyclerAdapter(appDatabase.getPhraseDao().getAll()) // create adapter instance and fill it with phrases
        adapter.setPhraseDeleteCallback(object : PhraseRecyclerAdapter.PhraseDeleteCallback {

            override fun onDeletePhrase(phrase: Phrase) {
                appDatabase.getPhraseDao().delete(phrase)

                // creating the same Intent (and pending intent) as we send when set a reminder inside OnTimeSetListener
                val intent = Intent(this@MainActivity, Receiver::class.java).putExtra("phrase",phrase.phrase)
                val pendingIntent = PendingIntent.getBroadcast(applicationContext,0,intent,PendingIntent.FLAG_CANCEL_CURRENT)
                (getSystemService(ALARM_SERVICE) as AlarmManager).cancel(pendingIntent) // cancel reminder with deleted phrase

                // checking empty database case (may be it's better to remove this if and delete
                // reminder for particular phrase (if it was set) but it is what it is)
                if (appDatabase.getPhraseDao().getAll().isEmpty()) {
                    (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()
                    Toast.makeText(this@MainActivity, "Reminder cancelled", Toast.LENGTH_SHORT).show()
                    binding.reminderTextView.text = "No reminder set"
                }

                // updating recyclerView after deleting phrase from db
                adapter.data =
                    appDatabase.getPhraseDao().getAll().toMutableList()
            }
        })

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.addButton.setOnClickListener {
            val addDialogView = this.layoutInflater.inflate(R.layout.add_phrase_dialog, null, false)
            AlertDialog.Builder(this)
                .setTitle("Add phrase")
                .setView(addDialogView)
                .setPositiveButton("ADD") { dialog, btn ->
                    if (addDialogView.findViewById<EditText>(R.id.editText).text.isNotEmpty()) {
                        val phrase = Phrase(
                            addDialogView.findViewById<EditText>(R.id.editText).text.toString()
                        )
                        appDatabase.getPhraseDao().insert(phrase) // insert new phrase to db
                        adapter.data =                            // updating recycler view
                            appDatabase.getPhraseDao().getAll().toMutableList()
                    } else Toast.makeText(this, "Input is empty", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("CANCEL", null)
                .show()
        }

        binding.reminderTextView.setOnClickListener {
            val formatter = SimpleDateFormat("HH:mm", Locale.ROOT) // enable formatting time in Millis into String
            val calendar = Calendar.getInstance() // instance of Calendar class to use with TimePicker

            // timepicker val is listener, that enabled when "OK" button inside timepicker dialog pressed
            if (appDatabase.getPhraseDao().getAll().isNotEmpty()) {
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
                    intent.putExtra("phrase", appDatabase.getPhraseDao().get().phrase) // putting random phrase of phrase list
                    val pendingIntent = PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT) // pending intent that get broadcast
                    val am: AlarmManager = getSystemService(ALARM_SERVICE) as AlarmManager // get alarm manager instance
                    am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent) // setting repeating notifications via alarm manager
                }

                TimePickerDialog(this,timepicker, calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),true).show()
            } else {
                Toast.makeText(this, "Add some phrases to list", Toast.LENGTH_LONG).show()
            }

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
