package com.vincenthuto.hemomancy.client.render.item.tile.harbinger.crafting;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.model.tile.crafting.PuppeteersSpindleModel;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.PuppeteersSpindleRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class PuppeteersSpindleItemRenderer extends BlockEntityWithoutLevelRenderer {
	public static final ResourceLocation TEXTURE = PuppeteersSpindleRenderer.TEXTURE;
	private static final float GUI_MODEL_PITCH_DEGREES = 198.0F;
	private static final float GUI_MODEL_YAW_DEGREES = -42.0F;
	private static final float GUI_MODEL_ROLL_DEGREES = -8.0F;
	/**
	 * The mesh's visual centre in entity-model units (the body stands on y = 24px and rises about 15.5px).
	 * Views rotate about this point so the wheel sits centred in slots and frames.
	 */
	private static final float MODEL_CENTRE_Y = 16.25F / 16.0F;
	private static final float MODEL_CENTRE_Z = 0.5F / 16.0F;

	private PuppeteersSpindleModel model;

	public PuppeteersSpindleItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
		if (modelSet != null) {
			this.model = new PuppeteersSpindleModel(modelSet.bakeLayer(PuppeteersSpindleModel.LAYER_LOCATION));
		}
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		if (this.model == null) {
			EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
			this.model = new PuppeteersSpindleModel(modelSet.bakeLayer(PuppeteersSpindleModel.LAYER_LOCATION));
		}

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		boolean isGui = displayContext == ItemDisplayContext.GUI;
		if (isGui) {
			Lighting.setupForEntityInInventory();
		}

		poseStack.pushPose();
		if (isGui) {
			poseStack.translate(0.5D, 0.5D, 0.5D);
			poseStack.scale(0.55F, 0.55F, 0.55F);
			poseStack.mulPose(Axis.XP.rotationDegrees(GUI_MODEL_PITCH_DEGREES));
			poseStack.mulPose(Axis.YP.rotationDegrees(GUI_MODEL_YAW_DEGREES));
			poseStack.mulPose(Axis.ZP.rotationDegrees(GUI_MODEL_ROLL_DEGREES));
			poseStack.translate(0.0F, -MODEL_CENTRE_Y, -MODEL_CENTRE_Z);
		} else if (displayContext == ItemDisplayContext.FIXED) {
			poseStack.translate(0.5, 0.5, 0.5);
			poseStack.scale(0.45F, 0.45F, 0.45F);
			poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
			poseStack.translate(0.0F, -MODEL_CENTRE_Y, -MODEL_CENTRE_Z);
		} else {
			poseStack.translate(0.5, 0.48, 0.5);
			poseStack.scale(0.35F, 0.35F, 0.35F);
			poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		}

		this.model.setupAnim(0.0F);
		VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
		this.model.renderToBuffer(poseStack, vertexConsumer, combinedLight, OverlayTexture.NO_OVERLAY, -1);
		this.model.renderToBuffer(poseStack, buffer.getBuffer(RenderType.eyes(PuppeteersSpindleRenderer.GLOW)),
				LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -1);
		poseStack.popPose();

		if (isGui) {
			Lighting.setupFor3DItems();
		}
	}
}
