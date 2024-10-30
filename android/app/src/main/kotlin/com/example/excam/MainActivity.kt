package com.example.excam

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.admin.DevicePolicyManager
import android.app.admin.SystemUpdatePolicy
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.SessionParams
import android.os.BatteryManager
import android.os.Bundle
import android.os.UserManager
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import pl.mrugacz95.kiosk.databinding.ActivityMainBinding
import android.widget.Toast
import android.os.Handler

class MainActivity : FlutterActivity() {

    private lateinit var mAdminComponentName: ComponentName
    private lateinit var mDevicePolicyManager: DevicePolicyManager
    private lateinit var binding: ActivityMainBinding

    companion object {
        const val CHANNEL = "kiosk_mode_channel"
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "startKioskMode" -> {
                    setKioskPolicies(true, isAdmin)
                    result.success(null)
                    Toast.makeText(this, "Admin Device start", Toast.LENGTH_SHORT).show()
                }
                "stopKioskMode" -> {
                    setKioskPolicies(false, isAdmin)
                    result.success(null)
                    Toast.makeText(this, "Admin Device stop", Toast.LENGTH_SHORT).show()
                }
                else -> result.notImplemented()
            }
        }
    }

    private fun requestAdminPermission() {
 
        AlertDialog.Builder(this)
            .setTitle("Admin Permission Required")
            .setMessage("This app requires admin permissions to run in kiosk mode.")
            .setPositiveButton("Grant") { _, _ ->
                // Start the Device Policy Manager to request admin permissions
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminComponentName)
                startActivityForResult(intent, REQUEST_CODE_ADMIN)
               
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAdminComponentName = MyDeviceAdminReceiver.getComponentName(this)
        mDevicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        val isAdmin = isAdmin()
        if (isAdmin) {
            Snackbar.make(binding.content, R.string.device_owner, Snackbar.LENGTH_SHORT).show()
            yesIsAdmin()
        } else {
            Snackbar.make(binding.content, R.string.not_device_owner, Snackbar.LENGTH_SHORT).show()
            requestAdminPermission() 
        }
    }

    private fun isAdmin() = mDevicePolicyManager.isDeviceOwnerApp(packageName)

    private fun setKioskPolicies(enable: Boolean, isAdmin: Boolean) {
        if (isAdmin) {
            setRestrictions(enable)
            enableStayOnWhilePluggedIn(enable)
            setUpdatePolicy(enable)
            setAsHomeApp(enable)
            setKeyGuardEnabled(enable)
        }
        setLockTask(enable, isAdmin)
        setImmersiveMode(enable)
    }

    private fun enableStayOnWhilePluggedIn(active: Boolean) = if (active) {
        mDevicePolicyManager.setGlobalSetting(
            mAdminComponentName,
            Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
            (BatteryManager.BATTERY_PLUGGED_AC
                    or BatteryManager.BATTERY_PLUGGED_USB
                    or BatteryManager.BATTERY_PLUGGED_WIRELESS).toString()
        )
    } else {
        mDevicePolicyManager.setGlobalSetting(mAdminComponentName, Settings.Global.STAY_ON_WHILE_PLUGGED_IN, "0")
    }
    
    private fun yesIsAdmin(){
        var admin = isAdmin()
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setMessage("is admin on ")
            .setTitle("admin on : $admin")
            .setCancelable(false)

        val dialog: AlertDialog = builder.create()
        dialog.show()
        
        Handler().postDelayed({
            if (alertDialog.isShowing) {
                alertDialog.dismiss()
            }
        }, 3000)
    }

    private fun setAsHomeApp(enable: Boolean) {
        if (enable) {
            val intentFilter = IntentFilter(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            mDevicePolicyManager.addPersistentPreferredActivity(
                mAdminComponentName, intentFilter, ComponentName(packageName, MainActivity::class.java.name)
            )
        } else {
            mDevicePolicyManager.clearPackagePersistentPreferredActivities(
                mAdminComponentName, packageName
            )
        }
    }

    private fun setLockTask(start: Boolean, isAdmin: Boolean) {
        if (isAdmin) {
            mDevicePolicyManager.setLockTaskPackages(
                mAdminComponentName, if (start) arrayOf(packageName) else arrayOf()
            )
        }
        if (start) {
            startLockTask()
        } else {
            stopLockTask()
        }
    }

    @Suppress("DEPRECATION")
    private fun setImmersiveMode(enable: Boolean) {
        if (enable) {
            val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            window.decorView.systemUiVisibility = flags
        } else {
            val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            window.decorView.systemUiVisibility = flags
        }
    }


    private fun setRestrictions(disallow: Boolean) {
        setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, disallow)
        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, disallow)
        setUserRestriction(UserManager.DISALLOW_ADD_USER, disallow)
        setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, disallow)
        setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, disallow)
        mDevicePolicyManager.setStatusBarDisabled(mAdminComponentName, disallow)
    }


    private fun setKeyGuardEnabled(enable: Boolean) {
        mDevicePolicyManager.setKeyguardDisabled(mAdminComponentName, !enable)
    }


    private fun setUpdatePolicy(enable: Boolean) {
        if (enable) {
            mDevicePolicyManager.setSystemUpdatePolicy(
                mAdminComponentName,
                SystemUpdatePolicy.createWindowedInstallPolicy(60, 120)
            )
        } else {
            mDevicePolicyManager.setSystemUpdatePolicy(mAdminComponentName, null)
        }
    }

    private fun setAsHomeApp(enable: Boolean) {
        if (enable) {
            val intentFilter = IntentFilter(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            mDevicePolicyManager.addPersistentPreferredActivity(
                mAdminComponentName, intentFilter, ComponentName(packageName, MainActivity::class.java.name)
            )
        } else {
            mDevicePolicyManager.clearPackagePersistentPreferredActivities(
                mAdminComponentName, packageName
            )
        }
    }
    // ... rest of your code
}


// import android.app.admin.DevicePolicyManager
// import android.content.ComponentName
// import android.content.Context
// import android.os.Bundle
// import android.view.View
// import android.widget.Toast
// import androidx.annotation.NonNull
// import androidx.appcompat.app.AlertDialog
// import io.flutter.embedding.android.FlutterActivity
// import io.flutter.embedding.engine.FlutterEngine
// import io.flutter.plugin.common.MethodChannel
// import android.app.Activity
// import android.content.Intent

// class MainActivity : FlutterActivity() {
//     private lateinit var mDevicePolicyManager: DevicePolicyManager
//     private lateinit var mAdminComponentName: ComponentName

//     // Define a method channel name
//     private val CHANNEL = "kiosk_mode_channel"
//     private val REQUEST_CODE_ENABLE_ADMIN = 1

//     override fun onCreate(savedInstanceState: Bundle?) {
//         super.onCreate(savedInstanceState)
//         setContentView(R.layout.activity_main) // Ensure activity_main.xml exists

//         mDevicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
//         mAdminComponentName = ComponentName(this, MyDeviceAdminReceiver::class.java)

//         // Check if the device admin is active
//         if (!mDevicePolicyManager.isAdminActive(mAdminComponentName)) {
//             showAdminPermissionDialog()
//         }
//     }

//     private fun setKioskPolicies(enable: Boolean) {
//         if (enable) {
//             mDevicePolicyManager.setLockTaskPackages(mAdminComponentName, arrayOf(packageName))
//             startLockTask()
//             setImmersiveMode(true) // Enable immersive mode when kiosk mode is activated
//             Toast.makeText(this, "Kiosk Mode Activated", Toast.LENGTH_SHORT).show()
//         } else {
//             stopLockTask()
//             setImmersiveMode(false) // Disable immersive mode when kiosk mode is deactivated
//             Toast.makeText(this, "Kiosk Mode Deactivated", Toast.LENGTH_SHORT).show()
//         }
//     }

//     private fun showAdminPermissionDialog() {
//         val builder = AlertDialog.Builder(this)
//         builder.setTitle("Admin Permission Required")
//         builder.setMessage("This app requires device admin permission for additional features. Enable now?")
//         builder.setPositiveButton("Yes") { _, _ ->
//             requestAdminPermission()
//         }
//         builder.setNegativeButton("No") { dialog, _ ->
//             dialog.dismiss()
//             finish()
//         }
//         builder.show()
//     }

//     private fun setImmersiveMode(enable: Boolean) {
//         if (enable) {
//             val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                     or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                     or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                     or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                     or View.SYSTEM_UI_FLAG_FULLSCREEN
//                     or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

//             window.decorView.systemUiVisibility = flags
//         } else {
//             val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                     or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                     or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

//             window.decorView.systemUiVisibility = flags
//         }
//     }

//     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//         super.onActivityResult(requestCode, resultCode, data)
//         if (requestCode == REQUEST_CODE_ENABLE_ADMIN) {
//             if (resultCode == Activity.RESULT_OK) {
//                 // Admin enabled, proceed with kiosk mode
//                 setKioskPolicies(true)
//             } else {
//                 showAdminPermissionDialog()
//                 // Admin not enabled, show error message
//                 Toast.makeText(this, "Admin Device not activated", Toast.LENGTH_SHORT).show()
//             }
//         }
//     }

//     private fun requestAdminPermission() {
//         val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
//             putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminComponentName)
//             putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "This permission is required for additional security features.")
//         }
//         startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
//     }

//     // Configure the Flutter engine
//     override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
//         super.configureFlutterEngine(flutterEngine)

//         // Set up the method channel
//         MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
//             when (call.method) {
//                 "startKioskMode" -> {
//                     if (mDevicePolicyManager.isDeviceOwnerApp(packageName)) {
//                         setKioskPolicies(true)
//                         result.success(null)
//                     } else {
//                         result.error("NOT_DEVICE_OWNER", "Not a device owner", null)
//                     }
//                 }
//                 "endKioskMode" -> {
//                     stopLockTask()
//                     Toast.makeText(this, "Kiosk Mode Deactivated", Toast.LENGTH_SHORT).show()
//                     //setImmersiveMode(false)
//                     result.success(null)
//                 }
//                 else -> {
//                     result.notImplemented()
//                 }
//             }
//         }
//     }
// }
