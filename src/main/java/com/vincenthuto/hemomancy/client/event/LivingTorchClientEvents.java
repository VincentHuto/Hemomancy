package com.vincenthuto.hemomancy.client.event;

import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.player.LivingTorchPlayerPose;
import com.vincenthuto.hemomancy.client.player.PlayerAnimationClientState;
import com.vincenthuto.hemomancy.client.player.LivingTorchBreathEffects;
import com.vincenthuto.hemomancy.client.render.item.hematic.LivingTorchRenderPlacement;
import com.vincenthuto.hemomancy.client.sound.LivingTorchBreathSound;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingTorchBreathRules;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingTorchItem;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PlayerAnimationKind;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;

import java.util.HashMap;
import java.util.Map;

/** First-person torch transforms and lifecycle-controlled channel audio. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class LivingTorchClientEvents {
	private static final Map<Integer, LivingTorchBreathSound> BREATH_LOOPS = new HashMap<>();

	private LivingTorchClientEvents() { }

	@SubscribeEvent
	public static void renderHand(RenderHandEvent event) {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		if (player == null || !(event.getItemStack().getItem() instanceof LivingTorchItem)) return;
		boolean right = event.getHand() == InteractionHand.MAIN_HAND
				? player.getMainArm() == net.minecraft.world.entity.HumanoidArm.RIGHT
				: player.getMainArm() != net.minecraft.world.entity.HumanoidArm.RIGHT;
		float side = right ? 1.0F : -1.0F;
		if (PlayerAnimationClientState.isBreathing(player)
				&& PlayerAnimationClientState.hand(player, PlayerAnimationKind.LIVING_TORCH_BREATH) == event.getHand()) {
			float elapsed = PlayerAnimationClientState.elapsed(player,
					PlayerAnimationKind.LIVING_TORCH_BREATH, event.getPartialTick());
			float windup = Math.min(1.0F, elapsed / LivingTorchBreathRules.WINDUP_TICKS);
			LivingTorchPlayerPose.BreathPose pose = LivingTorchPlayerPose.breath(windup, right);
			event.getPoseStack().translate(pose.firstPersonX(), pose.firstPersonY(), pose.firstPersonZ());
			event.getPoseStack().mulPose(Axis.XP.rotation(pose.armXRot() * 0.42F));
			event.getPoseStack().mulPose(Axis.YP.rotation(side * 0.24F));
			if (elapsed >= LivingTorchBreathRules.WINDUP_TICKS
					&& minecraft.level instanceof ClientLevel clientLevel) {
				emitFirstPersonTip(clientLevel, player, event, right, (int) elapsed);
			}
			return;
		}
		if (event.getSwingProgress() > 0.0F) {
			LivingTorchPlayerPose.ArmPose pose = LivingTorchPlayerPose.jabArm(event.getSwingProgress(), right);
			event.getPoseStack().translate(pose.xOffset(), pose.yOffset(), pose.zOffset());
			event.getPoseStack().mulPose(Axis.XP.rotation(pose.xRot() * 0.45F));
			event.getPoseStack().mulPose(Axis.YP.rotation(pose.yRot()));
		}
	}

	private static void emitFirstPersonTip(ClientLevel level, Player player, RenderHandEvent event,
			boolean right, int elapsed) {
		ItemDisplayContext context = right ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
				: ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
		event.getPoseStack().pushPose();
		event.getPoseStack().translate((right ? 1.0F : -1.0F) * 0.56F,
				-0.52F + event.getEquipProgress() * -0.6F, -0.72F);
		applyItemRendererEnvelope(event.getPoseStack(), context);
		Vec3 tip = LivingTorchRenderPlacement.tipFromCurrentPose(event.getPoseStack(),
				Minecraft.getInstance().gameRenderer.getMainCamera().getPosition());
		event.getPoseStack().popPose();
		LivingTorchBreathEffects.emitFromTip(level, player, tip, elapsed);
	}

	private static void applyItemRendererEnvelope(com.mojang.blaze3d.vertex.PoseStack poseStack,
			ItemDisplayContext context) {
		poseStack.translate(-0.5F, -0.5F, -0.5F);
		LivingTorchRenderPlacement.applyCustomModelTransform(poseStack, context);
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		PlayerAnimationClientState.tick();
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null) {
			stopAllBreathLoops();
			return;
		}
		for (Entity entity : minecraft.level.entitiesForRendering()) {
			if (entity instanceof Player player && PlayerAnimationClientState.isBreathing(player)
					&& !BREATH_LOOPS.containsKey(player.getId())) {
				LivingTorchBreathSound sound = new LivingTorchBreathSound(player);
				BREATH_LOOPS.put(player.getId(), sound);
				minecraft.getSoundManager().play(sound);
			}
		}
		BREATH_LOOPS.entrySet().removeIf(entry -> {
			Entity entity = minecraft.level.getEntity(entry.getKey());
			if (entity instanceof Player player && PlayerAnimationClientState.isBreathing(player)) return false;
			stopBreathLoop(entry.getValue());
			return true;
		});
	}

	private static void stopBreathLoop(LivingTorchBreathSound sound) {
		sound.forceStop();
	}

	private static void stopAllBreathLoops() {
		BREATH_LOOPS.values().forEach(LivingTorchClientEvents::stopBreathLoop);
		BREATH_LOOPS.clear();
	}
}
