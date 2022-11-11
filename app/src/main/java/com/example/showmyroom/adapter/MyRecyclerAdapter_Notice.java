package com.example.showmyroom.adapter;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.showmyroom.R;
import com.example.showmyroom.TimeMaximum;
import com.example.showmyroom.items.NoticeItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MyRecyclerAdapter_Notice extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "MyRecyclerAdapter_Notice";

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    // ArrayList NoticeItem
    ArrayList<NoticeItem> noticeItems = new ArrayList<>();

    // 실시간 데이터베이스
    private DatabaseReference mDatabase;

    // 파이어 스토리지
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef, pathRef;


    //아이템 클릭 리스너 인터페이스
    public interface OnItemClickListener {
        void onItemClick(View v, int position); //뷰와 포지션값

        void onProfileClick(View v, int position);
    }

    //리스너 객체 참조 변수
    private OnItemClickListener mListener = null;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclerview_notice, parent, false);
            return new ItemViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclerview_board_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return noticeItems.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            ((ItemViewHolder) holder).onBind(noticeItems.get(position));
        } else if (holder instanceof LoadingViewHolder) {

        }
    }

    @Override
    public int getItemCount() {
        return noticeItems == null ? 0 : noticeItems.size();
    }

    public void setNoticeItems(ArrayList<NoticeItem> noticeItems, int page) {
        this.noticeItems = noticeItems;
        notifyItemRangeChanged((page - 1) * 10, 10);
    }


    private class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView messageText, dateText;

        public ItemViewHolder(View view) {
            super(view);

            profileImage = view.findViewById(R.id.notice_profileImage);
            messageText = view.findViewById(R.id.notice_messageTextView);
            dateText = view.findViewById(R.id.notice_dateTextView);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        if (mListener != null) {
                            mListener.onItemClick(v, position);
                        }
                    }
                }
            });

            profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        if (mListener != null) {
                            mListener.onProfileClick(view, position);
                        }
                    }
                }
            });
        }

        public void onBind(NoticeItem noticeItem) {
            // 프사
            storageRef = storage.getReference();
            pathRef = storageRef.child("Profile/" + noticeItem.getMyKakaoId() + ".png");
            pathRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(profileImage.getContext()).load(uri).apply(RequestOptions.bitmapTransform(new RoundedCorners(14))).into(profileImage);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    profileImage.setImageResource(R.drawable.ic_baseline_person_24);

                }
            });

            // 내용
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("users").child(noticeItem.getMyKakaoId()).child("id").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String userId = snapshot.getValue().toString();

                    String message = userId + "님이 회원님";
                    switch (noticeItem.getType()) {
                        case "like":
                            message = message + "의 게시물에 좋아요를 눌렀습니다.";
                            break;
                        case "comment":
                            String comment = noticeItem.getMessage();
                            if (comment.length() > 20) {
                                comment = comment.substring(0, 20);
                                comment = comment + "...";
                            }
                            message = message + "의 게시물에 " + '"' + comment + '"' + "라고 댓글을 달았습니다.";
                            break;
                        case "reply":
                            String reply = noticeItem.getMessage();
                            if (reply.length() > 20) {
                                reply.substring(0, 20);
                                reply = reply + "...";
                            }
                            message = message + "의 댓글에 "+ '"' + reply + '"' + "라고 대댓글을 달았습니다.";
                            break;
                        case "follow":
                            message = message + "을 팔로우했습니다.";
                            break;
                    }
                    messageText.setText(message);

                    // 날짜
                    dateText.setText(formatTimeString(noticeItem.getDate()));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });


        }
    }

    private class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(View view) {
            super(view);
        }
    }

    // 시간 단위 나누기
    private String formatTimeString(String date) {
        SimpleDateFormat monthDateFormat = new SimpleDateFormat("MM월 dd일");
        SimpleDateFormat yearDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");

        long regTime = Long.parseLong(date);

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
            if (diffTime <= 7) {
                msg = (diffTime) + "일 전";
            } else {
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
