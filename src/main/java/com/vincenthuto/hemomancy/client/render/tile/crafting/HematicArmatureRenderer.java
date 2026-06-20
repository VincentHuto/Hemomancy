package com.vincenthuto.hemomancy.client.render.tile.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.crafting.HematicArmatureModel;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.HematicArmatureBlock;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.recipe.ArmatureUpgradeRules;
import com.vincenthuto.hemomancy.common.tile.crafting.HematicArmatureBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

public class HematicArmatureRenderer implements BlockEntityRenderer<HematicArmatureBlockEntity> {
	public static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/model_hematic_armature.png");
	private static final double BOWL_RENDER_BOUNDS = 3.5D;
	private static final double FRAME_RENDER_HEIGHT = 5.5D;
	private static final float RESERVOIR_RADIUS = 3.0F / 16.0F;
	private static final float RESERVOIR_BOTTOM_Y = 4.05F;
	private static final float RESERVOIR_MAX_HEIGHT = 7.0F / 16.0F;

	private static final float[][] BOWL_POSITIONS = new float[][] {
			{ -2.625F, 1.44F, 0.25F },
			{ 2.625F, 1.44F, 0.25F },
			{ -1.8125F, 1.26F, 1.0625F },
			{ 1.8125F, 1.26F, 1.0625F }
	};

	private final HematicArmatureModel model;

	public HematicArmatureRenderer(BlockEntityRendererProvider.Context context) {
		this.model = new HematicArmatureModel(context.bakeLayer(HematicArmatureModel.LAYER_LOCATION));
	}

	@Override
	public void render(HematicArmatureBlockEntity armature, float partialTicks, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		Direction facing = armature.getBlockState().hasProperty(HematicArmatureBlock.FACING)
				? armature.getBlockState().getValue(HematicArmatureBlock.FACING)
				: Direction.SOUTH;
		float yRot = rotationFor(facing);
		float itemYRot = itemRotationFor(facing);

		if (armature.getArmatureTier().id() >= ArmatureUpgradeRules.ArmatureTier.MONOLITHIC.id()) {
			renderReservoir(armature, poseStack, buffer);
		}
		if (buffer instanceof MultiBufferSource.BufferSource source) {
			source.endBatch(RenderTypeInit.FLASK_FLUID);
		}
		renderModel(armature, partialTicks, poseStack, buffer, combinedLight, yRot);
		renderBowlItems(armature, poseStack, buffer, combinedLight, combinedOverlay, itemYRot);
	}

	private void renderModel(HematicArmatureBlockEntity armature, float partialTicks, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, float yRot) {
		poseStack.pushPose();
		poseStack.translate(0.5D, 1.5D, 0.5D);
		poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
		VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
		float ageInTicks = armature.getLevel() != null
				? armature.getLevel().getGameTime() + partialTicks
				: partialTicks;
		model.setupBannerAnimation(ageInTicks);
		model.setupUpgradeVisibility(armature.getArmatureTier());
		model.renderToBuffer(poseStack, vertexConsumer, combinedLight, OverlayTexture.NO_OVERLAY, -1);
		poseStack.popPose();
	}

	private static void renderBowlItems(HematicArmatureBlockEntity armature, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay, float yRot) {
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		long gameTime = armature.getLevel() != null ? armature.getLevel().getGameTime() : 0L;
		for (int slot = HematicArmatureBlockEntity.SLOT_HEAD_REAGENT;
			 slot <= HematicArmatureBlockEntity.SLOT_FEET_REAGENT; slot++) {
			ItemStack stack = armature.getItem(slot);
			if (stack.isEmpty()) {
				continue;
			}
			float[] pos = BOWL_POSITIONS[slot];
			float age = gameTime + Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
			float bob = (float) Math.sin((age + slot * 11.0F) * 0.12F) * 0.025F;
			poseStack.pushPose();
			poseStack.translate(0.5D, 0.0D, 0.5D);
			poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
			poseStack.translate(pos[0], pos[1] + bob, pos[2]);
			poseStack.mulPose(Axis.YP.rotationDegrees((age * 2.5F + slot * 47.0F) % 360.0F));
			poseStack.scale(0.38F, 0.38F, 0.38F);
			itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, combinedLight, combinedOverlay,
					poseStack, buffer, armature.getLevel(), slot);
			poseStack.popPose();
		}
	}

	private static float rotationFor(Direction facing) {
		return switch (facing) {
			case NORTH -> 180.0F;
			case EAST -> 270.0F;
			case WEST -> 90.0F;
			default -> 0.0F;
		};
	}

	private static float itemRotationFor(Direction facing) {
		return switch (facing) {
			case NORTH -> 180.0F;
			case EAST -> 90.0F;
			case WEST -> 270.0F;
			default -> 0.0F;
		};
	}

	private static void renderReservoir(HematicArmatureBlockEntity armature, PoseStack poseStack,
			MultiBufferSource buffer) {
		double max = armature.getMaxBloodVolume();
		double current = armature.getBloodVolume();
		if (max <= 0.0D || current <= 0.0D) {
			return;
		}

		float fillPct = (float) Math.min(current / max, 1.0D);
		float height = RESERVOIR_MAX_HEIGHT * fillPct;

		poseStack.pushPose();
		poseStack.translate(0.5F, RESERVOIR_BOTTOM_Y, 0.5F);
		VertexConsumer vc = buffer.getBuffer(RenderTypeInit.FLASK_FLUID);
		Matrix4f mat = poseStack.last().pose();
		renderColorBox(vc, mat, -RESERVOIR_RADIUS, 0.0F, -RESERVOIR_RADIUS,
				RESERVOIR_RADIUS, height, RESERVOIR_RADIUS,
				154, 9, 9, 192);
		poseStack.popPose();
	}

	private static void renderColorBox(VertexConsumer vc, Matrix4f mat,
			float x0, float y0, float z0, float x1, float y1, float z1,
			int r, int g, int b, int a) {
		vc.addVertex(mat, x0, y1, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x0, y1, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y1, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x0, y0, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x0, y0, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y0, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y0, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y1, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y0, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x0, y0, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x0, y1, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x0, y1, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x0, y0, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y0, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x0, y1, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x0, y0, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x0, y0, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x0, y1, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y0, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y0, z0).setColor(r, g, b, a);
		vc.addVertex(mat, x1, y1, z0).setColor(r, g, b, a);
	}

	@Override
	public boolean shouldRenderOffScreen(HematicArmatureBlockEntity armature) {
		return true;
	}

	@Override
	public AABB getRenderBoundingBox(HematicArmatureBlockEntity armature) {
		return new AABB(armature.getBlockPos())
				.inflate(BOWL_RENDER_BOUNDS, 0.0D, BOWL_RENDER_BOUNDS)
				.expandTowards(0.0D, FRAME_RENDER_HEIGHT, 0.0D);
	}
}
