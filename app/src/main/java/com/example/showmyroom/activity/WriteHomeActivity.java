package com.example.showmyroom.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.android.gms.common.util.IOUtils;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import me.relex.circleindicator.CircleIndicator;

public class WriteHomeActivity extends AppCompatActivity {
    private static final String TAG = "WriteHomeActivity";

    // storage
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef, pathRef, thumbNailPathRef;
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
    private CircleIndicator circleIndicator;

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
        pagerAdapter = new MyPagerAdapter_Post(WriteHomeActivity.this);
        pagerAdapter.setPostImage(uriList);
        pager.setAdapter(pagerAdapter);

        circleIndicator = findViewById(R.id.indicator);
        circleIndicator.setViewPager(pager);

        contentEditText = findViewById(R.id.contentEditText);
        uploadPostButton = findViewById(R.id.uploadPostButton);
        uploadPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String content = contentEditText.getText().toString();
                if(content.length() > 0){
                    showProgressDialog("게시물을 등록하는 중입니다.");
                    long now = System.currentTimeMillis();
                    date = new Date(now);
//                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                    String postDate = sdf.format(date);
                    String postDate = String.valueOf(now);

                    UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
                        @Override
                        public Unit invoke(User user, Throwable throwable) {
                            kakaoId = String.valueOf(user.getId());
                            mDatabase.child("users").child(kakaoId).child("id").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    id = String.valueOf(task.getResult().getValue());
                                    String pathName = "Post/" + kakaoId + "/" + postDate+ "/";
                                    String thumbNailPathName = "Post/" + kakaoId + "/thumbNail/";

                                    for(int i = 0; i<uriList.size(); i++){
                                        InputStream in = null;
                                        try {
                                            in = getContentResolver().openInputStream(uriList.get(i));
                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                        Bitmap img = BitmapFactory.decodeStream(in);
                                        // bitmap 크기 400x400으로 줄이기
                                        img = img.createScaledBitmap(img, 1000, 1000, true);
                                        // bitmap 회전
                                        Matrix rotateMatrix = new Matrix();
                                        rotateMatrix.postRotate(getCameraPhotoOrientation(uriList.get(i), getFilePathFromURI(getApplicationContext(), uriList.get(i))));
                                        Bitmap rotateBitmap = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), rotateMatrix, false);
                                        try {
                                            in.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        // bitmap -> byte
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        rotateBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                        byte[] rotateByte = baos.toByteArray();
                                        if(i == 0){
                                            thumbNailPathRef = storageRef.child(thumbNailPathName+"/"+postDate+".png");
                                            UploadTask uploadTask = thumbNailPathRef.putBytes(rotateByte);
                                            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                    Log.d(TAG, "thumbNail Upload Success");
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                }
                                            });
                                        }

                                        pathRef = storageRef.child(pathName + "/"+i+".png");
                                        UploadTask uploadTask = pathRef.putBytes(rotateByte);
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

                }else{
                    Toast.makeText(getApplicationContext(), "내용을 작성해주세요", Toast.LENGTH_SHORT).show();
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
                "0",
                new ArrayList<String>()
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





    // 선택한 사진의 절대경로 구하는 것
    public static String getFilePathFromURI(Context context, Uri contentUri) {
        //copy file and send new file path
        String fileName = getFileName(contentUri);
        if (!TextUtils.isEmpty(fileName)) {
            File TEMP_DIR_PATH = context.getFilesDir();
            File copyFile = new File(TEMP_DIR_PATH + File.separator + fileName);
            copy(context, contentUri, copyFile);
            return copyFile.getAbsolutePath();
        }
        return null;
    }

    public static String getFileName(Uri uri) {
        if (uri == null) return null;
        String fileName = null;
        String path = uri.getPath();
        int cut = path.lastIndexOf('/');
        if (cut != -1) {
            fileName = path.substring(cut + 1);
        }
        return fileName;
    }

    public static void copy(Context context, Uri srcUri, File dstFile) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(srcUri);
            if (inputStream == null) return;
            OutputStream outputStream = new FileOutputStream(dstFile);
            IOUtils.copyStream(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // bitmap 회전 수치 구하기
    public int getCameraPhotoOrientation(Uri imageUri, String imagePath) {
        int rotate = 0;
        try {
            Log.d(TAG, "1 - " + imagePath);
            getContentResolver().notifyChange(imageUri, null);
            File imageFile = new File(imagePath);
            Log.d(TAG, "2 - " + imageFile);
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }

            Log.d("RotateImage", "Exif orientation: " + orientation);
            Log.d("RotateImage", "Rotate value: " + rotate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }


}