package com.example.showmyroom.items;

import java.util.ArrayList;

public class BoardItem {
    String kakaoId;
    String userId;
    String title;
    String message;
    String date;
    String id;
    ArrayList<CommentItem> comment;
    String commentNum;
    ArrayList<SecretMemberItem> secretMember;




    public BoardItem(String kakaoId, String userId, String title, String message, String date, String id, ArrayList<CommentItem> comment, String commentNum) {
        this.kakaoId = kakaoId;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.date = date;
        this.id = id;
        this.comment = comment;
        this.commentNum = commentNum;
    }

    // 자유게시판
    public BoardItem(String kakaoId, String userId, String title, String message, String date, ArrayList<CommentItem> comment, String commentNum) {
        this.kakaoId = kakaoId;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.date = date;
        this.comment = comment;
        this.commentNum = commentNum;
    }

    // 비밀게시판
    public BoardItem(String kakaoId, String userId, String title, String message, String date,  ArrayList<CommentItem> comment, String commentNum, ArrayList<SecretMemberItem> secretMember) {
        this.kakaoId = kakaoId;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.date = date;
        this.comment = comment;
        this.commentNum = commentNum;
        this.secretMember = secretMember;
    }

    public BoardItem() {

    }

    public ArrayList<SecretMemberItem> getSecretMember() {
        return secretMember;
    }

    public void setSecretMember(ArrayList<SecretMemberItem> secretMember) {
        this.secretMember = secretMember;
    }

    public String getCommentNum() {
        return commentNum;
    }

    public void setCommentNum(String commentNum) {
        this.commentNum = commentNum;
    }

    public ArrayList<CommentItem> getComment() {
        return comment;
    }

    public void setComment(ArrayList<CommentItem> comment) {
        this.comment = comment;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
