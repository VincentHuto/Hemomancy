package com.vincenthuto.hemomancy.client.screen.discovery;

import com.vincenthuto.hemomancy.client.screen.item.RiteHintScreen;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class DiscoveryInscriptionScreen extends Screen {
	private static final int WIDTH = 300;
	private static final int HEIGHT = 176;
	private static final int BG_REVEALED = 0xEE170A08;
	private static final int BG_OBSCURED = 0xEE111111;
	private static final int BORDER_REVEALED = 0xFF7A1F1F;
	private static final int BORDER_OBSCURED = 0xFF555555;
	private static final int TITLE_REVEALED = 0xFFE39A8A;
	private static final int TITLE_OBSCURED = 0xFF9AA0A6;
	private static final int TEXT_REVEALED = 0xFFD4C2B5;
	private static final int TEXT_OBSCURED = 0xFF8A8F94;

	private final List<String> lines;
	private final boolean revealed;
	@Nullable
	private final ResourceLocation riteId;
	private int left;
	private int top;
	private int scroll;

	private DiscoveryInscriptionScreen(String title, List<String> lines, boolean revealed,
			@Nullable ResourceLocation riteId) {
		super(Component.literal(title));
		this.lines = List.copyOf(lines);
		this.revealed = revealed;
		this.riteId = riteId;
	}

	public static void open(String title, List<String> lines, boolean revealed,
			@Nullable ResourceLocation riteId) {
		Minecraft.getInstance().setScreen(new DiscoveryInscriptionScreen(title, lines, revealed, riteId));
	}

	@Override
	protected void init() {
		this.left = (this.width - WIDTH) / 2;
		this.top = (this.height - HEIGHT) / 2;
		this.clearWidgets();
		if (revealed && riteId != null) {
			this.addRenderableWidget(Button.builder(
					Component.translatable("screen.hemomancy.discovery_inscription.open_rite"),
					button -> RiteHintScreen.open(riteId))
				.bounds(left + WIDTH - 96, top + HEIGHT - 28, 82, 20)
				.build());
		}
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
	}

	@Override
	public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
		int bg = revealed ? BG_REVEALED : BG_OBSCURED;
		int border = revealed ? BORDER_REVEALED : BORDER_OBSCURED;
		int titleColor = revealed ? TITLE_REVEALED : TITLE_OBSCURED;
		int textColor = revealed ? TEXT_REVEALED : TEXT_OBSCURED;

		gfx.fill(left, top, left + WIDTH, top + HEIGHT, bg);
		ScreenDrawUtils.drawBorder(gfx, left, top, WIDTH, HEIGHT, border, 0xFF211817);
		gfx.drawCenteredString(font, this.title, left + WIDTH / 2, top + 13, titleColor);
		gfx.fill(left + 18, top + 29, left + WIDTH - 18, top + 30, border);

		int contentLeft = left + 20;
		int contentTop = top + 40;
		int contentWidth = WIDTH - 40;
		int contentBottom = top + HEIGHT - (revealed && riteId != null ? 36 : 18);
		List<FormattedCharSequence> wrapped = wrappedLines(contentWidth);
		int lineHeight = 11;
		int maxScroll = Math.max(0, wrapped.size() * lineHeight - (contentBottom - contentTop));
		if (scroll > maxScroll) scroll = maxScroll;

		gfx.enableScissor(contentLeft - 2, contentTop, contentLeft + contentWidth + 2, contentBottom);
		int y = contentTop - scroll;
		for (FormattedCharSequence line : wrapped) {
			gfx.drawString(font, line, contentLeft, y, textColor);
			y += lineHeight;
		}
		gfx.disableScissor();

		super.render(gfx, mouseX, mouseY, partialTick);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		scroll = Math.max(0, scroll - (int) (scrollY * 12));
		return true;
	}

	private List<FormattedCharSequence> wrappedLines(int width) {
		List<FormattedCharSequence> wrapped = new ArrayList<>();
		for (String line : lines) {
			if (line == null || line.isBlank()) {
				wrapped.add(FormattedCharSequence.EMPTY);
				continue;
			}
			wrapped.addAll(font.split(Component.literal(line), width));
		}
		return wrapped;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

}
