package com.vincenthuto.hemomancy.client.render.item.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.functional.EarthenVeinModel;
import com.vincenthuto.hemomancy.client.render.tile.functional.EarthenVeinRenderer.EarthenVeinAnimContext;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class EarthenVeinItemRenderer extends BlockEntityWithoutLevelRenderer {

	public static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/earthen_vein/model_earthen_vein.png");

	private EarthenVeinModel model;
	private final EarthenVeinAnimContext animCtx = new EarthenVeinAnimContext(new AnimationState());

	public EarthenVeinItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
		if (modelSet != null) {
			this.model = new EarthenVeinModel(
					modelSet.bakeLayer(EarthenVeinModel.LAYER_LOCATION));
		}
		animCtx.state().start(0);
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {

		if (this.model == null) {
			EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
			this.model = new EarthenVeinModel(
					modelSet.bakeLayer(EarthenVeinModel.LAYER_LOCATION));
		}

		poseStack.pushPose();
		poseStack.translate(0.5, 1, 0.5);
		poseStack.scale(0.5f, 0.5f, 0.5f);
		poseStack.mulPose(new Quaternion(Vector3.XN, 180, true).toMoj());
		poseStack.mulPose(new Quaternion(Vector3.YN, 45, true).toMoj());

		// Drive the wiggle animation using the client level's game time
		if (Minecraft.getInstance().level != null) {
			float partialTicks = Minecraft.getInstance().getPartialTick();
			model.setupAnimation(Minecraft.getInstance().level, partialTicks, animCtx);
		}

		// Hide stent and nametag for item rendering
		model.getRoot().getChild("stent").visible = false;

		model.renderToBuffer(poseStack, buffer.getBuffer(model.renderType(TEXTURE)), combinedLight,
				OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
		poseStack.popPose();
	}
}

