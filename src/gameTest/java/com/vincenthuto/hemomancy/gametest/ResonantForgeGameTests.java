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
import net.minecraft.world.inventory.SimpleContainerData;
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
    public static void shiftClickSplitsBlankCylinderStacks(GameTestHelper helper) {
        ResonantForgeBlockEntity forge = placeForge(helper, new BlockPos(5, 3, 5), Direction.NORTH);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(forge.getBlockPos().getCenter());
        var menu = new com.vincenthuto.hemomancy.common.menu.tile.crafting.ResonantForgeMenu(
                1, player.getInventory(), forge, new SimpleContainerData(10));
        for (var medium : List.of(ItemInit.wax_cylinder.get(), ItemInit.ambergris_cylinder.get())) {
            player.getInventory().setItem(9, new ItemStack(medium, 2));
            helper.assertTrue(!menu.quickMoveStack(player, ResonantForgeBlockEntity.SLOT_COUNT).isEmpty(),
                    "Shift-click rejected a stack of two blank cylinders");
            helper.assertTrue(forge.getItem(ResonantForgeBlockEntity.GRINDING_CYLINDER).is(medium)
                            && forge.getItem(ResonantForgeBlockEntity.GRINDING_CYLINDER).getCount() == 1
                            && player.getInventory().getItem(9).getCount() == 1
                            && forge.getItem(ResonantForgeBlockEntity.APPLICATION_CYLINDER).isEmpty(),
                    "Shift-click must insert one blank cylinder and retain the remainder");
            helper.assertTrue(menu.quickMoveStack(player, ResonantForgeBlockEntity.SLOT_COUNT).isEmpty()
                            && player.getInventory().getItem(9).getCount() == 1
                            && forge.getItem(ResonantForgeBlockEntity.APPLICATION_CYLINDER).isEmpty(),
                    "An occupied grinder must not route the remaining blank to application");
            forge.removeItemNoUpdate(ResonantForgeBlockEntity.GRINDING_CYLINDER);
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "resonant_forge")
    public static void shiftClickPreservesOccupiedCylinderStacks(GameTestHelper helper) {
        ResonantForgeBlockEntity forge = placeForge(helper, new BlockPos(5, 3, 5), Direction.NORTH);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(forge.getBlockPos().getCenter());
        var menu = new com.vincenthuto.hemomancy.common.menu.tile.crafting.ResonantForgeMenu(
                1, player.getInventory(), forge, new SimpleContainerData(10));
        for (var medium : List.of(ItemInit.wax_cylinder.get(), ItemInit.ambergris_cylinder.get())) {
            ItemStack audio = new ItemStack(medium, 2);
            audio.set(DataComponentInit.CLAIRAUDIOGRAPH_RECORDING.get(),
                    new com.vincenthuto.hemomancy.common.item.component.ClairaudiographRecording(
                            "minecraft:cow", "minecraft:entity.cow.ambient", "ambient", 1F));
            ItemStack ancient = new ItemStack(medium, 2);
            ancient.set(DataComponentInit.ANCIENT_RECORDING.get(), "test_recording");
            for (ItemStack recorded : List.of(audio, ancient)) {
                player.getInventory().setItem(9, recorded.copy());
                helper.assertTrue(menu.quickMoveStack(player, ResonantForgeBlockEntity.SLOT_COUNT).isEmpty()
                                && ItemStack.matches(player.getInventory().getItem(9), recorded)
                                && forge.getItem(ResonantForgeBlockEntity.GRINDING_CYLINDER).isEmpty()
                                && forge.getItem(ResonantForgeBlockEntity.APPLICATION_CYLINDER).isEmpty(),
                        "Shift-click treated an occupied recording stack as blank");
            }
            ItemStack patterned = new ItemStack(medium, 2);
            patterned.set(DataComponentInit.RESONANT_PATTERN.get(), new ResonantPattern(
                    List.of(new ResonantPattern.Entry("minecraft:unbreaking", 3)), "", 0, 0, false));
            player.getInventory().setItem(9, patterned.copy());
            helper.assertTrue(!menu.quickMoveStack(player, ResonantForgeBlockEntity.SLOT_COUNT).isEmpty()
                            && ItemStack.matches(forge.getItem(ResonantForgeBlockEntity.APPLICATION_CYLINDER),
                                    patterned.copyWithCount(1))
                            && ItemStack.matches(player.getInventory().getItem(9), patterned.copyWithCount(1))
                            && forge.getItem(ResonantForgeBlockEntity.GRINDING_CYLINDER).isEmpty(),
                    "Shift-click lost a pattern or routed it as a blank cylinder");
            forge.removeItemNoUpdate(ResonantForgeBlockEntity.APPLICATION_CYLINDER);
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "resonant_forge")
    public static void shiftClickRoutesBlankWaxToGrinding(GameTestHelper helper) {
        ResonantForgeBlockEntity forge = placeForge(helper, new BlockPos(5, 3, 5), Direction.NORTH);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(forge.getBlockPos().getCenter());
        var menu = new com.vincenthuto.hemomancy.common.menu.tile.crafting.ResonantForgeMenu(
                1, player.getInventory(), forge, new SimpleContainerData(10));
        player.getInventory().setItem(9, new ItemStack(ItemInit.wax_cylinder.get()));
        helper.assertTrue(!menu.quickMoveStack(player, ResonantForgeBlockEntity.SLOT_COUNT).isEmpty()
                && forge.getItem(ResonantForgeBlockEntity.GRINDING_CYLINDER).is(ItemInit.wax_cylinder.get())
                && forge.getItem(ResonantForgeBlockEntity.APPLICATION_CYLINDER).isEmpty(),
                "Shift-click put blank wax on the application side");
        player.getInventory().setItem(10, new ItemStack(ItemInit.wax_cylinder.get()));
        helper.assertTrue(menu.quickMoveStack(player, ResonantForgeBlockEntity.SLOT_COUNT + 1).isEmpty()
                && forge.getItem(ResonantForgeBlockEntity.APPLICATION_CYLINDER).isEmpty(),
                "Blank wax fell back to the application side when grinding was occupied");
        helper.succeed();
    }
    @GameTest(template = "empty", timeoutTicks = 100, batch = "resonant_forge")
    public static void waxHandlesOrdinaryPatternsButCannotBecomeMaster(GameTestHelper helper) {
        ResonantForgeBlockEntity forge = placeForge(helper, new BlockPos(5, 3, 5), Direction.NORTH);
        var unbreaking = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.UNBREAKING);
        ItemStack source = new ItemStack(Items.DIAMOND_PICKAXE);
        source.enchant(unbreaking, 3);
        forge.receiveBlood(5000);
        forge.setItem(ResonantForgeBlockEntity.GRINDING_ITEM, source);
        forge.setItem(ResonantForgeBlockEntity.GRINDING_CYLINDER, new ItemStack(ItemInit.wax_cylinder.get()));
        helper.assertTrue(forge.startGrinding(), "Base Forge rejected wax for ordinary grinding");
        tick(forge, helper, ResonantForgeRules.OPERATION_TICKS);
        ItemStack wax = forge.removeItemNoUpdate(ResonantForgeBlockEntity.GRINDING_CYLINDER_OUTPUT);
        helper.assertTrue(wax.is(ItemInit.wax_cylinder.get()) && wax.has(DataComponentInit.RESONANT_PATTERN.get()),
                "Ordinary pattern was not recorded on wax");
        forge.setItem(ResonantForgeBlockEntity.APPLICATION_ITEM, new ItemStack(Items.DIAMOND_PICKAXE));
        forge.setItem(ResonantForgeBlockEntity.APPLICATION_CYLINDER, wax);
        helper.assertTrue(forge.startApply(), "Base Forge rejected wax pattern application");
        tick(forge, helper, ResonantForgeRules.OPERATION_TICKS);
        helper.assertTrue(forge.getItem(ResonantForgeBlockEntity.APPLICATION_CYLINDER).isEmpty(),
                "Ordinary wax was not consumed on application");

        forge.removeItemNoUpdate(ResonantForgeBlockEntity.APPLICATION_OUTPUT);
        helper.assertTrue(forge.completeUpgrade(ResonantForgeTier.PRECISION, UUID.randomUUID())
                && forge.completeUpgrade(ResonantForgeTier.MASTERWORK, UUID.randomUUID()), "Masterwork setup failed");
        forge.setItem(ResonantForgeBlockEntity.GRINDING_CYLINDER, new ItemStack(ItemInit.wax_cylinder.get()));
        forge.setItem(ResonantForgeBlockEntity.GRINDING_ITEM, source.copy());
        helper.assertTrue(forge.toggleMasterMode(), "Master mode did not become available");
        double bloodBefore = forge.getBloodVolume();
        helper.assertTrue(!forge.startGrinding() && forge.getBloodVolume() == bloodBefore
                && forge.getItem(ResonantForgeBlockEntity.GRINDING_ITEM).is(Items.DIAMOND_PICKAXE),
                "Wax master capture changed inputs or blood");
        forge.removeItemNoUpdate(ResonantForgeBlockEntity.GRINDING_ITEM);
        ItemStack ordinaryWax = new ItemStack(ItemInit.wax_cylinder.get());
        ordinaryWax.set(DataComponentInit.RESONANT_PATTERN.get(),
                new ResonantPattern(List.of(new ResonantPattern.Entry("minecraft:unbreaking", 3)), "", 0, 0, false));
        forge.setItem(ResonantForgeBlockEntity.GRINDING_CYLINDER, ordinaryWax);
        helper.assertTrue(!forge.startStabilizing() && forge.getBloodVolume() == bloodBefore,
                "Wax stabilization changed inputs or blood");
        forge.removeItemNoUpdate(ResonantForgeBlockEntity.GRINDING_CYLINDER);
        ItemStack forgedMasterWax = new ItemStack(ItemInit.wax_cylinder.get());
        forgedMasterWax.set(DataComponentInit.RESONANT_PATTERN.get(),
                new ResonantPattern(List.of(new ResonantPattern.Entry("minecraft:unbreaking", 3)), "", 0, 0, true));
        forge.setItem(ResonantForgeBlockEntity.APPLICATION_ITEM, new ItemStack(Items.DIAMOND_PICKAXE));
        forge.setItem(ResonantForgeBlockEntity.APPLICATION_CYLINDER, forgedMasterWax);
        helper.assertTrue(!forge.startApply() && forge.getBloodVolume() == bloodBefore,
                "Forged master pattern on wax was accepted or spent blood");
        helper.succeed();
    }
    @GameTest(template = "empty", timeoutTicks = 100, batch = "resonant_forge")
    public static void waxGrindingPreservesInputsAcrossBlockedOutputAndCancellation(GameTestHelper helper) {
        ResonantForgeBlockEntity forge = placeForge(helper, new BlockPos(5, 3, 5), Direction.NORTH);
        var unbreaking = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.UNBREAKING);
        ItemStack source = new ItemStack(Items.DIAMOND_PICKAXE);
        source.enchant(unbreaking, 3);
        ItemStack wax = new ItemStack(ItemInit.wax_cylinder.get());
        forge.receiveBlood(5000);
        forge.setItem(ResonantForgeBlockEntity.GRINDING_ITEM, source);
        forge.setItem(ResonantForgeBlockEntity.GRINDING_CYLINDER, wax);
        forge.setItem(ResonantForgeBlockEntity.GRINDING_CYLINDER_OUTPUT, new ItemStack(Items.PAPER));
        double bloodBefore = forge.getBloodVolume();
        helper.assertTrue(!forge.startGrinding() && forge.getBloodVolume() == bloodBefore
                && ItemStack.matches(wax, forge.getItem(ResonantForgeBlockEntity.GRINDING_CYLINDER)),
                "Blocked wax output consumed blood or input");
        forge.removeItemNoUpdate(ResonantForgeBlockEntity.GRINDING_CYLINDER_OUTPUT);
        helper.assertTrue(forge.startGrinding(), "Wax grinding did not start after clearing output");
        forge.setItem(ResonantForgeBlockEntity.GRINDING_CYLINDER, new ItemStack(ItemInit.ambergris_cylinder.get()));
        helper.assertTrue(forge.removeItem(ResonantForgeBlockEntity.GRINDING_CYLINDER, 1).isEmpty()
                && ItemStack.matches(wax, forge.getItem(ResonantForgeBlockEntity.GRINDING_CYLINDER)),
                "Active wax input could be swapped or extracted");
        helper.assertTrue(forge.getSlotsForFace(Direction.UP).length == 0
                && !forge.canPlaceItemThroughFace(ResonantForgeBlockEntity.GRINDING_CYLINDER, wax, Direction.UP)
                && !forge.canTakeItemThroughFace(ResonantForgeBlockEntity.GRINDING_CYLINDER, wax, Direction.DOWN),
                "Forge automation exposed the cylinder slot");
        helper.assertTrue(forge.cancelOperation() && forge.getBloodVolume() == bloodBefore
                && ItemStack.matches(wax, forge.getItem(ResonantForgeBlockEntity.GRINDING_CYLINDER))
                && ItemStack.matches(source, forge.getItem(ResonantForgeBlockEntity.GRINDING_ITEM)),
                "Cancellation did not restore blood and wax inputs");
        helper.succeed();
    }
    @GameTest(template = "empty", timeoutTicks = 100, batch = "resonant_forge")
    public static void ordinaryCylinderOperationsResumeAfterSerialization(GameTestHelper helper) {
        var registry = helper.getLevel().registryAccess();
        var unbreaking = registry.registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.UNBREAKING);
        for (var medium : List.of(ItemInit.wax_cylinder.get(), ItemInit.ambergris_cylinder.get())) {
            helper.setBlock(new BlockPos(5, 3, 5), net.minecraft.world.level.block.Blocks.AIR);
            ResonantForgeBlockEntity forge = placeForge(helper, new BlockPos(5, 3, 5), Direction.NORTH);
            ItemStack source = new ItemStack(Items.DIAMOND_PICKAXE);
            source.enchant(unbreaking, 3);
            source.setDamageValue(27);
            source.set(DataComponents.CUSTOM_NAME, Component.literal("Remembered tool"));
            ItemStack cylinder = new ItemStack(medium);
            cylinder.set(DataComponents.CUSTOM_NAME, Component.literal("Remembered cylinder"));
            var expectedCapture = ResonantForgeTransfer.capture(source, registry, "", false);
            ItemStack expectedCylinder = cylinder.copy();
            expectedCylinder.set(DataComponentInit.RESONANT_PATTERN.get(), expectedCapture.pattern());
            forge.receiveBlood(5000);
            forge.setItem(ResonantForgeBlockEntity.GRINDING_ITEM, source.copy());
            forge.setItem(ResonantForgeBlockEntity.GRINDING_CYLINDER, cylinder.copy());
            helper.assertTrue(forge.startGrinding(), "Ordinary capture did not start");
            tick(forge, helper, 17);
            double paidBlood = forge.getBloodVolume();
            helper.assertTrue(paidBlood == 5000 - ResonantForgeRules.grindingCost(expectedCapture.pattern()),
                    "Ordinary capture charged the wrong blood cost");
            forge = reloadForge(helper, forge);
            helper.assertTrue(forge.progress() == 17 && forge.getBloodVolume() == paidBlood
                            && ItemStack.matches(source, forge.getItem(ResonantForgeBlockEntity.GRINDING_ITEM))
                            && ItemStack.matches(cylinder, forge.getItem(ResonantForgeBlockEntity.GRINDING_CYLINDER)),
                    "Reload changed paid capture progress, blood or input components");
            tick(forge, helper, ResonantForgeRules.OPERATION_TICKS - 18);
            helper.assertTrue(forge.getItem(ResonantForgeBlockEntity.GRINDING_CYLINDER_OUTPUT).isEmpty(),
                    "Reload completed capture before its remaining time elapsed");
            tick(forge, helper, 1);
            helper.assertTrue(forge.idle() && forge.getBloodVolume() == paidBlood && forge.wheelUses() == 1
                            && ItemStack.matches(expectedCapture.equipment(),
                                    forge.getItem(ResonantForgeBlockEntity.GRINDING_EQUIPMENT_OUTPUT))
                            && ItemStack.matches(expectedCylinder,
                                    forge.getItem(ResonantForgeBlockEntity.GRINDING_CYLINDER_OUTPUT))
                            && forge.getItem(ResonantForgeBlockEntity.GRINDING_ITEM).isEmpty()
                            && forge.getItem(ResonantForgeBlockEntity.GRINDING_CYLINDER).isEmpty(),
                    "Resumed capture changed components, charged twice or retained inputs");
            forge.removeItemNoUpdate(ResonantForgeBlockEntity.GRINDING_EQUIPMENT_OUTPUT);
            ItemStack recorded = forge.removeItemNoUpdate(ResonantForgeBlockEntity.GRINDING_CYLINDER_OUTPUT);
            ItemStack target = new ItemStack(Items.DIAMOND_PICKAXE);
            target.setDamageValue(41);
            target.set(DataComponents.CUSTOM_NAME, Component.literal("Receiving tool"));
            var expectedApply = ResonantForgeTransfer.apply(target, expectedCapture.pattern(), registry);
            forge.setItem(ResonantForgeBlockEntity.APPLICATION_ITEM, target.copy());
            forge.setItem(ResonantForgeBlockEntity.APPLICATION_CYLINDER, recorded.copy());
            double beforeApply = forge.getBloodVolume();
            helper.assertTrue(forge.startApply(), "Saved ordinary cylinder could not be applied");
            tick(forge, helper, 23);
            paidBlood = forge.getBloodVolume();
            helper.assertTrue(paidBlood == beforeApply - ResonantForgeRules.hammeringCost(expectedCapture.pattern()),
                    "Ordinary application charged the wrong blood cost");
            forge = reloadForge(helper, forge);
            helper.assertTrue(forge.progress() == 23 && forge.getBloodVolume() == paidBlood
                            && ItemStack.matches(target, forge.getItem(ResonantForgeBlockEntity.APPLICATION_ITEM))
                            && ItemStack.matches(recorded, forge.getItem(ResonantForgeBlockEntity.APPLICATION_CYLINDER)),
                    "Reload changed paid application progress, blood or input components");
            tick(forge, helper, ResonantForgeRules.OPERATION_TICKS - 24);
            helper.assertTrue(forge.getItem(ResonantForgeBlockEntity.APPLICATION_OUTPUT).isEmpty(),
                    "Reload completed application before its remaining time elapsed");
            tick(forge, helper, 1);
            helper.assertTrue(forge.idle() && forge.getBloodVolume() == paidBlood && forge.hammerUses() == 1
                            && ItemStack.matches(expectedApply.equipment(),
                                    forge.getItem(ResonantForgeBlockEntity.APPLICATION_OUTPUT))
                            && forge.getItem(ResonantForgeBlockEntity.APPLICATION_ITEM).isEmpty()
                            && forge.getItem(ResonantForgeBlockEntity.APPLICATION_CYLINDER).isEmpty(),
                    "Resumed application changed components, charged twice or kept ordinary inputs");
            forge.removeItemNoUpdate(ResonantForgeBlockEntity.APPLICATION_OUTPUT);
            tick(forge, helper, ResonantForgeRules.OPERATION_TICKS);
            forge = reloadForge(helper, forge);
            tick(forge, helper, ResonantForgeRules.OPERATION_TICKS);
            helper.assertTrue(forge.getItem(ResonantForgeBlockEntity.APPLICATION_OUTPUT).isEmpty()
                            && forge.getBloodVolume() == paidBlood && forge.hammerUses() == 1,
                    "Completed application replayed after output removal or reload");
        }
        helper.succeed();
    }

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
    public static void forgeFullCycleIsTaughtAtD4WhileD3AccessRemains(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        var degree = HemoCapabilityAccess.requireInitiatoryDegree(player);
        var artificer = com.vincenthuto.hemomancy.common.init.EntityInit.harbinger_artificer.get()
                .create(helper.getLevel());
        artificer.setPos(player.position());
        degree.setDegreeNumber(3);
        var d3 = artificer.progressionDialogue(player);
        helper.assertTrue(d3.getNode("resonant_forge") != null
                        && d3.getNode("resonant_forge").lines().contains("hemomancy.artificer.resonant_forge.early"),
                "D3 Forge access was removed or the full cycle was taught early");
        degree.setDegreeNumber(4);
        helper.assertTrue(artificer.progressionDialogue(player).getNode("resonant_forge").lines()
                        .contains("hemomancy.artificer.resonant_forge.lesson"),
                "The Artificer did not teach the full Forge cycle at D4");
        degree.setDegreeNumber(6);
        helper.assertTrue(artificer.progressionDialogue(player).getNode("resonant_forge").lines()
                        .contains("hemomancy.artificer.resonant_forge.practice"),
                "D6 practice did not build on the same Forge tier");
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

    private static ResonantForgeBlockEntity reloadForge(GameTestHelper helper, ResonantForgeBlockEntity forge) {
        var saved = forge.saveWithoutMetadata(helper.getLevel().registryAccess());
        var restored = new ResonantForgeBlockEntity(forge.getBlockPos(), forge.getBlockState());
        restored.loadWithComponents(saved, helper.getLevel().registryAccess());
        helper.getLevel().removeBlockEntity(forge.getBlockPos());
        helper.getLevel().setBlockEntity(restored);
        return restored;
    }

    private static void tick(ResonantForgeBlockEntity forge, GameTestHelper helper, int ticks) {
        for (int i = 0; i < ticks; i++) ResonantForgeBlockEntity.serverTick(helper.getLevel(), forge.getBlockPos(),
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
