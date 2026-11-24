package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.rite.CardinalRiteWaveRules.Objective;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * The ordeal waves a ceremony may deal, one row each, by the id its recipe names in {@code waves}. A row carries what
 * the wave asks of the caster, the line that announces it, its checklist line, and whether and how high its threats
 * step out of the fog. What the threats then do is {@code CardinalRiteOrdealEngine}'s, switched on the row.
 */
public enum CardinalRiteWave {
	BLOODLICKER_SIPHON("bloodlicker_siphon", Objective.SURVIVE, "bloodlickers_crawl_for_the_anchors",
			"defend_the_boundary_from_bloodlickers", 1.0D),
	FARGONE_DIVE("fargone_dive", Objective.SURVIVE, "a_fargone_stoops_toward_your_living_blood",
			"survive_the_fargone_assault", 5.0D),
	ROGUE_WILL("rogue_will", Objective.SURVIVE, "a_rogue_will_claws_at_the_boundary",
			"drive_the_rogue_will_from_the_boundary", 1.0D),
	FALSE_OMENS("false_omens", Objective.DEFEAT, "counterfeit_anchors_bloom", null, 1.0D),
	RESPONSE_SIGIL("response_sigil", Objective.TRACE, "a_response_sigil_is_demanded", null, 0.0D),
	COVENANT_PRESSURE("covenant_pressure", Objective.TRACE, "the_covenant_presses_on_every_anchor", null, 0.0D),
	/** {@code discover_<sigil>}: an unknown Ichorian shape wakes and must be traced. */
	DISCOVER("discover_", Objective.TRACE, "an_unknown_ichorian_shape_wakes", null, 0.0D);

	/** The wave a rite with no wave left to deal, or an unknown one, falls back to. */
	public static final CardinalRiteWave FALLBACK = BLOODLICKER_SIPHON;

	private static final String CUE_PREFIX = "message.hemomancy.cardinal_rite_ordeal_engine.";
	private static final String CHECKLIST_PREFIX = "message.hemomancy.cardinal_rite_checklist.";

	private final String id;
	private final Objective objective;
	private final String cue;
	private final String checklist;
	private final double spawnHeight;

	CardinalRiteWave(String id, Objective objective, String cue, @Nullable String checklist, double spawnHeight) {
		this.id = id;
		this.objective = objective;
		this.cue = cue;
		this.checklist = checklist;
		this.spawnHeight = spawnHeight;
	}

	/** The id a recipe names; for {@link #DISCOVER} the prefix before the sigil's id. */
	public String id() {
		return id;
	}

	public Objective objective() {
		return objective;
	}

	/** Whether the wave's threats step out of the fog: every wave that is fought rather than traced. */
	public boolean spawnsThreats() {
		return objective != Objective.TRACE;
	}

	/** How far above the floor its threats appear; the Fargone dive from above. */
	public double spawnHeight() {
		return spawnHeight;
	}

	/** The line that announces the wave to its caster. */
	public String cueKey() {
		return CUE_PREFIX + cue;
	}

	/** The checklist line of a wave that is survived, or {@code null} when its deadline hint stands instead. */
	@Nullable
	public String checklistKey() {
		return checklist == null ? null : CHECKLIST_PREFIX + checklist;
	}

	/** The row a recipe's wave id names, or empty for an id the ordeal does not know. */
	public static Optional<CardinalRiteWave> byId(@Nullable String id) {
		if (id == null) return Optional.empty();
		if (id.startsWith(DISCOVER.id) && id.length() > DISCOVER.id.length()) return Optional.of(DISCOVER);
		for (CardinalRiteWave wave : values()) {
			if (wave != DISCOVER && wave.id.equals(id)) return Optional.of(wave);
		}
		return Optional.empty();
	}

	/** The row a dealt wave id names, falling back to {@link #FALLBACK}. */
	public static CardinalRiteWave of(@Nullable String id) {
		return byId(id).orElse(FALLBACK);
	}

	/** The sigil a {@code discover_<sigil>} wave wakes, or {@code null} for any other wave. */
	@Nullable
	public static String discoveredSigil(@Nullable String id) {
		return of(id) == DISCOVER ? id.substring(DISCOVER.id.length()) : null;
	}
}
