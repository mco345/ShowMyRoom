package com.example.showmyroom.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.showmyroom.PreferenceManager;
import com.example.showmyroom.R;
import com.example.showmyroom.adapter.MyPagerAdapter_Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator;

public class FeedPostUpdateActivity extends AppCompatActivity {
    private static final String TAG = "FeedPostUpdateActivity";

    // getIntent
    private String postId, postRefText, thisFeedKakaoId, content;
    private ArrayList<Uri> postUriList;
    private ArrayList<String> keywordsList;

    // UI
    private ViewPager feedPostImageViewPager;
    private CircleIndicator indicator;
    private EditText contentEditText;
    private Button updateButton;
    private ImageButton deleteButton;
    private ChipGroup chipGroup;

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
        keywordsList = (ArrayList<String>) postUpdateIntent.getSerializableExtra("keywordsList");


        // UI 선언
        contentEditText = findViewById(R.id.contentEditText);
        feedPostImageViewPager = findViewById(R.id.feedPostImageViewPager);
        indicator = findViewById(R.id.indicator);
        updateButton = findViewById(R.id.updateButton);
        deleteButton = findViewById(R.id.deleteButton);
        chipGroup = findViewById(R.id.feedPostUpdateChipGroup);

        // 내용
        contentEditText.setText(content);

        // 키워드
        try {
            LayoutInflater inflater = LayoutInflater.from(FeedPostUpdateActivity.this);
            if (keywordsList.size() == 0) chipGroup.setVisibility(View.GONE);
            for(int i =0; i<keywordsList.size(); i++){
                Log.d(TAG, i + "-"+keywordsList.get(i));
                Chip newChip = (Chip) inflater.inflate(R.layout.view_chip_feedpost_close, chipGroup, false);
                newChip.setText(keywordsList.get(i));
                chipGroup.addView(newChip);
                newChip.setOnCloseIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleChipCloseIconClicked(keywordsList, (Chip) v);
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

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
                        .update("message", update_content, "keyword", keywordsList);
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
                AlertDialog.Builder alert = new AlertDialog.Builder(FeedPostUpdateActivity.this);
                alert.setTitle("정말 삭제하시겠습니까?");
                alert.setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 게시물 데이터베이스 삭제
                        db.collection("homePosts").document(postId).delete();
                        Toast.makeText(getApplicationContext(), "삭제 완료하였습니다.", Toast.LENGTH_SHORT).show();

                        // 알림 데이터베이스 삭제(해당 게시물때문에 발생한 알림 모두 삭제)
                        db.collection("notification")
                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()){
                                    for(DocumentSnapshot document : task.getResult()){
                                        if(document.getData().get("postId").equals(postId)){
                                            db.collection("notification").document(document.getId()).delete();
                                        }
                                    }
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });

                        Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
                        intent.putExtra("kakaoId", thisFeedKakaoId);
                        intent.putExtra("isDelete", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                AlertDialog alertDialog = alert.create();
                alertDialog.show();
            }
        });
    }

    private void handleChipCloseIconClicked(ArrayList<String> keywordsList, Chip chip) {
        ChipGroup parent = (ChipGroup) chip.getParent();
        parent.removeView(chip);

        keywordsList.remove(chip.getText());
    }
}