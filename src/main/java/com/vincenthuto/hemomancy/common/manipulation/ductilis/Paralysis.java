package com.vincenthuto.hemomancy.common.manipulation.ductilis;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationChannelManager;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import java.util.Map;
import java.util.WeakHashMap;

@EventBusSubscriber(modid=Hemomancy.MOD_ID)
public final class Paralysis {
    private static final Map<LivingEntity,Long> RECOVERY = new WeakHashMap<>();
    private Paralysis() { }

    public static boolean isParalyzed(LivingEntity entity) {
        return entity.isAlive() && entity.hasEffect(EffectInit.paralysis);
    }

    public static boolean apply(LivingEntity target, int ticks) {
        if (target.level().isClientSide || !target.isAlive()) return false;
        if (ManipulationReactiveEvents.isBoss(target)) {
            target.forceAddEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                    DuctilisRules.paralysisDuration(ticks,false),1,false,true),null);
            return target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN);
        }
        return target.addEffect(new MobEffectInstance(EffectInit.paralysis,
                DuctilisRules.paralysisDuration(ticks,target instanceof Player),0,false,true,true));
    }

    public static void interrupt(LivingEntity entity) {
        enforceLock(entity);
        if (entity instanceof ServerPlayer player) com.vincenthuto.hemomancy.common.network.PacketHandler.sendToPlayer(
                player,com.vincenthuto.hemomancy.common.network.ManipulationInterruptedPacket.INSTANCE);
    }

    public static void enforceLock(LivingEntity entity) {
        entity.stopUsingItem();
        if (entity instanceof net.minecraft.world.entity.monster.Creeper creeper && !creeper.isIgnited()) creeper.setSwellDir(-1);
        if (entity instanceof Mob mob) mob.getNavigation().stop();
        if (entity instanceof ServerPlayer player) ManipulationChannelManager.stop(player,false);
    }

    public static void removed(LivingEntity entity, MobEffectInstance effect) {
        if (!entity.level().isClientSide && effect.is(EffectInit.paralysis)) {
            RECOVERY.put(entity,entity.level().getGameTime()+DuctilisRules.RECOVERY_TICKS);
        }
    }

    @SubscribeEvent public static void onApplicable(MobEffectEvent.Applicable event) {
        if (!event.getEffectInstance().is(EffectInit.paralysis)) return;
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) return;
        int duration=event.getEffectInstance().getDuration();
        int cap=target instanceof Player ? 20 : 60;
        if (duration<0 || duration>cap) {
            ((com.vincenthuto.hemomancy.mixin.core.MobEffectInstanceAccessor)(Object)event.getEffectInstance())
                    .hemomancy$setDuration(cap);
        }
        if (ManipulationReactiveEvents.isBoss(target)
                || !DuctilisRules.canParalyze(target.level().getGameTime(),isParalyzed(target),RECOVERY.getOrDefault(target,0L))) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    @SubscribeEvent public static void onAdded(MobEffectEvent.Added event) {
        if (event.getEffectInstance().is(EffectInit.paralysis)) interrupt(event.getEntity());
    }

    @SubscribeEvent public static void onUseItem(PlayerInteractEvent.RightClickItem e) {
        if (isParalyzed(e.getEntity())) e.setCanceled(true);
    }
    @SubscribeEvent public static void onUseBlock(PlayerInteractEvent.RightClickBlock e) {
        if (isParalyzed(e.getEntity())) e.setCanceled(true);
    }
    @SubscribeEvent public static void onUseEntity(PlayerInteractEvent.EntityInteract e) {
        if (isParalyzed(e.getEntity())) e.setCanceled(true);
    }
    @SubscribeEvent public static void onUseEntityAt(PlayerInteractEvent.EntityInteractSpecific e) {
        if (isParalyzed(e.getEntity())) e.setCanceled(true);
    }
    @SubscribeEvent public static void onAttackBlock(PlayerInteractEvent.LeftClickBlock e) {
        if (isParalyzed(e.getEntity())) e.setCanceled(true);
    }
    @SubscribeEvent public static void onDeath(LivingDeathEvent e) {
        e.getEntity().removeEffect(EffectInit.paralysis); RECOVERY.remove(e.getEntity());
    }

    private static void clear(Player player) {
        player.removeEffect(EffectInit.paralysis); RECOVERY.remove(player);
    }
    @SubscribeEvent public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) { clear(e.getEntity()); }
    @SubscribeEvent public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e) { clear(e.getEntity()); }
    @SubscribeEvent public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent e) { clear(e.getEntity()); }
    @SubscribeEvent public static void onRespawn(PlayerEvent.PlayerRespawnEvent e) { clear(e.getEntity()); }
}
