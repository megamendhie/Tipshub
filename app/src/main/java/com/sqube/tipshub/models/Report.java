package com.sqube.tipshub.models;

import java.util.Date;

public class Report {
    private String username;
    private String userId;
    private String content;
    private String postId;
    private String reportedUsername;
    private String reportedUserId;
    private long time;

    public Report(String username, String userId, String content, String postId, String reportedUsername, String reportedUserId){
        this.username = username;
        this.userId = userId;
        this.content = content;
        this.postId = postId;
        this.reportedUsername = reportedUsername;
        this.reportedUserId = reportedUserId;
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

    public String getReportedUsername() {
        return reportedUsername;
    }

    public void setReportedUsername(String reportedUsername) {
        this.reportedUsername = reportedUsername;
    }

    public String getReportedUserId() {
        return reportedUserId;
    }

    public void setReportedUserId(String reportedUserId) {
        this.reportedUserId = reportedUserId;
    }
}
