package com.vincenthuto.hemomancy.common.damage;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * What Absorption gains from the school state on the creature it draws from (core-loop Track 12, CL-26): one table
 * keyed by {@link SchoolState}, not a hook per manipulation. Every strand form lays states, and so does every existing
 * school hit, so Absorption specialises by what the player set up. A creature carrying several states gives every
 * rider at once: the scales multiply, the reach takes the longest, the effects add up.
 */
public final class AbsorptionRiders {
	/** What a rider does beside its scales. */
	public enum Effect {
		/** Pressure: the creature is pulled toward the one drawing. */
		PULL,
		/** Rime: the slow deepens; at full Rime the creature is held still. */
		HOLD_STILL,
		/** Lodestone: its armour is stripped while the draw holds. */
		STRIP_ARMOUR,
		/** Necrosis: a kill by draw spreads Necrosis to what stands near. */
		SPREAD_ON_KILL,
		/** Disrupted: the draw arcs to the nearest other marked creature. */
		ARC,
		/** Obscured: the creature loses its target and the one drawing is Veiled. */
		UNTARGET_AND_VEIL
	}

	/**
	 * @param damage scales the draw's damage per pulse
	 * @param blood  scales the blood that comes back for what the draw took
	 * @param reach  scales how far away the creature may be drawn from
	 */
	public record Rider(double damage, double blood, double reach, Set<Effect> effects) {
		public Rider {
			effects = effects.isEmpty() ? Set.of() : Set.copyOf(effects);
		}

		public boolean has(Effect effect) {
			return effects.contains(effect);
		}
	}

	public static final Rider NONE = new Rider(1.0D, 1.0D, 1.0D, Set.of());
	public static final double PULL_SPEED = 0.18D;
	public static final int SLOW_TICKS = 15;
	public static final int STRIP_TICKS = 15;
	public static final double STRIP_ARMOUR = -0.5D;
	public static final double ARC_RANGE = 6.0D;
	public static final double ARC_SHARE = 0.5D;
	public static final double SPREAD_RADIUS = 3.0D;
	public static final int VEIL_TICKS = 60;

	private static final Map<SchoolState, Rider> TABLE = new EnumMap<>(SchoolState.class);

	static {
		TABLE.put(SchoolState.PRESSURE, new Rider(1.0D, 1.25D, 1.0D, Set.of(Effect.PULL)));
		TABLE.put(SchoolState.SEARING, new Rider(1.5D, 0.6D, 1.0D, Set.of()));
		TABLE.put(SchoolState.RIME, new Rider(1.0D, 1.0D, 1.0D, Set.of(Effect.HOLD_STILL)));
		TABLE.put(SchoolState.LODESTONE, new Rider(1.0D, 1.0D, 1.0D, Set.of(Effect.STRIP_ARMOUR)));
		TABLE.put(SchoolState.NECROSIS, new Rider(1.0D, 1.0D, 1.0D, Set.of(Effect.SPREAD_ON_KILL)));
		TABLE.put(SchoolState.DISRUPTED, new Rider(1.0D, 1.0D, 1.0D, Set.of(Effect.ARC)));
		TABLE.put(SchoolState.ILLUMINATED, new Rider(1.0D, 1.0D, 1.5D, Set.of()));
		TABLE.put(SchoolState.OBSCURED, new Rider(1.0D, 1.0D, 1.0D, Set.of(Effect.UNTARGET_AND_VEIL)));
	}

	private AbsorptionRiders() {
	}

	/** The rider one state gives; Veiled is the caster's own state and gives none. */
	public static Rider of(SchoolState state) {
		return state == null ? NONE : TABLE.getOrDefault(state, NONE);
	}

	/** Every rider the states give at once. */
	public static Rider combine(Collection<SchoolState> states) {
		double damage = 1.0D, blood = 1.0D, reach = 1.0D;
		Set<Effect> effects = EnumSet.noneOf(Effect.class);
		for (SchoolState state : states) {
			Rider rider = of(state);
			damage *= rider.damage();
			blood *= rider.blood();
			reach = Math.max(reach, rider.reach());
			effects.addAll(rider.effects());
		}
		return new Rider(damage, blood, reach, effects);
	}

	/** Whether any state on a creature gives a rider: it reads as marked for the Disrupted arc. */
	public static boolean marks(Collection<SchoolState> states) {
		for (SchoolState state : states) {
			if (TABLE.containsKey(state)) return true;
		}
		return false;
	}
}
