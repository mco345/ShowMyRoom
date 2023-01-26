package com.example.showmyroom.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.showmyroom.Notification;
import com.example.showmyroom.TimeMaximum;
import com.example.showmyroom.items.CommentItem;
import com.example.showmyroom.adapter.MyRecyclerAdapter_Comment;
import com.example.showmyroom.R;
import com.example.showmyroom.items.SecretMemberItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import org.w3c.dom.Comment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class PostActivity extends AppCompatActivity {
    private static final String TAG = "PostActivity";

    private TextView idTextView, dateTextView, titleTextView, messageTextView, commentNumTextView;
    private View toEditButton, commentButton;
    private EditText commentEditText;

    // 답글
    private LinearLayout replyLayout;
    private TextView replyTextView;
    private View replyButton;
    private Boolean isReply = false;
    private Integer replyPosition, realPosition;
    private ArrayList<CommentItem> comments;
    private Boolean isReplySecret = false;
    private String realKakaoId;

    // Notification
    private Notification notification = new Notification();

    private RecyclerView commentRecyclerView;
    private MyRecyclerAdapter_Comment myRecyclerAdapter;

    // 댓글 작성
    String comment_kakaoId, comment_userId;
    private String id, kakaoId, thisPostKakaoId, title, message, userId, date;
    private String board; // 어떤 게시판인지
    private CommentItem item, reply;
    private ArrayList<CommentItem> commentItems;
    private int boardNum, commentNum;
    // 비밀게시판
    private ArrayList<SecretMemberItem> secretMemberItems, secretMemberItem;
    private int secretCommentMemberNum = 0;

    // 작성자만 보이기(비밀게시판)
    private LinearLayout isSecretLayout;
    private ImageView isSecretImage;
    private TextView isSecretTextView;
    private boolean isSecret = false;


    //키보드
    private InputMethodManager imm;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);


        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);


        db = FirebaseFirestore.getInstance();

        // MenuFragment로부터 data 받기
        Intent postIntent = getIntent();
        id = postIntent.getStringExtra("thisPostId");
        boardNum = postIntent.getIntExtra("boardNum", 0);
        switch (boardNum) {
            case 0:
                board = "posts";
                break;
            case 1:
                board = "posts_secret";
                break;
            case 2:
                board = "posts_deal";
                break;
            default:
                board = "";
                break;
        }

        idTextView = findViewById(R.id.idTextView);
        dateTextView = findViewById(R.id.dateTextView);
        titleTextView = findViewById(R.id.titleTextView);
        messageTextView = findViewById(R.id.messageTextView);

        // 게시물이 내 게시물일 때
        toEditButton = findViewById(R.id.toEditButton);

        commentRecyclerView = findViewById(R.id.recyclerView_comment);
        myRecyclerAdapter = new MyRecyclerAdapter_Comment();

        // 게시물 정보 받아와서 갱신
        getDataFromFireStore(id);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(PostActivity.this));


        // 댓글 등록하는 사용자 kakaoId, userId 정보 가져오기
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                comment_kakaoId = String.valueOf(user.getId());
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

        // isSecret
        isSecretLayout = findViewById(R.id.isSecretLayout);
        isSecretImage = findViewById(R.id.isSecretCheckImage);
        isSecretTextView = findViewById(R.id.isSecretTextView);
        if (boardNum == 1) {
            isSecretLayout.setVisibility(View.VISIBLE);
        }
        isSecretLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSecret) {
                    isSecretImage.setImageResource(R.drawable.ic_baseline_check_box_outline_blank_24);
                    isSecretImage.setColorFilter(Color.parseColor("#707070"));
                    isSecretTextView.setTextColor(Color.parseColor("#707070"));
                    isSecret = false;
                } else {
                    isSecretImage.setImageResource(R.drawable.ic_baseline_check_box_24);
                    isSecretImage.setColorFilter(Color.parseColor("#F08080"));
                    isSecretTextView.setTextColor(Color.parseColor("#F08080"));
                    isSecret = true;
                }

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
                    if (boardNum == 0) {
                        reply = new CommentItem(thisPostKakaoId, comment_kakaoId, comment_userId, comment, comment_date, true, false, realKakaoId);
                    } else if (boardNum == 1) {
                        if (comment_kakaoId.equals(thisPostKakaoId)) {
                            reply = new CommentItem(thisPostKakaoId, comment_kakaoId, "익명", comment, comment_date, true, isReplySecret, realKakaoId);
                        } else {
                            if (secretMemberItem.size() == 0) {
                                reply = new CommentItem(thisPostKakaoId, comment_kakaoId, "익명" + 1, comment, comment_date, true, isReplySecret, realKakaoId);
                                secretMemberItems.add(new SecretMemberItem(comment_kakaoId, secretMemberItem.size() + 1));
                            } else {
                                Boolean isContainSecretMember = false;
                                for (int i = 0; i < secretMemberItem.size(); i++) {
                                    if (secretMemberItem.get(i).getKakaoId().equals(comment_kakaoId)) {
                                        reply = new CommentItem(thisPostKakaoId, comment_kakaoId, "익명" + secretMemberItem.get(i).getNum(), comment, comment_date, true, isReplySecret, realKakaoId);
                                        isContainSecretMember = true;
                                        break;
                                    }
                                }
                                if (isContainSecretMember == false) {
                                    reply = new CommentItem(thisPostKakaoId, comment_kakaoId, "익명" + String.valueOf(secretMemberItem.size() + 1), comment, comment_date, true, isReplySecret, realKakaoId);
                                    secretMemberItems.add(new SecretMemberItem(comment_kakaoId, secretMemberItem.size() + 1));
                                }
                            }

                        }

                    } else if (boardNum == 2) {
                        reply = new CommentItem(thisPostKakaoId, comment_kakaoId, comment_userId, comment, comment_date, true, false, realKakaoId);
                    }

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
                            (Boolean) commentItem.get("secret")
                    ));

                }
                // 일반 댓글 등록
                else {
                    if (boardNum == 0) {
                        item = new CommentItem(thisPostKakaoId, comment_kakaoId, comment_userId, comment, comment_date, false, false, new ArrayList<CommentItem>(), isSecret);
                    } else if (boardNum == 1) {
                        if (comment_kakaoId.equals(thisPostKakaoId)) {
                            item = new CommentItem(thisPostKakaoId, comment_kakaoId, "익명", comment, comment_date, false, false, new ArrayList<CommentItem>(), isSecret);
                        } else {
                            if (secretCommentMemberNum == 0) {
                                item = new CommentItem(thisPostKakaoId, comment_kakaoId, "익명" + 1, comment, comment_date, false, false, new ArrayList<CommentItem>(), isSecret);
                                secretMemberItems.add(new SecretMemberItem(comment_kakaoId, secretMemberItem.size() + 1));
                            } else {
                                Boolean isContainSecretMember = false;
                                for (int i = 0; i < secretMemberItem.size(); i++) {
                                    if (secretMemberItem.get(i).getKakaoId().equals(comment_kakaoId)) {
                                        item = new CommentItem(thisPostKakaoId, comment_kakaoId, "익명" + secretMemberItem.get(i).getNum(), comment, comment_date, false, false, new ArrayList<CommentItem>(), isSecret);
                                        isContainSecretMember = true;
                                        break;
                                    }
                                }
                                if (isContainSecretMember == false) {
                                    item = new CommentItem(thisPostKakaoId, comment_kakaoId, "익명" + String.valueOf(secretMemberItem.size() + 1), comment, comment_date, false, false, new ArrayList<CommentItem>(), isSecret);
                                    secretMemberItems.add(new SecretMemberItem(comment_kakaoId, secretMemberItem.size() + 1));
                                }
                            }
                        }
                    } else if (boardNum == 2) {
                        item = new CommentItem(thisPostKakaoId, comment_kakaoId, comment_userId, comment, comment_date, false, false, new ArrayList<CommentItem>(), isSecret);
                    }


                    Log.d(TAG, "**" + item.getReply());

                    commentItems.add(item);
                }
                AddComment();

                // 알림 데이터베이스 추가
                if (isReply) {
                    if (boardNum == 0)
                        notification.notice_comment("자유게시판", id, thisPostKakaoId, comment_kakaoId, realKakaoId, comment, "reply", comment_date);
                    else if (boardNum == 1)
                        notification.notice_comment("비밀게시판", id, thisPostKakaoId, comment_kakaoId, realKakaoId, comment, "reply", comment_date);
                } else {
                    if (boardNum == 0)
                        notification.notice_comment("자유게시판", id, thisPostKakaoId, comment_kakaoId, "", comment, "comment", comment_date);
                    else if (boardNum == 1)
                        notification.notice_comment("비밀게시판", id, thisPostKakaoId, comment_kakaoId, "", comment, "comment", comment_date);
                }


                imm.hideSoftInputFromWindow(commentEditText.getWindowToken(), 0);
                Intent intent = new Intent(getApplicationContext(), PostActivity.class);
                intent.putExtra("thisPostId", id);
                intent.putExtra("boardNum", boardNum);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        // 댓글 갯수 TextView
        commentNumTextView = findViewById(R.id.commentNumTextView);

    }

    private void AddComment() {
        db.collection(board).document(id)
                .update("comment", commentItems);
        if (boardNum == 1)
            db.collection(board).document(id).update("secretMember", secretMemberItems);
        Toast.makeText(getApplicationContext(), "댓글을 등록하였습니다.", Toast.LENGTH_SHORT).show();
    }

    private void getDataFromFireStore(String id) {
        db.collection(board).document(id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            thisPostKakaoId = document.getData().get("kakaoId").toString();
                            title = document.getData().get("title").toString();
                            message = document.getData().get("message").toString();
                            userId = document.getData().get("userId").toString();
                            date = document.getData().get("date").toString();
                            if (boardNum == 1) {
                                secretMemberItems = new ArrayList<>();
                                secretMemberItems = (ArrayList<SecretMemberItem>) document.getData().get("secretMember");
                                secretCommentMemberNum = secretMemberItems.size();
                                secretMemberItem = new ArrayList<>();
                                for (int i = 0; i < secretCommentMemberNum; i++) {
                                    Map memberItem = new HashMap();
                                    memberItem = (Map) secretMemberItems.get(i);
                                    secretMemberItem.add(new SecretMemberItem(
                                            String.valueOf(memberItem.get("kakaoId")),
                                            Integer.parseInt(String.valueOf(memberItem.get("num")))
                                    ));
                                }
                            }
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


                            // 댓글 개수 갱신
                            commentNumTextView.setText(String.valueOf(comments.size()));
                            commentNum = comments.size();

                            // 댓글 갱신
                            commentRecyclerView.setAdapter(myRecyclerAdapter);

                            myRecyclerAdapter.setmCommentList(comments);
                            for (int i = 0; i < comments.size(); i++) {
                                Log.d(TAG, "" + i + " : " + comments.get(i).getRealKakaoId() + "\n");
                            }

                            // 댓글 개수 갱신
                            db.collection(board).document(id)
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
                                        if (comments.get(position).getSecret()) {
                                            if (!comments.get(position).getKakaoId().equals(comment_kakaoId) &&
                                                    !comments.get(position).getThisPostKakaoId().equals(comment_kakaoId)) {
                                                return;
                                            } else {
                                                isReplySecret = true;
                                            }
                                        }
                                        realKakaoId = comments.get(position).getKakaoId();
                                        isSecretLayout.setVisibility(View.GONE);
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
                                                if (boardNum == 1)
                                                    isSecretLayout.setVisibility(View.VISIBLE);
                                                replyLayout.setVisibility(View.GONE);
                                                // EditText 포커스 제거
                                                commentEditText.clearFocus();
                                                imm.hideSoftInputFromWindow(commentEditText.getWindowToken(), 0);
                                                isReply = false;
                                                isReplySecret = false;
                                            }
                                        });
                                    }
                                }

                                // 댓글 삭제
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
                                    AlertDialog.Builder alert = new AlertDialog.Builder(PostActivity.this);
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
                                                Log.d(TAG, "before delete : " + replyList.size() + "replyPosition : " + replyPosition);
                                                notification.notice_delete_comment(id, commentItem.get("realKakaoId").toString(), comment_kakaoId, commentItem.get("date").toString());
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
                                                notification.notice_delete_comment(id, commentItem.get("thisPostKakaoId").toString(), comment_kakaoId, commentItem.get("date").toString());
//
//                                                ArrayList<CommentItem> afterDeleteComments = new ArrayList<>();
//                                                AlertDialog.Builder alert2 = new AlertDialog.Builder(PostActivity.this);
//                                                alert2.setTitle("해당 댓글을 삭제하면 답글들도 같이 삭제됩니다. 괜찮습니까?");
//                                                alert2.setPositiveButton("네", new DialogInterface.OnClickListener() {
//                                                    @Override
//                                                    public void onClick(DialogInterface dialog, int which) {
//                                                        for(int i=0; i<commentItems.size(); i++) {
//                                                            Map commentItem = new HashMap();
//                                                            commentItem = (Map) commentItems.get(i);
//                                                            if(i == realPosition) continue;
//                                                            afterDeleteComments.add(new CommentItem(
//                                                                    String.valueOf(commentItem.get("thisPostKakaoId")),
//                                                                    String.valueOf(commentItem.get("kakaoId")),
//                                                                    String.valueOf(commentItem.get("userId")),
//                                                                    String.valueOf(commentItem.get("comment")),
//                                                                    String.valueOf(commentItem.get("date")),
//                                                                    (Boolean) commentItem.get("mode"),
//                                                                    (Boolean) commentItem.get("reply"),
//                                                                    (ArrayList<CommentItem>) commentItem.get("replyList")
//                                                            ));
//                                                            Log.d(TAG, afterDeleteComments.toString());
//                                                        }
//                                                        db.collection(board).document(id)
//                                                                .update("comment", afterDeleteComments);
//                                                        Intent intent = new Intent(getApplicationContext() , PostActivity.class);
//                                                        intent.putExtra("thisPostId", id);
//                                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                                        startActivity(intent);
//                                                    }
//                                                }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
//                                                    @Override
//                                                    public void onClick(DialogInterface dialog, int which) { }
//                                                });
//                                                AlertDialog alertDialog2 = alert2.create();
//                                                alertDialog2.show();
                                            }
                                            db.collection(board).document(id)
                                                    .update("comment", commentItems);
                                            Intent intent = new Intent(getApplicationContext(), PostActivity.class);
                                            intent.putExtra("thisPostId", id);
                                            intent.putExtra("boardNum", boardNum);
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
                            dateTextView.setText(formatTimeString(date));
                            titleTextView.setText(title);
                            messageTextView.setText(message);

                            // 내 게시물이면 업데이트 버튼 VISIBLE
                            UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
                                @Override
                                public Unit invoke(User user, Throwable throwable) {
                                    kakaoId = String.valueOf(user.getId());
                                    Log.d(TAG, "myKakaoId : " + kakaoId + " thisPostKakaoId : " + thisPostKakaoId);
                                    if (kakaoId.equals(thisPostKakaoId)) {
                                        idTextView.setText(userId + "(나)");
                                        toEditButton.setVisibility(View.VISIBLE);
                                        toEditButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent editIntent = new Intent(getApplicationContext(), PostEditActivity.class);
                                                editIntent.putExtra("thisPostId", id);
                                                editIntent.putExtra("boardNum", boardNum);
                                                startActivity(editIntent);
                                            }
                                        });
                                    } else {
                                        idTextView.setText(userId);
                                        if (userId.equals("탈퇴한 회원")) {
                                            idTextView.setTextColor(Color.parseColor("#D3D3D3"));
                                        }
                                    }
                                    return null;
                                }
                            });
                        }
                    }
                });
    }

    // 시간 단위 나누기
    private String formatTimeString(String date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat monthDateFormat = new SimpleDateFormat("MM/dd HH:mm");
        SimpleDateFormat yearDateFormat = new SimpleDateFormat("yy/MM/dd HH:mm");

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
        if (diffTime < TimeMaximum.SEC ||
                (diffTime /= TimeMaximum.SEC) < TimeMaximum.MIN ||
                (diffTime /= TimeMaximum.MIN) < TimeMaximum.HOUR ||
                (diffTime /= TimeMaximum.HOUR) < TimeMaximum.DAY ||
                (diffTime /= TimeMaximum.DAY) < TimeMaximum.MONTH) {
//            msg = (diffTime) + "달 전";
            msg = monthDateFormat.format(new Date(regTime));
        } else {
            msg = yearDateFormat.format(new Date(regTime));
        }
        return msg;
    }

}