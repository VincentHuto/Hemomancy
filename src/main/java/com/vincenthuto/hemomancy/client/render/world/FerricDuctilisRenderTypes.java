package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.vincenthuto.hemomancy.client.render.SchoolRenderTypes;
import com.vincenthuto.hemomancy.client.render.shader.ShaderInit;
import net.minecraft.client.renderer.RenderType;

final class FerricDuctilisRenderTypes {
    private static final SchoolRenderTypes.Clock CLOCK = new SchoolRenderTypes.Clock();
    static final RenderType IRON=SchoolRenderTypes.create(CLOCK,"ferric_plate",ShaderInit.MANIPULATION_FERRIC,DefaultVertexFormat.POSITION_TEX_COLOR,true,true,1f);
    static final RenderType NERVES=SchoolRenderTypes.create(CLOCK,"ductilis_impulse",ShaderInit.MANIPULATION_DUCTILIS,DefaultVertexFormat.POSITION_TEX_COLOR,false,false,1f);
    private FerricDuctilisRenderTypes() {}
    static void begin(double ticks){CLOCK.begin(ticks);}
}
