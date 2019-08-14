package adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.sqube.tipshub.R;

import java.util.List;

public class SliderAdapter extends PagerAdapter {

    private Context context;
    //private List<Integer> imageDrawable;

    private List<Drawable> images;
    private List<String> bodyList;

    public SliderAdapter(Context context, List<Drawable>images, List<String> bodyList){
        this.context = context;
        this.images = images;
        this.bodyList = bodyList;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position){
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.landing_slider, null);

        ImageView adsImage = view.findViewById(R.id.adsImage);
        TextView txtBody = view.findViewById(R.id.txtBody);

        RequestOptions requestOptions = new RequestOptions();
        //requestOptions.placeholder(R.drawable.bg);
        txtBody.setText(bodyList.get(position));
        Glide.with(context).setDefaultRequestOptions(requestOptions).load(images.get(position)).into(adsImage);

        ViewPager viewPager = (ViewPager) container;
        viewPager.addView(view, 0);

        return view;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ViewPager viewPager = (ViewPager) container;
        View view = (View) object;
        viewPager.removeView(view);
    }
}
