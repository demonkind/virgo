package com.huifu.virgo.common.exception;

public class VirgoException extends Exception {

    private static final long serialVersionUID = -1170009459100492182L;

    public VirgoException(String message) {
        super(message);
    }

    public VirgoException(Throwable cause) {
        super(cause);
    }

    public VirgoException(String message, Throwable cause) {
        super(message, cause);
    }
}
