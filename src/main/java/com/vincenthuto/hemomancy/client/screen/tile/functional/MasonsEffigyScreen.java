package com.vincenthuto.hemomancy.client.screen.tile.functional;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.util.InventoryPanelTextures;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.menu.tile.functional.MasonsEffigyMenu;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.scars.PacketPrepareScarPattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.VeinBackgroundRenderer;

public class MasonsEffigyScreen extends AbstractContainerScreen<MasonsEffigyMenu> {
	private static final int TOP_W = 226;
	private static final int TOP_H = 142;
	private static final int BG = 0xFF100403;
	private static final int PANEL = 0xE61B0805;
	private static final int PANEL_DARK = 0xF0060202;
	private static final int LINE = 0xFF9A260B;
	private static final int LINE_BRIGHT = 0xFFD98B24;
	private static final int LINE_DIM = 0xFF3B170C;
	private static final int SELECTED = 0x804A0906;
	private static final int ACTIVE = 0x552D6C23;
	private static final int TEXT = 0xFFE0A448;
	private static final int MUTED_TEXT = 0xFFA66D3B;
	private static final int ROW_HEIGHT = 20;
	private static final int PORTRAIT_X = 16;
	private static final int PORTRAIT_Y = 31;
	private static final int PORTRAIT_W = 74;
	private static final int PORTRAIT_H = 88;
	private static final int LIST_X = 99;
	private static final int LIST_Y = 42;
	private static final int LIST_W = 112;
	private static final int LIST_H = 74;
	private static final int SOCKET_X = 101;
	private static final int SOCKET_Y = 120;
	private static final int SLOT_SIZE = 18;

	private final List<ResourceLocation> selected = new ArrayList<>();
	private int scroll;
	private float oldMouseX;
	private float oldMouseY;
	private float animTime;
	private final VeinBackgroundRenderer veins = new VeinBackgroundRenderer(new VeinBackgroundRenderer.Style(
			VeinBackgroundRenderer.Field.MACHINE.count(12), VeinBackgroundRenderer.Palette.MACHINE.background(BG),
			VeinBackgroundRenderer.Speckles.Scattered.MACHINE));
	private Button prepareButton;

	public MasonsEffigyScreen(MasonsEffigyMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		imageWidth = TOP_W;
		imageHeight = 238;
		inventoryLabelY = MasonsEffigyMenu.PLAYER_INV_Y - 10;
	}

	@Override
	protected void init() {
		super.init();
		titleLabelX = 14;
		selected.clear();
		selected.addAll(menu.getOpeningActiveScarIds());
		prepareButton = addRenderableWidget(new PrepareIconButton(leftPos + 204, topPos + 120, button -> preparePattern()));
		syncSelection(false);
	}

	@Override
	public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
		renderBackground(gfx, mouseX, mouseY, partialTick);
		super.render(gfx, mouseX, mouseY, partialTick);
		renderTooltip(gfx, mouseX, mouseY);
		renderScarTooltip(gfx, mouseX, mouseY);
		oldMouseX = mouseX;
		oldMouseY = mouseY;
	}

	private void renderScarTooltip(GuiGraphics gfx, int mouseX, int mouseY) {
		if (mouseX < leftPos + LIST_X || mouseX >= leftPos + LIST_X + LIST_W
				|| mouseY < topPos + LIST_Y || mouseY >= topPos + LIST_Y + (LIST_H / ROW_HEIGHT) * ROW_HEIGHT) return;
		int index = scroll + (mouseY - topPos - LIST_Y) / ROW_HEIGHT;
		List<ResourceLocation> known = menu.getKnownScarIds();
		if (index >= known.size()) return;
		ResourceLocation id = known.get(index);
		ItemStack stack = scarStackFor(id);
		if (!(stack.getItem() instanceof com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScar scar)) return;
		List<Component> tooltip = new ArrayList<>();
		tooltip.add(stack.getHoverName());
		tooltip.add(Component.literal(menu.getActiveScarIds().contains(id) ? "Learned and active" : "Learned; not currently active"));
		tooltip.add(Component.literal(selected.contains(id) ? "Selected for the next motif" : "Not selected for the next motif"));
		scar.getScarDefinition().appendHoverText(tooltip);
		tooltip.add(Component.literal("Place motif paper, then project 500 ml per selected scar."));
		tooltip.add(Component.literal("Fit the prepared motif at a brazier: 50 ml."));
		gfx.renderComponentTooltip(font, tooltip, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
		animTime += partialTick * 0.008f;
		renderTopPanel(gfx, mouseX, mouseY);
		renderInventoryPanel(gfx);
	}

	@Override
	protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
		gfx.drawString(font, title, titleLabelX, 8, TEXT, false);
		gfx.drawString(font, Component.translatable("label.hemomancy.mason_effigy.known_scars"),
				LIST_X + 10, 27, TEXT, false);
		gfx.drawString(font, Component.translatable("label.hemomancy.mason_effigy.selected",
				selected.size(), menu.getMaxSelectableScars()), 16, 126, MUTED_TEXT, false);
	}

	private void renderTopPanel(GuiGraphics gfx, int mouseX, int mouseY) {
		int x = leftPos;
		int y = topPos;
		veins.draw(gfx, x, y, TOP_W, TOP_H, animTime);
		drawEffigyFrame(gfx, x, y, TOP_W, TOP_H);
		gfx.fill(x + 8, y + 23, x + TOP_W - 8, y + TOP_H - 9, PANEL_DARK);
		gfx.fill(x + 10, y + 25, x + TOP_W - 10, y + TOP_H - 11, PANEL);
		renderPlayerHeadDisplay(gfx, mouseX, mouseY);
		renderScarList(gfx, mouseX, mouseY);
		renderActiveScarSockets(gfx);
	}

	private void renderInventoryPanel(GuiGraphics gfx) {
		Slot firstInventorySlot = menu.slots.get(MasonsEffigyMenu.PLAYER_INV_START);
		InventoryPanelTextures.blit(gfx, InventoryPanelTextures.FUNGAL,
				leftPos + firstInventorySlot.x - 5, topPos + firstInventorySlot.y - 6);
	}

	private void renderPlayerHeadDisplay(GuiGraphics gfx, int mouseX, int mouseY) {
		int x = leftPos + PORTRAIT_X;
		int y = topPos + PORTRAIT_Y;
		drawEffigyFrame(gfx, x, y, PORTRAIT_W, PORTRAIT_H);
		gfx.fill(x + 2, y + 2, x + PORTRAIT_W - 2, y + PORTRAIT_H - 2, 0xAA110604);
		if (minecraft != null && minecraft.player != null) {
			gfx.enableScissor(x + 4, y + 4, x + PORTRAIT_W - 4, y + PORTRAIT_H - 4);
			InventoryScreen.renderEntityInInventoryFollowsMouse(gfx, x + 7, y + 3,
					x + PORTRAIT_W - 7, y + PORTRAIT_H + 28, 46, 0.0625F,
					oldMouseX, oldMouseY, minecraft.player);
			gfx.disableScissor();
		}
	}

	public void renderScarList(GuiGraphics gfx, int mouseX, int mouseY) {
		List<ResourceLocation> known = menu.getKnownScarIds();
		List<ResourceLocation> active = menu.getActiveScarIds();
		int x = leftPos + LIST_X;
		int y = topPos + LIST_Y;
		gfx.enableScissor(x, y, x + LIST_W, y + LIST_H);
		if (known.isEmpty()) {
			gfx.drawString(font, Component.translatable("label.hemomancy.mason_effigy.no_scars"),
					x + 8, y + 30, MUTED_TEXT, false);
		} else {
			int visibleRows = LIST_H / ROW_HEIGHT;
			int maxScroll = Math.max(0, known.size() - visibleRows);
			scroll = Mth.clamp(scroll, 0, maxScroll);
			for (int row = 0; row < visibleRows; row++) {
				int index = row + scroll;
				if (index >= known.size()) {
					break;
				}
				ResourceLocation id = known.get(index);
				renderScarRow(gfx, id, active.contains(id), selected.contains(id), x, y + row * ROW_HEIGHT);
			}
		}
		gfx.disableScissor();
	}

	private void renderScarRow(GuiGraphics gfx, ResourceLocation id, boolean active, boolean isSelected, int x, int rowY) {
		ItemStack scarStack = scarStackFor(id);
		gfx.fill(x, rowY, x + LIST_W, rowY + ROW_HEIGHT - 2, isSelected ? 0xFF310B07 : 0xE6080202);
		gfx.fill(x, rowY, x + LIST_W, rowY + 1, isSelected ? LINE_BRIGHT : LINE_DIM);
		gfx.fill(x, rowY + ROW_HEIGHT - 3, x + LIST_W, rowY + ROW_HEIGHT - 2, LINE_DIM);
		if (active) {
			gfx.fill(x + 1, rowY + 1, x + LIST_W - 1, rowY + ROW_HEIGHT - 3, ACTIVE);
		}
		if (isSelected) {
			gfx.fill(x + 1, rowY + 1, x + LIST_W - 1, rowY + ROW_HEIGHT - 3, SELECTED);
		}
		drawItemSlot(gfx, x, rowY, isSelected);
		if (!scarStack.isEmpty()) {
			gfx.renderItem(scarStack, x + 1, rowY + 1);
		}
		gfx.drawString(font, Component.literal(displayName(id)), x + 26, rowY + 6, TEXT, false);
	}

	private void renderActiveScarSockets(GuiGraphics gfx) {
		int x = leftPos + SOCKET_X;
		int y = topPos + SOCKET_Y;
		int socketSize = 14;
		for (int i = 0; i < MasonsEffigyMenu.MAX_SELECTED_SCARS; i++) {
			int sx = x + i * 14;
			boolean unlocked = i < menu.getMaxSelectableScars();
			boolean occupied = i < selected.size();
			gfx.fill(sx, y, sx + socketSize, y + socketSize, occupied ? LINE_BRIGHT : LINE_DIM);
			gfx.fill(sx + 1, y + 1, sx + socketSize - 1, y + socketSize - 1, PANEL_DARK);
			if (unlocked && occupied) {
				ItemStack scarStack = scarStackFor(selected.get(i));
				if (!scarStack.isEmpty()) {
					gfx.pose().pushPose();
					gfx.pose().translate(sx + 1, y + 1, 0);
					gfx.pose().scale(0.75F, 0.75F, 1);
					gfx.renderItem(scarStack, 0, 0);
					gfx.pose().popPose();
				}
			} else if (!unlocked) {
				gfx.fill(sx + 2, y + 2, sx + socketSize - 2, y + socketSize - 2, 0xAA030101);
			}
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && mouseX >= leftPos + LIST_X && mouseX < leftPos + LIST_X + LIST_W
				&& mouseY >= topPos + LIST_Y && mouseY < topPos + LIST_Y + LIST_H) {
			int row = ((int) mouseY - (topPos + LIST_Y)) / ROW_HEIGHT;
			int index = scroll + row;
			List<ResourceLocation> known = menu.getKnownScarIds();
			if (index >= 0 && index < known.size()) {
				toggle(known.get(index));
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (mouseX >= leftPos + LIST_X && mouseX < leftPos + LIST_X + LIST_W
				&& mouseY >= topPos + LIST_Y && mouseY < topPos + LIST_Y + LIST_H) {
			scroll = Mth.clamp(scroll - (int) Math.signum(scrollY), 0,
					Math.max(0, menu.getKnownScarIds().size() - LIST_H / ROW_HEIGHT));
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	private void toggle(ResourceLocation id) {
		if (selected.remove(id)) {
			syncSelection(false);
			return;
		}
		if (selected.size() < menu.getMaxSelectableScars()) {
			selected.add(id);
			syncSelection(false);
		} else if (minecraft != null && minecraft.player != null) {
			minecraft.player.displayClientMessage(Component.translatable("message.hemomancy.mason_effigy.too_many")
					.withStyle(ChatFormatting.RED), true);
		}
	}

	private void preparePattern() {
		if (selected.isEmpty()) {
			return;
		}
		syncSelection(true);
		if (minecraft != null && minecraft.player != null) {
			minecraft.player.displayClientMessage(Component.literal("Press motif paper to the Effigy to make a pattern.")
					.withStyle(ChatFormatting.DARK_RED), true);
		}
	}

	private void syncSelection(boolean announce) {
		if (minecraft == null || minecraft.player == null) {
			return;
		}
		PacketHandler.sendToServer(new PacketPrepareScarPattern(menu.getPos(), selected, announce));
	}

	private static ItemStack scarStackFor(ResourceLocation id) {
		Item item = BuiltInRegistries.ITEM.get(Hemomancy.rloc(id.getPath()));
		if (item == Items.AIR) {
			return ItemStack.EMPTY;
		}
		return new ItemStack(item);
	}

	private void drawItemSlot(GuiGraphics gfx, int x, int y, boolean highlighted) {
		int border = highlighted ? LINE_BRIGHT : 0xFF2B1407;
		gfx.fill(x - 1, y - 1, x + SLOT_SIZE + 1, y + SLOT_SIZE + 1, 0xFF070202);
		gfx.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF2A1307);
		gfx.fill(x, y, x + SLOT_SIZE, y + 1, 0xFF120703);
		gfx.fill(x, y, x + 1, y + SLOT_SIZE, 0xFF120703);
		gfx.fill(x + SLOT_SIZE - 1, y, x + SLOT_SIZE, y + SLOT_SIZE, border);
		gfx.fill(x, y + SLOT_SIZE - 1, x + SLOT_SIZE, y + SLOT_SIZE, border);
		gfx.fill(x + 2, y + 2, x + SLOT_SIZE - 2, y + SLOT_SIZE - 2, 0xFF3A1A0B);
	}

	private void drawEffigyFrame(GuiGraphics gfx, int x, int y, int w, int h) {
		gfx.fill(x, y, x + w, y + 2, LINE);
		gfx.fill(x, y + h - 2, x + w, y + h, LINE);
		gfx.fill(x, y, x + 2, y + h, LINE);
		gfx.fill(x + w - 2, y, x + w, y + h, LINE_BRIGHT);
	}

	private void drawVein(GuiGraphics gfx, int x1, int y1, int x2, int y2, int color) {
		int steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
		if (steps == 0) {
			gfx.fill(x1, y1, x1 + 1, y1 + 1, color);
			return;
		}
		for (int step = 0; step <= steps; step++) {
			float t = (float) step / steps;
			int x = Mth.floor(Mth.lerp(t, x1, x2));
			int y = Mth.floor(Mth.lerp(t, y1, y2));
			gfx.fill(x, y, x + 1, y + 1, color);
		}
	}

	private static String displayName(ResourceLocation id) {
		String text = id.getPath().replace("scar_", "").replace('_', ' ');
		String[] parts = text.split(" ");
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

	private static class PrepareIconButton extends Button {
		private PrepareIconButton(int x, int y, OnPress onPress) {
			super(x, y, 18, 18, Component.translatable("button.hemomancy.mason_effigy.prepare"),
					onPress, DEFAULT_NARRATION);
		}

		@Override
		protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
			boolean hot = isHoveredOrFocused();
			int x = getX();
			int y = getY();
			int edge = hot ? LINE_BRIGHT : LINE;
			gfx.fill(x, y, x + width, y + height, 0xFF050101);
			gfx.fill(x + 1, y + 1, x + width - 1, y + height - 1, hot ? 0xFF361107 : 0xFF1D0805);
			gfx.fill(x, y, x + width, y + 1, edge);
			gfx.fill(x, y + height - 1, x + width, y + height, edge);
			gfx.fill(x, y, x + 1, y + height, edge);
			gfx.fill(x + width - 1, y, x + width, y + height, LINE_BRIGHT);
			ItemStack icon = new ItemStack(ItemInit.scar_pattern.get());
			gfx.renderItem(icon, x + 1, y + 1);
			if (hot) {
				gfx.renderTooltip(Minecraft.getInstance().font, getMessage(), mouseX, mouseY);
			}
		}
	}
}
