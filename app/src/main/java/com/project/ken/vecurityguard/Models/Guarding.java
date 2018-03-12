package com.project.ken.vecurityguard.Models;

/**
 * Created by ken on 3/12/18.
 */

public class Guarding {
    String guard, owner, duration, start_time, end_time, totalCost, status;

    public Guarding() {
    }

    public Guarding(String guard, String owner, String duration, String start_time, String end_time, String totalCost, String status) {
        this.guard = guard;
        this.owner = owner;
        this.duration = duration;
        this.start_time = start_time;
        this.end_time = end_time;
        this.totalCost = totalCost;
        this.status = status;
    }

    public String getGuard() {
        return guard;
    }

    public void setGuard(String guard) {
        this.guard = guard;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(String totalCost) {
        this.totalCost = totalCost;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
