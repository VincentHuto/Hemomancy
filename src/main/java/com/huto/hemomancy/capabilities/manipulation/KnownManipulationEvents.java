package com.huto.hemomancy.capabilities.manipulation;

import java.util.List;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.PacketKnownManipulationServer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
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
		for (BloodManipulation s : known) {
			if (s != null)
				player.sendStatusMessage(new StringTextComponent(s.getName()), false);
		}

	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerChangedDimensionEvent event) {
		ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
		List<BloodManipulation> known = KnownManipulationProvider.getPlayerManips(player);
		BloodManipulation selected = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
				.orElseThrow(IllegalStateException::new).getSelectedManip();
		PacketHandler.CHANNELKNOWNMANIPS.send(PacketDistributor.PLAYER.with(() -> player),
				new PacketKnownManipulationServer(known, selected));
		for (BloodManipulation s : known) {
			if (s != null)
				player.sendStatusMessage(new StringTextComponent(s.getName()), false);
		}

	}

}
