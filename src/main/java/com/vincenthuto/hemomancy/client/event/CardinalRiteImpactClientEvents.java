package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.rite.CardinalRiteImpactClientState;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/** Client-only camera impulse for the culmination daemon impact. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class CardinalRiteImpactClientEvents {
	private static final CardinalRiteImpactClientState STATE = new CardinalRiteImpactClientState();

	private CardinalRiteImpactClientEvents() {
	}

	public static void start(int durationTicks, int seed) {
		STATE.start(durationTicks, seed);
	}

	public static void clear() {
		STATE.clear();
	}

	@SubscribeEvent
	public static void tickClient(ClientTickEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null || minecraft.player == null) {
			STATE.clear();
			return;
		}
		STATE.tick();
	}

	@SubscribeEvent
	public static void shakeCamera(ViewportEvent.ComputeCameraAngles event) {
		if (!STATE.isActive()) return;
		float partialTick = (float) event.getPartialTick();
		event.setPitch(event.getPitch() + STATE.pitchShake(partialTick));
		event.setRoll(event.getRoll() + STATE.rollShake(partialTick));
	}
}
