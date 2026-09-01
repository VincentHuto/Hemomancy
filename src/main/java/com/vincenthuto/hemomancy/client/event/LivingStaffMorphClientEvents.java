package com.vincenthuto.hemomancy.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.player.LivingStaffMorphClientState;
import com.vincenthuto.hemomancy.client.player.LivingStaffMorphEffects;
import com.vincenthuto.hemomancy.client.render.item.harbinger.LivingStaffMorphRenderer;
import com.vincenthuto.hemomancy.client.rite.CardinalRiteStaffPlantingClientState;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffMorphSequence;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import org.joml.Vector3f;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class LivingStaffMorphClientEvents {
	private LivingStaffMorphClientEvents() {
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void renderHand(RenderHandEvent event) {
		Minecraft minecraft = Minecraft.getInstance();
		AbstractClientPlayer player = minecraft.player;
		if (player == null || CardinalRiteStaffPlantingClientState.isAnimating(player)
				|| !LivingStaffMorphClientState.affectsHand(player, event.getHand())) return;

		var animation = LivingStaffMorphClientState.animation(player);
		float elapsed = LivingStaffMorphClientState.elapsed(player, event.getPartialTick());
		LivingStaffMorphSequence.Phase phase = LivingStaffMorphSequence.phase(
				elapsed, animation.hasOutgoing(), animation.hasIncoming());
		if (phase == LivingStaffMorphSequence.Phase.COMPLETE) return;
		float progress = LivingStaffMorphSequence.phaseProgress(
				elapsed, animation.hasOutgoing(), animation.hasIncoming());
		float dissolve = phase == LivingStaffMorphSequence.Phase.DISSOLVE ? progress : 1.0F - progress;
		ItemStack stack = phase == LivingStaffMorphSequence.Phase.DISSOLVE
				? animation.before(event.getHand()) : animation.after(event.getHand());

		event.setCanceled(true);
		HumanoidArm arm = event.getHand() == InteractionHand.MAIN_HAND
				? player.getMainArm() : player.getMainArm().getOpposite();
		renderArm(player, arm, event);
		if (stack.isEmpty()) return;

		PoseStack poseStack = event.getPoseStack();
		poseStack.pushPose();
		applyHeldItemTransform(poseStack, arm, event.getEquipProgress(), event.getSwingProgress());
		LivingStaffMorphEffects.emit(player, arm, currentOrigin(poseStack), phase);
		if (LivingStaffMorphRenderer.isFlail(stack)) {
			LivingStaffMorphRenderer.renderFirstPersonFlail(player, arm, stack, poseStack,
					event.getMultiBufferSource(), event.getPackedLight(), dissolve, event.getPartialTick());
		} else {
			ItemDisplayContext context = arm == HumanoidArm.RIGHT
					? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
			LivingStaffMorphRenderer.renderItem(player, arm, stack, context, poseStack,
					event.getMultiBufferSource(), event.getPackedLight(), dissolve, event.getPartialTick());
		}
		poseStack.popPose();
	}

	private static void applyHeldItemTransform(PoseStack poseStack, HumanoidArm arm,
			float equipProgress, float swingProgress) {
		float side = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
		float root = Mth.sqrt(swingProgress);
		poseStack.translate(side * (-0.4F * Mth.sin(root * (float) Math.PI)),
				0.2F * Mth.sin(root * (float) (Math.PI * 2.0D)),
				-0.2F * Mth.sin(swingProgress * (float) Math.PI));
		poseStack.translate(side * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
		float squaredSwing = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
		float rootSwing = Mth.sin(root * (float) Math.PI);
		poseStack.mulPose(Axis.YP.rotationDegrees(side * (45.0F - squaredSwing * 20.0F)));
		poseStack.mulPose(Axis.ZP.rotationDegrees(side * rootSwing * -20.0F));
		poseStack.mulPose(Axis.XP.rotationDegrees(rootSwing * -80.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(side * -45.0F));
	}

	private static void renderArm(AbstractClientPlayer player, HumanoidArm arm, RenderHandEvent event) {
		if (player.isInvisible()) return;
		PoseStack poseStack = event.getPoseStack();
		float swingProgress = event.getSwingProgress();
		float side = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
		float root = Mth.sqrt(swingProgress);
		poseStack.pushPose();
		poseStack.translate(side * (-0.3F * Mth.sin(root * (float) Math.PI) + 0.64000005F),
				0.4F * Mth.sin(root * (float) (Math.PI * 2.0D)) - 0.6F + event.getEquipProgress() * -0.6F,
				-0.4F * Mth.sin(swingProgress * (float) Math.PI) - 0.71999997F);
		poseStack.mulPose(Axis.YP.rotationDegrees(side * 45.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(side * Mth.sin(root * (float) Math.PI) * 70.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(side
				* Mth.sin(swingProgress * swingProgress * (float) Math.PI) * -20.0F));
		poseStack.translate(side * -1.0F, 3.6F, 3.5F);
		poseStack.mulPose(Axis.ZP.rotationDegrees(side * 120.0F));
		poseStack.mulPose(Axis.XP.rotationDegrees(200.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(side * -135.0F));
		poseStack.translate(side * 5.6F, 0.0F, 0.0F);
		PlayerRenderer renderer = (PlayerRenderer) Minecraft.getInstance()
				.getEntityRenderDispatcher().getRenderer(player);
		if (arm == HumanoidArm.RIGHT) {
			renderer.renderRightHand(poseStack, event.getMultiBufferSource(), event.getPackedLight(), player);
		} else {
			renderer.renderLeftHand(poseStack, event.getMultiBufferSource(), event.getPackedLight(), player);
		}
		poseStack.popPose();
	}

	private static Vec3 currentOrigin(PoseStack poseStack) {
		Vector3f point = poseStack.last().pose().transformPosition(new Vector3f());
		return Minecraft.getInstance().gameRenderer.getMainCamera().getPosition()
				.add(point.x(), point.y(), point.z());
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		LivingStaffMorphClientState.tick();
	}
}
