package com.turtlepaw.overlay.overlay

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.turtlepaw.overlay.OverlayService
import com.turtlepaw.overlay.isServiceForegrounded

fun Context.hideOverlay() {
    val intent = Intent(this, OverlayService::class.java)
    stopService(intent)
}

fun Context.startOverlayService(){
    if(!Settings.canDrawOverlays(this)) launchOverlayPermissions()

    val intent = Intent(this, OverlayService::class.java)
    startForegroundService(intent)
}

fun Context.showOverlay(){
    if(!Settings.canDrawOverlays(this)) launchOverlayPermissions()

    val intent = Intent(this, OverlayService::class.java)
        .apply {
            putExtra("play", true)
        }
    startForegroundService(intent)
}

private fun Context.launchOverlayPermissions(){
    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
    startActivity(intent)
}
