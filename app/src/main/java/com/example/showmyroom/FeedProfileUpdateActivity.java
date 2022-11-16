package com.example.showmyroom;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.showmyroom.activity.FeedActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class FeedProfileUpdateActivity extends AppCompatActivity {
    private static final String TAG = "FeedProfileUpdateActivity";

    // UI
    private Button changeProfileImageButton;
    private TextView updateButton, textCountTextView;
    private ImageButton backButton;
    private EditText nameEditText, introEditText;

    // name, introduction
    private String name = "", introduction = "";

    // getIntent - thisFeedKakaoId
    private String thisFeedKakaoId;

    // 실시간 데이터베이스
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_profile_update);

        // getIntent
        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        introduction = intent.getStringExtra("introduction");
        thisFeedKakaoId = intent.getStringExtra("thisFeedKakaoId");

        // UI
        backButton = findViewById(R.id.backButton);
        updateButton = findViewById(R.id.updateButton);
        nameEditText = findViewById(R.id.nameEditText);
        introEditText = findViewById(R.id.introEditText);
        textCountTextView = findViewById(R.id.textCountTextView);

        // 뒤로가기 버튼
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // name
        nameEditText.setText(name);
        // introduction
        if(introduction.length() != 0){
            introEditText.setText(introduction);
        }

        // introduction 글자 수 제한
        textCountTextView.setText(introduction.length() + "/50");

        introEditText.addTextChangedListener(new TextWatcher() {
            String previousString = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                previousString= s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textCountTextView.setText(introEditText.length() + "/50");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (introEditText.length() > 50)
                {
                    introEditText.setText(previousString);
                    introEditText.setSelection(introEditText.length());
                }
            }
        });


        // 수정 버튼
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // name
                mDatabase.child("users").child(thisFeedKakaoId).child("name").setValue(nameEditText.getText().toString());

                // introduction
                introduction = introEditText.getText().toString();
                Map<String, Object> introMap = new HashMap<>();
                introMap.put("introduction", introduction);
                mDatabase.child("users").child(thisFeedKakaoId).updateChildren(introMap);

                // intent
                Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
                intent.putExtra("kakaoId", thisFeedKakaoId);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

    }
}