package com.vincenthuto.hemomancy.client.screen.tile.functional;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.organs.EnumOrgan;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.visceral.VisceralMirrorCancelPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.visceral.VisceralMirrorExtractPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.visceral.VisceralMirrorUpdatePacket;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.VisceralMirrorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.VeinBackgroundRenderer;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;

/**
 * GUI screen for the Visceral Mirror — a blood-themed organ extraction
 * interface. Replaces the old click-to-cycle / shift-click-to-extract
 * mechanism with a proper selectable organ list and extraction progress bar.
 *
 * <p>Features an animated dark-red vein background, organ selection panel,
 * detailed organ information, and a pulsing extraction progress bar.</p>
 */
@OnlyIn(Dist.CLIENT)
public class VisceralMirrorScreen extends Screen {

	// ── Layout ──
	private static final int PANEL_W = 280;
	private static final int PANEL_H = 200;
	private static final int ORGAN_LIST_W = 100;
	private static final int ORGAN_ROW_H = 28;
	private static final int PROGRESS_BAR_H = 14;

	// ── Colors ──
	private static final int BG_COLOR = 0xE00A0204;
	private static final int BORDER_OUTER = 0xFF330808;
	private static final int BORDER_INNER = 0xFF220606;
	private static final int TEXT_DIM = 0xFF886666;
	private static final int TEXT_BRIGHT = 0xFFDDAAAA;
	private static final int TEXT_WHITE = 0xFFEEDDDD;
	private static final int ORGAN_BG = 0xC0120404;
	private static final int ORGAN_HOVER = 0xC0280808;
	private static final int ORGAN_SELECTED = 0xC0400C0C;
	private static final int ORGAN_BORDER = 0xFF441010;
	private static final int BUTTON_BG = 0xDD2A0808;
	private static final int BUTTON_HOVER = 0xDD4A0E0E;
	private static final int BUTTON_DISABLED = 0x80140404;
	private static final int BUTTON_BORDER = 0xFF772222;
	private static final int BUTTON_BORDER_HOVER = 0xFFBB3333;
	private static final int PROGRESS_BG = 0xFF0D0303;
	private static final int PROGRESS_BORDER = 0xFF331010;
	private static final int STATUS_GREEN = 0xFF55FF55;
	private static final int STATUS_RED = 0xFFFF5555;
	private static final int STATUS_GOLD = 0xFFFFAA00;

	// ── State ──
	private final BlockPos mirrorPos;
	private final int[] organLevels;
	private final boolean[] hasEcho;
	private double bloodVolume;
	private double maxBloodVolume;
	private int degree;

	private int selectedOrgan = -1;
	private static final VeinBackgroundRenderer.Style VEINS = new VeinBackgroundRenderer.Style(
			VeinBackgroundRenderer.Field.MACHINE.count(18).seed(66613L),
			VeinBackgroundRenderer.Palette.CRIMSON.rings(35, 45, 5, 0).tendrils(50, 60, 2, 10, 5, 6),
			new VeinBackgroundRenderer.Speckles.Scattered(66614L, 80, 12, 25, 6, 15, 20));
	private final VeinBackgroundRenderer veins = new VeinBackgroundRenderer(VEINS);

	// Ritual state (updated by server packets)
	private VisceralMirrorBlockEntity.RitualPhase phase = VisceralMirrorBlockEntity.RitualPhase.IDLE;
	private int ritualTicks = 0;
	private int totalRitualTicks = 0;
	private int ritualOrganOrdinal = -1;
	private String statusMessage = "";

	// Button bounds
	private int extractBtnX, extractBtnY, extractBtnW, extractBtnH;
	private int cancelBtnX, cancelBtnY, cancelBtnW, cancelBtnH;

	// Panel position
	private int panelX, panelY;

	private VisceralMirrorScreen(BlockPos pos, int[] organLevels, boolean[] hasEcho,
			double bloodVolume, double maxBloodVolume, int degree) {
		super(Component.literal("Visceral Mirror"));
		this.mirrorPos = pos;
		this.organLevels = organLevels;
		this.hasEcho = hasEcho;
		this.bloodVolume = bloodVolume;
		this.maxBloodVolume = maxBloodVolume;
		this.degree = degree;
	}

	public static void open(BlockPos pos, int[] organLevels, boolean[] hasEcho,
			double bloodVolume, double maxBloodVolume, int degree) {
		Minecraft.getInstance().setScreen(
				new VisceralMirrorScreen(pos, organLevels, hasEcho, bloodVolume, maxBloodVolume, degree));
	}

	public static void applyServerUpdate(VisceralMirrorUpdatePacket pkt) {
		if (Minecraft.getInstance().screen instanceof VisceralMirrorScreen screen) {
			screen.handleServerUpdate(pkt);
		}
	}

	/**
	 * Called by the S2C update packet to keep the screen in sync with the
	 * server's ritual state.
	 */
	public void handleServerUpdate(VisceralMirrorUpdatePacket pkt) {
		if (!pkt.getPos().equals(mirrorPos)) return;
		this.phase = pkt.getPhase();
		this.ritualTicks = pkt.getRitualTicks();
		this.totalRitualTicks = pkt.getTotalRitualTicks();
		this.ritualOrganOrdinal = pkt.getOrganOrdinal();
		this.statusMessage = pkt.getStatusMessage();

		// If ritual completed, auto-close after a brief delay
		if (phase == VisceralMirrorBlockEntity.RitualPhase.COMPLETE
				|| phase == VisceralMirrorBlockEntity.RitualPhase.IDLE) {
			if (!statusMessage.isEmpty()) {
				// Keep showing message briefly — the screen will be closed by the player
			}
		}
	}

	@Override
	protected void init() {
		super.init();
		panelX = (this.width - PANEL_W) / 2;
		panelY = (this.height - PANEL_H) / 2;

	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void onClose() {
		// If ritual is active, send cancel
		if (phase != VisceralMirrorBlockEntity.RitualPhase.IDLE
				&& phase != VisceralMirrorBlockEntity.RitualPhase.COMPLETE) {
			PacketHandler.sendToServer(new VisceralMirrorCancelPacket(mirrorPos));
		}
		super.onClose();
	}

	@Override
	public void tick() {
		super.tick();
		// Close if player moved too far
		if (minecraft != null && minecraft.player != null) {
			double distSq = minecraft.player.distanceToSqr(
					mirrorPos.getX() + 0.5, mirrorPos.getY() + 0.5, mirrorPos.getZ() + 0.5);
			if (distSq > 64.0) {
				this.onClose();
			}
		}
	}

	// ───── Render ─────

	@Override
	public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(gfx, mouseX, mouseY, partialTicks);

		int px = panelX;
		int py = panelY;

		// ── Animated blood background ──
		animTime += 0.016f; // ~60 FPS approximation
		veins.draw(gfx, px, py, PANEL_W, PANEL_H, animTime);
		drawVeinFog(gfx, px, py, PANEL_W, PANEL_H, animTime);
		ScreenDrawUtils.drawBorder(gfx, px, py, PANEL_W, PANEL_H, BORDER_OUTER, BORDER_INNER);

		// ── Title ──
		String title = "\u2666 Visceral Mirror \u2666";
		int titleW = font.width(title);
		gfx.drawString(font, title, px + (PANEL_W - titleW) / 2, py + 6, 0xFFCC3344, false);

		// ── Organ list panel (left side) ──
		int listX = px + 8;
		int listY = py + 22;
		int listH = ORGAN_ROW_H * EnumOrgan.values().length + 2;
		gfx.fill(listX, listY, listX + ORGAN_LIST_W, listY + listH, 0x80060102);
		ScreenDrawUtils.drawSimpleBorder(gfx, listX, listY, ORGAN_LIST_W, listH, ORGAN_BORDER);

		renderOrganList(gfx, listX, listY, mouseX, mouseY);

		// ── Detail panel (right side) ──
		int detailX = px + ORGAN_LIST_W + 16;
		int detailY = py + 22;
		int detailW = PANEL_W - ORGAN_LIST_W - 24;
		int detailH = 120;
		gfx.fill(detailX, detailY, detailX + detailW, detailY + detailH, 0x80060102);
		ScreenDrawUtils.drawSimpleBorder(gfx, detailX, detailY, detailW, detailH, ORGAN_BORDER);

		renderOrganDetails(gfx, detailX, detailY, detailW, detailH, mouseX, mouseY);

		// ── Progress bar (bottom) ──
		int barX = px + 8;
		int barY = py + PANEL_H - 30;
		int barW = PANEL_W - 16;
		renderProgressBar(gfx, barX, barY, barW);

		// ── Status message ──
		if (!statusMessage.isEmpty()) {
			int msgW = font.width(statusMessage);
			gfx.drawString(font, statusMessage, px + (PANEL_W - msgW) / 2, py + PANEL_H - 14, TEXT_DIM, false);
		}

		super.render(gfx, mouseX, mouseY, partialTicks);
	}

	// ───── Organ List ─────

	private void renderOrganList(GuiGraphics gfx, int listX, int listY, int mouseX, int mouseY) {
		EnumOrgan[] organs = EnumOrgan.values();
		boolean ritualActive = isRitualActive();

		for (int i = 0; i < organs.length; i++) {
			int rowY = listY + 1 + i * ORGAN_ROW_H;
			int rowX = listX + 1;
			int rowW = ORGAN_LIST_W - 2;
			int rowH = ORGAN_ROW_H - 1;

			boolean hovered = !ritualActive && mouseX >= rowX && mouseX < rowX + rowW
					&& mouseY >= rowY && mouseY < rowY + rowH;
			boolean selected = (selectedOrgan == i);

			// Background
			int bg = selected ? ORGAN_SELECTED : (hovered ? ORGAN_HOVER : ORGAN_BG);
			gfx.fill(rowX, rowY, rowX + rowW, rowY + rowH, bg);

			// Selection indicator
			if (selected) {
				gfx.fill(rowX, rowY, rowX + 2, rowY + rowH, 0xFFCC3333);
			}

			// Organ name
			EnumOrgan organ = organs[i];
			String tierSymbol = organ == EnumOrgan.HEART ? "\u2665 " : "\u25C6 "; // ♥ or ◆
			int nameColor = getOrganNameColor(i);
			gfx.drawString(font, tierSymbol + organ.getName(), rowX + 5, rowY + 4, nameColor, false);

			// Level indicator
			int level = organLevels[i];
			String levelStr = "Lv." + level + "/3";
			int levelColor = level >= 3 ? STATUS_GOLD : TEXT_DIM;
			gfx.drawString(font, levelStr, rowX + 5, rowY + 15, levelColor, false);

			// Tier badge
			String tierStr = "T" + organ.getTier();
			int tierW = font.width(tierStr);
			gfx.drawString(font, tierStr, rowX + rowW - tierW - 4, rowY + 4, TEXT_DIM, false);
		}
	}

	private int getOrganNameColor(int organIndex) {
		EnumOrgan organ = EnumOrgan.values()[organIndex];
		if (organLevels[organIndex] >= 3) return STATUS_GOLD;
		if (hasEcho[organIndex]) return STATUS_GOLD;
		if (organ.getTier() > degree) return STATUS_RED;
		double cost = organ.getTier() * 500.0;
		if (bloodVolume < cost) return STATUS_RED;
		return TEXT_WHITE;
	}

	// ───── Organ Details ─────

	private void renderOrganDetails(GuiGraphics gfx, int dx, int dy, int dw, int dh,
			int mouseX, int mouseY) {
		if (selectedOrgan < 0 || selectedOrgan >= EnumOrgan.values().length) {
			// No organ selected — hint text
			String hint = "Select an organ";
			int hintW = font.width(hint);
			gfx.drawString(font, hint, dx + (dw - hintW) / 2, dy + dh / 2 - 4, TEXT_DIM, false);
			return;
		}

		EnumOrgan organ = EnumOrgan.values()[selectedOrgan];
		int level = organLevels[selectedOrgan];
		boolean echoHeld = hasEcho[selectedOrgan];
		double cost = organ.getTier() * 500.0;
		boolean affordable = bloodVolume >= cost;
		boolean degreeOk = organ.getTier() <= degree;
		boolean maxed = level >= 3;

		int textX = dx + 8;
		int textY = dy + 8;

		// Organ name (large)
		String symbol = organ == EnumOrgan.HEART ? "\u2665 " : "\u25C6 ";
		gfx.drawString(font, symbol + organ.getName(), textX, textY, 0xFFCC3344, false);
		textY += 14;

		// Description
		gfx.drawString(font, organ.getDescription(), textX, textY, TEXT_DIM, false);
		textY += 14;

		// Tier and level
		gfx.drawString(font, "Tier " + organ.getTier() + "  \u2014  Level " + level + "/3",
				textX, textY, TEXT_BRIGHT, false);
		textY += 14;

		// Blood cost
		int costColor = affordable ? STATUS_GREEN : STATUS_RED;
		gfx.drawString(font, "Blood Cost: " + (int) cost, textX, textY, costColor, false);
		textY += 14;

		// Status
		String status;
		int statusColor;
		if (maxed) {
			status = "MAXIMUM MODIFICATION";
			statusColor = STATUS_GOLD;
		} else if (echoHeld) {
			status = "Echo already in inventory";
			statusColor = STATUS_GOLD;
		} else if (!degreeOk) {
			status = "Degree too low (need " + organ.getTier() + ")";
			statusColor = STATUS_RED;
		} else if (!affordable) {
			status = "Not enough blood";
			statusColor = STATUS_RED;
		} else {
			status = "Ready to extract";
			statusColor = STATUS_GREEN;
		}
		gfx.drawString(font, status, textX, textY, statusColor, false);
		textY += 16;

		// ── Extract / Cancel button ──
		boolean canExtract = !maxed && !echoHeld && degreeOk && affordable && !isRitualActive();
		boolean ritualActive = isRitualActive();

		if (ritualActive) {
			// Cancel button
			cancelBtnW = 80;
			cancelBtnH = 16;
			cancelBtnX = dx + (dw - cancelBtnW) / 2;
			cancelBtnY = textY;
			boolean cancelHover = mouseX >= cancelBtnX && mouseX < cancelBtnX + cancelBtnW
					&& mouseY >= cancelBtnY && mouseY < cancelBtnY + cancelBtnH;
			renderButton(gfx, cancelBtnX, cancelBtnY, cancelBtnW, cancelBtnH,
					"Cancel Ritual", cancelHover, true);
			// Clear extract button bounds
			extractBtnX = extractBtnY = extractBtnW = extractBtnH = 0;
		} else {
			// Extract button
			extractBtnW = 120;
			extractBtnH = 16;
			extractBtnX = dx + (dw - extractBtnW) / 2;
			extractBtnY = textY;
			boolean extractHover = canExtract && mouseX >= extractBtnX && mouseX < extractBtnX + extractBtnW
					&& mouseY >= extractBtnY && mouseY < extractBtnY + extractBtnH;
			renderButton(gfx, extractBtnX, extractBtnY, extractBtnW, extractBtnH,
					"Begin Extraction", extractHover, canExtract);
			// Clear cancel button bounds
			cancelBtnX = cancelBtnY = cancelBtnW = cancelBtnH = 0;
		}
	}

	private void renderButton(GuiGraphics gfx, int bx, int by, int bw, int bh,
			String text, boolean hovered, boolean enabled) {
		int bg = !enabled ? BUTTON_DISABLED : (hovered ? BUTTON_HOVER : BUTTON_BG);
		int border = hovered ? BUTTON_BORDER_HOVER : BUTTON_BORDER;

		gfx.fill(bx, by, bx + bw, by + bh, border);
		gfx.fill(bx + 1, by + 1, bx + bw - 1, by + bh - 1, bg);

		int textW = font.width(text);
		int textColor = !enabled ? 0xFF664444 : (hovered ? 0xFFFF5555 : 0xFFCC3333);
		gfx.drawString(font, text, bx + (bw - textW) / 2, by + (bh - 8) / 2, textColor, false);
	}

	// ───── Progress Bar ─────

	private void renderProgressBar(GuiGraphics gfx, int bx, int by, int bw) {
		if (!isRitualActive()) return;

		animTime += 0.016f; // ~60 FPS approximation

		float time = animTime;
				float progress = totalRitualTicks > 0 ? (float) ritualTicks / totalRitualTicks : 0;

		// Phase label
		String phaseLabel = phase == VisceralMirrorBlockEntity.RitualPhase.CHANNELING
				? "Channeling..." : "Extracting...";
		if (ritualOrganOrdinal >= 0 && ritualOrganOrdinal < EnumOrgan.values().length) {
			phaseLabel += " " + EnumOrgan.values()[ritualOrganOrdinal].getName();
		}
		gfx.drawString(font, phaseLabel, bx, by - 11, TEXT_DIM, false);

		// Percentage
		String pctText = (int) (progress * 100) + "%";
		int pctW = font.width(pctText);
		gfx.drawString(font, pctText, bx + bw - pctW, by - 11, TEXT_BRIGHT, false);

		// Bar border
		gfx.fill(bx, by, bx + bw, by + PROGRESS_BAR_H, PROGRESS_BORDER);
		gfx.fill(bx + 1, by + 1, bx + bw - 1, by + PROGRESS_BAR_H - 1, PROGRESS_BG);

		// Filled portion with animated pulse
		int fillW = (int) ((bw - 2) * progress);
		if (fillW > 0) {
			int fillX = bx + 1;
			int fillY = by + 1;
			int fillH = PROGRESS_BAR_H - 2;

			for (int col = 0; col < fillW; col++) {
				float colT = (float) col / fillW;
				float pulse = 0.7f + 0.3f * Mth.sin(time * 3f + col * 0.05f);
				int r = (int) (Mth.clamp((140 + 80 * colT) * pulse, 0, 255));
				int g = (int) (Mth.clamp((15 + 20 * colT) * pulse, 0, 255));
				int b = (int) (Mth.clamp((10 + 10 * colT) * pulse, 0, 255));
				int color = (0xDD << 24) | (r << 16) | (g << 8) | b;
				gfx.fill(fillX + col, fillY, fillX + col + 1, fillY + fillH, color);
			}

			// Leading edge glow
			float glowPulse = 0.5f + 0.5f * Mth.sin(time * 5f);
			int glowAlpha = (int) (150 * glowPulse);
			int glowColor = (glowAlpha << 24) | (0xFF << 16) | (0x40 << 8) | 0x20;
			int edgeX = fillX + fillW - 1;
			gfx.fill(edgeX, fillY, edgeX + 2, fillY + fillH, glowColor);

			// Meniscus
			float mPulse = 0.6f + 0.4f * Mth.sin(time * 2.5f);
			int mAlpha = (int) (200 * mPulse);
			int meniscus = (mAlpha << 24) | (0xCC << 16) | (0x20 << 8) | 0x15;
			gfx.fill(fillX, fillY, fillX + fillW, fillY + 1, meniscus);
		}
	}

	// ───── Input ─────

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

		int mx = (int) mouseX;
		int my = (int) mouseY;

		// Check organ list clicks
		if (!isRitualActive()) {
			int listX = panelX + 8;
			int listY = panelY + 22;
			EnumOrgan[] organs = EnumOrgan.values();
			for (int i = 0; i < organs.length; i++) {
				int rowY = listY + 1 + i * ORGAN_ROW_H;
				int rowX = listX + 1;
				int rowW = ORGAN_LIST_W - 2;
				int rowH = ORGAN_ROW_H - 1;
				if (mx >= rowX && mx < rowX + rowW && my >= rowY && my < rowY + rowH) {
					selectedOrgan = i;
					return true;
				}
			}
		}

		// Check extract button
		if (extractBtnW > 0 && mx >= extractBtnX && mx < extractBtnX + extractBtnW
				&& my >= extractBtnY && my < extractBtnY + extractBtnH) {
			if (selectedOrgan >= 0 && !isRitualActive()) {
				EnumOrgan organ = EnumOrgan.values()[selectedOrgan];
				boolean canExtract = organLevels[selectedOrgan] < 3
						&& !hasEcho[selectedOrgan]
						&& organ.getTier() <= degree
						&& bloodVolume >= organ.getTier() * 500.0;
				if (canExtract) {
					PacketHandler.sendToServer(
							new VisceralMirrorExtractPacket(mirrorPos, selectedOrgan));
					return true;
				}
			}
		}

		// Check cancel button
		if (cancelBtnW > 0 && mx >= cancelBtnX && mx < cancelBtnX + cancelBtnW
				&& my >= cancelBtnY && my < cancelBtnY + cancelBtnH) {
			if (isRitualActive()) {
				PacketHandler.sendToServer(
						new VisceralMirrorCancelPacket(mirrorPos));
				return true;
			}
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	// ───── Helpers ─────

	private boolean isRitualActive() {
		return phase == VisceralMirrorBlockEntity.RitualPhase.CHANNELING
				|| phase == VisceralMirrorBlockEntity.RitualPhase.EXTRACTING;
	}

	private float animTime = 0f;

	/** A pulsing blood fog over the veins. */
	private void drawVeinFog(GuiGraphics graphics, int gx, int gy, int gw, int gh, float time) {
		float fogPulse = 0.3f + 0.15f * Mth.sin(time * 0.8f);
		int fogAlpha = (int) (fogPulse * 255);
		int fogColor = (fogAlpha << 24) | (0x18 << 16) | (0x02 << 8) | 0x02;
		graphics.fill(gx, gy, gx + gw, gy + gh, fogColor);
	}

}
