package com.vincenthuto.hemomancy.client.render.item.hematic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.ICellHand;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;

public class CellHandItemRenderer extends BlockEntityWithoutLevelRenderer {

	public BakedModel location;

	public final ResourceLocation skinTexture = Hemomancy.rloc("textures/entity/hardened_skin.png");

	public CellHandItemRenderer(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
		super(p_172550_, p_172551_);
	}

	public void renderArm(PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
			AbstractClientPlayer playerIn, HumanoidArm side) {
		renderArm(matrixStackIn, bufferIn, combinedLightIn, playerIn, side, ItemStack.EMPTY);
	}

	private void renderArm(PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
			AbstractClientPlayer playerIn, HumanoidArm side, ItemStack stack) {
		if (side == HumanoidArm.RIGHT) {
			Minecraft mc = Minecraft.getInstance();
			mc.getTextureManager().bindForSetup(mc.player.getSkin().texture());
			PlayerRenderer playerrenderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(mc.player);
			renderItem(matrixStackIn, bufferIn, combinedLightIn, playerIn, (playerrenderer.getModel()).rightArm,
					(playerrenderer.getModel()).rightSleeve, side, stack);
		} else {
			Minecraft mc = Minecraft.getInstance();
			mc.getTextureManager().bindForSetup(mc.player.getSkin().texture());
			PlayerRenderer playerrenderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(mc.player);
			this.renderItem(matrixStackIn, bufferIn, combinedLightIn, playerIn, (playerrenderer.getModel()).leftArm,
					(playerrenderer.getModel()).leftSleeve, side, stack);
		}

	}

	private void renderArm(PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, HumanoidArm side,
			ItemStack stack) {
		Minecraft mc = Minecraft.getInstance();
		mc.getTextureManager().bindForSetup(mc.player.getSkin().texture());
		matrixStackIn.pushPose();
		applyCastingArmTransform(matrixStackIn, side);
		if (side == HumanoidArm.RIGHT) {
			matrixStackIn.mulPose(Vector3.YP.rotationDegrees(12.0f).toMoj());
			matrixStackIn.mulPose(Vector3.XP.rotationDegrees(-35.0f).toMoj());
			matrixStackIn.mulPose(Vector3.ZP.rotationDegrees(5.0f).toMoj());
			matrixStackIn.translate(1.0, -0.4, 0.8);
			renderArm(matrixStackIn, bufferIn, combinedLightIn, mc.player, side, stack);

		} else {
			matrixStackIn.mulPose(Vector3.YP.rotationDegrees(-12.0f).toMoj());
			matrixStackIn.mulPose(Vector3.XP.rotationDegrees(-35.0f).toMoj());
			matrixStackIn.mulPose(Vector3.ZP.rotationDegrees(5.0f).toMoj());
			matrixStackIn.translate(0.0, -0.3, 0.6);
			renderArm(matrixStackIn, bufferIn, combinedLightIn, mc.player, side, stack);
		}
		spawnFirstPersonParticlesForStack(stack, side, matrixStackIn);
		matrixStackIn.popPose();
	}

	private void applyCastingArmTransform(PoseStack matrixStackIn, HumanoidArm side) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || !player.isUsingItem() || !(player.getUseItem().getItem() instanceof ICellHand)) {
			return;
		}

		HumanoidArm activeArm = player.getUsedItemHand() == InteractionHand.MAIN_HAND
				? player.getMainArm()
				: player.getMainArm().getOpposite();
		if (activeArm != side) {
			return;
		}

		ItemStack useStack = player.getUseItem();
		float useTicks = useStack.getUseDuration(player) - player.getUseItemRemainingTicks()
				+ HLClientUtils.getPartialTicks();
		float sideSign = side == HumanoidArm.RIGHT ? 1.0F : -1.0F;
		float wave = Mth.sin(useTicks * 0.35F);
		float pulse = Mth.sin(useTicks * 0.18F);

		matrixStackIn.translate(sideSign * 0.03F * wave, 0.03F * pulse, -0.04F * pulse);
		matrixStackIn.mulPose(Vector3.YP.rotationDegrees(sideSign * (6.0F + 4.0F * wave)).toMoj());
		matrixStackIn.mulPose(Vector3.ZP.rotationDegrees(sideSign * 6.0F * pulse).toMoj());
		matrixStackIn.mulPose(Vector3.XP.rotationDegrees(-8.0F - 5.0F * wave).toMoj());
	}

	// ModelBloodArm model = new ModelBloodArm(1.0f);

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext ItemDisplayContext, PoseStack matrixStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		if (ItemDisplayContext != ItemDisplayContext.THIRD_PERSON_LEFT_HAND
				&& ItemDisplayContext != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
			if (ItemDisplayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
				if (!(stack.getItem() instanceof ICellHand)) {
					return;
				}
				this.renderArm(matrixStack, buffer, combinedLight, HumanoidArm.RIGHT, stack);
			} else if (ItemDisplayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
				if (!(stack.getItem() instanceof ICellHand)) {
					return;
				}
				this.renderArm(matrixStack, buffer, combinedLight, HumanoidArm.LEFT, stack);
			} else {
				this.renderDefaultItem(stack, ItemDisplayContext, matrixStack, buffer, combinedLight, combinedOverlay);
			}
		}
	}

	private void renderDefaultItem(ItemStack stack, ItemDisplayContext ItemDisplayContext, PoseStack matrixStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		VertexConsumer buffers = buffer.getBuffer(Sheets.cutoutBlockSheet());
		if (stack.getItem() instanceof ICellHand) {
			ICellHand cell = (ICellHand) stack.getItem();
			location = cell.getBakedModel();
			int color = Minecraft.getInstance().getItemColors().getColor(stack, 1);
			if (this.location == null) {
				float r = (color >> 16 & 0xFF) / 255F;
				float g = (color >> 8 & 0xFF) / 255F;
				float b = (color & 0xFF) / 255F;
				Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(matrixStack.last(), buffers,
						null, location, r, g, b, 0xF000F0, OverlayTexture.NO_OVERLAY, ModelData.EMPTY,
						RenderType.solid());
			} else if (!stack.isEmpty()) {
				if (this.location.isGui3d()) {
					Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(matrixStack.last(),
							buffers, null, location, 255, 255, 255, 0x000000, combinedOverlay, ModelData.EMPTY,
							RenderType.solid());
				} else {
					Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(matrixStack.last(),
							buffers, null, location, 255, 255, 255, combinedLight, combinedOverlay, ModelData.EMPTY,
							RenderType.solid());
					matrixStack.popPose();
					matrixStack.pushPose();
					Minecraft.getInstance().getItemRenderer().render(stack, ItemDisplayContext,
							ItemDisplayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND, matrixStack, buffer,
							ItemDisplayContext == ItemDisplayContext.GUI ? 0xF000F0 : combinedLight,
							ItemDisplayContext == ItemDisplayContext.GUI ? OverlayTexture.NO_OVERLAY : combinedOverlay,
							this.location);
				}
			}
		}
	}

	private void renderItem(PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
			AbstractClientPlayer playerIn, ModelPart rendererArmIn, ModelPart rendererArmwearIn, HumanoidArm side,
			ItemStack stack) {
		Minecraft mc = Minecraft.getInstance();

		mc.getTextureManager().bindForSetup(mc.player.getSkin().texture());
		PlayerRenderer playerrenderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(mc.player);
		PlayerModel<AbstractClientPlayer> playermodel = playerrenderer.getModel();
		playermodel.attackTime = 0.0F;
		playermodel.crouching = false;
		playermodel.swimAmount = 0.0F;
		playermodel.setupAnim(playerIn, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
		rendererArmIn.xRot = 0.0F;
		VertexConsumer ivertexbuilder = bufferIn.getBuffer(playermodel.renderType(skinTexture));
		rendererArmIn.render(matrixStackIn, ivertexbuilder, combinedLightIn, OverlayTexture.NO_OVERLAY);
		rendererArmwearIn.xRot = 0.0F;
		rendererArmwearIn.render(matrixStackIn,
				bufferIn.getBuffer(RenderType.entityTranslucent(playerIn.getSkin().texture())), combinedLightIn,
				OverlayTexture.NO_OVERLAY);
	}

	private void spawnFirstPersonParticlesForStack(ItemStack stack, HumanoidArm hand, PoseStack poseStack) {
		Minecraft mc = Minecraft.getInstance();
		PlayerRenderer playerRenderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(mc.player);
		ModelPart arm = hand == HumanoidArm.RIGHT
				? playerRenderer.getModel().rightArm
				: playerRenderer.getModel().leftArm;
		poseStack.pushPose();
		arm.translateAndRotate(poseStack);
		Vec3 origin = CellHandRenderOrigin.fromPose(poseStack.last().pose(),
				mc.gameRenderer.getMainCamera().getPosition(), 0.0F, CellHandRenderOrigin.PALM_OFFSET_Y, 0.0F);
		poseStack.popPose();
		CellHandParticleEffects.spawnFirstPersonParticlesForStack(stack, hand, origin);
	}
}
