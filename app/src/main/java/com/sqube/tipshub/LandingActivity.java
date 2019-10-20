package com.sqube.tipshub;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import adapters.SliderAdapter;

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        Button btnJoin = findViewById(R.id.btnJoin);
        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(LandingActivity.this, LoginActivity.class));
            }
        });
        activateSlider();
    }

    private void activateSlider(){

        //Initialize the slide viewpager, tablayout, and arraylist for the slides
        List<Drawable> images= new ArrayList<>();
        List<String> bodyList = new ArrayList<>();
        ViewPager viewPager = findViewById(R.id.adsViewPager);
        TabLayout indicator = findViewById(R.id.indicator);

        //Adds the writeups to each side
        bodyList.add("Share your predictions and opinions with others");
        bodyList.add("Follow people you like.\nSubscribe for their banker tips");
        bodyList.add("Don't go searching.\nAll the sports news is here");

        //Adds the images to each slide
        images.add(getResources().getDrawable(R.drawable.sld_c));
        images.add(getResources().getDrawable(R.drawable.sld_b));
        images.add(getResources().getDrawable(R.drawable.sld_d));

        //set adapter to viewpager
        viewPager.setAdapter(new SliderAdapter(getApplicationContext(), images, bodyList));
        indicator.setupWithViewPager(viewPager, true);

    }
}
