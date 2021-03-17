package com.huto.hemomancy.particle.util;

import com.huto.hemomancy.util.Vector3;

public class Segment {
	private Vector3 startPoint;
	private Vector3 endPoint;
	private Vector3 diff;
	public final float light;

	public Segment(Vector3 start, Vector3 end, float light) {
		this.startPoint = start;
		this.endPoint = end;
		this.light = light;
		this.calcDiff();
	}

	public Segment(Vector3 start, Vector3 end) {
		this(start, end, 1.0f);
	}

	private void calcDiff() {
		this.diff = this.endPoint.subtract(this.startPoint);
	}

	public Vector3 getDiff() {
		return this.diff;
	}

	public Vector3 getStart() {
		return this.startPoint;
	}

	public Vector3 getEnd() {
		return this.endPoint;
	}
}
