package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

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
        BLACKHEART_RUPTURE, TITHE_RETURN, TITHE_COLLECT, ROT_INFECTION, COMMUNION
    }

    public static void burst(ServerLevel level, Form form, Vec3 from, Vec3 to, double radius, int ticks) {
        send(level, new ManipulationVisualPacket(form, -1, from, to, (float) radius, ticks, 1));
    }

    public static void attached(Entity entity, Form form, double radius, int ticks, int count) {
        if (entity.level() instanceof ServerLevel level) {
            ThermalStatusVisuals.track(entity, form, radius, ticks, count);
            FerricDuctilisStatusVisuals.track(entity, form, radius, ticks, count);
            send(level, new ManipulationVisualPacket(form, entity.getId(), entity.position(),
                    entity.position(), (float) radius, ticks, count));
        }
    }

    private static void send(ServerLevel level, ManipulationVisualPacket packet) {
        // Include observers at the far end of long beams, not only near the caster.
        Vec3 center = packet.from().lerp(packet.to(), .5);
        PacketDistributor.sendToPlayersNear(level, null, center.x, center.y, center.z,
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
            case "ironhearted" -> Form.IRON_CHARGE;
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
            default -> null;
        };
        if (form != null) attached(player, form, 0, 0, 0);
    }
}
