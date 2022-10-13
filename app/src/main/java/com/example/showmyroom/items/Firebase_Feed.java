package com.example.showmyroom.items;

import java.util.ArrayList;

public class Firebase_Feed {
    public String kakaoId;
    public String userId;
    public String introduction;
    public String residence1, residence2, residence3;  // 거주지

    public Firebase_Feed(String kakaoId, String userId, String introduction, String residence1, String residence2, String residence3) {
        this.kakaoId = kakaoId;
        this.userId = userId;
        this.introduction = introduction;
        this.residence1 = residence1;
        this.residence2 = residence2;
        this.residence3 = residence3;
    }
}
