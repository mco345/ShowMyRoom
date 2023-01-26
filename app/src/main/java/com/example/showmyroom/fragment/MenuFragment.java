package com.example.showmyroom.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.showmyroom.items.BoardItem;
import com.example.showmyroom.items.CommentItem;
import com.example.showmyroom.adapter.MyRecyclerAdapter_Board;
import com.example.showmyroom.PreferenceManager;
import com.example.showmyroom.R;
import com.example.showmyroom.activity.MainActivity;
import com.example.showmyroom.activity.PostActivity;
import com.example.showmyroom.activity.WritePostActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MenuFragment extends Fragment {
    private static final String TAG = "MenuFragment";

    RecyclerView recyclerView;
    private MyRecyclerAdapter_Board myRecyclerAdapterBoard;
    private ArrayList<BoardItem> boardItems = new ArrayList<>();
    private View refreshButton, fadeInButton, fadeOutButton, searchButton, writeButton;
    private TextView boardTextView, noResultTextView;
    private EditText searchEditText;
    private LinearLayout boardMenuButton, searchLayout, progressBarLayout, noResultLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;

    private String containMessage = "";

    private int page = 1;
    private boolean isSearch = false;
    private int boardNum = 0;

    //키보드
    private InputMethodManager imm;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 0:
                    // 프래그먼트 첫 호출
                    recyclerView.setAdapter(myRecyclerAdapterBoard);
                    LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                    myRecyclerAdapterBoard.setmBoardList(boardItems, page);
                    recyclerView.setLayoutManager(mLayoutManager);
                    break;
                case 1:
                    // 갱신
                    myRecyclerAdapterBoard.setmBoardList(boardItems, page);
                    myRecyclerAdapterBoard.notifyDataSetChanged();
                    break;
                case 2:
                    // 다음 페이지 로딩
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            myRecyclerAdapterBoard.setmBoardList(boardItems, page);
                        }
                    }, 300);
                    break;
                case 3:
                    // 갱신
                    LinearLayoutManager mLayoutManager_Search = new LinearLayoutManager(getActivity());
                    myRecyclerAdapterBoard.setmBoardList(boardItems, page);
                    recyclerView.setLayoutManager(mLayoutManager_Search);
                    myRecyclerAdapterBoard.notifyDataSetChanged();
                    break;
            }
        }
    };


    public MenuFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_menu, container, false);
        Intent menuIntent = getActivity().getIntent();
        boardNum = menuIntent.getIntExtra("boardNum", 0);

        noResultLayout = v.findViewById(R.id.noResultLayout);
        noResultTextView = v.findViewById(R.id.noResultTextView);

        boardTextView = v.findViewById(R.id.boardTextView);
        switch (boardNum) {
            case 0:
                boardTextView.setText("자유게시판");
                break;
            case 1:
                boardTextView.setText("비밀게시판");
                break;
            case 2:
                boardTextView.setText("거래게시판");
                break;
            default:
                break;
        }
        boardMenuButton = v.findViewById(R.id.boardMenuButton);
        boardMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(getActivity(), v);
                getActivity().getMenuInflater().inflate(R.menu.post_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.freeBoard:
                                boardNum = 0;
                                page = 1;
                                boardTextView.setText("자유게시판");
                                break;
                            case R.id.secretBoard:
                                boardNum = 1;
                                boardTextView.setText("비밀게시판");
                                break;
                            default:
                                break;
                        }
                        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        progressBarLayout.setVisibility(View.VISIBLE);
                        searchLayout.setVisibility(View.GONE);
                        fadeInButton.setVisibility(View.VISIBLE);
                        fadeOutButton.setVisibility(View.GONE);
                        searchEditText.setText(null);
                        containMessage = "";
                        page = 1;
                        isSearch = false;
                        adaptData(containMessage, 0, page, boardNum);
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                progressBarLayout.setVisibility(View.GONE);
                            }
                        }, 500);
                        return false;
                    }
                });
                popupMenu.show();//Popup Menu 보이기
            }
        });

        searchLayout = v.findViewById(R.id.searchLayout);
        fadeInButton = v.findViewById(R.id.fadeInButton);
        searchEditText = v.findViewById(R.id.searchEditText);
        fadeInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchLayout.setVisibility(View.VISIBLE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                searchEditText.requestFocus();
                fadeInButton.setVisibility(View.GONE);
                fadeOutButton.setVisibility(View.VISIBLE);
            }
        });

        fadeOutButton = v.findViewById(R.id.fadeOutButton);
        fadeOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                searchLayout.setVisibility(View.GONE);
                fadeInButton.setVisibility(View.VISIBLE);
                fadeOutButton.setVisibility(View.GONE);
            }
        });

        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        progressBarLayout = v.findViewById(R.id.progressBarLayout);
        progressBar = v.findViewById(R.id.progressBar);
        searchButton = v.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                progressBarLayout.setVisibility(View.VISIBLE);
                fadeOutButton.setVisibility(View.GONE);
                page = 1;
                isSearch = true;
                containMessage = searchEditText.getText().toString();
                adaptData(containMessage, 3, page, boardNum);
                searchEditText.clearFocus();
                imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        progressBarLayout.setVisibility(View.GONE);
                    }
                }, 500);

            }
        });
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                progressBarLayout.setVisibility(View.VISIBLE);
                fadeOutButton.setVisibility(View.GONE);
                page = 1;
                isSearch = true;
                containMessage = searchEditText.getText().toString();
                adaptData(containMessage, 3, page, boardNum);
                searchEditText.clearFocus();
                imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        progressBarLayout.setVisibility(View.GONE);
                    }
                }, 500);

                return false;
            }
        });

        refreshButton = v.findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceManager.setInt(getActivity(), "BackToMain", 2);
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("boardNum", boardNum);
                startActivity(intent);
            }
        });

        swipeRefreshLayout = v.findViewById(R.id.swipeRefreshBoard);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                /* swipe 시 진행할 동작 */
                page = 1;
                adaptData(containMessage, 0, page, boardNum);

                /* 업데이트가 끝났음을 알림 */
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        myRecyclerAdapterBoard = new MyRecyclerAdapter_Board();

        adaptData("", 0, page, boardNum);

        // 게시물 선택
        myRecyclerAdapterBoard.setOnItemClickListener(new MyRecyclerAdapter_Board.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Intent postIntent = new Intent(getActivity(), PostActivity.class);
                postIntent.putExtra("thisPostId", boardItems.get(position).getId());
                postIntent.putExtra("boardNum", boardNum);
                startActivity(postIntent);

            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
//                LinearLayoutManager layoutManager = LinearLayoutManager.class.cast(recyclerView.getLayoutManager());
//                int totalItemCount = layoutManager.getItemCount()-1;
//                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
//
//                if(lastVisibleItemPosition == totalItemCount){
//                    Log.d(TAG, "@@@@@@@@@@@@끝 도달@@@@@@@@@@@@");
//                }
                if (!recyclerView.canScrollVertically(1)) {
                    Log.d(TAG, "@@@@@@@@@@@@끝 도달@@@@@@@@@@@@");
                    page++;
                    adaptData(containMessage, 2, page, boardNum);
                }
            }
        });

        myRecyclerAdapterBoard.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);


        // 게시물 등록 버튼
        writeButton = v.findViewById(R.id.writeFloatingButton);
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent writeIntent = new Intent(getActivity(), WritePostActivity.class);
                writeIntent.putExtra("boardNum", boardNum);
                startActivity(writeIntent);
            }
        });


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        adaptData(containMessage, 1, page, boardNum);

    }


    private void adaptData(String containMessage, int send_message, int page, int boardNum) {
        noResultLayout.setVisibility(View.GONE);
        String board = "";
        switch (boardNum) {
            case 0:
                board = "posts";
                break;
            case 1:
                board = "posts_secret";
                break;
            case 2:
                board = "posts_deal";
                break;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query first = db.collection(board).orderBy("date", Query.Direction.DESCENDING);
        first.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            boardItems = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.getData().get("title").toString().contains(containMessage)
                                        || document.getData().get("message").toString().contains(containMessage)) {
                                    boardItems.add(new BoardItem(
                                            document.getData().get("kakaoId").toString(),
                                            document.getData().get("userId").toString(),
                                            document.getData().get("title").toString(),
                                            document.getData().get("message").toString(),
                                            document.getData().get("date").toString(),
                                            document.getId(),
                                            (ArrayList<CommentItem>) document.getData().get("comment"),
                                            document.getData().get("commentNum").toString()
                                    ));
                                    if (boardItems.size() == page * 10) {
                                        boardItems.add(null);
                                        break;
                                    }
                                }
                            }
                            handler.sendEmptyMessage(send_message);

                            Log.d(TAG, "boardItems size : " + boardItems.size());
                            if(boardItems.size() == 0){
                                noResultLayout.setVisibility(View.VISIBLE);
                                if(isSearch){
                                    noResultTextView.setText("검색 결과가 없습니다.");
                                }else{
                                    noResultTextView.setText("등록된 게시물이 없습니다.");
                                }

                            }


                        } else {
                            Log.d(TAG, "Error getting documents", task.getException());
                        }
                    }

                });
    }
}

