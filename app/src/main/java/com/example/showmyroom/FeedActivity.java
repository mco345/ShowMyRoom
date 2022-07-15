package com.example.showmyroom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kakao.sdk.template.model.Button;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class FeedActivity extends AppCompatActivity {
    private static final String TAG = "FeedActivity";

    private View followButton, messageButton, profileEditButton;
    private TextView idTextView, postNumberTextView, followerTextView, followingTextView;
    private TextView nameTextView, introTextView;
    private ImageView profileImageView;

    private String kakaoId, userId, name;
    private String thisFeedKakaoId;

    private LinearLayout notMeLayout;

    private DatabaseReference mDatabase;

    // 파이어 스토리지
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef, profileRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        // Intent 변수 받아오기
        Intent feedIntent = getIntent();
        thisFeedKakaoId = feedIntent.getStringExtra("kakaoId");

        Log.d(TAG, thisFeedKakaoId);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        idTextView = findViewById(R.id.idTextView);
        nameTextView = findViewById(R.id.nameTextView);

        mDatabase.child("users").child(thisFeedKakaoId).child("id").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                userId = String.valueOf(task.getResult().getValue());
                idTextView.setText(userId);
            }
        });
        mDatabase.child("users").child(thisFeedKakaoId).child("name").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                name = String.valueOf(task.getResult().getValue());
                nameTextView.setText(name);
            }
        });

        notMeLayout = findViewById(R.id.notMeLayout);
        followButton = findViewById(R.id.followButton);
        messageButton = findViewById(R.id.messageButton);
        profileEditButton = findViewById(R.id.profileEditButton);
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                kakaoId = String.valueOf(user.getId());
                if (kakaoId.equals(thisFeedKakaoId)){
                    notMeLayout.setVisibility(View.GONE);
                }else{
                    profileEditButton.setVisibility(View.GONE);
                }
                return null;
            }
        });

        storageRef = storage.getReference();
        profileRef = storageRef.child("Profile/" + thisFeedKakaoId + ".png");
        profileImageView = findViewById(R.id.profileImageView);
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if (getApplicationContext() != null) {
                    Glide.with(getApplicationContext()).load(uri).apply(RequestOptions.bitmapTransform(new RoundedCorners(30))).into(profileImageView);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });




    }
}