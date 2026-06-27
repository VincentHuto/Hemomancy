package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.Random;

final class MnemonicLowtideChamberEffects extends AbstractChamberThemeEffects {
	MnemonicLowtideChamberEffects(ChamberSkyTheme theme) {
		super(theme);
	}

	@Override
	protected void renderBaseSkybox(ChamberThemeRenderContext context) {
		renderMnemonicLowtideBaseSkybox(context.poseStack(), context.tesselator(), context.skyDistance(),
				context.theme());
	}

	@Override
	protected void renderAfterNebula(ChamberThemeRenderContext context) {
		PoseStack poseStack = context.poseStack();
		Tesselator tesselator = context.tesselator();
		float time = context.time();
		float skyDistance = context.skyDistance();
		ChamberSkyTheme theme = context.theme();
		renderMnemonicLowtideDepthEffects(poseStack, tesselator, time, skyDistance, theme);
	}

	@Override
	protected void renderAfterSharedLayers(ChamberThemeRenderContext context) {
		PoseStack poseStack = context.poseStack();
		Tesselator tesselator = context.tesselator();
		float time = context.time();
		float skyDistance = context.skyDistance();
		ChamberSkyTheme theme = context.theme();
		renderMnemonicLowtideForegroundEffects(poseStack, tesselator, time, skyDistance, theme);
	}

	@Override
	public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
		float fogBrightness = Mth.clamp(brightness * 0.36F + 0.64F, 0.50F, 1.0F);
		return new Vec3(0.38D, 0.21D, 0.14D).scale(fogBrightness);
	}

	static void renderMnemonicLowtideBaseSkybox(PoseStack poseStack, Tesselator tesselator, float skyDistance,
			ChamberSkyTheme theme) {
		if (!theme.renderBaseSkybox()) {
			return;
		}

		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		for (int face = 0; face < 6; face++) {
			RenderSystem.setShaderTexture(0,
					face == 0 ? ChamberSkyThemeRegistry.MNEMONIC_LOWTIDE_FLUID : theme.skyTexture());
			poseStack.pushPose();
			ChamberOfWillRenderHelpers.rotateSkyFace(poseStack, face);
			Matrix4f matrix = poseStack.last().pose();
			BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
			int color = face == 0 ? 0xFFD8C1A4 : theme.skyboxColor();
			buffer.addVertex(matrix, -skyDistance, -skyDistance, -skyDistance).setUv(0.0F, 0.0F).setColor(color);
			buffer.addVertex(matrix, -skyDistance, -skyDistance, skyDistance).setUv(0.0F, 1.0F).setColor(color);
			buffer.addVertex(matrix, skyDistance, -skyDistance, skyDistance).setUv(1.0F, 1.0F).setColor(color);
			buffer.addVertex(matrix, skyDistance, -skyDistance, -skyDistance).setUv(1.0F, 0.0F).setColor(color);
			BufferUploader.drawWithShader(buffer.buildOrThrow());
			poseStack.popPose();
		}
	}

	static void renderMnemonicLowtideDepthEffects(PoseStack poseStack, Tesselator tesselator, float time,
			float skyDistance, ChamberSkyTheme theme) {
		if (!ChamberOfWillManager.THEME_MNEMONIC_LOWTIDE.equals(theme.id())) {
			return;
		}

		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		RenderSystem.depthMask(false);
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);

		renderMnemonicLowtideSkullVault(poseStack, tesselator, time, skyDistance, theme);
		renderMnemonicLowtideBlackFluid(poseStack, tesselator, time, skyDistance, theme);
		renderMnemonicLowtideHorizonHaze(poseStack, tesselator, time, skyDistance);
		renderMnemonicLowtideNerveRoots(poseStack, tesselator, time, skyDistance);
		renderMnemonicLowtideOutposts(poseStack, tesselator, time, skyDistance);
		renderMnemonicLowtideArteryBridges(poseStack, tesselator, time, skyDistance);
		renderMnemonicLowtideMemoryPearls(poseStack, tesselator, time, skyDistance);

		RenderSystem.depthMask(true);
		RenderSystem.enableCull();
	}

	static void renderMnemonicLowtideForegroundEffects(PoseStack poseStack, Tesselator tesselator, float time,
			float skyDistance, ChamberSkyTheme theme) {
		if (!ChamberOfWillManager.THEME_MNEMONIC_LOWTIDE.equals(theme.id())) {
			return;
		}

		RenderSystem.enableBlend();
		RenderSystem.disableCull();
		RenderSystem.depthMask(false);
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);

		renderMnemonicLowtideMemoryFragments(poseStack, tesselator, time, skyDistance);
		renderMnemonicLowtideSynapseFlashes(poseStack, tesselator, time, skyDistance);
		renderMnemonicLowtideRedVeinStrands(poseStack, tesselator, time, skyDistance);

		RenderSystem.depthMask(true);
		RenderSystem.enableCull();
	}

	static void renderMnemonicLowtideSkullVault(PoseStack poseStack, Tesselator tesselator, float time,
			float skyDistance, ChamberSkyTheme theme) {
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		for (int face = 1; face < 6; face++) {
			poseStack.pushPose();
			ChamberOfWillRenderHelpers.rotateSkyFace(poseStack, face);
			Matrix4f matrix = poseStack.last().pose();
			BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
			Random random = new Random(0x51A7B00DL + face * 19397L);
			float depth = -skyDistance * 0.948F;
			int bands = 6;

			// Primary bands: dark maroon at bandT=0, pale bone at bandT=1
			for (int band = 0; band < bands; band++) {
				float bandT = band / (float) Math.max(1, bands - 1);
				float centerZ = Mth.lerp(bandT, -0.72F, 0.70F) * skyDistance;
				float arc     = Mth.lerp(random.nextFloat(), 0.22F, 0.42F) * skyDistance;
				float phase   = face * 0.63F + band * 1.19F;
				float drift   = Mth.sin(time * 0.00042F + phase) * skyDistance * 0.018F;
				float startX  = -skyDistance * Mth.lerp(random.nextFloat(), 0.72F, 0.97F);
				float endX    =  skyDistance * Mth.lerp(random.nextFloat(), 0.72F, 0.97F);
				float prevX   = startX;
				float prevZ   = centerZ + drift + Mth.sin(phase) * skyDistance * 0.045F;

				// Gradient: horizon bands deep maroon, zenith bands pale bone
				int red   = (int) Mth.lerp(bandT, 75.0F,  195.0F);
				int green = (int) Mth.lerp(bandT, 18.0F,  172.0F);
				int blue  = (int) Mth.lerp(bandT, 12.0F,  140.0F);
				// Side faces recede into darkness; top face (face==1) gets full weight
				float faceMult = (face == 1) ? 1.0F : 0.38F;
				int alpha = (int) Mth.clamp(
						(Mth.lerp(bandT, 28.0F, 9.0F) + random.nextFloat() * 8.0F) * faceMult,
						6.0F, 36.0F);

				int segments = 24;
				for (int segment = 1; segment <= segments; segment++) {
					float t = segment / (float) segments;
					float x       = Mth.lerp(t, startX, endX);
					float bow     = Mth.sin(t * Mth.PI) * arc;
					float wrinkle = Mth.sin(t * Mth.TWO_PI * (1.4F + random.nextFloat() * 0.7F)
							+ phase + time * 0.0009F) * skyDistance * 0.018F;
					float z = centerZ + drift - bow + wrinkle;
					addLowtideFaceStroke(buffer, matrix, prevX, prevZ, x, z, depth,
							skyDistance * Mth.lerp(bandT, 0.0048F, 0.0018F), red, green, blue, alpha);
					prevX = x;
					prevZ = z;
				}
			}

			// Top face only: thin secondary vascular network, dark crimson
			if (face == 1) {
				Random vascRandom = new Random(0xC0A6471EL + face * 7331L);
				for (int vessel = 0; vessel < 12; vessel++) {
					float vPhase  = vessel * 0.52F;
					float vStartX = Mth.lerp(vascRandom.nextFloat(), -0.88F, 0.88F) * skyDistance;
					float vStartZ = Mth.lerp(vascRandom.nextFloat(), -0.68F, 0.68F) * skyDistance;
					float vLen    = Mth.lerp(vascRandom.nextFloat(), 0.18F, 0.46F) * skyDistance;
					float vAngle  = vascRandom.nextFloat() * Mth.TWO_PI;
					float prevX = vStartX;
					float prevZ = vStartZ;
					int vSegments = 4 + vascRandom.nextInt(4);
					for (int vs = 1; vs <= vSegments; vs++) {
						float vt   = vs / (float) vSegments;
						float bend = Mth.sin(vt * Mth.PI + vPhase + time * 0.0007F) * skyDistance * 0.055F;
						float nx = vStartX + Mth.cos(vAngle) * vLen * vt + Mth.cos(vAngle + Mth.HALF_PI) * bend;
						float nz = vStartZ + Mth.sin(vAngle) * vLen * vt + Mth.sin(vAngle + Mth.HALF_PI) * bend;
						int va = (int) Mth.clamp(28.0F * (1.0F - vt * 0.55F), 6.0F, 28.0F);
						addLowtideFaceStroke(buffer, matrix, prevX, prevZ, nx, nz, depth - 0.005F,
								skyDistance * 0.0015F, 90, 22, 16, va);
						prevX = nx;
						prevZ = nz;
					}
				}
			}

			BufferUploader.drawWithShader(buffer.buildOrThrow());
			poseStack.popPose();
		}
	}

	static void renderMnemonicLowtideBlackFluid(PoseStack poseStack, Tesselator tesselator, float time,
			float skyDistance, ChamberSkyTheme theme) {
		renderMnemonicLowtideFluidSurface(poseStack, time, skyDistance);

		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		Matrix4f matrix = poseStack.last().pose();
		BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		float y = -skyDistance * 0.64F;
		float half = skyDistance * 1.42F;

		for (int ring = 0; ring < 7; ring++) {
			float t = ring / 6.0F;
			float bandHalf = skyDistance * Mth.lerp(t, 0.30F, 1.30F);
			float z = Mth.lerp(t, -0.52F, 0.74F) * skyDistance
					+ Mth.sin(time * 0.00058F + ring * 0.71F) * skyDistance * 0.018F;
			int alpha = (int) Mth.clamp(Mth.lerp(t, 60.0F, 22.0F), 12.0F, 68.0F);
			addLowtideGroundQuad(buffer, matrix, -bandHalf, z - skyDistance * 0.012F, bandHalf,
					z + skyDistance * 0.012F, y - ring * 0.018F, 38, 10, 8, alpha);
		}

		for (int sigil = 0; sigil < 9; sigil++) {
			float angle = sigil / 9.0F * Mth.TWO_PI + Mth.sin(time * 0.00031F + sigil) * 0.035F;
			float distance = skyDistance * Mth.lerp((sigil % 4) / 3.0F, 0.28F, 0.96F);
			float centerX = Mth.cos(angle) * distance;
			float centerZ = Mth.sin(angle) * distance;
			float radius = skyDistance * (0.018F + (sigil % 3) * 0.006F);
			addLowtideGroundQuad(buffer, matrix, centerX - radius, centerZ - radius * 0.18F,
					centerX + radius, centerZ + radius * 0.18F, y - 0.05F, 90, 55, 20,
					12 + sigil % 3 * 6);
		}
		BufferUploader.drawWithShader(buffer.buildOrThrow());
	}

	static void renderMnemonicLowtideFluidSurface(PoseStack poseStack, float time, float skyDistance) {
		MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
		RenderType renderType = HemoRenderTypes.mnemonicLowtideFluid(
				ChamberSkyThemeRegistry.MNEMONIC_LOWTIDE_FLUID, time * 0.05F, 0.417F, 0.88F, 0.74F);
		VertexConsumer consumer = bufferSource.getBuffer(renderType);
		Matrix4f matrix = poseStack.last().pose();
		float y = -skyDistance * 0.642F;
		float half = skyDistance * 1.46F;
		float uvScale = 3.8F;
		int grid = 36;
		float cellSize = half * 2.0F / grid;
		float uvCell = uvScale / grid;
		for (int cellZ = 0; cellZ < grid; cellZ++) {
			for (int cellX = 0; cellX < grid; cellX++) {
				float minX = -half + cellX * cellSize;
				float maxX = minX + cellSize;
				float minZ = -half + cellZ * cellSize;
				float maxZ = minZ + cellSize;
				float minU = cellX * uvCell;
				float maxU = minU + uvCell;
				float minV = cellZ * uvCell;
				float maxV = minV + uvCell;
				addLowtideFluidVertex(consumer, matrix, minX, y, minZ, minU, maxV, 12, 4, 6, 218);
				addLowtideFluidVertex(consumer, matrix, minX, y, maxZ, minU, minV, 12, 4, 6, 218);
				addLowtideFluidVertex(consumer, matrix, maxX, y, maxZ, maxU, minV, 12, 4, 6, 218);
				addLowtideFluidVertex(consumer, matrix, maxX, y, minZ, maxU, maxV, 12, 4, 6, 218);
			}
		}
		bufferSource.endBatch(renderType);
	}

	static void renderMnemonicLowtideHorizonHaze(PoseStack poseStack, Tesselator tesselator, float time,
			float skyDistance) {
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		Matrix4f matrix = poseStack.last().pose();
		BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		float half = skyDistance * 1.55F;

		// 5 stacked haze quads rising from the fluid surface to hide the hard horizon seam
		for (int layer = 0; layer < 5; layer++) {
			float t = layer / 4.0F;
			float yBottom = -skyDistance * Mth.lerp(t, 0.660F, 0.510F);
			float yTop    = -skyDistance * Mth.lerp(t, 0.630F, 0.480F);
			int r = (int) Mth.lerp(t, 55.0F, 18.0F);
			int g = (int) Mth.lerp(t, 14.0F,  4.0F);
			int b = (int) Mth.lerp(t, 10.0F,  3.0F);
			int alpha = (int) Mth.lerp(t, 22.0F, 8.0F);
			addLowtideGroundQuad(buffer, matrix, -half, yBottom, half, yTop, 0.0F, r, g, b, alpha);
		}

		// Fog floor: wide shallow quad just above the fluid plane to blur the transition
		float fogFloorY = -skyDistance * 0.630F;
		addLowtideGroundQuad(buffer, matrix, -half, -half, half, half, fogFloorY, 28, 7, 5, 18);

		// Gentle lateral haze on inner side walls to soften the box corners
		for (int side = 0; side < 4; side++) {
			float angle  = side * Mth.HALF_PI;
			float cosA   = Mth.cos(angle);
			float sinA   = Mth.sin(angle);
			float wallDist = skyDistance * 0.96F;
			float wallX0 = cosA * wallDist - sinA * half;
			float wallZ0 = sinA * wallDist + cosA * half;
			float wallX1 = cosA * wallDist + sinA * half;
			float wallZ1 = sinA * wallDist - cosA * half;
			float topY   = -skyDistance * 0.30F;
			float botY   = -skyDistance * 0.68F;
			buffer.addVertex(matrix, wallX0, botY, wallZ0).setColor(22, 5, 4, 20);
			buffer.addVertex(matrix, wallX0, topY, wallZ0).setColor(22, 5, 4, 0);
			buffer.addVertex(matrix, wallX1, topY, wallZ1).setColor(22, 5, 4, 0);
			buffer.addVertex(matrix, wallX1, botY, wallZ1).setColor(22, 5, 4, 20);
		}

		BufferUploader.drawWithShader(buffer.buildOrThrow());
	}

	static void renderMnemonicLowtideMemoryFragments(PoseStack poseStack, Tesselator tesselator, float time,
			float skyDistance) {
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		Matrix4f matrix = poseStack.last().pose();
		BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		Random random = new Random(0x4D454D4FL);
		for (int fragment = 0; fragment < 26; fragment++) {
			float yaw = Mth.wrapDegrees(random.nextFloat() * 360.0F
					+ Mth.sin(time * (0.00045F + fragment * 0.000012F) + fragment) * 12.0F);
			float pitch = Mth.lerp(random.nextFloat(), -20.0F, 42.0F)
					+ Mth.sin(time * 0.00073F + fragment * 0.53F) * 5.0F;
			float distance = skyDistance * Mth.lerp(random.nextFloat(), 0.34F, 0.83F);
			Vec3 center = lowtideSkyPoint(yaw, pitch, distance);
			Vec3 forward = center.normalize();
			Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
			if (right.lengthSqr() < 0.0001D) {
				right = new Vec3(1.0D, 0.0D, 0.0D);
			} else {
				right = right.normalize();
			}
			Vec3 up = right.cross(forward).normalize();
			float spin = time * (0.0012F + fragment * 0.000021F) + fragment * 0.71F;
			Vec3 spunRight = right.scale(Mth.cos(spin)).add(up.scale(Mth.sin(spin)));
			Vec3 spunUp = up.scale(Mth.cos(spin)).subtract(right.scale(Mth.sin(spin)));
			float halfWidth = skyDistance * Mth.lerp(random.nextFloat(), 0.010F, 0.022F);
			float halfHeight = halfWidth * Mth.lerp(random.nextFloat(), 1.35F, 2.25F);
			int alpha = (int) Mth.clamp(Mth.lerp(distance / skyDistance, 124.0F, 70.0F), 54.0F, 138.0F);
			int red = fragment % 5 == 0 ? 232 : 197;
			int green = fragment % 5 == 0 ? 215 : 163;
			int blue = fragment % 5 == 0 ? 167 : 111;
			addLowtideBillboardQuad(buffer, matrix, center, spunRight, spunUp, halfWidth, halfHeight,
					red, green, blue, alpha);
		}
		BufferUploader.drawWithShader(buffer.buildOrThrow());
	}

	static void renderMnemonicLowtideNerveRoots(PoseStack poseStack, Tesselator tesselator, float time,
			float skyDistance) {
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		Matrix4f matrix = poseStack.last().pose();
		BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
		Random random = new Random(0xA11E57A1L);
		for (int root = 0; root < 34; root++) {
			float angle = random.nextFloat() * Mth.TWO_PI;
			float distance = skyDistance * Mth.lerp(random.nextFloat(), 0.30F, 1.03F);
			float x = Mth.cos(angle) * distance;
			float z = Mth.sin(angle) * distance;
			float topY = skyDistance * Mth.lerp(random.nextFloat(), 0.40F, 0.86F);
			float length = skyDistance * Mth.lerp(random.nextFloat(), 0.16F, 0.48F);
			float prevX = x;
			float prevY = topY;
			float prevZ = z;
			float phase = root * 0.77F;
			int segments = 5 + random.nextInt(5);
			for (int segment = 1; segment <= segments; segment++) {
				float t = segment / (float) segments;
				float sway = Mth.sin(t * Mth.PI * 2.0F + phase + time * 0.0011F) * skyDistance * 0.010F;
				float curl = Mth.cos(t * Mth.PI * 1.7F + phase * 0.6F + time * 0.0008F) * skyDistance * 0.008F;
				float nextX = x + Mth.cos(angle + Mth.HALF_PI) * sway + Mth.cos(angle) * curl;
				float nextY = topY - length * t;
				float nextZ = z + Mth.sin(angle + Mth.HALF_PI) * sway + Mth.sin(angle) * curl;
				float width = skyDistance * Mth.lerp(t, 0.0045F, 0.0014F);
				int alpha = (int) Mth.clamp(Mth.lerp(t, 100.0F, 42.0F), 26.0F, 108.0F);
				addLowtide3DStroke(buffer, matrix, prevX, prevY, prevZ, nextX, nextY, nextZ, width,
						225, 210, 174, alpha);
				prevX = nextX;
				prevY = nextY;
				prevZ = nextZ;
			}
		}
		BufferUploader.drawWithShader(buffer.buildOrThrow());
	}

	static void renderMnemonicLowtideOutposts(PoseStack poseStack, Tesselator tesselator, float time,
			float skyDistance) {
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		Matrix4f matrix = poseStack.last().pose();
		BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		Random random = new Random(0x0F710571L);
		float baseY = -skyDistance * 0.58F;
		for (int outpost = 0; outpost < 13; outpost++) {
			float angle = outpost / 13.0F * Mth.TWO_PI + Mth.lerp(random.nextFloat(), -0.14F, 0.14F);
			float distance = skyDistance * Mth.lerp(random.nextFloat(), 0.55F, 1.10F);
			Vec3 center = new Vec3(Mth.cos(angle) * distance, baseY,
					Mth.sin(angle) * distance);
			Vec3 right = new Vec3(-Mth.sin(angle), 0.0D, Mth.cos(angle)).normalize();
			Vec3 up = new Vec3(0.0D, 1.0D, 0.0D);
			float scale = skyDistance * Mth.lerp(random.nextFloat(), 0.018F, 0.045F);
			int alpha = (int) Mth.clamp(Mth.lerp(distance / skyDistance, 84.0F, 42.0F), 30.0F, 92.0F);
			addLowtideBillboardQuad(buffer, matrix, center.add(up.scale(scale * 0.74F)), right, up,
					scale * 0.52F, scale * 0.74F, 13, 10, 10, alpha);
			addLowtideBillboardQuad(buffer, matrix, center.add(right.scale(scale * 0.52F)).add(up.scale(scale * 1.18F)),
					right, up, scale * 0.18F, scale * 1.12F, 16, 12, 10, alpha);
			addLowtideBillboardQuad(buffer, matrix, center.subtract(right.scale(scale * 0.46F)).add(up.scale(scale * 0.98F)),
					right, up, scale * 0.14F, scale * 0.88F, 16, 12, 10, alpha - 8);
		}
		BufferUploader.drawWithShader(buffer.buildOrThrow());
	}

	static void renderMnemonicLowtideArteryBridges(PoseStack poseStack, Tesselator tesselator, float time,
			float skyDistance) {
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		Matrix4f matrix = poseStack.last().pose();
		BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
		for (int bridge = 0; bridge < 5; bridge++) {
			float angle = bridge / 5.0F * Mth.TWO_PI + 0.32F;
			float startDistance = skyDistance * (0.54F + bridge * 0.030F);
			float endDistance = skyDistance * (1.06F + bridge * 0.020F);
			float baseY = skyDistance * (0.070F + bridge * 0.032F);
			float bridgeHalfWidth = skyDistance * (0.0105F + bridge * 0.0008F);
			float bridgeDepth = skyDistance * (0.0140F + bridge * 0.0010F);
			int segments = 18;
			Vec3 previous = new Vec3(Mth.cos(angle - 0.36F) * startDistance,
					baseY + Mth.sin(time * 0.0008F + bridge) * skyDistance * 0.004F,
					Mth.sin(angle - 0.36F) * startDistance);
			for (int segment = 1; segment <= segments; segment++) {
				float t = segment / (float) segments;
				float segmentAngle = angle + Mth.lerp(t, -0.36F, 0.42F);
				float distance = Mth.lerp(t, startDistance, endDistance);
				float archRise = Mth.sin(t * Mth.PI) * skyDistance * (0.125F + bridge * 0.006F);
				float drift = Mth.sin(time * 0.0008F + bridge + t * 2.4F) * skyDistance * 0.005F;
				Vec3 next = new Vec3(Mth.cos(segmentAngle) * distance, baseY + archRise + drift,
						Mth.sin(segmentAngle) * distance);
				int alpha = (int) Mth.clamp(Mth.lerp(t, 110.0F, 24.0F), 22.0F, 118.0F);
				renderMnemonicLowtideBridgeArchSegment(buffer, matrix, previous, next, bridgeHalfWidth, bridgeDepth,
						182, 46, 28, alpha);

				Vec3 side = lowtideBridgeSide(previous, next);
				Vec3 railLift = new Vec3(0.0D, skyDistance * 0.010F, 0.0D);
				Vec3 leftStart = previous.add(side.scale(bridgeHalfWidth)).add(railLift);
				Vec3 leftEnd = next.add(side.scale(bridgeHalfWidth)).add(railLift);
				Vec3 rightStart = previous.subtract(side.scale(bridgeHalfWidth)).add(railLift);
				Vec3 rightEnd = next.subtract(side.scale(bridgeHalfWidth)).add(railLift);
				addLowtide3DStroke(buffer, matrix, (float) leftStart.x, (float) leftStart.y, (float) leftStart.z,
						(float) leftEnd.x, (float) leftEnd.y, (float) leftEnd.z, skyDistance * 0.0018F,
						228, 160, 66, alpha / 2);
				addLowtide3DStroke(buffer, matrix, (float) rightStart.x, (float) rightStart.y, (float) rightStart.z,
						(float) rightEnd.x, (float) rightEnd.y, (float) rightEnd.z, skyDistance * 0.0014F,
						130, 38, 28, alpha / 2);

				if (segment % 3 == 0 && segment < segments - 1) {
					addLowtideBridgeRib(buffer, matrix, next, side, bridgeHalfWidth, bridgeDepth,
							skyDistance, alpha);
				}
				previous = next;
			}
		}
		BufferUploader.drawWithShader(buffer.buildOrThrow());
	}

	static void renderMnemonicLowtideMemoryPearls(PoseStack poseStack, Tesselator tesselator, float time,
			float skyDistance) {
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		Matrix4f matrix = poseStack.last().pose();
		BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
		Random random = new Random(0xC0A57A1DL);
		for (int pearl = 0; pearl < 10; pearl++) {
			float yaw = pearl / 10.0F * 360.0F + Mth.lerp(random.nextFloat(), -14.0F, 14.0F);
			float pitch = Mth.lerp(random.nextFloat(), -10.0F, 20.0F);
			float distance = skyDistance * Mth.lerp(random.nextFloat(), 0.62F, 0.98F);
			Vec3 center = lowtideSkyPoint(yaw, pitch, distance);
			center = new Vec3(center.x, Mth.clamp(center.y, -skyDistance * 0.30F, skyDistance * 0.26F), center.z);
			Vec3 forward = center.normalize();
			Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
			if (right.lengthSqr() < 0.0001D) {
				right = new Vec3(1.0D, 0.0D, 0.0D);
			} else {
				right = right.normalize();
			}
			Vec3 up = right.cross(forward).normalize();
			float radius = skyDistance * Mth.lerp(random.nextFloat(), 0.025F, 0.070F);
			int alpha = (int) Mth.clamp(Mth.lerp(distance / skyDistance, 66.0F, 34.0F), 26.0F, 74.0F);
			addLowtideDisc(buffer, matrix, center, right, up, radius * 1.18F, radius * 0.82F,
					139, 76, 45, alpha, 18);
			addLowtideDisc(buffer, matrix, center.add(right.scale(radius * 0.18F)).add(up.scale(radius * 0.18F)),
					right, up, radius * 0.42F, radius * 0.28F, 245, 210, 133, alpha / 2, 12);
		}
		BufferUploader.drawWithShader(buffer.buildOrThrow());
	}

	static void renderMnemonicLowtideSynapseFlashes(PoseStack poseStack, Tesselator tesselator, float time,
			float skyDistance) {
		RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		for (int face = 0; face < 6; face++) {
			poseStack.pushPose();
			ChamberOfWillRenderHelpers.rotateSkyFace(poseStack, face);
			Matrix4f matrix = poseStack.last().pose();
			BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
			Random random = new Random(0x5A9A95EEL + face * 11717L);
			float depth = -skyDistance * 0.925F;
			boolean emitted = false;
			for (int flash = 0; flash < 4; flash++) {
				float cycle = (time * 0.0022F + flash * 0.17F + face * 0.083F) % 1.0F;
				float pulse = wrappedLowtidePulse(cycle, 0.08F + random.nextFloat() * 0.22F, 0.024F);
				if (pulse <= 0.025F) {
					continue;
				}
				float x = Mth.lerp(random.nextFloat(), -0.72F, 0.72F) * skyDistance;
				float z = Mth.lerp(random.nextFloat(), -0.56F, 0.58F) * skyDistance;
				float radius = skyDistance * Mth.lerp(random.nextFloat(), 0.030F, 0.075F) * pulse;
				int alpha = (int) Mth.clamp(130.0F * pulse, 0.0F, 150.0F);
				emitted |= addLowtideFaceStroke(buffer, matrix, x - radius, z, x + radius, z, depth,
						skyDistance * 0.0013F, 255, 239, 210, alpha);
				emitted |= addLowtideFaceStroke(buffer, matrix, x, z - radius, x, z + radius, depth - 0.012F,
						skyDistance * 0.0013F, 255, 239, 210, alpha);
				addLowtideDisc(buffer, matrix, new Vec3(x, depth - 0.020F, z), new Vec3(1.0D, 0.0D, 0.0D),
						new Vec3(0.0D, 0.0D, 1.0D), radius * 0.42F, radius * 0.42F,
						255, 239, 210, alpha / 2, 10);
				emitted = true;
			}
			if (emitted) {
				BufferUploader.drawWithShader(buffer.buildOrThrow());
			}
			poseStack.popPose();
		}

		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
	}

	static void renderMnemonicLowtideRedVeinStrands(PoseStack poseStack, Tesselator tesselator, float time,
			float skyDistance) {
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		Matrix4f matrix = poseStack.last().pose();
		BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
		Random random = new Random(0x7E1E571DL);
		float fluidY = -skyDistance * 0.61F;
		boolean emitted = false;
		for (int strand = 0; strand < 18; strand++) {
			float cycle = (time * (0.00075F + strand * 0.000013F) + strand * 0.061F) % 1.0F;
			float rise = Mth.sin(cycle * Mth.PI);
			if (rise <= 0.04F) {
				continue;
			}
			float angle = random.nextFloat() * Mth.TWO_PI;
			float distance = skyDistance * Mth.lerp(random.nextFloat(), 0.18F, 0.74F);
			float baseX = Mth.cos(angle) * distance;
			float baseZ = Mth.sin(angle) * distance;
			float height = skyDistance * Mth.lerp(random.nextFloat(), 0.07F, 0.20F) * rise;
			float prevX = baseX;
			float prevY = fluidY;
			float prevZ = baseZ;
			int segments = 4 + random.nextInt(4);
			for (int segment = 1; segment <= segments; segment++) {
				float t = segment / (float) segments;
				float sway = Mth.sin(t * Mth.PI * 2.0F + strand * 0.91F + time * 0.0017F)
						* skyDistance * 0.008F * rise;
				float nextX = baseX + Mth.cos(angle + Mth.HALF_PI) * sway;
				float nextY = fluidY + height * t;
				float nextZ = baseZ + Mth.sin(angle + Mth.HALF_PI) * sway;
				int alpha = (int) Mth.clamp(118.0F * rise * (1.0F - t * 0.42F), 0.0F, 126.0F);
				emitted |= addLowtide3DStroke(buffer, matrix, prevX, prevY, prevZ, nextX, nextY, nextZ,
						skyDistance * Mth.lerp(t, 0.0032F, 0.0012F), 218, 27, 18, alpha);
				prevX = nextX;
				prevY = nextY;
				prevZ = nextZ;
			}
		}
		if (emitted) {
			BufferUploader.drawWithShader(buffer.buildOrThrow());
		}
	}

	static boolean addLowtideFaceStroke(BufferBuilder buffer, Matrix4f matrix, float startX, float startZ,
			float endX, float endZ, float depth, float width, int red, int green, int blue, int alpha) {
		float dx = endX - startX;
		float dz = endZ - startZ;
		float length = Mth.sqrt(dx * dx + dz * dz);
		if (length <= 0.0001F || alpha <= 0) {
			return false;
		}
		float nx = -dz / length * width;
		float nz = dx / length * width;
		int edgeAlpha = Math.max(0, alpha / 5);
		buffer.addVertex(matrix, startX - nx, depth, startZ - nz).setColor(red, green, blue, edgeAlpha);
		buffer.addVertex(matrix, startX + nx, depth, startZ + nz).setColor(red, green, blue, alpha);
		buffer.addVertex(matrix, endX + nx, depth, endZ + nz).setColor(red, green, blue, alpha);
		buffer.addVertex(matrix, startX - nx, depth, startZ - nz).setColor(red, green, blue, edgeAlpha);
		buffer.addVertex(matrix, endX + nx, depth, endZ + nz).setColor(red, green, blue, alpha);
		buffer.addVertex(matrix, endX - nx, depth, endZ - nz).setColor(red, green, blue, edgeAlpha);
		return true;
	}

	static boolean addLowtide3DStroke(BufferBuilder buffer, Matrix4f matrix, float startX, float startY, float startZ,
			float endX, float endY, float endZ, float width, int red, int green, int blue, int alpha) {
		float dx = endX - startX;
		float dz = endZ - startZ;
		float length = Mth.sqrt(dx * dx + dz * dz);
		if (alpha <= 0) {
			return false;
		}
		float nx;
		float nz;
		if (length <= 0.0001F) {
			nx = width;
			nz = 0.0F;
		} else {
			nx = -dz / length * width;
			nz = dx / length * width;
		}
		int edgeAlpha = Math.max(0, alpha / 5);
		buffer.addVertex(matrix, startX - nx, startY, startZ - nz).setColor(red, green, blue, edgeAlpha);
		buffer.addVertex(matrix, startX + nx, startY, startZ + nz).setColor(red, green, blue, alpha);
		buffer.addVertex(matrix, endX + nx, endY, endZ + nz).setColor(red, green, blue, alpha);
		buffer.addVertex(matrix, startX - nx, startY, startZ - nz).setColor(red, green, blue, edgeAlpha);
		buffer.addVertex(matrix, endX + nx, endY, endZ + nz).setColor(red, green, blue, alpha);
		buffer.addVertex(matrix, endX - nx, endY, endZ - nz).setColor(red, green, blue, edgeAlpha);
		return true;
	}

	static void renderMnemonicLowtideBridgeArchSegment(BufferBuilder buffer, Matrix4f matrix, Vec3 start, Vec3 end,
			float halfWidth, float depth, int red, int green, int blue, int alpha) {
		Vec3 side = lowtideBridgeSide(start, end);
		Vec3 down = new Vec3(0.0D, -depth, 0.0D);
		Vec3 startLeft = start.add(side.scale(halfWidth));
		Vec3 startRight = start.subtract(side.scale(halfWidth));
		Vec3 endLeft = end.add(side.scale(halfWidth));
		Vec3 endRight = end.subtract(side.scale(halfWidth));
		Vec3 underStartLeft = start.add(side.scale(halfWidth * 0.78F)).add(down);
		Vec3 underStartRight = start.subtract(side.scale(halfWidth * 0.78F)).add(down);
		Vec3 underEndLeft = end.add(side.scale(halfWidth * 0.78F)).add(down);
		Vec3 underEndRight = end.subtract(side.scale(halfWidth * 0.78F)).add(down);

		addLowtideBridgeDeckQuad(buffer, matrix, startLeft, endLeft, endRight, startRight,
				red, green, blue, alpha);
		addLowtideBridgeDeckQuad(buffer, matrix, underStartRight, underEndRight, underEndLeft, underStartLeft,
				Math.max(0, red - 86), Math.max(0, green - 24), Math.max(0, blue - 14), alpha * 3 / 4);
		addLowtideBridgeDeckQuad(buffer, matrix, startLeft, underStartLeft, underEndLeft, endLeft,
				Math.max(0, red - 42), Math.max(0, green - 14), Math.max(0, blue - 10), alpha * 4 / 5);
		addLowtideBridgeDeckQuad(buffer, matrix, startRight, endRight, underEndRight, underStartRight,
				Math.max(0, red - 64), Math.max(0, green - 18), Math.max(0, blue - 12), alpha * 3 / 5);

		Vec3 keelStart = start.add(down).add(new Vec3(0.0D, -depth * 0.22F, 0.0D));
		Vec3 keelEnd = end.add(down).add(new Vec3(0.0D, -depth * 0.22F, 0.0D));
		addLowtide3DStroke(buffer, matrix, (float) keelStart.x, (float) keelStart.y, (float) keelStart.z,
				(float) keelEnd.x, (float) keelEnd.y, (float) keelEnd.z, halfWidth * 0.16F,
				94, 22, 19, alpha * 2 / 3);
	}

	static void addLowtideBridgeDeckQuad(BufferBuilder buffer, Matrix4f matrix, Vec3 a, Vec3 b, Vec3 c, Vec3 d,
			int red, int green, int blue, int alpha) {
		addLowtideVertex(buffer, matrix, a, red, green, blue, alpha);
		addLowtideVertex(buffer, matrix, b, red, green, blue, alpha);
		addLowtideVertex(buffer, matrix, c, red, green, blue, alpha);
		addLowtideVertex(buffer, matrix, a, red, green, blue, alpha);
		addLowtideVertex(buffer, matrix, c, red, green, blue, alpha);
		addLowtideVertex(buffer, matrix, d, red, green, blue, alpha);
	}

	static void addLowtideBridgeRib(BufferBuilder buffer, Matrix4f matrix, Vec3 center, Vec3 side,
			float halfWidth, float depth, float skyDistance, int alpha) {
		Vec3 up = new Vec3(0.0D, 1.0D, 0.0D);
		Vec3 crown = center.add(up.scale(depth * 1.15F));
		Vec3 left = center.add(side.scale(halfWidth * 1.15F));
		Vec3 right = center.subtract(side.scale(halfWidth * 1.15F));
		Vec3 under = center.add(up.scale(-depth * 1.65F));

		addLowtide3DStroke(buffer, matrix, (float) left.x, (float) left.y, (float) left.z,
				(float) crown.x, (float) crown.y, (float) crown.z, halfWidth * 0.18F,
				226, 154, 70, alpha * 2 / 3);
		addLowtide3DStroke(buffer, matrix, (float) crown.x, (float) crown.y, (float) crown.z,
				(float) right.x, (float) right.y, (float) right.z, halfWidth * 0.18F,
				226, 154, 70, alpha * 2 / 3);
		addLowtide3DStroke(buffer, matrix, (float) crown.x, (float) crown.y, (float) crown.z,
				(float) under.x, (float) under.y, (float) under.z, halfWidth * 0.11F,
				142, 36, 26, alpha / 2);
		Vec3 dripEnd = under.add(up.scale(-depth * 1.8F));
		addLowtide3DStroke(buffer, matrix, (float) under.x, (float) under.y, (float) under.z,
				(float) dripEnd.x, (float) dripEnd.y, (float) dripEnd.z, halfWidth * 0.09F,
				101, 25, 24, alpha / 3);
	}

	static Vec3 lowtideBridgeSide(Vec3 start, Vec3 end) {
		Vec3 horizontal = new Vec3(end.x - start.x, 0.0D, end.z - start.z);
		if (horizontal.lengthSqr() < 0.0001D) {
			return new Vec3(1.0D, 0.0D, 0.0D);
		}
		horizontal = horizontal.normalize();
		return new Vec3(-horizontal.z, 0.0D, horizontal.x);
	}

	static void addLowtideGroundQuad(BufferBuilder buffer, Matrix4f matrix, float minX, float minZ,
			float maxX, float maxZ, float y, int red, int green, int blue, int alpha) {
		buffer.addVertex(matrix, minX, y, minZ).setColor(red, green, blue, alpha);
		buffer.addVertex(matrix, minX, y, maxZ).setColor(red, green, blue, alpha);
		buffer.addVertex(matrix, maxX, y, maxZ).setColor(red, green, blue, alpha);
		buffer.addVertex(matrix, maxX, y, minZ).setColor(red, green, blue, alpha);
	}

	static void addLowtideBillboardQuad(BufferBuilder buffer, Matrix4f matrix, Vec3 center, Vec3 right, Vec3 up,
			float halfWidth, float halfHeight, int red, int green, int blue, int alpha) {
		Vec3 scaledRight = right.scale(halfWidth);
		Vec3 scaledUp = up.scale(halfHeight);
		Vec3 a = center.subtract(scaledRight).subtract(scaledUp);
		Vec3 b = center.subtract(scaledRight).add(scaledUp);
		Vec3 c = center.add(scaledRight).add(scaledUp);
		Vec3 d = center.add(scaledRight).subtract(scaledUp);
		addLowtideVertex(buffer, matrix, a, red, green, blue, Math.max(0, alpha / 4));
		addLowtideVertex(buffer, matrix, b, red, green, blue, alpha);
		addLowtideVertex(buffer, matrix, c, red, green, blue, alpha);
		addLowtideVertex(buffer, matrix, d, red, green, blue, Math.max(0, alpha / 4));
	}

	static void addLowtideDisc(BufferBuilder buffer, Matrix4f matrix, Vec3 center, Vec3 right, Vec3 up,
			float radiusX, float radiusY, int red, int green, int blue, int alpha, int segments) {
		int safeSegments = Math.max(6, segments);
		for (int segment = 0; segment < safeSegments; segment++) {
			float angleA = segment / (float) safeSegments * Mth.TWO_PI;
			float angleB = (segment + 1) / (float) safeSegments * Mth.TWO_PI;
			Vec3 a = center.add(right.scale(Mth.cos(angleA) * radiusX)).add(up.scale(Mth.sin(angleA) * radiusY));
			Vec3 b = center.add(right.scale(Mth.cos(angleB) * radiusX)).add(up.scale(Mth.sin(angleB) * radiusY));
			addLowtideVertex(buffer, matrix, center, red, green, blue, alpha);
			addLowtideVertex(buffer, matrix, a, red, green, blue, Math.max(0, alpha / 5));
			addLowtideVertex(buffer, matrix, b, red, green, blue, Math.max(0, alpha / 5));
		}
	}

	static void addLowtideVertex(BufferBuilder buffer, Matrix4f matrix, Vec3 position,
			int red, int green, int blue, int alpha) {
		buffer.addVertex(matrix, (float) position.x, (float) position.y, (float) position.z)
				.setColor(red, green, blue, Mth.clamp(alpha, 0, 255));
	}

	static void addLowtideFluidVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
			float u, float v, int red, int green, int blue, int alpha) {
		consumer.addVertex(matrix, x, y, z).setUv(u, v).setColor(red, green, blue, Mth.clamp(alpha, 0, 255));
	}

	static Vec3 lowtideSkyPoint(float yaw, float pitch, float distance) {
		float yawRadians = yaw * Mth.DEG_TO_RAD;
		float pitchRadians = pitch * Mth.DEG_TO_RAD;
		float horizontal = Mth.cos(pitchRadians);
		return new Vec3(
				-Mth.sin(yawRadians) * horizontal * distance,
				-Mth.sin(pitchRadians) * distance,
				-Mth.cos(yawRadians) * horizontal * distance);
	}

	static float wrappedLowtidePulse(float t, float center, float width) {
		float distance = Mth.abs(t - center);
		distance = Math.min(distance, 1.0F - distance);
		float pulse = Mth.clamp(1.0F - distance / width, 0.0F, 1.0F);
		return pulse * pulse * (3.0F - 2.0F * pulse);
	}
}
