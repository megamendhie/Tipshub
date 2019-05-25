package models;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Subscription {
    private boolean active;
    private String dateDelete;
    private String dateEnd;
    private String dateNotify;
    private String dateStart;
    private int duration;
    private String subFrom;
    private String subFromId;
    private String subTo;
    private String subToId;

    public Subscription(){}

    public Subscription(String subFrom, String subFromId, String subTo, String subToId, int duration){
        this.active = true;
        this.subFrom = subFrom;
        this.subFromId = subFromId;
        this.subTo = subTo;
        this.subToId = subToId;
        this.duration = duration;
        setDate();
        }

    private void setDate() {
        final Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd");
        String currentTime = sdf.format(today.getTime());
        Calendar c = Calendar.getInstance();
        if(duration==1)
            c.add(Calendar.WEEK_OF_MONTH, 2);
        if(duration==2)
            c.add(Calendar.MONTH, 1);
        this.dateStart = currentTime;
        this.dateEnd = sdf.format(c.getTime());
        c.add(Calendar.DAY_OF_MONTH, -3);
        this.dateNotify = sdf.format(c.getTime());
        c.add(Calendar.DAY_OF_MONTH, 3);
        c.add(Calendar.WEEK_OF_MONTH, 2);
        this.dateDelete = sdf.format(c.getTime());
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDateDelete() {
        return dateDelete;
    }

    public void setDateDelete(String dateDelete) {
        this.dateDelete = dateDelete;
    }

    public String getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(String dateEnd) {
        this.dateEnd = dateEnd;
    }

    public String getDateNotify() {
        return dateNotify;
    }

    public void setDateNotify(String dateNotify) {
        this.dateNotify = dateNotify;
    }

    public String getDateStart() {
        return dateStart;
    }

    public void setDateStart(String dateStart) {
        this.dateStart = dateStart;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getSubFrom() {
        return subFrom;
    }

    public void setSubFrom(String subFrom) {
        this.subFrom = subFrom;
    }

    public String getSubFromId() {
        return subFromId;
    }

    public void setSubFromId(String subFromId) {
        this.subFromId = subFromId;
    }

    public String getSubTo() {
        return subTo;
    }

    public void setSubTo(String subTo) {
        this.subTo = subTo;
    }

    public String getSubToId() {
        return subToId;
    }

    public void setSubToId(String subToId) {
        this.subToId = subToId;
    }
}
