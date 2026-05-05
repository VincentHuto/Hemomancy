package com.vincenthuto.hemomancy.common.item.harbinger.scar;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeServerPacket;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * Blood-Honed — FERRIC Tier 2 scar. When the wearer strikes with an enchanted
 * weapon (Sharpness, Smite, or Bane of Arthropods), it drains blood equal to
 * 40 × enchantment level and deals bonus damage matching that level.
 *
 * The enchanter traps force in metal. The blood-worker teaches that metal to hunger.
 */
public class BloodHonedScar extends ItemScar {

    private static final float BLOOD_DRAIN_PER_LEVEL = 40f;

    public BloodHonedScar(Properties properties) {
        super(properties, EnumBloodTendency.FERRIC, 2.0f, 2);
    }

    @Override
    public void onPlayerAttack(Player player, LivingEntity target) {
        super.onPlayerAttack(player, target);

        ItemStack weapon = player.getMainHandItem();
        if (weapon.isEmpty()) return;

        var registryAccess = player.level().registryAccess();
        int sharpness = getLevel(registryAccess, Enchantments.SHARPNESS, weapon);
        int smite     = getLevel(registryAccess, Enchantments.SMITE, weapon);
        int bane      = getLevel(registryAccess, Enchantments.BANE_OF_ARTHROPODS, weapon);
        int level = Math.max(sharpness, Math.max(smite, bane));
        if (level <= 0) return;

        HemoCapabilityAccess.getBloodVolume(player).ifPresent(blood -> {
            if (!blood.isActive()) return;
            float drain = level * BLOOD_DRAIN_PER_LEVEL;
            if (blood.getBloodVolume() < drain) return;

            blood.drain(drain);
            float bonus = level * (0.5f + (tier * 0.25f)); // tier 2 → +1.0 per enchantment level
            target.hurt(player.damageSources().playerAttack(player), bonus);

            if (player instanceof ServerPlayer sp) {
                PacketHandler.sendToPlayer(sp, new BloodVolumeServerPacket(blood));
            }
        });
    }

    private static int getLevel(net.minecraft.core.HolderLookup.Provider registryAccess,
                                 ResourceKey<Enchantment> key, ItemStack stack) {
        return registryAccess.lookupOrThrow(Registries.ENCHANTMENT)
                .get(key)
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack))
                .orElse(0);
    }
}
