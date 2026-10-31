package com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BorrowedBloodReserve;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.BloodTendencyEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumBloodFlow;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.HematicFortificationRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.VascularSystemEvents;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.*;
import com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class MuscleMemoryActivationService {
    private MuscleMemoryActivationService() {}

    public static boolean tryTrigger(ServerPlayer player, MuscleMemory memory, int activeTicks) {
        return tryTriggerDetailed(player, memory, activeTicks, activeTicks, false).accepted();
    }

    public static boolean tryTriggerSecondPulse(ServerPlayer player) {
        return tryTriggerSecondPulseDetailed(player).accepted();
    }

    public static TriggerResult tryTriggerDetailed(ServerPlayer player, MuscleMemory memory,
            int normalActiveTicks, int overexertedActiveTicks) {
        return tryTriggerDetailed(player, memory, normalActiveTicks, overexertedActiveTicks, false);
    }

    public static TriggerResult tryTriggerSecondPulseDetailed(ServerPlayer player) {
        return tryTriggerDetailed(player, MuscleMemory.SECOND_PULSE, 0, 0, true);
    }

    private static TriggerResult tryTriggerDetailed(ServerPlayer player, MuscleMemory memory,
            int normalActiveTicks, int overexertedActiveTicks, boolean allowBorrowed) {
        long now = player.level().getGameTime();
        MuscleMemoryState state = HemoCapabilityAccess.muscleMemory(player);
        boolean deadSection = VascularSystemEvents.isSectionDead(player, memory.section());
        if (!state.isEnabled(memory) || deadSection) return TriggerResult.REJECTED;
        MuscleMemoryResonanceRules.Resonance resonance = resonance(player, memory);
        double cost = memory.bloodCost() * resonance.costMultiplier();
        boolean purse = allowBorrowed && hasSignatureMorphling(player, MorphlingStrains.MERMAIDS_PURSE);
        boolean armed = state.isOverexertionArmed(memory, now);
        double maximumCost = armed ? cost * MuscleMemoryPrimingRules.OVEREXERT_BLOOD_MULTIPLIER : cost;
        double borrowedAvailable = purse ? Math.min(maximumCost * .5D, BorrowedBloodReserve.get(player)) : 0D;
        double available = HemoCapabilityAccess.getBloodVolume(player).getBloodVolume() + borrowedAvailable;
        MuscleMemoryActivationRules.Result eligibility = MuscleMemoryActivationRules.evaluate(
                true, state.isCooldownReady(memory, now), available,
                memory.bloodCost(), memory.vascularStrain(), resonance);
        if (!eligibility.accepted()) return TriggerResult.REJECTED;

        MuscleMemoryOverexertionRules.Payment payment = MuscleMemoryOverexertionRules.resolve(
                armed, available, eligibility.bloodCost(), eligibility.strain());
        double borrowed = purse ? BorrowedBloodReserve.drainToCover(player,
                Math.min(payment.bloodCost() * .5D, borrowedAvailable)) : 0D;
        double bloodCost = payment.bloodCost() - borrowed;
        boolean paid = bloodCost <= .000001D || BloodAccounting.payUpkeep(player, bloodCost,
                new BloodAccounting.Source("muscle_memory_" + memory.id(), "muscle_memory.hemomancy." + memory.id(),
                        Category.MUSCLE_MEMORY,
                        false, true), 1, true).satisfied();
        if (!paid) {
            if (borrowed > 0D) BorrowedBloodReserve.deposit(player, borrowed);
            return TriggerResult.REJECTED;
        }
        if (MuscleMemoryActivationRules.shouldConsumeArmedUse(armed, true)) {
            state.consumeOverexertion(memory, now);
        }

        if (HemoServerConfig.VASCULAR_DEGRADATION_ON_MANIP_ENABLED.get()) {
            var vascular = HemoCapabilityAccess.getVascularSystem(player);
            EnumBloodFlow before = vascular.getBloodFlowBySection(memory.section());
            boolean fortified = HemoCapabilityAccess.getInitiatoryDegree(player).hasHematicFortification();
            vascular.setVascularSectionHealth(memory.section(),
                    -HematicFortificationRules.adjustedStrain(payment.strain(), fortified));
            VascularSystemEvents.syncVascular(player, vascular);
            VeinMasonAssignments.onThelemicStrain(player, before,
                    vascular.getBloodFlowBySection(memory.section()));
        }
        var tendency = HemoCapabilityAccess.getBloodTendency(player);
        tendency.addTendencyAlignment(memory.primaryTendency(), .075F);
        tendency.addTendencyAlignment(memory.secondaryTendency(), .025F);
        BloodTendencyEvents.syncTendency(player, tendency);
        state.setCooldownUntil(memory, now + memory.cooldownTicks());
        int activeTicks = payment.overexerted() ? overexertedActiveTicks : normalActiveTicks;
        if (activeTicks > 0) state.setActiveUntil(memory, now + activeTicks);
        if (payment.overexerted() && activeTicks > 0) state.setEmpoweredUntil(memory, now + activeTicks);
        if (payment.fellBack()) player.displayClientMessage(Component.translatable(
                "message.hemomancy.muscle_memory.overexertion_fallback").withStyle(ChatFormatting.RED), true);
        MuscleMemoryEvents.sync(player);
        return new TriggerResult(true, payment.overexerted(), payment.fellBack());
    }

    public static boolean hasSignatureMorphling(ServerPlayer player, MorphlingStrain strain) {
        return MorphlingStrains.is(HemoCapabilityAccess.getEquippedMorphling(player).getEquippedMorphling(), strain);
    }

    private static MuscleMemoryResonanceRules.Resonance resonance(ServerPlayer player, MuscleMemory memory) {
        ItemStack stack = HemoCapabilityAccess.getEquippedMorphling(player).getEquippedMorphling();
        if (stack.getItem() instanceof IMorphling morphling) {
            return MuscleMemoryResonanceRules.resolve(memory, morphling.getPreferredTendency(), morphling.getSecondaryTendency(), isSignature(memory, stack));
        }
        return MuscleMemoryResonanceRules.Resonance.NONE;
    }

    /** The house strain whose body each memory echoes; it lowers that memory's cost. */
    static MorphlingStrain signatureStrain(MuscleMemory memory) {
        return switch (memory) {
            case SANGUINE_FISTS -> MorphlingStrains.KINBUD;
            case LABORING_ARMS -> MorphlingStrains.HOLDFAST;
            case COURSING_LEGS, SECOND_PULSE -> MorphlingStrains.MERMAIDS_PURSE;
            case HUSHED_GAIT -> MorphlingStrains.INK_CAP;
            case PREDATORY_EYES -> MorphlingStrains.DEAD_MANS_ROPE;
            case ENDURING_VISCERA -> MorphlingStrains.HUSHBELL;
            case CARRION_METABOLISM -> MorphlingStrains.MARROWPLUME;
        };
    }

    private static boolean isSignature(MuscleMemory memory, ItemStack stack) {
        return MorphlingStrains.is(stack, signatureStrain(memory));
    }


    public record TriggerResult(boolean accepted, boolean overexerted, boolean fellBack) {
        public static final TriggerResult REJECTED = new TriggerResult(false, false, false);
    }
}
