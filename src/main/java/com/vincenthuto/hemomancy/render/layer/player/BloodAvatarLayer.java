package com.vincenthuto.hemomancy.render.layer.player;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.math.Vector3f;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.init.RenderTypeInit;
import com.vincenthuto.hemomancy.model.armor.BloodAvatarModel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BloodAvatarLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
	private final BloodAvatarModel<T> modelBloodAvatar;
	ResourceLocation glowTexture = new ResourceLocation(Hemomancy.MOD_ID, "textures/models/armor/avatar_glow.png");

	public BloodAvatarLayer(RenderLayerParent<T, M> p_117346_) {
		super(p_117346_);
		modelBloodAvatar = new BloodAvatarModel<>(
				Minecraft.getInstance().getEntityModels().bakeLayer(BloodAvatarModel.layer));
	}

	@SuppressWarnings("unused")
	@Override
	public void render(PoseStack ms, MultiBufferSource pBuffer, int pPackedLight, T ent, float pLimbSwing,
			float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
		if (ent instanceof Player player) {
			player.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent(manip -> {

				if (manip.isAvatarActive()) {
					float f = (float) ent.tickCount + pPartialTicks;
					EntityModel<T> originalModel = this.getParentModel();
					modelBloodAvatar.prepareMobModel(ent, pLimbSwing, pLimbSwingAmount, pPartialTicks);
					this.getParentModel().copyPropertiesTo(modelBloodAvatar);
					ms.pushPose();
					ms.scale(2.75f, 2.75f, 2.75f);
					VertexConsumer swirlConsumer = pBuffer
							.getBuffer(RenderType.energySwirl(glowTexture, this.xOffset(f) % 4.0F, f * .01F % 2.0F));
					modelBloodAvatar.setupAnim(ent, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
					modelBloodAvatar.renderToBuffer(ms, swirlConsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 0.5F,
							0.5F, 0.5F, 0.3F);
					ms.popPose();

					if (!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
						renderArmWithItemPlayer(player, swirlConsumer, player.getItemInHand(InteractionHand.MAIN_HAND),
								TransformType.FIRST_PERSON_RIGHT_HAND, HumanoidArm.RIGHT, ms, pBuffer, pPackedLight);
					}
					if (!player.getItemInHand(InteractionHand.OFF_HAND).isEmpty()) {
						renderArmWithItemPlayer(player, swirlConsumer, player.getItemInHand(InteractionHand.OFF_HAND),
								TransformType.FIRST_PERSON_LEFT_HAND, HumanoidArm.LEFT, ms, pBuffer, pPackedLight);
					}

				}
			});
		}
	}

	protected float xOffset(float p_116683_) {
		return p_116683_ * 0.01F;
	}

	protected void renderArmWithItemPlayer(LivingEntity entity, VertexConsumer swirlConsumer, ItemStack stack,
			ItemTransforms.TransformType transform, HumanoidArm arm, PoseStack poseStack, MultiBufferSource buffer,
			int pCombinedLight) {
		if (stack.is(Items.SPYGLASS) && entity.getUseItem() == stack && entity.swingTime == 0) {
			this.renderArmWithSpyglass(entity, swirlConsumer, stack, arm, poseStack, buffer, pCombinedLight);
		} else {
			renderArmWithItem(entity, swirlConsumer, stack, transform, arm, poseStack, buffer, pCombinedLight);
		}

	}

	protected void renderArmWithItem(LivingEntity entity, VertexConsumer swirlConsumer, ItemStack stack,
			ItemTransforms.TransformType transform, HumanoidArm arm, PoseStack poseStack, MultiBufferSource buffer,
			int pCombinedLight) {
		if (!stack.isEmpty()) {
			poseStack.pushPose();
			this.getParentModel().translateToHand(arm, poseStack);
			poseStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
			poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
			poseStack.scale(2, 2, 2);
			boolean flag = arm == HumanoidArm.LEFT;
			poseStack.translate((double) ((float) (flag ? -1 : 1) / 4.0F), 0.125D, 1.5*-0.625D);
			renderItem(entity, swirlConsumer, stack, transform, flag, poseStack, buffer, pCombinedLight);
			poseStack.popPose();
		}
	}

	private void renderArmWithSpyglass(LivingEntity entity, VertexConsumer swirlConsumer, ItemStack stack,
			HumanoidArm arm, PoseStack poseStack, MultiBufferSource buffer, int pCombinedLight) {
		poseStack.pushPose();
		ModelPart modelpart = this.getParentModel().getHead();
		float f = modelpart.xRot;
		modelpart.xRot = Mth.clamp(modelpart.xRot, (-(float) Math.PI / 6F), ((float) Math.PI / 2F));
		modelpart.translateAndRotate(poseStack);
		modelpart.xRot = f;
		CustomHeadLayer.translateToHead(poseStack, false);
		boolean flag = arm == HumanoidArm.LEFT;
		poseStack.translate((double) ((flag ? -2.5F : 2.5F) / 16.0F), -0.0625D, 0.0D);
		renderItem(entity, swirlConsumer, stack, ItemTransforms.TransformType.HEAD, false, poseStack, buffer,
				pCombinedLight);
		poseStack.popPose();
	}

	public void renderItem(LivingEntity pLivingEntity, VertexConsumer swirlConsumer, ItemStack pItemStack,
			ItemTransforms.TransformType pTransformType, boolean pLeftHand, PoseStack pMatrixStack,
			MultiBufferSource pBuffer, int pCombinedLight) {
		if (!pItemStack.isEmpty()) {
			renderStatic(pLivingEntity, swirlConsumer, pItemStack, pTransformType, pLeftHand, pMatrixStack, pBuffer,
					pLivingEntity.level, pCombinedLight, OverlayTexture.NO_OVERLAY,
					pLivingEntity.getId() + pTransformType.ordinal());

		}
	}

	public void renderStatic(@Nullable LivingEntity p_174243_, VertexConsumer swirlConsumer, ItemStack p_174244_,
			ItemTransforms.TransformType p_174245_, boolean p_174246_, PoseStack p_174247_, MultiBufferSource p_174248_,
			@Nullable Level p_174249_, int p_174250_, int p_174251_, int p_174252_) {
		if (!p_174244_.isEmpty()) {
			BakedModel bakedmodel = Minecraft.getInstance().getItemRenderer().getModel(p_174244_, p_174249_, p_174243_,
					p_174252_);
			this.render(p_174244_, swirlConsumer, p_174245_, p_174246_, p_174247_, p_174248_, p_174250_, p_174251_,
					bakedmodel);
		}
	}

	public void render(ItemStack pItemStack, VertexConsumer swirlConsumer, ItemTransforms.TransformType pTransformType,
			boolean pLeftHand, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight,
			int pCombinedOverlay, BakedModel pModel) {
		if (!pItemStack.isEmpty()) {
			pMatrixStack.pushPose();

			pModel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(pMatrixStack, pModel,
					pTransformType, pLeftHand);
			pMatrixStack.translate(-0.5D, -0.5D, -0.5D);
			if (!pModel.isCustomRenderer() && (!pItemStack.is(Items.TRIDENT))) {

//				this.renderModelLists(pModel, pItemStack, pCombinedLight, pCombinedOverlay, pMatrixStack,
//						ItemRenderer.getArmorFoilBuffer(pBuffer, ItemBlockRenderTypes.getRenderType(pItemStack, false),
//								pLeftHand, pLeftHand));

				VertexConsumer glint = pBuffer.getBuffer(RenderTypeInit.getCrimsonGlint());
				MultiBufferSource.BufferSource irendertypebuffer$impl = MultiBufferSource
						.immediate(Tesselator.getInstance().getBuilder());
				VertexConsumer item = irendertypebuffer$impl
						.getBuffer(ItemBlockRenderTypes.getRenderType(pItemStack, false));
				// VertexConsumer swirl = irendertypebuffer$impl
				// .getBuffer(RenderType.energySwirl(glowTexture, this.xOffset(1f) % 4.0F, 1f *
				// .01F % 2.0F));

				VertexConsumer buffer = VertexMultiConsumer.create(item, glint);
				this.renderModelLists(pModel, pItemStack, pCombinedLight, pCombinedOverlay, pMatrixStack, buffer);
				irendertypebuffer$impl.endBatch();

//				this.renderModelLists(pModel, pItemStack, pCombinedLight, pCombinedOverlay, pMatrixStack,
//						ItemRenderer.getArmorFoilBuffer(pBuffer,
//								RenderTypeInit.itemEnergySwirl(glowTexture, this.xOffset(1) % 4.0F, 1 * .01F % 2.0F),
//								pLeftHand, pLeftHand));

			} else {
				net.minecraftforge.client.RenderProperties.get(pItemStack).getItemStackRenderer().renderByItem(
						pItemStack, pTransformType, pMatrixStack, pBuffer, pCombinedLight, pCombinedOverlay);
			}

			pMatrixStack.popPose();
		}
	}

	public void renderModelLists(BakedModel pModel, ItemStack pStack, int pCombinedLight, int pCombinedOverlay,
			PoseStack pMatrixStack, VertexConsumer pBuffer) {
		Random random = new Random();

		for (Direction direction : Direction.values()) {
			random.setSeed(42L);
			this.renderQuadList(pMatrixStack, pBuffer, pModel.getQuads((BlockState) null, direction, random), pStack,
					pCombinedLight, pCombinedOverlay);
		}

		random.setSeed(42L);
		this.renderQuadList(pMatrixStack, pBuffer, pModel.getQuads((BlockState) null, (Direction) null, random), pStack,
				pCombinedLight, pCombinedOverlay);
	}

	public void renderQuadList(PoseStack pMatrixStack, VertexConsumer pBuffer, List<BakedQuad> pQuads,
			ItemStack pItemStack, int pCombinedLight, int pCombinedOverlay) {
		boolean flag = !pItemStack.isEmpty();
		PoseStack.Pose posestack$pose = pMatrixStack.last();

		for (BakedQuad bakedquad : pQuads) {
			int i = -1;
			if (flag && bakedquad.isTinted()) {
			}

			float f = (float) (i >> 16 & 255) / 255.0F;
			float f1 = (float) (i >> 8 & 255) / 255.0F;
			float f2 = (float) (i & 255) / 255.0F;
			pBuffer.putBulkData(posestack$pose, bakedquad, f, f1, f2, pCombinedLight, pCombinedOverlay, true);
		}

	}
}