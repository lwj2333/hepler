package com.lwj.androidhelper.view.danmuview

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.lwj.androidhelper.R

/**
 * 简易弹幕控件
 */
class DanmuView : SurfaceView, SurfaceHolder.Callback, Runnable {
    private constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initAttrs(attrs)
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private fun initAttrs(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.DanmuView)
        val count = typedArray.indexCount
        for (i in 0 until count) {
            when (val attr = typedArray.getIndex(i)) {
                R.styleable.DanmuView_count -> {
                    trajectoryCount = typedArray.getInteger(attr, 1)
                }
                R.styleable.DanmuView_background -> {
                    val itemBackgroundID = typedArray.getResourceId(attr, -1)
                    itemBackground =
                        BitmapFactory.decodeResource(context.resources, itemBackgroundID)
                }
                R.styleable.DanmuView_speed -> {
                    speed = typedArray.getFloat(attr, 5f)
                }
                R.styleable.DanmuView_itemPadding -> {
                    val itemPadding = typedArray.getDimension(attr, 0f)
                    itemPaddingStart = itemPadding
                    itemPaddingEnd = itemPadding
                    itemPaddingTop = itemPadding
                    itemPaddingBottom = itemPadding
                }
                R.styleable.DanmuView_itemPaddingStart -> {
                    itemPaddingStart = typedArray.getDimension(attr, 0f)
                }
                R.styleable.DanmuView_itemPaddingEnd -> {
                    itemPaddingEnd = typedArray.getDimension(attr, 0f)
                }
                R.styleable.DanmuView_itemPaddingTop -> {
                    itemPaddingTop = typedArray.getDimension(attr, 0f)
                }
                R.styleable.DanmuView_itemPaddingBottom -> {
                    itemPaddingBottom = typedArray.getDimension(attr, 0f)
                }
                R.styleable.DanmuView_android_textSize -> {
                    textSize = typedArray.getDimension(attr, 0f)
                }
                R.styleable.DanmuView_android_textColor -> {
                    textColor = typedArray.getResourceId(attr, -1)
                }
                R.styleable.DanmuView_itemHeight -> {
                    itemHeight = typedArray.getDimension(attr, 100f)
                }
                R.styleable.DanmuView_spacingY -> {
                    trajectorySpacingY = typedArray.getDimension(attr, 10f)
                }
                R.styleable.DanmuView_spacingX -> {
                    danmuSpacingX = typedArray.getDimension(attr, 10f)
                }
                R.styleable.DanmuView_isTransparent -> {
                    isTransparent = typedArray.getBoolean(attr, false)
                }
            }
        }
        typedArray.recycle()
    }


    private lateinit var surfaceHolder: SurfaceHolder

    @Volatile
    private var isDrawing: Boolean = false
    private fun init() {
        surfaceHolder = holder
        surfaceHolder.addCallback(this)

        surfaceHolder.setKeepScreenOn(true) //屏幕常亮
        if (isTransparent) {
            surfaceHolder.setFormat(PixelFormat.TRANSPARENT)//使窗口支持透明度
            setZOrderOnTop(true)//使surfaceView放到最顶层
        }

        for (j in 0 until trajectoryCount) {//生成弹道控制开关
            map[j + 1] = true
        }
        initPaint()
    }

    private lateinit var paint: Paint
    private var trajectoryCount: Int = 3 //弹道数量
    private var trajectorySpacingY: Float = 10f //弹道间隔
    private var danmuSpacingX: Float = 0f //弹幕间隔
    private var widthView: Float = 0f //控件宽度
    private var heightView: Float = 0f //控件高度
    private var textSize: Float = 15f //文字大小
    private var textColor: Int = -1 //文字颜色
    private var itemBackground: Bitmap? = null
    private var itemPaddingEnd: Float = 0f // 弹幕内边距
    private var itemPaddingStart: Float = 0f
    private var itemPaddingTop: Float = 0f
    private var itemPaddingBottom: Float = 0f
    private var isTransparent: Boolean = false  //背景是否透明

    private var speed: Float = 5f //弹幕滑动的速度
    private var workThread: Thread? = null // 负责绘画的工作线程
    private var itemHeight: Float = 100f
    private var map: MutableMap<Int, Boolean> = mutableMapOf() //记录弹道是否可以使用
    private val warehouse: MutableList<DanmuItem> = mutableListOf()   // 正在使用的仓库
    private val spareWareHouseOne: MutableList<String> = mutableListOf() //备用的一号仓库
    private val spareWareHouseTwo: MutableList<String> = mutableListOf() //备用的二号仓库
    private var areUsingSpareWareHouse: MutableList<String>? = null //正在被使用的备用仓库
    private val warehouseToRemove: MutableList<Int> = mutableListOf()   // 需要移除的正在使用的仓库
    private var wareHouseIndex: Int = 0   //备用仓库指示标志
    private var listener: SwitchoverListener? = null //备用仓库切换监听
    private val handler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            listener!!.switchoverWareHouse()
        }
    }

    fun setSwitchoverListener(listener: SwitchoverListener) {
        this.listener = listener
    }

    private fun initPaint() {
        paint = Paint()
        paint.color = context.getColor(textColor)
        paint.isAntiAlias = true //抗锯齿
        paint.textSize = textSize
    }

    /**
     * 当SurfaceView被创建的时候吧调用
     */
    override fun surfaceCreated(holder: SurfaceHolder) {
        workThread = Thread(this)

    }

    /**
     * 当SurfaceView的视图发生改变，比如横竖屏切换时
     */
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        widthView = width.toFloat()
        heightView = height.toFloat()

        textSpace = itemHeight - itemPaddingTop - itemPaddingBottom

        workThread?.start()

    }

    /**
     * 当SurfaceView被销毁时，比如不可见  onStop
     */
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        workThread = null
    }


    override fun run() {

        while (isDrawing) {

            val canvas = surfaceHolder.lockCanvas()
           draw(canvas)
            holder.unlockCanvasAndPost(canvas)

        }
    }


    override fun draw(canvas: Canvas?) {
        if (canvas == null) return
        super.draw(canvas)
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)//清屏
        val speed = speed

        for (i in 0 until warehouse.size) {
            val item = warehouse[i]
            item.distance += speed //上一次的进度+速度=当前进度
            val lastX = item.right - item.distance //item最右边X坐标
            //  Log.e(TAG, "draw:lastX   $lastX $widthView")
            if (item.lastFlag && (lastX + danmuSpacingX) <= widthView) {
                map[item.position] = true
                item.lastFlag = false
            }
            if (lastX <= 0) { //弹幕已全部移动到容器之外，从  warehouse中移除
                warehouseToRemove.add(i)
            }

            if (itemBackground != null) {
                canvas.drawBitmap(
                    itemBackground!!, null,
                    Rect(
                        (item.left - item.distance).toInt(),
                        item.top.toInt(),
                        (item.right - item.distance).toInt(),
                        item.bottom.toInt()
                    ), paint
                )
            }
            canvas.drawText(item.content, item.textX - item.distance, item.textY, paint)
        }

        for ((key, value) in map) {
            if (value) {
                if (areUsingSpareWareHouse == null || areUsingSpareWareHouse!!.isEmpty()) {
                    areUsingSpareWareHouse = extractWareHouse(false)
                }
                if (areUsingSpareWareHouse!!.isNotEmpty()) {
                    spareToUsed(key, areUsingSpareWareHouse!!)
                }
            }
        }

        removeIndex(warehouseToRemove) {
            warehouse.removeAt(it)
        }

    }

    private var flagOne: Boolean = false
    private var flagTwo: Boolean = false
    private var textSpace: Float = 0f //单个弹幕文本空间
    private val TAG = "DanmuView"

    /**
     * 从备用仓库取出文本，生成弹幕，填充到弹幕仓库
     */
    private fun spareToUsed(key: Int, list: MutableList<String>) {
     
        val content = list[0]
        val rect = Rect()
        paint.getTextBounds(content, 0, content.length, rect)

        val left = widthView //从右往左，由容器之外移动进来
        val top =
            paddingTop + trajectorySpacingY * (key - 1) + itemHeight * (key - 1)
        val right = left + itemPaddingStart + itemPaddingEnd + rect.width()
        val bottom = top + itemHeight
        val textX = left + itemPaddingStart
        val textY =
            bottom - itemPaddingBottom - (textSpace - rect.height()) / 2 - rect.bottom
        val bean =
            DanmuItem(content, key, left, top, right, bottom, speed, textX, textY)
        warehouse.add(bean)
        map[key] = false
        list.removeAt(0)
    }

    /**
     * 从仓库中移除无效弹幕
     */
    private fun removeIndex(list: MutableList<Int>, block: (Int) -> Unit) {
        for (i in list.size - 1 downTo 0) {
            block.invoke(i)
        }
        list.clear()
    }

    /**
     * 填充弹幕库
     */
    fun fillWarehouse(list: MutableList<String>) {
        val data = extractWareHouse(true)
        data.addAll(list)
        when (wareHouseIndex) {
            0 -> {
                flagTwo = true
            }
            1 -> {
                flagOne = true
            }
        }

    }



    /**
     * 改变速度
     */
    fun setSpeed(speed: Float) {

        this.speed = speed
    }

    @Synchronized
    fun extractWareHouse(flag: Boolean): MutableList<String> {

        return when (flag) {
            true -> { //填充弹幕
                when (wareHouseIndex) {
                    0 -> {
                        flagTwo = false
                        spareWareHouseTwo
                    }
                    else -> {
                        flagOne = false
                        spareWareHouseOne
                    }
                }
            }
            false -> {//取出弹幕
                when (wareHouseIndex) {
                    0 -> {
                      if (spareWareHouseTwo.size > 0 && flagTwo) {

                            wareHouseIndex = 1
                            if (listener != null) {
                                handler.sendEmptyMessage(1)
                            }
                            spareWareHouseTwo
                        } else {
                            spareWareHouseOne
                        }
                    }
                    else -> {
                        if (spareWareHouseOne.size > 0 && flagOne) {

                            wareHouseIndex = 0
                            if (listener != null) {
                                handler.sendEmptyMessage(1)
                            }
                            spareWareHouseOne
                        } else {
                            spareWareHouseTwo
                        }
                    }
                }
            }
        }
    }

    /**
     * 开始发射弹幕
     */
    fun startLaunch() {
        if (workThread != null && !isDrawing) {
            isDrawing = true
            workThread = Thread(this)
            workThread!!.start()
        }
    }

    /**
     * 允许发射弹幕
     */
    fun start() {
        isDrawing = true
    }

    /**
     * 停止发射弹幕
     */
    fun stop() {
        isDrawing = false
    }

    /**
     * 设置内边距
     */
    fun setItemPadding(left: Float, top: Float, right: Float, bottom: Float) {
        itemPaddingBottom = bottom
        itemPaddingTop = top
        itemPaddingEnd = right
        itemPaddingStart = left
    }


    interface SwitchoverListener {
        fun switchoverWareHouse()
    }

    /**
     * @param content 弹幕文本内容
     * @param position 绘画弹幕的弹轨下标
     * @param left 弹幕左边相对容器原点的x坐标
     * @param top    弹幕上边相对容器原点的y坐标
     * @param right 弹幕右边相对容器原点的x坐标
     * @param bottom 弹幕下边相对容器原点的y坐标
     * @param speed  弹幕移动速度
     * @param textX  弹幕文本相对容器原点的x坐标
     * @param textY 弹幕文本相对容器原点的y坐标
     * @param lastFlag 该弹幕是否是本弹轨最后一条
     * @param distance 移动距离
     */
    data class DanmuItem(
        var content: String, var position: Int,
        var left: Float, var top: Float, var right: Float, var bottom: Float, var speed: Float,
        var textX: Float, var textY: Float, var lastFlag: Boolean = true, var distance: Float = 0f,
    )
}