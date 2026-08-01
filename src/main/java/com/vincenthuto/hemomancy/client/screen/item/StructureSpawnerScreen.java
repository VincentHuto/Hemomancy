package com.vincenthuto.hemomancy.client.screen.item;

import com.vincenthuto.hemomancy.common.menu.StructureSpawnerMenu;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.PlaceStructurePacket;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.RecipeDegreeGates;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StructureSpawnerScreen extends AbstractContainerScreen<StructureSpawnerMenu> {

	private static final int GUI_WIDTH = 256;
	private static final int GUI_HEIGHT = 200;
	private static final int LIST_X = 8;
	private static final int LIST_Y = 40;
	private static final int LIST_WIDTH = 240;
	private static final int LIST_HEIGHT = 150;
	private static final int TAB_X = 8;
	private static final int TAB_Y = 20;
	private static final int TAB_WIDTH = 116;
	private static final int TAB_HEIGHT = 16;
	private static final int TAB_GAP = 8;
	private static final int ENTRY_HEIGHT = 20;
	private static final int VISIBLE_ENTRIES = LIST_HEIGHT / ENTRY_HEIGHT;
	private static final int BORDER_OUTER = 0xFF880000;
	private static final int BORDER_INNER = 0xFF3A0808;
	private static final int OPTION_BORDER = 0xFF7A1414;
	private static final int OPTION_HOVER_BORDER = 0xFFB33838;
	private static final int SCREEN_DIM = 0x99000000;
	private static final int PANEL_BG = 0xF20A0204;
	private static final int PANEL_INNER_BG = 0xF214070A;
	private static final int TITLE_STRIP_BG = 0xEE180508;
	private static final int LIST_SHADOW_BG = 0xFF030102;
	private static final int LIST_PANEL_BG = 0xF5100508;
	private static final int LIST_PANEL_INNER_BG = 0xF018090D;
	private static final int HEADER_BG = 0xFF0D0305;
	private static final int HEADER_LINE = 0xFF660E0E;
	private static final int TAB_BG = 0xFF160407;
	private static final int TAB_SELECTED_BG = 0xFF2A080C;
	private static final int TAB_BORDER = 0xFF5A1010;
	private static final int TAB_SELECTED_BORDER = 0xFFB33838;
	private static final int UNSTAINED_BORDER_OUTER = 0xFF006E9A;
	private static final int UNSTAINED_BORDER_INNER = 0xFF073447;
	private static final int UNSTAINED_OPTION_BORDER = 0xFF0E6688;
	private static final int UNSTAINED_OPTION_HOVER_BORDER = 0xFF25B9E8;
	private static final int UNSTAINED_PANEL_BG = 0xF2020910;
	private static final int UNSTAINED_PANEL_INNER_BG = 0xF2071622;
	private static final int UNSTAINED_TITLE_STRIP_BG = 0xEE051C2A;
	private static final int UNSTAINED_LIST_SHADOW_BG = 0xFF010407;
	private static final int UNSTAINED_LIST_PANEL_BG = 0xF5051722;
	private static final int UNSTAINED_LIST_PANEL_INNER_BG = 0xF0081F2E;
	private static final int UNSTAINED_HEADER_BG = 0xFF03111A;
	private static final int UNSTAINED_HEADER_LINE = 0xFF0E6A9B;
	private static final int UNSTAINED_TAB_BG = 0xFF071821;
	private static final int UNSTAINED_TAB_SELECTED_BG = 0xFF0A2B3D;
	private static final int UNSTAINED_TAB_BORDER = 0xFF0E5F82;
	private static final int UNSTAINED_TAB_SELECTED_BORDER = 0xFF25B9E8;
	private static final int UNSTAINED_SCROLL_THUMB = 0xFF18A6D8;
	private static final int UNSTAINED_TITLE_TEXT = 0xFF66D9FF;
	private static final int UNSTAINED_HEADER_TEXT = 0xFF55CFFF;

	// Blood structure tier thresholds (must match BloodCraftingKeyPressPacket)

	private static final int HEADER_HEIGHT = 14;

	private final List<StructureEntry> entries = new ArrayList<>();
	/** Flat display list: mix of section headers (null recipeId) and clickable entries. */
	private final List<ListRow> displayRows = new ArrayList<>();
	private int scrollOffset = 0;
	private List<Button> entryButtons = new ArrayList<>();
	private final List<Button> tabButtons = new ArrayList<>();
	private PathTab selectedTab = PathTab.HARBINGER;

	public StructureSpawnerScreen(StructureSpawnerMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
		this.imageWidth = GUI_WIDTH;
		this.imageHeight = GUI_HEIGHT;
	}

	@Override
	protected void init() {
		super.init();
		entries.clear();

		// Gather all blood structure recipes
		if (Minecraft.getInstance().level != null) {
			List<BloodStructureRecipe> structures = BloodStructureRecipe.getAllRecipes(Minecraft.getInstance().level);
			for (BloodStructureRecipe recipe : structures) {
				String name = formatName(recipe.getId().getPath());
				entries.add(new StructureEntry(
						recipe.getId(),
						name,
						createEntryLabel(PlaceStructurePacket.StructureType.BLOOD_STRUCTURE, name),
						PlaceStructurePacket.StructureType.BLOOD_STRUCTURE,
						RecipeDegreeGates.getRequiredDegree(recipe),
						recipe.isUnstained()
				));
			}

			// Gather all cardinal rite recipes
			List<CardinalRiteRecipe> rites = CardinalRiteRecipe.getAllRecipes(Minecraft.getInstance().level);
			for (CardinalRiteRecipe recipe : rites) {
				String name = recipe.getRiteName() != null && !recipe.getRiteName().isEmpty()
						? recipe.getRiteName()
						: formatName(recipe.getId().getPath());
				entries.add(new StructureEntry(
						recipe.getId(),
						name,
						createEntryLabel(PlaceStructurePacket.StructureType.CARDINAL_RITE, name),
						PlaceStructurePacket.StructureType.CARDINAL_RITE,
						RecipeDegreeGates.getRequiredDegree(recipe),
						recipe.isUnstained()
				));
			}
		}

		entries.sort(Comparator.comparingInt(StructureEntry::sortOrder).thenComparing(StructureEntry::displayName));
		buildDisplayRows();
		scrollOffset = 0;
		rebuildTabButtons();
		rebuildButtons();
	}

	private void buildDisplayRows() {
		// Build flat display list with section headers
		displayRows.clear();
		int lastDegree = -1;
		for (StructureEntry entry : entries) {
			if (entry.unstained() != selectedTab.unstained) continue;
			if (entry.sortOrder() != lastDegree) {
				lastDegree = entry.sortOrder();
				displayRows.add(new ListRow(null, getSectionSortLabel(lastDegree), getSectionLabel(lastDegree), null, lastDegree, true));
			}
			displayRows.add(new ListRow(entry.recipeId(), entry.displayName(), entry.displayComponent(), entry.type(), entry.sortOrder(), false));
		}
	}

	private String getSectionSortLabel(int level) {
		if (selectedTab == PathTab.UNSTAINED) {
			return "-- Stage " + level + " - " + RecipeDegreeGates.unstainedStageLabel(level) + " --";
		}
		return "-- " + RecipeDegreeGates.degreeLabel(level) + " --";
	}

	private Component getSectionLabel(int level) {
		return Component.literal(getSectionSortLabel(level)).withStyle(sectionTextStyle());
	}

	private static Component createEntryLabel(PlaceStructurePacket.StructureType type, String name) {
		ChatFormatting prefixColor = switch (type) {
			case BLOOD_STRUCTURE -> ChatFormatting.AQUA;
			case CARDINAL_RITE -> ChatFormatting.LIGHT_PURPLE;
		};
		String prefix = switch (type) {
			case BLOOD_STRUCTURE -> "[Structure] ";
			case CARDINAL_RITE -> "[Rite] ";
		};
		return Component.literal(prefix).withStyle(prefixColor)
				.append(Component.literal(name).withStyle(ChatFormatting.WHITE));
	}

	private Component createTabLabel(PathTab tab) {
		ChatFormatting color = tab == selectedTab
				? (tab == PathTab.UNSTAINED ? ChatFormatting.AQUA : ChatFormatting.GOLD)
				: ChatFormatting.GRAY;
		String prefix = tab == selectedTab ? "> " : "";
		return Component.literal(prefix + tab.label).withStyle(color, ChatFormatting.BOLD);
	}

	private void selectTab(PathTab tab) {
		if (selectedTab == tab) return;
		selectedTab = tab;
		scrollOffset = 0;
		buildDisplayRows();
		rebuildTabButtons();
		rebuildButtons();
	}

	private void rebuildTabButtons() {
		for (Button btn : tabButtons) {
			removeWidget(btn);
		}
		tabButtons.clear();

		int x = leftPos + TAB_X;
		int y = topPos + TAB_Y;
		addTabButton(PathTab.HARBINGER, x, y);
		addTabButton(PathTab.UNSTAINED, x + TAB_WIDTH + TAB_GAP, y);
	}

	private void addTabButton(PathTab tab, int x, int y) {
		Button btn = Button.builder(createTabLabel(tab), (press) -> selectTab(tab))
				.bounds(x, y, TAB_WIDTH, TAB_HEIGHT)
				.build();
		tabButtons.add(btn);
		addRenderableWidget(btn);
	}

	private void rebuildButtons() {
		// Remove old buttons
		for (Button btn : entryButtons) {
			removeWidget(btn);
		}
		entryButtons.clear();

		int x = leftPos + LIST_X;
		int startY = topPos + LIST_Y;
		int currentY = 0;

		for (int i = scrollOffset; i < displayRows.size(); i++) {
			if (currentY + ENTRY_HEIGHT > LIST_HEIGHT) break;
			ListRow row = displayRows.get(i);

			if (row.isHeader()) {
				// Headers consume HEADER_HEIGHT, no button needed (drawn in renderBg)
				currentY += HEADER_HEIGHT;
			} else {
				if (currentY + ENTRY_HEIGHT > LIST_HEIGHT) break;
				final ListRow entryRow = row;
				Button btn = Button.builder(entryRow.displayComponent(), (press) -> {
					PacketHandler.sendToServer(
							new PlaceStructurePacket(entryRow.recipeId(), entryRow.type())
					);
					onClose();
				}).bounds(x, startY + currentY, LIST_WIDTH, ENTRY_HEIGHT - 2).build();

				entryButtons.add(btn);
				addRenderableWidget(btn);
				currentY += ENTRY_HEIGHT;
			}
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		int maxScroll = Math.max(0, displayRows.size() - 1);
		scrollOffset = (int) Math.max(0, Math.min(scrollOffset - scrollY, maxScroll));
		rebuildButtons();
		return true;
	}

	private void renderPanelBackground(GuiGraphics graphics) {
		// Main panel background. This screen deliberately avoids the vanilla blur effect,
		// so it needs to draw its own opaque backdrop behind the buttons.
		graphics.fill(leftPos, topPos, leftPos + GUI_WIDTH, topPos + GUI_HEIGHT, panelBg());
		graphics.fill(leftPos + 3, topPos + 3, leftPos + GUI_WIDTH - 3, topPos + GUI_HEIGHT - 3, panelInnerBg());
		graphics.fill(leftPos + 4, topPos + 4, leftPos + GUI_WIDTH - 4, topPos + 17, titleStripBg());
		graphics.fill(leftPos + 4, topPos + 17, leftPos + GUI_WIDTH - 4, topPos + 18, headerLine());
		renderTabBackground(graphics, PathTab.HARBINGER, leftPos + TAB_X, topPos + TAB_Y);
		renderTabBackground(graphics, PathTab.UNSTAINED, leftPos + TAB_X + TAB_WIDTH + TAB_GAP, topPos + TAB_Y);

		// List well background
		graphics.fill(leftPos + LIST_X - 2, topPos + LIST_Y - 2,
				leftPos + LIST_X + LIST_WIDTH + 2, topPos + LIST_Y + LIST_HEIGHT + 2, listShadowBg());
		graphics.fill(leftPos + LIST_X - 1, topPos + LIST_Y - 1,
				leftPos + LIST_X + LIST_WIDTH + 1, topPos + LIST_Y + LIST_HEIGHT + 1, listPanelBg());
		graphics.fill(leftPos + LIST_X, topPos + LIST_Y,
				leftPos + LIST_X + LIST_WIDTH, topPos + LIST_Y + LIST_HEIGHT, listPanelInnerBg());

		// Draw section headers
		int startY = topPos + LIST_Y;
		int currentY = 0;
		for (int i = scrollOffset; i < displayRows.size(); i++) {
			ListRow row = displayRows.get(i);
			if (row.isHeader()) {
				if (currentY + HEADER_HEIGHT > LIST_HEIGHT) break;
				int headerY = startY + currentY;
				// Dark background strip
				graphics.fill(leftPos + LIST_X, headerY, leftPos + LIST_X + LIST_WIDTH, headerY + HEADER_HEIGHT, headerBg());
				// Decorative line across the top of the header
				graphics.fill(leftPos + LIST_X, headerY, leftPos + LIST_X + LIST_WIDTH, headerY + 1, headerLine());
				// Draw the section label centered
				Component label = row.displayComponent();
				int labelWidth = font.width(label);
				int labelX = leftPos + LIST_X + (LIST_WIDTH - labelWidth) / 2;
				graphics.drawString(font, label, labelX, headerY + 3, headerTextColor(), false);
				currentY += HEADER_HEIGHT;
			} else {
				if (currentY + ENTRY_HEIGHT > LIST_HEIGHT) break;
				currentY += ENTRY_HEIGHT;
			}
		}

		// Draw scrollbar if needed
		if (displayRows.size() > VISIBLE_ENTRIES) {
			int scrollbarX = leftPos + GUI_WIDTH - 6;
			int scrollbarTop = topPos + LIST_Y;
			int scrollbarHeight = LIST_HEIGHT;
			// Track
			graphics.fill(scrollbarX, scrollbarTop, scrollbarX + 4, scrollbarTop + scrollbarHeight, 0xFF333333);

			// Thumb
			int maxScroll = Math.max(1, displayRows.size() - 1);
			int thumbHeight = Math.max(10, scrollbarHeight * VISIBLE_ENTRIES / displayRows.size());
			int thumbY = scrollbarTop + (scrollbarHeight - thumbHeight) * scrollOffset / maxScroll;
			graphics.fill(scrollbarX, thumbY, scrollbarX + 4, thumbY + thumbHeight, scrollThumbColor());
		}
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		// Drawn from renderBackground so the custom backdrop appears even when
		// AbstractContainerScreen's usual background hook is bypassed by the no-blur override.
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.drawString(this.font, Component.literal("Structure Spawner").withStyle(sectionTextStyle()), 8, 6, titleTextColor(), false);

		// Entry count info
		String info = getVisibleEntryCount() + " " + selectedTab.label.toLowerCase() + " entries";
		graphics.drawString(this.font, info, GUI_WIDTH - font.width(info) - 8, 6, 0x888888, false);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		// Do NOT call renderBackground() - it applies blur. This is not a pause screen.
		super.render(graphics, mouseX, mouseY, partialTick);
		renderBorders(graphics, mouseX, mouseY);
		this.renderTooltip(graphics, mouseX, mouseY);
	}

	/** Suppress the 1.21.1 menu_blur post-effect while still dimming the world behind this menu. */
	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(0, 0, this.width, this.height, SCREEN_DIM);
		renderPanelBackground(graphics);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void renderBorders(GuiGraphics graphics, int mouseX, int mouseY) {
		drawBorder(graphics, leftPos, topPos, GUI_WIDTH, GUI_HEIGHT, borderOuter(), borderInner());
		drawSingleBorder(graphics, leftPos + TAB_X, topPos + TAB_Y, TAB_WIDTH, TAB_HEIGHT,
				tabBorder(PathTab.HARBINGER));
		drawSingleBorder(graphics, leftPos + TAB_X + TAB_WIDTH + TAB_GAP, topPos + TAB_Y, TAB_WIDTH, TAB_HEIGHT,
				tabBorder(PathTab.UNSTAINED));
		drawBorder(graphics, leftPos + LIST_X - 2, topPos + LIST_Y - 2,
				LIST_WIDTH + 4, LIST_HEIGHT + 4, borderOuter(), borderInner());

		int startY = topPos + LIST_Y;
		int currentY = 0;
		for (int i = scrollOffset; i < displayRows.size(); i++) {
			if (currentY + ENTRY_HEIGHT > LIST_HEIGHT) break;
			ListRow row = displayRows.get(i);

			if (row.isHeader()) {
				currentY += HEADER_HEIGHT;
				continue;
			}

			int rowX = leftPos + LIST_X;
			int rowY = startY + currentY;
			int rowHeight = ENTRY_HEIGHT - 2;
			boolean hovered = mouseX >= rowX && mouseX < rowX + LIST_WIDTH
					&& mouseY >= rowY && mouseY < rowY + rowHeight;
			drawSingleBorder(graphics, rowX, rowY, LIST_WIDTH, rowHeight,
					hovered ? optionHoverBorder() : optionBorder());
			currentY += ENTRY_HEIGHT;
		}
	}

	private static void drawBorder(GuiGraphics graphics, int x, int y, int width, int height, int outer, int inner) {
		drawSingleBorder(graphics, x, y, width, height, outer);
		drawSingleBorder(graphics, x + 1, y + 1, width - 2, height - 2, inner);
	}

	private static void drawSingleBorder(GuiGraphics graphics, int x, int y, int width, int height, int color) {
		graphics.fill(x, y, x + width, y + 1, color);
		graphics.fill(x, y + height - 1, x + width, y + height, color);
		graphics.fill(x, y, x + 1, y + height, color);
		graphics.fill(x + width - 1, y, x + width, y + height, color);
	}

	private void renderTabBackground(GuiGraphics graphics, PathTab tab, int x, int y) {
		graphics.fill(x, y, x + TAB_WIDTH, y + TAB_HEIGHT,
				tabBackground(tab));
	}

	private boolean unstainedTheme() {
		return selectedTab == PathTab.UNSTAINED;
	}

	private int borderOuter() { return unstainedTheme() ? UNSTAINED_BORDER_OUTER : BORDER_OUTER; }
	private int borderInner() { return unstainedTheme() ? UNSTAINED_BORDER_INNER : BORDER_INNER; }
	private int optionBorder() { return unstainedTheme() ? UNSTAINED_OPTION_BORDER : OPTION_BORDER; }
	private int optionHoverBorder() { return unstainedTheme() ? UNSTAINED_OPTION_HOVER_BORDER : OPTION_HOVER_BORDER; }
	private int panelBg() { return unstainedTheme() ? UNSTAINED_PANEL_BG : PANEL_BG; }
	private int panelInnerBg() { return unstainedTheme() ? UNSTAINED_PANEL_INNER_BG : PANEL_INNER_BG; }
	private int titleStripBg() { return unstainedTheme() ? UNSTAINED_TITLE_STRIP_BG : TITLE_STRIP_BG; }
	private int listShadowBg() { return unstainedTheme() ? UNSTAINED_LIST_SHADOW_BG : LIST_SHADOW_BG; }
	private int listPanelBg() { return unstainedTheme() ? UNSTAINED_LIST_PANEL_BG : LIST_PANEL_BG; }
	private int listPanelInnerBg() { return unstainedTheme() ? UNSTAINED_LIST_PANEL_INNER_BG : LIST_PANEL_INNER_BG; }
	private int headerBg() { return unstainedTheme() ? UNSTAINED_HEADER_BG : HEADER_BG; }
	private int headerLine() { return unstainedTheme() ? UNSTAINED_HEADER_LINE : HEADER_LINE; }
	private int scrollThumbColor() { return unstainedTheme() ? UNSTAINED_SCROLL_THUMB : 0xFFAA0000; }
	private int titleTextColor() { return unstainedTheme() ? UNSTAINED_TITLE_TEXT : 0xFFAA3333; }
	private int headerTextColor() { return unstainedTheme() ? UNSTAINED_HEADER_TEXT : 0xFFAA3333; }
	private ChatFormatting sectionTextStyle() { return unstainedTheme() ? ChatFormatting.AQUA : ChatFormatting.DARK_RED; }

	private int tabBackground(PathTab tab) {
		if (tab == selectedTab) {
			return tab == PathTab.UNSTAINED ? UNSTAINED_TAB_SELECTED_BG : TAB_SELECTED_BG;
		}
		return tab == PathTab.UNSTAINED ? UNSTAINED_TAB_BG : TAB_BG;
	}

	private int tabBorder(PathTab tab) {
		if (tab == selectedTab) {
			return tab == PathTab.UNSTAINED ? UNSTAINED_TAB_SELECTED_BORDER : TAB_SELECTED_BORDER;
		}
		return tab == PathTab.UNSTAINED ? UNSTAINED_TAB_BORDER : TAB_BORDER;
	}

	private int getVisibleEntryCount() {
		int count = 0;
		for (StructureEntry entry : entries) {
			if (entry.unstained() == selectedTab.unstained) count++;
		}
		return count;
	}

	private static String formatName(String path) {
		// Convert something like "living_staff_recipe" to "Living Staff Recipe"
		String[] parts = path.replace('/', ' ').replace('_', ' ').split(" ");
		StringBuilder sb = new StringBuilder();
		for (String part : parts) {
			if (!part.isEmpty()) {
				if (sb.length() > 0) sb.append(' ');
				sb.append(Character.toUpperCase(part.charAt(0)));
				if (part.length() > 1) sb.append(part.substring(1));
			}
		}
		return sb.toString();
	}


	private enum PathTab {
		HARBINGER(false, "Harbingers"),
		UNSTAINED(true, "Unstained");

		private final boolean unstained;
		private final String label;

		PathTab(boolean unstained, String label) {
			this.unstained = unstained;
			this.label = label;
		}
	}

	private record StructureEntry(ResourceLocation recipeId, String displayName, Component displayComponent, PlaceStructurePacket.StructureType type, int sortOrder, boolean unstained) {}

	/** A row in the display list - either a section header or a clickable entry. */
	private record ListRow(ResourceLocation recipeId, String displayName, Component displayComponent, PlaceStructurePacket.StructureType type, int sortOrder, boolean isHeader) {}
}
