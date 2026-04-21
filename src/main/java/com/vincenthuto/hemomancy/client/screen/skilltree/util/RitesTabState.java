package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.HarbingerProgressScreen;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;

/**
 * Holds all mutable state for the Cardinal Rites browser tab.
 * <p>
 * Owned by {@link HarbingerProgressScreen}; passed into the static methods on
 * {@link RitesTabView} each frame. Separating state from rendering logic
 * reduces the size of the main screen class.
 */
public class RitesTabState {

	// ── Loaded recipe data ───────────────────────────────────────
	public final List<CardinalRiteRecipe> riteRecipes = new ArrayList<>();
	public final Map<CardinalRiteType, List<CardinalRiteRecipe>> ritesByTier = new LinkedHashMap<>();

	// ── Selection / navigation ───────────────────────────────────
	public CardinalRiteType selectedRiteTier = null;
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

	// ── Data helpers ─────────────────────────────────────────────

	/** Clears and rebuilds {@link #ritesByTier} from {@link #riteRecipes}. */
	public void rebuildTierMap() {
		ritesByTier.clear();
		for (CardinalRiteType type : CardinalRiteType.values()) {
			ritesByTier.put(type, new ArrayList<>());
		}
		for (CardinalRiteRecipe r : riteRecipes) {
			ritesByTier.get(r.getRiteType()).add(r);
		}
		for (List<CardinalRiteRecipe> list : ritesByTier.values()) {
			list.sort(java.util.Comparator.comparingDouble(CardinalRiteRecipe::getBloodCost));
		}
	}

	/** Selects the first accessible tier that contains at least one recipe. */
	public void autoSelectFirstTier(int playerDegree) {
		selectedRiteTier = null;
		selectedRiteIndexInTier = 0;
		for (CardinalRiteType type : CardinalRiteType.values()) {
			if (!ritesByTier.getOrDefault(type, List.of()).isEmpty()
					&& playerDegree >= RitesTabView.minDegree(type)) {
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
		for (CardinalRiteType type : CardinalRiteType.values()) {
			total += rowH + 2;
			if (type == selectedRiteTier) {
				total += rowH + 2 + ritesByTier.getOrDefault(type, List.of()).size() * 18;
			}
		}
		return total;
	}

	public void clampSidebarScroll(int visibleH) {
		riteSidebarScroll = Math.min(riteSidebarScroll,
				Math.max(0, sidebarContentH() - visibleH));
	}
}
