package com.huto.hemomancy.render.item;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.model.entity.armor.ModelSpikedShield;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RenderItemSpikedShield extends BlockEntityWithoutLevelRenderer {
	private final ModelSpikedShield modelShield = new ModelSpikedShield();
	public static final Material LOCATION_ROYAL_GUARD_SHIELD_BASE = new Material(
			AtlasTexture.LOCATION_BLOCKS_TEXTURE,
			new ResourceLocation(Hemomancy.MOD_ID, "entity/spiked_shield/model_spiked_shield"));

	public RenderItemSpikedShield() {
	}

	@Override
	public void func_239207_a_(ItemStack stack, TransformType type, MatrixStack matrixStack, IRenderTypeBuffer buffer,
			int combinedLight, int combinedOverlay) {
		matrixStack.push();
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
		matrixStack.translate(0, 0.25, -0.075);
		RenderMaterial rendermaterial = LOCATION_ROYAL_GUARD_SHIELD_BASE;
		IVertexBuilder ivertexbuilder = rendermaterial.getSprite().wrapBuffer(ItemRenderer.getEntityGlintVertexBuilder(
				buffer, this.modelShield.getRenderType(rendermaterial.getAtlasLocation()), true, stack.hasEffect()));
		this.modelShield.render(matrixStack, ivertexbuilder, combinedLight, combinedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
		matrixStack.pop();
	}

}
