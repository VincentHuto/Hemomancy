package com.vincenthuto.hemomancy.client.render.tile;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import com.vincenthuto.hemomancy.client.render.SeededRenderTypes;

public class RadiantPortalRendertype extends RenderType {

	// waves
	public static ShaderInstance WATER_SHADER;
	public static final ShaderStateShard RENDERTYPE_WATER_SHADER = new ShaderStateShard(() -> WATER_SHADER);

	public RadiantPortalRendertype(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_,
			boolean p_173182_, boolean p_173183_, Runnable one, Runnable two) {
		super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, one, two);
	}

	public static RenderType textWithWaterShader(ResourceLocation loc) {
		return WATER_TEXT_TYPES.get(loc, Boolean.TRUE);
	}

	/** One type per texture; it has no uniforms of its own. */
	private static final SeededRenderTypes<ResourceLocation, Boolean, RenderType> WATER_TEXT_TYPES =
			new SeededRenderTypes<>(64, (loc, none) -> {
				RenderType.CompositeState state = RenderType.CompositeState.builder().setShaderState(RENDERTYPE_WATER_SHADER)
						.setTextureState(new TextureStateShard(loc, false, false))
						.setTransparencyState(TRANSLUCENT_TRANSPARENCY).setLightmapState(LIGHTMAP).createCompositeState(false);
				return create("textWithWaterShader", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS,
						256, false, true, state);
			});
}
