package com.example.parameterapproval.parameter;

import com.example.parameterapproval.common.BusinessException;

public class ChangeConflictException extends BusinessException {
    public ChangeConflictException(String message) {
        super(message);
    }
}

