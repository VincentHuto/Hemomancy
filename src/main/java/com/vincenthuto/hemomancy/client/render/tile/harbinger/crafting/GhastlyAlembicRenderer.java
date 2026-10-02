package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.common.brewing.AlembicTier;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.AlembicGeometry;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.GhastlyAlembicBlockEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

import org.joml.Matrix4f;

public class GhastlyAlembicRenderer implements BlockEntityRenderer<GhastlyAlembicBlockEntity> {

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public GhastlyAlembicRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public boolean shouldRenderOffScreen(GhastlyAlembicBlockEntity te) {
		return true;
	}

	@Override
	public void render(GhastlyAlembicBlockEntity te, float partialTicks, PoseStack poseStack,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		renderFluidLevel(poseStack, bufferIn, te, combinedLightIn);
		poseStack.pushPose();
		poseStack.translate(0.5, 0, 0.5);
		poseStack.mulPose(Axis.YP.rotationDegrees(-(te.getBlockState().getValue(FACING).toYRot() + 180)));
		poseStack.translate(-0.5, 0, -0.5);
		if (te.tier() != AlembicTier.BASE)
			renderAssembly(te.tier() == AlembicTier.ATHANOR ? "athanor" : "condenser", false,
					poseStack, bufferIn, combinedLightIn, combinedOverlayIn);
		if (ghostActive(te) && te.tier() != AlembicTier.ATHANOR)
			renderAssembly(te.tier() == AlembicTier.BASE ? "condenser" : "athanor", true,
					poseStack, bufferIn, combinedLightIn, combinedOverlayIn);
		if (te.tier() == AlembicTier.ATHANOR && te.getBlockState().getValue(com.vincenthuto.hemomancy.common.block.harbinger.crafting.GhastlyAlembicBlock.LIT)
				&& te.getLevel() != null) {
			float pulse = (float) (0.5 + 0.5 * Math.sin((te.getLevel().getGameTime() + partialTicks) * 0.15));
			VertexConsumer channel = bufferIn.getBuffer(RenderTypeInit.FLASK_FLUID);
			renderColorBox(channel, poseStack.last().pose(), 2f / 16, 4.5f / 16, -2.3f / 16,
					14f / 16, 6.5f / 16, -2.25f / 16, 176, 18, 28, (int) (80 + pulse * 45));
		}
		poseStack.popPose();
		if (bufferIn instanceof MultiBufferSource.BufferSource source) {
			source.endBatch(RenderTypeInit.FLASK_FLUID);
		}
	}

	private static boolean ghostActive(GhastlyAlembicBlockEntity station) {
		if (!station.isRiteLocked()) return false;
		return ActiveRiteClientData.getActiveRites().stream().anyMatch(rite ->
				com.vincenthuto.hemomancy.common.station.StationUpgradeCatalog.forRite(rite.getRecipeId())
						.filter(tier -> tier.station() == com.vincenthuto.hemomancy.common.station.UpgradeStation.ALEMBIC)
						.isPresent()
						&& rite.getCenter().distManhattan(station.getBlockPos()) <= 2
						&& (rite.getPhase().equals("STATION_PROJECTION")
						|| rite.getPhase().equals("OFFERING_PROCESSION")
						|| rite.getPhase().equals("CULMINATION")));
	}

	private static void renderAssembly(String tier, boolean ghost, PoseStack pose,
			MultiBufferSource buffers, int light, int overlay) {
		var minecraft = Minecraft.getInstance();
		var model = minecraft.getModelManager().getModel(ModelResourceLocation.standalone(
				Hemomancy.rloc("block/ghastly_alembic_" + tier)));
		pose.pushPose();
		if (tier.equals("athanor")) pose.translate(0, AlembicGeometry.ATHANOR_LIFT, 0);
		var type = ghost ? RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS)
				: RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS);
		minecraft.getBlockRenderer().getModelRenderer().renderModel(pose.last(), buffers.getBuffer(type), null,
				model, ghost ? 0.8F : 1.0F, ghost ? 0.25F : 1.0F, ghost ? 0.3F : 1.0F, light, overlay);
		pose.popPose();
	}

	@Override public AABB getRenderBoundingBox(GhastlyAlembicBlockEntity te) {
		return new AABB(te.getBlockPos()).inflate(1.5D, 2.5D, 1.5D);
	}

	private void renderFluidLevel(PoseStack poseStack, MultiBufferSource bufferIn,
			GhastlyAlembicBlockEntity te, int combinedLightIn) {
		double current = te.getBloodVolume();
		double max = te.getMaxBloodVolume();
		if (max <= 0 || current <= 0) return;

		float fillPct = (float) Math.min(current / max, 1.0);

		float halfW = 4.5f / 16f;
		float halfD = 4.5f / 16f;
		float flaskBottomY = 4f / 16f;
		float flaskHeight = 15f / 16f;
		float fluidHeight = flaskHeight * fillPct;

		poseStack.pushPose();
		poseStack.translate(0.5f, flaskBottomY + (te.tier() == AlembicTier.ATHANOR ? AlembicGeometry.ATHANOR_LIFT : 0), 0.5f);

		int r = 153, g = 13, b = 13, a = 191;

		VertexConsumer vc = bufferIn.getBuffer(RenderTypeInit.FLASK_FLUID);
		Matrix4f mat = poseStack.last().pose();

		renderColorBox(vc, mat, -halfW, 0, -halfD, halfW, fluidHeight, halfD, r, g, b, a);

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
}
