package com.example.showmyroom.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.example.showmyroom.R;

public class TitleActivity extends AppCompatActivity {
    private ImageView splashImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title);

        splashImage = findViewById(R.id.splashImage);

        moveMain();
    }

    private void moveMain() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run()
            {
                Animation fadeInAnimation = new AlphaAnimation(0.0f, 1.0f);
                fadeInAnimation.setDuration(1500);
                fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        splashImage.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                StartMainActivity();
                            }
                        }, 500);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                splashImage.startAnimation(fadeInAnimation);


            }
        }, 300); // sec초 정도 딜레이를 준 후 시작
    }

    private void StartMainActivity(){
        //new Intent(현재 context, 이동할 activity)
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);	//intent 에 명시된 액티비티로 이동
        finish();	//현재 액티비티 종료
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}