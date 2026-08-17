package com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.EmberfangMorphlingItem;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncMuscleMemory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class MuscleMemoryEvents {
    private static final Map<UUID, PrimaryAttack> PRIMARY_ATTACKS = new HashMap<>();
    private static final Map<UUID, Map<Integer, Long>> LAST_TARGET_TRIGGER = new HashMap<>();

    private MuscleMemoryEvents() {
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
        MuscleMemoryState state = player.getData(HemoAttachmentTypes.MUSCLE_MEMORY);
        SanguineFistsRules.Result result = SanguineFistsRules.evaluate(
                state.isEnabled(MuscleMemory.SANGUINE_FISTS), directMelee, duplicate, Double.MAX_VALUE);
        if (!result.triggers()) {
            return;
        }
        var trigger = MuscleMemoryActivationService.tryTriggerDetailed(
                player, MuscleMemory.SANGUINE_FISTS, 20, 20);
        if (!trigger.accepted()) return;
        event.setNewDamage(event.getNewDamage() + result.bonusDamage() + (trigger.overexerted() ? 2F : 0F));
        if (trigger.overexerted() || MuscleMemoryActivationService.hasSignatureMorphling(player, EmberfangMorphlingItem.class)) {
            event.getEntity().knockback(trigger.overexerted() ? .75D : .5D,
                    Math.sin(Math.toRadians(player.getYRot())), -Math.cos(Math.toRadians(player.getYRot())));
        }
        if (MuscleMemoryWorldEvents.consumeEmberfangMomentum(player)) event.setNewDamage(event.getNewDamage() + 1F);
        LAST_TARGET_TRIGGER.computeIfAbsent(player.getUUID(), ignored -> new HashMap<>())
                .put(event.getEntity().getId(), gameTime);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void applySecondPulse(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide) return;
        float reduction = MuscleMemoryPowerRules.secondPulseReduction(player.getHealth(), player.getMaxHealth(), event.getNewDamage());
        if (reduction > 0F) {
            var trigger = MuscleMemoryActivationService.tryTriggerSecondPulseDetailed(player);
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
        PacketSyncMuscleMemory packet = new PacketSyncMuscleMemory(player.getId(),
                player.getData(HemoAttachmentTypes.MUSCLE_MEMORY));
        if (player.connection != null) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, packet);
        }
    }

    @SubscribeEvent
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			player.getData(HemoAttachmentTypes.MUSCLE_MEMORY).migrateLegacyPriming(player.level().getGameTime());
			sync(player);
		}
	}

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sync(player);
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer observer
                && event.getTarget() instanceof ServerPlayer target) {
            PacketDistributor.sendToPlayer(observer, new PacketSyncMuscleMemory(target.getId(),
                    target.getData(HemoAttachmentTypes.MUSCLE_MEMORY)));
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerId = event.getEntity().getUUID();
        PRIMARY_ATTACKS.remove(playerId);
        LAST_TARGET_TRIGGER.remove(playerId);
        if (event.getEntity() instanceof ServerPlayer player) MuscleMemoryWorldEvents.clearRuntime(player);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getData(HemoAttachmentTypes.MUSCLE_MEMORY).clearPrimed();
            PRIMARY_ATTACKS.remove(player.getUUID());
            LAST_TARGET_TRIGGER.remove(player.getUUID());
            MuscleMemoryWorldEvents.clearRuntime(player);
            sync(player);
        }
    }

    private record PrimaryAttack(int targetId, long gameTime) {
    }
}
