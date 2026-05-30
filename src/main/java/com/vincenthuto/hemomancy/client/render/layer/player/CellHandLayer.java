
package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.item.BloodArmModel;
import com.vincenthuto.hemomancy.client.render.item.hematic.CellHandParticleEffects;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.ICellHand;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class CellHandLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

	private final BloodArmModel<T> model;
	public final ResourceLocation skinTexture = Hemomancy.rloc("textures/entity/hardened_skin.png");

	public CellHandLayer(RenderLayerParent<T, M> rendererIn) {
		super(rendererIn);
		model = new BloodArmModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(BloodArmModel.blood_arm));

	}

	@Override
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, T entitylivingbaseIn,
			float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
			float headPitch) {

		if (entitylivingbaseIn.getEffect(MobEffects.INVISIBILITY) != null) {
			return;
		}
		boolean playerIsRightHanded = entitylivingbaseIn.getMainArm() == HumanoidArm.RIGHT;
		ItemStack rightHandItem = playerIsRightHanded ? entitylivingbaseIn.getMainHandItem()
				: entitylivingbaseIn.getOffhandItem();
		ItemStack leftHandItem = playerIsRightHanded ? entitylivingbaseIn.getOffhandItem()
				: entitylivingbaseIn.getMainHandItem();
		matrixStackIn.pushPose();
		if (this.getParentModel().young) {
			matrixStackIn.translate(0.0, 0.75, 0.0);
			matrixStackIn.scale(0.5f, 0.5f, 0.5f);
		}

		boolean rightHandPresentation = isCellHandPresentation(entitylivingbaseIn, rightHandItem);
		boolean leftHandPresentation = isCellHandPresentation(entitylivingbaseIn, leftHandItem);

		// Right InteractionHand only
		if (rightHandPresentation && !leftHandPresentation) {
			this.renderBloodArm(matrixStackIn, bufferIn, packedLightIn, true, false);
			this.renderHandParticle(entitylivingbaseIn, rightHandItem, HumanoidArm.RIGHT);
			// Left InteractionHand only
		} else if (leftHandPresentation && !rightHandPresentation) {
			this.renderBloodArm(matrixStackIn, bufferIn, packedLightIn, false, true);
			this.renderHandParticle(entitylivingbaseIn, leftHandItem, HumanoidArm.LEFT);
			// Both Hands
		} else if (leftHandPresentation && rightHandPresentation) {
			this.renderBloodArm(matrixStackIn, bufferIn, packedLightIn, true, true);
			this.renderHandParticle(entitylivingbaseIn, rightHandItem, HumanoidArm.RIGHT);
			this.renderHandParticle(entitylivingbaseIn, leftHandItem, HumanoidArm.LEFT);

		}
		matrixStackIn.popPose();
	}

	private void renderBloodArm(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn,
			boolean renderRightArm, boolean renderLeftArm) {
		copyBloodArmPose(this.getParentModel());
		model.rightArm.visible = renderRightArm;
		model.leftArm.visible = renderLeftArm;
		VertexConsumer ivertexbuilder = bufferIn.getBuffer(RenderType.entityCutoutNoCull(skinTexture));
		model.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, -1);
	}

	private void copyBloodArmPose(M parentModel) {
		model.setAllVisible(false);
		if (parentModel instanceof HumanoidModel<?> humanoidModel) {
			model.rightArm.copyFrom(humanoidModel.rightArm);
			model.leftArm.copyFrom(humanoidModel.leftArm);
		}
	}

	private void renderHandParticle(LivingEntity living, ItemStack stack, HumanoidArm side) {
		if (Minecraft.getInstance().isPaused()) {
			return;
		}
		if (!stack.isEmpty() && isCellHandPresentation(living, stack) && living.isUsingItem()) {
			HumanoidArm activeArm = living.getUsedItemHand() == InteractionHand.MAIN_HAND
					? living.getMainArm()
					: living.getMainArm().getOpposite();
			if (activeArm == side) {
				this.spawnParticleFromOrigin(calculateThirdPersonHandOrigin(living, side), living, stack);
			}
		}
	}

	private boolean isCellHandPresentation(LivingEntity living, ItemStack stack) {
		return stack.getItem() instanceof ICellHand || LivingStaffItem.isLivingStaffUtilityUse(living, stack);
	}

	private Vec3 calculateThirdPersonHandOrigin(LivingEntity living, HumanoidArm side) {
		double bodyYaw = Math.toRadians(living.yBodyRot);
		Vec3 forward = new Vec3(-Math.sin(bodyYaw), 0.0D, Math.cos(bodyYaw));
		Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
		double sideOffset = side == HumanoidArm.RIGHT ? 0.36D : -0.36D;

		return living.position()
				.add(0.0D, living.getBbHeight() * 0.72D, 0.0D)
				.add(forward.scale(0.46D))
				.add(right.scale(sideOffset));
	}

	private void spawnParticleFromOrigin(Vec3 origin, LivingEntity player, ItemStack activeStack) {
		CellHandParticleEffects.spawnThirdPersonParticlesFromOrigin(origin, player, activeStack);
	}
}
