package com.huto.hemomancy.init;

import static net.minecraft.client.renderer.RenderType.create;

import java.util.OptionalDouble;

import org.lwjgl.opengl.GL11;

import com.huto.hemomancy.Hemomancy;
import com.mojang.blaze3d.platform.//GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
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

	public static final RenderType LASER_MAIN_BEAM = create("MiningLaserMainBeam",
			DefaultVertexFormat.POSITION_COLOR_TEX, GL11.GL_QUADS, 256,
			RenderType.CompositeState.builder().setTextureState(new TextureStateShard(laserBeam2, false, false))
					.setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDepthTestState(NO_DEPTH_TEST)
					.setWriteMaskState(COLOR_DEPTH_WRITE).createCompositeState(false));

	public static final RenderType LASER_MAIN_ADDITIVE = create("LaserAdditiveBeam",
			DefaultVertexFormat.POSITION_COLOR_TEX, GL11.GL_QUADS, 256,
			RenderType.CompositeState.builder().setTextureState(new TextureStateShard(laserBeamGlow, false, false))
					.setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDepthTestState(NO_DEPTH_TEST)
					.setWriteMaskState(COLOR_DEPTH_WRITE).createCompositeState(false));

	public static final RenderType LASER_MAIN_CORE = create("LaserCoreBeam", DefaultVertexFormat.POSITION_COLOR_TEX,
			GL11.GL_QUADS, 256,
			RenderType.CompositeState.builder().setTextureState(new TextureStateShard(laserBeam, false, false))
					.setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDepthTestState(NO_DEPTH_TEST)
					.setWriteMaskState(COLOR_DEPTH_WRITE).createCompositeState(false));

	public static final RenderType ENDTRANS = create("end_trans", DefaultVertexFormat.POSITION_COLOR_TEX, 7, 256, false,
			true,
			CompositeState.builder().setTextureState(new TextureStateShard(end_trans, false, false))
					.setTransparencyState(CRUMBLING_TRANSPARENCY).setWriteMaskState(COLOR_DEPTH_WRITE)
					.createCompositeState(false));

	@SuppressWarnings("unused")
	private static final LineStateShard THICK_LINES = new LineStateShard(OptionalDouble.of(3.0D));

	public static final ParticleRenderType GLOW_RENDER = new ParticleRenderType() {
		@Override
		public void begin(BufferBuilder buffer, TextureManager textureManager) {

			RenderSystem.disableAlphaTest();
			RenderSystem.enableBlend();
			RenderSystem.alphaFunc(516, 0.3f);
			RenderSystem.enableCull();
			textureManager.bindForSetup(TextureAtlas.LOCATION_PARTICLES);
			RenderSystem.depthMask(false);
			RenderSystem.blendFunc(//GlStateManager.SourceFactor.SRC_ALPHA.value, //GlStateManager.DestFactor.ONE.value);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormat.PARTICLE);
		}

		@Override
		public void end(Tesselator tessellator) {
			tessellator.end();
			RenderSystem.enableDepthTest();
			RenderSystem.enableAlphaTest();
			RenderSystem.depthMask(true);
			RenderSystem.blendFunc(//GlStateManager.SourceFactor.SRC_ALPHA.value, //GlStateManager.DestFactor.ONE.value);
			RenderSystem.disableCull();
			RenderSystem.alphaFunc(516, 0.1F);

		}

		@Override
		public String toString() {
			return "hemomancy:glow_rend";
		}
	};

	public static final ParticleRenderType DARK_GLOW_RENDER = new ParticleRenderType() {
		@Override
		public void begin(BufferBuilder buffer, TextureManager textureManager) {
			RenderSystem.depthMask(true);
			textureManager.bindForSetup(TextureAtlas.LOCATION_PARTICLES);
			RenderSystem.enableBlend();
			RenderSystem.enableCull();
			RenderSystem.depthMask(false);
			RenderSystem.blendFuncSeparate(//GlStateManager.SourceFactor.SRC_ALPHA,
					//GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, //GlStateManager.SourceFactor.ONE,
					//GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderSystem.alphaFunc(516, 0.003921569F);
			buffer.begin(7, DefaultVertexFormat.PARTICLE);
		}

		@Override
		public void end(Tesselator tessellator) {
			RenderSystem.disableCull();
			RenderSystem.depthMask(true);

			tessellator.end();

		}

		@Override
		public String toString() {
			return "hemomancy:dark_glow_rend";
		}
	};

	public static RenderType getCrimsonGlint() {
		return CRIMSON_GLINT;
	}

	private static RenderType CRIMSON_GLINT = create("crimson_glint", DefaultVertexFormat.POSITION_TEX, 7, 256,
			RenderType.CompositeState.builder()
					.setTextureState(new RenderStateShard.TextureStateShard(
							new ResourceLocation(Hemomancy.MOD_ID, "textures/item/crimson_item_glint.png"), true,
							false))
					.setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).setDepthTestState(EQUAL_DEPTH_TEST)
					.setTransparencyState(GLINT_TRANSPARENCY).setTexturingState(GLINT_TEXTURING)
					.createCompositeState(false));

}
