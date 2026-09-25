package com.vincenthuto.hemomancy.common.brewing;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class AdvancedBrewTooltips {
    private AdvancedBrewTooltips() {}

    @SubscribeEvent public static void tooltip(ItemTooltipEvent event) {
        AdvancedBrewData data = event.getItemStack().get(DataComponentInit.ADVANCED_BREW.get());
        if (data == null) return;
        if (data.kind().equals("vessel")) {
            event.getToolTip().add(Component.translatable("tooltip.hemomancy.advanced_brew.doses", data.doses())
                    .withStyle(data.doses() == 0 ? ChatFormatting.RED : ChatFormatting.GRAY));
            event.getToolTip().add(Component.translatable("tooltip.hemomancy.advanced_brew.substitution")
                    .withStyle(ChatFormatting.GRAY));
        }
        for (int i = 0; i < data.parts().size(); i++) {
            for (MobEffectInstance effect : data.parts().get(i).getAllEffects()) {
                event.getToolTip().add(Component.translatable("tooltip.hemomancy.advanced_brew.part",
                        i + 1, effect.getEffect().value().getDisplayName(), effect.getAmplifier() + 1,
                        effect.getDuration() / 20).withStyle(ChatFormatting.DARK_RED));
            }
        }
        if (!data.attachment().isBlank())
            event.getToolTip().add(Component.translatable("tooltip.hemomancy.advanced_brew." + data.attachment())
                    .withStyle(ChatFormatting.GRAY));
    }
}
