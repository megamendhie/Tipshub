package models;

public class ProfileShort {
    //personal info
    private String a_userId;
    private String a0_firstName;
    private String a1_lastName;
    private String a2_username;
    private String a4_gender;
    private String a5_bio;

    //contact info
    private String b2_dpUrl;
    private String b3_dpTmUrl;
    private String b4_coverUrl;

    //values for overall No Of Games, Won Games, and Won Games Percentage
    private long e0a_NOG;
    private long e0b_WG;
    private long e0c_WGP;

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
}