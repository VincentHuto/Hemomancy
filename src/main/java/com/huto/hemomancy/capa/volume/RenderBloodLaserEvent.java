package com.huto.hemomancy.capa.volume;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RenderBloodLaserEvent {
	@SubscribeEvent
	public static void renderWorldLastEvent(RenderWorldLastEvent evt) {
		List<AbstractClientPlayerEntity> players = Minecraft.getInstance().world.getPlayers();
		for (PlayerEntity player : players) {
			RenderBloodLaser.renderLaser(evt, player, Minecraft.getInstance().getRenderPartialTicks());

		}
	}

}
