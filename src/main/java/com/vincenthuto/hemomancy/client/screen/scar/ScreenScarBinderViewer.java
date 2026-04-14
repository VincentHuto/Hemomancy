package com.vincenthuto.hemomancy.client.screen.scar;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.scar.ItemScarBinder;
import com.vincenthuto.hemomancy.common.item.scar.pattern.ItemScarPattern;
import com.vincenthuto.hemomancy.common.itemhandler.ScarBinderItemHandler;
import com.vincenthuto.hemomancy.common.recipe.ScarRecipe;
import com.vincenthuto.hutoslib.client.HLClientUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Refactored ScarBinder viewer screen. Displays all stored scarPatterns
 * in a scrollable list with scar icon, pattern name, and a mini 8x8
 * pattern preview grid for each entry. Click an entry to open the
 * full ScreenScarPattern detail view.
 */
@OnlyIn(Dist.CLIENT)
public class ScreenScarBinderViewer extends Screen {

	// ── Singleton access ────────────────────────────────────────────────
	private static ScreenScarBinderViewer screen;

	public static void openScreenViaItem() {
		openScreen(true);
	}

	public static void openScreen(boolean ignoreNextMouseClick) {
		screen = new ScreenScarBinderViewer();
		Minecraft.getInstance().setScreen(screen);
	}

	// ── Constants ───────────────────────────────────────────────────────

	/** Dimensions of the GUI panel. */
	private static final int GUI_WIDTH = 200;
	private static final int GUI_HEIGHT = 228;

	/** Layout constants for the entry list inside the GUI. */
	private static final int LIST_LEFT_PADDING = 16;
	private static final int LIST_TOP_PADDING = 29;
	private static final int ENTRY_HEIGHT = 28;
	private static final int ENTRY_SPACING = 2;
	private static final int VISIBLE_ENTRIES = 7;

	/** Mini pattern preview size (pixels per cell). */
	private static final int MINI_CELL_SIZE = 2;

	/** Scrollbar dimensions. */
	private static final int SCROLLBAR_WIDTH = 6;

	// ── Pulse animation for the mini-grid ───────────────────────────────
	private static final float PULSE_SPEED = 3.0f;
	private static final float PULSE_MIN = 0.55f;

	// ── Instance state ──────────────────────────────────────────────────
	private int left, top;
	private final ItemStack binderIcon = new ItemStack(ItemInit.scar_binder.get());
	public ScarBinderItemHandler handler;

	/** Cached list of pattern entries extracted from the binder inventory. */
	private final List<PatternEntry> entries = new ArrayList<>();

	/** Current scroll offset (index of the first visible entry). */
	private int scrollOffset = 0;

	/** Index of the entry under the mouse cursor, or -1 if none. */
	private int hoveredEntry = -1;

	/** Whether the user is currently dragging the scrollbar thumb. */
	private boolean draggingScrollbar = false;

	// ── Inner data class ────────────────────────────────────────────────
	/**
	 * Holds the data we need to render one pattern entry.
	 */
	private record PatternEntry(
			int slotIndex,
			ItemScarPattern patternItem,
			ItemStack resultIcon,
			String displayName,
			byte[][] pattern,
			RegistryObject<Item> scarRef,
			ScarRecipe recipe
	) {}

	// ── Constructor ─────────────────────────────────────────────────────
	public ScreenScarBinderViewer() {
		super(Component.translatable("screen.hemomancy.scar_binder_viewer"));
	}

	// ── Initialization ──────────────────────────────────────────────────
	@Override
	protected void init() {
		super.init();
		left = (width - GUI_WIDTH) / 2;
		top = (height - GUI_HEIGHT) / 2;
		scrollOffset = 0;
		hoveredEntry = -1;
		rebuildEntryList();
	}

	/**
	 * Scans the binder's inventory and builds the list of pattern entries.
	 */
	private void rebuildEntryList() {
		entries.clear();
		Player player = HLClientUtils.getClientPlayer();
		if (player == null) return;

		ItemStack stack = Hemomancy.findItemInPlayerInv(player, ItemScarBinder.class);
		if (stack.isEmpty()) return;

		if (!stack.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) return;
		IItemHandler binderHandler = stack.getCapability(ForgeCapabilities.ITEM_HANDLER)
				.orElseThrow(NullPointerException::new);
		if (!(binderHandler instanceof ScarBinderItemHandler rbHandler)) return;

		handler = rbHandler;
		handler.load();

		for (int i = 0; i < handler.getSlots(); i++) {
			ItemStack slotStack = handler.getStackInSlot(i);
			if (slotStack.getItem() instanceof ItemScarPattern pat) {
				ScarRecipe recipe = pat.getRecipe();
				if (recipe == null) continue;

				ItemStack resultIcon = recipe.getResultItem();
				String name = resultIcon.getHoverName().getString();
				byte[][] pattern = recipe.getPattern();

				entries.add(new PatternEntry(i, pat, resultIcon, name, pattern, pat.getSCAR(), recipe));
			}
		}
	}

	// ── Rendering ───────────────────────────────────────────────────────
	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(graphics);

		// ── Book-like parchment background ──────────────────────────────
		renderBookBackground(graphics, left, top, GUI_WIDTH, GUI_HEIGHT);

		// ── Title bar ───────────────────────────────────────────────────
		int patternCount = entries.size();
		String titleText = ChatFormatting.DARK_RED + "Scar Binder" + ChatFormatting.DARK_GRAY
				+ " (" + patternCount + " pattern" + (patternCount != 1 ? "s" : "") + ")";
		graphics.drawString(font, titleText, left + 26, top + 8, 0x442200, false);

		// Thin separator line below title
		graphics.fill(left + 6, top + 20, left + GUI_WIDTH - 6, top + 21, 0x40442200);

		// Binder icon in top-left
		Lighting.setupFor3DItems();
		graphics.renderItem(binderIcon, left + 6, top + 4);

		// ── Entry list ──────────────────────────────────────────────────
		int listX = left + LIST_LEFT_PADDING;
		int listY = top + LIST_TOP_PADDING;
		int listWidth = GUI_WIDTH - LIST_LEFT_PADDING * 2 - SCROLLBAR_WIDTH - 4;
		int listHeight = VISIBLE_ENTRIES * (ENTRY_HEIGHT + ENTRY_SPACING);

		// Clamp scroll
		int maxScroll = Math.max(0, entries.size() - VISIBLE_ENTRIES);
		scrollOffset = Mth.clamp(scrollOffset, 0, maxScroll);

		hoveredEntry = -1;

		// Draw entries
		for (int i = 0; i < VISIBLE_ENTRIES && (i + scrollOffset) < entries.size(); i++) {
			int entryIdx = i + scrollOffset;
			PatternEntry entry = entries.get(entryIdx);

			int entryX = listX;
			int entryY = listY + i * (ENTRY_HEIGHT + ENTRY_SPACING);

			boolean hovered = mouseX >= entryX && mouseX < entryX + listWidth
					&& mouseY >= entryY && mouseY < entryY + ENTRY_HEIGHT;

			if (hovered) {
				hoveredEntry = entryIdx;
			}

			renderEntry(graphics, entry, entryX, entryY, listWidth, hovered, partialTicks);
		}

		// ── Scrollbar ───────────────────────────────────────────────────
		if (entries.size() > VISIBLE_ENTRIES) {
			renderScrollbar(graphics, left + GUI_WIDTH - LIST_LEFT_PADDING - SCROLLBAR_WIDTH,
					listY, SCROLLBAR_WIDTH, listHeight, maxScroll);
		}

		// ── Tooltip for hovered entry ───────────────────────────────────
		if (hoveredEntry >= 0 && hoveredEntry < entries.size()) {
			PatternEntry entry = entries.get(hoveredEntry);
			List<Component> tooltipLines = new ArrayList<>();
			tooltipLines.add(Component.literal(ChatFormatting.GOLD + entry.displayName()));
			tooltipLines.add(Component.literal(ChatFormatting.GRAY + "Click to view full pattern"));
			if (!entry.recipe().getIngredients().isEmpty()) {
				tooltipLines.add(Component.literal(ChatFormatting.DARK_GRAY + "Ingredients: "
						+ ChatFormatting.WHITE + entry.recipe().getIngredients().size()));
			}
			graphics.renderComponentTooltip(font, tooltipLines, mouseX, mouseY);
		}

		// ── Empty state ─────────────────────────────────────────────────
		if (entries.isEmpty()) {
			String emptyMsg = "No patterns stored";
			int textWidth = font.width(emptyMsg);
			graphics.drawString(font, emptyMsg,
					left + (GUI_WIDTH - textWidth) / 2,
					top + GUI_HEIGHT / 2 - 4,
					0x998877, false);
		}
	}

	/**
	 * Renders a single pattern entry row.
	 */
	private void renderEntry(GuiGraphics graphics, PatternEntry entry,
							 int x, int y, int entryWidth, boolean hovered, float partialTicks) {
		// Background fill — warm parchment tones
		int bgColor = hovered ? 0x40885530 : 0x18442200;
		graphics.fill(x, y, x + entryWidth, y + ENTRY_HEIGHT, bgColor);

		// Border — subtle ink lines
		int borderColor = hovered ? 0x60885530 : 0x25442200;
		graphics.fill(x, y, x + entryWidth, y + 1, borderColor);                         // top
		graphics.fill(x, y + ENTRY_HEIGHT - 1, x + entryWidth, y + ENTRY_HEIGHT, borderColor); // bottom
		graphics.fill(x, y, x + 1, y + ENTRY_HEIGHT, borderColor);                       // left
		graphics.fill(x + entryWidth - 1, y, x + entryWidth, y + ENTRY_HEIGHT, borderColor);   // right

		// Scar item icon (16x16)
		Lighting.setupFor3DItems();
		graphics.renderItem(entry.resultIcon(), x + 4, y + 6);

		// Pattern name
		String name = entry.displayName();
		// Truncate if too long
		int maxNameWidth = entryWidth - 46 - (8 * MINI_CELL_SIZE) - 8;
		if (font.width(name) > maxNameWidth) {
			while (font.width(name + "...") > maxNameWidth && !name.isEmpty()) {
				name = name.substring(0, name.length() - 1);
			}
			name = name + "...";
		}
		int textColor = hovered ? 0x331100 : 0x553322;
		graphics.drawString(font, name, x + 24, y + 4, textColor, false);

		// Slot index label
		String slotLabel = "#" + (entry.slotIndex() + 1);
		graphics.drawString(font, slotLabel, x + 24, y + 16, 0x998877, false);

		// Mini 8x8 pattern preview on the right side of the entry
		if (entry.pattern() != null) {
			int miniX = x + entryWidth - (8 * MINI_CELL_SIZE) - 6;
			int miniY = y + (ENTRY_HEIGHT - 8 * MINI_CELL_SIZE) / 2;
			renderMiniPattern(graphics, entry.pattern(), miniX, miniY, partialTicks);
		}
	}

	/**
	 * Renders a tiny 8x8 pattern preview grid.
	 */
	private void renderMiniPattern(GuiGraphics graphics, byte[][] pattern, int x, int y, float partialTicks) {
		// Background for the mini grid
		int gridSize = 8 * MINI_CELL_SIZE;
		graphics.fill(x - 1, y - 1, x + gridSize + 1, y + gridSize + 1, 0x40442200);

		// Pulsing color for active cells
		float gameTime = (Minecraft.getInstance().level != null
				? Minecraft.getInstance().level.getGameTime() : 0) + partialTicks;
		float pulse = PULSE_MIN + (1.0f - PULSE_MIN)
				* ((float) Math.sin(gameTime * PULSE_SPEED * 0.1f) * 0.5f + 0.5f);

		int activeAlpha = Mth.clamp((int) (220 * pulse), 80, 255);
		int activeColor = (activeAlpha << 24) | (0xDC << 16) | (0x19 << 8) | 0x14; // Blood red

		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				int cellX = x + j * MINI_CELL_SIZE;
				int cellY = y + i * MINI_CELL_SIZE;
				if (pattern[i][j] != 0) {
					graphics.fill(cellX, cellY, cellX + MINI_CELL_SIZE, cellY + MINI_CELL_SIZE, activeColor);
				} else {
					graphics.fill(cellX, cellY, cellX + MINI_CELL_SIZE, cellY + MINI_CELL_SIZE, 0x20886644);
				}
			}
		}
	}

	/**
	 * Renders the scrollbar track and thumb.
	 */
	private void renderScrollbar(GuiGraphics graphics, int x, int y, int width, int height, int maxScroll) {
		// Track background
		graphics.fill(x, y, x + width, y + height, 0x20442200);

		// Thumb
		float thumbRatio = (float) VISIBLE_ENTRIES / entries.size();
		int thumbHeight = Math.max(15, (int) (height * thumbRatio));
		int scrollRange = height - thumbHeight;
		int thumbY = y + (maxScroll > 0 ? (int) ((float) scrollOffset / maxScroll * scrollRange) : 0);

		int thumbColor = draggingScrollbar ? 0xBB885530 : 0x77664422;
		graphics.fill(x + 1, thumbY, x + width - 1, thumbY + thumbHeight, thumbColor);
	}

	// ── Book-like Background ───────────────────────────────────────────

	/**
	 * Renders a warm parchment/book-style background with a leather-bound
	 * border, page crease, and subtle aged-edge shading.
	 */
	private void renderBookBackground(GuiGraphics gfx, int x, int y, int w, int h) {
		// Outer shadow (drop shadow around the book)
		gfx.fill(x + 3, y + 3, x + w + 3, y + h + 3, 0x44000000);

		// Leather cover — dark brown outer frame
		gfx.fill(x, y, x + w, y + h, 0xFF3B2312);

		// Inner cover bevel — slightly lighter brown
		gfx.fill(x + 2, y + 2, x + w - 2, y + h - 2, 0xFF4A3020);

		// Parchment page area
		int px = x + 5;
		int py = y + 5;
		int pw = w - 10;
		int ph = h - 10;
		gfx.fill(px, py, px + pw, py + ph, 0xFFD8C8A0);

		// Slightly darker parchment edges (aged look)
		// Top edge
		gfx.fill(px, py, px + pw, py + 2, 0x18442200);
		// Bottom edge
		gfx.fill(px, py + ph - 2, px + pw, py + ph, 0x18442200);
		// Left edge
		gfx.fill(px, py, px + 2, py + ph, 0x18442200);
		// Right edge
		gfx.fill(px + pw - 2, py, px + pw, py + ph, 0x18442200);

		// Spine crease — vertical shadow line near left edge
		gfx.fill(px + 8, py + 4, px + 9, py + ph - 4, 0x20442200);
		gfx.fill(px + 9, py + 4, px + 10, py + ph - 4, 0x10442200);

		// Subtle horizontal ruled lines across the page (like faint notebook lines)
		for (int ly = py + 22; ly < py + ph - 4; ly += 30) {
			gfx.fill(px + 12, ly, px + pw - 6, ly + 1, 0x10886644);
		}

		// Leather border detail — thin highlight on top-left, shadow on bottom-right
		gfx.fill(x, y, x + w, y + 1, 0xFF5A4030);      // top highlight
		gfx.fill(x, y, x + 1, y + h, 0xFF5A4030);      // left highlight
		gfx.fill(x, y + h - 1, x + w, y + h, 0xFF2A1808); // bottom shadow
		gfx.fill(x + w - 1, y, x + w, y + h, 0xFF2A1808); // right shadow
	}

	// ── Input handling ──────────────────────────────────────────────────

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			// Check scrollbar click
			if (entries.size() > VISIBLE_ENTRIES) {
				int sbX = left + GUI_WIDTH - LIST_LEFT_PADDING - SCROLLBAR_WIDTH;
				int sbY = top + LIST_TOP_PADDING;
				int sbHeight = VISIBLE_ENTRIES * (ENTRY_HEIGHT + ENTRY_SPACING);
				if (mouseX >= sbX && mouseX < sbX + SCROLLBAR_WIDTH
						&& mouseY >= sbY && mouseY < sbY + sbHeight) {
					draggingScrollbar = true;
					updateScrollFromMouse(mouseY, sbY, sbHeight);
					return true;
				}
			}

			// Check entry click
			if (hoveredEntry >= 0 && hoveredEntry < entries.size()) {
				PatternEntry entry = entries.get(hoveredEntry);
				Player player = HLClientUtils.getClientPlayer();
				if (player != null) {
					player.playSound(SoundEvents.BOOK_PAGE_TURN, 0.40f, 1F);
				}
				String patternText = I18n.get(Hemomancy.MOD_ID + "."
						+ ForgeRegistries.ITEMS.getKey(entry.scarRef().get()) + ".pattern.text");
				Minecraft.getInstance().setScreen(
						new ScreenScarPattern(entry.scarRef(), entry.recipe(), patternText));
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (draggingScrollbar && button == 0) {
			int sbY = top + LIST_TOP_PADDING;
			int sbHeight = VISIBLE_ENTRIES * (ENTRY_HEIGHT + ENTRY_SPACING);
			updateScrollFromMouse(mouseY, sbY, sbHeight);
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (draggingScrollbar && button == 0) {
			draggingScrollbar = false;
			return true;
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		int maxScroll = Math.max(0, entries.size() - VISIBLE_ENTRIES);
		scrollOffset = Mth.clamp(scrollOffset - (int) Math.signum(delta), 0, maxScroll);
		return true;
	}

	/**
	 * Updates scrollOffset from the mouse Y position within the scrollbar track.
	 */
	private void updateScrollFromMouse(double mouseY, int trackY, int trackHeight) {
		int maxScroll = Math.max(0, entries.size() - VISIBLE_ENTRIES);
		float thumbRatio = (float) VISIBLE_ENTRIES / entries.size();
		int thumbHeight = Math.max(15, (int) (trackHeight * thumbRatio));
		int scrollRange = trackHeight - thumbHeight;

		float relativeY = (float) (mouseY - trackY - thumbHeight / 2.0f);
		float fraction = Mth.clamp(relativeY / scrollRange, 0, 1);
		scrollOffset = Math.round(fraction * maxScroll);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);
		if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
			this.onClose();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}

