package com.huifu.virgo.common.exception;

public class VirgoPersistException extends RuntimeException {

    private static final long serialVersionUID = -1170009459100492182L;

    public VirgoPersistException(String message) {
        super(message);
    }

    public VirgoPersistException(Throwable cause) {
        super(cause);
    }

    public VirgoPersistException(String message, Throwable cause) {
        super(message, cause);
    }
}
