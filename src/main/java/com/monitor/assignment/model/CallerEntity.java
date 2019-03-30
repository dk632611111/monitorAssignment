package com.monitor.assignment.model;

import com.monitor.assignment.common.MapUtil;

import java.util.HashMap;
import java.util.Map;

public class CallerEntity {

    private String host;

    private int port;

    private Map<String,TcpServiceEntity> tcpServiceMap = new HashMap<>();

    public CallerEntity(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setTcpServiceMap(Map<String, TcpServiceEntity> tcpServiceMap) {
        this.tcpServiceMap = tcpServiceMap;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Map<String,TcpServiceEntity> getTcpServiceMap() {
        return tcpServiceMap;
    }

    public void addTcpServiceEntity(String host,
                                    int port,
                                    long pollingRateSec,
                                    long startOutage,
                                    long endOutage,
                                    int gracePeriodSec) {

        String mapKey = MapUtil.getMapKey(host,port);

        if (tcpServiceMap.get(mapKey) == null) {
            TcpServiceEntity tcpServiceEntity = new TcpServiceEntity(host,port,pollingRateSec);

            // Optional outage and gradePeriod
            if (startOutage != 0 && endOutage != 0) {
                OutageEntity outageEntity = new OutageEntity(startOutage, endOutage);
                tcpServiceEntity.setOutageEntity(outageEntity);
            }

            if (gracePeriodSec != 0) {
                tcpServiceEntity.setGracePeriodSec(gracePeriodSec);

                // Reset pollingRateSec if < gracePeriodSec
                if (pollingRateSec < gracePeriodSec) {
                    tcpServiceEntity.setPollingRateSec(gracePeriodSec);
                }
            }
            tcpServiceMap.put(mapKey,tcpServiceEntity);
        }
    }

    public void removeTcpServiceEntity(String host, int port) {

        String mapKey = MapUtil.getMapKey(host,port);

        if (tcpServiceMap.get(mapKey) != null) {
            tcpServiceMap.remove(mapKey);
        }
    }

    @Override
    public String toString() {
        return "CallerEntity{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", tcpServiceMap=" + tcpServiceMap +
                '}';
    }
}
