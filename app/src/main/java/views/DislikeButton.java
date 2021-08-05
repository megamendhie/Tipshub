package views;


import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.sqube.tipshub.R;

@SuppressLint("AppCompatCustomView")
public class DislikeButton extends ImageView {
    String TAG = DislikeButton.class.getSimpleName();

    public static final int DISLIKED = 1;
    public static final int NOT_DISLIKED = -1;

    private int state;
    public DislikeButton(Context context) {
        super(context);
    }

    public DislikeButton(Context context, AttributeSet attr){
        super(context, attr);
    }

    private void init() {
        switch (state){
            case DISLIKED:
                setImageResource(R.drawable.ic_thumbs_down_color_alt);
                break;
            default:
                setImageResource(R.drawable.ic_thumbs_down_alt);
                break;
        }
    }


    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
        init();
    }
}
