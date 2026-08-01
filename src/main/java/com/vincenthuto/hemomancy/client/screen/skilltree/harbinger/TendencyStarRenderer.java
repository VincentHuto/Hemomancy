package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.PanZoomState;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ProgressScreenContext;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hutoslib.client.screen.HLGuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.util.Map;

/** The shared eight-point player-tendency star used by radial Harbinger tabs. */
final class TendencyStarRenderer {
	private static final float VALUE_DISTANCE_DIVISOR = 1.75f;
	private static final int OPAQUE_MASK = 0xFF000000;

	private TendencyStarRenderer() {}

	static void draw(GuiGraphics gfx, ProgressScreenContext ctx, PanZoomState panZoom,
	                 int centerX, int centerY) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		int screenX = panZoom.sx(ctx.guiLeft(), centerX);
		int screenY = panZoom.sy(ctx.guiTop(), centerY);
		int starRadius = (int) (95 * panZoom.zoom);
		if (screenX + starRadius < ctx.guiLeft() || screenX - starRadius > ctx.guiLeft() + ctx.guiWidth()
				|| screenY + starRadius < ctx.guiTop() || screenY - starRadius > ctx.guiTop() + ctx.guiHeight()) return;

		HemoCapabilityAccess.getBloodTendency(mc.player).ifPresent(tendency -> {
			Map<EnumBloodTendency, Float> affs = tendency.getTendency();
			float angle = -90f;
			int outerRadius = (int) (210 * panZoom.zoom);
			int innerRadius = (int) (54 * panZoom.zoom);
			float spikeBaseWidth = 23.5f;
			double valueDistance = outerRadius / VALUE_DISTANCE_DIVISOR;
			for (EnumBloodTendency tend : EnumBloodTendency.values()) {
				float affinity = Mth.clamp(affs.getOrDefault(tend, 0f), 0f, 1f);
				int x1 = screenX + (int) (Math.cos(Math.toRadians(angle + spikeBaseWidth)) * innerRadius);
				int y1 = screenY + (int) (Math.sin(Math.toRadians(angle + spikeBaseWidth)) * innerRadius);
				int x2 = screenX + (int) (Math.cos(Math.toRadians(angle - spikeBaseWidth)) * innerRadius);
				int y2 = screenY + (int) (Math.sin(Math.toRadians(angle - spikeBaseWidth)) * innerRadius);
				double tipDistance = (outerRadius - innerRadius) * affinity * 0.5 + innerRadius;
				int tipX = screenX + (int) (Math.cos(Math.toRadians(angle)) * tipDistance);
				int tipY = screenY + (int) (Math.sin(Math.toRadians(angle)) * tipDistance);
				int displace = (int) ((Math.abs(x1 - x2) + Math.abs(y1 - y2)) / 2f);
				HLGuiUtils.fracLine(gfx.pose(), tipX, tipY, x1, y1, 10, tend.getColor(), displace, 1.1);
				HLGuiUtils.fracLine(gfx.pose(), tipX, tipY, x2, y2, 10, tend.getColor(), displace, 1.1);
				HLGuiUtils.fracLine(gfx.pose(), x1, y1, tipX, tipY, 10, tend.getColor(), displace, 0.8);
				HLGuiUtils.fracLine(gfx.pose(), x2, y2, tipX, tipY, 10, tend.getColor(), displace, 0.8);
				int valueX = screenX + (int) (Math.cos(Math.toRadians(angle)) * valueDistance);
				int valueY = screenY + (int) (Math.sin(Math.toRadians(angle)) * valueDistance);
				gfx.drawCenteredString(ctx.font(), String.valueOf(tendency.getAlignmentByTendency(tend)),
						valueX, valueY, OPAQUE_MASK | tend.getColor().getColor());
				angle += 45f;
			}
		});
	}
}
