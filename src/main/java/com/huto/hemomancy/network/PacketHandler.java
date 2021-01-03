package com.huto.hemomancy.network;

import com.huto.hemomancy.Hemomancy;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler {
	private static int networkID = 0;
	private static final String PROTOCOL_VERSION = "1";
	public static SimpleChannel INSTANCE;
	public static final SimpleChannel CHANNELBLOODTENDENCY = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(Hemomancy.MOD_ID, "bloodtendencychannel"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	public static final SimpleChannel HANDLER = NetworkRegistry.ChannelBuilder
			.named(new ResourceLocation(Hemomancy.MOD_ID + ("main_channel")))
			.clientAcceptedVersions(PROTOCOL_VERSION::equals).serverAcceptedVersions(PROTOCOL_VERSION::equals)
			.networkProtocolVersion(() -> PROTOCOL_VERSION).simpleChannel();
	
	
	public static void registerChannels() {

		CHANNELBLOODTENDENCY.registerMessage(networkID++, BloodTendencyPacketClient.class,
				BloodTendencyPacketClient::encode, BloodTendencyPacketClient::decode,
				BloodTendencyPacketClient::handle);
		CHANNELBLOODTENDENCY.registerMessage(networkID++, BloodTendencyPacketServer.class,
				BloodTendencyPacketServer::encode, BloodTendencyPacketServer::decode,
				BloodTendencyPacketServer::handle);
	
		HANDLER.registerMessage(networkID++, PacketUpdateChiselRunes.class, PacketUpdateChiselRunes::encode,
				PacketUpdateChiselRunes::decode, PacketUpdateChiselRunes.Handler::handle);
		HANDLER.registerMessage(networkID++, PacketChiselCraftingEvent.class, PacketChiselCraftingEvent::encode,
				PacketChiselCraftingEvent::decode, PacketChiselCraftingEvent.Handler::handle);
		INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(Hemomancy.MOD_ID, "runechannel"), () -> "1.0",
				s -> true, s -> true);
		INSTANCE.registerMessage(networkID++, OpenRunesInvPacket.class, OpenRunesInvPacket::toBytes,
				OpenRunesInvPacket::new, OpenRunesInvPacket::handle);
		INSTANCE.registerMessage(networkID++, OpenNormalInvPacket.class, OpenNormalInvPacket::toBytes,
				OpenNormalInvPacket::new, OpenNormalInvPacket::handle);
		INSTANCE.registerMessage(networkID++, SyncPacket.class, SyncPacket::toBytes, SyncPacket::new,
				SyncPacket::handle);
		
		
	}

	public static SimpleChannel RUNEBINDER = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(Hemomancy.MOD_ID, "runebindernetwork"), () -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	
	public static SimpleChannel registerRuneBinderChannels() {
		RUNEBINDER.messageBuilder(PacketBinderTogglePickup.class, networkID++).decoder(PacketBinderTogglePickup::decode)
				.encoder(PacketBinderTogglePickup::encode).consumer(PacketBinderTogglePickup::handle).add();
		RUNEBINDER.messageBuilder(PacketOpenRuneBinder.class, networkID++).decoder(PacketOpenRuneBinder::decode)
				.encoder(PacketOpenRuneBinder::encode).consumer(PacketOpenRuneBinder::handle).add();
		RUNEBINDER.messageBuilder(PacketToggleBinderMessage.class, networkID++)
				.decoder(PacketToggleBinderMessage::decode).encoder(PacketToggleBinderMessage::encode)
				.consumer(PacketToggleBinderMessage::handle).add();
		return RUNEBINDER;
	}
}
