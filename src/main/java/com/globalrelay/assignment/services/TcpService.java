package com.globalrelay.assignment.services;

import com.globalrelay.assignment.common.ErrorMessageEnum;
import com.globalrelay.assignment.common.MapUtil;
import com.globalrelay.assignment.common.MessageI;
import com.globalrelay.assignment.common.SuccessMessageEnum;
import com.globalrelay.assignment.model.HostEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TcpService {

    private static final String LOCAL_HOST = "localhost";
    private static final int MAX_POLL_RATE = 1000;

    private static volatile Map<String, ServerSocket> socketServerMap = new ConcurrentHashMap<>();
    private static volatile Map<String, HostEntity> hostEntityMap = new ConcurrentHashMap<>();

    public synchronized MessageI startTcpService(String host, int port) {

        MessageI message = null;

        try {
            String mapKey = MapUtil.getMapKey(host, port);

            if (host.equals(LOCAL_HOST)) {
                ServerSocket serverSocket = new ServerSocket(port);
                socketServerMap.put(mapKey, serverSocket);
            }

            Socket socket = new Socket(host, port);
            HostEntity hostEntity = new HostEntity(socket);
            hostEntityMap.put(mapKey, hostEntity);

            message = SuccessMessageEnum.START_TCP_SERVICE;
        } catch (IOException e) {
            message = ErrorMessageEnum.UNABLE_TO_START_TCP_SERVICE;
        }

        return message;
    }

    public synchronized MessageI stopTcpService(String host, int port) {

        MessageI message = null;

        try {
            String mapKey = MapUtil.getMapKey(host, port);

            if (host.equals(LOCAL_HOST)) {
                ServerSocket serverSocket = socketServerMap.get(mapKey);

                if (serverSocket != null) {
                    serverSocket.close();
                } else {
                    throw new IOException();
                }
            }

            HostEntity hostEntity = hostEntityMap.get(mapKey);

            if (hostEntity != null) {
                Socket socket = hostEntity.getSocket();

                if (socket != null) {
                    socket.close();
                } else {
                    throw new IOException();
                }
            } else {
                throw new IOException();
            }

            message = SuccessMessageEnum.STOP_TCP_SERVICE;
        } catch (IOException e) {
            message = ErrorMessageEnum.UNABLE_TO_STOP_TCP_SERVICE;
        }

        return message;
    }

    public synchronized MessageI deleteTcpService(String host, int port) {

        MessageI message = null;

        try {
            String mapKey = MapUtil.getMapKey(host, port);

            if (host.equals(LOCAL_HOST)) {
                ServerSocket serverSocket = socketServerMap.get(mapKey);

                if (serverSocket != null) {
                    serverSocket.close();
                    socketServerMap.remove(mapKey);
                } else {
                    throw new IOException();
                }
            }
            HostEntity hostEntity = hostEntityMap.get(mapKey);

            if (hostEntity != null) {
                Socket socket = hostEntity.getSocket();

                if (socket != null) {
                    socket.close();
                    hostEntityMap.remove(mapKey);
                } else {
                    throw new IOException();
                }
            } else {
                throw new IOException();
            }

            message = SuccessMessageEnum.DELETE_TCP_SERVICE;
        } catch (IOException e) {
            message = ErrorMessageEnum.UNABLE_TO_DELETE_TCP_SERVICE;
        }

        return message;
    }

    public synchronized MessageI pollTcpService(String hostAndPort) {

        MessageI message = null;
        HostEntity hostEntity = hostEntityMap.get(hostAndPort);

        if (hostEntity != null) {
            long timestampLastPolled = hostEntity.getTimestampLastPolled();
            long timestampCurrent = System.currentTimeMillis();

            if (timestampCurrent - timestampLastPolled > MAX_POLL_RATE) {

                hostEntity.setTimestampLastPolled(timestampCurrent);
                Socket socket = hostEntity.getSocket();

                if (socket != null && socket.isConnected()) {
                    message = SuccessMessageEnum.RUNNING_TCP_SERVICE;
                } else {
                    message = ErrorMessageEnum.NOT_RUNNING_TCP_SERVICE;
                }
            } else {
                message = ErrorMessageEnum.POLL_RATE_EXCEEDED;
            }
        } else {
            message = ErrorMessageEnum.NOT_RUNNING_TCP_SERVICE;
        }

        return message;
    }
}