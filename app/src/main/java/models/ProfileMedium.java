package models;

public class ProfileMedium {
    //personal info
    private String a_userId;
    private String a0_firstName;
    private String a1_lastName;
    private String a2_username;
    private String a3_email;
    private String a4_gender;
    private String a5_bio;
    private long a6_dOB;
    private long a7_dateJoined;
    private long a8_lastSeen;

    //contact info
    private String b0_country;
    private String b1_phone;
    private String b2_dpUrl;
    private String b3_dpTmUrl;
    private String b4_coverUrl;

    //stats
    private boolean c0_verified;
    private boolean c1_banker;
    private long c2_score;
    private long c3_reported;
    private long c4_followers;
    private long c5_following;
    private long c6_subscribers;
    private long c7_subscriptions;
    private long c8_lsPostTime;
    private long c9_todayPostCount;

    //values for overall No Of Games, Won Games, and Won Games Percentage
    private long e0a_NOG;
    private long e0b_WG;
    private long e0c_WGP;

    //values for No Of Games, Won Games, and Won Games Percentage for 3-5 odds
    private long e1a_NOG;
    private long e1b_WG;
    private long e1c_WGP;

    //values for No Of Games, Won Games, and Won Games Percentage for 6-10 odds
    private long e2a_NOG;
    private long e2b_WG;
    private long e2c_WGP;

    //values for No Of Games, Won Games, and Won Games Percentage for 11-50 odds
    private long e3a_NOG;
    private long e3b_WG;
    private long e3c_WGP;

    //values for No Of Games, Won Games, and Won Games Percentage for 50+ odds
    private long e4a_NOG;
    private long e4b_WG;
    private long e4c_WGP;

    //values for No Of Games, Won Games, and Won Games Percentage for draws
    private long e5a_NOG;
    private long e5b_WG;
    private long e5c_WGP;

    //values for No Of Games, Won Games, and Won Games Percentage for banker
    private long e6a_NOG;
    private long e6b_WG;
    private long e6c_WGP;

    public ProfileMedium(){}

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

    public String getA3_email() {
        return a3_email;
    }

    public void setA3_email(String a3_email) {
        this.a3_email = a3_email;
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

    public long getA6_dOB() {
        return a6_dOB;
    }

    public void setA6_dOB(long a6_dOB) {
        this.a6_dOB = a6_dOB;
    }

    public long getA7_dateJoined() {
        return a7_dateJoined;
    }

    public void setA7_dateJoined(long a7_dateJoined) {
        this.a7_dateJoined = a7_dateJoined;
    }

    public long getA8_lastSeen() {
        return a8_lastSeen;
    }

    public void setA8_lastSeen(long a8_lastSeen) {
        this.a8_lastSeen = a8_lastSeen;
    }

    public String getB0_country() {
        return b0_country;
    }

    public void setB0_country(String b0_country) {
        this.b0_country = b0_country;
    }

    public String getB1_phone() {
        return b1_phone;
    }

    public void setB1_phone(String b1_phone) {
        this.b1_phone = b1_phone;
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

    public boolean isC0_verified() {
        return c0_verified;
    }

    public void setC0_verified(boolean c0_verified) {
        this.c0_verified = c0_verified;
    }

    public boolean isC1_banker() {
        return c1_banker;
    }

    public void setC1_banker(boolean c1_banker) {
        this.c1_banker = c1_banker;
    }

    public long getC2_score() {
        return c2_score;
    }

    public void setC2_score(long c2_score) {
        this.c2_score = c2_score;
    }

    public long getC3_reported() {
        return c3_reported;
    }

    public void setC3_reported(long c3_reported) {
        this.c3_reported = c3_reported;
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

    public long getC6_subscribers() {
        return c6_subscribers;
    }

    public void setC6_subscribers(long c6_subscribers) {
        this.c6_subscribers = c6_subscribers;
    }

    public long getC7_subscriptions() {
        return c7_subscriptions;
    }

    public void setC7_subscriptions(long c7_subscriptions) {
        this.c7_subscriptions = c7_subscriptions;
    }

    public long getC8_lsPostTime() {
        return c8_lsPostTime;
    }

    public void setC8_lsPostTime(long c8_lsPostTime) {
        this.c8_lsPostTime = c8_lsPostTime;
    }

    public long getC9_todayPostCount() {
        return c9_todayPostCount;
    }

    public void setC9_todayPostCount(long c9_todayPostCount) {
        this.c9_todayPostCount = c9_todayPostCount;
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

    public long getE1a_NOG() {
        return e1a_NOG;
    }

    public void setE1a_NOG(long e1a_NOG) {
        this.e1a_NOG = e1a_NOG;
    }

    public long getE1b_WG() {
        return e1b_WG;
    }

    public void setE1b_WG(long e1b_WG) {
        this.e1b_WG = e1b_WG;
    }

    public long getE1c_WGP() {
        return e1c_WGP;
    }

    public void setE1c_WGP(long e1c_WGP) {
        this.e1c_WGP = e1c_WGP;
    }

    public long getE2a_NOG() {
        return e2a_NOG;
    }

    public void setE2a_NOG(long e2a_NOG) {
        this.e2a_NOG = e2a_NOG;
    }

    public long getE2b_WG() {
        return e2b_WG;
    }

    public void setE2b_WG(long e2b_WG) {
        this.e2b_WG = e2b_WG;
    }

    public long getE2c_WGP() {
        return e2c_WGP;
    }

    public void setE2c_WGP(long e2c_WGP) {
        this.e2c_WGP = e2c_WGP;
    }

    public long getE3a_NOG() {
        return e3a_NOG;
    }

    public void setE3a_NOG(long e3a_NOG) {
        this.e3a_NOG = e3a_NOG;
    }

    public long getE3b_WG() {
        return e3b_WG;
    }

    public void setE3b_WG(long e3b_WG) {
        this.e3b_WG = e3b_WG;
    }

    public long getE3c_WGP() {
        return e3c_WGP;
    }

    public void setE3c_WGP(long e3c_WGP) {
        this.e3c_WGP = e3c_WGP;
    }

    public long getE4a_NOG() {
        return e4a_NOG;
    }

    public void setE4a_NOG(long e4a_NOG) {
        this.e4a_NOG = e4a_NOG;
    }

    public long getE4b_WG() {
        return e4b_WG;
    }

    public void setE4b_WG(long e4b_WG) {
        this.e4b_WG = e4b_WG;
    }

    public long getE4c_WGP() {
        return e4c_WGP;
    }

    public void setE4c_WGP(long e4c_WGP) {
        this.e4c_WGP = e4c_WGP;
    }

    public long getE5a_NOG() {
        return e5a_NOG;
    }

    public void setE5a_NOG(long e5a_NOG) {
        this.e5a_NOG = e5a_NOG;
    }

    public long getE5b_WG() {
        return e5b_WG;
    }

    public void setE5b_WG(long e5b_WG) {
        this.e5b_WG = e5b_WG;
    }

    public long getE5c_WGP() {
        return e5c_WGP;
    }

    public void setE5c_WGP(long e5c_WGP) {
        this.e5c_WGP = e5c_WGP;
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
}