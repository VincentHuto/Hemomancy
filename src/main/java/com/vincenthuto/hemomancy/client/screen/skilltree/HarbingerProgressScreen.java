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
import com.vincenthuto.hemomancy.common.capability.player.degree.EnumInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.degree.InitiatoryDegreeProvider;
import com.vincenthuto.hemomancy.common.capability.player.kinship.BloodTendencyProvider;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
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
import com.vincenthuto.hemomancy.common.recipe.ScarRecipe;
import com.vincenthuto.hutoslib.client.screen.HLGuiUtils;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

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
public class HarbingerProgressScreen extends Screen {

	// ── Tabs ──
	private enum Tab {
		SKILLS("Skills", 0xFFCC3333),
		MANIPULATIONS("Manipulations", 0xFFCC8833),
		CRAFTING("Crafting", 0xFFAA2222),
		SCARS("Scars", 0xFF44AACC),
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
	private static final float TENDENCY_VALUE_DISTANCE_DIVISOR = 1.75f;
	private static final int TENDENCY_VALUE_VERTICAL_OFFSET = 18;

	// ── Colours ──
	private static final int COL_LINE_LOCKED      = 0x88444444;
	private static final int COL_LINE_UNLOCKED     = 0xFFAA0000;
	private static final int COL_NODE_BG           = 0xCC1A0505;
	private static final int COL_NODE_BORDER_LOCK  = 0xFF333333;
	private static final int COL_NODE_BORDER_UNLOCK= 0xFFCC2222;
	private static final int COL_NODE_BORDER_AVAIL = 0xFFBB8833;
	private static final int ALPHA_OPAQUE_MASK = 0xFF000000;

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
	private int manipRingCenterX, manipRingCenterY;
	private final Map<ManipulationTreeEntry, int[]> manipPositions = new HashMap<>();
	private final Set<String> knownManipNames = new HashSet<>();
	/** Lookup from manipulation name → its BloodMemoryItem's ItemStack. */
	private final Map<String, ItemStack> manipMemoryItems = new HashMap<>();
	private int manipContentW, manipContentH;
	/** Currently selected manipulation node (null = none selected). */
	private ManipulationTreeEntry selectedManipEntry = null;

	// ── Tab state objects (replaces individual rites/crafting/scars field blocks) ──
	private final RitesTabState    ritesState    = new RitesTabState();
	private final CraftingTabState craftingState = new CraftingTabState();
	private final ScarsTabState    scarsState    = new ScarsTabState();

	// ── Materials & Processes data ──
	private final Map<MaterialEntry, int[]> materialPositions = new java.util.LinkedHashMap<>();
	private int materialContentW, materialContentH;
	private MaterialEntry selectedMaterial = null;

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

	public HarbingerProgressScreen() {
		super(Component.translatable("screen.hemomancy.skill_tree"));
	}

	/** Call from the client side to open this screen. */
	public static void openScreen() {
		Minecraft.getInstance().setScreen(new HarbingerProgressScreen());
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
		cacheScarRecipes();

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
			default            -> view; // RITES / CRAFTING / SCARS don't pan
		};
	}

	/** Save current view state and switch to a new tab's view. */
	private void savePan() {
		if (activeTab == Tab.RITES || activeTab == Tab.CRAFTING || activeTab == Tab.SCARS) return;
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

	private String getCurrentInitiatoryRankTitle() {
		EnumInitiatoryDegree currentDegree = EnumInitiatoryDegree.byNumber(playerDegree);
		if (playerDegree <= 0) return "Uninitiated";
		return currentDegree != null ? currentDegree.getTitle() : ("Degree " + playerDegree);
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
		if (ManipulationTreeInit.ENTRIES.isEmpty()) return;

		final int padding = 40;
		Map<EnumBloodTendency, List<ManipulationTreeEntry>> byTendency = new java.util.EnumMap<>(EnumBloodTendency.class);
		List<ManipulationTreeEntry> fallbackEntries = new ArrayList<>();

		for (ManipulationTreeEntry entry : ManipulationTreeInit.ENTRIES) {
			BloodManipulation manip = entry.resolve();
			if (manip == null || manip.getTend() == null) {
				fallbackEntries.add(entry);
				continue;
			}
			byTendency.computeIfAbsent(manip.getTend(), k -> new ArrayList<>()).add(entry);
		}

		Map<ManipulationTreeEntry, int[]> rawPositions = new HashMap<>();
		int maxClusterW = 0;
		int maxClusterH = 0;
		int tendencyCount = 0;
		Map<EnumBloodTendency, int[]> boundsByTendency = new java.util.EnumMap<>(EnumBloodTendency.class);

		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			List<ManipulationTreeEntry> group = byTendency.getOrDefault(tend, List.of());
			if (group.isEmpty()) continue;
			tendencyCount++;
			int minX = Integer.MAX_VALUE;
			int minY = Integer.MAX_VALUE;
			int maxX = Integer.MIN_VALUE;
			int maxY = Integer.MIN_VALUE;
			for (ManipulationTreeEntry entry : group) {
				minX = Math.min(minX, entry.getX());
				minY = Math.min(minY, entry.getY());
				maxX = Math.max(maxX, entry.getX());
				maxY = Math.max(maxY, entry.getY());
			}
			boundsByTendency.put(tend, new int[]{minX, maxX, minY, maxY});
			maxClusterW = Math.max(maxClusterW, maxX - minX + NODE_SIZE);
			maxClusterH = Math.max(maxClusterH, maxY - minY + NODE_SIZE);
		}

		if (tendencyCount == 0) {
			for (ManipulationTreeEntry entry : ManipulationTreeInit.ENTRIES) {
				int x = entry.getX();
				int y = entry.getY();
				manipPositions.put(entry, new int[]{x, y});
				manipContentW = Math.max(manipContentW, x + NODE_SIZE + 20);
				manipContentH = Math.max(manipContentH, y + NODE_SIZE + 24);
			}
			return;
		}

		int radius = Math.max(250, (int) Math.ceil(Math.max(maxClusterW, maxClusterH) * 1.35));
		int clusterHalf = Math.max(maxClusterW, maxClusterH) / 2;
		float centerX = padding + clusterHalf + radius;
		float centerY = padding + clusterHalf + radius;

		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			List<ManipulationTreeEntry> group = byTendency.getOrDefault(tend, List.of());
			if (group.isEmpty()) continue;

			int[] b = boundsByTendency.get(tend);
			float clusterCenterX = (b[0] + b[1]) * 0.5f;
			float clusterCenterY = (b[2] + b[3]) * 0.5f;
			double angle = Math.toRadians(-90f + tend.ordinal() * 45f);
			float anchorX = centerX + (float) Math.cos(angle) * radius;
			float anchorY = centerY + (float) Math.sin(angle) * radius;

			for (ManipulationTreeEntry entry : group) {
				int x = Math.round(anchorX + (entry.getX() - clusterCenterX));
				int y = Math.round(anchorY + (entry.getY() - clusterCenterY));
				rawPositions.put(entry, new int[]{x, y});
			}
		}

		if (!fallbackEntries.isEmpty()) {
			int y = Math.round(centerY + radius + clusterHalf + NODE_GAP_Y);
			int startX = Math.round(centerX - ((fallbackEntries.size() - 1) * NODE_GAP_X) * 0.5f);
			for (int i = 0; i < fallbackEntries.size(); i++) {
				rawPositions.put(fallbackEntries.get(i), new int[]{startX + i * NODE_GAP_X, y});
			}
		}

		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		for (int[] pos : rawPositions.values()) {
			minX = Math.min(minX, pos[0]);
			minY = Math.min(minY, pos[1]);
			maxX = Math.max(maxX, pos[0]);
			maxY = Math.max(maxY, pos[1]);
		}
		int offsetX = padding - minX;
		int offsetY = padding - minY;
		for (var e : rawPositions.entrySet()) {
			int[] p = e.getValue();
			manipPositions.put(e.getKey(), new int[]{p[0] + offsetX, p[1] + offsetY});
		}
		manipContentW = maxX + offsetX + NODE_SIZE + padding;
		manipContentH = maxY + offsetY + NODE_SIZE + 24 + padding;
		manipRingCenterX = Math.round(centerX) + offsetX;
		manipRingCenterY = Math.round(centerY) + offsetY;
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
		ritesState.riteRecipes.clear();
		if (minecraft != null && minecraft.player != null && minecraft.level != null) {
			for (CardinalRiteRecipe r : CardinalRiteRecipe.getAllRecipes(minecraft.level)) {
				if (!r.isUnstained()) ritesState.riteRecipes.add(r);
			}
		}
		ritesState.rebuildTierMap();
		ritesState.autoSelectFirstTier(playerDegree);
	}

	private void cacheCraftingRecipes() {
		craftingState.craftingRecipes.clear();
		if (minecraft != null && minecraft.player != null && minecraft.level != null) {
			for (BloodStructureRecipe r : BloodStructureRecipe.getAllRecipes(minecraft.level)) {
				if (!r.isUnstained()) craftingState.craftingRecipes.add(r);
			}
		}
		craftingState.rebuildTierMap();
		craftingState.autoSelectFirstTier(playerDegree);
	}

	private void cacheScarRecipes() {
		scarsState.scarRecipes.clear();
		if (minecraft != null && minecraft.player != null && minecraft.level != null) {
			scarsState.scarRecipes.addAll(ScarRecipe.getAllRecipes(minecraft.level));
		}
		scarsState.rebuildTierMap();
		scarsState.autoSelectFirstTier(playerDegree);
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

	/** Builds the shared per-frame context used by all tab-view helpers. */
	private ProgressScreenContext makeContext() {
		return new ProgressScreenContext(font, guiLeft, guiTop, guiWidth, guiHeight, playerDegree);
	}

	// ────────────────────────────────────────────────────────────
	//  Input: drag-to-pan, scroll-to-zoom, click-to-unlock
	// ────────────────────────────────────────────────────────────

	@Override
	public boolean mouseClicked(double mx, double my, int btn) {
		if (btn == 0) {
			// Check home button click first (not on browse tabs)
			if (activeTab != Tab.RITES && activeTab != Tab.CRAFTING && activeTab != Tab.SCARS && isOverHomeButton(mx, my)) {
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
					ProgressScreenContext ctx = makeContext();
					String clickedTier = CraftingTabView.tierUnder(ctx, craftingState, mx, my);
					if (clickedTier != null) {
						int tierIdx = java.util.Arrays.asList(CraftingTabState.TIER_NAMES).indexOf(clickedTier);
						if (tierIdx >= 0 && playerDegree >= CraftingTabState.TIER_DEGREE_REQ[tierIdx]) {
							if (clickedTier.equals(craftingState.selectedCraftingTier)) {
								craftingState.selectedCraftingTier = null;
								craftingState.craftingSidebarScroll = 0;
								craftingState.craftingInfoScroll = 0;
							} else {
								craftingState.selectedCraftingTier = clickedTier;
								craftingState.selectedCraftingIndexInTier = 0;
								craftingState.craftingVisibleLayer = -1;
								craftingState.craftingSidebarScroll = 0;
								craftingState.craftingInfoScroll = 0;
							}
						}
						return true;
					}
					int clickedRecipeIdx = CraftingTabView.recipeUnder(ctx, craftingState, mx, my);
					if (clickedRecipeIdx >= 0) {
						craftingState.selectedCraftingIndexInTier = clickedRecipeIdx;
						craftingState.craftingVisibleLayer = -1;
						craftingState.craftingInfoScroll = 0;
						return true;
					}
					if (CraftingTabView.isOverLayerUpButton(ctx, craftingState, mx, my)) {
						int ml = craftingState.craftingMaxLayer;
						if (craftingState.craftingVisibleLayer == -1) craftingState.craftingVisibleLayer = ml;
						else if (craftingState.craftingVisibleLayer < ml) craftingState.craftingVisibleLayer++;
						else craftingState.craftingVisibleLayer = -1;
						return true;
					}
					if (CraftingTabView.isOverLayerDownButton(ctx, craftingState, mx, my)) {
						if (craftingState.craftingVisibleLayer == -1) craftingState.craftingVisibleLayer = 0;
						else if (craftingState.craftingVisibleLayer > 0) craftingState.craftingVisibleLayer--;
						else craftingState.craftingVisibleLayer = -1;
						return true;
					}
					if (mx >= guiLeft + ProgressScreenContext.TIER_SIDEBAR_W + 4) {
						craftingState.craftingDragging = true;
						craftingState.craftingDragLastX = mx;
					}
					return true;
				}
				if (activeTab == Tab.SCARS) {
					ProgressScreenContext ctx = makeContext();
					String clickedScarTier = ScarsTabView.tierUnder(ctx, scarsState, mx, my);
					if (clickedScarTier != null) {
						int tierIdx = java.util.Arrays.asList(ScarsTabState.TIER_NAMES).indexOf(clickedScarTier);
						if (tierIdx >= 0 && playerDegree >= ScarsTabState.TIER_DEGREE_REQ[tierIdx]) {
							if (clickedScarTier.equals(scarsState.selectedScarTier)) {
								scarsState.selectedScarTier = null;
								scarsState.scarSidebarScroll = 0;
							} else {
								scarsState.selectedScarTier = clickedScarTier;
								scarsState.selectedScarIndexInTier = 0;
								scarsState.scarSidebarScroll = 0;
							}
						}
						return true;
					}
					int clickedScarIdx = ScarsTabView.recipeUnder(ctx, scarsState, mx, my);
					if (clickedScarIdx >= 0) {
						scarsState.selectedScarIndexInTier = clickedScarIdx;
						return true;
					}
					return true;
				}
				if (activeTab == Tab.RITES) {
					ProgressScreenContext ctx = makeContext();
					CardinalRiteType clickedRiteTier = RitesTabView.tierUnder(ctx, ritesState, mx, my);
					if (clickedRiteTier != null) {
						if (playerDegree >= RitesTabView.minDegree(clickedRiteTier)
								&& !ritesState.ritesByTier.getOrDefault(clickedRiteTier, List.of()).isEmpty()) {
							if (clickedRiteTier == ritesState.selectedRiteTier) {
								ritesState.selectedRiteTier = null;
								ritesState.riteSidebarScroll = 0;
								ritesState.riteInfoScroll = 0;
							} else {
								ritesState.selectedRiteTier = clickedRiteTier;
								ritesState.selectedRiteIndexInTier = 0;
								ritesState.riteVisibleLayer = -1;
								ritesState.riteSidebarScroll = 0;
								ritesState.riteInfoScroll = 0;
							}
						}
						return true;
					}
					int clickedRiteIdx = RitesTabView.recipeUnder(ctx, ritesState, mx, my);
					if (clickedRiteIdx >= 0) {
						ritesState.selectedRiteIndexInTier = clickedRiteIdx;
						ritesState.riteVisibleLayer = -1;
						ritesState.riteInfoScroll = 0;
						return true;
					}
					if (RitesTabView.isOverLayerUpButton(ctx, ritesState, mx, my)) {
						int ml = ritesState.riteMaxLayer;
						if (ritesState.riteVisibleLayer == -1) ritesState.riteVisibleLayer = ml;
						else if (ritesState.riteVisibleLayer < ml) ritesState.riteVisibleLayer++;
						else ritesState.riteVisibleLayer = -1;
						return true;
					}
					if (RitesTabView.isOverLayerDownButton(ctx, ritesState, mx, my)) {
						if (ritesState.riteVisibleLayer == -1) ritesState.riteVisibleLayer = 0;
						else if (ritesState.riteVisibleLayer > 0) ritesState.riteVisibleLayer--;
						else ritesState.riteVisibleLayer = -1;
						return true;
					}
					if (mx >= guiLeft + ProgressScreenContext.TIER_SIDEBAR_W + 4) {
						ritesState.riteDragging = true;
						ritesState.riteDragLastX = mx;
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
			ritesState.riteDragging = false;
			craftingState.craftingDragging = false;
		}
		return super.mouseReleased(mx, my, btn);
	}

	@Override
	public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
		if (craftingState.craftingDragging && btn == 0 && activeTab == Tab.CRAFTING) {
			craftingState.craftingRotationAngle += (float)(mx - craftingState.craftingDragLastX) * 0.8f;
			craftingState.craftingDragLastX = mx;
			return true;
		}
		if (ritesState.riteDragging && btn == 0 && activeTab == Tab.RITES) {
			ritesState.riteRotationAngle += (float)(mx - ritesState.riteDragLastX) * 0.8f;
			ritesState.riteDragLastX = mx;
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

		// Rites, Crafting & Scars tabs — scroll the tier sidebar or info panel
		if (activeTab == Tab.RITES || activeTab == Tab.CRAFTING || activeTab == Tab.SCARS) {
			ProgressScreenContext ctx = makeContext();
			if (ctx.isOverTierSidebar(mx, my)) {
				int scrollAmt = (int)(-delta * 14);
				if (activeTab == Tab.RITES) {
					ritesState.riteSidebarScroll = Math.max(0, ritesState.riteSidebarScroll + scrollAmt);
					ritesState.clampSidebarScroll(ctx.tierSidebarVisibleH());
				} else if (activeTab == Tab.CRAFTING) {
					craftingState.craftingSidebarScroll = Math.max(0, craftingState.craftingSidebarScroll + scrollAmt);
					craftingState.clampSidebarScroll(ctx.tierSidebarVisibleH());
				} else {
					scarsState.scarSidebarScroll = Math.max(0, scarsState.scarSidebarScroll + scrollAmt);
					scarsState.clampSidebarScroll(ctx.tierSidebarVisibleH());
				}
			} else {
				int scrollAmt = (int)(-delta * 14);
				if (activeTab == Tab.RITES) {
					ritesState.riteInfoScroll = Math.max(0, ritesState.riteInfoScroll + scrollAmt);
				} else if (activeTab == Tab.CRAFTING) {
					craftingState.craftingInfoScroll = Math.max(0, craftingState.craftingInfoScroll + scrollAmt);
				}
			}
			return true;
		}

		view.applyScroll(guiLeft, guiTop, mx, my, delta);
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

		// Auto-rotate 3D models when not dragging
		if (activeTab == Tab.RITES && !ritesState.riteDragging) {
			ritesState.riteRotationAngle += partial * 0.4f;
		}
		if (activeTab == Tab.CRAFTING && !craftingState.craftingDragging) {
			craftingState.craftingRotationAngle += partial * 0.4f;
		}

		// ── 1. Animated vein background (scissored to GUI bounds) ──
		renderVeinBackground(gfx, guiLeft, guiTop, guiWidth, guiHeight);

		// ── 2. Border ──
		ScreenDrawUtils.drawBorder(gfx, guiLeft, guiTop, guiWidth, guiHeight, 0xFF330808, 0xFF220606);

		// ── 3. Tree content (scissored so it clips when panned) ──
		gfx.enableScissor(guiLeft + 2, guiTop + 2,
				guiLeft + guiWidth - 2, guiTop + guiHeight - 2);

		ProgressScreenContext ctx = makeContext();

		if (activeTab == Tab.SKILLS) {
			drawConnections(gfx);
			drawNodes(gfx);
		} else if (activeTab == Tab.MANIPULATIONS) {
			drawManipConnections(gfx);
			drawManipTendencyStar(gfx);
			drawManipNodes(gfx);
		} else if (activeTab == Tab.CRAFTING) {
			CraftingTabView.draw(gfx, ctx, craftingState, mouseX, mouseY, partial);
		} else if (activeTab == Tab.SCARS) {
			ScarsTabView.draw(gfx, ctx, scarsState, mouseX, mouseY, partial);
		} else if (activeTab == Tab.RITES) {
			RitesTabView.draw(gfx, ctx, ritesState, mouseX, mouseY, partial);
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
		if (activeTab != Tab.RITES && activeTab != Tab.CRAFTING && activeTab != Tab.SCARS) {
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

			String rankTitle = getCurrentInitiatoryRankTitle();
			gfx.drawString(font, Component.literal("Rank: " + rankTitle)
					.withStyle(s -> s.withColor(0xFFAA6666)),
					guiLeft + HOME_BTN_PAD + HOME_BTN_SIZE + 4,
					guiTop + HOME_BTN_PAD + HOME_BTN_SIZE + 2, 0);
		}

		if (activeTab != Tab.RITES && activeTab != Tab.CRAFTING && activeTab != Tab.SCARS) {
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
	//  Manipulation tree: tendency star
	// ────────────────────────────────────────────────────────────

	private void drawManipTendencyStar(GuiGraphics gfx) {
		if (minecraft == null || minecraft.player == null) return;

		int screenX = sx(manipRingCenterX);
		int screenY = sy(manipRingCenterY);

		// Cull if entirely outside the viewport
		int starRadius = (int) (95 * view.zoom);
		if (screenX + starRadius < guiLeft || screenX - starRadius > guiLeft + guiWidth
				|| screenY + starRadius < guiTop || screenY - starRadius > guiTop + guiHeight) return;

		minecraft.player.getCapability(BloodTendencyProvider.TENDENCY_CAPA).ifPresent(tendency -> {
			Map<EnumBloodTendency, Float> affs = tendency.getTendency();
			float rotAngle = -90f;
			int outerRadius = (int) (210 * view.zoom);
			int innerRadius = (int) (54 * view.zoom);
			float spikeBaseWidth = 23.5f;
			double valueDist = outerRadius / TENDENCY_VALUE_DISTANCE_DIVISOR;

			for (EnumBloodTendency tend : EnumBloodTendency.values()) {
				float affVal = Mth.clamp(affs.getOrDefault(tend, 0f), 0f, 1f);

				// Base edge points of this spike
				int cx1 = screenX + (int) (Math.cos(Math.toRadians(rotAngle + spikeBaseWidth)) * innerRadius);
				int cy1 = screenY + (int) (Math.sin(Math.toRadians(rotAngle + spikeBaseWidth)) * innerRadius);
				int cx2 = screenX + (int) (Math.cos(Math.toRadians(rotAngle - spikeBaseWidth)) * innerRadius);
				int cy2 = screenY + (int) (Math.sin(Math.toRadians(rotAngle - spikeBaseWidth)) * innerRadius);

				// Tip extends outward proportional to tendency value
				double tipDist = (outerRadius - innerRadius) * affVal * 0.5 + innerRadius;
				int lx = screenX + (int) (Math.cos(Math.toRadians(rotAngle)) * tipDist);
				int ly = screenY + (int) (Math.sin(Math.toRadians(rotAngle)) * tipDist);

				int displace = (int) ((Math.max(cx1, cx2) - Math.min(cx1, cx2)
						+ Math.max(cy1, cy2) - Math.min(cy1, cy2)) / 2f);

				HLGuiUtils.fracLine(gfx.pose(), lx, ly, cx1, cy1, 10, tend.getColor(), displace, 1.1);
				HLGuiUtils.fracLine(gfx.pose(), lx, ly, cx2, cy2, 10, tend.getColor(), displace, 1.1);
				HLGuiUtils.fracLine(gfx.pose(), cx1, cy1, lx, ly, 10, tend.getColor(), displace, 0.8);
				HLGuiUtils.fracLine(gfx.pose(), cx2, cy2, lx, ly, 10, tend.getColor(), displace, 0.8);

				// Numerical tendency value (matches TendencyViewScreen behavior)
				int valueX = screenX + (int) (Math.cos(Math.toRadians(rotAngle)) * valueDist);
				int valueY = screenY + (int) (Math.sin(Math.toRadians(rotAngle)) * valueDist);
				int tendColor = ALPHA_OPAQUE_MASK | tend.getColor().getColor();
				gfx.drawCenteredString(font, String.valueOf(tendency.getAlignmentByTendency(tend)),
						valueX, valueY - TENDENCY_VALUE_VERTICAL_OFFSET, tendColor);

				rotAngle += 45f;
			}
		});
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
	//  Milestone drawer
	// ────────────────────────────────────────────────────────────

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

	/** Returns true if the mouse is inside the tier sidebar region (Rites/Crafting/Scars). */
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
			case "skill_scar_affinity"   -> "\u2721";
			case "skill_scar_resonance"  -> "\u2721";
			case "skill_scar_mastery"    -> "\u2721";
			default                      -> "?";
		};
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
