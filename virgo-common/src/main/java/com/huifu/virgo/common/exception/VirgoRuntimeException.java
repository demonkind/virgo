package com.huifu.virgo.common.exception;

public class VirgoRuntimeException extends RuntimeException {

    /**  */
    private static final long serialVersionUID = -1170009459100492182L;

    public VirgoRuntimeException(String message) {
        super(message);
    }

    public VirgoRuntimeException(Throwable cause) {
        super(cause);
    }

    public VirgoRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

}
