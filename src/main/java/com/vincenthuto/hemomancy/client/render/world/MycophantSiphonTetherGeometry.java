package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantVisualRules;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * The Spawn Mother's feeding siphon latched onto a player: a sagging tether from the siphon's mouth to the victim's
 * chest. Drawn back to front in the Riposte manner: a broad translucent shell, a pale core that swallows in
 * heartbeat pulses, and a dark clot strand twisting round it.
 */
final class MycophantSiphonTetherGeometry {
	private static final int CORE = 0xFFD2C4;
	private static final int CLOT = 0x7A1020;
	private static final int SHELL = 0xFFFFFF;
	private static final int STATIONS = 14;

	private MycophantSiphonTetherGeometry() {
	}

	/** Adds every latched tether near the camera; points are camera-relative, so the camera argument is the origin. */
	static void collect(ClientLevel level, PoseStack poses, VertexConsumer vertices, Vec3 camera, double time, float partial) {
		for (MycophantEntity boss : level.getEntitiesOfClass(MycophantEntity.class, new AABB(camera, camera).inflate(96.0D))) {
			int id = boss.getSiphonTargetId();
			if (id < 0) continue;
			Entity target = level.getEntity(id);
			if (target == null) continue;
			Vec3 base = boss.getPosition(partial);
			float yaw = Mth.rotLerp(partial, boss.yBodyRotO, boss.yBodyRot) * Mth.DEG_TO_RAD;
			Vec3 forward = new Vec3(-Mth.sin(yaw), 0.0D, Mth.cos(yaw));
			Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
			double[] at = MycophantVisualRules.siphonMouth();
			Vec3 mouth = base.add(right.scale(at[0])).add(0.0D, at[1], 0.0D).add(forward.scale(at[2]));
			Vec3 chest = target.getPosition(partial).add(0.0D, target.getBbHeight() * 0.62D, 0.0D);
			draw(poses, vertices, mouth.subtract(camera), chest.subtract(camera), time, boss.getId());
		}
	}

	private static void draw(PoseStack poses, VertexConsumer vertices, Vec3 from, Vec3 to, double time, int seed) {
		double distance = from.distanceTo(to);
		float sag = MycophantVisualRules.tetherSag((float) distance);
		List<Vec3> spine = new ArrayList<>(STATIONS + 1);
		for (int i = 0; i <= STATIONS; i++) {
			double t = (double) i / STATIONS;
			double droop = Math.sin(Math.PI * t) * sag;
			double sway = Math.sin(time * 0.15D + t * 4.0D + seed) * 0.06D;
			spine.add(from.lerp(to, t).add(sway, -droop, sway * 0.5D));
		}
		float pulse = MycophantVisualRules.tetherPulse((float) time);
		LuxUmbraGeometry.ribbonPath(poses, vertices, spine, Vec3.ZERO, 0.24D, 0.16D, 0, seed, SHELL, 0.28F);
		LuxUmbraGeometry.ribbonPath(poses, vertices, spine, Vec3.ZERO, 0.075D + pulse * 0.04D, 0.05D, 1, seed + 3, CORE,
				0.75F + pulse * 0.25F);
		List<Vec3> clot = new ArrayList<>(STATIONS + 1);
		for (int i = 0; i <= STATIONS; i++) {
			double t = (double) i / STATIONS;
			double angle = t * Math.PI * 6.0D - time * 0.4D;
			clot.add(spine.get(i).add(Math.cos(angle) * 0.07D, Math.sin(angle) * 0.07D, Math.sin(angle) * 0.05D));
		}
		LuxUmbraGeometry.ribbonPath(poses, vertices, clot, Vec3.ZERO, 0.03D, 0.02D, 1, seed + 7, CLOT, 0.9F);
	}
}
