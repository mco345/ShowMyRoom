package com.example.showmyroom.items;

import android.net.Uri;

public class MemberItem {
    String kakaoId;
    String userId;
    Uri img;

    public MemberItem(String kakaoId, String userId) {
        this.kakaoId = kakaoId;
        this.userId = userId;
    }

    public MemberItem(String kakaoId, String userId, Uri img) {
        this.kakaoId = kakaoId;
        this.userId = userId;
        this.img = img;
    }

    public String getKakaoId() {
        return kakaoId;
    }

    public void setKakaoId(String kakaoId) {
        this.kakaoId = kakaoId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Uri getImg() {
        return img;
    }

    public void setImg(Uri img) {
        this.img = img;
    }
}
