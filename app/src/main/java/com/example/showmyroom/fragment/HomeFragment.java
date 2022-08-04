package com.example.showmyroom.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.showmyroom.R;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private View writeButton;

    // 갤러리 호출
    final int REQUEST_TAKE_ALBUM = 1;
    PermissionCheck permissionCheck;

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

        permissionCheck = new PermissionCheck(getActivity());

        writeButton = v.findViewById(R.id.writeFloatingButton);
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAlbum();
            }
        });

        return v;
    }

    private void getAlbum(){
        // 앨범 호출
        boolean isAlbum = permissionCheck.isCheck("Album");
        if(isAlbum){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                    startActivityForResult(Intent.createChooser(intent,"다중 선택은 '포토'를 선택하세요."), REQUEST_TAKE_ALBUM);
                }catch(Exception e){
                    Log.e("error", e.toString());
                }
            }
            else{
                Log.e("kitkat under", "..");
            }
        }
}