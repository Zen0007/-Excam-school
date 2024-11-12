package com.example.excam

import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.app.admin.SystemUpdatePolicy
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.UserManager
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import android.widget.Toast

class MainActivity : FlutterActivity() {

    private lateinit var mAdminComponentName: ComponentName
    private lateinit var mDevicePolicyManager: DevicePolicyManager
    private lateinit var alertDialog: AlertDialog
    private lateinit var mUserManager: UserManager

    companion object {
        const val CHANNEL = "kiosk_mode_channel"
        private const val REQUEST_CODE_ADMIN = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAdminComponentName = MyAdminReceiver.getComponentName(this)
        mDevicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        mUserManager = getSystemService(Context.USER_SERVICE) as UserManager

        // Now it's safe to call isAdmin()
        val isAdmin = isAdmin()
        if (!isAdmin) {
            requestAdminPermission()
        } else {
           yesIsAdmin()
        }
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)


        val isAdmin = isAdmin() // This should be safe now
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
        val  builder  =  AlertDialog.Builder(this)
        builder.setTitle("Admin Permission Required")
             .setMessage("This app requires admin permissions to run in kiosk mode.")
             .setPositiveButton("Yes") { _, _ ->
                 val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                 intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminComponentName)
                 startActivityForResult(intent, REQUEST_CODE_ADMIN)
             }
             .setNegativeButton("Cancel") { dialog, _ ->
                 dialog.dismiss()
                 finish()
             }
        val  alertDialog = builder.create()
             alertDialog.setTitle("Admin Permission Required")
             alertDialog.show()
    }

    private fun isAdmin(): Boolean {
        // Check if mDevicePolicyManager is initialized before accessing it
        return if (::mDevicePolicyManager.isInitialized) {
            mDevicePolicyManager.isDeviceOwnerApp(packageName)
        } else {
            false
        }
    }

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

    private fun enableStayOnWhilePluggedIn(active: Boolean) {
        val settingValue = if (active) {
            (BatteryManager.BATTERY_PLUGGED_AC or BatteryManager.BATTERY_PLUGGED_USB or BatteryManager.BATTERY_PLUGGED_WIRELESS).toString()
        } else {
            "0"
        }
        mDevicePolicyManager.setGlobalSetting(mAdminComponentName, Settings.Global.STAY_ON_WHILE_PLUGGED_IN, settingValue)
    }

    private fun yesIsAdmin() {
        val admin = isAdmin()
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage("is admin on ")
            .setTitle("admin on : $admin ")
            .setCancelable(false)

        alertDialog = builder.create()
        alertDialog.show()

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
            mDevicePolicyManager.clearPackagePersistentPreferredActivities(mAdminComponentName, packageName)
        }
    }

    private fun setLockTask(start: Boolean, isAdmin: Boolean) {
        if (isAdmin) {
            mDevicePolicyManager.setLockTaskPackages(mAdminComponentName, if (start) arrayOf(packageName) else arrayOf())
        }
        if (start) {
            startLockTask()
        } else {
            stopLockTask()
        }
    }

    @Suppress("DEPRECATION")
   private fun setImmersiveMode(enable: Boolean) {
    val flags = if (enable) {
        (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
         View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
         View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    } else {
        (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
         View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }
    window.decorView.systemUiVisibility = flags

    window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
        if ((visibility and View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
            setImmersiveMode(true) // Mengatur kembali mode immersive
        }
    }
 }

    private fun setRestrictions(disallow: Boolean) {
        mUserManager.setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, disallow)
        mUserManager.setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, disallow)
        mUserManager.setUserRestriction(UserManager.DISALLOW_ADD_USER, disallow)
        mUserManager.setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, disallow)
        mUserManager.setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, disallow)
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
}

