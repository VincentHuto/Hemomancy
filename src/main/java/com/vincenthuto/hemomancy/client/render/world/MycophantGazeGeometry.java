package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.MycophantAnimState;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantAttack;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantControllerRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantState;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantStrikeRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantVisualRules;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * The Gaze of the Many Eyes: a beam from the pulp along the synced sweep yaw, pitched at the body centre of the nearest
 * player (where the server's cover raycast aims) and stopped by the first block it meets, so a tumor visibly takes it.
 * Drawn back to front in the Riposte manner on the Lux material: a broad gold shell that wavers with a heartbeat, a hot
 * pale core, a dark clot strand winding round it, the eyes at the root and a burn where it lands. Only drawn through
 * the Gaze's active stage, opening over its first ticks and fraying over its last.
 */
final class MycophantGazeGeometry {
	private static final int SHELL = LuxUmbraGeometry.GOLD;
	private static final int CORE = 0xFFD2C4;
	private static final int CLOT = 0x7A1020;
	private static final int STATIONS = 16;
	private static final int EYES = 5;
	private static final float OPEN_TICKS = 3.0F;
	private static final float FRAY_TICKS = 5.0F;
	private static final Vec3 UP = new Vec3(0.0D, 1.0D, 0.0D);

	private MycophantGazeGeometry() {
	}

	/** Adds every sweeping Gaze near the camera; points are camera-relative, so the camera argument is the origin. */
	static void collect(ClientLevel level, PoseStack poses, VertexConsumer vertices, Vec3 camera, double time, float partial) {
		for (MycophantEntity boss : level.getEntitiesOfClass(MycophantEntity.class, new AABB(camera, camera).inflate(96.0D))) {
			if (boss.getState() != MycophantState.ATTACK || boss.getAttack() != MycophantAttack.GAZE || boss.isInvisible()) continue;
			MycophantAnimState motion = MycophantAnimState.of(boss);
			motion.update(boss, partial);
			BossMoveTimeline timeline = MycophantControllerRules.timeline(MycophantAttack.GAZE, boss.getPhase(), boss.getAttackVariant());
			float t = motion.attackTime() - timeline.windup();
			if (t < 0.0F || t >= timeline.active()) continue;
			float opacity = HemoEasing.smoothstep(t / OPEN_TICKS);
			float fray = HemoEasing.smoothstep((t - (timeline.active() - FRAY_TICKS)) / FRAY_TICKS);
			if (opacity * (1.0F - fray) <= 0.0F) continue;

			Vec3 base = boss.getPosition(partial);
			float bodyYaw = Mth.rotLerp(partial, boss.yBodyRotO, boss.yBodyRot) * Mth.DEG_TO_RAD;
			Vec3 forward = new Vec3(-Mth.sin(bodyYaw), 0.0D, Mth.cos(bodyYaw));
			Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
			double[] at = MycophantVisualRules.gazeEye();
			Vec3 eye = base.add(right.scale(at[0])).add(0.0D, at[1], 0.0D).add(forward.scale(at[2]));
			Vec3 end = beamEnd(level, boss, eye, motion.gazeYaw(partial) * Mth.DEG_TO_RAD, partial);
			draw(poses, vertices, eye.subtract(camera), end.subtract(camera), right, time, boss.getId(), opacity, fray);
		}
	}

	/** Along the beam's yaw, down to the nearest player's body centre at their distance, and on until a block stops it. */
	private static Vec3 beamEnd(ClientLevel level, MycophantEntity boss, Vec3 eye, float yaw, float partial) {
		Vec3 flat = new Vec3(-Mth.sin(yaw), 0.0D, Mth.cos(yaw));
		double reach = MycophantStrikeRules.GAZE_RANGE;
		double distance = 12.0D;
		double aimY = boss.getY() + 0.9D;
		Player nearest = null;
		for (Player player : level.players()) {
			if (player.isSpectator() || player.distanceToSqr(eye) > reach * reach) continue;
			if (nearest == null || player.distanceToSqr(eye) < nearest.distanceToSqr(eye)) nearest = player;
		}
		if (nearest != null) {
			Vec3 feet = nearest.getPosition(partial);
			distance = Mth.clamp(Math.hypot(feet.x - eye.x, feet.z - eye.z), 3.0D, reach);
			aimY = feet.y + MycophantStrikeRules.gazeTargetHeight(nearest.getBbHeight());
		}
		Vec3 direction = flat.scale(distance).add(0.0D, aimY - eye.y, 0.0D).normalize();
		Vec3 far = eye.add(direction.scale(reach));
		HitResult hit = level.clip(new ClipContext(eye, far, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
		return hit.getType() == HitResult.Type.MISS ? far : hit.getLocation();
	}

	private static void draw(PoseStack poses, VertexConsumer vertices, Vec3 from, Vec3 to, Vec3 bodyRight, double time, int seed,
			float opacity, float fray) {
		double length = from.distanceTo(to);
		if (length < 0.5D) return;
		Vec3 axis = to.subtract(from).scale(1.0D / length);
		Vec3 side = axis.cross(UP);
		side = side.lengthSqr() < 1.0E-6D ? bodyRight : side.normalize();
		Vec3 lift = side.cross(axis).normalize();
		float alpha = opacity * (1.0F - fray);
		float beat = (float) Math.pow(Math.max(0.0D, Math.sin(time * 0.6D + seed)), 4.0D);
		double grow = 1.0D + fray * 1.4D;

		// 1. shell: a broad translucent sheath that wavers along its length and swells on the heartbeat
		List<Vec3> spine = new ArrayList<>(STATIONS + 1);
		double[] widths = new double[STATIONS + 1];
		for (int i = 0; i <= STATIONS; i++) {
			double s = (double) i / STATIONS;
			double waver = Math.sin(time * 0.5D + s * 9.0D + seed) * 0.06D * s;
			Vec3 drift = UP.scale(fray * s * 0.8D);
			spine.add(from.add(axis.scale(length * s)).add(side.scale(waver)).add(lift.scale(waver * 0.6D)).add(drift));
			widths[i] = (0.32D + 0.42D * s + beat * 0.08D) * grow;
		}
		LuxUmbraGeometry.ribbonPath(poses, vertices, spine, Vec3.ZERO, widths, LuxUmbraGeometry.BEAM, seed, SHELL, 0.3F * alpha);

		// 2. core: the hot pale line, pulsing with the heartbeat
		LuxUmbraGeometry.ribbonPath(poses, vertices, spine, Vec3.ZERO, (0.09D + beat * 0.05D) * grow, (0.14D + beat * 0.05D) * grow,
				LuxUmbraGeometry.BEAM, seed + 3, CORE, (0.8F + beat * 0.2F) * alpha);

		// 3. clot: two dark strands winding round the core, out of step with each other
		for (int strand = 0; strand < 2; strand++) {
			List<Vec3> clot = new ArrayList<>(STATIONS + 1);
			for (int i = 0; i <= STATIONS; i++) {
				double s = (double) i / STATIONS;
				double angle = s * length * 1.1D - time * 0.35D + strand * Math.PI;
				double radius = (0.12D + 0.1D * s) * grow;
				clot.add(spine.get(i).add(side.scale(Math.cos(angle) * radius)).add(lift.scale(Math.sin(angle) * radius)));
			}
			LuxUmbraGeometry.ribbonPath(poses, vertices, clot, Vec3.ZERO, 0.035D, 0.025D, LuxUmbraGeometry.RIBBON, seed + 7 + strand,
					CLOT, 0.85F * alpha);
		}

		// 4. the eyes at the root: a focus glow and a fan of open eyes, each flickering on its own
		Vec3[] root = facing(from);
		LuxUmbraGeometry.card(poses, vertices, from, root[0], root[1], 0.5D + beat * 0.1D, 0.5D + beat * 0.1D, LuxUmbraGeometry.FOCUS,
				seed, CORE, 0.9F * alpha);
		for (int i = 0; i < EYES; i++) {
			double spread = (i - (EYES - 1) * 0.5D) * 0.38D;
			Vec3 at = from.add(bodyRight.scale(spread)).add(0.0D, 0.18D - Math.abs(spread) * 0.25D, 0.0D);
			float flicker = 0.7F + 0.3F * (float) Math.sin(time * 0.9D + i * 1.7D + seed);
			Vec3[] card = facing(at);
			LuxUmbraGeometry.card(poses, vertices, at, card[0], card[1], 0.15D, 0.1D, LuxUmbraGeometry.EYE, seed + 11 + i, SHELL,
					flicker * alpha);
		}

		// 5. the burn where it lands, and wisps lifting off it against the sweep
		Vec3[] tip = facing(to);
		LuxUmbraGeometry.card(poses, vertices, to, tip[0], tip[1], 0.7D + beat * 0.2D, 0.7D + beat * 0.2D, LuxUmbraGeometry.FOCUS,
				seed + 19, CORE, 0.75F * alpha);
		LuxUmbraGeometry.ground(poses, vertices, to.add(0.0D, 0.03D, 0.0D), 0.9D * grow, LuxUmbraGeometry.RIPPLE, seed + 23, SHELL, 0.5F * alpha);
		for (int i = 0; i < 4; i++) {
			double rise = (time * 0.08D + i * 0.25D) % 1.0D;
			Vec3 wisp = to.add(side.scale(Math.sin(i * 2.3D + seed) * 0.4D)).add(0.0D, rise * 1.2D, 0.0D);
			Vec3[] card = facing(wisp);
			LuxUmbraGeometry.card(poses, vertices, wisp, card[0], card[1], 0.22D, 0.3D, LuxUmbraGeometry.WISP, seed + 29 + i, SHELL,
					(float) (1.0D - rise) * 0.45F * alpha);
		}
	}

	/** A camera-facing right and up at a camera-relative point. */
	private static Vec3[] facing(Vec3 at) {
		Vec3 view = at.lengthSqr() < 1.0E-6D ? new Vec3(0.0D, 0.0D, 1.0D) : at.normalize();
		Vec3 right = view.cross(UP);
		right = right.lengthSqr() < 1.0E-6D ? new Vec3(1.0D, 0.0D, 0.0D) : right.normalize();
		return new Vec3[] { right, right.cross(view).normalize() };
	}
}
