package com.example.showmyroom.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.showmyroom.PreferenceManager;
import com.example.showmyroom.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private View kakaoLoginButton;
    private DatabaseReference mDatabase;

    private long backKeyPressedTime = 0;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        kakaoLoginButton = findViewById(R.id.kakaoLoginButton);

        // 카카오가 설치되어 있는지 확인 하는 메서드또한 카카오에서 제공 콜백 객체를 이용함
        Function2<OAuthToken, Throwable, Unit> callback = new  Function2<OAuthToken, Throwable, Unit>() {
            @Override
            public Unit invoke(OAuthToken oAuthToken, Throwable throwable) {
                // 이때 토큰이 전달이 되면 로그인이 성공한 것이고 토큰이 전달되지 않았다면 로그인 실패
                if(oAuthToken != null) {

                }
                if (throwable != null) {

                }
                updateKakaoLoginUi();
                return null;
            }
        };

        kakaoLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(UserApiClient.getInstance().isKakaoTalkLoginAvailable(LoginActivity.this)) {
//                    UserApiClient.getInstance().loginWithKakaoTalk(LoginActivity.this, callback);
//                }else {
//                    UserApiClient.getInstance().loginWithKakaoAccount(LoginActivity.this, callback);
//                }

                UserApiClient.getInstance().loginWithKakaoAccount(LoginActivity.this, callback);
            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference();

        if(PreferenceManager.getBoolean(this, "AutoLogin")){
            UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
                @Override
                public Unit invoke(User user, Throwable throwable) {
                    PreferenceManager.setInt(getApplicationContext(), "BackToMain", 0);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    return null;
                }
            });
        }else{
            updateKakaoLoginUi();
        }


    }

    private  void updateKakaoLoginUi() {
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                // 로그인이 되어있으면
                if (user != null) {

                    // 유저의 아이디
                    Log.d(TAG, "invoke: id - " + user.getId());
                    // 유저의 어카운트정보에 이메일
                    Log.d(TAG, "invoke: nickname - " + user.getKakaoAccount().getProfile().getNickname());
                    // 유저의 어카운트 정보의 프로파일에 닉네임
                    Log.d(TAG, "invoke: email - " + user.getKakaoAccount().getEmail());
                    // 유저의 어카운트 파일의 성별
                    Log.d(TAG, "invoke: gender - " + user.getKakaoAccount().getGender());
                    // 유저의 어카운트 정보에 나이
                    Log.d(TAG, "invoke: age - " + user.getKakaoAccount().getAgeRange());
                    String kakaoId = String.valueOf(user.getId());
                    mDatabase.child("users").child(kakaoId).child("id").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String value = snapshot.getValue(String.class);
                            if(value!=null){
                                PreferenceManager.setBoolean(LoginActivity.this, "AutoLogin", true);
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                PreferenceManager.setInt(getApplicationContext(), "BackToMain", 0);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }else{
                                Intent intent = new Intent(getApplicationContext(), LoginActivity2.class);
                                startActivity(intent);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                return null;
            }
        });
    }

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