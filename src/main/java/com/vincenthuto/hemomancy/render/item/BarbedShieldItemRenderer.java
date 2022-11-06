package com.vincenthuto.hemomancy.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.model.item.BarbedShieldModel;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class BarbedShieldItemRenderer extends BlockEntityWithoutLevelRenderer {

	public static final Material LOCATION_ROYAL_GUARD_SHIELD_BASE = new Material(TextureAtlas.LOCATION_BLOCKS,
			new ResourceLocation(Hemomancy.MOD_ID, "entity/barbed_shield/model_barbed_shield"));
	private final BarbedShieldModel modelShield;

	public BarbedShieldItemRenderer(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
		super(p_172550_, p_172551_);
		modelShield = new BarbedShieldModel(p_172551_.bakeLayer(BarbedShieldModel.barbed_shield));
	}

	@Override
	public void renderByItem(ItemStack stack, ItemTransforms.TransformType type, PoseStack matrixStack,
			MultiBufferSource buffers, int combinedLight, int combinedOverlay) {
		matrixStack.pushPose();
		matrixStack.scale(1.0F, -1.0F, -1.0F);

		if (type == TransformType.THIRD_PERSON_LEFT_HAND) {
			matrixStack.translate(-0.1, 0, 0);
		}
		if (type == TransformType.FIRST_PERSON_LEFT_HAND) {
			matrixStack.translate(0.1, -0.1, 0);
		}
		if (type == TransformType.FIRST_PERSON_RIGHT_HAND) {
			matrixStack.translate(-0.1, -0.1, 0);
		}
		matrixStack.translate(0, 0.78, 0.1);
		Material rendermaterial = LOCATION_ROYAL_GUARD_SHIELD_BASE;
		VertexConsumer ivertexbuilder = rendermaterial.sprite().wrap(ItemRenderer.getFoilBufferDirect(buffers,
				this.modelShield.renderType(rendermaterial.atlasLocation()), true, stack.hasFoil()));
		this.modelShield.renderToBuffer(matrixStack, ivertexbuilder, combinedLight, combinedOverlay, 1.0F, 1.0F, 1.0F,
				1.0F);
		matrixStack.popPose();
	}

}