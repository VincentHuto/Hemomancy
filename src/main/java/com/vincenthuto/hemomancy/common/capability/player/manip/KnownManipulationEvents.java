package com.vincenthuto.hemomancy.common.capability.player.manip;

import java.util.Collection;
import java.util.Collections;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.manips.KnownManipulationServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.manips.SyncTrackingAvatarPacket;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KnownManipulationEvents {
	@SubscribeEvent
	public static void attachCapabilitiesEntity(final AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject().getType() == EntityType.PLAYER) {
			event.addCapability(Hemomancy.rloc("knownmanipulations"), new KnownManipulationProvider());
		}
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerChangedDimensionEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		IKnownManipulations known = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
				.orElseThrow(IllegalStateException::new);
		PacketHandler.CHANNELKNOWNMANIPS.send(PacketDistributor.PLAYER.with(() -> player),
				new KnownManipulationServerPacket(known));
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerTickEvent event) {
		event.player.refreshDimensions();
		syncPlayerEvent(event.player);
	}

	@SubscribeEvent
	public static void onPlayerDamage(LivingDamageEvent e) {

		// Radiant Protection
		if (e.getEntity() instanceof Player) {
			Player player = (Player) e.getEntity();
			IKnownManipulations known = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
					.orElseThrow(NullPointerException::new);
			if (known.isAvatarActive()) {
				double dist = e.getEntity().distanceToSqr(player);
				HitResult trace = e.getEntity().pick(dist, 0, false);
				PacketHandler.sendAvatarHitParticles(trace.getLocation(), ParticleColor.WHITE, 16f,
						e.getEntity().level().dimension());
				e.setAmount(e.getAmount() * 0);

			}
		}

	}

	@SubscribeEvent
	public static void onStartTracking(PlayerEvent.StartTracking event) {
		Entity target = event.getTarget();
		if (target instanceof ServerPlayer) {
			syncAvatars((ServerPlayer) target, Collections.singletonList(event.getEntity()));
		}
	}

	@SubscribeEvent
	public static void playerDeath(PlayerEvent.Clone event) {

		if (event.getOriginal().level().isClientSide)
			return;

		Player peorig = event.getOriginal();
		Player playernew = event.getEntity();
		peorig.reviveCaps();

		peorig.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent(oldCap -> {
			playernew.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent(newCap -> {
				newCap.setCapa(oldCap);
			});
		});

		peorig.invalidateCaps();

	}

	@SubscribeEvent
	public static void playerRespawn(PlayerEvent.PlayerRespawnEvent e) {
		syncPlayerEvent(e.getEntity());
	}

	public static void syncPlayerEvent(Player playerEntity) {
		if (playerEntity instanceof ServerPlayer s) {
			s.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent(capa -> {
				PacketHandler.CHANNELKNOWNMANIPS.send(PacketDistributor.PLAYER.with(() -> s),
						new KnownManipulationServerPacket(capa));
			});
		}
	}

	@SubscribeEvent
	public static void playerJoin(EntityJoinLevelEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof ServerPlayer) {
			ServerPlayer player = (ServerPlayer) entity;
			syncAvatars(player, Collections.singletonList(player));

		}
		
		entity.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent(manips -> {
			if(manips.getLastVeinMineStart() != BlockPos.ZERO) {
				manips.setLastVeinMineStart(BlockPos.ZERO);
			}
		});
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		IKnownManipulations known = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
				.orElseThrow(IllegalStateException::new);
		PacketHandler.CHANNELKNOWNMANIPS.send(PacketDistributor.PLAYER.with(() -> player),
				new KnownManipulationServerPacket(known));

	}

	public static void syncAvatar(Player player, Collection<? extends Player> receivers, boolean isAvatarActive) {
		SyncTrackingAvatarPacket pkt = new SyncTrackingAvatarPacket(player.getId(), isAvatarActive);
		for (Player receiver : receivers) {
			PacketHandler.CHANNELKNOWNMANIPS.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) receiver), pkt);
		}
	}

	private static void syncAvatars(Player player, Collection<? extends Player> receivers) {
		player.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent(manips -> {
			syncAvatar(player, receivers, manips.isAvatarActive());
		});
	}

}