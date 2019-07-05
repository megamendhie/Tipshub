package models;

import java.util.Date;

public class Report {
    private String username;
    private String userId;
    private String content;
    private String postId;
    private long time;

    public Report(String username, String userId, String content, String postId){
        this.username = username;
        this.userId = userId;
        this.content = content;
        this.postId = postId;
        this.time = new Date().getTime();
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

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
}
