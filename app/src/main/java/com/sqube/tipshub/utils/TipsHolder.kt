package com.sqube.tipshub.utils

import com.sqube.tipshub.models.Tip

class TipsHolder {
    companion object{
        var tipsList = arrayListOf<Tip>()
    }
}