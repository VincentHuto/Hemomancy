package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrame;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrameClientState;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFramePainter;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrames;
import com.vincenthuto.hemomancy.config.HemoClientConfig;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;

/**
 * Draws a Hemomancy boss's bar in its frame in place of vanilla's. Bars without a binding (other mods', vanilla's)
 * are left alone, and the frame's increment keeps stacked bars apart.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class BossBarFrameRenderer {
	private BossBarFrameRenderer() {
	}

	@SubscribeEvent
	public static void onBossBar(CustomizeGuiOverlayEvent.BossEventProgress event) {
		if (!HemoClientConfig.bossBarFrames()) return;
		LerpingBossEvent bar = event.getBossEvent();
		BossBarFrameClientState.Entry entry = BossBarFrameClientState.entry(bar.getId());
		if (entry == null) return;
		BossBarFrame frame = BossBarFrames.get(entry.frame());
		if (frame == null) return;
		event.setCanceled(true);
		event.setIncrement(frame.increment());
		float now = BossBarFrameClientState.clock() + event.getPartialTick().getGameTimeDeltaPartialTick(false);
		entry.seen(now);
		float progress = bar.getProgress();
		BossBarFramePainter.paint(event.getGuiGraphics(), frame, bar, event.getX(), event.getY(), progress,
				entry.chip(progress, now), entry.reveal(now), now);
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		BossBarFrameClientState.tick();
	}
}
