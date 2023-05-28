package com.lwj.androidhelper.util

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * 使用Uri操作文件辅助类
 */
object UriFileUtils {


    suspend fun saveFile(context: Activity, msg: String, uri: Uri): Boolean {
        var result = false
        val sb = StringBuffer()
        sb.append(msg)
        val outputStream = context.contentResolver.openOutputStream(uri)
        if (outputStream != null) {
            withContext(Dispatchers.IO) {
                try {
                    outputStream.write(msg.toByteArray())
                    result = true
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    outputStream.close()
                }
            }
        }
        return result
    }


    fun readFile(context: Activity, uri: Uri): String? {
        val stringBuilder = StringBuilder()
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String? = reader.readLine()
                    while (line != null) {
                        stringBuilder.append(line)
                        line = reader.readLine()
                    }
                    reader.close()
                }
                inputStream.close()
            }
        } catch (t: Throwable) {
            return null
        }
        return stringBuilder.toString()
    }

    fun deleteFileByUri(context: Context, uri: Uri) {
       if (ContentResolver.SCHEME_CONTENT ==uri.scheme) { //以content://开头
            context.contentResolver.delete(uri, null, null)
        } else {
            val path = getRealFilePath(context, uri)?:return
            val file = File(path)
            if (file.exists() && file.isFile) {
                file.delete()
            }
        }
    }

    fun getRealFilePath(context: Context, uri: Uri): String? {
        val scheme = uri.scheme
        var data: String? = null
        if (scheme == null) {
            data = uri.path
        } else if (ContentResolver.SCHEME_FILE == scheme) {
            data = uri.path
        } else if (ContentResolver.SCHEME_CONTENT == scheme) {
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(MediaStore.Downloads.DATA),
                null,
                null,
                null
            )
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(MediaStore.Downloads.DATA)
                    if (index > -1) {
                        data = cursor.getString(index)
                    }
                }
                cursor.close()
            }
        }
        return data
    }
}