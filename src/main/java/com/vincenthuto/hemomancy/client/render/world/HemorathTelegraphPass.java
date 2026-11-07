package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.model.entity.boss.hemorath.HemorathAnimState;
import com.vincenthuto.hemomancy.client.render.BloodPoolSurface;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.client.render.world.HemorathFissureGeometry.Ground;
import com.vincenthuto.hemomancy.client.render.world.HemorathFissureRules.Crack;
import com.vincenthuto.hemomancy.client.render.world.HemorathFissureRules.Look;
import com.vincenthuto.hemomancy.client.render.world.HemorathFissureRules.Slab;
import com.vincenthuto.hemomancy.client.render.world.HemorathFissureRules.Spike;
import com.vincenthuto.hemomancy.client.render.world.HemorathFissureRules.Trench;
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
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathHaloPath;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathPresenceRules;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleFunction;

/**
 * Saint Hemorath's own telegraph and hazard pass (visual pass 2, 2026-11-06), run from {@link HemorathReliquaryGeometry}
 * in both of the manipulation visuals' passes. His {@link HemorathTelegraphStyle} declines nothing and draws nothing,
 * so the kit's flat tints never show for him; this pass reads the same synced move, footprint, frame, timeline and
 * reveal the kit does, and draws each footprint as the floor itself answering him: bone and venous stone cracking
 * along it in his fissure material (the Iron Rake's lanes and the Rib Burst's spokes sinking into trenches, pass 2b),
 * blood welling up into the cracks toward the commit, a flash at the commit, iron
 * punching out of the floor behind a rake's front or round a crown, then the cracks draining and sealing over
 * {@link HemorathFissureRules#LINGER} ticks into the recovery. The Thicket's standing iron and the tide's flood are his
 * hazards, drawn here too; struck bodies get {@link HemorathImpactGeometry}. Every drawn edge is the hit test's edge:
 * shapes come from the footprint the server tests.
 */
final class HemorathTelegraphPass {
	private static final double RANGE = 64.0D;
	/** The Iron Rake's iron punches out of its trenches every this many blocks along each lane. */
	private static final double RAKE_SITE = 1.5D;
	/** The Iron Rake's spikes stand this long after the active span before they sink. */
	private static final float RAKE_HOLD = 6.0F;
	/** Crown Fall's ring of clusters. */
	private static final int CROWN_CLUSTERS = 14;
	/** The Thicket's thrown iron leaves his hands this many ticks before the commit. */
	private static final float THROW_TICKS = 12.0F;
	/** The tide rises over this many ticks, and drains over its last DRAIN. */
	private static final float TIDE_RISE = 8.0F, TIDE_DRAIN = 20.0F;
	private static final double TIDE_DEPTH = 0.5D;
	/** The Thicket's spikes rust dark over this long before they sink. */
	private static final float THICKET_RUST = 60.0F, THICKET_SINK = 16.0F;

	/** The pass's frame: where it draws, and in what. Points are camera-relative. */
	record Scene(PoseStack p, VertexConsumer fissure, VertexConsumer blood, VertexConsumer iron, Vec3 camera, Vec3 right,
			Vec3 up, double time, float partial, boolean solids, ClientLevel level) {
		Vec3 local(Vec3 world) {
			return world.subtract(camera);
		}

		Vec3 local(double x, double y, double z) {
			return new Vec3(x - camera.x, y - camera.y, z - camera.z);
		}

		void cracks(Ground g, List<Crack> cracks, Look look, float strength, int seed) {
			if (!solids) HemorathFissureGeometry.cracks(p, fissure, g, cracks, look, strength, seed);
		}
	}

	/** One running move as the pass draws it. */
	private record Move(HemorathEntity boss, HemorathMove move, BossFootprint footprint, FootprintFrame frame,
			SaintMoveInputs inputs, float moveTime, float age, int windup, int active, int recovery, int reveal, Look look,
			Ground ground, int seed) {
		/** 0 at the reveal to 1 at the commit. */
		float build() {
			return windup <= reveal ? 1.0F : HemoEasing.clamp01((moveTime - reveal) / (float) (windup - reveal));
		}
	}

	private record Pool(double x, double y, double z, double half, int jet, int heat, int alpha, int seed) {
	}

	private static final List<Pool> POOLS = new ArrayList<>();

	private HemorathTelegraphPass() {
	}

	static void collect(ClientLevel level, PoseStack p, LuxUmbraBatch flows, VertexConsumer iron, Vec3 camera, Vec3 right,
			Vec3 up, double time, float partial, boolean solids) {
		AABB box = new AABB(camera, camera).inflate(RANGE);
		List<HemorathEntity> bosses = new ArrayList<>();
		for (HemorathEntity boss : level.getEntitiesOfClass(HemorathEntity.class, box)) {
			if (boss.isAlive() && !boss.isInvisible() && boss.kit() != null) bosses.add(boss);
		}
		List<BossGroundHazardEntity> hazards = new ArrayList<>();
		for (BossGroundHazardEntity hazard : level.getEntitiesOfClass(BossGroundHazardEntity.class, box)) {
			if (!hazard.isRemoved() && hazard.owner() instanceof HemorathEntity) hazards.add(hazard);
		}
		if (bosses.isEmpty() && hazards.isEmpty()) return;
		MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
		VertexConsumer fissure = FerricDuctilisGeometry.DISCARD;
		if (!solids) {
			HemorathRenderTypes.begin(time);
			HemorathFissureGeometry.begin();
			fissure = buffers.getBuffer(HemorathRenderTypes.FISSURE);
		}
		Scene s = new Scene(p, fissure, solids ? FerricDuctilisGeometry.DISCARD : flows.vertices(LuxUmbraBatch.Material.ANIMUS),
				solids ? iron : FerricDuctilisGeometry.DISCARD, camera, right, up, time, partial, solids, level);
		POOLS.clear();
		for (HemorathEntity boss : bosses) {
			if (risingOrEnding(boss, partial)) continue;
			move(s, boss);
			if (boss.thirsting()) thirstRing(s, boss);
			HemorathImpactGeometry.collect(s, boss);
		}
		for (BossGroundHazardEntity hazard : hazards) hazard(s, hazard);
		if (solids) return;
		buffers.endBatch(HemorathRenderTypes.FISSURE);
		pools(buffers, p.last().pose(), camera, time);
	}

	// ── moves ────────────────────────────────────────────────────────

	private static void move(Scene s, HemorathEntity boss) {
		SaintMoveView view = boss.view();
		HemorathMove move = HemorathMove.byId(view.moveId());
		if (move == null) return;
		float moveTime = view.moveTime(s.partial());
		if (moveTime < 0.0F) return;
		SaintMoveInputs inputs = view.inputs();
		BossMoveTimeline timeline = SaintControllerRules.effective(move.kind(), move.timeline(inputs));
		TelegraphSpec spec = move.telegraph(inputs);
		int reveal = SaintControllerRules.revealTick(spec == null ? TelegraphSpec.DEFAULT : spec, timeline, move.kind());
		int windup = timeline.commitTick(), active = timeline.active();
		if (moveTime > windup + active + timeline.recovery()) return;
		FootprintFrame frame = view.frame();
		if (frame == null) {
			frame = FootprintFrame.simple(boss.syncedMoveOrigin(), boss.syncedMoveAim(), boss.syncedMoveAim(), boss.getY());
		}
		BossFootprint footprint = move.footprint(inputs);
		Look look = HemorathFissureRules.look(moveTime, reveal, windup, active);
		Ground ground = Ground.of(frame.origin(), Shapes.forward(frame), frame.floorY(), s.camera());
		Move m = new Move(boss, move, footprint, frame, inputs, moveTime, moveTime - windup, windup, active,
				timeline.recovery(), reveal, look, ground, boss.getId() * 31 + move.id() * 7 + boss.syncedMoveSerial() * 131);
		switch (move) {
			case RAKING_CLAW, BACKHAND -> claw(s, m);
			case STAFF_HEEL -> heel(s, m);
			case STAFF_FALL -> staffFall(s, m);
			case IRON_RAKE -> rake(s, m);
			case EMPTY_PULSE -> pulse(s, m);
			case MARROW_RUPTURE -> rupture(s, m);
			case MARROW_SIPHON -> siphon(s, m);
			case CHAINED_DEBT -> debt(s, m);
			case TITHE_RECKONING -> tithe(s, m);
			case SPIKED_HALO -> halo(s, m);
			case CROWN_FALL -> crown(s, m);
			case IRON_THICKET -> thicket(s, m);
			case EXSANGUINATING_TIDE -> tide(s, m);
			case RIB_BURST -> ribs(s, m);
			case GORGE -> gorge(s, m);
			case WELLS_ANSWER, LAST_THIRST -> {
			}
		}
	}

	/**
	 * Raking Claw and Backhand: the cone's floor crazes and cracks; three claw gouges are scored across it from tick 2
	 * and rip open in the swing's direction at the commit, throwing chips and blood along the swing.
	 */
	private static void claw(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Cone cone)) return;
		double from = cone.offsetDegrees() - cone.degrees() / 2.0D, to = from + cone.degrees();
		boolean back = m.move() == HemorathMove.BACKHAND;
		List<Crack> gouges = HemorathFissureRules.gouges(cone.radius(), from, to, m.seed());
		if (!back) gouges = reversed(gouges);
		Look look = m.look();
		if (!s.solids()) {
			HemorathFissureGeometry.craze(s.p(), s.fissure(), m.ground(), 0.4D, cone.radius(), from, to, look, 0.4F, m.seed());
			s.cracks(m.ground(), HemorathFissureRules.cone(cone.inner(), cone.radius(), from, to, m.seed()), look, 0.65F, m.seed());
			Look scored = new Look(HemoEasing.clamp01((m.moveTime() - 2.0F) / 6.0F), 0.15F, 0.0F, look.rust(), 0.42F,
					look.alpha() * 0.85F);
			s.cracks(m.ground(), gouges, scored, 1.0F, m.seed() + 50);
			if (m.age() >= 0.0F) {
				Look rip = new Look(HemoEasing.clamp01(m.age() / 3.0F), look.heat(), look.blood(), 1.0F, look.width() * 1.15F,
						look.alpha());
				s.cracks(m.ground(), gouges, rip, 1.0F, m.seed() + 60);
			}
		}
		if (!s.solids()) slashes(s, m, cone.radius());
		if (m.age() < 0.0F || m.age() > HemorathFissureRules.CHIP_LIFE) return;
		for (int i = 0; i < gouges.size(); i++) {
			Crack gouge = gouges.get(i);
			int mid = gouge.size() / 2;
			Vec3 at = world(m.ground(), gouge.x()[mid], gouge.y()[mid], m.frame().floorY());
			Vec3 next = world(m.ground(), gouge.x()[Math.min(gouge.size() - 1, mid + 1)], gouge.y()[Math.min(gouge.size() - 1, mid + 1)],
					m.frame().floorY());
			float own = m.age() - i * 0.7F;
			if (s.solids()) HemorathIronGeometry.chips(s.p(), s.iron(), s.local(at), own, 1.4D, 4, m.seed() + 70 + i);
			else HemorathBloodGeometry.spatter(s.p(), s.blood(), s.local(at).add(0.0D, 0.1D, 0.0D), next.subtract(at), Vec3.ZERO,
					s.right(), s.up(), own, m.seed() + 80 + i, 3.0D, 0.9F);
		}
	}

	/** Staff Heel: a rust-crack wedge behind him that splits at the commit with a scuff of chips and rust dust at his heel. */
	private static void heel(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Cone cone)) return;
		double from = cone.offsetDegrees() - cone.degrees() / 2.0D, to = from + cone.degrees();
		Look look = m.look();
		Look rusty = new Look(look.grow(), look.heat() * 0.8F, look.blood() * 0.6F, Math.min(1.0F, look.rust() * 1.4F),
				look.width(), look.alpha());
		if (!s.solids()) {
			HemorathFissureGeometry.craze(s.p(), s.fissure(), m.ground(), 0.4D, cone.radius(), from, to, rusty, 0.5F, m.seed());
			s.cracks(m.ground(), HemorathFissureRules.cone(cone.inner(), cone.radius(), from, to, m.seed()), rusty, 0.9F, m.seed());
		}
		Vec3 heel = world(m.ground(), -1.0D, 0.0D, m.frame().floorY());
		if (s.solids()) HemorathIronGeometry.chips(s.p(), s.iron(), s.local(heel), m.age(), 1.6D, 7, m.seed() + 5);
		else {
			HemorathIronGeometry.dust(s.p(), s.blood(), s.local(heel), s.right(), s.up(), m.age(), 0.9D, m.seed() + 6);
			shock(s, heel, m.age(), 1.8D, 0.45D, m.seed() + 7);
		}
	}

	/**
	 * Staff Fall: a narrow fault runs out along the line from his feet with two starbursts at the chain marks; at the
	 * commit the line splits, iron splinters jut along it and chips and blood fly, and the chain marks burst four
	 * ticks later.
	 */
	private static void staffFall(Scene s, Move m) {
		double length = HemorathMove.FALL_LENGTH, half = HemorathMove.FALL_HALF_WIDTH;
		Look look = m.look();
		if (!s.solids()) {
			HemorathFissureGeometry.crazeRect(s.p(), s.fissure(), m.ground(), 0.3D, length, -half, half, look, 0.5F, m.seed());
			s.cracks(m.ground(), HemorathFissureRules.lane(length, half, m.seed()), look, 1.0F, m.seed());
		}
		Look marks = HemorathFissureRules.look(m.moveTime(), m.reveal(), m.windup() + HemorathMove.CHAIN_DELAY,
				Math.max(1, m.active() - HemorathMove.CHAIN_DELAY));
		List<Vec3> points = m.frame().points();
		for (int i = 0; i < points.size(); i++) {
			Vec3 point = points.get(i);
			Ground g = m.ground().at(point);
			s.cracks(g, HemorathFissureRules.star(HemorathMove.CHAIN_RADIUS, m.seed() + 30 + i), marks, 0.95F, m.seed() + 30 + i);
			float own = m.age() - HemorathMove.CHAIN_DELAY;
			Vec3 foot = new Vec3(point.x, m.frame().floorY(), point.z);
			if (s.solids()) HemorathIronGeometry.chips(s.p(), s.iron(), s.local(foot), own, 1.5D, 6, m.seed() + 40 + i);
			else HemorathBloodGeometry.crown(s.p(), s.blood(), s.local(foot), Vec3.ZERO, s.right(), s.up(), s.time(),
					m.seed() + 44 + i, 0.6D, 0.6D, own < 0.0F ? 0.0F : HemoEasing.clamp01(1.0F - own / 14.0F));
		}
		if (m.age() < 0.0F) return;
		if (!s.solids()) shock(s, world(m.ground(), 1.2D, 0.0D, m.frame().floorY()), m.age(), 2.2D, 0.6D, m.seed() + 3);
		for (int k = 0; k * 0.9D + 0.8D < length; k++) {
			double along = 0.8D + k * 0.9D;
			float own = m.age() - (float) (along * 0.35D);
			double side = (HemorathFissureRules.unit(m.seed(), 50 + k) - 0.5D) * half;
			Vec3 foot = world(m.ground(), along, side, m.frame().floorY());
			if (s.solids()) {
				List<Spike> splinter = HemorathFissureRules.cluster(m.seed() + 60 + k, 0.9D + 0.3D * HemorathFissureRules.unit(m.seed(), 70 + k), 3);
				HemorathIronGeometry.cluster(s.p(), s.iron(), s.local(foot), splinter, own, 8.0F, s.time(), HemorathIronGeometry.IRON,
						m.seed() + 60 + k);
				if (k % 2 == 0) HemorathIronGeometry.chips(s.p(), s.iron(), s.local(foot), own, 1.2D, 3, m.seed() + 90 + k);
			} else {
				HemorathIronGeometry.rupture(s.p(), s.blood(), s.local(foot), s.right(), s.up(), own, 0.6D, m.seed() + 140 + k);
				if (k % 2 == 1) HemorathBloodGeometry.spatter(s.p(), s.blood(), s.local(foot).add(0.0D, 0.1D, 0.0D),
						new Vec3(m.ground().fz() * -side, 0.0D, m.ground().fx() * side), Vec3.ZERO, s.right(), s.up(), own,
						m.seed() + 120 + k, 2.0D, 0.8F);
			}
		}
	}

	/**
	 * Iron Rake (visual pass 2b): each lane is a trench. Through the wind-up the floor splits out from his feet along
	 * the lane and drops into a channel, slabs of floor heaving up along both lips, while blood floods the channel from
	 * the far end back toward him. At the commit the seam at its bottom flares white-hot; behind the front a plug of
	 * iron punches up out of the channel every one and a half blocks with chips and rust dust, shivers, then cracks and
	 * sinks back as the trench drains and closes. The trench's mouth is the lane's hit strip exactly.
	 */
	private static void rake(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Lane lane)) return;
		double half = lane.halfWidth();
		float close = trenchClose(m);
		for (int n = 0; n < lane.lanes(); n++) {
			int index = lane.index(n);
			Ground g = laneGround(m.ground(), lane.offset(index), lane.angle(index));
			int seed = m.seed() + index * 131;
			double front = lane.front(m.age(), index);
			trench(s, g, 0.0D, lane.length(), half, 0.0D, along -> HemorathFissureRules.trench(m.moveTime(), m.reveal(),
					m.windup(), m.active(), close, along, 0.0D, lane.length(), front), seed);
			if (m.age() < 0.0F) continue;
			int k = 0;
			for (double along = 1.0D; along <= lane.length(); along += RAKE_SITE, k++) {
				double speed = Math.max(0.01D, lane.frontSpeed());
				float erupt = (float) (lane.delay(index) + along / speed);
				float own = m.age() - erupt;
				if (own <= 0.0F) break;
				// out of the channel's middle, so the scab plugs the trench across its width
				double jitter = (HemorathFissureRules.unit(seed, k) - 0.5D) * half * 0.2D;
				Vec3 foot = world(g, along, jitter, m.frame().floorY());
				float sinkAt = m.active() - erupt + RAKE_HOLD;
				if (s.solids()) {
					double scale = 1.3D + 0.6D * HemorathFissureRules.unit(seed, 200 + k);
					List<Spike> spikes = HemorathFissureRules.cluster(seed + k * 17, scale, 5);
					HemorathIronGeometry.cluster(s.p(), s.iron(), s.local(foot), spikes, own, sinkAt, s.time(),
							HemorathIronGeometry.IRON, seed + k * 17);
					HemorathIronGeometry.chips(s.p(), s.iron(), s.local(foot), own, 1.3D, 4, seed + k * 23);
				} else {
					HemorathIronGeometry.dust(s.p(), s.blood(), s.local(foot), s.right(), s.up(), own, 0.8D, seed + k * 29);
					HemorathIronGeometry.rupture(s.p(), s.blood(), s.local(foot), s.right(), s.up(), own, 0.75D, seed + k * 31);
				}
			}
		}
	}

	/**
	 * Empty Pulse: the disc round him crazes and cracks out to its rim; through the wind-up blood is drawn in along the
	 * radial cracks toward his chest; at the release chips and blood are thrown out off the rim.
	 */
	private static void pulse(Scene s, Move m) {
		double radius = HemorathMove.PULSE_RADIUS;
		Look look = m.look();
		Vec3 centre = m.frame().origin();
		if (!s.solids()) {
			HemorathFissureGeometry.craze(s.p(), s.fissure(), m.ground(), 0.3D, radius, -180.0D, 180.0D, look, 0.4F, m.seed());
			s.cracks(m.ground(), HemorathFissureRules.disc(radius, m.seed()), look, 1.0F, m.seed());
			if (m.age() < 0.0F) {
				float pull = m.build();
				for (int i = 0; i < 12; i++) {
					double phase = (s.time() * (0.02D + 0.05D * pull) + i / 12.0D) % 1.0D;
					double r = radius * (1.0D - HemoEasing.easeInCubic(phase));
					double a = Math.toRadians(i * 30.0D + 8.0D);
					Vec3 head = s.local(centre.x + Math.cos(a) * r, m.frame().floorY() + 0.06D, centre.z + Math.sin(a) * r);
					Vec3 tail = s.local(centre.x + Math.cos(a) * (r + 1.4D), m.frame().floorY() + 0.06D, centre.z + Math.sin(a) * (r + 1.4D));
					float fade = pull * (float) Math.min(1.0D, (1.0D - phase) * 4.0D);
					LuxUmbraGeometry.ribbonPath(s.p(), s.blood(), List.of(tail, tail.lerp(head, 0.6D), head), Vec3.ZERO, 0.015D, 0.09D,
							LuxUmbraGeometry.SLASH, m.seed() + i, HemorathBloodGeometry.TINT, fade * 0.85F);
				}
			}
		}
		if (m.age() < 0.0F) return;
		if (!s.solids()) shock(s, new Vec3(centre.x, m.frame().floorY(), centre.z), m.age(), radius, 0.5D, m.seed() + 3);
		for (int i = 0; i < 10; i++) {
			double a = Math.toRadians(i * 36.0D + 18.0D);
			Vec3 rim = new Vec3(centre.x + Math.cos(a) * radius, m.frame().floorY(), centre.z + Math.sin(a) * radius);
			if (s.solids()) HemorathIronGeometry.chips(s.p(), s.iron(), s.local(rim), m.age() - 2.0F, 1.2D, 3, m.seed() + 40 + i);
			else HemorathBloodGeometry.spatter(s.p(), s.blood(), s.local(rim).add(0.0D, 0.15D, 0.0D), new Vec3(Math.cos(a), 0, Math.sin(a)),
					Vec3.ZERO, s.right(), s.up(), m.age() - 2.0F, m.seed() + 50 + i, 3.0D, 0.85F);
		}
	}

	/**
	 * Marrow Rupture: the pulse's ring cracks dark round him and the reach shows faintly at ten; then a wave rolls out:
	 * a cracking band of floor with a standing sheet of blood exactly as high as a jump clears, a pale crest on top,
	 * and chips kicked up all along its front.
	 */
	private static void rupture(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Ring ring)) return;
		Look look = m.look();
		Vec3 centre = m.frame().origin();
		double floor = m.frame().floorY();
		if (m.age() < 0.0F) {
			if (s.solids()) return;
			s.cracks(m.ground(), HemorathFissureRules.ring(1.6D, m.seed()), look, 1.0F, m.seed());
			HemorathFissureGeometry.craze(s.p(), s.fissure(), m.ground(), 0.3D, 1.9D, -180.0D, 180.0D, look, 0.6F, m.seed());
			s.cracks(m.ground(), HemorathFissureRules.ring(ring.to(), m.seed() + 3), look, 0.35F, m.seed() + 3);
			return;
		}
		double radius = ring.radius(m.age(), 0);
		if (radius < 0.0D) radius = ring.to();
		boolean rolling = m.age() <= ring.travelTicks();
		if (!s.solids()) {
			Look band = rolling ? new Look(1.0F, 0.85F, 1.0F, 1.0F, 1.1F, 1.0F) : look;
			s.cracks(m.ground(), HemorathFissureRules.ring(radius, m.seed() + 5), band, 1.0F, m.seed() + 5);
			HemorathFissureGeometry.craze(s.p(), s.fissure(), m.ground(), Math.max(0.0D, radius - 1.4D), radius + 0.4D, -180.0D,
					180.0D, band, 0.55F, m.seed() + 6);
			if (rolling) {
				s.p().pushPose();
				Vec3 at = s.local(centre);
				s.p().translate(at.x, at.y, at.z);
				HemorathBloodGeometry.wall(s.p(), s.blood(), radius, floor - centre.y, BossFootprint.Height.JUMPABLE.max(),
						s.camera().subtract(centre), s.right(), s.up(), s.time(), m.seed(), 1.0F);
				s.p().popPose();
			}
			return;
		}
		if (!rolling) return;
		for (int i = 0; i < 18; i++) {
			double a = Math.toRadians(i * 20.0D + m.seed() % 20);
			Vec3 front = new Vec3(centre.x + Math.cos(a) * radius, floor, centre.z + Math.sin(a) * radius);
			float own = (float) ((m.age() * 1.3D + i * 2.7D) % 7.0D);
			HemorathIronGeometry.chips(s.p(), s.iron(), s.local(front), own, 0.8D, 1, m.seed() + 30 + i * 3 + (int) (m.age() / 7.0F));
		}
	}

	/**
	 * Marrow Siphon: a glowing crack circle at fourteen, the floor crazed just inside it, beads of blood drawn in
	 * along the floor; through the active span every body in the field bleeds a beading drain to his chest.
	 */
	private static void siphon(Scene s, Move m) {
		double radius = HemorathMove.SIPHON_RADIUS;
		Look look = m.look();
		Vec3 centre = m.frame().origin();
		if (s.solids()) return;
		s.cracks(m.ground(), HemorathFissureRules.ring(radius, m.seed()), look, 0.9F, m.seed());
		HemorathFissureGeometry.craze(s.p(), s.fissure(), m.ground(), radius - 1.4D, radius, -180.0D, 180.0D, look, 0.35F, m.seed());
		float strength = look.alpha();
		for (int i = 0; i < 16; i++) {
			double phase = (s.time() * 0.03D + i / 16.0D) % 1.0D;
			double r = radius * (1.0D - HemoEasing.easeInCubic(phase));
			double a = Math.toRadians(i * 22.5D + 10.0D);
			Vec3 head = s.local(centre.x + Math.cos(a) * r, m.frame().floorY() + 0.06D, centre.z + Math.sin(a) * r);
			Vec3 tail = s.local(centre.x + Math.cos(a) * (r + 0.8D), m.frame().floorY() + 0.06D, centre.z + Math.sin(a) * (r + 0.8D));
			float fade = strength * (float) Math.min(1.0D, (1.0D - phase) * 4.0D) * 0.8F;
			LuxUmbraGeometry.ribbonPath(s.p(), s.blood(), List.of(tail, head), Vec3.ZERO, 0.008D, 0.04D, LuxUmbraGeometry.RIBBON,
					m.seed() + i, HemorathBloodGeometry.TINT, fade);
			LuxUmbraGeometry.card(s.p(), s.blood(), head, s.right(), s.up(), 0.05D, 0.05D, LuxUmbraGeometry.FOCUS, m.seed() + 20 + i,
					HemorathBloodGeometry.TINT, fade);
		}
		if (m.age() < 0.0F || m.age() > m.active()) return;
		Vec3 chest = s.local(m.boss().getPosition(s.partial()).add(0.0D, HemorathEntity.CHEST_HEIGHT, 0.0D));
		float ramp = HemoEasing.clamp01(m.age() / 6.0F) * (1.0F - HemoEasing.ramp(m.age(), m.active() - 6.0F, m.active()));
		for (Player player : s.level().players()) {
			if (player.isSpectator() || !player.isAlive()) continue;
			Vec3 feet = player.getPosition(s.partial());
			if (Shapes.planar(feet, centre) > radius) continue;
			Vec3 from = s.local(feet.add(0.0D, player.getBbHeight() * 0.6D, 0.0D));
			// in first person the drain leaves from a little in front of the viewer, never from inside their eye
			Minecraft mc = Minecraft.getInstance();
			if (player == mc.player && mc.options.getCameraType().isFirstPerson() && from.distanceTo(chest) > 3.0D) {
				from = from.add(chest.subtract(from).normalize().scale(1.6D));
			}
			HemorathBloodGeometry.drain(s.p(), s.blood(), from, chest, Vec3.ZERO, s.right(), s.up(), s.time(), player.getId(), ramp);
		}
	}

	/** Chained Debt: a narrow groove scored along the throw; the chain itself is {@code HemorathMoveGeometry}'s. */
	private static void debt(Scene s, Move m) {
		if (s.solids()) return;
		s.cracks(m.ground(), HemorathFissureRules.lane(HemorathMove.CHAIN_LENGTH, HemorathMove.CHAIN_HALF_WIDTH, m.seed()),
				m.look(), 0.7F, m.seed());
	}

	/** Tithe Reckoning: the floor under the debtor cracks in a star while the chain is counted out. */
	private static void tithe(Scene s, Move m) {
		if (s.solids()) return;
		Vec3 target = m.frame().target();
		s.cracks(m.ground().at(target), HemorathFissureRules.star(1.1D, m.seed()), m.look(), 0.85F, m.seed());
	}

	/**
	 * Spiked Halo: the out line and the bowed return arc scored shallow into the floor through the wind-up; once it
	 * flies, the path behind the halo's head splits and bleeds as it passes.
	 */
	private static void halo(Scene s, Move m) {
		if (s.solids()) return;
		List<Vec3> points = m.frame().points();
		if (points.size() < 2) return;
		List<double[]> local = new ArrayList<>(points.size());
		for (Vec3 point : points) local.add(Shapes.local(m.frame(), m.frame().origin(), point, 0.0D));
		List<Crack> path = HemorathFissureRules.path(local, HemorathFissureRules.BRANCH * 1.6D, m.seed());
		Look look = m.look();
		Look scored = new Look(look.grow(), look.heat() * 0.6F, 0.0F, look.rust(), Math.min(look.width(), 0.6F), look.alpha() * 0.8F);
		s.cracks(m.ground(), path, scored, 1.0F, m.seed());
		if (m.age() < 0.0F) return;
		float flown = HemoEasing.clamp01(m.age() / (float) HemorathHaloPath.FLIGHT_TICKS);
		Look split = new Look(flown, look.heat(), look.blood(), 1.0F, look.width(), look.alpha());
		s.cracks(m.ground(), path, split, 1.0F, m.seed() + 9);
	}

	/**
	 * Crown Fall: the disc round him cracks and a ring of iron stubs pokes up through it as he catches the halo; at
	 * the commit fourteen clusters punch out round the ring, leaning out, rippling round from his front; they shiver,
	 * then crack and sink as the floor seals.
	 */
	private static void crown(Scene s, Move m) {
		double radius = HemorathMove.CROWN_RADIUS;
		Look look = m.look();
		Vec3 centre = m.frame().origin();
		if (!s.solids()) {
			HemorathFissureGeometry.craze(s.p(), s.fissure(), m.ground(), 0.3D, radius, -180.0D, 180.0D, look, 0.45F, m.seed());
			s.cracks(m.ground(), HemorathFissureRules.disc(radius, m.seed()), look, 1.0F, m.seed());
		}
		float sinkAt = m.active() + m.recovery() - HemorathFissureRules.SINK - 4.0F;
		if (!s.solids()) shock(s, new Vec3(centre.x, m.frame().floorY(), centre.z), m.age(), radius + 0.8D, 0.7D, m.seed() + 3);
		for (int i = 0; i < CROWN_CLUSTERS; i++) {
			double a = Math.toRadians(i * 360.0D / CROWN_CLUSTERS);
			Vec3 foot = world(m.ground(), Math.cos(a) * radius, Math.sin(a) * radius, m.frame().floorY());
			Vec3 out = new Vec3(foot.x - centre.x, 0.0D, foot.z - centre.z).normalize();
			int seed = m.seed() + i * 19;
			if (m.age() < 0.0F) {
				// the stubs are part of the tell, so a hidden tell (the Beholding deed) hides them too
				if (!s.solids() || m.moveTime() < m.reveal()) continue;
				List<Spike> stub = HemorathFissureRules.cluster(seed, 0.35D, 1);
				HemorathIronGeometry.spike(s.p(), s.iron(), s.local(foot), stub.get(0), 0.25D + 0.75D * m.build(), 0.6F + 0.4F * m.build(),
						0.0D, HemorathIronGeometry.IRON, seed);
				continue;
			}
			float own = m.age() - (float) (Math.abs(Math.sin(a * 0.5D)) * 3.0D);
			if (s.solids()) {
				List<Spike> spikes = outward(HemorathFissureRules.cluster(seed, 1.8D + 0.6D * HemorathFissureRules.unit(seed, 1), 4),
						out.x, out.z, 0.22D);
				HemorathIronGeometry.cluster(s.p(), s.iron(), s.local(foot), spikes, own, sinkAt, s.time(), HemorathIronGeometry.IRON, seed);
				HemorathIronGeometry.chips(s.p(), s.iron(), s.local(foot), own, 1.4D, 3, seed + 7);
			} else {
				HemorathIronGeometry.dust(s.p(), s.blood(), s.local(foot), s.right(), s.up(), own, 1.0D, seed + 9);
				HemorathIronGeometry.rupture(s.p(), s.blood(), s.local(foot), s.right(), s.up(), own, 0.95D, seed + 11);
			}
		}
	}

	/**
	 * Iron Thicket: a starburst of rust cracks at each landing, blood welling in them; iron torn from his bones flies
	 * overhead from his chest to each landing through the last twelve ticks of the wind-up. The standing iron is the
	 * hazard's.
	 */
	private static void thicket(Scene s, Move m) {
		List<Vec3> points = m.frame().points();
		Look look = m.look();
		Look rusty = new Look(look.grow(), look.heat(), look.blood(), 1.0F, look.width(), look.alpha());
		for (int i = 0; i < points.size(); i++) {
			Vec3 point = points.get(i);
			int seed = m.seed() + i * 41;
			if (!s.solids()) {
				Ground g = m.ground().at(point);
				HemorathFissureGeometry.craze(s.p(), s.fissure(), g, 0.0D, HemorathMove.THICKET_RADIUS, -180.0D, 180.0D, rusty, 0.55F, seed);
				s.cracks(g, HemorathFissureRules.star(HemorathMove.THICKET_RADIUS, seed), rusty, 1.0F, seed);
				continue;
			}
			float t = (m.moveTime() - (m.windup() - THROW_TICKS)) / THROW_TICKS;
			if (t <= 0.0F || t >= 1.0F) continue;
			Vec3 chest = m.boss().getPosition(s.partial()).add(0.0D, HemorathEntity.CHEST_HEIGHT + 0.6D, 0.0D);
			Vec3 land = new Vec3(point.x, m.frame().floorY() + 0.2D, point.z);
			Vec3 at = chest.lerp(land, t).add(0.0D, 4.0D * t * (1.0F - t) * 3.0D, 0.0D);
			HemorathIronGeometry.chip(s.p(), s.iron(), s.local(at), 0.2D, m.moveTime() * 0.8D + i, HemorathIronGeometry.IRON, seed);
			HemorathIronGeometry.chip(s.p(), s.iron(), s.local(at.add(0.12D, -0.08D, 0.05D)), 0.11D, m.moveTime() * 1.1D + i,
					HemorathIronGeometry.TIP, seed + 1);
		}
	}

	/**
	 * Exsanguinating Tide: the boss floor's rim cracks glowing and the whole floor crazing and sweating blood as he
	 * presses it out; the flood itself is the hazard's.
	 */
	private static void tide(Scene s, Move m) {
		if (s.solids() || m.age() > HemorathFissureRules.FLASH) return;
		AABB floor = m.frame().bossFloor();
		Vec3 middle = new Vec3((floor.minX + floor.maxX) * 0.5D, m.frame().floorY(), (floor.minZ + floor.maxZ) * 0.5D);
		Ground g = new Ground(middle.x, middle.z, 1.0D, 0.0D, m.frame().floorY(), s.camera());
		double hx = (floor.maxX - floor.minX) * 0.5D, hz = (floor.maxZ - floor.minZ) * 0.5D;
		Look look = m.look();
		s.cracks(g, HemorathFissureRules.rim(hx, hz, m.seed()), look, 1.0F, m.seed());
		Look sweat = new Look(1.0F, look.heat() * 0.7F, look.blood(), look.rust(), 1.0F, look.alpha() * m.build());
		HemorathFissureGeometry.crazeRect(s.p(), s.fissure(), g, -hx, hx, -hz, hz, sweat, 0.35F, m.seed() + 1);
		int beads = (int) (14 * m.build());
		for (int i = 0; i < beads; i++) {
			double x = middle.x + (HemorathFissureRules.unit(m.seed(), 70 + i) - 0.5D) * hx * 1.8D;
			double z = middle.z + (HemorathFissureRules.unit(m.seed(), 90 + i) - 0.5D) * hz * 1.8D;
			float swell = (float) ((s.time() * 0.05D + i * 0.31D) % 1.0D);
			LuxUmbraGeometry.ground(s.p(), s.blood(), s.local(x, m.frame().floorY() + 0.03D, z), 0.15D + 0.35D * swell * m.build(),
					LuxUmbraGeometry.POOL, m.seed() + i, HemorathBloodGeometry.TINT, 0.7F * look.alpha());
		}
	}

	/**
	 * Rib Burst (visual pass 2b): eight trenches split out from him in a star, each spoke's hit strip exactly, with the
	 * same life as the Iron Rake's lanes scaled to the spoke's width (the wedges between stay clean stone, the safe
	 * read; slabs heave only where they clear the next spoke); through the active span blood geysers up out of each
	 * channel behind its front, with chips.
	 */
	private static void ribs(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Spokes spokes)) return;
		double reach = m.age() < 0.0F ? spokes.length() : spokes.reach(m.age());
		double front = m.age() < 0.0F ? -1.0D : reach;
		double half = spokes.halfWidth();
		float close = trenchClose(m);
		// near him the spokes crowd together: a slab starts only where it cannot reach into the next spoke's strip
		double gap = Math.toRadians(360.0D / Math.max(1, spokes.count()));
		double slabsFrom = gap >= Math.PI * 0.5D ? spokes.inner()
				: (half + (half * (1.0D + HemorathFissureRules.SLAB_REACH) + 0.05D) * Math.cos(gap)) / Math.sin(gap);
		for (int i = 0; i < spokes.count(); i++) {
			Ground g = laneGround(m.ground(), 0.0D, spokes.angle(i));
			int seed = m.seed() + i * 53;
			trench(s, g, spokes.inner(), spokes.length(), half, slabsFrom, along -> HemorathFissureRules.trench(m.moveTime(),
					m.reveal(), m.windup(), m.active(), close, along, spokes.inner(), spokes.length(), front), seed);
			if (m.age() < 0.0F) continue;
			int k = 0;
			for (double along = spokes.inner() + 0.8D; along <= reach; along += 1.3D, k++) {
				float own = m.age() - (float) ((along - spokes.inner()) / Math.max(0.01D, spokes.frontSpeed()));
				Vec3 foot = world(g, along, 0.0D, m.frame().floorY());
				if (s.solids()) HemorathIronGeometry.chips(s.p(), s.iron(), s.local(foot), own, 1.0D, 2, seed + k);
				else if (own < 12.0F) HemorathBloodGeometry.crown(s.p(), s.blood(), s.local(foot), Vec3.ZERO, s.right(), s.up(), s.time(),
						seed + k, 0.9D, 0.4D, HemoEasing.clamp01(1.0F - own / 12.0F));
			}
		}
	}

	/** The Gorge: nothing before the pose; while it pulls, the dust of the floor is dragged in toward him. */
	private static void gorge(Scene s, Move m) {
		if (s.solids() || m.age() < 0.0F || m.age() > m.active()) return;
		if (!(m.footprint() instanceof BossFootprint.Cone cone)) return;
		for (int i = 0; i < 12; i++) {
			double phase = (s.time() * 0.06D + i / 12.0D) % 1.0D;
			double bearing = (HemorathFissureRules.unit(m.seed(), i) - 0.5D) * cone.degrees();
			double r = cone.radius() * (1.0D - phase);
			Vec3 head = world(m.ground(), Math.cos(Math.toRadians(bearing)) * r, Math.sin(Math.toRadians(bearing)) * r,
					m.frame().floorY() + 0.08D);
			Vec3 tail = world(m.ground(), Math.cos(Math.toRadians(bearing)) * (r + 1.0D), Math.sin(Math.toRadians(bearing)) * (r + 1.0D),
					m.frame().floorY() + 0.08D);
			float fade = (float) (Math.sin(phase * Math.PI) * 0.6D);
			LuxUmbraGeometry.ribbonPath(s.p(), s.blood(), List.of(s.local(tail), s.local(head)), Vec3.ZERO, 0.01D, 0.04D,
					LuxUmbraGeometry.RIBBON, m.seed() + i, 0xB0705C, fade);
		}
	}

	/** The Last Thirst: the whole chamber is his, a crack circle glowing at eighteen round him. */
	private static void thirstRing(Scene s, HemorathEntity boss) {
		if (s.solids()) return;
		Vec3 centre = boss.getPosition(s.partial());
		double floor = boss.getY();
		float beat = (float) Math.pow(Math.max(0.0D, Math.sin(s.time() * 0.18D)), 4.0D);
		Look look = new Look(1.0F, 0.45F + 0.35F * beat, 0.85F, 1.0F, 1.0F, 0.8F);
		Ground g = new Ground(centre.x, centre.z, 1.0D, 0.0D, floor, s.camera());
		s.cracks(g, HemorathFissureRules.ring(HemorathPresenceRules.THIRST_RADIUS, boss.getId()), look, 0.85F, boss.getId());
	}

	// ── hazards ──────────────────────────────────────────────────────

	private static void hazard(Scene s, BossGroundHazardEntity hazard) {
		float age = AnimationTime.age(hazard, s.partial());
		float fade = SaintTelegraphTiming.hazardFade(age, hazard.life(), hazard.fade(), hazard.fading(), hazard.fadeStart());
		if (fade <= 0.0F) return;
		HemorathEntity boss = (HemorathEntity) hazard.owner();
		if (hazard.kind() == HemorathEntity.HAZARD_THICKET) standingIron(s, hazard, age, fade);
		else if (hazard.kind() == HemorathEntity.HAZARD_TIDE) flood(s, hazard, boss, age, fade);
	}

	/**
	 * The Thicket standing: at each landing a big cluster and three smaller ones punch out, two ticks apart in the
	 * landing order, shiver, rust dark over their last sixty ticks and crack and sink over the last sixteen; under them
	 * the rust cracks stay, blood standing in them.
	 */
	private static void standingIron(Scene s, BossGroundHazardEntity hazard, float age, float fade) {
		FootprintFrame frame = hazard.frame();
		int gap = hazard.shape() instanceof BossFootprint.Discs discs ? discs.landingGap() : 0;
		double radius = hazard.shape() instanceof BossFootprint.Discs discs ? discs.radius() : HemorathMove.THICKET_RADIUS;
		float life = hazard.life();
		float sinkAt = life - THICKET_SINK;
		float rust = HemoEasing.ramp(age, life - THICKET_RUST, sinkAt);
		int tint = HemorathIronGeometry.rusted(rust);
		List<Vec3> points = frame.points();
		for (int i = 0; i < points.size(); i++) {
			float own = age - i * gap;
			if (own <= 0.0F) continue;
			Vec3 point = points.get(i);
			int seed = hazard.getId() * 13 + i * 97;
			Vec3 foot = new Vec3(point.x, frame.floorY(), point.z);
			if (!s.solids()) {
				float cool = 1.0F - HemoEasing.ramp(own, 0.0F, 40.0F);
				Look look = new Look(1.0F, 0.15F + 0.55F * cool, 0.85F * (1.0F - rust * 0.5F), 1.0F,
						1.0F - HemoEasing.ramp(age, sinkAt, life), fade);
				Ground g = new Ground(point.x, point.z, 1.0D, 0.0D, frame.floorY(), s.camera());
				HemorathFissureGeometry.craze(s.p(), s.fissure(), g, 0.0D, radius, -180.0D, 180.0D, look, 0.5F, seed);
				s.cracks(g, HemorathFissureRules.star(radius, seed), look, 1.0F, seed);
				HemorathIronGeometry.dust(s.p(), s.blood(), s.local(foot), s.right(), s.up(), own, 1.1D, seed + 3);
				HemorathIronGeometry.rupture(s.p(), s.blood(), s.local(foot), s.right(), s.up(), own, 1.3D, seed + 5);
				LuxUmbraGeometry.ground(s.p(), s.blood(), s.local(foot).add(0.0D, 0.03D, 0.0D), 0.7D, LuxUmbraGeometry.POOL, seed,
						HemorathBloodGeometry.TINT, 0.55F * fade * (1.0F - rust * 0.6F));
				continue;
			}
			HemorathIronGeometry.cluster(s.p(), s.iron(), s.local(foot), HemorathFissureRules.cluster(seed, 2.1D, 6), own,
					sinkAt - i * gap, s.time(), tint, seed);
			for (int k = 0; k < 3; k++) {
				double a = k * 2.0944D + HemorathFissureRules.unit(seed, k) * 0.8D;
				double out = radius * (0.5D + 0.25D * HemorathFissureRules.unit(seed, 10 + k));
				Vec3 at = foot.add(Math.cos(a) * out, 0.0D, Math.sin(a) * out);
				HemorathIronGeometry.cluster(s.p(), s.iron(), s.local(at), outward(HemorathFissureRules.cluster(seed + k * 7, 1.2D, 4),
						Math.cos(a), Math.sin(a), 0.18D), own - 1.0F - k * 0.7F, sinkAt - i * gap, s.time(), tint, seed + k * 7);
			}
			HemorathIronGeometry.chips(s.p(), s.iron(), s.local(foot), own, 1.6D, 7, seed + 40);
		}
	}

	/**
	 * The tide standing on the boss floor: one sheet of glossy blood (the brazier pool's shader, square, filling the
	 * boss floor to its edges) rising to half a block over eight ticks with ripples running out from where he dumped
	 * it, a roll of blood sweeping out from his feet at the flood, a lit meniscus along the edges, and at the end the
	 * surface sinking while four streams run off the floor to the wells.
	 */
	private static void flood(Scene s, BossGroundHazardEntity hazard, HemorathEntity boss, float age, float fade) {
		FootprintFrame frame = hazard.frame();
		AABB floor = frame.bossFloor();
		double cx = (floor.minX + floor.maxX) * 0.5D, cz = (floor.minZ + floor.maxZ) * 0.5D;
		double half = Math.max(floor.maxX - floor.minX, floor.maxZ - floor.minZ) * 0.5D;
		float life = hazard.life();
		float rise = HemoEasing.smoothstep(HemoEasing.clamp01(age / TIDE_RISE));
		float drain = HemoEasing.ramp(age, life - TIDE_DRAIN, life + SaintTelegraphTiming.HAZARD_NATURAL_FADE);
		double surface = frame.floorY() + 0.04D + TIDE_DEPTH * rise * (1.0F - drain * 0.85F);
		int seed = hazard.getId();
		if (s.solids()) return;
		POOLS.add(new Pool(cx, surface, cz, half, 70, 40, Math.round(225.0F * rise * fade * (1.0F - drain * 0.6F)), seed));
		Vec3 origin = frame.origin();
		if (age < 12.0F) {
			double reach = Math.hypot(half, half) * HemoEasing.easeOutCubic(HemoEasing.clamp01(age / 10.0F));
			s.p().pushPose();
			Vec3 at = s.local(origin.x, frame.floorY(), origin.z);
			s.p().translate(at.x, at.y, at.z);
			HemorathBloodGeometry.wall(s.p(), s.blood(), Math.max(0.3D, reach), 0.0D, 0.55D * (1.0D - age / 12.0D),
					s.camera().subtract(origin.x, frame.floorY(), origin.z), s.right(), s.up(), s.time(), seed, 1.0F - age / 12.0F);
			s.p().popPose();
		}
		List<Vec3> edge = List.of(s.local(floor.minX, surface + 0.01D, floor.minZ), s.local(floor.maxX, surface + 0.01D, floor.minZ),
				s.local(floor.maxX, surface + 0.01D, floor.maxZ), s.local(floor.minX, surface + 0.01D, floor.maxZ),
				s.local(floor.minX, surface + 0.01D, floor.minZ));
		LuxUmbraGeometry.ribbonPath(s.p(), s.blood(), edge, Vec3.ZERO, 0.035D, LuxUmbraGeometry.RIBBON, seed,
				HemorathBloodGeometry.CORE, 0.55F * rise * fade);
		if (drain <= 0.0F) return;
		List<BlockPos> wells = boss.syncedWellPositions();
		for (int i = 0; i < wells.size(); i++) {
			Vec3 well = Vec3.atBottomCenterOf(wells.get(i)).add(0.0D, 0.9D, 0.0D);
			double ex = Math.max(floor.minX, Math.min(floor.maxX, well.x)), ez = Math.max(floor.minZ, Math.min(floor.maxZ, well.z));
			Vec3 from = new Vec3(ex, surface, ez);
			HemorathBloodGeometry.stream(s.p(), s.blood(), s.local(from), s.local(well), Vec3.ZERO, s.right(), s.up(), s.time(),
					seed + i * 11, HemoEasing.clamp01(drain * 1.6F), 0.35D, 0.13D, 0.3D, fade);
		}
	}

	/** Draws the tide surfaces queued this frame, with the camera-relative world axes the pool shader expects. */
	private static void pools(MultiBufferSource.BufferSource buffers, Matrix4f modelView, Vec3 camera, double time) {
		if (POOLS.isEmpty()) return;
		Matrix4fStack stack = RenderSystem.getModelViewStack();
		stack.pushMatrix();
		stack.mul(modelView);
		RenderSystem.applyModelViewMatrix();
		try {
			Matrix4f identity = new Matrix4f();
			for (Pool pool : POOLS) {
				if (pool.alpha() <= 2) continue;
				RenderType type = HemoRenderTypes.squareBloodPool((float) (time % 24000.0D), Math.floorMod(pool.seed(), 997));
				BloodPoolSurface.draw(identity, buffers.getBuffer(type), (float) (pool.x() - camera.x), (float) (pool.y() - camera.y),
						(float) (pool.z() - camera.z), (float) pool.half(), pool.jet(), pool.heat(), 0, pool.alpha());
				buffers.endBatch(type);
			}
		} finally {
			stack.popMatrix();
			RenderSystem.applyModelViewMatrix();
		}
		POOLS.clear();
	}

	// ── strikes ──────────────────────────────────────────────────────

	/**
	 * The claw's three rakes through the air, on the Hematic Riposte recipe, laid along the path his claw hand really
	 * took (the renderer keeps it, {@link HemorathAnimState#clawAt}), so they follow the animation's swing whatever its
	 * direction. Each rake is a thick slash ribbon tapering from its tail to its head, a clot-dark leading half and a
	 * pale core line; the three are fanned across the swing. The trail runs with the hand through the strike, then
	 * holds where it was cut, widens, drifts up and frays out, dropping blood.
	 */
	private static void slashes(Scene s, Move m, double radius) {
		float head = HemorathFissureRules.slashHead(m.age());
		if (head < 0.0F) return;
		HemorathAnimState motion = HemorathAnimState.of(m.boss());
		float now = AnimationTime.age(m.boss(), s.partial());
		float commit = now - m.age();
		float end = Math.min(now, commit + HemorathFissureRules.SLASH_SWEEP - HemorathFissureRules.SLASH_LEAD + 1.0F);
		float begin = Math.max(commit - HemorathFissureRules.SLASH_LEAD - 1.5F, end - 5.0F);
		if (end - begin < 0.5F) return;
		int n = 14;
		List<Vec3> hand = new ArrayList<>(n + 1);
		for (int i = 0; i <= n; i++) {
			Vec3 at = motion.clawAt(begin + (end - begin) * i / n);
			if (at != null) hand.add(at);
		}
		if (hand.size() < 3 || hand.get(0).distanceToSqr(hand.get(hand.size() - 1)) < 0.36D) return;
		float fade = HemorathFissureRules.slashFade(m.age()), fray = HemorathFissureRules.slashFray(m.age());
		double scale = Math.max(0.75D, radius / HemorathMove.CLAW_RADIUS);
		// the claws fan across the swing as the viewer sees it, so three parallel rakes read apart on screen
		Vec3 swing = hand.get(hand.size() - 1).subtract(hand.get(0)).normalize();
		Vec3 toEye = s.camera().subtract(hand.get(hand.size() / 2)).normalize();
		Vec3 fan = swing.cross(toEye);
		fan = fan.lengthSqr() < 1e-4 ? new Vec3(0, 1, 0) : fan.normalize();
		int count = hand.size();
		for (int c = 0; c < 3; c++) {
			double offset = (c - 1) * 0.34D * scale * (1.0D + 0.4D * fray);
			List<Vec3> path = new ArrayList<>(count);
			double[] widths = new double[count], dark = new double[count], core = new double[count];
			for (int i = 0; i < count; i++) {
				double f = i / (double) (count - 1);
				// the outer claws trail the middle one a little, and the whole rake lifts as it frays
				Vec3 at = hand.get(i).add(fan.scale(offset * (0.6D + 0.4D * f))).add(0.0D, fray * 0.4D * f, 0.0D);
				path.add(s.local(at));
				// thin at the tail, full just behind the head, a point at the head itself
				double body = (0.02D + 0.13D * Math.pow(f, 1.3D) * Math.min(1.0D, (1.0D - f) * 6.0D + 0.25D)) * scale
						* (1.0D + fray * 0.8D) * (c == 1 ? 1.2D : 1.0D);
				widths[i] = body;
				dark[i] = f < 0.45D ? 0.0D : body * 0.55D;
				core[i] = body * 0.22D * (1.0D - fray);
			}
			int seed = m.seed() + 300 + c * 7;
			float flicker = (float) (0.88D + 0.12D * Math.sin(s.time() * 0.9D + c * 1.7D + m.seed()));
			LuxUmbraGeometry.ribbonPath(s.p(), s.blood(), path, Vec3.ZERO, widths, LuxUmbraGeometry.SLASH, seed,
					HemorathBloodGeometry.TINT, 0.95F * fade * flicker);
			LuxUmbraGeometry.ribbonPath(s.p(), s.blood(), path, Vec3.ZERO, dark, LuxUmbraGeometry.SLASH, seed + 1,
					HemorathBloodGeometry.CLOT, fade);
			LuxUmbraGeometry.ribbonPath(s.p(), s.blood(), path, Vec3.ZERO, core, LuxUmbraGeometry.RIBBON, seed + 2,
					HemorathBloodGeometry.CORE, 0.9F * fade);
			if (head < 1.0F) {
				LuxUmbraGeometry.card(s.p(), s.blood(), path.get(count - 1), s.right(), s.up(), 0.09D * scale, 0.09D * scale,
						LuxUmbraGeometry.FOCUS, seed + 3, HemorathBloodGeometry.CORE, 0.8F);
			}
			// drops flung off the rake as it frays
			if (fray > 0.0F) {
				for (int k = 0; k < 3; k++) {
					Vec3 at = path.get(Math.min(count - 1, 2 + k * count / 4)).add(0.0D, -fray * fray * (0.7D + 0.35D * k), 0.0D);
					LuxUmbraGeometry.card(s.p(), s.blood(), at, s.right(), s.up(), 0.045D, 0.06D, LuxUmbraGeometry.FOCUS,
							seed + 10 + k, HemorathBloodGeometry.TINT, fade);
				}
			}
		}
	}

	/**
	 * A slam's shock: a low ring of blood thrown out across the floor from {@code at}, its crest pale, dying as it
	 * reaches {@code reach}, with a crown of spurts at the point of impact.
	 */
	private static void shock(Scene s, Vec3 at, float age, double reach, double height, int seed) {
		double radius = HemorathFissureRules.shockRadius(age, reach);
		if (radius < 0.0D) return;
		float t = age / HemorathFissureRules.SHOCK;
		s.p().pushPose();
		Vec3 local = s.local(at);
		s.p().translate(local.x, local.y, local.z);
		HemorathBloodGeometry.wall(s.p(), s.blood(), radius, 0.0D, height * (1.0D - t * 0.5D), s.camera().subtract(at), s.right(),
				s.up(), s.time(), seed, 0.85F * (1.0F - t * t));
		s.p().popPose();
		HemorathBloodGeometry.crown(s.p(), s.blood(), local, Vec3.ZERO, s.right(), s.up(), s.time(), seed + 1, 0.9D, 0.8D,
				1.0F - t);
		if (age < 3.0F) {
			LuxUmbraGeometry.ground(s.p(), s.blood(), local.add(0.0D, 0.04D, 0.0D), 0.9D + age * 0.3D, LuxUmbraGeometry.POOL, seed + 2,
					HemorathBloodGeometry.CORE, 0.7F * (1.0F - age / 3.0F));
		}
	}

	// ── trenches ─────────────────────────────────────────────────────

	/**
	 * A strip of floor split and sunk into a trench from {@code from} to {@code to} along the ground's line,
	 * {@code half} to either side (the footprint's own strip): in the fissure pass its mouth, which the shader looks
	 * down into; in the solid pass the slabs heaved up along both lips (from {@code slabsFrom} on) with their rubble.
	 * {@code state} is the trench's state at a distance along.
	 */
	private static void trench(Scene s, Ground g, double from, double to, double half, double slabsFrom,
			DoubleFunction<Trench> state, int seed) {
		if (!s.solids()) {
			HemorathFissureGeometry.trench(s.p(), s.fissure(), g, from, to, half, state, seed);
			return;
		}
		List<Slab> slabs = HemorathFissureRules.slabs(Math.max(from, slabsFrom), to, half, seed);
		for (int i = 0; i < slabs.size(); i++) {
			Slab slab = slabs.get(i);
			double lift = HemorathFissureRules.slabLift(slab, state.apply(slab.along()).heave());
			HemorathIronGeometry.slab(s.p(), s.iron(), g, slab, half, lift, seed + 400 + i * 13);
		}
	}

	/** How long a move's trenches take to drain and close: never past its recovery, when the move stops drawing. */
	private static float trenchClose(Move m) {
		return Math.min(HemorathFissureRules.TRENCH_CLOSE, Math.max(1, m.recovery()));
	}

	// ── pieces ───────────────────────────────────────────────────────

	private static boolean risingOrEnding(HemorathEntity boss, float partial) {
		float rise = boss.riseTime(partial);
		if (rise >= 0.0F && rise < SaintRise.TICKS) return true;
		return boss.syncedEndingTick() >= 0;
	}

	/** The ground of one lane: offset to the aim's right, turned by its fan angle. */
	private static Ground laneGround(Ground g, double offset, double angle) {
		double[] forward = Shapes.rotate(new double[]{g.fx(), g.fz()}, angle);
		return new Ground(g.wx(0.0D, offset), g.wz(0.0D, offset), forward[0], forward[1], g.y(), g.camera());
	}

	/** A local point of a ground at height {@code y}, in the world. */
	private static Vec3 world(Ground g, double along, double right, double y) {
		return new Vec3(g.wx(along, right), y, g.wz(along, right));
	}

	/** The cluster leaning out along {x, z} by {@code lean} more per block of height. */
	private static List<Spike> outward(List<Spike> spikes, double x, double z, double lean) {
		List<Spike> out = new ArrayList<>(spikes.size());
		for (Spike spike : spikes) {
			out.add(new Spike(spike.dx(), spike.dz(), spike.height(), spike.radius(), spike.leanX() + x * lean,
					spike.leanZ() + z * lean, spike.sides(), spike.turn()));
		}
		return out;
	}

	private static List<Crack> reversed(List<Crack> cracks) {
		List<Crack> out = new ArrayList<>(cracks.size());
		for (Crack crack : cracks) {
			int n = crack.size();
			double[] x = new double[n], y = new double[n];
			for (int i = 0; i < n; i++) {
				x[i] = crack.x()[n - 1 - i];
				y[i] = crack.y()[n - 1 - i];
			}
			out.add(new Crack(x, y, crack.width(), crack.start(), crack.span()));
		}
		return out;
	}
}
