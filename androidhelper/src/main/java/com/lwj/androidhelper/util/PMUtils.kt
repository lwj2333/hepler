package com.lwj.androidhelper.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.*

/**
 * 安装APP辅助类
 */
object PMUtils {
    /**
     * 使用root权限静态安装app
     */
    fun installApp(path: String): Boolean {
        var result = false
        var es: BufferedReader? = null
        var os: DataOutputStream? = null

        try {
            val process: Process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process.outputStream)
            val command = "pm install -r $path\n"
            os.write(command.toByteArray())
            os.flush()
            os.writeBytes("exit\n")
            os.flush()
            process.waitFor()
            es = BufferedReader(InputStreamReader(process.errorStream))

            val builder = StringBuilder()
            es.lineSequence().forEach {
                builder.append(it)
            }
            if (!builder.toString().contains("Failure")) {
                result = true
            }
        } catch (e: Exception) {
            throw Exception(e)
        } finally {
            try {
                os?.close()
                es?.close()
            } catch (e: IOException) {
                throw Exception(e)
            }
        }
        return result
    }

    /**
     * 判断是否拥有Root权限
     * @return 有root权限返回true,否则返回false
     */
    fun isRoot(): Boolean {
        var bool = false
        try {
            bool = File("/system/bin/su").exists() || File("/system/xbin/su").exists()
        } catch (e: Exception) {
            throw Exception(e)
        }
        return bool
    }

    /**
     * @param authority 需要在配置清单文件里配置相关FileProvider
     */
    fun installApk(context: Context, path: String, authority: String) {
        val file = File(path)
        val apkUri = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    context,
                    authority,
                    file
                )
            } else {
                Uri.fromFile(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}