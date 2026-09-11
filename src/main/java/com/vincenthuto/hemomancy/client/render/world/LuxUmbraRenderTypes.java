package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.client.render.shader.ExtendedShaderInstance;
import com.vincenthuto.hemomancy.client.render.shader.ShaderHolder;
import com.vincenthuto.hemomancy.common.init.ShaderInit;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

final class LuxUmbraRenderTypes {
    private static float time;
    static final RenderType LUX = create("manipulation_lux", ShaderInit.MANIPULATION_LUX);
    static final RenderType UMBRA = create("manipulation_umbra", ShaderInit.MANIPULATION_UMBRA);

    private LuxUmbraRenderTypes() {}

    static void begin(double gameTime) { time = (float) (gameTime % 24000); }

    static void finish(MultiBufferSource.BufferSource buffers) {
        buffers.endBatch(UMBRA);
        buffers.endBatch(LUX);
    }

    private static RenderType create(String name, ShaderHolder holder) {
        var uniforms = new RenderStateShard.TexturingStateShard(name + "_uniforms", () -> {
            var shader = holder.getInstance().get();
            if (shader != null) {
                shader.safeGetUniform("HemoTime").set(time);
                shader.safeGetUniform("ColorModulator").set(1f, 1f, 1f, 1f);
            }
        }, () -> {
            if (holder.getInstance().get() instanceof ExtendedShaderInstance shader) shader.setUniformDefaults();
        });
        return RenderType.create(name, DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS,
                16384, false, true, RenderType.CompositeState.builder()
                        .setShaderState(holder.getShard()).setTexturingState(uniforms)
                        .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(RenderType.LEQUAL_DEPTH_TEST).setWriteMaskState(RenderType.COLOR_WRITE)
                        .setCullState(RenderType.NO_CULL).setLightmapState(RenderType.NO_LIGHTMAP)
                        .createCompositeState(false));
    }
}
