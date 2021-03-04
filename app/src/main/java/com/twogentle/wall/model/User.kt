package com.twogentle.wall.model

data class User(
    val name: String? = null,
    val email: String? = null,
    val joinType: String? = null,
    val timeZone: String? = null,
    val dateJoined: Long? = null,
    val likedCategories: ArrayList<String>? = null,
    val artPoints: Int? = null,
    val weeklyTime: Int? = null,
    val dailyTime: Int? = null,
    val streakStartDate: Long? = null,
    val streakEndDate: Long? = null,
    val token: String? = null,
    val userType: Int? = null,
    val subscription: Int? = null
) {

    companion object {
        const val SIGN_IN_TYPE_EMAIL = "email"
        const val SIGN_IN_TYPE_GOOGLE = "google"

        const val USER_FREE = 0
        const val USER_IAP = 1

        const val USER_SUBSCRIBED_ARTS = 2
        const val USER_SUBSCRIBED_COMICS = 3
        const val USER_SUBSCRIBED_FULL = 4

        const val DEFAULT_ART_POINTS = 50
    }

}