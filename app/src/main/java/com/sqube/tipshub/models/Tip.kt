package com.sqube.tipshub.models

data class Tip(
    val awayTeam: String? = null,
    val homeTeam: String? = null,
    val region: String? = null,
    val league: String? = null,
    val prediction: String? = null,
    val time: String? = null,
    val status: String? = null
)
