package com.vincenthuto.hemomancy.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;

/**
 * Client-local render types used by animated item renderers.
 */
public final class HemoRenderTypes {

	private HemoRenderTypes() {
	}

	public static final RenderType QLIPHOTH_CORE = RenderType.create("qliphoth_core",
			DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, true,
			RenderType.CompositeState.builder()
					.setShaderState(RenderType.RENDERTYPE_LIGHTNING_SHADER)
					.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
					.setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
					.setWriteMaskState(RenderType.COLOR_DEPTH_WRITE)
					.setCullState(RenderType.NO_CULL)
					.setLightmapState(RenderType.NO_LIGHTMAP)
					.createCompositeState(false));

	public static final RenderType QLIPHOTH_GLOW = RenderType.create("qliphoth_glow",
			DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, true,
			RenderType.CompositeState.builder()
					.setShaderState(RenderType.RENDERTYPE_LIGHTNING_SHADER)
					.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
					.setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
					.setWriteMaskState(RenderType.COLOR_WRITE)
					.setCullState(RenderType.NO_CULL)
					.setLightmapState(RenderType.NO_LIGHTMAP)
					.createCompositeState(false));
}

