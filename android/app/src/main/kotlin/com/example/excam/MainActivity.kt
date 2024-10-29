package com.example.excam

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import android.app.Activity
import android.content.Intent


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
    super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Ensure activity_main.xml exists

        mDevicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        mAdminComponentName = ComponentName(this, MyDeviceAdminReceiver::class.java)

        // Check if the device admin is active
       if (!mDevicePolicyManager.isAdminActive(mAdminComponentName)) {
              showAdminPermissionDialog()
         }
}   
    // override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    // super.onActivityResult(requestCode, resultCode, data)
    // if (requestCode == REQUEST_CODE) {
    //     if (resultCode == Activity.RESULT_OK) {
    //         // Admin perangkat diaktifkan, lanjutkan dengan pengaturan kiosk
    //         setKioskPolicies(true)
    //     } else {
    //         // Pengguna menolak untuk mengaktifkan admin perangkat
    //         Toast.makeText(this, "Admin perangkat tidak diaktifkan. Kiosk mode tidak dapat diaktifkan.", Toast.LENGTH_LONG).show()
    //     }
    // }
    //     setKioskPolicies(true)
    //   } else {
    //         // Pengguna menolak untuk mengaktifkan admin perangkat
    //         Toast.makeText(this, "Admin perangkat tidak diaktifkan. Kiosk mode tidak dapat diaktifkan.", Toast.LENGTH_LONG).show()
    //     }
    // }

   private fun setKioskPolicies(enable: Boolean) {
    if (enable) {
        startLockTask()
        setKioskPolicies(true)
    } else {
        stopLockTask() // Stop Lock Task Mode
        setImmersiveMode(false) // Disable immersive mode
        Toast.makeText(this, "Kiosk Mode Deactivated", Toast.LENGTH_SHORT).show()
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
        }else {
            // Reset to default visibility
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }
      private fun showAdminPermissionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Admin Permission Required")
        builder.setMessage("This app requires device admin permission for additional features. Enable now?")
        builder.setPositiveButton("Yes") { _, _ ->
            requestAdminPermission()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            Toast.makeText(this, "Admin perangkat tidak diaktifkan. Kiosk mode tidak dapat diaktifkan.", Toast.LENGTH_LONG).show()
            dialog.dismiss()
            finish()
        }
        builder.show()
    }

    private fun requestAdminPermission() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminComponentName)
            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "This permission is required for additional security features.")
        }
        startActivityForResult(intent, REQUEST_CODE)
    }

    // Configure the Flutter engine
    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        // Set up the method channel
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "startKioskMode" -> {
                     setKioskPolicies(true)
                     result.success(null)
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