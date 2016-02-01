package com.vincentas.transmogrify.engine.trace;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class MultiPointTrace implements Trace, Serializable {

	private float ds;
	
	private int steps = 0;
	
	private int[][] data;

    private long [] times;

    public MultiPointTrace(List<TracePoint> points) {
        int[][] data = new int[points.size()][];
        times = new long[data.length];
        long firstTime = points.get(0).time;

        for (int i = 0; i < points.size(); i++) {
            TracePoint point = points.get(i);
            data[i] = new int[] {(int) point.x, (int) point.y};

            times[i] = point.time - firstTime;
        }

        this.data = data;
    }

	private int getSegment(int step) {
		long ct = (long) (ds * step);
        int index = Arrays.binarySearch(times, ct);
        if (index < 0) {
            index = -(index + 1) - 1;
        }

        return index;
	}
	
	@Override
	public int getX(int step) {
		int index = getSegment(step);

        int dx = data[index + 1][0] - data[index][0];
        float ct = ds * step;
        float dt = ct - times[index];
        long dur = times[index + 1] - times[index];
        return (int) (data[index][0] + dx * (dt / dur));
	}

	@Override
	public int getY(int step) {
        int index = getSegment(step);
        int dy = data[index + 1][1] - data[index][1];
        float ct = ds * step;

        float dt = ct - times[index];
        long dur = times[index + 1] - times[index];

        return (int) (data[index][1] + dy * (dt / dur));
	}

    private int[] result = new int[2];

    @Override
    public int[] getXY(int step) {
        int index = getSegment(step);

        float ct = ds * step;
        float dt = ct - times[index];
        long dur = times[index + 1] - times[index];

        int dx = data[index + 1][0] - data[index][0];
        result[0] = (int) (data[index][0] + dx * (dt / dur));

        int dy = data[index + 1][1] - data[index][1];
        result[1] = (int) (data[index][1] + dy * (dt / dur));

        return result;
    }

    @Override
	public void setSteps(int steps) {
		this.steps = steps;
		ds = (float)times[times.length - 1] / steps;
	}

	@Override
	public int getLength() {
		throw new Error("Not implemented");
	}
}
