package com.vincenthuto.hemomancy.common.capability.player.harbinger.thelemicmemory;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrains;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncThelemicMemory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class ThelemicMemoryEvents {
    private static final Map<UUID, PrimaryAttack> PRIMARY_ATTACKS = new HashMap<>();
    private static final Map<UUID, Map<Integer, Long>> LAST_TARGET_TRIGGER = new HashMap<>();

    private ThelemicMemoryEvents() {
    }

	public static void clearSessionState() {
		PRIMARY_ATTACKS.clear();
		LAST_TARGET_TRIGGER.clear();
	}

    @SubscribeEvent
    public static void rememberPrimaryAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide && !event.isCanceled()) {
            PRIMARY_ATTACKS.put(player.getUUID(),
                    new PrimaryAttack(event.getTarget().getId(), player.level().getGameTime()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void applySanguineFists(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Entity direct = event.getSource().getDirectEntity();
        long gameTime = player.level().getGameTime();
        boolean directMelee = direct == player && event.getSource().isDirect()
                && isPrimaryAttack(player, event.getEntity(), gameTime);
        boolean duplicate = recentlyTriggered(player, event.getEntity(), gameTime);
        ThelemicMemoryState state = HemoCapabilityAccess.thelemicMemory(player);
        SanguineFistsRules.Result result = SanguineFistsRules.evaluate(
                state.isEnabled(ThelemicMemory.SANGUINE_FISTS), directMelee, duplicate, Double.MAX_VALUE);
        if (!result.triggers()) {
            return;
        }
        var trigger = ThelemicMemoryActivationService.tryTriggerDetailed(
                player, ThelemicMemory.SANGUINE_FISTS, 20, 20);
        if (!trigger.accepted()) return;
        event.setNewDamage(event.getNewDamage() + result.bonusDamage() + (trigger.overexerted() ? 2F : 0F));
        if (trigger.overexerted() || ThelemicMemoryActivationService.hasSignatureMorphling(player, MorphlingStrains.KINBUD)) {
            event.getEntity().knockback(trigger.overexerted() ? .75D : .5D,
                    Math.sin(Math.toRadians(player.getYRot())), -Math.cos(Math.toRadians(player.getYRot())));
        }
        if (ThelemicMemoryWorldEvents.consumeCoursingMomentum(player)) event.setNewDamage(event.getNewDamage() + 1F);
        LAST_TARGET_TRIGGER.computeIfAbsent(player.getUUID(), ignored -> new HashMap<>())
                .put(event.getEntity().getId(), gameTime);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void applySecondPulse(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide) return;
        float reduction = ThelemicMemoryPowerRules.secondPulseReduction(player.getHealth(), player.getMaxHealth(), event.getNewDamage());
        if (reduction > 0F) {
            var trigger = ThelemicMemoryActivationService.tryTriggerSecondPulseDetailed(player);
            if (trigger.accepted()) {
                float applied = trigger.overexerted()
                        ? Math.min(event.getNewDamage() * .5F, 10F) : reduction;
                event.setNewDamage(event.getNewDamage() - applied);
            }
        }
    }

    private static boolean isPrimaryAttack(Player player, Entity target, long gameTime) {
        PrimaryAttack attack = PRIMARY_ATTACKS.get(player.getUUID());
        return attack != null && attack.targetId() == target.getId()
                && gameTime - attack.gameTime() <= 1L;
    }

    private static boolean recentlyTriggered(Player player, Entity target, long gameTime) {
        Long last = LAST_TARGET_TRIGGER.getOrDefault(player.getUUID(), Map.of()).get(target.getId());
        return last != null && gameTime - last < SanguineFistsRules.TARGET_GUARD_TICKS;
    }

    public static void sync(ServerPlayer player) {
        PacketSyncThelemicMemory packet = new PacketSyncThelemicMemory(player.getId(),
                HemoCapabilityAccess.thelemicMemory(player));
        if (player.connection != null) {
            PacketHandler.sendToPlayerAndTrackers(player, packet);
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer observer
                && event.getTarget() instanceof ServerPlayer target) {
            PacketHandler.sendToPlayer(observer, new PacketSyncThelemicMemory(target.getId(),
                    HemoCapabilityAccess.thelemicMemory(target)));
        }
    }

    /** Called by {@code TransientServerStateEvents} when the player logs out. */
    public static void logout(Player player) {
        UUID playerId = player.getUUID();
        PRIMARY_ATTACKS.remove(playerId);
        LAST_TARGET_TRIGGER.remove(playerId);
        if (player instanceof ServerPlayer serverPlayer) ThelemicMemoryWorldEvents.clearRuntime(serverPlayer);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            HemoCapabilityAccess.thelemicMemory(player).clearPrimed();
            PRIMARY_ATTACKS.remove(player.getUUID());
            LAST_TARGET_TRIGGER.remove(player.getUUID());
            ThelemicMemoryWorldEvents.clearRuntime(player);
        }
    }

    private record PrimaryAttack(int targetId, long gameTime) {
    }
}
