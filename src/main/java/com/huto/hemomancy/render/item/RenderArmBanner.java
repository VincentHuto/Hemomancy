package com.huto.hemomancy.render.item;

import java.util.List;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.item.rune.ItemArmBanner;
import com.huto.hemomancy.model.entity.armor.ModelArmBanner;
import com.huto.hemomancy.model.entity.armor.ModelArmBannerNew;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.tileentity.BannerTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

public class RenderArmBanner extends ItemStackTileEntityRenderer {
	private final ModelArmBannerNew modelPauldron = new ModelArmBannerNew();
	private final ModelArmBanner modelShield = new ModelArmBanner();
	public static ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/arm_banner.png");
	public static final RenderMaterial LOCATION_ROYAL_GUARD_SHIELD_BASE = new RenderMaterial(
			AtlasTexture.LOCATION_BLOCKS_TEXTURE,
			new ResourceLocation(Hemomancy.MOD_ID, "entity/royal_guard_shield_base"));

	public RenderArmBanner() {
	}

	@Override
	public void func_239207_a_(ItemStack stack, ItemCameraTransforms.TransformType p_239207_2_, MatrixStack matrixStack,
			IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
		if (p_239207_2_ == TransformType.GUI) {
			matrixStack.scale(0.45f, 0.45f, 0.45f);
			matrixStack.rotate(new Quaternion(Vector3f.ZP, -73.5f, true));
			matrixStack.translate(0.34, 0, 0);
		}

		if (p_239207_2_ == TransformType.THIRD_PERSON_RIGHT_HAND) {
			matrixStack.scale(0.1f, 0.1f, 0.1f);
			matrixStack.rotate(new Quaternion(Vector3f.ZP, -73.5f, true));
			matrixStack.translate(-0.1, 2.5, 0);
		}
		if (p_239207_2_ == TransformType.THIRD_PERSON_LEFT_HAND) {
			matrixStack.scale(0.1f, 0.1f, 0.1f);
			matrixStack.rotate(new Quaternion(Vector3f.ZP, -73.5f, true));
			matrixStack.translate(-1.3, -1.75, 0);
		}
		if (p_239207_2_ == TransformType.FIRST_PERSON_RIGHT_HAND) {
			matrixStack.scale(0.2f, 0.2f, 0.2f);
			matrixStack.rotate(new Quaternion(Vector3f.ZP, -73.5f, true));
			matrixStack.translate(-1.3, 0.75, 0);

		}
		if (p_239207_2_ == TransformType.FIRST_PERSON_LEFT_HAND) {
			matrixStack.scale(0.2f, 0.2f, 0.2f);
			matrixStack.rotate(new Quaternion(Vector3f.ZP, -73.5f, true));

			matrixStack.translate(-1.3, 0.75, 0);

		}
		matrixStack.push();
		IRenderTypeBuffer.Impl impl = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
		IVertexBuilder vb = impl.getBuffer(modelPauldron.getRenderType(texture));
		matrixStack.scale(4.1f, 5f, 4.1f);
		matrixStack.translate(-0.21, 0.02, -0.53);
		matrixStack.rotate(new Quaternion(Vector3f.ZP, -105f, true));
		matrixStack.rotate(new Quaternion(Vector3f.YP, -90, true));
		modelPauldron.render(matrixStack, vb, combinedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		impl.finish();
		matrixStack.pop();

		boolean flag = stack.getChildTag("BlockEntityTag") != null;
		matrixStack.push();
		matrixStack.scale(1.0F, -1.0F, -1.0F);

		RenderMaterial rendermaterial = flag ? LOCATION_ROYAL_GUARD_SHIELD_BASE
				: ModelBakery.LOCATION_SHIELD_NO_PATTERN;
		if (flag) {
			List<Pair<BannerPattern, DyeColor>> list = BannerTileEntity
					.getPatternColorData(ItemArmBanner.getColor(stack), BannerTileEntity.getPatternData(stack));
			render_plate(matrixStack, buffer, combinedLight, combinedOverlay, this.modelShield.getPlate(),
					rendermaterial, false, list, stack.hasEffect());
		}

		matrixStack.pop();
	}

	public static void render_plate(MatrixStack ms, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay,
			ModelRenderer plate, RenderMaterial material, boolean p_241717_6_, List<Pair<BannerPattern, DyeColor>> list,
			boolean hasEffect) {
		ms.scale(2, 2, 2);
		ms.translate(0.03, 0.1, -0.05);
		ms.rotate(new Quaternion(Vector3f.ZP, -73.5f, true));
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