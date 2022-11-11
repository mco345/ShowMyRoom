package com.example.showmyroom.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.showmyroom.Notification;
import com.example.showmyroom.R;
import com.example.showmyroom.adapter.MyPagerAdapter_Post;
import com.example.showmyroom.adapter.MyRecyclerAdapter_Comment;
import com.example.showmyroom.items.CommentItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import me.relex.circleindicator.CircleIndicator;

public class FeedPostActivity extends AppCompatActivity {
    private static final String TAG = "FeedPostActivity";

    private TextView feedPostIdTextView, contentTextView, likeNumTextView;
    private ImageView feedPostProfileImageView;
    private View likeButton, nolikeButton, commentButton;
    private EditText commentEditText;
    private ChipGroup chipGroup;

    private String postId, thisFeedKakaoId, userId, postRefText;
    private ArrayList<Uri> postUriList;
    private int innerPos, count = 0;

    private DatabaseReference mDatabase;

    // Notification
    private Notification notification = new Notification();

    // whatType
    private String whatType;
    private TextView whatTypeTextView;

    // 초기화면 progressbar
    private LinearLayout progressBarLayout;

    // 수정
    private ImageButton updateButton;

    /// 프사
    private Uri profileImage;

    // 이미지 뷰페이저
    private ViewPager pager;
    private MyPagerAdapter_Post pagerAdapter;
    private CircleIndicator circleIndicator;

    // 파이어스토어
    private FirebaseFirestore db;

    // 키워드
    private ArrayList<String> keywordsList;

    // 좋아요
    private int likeNum;
    private boolean isLike = false;
    private ArrayList<String> likeList;

    // 답글
    private LinearLayout replyLayout;
    private TextView replyTextView;
    private View replyButton;
    private Boolean isReply = false;
    private Integer replyPosition, realPosition;
    private ArrayList<CommentItem> comments;
    private String realKakaoId;
    // 댓글 작성
    private RecyclerView commentRecyclerView;
    private MyRecyclerAdapter_Comment myRecyclerAdapter;
    String comment_kakaoId, comment_userId;
    private String board; // 어떤 게시판인지
    private CommentItem item, reply;
    private ArrayList<CommentItem> commentItems;
    private int commentNum;

    // 스토리지
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef, profileRef, postRef;
    private String content;

    //키보드
    private InputMethodManager imm;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 0:
                    postUriList = new ArrayList<>();
                    postRef = storageRef.child(postRefText);
                    postRef.listAll()
                            .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                                @Override
                                public void onSuccess(ListResult listResult) {
                                    for (int i = 0; i < listResult.getItems().size(); i++) {
                                        postUriList.add(Uri.parse(""));
                                    }
                                    Log.d(TAG, "size : " + postUriList.size());
                                    for (StorageReference item : listResult.getItems()) {
                                        // reference의 item(이미지) url 받아오기
                                        Log.d(TAG, "item : " + item);
                                        item.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Uri> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, String.valueOf(listResult.getItems().size()) + "  " + postRef.getPath());
                                                    innerPos = Integer.parseInt(item.toString().substring(item.toString().length() - 5, item.toString().length() - 4));
                                                    Log.d(TAG, "innerPos : " + innerPos + ", size : " + postUriList.size());
                                                    postUriList.set(innerPos, task.getResult());
                                                    count++;
                                                    if (count == listResult.getItems().size())
                                                        sendEmptyMessage(1);
                                                } else {
                                                    Log.d(TAG, "download fail");
                                                }
                                            }
                                        });
                                    }
                                }
                            });

                    break;

                case 1:
                    pager = findViewById(R.id.feedPostImageViewPager);
                    pagerAdapter = new MyPagerAdapter_Post(FeedPostActivity.this);
                    pagerAdapter.setPostImage(postUriList);
                    pager.setAdapter(pagerAdapter);

                    circleIndicator = findViewById(R.id.indicator);
                    circleIndicator.setViewPager(pager);

                    progressBarLayout.setVisibility(View.GONE);
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_post);

        Intent feedPostIntent = getIntent();
        postRefText = feedPostIntent.getStringExtra("postRef");
        postId = feedPostIntent.getStringExtra("postId");
        thisFeedKakaoId = feedPostIntent.getStringExtra("thisFeedKakaoId");

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        progressBarLayout = findViewById(R.id.progressBarLayout);
        progressBarLayout.setVisibility(View.VISIBLE);

        // 게시물 게시자 아이디
        feedPostIdTextView = findViewById(R.id.feedPostIdTextView);

        // 게시물 게시자 프사
        storageRef = storage.getReference();
        profileRef = storageRef.child("Profile/" + thisFeedKakaoId + ".png");
        feedPostProfileImageView = findViewById(R.id.feedPostProfileImageView);
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if (getApplicationContext() != null) {
                    profileImage = uri;
                    Glide.with(getApplicationContext()).load(uri).apply(RequestOptions.bitmapTransform(new RoundedCorners(30))).into(feedPostProfileImageView);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                feedPostProfileImageView.setImageResource(R.drawable.ic_baseline_person_24);
            }
        });
        feedPostProfileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
                intent.putExtra("kakaoId", thisFeedKakaoId);
                startActivity(intent);
            }
        });

        handler.sendEmptyMessage(0);

        // 좋아요 개수
        likeNumTextView = findViewById(R.id.likeNumTextView);
        // 게시물 내용
        contentTextView = findViewById(R.id.contentTextView);
        // 댓글
        commentRecyclerView = findViewById(R.id.recyclerView_comment);
        myRecyclerAdapter = new MyRecyclerAdapter_Comment();

        db = FirebaseFirestore.getInstance();
        getDataFromFireStore(postId);

        // 좋아요
        likeButton = findViewById(R.id.likeButton);
        nolikeButton = findViewById(R.id.noLikeButton);
        // 좋아요 안눌러진 상태
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
                    @Override
                    public Unit invoke(User user, Throwable throwable) {
                        likeList.add(String.valueOf(user.getId()));
                        likeNum = likeNum + 1;
                        db.collection("homePosts").document(postId)
                                .update("likeNum", likeNum, "likeList", likeList);
                        likeNumTextView.setText(String.valueOf(likeNum));
                        likeButton.setVisibility(View.GONE);
                        nolikeButton.setVisibility(View.VISIBLE);
                        notification.notice_like("피드", postId, thisFeedKakaoId, comment_kakaoId);
                        return null;
                    }
                });


            }
        });
        // 좋아요 놀러진 상태
        nolikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
                    @Override
                    public Unit invoke(User user, Throwable throwable) {
                        likeList.remove(String.valueOf(user.getId()));
                        likeNum = likeNum - 1;
                        db.collection("homePosts").document(postId)
                                .update("likeNum", likeNum, "likeList", likeList);
                        likeNumTextView.setText(String.valueOf(likeNum));
                        likeButton.setVisibility(View.VISIBLE);
                        nolikeButton.setVisibility(View.GONE);
                        notification.notice_noLike(postId, thisFeedKakaoId, comment_kakaoId);
                        return null;
                    }
                });
            }
        });

        // 키워드
        chipGroup = findViewById(R.id.feedPostChipGroup);



        commentRecyclerView.setLayoutManager(new LinearLayoutManager(FeedPostActivity.this));

        // 댓글 등록하는 사용자 kakaoId, userId 정보 가져오기
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                comment_kakaoId = String.valueOf(user.getId());
                // update 가능여부
                if (comment_kakaoId.equals(thisFeedKakaoId)) {
                    updateButton.setVisibility(View.VISIBLE);
                }

                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                mDatabase.child("users").child(comment_kakaoId).child("id").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        comment_userId = String.valueOf(task.getResult().getValue());

                    }
                });

                return null;
            }
        });

        // 댓글 등록
        commentButton = findViewById(R.id.commentButton);
        commentEditText = findViewById(R.id.commentEditText);
        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String comment_date = sdf.format(new Date(System.currentTimeMillis()));

                String comment = commentEditText.getText().toString();

                // 답글 등록
                if (isReply) {
                    reply = new CommentItem(thisFeedKakaoId, comment_kakaoId, comment_userId, comment, comment_date, true, false, realKakaoId);
                    ArrayList<CommentItem> replyList;
                    Map commentItem = new HashMap();
                    commentItem = (Map) commentItems.get(realPosition);
                    replyList = (ArrayList<CommentItem>) commentItem.get("replyList");
                    replyList.add(reply);
                    Log.d(TAG, String.valueOf(replyList));
                    commentItems.set(realPosition, new CommentItem(
                            String.valueOf(commentItem.get("thisPostKakaoId")),
                            String.valueOf(commentItem.get("kakaoId")),
                            String.valueOf(commentItem.get("userId")),
                            String.valueOf(commentItem.get("comment")),
                            String.valueOf(commentItem.get("date")),
                            true,
                            false,
                            replyList,
                            false
                    ));

                }
                // 일반 댓글 등록
                else {
                    item = new CommentItem(thisFeedKakaoId, comment_kakaoId, comment_userId, comment, comment_date, false, false, new ArrayList<CommentItem>(), false);
                    Log.d(TAG, "item : " + item.getComment());
                    commentItems.add(item);
                }
                AddComment();

                // 알림 데이터베이스 추가
                if (isReply)
                    notification.notice_comment("피드", postId, thisFeedKakaoId, comment_kakaoId, realKakaoId, comment, "reply", comment_date);
                else
                    notification.notice_comment("피드", postId, thisFeedKakaoId, comment_kakaoId, "", comment, "comment", comment_date);

                imm.hideSoftInputFromWindow(commentEditText.getWindowToken(), 0);
                Intent intent = new Intent(getApplicationContext(), FeedPostActivity.class);
                intent.putExtra("postId", postId);
                intent.putExtra("postRef", postRefText);
                intent.putExtra("thisFeedKakaoId", thisFeedKakaoId);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        updateButton = findViewById(R.id.updateButton);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "update - " + profileImage);
                Intent intent = new Intent(getApplicationContext(), FeedPostUpdateActivity.class);
                intent.putExtra("postId", postId);
                intent.putExtra("postRef", postRefText);
                intent.putExtra("thisFeedKakaoId", thisFeedKakaoId);
                intent.putExtra("thisPostUriList", postUriList);
                intent.putExtra("content", content);
                intent.putExtra("keywordsList", keywordsList);
                startActivity(intent);
            }
        });


    }

    private void AddComment() {
        db.collection("homePosts").document(postId)
                .update("comment", commentItems, "commentNum", comments.size() + 1);
        Toast.makeText(getApplicationContext(), "댓글을 등록하였습니다.", Toast.LENGTH_SHORT).show();
    }

    private void getDataFromFireStore(String postId) {
        db.collection("homePosts").document(postId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            content = document.getData().get("message").toString();
                            userId = document.getData().get("id").toString();
                            contentTextView.setText(content);
                            feedPostIdTextView.setText(userId);

                            // whatType
                            whatType = document.getData().get("whatSelected").toString();
                            Log.d(TAG, "whatType - "+whatType);
                            // 내방볼래 or 내일상볼래
                            whatTypeTextView = findViewById(R.id.whatTypeTextView);
                            switch (whatType){
                                case "room":
                                    whatTypeTextView.setText("내방볼래?");
                                    whatTypeTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.more_light_coral));
                                    break;
                                case "daily":
                                    whatTypeTextView.setText("내일상볼래?");
                                    whatTypeTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.more_light_blue));
                                    break;
                            }

                            // 키워드 관련
                            keywordsList = new ArrayList<>();
                            keywordsList = (ArrayList<String>) document.getData().get("keyword");
                            try {
                                LayoutInflater inflater = LayoutInflater.from(FeedPostActivity.this);
                                Log.d(TAG, "keywordsList - "+keywordsList);
                                if (keywordsList.size() == 0) chipGroup.setVisibility(View.GONE);
                                for(int i =0; i<keywordsList.size(); i++){
                                    Log.d(TAG, "keywordsList size - "+keywordsList.size());
                                    Log.d(TAG, i + "-"+keywordsList.get(i));
                                    Chip newChip = (Chip) inflater.inflate(R.layout.view_chip_feedpost, chipGroup, false);
                                    newChip.setText(keywordsList.get(i));
                                    chipGroup.addView(newChip);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            // 좋아요 관련
                            likeList = new ArrayList<>();
                            likeList = (ArrayList<String>) document.getData().get("likeList");
                            likeNum = Integer.parseInt(document.getData().get("likeNum").toString());
                            likeNumTextView.setText(String.valueOf(likeNum));
                            Log.d(TAG, "likeList : " + likeList + ", kakaoId : " + comment_kakaoId);
                            if (likeList.contains(comment_kakaoId)) {
                                Log.d(TAG, "isLike");
                                isLike = true;
                                nolikeButton.setVisibility(View.VISIBLE);
                            } else {
                                isLike = false;
                                likeButton.setVisibility(View.VISIBLE);
                            }


                            // 댓글 관련
                            commentItems = new ArrayList<>();
                            commentItems = (ArrayList<CommentItem>) document.getData().get("comment");
                            comments = new ArrayList<>();
                            for (int i = 0; i < commentItems.size(); i++) {
                                Map commentItem = new HashMap();
                                commentItem = (Map) commentItems.get(i);
                                comments.add(new CommentItem(
                                        String.valueOf(commentItem.get("thisPostKakaoId")),
                                        String.valueOf(commentItem.get("kakaoId")),
                                        String.valueOf(commentItem.get("userId")),
                                        String.valueOf(commentItem.get("comment")),
                                        String.valueOf(commentItem.get("date")),
                                        (Boolean) commentItem.get("reply"),
                                        (Boolean) commentItem.get("secret"),
                                        String.valueOf(commentItem.get("realKakaoId"))
                                ));
                                if ((Boolean) commentItem.get("mode")) {
                                    Log.d(TAG, "comments.get(i).getDate() : " + commentItems.get(i));
                                    ArrayList<CommentItem> replyList = (ArrayList<CommentItem>) commentItem.get("replyList");
                                    for (int j = 0; j < replyList.size(); j++) {
                                        Map replyItem = new HashMap();
                                        replyItem = (Map) replyList.get(j);
                                        comments.add(new CommentItem(
                                                String.valueOf(replyItem.get("thisPostKakaoId")),
                                                String.valueOf(replyItem.get("kakaoId")),
                                                String.valueOf(replyItem.get("userId")),
                                                String.valueOf(replyItem.get("comment")),
                                                String.valueOf(replyItem.get("date")),
                                                (Boolean) replyItem.get("reply"),
                                                (Boolean) replyItem.get("secret"),
                                                String.valueOf(replyItem.get("realKakaoId"))
                                        ));
                                        Log.d(TAG, "replyItem : " + replyItem);

                                    }
                                }
                            }

                            // 댓글 수 갱신
                            commentNum = comments.size();

                            // 댓글 갱신
                            commentRecyclerView.setAdapter(myRecyclerAdapter);

                            myRecyclerAdapter.setmCommentList(comments);


                            // 댓글 개수 갱신
                            db.collection("homePosts").document(postId)
                                    .update("commentNum", String.valueOf(myRecyclerAdapter.getItemCount()));

                            // 댓글 클릭 이벤트
                            myRecyclerAdapter.setOnItemClickListener(new MyRecyclerAdapter_Comment.OnItemClickListener() {
                                // 대댓글 달기
                                @Override
                                public void onItemClick(View v, int position) {
                                    realPosition = 0;
                                    replyLayout = findViewById(R.id.replyLayout);
                                    replyButton = findViewById(R.id.replyButton);
                                    replyTextView = findViewById(R.id.replyTextView);
                                    // 해당 position "전까지" 일반 댓글의 갯수
                                    // 즉, 첫 번째 댓글은 0
                                    for (int i = 0; i < position; i++) {
                                        if (!comments.get(i).getReply()) {
                                            realPosition++;
                                        }
                                    }
//                                    if(comments.get(position).getKakaoId().equals("")){
//                                        return;
//                                    }
                                    // 대댓글이 아닌 댓글만 클릭 가능
                                    if (!comments.get(position).reply) {
                                        realKakaoId = comments.get(position).getKakaoId();
                                        replyLayout.setVisibility(View.VISIBLE);
                                        Map commentItem = new HashMap();
                                        commentItem = (Map) commentItems.get(realPosition);
                                        if (commentItem.get("userId").equals("(삭제)")) {
                                            replyTextView.setText(String.valueOf("삭제된 댓글에 답글 남기는 중"));
                                        } else {
                                            if (commentItem.get("kakaoId").equals(commentItem.get("thisPostKakaoId"))) {
                                                replyTextView.setText(String.valueOf(commentItem.get("userId")) + "(작성자)에게 답글 남기는 중");
                                            } else {
                                                replyTextView.setText(String.valueOf(commentItem.get("userId")) + "에게 답글 남기는 중");
                                            }

                                        }

                                        // EditText 포커스 주기
                                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                                        commentEditText.requestFocus();
                                        isReply = true;
                                        replyButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                replyLayout.setVisibility(View.GONE);
                                                // EditText 포커스 제거
                                                commentEditText.clearFocus();
                                                imm.hideSoftInputFromWindow(commentEditText.getWindowToken(), 0);
                                                isReply = false;
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onDeleteClick(View v, int position) {
                                    realPosition = 0;
                                    replyPosition = 0;

                                    // 해당 position "전까지" 일반 댓글의 갯수
                                    // 즉, 첫 번째 댓글은 0
                                    for (int i = 0; i < position; i++) {
                                        if (!comments.get(i).getReply()) {
                                            realPosition++;
                                            replyPosition = 0;
                                        } else {
                                            replyPosition++;
                                        }
                                    }
                                    AlertDialog.Builder alert = new AlertDialog.Builder(FeedPostActivity.this);
                                    alert.setTitle("삭제한 댓글은 복구할 수 없습니다.\n정말 삭제하시겠습니까?");
                                    alert.setPositiveButton("네", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // 삭제할 댓글이 답글이라면
                                            if (comments.get(position).getReply()) {
                                                --realPosition; // 답글이므로 갯수 1개 줄이기(답글일 경우 "전까지"이면 1개가 더해지므로)
                                                ArrayList<CommentItem> replyList;
                                                Map commentItem = new HashMap();
                                                commentItem = (Map) commentItems.get(realPosition);
                                                replyList = (ArrayList<CommentItem>) commentItem.get("replyList");
                                                Map replyItem = new HashMap();
                                                replyItem = (Map) replyList.get(replyPosition);
                                                Log.d(TAG, "before delete : " + replyList.size() + "replyPosition : " + replyPosition);
                                                notification.notice_delete_comment(postId, replyItem.get("realKakaoId").toString(), comment_kakaoId, replyItem.get("date").toString());
                                                replyList.remove(0 + replyPosition);
                                                Log.d(TAG, "After delete : " + replyList.size());
                                                commentItems.set(realPosition, new CommentItem(
                                                        String.valueOf(commentItem.get("thisPostKakaoId")),
                                                        String.valueOf(commentItem.get("kakaoId")),
                                                        String.valueOf(commentItem.get("userId")),
                                                        String.valueOf(commentItem.get("comment")),
                                                        String.valueOf(commentItem.get("date")),
                                                        replyList.size() == 0 ? false : true,
                                                        false,
                                                        replyList,
                                                        (Boolean) commentItem.get("secret")
                                                ));


                                            }
                                            // 삭제할 댓글이 답글이 아니라면
                                            else {
                                                Map commentItem = new HashMap();
                                                commentItem = (Map) commentItems.get(realPosition);
                                                Log.d(TAG, "comment_date - " + commentItem.get("date").toString());
                                                notification.notice_delete_comment(postId, commentItem.get("thisPostKakaoId").toString(), comment_kakaoId, commentItem.get("date").toString());
                                                commentItems.set(realPosition, new CommentItem(
                                                        String.valueOf(commentItem.get("thisPostKakaoId")),
                                                        "",
                                                        "(삭제)",
                                                        "삭제된 댓글입니다.",
                                                        "",
                                                        (Boolean) commentItem.get("mode"),
                                                        (Boolean) commentItem.get("reply"),
                                                        (ArrayList<CommentItem>) commentItem.get("replyList"),
                                                        (Boolean) commentItem.get("secret")
                                                ));
                                            }


                                            db.collection("homePosts").document(postId)
                                                    .update("comment", commentItems);
                                            Intent intent = new Intent(getApplicationContext(), FeedPostActivity.class);
                                            intent.putExtra("postId", postId);
                                            intent.putExtra("postRef", postRefText);
                                            intent.putExtra("thisFeedKakaoId", thisFeedKakaoId);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                        }
                                    }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });

                                    AlertDialog alertDialog = alert.create();
                                    alertDialog.show();
                                }
                            });

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
}