package com.vincentas.transmogrify.engine;

public class TraceHandle {

    private volatile boolean cancel = false;

    public void cancel() {
        cancel = true;
    }

    public boolean isCanceled() {
        return cancel;
    }
}