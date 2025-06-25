package com.example.ringertoggler // Make sure this matches your project's package name

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var permissionStatusText: TextView
    private lateinit var permissionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionStatusText = findViewById(R.id.permission_status_text)
        permissionButton = findViewById(R.id.permission_button)

        permissionButton.setOnClickListener {
            // Open the system settings screen for Do Not Disturb access
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Check the permission status every time the app is resumed
        updatePermissionStatus()
    }

    private fun updatePermissionStatus() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && notificationManager.isNotificationPolicyAccessGranted) {
            permissionStatusText.text = "Permission Granted!\nYou can now add the widget to your home screen."
            permissionStatusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
            permissionButton.isEnabled = false
            permissionButton.text = "Permission OK"
        } else {
            permissionStatusText.text = "Permission Required:\nPlease tap the button below and enable access for 'Ringer Toggler' to allow it to change the phone to Silent mode."
            permissionStatusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            permissionButton.isEnabled = true
            permissionButton.text = "Grant Permission"
        }
    }
}
