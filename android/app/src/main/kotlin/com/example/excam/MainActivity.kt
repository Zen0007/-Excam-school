package com.example.excam

import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.NonNull
import android.app.admin.DeviceAdminReceiver


class MainActivity: FlutterActivity(){
    private val CHANNEL = "com.example.kiosk/mode"
    private  var adminComponet:ComponentName
    private var devicePolicy:DevicePolicyManager


 private fun startKioskMode(result:MethodChannel.Result) {
    if(devicePolicy.isDeviceOwnerApp(packageName)){
        devicePolicy.setLockTaskPackages(adminComponet,arrayOf(packageName))
        startLockTask()
        result.success("starting")
    }else{
        result.error("ERROR","apk not have owner",null)
    }
  }
 
  private fun stopKioskMode(result :MethodChannel.Result){
    stopLockTask()
    val emptyArray = emptyArray<String>()
    devicePolicy.setLockTaskPackages(adminComponet,emptyArray)
    result.success("kiosk mode off")
  }


    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
    super.configureFlutterEngine(flutterEngine)
    devicePolicy = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    adminComponet = ComponentName(this,MyAdminReceiver::class.java)

    MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
      call, result ->
      when (call.method) {
       "startKioskMode" -> {
        startKioskMode(result)
       }
       "stopKioskMode" -> {
        stopKioskMode(result)
       }
       else -> {
        result.notImplemented()
       }
     }

    }

    if(!devicePolicy.isAdminActive(adminComponet)){
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,adminComponet)
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,"AKTIFASI PERANGKAT ON")
        startActivityForResult(intent,REQUEST_CODE_ENABELE_ADMIN)

    }
  }

  companion object{
    private const val REQUEST_CODE_ENABELE_ADMIN = 1001
  }
}


class MyAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        val devicePolicy = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponet ComponentName(content,MyAdminReceiver::class.java)

        if(devicePolicy.isDeviceOwnerApp(content.packageName)){
            devicePolicy.setLockTaskPackages(adminComponet,arrayOf(content.packageName))
            content.startLockTask()
        }
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        val devicePolicy = content.getSystemService(Context.DEVICE_POLICY_SERVICE)as DevicePolicyManager
        val adminComponet = ComponentName(content,MyAdminReceiver::class.java)
        devicePolicy.setLockTaskPackages(adminComponet,emptyArray())

    }
}