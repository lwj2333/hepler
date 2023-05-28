package com.lwj.androidhelper.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresPermission
import java.io.File
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*

/**
 * 设备辅助类
 */
object DeviceUtils {
    /**
     * 通过网络接口 获取本机的物理地址 当没有连接WiFi或开通手机流量时，获得的 mac = null //TODO 小米Android 11 拿不到mac
     * @return mac
     */
    fun getDeviceMac(): String? {
        try {
            val all: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in all) {
                if (!nif.name.equals("wlan0", true)) continue
                val macBytes: ByteArray = nif.hardwareAddress ?: return null
                val res1 = StringBuilder()
                for (b in macBytes) {
                    res1.append(String.format("%02X:", b))
                }
                if (res1.isNotEmpty()) {
                    res1.deleteCharAt(res1.length - 1)
                }
                return res1.toString()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }


    /**
     * 使用UUID.randomUUID()生成唯一标识符,并保存
     */
    suspend fun getDeviceUUID(context: Activity, fileName: String): String? {
        val file =
            File("${context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)}/$fileName")
        return getDeviceUUID(context, file)
    }

    suspend fun getDeviceUUID(context: Activity, file: File): String? {
        val uri = Uri.fromFile(file)
        var msg = UriFileUtils.readFile(context, uri)
        if (msg != null) {
            return msg
        }
        val uuid = UUID.randomUUID().toString()
        val result = UriFileUtils.saveFile(context, uuid, uri)
        if (result) {
            msg = uuid
        }
        return msg
    }

    /**
     * 转换IP
     */
    private fun intIP2StringIP(ip: Int): String {
        return (ip and 0xFF).toString() + "." +
                (ip shr 8 and 0xFF) + "." +
                (ip shr 16 and 0xFF) + "." +
                (ip shr 24 and 0xFF)
    }

    /**
     * android10版本
     * 判断网络类型：移动网络
     */
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_NETWORK_STATE])
    fun isMobileQ29(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    /**
     * android10版本
     * 判断网络类型：Wi-Fi类型
     */
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_NETWORK_STATE])
    fun isWifiQ29(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    /**
     * android10版本
     * 判断网络是否连接
     */
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_NETWORK_STATE])
    fun isConnectedQ29(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * 自动获取IP地址   当没有连接WiFi或开通手机流量时，获得的IP = null
     */
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_NETWORK_STATE])
    fun getIPAddress(context: Context): String? {
        val connectivityManager =
            context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.run {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                when (activeNetworkInfo?.type) {
                    ConnectivityManager.TYPE_MOBILE -> {
                        // 通过手机流量
                        return getMobileIp()
                    }
                    ConnectivityManager.TYPE_WIFI -> {
                        // 通过WIFI
                        return getWifiIp(context)
                    }
                    else -> {}
                }
            } else {
                // Android M 以上建议使用getNetworkCapabilities API
                activeNetwork?.let { network ->
                    getNetworkCapabilities(network)?.let { networkCapabilities ->
                        if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                            when {
                                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                                    // 通过手机流量
                                    return getMobileIp()
                                }
                                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                                    // 通过WIFI
                                    return getWifiIp(context)
                                }
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    /**
     * 通过WiFi获取ip
     */
    private fun getWifiIp(context: Context): String? {
        //   if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        // IP 地址
        return intIP2StringIP(wifiManager.connectionInfo.ipAddress)
//        }
//        else {
//            // Android Q 以上建议使用getNetworkCapabilities API
//            val connectivityManager =
//                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//            connectivityManager.run {
//                activeNetwork?.let { network ->
//                    Log.e(TAG, "getWifi:TTTTTTT    ${getNetworkCapabilities(network)?.transportInfo}", )
//                    (getNetworkCapabilities(network)?.transportInfo as? WifiInfo)?.let { wifiInfo ->
//                        // IP 地址
//                        Log.e(TAG, "getWifi: ${wifiInfo.ipAddress}", )
//                        return intIP2StringIP(wifiInfo.ipAddress)
//                    }
//                }
//            }
//        }
//        return null
    }

    /**
     * 通过流量获取ip
     */
    private fun getMobileIp(): String? {
        try {
            NetworkInterface.getNetworkInterfaces().let {
                for (networkInterface in Collections.list(it)) {
                    for (inetAddresses in Collections.list(networkInterface.inetAddresses)) {
                        if (!inetAddresses.isLoopbackAddress && !inetAddresses.isLinkLocalAddress) {
                            // IP地址
                            return inetAddresses.hostAddress
                        }
                    }
                }
            }
        } catch (e: SocketException) {
            e.printStackTrace()
        }
        return null
    }
}