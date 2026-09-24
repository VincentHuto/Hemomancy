package com.vincenthuto.hemomancy.common.entity.boss.endgame;

/** Shared grip coordinates and the damage needed to interrupt capture. */
public final class NaeglerophaeonCombatRules {
	public static net.minecraft.world.phys.Vec3 gripPosition(net.minecraft.world.phys.Vec3 feet, double height, float yaw) {
		return feet.add(new net.minecraft.world.phys.Vec3(0, height * .48, .48).yRot((float)-Math.toRadians(yaw)));
	}
	private NaeglerophaeonCombatRules() {}

	public static boolean breaksGrab(float damageDealtDuringGrab) {
		return damageDealtDuringGrab >= 8.0F;
	}
}
