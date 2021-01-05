package com.huto.hemomancy.capabilities.bloodvolume;

import com.huto.hemomancy.Hemomancy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.FORGE, value = Dist.CLIENT)
public class BloodVolumeHudEventHandler {

	public BloodVolumeHudEventHandler() {

	}

	static Minecraft mc = Minecraft.getInstance();

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(receiveCanceled = true)
	public static void onEvent(RenderGameOverlayEvent.Pre event) {
		ClientPlayerEntity entityPlayerSP = Minecraft.getInstance().player;
		if (entityPlayerSP == null) {
			return;
		}
		switch (event.getType()) {
		case ALL:

			BloodVolumeHud BloodVolumeHud = new BloodVolumeHud(entityPlayerSP, mc);
			if (entityPlayerSP.isAlive()) {
				BloodVolumeHud.renderStatusBar(event.getMatrixStack(), event.getWindow().getScaledWidth(),
						event.getWindow().getScaledHeight(), entityPlayerSP.world,
						entityPlayerSP);
			}
		default: 
			break;
		}
	}

}
