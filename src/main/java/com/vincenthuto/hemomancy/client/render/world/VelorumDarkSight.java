package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.client.model.entity.boss.velorum.VelorumAnimState;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint.Shapes;
import com.vincenthuto.hemomancy.common.entity.boss.BossGroundHazardEntity;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintControllerRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveView;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumDevoteeEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEffects;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumOpeningRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumVisualRules;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Velorum's liquid dark as the client sees it (revision 2026-11-06, design R.2 to R.4), for his pass, the gloam fog and
 * his meter: the lantern's light {@code L} as a rule of his synced phase, move and opening; which of his ground hazards
 * are pools of dark, Eclipse shapes or Eclipse reaches; each pool's coverage at a point (grown from its age, flooded by
 * the Gutter, pushed back by his light and by an awake ally's warmth); whether the viewer is in the dark or gloamed; and
 * whether an Arrested Pulse has marked them. Nothing here decides a hit: the server's {@code VelorumDarkRules} does, from
 * the same numbers.
 */
public final class VelorumDarkSight {
	/** An awake ally's thawed blood is warm: it carves a hole this wide in the dark. */
	public static final double WARMTH = 1.5D;
	/** The gloam a struck challenger carries (R.4). */
	public static final float GLOAM_TICKS = 60.0F;
	private static final double SEARCH = 64.0D;

	/** Per Saint: when the last Gutter closed and how long it ran, for the light's return and the pools' ease-back. */
	private static final Map<VelorumEntity, float[]> GUTTERS = new WeakHashMap<>();
	/** Per viewer: when the gloam last fell on them. */
	private static final Map<Player, float[]> GLOAMS = new WeakHashMap<>();

	private VelorumDarkSight() {
	}

	// ── which hazards are his dark (one place, so a renamed kind is one line) ──

	public static boolean pool(BossGroundHazardEntity hazard) {
		return hazard.kind() == VelorumEntity.HAZARD_DARK_POOL;
	}

	/** An Eclipse shape standing out of a figure, free to reach or held by the hyphae ({@link #held}). */
	public static boolean shape(BossGroundHazardEntity hazard) {
		return hazard.kind() == VelorumEntity.HAZARD_ECLIPSE_SHAPE || held(hazard);
	}

	/** A pinned figure's shape: it rises but its arms are held. */
	public static boolean held(BossGroundHazardEntity hazard) {
		return hazard.kind() == VelorumEntity.HAZARD_ECLIPSE_SHAPE_HELD;
	}

	/** The congregation reaching together: an arm thrown along the floor at a challenger's locked place. */
	public static boolean reach(BossGroundHazardEntity hazard) {
		return hazard.kind() == VelorumEntity.HAZARD_ECLIPSE_REACH;
	}

	/** Whether the cruor field (the Release of Cruor's standing trenches) is this hazard. */
	public static boolean field(BossGroundHazardEntity hazard) {
		return hazard.kind() == VelorumEntity.HAZARD_CRUOR_FIELD;
	}

	// ── the lantern's light ──────────────────────────────────────────

	/** The move's effective timeline as both sides read it. */
	static BossMoveTimeline timeline(VelorumMove move, SaintMoveView view) {
		return SaintControllerRules.effective(move.kind(), move.timeline(view.inputs()));
	}

	/** How far the Eclipse has put his light out, 0 to 1 (0 outside it); relit or unbroken, it comes back. */
	public static float eclipse(VelorumEntity boss, float partial) {
		SaintMoveView view = boss.view();
		VelorumMove move = VelorumMove.byId(view.moveId());
		if (move != VelorumMove.ECLIPSE) return 0.0F;
		float t = view.moveTime(partial);
		if (t < 0.0F) return 0.0F;
		BossMoveTimeline timeline = timeline(move, view);
		int windup = timeline.commitTick();
		if (t < windup) return VelorumVisualRules.lightOut(t * VelorumVisualRules.ECLIPSE_WINDUP / Math.max(1.0F, windup));
		float age = t - windup;
		if (age <= timeline.active()) return 1.0F;
		return 1.0F - HemoEasing.clamp01((age - timeline.active()) / 12.0F);
	}

	/** Whether the Eclipse's hold is on: every challenger in the dark but for an ally's warmth. */
	public static boolean eclipseHold(VelorumEntity boss, float partial) {
		SaintMoveView view = boss.view();
		VelorumMove move = VelorumMove.byId(view.moveId());
		if (move != VelorumMove.ECLIPSE) return false;
		float t = view.moveTime(partial);
		BossMoveTimeline timeline = timeline(move, view);
		return t >= timeline.commitTick() && t <= timeline.commitTick() + timeline.active();
	}

	/** Ticks since the Eclipse was last relit (any of his), or -1. */
	static float relit(double now) {
		float best = -1.0F;
		for (VelorumCueLog.Entry entry : VelorumCueLog.of(VelorumEffects.ECLIPSE_RELIT, now)) {
			float age = entry.age(now);
			if (best < 0.0F || age < best) best = age;
		}
		return best;
	}

	/** {@code L}: the radius of his lantern's light on the floor (R.2's table). */
	public static float lantern(VelorumEntity boss, float partial) {
		SaintMoveView view = boss.view();
		double now = AnimationTime.gameTime(boss.level(), partial);
		int phase = Math.max(1, view.inputs().phase());
		float night = 0.0F;
		VelorumMove move = VelorumMove.byId(view.moveId());
		float t = view.moveTime(partial);
		if (move == VelorumMove.LONG_NIGHT && t >= 0.0F) {
			int windup = timeline(move, view).commitTick();
			night = t < windup ? HemoEasing.clamp01(t / Math.max(1.0F, windup)) : 1.0F;
		}
		float[] gutter = gutter(boss, partial, (float) now);
		return VelorumVisualRules.lanternRadius(phase, night, gutter[0], gutter[1], eclipse(boss, partial), relit(now));
	}

	/** {ticks into an open Gutter or -1, ticks since the last one closed or -1, the closed one's length}. */
	static float[] gutter(VelorumEntity boss, float partial, float now) {
		SaintMoveView view = boss.view();
		float[] state = GUTTERS.computeIfAbsent(boss, ignored -> new float[]{Float.NaN, 0.0F, 0.0F});
		boolean open = view.openingId() == VelorumOpeningRules.GUTTER_ID && view.openingTime(partial) >= 0.0F;
		if (open) {
			state[0] = Float.NaN;
			state[1] = view.openingTime(partial);
			state[2] = 1.0F;
			return new float[]{state[1], -1.0F, state[1]};
		}
		if (state[2] > 0.0F && Float.isNaN(state[0])) state[0] = now;
		state[2] = 0.0F;
		float since = Float.isNaN(state[0]) ? -1.0F : now - state[0];
		return new float[]{-1.0F, since, state[1]};
	}

	/** The floor point under his staff's lantern. */
	public static Vec3 lanternFoot(VelorumEntity boss, float partial) {
		Vec3 at = boss.getPosition(partial);
		Vec3[] lanterns = VelorumAnimState.of(boss).lanterns();
		Vec3 lantern = lanterns.length == 0 || lanterns[0] == null ? Vec3.ZERO : lanterns[0];
		return new Vec3(at.x + lantern.x, boss.getY(), at.z + lantern.z);
	}

	// ── a pool's reach ───────────────────────────────────────────────

	/** A pool's radius now, grown from its age and flooded by the Gutter (Disc pools only; 0 otherwise). */
	public static double radius(BossGroundHazardEntity hazard, VelorumEntity boss, float partial) {
		if (!(hazard.shape() instanceof BossFootprint.Disc disc)) return 0.0D;
		float age = AnimationTime.age(hazard, partial);
		double grown = VelorumVisualRules.poolRadius(age, disc.radius());
		if (boss == null) return grown;
		float[] gutter = gutter(boss, partial, (float) AnimationTime.gameTime(boss.level(), partial));
		if (gutter[0] >= 0.0F) return VelorumVisualRules.floodedRadius(grown, Math.min(gutter[0], age), -1.0F);
		if (gutter[1] >= 0.0F) return VelorumVisualRules.floodedRadius(grown, Math.min(gutter[2], age), gutter[1]);
		return grown;
	}

	/** How fully a pool covers a floor point, 0 to 1, before the light and warmth push it back. */
	public static float cover(BossGroundHazardEntity hazard, VelorumEntity boss, double x, double z, float partial) {
		float age = AnimationTime.age(hazard, partial);
		float fade = SaintTelegraphTiming.hazardFade(age, hazard.life(), hazard.fade(), hazard.fading(), hazard.fadeStart());
		if (fade <= 0.0F) return 0.0F;
		FootprintFrame frame = hazard.frame();
		Vec3 point = new Vec3(x, frame.floorY(), z);
		BossFootprint shape = hazard.shape();
		float cover = switch (shape) {
			case BossFootprint.Disc disc -> VelorumVisualRules.poolCover(Shapes.planar(frame.anchor(disc.at()), point), radius(hazard, boss, partial));
			case BossFootprint.Lane lane -> {
				float best = 0.0F;
				for (int n = 0; n < lane.lanes(); n++) {
					int index = lane.index(n);
					double front = lane.front(age, index);
					if (front <= 0.0D) continue;
					double[] local = Shapes.local(frame, frame.origin(), point, lane.angle(index));
					double across = lane.halfWidth() - Math.abs(local[1] - lane.offset(index));
					// a strip runs out behind its shade: dark only as far as its front has come
					double along = Math.min(local[0], front - local[0]);
					best = Math.max(best, (float) HemoEasing.clamp01(Math.min(across / 0.5D, along / 0.5D + 0.5D)));
				}
				yield best;
			}
			case BossFootprint.Path path -> {
				double[] along = Shapes.alongPath(frame.points(), point);
				yield along == null ? 0.0F : (float) HemoEasing.clamp01((path.halfWidth() - along[1]) / 0.5D + 0.5D);
			}
			case BossFootprint.Region region -> {
				AABB box = region.area() == BossFootprint.Area.CHAMBER ? frame.chamber() : frame.bossFloor();
				double edge = Math.min(Math.min(x - box.minX, box.maxX - x), Math.min(z - box.minZ, box.maxZ - z));
				yield (float) HemoEasing.clamp01(edge / 1.5D + 0.3D);
			}
			default -> 0.0F;
		};
		return cover * fade;
	}

	/** How far his light and an ally's warmth push the dark back at a point: 0 none to 1 wholly lit. */
	public static float pushed(Level level, VelorumEntity boss, double x, double z, float lantern, Vec3 foot,
			List<VelorumDevoteeEntity> allies, float partial) {
		float lit = VelorumVisualRules.lit(Math.hypot(x - foot.x, z - foot.z), lantern);
		for (VelorumDevoteeEntity ally : allies) {
			Vec3 at = ally.getPosition(partial);
			lit = Math.max(lit, VelorumVisualRules.lit(Math.hypot(x - at.x, z - at.z), WARMTH - 0.6D));
		}
		return lit;
	}

	/** The awake allies near a point, whose warmth holds the dark off. */
	public static List<VelorumDevoteeEntity> allies(Level level, Vec3 near, double range) {
		return level.getEntitiesOfClass(VelorumDevoteeEntity.class, new AABB(near, near).inflate(range),
				devotee -> devotee.isAlive() && devotee.awake() && !devotee.hostile());
	}

	// ── the viewer ───────────────────────────────────────────────────

	/** The Velorum the viewer is fighting, if one is near. */
	public static VelorumEntity near(Player viewer) {
		VelorumEntity best = null;
		double distance = SEARCH * SEARCH;
		for (VelorumEntity boss : viewer.level().getEntitiesOfClass(VelorumEntity.class, viewer.getBoundingBox().inflate(SEARCH))) {
			if (!boss.isAlive() || boss.kit() == null) continue;
			double d = boss.distanceToSqr(viewer);
			if (d < distance) {
				distance = d;
				best = boss;
			}
		}
		return best;
	}

	/**
	 * How far the viewer is in the dark, 0 to 1: inside a live pool outside his light, or anywhere in the Eclipse's hold;
	 * never within an awake ally's warmth, never while they carry their own heat ({@code SEARING}).
	 */
	public static float dark(Player viewer, float partial) {
		VelorumEntity boss = near(viewer);
		if (boss == null || boss.syncedEndingTick() >= 0 || SchoolStates.has(viewer, SchoolState.SEARING)) return 0.0F;
		Vec3 feet = viewer.getPosition(partial);
		List<VelorumDevoteeEntity> allies = allies(viewer.level(), feet, 4.0D);
		float lantern = VelorumDarkSight.lantern(boss, partial);
		float push = pushed(viewer.level(), boss, feet.x, feet.z, lantern, lanternFoot(boss, partial), allies, partial);
		float cover = eclipseHold(boss, partial) ? 1.0F : 0.0F;
		for (BossGroundHazardEntity hazard : viewer.level().getEntitiesOfClass(BossGroundHazardEntity.class,
				viewer.getBoundingBox().inflate(16.0D))) {
			if (hazard.isRemoved() || hazard.owner() != boss || !pool(hazard)) continue;
			cover = Math.max(cover, cover(hazard, boss, feet.x, feet.z, partial));
		}
		return HemoEasing.clamp01(cover * (1.0F - push));
	}

	/**
	 * The gloam on the viewer, 0 to 1: the 60 ticks after the Long Night's commit, a Gloam Cut or the Shadow Step's
	 * arrival struck them (read from the hurt flash and his synced move, so no packet is needed), fading over its last 15.
	 */
	public static float gloam(Player viewer, float partial) {
		float now = AnimationTime.gameTime(viewer.level(), partial);
		float[] state = GLOAMS.computeIfAbsent(viewer, ignored -> new float[]{Float.NaN, -1.0F});
		DamageSource source = viewer.getLastDamageSource();
		if (viewer.hurtTime > 0 && viewer.hurtTime != (int) state[1] && source != null && source.getEntity() instanceof VelorumEntity boss) {
			VelorumMove move = VelorumMove.byId(boss.view().moveId());
			if (move == VelorumMove.LONG_NIGHT || move == VelorumMove.GLOAM_CUTS || move == VelorumMove.UMBRAL_STEP) {
				if (viewer.hurtTime == viewer.hurtDuration) state[0] = now;
			}
		}
		state[1] = viewer.hurtTime;
		if (Float.isNaN(state[0])) return 0.0F;
		float since = now - state[0];
		if (since < 0.0F || since > GLOAM_TICKS) return 0.0F;
		return 1.0F - HemoEasing.clamp01((since - (GLOAM_TICKS - 15.0F)) / 15.0F);
	}

	/** Whether the viewer is in the Eclipse's hold (the gloam's inner edge and fog draw in further). */
	public static boolean eclipsed(Player viewer, float partial) {
		VelorumEntity boss = near(viewer);
		return boss != null && eclipseHold(boss, partial);
	}

	/** Ticks since an Arrested Pulse marked {@code player}, or -1 when no live mark is on them. */
	public static float marked(Player player, float partial) {
		double now = AnimationTime.gameTime(player.level(), partial);
		VelorumCueLog.Entry mark = VelorumCueLog.latest(VelorumEffects.PULSE_MARK, player.getId(), now);
		if (mark == null) return -1.0F;
		if (VelorumCueLog.since(VelorumEffects.PULSE_END, player.getId(), mark.born()) != null) return -1.0F;
		return mark.age(now);
	}

	/** Ticks since the mark on {@code player} last burst, or -1. */
	public static float burst(Player player, float partial) {
		double now = AnimationTime.gameTime(player.level(), partial);
		VelorumCueLog.Entry burst = VelorumCueLog.latest(VelorumEffects.PULSE_BURST, player.getId(), now);
		return burst == null ? -1.0F : burst.age(now);
	}
}
