package com.vincenthuto.hemomancy.capa.volume;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RenderBloodLaserEvent {
	@SubscribeEvent
	public static void renderLevelLastEvent(RenderLevelLastEvent evt) {
		List<AbstractClientPlayer> players = Minecraft.getInstance().level.players();
		players.forEach((p) -> {
			// RenderBloodLaser.renderLaser(evt, player,
			// Minecraft.getInstance().getFrameTime());
		});
	}

}
