package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.vincenthuto.hemomancy.client.render.SchoolRenderTypes;
import com.vincenthuto.hemomancy.client.render.shader.ShaderInit;
import net.minecraft.client.renderer.RenderType;

/**
 * Saint Seraphae's own materials, built once (visual pass 2): her floor healing over (shaders/core/world/
 * seraphae_tissue; translucent, depth-tested, no depth write) and the bone and hematic iron that grow and hang in her
 * fight (shaders/core/world/seraphae_bone; solid, depth-writing, drawn in the solid pass). Their vertices use the school
 * encoding ({@link MaterialQuads}) with the state in the colour, never a tint.
 */
final class SeraphaeRenderTypes {
	private static final SchoolRenderTypes.Clock CLOCK = new SchoolRenderTypes.Clock();
	static final RenderType TISSUE = SchoolRenderTypes.create(CLOCK, "seraphae_tissue", ShaderInit.SERAPHAE_TISSUE,
			DefaultVertexFormat.POSITION_TEX_COLOR, false, false, 1.0F);
	static final RenderType BONE = SchoolRenderTypes.create(CLOCK, "seraphae_bone", ShaderInit.SERAPHAE_BONE,
			DefaultVertexFormat.POSITION_TEX_COLOR, true, true, 1.0F);

	private SeraphaeRenderTypes() {
	}

	static void begin(double ticks) {
		CLOCK.begin(ticks);
	}
}
