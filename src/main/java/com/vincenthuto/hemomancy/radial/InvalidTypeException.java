/*
 * Decompiled with CFR 0.151.
 */
package com.vincenthuto.hemomancy.radial;

public class InvalidTypeException
extends RuntimeException {
    public InvalidTypeException() {
    }

    public InvalidTypeException(String message) {
        super(message);
    }

    public InvalidTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidTypeException(Throwable cause) {
        super(cause);
    }

    public InvalidTypeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

