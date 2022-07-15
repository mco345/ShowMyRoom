package com.example.showmyroom.items;

import java.util.ArrayList;

public class CommentItem {
    String thisPostKakaoId;
    String kakaoId;
    String userId;
    String comment;
    String date;
    Boolean mode; // 대댓글 여부
    public Boolean reply; // 이 댓글이 답글인지
    ArrayList<CommentItem> replyList;
    public Boolean secret;
    String realKakaoId; // 대댓글 단 댓글의 최초 댓글의 카카오 아이디

    // 답글
    public CommentItem(String thisPostKakaoId, String kakaoId, String userId, String comment, String date, Boolean reply, Boolean secret, String realKakaoId) {
        this.thisPostKakaoId = thisPostKakaoId;
        this.kakaoId = kakaoId;
        this.userId = userId;
        this.comment = comment;
        this.date = date;
        this.reply = reply;
        this.secret = secret;
        this.realKakaoId = realKakaoId;
    }

    // 일반댓글
    public CommentItem(String thisPostKakaoId, String kakaoId, String userId, String comment, String date, Boolean mode, Boolean reply, ArrayList<CommentItem> replyList, Boolean secret) {
        this.thisPostKakaoId = thisPostKakaoId;
        this.kakaoId = kakaoId;
        this.userId = userId;
        this.comment = comment;
        this.date = date;
        this.mode = mode;
        this.reply = reply;
        this.replyList = replyList;
        this.secret = secret;
    }

    public Boolean getReply() {
        return reply;
    }

    public void setReply(Boolean reply) {
        reply = reply;
    }

    public ArrayList<CommentItem> getReplyList() {
        return replyList;
    }

    public void setReplyList(ArrayList<CommentItem> replyList) {
        this.replyList = replyList;
    }

    public String getThisPostKakaoId() {
        return thisPostKakaoId;
    }

    public void setThisPostKakaoId(String thisPostKakaoId) {
        this.thisPostKakaoId = thisPostKakaoId;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Boolean getMode() {
        return mode;
    }

    public void setMode(Boolean mode) {
        this.mode = mode;
    }

    public Boolean getSecret() {
        return secret;
    }

    public void setSecret(Boolean secret) {
        this.secret = secret;
    }

    public String getRealKakaoId() {
        return realKakaoId;
    }

    public void setRealKakaoId(String realKakaoId) {
        this.realKakaoId = realKakaoId;
    }
}
