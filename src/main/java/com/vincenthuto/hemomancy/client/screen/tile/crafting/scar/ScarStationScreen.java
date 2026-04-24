package com.vincenthuto.hemomancy.client.screen.tile.crafting.scar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.scar.ItemScarBinder;
import com.vincenthuto.hemomancy.common.item.scar.pattern.ItemScarPattern;
import com.vincenthuto.hemomancy.common.itemhandler.ScarBinderItemHandler;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.ScarStationMenu;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.scars.PacketScarCraftingEvent;
import com.vincenthuto.hemomancy.common.network.capa.scars.PacketUpdateScarPattern;
import com.vincenthuto.hemomancy.common.recipe.ScarRecipe;
import com.vincenthuto.hemomancy.common.tile.crafting.ScarStationBlockEntity;
import com.vincenthuto.hutoslib.common.item.ItemKnapper;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

public class ScarStationScreen extends AbstractContainerScreen<ScarStationMenu> {
	/** Texture still used for scar grid button sprites. */
	private static final ResourceLocation GUI_Chisel = Hemomancy.rloc("textures/gui/scar_station.png");

	// ── Hemomancy theme colors (matching GhastlyAlembicScreen) ──
	private static final int SLOT_BG = 0xFF1A0808;
	private static final int SLOT_BORDER_DARK = 0xFF0D0303;
	private static final int SLOT_BORDER_LIGHT = 0xFF3A1212;
	private static final int BORDER_OUTER = 0xFF330808;
	private static final int BORDER_INNER = 0xFF220606;

	private static final int CRAFT_AREA_HEIGHT = 100;
	private static final int VEIN_COUNT = 14;

	private final Inventory playerInv;
	private final ScarStationBlockEntity te;
	int left, top;
	int guiWidth = 176;
	int guiHeight = 186;
	public ScarButton[][] scarbuttonArray = new ScarButton[8][8];
	ScarActionButton clearButton;
	ScarActionButton ScarButton;
	ScarActionButton loadPatternButton;
	public byte[][] pattern = ScarRecipe.blank();
	public byte[][] preview = ScarRecipe.blank();
	private ItemStack lastPatternSlotItem = ItemStack.EMPTY;

	/** Seeded vein parameters for animated background. */
	private float[][] veinParams;
	/** Pre-generated speckle positions and colors for the vein background. */
	private int[][] speckleData;

	// --- Click-and-drag scar painting state ---
	/** Whether the player is currently dragging across the scar grid. */
	private boolean isDragging = false;
	/** The paint mode for the current drag: true = activate scars, false = deactivate. */
	private boolean dragPaintOn = true;
	/** Tracks which buttons have already been toggled during this drag to avoid re-processing. */
	private final boolean[][] dragVisited = new boolean[8][8];

	// ── Binder pattern selector panel state ─────────────────────────────
	/** Cached pattern entries extracted from a ScarBinder in the pattern slot. */
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
			ItemScarPattern patternItem,
			ItemStack resultIcon,
			String displayName,
			byte[][] pattern,
			ScarRecipe recipe
	) {}

	public ScarStationScreen(ScarStationMenu screenContainer, Inventory inv, Component titleIn) {
		super(screenContainer, inv, titleIn);
		this.leftPos = 0;
		this.topPos = 0;
		this.imageWidth = 176;
		this.imageHeight = 186;
		this.playerInv = inv;
		this.te = screenContainer.getTe();
		// Sync local pattern from TE so reopening the screen preserves grid state
		if (te.scarsList != null) {
			for (int i = 0; i < te.scarsList.length && i < pattern.length; i++) {
				pattern[i] = te.scarsList[i].clone();
			}
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(graphics, mouseX, mouseY, partialTicks);
		super.render(graphics, mouseX, mouseY, partialTicks);
		// Render glowing borders: red for confirmed, purple for preview
		ScarButtonGlowRenderer.renderGlowGrid(graphics, scarbuttonArray, pattern, preview);

		// Render binder pattern selector panel if a ScarBinder is in the pattern slot
		if (binderPanelVisible && !binderEntries.isEmpty()) {
			renderBinderPanel(graphics, mouseX, mouseY, partialTicks);
		}

		this.renderTooltip(graphics, mouseX, mouseY);

		List<Component> cat1 = new ArrayList<Component>();
		cat1.add(Component.literal("Clear Scars"));
		if (clearButton.isHovered()) {
			graphics.renderComponentTooltip(font, cat1, mouseX, mouseY);
		}
		List<Component> cat9 = new ArrayList<Component>();
		cat9.add(Component.literal("Cerebral Scar"));
		if (ScarButton.isHovered()) {
			graphics.renderComponentTooltip(font, cat9, mouseX, mouseY);
		}
		List<Component> cat10 = new ArrayList<Component>();
		cat10.add(Component.literal("Load Pattern"));
		if (loadPatternButton.isHovered()) {
			graphics.renderComponentTooltip(font, cat10, mouseX, mouseY);
		}

	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.renderBackground(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	protected void containerTick() {
		super.containerTick();
		syncPatternSlotState();
	}

	private void syncPatternSlotState() {
		// Auto-detect when a pattern item is placed/removed in slot 4
		ItemStack currentPatternSlot = te.getItem(4);
		if (!ItemStack.isSameItemSameComponents(currentPatternSlot, lastPatternSlotItem)) {
			lastPatternSlotItem = currentPatternSlot.copy();
			if (currentPatternSlot.getItem() instanceof ItemScarBinder) {
				// A binder was placed — rebuild the pattern selector panel
				rebuildBinderEntries(currentPatternSlot);
				binderPanelVisible = true;
				binderSelectedEntry = -1;
			} else if (currentPatternSlot.getItem() instanceof ItemScarPattern) {
				// A single pattern was placed — hide binder panel, load it
				binderPanelVisible = false;
				binderEntries.clear();
				ItemScarPattern scarPattern = (ItemScarPattern) currentPatternSlot.getItem();
				ScarRecipe patternRecipe = scarPattern.getRecipe();
				if (patternRecipe != null && patternRecipe.getPattern() != null) {
					loadPatternIntoGrid(patternRecipe.getPattern());
				}
			} else {
				// Slot was emptied — hide binder panel, clear the grid
				binderPanelVisible = false;
				binderEntries.clear();
				binderSelectedEntry = -1;
				pattern = ScarRecipe.blank();
				preview = ScarRecipe.blank();
				refreshButtonsFromPattern();
				PacketHandler.sendToServer(new PacketUpdateScarPattern(pattern));
			}
		}
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int x, int y) {
		// Title with blood-red color, centered with drop shadow
		String titleText = "Scar Station";
		graphics.drawString(font, titleText,
				(this.imageWidth - font.width(titleText)) / 2, 4, 0xFFAA2222, true);

		// Inventory label
		graphics.drawString(font, this.playerInv.getDisplayName(), 8, this.imageHeight - 92,
				0xFF444444, false);

		if (te.hasValidRecipe()) {
			ScarRecipe currentRecipe = te.getCurrentRecipe();

			// Result item name (truncated, themed color)
			String resultName = I18n.get(currentRecipe.getResultItem().getDescriptionId());
			int maxNameWidth = 50;
			if (font.width(resultName) > maxNameWidth) {
				while (font.width(resultName + "...") > maxNameWidth && !resultName.isEmpty()) {
					resultName = resultName.substring(0, resultName.length() - 1);
				}
				resultName = resultName + "...";
			}
			graphics.drawString(font, resultName, 120, 65, 0xFFCCAAAA, true);

			// Render result item preview
			graphics.renderItem(currentRecipe.getResultItem(), 145, 44);

			// ── Programmatic status indicators ──
			int statusX = 120;
			int statusY = 77;

			// Scar Pattern match indicator
			if (te.areScarsMatching()) {
				renderCheckIcon(graphics, statusX, statusY, 0xFF44CC44);
			} else {
				renderXIcon(graphics, statusX, statusY, 0xFFCC4444);
			}

			// Knapper/tool indicator
			if (te.getItem(3).getItem() instanceof ItemKnapper) {
				renderCheckIcon(graphics, statusX + 16, statusY, 0xFF44CC44);
			} else {
				renderXIcon(graphics, statusX + 16, statusY, 0xFFCC4444);
			}
		}
	}

	/** Renders a small check mark icon at the given position. */
	private void renderCheckIcon(GuiGraphics gfx, int x, int y, int color) {
		// Background circle/box
		gfx.fill(x, y, x + 14, y + 14, 0x40000000);
		gfx.fill(x, y, x + 14, y + 1, 0x60000000);
		gfx.fill(x, y + 13, x + 14, y + 14, 0x60000000);
		gfx.fill(x, y, x + 1, y + 14, 0x60000000);
		gfx.fill(x + 13, y, x + 14, y + 14, 0x60000000);
		// Checkmark shape
		gfx.fill(x + 3, y + 7, x + 4, y + 10, color);
		gfx.fill(x + 4, y + 8, x + 5, y + 11, color);
		gfx.fill(x + 5, y + 9, x + 6, y + 11, color);
		gfx.fill(x + 6, y + 8, x + 7, y + 10, color);
		gfx.fill(x + 7, y + 7, x + 8, y + 9, color);
		gfx.fill(x + 8, y + 6, x + 9, y + 8, color);
		gfx.fill(x + 9, y + 5, x + 10, y + 7, color);
		gfx.fill(x + 10, y + 4, x + 11, y + 6, color);
	}

	/** Renders a small X icon at the given position. */
	private void renderXIcon(GuiGraphics gfx, int x, int y, int color) {
		// Background
		gfx.fill(x, y, x + 14, y + 14, 0x40000000);
		gfx.fill(x, y, x + 14, y + 1, 0x60000000);
		gfx.fill(x, y + 13, x + 14, y + 14, 0x60000000);
		gfx.fill(x, y, x + 1, y + 14, 0x60000000);
		gfx.fill(x + 13, y, x + 14, y + 14, 0x60000000);
		// X shape
		for (int i = 0; i < 8; i++) {
			gfx.fill(x + 3 + i, y + 3 + i, x + 4 + i, y + 4 + i, color);
			gfx.fill(x + 10 - i, y + 3 + i, x + 11 - i, y + 4 + i, color);
		}
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {

		int gx = this.leftPos;
		int gy = this.topPos;
		int gw = this.imageWidth;
		int gh = this.imageHeight;

		// ── Upper crafting area: animated vein background ──
		renderVeinBackground(graphics, gx, gy, gw, CRAFT_AREA_HEIGHT);
		drawBorder(graphics, gx, gy, gw, CRAFT_AREA_HEIGHT);

		// ── Lower inventory area: red gradient background ──
		int invTop = gy + CRAFT_AREA_HEIGHT;
		int invH = gh - CRAFT_AREA_HEIGHT;
		renderRedGradientBackground(graphics, gx, invTop, gw, invH);

		// Separator border between crafting and inventory areas
		graphics.fill(gx, invTop, gx + gw, invTop + 1, BORDER_OUTER);
		graphics.fill(gx, invTop + 1, gx + gw, invTop + 2, BORDER_INNER);

		// ── scar grid recessed area ──
		int gridX = gx + 49;
		int gridY = gy + 25;
		int gridW = 66;
		int gridH = 66;
		// Recessed border around the scar grid area
		graphics.fill(gridX - 2, gridY - 2, gridX + gridW + 2, gridY + gridH + 2, BORDER_OUTER);
		graphics.fill(gridX - 1, gridY - 1, gridX + gridW + 1, gridY + gridH + 1, BORDER_INNER);
		graphics.fill(gridX, gridY, gridX + gridW, gridY + gridH, 0xFF080204);

		// ── Draw slot backgrounds ──
		for (Slot slot : this.menu.slots) {
			int sx = gx + slot.x;
			int sy = gy + slot.y;
			drawSlotBackground(graphics, sx, sy, slot);
		}
	}

	// ───── Slot background rendering ─────

	private void drawSlotBackground(GuiGraphics gfx, int sx, int sy, Slot slot) {
		// Outer border
		gfx.fill(sx - 1, sy - 1, sx + 17, sy + 17, SLOT_BORDER_DARK);
		// Inner fill
		gfx.fill(sx, sy, sx + 16, sy + 16, SLOT_BG);
		// Bottom/right highlight
		gfx.fill(sx + 16, sy, sx + 17, sy + 17, SLOT_BORDER_LIGHT);
		gfx.fill(sx, sy + 16, sx + 17, sy + 17, SLOT_BORDER_LIGHT);

		// Themed tints per slot type
		if (slot instanceof com.vincenthuto.hemomancy.common.menu.slot.ScarSlot) {
			// Chisel tool slot: crimson tint
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x18FF3333);
		} else if (slot instanceof com.vincenthuto.hemomancy.common.menu.slot.ScarPatternSlot) {
			// Pattern slot: purple tint
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x18AA44DD);
		} else if (slot instanceof com.vincenthuto.hemomancy.common.menu.slot.OutputSlot) {
			// Output slot: bright red tint
			gfx.fill(sx, sy, sx + 16, sy + 16, 0x20FF4444);
		}
	}

	// ───── Programmatic border ─────

	private void drawBorder(GuiGraphics gfx, int x, int y, int w, int h) {
		gfx.fill(x, y, x + w, y + 1, BORDER_OUTER);
		gfx.fill(x, y + h - 1, x + w, y + h, BORDER_OUTER);
		gfx.fill(x, y, x + 1, y + h, BORDER_OUTER);
		gfx.fill(x + w - 1, y, x + w, y + h, BORDER_OUTER);

		gfx.fill(x + 1, y + 1, x + w - 1, y + 2, BORDER_INNER);
		gfx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, BORDER_INNER);
		gfx.fill(x + 1, y + 1, x + 2, y + h - 1, BORDER_INNER);
		gfx.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, BORDER_INNER);
	}

	// ───── Red gradient inventory background ─────

	private void renderRedGradientBackground(GuiGraphics gfx, int x, int y, int w, int h) {
		for (int row = 0; row < h; row++) {
			float t = (float) row / h;
			int r = (int) (60 + 100 * t);
			int g = (int) (10 + 40 * t);
			int b = (int) (10 + 30 * t);
			int color = (0xFF << 24) | (r << 16) | (g << 8) | b;
			gfx.fill(x, y + row, x + w, y + row + 1, color);
		}
	}

	// ───── Procedural animated vein background ─────

	private void renderVeinBackground(GuiGraphics graphics, int gx, int gy, int gw, int gh) {
		graphics.enableScissor(gx, gy, gx + gw, gy + gh);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		// Layer 1: solid near-black base
		graphics.fill(gx, gy, gx + gw, gy + gh, 0xFF0A0204);

		// Layer 2: subtle dark-red radial glow in the center
		int cx = gx + gw / 2;
		int cy = gy + gh / 2;
		int glowRadius = Math.max(gw, gh) / 2;
		for (int ring = glowRadius; ring > 0; ring -= 4) {
			float t = (float) ring / glowRadius;
			int alpha = (int) (30 * (1f - t));
			int red = (int) (35 * (1f - t));
			int color = (alpha << 24) | (red << 16);
			graphics.fill(cx - ring, cy - ring, cx + ring, cy + ring, color);
		}

		// Layer 3: animated vein tendrils
		float time = System.nanoTime() / 1_000_000_000f;
		if (veinParams != null) {
			for (int i = 0; i < VEIN_COUNT; i++) {
				drawVeinTendril(graphics, i, time, gx, gy, gw, gh);
			}
		}

		// Layer 4: pre-generated noise speckles
		if (speckleData != null) {
			for (int[] speck : speckleData) {
				int sx = gx + speck[0] % gw;
				int sy = gy + speck[1] % gh;
				int sr = speck[2];
				int sg = sr / 4;
				int sa = speck[3];
				graphics.fill(sx, sy, sx + 1, sy + 1, (sa << 24) | (sr << 16) | (sg << 8));
			}
		}

		RenderSystem.disableBlend();
		graphics.disableScissor();
	}

	private void drawVeinTendril(GuiGraphics graphics, int index, float time,
			int gx, int gy, int gw, int gh) {
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

		float angleDrift = baseAngle + 0.15f * Mth.sin(time * speed * 0.3f + index);
		float cosA = Mth.cos(angleDrift);
		float sinA = Mth.sin(angleDrift);

		float timeOffset = time * speed * 2.0f;

		int baseRed = (int) (40 + 50 * brightness);
		int baseGreen = (int) (2 + 8 * brightness);
		int baseBlue = (int) (5 + 5 * brightness);

		for (int step = 0; step < length; step++) {
			float squiggle = amplitude * Mth.sin(frequency * step + timeOffset);
			float microSquiggle = (amplitude * 0.3f) * Mth.sin(frequency * 2.7f * step + timeOffset * 1.4f + index);
			float displacement = squiggle + microSquiggle;

			float px = startX + step * cosA * 1.5f - displacement * sinA;
			float py = startY + step * sinA * 1.5f + displacement * cosA;

			int ix = (int) px;
			int iy = (int) py;

			if (ix + thickness < gx || ix >= gx + gw || iy + thickness < gy || iy >= gy + gh) continue;

			float tipFade = 1f;
			if (step < 10) tipFade = step / 10f;
			else if (step > length - 10) tipFade = (length - step) / 10f;

			float pulse = 0.7f + 0.3f * Mth.sin(time * 1.5f + index * 0.5f + step * 0.02f);

			int a = (int) (Mth.clamp(tipFade * pulse * 180, 20, 200));
			int r = (int) Mth.clamp(baseRed * pulse, 0, 255);
			int g = (int) Mth.clamp(baseGreen * pulse * 0.5f, 0, 255);
			int b = (int) Mth.clamp(baseBlue * pulse * 0.3f, 0, 255);

			graphics.fill(ix, iy, ix + thickness, iy + thickness,
					(a << 24) | (r << 16) | (g << 8) | b);
		}
	}

	/**
	 * Loads a pattern as a preview overlay (purple glow).
	 * The player must click each scar to confirm it (turns red).
	 */
	private void loadPatternIntoGrid(byte[][] sourcePattern) {
		// Clear any existing confirmed pattern
		pattern = ScarRecipe.blank();
		refreshButtonsFromPattern();
		PacketHandler.sendToServer(new PacketUpdateScarPattern(pattern));
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
				scarbuttonArray[i][j].setState(pattern[i][j] != 0);
			}
		}
	}

	// ── Binder pattern selector panel ───────────────────────────────────

	/**
	 * Scans the ScarBinder's inventory and populates binderEntries with
	 * all valid pattern items found inside.
	 */
	private void rebuildBinderEntries(ItemStack binderStack) {
		binderEntries.clear();
		binderScrollOffset = 0;
		binderHoveredEntry = -1;

		if (binderStack.isEmpty() || !(binderStack.getItem() instanceof ItemScarBinder)) return;

		IItemHandler handler = binderStack.getCapability(Capabilities.ItemHandler.ITEM);
		if (!(handler instanceof ScarBinderItemHandler rbHandler)) return;

		rbHandler.load();

		for (int i = 0; i < rbHandler.getSlots(); i++) {
			ItemStack slotStack = rbHandler.getStackInSlot(i);
			if (slotStack.getItem() instanceof ItemScarPattern pat) {
				ScarRecipe recipe = pat.getRecipe();
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
		//graphics.fill(x - 1, y - 1, x + gridSize + 1, y + gridSize + 1, 0x60000000);

	//	int activeColor = 0xFFDAA520; // solid gold — matches binder panel theme

//		for (int i = 0; i < 8; i++) {
//			for (int j = 0; j < 8; j++) {
//				int cx = x + j * MINI_CELL;
//				int cy = y + i * MINI_CELL;
//				if (patternData[i][j] != 0) {
//					graphics.fill(cx, cy, cx + MINI_CELL, cy + MINI_CELL, activeColor);
//				} else {
//					graphics.fill(cx, cy, cx + MINI_CELL, cy + MINI_CELL, 0x30333333);
//				}
//			}
//		}
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

		// Seed vein parameters for the animated background
		Random rand = new Random(42L);
		veinParams = new float[VEIN_COUNT][9];
		for (int i = 0; i < VEIN_COUNT; i++) {
			veinParams[i][0] = rand.nextFloat();
			veinParams[i][1] = rand.nextFloat();
			veinParams[i][2] = (float) (rand.nextFloat() * Math.PI * 2);
			veinParams[i][3] = 0.3f + rand.nextFloat() * 0.7f;
			veinParams[i][4] = 8f + rand.nextFloat() * 14f;
			veinParams[i][5] = 0.04f + rand.nextFloat() * 0.08f;
			veinParams[i][6] = 50 + rand.nextInt(100);
			veinParams[i][7] = 1 + rand.nextInt(2);
			veinParams[i][8] = rand.nextFloat();
		}

		// Pre-generate speckle positions and colors for the vein background
		Random speckRand = new Random(54321L);
		speckleData = new int[50][4]; // [index][x_offset, y_offset, red, alpha]
		for (int s = 0; s < 50; s++) {
			speckleData[s][0] = speckRand.nextInt(176); // max gui width
			speckleData[s][1] = speckRand.nextInt(CRAFT_AREA_HEIGHT);
			speckleData[s][2] = 10 + speckRand.nextInt(20); // red component
			speckleData[s][3] = 15 + speckRand.nextInt(20); // alpha
		}

		// scar grid buttons (8x8)
		int inc = 0;
		for (int i = 0; i < scarbuttonArray.length; i++) {
			for (int j = 0; j < scarbuttonArray.length; j++) {
				this.addRenderableWidget(scarbuttonArray[i][j] = new ScarButton(GUI_Chisel, inc, i, j,
						left + guiWidth - (guiWidth - 50 - (j * 8)), top + guiHeight - (160 - (i * 8)), 8, 8, 176, 0,
						te.scarsList[i][j] != 0, (press) -> {
							// Handled by click-and-drag system in mouseClicked/mouseDragged/mouseReleased
						}));
				inc++;
			}
		}

		// Themed action buttons
		this.addRenderableWidget(clearButton = new ScarActionButton(
				left + 120, top + 16, 16, 16,
				ScarActionButton.IconType.CLEAR, (press) -> {
					pattern = ScarRecipe.blank();
					preview = ScarRecipe.blank();
					refreshButtonsFromPattern();
					PacketHandler.sendToServer(new PacketUpdateScarPattern(pattern));
				}));
		this.addRenderableWidget(ScarButton = new ScarActionButton(
				left + 120, top + 36, 16, 16,
				ScarActionButton.IconType.CHISEL, (press) -> {
					if (te.contents.get(3).getItem() != Items.AIR) {
						PacketHandler.sendToServer(new PacketScarCraftingEvent());
						pattern = ScarRecipe.blank();
						preview = ScarRecipe.blank();
						refreshButtonsFromPattern();
						PacketHandler.sendToServer(new PacketUpdateScarPattern(pattern));
					}
				}));
		this.addRenderableWidget(loadPatternButton = new ScarActionButton(
				left + 28, top + 80, 16, 16,
				ScarActionButton.IconType.LOAD, (press) -> {
					ItemStack patternStack = te.getItem(4);
					if (patternStack.getItem() instanceof ItemScarPattern scarPattern) {
						ScarRecipe patternRecipe = scarPattern.getRecipe();
						if (patternRecipe != null && patternRecipe.getPattern() != null) {
							loadPatternIntoGrid(patternRecipe.getPattern());
						}
					}
				}));

	}

	// --- Click-and-drag scar painting ---

	/**
	 * Finds the ScarButton under the given mouse coordinates and applies
	 * the current drag paint mode to it (if not already visited this drag).
	 */
	private void paintScarAt(double mouseX, double mouseY) {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				ScarButton button = scarbuttonArray[i][j];
				if (!dragVisited[i][j]
						&& mouseX >= button.getX() && mouseX < button.getX() + button.getWidth()
						&& mouseY >= button.getY() && mouseY < button.getY() + button.getHeight()) {
					dragVisited[i][j] = true;
					if (dragPaintOn) {
						// Activate scar
						button.setState(true);
						pattern[i][j] = 1;
					} else {
						// Deactivate scar
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

			// ── scar button drag painting ───────────────────────────────
			// Check if click landed on any scar button
			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					ScarButton cb = scarbuttonArray[i][j];
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
						paintScarAt(mouseX, mouseY);
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
			paintScarAt(mouseX, mouseY);
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
			PacketHandler.sendToServer(new PacketUpdateScarPattern(pattern));
			return true;
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		// Scroll the binder panel if mouse is over it
		if (binderPanelVisible && !binderEntries.isEmpty()) {
			int px = getPanelX();
			int py = getPanelY();
			int panelHeight = getPanelListHeight() + 16 + PANEL_PADDING * 2;
			if (mouseX >= px && mouseX < px + PANEL_WIDTH
					&& mouseY >= py && mouseY < py + panelHeight) {
				int maxScroll = Math.max(0, binderEntries.size() - PANEL_VISIBLE_ENTRIES);
				binderScrollOffset = Mth.clamp(binderScrollOffset - (int) Math.signum(scrollY), 0, maxScroll);
				return true;
			}
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
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