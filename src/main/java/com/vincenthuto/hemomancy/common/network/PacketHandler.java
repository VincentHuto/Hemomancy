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

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
        net.playToServer(ClearLoomStatePacket.TYPE, ClearLoomStatePacket.STREAM_CODEC, ClearLoomStatePacket::handle);
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

    public static void sendLivingToolBreakParticles(Vec3 pos, ParticleColor color, double radius,
            ServerLevel level) {
        PacketDistributor.sendToPlayersNear(level, null, pos.x, pos.y, pos.z, radius,
                new SpawnLivingToolParticlesPacket(pos, color));
    }

    public static void sendMonolithShatterBurst(Vec3 pos, double radius, ServerLevel level) {
        PacketDistributor.sendToPlayersNear(level, null, pos.x, pos.y, pos.z, radius,
                new SpawnMonolithShatterBurstPacket(pos));
    }

    public static void sendClientElytraPacket() {
        PacketDistributor.sendToServer(new CPacketFlight());
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
