package com.twogentle.wall.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.twogentle.wall.R
import com.twogentle.wall.activity.AuthenticationActivity
import com.twogentle.wall.activity.PolicyActivity
import com.twogentle.wall.activity.SelectCollectionActivity
import com.twogentle.wall.extras.ExtraFunctions

class SettingsFragment : Fragment() {

    private lateinit var viewParent: View

    private lateinit var nameValue: String
    private lateinit var emailValue: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        viewParent = view

        val profileNameView = view.findViewById<View>(R.id.profileCardName)
        val profileNameTextView = profileNameView.findViewById<TextView>(R.id.profileCardItemValue)
        val profileEmailView = view.findViewById<View>(R.id.profileCardEmail)
        val profileEmailTextView =
            profileEmailView.findViewById<TextView>(R.id.profileCardItemValue)
        val profileEmailTitleTextView =
            profileEmailView.findViewById<TextView>(R.id.profileCardItemTitle)
        profileEmailTitleTextView.text = context!!.getString(R.string.e_mail)

        val profileNameTitleTextView =
            profileNameView.findViewById<TextView>(R.id.profileCardItemTitle)
        profileNameTitleTextView.text = context!!.getString(R.string.name)


        val settingLogoutTextView = view.findViewById<TextView>(R.id.settingsLogout)

        val progressBarView = view.findViewById<View>(R.id.settingsProgressBar)

        progressBarView.visibility = View.VISIBLE

        val query = FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().uid!!)

        query.get().addOnCompleteListener {
            if (it.isSuccessful) {
                profileNameTextView.text = it.result!!["name"].toString()
                profileEmailTextView.text = it.result!!["email"].toString()
                nameValue = it.result!!["name"].toString()
                emailValue = it.result!!["email"].toString()
            }
            progressBarView.visibility = View.GONE
        }

        settingLogoutTextView.setOnClickListener {

            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(context, AuthenticationActivity::class.java))
            activity!!.finish()

        }


        return view
    }

    override fun onStart() {
        super.onStart()

        /*val preferences = context!!.getSharedPreferences("settings", Context.MODE_PRIVATE)
        autoChangeWallpaperTimeSwitch.isChecked = preferences.getBoolean("autoWallpaper", true)
        autoChangeWallpaperTimeTextView.text =
            context!!.getSharedPreferences("settings", Context.MODE_PRIVATE)
                .getString("autoWallpaperTime", "00:00")
        if (!autoChangeWallpaperTimeSwitch.isChecked)
            autoChangeWallpaperTimeTextView.apply {
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }*/

        val settingsInArtInstagramTextView = viewParent.findViewById<TextView>(R.id.settingsInArtInstagram)
        val settingsFavCollections = viewParent.findViewById<TextView>(R.id.settingsChooseCollections)
        val settingRateUs = viewParent.findViewById<TextView>(R.id.settingsRateUs)
        val settingShareIt = viewParent.findViewById<TextView>(R.id.settingsShareIt)
        val settingContactUs = viewParent.findViewById<TextView>(R.id.settingsContactUs)
        val settingAbout = viewParent.findViewById<TextView>(R.id.settingsAbout)
        val settingPrivacyPolicy = viewParent.findViewById<TextView>(R.id.settingsPrivacyPolicy)

        settingsInArtInstagramTextView.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.inart_instagram_url))))
        }

        settingsFavCollections.setOnClickListener {
            activity!!.startActivity(Intent(activity, SelectCollectionActivity::class.java))
        }

        settingAbout.setOnClickListener {
            val intent = Intent(activity, PolicyActivity::class.java)
            intent.putExtra("type", "about")
            activity!!.startActivity(intent)
        }

        settingPrivacyPolicy.setOnClickListener {
            val intent = Intent(activity, PolicyActivity::class.java)
            intent.putExtra("type", "privacyPolicy")
            activity!!.startActivity(intent)
        }

        settingContactUs.setOnClickListener {
            val intent = Intent(activity, PolicyActivity::class.java)
            intent.putExtra("type", "contactUs")
            intent.putExtra("email", emailValue)
            intent.putExtra("name", nameValue)
            activity!!.startActivity(intent)
        }

        settingShareIt.setOnClickListener {
            ExtraFunctions.shareApp(context!!)
        }

        settingRateUs.setOnClickListener {
            ExtraFunctions.openPlayStorePage(context!!)
        }


        /*autoChangeWallpaperTimeSwitch.setOnCheckedChangeListener { _, isChecked ->

            var sharedPreferences = context!!.getSharedPreferences("settings", Context.MODE_PRIVATE)
            var editor = sharedPreferences.edit()
            editor.putBoolean("autoWallpaper", isChecked)
            editor.apply()

            if (isChecked) {
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)

                val timePickerDialog = TimePickerDialog(
                    context,
                    TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                        val time = "$selectedHour : $selectedMinute"
                        autoChangeWallpaperTimeTextView.text = time
                        notificationWallpaper(selectedHour, selectedMinute)

                        sharedPreferences =
                            context!!.getSharedPreferences("settings", Context.MODE_PRIVATE)
                        editor = sharedPreferences.edit()
                        editor.putString("autoWallpaperTime", time)
                        editor.putInt("autoWallpaperHour", hour)
                        editor.putInt("autoWallpaperMinute", minute)
                        editor.apply()

                    }, hour, minute, true
                )
                timePickerDialog.show()
                autoChangeWallpaperTimeTextView.apply {
                    paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }


            } else {
                autoChangeWallpaperTimeTextView.apply {
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
                editor = sharedPreferences.edit()
                editor.putBoolean("autoWallpaper", isChecked)
                editor.apply()

                val alarmManager = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val alarmIntent = Intent(context, WallpaperNotificationReceiver::class.java)
                val pendingIntent =
                    PendingIntent.getBroadcast(context, 0, alarmIntent, 0)
                alarmManager.cancel(pendingIntent)
            }
        }*/


    }

    /*private fun cancelNotification() {
        val alarmManager = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, WallpaperAlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmIntent.data = Uri.parse("custom://" + System.currentTimeMillis())
        alarmManager.cancel(pendingIntent)
    }

    private fun notificationForWallpaper(hour: Int, minute: Int) {
        val alarmManager = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, WallpaperAlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmIntent.data = Uri.parse("custom://" + System.currentTimeMillis())
        alarmManager.cancel(pendingIntent)

        val startTime = Calendar.getInstance()
        val now = Calendar.getInstance()
        startTime.set(Calendar.HOUR_OF_DAY, hour)
        startTime.set(Calendar.MINUTE, minute)
        startTime.set(Calendar.SECOND, 0)
        if (now.after(startTime))
            startTime.add(Calendar.DATE, 1)

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            startTime.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun notificationWallpaper(hour: Int, minute: Int) {
        val alarmManager = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, WallpaperNotificationReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(context, 0, alarmIntent, 0)

        val startTime = Calendar.getInstance()
        val now = Calendar.getInstance()
        startTime.set(Calendar.HOUR_OF_DAY, hour)
        startTime.set(Calendar.MINUTE, minute)
        if (now.after(startTime))
            startTime.add(Calendar.DATE, 1)

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            startTime.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }*/

}