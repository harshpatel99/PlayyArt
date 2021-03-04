package com.twogentle.wall.extras

import android.view.animation.Interpolator
import kotlin.math.cos
import kotlin.math.pow

class BounceInterpolator(private val amplitude: Double, private val frequency: Double) : Interpolator {
    override fun getInterpolation(p0: Float): Float {
        return (-1 * Math.E.pow(-p0 / amplitude) * cos(frequency * p0) + 1).toFloat()
    }
}