package com.vincenthuto.hemomancy.common.brewing;

import com.vincenthuto.hemomancy.Hemomancy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class AdvancedBrewingReload {
    private static int generation;

    private AdvancedBrewingReload() {}

    public static int generation() { return generation; }

    @SubscribeEvent public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) generation++;
    }
}
