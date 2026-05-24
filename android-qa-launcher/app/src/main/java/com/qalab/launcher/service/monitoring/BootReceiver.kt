package com.qalab.launcher.service.monitoring

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i(TAG, "Boot completed - QA Lab ready")
            // Re-register any active monitoring sessions if needed
        }
    }

    companion object {
        private const val TAG = "QALabBoot"
    }
}
