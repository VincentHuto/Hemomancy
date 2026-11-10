package com.vincenthuto.hemomancy.client.render.layer.mob.endgame;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;

/** Handed transforms for Vesper's rendered living arsenal. */
public final class VesperWeaponGripRules {
	private VesperWeaponGripRules() { }

	public static float yawDegrees(EnumBloodTemper temper, boolean leftHand) {
		return leftHand ? 0.0F : 180.0F;
	}
}
