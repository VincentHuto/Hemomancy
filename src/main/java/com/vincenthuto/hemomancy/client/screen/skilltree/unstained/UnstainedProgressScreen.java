package com.vincenthuto.hemomancy.client.screen.skilltree.unstained;

import com.vincenthuto.hemomancy.common.capability.player.shared.skill.EnumNodeShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.client.screen.skilltree.shared.*;
import com.vincenthuto.hemomancy.client.screen.skilltree.unstained.UnstainedProgressLayout.Kind;
import com.vincenthuto.hemomancy.client.screen.skilltree.unstained.UnstainedProgressLayout.Link;
import com.vincenthuto.hemomancy.client.screen.skilltree.unstained.UnstainedProgressLayout.Node;
import com.vincenthuto.hemomancy.client.screen.skilltree.unstained.UnstainedProgressLayout.Pip;
import com.vincenthuto.hemomancy.client.screen.skilltree.unstained.UnstainedProgressLayout.Rect;
import com.vincenthuto.hemomancy.client.screen.skilltree.unstained.UnstainedProgressLayout.Rite;
import com.vincenthuto.hemomancy.client.screen.skilltree.unstained.UnstainedProgressLayout.Status;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.*;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.*;
import com.vincenthuto.hemomancy.common.mission.unstained.UnstainedObservances;
import com.vincenthuto.hemomancy.common.mission.unstained.UnstainedObservances.Observance;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.unstained.PacketToggleUnstainedBonus;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Unstained Progress screen — displays the player's purification and clarity
 * progression along the Unstained path. Opened via the Self-Reflection Mirror item.
 * <p>
 * Features:
 * <ul>
 *   <li>Dark blue background with floating hollow rhombus particles</li>
 *   <li>Click-and-drag panning</li>
 *   <li>Scroll-wheel zoom (anchored to cursor)</li>
 *   <li>Home button to refit the path</li>
 *   <li>One left-to-right path: Lethean Baptism, the purity stages, the three bridge rites,
 *       then the clarity stages (once unlocked); positions come from {@link UnstainedProgressLayout}</li>
 *   <li>Observance pips under the stage that opens each observance</li>
 *   <li>Progress bars above the purity and clarity segments</li>
 *   <li>Tooltips for stages, rites and observances</li>
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
	private final RitesTabController ritesTab = new RitesTabController();
	private final CraftingTabController craftingTab = new CraftingTabController();
	private final MaterialsTabController materialsTab = new MaterialsTabController(
			MaterialAtlasPath.UNSTAINED, MaterialsData.getUnstainedEntries(), EnumNodeShape.DIAMOND,
			UTab.MATERIALS.color, 0x0080B0A0, 0xFF6088B0,
			0xFF203050, 0xDD101828, MiniRecipeRenderer.UNSTAINED);

	// ── Colours — silver/teal Unstained palette on dark blue ──
	private static final int PURITY_COLOR       = 0xFFB0C0E0;
	private static final int PURITY_GLOW        = 0xFF8090BB;

	private static final int CLARITY_COLOR      = 0xFF80D0C0;
	private static final int CLARITY_GLOW       = 0xFF50AA90;

	private static final int COL_BORDER_OUTER   = 0xFF203050;
	private static final int COL_BORDER_INNER   = 0xFF101828;

	private static final int COL_NODE_BG        = 0xCC0C1020;
	private static final int COL_NODE_LOCKED    = 0xFF303848;
	private static final int COL_NODE_VEILED    = 0xFF242C3A;
	private static final int COL_NODE_NEXT      = 0xFF8098C0;
	private static final int COL_NODE_REACHED   = 0xFF6088B0;
	private static final int COL_NODE_CURRENT   = 0xFFB0C0E0;

	private static final int COL_LINE_LOCKED    = 0x66283848;
	private static final int COL_LINE_VEILED    = 0x40283848;
	private static final int COL_LINE_REACHED   = 0xFF5080B0;
	private static final int COL_LINE_CLARITY   = 0xFF50A898;

	private static final int COL_OBS_CLAIMED    = 0xFF70C090;
	private static final int COL_OBS_ACCEPTED   = 0xFFB0C0E0;
	private static final int COL_OBS_AVAILABLE  = 0xFF80D0C0;
	private static final int COL_OBS_LOCKED     = 0xFF687080;

	private final UnstainedBackgroundRenderer backgroundRenderer = new UnstainedBackgroundRenderer();

	/** Below this zoom the path drops its text (labels, headers, penalty line). */
	private static final float LABEL_MIN_ZOOM = 0.4f;
	private static final int TOOLTIP_WRAP_W = 220;

	// ── Pan / zoom (encapsulated in PanZoomState) ──
	private final PanZoomState progressView = new PanZoomState();
	/** Active view — points to whichever tab's PanZoomState is current. */
	private PanZoomState view = progressView;
	private boolean isDragging;

	// ── Home button ──
	private static final int HOME_BTN_SIZE = 16;
	private static final int HOME_BTN_PAD  = 4;
	private static final int ZOOM_LABEL_H  = 14;

	// ── Milestones sidebar ──
	private static final int SIDEBAR_INSET = 4;
	private static final int SIDEBAR_TOP = 22;
	private static final int SIDEBAR_WIDTH = 170;
	private static final int SIDEBAR_TAB_W = 14;  // collapsed tab width
	private static final int SIDEBAR_TAB_H = 50;  // collapsed tab height
	private static final int SIDEBAR_SHADOW = 0xAA000000;
	private static final float SIDEBAR_Z = 700.0F;
	private boolean sidebarVisible = false;
	private int sidebarScroll;
	private int sidebarContentH;

	// ── Bonus toggle panel (right side) ──
	private static final int BONUS_BTN_SIZE = 24;
	private static final int BONUS_BTN_GAP  = 6;
	private static final int BONUS_PANEL_PAD = 6;

	// ── GUI viewport (screen-space pixels, set in init()) ──
	private int guiLeft, guiTop, guiWidth, guiHeight;

	// ── Path layout (content-space, rebuilt from the cached progress) ──
	private UnstainedProgressLayout layout;
	/** Progress the current layout was built from; render rebuilds only when it changes. */
	private UnstainedProgressLayout.Progress layoutProgress;

	// ── Cached capability data ──
	private boolean begunPurification;
	private float purity;
	private boolean clarityPrepared;
	private boolean clarityUnlocked;
	private float clarity;
	private float silverWardStrength;
	private float verdigrisAura;
	private EnumPurityStage currentPurityStage = EnumPurityStage.CORRUPTED;
	private EnumClarityStage currentClarityStage = EnumClarityStage.AWAKENED;
	private UnstainedPhase phase = UnstainedPhase.OUTSIDER;

	// ── Cached bonus toggle state ──
	private boolean silverWardEnabled;
	private boolean verdigrisAuraEnabled;

	// ── Cached milestone data ──
	private boolean mSleptHemolysis, mFirstHemoKill, mReachedAbstinence, mEmptiedBlood, mEarnedAdvancement;
	private int acceptedObservances, claimedObservances, availableObservances;

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
		ProgressScreenContext ctx = makeContext();
		ritesTab.onInit(ctx);
		craftingTab.onInit(ctx);
		materialsTab.onInit(ctx);

		fitProgressView();

		view = viewForTab(activeTab);
	}

	private void cachePlayerData() {
		if (Minecraft.getInstance().player != null) {
			var cap = HemoCapabilityAccess.getUnstainedProgress(Minecraft.getInstance().player);
			phase                = UnstainedAccessRules.phase(cap);
			begunPurification    = cap.hasBegunPurification();
			purity               = cap.getPurity();
			clarityPrepared      = cap.isClarityPrepared();
			clarityUnlocked      = cap.hasClarityUnlocked();
			clarity              = cap.getClarity();
			silverWardStrength   = cap.getSilverWardStrength();
			verdigrisAura        = cap.getVerdigrisAura();
			silverWardEnabled    = cap.isSilverWardEnabled();
			verdigrisAuraEnabled = cap.isVerdigrisAuraEnabled();
			currentPurityStage   = EnumPurityStage.byPurity(purity);
			currentClarityStage  = EnumClarityStage.byClarity(clarity);
			// Milestones
			mSleptHemolysis     = cap.hasSleptWithHemolysis();
			mFirstHemoKill      = cap.hasKilledFirstHemoMob();
			mReachedAbstinence  = cap.hasReachedAbstinence();
			mEmptiedBlood       = cap.hasEmptiedBlood();
			mEarnedAdvancement  = cap.hasEarnedAdvancement();
			acceptedObservances = cap.getAcceptedObservances();
			claimedObservances  = cap.getClaimedObservances();
			int available = 0;
			for (Observance observance : Observance.values()) {
				if (UnstainedObservances.isAvailable(cap, observance)) available |= observance.mask();
			}
			availableObservances = available;
		}
		UnstainedProgressLayout.Progress progress = new UnstainedProgressLayout.Progress(
				phase, currentPurityStage, clarityPrepared, clarityUnlocked, currentClarityStage, begunPurification);
		if (layout == null || !progress.equals(layoutProgress)) {
			layout = UnstainedProgressLayout.build(progress);
			layoutProgress = progress;
		}
	}

	// ────────────────────────────────────────────────────────────
	//  Coordinate helpers — delegate to active PanZoomState
	// ────────────────────────────────────────────────────────────

	/** Content-space X → screen-space X */
	private int sx(int contentX) { return view.sx(guiLeft, contentX); }
	/** Content-space Y → screen-space Y */
	private int sy(int contentY) { return view.sy(guiTop,  contentY); }
	/** Half of a node's on-screen size, accounting for zoom. */
	private int screenHalf(Node node) { return view.halfNode(node.halfSize() * 2); }

	private boolean insideGui(double mx, double my) {
		return mx >= guiLeft && mx < guiLeft + guiWidth
			&& my >= guiTop  && my < guiTop  + guiHeight;
	}

	private ProgressScreenContext makeContext() {
		int unstainedLevel = minecraft != null && minecraft.player != null
				? ProgressionGates.unstainedStage(minecraft.player)
				: 0;
		return new ProgressScreenContext(font, guiLeft, guiTop, guiWidth, guiHeight, unstainedLevel);
	}

	// ────────────────────────────────────────────────────────────
	//  What the Progress tab shows for the current phase
	// ────────────────────────────────────────────────────────────

	/** The path (and its tooltips) is drawn for these phases; the others show a route message. */
	private boolean pathVisible() {
		return phase == UnstainedPhase.PURIFYING || phase == UnstainedPhase.CURE_READY
				|| phase == UnstainedPhase.PLEDGED;
	}

	/** The Book of Observances sidebar is offered on every route once one has begun. */
	private boolean sidebarAvailable() {
		return phase != UnstainedPhase.OUTSIDER;
	}

	private boolean sidebarOpen() {
		return sidebarAvailable() && sidebarVisible;
	}

	private boolean bonusAvailable() {
		return begunPurification || clarityUnlocked;
	}

	private static boolean isVow(Observance observance) {
		return (observance.mask() & UnstainedAccessRules.NOVITIATE_MASK) != 0;
	}

	/** Vows show for players on the vow route; purification observances for everyone else. */
	private boolean observanceShown(Observance observance) {
		boolean vowRoute = phase == UnstainedPhase.NOVITIATE
				|| ((acceptedObservances | claimedObservances) & UnstainedAccessRules.NOVITIATE_MASK) != 0;
		if (isVow(observance)) return vowRoute;
		return begunPurification || clarityUnlocked || !vowRoute;
	}

	// ────────────────────────────────────────────────────────────
	//  Pan / zoom helpers
	// ────────────────────────────────────────────────────────────

	private int contentW() {
		return switch (activeTab) {
			case PROGRESS  -> layout.contentBounds().w();
			case MATERIALS -> materialsTab.getContentW();
			default        -> 0; // RITES / CRAFTING don't pan
		};
	}
	private int contentH() {
		return switch (activeTab) {
			case PROGRESS  -> layout.contentBounds().h();
			case MATERIALS -> materialsTab.getContentH();
			default        -> 0;
		};
	}

	/** Returns the PanZoomState for a given tab (null-safe for browse tabs). */
	private PanZoomState viewForTab(UTab tab) {
		return switch (tab) {
			case PROGRESS  -> progressView;
			case MATERIALS -> materialsTab.getPanZoomState();
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

	/** GUI-relative left edge of the area the path is fitted into (right of an open sidebar). */
	private int progressAreaLeft() {
		return sidebarOpen() ? SIDEBAR_INSET + SIDEBAR_WIDTH + SIDEBAR_TAB_W + 4 : 0;
	}

	private int progressAreaTop() {
		return TAB_PAD + TAB_HEIGHT + 2;
	}

	/** Fits the whole path between the tabs and the zoom readout, clear of an open sidebar. */
	private void fitProgressView() {
		int areaX = progressAreaLeft();
		int areaY = progressAreaTop();
		int areaW = Math.max(1, guiWidth - areaX);
		int areaH = Math.max(1, guiHeight - areaY - ZOOM_LABEL_H);
		UnstainedProgressLayout.View fit = layout.fit(areaX, areaY, areaW, areaH);
		progressView.zoom = fit.zoom();
		progressView.panX = fit.panX();
		progressView.panY = fit.panY();
	}

	private void resetToHome() {
		if (activeTab == UTab.PROGRESS) {
			fitProgressView();
			return;
		}
		if (activeTab == UTab.MATERIALS) {
			materialsTab.resetView(makeContext());
			return;
		}
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
				if (sidebarAvailable() && isOverSidebarToggle(mx, my)) {
					sidebarVisible = !sidebarVisible;
					fitProgressView();
					return true;
				}
				if (sidebarOpen() && isOverSidebarPanel(mx, my)) {
					return true;
				}
				// Bonus toggle buttons
				if (bonusAvailable()) {
					int clickedBonus = getBonusButtonAt(mx, my);
					if (clickedBonus >= 0) {
						PacketHandler.sendToServer(new PacketToggleUnstainedBonus(clickedBonus));
						return true;
					}
				}
			}
			if (activeTab == UTab.MATERIALS && insideGui(mx, my)) {
				if (materialsTab.mouseClicked(makeContext(), mx, my, btn)) return true;
				isDragging = true;
				return true;
			}
			if (activeTab == UTab.RITES && insideGui(mx, my)) return ritesTab.mouseClicked(makeContext(), mx, my, btn);
			if (activeTab == UTab.CRAFTING && insideGui(mx, my)) return craftingTab.mouseClicked(makeContext(), mx, my, btn);
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
			if (activeTab == UTab.RITES) ritesTab.mouseReleased(makeContext(), mx, my, btn);
			if (activeTab == UTab.CRAFTING) craftingTab.mouseReleased(makeContext(), mx, my, btn);
		}
		return super.mouseReleased(mx, my, btn);
	}

	@Override
	public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
		if (activeTab == UTab.CRAFTING && craftingTab.mouseDragged(makeContext(), mx, my, btn, dx, dy)) return true;
		if (activeTab == UTab.RITES && ritesTab.mouseDragged(makeContext(), mx, my, btn, dx, dy)) return true;
		if (isDragging && btn == 0) {
			view.applyDrag(dx, dy);
			saveTabPan();
			return true;
		}
		return super.mouseDragged(mx, my, btn, dx, dy);
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
		if (!insideGui(mx, my)) return super.mouseScrolled(mx, my, scrollX, scrollY);

		if (activeTab == UTab.RITES) return ritesTab.mouseScrolled(makeContext(), mx, my, scrollY);
		if (activeTab == UTab.CRAFTING) return craftingTab.mouseScrolled(makeContext(), mx, my, scrollY);
		if (activeTab == UTab.PROGRESS && sidebarOpen() && isOverSidebarPanel(mx, my)) {
			sidebarScroll -= (int) (scrollY * 12);
			clampSidebarScroll();
			return true;
		}

		view.applyScroll(guiLeft, guiTop, mx, my, scrollY);
		saveTabPan();
		return true;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		IProgressTab detailTab = activeDetailTab();
		if (detailTab != null) {
			if (detailTab.keyPressed(makeContext(), keyCode, scanCode, modifiers)) return true;
			if (ProgressDetailKeyHandler.handle(keyCode, detailTab::closeDetails)) return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	private IProgressTab activeDetailTab() {
		return switch (activeTab) {
			case RITES -> ritesTab;
			case CRAFTING -> craftingTab;
			case MATERIALS -> materialsTab;
			case PROGRESS -> null;
		};
	}

	// ────────────────────────────────────────────────────────────
	//  Render
	// ────────────────────────────────────────────────────────────

	@Override
	public void render(@Nonnull GuiGraphics gfx, int mouseX, int mouseY, float partial) {
		// Do NOT call renderBackground() — it applies blur. This is not a pause screen.
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableBlend();

		cachePlayerData();

		// 1. Dark blue background with floating hollow rhombuses
		backgroundRenderer.render(gfx, guiLeft, guiTop, guiWidth, guiHeight);

		// 2. Border
		drawBorder(gfx, guiLeft, guiTop, guiWidth, guiHeight);

		// 3. Scissored content area
		gfx.enableScissor(guiLeft + 2, guiTop + 2,
				guiLeft + guiWidth - 2, guiTop + guiHeight - 2);

		if (activeTab == UTab.PROGRESS) {
			if (phase == UnstainedPhase.OUTSIDER) {
				drawNotBegunMessage(gfx);
			} else if (!pathVisible()) {
				drawRouteMessage(gfx);
			} else {
				drawPath(gfx, mouseX, mouseY);
			}
		} else if (activeTab == UTab.RITES) {
			ritesTab.render(gfx, makeContext(), mouseX, mouseY, partial);
		} else if (activeTab == UTab.CRAFTING) {
			craftingTab.render(gfx, makeContext(), mouseX, mouseY, partial);
		} else if (activeTab == UTab.MATERIALS) {
			materialsTab.render(gfx, makeContext(), mouseX, mouseY, partial);
		}

		gfx.disableScissor();

		// 3b. Info panel overlay (outside scissor so it renders on top of nodes)
		if (activeTab == UTab.MATERIALS) {
			materialsTab.renderOverlay(gfx, makeContext(), mouseX, mouseY);
		}

		// 4. Milestones sidebar (fixed on far left, outside content scissor; Progress tab only)
		if (activeTab == UTab.PROGRESS && sidebarAvailable()) {
			drawMilestones(gfx, mouseX, mouseY);
		}

		// 4b. Bonus toggle buttons (fixed on far right, outside content scissor; Progress tab only)
		if (activeTab == UTab.PROGRESS && bonusAvailable()) {
			drawBonusToggleButtons(gfx, mouseX, mouseY);
		}

		// 5. Home button (outside scissor; not shown for browse tabs)
		if (activeTab != UTab.RITES && activeTab != UTab.CRAFTING) {
			drawHomeButton(gfx, mouseX, mouseY);
		}

		// 5b. Tabs
		drawTabs(gfx, mouseX, mouseY);

		// 6. Zoom indicator (hidden under an open milestones sidebar; not shown for browse tabs)
		boolean progressZoomShown = activeTab == UTab.PROGRESS && pathVisible() && !sidebarOpen();
		if (activeTab == UTab.MATERIALS || progressZoomShown) {
			gfx.drawString(font,
					String.format("%.0f%%", view.zoom * 100),
					guiLeft + 5, guiTop + guiHeight - 12, 0x55888888, false);
		}

		// 7. Tooltips (outside scissor)
		if (activeTab == UTab.PROGRESS) {
			if (pathVisible() && !isOverChrome(mouseX, mouseY)) {
				drawPathTooltips(gfx, mouseX, mouseY);
				drawStatTooltips(gfx, mouseX, mouseY);
			}
			if (bonusAvailable()) {
				drawBonusToggleTooltips(gfx, mouseX, mouseY);
			}
			if (sidebarAvailable() && isOverSidebarToggle(mouseX, mouseY)) {
				String tipText = sidebarVisible ? "Hide Milestones" : "Show Milestones";
				gfx.renderTooltip(font, Component.literal(tipText), mouseX, mouseY);
			}
		} else if (activeTab == UTab.MATERIALS) {
			materialsTab.renderTooltip(gfx, makeContext(), mouseX, mouseY);
		}

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableBlend();
		super.render(gfx, mouseX, mouseY, partial);
	}

	/** Suppress the 1.21.1 menu_blur post-effect from Screen#renderBackground. */
	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		// intentionally empty
	}

	/** Fixed chrome drawn over the path; path tooltips stay quiet underneath it. */
	private boolean isOverChrome(double mx, double my) {
		return isOverHomeButton(mx, my)
				|| tabUnder(mx, my) != null
				|| (sidebarAvailable() && isOverSidebarToggle(mx, my))
				|| (sidebarOpen() && isOverSidebarPanel(mx, my))
				|| (bonusAvailable() && getBonusButtonAt(mx, my) >= 0);
	}

	// ────────────────────────────────────────────────────────────
	//  Route messages (phases without a drawn path)
	// ────────────────────────────────────────────────────────────

	/** Screen-space centre X of the free area (right of an open sidebar). */
	private int messageCentreX() {
		int areaX = progressAreaLeft();
		return guiLeft + areaX + (guiWidth - areaX) / 2;
	}

	private int messageWidth() {
		return Math.max(80, guiWidth - progressAreaLeft() - 24);
	}

	private void drawNotBegunMessage(GuiGraphics gfx) {
		int centerX = guiLeft + guiWidth / 2;
		int centerY = guiTop + guiHeight / 2;

		gfx.drawCenteredString(font,
				Component.literal("The Unstained Path Awaits"),
				centerX, centerY - 16, 0xFF8098C0);
		gfx.drawCenteredString(font,
				Component.literal("Seek treatment for active blood, or take the five service vows."),
				centerX, centerY, 0xFF506080);
		gfx.drawCenteredString(font,
				Component.literal("Speak with an Unstained Zealot to begin."),
				centerX, centerY + 16, 0xFF384860);
	}

	private void drawRouteMessage(GuiGraphics gfx) {
		int centerX = messageCentreX();
		int y = guiTop + guiHeight / 2 - 12;
		if (phase == UnstainedPhase.NOVITIATE) {
			int vows = UnstainedAccessRules.completedNovitiateVows(claimedObservances);
			gfx.drawCenteredString(font, Component.literal("Novitiate Service"), centerX, y, 0xFF80D0C0);
			gfx.drawCenteredString(font, Component.literal("Vows fulfilled: " + vows + "/5"), centerX, y + 16, 0xFFB0C8E8);
			if (UnstainedAccessRules.hasCompletedNovitiateVows(claimedObservances)) {
				String next = clarityPrepared
						? "Consecrated Copper has steadied the crossing. Perform Clarity Ascension with your blood wholly relinquished."
						: "Your service is kept. Offer Consecrated Copper at an Unstained Podium, then perform Clarity Ascension. Both ask for blood wholly relinquished.";
				drawCentredWrapped(gfx, Component.literal(next), centerX, y + 32, 0xFF8098C0);
			}
		} else {
			gfx.drawCenteredString(font, Component.literal("Baseline Restored"), centerX, y, 0xFF80D0C0);
			String next = clarityPrepared
					? "Consecrated Copper has steadied the crossing. Perform Clarity Ascension."
					: "Offer Consecrated Copper, then complete Clarity Ascension.";
			drawCentredWrapped(gfx, Component.literal(next), centerX, y + 16, 0xFFB0C8E8);
		}
	}

	private void drawCentredWrapped(GuiGraphics gfx, Component text, int centerX, int y, int color) {
		for (FormattedCharSequence line : font.split(text, messageWidth())) {
			gfx.drawCenteredString(font, line, centerX, y, color);
			y += 11;
		}
	}

	// ────────────────────────────────────────────────────────────
	//  Path
	// ────────────────────────────────────────────────────────────

	private void drawPath(GuiGraphics gfx, int mouseX, int mouseY) {
		// Links first — they stop at the diamond tips so they never cut through a node
		int lw = Math.max(1, (int)(view.zoom * 1.5f));
		for (Link link : layout.links()) {
			int y = sy(link.from().y());
			int x1 = sx(link.from().x()) + screenHalf(link.from());
			int x2 = sx(link.to().x()) - screenHalf(link.to());
			if (x2 > x1) gfx.fill(x1, y - lw, x2, y + lw, linkColor(link));
		}

		for (Node node : layout.nodes()) {
			if (node.kind() == Kind.RITE) {
				drawRiteNode(gfx, node);
			} else {
				drawStageNode(gfx, node);
			}
		}

		if (view.zoom >= LABEL_MIN_ZOOM) {
			// Text lives in content space so labels shrink with the path and keep their spacing
			var pose = gfx.pose();
			pose.pushPose();
			pose.translate(guiLeft + (float) view.panX, guiTop + (float) view.panY, 0.0F);
			pose.scale(view.zoom, view.zoom, 1.0F);
			drawSegmentHeader(gfx, layout.purityHeader(), "— Purity —", purity, PURITY_COLOR, 0x7088C0, 0xFF8098B0);
			layout.clarityHeader().ifPresent(header ->
					drawSegmentHeader(gfx, header, "— Clarity —", clarity, CLARITY_COLOR, 0x50B0A0, 0xFF70B0A0));
			for (Node node : layout.nodes()) {
				int color = labelColor(node);
				int lineY = node.y() + UnstainedProgressLayout.LABEL_DY;
				for (String line : node.labelLines()) {
					gfx.drawCenteredString(font, line, node.x(), lineY, color);
					lineY += UnstainedProgressLayout.LABEL_LINE_H;
				}
			}
			Rect penalty = layout.penaltyRect();
			float penaltyValue = currentPurityStage.getBloodMagicPenalty();
			gfx.drawCenteredString(font, Component.literal(String.format("Blood Magic Penalty: -%.0f%%", penaltyValue * 100)),
					penalty.x() + penalty.w() / 2, penalty.y(), penaltyValue > 0 ? 0xFFCC8060 : 0xFF405060);
			pose.popPose();
		}

		Pip hoveredPip = pipUnder(mouseX, mouseY);
		for (Pip pip : layout.pips()) {
			if (observanceShown(pip.observance())) drawPip(gfx, pip, pip == hoveredPip);
		}
	}

	private int linkColor(Link link) {
		if (link.lit()) {
			return isClaritySide(link.to()) ? COL_LINE_CLARITY : COL_LINE_REACHED;
		}
		return link.to().veiled() ? COL_LINE_VEILED : COL_LINE_LOCKED;
	}

	private static boolean isClaritySide(Node node) {
		return node.kind() == Kind.CLARITY_STAGE || node.rite() == Rite.CLARITY_ASCENSION;
	}

	private int labelColor(Node node) {
		return switch (node.status()) {
			case CURRENT -> isClaritySide(node) ? CLARITY_COLOR : PURITY_COLOR;
			case REACHED -> 0xFF7090B0;
			case NEXT -> 0xFF8098C0;
			case LOCKED -> 0xFF384050;
			case VEILED -> 0xFF2C3444;
			case BYPASSED -> 0xFF4A5468;
		};
	}

	private void drawSegmentHeader(GuiGraphics gfx, Rect header, String title, float value,
								   int titleColor, int barRGB, int percentColor) {
		int centreX = header.x() + header.w() / 2;
		gfx.drawCenteredString(font, Component.literal(title), centreX, header.y(), titleColor);
		ScreenDrawUtils.drawProgressBar(gfx, header.x(), header.y() + UnstainedProgressLayout.HEADER_BAR_DY,
				header.w(), UnstainedProgressLayout.HEADER_BAR_H, value, 100f, barRGB);
		gfx.drawCenteredString(font, String.format("%.1f%%", value),
				centreX, header.y() + UnstainedProgressLayout.HEADER_PERCENT_DY, percentColor);
	}

	private void drawStageNode(GuiGraphics gfx, Node node) {
		boolean claritySide = node.kind() == Kind.CLARITY_STAGE;
		EnumNodeShape shape = claritySide ? node.clarityStage().getNodeShape() : node.purityStage().getNodeShape();
		ResourceLocation texture = claritySide ? node.clarityStage().getIconTexture() : node.purityStage().getIconTexture();
		ItemStack icon = claritySide ? node.clarityStage().getIconItem() : node.purityStage().getIconItem();
		drawShapedNode(gfx, sx(node.x()), sy(node.y()), screenHalf(node), node.title(), node.status(),
				claritySide ? CLARITY_GLOW : PURITY_GLOW, shape, texture, icon);
	}

	private void drawRiteNode(GuiGraphics gfx, Node node) {
		int cx = sx(node.x());
		int cy = sy(node.y());
		int hs = screenHalf(node);
		int accent = isClaritySide(node) ? CLARITY_COLOR : PURITY_COLOR;
		if (node.status() == Status.NEXT) {
			float p = 0.5f + 0.5f * Mth.sin(animTime * 2.5f);
			NodeShapeRenderer.drawFill(gfx, EnumNodeShape.DIAMOND, cx, cy, hs + 3,
					((int)(36 * p) << 24) | (accent & 0x00FFFFFF));
		}
		int border = switch (node.status()) {
			case REACHED, CURRENT -> COL_NODE_REACHED;
			case NEXT -> accent;
			case LOCKED, BYPASSED -> COL_NODE_LOCKED;
			case VEILED -> COL_NODE_VEILED;
		};
		NodeShapeRenderer.drawFill(gfx, EnumNodeShape.DIAMOND, cx, cy, hs, COL_NODE_BG);
		NodeShapeRenderer.drawOutline(gfx, EnumNodeShape.DIAMOND, cx, cy, hs, border);
		if (node.done() && node.status() != Status.BYPASSED) {
			NodeShapeRenderer.drawFill(gfx, EnumNodeShape.DIAMOND, cx, cy, Math.max(1, hs / 3), accent);
		} else if (node.veiled() && view.zoom >= 0.75f) {
			gfx.drawCenteredString(font, "?", cx, cy - 4, 0xFF404858);
		}
	}

	// ────────────────────────────────────────────────────────────
	//  Shape-aware stage node
	// ────────────────────────────────────────────────────────────
	private float animTime = 0f;
	private void drawShapedNode(GuiGraphics gfx, int scrX, int scrY, int hn, String title,
								 Status status, int glowColor, EnumNodeShape shape,
								 @javax.annotation.Nullable ResourceLocation iconTexture,
								 @javax.annotation.Nullable ItemStack iconStack) {
		animTime += 0.016f; // ~60 FPS approximation

		float time = animTime;
		boolean isCurrent = status == Status.CURRENT;
		boolean reached = status.done();

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
		} else if (status == Status.BYPASSED) {
			border = COL_NODE_LOCKED;
		} else if (reached) {
			border = COL_NODE_REACHED;
		} else if (status == Status.NEXT) {
			border = COL_NODE_NEXT;
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
	}

	// ────────────────────────────────────────────────────────────
	//  Observance pips
	// ────────────────────────────────────────────────────────────

	private int pipHalf() {
		return Math.max(2, Math.round(UnstainedProgressLayout.PIP_SIZE / 2.0f * view.zoom));
	}

	private Pip pipUnder(double mx, double my) {
		int hs = pipHalf() + 1;
		for (Pip pip : layout.pips()) {
			if (!observanceShown(pip.observance())) continue;
			int cx = sx(pip.x());
			int cy = sy(pip.y());
			if (mx >= cx - hs && mx <= cx + hs && my >= cy - hs && my <= cy + hs) return pip;
		}
		return null;
	}

	private void drawPip(GuiGraphics gfx, Pip pip, boolean hovered) {
		int cx = sx(pip.x());
		int cy = sy(pip.y());
		int hs = pipHalf();
		Observance observance = pip.observance();
		int color = observanceColor(observance);
		if (isClaimed(observance) || isAccepted(observance) || isAvailable(observance)) {
			NodeShapeRenderer.drawFill(gfx, EnumNodeShape.DIAMOND, cx, cy, hs, color);
		} else {
			NodeShapeRenderer.drawFill(gfx, EnumNodeShape.DIAMOND, cx, cy, hs, COL_NODE_BG);
			NodeShapeRenderer.drawOutline(gfx, EnumNodeShape.DIAMOND, cx, cy, hs, color);
		}
		if (hovered) {
			NodeShapeRenderer.drawOutline(gfx, EnumNodeShape.DIAMOND, cx, cy, hs + 1, 0xFFE0E8F8);
		}
	}

	private boolean isClaimed(Observance observance) { return (claimedObservances & observance.mask()) != 0; }
	private boolean isAccepted(Observance observance) { return (acceptedObservances & observance.mask()) != 0; }
	private boolean isAvailable(Observance observance) { return (availableObservances & observance.mask()) != 0; }

	private int observanceColor(Observance observance) {
		if (isClaimed(observance)) return COL_OBS_CLAIMED;
		if (isAccepted(observance)) return COL_OBS_ACCEPTED;
		return isAvailable(observance) ? COL_OBS_AVAILABLE : COL_OBS_LOCKED;
	}

	private String observancePrefix(Observance observance) {
		if (isClaimed(observance)) return "✓";
		if (isAccepted(observance)) return "•";
		return isAvailable(observance) ? "+" : "×";
	}

	// ────────────────────────────────────────────────────────────
	//  Stat readouts — Blood Magic Penalty tooltip
	// ────────────────────────────────────────────────────────────

	/** Hover tooltip for the Blood Magic Penalty line under the purity stages. */
	private void drawStatTooltips(GuiGraphics gfx, int mouseX, int mouseY) {
		if (!insideGui(mouseX, mouseY) || view.zoom < LABEL_MIN_ZOOM) return;

		Rect penalty = layout.penaltyRect();
		if (mouseX >= sx(penalty.x()) && mouseX <= sx(penalty.right())
				&& mouseY >= sy(penalty.y()) - 1 && mouseY <= sy(penalty.bottom()) + 1) {
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
			float penaltyValue = currentPurityStage.getBloodMagicPenalty() * 100;
			tip.add(Component.literal(String.format("Current: -%.0f%%", penaltyValue))
					.withStyle(s -> s.withColor(penaltyValue > 0 ? 0xCC6644 : 0x607060).withItalic(true)));
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

	/** Returns the screen-space Y for the top-left of the first bonus button (below the tabs). */
	private int bonusPanelStartY() {
		return guiTop + 24;
	}

	/** Returns the total number of visible bonus buttons (based on progress). */
	private int bonusButtonCount() {
		int count = 1; // Verdigris Aura is always shown once purification has begun
		if (clarityUnlocked) count++; // Silver Ward
		return count;
	}

	/**
	 * Hit-test: returns the bonus button index (0-based) at the given mouse position,
	 * or -1 if no button is under the cursor.
	 * 0 = Verdigris Aura, 1 = Silver Ward.
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

		// Verdigris Aura button (always visible once purification begun — purity path)
		drawBonusButton(gfx, px, py, "A", verdigrisAuraEnabled,
				purity > 0, PURITY_COLOR, PURITY_GLOW,
				getBonusButtonAt(mouseX, mouseY) == 0);

		// Silver Ward button (only if clarity unlocked — clarity path)
		if (clarityUnlocked) {
			int y2 = py + BONUS_BTN_SIZE + BONUS_BTN_GAP;
			drawBonusButton(gfx, px, y2, "W", silverWardEnabled,
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
			animTime += 0.016f; // ~60 FPS approximation
			float p = 0.3f + 0.3f * Mth.sin(animTime * 2.0f);
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
			// Verdigris Aura tooltip (purity path — always first button)
			boolean unlocked = purity > 0;
			tip.add(Component.literal("Verdigris Aura")
					.withStyle(s -> s.withColor(0x80D0C0).withBold(true)));
			if (!unlocked) {
				tip.add(Component.literal("Locked — increase Purity to unlock.")
						.withStyle(s -> s.withColor(0x606870).withItalic(true)));
			} else {
				tip.add(Component.literal("A copper-based anti-blood field that")
						.withStyle(s -> s.withColor(0x70A898)));
				tip.add(Component.literal("weakens nearby blood magic entities and effects.")
						.withStyle(s -> s.withColor(0x70A898)));
				tip.add(Component.literal(""));
				tip.add(Component.literal("Scales with Purity (purity ÷ 100).")
						.withStyle(s -> s.withColor(0x508878).withItalic(true)));
				tip.add(Component.literal(String.format("Current: %.0f%%", verdigrisAura * 100))
						.withStyle(s -> s.withColor(0x50B0A0).withItalic(true)));
				tip.add(Component.literal(""));
				String state = verdigrisAuraEnabled ? "✔ Enabled" : "✖ Disabled";
				int stateCol = verdigrisAuraEnabled ? 0x60CC60 : 0xCC6060;
				tip.add(Component.literal(state + " — click to toggle")
						.withStyle(s -> s.withColor(stateCol)));
			}
		} else if (hovered == 1) {
			// Silver Ward tooltip (clarity path — second button)
			tip.add(Component.literal("Silver Ward")
					.withStyle(s -> s.withColor(0xB0C0E0).withBold(true)));
			tip.add(Component.literal("Passive resistance to blood magic effects.")
					.withStyle(s -> s.withColor(0x8898B0)));
			tip.add(Component.literal("Grants +4 Armor and +0.2 Knockback Resistance")
					.withStyle(s -> s.withColor(0x8898B0)));
			tip.add(Component.literal("while the Silver Ward effect is active.")
					.withStyle(s -> s.withColor(0x8898B0)));
			tip.add(Component.literal(""));
			tip.add(Component.literal("Scales with Clarity (clarity ÷ 100).")
					.withStyle(s -> s.withColor(0x607090).withItalic(true)));
			tip.add(Component.literal(String.format("Current: %.0f%%", silverWardStrength * 100))
					.withStyle(s -> s.withColor(0x60A0CC).withItalic(true)));
			tip.add(Component.literal(""));
			String state = silverWardEnabled ? "✔ Enabled" : "✖ Disabled";
			int stateCol = silverWardEnabled ? 0x60CC60 : 0xCC6060;
			tip.add(Component.literal(state + " — click to toggle")
					.withStyle(s -> s.withColor(stateCol)));
		}

		if (!tip.isEmpty()) {
			gfx.renderTooltip(font, tip, Optional.empty(), mouseX, mouseY);
		}
	}

	// ────────────────────────────────────────────────────────────
	//  Milestones sidebar — Book of Observances + one-time checklist
	// ────────────────────────────────────────────────────────────

	private int sidebarHeight() {
		return guiHeight - 28;
	}

	private void clampSidebarScroll() {
		sidebarScroll = Mth.clamp(sidebarScroll, 0, Math.max(0, sidebarContentH - (sidebarHeight() - 2)));
	}

	private void drawMilestones(GuiGraphics gfx, int mouseX, int mouseY) {
		int sideX = guiLeft + SIDEBAR_INSET;
		int sideY = guiTop + SIDEBAR_TOP;
		boolean tabHovered = isOverSidebarToggle(mouseX, mouseY);

		var pose = gfx.pose();
		pose.pushPose();
		pose.translate(0.0F, 0.0F, SIDEBAR_Z);
		if (!sidebarVisible) {
			// ── Collapsed: draw only the toggle tab ──
			drawSidebarToggleTab(gfx, sideX, sideY, false, tabHovered);
			pose.popPose();
			return;
		}

		// ── Expanded sidebar ──
		int sideW = SIDEBAR_WIDTH;
		int sideH = sidebarHeight();

		// Solid overlay backing; stage nodes and item icons use their own GUI depth.
		gfx.fill(sideX - 2, sideY - 2, sideX + sideW + 2, sideY + sideH + 2, SIDEBAR_SHADOW);
		gfx.fill(sideX, sideY, sideX + sideW, sideY + sideH, 0xFF0C1020);

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
		int textW = sideW - 12;
		int top = sideY + 6;
		int y = top - sidebarScroll;
		int centerX = sideX + sideW / 2;

		// Section title
		gfx.drawCenteredString(font, Component.literal("Book of Observances"), centerX, y, 0xFFB0C0E0);
		y += 14;

		// Thin divider
		gfx.fill(sideX + 8, y, sideX + sideW - 8, y + 1, 0x33607890);
		y += 5;

		y = drawWrapped(gfx, Component.literal("Next rite: " + nextRiteLabel()), x, y, textW, 0xFF80D0C0);
		y += 2;
		for (Observance observance : UnstainedProgressLayout.earnOrder()) {
			if (!observanceShown(observance)) continue;
			int color = observanceColor(observance);
			gfx.drawString(font, observancePrefix(observance), x, y, color, false);
			y = drawWrapped(gfx, Component.translatable(observance.translation("title")), x + 10, y, textW - 10, color);
		}
		y += 3;
		gfx.fill(sideX + 8, y, sideX + sideW - 8, y + 1, 0x33607890);
		y += 5;

		// ── One-time checklist ──
		y = drawCheckItem(gfx, x, y, textW, "First Hemo Kill", mFirstHemoKill);
		y = drawCheckItem(gfx, x, y, textW, "Slept w/ Hemolysis", mSleptHemolysis);
		y = drawCheckItem(gfx, x, y, textW, "5-min Abstinence", mReachedAbstinence);
		y = drawCheckItem(gfx, x, y, textW, "Emptied Blood", mEmptiedBlood);
		y = drawCheckItem(gfx, x, y, textW, "Earned Advancement", mEarnedAdvancement);

		y += 4;
		gfx.fill(sideX + 8, y, sideX + sideW - 8, y + 1, 0x33607890);
		y += 6;

		y = drawWrapped(gfx, Component.literal("Counters are records only; observances award progression."),
				x, y, textW, 0xFF687890);

		sidebarContentH = y + sidebarScroll - top + 6;
		clampSidebarScroll();
		gfx.disableScissor();

		int visibleH = sideH - 2;
		if (sidebarContentH > visibleH) {
			if (sidebarScroll > 0) {
				gfx.drawCenteredString(font, "▲", sideX + sideW - 10, sideY + 3, 0xAAFFFFFF);
			}
			if (sidebarScroll < sidebarContentH - visibleH) {
				gfx.drawCenteredString(font, "▼", sideX + sideW - 10, sideY + sideH - 11, 0xAAFFFFFF);
			}
		}
		pose.popPose();
	}

	/** Draws {@code text} wrapped to {@code width}; returns the Y below the last line. */
	private int drawWrapped(GuiGraphics gfx, Component text, int x, int y, int width, int color) {
		for (FormattedCharSequence line : font.split(text, Math.max(10, width))) {
			gfx.drawString(font, line, x, y, color, false);
			y += 10;
		}
		return y;
	}

	private String nextRiteLabel() {
		if (phase == UnstainedPhase.NOVITIATE) {
			if (!UnstainedAccessRules.hasCompletedNovitiateVows(claimedObservances)) return "Complete the five service vows";
			return clarityPrepared ? "Clarity Ascension" : "Consecrated Copper offering, then Clarity Ascension";
		}
		if (phase == UnstainedPhase.CURE_READY) return "Closed Vein";
		if (phase == UnstainedPhase.CLEANSED_UNPLEDGED) {
			return clarityPrepared ? "Clarity Ascension pledge" : "Consecrated Copper offering, then the Clarity Ascension pledge";
		}
		return switch (UnstainedPathGuidance.nextRequiredRite(begunPurification, purity >= 100f, clarityUnlocked)) {
			case "lethean_baptism" -> "Lethean Baptism";
			case "patient_purification" -> "No rite: complete observances";
			case "clarity_ascension" -> "Clarity Ascension";
			default -> "No rite: Still Arts follow Clarity";
		};
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
		int tabY = guiTop + SIDEBAR_TOP;
		int tabX;
		if (sidebarVisible) {
			// Tab is on the right edge of the expanded sidebar
			tabX = guiLeft + SIDEBAR_INSET + SIDEBAR_WIDTH;
		} else {
			// Tab is on the left edge when collapsed
			tabX = guiLeft + SIDEBAR_INSET;
		}
		return mx >= tabX && mx <= tabX + SIDEBAR_TAB_W
			&& my >= tabY && my <= tabY + SIDEBAR_TAB_H;
	}

	/** Hit test for the expanded sidebar panel itself. */
	private boolean isOverSidebarPanel(double mx, double my) {
		int sideX = guiLeft + SIDEBAR_INSET;
		int sideY = guiTop + SIDEBAR_TOP;
		return mx >= sideX && mx <= sideX + SIDEBAR_WIDTH
			&& my >= sideY && my <= sideY + sidebarHeight();
	}

	/** Draws a single checklist item with a check/cross icon. Returns the next Y. */
	private int drawCheckItem(GuiGraphics gfx, int x, int y, int width, String label, boolean complete) {
		String icon = complete ? "✓" : "✗";
		int iconCol = complete ? 0xFF60CC60 : 0xFF605060;
		int labelCol = complete ? 0xFFA0C0D0 : 0xFF506070;

		gfx.drawString(font, icon, x, y, iconCol, false);
		int end = drawWrapped(gfx, Component.literal(label), x + 14, y, width - 14, labelCol);
		return Math.max(y + 12, end + 2);
	}

	// ────────────────────────────────────────────────────────────
	//  Tooltips — stages, rites and observance pips
	// ────────────────────────────────────────────────────────────

	private void drawPathTooltips(GuiGraphics gfx, int mouseX, int mouseY) {
		if (!insideGui(mouseX, mouseY)) return;
		for (Node node : layout.nodes()) {
			EnumNodeShape shape = node.purityStage() != null ? node.purityStage().getNodeShape()
					: node.clarityStage() != null ? node.clarityStage().getNodeShape() : EnumNodeShape.DIAMOND;
			if (NodeShapeRenderer.isInside(shape, mouseX, mouseY, sx(node.x()), sy(node.y()), screenHalf(node))) {
				List<Component> tip = switch (node.kind()) {
					case PURITY_STAGE -> purityTooltip(node.purityStage(), node.status() == Status.BYPASSED);
					case CLARITY_STAGE -> clarityTooltip(node.clarityStage());
					case RITE -> riteTooltip(node);
				};
				renderWrappedTooltip(gfx, tip, mouseX, mouseY);
				return;
			}
		}
		Pip pip = pipUnder(mouseX, mouseY);
		if (pip != null) {
			renderWrappedTooltip(gfx, observanceTooltip(pip.observance()), mouseX, mouseY);
		}
	}

	private void renderWrappedTooltip(GuiGraphics gfx, List<Component> lines, int mouseX, int mouseY) {
		List<FormattedCharSequence> wrapped = new ArrayList<>();
		for (Component line : lines) {
			if (line.getString().isEmpty()) {
				wrapped.add(FormattedCharSequence.EMPTY);
			} else {
				wrapped.addAll(font.split(line, TOOLTIP_WRAP_W));
			}
		}
		gfx.renderTooltip(font, wrapped, mouseX, mouseY);
	}

	private List<Component> purityTooltip(EnumPurityStage stage, boolean bypassed) {
		List<Component> tip = new ArrayList<>();
		boolean reached = !bypassed && currentPurityStage.getLevel() >= stage.getLevel();
		boolean isCurrent = !bypassed && currentPurityStage == stage;

		tip.add(Component.literal(stage.getTitle())
				.withStyle(s -> s.withColor(isCurrent ? 0xB0C0E0 : (reached ? 0x7090B0 : 0x404858)).withBold(true)));
		tip.add(Component.literal(String.format("Requires: %.0f%% Purity", stage.getMinPurity()))
				.withStyle(s -> s.withColor(0x888888)));
		if (stage.getBloodMagicPenalty() > 0) {
			tip.add(Component.literal(String.format("Blood Magic Penalty: -%.0f%%", stage.getBloodMagicPenalty() * 100))
					.withStyle(s -> s.withColor(0xAA6644)));
		}
		if (bypassed) {
			tip.add(Component.literal("✓ Passed over: the five service vows answered for it")
					.withStyle(s -> s.withColor(0x7088A0).withItalic(true)));
		} else if (isCurrent) {
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
		return tip;
	}

	private List<Component> clarityTooltip(EnumClarityStage stage) {
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
		return tip;
	}

	private List<Component> riteTooltip(Node node) {
		List<Component> tip = new ArrayList<>();
		if (node.veiled()) {
			tip.add(Component.literal("Veiled Rite").withStyle(s -> s.withColor(0x606C80).withBold(true)));
			tip.add(Component.literal("A later rite rests beneath still water. It surfaces when you reach Purified.")
					.withStyle(s -> s.withColor(0x6878A0).withItalic(true)));
			return tip;
		}
		Rite rite = node.rite();
		int titleColor = node.done() ? 0x7090B0 : (node.status() == Status.NEXT ? 0xB0C0E0 : 0x505868);
		tip.add(Component.literal(rite.title()).withStyle(s -> s.withColor(titleColor).withBold(true)));
		String description = switch (rite) {
			case LETHEAN_BAPTISM -> "Still water takes the blood's first claim, and purification begins.";
			case CLOSED_VEIN -> "At full Purity the last current stills, and the blood returns to a mortal baseline.";
			case CONSECRATED_COPPER -> "Consecrated Copper offered at an Unstained Podium steadies the crossing into Clarity.";
			case CLARITY_ASCENSION -> "The pledge. Clarity opens, and blood magic is set down for good.";
		};
		tip.add(Component.literal(description).withStyle(s -> s.withColor(0x8898B0)));
		String state;
		int stateColor;
		if (node.done()) {
			state = switch (rite) {
				case LETHEAN_BAPTISM -> begunPurification ? "✓ Performed" : "✓ Answered by the five service vows";
				case CLOSED_VEIN -> node.status() == Status.BYPASSED
						? "✓ Passed over: the five service vows answered for it" : "✓ Baseline restored";
				case CONSECRATED_COPPER -> "✓ Offered";
				case CLARITY_ASCENSION -> "✓ Pledged";
			};
			stateColor = 0x44AA44;
		} else if (node.status() == Status.NEXT) {
			state = switch (rite) {
				case LETHEAN_BAPTISM -> "▸ Next: quiet the infection at an Unstained Podium, then enter the rite";
				case CLOSED_VEIN -> "▸ Next: perform the rite at full Purity";
				case CONSECRATED_COPPER -> "▸ Next: offer it at a Podium with your blood wholly relinquished";
				case CLARITY_ASCENSION -> "▸ Next: perform the rite with your blood wholly relinquished";
			};
			stateColor = 0x60A0CC;
		} else {
			state = switch (rite) {
				case LETHEAN_BAPTISM -> "Not yet begun";
				case CLOSED_VEIN -> "Requires Purified";
				case CONSECRATED_COPPER -> "Follows the Closed Vein";
				case CLARITY_ASCENSION -> "Follows the Consecrated Copper offering";
			};
			stateColor = 0xAA4444;
		}
		tip.add(Component.literal(state).withStyle(s -> s.withColor(stateColor).withItalic(true)));
		return tip;
	}

	private List<Component> observanceTooltip(Observance observance) {
		List<Component> tip = new ArrayList<>();
		int color = observanceColor(observance);
		tip.add(Component.translatable(observance.translation("title"))
				.withStyle(s -> s.withColor(color & 0x00FFFFFF).withBold(true)));
		if (isClaimed(observance)) {
			tip.add(Component.literal("✓ Fulfilled").withStyle(s -> s.withColor(0x44AA44).withItalic(true)));
		} else if (isAccepted(observance)) {
			tip.add(Component.literal("• Accepted: bring the offering to its guide")
					.withStyle(s -> s.withColor(0xB0C0E0).withItalic(true)));
		} else if (isAvailable(observance)) {
			tip.add(Component.literal("+ Available from " + issuerName(observance))
					.withStyle(s -> s.withColor(0x80D0C0).withItalic(true)));
		} else {
			tip.add(Component.translatable(observance.translation("unlock"))
					.withStyle(s -> s.withColor(0x687080).withItalic(true)));
		}
		tip.add(Component.translatable(observance.translation("requirement")).withStyle(s -> s.withColor(0x8898B0)));
		tip.add(Component.translatable(observance.translation("reward"))
				.withStyle(s -> s.withColor(0x6878A0).withItalic(true)));
		return tip;
	}

	private static String issuerName(Observance observance) {
		return switch (observance.issuer()) {
			case ACOLYTE -> "an Acolyte";
			case ZEALOT -> "a Zealot";
			case GUARDIAN -> "a Guardian";
		};
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
	//  Misc
	// ────────────────────────────────────────────────────────────

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void removed() {
		ritesTab.onClose();
		craftingTab.onClose();
		materialsTab.onClose();
		super.removed();
	}
}
