package com.example.showmyroom.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.showmyroom.PreferenceManager;
import com.example.showmyroom.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class PostEditActivity extends AppCompatActivity {
    private static final String TAG = "PostEditActivity";

    private View deleteButton, editButton, cancelButton;
    private EditText titleEditText, messageEditText;
    private int boardNum;

    private String id, kakaoId, mykakaoId, title, message, userId, date;
    private String board;   // 어떤 게시판인지


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_edit);

        // MenuFragment로부터 data 받기
        Intent postIntent = getIntent();
        id = postIntent.getStringExtra("thisPostId");
        boardNum = postIntent.getIntExtra("boardNum",0);
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
        Log.d(TAG, "board is "+board);

        cancelButton = findViewById(R.id.cancelButton);
        deleteButton = findViewById(R.id.deleteButton);
        editButton = findViewById(R.id.editButton);
        titleEditText = findViewById(R.id.titleEditText);
        messageEditText = findViewById(R.id.messageEditText);

        // 게시물 정보 받아오기
        getDataFromFireStore(id);


        // 삭제
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(PostEditActivity.this);
                alert.setTitle("정말 삭제하시겠습니까?");
                alert.setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.collection(board).document(id)
                                .delete();
                        PreferenceManager.setInt(getApplicationContext(), "BackToMain", 2);
                        Intent intent = new Intent(getApplicationContext() , MainActivity.class);
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

        // 업데이트
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleEditText.getText().toString();
                String message = messageEditText.getText().toString();
                Log.d(TAG, "title : "+title+", message : "+message);
                db.collection(board).document(id)
                        .update("title", title, "message", message)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                Toast.makeText(getApplicationContext(),"수정이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                PreferenceManager.setInt(getApplicationContext(), "BackToMain", 2);
                Intent intent = new Intent(getApplicationContext() , PostActivity.class);
                intent.putExtra("thisPostId", id);
                intent.putExtra("boardNum", boardNum);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private void getDataFromFireStore(String id) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(board).document(id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            mykakaoId = document.getData().get("kakaoId").toString();
                            title = document.getData().get("title").toString();
                            message = document.getData().get("message").toString();
                            userId = document.getData().get("userId").toString();
                            date = document.getData().get("date").toString();

                            titleEditText.setText(title);
                            messageEditText.setText(message);
                        }
                    }
                });
    }
}