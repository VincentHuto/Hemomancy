package com.vincenthuto.hemomancy.common.enchanting;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class ScriptoriumUseEvents {
    private ScriptoriumUseEvents() {}

    @SubscribeEvent public static void breakBlock(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player) || event.isCanceled()) return;
        ItemStack tool = player.getMainHandItem();
        charge(player, tool, "sanguine_appetite", hasOvercap(tool, "efficiency"));
    }

    @SubscribeEvent public static void fortuneDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof ServerPlayer player) || event.getDrops().isEmpty()) return;
        ItemStack tool = event.getTool();
        if (hasOvercap(tool, "fortune") && event.getDrops().stream().anyMatch(drop -> drop.getItem().getCount() > 1)) {
            charge(player, player.getMainHandItem(), "", true);
            addDirectWear(player.getMainHandItem(), Math.max(1, overcapLevels(tool, "fortune")));
        }
    }

    @SubscribeEvent public static void successfulDamage(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide || event.getNewDamage() <= 0) return;
        if (event.getSource().getDirectEntity() instanceof ServerPlayer attacker) {
            ItemStack weapon = attacker.getMainHandItem();
            charge(attacker, weapon, "thirsting_edge", hasOvercap(weapon, "sharpness", "smite", "bane_of_arthropods"));
        }
        if (event.getEntity() instanceof ServerPlayer victim) {
            for (ItemStack armor : victim.getArmorSlots())
                charge(victim, armor, "open_vessel", event.getReduction(DamageContainer.Reduction.ENCHANTMENTS) > 0
                        && (hasOvercap(armor, "protection")
                        || event.getSource().is(DamageTypeTags.IS_FALL) && hasOvercap(armor, "feather_falling")));
        }
    }

    @SubscribeEvent public static void lootingDrops(LivingDropsEvent event) {
        if (event.getDrops().isEmpty() || !(event.getSource().getEntity() instanceof ServerPlayer attacker)) return;
        ItemStack weapon = attacker.getMainHandItem();
        if (hasOvercap(weapon, "looting")) charge(attacker, weapon, "", true);
    }

    @SubscribeEvent public static void shot(EntityJoinLevelEvent event) {
        if (event.loadedFromDisk() || !(event.getEntity() instanceof AbstractArrow arrow)
                || !(arrow.getOwner() instanceof ServerPlayer player)) return;
        ItemStack weapon = player.getMainHandItem();
        if (!weapon.has(DataComponentInit.SCRIPTORIUM_PROVENANCE.get())) weapon = player.getOffhandItem();
        if (!weapon.has(DataComponentInit.SCRIPTORIUM_PROVENANCE.get())) return;
        long now = player.level().getGameTime();
        if (player.getPersistentData().getLong("ScriptoriumLastShot") == now) return;
        player.getPersistentData().putLong("ScriptoriumLastShot", now);
        charge(player, weapon, "hemorrhagic_string", hasOvercap(weapon, "power"));
    }

    private static boolean hasOvercap(ItemStack stack, String... names) {
        for (String name : names) if (overcapLevels(stack, name) > 0) return true;
        return false;
    }

    private static int overcapLevels(ItemStack stack, String name) {
        for (var entry : stack.getEnchantments().entrySet()) {
            var key = entry.getKey().unwrapKey().orElse(null);
            if (key != null && key.location().getNamespace().equals("minecraft")
                    && key.location().getPath().equals(name))
                return Math.max(0, entry.getIntValue() - entry.getKey().value().getMaxLevel());
        }
        return 0;
    }

    private static int actualLoad(ItemStack stack) {
        int load = 0;
        for (var entry : stack.getEnchantments().entrySet())
            load += Math.max(0, entry.getIntValue() - entry.getKey().value().getMaxLevel());
        return load;
    }

    private static void charge(ServerPlayer player, ItemStack stack, String curseTrigger, boolean pressure) {
        if (stack.isEmpty()) return;
        ScriptoriumProvenance provenance = stack.get(DataComponentInit.SCRIPTORIUM_PROVENANCE.get());
        if (provenance == null) return;
        int load = pressure ? actualLoad(stack) : 0;
        int curseCost = provenance.curse().equals(curseTrigger)
                ? ScriptoriumCurseRules.bloodCost(provenance.curse(), provenance.severity()) : 0;
        int bloodCost = curseCost + ScriptoriumBalance.useBloodCost(load);
        if (bloodCost <= 0 && load <= 0) return;
        var blood = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
        boolean paid = bloodCost == 0 || blood != null && blood.getBloodVolume() >= bloodCost;
        if (paid && bloodCost > 0) {
            blood.drain(bloodCost);
            BloodVolumeEvents.syncVolume(player, blood);
        }
        if (stack.isDamageableItem()) {
            double extra = Math.max(0, ScriptoriumBalance.wearMultiplier(load) - 1);
            int wear = (int) extra;
            if (player.getRandom().nextDouble() < extra - wear) wear++;
            if (!paid) wear += Math.max(1, provenance.severity());
            addDirectWear(stack, wear);
        }
        if (load >= 3 && player.level().getGameTime() >= player.getPersistentData().getLong("ScriptoriumBacklashUntil")) {
            player.getPersistentData().putLong("ScriptoriumBacklashUntil", player.level().getGameTime() + 20);
            if (player.getRandom().nextFloat() < (load >= 5 ? 0.10F : 0.05F))
                player.hurt(player.damageSources().generic(), 1.0F);
        }
    }

    private static void addDirectWear(ItemStack stack, int wear) {
        if (wear <= 0 || !stack.isDamageableItem()) return;
        stack.setDamageValue(stack.getDamageValue() + wear);
        if (stack.getDamageValue() >= stack.getMaxDamage()) stack.shrink(1);
    }
}
