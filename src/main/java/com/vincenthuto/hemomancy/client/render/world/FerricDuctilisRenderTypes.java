package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.client.render.shader.ExtendedShaderInstance;
import com.vincenthuto.hemomancy.client.render.shader.ShaderHolder;
import com.vincenthuto.hemomancy.common.init.ShaderInit;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

final class FerricDuctilisRenderTypes {
    private static float time;
    static final RenderType IRON=create("ferric_plate",ShaderInit.MANIPULATION_FERRIC,true);
    static final RenderType NERVES=create("ductilis_impulse",ShaderInit.MANIPULATION_DUCTILIS,false);
    private FerricDuctilisRenderTypes() {}
    static void begin(double ticks){time=(float)(ticks%24000);}
    private static RenderType create(String name,ShaderHolder holder,boolean solid) {
        var uniforms=new RenderStateShard.TexturingStateShard(name+"_uniforms",()->{
            var shader=holder.getInstance().get();
            if(shader!=null){shader.safeGetUniform("HemoTime").set(time);shader.safeGetUniform("ColorModulator").set(1f,1f,1f,1f);}
        },()->{if(holder.getInstance().get() instanceof ExtendedShaderInstance shader)shader.setUniformDefaults();});
        return RenderType.create(name,DefaultVertexFormat.POSITION_TEX_COLOR,VertexFormat.Mode.QUADS,16384,false,!solid,
                RenderType.CompositeState.builder().setShaderState(holder.getShard()).setTexturingState(uniforms)
                        .setTransparencyState(solid?RenderType.NO_TRANSPARENCY:RenderType.TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(RenderType.LEQUAL_DEPTH_TEST).setWriteMaskState(solid?RenderType.COLOR_DEPTH_WRITE:RenderType.COLOR_WRITE)
                        .setCullState(RenderType.NO_CULL).setLightmapState(RenderType.NO_LIGHTMAP).createCompositeState(false));
    }
}
