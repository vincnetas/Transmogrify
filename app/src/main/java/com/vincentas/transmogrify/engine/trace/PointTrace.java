package com.vincentas.transmogrify.engine.trace;

import com.vincentas.transmogrify.engine.trace.Trace;

public class PointTrace implements Trace {

	private int x, y;
	
	public PointTrace(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public int getX(int step) {
		return x;
	}

	@Override
	public int getY(int step) {
		return y;
	}

	@Override
	public void setSteps(int steps) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getLength() {
		return 0;
	}

	@Override
	public int[] getXY(int step) {
		return new int[]{getX(step), getY(step)};
	}
}
