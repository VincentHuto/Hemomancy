package com.huto.hemomancy.network;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.model.animation.AnimationPacket;
import com.huto.hemomancy.network.binder.PacketBinderTogglePickup;
import com.huto.hemomancy.network.binder.PacketOpenRuneBinder;
import com.huto.hemomancy.network.binder.PacketToggleBinderMessage;
import com.huto.hemomancy.network.capa.BloodTendencyPacketClient;
import com.huto.hemomancy.network.capa.BloodTendencyPacketServer;
import com.huto.hemomancy.network.capa.BloodVolumePacketClient;
import com.huto.hemomancy.network.capa.BloodVolumePacketServer;
import com.huto.hemomancy.network.capa.OpenNormalInvPacket;
import com.huto.hemomancy.network.capa.OpenRunesInvPacket;
import com.huto.hemomancy.network.crafting.PacketBloodCraftingKeyPress;
import com.huto.hemomancy.network.crafting.PacketBloodFormationKeyPress;
import com.huto.hemomancy.network.jar.PacketJarTogglePickup;
import com.huto.hemomancy.network.jar.PacketOpenJar;
import com.huto.hemomancy.network.jar.PacketOpenStaff;
import com.huto.hemomancy.network.jar.PacketToggleJarMessage;
import com.huto.hemomancy.particle.ParticleColor;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler {
	private static int networkID = 0;
	private static final String PROTOCOL_VERSION = "1";
	public static SimpleChannel INSTANCE;
	public static final SimpleChannel CHANNELBLOODTENDENCY = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(Hemomancy.MOD_ID, "bloodtendencychannel"), () -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	public static final SimpleChannel CHANNELVASCULARSYSTEM = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(Hemomancy.MOD_ID, "vascularsystemchannel"), () -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	public static final SimpleChannel CHANNELBLOODVOLUME = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(Hemomancy.MOD_ID, "bloodvolumechannel"), () -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	public static final SimpleChannel HANDLER = NetworkRegistry.ChannelBuilder
			.named(new ResourceLocation(Hemomancy.MOD_ID + ("main_channel")))
			.clientAcceptedVersions(PROTOCOL_VERSION::equals).serverAcceptedVersions(PROTOCOL_VERSION::equals)
			.networkProtocolVersion(() -> PROTOCOL_VERSION).simpleChannel();
	public static final SimpleChannel ANIMATIONS = NetworkRegistry.ChannelBuilder
			.named(new ResourceLocation(Hemomancy.MOD_ID, "animchannel"))
			.clientAcceptedVersions(PROTOCOL_VERSION::equals).serverAcceptedVersions(PROTOCOL_VERSION::equals)
			.networkProtocolVersion(() -> PROTOCOL_VERSION).simpleChannel();

	public static void registerChannels() {

		ANIMATIONS.messageBuilder(AnimationPacket.class, networkID++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(AnimationPacket::encode).decoder(AnimationPacket::new).consumer(AnimationPacket::handle).add();

		CHANNELBLOODTENDENCY.registerMessage(networkID++, BloodTendencyPacketClient.class,
				BloodTendencyPacketClient::encode, BloodTendencyPacketClient::decode,
				BloodTendencyPacketClient::handle);
		CHANNELBLOODTENDENCY.registerMessage(networkID++, BloodTendencyPacketServer.class,
				BloodTendencyPacketServer::encode, BloodTendencyPacketServer::decode,
				BloodTendencyPacketServer::handle);

		CHANNELVASCULARSYSTEM.registerMessage(networkID++, VascularSystemPacketClient.class,
				VascularSystemPacketClient::encode, VascularSystemPacketClient::decode,
				VascularSystemPacketClient::handle);
		CHANNELVASCULARSYSTEM.registerMessage(networkID++, VascularSystemPacketServer.class,
				VascularSystemPacketServer::encode, VascularSystemPacketServer::decode,
				VascularSystemPacketServer::handle);

		CHANNELBLOODVOLUME.registerMessage(networkID++, BloodVolumePacketClient.class, BloodVolumePacketClient::encode,
				BloodVolumePacketClient::decode, BloodVolumePacketClient::handle);
		CHANNELBLOODVOLUME.registerMessage(networkID++, BloodVolumePacketServer.class, BloodVolumePacketServer::encode,
				BloodVolumePacketServer::decode, BloodVolumePacketServer::handle);
		CHANNELBLOODVOLUME.registerMessage(networkID++, PacketBloodFormationKeyPress.class,
				PacketBloodFormationKeyPress::encode, PacketBloodFormationKeyPress::decode,
				PacketBloodFormationKeyPress::handle);
		CHANNELBLOODVOLUME.registerMessage(networkID++, PacketBloodCraftingKeyPress.class,
				PacketBloodCraftingKeyPress::encode, PacketBloodCraftingKeyPress::decode,
				PacketBloodCraftingKeyPress::handle);
		CHANNELBLOODVOLUME.messageBuilder(PacketGroundBloodDraw.class, networkID++)
				.decoder(PacketGroundBloodDraw::decode).encoder(PacketGroundBloodDraw::encode)
				.consumer(PacketGroundBloodDraw::handle).add();
		CHANNELBLOODVOLUME.messageBuilder(PacketEntityHitParticle.class, networkID++)
				.decoder(PacketEntityHitParticle::decode).encoder(PacketEntityHitParticle::encode)
				.consumer(PacketEntityHitParticle::handle).add();
		CHANNELBLOODVOLUME.messageBuilder(PacketAirBloodDraw.class, networkID++).decoder(PacketAirBloodDraw::decode)
				.encoder(PacketAirBloodDraw::encode).consumer(PacketAirBloodDraw::handle).add();
		CHANNELBLOODVOLUME.registerMessage(networkID++, PacketSpawnLightningSimple.class, PacketSpawnLightningSimple::encode,
				PacketSpawnLightningSimple::decode, PacketSpawnLightningSimple::handle);
		CHANNELBLOODVOLUME.registerMessage(networkID++, PacketSpawnLightningComplex.class, PacketSpawnLightningComplex::encode,
				PacketSpawnLightningComplex::decode, PacketSpawnLightningComplex::handle);
		HANDLER.registerMessage(networkID++, PacketUpdateChiselRunes.class, PacketUpdateChiselRunes::encode,
				PacketUpdateChiselRunes::decode, PacketUpdateChiselRunes.Handler::handle);
		HANDLER.registerMessage(networkID++, PacketChangeMorphKey.class, PacketChangeMorphKey::encode,
				PacketChangeMorphKey::decode, PacketChangeMorphKey.Handler::handle);
		HANDLER.registerMessage(networkID++, PacketChiselCraftingEvent.class, PacketChiselCraftingEvent::encode,
				PacketChiselCraftingEvent::decode, PacketChiselCraftingEvent.Handler::handle);
		HANDLER.registerMessage(networkID++, PacketUpdateLivingStaffMorph.class, PacketUpdateLivingStaffMorph::encode,
				PacketUpdateLivingStaffMorph::decode, PacketUpdateLivingStaffMorph.Handler::handle);
		INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(Hemomancy.MOD_ID, "runechannel"),
				() -> PROTOCOL_VERSION, s -> true, s -> true);
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

	public static SimpleChannel MORPHLINGJAR = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(Hemomancy.MOD_ID, "morphlingjarnetwork"), () -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

	public static SimpleChannel registerMorphlingJarChannels() {
		MORPHLINGJAR.messageBuilder(PacketJarTogglePickup.class, networkID++).decoder(PacketJarTogglePickup::decode)
				.encoder(PacketJarTogglePickup::encode).consumer(PacketJarTogglePickup::handle).add();
		MORPHLINGJAR.messageBuilder(PacketOpenJar.class, networkID++).decoder(PacketOpenJar::decode)
				.encoder(PacketOpenJar::encode).consumer(PacketOpenJar::handle).add();
		MORPHLINGJAR.messageBuilder(PacketToggleJarMessage.class, networkID++).decoder(PacketToggleJarMessage::decode)
				.encoder(PacketToggleJarMessage::encode).consumer(PacketToggleJarMessage::handle).add();
		MORPHLINGJAR.messageBuilder(PacketOpenStaff.class, networkID++).decoder(PacketOpenStaff::decode)
				.encoder(PacketOpenStaff::encode).consumer(PacketOpenStaff::handle).add();
		return MORPHLINGJAR;
	}

	public static void sendLightningSpawn(Vector3d vec, Vector3d speedVec, float radius, ParticleColor color,
			RegistryKey<World> dimension) {
		PacketSpawnLightningSimple msg = new PacketSpawnLightningSimple(vec,speedVec, color);
		CHANNELBLOODVOLUME.send(PacketDistributor.NEAR
				.with(() -> new PacketDistributor.TargetPoint(vec.x, vec.y, vec.z, (double) radius, dimension)), msg);

	}
	public static void sendLightningSpawn(Vector3d vec, Vector3d speedVec, float radius, ParticleColor color,int speed,int maxAge,
			RegistryKey<World> dimension) {
		PacketSpawnLightningComplex msg = new PacketSpawnLightningComplex(vec,speedVec, color,speed,maxAge);
		CHANNELBLOODVOLUME.send(PacketDistributor.NEAR
				.with(() -> new PacketDistributor.TargetPoint(vec.x, vec.y, vec.z, (double) radius, dimension)), msg);

	}
}
