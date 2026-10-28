package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.client.render.SchoolRenderTypes;
import com.vincenthuto.hemomancy.client.render.shader.ShaderHolder;
import com.vincenthuto.hemomancy.client.render.shader.ShaderInit;
import net.minecraft.client.renderer.RenderType;

public final class AnimusMortemRenderTypes {
    private static final SchoolRenderTypes.Clock CLOCK = new SchoolRenderTypes.Clock();
    public static final RenderType ANIMUS=create("living_blood",ShaderInit.MANIPULATION_ANIMUS,false);
    public static final RenderType MORTEM=create("blood_decay",ShaderInit.MANIPULATION_MORTEM,false);
    public static final RenderType PUTRICIEL_ROT=create("putriciel_rot",ShaderInit.MANIPULATION_PUTRICIEL_ROT,false);
    public static final RenderType INFECTION=create("blood_infection",ShaderInit.MANIPULATION_INFECTION,true);
    public static final RenderType AVATAR=create("living_blood_avatar",ShaderInit.MANIPULATION_ANIMUS_MODEL,true);
    /** Living blood that writes depth, for streams fixed in the world: translucent water drawn later then sorts behind it. */
    public static final RenderType ANIMUS_DEPTH=create("living_blood_depth",ShaderInit.MANIPULATION_ANIMUS,false,true);
    private AnimusMortemRenderTypes() {}
    public static void begin(double ticks){CLOCK.begin(ticks);}
    private static RenderType create(String name,ShaderHolder holder,boolean entity) {
        return create(name,holder,entity,false);
    }
    private static RenderType create(String name,ShaderHolder holder,boolean entity,boolean depth) {
        VertexFormat format=entity?DefaultVertexFormat.NEW_ENTITY:DefaultVertexFormat.POSITION_TEX_COLOR;
        return SchoolRenderTypes.create(CLOCK,name,holder,format,false,depth,1f);
    }
}
