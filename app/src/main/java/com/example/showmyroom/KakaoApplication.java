package com.example.showmyroom;

import android.app.Application;

import com.kakao.sdk.common.KakaoSdk;

public class KakaoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        KakaoSdk.init(this, "06b6e0d125062bc71d7f2d70dd8b20d1");
    }
}
