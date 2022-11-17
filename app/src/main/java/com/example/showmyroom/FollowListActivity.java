package com.example.showmyroom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.showmyroom.activity.FeedActivity;
import com.example.showmyroom.adapter.MyRecyclerAdapter_SearchMember;
import com.example.showmyroom.items.MemberItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FollowListActivity extends AppCompatActivity {
    private static final String TAG = "FollowListActivity";

    // UI
    private TextView followTextView, followNumberTextView;
    private RecyclerView recyclerView;

    // getIntent
    private String isWhat;
    private ArrayList<String> followersList = new ArrayList<>(), followingList = new ArrayList<>();

    // adapt member
    private static final int ADAPT_VIEW = 1, DELETE_LOADING = 2;
    private MyRecyclerAdapter_SearchMember myRecyclerAdapter = new MyRecyclerAdapter_SearchMember();
    private ArrayList<MemberItem> memberItems = new ArrayList<>();

    // 실시간 데이터베이스
    private DatabaseReference mDatabase;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch(msg.what) {
                case ADAPT_VIEW:
                    // 프래그먼트 첫 호출
                    Log.d(TAG, String.valueOf(memberItems.size()));
                    recyclerView.setAdapter(myRecyclerAdapter);
                    LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                    myRecyclerAdapter.setMemberItems(memberItems);
                    recyclerView.setLayoutManager(mLayoutManager);
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_list);

        // getIntent
        isWhat = getIntent().getStringExtra("isWhat");
        followersList = getIntent().getStringArrayListExtra("followersList");
        followingList = getIntent().getStringArrayListExtra("followingList");

        // UI
        followTextView = findViewById(R.id.followTextView);
        followNumberTextView = findViewById(R.id.followNumberTextView);
        recyclerView = findViewById(R.id.recyclerView);

        // follow TextView
        switch (isWhat){
            case "follower":
                followTextView.setText("팔로워 ");
                followNumberTextView.setText(followersList.size() + "명");
                break;
            case "following":
                followTextView.setText("팔로잉 ");
                followNumberTextView.setText(followingList.size() + "명");
                break;
        }

        // adaptMember
        adaptMember(isWhat);

        // item click
        myRecyclerAdapter.setOnItemClickListener(new MyRecyclerAdapter_SearchMember.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
                intent.putExtra("kakaoId", memberItems.get(position).getKakaoId());
                startActivity(intent);
            }
        });
    }

    private void adaptMember(String isWhat) {
        mDatabase = FirebaseDatabase.getInstance().getReference();

        memberItems = new ArrayList<>();
        mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Map memberItem = new HashMap();
                    memberItem = (Map) dataSnapshot.getValue();
                    switch (isWhat){
                        case "follower":
                            if(followersList.contains(memberItem.get("kakaoId"))){
                                MemberItem item = new MemberItem(
                                        memberItem.get("kakaoId").toString(),
                                        memberItem.get("id").toString(),
                                        memberItem.get("name").toString()
                                );
                                memberItems.add(item);
                            }
                            break;
                        case "following":
                            if(followingList.contains(memberItem.get("kakaoId"))){
                                MemberItem item = new MemberItem(
                                        memberItem.get("kakaoId").toString(),
                                        memberItem.get("id").toString(),
                                        memberItem.get("name").toString()
                                );
                                memberItems.add(item);
                            }
                            break;
                    }
                    Log.d(TAG, ""+memberItems);
                }
                handler.sendEmptyMessage(ADAPT_VIEW);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}