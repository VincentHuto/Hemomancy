package com.vincenthuto.hemomancy.common.world.fold;

public final class FoldVector {
	public final double x;
	public final double y;
	public final double z;

	public FoldVector(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public FoldVector add(FoldVector other) {
		return new FoldVector(this.x + other.x, this.y + other.y, this.z + other.z);
	}

	public FoldVector scale(double scalar) {
		return new FoldVector(this.x * scalar, this.y * scalar, this.z * scalar);
	}
}
