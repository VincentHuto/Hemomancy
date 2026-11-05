package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.block.harbinger.functional.SanguineMonolithBlock;
import com.vincenthuto.hemomancy.common.block.shared.LidlessPassageService;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.item.EntityQliphothSeedItem;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.lidless.LidlessPassageStatePacket;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineMonolithBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The Monolith walk: at Degree 7 the Sanguine Monolith cracks and falls open instead of exploding, the Archon who
 * broke it walks a seed crossing between the halves, and the Qliphoth Seed is taken at the far end.
 */
@GameTestHolder("monolith_walk_validation")
@PrefixGameTestTemplate(false)
public final class MonolithWalkGameTests {
    static final String ARENA = "ductilis_arena";
    static final String NAMESPACE = "monolith_walk_validation";
    static final BlockPos STONE = new BlockPos(2, 3, 2);
    static final double WALK = 0.1 * 2.16;

    /** A Monolith facing south at {@link #STONE} (base and filler), the cells around it cleared; returns the base. */
    static BlockPos place(GameTestHelper h) {
        BlockPos base = h.absolutePos(STONE);
        for (BlockPos p : BlockPos.betweenClosed(base.offset(-3, -1, -3), base.offset(3, 4, 3)))
            h.getLevel().setBlock(p, p.getY() < base.getY() ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);
        BlockState state = BlockInit.sanguine_monolith.get().defaultBlockState().setValue(SanguineMonolithBlock.FACING, Direction.SOUTH);
        h.getLevel().setBlock(base, state, 3);
        ((SanguineMonolithBlock) state.getBlock()).placeFillers(h.getLevel(), base, state);
        return base;
    }

    static SanguineMonolithBlockEntity entity(GameTestHelper h, BlockPos base) {
        return (SanguineMonolithBlockEntity) h.getLevel().getBlockEntity(base);
    }

    /** Breaks the stone for {@code owner} and runs the fall to its end. */
    static void breakOpen(GameTestHelper h, BlockPos base, ServerPlayer owner) {
        SanguineMonolithBlockEntity stone = entity(h, base);
        stone.beginBreak(owner);
        for (int i = 0; i <= SanguineMonolithBlock.BREAK_TICKS; i++) stone.tick();
    }

    static int itemsNear(GameTestHelper h, BlockPos base, net.minecraft.world.item.Item item) {
        return h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(base).inflate(3))
                .stream().filter(e -> e.getItem().is(item)).mapToInt(e -> e.getItem().getCount()).sum();
    }

    /** An Archon at Degree 7 south of the stone, the far cell north of it. */
    static final class Fixture implements AutoCloseable {
        final GameTestHelper h;
        final BlockPos base;
        final ServerPlayer player;
        LidlessPassageStatePacket lastState;
        long token;
        int dialogues;
        Fixture(GameTestHelper h, BlockPos base, double zOffset) {
            this.h = h;
            this.base = base;
            player = DuctilisGameTests.player(h, packet -> {
                if (packet instanceof ClientboundCustomPayloadPacket custom
                        && custom.payload() instanceof LidlessPassageStatePacket state && state.playerId() == player().getId()) {
                    lastState = state;
                    if (state.active()) token = state.token();
                }
                if (packet instanceof ClientboundCustomPayloadPacket custom
                        && custom.payload() instanceof com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket) dialogues++;
            });
            HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(7);
            HemoCapabilityAccess.getBloodVolume(player).setActive(true);
            player.setPos(Vec3.atBottomCenterOf(base).add(0, 0, zOffset));
            player.setYRot(zOffset > 0 ? 180 : 0);
        }
        ServerPlayer player() { return player; }
        void enter() {
            player.setPos(Vec3.atBottomCenterOf(base).add(0, 0, player.getZ() > base.getZ() + 0.5 ? 0.6 : -0.6));
            LidlessPassageService.offer(player, entity(h, base).portal());
            LidlessPassageService.tick(player);
        }
        void step(int direction) {
            LidlessPassageService.input(player, token, direction, false, false);
            LidlessPassageService.tick(player);
        }
        void forward(int ticks) { for (int i = 0; i < ticks; i++) step(1); }
        boolean crossing() { return LidlessPassageService.isCrossing(player); }
        BlockPos farCell() { return STONE.offset(0, 0, -1); }
        BlockPos entryCell() { return STONE.offset(0, 0, 1); }
        boolean standingAt(BlockPos relative) { return player.blockPosition().equals(h.absolutePos(relative)); }
        boolean holdsSeed() { return player.getInventory().countItem(ItemInit.qliphoth_seed.get()) > 0; }
        public void close() { LidlessPassageService.end(player, false); player.discard(); }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "monolith")
    public static void second_interaction_breaks_instead_of_dropping(GameTestHelper h) {
        BlockPos base = place(h);
        try (Fixture f = new Fixture(h, base, 1.9)) {
            SanguineMonolithBlockEntity stone = entity(h, base);
            stone.beginBreak(f.player);
            h.assertTrue(stone.isBreaking() && !stone.isBroken(), "breaking, not yet broken");
            h.assertTrue(itemsNear(h, base, ItemInit.qliphoth_seed.get()) == 0, "no seed drops at the press");
            for (int i = 0; i <= SanguineMonolithBlock.BREAK_TICKS; i++) stone.tick();
            h.assertTrue(h.getLevel().getBlockState(base).getValue(SanguineMonolithBlock.BROKEN), "the stone is broken");
            h.assertTrue(itemsNear(h, base, ItemInit.monolith_fragment.get()) >= 5, "fragments lie at the base");
            h.assertTrue(itemsNear(h, base, ItemInit.qliphoth_seed.get()) == 0, "the seed stays inside");
            h.assertTrue(entity(h, base) != null && entity(h, base).seedInside(), "the block entity survives with the seed");
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "monolith")
    public static void stranger_is_solid_in_the_gap(GameTestHelper h) {
        BlockPos base = place(h);
        try (Fixture owner = new Fixture(h, base, 1.9); Fixture stranger = new Fixture(h, base, 2.5)) {
            breakOpen(h, base, owner.player);
            BlockState state = h.getLevel().getBlockState(base);
            h.assertTrue(!state.getCollisionShape(h.getLevel(), base, CollisionContext.of(stranger.player)).isEmpty(), "solid to a stranger");
            h.assertTrue(state.getCollisionShape(h.getLevel(), base, CollisionContext.of(owner.player)).isEmpty(), "open to the owner");
            h.assertTrue(!h.getLevel().getBlockState(base.above()).getCollisionShape(h.getLevel(), base.above(), CollisionContext.of(stranger.player)).isEmpty(), "the filler is solid to a stranger");
            stranger.enter();
            h.assertFalse(stranger.crossing(), "a stranger is never pulled in");
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "monolith")
    public static void owner_enters_in_seed_mode(GameTestHelper h) {
        BlockPos base = place(h);
        try (Fixture f = new Fixture(h, base, 1.9)) {
            breakOpen(h, base, f.player);
            f.enter();
            h.assertTrue(f.crossing(), "the owner crosses");
            h.assertTrue(f.lastState != null && f.lastState.mode() == LidlessPassageService.Mode.SEED, "seed mode: " + f.lastState);
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "monolith")
    public static void completion_takes_the_seed_and_closes(GameTestHelper h) {
        BlockPos base = place(h);
        try (Fixture f = new Fixture(h, base, 1.9)) {
            breakOpen(h, base, f.player);
            f.enter();
            f.forward(80);
            h.assertFalse(f.crossing(), "crossing over");
            h.assertTrue(f.holdsSeed(), "the seed is in hand");
            h.assertTrue(!entity(h, base).seedInside(), "the seed is taken");
            h.assertTrue(!h.getLevel().getBlockState(base).getCollisionShape(h.getLevel(), base, CollisionContext.of(f.player)).isEmpty(), "the gap closed");
            h.assertTrue(f.standingAt(f.farCell()), "out the far side");
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "monolith")
    public static void full_inventory_drops_the_seed_at_the_exit(GameTestHelper h) {
        BlockPos base = place(h);
        try (Fixture f = new Fixture(h, base, 1.9)) {
            breakOpen(h, base, f.player);
            for (int slot = 0; slot < f.player.getInventory().getContainerSize(); slot++)
                f.player.getInventory().setItem(slot, new ItemStack(Blocks.COBBLESTONE, 64));
            f.enter();
            f.forward(80);
            h.assertFalse(f.holdsSeed(), "nothing fit");
            boolean dropped = !h.getLevel().getEntitiesOfClass(EntityQliphothSeedItem.class, f.player.getBoundingBox().inflate(2)).isEmpty();
            h.assertTrue(dropped, "the seed floats at the player's feet");
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "monolith")
    public static void blocked_far_cell_still_takes_the_seed(GameTestHelper h) {
        BlockPos base = place(h);
        h.setBlock(STONE.offset(0, 0, -1), Blocks.STONE);
        h.setBlock(STONE.offset(0, 1, -1), Blocks.STONE);
        try (Fixture f = new Fixture(h, base, 1.9)) {
            breakOpen(h, base, f.player);
            f.enter();
            f.forward(80);
            h.assertFalse(f.crossing(), "a seed crossing never holds");
            h.assertTrue(f.holdsSeed(), "the seed is in hand");
            h.assertTrue(h.getLevel().noCollision(f.player, f.player.getBoundingBox()), "standing clear");
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "monolith")
    public static void husk_broken_mid_crossing_drops_the_seed(GameTestHelper h) {
        BlockPos base = place(h);
        try (Fixture f = new Fixture(h, base, 1.9)) {
            breakOpen(h, base, f.player);
            f.enter();
            f.forward(10);
            h.getLevel().getBlockState(base).getBlock().playerWillDestroy(h.getLevel(), base, h.getLevel().getBlockState(base), f.player);
            h.getLevel().destroyBlock(base, false);
            LidlessPassageService.tick(f.player);
            h.assertFalse(f.crossing(), "crossing over");
            h.assertTrue(itemsNear(h, base, ItemInit.qliphoth_seed.get()) == 1, "the seed lies at the stone");
            h.assertTrue(f.standingAt(f.entryCell()), "back on the entry side");
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "monolith")
    public static void relog_mid_crossing_keeps_the_seed_inside(GameTestHelper h) {
        BlockPos base = place(h);
        try (Fixture f = new Fixture(h, base, 1.9)) {
            breakOpen(h, base, f.player);
            f.enter();
            f.forward(10);
            LidlessPassageService.logout(f.player);
            h.assertFalse(f.crossing(), "crossing over");
            h.assertTrue(f.standingAt(f.entryCell()), "back on the entry side");
            h.assertTrue(entity(h, base).seedInside(), "the seed waits");
            h.assertTrue(h.getLevel().getBlockState(base).getCollisionShape(h.getLevel(), base, CollisionContext.of(f.player)).isEmpty(), "the portal is still open");
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "monolith")
    public static void broken_monolith_survives_reload(GameTestHelper h) {
        BlockPos base = place(h);
        try (Fixture f = new Fixture(h, base, 1.9)) {
            breakOpen(h, base, f.player);
            SanguineMonolithBlockEntity stone = entity(h, base);
            CompoundTag tag = stone.saveWithoutMetadata(h.getLevel().registryAccess());
            SanguineMonolithBlockEntity loaded = new SanguineMonolithBlockEntity(base, h.getLevel().getBlockState(base));
            loaded.loadWithComponents(tag, h.getLevel().registryAccess());
            h.assertTrue(loaded.isBroken() && loaded.seedInside() && loaded.admits(f.player), "owner and seed survive the save");
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "monolith")
    public static void husk_after_seed_drops_nothing(GameTestHelper h) {
        BlockPos base = place(h);
        try (Fixture f = new Fixture(h, base, 1.9)) {
            breakOpen(h, base, f.player);
            f.enter();
            f.forward(80);
            h.getLevel().getBlockState(base).getBlock().playerWillDestroy(h.getLevel(), base, h.getLevel().getBlockState(base), f.player);
            h.getLevel().destroyBlock(base, true);
            h.assertTrue(itemsNear(h, base, BlockInit.sanguine_monolith.get().asItem()) == 0, "no monolith comes back");
            h.assertTrue(itemsNear(h, base, ItemInit.qliphoth_seed.get()) == 0, "no second seed");
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "monolith")
    public static void filler_destroyed_drops_the_seed(GameTestHelper h) {
        BlockPos base = place(h);
        try (Fixture f = new Fixture(h, base, 1.9)) {
            breakOpen(h, base, f.player);
            h.getLevel().destroyBlock(base.above(), true);
            h.assertTrue(itemsNear(h, base, ItemInit.qliphoth_seed.get()) == 1, "the seed lies where the husk stood");
            h.assertTrue(itemsNear(h, base, BlockInit.sanguine_monolith.get().asItem()) == 0, "no monolith comes back");
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "monolith")
    public static void creative_full_inventory_drops_the_seed_at_the_exit(GameTestHelper h) {
        BlockPos base = place(h);
        try (Fixture f = new Fixture(h, base, 1.9)) {
            breakOpen(h, base, f.player);
            f.player.setGameMode(GameType.CREATIVE);
            for (int slot = 0; slot < f.player.getInventory().getContainerSize(); slot++)
                f.player.getInventory().setItem(slot, new ItemStack(Blocks.COBBLESTONE, 64));
            f.enter();
            f.forward(80);
            boolean dropped = !h.getLevel().getEntitiesOfClass(EntityQliphothSeedItem.class, f.player.getBoundingBox().inflate(2)).isEmpty();
            h.assertTrue(dropped, "creative discard is not delivery: the seed floats at the feet");
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "monolith")
    public static void husk_answers_no_conversation(GameTestHelper h) {
        BlockPos base = place(h);
        try (Fixture f = new Fixture(h, base, 1.9)) {
            breakOpen(h, base, f.player);
            BlockState state = h.getLevel().getBlockState(base);
            var hit = new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(base), Direction.SOUTH, base, false);
            state.useWithoutItem(h.getLevel(), f.player, hit);
            h.assertTrue(f.dialogues == 0, "a broken stone opens no dialogue: " + f.dialogues);
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "monolith")
    public static void press_further_refuses_a_breaking_stone(GameTestHelper h) {
        BlockPos base = place(h);
        try (Fixture f = new Fixture(h, base, 1.9)) {
            SanguineMonolithBlockEntity stone = entity(h, base);
            stone.beginBreak(f.player);
            int start = stone.breakTicks();
            stone.beginBreak(f.player);
            h.assertTrue(stone.breakTicks() == start, "a second press does not restart the break");
            h.succeed();
        }
    }
}
