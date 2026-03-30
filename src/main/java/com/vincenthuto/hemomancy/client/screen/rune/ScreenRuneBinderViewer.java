package com.vincenthuto.hemomancy.client.screen.rune;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.rune.ItemRuneBinder;
import com.vincenthuto.hemomancy.common.item.rune.pattern.ItemRunePattern;
import com.vincenthuto.hemomancy.common.itemhandler.RuneBinderItemHandler;
import com.vincenthuto.hemomancy.common.recipe.ChiselRecipe;
import com.vincenthuto.hutoslib.client.HLClientUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
 * Refactored RuneBinder viewer screen. Displays all stored RunePatterns
 * in a scrollable list with rune icon, pattern name, and a mini 8x8
 * pattern preview grid for each entry. Click an entry to open the
 * full ScreenRunePattern detail view.
 */
@OnlyIn(Dist.CLIENT)
public class ScreenRuneBinderViewer extends Screen {

	// ── Singleton access ────────────────────────────────────────────────
	private static ScreenRuneBinderViewer screen;

	public static void openScreenViaItem() {
		openScreen(true);
	}

	public static void openScreen(boolean ignoreNextMouseClick) {
		screen = new ScreenRuneBinderViewer();
		Minecraft.getInstance().setScreen(screen);
	}

	// ── Constants ───────────────────────────────────────────────────────
	private static final ResourceLocation TEXTURE =
			new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/rune_binder_gui.png");

	/** Dimensions of the background GUI texture. */
	private static final int GUI_WIDTH = 200;
	private static final int GUI_HEIGHT = 228;

	/** Layout constants for the entry list inside the GUI. */
	private static final int LIST_LEFT_PADDING = 10;
	private static final int LIST_TOP_PADDING = 24;
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
	private final ItemStack binderIcon = new ItemStack(ItemInit.rune_binder.get());
	public RuneBinderItemHandler handler;

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
			ItemRunePattern patternItem,
			ItemStack resultIcon,
			String displayName,
			byte[][] pattern,
			RegistryObject<Item> runeRef,
			ChiselRecipe recipe
	) {}

	// ── Constructor ─────────────────────────────────────────────────────
	public ScreenRuneBinderViewer() {
		super(Component.translatable("screen.hemomancy.rune_binder_viewer"));
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

		ItemStack stack = Hemomancy.findItemInPlayerInv(player, ItemRuneBinder.class);
		if (stack.isEmpty()) return;

		if (!stack.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) return;
		IItemHandler binderHandler = stack.getCapability(ForgeCapabilities.ITEM_HANDLER)
				.orElseThrow(NullPointerException::new);
		if (!(binderHandler instanceof RuneBinderItemHandler rbHandler)) return;

		handler = rbHandler;
		handler.load();

		for (int i = 0; i < handler.getSlots(); i++) {
			ItemStack slotStack = handler.getStackInSlot(i);
			if (slotStack.getItem() instanceof ItemRunePattern pat) {
				ChiselRecipe recipe = pat.getRecipe();
				if (recipe == null) continue;

				ItemStack resultIcon = recipe.getResultItem();
				String name = I18n.get(resultIcon.getDescriptionId());
				byte[][] pattern = recipe.getPattern();

				entries.add(new PatternEntry(i, pat, resultIcon, name, pattern, pat.getRune(), recipe));
			}
		}
	}

	// ── Rendering ───────────────────────────────────────────────────────
	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(graphics);

		// Draw background texture (tiled/stretched to our GUI size)
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		graphics.blit(TEXTURE, left, top, 0, 0, Math.min(GUI_WIDTH, 175), GUI_HEIGHT);
		// If the texture is smaller than GUI_WIDTH, fill the right side
		if (GUI_WIDTH > 175) {
			graphics.fill(left + 175, top, left + GUI_WIDTH, top + GUI_HEIGHT, 0xFF2B1A1A);
		}

		// ── Title bar ───────────────────────────────────────────────────
		int patternCount = entries.size();
		String titleText = ChatFormatting.GOLD + "Rune Binder" + ChatFormatting.DARK_GRAY
				+ " (" + patternCount + " pattern" + (patternCount != 1 ? "s" : "") + ")";
		graphics.drawString(font, titleText, left + 26, top + 8, 0xFFFFFF, true);

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
					0x888888, false);
		}
	}

	/**
	 * Renders a single pattern entry row.
	 */
	private void renderEntry(GuiGraphics graphics, PatternEntry entry,
							 int x, int y, int entryWidth, boolean hovered, float partialTicks) {
		// Background fill
		int bgColor = hovered ? 0x60C8A050 : 0x30FFFFFF;
		graphics.fill(x, y, x + entryWidth, y + ENTRY_HEIGHT, bgColor);

		// Border
		int borderColor = hovered ? 0xAAC8A050 : 0x40FFFFFF;
		graphics.fill(x, y, x + entryWidth, y + 1, borderColor);                         // top
		graphics.fill(x, y + ENTRY_HEIGHT - 1, x + entryWidth, y + ENTRY_HEIGHT, borderColor); // bottom
		graphics.fill(x, y, x + 1, y + ENTRY_HEIGHT, borderColor);                       // left
		graphics.fill(x + entryWidth - 1, y, x + entryWidth, y + ENTRY_HEIGHT, borderColor);   // right

		// Rune item icon (16x16)
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
		int textColor = hovered ? 0xFFFFDD : 0xDDDDDD;
		graphics.drawString(font, name, x + 24, y + 4, textColor, true);

		// Slot index label
		String slotLabel = ChatFormatting.DARK_GRAY + "#" + (entry.slotIndex() + 1);
		graphics.drawString(font, slotLabel, x + 24, y + 16, 0x999999, false);

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
		graphics.fill(x - 1, y - 1, x + gridSize + 1, y + gridSize + 1, 0x80000000);

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
					graphics.fill(cellX, cellY, cellX + MINI_CELL_SIZE, cellY + MINI_CELL_SIZE, 0x40333333);
				}
			}
		}
	}

	/**
	 * Renders the scrollbar track and thumb.
	 */
	private void renderScrollbar(GuiGraphics graphics, int x, int y, int width, int height, int maxScroll) {
		// Track background
		graphics.fill(x, y, x + width, y + height, 0x40000000);

		// Thumb
		float thumbRatio = (float) VISIBLE_ENTRIES / entries.size();
		int thumbHeight = Math.max(15, (int) (height * thumbRatio));
		int scrollRange = height - thumbHeight;
		int thumbY = y + (maxScroll > 0 ? (int) ((float) scrollOffset / maxScroll * scrollRange) : 0);

		int thumbColor = draggingScrollbar ? 0xCCC8A050 : 0x99AAAAAA;
		graphics.fill(x + 1, thumbY, x + width - 1, thumbY + thumbHeight, thumbColor);
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
						+ ForgeRegistries.ITEMS.getKey(entry.runeRef().get()) + ".pattern.text");
				Minecraft.getInstance().setScreen(
						new ScreenRunePattern(entry.runeRef(), entry.recipe(), patternText));
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

