package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.client.render.SchoolRenderTypes;
import com.vincenthuto.hemomancy.client.render.shader.ShaderHolder;
import com.vincenthuto.hemomancy.client.render.shader.ShaderInit;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;

/**
 * Saint Velorum's own materials, built once: his floor arrested by the cold and the liquid dark over it
 * (shaders/core/world/velorum_frost; translucent, depth-tested, no depth write), his Frozen Cruor standing out of it
 * (shaders/core/world/velorum_ice; solid and depth-writing, drawn in the solid pass) and the same cruor as glass
 * (translucent and depth-writing, for the shell panes that show the preserved body through them). Their vertices use the
 * school encoding ({@link MaterialQuads}) with the state in the colour. Besides the school clock they read one more
 * uniform, {@code CruorGlow}: the cruor's own dull glow, faint always and strong only in the Eclipse (revision
 * 2026-11-06), set by {@link #begin} each frame from the bosses' synced state.
 */
final class VelorumRenderTypes {
	/** The cruor's glow at rest and in the Eclipse (R.2 {@code CRUOR_GLOW}). */
	static final float GLOW_REST = 0.15F, GLOW_ECLIPSE = 0.8F;
	private static final SchoolRenderTypes.Clock CLOCK = new SchoolRenderTypes.Clock();
	private static float glow = GLOW_REST;
	private static final VertexFormat FORMAT = DefaultVertexFormat.POSITION_TEX_COLOR;
	static final RenderType FROST = RenderType.create("velorum_frost", FORMAT, VertexFormat.Mode.QUADS, 16384, false, true,
			state("velorum_frost", ShaderInit.VELORUM_FROST, false, false));
	static final RenderType ICE = RenderType.create("velorum_ice", FORMAT, VertexFormat.Mode.QUADS, 16384, false, false,
			state("velorum_ice", ShaderInit.VELORUM_ICE, true, true));
	static final RenderType ICE_CLEAR = RenderType.create("velorum_ice_clear", FORMAT, VertexFormat.Mode.QUADS, 16384, false, true,
			state("velorum_ice_clear", ShaderInit.VELORUM_ICE, false, true));

	private VelorumRenderTypes() {
	}

	/** The same composite state as {@link SchoolRenderTypes#create}, with the glow set beside the clock. */
	private static RenderType.CompositeState state(String name, ShaderHolder holder, boolean solid, boolean depthWrite) {
		return RenderType.CompositeState.builder()
				.setShaderState(holder.getShard())
				.setTexturingState(SchoolRenderTypes.uniforms(CLOCK, name, holder, 1.0F, () -> {
					ShaderInstance shader = holder.getInstance().get();
					if (shader != null) shader.safeGetUniform("CruorGlow").set(glow);
				}))
				.setTransparencyState(solid ? RenderType.NO_TRANSPARENCY : RenderType.TRANSLUCENT_TRANSPARENCY)
				.setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
				.setWriteMaskState(solid || depthWrite ? RenderType.COLOR_DEPTH_WRITE : RenderType.COLOR_WRITE)
				.setCullState(RenderType.NO_CULL)
				.setLightmapState(RenderType.NO_LIGHTMAP)
				.createCompositeState(false);
	}

	static void begin(double ticks) {
		begin(ticks, GLOW_REST);
	}

	/** The frame's clock and the cruor's glow (0.15 at rest, up to 0.8 in the Eclipse). */
	static void begin(double ticks, float cruorGlow) {
		CLOCK.begin(ticks);
		glow = Mth.clamp(cruorGlow, 0.0F, 1.0F);
	}
}
