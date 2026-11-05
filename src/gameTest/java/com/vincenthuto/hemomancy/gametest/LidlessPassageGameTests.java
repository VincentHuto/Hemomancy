package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.block.shared.LidlessDoorBlock;
import com.vincenthuto.hemomancy.common.block.shared.LidlessPassageService;
import com.vincenthuto.hemomancy.common.block.shared.LidlessPortal;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.AxonalTransductionManager;
import com.vincenthuto.hemomancy.common.network.lidless.LidlessPassageStatePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The Lidless Door: a 1x2 door that holds a player in its doorway for a void crossing and lets nothing else through. */
@GameTestHolder("lidless_passage_validation")
@PrefixGameTestTemplate(false)
public final class LidlessPassageGameTests {
    static final String ARENA = "ductilis_arena";
    static final String NAMESPACE = "lidless_passage_validation";
    static final BlockPos DOOR = new BlockPos(2, 3, 2);
    /** Vanilla walking: movement speed 0.1 scaled to blocks a tick. */
    static final double WALK = 0.1 * 2.16;

    /** Both halves facing north at {@link #DOOR}, the cells around it cleared; returns the lower position. */
    static BlockPos place(GameTestHelper h) {
        BlockPos lower = h.absolutePos(DOOR);
        for (BlockPos p : BlockPos.betweenClosed(lower.offset(-2, -1, -2), lower.offset(2, 3, 2)))
            h.getLevel().setBlock(p, p.getY() < lower.getY() ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);
        BlockState state = BlockInit.lidless_door.get().defaultBlockState().setValue(LidlessDoorBlock.FACING, Direction.NORTH);
        h.getLevel().setBlock(lower, state, 3);
        LidlessDoorBlock.placeUpper(h.getLevel(), lower, state);
        return lower;
    }

    /** A player south of a north-facing door, so entering heads toward the front and the far cell is north. */
    static final class Fixture implements AutoCloseable {
        final GameTestHelper h;
        final BlockPos lower;
        final ServerPlayer player;
        LidlessPassageStatePacket lastState;
        long token;
        Fixture(GameTestHelper h) {
            this(h, place(h), 1.9);
        }
        Fixture(GameTestHelper h, BlockPos lower, double zOffset) {
            this.h = h;
            this.lower = lower;
            player = DuctilisGameTests.player(h, packet -> {
                if (packet instanceof ClientboundCustomPayloadPacket custom
                        && custom.payload() instanceof LidlessPassageStatePacket state && state.playerId() == player().getId()) {
                    lastState = state;
                    if (state.active()) token = state.token();
                }
            });
            player.setPos(Vec3.atBottomCenterOf(lower).add(0, 0, zOffset));
            player.setYRot(zOffset > 0 ? 180 : 0);
        }
        ServerPlayer player() { return player; }
        void enter() {
            double z = player.getZ() > lower.getZ() + 0.5 ? 0.6 : -0.6;
            player.setPos(Vec3.atBottomCenterOf(lower).add(0, 0, z));
            LidlessPassageService.offer(player, LidlessDoorBlock.portalAt(h.getLevel(), lower));
            LidlessPassageService.tick(player);
        }
        void step(int direction, boolean sprinting, boolean sneaking) {
            LidlessPassageService.input(player, token, direction, sprinting, sneaking);
            LidlessPassageService.tick(player);
        }
        void forward(int ticks) { for (int i = 0; i < ticks; i++) step(1, false, false); }
        void back(int ticks) { for (int i = 0; i < ticks; i++) step(-1, false, false); }
        boolean crossing() { return LidlessPassageService.isCrossing(player); }
        double progress() { return LidlessPassageService.progress(player); }
        BlockPos farCell() { return DOOR.offset(0, 0, -1); }
        BlockPos entryCell() { return DOOR.offset(0, 0, 1); }
        void assertStandingAt(BlockPos relative) {
            h.assertTrue(player.blockPosition().equals(h.absolutePos(relative)),
                    "player stands at " + relative + " but is at " + player.blockPosition() + " relative " + h.relativePos(player.blockPosition()));
        }
        public void close() { LidlessPassageService.end(player, false); player.discard(); }
    }

    /** A portal with no block behind it: the fixture's cleared door cell, any mode, a completion counter. */
    static final class StubPortal implements LidlessPortal {
        final BlockPos anchor;
        final LidlessPassageService.Mode mode;
        int completed;
        StubPortal(BlockPos anchor, LidlessPassageService.Mode mode) { this.anchor = anchor; this.mode = mode; }
        @Override public BlockPos anchor() { return anchor; }
        @Override public Direction facing() { return Direction.NORTH; }
        @Override public boolean admits(ServerPlayer player) { return LidlessPassageService.mayCross(player); }
        @Override public boolean towardFront(ServerPlayer player) { return player.getZ() > anchor.getZ() + 0.5; }
        @Override public LidlessPassageService.Mode mode() { return mode; }
        @Override public boolean stillValid(net.minecraft.server.level.ServerLevel level) { return true; }
        @Override public void completed(ServerPlayer player, boolean towardFront) { completed++; }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "lidless")
    public static void seed_mode_retreat_slows_toward_the_end(GameTestHelper h) {
        BlockPos lower = place(h);
        h.getLevel().destroyBlock(lower, false);
        try (Fixture f = new Fixture(h, lower, 1.9)) {
            StubPortal stub = new StubPortal(lower, LidlessPassageService.Mode.SEED);
            f.player.setPos(Vec3.atBottomCenterOf(lower).add(0, 0, 0.6));
            LidlessPassageService.offer(f.player, stub);
            LidlessPassageService.tick(f.player);
            f.forward(60);
            double atEnd = f.progress();
            f.player.setYRot(180);
            f.back(1);
            double step = atEnd - f.progress();
            double expected = WALK * (1.0 - 0.75 * atEnd / 16.0);
            h.assertTrue(Math.abs(step - expected) < 0.01 && step < WALK * 0.5,
                    "retreat slows with progress: " + step + " expected " + expected);
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "lidless")
    public static void seed_mode_completion_calls_the_portal_and_never_holds(GameTestHelper h) {
        BlockPos lower = place(h);
        h.getLevel().destroyBlock(lower, false);
        h.setBlock(DOOR.offset(0, 1, -1), Blocks.STONE);
        try (Fixture f = new Fixture(h, lower, 1.9)) {
            StubPortal stub = new StubPortal(lower, LidlessPassageService.Mode.SEED);
            f.player.setPos(Vec3.atBottomCenterOf(lower).add(0, 0, 0.6));
            LidlessPassageService.offer(f.player, stub);
            LidlessPassageService.tick(f.player);
            f.forward(80);
            h.assertFalse(f.crossing(), "a seed crossing never holds at the end");
            h.assertTrue(stub.completed == 1, "the portal was told once: " + stub.completed);
            h.assertTrue(!f.player.blockPosition().equals(lower) && !f.player.blockPosition().equals(lower.above()),
                    "not left in the portal cell");
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "lidless")
    public static void state_packet_carries_the_mode(GameTestHelper h) {
        BlockPos lower = place(h);
        h.getLevel().destroyBlock(lower, false);
        try (Fixture f = new Fixture(h, lower, 1.9)) {
            StubPortal stub = new StubPortal(lower, LidlessPassageService.Mode.SEED);
            f.player.setPos(Vec3.atBottomCenterOf(lower).add(0, 0, 0.6));
            LidlessPassageService.offer(f.player, stub);
            LidlessPassageService.tick(f.player);
            h.assertTrue(f.lastState != null && f.lastState.mode() == LidlessPassageService.Mode.SEED, "mode on the wire: " + f.lastState);
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "lidless")
    public static void door_places_as_a_pair_and_breaks_as_a_pair(GameTestHelper h) {
        BlockPos lower = place(h);
        h.assertBlockProperty(DOOR.above(), LidlessDoorBlock.HALF, DoubleBlockHalf.UPPER);
        h.assertBlockProperty(DOOR.above(), LidlessDoorBlock.FACING, Direction.NORTH);
        h.getLevel().destroyBlock(lower, false);
        h.assertBlockPresent(Blocks.AIR, DOOR.above());
        h.succeed();
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "lidless")
    public static void panel_is_solid_to_a_zombie(GameTestHelper h) {
        BlockPos lower = place(h);
        Zombie zombie = h.spawn(EntityType.ZOMBIE, DOOR.offset(0, 0, 2));
        VoxelShape shape = h.getLevel().getBlockState(lower).getCollisionShape(h.getLevel(), lower, CollisionContext.of(zombie));
        h.assertTrue(!shape.isEmpty(), "the panel blocks mobs");
        h.succeed();
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "lidless")
    public static void player_collision_is_empty_while_eligible(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            VoxelShape shape = h.getLevel().getBlockState(f.lower).getCollisionShape(h.getLevel(), f.lower, CollisionContext.of(f.player));
            h.assertTrue(shape.isEmpty(), "a player who may cross walks into the panel");
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "lidless")
    public static void entering_pins_and_syncs(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            f.enter();
            h.assertTrue(f.crossing(), "crossing after entering");
            h.assertTrue(f.player.position().distanceTo(Vec3.atBottomCenterOf(f.lower)) < 1e-6, "pinned at the door centre");
            h.assertTrue(f.player.noPhysics && f.player.isNoGravity(), "held without physics or gravity");
            h.assertTrue(f.lastState != null && f.lastState.active() && f.lastState.towardFront()
                    && f.lastState.length() == 16.0F && f.lastState.door().equals(f.lower), "state synced: " + f.lastState);
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "lidless")
    public static void forward_input_advances_by_walking_speed(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            f.enter();
            f.forward(1);
            h.assertTrue(Math.abs(f.progress() - WALK) < 0.01, "one walking tick: " + f.progress());
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "lidless")
    public static void sprint_and_sneak_scale_progress(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            f.enter();
            f.step(1, true, false);
            double sprint = f.progress();
            f.step(1, false, true);
            double sneak = f.progress() - sprint;
            h.assertTrue(Math.abs(sprint - WALK * 1.3) < 0.01, "sprint tick: " + sprint);
            h.assertTrue(Math.abs(sneak - WALK * 0.3) < 0.01, "sneak tick: " + sneak);
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "lidless")
    public static void completion_lands_in_far_cell(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            f.enter();
            f.player.setYRot(217);
            f.forward(80);
            h.assertFalse(f.crossing(), "crossing over");
            f.assertStandingAt(f.farCell());
            h.assertTrue(!f.player.isNoGravity() && !f.player.noPhysics, "gravity and physics back");
            h.assertTrue(f.player.getYRot() == 217, "look kept");
            h.assertTrue(f.lastState != null && !f.lastState.active(), "inactive state synced");
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "lidless")
    public static void walking_back_lands_on_entry_side(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            f.enter();
            f.forward(3);
            f.back(20);
            h.assertFalse(f.crossing(), "crossing over");
            f.assertStandingAt(f.entryCell());
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "lidless")
    public static void forward_follows_the_look(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            f.enter();
            f.forward(3);
            double ahead = f.progress();
            f.player.setYRot(0);
            f.forward(1);
            h.assertTrue(f.progress() < ahead, "facing the entry, forward walks back: " + f.progress());
            f.forward(20);
            h.assertFalse(f.crossing(), "walked out the entry side");
            f.assertStandingAt(f.entryCell());
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "lidless")
    public static void both_cells_blocked_never_lands_in_the_door(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            f.enter();
            f.forward(10);
            h.setBlock(DOOR.offset(0, 1, -1), Blocks.STONE);
            h.setBlock(DOOR.offset(0, 1, 1), Blocks.STONE);
            h.getLevel().destroyBlock(f.lower, false);
            LidlessPassageService.tick(f.player);
            h.assertFalse(f.crossing(), "crossing over");
            h.assertTrue(!f.player.blockPosition().equals(f.lower) && !f.player.blockPosition().equals(f.lower.above()),
                    "not left in the door cell: " + h.relativePos(f.player.blockPosition()));
            h.assertTrue(h.getLevel().noCollision(f.player, f.player.getBoundingBox()), "standing somewhere clear");
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "lidless")
    public static void spectator_mid_crossing_is_released(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            f.enter();
            f.forward(5);
            f.player.setGameMode(GameType.SPECTATOR);
            LidlessPassageService.tick(f.player);
            h.assertFalse(f.crossing(), "a spectator is let go");
            h.assertTrue(!f.player.isNoGravity(), "gravity flag restored");
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "lidless")
    public static void blocked_far_cell_holds(GameTestHelper h) {
        BlockPos lower = place(h);
        h.setBlock(DOOR.offset(0, 1, -1), Blocks.STONE);
        try (Fixture f = new Fixture(h, lower, 1.9)) {
            f.enter();
            f.forward(80);
            h.assertTrue(f.crossing(), "still crossing");
            h.assertTrue(f.progress() == 16.0, "held at the end: " + f.progress());
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "lidless")
    public static void broken_door_returns_to_entry_side(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            f.enter();
            f.forward(10);
            h.getLevel().destroyBlock(f.lower, false);
            LidlessPassageService.tick(f.player);
            h.assertFalse(f.crossing(), "crossing over");
            f.assertStandingAt(f.entryCell());
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "lidless")
    public static void death_ends_in_place(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            f.enter();
            f.forward(10);
            f.player.kill();
            LidlessPassageService.tick(f.player);
            h.assertFalse(f.crossing(), "crossing over");
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "lidless")
    public static void spectator_does_not_enter(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            f.player.setGameMode(GameType.SPECTATOR);
            f.enter();
            h.assertFalse(f.crossing(), "a spectator passes through");
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "lidless")
    public static void logout_returns_player(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            f.enter();
            f.forward(10);
            LidlessPassageService.logout(f.player);
            h.assertFalse(f.crossing(), "crossing over");
            f.assertStandingAt(f.entryCell());
            h.assertTrue(!f.player.isNoGravity() && !f.player.noPhysics, "gravity and physics back");
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "lidless")
    public static void two_players_cross_independently(GameTestHelper h) {
        BlockPos lower = place(h);
        try (Fixture south = new Fixture(h, lower, 1.9); Fixture north = new Fixture(h, lower, -1.9)) {
            south.enter();
            north.enter();
            south.forward(10);
            north.forward(10);
            h.assertTrue(south.lastState.towardFront() && !north.lastState.towardFront(), "opposite directions");
            south.forward(80);
            south.assertStandingAt(south.farCell());
            h.assertTrue(north.crossing(), "the other player is still crossing");
            h.assertTrue(Math.abs(north.progress() - WALK * 10) < 0.05, "the other player kept their progress: " + north.progress());
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "lidless")
    public static void flying_player_restores_gravity(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            f.player.setNoGravity(true);
            f.enter();
            f.forward(80);
            h.assertFalse(f.crossing(), "crossing over");
            h.assertTrue(f.player.isNoGravity(), "the no-gravity flag the player entered with is restored");
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "lidless")
    public static void axonal_and_door_exclude_each_other(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            h.setBlock(DOOR.offset(0, -1, 0), BlockInit.synaptic_node.get());
            h.setBlock(DOOR.offset(1, -1, 0), BlockInit.synaptic_node.get());
            // An Axonal-ready player: the node under the door would otherwise pull them into Axonal travel.
            HemoCapabilityAccess.getInitiatoryDegree(f.player).setDegreeNumber(6);
            HemoCapabilityAccess.getBloodVolume(f.player).setActive(true);
            HemoCapabilityAccess.getBloodVolume(f.player).setBloodVolume(10000);
            HemoCapabilityAccess.getBloodTendency(f.player).setTendencyAlignment(EnumBloodTendency.DUCTILIS, 100);
            var known = HemoCapabilityAccess.getKnownManipulations(f.player);
            known.getKnownManips().put(ManipulationInit.axonal_transduction.get(), new ManipLevel(0, 0));
            known.setEquippedManipNames(java.util.List.of("axonal_transduction"));
            if (!known.isPassiveActive("axonal_transduction")) known.togglePassive("axonal_transduction");
            f.enter();
            AxonalTransductionManager.tick(f.player);
            h.assertTrue(f.crossing() && !AxonalTransductionManager.isTraveling(f.player), "the door keeps its player");
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "lidless")
    public static void server_stop_clears_sessions(GameTestHelper h) {
        try (Fixture f = new Fixture(h)) {
            f.enter();
            LidlessPassageService.clearSessionState();
            h.assertFalse(f.crossing(), "no session survives a server stop");
            h.succeed();
        }
    }
}
