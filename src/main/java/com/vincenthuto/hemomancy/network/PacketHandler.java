package com.vincenthuto.hemomancy.network;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.network.binder.PacketBinderTogglePickup;
import com.vincenthuto.hemomancy.network.binder.PacketOpenRuneBinder;
import com.vincenthuto.hemomancy.network.binder.PacketToggleBinderMessage;
import com.vincenthuto.hemomancy.network.capa.PacketBloodTendencyClient;
import com.vincenthuto.hemomancy.network.capa.PacketBloodTendencyServer;
import com.vincenthuto.hemomancy.network.capa.PacketBloodVolumeClient;
import com.vincenthuto.hemomancy.network.capa.PacketBloodVolumeServer;
import com.vincenthuto.hemomancy.network.capa.PacketVascularSystemClient;
import com.vincenthuto.hemomancy.network.capa.PacketVascularSystemServer;
import com.vincenthuto.hemomancy.network.capa.manips.PacketChangeSelectedManip;
import com.vincenthuto.hemomancy.network.capa.manips.PacketDisplayKnownManips;
import com.vincenthuto.hemomancy.network.capa.manips.PacketKnownManipulationClient;
import com.vincenthuto.hemomancy.network.capa.manips.PacketKnownManipulationServer;
import com.vincenthuto.hemomancy.network.capa.manips.PacketTeleportToVein;
import com.vincenthuto.hemomancy.network.capa.manips.PacketUpdateCurrentManip;
import com.vincenthuto.hemomancy.network.capa.manips.PacketUpdateCurrentVein;
import com.vincenthuto.hemomancy.network.capa.manips.PacketUseContManipKey;
import com.vincenthuto.hemomancy.network.capa.manips.PacketUseQuickManipKey;
import com.vincenthuto.hemomancy.network.capa.runes.PacketCurvedHornAnimation;
import com.vincenthuto.hemomancy.network.capa.runes.PacketOpenNormalInv;
import com.vincenthuto.hemomancy.network.capa.runes.PacketOpenRunesInv;
import com.vincenthuto.hemomancy.network.keybind.PacketBloodCraftingKeyPress;
import com.vincenthuto.hemomancy.network.keybind.PacketBloodFormationKeyPress;
import com.vincenthuto.hemomancy.network.morphling.PacketChangeMorphKey;
import com.vincenthuto.hemomancy.network.morphling.PacketJarTogglePickup;
import com.vincenthuto.hemomancy.network.morphling.PacketOpenJar;
import com.vincenthuto.hemomancy.network.morphling.PacketOpenStaff;
import com.vincenthuto.hemomancy.network.morphling.PacketToggleJarMessage;
import com.vincenthuto.hemomancy.network.particle.PacketAirBloodDraw;
import com.vincenthuto.hemomancy.network.particle.PacketEntityHitParticle;
import com.vincenthuto.hemomancy.network.particle.PacketGroundBloodDraw;
import com.vincenthuto.hemomancy.network.particle.PacketSpawnBloodClawParticles;
import com.vincenthuto.hemomancy.network.particle.PacketSpawnFlaskParticles;
import com.vincenthuto.hemomancy.network.particle.PacketSpawnLivingToolParticles;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;

public class PacketHandler {
	private static int networkID = 0;
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel CHANNELBLOODTENDENCY = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(Hemomancy.MOD_ID, "bloodtendencychannel"), () -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	public static final SimpleChannel CHANNELVASCULARSYSTEM = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(Hemomancy.MOD_ID, "vascularsystemchannel"), () -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	public static final SimpleChannel CHANNELBLOODVOLUME = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(Hemomancy.MOD_ID, "bloodvolumechannel"), () -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	public static final SimpleChannel CHANNELMAIN = NetworkRegistry.ChannelBuilder
			.named(new ResourceLocation(Hemomancy.MOD_ID + ("main_channel")))
			.clientAcceptedVersions(PROTOCOL_VERSION::equals).serverAcceptedVersions(PROTOCOL_VERSION::equals)
			.networkProtocolVersion(() -> PROTOCOL_VERSION).simpleChannel();
	public static final SimpleChannel CHANNELKNOWNMANIPS = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(Hemomancy.MOD_ID, "knownmanipulationchannel"), () -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	public static final SimpleChannel CHANNELPARTICLES = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(Hemomancy.MOD_ID, "particlechannel"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals);
	public static SimpleChannel CHANNELRUNES = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(Hemomancy.MOD_ID, "runechannel"), () -> PROTOCOL_VERSION, s -> true, s -> true);
	public static SimpleChannel CHANNELMORPHLINGJAR = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(Hemomancy.MOD_ID, "morphlingjarchannel"), () -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	public static SimpleChannel CHANNELRUNEBINDER = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(Hemomancy.MOD_ID, "runebinderchannel"), () -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

	public static void registerChannels() {

		CHANNELMAIN.registerMessage(networkID++, PacketUpdateChiselRunes.class, PacketUpdateChiselRunes::encode,
				PacketUpdateChiselRunes::decode, PacketUpdateChiselRunes.Handler::handle);
		CHANNELMAIN.registerMessage(networkID++, PacketChangeMorphKey.class, PacketChangeMorphKey::encode,
				PacketChangeMorphKey::decode, PacketChangeMorphKey.Handler::handle);
		CHANNELMAIN.registerMessage(networkID++, PacketChiselCraftingEvent.class, PacketChiselCraftingEvent::encode,
				PacketChiselCraftingEvent::decode, PacketChiselCraftingEvent.Handler::handle);
		CHANNELMAIN.registerMessage(networkID++, PacketUpdateLivingStaffMorph.class,
				PacketUpdateLivingStaffMorph::encode, PacketUpdateLivingStaffMorph::decode,
				PacketUpdateLivingStaffMorph.Handler::handle);
		CHANNELMAIN.registerMessage(networkID++, PacketClearRecallerState.class, PacketClearRecallerState::encode,
				PacketClearRecallerState::decode, PacketClearRecallerState.Handler::handle);

		CHANNELBLOODTENDENCY.registerMessage(networkID++, PacketBloodTendencyClient.class,
				PacketBloodTendencyClient::encode, PacketBloodTendencyClient::decode,
				PacketBloodTendencyClient::handle);
		CHANNELBLOODTENDENCY.registerMessage(networkID++, PacketBloodTendencyServer.class,
				PacketBloodTendencyServer::encode, PacketBloodTendencyServer::decode,
				PacketBloodTendencyServer::handle);

		CHANNELKNOWNMANIPS.registerMessage(networkID++, PacketKnownManipulationClient.class,
				PacketKnownManipulationClient::encode, PacketKnownManipulationClient::decode,
				PacketKnownManipulationClient::handle);
		CHANNELKNOWNMANIPS.registerMessage(networkID++, PacketKnownManipulationServer.class,
				PacketKnownManipulationServer::encode, PacketKnownManipulationServer::decode,
				PacketKnownManipulationServer::handle);
		CHANNELKNOWNMANIPS.registerMessage(networkID++, PacketDisplayKnownManips.class,
				PacketDisplayKnownManips::encode, PacketDisplayKnownManips::decode, PacketDisplayKnownManips::handle);
		CHANNELKNOWNMANIPS.registerMessage(networkID++, PacketChangeSelectedManip.class,
				PacketChangeSelectedManip::encode, PacketChangeSelectedManip::decode,
				PacketChangeSelectedManip::handle);
		CHANNELKNOWNMANIPS.registerMessage(networkID++, PacketUseQuickManipKey.class, PacketUseQuickManipKey::encode,
				PacketUseQuickManipKey::decode, PacketUseQuickManipKey::handle);
		CHANNELKNOWNMANIPS.registerMessage(networkID++, PacketUseContManipKey.class, PacketUseContManipKey::encode,
				PacketUseContManipKey::decode, PacketUseContManipKey::handle);
		CHANNELKNOWNMANIPS.registerMessage(networkID++, PacketUpdateCurrentManip.class,
				PacketUpdateCurrentManip::encode, PacketUpdateCurrentManip::decode,
				PacketUpdateCurrentManip.Handler::handle);
		CHANNELKNOWNMANIPS.registerMessage(networkID++, PacketTeleportToVein.class, PacketTeleportToVein::encode,
				PacketTeleportToVein::decode, PacketTeleportToVein.Handler::handle);

		CHANNELKNOWNMANIPS.registerMessage(networkID++, PacketUpdateCurrentVein.class, PacketUpdateCurrentVein::encode,
				PacketUpdateCurrentVein::decode, PacketUpdateCurrentVein.Handler::handle);
		CHANNELVASCULARSYSTEM.registerMessage(networkID++, PacketVascularSystemClient.class,
				PacketVascularSystemClient::encode, PacketVascularSystemClient::decode,
				PacketVascularSystemClient::handle);
		CHANNELVASCULARSYSTEM.registerMessage(networkID++, PacketVascularSystemServer.class,
				PacketVascularSystemServer::encode, PacketVascularSystemServer::decode,
				PacketVascularSystemServer::handle);

		CHANNELBLOODVOLUME.registerMessage(networkID++, PacketBloodVolumeClient.class, PacketBloodVolumeClient::encode,
				PacketBloodVolumeClient::decode, PacketBloodVolumeClient::handle);
		CHANNELBLOODVOLUME.registerMessage(networkID++, PacketBloodVolumeServer.class, PacketBloodVolumeServer::encode,
				PacketBloodVolumeServer::decode, PacketBloodVolumeServer::handle);
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

		CHANNELPARTICLES.messageBuilder(PacketSpawnFlaskParticles.class, networkID++)
				.decoder(PacketSpawnFlaskParticles::decode).encoder(PacketSpawnFlaskParticles::encode)
				.consumer(PacketSpawnFlaskParticles::handle).add();
		CHANNELPARTICLES.messageBuilder(PacketSpawnBloodClawParticles.class, networkID++)
				.decoder(PacketSpawnBloodClawParticles::decode).encoder(PacketSpawnBloodClawParticles::encode)
				.consumer(PacketSpawnBloodClawParticles::handle).add();
		CHANNELPARTICLES.messageBuilder(PacketSpawnLivingToolParticles.class, networkID++)
				.decoder(PacketSpawnLivingToolParticles::decode).encoder(PacketSpawnLivingToolParticles::encode)
				.consumer(PacketSpawnLivingToolParticles::handle).add();

		CHANNELRUNES.registerMessage(networkID++, PacketOpenRunesInv.class, PacketOpenRunesInv::decode,
				PacketOpenRunesInv::new, PacketOpenRunesInv::handle);
		CHANNELRUNES.registerMessage(networkID++, PacketOpenNormalInv.class, PacketOpenNormalInv::decode,
				PacketOpenNormalInv::new, PacketOpenNormalInv::handle);
		CHANNELRUNES.registerMessage(networkID++, PacketRuneSync.class, PacketRuneSync::toBytes, PacketRuneSync::new,
				PacketRuneSync::handle);
		CHANNELRUNES.registerMessage(networkID++, PacketCurvedHornAnimation.class, PacketCurvedHornAnimation::decode,
				PacketCurvedHornAnimation::new, PacketCurvedHornAnimation::handle);


		CHANNELRUNEBINDER.registerMessage(networkID++, PacketBinderTogglePickup.class, PacketBinderTogglePickup::encode,
				PacketBinderTogglePickup::decode, PacketBinderTogglePickup::handle);
		CHANNELRUNEBINDER.registerMessage(networkID++, PacketOpenRuneBinder.class, PacketOpenRuneBinder::encode,
				PacketOpenRuneBinder::decode, PacketOpenRuneBinder::handle);
		CHANNELRUNEBINDER.registerMessage(networkID++, PacketToggleBinderMessage.class,
				PacketToggleBinderMessage::encode, PacketToggleBinderMessage::decode,
				PacketToggleBinderMessage::handle);

		CHANNELMORPHLINGJAR.registerMessage(networkID++, PacketJarTogglePickup.class, PacketJarTogglePickup::encode,
				PacketJarTogglePickup::decode, PacketJarTogglePickup::handle);
		CHANNELMORPHLINGJAR.registerMessage(networkID++, PacketOpenJar.class, PacketOpenJar::encode,
				PacketOpenJar::decode, PacketOpenJar::handle);
		CHANNELMORPHLINGJAR.registerMessage(networkID++, PacketToggleJarMessage.class, PacketToggleJarMessage::encode,
				PacketToggleJarMessage::decode, PacketToggleJarMessage::handle);
		CHANNELMORPHLINGJAR.registerMessage(networkID++, PacketOpenStaff.class, PacketOpenStaff::encode,
				PacketOpenStaff::decode, PacketOpenStaff::handle);

	}

	public static void sendBloodFlaskParticles(Vec3 pos, ParticleColor color, double radius,
			ResourceKey<Level> dimension) {
		PacketSpawnFlaskParticles msg = new PacketSpawnFlaskParticles(pos, color);
		CHANNELPARTICLES.send(PacketDistributor.NEAR
				.with(() -> new PacketDistributor.TargetPoint(pos.x, pos.y, pos.z, radius, dimension)), msg);
	}

	public static void sendClawParticles(Vec3 pos, ParticleColor color, double radius, ResourceKey<Level> dimension) {
		PacketSpawnBloodClawParticles msg = new PacketSpawnBloodClawParticles(pos, color);
		CHANNELPARTICLES.send(PacketDistributor.NEAR
				.with(() -> new PacketDistributor.TargetPoint(pos.x, pos.y, pos.z, radius, dimension)), msg);
	}

	public static void sendLivingToolBreakParticles(Vec3 pos, ParticleColor color, double radius,
			ResourceKey<Level> dimension) {
		PacketSpawnLivingToolParticles msg = new PacketSpawnLivingToolParticles(pos, color);
		CHANNELPARTICLES.send(PacketDistributor.NEAR
				.with(() -> new PacketDistributor.TargetPoint(pos.x, pos.y, pos.z, radius, dimension)), msg);
	}

}
