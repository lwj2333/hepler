package com.lwj.androidhelper.util

import android.util.Log
import com.lwj.androidhelper.BuildConfig

/**
 * 日志辅助类
 */
object LogUtils {

    private val IS_LOG: Boolean = BuildConfig.DEBUG

    // 默认的tag
    private const val LOG_TAG = "debug_log"


    fun v(msg: String) {
        if (IS_LOG) {
            Log.v(LOG_TAG, msg)
        }
    }

    fun i(msg: String) {
        if (IS_LOG) {
            Log.i(LOG_TAG, msg)
        }
    }

    fun w(msg: String) {
        if (IS_LOG) {
            Log.w(LOG_TAG, msg)
        }
    }

    fun d(msg: String) {
        if (IS_LOG) {
            Log.d(LOG_TAG, msg)
        }
    }

    fun e(msg: String) {
        if (IS_LOG) {
            Log.e(LOG_TAG, msg)
        }
    }

}