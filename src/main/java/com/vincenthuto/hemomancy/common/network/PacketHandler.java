package com.vincenthuto.hemomancy.common.network;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.network.capa.*;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.*;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.bestiary.PacketRequestSpecimenBestiary;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.bestiary.PacketSyncSpecimenBestiary;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.*;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.scars.*;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.summon.KnownSummonsRequestPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.summon.KnownSummonsServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.unstained.*;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.visceral.OpenVisceralMirrorPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.visceral.VisceralMirrorCancelPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.visceral.VisceralMirrorExtractPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.visceral.VisceralMirrorUpdatePacket;
import com.vincenthuto.hemomancy.common.network.dialogue.DialogueOptionPacket;
import com.vincenthuto.hemomancy.common.network.dialogue.DialogueTopicOpenedPacket;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import com.vincenthuto.hemomancy.common.network.discovery.OpenInscriptionPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodCraftingKeyPressPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodFormationKeyPressPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.ToggleGourdKeyPacket;
import com.vincenthuto.hemomancy.common.network.morphling.*;
import com.vincenthuto.hemomancy.common.network.mission.OpenHarbingerAssignmentLedgerPacket;
import com.vincenthuto.hemomancy.common.network.mission.OpenBookOfObservancesPacket;
import com.vincenthuto.hemomancy.common.network.routing.PacketSpawnVeinSpiderCourier;
import com.vincenthuto.hemomancy.common.network.particle.*;
import com.vincenthuto.hemomancy.common.network.routing.PacketSyncSutureLinks;
import com.vincenthuto.hemomancy.common.network.summon.PacketPuppeteersSpindleAction;
import com.vincenthuto.hemomancy.common.network.summon.PacketCrossbarRadialAction;
import com.vincenthuto.hemomancy.common.network.will.WillPresenceCuePacket;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

// ── NeoForge 1.21 networking API ─────────────────────────────────────────────
// SimpleChannel / NetworkRegistry / PacketDistributor (old pattern) are REMOVED.
// Registration now happens via RegisterPayloadHandlersEvent on the mod bus.
// Sending now uses static PacketDistributor methods.

public class PacketHandler {

    // ─────────────────────────────────────────────────────────────────────────
    //  Channel registrar
    //  Called from Hemomancy#commonSetup, receives the NeoForge event bus.
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Register all packet payload types.
     *
     * <p>NeoForge 1.21 replaces the old {@code SimpleChannel.registerMessage} pattern with
     * {@link RegisterPayloadHandlersEvent}.  Each packet must implement
     * {@code net.minecraft.network.protocol.common.custom.CustomPacketPayload}.
     *
     * <p>Direction semantics:
     * <ul>
     *   <li>{@code playToClient} – server sends to client (replaces old CLIENT-bound packets)</li>
     *   <li>{@code playToServer} – client sends to server (replaces old SERVER-bound packets)</li>
     *   <li>{@code playBidirectional} – bidirectional</li>
     * </ul>
     *
     * <p>TODO: Every packet class listed here must be converted to implement
     * {@code CustomPacketPayload} with a static {@code TYPE} and {@code STREAM_CODEC}.
     * See {@link BloodVolumeClientPacket} and {@link BloodVolumeServerPacket} for
     * worked examples.
     */
    public static void registerChannels(IEventBus forgeBus) {
        forgeBus.addListener(PacketHandler::onRegisterPayloads);
    }

    private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        var net = event.registrar(Hemomancy.MOD_ID);

        // ── Blood Volume capability ───────────────────────────────────────────
        net.playToServer(BloodVolumeClientPacket.TYPE,
                BloodVolumeClientPacket.STREAM_CODEC, BloodVolumeClientPacket::handle);
        net.playToClient(BloodVolumeServerPacket.TYPE,
                BloodVolumeServerPacket.STREAM_CODEC, BloodVolumeServerPacket::handle);
        net.playToClient(PacketSyncBloodFlowDiagnostics.TYPE,
                PacketSyncBloodFlowDiagnostics.STREAM_CODEC, PacketSyncBloodFlowDiagnostics::handle);
        net.playToClient(PacketSyncMaxBloodDiagnostics.TYPE,
                PacketSyncMaxBloodDiagnostics.STREAM_CODEC, PacketSyncMaxBloodDiagnostics::handle);

        net.playToClient(PacketSyncPomeProgress.TYPE,
                PacketSyncPomeProgress.STREAM_CODEC, PacketSyncPomeProgress::handle);
        net.playToClient(PacketSyncFungalProjection.TYPE,
                PacketSyncFungalProjection.STREAM_CODEC, PacketSyncFungalProjection::handle);
        net.playToClient(PacketSyncChamberOfWill.TYPE,
                PacketSyncChamberOfWill.STREAM_CODEC, PacketSyncChamberOfWill::handle);
        net.playToClient(PacketSyncVesperFightScene.TYPE,
                PacketSyncVesperFightScene.STREAM_CODEC, PacketSyncVesperFightScene::handle);
        net.playToClient(LivingStaffProgressServerPacket.TYPE,
                LivingStaffProgressServerPacket.STREAM_CODEC, LivingStaffProgressServerPacket::handle);

        // ── Blood Tendency capability ─────────────────────────────────────────
        net.playToServer(BloodTendencyClientPacket.TYPE,
                BloodTendencyClientPacket.STREAM_CODEC, BloodTendencyClientPacket::handle);
        net.playToClient(BloodTendencyServerPacket.TYPE,
                BloodTendencyServerPacket.STREAM_CODEC, BloodTendencyServerPacket::handle);

        // ── Vascular System capability ────────────────────────────────────────
        net.playToServer(VascularSystemClientPacket.TYPE,
                VascularSystemClientPacket.STREAM_CODEC, VascularSystemClientPacket::handle);
        net.playToClient(VascularSystemServerPacket.TYPE,
                VascularSystemServerPacket.STREAM_CODEC, VascularSystemServerPacket::handle);

        // ── Scar system ───────────────────────────────────────────────────────
        net.playBidirectional(PacketEquipmentSync.TYPE, PacketEquipmentSync.STREAM_CODEC, PacketEquipmentSync::handle);
        net.playToClient(PacketSyncScarsState.TYPE, PacketSyncScarsState.STREAM_CODEC, PacketSyncScarsState::handle);
        net.playToServer(PacketPrepareScarPattern.TYPE, PacketPrepareScarPattern.STREAM_CODEC, PacketPrepareScarPattern::handle);
        net.playBidirectional(PacketGourdScarSync.TYPE, PacketGourdScarSync.STREAM_CODEC, PacketGourdScarSync::handle);
        net.playBidirectional(PacketOpenScarsInv.TYPE, PacketOpenScarsInv.STREAM_CODEC, PacketOpenScarsInv::handle);
        net.playBidirectional(PacketOpenSporeInv.TYPE, PacketOpenSporeInv.STREAM_CODEC, PacketOpenSporeInv::handle);
        net.playBidirectional(PacketOpenNormalInv.TYPE, PacketOpenNormalInv.STREAM_CODEC, PacketOpenNormalInv::handle);
        net.playBidirectional(CPacketFlight.TYPE, CPacketFlight.STREAM_CODEC, CPacketFlight::handle);
        net.playBidirectional(PacketUpdateScarPattern.TYPE, PacketUpdateScarPattern.STREAM_CODEC, PacketUpdateScarPattern::handle);
        net.playBidirectional(PacketScarCraftingEvent.TYPE, PacketScarCraftingEvent.STREAM_CODEC, PacketScarCraftingEvent::handle);
        net.playBidirectional(PacketLoadScarPattern.TYPE, PacketLoadScarPattern.STREAM_CODEC, PacketLoadScarPattern::handle);
        net.playBidirectional(PacketOpenScarBinder.TYPE, PacketOpenScarBinder.STREAM_CODEC, PacketOpenScarBinder::handle);
        net.playBidirectional(PacketToggleBinderMessage.TYPE, PacketToggleBinderMessage.STREAM_CODEC, PacketToggleBinderMessage::handle);
        net.playBidirectional(PacketCurvedHornAnimation.TYPE, PacketCurvedHornAnimation.STREAM_CODEC, PacketCurvedHornAnimation::handle);
        net.playBidirectional(ToggleGourdKeyPacket.TYPE, ToggleGourdKeyPacket.STREAM_CODEC, ToggleGourdKeyPacket::handle);
        net.playToServer(ToggleEquipmentLayerVisibilityPacket.TYPE,
                ToggleEquipmentLayerVisibilityPacket.STREAM_CODEC, ToggleEquipmentLayerVisibilityPacket::handle);
        net.playToClient(SyncEquipmentLayerVisibilityPacket.TYPE,
                SyncEquipmentLayerVisibilityPacket.STREAM_CODEC, SyncEquipmentLayerVisibilityPacket::handle);

        // ── Known Manipulations ───────────────────────────────────────────────
        net.playBidirectional(KnownManipulationClientPacket.TYPE, KnownManipulationClientPacket.STREAM_CODEC, KnownManipulationClientPacket::handle);
        net.playToClient(KnownManipulationServerPacket.TYPE, KnownManipulationServerPacket.STREAM_CODEC, KnownManipulationServerPacket::handle);
        net.playToClient(PacketSyncManipulationCostDiagnostics.TYPE,
                PacketSyncManipulationCostDiagnostics.STREAM_CODEC, PacketSyncManipulationCostDiagnostics::handle);
        net.playToClient(PacketSyncManipulationSlotDiagnostics.TYPE,
                PacketSyncManipulationSlotDiagnostics.STREAM_CODEC, PacketSyncManipulationSlotDiagnostics::handle);
        net.playBidirectional(DisplayKnownManipsPacket.TYPE, DisplayKnownManipsPacket.STREAM_CODEC, DisplayKnownManipsPacket::handle);
        net.playBidirectional(ChangeSelectedManipPacket.TYPE, ChangeSelectedManipPacket.STREAM_CODEC, ChangeSelectedManipPacket::handle);
        net.playBidirectional(UseQuickManipKeyPacket.TYPE, UseQuickManipKeyPacket.STREAM_CODEC, UseQuickManipKeyPacket::handle);
        net.playBidirectional(UseContManipKeyPacket.TYPE, UseContManipKeyPacket.STREAM_CODEC, UseContManipKeyPacket::handle);
        net.playBidirectional(UseManipKeyPacket.TYPE, UseManipKeyPacket.STREAM_CODEC, UseManipKeyPacket::handle);
        net.playBidirectional(ManipCooldownPacket.TYPE, ManipCooldownPacket.STREAM_CODEC, ManipCooldownPacket::handle);
        net.playBidirectional(UpdateCurrentManipPacket.TYPE, UpdateCurrentManipPacket.STREAM_CODEC, UpdateCurrentManipPacket::handle);
        net.playBidirectional(TeleportToVeinPacket.TYPE, TeleportToVeinPacket.STREAM_CODEC, TeleportToVeinPacket::handle);
        net.playBidirectional(SyncTrackingAvatarPacket.TYPE, SyncTrackingAvatarPacket.STREAM_CODEC, SyncTrackingAvatarPacket::handle);
        net.playBidirectional(UpdateCurrentVeinPacket.TYPE, UpdateCurrentVeinPacket.STREAM_CODEC, UpdateCurrentVeinPacket::handle);
        net.playBidirectional(StartCentrifugeButtonPacket.TYPE, StartCentrifugeButtonPacket.STREAM_CODEC, StartCentrifugeButtonPacket::handle);
        net.playToServer(ClearLoomStatePacket.TYPE, ClearLoomStatePacket.STREAM_CODEC, ClearLoomStatePacket::handle);
        net.playBidirectional(EquipManipulationPacket.TYPE, EquipManipulationPacket.STREAM_CODEC, EquipManipulationPacket::handle);
        net.playToServer(SynapticLoadoutActionPacket.TYPE, SynapticLoadoutActionPacket.STREAM_CODEC, SynapticLoadoutActionPacket::handle);
        net.playBidirectional(PacketOpenTendencyView.TYPE, PacketOpenTendencyView.STREAM_CODEC, PacketOpenTendencyView::handle);
        net.playBidirectional(PacketOpenVascularView.TYPE, PacketOpenVascularView.STREAM_CODEC, PacketOpenVascularView::handle);
        net.playBidirectional(PacketOpenScryingDiagnostics.TYPE, PacketOpenScryingDiagnostics.STREAM_CODEC, PacketOpenScryingDiagnostics::handle);

        // Still Arts
        net.playToClient(KnownStillArtsServerPacket.TYPE, KnownStillArtsServerPacket.STREAM_CODEC, KnownStillArtsServerPacket::handle);
        net.playToServer(UpdateSelectedStillArtPacket.TYPE, UpdateSelectedStillArtPacket.STREAM_CODEC, UpdateSelectedStillArtPacket::handle);
        net.playToServer(UseStillArtKeyPacket.TYPE, UseStillArtKeyPacket.STREAM_CODEC, UseStillArtKeyPacket::handle);
        net.playToClient(StillArtCooldownPacket.TYPE, StillArtCooldownPacket.STREAM_CODEC, StillArtCooldownPacket::handle);
        net.playToServer(KnownSummonsRequestPacket.TYPE, KnownSummonsRequestPacket.STREAM_CODEC, KnownSummonsRequestPacket::handle);
        net.playToClient(KnownSummonsServerPacket.TYPE, KnownSummonsServerPacket.STREAM_CODEC, KnownSummonsServerPacket::handle);
        net.playToServer(PacketRequestSpecimenBestiary.TYPE, PacketRequestSpecimenBestiary.STREAM_CODEC, PacketRequestSpecimenBestiary::handle);
        net.playToClient(PacketSyncSpecimenBestiary.TYPE, PacketSyncSpecimenBestiary.STREAM_CODEC, PacketSyncSpecimenBestiary::handle);
        net.playToServer(PacketPuppeteersSpindleAction.TYPE, PacketPuppeteersSpindleAction.STREAM_CODEC, PacketPuppeteersSpindleAction::handle);
        net.playToServer(PacketCrossbarRadialAction.TYPE, PacketCrossbarRadialAction.STREAM_CODEC, PacketCrossbarRadialAction::handle);
        net.playToClient(PacketSyncLastDeathMemory.TYPE, PacketSyncLastDeathMemory.STREAM_CODEC, PacketSyncLastDeathMemory::handle);
        net.playToClient(PacketSyncSutureLinks.TYPE, PacketSyncSutureLinks.STREAM_CODEC, PacketSyncSutureLinks::handle);
        net.playToClient(PacketSpawnVeinSpiderCourier.TYPE,
                PacketSpawnVeinSpiderCourier.STREAM_CODEC, PacketSpawnVeinSpiderCourier::handle);

        // ── Key-bind packets ──────────────────────────────────────────────────
        net.playToServer(BloodFormationKeyPressPacket.TYPE, BloodFormationKeyPressPacket.STREAM_CODEC, BloodFormationKeyPressPacket::handle);
        net.playToServer(BloodCraftingKeyPressPacket.TYPE, BloodCraftingKeyPressPacket.STREAM_CODEC, BloodCraftingKeyPressPacket::handle);
        net.playToServer(ActivateArmorSetAbilityC2SPacket.TYPE, ActivateArmorSetAbilityC2SPacket.STREAM_CODEC,
                ActivateArmorSetAbilityC2SPacket::handle);
        net.playToClient(SyncArmorSetAbilityCooldownS2CPacket.TYPE,
                SyncArmorSetAbilityCooldownS2CPacket.STREAM_CODEC,
                SyncArmorSetAbilityCooldownS2CPacket::handle);
        net.playToServer(ToggleSilentSlippingC2SPacket.TYPE, ToggleSilentSlippingC2SPacket.STREAM_CODEC,
                ToggleSilentSlippingC2SPacket::handle);
        net.playToClient(SyncSilentSlippingStateS2CPacket.TYPE, SyncSilentSlippingStateS2CPacket.STREAM_CODEC,
                SyncSilentSlippingStateS2CPacket::handle);
        net.playToClient(SyncMonolithicDislocationVisualS2CPacket.TYPE,
                SyncMonolithicDislocationVisualS2CPacket.STREAM_CODEC,
                SyncMonolithicDislocationVisualS2CPacket::handle);
        net.playToClient(SyncCrimsonFireVisualS2CPacket.TYPE,
                SyncCrimsonFireVisualS2CPacket.STREAM_CODEC,
                SyncCrimsonFireVisualS2CPacket::handle);

        // ── Particles ─────────────────────────────────────────────────────────
        net.playToServer(GroundBloodDrawPacket.TYPE, GroundBloodDrawPacket.STREAM_CODEC, GroundBloodDrawPacket::handle);
        net.playToClient(EntityHitParticlePacket.TYPE, EntityHitParticlePacket.STREAM_CODEC, EntityHitParticlePacket::handle);
        net.playToClient(AirBloodDrawPacket.TYPE, AirBloodDrawPacket.STREAM_CODEC, AirBloodDrawPacket::handle);
        net.playToClient(SpawnFlaskParticlesPacket.TYPE, SpawnFlaskParticlesPacket.STREAM_CODEC, SpawnFlaskParticlesPacket::handle);
        net.playToClient(SpawnAvatarParticlesPacket.TYPE, SpawnAvatarParticlesPacket.STREAM_CODEC, SpawnAvatarParticlesPacket::handle);
        net.playToClient(SpawnBloodClawParticlesPacket.TYPE, SpawnBloodClawParticlesPacket.STREAM_CODEC, SpawnBloodClawParticlesPacket::handle);
        net.playToClient(SpawnClawSlashPacket.TYPE, SpawnClawSlashPacket.STREAM_CODEC, SpawnClawSlashPacket::handle);
        net.playToClient(SpawnBrazierItemAbsorptionParticlesPacket.TYPE,
                SpawnBrazierItemAbsorptionParticlesPacket.STREAM_CODEC,
                SpawnBrazierItemAbsorptionParticlesPacket::handle);
        net.playToClient(SpawnLivingToolParticlesPacket.TYPE, SpawnLivingToolParticlesPacket.STREAM_CODEC, SpawnLivingToolParticlesPacket::handle);
        net.playToClient(SpawnMonolithShatterBurstPacket.TYPE, SpawnMonolithShatterBurstPacket.STREAM_CODEC, SpawnMonolithShatterBurstPacket::handle);
        net.playToClient(SpawnSanguineOmenEffectPacket.TYPE, SpawnSanguineOmenEffectPacket.STREAM_CODEC, SpawnSanguineOmenEffectPacket::handle);
        net.playToClient(CardinalRiteImpactPacket.TYPE, CardinalRiteImpactPacket.STREAM_CODEC, CardinalRiteImpactPacket::handle);
        net.playToClient(SpawnBlackVeilPacket.TYPE, SpawnBlackVeilPacket.STREAM_CODEC, SpawnBlackVeilPacket::handle);
        net.playToClient(SpawnPomePulsePacket.TYPE, SpawnPomePulsePacket.STREAM_CODEC, SpawnPomePulsePacket::handle);
        net.playToClient(WillPresenceCuePacket.TYPE, WillPresenceCuePacket.STREAM_CODEC, WillPresenceCuePacket::handle);
        // HutosLib registers this payload in HLPacketHandler; re-registering here causes
        // "already registered" crashes for hutoslib:packet_spawn_lightning.

        // ── Skill tree ────────────────────────────────────────────────────────
        net.playToServer(PacketUnlockSkill.TYPE, PacketUnlockSkill.STREAM_CODEC, PacketUnlockSkill::handle);
        net.playToServer(ImprintMnemonicBlueprintPacket.TYPE, ImprintMnemonicBlueprintPacket.STREAM_CODEC,
                ImprintMnemonicBlueprintPacket::handle);
        net.playToClient(PacketSyncSkills.TYPE, PacketSyncSkills.STREAM_CODEC, PacketSyncSkills::handle);

        // ── Bloodline pool ────────────────────────────────────────────────────
        net.playToServer(PacketLumpDonate.TYPE, PacketLumpDonate.STREAM_CODEC, PacketLumpDonate::handle);
        net.playToServer(PacketUpdatePoolSettings.TYPE, PacketUpdatePoolSettings.STREAM_CODEC, PacketUpdatePoolSettings::handle);
        net.playToClient(PacketSyncBloodlinePool.TYPE, PacketSyncBloodlinePool.STREAM_CODEC, PacketSyncBloodlinePool::handle);
        net.playToClient(PacketOpenBloodlinePoolScreen.TYPE, PacketOpenBloodlinePoolScreen.STREAM_CODEC, PacketOpenBloodlinePoolScreen::handle);
        net.playToServer(PacketRequestPoolData.TYPE, PacketRequestPoolData.STREAM_CODEC, PacketRequestPoolData::handle);
        net.playToServer(PacketKickBloodlinePlayer.TYPE, PacketKickBloodlinePlayer.STREAM_CODEC, PacketKickBloodlinePlayer::handle);
        net.playToServer(PacketBloodlineMessage.TYPE, PacketBloodlineMessage.STREAM_CODEC, PacketBloodlineMessage::handle);

        // ── Cardinal rites ────────────────────────────────────────────────────
        net.playToClient(PacketSyncActiveRites.TYPE, PacketSyncActiveRites.STREAM_CODEC, PacketSyncActiveRites::handle);
        net.playToClient(PacketCardinalRiteStaffPlanting.TYPE,
                PacketCardinalRiteStaffPlanting.STREAM_CODEC,
                PacketCardinalRiteStaffPlanting::handle);
        net.playToClient(PacketSyncIchorianKnowledge.TYPE, PacketSyncIchorianKnowledge.STREAM_CODEC,
                PacketSyncIchorianKnowledge::handle);
        net.playToClient(PacketSyncFaneBoundaries.TYPE, PacketSyncFaneBoundaries.STREAM_CODEC, PacketSyncFaneBoundaries::handle);
        net.playToClient(PacketBloodCraftRing.TYPE, PacketBloodCraftRing.STREAM_CODEC, PacketBloodCraftRing::handle);
        net.playToClient(PacketBloodStructureFeed.TYPE, PacketBloodStructureFeed.STREAM_CODEC, PacketBloodStructureFeed::handle);
        net.playToClient(PacketSanguineFormationProjection.TYPE,
                PacketSanguineFormationProjection.STREAM_CODEC, PacketSanguineFormationProjection::handle);
        net.playToClient(PacketBloodStructureOfferingBurst.TYPE, PacketBloodStructureOfferingBurst.STREAM_CODEC, PacketBloodStructureOfferingBurst::handle);

        // ── Degree / Unstained progress ───────────────────────────────────────
        net.playToClient(PacketSyncDegree.TYPE, PacketSyncDegree.STREAM_CODEC, PacketSyncDegree::handle);
        net.playToClient(PacketSyncLiberKnowledge.TYPE, PacketSyncLiberKnowledge.STREAM_CODEC, PacketSyncLiberKnowledge::handle);
        net.playToClient(PacketSyncUnstainedProgress.TYPE, PacketSyncUnstainedProgress.STREAM_CODEC, PacketSyncUnstainedProgress::handle);
        net.playToServer(PacketToggleUnstainedBonus.TYPE, PacketToggleUnstainedBonus.STREAM_CODEC, PacketToggleUnstainedBonus::handle);

        // ── Morphling Jar ─────────────────────────────────────────────────────
        net.playBidirectional(JarTogglePickupPacket.TYPE, JarTogglePickupPacket.STREAM_CODEC, JarTogglePickupPacket::handle);
        net.playBidirectional(OpenMorphlingJarPacket.TYPE, OpenMorphlingJarPacket.STREAM_CODEC, OpenMorphlingJarPacket::handle);
        net.playBidirectional(ToggleMorphlingJarMessagePacket.TYPE, ToggleMorphlingJarMessagePacket.STREAM_CODEC, ToggleMorphlingJarMessagePacket::handle);
        net.playBidirectional(OpenLivingStaffPacket.TYPE, OpenLivingStaffPacket.STREAM_CODEC, OpenLivingStaffPacket::handle);
        net.playBidirectional(PacketUpdateLivingStaffMorph.TYPE, PacketUpdateLivingStaffMorph.STREAM_CODEC, PacketUpdateLivingStaffMorph::handle);
        net.playBidirectional(ChangeMorphKeyPacket.TYPE, ChangeMorphKeyPacket.STREAM_CODEC, ChangeMorphKeyPacket::handle);
        net.playToClient(SyncEquippedMorphlingPacket.TYPE, SyncEquippedMorphlingPacket.STREAM_CODEC, SyncEquippedMorphlingPacket::handle);

        // ── Debug / structure spawner ─────────────────────────────────────────
        net.playToServer(PlaceStructurePacket.TYPE, PlaceStructurePacket.STREAM_CODEC, PlaceStructurePacket::handle);

        // ── Dialogue system ───────────────────────────────────────────────────
        net.playToClient(OpenDialoguePacket.TYPE, OpenDialoguePacket.STREAM_CODEC, OpenDialoguePacket::handle);
        net.playToServer(DialogueOptionPacket.TYPE, DialogueOptionPacket.STREAM_CODEC, DialogueOptionPacket::handle);
        net.playToServer(DialogueTopicOpenedPacket.TYPE, DialogueTopicOpenedPacket.STREAM_CODEC,
                DialogueTopicOpenedPacket::handle);
        net.playToClient(OpenInscriptionPacket.TYPE, OpenInscriptionPacket.STREAM_CODEC, OpenInscriptionPacket::handle);
        net.playToClient(OpenHarbingerAssignmentLedgerPacket.TYPE,
                OpenHarbingerAssignmentLedgerPacket.STREAM_CODEC, OpenHarbingerAssignmentLedgerPacket::handle);
        net.playToClient(OpenBookOfObservancesPacket.TYPE,
                OpenBookOfObservancesPacket.STREAM_CODEC, OpenBookOfObservancesPacket::handle);

        // ── World events ──────────────────────────────────────────────────────
        net.playToClient(PacketSyncQliphothBlooms.TYPE, PacketSyncQliphothBlooms.STREAM_CODEC, PacketSyncQliphothBlooms::handle);
        net.playToClient(PacketSyncBloodMoon.TYPE, PacketSyncBloodMoon.STREAM_CODEC, PacketSyncBloodMoon::handle);

        // ── Visceral Mirror ───────────────────────────────────────────────────
        net.playToClient(OpenVisceralMirrorPacket.TYPE, OpenVisceralMirrorPacket.STREAM_CODEC, OpenVisceralMirrorPacket::handle);
        net.playToServer(VisceralMirrorExtractPacket.TYPE, VisceralMirrorExtractPacket.STREAM_CODEC, VisceralMirrorExtractPacket::handle);
        net.playToServer(VisceralMirrorCancelPacket.TYPE, VisceralMirrorCancelPacket.STREAM_CODEC, VisceralMirrorCancelPacket::handle);
        net.playToClient(VisceralMirrorUpdatePacket.TYPE, VisceralMirrorUpdatePacket.STREAM_CODEC, VisceralMirrorUpdatePacket::handle);

        // ── Semi-Sentient Construct screen ────────────────────────────────────
        net.playToClient(OpenSSCScreenPacket.TYPE, OpenSSCScreenPacket.STREAM_CODEC, OpenSSCScreenPacket::handle);

        // ── Ledger ────────────────────────────────────────────────────────────
        net.playToServer(PacketLedgerAction.TYPE, PacketLedgerAction.STREAM_CODEC, PacketLedgerAction::handle);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Convenience send helpers
    //  NeoForge 1.21: PacketDistributor now has static send methods.
    //  New:  PacketDistributor.sendToPlayer(player, msg)
    // ─────────────────────────────────────────────────────────────────────────

    public static void sendAvatarHitParticles(Vec3 pos, ParticleColor color, double radius,
            ServerLevel level) {
        // NeoForge 1.21: sendToPlayersNear replaces PacketDistributor.NEAR.with(TargetPoint)
        PacketDistributor.sendToPlayersNear(level, null, pos.x, pos.y, pos.z, radius,
                new SpawnAvatarParticlesPacket(pos, color));
    }

    public static void sendBloodFlaskParticles(Vec3 pos, ParticleColor color, double radius,
            ServerLevel level) {
        PacketDistributor.sendToPlayersNear(level, null, pos.x, pos.y, pos.z, radius,
                new SpawnFlaskParticlesPacket(pos, color));
    }

    public static void sendClawParticles(Vec3 pos, ParticleColor color, double radius,
            ServerLevel level) {
        PacketDistributor.sendToPlayersNear(level, null, pos.x, pos.y, pos.z, radius,
                new SpawnBloodClawParticlesPacket(pos, color));
    }

    public static void sendClawSlash(Vec3 pos, Vec3 forward, ParticleColor color, boolean ambush, float scale,
            double radius, ServerLevel level) {
        PacketDistributor.sendToPlayersNear(level, null, pos.x, pos.y, pos.z, radius,
                new SpawnClawSlashPacket(pos, forward, color, ambush, scale, level.random.nextInt()));
    }

    public static void sendLivingToolBreakParticles(Vec3 pos, ParticleColor color, double radius,
            ServerLevel level) {
        PacketDistributor.sendToPlayersNear(level, null, pos.x, pos.y, pos.z, radius,
                new SpawnLivingToolParticlesPacket(pos, color));
    }

    public static void sendMonolithShatterBurst(Vec3 pos, double radius, ServerLevel level) {
        PacketDistributor.sendToPlayersNear(level, null, pos.x, pos.y, pos.z, radius,
                new SpawnMonolithShatterBurstPacket(pos));
    }

    public static void sendMonolithShardPulse(Vec3 pos, double radius, ServerLevel level) {
        PacketDistributor.sendToPlayersNear(level, null, pos.x, pos.y, pos.z, radius,
                new SpawnMonolithShatterBurstPacket(pos, true));
    }

    public static void sendSanguineOmenEffect(Vec3 pos, double radius, ServerLevel level, int durationTicks, float peakAlpha) {
        sendSanguineOmenEffect(pos, radius, level, durationTicks, peakAlpha, false);
    }

    public static void sendSanguineOmenEffect(Vec3 pos, double radius, ServerLevel level, int durationTicks, float peakAlpha, boolean screenOverlay) {
        PacketDistributor.sendToPlayersNear(level, null, pos.x, pos.y, pos.z, radius,
                new SpawnSanguineOmenEffectPacket(pos, durationTicks, peakAlpha, level.random.nextInt(), screenOverlay));
    }

    public static void sendPomePulse(Vec3 pos, double radius, ServerLevel level) {
        PacketDistributor.sendToPlayersNear(level, null, pos.x, pos.y, pos.z, radius,
                new SpawnPomePulsePacket(pos));
    }

    public static void sendBlackVeil(Vec3 pos, double radius, ServerLevel level, int durationTicks) {
        PacketDistributor.sendToPlayersNear(level, null, pos.x, pos.y, pos.z, radius + 48.0,
                new SpawnBlackVeilPacket(pos, (float) radius, durationTicks, level.random.nextInt()));
    }

    public static void sendMonolithicDislocationVisual(LivingEntity target, int durationTicks) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(target,
                new SyncMonolithicDislocationVisualS2CPacket(target.getId(), durationTicks));
    }

    public static void sendCrimsonFireVisual(LivingEntity target, int durationTicks) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(target,
                new SyncCrimsonFireVisualS2CPacket(target.getId(), durationTicks));
    }

    public static void sendClientElytraPacket() {
        PacketDistributor.sendToServer(new CPacketFlight());
    }

    /** Helper – send a payload to a specific player (server → client). */
    public static <P extends CustomPacketPayload> void sendToPlayer(
            ServerPlayer player, P payload) {
        // Semantic GameTests use detached server players so progression can be
        // exercised without a fake client connection. Logged-in players always
        // have one, and detached players have nothing to synchronize.
        if (player.connection == null) {
            return;
        }
        PacketDistributor.sendToPlayer(player, payload);
    }

    /** Helper – send a payload to the server (client → server). */
    public static <P extends CustomPacketPayload> void sendToServer(P payload) {
        PacketDistributor.sendToServer(payload);
    }
}
