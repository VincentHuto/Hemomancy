package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.client.registry.KeyMappings;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.MemoryEntryKind;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.UseManipKeyPacket;
import net.minecraft.client.Minecraft;

/**
 * The held cast key: charge, continuous and quick casts for the selected manipulation, ticked once
 * per client tick by ClientInputEvents.
 */
public final class ManipulationChargeInput {

    private ManipulationChargeInput() {
    }

    private static int manipulationChargeTicks;
    private static String lastManipulationName = "";
    private static EnumManipulationType lastManipulationType;
    private static boolean suppressManipulationUntilRelease;

    static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            manipulationChargeTicks = 0;
            lastManipulationName = "";
            lastManipulationType = null;
            suppressManipulationUntilRelease = false;
            return;
        }
        if (com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis.blocksActions(mc.player)) {
            interruptManipulationCharge();
            return;
        }
        var known = HemoCapabilityAccess.getKnownManipulations(mc.player);
                    var memory = known.getSelectedMemoryRef();
                    boolean thelemic = memory.kind() == MemoryEntryKind.MUSCLE_MEMORY;
                    var selected = thelemic ? null : ManipulationInit.getByName(memory.id());
                    if (selected == null && !thelemic) {
                        stopCastingChargePresentation();
                        if (lastManipulationType == EnumManipulationType.CONTINUOUS && manipulationChargeTicks > 0) {
                            PacketHandler.sendToServer(UseManipKeyPacket.stopContinuous());
                        }
                        manipulationChargeTicks = 0;
                        lastManipulationName = "";
                        lastManipulationType = null;
                        return;
                    }
                    boolean down = KeyMappings.useManip.isDown();
                    boolean clicked = KeyMappings.useManip.consumeClick();
                    var type = thelemic ? EnumManipulationType.PASSIVE : selected.getType();
                    if (!memory.storageKey().equals(lastManipulationName)) {
                        stopCastingChargePresentation();
                        if (lastManipulationType == EnumManipulationType.CONTINUOUS && manipulationChargeTicks > 0) {
                            PacketHandler.sendToServer(UseManipKeyPacket.stopContinuous());
                        }
                        manipulationChargeTicks = 0;
                        lastManipulationName = memory.storageKey();
                        lastManipulationType = type;
                        suppressManipulationUntilRelease = down && !clicked;
                    }
                    if (suppressManipulationUntilRelease) {
                        if (down) return;
                        suppressManipulationUntilRelease = false;
                    }
                    if (type == EnumManipulationType.CHARGED && down
                            && mc.player.hurtTime > 0 && mc.player.hurtTime == mc.player.hurtDuration
                            && manipulationChargeTicks > 0) {
                        manipulationChargeTicks = com.vincenthuto.hemomancy.common.capability.player.shared.skill.BodyRefinementSkillRules
                                .retainedChargeTicks(manipulationChargeTicks,
                                        SkillPointHelper.getNervesOfSteelLevel(mc.player));
                    }
                    var input = ManipulationInputRules.tick(type, down, clicked,
                            manipulationChargeTicks, thelemic ? 0 : selected.getRequiredChargeTicks());
                    manipulationChargeTicks = input.nextHeldTicks();
                    if (type == EnumManipulationType.CHARGED
                            && down && input.action() != ManipulationInputRules.Action.CAST
                            && (manipulationChargeTicks == 1 || manipulationChargeTicks % 4 == 0)) {
                        PacketHandler.sendToServer(new com.vincenthuto.hemomancy.common.network.CastingChargePacket(
                                selected.getName(), manipulationChargeTicks));
                    }
                    if (type == EnumManipulationType.CHARGED
                            && (down || input.action() == ManipulationInputRules.Action.CAST)
                            && com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.chargeForm(selected.getName()) != null
                            && (manipulationChargeTicks % 4 == 0 || input.action() == ManipulationInputRules.Action.CAST)) {
                        PacketHandler.sendToServer(new com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.ManipulationChargeVisualPacket(
                                input.action() == ManipulationInputRules.Action.CAST ? 0 : manipulationChargeTicks));
                    }
                    switch (input.action()) {
                        case NONE -> {
                        }
                        case START_CONTINUOUS -> PacketHandler.sendToServer(UseManipKeyPacket.startContinuous());
                        case STOP_CONTINUOUS -> PacketHandler.sendToServer(UseManipKeyPacket.stopContinuous());
                        case CAST -> PacketHandler.sendToServer(new UseManipKeyPacket(input.castTicks()));
                    }
    }

    public static void interruptManipulationCharge() {
        stopCastingChargePresentation();
        if (manipulationChargeTicks > 0) {
            if (lastManipulationType == EnumManipulationType.CONTINUOUS) {
                PacketHandler.sendToServer(UseManipKeyPacket.stopContinuous());
            }
        }
        manipulationChargeTicks = 0;
        suppressManipulationUntilRelease = KeyMappings.useManip.isDown();
        while (KeyMappings.useManip.consumeClick()) { }
    }

    public static int getManipulationChargeTicks() {
        return manipulationChargeTicks;
    }

    private static void stopCastingChargePresentation() {
        if (lastManipulationType == EnumManipulationType.CHARGED && manipulationChargeTicks > 0) {
            PacketHandler.sendToServer(new com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.ManipulationChargeVisualPacket(0));
            PacketHandler.sendToServer(new com.vincenthuto.hemomancy.common.network.CastingChargePacket(lastManipulationName, 0));
        }
    }

    public static void manipulationCastAccepted() {
        var manipulation = ManipulationInit.getByName(lastManipulationName);
        if (lastManipulationType == EnumManipulationType.CHARGED && manipulation != null
                && manipulationChargeTicks >= manipulation.getRequiredChargeTicks()) {
            manipulationChargeTicks = 0;
        }
    }

    public static String getChargingManipulationName() {
        return lastManipulationType == EnumManipulationType.CHARGED && manipulationChargeTicks > 0
                ? lastManipulationName : "";
    }

    public static float getManipulationChargeProgress() {
        var manipulation = ManipulationInit.getByName(getChargingManipulationName());
        return manipulation == null ? 0.0F
                : com.vincenthuto.hemomancy.common.manipulation.ManipulationCastingRules.chargeFraction(
                        manipulationChargeTicks, manipulation.getRequiredChargeTicks());
    }
}
