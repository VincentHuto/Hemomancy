package com.vincenthuto.hemomancy.client.render.tile.harbinger.decoration;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.decoration.NonEuclideanHallwayBlockEntity;
import com.vincenthuto.hemomancy.common.world.fold.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class NonEuclideanHallwayRenderer implements BlockEntityRenderer<NonEuclideanHallwayBlockEntity> {
	private static final int VISIBLE_SEGMENTS_WHILE_INSIDE = 20;
	private static final double FRAME_DEPTH = 0.0D;
	private static final double APERTURE_DEPTH = 0.0D;
	private static final double HALLWAY_HALF_WIDTH = 1.0D;
	private static final double HALLWAY_HEIGHT = 3.0D;

	public NonEuclideanHallwayRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public boolean shouldRenderOffScreen(NonEuclideanHallwayBlockEntity blockEntity) {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 256;
	}

	@Override
	public AABB getRenderBoundingBox(NonEuclideanHallwayBlockEntity blockEntity) {
		return AABB.INFINITE;
	}

	@Override
	public void render(NonEuclideanHallwayBlockEntity blockEntity, float partialTick, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight, int packedOverlay) {
		if (blockEntity.getLevel() == null) {
			return;
		}
		BlockState state = blockEntity.getBlockState();
		if (!state.is(BlockInit.non_euclidean_hallway.get())) {
			return;
		}

		FoldedHallwaySpec spec = FoldMinecraftAdapter.fromController(blockEntity.getLevel().dimension(),
				blockEntity.getBlockPos(), state);
		Player player = Minecraft.getInstance().player;
		FoldTravelerState traveler = player != null
				? FoldedHallwayManager.getState(player).filter(active -> spec.equals(active.activeFold())).orElse(null)
				: null;
		FoldViewerSide viewerSide = resolveViewerSide(spec, player, traveler, partialTick);

		renderFrameAndAperture(poseStack, blockEntity.getBlockPos(), spec, viewerSide);
		renderHallwayThroughAperture(poseStack, blockEntity.getBlockPos(), spec, viewerSide, traveler);
	}

	private static FoldViewerSide resolveViewerSide(FoldedHallwaySpec spec, Player player,
			FoldTravelerState traveler, float partialTick) {
		FoldTransform transform = FoldTransform.of(spec);
		if (player == null) {
			return FoldViewerSide.FRONT;
		}
		if (traveler != null) {
			Vec3 look = player.getViewVector(partialTick);
			double forwardDot = look.x * spec.facing().forwardX() + look.z * spec.facing().forwardZ();
			return forwardDot >= 0.0D ? FoldViewerSide.FRONT : FoldViewerSide.BACK;
		}

		Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
		return transform.viewerSide(FoldMinecraftAdapter.fromVec3(camera));
	}

	private static void renderHallwayThroughAperture(PoseStack poseStack, BlockPos anchor, FoldedHallwaySpec spec,
			FoldViewerSide viewerSide, FoldTravelerState traveler) {
		double realDepthProgress = 0.0D;
		double virtualDepthProgress = 0.0D;
		boolean inside = traveler != null;
		if (traveler != null) {
			FoldViewerSide entrySide = traveler.entrySide() == null ? viewerSide : traveler.entrySide();
			if (viewerSide == entrySide) {
				realDepthProgress = traveler.realDepthProgress();
				virtualDepthProgress = traveler.virtualDepthProgress();
			} else {
				realDepthProgress = spec.realDepth() - traveler.realDepthProgress();
				virtualDepthProgress = spec.virtualLength() - traveler.virtualDepthProgress();
			}
		}

		if (!FoldRenderWindow.needsApertureStencil(inside)) {
			renderHallwayMesh(poseStack, anchor, spec, viewerSide, realDepthProgress, virtualDepthProgress, true);
			restoreImmediateRenderState();
			return;
		}

		Minecraft.getInstance().getMainRenderTarget().enableStencil();

		GL11.glEnable(GL11.GL_STENCIL_TEST);
		GL11.glStencilMask(0xFF);
		GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
		GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
		GL11.glColorMask(false, false, false, false);
		GL11.glDepthMask(false);
		RenderSystem.disableDepthTest();

		renderApertureMask(poseStack, anchor, spec, viewerSide);

		GL11.glColorMask(true, true, true, true);
		GL11.glDepthMask(true);
		GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
		GL11.glStencilMask(0x00);

		renderHallwayMesh(poseStack, anchor, spec, viewerSide, realDepthProgress, virtualDepthProgress, inside);

		GL11.glStencilMask(0xFF);
		GL11.glDisable(GL11.GL_STENCIL_TEST);
		restoreImmediateRenderState();
	}

	private static void renderFrameAndAperture(PoseStack poseStack, BlockPos anchor, FoldedHallwaySpec spec,
			FoldViewerSide viewerSide) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableCull();
		RenderSystem.depthMask(false);
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		Matrix4f matrix = poseStack.last().pose();
		BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
				DefaultVertexFormat.POSITION_COLOR);

		drawPlaneQuad(builder, matrix, anchor, spec, viewerSide, -1.35D, 0.0D, -1.0D, 3.35D, FRAME_DEPTH,
				87, 18, 24, 210);
		drawPlaneQuad(builder, matrix, anchor, spec, viewerSide, 1.0D, 0.0D, 1.35D, 3.35D, FRAME_DEPTH,
				87, 18, 24, 210);
		drawPlaneQuad(builder, matrix, anchor, spec, viewerSide, -1.35D, 3.0D, 1.35D, 3.35D, FRAME_DEPTH,
				96, 23, 30, 220);
		drawPlaneQuad(builder, matrix, anchor, spec, viewerSide, -1.35D, 0.0D, 1.35D, 0.14D, FRAME_DEPTH,
				53, 12, 17, 220);
		drawPlaneQuad(builder, matrix, anchor, spec, viewerSide, -1.0D, 0.14D, 1.0D, 3.0D, FRAME_DEPTH + 0.01D,
				24, 126, 108, 42);

		BufferUploader.drawWithShader(builder.buildOrThrow());
		restoreImmediateRenderState();
	}

	private static void renderApertureMask(PoseStack poseStack, BlockPos anchor, FoldedHallwaySpec spec,
			FoldViewerSide viewerSide) {
		RenderSystem.disableCull();
		RenderSystem.setShader(GameRenderer::getPositionShader);
		Matrix4f matrix = poseStack.last().pose();
		BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
				DefaultVertexFormat.POSITION);
		addPosition(builder, matrix, anchor, spec, viewerSide, -1.0D, 0.0D, APERTURE_DEPTH);
		addPosition(builder, matrix, anchor, spec, viewerSide, -1.0D, 3.0D, APERTURE_DEPTH);
		addPosition(builder, matrix, anchor, spec, viewerSide, 1.0D, 3.0D, APERTURE_DEPTH);
		addPosition(builder, matrix, anchor, spec, viewerSide, 1.0D, 0.0D, APERTURE_DEPTH);
		BufferUploader.drawWithShader(builder.buildOrThrow());
	}

	private static void renderHallwayMesh(PoseStack poseStack, BlockPos anchor, FoldedHallwaySpec spec,
			FoldViewerSide viewerSide, double realDepthProgress, double virtualDepthProgress, boolean inside) {
		RenderSystem.disableBlend();
		RenderSystem.disableCull();
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		Matrix4f matrix = poseStack.last().pose();
		BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
				DefaultVertexFormat.POSITION_COLOR);

		double virtualStart = inside ? Math.max(0.0D, Math.floor(virtualDepthProgress) - 2.0D) : 0.0D;
		double virtualEnd = inside
				? Math.min(spec.virtualLength(), Math.ceil(virtualDepthProgress) + VISIBLE_SEGMENTS_WHILE_INSIDE)
				: spec.virtualLength();
		double depthOffset = realDepthProgress - virtualDepthProgress;

		for (int segment = (int) virtualStart; segment < (int) virtualEnd; segment++) {
			double d0 = segment + depthOffset;
			double d1 = segment + 1.0D + depthOffset;
			FoldRenderWindow.DepthSpan span = inside
					? FoldRenderWindow.clipForInsideCamera(viewerSide, realDepthProgress, d0, d1)
					: FoldRenderWindow.unclipped(d0, d1);
			if (!span.visible()) {
				continue;
			}
			d0 = span.start();
			d1 = span.end();
			boolean marker = segment % 4 == 0;
			int floorR = marker ? 44 : 30;
			int floorG = marker ? 78 : 54;
			int floorB = marker ? 64 : 50;
			int wallR = marker ? 74 : 52;
			int wallG = marker ? 28 : 22;
			int wallB = marker ? 38 : 30;

			drawDepthQuad(builder, matrix, anchor, spec, viewerSide, -HALLWAY_HALF_WIDTH, 0.02D, d0,
					HALLWAY_HALF_WIDTH, 0.02D, d1, floorR, floorG, floorB, 255);
			drawDepthQuad(builder, matrix, anchor, spec, viewerSide, -HALLWAY_HALF_WIDTH, HALLWAY_HEIGHT, d1,
					HALLWAY_HALF_WIDTH, HALLWAY_HEIGHT, d0, 18, 16, 22, 255);
			drawSideQuad(builder, matrix, anchor, spec, viewerSide, -HALLWAY_HALF_WIDTH, 0.0D, d1,
					HALLWAY_HEIGHT, d0, wallR, wallG, wallB, 255);
			drawSideQuad(builder, matrix, anchor, spec, viewerSide, HALLWAY_HALF_WIDTH, 0.0D, d0,
					HALLWAY_HEIGHT, d1, wallR, wallG, wallB, 255);
		}

		var meshData = builder.build();
		if (meshData != null) {
			BufferUploader.drawWithShader(meshData);
		}
	}

	private static void drawPlaneQuad(BufferBuilder builder, Matrix4f matrix, BlockPos anchor, FoldedHallwaySpec spec,
			FoldViewerSide viewerSide, double lateral0, double vertical0, double lateral1, double vertical1,
			double depth, int red, int green, int blue, int alpha) {
		addColored(builder, matrix, anchor, spec, viewerSide, lateral0, vertical0, depth, red, green, blue, alpha);
		addColored(builder, matrix, anchor, spec, viewerSide, lateral0, vertical1, depth, red, green, blue, alpha);
		addColored(builder, matrix, anchor, spec, viewerSide, lateral1, vertical1, depth, red, green, blue, alpha);
		addColored(builder, matrix, anchor, spec, viewerSide, lateral1, vertical0, depth, red, green, blue, alpha);
	}

	private static void drawDepthQuad(BufferBuilder builder, Matrix4f matrix, BlockPos anchor, FoldedHallwaySpec spec,
			FoldViewerSide viewerSide, double lateral0, double vertical0, double depth0, double lateral1,
			double vertical1, double depth1, int red, int green, int blue, int alpha) {
		addColored(builder, matrix, anchor, spec, viewerSide, lateral0, vertical0, depth0, red, green, blue, alpha);
		addColored(builder, matrix, anchor, spec, viewerSide, lateral0, vertical1, depth1, red, green, blue, alpha);
		addColored(builder, matrix, anchor, spec, viewerSide, lateral1, vertical1, depth1, red, green, blue, alpha);
		addColored(builder, matrix, anchor, spec, viewerSide, lateral1, vertical0, depth0, red, green, blue, alpha);
	}

	private static void drawSideQuad(BufferBuilder builder, Matrix4f matrix, BlockPos anchor, FoldedHallwaySpec spec,
			FoldViewerSide viewerSide, double lateral, double vertical0, double depth0, double vertical1,
			double depth1, int red, int green, int blue, int alpha) {
		addColored(builder, matrix, anchor, spec, viewerSide, lateral, vertical0, depth0, red, green, blue, alpha);
		addColored(builder, matrix, anchor, spec, viewerSide, lateral, vertical1, depth0, red, green, blue, alpha);
		addColored(builder, matrix, anchor, spec, viewerSide, lateral, vertical1, depth1, red, green, blue, alpha);
		addColored(builder, matrix, anchor, spec, viewerSide, lateral, vertical0, depth1, red, green, blue, alpha);
	}

	private static void addPosition(BufferBuilder builder, Matrix4f matrix, BlockPos anchor, FoldedHallwaySpec spec,
			FoldViewerSide viewerSide, double lateral, double vertical, double depth) {
		FoldVector offset = toRenderOffset(anchor, spec, viewerSide, lateral, vertical, depth);
		builder.addVertex(matrix, (float) offset.x, (float) offset.y, (float) offset.z);
	}

	private static void addColored(BufferBuilder builder, Matrix4f matrix, BlockPos anchor, FoldedHallwaySpec spec,
			FoldViewerSide viewerSide, double lateral, double vertical, double depth, int red, int green, int blue,
			int alpha) {
		FoldVector offset = toRenderOffset(anchor, spec, viewerSide, lateral, vertical, depth);
		builder.addVertex(matrix, (float) offset.x, (float) offset.y, (float) offset.z)
				.setColor(red, green, blue, alpha);
	}

	private static FoldVector toRenderOffset(BlockPos anchor, FoldedHallwaySpec spec, FoldViewerSide viewerSide,
			double lateral, double vertical, double depth) {
		FoldVector world = FoldTransform.of(spec).toVisualWorld(viewerSide, lateral, vertical, depth);
		return new FoldVector(world.x - anchor.getX(), world.y - anchor.getY(), world.z - anchor.getZ());
	}

	private static void restoreImmediateRenderState() {
		GL11.glColorMask(true, true, true, true);
		GL11.glDepthMask(true);
		GL11.glStencilMask(0xFF);
		GL11.glDisable(GL11.GL_STENCIL_TEST);
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.enableCull();
		RenderSystem.disableBlend();
	}
}
