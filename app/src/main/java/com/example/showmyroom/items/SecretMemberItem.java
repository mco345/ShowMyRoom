package com.example.showmyroom.items;

public class SecretMemberItem {
    private String kakaoId;
    private int num;

    public SecretMemberItem(String kakaoId, int num) {
        this.kakaoId = kakaoId;
        this.num = num;
    }

    public String getKakaoId() {
        return kakaoId;
    }

    public void setKakaoId(String kakaoId) {
        this.kakaoId = kakaoId;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
