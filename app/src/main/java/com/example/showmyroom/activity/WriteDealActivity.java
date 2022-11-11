package com.example.showmyroom.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.example.showmyroom.R;
import com.example.showmyroom.adapter.MyPagerAdapter_Post;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.relex.circleindicator.CircleIndicator;

public class WriteDealActivity extends AppCompatActivity {
    // UI
    private EditText titleEditText, tagEditText, contentEditText;
    private Button uploadButton;

    // 이미지 뷰페이저
    private ViewPager pager;
    private MyPagerAdapter_Post pagerAdapter;
    private CircleIndicator circleIndicator;

    // uriList
    ArrayList<Uri> uriList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_deal);

        uriList = (ArrayList<Uri>) getIntent().getSerializableExtra("uriList");

        //UI
        pager = findViewById(R.id.dealImageViewPager);
        pagerAdapter = new MyPagerAdapter_Post(WriteDealActivity.this);
        pagerAdapter.setPostImage(uriList);
        pager.setAdapter(pagerAdapter);
        circleIndicator = findViewById(R.id.indicator);
        circleIndicator.setViewPager(pager);

        //




    }
}