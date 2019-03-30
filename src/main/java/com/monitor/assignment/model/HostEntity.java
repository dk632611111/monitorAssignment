package com.monitor.assignment.model;

import java.net.Socket;

public class HostEntity {

    private Socket socket;

    private long timestampLastPolled;

    public HostEntity(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    public long getTimestampLastPolled() {
        return timestampLastPolled;
    }

    public void setTimestampLastPolled(long timestampLastPolled) {
        this.timestampLastPolled = timestampLastPolled;
    }
}
