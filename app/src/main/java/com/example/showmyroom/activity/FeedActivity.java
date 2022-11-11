package com.example.showmyroom.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.showmyroom.Notification;
import com.example.showmyroom.PreferenceManager;
import com.example.showmyroom.R;
import com.example.showmyroom.adapter.MyGridAdapter_Feed;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class FeedActivity extends AppCompatActivity {
    private static final String TAG = "FeedActivity";

    private Button followButton, messageButton, profileEditButton;
    private TextView idTextView, postNumberTextView, followerTextView, followingTextView;
    private TextView nameTextView, introTextView;
    private ImageView profileImageView;

    private String kakaoId, userId, name;
    private String thisFeedKakaoId;

    // 팔로우
    private boolean isFollow = false;
    private String thisFeedFollowingKey, thisFeedFollowerKey, followingKey, followersKey;
    private ArrayList<String> followersList, followingList;

    private LinearLayout notMeLayout;

    private MyGridAdapter_Feed myGridAdapterFeed;

    private DatabaseReference mDatabase;

    // 게시물 없음
    private LinearLayout noResultLayout;

    // 프로그래스바
    private LinearLayout progressBarLayout;

    // 그리드뷰
    private GridView postGridView;
    private MyGridAdapter_Feed gridAdapter;
    private ArrayList<String> date = new ArrayList<>();
    private ArrayList<String> postIdList = new ArrayList<>();
    private ArrayList<Uri> postUriList = new ArrayList<>();
    private ArrayList<List> postUriList2 = new ArrayList<>();
    private String realFileName;
    int pos, innerPos, count = 0;

    // 파이어 스토리지
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef, profileRef, postRef;

    // Notification
    private Notification notification = new Notification();

    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case -1:
                    pos++;
                    count = 0;
                    if(pos == date.size()){
                        Log.d(TAG, "finish");
                        sendEmptyMessage(1);
                        break;
                    }else{
                        Log.d(TAG, "pos : "+pos);
                        sendEmptyMessage(0);
                    }
                    break;
                case 0:
                    postUriList = new ArrayList<>();
                    if(date.size()!=0){
                        postRef = storageRef.child("Post/" + thisFeedKakaoId + "/thumbNail/");
                        postRef.listAll()
                                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                                    @Override
                                    public void onSuccess(ListResult listResult) {
                                        for(int i = 0; i<date.size();i++){
                                            postUriList.add(Uri.parse(""));
                                        }
                                        Log.d(TAG, "size : "+postUriList.size());
                                        for (StorageReference item : listResult.getItems()) {
                                            // reference의 item(이미지) url 받아오기
                                            Log.d(TAG, "item : "+item.toString());
                                            item.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Uri> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, String.valueOf(listResult.getItems().size()) + "  " + postRef.getPath());
                                                        realFileName = item.toString().substring(item.toString().length()-17, item.toString().indexOf(".png"));
                                                        Log.d(TAG, "realFileName : "+realFileName + ", index : "+date.indexOf(realFileName));
                                                        if(date.indexOf(realFileName) != -1){
                                                            postUriList.set(date.indexOf(realFileName), task.getResult());
                                                            Log.d(TAG, "postUriList : "+postUriList);
                                                            count++;
                                                            if(count == postUriList.size()){
//                                                            postUriList2.add(postUriList);
//                                                            Log.d(TAG, "postUriList index : "+ pos + ", postUriList : " + postUriList2);
                                                                sendEmptyMessage(1);
                                                            }
                                                        }
                                                    } else {
                                                        Log.d(TAG, "download fail");
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "download fail");
                            }
                        });
                    }

                    break;
                case 1:
                    // 프래그먼트 첫 호출
//                    Log.d(TAG, "2 postUriList : "+postUriList);
//                    postUriList = new ArrayList<>();
//                    for(int i = 0; i < postUriList2.size(); i++){
//                        postUriList.add((Uri) postUriList2.get(i).get(0));
//                    }
                    Log.d(TAG, "Complete postUriList : "+ postUriList);
                    gridAdapter.setUriList(postUriList);
                    postGridView.setAdapter(gridAdapter);
                    progressBarLayout.setVisibility(View.GONE);
                    sendEmptyMessage(2);
                    break;
                case 2:
                    postGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Log.d(TAG, "post Clicked : "+date.get(position));
                            Intent feedIntent = new Intent(getApplicationContext(), FeedPostActivity.class);
                            feedIntent.putExtra("postRef", "Post/" + thisFeedKakaoId + "/" + date.get(position) + "/");
                            feedIntent.putExtra("postId", postIdList.get(position));
                            feedIntent.putExtra("thisFeedKakaoId", thisFeedKakaoId);
                            startActivity(feedIntent);
                        }
                    });
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        // Intent 변수 받아오기
        Intent feedIntent = getIntent();
        thisFeedKakaoId = feedIntent.getStringExtra("kakaoId");

        Log.d(TAG, thisFeedKakaoId);

        noResultLayout = findViewById(R.id.noResultLayout);
        progressBarLayout = findViewById(R.id.progressBarLayout);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        idTextView = findViewById(R.id.idTextView);
        nameTextView = findViewById(R.id.nameTextView);

        // 아이디
        mDatabase.child("users").child(thisFeedKakaoId).child("id").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                userId = String.valueOf(task.getResult().getValue());
                idTextView.setText(userId);
            }
        });
        // 이름
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

        // 팔로우
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isFollow){
                    Map<String, Object> followersMap = new HashMap<>();
                    followersMap.put(followersKey, kakaoId);
                    mDatabase.child("followers").child(thisFeedKakaoId).updateChildren(followersMap);

                    Map<String, Object> followingMap = new HashMap<>();
                    followingMap.put(followingKey, thisFeedKakaoId);
                    mDatabase.child("following").child(kakaoId).updateChildren(followingMap);

                    notification.notice_follow(thisFeedKakaoId, kakaoId);
                }else{
                    mDatabase.child("following").child(kakaoId).child(thisFeedFollowingKey).removeValue();
                    mDatabase.child("followers").child(thisFeedKakaoId).child(thisFeedFollowerKey).removeValue();

                    notification.notice_unFollow(thisFeedKakaoId, kakaoId);
                }
                Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
                intent.putExtra("kakaoId", thisFeedKakaoId);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        });

        followerTextView = findViewById(R.id.followerNumberTextView);
        followingTextView = findViewById(R.id.followingNumberTextView);

        // 본인인지에 따라 UI 변경
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                // 본인인지 아닌지
                kakaoId = String.valueOf(user.getId());
                if (kakaoId.equals(thisFeedKakaoId)){
                    notMeLayout.setVisibility(View.GONE);
                }else{
                    profileEditButton.setVisibility(View.GONE);
                }

                // 팔로우, 팔로워
                followersKey = mDatabase.child("followers").child(thisFeedKakaoId).push().getKey();
                followingKey = mDatabase.child("following").child(kakaoId).push().getKey();

                // 팔로우한 회원인지 체크
                mDatabase.child("following").child(kakaoId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            String followKakaoId = String.valueOf(dataSnapshot.getValue());
                            Log.d(TAG, " followKakaoId : "+followKakaoId);
                            // 해당 회원을 팔로우했다면
                            if(followKakaoId.equals(thisFeedKakaoId)){
                                thisFeedFollowingKey = String.valueOf(dataSnapshot.getKey());
                                isFollow = true;
                                followButton.setText("언팔로우");
                                followButton.setTextColor(Color.BLACK);
                                followButton.setBackgroundColor(Color.WHITE);
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                // 해당 회원 팔로워, 팔로잉 리스트
                followersList = new ArrayList<>();
                followingList = new ArrayList<>();

                mDatabase.child("followers").child(thisFeedKakaoId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            followersList.add(String.valueOf(dataSnapshot.getValue()));
                            if(String.valueOf(dataSnapshot.getValue()).equals(kakaoId)){
                                thisFeedFollowerKey = String.valueOf(dataSnapshot.getKey());
                            }
                        }
                        followerTextView.setText(String.valueOf(followersList.size()));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
                mDatabase.child("following").child(thisFeedKakaoId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            followingList.add(String.valueOf(dataSnapshot.getValue()));
                        }
                        followingTextView.setText(String.valueOf(followingList.size()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });



                return null;
            }
        });

        // 프사
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

        postGridView = findViewById(R.id.postGridView);
        gridAdapter = new MyGridAdapter_Feed(this);

        // 게시물 작성한 시간 받아오기
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query first = db.collection("homePosts").orderBy("date", Query.Direction.DESCENDING);
        first.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                if (document.getData().get("kakaoId").toString().equals(thisFeedKakaoId)){
                                    date.add(document.getData().get("date").toString());
                                    postIdList.add(document.getId());
                                }
                            }
                            Log.d(TAG, "date list : "+ date.toString());

                            postNumberTextView = findViewById(R.id.postNumberTextView);
                            postNumberTextView.setText(String.valueOf(date.size()));
                            if(date.size() != 0){
                                handler.sendEmptyMessage(0);
                            }else{
                                progressBarLayout.setVisibility(View.GONE);
                                noResultLayout.setVisibility(View.VISIBLE);
                            }

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });





    }
}