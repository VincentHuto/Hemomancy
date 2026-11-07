package com.vincenthuto.hemomancy.client.model.entity.boss.putriciel;

import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielBodyFireRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielStage;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielVisualRules;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.phys.Vec3;

/**
 * The purifier on fire (design revision R1): the body's fire anchors and the burn of its growths. The anchors are the
 * generator's table ({@code putriciel.py}, emitted into {@link PutricielModel}): seventy named points on the staff,
 * the censers, the back colony, the shoulders and arms, the crown, the chest and the hem, each with an outward
 * normal, a steady-burn tongue size, a region and a kindle order. {@link PutricielAnimState#fireAnchors()} returns
 * this frame's, posed and moving; the world geometry stands its flames on them and reads their fire from
 * {@link PutricielBodyFireRules#anchor} with {@link Anchor#bodyRegion()}, {@link Anchor#place()} and
 * {@link Anchor#palmArm()}.
 *
 * <p>The growths burn by kindle order (R1.8) through {@link PutricielBodyFireRules#growth} and
 * {@link PutricielBodyFireRules#charAt}: each growth part's {@link #kindleOrder(String)} is its kindle step (one tick a
 * step from the Ignition's tick 10, each part black as the fire reaches it), and its regrowth step is the Embering's
 * rank (the back from its root outward, the arms and the crown from their tips inward, three ticks a step).
 */
public final class PutricielFireAnchors {
	/** The body fire's regions (R1.1); the generator writes them by ordinal, in {@link PutricielBodyFireRules.Region}'s order. */
	public enum Region {
		STAFF, CENSER, BACK, SHOULDER, CROWN, CHEST, HEM;

		private static final Region[] VALUES = values();
		private static final PutricielBodyFireRules.Region[] RULES = PutricielBodyFireRules.Region.values();

		public static Region byOrdinal(int ordinal) {
			return VALUES[ordinal];
		}

		/** The same region in the fire's rules. */
		public PutricielBodyFireRules.Region rules() {
			return RULES[ordinal()];
		}
	}

	/**
	 * One fire anchor this frame. {@code position} is relative to the body as drawn (as the renderer records the core
	 * and the palm: the rise's and the ending's sink included), {@code normal} the outward unit normal in the world,
	 * {@code velocity} the world velocity in blocks a tick (from consecutive frames, by render age), {@code size} where
	 * its tongue sits in its region's steady-burn range (0 the region's smallest, 1 its largest:
	 * {@link PutricielBodyFireRules.Region#height(double)}) and {@code order} the anchor's place in the whole body's
	 * kindle order (0 catches first; {@link #kindleTick(int)} gives its tick on the Ignition clock).
	 */
	public record Anchor(Vec3 position, Vec3 normal, Vec3 velocity, Region region, float size, int order) {
		/** Where the anchor stands in the world, given the body's position. */
		public Vec3 at(Vec3 origin) {
			return origin.add(position);
		}

		/** Its kindle tick on the Ignition's clock (the smoke shows four ticks before it). */
		public float kindleTick() {
			return PutricielFireAnchors.kindleTick(order);
		}

		/** Its kindle rank within its region as a fraction, 0 first to 1 last: {@link PutricielBodyFireRules}' place. */
		public double place() {
			return PLACE_BY_ORDER[Math.max(0, Math.min(COUNT - 1, order))];
		}

		/** Whether it is on the offering palm's own arm and shoulder (the Cauterising Lane and Pyre Lines flare it). */
		public boolean palmArm() {
			return PALM_BY_ORDER[Math.max(0, Math.min(COUNT - 1, order))];
		}

		public PutricielBodyFireRules.Region bodyRegion() {
			return region.rules();
		}

		/** The tongue's height at the steady burn, blocks. */
		public double height() {
			return bodyRegion().height(size);
		}

		/** Blocks a tick. */
		public double speed() {
			return velocity.length();
		}

		/** This anchor's fire at {@code moment}. */
		public PutricielBodyFireRules.Flame flame(PutricielBodyFireRules.Moment moment) {
			return PutricielBodyFireRules.anchor(bodyRegion(), place(), palmArm(), moment);
		}
	}

	/** Anchors on the body. */
	public static final int COUNT = PutricielModel.ANCHOR_COUNT;

	private static final float[] KINDLE_BY_ORDER = new float[COUNT];
	private static final double[] PLACE_BY_ORDER = new double[COUNT];
	private static final boolean[] PALM_BY_ORDER = new boolean[COUNT];
	/** Each table anchor's size in its region's range, 0 to 1. */
	private static final float[] SIZES = new float[COUNT];

	static {
		int[] inRegion = new int[Region.values().length];
		for (int i = 0; i < COUNT; i++) inRegion[PutricielModel.ANCHOR_REGIONS[i]]++;
		for (int i = 0; i < COUNT; i++) {
			int order = PutricielModel.ANCHOR_ORDERS[i];
			int region = PutricielModel.ANCHOR_REGIONS[i];
			KINDLE_BY_ORDER[order] = PutricielModel.ANCHOR_KINDLE[i];
			PALM_BY_ORDER[order] = PutricielModel.ANCHOR_PALM[i];
			int rank = 0;
			for (int j = 0; j < COUNT; j++)
				if (PutricielModel.ANCHOR_REGIONS[j] == region && PutricielModel.ANCHOR_ORDERS[j] < order) rank++;
			PLACE_BY_ORDER[order] = inRegion[region] <= 1 ? 0.0D : rank / (double) (inRegion[region] - 1);
			PutricielBodyFireRules.Region rules = Region.byOrdinal(region).rules();
			double span = rules.high() - rules.low();
			SIZES[i] = (float) HemoEasing.clamp01((PutricielModel.ANCHOR_HEIGHTS[i] - rules.low()) / span);
		}
	}

	private PutricielFireAnchors() {
	}

	/** Table anchor {@code index}'s size in its region's range. */
	static float size(int index) {
		return SIZES[index];
	}

	/** The kindle tick (Ignition clock) of the anchor at {@code order} in the body's kindle order. */
	public static float kindleTick(int order) {
		return KINDLE_BY_ORDER[Math.max(0, Math.min(COUNT - 1, order))];
	}

	/**
	 * A growth part's kindle order for {@link PutricielBodyFireRules#growth} and {@link PutricielBodyFireRules#charAt}:
	 * its kindle step, one tick a step from the Ignition's tick 10 (0 the chest's sacs, then the shoulders, the back
	 * from its root outward, the elbows, the crown, the hem and the knuckle); -1 for a part that is not a growth.
	 */
	public static int kindleOrder(String part) {
		int i = growthIndex(part);
		return i < 0 ? -1 : PutricielModel.GROWTH_ORDERS[i];
	}

	/** A growth part's kindle tick on the Ignition clock, or -1 for a part that is not a growth. */
	public static float kindleTick(String part) {
		int i = growthIndex(part);
		return i < 0 ? -1.0F : PutricielModel.GROWTH_KINDLE[i];
	}

	/** A growth part's rank in the Embering's regrowth (three ticks a step), or -1 for a part that is not a growth. */
	public static int regrowRank(String part) {
		int i = growthIndex(part);
		return i < 0 ? -1 : PutricielModel.GROWTH_STEPS[i];
	}

	/** The region a generator part burns in, or null. */
	public static Region regionOf(String part) {
		for (int i = 0; i < PutricielModel.REGION_PARTS.length; i++)
			if (PutricielModel.REGION_PARTS[i].equals(part)) return Region.byOrdinal(PutricielModel.REGION_OF[i]);
		return null;
	}

	static int growthIndex(String part) {
		for (int i = 0; i < PutricielModel.GROWTH_COUNT; i++) if (PutricielModel.GROWTH_PARTS[i].equals(part)) return i;
		return -1;
	}

	/** Growth part {@code index}'s scale from the fire alone at {@code moment} (the rise and the Bloom multiply over it). */
	static float growth(int index, PutricielBodyFireRules.Moment moment) {
		return PutricielBodyFireRules.growth(PutricielModel.GROWTH_ORDERS[index], PutricielModel.GROWTH_STEPS[index],
				PutricielModel.GROWTH_EATEN[index], moment);
	}

	/** Growth part {@code index}'s char alpha at {@code moment}. */
	static float charAlpha(int index, PutricielBodyFireRules.Moment moment) {
		return PutricielBodyFireRules.charAt(PutricielModel.GROWTH_ORDERS[index], PutricielModel.GROWTH_STEPS[index], moment);
	}

	/**
	 * How much the fire lights the body it is on, 0 to 1 (the renderer's block-light floor rises from 8 to 13 with it):
	 * from the first embers at the Ignition's tick 10 to the flames at 30, held through the burn, out with the
	 * Embering's embers by tick 30, and through the ending's fire beats. Never in a rot stage outside the ending.
	 */
	public static float selfLight(PutricielStage stage, float stageTime, float endingTime) {
		if (endingTime >= 0.0F) return HemoEasing.window(endingTime, PutricielBodyFireRules.ENDING_FROM - 2.0F,
				PutricielBodyFireRules.ENDING_FROM + 6.0F, PutricielBodyFireRules.ENDING_OUT - 10.0F, PutricielBodyFireRules.ENDING_OUT);
		return switch (stage) {
			case ROT -> 0.0F;
			case IGNITING -> HemoEasing.ramp(stageTime, PutricielVisualRules.BLACKEN_START, PutricielVisualRules.BLACKEN_END);
			case BURNING -> 1.0F;
			case EMBERING -> 1.0F - HemoEasing.ramp(stageTime, PutricielBodyFireRules.GUTTERED, PutricielBodyFireRules.EMBERS_OUT);
		};
	}
}
