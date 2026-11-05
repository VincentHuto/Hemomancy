package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.block.shared.LidlessPassageService;
import com.vincenthuto.hemomancy.common.block.shared.LidlessPortal;
import com.vincenthuto.hemomancy.common.block.shared.LidlessTheme;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager;
import com.vincenthuto.hemomancy.common.encounter.ChamberProxySnapshot;
import com.vincenthuto.hemomancy.common.encounter.ChamberRoomShape;
import com.vincenthuto.hemomancy.common.encounter.ChamberVisitService;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.SeedBinding;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodProjectionItem;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineMonolithBlockEntity;
import com.vincenthuto.hemomancy.common.tile.shared.ChamberGateBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

/**
 * The husk as a gateway: the Seed bound to whoever took it, the third eye woken by its owner's blood, the walk into the
 * Chamber and out of it, and the path's theme. The GameTest server has no Chamber dimension, so the arrival itself is
 * proved by the review client; here a crossing that cannot start a visit steps its player back out.
 */
@GameTestHolder("chamber_gateway_validation")
@PrefixGameTestTemplate(false)
public final class ChamberGatewayGameTests {
    static final String ARENA = MonolithWalkGameTests.ARENA;
    static final String NAMESPACE = "chamber_gateway_validation";

    /** A broken husk whose Seed is gone, and the Archon who took it. */
    private static SanguineMonolithBlockEntity emptied(GameTestHelper h, MonolithWalkGameTests.Fixture f) {
        MonolithWalkGameTests.breakOpen(h, f.base, f.player());
        SanguineMonolithBlockEntity stone = MonolithWalkGameTests.entity(h, f.base);
        stone.takeSeed(f.player());
        return stone;
    }

    private static ChamberOfWillManager manager(GameTestHelper h) {
        return ChamberOfWillManager.get(h.getLevel().getServer());
    }

    private static void blood(ServerPlayer player, double amount) {
        var volume = HemoCapabilityAccess.getBloodVolume(player);
        volume.setActive(true);
        volume.setMaxBloodVolume(Math.max(volume.getMaxBloodVolume(), amount));
        volume.setBloodVolume(amount);
    }

    private static void path(ServerPlayer player, EnumArchonPath path) {
        var degree = HemoCapabilityAccess.getInitiatoryDegree(player);
        degree.setDegreeNumber(8);
        degree.setArchonPath(path);
    }

    // ── The Seed ─────────────────────────────────────────────────────────────

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "gateway")
    public static void a_taken_seed_names_its_taker(GameTestHelper h) {
        BlockPos base = MonolithWalkGameTests.place(h);
        try (var f = new MonolithWalkGameTests.Fixture(h, base, 1.9)) {
            emptied(h, f);
            ItemStack seed = ItemStack.EMPTY;
            for (ItemStack stack : f.player().getInventory().items) if (stack.is(ItemInit.qliphoth_seed.get())) seed = stack;
            SeedBinding binding = seed.get(DataComponentInit.SEED_BINDING.get());
            h.assertTrue(binding != null && binding.owner().equals(f.player().getUUID()), "the Seed is bound to its taker");
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "gateway")
    public static void a_spilled_seed_names_the_archon_who_broke_the_stone(GameTestHelper h) {
        BlockPos base = MonolithWalkGameTests.place(h);
        try (var f = new MonolithWalkGameTests.Fixture(h, base, 1.9)) {
            MonolithWalkGameTests.breakOpen(h, base, f.player());
            MonolithWalkGameTests.entity(h, base).spillSeed(h.getLevel());
            ItemEntity drop = h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(base).inflate(3),
                    e -> e.getItem().is(ItemInit.qliphoth_seed.get())).get(0);
            SeedBinding binding = drop.getItem().get(DataComponentInit.SEED_BINDING.get());
            h.assertTrue(binding != null && binding.owner().equals(f.player().getUUID()), "still only theirs");
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "gateway")
    public static void a_bound_seed_refuses_anyone_else(GameTestHelper h) {
        BlockPos base = MonolithWalkGameTests.place(h);
        try (var owner = new MonolithWalkGameTests.Fixture(h, base, 1.9); var other = new MonolithWalkGameTests.Fixture(h, base, 2.5)) {
            ItemStack seed = new ItemStack(ItemInit.qliphoth_seed.get());
            h.assertTrue(SeedBinding.refusal(seed, other.player()) == null, "an unbound Seed answers to anyone");
            SeedBinding.bind(seed, owner.player().getUUID(), "Owner");
            h.assertTrue(SeedBinding.refusal(seed, owner.player()) == null, "its owner may seat it");
            h.assertTrue(SeedBinding.refusal(seed, other.player()) != null, "no one else may");
            h.assertTrue(SeedBinding.refusal(new ItemStack(ItemInit.monolith_fragment.get()), other.player()) == null, "other media are untouched");
            h.succeed();
        }
    }

    // ── The third eye ────────────────────────────────────────────────────────

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "gateway")
    public static void the_eye_shuts_when_the_seed_is_taken_and_admits_no_one(GameTestHelper h) {
        BlockPos base = MonolithWalkGameTests.place(h);
        try (var f = new MonolithWalkGameTests.Fixture(h, base, 1.9)) {
            SanguineMonolithBlockEntity stone = emptied(h, f);
            h.assertFalse(stone.admits(f.player()), "shut to its owner too");
            h.assertTrue(!h.getLevel().getBlockState(base).getCollisionShape(h.getLevel(), base, CollisionContext.of(f.player())).isEmpty(),
                    "solid once shut");
            h.assertTrue(manager(h).hasGateway(f.player().getUUID()), "taking the Seed links the husk");
            manager(h).clearGateway(f.player().getUUID(), h.getLevel().dimension().location(), base);
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "gateway", timeoutTicks = 200)
    public static void waking_the_eye_takes_the_overworld_around_the_husk(GameTestHelper h) {
        // So the walk back can show it: the blocks around the husk, nearest first, the sky's colour and the hour.
        BlockPos base = MonolithWalkGameTests.place(h);
        var f = new MonolithWalkGameTests.Fixture(h, base, 1.9);
        SanguineMonolithBlockEntity stone = emptied(h, f);
        blood(f.player(), 500);
        h.onEachTick(() -> { if (!stone.gateOpen()) stone.takeProjectedBlood(f.player(), base, 10.0D, false); });
        h.runAfterDelay(SanguineMonolithBlockEntity.GATE_CHARGE_TICKS + 10, () -> {
            var outside = manager(h).outside(f.player().getUUID());
            h.assertTrue(outside != null, "the husk's surroundings were taken");
            h.assertTrue(outside.husk().equals(base), "around this husk");
            h.assertTrue(outside.facing() == stone.front(), "facing as the husk faces");
            h.assertTrue(outside.blocks().length > 0 && outside.blocks().length <= com.vincenthuto.hemomancy.common.encounter.HuskOutsideSnapshot.CAP,
                    "with the arena's blocks, within the cap: " + outside.blocks().length);
            boolean huskInside = false;
            for (long packed : outside.blocks()) {
                if (ChamberProxySnapshot.dx(packed) == 0 && ChamberProxySnapshot.dz(packed) == 0
                        && (ChamberProxySnapshot.dy(packed) == 0 || ChamberProxySnapshot.dy(packed) == 1)) huskInside = true;
            }
            h.assertFalse(huskInside, "the husk itself is not in the view out of it");
            f.close();
            manager(h).clearGateway(f.player().getUUID(), h.getLevel().dimension().location(), base);
            h.succeed();
        });
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "gateway")
    public static void the_husk_admits_its_owner_from_the_front_only(GameTestHelper h) {
        // The eye is a window on the front face: a body coming from behind would walk into the back of the view. From
        // behind the husk stays solid and offers nothing, with the Seed inside and with the eye open alike.
        BlockPos base = MonolithWalkGameTests.place(h);
        try (var f = new MonolithWalkGameTests.Fixture(h, base, 1.9)) {
            MonolithWalkGameTests.breakOpen(h, base, f.player());
            SanguineMonolithBlockEntity stone = MonolithWalkGameTests.entity(h, base);
            h.assertTrue(stone.seedInside() && stone.admits(f.player()), "in front, the owner is admitted to the Seed");
            f.player().setPos(net.minecraft.world.phys.Vec3.atBottomCenterOf(base).add(0, 0, -1.9));
            h.assertFalse(stone.admits(f.player()), "from behind, not");
            h.assertFalse(h.getLevel().getBlockState(base).getCollisionShape(h.getLevel(), base, CollisionContext.of(f.player())).isEmpty(),
                    "and the stone stays solid to them there");
            f.player().setPos(net.minecraft.world.phys.Vec3.atBottomCenterOf(base).add(0, 0, 1.9));
            h.assertTrue(stone.admits(f.player()), "back in front, admitted again");
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "gateway")
    public static void a_held_item_on_the_husk_goes_on_to_its_own_use(GameTestHelper h) {
        // Blood Projection is started by its item's own use; a block that swallowed the click with an item in hand
        // made the owner start projecting elsewhere and walk onto the husk. Only an empty hand is told the hint.
        BlockPos base = MonolithWalkGameTests.place(h);
        try (var f = new MonolithWalkGameTests.Fixture(h, base, 1.9)) {
            var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(base), Direction.SOUTH, base, false);
            var projection = new ItemStack(ItemInit.blood_projection.get());
            // A cell hand is a cast, not a thing to ask the stone about: it goes on to its own use on a whole stone too,
            // where any other item opens the stone's item inquiry.
            h.assertTrue(h.getLevel().getBlockState(base).useItemOn(projection, h.getLevel(), f.player(), net.minecraft.world.InteractionHand.MAIN_HAND, hit)
                    == net.minecraft.world.ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION, "a cell hand passes a whole stone too");
            h.assertTrue(h.getLevel().getBlockState(base).useItemOn(new ItemStack(net.minecraft.world.item.Items.STONE), h.getLevel(), f.player(), net.minecraft.world.InteractionHand.MAIN_HAND, hit)
                    == net.minecraft.world.ItemInteractionResult.SUCCESS, "another item is asked about");
            emptied(h, f);
            var state = h.getLevel().getBlockState(base);
            h.assertTrue(state.useItemOn(projection, h.getLevel(), f.player(), net.minecraft.world.InteractionHand.MAIN_HAND, hit)
                    == net.minecraft.world.ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION, "a held item passes to its own use");
            h.assertTrue(state.useWithoutItem(h.getLevel(), f.player(), hit) == net.minecraft.world.InteractionResult.CONSUME,
                    "an empty hand is still answered");
            manager(h).clearGateway(f.player().getUUID(), h.getLevel().dimension().location(), base);
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "gateway", timeoutTicks = 200)
    public static void the_owners_blood_wakes_the_eye_in_two_seconds_for_a_small_price(GameTestHelper h) {
        BlockPos base = MonolithWalkGameTests.place(h);
        var f = new MonolithWalkGameTests.Fixture(h, base, 1.9);
        SanguineMonolithBlockEntity stone = emptied(h, f);
        blood(f.player(), 500);
        int[] fed = {0};
        h.onEachTick(() -> {
            if (!stone.gateOpen() && stone.takeProjectedBlood(f.player(), base, 10.0D, false) > 0.0D) fed[0]++;
        });
        h.runAfterDelay(SanguineMonolithBlockEntity.GATE_CHARGE_TICKS + 10, () -> {
            double left = HemoCapabilityAccess.getBloodVolume(f.player()).getBloodVolume();
            h.assertTrue(stone.gateOpen(), "open after its charge");
            h.assertTrue(fed[0] == SanguineMonolithBlockEntity.GATE_CHARGE_TICKS, "one feed a tick, fed " + fed[0]);
            h.assertTrue(Math.abs(500 - left - 100) < 1.0D, "a hundred blood, paid " + (500 - left));
            h.assertTrue(stone.admits(f.player()), "admits its owner while open");
            f.close();
            manager(h).clearGateway(f.player().getUUID(), h.getLevel().dimension().location(), base);
            h.succeed();
        });
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "gateway")
    public static void a_stranger_cannot_wake_it_or_be_charged(GameTestHelper h) {
        BlockPos base = MonolithWalkGameTests.place(h);
        try (var owner = new MonolithWalkGameTests.Fixture(h, base, 1.9); var other = new MonolithWalkGameTests.Fixture(h, base, 2.5)) {
            SanguineMonolithBlockEntity stone = emptied(h, owner);
            blood(other.player(), 500);
            h.assertTrue(stone.takeProjectedBlood(other.player(), base, 10.0D, false) == 0.0D, "takes nothing");
            h.assertTrue(HemoCapabilityAccess.getBloodVolume(other.player()).getBloodVolume() == 500, "charges nothing");
            manager(h).clearGateway(owner.player().getUUID(), h.getLevel().dimension().location(), base);
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "gateway")
    public static void blood_at_the_filler_reaches_the_stone(GameTestHelper h) {
        BlockPos base = MonolithWalkGameTests.place(h);
        try (var f = new MonolithWalkGameTests.Fixture(h, base, 1.9)) {
            emptied(h, f);
            blood(f.player(), 500);
            var filler = (com.vincenthuto.hemomancy.common.tile.ProjectedBloodSink) h.getLevel().getBlockEntity(base.above());
            h.assertTrue(filler.takeProjectedBlood(f.player(), base.above(), 10.0D, false) > 0.0D, "the upper cell takes it too");
            manager(h).clearGateway(f.player().getUUID(), h.getLevel().dimension().location(), base);
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "gateway", timeoutTicks = 200)
    public static void charge_bleeds_away_when_the_blood_stops(GameTestHelper h) {
        BlockPos base = MonolithWalkGameTests.place(h);
        var f = new MonolithWalkGameTests.Fixture(h, base, 1.9);
        SanguineMonolithBlockEntity stone = emptied(h, f);
        blood(f.player(), 500);
        int[] ticks = {0};
        h.onEachTick(() -> {
            if (ticks[0]++ < 20) stone.takeProjectedBlood(f.player(), base, 10.0D, false);
        });
        h.runAfterDelay(50, () -> {
            h.assertFalse(stone.gateOpen(), "half a charge opens nothing");
            CompoundTag saved = stone.saveWithoutMetadata(h.getLevel().registryAccess());
            h.assertTrue(saved.getInt("GateCharge") == 0, "the charge has bled away, left " + saved.getInt("GateCharge"));
            f.close();
            manager(h).clearGateway(f.player().getUUID(), h.getLevel().dimension().location(), base);
            h.succeed();
        });
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "gateway")
    public static void the_open_eye_closes_on_its_own(GameTestHelper h) {
        BlockPos base = MonolithWalkGameTests.place(h);
        try (var f = new MonolithWalkGameTests.Fixture(h, base, 1.9)) {
            SanguineMonolithBlockEntity stone = emptied(h, f);
            stone.openBriefly();
            for (int i = 0; i < SanguineMonolithBlockEntity.GATE_OPEN_TICKS; i++) stone.tick();
            h.assertFalse(stone.gateOpen(), "shut again");
            manager(h).clearGateway(f.player().getUUID(), h.getLevel().dimension().location(), base);
            h.succeed();
        }
    }

    // ── The walk ─────────────────────────────────────────────────────────────

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "gateway")
    public static void the_walk_in_is_six_blocks_in_gate_mode(GameTestHelper h) {
        BlockPos base = MonolithWalkGameTests.place(h);
        try (var f = new MonolithWalkGameTests.Fixture(h, base, 1.9)) {
            SanguineMonolithBlockEntity stone = emptied(h, f);
            stone.openBriefly();
            f.enter();
            h.assertTrue(f.crossing(), "the owner steps in");
            h.assertTrue(f.lastState.mode() == LidlessPassageService.Mode.GATE, "gate mode");
            h.assertTrue(f.lastState.length() == LidlessPassageService.GATEWAY_LENGTH, "six blocks, not " + f.lastState.length());
            int ticks = (int) Math.ceil(LidlessPassageService.GATEWAY_LENGTH / MonolithWalkGameTests.WALK) + 1;
            f.forward(ticks);
            h.assertFalse(f.crossing(), "reached the end");
            h.assertFalse(stone.gateOpen(), "the eye closes behind them");
            if (h.getLevel().getServer().getLevel(ChamberOfWillManager.CHAMBER_OF_WILL) == null) {
                h.assertTrue(f.standingAt(f.entryCell()), "with no Chamber to enter they step back out, at " + f.player().blockPosition());
            }
            manager(h).clearGateway(f.player().getUUID(), h.getLevel().dimension().location(), base);
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "gateway")
    public static void a_leaving_crossing_hands_over_instead_of_landing(GameTestHelper h) {
        BlockPos base = MonolithWalkGameTests.place(h);
        try (var f = new MonolithWalkGameTests.Fixture(h, base, 1.9)) {
            boolean[] completed = {false};
            LidlessPortal stub = new LidlessPortal() {
                @Override public BlockPos anchor() { return base; }
                @Override public Direction facing() { return Direction.SOUTH; }
                @Override public boolean admits(ServerPlayer player) { return true; }
                @Override public boolean towardFront(ServerPlayer player) { return false; }
                @Override public LidlessPassageService.Mode mode() { return LidlessPassageService.Mode.RETURN; }
                @Override public boolean stillValid(ServerLevel level) { return true; }
                @Override public void completed(ServerPlayer player, boolean towardFront) { completed[0] = true; }
                @Override public int length() { return 2; }
                @Override public LidlessTheme theme(ServerPlayer player) { return LidlessTheme.APOTHEOS; }
            };
            LidlessPassageService.offer(f.player(), stub);
            LidlessPassageService.tick(f.player());
            h.assertTrue(f.crossing(), "crossing");
            h.assertTrue(f.lastState.theme() == LidlessTheme.APOTHEOS, "the theme reaches the client");
            f.forward(12);
            h.assertTrue(completed[0], "the doorway takes over");
            h.assertFalse(f.crossing(), "the pin is released");
            h.assertTrue(f.player().blockPosition().equals(base), "and the service did not move them");
            h.succeed();
        }
    }

    // ── The link ─────────────────────────────────────────────────────────────

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "gateway")
    public static void removing_the_husk_clears_the_link(GameTestHelper h) {
        BlockPos base = MonolithWalkGameTests.place(h);
        try (var f = new MonolithWalkGameTests.Fixture(h, base, 1.9)) {
            emptied(h, f);
            h.assertTrue(manager(h).hasGateway(f.player().getUUID()), "linked");
            h.getLevel().setBlock(base, Blocks.AIR.defaultBlockState(), 3);
            h.assertFalse(manager(h).hasGateway(f.player().getUUID()), "the gate no longer leads anywhere");
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "gateway")
    public static void removing_an_older_husk_keeps_the_newer_link(GameTestHelper h) {
        BlockPos base = MonolithWalkGameTests.place(h);
        try (var f = new MonolithWalkGameTests.Fixture(h, base, 1.9)) {
            emptied(h, f);
            BlockPos elsewhere = base.offset(40, 0, 0);
            manager(h).linkGateway(f.player().getUUID(), h.getLevel().dimension().location(), elsewhere);
            h.getLevel().setBlock(base, Blocks.AIR.defaultBlockState(), 3);
            h.assertTrue(manager(h).hasGateway(f.player().getUUID()), "the link is to the other husk");
            manager(h).clearGateway(f.player().getUUID(), h.getLevel().dimension().location(), elsewhere);
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "gateway")
    public static void the_corridor_gate_opens_only_for_a_linked_owner(GameTestHelper h) {
        BlockPos base = MonolithWalkGameTests.place(h);
        BlockPos gatePos = base.offset(4, 0, 0);
        try (var f = new MonolithWalkGameTests.Fixture(h, base, 1.9); var other = new MonolithWalkGameTests.Fixture(h, base, 2.5)) {
            h.getLevel().setBlock(gatePos, BlockInit.chamber_gate.get().defaultBlockState(), 3);
            var gate = (ChamberGateBlockEntity) h.getLevel().getBlockEntity(gatePos);
            gate.configure(f.player().getUUID(), 7);
            h.assertFalse(gate.linked(), "sealed before any husk");
            h.assertFalse(gate.portal().admits(f.player()), "admits no one while sealed");
            emptied(h, f);
            gate.configure(f.player().getUUID(), 8);
            h.assertTrue(gate.linked(), "open once linked");
            h.assertTrue(gate.portal().admits(f.player()), "admits its owner");
            h.assertFalse(gate.portal().admits(other.player()), "and only its owner");
            h.assertTrue(gate.portal().length() == LidlessPassageService.GATEWAY_LENGTH, "six blocks back");
            h.assertTrue(gate.portal().mode() == LidlessPassageService.Mode.RETURN, "a walk out");
            // A dream or guided visit keeps its inventory snapshot: it does not walk out through the gate.
            var data = f.player().getPersistentData();
            data.putBoolean("hemomancy:chamber_visit_active", true);
            data.putString("hemomancy:chamber_visit_mode", "DREAM");
            h.assertTrue(ChamberVisitService.isObservational(f.player()), "fixture is a dream");
            h.assertFalse(gate.portal().admits(f.player()), "a dream is not let out this way");
            data.remove("hemomancy:chamber_visit_active");
            data.remove("hemomancy:chamber_visit_mode");
            manager(h).clearGateway(f.player().getUUID(), h.getLevel().dimension().location(), base);
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "gateway")
    public static void walking_out_to_a_vanished_husk_clears_the_link(GameTestHelper h) {
        BlockPos base = MonolithWalkGameTests.place(h);
        try (var f = new MonolithWalkGameTests.Fixture(h, base, 1.9)) {
            manager(h).linkGateway(f.player().getUUID(), h.getLevel().dimension().location(), base.offset(8, 0, 8));
            ChamberVisitService.walkOutThroughGate(f.player());
            h.assertFalse(manager(h).hasGateway(f.player().getUUID()), "nothing stands there now");
            h.succeed();
        }
    }

    // ── The projection route ─────────────────────────────────────────────────

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "gateway")
    public static void a_real_blood_projection_reaches_the_husk(GameTestHelper h) {
        BlockPos base = MonolithWalkGameTests.place(h);
        try (var f = new MonolithWalkGameTests.Fixture(h, base, 1.9)) {
            SanguineMonolithBlockEntity stone = emptied(h, f);
            blood(f.player(), 500);
            f.player().setYRot(180.0F);
            f.player().setXRot(0.0F);
            double spent = BloodProjectionItem.projectFromEntity(h.getLevel(), f.player(), 10.0D, 10.0D);
            h.assertTrue(spent > 0.0D, "nothing on the way to the stone takes the projection instead, spent " + spent);
            h.assertTrue(stone.saveWithoutMetadata(h.getLevel().registryAccess()).getInt("GateCharge") == 1, "and it charges the eye");
            manager(h).clearGateway(f.player().getUUID(), h.getLevel().dimension().location(), base);
            h.succeed();
        }
    }

    // ── The room ─────────────────────────────────────────────────────────────

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "gateway")
    public static void the_room_is_laid_out_as_a_keyhole_with_its_gate(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        UUID owner = UUID.randomUUID();
        manager(h).buildRoom(level, owner, null, 6);
        BlockPos cell = manager(h).cellPos(manager(h).idFor(owner));
        int reach = ChamberRoomShape.reach(6);
        h.assertFalse(level.getBlockState(cell.offset(6, 0, 0)).isAir(), "the circle's edge has floor");
        h.assertTrue(level.getBlockState(cell.offset(6, 0, -6)).isAir(), "the old square's corner has none");
        h.assertFalse(level.getBlockState(cell.offset(ChamberRoomShape.halfWidth(6), 0, reach)).isAir(), "the corridor's end has floor");
        h.assertTrue(level.getBlockState(cell.offset(0, 0, reach + 1)).isAir(), "and nothing past it");
        BlockPos gatePos = cell.offset(0, 1, reach);
        h.assertTrue(level.getBlockState(gatePos).is(BlockInit.chamber_gate.get()), "the gate stands on the last row");
        var gate = (ChamberGateBlockEntity) level.getBlockEntity(gatePos);
        h.assertTrue(gate.radius() == 6 && owner.equals(gate.owner()), "and knows its room");
        h.assertFalse(gate.linked(), "sealed: this owner has no husk");
        h.succeed();
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "gateway")
    public static void growth_moves_the_gate_and_touches_nothing_already_there(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        UUID owner = UUID.randomUUID();
        manager(h).buildRoom(level, owner, null, 4);
        BlockPos cell = manager(h).cellPos(manager(h).idFor(owner));
        BlockPos oldGate = cell.offset(0, 1, ChamberRoomShape.reach(4));
        h.assertTrue(level.getBlockState(oldGate).is(BlockInit.chamber_gate.get()), "a gate to begin with");
        BlockPos built = cell.offset(1, 1, 2);
        BlockPos inTheWay = cell.offset(5, 0, 0);
        level.setBlock(built, Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(inTheWay, Blocks.GOLD_BLOCK.defaultBlockState(), 3);
        var growth = manager(h).buildRoom(level, owner, null, 6);
        h.assertTrue(growth.expandedFromExistingRoom(), "it grew");
        h.assertTrue(level.getBlockState(oldGate).isAir(), "the old gate has gone");
        h.assertTrue(level.getBlockState(cell.offset(0, 1, ChamberRoomShape.reach(6))).is(BlockInit.chamber_gate.get()), "to the new end");
        h.assertTrue(level.getBlockState(built).is(Blocks.STONE), "a player's block stands");
        h.assertTrue(level.getBlockState(inTheWay).is(Blocks.GOLD_BLOCK), "even in the new floor's way");
        h.assertFalse(level.getBlockState(cell.offset(6, 0, 0)).isAir(), "the new floor is down");
        h.succeed();
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "gateway")
    public static void a_square_room_from_before_loses_only_its_bare_corners_and_only_once(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        UUID owner = UUID.randomUUID();
        manager(h).buildRoom(level, owner, null, 5);
        BlockPos cell = manager(h).cellPos(manager(h).idFor(owner));
        BlockPos bareCorner = cell.offset(5, 0, -5);
        BlockPos builtCorner = cell.offset(-5, 0, -5);
        level.setBlock(bareCorner, BlockInit.blood_wood_planks.get().defaultBlockState(), 3);
        level.setBlock(builtCorner, Blocks.STONE.defaultBlockState(), 3);
        // Its old lights sat at the square's corners, so the keyhole's own are not there yet.
        for (var marker : com.vincenthuto.hemomancy.common.encounter.ChamberExpansionRules.markerOffsets(5)) {
            level.setBlock(cell.offset(marker.x(), 0, marker.z()), BlockInit.blood_wood_planks.get().defaultBlockState(), 3);
        }
        CompoundTag saved = manager(h).save(new CompoundTag(), level.registryAccess());
        saved.remove("keyholeRooms");
        ChamberOfWillManager reloaded = ChamberOfWillManager.load(saved, level.registryAccess());
        reloaded.buildRoom(level, owner, null, 5);
        h.assertTrue(level.getBlockState(bareCorner).isAir(), "bare floor outside the keyhole goes");
        h.assertTrue(level.getBlockState(builtCorner).is(Blocks.STONE), "what a player put there stays");
        for (var marker : com.vincenthuto.hemomancy.common.encounter.ChamberExpansionRules.markerOffsets(5)) {
            h.assertTrue(level.getBlockState(cell.offset(marker.x(), 0, marker.z())).is(BlockInit.sporite_crystal.get()), "the room keeps its lights");
        }
        level.setBlock(bareCorner, BlockInit.blood_wood_planks.get().defaultBlockState(), 3);
        reloaded.buildRoom(level, owner, null, 5);
        h.assertTrue(level.getBlockState(bareCorner).is(BlockInit.blood_wood_planks.get()), "converted once, not every visit");
        h.succeed();
    }

    // ── The room through the eye ─────────────────────────────────────────────

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "gateway")
    public static void the_snapshot_holds_every_block_in_the_room_but_air_and_the_gate(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        UUID owner = UUID.randomUUID();
        manager(h).buildRoom(level, owner, null, 4);
        BlockPos cell = manager(h).cellPos(manager(h).idFor(owner));
        level.setBlock(cell.offset(1, 2, 3), Blocks.GOLD_BLOCK.defaultBlockState(), 3);
        manager(h).linkGateway(owner, level.dimension().location(), cell.offset(50, 0, 0));
        var snapshot = com.vincenthuto.hemomancy.common.encounter.ChamberProxySnapshot.take(manager(h), level, owner, level.getServer());
        h.assertTrue(snapshot.radius() == 4 && snapshot.cell().equals(cell), "the room's size and place");
        boolean gold = false;
        boolean floor = false;
        boolean gate = false;
        for (long packed : snapshot.blocks()) {
            var state = Block.stateById(com.vincenthuto.hemomancy.common.encounter.ChamberProxySnapshot.stateId(packed));
            int dx = com.vincenthuto.hemomancy.common.encounter.ChamberProxySnapshot.dx(packed);
            int dy = com.vincenthuto.hemomancy.common.encounter.ChamberProxySnapshot.dy(packed);
            int dz = com.vincenthuto.hemomancy.common.encounter.ChamberProxySnapshot.dz(packed);
            h.assertFalse(state.isAir(), "no air");
            if (state.is(Blocks.GOLD_BLOCK) && dx == 1 && dy == 2 && dz == 3) gold = true;
            if (state.is(BlockInit.blood_wood_planks.get()) && dx == 0 && dy == 0 && dz == 0) floor = true;
            if (state.is(BlockInit.chamber_gate.get())) gate = true;
        }
        h.assertTrue(gold, "the owner's block, where they put it");
        h.assertTrue(floor, "the floor under the arbor");
        h.assertFalse(gate, "the gate draws nothing and is left out");
        h.assertTrue(snapshot.blocks().length <= com.vincenthuto.hemomancy.common.encounter.ChamberProxySnapshot.CAP, "capped");
        manager(h).clearGateway(owner, level.dimension().location(), cell.offset(50, 0, 0));
        h.succeed();
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "gateway")
    public static void a_linked_husk_carries_its_room_and_keeps_it_through_a_reload(GameTestHelper h) {
        BlockPos base = MonolithWalkGameTests.place(h);
        try (var f = new MonolithWalkGameTests.Fixture(h, base, 1.9)) {
            SanguineMonolithBlockEntity stone = emptied(h, f);
            h.assertTrue(stone.proxy() != null, "linked, so the eye has a room to show");
            h.assertTrue(stone.proxy().blocks().length == 0, "this server has no Chamber level, so no blocks");
            h.assertTrue(stone.proxy().degree() >= 7, "the owner's arbor as an Archon's");
            CompoundTag saved = stone.saveWithoutMetadata(h.getLevel().registryAccess());
            var reloaded = new SanguineMonolithBlockEntity(base, h.getLevel().getBlockState(base));
            reloaded.loadWithComponents(saved, h.getLevel().registryAccess());
            h.assertTrue(reloaded.proxy() != null && reloaded.proxy().radius() == stone.proxy().radius(), "and keeps it");
            manager(h).clearGateway(f.player().getUUID(), h.getLevel().dimension().location(), base);
            h.succeed();
        }
    }

    // ── The paths ────────────────────────────────────────────────────────────

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "gateway")
    public static void the_theme_follows_the_path(GameTestHelper h) {
        BlockPos base = MonolithWalkGameTests.place(h);
        try (var f = new MonolithWalkGameTests.Fixture(h, base, 1.9)) {
            h.assertTrue(LidlessTheme.of(f.player()) == LidlessTheme.QLIPHOTH, "an Archon");
            path(f.player(), EnumArchonPath.SILENTIUM);
            h.assertTrue(LidlessTheme.of(f.player()) == LidlessTheme.SILENTIUM, "a Silentium");
            path(f.player(), EnumArchonPath.APOTHEOS);
            h.assertTrue(LidlessTheme.of(f.player()) == LidlessTheme.APOTHEOS, "an Apotheos");
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "gateway")
    public static void an_apotheos_overgrows_the_husk_for_good(GameTestHelper h) {
        BlockPos base = MonolithWalkGameTests.place(h);
        try (var f = new MonolithWalkGameTests.Fixture(h, base, 1.9)) {
            SanguineMonolithBlockEntity stone = emptied(h, f);
            blood(f.player(), 500);
            h.assertFalse(stone.overgrown(), "clean to begin with");
            path(f.player(), EnumArchonPath.APOTHEOS);
            stone.takeProjectedBlood(f.player(), base, 10.0D, false);
            h.assertTrue(stone.overgrown(), "the first blood takes it");
            CompoundTag saved = stone.saveWithoutMetadata(h.getLevel().registryAccess());
            var reloaded = new SanguineMonolithBlockEntity(base, h.getLevel().getBlockState(base));
            reloaded.loadWithComponents(saved, h.getLevel().registryAccess());
            h.assertTrue(reloaded.overgrown(), "and it survives a reload");
            manager(h).clearGateway(f.player().getUUID(), h.getLevel().dimension().location(), base);
            h.succeed();
        }
    }

    @GameTest(templateNamespace = NAMESPACE, template = ARENA, batch = "gateway")
    public static void other_owners_do_not_overgrow_it(GameTestHelper h) {
        BlockPos base = MonolithWalkGameTests.place(h);
        try (var f = new MonolithWalkGameTests.Fixture(h, base, 1.9)) {
            SanguineMonolithBlockEntity stone = emptied(h, f);
            blood(f.player(), 500);
            stone.takeProjectedBlood(f.player(), base, 10.0D, false);
            h.assertFalse(stone.overgrown(), "an Archon's blood leaves it clean");
            path(f.player(), EnumArchonPath.SILENTIUM);
            h.assertTrue(LidlessTheme.of(f.player()) == LidlessTheme.SILENTIUM, "a Silentium");
            h.assertFalse(stone.overgrown(), "so does a Silentium's");
            manager(h).clearGateway(f.player().getUUID(), h.getLevel().dimension().location(), base);
            h.succeed();
        }
    }
}
