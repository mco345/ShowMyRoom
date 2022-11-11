package com.example.showmyroom;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.showmyroom.items.NoticeItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;

public class Notification {
    private static final String TAG = "Notification";

    // NoticeItem
    private NoticeItem noticeItem;

    // Date
    long now = System.currentTimeMillis();
    String date = String.valueOf(now);

    // 파이어스토어
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    // ArrayList
    private ArrayList<NoticeItem> noticeItems;


    public void notice_like(String whatBoard, String postId, String thisPostKakaoId, String myKakaoId) {
        if (!thisPostKakaoId.equals(myKakaoId)) {
            noticeItem = new NoticeItem(
                    whatBoard,
                    postId,
                    thisPostKakaoId,
                    myKakaoId,
                    "",
                    "",
                    "like",
                    date,
                    ""
            );
            firestore.collection("notification").add(noticeItem)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "notice_like - " + myKakaoId + "가 " + whatBoard + "에서 " + thisPostKakaoId + "의 " + postId + "게시물에 좋아요를 눌렀습니다.");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
    }

    public void notice_noLike(String postId, String thisPostKakaoId, String myKakaoId) {
        if (!thisPostKakaoId.equals(myKakaoId)) {
            Query first = firestore.collection("notification");
            first.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot document : task.getResult()) {
                                    if (document.getData().get("postId").toString().equals(postId)
                                            && document.getData().get("myKakaoId").toString().equals(myKakaoId)) {
                                        firestore.collection("notification").document(document.getId()).delete();
                                        Log.d(TAG, "notice_like - " + myKakaoId + "가 피드에서 " + postId + "게시물에 좋아요를 취소했습니다.");
                                    }
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
    }

    public void notice_comment(String whatBoard, String postId, String thisPostKakaoId, String myKakaoId, String realKakaoId, String message, String type, String comment_date) {
        noticeItem = new NoticeItem(
                whatBoard,
                postId,
                thisPostKakaoId,
                myKakaoId,
                realKakaoId,
                message,
                type,
                date,
                comment_date
        );
        if (type.equals("reply")) {
            if (!realKakaoId.equals(myKakaoId)) {
                firestore.collection("notification").add(noticeItem)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG, "notice_comment - " + myKakaoId + "가 " + whatBoard + "에서 " + thisPostKakaoId + "의 " + postId + "게시물의 " +
                                        thisPostKakaoId + "의 댓글에 " + message + "라는 내용의 대댓글을 달았습니다.(" + comment_date + ")");

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        } else {
            if (!thisPostKakaoId.equals(myKakaoId)) {
                if (!realKakaoId.equals(myKakaoId)) {
                    firestore.collection("notification").add(noticeItem)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d(TAG, "notice_comment - " + myKakaoId + "가 " + whatBoard + "에서 " + thisPostKakaoId + "의 " +
                                            postId + "게시물에 " + message + "라는 내용의 댓글을 달았습니다.(" + comment_date + ")");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                }
            }
        }
    }

    public void notice_delete_comment(String postId, String thisPostKakaoId, String myKakaoId, String comment_date) {
        if (!thisPostKakaoId.equals(myKakaoId)) {
            Query first = firestore.collection("notification");
            first.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot document : task.getResult()) {
                                    if (document.getData().get("type").equals("comment") || document.getData().get("type").equals("reply")) {
                                        if (document.getData().get("postId").toString().equals(postId)
                                                && document.getData().get("myKakaoId").toString().equals(myKakaoId) && document.getData().get("comment_date").toString().equals(comment_date)) {
                                            firestore.collection("notification").document(document.getId()).delete();
                                            Log.d(TAG, "notice_delete_comment - " + myKakaoId + "가 " + postId + "게시물의 댓글을 삭제했습니다.(" + comment_date + ")");
                                        }
                                    }

                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
    }

    public void notice_follow(String thisFeedKakaoId, String myKakaoId) {
        noticeItem = new NoticeItem(
                "팔로우",
                "",
                thisFeedKakaoId,
                myKakaoId,
                "",
                "",
                "follow",
                date,
                ""
        );

        firestore.collection("notification").add(noticeItem)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "notice_follow - " + myKakaoId + "가 " + thisFeedKakaoId + "를 팔로우했습니다.");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    public void notice_unFollow(String thisFeedKakaoId, String myKakaoId){
        Query first = firestore.collection("notification");
        first.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                if (document.getData().get("myKakaoId").toString().equals(myKakaoId)
                                        && document.getData().get("thisPostKakaoId").toString().equals(thisFeedKakaoId)) {
                                    firestore.collection("notification").document(document.getId()).delete();
                                    Log.d(TAG, "notice_unFollow - " + myKakaoId + "가 " + thisFeedKakaoId + "를 언팔로우했습니다.");
                                }
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
}
