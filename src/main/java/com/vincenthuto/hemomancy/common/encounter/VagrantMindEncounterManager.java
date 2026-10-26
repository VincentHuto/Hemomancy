package com.vincenthuto.hemomancy.common.encounter;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.NaeglerophaeonEntity;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossScenes;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.worldgen.structure.vagrantmind.VagrantMindPiece;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class VagrantMindEncounterManager {
    private static final ResourceKey<Structure> MIND = ResourceKey.create(Registries.STRUCTURE, Hemomancy.rloc("vagrant_mind"));

    private VagrantMindEncounterManager() {}

    private static boolean participant(ServerPlayer player) {
        return player.isAlive() && !player.isCreative() && !player.isSpectator();
    }

    private static VagrantMindPiece pieceAt(ServerLevel level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) return null;
        var start = level.structureManager().getStructureWithPieceAt(pos, holder -> holder.is(MIND));
        if (!start.isValid()) return null;
        return start.getPieces().stream().filter(VagrantMindPiece.class::isInstance)
                .map(VagrantMindPiece.class::cast).findFirst().orElse(null);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0
                || !player.level().dimension().equals(Level.END) || !participant(player)) return;
        ServerLevel level = player.serverLevel();
        VagrantMindPiece piece = pieceAt(level, player.blockPosition());
        if (piece == null) return;
        maintainSite(level, piece);
        var encounter = VagrantMindEncounterData.get(level).encounter(piece.origin());
        if (encounter != null && !encounter.defeated() && encounter.entity() == null && encounter.knot() != null
                && player.distanceToSqr(encounter.knot().getCenter()) <= 64)
            player.displayClientMessage(Component.translatable("message.hemomancy.dormant_ganglion.warning"), true);
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !level.dimension().equals(Level.END)) return;
        var data = VagrantMindEncounterData.get(level);
        data.encounters().forEach((key, encounter) -> {
            BlockPos origin = BlockPos.of(key);
            if (encounter.entity() != null) updateAttempt(level, origin, encounter);
            if (level.getGameTime() % 20 == 0) {
                restoreTerrain(level, origin, encounter);
                if (!encounter.defeated() && encounter.entity() == null) restoreSite(level, origin, encounter);
            }
        });
    }

    /** Runs before a bound resident's AI, so stale or expired entities cannot surprise a returning player. */
    public static boolean tickResident(NaeglerophaeonEntity boss) {
        if (!(boss.level() instanceof ServerLevel level) || boss.mindOrigin() == null) return true;
        var encounter = VagrantMindEncounterData.get(level).encounter(boss.mindOrigin());
        // Unregistered command/review entities have no structure-owned attempt to maintain.
        if (encounter == null) return true;
        if (!boss.getUUID().equals(encounter.entity()) || encounter.defeated()) {
            boss.discard();
            return false;
        }
        updateAttempt(level, boss.mindOrigin(), encounter);
        return !boss.isRemoved();
    }

    private static void updateAttempt(ServerLevel level, BlockPos origin, VagrantMindEncounterData.Encounter encounter) {
        var data = VagrantMindEncounterData.get(level);
        boolean occupied = level.players().stream().anyMatch(player -> participant(player)
                && VagrantMindPiece.arenaBounds(origin).contains(player.position()));
        if (level.getDifficulty() == Difficulty.PEACEFUL || data.abandoned(origin, occupied, level.getGameTime())) {
            Entity old = level.getEntity(encounter.entity());
            data.abandon(origin);
            if (old instanceof NaeglerophaeonEntity) old.discard();
            restoreSite(level, origin, encounter);
        }
    }

    private static void maintainSite(ServerLevel level, VagrantMindPiece piece) {
        var data = VagrantMindEncounterData.get(level);
        var encounter = data.encounter(piece.origin());
        if (encounter != null && encounter.defeated()) return;
        if (encounter != null && encounter.entity() != null) {
            updateAttempt(level, piece.origin(), encounter);
            // Absence never proves removal: the last sampled chunk may differ from the unloaded entity's chunk.
            // Keep its identity until the arena has actually been empty for the full grace period.
        }
        if (encounter == null || encounter.knot() == null) {
            for (BlockPos node : piece.awakeningNodes()) {
                // Wait for earlier candidates' chunks rather than choosing a different knot on each approach.
                if (!level.hasChunkAt(node)) return;
                var state = level.getBlockState(node);
                if (state.is(BlockInit.synaptic_node.get()) || state.is(BlockInit.dormant_ganglion.get())) {
                    data.site(piece.origin(), node);
                    break;
                }
            }
            encounter = data.encounter(piece.origin());
        }
        if (encounter != null && encounter.entity() == null) restoreSite(level, piece.origin(), encounter);
    }

    private static void restoreSite(ServerLevel level, BlockPos origin, VagrantMindEncounterData.Encounter encounter) {
        restoreTerrain(level, origin, encounter);
        BlockPos knot = encounter.knot();
        if (knot != null && level.hasChunkAt(knot)) {
            var state = level.getBlockState(knot);
            if (state.isAir() || state.is(BlockInit.synaptic_node.get()))
                level.setBlockAndUpdate(knot, BlockInit.dormant_ganglion.get().defaultBlockState());
        }
    }

    private static void restoreTerrain(ServerLevel level, BlockPos origin, VagrantMindEncounterData.Encounter encounter) {
        var data = VagrantMindEncounterData.get(level);
        for (BlockPos node : encounter.pendingNodes()) {
            if (!level.hasChunkAt(node)) continue;
            if (level.getBlockState(node).isAir()) level.setBlockAndUpdate(node, BlockInit.synaptic_node.get().defaultBlockState());
            data.recoveredNode(origin, node);
        }
    }

    /** Called only by the block's completed playerDestroy callback, never by proximity or a pre-break event. */
    public static boolean tryAwaken(ServerPlayer player, BlockPos knot) {
        ServerLevel level = player.serverLevel();
        if (!participant(player) || !level.dimension().equals(Level.END) || player.distanceToSqr(knot.getCenter()) > 64
                || !level.getBlockState(knot).isAir()) return false;
        VagrantMindPiece piece = pieceAt(level, knot);
        if (piece == null || !piece.containsInterior(knot)) return false;
        var data = VagrantMindEncounterData.get(level);
        var encounter = data.encounter(piece.origin());
        // A newly generated knot can be broken before the first maintenance tick.
        if (encounter == null && knot.equals(piece.awakeningPosition())) {
            data.site(piece.origin(), knot);
            encounter = data.encounter(piece.origin());
        }
        if (encounter == null || encounter.defeated() || encounter.entity() != null || !knot.equals(encounter.knot())) return false;
        if (level.getDifficulty() == Difficulty.PEACEFUL) return failed(player, piece, "peaceful");
        NaeglerophaeonEntity boss = EntityInit.naeglerophaeon.get().create(level);
        if (boss == null) return failed(player, piece, "blocked");
        // Restore the conductor before collision checks, so the boss cannot emerge intersecting it.
        level.setBlockAndUpdate(knot, BlockInit.synaptic_node.get().defaultBlockState());
        var nodes = new ArrayList<BlockPos>();
        nodes.add(knot);
        nodes.addAll(piece.awakeningNodes().stream().filter(node -> !node.equals(knot))
                .filter(node -> player.distanceToSqr(node.getCenter()) <= 64 * 64).toList());
        boolean landing = false;
        for (BlockPos node : nodes) {
            if (!level.hasChunkAt(node) || !level.getBlockState(node).is(BlockInit.synaptic_node.get())) continue;
            for (int height = 1; height <= 3; height++) {
                boss.moveTo(node.getX() + .5, node.getY() + height, node.getZ() + .5, 0, 0);
                if (level.noCollision(boss)) { landing = true; break; }
            }
            if (landing) break;
        }
        if (!landing) return failed(player, piece, "blocked");
        boss.bindMind(piece.origin(), piece.seed(), piece.nodePositions());
        boss.setPersistenceRequired();
        boss.beginIntro();
        if (!level.addFreshEntity(boss)) return failed(player, piece, "blocked");
        data.active(piece.origin(), boss.getUUID(), boss.blockPosition());
        BossScenes.playTo(player, boss, BossPresentationIds.SCENE_NAEGLEROPHAEON, 0);
        return true;
    }

    private static boolean failed(ServerPlayer player, VagrantMindPiece piece, String reason) {
        restoreSite(player.serverLevel(), piece.origin(), VagrantMindEncounterData.get(player.serverLevel()).encounter(piece.origin()));
        player.displayClientMessage(Component.translatable("message.hemomancy.dormant_ganglion." + reason), true);
        return false;
    }
}
