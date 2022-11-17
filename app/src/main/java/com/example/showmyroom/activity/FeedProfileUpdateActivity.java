package com.example.showmyroom.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.showmyroom.PreferenceManager;
import com.example.showmyroom.R;
import com.example.showmyroom.activity.FeedActivity;
import com.google.android.gms.common.util.IOUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class FeedProfileUpdateActivity extends AppCompatActivity {
    private static final String TAG = "FeedPUActivity";

    // UI
    private ImageView profileImage;
    private Button changeProfileImageButton;
    private TextView updateButton, textCountTextView;
    private ImageButton backButton;
    private EditText nameEditText, introEditText;

    // change profileImage
    private boolean isChangeProfileImage = false;
    private String isChange = "";
    private Bitmap bitmap;

    // name, introduction
    private String name = "", introduction = "";

    // getIntent - thisFeedKakaoId
    private String thisFeedKakaoId;

    // 실시간 데이터베이스
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    // 파이어 스토어
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef, pathRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_profile_update);

        // getIntent
        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        introduction = intent.getStringExtra("introduction");
        thisFeedKakaoId = intent.getStringExtra("thisFeedKakaoId");

        // UI
        profileImage = findViewById(R.id.profileImage);
        changeProfileImageButton = findViewById(R.id.changeProfileImageButton);
        backButton = findViewById(R.id.backButton);
        updateButton = findViewById(R.id.updateButton);
        nameEditText = findViewById(R.id.nameEditText);
        introEditText = findViewById(R.id.introEditText);
        textCountTextView = findViewById(R.id.textCountTextView);

        // 뒤로가기 버튼
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // profile Image
        storageRef = storage.getReference();
        pathRef = storageRef.child("Profile/" + thisFeedKakaoId + ".png");
        pathRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if (getApplicationContext() != null) {
                    Glide.with(getApplicationContext()).load(uri).apply(RequestOptions.bitmapTransform(new RoundedCorners(30))).into(profileImage);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                profileImage.setImageResource(R.drawable.person);
            }
        });

        // chagne Profile Image
        changeProfileImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfileImage();
            }
        });


        // name
        nameEditText.setText(name);
        // introduction
        if (introduction.length() != 0) {
            introEditText.setText(introduction);
        }

        // introduction 글자 수 제한 - 50자
        textCountTextView.setText(introduction.length() + "/50");

        introEditText.addTextChangedListener(new TextWatcher() {
            String previousString = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                previousString = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textCountTextView.setText(introEditText.length() + "/50");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (introEditText.length() > 50) {
                    introEditText.setText(previousString);
                    introEditText.setSelection(introEditText.length());
                }
            }
        });


        // 수정 버튼
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name.length() == 0) {
                    Toast.makeText(getApplicationContext(), "이름을 입력해주세요", Toast.LENGTH_SHORT).show();
                } else {
                    // name
                    mDatabase.child("users").child(thisFeedKakaoId).child("name").setValue(nameEditText.getText().toString());

                    // introduction
                    introduction = introEditText.getText().toString();
                    Map<String, Object> introMap = new HashMap<>();
                    introMap.put("introduction", introduction);
                    mDatabase.child("users").child(thisFeedKakaoId).updateChildren(introMap);

                    // change profileImage
                    if (isChangeProfileImage) {
                        showProgressDialog("프로필을 수정중입니다.");

                        // storage에 저장
                        switch (isChange) {
                            case "change":
                                // bitmap -> byte
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] rotateByte = baos.toByteArray();

                                UploadTask uploadTask = pathRef.putBytes(rotateByte);
                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        Toast.makeText(getApplicationContext(), "프로필 수정을 완료했습니다.", Toast.LENGTH_SHORT).show();
                                        // intent
                                        Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
                                        intent.putExtra("kakaoId", thisFeedKakaoId);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                                break;
                            case "delete":
                                pathRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getApplicationContext(), "프로필 수정을 완료했습니다.", Toast.LENGTH_SHORT).show();
                                        // intent
                                        Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
                                        intent.putExtra("kakaoId", thisFeedKakaoId);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), "프로필 수정을 완료했습니다.", Toast.LENGTH_SHORT).show();
                                        // intent
                                        Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
                                        intent.putExtra("kakaoId", thisFeedKakaoId);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }
                                });
                                break;
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "프로필 수정을 완료했습니다.", Toast.LENGTH_SHORT).show();
                        // intent
                        Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
                        intent.putExtra("kakaoId", thisFeedKakaoId);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }

            }
        });

    }

    public void updateProfileImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(FeedProfileUpdateActivity.this);
        builder.setItems(new String[]{"프로필 사진 등록", "프로필 사진 삭제"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent, 0);
                        break;
                    case 1:
                        isChange = "delete";
                        isChangeProfileImage = true;
                        profileImage.setImageResource(R.drawable.person);
                        break;
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (data != null) {
                Uri uri = data.getData();
                try {
                    isChange = "change";
                    isChangeProfileImage = true;

                    InputStream in = getApplicationContext().getContentResolver().openInputStream(uri);
                    Bitmap img = BitmapFactory.decodeStream(in);
                    // bitmap 크기 400x400으로 줄이기
                    img = img.createScaledBitmap(img, 400, 400, true);
                    // bitmap 회전
                    Matrix rotateMatrix = new Matrix();
                    Log.d(TAG, String.valueOf(getCameraPhotoOrientation(uri, getFilePathFromURI(getApplicationContext(), uri))));
                    rotateMatrix.postRotate(getCameraPhotoOrientation(uri, getFilePathFromURI(getApplicationContext(), uri)));
                    bitmap = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), rotateMatrix, false);
                    in.close();

                    Glide.with(getApplicationContext()).load(bitmap).apply(RequestOptions.bitmapTransform(new RoundedCorners(30))).into(profileImage);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
            getApplicationContext().getContentResolver().notifyChange(imageUri, null);
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

    // 프로그래스바 다이얼로그
    private void showProgressDialog(String message) {
        ProgressDialog dialog = new ProgressDialog(FeedProfileUpdateActivity.this);
        dialog.setCancelable(false);    // 화면 밖 터치해도 dialog 취소되지 않게
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(message);
        dialog.show();
    }
}