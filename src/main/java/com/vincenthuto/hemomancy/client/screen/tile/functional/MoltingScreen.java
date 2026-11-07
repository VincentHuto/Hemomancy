package com.vincenthuto.hemomancy.client.screen.tile.functional;

import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.VeinBackgroundRenderer;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.MoltingRules;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.heartnode.MoltRequestPacket;
import com.vincenthuto.hemomancy.common.network.heartnode.PacketOpenHeartNodeScreen;
import com.vincenthuto.hemomancy.common.world.FungalGardensPlaces;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Molting at the heart-node: the grown tendency is added up, a tenth is shed, and the rest is placed by hand across
 * the eight tendencies before the molt is sent. The server checks every rule again ({@link MoltingRules}).
 */
public final class MoltingScreen extends Screen {
	private static final int WIDTH = 236;
	private static final int HEIGHT = 200;
	private static final int ROWS_TOP = 40;
	private static final int ROW_HEIGHT = 14;
	private static final int STEP_BUTTON = 14;
	private static final int BIG_STEP = 10;

	private static final int FRAME_OUTER = 0xFF2A0A10;
	private static final int FRAME_INNER = 0xFF8C7F74;
	private static final int ACCENT = 0xFF9A2836;
	private static final int WELL = 0xC00B0909;
	private static final int WELL_EDGE = 0xFF4A403A;
	private static final int TITLE = 0xFFEDE4D8;
	private static final int TEXT = 0xFFE2D8CC;
	private static final int MUTED = 0xFF9C9188;
	private static final int READY = 0xFFB9D4A0;
	private static final int WARN = 0xFFCC8E92;

	private final VeinBackgroundRenderer veins = new VeinBackgroundRenderer(VeinBackgroundRenderer.Style.MYCELIAL);
	private final Screen parent;
	private final PacketOpenHeartNodeScreen state;
	private final float[] base;
	private final int pool;
	private final int[] allocation;
	private final long softUntil;
	private Button moltButton;
	private int left;
	private int top;

	public MoltingScreen(Screen parent, PacketOpenHeartNodeScreen state) {
		super(Component.translatable("screen.hemomancy.molting.title"));
		this.parent = parent;
		this.state = state;
		this.base = state.molt().baseArray();
		this.pool = MoltingRules.pool(base);
		this.allocation = MoltingRules.suggested(base, pool);
		this.softUntil = minecraftTime() + state.molt().softTicks();
	}

	private static long minecraftTime() {
		var level = net.minecraft.client.Minecraft.getInstance().level;
		return level == null ? 0L : level.getGameTime();
	}

	@Override
	protected void init() {
		left = (width - WIDTH) / 2;
		top = (height - HEIGHT) / 2;
		for (int i = 0; i < MoltingRules.TENDENCIES; i++) {
			int index = i;
			int y = rowY(i) - 2;
			addRenderableWidget(Button.builder(Component.translatable("screen.hemomancy.molting.less"), button -> step(index, -1))
					.bounds(left + WIDTH - 72, y, STEP_BUTTON, 12).build());
			addRenderableWidget(Button.builder(Component.translatable("screen.hemomancy.molting.more"), button -> step(index, 1))
					.bounds(left + WIDTH - 22, y, STEP_BUTTON, 12).build());
		}
		int buttonsY = top + HEIGHT - 24;
		int buttonWidth = (WIDTH - 24) / 3;
		addRenderableWidget(Button.builder(Component.translatable("screen.hemomancy.molting.current"), button -> {
					int[] current = MoltingRules.suggested(base, pool);
					System.arraycopy(current, 0, allocation, 0, current.length);
					refresh();
				}).tooltip(Tooltip.create(Component.translatable("screen.hemomancy.molting.current.tooltip")))
				.bounds(left + 8, buttonsY, buttonWidth, 16).build());
		addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> onClose())
				.bounds(left + 12 + buttonWidth, buttonsY, buttonWidth, 16).build());
		moltButton = addRenderableWidget(Button.builder(Component.translatable("screen.hemomancy.molting.molt"), button -> {
					PacketHandler.sendToServer(new MoltRequestPacket(state.pos(), allocation));
					minecraft.setScreen(null);
				}).tooltip(Tooltip.create(Component.translatable("screen.hemomancy.molting.molt.tooltip",
						(int) MoltingRules.SPORE_COST, Math.round((1.0D - MoltingRules.KEPT) * 100))))
				.bounds(left + 16 + buttonWidth * 2, buttonsY, buttonWidth, 16).build());
		refresh();
	}

	private int rowY(int index) {
		return top + ROWS_TOP + index * ROW_HEIGHT;
	}

	/** Moves points into or out of one tendency; shift moves ten at a time. Never below zero or past the pool. */
	public void step(int index, int direction) {
		int amount = hasShiftDown() ? BIG_STEP : 1;
		if (direction > 0) amount = Math.min(amount, remaining());
		else amount = -Math.min(amount, allocation[index]);
		allocation[index] += amount;
		refresh();
	}

	private int remaining() {
		int placed = 0;
		for (int value : allocation) placed += value;
		return pool - placed;
	}

	private MoltingRules.Refusal refusal() {
		boolean inGardens = minecraft != null && minecraft.player != null
				&& FungalGardensPlaces.isInGardens(minecraft.player.level(), minecraft.player.blockPosition());
		MoltingRules.Refusal refusal = MoltingRules.refusal(true, inGardens, state.molt().spores(), minecraftTime(),
				softUntil, pool);
		if (refusal != MoltingRules.Refusal.NONE) return refusal;
		return MoltingRules.placed(allocation, pool) ? MoltingRules.Refusal.NONE : MoltingRules.Refusal.UNPLACED;
	}

	private void refresh() {
		if (moltButton != null) moltButton.active = !state.readOnly() && refusal() == MoltingRules.Refusal.NONE;
	}

	@Override
	public void tick() {
		refresh();
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		veins.render(graphics, left, top, WIDTH, HEIGHT);
		ScreenDrawUtils.drawBorder(graphics, left, top, WIDTH, HEIGHT, FRAME_OUTER, FRAME_INNER);
		ScreenDrawUtils.drawCornerAccents(graphics, left, top, WIDTH, HEIGHT, 3, ACCENT);
		graphics.drawString(font, title, left + 8, top + 8, TITLE, false);

		int remaining = remaining();
		graphics.drawString(font, Component.translatable("screen.hemomancy.molting.pool", remaining, pool),
				left + 8, top + 22, remaining == 0 ? READY : TEXT, false);

		int wellTop = top + ROWS_TOP - 4;
		int wellHeight = MoltingRules.TENDENCIES * ROW_HEIGHT + 4;
		graphics.fill(left + 6, wellTop, left + WIDTH - 6, wellTop + wellHeight, WELL);
		ScreenDrawUtils.drawSimpleBorder(graphics, left + 6, wellTop, WIDTH - 12, wellHeight, WELL_EDGE);
		EnumBloodTendency[] tendencies = EnumBloodTendency.values();
		for (int i = 0; i < tendencies.length; i++) {
			int y = rowY(i);
			int swatch = 0xFF000000 | tendencies[i].getColor().getColor();
			graphics.fill(left + 11, y, left + 18, y + 7, WELL_EDGE);
			graphics.fill(left + 12, y + 1, left + 17, y + 6, swatch);
			graphics.drawString(font, Component.translatable("blood_tendency.hemomancy."
					+ tendencies[i].name().toLowerCase(Locale.ROOT)), left + 22, y, TEXT, false);
			graphics.drawString(font, Component.translatable("screen.hemomancy.molting.was",
					String.format(Locale.ROOT, "%.1f", base[i])), left + 92, y, MUTED, false);
			String value = Integer.toString(allocation[i]);
			int centre = left + WIDTH - 40;
			graphics.drawString(font, value, centre - font.width(value) / 2, y, TITLE, false);
		}

		MoltingRules.Refusal refusal = refusal();
		Component status = refusal == MoltingRules.Refusal.NONE
				? Component.translatable("screen.hemomancy.molting.ready")
				: refusal == MoltingRules.Refusal.SPORES
				? Component.translatable("screen.hemomancy.molting.status.spores", (int) state.molt().spores(),
						(int) MoltingRules.SPORE_COST)
				: refusal == MoltingRules.Refusal.SOFT
				? Component.translatable("screen.hemomancy.molting.status.soft", minutes(softUntil - minecraftTime()))
				: Component.translatable("screen.hemomancy.molting.status." + refusal.name().toLowerCase(Locale.ROOT));
		graphics.drawString(font, font.split(status, WIDTH - 16).getFirst(), left + 8, top + HEIGHT - 38,
				refusal == MoltingRules.Refusal.NONE ? READY : WARN, false);
		super.render(graphics, mouseX, mouseY, partialTick);
	}

	private static String minutes(long ticks) {
		long seconds = Math.max(0L, (ticks + 19) / 20);
		return String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60);
	}

	@Override
	public void onClose() {
		minecraft.setScreen(parent);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
