package com.vincenthuto.hemomancy.common.capability.player.morphling;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.item.morphlings.IMorphling;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.morphling.SyncEquippedMorphlingPacket;
import com.vincenthuto.hemomancy.config.HemoServerConfig;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EquippedMorphlingEvents {

	@SubscribeEvent
	public static void attachCapabilitiesEntity(final AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Player) {
			event.addCapability(Hemomancy.rloc("equipped_morphling"), new EquippedMorphlingProvider());
		}
	}

	/**
	 * While a morphling is equipped, it passively drains blood to sustain itself.
	 * The drain rate scales with the morphling's blood cost. If the player runs out
	 * of blood, the morphling is forcefully unequipped.
	 */
	@SubscribeEvent
	public static void playerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		Player player = event.player;
		if (player.level().isClientSide) return;
		if (!HemoServerConfig.MORPHLING_PASSIVE_DRAIN_ENABLED.get()) return;

		player.getCapability(EquippedMorphlingProvider.MORPHLING_CAPA).ifPresent(morphCap -> {
			if (!morphCap.hasMorphling()) return;

			int drainInterval = HemoServerConfig.MORPHLING_DRAIN_INTERVAL.get();
			if (player.tickCount % drainInterval != 0) return;

			player.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(volume -> {
				if (!volume.isActive()) return;

				double drainRate = HemoServerConfig.MORPHLING_DRAIN_RATE.get();

				// Scale drain by morphling blood cost if it implements IMorphling
				if (morphCap.getEquippedMorphling().getItem() instanceof IMorphling morphling) {
					int bloodCost = morphling.getBloodCost();
					if (bloodCost > 0) {
						drainRate *= (1.0 + bloodCost / 100.0);
					}
				}

				if (volume.getBloodVolume() > drainRate) {
					volume.drain(drainRate);
					BloodVolumeEvents.syncVolume((ServerPlayer) player, volume);
				} else {
					// Not enough blood — unequip the morphling
					morphCap.clearMorphling();
					syncToClient((ServerPlayer) player);
					player.displayClientMessage(
							Component.literal("Your morphling withers from blood starvation...")
									.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
				}
			});

			// Delegate morphling-specific passive effects to the item itself
			if (morphCap.getEquippedMorphling().getItem() instanceof IMorphling morphling) {
				morphling.onEquippedTick(player, morphCap.getEquippedMorphling());
			}
		});
	}

	@SubscribeEvent
	public static void playerDeath(PlayerEvent.Clone event) {
		if (event.isWasDeath()) {
			Player original = event.getOriginal();
			Player newPlayer = event.getEntity();
			original.reviveCaps();
			original.getCapability(EquippedMorphlingProvider.MORPHLING_CAPA).ifPresent(oldCap -> {
				newPlayer.getCapability(EquippedMorphlingProvider.MORPHLING_CAPA).ifPresent(newCap -> {
					newCap.setEquippedMorphling(oldCap.getEquippedMorphling().copy());
				});
			});
			original.invalidateCaps();
		}
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		syncToClient((ServerPlayer) event.getEntity());
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerChangedDimensionEvent event) {
		syncToClient((ServerPlayer) event.getEntity());
	}

	@SubscribeEvent
	public static void playerRespawn(PlayerRespawnEvent event) {
		if (!event.getEntity().level().isClientSide) {
			syncToClient((ServerPlayer) event.getEntity());
		}
	}

	public static void syncToClient(ServerPlayer player) {
		player.getCapability(EquippedMorphlingProvider.MORPHLING_CAPA).ifPresent(cap -> {
			PacketHandler.CHANNELMORPHLINGJAR.send(
					PacketDistributor.PLAYER.with(() -> player),
					new SyncEquippedMorphlingPacket(cap.getEquippedMorphling()));
		});
	}

}
