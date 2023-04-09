package com.vincenthuto.hemomancy.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.model.item.ChitiniteShieldModel;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ChitiniteShieldItemRenderer extends BlockEntityWithoutLevelRenderer {

	public static final Material LOCATION_SHIELD = new Material(TextureAtlas.LOCATION_BLOCKS,
			Hemomancy.rloc("entity/chitinite_shield/model_chitinite_shield"));
	@SuppressWarnings("rawtypes")
	private final ChitiniteShieldModel modelShield;

	@SuppressWarnings("rawtypes")
	public ChitiniteShieldItemRenderer(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
		super(p_172550_, p_172551_);
		modelShield = new ChitiniteShieldModel(p_172551_.bakeLayer(ChitiniteShieldModel.chitinite_shield));
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext type, PoseStack matrixStack,
			MultiBufferSource buffers, int combinedLight, int combinedOverlay) {
		matrixStack.pushPose();
		matrixStack.scale(1.0F, -1.0F, -1.0F);

		if (type == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
			matrixStack.translate(-0.1, -0.5, -0.15);
		}
		if (type == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
			matrixStack.translate(0.1, -0.5, 0);
		}
		if (type == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
			matrixStack.translate(-0.1, -0.1, 0);
		}
		matrixStack.translate(0, 0.78, 0.1);
		Material rendermaterial = LOCATION_SHIELD;
		VertexConsumer ivertexbuilder = rendermaterial.sprite().wrap(ItemRenderer.getFoilBufferDirect(buffers,
				this.modelShield.renderType(rendermaterial.atlasLocation()), true, stack.hasFoil()));
		this.modelShield.renderToBuffer(matrixStack, ivertexbuilder, combinedLight, combinedOverlay, 1.0F, 1.0F, 1.0F,
				1.0F);
		matrixStack.popPose();
	}

}