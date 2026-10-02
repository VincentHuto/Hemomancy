package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.block.harbinger.crafting.GhastlyAlembicBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.station.StationTierProperty;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.GhastlyAlembicBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.UUID;

@GameTestHolder("station_upgrade_validation")
@PrefixGameTestTemplate(false)
public final class AlembicFootprintGameTests {
    @GameTest(template = "empty", timeoutTicks = 60, batch = "station_upgrade")
    public static void everyAlembicTierUsesClippedModelCollisionAndSelection(GameTestHelper h) {
        var level = h.getLevel();
        var block = (GhastlyAlembicBlock) BlockInit.ghastly_alembic.get();
        BlockPos pos = h.absolutePos(new BlockPos(4, 3, 4));
        for (int stage = 0; stage <= 2; stage++) {
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                clearSpace(h, pos);
                var state = block.defaultBlockState().setValue(StationTierProperty.STAGE, stage)
                        .setValue(GhastlyAlembicBlock.FACING, facing);
                level.setBlockAndUpdate(pos, state);
                block.setPlacedBy(level, pos, state, null, new ItemStack(block));
                var cells = com.vincenthuto.hemomancy.common.block.harbinger.crafting.AlembicGeometry
                        .cells(stage, facing);
                double height = 0;
                for (var part : cells.entrySet()) {
                    BlockPos cell = pos.offset(part.getKey());
                    var placed = level.getBlockState(cell);
                    var collision = placed.getCollisionShape(level, cell);
                    var selection = placed.getShape(level, cell);
                    h.assertTrue(!collision.isEmpty(), "Missing Alembic collision at " + part.getKey());
                    h.assertTrue(!net.minecraft.world.phys.shapes.Shapes.joinIsNotEmpty(
                                    collision, part.getValue(), net.minecraft.world.phys.shapes.BooleanOp.NOT_SAME)
                                    && !net.minecraft.world.phys.shapes.Shapes.joinIsNotEmpty(
                                    selection, part.getValue(), net.minecraft.world.phys.shapes.BooleanOp.NOT_SAME),
                            "Alembic model/selection/collision disagree for stage " + stage + " " + facing
                                    + " at " + part.getKey());
                    var bounds = collision.bounds();
                    h.assertTrue(bounds.minX >= 0 && bounds.minY >= 0 && bounds.minZ >= 0
                                    && bounds.maxX <= 1 && bounds.maxY <= 1 && bounds.maxZ <= 1,
                            "Alembic collision escapes its linked cell");
                    height = Math.max(height, part.getKey().getY() + bounds.maxY);
                }
                h.assertTrue(Math.abs(height - (stage == 2 ? 38.0 : 31.0) / 16.0) < 0.000001,
                        "Alembic lost its authored height for stage " + stage + " " + facing);
                level.removeBlock(pos, false);
            }
        }
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60, batch = "station_upgrade")
    public static void basicAlembicAndLinkedPartsLeaveLightPathsOpen(GameTestHelper h) {
        var level = h.getLevel();
        BlockPos pos = h.absolutePos(new BlockPos(4, 3, 4));
        clearSpace(h, pos);
        var state = BlockInit.ghastly_alembic.get().defaultBlockState();
        level.setBlockAndUpdate(pos, state);
        state.getBlock().setPlacedBy(level, pos, state, null, new ItemStack(state.getBlock()));
        h.assertTrue(!state.canOcclude() && !state.isViewBlocking(level,pos)
                && state.getLightBlock(level,pos)==0, "Open Alembic behaves as an opaque block");
        BlockPos upper = pos.above();
        var linked = level.getBlockState(upper);
        h.assertTrue(linked.is(BlockInit.filler_block.get()) && !linked.canOcclude()
                && linked.getLightBlock(level,upper)==0
                && linked.getOcclusionShape(level,upper).isEmpty(), "Invisible upper part blocks lighting");
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60, batch = "station_upgrade")
    public static void blockedMantleAtRiteCompletionRecoversEscrow(GameTestHelper h) {
        var tier = com.vincenthuto.hemomancy.common.station.StationUpgradeCatalog.get(
                com.vincenthuto.hemomancy.common.station.UpgradeStation.ALEMBIC, 2);
        var rite = StationUpgradeGameTests.upgradeRite(h, tier);
        var level = h.getLevel();
        h.assertTrue(com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.prepare(level, rite), "Clear Athanor preparation failed");
        var station = com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.station(level, rite);
        for (int i=0; i<rite.getAnchorBloodMl().length; i++) rite.fillAnchor(i,50);
        h.assertTrue(rite.enterInscription() && rite.sealAltar(false), "Rite did not enter projection");
        for (int circuit=0; circuit<tier.circuits(); circuit++)
            h.assertTrue(rite.fillUpgradeCircuit(circuit,tier.bloodPerCircuit()), "Circuit refused");
        BlockPos obstacle = station.getBlockPos().south();
        level.setBlockAndUpdate(obstacle, Blocks.STONE.defaultBlockState());
        rite.markComplete();
        h.assertTrue(!com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites.complete(level,rite)
                && rite.upgrade().getString("EscrowState").equals("RETURNED")
                && station.upgradeTier()==1 && !station.isRiteLocked()
                && level.getBlockState(obstacle).is(Blocks.STONE), "Blocked finale failed to recover safely");
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60, batch = "station_upgrade")
    public static void athanorPlacementReservesRotatedMantleAndCleansUp(GameTestHelper h) {
        var level = h.getLevel();
        var block = (GhastlyAlembicBlock) BlockInit.ghastly_alembic.get();
        BlockPos pos = h.absolutePos(new BlockPos(4, 3, 4));
        clearSpace(h, pos);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            var state = block.defaultBlockState().setValue(GhastlyAlembicBlock.FACING, facing)
                    .setValue(StationTierProperty.STAGE, 2);
            level.setBlockAndUpdate(pos, state);
            block.setPlacedBy(level, pos, state, null, new ItemStack(block));
            BlockPos mantleEdge = pos.relative(facing.getOpposite());
            h.assertTrue(level.getBlockState(mantleEdge).is(BlockInit.filler_block.get()), "Mantle footprint was not reserved " + facing);
            var edgeShape = level.getBlockState(mantleEdge).getCollisionShape(level, mantleEdge);
            h.assertTrue(!edgeShape.isEmpty() && edgeShape.bounds().maxY <= .625, "Mantle edge uses a full-block collision");
            h.assertTrue(level.getBlockState(pos.above(2)).is(BlockInit.filler_block.get()), "Raised Athanor lost its top clearance");
            level.removeBlock(pos, false);
            h.assertTrue(level.getBlockState(mantleEdge).isAir() && level.getBlockState(pos.above(2)).isAir(), "Breaking left orphan fillers");
        }
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60, batch = "station_upgrade")
    public static void blockedAthanorUpgradePreservesStageContentsAndObstacle(GameTestHelper h) {
        var level = h.getLevel();
        BlockPos pos = h.absolutePos(new BlockPos(4, 3, 4));
        clearSpace(h, pos);
        var state = BlockInit.ghastly_alembic.get().defaultBlockState().setValue(StationTierProperty.STAGE, 1);
        level.setBlockAndUpdate(pos, state);
        state.getBlock().setPlacedBy(level, pos, state, null, new ItemStack(state.getBlock()));
        var station = (GhastlyAlembicBlockEntity) level.getBlockEntity(pos);
        station.setItem(0, new ItemStack(net.minecraft.world.item.Items.GHAST_TEAR, 3));
        BlockPos blocked = pos.south();
        level.setBlockAndUpdate(blocked, Blocks.STONE.defaultBlockState());
        h.assertTrue(!station.readyForUpgradeRite(level, Direction.NORTH), "Rite accepted blocked mantle");
        h.assertTrue(!station.completeUpgrade(2, UUID.randomUUID()), "Completion overwrote mantle obstacle");
        h.assertTrue(station.upgradeTier() == 1 && station.getItem(0).getCount() == 3 && level.getBlockState(blocked).is(Blocks.STONE), "Rejected upgrade changed machine or obstacle");
        level.removeBlock(blocked, false);
        h.assertTrue(station.completeUpgrade(2, UUID.randomUUID()), "Clear upgrade refused");
        h.assertTrue(station.getItem(0).getCount() == 3 && level.getBlockState(blocked).is(BlockInit.filler_block.get()), "Upgrade failed to preserve inventory/reserve mantle");
        level.destroyBlock(blocked, true);
        h.assertTrue(level.getBlockState(pos).isAir() && level.getBlockState(pos.above(2)).isAir(), "Breaking mantle failed to remove controller and top");
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60, batch = "station_upgrade")
    public static void droppedAthanorItemChecksItsLargerPlacementSpace(GameTestHelper h) {
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        player.setYRot(180);
        var block = (GhastlyAlembicBlock) BlockInit.ghastly_alembic.get();
        var stack = new ItemStack(block);
        stack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(StationTierProperty.STAGE, 2));
        BlockPos pos = h.absolutePos(new BlockPos(4, 3, 4));
        clearSpace(h, pos);
        h.getLevel().setBlockAndUpdate(pos.south(), Blocks.STONE.defaultBlockState());
        var context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, stack,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        h.assertTrue(block.getStateForPlacement(context) == null, "Dropped Athanor ignores mantle clearance");
        h.getLevel().removeBlock(pos.south(), false);
        var placed = block.getStateForPlacement(context);
        h.assertTrue(placed != null && placed.getValue(StationTierProperty.STAGE) == 2, "Placement lost saved Athanor stage");
        h.succeed();
    }

    static void clearSpace(GameTestHelper h, BlockPos pos) {
        for (BlockPos cell : BlockPos.betweenClosed(pos.offset(-1,0,-1),pos.offset(1,2,1)))
            h.getLevel().setBlockAndUpdate(cell, Blocks.AIR.defaultBlockState());
    }
}
