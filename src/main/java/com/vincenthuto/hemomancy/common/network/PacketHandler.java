package com.vincenthuto.hemomancy.common.network;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.network.capa.BloodTendencyClientPacket;
import com.vincenthuto.hemomancy.common.network.capa.BloodTendencyServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeClientPacket;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.PacketCurvedHornAnimation;
import com.vincenthuto.hemomancy.common.network.capa.PacketGourdScarSync;
import com.vincenthuto.hemomancy.common.network.capa.PacketOpenNormalInv;
import com.vincenthuto.hemomancy.common.network.capa.PacketSyncSkills;
import com.vincenthuto.hemomancy.common.network.capa.PacketLedgerAction;
import com.vincenthuto.hemomancy.common.network.capa.PacketLumpDonate;
import com.vincenthuto.hemomancy.common.network.capa.PacketUpdatePoolSettings;
import com.vincenthuto.hemomancy.common.network.capa.PacketSyncBloodlinePool;
import com.vincenthuto.hemomancy.common.network.capa.PacketSyncActiveRites;
import com.vincenthuto.hemomancy.common.network.capa.PacketBloodCraftRing;
import com.vincenthuto.hemomancy.common.network.capa.PacketSyncDegree;
import com.vincenthuto.hemomancy.common.network.capa.PacketSyncUnstainedProgress;
import com.vincenthuto.hemomancy.common.network.capa.PacketBloodlineMessage;
import com.vincenthuto.hemomancy.common.network.capa.PacketRequestPoolData;
import com.vincenthuto.hemomancy.common.network.capa.PacketToggleBinderMessage;
import com.vincenthuto.hemomancy.common.network.capa.PacketToggleUnstainedBonus;
import com.vincenthuto.hemomancy.common.network.capa.PacketUnlockSkill;
import com.vincenthuto.hemomancy.common.network.capa.VascularSystemClientPacket;
import com.vincenthuto.hemomancy.common.network.capa.VascularSystemServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.manips.*;
import com.vincenthuto.hemomancy.common.network.capa.scars.*;
import com.vincenthuto.hemomancy.common.network.keybind.BloodCraftingKeyPressPacket;
import com.vincenthuto.hemomancy.common.network.keybind.BloodFormationKeyPressPacket;
import com.vincenthuto.hemomancy.common.network.keybind.ToggleGourdKeyPacket;
import com.vincenthuto.hemomancy.common.network.morphling.ChangeMorphKeyPacket;
import com.vincenthuto.hemomancy.common.network.morphling.JarTogglePickupPacket;
import com.vincenthuto.hemomancy.common.network.morphling.OpenLivingStaffPacket;
import com.vincenthuto.hemomancy.common.network.morphling.OpenMorphlingJarPacket;
import com.vincenthuto.hemomancy.common.network.morphling.PacketUpdateLivingStaffMorph;
import com.vincenthuto.hemomancy.common.network.morphling.SyncEquippedMorphlingPacket;
import com.vincenthuto.hemomancy.common.network.morphling.ToggleMorphlingJarMessagePacket;
import com.vincenthuto.hemomancy.common.network.particle.AirBloodDrawPacket;
import com.vincenthuto.hemomancy.common.network.particle.EntityHitParticlePacket;
import com.vincenthuto.hemomancy.common.network.particle.GroundBloodDrawPacket;
import com.vincenthuto.hemomancy.common.network.particle.SpawnAvatarParticlesPacket;
import com.vincenthuto.hemomancy.common.network.particle.SpawnBloodClawParticlesPacket;
import com.vincenthuto.hemomancy.common.network.particle.SpawnFlaskParticlesPacket;
import com.vincenthuto.hemomancy.common.network.particle.SpawnMonolithShatterBurstPacket;
import com.vincenthuto.hemomancy.common.network.dialogue.DialogueOptionPacket;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import com.vincenthuto.hemomancy.common.network.capa.PacketSyncBloodMoon;
import com.vincenthuto.hemomancy.common.network.capa.PacketSyncQliphothBlooms;
import com.vincenthuto.hemomancy.common.network.capa.visceral.OpenVisceralMirrorPacket;
import com.vincenthuto.hemomancy.common.network.capa.visceral.VisceralMirrorCancelPacket;
import com.vincenthuto.hemomancy.common.network.capa.visceral.VisceralMirrorExtractPacket;
import com.vincenthuto.hemomancy.common.network.capa.visceral.VisceralMirrorUpdatePacket;
import com.vincenthuto.hemomancy.common.network.particle.SpawnLivingToolParticlesPacket;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.network.PacketSpawnLightningParticle;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
	private static int networkID = 0;
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel CHANNELBLOODTENDENCY = NetworkRegistry.newSimpleChannel(
			Hemomancy.rloc("bloodtendencychannel"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals);
	public static final SimpleChannel CHANNELVASCULARSYSTEM = NetworkRegistry.newSimpleChannel(
			Hemomancy.rloc("vascularsystemchannel"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals);
	public static final SimpleChannel CHANNELBLOODVOLUME = NetworkRegistry.newSimpleChannel(
			Hemomancy.rloc("bloodvolumechannel"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals);
	public static final SimpleChannel CHANNELKNOWNMANIPS = NetworkRegistry.newSimpleChannel(
			Hemomancy.rloc("knownmanipulationchannel"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals);
	public static final SimpleChannel CHANNELPARTICLES = NetworkRegistry.newSimpleChannel(
			Hemomancy.rloc("particlechannel"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals);
	public static SimpleChannel CHANNELSCARS = NetworkRegistry.newSimpleChannel(Hemomancy.rloc("scarchannel"),
			() -> PROTOCOL_VERSION, s -> true, s -> true);
	public static SimpleChannel CHANNELMORPHLINGJAR = NetworkRegistry.newSimpleChannel(
			Hemomancy.rloc("morphlingjarchannel"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals);
	public static SimpleChannel CHANNELSCARBINDER = NetworkRegistry.newSimpleChannel(
			Hemomancy.rloc("scarbinderchannel"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals);

	public static void registerChannels() {

		CHANNELSCARS.registerMessage(networkID++, CPacketFlight.class, CPacketFlight::encode,
	        CPacketFlight::decode, CPacketFlight::handle);

		CHANNELSCARS.registerMessage(networkID++, PacketOpenSporeInv.class, PacketOpenSporeInv::decode,
				PacketOpenSporeInv::new, PacketOpenSporeInv::handle);

		CHANNELSCARS.registerMessage(networkID++, PacketOpenScarsInv.class, PacketOpenScarsInv::decode,
				PacketOpenScarsInv::new, PacketOpenScarsInv::handle);
		CHANNELSCARS.registerMessage(networkID++, PacketOpenNormalInv.class, PacketOpenNormalInv::decode,
				PacketOpenNormalInv::new, PacketOpenNormalInv::handle);
		CHANNELSCARS.registerMessage(networkID++, PacketScarSync.class, PacketScarSync::toBytes, PacketScarSync::new,
				PacketScarSync::handle);
		CHANNELSCARS.registerMessage(networkID++, PacketGourdScarSync.class, PacketGourdScarSync::toBytes,
				PacketGourdScarSync::new, PacketGourdScarSync::handle);

		CHANNELSCARS.registerMessage(networkID++, PacketCurvedHornAnimation.class, PacketCurvedHornAnimation::decode,
				PacketCurvedHornAnimation::new, PacketCurvedHornAnimation::handle);

		CHANNELSCARS.registerMessage(networkID++, ToggleGourdKeyPacket.class, ToggleGourdKeyPacket::encode,
				ToggleGourdKeyPacket::decode, ToggleGourdKeyPacket::handle);

		CHANNELSCARS.registerMessage(networkID++, PacketUpdateScarPattern.class, PacketUpdateScarPattern::encode,
				PacketUpdateScarPattern::decode, PacketUpdateScarPattern.Handler::handle);
		CHANNELSCARS.registerMessage(networkID++, PacketScarCraftingEvent.class, PacketScarCraftingEvent::encode,
				PacketScarCraftingEvent::decode, PacketScarCraftingEvent.Handler::handle);
		CHANNELSCARS.registerMessage(networkID++, PacketLoadScarPattern.class, PacketLoadScarPattern::encode,
				PacketLoadScarPattern::decode, PacketLoadScarPattern.Handler::handle);

		CHANNELSCARBINDER.registerMessage(networkID++, PacketOpenScarBinder.class, PacketOpenScarBinder::encode,
				PacketOpenScarBinder::decode, PacketOpenScarBinder::handle);
		CHANNELSCARBINDER.registerMessage(networkID++, PacketToggleBinderMessage.class,
				PacketToggleBinderMessage::encode, PacketToggleBinderMessage::decode,
				PacketToggleBinderMessage::handle);

		CHANNELKNOWNMANIPS.registerMessage(networkID++, PacketSpawnLightningParticle.class,
				PacketSpawnLightningParticle::encode, PacketSpawnLightningParticle::decode,
				PacketSpawnLightningParticle::handle);

		CHANNELBLOODTENDENCY.registerMessage(networkID++, BloodTendencyClientPacket.class,
				BloodTendencyClientPacket::encode, BloodTendencyClientPacket::decode,
				BloodTendencyClientPacket::handle);
		CHANNELBLOODTENDENCY.registerMessage(networkID++, BloodTendencyServerPacket.class,
				BloodTendencyServerPacket::encode, BloodTendencyServerPacket::decode,
				BloodTendencyServerPacket::handle);
		CHANNELBLOODTENDENCY.registerMessage(networkID++, PacketOpenTendencyView.class, PacketOpenTendencyView::decode,
				PacketOpenTendencyView::new, PacketOpenTendencyView::handle);

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
		CHANNELKNOWNMANIPS.registerMessage(networkID++, UseManipKeyPacket.class, UseManipKeyPacket::encode,
				UseManipKeyPacket::decode, UseManipKeyPacket::handle);
		CHANNELKNOWNMANIPS.registerMessage(networkID++, ManipCooldownPacket.class, ManipCooldownPacket::encode,
				ManipCooldownPacket::decode, ManipCooldownPacket::handle);
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
		CHANNELKNOWNMANIPS.registerMessage(networkID++, EquipManipulationPacket.class,
				EquipManipulationPacket::encode, EquipManipulationPacket::decode,
				EquipManipulationPacket::handle);
		CHANNELVASCULARSYSTEM.registerMessage(networkID++, VascularSystemClientPacket.class,
				VascularSystemClientPacket::encode, VascularSystemClientPacket::decode,
				VascularSystemClientPacket::handle);
		CHANNELVASCULARSYSTEM.registerMessage(networkID++, VascularSystemServerPacket.class,
				VascularSystemServerPacket::encode, VascularSystemServerPacket::decode,
				VascularSystemServerPacket::handle);

		CHANNELVASCULARSYSTEM.registerMessage(networkID++, PacketOpenVascularView.class, PacketOpenVascularView::decode,
				PacketOpenVascularView::new, PacketOpenVascularView::handle);


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
				.consumerNetworkThread(GroundBloodDrawPacket::handle).add();
		CHANNELBLOODVOLUME.messageBuilder(EntityHitParticlePacket.class, networkID++)
				.decoder(EntityHitParticlePacket::decode).encoder(EntityHitParticlePacket::encode)
				.consumerNetworkThread(EntityHitParticlePacket::handle).add();
		CHANNELBLOODVOLUME.messageBuilder(AirBloodDrawPacket.class, networkID++).decoder(AirBloodDrawPacket::decode)
				.encoder(AirBloodDrawPacket::encode).consumerNetworkThread(AirBloodDrawPacket::handle).add();

		// Skill tree packets
		CHANNELBLOODVOLUME.messageBuilder(PacketUnlockSkill.class, networkID++)
				.decoder(PacketUnlockSkill::decode).encoder(PacketUnlockSkill::encode)
				.consumerNetworkThread(PacketUnlockSkill::handle).add();
		CHANNELBLOODVOLUME.messageBuilder(PacketSyncSkills.class, networkID++)
				.decoder(PacketSyncSkills::decode).encoder(PacketSyncSkills::encode)
				.consumerNetworkThread(PacketSyncSkills::handle).add();

		// Bloodline pool packets
		CHANNELBLOODVOLUME.messageBuilder(PacketLumpDonate.class, networkID++)
				.decoder(PacketLumpDonate::decode).encoder(PacketLumpDonate::encode)
				.consumerNetworkThread(PacketLumpDonate::handle).add();
		CHANNELBLOODVOLUME.messageBuilder(PacketUpdatePoolSettings.class, networkID++)
				.decoder(PacketUpdatePoolSettings::decode).encoder(PacketUpdatePoolSettings::encode)
				.consumerNetworkThread(PacketUpdatePoolSettings::handle).add();
		CHANNELBLOODVOLUME.messageBuilder(PacketSyncBloodlinePool.class, networkID++)
				.decoder(PacketSyncBloodlinePool::decode).encoder(PacketSyncBloodlinePool::encode)
				.consumerNetworkThread(PacketSyncBloodlinePool::handle).add();
		CHANNELBLOODVOLUME.messageBuilder(PacketRequestPoolData.class, networkID++)
				.decoder(PacketRequestPoolData::decode).encoder(PacketRequestPoolData::encode)
				.consumerNetworkThread(PacketRequestPoolData::handle).add();

		// Bloodline whisper message packet (client → server, broadcast to bloodline)
		CHANNELBLOODVOLUME.messageBuilder(PacketBloodlineMessage.class, networkID++)
				.decoder(PacketBloodlineMessage::decode).encoder(PacketBloodlineMessage::encode)
				.consumerNetworkThread(PacketBloodlineMessage::handle).add();

		// Cardinal rite sync packet
		CHANNELBLOODVOLUME.messageBuilder(PacketSyncActiveRites.class, networkID++)
				.decoder(PacketSyncActiveRites::decode).encoder(PacketSyncActiveRites::encode)
				.consumerNetworkThread(PacketSyncActiveRites::handle).add();

		// Blood craft ring animation packet
		CHANNELBLOODVOLUME.messageBuilder(PacketBloodCraftRing.class, networkID++)
				.decoder(PacketBloodCraftRing::decode).encoder(PacketBloodCraftRing::encode)
				.consumerNetworkThread(PacketBloodCraftRing::handle).add();

		// Initiatory degree sync packet
		CHANNELBLOODVOLUME.messageBuilder(PacketSyncDegree.class, networkID++)
				.decoder(PacketSyncDegree::decode).encoder(PacketSyncDegree::encode)
				.consumerNetworkThread(PacketSyncDegree::handle).add();

		// Unstained progress sync packet
		CHANNELBLOODVOLUME.messageBuilder(PacketSyncUnstainedProgress.class, networkID++)
				.decoder(PacketSyncUnstainedProgress::decode).encoder(PacketSyncUnstainedProgress::encode)
				.consumerNetworkThread(PacketSyncUnstainedProgress::handle).add();

		// Unstained bonus toggle packet (client → server)
		CHANNELBLOODVOLUME.messageBuilder(PacketToggleUnstainedBonus.class, networkID++)
				.decoder(PacketToggleUnstainedBonus::decode).encoder(PacketToggleUnstainedBonus::encode)
				.consumerNetworkThread(PacketToggleUnstainedBonus::handle).add();

		CHANNELPARTICLES.messageBuilder(SpawnFlaskParticlesPacket.class, networkID++)
				.decoder(SpawnFlaskParticlesPacket::decode).encoder(SpawnFlaskParticlesPacket::encode)
				.consumerNetworkThread(SpawnFlaskParticlesPacket::handle).add();
		CHANNELPARTICLES.messageBuilder(SpawnAvatarParticlesPacket.class, networkID++)
				.decoder(SpawnAvatarParticlesPacket::decode).encoder(SpawnAvatarParticlesPacket::encode)
				.consumerNetworkThread(SpawnAvatarParticlesPacket::handle).add();
		CHANNELPARTICLES.messageBuilder(SpawnBloodClawParticlesPacket.class, networkID++)
				.decoder(SpawnBloodClawParticlesPacket::decode).encoder(SpawnBloodClawParticlesPacket::encode)
				.consumerNetworkThread(SpawnBloodClawParticlesPacket::handle).add();
		CHANNELPARTICLES.messageBuilder(SpawnLivingToolParticlesPacket.class, networkID++)
				.decoder(SpawnLivingToolParticlesPacket::decode).encoder(SpawnLivingToolParticlesPacket::encode)
				.consumerNetworkThread(SpawnLivingToolParticlesPacket::handle).add();
		CHANNELPARTICLES.messageBuilder(SpawnMonolithShatterBurstPacket.class, networkID++)
				.decoder(SpawnMonolithShatterBurstPacket::decode).encoder(SpawnMonolithShatterBurstPacket::encode)
				.consumerNetworkThread(SpawnMonolithShatterBurstPacket::handle).add();

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

		CHANNELMORPHLINGJAR.registerMessage(networkID++, SyncEquippedMorphlingPacket.class,
				SyncEquippedMorphlingPacket::encode, SyncEquippedMorphlingPacket::decode,
				SyncEquippedMorphlingPacket::handle);

		// Structure spawner debug packet
		CHANNELBLOODVOLUME.messageBuilder(PlaceStructurePacket.class, networkID++)
				.decoder(PlaceStructurePacket::decode).encoder(PlaceStructurePacket::encode)
				.consumerNetworkThread(PlaceStructurePacket::handle).add();

		// Dialogue system packets
		CHANNELBLOODVOLUME.messageBuilder(OpenDialoguePacket.class, networkID++)
				.decoder(OpenDialoguePacket::decode).encoder(OpenDialoguePacket::encode)
				.consumerNetworkThread(OpenDialoguePacket::handle).add();
		CHANNELBLOODVOLUME.messageBuilder(DialogueOptionPacket.class, networkID++)
				.decoder(DialogueOptionPacket::decode).encoder(DialogueOptionPacket::encode)
				.consumerNetworkThread(DialogueOptionPacket::handle).add();

		// Qliphoth Bloom sync packet
		CHANNELBLOODVOLUME.messageBuilder(PacketSyncQliphothBlooms.class, networkID++)
				.decoder(PacketSyncQliphothBlooms::decode).encoder(PacketSyncQliphothBlooms::encode)
				.consumerNetworkThread(PacketSyncQliphothBlooms::handle).add();

		// Blood Moon sync packet
		CHANNELBLOODVOLUME.messageBuilder(PacketSyncBloodMoon.class, networkID++)
				.decoder(PacketSyncBloodMoon::decode).encoder(PacketSyncBloodMoon::encode)
				.consumerNetworkThread(PacketSyncBloodMoon::handle).add();

		// Visceral Mirror packets
		CHANNELBLOODVOLUME.messageBuilder(OpenVisceralMirrorPacket.class, networkID++)
				.decoder(OpenVisceralMirrorPacket::decode).encoder(OpenVisceralMirrorPacket::encode)
				.consumerNetworkThread(OpenVisceralMirrorPacket::handle).add();
		CHANNELBLOODVOLUME.messageBuilder(VisceralMirrorExtractPacket.class, networkID++)
				.decoder(VisceralMirrorExtractPacket::decode).encoder(VisceralMirrorExtractPacket::encode)
				.consumerNetworkThread(VisceralMirrorExtractPacket::handle).add();
		CHANNELBLOODVOLUME.messageBuilder(VisceralMirrorCancelPacket.class, networkID++)
				.decoder(VisceralMirrorCancelPacket::decode).encoder(VisceralMirrorCancelPacket::encode)
				.consumerNetworkThread(VisceralMirrorCancelPacket::handle).add();
		CHANNELBLOODVOLUME.messageBuilder(VisceralMirrorUpdatePacket.class, networkID++)
				.decoder(VisceralMirrorUpdatePacket::decode).encoder(VisceralMirrorUpdatePacket::encode)
				.consumerNetworkThread(VisceralMirrorUpdatePacket::handle).add();

		// Ledger action packet (client → server: summon NPCs, recall to lodge, set recall point)
		CHANNELBLOODVOLUME.messageBuilder(PacketLedgerAction.class, networkID++)
				.decoder(PacketLedgerAction::decode).encoder(PacketLedgerAction::encode)
				.consumerNetworkThread(PacketLedgerAction::handle).add();

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

	public static void sendMonolithShatterBurst(Vec3 pos, double radius, ResourceKey<Level> dimension) {
		SpawnMonolithShatterBurstPacket msg = new SpawnMonolithShatterBurstPacket(pos);
		CHANNELPARTICLES.send(PacketDistributor.NEAR
				.with(() -> new PacketDistributor.TargetPoint(pos.x, pos.y, pos.z, radius, dimension)), msg);
	}

	public static void sendClientElytraPacket() {
		CHANNELSCARS.send(PacketDistributor.SERVER.noArg(), new CPacketFlight());
	}

}
