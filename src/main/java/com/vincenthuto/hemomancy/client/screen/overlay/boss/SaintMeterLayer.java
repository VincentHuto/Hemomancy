package com.vincenthuto.hemomancy.client.screen.overlay.boss;

import com.vincenthuto.hemomancy.client.registry.SaintClientStyles;
import com.vincenthuto.hemomancy.client.screen.overlay.GuiCanvas;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import com.vincenthuto.hemomancy.common.network.boss.SaintMeterPacket;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * The one HUD layer a Saint draws through (registered above the boss bars in {@code GuiLayerRegistrar}, behind
 * {@code HudVisibilityRules}). While {@link SaintMeterClientState} names a Saint it asks that Saint's
 * {@link SaintMeterStyle} for its full-screen pass, then for its meter in the rows under the Saint's bar. The bar's
 * origin comes from {@link BossBarFrameClientState}, which the frame renderer fills while it paints the bars this frame;
 * with the bar hidden the meter takes the first bar's place at the top of the screen.
 */
public final class SaintMeterLayer {
	/** Rows from a bar's origin to the first row under it. */
	public static final int UNDER_BAR = 14;
	/** Vanilla's first boss bar origin. */
	private static final int FIRST_BAR_Y = 12;

	private static int shownSaint = -1;
	private static float shownAt;

	private SaintMeterLayer() {
	}

	public static void render(GuiGraphics graphics, DeltaTracker delta) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer viewer = minecraft.player;
		SaintMeterPacket packet = SaintMeterClientState.packet();
		if (viewer == null || packet == null) {
			shownSaint = -1;
			return;
		}
		SaintTrialEntity saint = SaintMeterClientState.saint();
		EnumSaintType type = SaintMeterClientState.type();
		if (type == null) return;
		SaintMeterStyle style = SaintClientStyles.meter(type);
		float partial = delta.getGameTimeDeltaPartialTick(false);
		float now = BossBarFrameClientState.clock() + partial;
		if (shownSaint != packet.saintId()) {
			shownSaint = packet.saintId();
			shownAt = now;
		}
		int width = graphics.guiWidth(), height = graphics.guiHeight();
		BossBarFrameClientState.Entry bar = SaintMeterClientState.frame() == null ? null
				: BossBarFrameClientState.painted(SaintMeterClientState.frame(), now);
		int x = bar != null ? bar.paintedX() : width / 2 - BossBarFrame.WIDTH / 2;
		int y = bar != null ? bar.paintedY() : FIRST_BAR_Y;
		float reveal = HemoEasing.clamp01((now - shownAt) / BossBarFrameClientState.REVEAL_TICKS);
		graphics.drawManaged(() -> {
			GuiCanvas canvas = GuiCanvas.of(graphics);
			style.paintScreen(context(canvas, 0, 0, width, height, now, partial, saint, viewer, packet, reveal));
			style.paint(context(canvas, x, y + UNDER_BAR, width, height, now, partial, saint, viewer, packet, reveal));
		});
	}

	/**
	 * Extra rows the bar drawn by {@code frame} leaves under itself for a Saint meter: the meter style's height when that
	 * bar belongs to the Saint this client is fighting, else 0.
	 */
	public static int room(ResourceLocation frame) {
		EnumSaintType type = SaintMeterClientState.type();
		if (type == null || SaintMeterClientState.packet() == null || !frame.equals(SaintMeterClientState.frame())) return 0;
		return SaintClientStyles.meter(type).height();
	}

	/** The context a muffle query reads (no canvas), or null while no Saint is named. */
	@Nullable
	public static SaintMeterContext queryContext() {
		Minecraft minecraft = Minecraft.getInstance();
		SaintMeterPacket packet = SaintMeterClientState.packet();
		if (minecraft.player == null || packet == null) return null;
		float now = BossBarFrameClientState.clock();
		return context(null, 0, 0, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight(),
				now, 0.0F, SaintMeterClientState.saint(), minecraft.player, packet, 1.0F);
	}

	private static SaintMeterContext context(@Nullable GuiCanvas canvas, int x, int y, int width, int height, float now,
			float partial, @Nullable SaintTrialEntity saint, LocalPlayer viewer, SaintMeterPacket packet, float reveal) {
		int phase = saint == null ? 1 : saint.syncedPhase();
		int opening = saint == null ? 0 : saint.view().openingId();
		float openingTime = saint == null ? -1.0F : saint.view().openingTime(partial);
		int openingLength = saint == null ? 0 : saint.view().openingLength();
		return new SaintMeterContext(canvas, x, y, width, height, now, partial, saint, viewer,
				SaintMeterClientState.saintValue(), SaintMeterClientState.saintMax(), packet.value(), packet.max(),
				packet.secondary(), packet.secondaryMax(), packet.saintFlags(), SaintMeterClientState.stateTicks(), phase,
				opening, openingTime, openingLength, reveal);
	}
}
