package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.GatherEffectScreenTooltipsEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class SchoolStateTooltips {
    private SchoolStateTooltips() {}
    @SubscribeEvent public static void tooltip(GatherEffectScreenTooltipsEvent event) {
        for (SchoolState state : SchoolState.values()) {
            if (!event.getEffectInstance().is(state.effect())) continue;
            event.getTooltip().add(Component.translatable(state.effect().value().getDescriptionId() + ".desc")
                    .withStyle(ChatFormatting.GRAY));
            event.getTooltip().add(Component.translatable(state.effect().value().getDescriptionId() + ".counter")
                    .withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
    }
}
