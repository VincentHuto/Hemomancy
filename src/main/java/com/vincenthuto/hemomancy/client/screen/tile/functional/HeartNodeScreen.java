package com.vincenthuto.hemomancy.client.screen.tile.functional;

import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.VeinBackgroundRenderer;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.heartnode.HeartNodeSettingsPacket;
import com.vincenthuto.hemomancy.common.network.heartnode.PacketOpenHeartNodeScreen;
import com.vincenthuto.hemomancy.common.world.HeartNodeFootprint;
import com.vincenthuto.hemomancy.common.world.HeartNodeRules;
import com.vincenthuto.hemomancy.common.world.HeartNodeSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/**
 * The owner's control panel for their heart-node: a top-down map of the sanctum, its state, and how it shows in the
 * world. Each change is sent at once (at most every quarter second), so the world behind the panel follows it; nothing
 * here touches the sanctum's rules. A creative operator viewing someone else's node sees it read-only.
 */
public final class HeartNodeScreen extends Screen {
	private static final int WIDTH = 300;
	private static final int HEIGHT = 212;
	private static final int MAP = 120;
	private static final int PANEL_X = 140;
	private static final int PANEL_WIDTH = WIDTH - PANEL_X - 8;
	private static final int TOGGLE_TOP = 152;
	private static final int CELL_WIDTH = 92;
	private static final int CELL_HEIGHT = 16;
	/** One send per quarter second while toggles are flicked. */
	private static final int SEND_TICKS = 5;
	/** The node's heartbeat on the map, matching the spread's pulse. */
	private static final int HEARTBEAT_TICKS = HeartNodeRules.HEARTBEAT_TICKS;
	private static final int HOVER_RADIUS = 4;

	private static final int[] TOGGLES = {HeartNodeSettings.SPREAD, HeartNodeSettings.EDGE, HeartNodeSettings.THREADS,
			HeartNodeSettings.PULSE, HeartNodeSettings.MOTES, HeartNodeSettings.SHOW_STRANGERS,
			HeartNodeSettings.EDGE_WARNING};
	private static final String[] TOGGLE_KEYS = {"spread", "edge", "threads", "pulse", "motes", "show_strangers",
			"edge_warning"};

	private static final int FRAME_OUTER = 0xFF2A0A10;
	private static final int FRAME_INNER = 0xFF8C7F74;
	private static final int ACCENT = 0xFF9A2836;
	private static final int WELL = 0xC00B0909;
	private static final int WELL_EDGE = 0xFF4A403A;
	private static final int TITLE = 0xFFEDE4D8;
	private static final int TEXT = 0xFFE2D8CC;
	private static final int MUTED = 0xFF9C9188;
	private static final int HEADING = 0xFFCC8E92;
	private static final int FELT = 0x34D8CEC2;
	private static final int EDGE_LINE = 0xC0D8CEC2;
	private static final int THREAD = 0xFFB0303F;
	private static final int THREAD_OFF = 0x60B0303F;
	private static final int HEART = 0xFFC2283A;
	private static final int SYNAPSE = 0xFFEDE4D8;
	private static final int STRANDED = 0xFF7C726A;
	private static final int CACHE = 0xFFD9B98A;
	private static final int PLAYER = 0xFFFFFFFF;

	private final PacketOpenHeartNodeScreen state;
	private final HeartNodeFootprint footprint;
	private final VeinBackgroundRenderer veins = new VeinBackgroundRenderer(VeinBackgroundRenderer.Style.MYCELIAL);
	private final Button[] toggleButtons = new Button[TOGGLES.length];
	private HeartNodeSettings settings;
	private HeartNodeSettings sent;
	private int ticksSinceSend = SEND_TICKS;
	private int left;
	private int top;
	/** The map's projection for this frame: pixels per block, the envelope's centre and where it lands. */
	private double mapScale;
	private double mapCentreX;
	private double mapCentreZ;
	private int mapOriginX;
	private int mapOriginY;

	public HeartNodeScreen(PacketOpenHeartNodeScreen state) {
		super(Component.translatable("screen.hemomancy.heart_node.title", state.ownerName()));
		this.state = state;
		this.footprint = new HeartNodeFootprint(state.owner(), state.pos(),
				state.synapses().stream().map(PacketOpenHeartNodeScreen.Synapse::pos).toList());
		this.settings = state.settings();
		this.sent = state.settings();
	}

	public static void open(PacketOpenHeartNodeScreen state) {
		Minecraft.getInstance().setScreen(new HeartNodeScreen(state));
	}

	public HeartNodeSettings settings() {
		return settings;
	}

	@Override
	protected void init() {
		left = (width - WIDTH) / 2;
		top = (height - HEIGHT) / 2;
		for (int i = 0; i < TOGGLES.length; i++) {
			int index = i;
			String key = "screen.hemomancy.heart_node." + TOGGLE_KEYS[i];
			toggleButtons[i] = addRenderableWidget(Button.builder(toggleLabel(i), button -> flip(index))
					.tooltip(Tooltip.create(Component.translatable(key + ".tooltip")))
					.bounds(cellX(i), cellY(i), CELL_WIDTH, CELL_HEIGHT).build());
		}
		addRenderableWidget(new IntensitySlider(cellX(7), cellY(7)));
		addRenderableWidget(Button.builder(Component.translatable("screen.hemomancy.heart_node.reset"), button -> {
					settings = HeartNodeSettings.DEFAULT;
					changed();
					rebuildWidgets();
				}).tooltip(Tooltip.create(Component.translatable("screen.hemomancy.heart_node.reset.tooltip")))
				.bounds(cellX(8), cellY(8), CELL_WIDTH, CELL_HEIGHT).build());
		if (state.readOnly()) children().forEach(child -> {
			if (child instanceof net.minecraft.client.gui.components.AbstractWidget widget) widget.active = false;
		});
	}

	/** Flips one toggle, as a click on its button does. */
	public void flip(int index) {
		if (state.readOnly()) return;
		settings = settings.with(TOGGLES[index], !settings.has(TOGGLES[index]));
		toggleButtons[index].setMessage(toggleLabel(index));
		changed();
	}

	private Component toggleLabel(int index) {
		return Component.translatable("screen.hemomancy.heart_node.toggle",
				Component.translatable("screen.hemomancy.heart_node." + TOGGLE_KEYS[index]),
				settings.has(TOGGLES[index]) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
	}

	private int cellX(int index) {
		return left + 8 + index % 3 * (CELL_WIDTH + 4);
	}

	private int cellY(int index) {
		return top + TOGGLE_TOP + index / 3 * (CELL_HEIGHT + 2);
	}

	private void changed() {
		if (ticksSinceSend >= SEND_TICKS) send();
	}

	private void send() {
		if (state.readOnly() || settings.equals(sent)) return;
		PacketHandler.sendToServer(new HeartNodeSettingsPacket(state.pos(), settings));
		sent = settings;
		ticksSinceSend = 0;
	}

	@Override
	public void tick() {
		ticksSinceSend++;
		if (ticksSinceSend >= SEND_TICKS) send();
	}

	@Override
	public void removed() {
		send();
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		// No blur: the world behind the panel shows each change as it is made.
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		veins.render(graphics, left, top, WIDTH, HEIGHT);
		ScreenDrawUtils.drawBorder(graphics, left, top, WIDTH, HEIGHT, FRAME_OUTER, FRAME_INNER);
		ScreenDrawUtils.drawCornerAccents(graphics, left, top, WIDTH, HEIGHT, 3, ACCENT);
		graphics.drawString(font, title, left + 8, top + 8, TITLE, false);
		if (state.readOnly()) {
			Component viewing = Component.translatable("screen.hemomancy.heart_node.read_only");
			graphics.drawString(font, viewing, left + WIDTH - 8 - font.width(viewing), top + 8, MUTED, false);
		}

		int mapX = left + 8, mapY = top + 22;
		well(graphics, mapX, mapY, MAP, MAP);
		well(graphics, left + PANEL_X, mapY, PANEL_WIDTH, MAP);
		List<Component> tooltip = new ArrayList<>();
		graphics.enableScissor(mapX + 1, mapY + 1, mapX + MAP - 1, mapY + MAP - 1);
		drawMap(graphics, mapX, mapY, mouseX, mouseY, partialTick, tooltip);
		graphics.disableScissor();
		drawState(graphics, left + PANEL_X + 5, mapY + 5, mouseX, mouseY, tooltip);

		super.render(graphics, mouseX, mouseY, partialTick);
		if (!tooltip.isEmpty()) graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
	}

	private void well(GuiGraphics graphics, int x, int y, int w, int h) {
		graphics.fill(x, y, x + w, y + h, WELL);
		ScreenDrawUtils.drawSimpleBorder(graphics, x, y, w, h, WELL_EDGE);
	}

	// The map: north up, the whole envelope centred and scaled to fit.

	private void drawMap(GuiGraphics graphics, int mapX, int mapY, int mouseX, int mouseY, float partialTick,
			List<Component> tooltip) {
		BlockPos heart = state.pos();
		double minX = heart.getX() - HeartNodeRules.HEART_RADIUS, maxX = heart.getX() + HeartNodeRules.HEART_RADIUS;
		double minZ = heart.getZ() - HeartNodeRules.HEART_RADIUS, maxZ = heart.getZ() + HeartNodeRules.HEART_RADIUS;
		for (PacketOpenHeartNodeScreen.Synapse synapse : state.synapses()) {
			minX = Math.min(minX, synapse.pos().getX() - HeartNodeRules.SYNAPSE_RADIUS);
			maxX = Math.max(maxX, synapse.pos().getX() + HeartNodeRules.SYNAPSE_RADIUS);
			minZ = Math.min(minZ, synapse.pos().getZ() - HeartNodeRules.SYNAPSE_RADIUS);
			maxZ = Math.max(maxZ, synapse.pos().getZ() + HeartNodeRules.SYNAPSE_RADIUS);
		}
		mapScale = (MAP - 14.0D) / Math.max(maxX - minX, maxZ - minZ);
		mapCentreX = (minX + maxX) / 2.0D;
		mapCentreZ = (minZ + maxZ) / 2.0D;
		mapOriginX = mapX + MAP / 2;
		mapOriginY = mapY + MAP / 2;
		double scale = mapScale;
		int cx = (int) mapX(heart), cy = (int) mapY(heart);
		List<double[]> lobes = new ArrayList<>();
		lobes.add(new double[] {mapX(heart), mapY(heart), HeartNodeRules.HEART_RADIUS * scale});
		for (PacketOpenHeartNodeScreen.Synapse synapse : state.synapses()) {
			lobes.add(new double[] {mapX(synapse.pos()), mapY(synapse.pos()),
					HeartNodeRules.SYNAPSE_RADIUS * scale});
		}
		for (double[] lobe : lobes) fillDisc(graphics, lobe[0], lobe[1], lobe[2], FELT);
		drawEnvelopeEdge(graphics, lobes);

		// Threads, heart outward.
		for (PacketOpenHeartNodeScreen.Synapse synapse : state.synapses()) {
			BlockPos from = footprint.chainedFrom(synapse.pos());
			if (from == null) continue;
			int x0 = (int) mapX(from), y0 = (int) mapY(from);
			int x3 = (int) mapX(synapse.pos()), y3 = (int) mapY(synapse.pos());
			int bend = (synapse.pos().hashCode() & 7) - 3;
			int mx = (x0 + x3) / 2 - (y3 - y0) * bend / 16, my = (y0 + y3) / 2 + (x3 - x0) * bend / 16;
			ScreenDrawUtils.drawCubicTrace(graphics, x0, y0, mx, my, mx, my, x3, y3, 1,
					settings.has(HeartNodeSettings.THREADS) ? THREAD : THREAD_OFF);
		}

		// The heart beats on the node's clock.
		float time = minecraft.level == null ? 0f : AnimationTime.gameTime(minecraft.level, partialTick);
		float beat = (time % HEARTBEAT_TICKS) / HEARTBEAT_TICKS;
		float swell = beat < 0.2f ? 1f - beat / 0.2f : 0f;
		fillDisc(graphics, cx + 0.5D, cy + 0.5D, 3.0D + swell * 2.0D, (int) (0x50 * swell + 0x30) << 24 | (HEART & 0xFFFFFF));
		graphics.fill(cx - 1, cy - 1, cx + 2, cy + 2, HEART);
		if (near(mouseX, mouseY, cx, cy)) {
			tooltip.add(Component.translatable("screen.hemomancy.heart_node.map.heart"));
			tooltip.add(coords(state.pos()).withStyle(style -> style.withColor(MUTED)));
		}

		for (PacketOpenHeartNodeScreen.Synapse synapse : state.synapses()) {
			int x = (int) mapX(synapse.pos()), y = (int) mapY(synapse.pos());
			int colour = synapse.connected() ? SYNAPSE : STRANDED;
			graphics.fill(x - 1, y - 1, x + 1, y + 1, colour);
			ScreenDrawUtils.drawSimpleBorder(graphics, x - 2, y - 2, 4, 4, colour & 0x80FFFFFF);
			if (near(mouseX, mouseY, x, y)) synapseTooltip(synapse, tooltip);
		}

		for (BlockPos cache : state.caches()) {
			int x = (int) mapX(cache), y = (int) mapY(cache);
			graphics.fill(x - 1, y, x + 2, y + 1, CACHE);
			graphics.fill(x, y - 1, x + 1, y + 2, CACHE);
			if (near(mouseX, mouseY, x, y)) {
				tooltip.add(Component.translatable("screen.hemomancy.heart_node.map.cache"));
				tooltip.add(coords(cache).withStyle(style -> style.withColor(MUTED)));
			}
		}

		if (minecraft.player != null) {
			double px = mapOriginX + (minecraft.player.getX() - 0.5D - mapCentreX) * scale;
			double py = mapOriginY + (minecraft.player.getZ() - 0.5D - mapCentreZ) * scale;
			int x = Mth.clamp((int) px, mapX + 3, mapX + MAP - 4), y = Mth.clamp((int) py, mapY + 3, mapY + MAP - 4);
			boolean onMap = x == (int) px && y == (int) py;
			int alpha = (int) (160 + 95 * Mth.sin(time * 0.25f));
			graphics.fill(x - 1, y - 1, x + 1, y + 1, (onMap ? alpha : alpha / 2) << 24 | (PLAYER & 0xFFFFFF));
			if (near(mouseX, mouseY, x, y)) tooltip.add(Component.translatable("screen.hemomancy.heart_node.map.you"));
		}

		Component north = Component.translatable("screen.hemomancy.heart_node.north");
		graphics.drawString(font, north, mapOriginX - font.width(north) / 2 + 1, mapY + 3, MUTED, false);
	}

	/** The lobes' outline, leaving out the arcs that fall inside another lobe, so it traces one envelope. */
	private static void drawEnvelopeEdge(GuiGraphics graphics, List<double[]> lobes) {
		for (double[] lobe : lobes) {
			int steps = Math.max(24, (int) (lobe[2] * 7));
			for (int i = 0; i < steps; i++) {
				double angle = Math.PI * 2.0D * i / steps;
				double x = lobe[0] + Math.cos(angle) * lobe[2], y = lobe[1] + Math.sin(angle) * lobe[2];
				boolean inner = false;
				for (double[] other : lobes) {
					if (other != lobe && Mth.lengthSquared(x - other[0], y - other[1]) < other[2] * other[2] - 1.0D) {
						inner = true;
						break;
					}
				}
				if (!inner) graphics.fill((int) x, (int) y, (int) x + 1, (int) y + 1, EDGE_LINE);
			}
		}
	}

	private static void fillDisc(GuiGraphics graphics, double cx, double cy, double radius, int colour) {
		int r = (int) Math.ceil(radius);
		for (int dy = -r; dy <= r; dy++) {
			double half = radius * radius - dy * dy;
			if (half < 0) continue;
			int span = (int) Math.sqrt(half);
			graphics.fill((int) cx - span, (int) cy + dy, (int) cx + span + 1, (int) cy + dy + 1, colour);
		}
	}

	private double mapX(BlockPos pos) {
		return mapOriginX + (pos.getX() - mapCentreX) * mapScale;
	}

	private double mapY(BlockPos pos) {
		return mapOriginY + (pos.getZ() - mapCentreZ) * mapScale;
	}

	private static boolean near(int mouseX, int mouseY, int x, int y) {
		return Math.abs(mouseX - x) <= HOVER_RADIUS && Math.abs(mouseY - y) <= HOVER_RADIUS;
	}

	private void synapseTooltip(PacketOpenHeartNodeScreen.Synapse synapse, List<Component> tooltip) {
		tooltip.add(Component.translatable("screen.hemomancy.heart_node.map.synapse"));
		tooltip.add(coords(synapse.pos()).withStyle(style -> style.withColor(MUTED)));
		Direction facing = synapse.facing() < 0 || synapse.facing() >= Direction.values().length ? null
				: Direction.from3DDataValue(synapse.facing());
		tooltip.add(facing == null ? Component.translatable("screen.hemomancy.heart_node.map.facing.unknown")
				: Component.translatable("screen.hemomancy.heart_node.map.facing",
						Component.translatable("screen.hemomancy.heart_node.direction." + facing.getSerializedName())));
		tooltip.add(Component.translatable(synapse.connected() ? "screen.hemomancy.heart_node.map.chained"
				: "screen.hemomancy.heart_node.map.cut_off"));
	}

	private static net.minecraft.network.chat.MutableComponent coords(BlockPos pos) {
		return Component.translatable("screen.hemomancy.heart_node.coords", pos.getX(), pos.getY(), pos.getZ());
	}

	// The state panel.

	private void drawState(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, List<Component> tooltip) {
		long now = minecraft.level == null ? state.plantedAt() : minecraft.level.getGameTime();
		int stage = HeartNodeRules.stageAt(state.plantedAt(), now);
		long wait = HeartNodeRules.ticksToNextStage(state.plantedAt(), now);
		long seconds = (wait + 19) / 20;
		int line = 0;
		line = text(graphics, stage >= HeartNodeRules.MATURE_STAGE
				? Component.translatable("screen.hemomancy.heart_node.stage.mature", HeartNodeRules.MATURE_STAGE)
				: Component.translatable("screen.hemomancy.heart_node.stage", stage, HeartNodeRules.MATURE_STAGE,
						String.format("%d:%02d", seconds / 60, seconds % 60)), x, y, line, TEXT);
		line = text(graphics, Component.translatable("screen.hemomancy.heart_node.synapses", state.synapses().size(),
				HeartNodeRules.SYNAPSE_BUDGET), x, y, line, TEXT);
		line = text(graphics, Component.translatable("screen.hemomancy.heart_node.reach",
				(int) Math.round(footprint.maxRadius())), x, y, line, TEXT);
		line = text(graphics, Component.translatable("screen.hemomancy.heart_node.respawn",
				state.respawnHere() ? CommonComponents.GUI_YES : CommonComponents.GUI_NO), x, y, line, TEXT);
		int cacheLine = line;
		line = text(graphics, Component.translatable("screen.hemomancy.heart_node.caches", state.caches().size()),
				x, y, line, state.caches().isEmpty() ? MUTED : TEXT);
		if (!state.caches().isEmpty() && mouseX >= x && mouseX < x + PANEL_WIDTH - 10
				&& mouseY >= y + cacheLine * 10 && mouseY < y + cacheLine * 10 + 10) {
			tooltip.add(Component.translatable("screen.hemomancy.heart_node.map.cache"));
			for (BlockPos cache : state.caches()) tooltip.add(coords(cache).withStyle(style -> style.withColor(MUTED)));
		}

		line++;
		line = text(graphics, Component.translatable("screen.hemomancy.heart_node.benefits"), x, y, line, HEADING);
		line = text(graphics, Component.translatable("screen.hemomancy.heart_node.benefit.regeneration"), x, y, line, TEXT);
		line = text(graphics, Component.translatable("screen.hemomancy.heart_node.benefit.spores",
				(int) HeartNodeRules.BONUS_SPORES, HeartNodeRules.BONUS_TICKS / 20), x, y, line, TEXT);
		line = text(graphics, Component.translatable("screen.hemomancy.heart_node.benefit.taper",
				(int) Math.round((1.0D - HeartNodeRules.EDGE_FALLOFF) * 100)), x, y, line, MUTED);
		if (minecraft.player != null) {
			double here = footprint.strength(minecraft.player.blockPosition());
			text(graphics, here > 0.0D
					? Component.translatable("screen.hemomancy.heart_node.here", (int) Math.round(here * 100))
					: Component.translatable("screen.hemomancy.heart_node.here.outside"), x, y, line, MUTED);
		}
	}

	private int text(GuiGraphics graphics, Component text, int x, int y, int line, int colour) {
		graphics.drawString(font, font.split(text, PANEL_WIDTH - 10).getFirst(), x, y + line * 10, colour, false);
		return line + 1;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	/** The spread's intensity in five notches, from a hint to a carpet. */
	private final class IntensitySlider extends AbstractSliderButton {
		IntensitySlider(int x, int y) {
			super(x, y, CELL_WIDTH, CELL_HEIGHT, Component.empty(),
					settings.intensity() / (double) HeartNodeSettings.MAX_INTENSITY);
			setTooltip(Tooltip.create(Component.translatable("screen.hemomancy.heart_node.intensity.tooltip")));
			updateMessage();
		}

		private int notch() {
			return (int) Math.round(value * HeartNodeSettings.MAX_INTENSITY);
		}

		@Override
		protected void updateMessage() {
			setMessage(Component.translatable("screen.hemomancy.heart_node.intensity",
					Component.translatable("screen.hemomancy.heart_node.intensity." + notch())));
		}

		@Override
		protected void applyValue() {
			int notch = notch();
			value = notch / (double) HeartNodeSettings.MAX_INTENSITY;
			if (notch == settings.intensity() || state.readOnly()) return;
			settings = settings.withIntensity(notch);
			changed();
		}
	}
}
