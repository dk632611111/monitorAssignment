package com.monitor.assignment.model;


public class OutageEntity {

    private long startOutage;

    private long endOutage;

    public OutageEntity(long startOutage, long endOutage) {
        this.startOutage = startOutage;
        this.endOutage = endOutage;
    }

    public long getStartOutage() {
        return startOutage;
    }

    public void setStartOutage(long startOutage) {
        this.startOutage = startOutage;
    }

    public long getEndOutage() {
        return endOutage;
    }

    public void setEndOutage(long endOutage) {
        this.endOutage = endOutage;
    }

    @Override
    public String toString() {
        return "OutageEntity{" +
                "startOutage=" + startOutage +
                ", endOutage=" + endOutage +
                '}';
    }
}
