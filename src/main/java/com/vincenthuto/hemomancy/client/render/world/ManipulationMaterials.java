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
            case BELL, BELL_CHARGE, DRAIN, WOUND, HUNGER, GRAVE, BLACK_HEART,
                    MORTEM_CONJURE, MORTEM_BURST, GRAVE_REFUND, HUNGER_COLLAPSE, BLACKHEART_RUPTURE,
                    ROT_INFECTION, COMMUNION, CONTAGION -> MORTEM;
            case THREAD, THREAD_CHARGE, MARK, CIRCUIT, LIGHTNING_CHARGE, WARD, NERVE_PULSE, NERVE_HIT, PARALYSIS,
                    GAZE_CHARGE, GAZE_GRIP, GAZE_SLAM, NERVE_COMMAND -> DUCTILIS;
            case CHOIR, RETORT, MENDING, MAGNET, ORE, IRON_HEART, IRON_CHARGE, FERRIC_IMPACT, FERRIC_CONJURE, LODESTONE_COLLAPSE, LODESTONE_FIELD -> FERRIC;
            case GLASS, GLASS_CHARGE, FURNACE, FORGE, PHOENIX, PHOENIX_READY, CAUTERIZE, UPDRAFT,
                    IGNITION, FLAME_CONJURE, BOMBARDIER_SWEEP, SEARING_LASH -> FLAMMEUS;
            case STILLNESS, BONE, ICE, ICE_CHARGE, CRYOGENIC_PULSE,CRUOR_FORM,CRUOR_BREAK,
                    FROZEN_VEINS,RIMEBOUND,FROST_CONJURE,CRUOR_SURFACE,FROST_ADVANCE,CRUOR_JAVELIN -> CONGEATIO;
            case VERDICT, WHITE_VERDICT, VERDICT_CHARGE, BEACON, SUTURE, FLARE, LUX_MENDING, LUX_MIST, RADIANT_WAKE, SERAPHAE_RELIQUARY -> LUX;
            case WELL, WELL_CHARGE, VEIL, TELEPORT, UMBRA_ARRIVAL, UMBRA_SLASH, UMBRA_MIST, UMBRAL_REND, UMBRAL_FIELD -> TENEBRIS;
            case CROWN, CROWN_CHARGE, SWORD_IMPACT, CLOUD, GROWTH, COMMAND, RUSH, RUPTURE,
                    NEEDLE_CHARGE, FAN_CHARGE, LANCE_CHARGE, MORTAR_CHARGE,
                    ANEURYSM_CHARGE, ANIMUS_IMPACT, ANIMUS_CONJURE,
                    MARIONETTE_TETHER, MARIONETTE_ORDER, MNEMONIC_REPLAY, RIPOSTE, RIPOSTE_STRIKE -> ANIMUS;
            case BLOOD_SPLASH, ANEURYSM_RUPTURE, SOVEREIGN_LASH, PUPPET_DESCEND, PUPPET_UNSPOOL -> ANIMUS;
            case CHUM_BLOOM, SCAB_DRIP -> ANIMUS;
            case STRAND, STRAND_ACCENT -> ANIMUS;
            case VESPER_RECALL, CAST_IMPACT, WEAPON_CONJURE, CELL_HAND, AVATAR_MANIFEST -> ANIMUS;
            case HEMORATH_RELIQUARY, PUTRICIEL_OFFERING -> MORTEM;
            case VELORUM_RIME -> CONGEATIO;
            case TITHE_CHAIN, TITHE_VESSEL, TITHE_RELEASE -> ANIMUS;
            case EYE_CHARGE, EYE_BEAM, EYE_APERTURE, EYE_GROWTH -> LUX;
            case BLOOM_FIELD, BLOOM_IGNITION, BLOOM_COLONY -> MORTEM;
            case HOUR_FIELD, HOUR_PRESERVE, HOUR_STILL -> CONGEATIO;
        };
    }
}
