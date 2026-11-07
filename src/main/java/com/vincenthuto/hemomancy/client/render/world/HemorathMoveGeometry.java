package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.entity.boss.hemorath.HemorathAnimState;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveView;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathHaloPath;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathOpenings;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathPresenceRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathTitheRules;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Hemorath's world geometry read from his synced state each frame (design section 12): the presence's wet sheen and its
 * ring, the leak's drops, the iron chain of Chained Debt and the Tithe with its thread of blood, the streams of Wells
 * Answer and the Last Thirst, and the Spiked Halo in flight. All positions are relative to his feet (the pose is already
 * translated there); the Ferric links and spikes draw in the solid pass, the blood in the translucent one.
 */
final class HemorathMoveGeometry {
	private static final int TINT = HemorathReliquaryGeometry.TINT, CORE = HemorathReliquaryGeometry.CORE,
			CLOT = HemorathReliquaryGeometry.CLOT;
	/** The halo flies about this high above the floor, mid way through its height band. */
	private static final double HALO_HEIGHT = 1.3D;
	private static final double HALO_RADIUS = .84D;
	private static final int HALO_LINKS = 12;
	private static final int DROP_TICKS = 18;
	private static final double CHEST = HemorathEntity.CHEST_HEIGHT;
	/** The well streams run thicker than the ending's road home: they are his focal draw. */
	private static final double STREAM_WIDTH = .24;
	/** The flying halo's teeth: one spike a link pair, scaled by his patches. */
	private static final int HALO_TEETH = 8;

	private HemorathMoveGeometry() {
	}

	static void collect(HemorathEntity boss, HemorathAnimState motion, PoseStack p, VertexConsumer blood, VertexConsumer clot,
			VertexConsumer iron, Vec3 origin, Vec3 camera, Vec3 right, Vec3 up, double time, float partial, boolean solids) {
		if (boss.syncedEndingTick() >= 0) return;
		int seed = boss.getId();
		SaintMoveView view = boss.view();
		HemorathMove move = HemorathMove.byId(view.moveId());
		float moveTime = view.moveTime(partial);
		boolean thirst = boss.thirsting();
		if (!solids) {
			presence(p, blood, camera, boss, seed, thirst);
			leak(p, blood, camera, right, up, motion, seed, time, boss.syncedOpening(), thirst);
		}
		if (move == HemorathMove.CHAINED_DEBT || move == HemorathMove.TITHE_RECKONING) {
			chain(boss, motion, move, moveTime, p, blood, iron, origin, camera, right, up, time, seed, solids);
		}
		if (!solids && (move == HemorathMove.WELLS_ANSWER || thirst)) {
			streams(boss, move, moveTime, p, blood, origin, camera, right, up, time, seed, thirst);
		}
		if (move == HemorathMove.SPIKED_HALO) {
			halo(view, moveTime, p, blood, iron, origin, camera, seed, solids);
		}
	}

	/** A dark wet sheen under him out to the drain's edge, a little denser inside its full-strength ring. */
	private static void presence(PoseStack p, VertexConsumer blood, Vec3 camera, HemorathEntity boss, int seed, boolean thirst) {
		double radius = boss.syncedPresenceRadius();
		if (radius <= 0) return;
		// the Last Thirst's ring at eighteen is a crack circle in HemorathTelegraphPass
		if (thirst) return;
		ground(p, blood, new Vec3(0, .03, 0), HemorathPresenceRules.EDGE_RADIUS, POOL, seed, CLOT, .08F);
		ground(p, blood, new Vec3(0, .035, 0), HemorathPresenceRules.FULL_RADIUS, POOL, seed + 1, CLOT, .12F);
		HemorathReliquaryGeometry.ring(p, blood, HemorathPresenceRules.FULL_RADIUS, .04, camera, seed + 2, .02, TINT, .25F);
	}

	/** Drops from his ribs and fingertips, more the fuller he is; none while he digests. */
	private static void leak(PoseStack p, VertexConsumer blood, Vec3 camera, Vec3 right, Vec3 up, HemorathAnimState motion,
			int seed, double time, int opening, boolean thirst) {
		if (thirst || opening == HemorathOpenings.GORGED.id()) return;
		float swell = motion.swell();
		int drops = 2 + Math.round(swell * 4);
		Vec3 core = motion.core();
		for (int i = 0; i < drops; i++) {
			double phase = ((time / DROP_TICKS + i / (double) drops + seed * .07) % 1 + 1) % 1;
			double a = i * 2.39996 + seed;
			Vec3 from = i % 3 == 2 ? motion.claw() : core.add(Math.cos(a) * .25, -.5, Math.sin(a) * .2);
			Vec3 at = from.add(0, -from.y * phase * phase, 0);
			card(p, blood, at, right, up, .025, .035, FOCUS, seed + i, TINT, (float) (.5 * (1 - phase * .6)));
			if (phase > .9) ground(p, blood, new Vec3(at.x, .03, at.z), .12, POOL, seed + i, CLOT, .2F);
		}
	}

	/** The iron chain from his claw: Chained Debt's hook running the throw, or the Tithe's links, one per 100 debt. */
	private static void chain(HemorathEntity boss, HemorathAnimState motion, HemorathMove move, float moveTime, PoseStack p,
			VertexConsumer blood, VertexConsumer iron, Vec3 origin, Vec3 camera, Vec3 right, Vec3 up, double time, int seed,
			boolean solids) {
		Entity target = boss.level().getEntity(boss.syncedMoveTarget());
		Vec3 claw = motion.claw();
		Vec3 end;
		int links;
		float strength;
		if (move == HemorathMove.CHAINED_DEBT) {
			FootprintFrame frame = boss.view().frame();
			if (frame == null) return;
			int windup = move.timeline(boss.view().inputs()).windup();
			double travel = HemoEasing.clamp01((moveTime - windup) / 8.0);
			if (moveTime < windup - 4) return;
			double[] f = BossFootprint.Shapes.forward(frame);
			double reach = HemorathMove.CHAIN_LENGTH * travel;
			end = new Vec3(frame.origin().x + f[0] * reach, frame.floorY() + 1.0, frame.origin().z + f[1] * reach)
					.subtract(origin);
			if (target != null && moveTime > windup + 8) end = target.position().add(0, target.getBbHeight() * .55, 0)
					.subtract(origin);
			links = Math.max(2, (int) Math.round(claw.distanceTo(end) / .3));
			strength = 1;
		} else {
			if (target == null) return;
			end = target.position().add(0, target.getBbHeight() * .55, 0).subtract(origin);
			links = Math.max(1, HemorathTitheRules.links(boss.syncedTargetDebt()));
			strength = HemoEasing.clamp01(moveTime / 6.0F);
		}
		if (solids) {
			HemorathReliquaryGeometry.debtLinks(p, iron, claw, end, links, seed, strength);
			if (move == HemorathMove.TITHE_RECKONING) {
				for (int i = 0; i < links; i++) {
					double a = i * Math.PI * 2 / links + time * .02;
					Vec3 at = end.add(Math.cos(a) * .4, -.1, Math.sin(a) * .4);
					Vec3 tangent = new Vec3(-Math.sin(a), 0, Math.cos(a));
					HemorathReliquaryGeometry.link(p, iron, at, tangent.scale(.12 * strength), new Vec3(0, .07 * strength, 0),
							seed + 40 + i);
				}
			}
			return;
		}
		ribbonPath(p, blood, List.of(claw, claw.lerp(end, .5).add(0, -.15, 0), end), camera, .012, RIBBON, seed, CLOT,
				.55F * strength);
	}

	/** Wells Answer's streams lifting off the drawn wells, then running up into his chest; the Last Thirst's, from all. */
	private static void streams(HemorathEntity boss, HemorathMove move, float moveTime, PoseStack p, VertexConsumer blood,
			Vec3 origin, Vec3 camera, Vec3 right, Vec3 up, double time, int seed, boolean thirst) {
		List<BlockPos> wells = boss.syncedWellPositions();
		if (wells.isEmpty()) return;
		int mask = thirst && move != HemorathMove.WELLS_ANSWER ? 0xF : HemorathMove.wells(boss.syncedMoveVariant());
		double reach;
		if (thirst && move != HemorathMove.WELLS_ANSWER) reach = 1;
		else {
			int windup = move.timeline(boss.view().inputs()).windup();
			reach = HemoEasing.smoothstep(HemoEasing.clamp01(moveTime / windup)) * .35
					+ HemoEasing.smoothstep(HemoEasing.clamp01((moveTime - windup) / 6.0)) * .65;
		}
		Vec3 chest = new Vec3(0, CHEST, 0);
		for (int i = 0; i < wells.size(); i++) {
			if ((mask & (1 << i)) == 0) continue;
			float level = boss.syncedWell(i) / 1000.0F;
			float strength = .45F + .55F * Math.max(.2F, level);
			Vec3 from = Vec3.atBottomCenterOf(wells.get(i)).add(0, .9, 0).subtract(origin);
			// a fuller well drives a fatter stream; the surface breaks into a crown where it lifts off
			HemorathBloodGeometry.stream(p, blood, from, chest, camera, right, up, time, seed + i * 11, reach, 1.4,
					STREAM_WIDTH * (.75 + .5 * Math.max(.2F, level)), HemorathReliquaryGeometry.STREAM_SPEED, strength);
			HemorathBloodGeometry.crown(p, blood, from.add(0, -.3, 0), camera, right, up, time, seed + i * 11 + 5, .7, .5,
					strength * (float) Math.min(1.0, reach * 3.0));
		}
	}

	/** The Spiked Halo in flight along the path the hit reads, in iron, with a short trail behind it. */
	private static void halo(SaintMoveView view, float moveTime, PoseStack p, VertexConsumer blood, VertexConsumer iron,
			Vec3 origin, Vec3 camera, int seed, boolean solids) {
		FootprintFrame frame = view.frame();
		if (frame == null) return;
		int windup = HemorathMove.SPIKED_HALO.timeline(view.inputs()).windup();
		double age = moveTime - windup;
		if (age < 0 || age > HemorathHaloPath.FLIGHT_TICKS) return;
		Vec3 at = world(frame, HemorathHaloPath.at(age)).subtract(origin);
		double spin = age * .5;
		int patches = HemorathMove.patches(view.inputs().variant());
		if (solids) {
			for (int i = 0; i < HALO_LINKS; i++) {
				double a = spin + i * Math.PI * 2 / HALO_LINKS;
				Vec3 offset = new Vec3(Math.cos(a) * HALO_RADIUS, Math.sin(a * 2) * .04, Math.sin(a) * HALO_RADIUS);
				Vec3 tangent = new Vec3(-Math.sin(a), 0, Math.cos(a));
				HemorathReliquaryGeometry.link(p, iron, at.add(offset), tangent.scale(.24), new Vec3(0, .1, 0), seed + i);
			}
			// teeth: real spikes driven out of the ring's rim, longer the more iron he has grown
			for (int i = 0; i < HALO_TEETH; i++) {
				double a = spin + (i + .5) * Math.PI * 2 / HALO_TEETH;
				Vec3 out = new Vec3(Math.cos(a), 0, Math.sin(a));
				Vec3 root = at.add(out.scale(HALO_RADIUS * .92));
				p.pushPose();
				p.translate(root.x, root.y, root.z);
				p.mulPose(new Quaternionf().rotationTo(new Vector3f(0, 1, 0), new Vector3f((float) out.x, 0, (float) out.z)));
				HemorathIronGeometry.spike(p, iron, Vec3.ZERO, new HemorathFissureRules.Spike(0, 0, .5 + .05 * patches, .12, 0, 0,
						5, i), 1, 1, 0, HemorathIronGeometry.IRON, seed + 50 + i);
				p.popPose();
			}
			return;
		}
		List<Vec3> trail = new ArrayList<>();
		for (int i = 6; i >= 0; i--) trail.add(world(frame, HemorathHaloPath.at(Math.max(0, age - i * .7))).subtract(origin));
		ribbonPath(p, blood, trail, camera, .04, .34, SLASH, seed, TINT, .8F);
		ribbonPath(p, blood, trail, camera, .02, .16, SLASH, seed + 3, CLOT, .85F);
		ribbonPath(p, blood, trail, camera, .008, .06, SLASH, seed + 1, CORE, .7F);
		// the spinning rim smears into a ring of blood flung off its teeth
		List<Vec3> blur = new ArrayList<>(25);
		for (int i = 0; i <= 24; i++) {
			double a = spin + i * Math.PI * 2 / 24;
			blur.add(at.add(Math.cos(a) * (HALO_RADIUS + .25), 0, Math.sin(a) * (HALO_RADIUS + .25)));
		}
		ribbonPath(p, blood, blur, camera, .13, SLASH, seed + 2, TINT, .5F);
		for (int i = 0; i < 6; i++) {
			double phase = (age * .25 + i / 6.0) % 1;
			double a = spin + i * 1.05;
			Vec3 fling = at.add(Math.cos(a) * (HALO_RADIUS + .3 + phase), -phase * phase * .6, Math.sin(a) * (HALO_RADIUS + .3 + phase));
			card(p, blood, fling, right(camera, at), UP_AXIS, .05, .06, FOCUS, seed + 10 + i, TINT, (float) (1 - phase) * .8F);
		}
	}

	private static final Vec3 UP_AXIS = new Vec3(0, 1, 0);

	/** A horizontal right for camera-facing cards drawn without the camera's own axes. */
	private static Vec3 right(Vec3 camera, Vec3 at) {
		Vec3 view = at.subtract(camera);
		Vec3 side = new Vec3(-view.z, 0, view.x);
		return side.lengthSqr() < 1e-6 ? new Vec3(1, 0, 0) : side.normalize();
	}

	/** A point of the halo's path ({along, right}) in the world, at its flying height. */
	private static Vec3 world(FootprintFrame frame, double[] local) {
		double[] f = BossFootprint.Shapes.forward(frame);
		return new Vec3(frame.origin().x + f[0] * local[0] - f[1] * local[1], frame.floorY() + HALO_HEIGHT,
				frame.origin().z + f[1] * local[0] + f[0] * local[1]);
	}
}
