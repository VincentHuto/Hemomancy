package com.huto.hemomancy.capa.volume;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RenderBloodLaserEvent {
	@SubscribeEvent
	public static void renderLevelLastEvent(RenderLevelLastEvent evt) {
		List<AbstractClientPlayer> players = Minecraft.getInstance().level.players();
		for (Player player : players) {
			RenderBloodLaser.renderLaser(evt, player, Minecraft.getInstance().getFrameTime());

		}
	}

}
