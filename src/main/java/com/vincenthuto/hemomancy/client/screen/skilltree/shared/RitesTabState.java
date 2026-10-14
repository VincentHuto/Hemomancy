package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.RecipeDegreeGates;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Holds all mutable state for the Cardinal Rites browser tab.
 * <p>
 * Shared by both the Harbinger and Unstained progress screens; passed into the
 * static methods on {@link RitesTabView} each frame. Separating state from
 * rendering logic reduces the size of the main screen class.
 */
public class RitesTabState {
	/** Rites that open the next stretch of the Unstained path, in path order; they lead their stage's list. */
	static final List<String> GATEWAY_RITES = List.of("cardinal_rite/lethean_baptism",
			"cardinal_rite/closed_vein", "cardinal_rite/clarity_ascension");
	public static final int RECIPE_ROW_H = 16;
	public static final int RECIPE_ROW_H_WRAPPED = 26;
	public static final int RECIPE_ROW_GAP = 2;

	// ── Loaded recipe data ───────────────────────────────────────
	public final List<CardinalRiteRecipe> riteRecipes = new ArrayList<>();
	public final Map<Integer, List<CardinalRiteRecipe>> ritesByTier = new LinkedHashMap<>();
	/** Sidebar name lines per rite (one or two), wrapped by the view against the live font. */
	public final Map<ResourceLocation, List<String>> sidebarLabels = new HashMap<>();

	// ── Selection / navigation ───────────────────────────────────
	public Integer selectedRiteTier = null;
	public int selectedRiteIndexInTier = 0;

	// ── 3D model rotation ────────────────────────────────────────
	public float riteRotationAngle = 0f;
	public boolean riteDragging = false;
	public double riteDragLastX = 0;

	// ── Layer visibility (-1 = show all) ─────────────────────────
	public int riteVisibleLayer = -1;
	/** Updated by the model renderer each frame. */
	public int riteMaxLayer = 0;

	// ── Scroll offsets ───────────────────────────────────────────
	public int riteSidebarScroll = 0;
	public int riteInfoScroll = 0;
	public boolean showIchorianSigils = true;
	public boolean ichorianSigilsExpanded = false;
	public ResourceLocation selectedIchorianSigilId;
	public long ichorianSigilPreviewStartTick;

	// ── Theme / behaviour flags ───────────────────────────────────
	/** Accent colour used for borders, header text, layer buttons, and accents (default: Harbinger purple). */
	public int  tabColor          = 0xFF8844CC;
	/** Horizontal separator colour under the tier-sidebar header (default: Harbinger dark purple). */
	public int  separatorColor    = 0xFF332244;
	/** Title/selected-item text colour in the sidebar and info panel (default: Harbinger pink-lavender). */
	public int  nameColor         = 0xFFDDBBEE;
	/** Background colour for the selected sidebar row (default: Harbinger dark purple). */
	public int  rowBgSelected     = 0xDD120818;
	/** Background colour for a hovered sidebar row (default: Harbinger dark purple). */
	public int  rowBgHovered      = 0xBB100616;
	/** Background colour for a normal (unselected) sidebar row (default: Harbinger dark purple). */
	public int  rowBgNormal       = 0x990C0410;
	/** When {@code false}, all tiers are accessible regardless of player degree. */
	public boolean enableDegreeLock = true;
	/** When {@code true}, tiers with zero recipes are hidden from the sidebar. */
	public boolean hideEmptyTiers   = false;
	/** When {@code true}, tiers are headed by Unstained stage names instead of Harbinger degrees. */
	public boolean unstainedStages  = false;

	// ── Data helpers ─────────────────────────────────────────────

	/** Clears and rebuilds {@link #ritesByTier} from {@link #riteRecipes}. */
	public void rebuildTierMap() {
		ritesByTier.clear();
		for (int type : RecipeDegreeGates.LEVELS) {
			ritesByTier.put(type, new ArrayList<>());
		}
		for (CardinalRiteRecipe r : riteRecipes) {
			ritesByTier.get(RecipeDegreeGates.getRequiredDegree(r)).add(r);
		}
		for (List<CardinalRiteRecipe> list : ritesByTier.values()) {
			list.sort(sidebarOrder(recipe -> recipe.getId().getPath(), RitesTabState::displayName));
		}
	}

	/** Gateway rites first in path order, then alphabetical by the name the player reads. */
	static <T> Comparator<T> sidebarOrder(Function<T, String> path, Function<T, String> name) {
		return Comparator.comparingInt((T item) -> gatewayRank(path.apply(item)))
				.thenComparing(name, String.CASE_INSENSITIVE_ORDER)
				.thenComparing(path);
	}

	private static int gatewayRank(String path) {
		int rank = GATEWAY_RITES.indexOf(path);
		return rank < 0 ? GATEWAY_RITES.size() : rank;
	}

	public static String displayName(CardinalRiteRecipe recipe) {
		String name = recipe.displayName().getString();
		if (name != null && !name.isEmpty()) return name;
		String path = recipe.getId().getPath();
		if (path.contains("/")) path = path.substring(path.lastIndexOf('/') + 1);
		return HLTextUtils.toProperCase(path.replace("_", " "));
	}

	public String sidebarTitle() {
		return unstainedStages ? "Rites by Stage" : "Rite Degrees";
	}

	public String tierLabel(int level) {
		if (unstainedStages) return RecipeDegreeGates.unstainedStageLabel(level);
		return level == 0 ? "No Degree" : "Degree " + level;
	}

	public int recipeRowHeight(CardinalRiteRecipe recipe) {
		return sidebarLabels.getOrDefault(recipe.getId(), List.of()).size() > 1 ? RECIPE_ROW_H_WRAPPED : RECIPE_ROW_H;
	}

	/** Height of an expanded tier's rite rows, including the gap after each. */
	public int expandedRowsHeight(List<CardinalRiteRecipe> recipes) {
		int total = 0;
		for (CardinalRiteRecipe recipe : recipes) total += recipeRowHeight(recipe) + RECIPE_ROW_GAP;
		return total;
	}

	/** Selects the first accessible tier that contains at least one recipe. */
	public void autoSelectFirstTier(int playerDegree) {
		selectedRiteTier = null;
		selectedRiteIndexInTier = 0;
		for (int type : RecipeDegreeGates.LEVELS) {
			if (!ritesByTier.getOrDefault(type, List.of()).isEmpty()
					&& (!enableDegreeLock || playerDegree >= type)) {
				selectedRiteTier = type;
				break;
			}
		}
	}

	// ── Scroll helpers ───────────────────────────────────────────

	/** Total content height of the tier sidebar (for scroll clamping). */
	public int sidebarContentH() {
		int rowH = 22;
		int total = 0;
		for (int type : RecipeDegreeGates.LEVELS) {
			List<CardinalRiteRecipe> recipes = ritesByTier.getOrDefault(type, List.of());
			if (hideEmptyTiers && recipes.isEmpty()) continue;
			total += rowH + 2;
			if (this.selectedRiteTier != null) {
				if (type == selectedRiteTier) {
					total += rowH + 2 + expandedRowsHeight(recipes);
				}
			}

		}
		return total;
	}

	public void clampSidebarScroll(int visibleH) {
		riteSidebarScroll = Math.min(riteSidebarScroll,
				Math.max(0, sidebarContentH() - visibleH));
	}
}
