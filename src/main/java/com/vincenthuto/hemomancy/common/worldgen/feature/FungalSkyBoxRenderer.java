package com.vincenthuto.hemomancy.common.worldgen.feature;

import org.joml.Matrix4f;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FungalSkyBoxRenderer {

	private static VertexBuffer starBuffer;
	public static VertexBuffer skyBuffer;

	private static final ResourceLocation MOON_LOCATION = Hemomancy.rloc("textures/environment/earth.png");
	private static final ResourceLocation SUN_LOCATION = new ResourceLocation("textures/environment/sun.png");
	private static final ResourceLocation CLOUDS_LOCATION = new ResourceLocation("textures/environment/clouds.png");
	private static final ResourceLocation END_SKY_LOCATION = Hemomancy.rloc("textures/environment/blood_fill_tiled.png");

	public FungalSkyBoxRenderer() {
	}

	public static double heartbeatEffect(float f, double frequency, double amplitude) {
		// The sinusoidal heartbeat effect
		return amplitude * Math.sin(frequency * f);
	}

	public static double oscillatingValueWithHeartbeat(float f) {
		double baseValue = 180 - Math.abs(f - 180);
		// Add the heartbeat effect to the base oscillating value
		return baseValue + heartbeatEffect(f, 0, 0); // frequency and amplitude can be adjusted
	}

	public static boolean renderSky(ClientLevel level, float partialTicks, PoseStack stack, Camera camera,
			Matrix4f projectionMatrix, Runnable setupFog) {
		float f11 = 1.0F - level.getRainLevel(partialTicks);
		BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
		FogRenderer.levelFogColor();
		ShaderInstance shaderinstance = RenderSystem.getShader();
		LevelRenderer levelRenderer = Minecraft.getInstance().levelRenderer;

		
		
		
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
				GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		stack.pushPose();

	      RenderSystem.depthMask(false);
	      RenderSystem.enableBlend();
		RenderSystem.setShaderColor(0.5F, 0.0F, 0.0F, f11);
		stack.mulPose(Axis.YP.rotationDegrees(level.getGameTime() + partialTicks));

		createStars();

		RenderSystem.setShaderColor(1.0F, 1.0F, 0.0F, 1.0F);
		FogRenderer.setupNoFog();

		starBuffer.bind();
		starBuffer.drawWithShader(stack.last().pose(), projectionMatrix, GameRenderer.getPositionShader());
		stack.popPose();
		RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
		double d0 = camera.getEntity().getEyePosition(partialTicks).y(); // -
																			// from 63 to 0
		if (d0 < 0.0D) {
			stack.pushPose();
			stack.translate(0.0F, 12.0F, 0.0F);
			levelRenderer.darkBuffer.bind();
			levelRenderer.darkBuffer.drawWithShader(stack.last().pose(), projectionMatrix, shaderinstance);
			VertexBuffer.unbind();
			stack.popPose();
		}
		//Moon
		createLightSky();
		skyBuffer.bind();
		skyBuffer.drawWithShader(stack.last().pose(), projectionMatrix, shaderinstance);
		VertexBuffer.unbind();
		float[] afloat = level.effects().getSunriseColor(level.getTimeOfDay(partialTicks), partialTicks);
		if (afloat != null) {
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			stack.pushPose();
			stack.mulPose(Axis.XP.rotationDegrees(90.0F));
			float f3 = Mth.sin(level.getSunAngle(partialTicks)) < 0.0F ? 180.0F : 0.0F;
			stack.mulPose(Axis.ZP.rotationDegrees(f3));
			stack.mulPose(Axis.ZP.rotationDegrees(90.0F));
			float f4 = afloat[0];
			float f5 = afloat[1];
			float f6 = afloat[2];
			Matrix4f matrix4f = stack.last().pose();
			bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
			bufferbuilder.vertex(matrix4f, 0.0F, 100.0F, 0.0F).color(f4, f5, f6, afloat[3]).endVertex();

			for (int j = 0; j <= 16; ++j) {
				float f7 = (float) j * ((float) Math.PI * 2F) / 16.0F;
				float f8 = Mth.sin(f7);
				float f9 = Mth.cos(f7);
				bufferbuilder.vertex(matrix4f, f8 * 120.0F, f9 * 120.0F, -f9 * 40.0F * afloat[3])
						.color(afloat[0], afloat[1], afloat[2], 0.0F).endVertex();
			}

			BufferUploader.drawWithShader(bufferbuilder.end());
			stack.popPose();
		}
		

		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
				GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		stack.pushPose();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, f11);
		stack.mulPose(Axis.ZP.rotationDegrees(level.getTimeOfDay(partialTicks) * 360.0F));
		Matrix4f matrix4f1 = stack.last().pose();
		float moondistance = 30.0F;
		stack.mulPose(Axis.YN.rotationDegrees(level.getGameTime()*1.5f + partialTicks));

		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
				GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		stack.pushPose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        RenderSystem.setShaderTexture(0, MOON_LOCATION);
        int l = 4% 4;
        int i1 = 4/ 4 % 2;
        float f13 = (float)(l + 0) / 4.0F;
        float f14 = (float)(i1 + 0) / 2.0F;
        float f15 = (float)(l + 1) / 4.0F;
        float f16 = (float)(i1 + 1) / 2.0F;
        
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix4f1, -moondistance, -100.0F, moondistance).uv(f15, f16).endVertex();
        bufferbuilder.vertex(matrix4f1, moondistance, -100.0F, moondistance).uv(f13, f16).endVertex();
        bufferbuilder.vertex(matrix4f1, moondistance, -100.0F, -moondistance).uv(f13, f14).endVertex();
        bufferbuilder.vertex(matrix4f1, -moondistance, -100.0F, -moondistance).uv(f15, f14).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
		stack.popPose();

		
		stack.popPose();


		Tesselator tesselator = Tesselator.getInstance();
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.setShaderTexture(0, END_SKY_LOCATION);

		for (int i = 0; i < 6; ++i) {
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

			Matrix4f matrix4f = stack.last().pose();
			bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
			bufferbuilder.vertex(matrix4f, -100.0F, -100.0F, -100.0F).uv(0.0F, 0.0F).color(40, 40, 40, 255).endVertex();
			bufferbuilder.vertex(matrix4f, -100.0F, -100.0F, 100.0F).uv(0.0F, 16.0F).color(40, 40, 40, 255).endVertex();
			bufferbuilder.vertex(matrix4f, 100.0F, -100.0F, 100.0F).uv(16.0F, 16.0F).color(40, 40, 40, 255).endVertex();
			bufferbuilder.vertex(matrix4f, 100.0F, -100.0F, -100.0F).uv(16.0F, 0.0F).color(40, 40, 40, 255).endVertex();
			tesselator.end();
			stack.popPose();
		}


		setupFog.run();
		FogRenderer.levelFogColor();

		// BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder(); TF -

	
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.depthMask(true);
		

		return true;

	}

	private static void createLightSky() {
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tesselator.getBuilder();
		if (skyBuffer != null) {
			skyBuffer.close();
		}

		skyBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
		BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = buildSkyDisc(bufferbuilder, 16.0F);
		skyBuffer.bind();
		skyBuffer.upload(bufferbuilder$renderedbuffer);
		VertexBuffer.unbind();
	}

	private static BufferBuilder.RenderedBuffer buildSkyDisc(BufferBuilder pBuilder, float pY) {
		float f = Math.signum(pY) * 512.0F;
		float f1 = 512.0F;
		RenderSystem.setShader(GameRenderer::getPositionShader);
		pBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
		pBuilder.vertex(0.0D, (double) pY, 0.0D).endVertex();

		for (int i = -180; i <= 180; i += 45) {
			pBuilder.vertex((double) (f * Mth.cos((float) i * ((float) Math.PI / 180F))), (double) pY,
					(double) (512.0F * Mth.sin((float) i * ((float) Math.PI / 180F)))).endVertex();
		}

		return pBuilder.end();
	}

	// [VanillaCopy] LevelRenderer.createStars
	private static void createStars() {
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tesselator.getBuilder();
		RenderSystem.setShader(GameRenderer::getPositionShader);
		if (starBuffer != null) {
			starBuffer.close();
		}

		starBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);

		BufferBuilder.RenderedBuffer renderedBuffer = drawStars(bufferbuilder);
		starBuffer.bind();
		starBuffer.upload(renderedBuffer);
		VertexBuffer.unbind();
	}

	// [VanillaCopy] of LevelRenderer.drawStars but with double the number of them
	private static BufferBuilder.RenderedBuffer drawStars(BufferBuilder bufferBuilder) {
		RandomSource random = RandomSource.create(10842L);
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

		// TF - 1500 -> 3000
		for (int i = 0; i < 3000; ++i) {

			double spreadX = random.nextFloat() * 2.0F - 1F;
			double spreadY = random.nextFloat() * 2.0F - 1F;
			double spreadZ = random.nextFloat() * 2.0F - 1F;
			double vertexEdgeLength = 0.15f + random.nextFloat() * 0.1F;
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

				for (int j = 0; j < 4; ++j) {
					double d18 = ((j & 2) - 1) * vertexEdgeLength;
					double d19 = ((j + 1 & 2) - 1) * vertexEdgeLength;
					double d21 = d18 * d16 - d19 * d15;
					double d22 = d19 * d16 + d18 * d15;
					double d23 = d21 * d12 + 0.0D * d13;
					double d24 = 0.0D * d12 - d21 * d13;
					double d25 = d24 * d9 - d22 * d10;
					double d26 = d22 * d9 + d24 * d10;

					bufferBuilder.vertex(xDensity + d25, yDensity + d23, zDensity + d26).endVertex();
				}
			}
		}

		return bufferBuilder.end();
	}
}