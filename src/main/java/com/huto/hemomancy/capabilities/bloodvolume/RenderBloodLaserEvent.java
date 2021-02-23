package com.huto.hemomancy.capabilities.bloodvolume;

import java.util.List;

import com.huto.hemomancy.render.layer.HandParticleLayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.client.event.RenderPlayerEvent;
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

	private static boolean addedSpellLayer = false;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@SubscribeEvent
	public static void onPlayerRenderPre(RenderPlayerEvent.Pre event) {
		if (!addedSpellLayer) {
			event.getRenderer().addLayer(new HandParticleLayer(event.getRenderer()));
			addedSpellLayer = true;
		}
	}

}
