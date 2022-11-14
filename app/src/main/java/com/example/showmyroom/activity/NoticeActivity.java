package com.example.showmyroom.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.example.showmyroom.R;
import com.example.showmyroom.adapter.MyRecyclerAdapter_Notice;
import com.example.showmyroom.items.NoticeItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class NoticeActivity extends AppCompatActivity {
    private static final String TAG = "NoticeActivity";

    // getIntent
    private String myKakaoId;

    // ArrayList NoticeItem
    private ArrayList<NoticeItem> noticeItems = new ArrayList<>();

    // 파이어스토어
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    // UI
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private LinearLayout noNoticeLayout;

    // RecyclerView Adapter
    private MyRecyclerAdapter_Notice myRecyclerAdapterNotice;

    // 초기화면
    private int page = 1;

    // Handler
    private static final int ADAPT = 0, RENEW = 1;


    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case ADAPT:
                    // 프래그먼트 첫 호출
                    recyclerView.setAdapter(myRecyclerAdapterNotice);
                    LinearLayoutManager mLayoutManager = new LinearLayoutManager(NoticeActivity.this);
                    myRecyclerAdapterNotice.setNoticeItems(noticeItems, page);
                    recyclerView.setLayoutManager(mLayoutManager);
                    break;
                case RENEW:
                    // 갱신
                    myRecyclerAdapterNotice.setNoticeItems(noticeItems, page);
                    myRecyclerAdapterNotice.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);

        // getIntent
        Intent noticeIntent = getIntent();
        myKakaoId = noticeIntent.getStringExtra("myKakaoId");

        // UI
        noNoticeLayout = findViewById(R.id.noNoticeLayout);
        recyclerView = findViewById(R.id.recyclerView_notice);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshBoard_notice);

        // RecyclerView Adapter
        myRecyclerAdapterNotice = new MyRecyclerAdapter_Notice();

        get_noticeItems(ADAPT, page);

        // 게시물 선택
        myRecyclerAdapterNotice.setOnItemClickListener(new MyRecyclerAdapter_Notice.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                switch (noticeItems.get(position).getWhatBoard()) {
                    case "피드":
                        firestore.collection("homePosts").get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (DocumentSnapshot document : task.getResult()) {
                                                if (document.getId().equals(noticeItems.get(position).getPostId())) {
                                                    Intent intent = new Intent(getApplicationContext(), FeedPostActivity.class);
                                                    intent.putExtra("postRef", "Post/" + noticeItems.get(position).getThisPostKakaoId() + "/" + document.getData().get("date").toString() + "/");
                                                    intent.putExtra("postId", noticeItems.get(position).getPostId());
                                                    intent.putExtra("thisFeedKakaoId", noticeItems.get(position).getThisPostKakaoId());
                                                    startActivity(intent);
                                                }
                                            }
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                        break;
                    case "팔로우":
                        Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
                        intent.putExtra("kakaoId", noticeItems.get(position).getMyKakaoId());
                        startActivity(intent);
                        break;
                }

            }

            @Override
            public void onProfileClick(View v, int position) {
                Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
                intent.putExtra("kakaoId", noticeItems.get(position).getMyKakaoId());
                startActivity(intent);
            }
        });

        // swipe refresh
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                /* swipe 시 진행할 동작 */
                page = 1;
                get_noticeItems(RENEW, page);

                /* 업데이트가 끝났음을 알림 */
                swipeRefreshLayout.setRefreshing(false);
            }
        });

//        // 끝도달
//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
//                if (!recyclerView.canScrollVertically(1)) {
//                    Log.d(TAG, "@@@@@@@@@@@@끝 도달@@@@@@@@@@@@");
//                    page++;
//                    get_noticeItems(page);
//                }
//            }
//        });

        myRecyclerAdapterNotice.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
    }

    private void get_noticeItems(int handlerMessage, int page) {
        noticeItems = new ArrayList<>();
        Query first = firestore.collection("notification").orderBy("date", Query.Direction.DESCENDING);
        ;
        first.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                if (document.getData().get("type").toString().equals("reply")) {
                                    if (document.getData().get("realKakaoId").toString().equals(myKakaoId)) {
                                        noticeItems.add(new NoticeItem(
                                                document.getData().get("whatBoard").toString(),
                                                document.getData().get("postId").toString(),
                                                document.getData().get("thisPostKakaoId").toString(),
                                                document.getData().get("myKakaoId").toString(),
                                                document.getData().get("realKakaoId").toString(),
                                                document.getData().get("message").toString(),
                                                document.getData().get("type").toString(),
                                                document.getData().get("date").toString(),
                                                document.getData().get("comment_date").toString()
                                        ));
                                    }
                                } else {
                                    if (document.getData().get("thisPostKakaoId").toString().equals(myKakaoId)) {
                                        noticeItems.add(new NoticeItem(
                                                document.getData().get("whatBoard").toString(),
                                                document.getData().get("postId").toString(),
                                                document.getData().get("thisPostKakaoId").toString(),
                                                document.getData().get("myKakaoId").toString(),
                                                document.getData().get("realKakaoId").toString(),
                                                document.getData().get("message").toString(),
                                                document.getData().get("type").toString(),
                                                document.getData().get("date").toString(),
                                                document.getData().get("comment_date").toString()
                                        ));
                                    }
                                }

                                if (noticeItems.size() == 10 * page) {
                                    noticeItems.add(null);
                                    break;
                                }

                                if(noticeItems.size() == 0){
                                    noNoticeLayout.setVisibility(View.VISIBLE);
                                }
                            }

                            Log.d(TAG, "" + noticeItems);
                            handler.sendEmptyMessage(handlerMessage);

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
}