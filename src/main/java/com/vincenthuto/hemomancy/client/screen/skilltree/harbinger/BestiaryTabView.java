package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.ProgressScreenContext;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bestiary.SpecimenBestiaryDefinitions;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class BestiaryTabView {
	private static final int TAB_COLOR = 0xFF77AA66;
	private static final int PANEL_BG = 0xDD07120B;
	private static final int PANEL_BORDER = 0xFF2F5E3F;
	private static final int ROW_H = 18;
	private static final int GROUP_H = 14;
	private static final int TOP_H = 42;
	private static final int PAD = 10;
	private static final int LIST_INSET = 5;
	private static final int LIST_SCROLLBAR_W = 6;
	private static final int DETAIL_PAD = 10;
	private static final int PREVIEW_GAP = 10;
	private static final int PREVIEW_MIN_W = 96;
	private static final int TEXT_MIN_W = 120;

	private BestiaryTabView() {
	}

	public static void draw(GuiGraphics gfx, ProgressScreenContext ctx, BestiaryTabState state, int mouseX, int mouseY) {
		int x = ctx.guiLeft() + PAD;
		int y = ctx.guiTop() + 24;
		int w = ctx.guiWidth() - PAD * 2;
		int h = ctx.guiHeight() - 34;
		int listW = Math.min(220, Math.max(160, w / 3));
		int detailX = x + listW + PAD;
		int detailW = w - listW - PAD;

		drawHeader(gfx, ctx, x, y, w, state);
		drawList(gfx, ctx, state, x, y + TOP_H, listW, h - TOP_H, mouseX, mouseY);
		drawDetail(gfx, ctx, state, detailX, y + TOP_H, detailW, h - TOP_H, mouseX, mouseY);
	}

	static int listHeight(ProgressScreenContext ctx) {
		return ctx.guiHeight() - 34 - TOP_H;
	}

	static int contentHeight(BestiaryTabState state) {
		int height = LIST_INSET * 2;
		BestiaryTabState.Kind last = null;
		for (BestiaryTabState.Entry entry : state.entries) {
			if (entry.kind() != last) {
				height += GROUP_H;
				last = entry.kind();
			}
			height += ROW_H;
		}
		return height;
	}

	static boolean isOverList(ProgressScreenContext ctx, double mx, double my) {
		int x = ctx.guiLeft() + PAD;
		int y = ctx.guiTop() + 24 + TOP_H;
		int listW = Math.min(220, Math.max(160, (ctx.guiWidth() - PAD * 2) / 3));
		int listH = listHeight(ctx);
		return mx >= x && mx <= x + listW && my >= y && my <= y + listH;
	}

	static BestiaryTabState.Entry entryUnder(ProgressScreenContext ctx, BestiaryTabState state, double mx, double my) {
		int x = ctx.guiLeft() + PAD;
		int y = ctx.guiTop() + 24 + TOP_H;
		int listW = Math.min(220, Math.max(160, (ctx.guiWidth() - PAD * 2) / 3));
		int listH = listHeight(ctx);
		if (mx < x || mx > x + listW || my < y || my > y + listH) {
			return null;
		}
		int rowY = y + LIST_INSET - state.listScroll;
		BestiaryTabState.Kind last = null;
		for (BestiaryTabState.Entry entry : state.entries) {
			if (entry.kind() != last) {
				rowY += GROUP_H;
				last = entry.kind();
			}
			if (my >= rowY && my < rowY + ROW_H) {
				return entry;
			}
			rowY += ROW_H;
		}
		return null;
	}

	static boolean hasPreviewForSelected(BestiaryTabState state) {
		BestiaryTabState.Entry entry = state == null ? null : state.selectedEntry();
		return entry != null && entry.discovered() && BestiaryEntityPreview.hasPreview(entry.previewId());
	}

	static boolean isOverPreview(ProgressScreenContext ctx, BestiaryTabState state, double mx, double my) {
		PreviewBounds bounds = previewBounds(ctx, state);
		return bounds != null && bounds.contains(mx, my);
	}

	private static void drawHeader(GuiGraphics gfx, ProgressScreenContext ctx, int x, int y, int w,
			BestiaryTabState state) {
		gfx.drawString(ctx.font(), Component.translatable("bestiary.hemomancy.title")
				.withStyle(s -> s.withColor(TAB_COLOR).withBold(true)), x, y, 0, false);
		String counts = state.recordedSpecimenCount + "/" + SpecimenBestiaryDefinitions.totalResearchSpecimens()
				+ " specimens  |  " + state.recordedMorphlingLayerCount + "/"
				+ SpecimenBestiaryDefinitions.orderedMorphlingEntries().size() + " morphling strains";
		gfx.drawString(ctx.font(), counts, x, y + 14, 0xFF9DAA9D, false);
		gfx.fill(x, y + 30, x + w, y + 31, 0xFF24442D);
	}

	private static void drawList(GuiGraphics gfx, ProgressScreenContext ctx, BestiaryTabState state,
			int x, int y, int w, int h, int mouseX, int mouseY) {
		gfx.fill(x, y, x + w, y + h, PANEL_BG);
		ScreenDrawUtils.drawSimpleBorder(gfx, x, y, w, h, PANEL_BORDER);
		int maxScroll = Math.max(0, contentHeight(state) - h);
		int contentRight = maxScroll > 0 ? x + w - LIST_SCROLLBAR_W - 3 : x + w - 1;
		gfx.enableScissor(x + 1, y + 1, contentRight, y + h - 1);
		try {
			int rowY = y + LIST_INSET - state.listScroll;
			BestiaryTabState.Kind last = null;
			for (BestiaryTabState.Entry entry : state.entries) {
				if (entry.kind() != last) {
					if (rowY + GROUP_H >= y && rowY <= y + h) {
						Component label = entry.kind() == BestiaryTabState.Kind.SPECIMEN
								? Component.translatable("bestiary.hemomancy.section.specimens")
								: Component.translatable("bestiary.hemomancy.section.morphlings");
						int textW = maxScroll > 0 ? w - LIST_SCROLLBAR_W - 15 : w - 12;
						String drawnLabel = ScreenDrawUtils.truncateText(ctx.font(), label.getString(), textW);
						gfx.drawString(ctx.font(), drawnLabel, x + 6, rowY + 2, 0xFF8CA88C, false);
					}
					rowY += GROUP_H;
					last = entry.kind();
				}
				if (rowY + ROW_H >= y && rowY <= y + h) {
					boolean selected = state.selectedEntry() == entry;
					boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= rowY && mouseY < rowY + ROW_H;
					int bg = selected ? 0x66336644 : (hovered ? 0x4422332A : 0x00000000);
					if (bg != 0) {
						gfx.fill(x + 3, rowY, x + w - 3, rowY + ROW_H, bg);
					}
					int dotColor = entry.discovered() ? TAB_COLOR : 0xFF4F574F;
					gfx.fill(x + 8, rowY + 6, x + 12, rowY + 10, dotColor);
					Component title = entry.discovered()
							? Component.translatable(entry.titleKey())
							: Component.translatable("bestiary.hemomancy.locked.entry");
					int textColor = entry.discovered() ? 0xFFD6E0D1 : 0xFF697069;
					int textW = maxScroll > 0 ? w - LIST_SCROLLBAR_W - 28 : w - 25;
					String drawnTitle = ScreenDrawUtils.truncateText(ctx.font(), title.getString(), textW);
					gfx.drawString(ctx.font(), drawnTitle, x + 17, rowY + 5, textColor, false);
				}
				rowY += ROW_H;
			}
		} finally {
			gfx.disableScissor();
		}
		drawListScrollbar(gfx, x + w - LIST_SCROLLBAR_W - 2, y + 2, LIST_SCROLLBAR_W, h - 4,
				state.listScroll, maxScroll, contentHeight(state));
	}

	private static void drawListScrollbar(GuiGraphics gfx, int x, int y, int w, int h,
			int scrollOffset, int maxScroll, int totalContentH) {
		if (maxScroll <= 0 || totalContentH <= 0) {
			return;
		}
		gfx.fill(x, y, x + w, y + h, 0xAA06100A);
		ScreenDrawUtils.drawSimpleBorder(gfx, x, y, w, h, 0xFF1F3E2B);
		int thumbH = Math.max(14, h * h / totalContentH);
		int travel = Math.max(1, h - thumbH - 2);
		int clampedScroll = Math.max(0, Math.min(maxScroll, scrollOffset));
		int thumbY = y + 1 + (int) (travel * (clampedScroll / (float) maxScroll));
		gfx.fill(x + 1, thumbY, x + w - 1, thumbY + thumbH, TAB_COLOR);
	}

	private static void drawDetail(GuiGraphics gfx, ProgressScreenContext ctx, BestiaryTabState state,
			int x, int y, int w, int h, int mouseX, int mouseY) {
		gfx.fill(x, y, x + w, y + h, PANEL_BG);
		ScreenDrawUtils.drawSimpleBorder(gfx, x, y, w, h, PANEL_BORDER);
		BestiaryTabState.Entry entry = state.selectedEntry();
		if (entry == null) {
			gfx.drawCenteredString(ctx.font(), Component.translatable("bestiary.hemomancy.empty"),
					x + w / 2, y + h / 2, 0xFF777777);
			return;
		}

		if (entry.discovered() && BestiaryEntityPreview.hasPreview(entry.previewId())) {
			drawPreviewDossier(gfx, ctx, state, entry, x, y, w, h, mouseX, mouseY);
			return;
		}
		drawTextDossier(gfx, ctx, state, entry, x + DETAIL_PAD, y + DETAIL_PAD,
				w - DETAIL_PAD * 2, h - DETAIL_PAD * 2);
	}

	private static void drawPreviewDossier(GuiGraphics gfx, ProgressScreenContext ctx, BestiaryTabState state,
			BestiaryTabState.Entry entry, int x, int y, int w, int h, int mouseX, int mouseY) {
		PreviewBounds bounds = previewBounds(x, y, w, h);
		if (bounds == null) {
			drawTextDossier(gfx, ctx, state, entry, x + DETAIL_PAD, y + DETAIL_PAD,
					w - DETAIL_PAD * 2, h - DETAIL_PAD * 2);
			return;
		}

		gfx.fill(bounds.x(), bounds.y(), bounds.right(), bounds.bottom(), 0xAA031007);
		ScreenDrawUtils.drawSimpleBorder(gfx, bounds.x(), bounds.y(), bounds.w(), bounds.h(), 0xFF24442D);
		gfx.enableScissor(bounds.x() + 1, bounds.y() + 1, bounds.right() - 1, bounds.bottom() - 1);
		try {
			BestiaryEntityPreview.render(gfx, ctx, state, entry, bounds.x() + 1, bounds.y() + 1,
					bounds.right() - 1, bounds.bottom() - 1);
		} finally {
			gfx.disableScissor();
		}
		if (bounds.contains(mouseX, mouseY)) {
			drawPreviewHint(gfx, ctx, "Rotate | Zoom",
					bounds.x() + 4, bounds.bottom() - 13, bounds.w() - 8);
		}

		int textX = bounds.right() + PREVIEW_GAP;
		int textW = x + w - DETAIL_PAD - textX;
		drawTextDossier(gfx, ctx, state, entry, textX, y + DETAIL_PAD, textW, h - DETAIL_PAD * 2);
	}

	private static void drawTextDossier(GuiGraphics gfx, ProgressScreenContext ctx, BestiaryTabState state,
			BestiaryTabState.Entry entry, int x, int y, int w, int h) {
		int cy = y - state.infoScroll;
		Component title = entry.discovered()
				? Component.translatable(entry.titleKey())
				: Component.translatable("bestiary.hemomancy.locked.title");
		gfx.drawString(ctx.font(), title.copy().withStyle(s -> s.withColor(TAB_COLOR).withBold(true)),
				x, cy, 0, false);
		cy += 16;
		String statusKey = statusKey(entry);
		gfx.drawString(ctx.font(), Component.translatable(statusKey).withStyle(s -> s.withColor(0xFF9DB09D)),
				x, cy, 0, false);
		cy += 16;
		gfx.fill(x, cy, x + w, cy + 1, 0xFF24442D);
		cy += 9;

		String description = entry.discovered()
				? Component.translatable(entry.descriptionKey()).getString()
				: Component.translatable("bestiary.hemomancy.locked.description").getString();
		cy = drawWrapped(gfx, ctx, description, x, cy, w, 0xFFD0D8CC);
		cy += 10;
		String source = entry.discovered()
				? Component.translatable(entry.sourceKey()).getString()
				: Component.translatable("bestiary.hemomancy.locked.source").getString();
		drawWrapped(gfx, ctx, source, x, cy, w, 0xFF91A091);
	}

	private static PreviewBounds previewBounds(ProgressScreenContext ctx, BestiaryTabState state) {
		if (ctx == null || !hasPreviewForSelected(state)) {
			return null;
		}
		int x = ctx.guiLeft() + PAD;
		int y = ctx.guiTop() + 24;
		int w = ctx.guiWidth() - PAD * 2;
		int h = ctx.guiHeight() - 34;
		int listW = Math.min(220, Math.max(160, w / 3));
		int detailX = x + listW + PAD;
		int detailW = w - listW - PAD;
		return previewBounds(detailX, y + TOP_H, detailW, h - TOP_H);
	}

	private static PreviewBounds previewBounds(int x, int y, int w, int h) {
		int availableW = w - DETAIL_PAD * 2;
		int maxPreviewW = availableW - PREVIEW_GAP - TEXT_MIN_W;
		if (maxPreviewW < PREVIEW_MIN_W) {
			return null;
		}
		int preferred = w >= 360 ? Math.round(availableW * 0.45F) : Math.round(availableW * 0.38F);
		int previewW = Math.max(PREVIEW_MIN_W, Math.min(preferred, maxPreviewW));
		return new PreviewBounds(x + DETAIL_PAD, y + DETAIL_PAD, previewW, h - DETAIL_PAD * 2);
	}

	static float previewHintScale(int textWidth, int availableWidth) {
		if (textWidth <= 0 || availableWidth <= 0 || textWidth <= availableWidth) {
			return 1.0F;
		}
		return Math.max(0.5F, availableWidth / (float) textWidth);
	}

	private static void drawPreviewHint(GuiGraphics gfx, ProgressScreenContext ctx, String text, int x, int y,
			int availableWidth) {
		int textWidth = ctx.font().width(text);
		float scale = previewHintScale(textWidth, availableWidth);
		gfx.pose().pushPose();
		try {
			gfx.pose().translate(x + availableWidth / 2.0F, y, 0.0F);
			gfx.pose().scale(scale, scale, 1.0F);
			gfx.drawString(ctx.font(), text, Math.round(-textWidth / 2.0F), 0, 0xFF8CA88C, false);
		} finally {
			gfx.pose().popPose();
		}
	}

	private record PreviewBounds(int x, int y, int w, int h) {
		int right() {
			return x + w;
		}

		int bottom() {
			return y + h;
		}

		boolean contains(double mx, double my) {
			return mx >= x && mx <= right() && my >= y && my <= bottom();
		}
	}

	private static String statusKey(BestiaryTabState.Entry entry) {
		if (!entry.discovered()) {
			return "bestiary.hemomancy.status.unknown";
		}
		if (entry.kind() == BestiaryTabState.Kind.MORPHLING) {
			return "bestiary.hemomancy.status.wild_morphling";
		}
		return entry.surrendered()
				? "bestiary.hemomancy.status.surrendered"
				: "bestiary.hemomancy.status.recorded";
	}

	private static int drawWrapped(GuiGraphics gfx, ProgressScreenContext ctx, String text, int x, int y, int w,
			int color) {
		for (String line : ScreenDrawUtils.wrapText(ctx.font(), text, w)) {
			gfx.drawString(ctx.font(), line, x, y, color, false);
			y += 11;
		}
		return y;
	}
}
