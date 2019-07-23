package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Comment {
    private String username;
    private String userId;
    private String content;
    private String commentOn;
    private long time;
    private int mediaCount;
    private List<String> media = new ArrayList<>();
    private List<String> likes = new ArrayList<>();
    private long likesCount;
    private List<String> dislikes = new ArrayList<>();
    private long dislikesCount;
    private long relevance;
    private long timeRelevance;
    private long reportCount;
    private boolean verifiedUser;
    private boolean flag = false;


    public Comment(){}

    public Comment(String username, String userId, String content, String commentOn, boolean flag, boolean verifiedUser){
        this.username = username;
        this.userId = userId;
        this.content = content;
        this.commentOn = commentOn;
        this.verifiedUser = verifiedUser;
        this.time = new Date().getTime();
        this.flag = flag;

        this.mediaCount = 0;
        this.likesCount = 0;
        this.dislikesCount = 0;
        this.relevance = 0;
        this.timeRelevance = 1;
        this.reportCount = 0;
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

    public long getTimeRelevance() {
        return timeRelevance;
    }

    public void setTimeRelevance(long timeRelevance) {
        this.timeRelevance = timeRelevance;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public int getMediaCount() {
        return mediaCount;
    }

    public void setMediaCount(int mediaCount) {
        this.mediaCount = mediaCount;
    }

    public List<String> getMedia() {
        return media;
    }

    public void setMedia(List<String> media) {
        this.media = media;
    }

    public String getCommentOn() {
        return commentOn;
    }

    public void setCommentOn(String commentOn) {
        this.commentOn = commentOn;
    }

    public boolean isVerifiedUser() {
        return verifiedUser;
    }

    public void setVerifiedUser(boolean verifiedUser) {
        this.verifiedUser = verifiedUser;
    }
}
