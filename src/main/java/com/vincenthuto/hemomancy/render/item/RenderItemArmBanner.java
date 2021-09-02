package com.vincenthuto.hemomancy.render.item;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.init.ModelLayersInit;
import com.vincenthuto.hemomancy.item.armor.ItemArmBanner;
import com.vincenthuto.hemomancy.model.item.ModelArmBanner;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;

public class RenderItemArmBanner extends BlockEntityWithoutLevelRenderer {

	private final ModelArmBanner modelPauldron;
	public static final ResourceLocation fallback = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/arm_banner/arm_banner.png");
	public static final ResourceLocation leather = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/arm_banner/leather_arm_banner.png");
	public static final ResourceLocation iron = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/arm_banner/arm_banner.png");
	public static final ResourceLocation gold = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/arm_banner/gold_arm_banner.png");
	public static final ResourceLocation diamond = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/arm_banner/diamond_arm_banner.png");
	public static final ResourceLocation netherite = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/arm_banner/netherite_arm_banner.png");
	public static final Material LOCATION_ROYAL_GUARD_SHIELD_BASE = new Material(TextureAtlas.LOCATION_BLOCKS,
			new ResourceLocation(Hemomancy.MOD_ID, "entity/royal_guard_shield_base"));

	public RenderItemArmBanner(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
		super(p_172550_, p_172551_);
		modelPauldron = new ModelArmBanner(p_172551_.bakeLayer(ModelLayersInit.arm_banner));

	}

	@Override
	public void renderByItem(ItemStack stack, ItemTransforms.TransformType p_239207_2_, PoseStack matrixStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		Item type = stack.getItem();
		ResourceLocation texture = type == ItemInit.leather_arm_banner.get() ? leather
				: type == ItemInit.iron_arm_banner.get() ? iron
						: type == ItemInit.gold_arm_banner.get() ? gold
								: type == ItemInit.diamond_arm_banner.get() ? diamond
										: type == ItemInit.netherite_arm_banner.get() ? netherite : fallback;

		if (p_239207_2_ == TransformType.GUI) {
			matrixStack.scale(0.45f, 0.45f, 0.45f);
			matrixStack.mulPose(new Quaternion(Vector3f.ZP, -73.5f, true));
			matrixStack.translate(0.34, 0, 0);
		}

		if (p_239207_2_ == TransformType.THIRD_PERSON_RIGHT_HAND) {
			matrixStack.scale(0.1f, 0.1f, 0.1f);
			matrixStack.mulPose(new Quaternion(Vector3f.ZP, -73.5f, true));
			matrixStack.translate(-0.1, 2.5, 0);
		}
		if (p_239207_2_ == TransformType.THIRD_PERSON_LEFT_HAND) {
			matrixStack.scale(0.1f, 0.1f, 0.1f);
			matrixStack.mulPose(new Quaternion(Vector3f.ZP, -73.5f, true));
			matrixStack.translate(-1.3, -1.75, 0);
		}
		if (p_239207_2_ == TransformType.FIRST_PERSON_RIGHT_HAND) {
			matrixStack.scale(0.2f, 0.2f, 0.2f);
			matrixStack.mulPose(new Quaternion(Vector3f.ZP, -73.5f, true));
			matrixStack.translate(-1.3, 0.75, 0);

		}
		if (p_239207_2_ == TransformType.FIRST_PERSON_LEFT_HAND) {
			matrixStack.scale(0.2f, 0.2f, 0.2f);
			matrixStack.mulPose(new Quaternion(Vector3f.ZP, -73.5f, true));

			matrixStack.translate(-1.3, 0.75, 0);

		}
		matrixStack.pushPose();
		MultiBufferSource.BufferSource impl = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
		VertexConsumer vb = impl.getBuffer(modelPauldron.renderType(texture));
		matrixStack.scale(4.1f, 5f, 4.1f);
		matrixStack.translate(-0.21, 0.02, -0.53);
		matrixStack.mulPose(new Quaternion(Vector3f.ZP, -105f, true));
		matrixStack.mulPose(new Quaternion(Vector3f.YP, -90, true));
		modelPauldron.renderToBuffer(matrixStack, vb, combinedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		impl.endBatch();
		matrixStack.popPose();

		boolean flag = stack.getTagElement("BlockEntityTag") != null;
		matrixStack.pushPose();
		matrixStack.scale(1.0F, -1.0F, -1.0F);

		Material rendermaterial = flag ? LOCATION_ROYAL_GUARD_SHIELD_BASE : ModelBakery.NO_PATTERN_SHIELD;
		if (flag) {
			List<Pair<BannerPattern, DyeColor>> list = BannerBlockEntity.createPatterns(ItemArmBanner.getColor(stack),
					BannerBlockEntity.getItemPatterns(stack));
			render_plate(matrixStack, buffer, combinedLight, combinedOverlay, this.modelPauldron.parts, rendermaterial,
					false, list, stack.hasFoil());
		}

		matrixStack.popPose();
	}

	public static void render_plate(PoseStack ms, MultiBufferSource buffer, int combinedLight, int combinedOverlay,
			List<ModelPart> parts, Material material, boolean p_241717_6_, List<Pair<BannerPattern, DyeColor>> list,
			boolean hasEffect) {
		ms.scale(9,9, 9);
		ms.translate(0.03, 0.1, -0.05);
		ms.mulPose(new Quaternion(Vector3f.ZP, -73.5f, true));
		for (int i = 0; i < 17 && i < list.size(); ++i) {
			Pair<BannerPattern, DyeColor> pair = list.get(i);
			float[] afloat = pair.getSecond().getTextureDiffuseColors();
			Material rendermaterial = new Material(p_241717_6_ ? Sheets.BANNER_SHEET : Sheets.SHIELD_SHEET,
					pair.getFirst().location(p_241717_6_));
			
			parts.get(1).render(ms, rendermaterial.buffer(buffer, RenderType::entityNoOutline), combinedLight,
					combinedOverlay, afloat[0], afloat[1], afloat[2], 1.0F);
			
			/*
			 * parts.forEach((p) -> p.render(ms, rendermaterial.buffer(buffer,
			 * RenderType::entityNoOutline), combinedLight, combinedOverlay, afloat[0],
			 * afloat[1], afloat[2], 1.0F));
			 */
		}
	}
}