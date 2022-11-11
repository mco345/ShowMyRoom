package com.example.showmyroom.adapter;

import android.content.Context;
import android.net.Uri;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.showmyroom.R;
import com.example.showmyroom.items.DealItem;

import java.util.ArrayList;

public class MyGridAdapter_Deal extends BaseAdapter {
    private static final String TAG = "MyGridAdapter_Deal";

    private ArrayList<DealItem> dealItems;

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

    public void setProductList(ArrayList<DealItem> dealItems) {
        this.dealItems = dealItems;
        Log.d(TAG, "postUri : "+dealItems);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return dealItems.size();
    }

    @Override
    public Object getItem(int position) {
        return dealItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        final Context context  = parent.getContext();

        if(v == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.item_gridview_deal, parent, false);

            // 상품 사진
            ImageView productImageView = v.findViewById(R.id.productImageView);
            Glide.with(context)
                    .load(dealItems.get(position).getProductImage())
                    .into(productImageView);

            // 가격
            TextView tv_price = v.findViewById(R.id.priceTextView);
            tv_price.setText(dealItems.get(position).getPrice());

            // 제목
            TextView tv_title = v.findViewById(R.id.titleTextView);
            tv_title.setText(dealItems.get(position).getTitle());

        }else{
            View view = new View(context);
            view = v;
        }
        return v;
    }



}
