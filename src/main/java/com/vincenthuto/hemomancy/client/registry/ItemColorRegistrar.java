package com.vincenthuto.hemomancy.client.registry;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.item.*;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScarPattern;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class ItemColorRegistrar {

    private ItemColorRegistrar() {
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        ScarPatternItemColor scarPatternColor = new ScarPatternItemColor();
        ItemInit.BASEITEMS.getEntries().stream()
                .filter(entry -> entry.get() instanceof ItemScarPattern)
                .forEach(entry -> event.register(scarPatternColor, entry.get()));
    }
}
