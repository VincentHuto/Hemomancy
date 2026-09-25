package com.vincenthuto.hemomancy.common.brewing;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;

public final class AdvancedPotionUse {
    private AdvancedPotionUse() {}

    public static boolean isVessel(ItemStack stack) {
        AdvancedBrewData data = stack.get(DataComponentInit.ADVANCED_BREW.get());
        return data != null && data.kind().equals("vessel");
    }

    public static boolean isEmptyVessel(ItemStack stack) {
        AdvancedBrewData data = stack.get(DataComponentInit.ADVANCED_BREW.get());
        return data != null && data.kind().equals("vessel") && data.doses() == 0;
    }

    public static ItemStack finishVessel(ItemStack stack, ServerPlayer player) {
        AdvancedBrewData data = stack.get(DataComponentInit.ADVANCED_BREW.get());
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (data == null || !data.kind().equals("vessel") || data.doses() == 0
                || !data.matchesOutputContents(contents)) return stack;
        int count = 0;
        for (MobEffectInstance ignored : contents.getAllEffects()) count++;
        if (count != 3) return stack;

        if (!player.getAbilities().instabuild) {
            boolean paidBlood = false;
            var volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
            if (volume != null && volume.isActive() && !volume.wouldOverstrain(1000)
                    && BoundBrewRules.substitutesDose(volume.getBloodVolume(), player.getRandom().nextDouble())) {
                paidBlood = volume.drain(1000);
                if (paidBlood) BloodVolumeEvents.syncVolume(player, volume);
            }
            if (!paidBlood) stack.set(DataComponentInit.ADVANCED_BREW.get(), data.withDoses(data.doses() - 1));
        }
        for (MobEffectInstance effect : contents.getAllEffects()) player.addEffect(new MobEffectInstance(effect));
        return stack;
    }
}
