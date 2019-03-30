package com.monitor.assignment.controllers;

import com.monitor.assignment.common.*;
import com.monitor.assignment.model.CallerEntity;
import com.monitor.assignment.services.CallerService;
import com.monitor.assignment.services.TcpService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@CrossOrigin(origins ="*", methods={POST,GET,OPTIONS,PUT,DELETE})
@RestController
@Validated
@RequestMapping("/v1/caller")
public class CallerController {

    TcpService tcpService;

    CallerService callerService;

    public CallerController(TcpService tcpService, CallerService callerService) {
        this.tcpService = tcpService;
        this.callerService = callerService;
    }


    @RequestMapping("/create")
    public ResponseEntity createCallerWithOutage(
            @RequestParam(value = "callerHost", required = true) String callerHost,
            @RequestParam(value = "callerPort", required = true) int callerPort,
            @RequestParam(value = "host", required = true) String host,
            @RequestParam(value = "hostPort", required = true) int hostPort,
            @Valid @PollingRate @RequestParam(value = "pollingRateSec", required = true) int pollingRateSec,
            @RequestParam(value = "startOutage", required = false, defaultValue="0") long startOutage,
            @RequestParam(value = "endOutage", required = false, defaultValue="0") long endOutage,
            @RequestParam(value = "gracePeriodSec", required = false, defaultValue="0") int gracePeriodSec) throws Exception {

        MessageI message = createTcpService(host,hostPort);

        // Register caller (if already exist, simply add new host&port to caller list)
        if  (message.equals(SuccessMessageEnum.START_TCP_SERVICE) ||
             message.equals(SuccessMessageEnum.RUNNING_TCP_SERVICE)) {

            CallerEntity callerEntity = callerService.createCaller(callerHost,callerPort);

            callerEntity.addTcpServiceEntity(host,
                    hostPort,
                    pollingRateSec,
                    startOutage,
                    endOutage,
                    gracePeriodSec);

            message = SuccessMessageEnum.CREATE_CALLER;
        }

        return new ResponseEntity<>(message.getMessage(), message.getHttpStatus());
    }

    @RequestMapping("/delete")
    public ResponseEntity deleteCaller(
            @RequestParam(value = "callerHost", required = true) String callerHost,
            @RequestParam(value = "callerPort", required = true) int callerPort) throws Exception {

        MessageI message = callerService.deleteCaller(callerHost,callerPort);
        return new ResponseEntity<>(message.getMessage(), message.getHttpStatus());
    }

    // END PUBLIC INTERFACE //

    private MessageI createTcpService(String host, int hostPort) {

        // If doesn't exist, start service requested my caller
        String mapKey = MapUtil.getMapKey(host,hostPort);
        MessageI message = tcpService.pollTcpService(mapKey);

        if (message.equals(ErrorMessageEnum.NOT_RUNNING_TCP_SERVICE)) {
            message = tcpService.startTcpService(host,hostPort);
        }

        return message;
    }

}
