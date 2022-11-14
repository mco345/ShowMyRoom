package com.example.showmyroom.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
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
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.showmyroom.PreferenceManager;
import com.example.showmyroom.R;
import com.example.showmyroom.adapter.MyPagerAdapter_Post;
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
import java.util.ArrayList;
import java.util.Date;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import me.relex.circleindicator.CircleIndicator;

public class WriteHomeActivity extends AppCompatActivity {
    private static final String TAG = "WriteHomeActivity";

    // whatSelected
    private String whatSelected;

    // roomLayout
    private LinearLayout roomLayout;
    private Button pyButton, dwellButton, styleButton;
    private String py = "null", dwell = "null", style = "null", keyword = "";
    private Button keywordButton;

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

    // keyword
    private ArrayList<String> keywordsList = new ArrayList<>();

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
        whatSelected = getIntent().getStringExtra("whatSelected");
        Log.d(TAG, "whatSelected : " + whatSelected);

        // 파이어베이스
        mDatabase = FirebaseDatabase.getInstance().getReference();
        storageRef = storage.getReference();

        // roomLayout UI - 내 방 볼래 경우에만 visible
        roomLayout = findViewById(R.id.roomButtonLayout);
        pyButton = findViewById(R.id.pyButton);
        dwellButton = findViewById(R.id.dwellButton);
        styleButton = findViewById(R.id.styleButton);
        keywordButton = findViewById(R.id.keywordButton);
        if (whatSelected.equals("daily")) {
            roomLayout.setVisibility(View.GONE);
            keywordButton.setVisibility(View.GONE);
        }
        // 평수
        pyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenu popup_py = new PopupMenu(getApplicationContext(), v);
                getMenuInflater().inflate(R.menu.popup_py, popup_py.getMenu());
                popup_py.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        py = item.getTitle().toString();
                        pyButton.setText(py);
                        pyButton.setBackgroundResource(R.drawable.room_button_selected);
                        pyButton.setTextColor((ContextCompat.getColor(getApplicationContext(), R.color.light_coral)));

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
                final PopupMenu popup_dwell = new PopupMenu(getApplicationContext(), v);
                getMenuInflater().inflate(R.menu.popup_dwell, popup_dwell.getMenu());
                popup_dwell.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        dwell = item.getTitle().toString();
                        dwellButton.setText(dwell);
                        dwellButton.setBackgroundResource(R.drawable.room_button_selected);
                        dwellButton.setTextColor((ContextCompat.getColor(getApplicationContext(), R.color.light_coral)));
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
                final PopupMenu popup_style = new PopupMenu(getApplicationContext(), v);
                getMenuInflater().inflate(R.menu.popup_style, popup_style.getMenu());
                popup_style.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        style = item.getTitle().toString();
                        styleButton.setText(style);
                        styleButton.setBackgroundResource(R.drawable.room_button_selected);
                        styleButton.setTextColor((ContextCompat.getColor(getApplicationContext(), R.color.light_coral)));
                        return false;
                    }
                });
                popup_style.show();
            }
        });
        // 키워드 버튼
        keywordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TagActivity.class);
                intent.putExtra("keywordsList", keywordsList);
                startActivityForResult(intent, 0);
            }
        });


        // 뷰페이저
        pager = findViewById(R.id.postImageViewPager);
        pagerAdapter = new MyPagerAdapter_Post(WriteHomeActivity.this);
        pagerAdapter.setPostImage(uriList);
        pager.setAdapter(pagerAdapter);
        circleIndicator = findViewById(R.id.indicator);
        circleIndicator.setViewPager(pager);

        contentEditText = findViewById(R.id.contentEditText);
        uploadPostButton = findViewById(R.id.uploadPostButton);

        // 업로드
        uploadPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String content = contentEditText.getText().toString();
                if (content.length() > 0) {
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
                                    String pathName = "Post/" + kakaoId + "/" + postDate + "/";
                                    String thumbNailPathName = "Post/" + kakaoId + "/thumbNail/";

                                    for (int i = 0; i < uriList.size(); i++) {
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
                                        if (i == 0) {
                                            thumbNailPathRef = storageRef.child(thumbNailPathName + "/" + postDate + ".png");
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

                                        pathRef = storageRef.child(pathName + "/" + i + ".png");
                                        UploadTask uploadTask = pathRef.putBytes(rotateByte);
                                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                if (forNum == uriList.size()) {
                                                    uploader(kakaoId, id, content, postDate);
                                                } else {
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

                } else {
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
                whatSelected,
                kakaoId,
                id,
                content,
                postDate,
                keywordsList,
                py,
                dwell,
                style,
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

    // 태그 추가 후


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK && requestCode == 0){
            keyword = data.getExtras().getString("keyword");
            keywordsList =  (ArrayList<String>) data.getExtras().getSerializable("keywordsList");
            Log.d(TAG, "keywordsList"+ keywordsList);
            keywordButton.setText(keyword);
        }
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