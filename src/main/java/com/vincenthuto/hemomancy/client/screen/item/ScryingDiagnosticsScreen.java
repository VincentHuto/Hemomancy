package com.vincenthuto.hemomancy.client.screen.item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationEquipHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.IBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.IVascularSystem;
import com.vincenthuto.hemomancy.common.menu.ScryingDiagnosticsMenu;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;
import java.util.Random;

public class ScryingDiagnosticsScreen extends AbstractContainerScreen<ScryingDiagnosticsMenu> {
	private static final int PANEL_WIDTH = 322;
	private static final int PANEL_HEIGHT = 218;
	private static final int PANEL = 0xF00A0204;
	private static final int VEIN_COUNT = 10;
	private static final int VEIN_STEP_STRIDE = 4;
	private static final int GLOW_RING_STEP = 12;
	private static final int SPECKLE_COUNT = 48;
	private static final float VEIN_ANIMATION_SPEED = 0.35F;
	private static final int BORDER = 0xFF5C1010;
	private static final int BORDER_DARK = 0xFF1A0404;
	private static final int HEADER = 0xFFCC3344;
	private static final int SECTION = 0xFFE15A5A;
	private static final int TEXT = 0xFFD8B6B6;
	private static final int MUTED = 0xFF8B6666;
	private static final int GOOD = 0xFF86D986;
	private static final int WARN = 0xFFE6C65C;
	private static final int BAD = 0xFFE06C6C;

	private float[][] veinParams;
	private int[][] speckleParams;
	private float animTime = 0.0F;
	private long lastAnimMillis = -1L;

	public ScryingDiagnosticsScreen(ScryingDiagnosticsMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.imageWidth = PANEL_WIDTH;
		this.imageHeight = PANEL_HEIGHT;
	}

	@Override
	protected void init() {
		super.init();
		Random rand = new Random(42L);
		veinParams = new float[VEIN_COUNT][9];
		for (int i = 0; i < VEIN_COUNT; i++) {
			veinParams[i][0] = rand.nextFloat();
			veinParams[i][1] = rand.nextFloat();
			veinParams[i][2] = (float) (rand.nextFloat() * Math.PI * 2);
			veinParams[i][3] = 0.3F + rand.nextFloat() * 0.7F;
			veinParams[i][4] = 7.0F + rand.nextFloat() * 15.0F;
			veinParams[i][5] = 0.04F + rand.nextFloat() * 0.08F;
			veinParams[i][6] = 56 + rand.nextInt(72);
			veinParams[i][7] = 1 + rand.nextInt(3);
			veinParams[i][8] = rand.nextFloat();
		}
		Random speckleRandom = new Random(12345L);
		speckleParams = new int[SPECKLE_COUNT][5];
		for (int i = 0; i < SPECKLE_COUNT; i++) {
			speckleParams[i][0] = speckleRandom.nextInt(PANEL_WIDTH);
			speckleParams[i][1] = speckleRandom.nextInt(PANEL_HEIGHT);
			speckleParams[i][2] = 10 + speckleRandom.nextInt(20);
			speckleParams[i][3] = speckleRandom.nextInt(6);
			speckleParams[i][4] = 15 + speckleRandom.nextInt(25);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		int x = this.leftPos;
		int y = this.topPos;
		renderVeinBackground(graphics, x, y, PANEL_WIDTH, PANEL_HEIGHT);
		drawFrame(graphics, x, y, PANEL_WIDTH, PANEL_HEIGHT);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(graphics, mouseX, mouseY, partialTick);
		super.render(graphics, mouseX, mouseY, partialTick);
		renderDiagnostics(graphics);
		this.renderTooltip(graphics, mouseX, mouseY);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
	}

	private void renderDiagnostics(GuiGraphics graphics) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		int x = this.leftPos;
		int y = this.topPos;
		graphics.drawCenteredString(this.font, this.title, x + PANEL_WIDTH / 2, y + 8, HEADER);
		graphics.drawCenteredString(this.font, "The podium reads what the blood is already saying.",
				x + PANEL_WIDTH / 2, y + 21, MUTED);

		int leftX = x + 16;
		int rightX = x + 168;
		drawBloodVolume(graphics, player, leftX, y + 42);
		drawTendency(graphics, player, leftX, y + 96);
		drawKnownMemories(graphics, player, leftX, y + 150);
		drawVascularHealth(graphics, player, rightX, y + 42);
		drawRiteReadiness(graphics, player, rightX, y + 112);
	}

	private void drawBloodVolume(GuiGraphics graphics, LocalPlayer player, int x, int y) {
		drawSection(graphics, "Blood Volume", x, y);
		HemoCapabilityAccess.getBloodVolume(player).ifPresentOrElse(volume -> {
			String reserve = format(volume.getBloodVolume()) + " / " + format(volume.getMaxBloodVolume());
			drawValue(graphics, "Reserve", reserve, x, y + 13, colorForBlood(volume));
			drawValue(graphics, "State", volume.isActive() ? "Active covenant" : "Dormant", x, y + 25,
					volume.isActive() ? GOOD : WARN);
		}, () -> drawLine(graphics, "No blood record found.", x, y + 13, BAD));
	}

	private void drawTendency(GuiGraphics graphics, LocalPlayer player, int x, int y) {
		drawSection(graphics, "Dominant Tendency", x, y);
		HemoCapabilityAccess.getBloodTendency(player).ifPresentOrElse(tendency -> {
			EnumBloodTendency dominant = strongest(tendency);
			EnumBloodTendency latent = weakest(tendency);
			drawValue(graphics, "Dominant", tendencyLabel(tendency, dominant), x, y + 13, tendencyColor(dominant));
			drawValue(graphics, "Latent", tendencyLabel(tendency, latent), x, y + 25, MUTED);
		}, () -> drawLine(graphics, "No tendency record found.", x, y + 13, BAD));
	}

	private void drawKnownMemories(GuiGraphics graphics, LocalPlayer player, int x, int y) {
		drawSection(graphics, "Known Memories", x, y);
		HemoCapabilityAccess.getKnownManipulations(player).ifPresentOrElse(known -> {
			int knownCount = known.getKnownManips().size();
			int equippedCount = ManipulationEquipHelper.countNormalEquippedNames(known.getEquippedManipNames());
			drawValue(graphics, "Carried", knownCount + " memories", x, y + 13, TEXT);
			drawValue(graphics, "Equipped", equippedCount + " slotted", x, y + 25, TEXT);
			drawValue(graphics, "Current", selectedManipName(known), x, y + 37, MUTED);
		}, () -> drawLine(graphics, "No manipulation record found.", x, y + 13, BAD));
	}

	private void drawVascularHealth(GuiGraphics graphics, LocalPlayer player, int x, int y) {
		drawSection(graphics, "Vascular Health", x, y);
		HemoCapabilityAccess.getVascularSystem(player).ifPresentOrElse(vascular -> {
			float average = averageHealth(vascular);
			EnumVeinSections worst = weakestSection(vascular);
			drawValue(graphics, "Average", String.format(Locale.ROOT, "%.0f%%", average), x, y + 13,
					colorForHealth(average));
			drawValue(graphics, "Weakest", titleCase(worst.name()) + " "
					+ String.format(Locale.ROOT, "%.0f%%", vascular.getHealthBySection(worst)), x, y + 25,
					colorForHealth(vascular.getHealthBySection(worst)));
			drawValue(graphics, "Flow", titleCase(vascular.getBloodFlowBySection(worst).name()), x, y + 37, MUTED);
		}, () -> drawLine(graphics, "No vascular record found.", x, y + 13, BAD));
	}

	private void drawRiteReadiness(GuiGraphics graphics, LocalPlayer player, int x, int y) {
		drawSection(graphics, "Rite Readiness", x, y);
		int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (volume == null) {
			drawLine(graphics, "Unreadable: no blood record.", x, y + 13, BAD);
			return;
		}
		if (!volume.isActive()) {
			drawLine(graphics, "Dormant: blood magic is inactive.", x, y + 13, WARN);
		} else if (degree <= 0) {
			drawLine(graphics, "Uninitiated: no rite standing.", x, y + 13, WARN);
		} else if (volume.getBloodVolume() < volume.getMaxBloodVolume() * 0.25D) {
			drawLine(graphics, "Strained: restore blood first.", x, y + 13, BAD);
		} else {
			drawLine(graphics, "Stable: ordinary rites are supported.", x, y + 13, GOOD);
		}
		drawValue(graphics, "Degree", String.valueOf(degree), x, y + 29, TEXT);
	}

	private void drawSection(GuiGraphics graphics, String text, int x, int y) {
		graphics.drawString(this.font, text, x, y, SECTION);
	}

	private void drawValue(GuiGraphics graphics, String label, String value, int x, int y, int color) {
		graphics.drawString(this.font, label + ": ", x, y, MUTED);
		graphics.drawString(this.font, value, x + 58, y, color);
	}

	private void drawLine(GuiGraphics graphics, String text, int x, int y, int color) {
		graphics.drawString(this.font, text, x, y, color);
	}

	private void drawFrame(GuiGraphics graphics, int x, int y, int width, int height) {
		graphics.fill(x, y, x + width, y + 1, BORDER);
		graphics.fill(x, y + height - 1, x + width, y + height, BORDER_DARK);
		graphics.fill(x, y, x + 1, y + height, BORDER);
		graphics.fill(x + width - 1, y, x + width, y + height, BORDER_DARK);
		graphics.fill(x + 6, y + 33, x + width - 6, y + 34, 0xFF2B0707);
	}

	private void renderVeinBackground(GuiGraphics graphics, int gx, int gy, int gw, int gh) {
		graphics.enableScissor(gx, gy, gx + gw, gy + gh);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		graphics.fill(gx, gy, gx + gw, gy + gh, PANEL);

		int cx = gx + gw / 2;
		int cy = gy + gh / 2;
		int glowRadius = Math.max(gw, gh) / 2;
		for (int ring = glowRadius; ring > 0; ring -= GLOW_RING_STEP) {
			float t = (float) ring / glowRadius;
			int alpha = (int) (35 * (1.0F - t));
			int red = (int) (40 * (1.0F - t));
			int color = (alpha << 24) | (red << 16);
			graphics.fill(cx - ring, cy - ring, cx + ring, cy + ring, color);
		}

		long now = Util.getMillis();
		if (lastAnimMillis >= 0L) {
			float deltaSeconds = Math.min((now - lastAnimMillis) / 1000.0F, 0.05F);
			animTime += deltaSeconds * VEIN_ANIMATION_SPEED;
		}
		lastAnimMillis = now;

		float time = animTime;
		if (veinParams != null) {
			for (int i = 0; i < VEIN_COUNT; i++) {
				drawVeinTendril(graphics, i, time, gx, gy, gw, gh);
			}
		}

		if (speckleParams != null) {
			for (int[] speckle : speckleParams) {
				int sx = gx + speckle[0];
				int sy = gy + speckle[1];
				int color = (speckle[4] << 24) | (speckle[2] << 16) | (speckle[3] << 8);
				graphics.fill(sx, sy, sx + 1, sy + 1, color);
			}
		}

		RenderSystem.disableBlend();
		graphics.disableScissor();
	}

	private void drawVeinTendril(GuiGraphics graphics, int index, float time, int gx, int gy, int gw, int gh) {
		float[] p = veinParams[index];
		float startX = gx + p[0] * gw;
		float startY = gy + p[1] * gh;
		float baseAngle = p[2];
		float speed = p[3];
		float amplitude = p[4];
		float frequency = p[5];
		int length = (int) p[6];
		int thickness = (int) p[7];
		float brightness = p[8];

		float angleDrift = baseAngle + 0.15F * Mth.sin(time * speed * 0.3F + index);
		float cosA = Mth.cos(angleDrift);
		float sinA = Mth.sin(angleDrift);
		float timeOffset = time * speed * 2.0F;

		int baseRed = (int) (40 + 50 * brightness);
		int baseGreen = (int) (2 + 8 * brightness);
		int baseBlue = (int) (5 + 5 * brightness);

		for (int step = 0; step < length; step += VEIN_STEP_STRIDE) {
			float squiggle = amplitude * Mth.sin(frequency * step + timeOffset);
			float microSquiggle = (amplitude * 0.3F) * Mth.sin(frequency * 2.7F * step + timeOffset * 1.4F + index);
			float displacement = squiggle + microSquiggle;

			float px = startX + step * cosA * 1.5F - displacement * sinA;
			float py = startY + step * sinA * 1.5F + displacement * cosA;
			int ix = (int) px;
			int iy = (int) py;

			if (ix + thickness < gx || ix >= gx + gw || iy + thickness < gy || iy >= gy + gh) {
				continue;
			}

			float tipFade = 1.0F;
			if (step < 10) {
				tipFade = step / 10.0F;
			} else if (step > length - 10) {
				tipFade = (length - step) / 10.0F;
			}

			float pulse = 0.7F + 0.3F * Mth.sin(time * 1.5F + index * 0.5F + step * 0.02F);
			int alpha = (int) Mth.clamp(tipFade * pulse * 180, 20, 200);
			int r = (int) Mth.clamp(baseRed * pulse, 0, 255);
			int g = (int) Mth.clamp(baseGreen * pulse * 0.5F, 0, 255);
			int b = (int) Mth.clamp(baseBlue * pulse * 0.3F, 0, 255);
			int color = (alpha << 24) | (r << 16) | (g << 8) | b;
			graphics.fill(ix, iy, ix + thickness + 1, iy + thickness + 1, color);
		}
	}

	private static int colorForBlood(IBloodVolume volume) {
		if (volume.getMaxBloodVolume() <= 0) {
			return BAD;
		}
		double percent = volume.getBloodVolume() / volume.getMaxBloodVolume();
		if (percent >= 0.5D) {
			return GOOD;
		}
		if (percent >= 0.25D) {
			return WARN;
		}
		return BAD;
	}

	private static EnumBloodTendency strongest(IBloodTendency tendency) {
		EnumBloodTendency best = EnumBloodTendency.ANIMUS;
		for (EnumBloodTendency candidate : EnumBloodTendency.values()) {
			if (tendency.getAlignmentByTendency(candidate) > tendency.getAlignmentByTendency(best)) {
				best = candidate;
			}
		}
		return best;
	}

	private static EnumBloodTendency weakest(IBloodTendency tendency) {
		EnumBloodTendency weakest = EnumBloodTendency.ANIMUS;
		for (EnumBloodTendency candidate : EnumBloodTendency.values()) {
			if (tendency.getAlignmentByTendency(candidate) < tendency.getAlignmentByTendency(weakest)) {
				weakest = candidate;
			}
		}
		return weakest;
	}

	private static String tendencyLabel(IBloodTendency tendency, EnumBloodTendency value) {
		return titleCase(value.name()) + " " + String.format(Locale.ROOT, "%.2f",
				tendency.getAlignmentByTendency(value));
	}

	private static int tendencyColor(EnumBloodTendency tendency) {
		return 0xFF000000 | tendency.getColor().getColor();
	}

	private static float averageHealth(IVascularSystem vascular) {
		float total = 0.0F;
		for (EnumVeinSections section : EnumVeinSections.values()) {
			total += Mth.clamp(vascular.getHealthBySection(section), 0.0F, 100.0F);
		}
		return total / EnumVeinSections.values().length;
	}

	private static EnumVeinSections weakestSection(IVascularSystem vascular) {
		EnumVeinSections weakest = EnumVeinSections.HEAD;
		for (EnumVeinSections section : EnumVeinSections.values()) {
			if (vascular.getHealthBySection(section) < vascular.getHealthBySection(weakest)) {
				weakest = section;
			}
		}
		return weakest;
	}

	private static int colorForHealth(float health) {
		if (health >= 75.0F) {
			return GOOD;
		}
		if (health >= 40.0F) {
			return WARN;
		}
		return BAD;
	}

	private static String selectedManipName(IKnownManipulations known) {
		BloodManipulation selected = known.getSelectedManip();
		if (selected == null || selected == BloodManipulation.BLANK) {
			return "None selected";
		}
		return selected.getProperName();
	}

	private static String format(double value) {
		return String.format(Locale.ROOT, "%.0f", value);
	}

	private static String titleCase(String value) {
		String[] parts = value.toLowerCase(Locale.ROOT).split("_");
		StringBuilder builder = new StringBuilder();
		for (String part : parts) {
			if (part.isEmpty()) {
				continue;
			}
			if (!builder.isEmpty()) {
				builder.append(' ');
			}
			builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
		}
		return builder.toString();
	}
}
