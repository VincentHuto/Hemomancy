package com.vincenthuto.hemomancy.common.world.fold;

public enum FoldViewerSide {
	FRONT(1),
	BACK(-1);

	private final int visualDepthSign;

	FoldViewerSide(int visualDepthSign) {
		this.visualDepthSign = visualDepthSign;
	}

	public int visualDepthSign() {
		return visualDepthSign;
	}
}
