package com.example.showmyroom.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.showmyroom.activity.FeedActivity;
import com.example.showmyroom.items.MemberItem;
import com.example.showmyroom.adapter.MyRecyclerAdapter_SearchMember;
import com.example.showmyroom.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";

    RecyclerView recyclerView;
    private LinearLayout progressBarLayout;
    private EditText idSearchEditText;
    private ImageButton searchButton;
    private MyRecyclerAdapter_SearchMember myRecyclerAdapter;
    private ArrayList<MemberItem> memberItems = new ArrayList<>(), filteredList = new ArrayList<>();

    private DatabaseReference mDatabase;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private Uri img;
    private StorageReference storageRef, pathRef;

    private String kakaoId, userId;
//    private ArrayList<String> kakaoId, userId;
    private static final int GET_IMG = 0, ADAPT_VIEW = 1, DELETE_LOADING = 2;

    //키보드
    private InputMethodManager imm;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch(msg.what) {
//                case GET_IMG:
//                    memberItems = new ArrayList<>();
//                    for(int i = 0; i < kakaoId.size(); i++){
//                        String thisKakaoId = kakaoId.get(i);
//                        String thisUserId = userId.get(i);
//                        StorageReference storageRef = storage.getReference();
//                        StorageReference pathRef = storageRef.child("Profile/"+thisKakaoId+".png");
//                        if(pathRef == null){
//                            MemberItem item = new MemberItem(
//                                    thisKakaoId,
//                                    thisUserId,
//                                    null
//                            );
//                            memberItems.add(item);
//                        }else{
//                            pathRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                @Override
//                                public void onSuccess(Uri uri) {
//                                    MemberItem item = new MemberItem(
//                                            thisKakaoId,
//                                            thisUserId,
//                                            uri
//                                    );
//                                    memberItems.add(item);
//                                }
//                            }).addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//
//                                }
//                            });
//                        }
//                        if(i == kakaoId.size()-1){
//                            handler.sendEmptyMessage(ADAPT_VIEW);
//                            Log.d(TAG, "2");
//                            Log.d(TAG, String.valueOf(memberItems.size()));
//                        }
//                    }

                case ADAPT_VIEW:
                    // 프래그먼트 첫 호출
                    Log.d(TAG, "1");
                    Log.d(TAG, String.valueOf(memberItems.size()));
                    recyclerView.setAdapter(myRecyclerAdapter);
                    LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                    myRecyclerAdapter.setMemberItems(memberItems);
                    recyclerView.setLayoutManager(mLayoutManager);
                    handler.sendEmptyMessage(DELETE_LOADING);
                    break;
                case DELETE_LOADING:
                    progressBarLayout.setVisibility(View.GONE);
                    break;
            }
        }
    };

    public SearchFragment() {
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
        View v = inflater.inflate(R.layout.fragment_search, container, false);

        progressBarLayout = v.findViewById(R.id.progressBarLayout);


        recyclerView = v.findViewById(R.id.recyclerView_memberSearch);
        idSearchEditText = v.findViewById(R.id.idSearchEditText);

        myRecyclerAdapter = new MyRecyclerAdapter_SearchMember();

        adaptMember();

        myRecyclerAdapter.setOnItemClickListener(new MyRecyclerAdapter_SearchMember.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                if(idSearchEditText.getText().toString().length() != 0){
                    Intent intent = new Intent(getActivity(), FeedActivity.class);
                    intent.putExtra("kakaoId", filteredList.get(position).getKakaoId());
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(getActivity(), FeedActivity.class);
                    intent.putExtra("kakaoId", memberItems.get(position).getKakaoId());
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }

            }
        });

        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        idSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if(actionId == EditorInfo.IME_ACTION_SEARCH){ // IME_ACTION_DONE , IME_ACTION_GO
                    progressBarLayout.setVisibility(View.VISIBLE);
                    String searchId = idSearchEditText.getText().toString();
                    searchFilter(searchId);
                    idSearchEditText.clearFocus();
                    imm.hideSoftInputFromWindow(idSearchEditText.getWindowToken(), 0);
                }
                return false;
            }
        });

        searchButton = v.findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBarLayout.setVisibility(View.VISIBLE);
                String searchId = idSearchEditText.getText().toString();
                searchFilter(searchId);
                idSearchEditText.clearFocus();
                imm.hideSoftInputFromWindow(idSearchEditText.getWindowToken(), 0);
            }
        });

//        idSearchEditText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                String searchId = idSearchEditText.getText().toString();
//                searchFilter(searchId);
//            }
//        });


        return v;


    }

    private void searchFilter(String searchId) {
        filteredList.clear();


        for (int i = 0; i < memberItems.size(); i++) {
            if (memberItems.get(i).getUserId().toLowerCase().contains(searchId.toLowerCase())) {
                filteredList.add(memberItems.get(i));
            }
        }
        myRecyclerAdapter.setMemberItems(filteredList);
        Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                handler.sendEmptyMessage(DELETE_LOADING);
            }
        },500);

    }



    private void adaptMember() {
        mDatabase = FirebaseDatabase.getInstance().getReference();


        memberItems = new ArrayList<>();
        mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Map memberItem = new HashMap();
                    memberItem = (Map) dataSnapshot.getValue();
                    Log.d(TAG, memberItem.toString());
                    MemberItem item = new MemberItem(
                            memberItem.get("kakaoId").toString(),
                            userId = memberItem.get("id").toString()
                    );
                    memberItems.add(item);

                }
                handler.sendEmptyMessage(ADAPT_VIEW);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}