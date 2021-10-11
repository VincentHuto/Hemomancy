package com.vincenthuto.hemomancy.init;

import java.util.OptionalDouble;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class RenderTypeInit extends RenderType {

	
    private final static ResourceLocation laserBeam = new ResourceLocation(Hemomancy.MOD_ID + ":textures/misc/laser.png");
    private final static ResourceLocation laserBeam2 = new ResourceLocation(Hemomancy.MOD_ID + ":textures/misc/laser2.png");
    private final static ResourceLocation laserBeamGlow = new ResourceLocation(Hemomancy.MOD_ID + ":textures/misc/laser_glow.png");

	
	public RenderTypeInit(String nameIn, VertexFormat formatIn, Mode drawModeIn, int bufferSizeIn,
			boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
		super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
	}

	@SuppressWarnings("unused")
	private static final LineStateShard THICK_LINES = new LineStateShard(OptionalDouble.of(3.0D));

	   public static final RenderType LASER_MAIN_BEAM = create("MiningLaserMainBeam",
	            DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256,false, false,
	            RenderType.CompositeState.builder().setTextureState(new TextureStateShard(laserBeam2, false, false))
	                    .setShaderState(ShaderStateShard.POSITION_COLOR_TEX_SHADER)
	                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
	                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
	                    .setDepthTestState(NO_DEPTH_TEST)
	                    .setCullState(NO_CULL)
	                    .setLightmapState(NO_LIGHTMAP)
	                    .setWriteMaskState(COLOR_WRITE)
	                    .createCompositeState(false));

	    public static final RenderType LASER_MAIN_ADDITIVE = create("MiningLaserAdditiveBeam",
	            DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256,false, false,
	            RenderType.CompositeState.builder().setTextureState(new TextureStateShard(laserBeamGlow, false, false))
	                    .setShaderState(ShaderStateShard.POSITION_COLOR_TEX_SHADER)
	                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
	                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
	                    .setDepthTestState(NO_DEPTH_TEST)
	                    .setCullState(NO_CULL)
	                    .setLightmapState(NO_LIGHTMAP)
	                    .setWriteMaskState(COLOR_WRITE)
	                    .createCompositeState(false));

	    public static final RenderType LASER_MAIN_CORE = create("MiningLaserCoreBeam",
	            DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256,false, false,
	            RenderType.CompositeState.builder().setTextureState(new TextureStateShard(laserBeam, false, false))
	                    .setShaderState(ShaderStateShard.POSITION_COLOR_TEX_SHADER)
	                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
	                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
	                    .setDepthTestState(NO_DEPTH_TEST)
	                    .setCullState(NO_CULL)
	                    .setLightmapState(NO_LIGHTMAP)
	                    .setWriteMaskState(COLOR_WRITE)
	                    .createCompositeState(false));


	public static RenderType getCrimsonGlint() {
		return CRIMSON_GLINT;
	}

	private static final RenderType CRIMSON_GLINT = create("glint_direct", DefaultVertexFormat.POSITION_TEX,
			VertexFormat.Mode.QUADS, 256, false, false,
			RenderType.CompositeState.builder().setShaderState(RENDERTYPE_GLINT_DIRECT_SHADER)
					.setTextureState(new RenderStateShard.TextureStateShard(
							new ResourceLocation(Hemomancy.MOD_ID, "textures/item/crimson_item_glint.png"), true,
							false))
					.setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).setDepthTestState(EQUAL_DEPTH_TEST)
					.setTransparencyState(GLINT_TRANSPARENCY).setTexturingState(GLINT_TEXTURING)
					.createCompositeState(false));

}