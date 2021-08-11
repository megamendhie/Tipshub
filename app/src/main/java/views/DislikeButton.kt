package views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import views.DislikeButton
import com.sqube.tipshub.R

@SuppressLint("AppCompatCustomView")
class DislikeButton : ImageView {
    var TAG = DislikeButton::class.java.simpleName
    private var state = 0

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attr: AttributeSet?) : super(context, attr) {}

    private fun init() {
        when (state) {
            DISLIKED -> setImageResource(R.drawable.ic_thumbs_down_color_alt)
            else -> setImageResource(R.drawable.ic_thumbs_down_alt)
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
        const val DISLIKED = 1
        const val NOT_DISLIKED = -1
    }
}