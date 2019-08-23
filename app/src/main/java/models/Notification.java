package models;

import java.util.Date;

public class Notification {
    private String channel; //FCM channel
    private String action; //action that triggered the notification
    private String title;  //title of notification
    private String message; //notification body
    private String type;  //
    private String intentUrl;
    private String imageUrl;
    private String sendTo;
    private String sentFrom;
    private boolean seen = false;
    private long time;

    public Notification(){}

    public Notification(String action, String title, String message, String type, String intentUrl, String imageUrl, String sendTo, String sentFrom){
        this.action =action;
        this.title = title;
        this.message = message;
        this.type = type;
        this.intentUrl = intentUrl;
        this.imageUrl = imageUrl;
        this.sendTo = sendTo;
        this.sentFrom = sentFrom;
        time = new Date().getTime();
        if(type!="banker")
            this.channel = sendTo;
        else
            this.channel = "b_"+sentFrom;
    }


    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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

    public String getImgageUrl() {
        return imageUrl;
    }

    public void setImgageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSendTo() {
        return sendTo;
    }

    public void setSendTo(String sendTo) {
        this.sendTo = sendTo;
    }

    public String getIntentUrl() {
        return intentUrl;
    }

    public void setIntentUrl(String intentUrl) {
        this.intentUrl = intentUrl;
    }

    public String getSentFrom() {
        return sentFrom;
    }

    public void setSentFrom(String sentFrom) {
        this.sentFrom = sentFrom;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}
