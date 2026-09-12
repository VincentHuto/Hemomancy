package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

/** Textured, depth-tested surfaces; school identity is independent of particle settings. */
public enum ManipulationMaterials {
    ANIMUS, MORTEM, DUCTILIS, FERRIC, FLAMMEUS, CONGEATIO, LUX, TENEBRIS;

    private RenderType renderType;

    public RenderType renderType() {
        if (renderType == null) {
            String name = name().toLowerCase(java.util.Locale.ROOT);
            renderType = RenderType.create("manipulation_" + name,
                    DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 8192, false, true,
                    RenderType.CompositeState.builder()
                            .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionTexColorShader))
                            .setTextureState(new RenderStateShard.TextureStateShard(
                                    Hemomancy.rloc("textures/effect/manipulation/" + name + ".png"), false, false))
                            .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                            .setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
                            .setWriteMaskState(RenderType.COLOR_WRITE).setCullState(RenderType.NO_CULL)
                            .createCompositeState(false));
        }
        return renderType;
    }

    public static ManipulationMaterials forForm(Form form) {
        return switch (form) {
            case BELL, BELL_CHARGE, DRAIN, WOUND, HUNGER, GRAVE, BLACK_HEART, BLOOM, DEBT,
                    MORTEM_CONJURE, MORTEM_BURST, GRAVE_REFUND, HUNGER_COLLAPSE, BLACKHEART_RUPTURE,
                    TITHE_COLLECT, ROT_INFECTION, COMMUNION -> MORTEM;
            case THREAD, THREAD_CHARGE, MARK, CIRCUIT, LIGHTNING_CHARGE, WARD, NERVE_PULSE, NERVE_HIT, PARALYSIS -> DUCTILIS;
            case CHOIR, RETORT, MENDING, MAGNET, ORE, IRON_HEART, IRON_CHARGE, FERRIC_IMPACT, FERRIC_CONJURE -> FERRIC;
            case GLASS, GLASS_CHARGE, FURNACE, FORGE, PHOENIX, PHOENIX_READY, CAUTERIZE, UPDRAFT,
                    IGNITION,FLAME_CONJURE -> FLAMMEUS;
            case STILLNESS, BONE, ICE, ICE_CHARGE, HOUR,CRYOGENIC_PULSE,CRUOR_FORM,CRUOR_BREAK,
                    FROZEN_VEINS,RIMEBOUND,FROST_CONJURE,HOUR_BREAK,CRUOR_SURFACE,FROST_ADVANCE -> CONGEATIO;
            case VERDICT, WHITE_VERDICT, VERDICT_CHARGE, BEACON, SUTURE, FLARE, EYE, LUX_MENDING, LUX_MIST -> LUX;
            case WELL, WELL_CHARGE, VEIL, TELEPORT, UMBRA_ARRIVAL, UMBRA_SLASH, UMBRA_MIST -> TENEBRIS;
            case CROWN, CROWN_CHARGE, SWORD_IMPACT, CLOUD, GROWTH, COMMAND, RUSH, RUPTURE,
                    NEEDLE_CHARGE, FAN_CHARGE, LANCE_CHARGE, MORTAR_CHARGE, GAZE_CHARGE,
                    ANEURYSM_CHARGE, ANIMUS_IMPACT, ANIMUS_CONJURE, TITHE_RETURN,
                    MARIONETTE_TETHER, MARIONETTE_ORDER -> ANIMUS;
        };
    }
}
