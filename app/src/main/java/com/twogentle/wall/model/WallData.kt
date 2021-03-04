package com.twogentle.wall.model

data class WallData(val type: Int, val url: String, val id: String, var post: Post? = null) {

    companion object {
        const val TYPE_TITLE = 0
        const val TYPE_IMAGE = 1
        const val TYPE_IMAGE_LOCKED = 2
        const val TYPE_VIDEO = 3
    }

}