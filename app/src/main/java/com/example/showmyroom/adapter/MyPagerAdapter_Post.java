package com.example.showmyroom.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.showmyroom.R;
import com.example.showmyroom.items.BoardItem;

import java.util.ArrayList;

public class MyPagerAdapter_Post extends PagerAdapter {
    private ArrayList<Uri> uriList = new ArrayList<>();
    private Context context;

    public MyPagerAdapter_Post(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return uriList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (View)object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }

    public void setPostImage(ArrayList<Uri> postImagesUri) {
        uriList = postImagesUri;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.viewpager_postimage, container, false);

        ImageView imageView = view.findViewById(R.id.postImage);
        Glide.with(context).load(uriList.get(position)).apply(RequestOptions.bitmapTransform(new RoundedCorners(30))).into(imageView);

        container.addView(view);

        return view;
    }
}
