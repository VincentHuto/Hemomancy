package com.vincenthuto.hemomancy.client.screen.item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.client.data.BloodFlowDiagnosticsClientData;
import com.vincenthuto.hemomancy.client.data.ManipulationCostDiagnosticsClientData;
import com.vincenthuto.hemomancy.client.data.ManipulationSlotDiagnosticsClientData;
import com.vincenthuto.hemomancy.client.data.MaxBloodDiagnosticsClientData;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowSnapshot;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.MaxBloodSnapshot;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostSnapshot;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationEquipHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationSlotSnapshot;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;

public class ScryingDiagnosticsScreen extends AbstractContainerScreen<ScryingDiagnosticsMenu> {
	private static final int PANEL_WIDTH = 322;
	private static final int PANEL_HEIGHT = 230;
	private static final int TAB_HEIGHT = 16;
	private static final int TAB_PAD = 4;
	private static final int TAB_TOP = 34;
	private static final int CONTENT_TOP = 56;
	private static final int FLOW_VISIBLE_ROWS = 5;
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
	private int flowScrollOffset = 0;
	private ScryingDiagnosticsTabs.Tab activeTab = ScryingDiagnosticsTabs.Tab.BLOOD;
	private HoverRegion maxBloodModsRegion = HoverRegion.EMPTY;
	private HoverRegion bloodFlowModsRegion = HoverRegion.EMPTY;
	private HoverRegion manipulationCostModsRegion = HoverRegion.EMPTY;
	private HoverRegion manipulationSlotModsRegion = HoverRegion.EMPTY;

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
		renderDiagnostics(graphics, mouseX, mouseY);
		this.renderTooltip(graphics, mouseX, mouseY);
		renderDiagnosticsTooltips(graphics, mouseX, mouseY);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			int index = ScreenDrawUtils.tabIndexUnder(font, ScryingDiagnosticsTabs.descriptors(activeTab),
					this.leftPos, tabsVirtualTop(), PANEL_WIDTH, TAB_HEIGHT, TAB_PAD, mouseX, mouseY);
			if (index >= 0) {
				activeTab = ScryingDiagnosticsTabs.tabs().get(index);
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	private void renderDiagnostics(GuiGraphics graphics, int mouseX, int mouseY) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		int x = this.leftPos;
		int y = this.topPos;
		maxBloodModsRegion = HoverRegion.EMPTY;
		bloodFlowModsRegion = HoverRegion.EMPTY;
		manipulationCostModsRegion = HoverRegion.EMPTY;
		manipulationSlotModsRegion = HoverRegion.EMPTY;
		graphics.drawCenteredString(this.font, this.title, x + PANEL_WIDTH / 2, y + 8, HEADER);
		graphics.drawCenteredString(this.font, "The podium reads what the blood is already saying.",
				x + PANEL_WIDTH / 2, y + 21, MUTED);
		drawTabs(graphics, mouseX, mouseY);

		switch (activeTab) {
			case BLOOD -> drawBloodTab(graphics, player, x + 16, y + CONTENT_TOP);
			case MANIPULATIONS -> drawManipulationsTab(graphics, player, x + 16, y + CONTENT_TOP);
			case TENDENCY -> drawTendencyTab(graphics, player, x + 16, y + CONTENT_TOP);
		}
	}

	private void drawTabs(GuiGraphics graphics, int mouseX, int mouseY) {
		ScreenDrawUtils.drawTabs(graphics, font, ScryingDiagnosticsTabs.descriptors(activeTab),
				this.leftPos, tabsVirtualTop(), PANEL_WIDTH, TAB_HEIGHT, TAB_PAD, mouseX, mouseY);
	}

	private int tabsVirtualTop() {
		return this.topPos + TAB_TOP - TAB_PAD;
	}

	private void drawBloodTab(GuiGraphics graphics, LocalPlayer player, int x, int y) {
		int rightX = x + 152;
		drawBloodVolume(graphics, player, x, y);
		drawVascularHealth(graphics, player, rightX, y);
		drawBloodFlow(graphics, player, x, y + 58);
	}

	private void drawManipulationsTab(GuiGraphics graphics, LocalPlayer player, int x, int y) {
		drawKnownMemories(graphics, player, x, y);
	}

	private void drawTendencyTab(GuiGraphics graphics, LocalPlayer player, int x, int y) {
		drawTendency(graphics, player, x, y);
		drawTendencyProfile(graphics, player, x, y + 48);
		drawRiteReadiness(graphics, player, x, y + 122);
	}

	private void drawBloodVolume(GuiGraphics graphics, LocalPlayer player, int x, int y) {
		drawSection(graphics, "Blood Volume", x, y);
		HemoCapabilityAccess.getBloodVolume(player).ifPresentOrElse(volume -> {
			MaxBloodSnapshot maxBlood = MaxBloodDiagnosticsClientData.get();
			double totalMax = volume.isActive() ? maxBlood.totalMax() : volume.getMaxBloodVolume();
			String reserve = format(volume.getBloodVolume()) + " / " + format(totalMax);
			drawValue(graphics, "Reserve", reserve, x, y + 13, colorForBlood(volume, totalMax));
			if (volume.isActive()) {
				drawValue(graphics, "Max", format(totalMax) + " (" + signedWhole(maxBlood.netBonus()) + ")",
						x, y + 25, colorForMaxBonus(maxBlood.netBonus()));
				drawValue(graphics, "Mods", signedWhole(maxBlood.positiveBonus()) + " / "
						+ signedWhole(-maxBlood.negativePenalty()), x, y + 37,
						colorForMaxBonus(maxBlood.netBonus()));
				maxBloodModsRegion = new HoverRegion(x, y + 37, 142, 10);
			} else {
				drawValue(graphics, "State", "Dormant", x, y + 25, WARN);
			}
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
			ManipulationSlotSnapshot slots = ManipulationSlotDiagnosticsClientData.get();
			ManipulationCostSnapshot cost = ManipulationCostDiagnosticsClientData.get();
			String equipped = slots.maxSlots() > 0
					? equippedCount + " / " + slots.appliedSlots()
					: equippedCount + " slotted";
			String costText = isEmptyCost(cost)
					? "No selected cost"
					: amount(cost.baseCost()) + " -> " + amount(cost.effectiveCost()) + " mL";
			drawValue(graphics, "Carried", knownCount + " memories", x, y + 13, TEXT);
			drawValue(graphics, "Equipped", equipped, x, y + 25,
					slots.cappedOffSlots() > 0 ? WARN : TEXT);
			drawValue(graphics, "Selected", trimToWidth(selectedManipName(known), PANEL_WIDTH - 106),
					x, y + 37, TEXT);
			drawValue(graphics, "Cost", trimToWidth(costText, PANEL_WIDTH - 106), x, y + 49,
					colorForCost(cost));
			manipulationSlotModsRegion = new HoverRegion(x, y + 25, 142, 10);
			manipulationCostModsRegion = new HoverRegion(x, y + 49, 220, 10);
		}, () -> drawLine(graphics, "No manipulation record found.", x, y + 13, BAD));
	}

	private void drawTendencyProfile(GuiGraphics graphics, LocalPlayer player, int x, int y) {
		drawSection(graphics, "Tendency Profile", x, y);
		HemoCapabilityAccess.getBloodTendency(player).ifPresentOrElse(tendency -> {
			EnumBloodTendency[] values = EnumBloodTendency.values();
			for (int i = 0; i < values.length; i++) {
				EnumBloodTendency value = values[i];
				int col = i / 4;
				int row = i % 4;
				int rowX = x + col * 150;
				int rowY = y + 14 + row * 14;
				String label = trimToWidth(titleCase(value.name()), 70);
				String amount = String.format(Locale.ROOT, "%.2f", tendency.getAlignmentByTendency(value));
				graphics.drawString(this.font, label, rowX, rowY, tendencyColor(value));
				graphics.drawString(this.font, amount, rowX + 78, rowY, TEXT);
			}
		}, () -> drawLine(graphics, "No tendency record found.", x, y + 13, BAD));
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

	private void drawBloodFlow(GuiGraphics graphics, LocalPlayer player, int x, int y) {
		drawSection(graphics, "Blood Flow", x, y);
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (volume == null) {
			drawLine(graphics, "Unreadable: no blood record.", x, y + 13, BAD);
			return;
		}
		if (!volume.isActive()) {
			drawLine(graphics, "Dormant: no blood magic flow.", x, y + 13, MUTED);
			return;
		}

		BloodFlowSnapshot snapshot = BloodFlowDiagnosticsClientData.get();
		bloodFlowModsRegion = new HoverRegion(x, y + 13, PANEL_WIDTH - 32, 62);
		String positive = "Positive " + signed(snapshot.positivePerTick()) + " mL/t";
		String negative = "Negative " + signed(-snapshot.negativePerTick()) + " mL/t";
		String net = "Net " + signed(snapshot.netPerTick()) + " mL/t";
		graphics.drawString(this.font, positive, x, y + 13, GOOD);
		graphics.drawString(this.font, negative, x + 104, y + 13, BAD);
		graphics.drawString(this.font, net, x + 210, y + 13, colorForRate(snapshot.netPerTick()));

		String bandwidth = snapshot.circulationEnabled()
				? "Bandwidth " + amount(snapshot.circulationUsedInWindow()) + " used / "
				+ amount(snapshot.circulationBandwidthPerWindow()) + " cap / "
				+ amount(snapshot.circulationRemainingInWindow()) + " available"
				: "Bandwidth disabled: capped sources pass through";
		drawLine(graphics, trimToWidth(bandwidth, PANEL_WIDTH - 32), x, y + 25, MUTED);

		int rowCount = snapshot.contributions().size();
		int maxOffset = Math.max(0, rowCount - FLOW_VISIBLE_ROWS);
		flowScrollOffset = Mth.clamp(flowScrollOffset, 0, maxOffset);
		if (rowCount == 0) {
			drawLine(graphics, "No active blood-flow rows.", x, y + 39, MUTED);
			return;
		}
		for (int i = 0; i < FLOW_VISIBLE_ROWS && flowScrollOffset + i < rowCount; i++) {
			BloodFlowContribution contribution = snapshot.contributions().get(flowScrollOffset + i);
			int rowY = y + 39 + i * 12;
			String label = trimToWidth(sourceLabel(contribution), 205);
			String rate = rowRate(contribution);
			graphics.drawString(this.font, label, x, rowY, rowColor(contribution));
			graphics.drawString(this.font, rate, x + 218, rowY, rowColor(contribution));
		}
	}

	private void renderDiagnosticsTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		if (maxBloodModsRegion.contains(mouseX, mouseY)) {
			renderStringTooltip(graphics, "Max Blood Modifiers",
					ScryingDiagnosticsTooltipLines.maxBloodMods(MaxBloodDiagnosticsClientData.get()), mouseX, mouseY);
		} else if (bloodFlowModsRegion.contains(mouseX, mouseY)) {
			renderStringTooltip(graphics, "Blood Flow Modifiers",
					ScryingDiagnosticsTooltipLines.bloodFlowMods(BloodFlowDiagnosticsClientData.get()), mouseX, mouseY);
		} else if (manipulationSlotModsRegion.contains(mouseX, mouseY)) {
			renderStringTooltip(graphics, "Manipulation Slot Modifiers",
					ScryingDiagnosticsTooltipLines.manipulationSlotMods(ManipulationSlotDiagnosticsClientData.get()),
					mouseX, mouseY);
		} else if (manipulationCostModsRegion.contains(mouseX, mouseY)) {
			renderStringTooltip(graphics, "Selected Manipulation Cost",
					ScryingDiagnosticsTooltipLines.manipulationCostMods(ManipulationCostDiagnosticsClientData.get()),
					mouseX, mouseY);
		}
	}

	private void renderStringTooltip(GuiGraphics graphics, String title, List<String> lines, int mouseX, int mouseY) {
		List<Component> tooltip = new ArrayList<>();
		tooltip.add(Component.literal(title));
		for (String line : lines) {
			tooltip.add(Component.literal(line));
		}
		graphics.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (activeTab != ScryingDiagnosticsTabs.Tab.BLOOD) {
			return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
		}
		BloodFlowSnapshot snapshot = BloodFlowDiagnosticsClientData.get();
		int maxOffset = Math.max(0, snapshot.contributions().size() - FLOW_VISIBLE_ROWS);
		if (maxOffset > 0 && mouseX >= this.leftPos && mouseX <= this.leftPos + PANEL_WIDTH
				&& mouseY >= this.topPos && mouseY <= this.topPos + PANEL_HEIGHT) {
			flowScrollOffset = Mth.clamp(flowScrollOffset - (int) Math.signum(scrollY), 0, maxOffset);
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
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

	private String trimToWidth(String text, int maxWidth) {
		if (this.font.width(text) <= maxWidth) {
			return text;
		}
		return this.font.plainSubstrByWidth(text, Math.max(0, maxWidth - this.font.width("..."))) + "...";
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

	private static int colorForBlood(IBloodVolume volume, double maxBlood) {
		if (maxBlood <= 0) {
			return BAD;
		}
		double percent = volume.getBloodVolume() / maxBlood;
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

	private static String amount(double value) {
		return String.format(Locale.ROOT, "%.2f", value);
	}

	private static String signed(double value) {
		return String.format(Locale.ROOT, "%+.2f", value);
	}

	private static String signedWhole(double value) {
		return String.format(Locale.ROOT, "%+.0f", value);
	}

	private static int colorForMaxBonus(double value) {
		if (value > 0.000001D) {
			return GOOD;
		}
		if (value < -0.000001D) {
			return BAD;
		}
		return TEXT;
	}

	private static int colorForRate(double value) {
		if (value > 0.000001D) {
			return GOOD;
		}
		if (value < -0.000001D) {
			return BAD;
		}
		return MUTED;
	}

	private static int colorForCost(ManipulationCostSnapshot snapshot) {
		if (isEmptyCost(snapshot)) {
			return MUTED;
		}
		if (snapshot.effectiveCost() + 0.000001D < snapshot.baseCost()) {
			return GOOD;
		}
		if (snapshot.effectiveCost() > snapshot.baseCost() + 0.000001D) {
			return BAD;
		}
		return TEXT;
	}

	private static boolean isEmptyCost(ManipulationCostSnapshot snapshot) {
		return snapshot == null || (snapshot.contributions().isEmpty()
				&& Math.abs(snapshot.baseCost()) < 0.000001D
				&& Math.abs(snapshot.effectiveCost()) < 0.000001D);
	}

	private static int rowColor(BloodFlowContribution contribution) {
		if (!contribution.condition().isEmpty()) {
			return WARN;
		}
		return colorForRate(contribution.appliedPerTick());
	}

	private static String rowRate(BloodFlowContribution contribution) {
		if (Math.abs(contribution.requestedPerTick() - contribution.appliedPerTick()) > 0.000001D) {
			return signed(contribution.appliedPerTick()) + "/" + signed(contribution.requestedPerTick());
		}
		return signed(contribution.appliedPerTick());
	}

	private static String sourceLabel(BloodFlowContribution contribution) {
		String label = titleCase(contribution.category().name()) + " - " + contribution.label();
		if (!contribution.condition().isEmpty()) {
			label += " [" + contribution.condition() + "]";
		}
		return label;
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

	private record HoverRegion(int x, int y, int width, int height) {
		private static final HoverRegion EMPTY = new HoverRegion(0, 0, 0, 0);

		private boolean contains(int mouseX, int mouseY) {
			return width > 0 && height > 0
					&& mouseX >= x && mouseX < x + width
					&& mouseY >= y && mouseY < y + height;
		}
	}
}
