package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.FungalStrains;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.item.MorphicNectarMutationRules;
import com.vincenthuto.hemomancy.common.item.harbinger.armor.CleatsFitting;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.common.item.shared.StructureScannerItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Tooltip lines added to Hemomancy items.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class ItemTooltipEvents {

    private ItemTooltipEvents() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().has(DataComponentInit.STRUCTURE_SCANNER_TOOLTIP.get())) {
            event.getItemStack().addToTooltip(DataComponentInit.STRUCTURE_SCANNER_TOOLTIP.get(), event.getContext(),
                    event.getToolTip()::add, event.getFlags());
        }

        if (event.getItemStack().getItem() instanceof StructureScannerItem scanner) {
            scanner.addScannerStateTooltip(event.getItemStack(), event.getToolTip());
        }

        appendMorphicNectarMutationTooltip(event);
        appendFirstHourTooltip(event);
        appendSporeSubstrateTooltip(event);
        appendCleatsTooltip(event);
    }

    /** Boots the Armature fitted with Hematic Cleats say so, and whether the radial has them off (CN-10). */
    private static void appendCleatsTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!CleatsFitting.isFitted(stack)) {
            return;
        }
        event.getToolTip().add(Component.translatable(CleatsFitting.isOn(stack)
                ? "tooltip.hemomancy.cleats_fitted.on" : "tooltip.hemomancy.cleats_fitted.off")
                .withStyle(ChatFormatting.DARK_RED));
    }

    /** A spore names the ground its strain stands on when planted (core-loop round two, P7). */
    private static void appendSporeSubstrateTooltip(ItemTooltipEvent event) {
        var id = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        if (!Hemomancy.MOD_ID.equals(id.getNamespace())) {
            return;
        }
        FungalStrains strain = FungalStrains.bySpore(id.getPath());
        if (strain != null) {
            event.getToolTip().add(Component.translatable(strain.substrate().tooltipKey()).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static void appendMorphicNectarMutationTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!MorphicNectarMutationRules.shouldShowMutation(stack)) {
            return;
        }
        boolean primal = MorphlingItem.isPrimal(stack);
        event.getToolTip().add(Component.translatable(primal
                        ? "tooltip.hemomancy.morphic_nectar_mutated.primal"
                        : "tooltip.hemomancy.morphic_nectar_mutated")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }

    private static void appendFirstHourTooltip(ItemTooltipEvent event) {
        Item item = event.getItemStack().getItem();
        if (item == ItemInit.gourd_seeds.get()) {
            event.getToolTip().add(Component.translatable("item.hemomancy.gourd_seeds.tooltip")
                    .withStyle(ChatFormatting.GRAY));
        } else if (item == ItemInit.hematic_iron_scrap.get()) {
            event.getToolTip().add(Component.translatable("item.hemomancy.hematic_iron_scrap.tooltip")
                    .withStyle(ChatFormatting.GRAY));
            event.getToolTip().add(Component.translatable("item.hemomancy.hematic_iron_scrap.tooltip.nugget")
                    .withStyle(ChatFormatting.DARK_GRAY));
        } else if (item == ItemInit.chalybeate_sclerite.get()) {
            event.getToolTip().add(Component.translatable("item.hemomancy.chalybeate_sclerite.tooltip")
                    .withStyle(ChatFormatting.GRAY));
            event.getToolTip().add(Component.translatable("item.hemomancy.chalybeate_sclerite.tooltip.harvest")
                    .withStyle(ChatFormatting.DARK_GRAY));
        } else if (item == ItemInit.erythrocoral_fragment.get()) {
            event.getToolTip().add(Component.translatable("item.hemomancy.erythrocoral_fragment.tooltip")
                    .withStyle(ChatFormatting.GRAY));
            event.getToolTip().add(Component.translatable("item.hemomancy.erythrocoral_fragment.tooltip.harvest")
                    .withStyle(ChatFormatting.DARK_GRAY));
        } else if (item == ItemInit.salt_stained_voyager_log.get()) {
            event.getToolTip().add(Component.translatable("item.hemomancy.salt_stained_voyager_log.tooltip")
                    .withStyle(ChatFormatting.GRAY));
            event.getToolTip().add(Component.translatable("item.hemomancy.salt_stained_voyager_log.tooltip.inquiry")
                    .withStyle(ChatFormatting.DARK_GRAY));
            event.getToolTip().add(Component.translatable("item.hemomancy.salt_stained_voyager_log.tooltip.culture")
                    .withStyle(ChatFormatting.DARK_GRAY));
        } else if (item == ItemInit.charm_of_vascularium.get()) {
            event.getToolTip().add(Component.translatable("item.hemomancy.charm_of_vascularium.tooltip.first_hour")
                    .withStyle(ChatFormatting.DARK_RED));
        } else if (item == ItemInit.liber_sanguinum.get()) {
            event.getToolTip().add(Component.translatable("item.hemomancy.liber_sanguinum.tooltip.first_hour")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
