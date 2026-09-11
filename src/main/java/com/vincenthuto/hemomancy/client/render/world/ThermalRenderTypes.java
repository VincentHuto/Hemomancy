package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.client.render.shader.ExtendedShaderInstance;
import com.vincenthuto.hemomancy.client.render.shader.ShaderHolder;
import com.vincenthuto.hemomancy.common.init.ShaderInit;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public final class ThermalRenderTypes {
    private static float time;
    static final RenderType FLAME=create("thermal_flame",ShaderInit.MANIPULATION_FLAME,false,false);
    static final RenderType MOLTEN=create("thermal_molten",ShaderInit.MANIPULATION_MOLTEN,false,false);
    static final RenderType GLASS=create("thermal_glass",ShaderInit.MANIPULATION_GLASS,false,false);
    static final RenderType CRUOR=create("thermal_cruor",ShaderInit.MANIPULATION_CRUOR,false,false);
    static final RenderType CORE=create("thermal_cruor_core",ShaderInit.MANIPULATION_CRUOR,true,false);
    public static final RenderType SKIN=create("thermal_skin",ShaderInit.MANIPULATION_SKIN,false,true);
    private ThermalRenderTypes() {}

    public static void begin(double ticks) { time=(float)(ticks%24000); }
    static RenderType material(LuxUmbraBatch.Material material) {
        return switch(material) {
            case LUX -> LuxUmbraRenderTypes.LUX;
            case UMBRA -> LuxUmbraRenderTypes.UMBRA;
            case FLAME -> FLAME;
            case MOLTEN -> MOLTEN;
            case GLASS -> GLASS;
            case CRUOR -> CRUOR;
            case DUCTILIS -> FerricDuctilisRenderTypes.NERVES;
            case ANIMUS -> AnimusMortemRenderTypes.ANIMUS;
            case MORTEM -> AnimusMortemRenderTypes.MORTEM;
        };
    }
    private static RenderType create(String name,ShaderHolder holder,boolean solid,boolean entity) {
        var uniforms=new RenderStateShard.TexturingStateShard(name+"_uniforms",()->{
            var shader=holder.getInstance().get();
            if(shader!=null) {
                shader.safeGetUniform("HemoTime").set(time);
                shader.safeGetUniform("ColorModulator").set(1f,1f,1f,1f);
            }
        },()->{
            if(holder.getInstance().get() instanceof ExtendedShaderInstance shader)shader.setUniformDefaults();
        });
        return RenderType.create(name,entity?DefaultVertexFormat.NEW_ENTITY:DefaultVertexFormat.POSITION_TEX_COLOR,
                VertexFormat.Mode.QUADS,16384,false,!solid,RenderType.CompositeState.builder()
                .setShaderState(holder.getShard()).setTexturingState(uniforms)
                .setTransparencyState(solid?RenderType.NO_TRANSPARENCY:RenderType.TRANSLUCENT_TRANSPARENCY)
                .setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
                .setWriteMaskState(solid?RenderType.COLOR_DEPTH_WRITE:RenderType.COLOR_WRITE)
                .setCullState(RenderType.NO_CULL).setLightmapState(RenderType.NO_LIGHTMAP)
                .createCompositeState(false));
    }
}
