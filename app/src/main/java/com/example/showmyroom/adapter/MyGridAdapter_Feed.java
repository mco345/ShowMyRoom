package com.example.showmyroom.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class MyGridAdapter_Feed extends BaseAdapter {
    private static final String TAG = "MyGridAdapter";

    Context context;
    ArrayList<Uri> postUri = new ArrayList<>();

    //아이템 클릭 리스너 인터페이스
    public interface OnItemClickListener{
        void onItemClick(View v, int position); //뷰와 포지션값
    }
    //리스너 객체 참조 변수
    private MyRecyclerAdapter_Board.OnItemClickListener mListener = null;
    //리스너 객체 참조를 어댑터에 전달 메서드
    public void setOnItemClickListener(MyRecyclerAdapter_Board.OnItemClickListener listener) {
        this.mListener = listener;
    }

    public MyGridAdapter_Feed(Context context) {
        this.context = context;
    }

    public void setUriList(ArrayList<Uri> postUri) {
        this.postUri = postUri;
        Log.d(TAG, "postUri : "+postUri);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return postUri.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(400, 400));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setPadding(10,10,10,10);
        Glide.with(context)
                .load(postUri.get(position))
                .into(imageView);
        Log.d(TAG, "position : "+position);

        return imageView;
    }



}
