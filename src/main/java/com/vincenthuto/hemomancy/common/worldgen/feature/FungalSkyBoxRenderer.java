package com.vincenthuto.hemomancy.common.worldgen.feature;

import org.joml.Matrix4f;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FungalSkyBoxRenderer {

	private static VertexBuffer starBuffer;

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
		ResourceLocation END_SKY_LOCATION = Hemomancy.rloc("textures/environment/blood_fill_tiled.png");

		stack.pushPose();
		RenderSystem.enableBlend();
		RenderSystem.depthMask(false);
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.setShaderTexture(0, END_SKY_LOCATION);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tesselator.getBuilder();
		stack.mulPose(Axis.YN.rotationDegrees(-90.0F));
		stack.mulPose(Axis.XN.rotationDegrees(level.getTimeOfDay(partialTicks) * 360.0F));
		stack.mulPose(Axis.XN.rotationDegrees(level.getTimeOfDay(partialTicks) * 360.0F));
		stack.mulPose(Axis.YN.rotationDegrees(level.getGameTime() + partialTicks));
		var value = oscillatingValueWithHeartbeat((level.getGameTime() + partialTicks) % 360);


		stack.translate(0, 90, 0);
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
			bufferbuilder.vertex(matrix4f, -100.0F, -100.0F, -100.0F).uv(0.0F, 0.0F).color(40, 40, 40, 125).endVertex();
			bufferbuilder.vertex(matrix4f, -100.0F, -100.0F, 100.0F).uv(0.0F, 16.0F).color(40, 40, 40, 125).endVertex();
			bufferbuilder.vertex(matrix4f, 100.0F, -100.0F, 100.0F).uv(16.0F, 16.0F).color(40, 40, 40, 125).endVertex();
			bufferbuilder.vertex(matrix4f, 100.0F, -100.0F, -100.0F).uv(16.0F, 0.0F).color(40, 40, 40, 125).endVertex();
			tesselator.end();
			
			stack.popPose();
		}
		
		stack.popPose();
		
		


		LevelRenderer levelRenderer = Minecraft.getInstance().levelRenderer;
		setupFog.run();
		Vec3 vec3 = level.getSkyColor(camera.getPosition(), partialTicks);
		float f = (float) vec3.x();
		float f1 = (float) vec3.y();
		float f2 = (float) vec3.z();
		FogRenderer.levelFogColor();
		// BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder(); TF -
		// Unused
		RenderSystem.depthMask(false);
		RenderSystem.setShaderColor(f, f1, f2, 0.5F);
		ShaderInstance shaderinstance = RenderSystem.getShader();
		VertexBuffer.unbind();
		
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
				GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		stack.pushPose();
		float f11 = 0.5F - level.getRainLevel(partialTicks);
		RenderSystem.setShaderColor(0.5F, 0.0F, 0.0F, f11);
		stack.mulPose(Axis.YP.rotationDegrees(-90.0F));
		stack.mulPose(Axis.XP.rotationDegrees(level.getTimeOfDay(partialTicks) * 360.0F));
		stack.mulPose(Axis.XP.rotationDegrees(level.getTimeOfDay(partialTicks) * 360.0F));
		stack.mulPose(Axis.YP.rotationDegrees(level.getGameTime() + partialTicks));

		float f10 = 0.5F; // TF - stars are always bright
		RenderSystem.setShaderColor(1.0F, 1.0F, 0.0F, 1.0F);
		FogRenderer.setupNoFog();
		starBuffer.bind();
		starBuffer.drawWithShader(stack.last().pose(), projectionMatrix, GameRenderer.getPositionShader());
		RenderSystem.disableBlend();
		RenderSystem.defaultBlendFunc();
		stack.popPose();
		RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
		double d0 = camera.getEntity().getEyePosition(partialTicks).y(); // - level.getLevelData().getHorizonHeight(level); // TF: Lower Void Horizon Y-Threshold from 63 to 0
		if (d0 < 0.0D) {
			stack.pushPose();
			stack.translate(0.0F, 12.0F, 0.0F);
			levelRenderer.darkBuffer.bind();
			levelRenderer.darkBuffer.drawWithShader(stack.last().pose(), projectionMatrix, shaderinstance);
			VertexBuffer.unbind();
			stack.popPose();
		}

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.depthMask(true);
		
		createStars();


		return true;

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

			double spreadX = random.nextFloat() * 1.0F - 0.5F;
			double spreadY = random.nextFloat() * 1.0F - 0.5F;
			double spreadZ = random.nextFloat() * 1.0F - 0.5F;
			double vertexEdgeLength = 0.1f + random.nextFloat() * 0.1F;
			double pythagorean = spreadX * spreadX + spreadY * spreadY + spreadZ * spreadZ;
			if (pythagorean < 1.0D && pythagorean > 0.01D) {
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