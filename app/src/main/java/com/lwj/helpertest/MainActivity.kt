package com.lwj.helpertest

import android.os.Bundle
import android.util.Log
import com.lwj.androidhelper.view.danmuview.DanmuView
import com.lwj.helpertest.base.BaseActivity
import com.lwj.helpertest.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>() {
    private lateinit var danmu: DanmuView
    override fun initBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun initCreate(savedInstanceState: Bundle?) {
        val list: MutableList<String> = mutableListOf()
        list.add("恭喜张三破记录，获得免费兑换资格")
        list.add("李四两分钟前兑换了超级大礼包")
        list.add("王五三分钟前兑换了超级大礼包")
        list.add("赵六五分钟前兑换了超级大礼包")
        list.add("恭喜孙七破记录，获得免费兑换资格")
        list.add("恭喜周八破记录，获得免费兑换资格")
        list.add("吴九两分钟前兑换了超级大礼包")
        list.add("郑十三分钟前兑换了超级大礼包")
        binding.danmu.fillWarehouse(list)
        binding.btStart.setOnClickListener {
            binding.danmu.startLaunch()
        }
        binding.btStop.setOnClickListener {
            binding.danmu.stop()
        }

        binding.danmu.setSwitchoverListener(object : DanmuView.SwitchoverListener {
            override fun switchoverWareHouse() {
                fillData()
            }
        })
        binding.btAdd.setOnClickListener {
            binding.danmu.setSpeed(5f)
        }

        binding.btSub.setOnClickListener {
            binding.danmu.setSpeed(1f)
        }
    }


    private fun fillData() {
        val data: MutableList<String> = mutableListOf()

        data.add("恭喜张三破记录，获得免费兑换资格")
        data.add("李四两分钟前兑换了超级大礼包")
        data.add("王五三分钟前兑换了超级大礼包")
        data.add("赵六五分钟前兑换了超级大礼包")
        data.add("恭喜孙七破记录，获得免费兑换资格")
        data.add("恭喜张三破记录，获得免费兑换资格")
        data.add("李四两分钟前兑换了超级大礼包")
        data.add("王五三分钟前兑换了超级大礼包")
        data.add("赵六五分钟前兑换了超级大礼包")
        data.add("恭喜孙七破记录，获得免费兑换资格")
        data.add("恭喜张三破记录，获得免费兑换资格")
        data.add("李四两分钟前兑换了超级大礼包")
        data.add("王五三分钟前兑换了超级大礼包")
        data.add("赵六五分钟前兑换了超级大礼包")
        data.add("恭喜孙七破记录，获得免费兑换资格")
        binding.danmu.fillWarehouse(data)
    }

    private val TAG = "MainActivity"
    override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResume: ")
    }

    override fun onStart() {
        super.onStart()
        Log.e(TAG, "onStart: ")
        binding.danmu.start()
    }

    override fun onPause() {
        super.onPause()
        Log.e(TAG, "onPause: ")
        binding.danmu.stop()
    }

    override fun onStop() {
        super.onStop()
        Log.e(TAG, "onStop: ")

    }
}