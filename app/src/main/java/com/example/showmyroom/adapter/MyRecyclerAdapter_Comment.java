package com.example.showmyroom.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.showmyroom.TimeMaximum;
import com.example.showmyroom.activity.FeedActivity;
import com.example.showmyroom.items.CommentItem;
import com.example.showmyroom.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class MyRecyclerAdapter_Comment extends RecyclerView.Adapter<MyRecyclerAdapter_Comment.ViewHolder> {
    private static final String TAG = "commentAdapter";

    private Context context;

    private ArrayList<CommentItem> commentList;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef, pathRef;

    //아이템 클릭 리스너 인터페이스
    public interface OnItemClickListener{
        void onItemClick(View v, int position); //뷰와 포지션값
        void onDeleteClick(View v, int position);
    }
    //리스너 객체 참조 변수
    private MyRecyclerAdapter_Comment.OnItemClickListener mListener = null;
    //리스너 객체 참조를 어댑터에 전달 메서드
    public void setOnItemClickListener(MyRecyclerAdapter_Comment.OnItemClickListener listener) {
        this.mListener = listener;
    }

    private int mode = -1;

    @NonNull
    @Override
    public MyRecyclerAdapter_Comment.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();

        if(parent.getContext().toString().contains("FeedPostActivity")){
            mode = 0;
        }else if(parent.getContext().toString().contains("PostActivity")){
            mode = 1;
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclerview_comment, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyRecyclerAdapter_Comment.ViewHolder holder, int position) {
        Log.d(TAG, String.valueOf(commentList.size()));
        try{
            Map commentItem = new HashMap();
            commentItem = (Map) commentList.get(position);
            String thisPostkakaoId = String.valueOf(commentItem.get("thisPostKakaoId"));
            String userId = String.valueOf(commentItem.get("userId"));
            String comment = String.valueOf(commentItem.get("comment"));
            String date = String.valueOf(commentItem.get("date"));
            String kakaoId = String.valueOf(commentItem.get("kakaoId"));
            Boolean isReply = (Boolean) commentItem.get("reply");
            Boolean isSecret = (Boolean) commentItem.get("secret");
            String realKakaoId = String.valueOf(commentItem.get("realKakaoId"));
            CommentItem item = new CommentItem(thisPostkakaoId, kakaoId, userId, comment, date, isReply, isSecret, realKakaoId);

            holder.onBind(item);
        }catch(Exception e){
            holder.onBind(commentList.get(position));
        }

    }

    public void setmCommentList(ArrayList<CommentItem> commentItems) {
        commentList = commentItems;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        String userId, kakaoId;
        TextView name, secretComment, comment, date;
        View line;
        ImageView arrow, commentImage, secretImage;
        ImageButton deleteButton;
        LinearLayout commentLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            commentLayout = (LinearLayout) itemView.findViewById(R.id.commentLayout);
            name = (TextView) itemView.findViewById(R.id.name);
            secretComment = (TextView) itemView.findViewById(R.id.secretMessage);
            comment = (TextView) itemView.findViewById(R.id.message);
            date = (TextView) itemView.findViewById(R.id.date);
            arrow = (ImageView) itemView.findViewById(R.id.replyImageView);
            commentImage = (ImageView) itemView.findViewById(R.id.commentImageView);
            secretImage = (ImageView) itemView.findViewById(R.id.secretImage);
            deleteButton = (ImageButton) itemView.findViewById(R.id.commentDeleteButton);
            line = (View) itemView.findViewById(R.id.line);



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

            deleteButton.setOnClickListener (new View.OnClickListener () {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition ();
                    if (position!=RecyclerView.NO_POSITION){
                        if (mListener!=null){
                            mListener.onDeleteClick(view,position);
                        }
                    }
                }
            });



        }

        public void onBind(CommentItem item) {
            Activity activity = (Activity) context;

            UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
                @Override
                public Unit invoke(User user, Throwable throwable) {
                    line.setVisibility(View.VISIBLE);



                    // 프사
                    if(mode == 0) {
                        commentImage.setVisibility(View.VISIBLE);
                        commentImage.setImageResource(0);
                        storageRef = storage.getReference();
                        pathRef = storageRef.child("Profile/" + item.getKakaoId() + ".png");
                        pathRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                if(((Activity) context).isFinishing()) return;
                                Glide.with(commentImage.getContext()).load(uri).apply(RequestOptions.bitmapTransform(new RoundedCorners(14))).into(commentImage);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                commentImage.setImageResource(R.drawable.person);
                            }
                        });
                    }

                    commentImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!item.getUserId().equals("(삭제)") && !item.getUserId().equals("탈퇴한 회원")){
                                Intent intent = new Intent(v.getContext(), FeedActivity.class);
                                intent.putExtra("kakaoId", item.getKakaoId());
                                v.getContext().startActivity(intent);
                            }
                        }
                    });


                    // 아이디
                    if(item.getKakaoId().equals(item.getThisPostKakaoId())){
                        userId = item.getUserId()+"(작성자)";
                        name.setTextColor(Color.BLUE);
                    }else{
                        userId = item.getUserId();
                        name.setTextColor(Color.BLACK);
                    }

                    if(item.getUserId().equals("탈퇴한 회원")){
                        userId = "("+item.getUserId()+")";
                        name.setTypeface(Typeface.DEFAULT);
                        name.setTextColor(Color.parseColor("#D3D3D3"));
                    }else if(item.getUserId().equals("(삭제)")){
                        userId = item.getUserId();
                        name.setTypeface(Typeface.DEFAULT);
                        name.setTextColor(Color.parseColor("#D3D3D3"));
                    }else if(item.getSecret()){
                        name.setVisibility(View.GONE);
                        comment.setVisibility(View.GONE);
                        secretComment.setVisibility(View.VISIBLE);
                    }

                    name.setText(userId);
                    // 내용
                    comment.setText(item.getComment());
                    // 날짜
                    if(item.getDate().equals("")){
                        date.setVisibility(View.GONE);
                    }else{
                        date.setText(formatTimeString(item.getDate()));
                    }




                    // 대댓글일 경우
                    if(item.getReply()) {
                        arrow.setVisibility(View.VISIBLE);
                        // 비밀 댓글의 대댓글일 경우
                        if(item.getSecret()){
                            Log.d(TAG, "date : "+item.getDate()+" getRealKakaoId : "+item.getRealKakaoId()+" user.getId : "+user.getId());
                            // 내가 단 비밀댓글의 대댓글일 경우 내가 작성한 대댓글이 아니여도 보임
                            // 물론 게시물 작성자는 비밀댓글 그냥 다 보임
                            if(item.getRealKakaoId().equals(String.valueOf(user.getId()))){
                                name.setVisibility(View.VISIBLE);
                                comment.setVisibility(View.VISIBLE);
                                secretComment.setVisibility(View.GONE);
                                secretImage.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    Log.d(TAG, "kakaoId : "+user.getId());
                    // 비밀 댓글일 때
                    // 작성한 댓글이 내가 작성한 댓글이면 비밀 댓글이여도 댓글 보임
                    if(item.getKakaoId().equals(String.valueOf(user.getId()))){
                        deleteButton.setVisibility(View.VISIBLE);
                        name.setVisibility(View.VISIBLE);
                        comment.setVisibility(View.VISIBLE);
                        secretComment.setVisibility(View.GONE);
                        if(item.getSecret()) secretImage.setVisibility(View.VISIBLE);
                    }
                    // 내가 작성한 게시물이면 비밀 댓글이여도 댓글 보임
                    if(item.getThisPostKakaoId().equals(String.valueOf(user.getId()))){
                        name.setVisibility(View.VISIBLE);
                        comment.setVisibility(View.VISIBLE);
                        secretComment.setVisibility(View.GONE);
                        if(item.getSecret()) secretImage.setVisibility(View.VISIBLE);
                    }

                    Log.d(TAG, String.valueOf(item.getReply())+item.getComment()+String.valueOf(item.getSecret()));
                    return null;
                }
            });




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
