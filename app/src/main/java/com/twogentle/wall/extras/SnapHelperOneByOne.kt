package com.twogentle.wall.extras

import android.util.Log
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView

class SnapHelperOneByOne : LinearSnapHelper() {

    override fun findTargetSnapPosition(
        layoutManager: RecyclerView.LayoutManager?,
        velocityX: Int,
        velocityY: Int
    ): Int {
        if (layoutManager !is RecyclerView.SmoothScroller.ScrollVectorProvider) {
            return RecyclerView.NO_POSITION
        }

        val currentView = findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION

        var currentPosition: Int = layoutManager.getPosition(currentView)

        if(velocityY > 5000){
            currentPosition += 1
        }
        if(velocityY < -5000){
            currentPosition -= 1
        }

        if (currentPosition == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION
        }

        return currentPosition

    }

}