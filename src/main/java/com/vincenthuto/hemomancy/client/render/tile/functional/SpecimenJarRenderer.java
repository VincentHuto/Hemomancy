package com.vincenthuto.hemomancy.client.render.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.functional.SpecimenJarModel;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.SpecimenJarBlock;
import com.vincenthuto.hemomancy.common.tile.functional.SpecimenJarBlockEntity;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class SpecimenJarRenderer implements BlockEntityRenderer<SpecimenJarBlockEntity> {
	public static final ResourceLocation FRAME_TEXTURE = Hemomancy.rloc("textures/block/hematic_iron_block.png");
	public static final ResourceLocation GLASS_TEXTURE = Hemomancy.rloc("textures/block/vivianite_glass.png");

	private final SpecimenJarModel model;
	private Entity cachedEntity;
	private String cachedKey = "";

	public SpecimenJarRenderer(BlockEntityRendererProvider.Context context) {
		this.model = new SpecimenJarModel(context.bakeLayer(SpecimenJarModel.LAYER_LOCATION));
	}

	public SpecimenJarRenderer() {
		this.model = null;
	}

	@Override
	public void render(SpecimenJarBlockEntity jar, float partialTicks, PoseStack poseStack, MultiBufferSource buffer,
			int combinedLight, int combinedOverlay) {
		if (model == null) {
			return;
		}
		poseStack.pushPose();
		Direction facing = jar.getBlockState().getValue(SpecimenJarBlock.FACING);
		poseStack.translate(0.5D, 0.0D, 0.5D);
		poseStack.mulPose(Vector3.YP.rotationDegrees(180.0F - facing.toYRot()).toMoj());
		poseStack.translate(-0.5D, 0.0D, -0.5D);
		poseStack.translate(0D, -1.5D, 0D);
		renderJarFrame(model, poseStack, buffer, combinedLight, combinedOverlay);
		if (jar.hasSpecimen()) {
			poseStack.pushPose();
			poseStack.translate(0.1D, 1.25D, 0.125D);
			poseStack.scale(0.8F, 0.8F, 0.8F);
			renderSpecimen(jar.getLevel(), jar.getSpecimenCopy(), partialTicks, poseStack, buffer, combinedLight);
			poseStack.popPose();

		}
		renderJarGlass(model, poseStack, buffer, combinedLight, combinedOverlay);
		poseStack.popPose();
	}

	public static void renderJarModel(SpecimenJarModel model, PoseStack poseStack, MultiBufferSource buffer,
			int combinedLight, int combinedOverlay) {
		renderJarFrame(model, poseStack, buffer, combinedLight, combinedOverlay);
		renderJarGlass(model, poseStack, buffer, combinedLight, combinedOverlay);
	}

	public static void renderJarFrame(SpecimenJarModel model, PoseStack poseStack, MultiBufferSource buffer,
			int combinedLight, int combinedOverlay) {
		poseStack.pushPose();
		poseStack.translate(0.5D, 1.5D, 0.5D);
		poseStack.mulPose(Vector3.XP.rotationDegrees(180f).toMoj());
		VertexConsumer frame = buffer.getBuffer(RenderType.entityCutoutNoCull(FRAME_TEXTURE));
		model.renderFrame(poseStack, frame, combinedLight, combinedOverlay, -1);
		poseStack.popPose();
	}

	public static void renderJarGlass(SpecimenJarModel model, PoseStack poseStack, MultiBufferSource buffer,
			int combinedLight, int combinedOverlay) {
		poseStack.pushPose();
		poseStack.translate(0.5D, 1.5D, 0.5D);
		poseStack.mulPose(Vector3.XP.rotationDegrees(180f).toMoj());
		VertexConsumer glass = buffer.getBuffer(RenderType.entityTranslucent(GLASS_TEXTURE));
		model.renderGlass(poseStack, glass, combinedLight, combinedOverlay, 0x88FFFFFF);
		poseStack.popPose();
	}

	public void renderSpecimen(Level level, CompoundTag specimen, float partialTicks, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight) {
		Entity entity = getOrCreateEntity(level, specimen);
		if (entity == null) {
			return;
		}

		float maxDim = Math.max(entity.getBbWidth(), entity.getBbHeight());
		float scale = maxDim <= 0 ? 0.45F : Math.min(0.55F / maxDim, 0.75F);
		poseStack.pushPose();
		poseStack.translate(0.5D, 0.32D, 0.5D);
		poseStack.scale(scale, scale, scale);
		poseStack.mulPose(Vector3.YP.rotationDegrees(180.0F).toMoj());
		Minecraft.getInstance().getEntityRenderDispatcher().render(entity, 0.0D, 0.0D, 0.0D,
				0.0F, partialTicks, poseStack, buffer, combinedLight);
		poseStack.popPose();
	}

	private Entity getOrCreateEntity(Level level, CompoundTag specimen) {
		if (level == null || specimen == null || specimen.isEmpty()) {
			return null;
		}
		String key = specimen.toString();
		if (cachedEntity != null && key.equals(cachedKey)) {
			return cachedEntity;
		}
		CompoundTag renderTag = specimen.copy();
		ListTag pos = new ListTag();
		pos.add(net.minecraft.nbt.DoubleTag.valueOf(0.0D));
		pos.add(net.minecraft.nbt.DoubleTag.valueOf(0.0D));
		pos.add(net.minecraft.nbt.DoubleTag.valueOf(0.0D));
		renderTag.put("Pos", pos);
		Optional<Entity> entity = EntityType.create(renderTag, level);
		cachedEntity = entity.orElse(null);
		cachedKey = key;
		if (cachedEntity != null) {
			cachedEntity.setPos(0.0D, 0.0D, 0.0D);
			cachedEntity.setYRot(0.0F);
			cachedEntity.setXRot(0.0F);
		}
		return cachedEntity;
	}
}
