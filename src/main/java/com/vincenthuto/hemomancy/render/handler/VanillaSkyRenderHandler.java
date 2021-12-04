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
public class VanillaSkyRenderHandler implements ISkyRenderHandler {
	private static final ResourceLocation BLOOD_MOON_LOCATION = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/environment/blood_moon_phases.png");
	private static final ResourceLocation MOON_LOCATION = new ResourceLocation("textures/environment/moon_phases.png");
	private static final ResourceLocation SUN_LOCATION = new ResourceLocation("textures/environment/sun.png");
	private static final ResourceLocation CLOUDS_LOCATION = new ResourceLocation("textures/environment/clouds.png");
	private static final ResourceLocation END_SKY_LOCATION = new ResourceLocation("textures/environment/end_sky.png");
	private static final ResourceLocation FORCEFIELD_LOCATION = new ResourceLocation("textures/misc/forcefield.png");
	private static final ResourceLocation RAIN_LOCATION = new ResourceLocation("textures/environment/rain.png");
	private static final ResourceLocation SNOW_LOCATION = new ResourceLocation("textures/environment/snow.png");
	private VertexBuffer starVBO;
	private final VertexFormat vertexBufferFormat = DefaultVertexFormat.POSITION;
	
	public VanillaSkyRenderHandler() {
		generateStars();

		
	}
	@Override
	public void render(int ticks, float partialTicks, PoseStack ms, ClientLevel world, Minecraft mc) {
		LevelRenderer rg = mc.levelRenderer;

		if (mc.level.effects().skyType() == DimensionSpecialEffects.SkyType.NORMAL) {
			RenderSystem.disableTexture();
			Vec3 vec3 = world.getSkyColor(mc.gameRenderer.getMainCamera().getPosition(), partialTicks);
			float f = (float) vec3.x;
			float f1 = (float) vec3.y;
			float f2 = (float) vec3.z;
			FogRenderer.levelFogColor();
			BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
			RenderSystem.depthMask(false);
			RenderSystem.setShaderColor(f, f1, f2, 1.0F);
			ShaderInstance shaderinstance = RenderSystem.getShader();
			rg.skyBuffer.drawWithShader(ms.last().pose(), ms.last().pose(), shaderinstance);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			float[] afloat = world.effects().getSunriseColor(world.getTimeOfDay(partialTicks), partialTicks);
			if (afloat != null) {
				RenderSystem.setShader(GameRenderer::getPositionColorShader);
				RenderSystem.disableTexture();
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
				ms.pushPose();
				ms.mulPose(Vector3f.XP.rotationDegrees(90.0F));
				float f3 = Mth.sin(world.getSunAngle(partialTicks)) < 0.0F ? 180.0F : 0.0F;
				ms.mulPose(Vector3f.ZP.rotationDegrees(f3));
				ms.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
				float f4 = afloat[0];
				float f5 = afloat[1];
				float f6 = afloat[2];
				Matrix4f matrix4f = ms.last().pose();
				bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
				bufferbuilder.vertex(matrix4f, 0.0F, 100.0F, 0.0F).color(f4, f5, f6, afloat[3]).endVertex();
				int i = 16;

				for (int j = 0; j <= 16; ++j) {
					float f7 = j * ((float) Math.PI * 2F) / 16.0F;
					float f8 = Mth.sin(f7);
					float f9 = Mth.cos(f7);
					bufferbuilder.vertex(matrix4f, f8 * 120.0F, f9 * 120.0F, -f9 * 40.0F * afloat[3])
							.color(afloat[0], afloat[1], afloat[2], 0.0F).endVertex();
				}

				bufferbuilder.end();
				BufferUploader.end(bufferbuilder);
				ms.popPose();
			}

			RenderSystem.enableTexture();
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
					GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			ms.pushPose();
			float f11 = 1.0F - world.getRainLevel(partialTicks);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, f11);
			ms.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
			ms.mulPose(Vector3f.XP.rotationDegrees(world.getTimeOfDay(partialTicks) * 360.0F));
			Matrix4f matrix4f1 = ms.last().pose();
			float f12 = 30.0F;
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, SUN_LOCATION);
			bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
			bufferbuilder.vertex(matrix4f1, -f12, 100.0F, -f12).uv(0.0F, 0.0F).endVertex();
			bufferbuilder.vertex(matrix4f1, f12, 100.0F, -f12).uv(1.0F, 0.0F).endVertex();
			bufferbuilder.vertex(matrix4f1, f12, 100.0F, f12).uv(1.0F, 1.0F).endVertex();
			bufferbuilder.vertex(matrix4f1, -f12, 100.0F, f12).uv(0.0F, 1.0F).endVertex();
			bufferbuilder.end();
			BufferUploader.end(bufferbuilder);
			f12 = 20.0F;
			RenderSystem.setShaderTexture(0, MOON_LOCATION);
			int k = world.getMoonPhase();
			int l = k % 4;
			int i1 = k / 4 % 2;
			float f13 = (l + 0) / 4.0F;
			float f14 = (i1 + 0) / 2.0F;
			float f15 = (l + 1) / 4.0F;
			float f16 = (i1 + 1) / 2.0F;
			bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
			bufferbuilder.vertex(matrix4f1, -f12, -100.0F, f12).uv(f15, f16).endVertex();
			bufferbuilder.vertex(matrix4f1, f12, -100.0F, f12).uv(f13, f16).endVertex();
			bufferbuilder.vertex(matrix4f1, f12, -100.0F, -f12).uv(f13, f14).endVertex();
			bufferbuilder.vertex(matrix4f1, -f12, -100.0F, -f12).uv(f15, f14).endVertex();
			bufferbuilder.end();
			BufferUploader.end(bufferbuilder);
			RenderSystem.disableTexture();
			float f10 = world.getStarBrightness(partialTicks) * f11;
			if (f10 > 0.0F) {
				RenderSystem.setShaderColor(f10, f10, f10, f10);
				FogRenderer.setupNoFog();
				rg.skyBuffer.drawWithShader(ms.last().pose(), matrix4f1, GameRenderer.getPositionShader());
				// p_181413_.run();
			}

			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.disableBlend();
			ms.popPose();
			RenderSystem.disableTexture();
			RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
			double d0 = mc.player.getEyePosition(partialTicks).y - world.getLevelData().getHorizonHeight(world);
			if (d0 < 0.0D) {
				ms.pushPose();
				ms.translate(0.0D, 12.0D, 0.0D);
				rg.darkBuffer.drawWithShader(ms.last().pose(), matrix4f1, shaderinstance);
				ms.popPose();
			}

			if (world.effects().hasGround()) {
				RenderSystem.setShaderColor(f * 0.2F + 0.04F, f1 * 0.2F + 0.04F, f2 * 0.6F + 0.1F, 1.0F);
			} else {
				RenderSystem.setShaderColor(f, f1, f2, 1.0F);
			}

			RenderSystem.enableTexture();
			RenderSystem.depthMask(true);
		}
	}
	
	// [VanillaCopy] RenderGlobal.generateStars
		private void generateStars() {
			Tesselator tessellator = Tesselator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuilder();

			if (this.starVBO != null) {
				this.starVBO.close();
			}

			// TF - inlined RenderGlobal field that's only used once here
			this.starVBO = new VertexBuffer(/* DefaultVertexFormat.POSITION */);
			this.renderStars(bufferbuilder);
			bufferbuilder.end();
			this.starVBO.upload(bufferbuilder);
		}

		// [VanillaCopy] of RenderGlobal.renderStars but with double the number of them
		@SuppressWarnings("unused")
		private void renderStars(BufferBuilder bufferBuilder) {
			Random random = new Random(10842L);
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

			// TF - 1500 -> 3000
			for (int i = 0; i < 3000; ++i) {
				double d0 = random.nextFloat() * 2.0F - 1.0F;
				double d1 = random.nextFloat() * 2.0F - 1.0F;
				double d2 = random.nextFloat() * 2.0F - 1.0F;
				double d3 = 0.15F + random.nextFloat() * 0.1F;
				double d4 = d0 * d0 + d1 * d1 + d2 * d2;

				if (d4 < 1.0D && d4 > 0.01D) {
					d4 = 1.0D / Math.sqrt(d4);
					d0 = d0 * d4;
					d1 = d1 * d4;
					d2 = d2 * d4;
					double d5 = d0 * 100.0D;
					double d6 = d1 * 100.0D;
					double d7 = d2 * 100.0D;
					double d8 = Math.atan2(d0, d2);
					double d9 = Math.sin(d8);
					double d10 = Math.cos(d8);
					double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
					double d12 = Math.sin(d11);
					double d13 = Math.cos(d11);
					double d14 = random.nextDouble() * Math.PI * 2.0D;
					double d15 = Math.sin(d14);
					double d16 = Math.cos(d14);

					for (int j = 0; j < 4; ++j) {
						double d17 = 0.0D;
						double d18 = ((j & 2) - 1) * d3;
						double d19 = ((j + 1 & 2) - 1) * d3;
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