package com.huto.hemomancy.particle.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import com.huto.hemomancy.util.Vector3;

public class ParticleLightningStorage {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ArrayList<Segment> segments = new ArrayList();
	private Vector3 start;
	private Vector3 end;
	private float length;
	private int speed;
	private boolean finalized;
	private Random rand;
	private int age;
	private int maxAge;
	private float maxOffset = 0.5f;

	public ParticleLightningStorage(Vector3 start, Vector3 end, long seed) {
		this.start = start;
		this.end = end;
		this.rand = new Random(seed);
		this.speed = 4;
		this.length = this.end.subtract(this.start).length();
		this.maxAge = 10;
		this.age = 0;
		this.segments.add(new Segment(this.start, this.end));
	}
	
	public ParticleLightningStorage(Vector3 start, Vector3 end, long seed,int bPerTick,int maxAge) {
		this.start = start;
		this.end = end;
		this.rand = new Random(seed);
		this.speed = bPerTick;
		this.length = this.end.subtract(this.start).length();
		this.maxAge = maxAge;
		this.age = 0;
		this.segments.add(new Segment(this.start, this.end));
	}
	

	public void setMaxOffset(float offset) {
		this.maxOffset = offset;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void split() {
		if (this.finalized) {
			return;
		}
		ArrayList<Segment> oldSegments = this.segments;
		this.segments = new ArrayList();
		for (Segment segment : oldSegments) {
			Vector3[] newPoints = new Vector3[3];
			newPoints[0] = segment.getStart();
			newPoints[2] = segment.getEnd();
			Vector3 offset = Vector3.getPerpendicular(segment.getDiff())
					.rotate(this.rand.nextFloat() * 360.0f, segment.getDiff()).normalize();
			float offsetScale = -this.maxOffset + this.rand.nextFloat() * this.maxOffset * 1.0f;
			offset = offset.scale(offsetScale * segment.getDiff().length());
			newPoints[1] = offset = offset.add(segment.getDiff().scale(0.5f)).add(segment.getStart());
			for (int i = 0; i < newPoints.length - 1; ++i) {
				Segment seg = new Segment(newPoints[i], newPoints[i + 1], segment.light);
				this.segments.add(seg);
			}
		}
	}

	public void fractalize() {
		this.fractalize(9);
	}

	public void fractalize(int count) {
		for (int i = 0; i < count; ++i) {
			this.split();
		}
	}

	public void finalize() {
		if (this.finalized) {
			return;
		}
		this.finalized = true;
		Collections.sort(this.segments, new SegmentSorterLightValue());
	}

	public void onUpdate() {
		this.age += this.speed;
		if (this.age > this.maxAge) {
			this.age = this.maxAge;
		}
	}

	public int getAge() {
		return this.age;
	}

	public int getMaxAge() {
		return this.maxAge;
	}

	public void setMaxAge(int maxAge) {
		this.maxAge = maxAge;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public float getLength() {
		return this.length;
	}

	public int numSegments() {
		return this.segments.size();
	}

	public ArrayList<Segment> getSegments() {
		return this.segments;
	}

	public boolean isFinal() {
		return this.finalized;
	}

	class SegmentSorterLightValue implements Comparator<Segment> {
		SegmentSorterLightValue() {
		}

		@Override
		public int compare(Segment a, Segment b) {
			return Float.compare(b.light, a.light);
		}
	}
}