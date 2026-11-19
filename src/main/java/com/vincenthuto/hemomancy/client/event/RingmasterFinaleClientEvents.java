package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.RingmasterFinaleClientData;
import com.vincenthuto.hemomancy.client.data.VesperRumbleClientState;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.CarouselAlive;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleMoves;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * The camera's answer to the Ringmaster's finale moves (2026-11-18), read from the move and tick synced on his copy so
 * it lands on the hit's own tick: a jolt when his real knife strikes, a hard one as the carousel's horses hit the ends
 * of their lanes, the hardest as it slams down off its pole, and a flash of one as the spotlight burns.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class RingmasterFinaleClientEvents {
	public static final float VOLLEY_JOLT = 1.2F, HORSE_JOLT = 1.8F, SLAM_JOLT = 3.0F, SPOTLIGHT_JOLT = 1.4F, LIGHTS_JOLT = 0.8F,
			LEAP_JOLT = 2.6F, STAFF_JOLT = 2.2F, SWING_JOLT = 1.0F, CASCADE_JOLT = 0.9F, PULL_JOLT = 1.4F;
	private static float amplitude;

	private RingmasterFinaleClientEvents() {
	}

	@SubscribeEvent
	public static void tickClient(ClientTickEvent.Post event) {
		amplitude = 0.0F;
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		CircusRingmasterEntity copy = RingmasterFinaleClientData.ringmaster();
		if (minecraft.level == null || player == null || copy == null || !RingmasterFinaleClientData.isActive()) return;
		RingmasterFinaleMoves.Move move = RingmasterFinaleMoves.Move.byId(copy.finaleMove());
		float t = copy.finaleMoveTime(0.0F);
		amplitude = shake(move, t, player.hurtTime);
		// His landing from the rafters at the reveal.
		if (copy.finalePhase() == com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.FinalePhase.REVEAL)
			amplitude = Math.max(amplitude, jolt(copy.finaleTick() - RingmasterFinaleMoves.LEAP_LAND, LEAP_JOLT));
	}

	/** How hard {@code move} shakes the camera {@code t} ticks in; {@code hurtTime} tells a knife that struck from one that missed. */
	static float shake(RingmasterFinaleMoves.Move move, float t, int hurtTime) {
		return switch (move) {
			case RAFTER_VOLLEY -> {
				float since = t - (move.timeline().windup() + RingmasterFinaleMoves.VOLLEY_FLIGHT);
				// Only his knife jolts, and only when it struck: the player flinched on the knife's tick.
				yield since >= 0.0F && hurtTime > 0 && hurtTime >= 9 - since ? jolt(since, VOLLEY_JOLT) : 0.0F;
			}
			case HORSE_VOLLEY -> jolt(t - move.timeline().windup() - CarouselAlive.VOLLEY_OUT, HORSE_JOLT);
			case POLE_SLAM -> Math.max(jolt(t - CarouselAlive.slamAt(), SLAM_JOLT),
					jolt(t - CarouselAlive.slamAt() - 9, SLAM_JOLT * 0.5F));
			case BURNING_SPOTLIGHT -> jolt(t - RingmasterFinaleMoves.SPOTLIGHT_BURN, SPOTLIGHT_JOLT);
			case STRING_LIGHTS -> jolt(t - move.timeline().windup(), LIGHTS_JOLT);
			case STAFF_STRIKE -> jolt(t - move.timeline().windup(), STAFF_JOLT);
			case CAROUSEL_SWING -> jolt(t - move.timeline().windup(), SWING_JOLT);
			case MARIONETTE_PULL -> jolt(t - move.timeline().windup(), PULL_JOLT);
			case CRIMSON_CASCADE -> {
				float hardest = 0.0F;
				for (int i = 0; i < RingmasterFinaleMoves.CASCADE_DROPS; i++)
					hardest = Math.max(hardest, jolt(t - RingmasterFinaleMoves.cascadeAt(i), CASCADE_JOLT));
				yield hardest;
			}
			default -> 0.0F;
		};
	}

	/** The ordeals' jolt, nothing before the blow. */
	private static float jolt(float since, float peak) {
		return since < 0.0F ? 0.0F : OrdealImpactClientEvents.jolt(since, peak);
	}

	@SubscribeEvent
	public static void shakeCamera(ViewportEvent.ComputeCameraAngles event) {
		if (amplitude <= 0.0F || Minecraft.getInstance().level == null) return;
		float time = AnimationTime.gameTime(Minecraft.getInstance().level, (float) event.getPartialTick());
		event.setPitch(event.getPitch() + VesperRumbleClientState.pitchShake(amplitude, time * 1.8F));
		event.setRoll(event.getRoll() + VesperRumbleClientState.rollShake(amplitude, time * 1.8F));
	}
}
