package com.redix.carnative.car

import android.content.Intent
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.Session

class RedixCarSession : Session() {

    override fun onCreateScreen(intent: Intent): Screen {
        val targetUrl = intent.getStringExtra("url")
        if (!targetUrl.isNullOrEmpty()) {
            RedixCarPresentation.currentInstance?.loadUrl(targetUrl)
        }
        return RedixCarScreen(carContext)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val targetUrl = intent.getStringExtra("url")
        if (!targetUrl.isNullOrEmpty()) {
            RedixCarPresentation.currentInstance?.loadUrl(targetUrl)
        }
    }
}
