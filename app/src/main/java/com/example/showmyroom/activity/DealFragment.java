package com.example.showmyroom.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.showmyroom.R;
import com.example.showmyroom.adapter.MyGridAdapter_Deal;
import com.example.showmyroom.adapter.MyGridAdapter_Feed;
import com.example.showmyroom.fragment.HomeFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;

public class DealFragment extends Fragment {
    private static final String TAG = "DealFragment";

    //UI
    private EditText searchEditText;
    private ImageButton searchButton, filterButton;
    private FloatingActionButton writeButton;

    // 그리드뷰
    private GridView dealGridView;
    private MyGridAdapter_Deal myGridAdapter = new MyGridAdapter_Deal();

    // 앨범 이미지 선택
    private static final int FROM_GALLARY = 0;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef, pathRef;
    private ArrayList<Uri> uriList = new ArrayList<>(); // 선택한 이미지 리스트
    // crop
    private int cropPosition = 0;
    private ArrayList<Uri> cropList = new ArrayList<>();

    public DealFragment() {
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
        View v = inflater.inflate(R.layout.fragment_deal, container, false);

        // UI
        searchEditText = v.findViewById(R.id.searchEditText);
        searchButton = v.findViewById(R.id.searchButton);
        filterButton = v.findViewById(R.id.filterButton);
        writeButton = v.findViewById(R.id.writeFloatingButton);
        dealGridView = v.findViewById(R.id.dealGridView);

        // GridView



        // write
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

                    if (clipData.getItemCount() > 10) {
                        Toast.makeText(getActivity(), "사진은 10장까지 선택 가능합니다.", Toast.LENGTH_SHORT).show();
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

    private void startCropActivity(@NonNull ArrayList<Uri> uriList, int position) {
        CropImage.activity(uriList.get(position)).setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .start(getContext(), DealFragment.this);
    }

    private void handleCropResult(@NonNull Uri uri) {
        if (uri != null) {
            try {
                cropList.add(uri);
                Log.d(TAG, "cropList size : " + cropList.size());
                cropPosition++;
                if (cropList.size() == uriList.size()) {
                    Intent intent = new Intent(getActivity(), WriteDealActivity.class);
                    intent.putExtra("uriList", cropList);
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