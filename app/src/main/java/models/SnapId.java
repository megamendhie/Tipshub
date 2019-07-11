package models;

import android.support.annotation.NonNull;

public class SnapId implements Comparable {
    private String id;
    private long time;

    public SnapId(){}

    public SnapId(String id, long time) {
        this.id = id;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        SnapId snap = (SnapId) o;
        if(time==snap.getTime())
            return 0;
        else if (time>snap.getTime())
            return -1;
        else
            return 1;
    }
}
