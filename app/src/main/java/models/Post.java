package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Post implements Serializable{
    private String username;
    private String userId;
    private String content;
    private String imgUrl1;
    private String imgUrl2;
    private int status;
    private int type;
    private long time;
    private List<String> likes = new ArrayList<>();
    private long likesCount;
    private List<String> dislikes = new ArrayList<>();
    private long dislikesCount;
    private long repostCount;
    private long commentsCount;
    private long relevance;
    private long reportCount;

    private boolean hasChild;
    private String childLink;
    private String childUsername;
    private String childUserId;

    private int recommendedBookie;
    private String bookingCode;


    public Post(){}

    public Post(String username, String userId, String content, int status, int type,
                String childLink, String childUsername, String childUserId){
        this.username = username;
        this.userId = userId;
        this.content = content;
        this.status = status;
        this.time = new Date().getTime();
        this.type = type;

        this.likesCount = 0;
        this.dislikesCount = 0;
        this.repostCount = 0;
        this.commentsCount = 0;
        this.relevance = 1;
        this.reportCount = 0;

        this.hasChild = true;
        this.childLink = childLink;
        this.childUsername = childUsername;
        this.childUserId = childUserId;
    }

    public Post(String username, String userId, String content, int status, int type, String bookingCode, int recommendedBookie){
        this.username = username;
        this.userId = userId;
        this.content = content;
        this.status = status;
        this.time = new Date().getTime();
        this.type = type;

        this.likesCount = 0;
        this.dislikesCount = 0;
        this.repostCount = 0;
        this.commentsCount = 0;
        this.relevance = 1;
        this.reportCount = 0;

        this.bookingCode = bookingCode;
        this.recommendedBookie = recommendedBookie;

        this.hasChild = false;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImgUrl1() {
        return imgUrl1;
    }

    public void setImgUrl1(String imgUrl1) {
        this.imgUrl1 = imgUrl1;
    }

    public String getImgUrl2() {
        return imgUrl2;
    }

    public void setImgUrl2(String imgUrl2) {
        this.imgUrl2 = imgUrl2;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(long likesCount) {
        this.likesCount = likesCount;
    }

    public long getDislikesCount() {
        return dislikesCount;
    }

    public void setDislikesCount(long dislikesCount) {
        this.dislikesCount = dislikesCount;
    }

    public long getRepostCount() {
        return repostCount;
    }

    public void setRepostCount(long repostCount) {
        this.repostCount = repostCount;
    }

    public long getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(long commentsCount) {
        this.commentsCount = commentsCount;
    }

    public long getRelevance() {
        return relevance;
    }

    public void setRelevance(long relevance) {
        this.relevance = relevance;
    }

    public long getReportCount() {
        return reportCount;
    }

    public void setReportCount(long reportCount) {
        this.reportCount = reportCount;
    }

    public boolean isHasChild() {
        return hasChild;
    }

    public void setHasChild(boolean hasChild) {
        this.hasChild = hasChild;
    }

    public String getChildLink() {
        return childLink;
    }

    public void setChildLink(String childLink) {
        this.childLink = childLink;
    }

    public String getChildUsername() {
        return childUsername;
    }

    public void setChildUsername(String childUsername) {
        this.childUsername = childUsername;
    }

    public String getChildUserId() {
        return childUserId;
    }

    public void setChildUserId(String childUserId) {
        this.childUserId = childUserId;
    }

    public int getRecommendedBookie() {
        return recommendedBookie;
    }

    public void setRecommendedBookie(int recommendedBookie) {
        this.recommendedBookie = recommendedBookie;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public List<String> getLikes() {
        return likes;
    }

    public void setLikes(List<String> likes) {
        this.likes = likes;
    }

    public List<String> getDislikes() {
        return dislikes;
    }

    public void setDislikes(List<String> dislikes) {
        this.dislikes = dislikes;
    }
}
