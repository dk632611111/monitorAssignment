
package com.monitor.assignment.common;

import org.springframework.http.HttpStatus;

public enum ErrorMessageEnum implements MessageI {


    UNABLE_TO_START_TCP_SERVICE("Error: TCP service already exist"),
    NOT_RUNNING_TCP_SERVICE("Error: TCP service is NOT running"),
    UNABLE_TO_STOP_TCP_SERVICE("Error: unable to stop service"),
    UNABLE_TO_DELETE_TCP_SERVICE("Error: unable to delete service"),
    UNABLE_TO_DELETE_CALLER("Error: unable to delete caller"),
    POLL_RATE_EXCEEDED("Error: poll rate has been exceeded");

    private String message;

    ErrorMessageEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_ACCEPTABLE;
    }
}
