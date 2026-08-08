package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

enum VesperFightOuterRingDetail {
	LOW_POLY(1),
	HIGH_RES(VesperFightStoneSurface.SUBDIVISIONS);

	private final int subdivisions;

	VesperFightOuterRingDetail(int subdivisions) {
		this.subdivisions = subdivisions;
	}

	static VesperFightOuterRingDetail fromLowPolyToggle(boolean useLowPoly) {
		return useLowPoly ? LOW_POLY : HIGH_RES;
	}

	int subdivisions() {
		return subdivisions;
	}

	boolean writesDepth() {
		return true;
	}
}
