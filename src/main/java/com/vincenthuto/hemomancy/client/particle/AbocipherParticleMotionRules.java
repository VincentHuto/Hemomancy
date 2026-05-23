package com.vincenthuto.hemomancy.client.particle;

final class AbocipherParticleMotionRules {
	static final int MIN_LIFETIME_TICKS = 95;
	static final int LIFETIME_TICK_RANGE = 46;
	static final double MIN_SWIM_SPEED = 0.045D;
	static final double SWIM_SPEED_RANGE = 0.035D;
	static final double UPWARD_DRIFT_SCALE = 0.20D;
	static final double INHERITED_HORIZONTAL_DRIFT_SCALE = 0.45D;
	static final double VERTICAL_BOB_STRENGTH = 0.012D;

	private AbocipherParticleMotionRules() {
	}
}
