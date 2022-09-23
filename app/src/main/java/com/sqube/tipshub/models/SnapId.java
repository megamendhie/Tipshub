package com.sqube.tipshub.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SnapId implements Comparable {
  private String id;
  private String username;
  private long time;

  public SnapId(){}

  public SnapId(String id, long time) {
    this.id = id;
    this.time = time;
  }

  public SnapId(String id, String username) {
    this.id = id;
    this.username = username;
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

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
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


  @Override
  public boolean equals(@Nullable Object obj) {
    if(obj instanceof SnapId){
      SnapId temp = (SnapId) obj;
      return this.getId().equals(temp.getId()) && this.getUsername().equals(temp.getUsername()) && this.getTime() == this.getTime();
    }
    return false;
  }

  @Override
  public int hashCode() {
    return (this.getId().hashCode()+this.getUsername().hashCode());
  }
}
