package com.vincenthuto.hemomancy.client.screen.item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hemomancy.common.mission.unstained.UnstainedObservances.Observance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/** A dedicated, server-snapshotted ledger of observances grouped by directing NPC. */
public final class BookOfObservancesScreen extends Screen {
	private static final int MARGIN = 16;
	private static final int PAD = 18;
	private static final int HEADER_HEIGHT = 62;
	private static final int SECTION_HEADER_HEIGHT = 52;
	private static final int CARD_HEIGHT = 69;
	private static final int GAP = 6;
	private static final int SCROLLBAR_WIDTH = 6;
	private static final int PANEL = 0xE00A171A;
	private static final int PANEL_LIGHT = 0xE015292C;
	private static final int PANEL_DONE = 0xE019302B;
	private static final int BORDER = 0xFF47787B;
	private static final int BORDER_MUTED = 0xFF29474A;
	private static final int TITLE = 0xFFE7F2E9;
	private static final int TEXT = 0xFFD1E0DA;
	private static final int MUTED = 0xFF829B96;
	private static final int ACTIVE = 0xFF9DCBC4;
	private static final int READY = 0xFFBCE69A;
	private static final int LOCKED = 0xFF72817D;

	private enum NpcSection {
		ACOLYTE("screen.hemomancy.book_of_observances.npc.acolyte",
				Hemomancy.rloc("textures/entity/unstained_acolyte/unstained_acolyte_portrait.png")),
		ZEALOT("screen.hemomancy.book_of_observances.npc.zealot",
				Hemomancy.rloc("textures/entity/unstained_zealot/unstained_zealot_portrait.png")),
		GUARDIAN("screen.hemomancy.book_of_observances.npc.guardian",
				Hemomancy.rloc("textures/entity/unstained_guardian/unstained_guardian_portrait.png"));

		private final String nameKey;
		private final ResourceLocation portrait;
		NpcSection(String nameKey, ResourceLocation portrait) {
			this.nameKey = nameKey;
			this.portrait = portrait;
		}
	}

	private record SectionHitbox(NpcSection section, int x, int y, int width, int height) {
		private boolean contains(double mouseX, double mouseY) {
			return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
		}
	}

	private final int acceptedMask;
	private final int claimedMask;
	private final int availableMask;
	private final int readyMask;
	private final float purity;
	private final float clarity;
	private final boolean clarityUnlocked;
	private final EnumSet<NpcSection> collapsedSections = EnumSet.noneOf(NpcSection.class);
	private final List<SectionHitbox> sectionHitboxes = new ArrayList<>();
	private int left;
	private int top;
	private int panelWidth;
	private int panelHeight;
	private int scrollOffset;
	private int maxScroll;

	private BookOfObservancesScreen(int acceptedMask, int claimedMask, int availableMask, int readyMask,
			float purity, float clarity, boolean clarityUnlocked) {
		super(Component.translatable("screen.hemomancy.book_of_observances.title"));
		this.acceptedMask = acceptedMask;
		this.claimedMask = claimedMask;
		this.availableMask = availableMask;
		this.readyMask = readyMask;
		this.purity = purity;
		this.clarity = clarity;
		this.clarityUnlocked = clarityUnlocked;
	}

	public static void open(int acceptedMask, int claimedMask, int availableMask, int readyMask,
			float purity, float clarity, boolean clarityUnlocked) {
		Minecraft.getInstance().setScreen(new BookOfObservancesScreen(acceptedMask, claimedMask,
				availableMask, readyMask, purity, clarity, clarityUnlocked));
	}

	@Override
	protected void init() {
		left = MARGIN;
		top = MARGIN;
		panelWidth = width - MARGIN * 2;
		panelHeight = height - MARGIN * 2;
		scrollOffset = 0;
		maxScroll = 0;
		clearWidgets();
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		graphics.fillGradient(left, top, left + panelWidth, top + panelHeight, 0xF00B1519, 0xF0112528);
		ScreenDrawUtils.drawBorder(graphics, left, top, panelWidth, panelHeight, BORDER, BORDER_MUTED);
		graphics.drawCenteredString(font, title, left + panelWidth / 2, top + 9, TITLE);

		int contentX = left + PAD;
		int contentY = top + 27;
		int contentW = panelWidth - PAD * 2;
		int contentBottom = top + panelHeight - PAD;
		graphics.fill(contentX - 8, contentY - 5, contentX + contentW + 8, contentBottom + 5, PANEL);
		ScreenDrawUtils.drawBorder(graphics, contentX - 8, contentY - 5, contentW + 16,
				contentBottom - contentY + 10, BORDER_MUTED, 0xFF102326);
		renderHeader(graphics, contentX, contentY, contentW, mouseX, mouseY);

		int listY = contentY + HEADER_HEIGHT;
		int listHeight = Math.max(20, contentBottom - listY);
		int listWidth = contentW - SCROLLBAR_WIDTH - 4;
		maxScroll = Math.max(0, totalContentHeight() - listHeight);
		scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
		sectionHitboxes.clear();
		graphics.enableScissor(contentX, listY, contentX + listWidth, listY + listHeight);
		renderSections(graphics, contentX, listY - scrollOffset, listWidth, mouseX, mouseY);
		graphics.disableScissor();
		renderScrollbar(graphics, contentX + contentW - SCROLLBAR_WIDTH, listY,
				SCROLLBAR_WIDTH, listHeight, totalContentHeight());
		super.render(graphics, mouseX, mouseY, partialTick);
	}

	private void renderHeader(GuiGraphics graphics, int x, int y, int width, int mouseX, int mouseY) {
		graphics.drawString(font, Component.translatable("screen.hemomancy.book_of_observances.header"),
				x, y + 2, ACTIVE, false);
		Component progress = Component.translatable("screen.hemomancy.book_of_observances.progress",
				Integer.bitCount(claimedMask), Observance.values().length);
		graphics.drawString(font, progress, x, y + 15, MUTED, false);
		Component humors = clarityUnlocked
				? Component.translatable("screen.hemomancy.book_of_observances.humors", rounded(purity), rounded(clarity))
				: Component.translatable("screen.hemomancy.book_of_observances.purity", rounded(purity));
		graphics.drawString(font, humors, x, y + 28, TEXT, false);
		drawButton(graphics, x, y + 42, 78, Component.translatable("screen.hemomancy.book_of_observances.collapse_all"),
				mouseX >= x && mouseX <= x + 78 && mouseY >= y + 42 && mouseY <= y + 57);
		drawButton(graphics, x + 84, y + 42, 72, Component.translatable("screen.hemomancy.book_of_observances.expand_all"),
				mouseX >= x + 84 && mouseX <= x + 156 && mouseY >= y + 42 && mouseY <= y + 57);
	}

	private void renderSections(GuiGraphics graphics, int x, int y, int width, int mouseX, int mouseY) {
		int cursorY = y;
		for (NpcSection section : NpcSection.values()) {
			List<Observance> observations = observationsFor(section);
			if (observations.isEmpty()) continue;
			boolean collapsed = collapsedSections.contains(section);
			renderSectionHeader(graphics, section, observations, x, cursorY, width, collapsed, mouseX, mouseY);
			sectionHitboxes.add(new SectionHitbox(section, x, cursorY, width, SECTION_HEADER_HEIGHT));
			cursorY += SECTION_HEADER_HEIGHT + GAP;
			if (!collapsed) {
				for (Observance observation : observations) {
					renderObservanceCard(graphics, observation, x + 12, cursorY, width - 12);
					cursorY += CARD_HEIGHT + GAP;
				}
			}
			cursorY += GAP;
		}
	}

	private void renderSectionHeader(GuiGraphics graphics, NpcSection section, List<Observance> observations,
			int x, int y, int width, boolean collapsed, int mouseX, int mouseY) {
		boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + SECTION_HEADER_HEIGHT;
		graphics.fill(x, y, x + width, y + SECTION_HEADER_HEIGHT, hovered ? 0xE020373A : PANEL_LIGHT);
		ScreenDrawUtils.drawBorder(graphics, x, y, width, SECTION_HEADER_HEIGHT, BORDER, BORDER_MUTED);
		renderPortrait(graphics, section.portrait, x + 3, y + 3, SECTION_HEADER_HEIGHT - 6);
		int completed = 0;
		for (Observance observation : observations) if (has(claimedMask, observation)) completed++;
		graphics.drawString(font, Component.translatable(section.nameKey), x + SECTION_HEADER_HEIGHT + 4, y + 10, TITLE, false);
		graphics.drawString(font, Component.translatable("screen.hemomancy.book_of_observances.directed_by"),
				x + SECTION_HEADER_HEIGHT + 4, y + 24, MUTED, false);
		graphics.drawString(font, Component.literal(completed + " / " + observations.size()),
				x + width - 42, y + 19, completed == observations.size() ? READY : ACTIVE, false);
		graphics.drawString(font, Component.literal(collapsed ? "+" : "−"), x + width - 14, y + 7, TEXT, false);
	}

	private void renderObservanceCard(GuiGraphics graphics, Observance observation, int x, int y, int width) {
		Status status = statusOf(observation);
		graphics.fill(x, y, x + width, y + CARD_HEIGHT, status == Status.COMPLETE ? PANEL_DONE : PANEL_LIGHT);
		ScreenDrawUtils.drawBorder(graphics, x, y, width, CARD_HEIGHT,
				status == Status.READY || status == Status.COMPLETE ? BORDER : BORDER_MUTED, 0xFF102326);
		graphics.drawString(font, Component.translatable(observation.translation("title")), x + 8, y + 6,
				status.color, false);
		graphics.drawString(font, Component.translatable(status.key), x + width - 92, y + 6, status.color, false);
		drawWrappedLine(graphics, Component.translatable(observation.translation("description")), x + 8, y + 19,
				width - 16, TEXT);
		graphics.drawString(font, Component.translatable(observation.translation("requirement")), x + 8, y + 41,
				status == Status.LOCKED ? LOCKED : MUTED, false);
		graphics.drawString(font, Component.translatable(observation.translation("reward")), x + 8, y + 53,
				status == Status.COMPLETE ? READY : MUTED, false);
	}

	private void drawWrappedLine(GuiGraphics graphics, Component text, int x, int y, int width, int color) {
		List<FormattedCharSequence> lines = font.split(text, width);
		if (!lines.isEmpty()) graphics.drawString(font, lines.get(0), x, y, color, false);
		if (lines.size() > 1) graphics.drawString(font, lines.get(1), x, y + 9, color, false);
	}

	private Status statusOf(Observance observation) {
		if (has(claimedMask, observation)) return Status.COMPLETE;
		if (!has(availableMask, observation)) return Status.LOCKED;
		if (!has(acceptedMask, observation)) return Status.AVAILABLE;
		if (has(readyMask, observation)) return Status.READY;
		return Status.ACTIVE;
	}

	private enum Status {
		LOCKED("screen.hemomancy.book_of_observances.status.locked", BookOfObservancesScreen.LOCKED),
		AVAILABLE("screen.hemomancy.book_of_observances.status.available", BookOfObservancesScreen.ACTIVE),
		ACTIVE("screen.hemomancy.book_of_observances.status.active", BookOfObservancesScreen.ACTIVE),
		READY("screen.hemomancy.book_of_observances.status.ready", BookOfObservancesScreen.READY),
		COMPLETE("screen.hemomancy.book_of_observances.status.complete", BookOfObservancesScreen.READY);
		private final String key;
		private final int color;
		Status(String key, int color) { this.key = key; this.color = color; }
	}

	private void renderPortrait(GuiGraphics graphics, ResourceLocation portrait, int x, int y, int size) {
		AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(portrait);
		texture.setBlurMipmap(false, false);
		RenderSystem.enableBlend();
		graphics.blit(portrait, x, y, size, size, 0f, 0f, 48, 48, 48, 48);
		texture.restoreLastBlurMipmap();
	}

	private void drawButton(GuiGraphics graphics, int x, int y, int width, Component label, boolean hovered) {
		graphics.fill(x, y, x + width, y + 15, hovered ? 0xFF315154 : 0xFF1B3538);
		ScreenDrawUtils.drawBorder(graphics, x, y, width, 15, BORDER_MUTED, 0xFF102326);
		graphics.drawCenteredString(font, label, x + width / 2, y + 4, hovered ? TITLE : MUTED);
	}

	private void renderScrollbar(GuiGraphics graphics, int x, int y, int width, int height, int totalHeight) {
		if (maxScroll <= 0) return;
		graphics.fill(x, y, x + width, y + height, 0xAA102326);
		int thumbHeight = Math.max(18, height * height / Math.max(height, totalHeight));
		int thumbY = y + (height - thumbHeight) * scrollOffset / maxScroll;
		graphics.fill(x + 1, thumbY, x + width - 1, thumbY + thumbHeight, BORDER);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			int x = left + PAD;
			int y = top + 27;
			if (mouseX >= x && mouseX <= x + 78 && mouseY >= y + 42 && mouseY <= y + 57) {
				for (NpcSection section : NpcSection.values()) if (!observationsFor(section).isEmpty()) collapsedSections.add(section);
				scrollOffset = 0;
				return true;
			}
			if (mouseX >= x + 84 && mouseX <= x + 156 && mouseY >= y + 42 && mouseY <= y + 57) {
				collapsedSections.clear();
				scrollOffset = 0;
				return true;
			}
			for (SectionHitbox hitbox : sectionHitboxes) {
				if (hitbox.contains(mouseX, mouseY)) {
					if (!collapsedSections.remove(hitbox.section)) collapsedSections.add(hitbox.section);
					return true;
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (mouseX >= left + PAD && mouseX <= left + panelWidth - PAD
				&& mouseY >= top + 27 + HEADER_HEIGHT && mouseY <= top + panelHeight - PAD) {
			scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) (scrollY * 18)));
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	private int totalContentHeight() {
		int height = 0;
		for (NpcSection section : NpcSection.values()) {
			int count = observationsFor(section).size();
			if (count == 0) continue;
			height += SECTION_HEADER_HEIGHT + GAP * 2;
			if (!collapsedSections.contains(section)) height += count * (CARD_HEIGHT + GAP);
		}
		return height;
	}

	private static List<Observance> observationsFor(NpcSection section) {
		List<Observance> result = new ArrayList<>();
		for (Observance observation : Observance.values()) if (observation.issuer().name().equals(section.name())) result.add(observation);
		return result;
	}

	private static boolean has(int mask, Observance observation) {
		return (mask & observation.mask()) != 0;
	}

	private static int rounded(float value) {
		return Math.round(value);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
