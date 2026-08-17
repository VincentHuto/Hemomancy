package com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory;

import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BorrowedBloodReserve;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.BloodTendencyEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.VascularSystemEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumBloodFlow;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.IMorphling;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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
        MuscleMemoryState state = player.getData(HemoAttachmentTypes.MUSCLE_MEMORY);
        boolean deadSection = HemoCapabilityAccess.getVascularSystem(player)
                .map(vascular -> vascular.getBloodFlowBySection(memory.section()) == EnumBloodFlow.DEAD)
                .orElse(false);
        if (!state.isEnabled(memory) || deadSection) return TriggerResult.REJECTED;
        MuscleMemoryResonanceRules.Resonance resonance = resonance(player, memory);
        double cost = memory.bloodCost() * resonance.costMultiplier();
        boolean purse = allowBorrowed && hasSignatureMorphling(player, DeadmansPurseMorphlingItem.class);
        boolean armed = state.isOverexertionArmed(memory, now);
        double maximumCost = armed ? cost * MuscleMemoryPrimingRules.OVEREXERT_BLOOD_MULTIPLIER : cost;
        double borrowedAvailable = purse ? Math.min(maximumCost * .5D, BorrowedBloodReserve.get(player)) : 0D;
        double available = HemoCapabilityAccess.getBloodVolume(player).map(v -> v.getBloodVolume()).orElse(0D) + borrowedAvailable;
        MuscleMemoryActivationRules.Result eligibility = MuscleMemoryActivationRules.evaluate(
                true, state.isCooldownReady(memory, now), available,
                memory.bloodCost(), memory.vascularStrain(), resonance);
        if (!eligibility.accepted()) return TriggerResult.REJECTED;

        MuscleMemoryOverexertionRules.Payment payment = MuscleMemoryOverexertionRules.resolve(
                armed, available, eligibility.bloodCost(), eligibility.strain());
        double borrowed = purse ? BorrowedBloodReserve.drainToCover(player,
                Math.min(payment.bloodCost() * .5D, borrowedAvailable)) : 0D;
        double bloodCost = payment.bloodCost() - borrowed;
        boolean paid = bloodCost <= .000001D || HemoCapabilityAccess.getBloodVolume(player)
                .map(volume -> BloodFlowLedger.applyDrain(player, volume, "muscle_memory_" + memory.id(),
                        displayName(memory), Category.MUSCLE_MEMORY, bloodCost, 1, true).satisfied())
                .orElse(false);
        if (!paid) {
            if (borrowed > 0D) BorrowedBloodReserve.deposit(player, borrowed);
            return TriggerResult.REJECTED;
        }
        if (MuscleMemoryActivationRules.shouldConsumeArmedUse(armed, true)) {
            state.consumeOverexertion(memory, now);
        }

        HemoCapabilityAccess.getVascularSystem(player).ifPresent(vascular -> {
            vascular.setVascularSectionHealth(memory.section(), -payment.strain());
            VascularSystemEvents.syncVascular(player, vascular);
        });
        HemoCapabilityAccess.getBloodTendency(player).ifPresent(tendency -> {
            tendency.addTendencyAlignment(memory.primaryTendency(), .075F);
            tendency.addTendencyAlignment(memory.secondaryTendency(), .025F);
            BloodTendencyEvents.syncTendency(player, tendency);
        });
        state.setCooldownUntil(memory, now + memory.cooldownTicks());
        int activeTicks = payment.overexerted() ? overexertedActiveTicks : normalActiveTicks;
        if (activeTicks > 0) state.setActiveUntil(memory, now + activeTicks);
        if (payment.overexerted() && activeTicks > 0) state.setEmpoweredUntil(memory, now + activeTicks);
        if (payment.fellBack()) player.displayClientMessage(Component.translatable(
                "message.hemomancy.muscle_memory.overexertion_fallback").withStyle(ChatFormatting.RED), true);
        MuscleMemoryEvents.sync(player);
        return new TriggerResult(true, payment.overexerted(), payment.fellBack());
    }

    public static boolean hasSignatureMorphling(ServerPlayer player, Class<?> type) {
        ItemStack stack = HemoCapabilityAccess.getEquippedMorphling(player)
                .map(cap -> cap.getEquippedMorphling()).orElse(ItemStack.EMPTY);
        return !stack.isEmpty() && type.isInstance(stack.getItem());
    }

    private static MuscleMemoryResonanceRules.Resonance resonance(ServerPlayer player, MuscleMemory memory) {
        ItemStack stack = HemoCapabilityAccess.getEquippedMorphling(player)
                .map(cap -> cap.getEquippedMorphling()).orElse(ItemStack.EMPTY);
        if (stack.getItem() instanceof IMorphling morphling) {
            return MuscleMemoryResonanceRules.resolve(memory, morphling.getPreferredTendency(), morphling.getSecondaryTendency(), isSignature(memory, stack.getItem()));
        }
        return MuscleMemoryResonanceRules.Resonance.NONE;
    }

    private static boolean isSignature(MuscleMemory memory, Object item) {
        return switch (memory) {
            case SANGUINE_FISTS -> item instanceof EmberfangMorphlingItem;
            case LABORING_ARMS -> item instanceof IrontoothMorphlingItem;
            case COURSING_LEGS -> item instanceof EmberfangMorphlingItem;
            case HUSHED_GAIT -> item instanceof BootlaceMorphlingItem;
            case PREDATORY_EYES -> item instanceof WitchsEarMorphlingItem;
            case SECOND_PULSE -> item instanceof DeadmansPurseMorphlingItem;
            case ENDURING_VISCERA -> item instanceof WinterShroudMorphlingItem;
            case CARRION_METABOLISM -> item instanceof GravecapMorphlingItem;
        };
    }

    private static String displayName(MuscleMemory memory) {
        String[] words = memory.id().split("_");
        StringBuilder value = new StringBuilder();
        for (String word : words) {
            if (!value.isEmpty()) value.append(' ');
            value.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return value.toString();
    }

    public record TriggerResult(boolean accepted, boolean overexerted, boolean fellBack) {
        public static final TriggerResult REJECTED = new TriggerResult(false, false, false);
    }
}
