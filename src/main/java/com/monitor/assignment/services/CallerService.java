package com.monitor.assignment.services;

import com.monitor.assignment.common.ErrorMessageEnum;
import com.monitor.assignment.common.MapUtil;
import com.monitor.assignment.common.MessageI;
import com.monitor.assignment.common.SuccessMessageEnum;
import com.monitor.assignment.model.CallerEntity;
import com.monitor.assignment.model.OutageEntity;
import com.monitor.assignment.model.TcpServiceEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CallerService {

    private static volatile Map<String, CallerEntity> callerMap = new ConcurrentHashMap<>();

    private TcpService tcpService;

    private NotificationService notificationService;

    public CallerService(NotificationService notificationService, TcpService tcpService) {
        this.notificationService = notificationService;
        this.tcpService = tcpService;
    }

    public synchronized CallerEntity createCaller(String callerHost, int callerPort) {

        CallerEntity callerEntity = null;

        String mapKey = MapUtil.getMapKey(callerHost, callerPort);

        if ((callerEntity = callerMap.get(mapKey)) == null) {
            callerEntity = new CallerEntity(callerHost,callerPort);
            callerMap.put(mapKey,callerEntity);
        }

        return callerEntity;
    }

    public synchronized MessageI deleteCaller(String callerHost, int callerPort) {

        MessageI message = null;

        String mapKey = MapUtil.getMapKey(callerHost, callerPort);

        if (callerMap.remove(mapKey) != null) {
            message = SuccessMessageEnum.DELETE_CALLER;
        } else  {
            message = ErrorMessageEnum.UNABLE_TO_DELETE_CALLER;
        }

        return message;
    }

    public synchronized void notifyCallers() {

        // Derive outage map
        Map<String, List<OutageEntity>> outageMap = getOutages();

        // Iterate over all callers
        for (CallerEntity callerEntity : callerMap.values()) {

            Map<String,TcpServiceEntity> tcpServiceMap = callerEntity.getTcpServiceMap();

            // Iterate over all tcpServices related to this caller
            for (Map.Entry<String,TcpServiceEntity> tcpServiceEntry : tcpServiceMap.entrySet()) {

                TcpServiceEntity tcpServiceEntity = tcpServiceEntry.getValue();

                // if polling required then poll service
                if (isPollingRequired(tcpServiceEntity,outageMap)) {

                    String host = tcpServiceEntity.getHost();
                    int hostPort = tcpServiceEntity.getPort();

                    String mapKey = MapUtil.getMapKey(host, hostPort);
                    MessageI message = tcpService.pollTcpService(mapKey);

                    // If max poll rate not exceeded then notify caller of tcpService state
                    if (!message.equals(ErrorMessageEnum.POLL_RATE_EXCEEDED)) {

                        if (message.equals(SuccessMessageEnum.RUNNING_TCP_SERVICE) ||
                            isGracePeriodExceeded(tcpServiceEntity,message)) {

                            tcpServiceEntity.setTimestampFailure(0);
                            tcpServiceEntity.setTimestampLastPolled(System.currentTimeMillis());

                            String callerHost = callerEntity.getHost();
                            int callerPort = callerEntity.getPort();

                            try {
                                notificationService.notify(callerHost, callerPort, host, hostPort, message);
                            } catch (Exception e) {
                                // If caller unreachable, delete it
                                deleteCaller(callerHost,callerPort);
                            }
                        }
                    }
                }
            }
        }
    }

    // END PUBLIC INTERFACE //

    private boolean isPollingRequired(TcpServiceEntity tcpServiceEntity, Map<String, List<OutageEntity>> outageMap) {

        boolean isInOutage = false;

        long pollingRateMilliSec = tcpServiceEntity.getPollingRateSec()*1000;
        long timestampLastPolled = tcpServiceEntity.getTimestampLastPolled();
        long timestampCurrent = System.currentTimeMillis();

        String host = tcpServiceEntity.getHost();
        int port = tcpServiceEntity.getPort();
        String mapKey = MapUtil.getMapKey(host, port);

        List<OutageEntity> outageList = null;

        if ((outageList = outageMap.get(mapKey)) != null) {

            for (OutageEntity outageEntity : outageList) {
                long startOutage = outageEntity.getStartOutage();
                long endOutage = outageEntity.getEndOutage();

                isInOutage = timestampCurrent > startOutage && timestampCurrent < endOutage;
                if (isInOutage) break;
            }
        }

        boolean isPollingRateExceeded = timestampCurrent > timestampLastPolled + pollingRateMilliSec;
        boolean isPollingRequired = !isInOutage && isPollingRateExceeded;

        return isPollingRequired;
    }

    private Map<String, List<OutageEntity>> getOutages() {

        Map<String, List<OutageEntity>> outageMap = new HashMap<>();

        // Iterate over all callers
        for (CallerEntity callerEntity : callerMap.values()) {

            Map<String,TcpServiceEntity> tcpServiceMap = callerEntity.getTcpServiceMap();

            // Iterate over all tcpServices related to this caller
            for (Map.Entry<String, TcpServiceEntity> tcpServiceEntry : tcpServiceMap.entrySet()) {

                TcpServiceEntity tcpServiceEntity = tcpServiceEntry.getValue();
                OutageEntity outageEntity = tcpServiceEntity.getOutageEntity();

                if (outageEntity != null) {
                    String host = tcpServiceEntity.getHost();
                    int port = tcpServiceEntity.getPort();

                    List<OutageEntity> outageList = null;
                    String mapKey = MapUtil.getMapKey(host, port);

                    if (outageMap.get(mapKey) == null) {
                        outageList = new ArrayList<>();
                    } else {
                        outageList = outageMap.get(mapKey);
                    }
                    outageList.add(outageEntity);
                    outageMap.put(mapKey,outageList);
                }
            }
        }

        return outageMap;
    }

    private boolean isGracePeriodExceeded(TcpServiceEntity tcpServiceEntity, MessageI message) {

        boolean isGracePeriodExceeded = false;
        long currentTimestamp = System.currentTimeMillis();

        if (message.equals(ErrorMessageEnum.NOT_RUNNING_TCP_SERVICE)) {

            long  timestampFailure = tcpServiceEntity.getTimestampFailure();

            if (timestampFailure == 0) {
                tcpServiceEntity.setTimestampFailure(currentTimestamp);
                timestampFailure = currentTimestamp;
            }

            long gracePeriodMilliSec = tcpServiceEntity.getGracePeriodSec()*1000;
            isGracePeriodExceeded = currentTimestamp >= (timestampFailure+gracePeriodMilliSec);
        }

        return isGracePeriodExceeded;
    }

}