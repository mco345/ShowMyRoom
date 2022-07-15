package com.example.showmyroom.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.showmyroom.items.BoardItem;
import com.example.showmyroom.items.CommentItem;
import com.example.showmyroom.adapter.MyRecyclerAdapter_Board;
import com.example.showmyroom.PreferenceManager;
import com.example.showmyroom.R;
import com.example.showmyroom.items.SecretMemberItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class WritePostActivity extends AppCompatActivity {
    private static final String TAG = "WritePostActivity";
    View writeButton;
    EditText titleEditText, messageEditText;

    private MyRecyclerAdapter_Board myRecyclerAdapterBoard;
    private ArrayList<BoardItem> mBoardItems;

    private String kakaoId, id;
    private int boardNum;
    Date date;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);

        Intent writeIntent = getIntent();
        boardNum = writeIntent.getIntExtra("boardNum",0);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        titleEditText = findViewById(R.id.titleEditText);
        messageEditText = findViewById(R.id.messageEditText);

        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                kakaoId = String.valueOf(user.getId());
                mDatabase.child("users").child(kakaoId).child("id").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        id = String.valueOf(task.getResult().getValue());

                    }
                });

                return null;
            }
        });
        writeButton = findViewById(R.id.writeButton);
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               writeNewPost();
            }
        });

    }

    private void writeNewPost() {

        final String title = titleEditText.getText().toString();
        final String content = messageEditText.getText().toString();
        long now = System.currentTimeMillis();
        date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String date = sdf.format(this.date);

        if(title.length() > 0 && content.length() > 0){
            // 무슨 게시판이냐에 따라 등록되는 요소가 다름
            BoardItem boardItem = null;
            // 자유게시판
            if(boardNum == 0){
                boardItem = new BoardItem(
                        kakaoId,
                        id,
                        title,
                        content,
                        date,
                        new ArrayList<CommentItem>(),
                        "0"
                );
            }
            // 비밀게시판
            else if(boardNum == 1){
                boardItem = new BoardItem(
                        kakaoId,
                        "익명",
                        title,
                        content,
                        date,
                        new ArrayList<CommentItem>(),
                        "0",
                        new ArrayList<SecretMemberItem>()
                );
            }
            // 거래게시판
            else if(boardNum == 2){
                boardItem = new BoardItem(
                        kakaoId,
                        id,
                        title,
                        content,
                        date,
                        new ArrayList<CommentItem>(),
                        "0"
                );
            }

            uploader(boardItem, boardNum);
            Toast.makeText(getApplicationContext(), "게시글을 등록하였습니다.", Toast.LENGTH_SHORT).show();
            PreferenceManager.setInt(getApplicationContext(), "BackToMain", 2);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("boardNum", boardNum);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else{
            Toast.makeText(getApplicationContext(),"제목이나 글을 입력하시오", Toast.LENGTH_SHORT).show();
        }

    }

    private void uploader(BoardItem boardItem, int boardNum){
        String board;
        switch (boardNum){
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
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(board).add(boardItem)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

    }

}