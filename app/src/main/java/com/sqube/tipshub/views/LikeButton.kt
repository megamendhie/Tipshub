package com.sqube.tipshub.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import com.sqube.tipshub.R

@SuppressLint("AppCompatCustomView")
class LikeButton : ImageView {
    var TAG = LikeButton::class.java.simpleName
    private var state = 0

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attr: AttributeSet?) : super(context, attr) {}

    private fun init() {
        when (state) {
            LIKED -> setImageResource(R.drawable.ic_thumbs_up_color_alt)
            else -> setImageResource(R.drawable.ic_thumbs_up_alt)
        }
    }

    fun getState(): Int {
        return state
    }

    fun setState(state: Int) {
        this.state = state
        init()
    }

    companion object {
        const val LIKED = 1
        const val NOT_LIKED = -1
    }
}