package com.example.showmyroom.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.GradientDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.showmyroom.FeedActivity;
import com.example.showmyroom.PreferenceManager;
import com.example.showmyroom.R;
import com.example.showmyroom.TimeMaximum;
import com.example.showmyroom.activity.LoginActivity;
import com.example.showmyroom.activity.MainActivity;
import com.example.showmyroom.activity.PostActivity;
import com.example.showmyroom.items.CommentItem;
import com.example.showmyroom.items.SecretMemberItem;
import com.google.android.gms.common.util.IOUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;


public class SettingFragment extends Fragment {
    public MainActivity activity;   // context

    private static final String TAG = "SettingFragment";
    private TextView idText, nameText;
    private LinearLayout progressBarLayout;
    private TextView profileText, profileImageText, logout, withdraw; // 계정 칸
    private ImageView profileImage;
    private DatabaseReference mDatabase;
    private String kakaoId;
    private String[] boards = new String[]{"posts", "posts_secret", "posts_deal"};

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef, pathRef;

    // 댓글
    private ArrayList<CommentItem> commentItems;
    private ArrayList<SecretMemberItem> secretMemberItems;


    // 프로필 사진 요청코드
    private static final int FROM_GALLARY = 0;

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (MainActivity) activity;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_setting, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // 사용자 정보칸
        idText = v.findViewById(R.id.textId);
        nameText = v.findViewById(R.id.textName);
        profileImage = v.findViewById(R.id.profileImageView);
        progressBarLayout = v.findViewById(R.id.progressBarLayout);

        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                kakaoId = String.valueOf(user.getId());
                storageRef = storage.getReference();
                pathRef = storageRef.child("Profile/" + kakaoId + ".png");
                if (pathRef == null) {
                    Toast.makeText(getActivity(), "프로필 사진이 없습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    pathRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            if (getActivity() != null) {
                                Glide.with(getActivity()).load(uri).apply(RequestOptions.bitmapTransform(new RoundedCorners(30))).into(profileImage);
                                progressBarLayout.setVisibility(View.GONE);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBarLayout.setVisibility(View.GONE);
                        }
                    });
                }
                String kakaoId = String.valueOf(user.getId());
                mDatabase.child("users").child(kakaoId).child("id").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        idText.setText(String.valueOf(task.getResult().getValue()));
                    }
                });
                mDatabase.child("users").child(kakaoId).child("name").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        nameText.setText(String.valueOf(task.getResult().getValue()));
                    }
                });

                return null;
            }
        });

        // 계정
        // 내 프로필
        profileText = v.findViewById(R.id.profileTextView);
        profileText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FeedActivity.class);
                UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
                    @Override
                    public Unit invoke(User user, Throwable throwable) {
                        intent.putExtra("kakaoId", String.valueOf(user.getId()));
                        startActivity(intent);
                        return null;
                    }
                });

            }
        });

        // 프로필 사진 변경
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfileImage();
            }
        });
        profileImageText = v.findViewById(R.id.profileImageTextView);
        profileImageText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfileImage();
            }
        });

        // 로그아웃
        logout = v.findViewById(R.id.logoutTextView);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserApiClient.getInstance().logout(new Function1<Throwable, Unit>() {
                    @Override
                    public Unit invoke(Throwable throwable) {
                        Toast.makeText(getActivity(), "로그아웃하였습니다.", Toast.LENGTH_SHORT).show();
                        PreferenceManager.setBoolean(getActivity(), "AutoLogin", false);
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        return null;
                    }
                });
            }
        });
        // 회원 탈퇴
        withdraw = v.findViewById(R.id.withDrawTextView);
        withdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("정말 회원 탈퇴 하시겠습니까?");
                builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
                            @Override
                            public Unit invoke(User user, Throwable throwable) {
                                String kakaoId = String.valueOf(user.getId());
                                mDatabase.child("users").child(kakaoId).removeValue();

                                return null;
                            }
                        });

                        deletePost();

                        pathRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });

                        UserApiClient.getInstance().logout(new Function1<Throwable, Unit>() {
                            @Override
                            public Unit invoke(Throwable throwable) {
                                Toast.makeText(getActivity(), "탈퇴하였습니다.", Toast.LENGTH_SHORT).show();
                                PreferenceManager.setBoolean(getActivity(), "AutoLogin", false);
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                startActivity(intent);
                                return null;
                            }
                        });
                    }
                }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });


        return v;
    }

    public void updateProfileImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(new String[]{"프로필 사진 등록", "프로필 사진 삭제"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent, FROM_GALLARY);
                        break;
                    case 1:
                        showProgressDialog("프로필 사진 삭제 중");
                        pathRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                if (getActivity() != null) {
                                    Toast.makeText(getActivity(), "프로필 사진을 삭제했습니다.", Toast.LENGTH_SHORT).show();
//                                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                    PreferenceManager.setInt(getActivity(), "BackToMain", 3);
                                    Intent intent = new Intent(getActivity(), MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(activity, "현재 프로필 사진이 없습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });


                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // 작성한 게시물 또는 댓글 작성자 이름 탈퇴한 회원으로 변경
    public void deletePost() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        for (String board : boards) {
            Query first = db.collection(board);
            first.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    // 게시물 작성자 -  탈퇴한 회원으로 변경
                                    if (document.getData().get("kakaoId").equals(kakaoId)) {
                                        Log.d(TAG, "id - " + document.getId());
                                        db.collection(board).document(document.getId())
                                                .update("kakaoId", "", "userId", "탈퇴한 회원")
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {

                                                    }
                                                });
                                    }
                                    // 익명 리스트 삭제
                                    if(board.equals("posts_secret")){
                                        secretMemberItems = new ArrayList<>();
                                        secretMemberItems = (ArrayList<SecretMemberItem>) document.getData().get("secretMember");
                                        for (int i = 0; i < secretMemberItems.size(); i++) {
                                            Map secretMember = new HashMap();
                                            secretMember = (Map) secretMemberItems.get(i);
                                            if(secretMember.get("kakaoId").equals(kakaoId)){
                                                Log.d(TAG, "카카오 아이디가 같습니다!!!");
                                                secretMemberItems.set(i, new SecretMemberItem(
                                                        "",
                                                        Integer.parseInt(String.valueOf(secretMember.get("num")))
                                                ));
                                            }
                                        }
                                        db.collection(board).document(document.getId())
                                                .update( "secretMember", secretMemberItems);
                                    }
                                    // 댓글 작성자 - 탈퇴한 회원으로 변경
                                    commentItems = new ArrayList<>();
                                    commentItems = (ArrayList<CommentItem>) document.getData().get("comment");
                                    for (int i = 0; i < commentItems.size(); i++) {
                                        Map commentItem = new HashMap();
                                        commentItem = (Map) commentItems.get(i);
                                        String afterKakaoId = String.valueOf(commentItem.get("kakaoId"));
                                        String afterUserId = String.valueOf(commentItem.get("userId"));
                                        if (afterKakaoId.equals(kakaoId)) {
                                            afterKakaoId = "";
                                            afterUserId = "탈퇴한 회원";
                                        }
                                        ArrayList<CommentItem> replyList = (ArrayList<CommentItem>) commentItem.get("replyList");
                                        if ((Boolean) commentItem.get("mode")) {
                                            for (int j = 0; j < replyList.size(); j++) {
                                                Map replyItem = new HashMap();
                                                replyItem = (Map) replyList.get(j);
                                                if (replyItem.get("kakaoId").equals(kakaoId)) {
                                                    replyList.set(j, new CommentItem(
                                                            String.valueOf(replyItem.get("thisPostKakaoId")),
                                                            "",
                                                            "탈퇴한 회원",
                                                            String.valueOf(replyItem.get("comment")),
                                                            String.valueOf(replyItem.get("date")),
                                                            true,
                                                            (Boolean) replyItem.get("secret"),
                                                            String.valueOf(replyItem.get("realKakaoId"))
                                                    ));
                                                }
                                            }

                                        }
                                        commentItems.set(i, new CommentItem(
                                                String.valueOf(commentItem.get("thisPostKakaoId")),
                                                afterKakaoId,
                                                afterUserId,
                                                String.valueOf(commentItem.get("comment")),
                                                String.valueOf(commentItem.get("date")),
                                                (Boolean) commentItem.get("mode"),
                                                (Boolean) commentItem.get("reply"),
                                                replyList,
                                                (Boolean) commentItem.get("secret")
                                        ));
                                    }
                                    db.collection(board).document(document.getId())
                                            .update("comment", commentItems);
                                }
                            }
                        }
                    });
        }


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FROM_GALLARY) {
            if (data != null) {
                showProgressDialog("프로필 사진 변경 중");
                Uri uri = data.getData();
                try {
                    InputStream in = getActivity().getContentResolver().openInputStream(uri);
                    Bitmap img = BitmapFactory.decodeStream(in);
                    // bitmap 크기 400x400으로 줄이기
                    img = img.createScaledBitmap(img, 400, 400, true);
                    // bitmap 회전
                    Matrix rotateMatrix = new Matrix();
                    Log.d(TAG, String.valueOf(getCameraPhotoOrientation(uri, getFilePathFromURI(getActivity(), uri))));
                    rotateMatrix.postRotate(getCameraPhotoOrientation(uri, getFilePathFromURI(getActivity(), uri)));
                    Bitmap rotateBitmap = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), rotateMatrix, false);
                    in.close();

                    // bitmap -> byte
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    rotateBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] rotateByte = baos.toByteArray();

                    // storage에 저장
//                UploadTask uploadTask = pathRef.putFile(getImageUri(rotateBitmap));
                    UploadTask uploadTask = pathRef.putBytes(rotateByte);
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            if (getActivity() != null) {
                                Toast.makeText(getActivity(), "프로필 사진을 등록했습니다.", Toast.LENGTH_SHORT).show();
                                PreferenceManager.setInt(getActivity(), "BackToMain", 3);
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

//    // bitmap을 uri로 변환
//    private Uri getImageUri(Bitmap inImage) {
//        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
//        String path = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), inImage, "thumbnail" + kakaoId, null);
//        return Uri.parse(path);
//    }

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
            getActivity().getContentResolver().notifyChange(imageUri, null);
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
//        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setCancelable(false);    // 화면 밖 터치해도 dialog 취소되지 않게
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(message);
        dialog.show();
    }


}