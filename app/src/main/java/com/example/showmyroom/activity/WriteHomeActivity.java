package com.example.showmyroom.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.showmyroom.PreferenceManager;
import com.example.showmyroom.R;
import com.example.showmyroom.adapter.MyPagerAdapter_Post;
import com.example.showmyroom.items.BoardItem;
import com.example.showmyroom.items.CommentItem;
import com.example.showmyroom.items.PostItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class WriteHomeActivity extends AppCompatActivity {
    private static final String TAG = "WriteHomeActivity";

    // storage
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef, pathRef;
    private ArrayList<Uri> uriList = new ArrayList<>();

    // firestore
    private int forNum = 1;
    private String kakaoId, id;
    private int boardNum;
    private Date date;
    private DatabaseReference mDatabase;

    // ui
    private EditText contentEditText;
    private View uploadPostButton;


    // 이미지 뷰페이저
    private ViewPager pager;
    private MyPagerAdapter_Post pagerAdapter;

    // ProgressBar
    ProgressDialog dialog;

    // 뒤로가기
    private long backKeyPressedTime = 0;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_home);

        uriList = (ArrayList<Uri>) getIntent().getSerializableExtra("uriList");


        mDatabase = FirebaseDatabase.getInstance().getReference();
        storageRef = storage.getReference();

        pager = findViewById(R.id.postImageViewPager);
        pagerAdapter = new MyPagerAdapter_Post(this);
        pagerAdapter.setPostImage(uriList);
        pager.setAdapter(pagerAdapter);

        contentEditText = findViewById(R.id.contentEditText);
        uploadPostButton = findViewById(R.id.uploadPostButton);
        uploadPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog("게시물을 등록하는 중입니다.");
                String content = contentEditText.getText().toString();
                if(content.length() > 0){
                    long now = System.currentTimeMillis();
                    date = new Date(now);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String postDate = sdf.format(date);

                    UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
                        @Override
                        public Unit invoke(User user, Throwable throwable) {
                            kakaoId = String.valueOf(user.getId());
                            mDatabase.child("users").child(kakaoId).child("id").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    id = String.valueOf(task.getResult().getValue());
                                    String pathName = "Post/" + kakaoId + "/" + postDate+ "/";

                                    for(int i = 0; i<uriList.size(); i++){
                                        pathRef = storageRef.child(pathName + "/"+i+".png");
                                        UploadTask uploadTask = pathRef.putFile(uriList.get(i));
                                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                if(forNum == uriList.size()){
                                                    uploader(kakaoId, id, content, postDate);
                                                }else{
                                                    forNum++;
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                            }
                                        });
                                    }

                                }
                            });

                            return null;
                        }
                    });

                }





            }
        });


//        StorageReference listRef = FirebaseStorage.getInstance().getReference().child("Post/" + uploadTime);
//        Log.d(TAG, String.valueOf(listRef.listAll()));
//
//        listRef.listAll()
//                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
//                    @Override
//                    public void onSuccess(ListResult listResult) {
//                        int i = 0;
//                        Log.d(TAG, ""+listResult.getItems());
//                        // 폴더 내의 item이 동날 때까지 모두 가져온다.
//                        for (StorageReference item : listResult.getItems()) {
//                            Log.d(TAG, ""+2);
//                            // reference의 item(이미지) url 받아오기
//                            item.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Uri> task) {
//                                    if (task.isSuccessful()) {
//                                        uriArrayList.add(task.getResult());
//                                    } else {
//
//                                    }
//                                }
//
//
//                            }).addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    // Uh-oh, an error occurred!
//                                    Log.d(TAG, e.toString());
//                                }
//                            });
//                            Log.d(TAG, String.valueOf(uriArrayList.get(i)));
//                            i++;
//
//                        }
//
//
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Log.d(TAG, e.toString());
//            }
//        });
    }

    private void uploader(String kakaoId, String id, String content, String postDate) {
        PostItem postItem = new PostItem(
                kakaoId,
                id,
                content,
                postDate,
                new ArrayList<CommentItem>(),
                "0",
                "0"
                );
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("homePosts").add(postItem)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getApplicationContext(), "게시글을 등록하였습니다.", Toast.LENGTH_SHORT).show();
                        PreferenceManager.setInt(getApplicationContext(), "BackToMain", 0);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        dialog.dismiss();
                        startActivity(intent);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {

        // 기존 뒤로가기 버튼의 기능을 막기위해 주석처리 또는 삭제
        // super.onBackPressed();

        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지났으면 Toast Show
        // 2000 milliseconds = 2 seconds
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지나지 않았으면 종료
        // 현재 표시된 Toast 취소
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            PreferenceManager.setInt(getApplicationContext(), "BackToMain", 0);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    // 프로그래스바 다이얼로그
    private void showProgressDialog(String message) {
//        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        dialog = new ProgressDialog(WriteHomeActivity.this);
        dialog.setCancelable(false);    // 화면 밖 터치해도 dialog 취소되지 않게
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(message);
        dialog.show();
    }


}