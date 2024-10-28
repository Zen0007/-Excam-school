package com.example.excam // Ensure this matches your package name

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private lateinit var mDevicePolicyManager: DevicePolicyManager
    private lateinit var mAdminComponentName: ComponentName

    companion object {
        private const val REQUEST_CODE = 1 // Use uppercase for consistency
        private const val CHANNEL = "kiosk_mode_channel"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Device Policy Manager and Admin Component
        mAdminComponentName = MyDeviceAdminReceiver.getComponentName(this)
        mDevicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        // Check if the device admin is active
        if (!mDevicePolicyManager.isAdminActive(mAdminComponentName)) {
            // If not, prompt the user to enable it
         val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
         intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, ComponentName(this, MyDeviceAdminReceiver::class.java))
         intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "You need to enable this app as device admin to use kiosk mode.")
         startActivityForResult(intent, REQUEST_CODE) // Use the correct variable name
        } else {
            // If admin is active, set kiosk policies
            setKioskPolicies(true)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQUEST_CODE_ENABLE_ADMIN) {
        if (resultCode == Activity.RESULT_OK) {
            // Admin perangkat diaktifkan, lanjutkan dengan pengaturan kiosk
            setKioskPolicies(true)
        } else {
            // Pengguna menolak untuk mengaktifkan admin perangkat
            Toast.makeText(this, "Admin perangkat tidak diaktifkan. Kiosk mode tidak dapat diaktifkan.", Toast.LENGTH_LONG).show()
        }
      }
    }
    private fun setKioskPolicies(enable: Boolean) {
       val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
       val adminComponentName = ComponentName(this,MyDeviceAdminReceiver::class.java)

    // Memeriksa apakah admin perangkat aktif
    if (devicePolicyManager.isAdminActive(adminComponentName)) {
        if (enable) {
            // Mengatur paket yang diizinkan dalam mode kiosk
            devicePolicyManager.setLockTaskPackages(adminComponentName, arrayOf(packageName))
            startLockTask() // Memulai Lock Task Mode
            Toast.makeText(this, "Kiosk Mode Activated", Toast.LENGTH_SHORT).show()
        } else {
            stopLockTask() // Menghentikan Lock Task Mode
            Toast.makeText(this, "Kiosk Mode Deactivated", Toast.LENGTH_SHORT).show()
        }
    } else {
        // Jika admin perangkat tidak aktif
        Toast.makeText(this, "Admin perangkat tidak aktif. Kiosk mode tidak dapat diaktifkan.", Toast.LENGTH_LONG).show()
    }
    }

    private fun setImmersiveMode(enable: Boolean) {
        if (enable) {
            val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

            window.decorView.systemUiVisibility = flags
        }
    }

    // Configure the Flutter engine
    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        // Set up the method channel
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "startKioskMode" -> {
                    if (mDevicePolicyManager.isDeviceOwnerApp(packageName)) {
                        setKioskPolicies(true)
                        result.success(null)
                    } else {
                        result.error("NOT_DEVICE_OWNER", "Not a device owner", null)
                    }
                }
                "endKioskMode" -> {
                    setKioskPolicies(false)
                    result.success(null)
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }
}