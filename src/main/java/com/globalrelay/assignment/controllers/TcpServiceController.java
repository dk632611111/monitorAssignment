package com.globalrelay.assignment.controllers;

import com.globalrelay.assignment.common.MessageI;
import com.globalrelay.assignment.services.TcpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.web.bind.annotation.RequestMethod.*;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;

@CrossOrigin(origins ="*", methods={POST,GET,OPTIONS,PUT,DELETE})
@RestController
@RequestMapping("/v1/tcpservice")
public class TcpServiceController {

    TcpService tcpService;

    public TcpServiceController(TcpService tcpService) {
        this.tcpService = tcpService;
    }

    @RequestMapping("/start")
    public ResponseEntity createTcpService(@RequestParam(value = "host", required = true) String host,
                                           @RequestParam(value = "port", required = true) int port) throws Exception {

        MessageI message = tcpService.startTcpService(host,port);
        return new ResponseEntity<>(message.getMessage(), message.getHttpStatus());
    }

    @RequestMapping("/stop")
    public ResponseEntity stopTcpService(@RequestParam(value = "host", required = true) String host,
                                         @RequestParam(value = "port", required = true) int port) throws Exception {

        MessageI message = tcpService.stopTcpService(host,port);
        return  new ResponseEntity<>(message.getMessage(), message.getHttpStatus());
    }

    @RequestMapping("/running")
    public ResponseEntity runningTcpService(@RequestParam(value = "host", required = true) String host,
                                         @RequestParam(value = "port", required = true) int port) throws Exception {
        MessageI message = tcpService.pollTcpService(host+port);
        return  new ResponseEntity<>(message.getMessage(), message.getHttpStatus());
    }

    @RequestMapping("/delete")
    public ResponseEntity deleteTcpService(@RequestParam(value = "host", required = true) String host,
                                            @RequestParam(value = "port", required = true) int port) throws Exception {
        MessageI message = tcpService.deleteTcpService(host,port);
        return  new ResponseEntity<>(message.getMessage(), message.getHttpStatus());
    }

}
