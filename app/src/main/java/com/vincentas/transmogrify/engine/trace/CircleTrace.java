package com.vincentas.transmogrify.engine.trace;

import com.vincentas.transmogrify.engine.trace.Trace;

public class CircleTrace implements Trace {

    private int steps;

    private int x, y, r, from, to;

    private double da;

    public CircleTrace(int x, int y, int r, int from, int to) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.from = from;
        this.to = to;
    }

    @Override
    public int getX(int step) {
        return (int) (x + Math.cos(Math.toRadians(from + da * step)) * r);
    }

    @Override
    public int getY(int step) {
        return (int) (y + Math.sin(Math.toRadians(from + da * step)) * r);
    }

    @Override
    public int[] getXY(int step) {
        return new int[]{getX(step), getY(step)};
    }

    @Override
    public void setSteps(int steps) {
        this.steps = steps;
        da = (double) (to - from) / steps;
    }

    @Override
    public int getLength() {
        throw new Error("Not implemented");
    }

}
