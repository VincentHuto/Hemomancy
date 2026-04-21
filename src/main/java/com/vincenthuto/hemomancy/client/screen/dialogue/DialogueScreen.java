package com.vincenthuto.hemomancy.client.screen.dialogue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueNode;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueOption;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTheme;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.dialogue.DialogueOptionPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Custom non-traditional dialogue screen. Renders on the left side of the
 * screen with a portrait of the talking entity, their dialogue lines, and
 * clickable response options for the player.
 * <p>
 * The visual theme is selected by the {@link DialogueTheme} carried in the
 * {@link DialogueTree}:
 * <ul>
 *   <li><b>BLOOD</b> – Red/crimson veiny tendrils (default / Harbinger Hermit).</li>
 *   <li><b>UNSTAINED</b> – Dark blue with floating hollow rhombuses and
 *       blue-white glow (Zealots / Unstained faction).</li>
 *   <li><b>FUNGAL</b> – Dark amber with orange fungal tendrils and floating
 *       spore particles (Fungal Whispers).</li>
 * </ul>
 */
@OnlyIn(Dist.CLIENT)
public class DialogueScreen extends Screen {

	// ── Layout constants ──
	private static final int PANEL_WIDTH = 260;
	private static final int PANEL_MARGIN = 8;
	private static final int PORTRAIT_SIZE = 48;
	private static final int LINE_SPACING = 12;
	private static final int OPTION_SPACING = 16;
	private static final int OPTION_LEFT_PAD = 14;
	private static final int TEXT_LEFT_PAD = 10;

	// ── Portrait texture coordinates ──
	private static final int TEXTURE_HEAD_U = 8;
	private static final int TEXTURE_HEAD_V = 8;
	private static final int TEXTURE_HEAD_SIZE = 8;
	private static final int TEXTURE_WIDTH = 64;
	private static final int TEXTURE_HEIGHT = 64;

	// ── Particle counts ──
	private static final int BLOOD_VEIN_COUNT = 14;
	private static final int UNSTAINED_RHOMBUS_COUNT = 16;
	private static final int FUNGAL_TENDRIL_COUNT = 14;
	private static final int FUNGAL_SPORE_COUNT = 30;

	// ────────────────────────────────────────────────────────────
	//  Theme-dependent colour palette
	// ────────────────────────────────────────────────────────────

	private record Palette(
			int bgColor,
			int borderOuter,
			int borderInner,
			int speakerColor,
			int lineColor,
			int optionColor,
			int optionHoverColor,
			int separatorColor,
			int portraitBacking
	) {}

	private static final Palette BLOOD_PALETTE = new Palette(
			0xE00A0204, 0xFF330808, 0xFF220606,
			0xFFCC3344, 0xFFBBAAAA,
			0xFFDD9966, 0xFFFFCC88,
			0x44CC3344, 0xFF0A0204
	);

	private static final Palette UNSTAINED_PALETTE = new Palette(
			0xE0060A1E, 0xFF203050, 0xFF101828,
			0xFFB0C0E0, 0xFFBBBBCC,
			0xFF8098C0, 0xFFDDEEFF,
			0x44506080, 0xFF060A1E
	);

	private static final Palette FUNGAL_PALETTE = new Palette(
			0xE01A0D04, 0xFF663300, 0xFF4A2200,
			0xFFDD8822, 0xFFBBAA99,
			0xFFCC8844, 0xFFFFCC66,
			0x44AA4400, 0xFF1A0D04
	);

	// ── State ──
	private final DialogueTree tree;
	private final Palette palette;
	private DialogueNode currentNode;
	private final List<OptionRect> optionRects = new ArrayList<>();

	// Background animation data (allocated per-theme in init())
	private float[][] bloodVeinParams;
	private float[][] unstainedRhombusParams;
	private float[][] fungalTendrilParams;
	private float[][] fungalSporeParams;

	private DialogueScreen(DialogueTree tree) {
		super(Component.empty());
		this.tree = tree;
		this.currentNode = tree.getStartNode();
		this.palette = switch (tree.theme()) {
			case UNSTAINED -> UNSTAINED_PALETTE;
			case FUNGAL -> FUNGAL_PALETTE;
			default -> BLOOD_PALETTE;
		};
	}

	public static void open(DialogueTree tree) {
		Minecraft.getInstance().setScreen(new DialogueScreen(tree));
		// Trigger blood vignette flash for disembodied fungal whispers
		if (tree.entityId() == 0
				&& com.vincenthuto.hemomancy.client.screen.overlay.FungalWhisperVignetteOverlay.instance != null) {
			com.vincenthuto.hemomancy.client.screen.overlay.FungalWhisperVignetteOverlay.instance.trigger();
		}
	}

	// ──────────────────────────────────────────────

	@Override
	protected void init() {
		super.init();
		rebuildOptions();
		initBackgroundParams();
	}

	private void initBackgroundParams() {
		switch (tree.theme()) {
			case UNSTAINED -> {
				Random rand = new Random(54321L);
				unstainedRhombusParams = new float[UNSTAINED_RHOMBUS_COUNT][8];
				for (int i = 0; i < UNSTAINED_RHOMBUS_COUNT; i++) {
					unstainedRhombusParams[i][0] = rand.nextFloat();                      // startX ratio
					unstainedRhombusParams[i][1] = rand.nextFloat();                      // startY ratio
					unstainedRhombusParams[i][2] = 4 + rand.nextInt(10);                  // halfSize
					unstainedRhombusParams[i][3] = -8f + rand.nextFloat() * 16f;          // velX
					unstainedRhombusParams[i][4] = -6f + rand.nextFloat() * 12f;          // velY
					unstainedRhombusParams[i][5] = rand.nextFloat() * 100f;               // phase
					unstainedRhombusParams[i][6] = 0.3f + rand.nextFloat() * 0.7f;        // brightness
					unstainedRhombusParams[i][7] = 0.3f + rand.nextFloat() * 1.2f;        // rotSpeed
				}
			}
			case FUNGAL -> {
				Random rand = new Random(0xF04A1L);
				fungalTendrilParams = new float[FUNGAL_TENDRIL_COUNT][9];
				for (int i = 0; i < FUNGAL_TENDRIL_COUNT; i++) {
					fungalTendrilParams[i][0] = rand.nextFloat();
					fungalTendrilParams[i][1] = rand.nextFloat();
					fungalTendrilParams[i][2] = (float) (rand.nextFloat() * Math.PI * 2);
					fungalTendrilParams[i][3] = 0.2f + rand.nextFloat() * 0.6f;
					fungalTendrilParams[i][4] = 6f + rand.nextFloat() * 14f;
					fungalTendrilParams[i][5] = 0.05f + rand.nextFloat() * 0.1f;
					fungalTendrilParams[i][6] = 30 + rand.nextInt(60);
					fungalTendrilParams[i][7] = 1;
					fungalTendrilParams[i][8] = rand.nextFloat();
				}
				fungalSporeParams = new float[FUNGAL_SPORE_COUNT][5];
				for (int i = 0; i < FUNGAL_SPORE_COUNT; i++) {
					fungalSporeParams[i][0] = rand.nextFloat();
					fungalSporeParams[i][1] = rand.nextFloat();
					fungalSporeParams[i][2] = 0.3f + rand.nextFloat() * 0.7f;
					fungalSporeParams[i][3] = rand.nextFloat() * 100f;
					fungalSporeParams[i][4] = rand.nextFloat();
				}
			}
			default -> {
				Random rand = new Random(9991L);
				bloodVeinParams = new float[BLOOD_VEIN_COUNT][9];
				for (int i = 0; i < BLOOD_VEIN_COUNT; i++) {
					bloodVeinParams[i][0] = rand.nextFloat();
					bloodVeinParams[i][1] = rand.nextFloat();
					bloodVeinParams[i][2] = (float) (rand.nextFloat() * Math.PI * 2);
					bloodVeinParams[i][3] = 0.3f + rand.nextFloat() * 0.7f;
					bloodVeinParams[i][4] = 4f + rand.nextFloat() * 10f;
					bloodVeinParams[i][5] = 0.06f + rand.nextFloat() * 0.08f;
					bloodVeinParams[i][6] = 30 + rand.nextInt(60);
					bloodVeinParams[i][7] = 1;
					bloodVeinParams[i][8] = rand.nextFloat();
				}
			}
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	// ──────────────────────────────────────────────

	@Override
	public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
		// Do NOT call renderBackground — we want the world visible behind the panel.

		int panelX = PANEL_MARGIN;
		int panelY = PANEL_MARGIN;
		int panelH = this.height - PANEL_MARGIN * 2;
		int panelW = Math.min(PANEL_WIDTH, this.width - PANEL_MARGIN * 2);

		// ── Theme background ──
		renderThemedBackground(gfx, panelX, panelY, panelW, panelH);

		// ── Border ──
		drawBorder(gfx, panelX, panelY, panelW, panelH);

		Font font = this.font;
		int contentX = panelX + TEXT_LEFT_PAD;
		int contentW = panelW - TEXT_LEFT_PAD * 2;
		int y = panelY + 10;

		gfx.enableScissor(panelX, panelY, panelX + panelW, panelY + panelH);

		// ── Speaker portrait ──
		int portraitX = contentX;
		int portraitY = y;
		renderPortrait(gfx, portraitX, portraitY);

		int nameX = portraitX + PORTRAIT_SIZE + 8;
		Component speakerName = Component.translatable(tree.speakerName());
		gfx.drawString(font, speakerName, nameX, portraitY + 4, palette.speakerColor, true);

		// Thin separator
		int sepY = portraitY + PORTRAIT_SIZE + 6;
		gfx.fill(contentX, sepY, contentX + contentW, sepY + 1, palette.separatorColor);
		y = sepY + 8;

		// ── Dialogue lines ──
		if (currentNode != null) {
			for (String lineKey : currentNode.lines()) {
				Component line = Component.translatable(lineKey);
				List<net.minecraft.util.FormattedCharSequence> wrapped = font.split(line, contentW);
				for (var seq : wrapped) {
					gfx.drawString(font, seq, contentX, y, palette.lineColor, false);
					y += LINE_SPACING;
				}
				y += 4;
			}

			y += 8;
			optionRects.clear();
			int optX = contentX + OPTION_LEFT_PAD;
			int optW = contentW - OPTION_LEFT_PAD;
			for (int i = 0; i < currentNode.options().size(); i++) {
				DialogueOption opt = currentNode.options().get(i);
				Component optText = Component.literal("> ").append(Component.translatable(opt.text()));
				List<net.minecraft.util.FormattedCharSequence> wrapped = font.split(optText, optW);

				int optTop = y;
				boolean hovered = mouseX >= optX && mouseX <= optX + optW
						&& mouseY >= optTop && mouseY < optTop + wrapped.size() * LINE_SPACING;
				int col = hovered ? palette.optionHoverColor : palette.optionColor;

				for (var seq : wrapped) {
					gfx.drawString(font, seq, optX, y, col, false);
					y += LINE_SPACING;
				}
				optionRects.add(new OptionRect(optX, optTop, optX + optW, y, i));
				y += OPTION_SPACING - LINE_SPACING;
			}
		}

		gfx.disableScissor();
		super.render(gfx, mouseX, mouseY, partialTick);
	}

	// ──────────────────────────────────────────────

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && currentNode != null) {
			for (OptionRect rect : optionRects) {
				if (mouseX >= rect.x1 && mouseX <= rect.x2
						&& mouseY >= rect.y1 && mouseY <= rect.y2) {
					selectOption(rect.index);
					return true;
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	private void selectOption(int index) {
		if (currentNode == null || index < 0 || index >= currentNode.options().size()) return;
		DialogueOption opt = currentNode.options().get(index);

		if (opt.eventId() != null && !opt.eventId().isEmpty()) {
			PacketHandler.CHANNELBLOODVOLUME.sendToServer(
					new DialogueOptionPacket(opt.eventId(), tree.entityId()));
		}

		if (opt.nextNodeId() != null) {
			currentNode = tree.getNode(opt.nextNodeId());
			rebuildOptions();
		} else {
			this.onClose();
		}
	}

	private void rebuildOptions() {
		optionRects.clear();
	}

	// ──────────────────────────────────────────────
	// ── Rendering helpers ──
	// ──────────────────────────────────────────────

	private void renderPortrait(GuiGraphics gfx, int x, int y) {
		ResourceLocation icon = tree.speakerIcon();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		gfx.fill(x - 1, y - 1, x + PORTRAIT_SIZE + 1, y + PORTRAIT_SIZE + 1, palette.borderOuter);
		gfx.fill(x, y, x + PORTRAIT_SIZE, y + PORTRAIT_SIZE, palette.portraitBacking);

		RenderSystem.setShaderTexture(0, icon);
		gfx.blit(icon, x, y, PORTRAIT_SIZE, PORTRAIT_SIZE,
				(float) TEXTURE_HEAD_U, (float) TEXTURE_HEAD_V,
				TEXTURE_HEAD_SIZE, TEXTURE_HEAD_SIZE,
				TEXTURE_WIDTH, TEXTURE_HEIGHT);
		RenderSystem.disableBlend();
	}

	private void drawBorder(GuiGraphics gfx, int x, int y, int w, int h) {
		gfx.fill(x, y, x + w, y + 1, palette.borderOuter);
		gfx.fill(x, y + h - 1, x + w, y + h, palette.borderOuter);
		gfx.fill(x, y, x + 1, y + h, palette.borderOuter);
		gfx.fill(x + w - 1, y, x + w, y + h, palette.borderOuter);

		gfx.fill(x + 1, y + 1, x + w - 1, y + 2, palette.borderInner);
		gfx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, palette.borderInner);
		gfx.fill(x + 1, y + 1, x + 2, y + h - 1, palette.borderInner);
		gfx.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, palette.borderInner);
	}

	// ────────────────────────────────────────────────────────────
	//  Theme-dispatched background
	// ────────────────────────────────────────────────────────────

	private void renderThemedBackground(GuiGraphics gfx, int gx, int gy, int gw, int gh) {
		switch (tree.theme()) {
			case UNSTAINED -> renderUnstainedBackground(gfx, gx, gy, gw, gh);
			case FUNGAL -> renderFungalBackground(gfx, gx, gy, gw, gh);
			default -> renderBloodBackground(gfx, gx, gy, gw, gh);
		}
	}

	// ────────────────────────────────────────────────────────────
	//  BLOOD theme — red veiny tendrils (original)
	// ────────────────────────────────────────────────────────────

	private void renderBloodBackground(GuiGraphics gfx, int gx, int gy, int gw, int gh) {
		gfx.enableScissor(gx, gy, gx + gw, gy + gh);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		gfx.fill(gx, gy, gx + gw, gy + gh, palette.bgColor);

		// Center glow — red
		int cx = gx + gw / 2;
		int cy = gy + gh / 2;
		int glowRadius = Math.max(gw, gh) / 2;
		for (int ring = glowRadius; ring > 0; ring -= 6) {
			float t = (float) ring / glowRadius;
			int alpha = (int) (20 * (1f - t));
			int red = (int) (30 * (1f - t));
			gfx.fill(cx - ring, cy - ring, cx + ring, cy + ring, (alpha << 24) | (red << 16));
		}

		float time = System.nanoTime() / 1_000_000_000f;
		if (bloodVeinParams != null) {
			for (int i = 0; i < BLOOD_VEIN_COUNT; i++) {
				drawBloodVeinTendril(gfx, i, time, gx, gy, gw, gh);
			}
		}

		RenderSystem.disableBlend();
		gfx.disableScissor();
	}

	private void drawBloodVeinTendril(GuiGraphics gfx, int index, float time,
									   int gx, int gy, int gw, int gh) {
		float[] p = bloodVeinParams[index];
		float startX = gx + p[0] * gw;
		float startY = gy + p[1] * gh;
		float baseAngle = p[2];
		float speed = p[3];
		float amplitude = p[4];
		float frequency = p[5];
		int length = (int) p[6];
		float brightness = p[8];

		float angleDrift = baseAngle + 0.15f * Mth.sin(time * speed * 0.3f + index);
		float cosA = Mth.cos(angleDrift);
		float sinA = Mth.sin(angleDrift);
		float timeOffset = time * speed * 2.0f;

		int baseRed = (int) (30 + 40 * brightness);
		int baseGreen = (int) (2 + 6 * brightness);
		int baseBlue = (int) (4 + 4 * brightness);

		for (int step = 0; step < length; step++) {
			float squiggle = amplitude * Mth.sin(frequency * step + timeOffset);
			float px = startX + step * cosA * 1.5f - squiggle * sinA;
			float py = startY + step * sinA * 1.5f + squiggle * cosA;

			int ix = (int) px;
			int iy = (int) py;
			if (ix < gx || ix >= gx + gw || iy < gy || iy >= gy + gh) continue;

			float tipFade = 1f;
			if (step < 5) tipFade = step / 5f;
			else if (step > length - 5) tipFade = (length - step) / 5f;

			float pulse = 0.7f + 0.3f * Mth.sin(time * 1.5f + index * 0.5f + step * 0.03f);
			int alpha = (int) (Mth.clamp(tipFade * pulse * 140, 10, 150));
			int r = (int) Mth.clamp(baseRed * pulse, 0, 255);
			int g = (int) Mth.clamp(baseGreen * pulse * 0.5f, 0, 255);
			int b = (int) Mth.clamp(baseBlue * pulse * 0.3f, 0, 255);

			gfx.fill(ix, iy, ix + 1, iy + 1, (alpha << 24) | (r << 16) | (g << 8) | b);
		}
	}

	// ────────────────────────────────────────────────────────────
	//  UNSTAINED theme — dark blue with floating hollow rhombuses
	// ────────────────────────────────────────────────────────────

	private void renderUnstainedBackground(GuiGraphics gfx, int gx, int gy, int gw, int gh) {
		gfx.enableScissor(gx, gy, gx + gw, gy + gh);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		// Layer 1: rich dark blue base
		gfx.fill(gx, gy, gx + gw, gy + gh, palette.bgColor);

		// Layer 2: blue-white radial glow
		int cx = gx + gw / 2;
		int cy = gy + gh / 2;
		int glowRadius = Math.max(gw, gh) / 2;
		for (int ring = glowRadius; ring > 0; ring -= 4) {
			float t = (float) ring / glowRadius;
			float intensity = (1f - t) * (1f - t);
			int alpha = (int) (50 * intensity);
			int r = (int) (200 * intensity);
			int g = (int) (210 * intensity);
			int b = (int) (255 * intensity);
			gfx.fill(cx - ring, cy - ring, cx + ring, cy + ring,
					(alpha << 24) | (r << 16) | (g << 8) | b);
		}

		// Layer 3: floating hollow rhombuses
		float time = System.nanoTime() / 1_000_000_000f;
		if (unstainedRhombusParams != null) {
			for (int i = 0; i < UNSTAINED_RHOMBUS_COUNT; i++) {
				drawFloatingRhombus(gfx, i, time, gx, gy, gw, gh);
			}
		}

		// Layer 4: subtle blue speckles
		Random speckRand = new Random(54321L);
		for (int s = 0; s < 80; s++) {
			int spx = gx + speckRand.nextInt(gw);
			int spy = gy + speckRand.nextInt(gh);
			int sb = 10 + speckRand.nextInt(20);
			int sg = speckRand.nextInt(8);
			int sa = 15 + speckRand.nextInt(25);
			gfx.fill(spx, spy, spx + 1, spy + 1, (sa << 24) | (sg << 8) | sb);
		}

		RenderSystem.disableBlend();
		gfx.disableScissor();
	}

	private void drawFloatingRhombus(GuiGraphics gfx, int index, float time,
									  int gx, int gy, int gw, int gh) {
		float[] p = unstainedRhombusParams[index];
		float startXRatio = p[0];
		float startYRatio = p[1];
		int halfSize = (int) p[2];
		float velX = p[3];
		float velY = p[4];
		float phase = p[5];
		float brightness = p[6];
		float rotSpeed = p[7];

		float rawX = startXRatio * gw + velX * time;
		float rawY = startYRatio * gh + velY * time;
		rawX += 5f * Mth.sin(time * 0.4f + phase);
		rawY += 4f * Mth.cos(time * 0.35f + phase * 1.3f);

		int cx = gx + ((int) rawX % gw + gw) % gw;
		int cy = gy + ((int) rawY % gh + gh) % gh;

		float angle = phase + rotSpeed * time;

		float pulse = 0.6f + 0.4f * Mth.sin(time * 0.7f + phase);
		int baseAlpha = (int) (50 + 80 * brightness * pulse);

		int r = (int) Mth.clamp(180 + 75 * brightness, 0, 255);
		int g = (int) Mth.clamp(180 + 75 * brightness, 0, 255);
		int b = (int) Mth.clamp(200 + 55 * brightness, 0, 255);

		int color = (baseAlpha << 24) | (r << 16) | (g << 8) | b;
		int thickness = 2 + halfSize / 6;
		drawRotatedHollowRhombus(gfx, cx, cy, halfSize, thickness, angle, color);
	}

	private void drawRotatedHollowRhombus(GuiGraphics gfx, int cx, int cy,
										   int halfSize, int thickness, float angle, int color) {
		float cosA = Mth.cos(angle);
		float sinA = Mth.sin(angle);
		int innerSize = halfSize - thickness;
		int bound = halfSize + 1;

		for (int dy = -bound; dy <= bound; dy++) {
			int spanStart = Integer.MIN_VALUE;
			for (int dx = -bound; dx <= bound; dx++) {
				float u = dx * cosA + dy * sinA;
				float v = -dx * sinA + dy * cosA;
				float dist = Math.abs(u) + Math.abs(v);
				boolean inRing = dist <= halfSize && (innerSize <= 0 || dist >= innerSize);
				if (inRing) {
					if (spanStart == Integer.MIN_VALUE) spanStart = dx;
				} else if (spanStart != Integer.MIN_VALUE) {
					gfx.fill(cx + spanStart, cy + dy, cx + dx, cy + dy + 1, color);
					spanStart = Integer.MIN_VALUE;
				}
			}
			if (spanStart != Integer.MIN_VALUE) {
				gfx.fill(cx + spanStart, cy + dy, cx + bound + 1, cy + dy + 1, color);
			}
		}
	}

	// ────────────────────────────────────────────────────────────
	//  FUNGAL theme — amber/orange tendrils with floating spores
	// ────────────────────────────────────────────────────────────

	private void renderFungalBackground(GuiGraphics gfx, int gx, int gy, int gw, int gh) {
		gfx.enableScissor(gx, gy, gx + gw, gy + gh);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		// Layer 1: dark amber base
		gfx.fill(gx, gy, gx + gw, gy + gh, palette.bgColor);

		// Layer 2: warm radial glow
		int cx = gx + gw / 2;
		int cy = gy + gh / 2;
		int glowRadius = Math.max(gw, gh) / 2;
		for (int ring = glowRadius; ring > 0; ring -= 4) {
			float t = (float) ring / glowRadius;
			int alpha = (int) (30 * (1f - t));
			int r = (int) (60 * (1f - t));
			int g = (int) (25 * (1f - t));
			int b = (int) (5 * (1f - t));
			gfx.fill(cx - ring, cy - ring, cx + ring, cy + ring,
					(alpha << 24) | (r << 16) | (g << 8) | b);
		}

		// Layer 3: animated fungal tendrils
		float time = System.nanoTime() / 1_000_000_000f;
		if (fungalTendrilParams != null) {
			for (int i = 0; i < FUNGAL_TENDRIL_COUNT; i++) {
				drawFungalTendril(gfx, i, time, gx, gy, gw, gh);
			}
		}

		// Layer 4: floating spore particles
		if (fungalSporeParams != null) {
			drawFloatingSpores(gfx, time, gx, gy, gw, gh);
		}

		RenderSystem.disableBlend();
		gfx.disableScissor();
	}

	private void drawFungalTendril(GuiGraphics gfx, int index, float time,
									int gx, int gy, int gw, int gh) {
		float[] p = fungalTendrilParams[index];
		float startX = gx + p[0] * gw;
		float startY = gy + p[1] * gh;
		float baseAngle = p[2];
		float speed = p[3];
		float amplitude = p[4];
		float frequency = p[5];
		int length = (int) p[6];
		float brightness = p[8];

		float angleDrift = baseAngle + 0.12f * Mth.sin(time * speed * 0.4f + index);
		float cosA = Mth.cos(angleDrift);
		float sinA = Mth.sin(angleDrift);
		float timeOffset = time * speed * 1.5f;

		// Fungal palette: amber-red-yellow tones
		int baseRed = (int) (50 + 80 * brightness);
		int baseGreen = (int) (20 + 40 * brightness);
		int baseBlue = (int) (2 + 8 * brightness);

		for (int step = 0; step < length; step++) {
			float squiggle = amplitude * Mth.sin(frequency * step + timeOffset);
			float microSquiggle = (amplitude * 0.25f)
					* Mth.sin(frequency * 2.5f * step + timeOffset * 1.3f + index);
			float displacement = squiggle + microSquiggle;

			float px = startX + step * cosA * 1.5f - displacement * sinA;
			float py = startY + step * sinA * 1.5f + displacement * cosA;

			int ix = (int) px;
			int iy = (int) py;
			if (ix < gx || ix >= gx + gw || iy < gy || iy >= gy + gh) continue;

			float tipFade = 1f;
			if (step < 8) tipFade = step / 8f;
			else if (step > length - 8) tipFade = (length - step) / 8f;

			float pulse = 0.65f + 0.35f * Mth.sin(time * 1.2f + index * 0.6f + step * 0.025f);
			int a = (int) (Mth.clamp(tipFade * pulse * 160, 15, 180));
			int r = (int) Mth.clamp(baseRed * pulse, 0, 255);
			int g = (int) Mth.clamp(baseGreen * pulse, 0, 255);
			int b = (int) Mth.clamp(baseBlue * pulse * 0.4f, 0, 255);

			gfx.fill(ix, iy, ix + 1, iy + 1, (a << 24) | (r << 16) | (g << 8) | b);
		}
	}

	private void drawFloatingSpores(GuiGraphics gfx, float time,
									 int gx, int gy, int gw, int gh) {
		for (int i = 0; i < FUNGAL_SPORE_COUNT; i++) {
			float[] p = fungalSporeParams[i];
			float baseX = p[0] * gw;
			float baseY = p[1] * gh;
			float speed = p[2];
			float phase = p[3];
			float brightness = p[4];

			float yOffset = ((time * speed * 8f + phase) % (gh + 10)) - 5;
			float xWobble = 3f * Mth.sin(time * speed * 2f + phase);

			int sx = gx + Math.floorMod((int) (baseX + xWobble), gw);
			int sy = gy + gh - (int) yOffset;
			if (sy < gy || sy >= gy + gh || sx < gx || sx >= gx + gw) continue;

			float pulse = 0.5f + 0.5f * Mth.sin(time * 3f + phase);
			int alpha = (int) (40 + 60 * pulse * brightness);
			int r = (int) (120 + 80 * brightness);
			int g = (int) (60 + 40 * brightness);
			int b = (int) (5 + 10 * brightness);

			gfx.fill(sx, sy, sx + 1, sy + 1, (alpha << 24) | (r << 16) | (g << 8) | b);
		}
	}

	// ── Helper record for option click detection ──

	private record OptionRect(int x1, int y1, int x2, int y2, int index) {}
}
