/*
 * Copyright(c) 2018 TaxToken; all rights reserved
 */

package com.globalrelay.assignment.common;

import org.springframework.http.HttpStatus;

public enum SuccessMessageEnum implements MessageI {

    START_TCP_SERVICE("Success starting service"),
    RUNNING_TCP_SERVICE("Success TCP service is running"),
    STOP_TCP_SERVICE("Success stopping service"),
    DELETE_TCP_SERVICE("Success deleting service"),
    CREATE_CALLER("Success creating caller"),
    DELETE_CALLER("Success deleting caller");

    private String message;

    SuccessMessageEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return HttpStatus.OK;
    }

}
