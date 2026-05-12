package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

public final class MnemonicWhaleTuning {
	public static final float HITBOX_WIDTH = 4.2F;
	public static final float HITBOX_HEIGHT = 1.75F;
	public static final float RENDER_SHADOW_RADIUS = 1.7F;
	public static final double MOVEMENT_SPEED = 0.48D;
	public static final double CRUISE_SPEED_MODIFIER = 0.72D;
	public static final int CRUISE_INTERVAL_TICKS = 35;
	public static final int CRUISE_MAX_TICKS = 180;
	public static final int CRUISE_MIN_HORIZONTAL_DISTANCE = 10;
	public static final int CRUISE_HORIZONTAL_VARIANCE = 18;
	public static final int CRUISE_MIN_DEPTH_BELOW_SEA_LEVEL = 7;
	public static final int CRUISE_BASE_DEPTH_BELOW_SEA_LEVEL = 11;
	public static final int CRUISE_RANDOM_DIVE_DEPTH = 10;
	public static final int CRUISE_MIN_FLOOR_CLEARANCE = 6;
	public static final int CRUISE_TARGET_ATTEMPTS = 12;
	public static final float TAIL_FLUKE_VERTICAL_SWING_RADIANS = 0.18F;
	public static final float REAR_BODY_VERTICAL_SWING_RADIANS = 0.07F;
	public static final double FLOOR_LIFT_SPEED = 0.035D;
	public static final double SHALLOW_WATER_DOWNWARD_ACCELERATION = 0.02D;
	public static final double MAX_SHALLOW_DIVE_SPEED = 0.055D;
	public static final int SMOOTH_SWIM_MAX_TURN_X = 18;
	public static final int SMOOTH_SWIM_MAX_TURN_Y = 12;
	public static final float SMOOTH_SWIM_IN_WATER_SPEED_MODIFIER = 0.04F;
	public static final float SMOOTH_SWIM_OUT_OF_WATER_SPEED_MODIFIER = 0.1F;

	public static final int MODEL_BODY_SEGMENTS = 3;
	public static final int MODEL_HEAD_WIDTH_UNITS = 20;
	public static final int MODEL_MID_BODY_WIDTH_UNITS = 16;
	public static final int MODEL_REAR_BODY_WIDTH_UNITS = 14;
	public static final int MODEL_TOTAL_LENGTH_UNITS = 84;

	private MnemonicWhaleTuning() {
	}
}
