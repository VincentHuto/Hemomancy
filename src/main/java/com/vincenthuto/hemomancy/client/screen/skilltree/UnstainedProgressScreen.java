package com.vincenthuto.hemomancy.client.screen.skilltree;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumPurityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressProvider;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.PacketToggleUnstainedBonus;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
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

	// ── Pan / zoom ──
	private double panX, panY;
	private float zoom = 1.0f;
	private static final float ZOOM_MIN = 0.35f;
	private static final float ZOOM_MAX = 3.0f;
	private boolean isDragging;
	private static final int PAN_MARGIN = 200;

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

		int margin = 16;
		guiLeft   = margin;
		guiTop    = margin;
		guiWidth  = width  - margin * 2;
		guiHeight = height - margin * 2;

		clearWidgets();
		cachePlayerData();
		buildContentBounds();
		seedRhombusParams();

		// Centre content
		panX = (guiWidth  - contentW * zoom) / 2.0;
		panY = (guiHeight - contentH * zoom) / 2.0;
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
	//  Coordinate helpers (content ↔ screen)
	// ────────────────────────────────────────────────────────────

	/** Content-space → screen-space X */
	private int sx(int contentX) { return (int)(guiLeft + panX + contentX * zoom); }
	/** Content-space → screen-space Y */
	private int sy(int contentY) { return (int)(guiTop  + panY + contentY * zoom); }

	/** Screen-space → content-space X */
	private double cx(double screenX) { return (screenX - guiLeft - panX) / zoom; }
	/** Screen-space → content-space Y */
	private double cy(double screenY) { return (screenY - guiTop  - panY) / zoom; }

	/** Half-node size on screen, accounting for zoom */
	private int halfNode() { return Math.max(4, (int)(NODE_SIZE * zoom / 2)); }

	private boolean insideGui(double mx, double my) {
		return mx >= guiLeft && mx < guiLeft + guiWidth
			&& my >= guiTop  && my < guiTop  + guiHeight;
	}

	// ────────────────────────────────────────────────────────────
	//  Pan / zoom helpers
	// ────────────────────────────────────────────────────────────

	private void clampPan() {
		double scaledW = contentW * zoom;
		double scaledH = contentH * zoom;

		double minPanX = -scaledW + (-PAN_MARGIN);
		double maxPanX = guiWidth + PAN_MARGIN;
		panX = Mth.clamp(panX, minPanX, maxPanX);

		double minPanY = -scaledH + (-PAN_MARGIN);
		double maxPanY = guiHeight + PAN_MARGIN;
		panY = Mth.clamp(panY, minPanY, maxPanY);
	}

	private void resetToHome() {
		zoom = 1.0f;
		panX = (guiWidth  - contentW * zoom) / 2.0;
		panY = (guiHeight - contentH * zoom) / 2.0;
		clampPan();
	}

	// ────────────────────────────────────────────────────────────
	//  Input: drag-to-pan, scroll-to-zoom, home button
	// ────────────────────────────────────────────────────────────

	@Override
	public boolean mouseClicked(double mx, double my, int btn) {
		if (btn == 0) {
			if (isOverHomeButton(mx, my)) {
				resetToHome();
				return true;
			}
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
		}
		return super.mouseReleased(mx, my, btn);
	}

	@Override
	public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
		if (isDragging && btn == 0) {
			panX += dx;
			panY += dy;
			clampPan();
			return true;
		}
		return super.mouseDragged(mx, my, btn, dx, dy);
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double delta) {
		if (!insideGui(mx, my)) return super.mouseScrolled(mx, my, delta);

		double cxBefore = cx(mx);
		double cyBefore = cy(my);

		float oldZoom = zoom;
		zoom = Mth.clamp(zoom + (float) delta * 0.15f, ZOOM_MIN, ZOOM_MAX);

		panX += cxBefore * (oldZoom - zoom);
		panY += cyBefore * (oldZoom - zoom);
		clampPan();
		return true;
	}

	// ────────────────────────────────────────────────────────────
	//  Render
	// ────────────────────────────────────────────────────────────

	@Override
	public void render(GuiGraphics gfx, int mouseX, int mouseY, float partial) {
		renderBackground(gfx);

		cachePlayerData();
		buildContentBounds();

		// 1. Dark blue background with floating hollow rhombuses
		renderDiamondBackground(gfx, guiLeft, guiTop, guiWidth, guiHeight);

		// 2. Border
		drawBorder(gfx, guiLeft, guiTop, guiWidth, guiHeight);

		// 3. Scissored content area
		gfx.enableScissor(guiLeft + 2, guiTop + 2,
				guiLeft + guiWidth - 2, guiTop + guiHeight - 2);

		if (!begunPurification) {
			drawNotBegunMessage(gfx);
		} else {
			drawPurityColumn(gfx);
			if (clarityUnlocked) {
				drawClarityColumn(gfx);
			}
			drawStatReadouts(gfx);
		}

		gfx.disableScissor();

		// 4. Milestones sidebar (fixed on far left, outside content scissor)
		if (begunPurification) {
			drawMilestones(gfx, mouseX, mouseY);
		}

		// 4b. Bonus toggle buttons (fixed on far right, outside content scissor)
		if (begunPurification) {
			drawBonusToggleButtons(gfx, mouseX, mouseY);
		}

		// 5. Home button (outside scissor)
		drawHomeButton(gfx, mouseX, mouseY);

		// 5. Title
		gfx.drawCenteredString(font,
				Component.literal("Unstained Progress"),
				guiLeft + guiWidth / 2, guiTop + 5, 0xFFB0C0E0);

		// 6. Zoom indicator (hidden when milestones sidebar is open)
		if (!sidebarVisible || !begunPurification) {
			gfx.drawString(font,
					String.format("%.0f%%", zoom * 100),
					guiLeft + 5, guiTop + guiHeight - 12, 0x55888888, false);
		}

		// 7. Tooltips (outside scissor)
		if (begunPurification) {
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
		if (zoom >= 0.4f) {
			gfx.drawCenteredString(font, Component.literal("\u2014 Purity \u2014"),
					scrCenterX, sy(10), PURITY_COLOR);

			// Progress bar
			int barW = Math.max(20, (int)(100 * zoom));
			drawProgressBar(gfx, scrCenterX - barW / 2, sy(22), barW, Math.max(4, (int)(8 * zoom)),
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
			int lw = Math.max(1, (int)(zoom * 1.5f));
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

		if (zoom >= 0.4f) {
			gfx.drawCenteredString(font, Component.literal("\u2014 Clarity \u2014"),
					scrCenterX, sy(10), CLARITY_COLOR);

			int barW = Math.max(20, (int)(100 * zoom));
			drawProgressBar(gfx, scrCenterX - barW / 2, sy(22), barW, Math.max(4, (int)(8 * zoom)),
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
			int lw = Math.max(1, (int)(zoom * 1.5f));
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
		if (zoom >= 0.5f) {
			if (iconTexture != null) {
				renderScaledTexture(gfx, iconTexture, scrX, scrY, hn);
			} else if (iconStack != null && !iconStack.isEmpty()) {
				renderScaledItem(gfx, iconStack, scrX, scrY, hn);
			} else {
				String initial = !title.isEmpty() ? title.substring(0, 1).toUpperCase() : "?";
				int textCol = isCurrent ? 0xFFFFFFFF : (reached ? 0xFFA0B8D8 : 0xFF404858);
				gfx.drawCenteredString(font, initial, scrX, scrY - 4, textCol);
			}
		}

		// Stage name below node
		if (zoom >= 0.4f) {
			int labelCol = isCurrent ? accentColor : (reached ? 0xFF7090B0 : 0xFF384050);
			gfx.drawCenteredString(font, title, scrX, scrY + hn + 4, labelCol);
		}
	}

	/**
	 * Draws a diamond outline (1px border) by rendering edge pixels only.
	 */
	private void drawDiamondOutline(GuiGraphics gfx, int cx, int cy, int halfSize, int color) {
		for (int row = -halfSize; row <= halfSize; row++) {
			int w = halfSize - Math.abs(row);
			if (w <= 0) {
				// Single pixel at tip
				gfx.fill(cx, cy + row, cx + 1, cy + row + 1, color);
				continue;
			}
			// Left edge pixel
			gfx.fill(cx - w, cy + row, cx - w + 1, cy + row + 1, color);
			// Right edge pixel
			gfx.fill(cx + w - 1, cy + row, cx + w, cy + row + 1, color);
			// Top and bottom tip rows are fully filled
			if (Math.abs(row) >= halfSize - 1) {
				gfx.fill(cx - w, cy + row, cx + w, cy + row + 1, color);
			}
		}
	}

	/**
	 * Draws a filled diamond/rhombus shape at the given center point.
	 */
	private void drawDiamond(GuiGraphics gfx, int cx, int cy, int halfSize, int color) {
		for (int row = -halfSize; row <= halfSize; row++) {
			int w = halfSize - Math.abs(row);
			if (w <= 0) continue;
			gfx.fill(cx - w, cy + row, cx + w, cy + row + 1, color);
		}
	}

	/** Padding pixels around the item icon inside a node. */
	private static final int ITEM_PADDING = 4;

	/**
	 * Renders an ItemStack centred inside a node, scaled to fit within the node bounds.
	 * Items render at 16x16 by default; this scales them to fit (2*hn - ITEM_PADDING) pixels.
	 */
	private void renderScaledItem(GuiGraphics gfx, ItemStack stack, int centerX, int centerY, int halfNodeSize) {
		float nodeInner = Math.max(ITEM_PADDING, halfNodeSize * 2 - ITEM_PADDING);
		float itemScale = nodeInner / 16.0f;
		PoseStack pose = gfx.pose();
		pose.pushPose();
		pose.translate(centerX - nodeInner / 2.0f, centerY - nodeInner / 2.0f, 0);
		pose.scale(itemScale, itemScale, 1);
		gfx.renderItem(stack, 0, 0);
		pose.popPose();
	}

	/**
	 * Renders a texture from any ResourceLocation centred inside a node, scaled to fit
	 * within the node bounds. The entire texture is sampled (UV 0→1) and rendered at the
	 * node's inner size, so it works for textures of any pixel dimensions.
	 */
	private void renderScaledTexture(GuiGraphics gfx, ResourceLocation texture, int centerX, int centerY, int halfNodeSize) {
		int nodeInner = Math.max(ITEM_PADDING, halfNodeSize * 2 - ITEM_PADDING);
		int x = centerX - nodeInner / 2;
		int y = centerY - nodeInner / 2;
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		gfx.blit(texture, x, y, 0, 0, nodeInner, nodeInner, nodeInner, nodeInner);
		RenderSystem.disableBlend();
	}

	// ────────────────────────────────────────────────────────────
	//  Progress bar
	// ────────────────────────────────────────────────────────────

	private void drawProgressBar(GuiGraphics gfx, int x, int y, int w, int h,
								 float value, float max, int fillColorRGB) {
		float time = System.nanoTime() / 1_000_000_000f;
		float ratio = Mth.clamp(value / max, 0f, 1f);
		int fillW = (int)(w * ratio);

		// Background
		gfx.fill(x, y, x + w, y + h, 0xFF0C1020);

		// Frame
		int frameCol = 0xFF203040;
		gfx.fill(x, y, x + w, y + 1, frameCol);
		gfx.fill(x, y + h - 1, x + w, y + h, frameCol);
		gfx.fill(x, y, x + 1, y + h, frameCol);
		gfx.fill(x + w - 1, y, x + w, y + h, frameCol);

		// Fill with pulse
		if (fillW > 0) {
			float r = ((fillColorRGB >> 16) & 0xFF) / 255f;
			float g = ((fillColorRGB >> 8) & 0xFF) / 255f;
			float b = (fillColorRGB & 0xFF) / 255f;

			for (int col = 0; col < fillW; col++) {
				float colT = (float) col / Math.max(w, 1);
				float brightness = 0.6f + 0.4f * colT;
				float pulse = 0.9f + 0.1f * Mth.sin(time * 2.0f + col * 0.08f);
				float fb = brightness * pulse;

				int cr = (int) Mth.clamp(r * fb * 255, 0, 255);
				int cg = (int) Mth.clamp(g * fb * 255, 0, 255);
				int cb = (int) Mth.clamp(b * fb * 255, 0, 255);
				int color = 0xFF000000 | (cr << 16) | (cg << 8) | cb;
				gfx.fill(x + 1 + col, y + 1, x + 1 + col + 1, y + h - 1, color);
			}
		}

		// 25% notch marks
		for (int notch = 1; notch <= 3; notch++) {
			int nx = x + w * notch / 4;
			gfx.fill(nx, y, nx + 1, y + h, 0x30FFFFFF);
		}
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
		int divW = Math.max(30, (int)(200 * zoom));
		gfx.fill(centerScrX - divW / 2, scrY - 4, centerScrX + divW / 2, scrY - 3, 0x33405878);

		if (zoom >= 0.4f) {
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
		if (!insideGui(mouseX, mouseY) || zoom < 0.4f) return;

		EnumPurityStage[] stages = EnumPurityStage.values();
		int statCY = nodeStartCY() + stages.length * NODE_GAP_Y + 20;
		int centerScrX = sx(contentW / 2);
		int scrY = sy(statCY);
		int halfTextW = (int)(100 * zoom);  // approximate half-width of text hit area

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
			tip.add(Component.literal("Tainted: -10%  \u2022  Cleansing: -25%")
					.withStyle(s -> s.withColor(0x807060).withItalic(true)));
			tip.add(Component.literal("Absolved: -50%  \u2022  Purified: -100%")
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
			int bx = px;
			int by = py + i * (BONUS_BTN_SIZE + BONUS_BTN_GAP);
			if (mx >= bx && mx <= bx + BONUS_BTN_SIZE && my >= by && my <= by + BONUS_BTN_SIZE) {
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
		drawDiamond(gfx, cx, cy, halfDia, bgCol);

		// Border
		int borderCol;
		if (!unlocked) {
			borderCol = COL_NODE_LOCKED;
		} else if (enabled) {
			borderCol = hovered ? accent : COL_NODE_REACHED;
		} else {
			borderCol = hovered ? 0xFF505868 : 0xFF383E4C;
		}
		drawDiamondOutline(gfx, cx, cy, halfDia, borderCol);

		// Pulsing glow when enabled and unlocked
		if (enabled && unlocked) {
			float time = System.nanoTime() / 1_000_000_000f;
			float p = 0.3f + 0.3f * Mth.sin(time * 2.0f);
			int ga = (int)(30 * p);
			int gr = (glow >> 16) & 0xFF;
			int gg = (glow >> 8) & 0xFF;
			int gb = glow & 0xFF;
			drawDiamond(gfx, cx, cy, halfDia + 2, (ga << 24) | (gr << 16) | (gg << 8) | gb);
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
				tip.add(Component.literal("Scales with Purity (purity \u00F7 100).")
						.withStyle(s -> s.withColor(0x607090).withItalic(true)));
				tip.add(Component.literal(String.format("Current: %.0f%%", silverWardStrength * 100))
						.withStyle(s -> s.withColor(0x60A0CC).withItalic(true)));
				tip.add(Component.literal(""));
				String state = silverWardEnabled ? "\u2714 Enabled" : "\u2716 Disabled";
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
			tip.add(Component.literal("Scales with Clarity (clarity \u00F7 100).")
					.withStyle(s -> s.withColor(0x508878).withItalic(true)));
			tip.add(Component.literal(String.format("Current: %.0f%%", verdigrisAura * 100))
					.withStyle(s -> s.withColor(0x50B0A0).withItalic(true)));
			tip.add(Component.literal(""));
			String state = verdigrisAuraEnabled ? "\u2714 Enabled" : "\u2716 Disabled";
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
		y = drawMilestoneBar(gfx, x, barX, y, barW, barH,
				"Pets Healed", mPetsHealed, 15, 0xFFA0D0B0);

		gfx.disableScissor();
	}

	/**
	 * Draws the sidebar toggle tab — a small clickable arrow on the edge.
	 * When expanded: tab is on the right edge of the panel, arrow points left (collapse).
	 * When collapsed: tab is on the left edge of the GUI, arrow points right (expand).
	 */
	public  void drawSidebarToggleTab(GuiGraphics gfx, int tabX, int tabY, boolean expanded, boolean hovered) {
		int tw = SIDEBAR_TAB_W;
		int th = SIDEBAR_TAB_H;

		// Background — brighter when hovered
		gfx.fill(tabX, tabY, tabX + tw, tabY + th, hovered ? 0xDD101828 : 0xCC0C1020);

		// Border — highlight on hover
		int bc = hovered ? 0xFF4070A0 : 0xFF203050;
		gfx.fill(tabX, tabY, tabX + tw, tabY + 1, bc);
		gfx.fill(tabX, tabY + th - 1, tabX + tw, tabY + th, bc);
		gfx.fill(tabX, tabY, tabX + 1, tabY + th, bc);
		gfx.fill(tabX + tw - 1, tabY, tabX + tw, tabY + th, bc);

		// Arrow character: ◀ when expanded (click to collapse), ▶ when collapsed (click to expand)
		String arrow = expanded ? "\u25C0" : "\u25B6";
		int arrowCol = hovered ? 0xFFB0C8E8 : 0xFF8098C0;
		gfx.drawCenteredString(font, arrow, tabX + tw / 2, tabY + th / 2 - 4, arrowCol);
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
		String icon = complete ? "\u2713" : "\u2717";
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
		String countStr = current >= goal ? (current + "/" + goal + " \u2713") : (current + "/" + goal);
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
					tip.add(Component.literal("\u25B8 Current Stage")
							.withStyle(s -> s.withColor(0x60A0CC).withItalic(true)));
				} else if (reached) {
					tip.add(Component.literal("\u2713 Achieved")
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
					tip.add(Component.literal("\u25B8 Current Stage")
							.withStyle(s -> s.withColor(0x50B0A0).withItalic(true)));
				} else if (reached) {
					tip.add(Component.literal("\u2713 Achieved")
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
		int bs = HOME_BTN_SIZE;
		boolean hovered = isOverHomeButton(mouseX, mouseY);

		// Background
		int bg = hovered ? 0xDD101828 : 0x990C1020;
		gfx.fill(bx, by, bx + bs, by + bs, bg);

		// Border
		int bc = hovered ? 0xFF6088C0 : 0xFF304060;
		gfx.fill(bx, by, bx + bs, by + 1, bc);
		gfx.fill(bx, by + bs - 1, bx + bs, by + bs, bc);
		gfx.fill(bx, by, bx + 1, by + bs, bc);
		gfx.fill(bx + bs - 1, by, bx + bs, by + bs, bc);

		// House icon
		int textCol = hovered ? 0xFFB0C0E0 : 0xFF506888;
		gfx.drawCenteredString(font, "\u2302", bx + bs / 2, by + (bs - 8) / 2, textCol);

		// Tooltip on hover
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
		gfx.fill(x, y, x + w, y + 1, COL_BORDER_OUTER);
		gfx.fill(x, y + h - 1, x + w, y + h, COL_BORDER_OUTER);
		gfx.fill(x, y, x + 1, y + h, COL_BORDER_OUTER);
		gfx.fill(x + w - 1, y, x + w, y + h, COL_BORDER_OUTER);

		gfx.fill(x + 1, y + 1, x + w - 1, y + 2, COL_BORDER_INNER);
		gfx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, COL_BORDER_INNER);
		gfx.fill(x + 1, y + 1, x + 2, y + h - 1, COL_BORDER_INNER);
		gfx.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, COL_BORDER_INNER);
	}

	// ────────────────────────────────────────────────────────────
	//  Misc
	// ────────────────────────────────────────────────────────────

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
