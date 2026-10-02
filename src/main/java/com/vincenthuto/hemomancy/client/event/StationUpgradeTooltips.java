package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.station.StationUpgradeCatalog;
import com.vincenthuto.hemomancy.common.station.UpgradeStation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class StationUpgradeTooltips {
    private StationUpgradeTooltips() {}

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        if (event.getEntity() == null) return;
        var item = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        for (var tier : StationUpgradeCatalog.all()) {
            if (!tier.upgradeItem().equals(item)
                    || HemoCapabilityAccess.stationUpgrades(event.getEntity()).hasClaimed(tier.station(), tier.tier())) continue;
            String teacher = switch (tier.station()) {
                case ALEMBIC, CENTRIFUGE -> "entity.hemomancy.harbinger_alchemist";
                case RESONANT_FORGE -> "entity.hemomancy.harbinger_artificer";
                case SCRIPTORIUM -> "entity.hemomancy.harbinger_mnemonist";
                case ARMATURE -> tier.tier() == 1 ? "entity.hemomancy.harbinger_artificer" : "block.hemomancy.sanguine_monolith";
            };
            event.getToolTip().add(Component.translatable("item.hemomancy.station_upgrade.free_kit",
                    Component.translatable(teacher)).withStyle(ChatFormatting.YELLOW));
            event.getToolTip().add(Component.translatable("item.hemomancy.station_upgrade.crafting_reminder")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
