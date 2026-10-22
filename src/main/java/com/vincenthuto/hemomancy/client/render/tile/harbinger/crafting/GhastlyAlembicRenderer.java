package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.common.brewing.AlembicTier;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.AlembicGeometry;
import com.vincenthuto.hemomancy.client.render.RenderTypeInit;
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
			float[] mouth = AlembicGeometry.FIREBOX;
			VertexConsumer channel = bufferIn.getBuffer(RenderTypeInit.FLASK_FLUID);
			renderColorBox(channel, poseStack.last().pose(), mouth[0] / 16, mouth[1] / 16, mouth[4] / 16,
					mouth[2] / 16, mouth[3] / 16, (mouth[4] + 0.05f) / 16, 176, 18, 28, (int) (80 + pulse * 45));
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
		// Translucent either way: the cucurbit and receivers are glass the fluid shows through.
		var type = RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS);
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
		float bottom = AlembicGeometry.FLUID_BOTTOM / 16f;
		float height = (AlembicGeometry.FLUID_TOP - AlembicGeometry.FLUID_BOTTOM) / 16f * fillPct;

		poseStack.pushPose();
		poseStack.translate(0.5f, bottom + (te.tier() == AlembicTier.ATHANOR ? AlembicGeometry.ATHANOR_LIFT : 0), 0.5f);
		renderOctagon(bufferIn.getBuffer(RenderTypeInit.FLASK_FLUID), poseStack.last().pose(),
				AlembicGeometry.FLUID_RADIUS / 16f, height, 153, 13, 13, 191);
		poseStack.popPose();
	}

	/** Octagonal column matching the cucurbit's glass, flats facing the four sides. */
	private static void renderOctagon(VertexConsumer vc, Matrix4f mat, float radius, float height,
			int r, int g, int b, int a) {
		float[] x = new float[8], z = new float[8];
		float corner = radius / (float) Math.cos(Math.PI / 8);
		for (int i = 0; i < 8; i++) {
			double angle = Math.PI / 8 + i * Math.PI / 4;
			x[i] = (float) Math.cos(angle) * corner;
			z[i] = (float) Math.sin(angle) * corner;
		}
		for (int i = 0; i < 8; i++) {
			int j = (i + 1) % 8;
			vc.addVertex(mat, x[i], height, z[i]).setColor(r, g, b, a);
			vc.addVertex(mat, x[j], height, z[j]).setColor(r, g, b, a);
			vc.addVertex(mat, x[j], 0, z[j]).setColor(r, g, b, a);
			vc.addVertex(mat, x[i], 0, z[i]).setColor(r, g, b, a);
		}
		// Each cap is three quads: two trapezoids either side of a central rectangle.
		int[][] cap = {{0, 1, 2, 3}, {0, 3, 4, 7}, {4, 5, 6, 7}};
		for (int[] quad : cap) {
			for (int k : quad) vc.addVertex(mat, x[k], height, z[k]).setColor(r, g, b, a);
			for (int k = 3; k >= 0; k--) vc.addVertex(mat, x[quad[k]], 0, z[quad[k]]).setColor(r, g, b, a);
		}
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
