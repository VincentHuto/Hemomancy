package com.huto.hemomancy.init;

import java.util.OptionalDouble;

import org.lwjgl.opengl.GL11;

import com.huto.hemomancy.Hemomancy;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.resources.ResourceLocation;

public class RenderTypeInit extends RenderType {

	private final static ResourceLocation laserBeam = new ResourceLocation(
			Hemomancy.MOD_ID + ":textures/misc/laser.png");
	private final static ResourceLocation laserBeam2 = new ResourceLocation(
			Hemomancy.MOD_ID + ":textures/misc/laser2.png");
	private final static ResourceLocation laserBeamGlow = new ResourceLocation(
			Hemomancy.MOD_ID + ":textures/misc/laser_glow.png");
	private final static ResourceLocation end_trans = new ResourceLocation(
			Hemomancy.MOD_ID + ":textures/block/end_portal.png");

	public RenderTypeInit(String nameIn, VertexFormat formatIn, int drawModeIn, int bufferSizeIn, boolean useDelegateIn,
			boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
		super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
	}

	public static final RenderType LASER_MAIN_BEAM = makeType("MiningLaserMainBeam",
			DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_QUADS, 256,
			RenderType.State.getBuilder().texture(new TextureState(laserBeam2, false, false))
					.transparency(TRANSLUCENT_TRANSPARENCY).depthTest(DEPTH_ALWAYS).writeMask(COLOR_DEPTH_WRITE)
					.build(false));

	public static final RenderType LASER_MAIN_ADDITIVE = makeType("LaserAdditiveBeam",
			DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_QUADS, 256,
			RenderType.State.getBuilder().texture(new TextureState(laserBeamGlow, false, false))
					.transparency(TRANSLUCENT_TRANSPARENCY).depthTest(DEPTH_ALWAYS).writeMask(COLOR_DEPTH_WRITE)
					.build(false));

	public static final RenderType LASER_MAIN_CORE = makeType("LaserCoreBeam", DefaultVertexFormats.POSITION_COLOR_TEX,
			GL11.GL_QUADS, 256,
			RenderType.State.getBuilder().texture(new TextureState(laserBeam, false, false))
					.transparency(TRANSLUCENT_TRANSPARENCY).depthTest(DEPTH_ALWAYS).writeMask(COLOR_DEPTH_WRITE)
					.build(false));

	public static final RenderType ENDTRANS = makeType("end_trans", DefaultVertexFormats.POSITION_COLOR_TEX, 7, 256,
			false, true, State.getBuilder().texture(new TextureState(end_trans, false, false))
					.transparency(CRUMBLING_TRANSPARENCY).writeMask(COLOR_DEPTH_WRITE).build(false));

	@SuppressWarnings("unused")
	private static final LineState THICK_LINES = new LineState(OptionalDouble.of(3.0D));

	public static final IParticleRenderType GLOW_RENDER = new IParticleRenderType() {
		@Override
		public void beginRender(BufferBuilder buffer, TextureManager textureManager) {

			RenderSystem.disableAlphaTest();
			RenderSystem.enableBlend();
			RenderSystem.alphaFunc(516, 0.3f);
			RenderSystem.enableCull();
			textureManager.bindTexture(AtlasTexture.LOCATION_PARTICLES_TEXTURE);
			RenderSystem.depthMask(false);
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA.param, GlStateManager.DestFactor.ONE.param);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
		}

		@Override
		public void finishRender(Tessellator tessellator) {
			tessellator.draw();
			RenderSystem.enableDepthTest();
			RenderSystem.enableAlphaTest();
			RenderSystem.depthMask(true);
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA.param, GlStateManager.DestFactor.ONE.param);
			RenderSystem.disableCull();
			RenderSystem.alphaFunc(516, 0.1F);

		}

		@Override
		public String toString() {
			return "hemomancy:glow_rend";
		}
	};

	public static final IParticleRenderType DARK_GLOW_RENDER = new IParticleRenderType() {
		@Override
		public void beginRender(BufferBuilder buffer, TextureManager textureManager) {
			RenderSystem.depthMask(true);
			textureManager.bindTexture(AtlasTexture.LOCATION_PARTICLES_TEXTURE);
			RenderSystem.enableBlend();
			RenderSystem.enableCull();
			RenderSystem.depthMask(false);
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderSystem.alphaFunc(516, 0.003921569F);
			buffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
		}

		@Override
		public void finishRender(Tessellator tessellator) {
			RenderSystem.disableCull();
			RenderSystem.depthMask(true);

			tessellator.draw();

		}

		@Override
		public String toString() {
			return "hemomancy:dark_glow_rend";
		}
	};

	public static RenderType getCrimsonGlint() {
		return CRIMSON_GLINT;
	}

	private static RenderType CRIMSON_GLINT = makeType("crimson_glint", DefaultVertexFormats.POSITION_TEX, 7, 256,
			RenderType.State.getBuilder()
					.texture(new RenderState.TextureState(
							new ResourceLocation(Hemomancy.MOD_ID, "textures/item/crimson_item_glint.png"), true,
							false))
					.writeMask(COLOR_WRITE).cull(CULL_DISABLED).depthTest(DEPTH_EQUAL).transparency(GLINT_TRANSPARENCY)
					.texturing(GLINT_TEXTURING).build(false));

}
