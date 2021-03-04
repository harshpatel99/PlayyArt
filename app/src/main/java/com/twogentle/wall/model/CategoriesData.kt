package com.twogentle.wall.model

data class CategoriesData(
    val type: Int? = null,
    val title: String? = null,
    val imageUrl: String? = null,
    val collection: String? = null
) {

    companion object {
        const val TYPE_TITLE = 0
        const val TYPE_CATEGORY = 1
    }

}