package com.vincenthuto.hemomancy.client.screen.skilltree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.degree.EnumInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.degree.InitiatoryDegreeProvider;
import com.vincenthuto.hemomancy.common.capability.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.common.capability.player.skill.EnumSkillStates;
import com.vincenthuto.hemomancy.common.capability.player.skill.HemoMilestone;
import com.vincenthuto.hemomancy.common.capability.player.skill.SkillPoint;
import com.vincenthuto.hemomancy.common.init.ManipulationTreeInit;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.PacketUnlockSkill;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.BlockPosBlockPair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Skill tree screen opened from the Dendritic Distributor block.
 * <p>
 * Features:
 * <ul>
 *   <li>Animated vein background (identical to TendencyViewScreen / VascularViewScreen)</li>
 *   <li>Click-and-drag panning</li>
 *   <li>Scroll-wheel zoom (anchored to cursor position)</li>
 *   <li>Skill nodes built dynamically from {@link SkillPointInit}</li>
 * </ul>
 */
public class SkillTreeScreen extends Screen {

	// ── Tabs ──
	private enum Tab {
		SKILLS("Skills", 0xFFCC3333),
		MANIPULATIONS("Manipulations", 0xFFCC8833),
		CRAFTING("Crafting", 0xFFAA2222),
		RUNES("Runes", 0xFF44AACC),
		RITES("Rites", 0xFF8844CC),
		MATERIALS("Materials", 0xFFCC6644);

		final String label;
		final int color;
		Tab(String label, int color) { this.label = label; this.color = color; }
	}

	private Tab activeTab = Tab.SKILLS;
	private static final int TAB_HEIGHT = 16;
	private static final int TAB_PAD = 4;

	// ── Node layout (content-space pixels) ──
	private static final int NODE_SIZE = 26;
	private static final int NODE_GAP_X = 80;
	private static final int NODE_GAP_Y = 60;

	// ── Colours ──
	private static final int COL_LINE_LOCKED      = 0x88444444;
	private static final int COL_LINE_UNLOCKED     = 0xFFAA0000;
	private static final int COL_NODE_BG           = 0xCC1A0505;
	private static final int COL_NODE_BORDER_LOCK  = 0xFF333333;
	private static final int COL_NODE_BORDER_UNLOCK= 0xFFCC2222;
	private static final int COL_NODE_BORDER_AVAIL = 0xFFBB8833;

	// ── GUI viewport (screen-space pixels, set in init()) ──
	private int guiLeft, guiTop, guiWidth, guiHeight;

	// ── Per-tab pan / zoom (encapsulated in PanZoomState) ──
	private final PanZoomState skillView    = new PanZoomState();
	private final PanZoomState manipView    = new PanZoomState();
	private final PanZoomState materialView = new PanZoomState();
	/** Active view — points to whichever tab's PanZoomState is current. */
	private PanZoomState view = skillView;
	private boolean isDragging;

	// ── Pan limits ── (kept in PanZoomState.PAN_MARGIN)

	// ── Home button (top-left of GUI viewport) ──
	private static final int HOME_BTN_SIZE = 16;
	private static final int HOME_BTN_PAD  = 4;

	// ── Vein background (same system as TendencyViewScreen) ──
	private static final int VEIN_COUNT = 28;
	private float[][] veinParams;

	// ── Skill tree data ──
	private final Map<SkillPoint, int[]> nodePositions = new HashMap<>();
	private int skillContentW, skillContentH;

	// ── Manipulation tree data ──
	private final Map<ManipulationTreeEntry, int[]> manipPositions = new HashMap<>();
	private final Set<String> knownManipNames = new HashSet<>();
	/** Lookup from manipulation name → its BloodMemoryItem's ItemStack. */
	private final Map<String, ItemStack> manipMemoryItems = new HashMap<>();
	private int manipContentW, manipContentH;
	/** Currently selected manipulation node (null = none selected). */
	private ManipulationTreeEntry selectedManipEntry = null;

	// ── Cardinal Rites data ──
	private final List<CardinalRiteRecipe> riteRecipes = new ArrayList<>();
	/** Rites grouped by CardinalRiteType for tier-based display. */
	private final Map<CardinalRiteType, List<CardinalRiteRecipe>> ritesByTier = new java.util.LinkedHashMap<>();
	private CardinalRiteType selectedRiteTier = null;      // null = no tier selected yet
	private int selectedRiteIndexInTier = 0;               // index within the tier list
	private float riteRotationAngle = 0f;
	private boolean riteDragging = false;
	private double riteDragLastX = 0;
	private int riteVisibleLayer = -1;  // -1 = show all layers
	private int riteMaxLayer = 0;

	// ── Blood Crafting data ──
	private final List<BloodStructureRecipe> craftingRecipes = new ArrayList<>();
	/** Crafting recipes grouped by cost tier for tier-based display. */
	private final Map<String, List<BloodStructureRecipe>> craftingByTier = new java.util.LinkedHashMap<>();
	private static final String[] CRAFTING_TIER_NAMES = { "Basic", "Advanced", "Expert" };
	private static final int[] CRAFTING_TIER_THRESHOLDS = { 100, 200, Integer.MAX_VALUE };
	private static final int[] CRAFTING_TIER_DEGREE_REQ = { 0, 2, 4 };
	private String selectedCraftingTier = null;             // null = no tier selected yet
	private int selectedCraftingIndexInTier = 0;            // index within the tier list
	private float craftingRotationAngle = 0f;
	private boolean craftingDragging = false;
	private double craftingDragLastX = 0;
	private int craftingVisibleLayer = -1;  // -1 = show all layers
	private int craftingMaxLayer = 0;

	// ── Rune Chisel data ──
	private final List<com.vincenthuto.hemomancy.common.recipe.ChiselRecipe> chiselRecipes = new ArrayList<>();
	/** Chisel recipes grouped by tier for tier-based display. */
	private final Map<String, List<com.vincenthuto.hemomancy.common.recipe.ChiselRecipe>> chiselByTier = new java.util.LinkedHashMap<>();
	private static final String[] RUNE_TIER_NAMES = { "Tier 1", "Tier 2", "Tier 3" };
	private static final int[] RUNE_TIER_THRESHOLDS = { 1, 2, 3 };
	/** Minimum initiatory degree required to see each rune tier. */
	private static final int[] RUNE_TIER_DEGREE_REQ = { 4, 4, 5 };
	private String selectedRuneTier = null;
	private int selectedRuneIndexInTier = 0;

	// Shared nav button dimensions
	private static final int RITE_NAV_BTN_W = 24;
	private static final int RITE_NAV_BTN_H = 18;
	private static final int LAYER_BTN_SIZE = 16;

	// ── Tier sidebar scroll offsets ──
	private int riteSidebarScroll = 0;
	private int craftingSidebarScroll = 0;
	private int runeSidebarScroll = 0;

	// ── Info panel scroll offsets ──
	private int riteInfoScroll = 0;
	private int craftingInfoScroll = 0;

	// ── Materials & Processes data ──
	private final Map<MaterialEntry, int[]> materialPositions = new java.util.LinkedHashMap<>();
	private int materialContentW, materialContentH;
	private MaterialEntry selectedMaterial = null;

	/** Minimum player degree required to view recipes in each rite tier. */
	private static int riteMinDegree(CardinalRiteType type) {
		return switch (type) {
			case MINOR   -> 0;
			case LESSER  -> 1;
			case GREATER -> 3;
			case GRAND   -> 5;
		};
	}

	/** Minimum player degree required to see a manipulation of the given rank. */
	private static int manipMinDegree(EnumManipulationRank rank) {
		return switch (rank) {
			case HUMILIS      -> 0;
			case MEDIOCRITAS  -> 1;
			case SUMMA        -> 3;
			case MAGISTER     -> 5;
			case PERFECTUS    -> 6;
		};
	}

	/**
	 * Returns true if the given manipulation is above the player's current
	 * initiatory degree (i.e. its rank requires a higher degree).
	 */
	private boolean isManipRankLocked(BloodManipulation manip) {
		if (manip == null) return false;
		return playerDegree < manipMinDegree(manip.getRank());
	}

	/** Width of the tier sidebar on Crafting/Rites tabs (screen px). */
	private static final int TIER_SIDEBAR_W = 130;

	// ── Player initiatory degree (cached for rendering) ──
	private int playerDegree = 0;

	// ── Milestone drawer (left side, below home button) ──
	private static final int MILESTONE_DRAWER_W = 180;
	private static final int MILESTONE_TAB_W = 14;
	private static final int MILESTONE_TAB_H = 50;
	private boolean milestoneDrawerOpen = false;
	private int milestoneScrollOffset = 0;

	// ────────────────────────────────────────────────────────────
	//  Construction / opening
	// ────────────────────────────────────────────────────────────

	public SkillTreeScreen() {
		super(Component.translatable("screen.hemomancy.skill_tree"));
	}

	/** Call from the client side to open this screen. */
	public static void openScreen() {
		Minecraft.getInstance().setScreen(new SkillTreeScreen());
	}

	// ────────────────────────────────────────────────────────────
	//  Init
	// ────────────────────────────────────────────────────────────

	@Override
	protected void init() {
		super.init();
		RecipeLookup.clearCache();

		// Fill most of the window, leaving a small margin
		int margin = 16;
		guiLeft   = margin;
		guiTop    = margin;
		guiWidth  = width  - margin * 2;
		guiHeight = height - margin * 2;

		clearWidgets();
		buildLayout();
		buildManipLayout();
		buildMaterialLayout();
		seedVeinParams();
		cacheKnownManipulations();
		cacheRiteRecipes();
		cacheCraftingRecipes();
		cacheChiselRecipes();

		// Cache the player's initiatory degree for rendering
		if (Minecraft.getInstance().player != null) {
			playerDegree = Minecraft.getInstance().player
					.getCapability(InitiatoryDegreeProvider.DEGREE_CAPA)
					.map(d -> d.getDegreeNumber())
					.orElse(0);
		}

		// Centre each tab's content
		skillView.centreOn(skillContentW, skillContentH, guiWidth, guiHeight);
		manipView.centreOn(manipContentW, manipContentH, guiWidth, guiHeight);
		materialView.centreOn(materialContentW, materialContentH, guiWidth, guiHeight);

		view = viewForTab(activeTab);
	}

	/** Returns the PanZoomState for a given tab (null for browse tabs). */
	private PanZoomState viewForTab(Tab tab) {
		return switch (tab) {
			case SKILLS        -> skillView;
			case MANIPULATIONS -> manipView;
			case MATERIALS     -> materialView;
			default            -> view; // RITES / CRAFTING / RUNES don't pan
		};
	}

	/** Save current view state and switch to a new tab's view. */
	private void savePan() {
		if (activeTab == Tab.RITES || activeTab == Tab.CRAFTING || activeTab == Tab.RUNES) return;
		view.clamp(contentWForTab(activeTab), contentHForTab(activeTab), guiWidth, guiHeight);
	}

	private int contentWForTab(Tab tab) {
		return switch (tab) {
			case SKILLS        -> skillContentW;
			case MANIPULATIONS -> manipContentW;
			case MATERIALS     -> materialContentW;
			default            -> 0;
		};
	}
	private int contentHForTab(Tab tab) {
		return switch (tab) {
			case SKILLS        -> skillContentH;
			case MANIPULATIONS -> manipContentH;
			case MATERIALS     -> materialContentH;
			default            -> 0;
		};
	}

	/** Restore pan/zoom from the active tab's slot (alias kept for clarity). */
	private void restorePan() {
		view = viewForTab(activeTab);
	}

	/** Clamp pan for the active view. */
	private void clampPan() {
		view.clamp(contentWForTab(activeTab), contentHForTab(activeTab), guiWidth, guiHeight);
	}

	/** Reset pan/zoom to the centred default view for the active tab. */
	private void resetToHome() {
		view.centreOn(contentWForTab(activeTab), contentHForTab(activeTab), guiWidth, guiHeight);
	}

	/** Switch to a different tab. */
	private void switchTab(Tab tab) {
		if (tab == activeTab) return;
		savePan();
		activeTab = tab;
		view = viewForTab(activeTab);
	}

	private void seedVeinParams() {
		Random rand = new Random(42L);
		veinParams = new float[VEIN_COUNT][9];
		for (int i = 0; i < VEIN_COUNT; i++) {
			veinParams[i][0] = rand.nextFloat();                          // startX ratio
			veinParams[i][1] = rand.nextFloat();                          // startY ratio
			veinParams[i][2] = (float) (rand.nextFloat() * Math.PI * 2); // base angle
			veinParams[i][3] = 0.3f + rand.nextFloat() * 0.7f;           // speed
			veinParams[i][4] = 8f  + rand.nextFloat() * 18f;             // amplitude
			veinParams[i][5] = 0.04f + rand.nextFloat() * 0.08f;         // frequency
			veinParams[i][6] = 60 + rand.nextInt(120);                    // length
			veinParams[i][7] = 1  + rand.nextInt(3);                      // thickness
			veinParams[i][8] = rand.nextFloat();                           // brightness
		}
	}

	// ────────────────────────────────────────────────────────────
	//  Layout — build positions from SkillPointInit
	// ────────────────────────────────────────────────────────────

	private void buildLayout() {
		nodePositions.clear();
		skillContentW = 0;
		skillContentH = 0;

		for (List<SkillPoint> branch : SkillPointInit.SKILL_TREE) {
			if (branch.isEmpty()) continue;

			// Group by tree-depth
			Map<Integer, List<SkillPoint>> byDepth = new HashMap<>();
			for (SkillPoint sp : branch) {
				byDepth.computeIfAbsent(depth(sp), k -> new ArrayList<>()).add(sp);
			}

			int maxDepth = byDepth.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);

			// Widest row determines horizontal centering
			int widestRow = 0;
			for (int d = 0; d <= maxDepth; d++) {
				int n = byDepth.getOrDefault(d, List.of()).size();
				widestRow = Math.max(widestRow, (n - 1) * NODE_GAP_X + NODE_SIZE);
			}

			for (int d = 0; d <= maxDepth; d++) {
				List<SkillPoint> row = byDepth.getOrDefault(d, List.of());
				int n = row.size();
				int rowWidth = (n - 1) * NODE_GAP_X;
				int x0 = (widestRow - rowWidth) / 2;
				// Invert Y so root is at the bottom and deeper skills rise upward
				int y  = 40 + (maxDepth - d) * NODE_GAP_Y;
				for (int i = 0; i < n; i++) {
					int x = x0 + i * NODE_GAP_X;
					nodePositions.put(row.get(i), new int[]{x, y});
					skillContentW = Math.max(skillContentW, x + NODE_SIZE);
					skillContentH = Math.max(skillContentH, y + NODE_SIZE + 24);
				}
			}
		}
	}

	private static int depth(SkillPoint sp) {
		int d = 0;
		for (SkillPoint p = sp; p.getParent() != null; p = p.getParent()) d++;
		return d;
	}

	// ────────────────────────────────────────────────────────────
	//  Layout — manipulation tree from ManipulationTreeInit
	// ────────────────────────────────────────────────────────────

	private void buildManipLayout() {
		manipPositions.clear();
		manipContentW = 0;
		manipContentH = 0;
		if (ManipulationTreeInit.ENTRIES.isEmpty()) ManipulationTreeInit.init();

		for (ManipulationTreeEntry entry : ManipulationTreeInit.ENTRIES) {
			int x = entry.getX();
			int y = entry.getY();
			manipPositions.put(entry, new int[]{x, y});
			manipContentW = Math.max(manipContentW, x + NODE_SIZE + 20);
			manipContentH = Math.max(manipContentH, y + NODE_SIZE + 24);
		}
	}

	private void cacheKnownManipulations() {
		knownManipNames.clear();
		if (minecraft != null && minecraft.player != null) {
			minecraft.player.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent(cap -> {
				for (BloodManipulation m : cap.getManipList()) {
					if (m != null && m.getName() != null) {
						knownManipNames.add(m.getName());
					}
				}
			});
		}
		buildManipMemoryItemLookup();
	}

	/**
	 * Builds a lookup from manipulation name → its BloodMemoryItem's ItemStack.
	 * Scans all registered items for BloodMemoryItem instances. Only rebuilds
	 * when the cache is empty (item registrations don't change at runtime).
	 */
	private void buildManipMemoryItemLookup() {
		if (!manipMemoryItems.isEmpty()) return;
		for (Item item : net.minecraftforge.registries.ForgeRegistries.ITEMS) {
			if (item instanceof com.vincenthuto.hemomancy.common.item.memories.BloodMemoryItem memItem) {
				BloodManipulation manip = memItem.getManip();
				if (manip != null && manip.getName() != null) {
					manipMemoryItems.put(manip.getName(), new ItemStack(item));
				}
			}
		}
	}

	private void cacheRiteRecipes() {
		riteRecipes.clear();
		ritesByTier.clear();
		if (minecraft != null && minecraft.player != null && minecraft.level != null) {
			riteRecipes.addAll(CardinalRiteRecipe.getAllRecipes(minecraft.level));
		}
		// Group by rite type (tier)
		for (CardinalRiteType type : CardinalRiteType.values()) {
			ritesByTier.put(type, new ArrayList<>());
		}
		for (CardinalRiteRecipe recipe : riteRecipes) {
			ritesByTier.get(recipe.getRiteType()).add(recipe);
		}
		// Sort each tier by blood cost (ascending) so progression chains like
		// dried gourd → pallid → crimson → ashen → horn appear in order
		for (List<CardinalRiteRecipe> tierList : ritesByTier.values()) {
			tierList.sort(java.util.Comparator.comparingDouble(CardinalRiteRecipe::getBloodCost));
		}
		// Default selection: first accessible tier with recipes
		selectedRiteTier = null;
		selectedRiteIndexInTier = 0;
		for (CardinalRiteType type : CardinalRiteType.values()) {
			if (!ritesByTier.getOrDefault(type, List.of()).isEmpty()
					&& playerDegree >= riteMinDegree(type)) {
				selectedRiteTier = type;
				break;
			}
		}
	}

	private void cacheCraftingRecipes() {
		craftingRecipes.clear();
		craftingByTier.clear();
		if (minecraft != null && minecraft.player != null && minecraft.level != null) {
			craftingRecipes.addAll(BloodStructureRecipe.getAllRecipes(minecraft.level));
		}
		// Initialise empty tier lists
		for (String tierName : CRAFTING_TIER_NAMES) {
			craftingByTier.put(tierName, new ArrayList<>());
		}
		// Sort recipes into blood-cost tiers
		for (BloodStructureRecipe recipe : craftingRecipes) {
			for (int i = 0; i < CRAFTING_TIER_THRESHOLDS.length; i++) {
				if (recipe.getBloodCost() <= CRAFTING_TIER_THRESHOLDS[i]) {
					craftingByTier.get(CRAFTING_TIER_NAMES[i]).add(recipe);
					break;
				}
			}
		}
		// Default selection: first accessible tier with recipes
		selectedCraftingTier = null;
		selectedCraftingIndexInTier = 0;
		for (int i = 0; i < CRAFTING_TIER_NAMES.length; i++) {
			if (!craftingByTier.getOrDefault(CRAFTING_TIER_NAMES[i], List.of()).isEmpty()
					&& playerDegree >= CRAFTING_TIER_DEGREE_REQ[i]) {
				selectedCraftingTier = CRAFTING_TIER_NAMES[i];
				break;
			}
		}
	}

	private void cacheChiselRecipes() {
		chiselRecipes.clear();
		chiselByTier.clear();
		if (minecraft != null && minecraft.player != null && minecraft.level != null) {
			chiselRecipes.addAll(com.vincenthuto.hemomancy.common.recipe.ChiselRecipe.getAllRecipes(minecraft.level));
		}
		// Initialise empty tier lists
		for (String tierName : RUNE_TIER_NAMES) {
			chiselByTier.put(tierName, new ArrayList<>());
		}
		// Sort recipes into tiers based on recipe tier value
		for (com.vincenthuto.hemomancy.common.recipe.ChiselRecipe recipe : chiselRecipes) {
			for (int i = 0; i < RUNE_TIER_THRESHOLDS.length; i++) {
				if (recipe.getTier() <= RUNE_TIER_THRESHOLDS[i]) {
					chiselByTier.get(RUNE_TIER_NAMES[i]).add(recipe);
					break;
				}
			}
		}
		// Default selection: first accessible tier with recipes
		selectedRuneTier = null;
		selectedRuneIndexInTier = 0;
		for (int i = 0; i < RUNE_TIER_NAMES.length; i++) {
			if (!chiselByTier.getOrDefault(RUNE_TIER_NAMES[i], List.of()).isEmpty()
					&& playerDegree >= RUNE_TIER_DEGREE_REQ[i]) {
				selectedRuneTier = RUNE_TIER_NAMES[i];
				break;
			}
		}
	}

	// ────────────────────────────────────────────────────────────
	//  Coordinate helpers  (content ↔ screen) — delegate to view
	// ────────────────────────────────────────────────────────────

	/** Content-space → screen-space X */
	private int sx(int cx) { return view.sx(guiLeft, cx); }
	/** Content-space → screen-space Y */
	private int sy(int cy) { return view.sy(guiTop, cy); }
	/** Screen-space → content-space X */
	private double cx(double sx) { return view.cx(guiLeft, sx); }
	/** Screen-space → content-space Y */
	private double cy(double sy) { return view.cy(guiTop, sy); }
	/** Node half-size on screen, accounting for zoom */
	private int halfNode() { return view.halfNode(NODE_SIZE); }
	/** Convenience: current zoom level */

	private boolean insideGui(double mx, double my) {
		return mx >= guiLeft && mx < guiLeft + guiWidth
			&& my >= guiTop  && my < guiTop  + guiHeight;
	}

	// ────────────────────────────────────────────────────────────
	//  Input: drag-to-pan, scroll-to-zoom, click-to-unlock
	// ────────────────────────────────────────────────────────────

	@Override
	public boolean mouseClicked(double mx, double my, int btn) {
		if (btn == 0) {
			// Check home button click first (not on browse tabs)
			if (activeTab != Tab.RITES && activeTab != Tab.CRAFTING && activeTab != Tab.RUNES && isOverHomeButton(mx, my)) {
				resetToHome();
				return true;
			}

			// Check tab clicks first
			Tab clickedTab = tabUnder(mx, my);
			if (clickedTab != null) {
				switchTab(clickedTab);
				return true;
			}

			// Check milestone drawer toggle (Skills tab only)
			if (activeTab == Tab.SKILLS && isOverMilestoneToggle(mx, my)) {
				milestoneDrawerOpen = !milestoneDrawerOpen;
				milestoneScrollOffset = 0;
				return true;
			}

			if (insideGui(mx, my)) {
				if (activeTab == Tab.CRAFTING) {
					// Check tier sidebar click
					String clickedTier = craftingTierUnder(mx, my);
					if (clickedTier != null) {
						// Only allow selecting unlocked tiers
						int tierIdx = java.util.Arrays.asList(CRAFTING_TIER_NAMES).indexOf(clickedTier);
						if (tierIdx >= 0 && playerDegree >= CRAFTING_TIER_DEGREE_REQ[tierIdx]) {
							// Toggle: collapse if already selected, otherwise open
							if (clickedTier.equals(selectedCraftingTier)) {
								selectedCraftingTier = null;
								craftingSidebarScroll = 0;
								craftingInfoScroll = 0;
							} else {
								selectedCraftingTier = clickedTier;
								selectedCraftingIndexInTier = 0;
								craftingVisibleLayer = -1;
								craftingSidebarScroll = 0;
								craftingInfoScroll = 0;
							}
						}
						return true;
					}
					// Check recipe list click
					int clickedRecipeIdx = craftingRecipeUnder(mx, my);
					if (clickedRecipeIdx >= 0) {
						selectedCraftingIndexInTier = clickedRecipeIdx;
						craftingVisibleLayer = -1;
						craftingInfoScroll = 0;
						return true;
					}
					// Check layer buttons
					if (isOverLayerUpButton(mx, my, Tab.CRAFTING)) {
						if (craftingVisibleLayer == -1) craftingVisibleLayer = craftingMaxLayer;
						else if (craftingVisibleLayer < craftingMaxLayer) craftingVisibleLayer++;
						else craftingVisibleLayer = -1; // wrap to "all"
						return true;
					}
					if (isOverLayerDownButton(mx, my, Tab.CRAFTING)) {
						if (craftingVisibleLayer == -1) craftingVisibleLayer = 0;
						else if (craftingVisibleLayer > 0) craftingVisibleLayer--;
						else craftingVisibleLayer = -1; // wrap to "all"
						return true;
					}
					// Start rotation drag (only in the model area)
					if (mx >= guiLeft + TIER_SIDEBAR_W + 4) {
						craftingDragging = true;
						craftingDragLastX = mx;
					}
					return true;
				}
				if (activeTab == Tab.RUNES) {
					// Check tier sidebar click
					String clickedRuneTier = runeTierUnder(mx, my);
					if (clickedRuneTier != null) {
						int tierIdx = java.util.Arrays.asList(RUNE_TIER_NAMES).indexOf(clickedRuneTier);
						if (tierIdx >= 0 && playerDegree >= RUNE_TIER_DEGREE_REQ[tierIdx]) {
							// Toggle: collapse if already selected, otherwise open
							if (clickedRuneTier.equals(selectedRuneTier)) {
								selectedRuneTier = null;
								runeSidebarScroll = 0;
							} else {
								selectedRuneTier = clickedRuneTier;
								selectedRuneIndexInTier = 0;
								runeSidebarScroll = 0;
							}
						}
						return true;
					}
					// Check recipe list click
					int clickedRuneIdx = runeRecipeUnder(mx, my);
					if (clickedRuneIdx >= 0) {
						selectedRuneIndexInTier = clickedRuneIdx;
						return true;
					}
					return true;
				}
				if (activeTab == Tab.RITES) {
					// Check tier sidebar click
					CardinalRiteType clickedRiteTier = riteTierUnder(mx, my);
					if (clickedRiteTier != null) {
						if (playerDegree >= riteMinDegree(clickedRiteTier)
								&& !ritesByTier.getOrDefault(clickedRiteTier, List.of()).isEmpty()) {
							// Toggle: collapse if already selected, otherwise open
							if (clickedRiteTier == selectedRiteTier) {
								selectedRiteTier = null;
								riteSidebarScroll = 0;
								riteInfoScroll = 0;
							} else {
								selectedRiteTier = clickedRiteTier;
								selectedRiteIndexInTier = 0;
								riteVisibleLayer = -1;
								riteSidebarScroll = 0;
								riteInfoScroll = 0;
							}
						}
						return true;
					}
					// Check recipe list click
					int clickedRiteIdx = riteRecipeUnder(mx, my);
					if (clickedRiteIdx >= 0) {
						selectedRiteIndexInTier = clickedRiteIdx;
						riteVisibleLayer = -1;
						riteInfoScroll = 0;
						return true;
					}
					// Check layer buttons
					if (isOverLayerUpButton(mx, my, Tab.RITES)) {
						if (riteVisibleLayer == -1) riteVisibleLayer = riteMaxLayer;
						else if (riteVisibleLayer < riteMaxLayer) riteVisibleLayer++;
						else riteVisibleLayer = -1; // wrap to "all"
						return true;
					}
					if (isOverLayerDownButton(mx, my, Tab.RITES)) {
						if (riteVisibleLayer == -1) riteVisibleLayer = 0;
						else if (riteVisibleLayer > 0) riteVisibleLayer--;
						else riteVisibleLayer = -1; // wrap to "all"
						return true;
					}
					// Start rotation drag (only in the model area)
					if (mx >= guiLeft + TIER_SIDEBAR_W + 4) {
						riteDragging = true;
						riteDragLastX = mx;
					}
					return true;
				}
				if (activeTab == Tab.SKILLS) {
					SkillPoint hit = nodeUnder(mx, my);
					if (hit != null) {
						tryUnlock(hit);
						return true;
					}
				}
				if (activeTab == Tab.MANIPULATIONS && insideGui(mx, my)) {
					ManipulationTreeEntry manipHit = manipNodeUnder(mx, my);
					if (manipHit != null) {
						selectedManipEntry = (selectedManipEntry == manipHit) ? null : manipHit;
						return true;
					}
				}
				if (activeTab == Tab.MATERIALS) {
					MaterialEntry matHit = materialNodeUnder(mx, my);
					if (matHit != null) {
						selectedMaterial = (selectedMaterial == matHit) ? null : matHit;
						return true;
					}
				}
				isDragging = true;
				return true;
			}
		}
		return super.mouseClicked(mx, my, btn);
	}

	@Override
	public boolean mouseReleased(double mx, double my, int btn) {
		if (btn == 0) {
			isDragging = false;
			riteDragging = false;
			craftingDragging = false;
		}
		return super.mouseReleased(mx, my, btn);
	}

	@Override
	public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
		if (craftingDragging && btn == 0 && activeTab == Tab.CRAFTING) {
			craftingRotationAngle += (float)(mx - craftingDragLastX) * 0.8f;
			craftingDragLastX = mx;
			return true;
		}
		if (riteDragging && btn == 0 && activeTab == Tab.RITES) {
			riteRotationAngle += (float)(mx - riteDragLastX) * 0.8f;
			riteDragLastX = mx;
			return true;
		}
		if (isDragging && btn == 0) {
			view.applyDrag(dx, dy);
			savePan();
			return true;
		}
		return super.mouseDragged(mx, my, btn, dx, dy);
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double delta) {
		if (!insideGui(mx, my)) return super.mouseScrolled(mx, my, delta);

		// Milestone drawer scroll (Skills tab only, when drawer is open)
		if (activeTab == Tab.SKILLS && milestoneDrawerOpen && isOverMilestoneDrawer(mx, my)) {
			milestoneScrollOffset = Math.max(0, milestoneScrollOffset - (int)(delta * 12));
			return true;
		}

		// Rites, Crafting & Runes tabs — scroll the tier sidebar or info panel
		if (activeTab == Tab.RITES || activeTab == Tab.CRAFTING || activeTab == Tab.RUNES) {
			if (isOverTierSidebar(mx, my)) {
				int scrollAmt = (int)(-delta * 14);
				if (activeTab == Tab.RITES) {
					riteSidebarScroll = Math.max(0, riteSidebarScroll + scrollAmt);
					clampRiteSidebarScroll();
				} else if (activeTab == Tab.CRAFTING) {
					craftingSidebarScroll = Math.max(0, craftingSidebarScroll + scrollAmt);
					clampCraftingSidebarScroll();
				} else {
					runeSidebarScroll = Math.max(0, runeSidebarScroll + scrollAmt);
					clampRuneSidebarScroll();
				}
			} else {
				// Scroll the info panel (right side)
				int scrollAmt = (int)(-delta * 14);
				if (activeTab == Tab.RITES) {
					riteInfoScroll = Math.max(0, riteInfoScroll + scrollAmt);
				} else if (activeTab == Tab.CRAFTING) {
					craftingInfoScroll = Math.max(0, craftingInfoScroll + scrollAmt);
				}
			}
			return true;
		}

		// Remember the content-space point under the cursor
		double cxBefore = cx(mx);
		double cyBefore = cy(my);

		view.applyScroll(guiLeft, guiTop, mx, my, delta);

		// (cxBefore/cyBefore used only to anchor the zoom — already handled inside applyScroll)
		savePan();
		return true;
	}

	private SkillPoint nodeUnder(double mx, double my) {
		int h = halfNode();
		for (var e : nodePositions.entrySet()) {
			int[] p = e.getValue();
			int nx = sx(p[0]), ny = sy(p[1]);
			if (NodeShapeRenderer.isInside(e.getKey().getNodeShape(), mx, my, nx, ny, h))
				return e.getKey();
		}
		return null;
	}

	private ManipulationTreeEntry manipNodeUnder(double mx, double my) {
		int h = halfNode();
		for (var e : manipPositions.entrySet()) {
			int[] p = e.getValue();
			int nx = sx(p[0]), ny = sy(p[1]);
			if (NodeShapeRenderer.isInside(e.getKey().getNodeShape(), mx, my, nx, ny, h))
				return e.getKey();
		}
		return null;
	}
	private void tryUnlock(SkillPoint sp) {
		// Block interaction if player hasn't reached the required initiation degree
		if (sp.isDegreeLocked(playerDegree)) {
			return;
		}
		// Send packet to server — server validates cost, drains blood, applies change
		PacketHandler.CHANNELBLOODVOLUME.sendToServer(new PacketUnlockSkill(sp.getId()));
	}

	// ────────────────────────────────────────────────────────────
	//  Render
	// ────────────────────────────────────────────────────────────

	@Override
	public void render(GuiGraphics gfx, int mouseX, int mouseY, float partial) {
		// Dim world behind GUI
		renderBackground(gfx);

		// Auto-rotate rite model when not dragging
		if (activeTab == Tab.RITES && !riteDragging) {
			riteRotationAngle += partial * 0.4f;
		}
		if (activeTab == Tab.CRAFTING && !craftingDragging) {
			craftingRotationAngle += partial * 0.4f;
		}

		// ── 1. Animated vein background (scissored to GUI bounds) ──
		renderVeinBackground(gfx, guiLeft, guiTop, guiWidth, guiHeight);

		// ── 2. Border ──
		ScreenDrawUtils.drawBorder(gfx, guiLeft, guiTop, guiWidth, guiHeight, 0xFF330808, 0xFF220606);

		// ── 3. Tree content (scissored so it clips when panned) ──
		gfx.enableScissor(guiLeft + 2, guiTop + 2,
				guiLeft + guiWidth - 2, guiTop + guiHeight - 2);

		if (activeTab == Tab.SKILLS) {
			drawConnections(gfx);
			drawNodes(gfx);
		} else if (activeTab == Tab.MANIPULATIONS) {
			drawManipConnections(gfx);
			drawManipNodes(gfx);
		} else if (activeTab == Tab.CRAFTING) {
			drawCraftingContent(gfx, mouseX, mouseY, partial);
		} else if (activeTab == Tab.RUNES) {
			drawRunesContent(gfx, mouseX, mouseY, partial);
		} else if (activeTab == Tab.RITES) {
			drawRiteContent(gfx, mouseX, mouseY, partial);
		} else if (activeTab == Tab.MATERIALS) {
			drawMaterialNodes(gfx);
		}

		gfx.disableScissor();

		// 3b. Material info panel (outside scissor so it renders on top of nodes)
		if (activeTab == Tab.MATERIALS && selectedMaterial != null) {
			drawMaterialInfoPanel(gfx, selectedMaterial);
		}

		// 3c. Manipulation info panel (outside scissor so it renders on top of nodes)
		if (activeTab == Tab.MANIPULATIONS && selectedManipEntry != null) {
			drawManipInfoPanel(gfx, selectedManipEntry);
		}

		// ── 4. Tab buttons (top-right, outside scissor) ──
		drawTabs(gfx, mouseX, mouseY);

		// ── 4b. Home button (top-left, outside scissor; not on browse tabs) ──
		if (activeTab != Tab.RITES && activeTab != Tab.CRAFTING && activeTab != Tab.RUNES) {
			drawHomeButton(gfx, mouseX, mouseY);
		}

		// ── 4c. Milestone drawer (left side, below home button; Skills tab only) ──
		if (activeTab == Tab.SKILLS) {
			drawMilestoneDrawer(gfx, mouseX, mouseY);
		}

		// ── 5. Overlay text ──
//		gfx.drawCenteredString(font,
//				Component.literal(activeTab.label),
//				guiLeft + guiWidth / 2, guiTop + 5, activeTab.color);

		if (activeTab == Tab.SKILLS) {
			// Display current skill points to the right of the home button
			String spText = "Skill Points: " + SkillPointInit.skillPoints;
			gfx.drawString(font, Component.literal(spText)
					.withStyle(s -> s.withColor(0xFFBB8833).withBold(true)),
					guiLeft + HOME_BTN_PAD + HOME_BTN_SIZE + 4,
					guiTop + HOME_BTN_PAD + (HOME_BTN_SIZE - 8) / 2, 0);
		}

		if (activeTab != Tab.RITES && activeTab != Tab.CRAFTING && activeTab != Tab.RUNES) {
			// Hide zoom text when milestone drawer is open to avoid overlap
			if (!(activeTab == Tab.SKILLS && milestoneDrawerOpen)) {
				gfx.drawString(font,
						String.format("%.0f%%", view.zoom * 100),
						guiLeft + 5, guiTop + guiHeight - 12, 0x55888888, false);
			}
		}

		// ── 6. Tooltip (must be outside scissor) ──
		if (activeTab == Tab.SKILLS) {
			drawTooltip(gfx, mouseX, mouseY);
			// Milestone toggle tooltip
			if (isOverMilestoneToggle(mouseX, mouseY)) {
				String tipText = milestoneDrawerOpen ? "Hide Milestones" : "Show Milestones";
				gfx.renderTooltip(font, Component.literal(tipText), mouseX, mouseY);
			}
		} else if (activeTab == Tab.MANIPULATIONS) {
			drawManipTooltip(gfx, mouseX, mouseY);
		} else if (activeTab == Tab.MATERIALS) {
			drawMaterialTooltip(gfx, mouseX, mouseY);
		}

		super.render(gfx, mouseX, mouseY, partial);
	}

	// ────────────────────────────────────────────────────────────
	//  Vein Background  (exact copy of TendencyViewScreen logic)
	// ────────────────────────────────────────────────────────────

	private void renderVeinBackground(GuiGraphics gfx, int gx, int gy, int gw, int gh) {
		gfx.enableScissor(gx, gy, gx + gw, gy + gh);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		// Layer 1: solid near-black base filling GUI area
		gfx.fill(gx, gy, gx + gw, gy + gh, 0xFF0A0204);

		// Layer 2: subtle dark-red radial glow in the centre of the GUI
		int cx = gx + gw / 2;
		int cy = gy + gh / 2;
		int glowRadius = Math.max(gw, gh) / 2;
		for (int ring = glowRadius; ring > 0; ring -= 4) {
			float t = (float) ring / glowRadius;
			int alpha = (int) (35 * (1f - t));
			int red   = (int) (40 * (1f - t));
			int color = (alpha << 24) | (red << 16);
			gfx.fill(cx - ring, cy - ring, cx + ring, cy + ring, color);
		}

		// Layer 3: animated vein tendrils
		float time = System.nanoTime() / 1_000_000_000f;
		if (veinParams != null) {
			for (int i = 0; i < VEIN_COUNT; i++) {
				drawVeinTendril(gfx, i, time, gx, gy, gw, gh);
			}
		}

		// Layer 4: subtle noise-like speckles for organic texture
		Random speckRand = new Random(12345L);
		for (int s = 0; s < 120; s++) {
			int spx = gx + speckRand.nextInt(gw);
			int spy = gy + speckRand.nextInt(gh);
			int sr  = 10 + speckRand.nextInt(20);
			int sg  = speckRand.nextInt(6);
			int sa  = 15 + speckRand.nextInt(25);
			gfx.fill(spx, spy, spx + 1, spy + 1, (sa << 24) | (sr << 16) | (sg << 8));
		}

		RenderSystem.disableBlend();
		gfx.disableScissor();
	}

	/**
	 * Draws a single animated vein tendril as a squiggling curve within the
	 * GUI bounds — identical maths to TendencyViewScreen.drawVeinTendril().
	 */
	private void drawVeinTendril(GuiGraphics gfx, int index, float time,
								 int gx, int gy, int gw, int gh) {
		float[] p = veinParams[index];
		float startX    = gx + p[0] * gw;
		float startY    = gy + p[1] * gh;
		float baseAngle = p[2];
		float speed      = p[3];
		float amplitude  = p[4];
		float frequency  = p[5];
		int   length     = (int) p[6];
		int   thickness  = (int) p[7];
		float brightness = p[8];

		float angleDrift = baseAngle + 0.15f * Mth.sin(time * speed * 0.3f + index);
		float cosA = Mth.cos(angleDrift);
		float sinA = Mth.sin(angleDrift);

		float timeOffset = time * speed * 2.0f;

		int baseRed   = (int) (40 + 50 * brightness);
		int baseGreen = (int) (2  + 8  * brightness);
		int baseBlue  = (int) (5  + 5  * brightness);

		for (int step = 0; step < length; step++) {
			float squiggle      = amplitude         * Mth.sin(frequency * step + timeOffset);
			float microSquiggle = (amplitude * 0.3f) * Mth.sin(frequency * 2.7f * step + timeOffset * 1.4f + index);
			float displacement  = squiggle + microSquiggle;

			float px = startX + step * cosA * 1.5f - displacement * sinA;
			float py = startY + step * sinA * 1.5f + displacement * cosA;
			int ix = (int) px;
			int iy = (int) py;

			if (ix + thickness < gx || ix >= gx + gw || iy + thickness < gy || iy >= gy + gh)
				continue;

			float tipFade = 1f;
			if (step < 10)             tipFade = step / 10f;
			else if (step > length-10) tipFade = (length - step) / 10f;

			float pulse = 0.7f + 0.3f * Mth.sin(time * 1.5f + index * 0.5f + step * 0.02f);

			int a = (int) Mth.clamp(tipFade * pulse * 180, 20, 200);
			int r = (int) Mth.clamp(baseRed   * pulse,        0, 255);
			int g = (int) Mth.clamp(baseGreen * pulse * 0.5f, 0, 255);
			int b = (int) Mth.clamp(baseBlue  * pulse * 0.3f, 0, 255);

			gfx.fill(ix, iy, ix + thickness, iy + thickness,
					(a << 24) | (r << 16) | (g << 8) | b);
		}
	}
	// ────────────────────────────────────────────────────────────
	//  Tree: connections
	// ────────────────────────────────────────────────────────────

	private void drawConnections(GuiGraphics gfx) {
		int hn = halfNode();
		for (var e : nodePositions.entrySet()) {
			SkillPoint sp = e.getKey();
			if (sp.getParent() == null) continue;
			int[] cPos = e.getValue();
			int[] pPos = nodePositions.get(sp.getParent());
			if (pPos == null) continue;

			int x1 = sx(pPos[0]), y1 = sy(pPos[1]);
			int x2 = sx(cPos[0]), y2 = sy(cPos[1]);

			boolean parentUnlocked = sp.getParent().getState() == EnumSkillStates.UNLOCKED;
			int col = parentUnlocked ? COL_LINE_UNLOCKED : COL_LINE_LOCKED;
			int lw  = Math.max(1, (int)(view.zoom * 1.5f));

			// Elbow: parent down → horizontal → child up
			int midY = (y1 + y2) / 2;
			gfx.fill(x1 - lw, y1 + hn, x1 + lw, midY,       col);
			gfx.fill(Math.min(x1, x2) - lw, midY - lw,
					 Math.max(x1, x2) + lw, midY + lw,        col);
			gfx.fill(x2 - lw, midY,    x2 + lw, y2 - hn,     col);
		}
	}

	// ────────────────────────────────────────────────────────────
	//  Tree: nodes
	// ────────────────────────────────────────────────────────────

	private void drawNodes(GuiGraphics gfx) {
		float time = System.nanoTime() / 1_000_000_000f;
		int hn = halfNode();

		for (var e : nodePositions.entrySet()) {
			SkillPoint sp = e.getKey();
			int[] pos = e.getValue();
			int nx = sx(pos[0]);
			int ny = sy(pos[1]);

			boolean degreeLocked = sp.isDegreeLocked(playerDegree);
			EnumNodeShape shape = sp.getNodeShape();

			// ── determine border colour ──
			int border;
			if (degreeLocked) {
				border = COL_NODE_BORDER_LOCK;
			} else {
				switch (sp.getState()) {
					case UNLOCKED -> {
						border = COL_NODE_BORDER_UNLOCK;
						// pulsing glow
						float p = 0.7f + 0.3f * Mth.sin(time * 2f + sp.getId());
						int ga = (int)(40 * p);
						NodeShapeRenderer.drawFill(gfx, shape, nx, ny, hn + 3,
								(ga << 24) | 0x00AA0000);
					}
					case LOCKED -> {
						border = COL_NODE_BORDER_LOCK;
						if (sp.getParent() != null
								&& sp.getParent().getState() == EnumSkillStates.UNLOCKED)
							border = COL_NODE_BORDER_AVAIL;
					}
					default -> border = COL_NODE_BORDER_LOCK;
				}
			}

			// ── node fill ──
			NodeShapeRenderer.drawFill(gfx, shape, nx, ny, hn, COL_NODE_BG);

			// ── border ──
			NodeShapeRenderer.drawOutline(gfx, shape, nx, ny, hn, border);

			// ── degree-locked overlay: dark fill + black "?" ──
			if (degreeLocked) {
				NodeShapeRenderer.drawFill(gfx, shape, nx, ny, hn - 1, 0xBB000000);
				if (view.zoom >= 0.5f) {
					gfx.drawCenteredString(font, "?", nx, ny - 4, 0xFF111111);
				}
				continue; // skip normal text rendering for degree-locked nodes
			}

			// ── icon texture, icon item, or text (only when zoomed in enough) ──
			if (view.zoom >= 0.5f) {
				ResourceLocation iconTex = sp.getIconTexture();
				if (iconTex != null) {
					ScreenDrawUtils.renderScaledTexture(gfx, iconTex, nx, ny, hn);
				} else {
					ItemStack iconStack = sp.getIconItem();
					if (iconStack != null && !iconStack.isEmpty()) {
						ScreenDrawUtils.renderScaledItem(gfx , iconStack, nx, ny, hn);
					} else {
						String ini = getSkillInitial(sp);
						int textCol = sp.getState() == EnumSkillStates.UNLOCKED
								? 0xFFFFAAAA : 0xFF888888;
						gfx.drawCenteredString(font, ini, nx, ny - 4, textCol);
					}
				}

				// Show level progress below node
				if (sp.getMaxLevels() > 0) {
					String lvlStr = sp.getCurrentLevel() + "/" + sp.getMaxLevels();
					int lvlCol = sp.isMaxed() ? 0xFF44AA44 : 0xFF888888;
					gfx.drawCenteredString(font, lvlStr, nx, ny + hn + 3, lvlCol);
				}
			}
		}
	}

	// ────────────────────────────────────────────────────────────
	//  Tooltip
	// ────────────────────────────────────────────────────────────

	private void drawTooltip(GuiGraphics gfx, int mouseX, int mouseY) {
		if (!insideGui(mouseX, mouseY)) return;
		int hn = halfNode();

		for (var e : nodePositions.entrySet()) {
			SkillPoint sp = e.getKey();
			int[] pos = e.getValue();
			int nx = sx(pos[0]), ny = sy(pos[1]);

			if (!NodeShapeRenderer.isInside(sp.getNodeShape(), mouseX, mouseY, nx, ny, hn)) continue;

			List<Component> tip = new ArrayList<>();

			boolean degreeLocked = sp.isDegreeLocked(playerDegree);

			if (degreeLocked) {
				// Degree-locked: obscure the name and show the requirement
				tip.add(Component.literal("???")
						.withStyle(s -> s.withColor(0x555555).withBold(true)));
				EnumInitiatoryDegree needed = EnumInitiatoryDegree.byNumber(sp.getRequiredDegree());
				String degreeName = needed != null ? needed.getTitle() : ("Degree " + sp.getRequiredDegree());
				tip.add(Component.literal("Requires: " + degreeName)
						.withStyle(s -> s.withColor(0xAA4444)));
			} else {
				String pretty = HLTextUtils.toProperCase(
						sp.getName().replace("skill_", "").replace("_", " "));
				tip.add(Component.literal(pretty)
						.withStyle(s -> s.withColor(0xCC3333).withBold(true)));

				// Level info
				if (sp.getMaxLevels() > 0) {
					tip.add(Component.literal("Level: " + sp.getCurrentLevel() + " / " + sp.getMaxLevels())
							.withStyle(s -> s.withColor(sp.isMaxed() ? 0x44AA44 : 0x888888)));
				}

				// Description
				tip.add(Component.translatable("skill.hemomancy." + sp.getName() + ".desc")
						.withStyle(s -> s.withColor(0x999999).withItalic(true)));

				// Action / cost info
				if (sp.getState() == EnumSkillStates.LOCKED) {
					if (sp.getParent() != null && sp.getParent().getState() != EnumSkillStates.UNLOCKED) {
						String pn = HLTextUtils.toProperCase(
								sp.getParent().getName().replace("skill_", "").replace("_", " "));
						tip.add(Component.literal("Requires: " + pn)
								.withStyle(s -> s.withColor(0xAA4444)));
					} else {
						tip.add(Component.literal("Click to unlock! Cost: " + (int) sp.getLevelUpCost() + " mL + "
								+ sp.getSkillPointCost() + " SP")
								.withStyle(s -> s.withColor(0xBB8833)));
					}
				} else if (sp.getState() == EnumSkillStates.UNLOCKED) {
					if (sp.isMaxed()) {
						tip.add(Component.literal("MAX LEVEL")
								.withStyle(s -> s.withColor(0x44AA44).withBold(true)));
					} else {
						tip.add(Component.literal("Click to level up! Cost: " + (int) sp.getLevelUpCost() + " mL + "
								+ sp.getSkillPointCost() + " SP")
								.withStyle(s -> s.withColor(0xBB8833)));
					}
				}
			}

			gfx.renderTooltip(font, tip, Optional.empty(), mouseX, mouseY);
			break;
		}
	}

	// ────────────────────────────────────────────────────────────
	//  Manipulation tree: connections
	// ────────────────────────────────────────────────────────────

	private void drawManipConnections(GuiGraphics gfx) {
		int hn = halfNode();
		int lw = Math.max(1, (int)(view.zoom * 1.5f));

		for (ManipulationTreeEntry entry : ManipulationTreeInit.ENTRIES) {
			int[] childPos = manipPositions.get(entry);
			if (childPos == null) continue;
			boolean childKnown = knownManipNames.contains(entry.getManipName());
			boolean childLocked = isManipRankLocked(entry.resolve());

			for (String parentName : entry.getParentNames()) {
				ManipulationTreeEntry parentEntry = ManipulationTreeInit.getEntry(parentName);
				if (parentEntry == null) continue;
				int[] parentPos = manipPositions.get(parentEntry);
				if (parentPos == null) continue;

				boolean parentKnown = knownManipNames.contains(parentName);
				boolean parentLocked = isManipRankLocked(parentEntry.resolve());

				// If either end is rank-locked, use a very faint line
				int col;
				if (childLocked || parentLocked) {
					col = 0x44222222;
				} else if (parentKnown && childKnown) {
					// Get tendency color for the connection
					BloodManipulation manip = entry.resolve();
					if (manip != null) {
						ParticleColor pc = manip.getTend().getColor();
						int r = (int) Math.min(pc.getRed() * 0.7f, 255);
						int g = (int) Math.min(pc.getGreen() * 0.7f, 255);
						int b = (int) Math.min(pc.getBlue() * 0.7f, 255);
						col = 0xCC000000 | (r << 16) | (g << 8) | b;
					} else {
						col = 0xFFAA6600;
					}
				} else {
					col = COL_LINE_LOCKED;
				}

				int x1 = sx(parentPos[0]), y1 = sy(parentPos[1]);
				int x2 = sx(childPos[0]),  y2 = sy(childPos[1]);

				int midY = (y1 + y2) / 2;
				gfx.fill(x1 - lw, y1 + hn, x1 + lw, midY,    col);
				gfx.fill(Math.min(x1, x2) - lw, midY - lw,
						 Math.max(x1, x2) + lw, midY + lw,     col);
				gfx.fill(x2 - lw, midY,    x2 + lw, y2 - hn,  col);
			}
		}
	}

	// ────────────────────────────────────────────────────────────
	//  Manipulation tree: nodes
	// ────────────────────────────────────────────────────────────

	private void drawManipNodes(GuiGraphics gfx) {
		float time = System.nanoTime() / 1_000_000_000f;
		int hn = halfNode();

		for (var e : manipPositions.entrySet()) {
			ManipulationTreeEntry entry = e.getKey();
			int[] pos = e.getValue();
			int nx = sx(pos[0]);
			int ny = sy(pos[1]);

			BloodManipulation manip = entry.resolve();
			boolean known = knownManipNames.contains(entry.getManipName());
			boolean rankLocked = isManipRankLocked(manip);
			EnumNodeShape shape = entry.getNodeShape();

			// ── Tendency colour ──
			int tendR = 128, tendG = 128, tendB = 128;
			if (manip != null) {
				ParticleColor pc = manip.getTend().getColor();
				tendR = (int) pc.getRed();
				tendG = (int) pc.getGreen();
				tendB = (int) pc.getBlue();
			}

			// ── Border colour ──
			int borderColor;
			if (rankLocked) {
				borderColor = COL_NODE_BORDER_LOCK;
			} else if (known) {
				borderColor = 0xFF000000 | (tendR << 16) | (tendG << 8) | tendB;

				// Pulsing glow in tendency colour
				float pulse = 0.5f + 0.5f * Mth.sin(time * 2f + entry.getManipName().hashCode() * 0.1f);
				int ga = (int)(35 * pulse);
				int gr = (int)(tendR * 0.6f);
				int gg = (int)(tendG * 0.6f);
				int gb = (int)(tendB * 0.6f);
				NodeShapeRenderer.drawFill(gfx, shape, nx, ny, hn + 3,
						(ga << 24) | (gr << 16) | (gg << 8) | gb);
			} else {
				// Dimmed border for unknown
				int dr = (int)(tendR * 0.3f);
				int dg = (int)(tendG * 0.3f);
				int db = (int)(tendB * 0.3f);
				borderColor = 0xFF000000 | (dr << 16) | (dg << 8) | db;
			}

			// ── Selection highlight (bright pulsing glow when selected) ──
			boolean isSelected = entry == selectedManipEntry;
			if (isSelected) {
				float selPulse = 0.5f + 0.5f * Mth.sin(time * 3.0f);
				int selAlpha = (int)(55 * selPulse);
				int selColor = (selAlpha << 24) | (tendR << 16) | (tendG << 8) | tendB;
				NodeShapeRenderer.drawFill(gfx, shape, nx, ny, hn + 5, selColor);
			}

			// ── Fill ──
			int fill = known && !rankLocked ? COL_NODE_BG : 0xCC0D0303;
			NodeShapeRenderer.drawFill(gfx, shape, nx, ny, hn, fill);

			// ── Border ──
			NodeShapeRenderer.drawOutline(gfx, shape, nx, ny, hn, borderColor);

			// ── Rank-locked overlay: dark fill + "?" (mirrors skill degree-lock) ──
			if (rankLocked) {
				NodeShapeRenderer.drawFill(gfx, shape, nx, ny, hn - 1, 0xBB000000);
				if (view.zoom >= 0.5f) {
					gfx.drawCenteredString(font, "?", nx, ny - 4, 0xFF111111);
				}
				continue; // skip normal text rendering for rank-locked nodes
			}

			// ── Memory item icon or type symbol + name ──
			if (view.zoom >= 0.5f) {
				ItemStack memoryStack = manipMemoryItems.get(entry.getManipName());
				if (memoryStack != null && !memoryStack.isEmpty()) {
					ScreenDrawUtils.renderScaledItem(gfx, memoryStack, nx, ny, hn);
				} else {
					String sym = "?";
					if (manip != null) {
						sym = switch (manip.getType()) {
							case QUICK      -> "\u26A1"; // ⚡
							case CONTINUOUS -> "\u221E"; // ∞
							case PASSIVE    -> "\u25C6"; // ◆
							case CHARGED    -> "\u25B2"; // ▲
						};
					}
					int textCol = known ? 0xFFFFFFFF : 0xFF555555;
					gfx.drawCenteredString(font, sym, nx, ny - 4, textCol);
				}

				// Name below node (word-wrap to avoid overlapping neighbours)
				if (manip != null && view.zoom >= 0.7f) {
					String label = HLTextUtils.toProperCase(manip.getName().replace("_", " "));
					int labelCol = known ? (0xFF000000 | (tendR << 16) | (tendG << 8) | tendB) : 0xFF444444;
					int maxLabelW = Math.max(20, (int)(NODE_GAP_X * view.zoom) - 4);
					List<String> lines =ScreenDrawUtils.wrapText(font, label, maxLabelW);
					int ly = ny + hn + 3;
					for (String line : lines) {
						gfx.drawCenteredString(font, line, nx, ly, labelCol);
						ly += font.lineHeight;
					}
				}
			}
		}
	}

	// ────────────────────────────────────────────────────────────
	//  Manipulation tree: tooltips
	// ────────────────────────────────────────────────────────────

	private void drawManipTooltip(GuiGraphics gfx, int mouseX, int mouseY) {
		if (!insideGui(mouseX, mouseY)) return;
		int hn = halfNode();

		for (var e : manipPositions.entrySet()) {
			ManipulationTreeEntry entry = e.getKey();
			int[] pos = e.getValue();
			int nx = sx(pos[0]), ny = sy(pos[1]);

			if (!NodeShapeRenderer.isInside(entry.getNodeShape(), mouseX, mouseY, nx, ny, hn)) continue;

			BloodManipulation manip = entry.resolve();
			boolean known = knownManipNames.contains(entry.getManipName());
			boolean rankLocked = isManipRankLocked(manip);

			List<Component> tip = new ArrayList<>();

			if (rankLocked) {
				// Rank-locked: obscure the name and show the requirement
				tip.add(Component.literal("???")
						.withStyle(s -> s.withColor(0x555555).withBold(true)));
				if (manip != null) {
					int reqDeg = manipMinDegree(manip.getRank());
					EnumInitiatoryDegree needed = EnumInitiatoryDegree.byNumber(reqDeg);
					String degreeName = needed != null ? needed.getTitle() : ("Degree " + reqDeg);
					tip.add(Component.literal("Requires: " + degreeName)
							.withStyle(s -> s.withColor(0xAA4444)));
				}
			} else {
				// Name
				String pretty = manip != null
						? HLTextUtils.toProperCase(manip.getName().replace("_", " "))
						: HLTextUtils.toProperCase(entry.getManipName().replace("_", " "));

				int nameCol = known ? 0xFFAA44 : 0x888888;
				tip.add(Component.literal(pretty).withStyle(s -> s.withColor(nameCol).withBold(true)));

				// Known/Unknown status
				tip.add(Component.literal(known ? "Known" : "Unknown")
						.withStyle(s -> s.withColor(known ? 0x44AA44 : 0xAA4444).withItalic(!known)));

				if (manip != null) {
					// Type
					tip.add(Component.literal("Type: " + HLTextUtils.toProperCase(manip.getType().name()))
							.withStyle(s -> s.withColor(0x888888)));

					// Rank
					tip.add(Component.literal("Rank: " + HLTextUtils.toProperCase(manip.getRank().name()))
							.withStyle(s -> s.withColor(0x888888)));

					// Tendency
					ParticleColor pc = manip.getTend().getColor();
					int tendCol = (int)pc.getRed() << 16 | (int)pc.getGreen() << 8 | (int)pc.getBlue();
					String tendTipName = HLTextUtils.toProperCase(manip.getTend().name());
					double alignReq = manip.getAlignLevel();
					String tendTipText = alignReq > 0
							? "Tendency: " + tendTipName + " (" + (int) alignReq + ")"
							: "Tendency: " + tendTipName;
					tip.add(Component.literal(tendTipText)
							.withStyle(s -> s.withColor(tendCol)));

					// Blood cost
					tip.add(Component.literal("Blood Cost: " + (int)manip.getCost() + " mL")
							.withStyle(s -> s.withColor(0xAA4444)));

					// Section
					tip.add(Component.literal("Vein Section: " + HLTextUtils.toProperCase(manip.getSection().name()))
							.withStyle(s -> s.withColor(0x666666)));
				}

				// Parents
				if (!entry.getParentNames().isEmpty()) {
					StringBuilder sb = new StringBuilder("Relates to: ");
					for (int i = 0; i < entry.getParentNames().size(); i++) {
						if (i > 0) sb.append(", ");
						sb.append(HLTextUtils.toProperCase(entry.getParentNames().get(i).replace("_", " ")));
					}
					tip.add(Component.literal(sb.toString())
							.withStyle(s -> s.withColor(0x666666).withItalic(true)));
				}
			}

			gfx.renderTooltip(font, tip, Optional.empty(), mouseX, mouseY);
			break;
		}
	}

	// ────────────────────────────────────────────────────────────
	//  Manipulation info panel (right side, shown when a node is selected)
	// ────────────────────────────────────────────────────────────

	/**
	 * Draws the detail panel for the selected manipulation node on the right
	 * side of the screen. Shows the manipulation's stats and, if a corresponding
	 * BloodMemoryItem exists, its Memory Weaving recipe.
	 */
	private void drawManipInfoPanel(GuiGraphics gfx, ManipulationTreeEntry entry) {
		BloodManipulation manip = entry.resolve();
		if (manip == null) return;

		boolean known = knownManipNames.contains(entry.getManipName());
		boolean rankLocked = isManipRankLocked(manip);

		// ── Tendency colour ──
		ParticleColor pc = manip.getTend().getColor();
		int tendR = (int) pc.getRed();
		int tendG = (int) pc.getGreen();
		int tendB = (int) pc.getBlue();
		int tendCol = 0xFF000000 | (tendR << 16) | (tendG << 8) | tendB;

		// ── Panel geometry ──
		int panelW = 170;
		int panelX = guiLeft + guiWidth - panelW - 8;
		int panelY = guiTop + 30;
		int maxW = panelW - 16;
		int lineH = 11;

		// ── Pre-compute name wrapping ──
		String name;
		if (rankLocked) {
			name = "???";
		} else {
			name = HLTextUtils.toProperCase(manip.getName().replace("_", " "));
		}
		List<String> nameLines = ScreenDrawUtils.wrapText(font, name, maxW - 20);
		int nameRowH = Math.max(22, nameLines.size() * 10 + 4);

		// ── Look up the Memory Weaving recipe for this manipulation's memory item ──
		ItemStack memoryStack = manipMemoryItems.get(entry.getManipName());
		RecipeLookup.FoundRecipe foundRecipe = (memoryStack != null && !memoryStack.isEmpty())
				? RecipeLookup.find(memoryStack) : null;
		int recipeH = MiniRecipeRenderer.estimateHeight(foundRecipe);
		int recipeSection = recipeH > 0 ? recipeH + 12 : 0;

		// ── Total panel height ──
		int statsH = 0;
		if (!rankLocked) {
			statsH += lineH;       // known/unknown
			statsH += lineH;       // type
			statsH += lineH;       // rank
			statsH += lineH;       // tendency
			statsH += lineH;       // blood cost
			statsH += lineH;       // section
			if (manip.getCooldownTicks() > 0) statsH += lineH; // cooldown
		} else {
			statsH += lineH * 2;   // locked message
		}
		int panelH = 6 + nameRowH + 1 + 5 + statsH + recipeSection + 8;

		// ── Background ──
		gfx.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xDD1A0505);

		// ── Border ──
		ScreenDrawUtils.drawSimpleBorder(gfx, panelX, panelY, panelW, panelH, tendCol);

		int tx = panelX + 6;
		int ty = panelY + 6;

		// ── Icon + name row ──
		if (memoryStack != null && !memoryStack.isEmpty()) {
			gfx.renderItem(memoryStack, tx, ty);
		}
		int nameCol = rankLocked ? 0xFF555555 : tendCol;
		for (int li = 0; li < nameLines.size(); li++) {
			int nx = li == 0 ? tx + 20 : tx + 4;
			gfx.drawString(font,
					Component.literal(nameLines.get(li))
							.withStyle(s -> s.withColor(nameCol).withBold(true)),
					nx, ty + 4 + li * 10, 0, false);
		}
		ty += nameRowH;

		// ── Divider ──
		gfx.fill(tx, ty, panelX + panelW - 6, ty + 1, 0xFF442222);
		ty += 5;

		if (rankLocked) {
			// Locked message
			EnumInitiatoryDegree needed = EnumInitiatoryDegree.byNumber(manipMinDegree(manip.getRank()));
			String degreeName = needed != null ? needed.getTitle() : ("Degree " + manipMinDegree(manip.getRank()));
			gfx.drawString(font, "Locked", tx, ty, 0xFF555555, false);
			ty += lineH;
			gfx.drawString(font, "Requires: " + degreeName, tx, ty, 0xFFAA4444, false);
			ty += lineH;
		} else {
			// Known / Unknown
			String statusStr = known ? "Known" : "Unknown";
			int statusCol = known ? 0xFF44AA44 : 0xFFAA4444;
			gfx.drawString(font, statusStr, tx, ty, statusCol, false);
			ty += lineH;

			// Type
			gfx.drawString(font, Component.literal("Type: ")
							.withStyle(s -> s.withColor(0xFF888888))
							.append(Component.literal(HLTextUtils.toProperCase(manip.getType().name()))
									.withStyle(s -> s.withColor(0xFFCCCCCC))),
					tx, ty, 0, false);
			ty += lineH;

			// Rank
			gfx.drawString(font, Component.literal("Rank: ")
							.withStyle(s -> s.withColor(0xFF888888))
							.append(Component.literal(HLTextUtils.toProperCase(manip.getRank().name()))
									.withStyle(s -> s.withColor(0xFFCCCCCC))),
					tx, ty, 0, false);
			ty += lineH;

			// Tendency
			String tendName = HLTextUtils.toProperCase(manip.getTend().name());
			double alignReq = manip.getAlignLevel();
			String tendText = alignReq > 0
					? tendName + " (" + (int) alignReq + ")"
					: tendName;
			gfx.drawString(font, Component.literal("Tendency: ")
							.withStyle(s -> s.withColor(0xFF888888))
							.append(Component.literal(tendText)
									.withStyle(s -> s.withColor(tendCol))),
					tx, ty, 0, false);
			ty += lineH;

			// Blood cost
			gfx.drawString(font, Component.literal("Blood Cost: ")
							.withStyle(s -> s.withColor(0xFF888888))
							.append(Component.literal((int) manip.getCost() + " mL")
									.withStyle(s -> s.withColor(0xFFAA4444))),
					tx, ty, 0, false);
			ty += lineH;

			// Vein section
			gfx.drawString(font, Component.literal("Section: ")
							.withStyle(s -> s.withColor(0xFF888888))
							.append(Component.literal(HLTextUtils.toProperCase(manip.getSection().name()))
									.withStyle(s -> s.withColor(0xFFAAAAAA))),
					tx, ty, 0, false);
			ty += lineH;

			// Cooldown (if any)
			if (manip.getCooldownTicks() > 0) {
				float seconds = manip.getCooldownTicks() / 20f;
				gfx.drawString(font, Component.literal("Cooldown: ")
								.withStyle(s -> s.withColor(0xFF888888))
								.append(Component.literal(String.format("%.1fs", seconds))
										.withStyle(s -> s.withColor(0xFFAAAA88))),
						tx, ty, 0, false);
				ty += lineH;
			}
		}

		// ── Memory Weaving recipe preview ──
		if (foundRecipe != null) {
			ty += 3;
			gfx.fill(tx, ty, panelX + panelW - 6, ty + 1, 0xFF442222);
			ty += 4;
			MiniRecipeRenderer.draw(gfx, font, foundRecipe, tx, ty, maxW, tendCol, MiniRecipeRenderer.BLOOD);
		}
	}

	// ────────────────────────────────────────────────────────────
	//  Home button (top-left corner of the GUI)
	// ────────────────────────────────────────────────────────────

	private void drawHomeButton(GuiGraphics gfx, int mouseX, int mouseY) {
		int bx = guiLeft + HOME_BTN_PAD;
		int by = guiTop + HOME_BTN_PAD;
		boolean hovered = isOverHomeButton(mouseX, mouseY);

		ScreenDrawUtils.drawHomeButton(gfx, font, bx, by, HOME_BTN_SIZE, hovered,
				0xDD1A0505, 0x99120303,   // hoverBg, idleBg
				0xFFCC3333, 0xFF444444,   // hoverBorder, idleBorder
				0xFFFFAAAA, 0xFF888888);  // hoverText, idleText

		if (hovered) {
			gfx.renderTooltip(font, Component.literal("Return to Center"), mouseX, mouseY);
		}
	}

	// ────────────────────────────────────────────────────────────
	//  Tabs (top-right corner of the GUI)
	// ────────────────────────────────────────────────────────────

	/** Builds the tab descriptor list for the current state. */
	private List<ScreenDrawUtils.TabDesc> buildTabDescs() {
		List<ScreenDrawUtils.TabDesc> descs = new ArrayList<>();
		for (Tab tab : Tab.values()) {
			descs.add(new ScreenDrawUtils.TabDesc(tab.label, tab.color, tab == activeTab));
		}
		return descs;
	}

	private void drawTabs(GuiGraphics gfx, int mouseX, int mouseY) {
		ScreenDrawUtils.drawTabs(gfx, font, buildTabDescs(),
				guiLeft, guiTop, guiWidth, TAB_HEIGHT, TAB_PAD, mouseX, mouseY);
	}

	/** Returns the tab under the mouse, or null. */
	private Tab tabUnder(double mx, double my) {
		int idx = ScreenDrawUtils.tabIndexUnder(font, buildTabDescs(),
				guiLeft, guiTop, guiWidth, TAB_HEIGHT, TAB_PAD, mx, my);
		return idx >= 0 ? Tab.values()[idx] : null;
	}

	/** Returns true if the mouse is over the home button. */
	private boolean isOverHomeButton(double mx, double my) {
		int bx = guiLeft + HOME_BTN_PAD;
		int by = guiTop + HOME_BTN_PAD;
		return mx >= bx && mx <= bx + HOME_BTN_SIZE
			&& my >= by && my <= by + HOME_BTN_SIZE;
	}

	// ────────────────────────────────────────────────────────────
	//  Blood Crafting tab — tier-based layout
	// ────────────────────────────────────────────────────────────

	private void drawCraftingContent(GuiGraphics gfx, int mouseX, int mouseY, float partial) {
		if (craftingRecipes.isEmpty()) {
			gfx.drawCenteredString(font, "No Blood Crafting recipes found",
					guiLeft + guiWidth / 2, guiTop + guiHeight / 2, 0xFF666666);
			return;
		}

		// ── Recipe content (right of sidebar) ──
		int contentX = guiLeft + TIER_SIDEBAR_W + 6;
		int contentW = guiWidth - TIER_SIDEBAR_W - 10;

		if (selectedCraftingTier == null) {
			// Push z so these text labels are in front of any residual depth writes.
			gfx.pose().pushPose();
			gfx.pose().translate(0, 0, 400);
			drawCraftingTierSidebar(gfx, mouseX, mouseY);
			gfx.drawCenteredString(font, "Select a tier",
					contentX + contentW / 2, guiTop + guiHeight / 2, 0xFF555555);
			gfx.pose().popPose();
			return;
		}

		List<BloodStructureRecipe> tierRecipes = craftingByTier.getOrDefault(selectedCraftingTier, List.of());
		if (tierRecipes.isEmpty()) {
			gfx.pose().pushPose();
			gfx.pose().translate(0, 0, 400);
			drawCraftingTierSidebar(gfx, mouseX, mouseY);
			gfx.drawCenteredString(font, "No recipes in this tier",
					contentX + contentW / 2, guiTop + guiHeight / 2, 0xFF555555);
			gfx.pose().popPose();
			return;
		}
		if (selectedCraftingIndexInTier >= tierRecipes.size()) selectedCraftingIndexInTier = 0;
		BloodStructureRecipe recipe = tierRecipes.get(selectedCraftingIndexInTier);

		// Layout: left half of content = 3D model, right half = info panel
		int modelAreaW = contentW / 2;
		int modelX = contentX;
		int infoX = contentX + modelAreaW + 10;
		int infoW = contentW - modelAreaW - 20;

		// ── 3D multiblock preview (rendered first, at z=300) ──
		drawCraftingModel(gfx, recipe, modelX + 10, guiTop + 30,
				modelAreaW - 20, guiHeight - 60);

		// Push z above the 3D model (z=300) so ALL 2D overlays always draw on top of the blocks.
		gfx.pose().pushPose();
		gfx.pose().translate(0, 0, 400);

		// ── Tier sidebar (left) ──
		drawCraftingTierSidebar(gfx, mouseX, mouseY);

		// ── Layer buttons ──
		drawLayerButtons(gfx, mouseX, mouseY, Tab.CRAFTING, craftingVisibleLayer, craftingMaxLayer);

		// ── Info panel ──
		drawCraftingInfoPanel(gfx, recipe, infoX, guiTop + 30, infoW);

		// ── Drag hint ──
		gfx.drawCenteredString(font, "Drag to rotate",
				modelX + modelAreaW / 2, guiTop + guiHeight - 18, 0x44888888);

		gfx.pose().popPose();
	}

	/**
	 * Draws the tier sidebar for Blood Crafting, showing all tiers as rows.
	 * Locked tiers are greyed/obfuscated. Recipes within selected tier are listed below.
	 */
	private void drawCraftingTierSidebar(GuiGraphics gfx, int mouseX, int mouseY) {
		int sx = guiLeft + 4;
		int sy = guiTop + 24;
		int sw = TIER_SIDEBAR_W - 8;
		int rowH = 22;

		// Title (drawn above the scrollable area)
		gfx.drawString(font, Component.literal("Tiers")
				.withStyle(s -> s.withColor(Tab.CRAFTING.color).withBold(true)), sx + 2, sy, 0);
		sy += 14;

		// Separator
		gfx.fill(sx, sy, sx + sw, sy + 1, 0xFF442222);
		sy += 4;

		// Scissor to clip scrollable content within the sidebar
		int clipTop = sy;
		int clipBottom = guiTop + guiHeight - 4;
		gfx.enableScissor(sx, clipTop, sx + sw, clipBottom);

		// Apply scroll offset
		sy -= craftingSidebarScroll;

		// Tier rows
		for (int i = 0; i < CRAFTING_TIER_NAMES.length; i++) {
			String tierName = CRAFTING_TIER_NAMES[i];
			boolean locked = playerDegree < CRAFTING_TIER_DEGREE_REQ[i];
			boolean selected = tierName.equals(selectedCraftingTier);
			List<BloodStructureRecipe> recipes = craftingByTier.getOrDefault(tierName, List.of());

			boolean hovered = mouseX >= sx && mouseX <= sx + sw
					&& mouseY >= sy && mouseY <= sy + rowH
					&& mouseY >= clipTop && mouseY <= clipBottom;

			// Background
			int bg = selected ? 0xDD1A0808 : (hovered && !locked ? 0xBB180505 : 0x99120303);
			gfx.fill(sx, sy, sx + sw, sy + rowH, bg);

			// Border
			int bc = locked ? 0xFF333333 : (selected ? Tab.CRAFTING.color : 0xFF555555);
			gfx.fill(sx, sy, sx + sw, sy + 1, bc);
			gfx.fill(sx, sy + rowH - 1, sx + sw, sy + rowH, bc);
			gfx.fill(sx, sy, sx + 1, sy + rowH, bc);
			gfx.fill(sx + sw - 1, sy, sx + sw, sy + rowH, bc);

			if (locked) {
				// Dark overlay + lock indicator
				gfx.fill(sx + 1, sy + 1, sx + sw - 1, sy + rowH - 1, 0xBB000000);
				gfx.drawString(font, "[X] " + tierName + " (Locked)", sx + 4, sy + (rowH - 8) / 2, 0xFF444444, false);
			} else {
				String label = tierName + " (" + recipes.size() + ")";
				int textCol = selected ? 0xFFEEAAAA : 0xFF999999;
				gfx.drawString(font, label, sx + 4, sy + (rowH - 8) / 2, textCol, false);
			}

			// If this tier is selected, draw recipe list below
			if (selected && !locked) {
				sy += rowH + 2;
				for (int j = 0; j < recipes.size(); j++) {
					BloodStructureRecipe r = recipes.get(j);
					boolean recSel = (j == selectedCraftingIndexInTier);
					boolean recHov = mouseX >= sx + 4 && mouseX <= sx + sw - 4
							&& mouseY >= sy && mouseY <= sy + 16
							&& mouseY >= clipTop && mouseY <= clipBottom;

					int recBg = recSel ? 0xCC221010 : (recHov ? 0xAA1A0808 : 0x00000000);
					gfx.fill(sx + 2, sy, sx + sw - 2, sy + 16, recBg);

					if (recSel) {
						gfx.fill(sx + 2, sy, sx + 3, sy + 16, Tab.CRAFTING.color);
					}

					String recPath = r.getId().getPath();
					if (recPath.contains("/")) recPath = recPath.substring(recPath.lastIndexOf('/') + 1);
					String recName = HLTextUtils.toProperCase(recPath.replace("_", " "));
					int recCol = recSel ? 0xFFDDAAAA : 0xFF888888;
					gfx.drawString(font, recName, sx + 8, sy + 4, recCol, false);
					sy += 18;
				}
			}
			sy += rowH + 2;
		}

		gfx.disableScissor();

		// Draw scroll indicators if content overflows
		int contentH = craftingSidebarContentH();
		int visibleH = tierSidebarVisibleH();
		if (contentH > visibleH) {
			if (craftingSidebarScroll > 0) {
				gfx.drawCenteredString(font, "\u25B2", sx + sw / 2, clipTop, 0xAAFFFFFF);
			}
			if (craftingSidebarScroll < contentH - visibleH) {
				gfx.drawCenteredString(font, "\u25BC", sx + sw / 2, clipBottom - 10, 0xAAFFFFFF);
			}
		}
	}

	/** Returns the tier name clicked in the crafting sidebar, or null. */
	private String craftingTierUnder(double mx, double my) {
		int sx = guiLeft + 4;
		int sy = guiTop + 24 + 14 + 4;
		int sw = TIER_SIDEBAR_W - 8;
		int rowH = 22;

		int clipTop = sy;
		int clipBottom = guiTop + guiHeight - 4;
		if (my < clipTop || my > clipBottom) return null;

		// Apply scroll offset
		sy -= craftingSidebarScroll;

		for (int i = 0; i < CRAFTING_TIER_NAMES.length; i++) {
			String tierName = CRAFTING_TIER_NAMES[i];
			boolean selected = tierName.equals(selectedCraftingTier);
			List<BloodStructureRecipe> recipes = craftingByTier.getOrDefault(tierName, List.of());

			if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + rowH
					&& my >= clipTop && my <= clipBottom) {
				return tierName;
			}

			if (selected) {
				sy += rowH + 2 + recipes.size() * 18;
			}
			sy += rowH + 2;
		}
		return null;
	}

	/** Returns the recipe index clicked within the selected crafting tier, or -1. */
	private int craftingRecipeUnder(double mx, double my) {
		if (selectedCraftingTier == null) return -1;
		List<BloodStructureRecipe> recipes = craftingByTier.getOrDefault(selectedCraftingTier, List.of());
		if (recipes.isEmpty()) return -1;

		int sx = guiLeft + 4;
		int sy = guiTop + 24 + 14 + 4;
		int sw = TIER_SIDEBAR_W - 8;
		int rowH = 22;

		int clipTop = sy;
		int clipBottom = guiTop + guiHeight - 4;
		if (my < clipTop || my > clipBottom) return -1;

		// Apply scroll offset
		sy -= craftingSidebarScroll;

		for (int i = 0; i < CRAFTING_TIER_NAMES.length; i++) {
			String tierName = CRAFTING_TIER_NAMES[i];
			boolean selected = tierName.equals(selectedCraftingTier);
			List<BloodStructureRecipe> tierRecipes = craftingByTier.getOrDefault(tierName, List.of());

			sy += rowH + 2; // skip tier row

			if (selected) {
				for (int j = 0; j < tierRecipes.size(); j++) {
					if (mx >= sx + 4 && mx <= sx + sw - 4
							&& my >= sy && my <= sy + 16
							&& my >= clipTop && my <= clipBottom) {
						return j;
					}
					sy += 18;
				}
				return -1;
			}
			// If not the selected tier, just advance past it
		}
		return -1;
	}

	/**
	 * Renders the blood structure multiblock as an isometric 3D model preview.
	 * Supports layer-by-layer viewing via craftingVisibleLayer.
	 */
	private void drawCraftingModel(GuiGraphics gfx, BloodStructureRecipe recipe,
								   int areaX, int areaY, int areaW, int areaH) {
		if (recipe.getPattern() == null) return;

		List<BlockPosBlockPair> blockPairs = recipe.getPattern().getBlockPosBlockList();
		if (blockPairs.isEmpty()) return;

		// Determine bounding box of the structure
		int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
		int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
		for (BlockPosBlockPair pair : blockPairs) {
			BlockPos pos = pair.getPos();
			Block block = pair.getBlock();
			if (block == null || block == Blocks.AIR) continue;
			minX = Math.min(minX, pos.getX()); maxX = Math.max(maxX, pos.getX());
			minY = Math.min(minY, pos.getY()); maxY = Math.max(maxY, pos.getY());
			minZ = Math.min(minZ, pos.getZ()); maxZ = Math.max(maxZ, pos.getZ());
		}
		if (minX > maxX) return; // all air

		// Track max layer for the layer buttons
		craftingMaxLayer = maxY - minY;

		float sizeX = maxX - minX + 1;
		float sizeY = maxY - minY + 1;
		float sizeZ = maxZ - minZ + 1;
		float maxDim = Math.max(sizeX, Math.max(sizeY, sizeZ));

		float scale = Math.min(areaW, areaH) / (maxDim * 1.8f);
		int centerX = areaX + areaW / 2;
		int centerY = areaY + areaH / 2;

		PoseStack pose = gfx.pose();
		pose.pushPose();

		pose.translate(centerX, centerY, 300);
		pose.scale(scale, -scale, scale);

		// Isometric tilt + user rotation
		pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(30));
		pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(craftingRotationAngle));

		// Center the structure at origin
		float offX = -(minX + sizeX / 2f);
		float offY = -(minY + sizeY / 2f);
		float offZ = -(minZ + sizeZ / 2f);

		MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

		for (BlockPosBlockPair pair : blockPairs) {
			Block block = pair.getBlock();
			if (block == null || block == Blocks.AIR) continue;
			BlockPos pos = pair.getPos();

			int relativeY = pos.getY() - minY;

			// If a specific layer is selected, skip blocks above it
			if (craftingVisibleLayer >= 0 && relativeY > craftingVisibleLayer) continue;

			pose.pushPose();
			pose.translate(pos.getX() + offX, pos.getY() + offY, pos.getZ() + offZ);

			// Dim blocks below the selected layer to highlight the current one
			boolean dimmed = craftingVisibleLayer >= 0 && relativeY < craftingVisibleLayer;

			try {
				if (dimmed) {
					// Render with reduced brightness
					RenderSystem.enableBlend();
					RenderSystem.defaultBlendFunc();
				}
				Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
						block.defaultBlockState(), pose, bufferSource,
						dimmed ? 0x60006 : LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
			} catch (Exception e) {
				// Silently skip blocks that can't be rendered
			}

			pose.popPose();
		}

		bufferSource.endBatch();
		pose.popPose();
	}

	/**
	 * Draws the information panel for the selected blood crafting recipe.
	 */
	private void drawCraftingInfoPanel(GuiGraphics gfx, BloodStructureRecipe recipe,
									   int panelX, int panelY, int panelW) {
		int clipTop = panelY;
		int clipBottom = guiTop + guiHeight - 8;
		int visibleH = clipBottom - clipTop;

		// Clamp scroll
		int totalH = measureCraftingInfoPanelHeight(recipe, panelW);
		int maxScroll = Math.max(0, totalH - visibleH);
		if (craftingInfoScroll > maxScroll) craftingInfoScroll = maxScroll;

		gfx.enableScissor(panelX - 2, clipTop, panelX + panelW + 2, clipBottom);

		int y = panelY - craftingInfoScroll;
		int lineH = 12;

		// ── Recipe name (derived from ID) — word-wrapped ──
		String namePath = recipe.getId().getPath();
		if (namePath.contains("/")) namePath = namePath.substring(namePath.lastIndexOf('/') + 1);
		String name = HLTextUtils.toProperCase(namePath.replace("_", " "));
		for (String titleLine : ScreenDrawUtils.wrapText(font,name, panelW)) {
			gfx.drawString(font, Component.literal(titleLine)
					.withStyle(s -> s.withColor(0xCC3333).withBold(true)), panelX, y, 0);
			y += lineH;
		}
		y += 4;

		// ── Separator line ──
		gfx.fill(panelX, y, panelX + panelW, y + 1, 0xFF442222);
		y += 6;

		// ── Blood cost ──
		gfx.drawString(font, Component.literal("Blood Cost: ").withStyle(s -> s.withColor(0x888888))
				.append(Component.literal((int) recipe.getBloodCost() + " mL").withStyle(s -> s.withColor(0xAA4444))), panelX, y, 0);
		y += lineH + 4;

		// ── Held item ──
		ItemStack heldItem = recipe.getHeldItem();
		if (heldItem != null && !heldItem.isEmpty()) {
			gfx.drawString(font, Component.literal("Held Item:").withStyle(s -> s.withColor(0x888888)), panelX, y, 0);
			y += lineH;

			gfx.renderItem(heldItem, panelX, y);
			gfx.renderItemDecorations(font, heldItem, panelX, y);
			List<String> heldLines = ScreenDrawUtils.wrapText(font, heldItem.getHoverName().getString(), panelW - 20);
			for (int li = 0; li < heldLines.size(); li++) {
				int ix = li == 0 ? panelX + 20 : panelX + 4;
				gfx.drawString(font, Component.literal(heldLines.get(li))
						.withStyle(s -> s.withColor(0xDDDDDD)), ix, y + 4 + li * lineH, 0);
			}
			y += Math.max(20, heldLines.size() * lineH + 4);
		}

		// ── Hit block ──
		Block hitBlock = recipe.getHitBlock();
		if (hitBlock != null && hitBlock != Blocks.AIR) {
			gfx.drawString(font, Component.literal("Activate on:").withStyle(s -> s.withColor(0x888888)), panelX, y, 0);
			y += lineH;

			ItemStack hitStack = new ItemStack(hitBlock);
			if (!hitStack.isEmpty()) {
				gfx.renderItem(hitStack, panelX, y);
				List<String> hitLines =ScreenDrawUtils.wrapText(font, hitStack.getHoverName().getString(), panelW - 20);
				for (int li = 0; li < hitLines.size(); li++) {
					int ix = li == 0 ? panelX + 20 : panelX + 4;
					gfx.drawString(font, Component.literal(hitLines.get(li))
							.withStyle(s -> s.withColor(0xDDDDDD)), ix, y + 4 + li * lineH, 0);
				}
				y += Math.max(20, hitLines.size() * lineH + 4);
			}
		}

		y += 4;

		// ── Result item ──
		ItemStack result = recipe.getResult();
		if (result != null && !result.isEmpty()) {
			gfx.drawString(font, Component.literal("Result:").withStyle(s -> s.withColor(0x888888)), panelX, y, 0);
			y += lineH;

			gfx.renderItem(result, panelX, y);
			gfx.renderItemDecorations(font, result, panelX, y);
			List<String> resultLines = ScreenDrawUtils.wrapText(font, result.getHoverName().getString(), panelW - 20);
			for (int li = 0; li < resultLines.size(); li++) {
				int ix = li == 0 ? panelX + 20 : panelX + 4;
				gfx.drawString(font, Component.literal(resultLines.get(li))
						.withStyle(s -> s.withColor(0xDDDDDD)), ix, y + 4 + li * lineH, 0);
			}
			y += Math.max(20, resultLines.size() * lineH + 4);
		}

		y += 6;

		// ── Block materials list ──
		if (recipe.getPattern() != null) {
			Map<Block, Integer> blockCounts = recipe.getPattern().getBlockCount(false);
			if (!blockCounts.isEmpty()) {
				gfx.drawString(font, Component.literal("Materials:").withStyle(s -> s.withColor(0x888888)), panelX, y, 0);
				y += lineH;

				for (Map.Entry<Block, Integer> entry : blockCounts.entrySet()) {
					Block block = entry.getKey();
					if (block == null || block == Blocks.AIR) continue;
					int count = entry.getValue();

					ItemStack blockStack = new ItemStack(block);
					if (!blockStack.isEmpty()) {
						gfx.renderItem(blockStack, panelX + 2, y);
						String countPrefix = " x" + count + "  ";
						List<String> matLines =ScreenDrawUtils.wrapText(font, countPrefix + blockStack.getHoverName().getString(), panelW - 20);
						for (int li = 0; li < matLines.size(); li++) {
							gfx.drawString(font, Component.literal(matLines.get(li))
									.withStyle(s -> s.withColor(0xAAAAAA)), panelX + 20, y + 4 + li * lineH, 0);
						}
						y += Math.max(18, matLines.size() * lineH + 4);
					}
				}
			}
		}

		gfx.disableScissor();

		// Draw scroll indicators if content overflows
		if (totalH > visibleH) {
			if (craftingInfoScroll > 0) {
				gfx.drawCenteredString(font, "\u25B2", panelX + panelW / 2, clipTop, 0xAAFFFFFF);
			}
			if (craftingInfoScroll < maxScroll) {
				gfx.drawCenteredString(font, "\u25BC", panelX + panelW / 2, clipBottom - 10, 0xAAFFFFFF);
			}
		}
	}

	/** Measures the total content height of the crafting info panel (without clipping). */
	private int measureCraftingInfoPanelHeight(BloodStructureRecipe recipe, int panelW) {
		int y = 0;
		int lineH = 12;

		// Name
		String namePath = recipe.getId().getPath();
		if (namePath.contains("/")) namePath = namePath.substring(namePath.lastIndexOf('/') + 1);
		String name = HLTextUtils.toProperCase(namePath.replace("_", " "));
		y += ScreenDrawUtils.wrapText(font, name, panelW).size() * lineH;
		y += 4 + 1 + 6; // gap + separator + gap

		// Blood cost
		y += lineH + 4;

		// Held item
		ItemStack heldItem = recipe.getHeldItem();
		if (heldItem != null && !heldItem.isEmpty()) {
			y += lineH; // label
			List<String> heldLines = ScreenDrawUtils.wrapText(font, heldItem.getHoverName().getString(), panelW - 20);
			y += Math.max(20, heldLines.size() * lineH + 4);
		}

		// Hit block
		Block hitBlock = recipe.getHitBlock();
		if (hitBlock != null && hitBlock != Blocks.AIR) {
			y += lineH; // label
			ItemStack hitStack = new ItemStack(hitBlock);
			if (!hitStack.isEmpty()) {
				List<String> hitLines = ScreenDrawUtils.wrapText(font, hitStack.getHoverName().getString(), panelW - 20);
				y += Math.max(20, hitLines.size() * lineH + 4);
			}
		}

		y += 4; // gap

		// Result
		ItemStack result = recipe.getResult();
		if (result != null && !result.isEmpty()) {
			y += lineH; // label
			List<String> resultLines = ScreenDrawUtils.wrapText(font, result.getHoverName().getString(), panelW - 20);
			y += Math.max(20, resultLines.size() * lineH + 4);
		}

		y += 6; // gap

		// Materials
		if (recipe.getPattern() != null) {
			Map<Block, Integer> blockCounts = recipe.getPattern().getBlockCount(false);
			if (!blockCounts.isEmpty()) {
				y += lineH; // "Materials:" label
				for (Map.Entry<Block, Integer> entry : blockCounts.entrySet()) {
					Block block = entry.getKey();
					if (block == null || block == Blocks.AIR) continue;
					ItemStack blockStack = new ItemStack(block);
					if (!blockStack.isEmpty()) {
						String countPrefix = " x" + entry.getValue() + "  ";
						List<String> matLines = ScreenDrawUtils.wrapText(font, countPrefix + blockStack.getHoverName().getString(), panelW - 20);
						y += Math.max(18, matLines.size() * lineH + 4);
					}
				}
			}
		}

		return y;
	}

	// (Old nav buttons removed — now using tier sidebar)

	// ────────────────────────────────────────────────────────────
	//  Runes tab — chisel recipe display
	// ────────────────────────────────────────────────────────────

	private void drawRunesContent(GuiGraphics gfx, int mouseX, int mouseY, float partial) {
		if (chiselRecipes.isEmpty()) {
			gfx.drawCenteredString(font, "No Rune recipes found",
					guiLeft + guiWidth / 2, guiTop + guiHeight / 2, 0xFF666666);
			return;
		}

		// ── Recipe content (right of sidebar) ──
		int contentX = guiLeft + TIER_SIDEBAR_W + 6;
		int contentW = guiWidth - TIER_SIDEBAR_W - 10;

		// Push z so all overlays are above background
		gfx.pose().pushPose();
		gfx.pose().translate(0, 0, 400);

		// ── Tier sidebar (left) ──
		drawRunesTierSidebar(gfx, mouseX, mouseY);

		if (selectedRuneTier == null) {
			gfx.drawCenteredString(font, "Select a tier",
					contentX + contentW / 2, guiTop + guiHeight / 2, 0xFF555555);
			gfx.pose().popPose();
			return;
		}

		List<com.vincenthuto.hemomancy.common.recipe.ChiselRecipe> tierRecipes =
				chiselByTier.getOrDefault(selectedRuneTier, List.of());
		if (tierRecipes.isEmpty()) {
			gfx.drawCenteredString(font, "No recipes in this tier",
					contentX + contentW / 2, guiTop + guiHeight / 2, 0xFF555555);
			gfx.pose().popPose();
			return;
		}
		if (selectedRuneIndexInTier >= tierRecipes.size()) selectedRuneIndexInTier = 0;
		com.vincenthuto.hemomancy.common.recipe.ChiselRecipe recipe = tierRecipes.get(selectedRuneIndexInTier);

		// Layout: left half = 8×8 pattern grid, right half = info panel
		int patternAreaW = contentW / 2;
		int patternX = contentX;
		int infoX = contentX + patternAreaW + 10;
		int infoW = contentW - patternAreaW - 20;

		// ── 8×8 Rune pattern grid ──
		drawRunePatternGrid(gfx, recipe, patternX + 10, guiTop + 30,
				patternAreaW - 20, guiHeight - 60);

		// ── Info panel ──
		drawRuneInfoPanel(gfx, recipe, infoX, guiTop + 30, infoW);

		gfx.pose().popPose();
	}

	/**
	 * Draws the 8×8 chisel pattern grid for a rune recipe.
	 * Filled cells (value == 1) are drawn as solid colored squares;
	 * empty cells are drawn as faint outlines.
	 */
	private void drawRunePatternGrid(GuiGraphics gfx, com.vincenthuto.hemomancy.common.recipe.ChiselRecipe recipe,
									 int areaX, int areaY, int areaW, int areaH) {
		byte[][] pattern = recipe.getPattern();
		if (pattern == null) return;

		// Compute cell size to fit the grid in the available area
		int gridDim = Math.min(areaW, areaH);
		int cellSize = gridDim / 8;
		int gridW = cellSize * 8;
		int gridH = cellSize * 8;

		// Centre the grid in the area
		int gridX = areaX + (areaW - gridW) / 2;
		int gridY = areaY + (areaH - gridH) / 2;

		// Grid background
		gfx.fill(gridX - 2, gridY - 2, gridX + gridW + 2, gridY + gridH + 2, 0xFF0A0404);
		gfx.fill(gridX - 1, gridY - 1, gridX + gridW + 1, gridY + gridH + 1, Tab.RUNES.color & 0x33FFFFFF);

		// Draw cells
		int filledColor = Tab.RUNES.color;
		int emptyBg = 0xFF120808;
		int emptyBorder = 0xFF221111;

		for (int row = 0; row < 8 && row < pattern.length; row++) {
			for (int col = 0; col < 8 && col < pattern[row].length; col++) {
				int cx = gridX + col * cellSize;
				int cy = gridY + row * cellSize;

				if (pattern[row][col] != 0) {
					// Filled cell — solid color with pulse
					float time = System.nanoTime() / 1_000_000_000f;
					float pulse = 0.7f + 0.3f * (float) Math.sin(time * 2.0 + row * 0.5 + col * 0.3);
					int r = (int) (((filledColor >> 16) & 0xFF) * pulse);
					int g = (int) (((filledColor >> 8) & 0xFF) * pulse);
					int b = (int) ((filledColor & 0xFF) * pulse);
					int cellCol = 0xFF000000 | (r << 16) | (g << 8) | b;

					gfx.fill(cx + 1, cy + 1, cx + cellSize - 1, cy + cellSize - 1, cellCol);
					// Bright border
					gfx.fill(cx, cy, cx + cellSize, cy + 1, filledColor);
					gfx.fill(cx, cy + cellSize - 1, cx + cellSize, cy + cellSize, filledColor);
					gfx.fill(cx, cy, cx + 1, cy + cellSize, filledColor);
					gfx.fill(cx + cellSize - 1, cy, cx + cellSize, cy + cellSize, filledColor);
				} else {
					// Empty cell — faint outline
					gfx.fill(cx + 1, cy + 1, cx + cellSize - 1, cy + cellSize - 1, emptyBg);
					gfx.fill(cx, cy, cx + cellSize, cy + 1, emptyBorder);
					gfx.fill(cx, cy + cellSize - 1, cx + cellSize, cy + cellSize, emptyBorder);
					gfx.fill(cx, cy, cx + 1, cy + cellSize, emptyBorder);
					gfx.fill(cx + cellSize - 1, cy, cx + cellSize, cy + cellSize, emptyBorder);
				}
			}
		}

		// Label below the grid
		gfx.drawCenteredString(font, "Rune Pattern (8×8)", gridX + gridW / 2, gridY + gridH + 6, 0xFF888888);
	}

	/**
	 * Draws the information panel for the selected chisel recipe.
	 */
	private void drawRuneInfoPanel(GuiGraphics gfx, com.vincenthuto.hemomancy.common.recipe.ChiselRecipe recipe,
								   int panelX, int panelY, int panelW) {
		int y = panelY;
		int lineH = 12;

		// ── Recipe name (derived from ID) — word-wrapped ──
		String namePath = recipe.getId().getPath();
		if (namePath.contains("/")) namePath = namePath.substring(namePath.lastIndexOf('/') + 1);
		String name = HLTextUtils.toProperCase(namePath.replace("_", " "));
		for (String titleLine : ScreenDrawUtils.wrapText(font, name, panelW)) {
			gfx.drawString(font, Component.literal(titleLine)
					.withStyle(s -> s.withColor(Tab.RUNES.color).withBold(true)), panelX, y, 0);
			y += lineH;
		}
		y += 4;

		// ── Separator line ──
		gfx.fill(panelX, y, panelX + panelW, y + 1, 0xFF224444);
		y += 6;

		// ── Tier ──
		gfx.drawString(font, Component.literal("Tier: ").withStyle(s -> s.withColor(0x888888))
				.append(Component.literal(String.valueOf(recipe.getTier())).withStyle(s -> s.withColor(Tab.RUNES.color))), panelX, y, 0);
		y += lineH + 4;

		// ── Rune Type ──
		gfx.drawString(font, Component.literal("Type: ").withStyle(s -> s.withColor(0x888888))
				.append(Component.literal(recipe.getRuneType().name()).withStyle(s -> s.withColor(0xDDDDDD))), panelX, y, 0);
		y += lineH + 4;

		// ── Ingredients ──
		gfx.drawString(font, Component.literal("Ingredients:").withStyle(s -> s.withColor(0x888888)), panelX, y, 0);
		y += lineH;

		// Ingredient 1
		net.minecraft.world.item.crafting.Ingredient ing1 = recipe.getIngredient1();
		if (ing1 != null && !ing1.isEmpty()) {
			ItemStack[] items1 = ing1.getItems();
			if (items1.length > 0) {
				ItemStack display1 = items1[0];
				gfx.renderItem(display1, panelX + 2, y);
				gfx.renderItemDecorations(font, display1, panelX + 2, y);
				List<String> lines1 = ScreenDrawUtils.wrapText(font, display1.getHoverName().getString(), panelW - 24);
				for (int li = 0; li < lines1.size(); li++) {
					int ix = li == 0 ? panelX + 22 : panelX + 4;
					gfx.drawString(font, Component.literal(lines1.get(li))
							.withStyle(s -> s.withColor(0xDDDDDD)), ix, y + 4 + li * lineH, 0);
				}
				y += Math.max(20, lines1.size() * lineH + 4);
			}
		}

		// Ingredient 2
		net.minecraft.world.item.crafting.Ingredient ing2 = recipe.getIngredient2();
		if (ing2 != null && !ing2.isEmpty()) {
			ItemStack[] items2 = ing2.getItems();
			if (items2.length > 0) {
				ItemStack display2 = items2[0];
				gfx.renderItem(display2, panelX + 2, y);
				gfx.renderItemDecorations(font, display2, panelX + 2, y);
				List<String> lines2 = ScreenDrawUtils.wrapText(font, display2.getHoverName().getString(), panelW - 24);
				for (int li = 0; li < lines2.size(); li++) {
					int ix = li == 0 ? panelX + 22 : panelX + 4;
					gfx.drawString(font, Component.literal(lines2.get(li))
							.withStyle(s -> s.withColor(0xDDDDDD)), ix, y + 4 + li * lineH, 0);
				}
				y += Math.max(20, lines2.size() * lineH + 4);
			}
		}

		y += 6;

		// ── Result item ──
		ItemStack result = recipe.getResultItem();
		if (result != null && !result.isEmpty()) {
			gfx.drawString(font, Component.literal("Result:").withStyle(s -> s.withColor(0x888888)), panelX, y, 0);
			y += lineH;

			gfx.renderItem(result, panelX, y);
			gfx.renderItemDecorations(font, result, panelX, y);
			List<String> resultLines = ScreenDrawUtils.wrapText(font, result.getHoverName().getString(), panelW - 20);
			for (int li = 0; li < resultLines.size(); li++) {
				int ix = li == 0 ? panelX + 20 : panelX + 4;
				gfx.drawString(font, Component.literal(resultLines.get(li))
						.withStyle(s -> s.withColor(0xDDDDDD)), ix, y + 4 + li * lineH, 0);
			}
			y += Math.max(20, resultLines.size() * lineH + 4);
		}

		y += 8;

		// ── Per-rune lore ──
		gfx.fill(panelX, y, panelX + panelW, y + 1, 0xFF224444);
		y += 6;
		String recipeKey = recipe.getId().getPath();
		if (recipeKey.contains("/")) recipeKey = recipeKey.substring(recipeKey.lastIndexOf('/') + 1);
		String loreText = RuneLoreData.getLore(recipeKey);
		for (String loreLine : ScreenDrawUtils.wrapText(font, loreText, panelW)) {
			gfx.drawString(font, Component.literal(loreLine)
					.withStyle(s -> s.withColor(0xFF557788).withItalic(true)), panelX, y, 0);
			y += lineH;
		}
	}

	/**
	 * Draws the tier sidebar for Runes, showing all tiers as rows.
	 * Locked tiers are greyed/obfuscated. Recipes within selected tier are listed below.
	 */
	private void drawRunesTierSidebar(GuiGraphics gfx, int mouseX, int mouseY) {
		int sx = guiLeft + 4;
		int sy = guiTop + 24;
		int sw = TIER_SIDEBAR_W - 8;
		int rowH = 22;

		// Title (drawn above the scrollable area)
		gfx.drawString(font, Component.literal("Rune Tiers")
				.withStyle(s -> s.withColor(Tab.RUNES.color).withBold(true)), sx + 2, sy, 0);
		sy += 14;

		// Separator
		gfx.fill(sx, sy, sx + sw, sy + 1, 0xFF224444);
		sy += 4;

		// Scissor to clip scrollable content within the sidebar
		int clipTop = sy;
		int clipBottom = guiTop + guiHeight - 4;
		gfx.enableScissor(sx, clipTop, sx + sw, clipBottom);

		// Apply scroll offset
		sy -= runeSidebarScroll;

		// Tier rows
		for (int i = 0; i < RUNE_TIER_NAMES.length; i++) {
			String tierName = RUNE_TIER_NAMES[i];
			boolean locked = playerDegree < RUNE_TIER_DEGREE_REQ[i];
			boolean selected = tierName.equals(selectedRuneTier);
			List<com.vincenthuto.hemomancy.common.recipe.ChiselRecipe> recipes =
					chiselByTier.getOrDefault(tierName, List.of());

			boolean hovered = mouseX >= sx && mouseX <= sx + sw
					&& mouseY >= sy && mouseY <= sy + rowH
					&& mouseY >= clipTop && mouseY <= clipBottom;

			// Background
			int bg = selected ? 0xDD0A1818 : (hovered && !locked ? 0xBB081414 : 0x99061010);
			gfx.fill(sx, sy, sx + sw, sy + rowH, bg);

			// Border
			int bc = locked ? 0xFF333333 : (selected ? Tab.RUNES.color : 0xFF555555);
			gfx.fill(sx, sy, sx + sw, sy + 1, bc);
			gfx.fill(sx, sy + rowH - 1, sx + sw, sy + rowH, bc);
			gfx.fill(sx, sy, sx + 1, sy + rowH, bc);
			gfx.fill(sx + sw - 1, sy, sx + sw, sy + rowH, bc);

			if (locked) {
				// Dark overlay + lock indicator
				gfx.fill(sx + 1, sy + 1, sx + sw - 1, sy + rowH - 1, 0xBB000000);
				gfx.drawString(font, "[X] " + tierName + " (Locked)", sx + 4, sy + (rowH - 8) / 2, 0xFF444444, false);
			} else {
				String label = tierName + " (" + recipes.size() + ")";
				int textCol = selected ? 0xFFAADDEE : 0xFF999999;
				gfx.drawString(font, label, sx + 4, sy + (rowH - 8) / 2, textCol, false);
			}

			// If this tier is selected, draw recipe list below
			if (selected && !locked) {
				sy += rowH + 2;
				for (int j = 0; j < recipes.size(); j++) {
					com.vincenthuto.hemomancy.common.recipe.ChiselRecipe r = recipes.get(j);
					boolean recSel = (j == selectedRuneIndexInTier);
					boolean recHov = mouseX >= sx + 4 && mouseX <= sx + sw - 4
							&& mouseY >= sy && mouseY <= sy + 16
							&& mouseY >= clipTop && mouseY <= clipBottom;

					int recBg = recSel ? 0xCC102020 : (recHov ? 0xAA0A1818 : 0x00000000);
					gfx.fill(sx + 2, sy, sx + sw - 2, sy + 16, recBg);

					if (recSel) {
						gfx.fill(sx + 2, sy, sx + 3, sy + 16, Tab.RUNES.color);
					}

					String recPath = r.getId().getPath();
					if (recPath.contains("/")) recPath = recPath.substring(recPath.lastIndexOf('/') + 1);
					String recName = HLTextUtils.toProperCase(recPath.replace("_", " "));
					int recCol = recSel ? 0xFFAADDEE : 0xFF888888;
					gfx.drawString(font, recName, sx + 8, sy + 4, recCol, false);
					sy += 18;
				}
			}
			sy += rowH + 2;
		}

		gfx.disableScissor();

		// Draw scroll indicators if content overflows
		int contentH = runeSidebarContentH();
		int visibleH = tierSidebarVisibleH();
		if (contentH > visibleH) {
			if (runeSidebarScroll > 0) {
				gfx.drawCenteredString(font, "\u25B2", sx + sw / 2, clipTop, 0xAAFFFFFF);
			}
			if (runeSidebarScroll < contentH - visibleH) {
				gfx.drawCenteredString(font, "\u25BC", sx + sw / 2, clipBottom - 10, 0xAAFFFFFF);
			}
		}
	}

	/** Returns the tier name clicked in the runes sidebar, or null. */
	private String runeTierUnder(double mx, double my) {
		int sx = guiLeft + 4;
		int sy = guiTop + 24 + 14 + 4;
		int sw = TIER_SIDEBAR_W - 8;
		int rowH = 22;

		int clipTop = sy;
		int clipBottom = guiTop + guiHeight - 4;
		if (my < clipTop || my > clipBottom) return null;

		// Apply scroll offset
		sy -= runeSidebarScroll;

		for (int i = 0; i < RUNE_TIER_NAMES.length; i++) {
			String tierName = RUNE_TIER_NAMES[i];
			boolean selected = tierName.equals(selectedRuneTier);
			List<com.vincenthuto.hemomancy.common.recipe.ChiselRecipe> recipes =
					chiselByTier.getOrDefault(tierName, List.of());

			if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + rowH
					&& my >= clipTop && my <= clipBottom) {
				return tierName;
			}

			if (selected) {
				sy += rowH + 2 + recipes.size() * 18;
			}
			sy += rowH + 2;
		}
		return null;
	}

	/** Returns the recipe index clicked within the selected rune tier, or -1. */
	private int runeRecipeUnder(double mx, double my) {
		if (selectedRuneTier == null) return -1;
		List<com.vincenthuto.hemomancy.common.recipe.ChiselRecipe> recipes =
				chiselByTier.getOrDefault(selectedRuneTier, List.of());
		if (recipes.isEmpty()) return -1;

		int sx = guiLeft + 4;
		int sy = guiTop + 24 + 14 + 4;
		int sw = TIER_SIDEBAR_W - 8;
		int rowH = 22;

		int clipTop = sy;
		int clipBottom = guiTop + guiHeight - 4;
		if (my < clipTop || my > clipBottom) return -1;

		// Apply scroll offset
		sy -= runeSidebarScroll;

		for (int i = 0; i < RUNE_TIER_NAMES.length; i++) {
			String tierName = RUNE_TIER_NAMES[i];
			boolean selected = tierName.equals(selectedRuneTier);
			List<com.vincenthuto.hemomancy.common.recipe.ChiselRecipe> tierRecipes =
					chiselByTier.getOrDefault(tierName, List.of());

			sy += rowH + 2; // skip tier row

			if (selected) {
				for (int j = 0; j < tierRecipes.size(); j++) {
					if (mx >= sx + 4 && mx <= sx + sw - 4
							&& my >= sy && my <= sy + 16
							&& my >= clipTop && my <= clipBottom) {
						return j;
					}
					sy += 18;
				}
				return -1;
			}
			// If not the selected tier, just advance past it
		}
		return -1;
	}

	// ────────────────────────────────────────────────────────────
	//  Cardinal Rites tab — tier-based layout
	// ────────────────────────────────────────────────────────────

	private void drawRiteContent(GuiGraphics gfx, int mouseX, int mouseY, float partial) {
		if (riteRecipes.isEmpty()) {
			gfx.drawCenteredString(font, "No Cardinal Rites found",
					guiLeft + guiWidth / 2, guiTop + guiHeight / 2, 0xFF666666);
			return;
		}

		// ── Recipe content (right of sidebar) ──
		int contentX = guiLeft + TIER_SIDEBAR_W + 6;
		int contentW = guiWidth - TIER_SIDEBAR_W - 10;

		if (selectedRiteTier == null) {
			gfx.pose().pushPose();
			gfx.pose().translate(0, 0, 400);
			drawRiteTierSidebar(gfx, mouseX, mouseY);
			gfx.drawCenteredString(font, "Select a tier",
					contentX + contentW / 2, guiTop + guiHeight / 2, 0xFF555555);
			gfx.pose().popPose();
			return;
		}

		List<CardinalRiteRecipe> tierRites = ritesByTier.getOrDefault(selectedRiteTier, List.of());
		if (tierRites.isEmpty()) {
			gfx.pose().pushPose();
			gfx.pose().translate(0, 0, 400);
			drawRiteTierSidebar(gfx, mouseX, mouseY);
			gfx.drawCenteredString(font, "No rites in this tier",
					contentX + contentW / 2, guiTop + guiHeight / 2, 0xFF555555);
			gfx.pose().popPose();
			return;
		}
		if (selectedRiteIndexInTier >= tierRites.size()) selectedRiteIndexInTier = 0;
		CardinalRiteRecipe rite = tierRites.get(selectedRiteIndexInTier);

		// Layout: left half of content = 3D model, right half = info panel
		int modelAreaW = contentW / 2;
		int modelX = contentX;
		int infoX = contentX + modelAreaW + 10;
		int infoW = contentW - modelAreaW - 20;

		// ── 3D multiblock preview (rendered first, at z=300) ──
		drawRiteModel(gfx, rite, modelX + 10, guiTop + 30,
				modelAreaW - 20, guiHeight - 60, partial);

		// Push z above the 3D model (z=300) so ALL 2D overlays always draw on top of the blocks.
		gfx.pose().pushPose();
		gfx.pose().translate(0, 0, 400);

		// ── Tier sidebar (left) ──
		drawRiteTierSidebar(gfx, mouseX, mouseY);

		// ── Layer buttons ──
		drawLayerButtons(gfx, mouseX, mouseY, Tab.RITES, riteVisibleLayer, riteMaxLayer);

		// ── Info panel ──
		drawRiteInfoPanel(gfx, rite, infoX, guiTop + 30, infoW, mouseX, mouseY);

		// ── Drag hint ──
		gfx.drawCenteredString(font, "Drag to rotate",
				modelX + modelAreaW / 2, guiTop + guiHeight - 18, 0x44888888);

		gfx.pose().popPose();
	}

	/**
	 * Draws the tier sidebar for Cardinal Rites, grouped by rite type.
	 * Locked tiers are greyed/obfuscated based on player degree.
	 */
	private void drawRiteTierSidebar(GuiGraphics gfx, int mouseX, int mouseY) {
		int sx = guiLeft + 4;
		int sy = guiTop + 24;
		int sw = TIER_SIDEBAR_W - 8;
		int rowH = 22;

		// Title (drawn above the scrollable area)
		gfx.drawString(font, Component.literal("Rite Tiers")
				.withStyle(s -> s.withColor(Tab.RITES.color).withBold(true)), sx + 2, sy, 0);
		sy += 14;

		// Separator
		gfx.fill(sx, sy, sx + sw, sy + 1, 0xFF332244);
		sy += 4;

		// Scissor to clip scrollable content within the sidebar
		int clipTop = sy;
		int clipBottom = guiTop + guiHeight - 4;
		gfx.enableScissor(sx, clipTop, sx + sw, clipBottom);

		// Apply scroll offset
		sy -= riteSidebarScroll;

		for (CardinalRiteType type : CardinalRiteType.values()) {
			boolean locked = playerDegree < riteMinDegree(type);
			boolean selected = (type == selectedRiteTier);
			List<CardinalRiteRecipe> recipes = ritesByTier.getOrDefault(type, List.of());

			boolean hovered = mouseX >= sx && mouseX <= sx + sw
					&& mouseY >= sy && mouseY <= sy + rowH
					&& mouseY >= clipTop && mouseY <= clipBottom;

			// Background
			int bg = selected ? 0xDD120818 : (hovered && !locked ? 0xBB100616 : 0x990C0410);
			gfx.fill(sx, sy, sx + sw, sy + rowH, bg);

			// Border
			int bc = locked ? 0xFF333333 : (selected ? Tab.RITES.color : 0xFF555555);
			gfx.fill(sx, sy, sx + sw, sy + 1, bc);
			gfx.fill(sx, sy + rowH - 1, sx + sw, sy + rowH, bc);
			gfx.fill(sx, sy, sx + 1, sy + rowH, bc);
			gfx.fill(sx + sw - 1, sy, sx + sw, sy + rowH, bc);

			// Tier size indicator
			String sizeLabel = type.getSize() + "x" + type.getSize();
			String tierLabel = HLTextUtils.toProperCase(type.getSerializedName());

			if (locked) {
				gfx.fill(sx + 1, sy + 1, sx + sw - 1, sy + rowH - 1, 0xBB000000);
				gfx.drawString(font, "[X] " + tierLabel + " (Locked)", sx + 4, sy + (rowH - 8) / 2, 0xFF444444, false);
			} else {
				int textCol = selected ? 0xFFDDBBEE : 0xFF999999;
				gfx.drawString(font, tierLabel + " " + sizeLabel + " (" + recipes.size() + ")", sx + 4, sy + (rowH - 8) / 2, textCol, false);
			}

			// If this tier is selected, draw recipe list below
			if (selected && !locked) {
				sy += rowH + 2;
				for (int j = 0; j < recipes.size(); j++) {
					CardinalRiteRecipe r = recipes.get(j);
					boolean recSel = (j == selectedRiteIndexInTier);
					boolean recHov = mouseX >= sx + 4 && mouseX <= sx + sw - 4
							&& mouseY >= sy && mouseY <= sy + 16
							&& mouseY >= clipTop && mouseY <= clipBottom;

					int recBg = recSel ? 0xCC180818 : (recHov ? 0xAA140614 : 0x00000000);
					gfx.fill(sx + 2, sy, sx + sw - 2, sy + 16, recBg);

					if (recSel) {
						gfx.fill(sx + 2, sy, sx + 3, sy + 16, Tab.RITES.color);
					}

					String recName = r.getRiteName();
					if (recName == null || recName.isEmpty()) {
						String ritePath = r.getId().getPath();
						if (ritePath.contains("/")) ritePath = ritePath.substring(ritePath.lastIndexOf('/') + 1);
						recName = HLTextUtils.toProperCase(ritePath.replace("_", " "));
					}
					// Truncate long names
					recName = truncateText(recName, sw - 16);
					int recCol = recSel ? 0xFFDDBBEE : 0xFF888888;
					gfx.drawString(font, recName, sx + 8, sy + 4, recCol, false);
					sy += 18;
				}
			}
			sy += rowH + 2;
		}

		gfx.disableScissor();

		// Draw scroll indicators if content overflows
		int contentH = riteSidebarContentH();
		int visibleH = tierSidebarVisibleH();
		if (contentH > visibleH) {
			if (riteSidebarScroll > 0) {
				// Up arrow indicator
				gfx.drawCenteredString(font, "\u25B2", sx + sw / 2, clipTop, 0xAAFFFFFF);
			}
			if (riteSidebarScroll < contentH - visibleH) {
				// Down arrow indicator
				gfx.drawCenteredString(font, "\u25BC", sx + sw / 2, clipBottom - 10, 0xAAFFFFFF);
			}
		}
	}

	/** Returns the rite tier clicked in the sidebar, or null. */
	private CardinalRiteType riteTierUnder(double mx, double my) {
		int sx = guiLeft + 4;
		int sy = guiTop + 24 + 14 + 4;
		int sw = TIER_SIDEBAR_W - 8;
		int rowH = 22;

		int clipTop = sy;
		int clipBottom = guiTop + guiHeight - 4;
		if (my < clipTop || my > clipBottom) return null;

		// Apply scroll offset
		sy -= riteSidebarScroll;

		for (CardinalRiteType type : CardinalRiteType.values()) {
			boolean selected = (type == selectedRiteTier);
			List<CardinalRiteRecipe> recipes = ritesByTier.getOrDefault(type, List.of());

			if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + rowH
					&& my >= clipTop && my <= clipBottom) {
				return type;
			}

			if (selected) {
				sy += rowH + 2 + recipes.size() * 18;
			}
			sy += rowH + 2;
		}
		return null;
	}

	/** Returns the recipe index clicked within the selected rite tier, or -1. */
	private int riteRecipeUnder(double mx, double my) {
		if (selectedRiteTier == null) return -1;
		List<CardinalRiteRecipe> recipes = ritesByTier.getOrDefault(selectedRiteTier, List.of());
		if (recipes.isEmpty()) return -1;

		int sx = guiLeft + 4;
		int sy = guiTop + 24 + 14 + 4;
		int sw = TIER_SIDEBAR_W - 8;
		int rowH = 22;

		int clipTop = sy;
		int clipBottom = guiTop + guiHeight - 4;
		if (my < clipTop || my > clipBottom) return -1;

		// Apply scroll offset
		sy -= riteSidebarScroll;

		for (CardinalRiteType type : CardinalRiteType.values()) {
			boolean selected = (type == selectedRiteTier);
			List<CardinalRiteRecipe> tierRecipes = ritesByTier.getOrDefault(type, List.of());

			sy += rowH + 2; // skip tier row

			if (selected) {
				for (int j = 0; j < tierRecipes.size(); j++) {
					if (mx >= sx + 4 && mx <= sx + sw - 4
							&& my >= sy && my <= sy + 16
							&& my >= clipTop && my <= clipBottom) {
						return j;
					}
					sy += 18;
				}
				return -1;
			}
		}
		return -1;
	}

	/**
	 * Renders the multiblock pattern as an isometric 3D model preview.
	 * Uses Minecraft's block renderer to draw actual block models.
	 * Supports layer-by-layer viewing via riteVisibleLayer.
	 */
	private void drawRiteModel(GuiGraphics gfx, CardinalRiteRecipe rite,
							   int areaX, int areaY, int areaW, int areaH, float partial) {
		if (rite.getPattern() == null) return;

		List<BlockPosBlockPair> blockPairs = rite.getPattern().getBlockPosBlockList();
		if (blockPairs.isEmpty()) return;

		// Determine bounding box of the structure
		int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
		int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
		for (BlockPosBlockPair pair : blockPairs) {
			BlockPos pos = pair.getPos();
			Block block = pair.getBlock();
			if (block == null || block == Blocks.AIR) continue;
			minX = Math.min(minX, pos.getX()); maxX = Math.max(maxX, pos.getX());
			minY = Math.min(minY, pos.getY()); maxY = Math.max(maxY, pos.getY());
			minZ = Math.min(minZ, pos.getZ()); maxZ = Math.max(maxZ, pos.getZ());
		}
		if (minX > maxX) return; // all air

		// Track max layer for the layer buttons
		riteMaxLayer = maxY - minY;

		float sizeX = maxX - minX + 1;
		float sizeY = maxY - minY + 1;
		float sizeZ = maxZ - minZ + 1;
		float maxDim = Math.max(sizeX, Math.max(sizeY, sizeZ));

		// Scale so the structure fits within the area
		float scale = Math.min(areaW, areaH) / (maxDim * 1.8f);
		// Center of the rendering area
		int centerX = areaX + areaW / 2;
		int centerY = areaY + areaH / 2;

		PoseStack pose = gfx.pose();
		pose.pushPose();

		// Move to the center of the rendering area
		pose.translate(centerX, centerY, 300);

		// Apply scale
		pose.scale(scale, -scale, scale);

		// Apply isometric tilt
		pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(30));
		pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(riteRotationAngle));

		// Center the structure at origin
		float offX = -(minX + sizeX / 2f);
		float offY = -(minY + sizeY / 2f);
		float offZ = -(minZ + sizeZ / 2f);

		MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

		for (BlockPosBlockPair pair : blockPairs) {
			Block block = pair.getBlock();
			if (block == null || block == Blocks.AIR) continue;
			BlockPos pos = pair.getPos();

			int relativeY = pos.getY() - minY;

			// If a specific layer is selected, skip blocks above it
			if (riteVisibleLayer >= 0 && relativeY > riteVisibleLayer) continue;

			pose.pushPose();
			pose.translate(pos.getX() + offX, pos.getY() + offY, pos.getZ() + offZ);

			// Dim blocks below the selected layer to highlight the current one
			boolean dimmed = riteVisibleLayer >= 0 && relativeY < riteVisibleLayer;

			try {
				Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
						block.defaultBlockState(), pose, bufferSource,
						dimmed ? 0x60006 : LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
			} catch (Exception e) {
				// Silently skip blocks that can't be rendered
			}

			pose.popPose();
		}

		bufferSource.endBatch();
		pose.popPose();
	}

	/**
	 * Draws the information panel for the selected cardinal rite.
	 */
	private void drawRiteInfoPanel(GuiGraphics gfx, CardinalRiteRecipe rite,
								   int panelX, int panelY, int panelW, int mouseX, int mouseY) {
		int clipTop = panelY;
		int clipBottom = guiTop + guiHeight - 8;
		int visibleH = clipBottom - clipTop;

		// Clamp scroll
		int totalH = measureRiteInfoPanelHeight(rite, panelW);
		int maxScroll = Math.max(0, totalH - visibleH);
		if (riteInfoScroll > maxScroll) riteInfoScroll = maxScroll;

		gfx.enableScissor(panelX - 2, clipTop, panelX + panelW + 2, clipBottom);

		int y = panelY - riteInfoScroll;
		int lineH = 12;

		// ── Rite name ──
		String name = rite.getRiteName();
		if (name == null || name.isEmpty()) {
			String ritePath = rite.getId().getPath();
			if (ritePath.contains("/")) ritePath = ritePath.substring(ritePath.lastIndexOf('/') + 1);
			name = HLTextUtils.toProperCase(ritePath.replace("_", " "));
		}
		// ── Rite name — word-wrapped ──
		for (String titleLine :ScreenDrawUtils.wrapText(font, name, panelW)) {
			gfx.drawString(font, Component.literal(titleLine)
					.withStyle(s -> s.withColor(0xCC66DD).withBold(true)), panelX, y, 0);
			y += lineH;
		}
		y += 4;

		// ── Separator line ──
		gfx.fill(panelX, y, panelX + panelW, y + 1, 0xFF442244);
		y += 6;

		// ── Description ──
		String desc = rite.getRiteDescription();
		if (desc != null && !desc.isEmpty()) {
			// Word-wrap the description
			List<String> lines = ScreenDrawUtils.wrapText(font, desc, panelW);
			for (String line : lines) {
				gfx.drawString(font, Component.literal(line)
						.withStyle(s -> s.withColor(0x999999).withItalic(true)), panelX, y, 0);
				y += lineH;
			}
			y += 4;
		}

		// ── Rite type ──
		CardinalRiteType type = rite.getRiteType();
		String typeStr = HLTextUtils.toProperCase(type.getSerializedName()) + " (" + type.getSize() + "x" + type.getSize() + ")";
		gfx.drawString(font, Component.literal("Type: ").withStyle(s -> s.withColor(0x888888))
				.append(Component.literal(typeStr).withStyle(s -> s.withColor(0xBB88CC))), panelX, y, 0);
		y += lineH;

		// ── Blood cost ──
		gfx.drawString(font, Component.literal("Blood Cost: ").withStyle(s -> s.withColor(0x888888))
				.append(Component.literal((int) rite.getBloodCost() + " mL").withStyle(s -> s.withColor(0xAA4444))), panelX, y, 0);
		y += lineH;

		// ── Required degree ──
		int reqDeg = rite.getRequiredDegree() >= 0 ? rite.getRequiredDegree() : riteMinDegree(type);
		if (reqDeg > 0) {
			EnumInitiatoryDegree needed = EnumInitiatoryDegree.byNumber(reqDeg);
			String degName = needed != null ? needed.getTitle() : ("Degree " + reqDeg);
			int degColor = playerDegree >= reqDeg ? 0xFF88CC88 : 0xFFCC4444;
			gfx.drawString(font, Component.literal("Requires: ").withStyle(s -> s.withColor(0x888888))
					.append(Component.literal(degName).withStyle(s -> s.withColor(degColor))), panelX, y, 0);
			y += lineH;
		}

		// ── Cast time ──
		int ticks = type.getCastingDurationTicks();
		float seconds = ticks / 20f;
		gfx.drawString(font, Component.literal("Cast Time: ").withStyle(s -> s.withColor(0x888888))
				.append(Component.literal(String.format("%.1fs", seconds)).withStyle(s -> s.withColor(0xAAAA88))), panelX, y, 0);
		y += lineH + 6;

		// ── Result item ──
		ItemStack result = rite.getResult();
		if (result != null && !result.isEmpty()) {
			gfx.drawString(font, Component.literal("Result:").withStyle(s -> s.withColor(0x888888)), panelX, y, 0);
			y += lineH;

			gfx.renderItem(result, panelX, y);
			gfx.renderItemDecorations(font, result, panelX, y);
			List<String> resultLines = ScreenDrawUtils.wrapText(font, result.getHoverName().getString(), panelW - 20);
			for (int li = 0; li < resultLines.size(); li++) {
				int ix = li == 0 ? panelX + 20 : panelX + 4;
				gfx.drawString(font, Component.literal(resultLines.get(li))
						.withStyle(s -> s.withColor(0xDDDDDD)), ix, y + 4 + li * lineH, 0);
			}
			y += Math.max(20, resultLines.size() * lineH + 4);
		}

		y += 6;

		// ── Block materials list ──
		if (rite.getPattern() != null) {
			Map<Block, Integer> blockCounts = rite.getPattern().getBlockCount(false);
			if (!blockCounts.isEmpty()) {
				gfx.drawString(font, Component.literal("Materials:").withStyle(s -> s.withColor(0x888888)), panelX, y, 0);
				y += lineH;

				for (Map.Entry<Block, Integer> entry : blockCounts.entrySet()) {
					Block block = entry.getKey();
					if (block == null || block == Blocks.AIR) continue;
					int count = entry.getValue();

					ItemStack blockStack = new ItemStack(block);
					if (!blockStack.isEmpty()) {
						gfx.renderItem(blockStack, panelX + 2, y);
						String countPrefix = " x" + count + "  ";
						List<String> matLines = ScreenDrawUtils.wrapText(font, countPrefix + blockStack.getHoverName().getString(), panelW - 20);
						for (int li = 0; li < matLines.size(); li++) {
							gfx.drawString(font, Component.literal(matLines.get(li))
									.withStyle(s -> s.withColor(0xAAAAAA)), panelX + 20, y + 4 + li * lineH, 0);
						}
						y += Math.max(18, matLines.size() * lineH + 4);
					}
				}
			}
		}

		gfx.disableScissor();

		// Draw scroll indicators if content overflows
		if (totalH > visibleH) {
			if (riteInfoScroll > 0) {
				gfx.drawCenteredString(font, "\u25B2", panelX + panelW / 2, clipTop, 0xAAFFFFFF);
			}
			if (riteInfoScroll < maxScroll) {
				gfx.drawCenteredString(font, "\u25BC", panelX + panelW / 2, clipBottom - 10, 0xAAFFFFFF);
			}
		}
	}

	/** Measures the total content height of the rite info panel (without clipping). */
	private int measureRiteInfoPanelHeight(CardinalRiteRecipe rite, int panelW) {
		int y = 0;
		int lineH = 12;

		// Name
		String name = rite.getRiteName();
		if (name == null || name.isEmpty()) {
			String ritePath = rite.getId().getPath();
			if (ritePath.contains("/")) ritePath = ritePath.substring(ritePath.lastIndexOf('/') + 1);
			name = HLTextUtils.toProperCase(ritePath.replace("_", " "));
		}
		y += ScreenDrawUtils.wrapText(font, name, panelW).size() * lineH;
		y += 4 + 1 + 6; // gap + separator + gap

		// Description
		String desc = rite.getRiteDescription();
		if (desc != null && !desc.isEmpty()) {
			y += ScreenDrawUtils.wrapText(font, desc, panelW).size() * lineH + 4;
		}

		y += lineH; // type
		y += lineH; // blood cost

		CardinalRiteType type = rite.getRiteType();
		int reqDeg = rite.getRequiredDegree() >= 0 ? rite.getRequiredDegree() : riteMinDegree(type);
		if (reqDeg > 0) y += lineH; // degree

		y += lineH + 6; // cast time + gap

		// Result
		ItemStack result = rite.getResult();
		if (result != null && !result.isEmpty()) {
			y += lineH; // "Result:" label
			List<String> resultLines = ScreenDrawUtils.wrapText(font, result.getHoverName().getString(), panelW - 20);
			y += Math.max(20, resultLines.size() * lineH + 4);
		}

		y += 6; // gap

		// Materials
		if (rite.getPattern() != null) {
			Map<Block, Integer> blockCounts = rite.getPattern().getBlockCount(false);
			if (!blockCounts.isEmpty()) {
				y += lineH; // "Materials:" label
				for (Map.Entry<Block, Integer> entry : blockCounts.entrySet()) {
					Block block = entry.getKey();
					if (block == null || block == Blocks.AIR) continue;
					ItemStack blockStack = new ItemStack(block);
					if (!blockStack.isEmpty()) {
						String countPrefix = " x" + entry.getValue() + "  ";
						List<String> matLines = ScreenDrawUtils.wrapText(font, countPrefix + blockStack.getHoverName().getString(), panelW - 20);
						y += Math.max(18, matLines.size() * lineH + 4);
					}
				}
			}
		}

		return y;
	}

	/** Truncates text to fit within maxWidth, appending "..." if necessary. */
	private String truncateText(String text, int maxWidth) {
		if (font.width(text) <= maxWidth) return text;
		int ellipsisW = font.width("...");
		int targetW = maxWidth - ellipsisW;
		while (text.length() > 1 && font.width(text) > targetW) {
			text = text.substring(0, text.length() - 1);
		}
		return text + "...";
	}

	// (Old rite nav buttons removed — now using tier sidebar)

	private void drawNavButton(GuiGraphics gfx, int x, int y, int w, int h, String symbol, boolean hovered, int hoverColor) {
		int bg = hovered ? 0xDD1A0505 : 0x99120303;
		gfx.fill(x, y, x + w, y + h, bg);

		int bc = hovered ? hoverColor : 0xFF444444;
		gfx.fill(x, y, x + w, y + 1, bc);
		gfx.fill(x, y + h - 1, x + w, y + h, bc);
		gfx.fill(x, y, x + 1, y + h, bc);
		gfx.fill(x + w - 1, y, x + w, y + h, bc);

		int textCol = hovered ? 0xFFEEDDFF : 0xFF888888;
		gfx.drawCenteredString(font, symbol, x + w / 2, y + (h - 8) / 2, textCol);
	}

	// ────────────────────────────────────────────────────────────
	//  Layer buttons (shared between Crafting & Rites)
	// ────────────────────────────────────────────────────────────

	/** Returns the X position for layer buttons (left edge of model area, right of sidebar). */
	private int layerBtnX() { return guiLeft + TIER_SIDEBAR_W + 10; }

	/** Returns the Y center for layer buttons (vertically centred in model area). */
	private int layerBtnCenterY() { return guiTop + guiHeight / 2; }

	/**
	 * Draws ▲ (layer up) and ▼ (layer down) buttons and a layer indicator label
	 * on the left side of the 3D model area.
	 */
	private void drawLayerButtons(GuiGraphics gfx, int mouseX, int mouseY,
								  Tab tab, int visibleLayer, int maxLayer) {
		if (maxLayer <= 0) return; // single-layer structure, no buttons needed

		int bx = layerBtnX();
		int cy = layerBtnCenterY();
		int bs = LAYER_BTN_SIZE;
		int color = tab.color;

		// ▲ Up button
		int upY = cy - bs - 14;
		boolean upHov = isOverLayerUpButton(mouseX, mouseY, tab);
		drawNavButton(gfx, bx, upY, bs, bs, "\u25B2", upHov, color);

		// ▼ Down button
		int downY = cy + 14;
		boolean downHov = isOverLayerDownButton(mouseX, mouseY, tab);
		drawNavButton(gfx, bx, downY, bs, bs, "\u25BC", downHov, color);

		// Layer indicator between the buttons
		String label = visibleLayer < 0 ? "All" : "Y:" + (visibleLayer + 1);
		gfx.drawCenteredString(font, label, bx + bs / 2, cy - 4, 0xFFAAAAAA);

		// Tooltip on hover
		if (upHov) {
			gfx.renderTooltip(font, Component.literal("Layer Up"), mouseX, mouseY);
		} else if (downHov) {
			gfx.renderTooltip(font, Component.literal("Layer Down"), mouseX, mouseY);
		}
	}

	private boolean isOverLayerUpButton(double mx, double my, Tab tab) {
		if (tab != Tab.CRAFTING && tab != Tab.RITES) return false;
		int maxL = (tab == Tab.CRAFTING) ? craftingMaxLayer : riteMaxLayer;
		if (maxL <= 0) return false;
		int bx = layerBtnX();
		int cy = layerBtnCenterY();
		int upY = cy - LAYER_BTN_SIZE - 14;
		return mx >= bx && mx <= bx + LAYER_BTN_SIZE
			&& my >= upY && my <= upY + LAYER_BTN_SIZE;
	}

	private boolean isOverLayerDownButton(double mx, double my, Tab tab) {
		if (tab != Tab.CRAFTING && tab != Tab.RITES) return false;
		int maxL = (tab == Tab.CRAFTING) ? craftingMaxLayer : riteMaxLayer;
		if (maxL <= 0) return false;
		int bx = layerBtnX();
		int cy = layerBtnCenterY();
		int downY = cy + 14;
		return mx >= bx && mx <= bx + LAYER_BTN_SIZE
			&& my >= downY && my <= downY + LAYER_BTN_SIZE;
	}

	// ────────────────────────────────────────────────────────────
	//  Milestone drawer (left side, below home button)
	// ────────────────────────────────────────────────────────────

	/**
	 * Draws the collapsible milestone drawer on the left side of the skill
	 * tree screen, below the home button. Mirrors the sidebar pattern from
	 * {@link UnstainedProgressScreen}.
	 * <p>
	 * Milestones whose {@code requiredDegree} exceeds the player's current
	 * initiatory degree are hidden entirely.
	 */
	private void drawMilestoneDrawer(GuiGraphics gfx, int mouseX, int mouseY) {
		int drawerX = guiLeft + HOME_BTN_PAD;
		int drawerY = guiTop + HOME_BTN_PAD + HOME_BTN_SIZE + 4; // below home button
		boolean tabHovered = isOverMilestoneToggle(mouseX, mouseY);

		if (!milestoneDrawerOpen) {
			// ── Collapsed: draw only the toggle tab ──
			drawMilestoneToggleTab(gfx, drawerX, drawerY, false, tabHovered);
			return;
		}

		// ── Expanded drawer ──
		int drawerW = MILESTONE_DRAWER_W;
		int drawerH = guiHeight - (drawerY - guiTop) - 4;

		// Semi-transparent panel background
		gfx.fill(drawerX, drawerY, drawerX + drawerW, drawerY + drawerH, 0xCC1A0505);

		// Panel border
		int borderCol = 0xFF332222;
		gfx.fill(drawerX, drawerY, drawerX + drawerW, drawerY + 1, borderCol);
		gfx.fill(drawerX, drawerY + drawerH - 1, drawerX + drawerW, drawerY + drawerH, borderCol);
		gfx.fill(drawerX, drawerY, drawerX + 1, drawerY + drawerH, borderCol);
		gfx.fill(drawerX + drawerW - 1, drawerY, drawerX + drawerW, drawerY + drawerH, borderCol);

		// Toggle tab on the right edge of the panel (to collapse)
		drawMilestoneToggleTab(gfx, drawerX + drawerW, drawerY, true, tabHovered);

		// Scissor to the drawer interior
		gfx.enableScissor(drawerX + 1, drawerY + 1, drawerX + drawerW - 1, drawerY + drawerH - 1);

		int x = drawerX + 6;
		int y = drawerY + 6 - milestoneScrollOffset;
		int centerX = drawerX + drawerW / 2;

		// Header
		gfx.drawCenteredString(font, Component.literal("Milestones"), centerX, y, 0xFFCC3333);
		y += 12;

		// Summary line
		int completed = SkillPointInit.completedMilestones.size();
		int visible = 0;
		for (HemoMilestone m : HemoMilestone.values()) {
			if (m.getRequiredDegree() <= playerDegree) visible++;
		}
		gfx.drawCenteredString(font,
				Component.literal(completed + "/" + visible + " completed")
						.withStyle(s -> s.withColor(0xFF888888)),
				centerX, y, 0);
		y += 12;

		// Thin divider
		gfx.fill(drawerX + 8, y, drawerX + drawerW - 8, y + 1, 0x33804040);
		y += 5;

		// Render milestones grouped by category
		HemoMilestone.Category lastCategory = null;

		for (HemoMilestone m : HemoMilestone.values()) {
			// Hide milestones above the player's current degree
			if (m.getRequiredDegree() > playerDegree) continue;

			// Category header
			if (m.getCategory() != lastCategory) {
				lastCategory = m.getCategory();
				final HemoMilestone.Category cat = lastCategory;
				y += 3;
				gfx.drawString(font,
						Component.literal("\u25B8 " + cat.getLabel())
								.withStyle(s -> s.withColor(cat.getColor()).withBold(true)),
						x, y, 0, false);
				y += 11;
			}

			boolean complete = SkillPointInit.completedMilestones.contains(m);

			// Check/cross icon + milestone name
			String icon = complete ? "\u2713" : "\u2717";
			int iconCol = complete ? 0xFF60CC60 : 0xFF605050;
			int labelCol = complete ? 0xFFBBAAAA : 0xFF776666;

			gfx.drawString(font, icon, x + 4, y, iconCol, false);

			// Milestone name (use lang key via Component.translatable)
			Component nameComp = Component.translatable(m.getLangKey());
			gfx.drawString(font, nameComp, x + 14, y, labelCol, false);
			y += 10;

			// SP reward line
			String rewardStr = "  +" + m.getSkillPointReward() + " SP";
			int rewardCol = complete ? 0xFF44BB44 : 0xFF555555;
			gfx.drawString(font, rewardStr, x + 4, y, rewardCol, false);
			y += 11;
		}

		// Record total content height for scroll clamping
		int totalContentH = y + milestoneScrollOffset - (drawerY + 6);
		int maxScroll = Math.max(0, totalContentH - (drawerH - 12));
		if (milestoneScrollOffset > maxScroll) {
			milestoneScrollOffset = maxScroll;
		}

		gfx.disableScissor();
	}

	/**
	 * Draws the milestone drawer toggle tab — delegates to the shared
	 * {@link ScreenDrawUtils#drawSidebarToggleTab} with the blood-red theme.
	 */
	private void drawMilestoneToggleTab(GuiGraphics gfx, int tabX, int tabY,
										boolean expanded, boolean hovered) {
		ScreenDrawUtils.drawSidebarToggleTab(gfx, font,
				tabX, tabY, MILESTONE_TAB_W, MILESTONE_TAB_H,
				expanded, hovered,
				0xDD1A0505, 0xCC120303,  // hoverBg, idleBg
				0xFFCC4444, 0xFF332222,  // hoverBc, idleBc
				0xFFFFAAAA, 0xFF886666); // hoverArrow, idleArrow
	}

	/** Hit test for the milestone drawer toggle tab. */
	private boolean isOverMilestoneToggle(double mx, double my) {
		int tabY = guiTop + HOME_BTN_PAD + HOME_BTN_SIZE + 4;
		int tabX;
		if (milestoneDrawerOpen) {
			// Tab is on the right edge of the expanded drawer
			tabX = guiLeft + HOME_BTN_PAD + MILESTONE_DRAWER_W;
		} else {
			// Tab is at the left edge when collapsed
			tabX = guiLeft + HOME_BTN_PAD;
		}
		return mx >= tabX && mx <= tabX + MILESTONE_TAB_W
			&& my >= tabY && my <= tabY + MILESTONE_TAB_H;
	}

	/** Returns true if the mouse is over the expanded milestone drawer area. */
	private boolean isOverMilestoneDrawer(double mx, double my) {
		if (!milestoneDrawerOpen) return false;
		int drawerX = guiLeft + HOME_BTN_PAD;
		int drawerY = guiTop + HOME_BTN_PAD + HOME_BTN_SIZE + 4;
		int drawerW = MILESTONE_DRAWER_W;
		int drawerH = guiHeight - (drawerY - guiTop) - 4;
		return mx >= drawerX && mx <= drawerX + drawerW
			&& my >= drawerY && my <= drawerY + drawerH;
	}

	/** Returns true if the mouse is inside the tier sidebar region (Rites/Crafting/Runes). */
	private boolean isOverTierSidebar(double mx, double my) {
		return mx >= guiLeft && mx <= guiLeft + TIER_SIDEBAR_W
			&& my >= guiTop && my <= guiTop + guiHeight;
	}

	/** Visible height of the scrollable area inside the tier sidebar. */
	private int tierSidebarVisibleH() {
		return guiHeight - 42 - 4; // 42 = title(14) + separator(4) + top padding(24); 4 = bottom margin
	}

	/** Total content height for the Rites tier sidebar. */
	private int riteSidebarContentH() {
		int rowH = 22;
		int total = 0;
		for (CardinalRiteType type : CardinalRiteType.values()) {
			total += rowH + 2;
			if (type == selectedRiteTier) {
				List<CardinalRiteRecipe> recipes = ritesByTier.getOrDefault(type, List.of());
				total += rowH + 2 + recipes.size() * 18;
			}
		}
		return total;
	}

	/** Total content height for the Crafting tier sidebar. */
	private int craftingSidebarContentH() {
		int rowH = 22;
		int total = 0;
		for (String tierName : CRAFTING_TIER_NAMES) {
			total += rowH + 2;
			if (tierName.equals(selectedCraftingTier)) {
				List<BloodStructureRecipe> recipes = craftingByTier.getOrDefault(tierName, List.of());
				total += rowH + 2 + recipes.size() * 18;
			}
		}
		return total;
	}

	/** Total content height for the Runes tier sidebar. */
	private int runeSidebarContentH() {
		int rowH = 22;
		int total = 0;
		for (String tierName : RUNE_TIER_NAMES) {
			total += rowH + 2;
			if (tierName.equals(selectedRuneTier)) {
				List<com.vincenthuto.hemomancy.common.recipe.ChiselRecipe> recipes =
						chiselByTier.getOrDefault(tierName, List.of());
				total += rowH + 2 + recipes.size() * 18;
			}
		}
		return total;
	}

	private void clampRiteSidebarScroll() {
		int maxScroll = Math.max(0, riteSidebarContentH() - tierSidebarVisibleH());
		riteSidebarScroll = Math.min(riteSidebarScroll, maxScroll);
	}

	private void clampCraftingSidebarScroll() {
		int maxScroll = Math.max(0, craftingSidebarContentH() - tierSidebarVisibleH());
		craftingSidebarScroll = Math.min(craftingSidebarScroll, maxScroll);
	}

	private void clampRuneSidebarScroll() {
		int maxScroll = Math.max(0, runeSidebarContentH() - tierSidebarVisibleH());
		runeSidebarScroll = Math.min(runeSidebarScroll, maxScroll);
	}

	// ────────────────────────────────────────────────────────────
	//  Materials & Processes tab — node grid
	// ────────────────────────────────────────────────────────────

	/**
	 * Builds a grid layout of material nodes grouped by category.
	 * Each category starts a new row block; nodes flow left-to-right.
	 */
	private void buildMaterialLayout() {
		List<MaterialEntry> entries = MaterialsData.getBloodEntries();
		int[] bounds = new int[2];
		MaterialsTabView.buildLayout(entries, materialPositions, bounds, NODE_SIZE);
		materialContentW = bounds[0];
		materialContentH = bounds[1];
	}

	/** Draw material category headers and nodes. */
	private void drawMaterialNodes(GuiGraphics gfx) {
		MaterialsTabView.drawNodes(gfx, font,
				MaterialsData.getBloodEntries(), materialPositions,
				view, guiLeft, guiTop, NODE_SIZE, EnumNodeShape.SQUARE,
				Tab.MATERIALS.color, selectedMaterial,
				0x00CC6644, 0xFFBB7733);
	}

	/** Draws an info panel for the selected material on the right side of the screen. */
	private void drawMaterialInfoPanel(GuiGraphics gfx, MaterialEntry mat) {
		MaterialsTabView.drawInfoPanel(gfx, font, mat,
				guiLeft, guiTop, guiWidth,
				Tab.MATERIALS.color, 0xFF442222, 0xDD1A0505,
				MiniRecipeRenderer.BLOOD);
	}

	/** Tooltip for material nodes on hover. */
	private void drawMaterialTooltip(GuiGraphics gfx, int mouseX, int mouseY) {
		MaterialsTabView.drawTooltip(gfx, font, materialPositions,
				view, guiLeft, guiTop, guiWidth, guiHeight, NODE_SIZE,
				EnumNodeShape.SQUARE, Tab.MATERIALS.color, 0xFFBB8833,
				mouseX, mouseY);
	}

	/** Returns the material entry under the mouse, or null. */
	private MaterialEntry materialNodeUnder(double mx, double my) {
		return MaterialsTabView.nodeUnder(materialPositions,
				view, guiLeft, guiTop, NODE_SIZE, EnumNodeShape.SQUARE, mx, my);
	}

	// ────────────────────────────────────────────────────────────
	//  Helpers — delegate to ScreenDrawUtils
	// ────────────────────────────────────────────────────────────


	private static String getSkillInitial(SkillPoint sp) {
		return switch (sp.getName()) {
			case "base"                  -> "\u2726";
			case "skill_capacity"        -> "C";
			case "skill_efficiency"      -> "E";
			case "skill_last_wind"       -> "W";
			case "skill_dynamic_use"     -> "D";
			case "skill_feeding_frenzy"  -> "F";
			case "skill_hemostasis"      -> "H";
			case "skill_sanguine_surge"  -> "S";
			case "skill_crimson_mastery" -> "M";
			case "skill_vital_link"      -> "V";
			case "skill_iron_will"       -> "I";
			case "skill_blood_flow"      -> "B";
			case "skill_coagulation"     -> "G";
			case "skill_sanguine_reach"  -> "R";
			case "skill_rune_affinity"   -> "\u2721";
			case "skill_rune_resonance"  -> "\u2721";
			case "skill_rune_mastery"    -> "\u2721";
			default                      -> "?";
		};
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}

