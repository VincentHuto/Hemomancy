package com.vincenthuto.hemomancy.client.screen.dialogue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.common.dialogue.DialogueNode;
import com.vincenthuto.hemomancy.common.dialogue.DialogueOption;
import com.vincenthuto.hemomancy.common.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.dialogue.DialogueOptionPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Custom non-traditional dialogue screen. Renders on the left side of the
 * screen with a portrait of the talking entity, their dialogue lines, and
 * clickable response options for the player.
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
	private static final int VEIN_COUNT = 14;

	// ── Colors ──
	private static final int BG_COLOR = 0xE00A0204;
	private static final int BORDER_OUTER = 0xFF330808;
	private static final int BORDER_INNER = 0xFF220606;
	private static final int SPEAKER_COLOR = 0xFFCC3344;
	private static final int LINE_COLOR = 0xFFBBAAAA;
	private static final int OPTION_COLOR = 0xFFDD9966;
	private static final int OPTION_HOVER_COLOR = 0xFFFFCC88;
	private static final int OPTION_BULLET_COLOR = 0xFF884422;

	// ── State ──
	private final DialogueTree tree;
	private DialogueNode currentNode;
	private final List<OptionRect> optionRects = new ArrayList<>();
	private float[][] veinParams;

	private DialogueScreen(DialogueTree tree) {
		super(Component.empty());
		this.tree = tree;
		this.currentNode = tree.getStartNode();
	}

	public static void open(DialogueTree tree) {
		Minecraft.getInstance().setScreen(new DialogueScreen(tree));
	}

	// ──────────────────────────────────────────────

	@Override
	protected void init() {
		super.init();
		rebuildOptions();
		// seed vein params for organic background
		Random rand = new Random(9991L);
		veinParams = new float[VEIN_COUNT][9];
		for (int i = 0; i < VEIN_COUNT; i++) {
			veinParams[i][0] = rand.nextFloat();
			veinParams[i][1] = rand.nextFloat();
			veinParams[i][2] = (float) (rand.nextFloat() * Math.PI * 2);
			veinParams[i][3] = 0.3f + rand.nextFloat() * 0.7f;
			veinParams[i][4] = 4f + rand.nextFloat() * 10f;
			veinParams[i][5] = 0.06f + rand.nextFloat() * 0.08f;
			veinParams[i][6] = 30 + rand.nextInt(60);
			veinParams[i][7] = 1;
			veinParams[i][8] = rand.nextFloat();
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

		// ── Organic vein background ──
		renderVeinBackground(gfx, panelX, panelY, PANEL_WIDTH, panelH);

		// ── Border ──
		drawBorder(gfx, panelX, panelY, PANEL_WIDTH, panelH);

		Font font = this.font;
		int contentX = panelX + TEXT_LEFT_PAD;
		int contentW = PANEL_WIDTH - TEXT_LEFT_PAD * 2;
		int y = panelY + 10;

		// ── Speaker portrait placeholder (colored rectangle + name) ──
		int portraitX = contentX;
		int portraitY = y;
		renderPortrait(gfx, portraitX, portraitY);

		int nameX = portraitX + PORTRAIT_SIZE + 8;
		Component speakerName = Component.translatable(tree.speakerName());
		gfx.drawString(font, speakerName, nameX, portraitY + 4, SPEAKER_COLOR, true);

		// Thin separator
		int sepY = portraitY + PORTRAIT_SIZE + 6;
		gfx.fill(contentX, sepY, contentX + contentW, sepY + 1, 0x44CC3344);
		y = sepY + 8;

		// ── Dialogue lines ──
		if (currentNode != null) {
			for (String lineKey : currentNode.lines()) {
				Component line = Component.translatable(lineKey);
				List<net.minecraft.util.FormattedCharSequence> wrapped = font.split(line, contentW);
				for (var seq : wrapped) {
					gfx.drawString(font, seq, contentX, y, LINE_COLOR, false);
					y += LINE_SPACING;
				}
				y += 4; // extra gap between lines
			}

			y += 8;
			// ── Player options ──
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
				int col = hovered ? OPTION_HOVER_COLOR : OPTION_COLOR;

				for (var seq : wrapped) {
					gfx.drawString(font, seq, optX, y, col, false);
					y += LINE_SPACING;
				}
				optionRects.add(new OptionRect(optX, optTop, optX + optW, y, i));
				y += OPTION_SPACING - LINE_SPACING;
			}
		}

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

		// Fire server-side event if this option has one
		if (opt.eventId() != null && !opt.eventId().isEmpty()) {
			PacketHandler.CHANNELBLOODVOLUME.sendToServer(
					new DialogueOptionPacket(opt.eventId(), tree.entityId()));
		}

		// Navigate to next node or close
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
	// ── Rendering helpers (matching Hemomancy aesthetic) ──

	private void renderPortrait(GuiGraphics gfx, int x, int y) {
		ResourceLocation icon = tree.speakerIcon();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		// Dark backing
		gfx.fill(x - 1, y - 1, x + PORTRAIT_SIZE + 1, y + PORTRAIT_SIZE + 1, BORDER_OUTER);
		gfx.fill(x, y, x + PORTRAIT_SIZE, y + PORTRAIT_SIZE, 0xFF0A0204);

		// Render the entity texture as the portrait (head region)
		RenderSystem.setShaderTexture(0, icon);
		// Draw the head portion of a standard entity texture (8x8 area at 8,8 in a 64x64 sheet)
		gfx.blit(icon, x, y, PORTRAIT_SIZE, PORTRAIT_SIZE,
				8.0f, 8.0f, 8, 8, 64, 64);
		RenderSystem.disableBlend();
	}

	private void drawBorder(GuiGraphics gfx, int x, int y, int w, int h) {
		gfx.fill(x, y, x + w, y + 1, BORDER_OUTER);
		gfx.fill(x, y + h - 1, x + w, y + h, BORDER_OUTER);
		gfx.fill(x, y, x + 1, y + h, BORDER_OUTER);
		gfx.fill(x + w - 1, y, x + w, y + h, BORDER_OUTER);

		gfx.fill(x + 1, y + 1, x + w - 1, y + 2, BORDER_INNER);
		gfx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, BORDER_INNER);
		gfx.fill(x + 1, y + 1, x + 2, y + h - 1, BORDER_INNER);
		gfx.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, BORDER_INNER);
	}

	private void renderVeinBackground(GuiGraphics gfx, int gx, int gy, int gw, int gh) {
		gfx.enableScissor(gx, gy, gx + gw, gy + gh);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		gfx.fill(gx, gy, gx + gw, gy + gh, BG_COLOR);

		// Center glow
		int cx = gx + gw / 2;
		int cy = gy + gh / 2;
		int glowRadius = Math.max(gw, gh) / 2;
		for (int ring = glowRadius; ring > 0; ring -= 6) {
			float t = (float) ring / glowRadius;
			int alpha = (int) (20 * (1f - t));
			int red = (int) (30 * (1f - t));
			gfx.fill(cx - ring, cy - ring, cx + ring, cy + ring, (alpha << 24) | (red << 16));
		}

		// Vein tendrils
		float time = System.nanoTime() / 1_000_000_000f;
		if (veinParams != null) {
			for (int i = 0; i < VEIN_COUNT; i++) {
				drawVeinTendril(gfx, i, time, gx, gy, gw, gh);
			}
		}

		RenderSystem.disableBlend();
		gfx.disableScissor();
	}

	private void drawVeinTendril(GuiGraphics gfx, int index, float time,
								  int gx, int gy, int gw, int gh) {
		float[] p = veinParams[index];
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
			float t = step;
			float squiggle = amplitude * Mth.sin(frequency * t + timeOffset);
			float px = startX + t * cosA * 1.5f - squiggle * sinA;
			float py = startY + t * sinA * 1.5f + squiggle * cosA;

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

	// ── Helper record for option click detection ──

	private record OptionRect(int x1, int y1, int x2, int y2, int index) {}
}
