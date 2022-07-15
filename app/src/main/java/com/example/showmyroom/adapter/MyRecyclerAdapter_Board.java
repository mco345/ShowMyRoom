package com.example.showmyroom.adapter;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.showmyroom.TimeMaximum;
import com.example.showmyroom.items.BoardItem;
import com.example.showmyroom.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MyRecyclerAdapter_Board extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = "Adapter";

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    private ArrayList<BoardItem> mBoardList;

    //아이템 클릭 리스너 인터페이스
    public interface OnItemClickListener{
        void onItemClick(View v, int position); //뷰와 포지션값
    }
    //리스너 객체 참조 변수
    private OnItemClickListener mListener = null;
    //리스너 객체 참조를 어댑터에 전달 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_ITEM){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclerview_board, parent, false);
            return new ItemViewHolder(view);
        }else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclerview_board_loading, parent, false);
            return new LoadingViewHolder(view);
        }

    }

    @Override
    public int getItemViewType(int position) {
        return mBoardList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ItemViewHolder){
            ((ItemViewHolder) holder).onBind(mBoardList.get(position));
        }else if(holder instanceof LoadingViewHolder){

        }

    }

    @Override
    public int getItemCount() {
        return mBoardList == null ? 0 : mBoardList.size();
    }

    public void setmBoardList(ArrayList<BoardItem> mBoardItems, int page) {
        mBoardList = mBoardItems;
        notifyItemRangeChanged((page - 1) * 10, 10);
//        notifyDataSetChanged();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView name, title, message, date, commentNum;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            title = itemView.findViewById(R.id.title);
            message = itemView.findViewById(R.id.message);
            date = itemView.findViewById(R.id.date);
            commentNum = itemView.findViewById(R.id.commentNumTextView_Recycler);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition ();
                    if (position!=RecyclerView.NO_POSITION){
                        if (mListener!=null){
                            mListener.onItemClick (v,position);
                        }
                    }
                }
            });
        }

        public void onBind(BoardItem item) {
            Log.d(TAG, item.getCommentNum());

            if(item.getUserId().equals("탈퇴한 회원")){
                name.setText("("+item.getUserId()+")");
                name.setTextColor(Color.parseColor("#D3D3D3"));
            }else{
                name.setText(item.getUserId());
                name.setTextColor(Color.BLACK);
            }
            title.setText(item.getTitle());
            message.setText(item.getMessage().replace("\n", " "));
            date.setText(formatTimeString(item.getDate()));
            commentNum.setText(item.getCommentNum());
        }
    }

    private class LoadingViewHolder extends RecyclerView.ViewHolder{
        private ProgressBar progressBar;
        public LoadingViewHolder(View itemView) {
            super(itemView);

            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    // 시간 단위 나누기
    private String formatTimeString(String date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat monthDateFormat = new SimpleDateFormat("MM월 dd일");
        SimpleDateFormat yearDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");

        Date mDate = null;
        try {
            mDate = simpleDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long regTime = mDate.getTime();
        Log.d(TAG, String.valueOf(regTime));

        long curTime = System.currentTimeMillis();
        long diffTime = (curTime - regTime) / 1000;
        String msg = null;
        if (diffTime < TimeMaximum.SEC) {
            msg = "방금 전";
        } else if ((diffTime /= TimeMaximum.SEC) < TimeMaximum.MIN) {
            msg = diffTime + "분 전";
        } else if ((diffTime /= TimeMaximum.MIN) < TimeMaximum.HOUR) {
            msg = (diffTime) + "시간 전";
        } else if ((diffTime /= TimeMaximum.HOUR) < TimeMaximum.DAY) {
            if(diffTime <= 7){
                msg = (diffTime) + "일 전";
            }else{
                msg = monthDateFormat.format(new Date(regTime));
            }
        } else if ((diffTime /= TimeMaximum.DAY) < TimeMaximum.MONTH) {
//            msg = (diffTime) + "달 전";
            msg = monthDateFormat.format(new Date(regTime));
        } else {
            msg = yearDateFormat.format(new Date(regTime));
        }
        return msg;
    }


}
