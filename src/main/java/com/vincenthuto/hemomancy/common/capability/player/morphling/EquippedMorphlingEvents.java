package com.vincenthuto.hemomancy.common.capability.player.morphling;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.morphling.SyncEquippedMorphlingPacket;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
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
