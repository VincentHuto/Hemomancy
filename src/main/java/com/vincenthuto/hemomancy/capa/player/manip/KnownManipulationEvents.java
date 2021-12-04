package com.vincenthuto.hemomancy.capa.player.manip;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.manips.PacketKnownManipulationServer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

public class KnownManipulationEvents {
	@SubscribeEvent
	public static void attachCapabilitiesEntity(final AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Player) {
			event.addCapability(new ResourceLocation(Hemomancy.MOD_ID, "knownmanipulations"),
					new KnownManipulationProvider());
		}
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer player = (ServerPlayer) event.getPlayer();
		IKnownManipulations known = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
				.orElseThrow(IllegalStateException::new);
		PacketHandler.CHANNELKNOWNMANIPS.send(PacketDistributor.PLAYER.with(() -> player),
				new PacketKnownManipulationServer(known));

	}

	@SubscribeEvent
	public static void playerDeath(PlayerEvent.Clone event) {
		Player peorig = event.getOriginal();
		Player playernew = event.getPlayer();
		if (event.isWasDeath()) {
			peorig.reviveCaps();
			IKnownManipulations bloodTendencyNew = playernew.getCapability(KnownManipulationProvider.MANIP_CAPA)
					.orElseThrow(IllegalStateException::new);
			bloodTendencyNew.setKnownManips(peorig.getCapability(KnownManipulationProvider.MANIP_CAPA)
					.orElseThrow(IllegalArgumentException::new).getKnownManips());
			bloodTendencyNew.setSelectedManip(peorig.getCapability(KnownManipulationProvider.MANIP_CAPA)
					.orElseThrow(IllegalArgumentException::new).getSelectedManip());
			bloodTendencyNew.setVeinList(peorig.getCapability(KnownManipulationProvider.MANIP_CAPA)
					.orElseThrow(IllegalArgumentException::new).getVeinList());
			bloodTendencyNew.setSelectedVein(peorig.getCapability(KnownManipulationProvider.MANIP_CAPA)
					.orElseThrow(IllegalArgumentException::new).getSelectedVein());

			peorig.invalidateCaps();
		}

	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerChangedDimensionEvent event) {
		ServerPlayer player = (ServerPlayer) event.getPlayer();
		IKnownManipulations known = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
				.orElseThrow(IllegalStateException::new);
		PacketHandler.CHANNELKNOWNMANIPS.send(PacketDistributor.PLAYER.with(() -> player),
				new PacketKnownManipulationServer(known));
	}

	@SubscribeEvent
	public static void onPlayerDamage(LivingDamageEvent e) {

//		// Radiant Protection
//		if (e.getEntityLiving() instanceof Player) {
//			Player player = (Player) e.getEntityLiving();
//			if (ClientEventSubscriber.useContManip.isKeyDown()) {
//				if (!player.world.isRemote) {
//					IKnownManipulations known = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
//							.orElseThrow(NullPointerException::new);
//					if (known.getSelectedManip() != null) {
//						BloodManipulation selectedManip = ManipulationInit
//								.getByName(known.getSelectedManip().getName());
//						if (selectedManip != null) {
//							if (selectedManip.getName() == ManipulationInit.sanguine_ward.get().getName()) {
//								double dist = e.getEntityLiving().getDistance(player);
//								HitResult trace = e.getEntityLiving().pick(dist, 0, false);
//								PacketHandler.CHANNELBLOODVOLUME.sendToServer(new PacketEntityHitParticle(
//										trace.getHitVec().x, trace.getHitVec().y, trace.getHitVec().z));
//								e.setAmount((float) (e.getAmount() * 0));
//							}
//						}
//					}
//				}
//
//			}
//		}
//	}
	}

}