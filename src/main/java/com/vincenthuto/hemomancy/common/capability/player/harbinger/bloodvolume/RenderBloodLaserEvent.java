package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.List;

public class RenderBloodLaserEvent {
	@SubscribeEvent
	public static void renderLevelLastEvent(RenderLevelStageEvent evt) {
		if (Minecraft.getInstance().level == null) {
			return;
		}
		List<AbstractClientPlayer> players = Minecraft.getInstance().level.players();
		players.forEach((p) -> {
			RenderBloodLaser.renderLaser(evt, p);

		});
	}

}
