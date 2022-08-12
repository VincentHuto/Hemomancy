package com.vincenthuto.hemomancy.network;

import com.vincenthuto.hemomancy.Hemomancy;
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
import com.vincenthuto.hemomancy.network.capa.manips.PacketSyncTrackingAvatar;
import com.vincenthuto.hemomancy.network.capa.manips.PacketTeleportToVein;
import com.vincenthuto.hemomancy.network.capa.manips.PacketUpdateCurrentManip;
import com.vincenthuto.hemomancy.network.capa.manips.PacketUpdateCurrentVein;
import com.vincenthuto.hemomancy.network.capa.manips.PacketUseContManipKey;
import com.vincenthuto.hemomancy.network.capa.manips.PacketUseQuickManipKey;
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
import com.vincenthuto.hemomancy.network.particle.PacketSpawnAvatarParticles;
import com.vincenthuto.hemomancy.network.particle.PacketSpawnBloodClawParticles;
import com.vincenthuto.hemomancy.network.particle.PacketSpawnFlaskParticles;
import com.vincenthuto.hemomancy.network.particle.PacketSpawnLivingToolParticles;
import com.vincenthuto.hemomancy.radial.PacketCharmChange;
import com.vincenthuto.hemomancy.radial.PacketCharmContainerSlot;
import com.vincenthuto.hemomancy.radial.PacketOpenCharm;
import com.vincenthuto.hemomancy.radial.PacketSyncCharmSlotContents;
import com.vincenthuto.hemomancy.radial.RadialInventorySlotChangeMessage;
import com.vincenthuto.hemomancy.radial.SwapItems;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.network.PacketSpawnLightningParticle;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

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

		CHANNELKNOWNMANIPS.registerMessage(networkID++, RadialInventorySlotChangeMessage.class,
				RadialInventorySlotChangeMessage::encode, RadialInventorySlotChangeMessage::decode,
				ServerMessageHandler::handleRadialInventorySlotChangeMessage);

		CHANNELKNOWNMANIPS.registerMessage(networkID++, PacketSpawnLightningParticle.class,
				PacketSpawnLightningParticle::encode, PacketSpawnLightningParticle::decode,
				PacketSpawnLightningParticle::handle);

		CHANNELKNOWNMANIPS
				.messageBuilder(PacketSyncCharmSlotContents.class, networkID++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(PacketSyncCharmSlotContents::encode).decoder(PacketSyncCharmSlotContents::new)
				.consumer(PacketSyncCharmSlotContents::handle).add();

		CHANNELKNOWNMANIPS.messageBuilder(PacketOpenCharm.class, networkID++, NetworkDirection.PLAY_TO_SERVER)
				.encoder(PacketOpenCharm::encode).decoder(PacketOpenCharm::new).consumer(PacketOpenCharm::handle).add();

		CHANNELKNOWNMANIPS.messageBuilder(PacketOpenCharm.class, networkID++, NetworkDirection.PLAY_TO_SERVER)
				.encoder(PacketOpenCharm::encode).decoder(PacketOpenCharm::new).consumer(PacketOpenCharm::handle).add();

		CHANNELKNOWNMANIPS.messageBuilder(PacketCharmContainerSlot.class, networkID++, NetworkDirection.PLAY_TO_SERVER)
				.encoder(PacketCharmContainerSlot::encode).decoder(PacketCharmContainerSlot::new)
				.consumer(PacketCharmContainerSlot::handle).add();

		CHANNELKNOWNMANIPS.messageBuilder(PacketCharmChange.class, networkID++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(PacketCharmChange::encode).decoder(PacketCharmChange::new).consumer(PacketCharmChange::handle)
				.add();

		CHANNELKNOWNMANIPS.messageBuilder(SwapItems.class, networkID++, NetworkDirection.PLAY_TO_SERVER)
				.encoder(SwapItems::encode).decoder(SwapItems::new).consumer(SwapItems::handle).add();

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

		CHANNELKNOWNMANIPS.registerMessage(networkID++, PacketSyncTrackingAvatar.class,
				PacketSyncTrackingAvatar::toBytes, PacketSyncTrackingAvatar::new, PacketSyncTrackingAvatar::handle);
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
		CHANNELPARTICLES.messageBuilder(PacketSpawnAvatarParticles.class, networkID++)
				.decoder(PacketSpawnAvatarParticles::decode).encoder(PacketSpawnAvatarParticles::encode)
				.consumer(PacketSpawnAvatarParticles::handle).add();
		CHANNELPARTICLES.messageBuilder(PacketSpawnBloodClawParticles.class, networkID++)
				.decoder(PacketSpawnBloodClawParticles::decode).encoder(PacketSpawnBloodClawParticles::encode)
				.consumer(PacketSpawnBloodClawParticles::handle).add();
		CHANNELPARTICLES.messageBuilder(PacketSpawnLivingToolParticles.class, networkID++)
				.decoder(PacketSpawnLivingToolParticles::decode).encoder(PacketSpawnLivingToolParticles::encode)
				.consumer(PacketSpawnLivingToolParticles::handle).add();

		CHANNELMORPHLINGJAR.registerMessage(networkID++, PacketJarTogglePickup.class, PacketJarTogglePickup::encode,
				PacketJarTogglePickup::decode, PacketJarTogglePickup::handle);
		CHANNELMORPHLINGJAR.registerMessage(networkID++, PacketOpenJar.class, PacketOpenJar::encode,
				PacketOpenJar::decode, PacketOpenJar::handle);
		CHANNELMORPHLINGJAR.registerMessage(networkID++, PacketToggleJarMessage.class, PacketToggleJarMessage::encode,
				PacketToggleJarMessage::decode, PacketToggleJarMessage::handle);
		CHANNELMORPHLINGJAR.registerMessage(networkID++, PacketOpenStaff.class, PacketOpenStaff::encode,
				PacketOpenStaff::decode, PacketOpenStaff::handle);

		CHANNELMORPHLINGJAR.registerMessage(networkID++, PacketUpdateLivingStaffMorph.class,
				PacketUpdateLivingStaffMorph::encode, PacketUpdateLivingStaffMorph::decode,
				PacketUpdateLivingStaffMorph.Handler::handle);

		CHANNELMORPHLINGJAR.registerMessage(networkID++, PacketChangeMorphKey.class, PacketChangeMorphKey::encode,
				PacketChangeMorphKey::decode, PacketChangeMorphKey.Handler::handle);

	}

	public static void sendAvatarHitParticles(Vec3 pos, ParticleColor color, double radius,
			ResourceKey<Level> dimension) {
		PacketSpawnAvatarParticles msg = new PacketSpawnAvatarParticles(pos, color);
		CHANNELPARTICLES.send(PacketDistributor.NEAR
				.with(() -> new PacketDistributor.TargetPoint(pos.x, pos.y, pos.z, radius, dimension)), msg);
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
