package com.monitor.assignment.services;

import com.monitor.assignment.common.MessageI;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

@Service
public class NotificationService {


    public synchronized void notify(String callerHost, int callerPort, String host, int hostPort, MessageI message) throws Exception {

        Socket socket = null;
        ObjectOutputStream objectOutputStream = null;


        try {
            //establish socket connection to client's server socket
            socket = new Socket(callerHost, callerPort);

            //write message
            String fullMessage = "For host "+host+" port "+hostPort+" the status is "+message.getMessage();
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(fullMessage);

        } finally {
            try {
                objectOutputStream.close();
                socket.close();
            } catch (IOException e) {
            }
        }
    }
}
