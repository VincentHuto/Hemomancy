package com.vincenthuto.hemomancy.client.screen.skilltree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vincenthuto.hemomancy.common.recipe.ScarRecipe;

/**
 * Holds all mutable state for the Cerebral Scarring browser tab.
 * <p>
 * Owned by {@link HarbingerProgressScreen}; passed into the static methods on
 * {@link ScarsTabView} each frame.
 */
public class ScarsTabState {

	// ── Tier configuration (constant) ────────────────────────────
	public static final String[] TIER_NAMES      = { "Tier 1", "Tier 2", "Tier 3" };
	public static final int[]    TIER_THRESHOLDS = { 1, 2, 3 };
	/** Minimum initiatory degree required to see each scar tier. */
	public static final int[]    TIER_DEGREE_REQ = { 4, 4, 5 };

	// ── Loaded recipe data ───────────────────────────────────────
	public final List<ScarRecipe> scarRecipes = new ArrayList<>();
	public final Map<String, List<ScarRecipe>> chiselByTier = new LinkedHashMap<>();

	// ── Selection ────────────────────────────────────────────────
	public String selectedScarTier = null;
	public int selectedScarIndexInTier = 0;

	// ── Scroll ───────────────────────────────────────────────────
	public int scarSidebarScroll = 0;

	// ── Data helpers ─────────────────────────────────────────────

	/** Clears and rebuilds {@link #chiselByTier} from {@link #scarRecipes}. */
	public void rebuildTierMap() {
		chiselByTier.clear();
		for (String name : TIER_NAMES) chiselByTier.put(name, new ArrayList<>());
		for (ScarRecipe r : scarRecipes) {
			for (int i = 0; i < TIER_THRESHOLDS.length; i++) {
				if (r.getTier() <= TIER_THRESHOLDS[i]) {
					chiselByTier.get(TIER_NAMES[i]).add(r);
					break;
				}
			}
		}
	}

	/** Selects the first accessible tier that contains at least one recipe. */
	public void autoSelectFirstTier(int playerDegree) {
		selectedScarTier = null;
		selectedScarIndexInTier = 0;
		for (int i = 0; i < TIER_NAMES.length; i++) {
			if (!chiselByTier.getOrDefault(TIER_NAMES[i], List.of()).isEmpty()
					&& playerDegree >= TIER_DEGREE_REQ[i]) {
				selectedScarTier = TIER_NAMES[i];
				break;
			}
		}
	}

	// ── Scroll helpers ───────────────────────────────────────────

	/** Total content height of the tier sidebar (for scroll clamping). */
	public int sidebarContentH() {
		int rowH = 22;
		int total = 0;
		for (String name : TIER_NAMES) {
			total += rowH + 2;
			if (name.equals(selectedScarTier)) {
				total += rowH + 2 + chiselByTier.getOrDefault(name, List.of()).size() * 18;
			}
		}
		return total;
	}

	public void clampSidebarScroll(int visibleH) {
		scarSidebarScroll = Math.min(scarSidebarScroll,
				Math.max(0, sidebarContentH() - visibleH));
	}
}
