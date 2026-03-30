package com.vincenthuto.hemomancy.client.screen.rune;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.rune.ItemRuneBinder;
import com.vincenthuto.hemomancy.common.item.rune.pattern.ItemRunePattern;
import com.vincenthuto.hemomancy.common.itemhandler.RuneBinderItemHandler;
import com.vincenthuto.hemomancy.common.menu.ChiselStationMenu;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.runes.PacketChiselCraftingEvent;
import com.vincenthuto.hemomancy.common.network.capa.runes.PacketLoadChiselPattern;
import com.vincenthuto.hemomancy.common.network.capa.runes.PacketUpdateChiselRunes;
import com.vincenthuto.hemomancy.common.recipe.ChiselRecipe;
import com.vincenthuto.hemomancy.common.tile.ChiselStationBlockEntity;
import com.vincenthuto.hutoslib.client.screen.HLButtonTextured;
import com.vincenthuto.hutoslib.common.item.ItemKnapper;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

public class ChiselStationScreen extends AbstractContainerScreen<ChiselStationMenu> {
	private static final ResourceLocation GUI_Chisel = new ResourceLocation(
			Hemomancy.MOD_ID + ":textures/gui/chisel_station.png");

	private final Inventory playerInv;
	private final ChiselStationBlockEntity te;
	int left, top;
	int guiWidth = 176;
	int guiHeight = 186;
	public ChiselButton[][] runeButtonArray = new ChiselButton[8][8];
	int CLEARBUTTONID = 70;
	HLButtonTextured clearButton;
	int CHISELBUTTONID = 71;
	HLButtonTextured chiselButton;
	int LOADPATTERNBUTTONID = 72;
	HLButtonTextured loadPatternButton;
	public byte[][] pattern = ChiselRecipe.blank();
	public byte[][] preview = ChiselRecipe.blank();
	private ItemStack lastPatternSlotItem = ItemStack.EMPTY;

	// --- Click-and-drag rune painting state ---
	/** Whether the player is currently dragging across the rune grid. */
	private boolean isDragging = false;
	/** The paint mode for the current drag: true = activate runes, false = deactivate. */
	private boolean dragPaintOn = true;
	/** Tracks which buttons have already been toggled during this drag to avoid re-processing. */
	private final boolean[][] dragVisited = new boolean[8][8];

	// ── Binder pattern selector panel state ─────────────────────────────
	/** Cached pattern entries extracted from a RuneBinder in the pattern slot. */
	private final List<BinderPatternEntry> binderEntries = new ArrayList<>();
	/** Whether the binder panel is currently visible. */
	private boolean binderPanelVisible = false;
	/** Scroll offset for the binder panel (index of the first visible entry). */
	private int binderScrollOffset = 0;
	/** Index of the hovered entry in the binder panel, or -1. */
	private int binderHoveredEntry = -1;
	/** Index of the currently selected/loaded entry, or -1. */
	private int binderSelectedEntry = -1;
	/** Whether the user is dragging the binder panel scrollbar thumb. */
	private boolean binderDraggingScrollbar = false;

	/** Binder panel layout constants. */
	private static final int PANEL_WIDTH = 100;
	private static final int PANEL_ENTRY_HEIGHT = 24;
	private static final int PANEL_ENTRY_SPACING = 1;
	private static final int PANEL_VISIBLE_ENTRIES = 7;
	private static final int PANEL_PADDING = 4;
	private static final int PANEL_SCROLLBAR_WIDTH = 5;
	private static final int MINI_CELL = 2;

	/** Simple record holding data for one binder pattern entry. */
	private record BinderPatternEntry(
			int slotIndex,
			ItemRunePattern patternItem,
			ItemStack resultIcon,
			String displayName,
			byte[][] pattern,
			ChiselRecipe recipe
	) {}

	public ChiselStationScreen(ChiselStationMenu screenContainer, Inventory inv, Component titleIn) {
		super(screenContainer, inv, titleIn);
		this.leftPos = 0;
		this.topPos = 0;
		this.imageWidth = 176;
		this.imageHeight = 186;
		this.playerInv = inv;
		this.te = screenContainer.getTe();
		// Sync local pattern from TE so reopening the screen preserves grid state
		if (te.runesList != null) {
			for (int i = 0; i < te.runesList.length && i < pattern.length; i++) {
				pattern[i] = te.runesList[i].clone();
			}
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.render(graphics, mouseX, mouseY, partialTicks);
		// Render glowing borders: red for confirmed, purple for preview
		ChiselButtonGlowRenderer.renderGlowGrid(graphics, runeButtonArray, pattern, preview);

		// Render binder pattern selector panel if a RuneBinder is in the pattern slot
		if (binderPanelVisible && !binderEntries.isEmpty()) {
			renderBinderPanel(graphics, mouseX, mouseY, partialTicks);
		}

		this.renderTooltip(graphics, mouseX, mouseY);

		List<Component> cat1 = new ArrayList<Component>();
		cat1.add(Component.literal("Clear Runes"));
		if (clearButton.isHovered()) {
			graphics.renderComponentTooltip(font, cat1, mouseX, mouseY);
		}
		List<Component> cat9 = new ArrayList<Component>();
		cat9.add(Component.literal("Chisel Rune"));
		if (chiselButton.isHovered()) {
			graphics.renderComponentTooltip(font, cat9, mouseX, mouseY);
		}
		List<Component> cat10 = new ArrayList<Component>();
		cat10.add(Component.literal("Load Pattern"));
		if (loadPatternButton.isHovered()) {
			graphics.renderComponentTooltip(font, cat10, mouseX, mouseY);
		}

	}

	@Override
	public void renderBackground(GuiGraphics graphics) {
		super.renderBackground(graphics);
		for (int i = 0; i < renderables.size(); i++) {
			renderables.get(i).render(graphics, 0, 00, 10);
		}
		// Auto-detect when a pattern item is placed/removed in slot 4
		ItemStack currentPatternSlot = te.getItem(4);
		if (!ItemStack.isSameItemSameTags(currentPatternSlot, lastPatternSlotItem)) {
			lastPatternSlotItem = currentPatternSlot.copy();
			if (currentPatternSlot.getItem() instanceof ItemRuneBinder) {
				// A binder was placed — rebuild the pattern selector panel
				rebuildBinderEntries(currentPatternSlot);
				binderPanelVisible = true;
				binderSelectedEntry = -1;
			} else if (currentPatternSlot.getItem() instanceof ItemRunePattern) {
				// A single pattern was placed — hide binder panel, load it
				binderPanelVisible = false;
				binderEntries.clear();
				ItemRunePattern runePattern = (ItemRunePattern) currentPatternSlot.getItem();
				ChiselRecipe patternRecipe = runePattern.getRecipe();
				if (patternRecipe != null && patternRecipe.getPattern() != null) {
					loadPatternIntoGrid(patternRecipe.getPattern());
				}
			} else {
				// Slot was emptied — hide binder panel, clear the grid
				binderPanelVisible = false;
				binderEntries.clear();
				binderSelectedEntry = -1;
				pattern = ChiselRecipe.blank();
				preview = ChiselRecipe.blank();
				refreshButtonsFromPattern();
				PacketHandler.CHANNELRUNES.sendToServer(new PacketUpdateChiselRunes(pattern));
			}
		}
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int x, int y) {
		graphics.drawString(font, "Chisel Station", 8, 4, 0);
		graphics.drawString(font, "Chisel Station", 8, 4, 65444444);

		PoseStack matrixStack = graphics.pose();
		matrixStack.pushPose();
		graphics.drawString(font, this.playerInv.getDisplayName(), 8, this.imageHeight - 92,
				000000);

		if (te.hasValidRecipe()) {
			ChiselRecipe currentRecipe = te.getCurrentRecipe();
			graphics.drawString(font, currentRecipe.getResultItem().getDescriptionId(), 120, 65, 0);
			graphics.renderItem(currentRecipe.getResultItem(), 145, 44);
			if (te.areRunesMatching()) {
				RenderSystem.setShaderTexture(0, GUI_Chisel); // Cap

				graphics.blit(GUI_Chisel, 162 - 42, 45 + 32, 176, 96, 16, 16);
			} else {
				RenderSystem.setShaderTexture(0, GUI_Chisel); // Cap

				graphics.blit(GUI_Chisel, 162 - 42, 45 + 32, 176, 80, 16, 16);
			}
			if (te.getItem(3).getItem() instanceof ItemKnapper) {
				RenderSystem.setShaderTexture(0, GUI_Chisel); // Cap
				graphics.blit(GUI_Chisel, 162 - 42 + 16, 45 + 32, 176 + 16, 96, 16, 16);
			} else {
				RenderSystem.setShaderTexture(0, GUI_Chisel); // Cap
				graphics.blit(GUI_Chisel, 162 - 42 + 16, 45 + 32, 176 + 16, 96 - 16, 16, 16);
			}
		}
		matrixStack.popPose();

	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {
		this.renderBackground(graphics);
		graphics.blit(GUI_Chisel, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
	}

	/**
	 * Loads a pattern as a preview overlay (purple glow).
	 * The player must click each rune to confirm it (turns red).
	 */
	private void loadPatternIntoGrid(byte[][] sourcePattern) {
		// Clear any existing confirmed pattern
		pattern = ChiselRecipe.blank();
		refreshButtonsFromPattern();
		PacketHandler.CHANNELRUNES.sendToServer(new PacketUpdateChiselRunes(pattern));
		// Set the preview (purple highlight, client-side only)
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				preview[i][j] = sourcePattern[i][j];
			}
		}
	}

	/**
	 * Refreshes all button visual states from the current local pattern array.
	 */
	private void refreshButtonsFromPattern() {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				runeButtonArray[i][j].setState(pattern[i][j] != 0);
			}
		}
	}

	// ── Binder pattern selector panel ───────────────────────────────────

	/**
	 * Scans the RuneBinder's inventory and populates binderEntries with
	 * all valid pattern items found inside.
	 */
	private void rebuildBinderEntries(ItemStack binderStack) {
		binderEntries.clear();
		binderScrollOffset = 0;
		binderHoveredEntry = -1;

		if (binderStack.isEmpty() || !(binderStack.getItem() instanceof ItemRuneBinder)) return;

		IItemHandler handler = binderStack.getCapability(ForgeCapabilities.ITEM_HANDLER)
				.orElse(null);
		if (!(handler instanceof RuneBinderItemHandler rbHandler)) return;

		rbHandler.load();

		for (int i = 0; i < rbHandler.getSlots(); i++) {
			ItemStack slotStack = rbHandler.getStackInSlot(i);
			if (slotStack.getItem() instanceof ItemRunePattern pat) {
				ChiselRecipe recipe = pat.getRecipe();
				if (recipe == null) continue;

				ItemStack resultIcon = recipe.getResultItem();
				String name = I18n.get(resultIcon.getDescriptionId());
				byte[][] patternData = recipe.getPattern();

				binderEntries.add(new BinderPatternEntry(i, pat, resultIcon, name, patternData, recipe));
			}
		}
	}

	/** Returns the X position of the binder panel (to the left of the main GUI). */
	private int getPanelX() {
		return leftPos - PANEL_WIDTH - 4;
	}

	/** Returns the Y position of the binder panel (aligned with the top of the main GUI). */
	private int getPanelY() {
		return topPos;
	}

	/** Total pixel height of the panel's entry list area. */
	private int getPanelListHeight() {
		return PANEL_VISIBLE_ENTRIES * (PANEL_ENTRY_HEIGHT + PANEL_ENTRY_SPACING);
	}

	/**
	 * Renders the binder pattern selector panel to the left of the chisel GUI.
	 * Called from render() when binderPanelVisible is true.
	 */
	private void renderBinderPanel(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		int px = getPanelX();
		int py = getPanelY();
		int panelHeight = getPanelListHeight() + 16 + PANEL_PADDING * 2; // 16 for header

		// Panel background
		graphics.fill(px, py, px + PANEL_WIDTH, py + panelHeight, 0xCC1A1010);
		// Border
		graphics.fill(px, py, px + PANEL_WIDTH, py + 1, 0xAA8B6914);
		graphics.fill(px, py + panelHeight - 1, px + PANEL_WIDTH, py + panelHeight, 0xAA8B6914);
		graphics.fill(px, py, px + 1, py + panelHeight, 0xAA8B6914);
		graphics.fill(px + PANEL_WIDTH - 1, py, px + PANEL_WIDTH, py + panelHeight, 0xAA8B6914);

		// Header
		String header = ChatFormatting.GOLD + "Binder" + ChatFormatting.DARK_GRAY
				+ " (" + binderEntries.size() + ")";
		graphics.drawString(font, header, px + PANEL_PADDING, py + PANEL_PADDING, 0xFFFFFF, true);

		// Entry list area
		int listX = px + PANEL_PADDING;
		int listY = py + 14 + PANEL_PADDING;
		int listWidth = PANEL_WIDTH - PANEL_PADDING * 2 - PANEL_SCROLLBAR_WIDTH - 2;

		// Clamp scroll
		int maxScroll = Math.max(0, binderEntries.size() - PANEL_VISIBLE_ENTRIES);
		binderScrollOffset = Mth.clamp(binderScrollOffset, 0, maxScroll);

		binderHoveredEntry = -1;

		// Draw entries
		for (int i = 0; i < PANEL_VISIBLE_ENTRIES && (i + binderScrollOffset) < binderEntries.size(); i++) {
			int entryIdx = i + binderScrollOffset;
			BinderPatternEntry entry = binderEntries.get(entryIdx);

			int entryY = listY + i * (PANEL_ENTRY_HEIGHT + PANEL_ENTRY_SPACING);

			boolean hovered = mouseX >= listX && mouseX < listX + listWidth
					&& mouseY >= entryY && mouseY < entryY + PANEL_ENTRY_HEIGHT;
			boolean selected = (entryIdx == binderSelectedEntry);

			if (hovered) {
				binderHoveredEntry = entryIdx;
			}

			renderBinderEntry(graphics, entry, listX, entryY, listWidth, hovered, selected, partialTicks);
		}

		// Scrollbar
		if (binderEntries.size() > PANEL_VISIBLE_ENTRIES) {
			int sbX = px + PANEL_WIDTH - PANEL_PADDING - PANEL_SCROLLBAR_WIDTH;
			renderBinderScrollbar(graphics, sbX, listY, getPanelListHeight(), maxScroll);
		}

		// Tooltip for hovered entry
		if (binderHoveredEntry >= 0 && binderHoveredEntry < binderEntries.size()) {
			BinderPatternEntry entry = binderEntries.get(binderHoveredEntry);
			List<Component> tips = new ArrayList<>();
			tips.add(Component.literal(ChatFormatting.GOLD + entry.displayName()));
			tips.add(Component.literal(ChatFormatting.GRAY + "Click to load pattern"));
			graphics.renderComponentTooltip(font, tips, mouseX, mouseY);
		}

		// Empty state
		if (binderEntries.isEmpty()) {
			graphics.drawString(font, "No patterns", px + PANEL_PADDING, listY + 4, 0x888888, false);
		}
	}

	/**
	 * Renders a single entry row in the binder panel.
	 */
	private void renderBinderEntry(GuiGraphics graphics, BinderPatternEntry entry,
								   int x, int y, int entryWidth, boolean hovered, boolean selected, float partialTicks) {
		// Background
		int bgColor;
		if (selected) {
			bgColor = 0x608B6914;
		} else if (hovered) {
			bgColor = 0x40C8A050;
		} else {
			bgColor = 0x20FFFFFF;
		}
		graphics.fill(x, y, x + entryWidth, y + PANEL_ENTRY_HEIGHT, bgColor);

		// Selected indicator bar on the left edge
		if (selected) {
			graphics.fill(x, y, x + 2, y + PANEL_ENTRY_HEIGHT, 0xFFDAA520);
		}

		// Border
		int borderColor = selected ? 0xAA8B6914 : (hovered ? 0x80C8A050 : 0x30FFFFFF);
		graphics.fill(x, y, x + entryWidth, y + 1, borderColor);
		graphics.fill(x, y + PANEL_ENTRY_HEIGHT - 1, x + entryWidth, y + PANEL_ENTRY_HEIGHT, borderColor);

		// Item icon
		Lighting.setupFor3DItems();
		graphics.renderItem(entry.resultIcon(), x + 3, y + 4);

		// Pattern name (truncated)
		String name = entry.displayName();
		int maxNameWidth = entryWidth - 22;
		if (font.width(name) > maxNameWidth) {
			while (font.width(name + "..") > maxNameWidth && !name.isEmpty()) {
				name = name.substring(0, name.length() - 1);
			}
			name = name + "..";
		}
		int textColor = selected ? 0xFFDAA520 : (hovered ? 0xFFFFDD : 0xCCCCCC);
		graphics.drawString(font, name, x + 20, y + 4, textColor, true);

		// Mini pattern preview
		if (entry.pattern() != null) {
			int miniX = x + 20;
			int miniY = y + PANEL_ENTRY_HEIGHT - MINI_CELL * 8 - 1;
			// Only show if there's room below the name
			miniY = y + 14;
			renderMiniPattern(graphics, entry.pattern(), miniX, miniY, partialTicks);
		}
	}

	/**
	 * Renders a tiny 8x8 pattern preview grid for a binder panel entry.
	 */
	private void renderMiniPattern(GuiGraphics graphics, byte[][] patternData, int x, int y, float partialTicks) {
		int gridSize = 8 * MINI_CELL;
		graphics.fill(x - 1, y - 1, x + gridSize + 1, y + gridSize + 1, 0x60000000);

		float gameTime = (Minecraft.getInstance().level != null
				? Minecraft.getInstance().level.getGameTime() : 0) + partialTicks;
		float pulse = 0.7f + 0.3f * ((float) Math.sin(gameTime * 0.3f) * 0.5f + 0.5f);

		int activeAlpha = Mth.clamp((int) (200 * pulse), 80, 255);
		int activeColor = (activeAlpha << 24) | (0xDC << 16) | (0x19 << 8) | 0x14;

		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				int cx = x + j * MINI_CELL;
				int cy = y + i * MINI_CELL;
				if (patternData[i][j] != 0) {
					graphics.fill(cx, cy, cx + MINI_CELL, cy + MINI_CELL, activeColor);
				} else {
					graphics.fill(cx, cy, cx + MINI_CELL, cy + MINI_CELL, 0x30333333);
				}
			}
		}
	}

	/**
	 * Renders the scrollbar for the binder panel.
	 */
	private void renderBinderScrollbar(GuiGraphics graphics, int x, int y, int height, int maxScroll) {
		graphics.fill(x, y, x + PANEL_SCROLLBAR_WIDTH, y + height, 0x40000000);

		float thumbRatio = (float) PANEL_VISIBLE_ENTRIES / binderEntries.size();
		int thumbHeight = Math.max(12, (int) (height * thumbRatio));
		int scrollRange = height - thumbHeight;
		int thumbY = y + (maxScroll > 0 ? (int) ((float) binderScrollOffset / maxScroll * scrollRange) : 0);

		int thumbColor = binderDraggingScrollbar ? 0xCCDAA520 : 0x99AAAAAA;
		graphics.fill(x + 1, thumbY, x + PANEL_SCROLLBAR_WIDTH - 1, thumbY + thumbHeight, thumbColor);
	}

	/**
	 * Updates binderScrollOffset from the mouse Y position within the scrollbar track.
	 */
	private void updateBinderScrollFromMouse(double mouseY, int trackY, int trackHeight) {
		int maxScroll = Math.max(0, binderEntries.size() - PANEL_VISIBLE_ENTRIES);
		float thumbRatio = (float) PANEL_VISIBLE_ENTRIES / binderEntries.size();
		int thumbHeight = Math.max(12, (int) (trackHeight * thumbRatio));
		int scrollRange = trackHeight - thumbHeight;

		float relativeY = (float) (mouseY - trackY - thumbHeight / 2.0f);
		float fraction = Mth.clamp(relativeY / scrollRange, 0, 1);
		binderScrollOffset = Math.round(fraction * maxScroll);
	}

	@Override
	protected void init() {
		super.init();
		left = width / 2 - guiWidth / 2;
		top = height / 2 - guiHeight / 2;
		renderables.clear();
		int inc = 0;
		for (int i = 0; i < runeButtonArray.length; i++) {
			for (int j = 0; j < runeButtonArray.length; j++) {
				this.addRenderableWidget(runeButtonArray[i][j] = new ChiselButton(GUI_Chisel, inc, i, j,
						left + guiWidth - (guiWidth - 50 - (j * 8)), top + guiHeight - (160 - (i * 8)), 8, 8, 176, 0,
						te.runesList[i][j] != 0, (press) -> {
							// Handled by click-and-drag system in mouseClicked/mouseDragged/mouseReleased
						}));
				inc++;
			}
		}
		this.addRenderableWidget(clearButton = new HLButtonTextured(GUI_Chisel, CLEARBUTTONID,
				left + guiWidth - (guiWidth - 120), top + guiHeight - (170), 16, 16, 176, 16, (press) -> {
					pattern = ChiselRecipe.blank();
					preview = ChiselRecipe.blank();
					refreshButtonsFromPattern();
					PacketHandler.CHANNELRUNES.sendToServer(new PacketUpdateChiselRunes(pattern));
				}));
		this.addRenderableWidget(chiselButton = new HLButtonTextured(GUI_Chisel, CHISELBUTTONID,
				left + guiWidth - (guiWidth - 120), top + guiHeight - (150), 16, 16, 176, 48, (press) -> {
					if (te.contents.get(3).getItem() != Items.AIR) {
						PacketHandler.CHANNELRUNES.sendToServer(new PacketChiselCraftingEvent());
						pattern = ChiselRecipe.blank();
						preview = ChiselRecipe.blank();
						refreshButtonsFromPattern();
						PacketHandler.CHANNELRUNES.sendToServer(new PacketUpdateChiselRunes(pattern));
					}
				}));
		this.addRenderableWidget(loadPatternButton = new HLButtonTextured(GUI_Chisel, LOADPATTERNBUTTONID,
				left + 28, top + 80, 16, 16, 176, 32, (press) -> {
					ItemStack patternStack = te.getItem(4);
					if (patternStack.getItem() instanceof ItemRunePattern runePattern) {
						ChiselRecipe patternRecipe = runePattern.getRecipe();
						if (patternRecipe != null && patternRecipe.getPattern() != null) {
							loadPatternIntoGrid(patternRecipe.getPattern());
						}
					}
				}));

	}

	// --- Click-and-drag rune painting ---

	/**
	 * Finds the ChiselButton under the given mouse coordinates and applies
	 * the current drag paint mode to it (if not already visited this drag).
	 */
	private void paintRuneAt(double mouseX, double mouseY) {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				ChiselButton button = runeButtonArray[i][j];
				if (!dragVisited[i][j]
						&& mouseX >= button.getX() && mouseX < button.getX() + button.getWidth()
						&& mouseY >= button.getY() && mouseY < button.getY() + button.getHeight()) {
					dragVisited[i][j] = true;
					if (dragPaintOn) {
						// Activate rune
						button.setState(true);
						pattern[i][j] = 1;
					} else {
						// Deactivate rune
						button.setState(false);
						pattern[i][j] = 0;
					}
				}
			}
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) { // Left click only
			// ── Binder panel interaction ────────────────────────────────
			if (binderPanelVisible && !binderEntries.isEmpty()) {
				int px = getPanelX();
				int py = getPanelY();
				int listX = px + PANEL_PADDING;
				int listY = py + 14 + PANEL_PADDING;
				int listWidth = PANEL_WIDTH - PANEL_PADDING * 2 - PANEL_SCROLLBAR_WIDTH - 2;
				int listHeight = getPanelListHeight();

				// Check scrollbar click
				if (binderEntries.size() > PANEL_VISIBLE_ENTRIES) {
					int sbX = px + PANEL_WIDTH - PANEL_PADDING - PANEL_SCROLLBAR_WIDTH;
					if (mouseX >= sbX && mouseX < sbX + PANEL_SCROLLBAR_WIDTH
							&& mouseY >= listY && mouseY < listY + listHeight) {
						binderDraggingScrollbar = true;
						updateBinderScrollFromMouse(mouseY, listY, listHeight);
						return true;
					}
				}

				// Check entry click
				if (mouseX >= listX && mouseX < listX + listWidth
						&& mouseY >= listY && mouseY < listY + listHeight) {
					int maxScroll = Math.max(0, binderEntries.size() - PANEL_VISIBLE_ENTRIES);
					binderScrollOffset = Mth.clamp(binderScrollOffset, 0, maxScroll);
					for (int i = 0; i < PANEL_VISIBLE_ENTRIES && (i + binderScrollOffset) < binderEntries.size(); i++) {
						int entryY = listY + i * (PANEL_ENTRY_HEIGHT + PANEL_ENTRY_SPACING);
						if (mouseY >= entryY && mouseY < entryY + PANEL_ENTRY_HEIGHT) {
							int entryIdx = i + binderScrollOffset;
							binderSelectedEntry = entryIdx;
							BinderPatternEntry entry = binderEntries.get(entryIdx);
							// Load the selected pattern into the chisel grid
							if (entry.pattern() != null) {
								loadPatternIntoGrid(entry.pattern());
							}
							return true;
						}
					}
				}
			}

			// ── Rune button drag painting ───────────────────────────────
			// Check if click landed on any rune button
			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					ChiselButton cb = runeButtonArray[i][j];
					if (mouseX >= cb.getX() && mouseX < cb.getX() + cb.getWidth()
							&& mouseY >= cb.getY() && mouseY < cb.getY() + cb.getHeight()) {
						// Start a drag session
						isDragging = true;
						// Determine paint mode from the first button clicked:
						// If it has a preview waiting and isn't confirmed yet, paint ON
						// If it's already active, paint OFF (toggle off)
						// If it's inactive, paint ON
						if (preview[i][j] != 0 && pattern[i][j] == 0) {
							dragPaintOn = true;
						} else {
							dragPaintOn = (pattern[i][j] == 0);
						}
						// Clear visited tracking
						for (int x = 0; x < 8; x++) {
							for (int y = 0; y < 8; y++) {
								dragVisited[x][y] = false;
							}
						}
						// Paint the first button
						paintRuneAt(mouseX, mouseY);
						return true;
					}
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (binderDraggingScrollbar && button == 0) {
			int listY = getPanelY() + 14 + PANEL_PADDING;
			updateBinderScrollFromMouse(mouseY, listY, getPanelListHeight());
			return true;
		}
		if (isDragging && button == 0) {
			paintRuneAt(mouseX, mouseY);
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (binderDraggingScrollbar && button == 0) {
			binderDraggingScrollbar = false;
			return true;
		}
		if (isDragging && button == 0) {
			isDragging = false;
			// Sync the final pattern to the server in one batch
			PacketHandler.CHANNELRUNES.sendToServer(new PacketUpdateChiselRunes(pattern));
			return true;
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		// Scroll the binder panel if mouse is over it
		if (binderPanelVisible && !binderEntries.isEmpty()) {
			int px = getPanelX();
			int py = getPanelY();
			int panelHeight = getPanelListHeight() + 16 + PANEL_PADDING * 2;
			if (mouseX >= px && mouseX < px + PANEL_WIDTH
					&& mouseY >= py && mouseY < py + panelHeight) {
				int maxScroll = Math.max(0, binderEntries.size() - PANEL_VISIBLE_ENTRIES);
				binderScrollOffset = Mth.clamp(binderScrollOffset - (int) Math.signum(delta), 0, maxScroll);
				return true;
			}
		}
		return super.mouseScrolled(mouseX, mouseY, delta);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
		InputConstants.Key mouseKey = InputConstants.getKey(pKeyCode, pScanCode);
		if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
			this.onClose();
		}
		return super.keyPressed(pKeyCode, pScanCode, pModifiers);
	}

}