package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.entity.boss.seraphae.SeraphaeAnimState;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.world.SeraphaeOvergrowthRules.Kind;
import com.vincenthuto.hemomancy.client.render.world.SeraphaeOvergrowthRules.Look;
import com.vincenthuto.hemomancy.client.render.world.SeraphaeOvergrowthRules.Seam;
import com.vincenthuto.hemomancy.client.render.world.SeraphaeOvergrowthRules.Spur;
import com.vincenthuto.hemomancy.client.render.world.SeraphaeTissueGeometry.Ground;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint.Shapes;
import com.vincenthuto.hemomancy.common.entity.boss.BossGroundHazardEntity;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintControllerRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveView;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintRise;
import com.vincenthuto.hemomancy.common.entity.boss.saint.TelegraphSpec;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.ContainmentAnchorEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeDeeds;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeEndingRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeFragmentEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeLeashRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeNerveFlashRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeOpenings;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeRestraint;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

/**
 * Saint Seraphae's own telegraph and hazard pass (visual pass 2, 2026-11-06), run from {@code ManipulationVisualRenderer}
 * in both of its passes. Her {@link SeraphaeTelegraphStyle} claims every move and her pads and paints nothing, so the
 * kit's flat tints never show for her; this pass reads the same synced move, footprint, frame, timeline and reveal the
 * kit does, so every drawn edge is the hit test's edge. Her language is healing that hurts: the floor along a footprint
 * is cut open and swells shut too fast (a lane is one filled furrow, its whole width, never parallel lines), white light
 * wells in it and runs out along vessels, and at the strike gold nerve-fire races across it; bone grows out of the
 * floor and curls; light pours out of her in streams and sheets with body, and is caught in her iron chains, drawn link
 * by link. Lashes and reaches follow the animated limb's own trail ({@link SeraphaeAnimState#tipAt},
 * {@link SeraphaeAnimState#handAt}, which her renderer feeds every frame). Each move runs emerge,
 * build, the commit flash, the strike, the linger and the dissolve ({@link SeraphaeOvergrowthRules#look}). Struck bodies
 * get {@link SeraphaeImpactGeometry}. White is the Lux material and her tissue; gold only ever the Ductilis material.
 */
final class SeraphaeTelegraphPass {
	private static final double RANGE = 64.0D;
	/** The gold last-chance cue starts this many ticks before the commit. */
	private static final float PRE = 4.0F;
	/** Axon Lance dendrites punch up this far apart behind the front, and stand this long past the active span. */
	private static final double LANCE_SITE = 1.4D;
	private static final float LANCE_HOLD = 4.0F;
	/** Regrowth Knot pulses every this many active ticks; its rim spurs wither this soon after each. */
	private static final int KNOT_PULSE = 20;
	private static final float KNOT_SPUR_HOLD = 5.0F;
	/** Wing Grasp: five bone tines this tall close over the disc. */
	private static final int GRASP_TINES = 5;
	private static final double GRASP_TINE = 1.8D;
	/** The Radiance Flood's ring of growth round her. */
	private static final int FLOOD_CLUSTERS = 10;
	/** The ending's closing ring over the tomb. */
	private static final double SEAL_RADIUS = 2.6D;

	/** The pass's frame: where it draws, and in what. Points are camera-relative. */
	record Scene(PoseStack p, VertexConsumer tissue, VertexConsumer lux, VertexConsumer gold, VertexConsumer bone, Vec3 camera,
			Vec3 right, Vec3 up, double time, float partial, boolean solids, ClientLevel level) {
		Vec3 local(Vec3 world) {
			return world.subtract(camera);
		}

		Vec3 local(double x, double y, double z) {
			return new Vec3(x - camera.x, y - camera.y, z - camera.z);
		}

		void seams(Ground g, List<Seam> seams, Look look, float strength, int seed) {
			if (!solids) SeraphaeTissueGeometry.seams(p, tissue, g, seams, look, strength, seed);
		}

		void bloom(Ground g, double r0, double r1, double fromDeg, double toDeg, Look look, float strength, int seed) {
			if (!solids) SeraphaeTissueGeometry.bloom(p, tissue, g, r0, r1, fromDeg, toDeg, look, strength, seed);
		}

		void nerves(Ground g, List<Seam> seams, float grow, float fire, float front, int seed) {
			if (!solids) SeraphaeLightGeometry.nerves(p, gold, g, seams, grow, fire, front, seed);
		}
	}

	/** One running move as the pass draws it. */
	private record Move(SeraphaeEntity boss, SeraphaeMove move, BossFootprint footprint, FootprintFrame frame,
			SaintMoveInputs inputs, float moveTime, float age, int windup, int active, int recovery, int reveal, Look look,
			Ground ground, int seed) {
		/** 0 at the reveal to 1 at the commit. */
		float build() {
			return windup <= reveal ? 1.0F : HemoEasing.clamp01((moveTime - reveal) / (float) (windup - reveal));
		}

		boolean revealed() {
			return moveTime >= reveal;
		}

		/** The gold last-chance cue: rising over the 4 ticks before the commit, full at it, gone over the flash. */
		float cue() {
			if (!revealed() || age < -PRE) return 0.0F;
			if (age < 0.0F) return HemoEasing.smoothstep(1.0F + age / PRE);
			return 1.0F - HemoEasing.clamp01(age / SeraphaeOvergrowthRules.FLASH);
		}

		/** How far the cue's gold has run along its nerves: out through the 4 ticks before the commit. */
		float cueFront() {
			return age >= 0.0F ? 1.0F : HemoEasing.clamp01(1.0F + age / PRE);
		}
	}

	private SeraphaeTelegraphPass() {
	}

	static void collect(ClientLevel level, PoseStack p, LuxUmbraBatch flows, Vec3 camera, Vec3 right, Vec3 up, double time,
			float partial, boolean solids) {
		AABB box = new AABB(camera, camera).inflate(RANGE);
		List<SeraphaeEntity> saints = new ArrayList<>();
		for (SeraphaeEntity boss : level.getEntitiesOfClass(SeraphaeEntity.class, box)) {
			if (boss.isAlive() && boss.kit() != null) saints.add(boss);
		}
		List<BossGroundHazardEntity> hazards = new ArrayList<>();
		for (BossGroundHazardEntity hazard : level.getEntitiesOfClass(BossGroundHazardEntity.class, box)) {
			if (!hazard.isRemoved() && hazard.owner() instanceof SeraphaeEntity) hazards.add(hazard);
		}
		if (saints.isEmpty() && hazards.isEmpty()) return;
		MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
		SeraphaeRenderTypes.begin(time);
		VertexConsumer tissue = FerricDuctilisGeometry.DISCARD, bone = FerricDuctilisGeometry.DISCARD;
		if (solids) {
			bone = buffers.getBuffer(SeraphaeRenderTypes.BONE);
		} else {
			SeraphaeTissueGeometry.begin();
			tissue = buffers.getBuffer(SeraphaeRenderTypes.TISSUE);
		}
		Scene s = new Scene(p, tissue, solids ? FerricDuctilisGeometry.DISCARD : flows.vertices(LuxUmbraBatch.Material.LUX),
				solids ? FerricDuctilisGeometry.DISCARD : flows.vertices(LuxUmbraBatch.Material.DUCTILIS), bone, camera, right, up, time,
				partial, solids, level);
		for (SeraphaeEntity boss : saints) {
			float rise = boss.riseTime(partial);
			if (rise >= 0.0F && rise < SaintRise.TICKS) continue;
			float ending = boss.view().endingTime(partial);
			if (ending >= SeraphaeEndingRules.END_TRIAL) continue;
			SeraphaeAnimState motion = SeraphaeAnimState.of(boss);
			Vec3 feet = boss.getPosition(partial);
			chains(s, boss, motion, feet, ending);
			if (ending >= 0.0F) {
				ending(s, boss, motion, feet, ending);
				continue;
			}
			move(s, boss);
			opening(s, boss, motion, feet);
			hold(s, boss, motion, feet);
			SeraphaeImpactGeometry.collect(s, boss);
		}
		for (BossGroundHazardEntity hazard : hazards) pad(s, hazard);
		if (solids) buffers.endBatch(SeraphaeRenderTypes.BONE);
		else buffers.endBatch(SeraphaeRenderTypes.TISSUE);
	}

	// ── moves ────────────────────────────────────────────────────────

	private static void move(Scene s, SeraphaeEntity boss) {
		SaintMoveView view = boss.view();
		SeraphaeMove move = SeraphaeMove.byId(view.moveId());
		if (move == null) return;
		float moveTime = view.moveTime(s.partial());
		if (moveTime < 0.0F) return;
		SaintMoveInputs inputs = view.inputs();
		BossMoveTimeline timeline = SaintControllerRules.effective(move.kind(), move.timeline(inputs));
		TelegraphSpec spec = move.telegraph(inputs);
		int reveal = SaintControllerRules.revealTick(spec == null ? TelegraphSpec.DEFAULT : spec, timeline, move.kind());
		int windup = timeline.commitTick(), active = timeline.active(), recovery = timeline.recovery();
		if (moveTime > windup + active + recovery) return;
		FootprintFrame frame = view.frame();
		if (frame == null) {
			frame = FootprintFrame.simple(boss.syncedMoveOrigin(), boss.syncedMoveAim(), boss.syncedMoveAim(), boss.getY());
		}
		BossFootprint footprint = move.footprint(inputs);
		Look look = SeraphaeOvergrowthRules.look(moveTime, reveal, windup, active, SeraphaeOvergrowthRules.linger(recovery));
		Ground ground = Ground.of(frame.origin(), Shapes.forward(frame), frame.floorY(), s.camera());
		Move m = new Move(boss, move, footprint, frame, inputs, moveTime, moveTime - windup, windup, active, recovery, reveal,
				look, ground, boss.getId() * 31 + move.id() * 7 + boss.syncedMoveSerial() * 131);
		switch (move) {
			case LAYING_ON_OF_HANDS, STRAINING_REACH -> reach(s, m);
			case NERVE_LASH, COUNTER_LASH -> lash(s, m);
			case AXON_LANCE, AXON_MIRROR -> lance(s, m);
			case REGROWTH_KNOT -> knot(s, m);
			case TRANSFUSION_THREAD -> thread(s, m);
			case WING_GRASP -> grasp(s, m);
			case CONVULSION, CONVULSION_WIDE -> convulsion(s, m);
			case CHAIN_WRENCH -> wrench(s, m);
			case FRACTURE -> fracture(s, m);
			case HEALED_GROUND -> healedGround(s, m);
			case UNCLOSING_EYE -> eye(s, m);
			case EXTRACTION -> extraction(s, m);
			case RADIANCE_FLOOD -> flood(s, m);
		}
	}

	/**
	 * The Laying On of Hands and the Straining Reach: the lane opens as one furrow, its whole width, from her toward the
	 * patient, swelling shut with pale tissue while light wells in it and two vessels run down it from her palms;
	 * vessels branch off its lips into the floor. Where her chains will stop her a swollen ridge crosses the lane round
	 * a knot of new tissue, with a gold bead on it. In the last four ticks gold races down the furrow; through the glide
	 * her hands leave tongues of light along their own paths.
	 */
	private static void reach(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Lane lane)) return;
		double length = lane.length(), half = lane.halfWidth();
		Look look = m.look();
		Ground g = m.ground();
		List<Seam> trench = SeraphaeOvergrowthRules.trench(length, half, m.seed());
		// the light wells in the cut itself (the tissue shader traces it sunken), so nothing lies across its mouth; the
		// nerves run along the swollen lips either side
		List<Seam> lips = lipNerves(length, half, m.seed() + 45, SeraphaeOvergrowthRules.NERVE_FIBRE * 1.4D);
		s.seams(g, trench, look, 1.0F, m.seed());
		s.nerves(g, lips, look.grow(), Math.max(0.12F * m.build(), m.cue()) * look.alpha(), m.cueFront(), m.seed() + 60);
		boolean hidden = SeraphaeDeeds.unwatchedHands(m.boss().syncedDeeds());
		if (!hidden && m.revealed() && m.age() < 0.0F) chainEnd(s, m, length);
		float fade = SeraphaeOvergrowthRules.reachFade(m.age(), m.active());
		if (s.solids() || fade <= 0.0F) return;
		SeraphaeAnimState motion = SeraphaeAnimState.of(m.boss());
		float now = AnimationTime.age(m.boss(), s.partial());
		float commit = now - m.age();
		float end = Math.min(now, commit + m.active());
		float begin = Math.max(commit - 1.0F, end - 7.0F);
		for (boolean left : new boolean[]{true, false}) {
			List<Vec3> path = new ArrayList<>(15);
			for (int i = 0; i <= 14; i++) {
				Vec3 at = motion.handAt(left, begin + (end - begin) * i / 14.0F);
				if (at != null) path.add(s.local(at));
			}
			if (path.size() >= 3) {
				SeraphaeLightGeometry.reach(s.p(), s.lux(), s.gold(), path, Vec3.ZERO, s.right(), s.up(), fade, s.time(),
						m.seed() + (left ? 11 : 23));
			}
		}
	}

	/** A nerve along each swollen lip of a furrow of {@code half} (the lips' crest lies about 0.8 of the half-width out). */
	private static List<Seam> lipNerves(double length, double half, int seed, double width) {
		List<Seam> out = new ArrayList<>(2);
		for (int side = -1; side <= 1; side += 2) {
			out.add(SeraphaeOvergrowthRules.jag(0.3D, side * half * 0.8D, length * 0.97D, side * half * 0.8D, 0.8D, 0.04D, width, 0.0D,
					0.9D, seed + side, true, Kind.FIBRE));
		}
		return out;
	}

	/** Where her chains stop the reach: a swollen ridge across the lane, a knot of new tissue, a gold bead. */
	private static void chainEnd(Scene s, Move m, double length) {
		Vec3 origin = m.frame().origin();
		Vec3 centre = m.boss().syncedLeashCentre();
		double leash = m.boss().syncedLeash() + (m.move() == SeraphaeMove.STRAINING_REACH ? SeraphaeLeashRules.STRAINING_EXTRA : 0.0D);
		Ground g = m.ground();
		double stop = SeraphaeLeashRules.chainStop(origin.x, origin.z, g.fx(), g.fz(), centre.x, centre.z, leash);
		if (stop < 0.3D || stop > length) return;
		float build = m.build();
		List<Seam> ridge = List.of(SeraphaeOvergrowthRules.jag(stop, -1.4D, stop, 1.4D, 0.4D, 0.08D, 0.2D, 0.0D, 0.6D, m.seed() + 70, true,
				Kind.FURROW));
		s.seams(g, ridge, m.look(), 1.0F, m.seed() + 70);
		Vec3 knot = g.local(stop, 0.0D, 0.0D);
		if (s.solids()) {
			SeraphaeBoneGeometry.blister(s.p(), s.bone(), knot, 0.34D * (0.5D + 0.5D * build), 0.3D * build, 0.4F + 0.6F * build,
					SeraphaeBoneGeometry.withLight(SeraphaeBoneGeometry.BLISTER, 0.3F + 0.5F * build), m.seed() + 71);
		} else {
			SeraphaeLightGeometry.spark(s.p(), s.gold(), knot.add(0.0D, 0.4D * build + 0.1D, 0.0D), s.right(), s.up(), 0.07D, m.seed() + 72,
					0.85F * build);
		}
	}

	/**
	 * Nerve Lash and Counter-Lash: the cone's floor swells with overgrowth and a fan of gold nerve fibres; at the strike
	 * the live band swells full under the sweep and a sheet of light stands on its leading edge exactly as high as the
	 * lash reaches (gold crest: jump it) or from crouch height up (gold foot: duck under it); in the air the lash is
	 * laid along the wing tip's own path, nerve-fire down its middle, and the floor throws shards along the sweep.
	 */
	private static void lash(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Sweep sweep)) return;
		boolean low = m.move() == SeraphaeMove.NERVE_LASH;
		double from = -sweep.degrees() / 2.0D, to = sweep.degrees() / 2.0D;
		Look look = m.look();
		Ground g = m.ground();
		List<Seam> fan = SeraphaeOvergrowthRules.fan(sweep.inner(), sweep.radius(), from, to, m.seed());
		boolean sweeping = m.age() >= 0.0F && m.age() <= sweep.sweepTicks() + 1.0F;
		s.bloom(g, sweep.inner(), sweep.radius(), from, to, look, 0.3F, m.seed());
		s.seams(g, fan, look, 1.0F, m.seed());
		float fire = Math.max(0.2F * m.build(), m.cue());
		if (m.age() >= 0.0F && m.age() < m.active()) fire = Math.max(fire, 0.55F);
		s.nerves(g, fan, look.grow(), fire * look.alpha(), 1.0F, m.seed() + 7);
		double bandHalf = sweep.bandDegrees() / 2.0D;
		if (sweeping) {
			double angle = sweep.angle(m.age());
			double edge = angle + (sweep.clockwise() ? bandHalf : -bandHalf);
			s.bloom(g, sweep.inner(), sweep.radius(), angle - bandHalf, angle + bandHalf, new Look(1.0F, 1.1F, 1.0F, 0.8F, 1.0F, 1.0F),
					1.0F, m.seed() + 9);
			if (!s.solids()) {
				float alpha = 1.0F - HemoEasing.ramp(m.age(), sweep.sweepTicks() - 1.0F, sweep.sweepTicks() + 1.0F);
				List<Vec3> feet = radial(g, sweep.inner(), sweep.radius(), edge, 7);
				if (low) {
					SeraphaeLightGeometry.wall(s.p(), s.lux(), s.gold(), feet, 0.0D, BossFootprint.Height.JUMPABLE.max(), Vec3.ZERO, s.time(),
							m.seed() + 11, alpha, false, true);
				} else {
					SeraphaeLightGeometry.wall(s.p(), s.lux(), s.gold(), feet, 1.25D, 2.4D, Vec3.ZERO, s.time(), m.seed() + 11, alpha, true,
							false);
				}
			}
		}
		if (s.solids()) {
			for (int k = 0; k < 4; k++) {
				float at = k * 1.5F;
				double angle = Math.toRadians(sweep.angle(at));
				double r = sweep.radius() * (0.55D + 0.1D * k);
				SeraphaeBoneGeometry.shards(s.p(), s.bone(), g.local(Math.cos(angle) * r, Math.sin(angle) * r, 0.0D), m.age() - at, 1.0D, 3,
						m.seed() + k * 13);
			}
			return;
		}
		// the wing on the variant's side (0 her left, tips 0 to 2; else her right, 3 to 5): the lash's lower trunk, the
		// counter's upper
		lashTrail(s, m, (m.inputs().variant() == 0 ? 0 : 3) + (low ? 2 : 0), sweep.radius());
	}

	/** The lash itself, along the path tip {@code trunk} took from just before the commit through the sweep. */
	private static void lashTrail(Scene s, Move m, int trunk, double radius) {
		float head = SeraphaeOvergrowthRules.lashHead(m.age());
		if (head < 0.0F) return;
		SeraphaeAnimState motion = SeraphaeAnimState.of(m.boss());
		float now = AnimationTime.age(m.boss(), s.partial());
		float commit = now - m.age();
		float end = Math.min(now, commit + SeraphaeOvergrowthRules.LASH_SWEEP - SeraphaeOvergrowthRules.LASH_LEAD + 1.0F);
		float begin = Math.max(commit - SeraphaeOvergrowthRules.LASH_LEAD - 1.5F, end - 6.0F);
		if (end - begin < 0.5F) return;
		List<Vec3> path = new ArrayList<>(15);
		for (int i = 0; i <= 14; i++) {
			Vec3 at = motion.tipAt(trunk, begin + (end - begin) * i / 14.0F);
			if (at != null) path.add(s.local(at));
		}
		if (path.size() < 3 || path.get(0).distanceToSqr(path.get(path.size() - 1)) < 0.25D) return;
		SeraphaeLightGeometry.lash(s.p(), s.lux(), s.gold(), path, Vec3.ZERO, s.right(), s.up(),
				Math.max(0.8D, radius / SeraphaeMove.LASH_RADIUS), head, SeraphaeOvergrowthRules.lashFade(m.age()),
				SeraphaeOvergrowthRules.lashFray(m.age()), s.time(), m.seed() + 300);
	}

	/**
	 * Axon Lance and its mirror: each of the three rays opens as a furrow with a nerve down it; at the strike gold races
	 * out along each at the front's speed with a beam of light behind it at knee height, and dendrite spurs of bone
	 * punch up behind the front; where a ray ends a dendrite fan of bone bursts out with gold forking off it.
	 */
	private static void lance(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Lane lane)) return;
		Look look = m.look();
		double speed = Math.max(0.01D, lane.frontSpeed());
		for (int n = 0; n < lane.lanes(); n++) {
			int index = lane.index(n);
			Ground g = laneGround(m.ground(), lane.offset(index), lane.angle(index));
			int seed = m.seed() + index * 131;
			double front = m.age() < 0.0F ? -1.0D : lane.front(m.age(), index);
			s.seams(g, SeraphaeOvergrowthRules.trench(lane.length(), lane.halfWidth(), seed), look, 1.0F, seed);
			List<Seam> axon = lipNerves(lane.length(), lane.halfWidth(), seed + 5, SeraphaeOvergrowthRules.NERVE_FIBRE * 1.3D);
			float fire = Math.max(0.22F * m.build(), m.cue() * 0.6F);
			if (front >= 0.0D) fire = 1.0F;
			s.nerves(g, axon, look.grow(), fire * look.alpha(), front >= 0.0D ? (float) (front / lane.length()) : 1.0F, seed + 9);
			if (front <= 0.0D) continue;
			float delay = lane.delay(index);
			float hold = m.active() + LANCE_HOLD;
			if (!s.solids() && m.age() < hold + 8.0F) {
				float fade = 1.0F - HemoEasing.ramp(m.age(), hold, hold + 8.0F);
				SeraphaeLightGeometry.stream(s.p(), s.lux(), g.local(0.6D, 0.0D, 0.9D), g.local(Math.max(0.7D, front), 0.0D, 0.9D), Vec3.ZERO,
						s.right(), s.up(), s.time(), seed + 11, 1.0D, 0.0D, 0.16D * SeraphaeOvergrowthRules.beamFlare(m.age() - delay), 0.9D,
						fade);
				if (front < lane.length()) {
					Vec3 head = g.local(front, 0.0D, 0.9D);
					SeraphaeLightGeometry.dendrites(s.p(), s.gold(), head, new Vec3(g.fx(), 0.0D, g.fz()), Vec3.ZERO, 0.6D, seed + 12, 1.0F);
				}
			}
			int k = 0;
			for (double along = 1.0D; along <= front; along += LANCE_SITE, k++) {
				float own = m.age() - (float) (delay + along / speed);
				float witherAt = hold - (float) (delay + along / speed);
				double jitter = (SeraphaeOvergrowthRules.unit(seed, k) - 0.5D) * lane.halfWidth() * 0.6D;
				Vec3 foot = g.local(along, jitter, 0.0D);
				if (s.solids()) {
					List<Spur> spurs = toward(SeraphaeOvergrowthRules.cluster(seed + k * 17, 1.0D + 0.4D * SeraphaeOvergrowthRules.unit(seed, 200 + k), 3),
							g.fx(), g.fz(), 0.25D, 0.15D);
					SeraphaeBoneGeometry.cluster(s.p(), s.bone(), foot, spurs, own, witherAt, s.time(), SeraphaeBoneGeometry.SPUR, seed + k * 17);
					SeraphaeBoneGeometry.shards(s.p(), s.bone(), foot, own, 1.0D, 3, seed + k * 23);
				} else if (own > 0.0F && own < 6.0F) {
					LuxUmbraGeometry.ground(s.p(), s.lux(), foot.add(0.0D, 0.04D, 0.0D), 0.5D, LuxUmbraGeometry.POOL, seed + k,
							SeraphaeLightGeometry.CORE, 0.6F * (1.0F - own / 6.0F));
				}
			}
			float atEnd = m.age() - (float) (delay + lane.length() / speed);
			if (atEnd <= 0.0F) continue;
			Vec3 end = g.local(lane.length(), 0.0D, 0.0D);
			if (s.solids()) {
				List<Spur> fanOut = new ArrayList<>();
				for (Spur spur : SeraphaeOvergrowthRules.cluster(seed + 777, 1.1D, 5)) fanOut.add(spur.toward(spur.leanX(), spur.leanZ(), 0.6D, 0.3D));
				SeraphaeBoneGeometry.cluster(s.p(), s.bone(), end, toward(fanOut, g.fx(), g.fz(), 0.3D, 0.0D), atEnd,
						hold - (float) (delay + lane.length() / speed), s.time(), SeraphaeBoneGeometry.SPUR, seed + 777);
				SeraphaeBoneGeometry.shards(s.p(), s.bone(), end, atEnd, 1.6D, 6, seed + 778);
			} else {
				SeraphaeLightGeometry.dendrites(s.p(), s.gold(), end.add(0.0D, 0.6D, 0.0D), new Vec3(g.fx(), 0.35D, g.fz()), Vec3.ZERO, 1.1D,
						seed + 779, 1.0F - HemoEasing.ramp(atEnd, 4.0F, 10.0F));
			}
		}
	}

	/**
	 * Regrowth Knot: the disc swells with overgrowth and vessels run into it; at its heart a knot of blisters grows
	 * through the wind-up, throbbing. Each pulse rolls a ring of light out to its rim and a ring of small spurs bursts
	 * up round it and withers.
	 */
	private static void knot(Scene s, Move m) {
		Vec3 centre = m.frame().aim();
		Ground g = m.ground().at(centre);
		double radius = SeraphaeMove.KNOT_RADIUS;
		Look look = m.look();
		s.bloom(g, 0.0D, radius, -180.0D, 180.0D, look, 0.35F + 0.55F * m.build(), m.seed());
		s.seams(g, SeraphaeOvergrowthRules.disc(radius, m.seed()), look, 1.0F, m.seed());
		Vec3 heart = s.local(centre.x, m.frame().floorY(), centre.z);
		if (s.solids() && m.revealed()) {
			float grown = (m.age() < 0.0F ? m.build() : 1.0F) * look.alpha();
			double throb = SeraphaeOvergrowthRules.throb(m.moveTime(), s.time(), m.seed());
			for (int i = 0; i < 5; i++) {
				double a = i * 2.39996D + m.seed();
				double out = i == 0 ? 0.0D : 0.35D + 0.15D * SeraphaeOvergrowthRules.unit(m.seed(), i);
				double r = (i == 0 ? 0.5D : 0.3D) * (0.4D + 0.6D * grown) * throb;
				SeraphaeBoneGeometry.blister(s.p(), s.bone(), heart.add(Math.cos(a) * out, 0.0D, Math.sin(a) * out), r, r * 0.8D, grown,
						SeraphaeBoneGeometry.withLight(SeraphaeBoneGeometry.BLISTER, 0.3F + 0.6F * look.light()), m.seed() + i * 7);
			}
		}
		for (int pulse = 0; pulse < m.active(); pulse += KNOT_PULSE) {
			float own = m.age() - pulse;
			if (own < 0.0F || own > SeraphaeOvergrowthRules.SHARD_LIFE) continue;
			int seed = m.seed() + pulse * 3;
			if (s.solids()) {
				for (int i = 0; i < 7; i++) {
					double a = Math.toRadians(i * 360.0D / 7.0D + pulse * 9.0D);
					Vec3 foot = heart.add(Math.cos(a) * radius * 0.88D, 0.0D, Math.sin(a) * radius * 0.88D);
					SeraphaeBoneGeometry.cluster(s.p(), s.bone(), foot, toward(SeraphaeOvergrowthRules.cluster(seed + i * 5, 0.6D, 3),
							Math.cos(a), Math.sin(a), 0.2D, 0.2D), own - i * 0.3F, KNOT_SPUR_HOLD, s.time(), SeraphaeBoneGeometry.SPUR, seed + i * 5);
				}
				SeraphaeBoneGeometry.shards(s.p(), s.bone(), heart, own, 1.2D, 5, seed + 40);
				continue;
			}
			double wave = SeraphaeOvergrowthRules.wave(own, radius);
			if (wave > 0.0D) {
				float t = own / SeraphaeOvergrowthRules.WAVE;
				SeraphaeLightGeometry.curtain(s.p(), s.lux(), s.gold(), heart, new double[]{g.fx(), g.fz()}, wave, -180.0D, 180.0D, 0.0D,
						0.5D * (1.0D - t * 0.6D), Vec3.ZERO, s.time(), seed + 3, 0.9F * (1.0F - t * t), false, false);
			}
			if (own < 4.0F) {
				LuxUmbraGeometry.ground(s.p(), s.lux(), heart.add(0.0D, 0.05D, 0.0D), 0.8D + own * 0.25D, LuxUmbraGeometry.POOL, seed + 4,
						SeraphaeLightGeometry.CORE, 0.8F * (1.0F - own / 4.0F));
			}
		}
	}

	/**
	 * Transfusion Thread: one stream of light with body grows from her heart to the far patient through the wind-up and
	 * hangs there sagging, knots of light running down it into them and gold impulses with them; their chest glows on
	 * each beat. When it ends it frays from their end back toward her.
	 */
	private static void thread(Scene s, Move m) {
		if (s.solids() || !m.revealed()) return;
		Entity target = s.level().getEntity(m.boss().syncedMoveTarget());
		if (target == null) return;
		Vec3 heart = s.local(heart(m.boss(), s.partial()));
		Vec3 chest = s.local(target.getPosition(s.partial()).add(0.0D, target.getBbHeight() * 0.62D, 0.0D));
		double reach = 1.0D, width = 0.065D;
		float alpha = 1.0F;
		if (m.age() < 0.0F) {
			reach = HemoEasing.easeOutCubic(m.build());
		} else if (m.age() >= m.active()) {
			float t = (m.age() - m.active()) / 8.0F;
			if (t >= 1.0F) return;
			reach = 1.0D - 0.7D * t;
			width *= 1.0D + 1.2D * t;
			alpha = 1.0F - t;
		}
		SeraphaeLightGeometry.stream(s.p(), s.lux(), heart, chest, Vec3.ZERO, s.right(), s.up(), s.time(), m.seed(), reach, -0.3D, width,
				0.35D, alpha);
		if (m.age() < 0.0F || m.age() >= m.active()) return;
		SeraphaeLightGeometry.beads(s.p(), s.gold(), heart, chest, s.right(), s.up(), s.time(), 3, 0.05D, 0.06D, m.seed() + 5, 0.9F);
		float beat = HemoEasing.pulse(m.age() % 10.0F, 1.5F, 8.0F);
		LuxUmbraGeometry.card(s.p(), s.lux(), chest, s.right(), s.up(), 0.3D + 0.15D * beat, 0.3D + 0.15D * beat, LuxUmbraGeometry.WISP,
				m.seed() + 6, SeraphaeLightGeometry.TINT, 0.35F + 0.4F * beat);
		LuxUmbraGeometry.card(s.p(), s.lux(), chest, s.right(), s.up(), 0.1D + 0.08D * beat, 0.1D + 0.08D * beat, LuxUmbraGeometry.FOCUS,
				m.seed() + 7, SeraphaeLightGeometry.CORE, 0.6F + 0.4F * beat);
	}

	/**
	 * Wing Grasp: the disc swells and five furrows curl in from its rim like fingers closing, nerves in them; at the
	 * strike five bone tines grow out of the rim and curl inward over the centre like a hand shutting, gold crackling
	 * where their tips meet; they hold, then wither.
	 */
	private static void grasp(Scene s, Move m) {
		Vec3 centre = m.frame().aim();
		Ground g = m.ground().at(centre);
		double radius = SeraphaeMove.GRASP_RADIUS;
		Look look = m.look();
		List<Seam> fingers = SeraphaeOvergrowthRules.fingers(radius, GRASP_TINES, m.seed());
		List<Seam> nerves = new ArrayList<>(fingers.size());
		for (Seam finger : fingers) {
			nerves.add(new Seam(finger.x(), finger.y(), SeraphaeOvergrowthRules.NERVE_FIBRE, finger.start(), finger.span(), Kind.FIBRE));
		}
		s.bloom(g, 0.0D, radius, -180.0D, 180.0D, look, 0.45F, m.seed());
		s.seams(g, fingers, look, 1.0F, m.seed());
		s.nerves(g, nerves, look.grow(), Math.max(0.15F * m.build(), m.cue()) * look.alpha(), m.cueFront(), m.seed() + 5);
		if (!m.revealed()) return;
		Vec3 heart = s.local(centre.x, m.frame().floorY(), centre.z);
		float witherAt = m.active() + 4.0F;
		for (int i = 0; i < fingers.size(); i++) {
			Seam finger = fingers.get(i);
			Vec3 foot = g.local(finger.x()[0], finger.y()[0], 0.0D);
			Vec3 in = heart.subtract(foot);
			double flat = Math.hypot(in.x, in.z);
			if (flat < 1e-4) continue;
			Spur tine = new Spur(0.0D, 0.0D, GRASP_TINE, 0.13D, in.x / flat * 0.45D, in.z / flat * 0.45D, 6, i * 1.3D, 0.55D);
			int seed = m.seed() + i * 19;
			if (!s.solids()) continue;
			if (m.age() < 0.0F) {
				SeraphaeBoneGeometry.spur(s.p(), s.bone(), foot, tine, 0.1D + 0.12D * m.build(), 1.0D, 0.6F + 0.4F * m.build(),
						SeraphaeBoneGeometry.SPUR, seed);
				continue;
			}
			float own = m.age() - i * 0.4F;
			SeraphaeBoneGeometry.spur(s.p(), s.bone(), foot, tine, SeraphaeOvergrowthRules.height(own, witherAt),
					SeraphaeOvergrowthRules.throb(own, s.time(), seed), SeraphaeOvergrowthRules.formation(own, witherAt),
					SeraphaeBoneGeometry.withLight(SeraphaeBoneGeometry.SPUR, SeraphaeOvergrowthRules.glow(own)), seed);
			SeraphaeBoneGeometry.blister(s.p(), s.bone(), foot, 0.28D, 0.2D * Math.min(1.0D, own / 3.0D),
					SeraphaeOvergrowthRules.formation(own, witherAt), SeraphaeBoneGeometry.BLISTER, seed + 3);
		}
		if (m.age() < 0.0F) return;
		Vec3 close = heart.add(0.0D, 1.1D, 0.0D);
		if (s.solids()) {
			SeraphaeBoneGeometry.shards(s.p(), s.bone(), close, m.age() - 2.0F, 1.3D, 6, m.seed() + 90);
		} else {
			SeraphaeLightGeometry.burst(s.p(), s.lux(), s.gold(), close, new Vec3(0, 1, 0), Vec3.ZERO, s.right(), s.up(), m.age() - 2.0F,
					0.45D, m.seed() + 91, true);
		}
	}

	/**
	 * Convulsion and the wide Convulsion: six nerve bundles, each a band of swollen floor with gold fibres in it, turning
	 * with the spokes; the gaps between stay clean floor, the safe read. Through the wind-up the fibres flare on her
	 * three stutters; at the strike they fire and a low sheet of light stands on each bundle's rim, gold on its crest,
	 * turning with it, and shards jump off the floor.
	 */
	private static void convulsion(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Annulus annulus)) return;
		Look look = m.look();
		double half = annulus.arcDegrees() / 2.0D;
		boolean striking = m.age() >= 0.0F && m.age() < m.active();
		float fire;
		if (m.age() < 0.0F) fire = Math.max(Math.max(0.15F * m.build(), SeraphaeNerveFlashRules.stutter(m.moveTime())), m.cue());
		else fire = striking ? 0.85F + 0.15F * (float) Math.sin(s.time() * 2.1D) : 0.3F * look.light();
		Vec3 origin = s.local(m.frame().origin().x, m.frame().floorY(), m.frame().origin().z);
		for (int arc = 0; arc < annulus.arcs(); arc++) {
			double angle = annulus.arcAngle(Math.max(0.0F, m.age()), arc);
			Ground g = m.ground().turned(angle);
			int seed = m.seed() + arc * 29;
			List<Seam> spoke = SeraphaeOvergrowthRules.spoke(annulus.inner(), annulus.outer(), annulus.arcDegrees(), seed);
			s.bloom(g, annulus.inner(), annulus.outer(), -half, half, look, striking ? 0.95F : 0.5F, seed);
			s.seams(g, spoke, look, 1.0F, seed);
			s.nerves(g, spoke, look.grow(), fire * look.alpha(), 1.0F, seed + 3);
			if (!striking) continue;
			if (s.solids()) {
				double mid = (annulus.inner() + annulus.outer()) * 0.5D;
				SeraphaeBoneGeometry.shards(s.p(), s.bone(), g.local(mid, 0.0D, 0.0D), m.age(), 1.2D, 4, seed + 5);
				continue;
			}
			float alpha = 1.0F - HemoEasing.ramp(m.age(), m.active() - 3.0F, m.active());
			SeraphaeLightGeometry.curtain(s.p(), s.lux(), s.gold(), origin, new double[]{g.fx(), g.fz()}, annulus.outer(), -half, half, 0.0D,
					1.3D, Vec3.ZERO, s.time(), seed + 7, alpha, false, true);
			SeraphaeLightGeometry.curtain(s.p(), s.lux(), s.gold(), origin, new double[]{g.fx(), g.fz()}, annulus.inner(), -half * 0.9D,
					half * 0.9D, 0.0D, 0.8D, Vec3.ZERO, s.time(), seed + 8, alpha * 0.7F, false, true);
		}
	}

	/**
	 * Chain Wrench: the line under the chain opens as a furrow two blocks wide and the anchor's foot swells in a disc;
	 * through the wind-up gold runs the chain from her hands toward the anchor; at the strike a sheet of light stands
	 * along the line and the iron itself whips (drawn with the chains); a slipped anchor throws shards from its crown.
	 */
	private static void wrench(Scene s, Move m) {
		List<Vec3> anchors = m.frame().anchors();
		if (anchors.isEmpty()) return;
		Vec3 crown = anchors.get(0);
		Vec3 foot = m.frame().points().isEmpty() ? new Vec3(crown.x, m.frame().floorY(), crown.z) : m.frame().points().get(0);
		Vec3 origin = m.frame().origin();
		double dx = foot.x - origin.x, dz = foot.z - origin.z, length = Math.hypot(dx, dz);
		if (length < 0.5D) return;
		Look look = m.look();
		Ground line = new Ground(origin.x, origin.z, dx / length, dz / length, m.frame().floorY(), s.camera());
		s.seams(line, SeraphaeOvergrowthRules.trench(length, 1.0D, m.seed()), look, 1.0F, m.seed());
		Ground base = line.at(foot);
		s.bloom(base, 0.0D, 2.0D, -180.0D, 180.0D, look, 0.5F, m.seed() + 3);
		s.seams(base, SeraphaeOvergrowthRules.star(2.0D, m.seed() + 4), look, 1.0F, m.seed() + 4);
		if (s.solids()) {
			ContainmentAnchorEntity anchor = anchorAt(m.boss(), s.level(), crown);
			if (anchor != null && !anchor.isHeld() && m.age() >= 0.0F) {
				SeraphaeBoneGeometry.shards(s.p(), s.bone(), s.local(crown), m.age(), 1.4D, 8, m.seed() + 9);
			}
			return;
		}
		if (m.revealed() && m.age() < 0.0F) {
			SeraphaeAnimState motion = SeraphaeAnimState.of(m.boss());
			Vec3 feet = m.boss().getPosition(s.partial());
			Vec3 hands = feet.add(motion.hand(true)).lerp(feet.add(motion.hand(false)), 0.5D);
			float run = HemoEasing.easeInCubic(m.build());
			List<Vec3> lit = new ArrayList<>(9);
			for (int i = 0; i <= 8; i++) {
				double t = run * i / 8.0D;
				lit.add(s.local(hands.lerp(crown, t).add(0.0D, -SeraphaeOvergrowthRules.sag(t, 0.7F), 0.0D)));
			}
			SeraphaeLightGeometry.nerveLine(s.p(), s.gold(), lit, Vec3.ZERO, 0.08D, m.seed() + 5, 0.9F);
			SeraphaeLightGeometry.spark(s.p(), s.gold(), lit.get(lit.size() - 1), s.right(), s.up(), 0.09D, m.seed() + 6, 1.0F);
		}
		if (m.age() >= 0.0F && m.age() < m.active() + 4.0F) {
			float alpha = 1.0F - HemoEasing.ramp(m.age(), m.active() - 2.0F, m.active() + 4.0F);
			SeraphaeLightGeometry.wall(s.p(), s.lux(), s.gold(), straight(line, length, 10), 0.0D, 1.1D, Vec3.ZERO, s.time(), m.seed() + 7,
					alpha, false, true);
		}
	}

	/**
	 * Fracture: the disc round her swells and vessels of light run out under her; a crack of light opens down her chest
	 * through the wind-up; at the strike light pours out of her (a burst with body and a ring of light rolling out to
	 * the disc's edge, gold on its crest), shards of her are flung, and streams of light run from her into each new
	 * fragment.
	 */
	private static void fracture(Scene s, Move m) {
		double radius = SeraphaeMove.FRACTURE_RADIUS;
		Look look = m.look();
		Ground g = m.ground();
		s.bloom(g, 0.0D, radius, -180.0D, 180.0D, look, 0.5F, m.seed());
		s.seams(g, SeraphaeOvergrowthRules.disc(radius, m.seed()), look, 1.0F, m.seed());
		s.seams(g, SeraphaeOvergrowthRules.ring(radius, 0.18D, m.seed() + 2), look, 1.0F, m.seed() + 2);
		Vec3 heart = s.local(heart(m.boss(), s.partial()));
		if (s.solids()) {
			if (m.age() >= 0.0F) SeraphaeBoneGeometry.shards(s.p(), s.bone(), heart, m.age(), 2.2D, 12, m.seed() + 3);
			return;
		}
		if (m.revealed() && m.age() < 6.0F) {
			float open = m.age() < 0.0F ? m.build() : 1.0F - m.age() / 6.0F;
			SeraphaeLightGeometry.crack(s.p(), s.lux(), s.gold(), heart.add(0.0D, 0.55D * open, 0.0D), heart.add(0.0D, -0.65D * open, 0.0D),
					Vec3.ZERO, s.right(), s.up(), open, m.seed() + 4);
		}
		if (m.age() < 0.0F) return;
		Vec3 forward = new Vec3(g.fx(), 0.0D, g.fz());
		SeraphaeLightGeometry.burst(s.p(), s.lux(), s.gold(), heart, forward, Vec3.ZERO, s.right(), s.up(), m.age(), 1.3D, m.seed() + 5, false);
		double wave = SeraphaeOvergrowthRules.wave(m.age(), radius + 0.2D);
		if (wave > 0.0D) {
			float t = m.age() / SeraphaeOvergrowthRules.WAVE;
			Vec3 floor = s.local(m.frame().origin().x, m.frame().floorY(), m.frame().origin().z);
			SeraphaeLightGeometry.curtain(s.p(), s.lux(), s.gold(), floor, new double[]{g.fx(), g.fz()}, wave, -180.0D, 180.0D, 0.0D,
					1.6D * (1.0D - t * 0.5D), Vec3.ZERO, s.time(), m.seed() + 6, 1.0F - t * t, false, true);
		}
		if (m.age() < 10.0F) {
			double reach = HemoEasing.easeOutCubic(HemoEasing.clamp01(m.age() / 4.0F));
			int i = 0;
			for (SeraphaeFragmentEntity fragment : s.level().getEntitiesOfClass(SeraphaeFragmentEntity.class,
					m.boss().getBoundingBox().inflate(8.0D))) {
				Vec3 at = s.local(fragment.getPosition(s.partial()).add(0.0D, 0.5D, 0.0D));
				SeraphaeLightGeometry.stream(s.p(), s.lux(), heart, at, Vec3.ZERO, s.right(), s.up(), s.time(), m.seed() + 20 + i++, reach, 0.4D,
						0.12D, 0.6D, 1.0F - m.age() / 10.0F);
			}
		}
	}

	/**
	 * Healed Ground: under each mark the floor swells into a pad of overgrowth with roots running out of it, bone stubs
	 * showing through; at the strike a cluster of curling spurs bursts out of each pad with smaller ones round it, a
	 * ring of light rolls to its edge and shards fly; the spurs wither before the recovery ends and the standing pad
	 * (the hazard) takes over.
	 */
	private static void healedGround(Scene s, Move m) {
		List<Vec3> points = m.frame().points();
		Look look = m.look();
		double radius = SeraphaeMove.PAD_RADIUS;
		for (int i = 0; i < points.size(); i++) {
			Vec3 point = points.get(i);
			Ground g = m.ground().at(point);
			int seed = m.seed() + i * 41;
			s.bloom(g, 0.0D, radius, -180.0D, 180.0D, look, 0.6F, seed);
			s.seams(g, SeraphaeOvergrowthRules.star(radius, seed), look, 1.0F, seed);
			Vec3 foot = s.local(point.x, m.frame().floorY(), point.z);
			if (m.age() < 0.0F) {
				// the stubs are part of the tell, so a hidden tell hides them too
				if (!s.solids() || !m.revealed()) continue;
				for (int k = 0; k < 5; k++) {
					double a = k * 1.2566D + SeraphaeOvergrowthRules.unit(seed, k);
					double out = 0.5D + 0.8D * SeraphaeOvergrowthRules.unit(seed, 10 + k);
					Spur stub = SeraphaeOvergrowthRules.cluster(seed + k, 0.5D, 1).get(0);
					SeraphaeBoneGeometry.spur(s.p(), s.bone(), foot.add(Math.cos(a) * out, 0.0D, Math.sin(a) * out), stub, 0.3D + 0.5D * m.build(),
							1.0D, 0.6F + 0.4F * m.build(), SeraphaeBoneGeometry.SPUR, seed + k);
				}
				continue;
			}
			float own = m.age() - i * 1.5F;
			float witherAt = m.active() + m.recovery() - SeraphaeOvergrowthRules.WITHER - 2.0F - i * 1.5F;
			if (s.solids()) {
				SeraphaeBoneGeometry.cluster(s.p(), s.bone(), foot, SeraphaeOvergrowthRules.cluster(seed, 1.25D + 0.2D * SeraphaeOvergrowthRules.unit(seed, 1), 6),
						own, witherAt, s.time(), SeraphaeBoneGeometry.SPUR, seed);
				for (int k = 0; k < 4; k++) {
					double a = k * 1.5708D + SeraphaeOvergrowthRules.unit(seed, 20 + k) * 0.7D;
					Vec3 at = foot.add(Math.cos(a) * 1.1D, 0.0D, Math.sin(a) * 1.1D);
					SeraphaeBoneGeometry.cluster(s.p(), s.bone(), at, toward(SeraphaeOvergrowthRules.cluster(seed + k * 7, 0.7D, 4), Math.cos(a),
							Math.sin(a), 0.2D, 0.2D), own - 1.0F - k * 0.6F, witherAt, s.time(), SeraphaeBoneGeometry.SPUR, seed + k * 7);
				}
				SeraphaeBoneGeometry.shards(s.p(), s.bone(), foot, own, 1.5D, 7, seed + 50);
				continue;
			}
			double wave = SeraphaeOvergrowthRules.wave(own, radius);
			if (wave > 0.0D) {
				float t = own / SeraphaeOvergrowthRules.WAVE;
				SeraphaeLightGeometry.curtain(s.p(), s.lux(), s.gold(), foot, new double[]{g.fx(), g.fz()}, wave, -180.0D, 180.0D, 0.0D,
						0.6D * (1.0D - t * 0.5D), Vec3.ZERO, s.time(), seed + 3, 0.9F * (1.0F - t * t), false, false);
			}
			if (own > 0.0F && own < 4.0F) {
				LuxUmbraGeometry.ground(s.p(), s.lux(), foot.add(0.0D, 0.05D, 0.0D), 1.0D + own * 0.25D, LuxUmbraGeometry.POOL, seed + 4,
						SeraphaeLightGeometry.CORE, 0.75F * (1.0F - own / 4.0F));
			}
		}
	}

	/**
	 * The Unclosing Eye: the aperture opens on her chest through the wind-up while the room's light is drawn into it in
	 * streams, and a furrow scores the floor along the bearing the beam will start from, overgrowth seeded across the
	 * cone ahead of it. Then the beam: light with body from her chest to wherever the blocks stop it, sweeping, the
	 * floor it crosses swelling raw behind it in a seared band, light splashing up where it lands. The Lapse is the
	 * aperture guttering to a slit.
	 */
	private static void eye(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Occluded occluded) || !(occluded.shape() instanceof BossFootprint.Sweep sweep)) return;
		Look look = m.look();
		Ground g = m.ground();
		double start = sweep.angle(0.0D);
		boolean active = m.age() >= 0.0F && m.age() < m.active();
		s.bloom(g, 1.5D, 8.0D, -sweep.degrees() / 2.0D, sweep.degrees() / 2.0D, look, active ? 0.3F : 0.18F, m.seed());
		s.seams(g.turned(start), SeraphaeOvergrowthRules.trench(Math.min(sweep.radius(), 14.0D), 0.5D, m.seed() + 1), look, 0.75F, m.seed() + 1);
		Vec3 heartWorld = heart(m.boss(), s.partial());
		Vec3 heart = s.local(heartWorld);
		Vec3 end = null;
		if (active) {
			double[] f = Shapes.rotate(Shapes.forward(m.frame()), sweep.angle(m.age()));
			Vec3 far = heartWorld.add(f[0] * sweep.radius(), -1.2D, f[1] * sweep.radius());
			var hit = s.level().clip(new ClipContext(heartWorld, far, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, m.boss()));
			end = hit.getType() == HitResult.Type.MISS ? far : hit.getLocation();
			double reach = Shapes.planar(m.frame().origin(), end);
			double a = sweep.angle(m.age());
			s.bloom(g, Math.max(1.0D, reach - 2.5D), reach + 0.4D, Math.min(start, a), Math.max(start, a),
					new Look(1.0F, 1.0F, 0.6F, 0.9F, 1.0F, 1.0F), 0.85F, m.seed() + 2);
		}
		if (s.solids()) {
			if (end != null) {
				float burst = (m.age() % 6.0F);
				SeraphaeBoneGeometry.shards(s.p(), s.bone(), s.local(end), burst, 0.9D, 3, m.seed() + 3 + (int) (m.age() / 6.0F));
			}
			return;
		}
		float open = m.age() < 0.0F ? (m.revealed() ? m.build() : 0.0F) : active ? 1.0F : 0.0F;
		if (open > 0.01F) {
			LuxUmbraGeometry.card(s.p(), s.lux(), heart, s.right(), s.up(), 0.1D + 0.14D * open, 0.06D + 0.1D * open, LuxUmbraGeometry.EYE,
					m.seed() + 4, SeraphaeLightGeometry.TINT, 0.85F * open);
			LuxUmbraGeometry.card(s.p(), s.lux(), heart, s.right(), s.up(), 0.5D * open, 0.5D * open, LuxUmbraGeometry.LUX_SHELL, m.seed() + 5,
					SeraphaeLightGeometry.TINT, 0.2F * open);
		}
		if (m.age() < 0.0F && m.revealed()) {
			float pull = m.build();
			for (int i = 0; i < 8; i++) {
				double a = i * Math.PI / 4.0D + m.seed() * 0.3D;
				Vec3 from = heart.add(Math.cos(a) * 11.0D, 1.0D + 1.5D * SeraphaeOvergrowthRules.unit(m.seed(), i), Math.sin(a) * 11.0D);
				SeraphaeLightGeometry.stream(s.p(), s.lux(), from, heart, Vec3.ZERO, s.right(), s.up(), s.time(), m.seed() + 10 + i, pull,
						0.5D, 0.05D, 0.5D, 0.55F * pull);
			}
		}
		if (end != null) {
			Vec3 to = s.local(end);
			SeraphaeLightGeometry.stream(s.p(), s.lux(), heart, to, Vec3.ZERO, s.right(), s.up(), s.time(), m.seed() + 20, 1.0D, 0.0D,
					0.3D * SeraphaeOvergrowthRules.beamFlare(m.age()), 1.2D, 1.0F);
			LuxUmbraGeometry.ground(s.p(), s.lux(), to.add(0.0D, 0.05D, 0.0D), 0.9D, LuxUmbraGeometry.POOL, m.seed() + 21,
					SeraphaeLightGeometry.CORE, 0.7F);
			SeraphaeLightGeometry.curtain(s.p(), s.lux(), s.gold(), to, new double[]{1.0D, 0.0D}, 0.7D, -180.0D, 180.0D, 0.0D,
					0.7D + 0.2D * Math.sin(s.time() * 0.9D), Vec3.ZERO, s.time(), m.seed() + 22, 0.7F, false, false);
		}
		if (m.age() >= m.active()) {
			// the Lapse: the aperture gutters to a slit
			float lapse = m.age() - m.active();
			float flicker = 0.4F + 0.6F * (float) Math.abs(Math.sin(s.time() * 1.7D + lapse));
			LuxUmbraGeometry.card(s.p(), s.lux(), heart, s.right(), s.up(), 0.18D, 0.025D, LuxUmbraGeometry.EYE, m.seed() + 30,
					SeraphaeLightGeometry.TINT, 0.7F * flicker * (1.0F - HemoEasing.ramp(lapse, m.recovery() - 8.0F, m.recovery())));
		}
	}

	/**
	 * The Extraction: the line under every held chain opens as a furrow; then light pours out of her along every chain
	 * into its anchor and back, streams with body, while the iron of each chain lights from inside as it passes (drawn
	 * with the chains).
	 */
	private static void extraction(Scene s, Move m) {
		Vec3 origin = m.frame().origin();
		Look look = m.look();
		Vec3 heart = s.local(heart(m.boss(), s.partial()));
		List<Vec3> crowns = m.frame().anchors();
		for (int i = 0; i < crowns.size(); i++) {
			Vec3 crown = crowns.get(i);
			double dx = crown.x - origin.x, dz = crown.z - origin.z, length = Math.hypot(dx, dz);
			if (length < 0.5D) continue;
			int seed = m.seed() + i * 37;
			Ground line = new Ground(origin.x, origin.z, dx / length, dz / length, m.frame().floorY(), s.camera());
			s.seams(line, SeraphaeOvergrowthRules.trench(length, 0.8D, seed), look, 1.0F, seed);
			if (s.solids() || m.age() < 0.0F || m.age() > 20.0F) continue;
			Vec3 to = s.local(crown);
			if (m.age() < 10.0F) {
				SeraphaeLightGeometry.stream(s.p(), s.lux(), heart, to, Vec3.ZERO, s.right(), s.up(), s.time(), seed + 1,
						HemoEasing.easeOutCubic(HemoEasing.clamp01(m.age() / 8.0F)), -0.2D, 0.12D, 0.6D, 1.0F - HemoEasing.ramp(m.age(), 8.0F, 10.0F));
			} else {
				SeraphaeLightGeometry.stream(s.p(), s.lux(), to, heart, Vec3.ZERO, s.right(), s.up(), s.time(), seed + 2,
						HemoEasing.easeOutCubic(HemoEasing.clamp01((m.age() - 10.0F) / 8.0F)), -0.2D, 0.12D, 0.6D,
						1.0F - HemoEasing.ramp(m.age(), 18.0F, 20.0F));
			}
		}
	}

	/**
	 * The Radiance Flood: through the tell overgrowth spreads out from under her across the floor with vessels racing
	 * outward and a pale dome of light grows from her; at the flood a sheet of light rolls out to the walls, gold on its
	 * crest, and a ring of bone bursts up round her, then withers.
	 */
	private static void flood(Scene s, Move m) {
		Look look = m.look();
		Ground g = m.ground();
		float build = m.age() < 0.0F ? m.build() : 1.0F;
		double spread = 1.5D + 12.5D * HemoEasing.easeOutCubic(build);
		s.bloom(g, 0.0D, spread, -180.0D, 180.0D, look, 0.45F, m.seed());
		Look veins = new Look(build, look.swell(), look.light(), look.raw(), look.width(), look.alpha());
		s.seams(g, SeraphaeOvergrowthRules.disc(14.0D, m.seed()), veins, 1.0F, m.seed());
		Vec3 floor = s.local(m.frame().origin().x, m.frame().floorY(), m.frame().origin().z);
		Vec3 heart = s.local(heart(m.boss(), s.partial()));
		if (s.solids()) {
			if (m.age() < 0.0F) return;
			float witherAt = m.active() + m.recovery() - SeraphaeOvergrowthRules.WITHER;
			for (int i = 0; i < FLOOD_CLUSTERS; i++) {
				double a = Math.toRadians(i * 360.0D / FLOOD_CLUSTERS);
				Vec3 foot = floor.add(Math.cos(a) * 2.5D, 0.0D, Math.sin(a) * 2.5D);
				SeraphaeBoneGeometry.cluster(s.p(), s.bone(), foot, toward(SeraphaeOvergrowthRules.cluster(m.seed() + i * 11, 1.3D, 4),
						Math.cos(a), Math.sin(a), 0.25D, 0.25D), m.age() - (i % 3) * 0.6F, witherAt, s.time(), SeraphaeBoneGeometry.SPUR,
						m.seed() + i * 11);
			}
			return;
		}
		if (m.age() < 6.0F) {
			float dome = m.age() < 0.0F ? build : 1.0F - m.age() / 6.0F;
			double radius = 1.5D + dome * 14.0D;
			LuxUmbraGeometry.card(s.p(), s.lux(), heart, s.right(), s.up(), radius, radius, LuxUmbraGeometry.LUX_SHELL, m.seed() + 1,
					SeraphaeLightGeometry.TINT, 0.18F * dome);
		}
		if (m.age() < 0.0F) return;
		double wave = SeraphaeOvergrowthRules.wave(m.age(), 16.0D);
		if (wave > 0.0D) {
			float t = m.age() / SeraphaeOvergrowthRules.WAVE;
			SeraphaeLightGeometry.curtain(s.p(), s.lux(), s.gold(), floor, new double[]{g.fx(), g.fz()}, wave, -180.0D, 180.0D, 0.0D,
					2.2D * (1.0D - t * 0.5D), Vec3.ZERO, s.time(), m.seed() + 2, 1.0F - t * t, false, true);
		}
		SeraphaeLightGeometry.burst(s.p(), s.lux(), s.gold(), heart, new Vec3(g.fx(), 0.0D, g.fz()), Vec3.ZERO, s.right(), s.up(), m.age(),
				1.0D, m.seed() + 3, false);
	}

	// ── her chains ───────────────────────────────────────────────────

	/**
	 * Her chains in iron, link by link, from each anchor's crown to its iron on her (the radiance pass draws the light
	 * braided along them): sagging as the braid sags, snapping straight in a Recoil, whipping in a Chain Wrench, lit
	 * from inside by her light as it passes through them in the Extraction and as each anchor flares in her ending. A
	 * re-binding chain grows from its anchor toward her; a slipped one hangs from her as the model's own links.
	 */
	private static void chains(Scene s, SeraphaeEntity boss, SeraphaeAnimState motion, Vec3 feet, float ending) {
		if (!s.solids()) return;
		List<ContainmentAnchorEntity> anchors = new ArrayList<>(s.level().getEntitiesOfClass(ContainmentAnchorEntity.class,
				boss.getBoundingBox().inflate(48.0D), a -> boss.getUUID().equals(a.getParentId())));
		anchors.sort(Comparator.comparing((ContainmentAnchorEntity a) -> a.kind() != ContainmentAnchorEntity.Kind.BINDING)
				.thenComparingInt(Entity::getId));
		SeraphaeMove move = SeraphaeMove.byId(boss.view().moveId());
		float moveTime = boss.view().moveTime(s.partial());
		float age = move == null ? -1.0F : moveTime - SaintControllerRules.effective(move.kind(), move.timeline(boss.view().inputs())).commitTick();
		FootprintFrame frame = boss.view().frame();
		Vec3 wrenched = move == SeraphaeMove.CHAIN_WRENCH && frame != null && !frame.anchors().isEmpty() ? frame.anchors().get(0) : null;
		float recoil = boss.view().openingId() == SeraphaeOpenings.RECOIL_ID
				? SeraphaeRadianceGeometry.recoilSpike(boss.view().openingTime(s.partial())) : 0.0F;
		double reach = Math.max(1.0D, boss.syncedLeash() + 2.0D);
		for (int index = 0; index < anchors.size(); index++) {
			ContainmentAnchorEntity anchor = anchors.get(index);
			if (anchor.state() == ContainmentAnchorEntity.State.SLIPPED) continue;
			Vec3 crown = anchor.getPosition(s.partial()).add(0.0D, ContainmentAnchorEntity.CROWN, 0.0D);
			SeraphaeRestraint restraint = anchor.kind() == ContainmentAnchorEntity.Kind.RESERVE ? SeraphaeRestraint.WAIST : anchor.restraint();
			Vec3 iron = feet.add(motion.shackle(restraint));
			float taut = (float) HemoEasing.clamp01(crown.distanceTo(iron) / reach);
			float glow = 0.12F;
			if (ending >= 0.0F) {
				float braid = SeraphaeEndingRules.braid(ending, index);
				taut = Math.max(taut, braid);
				glow = Math.max(glow, braid * SeraphaeEndingRules.light(ending) * 0.95F);
			}
			taut = Math.min(1.0F, taut + recoil);
			Vec3 to = iron;
			if (anchor.state() == ContainmentAnchorEntity.State.BINDING) to = crown.lerp(iron, anchor.progress());
			Vec3 side = to.subtract(crown).cross(new Vec3(0, 1, 0));
			side = side.lengthSqr() < 1e-6 ? new Vec3(1, 0, 0) : side.normalize();
			DoubleUnaryOperator thrown = t -> 0.0D;
			if (wrenched != null && age >= 0.0F && crown.distanceToSqr(wrenched) < 0.5D) {
				float whipAge = age;
				thrown = t -> SeraphaeOvergrowthRules.whip(1.0D - t, whipAge, 1.0D);
			}
			float baseGlow = glow;
			DoubleUnaryOperator light = t -> baseGlow;
			if (move == SeraphaeMove.EXTRACTION && age >= 0.0F && age <= 20.0F) {
				// the light passes out along the chain and back; t runs from the crown (0) to her iron (1)
				double front = age < 10.0F ? 1.0D - age / 8.0D : (age - 10.0F) / 8.0D;
				light = t -> Math.max(baseGlow, 0.95D * Math.exp(-Math.pow((t - front) * 5.0D, 2.0D)));
			} else if (boss.view().openingId() == SeraphaeOpenings.CONDENSING_ID) {
				float gather = HemoEasing.clamp01(boss.view().openingTime(s.partial()) / SeraphaeOpenings.CONDENSING_GATHER);
				light = t -> Math.max(baseGlow, 0.6D * gather * (0.6D + 0.4D * Math.sin(t * 9.0D - s.time() * 0.3D)));
			}
			SeraphaeBoneGeometry.chain(s.p(), s.bone(), s.local(crown), s.local(to), taut, side, thrown, light, anchor.getId() * 7);
		}
	}

	// ── openings, the hold, the ending ───────────────────────────────

	/**
	 * Openings: a Recoil snaps a ring of light out from her and shards from her irons; Seized throws shards from her;
	 * Condensing draws streams of light in along each held chain over its gather.
	 */
	private static void opening(Scene s, SeraphaeEntity boss, SeraphaeAnimState motion, Vec3 feet) {
		int id = boss.view().openingId();
		if (id <= 0) return;
		float t = boss.view().openingTime(s.partial());
		if (t < 0.0F) return;
		int seed = boss.getId() * 13 + id;
		Vec3 floor = s.local(feet.x, feet.y - SeraphaeEntity.HOVER, feet.z);
		if (id == SeraphaeOpenings.RECOIL_ID || id == SeraphaeOpenings.SEIZED_ID) {
			if (s.solids()) {
				for (SeraphaeRestraint restraint : SeraphaeRestraint.values()) {
					SeraphaeBoneGeometry.shards(s.p(), s.bone(), s.local(feet.add(motion.shackle(restraint))), t, 0.8D, 2,
							seed + restraint.ordinal() * 5);
				}
				return;
			}
			double wave = SeraphaeOvergrowthRules.wave(t, 2.8D);
			if (wave > 0.0D) {
				float k = t / SeraphaeOvergrowthRules.WAVE;
				SeraphaeLightGeometry.curtain(s.p(), s.lux(), s.gold(), floor, new double[]{1.0D, 0.0D}, wave, -180.0D, 180.0D, 0.0D,
						1.4D * (1.0D - k * 0.5D), Vec3.ZERO, s.time(), seed, 0.85F * (1.0F - k * k), false, true);
			}
			return;
		}
		if (id == SeraphaeOpenings.CONDENSING_ID && !s.solids() && t < SeraphaeOpenings.CONDENSING_GATHER + 4.0F) {
			float gather = HemoEasing.clamp01(t / SeraphaeOpenings.CONDENSING_GATHER);
			Vec3 heart = s.local(feet.add(motion.heart()));
			int i = 0;
			for (ContainmentAnchorEntity anchor : s.level().getEntitiesOfClass(ContainmentAnchorEntity.class, boss.getBoundingBox().inflate(48.0D),
					a -> boss.getUUID().equals(a.getParentId()) && a.isHeld())) {
				Vec3 crown = s.local(anchor.crown());
				SeraphaeLightGeometry.stream(s.p(), s.lux(), crown, heart, Vec3.ZERO, s.right(), s.up(), s.time(), seed + i++,
						HemoEasing.easeOutCubic(gather), -0.25D, 0.08D, 0.5D, 1.0F - HemoEasing.ramp(t, SeraphaeOpenings.CONDENSING_GATHER,
								SeraphaeOpenings.CONDENSING_GATHER + 4.0F));
			}
		}
	}

	/**
	 * The hold: light pours from her palms into the patient's chest in streams with body, the floor under them swells
	 * with new tissue, and on each 10-tick beat gold runs up their arms from the chest.
	 */
	private static void hold(Scene s, SeraphaeEntity boss, SeraphaeAnimState motion, Vec3 feet) {
		int heldId = boss.syncedHoldPlayer();
		Entity held = heldId < 0 ? null : s.level().getEntity(heldId);
		if (held == null) return;
		float age = AnimationTime.age(boss, s.partial());
		int seed = boss.getId() * 17 + heldId;
		Vec3 standing = held.getPosition(s.partial());
		Ground under = new Ground(standing.x, standing.z, 1.0D, 0.0D, standing.y, s.camera());
		float beat = HemoEasing.pulse(age % 10.0F, 1.5F, 8.0F);
		Look look = new Look(1.0F, 0.7F + 0.3F * beat, 0.5F + 0.4F * beat, 0.5F, 1.0F, 1.0F);
		s.bloom(under, 0.0D, 1.1D, -180.0D, 180.0D, look, 0.8F, seed);
		if (s.solids()) return;
		Vec3 chest = s.local(standing.add(0.0D, held.getBbHeight() * 0.62D, 0.0D));
		for (boolean left : new boolean[]{true, false}) {
			Vec3 palm = s.local(feet.add(motion.hand(left)));
			SeraphaeLightGeometry.stream(s.p(), s.lux(), palm, chest, Vec3.ZERO, s.right(), s.up(), s.time(), seed + (left ? 1 : 2), 1.0D, 0.05D,
					0.07D, 0.25D, 0.95F);
		}
		if (beat > 0.05F) {
			double yaw = Math.toRadians(held instanceof net.minecraft.world.entity.LivingEntity living ? living.yBodyRot : held.getYRot());
			Vec3 side = new Vec3(Math.cos(yaw), 0.0D, Math.sin(yaw));
			for (int k = -1; k <= 1; k += 2) {
				Vec3 shoulder = chest.add(side.scale(k * 0.36D)).add(0.0D, 0.25D, 0.0D);
				Vec3 hand = shoulder.add(side.scale(k * 0.08D)).add(0.0D, -0.7D, 0.0D);
				SeraphaeLightGeometry.nerveLine(s.p(), s.gold(), List.of(chest, shoulder, hand), Vec3.ZERO, 0.06D, seed + 5 + k, beat);
			}
		}
	}

	/**
	 * Her ending, on its beats: each anchor flares in turn with a burst of light at its crown (its chain lights white
	 * through the iron, drawn with the chains); through the reel streams of light run in along every chain to her;
	 * as she sinks a ring of light closes over the tomb round her, gold at its foot, and the floor under her heals shut.
	 */
	private static void ending(Scene s, SeraphaeEntity boss, SeraphaeAnimState motion, Vec3 feet, float ending) {
		int seed = boss.getId() * 19;
		Vec3 floorWorld = new Vec3(feet.x, feet.y - SeraphaeEntity.HOVER, feet.z);
		Ground under = new Ground(floorWorld.x, floorWorld.z, 1.0D, 0.0D, floorWorld.y, s.camera());
		float sink = SeraphaeEndingRules.sink(ending);
		if (ending >= SeraphaeEndingRules.SINK_START - 10.0F) {
			float heal = HemoEasing.clamp01((ending - (SeraphaeEndingRules.SINK_START - 10.0F)) / 10.0F);
			Look look = new Look(1.0F, 1.0F - 0.6F * sink, 0.8F * (1.0F - sink), 0.3F, 1.0F, heal * (1.0F - HemoEasing.ramp(ending,
					SeraphaeEndingRules.SINK_START + 6.0F, SeraphaeEndingRules.END_TRIAL)));
			s.bloom(under, 0.0D, SEAL_RADIUS * (1.0D - 0.7D * sink), -180.0D, 180.0D, look, 0.9F, seed);
		}
		if (s.solids()) return;
		List<ContainmentAnchorEntity> anchors = new ArrayList<>(s.level().getEntitiesOfClass(ContainmentAnchorEntity.class,
				boss.getBoundingBox().inflate(48.0D), a -> boss.getUUID().equals(a.getParentId())));
		anchors.sort(Comparator.comparing((ContainmentAnchorEntity a) -> a.kind() != ContainmentAnchorEntity.Kind.BINDING)
				.thenComparingInt(Entity::getId));
		Vec3 heart = s.local(feet.add(motion.heart()));
		for (int i = 0; i < anchors.size(); i++) {
			ContainmentAnchorEntity anchor = anchors.get(i);
			Vec3 crown = s.local(anchor.getPosition(s.partial()).add(0.0D, ContainmentAnchorEntity.CROWN, 0.0D));
			float flare = ending - i * SeraphaeEndingRules.FLARE_GAP;
			if (flare >= 0.0F && flare < SeraphaeOvergrowthRules.IMPACT) {
				SeraphaeLightGeometry.burst(s.p(), s.lux(), s.gold(), crown, new Vec3(0, 1, 0), Vec3.ZERO, s.right(), s.up(), flare, 0.5D,
						seed + i * 7, false);
			}
			if (ending >= SeraphaeEndingRules.FLARE_END && ending < SeraphaeEndingRules.SINK_START) {
				float light = SeraphaeEndingRules.light(ending);
				SeraphaeLightGeometry.stream(s.p(), s.lux(), crown, heart, Vec3.ZERO, s.right(), s.up(), s.time(), seed + i * 7 + 3, 1.0D, -0.2D,
						0.06D, 0.4D, 0.8F * light);
			}
		}
		if (ending >= SeraphaeEndingRules.SINK_START - 4.0F) {
			double radius = SEAL_RADIUS * (1.0D - 0.85D * sink) + 0.15D;
			float alpha = 1.0F - HemoEasing.ramp(ending, SeraphaeEndingRules.END_TRIAL - 3.0F, SeraphaeEndingRules.END_TRIAL);
			SeraphaeLightGeometry.curtain(s.p(), s.lux(), s.gold(), s.local(floorWorld), new double[]{1.0D, 0.0D}, radius, -180.0D, 180.0D, 0.0D,
					1.6D + 1.2D * sink, Vec3.ZERO, s.time(), seed + 50, alpha, true, false);
		}
	}

	// ── hazards ──────────────────────────────────────────────────────

	/**
	 * Her healed pads standing: a pad of overgrowth with roots running out of it, swelling and lighting on its 20-tick
	 * pulse with a low ring of light rolling to its edge, a few short spurs and blisters standing in it, throbbing; it
	 * withers back into the floor over its fade.
	 */
	private static void pad(Scene s, BossGroundHazardEntity hazard) {
		if (hazard.kind() != SeraphaeEntity.PAD_KIND) return;
		float age = AnimationTime.age(hazard, s.partial());
		float fade = SaintTelegraphTiming.hazardFade(age, hazard.life(), hazard.fade(), hazard.fading(), hazard.fadeStart());
		if (fade <= 0.0F) return;
		double radius = hazard.shape() instanceof BossFootprint.Disc disc ? disc.radius() : SeraphaeMove.PAD_RADIUS;
		FootprintFrame frame = hazard.frame();
		Vec3 point = frame.origin();
		int seed = hazard.getId() * 13;
		Ground g = new Ground(point.x, point.z, 1.0D, 0.0D, frame.floorY(), s.camera());
		float pulse = age % SeraphaeEntity.PAD_PULSE;
		float beat = HemoEasing.pulse(pulse, 2.0F, 10.0F);
		Look look = new Look(1.0F, 0.8F + 0.2F * beat, 0.35F + 0.5F * beat, 0.45F, 1.0F, fade);
		s.bloom(g, 0.0D, radius, -180.0D, 180.0D, look, 0.85F, seed);
		s.seams(g, SeraphaeOvergrowthRules.star(radius, seed), look, 1.0F, seed);
		Vec3 foot = s.local(point.x, frame.floorY(), point.z);
		if (s.solids()) {
			// the hazard's age is its tick count, so the tick its owner expired it is the age its spurs start to wither
			float witherAt = hazard.fading() ? hazard.fadeStart() : hazard.life() - SeraphaeOvergrowthRules.WITHER;
			for (int k = 0; k < 3; k++) {
				double a = k * 2.0944D + SeraphaeOvergrowthRules.unit(seed, k);
				double out = radius * (0.3D + 0.3D * SeraphaeOvergrowthRules.unit(seed, 10 + k));
				SeraphaeBoneGeometry.cluster(s.p(), s.bone(), foot.add(Math.cos(a) * out, 0.0D, Math.sin(a) * out),
						SeraphaeOvergrowthRules.cluster(seed + k * 7, 0.55D + 0.2D * SeraphaeOvergrowthRules.unit(seed, 20 + k), 3), age - k,
						witherAt, s.time(), SeraphaeBoneGeometry.SPUR, seed + k * 7);
			}
			return;
		}
		double wave = SeraphaeOvergrowthRules.wave(pulse, radius);
		if (wave > 0.0D) {
			float t = pulse / SeraphaeOvergrowthRules.WAVE;
			SeraphaeLightGeometry.curtain(s.p(), s.lux(), s.gold(), foot, new double[]{1.0D, 0.0D}, wave, -180.0D, 180.0D, 0.0D, 0.35D,
					Vec3.ZERO, s.time(), seed + 3, 0.6F * fade * (1.0F - t * t), false, false);
		}
	}

	// ── pieces ───────────────────────────────────────────────────────

	/** Her heart in the world, as her renderer last recorded it. */
	static Vec3 heart(SeraphaeEntity boss, float partial) {
		return boss.getPosition(partial).add(SeraphaeAnimState.of(boss).heart());
	}

	/** The ground of one lane: offset to the aim's right, turned by its fan angle. */
	private static Ground laneGround(Ground g, double offset, double angle) {
		Ground turned = g.turned(angle);
		return new Ground(g.wx(0.0D, offset), g.wz(0.0D, offset), turned.fx(), turned.fz(), g.y(), g.camera());
	}

	/** {@code n + 1} floor points along a ray at {@code degrees} from {@code r0} to {@code r1}, camera-relative. */
	private static List<Vec3> radial(Ground g, double r0, double r1, double degrees, int n) {
		double a = Math.toRadians(degrees), c = Math.cos(a), sn = Math.sin(a);
		List<Vec3> out = new ArrayList<>(n + 1);
		for (int i = 0; i <= n; i++) {
			double r = r0 + (r1 - r0) * i / n;
			out.add(g.local(c * r, sn * r, 0.0D));
		}
		return out;
	}

	/** {@code n + 1} floor points along a ground's aim from its origin to {@code length}, camera-relative. */
	private static List<Vec3> straight(Ground g, double length, int n) {
		List<Vec3> out = new ArrayList<>(n + 1);
		for (int i = 0; i <= n; i++) out.add(g.local(length * i / n, 0.0D, 0.0D));
		return out;
	}

	/** The spurs leaning {@code lean} more per block of height along {x, z}, curling {@code curl} more. */
	private static List<Spur> toward(List<Spur> spurs, double x, double z, double lean, double curl) {
		List<Spur> out = new ArrayList<>(spurs.size());
		for (Spur spur : spurs) out.add(spur.toward(x, z, lean, curl));
		return out;
	}

	/** Her anchor whose crown stands at {@code crown}, if it is loaded. */
	private static ContainmentAnchorEntity anchorAt(SeraphaeEntity boss, ClientLevel level, Vec3 crown) {
		for (ContainmentAnchorEntity anchor : level.getEntitiesOfClass(ContainmentAnchorEntity.class, new AABB(crown, crown).inflate(2.0D),
				a -> boss.getUUID().equals(a.getParentId()))) {
			if (anchor.crown().distanceToSqr(crown) < 0.5D) return anchor;
		}
		return null;
	}
}
