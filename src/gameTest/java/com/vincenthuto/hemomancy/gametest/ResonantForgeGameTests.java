package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.ResonantForgeBlock;
import com.vincenthuto.hemomancy.common.enchanting.ResonantForgeRules;
import com.vincenthuto.hemomancy.common.enchanting.ResonantForgeTier;
import com.vincenthuto.hemomancy.common.enchanting.ResonantForgeTransfer;
import com.vincenthuto.hemomancy.common.enchanting.ResonantPattern;
import com.vincenthuto.hemomancy.common.enchanting.ScriptoriumProvenance;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import com.vincenthuto.hemomancy.common.rite.harbinger.AlembicUpgradeRites;
import com.vincenthuto.hemomancy.common.rite.harbinger.ResonantForgeUpgradeRites;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.ResonantForgeBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder("scriptorium_validation")
@PrefixGameTestTemplate(false)
public final class ResonantForgeGameTests {
    @GameTest(template = "empty", timeoutTicks = 100, batch = "resonant_forge")
    public static void everyScriptoriumCurseAndOverlevelRoundTrips(GameTestHelper helper) {
        var registry = helper.getLevel().registryAccess();
        var unbreaking = registry.registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.UNBREAKING);
        Object[][] cases = {
                {Items.DIAMOND_PICKAXE, "sanguine_appetite"},
                {Items.DIAMOND_SWORD, "thirsting_edge"},
                {Items.BOW, "hemorrhagic_string"},
                {Items.DIAMOND_CHESTPLATE, "open_vessel"}
        };
        for (Object[] test : cases) {
            ItemStack source = new ItemStack((net.minecraft.world.item.Item) test[0]);
            source.enchant(unbreaking, 5);
            source.set(DataComponentInit.SCRIPTORIUM_PROVENANCE.get(),
                    new ScriptoriumProvenance((String) test[1], 3, 2));
            var capture = ResonantForgeTransfer.capture(source, registry, "", false);
            helper.assertTrue(capture.success() && capture.equipment().getEnchantments().isEmpty()
                    && !capture.equipment().has(DataComponentInit.SCRIPTORIUM_PROVENANCE.get()),
                    "Complete extraction did not clean " + test[1]);
            var applied = ResonantForgeTransfer.apply(new ItemStack((net.minecraft.world.item.Item) test[0]),
                    capture.pattern(), registry);
            var provenance = applied.equipment().get(DataComponentInit.SCRIPTORIUM_PROVENANCE.get());
            helper.assertTrue(applied.success() && applied.equipment().getEnchantments().getLevel(unbreaking) == 5
                    && provenance != null && provenance.curse().equals(test[1]) && provenance.severity() == 3
                    && provenance.excessLoad() == 2, "Pattern failed to round-trip " + test[1]);
        }

        var sharpness = registry.registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.SHARPNESS);
        var smite = registry.registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.SMITE);
        ItemStack precise = new ItemStack(Items.DIAMOND_SWORD);
        precise.enchant(sharpness, 6);
        precise.setDamageValue(37);
        precise.set(DataComponents.CUSTOM_NAME, Component.literal("Tuning Fork"));
        precise.set(DataComponents.REPAIR_COST, 11);
        precise.set(DataComponentInit.SCRIPTORIUM_PROVENANCE.get(),
                new ScriptoriumProvenance("thirsting_edge", 2, 1));
        var enchantOnly = ResonantForgeTransfer.capture(precise, registry, "minecraft:sharpness", false);
        var remainingCurse = enchantOnly.equipment().get(DataComponentInit.SCRIPTORIUM_PROVENANCE.get());
        helper.assertTrue(enchantOnly.success() && enchantOnly.pattern().curse().isBlank()
                && enchantOnly.pattern().excessLoad() == 1 && remainingCurse != null
                && remainingCurse.curse().equals("thirsting_edge") && enchantOnly.equipment().getDamageValue() == 37
                && enchantOnly.equipment().getOrDefault(DataComponents.REPAIR_COST, 0) == 11
                && Component.literal("Tuning Fork").equals(enchantOnly.equipment().get(DataComponents.CUSTOM_NAME)),
                "Selective extraction lost curse state, overlevel provenance, or unrelated components");
        var curseOnly = ResonantForgeTransfer.capture(precise, registry, ResonantForgeRules.CURSE_SELECTION, false);
        var remainingExcess = curseOnly.equipment().get(DataComponentInit.SCRIPTORIUM_PROVENANCE.get());
        helper.assertTrue(curseOnly.success() && curseOnly.pattern().enchantments().isEmpty()
                && curseOnly.pattern().curse().equals("thirsting_edge")
                && EnchantmentHelper.getEnchantmentsForCrafting(curseOnly.equipment()).getLevel(sharpness) == 6
                && remainingExcess != null && remainingExcess.curse().isBlank() && remainingExcess.excessLoad() == 1,
                "Curse extraction changed its independent overlevel enchantment");

        var binding = registry.registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.BINDING_CURSE);
        ItemStack boundArmor = new ItemStack(Items.DIAMOND_CHESTPLATE);
        boundArmor.enchant(binding, 1);
        var vanillaCurse = ResonantForgeTransfer.capture(boundArmor, registry, "", false);
        var rebound = ResonantForgeTransfer.apply(new ItemStack(Items.DIAMOND_CHESTPLATE), vanillaCurse.pattern(), registry);
        helper.assertTrue(vanillaCurse.success() && rebound.success()
                && EnchantmentHelper.getEnchantmentsForCrafting(rebound.equipment()).getLevel(binding) == 1,
                "Vanilla curse did not round-trip");

        ItemStack conflicting = new ItemStack(Items.DIAMOND_SWORD);
        conflicting.enchant(smite, 5);
        var sharpnessPattern = new ResonantPattern(List.of(
                new ResonantPattern.Entry("minecraft:sharpness", 5)), "", 0, 0, false);
        helper.assertTrue(ResonantForgeTransfer.apply(conflicting, sharpnessPattern, registry).failure()
                        == ResonantForgeTransfer.Failure.INCOMPATIBLE_ENCHANTMENTS,
                "Incompatible playback was not rejected atomically");
        helper.assertTrue(ResonantForgeTransfer.capture(new ItemStack(Items.ENCHANTED_BOOK), registry, "", false)
                .failure() == ResonantForgeTransfer.Failure.UNSUPPORTED_ITEM,
                "Forge accepted an enchanted book as equipment");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "resonant_forge")
    public static void operationsConsumeOrdinaryButRetainMasterCylinder(GameTestHelper helper) {
        ResonantForgeBlockEntity forge = placeForge(helper, new BlockPos(5, 3, 5), Direction.NORTH);
        var unbreaking = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.UNBREAKING);
        ItemStack source = new ItemStack(Items.DIAMOND_PICKAXE);
        source.enchant(unbreaking, 3);
        forge.receiveBlood(5000);
        forge.setItem(ResonantForgeBlockEntity.GRINDING_ITEM, source);
        ItemStack ancient = new ItemStack(ItemInit.ambergris_cylinder.get());
        ancient.set(DataComponentInit.ANCIENT_RECORDING.get(), "test_recording");
        forge.setItem(ResonantForgeBlockEntity.GRINDING_CYLINDER, ancient);
        helper.assertTrue(!forge.startGrinding(), "Forge overwrote an Ancient Recording cylinder");
        forge.removeItemNoUpdate(ResonantForgeBlockEntity.GRINDING_CYLINDER);
        forge.setItem(ResonantForgeBlockEntity.GRINDING_CYLINDER, new ItemStack(ItemInit.ambergris_cylinder.get()));
        helper.assertTrue(forge.startGrinding(), "Base forge rejected complete recording");
        helper.assertTrue(!forge.startGrinding(), "Concurrent request started a second operation");
        var active = forge.saveWithoutMetadata(helper.getLevel().registryAccess());
        var restoredActive = new ResonantForgeBlockEntity(forge.getBlockPos(), forge.getBlockState());
        restoredActive.loadWithComponents(active, helper.getLevel().registryAccess());
        helper.assertTrue(restoredActive.operation() == ResonantForgeBlockEntity.Operation.GRIND
                && restoredActive.getItem(ResonantForgeBlockEntity.GRINDING_ITEM).is(Items.DIAMOND_PICKAXE),
                "Active operation did not survive serialization");
        helper.assertTrue(forge.cancelOperation() && forge.getBloodVolume() == 5000
                && forge.wheelUses() == 0 && !forge.getItem(ResonantForgeBlockEntity.GRINDING_ITEM).isEmpty(),
                "Cancellation did not return reserved blood and inputs");
        helper.assertTrue(forge.startGrinding(), "Canceled recording could not restart");
        tick(forge, helper, ResonantForgeRules.OPERATION_TICKS);
        ItemStack cylinder = forge.removeItemNoUpdate(ResonantForgeBlockEntity.GRINDING_CYLINDER_OUTPUT);
        helper.assertTrue(cylinder.has(DataComponentInit.RESONANT_PATTERN.get()), "Recording produced no pattern");
        forge.setItem(ResonantForgeBlockEntity.APPLICATION_ITEM, new ItemStack(Items.DIAMOND_PICKAXE));
        forge.setItem(ResonantForgeBlockEntity.APPLICATION_CYLINDER, cylinder);
        helper.assertTrue(forge.startApply(), "Ordinary playback did not start");
        tick(forge, helper, ResonantForgeRules.OPERATION_TICKS);
        helper.assertTrue(forge.getItem(ResonantForgeBlockEntity.APPLICATION_CYLINDER).isEmpty(),
                "Ordinary playback kept its cylinder");

        forge.removeItemNoUpdate(ResonantForgeBlockEntity.APPLICATION_OUTPUT);
        ItemStack prematureMaster = new ItemStack(ItemInit.ambergris_cylinder.get());
        prematureMaster.set(DataComponentInit.RESONANT_PATTERN.get(),
                new ResonantPattern(List.of(new ResonantPattern.Entry("minecraft:unbreaking", 3)), "", 0, 0, true));
        forge.setItem(ResonantForgeBlockEntity.APPLICATION_ITEM, new ItemStack(Items.DIAMOND_PICKAXE));
        forge.setItem(ResonantForgeBlockEntity.APPLICATION_CYLINDER, prematureMaster);
        helper.assertTrue(!forge.startApply(), "D3 forge accepted master playback");
        forge.removeItemNoUpdate(ResonantForgeBlockEntity.APPLICATION_ITEM);
        forge.removeItemNoUpdate(ResonantForgeBlockEntity.APPLICATION_CYLINDER);

        helper.assertTrue(forge.completeUpgrade(ResonantForgeTier.PRECISION, UUID.randomUUID())
                && forge.completeUpgrade(ResonantForgeTier.MASTERWORK, UUID.randomUUID()), "Tier setup failed");
        forge.receiveBlood(5000);
        ItemStack ordinary = new ItemStack(ItemInit.ambergris_cylinder.get());
        ordinary.set(DataComponentInit.RESONANT_PATTERN.get(),
                new ResonantPattern(List.of(new ResonantPattern.Entry("minecraft:unbreaking", 3)), "", 0, 0, false));
        forge.setItem(ResonantForgeBlockEntity.GRINDING_CYLINDER, ordinary);
        helper.assertTrue(forge.startStabilizing(), "D7 forge rejected ordinary stabilization");
        tick(forge, helper, ResonantForgeRules.MASTER_OPERATION_TICKS);
        ItemStack master = forge.removeItemNoUpdate(ResonantForgeBlockEntity.GRINDING_CYLINDER_OUTPUT);
        helper.assertTrue(master.getCount() == 1
                && master.get(DataComponentInit.RESONANT_PATTERN.get()) != null
                && master.get(DataComponentInit.RESONANT_PATTERN.get()).master()
                && forge.getItem(ResonantForgeBlockEntity.GRINDING_CYLINDER).isEmpty(),
                "Stabilization duplicated or failed to preserve the recording");
        forge.setItem(ResonantForgeBlockEntity.APPLICATION_ITEM, new ItemStack(Items.DIAMOND_PICKAXE));
        forge.setItem(ResonantForgeBlockEntity.APPLICATION_CYLINDER, master);
        helper.assertTrue(forge.startApply(), "Master playback did not start");
        tick(forge, helper, ResonantForgeRules.OPERATION_TICKS);
        helper.assertTrue(!forge.getItem(ResonantForgeBlockEntity.APPLICATION_CYLINDER).isEmpty(),
                "Master playback consumed its cylinder");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "resonant_forge")
    public static void maintenanceProgressPersistsAndWheelRepairSpendsTool(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(5, 3, 5));
        ResonantForgeBlockEntity forge = placeForge(helper, new BlockPos(5, 3, 5), Direction.NORTH);
        var tag = forge.saveWithoutMetadata(helper.getLevel().registryAccess());
        tag.putInt("HammerUses", ResonantForgeRules.HAMMER_SERVICE_INTERVAL);
        tag.putInt("WheelUses", ResonantForgeRules.WHEEL_SERVICE_INTERVAL);
        forge.loadWithComponents(tag, helper.getLevel().registryAccess());
        ItemStack iron = new ItemStack(BlockInit.hematic_iron_block.get());
        ItemStack smouldering = new ItemStack(BlockInit.smouldering_ash_trail.get());
        helper.assertTrue(forge.depositHammerIron(iron) && forge.depositHammerAsh(smouldering),
                "Hammer materials were rejected");
        helper.assertTrue(forge.receiveHammerRepairBlood(200) == 200 && forge.hammerWorn(),
                "Partial hammer blood did not persist as partial work");
        var saved = forge.saveWithoutMetadata(helper.getLevel().registryAccess());
        var restored = new ResonantForgeBlockEntity(pos, forge.getBlockState());
        restored.loadWithComponents(saved, helper.getLevel().registryAccess());
        helper.assertTrue(restored.hammerRepairBlood() == 200
                && restored.receiveHammerRepairBlood(300) == 300 && !restored.hammerWorn(),
                "Reload lost or mischarged hammer repair blood");

        ItemStack ash = new ItemStack(BlockInit.befouling_ash_trail.get());
        ItemStack scalpel = new ItemStack(ItemInit.vivianite_scalpel.get());
        ServerPlayer player = player(helper);
        helper.assertTrue(restored.depositWheelAsh(ash)
                && restored.redressWheel(player, InteractionHand.MAIN_HAND, scalpel), "Wheel repair failed");
        helper.assertTrue(!restored.wheelWorn() && scalpel.getDamageValue() == ResonantForgeRules.SCALPEL_REPAIR_DAMAGE,
                "Wheel repair spent the wrong durability");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "resonant_forge")
    public static void precisionRiteUsesTwoFullStagesAndPreservesMachineState(GameTestHelper helper) {
        ActiveCardinalRite rite = upgradeRite(helper, false);
        ResonantForgeBlockEntity forge = (ResonantForgeBlockEntity) helper.getLevel()
                .getBlockEntity(ResonantForgeUpgradeRites.seat(rite));
        forge.receiveBlood(1234);
        forge.setItem(ResonantForgeBlockEntity.APPLICATION_ITEM, new ItemStack(Items.DIAMOND_PICKAXE));
        UUID identity = forge.machineIdentity();
        helper.assertTrue(AlembicUpgradeRites.prepare(helper.getLevel(), rite), "Precision rite preparation failed");
        for (int i = 0; i < rite.getAnchorBloodMl().length; i++) rite.fillAnchor(i, 50);
        helper.assertTrue(rite.enterInscription() && rite.sealAltar(false)
                && rite.getPhase() == CardinalRitePhase.ALEMBIC_PROJECTION, "Rite did not enter projection");
        helper.assertTrue(rite.alembicBloodNeeded() == 250 && !rite.fillAlembicProjection(1, 250),
                "Rite accepted the wrong stage");
        helper.assertTrue(rite.fillAlembicProjection(0, 250) && rite.fillAlembicProjection(1, 250),
                "Rite rejected a 250 mL stage");
        rite.markComplete();
        helper.assertTrue(AlembicUpgradeRites.complete(helper.getLevel(), rite), "Precision upgrade failed");
        helper.assertTrue(forge.tier() == ResonantForgeTier.PRECISION && forge.machineIdentity().equals(identity)
                && forge.getBloodVolume() == 1234 && forge.getItem(ResonantForgeBlockEntity.APPLICATION_ITEM)
                .is(Items.DIAMOND_PICKAXE), "Upgrade changed machine state");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "resonant_forge")
    public static void interruptedRiteUnlocksForgeAndReturnsEscrowOnce(GameTestHelper helper) {
        ActiveCardinalRite rite = upgradeRite(helper, false);
        ResonantForgeBlockEntity forge = (ResonantForgeBlockEntity) helper.getLevel()
                .getBlockEntity(ResonantForgeUpgradeRites.seat(rite));
        helper.assertTrue(AlembicUpgradeRites.prepare(helper.getLevel(), rite) && forge.isRiteLocked(),
                "Rite did not lock its subject");
        AlembicUpgradeRites.recover(helper.getLevel(), rite);
        helper.assertTrue(!forge.isRiteLocked() && forge.tier() == ResonantForgeTier.BASE
                && rite.alembic().getString("EscrowState").equals("RETURNED"), "Recovery changed the forge");
        AlembicUpgradeRites.recover(helper.getLevel(), rite);
        helper.assertTrue(rite.alembic().getString("EscrowState").equals("RETURNED"),
                "Recovery returned escrow twice");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "resonant_forge")
    public static void masterRiteRequiresThreeOrderedStages(GameTestHelper helper) {
        ActiveCardinalRite rite = upgradeRite(helper, true);
        ResonantForgeBlockEntity forge = (ResonantForgeBlockEntity) helper.getLevel()
                .getBlockEntity(ResonantForgeUpgradeRites.seat(rite));
        helper.assertTrue(AlembicUpgradeRites.prepare(helper.getLevel(), rite), "Master rite preparation failed");
        for (int i = 0; i < rite.getAnchorBloodMl().length; i++) rite.fillAnchor(i, 50);
        helper.assertTrue(rite.enterInscription() && rite.sealAltar(false), "Master rite did not seal");
        helper.assertTrue(rite.fillAlembicProjection(0, 250) && rite.fillAlembicProjection(1, 250)
                && rite.fillAlembicProjection(2, 250), "Master rite rejected an ordered 250 mL stage");
        var expectedSnapshot = rite.alembic().getCompound("SubjectSnapshot");
        var actualSnapshot = forge.upgradeSnapshot(helper.getLevel().registryAccess());
        helper.assertTrue(expectedSnapshot.equals(actualSnapshot), "Master snapshot changed: expected="
                + expectedSnapshot + ", actual=" + actualSnapshot);
        rite.markComplete();
        boolean completed = AlembicUpgradeRites.complete(helper.getLevel(), rite);
        helper.assertTrue(completed, "Master completion refused: phase=" + rite.getPhase()
                + ", stage=" + rite.alembic().getInt("Stage") + ", escrow="
                + rite.alembic().getString("EscrowState") + ", tier=" + forge.tier());
        helper.assertTrue(forge.tier() == ResonantForgeTier.MASTERWORK,
                "Master completion kept tier " + forge.tier());
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "resonant_forge")
    public static void artificerLessonsGateOneTimeKitsAndRetryFullInventory(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        var degree = HemoCapabilityAccess.requireInitiatoryDegree(player);
        var progress = HemoCapabilityAccess.resonantForge(player);
        degree.setDegreeNumber(3);
        helper.assertTrue(progress.teach(player) && !progress.teach(player), "D3 lesson was not one-time");
        degree.setDegreeNumber(5);
        helper.assertTrue(progress.claimPrecision(player) && !progress.claimPrecision(player)
                && player.getInventory().contains(new ItemStack(ItemInit.precision_governor_kit.get())),
                "D5 kit gate or claim failed");
        for (int i = 0; i < player.getInventory().items.size(); i++)
            player.getInventory().items.set(i, new ItemStack(Items.STONE, 64));
        degree.setDegreeNumber(7);
        helper.assertTrue(progress.claimMaster(player), "D7 kit claim was rejected");
        helper.assertTrue(!player.getInventory().contains(new ItemStack(ItemInit.master_cam_kit.get())),
                "Full inventory accepted the pending kit");
        player.getInventory().items.set(0, ItemStack.EMPTY);
        progress.deliver(player);
        helper.assertTrue(player.getInventory().contains(new ItemStack(ItemInit.master_cam_kit.get())),
                "Pending D7 kit was not retried");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "resonant_forge")
    public static void everyOrientationBuildsFourBloodLinkedFillers(GameTestHelper helper) {
        Direction[] facings = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        BlockPos[] positions = {new BlockPos(3, 3, 3), new BlockPos(7, 3, 3),
                new BlockPos(3, 3, 8), new BlockPos(7, 3, 8)};
        for (int i = 0; i < facings.length; i++) {
            helper.getLevel().setBlockAndUpdate(helper.absolutePos(positions[i]).above(),
                    net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            ResonantForgeBlockEntity forge = placeForge(helper, positions[i], facings[i]);
            helper.assertTrue(ResonantForgeBlock.hasCompleteStructure(helper.getLevel(), forge.getBlockPos(),
                    forge.getBlockState()), "Incomplete structure facing " + facings[i]);
            forge.receiveBlood(321);
            int fillers = 0;
            for (BlockPos pos : BlockPos.betweenClosed(forge.getBlockPos().offset(-1, 0, -1),
                    forge.getBlockPos().offset(1, 1, 1))) {
                if (helper.getLevel().getBlockEntity(pos) instanceof com.vincenthuto.hemomancy.common.tile.shared.FillerBlockEntity filler) {
                    fillers++;
                    helper.assertTrue(filler.getBloodVolume() == 321, "Filler did not resolve controller blood");
                }
            }
            helper.assertTrue(fillers == 4, "Wrong filler count facing " + facings[i]);
            helper.assertTrue(helper.getLevel().getBlockState(forge.getBlockPos().above()).isAir(),
                    "Basin headroom is occupied");
            var headroom = net.minecraft.world.level.block.Block.box(0, 16, 0, 16, 32, 16);
            helper.assertTrue(!net.minecraft.world.phys.shapes.Shapes.joinIsNotEmpty(
                    forge.getBlockState().getCollisionShape(helper.getLevel(), forge.getBlockPos()),
                    headroom, net.minecraft.world.phys.shapes.BooleanOp.AND), "Basin headroom has collision");
            var above = forge.getBlockPos().above();
            helper.getLevel().setBlockAndUpdate(above, BlockInit.filler_block.get().defaultBlockState());
            var legacy = (com.vincenthuto.hemomancy.common.tile.shared.FillerBlockEntity)
                    helper.getLevel().getBlockEntity(above);
            legacy.setMainBlockPos(forge.getBlockPos());
            ResonantForgeBlockEntity.serverTick(helper.getLevel(), forge.getBlockPos(), forge.getBlockState(), forge);
            helper.assertTrue(helper.getLevel().getBlockState(above).isAir(), "Legacy filler was not removed");
            helper.assertTrue(helper.getLevel().getBlockEntity(forge.getBlockPos()) == forge
                    && forge.getBloodVolume() == 321, "Legacy cleanup damaged the forge");
        }
        helper.succeed();
    }

    private static ActiveCardinalRite upgradeRite(GameTestHelper helper, boolean master) {
        BlockPos center = helper.absolutePos(new BlockPos(5, 3, 5));
        ActiveCardinalRite rite = ActiveCardinalRite.interactive(UUID.randomUUID(), center,
                Hemomancy.rloc(master ? "cardinal_rite/enduring_pattern" : "cardinal_rite/true_groove"),
                2400, master ? 7 : 5, master ? 7 : 5, false, 0, master ? 8 : 4);
        rite.setMatchedFloor(Hemomancy.rloc("working_greater"), Direction.NORTH, Direction.UP);
        helper.getLevel().setBlockAndUpdate(center, BlockInit.cardinal_focus.get().defaultBlockState());
        ResonantForgeBlockEntity forge = placeForgeAt(helper, ResonantForgeUpgradeRites.seat(rite), Direction.NORTH);
        if (master) forge.completeUpgrade(ResonantForgeTier.PRECISION, UUID.randomUUID());
        BlockPos brazierPos = center.offset(3, 0, 3);
        helper.getLevel().setBlockAndUpdate(brazierPos, BlockInit.iron_brazier.get().defaultBlockState());
        IronBrazierBlockEntity brazier = (IronBrazierBlockEntity) helper.getLevel().getBlockEntity(brazierPos);
        ItemStack kit = new ItemStack(master ? ItemInit.master_cam_kit.get() : ItemInit.precision_governor_kit.get());
        brazier.insertOffering(null, kit.copy());
        rite.captureOfferingItinerary(List.of(new ActiveCardinalRite.RiteOffering(brazierPos, kit, true)));
        return rite;
    }

    private static ResonantForgeBlockEntity placeForge(GameTestHelper helper, BlockPos relative, Direction facing) {
        return placeForgeAt(helper, helper.absolutePos(relative), facing);
    }

    private static ResonantForgeBlockEntity placeForgeAt(GameTestHelper helper, BlockPos pos, Direction facing) {
        var state = BlockInit.resonant_forge.get().defaultBlockState().setValue(ResonantForgeBlock.FACING, facing);
        helper.getLevel().setBlockAndUpdate(pos, state);
        BlockInit.resonant_forge.get().setPlacedBy(helper.getLevel(), pos, state, null,
                new ItemStack(BlockInit.resonant_forge.get()));
        return (ResonantForgeBlockEntity) helper.getLevel().getBlockEntity(pos);
    }

    private static void tick(ResonantForgeBlockEntity forge, GameTestHelper helper, int ticks) {
        for (int i = 0; i <= ticks; i++) ResonantForgeBlockEntity.serverTick(helper.getLevel(), forge.getBlockPos(),
                forge.getBlockState(), forge);
    }

    private static ServerPlayer player(GameTestHelper helper) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "forge-test"), false);
        var player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
                cookie.gameProfile(), cookie.clientInformation());
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(player.server, connection, player, cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
        };
        player.setGameMode(GameType.SURVIVAL);
        player.setPos(helper.absolutePos(new BlockPos(5, 3, 5)).getCenter());
        return player;
    }
}
