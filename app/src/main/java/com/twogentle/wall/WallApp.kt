package com.twogentle.wall

import android.app.Application
import com.twogentle.wall.extras.FontOverride

class WallApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FontOverride.setDefaultFont(this, "DEFAULT", "font/nexa.ttf")
        FontOverride.setDefaultFont(this, "MONOSPACE", "font/nexa.ttf")
        FontOverride.setDefaultFont(this, "SERIF", "font/nexa.ttf")
        FontOverride.setDefaultFont(this, "SANS_SERIF", "font/nexa.ttf")
    }
}