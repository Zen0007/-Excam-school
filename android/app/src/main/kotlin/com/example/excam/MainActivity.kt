package com.example.excam


import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private lateinit var mDevicePolicyManager: DevicePolicyManager
    private lateinit var mAdminComponentName: ComponentName

    // Define a method channel name
    private val CHANNEL = "kiosk_mode_channel"
    private val REQUEST_CODE_ENABLE_ADMIN = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAdminComponentName = MyDeviceAdminReceiver.getComponentName(this)
        mDevicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        // Memeriksa apakah admin perangkat sudah aktif
        if (!mDevicePolicyManager.isAdminActive(mAdminComponentName)) {
            // Jika admin belum diaktifkan, meminta pengguna untuk mengaktifkannya
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminComponentName)
            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
        } else {
            // Admin sudah diaktifkan, lanjutkan dengan pengaturan kiosk
            setKioskPolicies(true)
        }
    }

    private fun setKioskPolicies(enable: Boolean) {
        if (enable) {
            mDevicePolicyManager.setLockTaskPackages(mAdminComponentName, arrayOf(packageName))
            startLockTask()
            setImmersiveMode(true) // Mengaktifkan mode imersif saat kiosk mode diaktifkan
            Toast.makeText(this, "Kiosk Mode Activated", Toast.LENGTH_SHORT).show()
        } else {
            stopLockTask()
            setImmersiveMode(false) // Menonaktifkan mode imersif saat kiosk mode dinonaktifkan
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
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ENABLE_ADMIN) {
            if (resultCode == RESULT_OK) {
                // Admin diaktifkan, lanjutkan dengan pengaturan kiosk
                setKioskPolicies(true)
            } else {
                // Admin tidak diaktifkan, berikan pesan kepada pengguna
                Toast.makeText(this, "Admin Device not activated", Toast.LENGTH_SHORT).show()
            }
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
                    stopLockTask()
                    Toast.makeText(this, "Kiosk Mode Deactivated", Toast.LENGTH_SHORT).show()
                    //setImmersiveMode(false)
                    result.success(null)
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }
}
