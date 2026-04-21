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
import com.vincenthuto.hemomancy.common.network.capa.PacketKickBloodlinePlayer;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
// ── NeoForge 1.21 networking API ─────────────────────────────────────────────
// SimpleChannel / NetworkRegistry / PacketDistributor (old pattern) are REMOVED.
// Registration now happens via RegisterPayloadsEvent on the mod bus.
// Sending now uses static PacketDistributor methods.
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadsEvent;

public class PacketHandler {

    // ─────────────────────────────────────────────────────────────────────────
    //  Channel registrar
    //  Called from Hemomancy#commonSetup, receives the NeoForge event bus.
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Register all packet payload types.
     *
     * <p>NeoForge 1.21 replaces the old {@code SimpleChannel.registerMessage} pattern with
     * {@link RegisterPayloadsEvent}.  Each packet must implement
     * {@code net.neoforged.neoforge.network.codec.NetworkPayload} (i.e., {@code CustomPacketPayload}).
     *
     * <p>Direction semantics:
     * <ul>
     *   <li>{@code playToClient} – server sends to client (replaces old CLIENT-bound packets)</li>
     *   <li>{@code playToServer} – client sends to server (replaces old SERVER-bound packets)</li>
     *   <li>{@code play}         – bidirectional</li>
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

    private static void onRegisterPayloads(RegisterPayloadsEvent event) {
        var net = event.registrar(Hemomancy.MOD_ID);

        // ── Blood Volume capability ───────────────────────────────────────────
        net.playToServer(BloodVolumeClientPacket.TYPE,
                BloodVolumeClientPacket.STREAM_CODEC, BloodVolumeClientPacket::handle);
        net.playToClient(BloodVolumeServerPacket.TYPE,
                BloodVolumeServerPacket.STREAM_CODEC, BloodVolumeServerPacket::handle);

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
        net.play(PacketScarSync.TYPE, PacketScarSync.STREAM_CODEC, PacketScarSync::handle);
        net.play(PacketGourdScarSync.TYPE, PacketGourdScarSync.STREAM_CODEC, PacketGourdScarSync::handle);
        net.play(PacketOpenScarsInv.TYPE, PacketOpenScarsInv.STREAM_CODEC, PacketOpenScarsInv::handle);
        net.play(PacketOpenSporeInv.TYPE, PacketOpenSporeInv.STREAM_CODEC, PacketOpenSporeInv::handle);
        net.play(PacketOpenNormalInv.TYPE, PacketOpenNormalInv.STREAM_CODEC, PacketOpenNormalInv::handle);
        net.play(CPacketFlight.TYPE, CPacketFlight.STREAM_CODEC, CPacketFlight::handle);
        net.play(PacketUpdateScarPattern.TYPE, PacketUpdateScarPattern.STREAM_CODEC, PacketUpdateScarPattern::handle);
        net.play(PacketScarCraftingEvent.TYPE, PacketScarCraftingEvent.STREAM_CODEC, PacketScarCraftingEvent::handle);
        net.play(PacketLoadScarPattern.TYPE, PacketLoadScarPattern.STREAM_CODEC, PacketLoadScarPattern::handle);
        net.play(PacketOpenScarBinder.TYPE, PacketOpenScarBinder.STREAM_CODEC, PacketOpenScarBinder::handle);
        net.play(PacketToggleBinderMessage.TYPE, PacketToggleBinderMessage.STREAM_CODEC, PacketToggleBinderMessage::handle);
        net.play(PacketCurvedHornAnimation.TYPE, PacketCurvedHornAnimation.STREAM_CODEC, PacketCurvedHornAnimation::handle);
        net.play(ToggleGourdKeyPacket.TYPE, ToggleGourdKeyPacket.STREAM_CODEC, ToggleGourdKeyPacket::handle);

        // ── Known Manipulations ───────────────────────────────────────────────
        net.play(KnownManipulationClientPacket.TYPE, KnownManipulationClientPacket.STREAM_CODEC, KnownManipulationClientPacket::handle);
        net.play(KnownManipulationServerPacket.TYPE, KnownManipulationServerPacket.STREAM_CODEC, KnownManipulationServerPacket::handle);
        net.play(DisplayKnownManipsPacket.TYPE, DisplayKnownManipsPacket.STREAM_CODEC, DisplayKnownManipsPacket::handle);
        net.play(ChangeSelectedManipPacket.TYPE, ChangeSelectedManipPacket.STREAM_CODEC, ChangeSelectedManipPacket::handle);
        net.play(UseQuickManipKeyPacket.TYPE, UseQuickManipKeyPacket.STREAM_CODEC, UseQuickManipKeyPacket::handle);
        net.play(UseContManipKeyPacket.TYPE, UseContManipKeyPacket.STREAM_CODEC, UseContManipKeyPacket::handle);
        net.play(UseManipKeyPacket.TYPE, UseManipKeyPacket.STREAM_CODEC, UseManipKeyPacket::handle);
        net.play(ManipCooldownPacket.TYPE, ManipCooldownPacket.STREAM_CODEC, ManipCooldownPacket::handle);
        net.play(UpdateCurrentManipPacket.TYPE, UpdateCurrentManipPacket.STREAM_CODEC, UpdateCurrentManipPacket::handle);
        net.play(TeleportToVeinPacket.TYPE, TeleportToVeinPacket.STREAM_CODEC, TeleportToVeinPacket::handle);
        net.play(SyncTrackingAvatarPacket.TYPE, SyncTrackingAvatarPacket.STREAM_CODEC, SyncTrackingAvatarPacket::handle);
        net.play(UpdateCurrentVeinPacket.TYPE, UpdateCurrentVeinPacket.STREAM_CODEC, UpdateCurrentVeinPacket::handle);
        net.play(StartCentrifugeButtonPacket.TYPE, StartCentrifugeButtonPacket.STREAM_CODEC, StartCentrifugeButtonPacket::handle);
        net.play(EquipManipulationPacket.TYPE, EquipManipulationPacket.STREAM_CODEC, EquipManipulationPacket::handle);
        net.play(PacketOpenTendencyView.TYPE, PacketOpenTendencyView.STREAM_CODEC, PacketOpenTendencyView::handle);
        net.play(PacketOpenVascularView.TYPE, PacketOpenVascularView.STREAM_CODEC, PacketOpenVascularView::handle);

        // ── Key-bind packets ──────────────────────────────────────────────────
        net.playToServer(BloodFormationKeyPressPacket.TYPE, BloodFormationKeyPressPacket.STREAM_CODEC, BloodFormationKeyPressPacket::handle);
        net.playToServer(BloodCraftingKeyPressPacket.TYPE, BloodCraftingKeyPressPacket.STREAM_CODEC, BloodCraftingKeyPressPacket::handle);

        // ── Particles ─────────────────────────────────────────────────────────
        net.playToClient(GroundBloodDrawPacket.TYPE, GroundBloodDrawPacket.STREAM_CODEC, GroundBloodDrawPacket::handle);
        net.playToClient(EntityHitParticlePacket.TYPE, EntityHitParticlePacket.STREAM_CODEC, EntityHitParticlePacket::handle);
        net.playToClient(AirBloodDrawPacket.TYPE, AirBloodDrawPacket.STREAM_CODEC, AirBloodDrawPacket::handle);
        net.playToClient(SpawnFlaskParticlesPacket.TYPE, SpawnFlaskParticlesPacket.STREAM_CODEC, SpawnFlaskParticlesPacket::handle);
        net.playToClient(SpawnAvatarParticlesPacket.TYPE, SpawnAvatarParticlesPacket.STREAM_CODEC, SpawnAvatarParticlesPacket::handle);
        net.playToClient(SpawnBloodClawParticlesPacket.TYPE, SpawnBloodClawParticlesPacket.STREAM_CODEC, SpawnBloodClawParticlesPacket::handle);
        net.playToClient(SpawnLivingToolParticlesPacket.TYPE, SpawnLivingToolParticlesPacket.STREAM_CODEC, SpawnLivingToolParticlesPacket::handle);
        net.playToClient(SpawnMonolithShatterBurstPacket.TYPE, SpawnMonolithShatterBurstPacket.STREAM_CODEC, SpawnMonolithShatterBurstPacket::handle);
        net.playToClient(PacketSpawnLightningParticle.TYPE, PacketSpawnLightningParticle.STREAM_CODEC, PacketSpawnLightningParticle::handle);

        // ── Skill tree ────────────────────────────────────────────────────────
        net.playToServer(PacketUnlockSkill.TYPE, PacketUnlockSkill.STREAM_CODEC, PacketUnlockSkill::handle);
        net.playToClient(PacketSyncSkills.TYPE, PacketSyncSkills.STREAM_CODEC, PacketSyncSkills::handle);

        // ── Bloodline pool ────────────────────────────────────────────────────
        net.playToServer(PacketLumpDonate.TYPE, PacketLumpDonate.STREAM_CODEC, PacketLumpDonate::handle);
        net.playToServer(PacketUpdatePoolSettings.TYPE, PacketUpdatePoolSettings.STREAM_CODEC, PacketUpdatePoolSettings::handle);
        net.playToClient(PacketSyncBloodlinePool.TYPE, PacketSyncBloodlinePool.STREAM_CODEC, PacketSyncBloodlinePool::handle);
        net.playToServer(PacketRequestPoolData.TYPE, PacketRequestPoolData.STREAM_CODEC, PacketRequestPoolData::handle);
        net.playToServer(PacketKickBloodlinePlayer.TYPE, PacketKickBloodlinePlayer.STREAM_CODEC, PacketKickBloodlinePlayer::handle);
        net.playToServer(PacketBloodlineMessage.TYPE, PacketBloodlineMessage.STREAM_CODEC, PacketBloodlineMessage::handle);

        // ── Cardinal rites ────────────────────────────────────────────────────
        net.playToClient(PacketSyncActiveRites.TYPE, PacketSyncActiveRites.STREAM_CODEC, PacketSyncActiveRites::handle);
        net.playToClient(PacketBloodCraftRing.TYPE, PacketBloodCraftRing.STREAM_CODEC, PacketBloodCraftRing::handle);

        // ── Degree / Unstained progress ───────────────────────────────────────
        net.playToClient(PacketSyncDegree.TYPE, PacketSyncDegree.STREAM_CODEC, PacketSyncDegree::handle);
        net.playToClient(PacketSyncUnstainedProgress.TYPE, PacketSyncUnstainedProgress.STREAM_CODEC, PacketSyncUnstainedProgress::handle);
        net.playToServer(PacketToggleUnstainedBonus.TYPE, PacketToggleUnstainedBonus.STREAM_CODEC, PacketToggleUnstainedBonus::handle);

        // ── Morphling Jar ─────────────────────────────────────────────────────
        net.play(JarTogglePickupPacket.TYPE, JarTogglePickupPacket.STREAM_CODEC, JarTogglePickupPacket::handle);
        net.play(OpenMorphlingJarPacket.TYPE, OpenMorphlingJarPacket.STREAM_CODEC, OpenMorphlingJarPacket::handle);
        net.play(ToggleMorphlingJarMessagePacket.TYPE, ToggleMorphlingJarMessagePacket.STREAM_CODEC, ToggleMorphlingJarMessagePacket::handle);
        net.play(OpenLivingStaffPacket.TYPE, OpenLivingStaffPacket.STREAM_CODEC, OpenLivingStaffPacket::handle);
        net.play(PacketUpdateLivingStaffMorph.TYPE, PacketUpdateLivingStaffMorph.STREAM_CODEC, PacketUpdateLivingStaffMorph::handle);
        net.play(ChangeMorphKeyPacket.TYPE, ChangeMorphKeyPacket.STREAM_CODEC, ChangeMorphKeyPacket::handle);
        net.playToClient(SyncEquippedMorphlingPacket.TYPE, SyncEquippedMorphlingPacket.STREAM_CODEC, SyncEquippedMorphlingPacket::handle);

        // ── Debug / structure spawner ─────────────────────────────────────────
        net.playToServer(PlaceStructurePacket.TYPE, PlaceStructurePacket.STREAM_CODEC, PlaceStructurePacket::handle);

        // ── Dialogue system ───────────────────────────────────────────────────
        net.playToClient(OpenDialoguePacket.TYPE, OpenDialoguePacket.STREAM_CODEC, OpenDialoguePacket::handle);
        net.playToServer(DialogueOptionPacket.TYPE, DialogueOptionPacket.STREAM_CODEC, DialogueOptionPacket::handle);

        // ── World events ──────────────────────────────────────────────────────
        net.playToClient(PacketSyncQliphothBlooms.TYPE, PacketSyncQliphothBlooms.STREAM_CODEC, PacketSyncQliphothBlooms::handle);
        net.playToClient(PacketSyncBloodMoon.TYPE, PacketSyncBloodMoon.STREAM_CODEC, PacketSyncBloodMoon::handle);

        // ── Visceral Mirror ───────────────────────────────────────────────────
        net.playToClient(OpenVisceralMirrorPacket.TYPE, OpenVisceralMirrorPacket.STREAM_CODEC, OpenVisceralMirrorPacket::handle);
        net.playToServer(VisceralMirrorExtractPacket.TYPE, VisceralMirrorExtractPacket.STREAM_CODEC, VisceralMirrorExtractPacket::handle);
        net.playToServer(VisceralMirrorCancelPacket.TYPE, VisceralMirrorCancelPacket.STREAM_CODEC, VisceralMirrorCancelPacket::handle);
        net.playToClient(VisceralMirrorUpdatePacket.TYPE, VisceralMirrorUpdatePacket.STREAM_CODEC, VisceralMirrorUpdatePacket::handle);

        // ── Ledger ────────────────────────────────────────────────────────────
        net.playToServer(PacketLedgerAction.TYPE, PacketLedgerAction.STREAM_CODEC, PacketLedgerAction::handle);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Convenience send helpers
    //  NeoForge 1.21: PacketDistributor now has static send methods.
    //  Old:  CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg)
    //  New:  PacketDistributor.sendToPlayer(player, msg)
    // ─────────────────────────────────────────────────────────────────────────

    public static void sendAvatarHitParticles(Vec3 pos, ParticleColor color, double radius,
            ResourceKey<Level> dimension) {
        // NeoForge 1.21: sendToPlayersNear replaces PacketDistributor.NEAR.with(TargetPoint)
        PacketDistributor.sendToPlayersNear(null, null, pos.x, pos.y, pos.z, radius,
                new SpawnAvatarParticlesPacket(pos, color));
    }

    public static void sendBloodFlaskParticles(Vec3 pos, ParticleColor color, double radius,
            ResourceKey<Level> dimension) {
        PacketDistributor.sendToPlayersNear(null, null, pos.x, pos.y, pos.z, radius,
                new SpawnFlaskParticlesPacket(pos, color));
    }

    public static void sendClawParticles(Vec3 pos, ParticleColor color, double radius,
            ResourceKey<Level> dimension) {
        PacketDistributor.sendToPlayersNear(null, null, pos.x, pos.y, pos.z, radius,
                new SpawnBloodClawParticlesPacket(pos, color));
    }

    public static void sendLivingToolBreakParticles(Vec3 pos, ParticleColor color, double radius,
            ResourceKey<Level> dimension) {
        PacketDistributor.sendToPlayersNear(null, null, pos.x, pos.y, pos.z, radius,
                new SpawnLivingToolParticlesPacket(pos, color));
    }

    public static void sendMonolithShatterBurst(Vec3 pos, double radius, ResourceKey<Level> dimension) {
        PacketDistributor.sendToPlayersNear(null, null, pos.x, pos.y, pos.z, radius,
                new SpawnMonolithShatterBurstPacket(pos));
    }

    /** Helper – send a payload to a specific player (server → client). */
    public static <P extends net.neoforged.neoforge.network.codec.NetworkPayload> void sendToPlayer(
            ServerPlayer player, P payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    /** Helper – send a payload to the server (client → server). */
    public static <P extends net.neoforged.neoforge.network.codec.NetworkPayload> void sendToServer(P payload) {
        PacketDistributor.sendToServer(payload);
    }
}

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
import com.vincenthuto.hemomancy.common.network.capa.PacketKickBloodlinePlayer;
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
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.simple.SimpleChannel;

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
		CHANNELBLOODVOLUME.messageBuilder(PacketKickBloodlinePlayer.class, networkID++)
				.decoder(PacketKickBloodlinePlayer::decode).encoder(PacketKickBloodlinePlayer::encode)
				.consumerNetworkThread(PacketKickBloodlinePlayer::handle).add();

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
