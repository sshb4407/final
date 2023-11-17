package com.yk.Motivation.base.exception;

import com.yk.Motivation.base.rsData.RsData;

public class NeedHistoryBackException extends RuntimeException { // Custom Exception
    public NeedHistoryBackException(RsData rs) {
        this(rs.getMsg());
    }

    public NeedHistoryBackException(String msg) {
        super(msg);
    }
}