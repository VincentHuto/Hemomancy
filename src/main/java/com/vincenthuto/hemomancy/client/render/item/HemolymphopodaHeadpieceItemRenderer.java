package com.vincenthuto.hemomancy.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.HemolymphopodaModel;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class HemolymphopodaHeadpieceItemRenderer extends BlockEntityWithoutLevelRenderer {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/hemolymphopoda/model_hemolymphopoda.png");

	// Dome pivot in the entity model is at (0, 23, -5.5) in pixel units.
	// This translate (applied first to vertices) centres the dome at the origin
	// before rotation and scale are applied.
	private static final float DOME_CENTER_Y = -23.0f / 16.0f;
	private static final float DOME_CENTER_Z = 5.5f / 16.0f;

	private HemolymphopodaModel<?> model;

	public HemolymphopodaHeadpieceItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
		if (modelSet != null) {
			this.model = new HemolymphopodaModel<>(modelSet.bakeLayer(HemolymphopodaModel.LAYER_LOCATION));
		}
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffer,
			int light, int overlay) {
		if (this.model == null) {
			this.model = new HemolymphopodaModel<>(
					Minecraft.getInstance().getEntityModels().bakeLayer(HemolymphopodaModel.LAYER_LOCATION));
		}

		float scale;
		switch (context) {
		case GUI -> scale = 0.6f;
		case GROUND -> scale = 0.4f;
		case FIXED -> scale = 0.5f;
		default -> scale = 0.5f;
		}

		poseStack.pushPose();

		// (5) Place into item display space
		poseStack.translate(0.5, 0.5, 0.5);
		// (4) Scale down to fit the 1×1×1 item space
		poseStack.scale(scale, scale, scale);
		// (3) Flip entity-model Y convention so the shell faces up
		poseStack.mulPose(new Quaternion(Vector3.XP, 180, true).toMoj());
		// (2) Rotate to an isometric angle so the shell shape is clearly visible
		poseStack.mulPose(new Quaternion(Vector3.YP, 135, true).toMoj());
		// (1) Centre the dome pivot at the local origin (applied first to vertices)
		poseStack.translate(0.0f, DOME_CENTER_Y, DOME_CENTER_Z);

		VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
		model.renderToBuffer(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);

		poseStack.popPose();
	}
}

