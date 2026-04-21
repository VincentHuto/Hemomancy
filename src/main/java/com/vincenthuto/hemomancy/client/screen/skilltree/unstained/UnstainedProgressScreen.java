package com.vincenthuto.hemomancy.client.screen.skilltree.unstained;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.vincenthuto.hemomancy.client.screen.skilltree.shared.CraftingTabController;
import com.vincenthuto.hemomancy.client.screen.skilltree.shared.MaterialsData;
import com.vincenthuto.hemomancy.client.screen.skilltree.shared.MaterialsTabController;
import com.vincenthuto.hemomancy.client.screen.skilltree.shared.RitesTabController;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.*;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumPurityStage;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.PacketToggleUnstainedBonus;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

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
	private final RitesTabController ritesTab = new RitesTabController(true);
	private final CraftingTabController craftingTab = new CraftingTabController(true);
	private final MaterialsTabController materialsTab = new MaterialsTabController(
			MaterialsData.getUnstainedEntries(), EnumNodeShape.DIAMOND,
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
	private static final int COL_NODE_REACHED   = 0xFF6088B0;
	private static final int COL_NODE_CURRENT   = 0xFFB0C0E0;

	private static final int COL_LINE_LOCKED    = 0x66283848;
	private static final int COL_LINE_REACHED   = 0xFF5080B0;

	private final UnstainedBackgroundRenderer backgroundRenderer = new UnstainedBackgroundRenderer();

	// ── Node layout (content-space pixels) ──
	private static final int NODE_SIZE    = 30;
	private static final int NODE_GAP_Y   = 72;
	private static final int COLUMN_SPACING = 260;

	// ── Pan / zoom (encapsulated in PanZoomState) ──
	private final PanZoomState progressView = new PanZoomState();
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
		ProgressScreenContext ctx = makeContext();
		ritesTab.onInit(ctx);
		craftingTab.onInit(ctx);
		materialsTab.onInit(ctx);

		// Centre the progress view
		progressView.centreOn(contentW, contentH, guiWidth, guiHeight);

		view = viewForTab(activeTab);
	}

	private void cachePlayerData() {
		if (Minecraft.getInstance().player != null) {
			Minecraft.getInstance().HemoCapabilityAccess.getUnstainedProgress(player).ifPresent(cap -> {
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

	private ProgressScreenContext makeContext() {
		return new ProgressScreenContext(font, guiLeft, guiTop, guiWidth, guiHeight, 0);
	}

	// ────────────────────────────────────────────────────────────
	//  Pan / zoom helpers
	// ────────────────────────────────────────────────────────────

	private int contentW() {
		return switch (activeTab) {
			case PROGRESS  -> contentW;
			case MATERIALS -> materialsTab.getContentW();
			default        -> 0; // RITES / CRAFTING don't pan
		};
	}
	private int contentH() {
		return switch (activeTab) {
			case PROGRESS  -> contentH;
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
	public boolean mouseScrolled(double mx, double my, double delta) {
		if (!insideGui(mx, my)) return super.mouseScrolled(mx, my, delta);

		if (activeTab == UTab.RITES) return ritesTab.mouseScrolled(makeContext(), mx, my, delta);
		if (activeTab == UTab.CRAFTING) return craftingTab.mouseScrolled(makeContext(), mx, my, delta);

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

		// 1. Dark blue background with floating hollow rhombuses
		backgroundRenderer.render(gfx, guiLeft, guiTop, guiWidth, guiHeight);

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
			materialsTab.renderTooltip(gfx, makeContext(), mouseX, mouseY);
		}

		super.render(gfx, mouseX, mouseY, partial);
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
	//  Misc
	// ────────────────────────────────────────────────────────────

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
