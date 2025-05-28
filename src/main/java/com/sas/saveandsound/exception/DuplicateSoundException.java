package com.sas.saveandsound.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateSoundException extends RuntimeException {
    public DuplicateSoundException(String message) {
        super(message);
    }
}
