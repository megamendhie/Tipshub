package com.sqube.tipshub.models;

public class ProfileShort {
    //personal info
    private String a_userId;
    private String a0_firstName;
    private String a1_lastName;
    private String a2_username;
    private String a3_email;
    private String a4_gender;
    private String a5_bio;

    //contact info
    private String b0_country;
    private String b2_dpUrl;
    private String b3_dpTmUrl;
    private String b4_coverUrl;
    private long c4_followers;
    private long c5_following;

    //subs
    private int d0_subAmount;

    //values for overall No Of Games, Won Games, and Won Games Percentage
    private long e0a_NOG;
    private long e0b_WG;
    private long e0c_WGP;

    //values for No Of Games, Won Games, and Won Games Percentage for banker
    private long e6a_NOG;
    private long e6b_WG;
    private long e6c_WGP;

    public ProfileShort(){}

    public String getA_userId() {
        return a_userId;
    }

    public void setA_userId(String a_userId) {
        this.a_userId = a_userId;
    }

    public String getA0_firstName() {
        return a0_firstName;
    }

    public void setA0_firstName(String a0_firstName) {
        this.a0_firstName = a0_firstName;
    }

    public String getA1_lastName() {
        return a1_lastName;
    }

    public void setA1_lastName(String a1_lastName) {
        this.a1_lastName = a1_lastName;
    }

    public String getA2_username() {
        return a2_username;
    }

    public void setA2_username(String a2_username) {
        this.a2_username = a2_username;
    }

    public String getA4_gender() {
        return a4_gender;
    }

    public void setA4_gender(String a4_gender) {
        this.a4_gender = a4_gender;
    }

    public String getA5_bio() {
        return a5_bio;
    }

    public void setA5_bio(String a5_bio) {
        this.a5_bio = a5_bio;
    }

    public String getB2_dpUrl() {
        return b2_dpUrl;
    }

    public void setB2_dpUrl(String b2_dpUrl) {
        this.b2_dpUrl = b2_dpUrl;
    }

    public String getB3_dpTmUrl() {
        return b3_dpTmUrl;
    }

    public void setB3_dpTmUrl(String b3_dpTmUrl) {
        this.b3_dpTmUrl = b3_dpTmUrl;
    }

    public String getB4_coverUrl() {
        return b4_coverUrl;
    }

    public void setB4_coverUrl(String b4_coverUrl) {
        this.b4_coverUrl = b4_coverUrl;
    }


    public long getE0a_NOG() {
        return e0a_NOG;
    }

    public void setE0a_NOG(long e0a_NOG) {
        this.e0a_NOG = e0a_NOG;
    }

    public long getE0b_WG() {
        return e0b_WG;
    }

    public void setE0b_WG(long e0b_WG) {
        this.e0b_WG = e0b_WG;
    }

    public long getE0c_WGP() {
        return e0c_WGP;
    }

    public void setE0c_WGP(long e0c_WGP) {
        this.e0c_WGP = e0c_WGP;
    }

    public long getC4_followers() {
        return c4_followers;
    }

    public void setC4_followers(long c4_followers) {
        this.c4_followers = c4_followers;
    }

    public long getC5_following() {
        return c5_following;
    }

    public void setC5_following(long c5_following) {
        this.c5_following = c5_following;
    }

    public int getD0_subAmount() {
        return d0_subAmount;
    }

    public void setD0_subAmount(int d0_subAmount) {
        this.d0_subAmount = d0_subAmount;
    }

    public long getE6a_NOG() {
        return e6a_NOG;
    }

    public void setE6a_NOG(long e6a_NOG) {
        this.e6a_NOG = e6a_NOG;
    }

    public long getE6b_WG() {
        return e6b_WG;
    }

    public void setE6b_WG(long e6b_WG) {
        this.e6b_WG = e6b_WG;
    }

    public long getE6c_WGP() {
        return e6c_WGP;
    }

    public void setE6c_WGP(long e6c_WGP) {
        this.e6c_WGP = e6c_WGP;
    }

    public String getB0_country() {
        return b0_country;
    }

    public void setB0_country(String b0_country) {
        this.b0_country = b0_country;
    }

    public String getA3_email() {
        return a3_email;
    }

    public void setA3_email(String a3_email) {
        this.a3_email = a3_email;
    }
}