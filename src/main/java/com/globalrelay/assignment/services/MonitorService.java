package com.globalrelay.assignment.services;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@ComponentScan("com.globalrelay.assignment.services")
public class MonitorService {

    private CallerService callerService;

    public MonitorService(CallerService callerService) {
        this.callerService = callerService;
    }

    @PostConstruct
    public void start() {
        monitor();
    }

    @Scheduled(fixedRate = 500)
    public void monitor() {
        callerService.notifyCallers();
    }
}
