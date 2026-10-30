package com.vincenthuto.hemomancy.client.screen.item;

import com.vincenthuto.hemomancy.client.data.FaneBoundaryClientData;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hemomancy.common.event.worldevent.FaneFootprint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The Bloodline Pool screen's top-down map of the bloodline's Founding Fane: the bloodwell's lobe, each Hematic Stake's
 * lobe and the chain between them, other fanes in sight, and where the viewer stands. It reads only the boundary data
 * the fane sync already sends (fanes within sight of the viewer), so it never asks the server for anything.
 */
final class FaneMapPanel {
	private static final int HOVER_RADIUS = 4;
	/** The bloodwell's pulse on the map. */
	private static final int HEARTBEAT_TICKS = 40;

	private static final int WELL = 0xC0080203;
	private static final int WELL_EDGE = 0xFF6A1218;
	private static final int HEADING = 0xFFFF8A8A;
	private static final int TEXT = 0xFFFFD9D0;
	private static final int MUTED = 0xFFAA7A7A;
	private static final int FELT = 0x30D32327;
	private static final int EDGE_LINE = 0xC0FF6A75;
	private static final int OTHER_EDGE = 0x70B0A090;
	private static final int THREAD = 0xFFB0303F;
	private static final int HEART = 0xFFFF3A48;
	private static final int STAKE = 0xFFFFD9D0;
	private static final int STRANDED = 0xFF7C6A6A;
	private static final int PLAYER = 0xFFFFFFFF;

	private final Font font;
	private double scale;
	private double centreX;
	private double centreZ;
	private int originX;
	private int originY;

	FaneMapPanel(Font font) {
		this.font = font;
	}

	/**
	 * Draws the panel: a heading, the square map, then the fane's figures below it. Tooltip lines for whatever the
	 * mouse is over are added to {@code tooltip}.
	 */
	void render(GuiGraphics graphics, int x, int y, int width, int height, UUID owner, int mouseX, int mouseY,
			float partialTick, List<Component> tooltip) {
		graphics.fill(x, y, x + width, y + height, WELL);
		ScreenDrawUtils.drawSimpleBorder(graphics, x, y, width, height, WELL_EDGE);
		graphics.drawString(font, Component.translatable("screen.hemomancy.bloodline_pool.map.title"), x + 5, y + 5,
				HEADING, true);

		int map = width - 10;
		int mapX = x + 5, mapY = y + 17;
		graphics.fill(mapX, mapY, mapX + map, mapY + map, 0xFF050101);
		ScreenDrawUtils.drawSimpleBorder(graphics, mapX, mapY, map, map, WELL_EDGE);

		FaneBoundaryClientData.Entry own = null;
		List<FaneBoundaryClientData.Entry> others = new ArrayList<>();
		for (FaneBoundaryClientData.Entry entry : FaneBoundaryClientData.entries()) {
			if (owner != null && owner.equals(entry.ownerUuid()) && own == null) own = entry;
			else others.add(entry);
		}

		int textY = mapY + map + 5;
		if (own == null) {
			Component none = Component.translatable("screen.hemomancy.bloodline_pool.map.none");
			List<net.minecraft.util.FormattedCharSequence> lines = font.split(none, map - 8);
			int lineY = mapY + map / 2 - lines.size() * 5;
			for (net.minecraft.util.FormattedCharSequence line : lines) {
				graphics.drawString(font, line, mapX + (map - font.width(line)) / 2, lineY, MUTED, false);
				lineY += 10;
			}
			text(graphics, Component.translatable("screen.hemomancy.bloodline_pool.map.none.hint"), x + 5, textY,
					width - 10, MUTED);
			return;
		}

		FaneFootprint footprint = new FaneFootprint(own.heart(), own.stakes(), null);
		graphics.enableScissor(mapX + 1, mapY + 1, mapX + map - 1, mapY + map - 1);
		drawMap(graphics, own, footprint, others, mapX, mapY, map, mouseX, mouseY, partialTick, tooltip);
		graphics.disableScissor();

		int line = 0;
		line += text(graphics, Component.translatable("screen.hemomancy.bloodline_pool.map.stakes", own.stakes().size()),
				x + 5, textY + line * 10, width - 10, TEXT);
		line += text(graphics, Component.translatable("screen.hemomancy.bloodline_pool.map.reach",
				(int) Math.round(footprint.maxRadius())), x + 5, textY + line * 10, width - 10, TEXT);
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player != null) {
			double here = footprint.effectStrength(minecraft.player.blockPosition());
			line += text(graphics, here > 0.0D
					? Component.translatable("screen.hemomancy.bloodline_pool.map.here", (int) Math.round(here * 100))
					: Component.translatable("screen.hemomancy.bloodline_pool.map.here.outside"),
					x + 5, textY + line * 10, width - 10, MUTED);
		}
		if (!others.isEmpty()) {
			text(graphics, Component.translatable("screen.hemomancy.bloodline_pool.map.others", others.size()),
					x + 5, textY + line * 10, width - 10, MUTED);
		}
	}

	private void drawMap(GuiGraphics graphics, FaneBoundaryClientData.Entry own, FaneFootprint footprint,
			List<FaneBoundaryClientData.Entry> others, int mapX, int mapY, int map, int mouseX, int mouseY,
			float partialTick, List<Component> tooltip) {
		BlockPos heart = own.heart();
		double heartRadius = FaneFootprint.HEART_RADIUS, stakeRadius = own.radius();
		double minX = heart.getX() - heartRadius, maxX = heart.getX() + heartRadius;
		double minZ = heart.getZ() - heartRadius, maxZ = heart.getZ() + heartRadius;
		for (BlockPos stake : own.stakes()) {
			minX = Math.min(minX, stake.getX() - stakeRadius);
			maxX = Math.max(maxX, stake.getX() + stakeRadius);
			minZ = Math.min(minZ, stake.getZ() - stakeRadius);
			maxZ = Math.max(maxZ, stake.getZ() + stakeRadius);
		}
		scale = (map - 14.0D) / Math.max(maxX - minX, maxZ - minZ);
		centreX = (minX + maxX) / 2.0D;
		centreZ = (minZ + maxZ) / 2.0D;
		originX = mapX + map / 2;
		originY = mapY + map / 2;

		// Neighbouring fanes first, faint, so the bloodline's own reads on top where they meet.
		for (FaneBoundaryClientData.Entry other : others) {
			drawEnvelopeEdge(graphics, lobes(other.heart(), other.stakes(), heartRadius, other.radius()), OTHER_EDGE);
		}

		List<double[]> lobes = lobes(heart, own.stakes(), heartRadius, stakeRadius);
		for (double[] lobe : lobes) fillDisc(graphics, lobe[0], lobe[1], lobe[2], FELT);
		drawEnvelopeEdge(graphics, lobes, EDGE_LINE);

		// The chain, bloodwell outward.
		for (BlockPos stake : own.stakes()) {
			BlockPos from = footprint.chainedFrom(stake);
			if (from == null) continue;
			int x0 = (int) mapX(from), y0 = (int) mapY(from);
			int x3 = (int) mapX(stake), y3 = (int) mapY(stake);
			int bend = (stake.hashCode() & 7) - 3;
			int mx = (x0 + x3) / 2 - (y3 - y0) * bend / 16, my = (y0 + y3) / 2 + (x3 - x0) * bend / 16;
			ScreenDrawUtils.drawCubicTrace(graphics, x0, y0, mx, my, mx, my, x3, y3, 1, THREAD);
		}

		Minecraft minecraft = Minecraft.getInstance();
		float time = minecraft.level == null ? 0f : AnimationTime.gameTime(minecraft.level, partialTick);
		float beat = (time % HEARTBEAT_TICKS) / HEARTBEAT_TICKS;
		float swell = beat < 0.2f ? 1f - beat / 0.2f : 0f;
		int cx = (int) mapX(heart), cy = (int) mapY(heart);
		fillDisc(graphics, cx + 0.5D, cy + 0.5D, 3.0D + swell * 2.0D,
				(int) (0x50 * swell + 0x30) << 24 | (HEART & 0xFFFFFF));
		graphics.fill(cx - 1, cy - 1, cx + 2, cy + 2, HEART);
		if (near(mouseX, mouseY, cx, cy)) {
			tooltip.add(Component.translatable("screen.hemomancy.bloodline_pool.map.heart"));
			tooltip.add(coords(heart).withStyle(style -> style.withColor(MUTED)));
		}

		for (BlockPos stake : own.stakes()) {
			int x = (int) mapX(stake), y = (int) mapY(stake);
			boolean chained = footprint.chainedFrom(stake) != null;
			int colour = chained ? STAKE : STRANDED;
			graphics.fill(x - 1, y - 1, x + 1, y + 1, colour);
			ScreenDrawUtils.drawSimpleBorder(graphics, x - 2, y - 2, 4, 4, colour & 0x80FFFFFF);
			if (near(mouseX, mouseY, x, y)) {
				tooltip.add(Component.translatable("screen.hemomancy.bloodline_pool.map.stake"));
				tooltip.add(coords(stake).withStyle(style -> style.withColor(MUTED)));
				tooltip.add(Component.translatable(chained ? "screen.hemomancy.bloodline_pool.map.chained"
						: "screen.hemomancy.bloodline_pool.map.cut_off"));
			}
		}

		if (minecraft.player != null) {
			double px = originX + (minecraft.player.getX() - 0.5D - centreX) * scale;
			double py = originY + (minecraft.player.getZ() - 0.5D - centreZ) * scale;
			int x = Mth.clamp((int) px, mapX + 3, mapX + map - 4), y = Mth.clamp((int) py, mapY + 3, mapY + map - 4);
			boolean onMap = x == (int) px && y == (int) py;
			int alpha = (int) (160 + 95 * Mth.sin(time * 0.25f));
			graphics.fill(x - 1, y - 1, x + 1, y + 1, (onMap ? alpha : alpha / 2) << 24 | (PLAYER & 0xFFFFFF));
			if (near(mouseX, mouseY, x, y)) tooltip.add(Component.translatable("screen.hemomancy.bloodline_pool.map.you"));
		}

		Component north = Component.translatable("screen.hemomancy.bloodline_pool.map.north");
		graphics.drawString(font, north, originX - font.width(north) / 2 + 1, mapY + 3, MUTED, false);
	}

	private List<double[]> lobes(BlockPos heart, List<BlockPos> stakes, double heartRadius, double stakeRadius) {
		List<double[]> lobes = new ArrayList<>(stakes.size() + 1);
		lobes.add(new double[] {mapX(heart), mapY(heart), heartRadius * scale});
		for (BlockPos stake : stakes) lobes.add(new double[] {mapX(stake), mapY(stake), stakeRadius * scale});
		return lobes;
	}

	/** The lobes' outline, leaving out the arcs that fall inside another lobe, so it traces one envelope. */
	private static void drawEnvelopeEdge(GuiGraphics graphics, List<double[]> lobes, int colour) {
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
				if (!inner) graphics.fill((int) x, (int) y, (int) x + 1, (int) y + 1, colour);
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
		return originX + (pos.getX() - centreX) * scale;
	}

	private double mapY(BlockPos pos) {
		return originY + (pos.getZ() - centreZ) * scale;
	}

	private static boolean near(int mouseX, int mouseY, int x, int y) {
		return Math.abs(mouseX - x) <= HOVER_RADIUS && Math.abs(mouseY - y) <= HOVER_RADIUS;
	}

	private static MutableComponent coords(BlockPos pos) {
		return Component.translatable("screen.hemomancy.bloodline_pool.map.coords", pos.getX(), pos.getY(), pos.getZ());
	}

	/** One line, cut to the panel's width; returns the lines used. */
	private int text(GuiGraphics graphics, Component text, int x, int y, int width, int colour) {
		graphics.drawString(font, font.split(text, width).getFirst(), x, y, colour, false);
		return 1;
	}
}
