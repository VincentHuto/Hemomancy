package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.HarbingerProgressScreen;
import com.vincenthuto.hemomancy.client.screen.skilltree.unstained.UnstainedProgressScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Static rendering helpers shared between {@link HarbingerProgressScreen}
 * and {@link UnstainedProgressScreen} (and any future skill/progress screens).
 */
public final class ScreenDrawUtils {

	private ScreenDrawUtils() {}

	// ── Node icon constants ──────────────────────────────────────
	/** Padding pixels around the item icon inside a node. */
	public static final int ITEM_PADDING = 4;

	// ── Border helpers ───────────────────────────────────────────

	/**
	 * Draws a two-pixel thick border rectangle (1px outer, 1px inner).
	 *
	 * @param outer ARGB colour of the outermost pixel line
	 * @param inner ARGB colour of the second-inmost pixel line
	 */
	public static void drawBorder(GuiGraphics gfx, int x, int y, int w, int h,
								  int outer, int inner) {
		// Outer frame
		gfx.fill(x,         y,         x + w,     y + 1,     outer);
		gfx.fill(x,         y + h - 1, x + w,     y + h,     outer);
		gfx.fill(x,         y,         x + 1,     y + h,     outer);
		gfx.fill(x + w - 1, y,         x + w,     y + h,     outer);
		// Inner frame
		gfx.fill(x + 1,     y + 1,     x + w - 1, y + 2,     inner);
		gfx.fill(x + 1,     y + h - 2, x + w - 1, y + h - 1, inner);
		gfx.fill(x + 1,     y + 1,     x + 2,     y + h - 1, inner);
		gfx.fill(x + w - 2, y + 1,     x + w - 1, y + h - 1, inner);
	}

	/**
	 * Draws a simple single-pixel border rectangle.
	 */
	public static void drawSimpleBorder(GuiGraphics gfx, int x, int y, int w, int h, int color) {
		gfx.fill(x,         y,         x + w,     y + 1,  color);
		gfx.fill(x,         y + h - 1, x + w,     y + h,  color);
		gfx.fill(x,         y,         x + 1,     y + h,  color);
		gfx.fill(x + w - 1, y,         x + w,     y + h,  color);
	}

	// ── Home button ──────────────────────────────────────────────

	/**
	 * Draws a standard "home / return to centre" button at the given position.
	 *
	 * @param bx          left edge of the button (screen pixels)
	 * @param by          top edge of the button (screen pixels)
	 * @param size        width and height of the button (square)
	 * @param hovered     whether the mouse is currently over the button
	 * @param hoverBorder ARGB border colour when hovered
	 * @param hoverText   ARGB icon colour when hovered
	 * @param idleBorder  ARGB border colour when not hovered
	 * @param idleText    ARGB icon colour when not hovered
	 * @param idleBg      ARGB background colour when not hovered
	 * @param hoverBg     ARGB background colour when hovered
	 */
	public static void drawHomeButton(GuiGraphics gfx, Font font,
									  int bx, int by, int size,
									  boolean hovered,
									  int hoverBg,   int idleBg,
									  int hoverBorder, int idleBorder,
									  int hoverText,   int idleText) {
		int bg = hovered ? hoverBg : idleBg;
		gfx.fill(bx, by, bx + size, by + size, bg);

		int bc = hovered ? hoverBorder : idleBorder;
		gfx.fill(bx,              by,              bx + size, by + 1,       bc);
		gfx.fill(bx,              by + size - 1,   bx + size, by + size,    bc);
		gfx.fill(bx,              by,              bx + 1,    by + size,    bc);
		gfx.fill(bx + size - 1,  by,              bx + size, by + size,    bc);

		int textCol = hovered ? hoverText : idleText;
		gfx.drawCenteredString(font, "⌂", bx + size / 2, by + (size - 8) / 2, textCol);
	}

	// ── Sidebar / drawer toggle tab ──────────────────────────────

	/**
	 * Draws a small clickable arrow tab used to expand/collapse a sidebar or drawer.
	 * The arrow points left (◀) when expanded and right (▶) when collapsed.
	 *
	 * @param tabX       left edge of the tab (screen pixels)
	 * @param tabY       top edge of the tab (screen pixels)
	 * @param tabW       width of the tab
	 * @param tabH       height of the tab
	 * @param expanded   true if the panel is currently expanded
	 * @param hovered    true if the mouse is over the tab
	 * @param hoverBg    ARGB background when hovered
	 * @param idleBg     ARGB background when idle
	 * @param hoverBc    ARGB border when hovered
	 * @param idleBc     ARGB border when idle
	 * @param hoverArrow ARGB arrow colour when hovered
	 * @param idleArrow  ARGB arrow colour when idle
	 */
	public static void drawSidebarToggleTab(GuiGraphics gfx, Font font,
											int tabX, int tabY, int tabW, int tabH,
											boolean expanded, boolean hovered,
											int hoverBg, int idleBg,
											int hoverBc, int idleBc,
											int hoverArrow, int idleArrow) {
		gfx.fill(tabX, tabY, tabX + tabW, tabY + tabH, hovered ? hoverBg : idleBg);

		int bc = hovered ? hoverBc : idleBc;
		gfx.fill(tabX,              tabY,              tabX + tabW, tabY + 1,          bc);
		gfx.fill(tabX,              tabY + tabH - 1,   tabX + tabW, tabY + tabH,       bc);
		gfx.fill(tabX,              tabY,              tabX + 1,    tabY + tabH,       bc);
		gfx.fill(tabX + tabW - 1,  tabY,              tabX + tabW, tabY + tabH,       bc);

		String arrow   = expanded ? "◀" : "▶";
		int    arrowCol = hovered ? hoverArrow : idleArrow;
		gfx.drawCenteredString(font, arrow, tabX + tabW / 2, tabY + tabH / 2 - 4, arrowCol);
	}

	// ── Tab bar ──────────────────────────────────────────────────

	/**
	 * A minimal descriptor for a single tab button — label, accent colour, and
	 * whether it is the currently active tab.
	 */
	public record TabDesc(String label, int accentColor, boolean active) {}

	/**
	 * Draws a row of tab buttons flush to the top-right of the GUI viewport.
	 * Tabs are drawn right-to-left so the first entry is rightmost.
	 *
	 * @param gfx      graphics context
	 * @param font     font renderer
	 * @param tabs     ordered list of tab descriptors (index 0 = rightmost)
	 * @param guiLeft  screen-space left edge of the GUI
	 * @param guiTop   screen-space top edge of the GUI
	 * @param guiWidth width of the GUI
	 * @param tabH     height of each tab button
	 * @param tabPad   horizontal gap between tabs and from the right edge
	 * @param mouseX   current mouse X
	 * @param mouseY   current mouse Y
	 */
	public static void drawTabs(GuiGraphics gfx, Font font,
								List<TabDesc> tabs,
								int guiLeft, int guiTop, int guiWidth,
								int tabH, int tabPad,
								int mouseX, int mouseY) {
		int x = guiLeft + guiWidth - tabPad;
		int y = guiTop + tabPad;

		for (int i = tabs.size() - 1; i >= 0; i--) {
			TabDesc tab = tabs.get(i);
			int tw = font.width(tab.label()) + 10;
			int tx = x - tw;

			boolean hovered = mouseX >= tx && mouseX <= tx + tw
					&& mouseY >= y && mouseY <= y + tabH;

			int bg = tab.active() ? 0xDD1A0505 : (hovered ? 0xBB180404 : 0x99120303);
			gfx.fill(tx, y, tx + tw, y + tabH, bg);

			int bc = tab.active() ? tab.accentColor() : 0xFF444444;
			gfx.fill(tx, y,              tx + tw, y + 1,              bc);
			gfx.fill(tx, y + tabH - 1,  tx + tw, y + tabH,           bc);
			gfx.fill(tx, y,              tx + 1,  y + tabH,           bc);
			gfx.fill(tx + tw - 1, y,    tx + tw, y + tabH,           bc);

			if (tab.active()) {
				gfx.fill(tx + 1, y + tabH - 2, tx + tw - 1, y + tabH - 1, tab.accentColor());
			}

			int textCol = tab.active() ? tab.accentColor() : (hovered ? 0xFFAAAAAA : 0xFF777777);
			gfx.drawCenteredString(font, tab.label(), tx + tw / 2, y + (tabH - 8) / 2, textCol);

			x = tx - tabPad;
		}
	}

	/**
	 * Returns the index (into {@code tabs}) of the tab the mouse is over,
	 * or -1 if none.  Must use the same geometry as {@link #drawTabs}.
	 */
	public static int tabIndexUnder(Font font,
									List<TabDesc> tabs,
									int guiLeft, int guiTop, int guiWidth,
									int tabH, int tabPad,
									double mouseX, double mouseY) {
		int x = guiLeft + guiWidth - tabPad;
		int y = guiTop  + tabPad;

		for (int i = tabs.size() - 1; i >= 0; i--) {
			TabDesc tab = tabs.get(i);
			int tw = font.width(tab.label()) + 10;
			int tx = x - tw;

			if (mouseX >= tx && mouseX <= tx + tw && mouseY >= y && mouseY <= y + tabH) {
				return i;
			}
			x = tx - tabPad;
		}
		return -1;
	}

	// ── Node icon rendering ──────────────────────────────────────

	/**
	 * Renders an {@link ItemStack} centred inside a node, scaled to fit within the
	 * node bounds. Items are normally 16×16; this scales them to
	 * {@code (2*halfNodeSize - ITEM_PADDING)} pixels.
	 */
	public static void renderScaledItem(GuiGraphics gfx, ItemStack stack,
										int centerX, int centerY, int halfNodeSize) {
		if (stack == null || stack.isEmpty()) return;
		float nodeInner = Math.max(ITEM_PADDING, halfNodeSize * 2 - ITEM_PADDING);
		float itemScale = nodeInner / 16.0f;
		PoseStack pose = gfx.pose();
		pose.pushPose();
		pose.translate(centerX - nodeInner / 2.0f, centerY - nodeInner / 2.0f, 0);
		pose.scale(itemScale, itemScale, 1);
		gfx.renderItem(stack, 0, 0);
		pose.popPose();
	}

	/**
	 * Renders a texture from any {@link ResourceLocation} centred inside a node,
	 * scaled to fit within the node bounds (UV 0→1 mapped to node inner size).
	 */
	public static void renderScaledTexture(GuiGraphics gfx, ResourceLocation texture,
										   int centerX, int centerY, int halfNodeSize) {
		int nodeInner = Math.max(ITEM_PADDING, halfNodeSize * 2 - ITEM_PADDING);
		int x = centerX - nodeInner / 2;
		int y = centerY - nodeInner / 2;
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		gfx.blit(texture, x, y, 0, 0, nodeInner, nodeInner, nodeInner, nodeInner);
		RenderSystem.disableBlend();
	}

	// ── Progress bar ─────────────────────────────────────────────

	/**
	 * Draws an animated progress bar with a pulsing fill, frame, and 25% notch marks.
	 *
	 * @param gfx         graphics context
	 * @param x           left edge in screen space
	 * @param y           top edge in screen space
	 * @param w           total bar width in pixels
	 * @param h           total bar height in pixels
	 * @param value       current value
	 * @param max         maximum value
	 * @param fillColorRGB  packed RGB (no alpha) of the fill colour
	 */
	public static void drawProgressBar(GuiGraphics gfx, int x, int y, int w, int h,
									   float value, float max, int fillColorRGB) {
	 float animTime = 0f;

		animTime += 0.016f; // ~60 FPS approximation

		float time = animTime;		
		float ratio = Mth.clamp(value / max, 0f, 1f);
		int fillW = (int)(w * ratio);

		// Background
		gfx.fill(x, y, x + w, y + h, 0xFF0C1020);

		// Frame
		int frameCol = 0xFF203040;
		gfx.fill(x,         y,         x + w, y + 1,     frameCol);
		gfx.fill(x,         y + h - 1, x + w, y + h,     frameCol);
		gfx.fill(x,         y,         x + 1, y + h,     frameCol);
		gfx.fill(x + w - 1, y,         x + w, y + h,     frameCol);

		// Fill with per-column pulse
		if (fillW > 0) {
			float r = ((fillColorRGB >> 16) & 0xFF) / 255f;
			float g = ((fillColorRGB >> 8)  & 0xFF) / 255f;
			float b = ( fillColorRGB        & 0xFF) / 255f;

			for (int col = 0; col < fillW; col++) {
				float colT      = (float) col / Math.max(w, 1);
				float brightness = 0.6f + 0.4f * colT;
				float pulse      = 0.9f + 0.1f * Mth.sin(time * 2.0f + col * 0.08f);
				float fb         = brightness * pulse;

				int cr = (int) Mth.clamp(r * fb * 255, 0, 255);
				int cg = (int) Mth.clamp(g * fb * 255, 0, 255);
				int cb = (int) Mth.clamp(b * fb * 255, 0, 255);
				gfx.fill(x + 1 + col, y + 1, x + 1 + col + 1, y + h - 1,
						0xFF000000 | (cr << 16) | (cg << 8) | cb);
			}
		}

		// 25% notch marks
		for (int notch = 1; notch <= 3; notch++) {
			int nx = x + w * notch / 4;
			gfx.fill(nx, y, nx + 1, y + h, 0x30FFFFFF);
		}
	}

	// ── Text helpers ─────────────────────────────────────────────

	/**
	 * Simple word-wrap: splits {@code text} into lines no wider than
	 * {@code maxWidth} pixels (measured using the supplied font).
	 */
	public static List<String> wrapText(Font font, String text, int maxWidth) {
		List<String> lines = new ArrayList<>();
		String[] words = text.split(" ");
		StringBuilder current = new StringBuilder();
		for (String word : words) {
			if (!current.isEmpty() && font.width(current + " " + word) > maxWidth) {
				lines.add(current.toString());
				current = new StringBuilder(word);
			} else {
				if (!current.isEmpty()) current.append(" ");
				current.append(word);
			}
		}
		if (!current.isEmpty()) lines.add(current.toString());
		return lines;
	}

	// ── Shared tier-browser helpers ──────────────────────────────

	/**
	 * Truncates {@code text} to fit within {@code maxWidth} pixels,
	 * appending {@code "…"} if necessary.
	 */
	public static String truncateText(Font font, String text, int maxWidth) {
		if (font.width(text) <= maxWidth) return text;
		int ellipsisW = font.width("...");
		int targetW = maxWidth - ellipsisW;
		while (text.length() > 1 && font.width(text) > targetW) {
			text = text.substring(0, text.length() - 1);
		}
		return text + "...";
	}

	/**
	 * Draws a small navigation button (used for layer-up / layer-down controls
	 * and any other single-symbol buttons in the tier-browser tabs).
	 */
	public static void drawNavButton(GuiGraphics gfx, Font font,
			int x, int y, int w, int h, String symbol,
			boolean hovered, int hoverColor) {
		int bg = hovered ? 0xDD1A0505 : 0x99120303;
		gfx.fill(x, y, x + w, y + h, bg);
		int bc = hovered ? hoverColor : 0xFF444444;
		gfx.fill(x,         y,         x + w,     y + 1,     bc);
		gfx.fill(x,         y + h - 1, x + w,     y + h,     bc);
		gfx.fill(x,         y,         x + 1,     y + h,     bc);
		gfx.fill(x + w - 1, y,         x + w,     y + h,     bc);
		int textCol = hovered ? 0xFFEEDDFF : 0xFF888888;
		gfx.drawCenteredString(font, symbol, x + w / 2, y + (h - 8) / 2, textCol);
	}

	private static final int LAYER_BTN_SIZE = 16;

	/**
	 * Draws ▲/▼ layer-control buttons and a centre label on the left side of the
	 * 3D model area.  Returns {@code true} if a tooltip was rendered (so the
	 * caller can skip its own tooltip logic).
	 *
	 * @param maxLayer   maximum layer index (0 = single-layer, no buttons drawn)
	 * @param visibleLayer current layer selection (-1 = all)
	 * @param tabColor   accent colour for the buttons
	 */
	public static void drawLayerButtons(GuiGraphics gfx, Font font,
			int bx, int centerY, int maxLayer, int visibleLayer,
			int tabColor, int mouseX, int mouseY) {
		if (maxLayer <= 0) return;

		int bs = LAYER_BTN_SIZE;

		int upY   = centerY - bs - 14;
		int downY = centerY + 14;

		boolean upHov   = isOverLayerButton(mouseX, mouseY, bx, upY,   bs);
		boolean downHov = isOverLayerButton(mouseX, mouseY, bx, downY, bs);

		drawNavButton(gfx, font, bx, upY,   bs, bs, "\u25B2", upHov,   tabColor);
		drawNavButton(gfx, font, bx, downY, bs, bs, "\u25BC", downHov, tabColor);

		String label = visibleLayer < 0 ? "All" : "Y:" + (visibleLayer + 1);
		gfx.drawCenteredString(font, label, bx + bs / 2, centerY - 4, 0xFFAAAAAA);

		if (upHov) {
			gfx.renderTooltip(font,
					net.minecraft.network.chat.Component.literal("Layer Up"), mouseX, mouseY);
		} else if (downHov) {
			gfx.renderTooltip(font,
					net.minecraft.network.chat.Component.literal("Layer Down"), mouseX, mouseY);
		}
	}

	/** Hit-test for a layer button of {@link #LAYER_BTN_SIZE}×{@link #LAYER_BTN_SIZE}. */
	public static boolean isOverLayerButton(double mx, double my, int bx, int by, int size) {
		return mx >= bx && mx <= bx + size && my >= by && my <= by + size;
	}

	/** Convenience overload using the default {@link #LAYER_BTN_SIZE}. */
	public static boolean isOverLayerButton(double mx, double my, int bx, int by) {
		return isOverLayerButton(mx, my, bx, by, LAYER_BTN_SIZE);
	}
}
