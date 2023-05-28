package com.lwj.androidhelper.util

/**
 * 定时器
 */
class CountDownTimer @JvmOverloads constructor(
    millisInFuture: Long = DEFAULT_MILLIS_IN_FUTURE,
    countDownInterval: Long = DEFAULT_COUNT_DOWN_INTERVAL,
) : android.os.CountDownTimer(millisInFuture, countDownInterval) {
    private var onCountDownListener: OnCountDownListener? = null

    fun setOnCountDownListener(onCountDownListener: OnCountDownListener): CountDownTimer {
        this.onCountDownListener = onCountDownListener
        return this
    }

    override fun onTick(l: Long) {
        if (onCountDownListener != null) {
            onCountDownListener!!.onTick(l)
        }
    }

    override fun onFinish() {
        if (onCountDownListener != null) {
            onCountDownListener!!.onFinish()
        }
    }

    interface OnCountDownListener {
        fun onTick(remain: Long)

        fun onFinish()
    }

    companion object {
        const val DEFAULT_MILLIS_IN_FUTURE = 3000L       //定时时间
        const val DEFAULT_COUNT_DOWN_INTERVAL = 1000L   //时间间隔
    }
}
