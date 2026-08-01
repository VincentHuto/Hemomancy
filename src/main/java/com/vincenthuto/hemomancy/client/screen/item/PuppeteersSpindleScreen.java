package com.vincenthuto.hemomancy.client.screen.item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.util.InventoryPanelTextures;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.menu.PuppeteersSpindleMenu;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.summon.PacketPuppeteersSpindleAction;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinition;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonRules;
import com.vincenthuto.hemomancy.common.tile.crafting.PuppeteersSpindleBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PuppeteersSpindleScreen extends AbstractContainerScreen<PuppeteersSpindleMenu> {
	private static final int GUI_WIDTH = 280;
	private static final int GUI_HEIGHT = 234;
	private static final int CRAFT_AREA_HEIGHT = 146;
	private static final int VEIN_COUNT = 10;
	private static final int INPUT_X = 8;
	private static final int PATTERN_X = 132;
	private static final int PANEL_Y = 28;
	private static final int INPUT_W = 116;
	private static final int PATTERN_W = 140;
	private static final int PANEL_H = 102;
	private static final int SLOT_BG = 0xFF1A0808;
	private static final int SLOT_BORDER_DARK = 0xFF0D0303;
	private static final int SLOT_BORDER_LIGHT = 0xFF4A151B;
	private static final int BORDER_OUTER = 0xFF3A080D;
	private static final int BORDER_INNER = 0xFF220509;
	private static final int PANEL_BG = 0xDD130507;
	private static final int PANEL_EDGE = 0xFF43131A;
	private static final int TEXT_MUTED = 0xFFB98F8C;
	private static final int TEXT_RED = 0xFFFFB6AA;
	private static final int TEXT_LOCKED = 0xFF65565A;

	private final List<String> knownSummons = new ArrayList<>();
	private int selectedIndex;
	private float animTime;
	private float[][] veinParams;

	public PuppeteersSpindleScreen(PuppeteersSpindleMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
		this.imageWidth = GUI_WIDTH;
		this.imageHeight = GUI_HEIGHT;
		this.inventoryLabelX = 78;
		this.inventoryLabelY = CRAFT_AREA_HEIGHT + 7;
	}

	@Override
	protected void init() {
		super.init();
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
		reloadKnownSummons();
		seedVeins();
	}

	private void seedVeins() {
		Random rand = new Random(54321L);
		veinParams = new float[VEIN_COUNT][9];
		for (int i = 0; i < VEIN_COUNT; i++) {
			veinParams[i][0] = rand.nextFloat();
			veinParams[i][1] = rand.nextFloat();
			veinParams[i][2] = (float) (rand.nextFloat() * Math.PI * 2);
			veinParams[i][3] = 0.35f + rand.nextFloat() * 0.7f;
			veinParams[i][4] = 7f + rand.nextFloat() * 14f;
			veinParams[i][5] = 0.05f + rand.nextFloat() * 0.08f;
			veinParams[i][6] = 42 + rand.nextInt(90);
			veinParams[i][7] = 1 + rand.nextInt(2);
			veinParams[i][8] = rand.nextFloat();
		}
	}

	private void reloadKnownSummons() {
		knownSummons.clear();
		Player player = Minecraft.getInstance().player;
		if (player != null) {
			HemoCapabilityAccess.getKnownSummons(player)
					.ifPresent(known -> knownSummons.addAll(known.getKnownSummonNames()));
		}
		String selected = menu.getSelectedSummonName();
		selectedIndex = 0;
		for (int i = 0; i < PuppeteerSummonDefinitions.all().size(); i++) {
			if (PuppeteerSummonDefinitions.all().get(i).name().equals(selected)) {
				selectedIndex = i;
				break;
			}
		}
	}

	private void sendPrepare(String summon) {
		PacketHandler.sendToServer(new PacketPuppeteersSpindleAction(
				PacketPuppeteersSpindleAction.Action.PREPARE, summon == null ? "" : summon));
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderBackground(graphics, mouseX, mouseY, partialTick);
		super.render(graphics, mouseX, mouseY, partialTick);
		renderHoverTooltips(graphics, mouseX, mouseY);
		renderTooltip(graphics, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			int clicked = summonRowAt(mouseX, mouseY);
			if (clicked >= 0) {
				String clickedName = PuppeteerSummonDefinitions.all().get(clicked).name();
				if (!knownSummons.contains(clickedName)) return true;
				selectedIndex = clicked;
				sendPrepare(selectedSummonName());
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		animTime += 0.004f;
		int gx = this.leftPos;
		int gy = this.topPos;

		renderVeinBackground(graphics, gx, gy, imageWidth, CRAFT_AREA_HEIGHT);
		drawPanel(graphics, gx + INPUT_X, gy + PANEL_Y, INPUT_W, PANEL_H);
		drawPanel(graphics, gx + PATTERN_X, gy + PANEL_Y, PATTERN_W, PANEL_H);
		drawBorder(graphics, gx, gy, imageWidth, CRAFT_AREA_HEIGHT);
		Slot firstInventorySlot = this.menu.slots.get(PuppeteersSpindleMenu.SLOT_COUNT);
		InventoryPanelTextures.blit(graphics, InventoryPanelTextures.BLOODY,
				gx + firstInventorySlot.x - 5, gy + firstInventorySlot.y - 6);

		for (int i = 0; i < PuppeteersSpindleMenu.SLOT_COUNT; i++) {
			Slot slot = menu.slots.get(i);
			drawSlotBackground(graphics, gx + slot.x, gy + slot.y, slot.index);
		}

		renderMeters(graphics, gx, gy);
		renderSummonRows(graphics, gx, gy, mouseX, mouseY);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.drawString(font, title, titleLabelX, 8, 0xFFFFD7D0, false);
		drawTrimmedString(graphics, Component.translatable("screen.hemomancy.puppeteers_spindle.inputs"),
				INPUT_X + 7, PANEL_Y + 8, INPUT_W - 14, TEXT_MUTED);
		drawTrimmedString(graphics, Component.translatable("screen.hemomancy.puppeteers_spindle.crossbar"),
				INPUT_X + 35, 50, INPUT_W - 40, TEXT_MUTED);
		drawTrimmedString(graphics, Component.translatable("screen.hemomancy.puppeteers_spindle.thread_slot_short"),
				INPUT_X + 35, 74, INPUT_W - 40, TEXT_MUTED);
		drawTrimmedString(graphics, Component.translatable("screen.hemomancy.puppeteers_spindle.patterns"),
				PATTERN_X + 7, PANEL_Y + 8, PATTERN_W - 14, TEXT_MUTED);
	}

	private void renderMeters(GuiGraphics graphics, int gx, int gy) {
		int x = gx + INPUT_X + 8;
		int w = INPUT_W - 16;
		drawMeterLabelValue(graphics, Component.translatable("screen.hemomancy.puppeteers_spindle.crossbar"),
				menu.getCrossbarThread(), menu.getCrossbarCapacity(), x, gy + 88, w);
		drawThreadSpoolMeter(graphics, x, gy + 98, w,
				menu.getCrossbarThread(), menu.getCrossbarCapacity(), 0xFFC72C3C, 0xFFFF7A7F);
		drawMeterLabelValue(graphics, Component.translatable("screen.hemomancy.puppeteers_spindle.spindle"),
				menu.getThreadBuffer(), PuppeteersSpindleBlockEntity.THREAD_BUFFER_CAPACITY, x, gy + 110, w);
		drawThreadSpoolMeter(graphics, x, gy + 120, w,
				menu.getThreadBuffer(), PuppeteersSpindleBlockEntity.THREAD_BUFFER_CAPACITY,
				0xFF9E3041, 0xFFE96A74);
	}

	private void drawMeterLabelValue(GuiGraphics graphics, Component label, int current, int capacity,
			int x, int y, int width) {
		String value = current + " / " + capacity;
		int valueWidth = font.width(value);
		drawTrimmedString(graphics, label, x, y, Math.max(0, width - valueWidth - 4), TEXT_MUTED);
		graphics.drawString(font, value, x + width - valueWidth, y, TEXT_RED, false);
	}

	private void drawThreadSpoolMeter(GuiGraphics graphics, int x, int y, int width,
			int current, int capacity, int threadColor, int highlightColor) {
		float ratio = capacity <= 0 ? 0.0f : Mth.clamp(current / (float) capacity, 0.0f, 1.0f);
		int shaftStart = x + 5;
		int shaftEnd = x + width - 5;
		int threadStart = x + 6;
		int threadEnd = x + width - 10;
		int woundEnd = threadStart + Math.round((threadEnd - threadStart) * ratio);

		// Recessed needle shaft: visible wherever thread has not yet been wound.
		graphics.fill(shaftStart, y + 3, shaftEnd, y + 7, 0xFF090203);
		graphics.fill(shaftStart, y + 4, shaftEnd, y + 6, 0xFF5A2630);
		graphics.fill(shaftStart, y + 4, shaftEnd, y + 5, 0xFF9A4A55);
		drawSpoolEndCaps(graphics, x, y, width, highlightColor);
		drawThreadWraps(graphics, threadStart, woundEnd, y, threadColor, highlightColor);

		// The collar marks the live edge of the stored thread.
		int collar = Mth.clamp(woundEnd, threadStart, threadEnd);
		graphics.fill(collar - 1, y + 1, collar + 2, y + 9, 0xFF24070C);
		graphics.fill(collar, y + 2, collar + 1, y + 7, highlightColor);
		graphics.fill(collar, y + 7, collar + 1, y + 8, shadeColor(threadColor, 0.55f));
	}

	private void drawThreadWraps(GuiGraphics graphics, int start, int end, int y,
			int threadColor, int highlightColor) {
		if (end <= start) return;
		graphics.fill(start, y + 1, end + 1, y + 9, 0xFF2A070D);
		graphics.fill(start + 1, y + 2, end, y + 8, shadeColor(threadColor, 0.55f));
		graphics.fill(start + 1, y + 3, end, y + 7, threadColor);
		graphics.fill(start + 1, y + 3, end, y + 4, shadeColor(threadColor, 0.78f));
		graphics.fill(start + 1, y + 2, end, y + 3, highlightColor);
		for (int wrap = start + 2; wrap < end; wrap += 3) {
			graphics.fill(wrap, y + 2, wrap + 1, y + 8, shadeColor(threadColor, 0.55f));
			if (wrap + 1 < end) {
				graphics.fill(wrap + 1, y + 3, wrap + 2, y + 6, highlightColor);
				graphics.fill(wrap + 1, y + 6, wrap + 2, y + 7, shadeColor(threadColor, 0.78f));
			}
		}
	}

	private void drawSpoolEndCaps(GuiGraphics graphics, int x, int y, int width, int highlightColor) {
		graphics.fill(x, y + 3, x + 2, y + 7, 0xFF180408);
		graphics.fill(x + 2, y + 1, x + 5, y + 9, 0xFF3C1018);
		graphics.fill(x + 3, y + 2, x + 4, y + 6, highlightColor);
		graphics.fill(x + 3, y + 6, x + 4, y + 8, 0xFF6A202C);
		graphics.fill(x + width - 5, y + 1, x + width - 2, y + 9, 0xFF3C1018);
		graphics.fill(x + width - 4, y + 2, x + width - 3, y + 6, highlightColor);
		graphics.fill(x + width - 4, y + 6, x + width - 3, y + 8, 0xFF6A202C);
		graphics.fill(x + width - 2, y + 3, x + width, y + 7, 0xFF180408);
	}

	private static int shadeColor(int color, float factor) {
		int alpha = color >>> 24;
		int red = Math.round(((color >>> 16) & 0xFF) * factor);
		int green = Math.round(((color >>> 8) & 0xFF) * factor);
		int blue = Math.round((color & 0xFF) * factor);
		return alpha << 24 | red << 16 | green << 8 | blue;
	}

	private void renderSummonRows(GuiGraphics graphics, int gx, int gy, int mouseX, int mouseY) {
		List<PuppeteerSummonDefinition> definitions = PuppeteerSummonDefinitions.all();
		int x = gx + PATTERN_X + 6;
		int y = gy + 48;
		int w = PATTERN_W - 12;
		for (int i = 0; i < definitions.size(); i++) {
			PuppeteerSummonDefinition definition = definitions.get(i);
			boolean known = knownSummons.contains(definition.name());
			boolean selected = i == selectedIndex;
			boolean hovered = summonRowAt(mouseX, mouseY) == i;
			int rowY = y + i * 18;
			int bg = selected ? 0xDD240A12 : (hovered ? 0xBB1B080E : 0xAA0E0407);
			graphics.fill(x, rowY, x + w, rowY + 16, bg);
			graphics.fill(x, rowY, x + w, rowY + 1, selected ? 0xFFB93645 : 0xFF43131A);
			graphics.fill(x, rowY + 15, x + w, rowY + 16, 0xFF170407);
			if (selected) {
				graphics.fill(x, rowY, x + 2, rowY + 16, 0xFFFF6A70);
			}

			Component name = Component.translatable(definition.translationKey());
			int color = known ? TEXT_RED : TEXT_LOCKED;
			String label = trimToWidth(name.getString(), w - 10);
			graphics.drawString(font, label, x + 5, rowY + 4, color, false);
		}
	}

	private int playerDegree() {
		Player player = Minecraft.getInstance().player;
		return player == null ? 0 : HemoCapabilityAccess.getPlayerDegreeNumber(player);
	}

	private boolean hasTrialRecipe(PuppeteerSummonDefinition definition) {
		if (!(Minecraft.getInstance().player instanceof net.minecraft.client.player.LocalPlayer player)) {
			return false;
		}
		return player.getRecipeBook()
				.contains(com.vincenthuto.hemomancy.common.summon.PuppeteerSummonTrialEvents.recipeId(definition));
	}

	private int summonRowAt(double mouseX, double mouseY) {
		int x = leftPos + PATTERN_X + 6;
		int y = topPos + 48;
		int w = PATTERN_W - 12;
		List<PuppeteerSummonDefinition> definitions = PuppeteerSummonDefinitions.all();
		for (int i = 0; i < definitions.size(); i++) {
			int rowY = y + i * 18;
			if (mouseX >= x && mouseX <= x + w && mouseY >= rowY && mouseY <= rowY + 16) {
				return i;
			}
		}
		return -1;
	}

	private String selectedSummonName() {
		List<PuppeteerSummonDefinition> definitions = PuppeteerSummonDefinitions.all();
		if (definitions.isEmpty()) {
			return "";
		}
		return definitions.get(Mth.clamp(selectedIndex, 0, definitions.size() - 1)).name();
	}

	private void renderHoverTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		int row = summonRowAt(mouseX, mouseY);
		if (row >= 0) {
			PuppeteerSummonDefinition definition = PuppeteerSummonDefinitions.all().get(row);
			boolean known = knownSummons.contains(definition.name());
			int economy = SkillPointHelper.getThreadEconomyLevel();
			int callCost = PuppeteerSummonRules.adjustedThreadCost(definition.threadSummonCost(), economy);
			int upkeep = PuppeteerSummonRules.adjustedThreadCost(definition.threadUpkeepPerMinute(), economy);
			List<Component> tooltip = new ArrayList<>();
			tooltip.add(Component.translatable(definition.translationKey()).withStyle(ChatFormatting.RED));
			tooltip.add(Component.translatable("screen.hemomancy.puppeteers_spindle.cost",
					callCost, upkeep).withStyle(ChatFormatting.GRAY));
			if (known) {
				tooltip.add(Component.translatable("screen.hemomancy.puppeteers_spindle.known")
						.withStyle(ChatFormatting.DARK_RED));
			} else if (playerDegree() < definition.requiredDegree()) {
				tooltip.add(Component.translatable("screen.hemomancy.puppeteers_spindle.degree_required",
						definition.requiredDegree()).withStyle(ChatFormatting.DARK_GRAY));
			} else if (!hasTrialRecipe(definition)) {
				tooltip.add(Component.translatable("screen.hemomancy.puppeteers_spindle.recipe_required")
						.withStyle(ChatFormatting.DARK_GRAY));
			} else {
				tooltip.add(Component.translatable("screen.hemomancy.puppeteers_spindle.trial_required",
						definition.requiredDegree()).withStyle(ChatFormatting.DARK_GRAY));
			}
			graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
		}
		Slot threadSlot = menu.slots.get(PuppeteersSpindleMenu.THREAD_SLOT);
		if (isHovering(threadSlot.x, threadSlot.y, 16, 16, mouseX, mouseY)) {
			graphics.renderTooltip(font,
					Component.translatable("screen.hemomancy.puppeteers_spindle.thread_slot"),
					mouseX, mouseY);
		}
	}

	private void drawSlotBackground(GuiGraphics graphics, int sx, int sy, int slotIndex) {
		graphics.fill(sx - 1, sy - 1, sx + 17, sy + 17, SLOT_BORDER_DARK);
		graphics.fill(sx, sy, sx + 16, sy + 16, SLOT_BG);
		graphics.fill(sx + 16, sy, sx + 17, sy + 17, SLOT_BORDER_LIGHT);
		graphics.fill(sx, sy + 16, sx + 17, sy + 17, SLOT_BORDER_LIGHT);
		if (slotIndex == PuppeteersSpindleMenu.CROSSBAR_SLOT) {
			graphics.fill(sx, sy, sx + 16, sy + 16, 0x22FF4444);
		}
		if (slotIndex == PuppeteersSpindleMenu.THREAD_SLOT) {
			graphics.fill(sx, sy, sx + 16, sy + 16, 0x22D8A0A0);
		}
	}

	private void drawPanel(GuiGraphics graphics, int x, int y, int w, int h) {
		graphics.fill(x, y, x + w, y + h, PANEL_BG);
		graphics.fill(x, y, x + w, y + 1, PANEL_EDGE);
		graphics.fill(x, y + h - 1, x + w, y + h, 0xFF170407);
		graphics.fill(x, y, x + 1, y + h, 0xFF0D0303);
		graphics.fill(x + w - 1, y, x + w, y + h, PANEL_EDGE);
	}

	private String trimToWidth(String text, int maxWidth) {
		if (font.width(text) <= maxWidth) {
			return text;
		}
		String ellipsis = "...";
		int width = Math.max(0, maxWidth - font.width(ellipsis));
		return font.plainSubstrByWidth(text, width) + ellipsis;
	}

	private void drawTrimmedString(GuiGraphics graphics, Component text, int x, int y, int maxWidth, int color) {
		drawTrimmedString(graphics, text.getString(), x, y, maxWidth, color);
	}

	private void drawTrimmedString(GuiGraphics graphics, String text, int x, int y, int maxWidth, int color) {
		graphics.drawString(font, trimToWidth(text, maxWidth), x, y, color, false);
	}

	private void drawBorder(GuiGraphics graphics, int x, int y, int w, int h) {
		graphics.fill(x, y, x + w, y + 1, BORDER_OUTER);
		graphics.fill(x, y + h - 1, x + w, y + h, BORDER_OUTER);
		graphics.fill(x, y, x + 1, y + h, BORDER_OUTER);
		graphics.fill(x + w - 1, y, x + w, y + h, BORDER_OUTER);
		graphics.fill(x + 1, y + 1, x + w - 1, y + 2, BORDER_INNER);
		graphics.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, BORDER_INNER);
		graphics.fill(x + 1, y + 1, x + 2, y + h - 1, BORDER_INNER);
		graphics.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, BORDER_INNER);
	}

	private void renderVeinBackground(GuiGraphics graphics, int gx, int gy, int gw, int gh) {
		graphics.enableScissor(gx, gy, gx + gw, gy + gh);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		graphics.fill(gx, gy, gx + gw, gy + gh, 0xFF090204);
		graphics.fill(gx + 3, gy + 3, gx + gw - 3, gy + gh - 3, 0xDD120508);

		if (veinParams != null) {
			for (int i = 0; i < VEIN_COUNT; i++) {
				drawVeinTendril(graphics, i, gx, gy, gw, gh);
			}
		}

		Random speckles = new Random(24680L);
		for (int i = 0; i < 40; i++) {
			int sx = gx + speckles.nextInt(gw);
			int sy = gy + speckles.nextInt(gh);
			int alpha = 10 + speckles.nextInt(16);
			graphics.fill(sx, sy, sx + 1, sy + 1, (alpha << 24) | 0x002A0508);
		}
		RenderSystem.disableBlend();
		graphics.disableScissor();
	}

	private void drawVeinTendril(GuiGraphics graphics, int index, int gx, int gy, int gw, int gh) {
		float[] p = veinParams[index];
		float startX = gx + p[0] * gw;
		float startY = gy + p[1] * gh;
		float angle = p[2] + 0.15f * Mth.sin(animTime * p[3] + index);
		float cos = Mth.cos(angle);
		float sin = Mth.sin(angle);
		int length = (int) p[6];
		int thickness = (int) p[7];
		for (int step = 0; step < length; step++) {
			float wave = p[4] * Mth.sin(p[5] * step + animTime * 2.0f);
			int x = (int) (startX + step * cos * 1.35f - wave * sin);
			int y = (int) (startY + step * sin * 1.35f + wave * cos);
			if (x < gx || x >= gx + gw || y < gy || y >= gy + gh) {
				continue;
			}
			float fade = step < 8 ? step / 8.0f : (step > length - 8 ? (length - step) / 8.0f : 1.0f);
			int alpha = (int) Mth.clamp(fade * (42 + 48 * p[8]), 8, 90);
			int red = (int) (45 + 46 * p[8]);
			graphics.fill(x, y, x + thickness, y + thickness, (alpha << 24) | (red << 16) | 0x000708);
		}
	}

}
