package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.vincenthuto.hemomancy.client.render.SchoolRenderTypes;
import com.vincenthuto.hemomancy.client.render.shader.ShaderInit;
import net.minecraft.client.renderer.RenderType;

/**
 * Saint Putriciel's own materials (visual pass 2), built once: the floor the rot colonises and the fire scorches
 * (shaders/core/world/putriciel_ground; translucent, depth-tested, no depth write), the rot in the air
 * (putriciel_spore; translucent) and the growths it pushes up (putriciel_growth; solid, depth-writing, drawn in the
 * solid pass). Their vertices use the school encoding ({@link MaterialQuads}) with the state in the colour. The fire
 * itself stays the Flammeus {@code FLAME} material the pyres burn.
 */
final class PutricielRenderTypes {
	private static final SchoolRenderTypes.Clock CLOCK = new SchoolRenderTypes.Clock();
	static final RenderType GROUND = SchoolRenderTypes.create(CLOCK, "putriciel_ground", ShaderInit.PUTRICIEL_GROUND,
			DefaultVertexFormat.POSITION_TEX_COLOR, false, false, 1.0F);
	static final RenderType SPORE = SchoolRenderTypes.create(CLOCK, "putriciel_spore", ShaderInit.PUTRICIEL_SPORE,
			DefaultVertexFormat.POSITION_TEX_COLOR, false, false, 1.0F);
	static final RenderType GROWTH = SchoolRenderTypes.create(CLOCK, "putriciel_growth", ShaderInit.PUTRICIEL_GROWTH,
			DefaultVertexFormat.POSITION_TEX_COLOR, true, true, 1.0F);

	private PutricielRenderTypes() {
	}

	static void begin(double ticks) {
		CLOCK.begin(ticks);
	}
}
