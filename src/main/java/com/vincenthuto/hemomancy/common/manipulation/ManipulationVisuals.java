package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** Server-authored geometry and lifetime; these cues never apply gameplay. */
public final class ManipulationVisuals {
    private ManipulationVisuals() {}

    public enum Form {
        VERDICT, GLASS, THREAD, CROWN, SWORD_IMPACT, WELL, STILLNESS, BEACON,
        PHOENIX, BELL, DRAIN, SUTURE, WARD, CHOIR, FURNACE, MARK, EYE, WOUND,
        FORGE, MENDING, GROWTH, UPDRAFT, TELEPORT, BONE, ICE, FLARE, DEBT, VEIL,
        VERDICT_CHARGE, GLASS_CHARGE, CROWN_CHARGE, BELL_CHARGE, THREAD_CHARGE, ORE, HOUR,
        BLOOM, CLOUD, MAGNET, RETORT, HUNGER, GRAVE, COMMAND, CIRCUIT, CAUTERIZE, RUSH, IRON_HEART, BLACK_HEART, PHOENIX_READY, RUPTURE,
        NEEDLE_CHARGE, FAN_CHARGE, LANCE_CHARGE, MORTAR_CHARGE, GAZE_CHARGE,
        ANEURYSM_CHARGE, ICE_CHARGE, WELL_CHARGE, LIGHTNING_CHARGE, IRON_CHARGE,
        LUX_MENDING, UMBRA_SLASH, LUX_MIST, UMBRA_MIST, UMBRA_ARRIVAL, WHITE_VERDICT,
        IGNITION, CRYOGENIC_PULSE, CRUOR_FORM, CRUOR_BREAK, FROZEN_VEINS, RIMEBOUND,
        FLAME_CONJURE, FROST_CONJURE, HOUR_BREAK, CRUOR_SURFACE, FROST_ADVANCE,
        FERRIC_IMPACT, FERRIC_CONJURE, NERVE_PULSE, NERVE_HIT, PARALYSIS,
        ANIMUS_IMPACT, ANIMUS_CONJURE, MORTEM_CONJURE, MORTEM_BURST, GRAVE_REFUND, HUNGER_COLLAPSE,
        BLACKHEART_RUPTURE, TITHE_RETURN, TITHE_COLLECT, ROT_INFECTION, COMMUNION, MARIONETTE_TETHER, MARIONETTE_ORDER,
        BOMBARDIER_SWEEP, MNEMONIC_REPLAY, RIPOSTE, RIPOSTE_STRIKE, BLOOD_SPLASH, GAZE_GRIP, GAZE_SLAM,
        ANEURYSM_RUPTURE, SOVEREIGN_LASH,
        // Encoded by ordinal: append new forms here.
        PUPPET_DESCEND, PUPPET_UNSPOOL, UMBRAL_REND, LODESTONE_COLLAPSE,
        SEARING_LASH, CRUOR_JAVELIN, RADIANT_WAKE, CONTAGION, NERVE_COMMAND, LODESTONE_FIELD,
        VESPER_RECALL, CAST_IMPACT, WEAPON_CONJURE, CELL_HAND, AVATAR_MANIFEST
    }

    public static void burst(ServerLevel level, Form form, Vec3 from, Vec3 to, double radius, int ticks) {
        // For UMBRA_SLASH, the form-specific count field carries the shared roll in degrees.
        int count = form == Form.UMBRA_SLASH ? level.random.nextInt(360) : 1;
        send(level, new ManipulationVisualPacket(form, -1, from, to, (float) radius, ticks, count));
    }

    /** A burst whose form-specific {@code count} carries extra detail, such as RIPOSTE_STRIKE's melee/projectile variant. */
    public static void burst(ServerLevel level, Form form, Vec3 from, Vec3 to, double radius, int ticks, int count) {
        send(level, new ManipulationVisualPacket(form, -1, from, to, (float) radius, ticks, count));
    }

    /** An attached cue that also carries a world point, such as the eyes a gaze tether runs back to. */
    public static void attached(Entity entity, Form form, Vec3 to, double radius, int ticks, int count) {
        if (entity.level() instanceof ServerLevel level)
            send(level, new ManipulationVisualPacket(form, entity.getId(), entity.position(), to, (float) radius, ticks, count));
    }

    public static void attached(Entity entity, Form form, double radius, int ticks, int count) {
        if (entity.level() instanceof ServerLevel level) {
            ThermalStatusVisuals.track(entity, form, radius, ticks, count);
            FerricDuctilisStatusVisuals.track(entity, form, radius, ticks, count);
            send(level, new ManipulationVisualPacket(form, entity.getId(), entity.position(),
                    entity.position(), (float) radius, ticks, count));
        }
    }

    /** Restarts an entity's keyed cue, so a recast replays its whole timeline instead of refreshing a dying one. */
    public static void replay(Entity entity, Form form, double radius, int ticks, int count) {
        attached(entity, form, 0, 0, 0);
        attached(entity, form, radius, ticks, count);
    }

    private static void send(ServerLevel level, ManipulationVisualPacket packet) {
        // Include observers at the far end of long beams, not only near the caster.
        Vec3 center = packet.from().lerp(packet.to(), .5);
        PacketHandler.sendNear(level, center.x, center.y, center.z,
                64 + packet.from().distanceTo(packet.to()) / 2, packet);
    }

    public static Vec3 swordOffset(double time, int slot) {
        double angle = time * .025 + slot * Math.PI / 4;
        return new Vec3(Math.cos(angle) * 1.15, 3.05 + Math.sin(time * .07 + slot) * .08,
                Math.sin(angle) * 1.15);
    }

    public static Form chargeForm(String name) {
        return switch (name) {
            case "white_verdict" -> Form.VERDICT_CHARGE;
            case "vitric_combustion" -> Form.GLASS_CHARGE;
            case "crimson_coronation" -> Form.CROWN_CHARGE;
            case "funeral_bell" -> Form.BELL_CHARGE;
            case "thread_ripper" -> Form.THREAD_CHARGE;
            case "blood_needle" -> Form.NEEDLE_CHARGE;
            case "blood_needle_fan" -> Form.FAN_CHARGE;
            case "blood_needle_lance" -> Form.LANCE_CHARGE;
            case "hematic_mortar" -> Form.MORTAR_CHARGE;
            case "deadly_gaze" -> Form.GAZE_CHARGE;
            case "blood_aneurysm" -> Form.ANEURYSM_CHARGE;
            case "rimebound_sentence" -> Form.ICE_CHARGE;
            case "eclipse_well" -> Form.WELL_CHARGE;
            case "activation_potential", "synaptic_storm" -> Form.LIGHTNING_CHARGE;
            case "ironhearted", "lodestone_collapse" -> Form.IRON_CHARGE;
            default -> null;
        };
    }

    public static void endChannel(Entity player, String name) {
        if (player instanceof net.minecraft.world.entity.LivingEntity living && name.equals("carrion_communion"))
            BloodFlowVisuals.stop(living, com.vincenthuto.hemomancy.common.network.particle.BloodFlowPacket.Style.COMMUNION);
        Form form = switch (name) {
            case "absolute_stillness" -> Form.STILLNESS;
            case "furnace_veins" -> Form.FURNACE;
            case "iron_choir" -> Form.CHOIR;
            case "sanguine_ward" -> Form.WARD;
            case "penumbral_drift" -> Form.VEIL;
            case "radiant_wake" -> Form.RADIANT_WAKE;
            default -> null;
        };
        if (form != null) attached(player, form, 0, 0, 0);
    }
}
