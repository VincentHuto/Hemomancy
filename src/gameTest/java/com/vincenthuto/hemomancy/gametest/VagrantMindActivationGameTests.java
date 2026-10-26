package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.encounter.VagrantMindEncounterData;
import com.vincenthuto.hemomancy.common.encounter.VagrantMindEncounterManager;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.NaeglerophaeonEntity;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.network.boss.BossScenePacket;
import com.vincenthuto.hemomancy.common.worldgen.structure.vagrantmind.VagrantMindPiece;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("naeglerophaeon_validation")
@PrefixGameTestTemplate(false)
public final class VagrantMindActivationGameTests {
    /** Supplies real structure references in the End; it does not stand in for natural generation. */
    private static final class Mind implements AutoCloseable {
        final ServerLevel level;
        final VagrantMindPiece piece;
        final BlockPos knot;
        final CapturedPlayer breaker;
        final CapturedPlayer observer;
        final Map<LevelChunk, Map<Structure, StructureStart>> starts = new HashMap<>();
        final Map<LevelChunk, Map<Structure, it.unimi.dsi.fastutil.longs.LongSet>> references = new HashMap<>();
        final Map<BlockPos, net.minecraft.world.level.block.state.BlockState> blocks = new HashMap<>();

        Mind(GameTestHelper h) { this(h, false, false); }

        Mind(GameTestHelper h, boolean legacy, boolean missingPreferred) {
            level = h.getLevel().getServer().getLevel(Level.END);
            int x = 30000 + (UUID.randomUUID().hashCode() & 0x3fff) * 512;
            piece = new VagrantMindPiece(new BlockPos(x, 160, 30000), 37L);
            knot = missingPreferred ? piece.awakeningNodes().get(1) : piece.awakeningPosition();
            h.assertTrue(knot != null, "fixture needs an interior knot");
            Structure structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(Hemomancy.rloc("vagrant_mind"));
            LevelChunk startChunk = chunk(piece.origin());
            startChunk.setStartForStructure(structure, new StructureStart(structure, startChunk.getPos(), 0,
                    new PiecesContainer(List.of(piece))));
            for (BlockPos node : piece.nodePositions()) {
                LevelChunk chunk = chunk(node);
                chunk.addReferenceForStructure(structure, startChunk.getPos().toLong());
                for (BlockPos cell : BlockPos.betweenClosed(node.offset(-1, 0, -1), node.offset(1, 5, 1)))
                    set(cell.immutable(), Blocks.AIR.defaultBlockState());
                set(node, BlockInit.synaptic_node.get().defaultBlockState());
            }
            if (missingPreferred) set(piece.awakeningPosition(), Blocks.AIR.defaultBlockState());
            if (!legacy) set(knot, BlockInit.dormant_ganglion.get().defaultBlockState());
            breaker = CapturedPlayer.create(h);
            observer = CapturedPlayer.create(h);
            for (CapturedPlayer actor : List.of(breaker, observer)) {
                actor.player.setServerLevel(level);
                actor.player.setGameMode(GameType.SURVIVAL);
                actor.player.setPos(Vec3.atBottomCenterOf(knot.above()));
                actor.player.setNoGravity(true);
                level.addNewPlayer(actor.player);
            }
            approach();
        }

        LevelChunk chunk(BlockPos pos) {
            LevelChunk chunk = level.getChunkAt(pos);
            if (!starts.containsKey(chunk)) {
                starts.put(chunk, new HashMap<>(chunk.getAllStarts()));
                var oldReferences = new HashMap<>(chunk.getAllReferences());
                oldReferences.replaceAll((key, value) -> new LongOpenHashSet(value));
                references.put(chunk, oldReferences);
            }
            return chunk;
        }

        void set(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
            blocks.putIfAbsent(pos, level.getBlockState(pos));
            level.setBlockAndUpdate(pos, state);
        }

        void approach() {
            breaker.player.tickCount = 20;
            VagrantMindEncounterManager.onPlayerTick(new PlayerTickEvent.Post(breaker.player));
        }

        VagrantMindEncounterData data() { return VagrantMindEncounterData.get(level); }
        VagrantMindEncounterData.Encounter encounter() { return data().encounter(piece.origin()); }
        NaeglerophaeonEntity boss() {
            return encounter().entity() == null ? null : (NaeglerophaeonEntity) level.getEntity(encounter().entity());
        }
        void awaken(GameTestHelper h) {
            h.assertTrue(breaker.player.gameMode.destroyBlock(knot), "completed player mining must break the knot");
            h.assertTrue(boss() != null, "the completed break must awaken a resident");
        }

        @Override public void close() {
            if (boss() != null) boss().discard();
            breaker.close(); observer.close();
            blocks.forEach(level::setBlockAndUpdate);
            starts.forEach(LevelChunk::setAllStarts);
            references.forEach(LevelChunk::setAllReferences);
            data().abandon(piece.origin());
        }
    }

    @GameTest(template = "empty")
    public static void explorationMiningAndBorerDeathDoNotAwaken(GameTestHelper h) {
        try (var mind = new Mind(h)) {
            h.assertTrue(mind.encounter().entity() == null, "approaching a Mind must not spawn its boss");
            BlockPos cable = mind.knot.offset(2, 0, 0);
            mind.set(cable, BlockInit.nerve_fiber.get().defaultBlockState());
            mind.breaker.player.gameMode.destroyBlock(cable);
            var borer = EntityInit.myelin_borer.get().create(mind.level);
            borer.setPos(mind.breaker.player.position());
            mind.level.addFreshEntity(borer);
            borer.hurt(mind.level.damageSources().playerAttack(mind.breaker.player), 1000);
            borer.discard();
            mind.approach();
            h.assertTrue(mind.encounter().entity() == null && mind.breaker.sent(BossScenePacket.class).isEmpty(),
                    "ordinary mining and killing Borers must not opt into a boss or cinematic");
            h.assertTrue(!mind.breaker.chat.isEmpty(), "the nearby knot must explain the deliberate activation");
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void completedBreakStartsExactlyOneBossAndOnlyBreakersCinematic(GameTestHelper h) {
        try (var mind = new Mind(h)) {
            mind.awaken(h);
            UUID first = mind.boss().getUUID();
            h.assertTrue(mind.breaker.sent(BossScenePacket.class).size() == 1, "breaker receives one cinematic");
            h.assertTrue(mind.observer.sent(BossScenePacket.class).isEmpty(), "a registered nearby observer keeps control");
            h.assertTrue(mind.level.getBlockState(mind.knot).is(BlockInit.synaptic_node.get()), "activation retains the conductor");
            h.assertTrue(!VagrantMindEncounterManager.tryAwaken(mind.observer.player, mind.knot)
                    && first.equals(mind.encounter().entity()), "a second request cannot replace the active resident");
            mind.approach();
            h.assertTrue(mind.breaker.sent(BossScenePacket.class).size() == 1, "proximity never replays the opening");
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void cancelledAndCreativeBreaksDoNotActivate(GameTestHelper h) {
        try (var mind = new Mind(h)) {
            Consumer<BlockEvent.BreakEvent> cancel = event -> {
                if (event.getPos().equals(mind.knot) && event.getLevel() == mind.level) event.setCanceled(true);
            };
            NeoForge.EVENT_BUS.addListener(cancel);
            try {
                h.assertTrue(!mind.breaker.player.gameMode.destroyBlock(mind.knot), "protection cancels the break");
            } finally { NeoForge.EVENT_BUS.unregister(cancel); }
            h.assertTrue(mind.encounter().entity() == null, "a cancelled break cannot awaken");
            mind.breaker.player.setGameMode(GameType.CREATIVE);
            mind.breaker.player.gameMode.destroyBlock(mind.knot);
            h.assertTrue(mind.encounter().entity() == null, "Creative removal cannot awaken");
            mind.breaker.player.setGameMode(GameType.SURVIVAL);
            mind.approach();
            h.assertTrue(mind.level.getBlockState(mind.knot).is(BlockInit.dormant_ganglion.get()), "maintenance repairs Creative removal");
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void peacefulAndBlockedEmergenceRestoreTheKnot(GameTestHelper h) {
        try (var mind = new Mind(h)) {
            Difficulty before = mind.level.getDifficulty();
            try {
                mind.level.getServer().setDifficulty(Difficulty.PEACEFUL, true);
                mind.breaker.player.gameMode.destroyBlock(mind.knot);
                h.assertTrue(mind.boss() == null && mind.level.getBlockState(mind.knot).is(BlockInit.dormant_ganglion.get()),
                        "Peaceful must preserve a retryable site");
            } finally { mind.level.getServer().setDifficulty(before, true); }
            for (BlockPos node : mind.piece.nodePositions())
                for (int height = 1; height <= 4; height++) mind.set(node.above(height), Blocks.BARRIER.defaultBlockState());
            mind.breaker.player.gameMode.destroyBlock(mind.knot);
            h.assertTrue(mind.boss() == null && mind.level.getBlockState(mind.knot).is(BlockInit.dormant_ganglion.get()),
                    "no clear landing must preserve a retryable site");
            h.assertTrue(mind.breaker.sent(BossScenePacket.class).isEmpty(), "failed attempts never send cinematics");
        }
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 660)
    public static void expiredAttemptDiscardsStaleBossAndRestoresOnlyItsDamage(GameTestHelper h) {
        var mind = new Mind(h);
        h.testInfo.addListener(new net.minecraft.gametest.framework.GameTestListener() {
            public void testStructureLoaded(net.minecraft.gametest.framework.GameTestInfo test) { }
            public void testPassed(net.minecraft.gametest.framework.GameTestInfo test, net.minecraft.gametest.framework.GameTestRunner runner) { mind.close(); }
            public void testFailed(net.minecraft.gametest.framework.GameTestInfo test, net.minecraft.gametest.framework.GameTestRunner runner) { mind.close(); }
            public void testAddedForRerun(net.minecraft.gametest.framework.GameTestInfo oldTest, net.minecraft.gametest.framework.GameTestInfo newTest, net.minecraft.gametest.framework.GameTestRunner runner) { }
        });
        mind.awaken(h);
        var boss = mind.boss();
        List<BlockPos> nodes = mind.piece.awakeningNodes().stream().filter(node -> !node.equals(mind.knot)).limit(3).toList();
        BlockPos recover = nodes.get(0), building = nodes.get(1), mined = nodes.get(2);
        for (BlockPos node : List.of(recover, building)) mind.data().destroyedNode(mind.piece.origin(), boss.getUUID(), node);
        mind.set(recover, Blocks.AIR.defaultBlockState());
        mind.set(building, Blocks.DIAMOND_BLOCK.defaultBlockState());
        mind.set(mined, Blocks.AIR.defaultBlockState());
        mind.breaker.player.setPos(mind.breaker.player.position().add(500, 0, 0));
        mind.observer.player.setGameMode(GameType.SPECTATOR);
        mind.data().abandoned(mind.piece.origin(), false, mind.level.getGameTime());
        h.runAfterDelay(599, () -> h.assertTrue(!boss.isRemoved(), "the boss must remain for the full grace period"));
        h.runAfterDelay(601, () -> {
            h.assertTrue(!VagrantMindEncounterManager.tickResident(boss) && boss.isRemoved(), "expired attempt must discard without victory");
            h.assertTrue(!mind.encounter().defeated() && mind.encounter().entity() == null, "abandonment awards no defeat");
            h.assertTrue(mind.level.getBlockState(recover).is(BlockInit.synaptic_node.get()), "boss damage recovers");
            h.assertTrue(mind.level.getBlockState(building).is(Blocks.DIAMOND_BLOCK) && mind.level.getBlockState(mined).isAir(),
                    "construction and player mining survive the reset");
            h.assertTrue(mind.level.getBlockState(mind.knot).is(BlockInit.dormant_ganglion.get()), "the knot returns");
            var stale = EntityInit.naeglerophaeon.get().create(mind.level);
            stale.bindMind(mind.piece.origin(), mind.piece.seed(), mind.piece.nodePositions());
            h.assertTrue(!VagrantMindEncounterManager.tickResident(stale) && stale.isRemoved(), "late unloaded UUIDs cannot resume combat");
            mind.breaker.player.setPos(Vec3.atBottomCenterOf(mind.knot.above()));
            mind.awaken(h);
            h.assertTrue(mind.boss().getHealth() == mind.boss().getMaxHealth(), "a new attempt starts at full health");
            mind.boss().kill();
            mind.approach();
            h.assertTrue(mind.encounter().defeated() && !mind.level.getBlockState(mind.knot).is(BlockInit.dormant_ganglion.get()),
                    "victory permanently clears the Mind");
            h.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void suppliedMindSelectsOneStableInteriorKnot(GameTestHelper h) {
        for (long seed : new long[] {0, 1, 37, 42, -1, Long.MAX_VALUE}) {
            var piece = new VagrantMindPiece(new BlockPos(128, 150, -128), seed);
            BlockPos knot = piece.awakeningPosition();
            h.assertTrue(knot != null && piece.containsInterior(knot), "the knot must be in the hollow interior");
            for (BlockPos node : piece.nodePositions()) {
                if (!piece.containsInterior(node)) continue;
                h.assertTrue(knot.distSqr(piece.origin()) <= node.distSqr(piece.origin()), "choose a central node");
            }
            h.assertTrue(knot.equals(new VagrantMindPiece(piece.origin(), seed).awakeningPosition()), "selection survives piece reconstruction");
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void cancelledEntityInsertionKeepsSiteAndDoesNotPlayCinematic(GameTestHelper h) {
        try (var mind = new Mind(h)) {
            Consumer<net.neoforged.neoforge.event.entity.EntityJoinLevelEvent> cancel = event -> {
                if (event.getLevel() == mind.level && event.getEntity() instanceof NaeglerophaeonEntity boss
                        && mind.piece.origin().equals(boss.mindOrigin())) event.setCanceled(true);
            };
            NeoForge.EVENT_BUS.addListener(cancel);
            try { mind.breaker.player.gameMode.destroyBlock(mind.knot); }
            finally { NeoForge.EVENT_BUS.unregister(cancel); }
            h.assertTrue(mind.encounter().entity() == null && mind.breaker.sent(BossScenePacket.class).isEmpty(),
                    "failed insertion cannot commit an encounter or play its cinematic");
            h.assertTrue(mind.level.getBlockState(mind.knot).is(BlockInit.dormant_ganglion.get()), "failed insertion restores the knot");
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void olderMindsGainOneKnotWithoutReplacingPlayerConstruction(GameTestHelper h) {
        try (var mind = new Mind(h, true, true)) {
            h.assertTrue(mind.encounter().knot().equals(mind.knot)
                    && mind.level.getBlockState(mind.knot).is(BlockInit.dormant_ganglion.get()), "backfill uses the next intact interior node");
            h.assertTrue(mind.level.getBlockState(mind.piece.awakeningPosition()).isAir(), "backfill preserves previous mining");
            mind.set(mind.knot, Blocks.DIAMOND_BLOCK.defaultBlockState());
            mind.approach();
            h.assertTrue(mind.level.getBlockState(mind.knot).is(Blocks.DIAMOND_BLOCK), "site repair never overwrites construction");
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void indirectDestructionCannotAwakenOrFarmTheSite(GameTestHelper h) {
        try (var mind = new Mind(h)) {
            var state = mind.level.getBlockState(mind.knot);
            h.assertTrue(state.getPistonPushReaction() == net.minecraft.world.level.material.PushReaction.BLOCK,
                    "the encounter site cannot be moved by a piston");
            h.assertTrue(BlockInit.dormant_ganglion.get().asItem() == net.minecraft.world.item.Items.AIR
                    && net.minecraft.world.level.block.Block.getDrops(state, mind.level, mind.knot, null).isEmpty(),
                    "the encounter site cannot be collected or farmed");
            mind.level.explode(null, mind.knot.getX() + .5, mind.knot.getY() + .5, mind.knot.getZ() + .5,
                    4, Level.ExplosionInteraction.TNT);
            h.assertTrue(mind.level.getBlockState(mind.knot).is(BlockInit.dormant_ganglion.get()), "TNT must leave the opt-in block intact");
            mind.level.destroyBlock(mind.knot, true);
            h.assertTrue(mind.encounter().entity() == null && mind.breaker.sent(BossScenePacket.class).isEmpty(),
                    "non-player destruction must not awaken or interrupt nearby players");
            mind.approach();
            h.assertTrue(mind.level.getBlockState(mind.knot).is(BlockInit.dormant_ganglion.get()), "indirect removal remains retryable");
        }
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void crossingIntoAnUnloadedSectionDoesNotResetAnOccupiedMind(GameTestHelper h) {
        var mind = new Mind(h);
        h.testInfo.addListener(new net.minecraft.gametest.framework.GameTestListener() {
            public void testStructureLoaded(net.minecraft.gametest.framework.GameTestInfo test) { }
            public void testPassed(net.minecraft.gametest.framework.GameTestInfo test, net.minecraft.gametest.framework.GameTestRunner runner) { mind.close(); }
            public void testFailed(net.minecraft.gametest.framework.GameTestInfo test, net.minecraft.gametest.framework.GameTestRunner runner) { mind.close(); }
            public void testAddedForRerun(net.minecraft.gametest.framework.GameTestInfo oldTest, net.minecraft.gametest.framework.GameTestInfo newTest, net.minecraft.gametest.framework.GameTestRunner runner) { }
        });
        mind.awaken(h);
        var boss = mind.boss();
        UUID id = boss.getUUID();
        BlockPos recorded = mind.encounter().position();
        h.startSequence().thenWaitUntil(() -> h.assertTrue(mind.level.areEntitiesLoaded(new ChunkPos(recorded).toLong()),
                "the previous position's entity section must finish loading"))
                .thenExecute(() -> {
            boss.setPos(boss.position().add(48, 0, 0));
            // This is the actual unload removal path; it bypasses Entity.remove and its position sampling override.
            boss.setRemoved(net.minecraft.world.entity.Entity.RemovalReason.UNLOADED_TO_CHUNK);
            h.assertTrue(mind.level.getEntity(id) == null && mind.encounter().position().equals(recorded),
                    "fixture must have an absent UUID with a stale loaded last-position chunk");
            mind.approach();
            h.assertTrue(id.equals(mind.encounter().entity())
                    && mind.level.getBlockState(mind.knot).is(BlockInit.synaptic_node.get()),
                    "absence cannot abandon an occupied attempt or restore its knot");
                }).thenSucceed();
    }

    @GameTest(template = "empty")
    public static void arenaOccupancyUsesLivingSurvivalPlayersInThisDimension(GameTestHelper h) {
        try (var mind = new Mind(h)) {
            mind.awaken(h);
            var boss = mind.boss();
            mind.breaker.player.setGameMode(GameType.SPECTATOR);
            assertOccupancy(h, mind, boss, false, "another living Survival player keeps the fight awake");
            mind.observer.player.setGameMode(GameType.CREATIVE);
            assertOccupancy(h, mind, boss, true, "Creative observation does not keep the arena occupied");
            mind.observer.player.setGameMode(GameType.SPECTATOR);
            assertOccupancy(h, mind, boss, true, "spectators do not keep the arena occupied");
            mind.observer.player.setGameMode(GameType.SURVIVAL);
            mind.observer.player.setHealth(0);
            assertOccupancy(h, mind, boss, true, "a dead player does not keep the arena occupied");
            mind.observer.player.setHealth(20);
            mind.observer.player.teleportTo(h.getLevel(), mind.knot.getX(), mind.knot.getY(), mind.knot.getZ(), 0, 0);
            assertOccupancy(h, mind, boss, true, "a player in another dimension does not occupy the Mind");
            mind.level.removePlayerImmediately(mind.breaker.player, net.minecraft.world.entity.Entity.RemovalReason.UNLOADED_WITH_PLAYER);
            assertOccupancy(h, mind, boss, true, "logged-out players do not occupy the Mind");
        }
        h.succeed();
    }

    private static void assertOccupancy(GameTestHelper h, Mind mind, NaeglerophaeonEntity boss, boolean empty, String message) {
        long now = mind.level.getGameTime();
        VagrantMindEncounterManager.tickResident(boss);
        h.assertTrue(mind.data().abandoned(mind.piece.origin(), false, now + 600) == empty, message);
        // Reset the simulated clock before the next status case; the production query above uses real occupancy.
        mind.data().abandoned(mind.piece.origin(), true, now);
    }

    @GameTest(template = "empty")
    public static void savedAttemptsKeepExpiryRecoveryAndPermanentDefeat(GameTestHelper h) {
        BlockPos origin = new BlockPos(100, 150, 100), knot = origin.offset(2, 0, 0), node = origin.offset(3, 0, 0);
        UUID id = UUID.randomUUID();
        var data = new VagrantMindEncounterData();
        data.site(origin, knot); data.active(origin, id, knot.above()); data.destroyedNode(origin, id, node);
        h.assertTrue(!data.abandoned(origin, false, 100) && !data.abandoned(origin, false, 699), "wait the full 600 ticks");
        var restored = VagrantMindEncounterData.load(data.save(new CompoundTag(), h.getLevel().registryAccess()), h.getLevel().registryAccess());
        h.assertTrue(restored.abandoned(origin, true, 700), "reload and a returning occupant cannot erase elapsed abandonment");
        restored.abandon(origin);
        restored = VagrantMindEncounterData.load(restored.save(new CompoundTag(), h.getLevel().registryAccess()), h.getLevel().registryAccess());
        h.assertTrue(restored.encounter(origin).knot().equals(knot) && restored.encounter(origin).pendingNodes().contains(node),
                "dormant site and unloaded restoration survive reload");
        restored.active(origin, id, knot.above()); restored.defeated(origin, id);
        restored.active(origin, UUID.randomUUID(), knot.above());
        h.assertTrue(restored.encounter(origin).defeated() && restored.encounter(origin).entity() == null, "defeat cannot be reopened");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void anotherOccupantRestartsGraceAndLegacySavesRetainIdentity(GameTestHelper h) {
        var data = new VagrantMindEncounterData();
        BlockPos origin = new BlockPos(80, 150, 80);
        UUID id = UUID.randomUUID();
        CompoundTag old = new CompoundTag(), entry = new CompoundTag();
        entry.putLong("Origin", origin.asLong()); entry.putLong("Position", origin.above().asLong());
        entry.putUUID("Entity", id); entry.putBoolean("Defeated", false);
        ListTag entries = new ListTag(); entries.add(entry); old.put("Encounters", entries);
        data = VagrantMindEncounterData.load(old, h.getLevel().registryAccess());
        h.assertTrue(id.equals(data.encounter(origin).entity()) && data.encounter(origin).knot() == null, "legacy active identity survives");
        data.abandoned(origin, false, 100); data.abandoned(origin, true, 699);
        h.assertTrue(!data.abandoned(origin, false, 700) && !data.abandoned(origin, false, 1299)
                && data.abandoned(origin, false, 1300), "another occupant resets the consecutive-empty timer");
        h.succeed();
    }
}
