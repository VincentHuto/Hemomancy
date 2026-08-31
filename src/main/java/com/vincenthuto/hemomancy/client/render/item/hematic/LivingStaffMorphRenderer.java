package com.vincenthuto.hemomancy.client.render.item.hematic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.item.LivingFlailModel;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingAxeItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingBladeItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingFlailItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingSickleItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingSpearItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingTorchItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

public final class LivingStaffMorphRenderer {
	private static LivingFlailModel<?> flailModel;

	private LivingStaffMorphRenderer() {
	}

	public static boolean isFlail(ItemStack stack) {
		return stack.getItem() instanceof LivingFlailItem;
	}

	public static void renderItem(LivingEntity holder, HumanoidArm arm, ItemStack stack,
			ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
			float dissolveProgress, float partialTick) {
		poseStack.pushPose();
		applyContraction(poseStack, dissolveProgress);
		ResourceLocation texture = textureFor(stack);
		Minecraft.getInstance().getItemRenderer().renderStatic(stack, context, packedLight,
				OverlayTexture.NO_OVERLAY, poseStack, new MorphBufferSource(buffer,
						HemoRenderTypes.hermitFarewellDissolve(texture, holder.tickCount + partialTick,
								dissolveProgress, seed(holder, arm))), holder.level(), holder.getId());
		Minecraft.getInstance().getItemRenderer().renderStatic(stack, context, packedLight,
				OverlayTexture.NO_OVERLAY, poseStack, new MorphBufferSource(buffer,
						bloodMelt(holder, arm, poseStack, dissolveProgress, partialTick)),
				holder.level(), holder.getId());
		poseStack.popPose();
	}

	public static void renderFirstPersonFlail(LivingEntity holder, HumanoidArm arm, ItemStack stack,
			PoseStack poseStack, MultiBufferSource buffer, int packedLight, float dissolveProgress,
			float partialTick) {
		poseStack.pushPose();
		boolean left = arm == HumanoidArm.LEFT;
		float side = left ? 1.0F : 3.0F;
		poseStack.translate(side * 0.18F, 0.52F, 0.2F);
		poseStack.mulPose(Axis.XP.rotationDegrees(-110.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(side));
		poseStack.scale(0.72F, 0.72F, 0.72F);
		applyContraction(poseStack, dissolveProgress);
		LivingFlailRenderHelper.renderHeldDissolving(flailModel(), holder, arm, stack, poseStack, buffer,
				packedLight, OverlayTexture.NO_OVERLAY, dissolveProgress, seed(holder, arm), true, partialTick);
		poseStack.popPose();
	}

	public static void renderThirdPersonFlail(LivingEntity holder, HumanoidArm arm, ItemStack stack,
			PoseStack poseStack, MultiBufferSource buffer, int packedLight, float dissolveProgress,
			float partialTick) {
		poseStack.pushPose();
		float side = arm == HumanoidArm.LEFT ? -1.0F : 1.0F;
		poseStack.translate(side * 0.08F, 0.48F, -0.09F);
		poseStack.mulPose(Axis.ZP.rotationDegrees(side * 8.0F));
		poseStack.scale(0.9F, 0.9F, 0.9F);
		applyContraction(poseStack, dissolveProgress);
		LivingFlailRenderHelper.renderHeldDissolving(flailModel(), holder, arm, stack, poseStack, buffer,
				packedLight, OverlayTexture.NO_OVERLAY, dissolveProgress, seed(holder, arm), false, partialTick);
		poseStack.popPose();
	}

	private static void applyContraction(PoseStack poseStack, float dissolveProgress) {
		float progress = Mth.clamp(dissolveProgress, 0.0F, 1.0F);
		float eased = progress * progress * (3.0F - 2.0F * progress);
		float scale = Mth.lerp(eased, 1.0F, 0.12F);
		poseStack.translate(0.0F, eased * 0.08F, 0.0F);
		poseStack.scale(scale, scale, scale);
	}

	private static float seed(LivingEntity holder, HumanoidArm arm) {
		return holder.getId() * 0.173F + (arm == HumanoidArm.LEFT ? 0.41F : 0.0F);
	}

	static RenderType bloodMelt(LivingEntity holder, HumanoidArm arm, PoseStack poseStack,
			float dissolveProgress, float partialTick) {
		Vector3f center = poseStack.last().pose().transformPosition(new Vector3f());
		return HemoRenderTypes.cardinalStaffBloodMelt(holder.tickCount + partialTick,
				seed(holder, arm), 0.055F, center.x, center.y, center.z,
				dissolveProgress, center.y - 0.65F, 1.3F);
	}

	private static ResourceLocation textureFor(ItemStack stack) {
		if (stack.getItem() instanceof LivingBladeItem) {
			return Hemomancy.rloc("textures/entity/model_living_blade_hand.png");
		}
		if (stack.getItem() instanceof LivingAxeItem) {
			return Hemomancy.rloc("textures/entity/model_living_axe_hand.png");
		}
		if (stack.getItem() instanceof LivingSpearItem) {
			return Hemomancy.rloc("textures/entity/model_living_spear_hand.png");
		}
		if (stack.getItem() instanceof LivingTorchItem) {
			return Hemomancy.rloc("textures/entity/model_living_torch.png");
		}
		if (stack.getItem() instanceof LivingSickleItem) {
			return Hemomancy.rloc("textures/entity/model_living_sickle.png");
		}
		return TextureAtlas.LOCATION_BLOCKS;
	}

	private static LivingFlailModel<?> flailModel() {
		if (flailModel == null) {
			flailModel = new LivingFlailModel<>(Minecraft.getInstance().getEntityModels()
					.bakeLayer(LivingFlailModel.LAYER_LOCATION));
		}
		return flailModel;
	}

	public static boolean isMorphBuffer(MultiBufferSource buffer) {
		return buffer instanceof MorphBufferSource;
	}

	private record MorphBufferSource(MultiBufferSource delegate, RenderType renderType)
			implements MultiBufferSource {
		@Override
		public VertexConsumer getBuffer(RenderType ignored) {
			return delegate.getBuffer(renderType);
		}
	}
}
