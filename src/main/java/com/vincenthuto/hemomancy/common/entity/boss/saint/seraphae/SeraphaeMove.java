package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import com.vincenthuto.hemomancy.common.entity.boss.BossBands;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint.Height;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.saint.FollowCondition;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintFollowUp;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveContext;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintOpening;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPickContext;
import com.vincenthuto.hemomancy.common.entity.boss.saint.TelegraphSpec;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeSelectionRules.Row;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntUnaryOperator;

/**
 * Seraphae's moves (design section 7). Timelines, footprints and telegraphs read only the synced inputs, so the client
 * draws what the server tests: the reaches carry their lane length in the variant, the lashes and the lance their wing,
 * the wrench its anchor, the Eye its sweep. Weights come from {@link SeraphaeSelectionRules}; what a move does each tick
 * and to whom it lands is her own ({@link SeraphaeEntity#moveTick}, {@link SeraphaeEntity#moveHit}).
 */
public enum SeraphaeMove implements SaintMove {
	LAYING_ON_OF_HANDS(1, "laying_on_of_hands", Kind.AREA, Aim.TRACK, 0.0F, 0,
			new Row(0, 30, 40, 25, 1.0F, 0.4F, 0.0F, 1.0F, 1.0F, 1.2F, 140, 100)),
	NERVE_LASH(2, "nerve_lash", Kind.MELEE, Aim.TRACK, 7.0F, 0,
			new Row(35, 35, 0, 0, 1.0F, 1.0F, 0.3F, 1.0F, 1.0F, 1.0F, 60, 60)),
	COUNTER_LASH(3, "counter_lash", Kind.MELEE, Aim.TRACK, 7.0F, 0, null),
	AXON_LANCE(4, "axon_lance", Kind.AREA, Aim.TRACK, 6.0F, 0,
			new Row(0, 10, 30, 40, 1.0F, 0.4F, 0.0F, 1.0F, 1.0F, 0.8F, 80, 80)),
	AXON_MIRROR(5, "axon_mirror", Kind.AREA, Aim.LOCK_AT_START, 6.0F, 0, null),
	REGROWTH_KNOT(6, "regrowth_knot", Kind.AREA, Aim.TRACK, 3.0F, 20,
			new Row(0, 10, 25, 25, 1.0F, 1.0F, 1.0F, 1.0F, 0.8F, 0.6F, 160, 160)),
	TRANSFUSION_THREAD(7, "transfusion_thread", Kind.AREA, Aim.TRACK, 0.0F, 10,
			new Row(0, 0, 10, 45, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 200, 200)),
	WING_GRASP(8, "wing_grasp", Kind.AREA, Aim.TRACK, 5.0F, 0,
			new Row(10, 25, 15, 0, 0.3F, 1.2F, 1.6F, 0.0F, 1.0F, 1.0F, 120, 120)),
	CONVULSION(9, "convulsion", Kind.MELEE, Aim.LOCK_AT_START, 8.0F, 0,
			new Row(40, 25, 0, 0, 1.0F, 1.0F, 1.0F, 0.0F, 1.0F, 1.2F, 100, 100)),
	CONVULSION_WIDE(10, "convulsion_wide", Kind.AREA, Aim.LOCK_AT_START, 8.0F, 0, null),
	CHAIN_WRENCH(11, "chain_wrench", Kind.AREA, Aim.LOCK_AT_START, 9.0F, 0,
			Row.any(35, 0.0F, 1.0F, 1.0F, 180)),
	FRACTURE(12, "fracture", Kind.MELEE, Aim.LOCK_AT_START, 4.0F, 0,
			Row.any(30, 0.0F, 1.0F, 0.8F, 500)),
	HEALED_GROUND(13, "healed_ground", Kind.AREA, Aim.LOCK_AT_START, 6.0F, 0,
			new Row(0, 0, 25, 30, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 220, 220)),
	UNCLOSING_EYE(14, "unclosing_eye", Kind.AREA, Aim.TRACK, 3.0F, 5,
			new Row(0, 0, 30, 40, 1.0F, 0.3F, 0.0F, 0.0F, 0.0F, 1.0F, 260, 260)),
	STRAINING_REACH(15, "straining_reach", Kind.AREA, Aim.TRACK, 0.0F, 0,
			Row.any(50, 0.0F, 0.0F, 1.0F, 400)),
	EXTRACTION(16, "extraction", Kind.AREA, Aim.LOCK_AT_START, 6.0F, 10,
			Row.any(25, 0.0F, 1.0F, 1.0F, 500)),
	RADIANCE_FLOOD(17, "radiance_flood", Kind.AREA, Aim.LOCK_AT_START, 6.0F, 0, null);

	/** The reach's lane, in quarter blocks, lives in the variant's low bits; the hasty bit marks a thrown follow-up. */
	public static final int LANE_MASK = 0xFFFF;
	public static final int HASTY = 1 << 16;
	public static final double REACH_HALF_WIDTH = 0.8D;
	public static final double LASH_RADIUS = 6.0D;
	public static final double LASH_DEGREES = 150.0D;
	public static final double LASH_BAND = 40.0D;
	public static final double LANCE_LENGTH = 16.0D;
	public static final double LANCE_FAN = 7.0D;
	public static final double KNOT_RADIUS = 2.75D;
	public static final double THREAD_RANGE = 22.0D;
	public static final double GRASP_RADIUS = 2.5D;
	public static final double FRACTURE_RADIUS = 4.0D;
	public static final double PAD_RADIUS = 2.0D;
	public static final double EYE_RANGE = 24.0D;
	public static final double EYE_DEGREES = 110.0D;
	public static final double EYE_BAND = 6.0D;
	public static final int EXTRACTION_CHAINS = 5;
	/** The spokes' arcs: six of 34 degrees, so the gaps between are 26 degrees. */
	public static final int SPOKES = 6;
	public static final double SPOKE_DEGREES = 34.0D;
	public static final double SPOKE_TURN = 1.0D / 6.0D;

	private final int id;
	private final String key;
	private final Kind kind;
	private final Aim aim;
	private final float baseDamage;
	private final int hitEvery;
	@Nullable
	private final Row row;

	SeraphaeMove(int id, String key, Kind kind, Aim aim, float baseDamage, int hitEvery, @Nullable Row row) {
		this.id = id;
		this.key = key;
		this.kind = kind;
		this.aim = aim;
		this.baseDamage = baseDamage;
		this.hitEvery = hitEvery;
		this.row = row;
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
		return row != null;
	}

	@Nullable
	public Row row() {
		return row;
	}

	@Override
	public boolean inPhase(int phase) {
		return switch (this) {
			case COUNTER_LASH, LAYING_ON_OF_HANDS, NERVE_LASH, AXON_LANCE, REGROWTH_KNOT, TRANSFUSION_THREAD,
					RADIANCE_FLOOD -> true;
			case AXON_MIRROR, WING_GRASP, CONVULSION, CHAIN_WRENCH, FRACTURE, EXTRACTION -> phase >= 2;
			case CONVULSION_WIDE, HEALED_GROUND, UNCLOSING_EYE, STRAINING_REACH -> phase >= 3;
		};
	}

	/** True for the two reaches, whose lane and whose hold share one shape. */
	public boolean reach() {
		return this == LAYING_ON_OF_HANDS || this == STRAINING_REACH;
	}

	// ── read by both sides ───────────────────────────────────────────

	@Override
	public BossMoveTimeline timeline(SaintMoveInputs in) {
		int mask = in.deedMask();
		return switch (this) {
			case LAYING_ON_OF_HANDS -> new BossMoveTimeline((in.variant() & HASTY) != 0 ? 20 : 24, 10,
					SeraphaeDeeds.recovery(mask, 20, false));
			case NERVE_LASH -> new BossMoveTimeline(14, 6, SeraphaeDeeds.recovery(mask, 16, false));
			case COUNTER_LASH -> new BossMoveTimeline(12, 6, SeraphaeDeeds.recovery(mask, 22, false));
			case AXON_LANCE, AXON_MIRROR -> new BossMoveTimeline(22, 8, SeraphaeDeeds.recovery(mask, 18, false));
			case REGROWTH_KNOT -> new BossMoveTimeline(30, 60, SeraphaeDeeds.recovery(mask, 14, false));
			case TRANSFUSION_THREAD -> new BossMoveTimeline(20, 80, SeraphaeDeeds.recovery(mask, 16, false));
			case WING_GRASP -> new BossMoveTimeline(20, 8, SeraphaeDeeds.recovery(mask, 22, false));
			case CONVULSION -> new BossMoveTimeline(22, 12, SeraphaeDeeds.recovery(mask, 30, false));
			case CONVULSION_WIDE -> new BossMoveTimeline(20, 14, SeraphaeDeeds.recovery(mask, 30, false));
			case CHAIN_WRENCH -> new BossMoveTimeline(24, 10, SeraphaeDeeds.recovery(mask, 20, false));
			case FRACTURE -> new BossMoveTimeline(30, 12, SeraphaeDeeds.recovery(mask, 20, false));
			case HEALED_GROUND -> new BossMoveTimeline(28, 10, SeraphaeDeeds.recovery(mask, 14, false));
			case UNCLOSING_EYE -> new BossMoveTimeline(26, 40, SeraphaeOpenings.LAPSE_TICKS);
			case STRAINING_REACH -> new BossMoveTimeline(28, 14, SeraphaeDeeds.recovery(mask, 24, false));
			case EXTRACTION -> new BossMoveTimeline(30, 20, SeraphaeDeeds.recovery(mask, 26, false));
			case RADIANCE_FLOOD -> new BossMoveTimeline(40, 2, 30);
		};
	}

	@Override
	public BossFootprint footprint(SaintMoveInputs in) {
		int variant = in.variant();
		return switch (this) {
			case LAYING_ON_OF_HANDS, STRAINING_REACH -> {
				double length = laneLength(variant);
				yield new BossFootprint.Lane(length, REACH_HALF_WIDTH, 0, 0.0D, 0.0D,
						length / SeraphaeLeashRules.REACH_GLIDE_TICKS, 0, Height.STANDING);
			}
			case NERVE_LASH -> new BossFootprint.Sweep(0.6D, LASH_RADIUS, LASH_DEGREES, LASH_BAND, variant == 0, 6,
					Height.JUMPABLE);
			case COUNTER_LASH -> new BossFootprint.Sweep(0.6D, LASH_RADIUS, LASH_DEGREES, LASH_BAND, variant != 0, 6,
					Height.STANDING);
			case AXON_LANCE, AXON_MIRROR -> new BossFootprint.Lane(LANCE_LENGTH, 0.6D, 1, 0.0D, LANCE_FAN, 2.5D, 4,
					Height.STANDING);
			case REGROWTH_KNOT -> new BossFootprint.Disc(BossFootprint.Anchor.AIM, KNOT_RADIUS, Height.STANDING);
			case TRANSFUSION_THREAD -> new BossFootprint.Occluded(new BossFootprint.Tether(BossFootprint.End.BODY,
					BossFootprint.End.TARGET, 0, 0.0D, THREAD_RANGE, Height.STANDING));
			case WING_GRASP -> new BossFootprint.Disc(BossFootprint.Anchor.AIM, GRASP_RADIUS, Height.STANDING);
			case CONVULSION -> new BossFootprint.Annulus(1.5D, 7.0D, SPOKES, SPOKE_DEGREES, SPOKE_TURN, 12,
					Height.STANDING);
			case CONVULSION_WIDE -> new BossFootprint.Annulus(2.0D, 9.0D, SPOKES, SPOKE_DEGREES, -SPOKE_TURN, 14,
					Height.STANDING);
			case CHAIN_WRENCH -> new BossFootprint.Union(List.of(
					new BossFootprint.Union.Part(new BossFootprint.Tether(BossFootprint.End.BODY, BossFootprint.End.ANCHOR,
							0, 1.0D, 0.0D, Height.STANDING), 0),
					new BossFootprint.Union.Part(new BossFootprint.Discs(2.0D, 0, Height.STANDING), 0)));
			case FRACTURE -> new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN, FRACTURE_RADIUS, Height.STANDING);
			case HEALED_GROUND -> new BossFootprint.Discs(PAD_RADIUS, 0, Height.STANDING);
			case UNCLOSING_EYE -> new BossFootprint.Occluded(new BossFootprint.Sweep(1.0D, EYE_RANGE, EYE_DEGREES,
					EYE_BAND, variant == 0, 40, Height.STANDING));
			case EXTRACTION -> {
				List<BossFootprint.Union.Part> parts = new ArrayList<>(EXTRACTION_CHAINS);
				for (int chain = 0; chain < EXTRACTION_CHAINS; chain++) {
					parts.add(new BossFootprint.Union.Part(new BossFootprint.Tether(BossFootprint.End.BODY,
							BossFootprint.End.ANCHOR, chain, 0.8D, 0.0D, Height.STANDING), 0));
				}
				yield new BossFootprint.Union(parts);
			}
			case RADIANCE_FLOOD -> new BossFootprint.Region(BossFootprint.Area.CHAMBER, 0.0D, Height.STANDING);
		};
	}

	/**
	 * The reaches under Unwatched Hands show their footprint only from the kit's reveal floor (the kit clamps the
	 * reveal tick to 20 before the commit); every other move shows from its first tick. Style ids are the move ids.
	 */
	@Override
	public TelegraphSpec telegraph(SaintMoveInputs in) {
		int reveal = reach() && SeraphaeDeeds.unwatchedHands(in.deedMask()) ? timeline(in).commitTick() : 0;
		return new TelegraphSpec(null, null, reveal, id, true);
	}

	/** The reach lane's length in blocks, from the variant. */
	public static double laneLength(int variant) {
		return Math.max(1.0D, (variant & LANE_MASK) / 4.0D);
	}

	public static int laneVariant(double length, boolean hasty) {
		return Math.max(4, Math.min(LANE_MASK, (int) Math.round(length * 4.0D))) | (hasty ? HASTY : 0);
	}

	// ── server ───────────────────────────────────────────────────────

	@Override
	public int weight(SaintPickContext ctx) {
		if (row == null) return 0;
		int base = SeraphaeSelectionRules.weight(row, ctx.band(), ctx.sector(), ctx.phase());
		if (ctx.saint() instanceof SeraphaeEntity seraphae) return seraphae.adjustWeight(this, ctx, base);
		return base;
	}

	@Override
	public boolean eligible(SaintPickContext ctx) {
		if (this == EXTRACTION && !SeraphaeDeeds.extraction(ctx.deedMask())) return false;
		if (ctx.saint() instanceof SeraphaeEntity seraphae) return seraphae.eligible(this, ctx);
		return this != STRAINING_REACH && this != CHAIN_WRENCH && this != FRACTURE;
	}

	@Override
	public int cooldown(SaintPickContext ctx) {
		return row == null ? 0 : row.cooldown(ctx.phase());
	}

	@Override
	public int rollVariant(SaintPickContext ctx, IntUnaryOperator roll) {
		if (ctx.saint() instanceof SeraphaeEntity seraphae) return seraphae.variantFor(this, ctx, roll);
		return reach() ? laneVariant(6.0D, false) : 0;
	}

	@Override
	public List<SaintFollowUp> followUps(int phase) {
		return switch (this) {
			case NERVE_LASH -> List.of(new SaintFollowUp(COUNTER_LASH, 0.6F, 6,
					FollowCondition.bands(BossBands.Band.HUG, BossBands.Band.NEAR)));
			case AXON_LANCE -> phase >= 2 ? List.of(new SaintFollowUp(AXON_MIRROR, 0.5F, 8, FollowCondition.ALWAYS))
					: List.of();
			case WING_GRASP -> List.of(new SaintFollowUp(LAYING_ON_OF_HANDS, 0.8F, 4, FollowCondition.onHit()));
			case CONVULSION -> phase >= 3 ? List.of(new SaintFollowUp(CONVULSION_WIDE, 0.5F, 10, FollowCondition.ALWAYS))
					: List.of();
			default -> List.of();
		};
	}

	@Override
	@Nullable
	public SaintOpening.Spec recoveryOpening(SaintMoveContext ctx) {
		return ctx.saint() instanceof SeraphaeEntity seraphae ? seraphae.recoveryOpening(this, ctx) : null;
	}

	@Override
	public int hitEvery() {
		return hitEvery;
	}

	@Override
	public float baseDamage() {
		return baseDamage;
	}

	@Override
	public void onTick(SaintMoveContext ctx) {
		if (ctx.saint() instanceof SeraphaeEntity seraphae) seraphae.moveTick(this, ctx);
	}

	@Override
	public void onHit(SaintMoveContext ctx, LivingEntity target) {
		if (ctx.saint() instanceof SeraphaeEntity seraphae) seraphae.moveHit(this, ctx, target);
	}

	@Nullable
	public static SeraphaeMove byId(int id) {
		for (SeraphaeMove move : values()) if (move.id == id) return move;
		return null;
	}
}
