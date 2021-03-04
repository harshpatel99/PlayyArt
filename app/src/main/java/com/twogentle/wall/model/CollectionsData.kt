package com.twogentle.wall.model

import java.io.Serializable

data class CollectionsData(
    val type: Int? = null,
    val title: String? = null,
    val imageUrl: String? = null,
    var isChecked:Boolean = false
) : Serializable {

    companion object {
        const val TYPE_TITLE = 0
        const val TYPE_COLLECTION = 1
    }

}