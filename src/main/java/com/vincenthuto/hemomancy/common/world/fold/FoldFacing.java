package com.vincenthuto.hemomancy.common.world.fold;

public enum FoldFacing {
	NORTH(0, -1, -1, 0),
	SOUTH(0, 1, 1, 0),
	WEST(-1, 0, 0, -1),
	EAST(1, 0, 0, 1);

	private final int forwardX;
	private final int forwardZ;
	private final int rightX;
	private final int rightZ;

	FoldFacing(int forwardX, int forwardZ, int rightX, int rightZ) {
		this.forwardX = forwardX;
		this.forwardZ = forwardZ;
		this.rightX = rightX;
		this.rightZ = rightZ;
	}

	public int forwardX() {
		return forwardX;
	}

	public int forwardZ() {
		return forwardZ;
	}

	public int rightX() {
		return rightX;
	}

	public int rightZ() {
		return rightZ;
	}
}
