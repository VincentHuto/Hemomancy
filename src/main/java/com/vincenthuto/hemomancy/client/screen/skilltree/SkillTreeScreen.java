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
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.common.capability.player.skill.EnumSkillStates;
import com.vincenthuto.hemomancy.common.capability.player.skill.SkillPoint;
import com.vincenthuto.hemomancy.common.init.ManipulationTreeInit;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.PacketUnlockSkill;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

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

	// ── Textures ──
	private static final ResourceLocation BACKGROUND_TEX = new ResourceLocation(
			Hemomancy.MOD_ID, "textures/gui/tendency_view.png");
	private static final ResourceLocation BORDER_TEX = new ResourceLocation(
			Hemomancy.MOD_ID, "textures/gui/tendency_border.png");

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

	// ── Pan / zoom ──
	private double panX, panY;
	private float zoom = 1.0f;
	private static final float ZOOM_MIN = 0.35f;
	private static final float ZOOM_MAX = 3.0f;
	private boolean isDragging;

	// ── Vein background (same system as TendencyViewScreen) ──
	private static final int VEIN_COUNT = 28;
	private float[][] veinParams;

	// ── Skill tree data ──
	private final Map<SkillPoint, int[]> nodePositions = new HashMap<>();
	private int contentW, contentH;

	// ── Manipulation tree data ──
	private final Map<ManipulationTreeEntry, int[]> manipPositions = new HashMap<>();
	private final Set<String> knownManipNames = new HashSet<>();

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

		// Centre the content in the viewport
		panX = (guiWidth  - contentW * zoom) / 2.0;
		panY = (guiHeight - contentH * zoom) / 2.0;
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
		contentW = 0;
		contentH = 0;

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
				int y  = 40 + d * NODE_GAP_Y;
				for (int i = 0; i < n; i++) {
					int x = x0 + i * NODE_GAP_X;
					nodePositions.put(row.get(i), new int[]{x, y});
					contentW = Math.max(contentW, x + NODE_SIZE);
					contentH = Math.max(contentH, y + NODE_SIZE + 24);
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
		if (ManipulationTreeInit.ENTRIES.isEmpty()) ManipulationTreeInit.init();

		for (ManipulationTreeEntry entry : ManipulationTreeInit.ENTRIES) {
			int x = entry.getX();
			int y = entry.getY();
			manipPositions.put(entry, new int[]{x, y});
			contentW = Math.max(contentW, x + NODE_SIZE + 20);
			contentH = Math.max(contentH, y + NODE_SIZE + 24);
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
		if (btn == 0 && insideGui(mx, my)) {
			SkillPoint hit = nodeUnder(mx, my);
			if (hit != null) {
				tryUnlock(hit);
				return true;
			}
			isDragging = true;
			return true;
		}
		return super.mouseClicked(mx, my, btn);
	}

	@Override
	public boolean mouseReleased(double mx, double my, int btn) {
		if (btn == 0) isDragging = false;
		return super.mouseReleased(mx, my, btn);
	}

	@Override
	public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
		if (isDragging && btn == 0) {
			panX += dx;
			panY += dy;
			return true;
		}
		return super.mouseDragged(mx, my, btn, dx, dy);
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double delta) {
		if (!insideGui(mx, my)) return super.mouseScrolled(mx, my, delta);

		// Remember the content-space point under the cursor
		double cxBefore = cx(mx);
		double cyBefore = cy(my);

		float oldZoom = zoom;
		zoom = Mth.clamp(zoom + (float) delta * 0.15f, ZOOM_MIN, ZOOM_MAX);

		// Adjust pan so that point stays under the cursor
		panX += cxBefore * (oldZoom - zoom);
		panY += cyBefore * (oldZoom - zoom);
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

		// ── 1. Animated vein background (scissored to GUI bounds) ──
		renderVeinBackground(gfx, guiLeft, guiTop, guiWidth, guiHeight);

		// ── 2. Border ──
		drawBorder(gfx, guiLeft, guiTop, guiWidth, guiHeight);

		// ── 3. Tree content (scissored so it clips when panned) ──
		gfx.enableScissor(guiLeft + 2, guiTop + 2,
				guiLeft + guiWidth - 2, guiTop + guiHeight - 2);
		drawConnections(gfx);
		drawNodes(gfx);
		drawManipConnections(gfx);
		drawManipNodes(gfx);

		// Section labels (inside scissor so they pan with content)
		drawSectionLabels(gfx);
		gfx.disableScissor();

		// ── 4. Overlay text ──
		gfx.drawCenteredString(font,
				Component.translatable("screen.hemomancy.skill_tree"),
				guiLeft + guiWidth / 2, guiTop + 5, 0xFFCC3333);

		gfx.drawString(font,
				String.format("%.0f%%", zoom * 100),
				guiLeft + 5, guiTop + guiHeight - 12, 0x55888888, false);

		// ── 5. Tooltip (must be outside scissor) ──
		drawTooltip(gfx, mouseX, mouseY);
		drawManipTooltip(gfx, mouseX, mouseY);

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

			// ── determine border colour ──
			int border;
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

			// ── node fill ──
			gfx.fill(nx - hn, ny - hn, nx + hn, ny + hn, COL_NODE_BG);

			// ── border (1-px lines) ──
			gfx.fill(nx - hn, ny - hn, nx + hn, ny - hn + 1, border);
			gfx.fill(nx - hn, ny + hn - 1, nx + hn, ny + hn, border);
			gfx.fill(nx - hn, ny - hn, nx - hn + 1, ny + hn, border);
			gfx.fill(nx + hn - 1, ny - hn, nx + hn, ny + hn, border);

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
					tip.add(Component.literal("Click to unlock! Cost: " + (int) sp.getLevelUpCost() + " mL")
							.withStyle(s -> s.withColor(0xBB8833)));
				}
			} else if (sp.getState() == EnumSkillStates.UNLOCKED) {
				if (sp.isMaxed()) {
					tip.add(Component.literal("MAX LEVEL")
							.withStyle(s -> s.withColor(0x44AA44).withBold(true)));
				} else {
					tip.add(Component.literal("Click to level up! Cost: " + (int) sp.getLevelUpCost() + " mL")
							.withStyle(s -> s.withColor(0xBB8833)));
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
	//  Section labels (rendered in content-space, inside scissor)
	// ────────────────────────────────────────────────────────────

	private void drawSectionLabels(GuiGraphics gfx) {
		if (zoom < 0.4f) return;

		// "Skills" label over skill tree
		int skillLabelX = sx(contentW / 4);   // approximate centre of skill area
		int skillLabelY = sy(15);
		gfx.drawCenteredString(font, Component.literal("Skills")
				.withStyle(s -> s.withColor(0xCC3333).withBold(true)), skillLabelX, skillLabelY, 0xFFCC3333);

		// "Manipulations" label over manipulation tree
		int manipCenterX = ManipulationTreeInit.TREE_OFFSET_X + 260;
		int manipLabelX = sx(manipCenterX);
		int manipLabelY = sy(15);
		gfx.drawCenteredString(font, Component.literal("Manipulations")
				.withStyle(s -> s.withColor(0xCC8833).withBold(true)), manipLabelX, manipLabelY, 0xFFCC8833);
	}

	// ────────────────────────────────────────────────────────────
	//  Helpers
	// ────────────────────────────────────────────────────────────

	private static String getSkillInitial(SkillPoint sp) {
		return switch (sp.getName()) {
			case "base"                -> "\u2726";
			case "skill_capacity"      -> "C";
			case "skill_efficiency"    -> "E";
			case "skill_last_wind"     -> "W";
			case "skill_dynamic_use"   -> "D";
			case "skill_feeding_frenzy"-> "F";
			default                    -> "?";
		};
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
