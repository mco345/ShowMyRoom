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
import com.example.showmyroom.activity.WriteHomeActivity;
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

    private SwipeRefreshLayout swipeRefreshLayout;

    // 게시물 불러오기
    private RecyclerView recyclerView;
    private MyRecyclerAdapter_Home myRecyclerAdapterHome;
    private int page = 1;
    private String kakaoId;
    private ArrayList<String> followList = new ArrayList<>();
    private ArrayList<HomeItem> homeItems = new ArrayList<>(), complete_homeItems = new ArrayList<>();
    private int handlerMessage = 0;

    // 게시물 사진
    private ArrayList<Uri> postUriList;
    private static final int SET_POSITION = -2, LOAD_URI = -1, ADAPT = 0, LOAD_NEXTPAGE = 1, RENEW = 2;
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

    Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case SET_POSITION:
                    pos++;
                    count = 0;
                    if(pos == homeItems.size()){
                        Log.d(TAG, "FINISH");
                        pos = 0;
                        sendEmptyMessage(handlerMessage);
                    }else{
                        Log.d(TAG, "LOAD URI");
                        sendEmptyMessage(LOAD_URI);
                    }
                    break;
                case LOAD_URI:
                    postUriList = new ArrayList<>();
                    if (homeItems.size() != 0){
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
                                                        Log.d(TAG, String.valueOf(listResult.getItems().size()) + "  " + postRef.getPath());
                                                        innerPos = Integer.parseInt(item.toString().substring(item.toString().length() - 5, item.toString().length() - 4));
                                                        Log.d(TAG, "innerPos : " + innerPos + ", size : " + postUriList.size());
                                                        postUriList.set(innerPos, task.getResult());
                                                        count++;
                                                        if(count == listResult.getItems().size()){
                                                            complete_homeItems.add(new HomeItem(
                                                                    homeItems.get(pos).getThisFeedKakaoId(),
                                                                    homeItems.get(pos).getUserId(),
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
                    }else{
                        sendEmptyMessage(ADAPT);
                    }
                    break;
                case ADAPT:
                    // 프래그먼트 첫 호출
                    Log.d(TAG, "complete homeItems size : "+complete_homeItems.size());
                    recyclerView.setAdapter(myRecyclerAdapterHome);
                    LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                    myRecyclerAdapterHome.setHomeList(complete_homeItems, page);
                    recyclerView.setLayoutManager(mLayoutManager);
                    /* 업데이트가 끝났음을 알림 */
                    swipeRefreshLayout.setRefreshing(false);
                    break;
                case LOAD_NEXTPAGE:
                    // 다음 페이지 로딩
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            myRecyclerAdapterHome.setHomeList(complete_homeItems, page);
                        }
                    }, 300);
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

                adaptData(ADAPT, page);
                return null;
            }
        });

        swipeRefreshLayout = v.findViewById(R.id.swipeRefreshHome);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                /* swipe 시 진행할 동작 */
                page = 1;
                adaptData(ADAPT, page);
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
                    adaptData(LOAD_NEXTPAGE, page);
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

//    @Override
//    public void onResume() {
//        super.onResume();
//
//        adaptData(1, page);
//
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK && requestCode == FROM_GALLARY){
            position = 0;
            cropList = new ArrayList<>();
            uriList = new ArrayList<>();
            if (data == null) {
//                Toast.makeText(getActivity(), "이미지를 선택하지 않았습니다.", Toast.LENGTH_SHORT).show();
            } else {
                if (data.getClipData() == null) {
                    Log.e(TAG, "single choice: "+String.valueOf(data.getData()));
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
                        for(int i = 0; i < clipData.getItemCount(); i++){
                            Uri imageUri = clipData.getItemAt(i).getUri();
                            uriList.add(imageUri);
                        }
                        startCropActivity(uriList, position);
                    }
                }




            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            Log.d(TAG, "come back!");
            try{
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                handleCropResult(result.getUri());
            }catch (Exception e){

            }
        }

    }

    private void adaptData(int send_message, int page){
        followList = new ArrayList<>();
        mDatabase.child("following").child(kakaoId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    followList.add(String.valueOf(dataSnapshot.getValue()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query first = db.collection("homePosts").orderBy("date", Query.Direction.DESCENDING);
        first.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            homeItems = new ArrayList<>();
                            Log.d(TAG, homeItems.size() + " & followList : "+followList);
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (followList.contains(document.getData().get("kakaoId").toString())) {
                                    homeItems.add(new HomeItem(
                                            document.getData().get("kakaoId").toString(),
                                            document.getData().get("id").toString(),
                                            document.getData().get("date").toString(),
                                            document.getData().get("likeNum").toString(),
                                            document.getData().get("commentNum").toString(),
                                            document.getId(),
                                            (ArrayList<String>) document.getData().get("likeList")
                                    ));
                                }
                                if (homeItems.size() == page * 10) {
                                    homeItems.add(null);
                                    break;
                                }
                            }

                            if(homeItems.size() == 0){
                                Log.d(TAG, "no result");
                            }
                            if(followList.size() == 0){
                                Log.d(TAG, "no following");
                            }

                            complete_homeItems = new ArrayList<>();
                            handlerMessage = send_message;
                            handler.sendEmptyMessage(LOAD_URI);
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
                .setAspectRatio(1,1)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .start(getContext(), HomeFragment.this);
    }

    private void handleCropResult(@NonNull Uri uri) {
        if (uri != null) {
            try{
                cropList.add(uri);
                Log.d(TAG, "cropList size : " + cropList.size());
                position++;
                if(cropList.size() == uriList.size()){
                    Intent intent = new Intent(getActivity(), WriteHomeActivity.class);
                    intent.putExtra("uriList", cropList);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }else{
                    startCropActivity(uriList, position);
                }
            }catch (Exception e){
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