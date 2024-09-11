package com.unitytests.virtumarttest.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.unitytests.virtumarttest.R

class AppNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "your_channel_id"
        const val CHANNEL_NAME = "Login Notifications"
        const val LOGIN_NOTIFICATION_ID = 1
        const val LOGOUT_NOTIFICATION_ID = 2
    }

    init {
        createNotificationChannel()
    }

    private fun isNotificationsEnabled(): Boolean {
        // Check if notifications are enabled
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val isNotificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true)

        return isNotificationsEnabled
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for login and logout notifications"
            }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showLoginNotification() {
        if (isNotificationsEnabled()) {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_logo)
                .setContentTitle("Login Success")
                .setContentText("You have successfully logged in.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(context)) {
                notify(LOGIN_NOTIFICATION_ID, builder.build())
            }
        }
    }

    fun showLogoutNotification() {
        if (isNotificationsEnabled()) {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_logo)
                .setContentTitle("Logout Success")
                .setContentText("You have successfully logged out.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(context)) {
                notify(LOGOUT_NOTIFICATION_ID, builder.build())
            }
        }
    }
}
