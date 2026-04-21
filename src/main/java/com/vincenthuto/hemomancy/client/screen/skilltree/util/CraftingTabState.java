package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.HarbingerProgressScreen;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;

/**
 * Holds all mutable state for the Blood Crafting browser tab.
 * <p>
 * Owned by {@link HarbingerProgressScreen}; passed into the static methods on
 * {@link CraftingTabView} each frame.
 */
public class CraftingTabState {

	// ── Tier configuration (constant, not per-recipe) ────────────
	public static final String[] TIER_NAMES       = { "Basic", "Advanced", "Expert" };
	public static final int[]    TIER_THRESHOLDS  = { 100, 200, Integer.MAX_VALUE };
	public static final int[]    TIER_DEGREE_REQ  = { 0, 2, 4 };

	// ── Loaded recipe data ───────────────────────────────────────
	public final List<BloodStructureRecipe> craftingRecipes = new ArrayList<>();
	public final Map<String, List<BloodStructureRecipe>> craftingByTier = new LinkedHashMap<>();

	// ── Selection / navigation ───────────────────────────────────
	public String selectedCraftingTier = null;
	public int selectedCraftingIndexInTier = 0;

	// ── 3D model rotation ────────────────────────────────────────
	public float craftingRotationAngle = 0f;
	public boolean craftingDragging = false;
	public double craftingDragLastX = 0;

	// ── Layer visibility (-1 = show all) ─────────────────────────
	public int craftingVisibleLayer = -1;
	/** Updated by the model renderer each frame. */
	public int craftingMaxLayer = 0;

	// ── Scroll offsets ───────────────────────────────────────────
	public int craftingSidebarScroll = 0;
	public int craftingInfoScroll = 0;

	// ── Data helpers ─────────────────────────────────────────────

	/** Clears and rebuilds {@link #craftingByTier} from {@link #craftingRecipes}. */
	public void rebuildTierMap() {
		craftingByTier.clear();
		for (String name : TIER_NAMES) craftingByTier.put(name, new ArrayList<>());
		for (BloodStructureRecipe r : craftingRecipes) {
			for (int i = 0; i < TIER_THRESHOLDS.length; i++) {
				if (r.getBloodCost() <= TIER_THRESHOLDS[i]) {
					craftingByTier.get(TIER_NAMES[i]).add(r);
					break;
				}
			}
		}
	}

	/** Selects the first accessible tier that contains at least one recipe. */
	public void autoSelectFirstTier(int playerDegree) {
		selectedCraftingTier = null;
		selectedCraftingIndexInTier = 0;
		for (int i = 0; i < TIER_NAMES.length; i++) {
			if (!craftingByTier.getOrDefault(TIER_NAMES[i], List.of()).isEmpty()
					&& playerDegree >= TIER_DEGREE_REQ[i]) {
				selectedCraftingTier = TIER_NAMES[i];
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
			if (name.equals(selectedCraftingTier)) {
				total += rowH + 2 + craftingByTier.getOrDefault(name, List.of()).size() * 18;
			}
		}
		return total;
	}

	public void clampSidebarScroll(int visibleH) {
		craftingSidebarScroll = Math.min(craftingSidebarScroll,
				Math.max(0, sidebarContentH() - visibleH));
	}
}
