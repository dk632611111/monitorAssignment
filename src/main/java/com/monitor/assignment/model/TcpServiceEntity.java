package com.monitor.assignment.model;

public class TcpServiceEntity {

    private String host;

    private int port;

    private long pollingRateSec;

    private long timestampLastPolled;

    private OutageEntity outageEntity;

    private int gracePeriodSec;

    private long timestampFailure;

    public TcpServiceEntity(String host, int port, long pollingRateSec) {
        this.host = host;
        this.port = port;
        this.pollingRateSec = pollingRateSec;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getPollingRateSec() {
        return pollingRateSec;
    }

    public void setPollingRateSec(long pollingRateSec) {
        this.pollingRateSec = pollingRateSec;
    }

    public long getTimestampLastPolled() {
        return timestampLastPolled;
    }

    public void setTimestampLastPolled(long timestampLastPolled) {
        this.timestampLastPolled = timestampLastPolled;
    }

    public OutageEntity getOutageEntity() {
        return outageEntity;
    }

    public void setOutageEntity(OutageEntity outageEntity) {
        this.outageEntity = outageEntity;
    }

    public int getGracePeriodSec() {
        return gracePeriodSec;
    }

    public void setGracePeriodSec(int gracePeriodSec) {
        this.gracePeriodSec = gracePeriodSec;
    }

    public long getTimestampFailure() {
        return timestampFailure;
    }

    public void setTimestampFailure(long timestampFailure) {
        this.timestampFailure = timestampFailure;
    }

    @Override
    public String toString() {
        return "TcpServiceEntity{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", pollingRateSec=" + pollingRateSec +
                ", timestampLastPolled=" + timestampLastPolled +
                ", outageEntity=" + outageEntity +
                ", gracePeriodSec=" + gracePeriodSec +
                ", timestampFailure=" + timestampFailure +
                '}';
    }
}
