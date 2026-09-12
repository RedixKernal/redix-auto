package com.redix.carnative.media

import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.redix.carnative.car.RedixCarPresentation

object CarMediaSessionManager {

    private var mediaSession: MediaSessionCompat? = null

    fun initSession(context: Context) {
        if (mediaSession != null) return

        mediaSession = MediaSessionCompat(context, "RedixCarMediaSession").apply {
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                }

                override fun onPause() {
                    updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
                }

                override fun onSkipToNext() {
                    RedixCarPresentation.currentInstance?.loadUrl("https://m.youtube.com")
                }

                override fun onSkipToPrevious() {
                    RedixCarPresentation.currentInstance?.loadUrl("https://m.youtube.com")
                }
            })

            isActive = true
        }

        updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
    }

    private fun updatePlaybackState(state: Int) {
        val stateBuilder = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_STOP
            )
            .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)

        mediaSession?.setPlaybackState(stateBuilder.build())
    }
}
