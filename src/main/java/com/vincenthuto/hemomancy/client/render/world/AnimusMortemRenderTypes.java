package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.client.render.shader.ExtendedShaderInstance;
import com.vincenthuto.hemomancy.client.render.shader.ShaderHolder;
import com.vincenthuto.hemomancy.common.init.ShaderInit;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public final class AnimusMortemRenderTypes {
    private static float time;
    public static final RenderType ANIMUS=create("living_blood",ShaderInit.MANIPULATION_ANIMUS,false);
    public static final RenderType MORTEM=create("blood_decay",ShaderInit.MANIPULATION_MORTEM,false);
    public static final RenderType INFECTION=create("blood_infection",ShaderInit.MANIPULATION_INFECTION,true);
    public static final RenderType AVATAR=create("living_blood_avatar",ShaderInit.MANIPULATION_ANIMUS_MODEL,true);
    private AnimusMortemRenderTypes() {}
    public static void begin(double ticks){time=(float)(ticks%24000);}
    private static RenderType create(String name,ShaderHolder holder,boolean entity) {
        var uniforms=new RenderStateShard.TexturingStateShard(name+"_uniforms",()->{
            var shader=holder.getInstance().get();
            if(shader!=null){shader.safeGetUniform("HemoTime").set(time);shader.safeGetUniform("ColorModulator").set(1f,1f,1f,1f);}
        },()->{if(holder.getInstance().get() instanceof ExtendedShaderInstance shader)shader.setUniformDefaults();});
        return RenderType.create(name,entity?DefaultVertexFormat.NEW_ENTITY:DefaultVertexFormat.POSITION_TEX_COLOR,
                VertexFormat.Mode.QUADS,16384,false,true,RenderType.CompositeState.builder()
                        .setShaderState(holder.getShard()).setTexturingState(uniforms)
                        .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY).setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(RenderType.COLOR_WRITE).setCullState(RenderType.NO_CULL)
                        .setLightmapState(RenderType.NO_LIGHTMAP).createCompositeState(false));
    }
}
