package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketCardinalRiteStaffPlanting;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRitePlantingSequence;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class TerrestrialSpeculumPlantingManager {
	private static final double MAX_DISTANCE_SQUARED = 16.0D;
	private static final Map<UUID, Planting> ACTIVE = new HashMap<>();

	private TerrestrialSpeculumPlantingManager() {
	}

	public static boolean start(ServerPlayer player, BlockPos origin, Direction facing, InteractionHand hand) {
		if (ACTIVE.containsKey(player.getUUID())) return false;
		long startedAt = player.level().getGameTime();
		ACTIVE.put(player.getUUID(), new Planting(
				player.level().dimension(), origin.immutable(), facing, hand, startedAt, false));
		player.getCooldowns().addCooldown(ItemInit.terrestrial_speculum.get(),
				CardinalRitePlantingSequence.DURATION_TICKS);
		PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
				new PacketCardinalRiteStaffPlanting(player.getId(), origin, player.getItemInHand(hand)));
		return true;
	}

	@SubscribeEvent
	public static void tick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		Planting planting = ACTIVE.get(player.getUUID());
		if (planting == null) return;
		if (!player.level().dimension().equals(planting.dimension)) {
			cancel(player);
			return;
		}
		long now = player.level().getGameTime();
		if (!TerrestrialSpeculumPlantingSequence.isActive(planting.startedAt, now)) {
			ACTIVE.remove(player.getUUID());
			return;
		}
		if (planting.manifested
				|| !TerrestrialSpeculumPlantingSequence.shouldManifest(planting.startedAt, now)) return;

		boolean valid = player.isAlive()
				&& player.getItemInHand(planting.hand).is(ItemInit.terrestrial_speculum.get())
				&& player.distanceToSqr(Vec3.atCenterOf(planting.origin)) <= MAX_DISTANCE_SQUARED
				&& TerrestrialSpeculumItem.canManifestAt(player, planting.origin);
		if (!valid || !TerrestrialSpeculumItem.manifest(player, planting.origin, planting.facing)) {
			cancel(player);
			player.displayClientMessage(Component.literal("The Speculum loses its purchase in the earth.")
					.withStyle(ChatFormatting.DARK_RED), true);
			return;
		}

		player.serverLevel().playSound(null, planting.origin, SoundEvents.ROOTED_DIRT_HIT,
				SoundSource.PLAYERS, 1.15F, 0.62F);
		ACTIVE.put(player.getUUID(), planting.withManifested());
	}

	@SubscribeEvent
	public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) cancel(player);
	}

	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		ACTIVE.clear();
	}

	private static void cancel(ServerPlayer player) {
		ACTIVE.remove(player.getUUID());
		PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
				PacketCardinalRiteStaffPlanting.stop(player.getId()));
	}

	private record Planting(ResourceKey<Level> dimension, BlockPos origin, Direction facing,
			InteractionHand hand, long startedAt, boolean manifested) {
		private Planting withManifested() {
			return new Planting(dimension, origin, facing, hand, startedAt, true);
		}
	}
}
