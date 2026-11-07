package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.entity.boss.velorum.VelorumAnimState;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.world.VelorumDarkGeometry.Light;
import com.vincenthuto.hemomancy.client.render.world.VelorumFrostGeometry.Field;
import com.vincenthuto.hemomancy.client.render.world.VelorumFrostGeometry.Ground;
import com.vincenthuto.hemomancy.client.render.world.VelorumFrostRules.Look;
import com.vincenthuto.hemomancy.client.render.world.VelorumFrostRules.Seam;
import com.vincenthuto.hemomancy.client.render.world.VelorumFrostRules.Shard;
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
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumDevoteeEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEffects;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumOpeningRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumPresence;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumStillnessRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumVisualRules;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Saint Velorum's own telegraph, hazard and impact pass (visual pass 2, 2026-11-06), run from
 * {@code ManipulationVisualRenderer} in both of the manipulation visuals' passes. His {@link VelorumTelegraphStyle}
 * claims every move and the black ice and paints nothing, so the kit's flat tints never show for him; this pass reads
 * the same synced move, footprint, frame, timeline and reveal the kit does, so every drawn edge is the hit test's
 * edge. His language is the cold arresting the floor and the dark narrowing sight: seams freezing shut, rime spreading
 * plate by plate, black ice growing, the floor sinking into trenches that black ice wells up to fill, folds of darkness
 * rising and flooding out, and real ice standing up out of it all, which punches, holds and sublimes. Three phases a
 * frame: the solid ice (in the solid pass), then the floor and the trails in his frost material with the school flows
 * as accents, then the clear panes of a preserving shell. Things slow under him; nothing ever freezes in mid-air.
 * <p>
 * Revision 2026-11-06 (Frozen Cruor and liquid dark): every piece of his ice is Frozen Cruor, blood arrested where it
 * stood, and it is always shown leaving its source first (the floor's seams, the congregation's veins, the challengers'
 * own blood); his dark is liquid, drawn in a DARK phase before everything else so every telegraph draws after it; and
 * the six new moves (Arrested Pulse, Cruor Javelins, Veins of Frost, the Shadow Step, Gloam Cuts and the Eclipse), the
 * pools of dark, the Eclipse's shapes and reaches and the logged beats ({@link VelorumCueLog}) are drawn here too, with
 * {@link VelorumCruorGeometry} and {@link VelorumDarkGeometry}.
 */
final class VelorumTelegraphPass {
	private static final double RANGE = 64.0D;
	/** Black-Ice splinter sites every this many blocks along a lane. */
	private static final double SITE = 1.45D;
	/** Never: ice that the standing hazard will take over. */
	private static final float NEVER = 1.0E6F;
	/** The accents' colours: frost white, the pale core and a cold light (frost light, the only blue), and the liquid dark. */
	private static final int WHITE = 0xFFFFFF, CORE = 0xE8F4FF, COLD = 0xBFE2FF, DARK = 0x140A1E;

	enum Phase {
		DARK, SOLID, FLOOR, CLEAR
	}

	/** The limbs a cut follows: the staff's lantern, the side lantern on its hip chain, the crooked hand. */
	private enum Limb {
		STAFF, SIDE, CLAW
	}

	/** A cut's path, kept once its sweep has ended so it can hold and fray without the limb's history. */
	private record Cut(int key, List<Vec3> path) {
	}

	/** The pass's frame: where it draws and in what. Points handed to geometry are camera-relative. */
	record Scene(PoseStack p, Phase phase, VertexConsumer frost, VertexConsumer ice, VertexConsumer rime, VertexConsumer shade,
			VertexConsumer lux, VertexConsumer cruor, Vec3 camera, Vec3 right, Vec3 up, double time, float partial, ClientLevel level,
			List<BossGroundHazardEntity> hazards) {
		/** The liquid dark, drawn and batched before anything else so every telegraph draws after it. */
		boolean dark() {
			return phase == Phase.DARK;
		}

		boolean solid() {
			return phase == Phase.SOLID;
		}

		boolean floor() {
			return phase == Phase.FLOOR;
		}

		boolean clear() {
			return phase == Phase.CLEAR;
		}

		Vec3 local(Vec3 world) {
			return world.subtract(camera);
		}

		Vec3 local(double x, double y, double z) {
			return new Vec3(x - camera.x, y - camera.y, z - camera.z);
		}
	}

	/** One running move as the pass draws it. */
	private record Move(VelorumEntity boss, VelorumMove move, BossFootprint footprint, FootprintFrame frame, SaintMoveInputs inputs,
			float moveTime, float age, int windup, int active, int recovery, int reveal, Look look, Ground ground, int seed) {
		/** 0 at the reveal to 1 at the commit. */
		float build() {
			return windup <= reveal ? 1.0F : HemoEasing.clamp01((moveTime - reveal) / (float) (windup - reveal));
		}

		/** The ice of this move crumbles from here (in the time of ice that broke out {@code offset} ticks after the commit). */
		float crumble(float offset) {
			return active + recovery - VelorumFrostRules.CRUMBLE - 2.0F - offset;
		}

		double floorY() {
			return frame.floorY();
		}
	}

	/** When each challenger's shell closed and opened: {closed at, broke at}, NaN when not. */
	private static final Map<Player, float[]> SHELLS = new WeakHashMap<>();
	/** Each Saint's last cut through the air, by move serial and limb. */
	private static final Map<VelorumEntity, Cut> CUTS = new WeakHashMap<>();
	/** Where each Saint's staff heel last struck, by move serial. */
	private static final Map<VelorumEntity, Cut> HEELS = new WeakHashMap<>();

	private VelorumTelegraphPass() {
	}

	static void collect(ClientLevel level, PoseStack p, LuxUmbraBatch flows, Vec3 camera, Vec3 right, Vec3 up, double time,
			float partial, boolean solids) {
		AABB box = new AABB(camera, camera).inflate(RANGE);
		List<VelorumEntity> bosses = new ArrayList<>();
		for (VelorumEntity boss : level.getEntitiesOfClass(VelorumEntity.class, box)) {
			if (boss.isAlive() && !boss.isInvisible() && boss.kit() != null) bosses.add(boss);
		}
		List<BossGroundHazardEntity> hazards = new ArrayList<>();
		for (BossGroundHazardEntity hazard : level.getEntitiesOfClass(BossGroundHazardEntity.class, box)) {
			if (!hazard.isRemoved() && hazard.owner() instanceof VelorumEntity) hazards.add(hazard);
		}
		List<VelorumDevoteeEntity> devotees = level.getEntitiesOfClass(VelorumDevoteeEntity.class, box);
		VelorumCueLog.begin(level, time);
		if (bosses.isEmpty() && hazards.isEmpty() && devotees.isEmpty()) return;
		// the cruor's own glow: faint always, strong while an Eclipse has put his light out
		float glow = VelorumRenderTypes.GLOW_REST;
		for (VelorumEntity boss : bosses) glow = Math.max(glow, VelorumVisualRules.cruorGlow(VelorumDarkSight.eclipse(boss, partial)));
		VelorumRenderTypes.begin(time, glow);
		MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
		VertexConsumer none = FerricDuctilisGeometry.DISCARD;
		if (solids) {
			VertexConsumer ice = buffers.getBuffer(VelorumRenderTypes.ICE);
			run(new Scene(p, Phase.SOLID, none, ice, none, none, none, none, camera, right, up, time, partial, level, hazards), bosses,
					hazards, devotees);
			buffers.endBatch(VelorumRenderTypes.ICE);
			return;
		}
		VelorumFrostGeometry.begin();
		VertexConsumer rime = flows.vertices(LuxUmbraBatch.Material.RIME), shade = flows.vertices(LuxUmbraBatch.Material.UMBRA);
		VertexConsumer lux = flows.vertices(LuxUmbraBatch.Material.LUX), cruor = flows.vertices(LuxUmbraBatch.Material.CRUOR);
		// the dark first, in a batch of its own, so every telegraph draws after it and none is hidden by it
		VertexConsumer dark = buffers.getBuffer(VelorumRenderTypes.FROST);
		run(new Scene(p, Phase.DARK, dark, none, rime, shade, lux, cruor, camera, right, up, time, partial, level, hazards), bosses,
				hazards, devotees);
		buffers.endBatch(VelorumRenderTypes.FROST);
		VertexConsumer frost = buffers.getBuffer(VelorumRenderTypes.FROST);
		run(new Scene(p, Phase.FLOOR, frost, none, rime, shade, lux, cruor, camera, right, up, time, partial, level, hazards), bosses,
				hazards, devotees);
		buffers.endBatch(VelorumRenderTypes.FROST);
		VertexConsumer clear = buffers.getBuffer(VelorumRenderTypes.ICE_CLEAR);
		run(new Scene(p, Phase.CLEAR, none, clear, none, none, none, none, camera, right, up, time, partial, level, hazards), bosses,
				hazards, devotees);
		buffers.endBatch(VelorumRenderTypes.ICE_CLEAR);
	}

	private static void run(Scene s, List<VelorumEntity> bosses, List<BossGroundHazardEntity> hazards,
			List<VelorumDevoteeEntity> devotees) {
		if (s.dark()) {
			for (VelorumEntity boss : bosses) dark(s, boss, hazards);
			return;
		}
		for (VelorumEntity boss : bosses) {
			float rise = boss.riseTime(s.partial());
			if (rise >= 0.0F && rise < SaintRise.TICKS) continue;
			if (boss.syncedEndingTick() >= 0) {
				ending(s, boss);
			} else {
				move(s, boss);
				opening(s, boss);
				presence(s, boss);
			}
			impacts(s, boss);
			held(s, boss);
		}
		for (BossGroundHazardEntity hazard : hazards) hazard(s, hazard);
		for (VelorumDevoteeEntity devotee : devotees) devotee(s, devotee);
		cues(s);
		lodged(s);
	}

	// ── moves ────────────────────────────────────────────────────────

	private static void move(Scene s, VelorumEntity boss) {
		SaintMoveView view = boss.view();
		VelorumMove move = VelorumMove.byId(view.moveId());
		if (move == null) return;
		float moveTime = view.moveTime(s.partial());
		if (moveTime < 0.0F) return;
		SaintMoveInputs inputs = view.inputs();
		BossMoveTimeline timeline = SaintControllerRules.effective(move.kind(), move.timeline(inputs));
		TelegraphSpec spec = move.telegraph(inputs);
		int reveal = SaintControllerRules.revealTick(spec == null ? TelegraphSpec.DEFAULT : spec, timeline, move.kind());
		int windup = timeline.commitTick(), active = timeline.active();
		if (moveTime > windup + active + timeline.recovery() + VelorumFrostRules.SLASH_HOLD) return;
		FootprintFrame frame = view.frame();
		if (frame == null) {
			frame = FootprintFrame.simple(boss.syncedMoveOrigin(), boss.syncedMoveAim(), boss.syncedMoveAim(), boss.getY());
		}
		Look look = VelorumFrostRules.look(moveTime, reveal, windup, active, timeline.recovery());
		Ground ground = Ground.of(frame.origin(), Shapes.forward(frame), frame.floorY(), s.camera());
		Move m = new Move(boss, move, move.footprint(inputs), frame, inputs, moveTime, moveTime - windup, windup, active,
				timeline.recovery(), reveal, look, ground, boss.getId() * 31 + move.id() * 7 + boss.syncedMoveSerial() * 131);
		switch (move) {
			case STAFF_TOLL -> toll(s, m);
			case LANTERN_SWEEP -> sweep(s, m);
			case HANGING_LANTERN -> hangingLantern(s, m);
			case CRUOR_RELEASE -> blackIce(s, m);
			case STILL_FLOOR -> stillFloor(s, m);
			case BID_TO_KNEEL -> gaze(s, m);
			case LONG_NIGHT -> longNight(s, m);
			case SET_IN_PLACE -> setInPlace(s, m);
			case RECALL -> recall(s, m);
			case CALL_TO_PRAYER -> callToPrayer(s, m);
			case PROCESSION -> procession(s, m);
			case SPLINTER_CROWN -> splinterCrown(s, m);
			case ENDLESS_HOUR -> endlessHour(s, m);
			case HOLLOW_TOLL -> hollowToll(s, m);
			case SILENCE_DRAW -> silenceDraw(s, m);
			case ARRESTED_PULSE -> pulse(s, m);
			case CRUOR_JAVELINS -> javelins(s, m);
			case VEINS_OF_FROST -> veins(s, m);
			case UMBRAL_STEP -> step(s, m);
			case GLOAM_CUTS -> gloamCuts(s, m);
			case ECLIPSE -> eclipse(s, m);
			default -> {
			}
		}
	}

	/**
	 * Staff Toll: rime spreads across the cone from his hem and seams fan out through it. At the commit the staff's
	 * heel strikes: a crystal cluster bursts up where it struck, the cold races out across the cone heaving the floor
	 * into leaning blades, a white frost shock runs over it, and the lantern's fall is cut through the air behind it.
	 */
	private static void toll(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Cone cone)) return;
		double from = cone.offsetDegrees() - cone.degrees() / 2.0D, to = from + cone.degrees();
		Look look = m.look();
		if (s.floor()) {
			rimeFan(s, m.ground(), Math.max(0.3D, cone.inner()), cone.radius(), from, to, look, 1.0F, m.seed());
			VelorumFrostGeometry.seams(s.p(), s.frost(), m.ground(), VelorumFrostRules.cone(cone.inner(), cone.radius(), from, to,
					m.seed()), look, 1.0F, m.seed());
			welling(s, m, m.ground(), Math.max(0.3D, cone.inner()), cone.radius(), from, to, m.seed() + 90);
		}
		slash(s, m, Limb.STAFF, 4.0F, 2.0F, 1.0D);
		if (m.age() < 0.0F) return;
		Vec3 heel = struck(s, m);
		if (s.floor()) frostShock(s, m.ground(), heel, m.age(), cone.radius() + 0.6D, m.seed() + 3);
		if (!s.solid()) return;
		VelorumIceGeometry.cluster(s.p(), s.ice(), s.local(heel), VelorumFrostRules.cluster(m.seed() + 5, 0.95D, 6, 0.85D, 6),
				m.age(), m.crumble(0.0F), VelorumIceGeometry.CRYSTAL, VelorumIceGeometry.SHARD, true, m.seed() + 5);
		VelorumIceGeometry.chips(s.p(), s.ice(), s.local(heel), m.age(), 1.4D, 6, VelorumIceGeometry.CRYSTAL, m.seed() + 7);
		double middle = (from + to) / 2.0D;
		for (int k = 0; k < 3; k++) {
			double r = 1.7D + k * 0.85D;
			for (int j = -1; j <= 1; j++) {
				double a = Math.toRadians(middle + j * cone.degrees() * 0.3D + (VelorumFrostRules.unit(m.seed(), 40 + k * 3 + j) - 0.5D) * 8.0D);
				Vec3 foot = world(m.ground(), Math.cos(a) * r, Math.sin(a) * r, m.floorY());
				Vec3 out = m.ground().direction(Math.cos(a), Math.sin(a));
				float offset = (float) (r * 0.5D);
				int seed = m.seed() + 60 + k * 11 + j * 3;
				List<Shard> blades = VelorumFrostRules.leaned(VelorumFrostRules.cluster(seed, 0.55D + 0.15D * k, 3, 0.5D, 4), out.x, out.z,
						0.32D);
				VelorumIceGeometry.cluster(s.p(), s.ice(), s.local(foot), blades, m.age() - offset, m.crumble(offset),
						VelorumIceGeometry.PLATE, VelorumIceGeometry.SHARD, false, seed);
			}
		}
	}

	/**
	 * Lantern Sweep (focal): faint rime over the whole arc through the wind-up with its outer edge freezing; as the band
	 * sweeps, the floor behind it freezes hard and rime blades heave up leaning with the sweep, while the lantern's real
	 * path is cut through the air (shadow shell, frost blade, cold core, twisting sinews, the lantern's knot).
	 */
	private static void sweep(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Sweep sweep)) return;
		double half = sweep.degrees() / 2.0D;
		Look look = m.look();
		if (s.floor()) {
			rimeFan(s, m.ground(), sweep.inner(), sweep.radius(), -half, half, look.scaled(0.45F), 1.0F, m.seed());
			VelorumFrostGeometry.seams(s.p(), s.frost(), m.ground(), VelorumFrostRules.arc(sweep.radius(), -half, half,
					VelorumFrostRules.EDGE, 0.0D, 1.0D, m.seed()), look, 1.0F, m.seed());
			VelorumFrostGeometry.seams(s.p(), s.frost(), m.ground(), VelorumFrostRules.arc(sweep.inner(), -half, half,
					VelorumFrostRules.BRANCH, 0.0D, 1.0D, m.seed() + 1), look, 0.7F, m.seed() + 1);
			if (m.age() < 0.0F) {
				// the dark the lantern leaves behind gathers at his heel as it is drawn back
				VelorumDarkGeometry.disc(s, m.boss().getPosition(s.partial()), m.floorY(), 0.4D + 1.2D * m.build(), 0.85F * look.alpha(),
						Light.NONE, m.seed() + 5);
			}
			if (m.age() >= 0.0F) {
				double start = sweep.angle(0.0D), now = sweep.angle(m.age());
				double lo = Math.min(start, now), hi = Math.max(start, now);
				if (hi - lo > 0.5D) {
					VelorumFrostGeometry.annulus(s.p(), s.frost(), m.ground(), VelorumFrostGeometry.RIME, sweep.inner(), sweep.radius(), lo,
							hi, Field.of(1.0F), look.ice() * 0.8F, look.glint(), Field.of(look.alpha()), m.seed() + 2);
					VelorumCruorGeometry.seams(s, m.ground(), sweep.inner(), sweep.radius(), lo, hi, Field.of(1.0F), 1.0F, look.alpha(),
							m.seed() + 3);
				}
			}
		}
		slash(s, m, Limb.STAFF, 4.0F, sweep.sweepTicks() + 1.0F, 1.4D);
		dragged(s, m, Limb.STAFF, sweep.sweepTicks() + 1.0F);
		if (m.age() < 0.0F || !s.solid()) return;
		int sites = 9;
		double sign = sweep.clockwise() ? 1.0D : -1.0D;
		for (int i = 0; i < sites; i++) {
			double t = (i + 0.5D) / sites;
			double bearing = Math.toRadians(-sign * half + sign * t * sweep.degrees());
			float passAt = (float) (t * sweep.sweepTicks());
			Vec3 tangent = m.ground().direction(-Math.sin(bearing) * sign, Math.cos(bearing) * sign);
			for (int k = 0; k < 2; k++) {
				int seed = m.seed() + 80 + i * 5 + k;
				double r = sweep.inner() + 1.2D + k * 1.6D + VelorumFrostRules.unit(seed, 1) * 0.4D;
				Vec3 foot = world(m.ground(), Math.cos(bearing) * r, Math.sin(bearing) * r, m.floorY());
				float offset = passAt + k * 0.4F;
				List<Shard> blades = VelorumFrostRules.leaned(VelorumFrostRules.cluster(seed, 0.6D + 0.2D * k, 3, 0.55D, 5), tangent.x,
						tangent.z, 0.3D);
				VelorumIceGeometry.cluster(s.p(), s.ice(), s.local(foot), blades, m.age() - offset, m.crumble(offset),
						VelorumIceGeometry.CRYSTAL, VelorumIceGeometry.SHARD, false, seed);
				if ((i + k) % 2 == 0) VelorumIceGeometry.chips(s.p(), s.ice(), s.local(foot), m.age() - offset, 1.0D, 3,
						VelorumIceGeometry.CRYSTAL, seed + 3);
			}
		}
	}

	/** The Hanging Lantern: rime and seams open behind him; the side lantern's swing is cut through the air and an arc of rime blades heaves up across his back. */
	private static void hangingLantern(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Cone cone)) return;
		double from = cone.offsetDegrees() - cone.degrees() / 2.0D, to = from + cone.degrees();
		Look look = m.look();
		if (s.floor()) {
			rimeFan(s, m.ground(), Math.max(0.3D, cone.inner()), cone.radius(), from, to, look, 1.0F, m.seed());
			VelorumFrostGeometry.seams(s.p(), s.frost(), m.ground(), VelorumFrostRules.cone(cone.inner(), cone.radius(), from, to,
					m.seed()), look, 1.0F, m.seed());
			welling(s, m, m.ground(), Math.max(0.3D, cone.inner()), cone.radius(), from, to, m.seed() + 90);
		}
		slash(s, m, Limb.SIDE, 3.0F, m.active(), 0.8D);
		if (m.age() < 0.0F || !s.solid()) return;
		for (int i = 0; i < 7; i++) {
			double bearing = Math.toRadians(from + (to - from) * (i + 0.5D) / 7.0D);
			int seed = m.seed() + 40 + i;
			double r = 2.0D + VelorumFrostRules.unit(seed, 1) * 0.6D;
			Vec3 foot = world(m.ground(), Math.cos(bearing) * r, Math.sin(bearing) * r, m.floorY());
			Vec3 out = m.ground().direction(Math.cos(bearing), Math.sin(bearing));
			float offset = i * 0.5F;
			VelorumIceGeometry.cluster(s.p(), s.ice(), s.local(foot), VelorumFrostRules.leaned(VelorumFrostRules.cluster(seed, 0.6D, 3, 0.6D, 5),
					out.x, out.z, 0.28D), m.age() - offset, m.crumble(offset), VelorumIceGeometry.CRYSTAL, VelorumIceGeometry.SHARD, false, seed);
		}
	}

	/**
	 * Black-Ice Splinters, the trench: a knot of dark seams gathers at his hem while each of the five lanes sinks into a
	 * trench running out from his feet, its lips freezing and feathering with hoarfrost, its far wall and its empty bed of
	 * shadow showing under the lip. At the commit the front runs out at a block a tick; behind it black ice wells up the
	 * trench until it lies glossy just under the floor, and leaning black-ice blades break out of it. The standing black
	 * ice takes over the moment the hazard stands (see {@link #standingIce}).
	 */
	private static void blackIce(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Lane lane)) return;
		Look look = m.look();
		boolean handed = m.age() >= 0.0F && handedOver(s, m);
		if (s.floor()) {
			Look knot = look.withIce(look.ice() * 0.4F);
			VelorumFrostGeometry.seams(s.p(), s.frost(), m.ground(), VelorumFrostRules.star(1.4D, 9, m.seed()), knot, 1.0F, m.seed());
			VelorumCruorGeometry.seams(s, m.ground(), 0.0D, 1.8D, -180.0D, 180.0D, Field.of(VelorumCruorGeometry.welled(m.build())),
					m.age() < 0.0F ? 0.2F : 1.0F, look.alpha(), m.seed() + 2);
		}
		slash(s, m, Limb.CLAW, 4.0F, 2.0F, 0.7D);
		if (handed) return;
		double half = lane.halfWidth();
		for (int n = 0; n < lane.lanes(); n++) {
			int index = lane.index(n);
			Ground g = m.ground().lane(lane.offset(index), lane.angle(index));
			// seeded by lane only, as the standing ice is, so nothing changes when it takes over
			int seed = 7919 + index * 131;
			if (m.age() < 0.0F) {
				if (!s.floor()) continue;
				double reach = 0.6D + (lane.length() - 0.6D) * look.grow();
				double depth = VelorumFrostRules.TRENCH_DEPTH * HemoEasing.smoothstep(HemoEasing.clamp01(m.build() * 1.4F));
				lips(s, g, 0.6D, lane.length(), half, look, seed);
				VelorumFrostGeometry.trench(s.p(), s.frost(), g, 0.6D, reach, half, depth, true, true, look.frost() * 0.15F, 0.0F,
						look.glint(), look.alpha(), seed + 1);
				continue;
			}
			double speed = Math.max(0.01D, lane.frontSpeed());
			double front = Math.max(0.6D, lane.front(m.age(), index));
			if (s.floor()) {
				lips(s, g, 0.6D, lane.length(), half, look, seed);
				filling(s, g, lane.length(), half, (m.age() - lane.delay(index)) * speed, 0.0D, 0.06F, look.glint(), look.alpha(), seed);
				forming(s, g, lane, index, speed, m.floorY(), m.age());
				if (front < lane.length() - 0.1D) {
					// the front: a cold flash running up the trench, frost spray lifting off it
					Vec3 at = s.local(world(g, front, 0.0D, m.floorY() + 0.25D));
					LuxUmbraGeometry.card(s.p(), s.lux(), at, s.right(), s.up(), 0.55D, 0.4D, LuxUmbraGeometry.FOCUS, seed + 4, COLD, 0.7F);
					LuxUmbraGeometry.card(s.p(), s.rime(), at.add(0.0D, 0.35D, 0.0D), s.right(), s.up(), 0.5D, 0.45D, LuxUmbraGeometry.WISP,
							seed + 5, WHITE, 0.45F);
				}
			}
			if (s.solid()) splinters(s, g, lane, index, speed, m.floorY(), m.age(), NEVER);
		}
	}

	/**
	 * One lane's trench as its black ice wells up: behind the front ({@code frontRaw} blocks out, unclamped, so the last
	 * stretch fills after the front has reached the end) it lies filled just under the floor; over the WELL blocks behind
	 * the front it is welling, half full and lit by the front's cold; ahead of the front it is still the empty trench.
	 * {@code close} heals the floor over the filled part as the standing ice ends.
	 */
	private static void filling(Scene s, Ground g, double length, double half, double frontRaw, double close, float frost, float glint,
			float alpha, int seed) {
		double front = Math.min(length, Math.max(0.6D, frontRaw));
		double well = Math.min(length, Math.max(0.6D, frontRaw - VelorumFrostRules.WELL));
		double middle = HemoEasing.lerp(VelorumFrostRules.TRENCH_DEPTH, VelorumFrostRules.TRENCH_FILLED, 0.5D);
		boolean wellDone = well >= length - 1.0E-3D, frontDone = front >= length - 1.0E-3D;
		if (well > 0.6D) {
			VelorumFrostGeometry.trench(s.p(), s.frost(), g, 0.6D, well, half, VelorumFrostRules.TRENCH_FILLED * (1.0D - close), true, wellDone,
					frost, 1.0F, glint * 0.4F, alpha, seed + 1);
		}
		if (front > well) {
			VelorumFrostGeometry.trench(s.p(), s.frost(), g, well, front, half, middle, well <= 0.6D, frontDone, 0.2F, 0.6F, 1.0F, alpha,
					seed + 2);
		}
		if (front < length) {
			VelorumFrostGeometry.trench(s.p(), s.frost(), g, front, length, half, VelorumFrostRules.TRENCH_DEPTH, front <= 0.6D, true, 0.15F,
					0.0F, glint, alpha, seed + 3);
		}
	}

	/** Whether the standing black ice of this cast has come to stand, so it draws the trench from here on. */
	private static boolean handedOver(Scene s, Move m) {
		for (BossGroundHazardEntity hazard : s.hazards()) {
			if (hazard.owner() != m.boss() || !VelorumDarkSight.field(hazard)) continue;
			if (Shapes.planar(hazard.frame().origin(), m.frame().origin()) < 0.75D && hazard.tickCount < 40) return true;
		}
		return false;
	}

	/** How far the rime feathers out over the floor beside a lane's lip: narrow, so the five lanes stay five lanes. */
	private static final double LIP_RIME = 0.3D;

	/** One lane's lips: seams freezing outward along both edges, rime feathering over the floor beside them. */
	private static void lips(Scene s, Ground g, double from, double length, double half, Look look, int seed) {
		VelorumFrostGeometry.seams(s.p(), s.frost(), g, VelorumFrostRules.lips(from, length, half, seed), look, 1.0F, seed);
		double reach = from + (length - from) * look.grow();
		float frost = look.frost();
		Field beside = (along, right) -> frost * (float) HemoEasing.clamp01(1.0D - (Math.abs(right) - half) / LIP_RIME);
		Field alpha = Field.of(look.alpha());
		VelorumFrostGeometry.rect(s.p(), s.frost(), g, VelorumFrostGeometry.RIME, from, reach, half, half + LIP_RIME, beside,
				look.ice() * 0.4F, look.glint(), alpha, seed + 7);
		VelorumFrostGeometry.rect(s.p(), s.frost(), g, VelorumFrostGeometry.RIME, from, reach, -half - LIP_RIME, -half, beside,
				look.ice() * 0.4F, look.glint(), alpha, seed + 8);
	}

	/**
	 * The black-ice blades of one lane, a cluster every {@link #SITE} blocks, each breaking out as the front passes it
	 * ({@code moveAge} is the move's own age past its commit) and leaning on along the lane; they crumble from
	 * {@code crumbleAt} in the move's time, the far ones a little later. Sites are seeded by lane and place only, so the
	 * standing hazard draws the very same blades the move did.
	 */
	private static void splinters(Scene s, Ground g, BossFootprint.Lane lane, int index, double speed, double floorY, float moveAge,
			float crumbleAt) {
		double half = lane.halfWidth();
		for (int k = 0; ; k++) {
			double along = 0.9D + k * SITE;
			if (along > lane.length() - 0.2D) break;
			float erupt = (float) (lane.delay(index) + along / speed);
			float own = moveAge - erupt;
			if (own <= 0.0F) break;
			int seed = 7919 + index * 131 + k * 17;
			Vec3 foot = world(g, along, (VelorumFrostRules.unit(seed, 1) - 0.5D) * half * 0.7D, floorY - 0.02D);
			double scale = 1.1D + 0.5D * VelorumFrostRules.unit(seed, 2);
			List<Shard> blades = VelorumFrostRules.leaned(VelorumFrostRules.cluster(seed, scale, 3, 0.42D, 4), g.fx(), g.fz(), 0.3D);
			float crumble = crumbleAt >= NEVER ? NEVER : crumbleAt + erupt * 0.25F - erupt;
			VelorumIceGeometry.cluster(s.p(), s.ice(), s.local(foot), blades, own, crumble, VelorumIceGeometry.BLACK_ICE,
					VelorumIceGeometry.SHARD, true, seed);
			VelorumIceGeometry.chips(s.p(), s.ice(), s.local(foot), own, 1.1D, 3, VelorumIceGeometry.BLACK_ICE, seed + 5);
		}
	}

	/**
	 * Each blade of frozen blood forming before it breaks out, as a Cruor Javelin's shaft does: three frost strands twisting
	 * up together round a spine of clots over the three ticks before the front reaches its site.
	 */
	private static void forming(Scene s, Ground g, BossFootprint.Lane lane, int index, double speed, double floorY, float moveAge) {
		double half = lane.halfWidth();
		for (int k = 0; ; k++) {
			double along = 0.9D + k * SITE;
			if (along > lane.length() - 0.2D) break;
			float erupt = (float) (lane.delay(index) + along / speed);
			float own = moveAge - erupt;
			if (own > 0.5F) continue;
			if (own < -3.0F) break;
			int seed = 7919 + index * 131 + k * 17;
			float form = HemoEasing.clamp01((own + 3.0F) / 3.0F);
			Vec3 foot = world(g, along, (VelorumFrostRules.unit(seed, 1) - 0.5D) * half * 0.7D, floorY);
			for (int strand = 0; strand < 3; strand++) {
				List<Vec3> points = new ArrayList<>(6);
				for (int i = 0; i <= 5; i++) {
					double t = i / 5.0D;
					double a = t * 3.5D + strand * Math.PI * 2.0D / 3.0D + s.time() * 0.06D;
					double r = 0.16D * (1.0D - t * 0.75D) * (1.2D - form * 0.6D);
					points.add(s.local(foot.add(Math.cos(a) * r, t * 1.2D * form, Math.sin(a) * r)));
				}
				LuxUmbraGeometry.ribbonPath(s.p(), s.cruor(), points, Vec3.ZERO, 0.04D, 0.01D, ThermalGeometry.FROST_STRAND, seed + strand, WHITE,
						0.9F * form);
			}
		}
	}

	/**
	 * The Still Floor: rime spreads ring by ring from the bare eye round him out to ten blocks through the wind-up, sixteen
	 * seams running out through it, the eye's edge a dark frozen ring and the reach a faint one; black ice films between
	 * the seams late in the wind-up. At the commit the whole ring flashes cold white and the seams freeze shut, three
	 * concentric rows of ice plates heave up rippling out from the eye, and a frost shock runs to the rim.
	 */
	private static void stillFloor(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Annulus ring)) return;
		Look look = m.look();
		double eye = ring.inner(), outer = ring.outer();
		Vec3 centre = new Vec3(m.frame().origin().x, m.floorY(), m.frame().origin().z);
		if (s.floor()) {
			double front = eye + (outer - eye + 0.8D) * look.grow();
			float frost = look.frost();
			VelorumFrostGeometry.annulus(s.p(), s.frost(), m.ground(), VelorumFrostGeometry.RIME, eye, outer, -180.0D, 180.0D,
					(along, right) -> frost * VelorumFrostRules.reach(Math.hypot(along, right), front, 1.2D), look.ice(), look.glint(),
					Field.of(look.alpha()), m.seed());
			VelorumFrostGeometry.seams(s.p(), s.frost(), m.ground(), VelorumFrostRules.radial(eye, outer, 16, m.seed() % 7, m.seed()),
					look, 1.0F, m.seed());
			float well = m.age() < 0.0F ? VelorumCruorGeometry.welled(m.build()) : 1.0F;
			double blood = eye + (outer - eye + 0.8D) * well;
			VelorumCruorGeometry.seams(s, m.ground(), eye, outer, -180.0D, 180.0D,
					(along, right) -> VelorumFrostRules.reach(Math.hypot(along, right), blood, 1.4D), m.age() < 0.0F ? 0.3F * m.build() : 1.0F,
					look.alpha(), m.seed() + 7);
			Look rim = look.withGrow(1.0F).withWidth(look.width() * 1.3F).withIce(0.0F);
			VelorumFrostGeometry.seams(s.p(), s.frost(), m.ground(), VelorumFrostRules.ring(eye, m.seed() + 1), rim, 1.0F, m.seed() + 1);
			VelorumFrostGeometry.seams(s.p(), s.frost(), m.ground(), VelorumFrostRules.ring(outer, m.seed() + 2), rim, 0.45F, m.seed() + 2);
			if (m.age() >= 0.0F) frostShock(s, m.ground(), centre, m.age(), outer, m.seed() + 3);
		}
		if (m.age() < 0.0F || !s.solid()) return;
		double[] radii = {4.0D, 6.5D, 9.0D};
		for (int k = 0; k < radii.length; k++) {
			double r = radii[k];
			int count = (int) Math.round(Math.PI * 2.0D * r / 2.1D);
			float offset = (float) ((r - eye) * 0.25D);
			for (int i = 0; i < count; i++) {
				int seed = m.seed() + 200 + k * 97 + i;
				double a = (i + VelorumFrostRules.unit(seed, 1) * 0.4D) / count * Math.PI * 2.0D + k * 0.3D;
				Vec3 foot = world(m.ground(), Math.cos(a) * r, Math.sin(a) * r, m.floorY());
				Vec3 out = m.ground().direction(Math.cos(a), Math.sin(a));
				List<Shard> plate = VelorumFrostRules.leaned(VelorumFrostRules.cluster(seed, 0.8D + 0.4D * VelorumFrostRules.unit(seed, 2),
						i % 2 == 0 ? 3 : 2, 0.45D, 4), out.x, out.z, 0.35D);
				VelorumIceGeometry.cluster(s.p(), s.ice(), s.local(foot), plate, m.age() - offset, m.crumble(offset), VelorumIceGeometry.PLATE,
						VelorumIceGeometry.SHARD, false, seed);
			}
		}
	}

	/**
	 * Bid to Kneel: his raised lantern's cold light laid on the floor in its cone, each ray stopped where a pillar or a
	 * figure blocks it (so the cover reads), darkness closing in at both sides of it, a faint shaft of light from the
	 * lantern and motes drawn in toward it; through the hold, rime climbs the shins of whoever stands in it.
	 */
	private static void gaze(Scene s, Move m) {
		BossFootprint shape = m.footprint() instanceof BossFootprint.Occluded occluded ? occluded.shape() : m.footprint();
		if (!(shape instanceof BossFootprint.Cone cone)) return;
		Look look = m.look();
		if (!look.visible()) return;
		float light = look.alpha() * (0.45F + 0.55F * look.grow());
		float hold = m.age() < 0.0F ? 0.0F : HemoEasing.clamp01(m.age() / Math.max(1.0F, m.active()));
		Vec3 lantern = anchor(m.boss(), s.partial(), Limb.STAFF);
		double from = cone.offsetDegrees() - cone.degrees() / 2.0D, to = from + cone.degrees();
		if (s.floor()) {
			int rays = 17;
			double[] reach = new double[rays];
			for (int i = 0; i < rays; i++) {
				double bearing = Math.toRadians(from + (to - from) * i / (rays - 1));
				Vec3 end = world(m.ground(), Math.cos(bearing) * cone.radius(), Math.sin(bearing) * cone.radius(), m.floorY() + 1.0D);
				BlockHitResult hit = s.level().clip(new ClipContext(lantern, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
						m.boss()));
				reach[i] = hit.getType() == HitResult.Type.MISS ? cone.radius()
						: Math.min(cone.radius(), Shapes.planar(m.frame().origin(), hit.getLocation()) + 0.3D);
			}
			VelorumFrostGeometry.light(s.p(), s.frost(), m.ground(), 0.6D, reach, from, to, cone.radius(), look.glint(), light, m.seed());
			// the dark closing in at both sides of the light
			for (int side = -1; side <= 1; side += 2) {
				double edge = side < 0 ? from : to, beyond = edge + side * 24.0D;
				double lo = Math.min(edge, beyond), hi = Math.max(edge, beyond);
				Field gloom = (along, right) -> {
					double bearing = Math.toDegrees(Math.atan2(right, along));
					double into = 1.0D - Math.abs(bearing - edge) / 24.0D;
					return light * (float) HemoEasing.clamp01(0.35D + 0.75D * into);
				};
				VelorumFrostGeometry.annulus(s.p(), s.frost(), m.ground(), VelorumFrostGeometry.DARK, 0.8D, cone.radius() * 0.75D, lo, hi,
						Field.of(1.0F), 0.0F, 0.0F, gloom, m.seed() + 10 + side);
			}
			// the shaft of cold light from the lantern down the cone, and motes drawn in along it
			// down the middle of the cone (its offset, not the frame's forward, so the shaft lands in the light)
			double middle = Math.toRadians(cone.offsetDegrees());
			Vec3 down = world(m.ground(), Math.cos(middle) * cone.radius() * 0.45D, Math.sin(middle) * cone.radius() * 0.45D,
					m.floorY() + 0.4D);
			LuxUmbraGeometry.ribbonPath(s.p(), s.lux(), List.of(s.local(lantern), s.local(lantern.lerp(down, 0.5D)), s.local(down)), Vec3.ZERO,
					0.08D, 0.9D, LuxUmbraGeometry.RIBBON, m.seed() + 20, COLD, 0.22F * light);
			for (int i = 0; i < 12; i++) {
				double phase = (s.time() * 0.012D + i / 12.0D) % 1.0D;
				double bearing = Math.toRadians(from + (to - from) * VelorumFrostRules.unit(m.seed(), 30 + i));
				double r = cone.radius() * 0.8D * (1.0D - phase);
				Vec3 floorPoint = world(m.ground(), Math.cos(bearing) * r, Math.sin(bearing) * r, m.floorY() + 0.3D);
				Vec3 at = floorPoint.lerp(lantern, phase * phase);
				float fade = light * (float) Math.sin(phase * Math.PI);
				LuxUmbraGeometry.card(s.p(), s.lux(), s.local(at), s.right(), s.up(), 0.06D, 0.06D, LuxUmbraGeometry.FOCUS, m.seed() + 40 + i,
						CORE, 0.8F * fade);
			}
		}
		if (hold <= 0.0F || m.age() > m.active() + 6.0F) return;
		for (Player player : s.level().players()) {
			if (player.isSpectator() || !player.isAlive()) continue;
			Vec3 feet = player.getPosition(s.partial());
			double distance = Shapes.planar(m.frame().origin(), feet);
			if (distance > cone.radius() || distance < 0.5D) continue;
			double[] local = m.ground().local(feet.x, feet.z);
			if (Math.abs(Math.toDegrees(Math.atan2(local[1], local[0])) - cone.offsetDegrees()) > cone.degrees() / 2.0D) continue;
			if (!m.boss().hasLineOfSight(player)) continue;
			int seed = player.getId() * 13;
			if (s.floor()) {
				Ground at = m.ground().at(feet);
				VelorumFrostGeometry.annulus(s.p(), s.frost(), at, VelorumFrostGeometry.RIME, 0.0D, 0.95D, -180.0D, 180.0D,
						Field.of(0.4F + 0.6F * hold), 0.2F, 0.2F, Field.of(look.alpha()), seed);
			} else if (s.solid()) {
				rimeAtShins(s, feet, 0.38D, 0.25D + 0.45D * hold, 1.0F, VelorumIceGeometry.CRYSTAL, seed);
			}
		}
	}

	/**
	 * The Long Night: a pool of shadow spreads under him while eight narrow folds of darkness rise round his lantern,
	 * each two leaves of pleated dark with a cold hairline down its crests; at the commit they sink into the floor and
	 * the dark floods out across it, a band of gloom with a frost crest running to sixteen blocks.
	 */
	private static void longNight(Scene s, Move m) {
		if (!s.floor()) return;
		Vec3 feet = m.boss().getPosition(s.partial());
		float rise = VelorumFrostRules.foldRise(m.moveTime(), m.windup());
		float sink = VelorumFrostRules.foldSink(m.age());
		float leave = m.age() <= 0.0F ? 0.0F : HemoEasing.clamp01(m.age() / (VelorumFrostRules.FOLD_SINK + 6.0F));
		Vec3 base = new Vec3(feet.x, m.floorY(), feet.z);
		VelorumDarkGeometry.disc(s, base, m.floorY(), 1.5D + 5.5D * rise + 4.0D * sink, 0.9F * rise * (1.0F - leave),
				Light.of(m.boss(), s.partial()), m.seed());
		if (sink < 1.0F) {
			for (int i = 0; i < 8; i++) {
				int seed = m.seed() + 10 + i;
				double a = i * Math.PI / 4.0D + m.seed() * 0.1D + Math.sin(s.time() * 0.02D + i) * 0.05D;
				double r = 1.6D + 0.5D * VelorumFrostRules.unit(seed, 1) + sink * 1.2D;
				double height = (2.2D + 1.6D * VelorumFrostRules.unit(seed, 2)) * rise * (1.0D - sink);
				double width = 0.6D + 0.35D * VelorumFrostRules.unit(seed, 3);
				if (height < 0.05D) continue;
				Vec3 out = new Vec3(Math.cos(a), 0.0D, Math.sin(a));
				Vec3 root = base.add(out.scale(r));
				for (int leaf = 0; leaf < 2; leaf++) {
					double turn = a + Math.PI / 2.0D + (leaf == 0 ? -0.35D : 0.35D);
					Vec3 across = new Vec3(Math.cos(turn), 0.0D, Math.sin(turn)).scale(width);
					Vec3 lean = out.scale(0.25D * height / 3.0D).add(0.0D, height, 0.0D);
					Vec3 a0 = s.local(root.subtract(across)), b0 = s.local(root.add(across));
					VelorumFrostGeometry.card(s.p(), s.frost(), a0, b0, b0.add(lean), a0.add(lean), VelorumFrostGeometry.FOLD,
							VelorumFrostGeometry.state(0.8F, 0.0F, 0.0F), 0.92F * (1.0F - sink), seed + leaf * 3);
				}
			}
		}
		if (runsToPools(s, m, base)) return;
		double wave = VelorumFrostRules.darkWave(m.age(), 16.0D);
		if (wave <= 0.0D) return;
		float fade = 1.0F - m.age() / VelorumFrostRules.DARK_WAVE;
		Ground g = new Ground(base.x, base.z, 1.0D, 0.0D, m.floorY(), s.camera());
		Field band = (along, right) -> {
			double behind = wave - Math.hypot(along, right);
			return fade * 0.85F * (float) HemoEasing.clamp01(1.0D - Math.abs(behind - 0.6D) / 1.6D);
		};
		VelorumFrostGeometry.annulus(s.p(), s.frost(), g, VelorumFrostGeometry.GLOOM, Math.max(0.0D, wave - 2.4D), wave, -180.0D, 180.0D,
				Field.of(0.5F), 0.0F, 0.0F, band, m.seed() + 30);
		VelorumFrostGeometry.seams(s.p(), s.frost(), g, VelorumFrostRules.ring(wave, m.seed() + 31),
				new Look(1.0F, 0.8F, 0.0F, 0.15F, 1.1F, fade), 1.0F, m.seed() + 31);
	}

	/**
	 * Set in Place: rime gathers in a disc under the held figure while five icicles form in the air above it, glinting;
	 * at the commit they fall, the claw's stroke cut through the air after them, and drive into the floor round the
	 * figure, shattering into a burst of chips and leaving stubs that sublime through the recovery.
	 */
	private static void setInPlace(Scene s, Move m) {
		Vec3 target = m.frame().target();
		Vec3 foot = new Vec3(target.x, m.floorY(), target.z);
		Look look = m.look();
		Ground g = m.ground().at(foot);
		if (s.floor()) {
			rimeFan(s, g, 0.0D, 1.6D, -180.0D, 180.0D, look, 1.0F, m.seed());
			VelorumFrostGeometry.seams(s.p(), s.frost(), g, VelorumFrostRules.ring(1.2D, m.seed() + 1), look, 1.0F, m.seed() + 1);
			VelorumFrostGeometry.seams(s.p(), s.frost(), g, VelorumFrostRules.star(1.2D, 6, m.seed() + 2), look, 0.8F, m.seed() + 2);
			if (m.age() >= 0.0F) frostShock(s, g, foot, m.age() - 2.0F, 2.0D, m.seed() + 3);
			if (m.age() < 0.0F) {
				float rising = HemoEasing.ramp(m.moveTime(), m.reveal() + 4.0F, m.windup() - 2.0F);
				for (int i = 0; i < 5; i++) {
					double a = i * Math.PI * 2.0D / 5.0D + VelorumFrostRules.unit(m.seed() + 50 + i, 1);
					Vec3 at = foot.add(Math.cos(a) * 0.45D, 0.0D, Math.sin(a) * 0.45D);
					double lifted = 1.0D + 1.8D * HemoEasing.smoothstep(HemoEasing.clamp01(rising * 1.4F));
					LuxUmbraGeometry.ribbonPath(s.p(), s.cruor(), List.of(s.local(at.add(0.0D, 0.9D, 0.0D)), s.local(at.add(0.0D, lifted, 0.0D))),
							Vec3.ZERO, 0.05D, 0.02D, ThermalGeometry.FROST_STRAND, m.seed() + 70 + i, 0xFFFFFF, 0.85F * rising * (1.0F - rising));
				}
			}
		}
		slash(s, m, Limb.CLAW, 4.0F, 2.0F, 0.7D);
		if (!s.solid()) return;
		float form = HemoEasing.ramp(m.moveTime(), m.reveal() + 4.0F, m.windup() - 2.0F);
		for (int i = 0; i < 5; i++) {
			int seed = m.seed() + 50 + i;
			double a = i * Math.PI * 2.0D / 5.0D + VelorumFrostRules.unit(seed, 1);
			Vec3 at = foot.add(Math.cos(a) * 0.45D, 0.0D, Math.sin(a) * 0.45D);
			if (m.age() < 2.0F) {
				if (form <= 0.02F) continue;
				double fall = m.age() < 0.0F ? 0.0D : HemoEasing.easeInCubic(m.age() / 2.0D);
				// a dark bead of their blood rising out of the shell or the collar, stretching into an icicle of cruor
				double lifted = 1.0D + 1.8D * HemoEasing.smoothstep(HemoEasing.clamp01(form * 1.4F));
				double length = 0.15D + 1.1D * form;
				Vec3 top = at.add(0.0D, lifted - fall * (lifted - 0.25D) + length, 0.0D);
				float glint = 0.3F + 0.4F * (float) Math.sin(s.time() * 0.2D + i);
				VelorumIceGeometry.shard(s.p(), s.ice(), s.local(top), new Vec3(0.0D, -1.0D, 0.0D), length, 0.16D, 0.9D, a, 6, 1.0F,
						VelorumIceGeometry.withGlint(VelorumIceGeometry.CRYSTAL, glint), VelorumIceGeometry.SHARD, seed);
				continue;
			}
			float own = m.age() - 2.0F;
			Shard stub = new Shard(0.0D, 0.0D, 0.95D + 0.25D * VelorumFrostRules.unit(seed, 2), 0.16D, 0.9D, a, Math.cos(a) * 0.1D,
					Math.sin(a) * 0.1D, 6);
			VelorumIceGeometry.cluster(s.p(), s.ice(), s.local(at), List.of(stub), own + VelorumFrostRules.PUNCH, m.crumble(2.0F),
					VelorumIceGeometry.CRYSTAL, VelorumIceGeometry.SHARD, false, seed);
			VelorumIceGeometry.chips(s.p(), s.ice(), s.local(at), own, 1.2D, 4, VelorumIceGeometry.CRYSTAL, seed + 7);
		}
	}

	/**
	 * Recall to Stillness: five pale threads draw from the awake ally's chest to his raised hand, a bright point running
	 * in along each, and rime climbs the ally's shins through the hold.
	 */
	private static void recall(Scene s, Move m) {
		if (m.age() < 0.0F || m.age() > m.active()) return;
		VelorumDevoteeEntity devotee = recalled(s, m.boss());
		if (devotee == null) return;
		float hold = HemoEasing.clamp01(m.age() / Math.max(1.0F, m.active()));
		float ease = HemoEasing.clamp01(m.age() / 6.0F) * (1.0F - HemoEasing.ramp(m.age(), m.active() - 4.0F, m.active()));
		Vec3 feet = devotee.getPosition(s.partial());
		if (s.solid()) {
			rimeAtShins(s, feet, 0.4D, 0.25D + 0.5D * hold, 1.0F, VelorumIceGeometry.CRYSTAL, devotee.getId() * 11);
			return;
		}
		if (!s.floor()) return;
		Vec3 chest = feet.add(0.0D, devotee.getBbHeight() * 0.6D, 0.0D);
		Vec3 hand = anchor(m.boss(), s.partial(), Limb.CLAW);
		for (int i = 0; i < 5; i++) {
			Vec3 from = chest.add(Math.cos(i * 1.26D) * 0.12D, (i - 2) * 0.08D, Math.sin(i * 1.26D) * 0.12D);
			List<Vec3> thread = new ArrayList<>(9);
			for (int k = 0; k <= 8; k++) {
				double t = k / 8.0D;
				thread.add(s.local(from.lerp(hand, t).add(0.0D, -Math.sin(t * Math.PI) * 0.3D, 0.0D)));
			}
			LuxUmbraGeometry.ribbonPath(s.p(), s.rime(), thread, Vec3.ZERO, 0.014D, 0.008D, LuxUmbraGeometry.EYE, m.seed() + i, WHITE,
					0.6F * ease);
			double run = (s.time() * 0.03D + i / 5.0D) % 1.0D;
			Vec3 bead = thread.get((int) Math.min(8, Math.floor(run * 8.0D)));
			LuxUmbraGeometry.card(s.p(), s.lux(), bead, s.right(), s.up(), 0.07D, 0.07D, LuxUmbraGeometry.FOCUS, m.seed() + 10 + i, CORE,
					0.8F * ease);
		}
		VelorumFrostGeometry.annulus(s.p(), s.frost(), m.ground().at(feet), VelorumFrostGeometry.RIME, 0.0D, 1.0D, -180.0D, 180.0D,
				Field.of(0.4F + 0.6F * hold), 0.3F, 0.2F, Field.of(ease), devotee.getId());
	}

	/** The awake ally Recall holds: the nearest one within fourteen blocks. */
	private static VelorumDevoteeEntity recalled(Scene s, VelorumEntity boss) {
		VelorumDevoteeEntity best = null;
		double distance = 14.0D * 14.0D;
		for (VelorumDevoteeEntity devotee : s.level().getEntitiesOfClass(VelorumDevoteeEntity.class, boss.getBoundingBox().inflate(14.0D))) {
			if (!devotee.awake() || devotee.hostile()) continue;
			double d = devotee.distanceToSqr(boss);
			if (d < distance) {
				distance = d;
				best = devotee;
			}
		}
		return best;
	}

	/** Call to Prayer: the lantern flares as it is raised to the aisle, and at the commit two cold rings run out over the floor. */
	private static void callToPrayer(Scene s, Move m) {
		if (!s.floor()) return;
		Vec3 lantern = anchor(m.boss(), s.partial(), Limb.STAFF);
		float flare = HemoEasing.ramp(m.moveTime(), 0.0F, m.windup()) * (1.0F - HemoEasing.ramp(m.age(), 0.0F, 10.0F));
		LuxUmbraGeometry.card(s.p(), s.lux(), s.local(lantern), s.right(), s.up(), 0.35D * flare, 0.35D * flare, LuxUmbraGeometry.FOCUS,
				m.seed(), CORE, 0.8F * flare);
		Vec3 centre = new Vec3(m.frame().origin().x, m.floorY(), m.frame().origin().z);
		frostShock(s, m.ground(), centre, m.age(), 12.0D, m.seed() + 1);
		frostShock(s, m.ground(), centre, m.age() - 4.0F, 12.0D, m.seed() + 2);
	}

	/**
	 * The Procession of Shades: a road of rime is laid along the line with shadow gathering where they come in; three
	 * shades walk it, each a hooded figure of shadow with a dark wisp trailing, their bare feet leaving frost prints that
	 * fade over thirty ticks.
	 */
	private static void procession(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Path path) || !s.floor()) return;
		List<Vec3> points = m.frame().points();
		if (points.size() < 2) return;
		double length = Shapes.pathLength(points);
		if (length < 0.5D) return;
		Look look = m.look();
		Vec3 start = points.get(0), end = points.get(points.size() - 1);
		double dx = end.x - start.x, dz = end.z - start.z, span = Math.hypot(dx, dz);
		Ground g = new Ground(start.x, start.z, dx / span, dz / span, m.floorY(), s.camera());
		double half = path.halfWidth() * 1.1D;
		double laid = length * look.grow();
		float frost = look.frost();
		VelorumFrostGeometry.rect(s.p(), s.frost(), g, VelorumFrostGeometry.RIME, 0.0D, Math.max(0.01D, laid), -half - 0.3D, half + 0.3D,
				(along, right) -> frost * (float) HemoEasing.clamp01(1.0D - (Math.abs(right) - half) / 0.3D), look.ice() * 0.5F,
				look.glint(), Field.of(look.alpha()), m.seed());
		VelorumFrostGeometry.seams(s.p(), s.frost(), g, VelorumFrostRules.lips(0.0D, length, half, m.seed()), look, 0.8F, m.seed());
		if (m.age() < 0.0F) {
			VelorumDarkGeometry.disc(s, new Vec3(start.x, m.floorY(), start.z), m.floorY(), 0.6D + 1.9D * m.build(), look.alpha(), Light.NONE,
					m.seed() + 1);
		}
		if (m.age() < 0.0F) return;
		for (int i = 0; i < path.walkers(); i++) {
			double head = path.walker(m.age(), i);
			if (head < 0.0D) continue;
			double at = Math.min(length, head - path.frontLength() * 0.5D);
			if (at > 0.0D && head - path.frontLength() < length) {
				float in = (float) HemoEasing.clamp01(head / 1.5D);
				float out = (float) HemoEasing.clamp01(1.0D - (head - length) / 1.0D);
				Vec3 pos = Shapes.pointAlong(points, at);
				Vec3 foot = new Vec3(pos.x, m.floorY(), pos.z);
				shadeSmoke(s, foot, in * out, m.seed() + 20 + i);
				Vec3 behind = Shapes.pointAlong(points, Math.max(0.0D, at - 2.0D));
				LuxUmbraGeometry.ribbonPath(s.p(), s.shade(), List.of(s.local(behind.x, m.floorY() + 1.0D, behind.z),
						s.local(foot.add(0.0D, 1.0D, 0.0D))), Vec3.ZERO, 0.02D, 0.4D, LuxUmbraGeometry.WISP, m.seed() + 30 + i, DARK,
						0.45F * in * out);
			}
			// frost prints behind it, alternating feet, fading over thirty ticks
			int step = 0;
			for (double d = 0.5D; d < Math.min(length, head); d += 0.55D, step++) {
				float placed = (float) ((d + i * path.spacing()) / Math.max(0.01D, path.frontSpeed()));
				float since = m.age() - placed;
				if (since < 0.0F || since > 30.0F) continue;
				double side = (step % 2 == 0 ? -0.14D : 0.14D) + (i - 1) * 0.04D;
				float fade = 1.0F - since / 30.0F;
				VelorumFrostGeometry.rect(s.p(), s.frost(), g, VelorumFrostGeometry.RIME, d - 0.14D, d + 0.14D, side - 0.08D, side + 0.08D,
						Field.of(fade), 0.0F, 0.0F, Field.of(fade), m.seed() + 40 + step);
			}
		}
	}

	/**
	 * The Splinter Crown: from tick eight a landing ring freezes under each mark and locks at fourteen while an icicle
	 * hangs and grows six and a half blocks above it, glinting; it falls through the last four ticks and strikes at the
	 * commit: a frost shock, a stump of the icicle standing, a crown of shards splaying out round it and chips.
	 */
	private static void splinterCrown(Scene s, Move m) {
		List<Vec3> points = m.frame().points();
		Look look = m.look();
		float lock = HemoEasing.ramp(m.moveTime(), 8.0F, 14.0F);
		if (m.moveTime() < 8.0F) return;
		for (int i = 0; i < points.size(); i++) {
			Vec3 site = points.get(i);
			Vec3 foot = new Vec3(site.x, m.floorY(), site.z);
			Ground g = m.ground().at(foot);
			int seed = m.seed() + i * 37;
			if (s.floor()) {
				Look ring = look.withGrow(lock).scaled(0.5F + 0.5F * lock);
				VelorumFrostGeometry.seams(s.p(), s.frost(), g, VelorumFrostRules.ring(1.5D, seed), ring, 1.0F, seed);
				rimeFan(s, g, 0.0D, 1.5D, -180.0D, 180.0D, look.withGrow(lock), 0.8F, seed + 1);
				if (lock >= 1.0F) VelorumFrostGeometry.seams(s.p(), s.frost(), g, VelorumFrostRules.star(1.4D, 5, seed + 2), look, 0.8F, seed + 2);
				if (m.age() >= 0.0F) frostShock(s, g, foot, m.age(), 2.4D, seed + 3);
				// the blood they spilled where they stood still beads up and darkens, then the splinters take it
				float spent = m.age() < 0.0F ? 1.0F : 1.0F - HemoEasing.ramp(m.age(), 0.0F, 6.0F);
				VelorumCruorGeometry.stain(s, foot, 1.0F, lock, lock, look.alpha() * spent, seed + 9);
				continue;
			}
			if (!s.solid() || m.age() < 0.0F) continue;
			// cruor splinters burst up out of the stain: five to seven, 1.6 to 2.3 tall, leaning out
			float crumble = m.crumble(0.0F);
			int count = 5 + (int) (VelorumFrostRules.unit(seed, 3) * 3.0D);
			for (int j = 0; j < count; j++) {
				double a = j * Math.PI * 2.0D / count + VelorumFrostRules.unit(seed, j) * 0.5D;
				double r = j == 0 ? 0.0D : 0.28D + 0.2D * VelorumFrostRules.unit(seed, 20 + j);
				Vec3 at = foot.add(Math.cos(a) * r, 0.0D, Math.sin(a) * r);
				double lean = j == 0 ? 0.08D : 0.3D + 0.22D * VelorumFrostRules.unit(seed, 30 + j);
				Shard splinter = new Shard(0.0D, 0.0D, 1.6D + 0.7D * VelorumFrostRules.unit(seed, 10 + j), j == 0 ? 0.22D : 0.15D, 0.7D, a,
						Math.cos(a) * lean, Math.sin(a) * lean, 5);
				VelorumIceGeometry.cluster(s.p(), s.ice(), s.local(at), List.of(splinter), m.age() + 1.0F - j * 0.25F, crumble,
						VelorumIceGeometry.CRUOR_SPLINTER, VelorumIceGeometry.SHARD, j == 0, seed + 20 + j);
			}
			VelorumIceGeometry.chips(s.p(), s.ice(), s.local(foot), m.age(), 1.6D, 7, VelorumIceGeometry.CRUOR_SPLINTER, seed + 40);
		}
	}

	/**
	 * The Endless Hour: rime spreads from his feet over the whole floor through the wind-up, twenty-four seams running out
	 * and two rings freezing; through the hold the frost thickens and black ice grows between the plates, while gloom
	 * creeps in from the floor's rim, the dark narrowing sight. The release sublimes it all over the recovery.
	 */
	private static void endlessHour(Scene s, Move m) {
		if (!s.floor()) return;
		AABB floor = m.frame().bossFloor();
		Vec3 him = m.boss().getPosition(s.partial());
		float spread = HemoEasing.ramp(m.moveTime(), 0.0F, m.windup());
		float hold = m.age() < 0.0F ? 0.0F : HemoEasing.clamp01(m.age() / Math.max(1.0F, m.active()));
		float release = m.age() > m.active() ? HemoEasing.clamp01((m.age() - m.active()) / Math.max(1.0F, m.recovery())) : 0.0F;
		if (release >= 1.0F) return;
		float strength = 1.0F - release;
		Ground g = new Ground(him.x, him.z, 1.0D, 0.0D, m.floorY(), s.camera());
		double a0 = floor.minX - him.x, a1 = floor.maxX - him.x, b0 = floor.minZ - him.z, b1 = floor.maxZ - him.z;
		double far = Math.max(Math.max(Math.abs(a0), Math.abs(a1)), Math.max(Math.abs(b0), Math.abs(b1))) * 1.42D;
		double front = 1.0D + far * spread;
		float thick = 0.6F + 0.4F * hold;
		VelorumFrostGeometry.rect(s.p(), s.frost(), g, VelorumFrostGeometry.RIME, a0, a1, b0, b1,
				(along, right) -> thick * strength * VelorumFrostRules.reach(Math.hypot(along, right), front, 2.0D), 0.15F + 0.6F * hold,
				0.1F * (1.0F - hold), Field.of(strength), m.seed());
		VelorumCruorGeometry.seamsRect(s, g, a0, a1, b0, b1, (along, right) -> strength * VelorumFrostRules.reach(Math.hypot(along, right),
				front, 2.0D), 0.4F + 0.6F * hold, 0.0F, 1.0F, m.seed() + 9);
		Look seams = new Look(spread, 1.0F, hold, 0.15F, 0.8F + 0.3F * hold, strength);
		VelorumFrostGeometry.seams(s.p(), s.frost(), g, VelorumFrostRules.radial(0.8D, far * 0.7D, 24, m.seed() % 15, m.seed()), seams,
				1.0F, m.seed());
		Look rings = seams.withGrow(HemoEasing.clamp01((spread - 0.4F) / 0.6F));
		VelorumFrostGeometry.seams(s.p(), s.frost(), g, VelorumFrostRules.ring(4.0D, m.seed() + 1), rings, 1.0F, m.seed() + 1);
		VelorumFrostGeometry.seams(s.p(), s.frost(), g, VelorumFrostRules.ring(8.0D, m.seed() + 2), rings, 0.8F, m.seed() + 2);
		double depth = 1.0D + 3.0D * hold;
		float dark = 0.85F * spread * strength;
		Field gloom = (along, right) -> {
			double edge = Math.min(Math.min(along - a0, a1 - along), Math.min(right - b0, b1 - right));
			return dark * (float) HemoEasing.clamp01(1.0D - edge / depth);
		};
		VelorumFrostGeometry.rect(s.p(), s.frost(), g, VelorumFrostGeometry.DARK, a0, a1, b0, b0 + depth, Field.of(1.0F), 0.0F, 0.0F, gloom,
				m.seed() + 3);
		VelorumFrostGeometry.rect(s.p(), s.frost(), g, VelorumFrostGeometry.DARK, a0, a1, b1 - depth, b1, Field.of(1.0F), 0.0F, 0.0F, gloom,
				m.seed() + 4);
		VelorumFrostGeometry.rect(s.p(), s.frost(), g, VelorumFrostGeometry.DARK, a0, a0 + depth, b0 + depth, b1 - depth, Field.of(1.0F),
				0.0F, 0.0F, gloom, m.seed() + 5);
		VelorumFrostGeometry.rect(s.p(), s.frost(), g, VelorumFrostGeometry.DARK, a1 - depth, a1, b0 + depth, b1 - depth, Field.of(1.0F),
				0.0F, 0.0F, gloom, m.seed() + 6);
	}

	/**
	 * The Hollow Toll: frost gathers at the staff's heel and the reach shows faintly; the staff is driven down (the stroke
	 * cut through the air) and a ring of frost leaves his feet: a bright frozen front with frost spray lifting off it, the
	 * floor rimed and glazed behind it, and rows of ice plates heaving up as it passes and crumbling behind it.
	 */
	private static void hollowToll(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Ring ring)) return;
		Look look = m.look();
		Vec3 centre = new Vec3(m.frame().origin().x, m.floorY(), m.frame().origin().z);
		slash(s, m, Limb.STAFF, 4.0F, 1.5F, 1.0D);
		if (m.age() < 0.0F) {
			if (!s.floor()) return;
			rimeFan(s, m.ground(), 0.0D, 1.4D, -180.0D, 180.0D, look, 1.0F, m.seed());
			VelorumFrostGeometry.seams(s.p(), s.frost(), m.ground(), VelorumFrostRules.star(1.4D, 7, m.seed() + 1), look, 1.0F, m.seed() + 1);
			VelorumFrostGeometry.seams(s.p(), s.frost(), m.ground(), VelorumFrostRules.ring(ring.to(), m.seed() + 2), look, 0.9F, m.seed() + 2);
			// the reach freezes first: a band of rime just inside it, so the edge the ring will run to reads across the floor
			Look edge = look.scaled(0.7F);
			rimeFan(s, m.ground(), Math.max(ring.from(), ring.to() - 1.1D), ring.to(), -180.0D, 180.0D, edge.withGrow(1.0F), 0.8F, m.seed() + 5);
			return;
		}
		double radius = ring.radius(m.age(), 0);
		boolean rolling = radius >= 0.0D;
		if (!rolling) radius = ring.to();
		if (s.floor()) {
			double front = radius;
			VelorumFrostGeometry.annulus(s.p(), s.frost(), m.ground(), VelorumFrostGeometry.RIME, ring.from(), Math.max(ring.from() + 0.1D, radius),
					-180.0D, 180.0D, (along, right) -> 0.6F + 0.4F * VelorumFrostRules.reach(front - Math.hypot(along, right), 1.2D, 1.0D),
					0.45F * look.ice(), look.glint() * 0.5F, Field.of(look.alpha()), m.seed() + 3);
			Look band = new Look(1.0F, 1.0F, 0.2F, rolling ? 1.0F : 0.3F, 1.4F, look.alpha());
			VelorumFrostGeometry.seams(s.p(), s.frost(), m.ground(), VelorumFrostRules.ring(radius, m.seed() + 4), band, 1.0F, m.seed() + 4);
			if (rolling) VelorumCruorGeometry.seams(s, m.ground(), Math.max(ring.from(), radius - 1.2D), radius + 0.2D, -180.0D, 180.0D,
					Field.of(1.0F), 1.0F, look.alpha(), m.seed() + 6);
			if (rolling) {
				for (int i = 0; i < 24; i++) {
					double a = i * Math.PI / 12.0D + m.seed() * 0.05D;
					Vec3 at = centre.add(Math.cos(a) * radius, 0.3D + 0.2D * VelorumFrostRules.unit(m.seed(), i), Math.sin(a) * radius);
					LuxUmbraGeometry.card(s.p(), s.rime(), s.local(at), s.right(), s.up(), 0.4D, 0.35D, LuxUmbraGeometry.WISP, m.seed() + 10 + i,
							WHITE, 0.5F);
				}
			}
		}
		if (!s.solid()) return;
		double[] radii = {2.0D, 3.5D, 5.0D, 6.5D, 8.0D};
		double speed = (ring.to() - ring.from()) / Math.max(1, ring.travelTicks());
		for (int k = 0; k < radii.length; k++) {
			double r = radii[k];
			float passAt = (float) ((r - ring.from()) / Math.max(0.01D, speed));
			int count = (int) Math.round(Math.PI * 2.0D * r / 1.7D);
			for (int i = 0; i < count; i++) {
				int seed = m.seed() + 400 + k * 71 + i;
				double a = (i + VelorumFrostRules.unit(seed, 1) * 0.5D) / count * Math.PI * 2.0D;
				Vec3 foot = centre.add(Math.cos(a) * r, 0.0D, Math.sin(a) * r);
				Shard plate = new Shard(0.0D, 0.0D, 0.75D + 0.3D * VelorumFrostRules.unit(seed, 2), 0.2D, 0.35D, a + Math.PI / 2.0D,
						Math.cos(a) * 0.45D, Math.sin(a) * 0.45D, 4);
				VelorumIceGeometry.cluster(s.p(), s.ice(), s.local(foot), List.of(plate), m.age() - passAt, 4.0F, VelorumIceGeometry.PLATE,
						VelorumIceGeometry.SHARD, false, seed);
			}
		}
	}

	/**
	 * The Draw of Silence: the frost over his disc is drawn in toward him through the hold, seams running in from the
	 * rim, and from each challenger he can see a pale stream of drawn silence runs to his closing hand, slow beads of
	 * rime travelling in along it.
	 */
	private static void silenceDraw(Scene s, Move m) {
		if (!s.floor() && !s.solid()) return;
		BossFootprint shape = m.footprint() instanceof BossFootprint.Occluded occluded ? occluded.shape() : m.footprint();
		double radius = shape instanceof BossFootprint.Disc disc ? disc.radius() : 10.0D;
		Look look = m.look();
		float drawn = m.age() < 0.0F ? 0.0F : HemoEasing.clamp01(m.age() / Math.max(1.0F, m.active()));
		double front = radius - (radius - 1.5D) * drawn;
		float frost = look.frost();
		if (s.floor()) {
			VelorumFrostGeometry.annulus(s.p(), s.frost(), m.ground(), VelorumFrostGeometry.RIME, 1.2D, radius, -180.0D, 180.0D,
					(along, right) -> frost * (0.35F + 0.65F * VelorumFrostRules.reach(-Math.hypot(along, right), -front, 1.0D)), look.ice() * 0.5F,
					look.glint(), Field.of(look.alpha() * 0.9F), m.seed());
			VelorumFrostGeometry.seams(s.p(), s.frost(), m.ground(), VelorumFrostRules.inward(1.0D, radius, 14, m.seed() % 11, m.seed()), look,
					1.0F, m.seed());
			VelorumFrostGeometry.seams(s.p(), s.frost(), m.ground(), VelorumFrostRules.ring(radius, m.seed() + 1), look.withGrow(1.0F), 0.8F,
					m.seed() + 1);
		}
		if (m.age() < 0.0F || m.age() > m.active() + 4.0F) return;
		float ease = HemoEasing.clamp01(m.age() / 4.0F) * (1.0F - HemoEasing.ramp(m.age(), m.active(), m.active() + 4.0F));
		Vec3 hand = anchor(m.boss(), s.partial(), Limb.CLAW);
		int streams = 0;
		for (Player player : s.level().players()) {
			if (streams >= 4) break;
			if (player.isSpectator() || !player.isAlive()) continue;
			Vec3 feet = player.getPosition(s.partial());
			if (Shapes.planar(feet, m.frame().origin()) > radius || !m.boss().hasLineOfSight(player)) continue;
			Vec3 chest = feet.add(0.0D, player.getBbHeight() * 0.6D, 0.0D);
			Minecraft mc = Minecraft.getInstance();
			if (player == mc.player && mc.options.getCameraType().isFirstPerson() && chest.distanceTo(hand) > 3.0D) {
				chest = chest.add(hand.subtract(chest).normalize().scale(1.4D));
			}
			VelorumCruorGeometry.stream(s, chest, hand, ease, m.age() / Math.max(1.0F, m.active()), true, player.getId() * 17);
			streams++;
		}
	}

	// ── openings and the ending ──────────────────────────────────────

	/**
	 * The openings on the floor: in the Guttering Lantern the folds of darkness lie sunk in a breathing pool round his
	 * feet; in the Bow the frost at his feet thaws back; in the Overheld Invocation a crack of frost snaps out across the
	 * floor from him and fades.
	 */
	private static void opening(Scene s, VelorumEntity boss) {
		if (!s.floor()) return;
		SaintMoveView view = boss.view();
		int id = view.openingId();
		if (id == 0) return;
		float t = view.openingTime(s.partial());
		if (t < 0.0F) return;
		int length = Math.max(1, view.openingLength());
		float weight = HemoEasing.clamp01(t / 5.0F) * (1.0F - HemoEasing.ramp(t, length - 6.0F, length));
		if (weight <= 0.0F) return;
		Vec3 feet = boss.getPosition(s.partial());
		Vec3 base = new Vec3(feet.x, boss.getY(), feet.z);
		Ground g = new Ground(base.x, base.z, 1.0D, 0.0D, base.y, s.camera());
		int seed = boss.getId() * 7 + id;
		if (id == VelorumOpeningRules.GUTTER_ID) {
			// the dark floods in to his feet: drawn before everything, in the DARK phase (gutterFlood)
			return;
		} else if (id == VelorumOpeningRules.BOW_ID) {
			float thaw = HemoEasing.clamp01(t / length);
			VelorumFrostGeometry.annulus(s.p(), s.frost(), g, VelorumFrostGeometry.RIME, 0.6D, 2.6D, -180.0D, 180.0D,
					Field.of(0.9F * (1.0F - thaw)), 0.3F * (1.0F - thaw), 0.0F, Field.of(0.7F * weight), seed);
		} else if (id == VelorumOpeningRules.OVERHELD_ID) {
			float snap = 1.0F - HemoEasing.ramp(t, 10.0F, 30.0F);
			VelorumFrostGeometry.seams(s.p(), s.frost(), g, VelorumFrostRules.star(2.4D, 7, seed),
					new Look(HemoEasing.clamp01(t / 3.0F), 1.0F, 0.3F, HemoEasing.clamp01(1.0F - t / 8.0F), 1.2F, weight * snap), 1.0F, seed);
		}
	}

	/**
	 * The ending, warmth returning: the rime round him draws back and fades as the hood bows; then the frost falls out of
	 * the light, fragments of rime sliding off his shoulders, stoles and sleeves to the floor.
	 */
	private static void ending(Scene s, VelorumEntity boss) {
		float t = boss.view().endingTime(s.partial());
		if (t < 0.0F) return;
		Vec3 feet = boss.getPosition(s.partial());
		double floorY = boss.getY();
		int seed = boss.getId() * 5;
		if (s.floor()) {
			float recede = HemoEasing.ramp(t, 16.0F, 80.0F);
			if (recede < 1.0F) {
				Ground g = new Ground(feet.x, feet.z, 1.0D, 0.0D, floorY, s.camera());
				double reach = 6.0D * (1.0D - recede);
				VelorumFrostGeometry.annulus(s.p(), s.frost(), g, VelorumFrostGeometry.RIME, 0.0D, Math.max(0.2D, reach), -180.0D, 180.0D,
						(along, right) -> 0.8F * VelorumFrostRules.reach(Math.hypot(along, right), reach, 1.2D), 0.2F * (1.0F - recede), 0.0F,
						Field.of(1.0F - recede), seed);
			}
			return;
		}
		if (!s.solid() || t < 40.0F || t > 64.0F + VelorumFrostRules.CHIP_LIFE) return;
		for (int i = 0; i < 12; i++) {
			float own = t - (40.0F + i * 2.0F);
			if (own < 0.0F || own > 14.0F) continue;
			double a = VelorumFrostRules.unit(seed, i) * Math.PI * 2.0D;
			double height = 1.2D + 0.9D * VelorumFrostRules.unit(seed, 20 + i);
			double drop = Math.min(height, 0.035D * own * own);
			double slide = drop >= height ? (own - Math.sqrt(height / 0.035D)) * 0.04D : 0.0D;
			double r = 0.3D + 0.12D * VelorumFrostRules.unit(seed, 40 + i) + slide;
			Vec3 at = new Vec3(feet.x + Math.cos(a) * r, floorY + height - drop + 0.04D, feet.z + Math.sin(a) * r);
			double size = (0.06D + 0.05D * VelorumFrostRules.unit(seed, 60 + i)) * (1.0D - HemoEasing.ramp(own, 10.0F, 14.0F));
			VelorumIceGeometry.chip(s.p(), s.ice(), s.local(at), size, own * 0.7D * (drop >= height ? 0.2D : 1.0D), VelorumIceGeometry.CRYSTAL,
					seed + i);
		}
	}

	// ── what his blows and his cold do to bodies ─────────────────────

	/**
	 * A body Velorum has just struck, read from the hurt flash every client already has (the body's hurt time and the
	 * damage event's source), so every viewer sees the same hit: clear ice splinters bite in along the blow and shatter
	 * back out of it with chips flung on, a cold flash and a shadow at the chest, frost spraying along the blow, and the
	 * floor at the feet frosting with a small star of seams. Timeline: bite 0 to 1.5, hold, shatter 3 to 8, fray to 10.
	 */
	private static void impacts(Scene s, VelorumEntity boss) {
		Vec3 him = boss.getPosition(s.partial());
		List<LivingEntity> struck = s.level().getEntitiesOfClass(LivingEntity.class, boss.getBoundingBox().inflate(24.0D),
				body -> body != boss && body.hurtTime > 0 && body.isAlive());
		for (LivingEntity body : struck) {
			DamageSource source = body.getLastDamageSource();
			if (source == null || source.getEntity() != boss) continue;
			float age = body.hurtDuration - body.hurtTime + s.partial();
			if (age < 0.0F || age >= 10.0F) continue;
			Vec3 feet = body.getPosition(s.partial());
			Vec3 at = feet.add(0.0D, body.getBbHeight() * 0.55D, 0.0D);
			Vec3 away = new Vec3(at.x - him.x, 0.0D, at.z - him.z);
			away = away.lengthSqr() < 1.0E-4D ? new Vec3(1.0D, 0.0D, 0.0D) : away.normalize();
			int seed = body.getId() * 7 + body.hurtDuration;
			if (s.solid()) {
				float bite = HemoEasing.clamp01(age / 1.5F);
				float shatter = 1.0F - HemoEasing.ramp(age, 3.0F, 8.0F);
				Vec3 across = away.cross(new Vec3(0.0D, 1.0D, 0.0D));
				for (int i = 0; i < 4; i++) {
					double spread = (VelorumFrostRules.unit(seed, i) - 0.5D) * 1.3D;
					Vec3 dir = away.add(across.scale(spread)).add(0.0D, (VelorumFrostRules.unit(seed, 10 + i) - 0.3D) * 0.8D, 0.0D).normalize();
					Vec3 root = at.subtract(dir.scale(0.3D)).add(0.0D, (i - 1.5D) * 0.12D, 0.0D);
					VelorumIceGeometry.shard(s.p(), s.ice(), s.local(root), dir, (0.5D + 0.2D * VelorumFrostRules.unit(seed, 20 + i)) * bite, 0.06D,
							0.8D, i, 5, shatter, VelorumIceGeometry.withGlint(VelorumIceGeometry.CRYSTAL, 1.0F - age / 10.0F),
							VelorumIceGeometry.SHARD, seed + i * 5);
				}
				VelorumIceGeometry.chips(s.p(), s.ice(), s.local(at.add(away.scale(0.2D))), age - 2.5F, 1.3D, 6, VelorumIceGeometry.CRYSTAL,
						seed + 30);
			} else if (s.floor()) {
				float flash = 1.0F - HemoEasing.clamp01(age / 3.5F);
				float fray = 1.0F - age / 10.0F;
				Vec3 chest = s.local(at);
				LuxUmbraGeometry.card(s.p(), s.shade(), chest, s.right(), s.up(), 0.55D, 0.55D, LuxUmbraGeometry.WISP, seed, DARK, 0.5F * fray);
				LuxUmbraGeometry.card(s.p(), s.lux(), chest, s.right(), s.up(), 0.42D, 0.42D, LuxUmbraGeometry.FOCUS, seed + 1, CORE, 0.85F * flash);
				for (int i = 0; i < 4; i++) {
					double travel = HemoEasing.easeOutCubic(HemoEasing.clamp01(age / 6.0F)) * (0.6D + 0.25D * i);
					Vec3 spray = chest.add(away.scale(travel)).add(0.0D, (i - 1.5D) * 0.15D + travel * 0.2D, 0.0D);
					LuxUmbraGeometry.card(s.p(), s.rime(), spray, s.right(), s.up(), 0.22D + 0.1D * travel, 0.2D, LuxUmbraGeometry.WISP,
							seed + 2 + i, WHITE, 0.55F * fray);
				}
				Ground g = new Ground(feet.x, feet.z, away.x, away.z, feet.y, s.camera());
				float grow = HemoEasing.clamp01(age / 3.0F);
				VelorumFrostGeometry.annulus(s.p(), s.frost(), g, VelorumFrostGeometry.RIME, 0.0D, 1.0D, -180.0D, 180.0D,
						(along, right) -> VelorumFrostRules.reach(Math.hypot(along, right), grow, 0.4D), 0.2F, flash, Field.of(fray), seed + 6);
				VelorumFrostGeometry.seams(s.p(), s.frost(), g, VelorumFrostRules.star(0.9D, 5, seed + 7),
						new Look(grow, 1.0F, 0.2F, flash, 1.0F, fray), 1.0F, seed + 7);
			}
		}
	}

	/**
	 * The challengers he holds: a preserved one is shut in a faceted shell of clear ice that grows up from the feet in
	 * three ticks, crowned, with crystals at its foot and rime on the floor, and breaks into falling shards when it
	 * opens; a kneeling one has rime round them and crystals at the shins.
	 */
	private static void held(Scene s, VelorumEntity boss) {
		float now = AnimationTime.gameTime(s.level(), s.partial());
		for (Player player : s.level().players()) {
			if (player.isSpectator() || player.distanceToSqr(boss) > 48.0D * 48.0D) continue;
			int flags = boss.syncedChallengerFlags(player.getId());
			boolean preserved = player.isAlive() && (flags & VelorumStillnessRules.FLAG_PRESERVED) != 0;
			boolean kneeling = player.isAlive() && (flags & VelorumStillnessRules.FLAG_KNEELING) != 0;
			float[] shell = shell(player, preserved, now);
			Vec3 feet = player.getPosition(s.partial());
			int seed = player.getId() * 31;
			double width = Math.max(0.45D, player.getBbWidth() * 0.85D), height = player.getBbHeight() * 1.04D;
			if (preserved) {
				float grow = VelorumFrostRules.shellGrow(now - shell[0]);
				if (s.clear()) shellPanes(s, feet, width, height, grow, seed);
				else if (s.solid()) rimeAtShins(s, feet, width * 1.05D, 0.3D + 0.25D * grow, grow, VelorumIceGeometry.CRYSTAL, seed);
				else VelorumFrostGeometry.annulus(s.p(), s.frost(), new Ground(feet.x, feet.z, 1.0D, 0.0D, feet.y, s.camera()),
						VelorumFrostGeometry.RIME, 0.0D, 1.1D, -180.0D, 180.0D, Field.of(grow), 0.4F, 0.1F, Field.of(1.0F), seed);
			} else if (!Float.isNaN(shell[1]) && s.clear()) {
				shellBreak(s, feet, width, height, now - shell[1], seed);
			}
			if (kneeling) {
				if (s.solid()) rimeAtShins(s, feet, 0.42D, 0.35D, 1.0F, VelorumIceGeometry.CRYSTAL, seed + 3);
				else if (s.floor()) {
					Ground g = new Ground(feet.x, feet.z, 1.0D, 0.0D, feet.y, s.camera());
					VelorumFrostGeometry.annulus(s.p(), s.frost(), g, VelorumFrostGeometry.RIME, 0.0D, 0.85D, -180.0D, 180.0D, Field.of(0.9F), 0.3F,
							0.1F, Field.of(1.0F), seed + 4);
					VelorumFrostGeometry.seams(s.p(), s.frost(), g, VelorumFrostRules.ring(0.85D, seed + 5), new Look(1.0F, 1.0F, 0.3F, 0.2F, 1.0F, 1.0F),
							1.0F, seed + 5);
				}
			}
		}
	}

	/** Tracks when a challenger's shell closed and when it broke: {closed at, broke at}, NaN when not. */
	private static float[] shell(Player player, boolean preserved, float now) {
		float[] state = SHELLS.computeIfAbsent(player, ignored -> new float[]{Float.NaN, Float.NaN});
		if (preserved) {
			if (Float.isNaN(state[0])) state[0] = now;
			state[1] = Float.NaN;
		} else if (!Float.isNaN(state[0])) {
			state[0] = Float.NaN;
			state[1] = now;
		} else if (!Float.isNaN(state[1]) && now - state[1] > VelorumFrostRules.SHELL_BREAK) {
			state[1] = Float.NaN;
		}
		return state;
	}

	/** The shell's panes: seven tilted faces of clear ice round the body, leaning in to a crown over the head. */
	private static void shellPanes(Scene s, Vec3 feet, double width, double height, float grow, int seed) {
		int faces = 7;
		Vec3 base = s.local(feet);
		Vec3 apex = base.add(0.0D, height * 1.12D, 0.0D);
		// polish round 2: thin lit cruor read as rose quartz; the shell is the player's own blood, dark oxblood glass
		int state = VelorumIceGeometry.state(0.3F, 0.6F, 0.25F * (1.0F - grow));
		Vec3[] low = new Vec3[faces], high = new Vec3[faces];
		for (int i = 0; i < faces; i++) {
			double a = i * Math.PI * 2.0D / faces + seed * 0.37D;
			double r = width * (0.92D + 0.16D * VelorumFrostRules.unit(seed, i));
			low[i] = base.add(Math.cos(a) * r, -0.02D, Math.sin(a) * r);
			high[i] = base.add(Math.cos(a + 0.12D) * r * 0.8D, height * (0.86D + 0.1D * VelorumFrostRules.unit(seed, 10 + i)),
					Math.sin(a + 0.12D) * r * 0.8D);
		}
		for (int i = 0; i < faces; i++) {
			int j = (i + 1) % faces;
			VelorumIceGeometry.pane(s.p(), s.ice(), low[i], low[j], high[j], high[i], state, grow, seed + i);
			VelorumIceGeometry.pane(s.p(), s.ice(), high[i], high[j], apex, apex, state, Math.max(0.0F, grow * 1.2F - 0.2F), seed + 10 + i);
		}
	}

	/** The shell breaking: its panes fall away outward as clear shards, tumbling and subliming over SHELL_BREAK ticks. */
	private static void shellBreak(Scene s, Vec3 feet, double width, double height, float age, int seed) {
		if (age < 0.0F || age >= VelorumFrostRules.SHELL_BREAK) return;
		float t = age / VelorumFrostRules.SHELL_BREAK;
		for (int i = 0; i < 14; i++) {
			double a = i * Math.PI * 2.0D / 14.0D + seed * 0.37D;
			double y = height * (0.15D + 0.7D * VelorumFrostRules.unit(seed, 30 + i));
			double out = width + 0.5D * t + 0.3D * VelorumFrostRules.unit(seed, 50 + i) * t;
			double fall = Math.min(y, 0.04D * age * age);
			Vec3 at = s.local(feet.add(Math.cos(a) * out, y - fall, Math.sin(a) * out));
			Vec3 axis = new Vec3(Math.cos(a + age * 0.3D), 0.6D - t, Math.sin(a + age * 0.3D));
			VelorumIceGeometry.shard(s.p(), s.ice(), at, axis, 0.28D, 0.07D, 0.35D, a, 4, 1.0F - t, VelorumIceGeometry.CRYSTAL,
					VelorumIceGeometry.CLEAR, seed + i);
		}
	}

	/** Rime crystals round a body's shins, grown to {@code grown} of {@code height}. */
	private static void rimeAtShins(Scene s, Vec3 feet, double radius, double height, float grown, int state, int seed) {
		for (int i = 0; i < 6; i++) {
			double a = i * Math.PI / 3.0D + VelorumFrostRules.unit(seed, i) * 0.6D;
			Shard crystal = new Shard(0.0D, 0.0D, height * (0.7D + 0.4D * VelorumFrostRules.unit(seed, 10 + i)), 0.06D, 0.85D, a,
					Math.cos(a) * 0.25D, Math.sin(a) * 0.25D, 5);
			VelorumIceGeometry.stand(s.p(), s.ice(), s.local(feet.add(Math.cos(a) * radius, 0.0D, Math.sin(a) * radius)), crystal, grown, 1.0F,
					state, VelorumIceGeometry.SHARD, seed + i);
		}
	}

	/**
	 * A woken devotee: while it thaws the rime cracks off it and falls as chips, a puddle of rime at its feet fading;
	 * stilled or kneeling home, rime crystals climb round it again over thirty ticks.
	 */
	private static void devotee(Scene s, VelorumDevoteeEntity devotee) {
		float since = AnimationTime.gameTime(s.level(), s.partial()) - devotee.stateStart();
		if (since < 0.0F) return;
		Vec3 feet = devotee.getPosition(s.partial());
		int seed = devotee.getId() * 19;
		switch (devotee.state()) {
			case THAWING -> {
				if (since > 44.0F) return;
				if (s.floor()) {
					Ground g = new Ground(feet.x, feet.z, 1.0D, 0.0D, feet.y, s.camera());
					VelorumFrostGeometry.annulus(s.p(), s.frost(), g, VelorumFrostGeometry.RIME, 0.0D, 0.95D, -180.0D, 180.0D,
							Field.of(1.0F - since / 44.0F), 0.2F, 0.0F, Field.of(1.0F - since / 44.0F), seed);
				} else if (s.solid()) {
					for (int i = 0; i < 12; i++) {
						float own = since - i * 2.5F;
						if (own < 0.0F || own > 12.0F) continue;
						double a = VelorumFrostRules.unit(seed, i) * Math.PI * 2.0D;
						double height = 0.3D + 1.5D * VelorumFrostRules.unit(seed, 20 + i);
						double drop = Math.min(height, 0.04D * own * own);
						Vec3 at = feet.add(Math.cos(a) * 0.32D, height - drop + 0.03D, Math.sin(a) * 0.32D);
						VelorumIceGeometry.chip(s.p(), s.ice(), s.local(at), 0.07D * (1.0D - HemoEasing.ramp(own, 8.0F, 12.0F)), own * 0.6D,
								VelorumIceGeometry.FROSTED, seed + i);
					}
				}
			}
			case STILLED, KNEELING -> {
				float grown = HemoEasing.ramp(since, 0.0F, 30.0F);
				if (s.solid()) rimeAtShins(s, feet, 0.36D, 0.6D, grown, VelorumIceGeometry.CRYSTAL, seed);
				else if (s.floor()) {
					VelorumFrostGeometry.annulus(s.p(), s.frost(), new Ground(feet.x, feet.z, 1.0D, 0.0D, feet.y, s.camera()),
							VelorumFrostGeometry.RIME, 0.0D, 0.9D, -180.0D, 180.0D, Field.of(grown), 0.2F, 0.0F, Field.of(1.0F), seed);
				}
			}
			default -> {
			}
		}
	}

	// ── the standing black ice ───────────────────────────────────────

	private static void hazard(Scene s, BossGroundHazardEntity hazard) {
		if (!(hazard.owner() instanceof VelorumEntity boss)) return;
		if (VelorumDarkSight.shape(hazard)) {
			shapeOfDark(s, hazard, boss);
			return;
		}
		if (VelorumDarkSight.reach(hazard)) {
			reachingArm(s, hazard, boss);
			return;
		}
		if (!VelorumDarkSight.field(hazard)) return;
		float age = AnimationTime.age(hazard, s.partial());
		float fade = SaintTelegraphTiming.hazardFade(age, hazard.life(), hazard.fade(), hazard.fading(), hazard.fadeStart());
		if (fade <= 0.0F) return;
		standingIce(s, hazard, boss, age, fade);
	}

	/**
	 * The black ice standing (sixty ticks): each lane a trench filled with glossy black ice lying just under the floor,
	 * its lips rimed and feathered, a slow cold haze drifting over it, and the blades the move broke out still standing.
	 * Over its last stretch rime greys the ice over, the blades crumble from their points outward along the lane, and the
	 * trench closes as the floor heals.
	 */
	private static void standingIce(Scene s, BossGroundHazardEntity hazard, VelorumEntity boss, float age, float fade) {
		if (!(hazard.shape() instanceof BossFootprint.Lane lane)) return;
		FootprintFrame frame = hazard.frame();
		Ground base = Ground.of(frame.origin(), Shapes.forward(frame), frame.floorY(), s.camera());
		float life = hazard.life();
		float grey = HemoEasing.ramp(age, life - 30.0F, life);
		float close = HemoEasing.ramp(age, life - 16.0F, life + SaintTelegraphTiming.HAZARD_NATURAL_FADE);
		float handover = VelorumMove.CRUOR_RELEASE.authored().active() - 1.0F;
		BossFootprint moving = VelorumMove.CRUOR_RELEASE.footprint(boss.view().inputs());
		double speed = moving instanceof BossFootprint.Lane cast ? Math.max(0.01D, cast.frontSpeed()) : 1.0D;
		double half = lane.halfWidth();
		Look lips = new Look(1.0F, 1.0F, 0.6F + 0.4F * grey, 0.1F * (1.0F - grey), 1.0F - 0.4F * close, fade * (1.0F - close * 0.6F));
		for (int n = 0; n < lane.lanes(); n++) {
			int index = lane.index(n);
			Ground g = base.lane(lane.offset(index), lane.angle(index));
			int seed = 7919 + index * 131;
			if (s.floor()) {
				lips(s, g, 0.6D, lane.length(), half, lips, seed);
				filling(s, g, lane.length(), half, (age + handover - lane.delay(index)) * speed, close, 0.06F + 0.6F * grey, 0.1F * (1.0F - grey),
						fade, seed);
				for (int k = 0; k < 4; k++) {
					double drift = (s.time() * 0.02D + k * 0.25D + index * 0.13D) % 1.0D;
					Vec3 at = world(g, 0.6D + (lane.length() - 0.6D) * drift, 0.0D, frame.floorY() + 0.2D);
					LuxUmbraGeometry.card(s.p(), s.rime(), s.local(at), s.right(), s.up(), 0.55D, 0.22D, LuxUmbraGeometry.WISP, seed + 10 + k,
							WHITE, 0.25F * fade * (float) Math.sin(drift * Math.PI));
				}
			} else if (s.solid()) {
				splinters(s, g, lane, index, speed, frame.floorY(), age + handover, handover + life - 14.0F);
			}
		}
	}

	// ── pieces ───────────────────────────────────────────────────────

	/** Rime over a sector of floor, its front running out with the look's grow. */
	private static void rimeFan(Scene s, Ground g, double inner, double radius, double from, double to, Look look, float strength, int seed) {
		if (!look.visible()) return;
		double front = inner + (radius - inner + 0.6D) * look.grow();
		float frost = look.frost();
		VelorumFrostGeometry.annulus(s.p(), s.frost(), g, VelorumFrostGeometry.RIME, inner, radius, from, to,
				(along, right) -> frost * VelorumFrostRules.reach(Math.hypot(along, right), front, 0.9D), look.ice(), look.glint(),
				Field.of(look.alpha() * strength), seed);
	}

	/**
	 * A frost shock leaving {@code at}: a bright frozen ring running out to {@code reach} over SHOCK ticks with a band of
	 * rime behind it, frost spray lifting off its front and a cold flash where it started.
	 */
	private static void frostShock(Scene s, Ground base, Vec3 at, float age, double reach, int seed) {
		double radius = VelorumFrostRules.shockRadius(age, reach);
		if (radius <= 0.05D) return;
		float t = age / VelorumFrostRules.SHOCK;
		float fade = 1.0F - t;
		Ground g = base.at(at);
		VelorumFrostGeometry.seams(s.p(), s.frost(), g, VelorumFrostRules.ring(radius, seed), new Look(1.0F, 1.0F, 0.2F, fade, 1.25F, fade * fade),
				1.0F, seed);
		VelorumFrostGeometry.annulus(s.p(), s.frost(), g, VelorumFrostGeometry.RIME, Math.max(0.0D, radius - 1.6D), radius, -180.0D, 180.0D,
				(along, right) -> 0.9F * (float) HemoEasing.clamp01(1.0D - Math.abs(Math.hypot(along, right) - radius) / 1.6D), 0.2F, fade,
				Field.of(0.8F * fade), seed + 1);
		int sprays = Math.max(6, (int) (radius * 3.0D));
		for (int i = 0; i < sprays; i++) {
			double a = i * Math.PI * 2.0D / sprays + seed * 0.1D;
			Vec3 spray = new Vec3(at.x + Math.cos(a) * radius, at.y + 0.25D + 0.3D * t, at.z + Math.sin(a) * radius);
			LuxUmbraGeometry.card(s.p(), s.rime(), s.local(spray), s.right(), s.up(), 0.3D, 0.28D, LuxUmbraGeometry.WISP, seed + 2 + i, WHITE,
					0.45F * fade);
		}
		if (age < 3.0F) {
			LuxUmbraGeometry.card(s.p(), s.lux(), s.local(at.add(0.0D, 0.3D, 0.0D)), s.right(), s.up(), 0.6D + age * 0.2D, 0.45D,
					LuxUmbraGeometry.FOCUS, seed + 40, COLD, 0.7F * (1.0F - age / 3.0F));
		}
	}

	/**
	 * A trail cut through the air along the path his {@code limb} really took (the animation's own world-space trail,
	 * {@link VelorumAnimState#lanternAt}, {@code sideLanternAt} and {@code clawAt}, which the renderer records every frame
	 * it draws him): a shadow shell he drags, a pale frost blade tapering from tail to head, a cold core, two cold sinews
	 * twisting slowly round its spine (his blood barely moves: a shimmer, no heartbeat) and a knot of light at the
	 * lantern or the hand. It runs from {@code lead} ticks before the commit to {@code sweep} after it, then holds where
	 * it was cut, drifts up, widens and frays, shedding frost that drifts down slowly. A limb that barely moved cuts
	 * nothing, so the effect can never disagree with the animation.
	 */
	private static void slash(Scene s, Move m, Limb limb, float lead, float sweep, double scale) {
		if (!s.floor() || m.age() < -lead - 1.0F) return;
		float fade = VelorumFrostRules.slashFade(m.age(), sweep);
		if (fade <= 0.0F) return;
		float fray = VelorumFrostRules.slashFray(m.age(), sweep);
		int key = m.boss().syncedMoveSerial() * 8 + limb.ordinal();
		Cut kept = CUTS.get(m.boss());
		List<Vec3> hand;
		if (m.age() > sweep && kept != null && kept.key() == key) {
			hand = kept.path();
		} else {
			hand = cut(s, m, limb, lead, sweep);
			if (m.age() >= sweep && hand.size() >= 3) CUTS.put(m.boss(), new Cut(key, hand));
		}
		if (hand.size() < 3 || hand.get(0).distanceToSqr(hand.get(hand.size() - 1)) < 0.2D) return;
		int count = hand.size();
		List<Vec3> path = new ArrayList<>(count);
		double[] shell = new double[count], body = new double[count], core = new double[count];
		for (int i = 0; i < count; i++) {
			double f = i / (double) (count - 1);
			path.add(s.local(hand.get(i).add(0.0D, fray * 0.3D * f, 0.0D)));
			double taper = Math.min(1.0D, (1.0D - f) * 5.0D + 0.3D);
			double w = (0.02D + 0.15D * Math.pow(f, 1.3D) * taper) * scale * (1.0D + 0.5D * fray);
			body[i] = w;
			shell[i] = w * 2.0D + 0.02D;
			core[i] = w * 0.2D * (1.0D - fray);
		}
		int seed = m.seed() + 300 + limb.ordinal() * 7;
		LuxUmbraGeometry.ribbonPath(s.p(), s.shade(), path, Vec3.ZERO, shell, LuxUmbraGeometry.WISP, seed, DARK, 0.55F * fade);
		VelorumFrostGeometry.trail(s.p(), s.frost(), path, body, 1.0F, 0.75F, 1.0F - fray, fade, seed + 1);
		LuxUmbraGeometry.ribbonPath(s.p(), s.lux(), path, Vec3.ZERO, core, LuxUmbraGeometry.RIBBON, seed + 2, CORE, 0.85F * fade);
		for (int k = 0; k < 2; k++) {
			List<Vec3> sinew = new ArrayList<>(count);
			for (int i = 0; i < count; i++) {
				double turn = i * 0.9D + k * Math.PI + s.time() * 0.05D;
				double r = body[i] * 0.9D;
				sinew.add(path.get(i).add(s.right().scale(Math.cos(turn) * r)).add(s.up().scale(Math.sin(turn) * r)));
			}
			LuxUmbraGeometry.ribbonPath(s.p(), s.rime(), sinew, Vec3.ZERO, 0.006D * scale, 0.016D * scale, LuxUmbraGeometry.EYE, seed + 4 + k,
					WHITE, 0.6F * fade);
		}
		if (fray <= 0.0F) {
			LuxUmbraGeometry.card(s.p(), s.lux(), path.get(count - 1), s.right(), s.up(), 0.24D * scale, 0.24D * scale, LuxUmbraGeometry.FOCUS,
					seed + 6, CORE, 0.75F);
			return;
		}
		for (int k = 0; k < 5; k++) {
			Vec3 at = path.get(Math.min(count - 1, 2 + k * (count - 3) / 5)).add(0.0D, -fray * (0.25D + 0.12D * k), 0.0D);
			LuxUmbraGeometry.card(s.p(), s.rime(), at, s.right(), s.up(), 0.05D, 0.05D, LuxUmbraGeometry.FOCUS, seed + 10 + k, WHITE, 0.7F * fade);
		}
	}

	/** The limb's world path from {@code lead} ticks before the commit to {@code sweep} after it (or to now), sixteen steps. */
	private static List<Vec3> cut(Scene s, Move m, Limb limb, float lead, float sweep) {
		VelorumAnimState motion = VelorumAnimState.of(m.boss());
		float now = AnimationTime.age(m.boss(), s.partial());
		float commit = now - m.age();
		float stop = Math.min(now, commit + sweep);
		float begin = Math.max(commit - lead - 1.0F, stop - 7.0F);
		List<Vec3> hand = new ArrayList<>(17);
		if (stop - begin < 0.5F) return hand;
		for (int i = 0; i <= 16; i++) {
			float age = begin + (stop - begin) * i / 16.0F;
			Vec3 at = switch (limb) {
				case STAFF -> motion.lanternAt(age);
				case SIDE -> motion.sideLanternAt(age);
				case CLAW -> motion.clawAt(age);
			};
			if (at != null) hand.add(at);
		}
		return hand;
	}

	/**
	 * Where the staff's heel struck at the commit, on the floor: the animation's own heel trail read at the commit tick
	 * and kept for the rest of the move; a block ahead of him when it has none (or a heel that wandered off the cone).
	 */
	private static Vec3 struck(Scene s, Move m) {
		int key = m.boss().syncedMoveSerial();
		Cut kept = HEELS.get(m.boss());
		if (kept != null && kept.key() == key) return kept.path().get(0);
		float commit = AnimationTime.age(m.boss(), s.partial()) - m.age();
		Vec3 at = VelorumAnimState.of(m.boss()).heelAt(commit);
		Vec3 heel = at == null || Shapes.planar(at, m.frame().origin()) > 3.0D ? world(m.ground(), 0.9D, 0.0D, m.floorY())
				: new Vec3(at.x, m.floorY(), at.z);
		if (at != null) HEELS.put(m.boss(), new Cut(key, List.of(heel)));
		return heel;
	}

	/** Where {@code limb} is drawn this frame, in the world. */
	private static Vec3 anchor(VelorumEntity boss, float partial, Limb limb) {
		VelorumAnimState motion = VelorumAnimState.of(boss);
		Vec3 at = boss.getPosition(partial);
		Vec3[] lanterns = motion.lanterns();
		return switch (limb) {
			case STAFF -> at.add(lanterns[0]);
			case SIDE -> at.add(lanterns[Math.min(2, lanterns.length - 1)]);
			case CLAW -> at.add(motion.claw());
		};
	}

	/** A local point of a ground at height {@code y}, in the world. */
	private static Vec3 world(Ground g, double along, double right, double y) {
		return new Vec3(g.wx(along, right), y, g.wz(along, right));
	}

	// ── the Frozen Cruor and liquid dark revision (2026-11-06) ───────

	/** The chill's ground read: the old blood in the seams round him darkening as the viewer grows still (the disc's blood). */
	private static void presence(Scene s, VelorumEntity boss) {
		if (!s.floor() || boss.syncedPresenceRadius() <= 0) return;
		Player viewer = Minecraft.getInstance().player;
		float still = viewer == null ? 0.0F : Math.max(0, boss.syncedChallengerValue(viewer.getId())) / 255.0F;
		Vec3 at = boss.getPosition(s.partial());
		Ground g = new Ground(at.x, at.z, 1.0D, 0.0D, boss.getY(), s.camera());
		double radius = VelorumPresence.RADIUS;
		float well = 0.2F + 0.6F * still;
		VelorumCruorGeometry.seams(s, g, 0.6D, radius, -180.0D, 180.0D,
				(along, right) -> well * (float) HemoEasing.clamp01((radius - Math.hypot(along, right)) / 2.0D), 0.5F * still, 0.55F,
				boss.getId() * 13);
	}

	/** Old blood welling into a move's floor seams through its wind-up and setting at its commit: the cruor's source. */
	private static void welling(Scene s, Move m, Ground g, double r0, double r1, double from, double to, int seed) {
		if (!s.floor()) return;
		Look look = m.look();
		if (!look.visible()) return;
		float well = m.age() < 0.0F ? VelorumCruorGeometry.welled(m.build()) : 1.0F;
		float set = m.age() < 0.0F ? 0.25F * m.build() : HemoEasing.clamp01(m.age());
		double front = r0 + (r1 - r0 + 0.8D) * well;
		VelorumCruorGeometry.seams(s, g, r0, r1, from, to, (along, right) -> VelorumFrostRules.reach(Math.hypot(along, right), front, 1.0D),
				set, look.alpha(), seed);
	}

	/** Torn tatters of liquid dark dragging off the shell a lantern drags through its cut. */
	private static void dragged(Scene s, Move m, Limb limb, float sweep) {
		if (!s.floor() || m.age() < -2.0F) return;
		float fade = VelorumFrostRules.slashFade(m.age(), sweep);
		if (fade <= 0.0F) return;
		float fray = VelorumFrostRules.slashFray(m.age(), sweep);
		List<Vec3> path = cut(s, m, limb, 4.0F, sweep);
		for (int i = 2; i < path.size(); i += 3) {
			Vec3 at = path.get(i);
			VelorumDarkGeometry.tatter(s, new Vec3(at.x, Math.max(m.floorY(), at.y - 1.0D - 0.4D * fray), at.z), 0.3D + 0.2D * fray,
					0.9D + 0.5D * fray, 0.2F + 0.7F * fray, 0.7F * fade, m.seed() + 500 + i);
		}
	}

	/** The Long Night's commit: the dark runs out along the floor from his feet to each pool it seeds; false while none stands. */
	private static boolean runsToPools(Scene s, Move m, Vec3 base) {
		if (m.age() < 0.0F || !s.floor()) return false;
		boolean any = false;
		for (BossGroundHazardEntity hazard : s.hazards()) {
			if (hazard.owner() != m.boss() || !VelorumDarkSight.pool(hazard) || !(hazard.shape() instanceof BossFootprint.Disc disc)) continue;
			float age = AnimationTime.age(hazard, s.partial());
			if (age > m.age() + 2.0F || age > 24.0F) continue;
			any = true;
			Vec3 to = hazard.frame().anchor(disc.at());
			double dx = to.x - base.x, dz = to.z - base.z, length = Math.hypot(dx, dz);
			if (length < 1.0D) continue;
			Ground g = new Ground(base.x, base.z, dx / length, dz / length, m.floorY(), s.camera());
			double front = length * HemoEasing.easeOutCubic(HemoEasing.clamp01(age / 10.0F));
			float fade = 1.0F - HemoEasing.ramp(age, 14.0F, 24.0F);
			Field cover = (along, right) -> (float) (HemoEasing.clamp01((0.9D - Math.abs(right)) / 0.6D)
					* HemoEasing.clamp01((front - along) / 1.2D + 0.3D)) * fade;
			VelorumFrostGeometry.rect(s.p(), s.frost(), g, VelorumFrostGeometry.DARK, 0.0D, Math.max(0.1D, front), -1.2D, 1.2D, Field.of(1.0F),
					0.0F, 0.0F, cover, m.seed() + 600 + hazard.getId());
		}
		return any;
	}

	/** A shade's torn smoke at hood and hem (the shade itself, with its two pale eye points, is the devotee model, the renderer's). */
	private static void shadeSmoke(Scene s, Vec3 foot, float alpha, int seed) {
		if (alpha <= 0.004F) return;
		for (int k = 0; k < 3; k++) {
			double a = k * 2.1D + seed;
			double phase = (s.time() / 20.0D + k / 3.0D) % 1.0D;
			VelorumDarkGeometry.tatter(s, foot.add(Math.cos(a) * 0.3D, 0.0D, Math.sin(a) * 0.3D), 0.3D, 0.6D + 0.4D * phase, (float) phase,
					0.7F * alpha * (float) Math.sin(phase * Math.PI), seed + k);
		}
		for (int k = 0; k < 2; k++) {
			double phase = (s.time() / 16.0D + k * 0.5D) % 1.0D;
			VelorumDarkGeometry.tatter(s, foot.add(0.0D, 1.55D + 0.1D * k, 0.0D), 0.26D, 0.5D + 0.5D * phase, (float) phase,
					0.6F * alpha * (float) Math.sin(phase * Math.PI), seed + 10 + k);
		}
	}

	// ── the dark, drawn first ────────────────────────────────────────

	/** The DARK phase: every pool of his, the Eclipse's dark if no pool stands for it, and the Gutter's flood to his feet. */
	private static void dark(Scene s, VelorumEntity boss, List<BossGroundHazardEntity> hazards) {
		float rise = boss.riseTime(s.partial());
		if (rise >= 0.0F && rise < SaintRise.TICKS) return;
		Light light = Light.of(boss, s.partial());
		boolean eclipse = false;
		for (BossGroundHazardEntity hazard : hazards) {
			if (hazard.owner() != boss || !VelorumDarkSight.pool(hazard)) continue;
			VelorumDarkGeometry.pool(s, hazard, boss, light, 9000 + hazard.getId() * 7);
			if (hazard.shape() instanceof BossFootprint.Region) eclipse = true;
		}
		FootprintFrame frame = boss.view().frame();
		if (!eclipse && frame != null && VelorumDarkSight.eclipseHold(boss, s.partial())) {
			AABB floor = frame.bossFloor();
			Vec3 middle = floor.getCenter();
			Ground g = new Ground(middle.x, middle.z, 1.0D, 0.0D, frame.floorY(), s.camera());
			Field cover = (along, right) -> 1.0F - light.push(g.wx(along, right), g.wz(along, right));
			VelorumFrostGeometry.rect(s.p(), s.frost(), g, VelorumFrostGeometry.DARK, floor.minX - middle.x, floor.maxX - middle.x,
					floor.minZ - middle.z, floor.maxZ - middle.z, Field.of(1.0F), 0.0F, 0.0F, cover, boss.getId() * 3);
		}
		gutterFlood(s, boss, light);
	}

	/** The Guttering Lantern: the dark floods in to his feet, his light shrunk to a block (R.2), until it closes. */
	private static void gutterFlood(Scene s, VelorumEntity boss, Light light) {
		SaintMoveView view = boss.view();
		float t = view.openingTime(s.partial());
		if (view.openingId() != VelorumOpeningRules.GUTTER_ID || t < 0.0F || boss.syncedEndingTick() >= 0) return;
		int length = Math.max(1, view.openingLength());
		float out = HemoEasing.clamp01(t / 4.0F) * (1.0F - HemoEasing.ramp(t, length - 6.0F, length));
		double reach = Math.min(5.5D, 1.6D + VelorumVisualRules.FLOOD_RATE * t * 1.4D);
		Vec3 feet = boss.getPosition(s.partial());
		VelorumDarkGeometry.disc(s, new Vec3(feet.x, boss.getY(), feet.z), boss.getY(), reach, 0.95F * out, light, boss.getId() * 7 + 1);
	}

	// ── Arrested Pulse ───────────────────────────────────────────────

	/**
	 * Arrested Pulse's wind-up: the claw reaches out palm up and closes as round a heart, and the target's blood slows: a
	 * ring of cruor tightens at their feet to the beat's disc as the fingers close, blood darkens in the seams under them
	 * and frost crusts at their wrists. The mark itself (the veins, the beats, the spines) is drawn from its cue.
	 */
	private static void pulse(Scene s, Move m) {
		if (m.age() >= 0.0F) return;
		Player target = nearestPlayer(s, m.frame().target(), 3.0D);
		if (target == null) return;
		Vec3 feet = target.getPosition(s.partial());
		float close = m.build();
		Ground g = m.ground().at(feet);
		if (s.floor() && m.look().visible()) {
			Look ring = new Look(1.0F, 0.7F, 1.0F, 0.2F + 0.5F * close * close, 1.0F + 0.3F * close, m.look().alpha());
			VelorumFrostGeometry.seams(s.p(), s.frost(), g, VelorumFrostRules.ring(2.2D - 0.7D * close, m.seed() + 1), ring, 1.0F, m.seed() + 1);
			VelorumCruorGeometry.seams(s, g, 0.0D, 1.6D, -180.0D, 180.0D, Field.of(close), 0.3F * close, m.look().alpha(), m.seed() + 2);
		}
		wrists(s, target, close, m.seed() + 3);
	}

	/** Frost crusting at a body's two wrists, grown to {@code amount}. */
	private static void wrists(Scene s, Player player, float amount, int seed) {
		if (!s.solid() || amount <= 0.02F) return;
		Vec3 feet = player.getPosition(s.partial());
		float yaw = Mth.rotLerp(s.partial(), player.yBodyRotO, player.yBodyRot) * Mth.DEG_TO_RAD;
		Vec3 right = new Vec3(-Math.cos(yaw), 0.0D, -Math.sin(yaw));
		for (int side = -1; side <= 1; side += 2) {
			Vec3 wrist = feet.add(right.scale(side * 0.36D)).add(0.0D, player.getBbHeight() * 0.42D, 0.0D);
			for (int k = 0; k < 3; k++) {
				double a = k * 2.1D + side;
				VelorumIceGeometry.shard(s.p(), s.ice(), s.local(wrist), new Vec3(Math.cos(a), 0.4D, Math.sin(a)), 0.12D * amount, 0.025D, 0.8D, a, 4,
						1.0F, VelorumIceGeometry.HOARFROST, VelorumIceGeometry.SHARD, seed + side * 5 + k);
			}
		}
	}

	/**
	 * A marked challenger (from the {@code PULSE_MARK} cue): the frost at the wrists, a small cruor ring round their feet
	 * tightening beat by beat, and through each beat's 12-tick tell the disc's rim freezing and cruor points pushing out of
	 * the body's silhouette. The black-red veins under the skin are the shared Congeatio skin ({@code FROZEN_VEINS}).
	 */
	private static void mark(Scene s, VelorumCueLog.Entry entry, double now) {
		if (!(s.level().getEntity(entry.entity()) instanceof Player player) || !player.isAlive()) return;
		if (VelorumCueLog.since(VelorumEffects.PULSE_END, player.getId(), entry.born()) != null) return;
		float since = entry.age(now);
		Vec3 feet = player.getPosition(s.partial());
		int seed = player.getId() * 29;
		float tell = VelorumVisualRules.tell(since);
		float shown = HemoEasing.clamp01(since / 4.0F)
				* (1.0F - HemoEasing.ramp(since, VelorumVisualRules.MARK_LIFE - 6.0F, VelorumVisualRules.MARK_LIFE));
		if (s.floor()) {
			Ground g = new Ground(feet.x, feet.z, 1.0D, 0.0D, feet.y, s.camera());
			VelorumFrostGeometry.seams(s.p(), s.frost(), g, VelorumFrostRules.ring(VelorumVisualRules.markRing(since), seed),
					// polish round 2: a band of the marked's own blood, not a double white contour; the rim's frost is the tell
					new Look(1.0F, 0.35F, 1.0F, 0.05F + 0.6F * tell, 1.7F, shown), 1.0F, seed);
			if (tell > 0.0F) {
				Look rim = new Look(tell, 1.0F, 0.5F, tell * tell, 1.3F, shown);
				VelorumFrostGeometry.seams(s.p(), s.frost(), g, VelorumFrostRules.ring(1.5D, seed + 1), rim, 1.0F, seed + 1);
				rimeFan(s, g, 1.0D, 1.5D, -180.0D, 180.0D, rim, 0.8F, seed + 2);
			}
		}
		wrists(s, player, shown, seed + 3);
		VelorumCruorGeometry.points(s, feet, player.getBbHeight(), player.getBbWidth(), tell, seed + 4);
	}

	/** A beat on a still or cold challenger: cruor spines burst out of their own blood, with chips and a frost shock. */
	private static void burst(Scene s, VelorumCueLog.Entry entry, double now) {
		Entity body = s.level().getEntity(entry.entity());
		float age = entry.age(now);
		Vec3 feet = body == null ? entry.packet().from() : body.getPosition(s.partial());
		double height = body == null ? 1.8D : body.getBbHeight(), width = body == null ? 0.6D : body.getBbWidth();
		int seed = entry.entity() * 31 + (int) entry.born();
		VelorumCruorGeometry.spines(s, feet, height, width, VelorumVisualRules.spines(age), 1.0F, seed);
		if (s.solid()) {
			VelorumIceGeometry.chips(s.p(), s.ice(), s.local(feet.add(0.0D, height * 0.5D, 0.0D)), age - 1.0F, 1.2D, 6,
					VelorumIceGeometry.CRUOR_SPLINTER, seed + 9);
		}
		if (s.floor()) frostShock(s, new Ground(feet.x, feet.z, 1.0D, 0.0D, feet.y, s.camera()), feet, age, 1.8D, seed + 11);
	}

	/** A beat walked through: the frost at the wrists cracks off in pale chips. */
	private static void passed(Scene s, VelorumCueLog.Entry entry, double now) {
		if (!s.solid() || !(s.level().getEntity(entry.entity()) instanceof Player player)) return;
		float age = entry.age(now);
		Vec3 feet = player.getPosition(s.partial());
		int seed = player.getId() * 37 + (int) entry.born();
		for (int i = 0; i < 6; i++) {
			float own = age - i * 0.6F;
			if (own < 0.0F || own > VelorumVisualRules.PASS) continue;
			double a = VelorumFrostRules.unit(seed, i) * Math.PI * 2.0D;
			double y = player.getBbHeight() * 0.42D - 0.03D * own * own;
			Vec3 at = feet.add(Math.cos(a) * (0.36D + 0.03D * own), Math.max(0.03D, y), Math.sin(a) * (0.36D + 0.03D * own));
			VelorumIceGeometry.chip(s.p(), s.ice(), s.local(at), 0.045D * (1.0D - own / VelorumVisualRules.PASS), own * 0.7D,
					VelorumIceGeometry.HOARFROST, seed + i);
		}
	}

	// ── Cruor Javelins ───────────────────────────────────────────────

	/** A lodged shaft: where its head stands, which way it points, the floor, when it landed, and whether it struck rime. */
	private record Lodged(Vec3 head, Vec3 dir, double floor, double born, boolean rime) {
	}

	/** Lodged shafts by Saint, move serial and shaft, so they stand their 40 ticks after the move has ended. */
	private static final Map<String, Lodged> LODGED = new HashMap<>();

	/**
	 * Cruor Javelins: the cruor is drawn up the staff from the seams under his heel into the cage, where the shafts freeze
	 * together one after another; their lines are scored into the floor as frost grooves with a cruor bead running along
	 * each, tracking until the commit (the footprint's tethers, cut at the first pillar, figure or wall). Loosed four ticks
	 * apart, each flies point-first shedding powder, and lodges in the floor with a rime collar or shatters on rime.
	 */
	private static void javelins(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Union union)) return;
		List<Vec3> ends = m.frame().anchors();
		int count = union.parts().size();
		Vec3 origin = m.frame().origin();
		Vec3 cage = anchor(m.boss(), s.partial(), Limb.STAFF);
		Vec3 heel = m.boss().getPosition(s.partial());
		Look look = m.look();
		if (m.age() < 0.0F && s.floor()) {
			float climb = VelorumVisualRules.climb(m.moveTime());
			VelorumCruorGeometry.seams(s, m.ground().at(heel), 0.0D, 1.4D, -180.0D, 180.0D, Field.of(climb), 0.3F, 1.0F, m.seed() + 1);
			Vec3 foot = heel.add(0.0D, 0.05D, 0.0D);
			LuxUmbraGeometry.ribbonPath(s.p(), s.cruor(), List.of(s.local(foot), s.local(foot.lerp(cage, climb))), Vec3.ZERO, 0.13D, 0.08D,
					ThermalGeometry.FROST_STRAND, m.seed() + 2, 0xFFFFFF, 0.9F);
		}
		for (int i = 0; i < Math.min(count, ends.size()); i++) {
			BossFootprint.Union.Part part = union.parts().get(i);
			Vec3 end = ends.get(i);
			double dx = end.x - origin.x, dz = end.z - origin.z, reach = Math.hypot(dx, dz);
			if (reach < 0.5D) continue;
			Vec3 dir = new Vec3(dx / reach, 0.0D, dz / reach);
			int seed = m.seed() + 700 + i * 23;
			Ground g = new Ground(origin.x, origin.z, dir.x, dir.z, m.floorY(), s.camera());
			float since = m.age() - part.delay();
			if (since < 0.0F) {
				if (s.floor() && look.visible()) {
					VelorumFrostGeometry.seams(s.p(), s.frost(), g, List.of(new Seam(new double[]{0.9D, reach}, new double[]{0.0D, 0.0D},
							VelorumFrostRules.MAIN, 0.0D, 1.0D)), new Look(look.grow(), look.frost(), 1.0F, look.glint(), look.width() * 2.2F,
						look.alpha()), 1.0F, seed);
				}
				if (s.solid() && look.visible()) {
					double run = (s.time() * 0.06D + i * 0.31D) % 1.0D;
					double d = 0.9D + (reach - 0.9D) * run;
					VelorumIceGeometry.chip(s.p(), s.ice(), s.local(g.wx(d, 0.0D), m.floorY() + 0.1D, g.wz(d, 0.0D)), 0.14D * look.alpha(),
							s.time() * 0.1D, VelorumIceGeometry.CRUOR_SHAFT, seed + 1);
				}
				// held formed at the cage until it is loosed
				float formed = VelorumVisualRules.formed(m.moveTime(), i, count);
				if (formed <= 0.02F) continue;
				Vec3 across = new Vec3(-dir.z, 0.0D, dir.x);
				Vec3 at = cage.add(across.scale((i - (count - 1) * 0.5D) * 0.3D)).add(0.0D, (i % 2 == 0 ? 0.12D : -0.12D), 0.0D);
				Vec3 aim = new Vec3(end.x, m.floorY() + 1.1D, end.z).subtract(at).normalize();
				VelorumCruorGeometry.javelin(s, at.subtract(aim.scale(0.3D)), at.add(aim.scale(VelorumVisualRules.SHAFT_LENGTH * formed - 0.3D)),
						formed, 0.0F, false, seed);
				continue;
			}
			double flown = VelorumVisualRules.flown(since, reach);
			float lands = VelorumVisualRules.lands(reach);
			boolean rime = blocked(s, m.boss(), origin, end, m.floorY());
			if (since < lands) {
				double t = flown / reach;
				double dive = t > 0.82D && !rime ? (t - 0.82D) / 0.18D * 1.2D : 0.0D;
				Vec3 head = new Vec3(g.wx(flown, 0.0D), m.floorY() + HemoEasing.lerp(2.0D, 1.05D, t) - dive, g.wz(flown, 0.0D));
				Vec3 back = new Vec3(dir.x, dive > 0.0D ? 0.5D : 0.04D, dir.z).normalize();
				VelorumCruorGeometry.javelin(s, head.subtract(back.scale(VelorumVisualRules.SHAFT_LENGTH)), head, 1.0F, 0.0F, true, seed);
				continue;
			}
			String key = m.boss().getId() + ":" + m.boss().syncedMoveSerial() + ":" + i;
			if (!LODGED.containsKey(key)) {
				Vec3 head = new Vec3(end.x, m.floorY() + (rime ? 1.05D : -0.15D), end.z);
				Vec3 axis = new Vec3(dir.x, rime ? -0.04D : -0.42D, dir.z).normalize();
				LODGED.put(key, new Lodged(head, axis, m.floorY(), s.time() - (since - lands), rime));
			}
		}
	}

	/** Whether a shaft's line was cut short by a pillar, a figure or a wall: it then strikes rime and shatters. */
	private static boolean blocked(Scene s, VelorumEntity boss, Vec3 from, Vec3 end, double floorY) {
		Vec3 a = new Vec3(from.x, floorY + 1.3D, from.z), b = new Vec3(end.x, floorY + 1.3D, end.z);
		double length = a.distanceTo(b);
		if (length < 0.5D) return false;
		Vec3 past = a.add(b.subtract(a).scale((length + 0.7D) / length));
		BlockHitResult hit = s.level().clip(new ClipContext(a, past, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, boss));
		return hit.getType() != HitResult.Type.MISS && hit.getLocation().distanceTo(a) > length - 1.0D;
	}

	/** The lodged shafts: standing in their rime collars for 40 ticks, then shattering; on rime, shattered at once. */
	private static void lodged(Scene s) {
		double now = s.time();
		LODGED.values().removeIf(l -> now - l.born() > VelorumVisualRules.LODGE + VelorumVisualRules.SHATTER + 4 || now < l.born() - 1.0D);
		for (Lodged l : LODGED.values()) {
			float since = (float) (now - l.born());
			float shatter = VelorumVisualRules.shatter(since, l.rime());
			int seed = (int) (l.head().x * 31.0D + l.head().z * 17.0D);
			if (l.rime()) {
				VelorumCruorGeometry.shards(s, l.head(), since, seed);
				if (since < 6.0F) VelorumCruorGeometry.fingers(s, l.head(), l.dir().scale(-1.0D), 0.5D * (1.0F - since / 6.0F), 1.0F - since / 6.0F,
						seed + 3);
				continue;
			}
			Vec3 tail = l.head().subtract(l.dir().scale(VelorumVisualRules.SHAFT_LENGTH));
			VelorumCruorGeometry.javelin(s, tail, l.head(), 1.0F, shatter, false, seed);
			double into = l.dir().y < -0.05D ? (l.floor() - l.head().y) / l.dir().y : 0.0D;
			Vec3 foot = l.head().add(l.dir().scale(into));
			VelorumCruorGeometry.collar(s, new Vec3(foot.x, l.floor(), foot.z), HemoEasing.clamp01(since / 3.0F), 1.0F - shatter, seed + 5);
			if (since < 7.0F) VelorumCruorGeometry.fingers(s, new Vec3(foot.x, l.floor() + 0.04D, foot.z), new Vec3(0.0D, 1.0D, 0.0D),
					0.7D * HemoEasing.clamp01(since / 3.0F), 1.0F - since / 7.0F, seed + 7);
			if (shatter > 0.0F) VelorumCruorGeometry.shards(s, tail.lerp(l.head(), 0.5D), since - VelorumVisualRules.LODGE, seed + 9);
		}
	}

	// ── Veins of Frost ───────────────────────────────────────────────

	/**
	 * Veins of Frost: from each feeding figure a frozen vein runs along the floor's block seams toward the target (a
	 * raised cruor ridge with a frost crust, a bright bead at its front), all fronts arriving together at the commit; the
	 * meeting disc freezes a ring and its seams fill. At the commit spines erupt along every vein and a cluster in the
	 * disc. The figures' veins flaring through their rime are drawn from their {@code VEIN_FLARE} cues.
	 */
	private static void veins(Scene s, Move m) {
		List<Vec3> points = m.frame().points();
		if (points.isEmpty()) return;
		Vec3 meet = points.get(0);
		Look look = m.look();
		float run = VelorumVisualRules.veinRun(m.moveTime());
		Ground g = m.ground().at(meet);
		if (s.floor() && look.visible()) {
			VelorumFrostGeometry.seams(s.p(), s.frost(), g, VelorumFrostRules.ring(2.0D, m.seed() + 1), look, 1.0F, m.seed() + 1);
			VelorumCruorGeometry.seams(s, g, 0.0D, 2.0D, -180.0D, 180.0D, Field.of(run), m.age() < 0.0F ? 0.3F * run : 1.0F, look.alpha(),
					m.seed() + 2);
			if (m.age() >= 0.0F) frostShock(s, g, meet, m.age(), 2.6D, m.seed() + 3);
		}
		List<double[]> veins = veinPaths(points, m.frame().anchors());
		float fade = m.age() < 0.0F ? look.alpha() : 1.0F - HemoEasing.ramp(m.age(), m.active() + m.recovery() - 10.0F, m.active() + m.recovery());
		for (int i = 0; i < veins.size(); i++) {
			double[] xz = veins.get(i);
			int seed = m.seed() + 800 + i * 19;
			VelorumCruorGeometry.vein(s, xz, m.floorY(), VelorumFrostGeometry.length(xz) * run, 0.4F + 0.6F * m.build(), fade, seed);
			if (m.age() >= 0.0F) VelorumCruorGeometry.erupt(s, xz, m.floorY(), m.age(), m.crumble(0.0F), seed + 1);
		}
		if (m.age() >= 0.0F && s.solid()) {
			VelorumIceGeometry.cluster(s.p(), s.ice(), s.local(meet.x, m.floorY(), meet.z), VelorumFrostRules.cluster(m.seed() + 9, 1.6D, 6, 0.6D, 5),
					m.age(), m.crumble(0.0F), VelorumIceGeometry.CRUOR_SPINE, VelorumIceGeometry.SHARD, true, m.seed() + 9);
		}
	}

	/**
	 * Each vein of a Veins of Frost path as world points {x, z, ...}, from its figure to the meeting point: the move's
	 * path starts at the meeting point and runs out to each figure (an anchor) and back.
	 */
	static List<double[]> veinPaths(List<Vec3> points, List<Vec3> figures) {
		List<double[]> out = new ArrayList<>();
		if (points.isEmpty()) return out;
		Vec3 meet = points.get(0);
		for (Vec3 figure : figures) {
			int at = -1;
			for (int i = 1; i < points.size(); i++) {
				if (Shapes.planar(points.get(i), figure) < 0.05D) {
					at = i;
					break;
				}
			}
			if (at < 0) continue;
			int from = at - 1;
			while (from > 0 && Shapes.planar(points.get(from), meet) > 0.05D) from--;
			double[] xz = new double[(at - from + 1) * 2];
			for (int k = 0; k <= at - from; k++) {
				Vec3 p = points.get(at - k);
				xz[k * 2] = p.x;
				xz[k * 2 + 1] = p.z;
			}
			out.add(xz);
		}
		return out;
	}

	/** A figure's veins showing black-red through its rime (an overlay, so restrained), or running bright and warm as it wakes. */
	private static void veinsOn(Scene s, Vec3 base, float strength, boolean warm, int seed) {
		if (!s.floor() || strength <= 0.01F) return;
		for (int i = 0; i < 5; i++) {
			double a = i * Math.PI * 2.0D / 5.0D + VelorumFrostRules.unit(seed, i);
			double r = 0.3D;
			Vec3 foot = base.add(Math.cos(a) * r, 0.15D, Math.sin(a) * r);
			Vec3 mid = base.add(Math.cos(a + 0.25D) * r * 0.95D, 0.75D + 0.2D * VelorumFrostRules.unit(seed, 10 + i), Math.sin(a + 0.25D) * r * 0.95D);
			Vec3 top = base.add(Math.cos(a + 0.1D) * r * 0.8D, 1.35D + 0.2D * VelorumFrostRules.unit(seed, 20 + i), Math.sin(a + 0.1D) * r * 0.8D);
			List<Vec3> vein = List.of(s.local(foot), s.local(mid), s.local(top));
			if (warm) {
				LuxUmbraGeometry.ribbonPath(s.p(), s.lux(), vein, Vec3.ZERO, 0.04D, 0.016D, LuxUmbraGeometry.RIBBON, seed + i, LuxUmbraGeometry.CRIMSON,
						0.85F * strength);
			} else {
				LuxUmbraGeometry.ribbonPath(s.p(), s.cruor(), vein, Vec3.ZERO, 0.045D, 0.02D, ThermalGeometry.FROST_STRAND, seed + i, 0xFFFFFF,
						0.8F * strength);
			}
		}
	}

	// ── the Shadow Step ──────────────────────────────────────────────

	/**
	 * The Shadow Step: he bows and sinks straight down into the dark (the sink is the renderer's) while torn smoke rises
	 * round him; among the congregation a torn column of liquid dark rises out of the pool he will come up from (the
	 * tell). At the commit he pours up out of the tear ({@code UmbralTearGeometry}'s arrival, scaled to his height) and a
	 * low wave of torn dark spreads from it: the jumpable arrival ring. The pools at both ends are the server's hazards.
	 */
	private static void step(Scene s, Move m) {
		List<Vec3> points = m.frame().points();
		Vec3 dest = points.isEmpty() ? m.frame().origin() : points.get(0);
		Vec3 him = m.boss().getPosition(s.partial());
		Look look = m.look();
		if (m.age() < 0.0F) {
			if (!s.floor()) return;
			float sink = (float) (VelorumVisualRules.sunk(m.moveTime()) / VelorumVisualRules.SINK_DEPTH);
			float come = HemoEasing.clamp01(m.moveTime() / 6.0F);
			for (int i = 0; i < 8; i++) {
				double a = i * Math.PI / 4.0D + m.seed() * 0.1D;
				double phase = (s.time() / 14.0D + VelorumFrostRules.unit(m.seed(), i)) % 1.0D;
				VelorumDarkGeometry.tatter(s, new Vec3(him.x + Math.cos(a) * 0.75D, m.floorY(), him.z + Math.sin(a) * 0.75D), 0.35D,
						(0.8D + 1.4D * phase) * (0.4D + sink), (float) phase, 0.8F * come * (float) Math.sin(phase * Math.PI), m.seed() + 20 + i);
			}
			float column = HemoEasing.ramp(m.moveTime(), 6.0F, m.windup());
			for (int k = 0; k < 6; k++) {
				double a = k * Math.PI / 3.0D + m.seed();
				double phase = (s.time() / 18.0D + k / 6.0D) % 1.0D;
				VelorumDarkGeometry.tatter(s, new Vec3(dest.x + Math.cos(a) * 0.4D, m.floorY(), dest.z + Math.sin(a) * 0.4D), 0.42D,
						(1.0D + 1.6D * column) * (0.7D + 0.3D * phase), (float) (0.2D + 0.6D * phase), 0.85F * column * look.alpha(),
						m.seed() + 40 + k);
			}
			return;
		}
		if (!s.floor()) return;
		tear(s, dest, m.age());
		BossFootprint shape = m.footprint();
		int delay = 0;
		if (shape instanceof BossFootprint.Union union && !union.parts().isEmpty()) {
			delay = union.parts().get(0).delay();
			shape = union.parts().get(0).shape();
		}
		if (shape instanceof BossFootprint.Ring wave) {
			double r = wave.radius(m.age() - delay, 0);
			if (r > 0.0D) {
				float left = 1.0F - (float) ((r - wave.from()) / Math.max(0.1D, wave.to() - wave.from())) * 0.5F;
				arrivalWave(s, m.frame().origin(), m.floorY(), r, wave.band(), left, m.seed() + 70);
			}
		}
	}

	/** The tear he pours up out of: {@code UmbralTearGeometry}'s arrival, scaled to his height. */
	private static void tear(Scene s, Vec3 at, float age) {
		if (age < 0.0F || age > UmbralTearGeometry.ARRIVE_TICKS) return;
		double scale = VelorumEntity.HEIGHT / UmbralTearGeometry.HEIGHT;
		ManipulationVisualPacket packet = new ManipulationVisualPacket(ManipulationVisuals.Form.UMBRA_ARRIVAL, -1, at, at, 1.0F,
				UmbralTearGeometry.ARRIVE_TICKS, 1);
		Vec3 local = s.local(at);
		s.p().pushPose();
		s.p().translate(local.x, local.y, local.z);
		s.p().scale((float) scale, (float) scale, (float) scale);
		UmbralTearGeometry.draw(packet, s.p(), s.shade(), s.camera().subtract(at).scale(1.0D / scale), s.right(), s.up(), s.time(), age, 1.0F);
		s.p().popPose();
	}

	/** A low wave of torn dark running out over the floor, {@code band} thick, its smoke knee-high (it is jumped). */
	private static void arrivalWave(Scene s, Vec3 centre, double floorY, double radius, double band, float alpha, int seed) {
		Ground g = new Ground(centre.x, centre.z, 1.0D, 0.0D, floorY, s.camera());
		Field cover = (along, right) -> (float) HemoEasing.clamp01(1.0D - Math.abs(Math.hypot(along, right) - radius) / (band * 0.6D)) * alpha;
		VelorumFrostGeometry.annulus(s.p(), s.frost(), g, VelorumFrostGeometry.DARK, Math.max(0.0D, radius - band), radius + band * 0.5D, -180.0D,
				180.0D, Field.of(1.0F), 0.0F, 0.0F, cover, seed);
		int count = Math.max(10, (int) (radius * 6.0D));
		for (int i = 0; i < count; i++) {
			double a = i * Math.PI * 2.0D / count + seed * 0.1D;
			VelorumDarkGeometry.tatter(s, new Vec3(centre.x + Math.cos(a) * radius, floorY, centre.z + Math.sin(a) * radius), 0.34D, 0.75D, 0.3F,
					0.85F * alpha, seed + i);
		}
	}

	// ── Gloam Cuts ───────────────────────────────────────────────────

	/**
	 * Gloam Cuts: liquid dark gathers on the raised claw's fingers, smoking, while the paths show on the floor (the low
	 * cuts as torn dark bands lying on it, the high cut as a frost line under a hanging smoke edge); then three crescents
	 * of liquid dark fly out along the footprint's spokes, two low and one at face height, each settling into a pool.
	 */
	private static void gloamCuts(Scene s, Move m) {
		if (!(m.footprint() instanceof BossFootprint.Union union)) return;
		Look look = m.look();
		Vec3 hand = anchor(m.boss(), s.partial(), Limb.CLAW);
		if (m.age() < 0.0F && s.floor()) {
			float gather = HemoEasing.clamp01(m.moveTime() / VelorumVisualRules.GATHER);
			for (int k = 0; k < 4; k++) {
				double phase = (s.time() / 10.0D + k / 4.0D) % 1.0D;
				VelorumDarkGeometry.tatter(s, hand.add((k - 1.5D) * 0.06D, -0.15D, 0.0D), 0.14D + 0.12D * gather, 0.35D + 0.5D * phase * gather,
						(float) phase, 0.85F * gather * (float) Math.sin(phase * Math.PI), m.seed() + k);
			}
			LuxUmbraGeometry.card(s.p(), s.shade(), s.local(hand), s.right(), s.up(), 0.22D * gather, 0.22D * gather, LuxUmbraGeometry.WISP,
					m.seed() + 9, VelorumDarkGeometry.INK, 0.8F * gather);
		}
		for (int i = 0; i < union.parts().size(); i++) {
			BossFootprint.Union.Part part = union.parts().get(i);
			if (!(part.shape() instanceof BossFootprint.Spokes spoke)) continue;
			boolean high = spoke.height().max() > 1.5D;
			Ground g = m.ground().lane(0.0D, spoke.angle(0));
			int seed = m.seed() + 900 + i * 13;
			float since = m.age() - part.delay();
			if (since < 0.0F) {
				if (!s.floor() || !look.visible()) continue;
				float shown = m.age() < 0.0F ? m.build() : 1.0F;
				double half = spoke.halfWidth();
				if (!high) {
					Field band = (along, right) -> (float) HemoEasing.clamp01((half - Math.abs(right)) / 0.6D + 0.2D) * 0.75F * shown;
					VelorumFrostGeometry.rect(s.p(), s.frost(), g, VelorumFrostGeometry.DARK, 0.8D, spoke.length(), -half - 0.3D, half + 0.3D,
							Field.of(1.0F), 0.0F, 0.0F, band, seed);
				} else {
					VelorumFrostGeometry.seams(s.p(), s.frost(), g, List.of(new Seam(new double[]{0.8D, spoke.length()}, new double[]{0.0D, 0.0D},
							VelorumFrostRules.EDGE, 0.0D, 1.0D)), look.withIce(0.0F), 1.0F, seed);
					for (double d = 1.4D; d < spoke.length(); d += 1.6D) {
						VelorumDarkGeometry.tatter(s, new Vec3(g.wx(d, 0.0D), m.floorY() + 1.55D, g.wz(d, 0.0D)), half * 0.45D, 1.0D, 0.3F,
								0.5F * shown * look.alpha(), seed + (int) (d * 3.0D));
					}
				}
				continue;
			}
			double front = VelorumVisualRules.cutFront(since, spoke.length(), Math.max(0.01D, spoke.frontSpeed()));
			if (front < 0.0D || !s.floor()) continue;
			VelorumDarkGeometry.cut(s, g, front, spoke.halfWidth(), high, VelorumVisualRules.cutFade(since), since < 2.0F ? 1.0F : 0.4F, seed);
		}
	}

	// ── the Eclipse ──────────────────────────────────────────────────

	/**
	 * The Eclipse: as the claw closes over the cage his light goes out and the floor's block seams fill with dull glowing
	 * cruor, a red grid across the whole floor (the cruor everywhere glows with it through {@code CruorGlow}), and every
	 * challenger's own Rime crust glows at their feet; so the room is dark but the floor stays readable. The dark itself
	 * is the Eclipse's pool, drawn first; the shapes and their reaches are the congregation's hazards.
	 */
	private static void eclipse(Scene s, Move m) {
		if (!s.floor()) return;
		float out = VelorumDarkSight.eclipse(m.boss(), s.partial());
		float grid;
		if (m.age() < 0.0F) grid = VelorumVisualRules.grid(m.moveTime() * VelorumVisualRules.ECLIPSE_WINDUP / Math.max(1.0F, m.windup()));
		else if (m.age() <= m.active()) grid = 1.0F;
		else grid = 1.0F - HemoEasing.clamp01((m.age() - m.active()) / Math.max(1.0F, m.recovery()));
		VelorumDarkGeometry.grid(s, m.frame().bossFloor(), m.floorY(), grid, m.seed());
		Vec3 lantern = anchor(m.boss(), s.partial(), Limb.STAFF);
		float light = 1.0F - out;
		if (light > 0.02F) {
			LuxUmbraGeometry.card(s.p(), s.lux(), s.local(lantern), s.right(), s.up(), 0.5D * light, 0.5D * light, LuxUmbraGeometry.FOCUS,
					m.seed() + 1, CORE, 0.8F * light);
		}
		if (grid <= 0.05F) return;
		for (Player player : s.level().players()) {
			if (player.isSpectator() || !player.isAlive() || player.distanceToSqr(m.boss()) > 40.0D * 40.0D) continue;
			Vec3 feet = player.getPosition(s.partial());
			Ground g = new Ground(feet.x, feet.z, 1.0D, 0.0D, feet.y, s.camera());
			VelorumCruorGeometry.seamsRect(s, g, -0.9D, 0.9D, -0.9D, 0.9D, Field.of(1.0F), 1.0F, grid, grid, player.getId() * 3);
			LuxUmbraGeometry.ground(s.p(), s.lux(), s.local(feet.add(0.0D, 0.04D, 0.0D)), 0.7D, LuxUmbraGeometry.POOL, player.getId(),
					LuxUmbraGeometry.CRIMSON, 0.22F * grid);
		}
	}

	/**
	 * An Eclipse shape standing up out of its figure (the shape is the devotee model, the renderer's): the figure's veins
	 * glowing through its rime, torn smoke lifting off the shape as it rises; a pinned figure's shape has the Apotheos's
	 * hyphae binding its arms.
	 */
	private static void shapeOfDark(Scene s, BossGroundHazardEntity hazard, VelorumEntity boss) {
		float age = AnimationTime.age(hazard, s.partial());
		float fade = SaintTelegraphTiming.hazardFade(age, hazard.life(), hazard.fade(), hazard.fading(), hazard.fadeStart());
		if (fade <= 0.0F || !s.floor()) return;
		float rise = VelorumVisualRules.shapeRise(age) * fade;
		Vec3 foot = hazard.frame().origin();
		int seed = hazard.getId() * 41;
		veinsOn(s, foot, fade, false, seed);
		double height = 2.4D * rise;
		for (int k = 0; k < 6; k++) {
			double a = k * Math.PI / 3.0D + seed;
			double phase = (s.time() / 20.0D + k / 6.0D) % 1.0D;
			double y = foot.y + height * (0.2D + 0.6D * (k / 5.0D));
			VelorumDarkGeometry.tatter(s, new Vec3(foot.x + Math.cos(a) * 0.35D, y, foot.z + Math.sin(a) * 0.35D), 0.3D, 0.6D + 0.5D * phase,
					(float) (0.3D + 0.6D * phase), 0.7F * rise * (float) Math.sin(phase * Math.PI), seed + k);
		}
		if (!VelorumDarkSight.held(hazard)) return;
		for (int k = 0; k < 4; k++) {
			double a = k * Math.PI / 2.0D + seed;
			Vec3 root = new Vec3(foot.x + Math.cos(a) * 0.6D, foot.y + 0.02D, foot.z + Math.sin(a) * 0.6D);
			LuxUmbraGeometry.ribbonPath(s.p(), s.rime(), List.of(s.local(root), s.local(foot.add(Math.cos(a) * 0.25D, 1.2D * rise, Math.sin(a) * 0.25D))),
					Vec3.ZERO, 0.04D, 0.015D, LuxUmbraGeometry.RIBBON, seed + 10 + k, 0xD8D2C6, 0.55F * rise);
		}
	}

	/**
	 * The congregation reaching together: the arm's line revealed along the floor (a torn band of dark over glowing seams)
	 * for its 20 ticks, then the arm thrown along it, a torn smoke limb ending in a cruor-red hand.
	 */
	private static void reachingArm(Scene s, BossGroundHazardEntity hazard, VelorumEntity boss) {
		float age = AnimationTime.age(hazard, s.partial());
		float fade = SaintTelegraphTiming.hazardFade(age, hazard.life(), hazard.fade(), hazard.fading(), hazard.fadeStart());
		if (fade <= 0.0F) return;
		BossFootprint shape = hazard.shape();
		int delay = 0;
		if (shape instanceof BossFootprint.Union union && !union.parts().isEmpty()) {
			delay = union.parts().get(0).delay();
			shape = union.parts().get(0).shape();
		}
		if (!(shape instanceof BossFootprint.Lane lane)) return;
		FootprintFrame frame = hazard.frame();
		Ground g = Ground.of(frame.origin(), Shapes.forward(frame), frame.floorY(), s.camera());
		int seed = hazard.getId() * 43;
		float reveal = HemoEasing.clamp01(age / Math.max(1.0F, delay));
		if (s.floor()) {
			double half = lane.halfWidth();
			Field band = (along, right) -> (float) HemoEasing.clamp01((half - Math.abs(right)) / 0.4D + 0.4D) * 0.6F * reveal * fade;
			VelorumFrostGeometry.rect(s.p(), s.frost(), g, VelorumFrostGeometry.DARK, 0.4D, lane.length(), -half - 0.4D, half + 0.4D, Field.of(1.0F),
					0.0F, 0.0F, band, seed);
			VelorumCruorGeometry.seamsRect(s, g, 0.4D, lane.length(), -half, half, Field.of(reveal), 1.0F, reveal, fade, seed + 1);
		}
		double front = lane.front(age - delay, 0);
		float thrown = front < 0.0D ? 0.12F * reveal : (float) (front / Math.max(0.1D, lane.length()));
		Vec3 to = new Vec3(g.wx(lane.length(), 0.0D), frame.floorY(), g.wz(lane.length(), 0.0D));
		List<Vec3> joints = VelorumDarkGeometry.limb(s, frame.origin().add(0.0D, 1.9D, 0.0D), to, frame.floorY(), Math.max(0.05F, thrown), fade,
				seed + 2);
		if (!joints.isEmpty()) {
			VelorumCruorGeometry.hand(s, joints.get(joints.size() - 1), to.subtract(frame.origin()), HemoEasing.clamp01(thrown * 3.0F) * fade, seed + 3);
		}
	}

	// ── the logged beats ─────────────────────────────────────────────

	/** Every live beat from {@link VelorumCueLog}, in this phase. */
	private static void cues(Scene s) {
		double now = s.time();
		for (VelorumCueLog.Entry e : VelorumCueLog.of(VelorumEffects.STAIN, now)) stain(s, e, now);
		for (VelorumCueLog.Entry e : VelorumCueLog.of(VelorumEffects.PULSE_MARK, now)) mark(s, e, now);
		for (VelorumCueLog.Entry e : VelorumCueLog.of(VelorumEffects.PULSE_BEAT, now)) passed(s, e, now);
		for (VelorumCueLog.Entry e : VelorumCueLog.of(VelorumEffects.PULSE_BURST, now)) burst(s, e, now);
		for (VelorumCueLog.Entry e : VelorumCueLog.of(VelorumEffects.VEIN_FLARE, now)) {
			float age = e.age(now);
			float strength = HemoEasing.clamp01(age / VelorumVisualRules.VEIN_FLARE) * (1.0F - HemoEasing.ramp(age, 30.0F, 40.0F));
			veinsOn(s, e.packet().from(), strength, false, Math.floorMod(e.packet().from().hashCode(), 977));
		}
		for (VelorumCueLog.Entry e : VelorumCueLog.of(VelorumEffects.THAW, now)) {
			// warm blood into frozen cruor: the one warm red in the fight, for six ticks
			float age = e.age(now);
			if (age < VelorumVisualRules.WARM_VEINS) veinsOn(s, e.packet().from(), 1.0F - age / VelorumVisualRules.WARM_VEINS, true,
					Math.floorMod(e.packet().from().hashCode(), 977));
		}
		for (VelorumCueLog.Entry e : VelorumCueLog.of(VelorumEffects.STEP_SINK, now)) sunk(s, e, now);
		for (VelorumCueLog.Entry e : VelorumCueLog.of(VelorumEffects.STEP_RISE, now)) {
			VelorumEntity boss = bossNear(s, e.packet().from());
			if (s.floor() && (boss == null || boss.view().moveId() != VelorumMove.UMBRAL_STEP.id())) tear(s, e.packet().from(), e.age(now));
		}
		for (VelorumCueLog.Entry e : VelorumCueLog.of(VelorumEffects.GLOAM_CUT, now)) {
			VelorumEntity boss = bossNear(s, e.packet().from());
			if (boss == null || !s.floor()) continue;
			float age = e.age(now);
			Vec3 hand = anchor(boss, s.partial(), Limb.CLAW);
			for (int k = 0; k < 3; k++) {
				VelorumDarkGeometry.tatter(s, hand.add(0.0D, -0.3D, 0.0D), 0.2D + 0.1D * k, 0.4D + 0.08D * age, 0.3F + age / 12.0F,
						0.7F * (1.0F - age / 10.0F), boss.getId() + k + (int) e.packet().radius() * 5);
			}
		}
		for (VelorumCueLog.Entry e : VelorumCueLog.of(VelorumEffects.ECLIPSE_OUT, now)) {
			float age = e.age(now);
			if (s.floor() && age < 8.0F) LuxUmbraGeometry.card(s.p(), s.lux(), s.local(e.packet().from()), s.right(), s.up(), 0.7D * (1.0F - age / 8.0F),
					0.7D * (1.0F - age / 8.0F), LuxUmbraGeometry.FOCUS, 23, CORE, 0.9F * (1.0F - age / 8.0F));
		}
		for (VelorumCueLog.Entry e : VelorumCueLog.of(VelorumEffects.ECLIPSE_RELIT, now)) relit(s, e, now);
		for (VelorumCueLog.Entry e : VelorumCueLog.of(VelorumEffects.SHAPE_REACH, now)) reach(s, e, now);
	}

	/** A stain of blood a challenger spilled where they stood still, deepening cue by cue. */
	private static void stain(Scene s, VelorumCueLog.Entry e, double now) {
		float age = e.age(now);
		float depth = HemoEasing.clamp01(e.packet().radius());
		float alpha = HemoEasing.clamp01(age / 3.0F) * (1.0F - HemoEasing.ramp(age, VelorumVisualRules.STAIN_LIFE - 5.0F, VelorumVisualRules.STAIN_LIFE));
		Vec3 at = e.packet().from();
		VelorumCruorGeometry.stain(s, at, depth, 0.0F, 0.0F, alpha * (0.6F + 0.4F * depth), Math.floorMod((int) (at.x * 13.0D + at.z * 7.0D), 997));
	}

	/** The phase-3 card's sink into the dark (his move's sink is drawn from the move): the dark welling, torn smoke rising. */
	private static void sunk(Scene s, VelorumCueLog.Entry e, double now) {
		VelorumEntity boss = bossNear(s, e.packet().from());
		if (!s.floor() || boss != null && boss.view().moveId() == VelorumMove.UMBRAL_STEP.id()) return;
		float age = e.age(now);
		Vec3 at = e.packet().from();
		float strength = 1.0F - HemoEasing.ramp(age, 22.0F, 30.0F);
		VelorumDarkGeometry.disc(s, at, at.y, Math.min(2.0D, 0.3D + age * 0.2D), strength, Light.NONE, 501);
		for (int i = 0; i < 8; i++) {
			double a = i * Math.PI / 4.0D;
			double phase = (s.time() / 14.0D + i / 8.0D) % 1.0D;
			VelorumDarkGeometry.tatter(s, at.add(Math.cos(a) * 0.75D, 0.0D, Math.sin(a) * 0.75D), 0.35D, 0.8D + 1.6D * phase, (float) phase,
					0.8F * strength * (float) Math.sin(phase * Math.PI), 520 + i);
		}
	}

	/** The Eclipse relit: the light bursts through his fingers, and a ring of torn dark is blown back to the walls. */
	private static void relit(Scene s, VelorumCueLog.Entry e, double now) {
		if (!s.floor()) return;
		float age = e.age(now);
		Vec3 lantern = e.packet().from(), centre = e.packet().to();
		float burst = 1.0F - HemoEasing.clamp01(age / VelorumVisualRules.RELIT_BURST);
		if (burst > 0.0F) {
			double size = 1.4D * (1.0D - burst * 0.5D);
			LuxUmbraGeometry.card(s.p(), s.lux(), s.local(lantern), s.right(), s.up(), size, size, LuxUmbraGeometry.FOCUS, 31, CORE, burst);
			for (int i = 0; i < 8; i++) {
				double a = i * Math.PI / 4.0D;
				Vec3 ray = lantern.add(Math.cos(a) * 2.2D, Math.sin(a) * 1.2D, Math.sin(a + 1.0D) * 2.2D);
				LuxUmbraGeometry.ribbonPath(s.p(), s.lux(), List.of(s.local(lantern), s.local(lantern.lerp(ray, 0.5D + 0.5D * (1.0F - burst)))),
						Vec3.ZERO, 0.09D, 0.02D, LuxUmbraGeometry.RIBBON, 40 + i, CORE, 0.85F * burst);
			}
		}
		float tear = VelorumVisualRules.tearBack(age);
		if (tear > 0.0F && tear < 1.0F) arrivalWave(s, centre, centre.y, 1.5D + (e.packet().radius() - 1.5D) * tear, 1.6D, 1.0F - tear, 77);
	}

	/**
	 * A shape reaching for one challenger: through its 16-tick tell its arm stretches along the floor toward them as torn
	 * smoke with a cruor-red hand forming at its end; then it grips, while they stay within 6 of the figure.
	 */
	private static void reach(Scene s, VelorumCueLog.Entry e, double now) {
		float age = e.age(now);
		Vec3 foot = e.packet().from();
		Entity target = s.level().getEntity(e.entity());
		Vec3 to = target == null ? e.packet().to() : target.getPosition(s.partial());
		boolean gripping = age >= VelorumVisualRules.REACH_TELL;
		if (gripping && (target == null || Math.hypot(to.x - foot.x, to.z - foot.z) > e.packet().radius())) return;
		float reach = gripping ? 1.0F : VelorumVisualRules.reach(age);
		float fade = 1.0F - HemoEasing.ramp(age, VelorumVisualRules.REACH_TELL + VelorumVisualRules.GRIP - 4.0F,
				VelorumVisualRules.REACH_TELL + VelorumVisualRules.GRIP);
		int seed = e.entity() * 53 + (int) (foot.x * 7.0D);
		List<Vec3> joints = VelorumDarkGeometry.limb(s, foot.add(0.0D, 1.9D, 0.0D), new Vec3(to.x, foot.y, to.z), foot.y, reach, fade, seed);
		if (!joints.isEmpty()) {
			VelorumCruorGeometry.hand(s, joints.get(joints.size() - 1), to.subtract(foot), HemoEasing.clamp01(reach * 1.5F - 0.3F) * fade, seed + 1);
		}
	}

	/** The Velorum nearest a point, within 32 blocks, or null. */
	private static VelorumEntity bossNear(Scene s, Vec3 at) {
		VelorumEntity best = null;
		double distance = 32.0D * 32.0D;
		for (VelorumEntity boss : s.level().getEntitiesOfClass(VelorumEntity.class, new AABB(at, at).inflate(32.0D))) {
			double d = boss.distanceToSqr(at);
			if (d < distance) {
				distance = d;
				best = boss;
			}
		}
		return best;
	}

	/** The living challenger nearest a point, within {@code range}, or null. */
	private static Player nearestPlayer(Scene s, Vec3 at, double range) {
		Player best = null;
		double distance = range * range;
		for (Player player : s.level().players()) {
			if (player.isSpectator() || !player.isAlive()) continue;
			double d = player.distanceToSqr(at);
			if (d < distance) {
				distance = d;
				best = player;
			}
		}
		return best;
	}
}
