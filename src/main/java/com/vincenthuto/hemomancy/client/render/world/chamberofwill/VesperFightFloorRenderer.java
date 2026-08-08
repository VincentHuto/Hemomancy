package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.client.data.VesperFightClientData;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.config.HemoClientConfig;
import com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

public final class VesperFightFloorRenderer {
	private static VertexBuffer opaqueBuffer;
	private static VertexBuffer horizonBuffer;
	private static BlockPos cachedCenter;
	private static VesperFightArenaLayout cachedLayout;
	private static VesperFightOuterRingDetail cachedOuterRingDetail;
	private static float lightningPulse;

	private VesperFightFloorRenderer() {
	}

	public static void renderOpaque(RenderLevelStageEvent event) {
		if (!canRender()) return;
		BlockPos center = VesperFightClientData.center();
		if (!ensureOpaqueBuffer(center)) return;

		Matrix4f modelView = VesperFightFloorTransform.arenaModelView(
				event.getModelViewMatrix(), event.getCamera().getPosition(), center);
		RenderSystem.enableDepthTest();
		RenderSystem.depthMask(true);
		RenderSystem.disableCull();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		opaqueBuffer.bind();
		opaqueBuffer.drawWithShader(modelView, event.getProjectionMatrix(),
				GameRenderer.getPositionColorShader());
		VertexBuffer.unbind();
		RenderSystem.enableCull();
		renderMonolithRocks(event, center);
	}

	public static void renderFissures(PoseStack poseStack, float partialTick) {
		if (!canRender()) return;
		BlockPos center = VesperFightClientData.center();
		if (cachedLayout == null || !center.equals(cachedCenter)) {
			cachedLayout = VesperFightArenaLayout.generate(center);
			cachedCenter = center.immutable();
		}

		Minecraft minecraft = Minecraft.getInstance();
		translateToArena(poseStack, minecraft.gameRenderer.getMainCamera(), center);
		float time = minecraft.level.getGameTime() + partialTick;
		float idlePulse = 0.08F + 0.05F * Mth.sin(time * 0.045F);
		float pulse = Mth.clamp(idlePulse + lightningPulse, 0.0F, 1.0F);
		lightningPulse *= 0.88F;

		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
		RenderSystem.enableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.disableCull();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
				DefaultVertexFormat.POSITION_COLOR);
		Matrix4f matrix = poseStack.last().pose();
		for (int fissureIndex = 0; fissureIndex < cachedLayout.fissures().size(); fissureIndex++) {
			VesperFightArenaLayout.Fissure fissure = cachedLayout.fissures().get(fissureIndex);
			float brightness = Mth.clamp(fissure.intensity() + pulse * 0.42F, 0.0F, 1.0F);
			int red = (int) Mth.lerp(brightness, 82.0F, 196.0F);
			int green = (int) Mth.lerp(brightness, 4.0F, 24.0F);
			int blue = (int) Mth.lerp(brightness, 10.0F, 36.0F);
			int baseAlpha = (int) Mth.lerp(brightness, 105.0F, 220.0F);
			int segmentCount = fissure.points().size() - 1;
			for (int point = 0; point < segmentCount; point++) {
				var start = fissure.points().get(point);
				var end = fissure.points().get(point + 1);
				float alphaScale = VesperFightFissureStyle.alphaScale(
						time, fissureIndex, point, segmentCount);
				emitRibbon(buffer, matrix, start.x(), start.z(), end.x(), end.z(),
						fissure.width() * (2.35F + pulse * 0.65F), VesperFightFissureStyle.GLOW_Y,
						red, green, blue, VesperFightFissureStyle.glowAlpha(baseAlpha, alphaScale));
				emitRibbon(buffer, matrix, start.x(), start.z(), end.x(), end.z(),
						fissure.width() * (0.48F + pulse * 0.20F), VesperFightFissureStyle.SURFACE_Y,
						Math.min(255, red + 34), Math.min(255, green + 8), Math.min(255, blue + 12),
						VesperFightFissureStyle.coreAlpha(baseAlpha, alphaScale));
			}
		}
		MeshData mesh = buffer.build();
		if (mesh != null) {
			com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(mesh);
		}
		RenderSystem.depthMask(true);
		RenderSystem.enableCull();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableBlend();
		poseStack.popPose();
	}

	public static void renderFadingPerimeter(RenderLevelStageEvent event) {
		if (!canRender()) return;
		BlockPos center = VesperFightClientData.center();
		if (!ensureOpaqueBuffer(center) || horizonBuffer == null) return;
		Matrix4f modelView = VesperFightFloorTransform.arenaModelView(
				event.getModelViewMatrix(), event.getCamera().getPosition(), center);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		RenderSystem.depthMask(cachedOuterRingDetail.writesDepth());
		RenderSystem.disableCull();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		horizonBuffer.bind();
		horizonBuffer.drawWithShader(modelView, event.getProjectionMatrix(),
				GameRenderer.getPositionColorShader());
		VertexBuffer.unbind();
		RenderSystem.depthMask(true);
		RenderSystem.enableCull();
		RenderSystem.disableBlend();
	}

	/** Called by the later storm-lightning renderer to illuminate the floor seams. */
	public static void flashFromDistantLightning(float strength) {
		lightningPulse = Math.max(lightningPulse, Mth.clamp(strength, 0.0F, 1.0F));
	}

	public static void clear() {
		if (opaqueBuffer != null) {
			opaqueBuffer.close();
			opaqueBuffer = null;
		}
		if (horizonBuffer != null) {
			horizonBuffer.close();
			horizonBuffer = null;
		}
		cachedCenter = null;
		cachedLayout = null;
		cachedOuterRingDetail = null;
		lightningPulse = 0.0F;
	}

	private static boolean canRender() {
		Minecraft minecraft = Minecraft.getInstance();
		return VesperFightClientData.isActive()
				&& minecraft.level != null
				&& minecraft.level.dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL);
	}

	private static boolean ensureOpaqueBuffer(BlockPos center) {
		VesperFightOuterRingDetail outerRingDetail = VesperFightOuterRingDetail.fromLowPolyToggle(
				HemoClientConfig.useLowPolyVesperFightOuterRing());
		if (opaqueBuffer != null && horizonBuffer != null && center.equals(cachedCenter)
				&& outerRingDetail == cachedOuterRingDetail) return true;
		VesperFightArenaLayout layout = VesperFightArenaLayout.generate(center);
		BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
				DefaultVertexFormat.POSITION_COLOR);
		emitBox(buffer, -25.0F, -0.16F, -25.0F, 25.0F, -0.035F, 25.0F,
				38, 3, 9, 255, 22, 2, 6, 255);
		for (VesperFightArenaLayout.Tile tile : layout.tiles()) {
			if (!tile.missing()) emitTile(buffer, tile, 255);
		}
		MeshData mesh = buffer.build();
		if (mesh == null) return false;
		if (opaqueBuffer != null) opaqueBuffer.close();
		opaqueBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
		opaqueBuffer.bind();
		opaqueBuffer.upload(mesh);
		VertexBuffer.unbind();

		BufferBuilder horizonBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
				DefaultVertexFormat.POSITION_COLOR);
		for (VesperFightArenaLayout.HorizonWound wound : layout.horizonWounds()) {
			emitHorizonWound(horizonBuilder, wound);
		}
		for (VesperFightArenaLayout.HorizonTile tile : layout.horizonTiles()) {
			emitHorizonTile(horizonBuilder, tile, outerRingDetail);
		}
		MeshData horizonMesh = horizonBuilder.build();
		if (horizonMesh == null) return false;
		if (horizonBuffer != null) horizonBuffer.close();
		horizonBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
		horizonBuffer.bind();
		horizonBuffer.upload(horizonMesh);
		VertexBuffer.unbind();
		cachedCenter = center.immutable();
		cachedLayout = layout;
		cachedOuterRingDetail = outerRingDetail;
		return true;
	}

	private static void emitHorizonWound(BufferBuilder buffer, VesperFightArenaLayout.HorizonWound wound) {
		quad(buffer, new Matrix4f(), wound.x(), wound.height(), wound.z(),
				wound.x() + 1.0F, wound.height(), wound.z(),
				wound.x() + 1.0F, wound.height(), wound.z() + 1.0F,
				wound.x(), wound.height(), wound.z() + 1.0F,
				38, 3, 9, wound.alpha());
	}

	private static void emitHorizonTile(BufferBuilder buffer, VesperFightArenaLayout.HorizonTile tile,
			VesperFightOuterRingDetail detail) {
		if (detail == VesperFightOuterRingDetail.HIGH_RES) {
			emitTile(buffer, tile.asInteriorTile(), tile.alpha());
			return;
		}
		float inset = tile.chipAmount();
		int coordinateTone = Math.floorMod(tile.x() * 19 + tile.z() * 31, 11) - 5;
		int red = (tile.material() == VesperFightArenaLayout.Material.BASALT ? 22 : 120) + coordinateTone;
		int green = (tile.material() == VesperFightArenaLayout.Material.BASALT ? 20 : 113) + coordinateTone;
		int blue = (tile.material() == VesperFightArenaLayout.Material.BASALT ? 25 : 102) + coordinateTone;
		switch (tile.damage()) {
			case INTACT -> emitHorizonLocalQuad(buffer, tile, inset, inset,
					1.0F - inset, 1.0F - inset, 0.0F, red, green, blue, detail);
			case CHIPPED_CORNER -> {
				float cut = 0.25F + inset * 1.5F;
				emitHorizonLocalQuad(buffer, tile, inset, inset,
						1.0F - cut, 1.0F - inset, 0.0F, red, green, blue, detail);
				emitHorizonLocalQuad(buffer, tile, 1.0F - cut, inset,
						1.0F - inset, 1.0F - cut, 0.006F, red, green, blue, detail);
			}
			case FRACTURED -> {
				float gap = 0.035F + inset;
				emitHorizonLocalQuad(buffer, tile, inset, inset,
						0.5F - gap, 1.0F - inset, -0.008F, red - 2, green - 2, blue - 1, detail);
				emitHorizonLocalQuad(buffer, tile, 0.5F + gap, inset,
						1.0F - inset, 1.0F - inset, 0.018F, red + 2, green + 2, blue + 2, detail);
			}
			case SHARD -> emitHorizonShard(buffer, tile, red, green, blue, detail);
		}
	}

	private static void emitHorizonLocalQuad(BufferBuilder buffer, VesperFightArenaLayout.HorizonTile tile,
			float u0, float v0, float u1, float v1, float heightOffset,
			int red, int green, int blue, VesperFightOuterRingDetail detail) {
		emitHorizonPatch(buffer, tile,
				u0, v0, u1, v0, u1, v1, u0, v1,
				heightOffset, heightOffset, heightOffset, heightOffset,
				red, green, blue, detail);
	}

	private static void emitHorizonShard(BufferBuilder buffer, VesperFightArenaLayout.HorizonTile tile,
			int red, int green, int blue, VesperFightOuterRingDetail detail) {
		emitHorizonPatch(buffer, tile,
				0.18F, 0.39F, 0.55F, 0.15F, 0.84F, 0.57F, 0.40F, 0.86F,
				0.035F, 0.047F, 0.035F, 0.027F,
				red - 3, green - 3, blue - 2, detail);
	}

	private static void emitHorizonPatch(BufferBuilder buffer, VesperFightArenaLayout.HorizonTile tile,
			float u00, float v00, float u10, float v10, float u11, float v11, float u01, float v01,
			float y00, float y10, float y11, float y01,
			int red, int green, int blue, VesperFightOuterRingDetail detail) {
		Matrix4f identity = new Matrix4f();
		int subdivisions = detail.subdivisions();
		for (int gridX = 0; gridX < subdivisions; gridX++) {
			float s0 = gridX / (float) subdivisions;
			float s1 = (gridX + 1) / (float) subdivisions;
			for (int gridZ = 0; gridZ < subdivisions; gridZ++) {
				float t0 = gridZ / (float) subdivisions;
				float t1 = (gridZ + 1) / (float) subdivisions;
				emitHorizonPatchVertex(buffer, identity, tile, s0, t0,
						u00, v00, u10, v10, u11, v11, u01, v01, y00, y10, y11, y01,
						gridX, gridZ, red, green, blue, detail);
				emitHorizonPatchVertex(buffer, identity, tile, s1, t0,
						u00, v00, u10, v10, u11, v11, u01, v01, y00, y10, y11, y01,
						gridX + 1, gridZ, red, green, blue, detail);
				emitHorizonPatchVertex(buffer, identity, tile, s1, t1,
						u00, v00, u10, v10, u11, v11, u01, v01, y00, y10, y11, y01,
						gridX + 1, gridZ + 1, red, green, blue, detail);
				emitHorizonPatchVertex(buffer, identity, tile, s0, t1,
						u00, v00, u10, v10, u11, v11, u01, v01, y00, y10, y11, y01,
						gridX, gridZ + 1, red, green, blue, detail);
			}
		}
	}

	private static void emitHorizonPatchVertex(BufferBuilder buffer, Matrix4f matrix,
			VesperFightArenaLayout.HorizonTile tile, float s, float t,
			float u00, float v00, float u10, float v10, float u11, float v11, float u01, float v01,
			float y00, float y10, float y11, float y01, int gridX, int gridZ,
			int red, int green, int blue, VesperFightOuterRingDetail detail) {
		float localU = bilerp(u00, u10, u11, u01, s, t);
		float localV = bilerp(v00, v10, v11, v01, s, t);
		float height = tile.height() + bilerp(y00, y10, y11, y01, s, t);
		buffer.addVertex(matrix, horizonX(tile, localU, localV), height, horizonZ(tile, localU, localV))
				.setColor(red, green, blue, tile.alpha());
	}

	private static float bilerp(float value00, float value10, float value11, float value01,
			float s, float t) {
		return Mth.lerp(t, Mth.lerp(s, value00, value10), Mth.lerp(s, value01, value11));
	}

	private static float horizonX(VesperFightArenaLayout.HorizonTile tile, float u, float v) {
		return tile.x() + switch (tile.damageRotation() & 3) {
			case 1 -> 1.0F - v;
			case 2 -> 1.0F - u;
			case 3 -> v;
			default -> u;
		};
	}

	private static float horizonZ(VesperFightArenaLayout.HorizonTile tile, float u, float v) {
		return tile.z() + switch (tile.damageRotation() & 3) {
			case 1 -> u;
			case 2 -> 1.0F - v;
			case 3 -> 1.0F - u;
			default -> v;
		};
	}

	private static void emitTile(BufferBuilder buffer, VesperFightArenaLayout.Tile tile, int alpha) {
		float chip = 0.006F + tile.chipAmount();
		float x0 = tile.x() + chip * (tile.variant() == VesperFightArenaLayout.SurfaceVariant.CHIPPED ? 1.35F : 1.0F);
		float z0 = tile.z() + chip;
		float x1 = tile.x() + 1.0F - chip;
		float z1 = tile.z() + 1.0F - chip * (tile.variant() == VesperFightArenaLayout.SurfaceVariant.CHIPPED ? 1.25F : 1.0F);
		float base = 0.012F + tile.heightOffset();
		float y00 = base - tile.tiltX() - tile.tiltZ();
		float y10 = base + tile.tiltX() - tile.tiltZ();
		float y11 = base + tile.tiltX() + tile.tiltZ();
		float y01 = base - tile.tiltX() + tile.tiltZ();
		int[] color = tileColor(tile);
		int sideRed = Math.max(4, color[0] - 24);
		int sideGreen = Math.max(3, color[1] - 22);
		int sideBlue = Math.max(4, color[2] - 20);
		Matrix4f identity = new Matrix4f();
		emitSubdividedTop(buffer, identity, tile, x0, z0, x1, z1, y00, y10, y11, y01, color, alpha);
		float bottom = VesperFightStoneSurface.slabBottom(y00, y10, y11, y01);
		quad(buffer, identity, x0, bottom, z0, x1, bottom, z0, x1, y10, z0, x0, y00, z0,
				sideRed, sideGreen, sideBlue, alpha);
		quad(buffer, identity, x1, bottom, z0, x1, bottom, z1, x1, y11, z1, x1, y10, z0,
				sideRed, sideGreen, sideBlue, alpha);
		quad(buffer, identity, x1, bottom, z1, x0, bottom, z1, x0, y01, z1, x1, y11, z1,
				sideRed, sideGreen, sideBlue, alpha);
		quad(buffer, identity, x0, bottom, z1, x0, bottom, z0, x0, y00, z0, x0, y01, z1,
				sideRed, sideGreen, sideBlue, alpha);
	}

	private static void emitSubdividedTop(BufferBuilder buffer, Matrix4f matrix,
			VesperFightArenaLayout.Tile tile, float x0, float z0, float x1, float z1,
			float y00, float y10, float y11, float y01, int[] color, int alpha) {
		int divisions = VesperFightStoneSurface.SUBDIVISIONS;
		for (int gridX = 0; gridX < divisions; gridX++) {
			float u0 = gridX / (float) divisions;
			float u1 = (gridX + 1) / (float) divisions;
			float surfaceX0 = Mth.lerp(u0, x0, x1);
			float surfaceX1 = Mth.lerp(u1, x0, x1);
			for (int gridZ = 0; gridZ < divisions; gridZ++) {
				float v0 = gridZ / (float) divisions;
				float v1 = (gridZ + 1) / (float) divisions;
				float surfaceZ0 = Mth.lerp(v0, z0, z1);
				float surfaceZ1 = Mth.lerp(v1, z0, z1);
				emitSurfaceVertex(buffer, matrix, tile, gridX, gridZ, surfaceX0,
						surfaceHeight(tile, gridX, gridZ, u0, v0, y00, y10, y11, y01), surfaceZ0, color, alpha);
				emitSurfaceVertex(buffer, matrix, tile, gridX + 1, gridZ, surfaceX1,
						surfaceHeight(tile, gridX + 1, gridZ, u1, v0, y00, y10, y11, y01), surfaceZ0, color, alpha);
				emitSurfaceVertex(buffer, matrix, tile, gridX + 1, gridZ + 1, surfaceX1,
						surfaceHeight(tile, gridX + 1, gridZ + 1, u1, v1, y00, y10, y11, y01), surfaceZ1, color, alpha);
				emitSurfaceVertex(buffer, matrix, tile, gridX, gridZ + 1, surfaceX0,
						surfaceHeight(tile, gridX, gridZ + 1, u0, v1, y00, y10, y11, y01), surfaceZ1, color, alpha);
			}
		}
	}

	private static float surfaceHeight(VesperFightArenaLayout.Tile tile, int gridX, int gridZ,
			float u, float v, float y00, float y10, float y11, float y01) {
		float north = Mth.lerp(u, y00, y10);
		float south = Mth.lerp(u, y01, y11);
		return Mth.lerp(v, north, south) + VesperFightStoneSurface.relief(tile, gridX, gridZ);
	}

	private static void emitSurfaceVertex(BufferBuilder buffer, Matrix4f matrix,
			VesperFightArenaLayout.Tile tile, int gridX, int gridZ,
			float x, float y, float z, int[] color, int alpha) {
		int tone = VesperFightStoneSurface.tone(tile, gridX, gridZ);
		buffer.addVertex(matrix, x, y, z).setColor(
				Math.max(0, Math.min(255, color[0] + tone)),
				Math.max(0, Math.min(255, color[1] + tone)),
				Math.max(0, Math.min(255, color[2] + tone)), alpha);
	}

	private static int[] tileColor(VesperFightArenaLayout.Tile tile) {
		int red = tile.material() == VesperFightArenaLayout.Material.BASALT ? 24 : 126;
		int green = tile.material() == VesperFightArenaLayout.Material.BASALT ? 22 : 118;
		int blue = tile.material() == VesperFightArenaLayout.Material.BASALT ? 27 : 106;
		int roughness = (int) (tile.chipAmount() * 92.0F) - 5;
		red += roughness;
		green += roughness;
		blue += roughness;
		return switch (tile.variant()) {
			case CHIPPED -> new int[] {red - 4, green - 4, blue - 3};
			case STAINED -> new int[] {red - 13, green - 16, blue - 13};
			case CRACKED -> new int[] {red + 5, green + 3, blue + 2};
		};
	}

	private static void renderMonolithRocks(RenderLevelStageEvent event, BlockPos center) {
		if (cachedLayout == null || !center.equals(cachedCenter)) return;
		Minecraft minecraft = Minecraft.getInstance();
		float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
		float time = minecraft.level.getGameTime() + partialTick;
		Matrix4f rockModelView = VesperFightFloorTransform.arenaModelView(
				event.getModelViewMatrix(), event.getCamera().getPosition(), center);
		PoseStack poseStack = new PoseStack();
		poseStack.mulPose(rockModelView);
		var modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushMatrix();
		modelViewStack.identity();
		RenderSystem.applyModelViewMatrix();

		try {
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableBlend();
			RenderSystem.disableCull();
			RenderSystem.enableDepthTest();
			MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
			for (int rockIndex = 0; rockIndex < cachedLayout.rocks().size(); rockIndex++) {
				VesperFightArenaLayout.Rock rock = cachedLayout.rocks().get(rockIndex);
				float seed = (rockIndex * 37 + 11) / 997.0F;
				RenderType renderType = HemoRenderTypes.monolithEntitySurface(
						time * 0.05F, seed, 0.72F, 0.90F, 10.5F);
				VertexConsumer consumer = bufferSource.getBuffer(renderType);
				emitMonolithRock(poseStack, consumer, rock, rockIndex);
				bufferSource.endBatch(renderType);
			}
		} finally {
			modelViewStack.popMatrix();
			RenderSystem.applyModelViewMatrix();
			RenderSystem.enableCull();
			RenderSystem.defaultBlendFunc();
			RenderSystem.disableBlend();
		}
	}

	private static void emitMonolithRock(PoseStack poseStack, VertexConsumer consumer,
			VesperFightArenaLayout.Rock rock, int rockIndex) {
		float half = rock.width() * 0.5F;
		float radians = rock.rotationDegrees() * Mth.DEG_TO_RAD;
		float cos = Mth.cos(radians);
		float sin = Mth.sin(radians);
		float[][] bottom = rotatedSquare(rock.x(), rock.z(), half, cos, sin);
		float[][] top = rotatedSquare(rock.x(), rock.z(), half * 0.34F, cos, sin);
		PoseStack.Pose pose = poseStack.last();
		Matrix4f matrix = pose.pose();
		float vTop = Math.max(1.0F, rock.height() / Math.max(0.5F, rock.width()));
		int color = rockIndex % 5 == 0 ? 0xFF110708 : 0xFF080607;
		for (int side = 0; side < 4; side++) {
			int next = (side + 1) & 3;
			float edgeX = bottom[next][0] - bottom[side][0];
			float edgeZ = bottom[next][1] - bottom[side][1];
			float edgeLength = Mth.sqrt(edgeX * edgeX + edgeZ * edgeZ);
			float normalX = edgeZ / edgeLength;
			float normalZ = -edgeX / edgeLength;
			float u0 = side * 0.25F;
			float u1 = (side + 1) * 0.25F;
			addMonolithVertex(consumer, pose, matrix, bottom[side][0], -0.02F, bottom[side][1],
					u0, 0.0F, color, normalX, 0.0F, normalZ);
			addMonolithVertex(consumer, pose, matrix, bottom[next][0], -0.02F, bottom[next][1],
					u1, 0.0F, color, normalX, 0.0F, normalZ);
			addMonolithVertex(consumer, pose, matrix, top[next][0], rock.height(), top[next][1],
					u1, vTop, color, normalX, 0.0F, normalZ);
			addMonolithVertex(consumer, pose, matrix, top[side][0], rock.height(), top[side][1],
					u0, vTop, color, normalX, 0.0F, normalZ);
		}
		addMonolithVertex(consumer, pose, matrix, top[0][0], rock.height(), top[0][1],
				0.0F, 0.0F, color, 0.0F, 1.0F, 0.0F);
		addMonolithVertex(consumer, pose, matrix, top[1][0], rock.height(), top[1][1],
				1.0F, 0.0F, color, 0.0F, 1.0F, 0.0F);
		addMonolithVertex(consumer, pose, matrix, top[2][0], rock.height(), top[2][1],
				1.0F, 1.0F, color, 0.0F, 1.0F, 0.0F);
		addMonolithVertex(consumer, pose, matrix, top[3][0], rock.height(), top[3][1],
				0.0F, 1.0F, color, 0.0F, 1.0F, 0.0F);
	}

	private static void addMonolithVertex(VertexConsumer consumer, PoseStack.Pose pose, Matrix4f matrix,
			float x, float y, float z, float u, float v, int color,
			float normalX, float normalY, float normalZ) {
		consumer.addVertex(matrix, x, y, z)
				.setColor(color)
				.setUv(u, v)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(LightTexture.FULL_BRIGHT)
				.setNormal(pose, normalX, normalY, normalZ);
	}

	private static float[][] rotatedSquare(float centerX, float centerZ, float half, float cos, float sin) {
		float[][] points = new float[4][2];
		float[][] corners = {{-half, -half}, {half, -half}, {half, half}, {-half, half}};
		for (int i = 0; i < 4; i++) {
			points[i][0] = centerX + corners[i][0] * cos - corners[i][1] * sin;
			points[i][1] = centerZ + corners[i][0] * sin + corners[i][1] * cos;
		}
		return points;
	}

	private static void emitBox(BufferBuilder buffer, float x0, float y0, float z0, float x1, float y1, float z1,
			int topRed, int topGreen, int topBlue, int topAlpha,
			int sideRed, int sideGreen, int sideBlue, int sideAlpha) {
		Matrix4f identity = new Matrix4f();
		quad(buffer, identity, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1,
				topRed, topGreen, topBlue, topAlpha);
		quad(buffer, identity, x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0,
				sideRed, sideGreen, sideBlue, sideAlpha);
		quad(buffer, identity, x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0,
				sideRed, sideGreen, sideBlue, sideAlpha);
		quad(buffer, identity, x1, y0, z1, x0, y0, z1, x0, y1, z1, x1, y1, z1,
				sideRed, sideGreen, sideBlue, sideAlpha);
		quad(buffer, identity, x0, y0, z1, x0, y0, z0, x0, y1, z0, x0, y1, z1,
				sideRed, sideGreen, sideBlue, sideAlpha);
	}

	private static void emitRibbon(BufferBuilder buffer, Matrix4f matrix, float x0, float z0, float x1, float z1,
			float width, float y, int red, int green, int blue, int alpha) {
		float dx = x1 - x0;
		float dz = z1 - z0;
		float length = Mth.sqrt(dx * dx + dz * dz);
		if (length < 0.0001F) return;
		float sideX = -dz / length * width;
		float sideZ = dx / length * width;
		quad(buffer, matrix, x0 - sideX, y, z0 - sideZ, x0 + sideX, y, z0 + sideZ,
				x1 + sideX, y, z1 + sideZ, x1 - sideX, y, z1 - sideZ, red, green, blue, alpha);
	}

	private static void quad(BufferBuilder buffer, Matrix4f matrix,
			float x0, float y0, float z0, float x1, float y1, float z1,
			float x2, float y2, float z2, float x3, float y3, float z3,
			int red, int green, int blue, int alpha) {
		buffer.addVertex(matrix, x0, y0, z0).setColor(red, green, blue, alpha);
		buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha);
		buffer.addVertex(matrix, x2, y2, z2).setColor(red, green, blue, alpha);
		buffer.addVertex(matrix, x3, y3, z3).setColor(red, green, blue, alpha);
	}

	private static void translateToArena(PoseStack poseStack, Camera camera, BlockPos center) {
		Vec3 cameraPosition = camera.getPosition();
		poseStack.pushPose();
		poseStack.translate(center.getX() - cameraPosition.x, center.getY() + 1.0D - cameraPosition.y,
				center.getZ() - cameraPosition.z);
	}

}
