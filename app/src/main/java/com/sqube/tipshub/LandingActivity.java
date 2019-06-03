package com.sqube.tipshub;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
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
        bodyList.add("Share the joy. Share the prediction.\nShare the gist");
        bodyList.add("Follow people you like.\nSubscribe for their banker tips");
        bodyList.add("Don't go searching.\nAll the sports news is here");

        //Adds the images to each slide
        images.add(getResources().getDrawable(R.drawable.slide_a));
        images.add(getResources().getDrawable(R.drawable.slide_b));
        images.add(getResources().getDrawable(R.drawable.slide_c));

        //set adapter to viewpager
        viewPager.setAdapter(new SliderAdapter(getApplicationContext(), images, bodyList));
        indicator.setupWithViewPager(viewPager, true);

    }
}
