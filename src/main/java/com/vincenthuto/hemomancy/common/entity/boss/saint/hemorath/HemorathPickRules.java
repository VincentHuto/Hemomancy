package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import com.vincenthuto.hemomancy.common.entity.boss.BossBands.Band;
import com.vincenthuto.hemomancy.common.entity.boss.BossBands.Sector;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPickRules;

/**
 * Hemorath's choice as pure rules (design section 7): each move's weight by band and sector, then what he needs on top
 * of where the player stands. Feeding moves weigh x1.5 while he is Sunken and x0.3 above 85%; venting moves weigh x2
 * above 85%; kiting earns the siphon and the chain up to +24, hugging earns the pulse up to +18. Wells Answer needs the
 * pit and a pair worth drinking, the Tithe 150 debt, the Thicket three patches. While the Last Thirst roots him only the
 * Iron Rake and the Spiked Halo answer. JVM-tested; {@link HemorathMove} reads it.
 */
public final class HemorathPickRules {
	public static final float FEEDING_SUNKEN = 1.5F;
	public static final float FEEDING_BRIMMING = 0.3F;
	public static final float VENTING_BRIMMING = 2.0F;
	public static final int PRESSURE_STEP = 20;
	public static final int PRESSURE_PER_STEP = 3;
	public static final int KITE_MAX = 24;
	public static final int HUG_MAX = 18;
	/** The tide is worth spending from this meter fraction: 10 at 55%, 40 at full, x2 above 85%. */
	public static final float TIDE_FROM = 0.55F;
	public static final int TIDE_BASE = 10;
	public static final int TIDE_RANGE = 30;
	public static final int WELLS_BASE = 6;
	public static final int WELLS_HUNGER = 20;
	public static final float WELLS_PER_WEIGHT = 100.0F;

	/** Everything a pick reads, all of it plain numbers so a test can build any cell. */
	public record Situation(int phase, Band band, Sector sector, float fraction, int patches, float maxDebt,
			boolean inPit, float bestPair, int kiteTicks, int hugTicks, boolean downfall, boolean thirst) {
		public static Situation of(int phase, Band band, Sector sector, float fraction) {
			return new Situation(phase, band, sector, fraction, 0, 0.0F, false, 0.0F, 0, 0, false, false);
		}

		public Situation withPatches(int value) {
			return new Situation(phase, band, sector, fraction, value, maxDebt, inPit, bestPair, kiteTicks, hugTicks,
					downfall, thirst);
		}

		public Situation withDebt(float value) {
			return new Situation(phase, band, sector, fraction, patches, value, inPit, bestPair, kiteTicks, hugTicks,
					downfall, thirst);
		}

		public Situation withPit(boolean pit, float pair) {
			return new Situation(phase, band, sector, fraction, patches, maxDebt, pit, pair, kiteTicks, hugTicks,
					downfall, thirst);
		}

		public Situation withPressure(int kite, int hug) {
			return new Situation(phase, band, sector, fraction, patches, maxDebt, inPit, bestPair, kite, hug, downfall,
					thirst);
		}

		public Situation withDeeds(boolean fall) {
			return new Situation(phase, band, sector, fraction, patches, maxDebt, inPit, bestPair, kiteTicks, hugTicks,
					fall, thirst);
		}

		public Situation withThirst(boolean value) {
			return new Situation(phase, band, sector, fraction, patches, maxDebt, inPit, bestPair, kiteTicks, hugTicks,
					downfall, value);
		}
	}

	private HemorathPickRules() {
	}

	public static boolean eligible(HemorathMove move, Situation s) {
		if (!move.inPhase(s.phase())) return false;
		if (s.thirst()) return move == HemorathMove.IRON_RAKE || move == HemorathMove.SPIKED_HALO;
		return switch (move) {
			case WELLS_ANSWER -> s.inPit() && s.bestPair() >= HemorathWellRules.MIN_PAIR;
			case TITHE_RECKONING -> s.maxDebt() >= HemorathTitheRules.MIN_DEBT;
			case IRON_THICKET -> s.patches() >= HemorathIronRules.THICKET_NEEDS;
			case RIB_BURST -> s.fraction() >= HemorathMeterRules.BRIMMING_FROM;
			case EXSANGUINATING_TIDE -> s.fraction() >= TIDE_FROM;
			case GORGE -> s.downfall();
			default -> true;
		};
	}

	/** The weight in this cell; 0 where the move does not answer. */
	public static int weight(HemorathMove move, Situation s) {
		if (!eligible(move, s)) return 0;
		Band band = s.band();
		Sector sector = s.sector();
		boolean hug = band == Band.HUG, near = band == Band.NEAR, mid = band == Band.MID, far = band == Band.FAR;
		boolean front = sector == Sector.FRONT, flank = sector == Sector.FLANK, rear = sector == Sector.REAR;
		if (s.thirst()) {
			// rooted, he turns where he stands: the rake and the halo answer wherever the quarry is
			return move == HemorathMove.IRON_RAKE ? 20 : 18;
		}
		int base = switch (move) {
			case RAKING_CLAW -> hug || near ? SaintPickRules.cell(24, front, flank) : 0;
			case STAFF_HEEL -> hug || near ? SaintPickRules.cell(16, rear, flank) : 0;
			case STAFF_FALL -> near || mid ? SaintPickRules.cell(20, front, false) : 0;
			case IRON_RAKE -> mid || far ? SaintPickRules.cell(20, front, flank) : 0;
			case EMPTY_PULSE -> SaintPickRules.cell(18, hug, near);
			case MARROW_RUPTURE -> mid ? SaintPickRules.cell(16, false, true) : 0;
			case MARROW_SIPHON -> mid || far ? SaintPickRules.cell(14, front || rear, flank) : 0;
			case CHAINED_DEBT -> front ? SaintPickRules.cell(16, mid, near) : 0;
			case TITHE_RECKONING -> front || flank ? HemorathTitheRules.weight(s.maxDebt()) : 0;
			case WELLS_ANSWER -> WELLS_BASE + Math.round(WELLS_HUNGER * (1.0F - s.fraction()))
					+ (int) (s.bestPair() / WELLS_PER_WEIGHT);
			case SPIKED_HALO -> mid || far ? SaintPickRules.cell(18, front, flank) : 0;
			case IRON_THICKET -> SaintPickRules.cell(14, mid || far, near);
			case EXSANGUINATING_TIDE -> TIDE_BASE + Math.round(TIDE_RANGE * (s.fraction() - TIDE_FROM) / (1.0F - TIDE_FROM));
			case RIB_BURST -> SaintPickRules.cell(20, hug || near, mid);
			case GORGE -> hug || near || mid ? SaintPickRules.cell(18, front, false) : 0;
			case BACKHAND, CROWN_FALL, LAST_THIRST -> 0;
		};
		if (base <= 0) return 0;
		if (move == HemorathMove.MARROW_SIPHON || move == HemorathMove.CHAINED_DEBT) base += kiteBonus(s.kiteTicks());
		if (move == HemorathMove.EMPTY_PULSE) base += hugBonus(s.hugTicks());
		return Math.max(0, Math.round(base * needFactor(move, s.fraction())));
	}

	/** What he needs: feed when hollow, vent when about to burst. */
	public static float needFactor(HemorathMove move, float fraction) {
		if (feeding(move)) {
			if (fraction >= HemorathMeterRules.VENT_FROM) return FEEDING_BRIMMING;
			if (fraction < HemorathMeterRules.FILLING_FROM) return FEEDING_SUNKEN;
		}
		if (venting(move) && fraction >= HemorathMeterRules.VENT_FROM) return VENTING_BRIMMING;
		return 1.0F;
	}

	public static boolean feeding(HemorathMove move) {
		return move == HemorathMove.MARROW_SIPHON || move == HemorathMove.WELLS_ANSWER || move == HemorathMove.CHAINED_DEBT;
	}

	public static boolean venting(HemorathMove move) {
		return move == HemorathMove.RIB_BURST || move == HemorathMove.EXSANGUINATING_TIDE;
	}

	public static int kiteBonus(int kiteTicks) {
		return Math.min(KITE_MAX, PRESSURE_PER_STEP * (Math.max(0, kiteTicks) / PRESSURE_STEP));
	}

	public static int hugBonus(int hugTicks) {
		return Math.min(HUG_MAX, PRESSURE_PER_STEP * (Math.max(0, hugTicks) / PRESSURE_STEP));
	}
}
