package com.vincenthuto.hemomancy.common.registry;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class RenderTypeInit extends RenderType {

	private final static ResourceLocation laserBeam = new ResourceLocation(
			Hemomancy.MOD_ID + ":textures/misc/laser.png");
	private final static ResourceLocation vine = new ResourceLocation(
			Hemomancy.MOD_ID + ":textures/misc/vine.png");
	private final static ResourceLocation laserBeam2 = new ResourceLocation(
			Hemomancy.MOD_ID + ":textures/misc/laser2.png");
	private final static ResourceLocation laserBeamGlow = new ResourceLocation(
			Hemomancy.MOD_ID + ":textures/misc/laser_glow.png");

	static RenderType.CompositeState lightningState = RenderType.CompositeState.builder()
			.setShaderState(POSITION_COLOR_SHADER).setTransparencyState(LIGHTNING_TRANSPARENCY)
			.createCompositeState(false);

	public static final RenderType LIGHTNING = create("lightning", DefaultVertexFormat.POSITION_COLOR,
			VertexFormat.Mode.QUADS, 256, false, true, lightningState);
	public static final RenderType RADIANT_RENDER_TYPE = create("radiant", DefaultVertexFormat.POSITION_COLOR,
			VertexFormat.Mode.QUADS, 256, false, true,
			RenderType.CompositeState.builder().setShaderState(RENDERTYPE_LIGHTNING_SHADER)
					.setWriteMaskState(COLOR_WRITE).setTransparencyState(TRANSLUCENT_TRANSPARENCY)
					.setOutputState(PARTICLES_TARGET).setCullState(NO_CULL).setDepthTestState(LEQUAL_DEPTH_TEST)
					.createCompositeState(false));

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
	    
		public static final RenderType ENTITY_BEAM_RENDER_TYPE = create("beam",
				DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, true,
						RenderType.CompositeState.builder()
						.setShaderState(ShaderStateShard.RENDERTYPE_ENTITY_CUTOUT_NO_CULL_SHADER)
						.setLightmapState(LIGHTMAP)
						.setTextureState(new TextureStateShard(vine, false, false))
						.setWriteMaskState(COLOR_WRITE)
						.setCullState(NO_CULL)
						.createCompositeState(false));
		


	private static final RenderType CRIMSON_GLINT = create("glint_direct", DefaultVertexFormat.POSITION_TEX,
			VertexFormat.Mode.QUADS, 256, false, false,
			RenderType.CompositeState.builder().setShaderState(RENDERTYPE_GLINT_DIRECT_SHADER)
					.setTextureState(new RenderStateShard.TextureStateShard(
							Hemomancy.rloc("textures/item/crimson_item_glint.png"), true, false))
					.setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).setDepthTestState(EQUAL_DEPTH_TEST)
					.setTransparencyState(GLINT_TRANSPARENCY).setTexturingState(GLINT_TEXTURING)
					.createCompositeState(false));

	public static RenderType energySwirl(ResourceLocation pLocation, float pU, float pV) {
		return create("energy_swirl", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true,
				RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENERGY_SWIRL_SHADER)
						.setTextureState(new RenderStateShard.TextureStateShard(pLocation, false, false))
						.setTexturingState(new RenderStateShard.OffsetTexturingStateShard(pU, pV))
						.setTransparencyState(ADDITIVE_TRANSPARENCY).setCullState(NO_CULL).setLightmapState(LIGHTMAP)
						.setOverlayState(OVERLAY).createCompositeState(false));
	}

	public static RenderType getCrimsonGlint() {
		return CRIMSON_GLINT;
	}

	public static RenderType itemEnergySwirl(ResourceLocation pLocation, float pU, float pV) {
		return create("energy_swirl", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true,
				RenderType.CompositeState.builder().setShaderState(POSITION_COLOR_TEX_SHADER)
						.setTextureState(new RenderStateShard.TextureStateShard(pLocation, false, false))
						.setTexturingState(new RenderStateShard.OffsetTexturingStateShard(pU, pV))
						.setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(CULL).setLightmapState(LIGHTMAP)
						.setOverlayState(OVERLAY).createCompositeState(false));
	}

	public RenderTypeInit(String nameIn, VertexFormat formatIn, Mode drawModeIn, int bufferSizeIn,
			boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
		super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
	}

}
