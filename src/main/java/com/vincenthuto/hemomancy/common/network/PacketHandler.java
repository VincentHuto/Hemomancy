package com.vincenthuto.hemomancy.common.network;

import com.vincenthuto.hemomancy.common.network.capa.PacketSyncLiberKnowledge;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.*;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.bestiary.PacketRequestSpecimenBestiary;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.bestiary.PacketSyncSpecimenBestiary;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.*;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.scars.*;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.summon.KnownSummonsRequestPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.summon.KnownSummonsServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.visceral.OpenVisceralMirrorPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.visceral.VisceralMirrorCancelPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.visceral.VisceralMirrorExtractPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.visceral.VisceralMirrorUpdatePacket;
import com.vincenthuto.hemomancy.common.network.capa.unstained.*;
import com.vincenthuto.hemomancy.common.network.circus.PacketSyncCircusPerception;
import com.vincenthuto.hemomancy.common.network.dialogue.DialogueOptionPacket;
import com.vincenthuto.hemomancy.common.network.dialogue.DialogueRewardChoicePacket;
import com.vincenthuto.hemomancy.common.network.dialogue.DialogueRewardResultPacket;
import com.vincenthuto.hemomancy.common.network.dialogue.DialogueTopicOpenedPacket;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenPendingWhisperPacket;
import com.vincenthuto.hemomancy.common.network.dialogue.ClosePendingWhisperPacket;
import com.vincenthuto.hemomancy.common.network.dialogue.DialogueSender;
import com.vincenthuto.hemomancy.common.network.dialogue.PacketSyncNpcProgressionMarkers;
import com.vincenthuto.hemomancy.common.network.discovery.OpenInscriptionPacket;
import com.vincenthuto.hemomancy.common.network.mission.OpenBookOfObservancesPacket;
import com.vincenthuto.hemomancy.common.network.mission.OpenHarbingerAssignmentLedgerPacket;
import com.vincenthuto.hemomancy.common.network.morphling.*;
import com.vincenthuto.hemomancy.common.network.particle.*;
import com.vincenthuto.hemomancy.common.network.routing.PacketSpawnVeinSpiderCourier;
import com.vincenthuto.hemomancy.common.network.routing.PacketSyncSutureLinks;
import com.vincenthuto.hemomancy.common.network.summon.PacketCrossbarRadialAction;
import com.vincenthuto.hemomancy.common.network.summon.PacketPuppeteersSpindleAction;
import com.vincenthuto.hemomancy.common.network.will.WillPresenceCuePacket;
import com.vincenthuto.hemomancy.common.network.vein.*;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

// ── NeoForge 1.21 networking API ─────────────────────────────────────────────
// NeoForge 1.21 payload registration replaces the old SimpleChannel pattern.

public class PacketHandler {

    /**
     * Network version shared by every Hemomancy payload. NeoForge refuses a connection whose client and server
     * versions differ, so bump this whenever a payload is added, removed, or changes its byte layout.
     *
     * <ul>
     *   <li>{@code "hemomancy"}: the mod ID, used as the version before it was tracked.</li>
     *   <li>{@code "2"}: consolidation step 4 removed 18 never-sent payloads and registered 15 client-to-server
     *   payloads in their real direction.</li>
     *   <li>{@code "3"}: eight single-purpose VFX payloads folded into ParticleBurstPacket and EntityVisualPacket.</li>
     *   <li>{@code "6"}: the Ironhearted and Blackhearted sync renamed to PacketSyncManipulationHearts
     *   ({@code sync_manipulation_hearts}).</li>
     * </ul>
     */
    public static final String PROTOCOL_VERSION = "16";

    // ─────────────────────────────────────────────────────────────────────────
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Registers all packet payload types.
     *
     * <p>NeoForge 1.21 replaces the old {@code SimpleChannel.registerMessage} pattern with
     * {@link RegisterPayloadHandlersEvent}.  Each packet must implement
     * {@code net.minecraft.network.protocol.common.custom.CustomPacketPayload}.
     *
     * <p>Direction semantics:
     * <ul>
     *   <li>{@code playToClient} – server sends to client (replaces old CLIENT-bound packets)</li>
     *   <li>{@code playToServer} – client sends to server (replaces old SERVER-bound packets)</li>
     *   <li>{@code playBidirectional} – only {@link AntecedentEffectPacket}, which really travels both ways</li>
     * </ul>
     *
     */
    public static void registerChannels(IEventBus modBus) {
        modBus.addListener(PacketHandler::onRegisterPayloads);
    }

    private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        var net = event.registrar(PROTOCOL_VERSION);
        net.playToClient(PhlegethosomaSuctionPacket.TYPE, PhlegethosomaSuctionPacket.STREAM_CODEC, PhlegethosomaSuctionPacket::handle);
        net.playToClient(PhlegethonticCurrentPacket.TYPE, PhlegethonticCurrentPacket.STREAM_CODEC,
                PhlegethonticCurrentPacket::handle);
        net.playToClient(PacketSyncStationUpgrades.TYPE, PacketSyncStationUpgrades.STREAM_CODEC,
                PacketSyncStationUpgrades::handle);
        net.playToServer(com.vincenthuto.hemomancy.common.network.succession.ResidentsRequestPacket.TYPE,
                com.vincenthuto.hemomancy.common.network.succession.ResidentsRequestPacket.STREAM_CODEC,
                com.vincenthuto.hemomancy.common.network.succession.ResidentsRequestPacket::handle);
        net.playToClient(com.vincenthuto.hemomancy.common.network.succession.ResidentsSnapshotPacket.TYPE,
                com.vincenthuto.hemomancy.common.network.succession.ResidentsSnapshotPacket.STREAM_CODEC,
                ClientPayloadHandlers::dispatch);
        net.playToClient(CastingAnimationPacket.TYPE, CastingAnimationPacket.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToServer(CastingChargePacket.TYPE, CastingChargePacket.STREAM_CODEC, CastingChargePacket::handle);
        net.playToServer(ClairaudiographActionPacket.TYPE, ClairaudiographActionPacket.STREAM_CODEC, ClairaudiographActionPacket::handle);
        net.playToClient(ClairaudiographChoicesPacket.TYPE, ClairaudiographChoicesPacket.STREAM_CODEC, ClairaudiographChoicesPacket::handle);
        net.playToClient(ClairaudiographSoundPacket.TYPE, ClairaudiographSoundPacket.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(DatapackSyncPacket.TYPE, DatapackSyncPacket.STREAM_CODEC, DatapackSyncPacket::handle);
        net.playToClient(PacketHematicMicroscopeViewing.TYPE, PacketHematicMicroscopeViewing.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(PacketBloodVialInjection.TYPE, PacketBloodVialInjection.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToServer(ManipulationChargeVisualPacket.TYPE, ManipulationChargeVisualPacket.STREAM_CODEC,
                ManipulationChargeVisualPacket::handle);
        net.playToClient(BloodFlowPacket.TYPE, BloodFlowPacket.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(ManipulationFlowPacket.TYPE, ManipulationFlowPacket.STREAM_CODEC,
                ClientPayloadHandlers::dispatch);
        net.playToClient(ManipulationInterruptedPacket.TYPE, ManipulationInterruptedPacket.STREAM_CODEC,
                ClientPayloadHandlers::dispatch);
        net.playToClient(ConductionPathPacket.TYPE, ConductionPathPacket.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(NerveLinkPacket.TYPE, NerveLinkPacket.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(ManipulationVisualPacket.TYPE, ManipulationVisualPacket.STREAM_CODEC,
                ClientPayloadHandlers::dispatch);
        net.playToClient(ManipulationAccentPacket.TYPE, ManipulationAccentPacket.STREAM_CODEC,
                ClientPayloadHandlers::dispatch);

        // ── Blood Volume capability ───────────────────────────────────────────
        net.playToServer(BloodVolumeClientPacket.TYPE,
                BloodVolumeClientPacket.STREAM_CODEC, BloodVolumeClientPacket::handle);
        net.playToClient(BloodVolumeServerPacket.TYPE,
                BloodVolumeServerPacket.STREAM_CODEC, BloodVolumeServerPacket::handle);
        net.playToClient(PacketSyncBloodFlowDiagnostics.TYPE,
                PacketSyncBloodFlowDiagnostics.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(PacketSyncMaxBloodDiagnostics.TYPE,
                PacketSyncMaxBloodDiagnostics.STREAM_CODEC, ClientPayloadHandlers::dispatch);

        net.playToClient(PacketSyncPomeProgress.TYPE,
                PacketSyncPomeProgress.STREAM_CODEC, PacketSyncPomeProgress::handle);
        net.playToClient(PacketSyncFungalProjection.TYPE,
                PacketSyncFungalProjection.STREAM_CODEC, ClientPayloadHandlers::dispatch);
		net.playToClient(PacketSyncChamberVisit.TYPE,
				PacketSyncChamberVisit.STREAM_CODEC, ClientPayloadHandlers::dispatch);
		net.playToClient(com.vincenthuto.hemomancy.common.network.circus.PacketSyncCircusSchool.TYPE, com.vincenthuto.hemomancy.common.network.circus.PacketSyncCircusSchool.STREAM_CODEC, ClientPayloadHandlers::dispatch);
		net.playToClient(PacketSyncCircusPerception.TYPE,
				PacketSyncCircusPerception.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(PacketSyncChamberOfWill.TYPE,
                PacketSyncChamberOfWill.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(PacketSyncVesperFightScene.TYPE,
                PacketSyncVesperFightScene.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(com.vincenthuto.hemomancy.common.network.boss.BossBarFramePacket.TYPE,
                com.vincenthuto.hemomancy.common.network.boss.BossBarFramePacket.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(com.vincenthuto.hemomancy.common.network.boss.BossScenePacket.TYPE,
                com.vincenthuto.hemomancy.common.network.boss.BossScenePacket.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(PacketSyncMycophantFightScene.TYPE,
                PacketSyncMycophantFightScene.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketMycophantIntro.TYPE,
                com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketMycophantIntro.STREAM_CODEC,
                ClientPayloadHandlers::dispatch);
        net.playToClient(LivingStaffProgressServerPacket.TYPE,
                LivingStaffProgressServerPacket.STREAM_CODEC, LivingStaffProgressServerPacket::handle);

        // ── Blood Tendency capability ─────────────────────────────────────────
        net.playToClient(BloodTendencyServerPacket.TYPE,
                BloodTendencyServerPacket.STREAM_CODEC, BloodTendencyServerPacket::handle);

        // ── Vascular System capability ────────────────────────────────────────
        net.playToClient(VascularSystemServerPacket.TYPE,
                VascularSystemServerPacket.STREAM_CODEC, VascularSystemServerPacket::handle);
        net.playToClient(PacketSyncMuscleMemory.TYPE,
                PacketSyncMuscleMemory.STREAM_CODEC, PacketSyncMuscleMemory::handle);
		net.playToClient(PacketSyncManipulationHearts.TYPE,
				PacketSyncManipulationHearts.STREAM_CODEC, PacketSyncManipulationHearts::handle);

        // ── Scar system ───────────────────────────────────────────────────────
        net.playToClient(PacketEquipmentSync.TYPE, PacketEquipmentSync.STREAM_CODEC, PacketEquipmentSync::handle);
        net.playToClient(PacketSyncScarsState.TYPE, PacketSyncScarsState.STREAM_CODEC, PacketSyncScarsState::handle);
        net.playToServer(PacketPrepareScarPattern.TYPE, PacketPrepareScarPattern.STREAM_CODEC, PacketPrepareScarPattern::handle);
        net.playToClient(PacketGourdScarSync.TYPE, PacketGourdScarSync.STREAM_CODEC, PacketGourdScarSync::handle);
        net.playToServer(PacketOpenScarsInv.TYPE, PacketOpenScarsInv.STREAM_CODEC, PacketOpenScarsInv::handle);
        net.playToServer(CPacketFlight.TYPE, CPacketFlight.STREAM_CODEC, CPacketFlight::handle);
        net.playToServer(PacketUpdateScarPattern.TYPE, PacketUpdateScarPattern.STREAM_CODEC, PacketUpdateScarPattern::handle);
        net.playToServer(PacketScarCraftingEvent.TYPE, PacketScarCraftingEvent.STREAM_CODEC, PacketScarCraftingEvent::handle);
        net.playToClient(PacketCurvedHornAnimation.TYPE, PacketCurvedHornAnimation.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToServer(ToggleGourdKeyPacket.TYPE, ToggleGourdKeyPacket.STREAM_CODEC, ToggleGourdKeyPacket::handle);
        net.playToServer(ToggleEquipmentLayerVisibilityPacket.TYPE,
                ToggleEquipmentLayerVisibilityPacket.STREAM_CODEC, ToggleEquipmentLayerVisibilityPacket::handle);
        net.playToClient(SyncEquipmentLayerVisibilityPacket.TYPE,
                SyncEquipmentLayerVisibilityPacket.STREAM_CODEC, SyncEquipmentLayerVisibilityPacket::handle);

        // ── Known Manipulations ───────────────────────────────────────────────
        net.playToServer(KnownManipulationClientPacket.TYPE, KnownManipulationClientPacket.STREAM_CODEC, KnownManipulationClientPacket::handle);
        net.playToClient(KnownManipulationServerPacket.TYPE, KnownManipulationServerPacket.STREAM_CODEC, KnownManipulationServerPacket::handle);
        net.playToClient(PacketSyncManipulationCostDiagnostics.TYPE,
                PacketSyncManipulationCostDiagnostics.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(PacketSyncManipulationSlotDiagnostics.TYPE,
                PacketSyncManipulationSlotDiagnostics.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToServer(ChangeSelectedManipPacket.TYPE, ChangeSelectedManipPacket.STREAM_CODEC, ChangeSelectedManipPacket::handle);
        net.playToServer(UseManipKeyPacket.TYPE, UseManipKeyPacket.STREAM_CODEC, UseManipKeyPacket::handle);
        net.playToClient(ManipCooldownPacket.TYPE, ManipCooldownPacket.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToServer(UpdateCurrentManipPacket.TYPE, UpdateCurrentManipPacket.STREAM_CODEC, UpdateCurrentManipPacket::handle);
        net.playToServer(UpdateCurrentMemoryPacket.TYPE, UpdateCurrentMemoryPacket.STREAM_CODEC, UpdateCurrentMemoryPacket::handle);
        net.playToClient(OpenEarthenVeinDisplayPacket.TYPE,
                OpenEarthenVeinDisplayPacket.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToServer(SelectEarthenVeinDestinationPacket.TYPE,
                SelectEarthenVeinDestinationPacket.STREAM_CODEC, SelectEarthenVeinDestinationPacket::handle);
        net.playToClient(EarthenVeinTravelVisualPacket.TYPE,
                EarthenVeinTravelVisualPacket.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(SyncTrackingAvatarPacket.TYPE, SyncTrackingAvatarPacket.STREAM_CODEC, SyncTrackingAvatarPacket::handle);
        net.playToServer(StartCentrifugeButtonPacket.TYPE, StartCentrifugeButtonPacket.STREAM_CODEC, StartCentrifugeButtonPacket::handle);
        net.playToServer(EquipManipulationPacket.TYPE, EquipManipulationPacket.STREAM_CODEC, EquipManipulationPacket::handle);
        net.playToServer(SynapticLoadoutActionPacket.TYPE, SynapticLoadoutActionPacket.STREAM_CODEC, SynapticLoadoutActionPacket::handle);
        net.playToServer(PacketOpenTendencyView.TYPE, PacketOpenTendencyView.STREAM_CODEC, PacketOpenTendencyView::handle);
        net.playToServer(PacketOpenScryingDiagnostics.TYPE, PacketOpenScryingDiagnostics.STREAM_CODEC, PacketOpenScryingDiagnostics::handle);

        // Still Arts
        net.playToClient(KnownStillArtsServerPacket.TYPE, KnownStillArtsServerPacket.STREAM_CODEC, KnownStillArtsServerPacket::handle);
        net.playToServer(UpdateSelectedStillArtPacket.TYPE, UpdateSelectedStillArtPacket.STREAM_CODEC, UpdateSelectedStillArtPacket::handle);
        net.playToServer(UseStillArtKeyPacket.TYPE, UseStillArtKeyPacket.STREAM_CODEC, UseStillArtKeyPacket::handle);
        net.playToClient(StillArtCooldownPacket.TYPE, StillArtCooldownPacket.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToServer(KnownSummonsRequestPacket.TYPE, KnownSummonsRequestPacket.STREAM_CODEC, KnownSummonsRequestPacket::handle);
        net.playToClient(KnownSummonsServerPacket.TYPE, KnownSummonsServerPacket.STREAM_CODEC, KnownSummonsServerPacket::handle);
        net.playToServer(PacketRequestSpecimenBestiary.TYPE, PacketRequestSpecimenBestiary.STREAM_CODEC, PacketRequestSpecimenBestiary::handle);
        net.playToClient(PacketSyncSpecimenBestiary.TYPE, PacketSyncSpecimenBestiary.STREAM_CODEC, PacketSyncSpecimenBestiary::handle);
        net.playToServer(PacketPuppeteersSpindleAction.TYPE, PacketPuppeteersSpindleAction.STREAM_CODEC, PacketPuppeteersSpindleAction::handle);
        net.playToServer(PacketCrossbarRadialAction.TYPE, PacketCrossbarRadialAction.STREAM_CODEC, PacketCrossbarRadialAction::handle);
        net.playToClient(PacketSyncLastDeathMemory.TYPE, PacketSyncLastDeathMemory.STREAM_CODEC, PacketSyncLastDeathMemory::handle);
        net.playToClient(PacketSyncSutureLinks.TYPE, PacketSyncSutureLinks.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(PacketSpawnVeinSpiderCourier.TYPE,
                PacketSpawnVeinSpiderCourier.STREAM_CODEC, ClientPayloadHandlers::dispatch);

        // ── Key-bind packets ──────────────────────────────────────────────────
        net.playToServer(BloodFormationKeyPressPacket.TYPE, BloodFormationKeyPressPacket.STREAM_CODEC, BloodFormationKeyPressPacket::handle);
        net.playToServer(BloodCraftingKeyPressPacket.TYPE, BloodCraftingKeyPressPacket.STREAM_CODEC, BloodCraftingKeyPressPacket::handle);
        net.playToServer(ActivateArmorSetAbilityC2SPacket.TYPE, ActivateArmorSetAbilityC2SPacket.STREAM_CODEC,
                ActivateArmorSetAbilityC2SPacket::handle);
        net.playToClient(SyncArmorSetAbilityCooldownS2CPacket.TYPE,
                SyncArmorSetAbilityCooldownS2CPacket.STREAM_CODEC,
                SyncArmorSetAbilityCooldownS2CPacket::handle);
        net.playToServer(com.vincenthuto.hemomancy.common.network.axonal.AxonalInputPacket.TYPE,
                com.vincenthuto.hemomancy.common.network.axonal.AxonalInputPacket.STREAM_CODEC,
                com.vincenthuto.hemomancy.common.network.axonal.AxonalInputPacket::handle);
        net.playToClient(com.vincenthuto.hemomancy.common.network.axonal.AxonalStatePacket.TYPE,
                com.vincenthuto.hemomancy.common.network.axonal.AxonalStatePacket.STREAM_CODEC,
                ClientPayloadHandlers::dispatch);
        net.playToServer(ToggleSilentSlippingC2SPacket.TYPE, ToggleSilentSlippingC2SPacket.STREAM_CODEC,
                ToggleSilentSlippingC2SPacket::handle);
        net.playToClient(SyncSilentSlippingStateS2CPacket.TYPE, SyncSilentSlippingStateS2CPacket.STREAM_CODEC,
                SyncSilentSlippingStateS2CPacket::handle);
        net.playToClient(SyncSchoolCombatS2CPacket.TYPE, SyncSchoolCombatS2CPacket.STREAM_CODEC, SyncSchoolCombatS2CPacket::handle);
        net.playToClient(SyncBloodBindingTendrilS2CPacket.TYPE,
                SyncBloodBindingTendrilS2CPacket.STREAM_CODEC,
                ClientPayloadHandlers::dispatch);

        // ── Particles ─────────────────────────────────────────────────────────
        net.playToClient(ParticleBurstPacket.TYPE, ParticleBurstPacket.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(EntityVisualPacket.TYPE, EntityVisualPacket.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToServer(GroundBloodDrawPacket.TYPE, GroundBloodDrawPacket.STREAM_CODEC, GroundBloodDrawPacket::handle);
        net.playToClient(SpawnClawSlashPacket.TYPE, SpawnClawSlashPacket.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(SpawnBrazierItemAbsorptionParticlesPacket.TYPE,
                SpawnBrazierItemAbsorptionParticlesPacket.STREAM_CODEC,
                ClientPayloadHandlers::dispatch);
        net.playToClient(SpawnSanguineOmenEffectPacket.TYPE, SpawnSanguineOmenEffectPacket.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(CardinalRiteImpactPacket.TYPE, CardinalRiteImpactPacket.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(SpawnBlackVeilPacket.TYPE, SpawnBlackVeilPacket.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(WillPresenceCuePacket.TYPE, WillPresenceCuePacket.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        // HutosLib registers this payload in HLPacketHandler; re-registering here causes
        // "already registered" crashes for hutoslib:packet_spawn_lightning.

        // ── Skill tree ────────────────────────────────────────────────────────
        net.playToServer(PacketUnlockSkill.TYPE, PacketUnlockSkill.STREAM_CODEC, PacketUnlockSkill::handle);
        net.playToServer(PacketToggleSkill.TYPE, PacketToggleSkill.STREAM_CODEC, PacketToggleSkill::handle);
        net.playToServer(ImprintMnemonicBlueprintPacket.TYPE, ImprintMnemonicBlueprintPacket.STREAM_CODEC,
                ImprintMnemonicBlueprintPacket::handle);
        net.playToClient(PacketSyncSkills.TYPE, PacketSyncSkills.STREAM_CODEC, PacketSyncSkills::handle);
        net.playToServer(ArborFruitInteractPacket.TYPE, ArborFruitInteractPacket.STREAM_CODEC,
                ArborFruitInteractPacket::handle);
        net.playToClient(OpenArborSkillsPacket.TYPE, OpenArborSkillsPacket.STREAM_CODEC,
                ClientPayloadHandlers::dispatch);

        // ── Bloodline pool ────────────────────────────────────────────────────
        net.playToServer(PacketLumpDonate.TYPE, PacketLumpDonate.STREAM_CODEC, PacketLumpDonate::handle);
        net.playToServer(PacketUpdatePoolSettings.TYPE, PacketUpdatePoolSettings.STREAM_CODEC, PacketUpdatePoolSettings::handle);
        net.playToClient(PacketSyncBloodlinePool.TYPE, PacketSyncBloodlinePool.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(PacketOpenBloodlinePoolScreen.TYPE, PacketOpenBloodlinePoolScreen.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToServer(PacketRequestPoolData.TYPE, PacketRequestPoolData.STREAM_CODEC, PacketRequestPoolData::handle);
        net.playToServer(PacketKickBloodlinePlayer.TYPE, PacketKickBloodlinePlayer.STREAM_CODEC, PacketKickBloodlinePlayer::handle);
        net.playToServer(PacketBloodlineMessage.TYPE, PacketBloodlineMessage.STREAM_CODEC, PacketBloodlineMessage::handle);

        // ── Cardinal rites ────────────────────────────────────────────────────
        net.playToClient(PacketSyncActiveRites.TYPE, PacketSyncActiveRites.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(com.vincenthuto.hemomancy.common.network.capa.harbinger.CardinalRiteEventPacket.TYPE,
                com.vincenthuto.hemomancy.common.network.capa.harbinger.CardinalRiteEventPacket.STREAM_CODEC,
                ClientPayloadHandlers::dispatch);
        net.playToClient(PacketCardinalRiteStaffPlanting.TYPE,
                PacketCardinalRiteStaffPlanting.STREAM_CODEC,
                ClientPayloadHandlers::dispatch);
		net.playToClient(PacketLivingStaffMorph.TYPE,
				PacketLivingStaffMorph.STREAM_CODEC,
				ClientPayloadHandlers::dispatch);
        net.playToClient(PacketSyncPlayerAnimation.TYPE,
                PacketSyncPlayerAnimation.STREAM_CODEC,
                ClientPayloadHandlers::dispatch);
        net.playToClient(LivingFlailImpactPacket.TYPE,
                LivingFlailImpactPacket.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(PacketSyncIchorianKnowledge.TYPE, PacketSyncIchorianKnowledge.STREAM_CODEC,
                PacketSyncIchorianKnowledge::handle);
        net.playToClient(PacketSyncFaneBoundaries.TYPE, PacketSyncFaneBoundaries.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(PacketBloodCraftRing.TYPE, PacketBloodCraftRing.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(PacketBloodStructureFeed.TYPE, PacketBloodStructureFeed.STREAM_CODEC, ClientPayloadHandlers::dispatch);

        // ── Degree / Unstained progress ───────────────────────────────────────
        net.playToClient(PacketSyncDegree.TYPE, PacketSyncDegree.STREAM_CODEC, PacketSyncDegree::handle);
        // The one payload that really travels both ways: the server plays Antecedent effects, and the client sends
        // kind 4 back once it has shown the epilogue. Its handler branches on the side.
        net.playBidirectional(AntecedentEffectPacket.TYPE, AntecedentEffectPacket.STREAM_CODEC, AntecedentEffectPacket::handle);
        net.playToClient(PacketAntecedentResearch.TYPE, PacketAntecedentResearch.STREAM_CODEC, PacketAntecedentResearch::handle);
        net.playToClient(PacketSyncClinicalBlood.TYPE, PacketSyncClinicalBlood.STREAM_CODEC, PacketSyncClinicalBlood::handle);
        net.playToClient(PacketSyncLiberKnowledge.TYPE, PacketSyncLiberKnowledge.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(PacketSyncUnstainedProgress.TYPE, PacketSyncUnstainedProgress.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToServer(PacketToggleUnstainedBonus.TYPE, PacketToggleUnstainedBonus.STREAM_CODEC, PacketToggleUnstainedBonus::handle);

        // ── Morphling Jar ─────────────────────────────────────────────────────
        net.playToServer(OpenMorphlingJarPacket.TYPE, OpenMorphlingJarPacket.STREAM_CODEC, OpenMorphlingJarPacket::handle);
        net.playToServer(PacketUpdateLivingStaffMorph.TYPE, PacketUpdateLivingStaffMorph.STREAM_CODEC, PacketUpdateLivingStaffMorph::handle);
        net.playToClient(SyncEquippedMorphlingPacket.TYPE, SyncEquippedMorphlingPacket.STREAM_CODEC, SyncEquippedMorphlingPacket::handle);

        // ── Debug / structure spawner ─────────────────────────────────────────
        net.playToServer(PlaceStructurePacket.TYPE, PlaceStructurePacket.STREAM_CODEC, PlaceStructurePacket::handle);

        // ── Dialogue system ───────────────────────────────────────────────────
        net.playToClient(OpenPendingWhisperPacket.TYPE, OpenPendingWhisperPacket.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToServer(ClosePendingWhisperPacket.TYPE, ClosePendingWhisperPacket.STREAM_CODEC, ClosePendingWhisperPacket::handle);
        net.playToClient(OpenDialoguePacket.TYPE, OpenDialoguePacket.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(PacketSyncNpcProgressionMarkers.TYPE, PacketSyncNpcProgressionMarkers.STREAM_CODEC,
                ClientPayloadHandlers::dispatch);
        net.playToServer(DialogueOptionPacket.TYPE, DialogueOptionPacket.STREAM_CODEC, DialogueOptionPacket::handle);
        net.playToServer(DialogueRewardChoicePacket.TYPE, DialogueRewardChoicePacket.STREAM_CODEC, DialogueRewardChoicePacket::handle);
        net.playToClient(DialogueRewardResultPacket.TYPE, DialogueRewardResultPacket.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToServer(DialogueTopicOpenedPacket.TYPE, DialogueTopicOpenedPacket.STREAM_CODEC,
                DialogueTopicOpenedPacket::handle);
        net.playToClient(OpenInscriptionPacket.TYPE, OpenInscriptionPacket.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(OpenHarbingerAssignmentLedgerPacket.TYPE,
                OpenHarbingerAssignmentLedgerPacket.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(OpenBookOfObservancesPacket.TYPE,
                OpenBookOfObservancesPacket.STREAM_CODEC, ClientPayloadHandlers::dispatch);

        // ── World events ──────────────────────────────────────────────────────
        net.playToClient(PacketSyncQliphothBlooms.TYPE, PacketSyncQliphothBlooms.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(com.vincenthuto.hemomancy.common.network.rite.RiteZoneSyncPacket.TYPE, com.vincenthuto.hemomancy.common.network.rite.RiteZoneSyncPacket.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(com.vincenthuto.hemomancy.common.network.rite.RiteSpaceSyncPacket.TYPE, com.vincenthuto.hemomancy.common.network.rite.RiteSpaceSyncPacket.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToClient(PacketSyncBloodMoon.TYPE, PacketSyncBloodMoon.STREAM_CODEC, PacketSyncBloodMoon::handle);

        // ── Visceral Mirror ───────────────────────────────────────────────────
        net.playToClient(OpenVisceralMirrorPacket.TYPE, OpenVisceralMirrorPacket.STREAM_CODEC, ClientPayloadHandlers::dispatch);
        net.playToServer(VisceralMirrorExtractPacket.TYPE, VisceralMirrorExtractPacket.STREAM_CODEC, VisceralMirrorExtractPacket::handle);
        net.playToServer(VisceralMirrorCancelPacket.TYPE, VisceralMirrorCancelPacket.STREAM_CODEC, VisceralMirrorCancelPacket::handle);
        net.playToClient(VisceralMirrorUpdatePacket.TYPE, VisceralMirrorUpdatePacket.STREAM_CODEC, ClientPayloadHandlers::dispatch);

        // ── Semi-Sentient Construct screen ────────────────────────────────────
        net.playToClient(OpenSSCScreenPacket.TYPE, OpenSSCScreenPacket.STREAM_CODEC, ClientPayloadHandlers::dispatch);

        // ── Ledger ────────────────────────────────────────────────────────────
        net.playToServer(PacketLedgerAction.TYPE, PacketLedgerAction.STREAM_CODEC, PacketLedgerAction::handle);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Per-effect helpers: domain shortcuts over the facade below.
    // ─────────────────────────────────────────────────────────────────────────

    public static void sendAvatarHitParticles(Vec3 pos, ParticleColor color, double radius,
            ServerLevel level) {
        sendNear(level, pos.x, pos.y, pos.z, radius,
                new ParticleBurstPacket(ParticleBurstPacket.Kind.AVATAR_HIT, pos, color));
    }

    public static void sendBloodFlaskParticles(Vec3 pos, ParticleColor color, double radius,
            ServerLevel level) {
        sendNear(level, pos.x, pos.y, pos.z, radius,
                new ParticleBurstPacket(ParticleBurstPacket.Kind.BLOOD_FLASK, pos, color));
    }

    public static void sendClawParticles(Vec3 pos, ParticleColor color, double radius,
            ServerLevel level) {
        sendNear(level, pos.x, pos.y, pos.z, radius,
                new ParticleBurstPacket(ParticleBurstPacket.Kind.BLOOD_CLAW, pos, color));
    }

    public static void sendClawSlash(Vec3 pos, Vec3 forward, ParticleColor color, boolean ambush, float scale,
            double radius, ServerLevel level) {
        sendNear(level, pos.x, pos.y, pos.z, radius,
                new SpawnClawSlashPacket(pos, forward, color, ambush, scale, level.random.nextInt()));
    }

    public static void sendLivingToolBreakParticles(Vec3 pos, ParticleColor color, double radius,
            ServerLevel level) {
        sendNear(level, pos.x, pos.y, pos.z, radius,
                new ParticleBurstPacket(ParticleBurstPacket.Kind.LIVING_TOOL_BREAK, pos, color));
    }

    public static void sendMonolithShatterBurst(Vec3 pos, double radius, ServerLevel level) {
        sendNear(level, pos.x, pos.y, pos.z, radius,
                new ParticleBurstPacket(ParticleBurstPacket.Kind.MONOLITH_SHATTER, pos));
    }

    public static void sendMonolithShardPulse(Vec3 pos, double radius, ServerLevel level) {
        sendNear(level, pos.x, pos.y, pos.z, radius,
                new ParticleBurstPacket(ParticleBurstPacket.Kind.MONOLITH_SHARD_PULSE, pos));
    }

    public static void sendSanguineOmenEffect(Vec3 pos, double radius, ServerLevel level, int durationTicks, float peakAlpha) {
        sendSanguineOmenEffect(pos, radius, level, durationTicks, peakAlpha, false);
    }

    public static void sendSanguineOmenEffect(Vec3 pos, double radius, ServerLevel level, int durationTicks, float peakAlpha, boolean screenOverlay) {
        sendNear(level, pos.x, pos.y, pos.z, radius,
                new SpawnSanguineOmenEffectPacket(pos, durationTicks, peakAlpha, level.random.nextInt(), screenOverlay));
    }

    public static void sendPomePulse(Vec3 pos, double radius, ServerLevel level) {
        sendNear(level, pos.x, pos.y, pos.z, radius,
                new ParticleBurstPacket(ParticleBurstPacket.Kind.POME_PULSE, pos));
    }

    public static void sendBlackVeil(Vec3 pos, double radius, ServerLevel level, int durationTicks) {
        sendNear(level, pos.x, pos.y, pos.z, radius + 48.0,
                new SpawnBlackVeilPacket(pos, (float) radius, durationTicks, level.random.nextInt()));
    }

    public static void sendMonolithicDislocationVisual(LivingEntity target, int durationTicks) {
        sendTracking(target,
                new EntityVisualPacket(EntityVisualPacket.Kind.MONOLITHIC_DISLOCATION, target.getId(), durationTicks));
    }

    public static void sendCrimsonFireVisual(LivingEntity target, int durationTicks) {
        sendTracking(target,
                new EntityVisualPacket(EntityVisualPacket.Kind.CRIMSON_FIRE, target.getId(), durationTicks));
    }

    public static void sendBloodBindingTendril(LivingEntity caster, LivingEntity target,
            int durationTicks, long seed) {
        if (!(caster.level() instanceof ServerLevel level)) return;
        sendNear(level, caster.getX(), caster.getY(), caster.getZ(), 64.0D,
                new SyncBloodBindingTendrilS2CPacket(caster.getId(), target.getId(), durationTicks, seed));
    }

    public static void syncPlayerAnimation(LivingEntity target, PlayerAnimationKind kind,
            boolean active, net.minecraft.world.InteractionHand hand) {
        sendTracking(target,
                new PacketSyncPlayerAnimation(target.getId(), kind, active, hand));
    }

    public static void sendClientElytraPacket() {
        sendToServer(new CPacketFlight());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  The send facade. Every payload leaves through these methods; nothing else
    //  in the mod calls PacketDistributor (DialogueSender aside).
    // ─────────────────────────────────────────────────────────────────────────

    /** Server to one player. Dialogue is routed through {@link DialogueSender}. */
    public static <P extends CustomPacketPayload> void sendToPlayer(
            ServerPlayer player, P payload) {
        // Semantic GameTests use detached server players so progression can be
        // exercised without a fake client connection. Logged-in players always
        // have one, and detached players have nothing to synchronize.
        if (player.connection == null) {
            return;
        }
        if (payload instanceof OpenDialoguePacket open) {
            DialogueSender.open(player, open);
            return;
        }
        PacketDistributor.sendToPlayer(player, payload);
    }

    /** Server to one player, several payloads in order. */
    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload, CustomPacketPayload... more) {
        sendToPlayer(player, payload);
        for (CustomPacketPayload next : more) {
            sendToPlayer(player, next);
        }
    }

    /** Server to every player within {@code radius} of a point in {@code level}. */
    public static <P extends CustomPacketPayload> void sendNear(ServerLevel level, double x, double y, double z,
            double radius, P payload) {
        PacketDistributor.sendToPlayersNear(level, null, x, y, z, radius, payload);
    }

    /** Server to every player tracking {@code entity}, and to the entity itself when it is a player. */
    public static <P extends CustomPacketPayload> void sendTracking(Entity entity, P payload) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload);
    }

    /**
     * Server to a player and everyone tracking them. Unlike {@link #sendTracking}, the player's own copy does not depend
     * on the player being tracked, so it also reaches a player who is not (yet) in a level.
     */
    public static <P extends CustomPacketPayload> void sendToPlayerAndTrackers(ServerPlayer player, P payload) {
        sendToPlayer(player, payload);
        PacketDistributor.sendToPlayersTrackingEntity(player, payload);
    }

    /** Server to every player in {@code level}. */
    public static <P extends CustomPacketPayload> void sendToDimension(ServerLevel level, P payload) {
        PacketDistributor.sendToPlayersInDimension(level, payload);
    }

    /** Server to every connected player. */
    public static <P extends CustomPacketPayload> void sendToAll(P payload) {
        PacketDistributor.sendToAllPlayers(payload);
    }

    /** Client to server. */
    public static <P extends CustomPacketPayload> void sendToServer(P payload) {
        PacketDistributor.sendToServer(payload);
    }
}
