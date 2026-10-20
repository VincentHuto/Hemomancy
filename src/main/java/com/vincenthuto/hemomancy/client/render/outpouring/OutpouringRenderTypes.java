package com.vincenthuto.hemomancy.client.render.outpouring;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.shader.ExtendedShaderInstance;
import com.vincenthuto.hemomancy.client.render.shader.ShaderHolder;
import com.vincenthuto.hemomancy.common.init.ShaderInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

/** Render types for the cast outpouring. All share one clock, set once per frame by {@link #begin(double)}. */
public final class OutpouringRenderTypes {
    private static final ResourceLocation ATLAS_WIDE = Hemomancy.rloc("dynamic/cast_body_atlas_wide");
    private static final ResourceLocation ATLAS_SLIM = Hemomancy.rloc("dynamic/cast_body_atlas_slim");
    private static float time;
    private static boolean atlases;
    private static RenderType veinsWide, veinsSlim;

    private OutpouringRenderTypes() {}

    public static void begin(double ticks) { time = (float) (ticks % 24000); }

    /** The vein overlay for a skin layout; the body atlas textures upload on first use, on the render thread. */
    static RenderType veins(boolean slim) {
        if (!atlases) {
            upload(ATLAS_WIDE, false);
            upload(ATLAS_SLIM, true);
            atlases = true;
        }
        if (veinsWide == null) {
            veinsWide = entity("cast_veins_wide", ShaderInit.CAST_VEINS, ATLAS_WIDE);
            veinsSlim = entity("cast_veins_slim", ShaderInit.CAST_VEINS, ATLAS_SLIM);
        }
        return slim ? veinsSlim : veinsWide;
    }

    private static void upload(ResourceLocation location, boolean slim) {
        var image = new NativeImage(64, 64, false);
        int[] pixels = SkinBodyAtlas.rgba(slim);
        for (int v = 0; v < 64; v++) for (int u = 0; u < 64; u++) image.setPixelRGBA(u, v, pixels[v * 64 + u]);
        Minecraft.getInstance().getTextureManager().register(location, new DynamicTexture(image));
    }

    static RenderStateShard.TexturingStateShard clock(String name, ShaderHolder holder, Runnable extra) {
        return new RenderStateShard.TexturingStateShard(name + "_uniforms", () -> {
            var shader = holder.getInstance().get();
            if (shader != null) {
                shader.safeGetUniform("HemoTime").set(time);
                shader.safeGetUniform("ColorModulator").set(1f, 1f, 1f, 1f);
            }
            extra.run();
        }, () -> {
            if (holder.getInstance().get() instanceof ExtendedShaderInstance shader) shader.setUniformDefaults();
        });
    }

    private static RenderType entity(String name, ShaderHolder holder, ResourceLocation atlas) {
        return RenderType.create(name, DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 65536, false, true,
                RenderType.CompositeState.builder()
                        .setShaderState(holder.getShard())
                        .setTextureState(new RenderStateShard.TextureStateShard(atlas, false, false))
                        .setTexturingState(clock(name, holder, () -> {}))
                        .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(RenderType.COLOR_WRITE)
                        .setCullState(RenderType.NO_CULL)
                        .setLightmapState(RenderType.NO_LIGHTMAP)
                        .setOverlayState(RenderType.NO_OVERLAY)
                        .createCompositeState(false));
    }
}
