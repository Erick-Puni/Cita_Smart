package app.punisoft.citasmart.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.punisoft.citasmart.util.NotificationUtils

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Recordatorio"
        val message = intent.getStringExtra("message") ?: "Tienes una notificación pendiente."
        NotificationUtils.showLocalNotification(context, title, message)
    }
}
