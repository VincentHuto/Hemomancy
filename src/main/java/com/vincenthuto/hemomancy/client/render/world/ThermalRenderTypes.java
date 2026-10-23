package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.vincenthuto.hemomancy.client.render.SchoolRenderTypes;
import com.vincenthuto.hemomancy.client.render.shader.ShaderHolder;
import com.vincenthuto.hemomancy.client.render.shader.ShaderInit;
import net.minecraft.client.renderer.RenderType;

public final class ThermalRenderTypes {
    private static final SchoolRenderTypes.Clock CLOCK = new SchoolRenderTypes.Clock();
    static final RenderType FLAME=create("thermal_flame",ShaderInit.MANIPULATION_FLAME,false,false);
    /** The same fire burning at a steadier pace, for standing flames such as the Iron Brazier's. */
    static final RenderType STEADY_FLAME=create("thermal_flame_steady",ShaderInit.MANIPULATION_FLAME,false,false,.45f);
    static final RenderType MOLTEN=create("thermal_molten",ShaderInit.MANIPULATION_MOLTEN,false,false);
    static final RenderType GLASS=create("thermal_glass",ShaderInit.MANIPULATION_GLASS,false,false);
    static final RenderType CRUOR=create("thermal_cruor",ShaderInit.MANIPULATION_CRUOR,false,false);
    static final RenderType CORE=create("thermal_cruor_core",ShaderInit.MANIPULATION_CRUOR,true,false);
    public static final RenderType SKIN=create("thermal_skin",ShaderInit.MANIPULATION_SKIN,false,true);
    private ThermalRenderTypes() {}

    public static void begin(double ticks) { CLOCK.begin(ticks); }
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
        return create(name,holder,solid,entity,1f);
    }
    private static RenderType create(String name,ShaderHolder holder,boolean solid,boolean entity,float pace) {
        return SchoolRenderTypes.create(CLOCK,name,holder,entity?DefaultVertexFormat.NEW_ENTITY:DefaultVertexFormat.POSITION_TEX_COLOR,
                solid,false,pace);
    }
}
