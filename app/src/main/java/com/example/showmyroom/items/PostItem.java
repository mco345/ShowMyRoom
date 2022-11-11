package com.example.showmyroom.items;

import java.util.ArrayList;

public class PostItem {
    String whatSelected;
    String kakaoId;
    String id;
    String message;
    String date;
    ArrayList<String> keyword;
    String py;
    String dwell;
    String style;
    ArrayList<CommentItem> comment;
    String commentNum;
    String likeNum;
    ArrayList<String> likeList;

    public PostItem(String whatSelected, String kakaoId, String id, String message, String date, ArrayList<String> keyword, String py, String dwell, String style, ArrayList<CommentItem> comment, String commentNum, String likeNum, ArrayList<String> likeList) {
        this.whatSelected = whatSelected;
        this.kakaoId = kakaoId;
        this.id = id;
        this.message = message;
        this.date = date;
        this.keyword = keyword;
        this.py = py;
        this.dwell = dwell;
        this.style = style;
        this.comment = comment;
        this.commentNum = commentNum;
        this.likeNum = likeNum;
        this.likeList = likeList;
    }

    public String getWhatSelected() {
        return whatSelected;
    }

    public void setWhatSelected(String whatSelected) {
        this.whatSelected = whatSelected;
    }

    public ArrayList<String> getKeyword() {
        return keyword;
    }

    public void setKeyword(ArrayList<String> keyword) {
        this.keyword = keyword;
    }

    public String getPy() {
        return py;
    }

    public void setPy(String py) {
        this.py = py;
    }

    public String getDwell() {
        return dwell;
    }

    public void setDwell(String dwell) {
        this.dwell = dwell;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public ArrayList<String> getLikeList() {
        return likeList;
    }

    public void setLikeList(ArrayList<String> likeList) {
        this.likeList = likeList;
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
