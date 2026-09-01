package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.player.LivingTorchBreathEffects;
import com.vincenthuto.hemomancy.client.player.PlayerAnimationClientState;
import com.vincenthuto.hemomancy.client.render.item.harbinger.LivingTorchRenderPlacement;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingTorchBreathRules;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingTorchItem;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PlayerAnimationKind;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** Emits third-person breath effects from the same animated hand stack that renders the torch. */
public final class LivingTorchBreathLayer<T extends Player, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
	public LivingTorchBreathLayer(LivingEntityRenderer<T, M> owner) {
		super(owner);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T player,
			float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
			float netHeadYaw, float headPitch) {
		Minecraft minecraft = Minecraft.getInstance();
		if (!PlayerAnimationClientState.isBreathing(player)
				|| player == minecraft.player && minecraft.options.getCameraType().isFirstPerson()
				|| !(player.level() instanceof ClientLevel clientLevel)) {
			return;
		}
		float elapsed = PlayerAnimationClientState.elapsed(player,
				PlayerAnimationKind.LIVING_TORCH_BREATH, partialTick);
		if (elapsed < LivingTorchBreathRules.WINDUP_TICKS) return;

		InteractionHand hand = PlayerAnimationClientState.hand(player,
				PlayerAnimationKind.LIVING_TORCH_BREATH);
		ItemStack stack = player.getItemInHand(hand);
		if (!(stack.getItem() instanceof LivingTorchItem)) return;
		HumanoidArm arm = hand == InteractionHand.MAIN_HAND
				? player.getMainArm() : player.getMainArm().getOpposite();
		ItemDisplayContext context = arm == HumanoidArm.RIGHT
				? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : ItemDisplayContext.THIRD_PERSON_LEFT_HAND;

		poseStack.pushPose();
		getParentModel().translateToHand(arm, poseStack);
		poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		poseStack.translate((arm == HumanoidArm.LEFT ? -1.0F : 1.0F) / 16.0F, 0.125F, -0.625F);
		poseStack.translate(-0.5F, -0.5F, -0.5F);
		LivingTorchRenderPlacement.applyCustomModelTransform(poseStack, context);
		Vec3 tip = LivingTorchRenderPlacement.tipFromCurrentPose(poseStack,
				minecraft.gameRenderer.getMainCamera().getPosition());
		poseStack.popPose();
		LivingTorchBreathEffects.emitFromTip(clientLevel, player, tip, (int) elapsed);
	}
}
