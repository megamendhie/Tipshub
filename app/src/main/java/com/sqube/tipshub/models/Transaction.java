package com.sqube.tipshub.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Transaction {
    private String amount;
    private String description;
    private String type;
    private String method;
    private String acc;
    private String senderId;
    private String senderUsername;
    private String receiverId;
    private String receiverUsername;
    private boolean credit;
    private long time;
    private int status;
    private List<String> userIds = new ArrayList<>();

    public Transaction(){}

    public Transaction(String amount, String description, String type, String method, String acc, String senderId, String senderUsername,
                       String receiverId, String receiverUsername, boolean credit, int status){
        this.amount = amount;
        this.description = description;
        this.type = type;
        this.method = method;
        this.acc = acc;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.receiverId = receiverId;
        this.receiverUsername = receiverUsername;
        this.credit = credit;
        this.status = status;
        this.userIds.add(senderId); this.userIds.add(receiverId);
        this.time = new Date().getTime();
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getAcc() {
        return acc;
    }

    public void setAcc(String acc) {
        this.acc = acc;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }

    public boolean isCredit() {
        return credit;
    }

    public void setCredit(boolean credit) {
        this.credit = credit;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
