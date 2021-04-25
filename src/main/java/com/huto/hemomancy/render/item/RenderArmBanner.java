package com.huto.hemomancy.render.item;

import java.util.List;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.item.rune.ItemArmBanner;
import com.huto.hemomancy.model.entity.armor.ModelArmBanner;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.tileentity.BannerTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

public class RenderArmBanner extends ItemStackTileEntityRenderer {
	private final ModelArmBanner modelShield = new ModelArmBanner();
	public static final RenderMaterial LOCATION_ROYAL_GUARD_SHIELD_BASE = new RenderMaterial(
			AtlasTexture.LOCATION_BLOCKS_TEXTURE,
			new ResourceLocation(Hemomancy.MOD_ID, "entity/royal_guard_shield_base"));

	public RenderArmBanner() {
	}

	float close = 1f;

	@Override
	public void func_239207_a_(ItemStack stack, ItemCameraTransforms.TransformType p_239207_2_, MatrixStack matrixStack,
			IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
		boolean flag = stack.getChildTag("BlockEntityTag") != null;
		matrixStack.push();
		matrixStack.scale(1.0F, -1.0F, -1.0F);
		RenderMaterial rendermaterial = flag ? LOCATION_ROYAL_GUARD_SHIELD_BASE
				: ModelBakery.LOCATION_SHIELD_NO_PATTERN;
		IVertexBuilder ivertexbuilder = rendermaterial.getSprite().wrapBuffer(ItemRenderer.getEntityGlintVertexBuilder(
				buffer, this.modelShield.getRenderType(rendermaterial.getAtlasLocation()), true, stack.hasEffect()));
		this.modelShield.getHandle().render(matrixStack, ivertexbuilder, combinedLight, combinedOverlay, 1.0F, 1.0F,
				1.0F, 1.0F);
		if (flag) {
			List<Pair<BannerPattern, DyeColor>> list = BannerTileEntity
					.getPatternColorData(ItemArmBanner.getColor(stack), BannerTileEntity.getPatternData(stack));
			render_plate(matrixStack, buffer, combinedLight, combinedOverlay, this.modelShield.getPlate(),
					rendermaterial, false, list, stack.hasEffect());
		} else {
			this.modelShield.getPlate().render(matrixStack, ivertexbuilder, combinedLight, combinedOverlay, 1.0F, 1.0F,
					1.0F, 1.0F);
		}

		matrixStack.pop();
	}

	public static void render_plate(MatrixStack ms, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay,
			ModelRenderer plate, RenderMaterial material, boolean p_241717_6_, List<Pair<BannerPattern, DyeColor>> list,
			boolean hasEffect) {
		plate.render(ms, material.getItemRendererBuffer(buffer, RenderType::getEntitySolid, hasEffect), combinedLight,
				combinedOverlay);
		ms.scale(2, 2, 2);
		ms.rotate(new Quaternion(Vector3f.ZP, -72.5f, true));
		for (int i = 0; i < 17 && i < list.size(); ++i) {
			Pair<BannerPattern, DyeColor> pair = list.get(i);
			float[] afloat = pair.getSecond().getColorComponentValues();
			RenderMaterial rendermaterial = new RenderMaterial(
					p_241717_6_ ? Atlases.BANNER_ATLAS : Atlases.SHIELD_ATLAS,
					pair.getFirst().getTextureLocation(p_241717_6_));
			plate.render(ms, rendermaterial.getBuffer(buffer, RenderType::getEntityNoOutline), combinedLight,
					combinedOverlay, afloat[0], afloat[1], afloat[2], 1.0F);
		}

	}
}