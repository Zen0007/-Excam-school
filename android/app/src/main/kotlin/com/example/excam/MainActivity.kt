package com.example.excam


import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import android.app.admin.DeviceAdminReceiver
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity(){
    private val CHANNEL = "com.example.kiosk/mode"

  fun startKioskMode() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(this, MyAdminReceiver::class.java)
        if (dpm.isDeviceOwnerApp(packageName)) {
            dpm.setLockTaskPackages(componentName, arrayOf(packageName))
            startLockTask()
            Toast.makeText(this, "Kiosk Mode Started", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Not device owner", Toast.LENGTH_SHORT).show()
        }
    }
}

   fun stopKioskMode() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        stopLockTask()
        Toast.makeText(this, "Kiosk Mode Stopped", Toast.LENGTH_SHORT).show()
    }
}


    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
    super.configureFlutterEngine(flutterEngine)
    MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
      call, result ->
      when (call.method) {
        "startKioskMode" -> startKioskMode()
        "stopKioskMode" -> stopKioskMode()
        else -> result.notImplemented()
     }

    }
  }
}


class MyAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
    }
}