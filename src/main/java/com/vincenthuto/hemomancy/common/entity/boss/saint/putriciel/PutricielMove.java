package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import com.vincenthuto.hemomancy.common.entity.boss.BossBands.Band;
import com.vincenthuto.hemomancy.common.entity.boss.BossBands.Sector;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint.Anchor;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint.Height;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.saint.FollowCondition;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintDamageTypes;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintFollowUp;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveContext;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoves;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintOpening;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPickContext;
import com.vincenthuto.hemomancy.common.entity.boss.saint.TelegraphFamily;
import com.vincenthuto.hemomancy.common.entity.boss.saint.TelegraphSpec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntUnaryOperator;

/**
 * Putriciel's moves (design section 7 after the resolutions of 2026-11-05). Rot moves run only in the rot phases and
 * never burn; fire moves run only in the cleansing interval and take the cleansing damage type and the FLAME telegraph.
 * Timelines, footprints and telegraphs read only synced inputs (the client draws them); weights, eligibility, openings
 * and effects run on the server. Rot III cuts every rot wind-up by 3 (never under the kind's floor); the Repose deed
 * cuts every rot recovery by a quarter.
 */
public enum PutricielMove implements SaintMove {
	// ── rot ──────────────────────────────────────────────────────────
	CENSER_SWING(1, Kind.MELEE, Aim.TRACK, 0, false, 16, 6, 14, 7.0F, 60, 0, 1),
	CENSER_BACKSWING(2, Kind.MELEE, Aim.TRACK, 0, false, 12, 6, 18, 7.0F, 0, 0, 1),
	CENSER_DROP(3, Kind.AREA, Aim.TRACK, 0, false, 22, 4, 30, 10.0F, 0, 0, 3),
	OFFERING_GRASP(4, Kind.AREA, Aim.TRACK, 0, false, 20, 5, 16, 5.0F, 80, 0, 1),
	ROT_BREATH(5, Kind.AREA, Aim.TRACK, 0, false, 20, 30, 18, 2.0F, 100, 10, 1),
	SAC_SWELL(6, Kind.AREA, Aim.LOCK_AT_START, 0, false, 30, 36, 34, 6.0F, 160, 0, 1),
	SHELF_VOLLEY(7, Kind.AREA, Aim.LEAD, 10, false, 24, 0, 16, 5.0F, 140, 0, 1),
	ROOT_SNARE(8, Kind.AREA, Aim.TRACK, 0, false, 26, 8, 18, 7.0F, 120, 0, 1),
	CENSER_ORBIT(9, Kind.MELEE, Aim.LOCK_AT_START, 0, false, 22, 40, 26, 5.0F, 120, 10, 4),
	MOULD_COMMUNION(10, Kind.MELEE, Aim.LOCK_AT_START, 0, false, 16, 60, 20, 0.0F, 600, 0, 3),
	BLOOM_OF_ROT(11, Kind.AREA, Aim.LOCK_AT_START, 0, false, 50, 60, 40, 5.0F, 900, 20, 3),
	// ── fire ─────────────────────────────────────────────────────────
	IGNITION(20, Kind.AREA, Aim.LOCK_AT_START, 0, true, 30, 6, 20, 6.0F, 0, 0, 2),
	PURGING_SWEEP(21, Kind.AREA, Aim.TRACK, 0, true, 20, 6, 12, 8.0F, 50, 0, 2),
	CENSER_CAST(22, Kind.AREA, Aim.LEAD, 8, true, 22, 4, 18, 10.0F, 80, 0, 2),
	CAUTERISING_LANE(23, Kind.AREA, Aim.LOCK_AT_START, 0, true, 26, 24, 20, 9.0F, 120, 0, 2),
	PYRE_HEART(24, Kind.MELEE, Aim.LOCK_AT_START, 0, true, 28, 8, 30, 12.0F, 140, 0, 2),
	PYRE_LINES(25, Kind.AREA, Aim.LOCK_AT_START, 0, true, 30, 20, 24, 8.0F, 200, 0, 2),
	FULL_PURGE(26, Kind.AREA, Aim.LOCK_AT_START, 0, true, 40, 48, 20, 7.0F, 0, 16, 2),
	// ── the kneeling beats: their recovery is the opening ────────────
	OFFERING(30, Kind.MELEE, Aim.TRACK, 0, true, 12, 1, PutricielAbsolutionRules.OFFERING_TICKS, 0.0F, 0, 0, 2),
	EMBERING(31, Kind.MELEE, Aim.LOCK_AT_START, 0, true, 12, 1, 60, 0.0F, 0, 0, 2);

	/** Height of the censer and reach moves: a body-high sweep. */
	public static final Height SWING_HEIGHT = new Height(0.0D, 2.2D, false);
	public static final Height LOW_HEIGHT = new Height(0.0D, 2.0D, false);
	/** Spore cloud radius by phase (I, II, III) and its travel. */
	public static final double[] SAC_RADIUS = {8.0D, 9.0D, 10.0D};
	public static final int SAC_TRAVEL = 36;
	public static final int VOLLEY_DISCS = 5, VOLLEY_DISCS_III = 7, VOLLEY_PER_EXTRA = 2, VOLLEY_CAP = 9, VOLLEY_GAP = 4;
	public static final double VOLLEY_RADIUS = 1.5D;
	public static final int HUG_PRESSURE_STEP = 20, KITE_PRESSURE_STEP = 20;
	public static final int SAC_HUG_BONUS = 3, SAC_HUG_CAP = 21, KITE_BONUS = 2, KITE_CAP = 16;
	/** The Communion is picked only with Kindling at least this full and nobody within NEAR. */
	public static final float COMMUNION_MIN_KINDLING = 0.3F;
	/** The Bloom: 900 ticks in the phase and Kindling under half (resolution D-7). */
	public static final int BLOOM_MIN_TICKS = 900;
	public static final float BLOOM_MAX_KINDLING = 0.5F;
	public static final int BLOOM_WEIGHT = 12;
	public static final int BLOOM_PULSE = 20;

	private final int id;
	private final Kind kind;
	private final Aim aim;
	private final int lead;
	private final boolean fire;
	private final int windup;
	private final int active;
	private final int recovery;
	private final float damage;
	private final int cooldown;
	private final int hitEvery;
	/** 1: every rot phase (or the interval for fire); 3: Rot II on; 4: Rot III only; 2: the interval. */
	private final int fromPhase;

	PutricielMove(int id, Kind kind, Aim aim, int lead, boolean fire, int windup, int active, int recovery, float damage,
			int cooldown, int hitEvery, int fromPhase) {
		this.id = id;
		this.kind = kind;
		this.aim = aim;
		this.lead = lead;
		this.fire = fire;
		this.windup = windup;
		this.active = active;
		this.recovery = recovery;
		this.damage = damage;
		this.cooldown = cooldown;
		this.hitEvery = hitEvery;
		this.fromPhase = fromPhase;
	}

	@Override
	public int id() {
		return id;
	}

	@Override
	public String key() {
		return name().toLowerCase(java.util.Locale.ROOT);
	}

	@Override
	public Kind kind() {
		return kind;
	}

	@Override
	public Aim aim() {
		return aim;
	}

	@Override
	public int leadTicks() {
		return lead;
	}

	/** A fire move: the interval only, cleansing damage, the FLAME telegraph. */
	public boolean fire() {
		return fire;
	}

	@Override
	public boolean picked() {
		return switch (this) {
			case CENSER_BACKSWING, CENSER_DROP, IGNITION, FULL_PURGE, OFFERING, EMBERING -> false;
			default -> true;
		};
	}

	@Override
	public boolean inPhase(int phase) {
		if (fire) return phase == PutricielPhase.INTERVAL.number();
		if (!PutricielPhase.isRot(phase)) return false;
		return switch (fromPhase) {
			case 3 -> phase == PutricielPhase.ROT_II.number() || phase == PutricielPhase.ROT_III.number();
			case 4 -> phase == PutricielPhase.ROT_III.number();
			default -> true;
		};
	}

	// ── read by both sides ───────────────────────────────────────────

	@Override
	public BossMoveTimeline timeline(SaintMoveInputs in) {
		int w = windup;
		if (!fire && in.phase() == PutricielPhase.ROT_III.number()) w = Math.max(SaintMoves.revealFloor(kind), w - PutricielPhase.ROT_III_WINDUP_CUT);
		int a = this == SHELF_VOLLEY ? volleyDiscs(in) * VOLLEY_GAP + VOLLEY_GAP : active;
		int r = PutricielDeeds.recovery(recovery, in.deedMask(), !fire);
		return SaintMoves.floored(kind, new BossMoveTimeline(w, a, r));
	}

	@Override
	public BossFootprint footprint(SaintMoveInputs in) {
		return switch (this) {
			case CENSER_SWING -> new BossFootprint.Sweep(1.0D, 6.0D, 130.0D, 40.0D, false, 6, SWING_HEIGHT);
			case CENSER_BACKSWING -> new BossFootprint.Sweep(1.0D, 6.5D, 150.0D, 40.0D, true, 6, SWING_HEIGHT);
			case CENSER_DROP -> new BossFootprint.Disc(Anchor.AIM, 2.75D, Height.STANDING);
			case OFFERING_GRASP -> new BossFootprint.Lane(6.0D, 0.8D, 0, 0.0D, 0.0D, 0.0D, 0, Height.STANDING);
			case ROT_BREATH -> breathCone();
			case SAC_SWELL -> new BossFootprint.Ring(0.0D, sacRadius(in.phase()), 1.8D, 1, 0, SAC_TRAVEL, Height.STANDING);
			case SHELF_VOLLEY -> new BossFootprint.Discs(VOLLEY_RADIUS, VOLLEY_GAP, Height.STANDING);
			case ROOT_SNARE -> new BossFootprint.Cross(Anchor.AIM, 4.5D, 0.6D, Height.STANDING);
			case CENSER_ORBIT -> new BossFootprint.Annulus(1.5D, 4.5D, 2, 40.0D, 2.0D, 40, LOW_HEIGHT);
			case BLOOM_OF_ROT -> new BossFootprint.Region(BossFootprint.Area.AROUND_ORIGIN, PutricielArenaRules.BLOOM_RADIUS,
					LOW_HEIGHT);
			case IGNITION -> new BossFootprint.Disc(Anchor.ORIGIN, 6.0D, Height.STANDING);
			case PURGING_SWEEP -> new BossFootprint.Lane(8.0D, 1.0D, 0, 0.0D, 0.0D, 2.0D, 0, SWING_HEIGHT);
			case CENSER_CAST -> new BossFootprint.Disc(Anchor.AIM, 2.5D, Height.STANDING);
			case CAUTERISING_LANE -> new BossFootprint.Path(1.5D, 1.0D, 4.0D, 1, 0.0D, SWING_HEIGHT);
			case PYRE_HEART -> new BossFootprint.Disc(Anchor.ORIGIN, 3.5D, Height.STANDING);
			case PYRE_LINES -> pyreLines(in.variant());
			case FULL_PURGE -> new BossFootprint.Ring(0.0D, 8.0D, 0.9D, 3, 16, 16, Height.JUMPABLE);
			case MOULD_COMMUNION, OFFERING, EMBERING -> BossFootprint.None.INSTANCE;
		};
	}

	/** Rot Breath's cone, which its lingering cloud repeats. */
	public static BossFootprint breathCone() {
		return new BossFootprint.Cone(0.0D, 7.0D, 60.0D, 0.0D, Height.STANDING);
	}

	@Override
	public TelegraphSpec telegraph(SaintMoveInputs in) {
		TelegraphFamily family = fire ? TelegraphFamily.FLAME : TelegraphFamily.PUTRICIEL_ROT;
		return new TelegraphSpec(family, family, 0, id, true);
	}

	public static double sacRadius(int phase) {
		if (phase == PutricielPhase.ROT_III.number()) return SAC_RADIUS[2];
		return phase == PutricielPhase.ROT_II.number() ? SAC_RADIUS[1] : SAC_RADIUS[0];
	}

	/** Shelf Volley's discs: 5 (Rot III 7), 2 more per extra challenger, at most 9. */
	public static int volleyDiscs(SaintMoveInputs in) {
		int base = in.phase() == PutricielPhase.ROT_III.number() ? VOLLEY_DISCS_III : VOLLEY_DISCS;
		return Math.min(VOLLEY_CAP, base + VOLLEY_PER_EXTRA * Math.max(0, in.challengers() - 1));
	}

	/** One tether from each flared pyre (an anchor) to the body; the variant is how many pyres flared. */
	public static BossFootprint pyreLines(int count) {
		int lines = Math.min(PutricielArenaRules.MAX_PYRE_LINES, Math.max(0, count));
		if (lines == 0) return BossFootprint.None.INSTANCE;
		List<BossFootprint.Union.Part> parts = new ArrayList<>();
		for (int i = 0; i < lines; i++) {
			parts.add(new BossFootprint.Union.Part(new BossFootprint.Tether(BossFootprint.End.ANCHOR, BossFootprint.End.BODY,
					i, 0.6D, 0.0D, Height.STANDING), 0));
		}
		return new BossFootprint.Union(parts);
	}

	// ── server ───────────────────────────────────────────────────────

	@Override
	public int weight(SaintPickContext ctx) {
		Band band = ctx.band();
		Sector sector = ctx.sector();
		boolean front = sector == Sector.FRONT, flank = sector == Sector.FLANK;
		boolean hugNear = band == Band.HUG || band == Band.NEAR, midFar = band == Band.MID || band == Band.FAR;
		int hug = Math.min(SAC_HUG_CAP, SAC_HUG_BONUS * (ctx.hugTicks() / HUG_PRESSURE_STEP));
		int kite = Math.min(KITE_CAP, KITE_BONUS * (ctx.kiteTicks() / KITE_PRESSURE_STEP));
		return switch (this) {
			case CENSER_SWING -> hugNear ? (front ? 22 : flank ? 11 : 0) : 0;
			case OFFERING_GRASP -> {
				int base = front ? (band == Band.NEAR ? 16 : band == Band.MID ? 8 : 0) : 0;
				yield ctx.target() instanceof Player player && player.isBlocking() ? base * 2 : base;
			}
			case ROT_BREATH -> front ? (band == Band.NEAR ? 12 : band == Band.MID ? 6 : 0) : 0;
			case SAC_SWELL -> hugNear ? 14 + hug : 0;
			case SHELF_VOLLEY -> midFar && (front || flank) ? (front ? 16 : 8) + kite : 0;
			case ROOT_SNARE -> midFar ? 12 + kite : 0;
			case CENSER_ORBIT -> band == Band.HUG ? 18 + hug : 0;
			case MOULD_COMMUNION -> 10;
			case BLOOM_OF_ROT -> BLOOM_WEIGHT;
			case PURGING_SWEEP -> hugNear ? (front ? 20 : flank ? 10 : 0) : 0;
			case CENSER_CAST -> midFar && (front || flank) ? (front ? 18 : 9) + kite : 0;
			case CAUTERISING_LANE -> 12;
			case PYRE_HEART -> band == Band.HUG ? 18 + hug : 0;
			case PYRE_LINES -> midFar ? 16 + kite : 0;
			default -> 0;
		};
	}

	@Override
	public boolean eligible(SaintPickContext ctx) {
		PutricielEntity saint = ctx.saint() instanceof PutricielEntity p ? p : null;
		return switch (this) {
			case MOULD_COMMUNION -> ctx.meterFraction() >= COMMUNION_MIN_KINDLING && (saint == null || saint.nobodyClose());
			case BLOOM_OF_ROT -> ctx.ticksInPhase() >= BLOOM_MIN_TICKS && ctx.meterFraction() < BLOOM_MAX_KINDLING
					&& (saint == null || !saint.bloomUsedThisPhase());
			case CAUTERISING_LANE -> saint == null || saint.litPyreCount() > 0;
			case PYRE_LINES -> saint == null || saint.intervalsEnded() >= 1 && saint.litPyreCount() > 0;
			default -> true;
		};
	}

	@Override
	public int cooldown(SaintPickContext ctx) {
		return cooldown;
	}

	@Override
	public int rollVariant(SaintPickContext ctx, IntUnaryOperator roll) {
		if (this == PYRE_LINES && ctx.saint() instanceof PutricielEntity saint) return saint.pyreLineCount();
		return 0;
	}

	@Override
	public List<SaintFollowUp> followUps(int phase) {
		return switch (this) {
			case CENSER_SWING -> List.of(new SaintFollowUp(CENSER_BACKSWING, 0.7F, 5, FollowCondition.bands(Band.HUG, Band.NEAR)));
			case CENSER_BACKSWING -> List.of(new SaintFollowUp(CENSER_DROP, 0.5F, 6, FollowCondition.bands(Band.HUG)));
			case OFFERING_GRASP -> List.of(new SaintFollowUp(ROT_BREATH, 0.75F, 4, FollowCondition.onHit()));
			case ROOT_SNARE -> List.of(new SaintFollowUp(SHELF_VOLLEY, 0.5F, 6, FollowCondition.onHit()));
			case PURGING_SWEEP -> List.of(new SaintFollowUp(CENSER_CAST, 0.6F, 6, FollowCondition.bands(Band.MID)));
			default -> List.of();
		};
	}

	@Override
	public @Nullable SaintOpening.Spec recoveryOpening(SaintMoveContext ctx) {
		return switch (this) {
			case SAC_SWELL -> PutricielOpenings.SPENT_SACS;
			case CENSER_DROP -> PutricielOpenings.FOULED_CHAIN;
			case BLOOM_OF_ROT -> PutricielOpenings.SPENT_BLOOM;
			case EMBERING -> PutricielOpenings.EMBERING;
			case OFFERING -> PutricielOpenings.offering(ctx.saint() instanceof PutricielEntity saint
					? saint.offeringTicks() : PutricielAbsolutionRules.OFFERING_TICKS);
			default -> null;
		};
	}

	@Override
	public int hitEvery() {
		return hitEvery;
	}

	@Override
	public float baseDamage() {
		return damage;
	}

	@Override
	public @Nullable ResourceKey<DamageType> damageType() {
		return fire ? SaintDamageTypes.PUTRICIEL_CLEANSING : SaintDamageTypes.PUTRICIEL;
	}

	@Override
	public void onTick(SaintMoveContext ctx) {
		PutricielMoveEffects.tick(this, ctx);
	}

	@Override
	public void onHit(SaintMoveContext ctx, LivingEntity target) {
		PutricielMoveEffects.hit(this, ctx, target);
	}

	public static @Nullable PutricielMove byId(int id) {
		for (PutricielMove move : values()) if (move.id == id) return move;
		return null;
	}
}
