package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.VesperRumbleClientState;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealMoves;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusFacultyEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * The camera's answer to a professor's blows in a Chamber ordeal (2026-11-17): a hard jolt when the weight or the heave
 * lands, a grinding rumble while the bar crosses and while he hauls on the line. Read from the true self's synced
 * gesture, so it lands on the same tick as the hit.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class OrdealImpactClientEvents {
	private static final int JOLT_TICKS = 9;
	private static float amplitude;

	private OrdealImpactClientEvents() {
	}

	@SubscribeEvent
	public static void tickClient(ClientTickEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		amplitude = 0.0F;
		if (minecraft.level == null || player == null || player.isSpectator()) return;
		for (CircusPerformerEntity professor : minecraft.level.getEntitiesOfClass(CircusPerformerEntity.class,
				player.getBoundingBox().inflate(48.0D), CircusPerformerEntity::isTrueSelf)) {
			if (professor.gesture() < 0) continue;
			String role = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(professor.getType()).getPath();
			float age = professor.gestureAge(0.0F);
			// Each professor's gestures mean their own moves: one branch per role.
			float shake = switch (role) {
				case "circus_strongman" -> strongman(professor.gesture(), age - CircusFacultyEntity.GESTURE_IMPACT);
				case "circus_knife_thrower" -> com.vincenthuto.hemomancy.client.render.entity.npc.CircusTrueKnifeThrowerRenderer
						.cameraShake(professor.gesture(), age);
				case "circus_understudy" -> com.vincenthuto.hemomancy.client.render.entity.npc.CircusTrueUnderstudyRenderer
						.cameraShake(professor.gesture(), age);
				case "circus_acrobat" -> com.vincenthuto.hemomancy.client.render.entity.npc.CircusTrueAcrobatRenderer
						.cameraShake(professor.gesture(), age);
				case "circus_stilt_walker" -> com.vincenthuto.hemomancy.client.render.entity.npc.CircusTrueStiltWalkerRenderer
						.cameraShake(professor.gesture(), age);
				case "circus_beast_tamer" -> com.vincenthuto.hemomancy.client.render.entity.npc.CircusTrueBeastTamerRenderer
						.cameraShake(professor.gesture(), age);
				case "circus_fire_eater" -> com.vincenthuto.hemomancy.client.render.entity.npc.CircusTrueFireEaterRenderer
						.cameraShake(professor.gesture(), age);
				default -> 0.0F;
			};
			amplitude = Math.max(amplitude, shake);
		}
	}

	/** How hard the Strongman's blows shake the camera, {@code since} ticks after the blow. */
	private static float strongman(int gesture, float since) {
		if (since < 0.0F) return 0.0F;
		return switch (gesture) {
			case CircusFacultyEntity.GESTURE_DROP -> jolt(since, 2.2F);
			case CircusFacultyEntity.GESTURE_SLAM -> jolt(since, 3.0F);
			case CircusFacultyEntity.GESTURE_SWEEP -> since < OrdealMoves.SWEEP_TICKS ? .35F : 0.0F;
			case CircusFacultyEntity.GESTURE_PULL -> Math.max(jolt(since, 1.0F), since < 60.0F ? .25F : 0.0F);
			default -> 0.0F;
		};
	}

	/** A hard jolt fading over {@link #JOLT_TICKS}, for any professor's blow. */
	static float jolt(float since, float peak) {
		if (since >= JOLT_TICKS) return 0.0F;
		float left = 1.0F - since / JOLT_TICKS;
		return peak * left * left;
	}

	@SubscribeEvent
	public static void shakeCamera(ViewportEvent.ComputeCameraAngles event) {
		if (amplitude <= 0.0F || Minecraft.getInstance().level == null) return;
		float time = AnimationTime.gameTime(Minecraft.getInstance().level, (float) event.getPartialTick());
		event.setPitch(event.getPitch() + VesperRumbleClientState.pitchShake(amplitude, time * 1.8F));
		event.setRoll(event.getRoll() + VesperRumbleClientState.rollShake(amplitude, time * 1.8F));
	}
}
