package com.vincenthuto.hemomancy.common.encounter;

import java.util.ArrayList;
import java.util.List;

/** Pure progression and eligibility rules shared by Chamber state and the Orb. */
public final class ChamberProgressionRules {
	private static final int BASE_RADIUS = 4;
	private static final int MAX_TIER = 3;

	private ChamberProgressionRules() {
	}

	public static State stateFor(Facts facts) {
		if (facts.apotheos()) return new State(3, "apotheos");
		if (facts.silentium()) return new State(3, "silent_archon");
		if (facts.qliphothStarted()) return new State(2, "qliphoth_communion");
		if (facts.degree() >= 7) return new State(1, "archon_revelation");
		if (facts.firstScarLearned()) return new State(0, "mnemonic_lowtide");
		return new State(0, "will_default");
	}

	public static List<String> availableThemes(Facts facts) {
		List<String> themes = new ArrayList<>();
		themes.add("will_default");
		if (facts.firstScarLearned()) themes.add("mnemonic_lowtide");
		if (facts.degree() >= 7) themes.add("archon_revelation");
		if (facts.qliphothStarted()) themes.add("qliphoth_communion");
		if (facts.silentium()) themes.add("silent_archon");
		if (facts.apotheos()) themes.add("apotheos");
		return List.copyOf(themes);
	}

	public static Refresh compare(State previous, State next) {
		return new Refresh(previous.tier() != next.tier(),
				radiusForTier(next.tier()) > radiusForTier(previous.tier()),
				!previous.theme().equals(next.theme()));
	}

	public static int radiusForTier(int tier) {
		return BASE_RADIUS + Math.max(0, Math.min(MAX_TIER, tier)) * 2;
	}

	/** {@code silentium} and {@code apotheos} are the two finished eighth-degree paths. */
	public record Facts(int degree, boolean firstScarLearned, boolean qliphothStarted,
			boolean silentium, boolean apotheos) {
	}

	public record State(int tier, String theme) {
	}

	public record Refresh(boolean tierChanged, boolean radiusIncreased, boolean themeChanged) {
		public boolean changed() {
			return tierChanged || themeChanged;
		}
	}
}
