package com.mhssn.meterView

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.coroutines.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var meterView: MeterView
    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        meterView = findViewById(R.id.meterView)

        scope.launch { testProgress() }
    }


    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private suspend fun testProgress() = withContext(Dispatchers.IO) {
        repeat(1000) {
            val random = Random.nextFloat()
            withContext(Dispatchers.Main) {
                meterView.progress = random
            }
            delay(300)
        }
    }
}