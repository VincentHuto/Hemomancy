package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules;
import com.vincenthuto.hemomancy.common.entity.boss.BossBands;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint.Height;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.saint.FollowCondition;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintDeeds;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintFollowUp;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveContext;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoves;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintOpening;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPhaseTable;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPickContext;
import com.vincenthuto.hemomancy.common.entity.boss.saint.TelegraphFamily;
import com.vincenthuto.hemomancy.common.entity.boss.saint.TelegraphSpec;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.IntUnaryOperator;

/**
 * Hemorath's moves on the Saint combat kit (design section 7, as resolved on 2026-11-05). The timeline, footprint and
 * telegraph read only synced inputs: the phase, the deed mask, and the variant, which carries his patch count, the
 * Beholding claw's held feint, the forced tide after the phase-1 overload and the wells a draw takes. The server half
 * (weights, effects, openings) is in {@link HemorathPickRules} and {@link HemorathMoves}.
 */
public enum HemorathMove implements SaintMove {
	RAKING_CLAW(1, "raking_claw", Kind.MELEE, Aim.TRACK, 1, true, 14, 5, 16, 0),
	BACKHAND(2, "backhand", Kind.MELEE, Aim.LOCK_AT_START, 1, false, 12, 4, 18, 0),
	STAFF_HEEL(3, "staff_heel", Kind.MELEE, Aim.LOCK_AT_START, 1, true, 12, 4, 16, 40),
	STAFF_FALL(4, "staff_fall", Kind.AREA, Aim.TRACK, 1, true, 22, 6, 24, 40),
	IRON_RAKE(5, "iron_rake", Kind.AREA, Aim.TRACK, 1, true, 28, 12, 20, 80),
	EMPTY_PULSE(6, "empty_pulse", Kind.MELEE, Aim.LOCK_AT_START, 1, true, 24, 4, 18, 60),
	MARROW_RUPTURE(7, "marrow_rupture", Kind.AREA, Aim.LOCK_AT_START, 2, true, 20, 24, 24, 80),
	MARROW_SIPHON(8, "marrow_siphon", Kind.AREA, Aim.LOCK_AT_START, 1, true, 24, 40, 20, 240),
	CHAINED_DEBT(9, "chained_debt", Kind.AREA, Aim.TRACK, 1, true, 20, 8, 14, 120),
	TITHE_RECKONING(10, "tithe_reckoning", Kind.AREA, Aim.TRACK, 1, true, 36, 2, 22, 160),
	WELLS_ANSWER(11, "wells_answer", Kind.MELEE, Aim.LOCK_AT_START, 1, true, 30, 40, 30, 300),
	SPIKED_HALO(12, "spiked_halo", Kind.AREA, Aim.TRACK, 2, true, 26, 34, 22, 200),
	CROWN_FALL(13, "crown_fall", Kind.AREA, Aim.LOCK_AT_START, 2, false, 20, 4, 26, 0),
	IRON_THICKET(14, "iron_thicket", Kind.AREA, Aim.TRACK, 2, true, 24, 6, 20, 240),
	EXSANGUINATING_TIDE(15, "exsanguinating_tide", Kind.AREA, Aim.LOCK_AT_START, 2, true, 30, 100, 20, 500),
	RIB_BURST(16, "rib_burst", Kind.AREA, Aim.LOCK_AT_START, 1, true, 24, 10, 24, 160),
	GORGE(17, "gorge", Kind.AREA, Aim.TRACK, 1, true, 20, 30, 20, 200),
	LAST_THIRST(18, "last_thirst", Kind.MELEE, Aim.LOCK_AT_START, 2, false, 40, 1, 1, 0);

	// ── the variant word ─────────────────────────────────────────────
	public static final int VARIANT_PATCHES = 0xF;
	/** Raking Claw's Beholding feint: it freezes mid wind-up before it strikes. */
	public static final int VARIANT_HELD = 1 << 4;
	/** The tide forced by the phase-1 overload: it waits out the card, then empties him to 30%. */
	public static final int VARIANT_FORCED = 1 << 5;
	/** Wells Answer's drawn wells, one bit each from bit 6. */
	public static final int VARIANT_WELLS_SHIFT = 6;

	/** The Beholding claw's longer wind-up and its freeze. */
	public static final int HELD_WINDUP = 22;
	public static final int HELD_FREEZE_FROM = 10;
	public static final int HELD_FREEZE_TICKS = 8;

	// ── footprint numbers ────────────────────────────────────────────
	public static final double CLAW_RADIUS = 4.5D, CLAW_DEGREES = 100.0D;
	public static final double BACKHAND_RADIUS = 3.5D, BACKHAND_DEGREES = 80.0D, BACKHAND_OFFSET = -45.0D;
	public static final double HEEL_RADIUS = 3.5D, HEEL_DEGREES = 120.0D;
	public static final double FALL_LENGTH = 7.0D, FALL_HALF_WIDTH = 0.7D, CHAIN_RADIUS = 1.2D, CHAIN_SIDE = 1.5D;
	public static final int CHAIN_DELAY = 4;
	public static final double RAKE_LENGTH = 14.0D, RAKE_SPACING = 2.0D;
	public static final double PULSE_RADIUS = 6.0D;
	public static final double RUPTURE_REACH = 10.0D, RUPTURE_BAND = 0.8D;
	public static final double SIPHON_RADIUS = 14.0D;
	public static final double CHAIN_LENGTH = 12.0D, CHAIN_HALF_WIDTH = 0.4D;
	public static final double CROWN_RADIUS = 3.5D;
	public static final double THICKET_RADIUS = 1.5D;
	public static final int THICKET_LANDING_GAP = 2;
	public static final Height TIDE_HEIGHT = new Height(0.0D, 0.6D, false);
	public static final int RIB_SPOKES = 8;
	public static final double RIB_LENGTH = 8.0D, RIB_HALF_WIDTH = 0.6D, RIB_INNER = 0.5D, RIB_OFFSET = 22.5D;
	public static final double GORGE_RADIUS = 9.0D, GORGE_DEGREES = 50.0D;
	public static final Height HALO_HEIGHT = new Height(HemorathHaloPath.LOW, HemorathHaloPath.HIGH, false);

	private final int id;
	private final String key;
	private final Kind kind;
	private final Aim aim;
	private final int fromPhase;
	private final boolean picked;
	private final int windup;
	private final int active;
	private final int recovery;
	private final int cooldown;

	HemorathMove(int id, String key, Kind kind, Aim aim, int fromPhase, boolean picked, int windup, int active,
			int recovery, int cooldown) {
		this.id = id;
		this.key = key;
		this.kind = kind;
		this.aim = aim;
		this.fromPhase = fromPhase;
		this.picked = picked;
		this.windup = windup;
		this.active = active;
		this.recovery = recovery;
		this.cooldown = cooldown;
	}

	@Override
	public int id() {
		return id;
	}

	@Override
	public String key() {
		return key;
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
	public boolean picked() {
		return picked;
	}

	@Override
	public boolean inPhase(int phase) {
		return phase >= fromPhase;
	}

	@Nullable
	public static HemorathMove byId(int id) {
		for (HemorathMove move : values()) if (move.id == id) return move;
		return null;
	}

	// ── read by both sides ───────────────────────────────────────────

	@Override
	public BossMoveTimeline timeline(SaintMoveInputs in) {
		int w = this == RAKING_CLAW && held(in.variant()) ? HELD_WINDUP : windup;
		if (in.phase() >= 2 && this != LAST_THIRST) {
			w = SaintMoves.cut(kind, w, HemorathPhaseRules.PHASE_2_WINDUP_CUT);
		}
		// the forced tide waits out the Vessel Empties card before its own wind-up shows
		if (this == EXSANGUINATING_TIDE && forced(in.variant())) w += SaintPhaseTable.CARD_TICKS;
		return SaintMoves.floored(kind, new BossMoveTimeline(w, active, recovery));
	}

	@Override
	public BossFootprint footprint(SaintMoveInputs in) {
		int patches = patches(in.variant());
		return switch (this) {
			case RAKING_CLAW -> new BossFootprint.Cone(0.0D, CLAW_RADIUS, CLAW_DEGREES, 0.0D, Height.STANDING);
			case BACKHAND -> new BossFootprint.Cone(0.0D, BACKHAND_RADIUS, BACKHAND_DEGREES, BACKHAND_OFFSET, Height.STANDING);
			case STAFF_HEEL -> new BossFootprint.Cone(0.0D, HEEL_RADIUS, HEEL_DEGREES, 180.0D, Height.STANDING);
			case STAFF_FALL -> new BossFootprint.Union(List.of(
					new BossFootprint.Union.Part(new BossFootprint.Lane(FALL_LENGTH, FALL_HALF_WIDTH, 0, 0.0D, 0.0D, 0.0D,
							0, Height.STANDING), 0),
					new BossFootprint.Union.Part(new BossFootprint.Discs(CHAIN_RADIUS, 0, Height.STANDING), CHAIN_DELAY)));
			case IRON_RAKE -> new BossFootprint.Lane(RAKE_LENGTH, HemorathIronRules.rakeHalfWidth(patches),
					HemorathIronRules.rakeSideLanes(patches), RAKE_SPACING, 0.0D, RAKE_LENGTH / active, 0,
					Height.STANDING);
			case EMPTY_PULSE -> new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN, PULSE_RADIUS, Height.STANDING);
			case MARROW_RUPTURE -> new BossFootprint.Ring(0.0D, RUPTURE_REACH, RUPTURE_BAND, 1, 0, active,
					Height.JUMPABLE);
			case MARROW_SIPHON -> new BossFootprint.Occluded(new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN,
					SIPHON_RADIUS, Height.STANDING));
			case CHAINED_DEBT -> new BossFootprint.Lane(CHAIN_LENGTH, CHAIN_HALF_WIDTH, 0, 0.0D, 0.0D,
					CHAIN_LENGTH / active, 0, Height.STANDING);
			case TITHE_RECKONING -> new BossFootprint.Occluded(new BossFootprint.Tether(BossFootprint.End.BODY,
					BossFootprint.End.TARGET, 0, 0.0D, HemorathTitheRules.RANGE, Height.STANDING));
			case SPIKED_HALO -> new BossFootprint.Path(HemorathHaloPath.HALF_WIDTH, HemorathHaloPath.speed(),
					HemorathHaloPath.FRONT, 1, 0.0D, HALO_HEIGHT);
			case CROWN_FALL -> new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN, CROWN_RADIUS, Height.STANDING);
			case IRON_THICKET -> new BossFootprint.Discs(THICKET_RADIUS, THICKET_LANDING_GAP, Height.STANDING);
			case EXSANGUINATING_TIDE -> new BossFootprint.Region(BossFootprint.Area.BOSS_FLOOR, 0.0D, TIDE_HEIGHT);
			case RIB_BURST -> new BossFootprint.Spokes(RIB_SPOKES, RIB_HALF_WIDTH, RIB_INNER, RIB_LENGTH, RIB_OFFSET,
					RIB_LENGTH / 4.0D, Height.STANDING);
			case GORGE -> new BossFootprint.Cone(0.0D, GORGE_RADIUS, GORGE_DEGREES, 0.0D, Height.STANDING);
			case WELLS_ANSWER, LAST_THIRST -> BossFootprint.None.INSTANCE;
		};
	}

	@Override
	public TelegraphSpec telegraph(SaintMoveInputs in) {
		// Beholding skipped: his tells hide until the floor (the kit clamps the reveal to 20 area, 12 melee)
		int reveal = SaintDeeds.skipped(in.deedMask(), ChapelRules.Kind.BEHOLDING) ? Integer.MAX_VALUE : 0;
		if (this == EXSANGUINATING_TIDE && forced(in.variant())) reveal = Math.max(reveal, SaintPhaseTable.CARD_TICKS);
		TelegraphFamily fill = switch (this) {
			case IRON_RAKE, IRON_THICKET, CROWN_FALL, STAFF_HEEL -> TelegraphFamily.CRUOR;
			case STAFF_FALL, CHAINED_DEBT, TITHE_RECKONING -> TelegraphFamily.IRON;
			default -> TelegraphFamily.ANIMUS;
		};
		TelegraphFamily edge = switch (this) {
			case IRON_RAKE, IRON_THICKET, CROWN_FALL, STAFF_FALL, STAFF_HEEL, CHAINED_DEBT, TITHE_RECKONING -> TelegraphFamily.IRON;
			case RIB_BURST, MARROW_RUPTURE -> TelegraphFamily.CRUOR;
			default -> TelegraphFamily.ANIMUS;
		};
		boolean whileActive = this != EXSANGUINATING_TIDE && this != MARROW_SIPHON;
		return new TelegraphSpec(fill, edge, reveal, id, whileActive);
	}

	// ── server ───────────────────────────────────────────────────────

	@Override
	public int weight(SaintPickContext ctx) {
		return HemorathPickRules.weight(this, HemorathMoves.situation(ctx));
	}

	@Override
	public boolean eligible(SaintPickContext ctx) {
		return HemorathPickRules.eligible(this, HemorathMoves.situation(ctx));
	}

	@Override
	public int cooldown(SaintPickContext ctx) {
		return cooldown;
	}

	@Override
	public int rollVariant(SaintPickContext ctx, IntUnaryOperator roll) {
		return HemorathMoves.variant(this, ctx, roll);
	}

	@Override
	public List<SaintFollowUp> followUps(int phase) {
		return switch (this) {
			case RAKING_CLAW -> List.of(new SaintFollowUp(BACKHAND, 0.55F, 6,
					FollowCondition.bands(BossBands.Band.HUG, BossBands.Band.NEAR)));
			case EMPTY_PULSE -> phase >= 2 ? List.of(new SaintFollowUp(MARROW_RUPTURE, 1.0F, 8, FollowCondition.ALWAYS))
					: List.of();
			case CHAINED_DEBT -> List.of(new SaintFollowUp(phase >= 2 ? TITHE_RECKONING : RAKING_CLAW, 1.0F, 4,
					FollowCondition.onHit()));
			case SPIKED_HALO -> List.of(new SaintFollowUp(CROWN_FALL, 0.5F, 4, c -> c.saint() != null
					&& c.saint().getHealth() / Math.max(1.0F, c.saint().getMaxHealth()) < HemorathPhaseRules.CROWN_FALL_HEALTH));
			default -> List.of();
		};
	}

	@Override
	@Nullable
	public SaintOpening.Spec recoveryOpening(SaintMoveContext ctx) {
		return HemorathMoves.recoveryOpening(this, ctx);
	}

	@Override
	public int hitEvery() {
		return switch (this) {
			case MARROW_SIPHON, GORGE -> 1;
			case SPIKED_HALO -> 14;
			default -> 0;
		};
	}

	@Override
	public float baseDamage() {
		return switch (this) {
			case RAKING_CLAW -> 7.0F;
			case BACKHAND, STAFF_HEEL, IRON_RAKE, MARROW_RUPTURE, EXSANGUINATING_TIDE -> 6.0F;
			case STAFF_FALL -> 11.0F;
			case EMPTY_PULSE -> 5.0F;
			case CHAINED_DEBT -> 3.0F;
			case CROWN_FALL -> 12.0F;
			case IRON_THICKET -> 8.0F;
			case RIB_BURST -> 9.0F;
			case SPIKED_HALO -> HemorathIronRules.HALO_DAMAGE;
			default -> 0.0F;
		};
	}

	@Override
	public void onTick(SaintMoveContext ctx) {
		HemorathMoves.tick(this, ctx);
	}

	@Override
	public void onHit(SaintMoveContext ctx, LivingEntity target) {
		HemorathMoves.hit(this, ctx, target);
	}

	// ── the variant ──────────────────────────────────────────────────

	public static int patches(int variant) {
		return Math.min(HemorathIronRules.MAX_PATCHES, variant & VARIANT_PATCHES);
	}

	public static boolean held(int variant) {
		return (variant & VARIANT_HELD) != 0;
	}

	public static boolean forced(int variant) {
		return (variant & VARIANT_FORCED) != 0;
	}

	/** The wells a draw takes, one bit per well index. */
	public static int wells(int variant) {
		return (variant >>> VARIANT_WELLS_SHIFT) & 0xF;
	}

	/** Lower-case key of the move with this id, for lang and review stops; empty for none. */
	public static String keyOf(int id) {
		HemorathMove move = byId(id);
		return move == null ? "" : move.key;
	}

	public int cooldownTicks() {
		return cooldown;
	}

	public int baseWindup() {
		return windup;
	}
}
