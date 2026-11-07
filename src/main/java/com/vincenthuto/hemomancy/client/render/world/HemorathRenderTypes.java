package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.vincenthuto.hemomancy.client.render.SchoolRenderTypes;
import com.vincenthuto.hemomancy.client.render.shader.ShaderInit;
import net.minecraft.client.renderer.RenderType;

/**
 * Saint Hemorath's own materials, built once: his floor (shaders/core/world/hemorath_fissure; translucent,
 * depth-tested, no depth write) and his iron (shaders/core/world/hemorath_iron; solid, depth-writing, drawn in the
 * solid pass). Their vertices use the school encoding ({@link MaterialQuads}) with the state in the colour.
 */
final class HemorathRenderTypes {
	private static final SchoolRenderTypes.Clock CLOCK = new SchoolRenderTypes.Clock();
	static final RenderType FISSURE = SchoolRenderTypes.create(CLOCK, "hemorath_fissure", ShaderInit.HEMORATH_FISSURE,
			DefaultVertexFormat.POSITION_TEX_COLOR, false, false, 1.0F);
	static final RenderType IRON = SchoolRenderTypes.create(CLOCK, "hemorath_iron", ShaderInit.HEMORATH_IRON,
			DefaultVertexFormat.POSITION_TEX_COLOR, true, true, 1.0F);

	private HemorathRenderTypes() {
	}

	static void begin(double ticks) {
		CLOCK.begin(ticks);
	}
}
