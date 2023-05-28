package com.lwj.androidhelper.util

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/**
 * app信息辅助类
 */
object AppUtils {

    /**
     * 获取指定包名的应用信息
     * @param context 上下文
     * @param packageName 包名
     */
    fun getAppInfo(context: Context, packageName: String): AppInfo? {

        val manager = context.packageManager
        try {
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {  //大于32
                manager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                manager.getPackageInfo(packageName, 0)
            }
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {  //大于32
                AppInfo(info.longVersionCode, info.versionName)
            } else {
                AppInfo(info.versionCode.toLong(), info.versionName)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 确定指定服务是否运行，有点小问题 多测试
     * 从Build.VERSION_CODES=O开始，此方法不再适用于第三方应用程序。为了向后兼容，它仍然返回调用者自己的服务。
     */
    fun isServiceRunning(context: Context,packageName: String): Boolean {
        val am: ActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info: List<ActivityManager.RunningServiceInfo> = am.getRunningServices(100)
        for (element in info) {
            if (element.service.className == packageName) {
                return true
            }
        }
        return false
    }
    class AppInfo(var code: Long, var name: String)
}