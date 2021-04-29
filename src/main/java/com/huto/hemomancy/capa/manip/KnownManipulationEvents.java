package com.huto.hemomancy.capa.manip;

import java.util.List;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.event.ClientEventSubscriber;
import com.huto.hemomancy.init.ManipulationInit;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.network.PacketEntityHitParticle;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.PacketKnownManipulationServer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;

public class KnownManipulationEvents {
	@SubscribeEvent
	public static void attachCapabilitiesEntity(final AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof PlayerEntity) {
			event.addCapability(new ResourceLocation(Hemomancy.MOD_ID, "knownmanipulations"),
					new KnownManipulationProvider());
		}
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
		List<BloodManipulation> known = KnownManipulationProvider.getPlayerManips(player);
		BloodManipulation selected = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
				.orElseThrow(IllegalStateException::new).getSelectedManip();
		PacketHandler.CHANNELKNOWNMANIPS.send(PacketDistributor.PLAYER.with(() -> player),
				new PacketKnownManipulationServer(known, selected));
//		for (BloodManipulation s : known) {
//			if (s != null)
//				player.sendStatusMessage(new StringTextComponent(s.getProperName()), false);
//		}

	}

	@SubscribeEvent
	public static void playerDeath(PlayerEvent.Clone event) {
		IKnownManipulations bloodVolumeOld = event.getOriginal().getCapability(KnownManipulationProvider.MANIP_CAPA)
				.orElseThrow(IllegalStateException::new);
		IKnownManipulations bloodVolumeNew = event.getEntity().getCapability(KnownManipulationProvider.MANIP_CAPA)
				.orElseThrow(IllegalStateException::new);
		bloodVolumeNew.setKnownManips(bloodVolumeOld.getKnownManips());
		bloodVolumeNew.setSelectedManip(bloodVolumeOld.getSelectedManip());
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerChangedDimensionEvent event) {
		ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
		List<BloodManipulation> known = KnownManipulationProvider.getPlayerManips(player);
		BloodManipulation selected = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
				.orElseThrow(IllegalStateException::new).getSelectedManip();
		PacketHandler.CHANNELKNOWNMANIPS.send(PacketDistributor.PLAYER.with(() -> player),
				new PacketKnownManipulationServer(known, selected));
//		for (BloodManipulation s : known) {
//			if (s != null)
//				player.sendStatusMessage(new StringTextComponent(s.getProperName()), false);
//		}

	}

	@SubscribeEvent
	public static void onPlayerDamage(LivingDamageEvent e) {

		// Radiant Protection
		if (e.getEntityLiving() instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) e.getEntityLiving();
			if (ClientEventSubscriber.useContManip.isKeyDown()) {
				if (!player.world.isRemote) {
					IKnownManipulations known = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
							.orElseThrow(NullPointerException::new);
					if (known.getSelectedManip() != null) {
						BloodManipulation selectedManip = ManipulationInit
								.getByName(known.getSelectedManip().getName());
						if (selectedManip != null) {
							if (selectedManip.getName() == ManipulationInit.sanguine_ward.get().getName()) {
								double dist = e.getEntityLiving().getDistance(player);
								RayTraceResult trace = e.getEntityLiving().pick(dist, 0, false);
								PacketHandler.CHANNELBLOODVOLUME.sendToServer(new PacketEntityHitParticle(
										trace.getHitVec().x, trace.getHitVec().y, trace.getHitVec().z));
								e.setAmount((float) (e.getAmount() * 0));
							}
						}
					}
				}

			}
		}
	}

}
