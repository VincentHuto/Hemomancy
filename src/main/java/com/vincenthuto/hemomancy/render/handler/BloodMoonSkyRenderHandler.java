package com.vincenthuto.hemomancy.render.handler;

import java.util.Random;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ISkyRenderHandler;
@OnlyIn(Dist.CLIENT)
public class BloodMoonSkyRenderHandler implements ISkyRenderHandler {
	private static final ResourceLocation BLOOD_MOON_LOCATION = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/environment/blood_moon_phases.png");
//	private static final ResourceLocation MOON_LOCATION = new ResourceLocation("textures/environment/moon_phases.png");
	private static final ResourceLocation SUN_LOCATION = new ResourceLocation("textures/environment/sun.png");
//	private static final ResourceLocation CLOUDS_LOCATION = new ResourceLocation("textures/environment/clouds.png");
//	private static final ResourceLocation END_SKY_LOCATION = new ResourceLocation("textures/environment/end_sky.png");
//	private static final ResourceLocation FORCEFIELD_LOCATION = new ResourceLocation("textures/misc/forcefield.png");
//	private static final ResourceLocation RAIN_LOCATION = new ResourceLocation("textures/environment/rain.png");
//	private static final ResourceLocation SNOW_LOCATION = new ResourceLocation("textures/environment/snow.png");
	private VertexBuffer starVBO;
	private final VertexFormat vertexBufferFormat = DefaultVertexFormat.POSITION;

	public BloodMoonSkyRenderHandler() {
		generateStars();
	}

	@Override
	public void render(int ticks, float partialTicks, PoseStack matrixStack, ClientLevel world, Minecraft mc) {
		Matrix4f matrix4f = matrixStack.last().pose();
		this.renderSky(matrixStack, matrix4f, partialTicks, world, mc, ticks);

	}

	public void renderSky(PoseStack ms, Matrix4f p_181411_, float p_181412_, ClientLevel level, Minecraft minecraft,
			int ticks) {
		if (minecraft.level.effects().skyType() == DimensionSpecialEffects.SkyType.NORMAL) {
			RenderSystem.disableTexture();
			LevelRenderer rg = minecraft.levelRenderer;
			Vec3 vec3 = level.getSkyColor(minecraft.gameRenderer.getMainCamera().getPosition(), p_181412_);
			float f = (float) vec3.x;
			float f1 = (float) vec3.y;
			float f2 = (float) vec3.z;
			FogRenderer.levelFogColor();
			Tesselator tessellator = Tesselator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuilder();
			RenderSystem.depthMask(false);
			RenderSystem.setShaderColor(f, f1, f2, 1.0F);
			ShaderInstance shaderinstance = RenderSystem.getShader();
			// Sky Color
			// RenderSystem.setShaderColor(0.01F, 0F, 0.0F, 0.1F);
			rg.skyBuffer.bind();
			rg.skyBuffer.drawWithShader(ms.last().pose(), RenderSystem.getProjectionMatrix(), shaderinstance);
			VertexBuffer.unbind();
			this.vertexBufferFormat.clearBufferState();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.enableTexture();
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
					GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			ms.pushPose();
			float f11 = 1.0F - level.getRainLevel(p_181412_);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, f11);
			ms.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
			ms.mulPose(Vector3f.XP.rotationDegrees(level.getTimeOfDay(p_181412_) * 360.0F));
			Matrix4f matrix4f1 = ms.last().pose();
			float scale = 30.0F;
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, SUN_LOCATION);
			bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
			bufferbuilder.vertex(matrix4f1, -scale, 100.0F, -scale).uv(0.0F, 0.0F).endVertex();
			bufferbuilder.vertex(matrix4f1, scale, 100.0F, -scale).uv(1.0F, 0.0F).endVertex();
			bufferbuilder.vertex(matrix4f1, scale, 100.0F, scale).uv(1.0F, 1.0F).endVertex();
			bufferbuilder.vertex(matrix4f1, -scale, 100.0F, scale).uv(0.0F, 1.0F).endVertex();
			bufferbuilder.end();
			BufferUploader.end(bufferbuilder);
			scale = 20.0F;
			RenderSystem.setShaderTexture(0, BLOOD_MOON_LOCATION);
			int moonPhase = level.getMoonPhase();
			int l = moonPhase % 4;
			int i1 = moonPhase / 4 % 2;
			float u2 = (l + 0) / 4.0F;
			float v2 = (i1 + 0) / 2.0F;
			float u1 = (l + 1) / 4.0F;
			float v1 = (i1 + 1) / 2.0F;
			bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
			bufferbuilder.vertex(matrix4f1, -scale, -100.0F, scale).uv(u1, v1).endVertex();
			bufferbuilder.vertex(matrix4f1, scale, -100.0F, scale).uv(u2, v1).endVertex();
			bufferbuilder.vertex(matrix4f1, scale, -100.0F, -scale).uv(u2, v2).endVertex();
			bufferbuilder.vertex(matrix4f1, -scale, -100.0F, -scale).uv(u1, v2).endVertex();
			bufferbuilder.end();
			BufferUploader.end(bufferbuilder);
			RenderSystem.disableTexture();
			float time = Mth.sin(level.getGameTime() * 0.05f) * 0.5f;
			RenderSystem.setShaderColor(1f + time, 0.3f, 0.3f, 1);

			// Makes sure stars actually dim
			float f10 = level.getStarBrightness(p_181412_) * f11;
			if (f10 > 0.0F) {
				this.starVBO.bind();
				this.starVBO.drawWithShader(ms.last().pose(), RenderSystem.getProjectionMatrix(), shaderinstance);
				VertexBuffer.unbind();
				this.vertexBufferFormat.clearBufferState();
			}

			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.disableBlend();
			ms.popPose();
			RenderSystem.disableTexture();
			RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
			double d0 = minecraft.player.getEyePosition(p_181412_).y - level.getLevelData().getHorizonHeight(level);
			if (d0 < 0.0D) {
				ms.pushPose();
				ms.translate(0.0F, 12.0F, 0.0F);

				rg.darkBuffer.bind();
				rg.darkBuffer.drawWithShader(ms.last().pose(), RenderSystem.getProjectionMatrix(), shaderinstance);
				VertexBuffer.unbind();
				this.vertexBufferFormat.clearBufferState();

				ms.popPose();
				float f19 = -((float) (d0 + 65.0D));
				bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
				bufferbuilder.vertex(-1.0D, f19, 1.0D).color(0, 0, 0, 255).endVertex();
				bufferbuilder.vertex(1.0D, f19, 1.0D).color(0, 0, 0, 255).endVertex();
				bufferbuilder.vertex(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
				bufferbuilder.vertex(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
				bufferbuilder.vertex(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
				bufferbuilder.vertex(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
				bufferbuilder.vertex(1.0D, f19, -1.0D).color(0, 0, 0, 255).endVertex();
				bufferbuilder.vertex(-1.0D, f19, -1.0D).color(0, 0, 0, 255).endVertex();
				bufferbuilder.vertex(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
				bufferbuilder.vertex(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
				bufferbuilder.vertex(1.0D, f19, 1.0D).color(0, 0, 0, 255).endVertex();
				bufferbuilder.vertex(1.0D, f19, -1.0D).color(0, 0, 0, 255).endVertex();
				bufferbuilder.vertex(-1.0D, f19, -1.0D).color(0, 0, 0, 255).endVertex();
				bufferbuilder.vertex(-1.0D, f19, 1.0D).color(0, 0, 0, 255).endVertex();
				bufferbuilder.vertex(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
				bufferbuilder.vertex(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
				bufferbuilder.vertex(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
				bufferbuilder.vertex(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
				bufferbuilder.vertex(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
				bufferbuilder.vertex(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
				tessellator.end();
			}

			if (level.effects().hasGround()) {
				RenderSystem.setShaderColor(f * 0.2F + 0.04F, f1 * 0.2F + 0.04F, f2 * 0.6F + 0.1F, 1.0F);
			} else {
				RenderSystem.setShaderColor(f, f1, f2, 1.0F);
			}

			RenderSystem.enableTexture();
			RenderSystem.depthMask(true);
		}
	}

	private void generateStars() {
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuilder();
		if (this.starVBO != null) {
			this.starVBO.close();
		}
		this.starVBO = new VertexBuffer();
		this.renderStars(bufferbuilder);
		bufferbuilder.end();
		this.starVBO.upload(bufferbuilder);
	}

	// [VanillaCopy] of RenderGlobal.renderStars but with double the number of them
	@SuppressWarnings("unused")
	private void renderStars(BufferBuilder bufferBuilder) {
		Random random = new Random(10842L);
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
		int starCount = 3500;
		for (int i = 0; i < starCount; ++i) {
			double sX = random.nextFloat() * 2.0F - 1.0F;
			double sY = random.nextFloat() * 2.0F - 1.0F;
			double sZ = random.nextFloat() * 2.0F - 1.0F;
			float starScale = 0.15F;
			double starScaleRandomizer = starScale + random.nextFloat() * 0.1F;

			double sphereEquation = sX * sX + sY * sY + sZ * sZ;

			// Begin black magic trig
			if (sphereEquation < 1.0D && sphereEquation > 0.01D) {
				sphereEquation = 1.0D / Math.sqrt(sphereEquation);
				sX = sX * sphereEquation;
				sY = sY * sphereEquation;
				sZ = sZ * sphereEquation;
				double d5 = sX * 100.0D;
				double d6 = sY * 100.0D;
				double d7 = sZ * 100.0D;
				double d8 = Math.atan2(sX, sZ);
				double d9 = Math.sin(d8);
				double d10 = Math.cos(d8);
				double d11 = Math.atan2(Math.sqrt(sX * sX + sZ * sZ), sY);
				double d12 = Math.sin(d11);
				double d13 = Math.cos(d11);
				double d14 = random.nextDouble() * Math.PI * 2.0D;
				double d15 = Math.sin(d14);
				double d16 = Math.cos(d14);

				for (int j = 0; j < 4; ++j) {
					double d17 = 0.0D;
					double d18 = ((j & 2) - 1) * starScaleRandomizer;
					double d19 = ((j + 1 & 2) - 1) * starScaleRandomizer;
					double d20 = 0.0D;
					double d21 = d18 * d16 - d19 * d15;
					double d22 = d19 * d16 + d18 * d15;
					double d23 = d21 * d12 + 0.0D * d13;
					double d24 = 0.0D * d12 - d21 * d13;
					double d25 = d24 * d9 - d22 * d10;
					double d26 = d22 * d9 + d24 * d10;
					bufferBuilder.vertex(d5 + d25, d6 + d23, d7 + d26).endVertex();
				}
			}
		}
	}

}
