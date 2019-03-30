
package com.monitor.assignment.common;

import org.springframework.http.HttpStatus;

public interface MessageI {

    String getMessage();

    HttpStatus getHttpStatus();
}
