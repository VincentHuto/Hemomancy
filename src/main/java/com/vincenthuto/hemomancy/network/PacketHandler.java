package com.vincenthuto.hemomancy.network;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.gui.radial.IRadialInventorySelect;
import com.vincenthuto.hemomancy.network.capa.BloodTendencyClientPacket;
import com.vincenthuto.hemomancy.network.capa.BloodTendencyServerPacket;
import com.vincenthuto.hemomancy.network.capa.BloodVolumeClientPacket;
import com.vincenthuto.hemomancy.network.capa.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.network.capa.PacketCurvedHornAnimation;
import com.vincenthuto.hemomancy.network.capa.PacketGourdRuneSync;
import com.vincenthuto.hemomancy.network.capa.PacketOpenNormalInv;
import com.vincenthuto.hemomancy.network.capa.PacketOpenRunesInv;
import com.vincenthuto.hemomancy.network.capa.PacketRuneSync;
import com.vincenthuto.hemomancy.network.capa.VascularSystemClientPacket;
import com.vincenthuto.hemomancy.network.capa.VascularSystemServerPacket;
import com.vincenthuto.hemomancy.network.capa.manips.ChangeSelectedManipPacket;
import com.vincenthuto.hemomancy.network.capa.manips.DisplayKnownManipsPacket;
import com.vincenthuto.hemomancy.network.capa.manips.KnownManipulationClientPacket;
import com.vincenthuto.hemomancy.network.capa.manips.KnownManipulationServerPacket;
import com.vincenthuto.hemomancy.network.capa.manips.StartCentrifugeButtonPacket;
import com.vincenthuto.hemomancy.network.capa.manips.SyncTrackingAvatarPacket;
import com.vincenthuto.hemomancy.network.capa.manips.TeleportToVeinPacket;
import com.vincenthuto.hemomancy.network.capa.manips.UpdateCurrentManipPacket;
import com.vincenthuto.hemomancy.network.capa.manips.UpdateCurrentVeinPacket;
import com.vincenthuto.hemomancy.network.capa.manips.UseContManipKeyPacket;
import com.vincenthuto.hemomancy.network.capa.manips.UseQuickManipKeyPacket;
import com.vincenthuto.hemomancy.network.keybind.BloodCraftingKeyPressPacket;
import com.vincenthuto.hemomancy.network.keybind.BloodFormationKeyPressPacket;
import com.vincenthuto.hemomancy.network.keybind.RadialInventorySlotChangePacket;
import com.vincenthuto.hemomancy.network.morphling.ChangeMorphKeyPacket;
import com.vincenthuto.hemomancy.network.morphling.JarTogglePickupPacket;
import com.vincenthuto.hemomancy.network.morphling.OpenLivingStaffPacket;
import com.vincenthuto.hemomancy.network.morphling.OpenMorphlingJarPacket;
import com.vincenthuto.hemomancy.network.morphling.PacketUpdateLivingStaffMorph;
import com.vincenthuto.hemomancy.network.morphling.ToggleMorphlingJarMessagePacket;
import com.vincenthuto.hemomancy.network.particle.AirBloodDrawPacket;
import com.vincenthuto.hemomancy.network.particle.EntityHitParticlePacket;
import com.vincenthuto.hemomancy.network.particle.GroundBloodDrawPacket;
import com.vincenthuto.hemomancy.network.particle.SpawnAvatarParticlesPacket;
import com.vincenthuto.hemomancy.network.particle.SpawnBloodClawParticlesPacket;
import com.vincenthuto.hemomancy.network.particle.SpawnFlaskParticlesPacket;
import com.vincenthuto.hemomancy.network.particle.SpawnLivingToolParticlesPacket;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.network.PacketSpawnLightningParticle;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
	private static int networkID = 0;
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel CHANNELBLOODTENDENCY = NetworkRegistry.newSimpleChannel(
			Hemomancy.rloc("bloodtendencychannel"), () -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	public static final SimpleChannel CHANNELVASCULARSYSTEM = NetworkRegistry.newSimpleChannel(
			Hemomancy.rloc("vascularsystemchannel"), () -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	public static final SimpleChannel CHANNELBLOODVOLUME = NetworkRegistry.newSimpleChannel(
			Hemomancy.rloc("bloodvolumechannel"), () -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	public static final SimpleChannel CHANNELKNOWNMANIPS = NetworkRegistry.newSimpleChannel(
			Hemomancy.rloc("knownmanipulationchannel"), () -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	public static final SimpleChannel CHANNELPARTICLES = NetworkRegistry.newSimpleChannel(
			Hemomancy.rloc("particlechannel"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals);
	public static SimpleChannel CHANNELRUNES = NetworkRegistry.newSimpleChannel(
			Hemomancy.rloc("runechannel"), () -> PROTOCOL_VERSION, s -> true, s -> true);
	public static SimpleChannel CHANNELMORPHLINGJAR = NetworkRegistry.newSimpleChannel(
			Hemomancy.rloc("morphlingjarchannel"), () -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	public static SimpleChannel CHANNELRUNEBINDER = NetworkRegistry.newSimpleChannel(
			Hemomancy.rloc("runebinderchannel"), () -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

	public static void handleRadialInventorySlotChangeMessage(RadialInventorySlotChangePacket message,
			Supplier<NetworkEvent.Context> ctxSupplier) {
		NetworkEvent.Context ctx = ctxSupplier.get();
		if (!PacketHandler.validateBasics(message, ctx)) {
			return;
		}
		ServerPlayer sendingPlayer = ctx.getSender();
		if (sendingPlayer == null) {
			Hemomancy.LOGGER.error("EntityPlayerMP was null when RadialInventorySlotChangeMessage was received");
			return;
		}
		ctx.enqueueWork(() -> {
			ItemStack stack;
			stack = message.isOffhand() ? sendingPlayer.getOffhandItem() : sendingPlayer.getMainHandItem();
			if (stack.getItem() instanceof IRadialInventorySelect) {
				((IRadialInventorySelect) stack.getItem()).setSlot(sendingPlayer, stack, message.getSlot(),
						message.isOffhand(), false);
			}
		});
	}

	public static void registerChannels() {

		CHANNELRUNES.registerMessage(networkID++, PacketOpenRunesInv.class, PacketOpenRunesInv::decode,
				PacketOpenRunesInv::new, PacketOpenRunesInv::handle);
		CHANNELRUNES.registerMessage(networkID++, PacketOpenNormalInv.class, PacketOpenNormalInv::decode,
				PacketOpenNormalInv::new, PacketOpenNormalInv::handle);
		CHANNELRUNES.registerMessage(networkID++, PacketRuneSync.class, PacketRuneSync::toBytes, PacketRuneSync::new,
				PacketRuneSync::handle);
		CHANNELRUNES.registerMessage(networkID++, PacketGourdRuneSync.class, PacketGourdRuneSync::toBytes,
				PacketGourdRuneSync::new, PacketGourdRuneSync::handle);

		CHANNELRUNES.registerMessage(networkID++, PacketCurvedHornAnimation.class, PacketCurvedHornAnimation::decode,
				PacketCurvedHornAnimation::new, PacketCurvedHornAnimation::handle);

		CHANNELKNOWNMANIPS.registerMessage(networkID++, RadialInventorySlotChangePacket.class,
				RadialInventorySlotChangePacket::encode, RadialInventorySlotChangePacket::decode,
				PacketHandler::handleRadialInventorySlotChangeMessage);

		CHANNELKNOWNMANIPS.registerMessage(networkID++, PacketSpawnLightningParticle.class,
				PacketSpawnLightningParticle::encode, PacketSpawnLightningParticle::decode,
				PacketSpawnLightningParticle::handle);

		CHANNELBLOODTENDENCY.registerMessage(networkID++, BloodTendencyClientPacket.class,
				BloodTendencyClientPacket::encode, BloodTendencyClientPacket::decode,
				BloodTendencyClientPacket::handle);
		CHANNELBLOODTENDENCY.registerMessage(networkID++, BloodTendencyServerPacket.class,
				BloodTendencyServerPacket::encode, BloodTendencyServerPacket::decode,
				BloodTendencyServerPacket::handle);

		CHANNELKNOWNMANIPS.registerMessage(networkID++, KnownManipulationClientPacket.class,
				KnownManipulationClientPacket::encode, KnownManipulationClientPacket::decode,
				KnownManipulationClientPacket::handle);
		CHANNELKNOWNMANIPS.registerMessage(networkID++, KnownManipulationServerPacket.class,
				KnownManipulationServerPacket::encode, KnownManipulationServerPacket::decode,
				KnownManipulationServerPacket::handle);
		CHANNELKNOWNMANIPS.registerMessage(networkID++, DisplayKnownManipsPacket.class,
				DisplayKnownManipsPacket::encode, DisplayKnownManipsPacket::decode, DisplayKnownManipsPacket::handle);
		CHANNELKNOWNMANIPS.registerMessage(networkID++, ChangeSelectedManipPacket.class,
				ChangeSelectedManipPacket::encode, ChangeSelectedManipPacket::decode,
				ChangeSelectedManipPacket::handle);
		CHANNELKNOWNMANIPS.registerMessage(networkID++, UseQuickManipKeyPacket.class, UseQuickManipKeyPacket::encode,
				UseQuickManipKeyPacket::decode, UseQuickManipKeyPacket::handle);
		CHANNELKNOWNMANIPS.registerMessage(networkID++, UseContManipKeyPacket.class, UseContManipKeyPacket::encode,
				UseContManipKeyPacket::decode, UseContManipKeyPacket::handle);
		CHANNELKNOWNMANIPS.registerMessage(networkID++, UpdateCurrentManipPacket.class,
				UpdateCurrentManipPacket::encode, UpdateCurrentManipPacket::decode,
				UpdateCurrentManipPacket.Handler::handle);
		CHANNELKNOWNMANIPS.registerMessage(networkID++, TeleportToVeinPacket.class, TeleportToVeinPacket::encode,
				TeleportToVeinPacket::decode, TeleportToVeinPacket.Handler::handle);

		CHANNELKNOWNMANIPS.registerMessage(networkID++, SyncTrackingAvatarPacket.class,
				SyncTrackingAvatarPacket::toBytes, SyncTrackingAvatarPacket::new, SyncTrackingAvatarPacket::handle);
		CHANNELKNOWNMANIPS.registerMessage(networkID++, UpdateCurrentVeinPacket.class, UpdateCurrentVeinPacket::encode,
				UpdateCurrentVeinPacket::decode, UpdateCurrentVeinPacket.Handler::handle);
		CHANNELKNOWNMANIPS.registerMessage(networkID++, StartCentrifugeButtonPacket.class,
				StartCentrifugeButtonPacket::encode, StartCentrifugeButtonPacket::decode,
				StartCentrifugeButtonPacket::handle);
		CHANNELVASCULARSYSTEM.registerMessage(networkID++, VascularSystemClientPacket.class,
				VascularSystemClientPacket::encode, VascularSystemClientPacket::decode,
				VascularSystemClientPacket::handle);
		CHANNELVASCULARSYSTEM.registerMessage(networkID++, VascularSystemServerPacket.class,
				VascularSystemServerPacket::encode, VascularSystemServerPacket::decode,
				VascularSystemServerPacket::handle);

		CHANNELBLOODVOLUME.registerMessage(networkID++, BloodVolumeClientPacket.class, BloodVolumeClientPacket::encode,
				BloodVolumeClientPacket::decode, BloodVolumeClientPacket::handle);
		CHANNELBLOODVOLUME.registerMessage(networkID++, BloodVolumeServerPacket.class, BloodVolumeServerPacket::encode,
				BloodVolumeServerPacket::decode, BloodVolumeServerPacket::handle);
		CHANNELBLOODVOLUME.registerMessage(networkID++, BloodFormationKeyPressPacket.class,
				BloodFormationKeyPressPacket::encode, BloodFormationKeyPressPacket::decode,
				BloodFormationKeyPressPacket::handle);
		CHANNELBLOODVOLUME.registerMessage(networkID++, BloodCraftingKeyPressPacket.class,
				BloodCraftingKeyPressPacket::encode, BloodCraftingKeyPressPacket::decode,
				BloodCraftingKeyPressPacket::handle);
		CHANNELBLOODVOLUME.messageBuilder(GroundBloodDrawPacket.class, networkID++)
				.decoder(GroundBloodDrawPacket::decode).encoder(GroundBloodDrawPacket::encode)
				.consumer(GroundBloodDrawPacket::handle).add();
		CHANNELBLOODVOLUME.messageBuilder(EntityHitParticlePacket.class, networkID++)
				.decoder(EntityHitParticlePacket::decode).encoder(EntityHitParticlePacket::encode)
				.consumer(EntityHitParticlePacket::handle).add();
		CHANNELBLOODVOLUME.messageBuilder(AirBloodDrawPacket.class, networkID++).decoder(AirBloodDrawPacket::decode)
				.encoder(AirBloodDrawPacket::encode).consumer(AirBloodDrawPacket::handle).add();

		CHANNELPARTICLES.messageBuilder(SpawnFlaskParticlesPacket.class, networkID++)
				.decoder(SpawnFlaskParticlesPacket::decode).encoder(SpawnFlaskParticlesPacket::encode)
				.consumer(SpawnFlaskParticlesPacket::handle).add();
		CHANNELPARTICLES.messageBuilder(SpawnAvatarParticlesPacket.class, networkID++)
				.decoder(SpawnAvatarParticlesPacket::decode).encoder(SpawnAvatarParticlesPacket::encode)
				.consumer(SpawnAvatarParticlesPacket::handle).add();
		CHANNELPARTICLES.messageBuilder(SpawnBloodClawParticlesPacket.class, networkID++)
				.decoder(SpawnBloodClawParticlesPacket::decode).encoder(SpawnBloodClawParticlesPacket::encode)
				.consumer(SpawnBloodClawParticlesPacket::handle).add();
		CHANNELPARTICLES.messageBuilder(SpawnLivingToolParticlesPacket.class, networkID++)
				.decoder(SpawnLivingToolParticlesPacket::decode).encoder(SpawnLivingToolParticlesPacket::encode)
				.consumer(SpawnLivingToolParticlesPacket::handle).add();

		CHANNELMORPHLINGJAR.registerMessage(networkID++, JarTogglePickupPacket.class, JarTogglePickupPacket::encode,
				JarTogglePickupPacket::decode, JarTogglePickupPacket::handle);
		CHANNELMORPHLINGJAR.registerMessage(networkID++, OpenMorphlingJarPacket.class, OpenMorphlingJarPacket::encode,
				OpenMorphlingJarPacket::decode, OpenMorphlingJarPacket::handle);
		CHANNELMORPHLINGJAR.registerMessage(networkID++, ToggleMorphlingJarMessagePacket.class,
				ToggleMorphlingJarMessagePacket::encode, ToggleMorphlingJarMessagePacket::decode,
				ToggleMorphlingJarMessagePacket::handle);
		CHANNELMORPHLINGJAR.registerMessage(networkID++, OpenLivingStaffPacket.class, OpenLivingStaffPacket::encode,
				OpenLivingStaffPacket::decode, OpenLivingStaffPacket::handle);

		CHANNELMORPHLINGJAR.registerMessage(networkID++, PacketUpdateLivingStaffMorph.class,
				PacketUpdateLivingStaffMorph::encode, PacketUpdateLivingStaffMorph::decode,
				PacketUpdateLivingStaffMorph.Handler::handle);

		CHANNELMORPHLINGJAR.registerMessage(networkID++, ChangeMorphKeyPacket.class, ChangeMorphKeyPacket::encode,
				ChangeMorphKeyPacket::decode, ChangeMorphKeyPacket.Handler::handle);

	}

	public static void sendAvatarHitParticles(Vec3 pos, ParticleColor color, double radius,
			ResourceKey<Level> dimension) {
		SpawnAvatarParticlesPacket msg = new SpawnAvatarParticlesPacket(pos, color);
		CHANNELPARTICLES.send(PacketDistributor.NEAR
				.with(() -> new PacketDistributor.TargetPoint(pos.x, pos.y, pos.z, radius, dimension)), msg);
	}

	public static void sendBloodFlaskParticles(Vec3 pos, ParticleColor color, double radius,
			ResourceKey<Level> dimension) {
		SpawnFlaskParticlesPacket msg = new SpawnFlaskParticlesPacket(pos, color);
		CHANNELPARTICLES.send(PacketDistributor.NEAR
				.with(() -> new PacketDistributor.TargetPoint(pos.x, pos.y, pos.z, radius, dimension)), msg);
	}

	public static void sendClawParticles(Vec3 pos, ParticleColor color, double radius, ResourceKey<Level> dimension) {
		SpawnBloodClawParticlesPacket msg = new SpawnBloodClawParticlesPacket(pos, color);
		CHANNELPARTICLES.send(PacketDistributor.NEAR
				.with(() -> new PacketDistributor.TargetPoint(pos.x, pos.y, pos.z, radius, dimension)), msg);
	}

	public static void sendLivingToolBreakParticles(Vec3 pos, ParticleColor color, double radius,
			ResourceKey<Level> dimension) {
		SpawnLivingToolParticlesPacket msg = new SpawnLivingToolParticlesPacket(pos, color);
		CHANNELPARTICLES.send(PacketDistributor.NEAR
				.with(() -> new PacketDistributor.TargetPoint(pos.x, pos.y, pos.z, radius, dimension)), msg);
	}

	public static void sendRadialInventorySlotChange(int slot, boolean offhand) {
		PacketHandler.CHANNELKNOWNMANIPS.sendTo(new RadialInventorySlotChangePacket(slot, offhand),
				Minecraft.getInstance().getConnection().getConnection(), NetworkDirection.PLAY_TO_SERVER);
	}

	private static <T extends RadialInventorySlotChangePacket> boolean validateBasics(T message,
			NetworkEvent.Context ctx) {
		LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
		ctx.setPacketHandled(true);
		if (sideReceived != LogicalSide.SERVER) {
			Hemomancy.LOGGER.error(message.getClass().getName() + " received on wrong side: " + sideReceived);
			return false;
		}
		if (!message.isMessageValid()) {
			Hemomancy.LOGGER.error(message.getClass().getName() + " was invalid: " + message);
			return false;
		}
		return true;
	}
}
