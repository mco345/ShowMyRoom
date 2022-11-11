package com.example.showmyroom.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.example.showmyroom.Pref;
import com.example.showmyroom.fragment.MenuFragment;
import com.example.showmyroom.PreferenceManager;
import com.example.showmyroom.R;
import com.example.showmyroom.fragment.SearchFragment;
import com.example.showmyroom.fragment.SettingFragment;
import com.example.showmyroom.fragment.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import java.util.ArrayList;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button kakaoLogoutButton;

    final Fragment homeFragment = new HomeFragment();
    final Fragment searchFragment = new SearchFragment();
    final Fragment menuFragment = new MenuFragment();
    final Fragment settingFragment = new SettingFragment();
    private FragmentManager fragmentManager = getSupportFragmentManager();
    Fragment activeFragment = homeFragment;

    LinearLayout home_ly;
    BottomNavigationView bottomNavigationView;

    private long backKeyPressedTime = 0;
    private Toast toast;
    private String kakaoId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);


        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);

        // 카카오 아이디
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                kakaoId = String.valueOf(user.getId());
                PreferenceManager.setString(getApplicationContext(), "kakaoId", kakaoId);   // 카카오아이디 내장db 저장
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                mDatabase.child("users").child(kakaoId).child("id").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        PreferenceManager.setString(getApplicationContext(), "userId", String.valueOf(snapshot.getValue()));    // 유저아이디 내장db 저장
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                ArrayList<String> followList = new ArrayList<>();
                mDatabase.child("following").child(kakaoId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            followList.add(String.valueOf(dataSnapshot.getValue()));
                            if(followList.size() == snapshot.getChildrenCount()){
                                Pref pref = new Pref();
                                pref.setStringArrayPref(getApplicationContext(), "myFollowList", followList);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
                return null;
            }
        });

        Log.d(TAG, "BackToMain is "+ PreferenceManager.getInt(this, "BackToMain"));

        switch (PreferenceManager.getInt(this, "BackToMain")){
            case 0:
                fragmentManager.beginTransaction().add(R.id.home_ly, searchFragment, "1").hide(searchFragment).commit();
                fragmentManager.beginTransaction().add(R.id.home_ly, menuFragment, "2").hide(menuFragment).commit();
                fragmentManager.beginTransaction().add(R.id.home_ly, settingFragment, "4").hide(settingFragment).commit();
                fragmentManager.beginTransaction().add(R.id.home_ly, homeFragment, "0").commit();
                bottomNavigationView.setSelectedItemId(R.id.tab_home);
                break;
            case 1:
                fragmentManager.beginTransaction().add(R.id.home_ly, searchFragment, "1").commit();
                fragmentManager.beginTransaction().add(R.id.home_ly, menuFragment, "2").hide(menuFragment).commit();
                fragmentManager.beginTransaction().add(R.id.home_ly, settingFragment, "3").hide(settingFragment).commit();
                fragmentManager.beginTransaction().add(R.id.home_ly, homeFragment, "0").hide(homeFragment).commit();
                bottomNavigationView.setSelectedItemId(R.id.tab_search);
                break;
            case 2:
                fragmentManager.beginTransaction().add(R.id.home_ly, searchFragment, "1").hide(searchFragment).commit();
                fragmentManager.beginTransaction().add(R.id.home_ly, menuFragment, "2").commit();
                fragmentManager.beginTransaction().add(R.id.home_ly, settingFragment, "3").hide(settingFragment).commit();
                fragmentManager.beginTransaction().add(R.id.home_ly, homeFragment, "0").hide(homeFragment).commit();
                bottomNavigationView.setSelectedItemId(R.id.tab_menu);
                break;
            case 3:
                fragmentManager.beginTransaction().add(R.id.home_ly, searchFragment, "1").hide(searchFragment).commit();
                fragmentManager.beginTransaction().add(R.id.home_ly, menuFragment, "2").hide(menuFragment).commit();
                fragmentManager.beginTransaction().add(R.id.home_ly, settingFragment, "3").commit();
                fragmentManager.beginTransaction().add(R.id.home_ly, homeFragment, "0").hide(homeFragment).commit();
                bottomNavigationView.setSelectedItemId(R.id.tab_setting);
                break;
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.tab_home:
                    fragmentManager.beginTransaction().hide(activeFragment).show(homeFragment).commit();
                    activeFragment = homeFragment;
                    return true;
                case R.id.tab_search:
                    fragmentManager.beginTransaction().hide(activeFragment).show(searchFragment).commit();
                    activeFragment = searchFragment;
                    return true;
                case R.id.tab_menu:
                    fragmentManager.beginTransaction().hide(activeFragment).show(menuFragment).commit();
                    activeFragment = menuFragment;
                    return true;
                case R.id.tab_setting:
                    fragmentManager.beginTransaction().hide(activeFragment).show(settingFragment).commit();
                    activeFragment = settingFragment;
                    return true;
            }
            return false;
        }
    };

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
            PreferenceManager.setInt(getApplicationContext(), "BackToMain", 0);
            finish();
            toast.cancel();
            finishAffinity();
            System.runFinalization();
            System.exit(0);
        }
    }
}