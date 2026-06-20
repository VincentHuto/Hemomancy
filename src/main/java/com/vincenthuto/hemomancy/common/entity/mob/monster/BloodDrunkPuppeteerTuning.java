package com.vincenthuto.hemomancy.common.entity.mob.monster;

public final class BloodDrunkPuppeteerTuning {
	public static final int DOLL_COUNT = 4;
	public static final double DOLL_OWNER_SEARCH_RANGE = 56.0;
	public static final double DOLL_FOLLOW_START_DISTANCE = 7.0;
	public static final double DOLL_TELEPORT_DISTANCE = 40.0;
	public static final double DOLL_FOLLOW_SPEED = 1.12;
	public static final double DOLL_ATTACK_SPEED = 1.18;
	public static final float PUPPETEER_AVOID_PLAYER_DISTANCE = 6.0F;
	public static final boolean SUMMONED_DOLLS_DROP_LOOT = false;

	private static final double[][] DOLL_OFFSETS = {
			{ 1.2, 0.05, 0.0 },
			{ -1.2, 0.05, 0.0 },
			{ 0.0, 0.05, 1.2 },
			{ 0.0, 0.05, -1.2 }
	};

	private BloodDrunkPuppeteerTuning() {
	}

	public static double[] dollSpawnOffset(int index) {
		return DOLL_OFFSETS[Math.floorMod(index, DOLL_OFFSETS.length)].clone();
	}
}
