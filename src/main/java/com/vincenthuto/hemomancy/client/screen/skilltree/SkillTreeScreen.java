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
import com.vincenthuto.hemomancy.common.capability.player.skill.SkillPoint;
import com.vincenthuto.hemomancy.common.init.ManipulationTreeInit;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
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
import net.minecraft.util.Mth;
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
		RITES("Rites", 0xFF8844CC);

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

	// ── Per-tab pan / zoom ──
	private double skillPanX, skillPanY;
	private float skillZoom = 1.0f;
	private double manipPanX, manipPanY;
	private float manipZoom = 1.0f;

	// Active references (point to the current tab's values)
	private double panX, panY;
	private float zoom = 1.0f;
	private static final float ZOOM_MIN = 0.35f;
	private static final float ZOOM_MAX = 3.0f;
	private boolean isDragging;

	// ── Pan limits: how far (in screen pixels) the content edge can be
	//    dragged past the viewport edge before clamping kicks in ──
	private static final int PAN_MARGIN = 150;

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
	private int manipContentW, manipContentH;

	// ── Cardinal Rites data ──
	private final List<CardinalRiteRecipe> riteRecipes = new ArrayList<>();
	private int selectedRiteIndex = 0;
	private float riteRotationAngle = 0f;
	private boolean riteDragging = false;
	private double riteDragLastX = 0;
	private int riteVisibleLayer = -1;  // -1 = show all layers
	private int riteMaxLayer = 0;

	// ── Blood Crafting data ──
	private final List<BloodStructureRecipe> craftingRecipes = new ArrayList<>();
	private int selectedCraftingIndex = 0;
	private float craftingRotationAngle = 0f;
	private boolean craftingDragging = false;
	private double craftingDragLastX = 0;
	private int craftingVisibleLayer = -1;  // -1 = show all layers
	private int craftingMaxLayer = 0;

	// Shared nav button dimensions
	private static final int RITE_NAV_BTN_W = 24;
	private static final int RITE_NAV_BTN_H = 18;
	private static final int LAYER_BTN_SIZE = 16;

	// ── Player initiatory degree (cached for rendering) ──
	private int playerDegree = 0;

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

		// Fill most of the window, leaving a small margin
		int margin = 16;
		guiLeft   = margin;
		guiTop    = margin;
		guiWidth  = width  - margin * 2;
		guiHeight = height - margin * 2;

		clearWidgets();
		buildLayout();
		buildManipLayout();
		seedVeinParams();
		cacheKnownManipulations();
		cacheRiteRecipes();
		cacheCraftingRecipes();

		// Cache the player's initiatory degree for rendering
		if (Minecraft.getInstance().player != null) {
			playerDegree = Minecraft.getInstance().player
					.getCapability(InitiatoryDegreeProvider.DEGREE_CAPA)
					.map(d -> d.getDegreeNumber())
					.orElse(0);
		}

		// Centre each tab's content
		skillPanX = (guiWidth  - skillContentW * skillZoom) / 2.0;
		skillPanY = (guiHeight - skillContentH * skillZoom) / 2.0;
		manipPanX = (guiWidth  - manipContentW * manipZoom) / 2.0;
		manipPanY = (guiHeight - manipContentH * manipZoom) / 2.0;

		restorePan();
	}

	/** Save current pan/zoom into the active tab's slot. */
	private void savePan() {
		if (activeTab == Tab.RITES || activeTab == Tab.CRAFTING) return; // Browse tabs don't use pan/zoom
		clampPan();
		if (activeTab == Tab.SKILLS) {
			skillPanX = panX; skillPanY = panY; skillZoom = zoom;
		} else {
			manipPanX = panX; manipPanY = panY; manipZoom = zoom;
		}
	}

	/** Restore pan/zoom from the active tab's slot. */
	private void restorePan() {
		if (activeTab == Tab.RITES || activeTab == Tab.CRAFTING) return; // Browse tabs don't use pan/zoom
		if (activeTab == Tab.SKILLS) {
			panX = skillPanX; panY = skillPanY; zoom = skillZoom;
		} else {
			panX = manipPanX; panY = manipPanY; zoom = manipZoom;
		}
	}

	/**
	 * Clamp pan so the tree content can never be dragged entirely off-screen.
	 * The content edge is allowed PAN_MARGIN pixels past the viewport edge
	 * so there's a comfortable "rubber band" feel without getting lost.
	 */
	private void clampPan() {
		int contentW = (activeTab == Tab.SKILLS) ? skillContentW : manipContentW;
		int contentH = (activeTab == Tab.SKILLS) ? skillContentH : manipContentH;

		double scaledW = contentW * zoom;
		double scaledH = contentH * zoom;

		// panX: content left edge must not go further right than (guiWidth + margin),
		//       content right edge must not go further left than (-margin).
		double minPanX = -scaledW + (-PAN_MARGIN);
		double maxPanX = guiWidth + PAN_MARGIN;
		panX = Mth.clamp(panX, minPanX, maxPanX);

		double minPanY = -scaledH + (-PAN_MARGIN);
		double maxPanY = guiHeight + PAN_MARGIN;
		panY = Mth.clamp(panY, minPanY, maxPanY);
	}

	/** Reset pan/zoom to the centred default view for the active tab. */
	private void resetToHome() {
		zoom = 1.0f;
		if (activeTab == Tab.SKILLS) {
			panX = (guiWidth  - skillContentW * zoom) / 2.0;
			panY = (guiHeight - skillContentH * zoom) / 2.0;
		} else {
			panX = (guiWidth  - manipContentW * zoom) / 2.0;
			panY = (guiHeight - manipContentH * zoom) / 2.0;
		}
		savePan();
	}

	/** Switch to a different tab. */
	private void switchTab(Tab tab) {
		if (tab == activeTab) return;
		savePan();
		activeTab = tab;
		restorePan();
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
	}

	private void cacheRiteRecipes() {
		riteRecipes.clear();
		if (minecraft != null && minecraft.player != null && minecraft.level != null) {
			riteRecipes.addAll(CardinalRiteRecipe.getAllRecipes(minecraft.level));
		}
		if (selectedRiteIndex >= riteRecipes.size()) {
			selectedRiteIndex = 0;
		}
	}

	private void cacheCraftingRecipes() {
		craftingRecipes.clear();
		if (minecraft != null && minecraft.player != null && minecraft.level != null) {
			craftingRecipes.addAll(BloodStructureRecipe.getAllRecipes(minecraft.level));
		}
		if (selectedCraftingIndex >= craftingRecipes.size()) {
			selectedCraftingIndex = 0;
		}
	}

	// ────────────────────────────────────────────────────────────
	//  Coordinate helpers  (content ↔ screen)
	// ────────────────────────────────────────────────────────────

	/** Content-space → screen-space X */
	private int sx(int cx) { return (int)(guiLeft + panX + cx * zoom); }
	/** Content-space → screen-space Y */
	private int sy(int cy) { return (int)(guiTop  + panY + cy * zoom); }

	/** Screen-space → content-space X */
	private double cx(double sx) { return (sx - guiLeft - panX) / zoom; }
	/** Screen-space → content-space Y */
	private double cy(double sy) { return (sy - guiTop  - panY) / zoom; }

	/** Node half-size on screen, accounting for zoom */
	private int halfNode() { return Math.max(3, (int)(NODE_SIZE * zoom / 2)); }

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
			if (activeTab != Tab.RITES && activeTab != Tab.CRAFTING && isOverHomeButton(mx, my)) {
				resetToHome();
				return true;
			}

			// Check tab clicks first
			Tab clickedTab = tabUnder(mx, my);
			if (clickedTab != null) {
				switchTab(clickedTab);
				return true;
			}

			if (insideGui(mx, my)) {
				if (activeTab == Tab.CRAFTING) {
					// Check crafting nav buttons
					if (isOverCraftingPrevButton(mx, my) && !craftingRecipes.isEmpty()) {
						selectedCraftingIndex = (selectedCraftingIndex - 1 + craftingRecipes.size()) % craftingRecipes.size();
						craftingVisibleLayer = -1;
						return true;
					}
					if (isOverCraftingNextButton(mx, my) && !craftingRecipes.isEmpty()) {
						selectedCraftingIndex = (selectedCraftingIndex + 1) % craftingRecipes.size();
						craftingVisibleLayer = -1;
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
					// Start rotation drag
					craftingDragging = true;
					craftingDragLastX = mx;
					return true;
				}
				if (activeTab == Tab.RITES) {
					// Check rite nav buttons
					if (isOverRitePrevButton(mx, my) && !riteRecipes.isEmpty()) {
						selectedRiteIndex = (selectedRiteIndex - 1 + riteRecipes.size()) % riteRecipes.size();
						riteVisibleLayer = -1;
						return true;
					}
					if (isOverRiteNextButton(mx, my) && !riteRecipes.isEmpty()) {
						selectedRiteIndex = (selectedRiteIndex + 1) % riteRecipes.size();
						riteVisibleLayer = -1;
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
					// Start rotation drag
					riteDragging = true;
					riteDragLastX = mx;
					return true;
				}
				if (activeTab == Tab.SKILLS) {
					SkillPoint hit = nodeUnder(mx, my);
					if (hit != null) {
						tryUnlock(hit);
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
			panX += dx;
			panY += dy;
			savePan();
			return true;
		}
		return super.mouseDragged(mx, my, btn, dx, dy);
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double delta) {
		if (!insideGui(mx, my)) return super.mouseScrolled(mx, my, delta);

		// Rites tab: scroll to cycle through rites
		if (activeTab == Tab.RITES) {
			if (!riteRecipes.isEmpty()) {
				if (delta > 0) {
					selectedRiteIndex = (selectedRiteIndex - 1 + riteRecipes.size()) % riteRecipes.size();
				} else {
					selectedRiteIndex = (selectedRiteIndex + 1) % riteRecipes.size();
				}
				riteVisibleLayer = -1;
			}
			return true;
		}

		// Crafting tab: scroll to cycle through crafting recipes
		if (activeTab == Tab.CRAFTING) {
			if (!craftingRecipes.isEmpty()) {
				if (delta > 0) {
					selectedCraftingIndex = (selectedCraftingIndex - 1 + craftingRecipes.size()) % craftingRecipes.size();
				} else {
					selectedCraftingIndex = (selectedCraftingIndex + 1) % craftingRecipes.size();
				}
				craftingVisibleLayer = -1;
			}
			return true;
		}

		// Remember the content-space point under the cursor
		double cxBefore = cx(mx);
		double cyBefore = cy(my);

		float oldZoom = zoom;
		zoom = Mth.clamp(zoom + (float) delta * 0.15f, ZOOM_MIN, ZOOM_MAX);

		// Adjust pan so that point stays under the cursor
		panX += cxBefore * (oldZoom - zoom);
		panY += cyBefore * (oldZoom - zoom);
		savePan();
		return true;
	}

	private SkillPoint nodeUnder(double mx, double my) {
		int h = halfNode();
		for (var e : nodePositions.entrySet()) {
			int[] p = e.getValue();
			int nx = sx(p[0]), ny = sy(p[1]);
			if (mx >= nx - h && mx <= nx + h && my >= ny - h && my <= ny + h)
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
		drawBorder(gfx, guiLeft, guiTop, guiWidth, guiHeight);

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
		} else if (activeTab == Tab.RITES) {
			drawRiteContent(gfx, mouseX, mouseY, partial);
		}

		gfx.disableScissor();

		// ── 4. Tab buttons (top-right, outside scissor) ──
		drawTabs(gfx, mouseX, mouseY);

		// ── 4b. Home button (top-left, outside scissor; not on browse tabs) ──
		if (activeTab != Tab.RITES && activeTab != Tab.CRAFTING) {
			drawHomeButton(gfx, mouseX, mouseY);
		}

		// ── 5. Overlay text ──
		gfx.drawCenteredString(font,
				Component.literal(activeTab.label),
				guiLeft + guiWidth / 2, guiTop + 5, activeTab.color);

		if (activeTab == Tab.SKILLS) {
			// Display current skill points in top-left area of the GUI
			String spText = "Skill Points: " + SkillPointInit.skillPoints;
			gfx.drawString(font, Component.literal(spText)
					.withStyle(s -> s.withColor(0xFFBB8833).withBold(true)),
					guiLeft + 5, guiTop + 18, 0);
		}

		if (activeTab != Tab.RITES && activeTab != Tab.CRAFTING) {
			gfx.drawString(font,
					String.format("%.0f%%", zoom * 100),
					guiLeft + 5, guiTop + guiHeight - 12, 0x55888888, false);
		}

		// ── 6. Tooltip (must be outside scissor) ──
		if (activeTab == Tab.SKILLS) {
			drawTooltip(gfx, mouseX, mouseY);
		} else if (activeTab == Tab.MANIPULATIONS) {
			drawManipTooltip(gfx, mouseX, mouseY);
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
	//  Border
	// ────────────────────────────────────────────────────────────

	private void drawBorder(GuiGraphics gfx, int x, int y, int w, int h) {
		int outer = 0xFF330808;
		gfx.fill(x, y, x + w, y + 1, outer);
		gfx.fill(x, y + h - 1, x + w, y + h, outer);
		gfx.fill(x, y, x + 1, y + h, outer);
		gfx.fill(x + w - 1, y, x + w, y + h, outer);

		int inner = 0xFF220606;
		gfx.fill(x + 1, y + 1, x + w - 1, y + 2, inner);
		gfx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, inner);
		gfx.fill(x + 1, y + 1, x + 2, y + h - 1, inner);
		gfx.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, inner);
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
			int lw  = Math.max(1, (int)(zoom * 1.5f));

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
						gfx.fill(nx - hn - 3, ny - hn - 3, nx + hn + 3, ny + hn + 3,
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
			gfx.fill(nx - hn, ny - hn, nx + hn, ny + hn, COL_NODE_BG);

			// ── border (1-px lines) ──
			gfx.fill(nx - hn, ny - hn, nx + hn, ny - hn + 1, border);
			gfx.fill(nx - hn, ny + hn - 1, nx + hn, ny + hn, border);
			gfx.fill(nx - hn, ny - hn, nx - hn + 1, ny + hn, border);
			gfx.fill(nx + hn - 1, ny - hn, nx + hn, ny + hn, border);

			// ── degree-locked overlay: dark fill + black "?" ──
			if (degreeLocked) {
				gfx.fill(nx - hn + 1, ny - hn + 1, nx + hn - 1, ny + hn - 1, 0xBB000000);
				if (zoom >= 0.5f) {
					gfx.drawCenteredString(font, "?", nx, ny - 4, 0xFF111111);
				}
				continue; // skip normal text rendering for degree-locked nodes
			}

			// ── text (only when zoomed in enough) ──
			if (zoom >= 0.5f) {
				String ini = getSkillInitial(sp);
				int textCol = sp.getState() == EnumSkillStates.UNLOCKED
						? 0xFFFFAAAA : 0xFF888888;
				gfx.drawCenteredString(font, ini, nx, ny - 4, textCol);

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

			if (mouseX < nx - hn || mouseX > nx + hn
					|| mouseY < ny - hn || mouseY > ny + hn) continue;

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
		int lw = Math.max(1, (int)(zoom * 1.5f));

		for (ManipulationTreeEntry entry : ManipulationTreeInit.ENTRIES) {
			int[] childPos = manipPositions.get(entry);
			if (childPos == null) continue;
			boolean childKnown = knownManipNames.contains(entry.getManipName());

			for (String parentName : entry.getParentNames()) {
				ManipulationTreeEntry parentEntry = ManipulationTreeInit.getEntry(parentName);
				if (parentEntry == null) continue;
				int[] parentPos = manipPositions.get(parentEntry);
				if (parentPos == null) continue;

				boolean parentKnown = knownManipNames.contains(parentName);
				int col = (parentKnown && childKnown) ? 0xFFAA6600 : COL_LINE_LOCKED;

				// Get tendency color for the connection if both are known
				if (parentKnown && childKnown) {
					BloodManipulation manip = entry.resolve();
					if (manip != null) {
						ParticleColor pc = manip.getTend().getColor();
						int r = (int) Math.min(pc.getRed() * 0.7f, 255);
						int g = (int) Math.min(pc.getGreen() * 0.7f, 255);
						int b = (int) Math.min(pc.getBlue() * 0.7f, 255);
						col = 0xCC000000 | (r << 16) | (g << 8) | b;
					}
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

			// ── Tendency colour ──
			int tendR = 128, tendG = 128, tendB = 128;
			if (manip != null) {
				ParticleColor pc = manip.getTend().getColor();
				tendR = (int) pc.getRed();
				tendG = (int) pc.getGreen();
				tendB = (int) pc.getBlue();
			}

			int borderColor;
			if (known) {
				borderColor = 0xFF000000 | (tendR << 16) | (tendG << 8) | tendB;

				// Pulsing glow in tendency colour
				float pulse = 0.5f + 0.5f * Mth.sin(time * 2f + entry.getManipName().hashCode() * 0.1f);
				int ga = (int)(35 * pulse);
				int gr = (int)(tendR * 0.6f);
				int gg = (int)(tendG * 0.6f);
				int gb = (int)(tendB * 0.6f);
				gfx.fill(nx - hn - 3, ny - hn - 3, nx + hn + 3, ny + hn + 3,
						(ga << 24) | (gr << 16) | (gg << 8) | gb);
			} else {
				// Dimmed border for unknown
				int dr = (int)(tendR * 0.3f);
				int dg = (int)(tendG * 0.3f);
				int db = (int)(tendB * 0.3f);
				borderColor = 0xFF000000 | (dr << 16) | (dg << 8) | db;
			}

			// ── Fill ──
			int fill = known ? COL_NODE_BG : 0xCC0D0303;
			gfx.fill(nx - hn, ny - hn, nx + hn, ny + hn, fill);

			// ── Border ──
			gfx.fill(nx - hn, ny - hn, nx + hn, ny - hn + 1, borderColor);
			gfx.fill(nx - hn, ny + hn - 1, nx + hn, ny + hn, borderColor);
			gfx.fill(nx - hn, ny - hn, nx - hn + 1, ny + hn, borderColor);
			gfx.fill(nx + hn - 1, ny - hn, nx + hn, ny + hn, borderColor);

			// ── Type symbol + name ──
			if (zoom >= 0.5f) {
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

				// Name below node
				if (manip != null && zoom >= 0.7f) {
					String label = HLTextUtils.toProperCase(manip.getName().replace("_", " "));
					int labelCol = known ? (0xFF000000 | (tendR << 16) | (tendG << 8) | tendB) : 0xFF444444;
					gfx.drawCenteredString(font, label, nx, ny + hn + 3, labelCol);
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

			if (mouseX < nx - hn || mouseX > nx + hn
					|| mouseY < ny - hn || mouseY > ny + hn) continue;

			BloodManipulation manip = entry.resolve();
			boolean known = knownManipNames.contains(entry.getManipName());

			List<Component> tip = new ArrayList<>();

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
				tip.add(Component.literal("Tendency: " + HLTextUtils.toProperCase(manip.getTend().name()))
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

			gfx.renderTooltip(font, tip, Optional.empty(), mouseX, mouseY);
			break;
		}
	}

	// ────────────────────────────────────────────────────────────
	//  Home button (top-left corner of the GUI)
	// ────────────────────────────────────────────────────────────

	private void drawHomeButton(GuiGraphics gfx, int mouseX, int mouseY) {
		int bx = guiLeft + HOME_BTN_PAD;
		int by = guiTop + HOME_BTN_PAD;
		int bs = HOME_BTN_SIZE;
		boolean hovered = isOverHomeButton(mouseX, mouseY);

		// Background
		int bg = hovered ? 0xDD1A0505 : 0x99120303;
		gfx.fill(bx, by, bx + bs, by + bs, bg);

		// Border
		int bc = hovered ? 0xFFCC3333 : 0xFF444444;
		gfx.fill(bx, by, bx + bs, by + 1, bc);
		gfx.fill(bx, by + bs - 1, bx + bs, by + bs, bc);
		gfx.fill(bx, by, bx + 1, by + bs, bc);
		gfx.fill(bx + bs - 1, by, bx + bs, by + bs, bc);

		// House icon — draw a simple ⌂ symbol centered in the button
		int textCol = hovered ? 0xFFFFAAAA : 0xFF888888;
		gfx.drawCenteredString(font, "\u2302", bx + bs / 2, by + (bs - 8) / 2, textCol);

		// Tooltip on hover
		if (hovered) {
			gfx.renderTooltip(font, Component.literal("Return to Center"), mouseX, mouseY);
		}
	}

	// ────────────────────────────────────────────────────────────
	//  Tabs (top-right corner of the GUI)
	// ────────────────────────────────────────────────────────────

	private void drawTabs(GuiGraphics gfx, int mouseX, int mouseY) {
		int x = guiLeft + guiWidth - TAB_PAD;
		int y = guiTop + TAB_PAD;

		// Draw tabs right-to-left so the first tab is rightmost
		for (int i = Tab.values().length - 1; i >= 0; i--) {
			Tab tab = Tab.values()[i];
			int tw = font.width(tab.label) + 10;
			int tx = x - tw;

			boolean active = (tab == activeTab);
			boolean hovered = mouseX >= tx && mouseX <= tx + tw
					&& mouseY >= y && mouseY <= y + TAB_HEIGHT;

			// Background
			int bg = active ? 0xDD1A0505 : (hovered ? 0xBB180404 : 0x99120303);
			gfx.fill(tx, y, tx + tw, y + TAB_HEIGHT, bg);

			// Border
			int bc = active ? tab.color : 0xFF444444;
			gfx.fill(tx, y, tx + tw, y + 1, bc);               // top
			gfx.fill(tx, y + TAB_HEIGHT - 1, tx + tw, y + TAB_HEIGHT, bc); // bottom
			gfx.fill(tx, y, tx + 1, y + TAB_HEIGHT, bc);        // left
			gfx.fill(tx + tw - 1, y, tx + tw, y + TAB_HEIGHT, bc); // right

			// Active tab: bright underline
			if (active) {
				gfx.fill(tx + 1, y + TAB_HEIGHT - 2, tx + tw - 1, y + TAB_HEIGHT - 1, tab.color);
			}

			// Label
			int textCol = active ? tab.color : (hovered ? 0xFFAAAAAA : 0xFF777777);
			gfx.drawCenteredString(font, tab.label, tx + tw / 2, y + (TAB_HEIGHT - 8) / 2, textCol);

			x = tx - TAB_PAD;
		}
	}

	/** Returns the tab under the mouse, or null. */
	private Tab tabUnder(double mx, double my) {
		int x = guiLeft + guiWidth - TAB_PAD;
		int y = guiTop + TAB_PAD;

		for (int i = Tab.values().length - 1; i >= 0; i--) {
			Tab tab = Tab.values()[i];
			int tw = font.width(tab.label) + 10;
			int tx = x - tw;

			if (mx >= tx && mx <= tx + tw && my >= y && my <= y + TAB_HEIGHT) {
				return tab;
			}
			x = tx - TAB_PAD;
		}
		return null;
	}

	/** Returns true if the mouse is over the home button. */
	private boolean isOverHomeButton(double mx, double my) {
		int bx = guiLeft + HOME_BTN_PAD;
		int by = guiTop + HOME_BTN_PAD;
		return mx >= bx && mx <= bx + HOME_BTN_SIZE
			&& my >= by && my <= by + HOME_BTN_SIZE;
	}

	// ────────────────────────────────────────────────────────────
	//  Blood Crafting tab
	// ────────────────────────────────────────────────────────────

	private void drawCraftingContent(GuiGraphics gfx, int mouseX, int mouseY, float partial) {
		if (craftingRecipes.isEmpty()) {
			gfx.drawCenteredString(font, "No Blood Crafting recipes found",
					guiLeft + guiWidth / 2, guiTop + guiHeight / 2, 0xFF666666);
			return;
		}

		BloodStructureRecipe recipe = craftingRecipes.get(selectedCraftingIndex);

		// Layout: left half = 3D model, right half = info panel
		int midX = guiLeft + guiWidth / 2;

		// ── 3D multiblock preview (left half) ──
		drawCraftingModel(gfx, recipe, guiLeft + 10, guiTop + 30,
				midX - guiLeft - 20, guiHeight - 60);

		// ── Layer buttons (left side of model area) ──
		drawLayerButtons(gfx, mouseX, mouseY, Tab.CRAFTING, craftingVisibleLayer, craftingMaxLayer);

		// ── Info panel (right half) ──
		drawCraftingInfoPanel(gfx, recipe, midX + 10, guiTop + 30, guiWidth / 2 - 20);

		// ── Navigation buttons (bottom center) ──
		drawCraftingNavButtons(gfx, mouseX, mouseY);

		// ── Page indicator ──
		String pageStr = (selectedCraftingIndex + 1) + " / " + craftingRecipes.size();
		gfx.drawCenteredString(font, pageStr,
				guiLeft + guiWidth / 2, guiTop + guiHeight - 18, 0xFF888888);

		// ── Drag hint ──
		gfx.drawCenteredString(font, "Drag to rotate",
				guiLeft + (midX - guiLeft) / 2 + 5, guiTop + guiHeight - 18, 0x44888888);
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
		int y = panelY;
		int lineH = 12;

		// ── Recipe name (derived from ID) ──
		String name = HLTextUtils.toProperCase(recipe.getId().getPath().replace("_", " "));
		gfx.drawString(font, Component.literal(name)
				.withStyle(s -> s.withColor(0xCC3333).withBold(true)), panelX, y, 0);
		y += lineH + 4;

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
			gfx.drawString(font, heldItem.getHoverName().copy()
					.withStyle(s -> s.withColor(0xDDDDDD)), panelX + 20, y + 4, 0);
			y += 20;
		}

		// ── Hit block ──
		Block hitBlock = recipe.getHitBlock();
		if (hitBlock != null && hitBlock != Blocks.AIR) {
			gfx.drawString(font, Component.literal("Activate on:").withStyle(s -> s.withColor(0x888888)), panelX, y, 0);
			y += lineH;

			ItemStack hitStack = new ItemStack(hitBlock);
			if (!hitStack.isEmpty()) {
				gfx.renderItem(hitStack, panelX, y);
				gfx.drawString(font, hitStack.getHoverName().copy()
						.withStyle(s -> s.withColor(0xDDDDDD)), panelX + 20, y + 4, 0);
				y += 20;
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
			gfx.drawString(font, result.getHoverName().copy()
					.withStyle(s -> s.withColor(0xDDDDDD)), panelX + 20, y + 4, 0);
			y += 20;
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
						gfx.drawString(font, Component.literal(" x" + count + "  ")
								.append(blockStack.getHoverName().copy())
								.withStyle(s -> s.withColor(0xAAAAAA)), panelX + 20, y + 4, 0);
						y += 18;
					}
				}
			}
		}
	}

	// ── Crafting nav buttons ──

	private void drawCraftingNavButtons(GuiGraphics gfx, int mouseX, int mouseY) {
		if (craftingRecipes.size() <= 1) return;

		int centerX = guiLeft + guiWidth / 2;
		int btnY = guiTop + guiHeight - 38;

		int prevX = centerX - RITE_NAV_BTN_W - 30;
		boolean prevHov = isOverCraftingPrevButton(mouseX, mouseY);
		drawNavButton(gfx, prevX, btnY, RITE_NAV_BTN_W, RITE_NAV_BTN_H, "\u25C0", prevHov, Tab.CRAFTING.color);

		int nextX = centerX + 30;
		boolean nextHov = isOverCraftingNextButton(mouseX, mouseY);
		drawNavButton(gfx, nextX, btnY, RITE_NAV_BTN_W, RITE_NAV_BTN_H, "\u25B6", nextHov, Tab.CRAFTING.color);
	}

	private boolean isOverCraftingPrevButton(double mx, double my) {
		int centerX = guiLeft + guiWidth / 2;
		int btnY = guiTop + guiHeight - 38;
		int prevX = centerX - RITE_NAV_BTN_W - 30;
		return mx >= prevX && mx <= prevX + RITE_NAV_BTN_W
			&& my >= btnY && my <= btnY + RITE_NAV_BTN_H;
	}

	private boolean isOverCraftingNextButton(double mx, double my) {
		int centerX = guiLeft + guiWidth / 2;
		int btnY = guiTop + guiHeight - 38;
		int nextX = centerX + 30;
		return mx >= nextX && mx <= nextX + RITE_NAV_BTN_W
			&& my >= btnY && my <= btnY + RITE_NAV_BTN_H;
	}

	// ────────────────────────────────────────────────────────────
	//  Cardinal Rites tab
	// ────────────────────────────────────────────────────────────

	private void drawRiteContent(GuiGraphics gfx, int mouseX, int mouseY, float partial) {
		if (riteRecipes.isEmpty()) {
			gfx.drawCenteredString(font, "No Cardinal Rites found",
					guiLeft + guiWidth / 2, guiTop + guiHeight / 2, 0xFF666666);
			return;
		}

		CardinalRiteRecipe rite = riteRecipes.get(selectedRiteIndex);

		// Layout: left half = 3D model, right half = info panel
		int midX = guiLeft + guiWidth / 2;

		// ── 3D multiblock preview (left half) ──
		drawRiteModel(gfx, rite, guiLeft + 10, guiTop + 30,
				midX - guiLeft - 20, guiHeight - 60, partial);

		// ── Layer buttons (left side of model area) ──
		drawLayerButtons(gfx, mouseX, mouseY, Tab.RITES, riteVisibleLayer, riteMaxLayer);

		// ── Info panel (right half) ──
		drawRiteInfoPanel(gfx, rite, midX + 10, guiTop + 30, guiWidth / 2 - 20, mouseX, mouseY);

		// ── Navigation buttons (bottom center) ──
		drawRiteNavButtons(gfx, mouseX, mouseY);

		// ── Page indicator ──
		String pageStr = (selectedRiteIndex + 1) + " / " + riteRecipes.size();
		gfx.drawCenteredString(font, pageStr,
				guiLeft + guiWidth / 2, guiTop + guiHeight - 18, 0xFF888888);

		// ── Drag hint ──
		gfx.drawCenteredString(font, "Drag to rotate",
				guiLeft + (midX - guiLeft) / 2 + 5, guiTop + guiHeight - 18, 0x44888888);
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
		int y = panelY;
		int lineH = 12;

		// ── Rite name ──
		String name = rite.getRiteName();
		if (name == null || name.isEmpty()) {
			name = HLTextUtils.toProperCase(rite.getId().getPath().replace("_", " "));
		}
		gfx.drawString(font, Component.literal(name)
				.withStyle(s -> s.withColor(0xCC66DD).withBold(true)), panelX, y, 0);
		y += lineH + 4;

		// ── Separator line ──
		gfx.fill(panelX, y, panelX + panelW, y + 1, 0xFF442244);
		y += 6;

		// ── Description ──
		String desc = rite.getRiteDescription();
		if (desc != null && !desc.isEmpty()) {
			// Word-wrap the description
			List<String> lines = wrapText(desc, panelW);
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

			// Render the item icon
			gfx.renderItem(result, panelX, y);
			gfx.renderItemDecorations(font, result, panelX, y);

			// Item name next to the icon
			gfx.drawString(font, result.getHoverName().copy()
					.withStyle(s -> s.withColor(0xDDDDDD)), panelX + 20, y + 4, 0);
			y += 20;
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

					// Render block as item
					ItemStack blockStack = new ItemStack(block);
					if (!blockStack.isEmpty()) {
						gfx.renderItem(blockStack, panelX + 2, y);
						gfx.drawString(font, Component.literal(" x" + count + "  ")
								.append(blockStack.getHoverName().copy())
								.withStyle(s -> s.withColor(0xAAAAAA)), panelX + 20, y + 4, 0);
						y += 18;
					}
				}
			}
		}
	}

	/** Simple word-wrap helper. */
	private List<String> wrapText(String text, int maxWidth) {
		List<String> lines = new ArrayList<>();
		String[] words = text.split(" ");
		StringBuilder current = new StringBuilder();
		for (String word : words) {
			if (current.length() > 0 && font.width(current + " " + word) > maxWidth) {
				lines.add(current.toString());
				current = new StringBuilder(word);
			} else {
				if (current.length() > 0) current.append(" ");
				current.append(word);
			}
		}
		if (current.length() > 0) lines.add(current.toString());
		return lines;
	}

	// ── Rite nav buttons ──

	private void drawRiteNavButtons(GuiGraphics gfx, int mouseX, int mouseY) {
		if (riteRecipes.size() <= 1) return;

		int centerX = guiLeft + guiWidth / 2;
		int btnY = guiTop + guiHeight - 38;

		// ◀ Prev button
		int prevX = centerX - RITE_NAV_BTN_W - 30;
		boolean prevHov = isOverRitePrevButton(mouseX, mouseY);
		drawNavButton(gfx, prevX, btnY, RITE_NAV_BTN_W, RITE_NAV_BTN_H, "\u25C0", prevHov, Tab.RITES.color);

		// ▶ Next button
		int nextX = centerX + 30;
		boolean nextHov = isOverRiteNextButton(mouseX, mouseY);
		drawNavButton(gfx, nextX, btnY, RITE_NAV_BTN_W, RITE_NAV_BTN_H, "\u25B6", nextHov, Tab.RITES.color);
	}

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

	private boolean isOverRitePrevButton(double mx, double my) {
		int centerX = guiLeft + guiWidth / 2;
		int btnY = guiTop + guiHeight - 38;
		int prevX = centerX - RITE_NAV_BTN_W - 30;
		return mx >= prevX && mx <= prevX + RITE_NAV_BTN_W
			&& my >= btnY && my <= btnY + RITE_NAV_BTN_H;
	}

	private boolean isOverRiteNextButton(double mx, double my) {
		int centerX = guiLeft + guiWidth / 2;
		int btnY = guiTop + guiHeight - 38;
		int nextX = centerX + 30;
		return mx >= nextX && mx <= nextX + RITE_NAV_BTN_W
			&& my >= btnY && my <= btnY + RITE_NAV_BTN_H;
	}

	// ────────────────────────────────────────────────────────────
	//  Layer buttons (shared between Crafting & Rites)
	// ────────────────────────────────────────────────────────────

	/** Returns the X position for layer buttons (left edge of model area). */
	private int layerBtnX() { return guiLeft + 14; }

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
	//  Helpers
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
			default                      -> "?";
		};
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
