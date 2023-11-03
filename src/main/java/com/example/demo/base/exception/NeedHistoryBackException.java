package com.example.demo.base.exception;

import com.example.demo.base.rsData.RsData;

public class NeedHistoryBackException extends RuntimeException { // Custom Exception
    public NeedHistoryBackException(RsData rs) {
        this(rs.getMsg());
    }

    public NeedHistoryBackException(String msg) {
        super(msg);
    }
}