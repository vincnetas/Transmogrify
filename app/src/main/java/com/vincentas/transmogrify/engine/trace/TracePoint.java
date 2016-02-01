package com.vincentas.transmogrify.engine.trace;

/**
 * Created by vincentas on 1/27/16.
 */
public class TracePoint {

    public float x;

    public float y;

    public long time;

    public TracePoint(float x, float y) {
        this(x, y , System.currentTimeMillis());
    }

    public TracePoint(float x, float y, long time) {
        this.x = x;
        this.y = y;
        this.time = time;
    }
}
