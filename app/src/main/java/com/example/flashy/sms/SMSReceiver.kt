package com.example.flashy.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.flashy.services.FlashCallService

class SMSReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == "android.provider.Telephony.SMS_RECEIVED") {
            val serviceIntent = Intent(context, FlashCallService::class.java).apply {
                putExtra("eventType", "SMS")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            Log.d("SMSReceiver", "SMS received, FlashCallService started")
        }
    }
}
