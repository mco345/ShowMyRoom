package com.example.showmyroom;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.showmyroom.activity.FeedPostActivity;
import com.example.showmyroom.activity.MainActivity;
import com.example.showmyroom.adapter.MyPagerAdapter_Post;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator;

public class FeedPostUpdateActivity extends AppCompatActivity {
    private static final String TAG = "FeedPostUpdateActivity";

    // getIntent
    private String postId, postRefText, thisFeedKakaoId, content;
    private ArrayList<Uri> postUriList;

    // UI
    private ViewPager feedPostImageViewPager;
    private CircleIndicator indicator;
    private EditText contentEditText;
    private Button updateButton;
    private ImageButton deleteButton;

    // 뷰페이저 어댑터
    private MyPagerAdapter_Post pagerAdapter;

    // 파이어스토어
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_post_update);

        // getIntent
        Intent postUpdateIntent = getIntent();
        postId = postUpdateIntent.getStringExtra("postId");
        postRefText = postUpdateIntent.getStringExtra("postRef");
        thisFeedKakaoId = postUpdateIntent.getStringExtra("thisFeedKakaoId");
        postUriList = (ArrayList<Uri>) postUpdateIntent.getSerializableExtra("thisPostUriList");
        content = postUpdateIntent.getStringExtra("content");

        // UI 선언
        contentEditText = findViewById(R.id.contentEditText);
        feedPostImageViewPager = findViewById(R.id.feedPostImageViewPager);
        indicator = findViewById(R.id.indicator);
        updateButton = findViewById(R.id.updateButton);
        deleteButton = findViewById(R.id.deleteButton);

        // 내용
        contentEditText.setText(content);

        // 뷰페이저
        pagerAdapter = new MyPagerAdapter_Post(FeedPostUpdateActivity.this);
        pagerAdapter.setPostImage(postUriList);
        feedPostImageViewPager.setAdapter(pagerAdapter);
        indicator.setViewPager(feedPostImageViewPager);

        // 파이어스토어
        db = FirebaseFirestore.getInstance();

        // 업데이트 버튼
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String update_content = contentEditText.getText().toString();
                db.collection("homePosts").document(postId)
                        .update("message", update_content);
                Toast.makeText(getApplicationContext(), "수정을 완료하였습니다.", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(), FeedPostActivity.class);
                intent.putExtra("postRef", postRefText);
                intent.putExtra("postId", postId);
                intent.putExtra("thisFeedKakaoId", thisFeedKakaoId);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        // 삭제 버튼
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("homePosts").document(postId).delete();
                Toast.makeText(getApplicationContext(), "삭제 완료하였습니다.", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
}