package com.example.showmyroom.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.showmyroom.items.Firebase_User;
import com.example.showmyroom.PreferenceManager;
import com.example.showmyroom.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

public class LoginActivity2 extends AppCompatActivity {
    private EditText idEdit, phoneNumberEdit, phoneCheckEdit, addressEdit;
    private TextView nameText, birthText, addressText, kakaoLogoutText;
    private Button idButton, phoneCheckButton, phoneCheckButton2, addressButton, signUpButton;
    private ImageView birthButton;
    private LinearLayout phoneLayout, phoneCheckLayout, idLayout, loginLayout;

    private String kakaoId, id, name, inputPhoneNumber, address, numberGen;
    private boolean idOK, phoneOK, birthOK = false, usedId;
    private int birthYear, birthMonth, birthDate;

    private static final int SMS_SEND_PERMISSION = 1;

    private DatabaseReference mDatabase;

    private long backKeyPressedTime = 0;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // #1
        // 문자 보내기 권한 확인
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
                Toast.makeText(getApplicationContext(), "SMS권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_SEND_PERMISSION);
        }

        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                kakaoId = String.valueOf(user.getId());
                return null;
            }
        });

        // 인증번호
        phoneNumberEdit = findViewById(R.id.phoneNumberEdit);
        phoneCheckEdit = findViewById(R.id.phoneCheckEdit);
        phoneCheckButton = findViewById(R.id.phoneCheckButton);
        phoneLayout = findViewById(R.id.phoneLayout);
        phoneCheckLayout = findViewById(R.id.phoneCheckLayout);
        idLayout = findViewById(R.id.idLayout);
        phoneCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputPhoneNumber = phoneNumberEdit.getText().toString();
                numberGen = numberGen();
                sendSMS(inputPhoneNumber, "[내방볼래?]본인확인 인증번호 [" + numberGen + "]입니다.");

                phoneCheckLayout.setVisibility(View.VISIBLE);
            }
        });

        phoneCheckButton2 = findViewById(R.id.phoneCheckButton2);
        phoneCheckButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneCheck = phoneCheckEdit.getText().toString();
                if (phoneCheck.equals(numberGen)) {
                    Toast.makeText(getApplicationContext(), "인증에 성공했습니다.", Toast.LENGTH_SHORT).show();
                    phoneLayout.setVisibility(View.GONE);
                    phoneCheckLayout.setVisibility(View.GONE);
                    idLayout.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getApplicationContext(), "인증번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // #2
        // 아이디
        idEdit = findViewById(R.id.idEdit);
        // 영문과 숫자만 입력 가능
        idEdit.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                Pattern ps = Pattern.compile("^[a-zA-Z0-9]+$");
                if (source.equals("") || ps.matcher(source).matches()) {
                    return source;
                }
                Toast.makeText(getApplicationContext(), "영문, 숫자만 입력 가능합니다.", Toast.LENGTH_SHORT).show();
                return "";
            }
        },new InputFilter.LengthFilter(9)});
        loginLayout = findViewById(R.id.loginLayout);
        idButton = findViewById(R.id.idCheckButton);
        idButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                id = idEdit.getText().toString();
                usedId = false;
                mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Map memberItem = new HashMap();
                            memberItem = (Map) dataSnapshot.getValue();
                            if(id.equals(memberItem.get("id"))){
                                Toast.makeText(getApplicationContext(), "이미 존재하는 아이디입니다.", Toast.LENGTH_SHORT).show();
                                usedId = true;
                            }
                        }
                        if(!usedId){
                            idLayout.setVisibility(View.GONE);
                            loginLayout.setVisibility(View.VISIBLE);
                            Toast.makeText(getApplicationContext(), "사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
//                mDatabase.child("id_list").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        String value = snapshot.getValue(String.class);
//                        if (value != null) {
//                            Toast.makeText(getApplicationContext(), "이미 존재하는 아이디입니다.", Toast.LENGTH_SHORT).show();
//                        } else {
//
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });

            }
        });

        // #3
        // 이름
        nameText = findViewById(R.id.nameText);
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                name = user.getKakaoAccount().getProfile().getNickname();
                nameText.setText(name);
                return null;
            }
        });

        // 생년월일
        birthButton = findViewById(R.id.birthButton);
        birthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                new DatePickerDialog(LoginActivity2.this, listener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE)).show();
            }
        });



        // 주소
        addressText = findViewById(R.id.addressText);
        addressButton = findViewById(R.id.addressButton);
        addressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                getSearchResult.launch(intent);
            }
        });
        addressEdit = findViewById(R.id.addressEdit);

        // 가입하기 버튼
        signUpButton = findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (signUpAvailable()) {
                    Toast.makeText(getApplicationContext(), "가입 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    writeNewUser(kakaoId, id, name, birthYear, birthMonth, birthDate, inputPhoneNumber, address + addressEdit.getText().toString(), "");
                    PreferenceManager.setBoolean(LoginActivity2.this, "AutoLogin", true);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    if(!idOK){
                        Toast.makeText(getApplicationContext(), "아이디 중복확인을 해주세요.", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "입력되지 않은 정보가 있습니다.", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        // 카카오 로그아웃
        kakaoLogoutText = findViewById(R.id.kakaoLogoutText);
        kakaoLogoutText.setPaintFlags(kakaoLogoutText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);  // 밑줄
        kakaoLogoutText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserApiClient.getInstance().logout(new Function1<Throwable, Unit>() {
                    @Override
                    public Unit invoke(Throwable throwable) {
                        Toast.makeText(getApplicationContext(), "로그아웃하였습니다.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        return null;
                    }
                });
            }
        });


    }

    // 가입버튼 클릭시 파이어베이스에 정보 저장
    private void writeNewUser(String _kakaoId, String _id, String _name, int _birthYear, int _birthMonth, int _birthDate, String _inputPhoneNumber, String _address, String _profileImage) {
        Firebase_User user = new Firebase_User(_kakaoId, _id, _name, _birthYear, _birthMonth, _birthDate, _inputPhoneNumber, _address, _profileImage);

        mDatabase.child("users").child(_kakaoId).setValue(user);

    }

    // 인증번호 전송
    private void sendSMS(String phoneNumber, String message) {
//        PendingIntent pi = PendingIntent.getActivity(this, 0,
//                new Intent(this, LoginActivity2.class), PendingIntent.FLAG_IMMUTABLE);
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);

        Toast.makeText(getBaseContext(), "메시지가 전송되었습니다.", Toast.LENGTH_SHORT).show();
    }

    // 난수 형태의 인증번호 생성
    public static String numberGen() {
        Random rand = new Random();
        String numStr = "";

        for (int i = 0; i < 4; i++) {
            String ran = Integer.toString(rand.nextInt(10));
            numStr += ran;
        }
        return numStr;
    }

    // 생년월일 DatePicker
    private DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            birthText = findViewById(R.id.birthText);
            birthYear = year;
            birthMonth = month + 1;
            birthDate = dayOfMonth; // 생년월일 변수에 저장
            birthText.setText(String.format("%d-%d-%d", year, month + 1, dayOfMonth));
            birthOK = true;
        }
    };

    // Search Activity로부터 결과값이 이곳으로 전달된다(setResult에 의해)
    private final ActivityResultLauncher<Intent> getSearchResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        String data = result.getData().getStringExtra("data");
                        address = data;
                        addressText.setText(address);
                    }
                }
            }
    );

    // 가입 조건 충족
    private Boolean signUpAvailable() {
        if ((addressEdit.getText().toString() != null) && birthOK) {
            return true;
        } else {
            return false;
        }
    }

    // 뒤로가기 두번 종료
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {

        // 기존 뒤로가기 버튼의 기능을 막기위해 주석처리 또는 삭제
        // super.onBackPressed();

        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지났으면 Toast Show
        // 2000 milliseconds = 2 seconds
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지나지 않았으면 종료
        // 현재 표시된 Toast 취소
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish();
            toast.cancel();
            moveTaskToBack(true); // 태스크를 백그라운드로 이동
            finishAndRemoveTask();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }
    }


}
