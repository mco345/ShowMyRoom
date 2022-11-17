package com.example.showmyroom.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.showmyroom.R;
import com.example.showmyroom.TimeMaximum;
import com.example.showmyroom.activity.FeedActivity;
import com.example.showmyroom.activity.FeedPostActivity;
import com.example.showmyroom.items.HomeItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import me.relex.circleindicator.CircleIndicator;

public class MyRecyclerAdapter_Home extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "homeAdapter";

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef, profileRef, postRef;

    private String kakaoId, thisFeedKakaoId, date;

    private boolean isLike = false;
    private int position;

    // 파이어스토어
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ArrayList<String> likeList;

    private ArrayList<HomeItem> mHomeList;

    private Context context = null;

    //아이템 클릭 리스너 인터페이스
    public interface OnItemClickListener {
        void onItemClick(View v, int position); //뷰와 포지션값
    }

    //리스너 객체 참조 변수
    private MyRecyclerAdapter_Board.OnItemClickListener mListener = null;

    //리스너 객체 참조를 어댑터에 전달 메서드
    public void setOnItemClickListener(MyRecyclerAdapter_Board.OnItemClickListener listener) {
        this.mListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();

        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclerview_home, parent, false);
            return new ItemViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclerview_board_loading, parent, false);
            return new MyRecyclerAdapter_Home.LoadingViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mHomeList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyRecyclerAdapter_Home.ItemViewHolder) {
            ((MyRecyclerAdapter_Home.ItemViewHolder) holder).onBind(mHomeList.get(position));
            ((ItemViewHolder) holder).viewPager.setId(position + 1);
        } else if (holder instanceof MyRecyclerAdapter_Home.LoadingViewHolder) {

        }
    }

    @Override
    public int getItemCount() {
        return mHomeList == null ? 0 : mHomeList.size();
    }

    public void setHomeList(ArrayList<HomeItem> mHomeList, int page) {
        this.mHomeList = mHomeList;
        notifyItemRangeChanged((page - 1) * 5, 5);
//        notifyDataSetChanged();
    }

    private class LoadingViewHolder extends RecyclerView.ViewHolder {
        private ProgressBar progressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);

            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView profile;
        ImageView likeButton, commentButton;
        TextView whatTypeTextView, idTextView, messageTextView, likeNum, commentNum, dateTextView, roomTextView;
        ViewPager viewPager;
        CircleIndicator circleIndicator;

        public ItemViewHolder(View itemView) {
            super(itemView);

            profile = itemView.findViewById(R.id.memberImageView);
            likeButton = itemView.findViewById(R.id.likeImageView);
            commentButton = itemView.findViewById(R.id.commentImageView);
            whatTypeTextView = itemView.findViewById(R.id.whatTypeTextView);
            idTextView = itemView.findViewById(R.id.memberIdTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            likeNum = itemView.findViewById(R.id.likeNumTextView);
            commentNum = itemView.findViewById(R.id.commentNumTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            viewPager = itemView.findViewById(R.id.homeImageViewPager);
            circleIndicator = itemView.findViewById(R.id.indicator);
            roomTextView = itemView.findViewById(R.id.roomTextView);


            itemView.setOnClickListener(new View.OnClickListener() {
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


        }

        public void onBind(HomeItem homeItem) {
            // 내방볼래 or 내일상볼래
            switch (homeItem.getWhatSelected()){
                case "room":
                    whatTypeTextView.setText("내방볼래?");
                    whatTypeTextView.setTextColor(ContextCompat.getColor(context, R.color.more_light_coral));
                    break;
                case "daily":
                    whatTypeTextView.setText("내일상볼래?");
                    whatTypeTextView.setTextColor(ContextCompat.getColor(context, R.color.more_light_blue));
                    break;
            }

            // 프사
            profile.setImageResource(0);
            storageRef = storage.getReference();
            profileRef = storageRef.child("Profile/" + homeItem.getThisFeedKakaoId() + ".png");
            profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    if(((Activity) context).isFinishing()) return;
                    Glide.with(profile.getContext()).load(uri).apply(RequestOptions.bitmapTransform(new RoundedCorners(14))).into(profile);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    profile.setImageResource(R.drawable.person);
                }
            });
            profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), FeedActivity.class);
                    intent.putExtra("kakaoId", homeItem.getThisFeedKakaoId());
                    v.getContext().startActivity(intent);
                }
            });

            // 아이디
            idTextView.setText(homeItem.getUserId());

            // 내용
            messageTextView.setText(homeItem.getMessage());

            // 좋아요 개수
            likeNum.setText(homeItem.getLikeNum());

            // 댓글 개수
            commentNum.setText(homeItem.getCommentNum());

            // 날짜
            dateTextView.setText(formatTimeString(homeItem.getDate()));

//            // 좋아요 버튼
//            likeList = new ArrayList<>();
//            likeList = homeItem.getLikeList();
//
//            UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
//                @Override
//                public Unit invoke(User user, Throwable throwable) {
//                    kakaoId = String.valueOf(user.getId());
//                    if(likeList.contains(kakaoId)){
//                        likeButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_favorite_24));
//                        isLike = true;
//                    }else{
//                        likeButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_favorite_border_24));
//                        isLike = false;
//                    }
//                    return null;
//                }
//            });
//
//            like = Integer.parseInt(homeItem.getLikeNum());
//
//            likeButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if(isLike){
//                        isLike = false;
//                        likeList.add(kakaoId);
//                        like = like + 1;
//                    }else{
//                        isLike = true;
//                        likeList.remove(kakaoId);
//                        like = like - 1;
//                    }
//                    db.collection("homePosts").document(homeItem.getThisFeedKakaoId())
//                            .update("likeNum", like, "likeList", likeList);
//                    likeNum.setText(String.valueOf(like));
//                }
//            });

            // 뷰페이저
            MyPagerAdapter_Home pagerAdapter = new MyPagerAdapter_Home(context);
            pagerAdapter.setHomeItem(homeItem);
            pagerAdapter.setPostImage(homeItem.getPostUriList());
            viewPager.setAdapter(pagerAdapter);
            circleIndicator.setViewPager(viewPager);


            // 방 정보
            if (homeItem.getWhatSelected().equals("daily")) {
                roomTextView.setVisibility(View.GONE);
            } else {
                String roomInfo = "";
                String py = "", dwell = "", style = "";
                py = homeItem.getPy();
                dwell = homeItem.getDwell();
                style = homeItem.getStyle();
                if(py.equals("null")) py = "";
                if(dwell.equals("null")) dwell = "";
                if(style.equals("null")) style = "";

                String between = " | ";
                if(!py.equals("")) py = py+between;
                if(!dwell.equals("")) dwell = dwell + between;
                if (py.equals("") && dwell.equals("") && style.equals("")) {
                    roomTextView.setVisibility(View.GONE);
                } else {
                    roomInfo = py + dwell + style;
                    if (roomInfo.substring(roomInfo.length() - 1).equals(" ")) {
                        roomInfo = roomInfo.substring(0, roomInfo.length() - 3);
                    }
                    roomTextView.setText(roomInfo);
                }
            }
        }

        public class MyPagerAdapter_Home extends PagerAdapter {
            private ArrayList<Uri> uriList = new ArrayList<>();
            private HomeItem homeItem;
            private Context context;

            public MyPagerAdapter_Home(Context context) {
                this.context = context;
            }

            public void setHomeItem(HomeItem homeItem) {
                this.homeItem = homeItem;
            }

            @Override
            public int getCount() {
                return uriList.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == (View) object;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView((View) object);
            }

            public void setPostImage(ArrayList<Uri> postImagesUri) {
                uriList = postImagesUri;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.viewpager_postimage, container, false);

                ImageView imageView = view.findViewById(R.id.postImage);
                Glide.with(context).load(uriList.get(position)).apply(RequestOptions.bitmapTransform(new RoundedCorners(30))).into(imageView);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, FeedPostActivity.class);
                        intent.putExtra("postRef", "Post/" + homeItem.getThisFeedKakaoId() + "/" + homeItem.getDate() + "/");
                        intent.putExtra("postId", homeItem.getId());
                        intent.putExtra("thisFeedKakaoId", homeItem.getThisFeedKakaoId());
                        context.startActivity(intent);
                    }
                });

                container.addView(view);

                return view;
            }
        }
    }

    // 시간 단위 나누기
    private String formatTimeString(String date) {
        SimpleDateFormat monthDateFormat = new SimpleDateFormat("MM월 dd일");
        SimpleDateFormat yearDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");

        long regTime = Long.parseLong(date);
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
