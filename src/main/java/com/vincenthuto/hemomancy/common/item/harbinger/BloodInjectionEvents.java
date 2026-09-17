package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.network.BloodInjectionSyncPacket;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class BloodInjectionEvents {
    private static final TagKey<Block> EARTH_AND_STONE = TagKey.create(Registries.BLOCK, Hemomancy.rloc("blood_injection_mineable"));
    private BloodInjectionEvents() {}
    @SubscribeEvent public static void sync(OnDatapackSyncEvent event) {
        var packet = new BloodInjectionSyncPacket(BloodInjectionData.snapshot(false).json());
        if (event.getPlayer() != null) PacketDistributor.sendToPlayer(event.getPlayer(), packet);
        else for (var player : event.getPlayerList().getPlayers()) PacketDistributor.sendToPlayer(player, packet);
    }
    @SubscribeEvent public static void damage(LivingIncomingDamageEvent event) {
        var entity = event.getEntity();
        if (entity.hasEffect(EffectInit.cryoprotection) && event.getSource().is(DamageTypes.FREEZE)) {
            event.setCanceled(true);
            return;
        }
        if (entity.hasEffect(EffectInit.ender_resistance) && event.getSource().is(DamageTypeTags.IS_PROJECTILE)
            || entity.hasEffect(EffectInit.explosion_resistance) && event.getSource().is(DamageTypeTags.IS_EXPLOSION))
            event.setAmount(event.getAmount() * BloodInjectionRules.RESISTANCE_MULTIPLIER);
    }
    @SubscribeEvent public static void heal(LivingHealEvent event) {
        if (event.getEntity().hasEffect(EffectInit.impaired_recovery))
            event.setAmount(event.getAmount() * BloodInjectionRules.HEALING_MULTIPLIER);
    }
    @SubscribeEvent public static void mining(PlayerEvent.BreakSpeed event) {
        if (event.getEntity().hasEffect(EffectInit.earthen_mining)
            && event.getState().is(EARTH_AND_STONE))
            event.setNewSpeed(event.getNewSpeed() * BloodInjectionRules.MINING_MULTIPLIER);
    }
    @SubscribeEvent public static void effect(MobEffectEvent.Applicable event) {
        var incoming = event.getEffectInstance().getEffect();
        if (incoming == MobEffects.POISON && event.getEntity().hasEffect(EffectInit.poison_resistance)
            || incoming == MobEffects.WITHER && event.getEntity().hasEffect(EffectInit.wither_resistance))
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
    }
}

