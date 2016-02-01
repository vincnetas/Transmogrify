package com.vincentas.transmogrify.engine.trace;

import com.vincentas.transmogrify.engine.trace.Trace;

public class LineTrace implements Trace {

	private int steps;
	
	private float dx;
	
	private float dy;
	
	private int x1, y1, x2, y2;
	
	public LineTrace(int x1, int y1, int x2, int y2) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}
	
	@Override
	public int getX(int step) {
		return (int) (x1 + (dx * step));
	}

	@Override
	public int getY(int step) {
		return (int) (y1 + (dy * step));
	}

	@Override
	public void setSteps(int steps) {
		this.steps = steps;
		dx = (float) (x2 - x1) / steps;
		dy = (float) (y2 - y1) / steps;	
	}

	@Override
	public int getLength() {
		throw new Error("Not implemented");
	}

	@Override
	public int[] getXY(int step) {
		return new int[]{getX(step), getY(step)};
	}

}
