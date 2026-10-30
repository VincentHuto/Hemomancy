package com.vincenthuto.hemomancy.client.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hemomancy.common.init.CreativeTabInit;
import com.vincenthuto.hemomancy.common.init.CreativeTabSection;
import com.vincenthuto.hemomancy.mixin.core.CreativePickerMenuInvoker;
import com.vincenthuto.hemomancy.mixin.core.CreativeScreenAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;

/**
 * The main Hemomancy creative tab's look: a lacquered frame over the vanilla panel, and a banner row before each
 * {@link CreativeTabSection}. {@link #layout} pads the tab's item list so every section starts on a fresh row behind
 * an empty banner row (called from {@code MixinCreativeTabSections}); the frame draws behind the slots and the
 * banners draw over their empty rows.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class CreativeTabChrome {
	private static final int COLUMNS = 9;
	private static final int VISIBLE_ROWS = 5;
	private static final int GRID_X = 9;
	private static final int GRID_Y = 18;
	private static final int SLOT = 18;

	private static final int OUTLINE = 0xFF050102;
	private static final int IRON = 0xFF5E1E24;
	private static final int IRON_DARK = 0xFF2A0D10;
	private static final int IRON_LIGHT = 0xFF8A3A40;
	private static final int BONE = 0xFFC9B894;
	private static final int PANEL_TOP = 0xFF22090D;
	private static final int PANEL_BOTTOM = 0xFF110406;
	private static final int WELL_DARK = 0xFF0C0304;
	private static final int WELL = 0xFF2A1014;
	private static final int WELL_LIGHT = 0xFF4E242A;

	/** The section whose banner fills each row of the padded list, or null. Empty unless the Hemomancy tab is open. */
	private static CreativeTabSection[] bannerRows = new CreativeTabSection[0];

	private CreativeTabChrome() {
	}

	public static boolean isHemomancyTab(CreativeModeTab tab) {
		return tab == CreativeTabInit.hemomancytab.get();
	}

	/**
	 * Rewrites {@code items} in place: each section opens with a row of empty slots for its banner, and each run
	 * inside it (a Hematic group, a section's spawn eggs) starts on a fresh row. Any other tab just clears the
	 * banner rows.
	 */
	public static void layout(CreativeModeTab tab, List<ItemStack> items) {
		bannerRows = new CreativeTabSection[0];
		if (!isHemomancyTab(tab) || items.isEmpty()) {
			return;
		}
		List<ItemStack> padded = new ArrayList<>(items.size() + COLUMNS * 2 * CreativeTabSection.values().length);
		List<CreativeTabSection> rows = new ArrayList<>();
		CreativeTabSection current = null;
		int run = Integer.MIN_VALUE;
		for (ItemStack stack : items) {
			CreativeTabSection section = CreativeTabSection.of(stack);
			int stackRun = CreativeTabSection.run(stack);
			if (section == current && stackRun != run) {
				padRow(padded);
			}
			run = stackRun;
			if (section != current) {
				padRow(padded);
				while (rows.size() < padded.size() / COLUMNS) {
					rows.add(null);
				}
				rows.add(section);
				for (int i = 0; i < COLUMNS; i++) {
					padded.add(ItemStack.EMPTY);
				}
				current = section;
			}
			padded.add(stack);
		}
		padRow(padded);
		bannerRows = rows.toArray(CreativeTabSection[]::new);
		items.clear();
		items.addAll(padded);
	}

	private static void padRow(List<ItemStack> items) {
		while (items.size() % COLUMNS != 0) {
			items.add(ItemStack.EMPTY);
		}
	}

	/** Whether a grid slot (0-44) sits on a banner row at the current scroll. */
	public static boolean isBannerSlot(CreativeModeInventoryScreen screen, int slotId) {
		return slotId >= 0 && slotId < COLUMNS * VISIBLE_ROWS && bannerRows.length > 0
				&& bannerAt(firstRow(screen) + slotId / COLUMNS) != null;
	}

	private static CreativeTabSection bannerAt(int row) {
		return row >= 0 && row < bannerRows.length ? bannerRows[row] : null;
	}

	private static int firstRow(CreativeModeInventoryScreen screen) {
		float scroll = ((CreativeScreenAccessor) screen).hemomancy$scrollOffs();
		return ((CreativePickerMenuInvoker) screen.getMenu()).hemomancy$rowIndexForScroll(scroll);
	}

	private static boolean showing(Object screen) {
		return screen instanceof CreativeModeInventoryScreen
				&& isHemomancyTab(CreativeScreenAccessor.hemomancy$selectedTab());
	}

	@SubscribeEvent
	public static void onBackground(ContainerScreenEvent.Render.Background event) {
		if (!showing(event.getContainerScreen())) {
			return;
		}
		CreativeModeInventoryScreen screen = (CreativeModeInventoryScreen) event.getContainerScreen();
		GuiGraphics g = event.getGuiGraphics();
		int x = screen.getGuiLeft();
		int y = screen.getGuiTop();
		int w = screen.getXSize();
		int h = screen.getYSize();

		g.fill(x, y, x + w, y + h, OUTLINE);
		g.fillGradient(x + 1, y + 1, x + w - 1, y + h - 1, PANEL_TOP, PANEL_BOTTOM);
		ScreenDrawUtils.drawRingBorder(g, x, y, w, h, OUTLINE, IRON, IRON_DARK);
		ScreenDrawUtils.drawCornerAccents(g, x + 1, y + 1, w - 2, h - 2, 2, BONE);
		for (int rx = x + 24; rx < x + w - 20; rx += 36) {
			g.fill(rx, y + 1, rx + 1, y + 2, IRON_LIGHT);
			g.fill(rx, y + h - 2, rx + 1, y + h - 1, IRON_LIGHT);
		}

		// Item grid: a sunken bed, wells on every row that is not a banner.
		ScreenDrawUtils.drawBevelBorder(g, x + GRID_X - 2, y + GRID_Y - 2, COLUMNS * SLOT + 2, VISIBLE_ROWS * SLOT + 2,
				OUTLINE, IRON_LIGHT);
		g.fill(x + GRID_X - 1, y + GRID_Y - 1, x + GRID_X - 1 + COLUMNS * SLOT, y + GRID_Y - 1 + VISIBLE_ROWS * SLOT,
				0xFF0E0405);
		int first = firstRow(screen);
		for (int row = 0; row < VISIBLE_ROWS; row++) {
			if (bannerAt(first + row) != null) {
				continue;
			}
			for (int col = 0; col < COLUMNS; col++) {
				ScreenDrawUtils.drawSlotWell(g, x + GRID_X + col * SLOT, y + GRID_Y + row * SLOT, WELL_DARK, WELL,
						WELL_LIGHT);
			}
		}

		// Hotbar.
		ScreenDrawUtils.drawBevelBorder(g, x + GRID_X - 2, y + 110, COLUMNS * SLOT + 2, SLOT + 2, OUTLINE, IRON_LIGHT);
		for (int col = 0; col < COLUMNS; col++) {
			ScreenDrawUtils.drawSlotWell(g, x + GRID_X + col * SLOT, y + 112, WELL_DARK, WELL, WELL_LIGHT);
		}

		drawScrollbar(g, screen, x, y);
		drawSelectedTab(g, screen, x, y, h);
	}

	@SubscribeEvent
	public static void onForeground(ContainerScreenEvent.Render.Foreground event) {
		if (!showing(event.getContainerScreen()) || bannerRows.length == 0) {
			return;
		}
		CreativeModeInventoryScreen screen = (CreativeModeInventoryScreen) event.getContainerScreen();
		GuiGraphics g = event.getGuiGraphics();
		Minecraft minecraft = Minecraft.getInstance();
		float time = minecraft.level == null ? 0f : AnimationTime.gameTime(minecraft.level, AnimationTime.partialTick());
		int first = firstRow(screen);
		for (int row = 0; row < VISIBLE_ROWS; row++) {
			CreativeTabSection section = bannerAt(first + row);
			if (section != null) {
				// The foreground pose is already relative to the panel's corner.
				CreativeTabBanners.draw(g, minecraft.font, section, GRID_X - 1, GRID_Y - 1 + row * SLOT,
						COLUMNS * SLOT, SLOT, time);
			}
		}
	}

	private static void drawScrollbar(GuiGraphics g, CreativeModeInventoryScreen screen, int x, int y) {
		int trackX = x + 174;
		int trackTop = y + 17;
		ScreenDrawUtils.drawBevelBorder(g, trackX, trackTop, 14, 114, OUTLINE, IRON_LIGHT);
		g.fill(trackX + 1, trackTop + 1, trackX + 13, trackTop + 113, 0xFF0E0405);
		float scroll = ((CreativeScreenAccessor) screen).hemomancy$scrollOffs();
		boolean live = screen.getMenu().canScroll();
		int kx = x + 175;
		int ky = y + 18 + (int) ((112 - 17) * scroll);
		g.fill(kx, ky, kx + 12, ky + 15, OUTLINE);
		g.fillGradient(kx + 1, ky + 1, kx + 11, ky + 14, live ? 0xFF9A2A30 : 0xFF3A1A1D, live ? 0xFF4E0E12 : 0xFF241012);
		ScreenDrawUtils.drawBevelBorder(g, kx + 1, ky + 1, 10, 13, live ? 0xFFC85A60 : 0xFF54303A,
				live ? 0xFF2A0608 : 0xFF180A0C);
		int grip = live ? BONE : 0xFF6A5E50;
		for (int line = 0; line < 3; line++) {
			g.fill(kx + 3, ky + 5 + line * 2, kx + 9, ky + 6 + line * 2, grip);
		}
	}

	/** Repaints the open tab's flag so it joins the frame instead of the vanilla grey. */
	private static void drawSelectedTab(GuiGraphics g, CreativeModeInventoryScreen screen, int x, int y, int h) {
		CreativeModeTab tab = CreativeScreenAccessor.hemomancy$selectedTab();
		var page = screen.getCurrentPage();
		if (!page.getVisibleTabs().contains(tab)) {
			return;
		}
		boolean top = page.isTop(tab);
		int tx = x + ((CreativeScreenAccessor) screen).hemomancy$tabX(tab);
		int ty = top ? y - 28 : y + h - 4;
		int tw = 26;
		int th = 32;
		// Body runs 3 px into the frame so the ring opens under the tab.
		int bodyTop = top ? ty : y + h - 3;
		int bodyBottom = top ? y + 3 : ty + th;
		g.fillGradient(tx + 1, bodyTop, tx + tw - 1, bodyBottom, top ? PANEL_BOTTOM : PANEL_TOP,
				top ? PANEL_TOP : PANEL_BOTTOM);
		int edgeTop = top ? ty : y + h - 3;
		int edgeBottom = top ? y + 3 : ty + th;
		g.fill(tx, edgeTop, tx + 1, edgeBottom, OUTLINE);
		g.fill(tx + tw - 1, edgeTop, tx + tw, edgeBottom, OUTLINE);
		g.fill(tx + 1, edgeTop, tx + 2, edgeBottom, IRON);
		g.fill(tx + tw - 2, edgeTop, tx + tw - 1, edgeBottom, IRON);
		int capY = top ? ty : ty + th - 1;
		int innerCapY = top ? ty + 1 : ty + th - 2;
		g.fill(tx, capY, tx + tw, capY + 1, OUTLINE);
		g.fill(tx + 1, innerCapY, tx + tw - 1, innerCapY + 1, IRON);
		g.fill(tx + 1, capY, tx + 3, capY + 1, BONE);
		g.fill(tx + tw - 3, capY, tx + tw - 1, capY + 1, BONE);
		g.pose().pushPose();
		g.pose().translate(0.0F, 0.0F, 100.0F);
		g.renderItem(tab.getIconItem(), tx + 5, ty + (top ? 9 : 7));
		g.pose().popPose();
	}
}
