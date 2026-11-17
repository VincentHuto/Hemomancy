package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.block.harbinger.functional.EarthenVeinBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.block.vein.VeinLocation;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulations;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.KnownManipulationServerPacket;
import com.vincenthuto.hemomancy.common.network.vein.OpenEarthenVeinDisplayPacket;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.EarthenVeinBlockEntity;
import com.vincenthuto.hemomancy.common.world.vein.EarthenVeinTravelManager;
import com.vincenthuto.hemomancy.common.world.vein.EarthenVeinTravelRules;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("earthen_vein_travel_validation")
@PrefixGameTestTemplate(false)
public final class EarthenVeinTravelGameTests {
    @GameTest(template = "empty", timeoutTicks = 40)
    public static void destroyedDestinationDisappearsFromKnowledgeAndOpenDisplay(GameTestHelper helper) {
        checkDestination(helper, false, false, false);
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void destroyedLastDestinationClearsSelectionAndSavedKnowledge(GameTestHelper helper) {
        checkDestination(helper, false, false, true);
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void replacementAtSamePositionDoesNotKeepOldClaim(GameTestHelper helper) {
        checkDestination(helper, true, false, false);
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void obstructedLivingDestinationRemainsKnown(GameTestHelper helper) {
        checkDestination(helper, true, true, false);
    }

    private static void checkDestination(GameTestHelper helper, boolean present, boolean obstructed,
            boolean lastDestination) {
        try (var captured = CapturedPlayer.create(helper).online()) {
            var player = captured.player;
            var level = helper.getLevel();
            BlockPos sourcePos = helper.absolutePos(new BlockPos(1, 2, 1));
            BlockPos targetPos = sourcePos.offset(5, 0, 0);
            BlockPos otherPos = sourcePos.offset(0, 0, 5);
            var source = placeVein(helper, sourcePos);
            if (lastDestination) source.makeTemporary(player.getUUID(), level.getGameTime() + 600);
            var target = placeVein(helper, targetPos);
            var known = HemoCapabilityAccess.getKnownManipulations(player);
            if (!lastDestination) known.getVeinList().add(source.getLoc());
            VeinLocation claimed = target.getLoc();
            known.getVeinList().add(claimed);
            known.setSelectedVein(claimed);
            if (!lastDestination) known.getVeinList().add(placeVein(helper, otherPos).getLoc());
            player.setPos(sourcePos.getX() + .5, sourcePos.getY() + 1, sourcePos.getZ() + .5);
            helper.assertTrue(EarthenVeinTravelManager.begin(player, sourcePos), "Claimed source opens travel");
            var opened = captured.sent(OpenEarthenVeinDisplayPacket.class).getFirst();
            if (!present) level.removeBlock(targetPos, false);
            else if (!obstructed) {
                level.removeBlock(targetPos, false);
                placeVein(helper, targetPos);
            } else level.setBlock(targetPos.above(), Blocks.STONE.defaultBlockState(), 3);
            captured.payloads.clear();
            EarthenVeinTravelManager.select(player, opened.nonce(), claimed.getUUID());

            if (obstructed) {
                helper.assertTrue(known.getVeinList().contains(claimed), "Obstruction must retain the claim");
                helper.assertTrue(captured.sent(OpenEarthenVeinDisplayPacket.class).isEmpty(),
                        "An obstructed living destination does not prune the display");
            } else {
                helper.assertTrue(!known.getVeinList().contains(claimed), "Ruptured claim removed from knowledge");
                var updates = captured.sent(OpenEarthenVeinDisplayPacket.class);
                helper.assertTrue(updates.size() == 1, "The open destination display must refresh immediately");
                var refreshed = updates.getFirst();
                helper.assertTrue(refreshed.nonce().equals(opened.nonce()), "Refresh retains the active session");
                helper.assertTrue(refreshed.destinations().size() == (lastDestination ? 0 : 1),
                        "Only the remaining destination stays visible");
                helper.assertTrue(refreshed.destinations().stream().noneMatch(d -> d.id().equals(claimed.getUUID())),
                        "The ruptured marker is absent from the refreshed display");
                helper.assertTrue(!known.getSelectedVein().getUUID().equals(claimed.getUUID()),
                        "Legacy selected destination must not retain the ruptured vein");
                helper.assertTrue(captured.sent(KnownManipulationServerPacket.class).size() == 1,
                        "Updated knowledge is sent to the owner");
                var restored = new KnownManipulations();
                restored.deserializeNBT(level.registryAccess(), known.serializeNBT(level.registryAccess()));
                helper.assertTrue(restored.getVeinList().stream().noneMatch(v -> v.getUUID().equals(claimed.getUUID())),
                        "The removed claim stays absent after a save round trip");
                if (lastDestination) helper.assertTrue(level.getBlockEntity(sourcePos) == null,
                        "Losing the final destination dismisses the temporary origin");
            }
            EarthenVeinTravelManager.logout(player);
            level.removeBlock(sourcePos, false);
            level.removeBlock(targetPos, false);
            level.removeBlock(targetPos.above(), false);
            if (!lastDestination) level.removeBlock(otherPos, false);
        }
        helper.succeed();
    }

    /**
     * Core-loop round two P8: two stented, claimed Earthen Veins five blocks apart on one floor. A living vein run that
     * joins them carries the crossing for {@code LIVING_VEIN_COST} (125), shown in the display and charged when fed; the
     * same pair with a cell of the run never grown (plain gouge) costs what the distance does (500).
     */
    @GameTest(template = "empty", timeoutTicks = 40)
    public static void aLivingVeinBetweenTwoOpeningsCarriesTheCrossingCheaply(GameTestHelper helper) {
        checkCarried(helper, true);
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void openingsAGougeOnlyJoinsCostWhatTheDistanceDoes(GameTestHelper helper) {
        checkCarried(helper, false);
    }

    private static void checkCarried(GameTestHelper helper, boolean living) {
        var level = helper.getLevel();
        BlockPos sourcePos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos targetPos = sourcePos.offset(5, 0, 0);
        for (int x = 0; x <= 7; x++) {
            for (int z = 0; z <= 2; z++) {
                BlockPos floor = helper.absolutePos(new BlockPos(x, 1, z));
                level.setBlock(floor, Blocks.POLISHED_ANDESITE.defaultBlockState(), 3);
                for (int y = 2; y <= 4; y++) level.setBlock(floor.above(y - 1), Blocks.AIR.defaultBlockState(), 3);
            }
        }
        var source = placeVein(helper, sourcePos);
        var target = placeVein(helper, targetPos);
        for (int step = 1; step <= 4; step++) {
            BlockPos cell = sourcePos.offset(step, 0, 0);
            var block = living || step != 2 ? BlockInit.living_vein.get() : BlockInit.blood_gouge.get();
            level.setBlock(cell, block.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.MultifaceBlock.getFaceProperty(net.minecraft.core.Direction.DOWN), true), 3);
        }
        int expected = living ? EarthenVeinTravelRules.LIVING_VEIN_COST : 500;
        try (var captured = CapturedPlayer.create(helper).online()) {
            var player = captured.player;
            var blood = HemoCapabilityAccess.getBloodVolume(player);
            blood.setActive(true);
            blood.setMaxBloodVolume(5_000.0D);
            blood.setBloodVolume(4_000.0D);
            var known = HemoCapabilityAccess.getKnownManipulations(player);
            known.getVeinList().add(source.getLoc());
            known.getVeinList().add(target.getLoc());
            player.setPos(sourcePos.getX() + .5, sourcePos.getY() + 1, sourcePos.getZ() + .5);
            helper.assertTrue(EarthenVeinTravelManager.begin(player, sourcePos), "Claimed source opens travel");
            var opened = captured.sent(OpenEarthenVeinDisplayPacket.class).getFirst();
            var shown = opened.destinations().stream().filter(d -> d.id().equals(target.getLoc().getUUID())).findFirst();
            helper.assertTrue(shown.isPresent(), "the joined vein is not offered");
            helper.assertTrue(shown.get().cost() == expected, "the display prices the crossing at " + shown.get().cost()
                    + ", not " + expected);
            EarthenVeinTravelManager.select(player, opened.nonce(), target.getLoc().getUUID());
            helper.assertTrue(!com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(player, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_CARRIED_CROSSING), "carried before any crossing was paid");
            double fed = EarthenVeinTravelManager.project(player, level, sourcePos, 10_000.0D);
            helper.assertTrue(Math.abs(fed - expected) < 1.0E-6, "the opening took " + fed + " mB, not " + expected);
            helper.assertTrue(com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(player, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_CARRIED_CROSSING) == living, living
                    ? "a crossing a living vein carries did not grant Carried by the Vein"
                    : "a crossing a gouge joins granted Carried by the Vein");
            helper.assertTrue(EarthenVeinTravelManager.project(player, level, sourcePos, 100.0D) == 0.0D,
                    "a sated opening took more blood");
            EarthenVeinTravelManager.logout(player);
        } finally {
            for (int step = 0; step <= 5; step++) level.removeBlock(sourcePos.offset(step, 0, 0), false);
        }
        helper.succeed();
    }

    private static EarthenVeinBlockEntity placeVein(GameTestHelper helper, BlockPos pos) {
        var level = helper.getLevel();
        level.setBlock(pos, BlockInit.earthen_vein.get().defaultBlockState()
                .setValue(EarthenVeinBlock.STENTED, true), 3);
        var vein = (EarthenVeinBlockEntity) level.getBlockEntity(pos);
        vein.setLoc(new VeinLocation("Travel fixture", level.dimension().location(), pos));
        vein.setHasTicked(true);
        return vein;
    }
}
