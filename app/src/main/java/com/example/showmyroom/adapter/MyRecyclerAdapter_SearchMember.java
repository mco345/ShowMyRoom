package com.example.showmyroom.adapter;

import android.app.Activity;
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
import com.example.showmyroom.fragment.SearchFragment;
import com.example.showmyroom.items.MemberItem;
import com.example.showmyroom.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class MyRecyclerAdapter_SearchMember extends RecyclerView.Adapter<MyRecyclerAdapter_SearchMember.ViewHolder> {

    private ArrayList<MemberItem> memberItems;

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef, pathRef;


    //아이템 클릭 리스너 인터페이스
    public interface OnItemClickListener{
        void onItemClick(View v, int position); //뷰와 포지션값
    }
    //리스너 객체 참조 변수
    private MyRecyclerAdapter_SearchMember.OnItemClickListener mListener = null;
    //리스너 객체 참조를 어댑터에 전달 메서드
    public void setOnItemClickListener(MyRecyclerAdapter_SearchMember.OnItemClickListener listener) {
        this.mListener = listener;
    }

    public MyRecyclerAdapter_SearchMember() {
    }

    @NonNull
    @Override
    public MyRecyclerAdapter_SearchMember.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclerview_searchmember, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyRecyclerAdapter_SearchMember.ViewHolder holder, int position) {
        holder.onBind(memberItems.get(position));
    }

    @Override
    public int getItemCount() {
        return memberItems.size();
    }
    public void setMemberItems(ArrayList<MemberItem> memberItems) {
        this.memberItems = memberItems;
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView memberImageView;
        TextView memberIdTextView, memberNameTextView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            memberImageView = itemView.findViewById(R.id.memberImageView);
            memberIdTextView = itemView.findViewById(R.id.memberIdTextView);
            memberNameTextView = itemView.findViewById(R.id.memberNameTextView);



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

        public void onBind(MemberItem memberItems) {
            Log.d("SearchMember", memberItems.getUserId()+memberItems.getKakaoId());
            memberImageView.setImageResource(0);
            memberIdTextView.setText("");
            memberNameTextView.setText("");

            storageRef = storage.getReference();
            pathRef = storageRef.child("Profile/" + memberItems.getKakaoId() + ".png");
            pathRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Activity activity = (Activity) memberImageView.getContext();
                    if(activity.isFinishing()) return;
                    Glide.with(memberImageView.getContext()).load(uri).apply(RequestOptions.bitmapTransform(new RoundedCorners(14))).into(memberImageView);
                    memberIdTextView.setText(memberItems.getUserId());
                    memberNameTextView.setText(memberItems.getName());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    memberImageView.setImageResource(R.drawable.person);
                    memberIdTextView.setText(memberItems.getUserId());
                    memberNameTextView.setText(memberItems.getName());
                }
            });


//            Glide.with(memberImageView.getContext()).load(memberItems.getImg()).centerCrop().into(memberImageView);


        }
    }
}
