package com.example.showmyroom.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.showmyroom.PreferenceManager;
import com.example.showmyroom.R;
import com.example.showmyroom.activity.CropImageActivity;
import com.example.showmyroom.activity.MainActivity;
import com.example.showmyroom.activity.WriteHomeActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private View writeButton;

    // 앨범 이미지 선택
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef, pathRef;
    private ArrayList<Uri> uriList = new ArrayList<>(); // 선택한 이미지 리스트
    // 사진 요청코드
    private static final int FROM_GALLARY = 0;

    // crop
    private int position = 0;
    private ArrayList<Uri> cropList = new ArrayList<>();    // crop 후 이미지 리스트

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

        storageRef = storage.getReference();


        writeButton = v.findViewById(R.id.writeFloatingButton);
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, FROM_GALLARY);
            }
        });

        return v;
    }

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
                } else {
                    ClipData clipData = data.getClipData();
                    Log.e("clipData", String.valueOf(clipData.getItemCount()));

                    if (clipData.getItemCount() > 5) {
                        Toast.makeText(getActivity(), "사진은 5장까지 선택 가능합니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "multiple choice");
                        for(int i = 0; i < clipData.getItemCount(); i++){
                            Uri imageUri = clipData.getItemAt(i).getUri();
                            uriList.add(imageUri);
                        }
                    }
                }
                startCropActivity(uriList, position);



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