package com.vincenthuto.hemomancy.common.enchanting;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.GrindstoneEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.AnvilRepairEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class ScriptoriumItemEvents {
    private ScriptoriumItemEvents() {}

    @SubscribeEvent public static void tooltip(ItemTooltipEvent event) {
        ScriptoriumProvenance provenance = event.getItemStack().get(DataComponentInit.SCRIPTORIUM_PROVENANCE.get());
        if (provenance == null) return;
        event.getToolTip().add(Component.literal("Inscribed in blood").withStyle(ChatFormatting.DARK_RED));
        if (!provenance.curse().isBlank())
            event.getToolTip().add(Component.translatable("hemomancy.scriptorium.curse." + provenance.curse())
                    .append(" " + provenance.severity()).withStyle(ChatFormatting.RED));
        if (provenance.excessLoad() > 0)
            event.getToolTip().add(Component.literal("Excess Load " + provenance.excessLoad()).withStyle(ChatFormatting.RED));
    }

    @SubscribeEvent public static void grindstone(GrindstoneEvent.OnPlaceItem event) {
        ItemStack result = event.getOutput();
        if (result.isEmpty()) return;
        ScriptoriumProvenance provenance = event.getTopItem().get(DataComponentInit.SCRIPTORIUM_PROVENANCE.get());
        if (provenance == null) provenance = event.getBottomItem().get(DataComponentInit.SCRIPTORIUM_PROVENANCE.get());
        if (provenance != null && !result.has(DataComponentInit.SCRIPTORIUM_PROVENANCE.get())) {
            result.set(DataComponentInit.SCRIPTORIUM_PROVENANCE.get(), provenance);
            event.setOutput(result);
        }
    }

    @SubscribeEvent public static void anvil(AnvilRepairEvent event) {
        ScriptoriumProvenance provenance = event.getLeft().get(DataComponentInit.SCRIPTORIUM_PROVENANCE.get());
        if (provenance == null) provenance = event.getRight().get(DataComponentInit.SCRIPTORIUM_PROVENANCE.get());
        if (provenance != null && !event.getOutput().has(DataComponentInit.SCRIPTORIUM_PROVENANCE.get()))
            event.getOutput().set(DataComponentInit.SCRIPTORIUM_PROVENANCE.get(), provenance);
    }
}
