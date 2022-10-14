package com.example.showmyroom.items;

import android.net.Uri;

import java.util.ArrayList;

public class HomeItem {
    String thisFeedKakaoId;
    String userId;
    String date;
    String likeNum;
    String commentNum;
    String id;
    String message;
    ArrayList<String> likeList;
    ArrayList<Uri> postUriList;

    public HomeItem(String thisFeedKakaoId, String userId, String message, String date, String likeNum, String commentNum, String id, ArrayList<String> likeList) {
        this.thisFeedKakaoId = thisFeedKakaoId;
        this.userId = userId;
        this.message = message;
        this.date = date;
        this.likeNum = likeNum;
        this.commentNum = commentNum;
        this.id = id;
        this.likeList = likeList;
    }

    public HomeItem(String thisFeedKakaoId, String userId, String message, String date, String likeNum, String commentNum, String id, ArrayList<String> likeList, ArrayList<Uri> postUriList) {
        this.thisFeedKakaoId = thisFeedKakaoId;
        this.userId = userId;
        this.message = message;
        this.date = date;
        this.likeNum = likeNum;
        this.commentNum = commentNum;
        this.id = id;
        this.likeList = likeList;
        this.postUriList = postUriList;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ArrayList<Uri> getPostUriList() {
        return postUriList;
    }

    public void setPostUriList(ArrayList<Uri> postUriList) {
        this.postUriList = postUriList;
    }

    public String getThisFeedKakaoId() {
        return thisFeedKakaoId;
    }

    public void setThisFeedKakaoId(String thisFeedKakaoId) {
        this.thisFeedKakaoId = thisFeedKakaoId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLikeNum() {
        return likeNum;
    }

    public void setLikeNum(String likeNum) {
        this.likeNum = likeNum;
    }

    public String getCommentNum() {
        return commentNum;
    }

    public void setCommentNum(String commentNum) {
        this.commentNum = commentNum;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<String> getLikeList() {
        return likeList;
    }

    public void setLikeList(ArrayList<String> likeList) {
        this.likeList = likeList;
    }
}
