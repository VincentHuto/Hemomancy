package com.vincenthuto.hemomancy.common.brewing;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.PotionBrewEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class AdvancedBrewingStandGuard {
    private AdvancedBrewingStandGuard() {}

    @SubscribeEvent public static void beforeBrew(PotionBrewEvent.Pre event) {
        for (int slot = 0; slot < Math.min(3, event.getLength()); slot++) {
            if (event.getItem(slot).has(DataComponentInit.ADVANCED_BREW.get())) {
                event.setCanceled(true);
                return;
            }
        }
    }
}
