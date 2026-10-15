package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.common.recipe.RecipeDegreeGates;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds all mutable state for the Blood Crafting browser tab.
 * <p>
 * Shared by both the Harbinger and Unstained progress screens; passed into the
 * static methods on {@link CraftingTabView} each frame.
 */
public class CraftingTabState {

	// ── Tier configuration (constant, not per-recipe) ────────────
	public static final String[] TIER_NAMES       = {
			"No Degree", "Degree 1", "Degree 2", "Degree 3", "Degree 4",
			"Degree 5", "Degree 6", "Degree 7", "Degree 8"
	};
	public static final int[]    TIER_DEGREE_REQ  = RecipeDegreeGates.LEVELS;

	// ── Loaded recipe data ───────────────────────────────────────
	public final List<CraftingMapRecipe> craftingRecipes = new ArrayList<>();
	public final Map<String, List<CraftingMapRecipe>> craftingByTier = new LinkedHashMap<>();
	/** Sidebar name lines per recipe (one or two), wrapped by the view against the live font. */
	public final Map<ResourceLocation, List<String>> sidebarLabels = new HashMap<>();

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

	// ── Theme / behaviour flags ───────────────────────────────────
	/** Accent colour used for borders, header text, layer buttons, and accents (default: Harbinger red). */
	public int  tabColor          = 0xFFAA2222;
	/** Horizontal separator colour under the tier-sidebar header (default: Harbinger dark red). */
	public int  separatorColor    = 0xFF442222;
	/** Title/selected-item text colour in the sidebar and info panel (default: Harbinger gold). */
	public int  nameColor         = 0xFFEEAAAA;
	/** Background colour for the selected sidebar row (default: Harbinger dark red). */
	public int  rowBgSelected     = 0xDD1A0808;
	/** Background colour for a hovered sidebar row (default: Harbinger dark red). */
	public int  rowBgHovered      = 0xBB180505;
	/** Background colour for a normal (unselected) sidebar row (default: Harbinger dark red). */
	public int  rowBgNormal       = 0x99120303;
	/** When {@code false}, all tiers are accessible regardless of player degree. */
	public boolean enableDegreeLock = true;
	/** When {@code true}, tiers with zero recipes are hidden from the sidebar. */
	public boolean hideEmptyTiers   = false;
	/** When {@code true}, tiers are headed by Unstained stage names instead of Harbinger degrees. */
	public boolean unstainedStages  = false;

	// ── Data helpers ─────────────────────────────────────────────

	/** Clears and rebuilds {@link #craftingByTier} from {@link #craftingRecipes}. */
	public void rebuildTierMap() {
		craftingByTier.clear();
		for (String name : TIER_NAMES) craftingByTier.put(name, new ArrayList<>());
		for (CraftingMapRecipe r : craftingRecipes) {
			int degree = r.requiredDegree();
			craftingByTier.get(TIER_NAMES[degree]).add(r);
		}
		for (List<CraftingMapRecipe> list : craftingByTier.values()) {
			list.sort(RitesTabState.sidebarOrder(recipe -> recipe.getId().getPath(), CraftingTabState::displayName));
		}
	}

	/** The crafted item's name, falling back to the recipe path when the result is empty. */
	public static String displayName(CraftingMapRecipe recipe) {
		ItemStack result = recipe.getResult();
		if (result != null && !result.isEmpty()) return result.getHoverName().getString();
		String path = recipe.getId().getPath();
		if (path.contains("/")) path = path.substring(path.lastIndexOf('/') + 1);
		return HLTextUtils.toProperCase(path.replace("_", " "));
	}

	public String sidebarTitle() {
		return unstainedStages ? "Crafting by Stage" : "Tiers";
	}

	public String tierLabel(int tierIndex) {
		return unstainedStages ? RecipeDegreeGates.unstainedStageLabel(TIER_DEGREE_REQ[tierIndex]) : TIER_NAMES[tierIndex];
	}

	public boolean isTierHidden(String tierName) {
		return hideEmptyTiers && craftingByTier.getOrDefault(tierName, List.of()).isEmpty();
	}

	public int recipeRowHeight(CraftingMapRecipe recipe) {
		return sidebarLabels.getOrDefault(recipe.getId(), List.of()).size() > 1
				? RitesTabState.RECIPE_ROW_H_WRAPPED : RitesTabState.RECIPE_ROW_H;
	}

	/** Height of an expanded tier's recipe rows, including the gap after each. */
	public int expandedRowsHeight(List<CraftingMapRecipe> recipes) {
		int total = 0;
		for (CraftingMapRecipe recipe : recipes) total += recipeRowHeight(recipe) + RitesTabState.RECIPE_ROW_GAP;
		return total;
	}

	/** Selects the first accessible tier that contains at least one recipe. */
	public void autoSelectFirstTier(int playerDegree) {
		selectedCraftingTier = null;
		selectedCraftingIndexInTier = 0;
		for (int i = 0; i < TIER_NAMES.length; i++) {
			if (!craftingByTier.getOrDefault(TIER_NAMES[i], List.of()).isEmpty()
					&& (!enableDegreeLock || playerDegree >= TIER_DEGREE_REQ[i])) {
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
			if (isTierHidden(name)) continue;
			total += rowH + 2;
			if (name.equals(selectedCraftingTier)) {
				total += rowH + 2 + expandedRowsHeight(craftingByTier.getOrDefault(name, List.of()));
			}
		}
		return total;
	}

	public void clampSidebarScroll(int visibleH) {
		craftingSidebarScroll = Math.min(craftingSidebarScroll,
				Math.max(0, sidebarContentH() - visibleH));
	}
}
