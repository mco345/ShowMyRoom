package com.example.showmyroom.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.showmyroom.R;
import com.example.showmyroom.activity.FeedActivity;
import com.example.showmyroom.activity.FeedPostActivity;
import com.example.showmyroom.items.BoardItem;
import com.example.showmyroom.items.HomeItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import java.util.ArrayList;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import me.relex.circleindicator.CircleIndicator;

public class MyRecyclerAdapter_Home extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "homeAdapter";

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef, profileRef, postRef;

    private String kakaoId, thisFeedKakaoId, date;

    private boolean isLike = false;
    private int like;

    // 파이어스토어
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ArrayList<String> likeList;

    private ArrayList<HomeItem> mHomeList;

    private Context context = null;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 0:


                    break;

            }
        }
    };

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();

        if(viewType == VIEW_TYPE_ITEM){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclerview_home, parent, false);
            return new ItemViewHolder(view);
        }else{
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
        if(holder instanceof MyRecyclerAdapter_Home.ItemViewHolder){
            ((MyRecyclerAdapter_Home.ItemViewHolder) holder).onBind(mHomeList.get(position));
            ((ItemViewHolder) holder).viewPager.setId(position+1);
        }else if(holder instanceof MyRecyclerAdapter_Home.LoadingViewHolder){

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

    private class LoadingViewHolder extends RecyclerView.ViewHolder{
        private ProgressBar progressBar;
        public LoadingViewHolder(View itemView) {
            super(itemView);

            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView profile;
        ImageButton likeButton, commentButton;
        TextView idTextView, likeNum, commentNum, dateTextView;
        ViewPager viewPager;
        CircleIndicator circleIndicator;

        public ItemViewHolder(View itemView) {
            super(itemView);

            profile = itemView.findViewById(R.id.memberImageView);
            likeButton = itemView.findViewById(R.id.likeImageView);
            commentButton = itemView.findViewById(R.id.commentImageView);
            idTextView = itemView.findViewById(R.id.memberIdTextView);
            likeNum = itemView.findViewById(R.id.likeNumTextView);
            commentNum = itemView.findViewById(R.id.commentNumTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            viewPager = itemView.findViewById(R.id.homeImageViewPager);
            circleIndicator = itemView.findViewById(R.id.indicator);


        }

        public void onBind(HomeItem homeItem) {
            // 프사
            profile.setImageResource(0);
            storageRef = storage.getReference();
            profileRef = storageRef.child("Profile/" + homeItem.getThisFeedKakaoId() + ".png");
            profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(profile.getContext()).load(uri).apply(RequestOptions.bitmapTransform(new RoundedCorners(14))).into(profile);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    profile.setImageResource(R.drawable.ic_baseline_person_24);
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

            // 좋아요 개수
            likeNum.setText(homeItem.getLikeNum());

            // 댓글 개수
            commentNum.setText(homeItem.getCommentNum());

            // 좋아요 버튼
            likeList = new ArrayList<>();
            likeList = homeItem.getLikeList();

            UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
                @Override
                public Unit invoke(User user, Throwable throwable) {
                    kakaoId = String.valueOf(user.getId());
                    if(likeList.contains(kakaoId)){
                        likeButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_favorite_24));
                        isLike = true;
                    }else{
                        likeButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_favorite_border_24));
                        isLike = false;
                    }
                    return null;
                }
            });

            like = Integer.parseInt(homeItem.getLikeNum());

            likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isLike){
                        isLike = false;
                        likeList.add(kakaoId);
                        like = like + 1;
                    }else{
                        isLike = true;
                        likeList.remove(kakaoId);
                        like = like - 1;
                    }
                    db.collection("homePosts").document(homeItem.getThisFeedKakaoId())
                            .update("likeNum", like, "likeList", likeList);
                    likeNum.setText(String.valueOf(like));
                }
            });

            // 뷰페이저
            MyPagerAdapter_Home pagerAdapter = new MyPagerAdapter_Home(context);
            pagerAdapter.setPostImage(homeItem.getPostUriList());
            viewPager.setAdapter(pagerAdapter);
            circleIndicator.setViewPager(viewPager);
        }

        public class MyPagerAdapter_Home extends PagerAdapter {
            private ArrayList<Uri> uriList = new ArrayList<>();
            private Context context;

            public MyPagerAdapter_Home(Context context) {
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
    }


}
