package com.vincenthuto.hemomancy.client.screen.item;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.common.menu.PuppeteersSpindleMenu;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.summon.PacketPuppeteersSpindleAction;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinition;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonRules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PuppeteersSpindleScreen extends AbstractContainerScreen<PuppeteersSpindleMenu> {
	private static final int GUI_WIDTH = 246;
	private static final int GUI_HEIGHT = 186;
	private static final int PANEL_BG = 0xF20A0204;
	private static final int PANEL_INNER = 0xF018090D;
	private static final int PANEL_LINE = 0xFF6C151A;
	private static final int PANEL_DARK = 0xFF12070A;
	private static final int TEXT_MUTED = 0xFFB98F8C;
	private static final int TEXT_RED = 0xFFFFA095;
	private final List<String> knownSummons = new ArrayList<>();
	private int selectedIndex;

	public PuppeteersSpindleScreen(PuppeteersSpindleMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
		this.imageWidth = GUI_WIDTH;
		this.imageHeight = GUI_HEIGHT;
	}

	@Override
	protected void init() {
		super.init();
		reloadKnownSummons();
		addActionButtons();
		addSummonButtons();
	}

	private void reloadKnownSummons() {
		knownSummons.clear();
		Player player = Minecraft.getInstance().player;
		if (player != null) {
			HemoCapabilityAccess.getKnownSummons(player)
					.ifPresent(known -> knownSummons.addAll(known.getKnownSummonNames()));
		}
		String selected = selectedSummon();
		selectedIndex = Math.max(0, knownSummons.indexOf(selected));
	}

	private void addActionButtons() {
		int x = leftPos + 154;
		int y = topPos + 34;
		addRenderableWidget(Button.builder(Component.translatable("screen.hemomancy.puppeteers_spindle.bind"),
				btn -> send(PacketPuppeteersSpindleAction.Action.BIND, selectedSummon()))
				.bounds(x, y, 74, 18).build());
		addRenderableWidget(Button.builder(Component.translatable("screen.hemomancy.puppeteers_spindle.wind"),
				btn -> send(PacketPuppeteersSpindleAction.Action.WIND, selectedSummon()))
				.bounds(x, y + 22, 74, 18).build());
		addRenderableWidget(Button.builder(Component.translatable("screen.hemomancy.puppeteers_spindle.unlock"),
				btn -> send(PacketPuppeteersSpindleAction.Action.UNLOCK, selectedSummon()))
				.bounds(x, y + 44, 74, 18).build());
		addRenderableWidget(Button.builder(Component.translatable("screen.hemomancy.puppeteers_spindle.call"),
				btn -> send(PacketPuppeteersSpindleAction.Action.CALL_OR_RECALL, selectedSummon()))
				.bounds(x, y + 72, 74, 20).build());
	}

	private void addSummonButtons() {
		int x = leftPos + 14;
		int y = topPos + 38;
		int visible = Math.min(knownSummons.size(), 5);
		for (int i = 0; i < visible; i++) {
			int index = i;
			String summon = knownSummons.get(index);
			Component label = Component.translatable("entity.hemomancy." + summon);
			addRenderableWidget(Button.builder(label, btn -> {
				selectedIndex = index;
				send(PacketPuppeteersSpindleAction.Action.SELECT, summon);
			}).bounds(x, y + i * 22, 126, 18).build());
		}
	}

	private void send(PacketPuppeteersSpindleAction.Action action, String summon) {
		PacketHandler.sendToServer(new PacketPuppeteersSpindleAction(action, summon == null ? "" : summon));
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderBackground(graphics, mouseX, mouseY, partialTick);
		super.render(graphics, mouseX, mouseY, partialTick);
		renderTooltip(graphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, PANEL_BG);
		graphics.fill(leftPos + 3, topPos + 3, leftPos + imageWidth - 3, topPos + imageHeight - 3, PANEL_INNER);
		graphics.fill(leftPos + 8, topPos + 28, leftPos + 146, topPos + 158, PANEL_DARK);
		graphics.fill(leftPos + 150, topPos + 28, leftPos + 232, topPos + 158, PANEL_DARK);
		graphics.fill(leftPos + 8, topPos + 28, leftPos + 146, topPos + 29, PANEL_LINE);
		graphics.fill(leftPos + 150, topPos + 28, leftPos + 232, topPos + 29, PANEL_LINE);
		renderThreadMeter(graphics);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.drawString(font, title, 10, 9, 0xFFFFD7D0, false);
		graphics.drawString(font, Component.translatable("screen.hemomancy.puppeteers_spindle.patterns"),
				14, 20, TEXT_MUTED, false);
		graphics.drawString(font, Component.translatable("screen.hemomancy.puppeteers_spindle.work"),
				154, 20, TEXT_MUTED, false);

		String selected = selectedSummon();
		Component selectedLabel = selected == null || selected.isBlank()
				? Component.translatable("tooltip.hemomancy.marionette_crossbar.unbound")
				: Component.translatable("entity.hemomancy." + selected).withStyle(ChatFormatting.RED);
		graphics.drawString(font, Component.translatable("screen.hemomancy.puppeteers_spindle.selected", selectedLabel),
				14, 148, TEXT_RED, false);

		graphics.drawString(font, Component.translatable("screen.hemomancy.puppeteers_spindle.thread",
						thread(), PuppeteerSummonRules.THREAD_CAPACITY),
				154, 124, TEXT_MUTED, false);
		costLine(selected).ifPresent(line -> graphics.drawString(font, line, 154, 138, TEXT_MUTED, false));
		if (knownSummons.isEmpty()) {
			graphics.drawString(font, Component.translatable("hemomancy.summon.bind.none"),
					16, 48, TEXT_MUTED, false);
		}
	}

	private void renderThreadMeter(GuiGraphics graphics) {
		int x = leftPos + 154;
		int y = topPos + 112;
		graphics.fill(x, y, x + 74, y + 8, 0xFF060203);
		int width = (int) (72.0F * Math.min(1.0F, thread() / (float) PuppeteerSummonRules.THREAD_CAPACITY));
		graphics.fill(x + 1, y + 1, x + 1 + width, y + 7, 0xFFB51B25);
	}

	private Optional<Component> costLine(String selected) {
		return PuppeteerSummonDefinitions.byName(selected)
				.map(definition -> Component.translatable("screen.hemomancy.puppeteers_spindle.cost",
						definition.threadSummonCost(), definition.threadUpkeepPerMinute()));
	}

	private String selectedSummon() {
		Optional<ItemStack> crossbar = currentCrossbar();
		String stackSelection = crossbar.map(MarionetteCrossbarItem::getSelectedSummonName).orElse("");
		if (stackSelection != null && !stackSelection.isBlank()) {
			return stackSelection;
		}
		if (!knownSummons.isEmpty()) {
			return knownSummons.get(Math.min(selectedIndex, knownSummons.size() - 1));
		}
		return "";
	}

	private int thread() {
		return currentCrossbar().map(MarionetteCrossbarItem::getThread).orElse(0);
	}

	private Optional<ItemStack> currentCrossbar() {
		Player player = Minecraft.getInstance().player;
		return MarionetteCrossbarItem.findFirstCrossbar(player);
	}
}
