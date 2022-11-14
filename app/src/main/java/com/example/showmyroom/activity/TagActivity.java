package com.example.showmyroom.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.showmyroom.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH;

public class TagActivity extends AppCompatActivity {
    private static final String TAG = "TagActivity";

    private EditText keywordEditText;
    private Button addButton;
    private ChipGroup chipGroup;
    private TextView showButton;
    private ArrayList<String> keywordsList = new ArrayList<>();

    String preText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);

        addButton = findViewById(R.id.addButton);
        chipGroup = findViewById(R.id.chipGroup);
        showButton = findViewById(R.id.showButton);
        keywordEditText = findViewById(R.id.keywordEditText);

        keywordsList = getIntent().getStringArrayListExtra("keywordsList");

        // 초기화
        try {
            LayoutInflater inflater = LayoutInflater.from(TagActivity.this);
            Log.d(TAG, "keywordsList - "+keywordsList);
            for(int i =0; i<keywordsList.size(); i++){
                Log.d(TAG, "keywordsList size - "+keywordsList.size());
                Log.d(TAG, i + "-"+keywordsList.get(i));
                Chip newChip = (Chip) inflater.inflate(R.layout.view_chip, chipGroup, false);
                newChip.setText(keywordsList.get(i));
                chipGroup.addView(newChip);
                newChip.setOnCloseIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleChipCloseIconClicked((Chip) v);
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 띄어쓰기 막기
        keywordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                preText = s.toString();
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().contains(" ")) {
                    Toast.makeText(getApplicationContext(), "띄어쓰기가 불가능합니다.", Toast.LENGTH_SHORT).show();
                    keywordEditText.setText(preText);
                    keywordEditText.setSelection(keywordEditText.length());
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 키워드를 입력하지 않았을 때
                if (keywordEditText.getText().toString().length() == 0) {
                    Toast.makeText(getApplicationContext(), "키워드를 입력해주세요", Toast.LENGTH_SHORT).show();
                }
                // 키워드는 10개까지 추가 가능
                if (chipGroup.getChildCount() == 10) {
                    Toast.makeText(getApplicationContext(), "키워드 추가는 10개까지 가능합니다.", Toast.LENGTH_SHORT).show();
                } else {
                    addNewChip();
                }
            }
        });
        keywordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case IME_ACTION_DONE:
                        // 키워드를 입력하지 않았을 때
                        if (keywordEditText.getText().toString().length() == 0) {
                            Toast.makeText(getApplicationContext(), "키워드를 입력해주세요", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        // 키워드는 10개까지 추가 가능
                        if (chipGroup.getChildCount() == 10) {
                            Toast.makeText(getApplicationContext(), "키워드 추가는 10개까지 가능합니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            addNewChip();
                        }
                        break;
                }
                return true;
            }
        });
        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelections();
            }
        });
    }


    private void addNewChip() {
        String keyword = keywordEditText.getText().toString();
        if (keyword == null || keyword.isEmpty()) {
            Toast.makeText(getApplicationContext(), "키워드를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(keywordsList.contains("#"+keyword)){
            Toast.makeText(getApplicationContext(), "해당 키워드가 이미 존재합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            LayoutInflater inflater = LayoutInflater.from(TagActivity.this);

            Chip newChip = (Chip) inflater.inflate(R.layout.view_chip, chipGroup, false);
            newChip.setText(keyword);

            chipGroup.addView(newChip);

            newChip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleChipCloseIconClicked((Chip) v);
                }
            });
            keywordEditText.setText("");
            keywordEditText.setHint("키워드를 입력해주세요");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleChipCloseIconClicked(Chip chip) {
        ChipGroup parent = (ChipGroup) chip.getParent();
        parent.removeView(chip);
    }

    private void showSelections() {
        int count = chipGroup.getChildCount();

        String s = "";

        for (int i = 0; i < count; i++) {
            Chip child = (Chip) chipGroup.getChildAt(i);
            keywordsList.add("#"+child.getText().toString());

            if (s.equals("")) {
                s = "#" + child.getText().toString();
            } else {
                s += " #" + child.getText().toString();
            }
        }
//        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.putExtra("keyword", s);
        intent.putExtra("keywordsList", keywordsList);
        setResult(RESULT_OK, intent);
        finish();
    }
}