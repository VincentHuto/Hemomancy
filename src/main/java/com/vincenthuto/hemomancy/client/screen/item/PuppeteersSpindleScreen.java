package com.vincenthuto.hemomancy.client.screen.item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
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
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
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
	private static final int GUI_WIDTH = 276;
	private static final int GUI_HEIGHT = 232;
	private static final int CRAFT_AREA_HEIGHT = 136;
	private static final int VEIN_COUNT = 10;
	private static final int INPUT_X = 10;
	private static final int PATTERN_X = 110;
	private static final int WORK_X = 224;
	private static final int PANEL_Y = 28;
	private static final int INPUT_W = 92;
	private static final int PATTERN_W = 104;
	private static final int WORK_W = 42;
	private static final int PANEL_H = 102;
	private static final int INVENTORY_PANEL_W = 172;

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
		this.inventoryLabelX = 57;
		this.inventoryLabelY = CRAFT_AREA_HEIGHT + 7;
	}

	@Override
	protected void init() {
		super.init();
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
		reloadKnownSummons();
		seedVeins();
		addActionButtons();
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

	private void addActionButtons() {
		int x = leftPos + WORK_X + 5;
		int y = topPos + 52;
		addRenderableWidget(new SpindleButton(x, y, WORK_W - 10, 16,
				Component.translatable("screen.hemomancy.puppeteers_spindle.bind"),
				btn -> send(PacketPuppeteersSpindleAction.Action.BIND, selectedSummonName())));
		addRenderableWidget(new SpindleButton(x, y + 22, WORK_W - 10, 16,
				Component.translatable("screen.hemomancy.puppeteers_spindle.call"),
				btn -> send(PacketPuppeteersSpindleAction.Action.CALL_OR_RECALL, selectedSummonName())));
	}

	private void send(PacketPuppeteersSpindleAction.Action action, String summon) {
		PacketHandler.sendToServer(new PacketPuppeteersSpindleAction(action, summon == null ? "" : summon));
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
				selectedIndex = clicked;
				PuppeteerSummonDefinition definition = PuppeteerSummonDefinitions.all().get(clicked);
				if (knownSummons.contains(definition.name())) {
					send(PacketPuppeteersSpindleAction.Action.SELECT, definition.name());
				}
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
		drawPanel(graphics, gx + WORK_X, gy + PANEL_Y, WORK_W, PANEL_H);
		drawBorder(graphics, gx, gy, imageWidth, CRAFT_AREA_HEIGHT);
		int invPanelX = gx + inventoryLabelX - 5;
		int invPanelY = gy + CRAFT_AREA_HEIGHT + 2;
		renderInventoryBackground(graphics, invPanelX, invPanelY,
				INVENTORY_PANEL_W, imageHeight - (CRAFT_AREA_HEIGHT + 2) - 2);

		for (Slot slot : menu.slots) {
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
		drawTrimmedString(graphics, Component.translatable("screen.hemomancy.puppeteers_spindle.thread_slot"),
				INPUT_X + 35, 74, INPUT_W - 40, TEXT_MUTED);
		drawTrimmedString(graphics, Component.translatable("screen.hemomancy.puppeteers_spindle.patterns"),
				PATTERN_X + 7, PANEL_Y + 8, PATTERN_W - 14, TEXT_MUTED);
		drawTrimmedString(graphics, Component.translatable("screen.hemomancy.puppeteers_spindle.work"),
				WORK_X + 7, PANEL_Y + 8, WORK_W - 14, TEXT_MUTED);
		graphics.drawString(font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY,
				0xFF452126, false);
	}

	private void renderMeters(GuiGraphics graphics, int gx, int gy) {
		int x = gx + INPUT_X + 8;
		int w = INPUT_W - 16;
		drawTrimmedString(graphics, "Bar " + menu.getCrossbarThread() + "/" + PuppeteerSummonRules.THREAD_CAPACITY,
				x, gy + 96, w, TEXT_MUTED);
		drawMeter(graphics, x, gy + 107, w, 6,
				menu.getScaledCrossbarThreadWidth(w - 2), 0xFFB51B25);
		drawTrimmedString(graphics, "Thread " + menu.getThreadBuffer() + "/"
						+ PuppeteersSpindleBlockEntity.THREAD_BUFFER_CAPACITY,
				x, gy + 114, w, TEXT_MUTED);
		drawMeter(graphics, x, gy + 125, w, 5,
				menu.getScaledThreadBufferWidth(w - 2), 0xFFC0444C);
	}

	private void drawMeter(GuiGraphics graphics, int x, int y, int w, int h, int fill, int color) {
		graphics.fill(x, y, x + w, y + h, 0xFF060203);
		graphics.fill(x + 1, y + 1, x + 1 + Mth.clamp(fill, 0, w - 2), y + h - 1, color);
		graphics.fill(x, y, x + w, y + 1, 0xFF2A0A0D);
		graphics.fill(x, y + h - 1, x + w, y + h, 0xFF4A151B);
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
			boolean degreeOk = playerDegree() >= definition.requiredDegree();
			boolean recipeUnlocked = hasTrialRecipe(definition);
			int color = known ? TEXT_RED : (degreeOk ? 0xFF9C6D76 : TEXT_LOCKED);
			String status = statusLabel(definition, known, degreeOk, recipeUnlocked);
			int statusWidth = font.width(status);
			String label = trimToWidth(name.getString(), w - statusWidth - 14);
			graphics.drawString(font, label, x + 5, rowY + 4, color, false);
			graphics.drawString(font, status, x + w - font.width(status) - 5, rowY + 4,
					known ? 0xFFB4A0A2 : 0xFF766166, false);
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
				.contains(Hemomancy.rloc("puppeteer_trial/" + definition.name()));
	}

	private String statusLabel(PuppeteerSummonDefinition definition, boolean known,
			boolean degreeOk, boolean recipeUnlocked) {
		if (known) {
			return Component.translatable("screen.hemomancy.puppeteers_spindle.status_ready").getString();
		}
		if (!degreeOk) {
			return Component.translatable("screen.hemomancy.puppeteers_spindle.status_degree",
					definition.requiredDegree()).getString();
		}
		if (!recipeUnlocked) {
			return Component.translatable("screen.hemomancy.puppeteers_spindle.status_recipe").getString();
		}
		return Component.translatable("screen.hemomancy.puppeteers_spindle.status_trial").getString();
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
			List<Component> tooltip = new ArrayList<>();
			tooltip.add(Component.translatable(definition.translationKey()).withStyle(ChatFormatting.RED));
			tooltip.add(Component.translatable("screen.hemomancy.puppeteers_spindle.cost",
					definition.threadSummonCost(), definition.threadUpkeepPerMinute()).withStyle(ChatFormatting.GRAY));
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

	private void renderInventoryBackground(GuiGraphics graphics, int x, int y, int w, int h) {
		for (int row = 0; row < h; row++) {
			float t = (float) row / Math.max(h, 1);
			int red = (int) (26 + 44 * t);
			int green = (int) (4 + 12 * t);
			int blue = (int) (6 + 14 * t);
			graphics.fill(x, y + row, x + w, y + row + 1,
					(0xEE << 24) | (red << 16) | (green << 8) | blue);
		}
		drawInventoryBorder(graphics, x, y, w, h);
	}

	private void drawInventoryBorder(GuiGraphics graphics, int x, int y, int w, int h) {
		graphics.fill(x, y, x + w, y + 1, BORDER_OUTER);
		graphics.fill(x, y + h - 1, x + w, y + h, BORDER_OUTER);
		graphics.fill(x, y, x + 1, y + h, BORDER_OUTER);
		graphics.fill(x + w - 1, y, x + w, y + h, BORDER_OUTER);
		graphics.fill(x + 1, y + 1, x + w - 1, y + 2, BORDER_INNER);
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

	private static class SpindleButton extends Button {
		protected SpindleButton(int x, int y, int width, int height, Component message, OnPress onPress) {
			super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
		}

		@Override
		protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			Minecraft minecraft = Minecraft.getInstance();
			int bg = isHoveredOrFocused() ? 0xFFE0444E : 0xFF54121A;
			int edge = isHoveredOrFocused() ? 0xFFFFA0A4 : 0xFF8C2530;
			graphics.fill(getX(), getY(), getX() + width, getY() + height, 0xFF110407);
			graphics.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + height - 1, bg);
			graphics.fill(getX(), getY(), getX() + width, getY() + 1, edge);
			graphics.fill(getX(), getY() + height - 1, getX() + width, getY() + height, 0xFF21060A);
			String label = minecraft.font.plainSubstrByWidth(getMessage().getString(), Math.max(0, width - 6));
			graphics.drawCenteredString(minecraft.font, label,
					getX() + width / 2, getY() + (height - 8) / 2, 0xFFFFE2DC);
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
			defaultButtonNarrationText(narrationElementOutput);
		}
	}
}
