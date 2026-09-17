package com.vincenthuto.hemomancy.common.manipulation.animation;

import static com.vincenthuto.hemomancy.common.manipulation.animation.CastPurpose.*;

/** Presentation metadata is resolved from registry identity, never written into learned-memory NBT. */
public record CastPresentation(CastPurpose purpose, CastStyle style) {
    public static boolean hasExplicit(String id) { return purposeFor(id) != null; }
    public static CastPresentation forId(String id) {
        var purpose = purposeFor(id);
        return new CastPresentation(purpose == null ? TARGETED : purpose, switch(id) {
            case "ironhearted" -> CastStyle.IRONHEART;
            case "umbral_step" -> CastStyle.UMBRAL_STEP;
            case "umbral_reversal" -> CastStyle.UMBRAL_REVERSAL;
            case "summon_avatar", "summon_avatar_arms", "summon_avatar_armor", "summon_avatar_legs",
                    "summon_avatar_complete" -> CastStyle.AVATAR;
            case "conjure_staff", "conjure_blade", "conjure_axe", "conjure_spear", "conjure_claws",
                    "conjure_crossbow", "conjure_torch", "conjure_flail", "conjure_sickle" -> CastStyle.STAFF;
            case "crimson_tithe" -> CastStyle.CRIMSON_TITHE;
            case "unclosing_eye" -> CastStyle.UNCLOSING_EYE;
            case "bloom_of_rot" -> CastStyle.BLOOM_OF_ROT;
            case "endless_hour" -> CastStyle.ENDLESS_HOUR;
            default -> CastStyle.SCHOOL;
        });
    }
    private static CastPurpose purposeFor(String id) {
        return switch(id) {
            case "blood_shot", "guided_blood_shot", "hematic_mortar", "blood_needle", "blood_needle_fan",
                    "blood_needle_lance", "blood_projection", "synaptic_jolt", "gloam_laceration",
                    "white_verdict", "thread_ripper" -> PROJECTILE;
            case "blood_rush", "summon_avatar", "summon_avatar_arms", "summon_avatar_armor", "summon_avatar_legs",
                    "summon_avatar_complete", "iron_retort", "ironhearted", "void_shroud", "black_veil_covenant",
                    "insatiable_hunger", "grave_debt", "blackhearted", "sovereign_instinct", "phoenix_debt",
                    "living_circuit", "furnace_veins", "crimson_tithe", "endless_hour" -> SELF;
            case "sanguine_halo", "blood_cloud", "expansive_blood_cloud", "pursuing_blood_cloud", "sanguine_tempest",
                    "blood_aneurysm", "vital_effusion", "activation_potential", "sanguine_ward", "hemolymphal_pulse",
                    "hematic_flare", "hematic_beacon", "cryogenic_pulse", "osseous_bloom", "vitric_combustion",
                    "cauterizing_rebuke", "blood_eclipse", "crimson_coronation", "synaptic_storm",
                    "absolute_stillness", "iron_choir", "funeral_bell", "carrion_communion", "eclipse_well",
                    "unclosing_eye", "bloom_of_rot" -> AREA;
            case "blood_binding", "lingering_blood_binding", "chain_blood_binding", "sanguine_marionette",
                    "blood_lattice", "deadly_gaze", "hematic_rebuke", "hematic_impressment", "conductive_mark",
                    "blood_absorption", "sanguine_mending", "glacial_grasp", "vascular_dowsing", "pyretic_forge",
                    "prismatic_reproof", "lumen_suture", "sanguine_ignition", "hemorrhage", "lignum_mortis",
                    "canopy_mortis", "worked_lignum", "exsanguinate", "rimebound_sentence" -> TARGETED;
            case "sanguine_magnetism", "ferric_rampart", "ferric_spikes", "glacial_rampart" -> CONSTRUCTION;
            case "conjure_blade", "conjure_axe", "conjure_spear", "conjure_claws", "conjure_crossbow",
                    "conjure_torch", "conjure_flail", "conjure_sickle", "conjure_staff", "crimson_flame_conjuration" -> CONJURATION;
            case "umbral_step", "umbral_reversal", "penumbral_drift", "scalding_updraft", "soaring_updraft",
                    "suspended_updraft", "expulsive_updraft" -> TRAVEL;
            default -> null;
        };
    }
}
