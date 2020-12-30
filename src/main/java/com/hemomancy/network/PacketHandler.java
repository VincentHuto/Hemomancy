package com.hemomancy.network;

import com.hemomancy.Hemomancy;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler {
	private static int networkID = 0;
	public static SimpleChannel INSTANCE;
	public static final SimpleChannel CHANNELBLOODTENDENCY = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(Hemomancy.MOD_ID, "bloodtendencychannel"), () -> "1", "1"::equals, "1"::equals);

	public static void registerChannels() {
		CHANNELBLOODTENDENCY.registerMessage(networkID++, BloodTendencyPacketClient.class,
				BloodTendencyPacketClient::encode, BloodTendencyPacketClient::decode,
				BloodTendencyPacketClient::handle);
		CHANNELBLOODTENDENCY.registerMessage(networkID++, BloodTendencyPacketServer.class,
				BloodTendencyPacketServer::encode, BloodTendencyPacketServer::decode,
				BloodTendencyPacketServer::handle);
	}
}
