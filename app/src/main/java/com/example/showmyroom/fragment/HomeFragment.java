package com.example.showmyroom.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.showmyroom.PreferenceManager;
import com.example.showmyroom.R;
import com.example.showmyroom.activity.FeedPostActivity;
import com.example.showmyroom.activity.NoticeActivity;
import com.example.showmyroom.activity.WriteHomeActivity;
import com.example.showmyroom.adapter.MyRecyclerAdapter_Board;
import com.example.showmyroom.adapter.MyRecyclerAdapter_Home;
import com.example.showmyroom.items.HomeItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.Arrays;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private View filterButton, writeButton;

    // no result
    private boolean isNoResult = false;

    // 끝도달
    private boolean is5Load = false;

    // swipe
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isSwipe = false;

    // progressBar
    private LinearLayout progressBarLayout;
    private ProgressBar progressBar;
    private TextView progressBarTextView;

    // homeDialog
    private Boolean isSelected = false;
    private String whatSelected;

    // notification
    private boolean isNotice = false;
    private ImageView noticeButton;

    // filter
    private ImageButton refreshButton;
    private LinearLayout filterLayout;
    private int showPost = 2;   // 0 : room, 1 : daily, 2 : both
    private boolean roomClicked = true, dailyClicked = true;
    private String py = "", dwell = "", style = "";
    private ArrayList<String> whatType_filtered = new ArrayList<>(), pySelected_filtered = new ArrayList<>(), dwellSelected_filtered = new ArrayList<>(), styleSelected_filtered = new ArrayList<>();
    private ArrayList<String> whatType = new ArrayList<>(Arrays.asList("room", "daily"));
    private ArrayList<String> pySelected = new ArrayList<>(Arrays.asList("null", "10평 미만", "10평대", "20평대", "30평대", "40평대 이상"));
    private ArrayList<String> dwellSelected = new ArrayList<>(Arrays.asList("null", "원룸 & 오피스텔", "아파트", "빌라 & 연립", "기타 주거형태"));
    private ArrayList<String> styleSelected = new ArrayList<>(Arrays.asList("null", "모던", "북유럽", "빈티지", "내추럴", "프로방스 & 로맨틱", "클래식 & 앤틱", "한국 & 아시아", "유니크", "기타 스타일"));
    private boolean isFilter = false;

    // 게시물 불러오기
    private RecyclerView recyclerView;
    private MyRecyclerAdapter_Home myRecyclerAdapterHome;
    private int page = 1;
    private String kakaoId;
    private ArrayList<String> followList = new ArrayList<>();
    private ArrayList<HomeItem> homeItems = new ArrayList<>(), complete_homeItems = new ArrayList<>();
    private int handlerMessage = 0;

    // 게시물 사진
    private boolean isDownload = true,
            isFirstLoading = true;   // 처음 로딩때만 true 나머지땐 false
    private ArrayList<Uri> postUriList;
    private ArrayList<ArrayList<Uri>> complete_postUriList = new ArrayList<>();
    private static final int LOAD_FOLLOWLIST = -3, SET_POSITION = -2, LOAD_URI = -1, ADAPT = 0, LOAD_NEXTPAGE = 1, RENEW = 2;
    private int pos = 0;
    private int innerPos, count = 0;
    private StorageReference postRef;

    // 앨범 이미지 선택
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef, pathRef;
    private ArrayList<Uri> uriList = new ArrayList<>(); // 선택한 이미지 리스트
    // 사진 요청코드
    private static final int FROM_GALLARY = 0;

    // crop
    private int cropPosition = 0;
    private ArrayList<Uri> cropList = new ArrayList<>();    // crop 후 이미지 리스트

    private DatabaseReference mDatabase;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

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
                        Toast.makeText(getActivity(), "불러올 게시물이 없습니다.", Toast.LENGTH_SHORT).show();
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
                    LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                    myRecyclerAdapterHome.setHomeList(complete_homeItems, page);
                    recyclerView.setLayoutManager(mLayoutManager);

                    swipeRefreshLayout.setRefreshing(false);
                    isSwipe = false;
                    isFirstLoading = false;
                    progressBar.setVisibility(View.GONE);
                    progressBarTextView.setVisibility(View.GONE);
                    filterLayout.setVisibility(View.VISIBLE);
                    writeButton.setVisibility(View.VISIBLE);
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

    public HomeFragment() {
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
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        Log.d(TAG, "Pref-kakaoId : " + PreferenceManager.getString(getActivity(), "kakaoId"));

        recyclerView = v.findViewById(R.id.homeRecyclerView);
        myRecyclerAdapterHome = new MyRecyclerAdapter_Home();
        progressBarLayout = v.findViewById(R.id.progressBarLayout);
        progressBar = v.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        progressBarTextView = v.findViewById(R.id.progressBarText);
        filterButton = v.findViewById(R.id.filterView);
        filterLayout = v.findViewById(R.id.filterLayout);
        filterLayout.setVisibility(View.GONE);
        refreshButton = v.findViewById(R.id.refreshButton);
        writeButton = v.findViewById(R.id.writeFloatingButton);
        writeButton.setVisibility(View.GONE);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                kakaoId = String.valueOf(user.getId());

                mDatabase.child("notification").child(kakaoId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if(task.isSuccessful()){
                            if(task.getResult().getValue() != null) isNotice = (boolean) task.getResult().getValue();
                            if(isNotice) noticeButton.setImageResource(R.drawable.notice_new);
                        }
                    }
                });

                complete_homeItems = new ArrayList<>();
                adaptData(ADAPT, page); // page = 1
                return null;
            }
        });



        // 알람 버튼
        noticeButton = v.findViewById(R.id.noticeButton);

        noticeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
                    @Override
                    public Unit invoke(User user, Throwable throwable) {
                        isNotice = false;
                        mDatabase.child("notification").child(kakaoId).setValue(isNotice);
                        noticeButton.setImageResource(R.drawable.notice);

                        Intent intent = new Intent(getActivity(), NoticeActivity.class);
                        intent.putExtra("myKakaoId", String.valueOf(user.getId()));
                        startActivity(intent);
                        return null;
                    }
                });
            }
        });

        // 게시물 선택
        myRecyclerAdapterHome.setOnItemClickListener(new MyRecyclerAdapter_Board.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Intent intent = new Intent(getActivity(), FeedPostActivity.class);
                intent.putExtra("postRef", "Post/" + complete_homeItems.get(position).getThisFeedKakaoId() + "/" + complete_homeItems.get(position).getDate() + "/");
                intent.putExtra("postId", complete_homeItems.get(position).getId());
                intent.putExtra("thisFeedKakaoId", complete_homeItems.get(position).getThisFeedKakaoId());
                startActivity(intent);
            }
        });

        // swipe
        swipeRefreshLayout = v.findViewById(R.id.swipeRefreshHome);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (is5Load || isFirstLoading) {
                    Log.d(TAG, "" + is5Load + " " + isFirstLoading);
                    Toast.makeText(getActivity(), "잠시 후에 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    /* swipe 시 진행할 동작 */
                    isSwipe = true;
                    complete_homeItems = new ArrayList<>();
                    complete_postUriList = new ArrayList<>();
                    pos = 0;    // index -> 0번부터
                    page = 1;   // page -> 첫페이지로
                    isDownload = true;  // 다운로드 가능하게
                    adaptData(ADAPT, page);
                }

            }
        });

        // 끝도달
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!recyclerView.canScrollVertically(1)) {
                    if (!isSwipe && !is5Load) {
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

        myRecyclerAdapterHome.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);


        storageRef = storage.getReference();

        // 새로고침 버튼
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomClicked = true;
                dailyClicked = true;
                whatType = new ArrayList<>(Arrays.asList("room", "daily"));
                showPost = 2;

                pySelected = new ArrayList<>(Arrays.asList("null", "10평 미만","10평대", "20평대", "30평대", "40평대 이상"));
                dwellSelected = new ArrayList<>(Arrays.asList("null", "원룸 & 오피스텔", "아파트", "빌라 & 연립", "기타 주거형태"));
                styleSelected = new ArrayList<>(Arrays.asList("null", "모던", "북유럽", "빈티지", "내추럴", "프로방스 & 로맨틱", "클래식 & 앤틱", "한국 & 아시아", "유니크", "기타 스타일"));

                progressBar.setVisibility(View.VISIBLE);
                isFilter = false;
                isSwipe = true;
                complete_homeItems = new ArrayList<>();
                complete_postUriList = new ArrayList<>();
                pos = 0;    // index -> 0번부터
                page = 1;   // page -> 첫페이지로
                isDownload = true;  // 다운로드 가능하게
                adaptData(ADAPT, page);
            }
        });


        // 필터 버튼
        if(isNoResult) filterLayout.setVisibility(View.GONE);

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog filterDialog = new Dialog(getActivity());
                filterDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                filterDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                filterDialog.setContentView(R.layout.dialog_filter);
                // 다이얼로그 크기 원하는대로
                WindowManager.LayoutParams params = filterDialog.getWindow().getAttributes();
                params.width = 1000;
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                filterDialog.getWindow().setAttributes(params);
                filterDialog.show();


                pySelected = new ArrayList<>();
                dwellSelected = new ArrayList<>();
                styleSelected = new ArrayList<>();

                py = ""; dwell = ""; style = "";

                LinearLayout roomLayout = filterDialog.findViewById(R.id.roomLayout);
                TextView roomTextView = filterDialog.findViewById(R.id.roomTextView);
                LinearLayout dailyLayout = filterDialog.findViewById(R.id.dailyLayout);
                TextView dailyTextView = filterDialog.findViewById(R.id.dailyTextView);
                LinearLayout openLayout = filterDialog.findViewById(R.id.openLayout);
                Button pyButton = filterDialog.findViewById(R.id.pyButton);
                Button dwellButton = filterDialog.findViewById(R.id.dwellButton);
                Button styleButton = filterDialog.findViewById(R.id.styleButton);
                TextView openTextView = filterDialog.findViewById(R.id.openTextView);
                ChipGroup chipGroup_py = filterDialog.findViewById(R.id.chipGroup_py);
                ChipGroup chipGroup_dwell = filterDialog.findViewById(R.id.chipGroup_dwell);
                ChipGroup chipGroup_style = filterDialog.findViewById(R.id.chipGroup_style);
                Button filterButton = filterDialog.findViewById(R.id.filterButton);
                ImageButton refreshButton = filterDialog.findViewById(R.id.refreshButton);

                // 초기화
                if(isFilter){
                    // 이전에 넣어놓은 필터링된 리스트 복사
                    pySelected = pySelected_filtered;
                    dwellSelected = dwellSelected_filtered;
                    styleSelected = styleSelected_filtered;

                    Log.d(TAG, "pySelected : "+pySelected);
                    Log.d(TAG, "dwellSelectd : "+dwellSelected);
                    Log.d(TAG, "styleSelected : "+styleSelected);

                    try {
                        LayoutInflater inflater = LayoutInflater.from(getActivity());

                        // 평수 초기화
                        for (int i = 0; i < pySelected.size(); i++){
                            Chip newChip = (Chip) inflater.inflate(R.layout.view_chip2, chipGroup_py, false);
                            newChip.setText(pySelected.get(i));
                            chipGroup_py.addView(newChip);
                            newChip.setOnCloseIconClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    handleChipCloseIconClicked(pyButton, pySelected, (Chip) v);
                                }
                            });
                        }

                        // 주거형태 초기화
                        for (int i = 0; i < dwellSelected.size(); i++){
                            Chip newChip = (Chip) inflater.inflate(R.layout.view_chip2, chipGroup_dwell, false);
                            newChip.setText(dwellSelected.get(i));
                            chipGroup_dwell.addView(newChip);
                            newChip.setOnCloseIconClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    handleChipCloseIconClicked(dwellButton, dwellSelected, (Chip) v);
                                }
                            });
                        }

                        // 스타일 초기화
                        for (int i = 0; i < styleSelected.size(); i++){
                            Chip newChip = (Chip) inflater.inflate(R.layout.view_chip2, chipGroup_style, false);
                            newChip.setText(styleSelected.get(i));
                            chipGroup_style.addView(newChip);
                            newChip.setOnCloseIconClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    handleChipCloseIconClicked(styleButton, styleSelected, (Chip) v);
                                }
                            });
                        }



                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }




                // 게시물 종류 - 현재 상태
                switch (showPost) {
                    case 0:
                        roomLayout.setBackgroundResource(R.drawable.square_selected);
                        roomTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.light_coral));
                        openTextView.setEnabled(true);
                        for (int i = 0; i < openLayout.getChildCount(); i++) {
                            View child = openLayout.getChildAt(i);
                            child.setEnabled(true);
                        }
                        roomClicked = true;
                        dailyClicked = false;
                        break;
                    case 1:
                        dailyLayout.setBackgroundResource(R.drawable.square_selected);
                        dailyTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.light_coral));
                        openTextView.setEnabled(false);
                        for (int i = 0; i < openLayout.getChildCount(); i++) {
                            View child = openLayout.getChildAt(i);
                            child.setEnabled(false);
                        }
                        roomClicked = false;
                        dailyClicked = true;
                        break;
                    case 2:
                        roomLayout.setBackgroundResource(R.drawable.square_selected);
                        roomTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.light_coral));
                        dailyLayout.setBackgroundResource(R.drawable.square_selected);
                        dailyTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.light_coral));
                        openTextView.setEnabled(true);
                        for (int i = 0; i < openLayout.getChildCount(); i++) {
                            View child = openLayout.getChildAt(i);
                            child.setEnabled(true);
                        }
                        roomClicked = true;
                        dailyClicked = true;
                        break;
                }
                // 게시물 종류 - 내방볼래 선택시
                roomLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (roomClicked) {
                            roomLayout.setBackgroundResource(R.drawable.square);
                            roomTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_bold_color));
                            openTextView.setEnabled(false);
                            for (int i = 0; i < openLayout.getChildCount(); i++) {
                                View child = openLayout.getChildAt(i);
                                child.setEnabled(false);
                            }
                            roomClicked = false;
                        } else {
                            roomLayout.setBackgroundResource(R.drawable.square_selected);
                            roomTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.light_coral));
                            openTextView.setEnabled(true);
                            for (int i = 0; i < openLayout.getChildCount(); i++) {
                                View child = openLayout.getChildAt(i);
                                child.setEnabled(true);
                            }
                            roomClicked = true;
                        }
                    }
                });
                // 게시물 종류 - 내일상볼래 선택시
                dailyLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (dailyClicked) {
                            dailyLayout.setBackgroundResource(R.drawable.square);
                            dailyTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_bold_color));
                            dailyClicked = false;
                        } else {
                            dailyLayout.setBackgroundResource(R.drawable.square_selected);
                            dailyTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.light_coral));
                            dailyClicked = true;
                        }
                    }
                });

                // 평수
                pyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final PopupMenu popup_py = new PopupMenu(getActivity(), v);
                        getActivity().getMenuInflater().inflate(R.menu.popup_py, popup_py.getMenu());
                        popup_py.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                py = item.getTitle().toString();
                                pyButton.setBackgroundResource(R.drawable.square_selected);
                                if (!pySelected.contains(py)) {
                                    pySelected.add(py);
                                    try {
                                        LayoutInflater inflater = LayoutInflater.from(getActivity());
                                        Chip newChip = (Chip) inflater.inflate(R.layout.view_chip2, chipGroup_py, false);
                                        newChip.setText(py);
                                        chipGroup_py.addView(newChip);
                                        newChip.setOnCloseIconClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                handleChipCloseIconClicked(pyButton, pySelected, (Chip) v);
                                            }
                                        });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Toast.makeText(getActivity(), "하나의 키워드는 한 번만 선택 가능합니다.", Toast.LENGTH_SHORT).show();
                                }
                                return false;
                            }
                        });
                        popup_py.show();

                    }
                });
                // 주거형태
                dwellButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final PopupMenu popup_dwell = new PopupMenu(getActivity(), v);
                        getActivity().getMenuInflater().inflate(R.menu.popup_dwell, popup_dwell.getMenu());
                        popup_dwell.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                dwell = item.getTitle().toString();
                                dwellButton.setBackgroundResource(R.drawable.square_selected);
                                if (!dwellSelected.contains(dwell)) {
                                    dwellSelected.add(dwell);
                                    try {
                                        LayoutInflater inflater = LayoutInflater.from(getActivity());
                                        Chip newChip = (Chip) inflater.inflate(R.layout.view_chip2, chipGroup_dwell, false);
                                        newChip.setText(dwell);
                                        chipGroup_dwell.addView(newChip);
                                        newChip.setOnCloseIconClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                handleChipCloseIconClicked(dwellButton, dwellSelected, (Chip) v);
                                            }
                                        });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Toast.makeText(getActivity(), "하나의 키워드는 한 번만 선택 가능합니다.", Toast.LENGTH_SHORT).show();
                                }
                                return false;
                            }
                        });
                        popup_dwell.show();

                    }
                });
                // 스타일
                styleButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final PopupMenu popup_style = new PopupMenu(getActivity(), v);
                        getActivity().getMenuInflater().inflate(R.menu.popup_style, popup_style.getMenu());
                        popup_style.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                style = item.getTitle().toString();
                                styleButton.setBackgroundResource(R.drawable.square_selected);
                                if (!styleSelected.contains(style)) {
                                    styleSelected.add(style);
                                    try {
                                        LayoutInflater inflater = LayoutInflater.from(getActivity());
                                        Chip newChip = (Chip) inflater.inflate(R.layout.view_chip2, chipGroup_style, false);
                                        newChip.setText(style);
                                        chipGroup_style.addView(newChip);
                                        newChip.setOnCloseIconClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                handleChipCloseIconClicked(styleButton, styleSelected, (Chip) v);
                                            }
                                        });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Toast.makeText(getActivity(), "하나의 키워드는 한 번만 선택 가능합니다.", Toast.LENGTH_SHORT).show();
                                }
                                return false;
                            }
                        });
                        popup_style.show();

                    }
                });

                // 확인 버튼
                filterButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 초기화
                        whatType = new ArrayList<>();
                        if (roomClicked) whatType.add("room");
                        if (dailyClicked) whatType.add("daily");
                        switch (whatType.size()) {
                            case 0:
                                Toast.makeText(getActivity(), "게시물 종류를 선택해주세요", Toast.LENGTH_SHORT).show();
                                return;
                            case 1:
                                if (roomClicked) showPost = 0;
                                else showPost = 1;
                                break;
                            case 2:
                                showPost = 2;
                        }

                        if(pySelected.size() == 0) {
                            pySelected = new ArrayList<>(Arrays.asList("null", "10평 미만","10평대", "20평대", "30평대", "40평대 이상"));
                            pySelected_filtered = new ArrayList<>();
                        }else pySelected_filtered = pySelected;

                        if (dwellSelected.size() == 0){
                            dwellSelected = new ArrayList<>(Arrays.asList("null", "원룸 & 오피스텔", "아파트", "빌라 & 연립", "기타 주거형태"));
                            dwellSelected_filtered = new ArrayList<>();
                        }else dwellSelected_filtered = dwellSelected;

                        if (styleSelected.size() == 0){
                            styleSelected = new ArrayList<>(Arrays.asList("null", "모던", "북유럽", "빈티지", "내추럴", "프로방스 & 로맨틱", "클래식 & 앤틱", "한국 & 아시아", "유니크", "기타 스타일"));
                            styleSelected_filtered = new ArrayList<>();
                        }else styleSelected_filtered = styleSelected;


                        Log.d(TAG, "whatType : "+whatType);
                        Log.d(TAG, "pySelected : "+pySelected);
                        Log.d(TAG, "dwellSelectd : "+dwellSelected);
                        Log.d(TAG, "styleSelected : "+styleSelected);

                        progressBar.setVisibility(View.VISIBLE);
                        isFilter = true;
                        isSwipe = true;
                        isNoResult = false;
                        complete_homeItems = new ArrayList<>();
                        complete_postUriList = new ArrayList<>();
                        pos = 0;    // index -> 0번부터
                        page = 1;   // page -> 첫페이지로
                        isDownload = true;  // 다운로드 가능하게
                        adaptData(ADAPT, page);
                        filterDialog.dismiss();
                    }
                });


            }
        });

        // 피드 게시물 작성
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // homeDialog
                Dialog homeDialog = new Dialog(getActivity());
                homeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                homeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                homeDialog.setContentView(R.layout.dialog_home);
                homeDialog.show();

                Button checkButton = homeDialog.findViewById(R.id.checkButton);
                LinearLayout roomLayout = homeDialog.findViewById(R.id.roomLayout);
                LinearLayout dailyLayout = homeDialog.findViewById(R.id.dailyLayout);
                TextView roomTextView = homeDialog.findViewById(R.id.roomTextView);
                TextView dailyTextView = homeDialog.findViewById(R.id.dailyTextView);

                // 내 방 볼래? 선택
                roomLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        roomLayout.setBackgroundResource(R.drawable.square_selected);
                        roomTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.light_coral));
                        dailyLayout.setBackgroundResource(R.drawable.square);
                        dailyTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_bold_color));
                        isSelected = true;
                        whatSelected = "room";
                    }
                });
                // 내 일상 볼래? 선택
                dailyLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        roomLayout.setBackgroundResource(R.drawable.square);
                        roomTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_bold_color));
                        dailyLayout.setBackgroundResource(R.drawable.square_selected);
                        dailyTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.light_coral));
                        isSelected = true;
                        whatSelected = "daily";
                    }
                });

                // 확인 버튼
                checkButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isSelected) {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                            intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivityForResult(intent, FROM_GALLARY);
                        }
                    }
                });


            }
        });

        return v;
    }

    private void handleChipCloseIconClicked(Button whatButton, ArrayList<String> whatSelected, Chip chip) {
        ChipGroup parent = (ChipGroup) chip.getParent();
        parent.removeView(chip);

        Log.d(TAG, "remove - "+whatSelected+", "+chip.getText());
        whatSelected.remove(chip.getText());

        if(whatSelected.size() == 0) whatButton.setBackgroundResource(R.drawable.square);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == FROM_GALLARY) {
            cropPosition = 0;
            cropList = new ArrayList<>();
            uriList = new ArrayList<>();
            if (data == null) {
//                Toast.makeText(getActivity(), "이미지를 선택하지 않았습니다.", Toast.LENGTH_SHORT).show();
            } else {
                if (data.getClipData() == null) {
                    Log.e(TAG, "single choice: " + data.getData());
                    Uri imageUri = data.getData();
                    uriList.add(imageUri);
                    startCropActivity(uriList, cropPosition);
                } else {
                    ClipData clipData = data.getClipData();
                    Log.e("clipData", String.valueOf(clipData.getItemCount()));

                    if (clipData.getItemCount() > 5) {
                        Toast.makeText(getActivity(), "사진은 5장까지 선택 가능합니다.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivityForResult(intent, FROM_GALLARY);
                    } else {
                        Log.e(TAG, "multiple choice");
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            Uri imageUri = clipData.getItemAt(i).getUri();
                            uriList.add(imageUri);
                        }
                        startCropActivity(uriList, cropPosition);
                    }
                }


            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            Log.d(TAG, "come back!");
            try {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                handleCropResult(result.getUri());
            } catch (Exception e) {

            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "resume");
        Log.d(TAG, "isFirstLoading - "+isFirstLoading+", isNoResult - "+isNoResult );
        if (!isFirstLoading && !isNoResult) {
            Log.d(TAG, followList.size() + "");
            Query first = db.collection("homePosts").orderBy("date", Query.Direction.DESCENDING);
            first.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                int count = 0;
                                Log.d(TAG, "renew - followList : " + followList);
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, "renew - is followList contain : " + followList.contains(document.getData().get("kakaoId").toString()));
                                    if (followList.contains(document.getData().get("kakaoId").toString()) &&
                                            whatType.contains(document.getData().get("whatSelected").toString())&&
                                            pySelected.contains(document.getData().get("py").toString()) &&
                                            dwellSelected.contains(document.getData().get("dwell").toString()) &&
                                            styleSelected.contains(document.getData().get("style").toString()))
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
                                        Log.d(TAG, "renew - i : " + count + ", date : " + homeItems.get(count).getDate() + ", likeNum : " + document.getData().get("likeNum"));
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
        followList = new ArrayList<>();
        followList.add(kakaoId);
        mDatabase.child("following").child(kakaoId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    followList.add(String.valueOf(dataSnapshot.getValue()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        Query first = db.collection("homePosts").orderBy("date", Query.Direction.DESCENDING);
        first.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            homeItems = new ArrayList<>();
                            Log.d(TAG, homeItems.size() + " & followList : " + followList);
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (followList.contains(document.getData().get("kakaoId").toString())&&
                                        whatType.contains(document.getData().get("whatSelected").toString())&&
                                        pySelected.contains(document.getData().get("py").toString()) &&
                                        dwellSelected.contains(document.getData().get("dwell").toString()) &&
                                        styleSelected.contains(document.getData().get("style").toString()))
                                {
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

                            if (homeItems.size() == 0) {
                                Log.d(TAG, "no result");
                                progressBar.setVisibility(View.GONE);
                                progressBarTextView.setVisibility(View.VISIBLE);
                                progressBarTextView.setText("게시물이 없습니다.");
//                                if (followList.size() == 1) {
//                                    Log.d(TAG, "no following");
//                                    progressBarTextView.setText("팔로우하는 회원이 없습니다.");
//
//                                }
                                if(isFilter){
                                    progressBarTextView.setText("필터링한 게시물이 없습니다.");
                                }
                                if (is5Load) {
                                    is5Load = false;
                                    pos--;
                                    page--;
                                }
                                isFirstLoading = false;
                                isSwipe = false;
                                isNoResult = true;
                                swipeRefreshLayout.setRefreshing(false);
                                handler.sendEmptyMessage(RENEW);
                                filterLayout.setVisibility(View.VISIBLE);
                                writeButton.setVisibility(View.VISIBLE);
                                return;
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
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });


    }

    private void startCropActivity(@NonNull ArrayList<Uri> uriList, int position) {
        CropImage.activity(uriList.get(position)).setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .start(getContext(), HomeFragment.this);
    }

    private void handleCropResult(@NonNull Uri uri) {
        if (uri != null) {
            try {
                cropList.add(uri);
                Log.d(TAG, "cropList size : " + cropList.size());
                cropPosition++;
                if (cropList.size() == uriList.size()) {
                    Intent intent = new Intent(getActivity(), WriteHomeActivity.class);
                    intent.putExtra("uriList", cropList);
                    intent.putExtra("whatSelected", whatSelected);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    startCropActivity(uriList, cropPosition);
                }
            } catch (Exception e) {
                Log.e(TAG, "File select error", e);
            }
        }
    }

}