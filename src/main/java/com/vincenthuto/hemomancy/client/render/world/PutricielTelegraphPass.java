package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.entity.boss.putriciel.PutricielAnimState;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.world.PutricielBloomRules.Burn;
import com.vincenthuto.hemomancy.client.render.world.PutricielBloomRules.Cap;
import com.vincenthuto.hemomancy.client.render.world.PutricielBloomRules.Look;
import com.vincenthuto.hemomancy.client.render.world.PutricielGroundGeometry.Front;
import com.vincenthuto.hemomancy.client.render.world.PutricielGroundGeometry.Ground;
import com.vincenthuto.hemomancy.client.render.world.PutricielGroundGeometry.Paint;
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
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielEndingRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielHazard;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielSludgeRules;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

/**
 * Saint Putriciel's own telegraph and hazard pass (visual pass 2, 2026-11-06), called from the manipulation visuals'
 * dispatcher in both of its passes. Their {@link PutricielTelegraphStyle} claims every move and hazard and paints
 * nothing, so the kit's flat tints never show for them; this pass reads the same synced move, footprint, frame,
 * timeline and reveal the kit does, so every drawn edge is the hit test's edge.
 * <p>
 * In the rot a footprint is the floor being colonised: the mould spreads out over it from where the move starts, rot
 * pools into it, it fruits toward the commit, trenches tear open along lanes and fill with ooze, bracket fungus and
 * spore sacs push up through it, and at the commit it bursts: spores roll out, ooze is flung, roots breach. In the
 * cleansing interval a footprint is purifying fire scorching that rot black: the char eats out from the move's start,
 * its cracks glow crimson to pink-white at the commit, furrows burn into beds of coals and the Flammeus tongues erupt.
 * Every move runs in phases: emerge, build, commit flash, strike, linger, dissolve ({@link PutricielBloomRules}).
 * <p>
 * Three layers a frame, each a whole batch: the floor ({@link PutricielRenderTypes#GROUND}), then the air
 * ({@link PutricielRenderTypes#SPORE}, with the fire on the {@code FLAME} flows), then in the solid pass the growths
 * ({@link PutricielRenderTypes#GROWTH}). Censer wakes and the Grasp's reach follow the posed model's own censers and
 * palm and staff head, which the renderer records each frame ({@link PutricielAnimState#censerAt},
 * {@link PutricielAnimState#palmAt}, {@link PutricielAnimState#staffHeadAt}), so an effect never disagrees with the
 * animation. Struck bodies get {@link PutricielImpactGeometry}; the ending's floor and fire are
 * drawn here too.
 */
final class PutricielTelegraphPass {
	private static final double RANGE = 64.0D;
	/** A thrown shelf leaves the back colony this high over the body's feet, this far behind it. */
	private static final double COLONY_HEIGHT = 2.6D, COLONY_BACK = 0.5D;
	/** Height of the Sac Swell's spore curtain: the full standing band, so it reads as a wall that cannot be jumped. */
	private static final double SAC_CURTAIN = 2.6D;
	/**
	 * The Full Purge's tongues: their flicker carries them to about the jump's clearance (0.9) and no higher once the
	 * flare has settled, so the ring reads as one to jump.
	 */
	private static final double PURGE_TONGUE = 0.7D;

	enum Layer { GROUND, SPORE, SOLID }

	/** The pass's frame: which layer it draws and where to. Points are camera-relative. */
	record Scene(PoseStack p, Layer layer, VertexConsumer floor, VertexConsumer spores, VertexConsumer growth, VertexConsumer flame,
			Vec3 camera, Vec3 right, Vec3 up, double time, float partial, ClientLevel level) {
		boolean ground() {
			return layer == Layer.GROUND;
		}

		boolean spore() {
			return layer == Layer.SPORE;
		}

		boolean solid() {
			return layer == Layer.SOLID;
		}

		Vec3 local(Vec3 world) {
			return world.subtract(camera);
		}

		Vec3 local(double x, double y, double z) {
			return new Vec3(x - camera.x, y - camera.y, z - camera.z);
		}
	}

	/** One running move as the pass draws it. */
	private record Move(PutricielEntity boss, PutricielMove move, BossFootprint footprint, FootprintFrame frame, SaintMoveInputs inputs,
			float moveTime, float age, int windup, int active, int recovery, int reveal, Look look, Burn burn, Ground ground, int seed,
			float bodyAge) {
		float build() {
			return PutricielBloomRules.build(moveTime, reveal, windup);
		}

		Vec3 origin() {
			return new Vec3(frame.origin().x, frame.floorY(), frame.origin().z);
		}

		double floor() {
			return frame.floorY();
		}

		/** A point on the floor in the footprint's frame. */
		Vec3 at(double along, double right) {
			return ground.world(along, right);
		}

		/** The tick (move time) a point {@code fraction} of the way out of the footprint is taken by the spreading colony. */
		float taken(double fraction) {
			return reveal + (windup - reveal) * PutricielBloomRules.SPREAD_LEAD * (float) HemoEasing.clamp01(fraction);
		}
	}

	private PutricielTelegraphPass() {
	}

	static void collect(ClientLevel level, PoseStack p, LuxUmbraBatch flows, Vec3 camera, Vec3 right, Vec3 up, double time,
			float partial, boolean solids) {
		AABB box = new AABB(camera, camera).inflate(RANGE);
		List<PutricielEntity> bosses = new ArrayList<>();
		for (PutricielEntity boss : level.getEntitiesOfClass(PutricielEntity.class, box)) {
			if (boss.isAlive() && !boss.isInvisible() && boss.kit() != null) bosses.add(boss);
		}
		List<BossGroundHazardEntity> hazards = new ArrayList<>();
		for (BossGroundHazardEntity hazard : level.getEntitiesOfClass(BossGroundHazardEntity.class, box)) {
			if (!hazard.isRemoved() && hazard.owner() instanceof PutricielEntity) hazards.add(hazard);
		}
		if (bosses.isEmpty() && hazards.isEmpty()) return;
		MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
		VertexConsumer none = FerricDuctilisGeometry.DISCARD;
		PutricielRenderTypes.begin(time);
		if (solids) {
			VertexConsumer growth = buffers.getBuffer(PutricielRenderTypes.GROWTH);
			draw(new Scene(p, Layer.SOLID, none, none, growth, none, camera, right, up, time, partial, level), bosses, hazards);
			buffers.endBatch(PutricielRenderTypes.GROWTH);
			return;
		}
		// the frame's live sludge pools: every floor read rides their surface (revision 2026-11-06, R2.4)
		PutricielGroundGeometry.begin(PutricielSludgeField.of(hazards, partial));
		VertexConsumer floor = buffers.getBuffer(PutricielRenderTypes.GROUND);
		draw(new Scene(p, Layer.GROUND, floor, none, none, none, camera, right, up, time, partial, level), bosses, hazards);
		buffers.endBatch(PutricielRenderTypes.GROUND);
		VertexConsumer spores = buffers.getBuffer(PutricielRenderTypes.SPORE);
		draw(new Scene(p, Layer.SPORE, none, spores, none, flows.vertices(LuxUmbraBatch.Material.FLAME), camera, right, up, time,
				partial, level), bosses, hazards);
		buffers.endBatch(PutricielRenderTypes.SPORE);
	}

	private static void draw(Scene s, List<PutricielEntity> bosses, List<BossGroundHazardEntity> hazards) {
		for (PutricielEntity boss : bosses) {
			float rise = boss.riseTime(s.partial());
			if (rise >= 0.0F && rise < SaintRise.TICKS) continue;
			// the purifier on fire (revision 2026-11-06, R1): nothing in the rot, real flame through the interval
			PutricielBodyFireGeometry.collect(s, boss);
			if (boss.syncedEndingTick() >= 0) {
				ending(s, boss);
				continue;
			}
			move(s, boss);
			PutricielImpactGeometry.collect(s, boss);
		}
		for (BossGroundHazardEntity hazard : hazards) hazard(s, hazard);
	}

	// ── moves ────────────────────────────────────────────────────────

	private static void move(Scene s, PutricielEntity boss) {
		SaintMoveView view = boss.view();
		PutricielMove move = PutricielMove.byId(view.moveId());
		if (move == null) return;
		float moveTime = view.moveTime(s.partial());
		if (moveTime < 0.0F) return;
		SaintMoveInputs inputs = view.inputs();
		BossMoveTimeline timeline = SaintControllerRules.effective(move.kind(), move.timeline(inputs));
		TelegraphSpec spec = move.telegraph(inputs);
		int reveal = SaintControllerRules.revealTick(spec == null ? TelegraphSpec.DEFAULT : spec, timeline, move.kind());
		// a recovery opening (the Offering's kneel, the Embering) stretches the recovery to the opening's length
		if (view.openingLength() > 0) timeline = SaintControllerRules.withOpening(timeline, view.openingLength());
		int windup = timeline.commitTick(), active = timeline.active();
		if (moveTime > windup + active + Math.max(timeline.recovery(), PutricielBloomRules.COOL)) return;
		FootprintFrame frame = view.frame();
		if (frame == null) {
			frame = FootprintFrame.simple(boss.syncedMoveOrigin(), boss.syncedMoveAim(), boss.syncedMoveAim(), boss.getY());
		}
		BossFootprint footprint = move.footprint(inputs);
		Look look = PutricielBloomRules.look(moveTime, reveal, windup, active);
		Burn burn = PutricielBloomRules.burn(moveTime, reveal, windup, active);
		Ground ground = Ground.of(frame.origin(), Shapes.forward(frame), frame.floorY(), s.camera());
		Move m = new Move(boss, move, footprint, frame, inputs, moveTime, moveTime - windup, windup, active, timeline.recovery(),
				reveal, look, burn, ground, boss.getId() * 31 + move.id() * 7 + boss.syncedMoveSerial() * 131,
				AnimationTime.age(boss, s.partial()));
		switch (move) {
			case CENSER_SWING, CENSER_BACKSWING -> censer(s, m);
			case CENSER_DROP -> censerDrop(s, m);
			case OFFERING_GRASP -> grasp(s, m);
			case ROT_BREATH -> breath(s, m);
			case SAC_SWELL -> sacSwell(s, m);
			case SHELF_VOLLEY -> volley(s, m);
			case ROOT_SNARE -> snare(s, m);
			case CENSER_ORBIT -> orbit(s, m);
			case MOULD_COMMUNION -> communion(s, m);
			case BLOOM_OF_ROT -> bloom(s, m);
			case IGNITION -> ignition(s, m);
			case PURGING_SWEEP -> purgingSweep(s, m);
			case CENSER_CAST -> censerCast(s, m);
			case CAUTERISING_LANE -> cauterise(s, m);
			case PYRE_HEART -> pyreHeart(s, m);
			case PYRE_LINES -> pyreLines(s, m);
			case FULL_PURGE -> fullPurge(s, m);
			case OFFERING -> offering(s, m);
			case EMBERING -> embering(s, m);
		}
	}

	/**
	 * Censer Swing and Backswing: the cone's floor colonises out from the body, its outer arc torn into a shallow
	 * trench fringed with bracket rosettes that unfurl along it through the wind-up; the swing drags a wake of spores
	 * and rot smoke along the censers' real path, the floor behind the censer's band fruits, and each rosette puffs as
	 * the band passes it.
	 */
	private static void censer(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Sweep sweep)) return;
		double half = sweep.degrees() / 2.0D, radius = sweep.radius();
		Look look = m.look();
		if (s.ground()) {
			PutricielGroundGeometry.sector(s.p(), s.floor(), m.ground(), sweep.inner(), radius, -half, half,
					Front.from(m.origin(), look.grow() * radius, 0.9D), rot(look, 0.6F, 0.9F), m.seed());
			if (m.age() >= 0.0F && look.visible()) {
				double band = sweep.angle(m.age());
				double from = sweep.clockwise() ? -half : band, to = sweep.clockwise() ? band : half;
				PutricielGroundGeometry.sector(s.p(), s.floor(), m.ground(), sweep.inner(), radius, from, to, Front.whole(m.origin()),
						new Paint(PutricielGroundGeometry.MAT, 0.9F * look.wet(), 1.0F, look.alpha() * 0.75F), m.seed() + 5);
			}
			PutricielGroundGeometry.strip(s.p(), s.floor(), m.ground(), arc(m.ground(), radius * 0.97D, -half, half, m.seed()), 0.24D,
					look.grow(), look.width() * 0.75F, trench(look, 0.85F), m.seed() + 9);
		}
		int count = (int) Math.max(4, Math.round(Math.toRadians(sweep.degrees()) * radius / 1.5D));
		for (int i = 0; i < count; i++) {
			double f = (i + 0.5D) / count;
			double bearing = -half + sweep.degrees() * (sweep.clockwise() ? f : 1.0D - f);
			Vec3 foot = m.at(Math.cos(Math.toRadians(bearing)) * radius * 0.9D, Math.sin(Math.toRadians(bearing)) * radius * 0.9D);
			int seed = m.seed() + 40 + i * 17;
			float sprout = m.reveal() + (m.windup() - m.reveal()) * 0.6F * (float) f;
			float own = m.moveTime() - sprout;
			if (s.solid()) {
				PutricielGrowthGeometry.rosette(s.p(), s.growth(), s.local(foot),
						PutricielBloomRules.rosette(seed, 0.45D + 0.2D * PutricielBloomRules.unit(seed, 1), 4), own,
						m.windup() + m.active() + 8.0F - sprout, s.time(), PutricielGrowthGeometry.FRESH, seed);
			} else if (s.spore() && m.age() >= 0.0F) {
				// the band reaches this rosette f of the way through its sweep
				float passed = m.age() - (float) (f * Math.max(1, sweep.sweepTicks()));
				PutricielSporeGeometry.burst(s.p(), s.spores(), s.local(foot).add(0.0D, 0.35D, 0.0D), s.right(), s.up(), passed, 0.8D, 4,
						0.2F, 0.9F, seed + 5);
			}
		}
		if (s.spore()) {
			float wake = PutricielBloomRules.wake(m.age(), m.active());
			for (int c = 0; c < 2 && wake > 0.0F; c++) {
				List<Vec3> path = trail(s, m, c, -PutricielBloomRules.WAKE_LEAD, Math.min(m.age(), m.active() + 1.0F), 12);
				PutricielSporeGeometry.wake(s.p(), s.spores(), path, Vec3.ZERO, s.right(), s.up(), s.time(), 0.32D,
						PutricielBloomRules.fray(m.age(), m.active()), wake, m.seed() + 70 + c * 9);
			}
		}
	}

	/**
	 * Censer Drop: the disc at the aim colonises and rot pools into it while a ring of spore sacs swells round it and
	 * radial cracks tear open; the censer comes down along its real arc and bursts: the sacs tear open, ooze is
	 * thrown up, a ring of spores rolls out, clods fly and three rosettes push up out of the fouled crater.
	 */
	private static void censerDrop(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Disc disc)) return;
		double r = disc.radius();
		Look look = m.look();
		Vec3 aim = m.frame().aim();
		Vec3 centre = new Vec3(aim.x, m.floor(), aim.z);
		Ground g = m.ground().at(aim);
		if (s.ground()) {
			PutricielGroundGeometry.sector(s.p(), s.floor(), g, 0.0D, r, -180.0D, 180.0D, Front.from(centre, look.grow() * r, 0.8D),
					rot(look, 1.0F, 1.0F), m.seed());
			for (int i = 0; i < 5; i++) {
				double a = i * 72.0D + PutricielBloomRules.unit(m.seed(), i) * 30.0D;
				PutricielGroundGeometry.strip(s.p(), s.floor(), g.turned(a), jag(g.turned(a), 0.25D, r * 0.95D, 0.0D, 0.12D, m.seed() + i),
						0.18D, look.grow(), look.width() * (m.age() >= 0.0F ? 1.0F : 0.6F), trench(look, 0.9F), m.seed() + 10 + i);
			}
			if (m.age() >= 0.0F) {
				PutricielGroundGeometry.sector(s.p(), s.floor(), g, 0.0D, 1.3D, -180.0D, 180.0D,
						Front.from(centre, 1.3D * HemoEasing.clamp01(m.age() / 3.0F), 0.4D),
						new Paint(PutricielGroundGeometry.MAT, 1.0F, 1.0F, look.alpha()), m.seed() + 20);
			}
		}
		for (int i = 0; i < 5; i++) {
			double a = Math.toRadians(i * 72.0D + 36.0D);
			Vec3 foot = centre.add(Math.cos(a) * r * 0.62D, 0.0D, Math.sin(a) * r * 0.62D);
			float swellAge = m.moveTime() - m.reveal() - i * 1.5F;
			float burst = m.age() - i * 0.6F;
			if (s.solid()) {
				PutricielGrowthGeometry.sac(s.p(), s.growth(), s.local(foot), 0.26D, 0.42D,
						PutricielBloomRules.swell(swellAge, s.time(), m.seed() + i), PutricielBloomRules.tear(burst),
						PutricielGrowthGeometry.SAC_SKIN, m.seed() + 30 + i);
			} else if (s.spore()) {
				PutricielSporeGeometry.burst(s.p(), s.spores(), s.local(foot).add(0.0D, 0.3D, 0.0D), s.right(), s.up(), burst, 0.9D, 4,
						0.35F, 1.0F, m.seed() + 50 + i);
			}
		}
		if (m.age() >= 0.0F) {
			float witherAt = m.active() + m.recovery() - PutricielBloomRules.WITHER - 2.0F;
			for (int i = 0; i < 3; i++) {
				double a = Math.toRadians(i * 120.0D + PutricielBloomRules.unit(m.seed(), 60 + i) * 40.0D);
				Vec3 foot = centre.add(Math.cos(a) * r * 0.3D, 0.0D, Math.sin(a) * r * 0.3D);
				float own = m.age() - 1.0F - i;
				if (s.solid()) {
					PutricielGrowthGeometry.rosette(s.p(), s.growth(), s.local(foot), PutricielBloomRules.rosette(m.seed() + 70 + i, 0.75D, 5),
							own, witherAt - 1.0F - i, s.time(), PutricielGrowthGeometry.FRESH, m.seed() + 70 + i);
				}
			}
			if (s.solid()) PutricielGrowthGeometry.clods(s.p(), s.growth(), s.local(centre), m.age(), 2.2D, 7, PutricielGrowthGeometry.LOAM,
					m.seed() + 80);
		}
		if (!s.spore()) return;
		// the censer's swing up and its drop: the last ten ticks of the wind-up
		float wake = HemoEasing.window(m.age(), -10.0F, -7.0F, m.active(), m.active() + PutricielBloomRules.WAKE_LINGER);
		for (int c = 0; c < 2 && wake > 0.0F; c++) {
			PutricielSporeGeometry.wake(s.p(), s.spores(), trail(s, m, c, -8.0F, Math.min(m.age(), 1.0F), 10), Vec3.ZERO, s.right(), s.up(),
					s.time(), 0.26D, PutricielBloomRules.fray(m.age(), m.active()), wake * 0.8F, m.seed() + 90 + c);
		}
		Vec3 local = s.local(centre);
		PutricielSporeGeometry.splash(s.p(), s.spores(), local, Vec3.ZERO, s.right(), s.up(), m.age(), 1.4D, 2.2D, 1.0F, m.seed() + 95);
		double shock = PutricielBloomRules.shockRadius(m.age(), r + 0.8D);
		if (shock > 0.0D) {
			PutricielSporeGeometry.curtain(s.p(), s.spores(), local, shock, 0.9D, Vec3.ZERO, s.right(), s.up(), s.time(),
					m.age() / PutricielBloomRules.SHOCK, 0.85F * (1.0F - m.age() / PutricielBloomRules.SHOCK), m.seed() + 97);
		}
	}

	/**
	 * Offering Grasp: a trench tears open along the lane from the body, ragged, filling with ooze, its lips frilled
	 * with bracket caps that unfurl as the tear reaches them; at the commit the palm lashes a reach of rot ooze out
	 * along it from where the animated palm is, holds through the active span and snaps back, and spores erupt out of
	 * the trench behind the reach's head.
	 */
	private static void grasp(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Lane lane)) return;
		double length = lane.length(), half = lane.halfWidth();
		Look look = m.look();
		if (s.ground()) {
			PutricielGroundGeometry.rect(s.p(), s.floor(), m.ground(), 0.0D, length, -half * 1.5D, half * 1.5D,
					Front.from(m.origin(), look.grow() * length, 1.0D), rot(look, 0.5F, 0.75F), m.seed());
			PutricielGroundGeometry.strip(s.p(), s.floor(), m.ground(), jag(m.ground(), 0.5D, length, 0.0D, 0.18D, m.seed()),
					half * 0.85D, look.grow(), look.width(), trench(look, 1.0F), m.seed() + 1);
		}
		if (s.solid()) {
			int k = 0;
			for (double along = 1.0D; along < length; along += 0.9D, k++) {
				for (int side = -1; side <= 1; side += 2) {
					int seed = m.seed() + 20 + k * 7 + side;
					double out = Math.atan2(m.ground().fx() * side, -m.ground().fz() * side);
					Vec3 foot = m.at(along, side * half * (0.85D + 0.2D * PutricielBloomRules.unit(seed, 3)));
					float sprout = m.taken(along / length);
					PutricielGrowthGeometry.rosette(s.p(), s.growth(), s.local(foot), frill(seed, out, 0.32D), m.moveTime() - sprout,
							m.windup() + m.active() + 10.0F - sprout, s.time(), PutricielGrowthGeometry.FRESH, seed);
				}
			}
		}
		if (!s.spore() || m.age() < 0.0F) return;
		float head = PutricielBloomRules.reachHead(m.age(), m.active());
		if (head > 0.0F) {
			Vec3 palm = m.boss().getPosition(s.partial()).add(PutricielAnimState.of(m.boss()).palm());
			Vec3 end = m.at(length * head, 0.0D).add(0.0D, 0.45D, 0.0D);
			List<Vec3> spine = new ArrayList<>(11);
			for (int i = 0; i <= 10; i++) {
				double t = i / 10.0D;
				// it leaves the palm, dips low over the trench and runs along it
				Vec3 at = palm.lerp(end, t).add(0.0D, -Math.sin(t * Math.PI) * 0.35D * (palm.y - end.y > 0.5D ? 1.0D : 0.3D), 0.0D);
				spine.add(s.local(at));
			}
			PutricielSporeGeometry.reach(s.p(), s.spores(), spine, Vec3.ZERO, s.right(), s.up(), s.time(), 0.17D, 1.0F, m.seed() + 60);
		}
		for (double along = 1.0D; along < length; along += 1.25D) {
			float own = m.age() - (float) (along / length * PutricielBloomRules.REACH_OUT);
			PutricielSporeGeometry.burst(s.p(), s.spores(), s.local(m.at(along, 0.0D)).add(0.0D, 0.2D, 0.0D), s.right(), s.up(), own, 0.7D, 4,
					0.4F, 0.9F, m.seed() + 70 + (int) (along * 10));
		}
	}

	/**
	 * Rot Breath: the cone's floor colonises and puffballs swell in it; spores gather into the hood; then the exhale,
	 * a billowing stream of spore cloud rolling out from the face along the cone, while the puffballs burst one after
	 * another from the body outward. The lingering cloud is the hazard's.
	 */
	private static void breath(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Cone cone)) return;
		double half = cone.degrees() / 2.0D, radius = cone.radius();
		Look look = m.look();
		boolean exhaling = m.age() >= 0.0F && m.age() < m.active();
		if (s.ground()) {
			PutricielGroundGeometry.sector(s.p(), s.floor(), m.ground(), 0.3D, radius, -half, half,
					Front.from(m.origin(), look.grow() * radius, 1.0D), new Paint(PutricielGroundGeometry.MAT, 0.4F * look.wet(),
							exhaling ? 1.0F : look.spore(), look.alpha()), m.seed());
		}
		for (int i = 0; i < 6; i++) {
			double bearing = (PutricielBloomRules.unit(m.seed(), 10 + i) - 0.5D) * cone.degrees() * 0.8D;
			double distance = radius * (0.35D + 0.55D * PutricielBloomRules.unit(m.seed(), 20 + i));
			Vec3 foot = m.at(Math.cos(Math.toRadians(bearing)) * distance, Math.sin(Math.toRadians(bearing)) * distance);
			float burst = m.age() - (float) (distance / radius * 6.0D);
			if (s.solid()) {
				PutricielGrowthGeometry.sac(s.p(), s.growth(), s.local(foot), 0.22D + 0.08D * PutricielBloomRules.unit(m.seed(), 30 + i), 0.36D,
						PutricielBloomRules.swell(m.moveTime() - m.reveal() - i * 1.2F, s.time(), m.seed() + i),
						PutricielBloomRules.tear(burst), PutricielGrowthGeometry.SAC_SKIN, m.seed() + 40 + i);
			} else if (s.spore()) {
				PutricielSporeGeometry.burst(s.p(), s.spores(), s.local(foot).add(0.0D, 0.3D, 0.0D), s.right(), s.up(), burst, 1.0D, 5,
						0.3F, 1.0F, m.seed() + 50 + i);
			}
		}
		if (!s.spore()) return;
		Vec3 face = m.boss().getPosition(s.partial()).add(PutricielAnimState.of(m.boss()).face());
		if (m.age() < 0.0F) {
			float gather = look.alpha() * m.build();
			for (int i = 0; i < 8; i++) {
				double phase = (s.time() * 0.04D + i / 8.0D) % 1.0D;
				double a = i * 0.785D + phase * 2.0D;
				double out = 1.3D * (1.0D - phase);
				PutricielSporeGeometry.mote(s.p(), s.spores(), s.local(face.add(Math.cos(a) * out, -0.3D * (1.0D - phase), Math.sin(a) * out)),
						s.right(), s.up(), 0.05D, 0.8F, gather * (float) Math.sin(phase * Math.PI), m.seed() + 60 + i);
			}
			return;
		}
		float envelope = 1.0F - HemoEasing.ramp(m.age(), m.active(), m.active() + 8.0F);
		if (envelope <= 0.0F) return;
		double travel = 9.0D;
		for (int k = 0; k < 20; k++) {
			double phase = (m.age() / travel + k / 20.0D) % 1.0D;
			if (phase * travel > m.age() + 0.5D) continue;
			double bearing = Math.toRadians((PutricielBloomRules.unit(m.seed(), 100 + k) - 0.5D) * cone.degrees() * 0.85D);
			double distance = phase * radius * 1.02D;
			double fx = m.ground().fx() * Math.cos(bearing) - m.ground().fz() * Math.sin(bearing);
			double fz = m.ground().fz() * Math.cos(bearing) + m.ground().fx() * Math.sin(bearing);
			double y = HemoEasing.lerp(face.y - 0.1D, m.floor() + 0.75D, Math.min(1.0D, phase * 1.4D));
			Vec3 at = new Vec3(face.x + fx * distance, y, face.z + fz * distance);
			double size = 0.32D + 1.1D * phase;
			PutricielSporeGeometry.puff(s.p(), s.spores(), s.local(at), s.right(), s.up(), size, (float) Math.pow(phase, 1.5D) * 0.8F,
					k % 3 == 0 ? 0.65F : 0.1F, 0.4F, envelope, m.seed() + 110 + k);
		}
	}

	/**
	 * Spore Sac Swell: the floor round the body colonises and a faint ring is torn at the wave's full reach; at the
	 * release a curtain of spores as tall as the standing band rolls out, puffs rolling at its foot and cresting on
	 * top, the floor behind it fruiting as it passes. It cannot be jumped, only walked out of.
	 */
	private static void sacSwell(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Ring ring)) return;
		Look look = m.look();
		double reach = ring.to();
		double radius = m.age() < 0.0F ? -1.0D : ring.radius(m.age(), 0);
		boolean rolling = radius >= 0.0D;
		if (m.age() >= 0.0F && !rolling) radius = reach;
		if (s.ground()) {
			if (m.age() < 0.0F) {
				PutricielGroundGeometry.sector(s.p(), s.floor(), m.ground(), 0.0D, 2.6D, -180.0D, 180.0D,
						Front.from(m.origin(), look.grow() * 2.6D, 0.6D), rot(look, 0.8F, 1.0F), m.seed());
				PutricielGroundGeometry.ring(s.p(), s.floor(), m.ground(), reach, 0.22D, look.grow(), 0.35F, trench(look, 0.55F),
						m.seed() + 3);
			} else {
				PutricielGroundGeometry.sector(s.p(), s.floor(), m.ground(), 0.0D, Math.min(reach, radius + 0.9D), -180.0D, 180.0D,
						Front.from(m.origin(), radius, 1.2D), new Paint(PutricielGroundGeometry.MAT, 0.6F * look.wet(), 1.0F, look.alpha()),
						m.seed() + 5);
			}
		}
		if (!s.spore()) return;
		Vec3 centre = s.local(m.origin());
		if (m.age() < 0.0F) {
			float rise = look.alpha() * m.build();
			for (int i = 0; i < 8; i++) {
				double phase = (s.time() * 0.03D + i / 8.0D) % 1.0D;
				double a = i * 0.785D + s.time() * 0.01D;
				PutricielSporeGeometry.puff(s.p(), s.spores(), centre.add(Math.cos(a) * (0.9D + phase), 0.3D + phase * 1.6D,
						Math.sin(a) * (0.9D + phase)), s.right(), s.up(), 0.25D + 0.25D * phase, (float) phase, 0.2F, 0.5F,
						rise * (float) Math.sin(phase * Math.PI), m.seed() + 10 + i);
			}
			return;
		}
		float progress = (float) HemoEasing.clamp01(radius / reach);
		float thin = rolling ? 0.1F + 0.45F * progress : 0.55F + 0.45F * HemoEasing.clamp01((m.age() - ring.travelTicks()) / 8.0F);
		float alpha = rolling ? 1.0F : 1.0F - HemoEasing.clamp01((m.age() - ring.travelTicks()) / 8.0F);
		PutricielSporeGeometry.curtain(s.p(), s.spores(), centre, radius, SAC_CURTAIN * (rolling ? 1.0D : 1.0D - 0.3D * (1.0F - alpha)),
				Vec3.ZERO, s.right(), s.up(), s.time(), thin, alpha, m.seed() + 20);
	}

	/**
	 * Shelf Volley: each landing colonises a little and is ringed with a shallow tear; bracket shelves torn off the
	 * back colony tumble through the air on arcs to them, shedding spores, and land as each disc lands: a splash of
	 * ooze, a rolling burst of spores and clods. The colony that stands after is the hazard's.
	 */
	private static void volley(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Discs discs)) return;
		Look look = m.look();
		List<Vec3> points = m.frame().points();
		Vec3 body = m.boss().getPosition(s.partial());
		Vec3 colony = body.add(-m.ground().fx() * COLONY_BACK, COLONY_HEIGHT, -m.ground().fz() * COLONY_BACK);
		for (int i = 0; i < points.size(); i++) {
			Vec3 point = points.get(i);
			Vec3 foot = new Vec3(point.x, m.floor(), point.z);
			int seed = m.seed() + i * 37;
			float land = m.age() - i * discs.landingGap();
			if (s.ground() && land < 1.0F) {
				Ground g = m.ground().at(point);
				PutricielGroundGeometry.sector(s.p(), s.floor(), g, 0.0D, discs.radius(), -180.0D, 180.0D,
						Front.from(foot, look.grow() * discs.radius(), 0.5D), new Paint(PutricielGroundGeometry.MAT, 0.25F, 0.35F,
								look.alpha() * 0.65F), seed);
				PutricielGroundGeometry.ring(s.p(), s.floor(), g, discs.radius(), 0.16D, look.grow(), 0.45F, trench(look, 0.7F), seed + 1);
			}
			float t = (land + PutricielBloomRules.THROW) / PutricielBloomRules.THROW;
			if (t > 0.0F && t < 1.0F) {
				Vec3 at = colony.lerp(foot, t).add(0.0D, PutricielBloomRules.arc(t, 2.4D), 0.0D);
				if (s.solid()) {
					s.p().pushPose();
					Vec3 local = s.local(at);
					s.p().translate(local.x, local.y, local.z);
					s.p().mulPose(new Quaternionf().rotationXYZ(t * 7.0F + i, t * 3.0F, t * 5.0F + i * 0.7F));
					List<Cap> shelf = PutricielBloomRules.rosette(seed, 0.6D, 2);
					for (Cap cap : shelf) {
						PutricielGrowthGeometry.cap(s.p(), s.growth(), Vec3.ZERO, cap, 1.0D, 0.0D, 0.0D, PutricielGrowthGeometry.FRESH, 1.0F,
								seed + 3);
					}
					s.p().popPose();
				} else if (s.spore()) {
					for (int k = 1; k <= 3; k++) {
						float back = Math.max(0.0F, t - k * 0.07F);
						Vec3 trail = colony.lerp(foot, back).add(0.0D, PutricielBloomRules.arc(back, 2.4D), 0.0D);
						PutricielSporeGeometry.puff(s.p(), s.spores(), s.local(trail), s.right(), s.up(), 0.15D + 0.07D * k, 0.3F + 0.2F * k,
								0.2F, 0.4F, 0.8F, seed + 10 + k);
					}
				}
			}
			if (land >= 0.0F) {
				if (s.solid()) {
					PutricielGrowthGeometry.clods(s.p(), s.growth(), s.local(foot), land, 1.6D, 6, PutricielGrowthGeometry.LOAM, seed + 20);
				} else if (s.spore()) {
					PutricielSporeGeometry.splash(s.p(), s.spores(), s.local(foot), Vec3.ZERO, s.right(), s.up(), land, 0.9D, 1.5D, 1.0F,
							seed + 30);
					PutricielSporeGeometry.burst(s.p(), s.spores(), s.local(foot).add(0.0D, 0.25D, 0.0D), s.right(), s.up(), land, 1.2D, 6,
							0.3F, 1.0F, seed + 40);
				}
			}
		}
	}

	/**
	 * Root Snare: a crack trench races across the floor from the body to the quarry, then the cross tears open there,
	 * colonised and filling with ooze; at the commit roots breach along all four arms in arches and a clutch of them
	 * rises round the centre and closes over it, throwing clods; they hold through the active span, then wither and
	 * crumble back into the floor.
	 */
	private static void snare(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Cross cross)) return;
		Look look = m.look();
		Vec3 aim = m.frame().anchor(cross.at());
		Vec3 centre = new Vec3(aim.x, m.floor(), aim.z);
		Ground g = m.ground().at(aim);
		double arm = cross.arm(), half = cross.halfWidth();
		float build = m.build();
		float race = m.age() >= 0.0F ? 1.0F : HemoEasing.clamp01(build / 0.45F);
		float open = m.age() >= 0.0F ? 1.0F : HemoEasing.easeOutCubic(HemoEasing.clamp01((build - 0.35F) / 0.65F));
		if (s.ground()) {
			double distance = Shapes.planar(m.frame().origin(), aim);
			if (distance > 1.0D) {
				PutricielGroundGeometry.strip(s.p(), s.floor(), m.ground(), jag(m.ground(), 0.6D, distance, 0.0D, 0.25D, m.seed()), 0.2D,
						race, 0.45F, trench(look, 0.85F), m.seed() + 1);
			}
			for (int bar = 0; bar < 2; bar++) {
				Ground turned = g.turned(bar * 90.0D);
				PutricielGroundGeometry.rect(s.p(), s.floor(), turned, -arm, arm, -half * 1.6D, half * 1.6D,
						Front.from(centre, open * arm, 0.8D), rot(look, 0.8F, 0.9F), m.seed() + 2 + bar);
			}
			for (int a = 0; a < 4; a++) {
				Ground turned = g.turned(a * 90.0D);
				PutricielGroundGeometry.strip(s.p(), s.floor(), turned, jag(turned, 0.0D, arm, 0.0D, 0.15D, m.seed() + 10 + a), half * 0.8D,
						open, look.width(), trench(look, 1.0F), m.seed() + 10 + a);
			}
		}
		if (m.age() < 0.0F) return;
		float witherAt = m.active() + 4.0F;
		float formation = 1.0F - HemoEasing.ramp(m.age(), witherAt, witherAt + PutricielBloomRules.WITHER);
		if (s.solid()) {
			for (int a = 0; a < 4; a++) {
				Ground turned = g.turned(a * 90.0D);
				for (int j = 0; j < 2; j++) {
					int seed = m.seed() + 30 + a * 5 + j;
					double d0 = 0.5D + j * 1.9D, d1 = d0 + 1.7D + 0.4D * PutricielBloomRules.unit(seed, 1);
					double offset = (PutricielBloomRules.unit(seed, 2) - 0.5D) * half;
					double rise = 0.55D + 0.3D * PutricielBloomRules.unit(seed, 3);
					List<Vec3> spine = new ArrayList<>(8);
					double[] radii = new double[8];
					for (int k = 0; k < 8; k++) {
						double t = k / 7.0D;
						spine.add(s.local(turned.world(HemoEasing.lerp(d0, d1, t), offset).add(0.0D, Math.sin(t * Math.PI) * rise - 0.05D, 0.0D)));
						radii[k] = 0.11D * (1.0D - 0.45D * t);
					}
					PutricielGrowthGeometry.root(s.p(), s.growth(), spine, radii, PutricielBloomRules.breach(m.age() - j * 0.8F), formation,
							PutricielGrowthGeometry.BARK, seed);
				}
			}
			for (int k = 0; k < 5; k++) {
				int seed = m.seed() + 60 + k;
				double a = k * Math.PI * 2.0D / 5.0D + PutricielBloomRules.unit(seed, 1);
				Vec3 base = centre.add(Math.cos(a) * 0.75D, -0.05D, Math.sin(a) * 0.75D);
				Vec3 top = centre.add(Math.cos(a) * 0.12D, 1.55D + 0.2D * PutricielBloomRules.unit(seed, 2), Math.sin(a) * 0.12D);
				List<Vec3> spine = new ArrayList<>(7);
				double[] radii = new double[7];
				for (int i = 0; i < 7; i++) {
					double t = i / 6.0D;
					Vec3 at = base.lerp(top, t).add(Math.cos(a) * Math.sin(t * Math.PI) * 0.35D, 0.0D, Math.sin(a) * Math.sin(t * Math.PI) * 0.35D);
					spine.add(s.local(at));
					radii[i] = 0.1D * (1.0D - 0.6D * t);
				}
				PutricielGrowthGeometry.root(s.p(), s.growth(), spine, radii, PutricielBloomRules.breach(m.age() - 0.5F - k * 0.3F), formation,
						PutricielGrowthGeometry.BARK, seed);
			}
			PutricielGrowthGeometry.clods(s.p(), s.growth(), s.local(centre), m.age(), 2.0D, 8, PutricielGrowthGeometry.LOAM, m.seed() + 70);
		} else if (s.spore()) {
			PutricielSporeGeometry.burst(s.p(), s.spores(), s.local(centre).add(0.0D, 0.3D, 0.0D), s.right(), s.up(), m.age(), 1.4D, 7, 0.4F,
					1.0F, m.seed() + 80);
			for (int a = 0; a < 4; a++) {
				PutricielSporeGeometry.burst(s.p(), s.spores(), s.local(g.turned(a * 90.0D).world(arm * 0.8D, 0.0D)).add(0.0D, 0.25D, 0.0D),
						s.right(), s.up(), m.age() - 1.5F, 0.8D, 4, 0.3F, 0.9F, m.seed() + 90 + a);
			}
		}
	}

	/**
	 * Censer Orbit: the annulus colonises out from its inner edge and a cream-frilled trench marks the eye's edge, the
	 * safe ground; through the active span each censer's arc rolls round as a wedge of billowing spore cloud with its
	 * wake thinning behind it, the floor it has passed fruiting, and the censers' own paths smoking.
	 */
	private static void orbit(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Annulus annulus)) return;
		Look look = m.look();
		double inner = annulus.inner(), outer = annulus.outer();
		if (s.ground()) {
			PutricielGroundGeometry.sector(s.p(), s.floor(), m.ground(), inner, outer, -180.0D, 180.0D,
					Front.from(m.origin(), inner + look.grow() * (outer - inner), 0.8D), rot(look, 0.6F, 0.8F), m.seed());
			PutricielGroundGeometry.ring(s.p(), s.floor(), m.ground(), inner, 0.2D, look.grow(), 0.5F,
					new Paint(PutricielGroundGeometry.TRENCH, 0.4F, 0.9F, look.alpha()), m.seed() + 1);
			if (m.age() >= 0.0F && m.age() < m.active()) {
				for (int arc = 0; arc < annulus.arcs(); arc++) {
					double angle = annulus.arcAngle(m.age(), arc);
					PutricielGroundGeometry.sector(s.p(), s.floor(), m.ground(), inner, outer, angle - annulus.arcDegrees() / 2.0D - 60.0D,
							angle + annulus.arcDegrees() / 2.0D, Front.whole(m.origin()),
							new Paint(PutricielGroundGeometry.MAT, 0.8F, 1.0F, look.alpha() * 0.7F), m.seed() + 3 + arc);
				}
			}
		}
		if (!s.spore() || m.age() < 0.0F) return;
		float envelope = 1.0F - HemoEasing.ramp(m.age(), m.active(), m.active() + 8.0F);
		if (envelope <= 0.0F) return;
		float clamped = Math.min(m.age(), m.active());
		for (int arc = 0; arc < annulus.arcs(); arc++) {
			for (int step = 0; step < 4; step++) {
				// the wedge now, then its wake fading behind it
				double angle = annulus.arcAngle(clamped - step * 1.5F, arc);
				float thin = step == 0 ? 0.1F : 0.3F + 0.18F * step;
				for (int r = 0; r < 3; r++) {
					for (int a = 0; a < (step == 0 ? 4 : 2); a++) {
						int seed = m.seed() + 100 + arc * 97 + step * 31 + r * 7 + a;
						double bearing = angle - annulus.arcDegrees() / 2.0D + annulus.arcDegrees() * (a + 0.5D) / (step == 0 ? 4 : 2);
						double distance = inner + (outer - inner) * (r + 0.5D) / 3.0D;
						Vec3 at = m.at(Math.cos(Math.toRadians(bearing)) * distance, Math.sin(Math.toRadians(bearing)) * distance)
								.add(0.0D, 0.5D + 0.8D * PutricielBloomRules.unit(seed, 1), 0.0D);
						PutricielSporeGeometry.puff(s.p(), s.spores(), s.local(at), s.right(), s.up(), 0.55D + 0.15D * r, thin,
								a % 2 == 0 ? 0.55F : 0.1F, 0.4F, envelope, seed);
					}
				}
			}
		}
		for (int c = 0; c < 2; c++) {
			PutricielSporeGeometry.wake(s.p(), s.spores(), trail(s, m, c, clamped - 5.0F, clamped, 8), Vec3.ZERO, s.right(), s.up(), s.time(),
					0.24D, PutricielBloomRules.fray(m.age(), m.active()), envelope * 0.8F, m.seed() + 200 + c);
		}
	}

	/**
	 * Mould Communion: under the kneeling palm the floor colonises and rot pools out into it, breathing; rosettes
	 * push up round the stain; and spores drift in from all round the chamber in trains of puffs that shrink as the
	 * palm draws them in.
	 */
	private static void communion(Scene s, Move m) {
		float draw = HemoEasing.window(m.moveTime(), 14.0F, 20.0F, m.windup() + m.active() + 2.0F, m.windup() + m.active() + 14.0F);
		if (draw <= 0.0F) return;
		Vec3 palm = m.boss().getPosition(s.partial()).add(PutricielAnimState.of(m.boss()).palm());
		Vec3 stain = new Vec3(palm.x, m.floor(), palm.z);
		float breath = (float) Math.pow(Math.max(0.0D, Math.sin(s.time() * 0.08D)), 3.0D);
		if (s.ground()) {
			PutricielGroundGeometry.sector(s.p(), s.floor(), m.ground().at(stain), 0.0D, 2.4D, -180.0D, 180.0D,
					Front.from(stain, 2.4D * HemoEasing.ramp(m.moveTime(), 14.0F, m.windup() + m.active() * 0.5F), 0.7D),
					new Paint(PutricielGroundGeometry.MAT, 0.9F, 0.45F + 0.4F * breath, draw), m.seed());
		}
		if (s.solid()) {
			for (int i = 0; i < 4; i++) {
				double a = i * 1.5708D + PutricielBloomRules.unit(m.seed(), i);
				Vec3 foot = stain.add(Math.cos(a) * (1.1D + 0.4D * PutricielBloomRules.unit(m.seed(), 10 + i)), 0.0D, Math.sin(a) * 1.2D);
				float sprout = 18.0F + i * 6.0F;
				PutricielGrowthGeometry.rosette(s.p(), s.growth(), s.local(foot), PutricielBloomRules.rosette(m.seed() + 20 + i, 0.45D, 4),
						m.moveTime() - sprout, m.windup() + m.active() + 2.0F - sprout, s.time(), PutricielGrowthGeometry.FRESH, m.seed() + 20 + i);
			}
		}
		if (!s.spore()) return;
		Vec3 into = s.local(palm);
		for (int i = 0; i < 12; i++) {
			double a = i * 2.39996D + m.seed() * 0.1D;
			Vec3 from = s.local(m.origin().add(Math.cos(a) * 7.5D, 2.6D, Math.sin(a) * 7.5D));
			for (int k = 0; k < 3; k++) {
				double phase = (s.time() * 0.018D + i / 12.0D + k * 0.33D) % 1.0D;
				Vec3 at = from.lerp(into, phase).add(0.0D, Math.sin(phase * Math.PI) * 0.8D, 0.0D);
				PutricielSporeGeometry.puff(s.p(), s.spores(), at, s.right(), s.up(), 0.4D * (1.0D - phase * 0.7D), 0.25F, 0.1F, 0.6F,
						draw * (float) Math.sin(phase * Math.PI) * 0.9F, m.seed() + 40 + i * 3 + k);
			}
		}
	}

	/**
	 * Bloom of Rot: the field colonises out from the body cell by cell, stopping short of the ash, the pyre circles
	 * and the body with a fringe toward them, so the safe ground reads at a glance; rosettes and sacs push up across
	 * it from the body outward; each pulse heaves a low ring of spores out across the field, every rosette puffing and
	 * a third of the sacs bursting; then it all withers.
	 */
	private static void bloom(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Region region)) return;
		Look look = m.look();
		double radius = region.radius();
		var excluded = m.frame().cells();
		Ground g = new Ground(m.frame().origin().x, m.frame().origin().z, 1.0D, 0.0D, m.floor(), s.camera());
		if (s.ground()) {
			float pulse = 0.0F;
			for (int k = 0; k < 3; k++) pulse = Math.max(pulse, HemoEasing.pulse(m.age() - k * PutricielMove.BLOOM_PULSE, 2.0F, 12.0F));
			PutricielGroundGeometry.cells(s.p(), s.floor(), g, radius, cell -> !excluded.contains(cell),
					Front.from(m.origin(), look.grow() * radius, 1.5D), new Paint(PutricielGroundGeometry.MAT, 0.7F * look.wet(),
							Math.min(1.0F, look.spore() + 0.5F * pulse), look.alpha()), m.seed());
			for (int k = 0; k < 3; k++) {
				float age = m.age() - k * PutricielMove.BLOOM_PULSE;
				if (age < 0.0F || age > 10.0F) continue;
				double ring = radius * HemoEasing.easeOutCubic(age / 10.0F);
				PutricielGroundGeometry.sector(s.p(), s.floor(), g, Math.max(0.0D, ring - 1.2D), ring + 0.5D, -180.0D, 180.0D,
						Front.whole(m.origin()), new Paint(PutricielGroundGeometry.MAT, 1.0F, 1.0F, 0.65F * (1.0F - age / 10.0F)), m.seed() + 5 + k);
			}
		}
		float witherAt = m.windup() + m.active() + m.recovery() * 0.5F;
		for (int i = 0; i < 18; i++) {
			int seed = m.seed() + 30 + i * 13;
			double a = PutricielBloomRules.unit(seed, 1) * Math.PI * 2.0D;
			double d = 1.8D + (radius * 0.92D - 1.8D) * Math.sqrt(PutricielBloomRules.unit(seed, 2));
			Vec3 foot = m.origin().add(Math.cos(a) * d, 0.0D, Math.sin(a) * d);
			if (excluded.contains(PutricielGroundGeometry.cell((int) Math.floor(foot.x), (int) Math.floor(foot.z)))) continue;
			float sprout = m.taken(d / radius);
			boolean sac = i % 3 == 2;
			int pulseOf = i % 3;
			float burst = m.age() - pulseOf * PutricielMove.BLOOM_PULSE - (float) (d / radius * 4.0D);
			if (s.solid()) {
				if (sac) {
					PutricielGrowthGeometry.sac(s.p(), s.growth(), s.local(foot), 0.3D, 0.5D,
							PutricielBloomRules.swell(m.moveTime() - sprout, s.time(), seed), PutricielBloomRules.tear(burst),
							PutricielGrowthGeometry.SAC_SKIN, seed);
				} else {
					PutricielGrowthGeometry.rosette(s.p(), s.growth(), s.local(foot),
							PutricielBloomRules.rosette(seed, 0.6D + 0.35D * PutricielBloomRules.unit(seed, 3), 5), m.moveTime() - sprout,
							witherAt - sprout, s.time(), PutricielGrowthGeometry.withBloom(PutricielGrowthGeometry.FRESH, look.spore()), seed);
				}
			} else if (s.spore()) {
				if (sac) {
					PutricielSporeGeometry.burst(s.p(), s.spores(), s.local(foot).add(0.0D, 0.35D, 0.0D), s.right(), s.up(), burst, 1.1D, 5,
							0.3F, 1.0F, seed + 5);
				} else {
					for (int k = 0; k < 3; k++) {
						float age = m.age() - k * PutricielMove.BLOOM_PULSE - (float) (d / radius * 6.0D);
						PutricielSporeGeometry.burst(s.p(), s.spores(), s.local(foot).add(0.0D, 0.5D, 0.0D), s.right(), s.up(), age, 0.7D, 3,
								0.15F, 0.8F, seed + 10 + k);
					}
				}
			}
		}
		if (!s.spore()) return;
		for (int k = 0; k < 3; k++) {
			float age = m.age() - k * PutricielMove.BLOOM_PULSE;
			if (age < 0.0F || age > 10.0F) continue;
			PutricielSporeGeometry.curtain(s.p(), s.spores(), s.local(m.origin()), radius * HemoEasing.easeOutCubic(age / 10.0F), 1.1D, Vec3.ZERO,
					s.right(), s.up(), s.time(), 0.2F + 0.6F * age / 10.0F, 0.85F * (1.0F - age / 10.0F), m.seed() + 90 + k);
		}
	}

	// ── fire ─────────────────────────────────────────────────────────

	/**
	 * Ignition: the floor round the body scorches black out to six blocks and a furrow burns into it at the edge,
	 * low tongues licking along it; at the release the purge sweeps out from the body as a ring of tall tongues with a
	 * lower wake behind it, sparks thrown out ahead, and the heat flashes at the core.
	 */
	private static void ignition(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Disc disc)) return;
		Burn burn = m.burn();
		double radius = disc.radius();
		if (s.ground()) {
			PutricielGroundGeometry.sector(s.p(), s.floor(), m.ground(), 0.0D, radius, -180.0D, 180.0D,
					Front.from(m.origin(), burn.grow() * radius, 0.9D), scorch(burn, 1.0F), m.seed());
			PutricielGroundGeometry.ring(s.p(), s.floor(), m.ground(), radius, 0.3D, 1.0D, burn.width(), furrow(burn, 1.0F), m.seed() + 1);
		}
		if (!s.spore()) return;
		Vec3 centre = s.local(m.origin());
		if (m.age() < 0.0F) {
			PutricielFlameGeometry.ring(s.p(), s.flame(), centre, radius, 0.55D, (0.22D + 0.35D * m.build()) * burn.alpha(), 0.08D, s.right(),
					s.up(), s.time(), m.seed() + 2, burn.alpha());
			PutricielFlameGeometry.bed(s.p(), s.flame(), centre, 0.8D + m.build(), m.seed() + 3, 0.6F * m.build());
			return;
		}
		float out = HemoEasing.easeOutCubic(HemoEasing.clamp01(m.age() / Math.max(1.0F, m.active())));
		float die = 1.0F - HemoEasing.ramp(m.age(), m.active(), m.active() + 12.0F);
		if (die > 0.0F) {
			PutricielFlameGeometry.ring(s.p(), s.flame(), centre, Math.max(0.4D, radius * out), 0.42D, 1.35D * die, 0.25D, s.right(), s.up(),
					s.time(), m.seed() + 4, die);
			PutricielFlameGeometry.ring(s.p(), s.flame(), centre, Math.max(0.3D, radius * out * 0.78D), 0.6D, 0.55D * die, 0.1D, s.right(),
					s.up(), s.time(), m.seed() + 5, 0.8F * die);
		}
		PutricielFlameGeometry.sparks(s.p(), s.flame(), centre.add(0.0D, 0.5D, 0.0D), Vec3.ZERO, s.right(), s.up(), m.age(), 4.0D, 16,
				m.seed() + 6, 1.0F);
		PutricielFlameGeometry.flash(s.p(), s.flame(), centre.add(0.0D, 1.5D, 0.0D), s.right(), s.up(), m.age(), 1.4D, m.seed() + 7, 1.0F);
	}

	/**
	 * Purging Sweep: a furrow burns along the lane into the scorched floor, coals glowing in it, while the censers'
	 * real paths trail flame; at the commit the fire runs out along the lane behind its front, tall tongues leaping
	 * higher toward the head, sparks off the head, and dies back to embers. The wall at its end is the hazard's.
	 */
	private static void purgingSweep(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Lane lane)) return;
		Burn burn = m.burn();
		double length = lane.length(), half = lane.halfWidth();
		if (s.ground()) {
			PutricielGroundGeometry.rect(s.p(), s.floor(), m.ground(), 0.0D, length, -half * 1.5D, half * 1.5D,
					Front.from(m.origin(), burn.grow() * length, 1.0D), scorch(burn, 0.9F), m.seed());
			PutricielGroundGeometry.strip(s.p(), s.floor(), m.ground(), jag(m.ground(), 0.5D, length, 0.0D, 0.15D, m.seed()), half * 0.8D,
					burn.grow(), burn.width(), furrow(burn, 1.0F), m.seed() + 1);
		}
		if (!s.spore()) return;
		Vec3 start = s.local(m.at(0.6D, 0.0D));
		if (m.age() < 0.0F) {
			Vec3 head = s.local(m.at(Math.max(0.7D, burn.grow() * length), 0.0D));
			PutricielFlameGeometry.row(s.p(), s.flame(), start, head, 0.75D, 0.16D + 0.22D * m.build(), PutricielFlameGeometry.Profile.FLAT,
					Vec3.ZERO, s.right(), s.up(), s.time(), m.seed() + 2, burn.alpha());
		} else {
			double front = Math.max(0.7D, lane.front(m.age(), 0));
			float die = 1.0F - HemoEasing.ramp(m.age(), m.active(), m.active() + PutricielBloomRules.COOL);
			Vec3 head = s.local(m.at(front, 0.0D));
			Vec3 lean = new Vec3(m.ground().fx() * 0.18D, 0.0D, m.ground().fz() * 0.18D);
			PutricielFlameGeometry.row(s.p(), s.flame(), start, head, 0.42D, 1.9D * Math.max(0.15F, die),
					f -> 0.5D + 0.5D * f * PutricielBloomRules.flare(m.age() - (float) (f * front / Math.max(0.01D, lane.frontSpeed()))), lean,
					s.right(), s.up(), s.time(), m.seed() + 3, Math.max(0.4F, die));
			// the fire fills the lane's width, not one file down its middle: lower tongues licking off both lips
			for (int side = -1; side <= 1; side += 2) {
				Vec3 from = s.local(m.at(1.0D, side * half * 0.55D)), to = s.local(m.at(front, side * half * 0.55D));
				Vec3 out = new Vec3(-m.ground().fz() * side * 0.12D, 0.0D, m.ground().fx() * side * 0.12D).add(lean.scale(0.6D));
				PutricielFlameGeometry.row(s.p(), s.flame(), from, to, 0.6D, 1.1D * Math.max(0.15F, die),
						f -> 0.45D + 0.55D * f * PutricielBloomRules.flare(m.age() - (float) (f * front / Math.max(0.01D, lane.frontSpeed()))),
						out, s.right(), s.up(), s.time(), m.seed() + 7 + side, Math.max(0.35F, die));
			}
			PutricielFlameGeometry.sparks(s.p(), s.flame(), head.add(0.0D, 0.6D, 0.0D), lean, s.right(), s.up(), m.age(), 2.4D, 10,
					m.seed() + 4, 1.0F);
		}
		// the sweep itself: fire along the staff head's and both censers' real paths through the swing
		float wake = PutricielBloomRules.wake(m.age(), m.active());
		for (int limb = CENSER_LEFT; limb <= STAFF_HEAD && wake > 0.0F; limb++) {
			if (limb == PALM) continue;
			limbFire(s, m, limb, -PutricielBloomRules.WAKE_LEAD, Math.min(m.age(), m.active() + 1.0F), wake);
		}
	}

	/**
	 * Censer Cast: the target disc scorches and a furrow rings it; the censer's real whirl overhead trails flame and
	 * sparks; at the commit the censer strikes: a column of fire leaps at the aim, a ring of tongues runs out to the
	 * disc's edge, sparks scatter. The purging fire left standing is the hazard's.
	 */
	private static void censerCast(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Disc disc)) return;
		Burn burn = m.burn();
		double r = disc.radius();
		Vec3 aim = m.frame().aim();
		Vec3 centre = new Vec3(aim.x, m.floor(), aim.z);
		Ground g = m.ground().at(aim);
		if (s.ground()) {
			PutricielGroundGeometry.sector(s.p(), s.floor(), g, 0.0D, r, -180.0D, 180.0D, Front.from(centre, burn.grow() * r, 0.7D),
					scorch(burn, 1.0F), m.seed());
			PutricielGroundGeometry.ring(s.p(), s.floor(), g, r, 0.22D, 1.0D, burn.width(), furrow(burn, 0.9F), m.seed() + 1);
		}
		if (!s.spore()) return;
		if (m.age() < 1.0F) {
			for (int c = CENSER_LEFT; c <= CENSER_RIGHT; c++) limbFire(s, m, c, -9.0F, Math.min(m.age(), 0.0F), burn.alpha());
		}
		if (m.age() < 0.0F) return;
		Vec3 local = s.local(centre);
		float die = 1.0F - HemoEasing.ramp(m.age(), m.active() + 2.0F, m.active() + 12.0F);
		PutricielFlameGeometry.column(s.p(), s.flame(), local, 0.5D, 1.9D * PutricielBloomRules.flare(m.age()) * die, s.right(), s.up(),
				s.time(), m.seed() + 2, die);
		if (m.age() < 8.0F) {
			PutricielFlameGeometry.ring(s.p(), s.flame(), local, Math.max(0.3D, r * HemoEasing.easeOutCubic(m.age() / 4.0F)), 0.45D,
					1.0D * (1.0F - m.age() / 8.0F), 0.3D, s.right(), s.up(), s.time(), m.seed() + 3, 1.0F - m.age() / 8.0F);
		}
		PutricielFlameGeometry.sparks(s.p(), s.flame(), local.add(0.0D, 0.4D, 0.0D), Vec3.ZERO, s.right(), s.up(), m.age(), 3.0D, 12,
				m.seed() + 4, 1.0F);
		PutricielFlameGeometry.flash(s.p(), s.flame(), local.add(0.0D, 0.6D, 0.0D), s.right(), s.up(), m.age(), 1.0D, m.seed() + 5, 1.0F);
	}

	/**
	 * Cauterising Lane: from the lit pyre a wide furrow burns toward the body through the scorched floor, coals
	 * glowing; through the active span the front runs down it, a wall of tall tongues four blocks long, tallest at
	 * its head, the furrow behind it still burning low and dying, sparks thrown off the head.
	 */
	private static void cauterise(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Path path)) return;
		List<Vec3> points = m.frame().points();
		if (points.size() < 2) return;
		Burn burn = m.burn();
		List<Vec3> floor = new ArrayList<>(points.size());
		for (Vec3 point : points) floor.add(new Vec3(point.x, m.floor(), point.z));
		double total = Shapes.pathLength(floor);
		if (total < 0.5D) return;
		Vec3 a = floor.get(0), b = floor.get(floor.size() - 1);
		double dx = b.x - a.x, dz = b.z - a.z, l = Math.max(1.0E-4D, Math.hypot(dx, dz));
		Ground g = new Ground(a.x, a.z, dx / l, dz / l, m.floor(), s.camera());
		double half = path.halfWidth();
		if (s.ground()) {
			PutricielGroundGeometry.rect(s.p(), s.floor(), g, 0.0D, total, -half * 1.3D, half * 1.3D, Front.from(a, burn.grow() * total, 1.2D),
					scorch(burn, 0.9F), m.seed());
			PutricielGroundGeometry.strip(s.p(), s.floor(), g, floor, half * 0.7D, burn.grow(), burn.width(), furrow(burn, 1.0F), m.seed() + 1);
		}
		if (!s.spore()) return;
		Vec3 lean = new Vec3(dx / l * 0.15D, 0.0D, dz / l * 0.15D);
		// the palm turns down and sweeps low across the body: fire follows its real path through the last of the wind-up
		float sweep = HemoEasing.window(m.age(), -10.0F, -6.0F, 0.0F, PutricielBloomRules.WAKE_LINGER);
		if (sweep > 0.0F) limbFire(s, m, PALM, -8.0F, Math.min(m.age(), 1.0F), sweep);
		if (m.age() < 0.0F) {
			PutricielFlameGeometry.row(s.p(), s.flame(), s.local(a), s.local(Shapes.pointAlong(floor, Math.max(0.6D, burn.grow() * total))),
					0.8D, 0.18D + 0.2D * m.build(), PutricielFlameGeometry.Profile.FLAT, Vec3.ZERO, s.right(), s.up(), s.time(), m.seed() + 2,
					burn.alpha());
			return;
		}
		float die = 1.0F - HemoEasing.ramp(m.age(), m.active(), m.active() + PutricielBloomRules.COOL);
		double head = Math.min(total, path.walker(m.age(), 0));
		double tail = Math.max(0.0D, head - path.frontLength());
		if (m.age() < m.active() && head > 0.1D) {
			PutricielFlameGeometry.row(s.p(), s.flame(), s.local(Shapes.pointAlong(floor, tail)), s.local(Shapes.pointAlong(floor, head)), 0.38D,
					2.1D, f -> 0.55D + 0.45D * f, lean, s.right(), s.up(), s.time(), m.seed() + 3, 1.0F);
			PutricielFlameGeometry.sparks(s.p(), s.flame(), s.local(Shapes.pointAlong(floor, head)).add(0.0D, 0.6D, 0.0D), lean, s.right(), s.up(),
					(float) ((m.age() * 1.3D) % 10.0D), 2.0D, 6, m.seed() + 4 + (int) (m.age() / 10.0F), 1.0F);
		}
		double behind = m.age() < m.active() ? tail : total;
		if (behind > 0.2D) {
			PutricielFlameGeometry.row(s.p(), s.flame(), s.local(a), s.local(Shapes.pointAlong(floor, behind)), 0.7D, 0.55D * die,
					f -> 0.35D + 0.65D * f, Vec3.ZERO, s.right(), s.up(), s.time(), m.seed() + 5, die);
		}
	}

	/**
	 * Pyre Heart: the disc round the body scorches, its cracks heating, and a ring of tongues licks up round its edge
	 * leaning in while the core's glow swells; at the commit a column of fire erupts from the body, a ring of tongues
	 * races out to the edge, sparks fly and soot climbs.
	 */
	private static void pyreHeart(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Disc disc)) return;
		Burn burn = m.burn();
		double r = disc.radius();
		if (s.ground()) {
			PutricielGroundGeometry.sector(s.p(), s.floor(), m.ground(), 0.0D, r, -180.0D, 180.0D, Front.from(m.origin(), burn.grow() * r, 0.6D),
					scorch(burn, 1.0F), m.seed());
			PutricielGroundGeometry.ring(s.p(), s.floor(), m.ground(), r, 0.26D, 1.0D, burn.width(), furrow(burn, 1.0F), m.seed() + 1);
		}
		if (!s.spore()) return;
		Vec3 centre = s.local(m.origin());
		if (m.age() < 0.0F) {
			float b = m.build();
			PutricielFlameGeometry.ring(s.p(), s.flame(), centre, r, 0.6D, 0.2D + 0.65D * b * b, -0.15D, s.right(), s.up(), s.time(),
					m.seed() + 2, burn.alpha());
			PutricielFlameGeometry.bed(s.p(), s.flame(), centre, 0.6D + 1.4D * b, m.seed() + 3, 0.7F * b);
			return;
		}
		float die = 1.0F - HemoEasing.ramp(m.age(), m.active(), m.active() + 14.0F);
		PutricielFlameGeometry.column(s.p(), s.flame(), centre, 0.6D, 3.4D * PutricielBloomRules.flare(m.age()) * die, s.right(), s.up(),
				s.time(), m.seed() + 4, die);
		if (m.age() < 7.0F) {
			PutricielFlameGeometry.ring(s.p(), s.flame(), centre, Math.max(0.4D, r * HemoEasing.easeOutCubic(m.age() / 3.0F)), 0.42D,
					1.25D * (1.0F - m.age() / 7.0F), 0.3D, s.right(), s.up(), s.time(), m.seed() + 5, 1.0F - m.age() / 7.0F);
		}
		PutricielFlameGeometry.sparks(s.p(), s.flame(), centre.add(0.0D, 1.2D, 0.0D), Vec3.ZERO, s.right(), s.up(), m.age(), 4.5D, 18,
				m.seed() + 6, 1.0F);
		PutricielFlameGeometry.flash(s.p(), s.flame(), centre.add(0.0D, 1.6D, 0.0D), s.right(), s.up(), m.age(), 1.6D, m.seed() + 7, 1.0F);
	}

	/**
	 * Pyre Lines: from every flared pyre a furrow burns in toward the body, an ember running at its head; through the
	 * active span each line stands as a row of tongues, the wedges between them clean scorched floor, the safe read;
	 * then they die back.
	 */
	private static void pyreLines(Scene s, Move m) {
		List<Vec3> anchors = m.frame().anchors();
		Burn burn = m.burn();
		Vec3 body = m.origin();
		for (int i = 0; i < anchors.size(); i++) {
			Vec3 pyre = new Vec3(anchors.get(i).x, m.floor(), anchors.get(i).z);
			double dx = body.x - pyre.x, dz = body.z - pyre.z, l = Math.hypot(dx, dz);
			if (l < 0.8D) continue;
			Ground g = new Ground(pyre.x, pyre.z, dx / l, dz / l, m.floor(), s.camera());
			int seed = m.seed() + i * 29;
			if (s.ground()) {
				PutricielGroundGeometry.rect(s.p(), s.floor(), g, 0.0D, l, -0.9D, 0.9D, Front.from(pyre, burn.grow() * l, 1.0D), scorch(burn, 0.8F),
						seed);
				PutricielGroundGeometry.strip(s.p(), s.floor(), g, jag(g, 0.3D, l, 0.0D, 0.12D, seed), 0.45D, burn.grow(), burn.width(),
						furrow(burn, 1.0F), seed + 1);
				continue;
			}
			if (!s.spore()) continue;
			if (m.age() < 0.0F) {
				Vec3 head = s.local(g.world(Math.max(0.3D, burn.grow() * l), 0.0D));
				PutricielFlameGeometry.tongue(s.p(), s.flame(), head, Vec3.ZERO, 0.35D + 0.25D * m.build(), 0.09D, s.time(), seed + 2, burn.alpha());
				PutricielFlameGeometry.bed(s.p(), s.flame(), head, 0.4D, seed + 3, 0.6F * burn.alpha());
				continue;
			}
			float die = 1.0F - HemoEasing.ramp(m.age(), m.active(), m.active() + PutricielBloomRules.COOL);
			PutricielFlameGeometry.row(s.p(), s.flame(), s.local(g.world(0.4D, 0.0D)), s.local(g.world(l - 0.6D, 0.0D)), 0.55D,
					1.6D * Math.max(0.2F, die), f -> PutricielBloomRules.flare(m.age() - (float) f * 2.0F), Vec3.ZERO, s.right(), s.up(),
					s.time(), seed + 4, Math.max(0.35F, die));
		}
	}

	/**
	 * The Full Purge: the floor round the body scorches and three ring furrows burn in through the wind-up, one after
	 * another; then three waves of fire roll out to eight blocks, each a ring of tongues kept under the jump's
	 * clearance with a glowing crest, the floor charring black behind it.
	 */
	private static void fullPurge(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Ring ring)) return;
		Burn burn = m.burn();
		double reach = ring.to();
		if (s.ground()) {
			PutricielGroundGeometry.sector(s.p(), s.floor(), m.ground(), 0.0D, 2.4D, -180.0D, 180.0D, Front.from(m.origin(), burn.grow() * 2.4D, 0.6D),
					scorch(burn, 1.0F), m.seed());
			for (int k = 0; k < 3 && m.age() < 0.0F; k++) {
				float shown = HemoEasing.clamp01((m.build() - k * 0.25F) / 0.5F);
				PutricielGroundGeometry.ring(s.p(), s.floor(), m.ground(), reach * (k + 1) / 3.0D, 0.18D, shown, 0.5F, furrow(burn, 0.8F),
						m.seed() + 1 + k);
			}
			for (int wave = 0; wave < Math.max(1, ring.waves()); wave++) {
				double radius = m.age() < 0.0F ? -1.0D : ring.radius(m.age(), wave);
				if (radius < 0.0D) continue;
				// the char behind the wave cooling, and a band of white-hot floor under its crest, so the wave reads as a
				// burning front from above as well as from the floor
				PutricielGroundGeometry.sector(s.p(), s.floor(), m.ground(), Math.max(0.0D, radius - 1.8D), Math.max(0.05D, radius - 0.5D), -180.0D,
						180.0D, Front.whole(m.origin()), new Paint(PutricielGroundGeometry.SCORCH, 0.92F, 0.3F, 1.0F), m.seed() + 10 + wave);
				PutricielGroundGeometry.sector(s.p(), s.floor(), m.ground(), Math.max(0.0D, radius - 0.6D), radius + 0.45D, -180.0D, 180.0D,
						Front.from(m.origin(), radius, 0.6D), new Paint(PutricielGroundGeometry.SCORCH, 0.97F, 0.95F, 1.0F), m.seed() + 13 + wave);
			}
		}
		if (!s.spore() || m.age() < 0.0F) return;
		Vec3 centre = s.local(m.origin());
		for (int wave = 0; wave < Math.max(1, ring.waves()); wave++) {
			double radius = ring.radius(m.age(), wave);
			if (radius < 0.0D) continue;
			float own = m.age() - wave * ring.waveGap();
			PutricielFlameGeometry.ring(s.p(), s.flame(), centre, Math.max(0.3D, radius), 0.3D, PURGE_TONGUE * PutricielBloomRules.flare(own),
					0.18D, s.right(), s.up(), s.time(), m.seed() + 20 + wave, 1.0F);
			// a lower ring burning on just behind the crest, so the wave has depth rather than one file of tongues
			PutricielFlameGeometry.ring(s.p(), s.flame(), centre, Math.max(0.2D, radius - 0.45D), 0.45D, PURGE_TONGUE * 0.6D,
					0.3D, s.right(), s.up(), s.time(), m.seed() + 25 + wave, 0.85F);
			PutricielFlameGeometry.sparks(s.p(), s.flame(), centre.add(0.0D, 0.3D, 0.0D), Vec3.ZERO, s.right(), s.up(), own, radius * 0.8D + 1.0D,
					10, m.seed() + 30 + wave, 0.8F);
		}
	}

	/** The Offering: a scorched ring round the kneeling body, its cracks breathing with the palm's fire. */
	private static void offering(Scene s, Move m) {
		if (!s.ground()) return;
		float open = HemoEasing.ramp(m.moveTime(), 0.0F, m.windup());
		float beat = (float) Math.pow(Math.max(0.0D, Math.sin(s.time() * 0.3D)), 3.0D);
		PutricielGroundGeometry.sector(s.p(), s.floor(), m.ground(), 0.0D, 1.9D, -180.0D, 180.0D, Front.from(m.origin(), 1.9D * open, 0.4D),
				new Paint(PutricielGroundGeometry.SCORCH, 0.45F + 0.3F * beat, 0.45F, open), m.seed());
	}

	/**
	 * The Embering: the scorched floor round the slumped body cools to ash while the rot creeps back in over it from
	 * the edge, and the tongues round the body gutter down to soot.
	 */
	private static void embering(Scene s, Move m) {
		float t = m.moveTime();
		if (s.ground()) {
			PutricielGroundGeometry.sector(s.p(), s.floor(), m.ground(), 0.0D, 3.0D, -180.0D, 180.0D, Front.whole(m.origin()),
					new Paint(PutricielGroundGeometry.SCORCH, 0.6F * (1.0F - HemoEasing.ramp(t, 0.0F, 30.0F)), 0.6F + 0.4F * HemoEasing.ramp(t, 0.0F, 40.0F),
							1.0F - HemoEasing.ramp(t, 60.0F, 74.0F)), m.seed());
			PutricielGroundGeometry.sector(s.p(), s.floor(), m.ground(), 0.0D, 3.4D, -180.0D, 180.0D,
					Front.inward(m.origin(), 3.4D, 3.4D * HemoEasing.ramp(t, 10.0F, 70.0F), 0.6D),
					new Paint(PutricielGroundGeometry.MAT, 0.3F, 0.3F, 1.0F - HemoEasing.ramp(t, 66.0F, 78.0F)), m.seed() + 1);
		}
		if (!s.spore()) return;
		Vec3 centre = s.local(m.origin());
		float gutter = 1.0F - HemoEasing.ramp(t, 0.0F, 16.0F);
		PutricielFlameGeometry.ring(s.p(), s.flame(), centre, 0.85D, 0.5D, 0.6D * gutter, 0.0D, s.right(), s.up(), s.time(), m.seed() + 2,
				gutter);
		float soot = HemoEasing.window(t, 4.0F, 12.0F, 30.0F, 50.0F);
		PutricielFlameGeometry.smoke(s.p(), s.flame(), centre.add(0.0D, 1.3D, 0.0D), s.right(), s.up(), 0.6D, s.time(), m.seed() + 3, 0.6F * soot);
		PutricielFlameGeometry.smoke(s.p(), s.flame(), centre.add(0.3D, 1.8D, -0.2D), s.right(), s.up(), 0.5D, s.time() + 13.0D, m.seed() + 4,
				0.5F * soot);
	}

	// ── the ending ───────────────────────────────────────────────────

	/**
	 * The ending (design section 11): the rot round the kneeling body darkens as it stills; through the last cleansing
	 * the floor scorches out from the body while a column of fire climbs it with a ring of tongues round the knees and
	 * sparks rising; then the fire dies to ash, and spores lift slowly off the body as it rests.
	 */
	private static void ending(Scene s, PutricielEntity boss) {
		float t = boss.view().endingTime(s.partial());
		if (t < 0.0F) return;
		Vec3 at = boss.getPosition(s.partial());
		Vec3 o = new Vec3(at.x, boss.getY(), at.z);
		int seed = boss.getId() * 53;
		Ground g = new Ground(o.x, o.z, 1.0D, 0.0D, o.y, s.camera());
		float burn = PutricielEndingRules.ember(t);
		if (s.ground()) {
			PutricielGroundGeometry.sector(s.p(), s.floor(), g, 0.0D, 2.6D, -180.0D, 180.0D, Front.from(o, 2.6D * PutricielEndingRules.kneel(t), 0.6D),
					new Paint(PutricielGroundGeometry.MAT, 0.4F, 0.1F, 1.0F - HemoEasing.ramp(t, PutricielEndingRules.BURN_START,
							PutricielEndingRules.BURN_START + 10.0F)), seed);
			PutricielGroundGeometry.sector(s.p(), s.floor(), g, 0.0D, 3.6D, -180.0D, 180.0D,
					Front.from(o, 3.6D * HemoEasing.ramp(t, PutricielEndingRules.BURN_START, PutricielEndingRules.BURN_START + 18.0F), 0.6D),
					new Paint(PutricielGroundGeometry.SCORCH, burn, HemoEasing.ramp(t, 40.0F, 80.0F),
							1.0F - HemoEasing.ramp(t, 88.0F, PutricielEndingRules.END_TRIAL)), seed + 1);
			return;
		}
		if (!s.spore()) return;
		Vec3 centre = s.local(o);
		if (burn > 0.01F) {
			PutricielFlameGeometry.column(s.p(), s.flame(), centre, 0.55D, 2.7D * burn, s.right(), s.up(), s.time(), seed + 2, burn);
			PutricielFlameGeometry.ring(s.p(), s.flame(), centre, 1.3D, 0.5D, 0.75D * burn, 0.05D, s.right(), s.up(), s.time(), seed + 3, burn);
			PutricielFlameGeometry.sparks(s.p(), s.flame(), centre.add(0.0D, 1.0D, 0.0D), Vec3.ZERO, s.right(), s.up(), t % 10.0F, 2.0D, 8,
					seed + 4 + (int) (t / 10.0F), burn);
		}
		float rest = HemoEasing.window(t, PutricielEndingRules.BURN_END, PutricielEndingRules.SOOT_END, 92.0F, 110.0F);
		if (rest <= 0.0F) return;
		for (int k = 0; k < 10; k++) {
			double phase = ((t - PutricielEndingRules.BURN_END) * 0.018D + k / 10.0D) % 1.0D;
			double a = k * 2.39996D + seed;
			Vec3 puff = centre.add(Math.cos(a) * (0.4D + 0.4D * phase), 0.4D + phase * 2.6D, Math.sin(a) * (0.4D + 0.4D * phase));
			PutricielSporeGeometry.puff(s.p(), s.spores(), puff, s.right(), s.up(), 0.2D + 0.3D * phase, (float) phase * 0.8F, 0.1F, 0.7F,
					rest * 0.6F * (float) Math.sin(phase * Math.PI), seed + 10 + k);
			if (k % 2 == 0) {
				PutricielSporeGeometry.mote(s.p(), s.spores(), puff.add(0.1D, 0.2D, 0.0D), s.right(), s.up(), 0.04D, 0.9F,
						rest * 0.4F * (float) Math.sin(phase * Math.PI), seed + 30 + k);
			}
		}
	}

	// ── hazards ──────────────────────────────────────────────────────

	private static void hazard(Scene s, BossGroundHazardEntity hazard) {
		PutricielHazard kind = PutricielHazard.byId(hazard.kind());
		if (kind == null) return;
		float age = AnimationTime.age(hazard, s.partial());
		float fade = SaintTelegraphTiming.hazardFade(age, hazard.life(), hazard.fade(), hazard.fading(), hazard.fadeStart());
		if (fade <= 0.0F) return;
		PutricielEntity boss = hazard.owner() instanceof PutricielEntity owner ? owner : null;
		boolean burning = !kind.fire() && hazard.fading() && boss != null && boss.stage().fire();
		switch (kind) {
			case ROT_COLONY -> colony(s, hazard, age, fade, burning);
			case ROT_CLOUD -> cloud(s, hazard, age, fade);
			case PURGING_FIRE -> purgingFire(s, hazard, age, fade);
			case FIRE_WALL -> fireWall(s, hazard, age, fade);
			case ROT_SLUDGE, SLUDGE_FIRE -> sludge(s, hazard, kind, age, boss);
			case FIRE_RUNNER -> runner(s, hazard, age, fade);
		}
	}

	/**
	 * Rot sludge (revision 2026-11-06, R2): a thick, wet, glossy pool standing on the floor, its dark stain round it,
	 * bubbling in the rot and heaving with each of the Bloom's pulses; settling into the moss under the cap; and, as a
	 * {@code SLUDGE_FIRE} on the same footprint, burning from where the fire caught it: the front, the boil, the crust,
	 * the crumble onto the ash and the scorch cooling on it ({@link PutricielSludgeGeometry}).
	 */
	private static void sludge(Scene s, BossGroundHazardEntity hazard, PutricielHazard kind, float age, PutricielEntity boss) {
		PutricielSludgeGeometry.Pool pool = PutricielSludgeGeometry.Pool.of(hazard);
		PutricielSludgeGeometry.State state = PutricielSludgeGeometry.state(hazard, kind, age, pool, boss);
		if (state == null) return;
		int seed = pool.seed();
		if (s.ground()) {
			PutricielSludgeGeometry.ground(s.p(), s.floor(), pool, state, s.camera(), seed);
			return;
		}
		if (s.solid()) {
			PutricielSludgeGeometry.solid(s.p(), s.growth(), pool, state, s.camera(), s.time(), bloomHeave(s, boss));
			return;
		}
		if (s.spore()) PutricielSludgeGeometry.air(s.p(), s.spores(), s.flame(), pool, state, s.camera(), s.right(), s.up(), s.time(), 36);
	}

	/** The Bloom of Rot's pulses heave every pool's surface: a swell and a ring of bubbles on each. */
	private static float bloomHeave(Scene s, PutricielEntity boss) {
		if (boss == null || boss.view().moveId() != PutricielMove.BLOOM_OF_ROT.id()) return 0.0F;
		float t = boss.view().moveTime(s.partial()) - PutricielMove.BLOOM_OF_ROT.timeline(boss.view().inputs()).commitTick();
		float heave = 0.0F;
		for (int k = 0; k < 3; k++) heave = Math.max(heave, HemoEasing.pulse(t - k * PutricielMove.BLOOM_PULSE, 2.0F, 12.0F));
		return heave;
	}

	/** A fire runner: a burning fuse from a relit pyre or a burning pool to the pool it lights (frame origin to aim). */
	private static void runner(Scene s, BossGroundHazardEntity hazard, float age, float fade) {
		FootprintFrame frame = hazard.frame();
		Vec3 from = new Vec3(frame.origin().x, frame.floorY(), frame.origin().z);
		Vec3 to = new Vec3(frame.aim().x, frame.floorY(), frame.aim().z);
		PutricielSludgeGeometry.runner(s.p(), s, from, to, age, fade, hazard.getId() * 41);
	}

	/**
	 * A rot colony where a shelf landed: a breathing patch of colonised floor with a big rosette, two smaller ones and
	 * two sacs, puffing spores on every pulse, darkening as it ages and withering at its end. The fire burns it: the
	 * growths char black with embers in their cracks and crumble, the patch scorches, tongues lick over it.
	 */
	private static void colony(Scene s, BossGroundHazardEntity hazard, float age, float fade, boolean burning) {
		FootprintFrame frame = hazard.frame();
		Vec3 c = new Vec3(frame.origin().x, frame.floorY(), frame.origin().z);
		double r = PutricielHazard.COLONY_RADIUS;
		int seed = hazard.getId() * 17;
		int pulse = PutricielHazard.ROT_COLONY.pulse();
		float puffAge = age % pulse;
		float breath = HemoEasing.pulse(puffAge, 2.0F, 10.0F);
		float charred = burning ? HemoEasing.clamp01((1.0F - fade) * 1.6F) : 0.0F;
		float life = hazard.life();
		Ground g = new Ground(c.x, c.z, 1.0D, 0.0D, c.y, s.camera());
		if (s.ground()) {
			PutricielGroundGeometry.sector(s.p(), s.floor(), g, 0.0D, r * 1.15D, -180.0D, 180.0D,
					Front.from(c, r * 1.15D * HemoEasing.easeOutCubic(HemoEasing.clamp01(age / 8.0F)), 0.5D),
					new Paint(PutricielGroundGeometry.MAT, 0.6F + 0.2F * breath, 0.45F + 0.55F * breath, burning ? 1.0F : fade), seed);
			if (burning) {
				PutricielGroundGeometry.sector(s.p(), s.floor(), g, 0.0D, r * 1.3D, -180.0D, 180.0D, Front.from(c, r * 1.3D * charred * 1.2D, 0.4D),
						new Paint(PutricielGroundGeometry.SCORCH, 0.85F * fade, 0.5F, 1.0F), seed + 1);
			}
			return;
		}
		float witherAt = life - PutricielBloomRules.WITHER - 8.0F;
		int state = PutricielGrowthGeometry.withChar(PutricielGrowthGeometry.withBloom(PutricielGrowthGeometry.FRESH, 0.15F + 0.6F * breath), charred);
		// a pool laid over the patch absorbs it (revision 2026-11-06, R2.6): its rosettes and sacs sink into the sludge
		// over the absorption's fade instead of withering
		boolean absorbed = !burning && hazard.fading() && hazard.fade() == PutricielSludgeRules.ABSORB;
		double sunk = absorbed ? 0.55D * HemoEasing.easeInCubic(1.0F - fade) : 0.0D;
		Vec3 sinking = c.add(0.0D, -sunk, 0.0D);
		Vec3[] feet = {sinking, sinking.add(0.85D, 0.0D, 0.3D), sinking.add(-0.5D, 0.0D, -0.75D)};
		if (s.solid()) {
			for (int i = 0; i < feet.length; i++) {
				List<Cap> caps = PutricielBloomRules.rosette(seed + i * 5, i == 0 ? 0.85D : 0.45D, i == 0 ? 6 : 3);
				float own = age - i * 1.5F;
				float form = burning ? Math.min(PutricielBloomRules.formation(own, witherAt), 0.3F + 0.7F * fade) : PutricielBloomRules.formation(own, witherAt);
				PutricielGrowthGeometry.knot(s.p(), s.growth(), s.local(feet[i]), 0.2D, 0.14D, form, PutricielGrowthGeometry.withChar(PutricielGrowthGeometry.BARK, charred),
						seed + i);
				for (int k = 0; k < caps.size(); k++) {
					Cap cap = caps.get(k);
					float capAge = own - cap.delay();
					PutricielGrowthGeometry.cap(s.p(), s.growth(), s.local(feet[i]), cap, PutricielBloomRules.size(capAge, witherAt),
							Math.max(PutricielBloomRules.withered(capAge, witherAt), charred * 0.6F), PutricielBloomRules.quiver(capAge, s.time(), seed + k),
							state, burning ? Math.min(PutricielBloomRules.formation(capAge, witherAt), 0.3F + 0.7F * fade)
									: PutricielBloomRules.formation(capAge, witherAt), seed + i * 31 + k);
				}
			}
			for (int i = 0; i < 2; i++) {
				double a = i * 3.1D + 1.2D;
				PutricielGrowthGeometry.sac(s.p(), s.growth(), s.local(sinking.add(Math.cos(a) * 0.75D, 0.0D, Math.sin(a) * 0.75D)), 0.2D, 0.32D,
						PutricielBloomRules.swell(age - 3.0F - i * 2.0F, s.time(), seed + i) * (1.0F + 0.08F * breath),
						burning ? fade : 1.0F - HemoEasing.ramp(age, witherAt, witherAt + PutricielBloomRules.WITHER),
						PutricielGrowthGeometry.withChar(PutricielGrowthGeometry.SAC_SKIN, charred), seed + 40 + i);
			}
			return;
		}
		if (!s.spore()) return;
		if (!burning) {
			PutricielSporeGeometry.burst(s.p(), s.spores(), s.local(c).add(0.0D, 0.55D, 0.0D), s.right(), s.up(), puffAge, 0.9D, 4, 0.2F,
					fade * 0.9F, seed + 50 + (int) (age / pulse));
			return;
		}
		float flame = HemoEasing.window(1.0F - fade, 0.0F, 0.2F, 0.7F, 1.0F);
		PutricielFlameGeometry.ring(s.p(), s.flame(), s.local(c), 0.7D, 0.45D, 0.65D * flame, 0.05D, s.right(), s.up(), s.time(), seed + 60, flame);
		PutricielFlameGeometry.tongue(s.p(), s.flame(), s.local(c), Vec3.ZERO, 0.9D * flame, 0.12D, s.time(), seed + 61, flame);
		PutricielFlameGeometry.smoke(s.p(), s.flame(), s.local(c).add(0.0D, 1.2D, 0.0D), s.right(), s.up(), 0.5D, s.time(), seed + 62, 0.6F * flame);
	}

	/** Rot Breath's lingering cloud: billows of spores low over the cone, turning slowly and thinning out. */
	private static void cloud(Scene s, BossGroundHazardEntity hazard, float age, float fade) {
		FootprintFrame frame = hazard.frame();
		BossFootprint.Cone cone = hazard.shape() instanceof BossFootprint.Cone shape ? shape : (BossFootprint.Cone) PutricielMove.breathCone();
		Vec3 o = new Vec3(frame.origin().x, frame.floorY(), frame.origin().z);
		int seed = hazard.getId() * 19;
		float life = hazard.life();
		float thin = 0.1F + 0.75F * HemoEasing.ramp(age, life * 0.4F, life + 6.0F);
		if (s.ground()) {
			Ground g = Ground.of(frame.origin(), Shapes.forward(frame), frame.floorY(), s.camera());
			PutricielGroundGeometry.sector(s.p(), s.floor(), g, 0.3D, cone.radius(), -cone.degrees() / 2.0D, cone.degrees() / 2.0D, Front.whole(o),
					new Paint(PutricielGroundGeometry.MAT, 0.3F, 0.9F, 0.6F * fade), seed);
			return;
		}
		if (!s.spore()) return;
		PutricielSporeGeometry.coneCloud(s.p(), s.spores(), s.local(o), Shapes.forward(frame), cone.radius(), cone.degrees(), s.right(), s.up(),
				s.time(), 18, thin, fade * HemoEasing.smoothstep(HemoEasing.clamp01(age / 4.0F)), seed);
	}

	/** The purging fire a Censer Cast leaves: a scorched disc of coals with low tongues across it and soot rising. */
	private static void purgingFire(Scene s, BossGroundHazardEntity hazard, float age, float fade) {
		FootprintFrame frame = hazard.frame();
		Vec3 c = new Vec3(frame.origin().x, frame.floorY(), frame.origin().z);
		double r = PutricielHazard.FIRE_RADIUS;
		int seed = hazard.getId() * 23;
		float heat = 0.72F + 0.12F * (float) Math.sin(s.time() * 0.3D + seed);
		if (s.ground()) {
			Ground g = new Ground(c.x, c.z, 1.0D, 0.0D, c.y, s.camera());
			PutricielGroundGeometry.sector(s.p(), s.floor(), g, 0.0D, r * 1.1D, -180.0D, 180.0D,
					Front.from(c, r * 1.1D * HemoEasing.easeOutCubic(HemoEasing.clamp01(age / 4.0F)), 0.4D),
					new Paint(PutricielGroundGeometry.SCORCH, heat * fade, 0.3F + 0.5F * age / hazard.life(), 1.0F), seed);
			return;
		}
		if (!s.spore()) return;
		Vec3 local = s.local(c);
		float rise = HemoEasing.smoothstep(HemoEasing.clamp01(age / 4.0F)) * fade;
		PutricielFlameGeometry.ring(s.p(), s.flame(), local, r * 0.8D, 0.5D, 0.6D * rise, 0.05D, s.right(), s.up(), s.time(), seed + 1, rise);
		PutricielFlameGeometry.ring(s.p(), s.flame(), local, r * 0.4D, 0.5D, 0.7D * rise, 0.0D, s.right(), s.up(), s.time(), seed + 2, rise);
		PutricielFlameGeometry.tongue(s.p(), s.flame(), local, Vec3.ZERO, 0.85D * rise, 0.12D, s.time(), seed + 3, rise);
		PutricielFlameGeometry.bed(s.p(), s.flame(), local, r, seed + 4, 0.45F * rise);
	}

	/** The fire wall at a Purging Sweep's end: a burning furrow with a row of tall tongues standing along it. */
	private static void fireWall(Scene s, BossGroundHazardEntity hazard, float age, float fade) {
		FootprintFrame frame = hazard.frame();
		Vec3 from = new Vec3(frame.origin().x, frame.floorY(), frame.origin().z);
		Vec3 to = new Vec3(frame.aim().x, frame.floorY(), frame.aim().z);
		int seed = hazard.getId() * 29;
		double length = Math.max(0.1D, Shapes.planar(from, to));
		Ground g = Ground.of(frame.origin(), Shapes.forward(frame), frame.floorY(), s.camera());
		if (s.ground()) {
			PutricielGroundGeometry.rect(s.p(), s.floor(), g, 0.0D, length, -0.7D, 0.7D, Front.whole(from),
					new Paint(PutricielGroundGeometry.SCORCH, 0.8F * fade, 0.4F, 1.0F), seed);
			PutricielGroundGeometry.strip(s.p(), s.floor(), g, List.of(from, to), PutricielHazard.WALL_HALF_WIDTH, 1.0D, 1.0F,
					new Paint(PutricielGroundGeometry.FURROW, 0.85F * fade, 0.3F, 1.0F), seed + 1);
			return;
		}
		if (!s.spore()) return;
		PutricielFlameGeometry.row(s.p(), s.flame(), s.local(from), s.local(to), 0.36D, 2.0D * PutricielBloomRules.flare(age) * fade,
				PutricielFlameGeometry.Profile.FLAT, Vec3.ZERO, s.right(), s.up(), s.time(), seed + 2, Math.max(0.3F, fade));
	}

	// ── pieces ───────────────────────────────────────────────────────

	/**
	 * Fire trailing a limb's real path between two ages (ticks from the commit): tongues leaning back along it, sparks
	 * off its head. {@code limb} is a {@link #trail} limb.
	 */
	private static void limbFire(Scene s, Move m, int limb, float from, float to, float alpha) {
		List<Vec3> path = trail(s, m, limb, from, to, 10);
		for (int i = 1; i < path.size(); i++) {
			Vec3 at = path.get(i), back = path.get(i - 1);
			double f = i / (double) (path.size() - 1);
			PutricielFlameGeometry.tongue(s.p(), s.flame(), at, back.subtract(at).scale(0.8D), 0.2D + 0.3D * f, 0.08D, s.time(),
					m.seed() + 300 + limb * 17 + i, alpha * (float) (0.4D + 0.6D * f));
		}
		if (!path.isEmpty()) {
			PutricielFlameGeometry.sparks(s.p(), s.flame(), path.get(path.size() - 1), Vec3.ZERO, s.right(), s.up(),
					(float) ((s.time() * 0.7D) % 8.0D), 0.8D, 4, m.seed() + 320 + limb + (int) (s.time() / 11.0D), alpha);
		}
	}

	/** The limbs whose real paths the renderer keeps: the two censers, the offering palm and the staff head. */
	static final int CENSER_LEFT = 0, CENSER_RIGHT = 1, PALM = 2, STAFF_HEAD = 3;

	/**
	 * A limb's real path between two ages (ticks from the commit), camera-relative, oldest first: read from the trails
	 * the renderer records off the posed model each frame, keyed by the body's render age, so the effect follows the
	 * animation's own path. Empty outside the kept frames.
	 */
	private static List<Vec3> trail(Scene s, Move m, int limb, float from, float to, int samples) {
		List<Vec3> out = new ArrayList<>(samples + 1);
		if (to <= from) return out;
		PutricielAnimState motion = PutricielAnimState.of(m.boss());
		float commit = m.bodyAge() - m.age();
		for (int i = 0; i <= samples; i++) {
			float age = commit + from + (to - from) * i / samples;
			Vec3 at = switch (limb) {
				case PALM -> motion.palmAt(age);
				case STAFF_HEAD -> motion.staffHeadAt(age);
				default -> motion.censerAt(limb, age);
			};
			if (at != null) out.add(s.local(at));
		}
		return out;
	}

	/** The rot's ground paint from a look: its wet and spore scaled, its strength times {@code strength}. */
	private static Paint rot(Look look, float wet, float strength) {
		return new Paint(PutricielGroundGeometry.MAT, look.wet() * wet, look.spore(), look.alpha() * strength);
	}

	private static Paint trench(Look look, float strength) {
		return new Paint(PutricielGroundGeometry.TRENCH, look.wet(), look.spore(), look.alpha() * strength);
	}

	private static Paint scorch(Burn burn, float strength) {
		return new Paint(PutricielGroundGeometry.SCORCH, burn.heat(), burn.ash(), burn.alpha() * strength);
	}

	private static Paint furrow(Burn burn, float strength) {
		return new Paint(PutricielGroundGeometry.FURROW, burn.heat(), burn.ash(), burn.alpha() * strength);
	}

	/** World points along an arc of {@code radius} round the ground's origin, bearings about the aim, a little ragged. */
	private static List<Vec3> arc(Ground g, double radius, double fromDeg, double toDeg, int seed) {
		int segments = (int) Math.max(6, Math.ceil(Math.toRadians(toDeg - fromDeg) * radius / 0.6D));
		List<Vec3> out = new ArrayList<>(segments + 1);
		for (int i = 0; i <= segments; i++) {
			double a = Math.toRadians(fromDeg + (toDeg - fromDeg) * i / segments);
			double r = radius + (PutricielBloomRules.unit(seed, 30 + i) - 0.5D) * 0.14D;
			out.add(g.world(Math.cos(a) * r, Math.sin(a) * r));
		}
		return out;
	}

	/** World points of a ragged line along the ground's aim from {@code a0} to {@code a1}, wandering {@code amp} to the sides. */
	private static List<Vec3> jag(Ground g, double a0, double a1, double right, double amp, int seed) {
		int segments = (int) Math.max(2, Math.ceil((a1 - a0) / 0.6D));
		List<Vec3> out = new ArrayList<>(segments + 1);
		double drift = 0.0D;
		for (int i = 0; i <= segments; i++) {
			double t = i / (double) segments;
			drift = drift * 0.5D + (PutricielBloomRules.unit(seed, 60 + i) - 0.5D);
			double off = i == 0 ? 0.0D : drift * amp * 2.0D * Math.sin(Math.PI * Math.min(1.0D, t * 1.6D));
			out.add(g.world(HemoEasing.lerp(a0, a1, t), right + off));
		}
		return out;
	}

	/** Two bracket caps stacked on a lip, both facing {@code yaw} (radians), {@code scale} across. */
	private static List<Cap> frill(int seed, double yaw, double scale) {
		double u = PutricielBloomRules.unit(seed, 1), w = PutricielBloomRules.unit(seed, 2);
		return List.of(new Cap(0.0D, 0.0D, 0.02D, scale * (0.55D + 0.15D * u), yaw + (u - 0.5D) * 0.5D, 0.32D, 2.0D + 0.4D * w,
						scale * 0.14D, 0, 0.0F),
				new Cap(0.0D, 0.0D, 0.02D + scale * 0.3D, scale * (0.4D + 0.1D * w), yaw + (w - 0.5D) * 0.6D, 0.42D, 1.8D, scale * 0.12D, 1,
						1.0F));
	}
}
