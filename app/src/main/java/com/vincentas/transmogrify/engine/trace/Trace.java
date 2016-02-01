package com.vincentas.transmogrify.engine.trace;

public interface Trace {

    int getX(int step);

    int getY(int step);

    int[] getXY(int step);

    void setSteps(int steps);

    int getLength();
}
