package com.redix.carnative.car

import android.content.Context
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.util.Log
import androidx.car.app.AppManager
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.SurfaceCallback
import androidx.car.app.SurfaceContainer
import androidx.car.app.model.*
import androidx.car.app.navigation.model.NavigationTemplate

class RedixCarScreen(carContext: CarContext) : Screen(carContext) {

    private val TAG = "RedixCarScreen"
    private var virtualDisplay: VirtualDisplay? = null
    private var presentation: RedixCarPresentation? = null

    private val surfaceCallback = object : SurfaceCallback {
        override fun onSurfaceAvailable(surfaceContainer: SurfaceContainer) {
            val surface = surfaceContainer.surface
            if (surface == null || !surface.isValid) {
                Log.e(TAG, "Car host provided an invalid surface")
                return
            }

            try {
                Log.d(TAG, "onSurfaceAvailable: ${surfaceContainer.width}x${surfaceContainer.height} dpi=${surfaceContainer.dpi}")
                val displayManager = carContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

                virtualDisplay = displayManager.createVirtualDisplay(
                    "RedixCarVirtualDisplay",
                    surfaceContainer.width,
                    surfaceContainer.height,
                    surfaceContainer.dpi,
                    surface,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION
                )

                val display = virtualDisplay?.display
                if (display != null) {
                    presentation = RedixCarPresentation(carContext, display).apply {
                        show()
                    }
                    Log.d(TAG, "RedixCarPresentation mounted successfully onto car surface")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to mount presentation on car surface: ${e.message}", e)
            }
        }

        override fun onSurfaceDestroyed(surfaceContainer: SurfaceContainer) {
            Log.d(TAG, "onSurfaceDestroyed")
            try {
                presentation?.dismiss()
                presentation = null
                virtualDisplay?.release()
                virtualDisplay = null
            } catch (e: Exception) {
                Log.e(TAG, "Error cleaning up car presentation: ${e.message}")
            }
        }

        override fun onClick(x: Float, y: Float) {
            presentation?.dispatchCarClick(x, y)
        }

        override fun onScroll(distanceX: Float, distanceY: Float) {
            presentation?.dispatchCarScroll(distanceX, distanceY)
        }

        override fun onFling(velocityX: Float, velocityY: Float) {
            presentation?.dispatchCarScroll(-velocityX / 12f, -velocityY / 12f)
        }
    }

    init {
        try {
            carContext.getCarService(AppManager::class.java).setSurfaceCallback(surfaceCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Unable to register surface callback: ${e.message}")
        }
    }

    override fun onGetTemplate(): Template {
        val actionStrip = ActionStrip.Builder()
            .addAction(
                Action.Builder()
                    .setTitle("YouTube")
                    .setOnClickListener {
                        presentation?.loadUrl("https://m.youtube.com")
                    }
                    .build()
            )
            .addAction(
                Action.Builder()
                    .setTitle("Twitch")
                    .setOnClickListener {
                        presentation?.loadUrl("https://m.twitch.tv")
                    }
                    .build()
            )
            .addAction(
                Action.Builder()
                    .setTitle("Maps")
                    .setOnClickListener {
                        presentation?.loadUrl("https://maps.google.com")
                    }
                    .build()
            )
            .build()

        return NavigationTemplate.Builder()
            .setActionStrip(actionStrip)
            .build()
    }
}
