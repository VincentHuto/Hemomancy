package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.boss.VesperRumbleClientState;
import com.vincenthuto.hemomancy.client.data.VesperBurrowClientData;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperBurrowRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheCrownedRefusalEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWingedFlightRules;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/** Follows burrowing Vespers on the client: records their fissure trail and shakes the camera through the rumble. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class VesperBurrowClientEvents {
	private VesperBurrowClientEvents() {
	}

	@SubscribeEvent
	public static void tickClient(ClientTickEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		if (minecraft.level == null || player == null) {
			VesperBurrowClientData.clear();
			return;
		}
		long time = minecraft.level.getGameTime();
		VesperBurrowClientData.expire(time);
		float amplitude = 0.0F;
		double reach = VesperWingedFlightRules.SAFE_ARENA_HALF_EXTENT + 6.0D;
		for (VesperTheCrownedRefusalEntity vesper : minecraft.level.getEntitiesOfClass(
				VesperTheCrownedRefusalEntity.class, player.getBoundingBox().inflate(64.0D))) {
			VesperBurrowRules.Stage stage = vesper.getBurrowStage();
			if (stage == null) continue;
			VesperBurrowRules.Plan plan = new VesperBurrowRules.Plan(vesper.getBurrowHuntTicks(), false);
			if (stage == VesperBurrowRules.Stage.HUNT || stage == VesperBurrowRules.Stage.STRIKE_TELL) {
				VesperBurrowClientData.record(vesper.getX(), vesper.getY() + VesperBurrowRules.SUBMERGED_DEPTH,
						vesper.getZ(), time);
			}
			double dx = player.getX() - vesper.getX();
			double dz = player.getZ() - vesper.getZ();
			// Only those standing over the hunt feel it; spectating from beyond the arena does not shake.
			if (dx * dx + dz * dz <= reach * reach && !player.isSpectator()) {
				amplitude = Math.max(amplitude, VesperRumbleClientState.amplitude(plan, vesper.getBurrowTick()));
			}
		}
		VesperBurrowClientData.setShakeAmplitude(amplitude);
	}

	@SubscribeEvent
	public static void shakeCamera(ViewportEvent.ComputeCameraAngles event) {
		float amplitude = VesperBurrowClientData.shakeAmplitude();
		if (amplitude <= 0.0F || Minecraft.getInstance().level == null) return;
		float time = AnimationTime.gameTime(Minecraft.getInstance().level, (float) event.getPartialTick());
		event.setPitch(event.getPitch() + VesperRumbleClientState.pitchShake(amplitude, time));
		event.setRoll(event.getRoll() + VesperRumbleClientState.rollShake(amplitude, time));
	}
}
