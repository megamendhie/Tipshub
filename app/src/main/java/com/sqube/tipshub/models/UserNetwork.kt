package com.sqube.tipshub.models

import kotlin.collections.ArrayList

object UserNetwork {
    var followersList: ArrayList<String>? = null
    var followingList: ArrayList<String>? = null
    var subscribedList: ArrayList<String>? = null

    val followers get() = followersList ?: arrayListOf()
    val following get() = followingList ?: arrayListOf()
    val subscribed get() = subscribedList ?: arrayListOf()
}