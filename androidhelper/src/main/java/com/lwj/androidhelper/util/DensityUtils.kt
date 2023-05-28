package com.lwj.androidhelper.util

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.annotation.DimenRes

/**
 *密度辅助类
 */
object DensityUtils {
    fun dip2px(context: Context, dpValue: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun px2dip(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    fun px2sp(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.scaledDensity
        return (pxValue / scale + 0.5f).toInt()
    }

    fun getSP(context: Context, @DimenRes spSize: Int): Int {
        val pxValue = context.resources.getDimension(spSize)
        return px2sp(context, pxValue)
    }

    fun getDP(context: Context, @DimenRes dpSize: Int): Int {
        val pxValue = context.resources.getDimension(dpSize)
        return px2dip(context, pxValue)
    }

    fun getAndroidScreenProperty(context: Context) {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        wm.defaultDisplay.getMetrics(dm)
        val width = dm.widthPixels // 屏幕宽度（像素）
        val height = dm.heightPixels // 屏幕高度（像素）
        val density = dm.density // 屏幕密度（0.75 / 1.0 / 1.5）
        val densityDpi = dm.densityDpi // 屏幕密度dpi（120 / 160 / 240）
        // 屏幕宽度算法:屏幕宽度（像素）/屏幕密度
        val screenWidth = (width / density).toInt() // 屏幕宽度(dp)
        val screenHeight = (height / density).toInt() // 屏幕高度(dp)
//        Log.d("DensityUtils", "屏幕宽度（像素）：$width")
//        Log.d("DensityUtils", "屏幕高度（像素）：$height")
//        Log.d("DensityUtils", "屏幕密度（0.75 / 1.0 / 1.5）：$density")
//        Log.d("DensityUtils", "屏幕密度dpi（120 / 160 / 240）：$densityDpi")
//        Log.d("DensityUtils", "屏幕宽度（dp）：$screenWidth")
//        Log.d("DensityUtils", "屏幕高度（dp）：$screenHeight")
    }
}