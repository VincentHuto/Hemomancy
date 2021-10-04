package com.vincenthuto.hemomancy.capa.player.volume;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.rune.IRunesItemHandler;
import com.vincenthuto.hemomancy.capa.player.rune.RunesCapabilities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
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
		LocalPlayer entityPlayerSP = Minecraft.getInstance().player;
		if (entityPlayerSP == null) {
			return;
		}

		switch (event.getType()) {
		case ALL:
			if (entityPlayerSP.isAlive()) {
				IRunesItemHandler runes = entityPlayerSP.getCapability(RunesCapabilities.RUNES)
						.orElseThrow(NullPointerException::new);
				if (!runes.getStackInSlot(4).isEmpty()) {
					BloodVolumeHud BloodVolumeHud = new BloodVolumeHud(entityPlayerSP, mc);
					BloodVolumeHud.renderStatusBar(event.getMatrixStack(), event.getWindow().getGuiScaledWidth(),
							event.getWindow().getGuiScaledHeight(), entityPlayerSP.level, entityPlayerSP);
				}
			}
		default:
			break;
		}
	}

}
