package com.twogentle.wall.model

import java.io.Serializable

data class Post(
    var postType: Int? = null,
    val title: String? = null,
    val id: String? = null,
    var url: String? = null,
    val datePosted: Long? = null,
    val views: Int? = null,
    var likes: Int? = null,
    var saves: Int? = null,
    val shares: Int? = null,
    val artistID: String? = null,
    val artistName: String? = null,
    val artistUsername: String? = null,
    val artistBio: String? = null,
    val artistProfilePic: String? = null,
    val artistSocial: HashMap<String, String>? = null,
    val category: String? = null,
    val collection: String? = null,
    val type: String? = null,
    val userLiked: List<String>? = null,
    val userSaved: List<String>? = null,
    val userUnlocked: List<String>? = null,
    val contentType: String? = null,
    var thumbnail: String? = null,
    var date: Long? = null,
    val random: Long? = null,
    val instagramUrl: String? = null
) : Serializable {

    companion object {
        const val TYPE_TITLE = 0
        const val TYPE_POST = 1
        const val TYPE_POLL = 2
        const val TYPE_POST_LOCKED = 3
        const val TYPE_VIDEO = 4
        const val TYPE_LOADING = 5
        const val TYPE_COMIC = 6
        const val TYPE_NATIVE_AD = 9
        const val TYPE_UNAVAILABLE = 99
    }

}