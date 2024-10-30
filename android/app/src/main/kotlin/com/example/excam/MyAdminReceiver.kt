package com.example.excam

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log

class MyAdminReceiver : DeviceAdminReceiver() {
    companion object {
        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context.applicationContext, MyAdminReceiver::class.java)
        }

        private val TAG = MyAdminReceiver::class.java.simpleName
    }

    override fun onLockTaskModeEntering(context: Context, intent: Intent, pkg: String) {
        super.onLockTaskModeEntering(context, intent, pkg)
        Log.d(TAG, "onLockTaskModeEntering")
    }

    override fun onLockTaskModeExiting(context: Context, intent: Intent) {
        super.onLockTaskModeExiting(context, intent)
        Log.d(TAG, "onLockTaskModeExiting")
    }
}

//C:\Users\PC-Server\Documents\flutterApp\-Excam-school\android\app\src\main\kotlin\com\example\excam\MyAdminReceiver.kt