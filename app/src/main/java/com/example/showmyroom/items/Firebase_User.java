package com.example.showmyroom.items;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Firebase_User {

    public String kakaoId;
    public String id;
    public String name;
    public int birthYear;
    public int birthMonth;
    public int birthDate;
    public String phoneNumber;
    public String address;
    public String profileImage;



    public Firebase_User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Firebase_User(String kakaoId, String id, String name, int birthYear, int birthMonth, int birthDate, String phoneNumber, String address, String profileImage) {
        this.kakaoId = kakaoId;
        this.id = id;
        this.name = name;
        this.birthYear = birthYear;
        this.birthMonth = birthMonth;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.profileImage = profileImage;
    }
}