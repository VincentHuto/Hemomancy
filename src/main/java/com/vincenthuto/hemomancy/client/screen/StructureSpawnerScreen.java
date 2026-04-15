package com.vincenthuto.hemomancy.client.screen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.vincenthuto.hemomancy.common.menu.StructureSpawnerMenu;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.PlaceStructurePacket;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class StructureSpawnerScreen extends AbstractContainerScreen<StructureSpawnerMenu> {

	private static final int GUI_WIDTH = 256;
	private static final int GUI_HEIGHT = 200;
	private static final int LIST_X = 8;
	private static final int LIST_Y = 20;
	private static final int LIST_WIDTH = 240;
	private static final int LIST_HEIGHT = 170;
	private static final int ENTRY_HEIGHT = 20;
	private static final int VISIBLE_ENTRIES = LIST_HEIGHT / ENTRY_HEIGHT;

	// Blood structure tier thresholds (must match BloodCraftingKeyPressPacket)
	private static final int[] CRAFTING_TIER_THRESHOLDS = { 100, 200, Integer.MAX_VALUE };
	private static final int[] CRAFTING_TIER_DEGREE_REQ = { 0, 2, 4 };

	private static final int HEADER_HEIGHT = 14;

	private final List<StructureEntry> entries = new ArrayList<>();
	/** Flat display list: mix of section headers (null recipeId) and clickable entries. */
	private final List<ListRow> displayRows = new ArrayList<>();
	private int scrollOffset = 0;
	private List<Button> entryButtons = new ArrayList<>();

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
				entries.add(new StructureEntry(
						recipe.getId(),
						"§b[Structure] §r" + formatName(recipe.getId().getPath()),
						PlaceStructurePacket.StructureType.BLOOD_STRUCTURE,
						getRequiredDegreeForStructure(recipe)
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
						"§d[Rite] §r" + name,
						PlaceStructurePacket.StructureType.CARDINAL_RITE,
						getRequiredDegreeForRite(recipe.getRiteType())
				));
			}
		}

		entries.sort(Comparator.comparingInt(StructureEntry::sortOrder).thenComparing(StructureEntry::displayName));

		// Build flat display list with section headers
		displayRows.clear();
		int lastDegree = -1;
		for (StructureEntry entry : entries) {
			if (entry.sortOrder() != lastDegree) {
				lastDegree = entry.sortOrder();
				displayRows.add(new ListRow(null, getSectionLabel(lastDegree), null, lastDegree, true));
			}
			displayRows.add(new ListRow(entry.recipeId(), entry.displayName(), entry.type(), entry.sortOrder(), false));
		}

		scrollOffset = 0;
		rebuildButtons();
	}

	private static String getSectionLabel(int degree) {
		if (degree == 0) return "§4── Tier I (No Degree) ──";
		if (degree <= 1) return "§4── Tier II (Degree " + degree + ") ──";
		if (degree <= 2) return "§4── Tier III (Degree " + degree + ") ──";
		if (degree <= 3) return "§4── Tier IV (Degree " + degree + ") ──";
		if (degree <= 4) return "§4── Tier V (Degree " + degree + ") ──";
		return "§4── Tier VI (Degree " + degree + ") ──";
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
				Button btn = Button.builder(Component.literal(entryRow.displayName()), (press) -> {
					PacketHandler.CHANNELBLOODVOLUME.sendToServer(
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
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		int maxScroll = Math.max(0, displayRows.size() - 1);
		scrollOffset = (int) Math.max(0, Math.min(scrollOffset - delta, maxScroll));
		rebuildButtons();
		return true;
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		// Draw a dark background
		graphics.fill(leftPos, topPos, leftPos + GUI_WIDTH, topPos + GUI_HEIGHT, 0xCC101020);
		// Draw border
		graphics.fill(leftPos, topPos, leftPos + GUI_WIDTH, topPos + 1, 0xFF880000);
		graphics.fill(leftPos, topPos + GUI_HEIGHT - 1, leftPos + GUI_WIDTH, topPos + GUI_HEIGHT, 0xFF880000);
		graphics.fill(leftPos, topPos, leftPos + 1, topPos + GUI_HEIGHT, 0xFF880000);
		graphics.fill(leftPos + GUI_WIDTH - 1, topPos, leftPos + GUI_WIDTH, topPos + GUI_HEIGHT, 0xFF880000);

		// Draw section headers
		int startY = topPos + LIST_Y;
		int currentY = 0;
		for (int i = scrollOffset; i < displayRows.size(); i++) {
			ListRow row = displayRows.get(i);
			if (row.isHeader()) {
				if (currentY + HEADER_HEIGHT > LIST_HEIGHT) break;
				int headerY = startY + currentY;
				// Dark background strip
				graphics.fill(leftPos + LIST_X, headerY, leftPos + LIST_X + LIST_WIDTH, headerY + HEADER_HEIGHT, 0xFF0D0305);
				// Decorative line across the top of the header
				graphics.fill(leftPos + LIST_X, headerY, leftPos + LIST_X + LIST_WIDTH, headerY + 1, 0xFF660E0E);
				// Draw the section label centered
				String label = row.displayName();
				int labelWidth = font.width(label);
				int labelX = leftPos + LIST_X + (LIST_WIDTH - labelWidth) / 2;
				graphics.drawString(font, label, labelX, headerY + 3, 0xFFAA3333, false);
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
			graphics.fill(scrollbarX, thumbY, scrollbarX + 4, thumbY + thumbHeight, 0xFFAA0000);
		}
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.drawString(this.font, Component.literal("§4Structure Spawner"), 8, 6, 0xFFFFFF, false);

		// Entry count info
		String info = entries.size() + " structures available";
		graphics.drawString(this.font, info, GUI_WIDTH - font.width(info) - 8, 6, 0x888888, false);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, partialTick);
		this.renderTooltip(graphics, mouseX, mouseY);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
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

	private static int getRequiredDegreeForStructure(BloodStructureRecipe recipe) {
		double cost = recipe.getBloodCost();
		for (int i = 0; i < CRAFTING_TIER_THRESHOLDS.length; i++) {
			if (cost <= CRAFTING_TIER_THRESHOLDS[i]) {
				return CRAFTING_TIER_DEGREE_REQ[i];
			}
		}
		return CRAFTING_TIER_DEGREE_REQ[CRAFTING_TIER_DEGREE_REQ.length - 1];
	}

	private static int getRequiredDegreeForRite(CardinalRiteType type) {
		return switch (type) {
			case MINOR   -> 0;
			case LESSER  -> 1;
			case GREATER -> 3;
			case GRAND   -> 5;
		};
	}

	private record StructureEntry(ResourceLocation recipeId, String displayName, PlaceStructurePacket.StructureType type, int sortOrder) {}

	/** A row in the display list – either a section header or a clickable entry. */
	private record ListRow(ResourceLocation recipeId, String displayName, PlaceStructurePacket.StructureType type, int sortOrder, boolean isHeader) {}
}
