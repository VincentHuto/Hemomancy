package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.player.LivingStaffMorphClientState;
import com.vincenthuto.hemomancy.client.player.LivingStaffMorphEffects;
import com.vincenthuto.hemomancy.client.render.item.harbinger.LivingStaffMorphRenderer;
import com.vincenthuto.hemomancy.client.rite.CardinalRiteStaffPlantingClientState;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffMorphSequence;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class LivingStaffMorphLayer<T extends Player, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
	public LivingStaffMorphLayer(LivingEntityRenderer<T, M> owner) {
		super(owner);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T player,
			float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
			float netHeadYaw, float headPitch) {
		Minecraft minecraft = Minecraft.getInstance();
		if (player.isInvisible() || CardinalRiteStaffPlantingClientState.isAnimating(player)
				|| player == minecraft.player && minecraft.options.getCameraType().isFirstPerson()) return;
		var animation = LivingStaffMorphClientState.animation(player);
		if (animation == null) return;

		float elapsed = LivingStaffMorphClientState.elapsed(player, partialTick);
		LivingStaffMorphSequence.Phase phase = LivingStaffMorphSequence.phase(
				elapsed, animation.hasOutgoing(), animation.hasIncoming());
		if (phase == LivingStaffMorphSequence.Phase.COMPLETE) return;
		float progress = LivingStaffMorphSequence.phaseProgress(
				elapsed, animation.hasOutgoing(), animation.hasIncoming());
		float dissolve = phase == LivingStaffMorphSequence.Phase.DISSOLVE ? progress : 1.0F - progress;
		renderHand(player, InteractionHand.MAIN_HAND, player.getMainArm(), animation, phase,
				dissolve, partialTick, poseStack, buffer, packedLight);
		renderHand(player, InteractionHand.OFF_HAND, player.getMainArm().getOpposite(), animation, phase,
				dissolve, partialTick, poseStack, buffer, packedLight);
	}

	private void renderHand(T player, InteractionHand hand, HumanoidArm arm,
			LivingStaffMorphClientState.Animation animation, LivingStaffMorphSequence.Phase phase,
			float dissolve, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		if (!animation.affects(hand)) return;
		ItemStack stack = phase == LivingStaffMorphSequence.Phase.DISSOLVE
				? animation.before(hand) : animation.after(hand);
		if (stack.isEmpty()) return;

		poseStack.pushPose();
		getParentModel().translateToHand(arm, poseStack);
		if (LivingStaffMorphRenderer.isFlail(stack)) {
			LivingStaffMorphEffects.emit(player, arm, currentOrigin(poseStack), phase);
			LivingStaffMorphRenderer.renderThirdPersonFlail(player, arm, stack, poseStack, buffer,
					packedLight, dissolve, partialTick);
		} else {
			poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
			poseStack.translate((arm == HumanoidArm.LEFT ? -1.0F : 1.0F) / 16.0F, 0.125F, -0.625F);
			LivingStaffMorphEffects.emit(player, arm, currentOrigin(poseStack), phase);
			ItemDisplayContext context = arm == HumanoidArm.RIGHT
					? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
			LivingStaffMorphRenderer.renderItem(player, arm, stack, context, poseStack, buffer,
					packedLight, dissolve, partialTick);
		}
		poseStack.popPose();
	}

	private static Vec3 currentOrigin(PoseStack poseStack) {
		Vector3f point = poseStack.last().pose().transformPosition(new Vector3f());
		return Minecraft.getInstance().gameRenderer.getMainCamera().getPosition()
				.add(point.x(), point.y(), point.z());
	}
}
