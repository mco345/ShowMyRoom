package com.example.showmyroom.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.showmyroom.R;
import com.example.showmyroom.activity.FeedPostActivity;
import com.example.showmyroom.activity.PostActivity;
import com.example.showmyroom.activity.WriteHomeActivity;
import com.example.showmyroom.adapter.MyRecyclerAdapter_Board;
import com.example.showmyroom.adapter.MyRecyclerAdapter_Home;
import com.example.showmyroom.items.HomeItem;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private View writeButton;

    // 끝도달
    private boolean is5Load = false;

    // swipe
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isSwipe = false;

    // 게시물 불러오기
    private RecyclerView recyclerView;
    private MyRecyclerAdapter_Home myRecyclerAdapterHome;
    private int page = 1;
    private String kakaoId;
    private ArrayList<String> followList = new ArrayList<>();
    private ArrayList<HomeItem> homeItems = new ArrayList<>(), complete_homeItems = new ArrayList<>();
    private int handlerMessage = 0;

    // 게시물 사진
    private boolean isDownload = true, isFirstLoading;
    private ArrayList<Uri> postUriList;
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
    private int position = 0;
    private ArrayList<Uri> cropList = new ArrayList<>();    // crop 후 이미지 리스트

    private DatabaseReference mDatabase;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case LOAD_FOLLOWLIST:
                    followList = new ArrayList<>();
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
                    break;
                case SET_POSITION:
                    count = 0;
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
                                                                        homeItems.get(pos).getThisFeedKakaoId(),
                                                                        homeItems.get(pos).getUserId(),
                                                                        homeItems.get(pos).getMessage(),
                                                                        homeItems.get(pos).getDate(),
                                                                        homeItems.get(pos).getLikeNum(),
                                                                        homeItems.get(pos).getCommentNum(),
                                                                        homeItems.get(pos).getId(),
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

        recyclerView = v.findViewById(R.id.homeRecyclerView);
        myRecyclerAdapterHome = new MyRecyclerAdapter_Home();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                kakaoId = String.valueOf(user.getId());
                complete_homeItems = new ArrayList<>();
                isFirstLoading = true;  // 처음 로딩때만 true 나머지땐 false
                adaptData(ADAPT, page); // page = 1
                return null;
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

        swipeRefreshLayout = v.findViewById(R.id.swipeRefreshHome);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (is5Load || isFirstLoading) {
                    Toast.makeText(getActivity(), "잠시 후에 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    /* swipe 시 진행할 동작 */
                    isSwipe = true;
                    complete_homeItems = new ArrayList<>();
                    pos = 0;    // index -> 0번부터
                    page = 1;   // page -> 첫페이지로
                    isDownload = true;  // 다운로드 가능하게
                    adaptData(ADAPT, page);
                }

            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!recyclerView.canScrollVertically(1)) {
                    if (!isSwipe && !is5Load) {
                        // 게시물이 5개단위로 로드되어있을 때(끝에 프로그래스바 존재) -> 더 로딩(로드할 게 없으면 뒤에서 처리함)
                        if (homeItems.size() % 5 == 0) {
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

        // 피드 게시물 작성
        writeButton = v.findViewById(R.id.writeFloatingButton);
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, FROM_GALLARY);
            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == FROM_GALLARY) {
            position = 0;
            cropList = new ArrayList<>();
            uriList = new ArrayList<>();
            if (data == null) {
//                Toast.makeText(getActivity(), "이미지를 선택하지 않았습니다.", Toast.LENGTH_SHORT).show();
            } else {
                if (data.getClipData() == null) {
                    Log.e(TAG, "single choice: " + data.getData());
                    Uri imageUri = data.getData();
                    uriList.add(imageUri);
                    startCropActivity(uriList, position);
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
                        startCropActivity(uriList, position);
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

        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                followList = new ArrayList<>();
                mDatabase.child("following").child(String.valueOf(user.getId())).addListenerForSingleValueEvent(new ValueEventListener() {
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
                return null;
            }
        });
    }

    private void adaptData(int send_message, int pg) {
        handler.sendEmptyMessage(LOAD_FOLLOWLIST);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query first = db.collection("homePosts").orderBy("date", Query.Direction.DESCENDING);
        first.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            homeItems = new ArrayList<>();
                            Log.d(TAG, homeItems.size() + " & followList : " + followList);
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (followList.contains(document.getData().get("kakaoId").toString())) {
                                    homeItems.add(new HomeItem(
                                            document.getData().get("kakaoId").toString(),
                                            document.getData().get("id").toString(),
                                            document.getData().get("message").toString(),
                                            document.getData().get("date").toString(),
                                            document.getData().get("likeNum").toString(),
                                            document.getData().get("commentNum").toString(),
                                            document.getId(),
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
                            }
                            if (followList.size() == 0) {
                                Log.d(TAG, "no following");
                                if (is5Load) {
                                    is5Load = false;
                                    pos--;
                                    page--;
                                }
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
                position++;
                if (cropList.size() == uriList.size()) {
                    Intent intent = new Intent(getActivity(), WriteHomeActivity.class);
                    intent.putExtra("uriList", cropList);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    startCropActivity(uriList, position);
                }
            } catch (Exception e) {
                Log.e(TAG, "File select error", e);
            }
        }
    }


    // 프로그래스바 다이얼로그
    private void showProgressDialog(String message) {
//        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setCancelable(false);    // 화면 밖 터치해도 dialog 취소되지 않게
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(message);
        dialog.show();
    }
}