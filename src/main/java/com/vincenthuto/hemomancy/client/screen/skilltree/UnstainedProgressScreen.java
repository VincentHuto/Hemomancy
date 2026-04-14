package com.vincenthuto.hemomancy.client.screen.skilltree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumPurityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressProvider;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.PacketToggleUnstainedBonus;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import com.vincenthuto.hutoslib.math.BlockPosBlockPair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Unstained Progress screen — displays the player's purification and clarity
 * progression along the Unstained path. Opened via the Self-Reflection Mirror item.
 * <p>
 * Features:
 * <ul>
 *   <li>Dark blue background with floating hollow rhombus particles</li>
 *   <li>Click-and-drag panning</li>
 *   <li>Scroll-wheel zoom (anchored to cursor)</li>
 *   <li>Home button to reset view</li>
 *   <li>Diamond-shaped stage nodes</li>
 *   <li>Purity stage progression (left column)</li>
 *   <li>Clarity stage progression (right column, if unlocked)</li>
 *   <li>Progress bars and derived stat readouts</li>
 *   <li>Tooltips for each stage node</li>
 * </ul>
 */
public class UnstainedProgressScreen extends Screen {

	// ── Tabs ──
	private enum UTab {
		PROGRESS("Progress", 0xFFB0C0E0),
		RITES("Rites", 0xFF8090BB),
		CRAFTING("Crafting", 0xFF80D0C0),
		MATERIALS("Materials", 0xFF80B0A0);

		final String label;
		final int color;
		UTab(String label, int color) { this.label = label; this.color = color; }
	}

	private UTab activeTab = UTab.PROGRESS;
	private static final int TAB_HEIGHT = 16;
	private static final int TAB_PAD = 4;

	// ── Colours — silver/teal Unstained palette on dark blue ──
	private static final int PURITY_COLOR       = 0xFFB0C0E0;
	private static final int PURITY_GLOW        = 0xFF8090BB;

	private static final int CLARITY_COLOR      = 0xFF80D0C0;
	private static final int CLARITY_GLOW       = 0xFF50AA90;

	private static final int COL_BORDER_OUTER   = 0xFF203050;
	private static final int COL_BORDER_INNER   = 0xFF101828;

	private static final int COL_NODE_BG        = 0xCC0C1020;
	private static final int COL_NODE_LOCKED    = 0xFF303848;
	private static final int COL_NODE_REACHED   = 0xFF6088B0;
	private static final int COL_NODE_CURRENT   = 0xFFB0C0E0;

	private static final int COL_LINE_LOCKED    = 0x66283848;
	private static final int COL_LINE_REACHED   = 0xFF5080B0;

	// ── Floating hollow rhombus particles ──
	private static final int RHOMBUS_COUNT      = 10;

	// ── Node layout (content-space pixels) ──
	private static final int NODE_SIZE    = 30;
	private static final int NODE_GAP_Y   = 72;
	private static final int COLUMN_SPACING = 260;

	// ── Pan / zoom (encapsulated in PanZoomState) ──
	private final PanZoomState progressView = new PanZoomState();
	private final PanZoomState materialView = new PanZoomState();
	/** Active view — points to whichever tab's PanZoomState is current. */
	private PanZoomState view = progressView;
	private boolean isDragging;

	// ── Home button ──
	private static final int HOME_BTN_SIZE = 16;
	private static final int HOME_BTN_PAD  = 4;

	// ── Milestones sidebar ──
	private static final int SIDEBAR_WIDTH = 170;
	private static final int SIDEBAR_TAB_W = 14;  // collapsed tab width
	private static final int SIDEBAR_TAB_H = 50;  // collapsed tab height
	private boolean sidebarVisible = true;

	// ── Bonus toggle panel (right side) ──
	private static final int BONUS_BTN_SIZE = 24;
	private static final int BONUS_BTN_GAP  = 6;
	private static final int BONUS_PANEL_PAD = 6;

	// ── GUI viewport (screen-space pixels, set in init()) ──
	private int guiLeft, guiTop, guiWidth, guiHeight;

	// ── Content bounds (content-space) ──
	private int contentW, contentH;

	// ── Cached capability data ──
	private boolean begunPurification;
	private float purity;
	private boolean clarityUnlocked;
	private float clarity;
	private float silverWardStrength;
	private float verdigrisAura;
	private EnumPurityStage currentPurityStage;
	private EnumClarityStage currentClarityStage;

	// ── Cached bonus toggle state ──
	private boolean silverWardEnabled;
	private boolean verdigrisAuraEnabled;

	// ── Cached milestone data ──
	private int mHemoKills, mUndeadKills, mHostileKills, mFlawlessKills;
	private int mAnimalsBreed, mCropsPlanted, mAdvancementsEarned, mNightsSlept, mPetsHealed;
	private boolean mSleptHemolysis, mFirstHemoKill, mReachedAbstinence, mEmptiedBlood, mEarnedAdvancement;

	// ── Background floating rhombus params (seeded for consistency) ──
	// Each: [startX ratio, startY ratio, size, velX, velY, phase, brightness]
	private float[][] rhombusParams;

	// ── Materials & Processes tab data ──
	private final java.util.Map<MaterialEntry, int[]> materialPositions = new java.util.LinkedHashMap<>();
	private int matContentW, matContentH;
	private MaterialEntry selectedMaterial = null;

	// ── Unstained Rites tab data ──
	private static final int TIER_SIDEBAR_W = 130;
	private final List<CardinalRiteRecipe> riteRecipes = new ArrayList<>();
	private final java.util.LinkedHashMap<CardinalRiteType, List<CardinalRiteRecipe>> ritesByTier = new java.util.LinkedHashMap<>();
	private CardinalRiteType selectedRiteTier = null;
	private int selectedRiteIndexInTier = 0;
	private float riteRotationAngle = 0f;
	private boolean riteDragging = false;
	private double riteDragLastX = 0;
	private int riteVisibleLayer = -1;
	private int riteMaxLayer = 0;
	private int riteSidebarScroll = 0;
	private int riteInfoScroll = 0;

	// ── Unstained Crafting tab data ──
	private final List<BloodStructureRecipe> craftingRecipes = new ArrayList<>();
	private final java.util.LinkedHashMap<String, List<BloodStructureRecipe>> craftingByTier = new java.util.LinkedHashMap<>();
	private static final String[] CRAFTING_TIER_NAMES = { "Basic", "Advanced", "Expert" };
	private static final int[] CRAFTING_TIER_THRESHOLDS = { 100, 200, Integer.MAX_VALUE };
	private String selectedCraftingTier = null;
	private int selectedCraftingIndexInTier = 0;
	private float craftingRotationAngle = 0f;
	private boolean craftingDragging = false;
	private double craftingDragLastX = 0;
	private int craftingVisibleLayer = -1;
	private int craftingMaxLayer = 0;
	private int craftingSidebarScroll = 0;
	private int craftingInfoScroll = 0;

	// Shared nav button dimensions
	private static final int NAV_BTN_W = 24;
	private static final int NAV_BTN_H = 18;
	private static final int LAYER_BTN_SIZE = 16;

	// ────────────────────────────────────────────────────────────
	//  Construction / opening
	// ────────────────────────────────────────────────────────────

	public UnstainedProgressScreen() {
		super(Component.translatable("screen.hemomancy.unstained_progress"));
	}

	/** Call from the client side to open this screen. */
	public static void openScreen() {
		Minecraft.getInstance().setScreen(new UnstainedProgressScreen());
	}

	// ────────────────────────────────────────────────────────────
	//  Init
	// ────────────────────────────────────────────────────────────

	@Override
	protected void init() {
		super.init();
		RecipeLookup.clearCache();

		int margin = 16;
		guiLeft   = margin;
		guiTop    = margin;
		guiWidth  = width  - margin * 2;
		guiHeight = height - margin * 2;

		clearWidgets();
		cachePlayerData();
		buildContentBounds();
		buildMaterialLayout();
		cacheRiteRecipes();
		cacheCraftingRecipes();
		seedRhombusParams();

		// Centre each tab's content
		progressView.centreOn(contentW,    contentH,    guiWidth, guiHeight);
		materialView.centreOn(matContentW, matContentH, guiWidth, guiHeight);

		view = viewForTab(activeTab);
	}

	private void cachePlayerData() {
		if (Minecraft.getInstance().player != null) {
			Minecraft.getInstance().player.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA).ifPresent(cap -> {
				begunPurification    = cap.hasBegunPurification();
				purity               = cap.getPurity();
				clarityUnlocked      = cap.hasClarityUnlocked();
				clarity              = cap.getClarity();
				silverWardStrength   = cap.getSilverWardStrength();
				verdigrisAura        = cap.getVerdigrisAura();
				silverWardEnabled    = cap.isSilverWardEnabled();
				verdigrisAuraEnabled = cap.isVerdigrisAuraEnabled();
				currentPurityStage   = EnumPurityStage.byPurity(purity);
				currentClarityStage  = EnumClarityStage.byClarity(clarity);
				// Milestones
				mHemoKills          = cap.getHemoMobKills();
				mUndeadKills        = cap.getUndeadKills();
				mHostileKills       = cap.getHostileKills();
				mFlawlessKills      = cap.getFlawlessKills();
				mAnimalsBreed       = cap.getAnimalsBreed();
				mCropsPlanted       = cap.getCropsPlanted();
				mAdvancementsEarned = cap.getAdvancementsEarned();
				mNightsSlept        = cap.getNightsSlept();
				mPetsHealed         = cap.getPetsHealed();
				mSleptHemolysis     = cap.hasSleptWithHemolysis();
				mFirstHemoKill      = cap.hasKilledFirstHemoMob();
				mReachedAbstinence  = cap.hasReachedAbstinence();
				mEmptiedBlood       = cap.hasEmptiedBlood();
				mEarnedAdvancement  = cap.hasEarnedAdvancement();
			});
		}
	}

	private void buildContentBounds() {
		int stages = EnumPurityStage.values().length;
		int columnHeight = 50 + stages * NODE_GAP_Y + 30;
		int statAreaHeight = 80;

		if (clarityUnlocked) {
			contentW = COLUMN_SPACING + 160;
		} else {
			contentW = 200;
		}
		contentH = columnHeight + statAreaHeight;
	}

	private void seedRhombusParams() {
		Random rand = new Random(99L);
		rhombusParams = new float[RHOMBUS_COUNT][8];
		for (int i = 0; i < RHOMBUS_COUNT; i++) {
			rhombusParams[i][0] = rand.nextFloat();                             // startX ratio
			rhombusParams[i][1] = rand.nextFloat();                             // startY ratio
			rhombusParams[i][2] = 12 + rand.nextInt(24);                        // half-size (bigger)
			rhombusParams[i][3] = (rand.nextFloat() - 0.5f) * 10f;             // velX (pixels/sec)
			rhombusParams[i][4] = (rand.nextFloat() - 0.5f) * 8f;              // velY (pixels/sec)
			rhombusParams[i][5] = rand.nextFloat() * (float)(Math.PI * 2);     // phase offset
			rhombusParams[i][6] = 0.5f + rand.nextFloat() * 0.5f;              // brightness (higher base)
			rhombusParams[i][7] = (rand.nextFloat() - 0.5f) * 1.2f;            // rotation speed (rad/sec)
		}
	}

	// ────────────────────────────────────────────────────────────
	//  Coordinate helpers — delegate to active PanZoomState
	// ────────────────────────────────────────────────────────────

	/** Content-space → screen-space X */
	private int sx(int contentX) { return view.sx(guiLeft, contentX); }
	/** Content-space → screen-space Y */
	private int sy(int contentY) { return view.sy(guiTop,  contentY); }
	/** Half-node size on screen, accounting for zoom */
	private int halfNode() { return view.halfNode(NODE_SIZE); }

	private boolean insideGui(double mx, double my) {
		return mx >= guiLeft && mx < guiLeft + guiWidth
			&& my >= guiTop  && my < guiTop  + guiHeight;
	}

	// ────────────────────────────────────────────────────────────
	//  Pan / zoom helpers
	// ────────────────────────────────────────────────────────────

	private int contentW() {
		return switch (activeTab) {
			case PROGRESS  -> contentW;
			case MATERIALS -> matContentW;
			default        -> 0; // RITES / CRAFTING don't pan
		};
	}
	private int contentH() {
		return switch (activeTab) {
			case PROGRESS  -> contentH;
			case MATERIALS -> matContentH;
			default        -> 0;
		};
	}

	/** Returns the PanZoomState for a given tab (null-safe for browse tabs). */
	private PanZoomState viewForTab(UTab tab) {
		return switch (tab) {
			case PROGRESS  -> progressView;
			case MATERIALS -> materialView;
			default        -> view; // RITES / CRAFTING don't pan
		};
	}

	private void saveTabPan() {
		if (activeTab == UTab.RITES || activeTab == UTab.CRAFTING) return;
		view.clamp(contentW(), contentH(), guiWidth, guiHeight);
	}

	private void restoreTabPan() {
		view = viewForTab(activeTab);
	}

	private void switchTab(UTab tab) {
		if (tab == activeTab) return;
		saveTabPan();
		activeTab = tab;
		restoreTabPan();
	}

	private void clampPan() {
		view.clamp(contentW(), contentH(), guiWidth, guiHeight);
	}

	private void resetToHome() {
		view.centreOn(contentW(), contentH(), guiWidth, guiHeight);
	}

	// ────────────────────────────────────────────────────────────
	//  Input: drag-to-pan, scroll-to-zoom, home button
	// ────────────────────────────────────────────────────────────

	@Override
	public boolean mouseClicked(double mx, double my, int btn) {
		if (btn == 0) {
			// Home button (not on browse tabs)
			if (activeTab != UTab.RITES && activeTab != UTab.CRAFTING && isOverHomeButton(mx, my)) {
				resetToHome();
				return true;
			}
			// Tab clicks
			UTab clickedTab = tabUnder(mx, my);
			if (clickedTab != null) {
				switchTab(clickedTab);
				return true;
			}
			if (activeTab == UTab.PROGRESS) {
				if (begunPurification && isOverSidebarToggle(mx, my)) {
					sidebarVisible = !sidebarVisible;
					return true;
				}
				// Bonus toggle buttons
				if (begunPurification) {
					int clickedBonus = getBonusButtonAt(mx, my);
					if (clickedBonus >= 0) {
						PacketHandler.CHANNELBLOODVOLUME.sendToServer(new PacketToggleUnstainedBonus(clickedBonus));
						return true;
					}
				}
			}
			if (activeTab == UTab.MATERIALS && insideGui(mx, my)) {
				MaterialEntry matHit = materialNodeUnder(mx, my);
				if (matHit != null) {
					selectedMaterial = (selectedMaterial == matHit) ? null : matHit;
					return true;
				}
			}
			if (activeTab == UTab.RITES && insideGui(mx, my)) {
				// Check tier sidebar click
				CardinalRiteType clickedRiteTier = riteTierUnder(mx, my);
				if (clickedRiteTier != null) {
					if (!ritesByTier.getOrDefault(clickedRiteTier, List.of()).isEmpty()) {
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
				if (isOverLayerUpButton(mx, my, true)) {
					if (riteVisibleLayer == -1) riteVisibleLayer = riteMaxLayer;
					else if (riteVisibleLayer < riteMaxLayer) riteVisibleLayer++;
					else riteVisibleLayer = -1;
					return true;
				}
				if (isOverLayerDownButton(mx, my, true)) {
					if (riteVisibleLayer == -1) riteVisibleLayer = 0;
					else if (riteVisibleLayer > 0) riteVisibleLayer--;
					else riteVisibleLayer = -1;
					return true;
				}
				// Start rotation drag (only in the model area)
				if (mx >= guiLeft + TIER_SIDEBAR_W + 4) {
					riteDragging = true;
					riteDragLastX = mx;
				}
				return true;
			}
			if (activeTab == UTab.CRAFTING && insideGui(mx, my)) {
				// Check tier sidebar click
				String clickedTier = craftingTierUnder(mx, my);
				if (clickedTier != null) {
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
				if (isOverLayerUpButton(mx, my, false)) {
					if (craftingVisibleLayer == -1) craftingVisibleLayer = craftingMaxLayer;
					else if (craftingVisibleLayer < craftingMaxLayer) craftingVisibleLayer++;
					else craftingVisibleLayer = -1;
					return true;
				}
				if (isOverLayerDownButton(mx, my, false)) {
					if (craftingVisibleLayer == -1) craftingVisibleLayer = 0;
					else if (craftingVisibleLayer > 0) craftingVisibleLayer--;
					else craftingVisibleLayer = -1;
					return true;
				}
				// Start rotation drag
				if (mx >= guiLeft + TIER_SIDEBAR_W + 4) {
					craftingDragging = true;
					craftingDragLastX = mx;
				}
				return true;
			}
			if (insideGui(mx, my)) {
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
		if (craftingDragging && btn == 0 && activeTab == UTab.CRAFTING) {
			craftingRotationAngle += (float)(mx - craftingDragLastX) * 0.8f;
			craftingDragLastX = mx;
			return true;
		}
		if (riteDragging && btn == 0 && activeTab == UTab.RITES) {
			riteRotationAngle += (float)(mx - riteDragLastX) * 0.8f;
			riteDragLastX = mx;
			return true;
		}
		if (isDragging && btn == 0) {
			view.applyDrag(dx, dy);
			saveTabPan();
			return true;
		}
		return super.mouseDragged(mx, my, btn, dx, dy);
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double delta) {
		if (!insideGui(mx, my)) return super.mouseScrolled(mx, my, delta);

		// Rites & Crafting tabs — scroll the tier sidebar or info panel
		if (activeTab == UTab.RITES || activeTab == UTab.CRAFTING) {
			if (isOverTierSidebar(mx, my)) {
				int scrollAmt = (int)(-delta * 14);
				if (activeTab == UTab.RITES) {
					riteSidebarScroll = Math.max(0, riteSidebarScroll + scrollAmt);
					clampRiteSidebarScroll();
				} else {
					craftingSidebarScroll = Math.max(0, craftingSidebarScroll + scrollAmt);
					clampCraftingSidebarScroll();
				}
			} else {
				int scrollAmt = (int)(-delta * 14);
				if (activeTab == UTab.RITES) {
					riteInfoScroll = Math.max(0, riteInfoScroll + scrollAmt);
				} else {
					craftingInfoScroll = Math.max(0, craftingInfoScroll + scrollAmt);
				}
			}
			return true;
		}

		view.applyScroll(guiLeft, guiTop, mx, my, delta);
		saveTabPan();
		return true;
	}

	// ────────────────────────────────────────────────────────────
	//  Render
	// ────────────────────────────────────────────────────────────

	@Override
	public void render(@Nonnull GuiGraphics gfx, int mouseX, int mouseY, float partial) {
		renderBackground(gfx);

		cachePlayerData();
		buildContentBounds();

		// Auto-rotate rite/crafting model when not dragging
		if (activeTab == UTab.RITES && !riteDragging) {
			riteRotationAngle += partial * 0.4f;
		}
		if (activeTab == UTab.CRAFTING && !craftingDragging) {
			craftingRotationAngle += partial * 0.4f;
		}

		// 1. Dark blue background with floating hollow rhombuses
		renderDiamondBackground(gfx, guiLeft, guiTop, guiWidth, guiHeight);

		// 2. Border
		drawBorder(gfx, guiLeft, guiTop, guiWidth, guiHeight);

		// 3. Scissored content area
		gfx.enableScissor(guiLeft + 2, guiTop + 2,
				guiLeft + guiWidth - 2, guiTop + guiHeight - 2);

		if (activeTab == UTab.PROGRESS) {
			if (!begunPurification) {
				drawNotBegunMessage(gfx);
			} else {
				drawPurityColumn(gfx);
				if (clarityUnlocked) {
					drawClarityColumn(gfx);
				}
				drawStatReadouts(gfx);
			}
		} else if (activeTab == UTab.RITES) {
			drawUnstainedRiteContent(gfx, mouseX, mouseY, partial);
		} else if (activeTab == UTab.CRAFTING) {
			drawUnstainedCraftingContent(gfx, mouseX, mouseY, partial);
		} else if (activeTab == UTab.MATERIALS) {
			drawMaterialNodes(gfx);
		}

		gfx.disableScissor();

		// 3b. Material info panel (outside scissor so it renders on top of nodes)
		if (activeTab == UTab.MATERIALS && selectedMaterial != null) {
			drawMaterialInfoPanel(gfx, selectedMaterial);
		}

		// 4. Milestones sidebar (fixed on far left, outside content scissor; Progress tab only)
		if (activeTab == UTab.PROGRESS && begunPurification) {
			drawMilestones(gfx, mouseX, mouseY);
		}

		// 4b. Bonus toggle buttons (fixed on far right, outside content scissor; Progress tab only)
		if (activeTab == UTab.PROGRESS && begunPurification) {
			drawBonusToggleButtons(gfx, mouseX, mouseY);
		}

		// 5. Home button (outside scissor; not shown for browse tabs)
		if (activeTab != UTab.RITES && activeTab != UTab.CRAFTING) {
			drawHomeButton(gfx, mouseX, mouseY);
		}

		// 5b. Tabs
		drawTabs(gfx, mouseX, mouseY);

		// 5c. Title
		gfx.drawCenteredString(font,
				Component.literal(activeTab.label),
				guiLeft + guiWidth / 2, guiTop + 5, activeTab.color);

		// 6. Zoom indicator (hidden when milestones sidebar is open; not shown for browse tabs)
		if (activeTab != UTab.RITES && activeTab != UTab.CRAFTING
				&& (!sidebarVisible || !begunPurification || activeTab == UTab.MATERIALS)) {
			gfx.drawString(font,
					String.format("%.0f%%", view.zoom * 100),
					guiLeft + 5, guiTop + guiHeight - 12, 0x55888888, false);
		}

		// 7. Tooltips (outside scissor)
		if (activeTab == UTab.PROGRESS && begunPurification) {
			drawPurityTooltip(gfx, mouseX, mouseY);
			if (clarityUnlocked) {
				drawClarityTooltip(gfx, mouseX, mouseY);
			}
			drawStatTooltips(gfx, mouseX, mouseY);
			drawBonusToggleTooltips(gfx, mouseX, mouseY);
			// Sidebar toggle tooltip
			if (isOverSidebarToggle(mouseX, mouseY)) {
				String tipText = sidebarVisible ? "Hide Milestones" : "Show Milestones";
				gfx.renderTooltip(font, Component.literal(tipText), mouseX, mouseY);
			}
		} else if (activeTab == UTab.MATERIALS) {
			drawMaterialTooltip(gfx, mouseX, mouseY);
		}

		super.render(gfx, mouseX, mouseY, partial);
	}

	// ────────────────────────────────────────────────────────────
	//  Background (dark blue with floating hollow rhombuses)
	// ────────────────────────────────────────────────────────────

	private void renderDiamondBackground(GuiGraphics gfx, int gx, int gy, int gw, int gh) {
		gfx.enableScissor(gx, gy, gx + gw, gy + gh);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		// Layer 1: rich dark blue base
		gfx.fill(gx, gy, gx + gw, gy + gh, 0xFF060A1E);

		// Layer 2: dark-blue → blue-white radial gradient from the centre (subtle)
		int centerX = gx + gw / 2;
		int centerY = gy + gh / 2;
		int glowRadius = Math.max(gw, gh) / 2;
		for (int ring = glowRadius; ring > 0; ring -= 4) {
			float t = (float) ring / glowRadius;          // 1.0 at edge, 0.0 at centre
			float intensity = (1f - t) * (1f - t);        // quadratic falloff
			int alpha = (int)(50 * intensity);
			int r     = (int)(200 * intensity);
			int g     = (int)(210 * intensity);
			int b     = (int)(255 * intensity);
			int color = (alpha << 24) | (r << 16) | (g << 8) | b;
			gfx.fill(centerX - ring, centerY - ring, centerX + ring, centerY + ring, color);
		}

		// Layer 3: floating hollow rhombuses
		float time = System.nanoTime() / 1_000_000_000f;
		if (rhombusParams != null) {
			for (int i = 0; i < RHOMBUS_COUNT; i++) {
				drawFloatingRhombus(gfx, i, time, gx, gy, gw, gh);
			}
		}

		// Layer 4: subtle blue-tinted speckles (mirrors SkillTree's organic texture)
		Random speckRand = new Random(54321L);
		for (int s = 0; s < 120; s++) {
			int spx = gx + speckRand.nextInt(gw);
			int spy = gy + speckRand.nextInt(gh);
			int sb  = 10 + speckRand.nextInt(20);
			int sg  = speckRand.nextInt(8);
			int sa  = 15 + speckRand.nextInt(25);
			gfx.fill(spx, spy, spx + 1, spy + 1, (sa << 24) | (sg << 8) | sb);
		}

		RenderSystem.disableBlend();
		gfx.disableScissor();
	}

	/**
	 * Draws a single floating hollow rhombus particle that rotates over time.
	 * Each one drifts, wraps at edges, and spins around its own center.
	 * Bright white colour with thick walls.
	 */
	private void drawFloatingRhombus(GuiGraphics gfx, int index, float time,
									  int gx, int gy, int gw, int gh) {
		float[] p = rhombusParams[index];
		float startXRatio = p[0];
		float startYRatio = p[1];
		int   halfSize    = (int) p[2];
		float velX        = p[3];
		float velY        = p[4];
		float phase       = p[5];
		float brightness  = p[6];
		float rotSpeed    = p[7];

		// Position: drift over time, wrap around
		float rawX = startXRatio * gw + velX * time;
		float rawY = startYRatio * gh + velY * time;
		// Gentle wobble
		rawX += 5f * Mth.sin(time * 0.4f + phase);
		rawY += 4f * Mth.cos(time * 0.35f + phase * 1.3f);

		// Wrap around seamlessly
		int cx = gx + ((int) rawX % gw + gw) % gw;
		int cy = gy + ((int) rawY % gh + gh) % gh;

		// Rotation angle — each particle spins at its own speed
		float angle = phase + rotSpeed * time;

		// Pulsing alpha — brighter base
		float pulse = 0.6f + 0.4f * Mth.sin(time * 0.7f + phase);
		int baseAlpha = (int)(50 + 80 * brightness * pulse);

		// Colour: bright white with very slight cool tint
		int r = (int) Mth.clamp(180 + 75 * brightness, 0, 255);
		int g = (int) Mth.clamp(180 + 75 * brightness, 0, 255);
		int b = (int) Mth.clamp(200 + 55 * brightness, 0, 255);

		int color = (baseAlpha << 24) | (r << 16) | (g << 8) | b;

		// Thickness scales with size — thick walls
		int thickness = 3 + halfSize / 6;

		drawRotatedHollowRhombus(gfx, cx, cy, halfSize, thickness, angle, color);
	}

	/**
	 * Draws a hollow diamond/rhombus ring rotated by the given angle.
	 * Iterates over the bounding box, testing each pixel against
	 * the rotated diamond distance function (|u|+|v| in local frame).
	 * Pixels between outerRadius and innerRadius form the hollow ring.
	 */
	private void drawRotatedHollowRhombus(GuiGraphics gfx, int cx, int cy,
										   int halfSize, int thickness, float angle, int color) {
		float cosA = Mth.cos(angle);
		float sinA = Mth.sin(angle);
		int innerSize = halfSize - thickness;

		int bound = halfSize + 1;

		for (int dy = -bound; dy <= bound; dy++) {
			int spanStart = Integer.MIN_VALUE;

			for (int dx = -bound; dx <= bound; dx++) {
				// Rotate (dx, dy) into the diamond's local coordinate frame
				float u = dx * cosA + dy * sinA;
				float v = -dx * sinA + dy * cosA;

				// Diamond distance: |u| + |v|
				float dist = Math.abs(u) + Math.abs(v);

				boolean inRing = dist <= halfSize && (innerSize <= 0 || dist >= innerSize);

				if (inRing) {
					if (spanStart == Integer.MIN_VALUE) {
						spanStart = dx;
					}
				} else {
					// End of a span — flush it
					if (spanStart != Integer.MIN_VALUE) {
						gfx.fill(cx + spanStart, cy + dy, cx + dx, cy + dy + 1, color);
						spanStart = Integer.MIN_VALUE;
					}
				}
			}
			// Flush any remaining span at end of row
			if (spanStart != Integer.MIN_VALUE) {
				gfx.fill(cx + spanStart, cy + dy, cx + bound + 1, cy + dy + 1, color);
			}
		}
	}

	// ────────────────────────────────────────────────────────────
	//  "Not begun" message
	// ────────────────────────────────────────────────────────────

	private void drawNotBegunMessage(GuiGraphics gfx) {
		int centerX = guiLeft + guiWidth / 2;
		int centerY = guiTop + guiHeight / 2;

		gfx.drawCenteredString(font,
				Component.literal("The Unstained Path Has Not Yet Begun"),
				centerX, centerY - 16, 0xFF8098C0);
		gfx.drawCenteredString(font,
				Component.literal("Seek purification to awaken this mirror's vision."),
				centerX, centerY, 0xFF506080);
		gfx.drawCenteredString(font,
				Component.literal("Perform a Rite of Purification to begin."),
				centerX, centerY + 16, 0xFF384860);
	}

	// ────────────────────────────────────────────────────────────
	//  Content-space layout helpers
	// ────────────────────────────────────────────────────────────

	/** Content-space X centre for the purity column */
	private int purityCenterCX() {
		if (clarityUnlocked) {
			return contentW / 2 - COLUMN_SPACING / 2;
		}
		return contentW / 2;
	}

	/** Content-space X centre for the clarity column */
	private int clarityCenterCX() {
		return contentW / 2 + COLUMN_SPACING / 2;
	}

	/** Content-space Y for the top of the node area */
	private int nodeStartCY() {
		return 50;
	}

	// ────────────────────────────────────────────────────────────
	//  Purity column
	// ────────────────────────────────────────────────────────────

	private void drawPurityColumn(GuiGraphics gfx) {
		int ccx = purityCenterCX();
		int scrCenterX = sx(ccx);
		int startCY = nodeStartCY();

		// Column header
		if (view.zoom >= 0.4f) {
			gfx.drawCenteredString(font, Component.literal("— Purity —"),
					scrCenterX, sy(10), PURITY_COLOR);

			// Progress bar
			int barW = Math.max(20, (int)(100 * view.zoom));
			drawProgressBar(gfx, scrCenterX - barW / 2, sy(22), barW, Math.max(4, (int)(8 * view.zoom)),
					purity, 100f, 0x7088C0);

			gfx.drawCenteredString(font, String.format("%.1f%%", purity),
					scrCenterX, sy(34), 0xFF8098B0);
		}

		EnumPurityStage[] stages = EnumPurityStage.values();

		// Connections first — lines stop at diamond edges to avoid cutting through nodes
		int hn = halfNode();
		for (int i = 1; i < stages.length; i++) {
			int cy1 = startCY + (stages.length - 1 - (i - 1)) * NODE_GAP_Y;
			int cy2 = startCY + (stages.length - 1 - i) * NODE_GAP_Y;
			boolean reached = currentPurityStage.getLevel() >= stages[i].getLevel();
			int lineCol = reached ? COL_LINE_REACHED : COL_LINE_LOCKED;
			int lw = Math.max(1, (int)(view.zoom * 1.5f));
			// Diamond centers are at sy(cy)+hn, tips are at sy(cy) and sy(cy)+2*hn
			int upperBottom = sy(cy2) + 2 * hn; // bottom tip of upper diamond
			int lowerTop    = sy(cy1);           // top tip of lower diamond
			gfx.fill(sx(ccx) - lw, upperBottom, sx(ccx) + lw, lowerTop, lineCol);
		}

		// Nodes
		for (int i = 0; i < stages.length; i++) {
			EnumPurityStage stage = stages[i];
			int nodeCY = startCY + (stages.length - 1 - i) * NODE_GAP_Y;
			drawShapedNode(gfx, sx(ccx), sy(nodeCY) + halfNode(), stage.getTitle(),
					currentPurityStage.getLevel() >= stage.getLevel(),
					currentPurityStage == stage,
					PURITY_COLOR, PURITY_GLOW, stage.getNodeShape(),
					stage.getIconTexture(), stage.getIconItem());
		}
	}

	// ────────────────────────────────────────────────────────────
	//  Clarity column
	// ────────────────────────────────────────────────────────────

	private void drawClarityColumn(GuiGraphics gfx) {
		int ccx = clarityCenterCX();
		int scrCenterX = sx(ccx);
		int startCY = nodeStartCY();

		if (view.zoom >= 0.4f) {
			gfx.drawCenteredString(font, Component.literal("— Clarity —"),
					scrCenterX, sy(10), CLARITY_COLOR);

			int barW = Math.max(20, (int)(100 * view.zoom));
			drawProgressBar(gfx, scrCenterX - barW / 2, sy(22), barW, Math.max(4, (int)(8 * view.zoom)),
					clarity, 100f, 0x50B0A0);

			gfx.drawCenteredString(font, String.format("%.1f%%", clarity),
					scrCenterX, sy(34), 0xFF70B0A0);
		}

		EnumClarityStage[] stages = EnumClarityStage.values();

		// Connections — lines stop at diamond edges to avoid cutting through nodes
		int hn = halfNode();
		for (int i = 1; i < stages.length; i++) {
			int cy1 = startCY + (stages.length - 1 - (i - 1)) * NODE_GAP_Y;
			int cy2 = startCY + (stages.length - 1 - i) * NODE_GAP_Y;
			boolean reached = currentClarityStage.getLevel() >= stages[i].getLevel();
			int lineCol = reached ? 0xFF50A898 : COL_LINE_LOCKED;
			int lw = Math.max(1, (int)(view.zoom * 1.5f));
			// Diamond centers are at sy(cy)+hn, tips are at sy(cy) and sy(cy)+2*hn
			int upperBottom = sy(cy2) + 2 * hn; // bottom tip of upper diamond
			int lowerTop    = sy(cy1);           // top tip of lower diamond
			gfx.fill(sx(ccx) - lw, upperBottom, sx(ccx) + lw, lowerTop, lineCol);
		}

		// Nodes
		for (int i = 0; i < stages.length; i++) {
			EnumClarityStage stage = stages[i];
			int nodeCY = startCY + (stages.length - 1 - i) * NODE_GAP_Y;
			drawShapedNode(gfx, sx(ccx), sy(nodeCY) + halfNode(), stage.getTitle(),
					currentClarityStage.getLevel() >= stage.getLevel(),
					currentClarityStage == stage,
					CLARITY_COLOR, CLARITY_GLOW, stage.getNodeShape(),
					stage.getIconTexture(), stage.getIconItem());
		}
	}

	// ────────────────────────────────────────────────────────────
	//  Shape-aware stage node
	// ────────────────────────────────────────────────────────────

	private void drawShapedNode(GuiGraphics gfx, int scrX, int scrY, String title,
								 boolean reached, boolean isCurrent, int accentColor, int glowColor,
								 EnumNodeShape shape,
								 @javax.annotation.Nullable ResourceLocation iconTexture,
								 @javax.annotation.Nullable ItemStack iconStack) {
		float time = System.nanoTime() / 1_000_000_000f;
		int hn = halfNode();

		// Determine border colour
		int border;
		if (isCurrent) {
			border = COL_NODE_CURRENT;
			// Pulsing glow behind the node
			float p = 0.5f + 0.5f * Mth.sin(time * 2.5f);
			int ga = (int)(40 * p);
			int gr = (glowColor >> 16) & 0xFF;
			int gg = (glowColor >> 8) & 0xFF;
			int gb = glowColor & 0xFF;
			NodeShapeRenderer.drawFill(gfx, shape, scrX, scrY, hn + 4,
					(ga << 24) | (gr << 16) | (gg << 8) | gb);
		} else if (reached) {
			border = COL_NODE_REACHED;
		} else {
			border = COL_NODE_LOCKED;
		}

		// Node fill
		NodeShapeRenderer.drawFill(gfx, shape, scrX, scrY, hn, COL_NODE_BG);

		// Border outline
		NodeShapeRenderer.drawOutline(gfx, shape, scrX, scrY, hn, border);

		// Icon texture, icon item, or text inside node (only when zoomed in enough)
		if (view.zoom >= 0.5f) {
			if (iconTexture != null) {
				ScreenDrawUtils.renderScaledTexture(gfx, iconTexture, scrX, scrY, hn);
			} else if (iconStack != null && !iconStack.isEmpty()) {
				ScreenDrawUtils.renderScaledItem(gfx, iconStack, scrX, scrY, hn);
			} else {
				String initial = !title.isEmpty() ? title.substring(0, 1).toUpperCase() : "?";
				int textCol = isCurrent ? 0xFFFFFFFF : (reached ? 0xFFA0B8D8 : 0xFF404858);
				gfx.drawCenteredString(font, initial, scrX, scrY - 4, textCol);
			}
		}

		// Stage name below node
		if (view.zoom >= 0.4f) {
			int labelCol = isCurrent ? accentColor : (reached ? 0xFF7090B0 : 0xFF384050);
			gfx.drawCenteredString(font, title, scrX, scrY + hn + 4, labelCol);
		}
	}

	// ────────────────────────────────────────────────────────────
	//  Progress bar — delegate to shared utility — delegate to shared utility
	// ────────────────────────────────────────────────────────────

	private void drawProgressBar(GuiGraphics gfx, int x, int y, int w, int h,
								 float value, float max, int fillColorRGB) {
		ScreenDrawUtils.drawProgressBar(gfx, x, y, w, h, value, max, fillColorRGB);
	}

	// ────────────────────────────────────────────────────────────
	//  Stat readouts (below the nodes, in content space)
	// ────────────────────────────────────────────────────────────

	private void drawStatReadouts(GuiGraphics gfx) {
		EnumPurityStage[] stages = EnumPurityStage.values();
		int statCY = nodeStartCY() + stages.length * NODE_GAP_Y + 20;
		int centerScrX = sx(contentW / 2);
		int scrY = sy(statCY);

		// Divider line
		int divW = Math.max(30, (int)(200 * view.zoom));
		gfx.fill(centerScrX - divW / 2, scrY - 4, centerScrX + divW / 2, scrY - 3, 0x33405878);

		if (view.zoom >= 0.4f) {
			String penaltyStr = String.format("Blood Magic Penalty: -%.0f%%",
					currentPurityStage.getBloodMagicPenalty() * 100);
			int penaltyCol = currentPurityStage.getBloodMagicPenalty() > 0 ? 0xFFCC8060 : 0xFF405060;
			gfx.drawCenteredString(font, Component.literal(penaltyStr), centerScrX, scrY, penaltyCol);
		}
	}

	/**
	 * Draws hover tooltips for the stat readout lines (Blood Magic Penalty).
	 * Recomputes the same screen positions used by drawStatReadouts.
	 */
	private void drawStatTooltips(GuiGraphics gfx, int mouseX, int mouseY) {
		if (!insideGui(mouseX, mouseY) || view.zoom < 0.4f) return;

		EnumPurityStage[] stages = EnumPurityStage.values();
		int statCY = nodeStartCY() + stages.length * NODE_GAP_Y + 20;
		int centerScrX = sx(contentW / 2);
		int scrY = sy(statCY);
		int halfTextW = (int)(100 * view.zoom);  // approximate half-width of text hit area

		// Blood Magic Penalty
		if (mouseX >= centerScrX - halfTextW && mouseX <= centerScrX + halfTextW
				&& mouseY >= scrY - 1 && mouseY <= scrY + 9) {
			List<Component> tip = new ArrayList<>();
			tip.add(Component.literal("Blood Magic Penalty")
					.withStyle(s -> s.withColor(0xCC8060).withBold(true)));
			tip.add(Component.literal("Pursuing the Unstained path weakens your")
					.withStyle(s -> s.withColor(0xA08070)));
			tip.add(Component.literal("blood manipulations. Higher purity stages")
					.withStyle(s -> s.withColor(0xA08070)));
			tip.add(Component.literal("inflict a greater cost and damage reduction")
					.withStyle(s -> s.withColor(0xA08070)));
			tip.add(Component.literal("to all hemomancy abilities.")
					.withStyle(s -> s.withColor(0xA08070)));
			tip.add(Component.literal("")); // spacer
		tip.add(Component.literal("Tainted: -10%  •  Cleansing: -25%")
				.withStyle(s -> s.withColor(0x807060).withItalic(true)));
		tip.add(Component.literal("Absolved: -50%  •  Purified: -100%")
					.withStyle(s -> s.withColor(0x807060).withItalic(true)));
			float penalty = currentPurityStage.getBloodMagicPenalty() * 100;
			tip.add(Component.literal(String.format("Current: -%.0f%%", penalty))
					.withStyle(s -> s.withColor(penalty > 0 ? 0xCC6644 : 0x607060).withItalic(true)));
			gfx.renderTooltip(font, tip, Optional.empty(), mouseX, mouseY);
		}
	}

	// ────────────────────────────────────────────────────────────
	//  Bonus Toggle Buttons (fixed on the right side of the GUI)
	// ────────────────────────────────────────────────────────────

	/** Returns the screen-space X for the top-left corner of the bonus panel. */
	private int bonusPanelX() {
		return guiLeft + guiWidth - BONUS_PANEL_PAD - BONUS_BTN_SIZE;
	}

	/** Returns the screen-space Y for the top-left of the first bonus button. */
	private int bonusPanelStartY() {
		return guiTop + 24;
	}

	/** Returns the total number of visible bonus buttons (based on progress). */
	private int bonusButtonCount() {
		int count = 1; // Silver Ward is always shown once purification has begun
		if (clarityUnlocked) count++; // Verdigris Aura
		return count;
	}

	/**
	 * Hit-test: returns the bonus button index (0-based) at the given mouse position,
	 * or -1 if no button is under the cursor.
	 * 0 = Silver Ward, 1 = Verdigris Aura.
	 * Only returns indices for unlocked bonuses.
	 */
	private int getBonusButtonAt(double mx, double my) {
		int px = bonusPanelX();
		int py = bonusPanelStartY();
		int count = bonusButtonCount();
		for (int i = 0; i < count; i++) {
			int by = py + i * (BONUS_BTN_SIZE + BONUS_BTN_GAP);
			if (mx >= px && mx <= px + BONUS_BTN_SIZE && my >= by && my <= by + BONUS_BTN_SIZE) {
				return i;
			}
		}
		return -1;
	}

	/** Draws the bonus toggle buttons on the right side of the GUI. */
	private void drawBonusToggleButtons(GuiGraphics gfx, int mouseX, int mouseY) {
		int px = bonusPanelX();
		int py = bonusPanelStartY();

		// Silver Ward button (always visible once purification begun)
		drawBonusButton(gfx, px, py, "W", silverWardEnabled,
				purity > 0, PURITY_COLOR, PURITY_GLOW,
				getBonusButtonAt(mouseX, mouseY) == 0);

		// Verdigris Aura button (only if clarity unlocked)
		if (clarityUnlocked) {
			int y2 = py + BONUS_BTN_SIZE + BONUS_BTN_GAP;
			drawBonusButton(gfx, px, y2, "A", verdigrisAuraEnabled,
					true, CLARITY_COLOR, CLARITY_GLOW,
					getBonusButtonAt(mouseX, mouseY) == 1);
		}
	}

	/**
	 * Draws a single bonus toggle button as a diamond-themed icon.
	 * @param icon      single-character label inside the button
	 * @param enabled   whether the bonus is currently toggled ON
	 * @param unlocked  whether the player has progressed enough to use this bonus
	 * @param accent    accent colour when active
	 * @param glow      glow colour for pulse when active
	 * @param hovered   whether the mouse is over this button
	 */
	private void drawBonusButton(GuiGraphics gfx, int bx, int by, String icon,
								  boolean enabled, boolean unlocked, int accent, int glow, boolean hovered) {
		int bs = BONUS_BTN_SIZE;
		int cx = bx + bs / 2;
		int cy = by + bs / 2;
		int halfDia = bs / 2 - 2;

		// Background fill diamond
		int bgCol;
		if (!unlocked) {
			bgCol = 0xCC0C1020;
		} else if (enabled) {
			bgCol = hovered ? 0xDD101830 : 0xCC0C1020;
		} else {
			bgCol = hovered ? 0xDD0C0C16 : 0xCC080A14;
		}
		NodeShapeRenderer.drawFill(gfx, EnumNodeShape.DIAMOND, cx, cy, halfDia, bgCol);

		// Border
		int borderCol;
		if (!unlocked) {
			borderCol = COL_NODE_LOCKED;
		} else if (enabled) {
			borderCol = hovered ? accent : COL_NODE_REACHED;
		} else {
			borderCol = hovered ? 0xFF505868 : 0xFF383E4C;
		}
		NodeShapeRenderer.drawOutline(gfx, EnumNodeShape.DIAMOND, cx, cy, halfDia, borderCol);

		// Pulsing glow when enabled and unlocked
		if (enabled && unlocked) {
			float time = System.nanoTime() / 1_000_000_000f;
			float p = 0.3f + 0.3f * Mth.sin(time * 2.0f);
			int ga = (int)(30 * p);
			int gr = (glow >> 16) & 0xFF;
			int gg = (glow >> 8) & 0xFF;
			int gb = glow & 0xFF;
			NodeShapeRenderer.drawFill(gfx, EnumNodeShape.DIAMOND, cx, cy, halfDia + 2,
					(ga << 24) | (gr << 16) | (gg << 8) | gb);
		}

		// Icon letter
		int iconCol;
		if (!unlocked) {
			iconCol = 0xFF303840;
		} else if (enabled) {
			iconCol = hovered ? 0xFFFFFFFF : accent;
		} else {
			iconCol = hovered ? 0xFF707880 : 0xFF505868;
		}
		gfx.drawCenteredString(font, icon, cx, cy - 4, iconCol);

		// Small toggle indicator dot below icon
		if (unlocked) {
			int dotCol = enabled ? 0xFF60CC60 : 0xFF804040;
			gfx.fill(cx - 1, cy + 4, cx + 2, cy + 6, dotCol);
		}
	}

	/** Draws hover tooltips for the bonus toggle buttons. */
	private void drawBonusToggleTooltips(GuiGraphics gfx, int mouseX, int mouseY) {
		int hovered = getBonusButtonAt(mouseX, mouseY);
		if (hovered < 0) return;

		List<Component> tip = new ArrayList<>();

		if (hovered == 0) {
			// Silver Ward tooltip
			boolean unlocked = purity > 0;
			tip.add(Component.literal("Silver Ward")
					.withStyle(s -> s.withColor(0xB0C0E0).withBold(true)));
			if (!unlocked) {
				tip.add(Component.literal("Locked — increase Purity to unlock.")
						.withStyle(s -> s.withColor(0x606870).withItalic(true)));
			} else {
				tip.add(Component.literal("Passive resistance to blood magic effects.")
						.withStyle(s -> s.withColor(0x8898B0)));
				tip.add(Component.literal("Grants +4 Armor and +0.2 Knockback Resistance")
						.withStyle(s -> s.withColor(0x8898B0)));
				tip.add(Component.literal("while the Silver Ward effect is active.")
						.withStyle(s -> s.withColor(0x8898B0)));
				tip.add(Component.literal(""));
			tip.add(Component.literal("Scales with Purity (purity ÷ 100).")
					.withStyle(s -> s.withColor(0x607090).withItalic(true)));
			tip.add(Component.literal(String.format("Current: %.0f%%", silverWardStrength * 100))
					.withStyle(s -> s.withColor(0x60A0CC).withItalic(true)));
			tip.add(Component.literal(""));
			String state = silverWardEnabled ? "✔ Enabled" : "✖ Disabled";
				int stateCol = silverWardEnabled ? 0x60CC60 : 0xCC6060;
				tip.add(Component.literal(state + " — click to toggle")
						.withStyle(s -> s.withColor(stateCol)));
			}
		} else if (hovered == 1) {
			// Verdigris Aura tooltip
			tip.add(Component.literal("Verdigris Aura")
					.withStyle(s -> s.withColor(0x80D0C0).withBold(true)));
			tip.add(Component.literal("A copper-based anti-blood field that")
					.withStyle(s -> s.withColor(0x70A898)));
			tip.add(Component.literal("weakens nearby blood magic entities and effects.")
					.withStyle(s -> s.withColor(0x70A898)));
			tip.add(Component.literal(""));
		tip.add(Component.literal("Scales with Clarity (clarity ÷ 100).")
				.withStyle(s -> s.withColor(0x508878).withItalic(true)));
		tip.add(Component.literal(String.format("Current: %.0f%%", verdigrisAura * 100))
				.withStyle(s -> s.withColor(0x50B0A0).withItalic(true)));
		tip.add(Component.literal(""));
		String state = verdigrisAuraEnabled ? "✔ Enabled" : "✖ Disabled";
			int stateCol = verdigrisAuraEnabled ? 0x60CC60 : 0xCC6060;
			tip.add(Component.literal(state + " — click to toggle")
					.withStyle(s -> s.withColor(stateCol)));
		}

		if (!tip.isEmpty()) {
			gfx.renderTooltip(font, tip, Optional.empty(), mouseX, mouseY);
		}
	}

	// ────────────────────────────────────────────────────────────
	//  Milestones — checklist (one-time) + progress bars (repeatable)
	// ────────────────────────────────────────────────────────────

	private void drawMilestones(GuiGraphics gfx, int mouseX, int mouseY) {
		int sideX = guiLeft + 4;
		int sideY = guiTop + 22;
		boolean tabHovered = isOverSidebarToggle(mouseX, mouseY);

		if (!sidebarVisible) {
			// ── Collapsed: draw only the toggle tab ──
			drawSidebarToggleTab(gfx, sideX, sideY, false, tabHovered);
			return;
		}

		// ── Expanded sidebar ──
		int sideW = SIDEBAR_WIDTH;
		int sideH = guiHeight - 28;

		// Semi-transparent panel background
		gfx.fill(sideX, sideY, sideX + sideW, sideY + sideH, 0xCC0C1020);

		// Panel border
		int borderCol = 0xFF203050;
		gfx.fill(sideX, sideY, sideX + sideW, sideY + 1, borderCol);
		gfx.fill(sideX, sideY + sideH - 1, sideX + sideW, sideY + sideH, borderCol);
		gfx.fill(sideX, sideY, sideX + 1, sideY + sideH, borderCol);
		gfx.fill(sideX + sideW - 1, sideY, sideX + sideW, sideY + sideH, borderCol);

		// Toggle tab on the right edge of the panel (to collapse)
		drawSidebarToggleTab(gfx, sideX + sideW, sideY, true, tabHovered);

		// Scissor to the sidebar panel
		gfx.enableScissor(sideX + 1, sideY + 1, sideX + sideW - 1, sideY + sideH - 1);

		int x = sideX + 6;
		int y = sideY + 6;
		int centerX = sideX + sideW / 2;

		// Section title
		gfx.drawCenteredString(font, Component.literal("Milestones"), centerX, y, 0xFFB0C0E0);
		y += 14;

		// Thin divider
		gfx.fill(sideX + 8, y, sideX + sideW - 8, y + 1, 0x33607890);
		y += 5;

		// ── One-time checklist ──
		y = drawCheckItem(gfx, x, y, "First Hemo Kill", mFirstHemoKill);
		y = drawCheckItem(gfx, x, y, "Slept w/ Hemolysis", mSleptHemolysis);
		y = drawCheckItem(gfx, x, y, "5-min Abstinence", mReachedAbstinence);
		y = drawCheckItem(gfx, x, y, "Emptied Blood", mEmptiedBlood);
		y = drawCheckItem(gfx, x, y, "Earned Advancement", mEarnedAdvancement);

		y += 4;
		gfx.fill(sideX + 8, y, sideX + sideW - 8, y + 1, 0x33607890);
		y += 6;

		// ── Progress bars ──
		gfx.drawCenteredString(font, Component.literal("Progress"), centerX, y, 0xFF8898B0);
		y += 12;

		int barW = sideW - 16;
		int barH = 6;
		int barX = sideX + 8;

		y = drawMilestoneBar(gfx, x, barX, y, barW, barH,
				"Hemo Mobs Slain", mHemoKills, 25, 0xFFB06080);
		y = drawMilestoneBar(gfx, x, barX, y, barW, barH,
				"Undead Vanquished", mUndeadKills, 50, 0xFF8090CC);
		y = drawMilestoneBar(gfx, x, barX, y, barW, barH,
				"Hostiles Purged", mHostileKills, 100, 0xFF7088A0);
		y = drawMilestoneBar(gfx, x, barX, y, barW, barH,
				"Flawless Kills", mFlawlessKills, 20, 0xFFD0D0FF);
		y = drawMilestoneBar(gfx, x, barX, y, barW, barH,
				"Animals Bred", mAnimalsBreed, 20, 0xFF80C080);
		y = drawMilestoneBar(gfx, x, barX, y, barW, barH,
				"Crops Planted", mCropsPlanted, 50, 0xFF60B060);
		y = drawMilestoneBar(gfx, x, barX, y, barW, barH,
				"Advancements", mAdvancementsEarned, 10, 0xFFD0B060);
		y = drawMilestoneBar(gfx, x, barX, y, barW, barH,
				"Nights Slept", mNightsSlept, 10, 0xFF6080B0);
		drawMilestoneBar(gfx, x, barX, y, barW, barH,
				"Pets Healed", mPetsHealed, 15, 0xFFA0D0B0);

		gfx.disableScissor();
	}

	/**
	 * Draws the sidebar toggle tab — a small clickable arrow on the edge.
	 * When expanded: tab is on the right edge of the panel, arrow points left (collapse).
	 * When collapsed: tab is on the left edge of the GUI, arrow points right (expand).
	 */
	public void drawSidebarToggleTab(GuiGraphics gfx, int tabX, int tabY, boolean expanded, boolean hovered) {
		ScreenDrawUtils.drawSidebarToggleTab(gfx, font,
				tabX, tabY, SIDEBAR_TAB_W, SIDEBAR_TAB_H,
				expanded, hovered,
				0xDD101828, 0xCC0C1020,  // hoverBg, idleBg
				0xFF4070A0, 0xFF203050,  // hoverBc, idleBc
				0xFFB0C8E8, 0xFF8098C0); // hoverArrow, idleArrow
	}

	/** Hit test for the sidebar toggle tab. */
	private boolean isOverSidebarToggle(double mx, double my) {
		int tabY = guiTop + 22;
		int tabX;
		if (sidebarVisible) {
			// Tab is on the right edge of the expanded sidebar
			tabX = guiLeft + 4 + SIDEBAR_WIDTH;
		} else {
			// Tab is on the left edge when collapsed
			tabX = guiLeft + 4;
		}
		return mx >= tabX && mx <= tabX + SIDEBAR_TAB_W
			&& my >= tabY && my <= tabY + SIDEBAR_TAB_H;
	}

	/** Draws a single checklist item with a check/cross icon. Returns the next Y. */
	private int drawCheckItem(GuiGraphics gfx, int x, int y, String label, boolean complete) {
		String icon = complete ? "✓" : "✗";
		int iconCol = complete ? 0xFF60CC60 : 0xFF605060;
		int labelCol = complete ? 0xFFA0C0D0 : 0xFF506070;

		gfx.drawString(font, icon, x, y, iconCol, false);
		gfx.drawString(font, " " + label, x + 10, y, labelCol, false);
		return y + 12;
	}

	/** Draws a labeled milestone progress bar. Returns the next Y. */
	private int drawMilestoneBar(GuiGraphics gfx, int labelX, int barX, int y,
								 int barW, int barH, String label, int current, int goal, int fillColor) {
		// Label + count on the same line
		gfx.drawString(font, label, labelX, y, 0xFF8898B0, false);
		String countStr = current >= goal ? (current + "/" + goal + " ✓") : (current + "/" + goal);
		int countCol = current >= goal ? 0xFF60CC60 : 0xFF6080A0;
		int countW = font.width(countStr);
		gfx.drawString(font, countStr, barX + barW - countW, y, countCol, false);
		y += 10;

		// Bar background
		gfx.fill(barX, y, barX + barW, y + barH, 0xFF0C1020);

		// Bar frame
		int frameCol = 0xFF203040;
		gfx.fill(barX, y, barX + barW, y + 1, frameCol);
		gfx.fill(barX, y + barH - 1, barX + barW, y + barH, frameCol);
		gfx.fill(barX, y, barX + 1, y + barH, frameCol);
		gfx.fill(barX + barW - 1, y, barX + barW, y + barH, frameCol);

		// Bar fill
		float ratio = Math.min(1.0f, (float) current / goal);
		int fillW = (int) (barW * ratio);
		if (fillW > 0) {
			gfx.fill(barX + 1, y + 1, barX + 1 + fillW, y + barH - 1, fillColor);
		}

		// Completion glow
		if (current >= goal) {
			float time = System.nanoTime() / 1_000_000_000f;
			float pulse = 0.3f + 0.2f * Mth.sin(time * 2.0f);
			int ga = (int) (pulse * 255);
			gfx.fill(barX, y, barX + barW, y + barH, (ga << 24) | 0x60CC60);
		}

		return y + barH + 6;
	}

	// ────────────────────────────────────────────────────────────
	//  Tooltips
	// ────────────────────────────────────────────────────────────

	private void drawPurityTooltip(GuiGraphics gfx, int mouseX, int mouseY) {
		if (!insideGui(mouseX, mouseY)) return;
		EnumPurityStage[] stages = EnumPurityStage.values();
		int ccx = purityCenterCX();
		int startCY = nodeStartCY();
		int hn = halfNode();

		for (int i = 0; i < stages.length; i++) {
			EnumPurityStage stage = stages[i];
			int nodeCY = startCY + (stages.length - 1 - i) * NODE_GAP_Y;
			int nodeScrX = sx(ccx);
			int nodeScrY = sy(nodeCY) + hn;

			if (NodeShapeRenderer.isInside(stage.getNodeShape(), mouseX, mouseY, nodeScrX, nodeScrY, hn)) {
				List<Component> tip = new ArrayList<>();
				boolean reached = currentPurityStage.getLevel() >= stage.getLevel();
				boolean isCurrent = currentPurityStage == stage;

				tip.add(Component.literal(stage.getTitle())
						.withStyle(s -> s.withColor(isCurrent ? 0xB0C0E0 : (reached ? 0x7090B0 : 0x404858)).withBold(true)));

				tip.add(Component.literal(String.format("Requires: %.0f%% Purity", stage.getMinPurity()))
						.withStyle(s -> s.withColor(0x888888)));

				if (stage.getBloodMagicPenalty() > 0) {
					tip.add(Component.literal(String.format("Blood Magic Penalty: -%.0f%%", stage.getBloodMagicPenalty() * 100))
							.withStyle(s -> s.withColor(0xAA6644)));
				}

			if (isCurrent) {
				tip.add(Component.literal("▸ Current Stage")
						.withStyle(s -> s.withColor(0x60A0CC).withItalic(true)));
			} else if (reached) {
				tip.add(Component.literal("✓ Achieved")
						.withStyle(s -> s.withColor(0x44AA44).withItalic(true)));
			} else {
				float needed = stage.getMinPurity() - purity;
					tip.add(Component.literal(String.format("%.1f%% more purity needed", needed))
							.withStyle(s -> s.withColor(0xAA4444).withItalic(true)));
				}

				tip.add(Component.translatable("unstained.hemomancy.purity." + stage.name().toLowerCase() + ".desc")
						.withStyle(s -> s.withColor(0x6878A0).withItalic(true)));

				gfx.renderTooltip(font, tip, Optional.empty(), mouseX, mouseY);
				return;
			}
		}
	}

	private void drawClarityTooltip(GuiGraphics gfx, int mouseX, int mouseY) {
		if (!insideGui(mouseX, mouseY)) return;
		EnumClarityStage[] stages = EnumClarityStage.values();
		int ccx = clarityCenterCX();
		int startCY = nodeStartCY();
		int hn = halfNode();

		for (int i = 0; i < stages.length; i++) {
			EnumClarityStage stage = stages[i];
			int nodeCY = startCY + (stages.length - 1 - i) * NODE_GAP_Y;
			int nodeScrX = sx(ccx);
			int nodeScrY = sy(nodeCY) + hn;

			if (NodeShapeRenderer.isInside(stage.getNodeShape(), mouseX, mouseY, nodeScrX, nodeScrY, hn)) {
				List<Component> tip = new ArrayList<>();
				boolean reached = currentClarityStage.getLevel() >= stage.getLevel();
				boolean isCurrent = currentClarityStage == stage;

				tip.add(Component.literal(stage.getTitle())
						.withStyle(s -> s.withColor(isCurrent ? 0x80D0C0 : (reached ? 0x508878 : 0x384848)).withBold(true)));

				tip.add(Component.literal(String.format("Requires: %.0f%% Clarity", stage.getMinClarity()))
						.withStyle(s -> s.withColor(0x888888)));

			if (isCurrent) {
				tip.add(Component.literal("▸ Current Stage")
						.withStyle(s -> s.withColor(0x50B0A0).withItalic(true)));
			} else if (reached) {
				tip.add(Component.literal("✓ Achieved")
						.withStyle(s -> s.withColor(0x44AA44).withItalic(true)));
			} else {
				float needed = stage.getMinClarity() - clarity;
					tip.add(Component.literal(String.format("%.1f%% more clarity needed", needed))
							.withStyle(s -> s.withColor(0xAA4444).withItalic(true)));
				}

				tip.add(Component.translatable("unstained.hemomancy.clarity." + stage.name().toLowerCase() + ".desc")
						.withStyle(s -> s.withColor(0x508878).withItalic(true)));

				gfx.renderTooltip(font, tip, Optional.empty(), mouseX, mouseY);
				return;
			}
		}
	}

	// ────────────────────────────────────────────────────────────
	//  Home button (top-left corner of GUI)
	// ────────────────────────────────────────────────────────────

	private void drawHomeButton(GuiGraphics gfx, int mouseX, int mouseY) {
		int bx = guiLeft + HOME_BTN_PAD;
		int by = guiTop + HOME_BTN_PAD;
		boolean hovered = isOverHomeButton(mouseX, mouseY);

		ScreenDrawUtils.drawHomeButton(gfx, font, bx, by, HOME_BTN_SIZE, hovered,
				0xDD101828, 0x990C1020,   // hoverBg, idleBg
				0xFF6088C0, 0xFF304060,   // hoverBorder, idleBorder
				0xFFB0C0E0, 0xFF506888);  // hoverText, idleText

		if (hovered) {
			gfx.renderTooltip(font, Component.literal("Return to Center"), mouseX, mouseY);
		}
	}

	private boolean isOverHomeButton(double mx, double my) {
		int bx = guiLeft + HOME_BTN_PAD;
		int by = guiTop + HOME_BTN_PAD;
		return mx >= bx && mx <= bx + HOME_BTN_SIZE
			&& my >= by && my <= by + HOME_BTN_SIZE;
	}

	// ────────────────────────────────────────────────────────────
	//  Border (dark blue variant)
	// ────────────────────────────────────────────────────────────

	private void drawBorder(GuiGraphics gfx, int x, int y, int w, int h) {
		ScreenDrawUtils.drawBorder(gfx, x, y, w, h, COL_BORDER_OUTER, COL_BORDER_INNER);
	}

	// ────────────────────────────────────────────────────────────
	//  Tab buttons (top-right)
	// ────────────────────────────────────────────────────────────

	// ────────────────────────────────────────────────────────────
	//  Tab buttons (top-right) — delegate to ScreenDrawUtils
	// ────────────────────────────────────────────────────────────

	private List<ScreenDrawUtils.TabDesc> buildTabDescs() {
		List<ScreenDrawUtils.TabDesc> descs = new ArrayList<>();
		for (UTab tab : UTab.values()) {
			descs.add(new ScreenDrawUtils.TabDesc(tab.label, tab.color, tab == activeTab));
		}
		return descs;
	}

	private void drawTabs(GuiGraphics gfx, int mouseX, int mouseY) {
		ScreenDrawUtils.drawTabs(gfx, font, buildTabDescs(),
				guiLeft, guiTop, guiWidth, TAB_HEIGHT, TAB_PAD, mouseX, mouseY);
	}

	private UTab tabUnder(double mx, double my) {
		int idx = ScreenDrawUtils.tabIndexUnder(font, buildTabDescs(),
				guiLeft, guiTop, guiWidth, TAB_HEIGHT, TAB_PAD, mx, my);
		return idx >= 0 ? UTab.values()[idx] : null;
	}

	// ────────────────────────────────────────────────────────────
	//  Materials & Processes tab — delegate to MaterialsTabView
	// ────────────────────────────────────────────────────────────

	private void buildMaterialLayout() {
		int[] bounds = new int[2];
		MaterialsTabView.buildLayout(MaterialsData.getUnstainedEntries(),
				materialPositions, bounds, NODE_SIZE);
		matContentW = bounds[0];
		matContentH = bounds[1];
	}

	private void drawMaterialNodes(GuiGraphics gfx) {
		MaterialsTabView.drawNodes(gfx, font,
				MaterialsData.getUnstainedEntries(), materialPositions,
				view, guiLeft, guiTop, NODE_SIZE, EnumNodeShape.DIAMOND,
				UTab.MATERIALS.color, selectedMaterial,
				0x0080B0A0, 0xFF6088B0);
	}

	private void drawMaterialInfoPanel(GuiGraphics gfx, MaterialEntry mat) {
		MaterialsTabView.drawInfoPanel(gfx, font, mat,
				guiLeft, guiTop, guiWidth,
				UTab.MATERIALS.color, 0xFF203050, 0xDD101828,
				MiniRecipeRenderer.UNSTAINED);
	}

	private void drawMaterialTooltip(GuiGraphics gfx, int mouseX, int mouseY) {
		MaterialsTabView.drawTooltip(gfx, font, materialPositions,
				view, guiLeft, guiTop, guiWidth, guiHeight, NODE_SIZE,
				EnumNodeShape.DIAMOND, UTab.MATERIALS.color, 0xFF80B0A0,
				mouseX, mouseY);
	}

	private MaterialEntry materialNodeUnder(double mx, double my) {
		return MaterialsTabView.nodeUnder(materialPositions,
				view, guiLeft, guiTop, NODE_SIZE, EnumNodeShape.DIAMOND, mx, my);
	}

	// ────────────────────────────────────────────────────────────
	//  Unstained Rites & Crafting — recipe caching
	// ────────────────────────────────────────────────────────────

	private void cacheRiteRecipes() {
		riteRecipes.clear();
		ritesByTier.clear();
		if (Minecraft.getInstance().player != null && Minecraft.getInstance().level != null) {
			for (CardinalRiteRecipe r : CardinalRiteRecipe.getAllRecipes(Minecraft.getInstance().level)) {
				if (r.isUnstained()) {
					riteRecipes.add(r);
				}
			}
		}
		for (CardinalRiteType type : CardinalRiteType.values()) {
			ritesByTier.put(type, new ArrayList<>());
		}
		for (CardinalRiteRecipe recipe : riteRecipes) {
			ritesByTier.get(recipe.getRiteType()).add(recipe);
		}
		for (List<CardinalRiteRecipe> tierList : ritesByTier.values()) {
			tierList.sort(java.util.Comparator.comparingDouble(CardinalRiteRecipe::getBloodCost));
		}
		selectedRiteTier = null;
		selectedRiteIndexInTier = 0;
		for (CardinalRiteType type : CardinalRiteType.values()) {
			if (!ritesByTier.getOrDefault(type, List.of()).isEmpty()) {
				selectedRiteTier = type;
				break;
			}
		}
	}

	private void cacheCraftingRecipes() {
		craftingRecipes.clear();
		craftingByTier.clear();
		if (Minecraft.getInstance().player != null && Minecraft.getInstance().level != null) {
			for (BloodStructureRecipe r : BloodStructureRecipe.getAllRecipes(Minecraft.getInstance().level)) {
				if (r.isUnstained()) {
					craftingRecipes.add(r);
				}
			}
		}
		for (String tierName : CRAFTING_TIER_NAMES) {
			craftingByTier.put(tierName, new ArrayList<>());
		}
		for (BloodStructureRecipe recipe : craftingRecipes) {
			for (int i = 0; i < CRAFTING_TIER_THRESHOLDS.length; i++) {
				if (recipe.getBloodCost() <= CRAFTING_TIER_THRESHOLDS[i]) {
					craftingByTier.get(CRAFTING_TIER_NAMES[i]).add(recipe);
					break;
				}
			}
		}
		selectedCraftingTier = null;
		selectedCraftingIndexInTier = 0;
		for (String tierName : CRAFTING_TIER_NAMES) {
			if (!craftingByTier.getOrDefault(tierName, List.of()).isEmpty()) {
				selectedCraftingTier = tierName;
				break;
			}
		}
	}

	// ────────────────────────────────────────────────────────────
	//  Unstained Rites tab — tier-based layout
	// ────────────────────────────────────────────────────────────

	private void drawUnstainedRiteContent(GuiGraphics gfx, int mouseX, int mouseY, float partial) {
		if (riteRecipes.isEmpty()) {
			gfx.drawCenteredString(font, "No Unstained Rites found",
					guiLeft + guiWidth / 2, guiTop + guiHeight / 2, 0xFF666666);
			return;
		}

		int contentX = guiLeft + TIER_SIDEBAR_W + 6;
		int cw = guiWidth - TIER_SIDEBAR_W - 10;

		if (selectedRiteTier == null) {
			gfx.pose().pushPose();
			gfx.pose().translate(0, 0, 400);
			drawRiteTierSidebar(gfx, mouseX, mouseY);
			gfx.drawCenteredString(font, "Select a tier",
					contentX + cw / 2, guiTop + guiHeight / 2, 0xFF555555);
			gfx.pose().popPose();
			return;
		}

		List<CardinalRiteRecipe> tierRites = ritesByTier.getOrDefault(selectedRiteTier, List.of());
		if (tierRites.isEmpty()) {
			gfx.pose().pushPose();
			gfx.pose().translate(0, 0, 400);
			drawRiteTierSidebar(gfx, mouseX, mouseY);
			gfx.drawCenteredString(font, "No rites in this tier",
					contentX + cw / 2, guiTop + guiHeight / 2, 0xFF555555);
			gfx.pose().popPose();
			return;
		}
		if (selectedRiteIndexInTier >= tierRites.size()) selectedRiteIndexInTier = 0;
		CardinalRiteRecipe rite = tierRites.get(selectedRiteIndexInTier);

		int modelAreaW = cw / 2;
		int modelX = contentX;
		int infoX = contentX + modelAreaW + 10;
		int infoW = cw - modelAreaW - 20;

		// 3D multiblock preview
		drawRiteModel(gfx, rite, modelX + 10, guiTop + 30, modelAreaW - 20, guiHeight - 60, partial);

		gfx.pose().pushPose();
		gfx.pose().translate(0, 0, 400);

		drawRiteTierSidebar(gfx, mouseX, mouseY);
		drawLayerButtons(gfx, mouseX, mouseY, true, riteVisibleLayer, riteMaxLayer);
		drawRiteInfoPanel(gfx, rite, infoX, guiTop + 30, infoW, mouseX, mouseY);

		gfx.drawCenteredString(font, "Drag to rotate",
				modelX + modelAreaW / 2, guiTop + guiHeight - 18, 0x44888888);

		gfx.pose().popPose();
	}

	private void drawRiteTierSidebar(GuiGraphics gfx, int mouseX, int mouseY) {
		int sx = guiLeft + 4;
		int sy = guiTop + 24;
		int sw = TIER_SIDEBAR_W - 8;
		int rowH = 22;

		gfx.drawString(font, Component.literal("Rite Tiers")
				.withStyle(s -> s.withColor(UTab.RITES.color).withBold(true)), sx + 2, sy, 0);
		sy += 14;

		gfx.fill(sx, sy, sx + sw, sy + 1, 0xFF203050);
		sy += 4;

		int clipTop = sy;
		int clipBottom = guiTop + guiHeight - 4;
		gfx.enableScissor(sx, clipTop, sx + sw, clipBottom);

		sy -= riteSidebarScroll;

		for (CardinalRiteType type : CardinalRiteType.values()) {
			boolean selected = (type == selectedRiteTier);
			List<CardinalRiteRecipe> recipes = ritesByTier.getOrDefault(type, List.of());
			if (recipes.isEmpty()) {
				sy += rowH + 2;
				continue;
			}

			boolean hovered = mouseX >= sx && mouseX <= sx + sw
					&& mouseY >= sy && mouseY <= sy + rowH
					&& mouseY >= clipTop && mouseY <= clipBottom;

			int bg = selected ? 0xDD101828 : (hovered ? 0xBB0C1420 : 0x990A0E18);
			gfx.fill(sx, sy, sx + sw, sy + rowH, bg);

			int bc = selected ? UTab.RITES.color : 0xFF555555;
			gfx.fill(sx, sy, sx + sw, sy + 1, bc);
			gfx.fill(sx, sy + rowH - 1, sx + sw, sy + rowH, bc);
			gfx.fill(sx, sy, sx + 1, sy + rowH, bc);
			gfx.fill(sx + sw - 1, sy, sx + sw, sy + rowH, bc);

			String sizeLabel = type.getSize() + "x" + type.getSize();
			String tierLabel = HLTextUtils.toProperCase(type.getSerializedName());
			int textCol = selected ? 0xFFB0C0E0 : 0xFF999999;
			gfx.drawString(font, tierLabel + " " + sizeLabel + " (" + recipes.size() + ")", sx + 4, sy + (rowH - 8) / 2, textCol, false);

			if (selected) {
				sy += rowH + 2;
				for (int j = 0; j < recipes.size(); j++) {
					CardinalRiteRecipe r = recipes.get(j);
					boolean recSel = (j == selectedRiteIndexInTier);
					boolean recHov = mouseX >= sx + 4 && mouseX <= sx + sw - 4
							&& mouseY >= sy && mouseY <= sy + 16
							&& mouseY >= clipTop && mouseY <= clipBottom;

					int recBg = recSel ? 0xCC0E1420 : (recHov ? 0xAA0C1020 : 0x00000000);
					gfx.fill(sx + 2, sy, sx + sw - 2, sy + 16, recBg);

					if (recSel) {
						gfx.fill(sx + 2, sy, sx + 3, sy + 16, UTab.RITES.color);
					}

					String recName = r.getRiteName();
					if (recName == null || recName.isEmpty()) {
						String ritePath = r.getId().getPath();
						if (ritePath.contains("/")) ritePath = ritePath.substring(ritePath.lastIndexOf('/') + 1);
						recName = HLTextUtils.toProperCase(ritePath.replace("_", " "));
					}
					recName = truncateText(recName, sw - 16);
					int recCol = recSel ? 0xFFB0C0E0 : 0xFF888888;
					gfx.drawString(font, recName, sx + 8, sy + 4, recCol, false);
					sy += 18;
				}
			}
			sy += rowH + 2;
		}

		gfx.disableScissor();

		int contentH = riteSidebarContentH();
		int visibleH = tierSidebarVisibleH();
		if (contentH > visibleH) {
			if (riteSidebarScroll > 0) {
				gfx.drawCenteredString(font, "\u25B2", sx + sw / 2, clipTop, 0xAAFFFFFF);
			}
			if (riteSidebarScroll < contentH - visibleH) {
				gfx.drawCenteredString(font, "\u25BC", sx + sw / 2, clipBottom - 10, 0xAAFFFFFF);
			}
		}
	}

	private CardinalRiteType riteTierUnder(double mx, double my) {
		int sx = guiLeft + 4;
		int sy = guiTop + 24 + 14 + 4;
		int sw = TIER_SIDEBAR_W - 8;
		int rowH = 22;

		int clipTop = sy;
		int clipBottom = guiTop + guiHeight - 4;
		if (my < clipTop || my > clipBottom) return null;

		sy -= riteSidebarScroll;

		for (CardinalRiteType type : CardinalRiteType.values()) {
			boolean selected = (type == selectedRiteTier);
			List<CardinalRiteRecipe> recipes = ritesByTier.getOrDefault(type, List.of());
			if (recipes.isEmpty()) {
				sy += rowH + 2;
				continue;
			}

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

		sy -= riteSidebarScroll;

		for (CardinalRiteType type : CardinalRiteType.values()) {
			boolean selected = (type == selectedRiteTier);
			List<CardinalRiteRecipe> tierRecipes = ritesByTier.getOrDefault(type, List.of());
			if (tierRecipes.isEmpty()) {
				sy += rowH + 2;
				continue;
			}

			sy += rowH + 2;

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

	private void drawRiteModel(GuiGraphics gfx, CardinalRiteRecipe rite,
							   int areaX, int areaY, int areaW, int areaH, float partial) {
		if (rite.getPattern() == null) return;

		List<BlockPosBlockPair> blockPairs = rite.getPattern().getBlockPosBlockList();
		if (blockPairs.isEmpty()) return;

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
		if (minX > maxX) return;

		riteMaxLayer = maxY - minY;

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
		pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(30));
		pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(riteRotationAngle));

		float offX = -(minX + sizeX / 2f);
		float offY = -(minY + sizeY / 2f);
		float offZ = -(minZ + sizeZ / 2f);

		MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

		for (BlockPosBlockPair pair : blockPairs) {
			Block block = pair.getBlock();
			if (block == null || block == Blocks.AIR) continue;
			BlockPos pos = pair.getPos();

			int relativeY = pos.getY() - minY;
			if (riteVisibleLayer >= 0 && relativeY > riteVisibleLayer) continue;

			pose.pushPose();
			pose.translate(pos.getX() + offX, pos.getY() + offY, pos.getZ() + offZ);

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

	private void drawRiteInfoPanel(GuiGraphics gfx, CardinalRiteRecipe rite,
								   int panelX, int panelY, int panelW, int mouseX, int mouseY) {
		int clipTop = panelY;
		int clipBottom = guiTop + guiHeight - 8;
		int visibleH = clipBottom - clipTop;

		int totalH = measureRiteInfoPanelHeight(rite, panelW);
		int maxScroll = Math.max(0, totalH - visibleH);
		if (riteInfoScroll > maxScroll) riteInfoScroll = maxScroll;

		gfx.enableScissor(panelX - 2, clipTop, panelX + panelW + 2, clipBottom);

		int y = panelY - riteInfoScroll;
		int lineH = 12;

		// Rite name
		String name = rite.getRiteName();
		if (name == null || name.isEmpty()) {
			String ritePath = rite.getId().getPath();
			if (ritePath.contains("/")) ritePath = ritePath.substring(ritePath.lastIndexOf('/') + 1);
			name = HLTextUtils.toProperCase(ritePath.replace("_", " "));
		}
		for (String titleLine : ScreenDrawUtils.wrapText(font, name, panelW)) {
			gfx.drawString(font, Component.literal(titleLine)
					.withStyle(s -> s.withColor(PURITY_COLOR).withBold(true)), panelX, y, 0);
			y += lineH;
		}
		y += 4;

		gfx.fill(panelX, y, panelX + panelW, y + 1, 0xFF203050);
		y += 6;

		// Description
		String desc = rite.getRiteDescription();
		if (desc != null && !desc.isEmpty()) {
			List<String> lines = ScreenDrawUtils.wrapText(font, desc, panelW);
			for (String line : lines) {
				gfx.drawString(font, Component.literal(line)
						.withStyle(s -> s.withColor(0x999999).withItalic(true)), panelX, y, 0);
				y += lineH;
			}
			y += 4;
		}

		// Rite type
		CardinalRiteType type = rite.getRiteType();
		String typeStr = HLTextUtils.toProperCase(type.getSerializedName()) + " (" + type.getSize() + "x" + type.getSize() + ")";
		gfx.drawString(font, Component.literal("Type: ").withStyle(s -> s.withColor(0x888888))
				.append(Component.literal(typeStr).withStyle(s -> s.withColor(PURITY_COLOR))), panelX, y, 0);
		y += lineH;

		// Cast time
		int ticks = type.getCastingDurationTicks();
		float seconds = ticks / 20f;
		gfx.drawString(font, Component.literal("Cast Time: ").withStyle(s -> s.withColor(0x888888))
				.append(Component.literal(String.format("%.1fs", seconds)).withStyle(s -> s.withColor(0xAAAA88))), panelX, y, 0);
		y += lineH + 6;

		// Result item
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

		// Block materials list
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

		if (totalH > visibleH) {
			if (riteInfoScroll > 0) {
				gfx.drawCenteredString(font, "\u25B2", panelX + panelW / 2, clipTop, 0xAAFFFFFF);
			}
			if (riteInfoScroll < maxScroll) {
				gfx.drawCenteredString(font, "\u25BC", panelX + panelW / 2, clipBottom - 10, 0xAAFFFFFF);
			}
		}
	}

	private int measureRiteInfoPanelHeight(CardinalRiteRecipe rite, int panelW) {
		int y = 0;
		int lineH = 12;

		String name = rite.getRiteName();
		if (name == null || name.isEmpty()) {
			String ritePath = rite.getId().getPath();
			if (ritePath.contains("/")) ritePath = ritePath.substring(ritePath.lastIndexOf('/') + 1);
			name = HLTextUtils.toProperCase(ritePath.replace("_", " "));
		}
		y += ScreenDrawUtils.wrapText(font, name, panelW).size() * lineH;
		y += 4 + 1 + 6;

		String desc = rite.getRiteDescription();
		if (desc != null && !desc.isEmpty()) {
			y += ScreenDrawUtils.wrapText(font, desc, panelW).size() * lineH + 4;
		}

		y += lineH; // type
		y += lineH + 6; // cast time + gap

		ItemStack result = rite.getResult();
		if (result != null && !result.isEmpty()) {
			y += lineH;
			List<String> resultLines = ScreenDrawUtils.wrapText(font, result.getHoverName().getString(), panelW - 20);
			y += Math.max(20, resultLines.size() * lineH + 4);
		}

		y += 6;

		if (rite.getPattern() != null) {
			Map<Block, Integer> blockCounts = rite.getPattern().getBlockCount(false);
			if (!blockCounts.isEmpty()) {
				y += lineH;
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

	// ────────────────────────────────────────────────────────────
	//  Unstained Crafting tab — tier-based layout
	// ────────────────────────────────────────────────────────────

	private void drawUnstainedCraftingContent(GuiGraphics gfx, int mouseX, int mouseY, float partial) {
		if (craftingRecipes.isEmpty()) {
			gfx.drawCenteredString(font, "No Unstained Crafting recipes found",
					guiLeft + guiWidth / 2, guiTop + guiHeight / 2, 0xFF666666);
			return;
		}

		int contentX = guiLeft + TIER_SIDEBAR_W + 6;
		int cw = guiWidth - TIER_SIDEBAR_W - 10;

		if (selectedCraftingTier == null) {
			gfx.pose().pushPose();
			gfx.pose().translate(0, 0, 400);
			drawCraftingTierSidebar(gfx, mouseX, mouseY);
			gfx.drawCenteredString(font, "Select a tier",
					contentX + cw / 2, guiTop + guiHeight / 2, 0xFF555555);
			gfx.pose().popPose();
			return;
		}

		List<BloodStructureRecipe> tierRecipes = craftingByTier.getOrDefault(selectedCraftingTier, List.of());
		if (tierRecipes.isEmpty()) {
			gfx.pose().pushPose();
			gfx.pose().translate(0, 0, 400);
			drawCraftingTierSidebar(gfx, mouseX, mouseY);
			gfx.drawCenteredString(font, "No recipes in this tier",
					contentX + cw / 2, guiTop + guiHeight / 2, 0xFF555555);
			gfx.pose().popPose();
			return;
		}
		if (selectedCraftingIndexInTier >= tierRecipes.size()) selectedCraftingIndexInTier = 0;
		BloodStructureRecipe recipe = tierRecipes.get(selectedCraftingIndexInTier);

		int modelAreaW = cw / 2;
		int modelX = contentX;
		int infoX = contentX + modelAreaW + 10;
		int infoW = cw - modelAreaW - 20;

		drawCraftingModel(gfx, recipe, modelX + 10, guiTop + 30, modelAreaW - 20, guiHeight - 60);

		gfx.pose().pushPose();
		gfx.pose().translate(0, 0, 400);

		drawCraftingTierSidebar(gfx, mouseX, mouseY);
		drawLayerButtons(gfx, mouseX, mouseY, false, craftingVisibleLayer, craftingMaxLayer);
		drawCraftingInfoPanel(gfx, recipe, infoX, guiTop + 30, infoW);

		gfx.drawCenteredString(font, "Drag to rotate",
				modelX + modelAreaW / 2, guiTop + guiHeight - 18, 0x44888888);

		gfx.pose().popPose();
	}

	private void drawCraftingTierSidebar(GuiGraphics gfx, int mouseX, int mouseY) {
		int sx = guiLeft + 4;
		int sy = guiTop + 24;
		int sw = TIER_SIDEBAR_W - 8;
		int rowH = 22;

		gfx.drawString(font, Component.literal("Tiers")
				.withStyle(s -> s.withColor(UTab.CRAFTING.color).withBold(true)), sx + 2, sy, 0);
		sy += 14;

		gfx.fill(sx, sy, sx + sw, sy + 1, 0xFF203050);
		sy += 4;

		int clipTop = sy;
		int clipBottom = guiTop + guiHeight - 4;
		gfx.enableScissor(sx, clipTop, sx + sw, clipBottom);

		sy -= craftingSidebarScroll;

		for (int i = 0; i < CRAFTING_TIER_NAMES.length; i++) {
			String tierName = CRAFTING_TIER_NAMES[i];
			boolean selected = tierName.equals(selectedCraftingTier);
			List<BloodStructureRecipe> recipes = craftingByTier.getOrDefault(tierName, List.of());

			boolean hovered = mouseX >= sx && mouseX <= sx + sw
					&& mouseY >= sy && mouseY <= sy + rowH
					&& mouseY >= clipTop && mouseY <= clipBottom;

			int bg = selected ? 0xDD101828 : (hovered ? 0xBB0C1420 : 0x990A0E18);
			gfx.fill(sx, sy, sx + sw, sy + rowH, bg);

			int bc = selected ? UTab.CRAFTING.color : 0xFF555555;
			gfx.fill(sx, sy, sx + sw, sy + 1, bc);
			gfx.fill(sx, sy + rowH - 1, sx + sw, sy + rowH, bc);
			gfx.fill(sx, sy, sx + 1, sy + rowH, bc);
			gfx.fill(sx + sw - 1, sy, sx + sw, sy + rowH, bc);

			String label = tierName + " (" + recipes.size() + ")";
			int textCol = selected ? 0xFF80D0C0 : 0xFF999999;
			gfx.drawString(font, label, sx + 4, sy + (rowH - 8) / 2, textCol, false);

			if (selected) {
				sy += rowH + 2;
				for (int j = 0; j < recipes.size(); j++) {
					BloodStructureRecipe r = recipes.get(j);
					boolean recSel = (j == selectedCraftingIndexInTier);
					boolean recHov = mouseX >= sx + 4 && mouseX <= sx + sw - 4
							&& mouseY >= sy && mouseY <= sy + 16
							&& mouseY >= clipTop && mouseY <= clipBottom;

					int recBg = recSel ? 0xCC0E1420 : (recHov ? 0xAA0C1020 : 0x00000000);
					gfx.fill(sx + 2, sy, sx + sw - 2, sy + 16, recBg);

					if (recSel) {
						gfx.fill(sx + 2, sy, sx + 3, sy + 16, UTab.CRAFTING.color);
					}

					String recPath = r.getId().getPath();
					if (recPath.contains("/")) recPath = recPath.substring(recPath.lastIndexOf('/') + 1);
					String recName = HLTextUtils.toProperCase(recPath.replace("_", " "));
					int recCol = recSel ? 0xFF80D0C0 : 0xFF888888;
					gfx.drawString(font, recName, sx + 8, sy + 4, recCol, false);
					sy += 18;
				}
			}
			sy += rowH + 2;
		}

		gfx.disableScissor();

		int cH = craftingSidebarContentH();
		int vH = tierSidebarVisibleH();
		if (cH > vH) {
			if (craftingSidebarScroll > 0) {
				gfx.drawCenteredString(font, "\u25B2", sx + sw / 2, clipTop, 0xAAFFFFFF);
			}
			if (craftingSidebarScroll < cH - vH) {
				gfx.drawCenteredString(font, "\u25BC", sx + sw / 2, clipBottom - 10, 0xAAFFFFFF);
			}
		}
	}

	private String craftingTierUnder(double mx, double my) {
		int sx = guiLeft + 4;
		int sy = guiTop + 24 + 14 + 4;
		int sw = TIER_SIDEBAR_W - 8;
		int rowH = 22;

		int clipTop = sy;
		int clipBottom = guiTop + guiHeight - 4;
		if (my < clipTop || my > clipBottom) return null;

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

		sy -= craftingSidebarScroll;

		for (int i = 0; i < CRAFTING_TIER_NAMES.length; i++) {
			String tierName = CRAFTING_TIER_NAMES[i];
			boolean selected = tierName.equals(selectedCraftingTier);
			List<BloodStructureRecipe> tierRecipes = craftingByTier.getOrDefault(tierName, List.of());

			sy += rowH + 2;

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

	private void drawCraftingModel(GuiGraphics gfx, BloodStructureRecipe recipe,
								   int areaX, int areaY, int areaW, int areaH) {
		if (recipe.getPattern() == null) return;

		List<BlockPosBlockPair> blockPairs = recipe.getPattern().getBlockPosBlockList();
		if (blockPairs.isEmpty()) return;

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
		if (minX > maxX) return;

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
		pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(30));
		pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(craftingRotationAngle));

		float offX = -(minX + sizeX / 2f);
		float offY = -(minY + sizeY / 2f);
		float offZ = -(minZ + sizeZ / 2f);

		MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

		for (BlockPosBlockPair pair : blockPairs) {
			Block block = pair.getBlock();
			if (block == null || block == Blocks.AIR) continue;
			BlockPos pos = pair.getPos();

			int relativeY = pos.getY() - minY;
			if (craftingVisibleLayer >= 0 && relativeY > craftingVisibleLayer) continue;

			pose.pushPose();
			pose.translate(pos.getX() + offX, pos.getY() + offY, pos.getZ() + offZ);

			boolean dimmed = craftingVisibleLayer >= 0 && relativeY < craftingVisibleLayer;

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

	private void drawCraftingInfoPanel(GuiGraphics gfx, BloodStructureRecipe recipe,
									   int panelX, int panelY, int panelW) {
		int clipTop = panelY;
		int clipBottom = guiTop + guiHeight - 8;
		int visibleH = clipBottom - clipTop;

		int totalH = measureCraftingInfoPanelHeight(recipe, panelW);
		int maxScroll = Math.max(0, totalH - visibleH);
		if (craftingInfoScroll > maxScroll) craftingInfoScroll = maxScroll;

		gfx.enableScissor(panelX - 2, clipTop, panelX + panelW + 2, clipBottom);

		int y = panelY - craftingInfoScroll;
		int lineH = 12;

		// Recipe name
		String namePath = recipe.getId().getPath();
		if (namePath.contains("/")) namePath = namePath.substring(namePath.lastIndexOf('/') + 1);
		String name = HLTextUtils.toProperCase(namePath.replace("_", " "));
		for (String titleLine : ScreenDrawUtils.wrapText(font, name, panelW)) {
			gfx.drawString(font, Component.literal(titleLine)
					.withStyle(s -> s.withColor(CLARITY_COLOR).withBold(true)), panelX, y, 0);
			y += lineH;
		}
		y += 4;

		gfx.fill(panelX, y, panelX + panelW, y + 1, 0xFF203050);
		y += 6;

		// Blood cost
		gfx.drawString(font, Component.literal("Blood Cost: ").withStyle(s -> s.withColor(0x888888))
				.append(Component.literal((int) recipe.getBloodCost() + " mL").withStyle(s -> s.withColor(0xAA4444))), panelX, y, 0);
		y += lineH + 4;

		// Held item
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

		// Hit block
		Block hitBlock = recipe.getHitBlock();
		if (hitBlock != null && hitBlock != Blocks.AIR) {
			gfx.drawString(font, Component.literal("Activate on:").withStyle(s -> s.withColor(0x888888)), panelX, y, 0);
			y += lineH;

			ItemStack hitStack = new ItemStack(hitBlock);
			if (!hitStack.isEmpty()) {
				gfx.renderItem(hitStack, panelX, y);
				List<String> hitLines = ScreenDrawUtils.wrapText(font, hitStack.getHoverName().getString(), panelW - 20);
				for (int li = 0; li < hitLines.size(); li++) {
					int ix = li == 0 ? panelX + 20 : panelX + 4;
					gfx.drawString(font, Component.literal(hitLines.get(li))
							.withStyle(s -> s.withColor(0xDDDDDD)), ix, y + 4 + li * lineH, 0);
				}
				y += Math.max(20, hitLines.size() * lineH + 4);
			}
		}

		y += 4;

		// Result item
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

		// Block materials list
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

		if (totalH > visibleH) {
			if (craftingInfoScroll > 0) {
				gfx.drawCenteredString(font, "\u25B2", panelX + panelW / 2, clipTop, 0xAAFFFFFF);
			}
			if (craftingInfoScroll < maxScroll) {
				gfx.drawCenteredString(font, "\u25BC", panelX + panelW / 2, clipBottom - 10, 0xAAFFFFFF);
			}
		}
	}

	private int measureCraftingInfoPanelHeight(BloodStructureRecipe recipe, int panelW) {
		int y = 0;
		int lineH = 12;

		String namePath = recipe.getId().getPath();
		if (namePath.contains("/")) namePath = namePath.substring(namePath.lastIndexOf('/') + 1);
		String name = HLTextUtils.toProperCase(namePath.replace("_", " "));
		y += ScreenDrawUtils.wrapText(font, name, panelW).size() * lineH;
		y += 4 + 1 + 6;

		y += lineH + 4; // blood cost

		ItemStack heldItem = recipe.getHeldItem();
		if (heldItem != null && !heldItem.isEmpty()) {
			y += lineH;
			List<String> heldLines = ScreenDrawUtils.wrapText(font, heldItem.getHoverName().getString(), panelW - 20);
			y += Math.max(20, heldLines.size() * lineH + 4);
		}

		Block hitBlock = recipe.getHitBlock();
		if (hitBlock != null && hitBlock != Blocks.AIR) {
			y += lineH;
			ItemStack hitStack = new ItemStack(hitBlock);
			if (!hitStack.isEmpty()) {
				List<String> hitLines = ScreenDrawUtils.wrapText(font, hitStack.getHoverName().getString(), panelW - 20);
				y += Math.max(20, hitLines.size() * lineH + 4);
			}
		}

		y += 4;

		ItemStack result = recipe.getResult();
		if (result != null && !result.isEmpty()) {
			y += lineH;
			List<String> resultLines = ScreenDrawUtils.wrapText(font, result.getHoverName().getString(), panelW - 20);
			y += Math.max(20, resultLines.size() * lineH + 4);
		}

		y += 6;

		if (recipe.getPattern() != null) {
			Map<Block, Integer> blockCounts = recipe.getPattern().getBlockCount(false);
			if (!blockCounts.isEmpty()) {
				y += lineH;
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

	// ────────────────────────────────────────────────────────────
	//  Shared rite/crafting helpers
	// ────────────────────────────────────────────────────────────

	private void drawNavButton(GuiGraphics gfx, int x, int y, int w, int h, String symbol, boolean hovered, int hoverColor) {
		int bg = hovered ? 0xDD101828 : 0x990C1020;
		gfx.fill(x, y, x + w, y + h, bg);

		int bc = hovered ? hoverColor : 0xFF444444;
		gfx.fill(x, y, x + w, y + 1, bc);
		gfx.fill(x, y + h - 1, x + w, y + h, bc);
		gfx.fill(x, y, x + 1, y + h, bc);
		gfx.fill(x + w - 1, y, x + w, y + h, bc);

		int textCol = hovered ? 0xFFB0C0E0 : 0xFF888888;
		gfx.drawCenteredString(font, symbol, x + w / 2, y + (h - 8) / 2, textCol);
	}

	private int layerBtnX() { return guiLeft + TIER_SIDEBAR_W + 10; }
	private int layerBtnCenterY() { return guiTop + guiHeight / 2; }

	/**
	 * Draws ▲ (layer up) and ▼ (layer down) buttons and a layer indicator.
	 * @param isRites true for rites tab, false for crafting tab
	 */
	private void drawLayerButtons(GuiGraphics gfx, int mouseX, int mouseY,
								  boolean isRites, int visibleLayer, int maxLayer) {
		if (maxLayer <= 0) return;

		int bx = layerBtnX();
		int cy = layerBtnCenterY();
		int bs = LAYER_BTN_SIZE;
		int color = isRites ? UTab.RITES.color : UTab.CRAFTING.color;

		int upY = cy - bs - 14;
		boolean upHov = isOverLayerUpButton(mouseX, mouseY, isRites);
		drawNavButton(gfx, bx, upY, bs, bs, "\u25B2", upHov, color);

		int downY = cy + 14;
		boolean downHov = isOverLayerDownButton(mouseX, mouseY, isRites);
		drawNavButton(gfx, bx, downY, bs, bs, "\u25BC", downHov, color);

		String label = visibleLayer < 0 ? "All" : "Y:" + (visibleLayer + 1);
		gfx.drawCenteredString(font, label, bx + bs / 2, cy - 4, 0xFFAAAAAA);

		if (upHov) {
			gfx.renderTooltip(font, Component.literal("Layer Up"), mouseX, mouseY);
		} else if (downHov) {
			gfx.renderTooltip(font, Component.literal("Layer Down"), mouseX, mouseY);
		}
	}

	private boolean isOverLayerUpButton(double mx, double my, boolean isRites) {
		int maxL = isRites ? riteMaxLayer : craftingMaxLayer;
		if (maxL <= 0) return false;
		int bx = layerBtnX();
		int cy = layerBtnCenterY();
		int upY = cy - LAYER_BTN_SIZE - 14;
		return mx >= bx && mx <= bx + LAYER_BTN_SIZE
			&& my >= upY && my <= upY + LAYER_BTN_SIZE;
	}

	private boolean isOverLayerDownButton(double mx, double my, boolean isRites) {
		int maxL = isRites ? riteMaxLayer : craftingMaxLayer;
		if (maxL <= 0) return false;
		int bx = layerBtnX();
		int cy = layerBtnCenterY();
		int downY = cy + 14;
		return mx >= bx && mx <= bx + LAYER_BTN_SIZE
			&& my >= downY && my <= downY + LAYER_BTN_SIZE;
	}

	private boolean isOverTierSidebar(double mx, double my) {
		return mx >= guiLeft && mx <= guiLeft + TIER_SIDEBAR_W
			&& my >= guiTop && my <= guiTop + guiHeight;
	}

	private int tierSidebarVisibleH() {
		return guiHeight - 42 - 4;
	}

	private int riteSidebarContentH() {
		int rowH = 22;
		int total = 0;
		for (CardinalRiteType type : CardinalRiteType.values()) {
			List<CardinalRiteRecipe> recipes = ritesByTier.getOrDefault(type, List.of());
			if (recipes.isEmpty()) continue;
			total += rowH + 2;
			if (type == selectedRiteTier) {
				total += rowH + 2 + recipes.size() * 18;
			}
		}
		return total;
	}

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

	private void clampRiteSidebarScroll() {
		int maxScroll = Math.max(0, riteSidebarContentH() - tierSidebarVisibleH());
		riteSidebarScroll = Math.min(riteSidebarScroll, maxScroll);
	}

	private void clampCraftingSidebarScroll() {
		int maxScroll = Math.max(0, craftingSidebarContentH() - tierSidebarVisibleH());
		craftingSidebarScroll = Math.min(craftingSidebarScroll, maxScroll);
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

	// ────────────────────────────────────────────────────────────
	//  Misc
	// ────────────────────────────────────────────────────────────

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
