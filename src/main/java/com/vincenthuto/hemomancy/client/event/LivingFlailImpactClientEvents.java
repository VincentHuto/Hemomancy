package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.player.LivingFlailImpactClientState;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class LivingFlailImpactClientEvents {
	private static final LivingFlailImpactClientState STATE = new LivingFlailImpactClientState();

	private LivingFlailImpactClientEvents() {
	}

	public static void start(float charge, int seed) {
		STATE.start(charge, seed);
	}

	@SubscribeEvent
	public static void tickClient(ClientTickEvent.Post event) {
		if (Minecraft.getInstance().level == null) STATE.clear();
		else STATE.tick();
	}

	@SubscribeEvent
	public static void shakeCamera(ViewportEvent.ComputeCameraAngles event) {
		if (!STATE.isActive()) return;
		float partialTick = (float) event.getPartialTick();
		event.setPitch(event.getPitch() + STATE.pitchShake(partialTick));
		event.setRoll(event.getRoll() + STATE.rollShake(partialTick));
	}
}
