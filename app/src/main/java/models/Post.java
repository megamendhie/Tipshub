package models;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Post implements Parcelable, Comparable {
    private String username;
    private String userId;
    private String content;
    private String imgUrl1="";
    private String imgUrl2="";
    private int status;
    private int type;
    private long time;
    private int mediaCount;
    private List<String> media = new ArrayList<>();
    private List<String> likes = new ArrayList<>();
    private long likesCount;
    private List<String> dislikes = new ArrayList<>();
    private long dislikesCount;
    private long repostCount;
    private long commentsCount;
    private long relevance;
    private long timeRelevance;
    private long reportCount;
    private int recommendedBookie;
    private String bookingCode;

    private boolean hasChild;
    private boolean verifiedUser;
    private boolean childVerifiedUser;
    private String childLink;
    private String childUsername;
    private String childUserId;
    private String childContent;
    private String childImgUrl1="";
    private String childImgUrl2="";
    private String childBookingCode;
    private int childBookie;
    private int childType;


    public Post(){}

    public Post(String username, String userId, String content, boolean verifiedUser, int status, int type,
                String childLink, String childUsername, String childUserId, String childContent, boolean childVerifiedUser, int childType,
                String childImgUrl1, String childImgUrl2, String childBookingCode, int childBookie){
        this.username = username;
        this.userId = userId;
        this.content = content;
        this.verifiedUser = verifiedUser;
        this.status = status;
        this.time = new Date().getTime();
        this.type = type;

        this.mediaCount = 0;
        this.likesCount = 0;
        this.dislikesCount = 0;
        this.repostCount = 0;
        this.commentsCount = 0;
        this.relevance = 1;
        this.timeRelevance = 1;
        this.reportCount = 0;

        this.hasChild = true;
        this.childVerifiedUser = childVerifiedUser;
        this.childLink = childLink;
        this.childUsername = childUsername;
        this.childUserId = childUserId;
        this.childContent = childContent;
        this.childType = childType;
        this.childImgUrl1= childImgUrl1;
        this.childImgUrl2= childImgUrl2;
        this.childBookingCode= childBookingCode;
        this.childBookie= childBookie;
    }

    public Post(String username, String userId, String content, boolean verifiedUser, int status, int type, String bookingCode, int recommendedBookie){
        this.username = username;
        this.userId = userId;
        this.content = content;
        this.verifiedUser = verifiedUser;
        this.status = status;
        this.time = new Date().getTime();
        this.type = type;

        this.mediaCount = 0;
        this.likesCount = 0;
        this.dislikesCount = 0;
        this.repostCount = 0;
        this.commentsCount = 0;
        this.relevance = 1;
        this.timeRelevance = 1;
        this.reportCount = 0;

        this.bookingCode = bookingCode;
        this.recommendedBookie = recommendedBookie;

        this.hasChild = false;
    }

    protected Post(Parcel in) {
        username = in.readString();
        userId = in.readString();
        content = in.readString();
        imgUrl1 = in.readString();
        imgUrl2 = in.readString();
        status = in.readInt();
        type = in.readInt();
        time = in.readLong();
        mediaCount = in.readInt();
        media = in.createStringArrayList();
        likes = in.createStringArrayList();
        likesCount = in.readLong();
        dislikes = in.createStringArrayList();
        dislikesCount = in.readLong();
        repostCount = in.readLong();
        commentsCount = in.readLong();
        relevance = in.readLong();
        timeRelevance = in.readLong();
        reportCount = in.readLong();
        recommendedBookie = in.readInt();
        bookingCode = in.readString();
        verifiedUser = in.readByte() != 0;
        hasChild = in.readByte() != 0;
        childVerifiedUser = in.readByte() != 0;
        childLink = in.readString();
        childUsername = in.readString();
        childUserId = in.readString();
        childContent = in.readString();
        childImgUrl1 = in.readString();
        childImgUrl2 = in.readString();
        childBookingCode = in.readString();
        childBookie = in.readInt();
        childType = in.readInt();
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

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

    public boolean isVerifiedUser() {
        return verifiedUser;
    }

    public void setVerifiedUser(boolean verifiedUser) {
        this.verifiedUser = verifiedUser;
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

    public boolean isChildVerifiedUser() {
        return childVerifiedUser;
    }

    public void setChildVerifiedUser(boolean childVerifiedUser) {
        this.childVerifiedUser = childVerifiedUser;
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

    public long getTimeRelevance() {
        return timeRelevance;
    }

    public void setTimeRelevance(long timeRelevance) {
        this.timeRelevance = timeRelevance;
    }

    public String getChildContent() {
        return childContent;
    }

    public void setChildContent(String childContent) {
        this.childContent = childContent;
    }

    public String getChildImgUrl1() {
        return childImgUrl1;
    }

    public void setChildImgUrl1(String childImgUrl1) {
        this.childImgUrl1 = childImgUrl1;
    }

    public String getChildImgUrl2() {
        return childImgUrl2;
    }

    public void setChildImgUrl2(String childImgUrl2) {
        this.childImgUrl2 = childImgUrl2;
    }

    public String getChildBookingCode() {
        return childBookingCode;
    }

    public void setChildBookingCode(String childBookingCode) {
        this.childBookingCode = childBookingCode;
    }

    public int getChildBookie() {
        return childBookie;
    }

    public void setChildBookie(int childBookie) {
        this.childBookie = childBookie;
    }

    public int getChildType() {
        return childType;
    }

    public void setChildType(int childType) {
        this.childType = childType;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(userId);
        dest.writeString(content);
        dest.writeString(imgUrl1);
        dest.writeString(imgUrl2);
        dest.writeInt(status);
        dest.writeInt(type);
        dest.writeLong(time);
        dest.writeInt(mediaCount);
        dest.writeStringList(media);
        dest.writeStringList(likes);
        dest.writeLong(likesCount);
        dest.writeStringList(dislikes);
        dest.writeLong(dislikesCount);
        dest.writeLong(repostCount);
        dest.writeLong(commentsCount);
        dest.writeLong(relevance);
        dest.writeLong(timeRelevance);
        dest.writeLong(reportCount);
        dest.writeInt(recommendedBookie);
        dest.writeString(bookingCode);
        dest.writeByte((byte) (verifiedUser ? 1 : 0));
        dest.writeByte((byte) (hasChild ? 1 : 0));
        dest.writeByte((byte) (childVerifiedUser ? 1 : 0));
        dest.writeString(childLink);
        dest.writeString(childUsername);
        dest.writeString(childUserId);
        dest.writeString(childContent);
        dest.writeString(childImgUrl1);
        dest.writeString(childImgUrl2);
        dest.writeString(childBookingCode);
        dest.writeInt(childBookie);
        dest.writeInt(childType);
    }

    @Override
    public int compareTo(@NonNull Object o) {
        Post post = (Post) o;
        if(time==post.getTime())
            return 0;
        else if (time>post.getTime())
            return -1;
        else
            return 1;
    }
}
