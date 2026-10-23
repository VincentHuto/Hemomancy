package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.vincenthuto.hemomancy.client.render.SchoolRenderTypes;
import com.vincenthuto.hemomancy.client.render.shader.ShaderInit;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

final class LuxUmbraRenderTypes {
    private static final SchoolRenderTypes.Clock CLOCK = new SchoolRenderTypes.Clock();
    static final RenderType LUX = SchoolRenderTypes.create(CLOCK, "manipulation_lux", ShaderInit.MANIPULATION_LUX,
            DefaultVertexFormat.POSITION_TEX_COLOR, false, false, 1f);
    static final RenderType UMBRA = SchoolRenderTypes.create(CLOCK, "manipulation_umbra", ShaderInit.MANIPULATION_UMBRA,
            DefaultVertexFormat.POSITION_TEX_COLOR, false, false, 1f);

    private LuxUmbraRenderTypes() {}

    static void begin(double gameTime) { CLOCK.begin(gameTime); }

    static void finish(MultiBufferSource.BufferSource buffers) {
        buffers.endBatch(UMBRA);
        buffers.endBatch(LUX);
    }
}
