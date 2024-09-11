package com.unitytests.virtumarttest.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.unitytests.virtumarttest.R

class AppNotificationManager(private val context: Context) {

    companion object {
        const val LOGIN_LOGOUT_CHANNEL_ID = "login_logout_channel_id"
        const val LOGIN_LOGOUT_CHANNEL_NAME = "Login Notifications"
        const val LOGIN_NOTIFICATION_ID = 1
        const val LOGOUT_NOTIFICATION_ID = 2

        const val ORDER_UPDATE_CHANNEL_ID = "order_update_channel_id"
        const val ORDER_UPDATE_CHANNEL_NAME = "Order Update Notifications"
        const val ORDER_UPDATE_NOTIFICATION_ID = 3
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

            // Session management notifications channel
            val sessionManagementChannel = NotificationChannel(
                LOGIN_LOGOUT_CHANNEL_ID,
                LOGIN_LOGOUT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for login and logout notifications"
            }

            // Order update notifications channel
            val orderUpdateChannel = NotificationChannel(
                ORDER_UPDATE_CHANNEL_ID,
                ORDER_UPDATE_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for order update notifications"
            }

            // Register the channels with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(sessionManagementChannel)
            notificationManager.createNotificationChannel(orderUpdateChannel)
        }
    }

    fun showLoginNotification() {
        if (isNotificationsEnabled()) {
            val builder = NotificationCompat.Builder(context, LOGIN_LOGOUT_CHANNEL_ID)
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
            val builder = NotificationCompat.Builder(context, LOGIN_LOGOUT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_logo)
                .setContentTitle("Logout Success")
                .setContentText("You have successfully logged out.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(context)) {
                notify(LOGOUT_NOTIFICATION_ID, builder.build())
            }
        }
    }

    fun showOrderUpdateNotification(orderId: Long, status: String) {
        if (isNotificationsEnabled()) {
            val builder = NotificationCompat.Builder(context, ORDER_UPDATE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_logo)
                .setContentTitle("Order Update")
                .setContentText("Your order #$orderId has been set to \"$status.\"")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(context)) {
                notify(ORDER_UPDATE_NOTIFICATION_ID, builder.build())
            }
        }
    }
}