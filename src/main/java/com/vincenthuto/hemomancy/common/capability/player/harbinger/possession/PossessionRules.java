package com.vincenthuto.hemomancy.common.capability.player.harbinger.possession;

/**
 * Concentration, the possession's only meter: a hit counter separate from health that never regenerates on its own.
 * Who may begin a possession, and what a hit costs.
 */
public final class PossessionRules {
	public static final int MIND_BROKEN_TICKS = 6000;
	public static final int RETURN_HOLD_TICKS = 40;
	public static final int SHELL_HITS = 3;
	public static final float HEAVY_HIT = 6f;
	public static final int WATER_DRAIN_TICKS = 20;
	/** Adipocere burned in a brazier: corpse wax seals three more hits of Concentration. */
	public static final int ADIPOCERE_BONUS = 3;

	/** The effect ids the Rite of Possession's optional brazier offerings name. */
	public static final String REGEN = "concentration_regen";
	public static final String SHELL = "shell";
	public static final String TOTEM = "totem";
	public static final String MAX_CONCENTRATION = "max_concentration";
	public static final java.util.Set<String> OFFERING_EFFECTS = java.util.Set.of(REGEN, SHELL, TOTEM, MAX_CONCENTRATION);

	private PossessionRules() {}

	/** What the offerings burned in the braziers tune; an unknown effect id tunes nothing. */
	public record Offerings(boolean regen, boolean shell, boolean totem, boolean wax) {
		public static final Offerings NONE = new Offerings(false, false, false, false);

		public static Offerings of(java.util.Collection<String> effects) {
			return new Offerings(effects.contains(REGEN), effects.contains(SHELL), effects.contains(TOTEM),
					effects.contains(MAX_CONCENTRATION));
		}

		public int pool(SurrogateForm form) {
			return form.pool() + (wax ? ADIPOCERE_BONUS : 0);
		}

		public int shellHits() {
			return shell ? SHELL_HITS : 0;
		}
	}

	/** What remains after a hit: the shell takes it first, then Concentration; broken at zero. */
	public record Hit(int shell, float concentration, boolean broken) {}

	public static float hitCost(float damage) {
		return damage >= HEAVY_HIT ? 2f : 1f;
	}

	public static Hit absorb(int shell, float concentration, float cost) {
		if (shell > 0) return new Hit(shell - 1, concentration, false);
		float left = Math.max(0f, concentration - cost);
		return new Hit(0, left, left <= 0f);
	}

	public static boolean mayBegin(boolean silentium, boolean mindBroken, boolean active) {
		return silentium && !mindBroken && !active;
	}

	/** Vanilla's hurt cooldown: a hit while still invulnerable from the last one costs nothing. */
	public static boolean ignoresHit(int invulnerableTime, boolean bypassesCooldown) {
		return invulnerableTime > 10 && !bypassesCooldown;
	}

	public static float regenPer100(boolean ambergris) {
		return ambergris ? 1f : 0f;
	}
}
