package com.example.showmyroom.items;

public class NoticeItem {
    String whatBoard;
    String postId;
    String thisPostKakaoId;
    String myKakaoId;
    String realKakaoId;
    String message;
    String type;
    String date;
    String comment_date;

    public NoticeItem(String whatBoard, String postId, String thisPostKakaoId, String myKakaoId, String realKakaoId, String message, String type, String date, String comment_date) {
        this.whatBoard = whatBoard;
        this.postId = postId;
        this.thisPostKakaoId = thisPostKakaoId;
        this.myKakaoId = myKakaoId;
        this.realKakaoId = realKakaoId;
        this.message = message;
        this.type = type;
        this.date = date;
        this.comment_date = comment_date;
    }

    public String getRealKakaoId() {
        return realKakaoId;
    }

    public void setRealKakaoId(String realKakaoId) {
        this.realKakaoId = realKakaoId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getComment_date() {
        return comment_date;
    }

    public void setComment_date(String comment_date) {
        this.comment_date = comment_date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getWhatBoard() {
        return whatBoard;
    }

    public void setWhatBoard(String whatBoard) {
        this.whatBoard = whatBoard;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getThisPostKakaoId() {
        return thisPostKakaoId;
    }

    public void setThisPostKakaoId(String thisPostKakaoId) {
        this.thisPostKakaoId = thisPostKakaoId;
    }

    public String getMyKakaoId() {
        return myKakaoId;
    }

    public void setMyKakaoId(String myKakaoId) {
        this.myKakaoId = myKakaoId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
