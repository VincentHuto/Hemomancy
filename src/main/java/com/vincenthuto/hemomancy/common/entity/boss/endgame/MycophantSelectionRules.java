package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantPositionRules.Band;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantPositionRules.Sector;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.function.DoubleSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.ToIntFunction;

/**
 * The Spawn Mother's move picker. Each move answers some bands and sectors with a weight per phase; recent moves are
 * damped, kiting raises the embrace and hugging raises the Brood Settle. Strings come from follow-ups, which skip the
 * picker. Pure, so it is JVM-tested.
 */
public final class MycophantSelectionRules {
	/** What a follow-up needs to fire: always, a landed hit, the quarry under her (HUG) or close (HUG/NEAR), a net. */
	public enum Condition {
		ALWAYS, ON_HIT, HUGGED, CLOSE, NETTED;

		boolean holds(boolean hitLanded, boolean netted, Band band) {
			return switch (this) {
				case ALWAYS -> true;
				case ON_HIT -> hitLanded;
				case HUGGED -> band == Band.HUG;
				case CLOSE -> band == Band.HUG || band == Band.NEAR;
				case NETTED -> netted;
			};
		}
	}

	/** A move that may start straight after another, after the link gap instead of the decision delay. */
	public record FollowUp(MycophantAttack next, Condition condition, float chance) {
	}

	/** What the picker knows beyond position: {@code nursingTarget} is a living brood fungling in reach to nurse. */
	public record Context(boolean nursingTarget) {
	}

	/** Follow-ups never chain more than two moves deep. */
	private static final int MAX_CHAIN = 2;
	private static final float[] REPEAT_FACTORS = {0.0F, 0.4F, 0.6F, 0.8F};

	private MycophantSelectionRules() {
	}

	/**
	 * The base weight of {@code attack} for a quarry in {@code band} and {@code sector}: the embrace without kite
	 * pressure, the settle without hug pressure, the nursing as if a fungling were in reach. 0 means ineligible there.
	 */
	public static int weight(MycophantAttack attack, int phase, Band band, Sector sector) {
		return weight(attack, phase, band, sector, 0, 0, new Context(true));
	}

	/** The weight with the live pressure counts and context; half cells give half the move's weight. */
	public static int weight(MycophantAttack attack, int phase, Band band, Sector sector, int kiteTicks, int hugTicks,
			Context context) {
		if (!MycophantControllerRules.allowed(attack, phase)) return 0;
		boolean front = sector == Sector.FRONT;
		boolean flank = sector == Sector.FLANK;
		boolean rear = sector == Sector.REAR;
		boolean close = band == Band.HUG || band == Band.NEAR;
		boolean kiting = band == Band.MID || band == Band.FAR;
		boolean reach = band == Band.NEAR || band == Band.MID;
		return switch (attack) {
			case RUSHING_EMBRACE, FUSION_EMBRACE -> cell(embraceWeight(kiteTicks), kiting && front, kiting && flank);
			case SIPHON_FEED -> cell(16, reach && front, false);
			case PLUCK -> cell(14, close && front, false);
			case RAKE_STRING -> cell(24, close && front, close && flank);
			case BROOD_CALL -> cell(8, kiting, close);
			case NECTAR_SURGE -> 5;
			case BROOD_SETTLE -> band == Band.HUG ? 10 + settleBonus(hugTicks) : 0;
			case CLAW_PIN -> cell(14, band == Band.NEAR && front, false);
			case TAIL_LASH -> cell(26, close && rear, close && flank);
			case VENT_BLOOM -> cell(14, band != Band.FAR && rear, band != Band.FAR && flank);
			case HAND_SWEEP -> cell(18, reach && front, false);
			case SIPHON_SPIT -> cell(18, kiting && front, kiting && flank);
			case SPORE_MORTAR -> cell(16, band == Band.FAR, band == Band.MID);
			case NURSING -> context.nursingTarget() ? 12 : 0;
			case WING_GUARD -> cell(10, reach && front, false);
			case WING_SWEEP -> cell(18, close && front, close && flank);
			case PULP_BLOOM -> cell(12, close && front, false);
			case HYPHAE_LANCES -> cell(16, band != Band.HUG && front, band != Band.HUG && flank);
			case LATTICE_NET -> cell(12, band == Band.MID && front, false);
			case GAZE -> cell(10, kiting && front, false);
			case LAST_BROOD, NONE -> 0;
		};
	}

	private static int cell(int weight, boolean full, boolean half) {
		if (full) return weight;
		return half ? weight / 2 : 0;
	}

	/** The embrace punishes kiting: 8, plus 4 for every 20 ticks spent in MID or FAR, capped at 40. */
	public static int embraceWeight(int kiteTicks) {
		return Math.min(40, 8 + 4 * (kiteTicks / 20));
	}

	/** The Brood Settle punishes hugging: 3 for every 20 ticks spent in HUG, capped at 24. */
	public static int settleBonus(int hugTicks) {
		return Math.min(24, 3 * (hugTicks / 20));
	}

	/**
	 * Weighted pick among the moves that are allowed, off cooldown and answer the quarry's band and sector, each damped
	 * by {@link History#repeatFactor}. NONE when nothing qualifies. The caller records the pick and resets the kite
	 * count when an embrace starts.
	 */
	public static MycophantAttack choose(int phase, Band band, Sector sector, ToIntFunction<MycophantAttack> cooldownLeft,
			History history, Pressure pressure, Context context, IntUnaryOperator roll) {
		MycophantAttack[] attacks = MycophantAttack.values();
		int[] weights = new int[attacks.length];
		int sum = 0;
		for (MycophantAttack attack : attacks) {
			if (cooldownLeft.applyAsInt(attack) > 0) continue;
			int base = weight(attack, phase, band, sector, pressure.kiteTicks(), pressure.hugTicks(), context);
			int damped = Math.round(base * history.repeatFactor(attack));
			weights[attack.ordinal()] = damped;
			sum += damped;
		}
		if (sum <= 0) return MycophantAttack.NONE;
		int pick = Math.floorMod(roll.applyAsInt(sum), sum);
		for (MycophantAttack attack : attacks) {
			pick -= weights[attack.ordinal()];
			if (pick < 0) return attack;
		}
		return MycophantAttack.NONE;
	}

	@Nullable
	private static FollowUp followUpOf(MycophantAttack done, int phase) {
		boolean two = phase >= 2;
		return switch (done) {
			case CLAW_PIN -> new FollowUp(MycophantAttack.PLUCK, Condition.ON_HIT, 1.0F);
			case HAND_SWEEP -> new FollowUp(two ? MycophantAttack.FUSION_EMBRACE : MycophantAttack.RUSHING_EMBRACE,
					Condition.ON_HIT, 0.7F);
			case WING_SWEEP -> new FollowUp(MycophantAttack.PLUCK, Condition.CLOSE, 0.4F);
			case LATTICE_NET -> new FollowUp(MycophantAttack.FUSION_EMBRACE, Condition.NETTED, 0.85F);
			case RAKE_STRING -> new FollowUp(MycophantAttack.BROOD_SETTLE, Condition.HUGGED, 0.3F);
			default -> null;
		};
	}

	/**
	 * The move that follows {@code done}, or NONE. {@code depth} is how many follow-ups already ran in this chain; a
	 * pending interrupt (the split, a stagger, the Last Brood) cancels the chain. Follow-ups ignore the anti-repeat
	 * damping and the cooldowns.
	 */
	public static MycophantAttack followUp(MycophantAttack done, int phase, boolean hitLanded, boolean netted, Band band,
			int depth, boolean interruptPending, DoubleSupplier chanceRoll) {
		if (depth >= MAX_CHAIN || interruptPending) return MycophantAttack.NONE;
		FollowUp link = followUpOf(done, phase);
		if (link == null || !MycophantControllerRules.allowed(link.next(), phase)) return MycophantAttack.NONE;
		if (!link.condition().holds(hitLanded, netted, band)) return MycophantAttack.NONE;
		return chanceRoll.getAsDouble() < link.chance() ? link.next() : MycophantAttack.NONE;
	}

	/** Ticks between a move and its follow-up. */
	public static int linkGapTicks(int phase) {
		return phase >= 2 ? 4 : 6;
	}

	/**
	 * The variant a move starts with. Rake String: two-hit (0) or delayed-slam (1) in phase 1, slam (1) or four-swing
	 * (2) in phase 2. Tail Lash swings twice (1) in phase 2. Everything else has one form (0).
	 */
	public static int rollVariant(MycophantAttack attack, int phase, IntUnaryOperator roll) {
		boolean two = phase >= 2;
		return switch (attack) {
			case RAKE_STRING -> (two ? 1 : 0) + Math.floorMod(roll.applyAsInt(2), 2);
			case TAIL_LASH -> two ? 1 : 0;
			default -> 0;
		};
	}

	/** Her last four picks, most recent first, for the no-repeat rule and the decay. */
	public static final class History {
		private final MycophantAttack[] recent = new MycophantAttack[REPEAT_FACTORS.length];

		public History() {
			Arrays.fill(recent, MycophantAttack.NONE);
		}

		public void record(MycophantAttack attack) {
			System.arraycopy(recent, 0, recent, 1, recent.length - 1);
			recent[0] = attack;
		}

		public MycophantAttack last() {
			return recent[0];
		}

		/** 0 for the last pick, then 0.4, 0.6 and 0.8 down the history, 1 for anything older or unused. */
		public float repeatFactor(MycophantAttack attack) {
			for (int i = 0; i < recent.length; i++) if (recent[i] == attack) return REPEAT_FACTORS[i];
			return 1.0F;
		}
	}

	/** The quarry's band over the last 200 ticks, one sample per tick: how long they kited and how long they hugged. */
	public static final class Pressure {
		private static final int WINDOW = 200;
		private static final byte EMPTY = -1;
		private final byte[] ring = new byte[WINDOW];
		private int next;
		private int kite;
		private int hug;

		public Pressure() {
			Arrays.fill(ring, EMPTY);
		}

		public void sample(Band band) {
			forget(ring[next]);
			ring[next] = (byte) band.ordinal();
			if (isKite(ring[next])) kite++;
			else if (ring[next] == Band.HUG.ordinal()) hug++;
			next = (next + 1) % WINDOW;
		}

		/** Ticks in MID or FAR within the window. */
		public int kiteTicks() {
			return kite;
		}

		/** Ticks in HUG within the window. */
		public int hugTicks() {
			return hug;
		}

		/** An embrace started: the kiting that earned it no longer counts. */
		public void resetKite() {
			for (int i = 0; i < WINDOW; i++) if (isKite(ring[i])) ring[i] = EMPTY;
			kite = 0;
		}

		private void forget(byte sample) {
			if (isKite(sample)) kite--;
			else if (sample == Band.HUG.ordinal()) hug--;
		}

		private static boolean isKite(byte sample) {
			return sample == Band.MID.ordinal() || sample == Band.FAR.ordinal();
		}
	}
}
