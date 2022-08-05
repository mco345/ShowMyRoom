package com.example.showmyroom.items;

import java.util.ArrayList;

public class PostItem {
    String kakaoId;
    String id;
    String message;
    String date;
    ArrayList<CommentItem> comment;
    String commentNum;
    String likeNum;

    public PostItem(String kakaoId, String id, String message, String date, ArrayList<CommentItem> comment, String commentNum, String likeNum) {
        this.kakaoId = kakaoId;
        this.id = id;
        this.message = message;
        this.date = date;
        this.comment = comment;
        this.commentNum = commentNum;
        this.likeNum = likeNum;
    }

    public String getKakaoId() {
        return kakaoId;
    }

    public void setKakaoId(String kakaoId) {
        this.kakaoId = kakaoId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public ArrayList<CommentItem> getComment() {
        return comment;
    }

    public void setComment(ArrayList<CommentItem> comment) {
        this.comment = comment;
    }

    public String getCommentNum() {
        return commentNum;
    }

    public void setCommentNum(String commentNum) {
        this.commentNum = commentNum;
    }

    public String getLikeNum() {
        return likeNum;
    }

    public void setLikeNum(String likeNum) {
        this.likeNum = likeNum;
    }
}
