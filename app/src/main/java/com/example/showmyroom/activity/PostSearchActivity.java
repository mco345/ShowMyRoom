package com.example.showmyroom.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.showmyroom.R;
import com.example.showmyroom.adapter.MyRecyclerAdapter_Board;
import com.example.showmyroom.adapter.MyRecyclerAdapter_Home;
import com.example.showmyroom.items.HomeItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class PostSearchActivity extends AppCompatActivity {
    private static final String TAG = "PostSearchActivity";

    // UI
    private EditText searchEditText;
    private ImageButton backButton, searchButton;
    private FrameLayout firstLayout, progressBarLayout, noResultLayout;
    private RecyclerView recyclerView;

    // search
    private String search_keyword = "";

    // Reyclerview
    private MyRecyclerAdapter_Home myRecyclerAdapterHome;

    // adaptData
    private static final int SET_POSITION = -2, LOAD_URI = -1, ADAPT = 0, LOAD_NEXTPAGE = 1, RENEW = 2;
    private int page = 1;
    private ArrayList<String> keywords = new ArrayList<>();
    private ArrayList<HomeItem> homeItems = new ArrayList<>(), complete_homeItems = new ArrayList<>();
    private ArrayList<Uri> postUriList;
    private ArrayList<ArrayList<Uri>> complete_postUriList = new ArrayList<>();
    private int pos, innerPos, count = 0;
    private int handlerMessage = 0;
    private boolean isNoResult = false, isDownload = true, is5Load = false;

    // 스토리지
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();
    private StorageReference postRef;

    // 파이어 스토어
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    //키보드
    private InputMethodManager imm;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case SET_POSITION:
                    count = 0;
                    complete_postUriList.add(postUriList);  // 로드한 uri들 저장
                    if (pos == homeItems.size() - 1) {
                        Log.d(TAG, "FINISH");
                        // 끝 도달 -> 중간에 프로그래스바 삭제
                        if (handlerMessage == LOAD_NEXTPAGE)
                            complete_homeItems.remove(null);
                        // 5개 단위 -> 끝에 프로그래스바 추가
                        if (complete_homeItems.size() == page * 5)
                            complete_homeItems.add(null);
                        sendEmptyMessage(handlerMessage);
                        break;
                    } else {
                        pos++;
                        sendEmptyMessage(LOAD_URI);
                        break;
                    }
                case LOAD_URI:
                    postUriList = new ArrayList<>();
                    if (homeItems.size() == 0) {
                        // 게시물이 없을 시
                        sendEmptyMessage(ADAPT);
                        break;
                    } else {
                        if (!isDownload) break;
                        else {
                            Log.d(TAG, pos + " - homeItems : " + homeItems.get(pos));
                            postRef = storageRef.child("Post/" + homeItems.get(pos).getThisFeedKakaoId() + "/" + homeItems.get(pos).getDate() + "/");
                            postRef.listAll()
                                    .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                                        @Override
                                        public void onSuccess(ListResult listResult) {
                                            for (int i = 0; i < listResult.getItems().size(); i++) {
                                                postUriList.add(Uri.parse(""));
                                            }
                                            Log.d(TAG, "size : " + postUriList.size());
                                            for (StorageReference item : listResult.getItems()) {
                                                // reference의 item(이미지) url 받아오기
                                                Log.d(TAG, "item : " + item);
                                                item.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Uri> task) {
                                                        if (task.isSuccessful()) {
                                                            // 한 게시물 내의 사진 간에 순서대로 정렬
                                                            innerPos = Integer.parseInt(item.toString().substring(item.toString().length() - 5, item.toString().length() - 4));
                                                            postUriList.set(innerPos, task.getResult());
                                                            count++;
                                                            // 한 게시물 내의 모든 사진 불러왔다면 리스트 추가
                                                            if (count == listResult.getItems().size()) {
                                                                complete_homeItems.add(new HomeItem(
                                                                        homeItems.get(pos).getWhatSelected(),
                                                                        homeItems.get(pos).getThisFeedKakaoId(),
                                                                        homeItems.get(pos).getUserId(),
                                                                        homeItems.get(pos).getMessage(),
                                                                        homeItems.get(pos).getDate(),
                                                                        homeItems.get(pos).getLikeNum(),
                                                                        homeItems.get(pos).getCommentNum(),
                                                                        homeItems.get(pos).getId(),
                                                                        homeItems.get(pos).getPy(),
                                                                        homeItems.get(pos).getDwell(),
                                                                        homeItems.get(pos).getStyle(),
                                                                        homeItems.get(pos).getkeyword(),
                                                                        homeItems.get(pos).getLikeList(),
                                                                        postUriList
                                                                ));
                                                                sendEmptyMessage(SET_POSITION);
                                                            }

                                                        } else {
                                                            Log.d(TAG, "download fail");
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                        }
                    }
                    break;
                case ADAPT:
                    // 프래그먼트 첫 호출
                    Log.d(TAG, "complete homeItems size : " + complete_homeItems.size());

                    recyclerView.setAdapter(myRecyclerAdapterHome);
                    LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                    myRecyclerAdapterHome.setHomeList(complete_homeItems, page);
                    recyclerView.setLayoutManager(mLayoutManager);

                    progressBarLayout.setVisibility(View.GONE);
                    break;
                case LOAD_NEXTPAGE:
                    // 다음 페이지 로딩
                    myRecyclerAdapterHome.setHomeList(complete_homeItems, page);
                    is5Load = false;
                    Log.d(TAG, "loaded complete homeItems size : " + complete_homeItems.size());
                    break;
                case RENEW:
                    // 갱신
                    myRecyclerAdapterHome.setHomeList(complete_homeItems, page);
                    myRecyclerAdapterHome.notifyDataSetChanged();
                    break;

            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_search);

        // 키보드가 View에 아무런 영향 주지 않게
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        // 키보드
        imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        // UI
        backButton = findViewById(R.id.backButton);
        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        firstLayout = findViewById(R.id.firstLayout);
        progressBarLayout = findViewById(R.id.progressBar);
        noResultLayout = findViewById(R.id.noResult);
        recyclerView = findViewById(R.id.recyclerView);

        // Recyclerview Adapt
        myRecyclerAdapterHome = new MyRecyclerAdapter_Home();
        myRecyclerAdapterHome.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);

        // 끝도달
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!recyclerView.canScrollVertically(1)) {
                    if (!is5Load) {
                        // 게시물이 5개단위로 로드되어있을 때(끝에 프로그래스바 존재) -> 더 로딩(로드할 게 없으면 뒤에서 처리함)
                        if (!(homeItems.size() == 0) && homeItems.size() % 5 == 0) {
                            Log.d(TAG, "@@@@@@@@@@@@끝 도달@@@@@@@@@@@@");
                            is5Load = true;
                            pos++; // 더 로딩할 것이 있다는 가정하에(없으면 뒤에서 처리) 다음 index로 이동
                            page++; // page = 2,3,4...
                            Log.d(TAG, "homeItems size : " + homeItems.size() + ", page : " + page);
                            adaptData(LOAD_NEXTPAGE, page);
                        }
                    }
                }
            }
        });

        // 게시물 선택
        myRecyclerAdapterHome.setOnItemClickListener(new MyRecyclerAdapter_Board.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Intent intent = new Intent(getApplicationContext(), FeedPostActivity.class);
                intent.putExtra("postRef", "Post/" + complete_homeItems.get(position).getThisFeedKakaoId() + "/" + complete_homeItems.get(position).getDate() + "/");
                intent.putExtra("postId", complete_homeItems.get(position).getId());
                intent.putExtra("thisFeedKakaoId", complete_homeItems.get(position).getThisFeedKakaoId());
                startActivity(intent);
            }
        });

        // 뒤로가기
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 검색
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                search();
                return false;
            }
        });
    }

    private void search() {
        if(searchEditText.getText().toString().length() == 0){
            Toast.makeText(getApplicationContext(), "키워드를 입력해주세요", Toast.LENGTH_SHORT).show();
        }else{
            // 초기화
            pos = 0;    // index -> 0번부터
            page = 1;   // page -> 첫페이지로
            isDownload = true;  // 다운로드 가능하게
            homeItems = new ArrayList<>();
            complete_homeItems = new ArrayList<>();
            complete_postUriList = new ArrayList<>();
            // editText 작업
            searchEditText.clearFocus();
            imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
            search_keyword = "#"+ searchEditText.getText().toString();
            // layout visibility 작업
            firstLayout.setVisibility(View.GONE);
            noResultLayout.setVisibility(View.GONE);
            progressBarLayout.setVisibility(View.VISIBLE);
            // adapt
            adaptData(ADAPT, page);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "resume");
        if (!isNoResult) {
            Query first = db.collection("homePosts").orderBy("date", Query.Direction.DESCENDING);
            first.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                int count = 0;
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    keywords = (ArrayList<String>) document.getData().get("keyword");
                                    if (keywords.contains(search_keyword))
                                    {
                                        complete_homeItems.set(count, new HomeItem(
                                                homeItems.get(count).getWhatSelected(),
                                                homeItems.get(count).getThisFeedKakaoId(),
                                                homeItems.get(count).getUserId(),
                                                homeItems.get(count).getMessage(),
                                                homeItems.get(count).getDate(),
                                                document.getData().get("likeNum").toString(),
                                                document.getData().get("commentNum").toString(),
                                                homeItems.get(count).getId(),
                                                homeItems.get(count).getPy(),
                                                homeItems.get(count).getDwell(),
                                                homeItems.get(count).getStyle(),
                                                homeItems.get(count).getkeyword(),
                                                (ArrayList<String>) document.getData().get("likeList"),
                                                complete_postUriList.get(count)
                                        ));
                                        Log.d(TAG, "renew - i : " + count + ", date : " + document.getData().get("date") + ", likeNum : " + document.getData().get("likeNum"));
                                        count++;
                                        if (count == homeItems.size()) break;
                                    }

                                }
                                handler.sendEmptyMessage(RENEW);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
    }

    private void adaptData(int send_message, int pg) {
        Query first = db.collection("homePosts").orderBy("date", Query.Direction.DESCENDING);
        first.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            homeItems = new ArrayList<>();
                            keywords = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                keywords = (ArrayList<String>) document.getData().get("keyword");
                                if(keywords.contains(search_keyword)){
                                    homeItems.add(new HomeItem(
                                            document.getData().get("whatSelected").toString(),
                                            document.getData().get("kakaoId").toString(),
                                            document.getData().get("id").toString(),
                                            document.getData().get("message").toString(),
                                            document.getData().get("date").toString(),
                                            document.getData().get("likeNum").toString(),
                                            document.getData().get("commentNum").toString(),
                                            document.getId(),
                                            document.getData().get("py").toString(),
                                            document.getData().get("dwell").toString(),
                                            document.getData().get("style").toString(),
                                            document.getData().get("keyword").toString(),
                                            (ArrayList<String>) document.getData().get("likeList")
                                    ));
                                }
                                // 5개 단위씩 자르되 page 고려해서
                                if (homeItems.size() == pg * 5) {
                                    break;
                                }

                            }
                            if(homeItems.size() == 0){
                                isNoResult = true;
                                progressBarLayout.setVisibility(View.GONE); // 로딩 멈추기
                                noResultLayout.setVisibility(View.VISIBLE);   // 게시물이 없습니다.
                            }

                            // 불러온 게시물이 5개 단위인데 이미 다 로드되어 있다면? -> 더이상 로딩x
                            if (homeItems.size() != pg * 5
                                    && homeItems.size() % 5 == 0
                                    && homeItems.size() != 0) {
                                Log.d(TAG, "is Download false");
                                isDownload = false;
                                complete_homeItems.remove(null);
                                handler.sendEmptyMessage(LOAD_NEXTPAGE);
                            } else {
                                Log.d(TAG, "여기여기여기 : " + homeItems.size());
                                handlerMessage = send_message;
                                handler.sendEmptyMessage(LOAD_URI);
                            }
                        }
                    }
                });
    }
}