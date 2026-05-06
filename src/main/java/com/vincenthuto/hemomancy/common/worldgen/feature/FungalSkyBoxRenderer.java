package com.vincenthuto.hemomancy.common.worldgen.feature;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class FungalSkyBoxRenderer {

	private static final int STAR_LAYER_COUNT = 4;
	private static VertexBuffer[] starBuffers;
	private static boolean starsCreated = false;
	private static VertexBuffer skyBuffer;
	private static VertexBuffer darkBuffer;

	private static final ResourceLocation EARTH_LOCATION = Hemomancy.rloc("textures/environment/earth.png");
	private static final ResourceLocation MOON_LOCATION = Hemomancy.rloc("textures/environment/moon.png");
	private static final ResourceLocation END_SKY_LOCATION = Hemomancy.rloc("textures/environment/blood_fill_tiled.png");

	public static double heartbeatEffect(float f, double frequency, double amplitude) {
		return amplitude * Math.sin(frequency * f);
	}

	public static double oscillatingValueWithHeartbeat(float f) {
		double baseValue = 180 - Math.abs(f - 180);
		return baseValue + heartbeatEffect(f, 0, 0);
	}

	public static boolean renderSky(ClientLevel level, float partialTicks, Matrix4f modelViewMatrix, Camera camera,
			Matrix4f projectionMatrix, Runnable setupFog) {
		PoseStack stack = new PoseStack();
		stack.mulPose(modelViewMatrix);

		float rainFade = 1.0F - level.getRainLevel(partialTicks);
		Tesselator tesselator = Tesselator.getInstance();
		FogRenderer.levelFogColor();

		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
				GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

		RenderSystem.disableDepthTest();
		renderHorizonDarkness(level, partialTicks, stack, camera, projectionMatrix);
		renderLightSkyDisc(stack, projectionMatrix);
		renderSunrise(level, partialTicks, stack, tesselator);
		renderSporeSkybox(stack, tesselator);

		renderEarthAndMoon(level, partialTicks, stack, rainFade, tesselator);
		renderStars(level, partialTicks, stack, projectionMatrix);

		setupFog.run();
		FogRenderer.levelFogColor();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
		return true;
	}

	private static void renderEarthAndMoon(ClientLevel level, float partialTicks, PoseStack stack, float rainFade,
			Tesselator tesselator) {
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.disableCull();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
				GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, rainFade);

		stack.pushPose();
		stack.mulPose(Axis.YN.rotationDegrees((level.getGameTime() + partialTicks) * 0.03F));
		Matrix4f matrix = stack.last().pose();

		RenderSystem.setShaderTexture(0, EARTH_LOCATION);
		renderTexturedQuad(tesselator, matrix, 30.0F, 0.0F, 0.0F, 0.0F, 0.5F, 0.25F, 1.0F);

		RenderSystem.setShaderTexture(0, MOON_LOCATION);
		renderTexturedQuad(tesselator, matrix, 5.0F, -34.0F, -34.0F, 0.0F, 0.5F, 0.25F, 1.0F);

		stack.popPose();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
				GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		RenderSystem.enableCull();
	}

	private static void renderTexturedQuad(Tesselator tesselator, Matrix4f matrix, float size, float xOffset,
			float zOffset, float minU, float minV, float maxU, float maxV) {
		BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		buffer.addVertex(matrix, -size + xOffset, 100.0F, -size + zOffset).setUv(minU, minV);
		buffer.addVertex(matrix, size + xOffset, 100.0F, -size + zOffset).setUv(maxU, minV);
		buffer.addVertex(matrix, size + xOffset, 100.0F, size + zOffset).setUv(maxU, maxV);
		buffer.addVertex(matrix, -size + xOffset, 100.0F, size + zOffset).setUv(minU, maxV);
		BufferUploader.drawWithShader(buffer.buildOrThrow());
	}

	private static void renderStars(ClientLevel level, float partialTicks, PoseStack stack, Matrix4f projectionMatrix) {
		RenderSystem.depthMask(false);
		RenderSystem.enableDepthTest();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
				GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

		createStars();

		float[] layerSpeeds = { 1.0F, 0.6F, 0.35F, 0.15F };
		float[] layerTilts = { 0.0F, 15.0F, -25.0F, 40.0F };
		float[] layerAxisTilts = { 0.0F, 10.0F, -20.0F, 30.0F };

		for (int layer = 0; layer < STAR_LAYER_COUNT; layer++) {
			stack.pushPose();
			stack.mulPose(Axis.XP.rotationDegrees(layerTilts[layer]));
			stack.mulPose(Axis.ZP.rotationDegrees(layerAxisTilts[layer]));
			stack.mulPose(Axis.YP.rotationDegrees((level.getGameTime() + partialTicks) * layerSpeeds[layer]));

			RenderSystem.setShaderColor(1.0F, 1.0F, 0.0F, 1.0F);
			FogRenderer.setupNoFog();
			starBuffers[layer].bind();
			starBuffers[layer].drawWithShader(stack.last().pose(), projectionMatrix, GameRenderer.getPositionShader());
			VertexBuffer.unbind();
			stack.popPose();
		}
	}

	private static void renderHorizonDarkness(ClientLevel level, float partialTicks, PoseStack stack, Camera camera,
			Matrix4f projectionMatrix) {
		RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
		double distanceBelowHorizon = camera.getEntity().getEyePosition(partialTicks).y()
				- level.getLevelData().getHorizonHeight(level);
		if (distanceBelowHorizon < 0.0D) {
			createDarkSky();
			stack.pushPose();
			stack.translate(0.0F, 12.0F, 0.0F);
			darkBuffer.bind();
			darkBuffer.drawWithShader(stack.last().pose(), projectionMatrix, GameRenderer.getPositionShader());
			VertexBuffer.unbind();
			stack.popPose();
		}
	}

	private static void renderLightSkyDisc(PoseStack stack, Matrix4f projectionMatrix) {
		createLightSky();
		skyBuffer.bind();
		skyBuffer.drawWithShader(stack.last().pose(), projectionMatrix, GameRenderer.getPositionShader());
		VertexBuffer.unbind();
	}

	private static void renderSunrise(ClientLevel level, float partialTicks, PoseStack stack, Tesselator tesselator) {
		float[] sunriseColor = level.effects().getSunriseColor(level.getTimeOfDay(partialTicks), partialTicks);
		if (sunriseColor == null) {
			return;
		}

		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		stack.pushPose();
		stack.mulPose(Axis.XP.rotationDegrees(90.0F));
		float sunSide = Mth.sin(level.getSunAngle(partialTicks)) < 0.0F ? 180.0F : 0.0F;
		stack.mulPose(Axis.ZP.rotationDegrees(sunSide));
		stack.mulPose(Axis.ZP.rotationDegrees(90.0F));

		Matrix4f matrix = stack.last().pose();
		BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
		buffer.addVertex(matrix, 0.0F, 100.0F, 0.0F)
				.setColor(sunriseColor[0], sunriseColor[1], sunriseColor[2], sunriseColor[3]);

		for (int j = 0; j <= 16; j++) {
			float angle = (float) j * ((float) Math.PI * 2F) / 16.0F;
			float sin = Mth.sin(angle);
			float cos = Mth.cos(angle);
			buffer.addVertex(matrix, sin * 120.0F, cos * 120.0F, -cos * 40.0F * sunriseColor[3])
					.setColor(sunriseColor[0], sunriseColor[1], sunriseColor[2], 0.0F);
		}

		BufferUploader.drawWithShader(buffer.buildOrThrow());
		stack.popPose();
	}

	private static void renderSporeSkybox(PoseStack stack, Tesselator tesselator) {
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
				GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.setShaderTexture(0, END_SKY_LOCATION);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		for (int i = 0; i < 6; i++) {
			stack.pushPose();
			if (i == 1) {
				stack.mulPose(Axis.XP.rotationDegrees(90.0F));
			}
			if (i == 2) {
				stack.mulPose(Axis.XP.rotationDegrees(-90.0F));
			}
			if (i == 3) {
				stack.mulPose(Axis.XP.rotationDegrees(180.0F));
			}
			if (i == 4) {
				stack.mulPose(Axis.ZP.rotationDegrees(90.0F));
			}
			if (i == 5) {
				stack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
			}

			Matrix4f matrix = stack.last().pose();
			BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
			buffer.addVertex(matrix, -100.0F, -100.0F, -100.0F).setUv(0.0F, 0.0F).setColor(24, 24, 24, 255);
			buffer.addVertex(matrix, -100.0F, -100.0F, 100.0F).setUv(0.0F, 16.0F).setColor(24, 24, 24, 255);
			buffer.addVertex(matrix, 100.0F, -100.0F, 100.0F).setUv(16.0F, 16.0F).setColor(24, 24, 24, 255);
			buffer.addVertex(matrix, 100.0F, -100.0F, -100.0F).setUv(16.0F, 0.0F).setColor(24, 24, 24, 255);
			BufferUploader.drawWithShader(buffer.buildOrThrow());
			stack.popPose();
		}
	}

	private static void createLightSky() {
		if (skyBuffer != null) {
			skyBuffer.close();
		}

		skyBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
		skyBuffer.bind();
		skyBuffer.upload(buildSkyDisc(Tesselator.getInstance(), 16.0F));
		VertexBuffer.unbind();
	}

	private static void createDarkSky() {
		if (darkBuffer != null) {
			darkBuffer.close();
		}

		darkBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
		darkBuffer.bind();
		darkBuffer.upload(buildSkyDisc(Tesselator.getInstance(), -16.0F));
		VertexBuffer.unbind();
	}

	private static MeshData buildSkyDisc(Tesselator tesselator, float y) {
		float radiusX = Math.signum(y) * 512.0F;
		BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
		buffer.addVertex(0.0F, y, 0.0F);

		for (int i = -180; i <= 180; i += 45) {
			buffer.addVertex(radiusX * Mth.cos((float) i * ((float) Math.PI / 180F)), y,
					512.0F * Mth.sin((float) i * ((float) Math.PI / 180F)));
		}

		return buffer.buildOrThrow();
	}

	private static void createStars() {
		if (starsCreated && starBuffers != null) {
			return;
		}

		if (starBuffers != null) {
			for (VertexBuffer buffer : starBuffers) {
				if (buffer != null) {
					buffer.close();
				}
			}
		}

		starBuffers = new VertexBuffer[STAR_LAYER_COUNT];
		long[] seeds = { 10842L, 27519L, 54637L, 83291L };
		int starsPerLayer = 3000 / STAR_LAYER_COUNT;

		for (int layer = 0; layer < STAR_LAYER_COUNT; layer++) {
			starBuffers[layer] = new VertexBuffer(VertexBuffer.Usage.STATIC);
			starBuffers[layer].bind();
			starBuffers[layer].upload(drawStars(Tesselator.getInstance(), starsPerLayer, seeds[layer]));
			VertexBuffer.unbind();
		}
		starsCreated = true;
	}

	private static MeshData drawStars(Tesselator tesselator, int starCount, long seed) {
		RandomSource random = RandomSource.create(seed);
		BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

		for (int i = 0; i < starCount; i++) {
			double spreadX = random.nextFloat() * 2.0F - 1F;
			double spreadY = random.nextFloat() * 2.0F - 1F;
			double spreadZ = random.nextFloat() * 2.0F - 1F;
			double vertexEdgeLength = 0.15F + random.nextFloat() * 0.1F;
			double pythagorean = spreadX * spreadX + spreadY * spreadY + spreadZ * spreadZ;
			if (pythagorean < 1.0D && pythagorean > 0.31D) {
				pythagorean = 1.0D / Math.sqrt(pythagorean);
				spreadX *= pythagorean;
				spreadY *= pythagorean;
				spreadZ *= pythagorean;
				double xDensity = spreadX * 100.0D;
				double yDensity = spreadY * 100.0D;
				double zDensity = spreadZ * 100.0D;
				double d8 = Math.atan2(spreadX, spreadZ);
				double d9 = Math.sin(d8);
				double d10 = Math.cos(d8);
				double d11 = Math.atan2(Math.sqrt(spreadX * spreadX + spreadZ * spreadZ), spreadY);
				double d12 = Math.sin(d11);
				double d13 = Math.cos(d11);
				double d14 = random.nextDouble() * Math.PI * 2.0D;
				double d15 = Math.sin(d14);
				double d16 = Math.cos(d14);

				for (int j = 0; j < 4; j++) {
					double d18 = ((j & 2) - 1) * vertexEdgeLength;
					double d19 = ((j + 1 & 2) - 1) * vertexEdgeLength;
					double d21 = d18 * d16 - d19 * d15;
					double d22 = d19 * d16 + d18 * d15;
					double d23 = d21 * d12;
					double d24 = -d21 * d13;
					double d25 = d24 * d9 - d22 * d10;
					double d26 = d22 * d9 + d24 * d10;

					buffer.addVertex((float) (xDensity + d25), (float) (yDensity + d23), (float) (zDensity + d26));
				}
			}
		}

		return buffer.buildOrThrow();
	}
}
