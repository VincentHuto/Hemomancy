package com.vincenthuto.hemomancy.client.screen.item;

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
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.menu.ScryingDiagnosticsMenu;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import java.util.*;
import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.VeinBackgroundRenderer;

public class ScryingDiagnosticsScreen extends AbstractContainerScreen<ScryingDiagnosticsMenu> {
	private static final int PANEL_WIDTH = 322;
	private static final int PANEL_HEIGHT = 230;
	private static final int TAB_HEIGHT = 16;
	private static final int TAB_PAD = 4;
	private static final int TAB_TOP = 34;
	private static final int CONTENT_TOP = 56;
	private static final int FLOW_VISIBLE_ROWS = 5;
	private static final int PANEL = 0xF00A0204;
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

	/** Ten short tendrils drawn every fourth step, and 48 motes rolled once: a cheap panel. */
	private static final VeinBackgroundRenderer.Style VEINS = new VeinBackgroundRenderer.Style(
			new VeinBackgroundRenderer.Field(10, 42L, 0.3f, 0.7f, 7f, 15f, 56, 72, 3, 4, 1, false),
			VeinBackgroundRenderer.Palette.CRIMSON.background(PANEL).ringStep(12),
			new VeinBackgroundRenderer.Speckles.Fixed(12345L, 48, PANEL_WIDTH, PANEL_HEIGHT, 10, 20, 6, 0, 15, 25));
	private final VeinBackgroundRenderer veins = new VeinBackgroundRenderer(VEINS);
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
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		int x = this.leftPos;
		int y = this.topPos;
		veins.draw(graphics, x, y, PANEL_WIDTH, PANEL_HEIGHT, advanceVeinClock());
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
		graphics.drawCenteredString(this.font,
				Component.translatable("screen.hemomancy.scrying_diagnostics.the_podium_reads_what_the_blood_is"),
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
		drawSection(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.blood_volume"), x, y);
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		MaxBloodSnapshot maxBlood = MaxBloodDiagnosticsClientData.get();
		double totalMax = volume.isActive() ? maxBlood.totalMax() : volume.getMaxBloodVolume();
		String reserve = format(volume.getBloodVolume()) + " / " + format(totalMax);
		drawValue(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.reserve"),
				Component.literal(reserve), x, y + 13, colorForBlood(volume, totalMax));
		if (volume.isActive()) {
			drawValue(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.max"),
					Component.literal(format(totalMax) + " (" + signedWhole(maxBlood.netBonus()) + ")"),
					x, y + 25, colorForMaxBonus(maxBlood.netBonus()));
			drawValue(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.mods"),
					Component.literal(signedWhole(maxBlood.positiveBonus()) + " / "
							+ signedWhole(-maxBlood.negativePenalty())), x, y + 37,
					colorForMaxBonus(maxBlood.netBonus()));
			maxBloodModsRegion = new HoverRegion(x, y + 37, 142, 10);
		} else {
			drawValue(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.state"),
					Component.translatable("screen.hemomancy.scrying_diagnostics.dormant"), x, y + 25, WARN);
		}
	}

	private void drawTendency(GuiGraphics graphics, LocalPlayer player, int x, int y) {
		drawSection(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.dominant_tendency"), x, y);
		var tendency = HemoCapabilityAccess.getBloodTendency(player);
		EnumBloodTendency dominant = strongest(tendency);
		EnumBloodTendency latent = weakest(tendency);
		drawValue(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.dominant"),
				tendencyLabel(tendency, dominant), x, y + 13, tendencyColor(dominant));
		drawValue(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.latent"),
				tendencyLabel(tendency, latent), x, y + 25, MUTED);
	}

	private void drawKnownMemories(GuiGraphics graphics, LocalPlayer player, int x, int y) {
		drawSection(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.known_memories"), x, y);
		var known = HemoCapabilityAccess.getKnownManipulations(player);
		int knownCount = known.getKnownManips().size();
		int equippedCount = ManipulationEquipHelper.countNormalEquippedNames(known.getEquippedManipNames());
		ManipulationSlotSnapshot slots = ManipulationSlotDiagnosticsClientData.get();
		ManipulationCostSnapshot cost = ManipulationCostDiagnosticsClientData.get();
		Component equipped = slots.maxSlots() > 0
				? Component.literal(equippedCount + " / " + slots.appliedSlots())
				: Component.translatable("screen.hemomancy.scrying_diagnostics.slotted", equippedCount);
		Component costText = isEmptyCost(cost)
				? Component.translatable("screen.hemomancy.scrying_diagnostics.no_selected_cost")
				: Component.translatable("screen.hemomancy.scrying_diagnostics.cost_change_ml",
						amount(cost.baseCost()), amount(cost.effectiveCost()));
		drawValue(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.carried"),
				Component.translatable("screen.hemomancy.scrying_diagnostics.memories", knownCount), x, y + 13, TEXT);
		drawValue(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.equipped"), equipped, x, y + 25,
				slots.cappedOffSlots() > 0 ? WARN : TEXT);
		drawValue(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.selected"),
				Component.literal(trimToWidth(selectedManipName(known).getString(), PANEL_WIDTH - 106)),
				x, y + 37, TEXT);
		drawValue(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.cost"),
				Component.literal(trimToWidth(costText.getString(), PANEL_WIDTH - 106)), x, y + 49,
				colorForCost(cost));
		manipulationSlotModsRegion = new HoverRegion(x, y + 25, 142, 10);
		manipulationCostModsRegion = new HoverRegion(x, y + 49, 220, 10);
	}

	private void drawTendencyProfile(GuiGraphics graphics, LocalPlayer player, int x, int y) {
		drawSection(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.tendency_profile"), x, y);
		var tendency = HemoCapabilityAccess.getBloodTendency(player);
		EnumBloodTendency[] values = EnumBloodTendency.values();
		for (int i = 0; i < values.length; i++) {
			EnumBloodTendency value = values[i];
			int col = i / 4;
			int row = i % 4;
			int rowX = x + col * 150;
			int rowY = y + 14 + row * 14;
			String label = trimToWidth(BloodManipulation.tendencyName(value).getString(), 70);
			String amount = String.format(Locale.ROOT, "%.2f", tendency.getAlignmentByTendency(value));
			graphics.drawString(this.font, label, rowX, rowY, tendencyColor(value));
			graphics.drawString(this.font, amount, rowX + 78, rowY, TEXT);
		}
	}

	private void drawVascularHealth(GuiGraphics graphics, LocalPlayer player, int x, int y) {
		drawSection(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.vascular_health"), x, y);
		var vascular = HemoCapabilityAccess.getVascularSystem(player);
		float average = averageHealth(vascular);
		EnumVeinSections worst = weakestSection(vascular);
		drawValue(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.average"),
				Component.literal(String.format(Locale.ROOT, "%.0f%%", average)), x, y + 13,
				colorForHealth(average));
		drawValue(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.weakest"),
				Component.translatable("screen.hemomancy.scrying_diagnostics.section_health",
						BloodManipulation.sectionName(worst),
						String.format(Locale.ROOT, "%.0f", vascular.getHealthBySection(worst))), x, y + 25,
				colorForHealth(vascular.getHealthBySection(worst)));
		drawValue(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.flow"),
				Component.translatable("blood_flow_state.hemomancy."
						+ vascular.getBloodFlowBySection(worst).name().toLowerCase(Locale.ROOT)), x, y + 37, MUTED);
	}

	private void drawRiteReadiness(GuiGraphics graphics, LocalPlayer player, int x, int y) {
		drawSection(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.rite_readiness"), x, y);
		int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		if (volume == null) {
			drawLine(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.unreadable_no_blood_record"), x, y + 13, BAD);
			return;
		}
		if (!volume.isActive()) {
			drawLine(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.dormant_blood_magic_is_inactive"), x, y + 13, WARN);
		} else if (degree <= 0) {
			drawLine(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.uninitiated_no_rite_standing"), x, y + 13, WARN);
		} else if (volume.getBloodVolume() < volume.getMaxBloodVolume() * 0.25D) {
			drawLine(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.strained_restore_blood_first"), x, y + 13, BAD);
		} else {
			drawLine(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.stable_ordinary_rites_are_supported"), x, y + 13, GOOD);
		}
		drawValue(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.degree"),
				Component.literal(String.valueOf(degree)), x, y + 29, TEXT);
	}

	private void drawBloodFlow(GuiGraphics graphics, LocalPlayer player, int x, int y) {
		drawSection(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.blood_flow"), x, y);
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		if (volume == null) {
			drawLine(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.unreadable_no_blood_record"), x, y + 13, BAD);
			return;
		}
		if (!volume.isActive()) {
			drawLine(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.dormant_no_blood_magic_flow"), x, y + 13, MUTED);
			return;
		}

		BloodFlowSnapshot snapshot = BloodFlowDiagnosticsClientData.get();
		bloodFlowModsRegion = new HoverRegion(x, y + 13, PANEL_WIDTH - 32, 62);
		Component positive = Component.translatable("screen.hemomancy.scrying_diagnostics.positive_ml_per_tick",
				signed(snapshot.positivePerTick()));
		Component negative = Component.translatable("screen.hemomancy.scrying_diagnostics.negative_ml_per_tick",
				signed(-snapshot.negativePerTick()));
		Component net = Component.translatable("screen.hemomancy.scrying_diagnostics.net_ml_per_tick",
				signed(snapshot.netPerTick()));
		graphics.drawString(this.font, positive, x, y + 13, GOOD);
		graphics.drawString(this.font, negative, x + 104, y + 13, BAD);
		graphics.drawString(this.font, net, x + 210, y + 13, colorForRate(snapshot.netPerTick()));

		Component bandwidth = snapshot.circulationEnabled()
				? Component.translatable("screen.hemomancy.scrying_diagnostics.bandwidth_used_cap_available",
						amount(snapshot.circulationUsedInWindow()),
						amount(snapshot.circulationBandwidthPerWindow()),
						amount(snapshot.circulationRemainingInWindow()))
				: Component.translatable("screen.hemomancy.scrying_diagnostics.bandwidth_disabled_capped_sources_pass");
		drawLine(graphics, Component.literal(trimToWidth(bandwidth.getString(), PANEL_WIDTH - 32)), x, y + 25, MUTED);

		int rowCount = snapshot.contributions().size();
		int maxOffset = Math.max(0, rowCount - FLOW_VISIBLE_ROWS);
		flowScrollOffset = Mth.clamp(flowScrollOffset, 0, maxOffset);
		if (rowCount == 0) {
			drawLine(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.no_active_blood_flow_rows"), x, y + 39, MUTED);
			return;
		}
		for (int i = 0; i < FLOW_VISIBLE_ROWS && flowScrollOffset + i < rowCount; i++) {
			BloodFlowContribution contribution = snapshot.contributions().get(flowScrollOffset + i);
			int rowY = y + 39 + i * 12;
			String label = trimToWidth(sourceLabel(contribution).getString(), 205);
			String rate = rowRate(contribution);
			graphics.drawString(this.font, label, x, rowY, rowColor(contribution));
			graphics.drawString(this.font, rate, x + 218, rowY, rowColor(contribution));
		}
	}

	private void renderDiagnosticsTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		if (maxBloodModsRegion.contains(mouseX, mouseY)) {
			renderStringTooltip(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.max_blood_modifiers"),
					ScryingDiagnosticsTooltipLines.maxBloodMods(MaxBloodDiagnosticsClientData.get()), mouseX, mouseY);
		} else if (bloodFlowModsRegion.contains(mouseX, mouseY)) {
			renderStringTooltip(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.blood_flow_modifiers"),
					ScryingDiagnosticsTooltipLines.bloodFlowMods(BloodFlowDiagnosticsClientData.get()), mouseX, mouseY);
		} else if (manipulationSlotModsRegion.contains(mouseX, mouseY)) {
			renderStringTooltip(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.manipulation_slot_modifiers"),
					ScryingDiagnosticsTooltipLines.manipulationSlotMods(ManipulationSlotDiagnosticsClientData.get()),
					mouseX, mouseY);
		} else if (manipulationCostModsRegion.contains(mouseX, mouseY)) {
			renderStringTooltip(graphics, Component.translatable("screen.hemomancy.scrying_diagnostics.selected_manipulation_cost"),
					ScryingDiagnosticsTooltipLines.manipulationCostMods(ManipulationCostDiagnosticsClientData.get()),
					mouseX, mouseY);
		}
	}

	private void renderStringTooltip(GuiGraphics graphics, Component title, List<Component> lines, int mouseX, int mouseY) {
		List<Component> tooltip = new ArrayList<>();
		tooltip.add(title);
		tooltip.addAll(lines);
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

	private void drawSection(GuiGraphics graphics, Component text, int x, int y) {
		graphics.drawString(this.font, text, x, y, SECTION);
	}

	private void drawValue(GuiGraphics graphics, Component label, Component value, int x, int y, int color) {
		graphics.drawString(this.font, Component.translatable("screen.hemomancy.scrying_diagnostics.value_label", label),
				x, y, MUTED);
		graphics.drawString(this.font, value, x + 58, y, color);
	}

	private void drawLine(GuiGraphics graphics, Component text, int x, int y, int color) {
		graphics.drawString(this.font, text, x, y, color);
	}

	private String trimToWidth(String text, int maxWidth) {
		if (this.font.width(text) <= maxWidth) {
			return text;
		}
		return this.font.plainSubstrByWidth(text, Math.max(0, maxWidth - this.font.width("..."))) + "...";
	}

	/** Wall time, capped per frame and slowed by VEIN_ANIMATION_SPEED. */
	private float advanceVeinClock() {
		long now = Util.getMillis();
		if (lastAnimMillis >= 0L) {
			float deltaSeconds = Math.min((now - lastAnimMillis) / 1000.0F, 0.05F);
			animTime += deltaSeconds * VEIN_ANIMATION_SPEED;
		}
		lastAnimMillis = now;
		return animTime;
	}

	private void drawFrame(GuiGraphics graphics, int x, int y, int width, int height) {
		ScreenDrawUtils.drawBevelBorder(graphics, x, y, width, height, BORDER, BORDER_DARK);
		graphics.fill(x + 6, y + 33, x + width - 6, y + 34, 0xFF2B0707);
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

	private static Component tendencyLabel(IBloodTendency tendency, EnumBloodTendency value) {
		return Component.translatable("screen.hemomancy.scrying_diagnostics.tendency_alignment",
				BloodManipulation.tendencyName(value),
				String.format(Locale.ROOT, "%.2f", tendency.getAlignmentByTendency(value)));
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

	private static Component selectedManipName(IKnownManipulations known) {
		BloodManipulation selected = known.getSelectedManip();
		if (selected == null || selected == BloodManipulation.BLANK) {
			return Component.translatable("screen.hemomancy.scrying_diagnostics.none_selected");
		}
		return selected.getDisplayName();
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

	private static Component sourceLabel(BloodFlowContribution contribution) {
		return ScryingDiagnosticsTooltipLines.sourceLabel(contribution.category(), contribution.label(),
				contribution.condition());
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
