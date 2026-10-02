package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.block.harbinger.crafting.MycelialCrucibleBlock;
import com.vincenthuto.hemomancy.common.event.MachineAccessEvents;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.crafting.MycelialCrucibleBlockItem;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.MycelialCrucibleBlockEntity;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.stats.Stats;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.UUID;

@GameTestHolder("registration_validation")
@PrefixGameTestTemplate(false)
public final class RegistrationGameTests {
    @GameTest(template = "empty")
    public static void corticalDriftRegistersBorerAndItsCableHabitat(GameTestHelper helper) {
        var type = com.vincenthuto.hemomancy.common.init.EntityInit.myelin_borer.get();
        helper.assertTrue(BuiltInRegistries.ENTITY_TYPE.getKey(type).equals(
                        com.vincenthuto.hemomancy.Hemomancy.rloc("myelin_borer")),
                "Active Borer must retain its registered entity ID");
        var borer = type.create(helper.getLevel());
        helper.assertTrue(borer != null, "Registered Borer must be constructible");
        try {
            helper.assertTrue(borer.getMaxHealth() > 0 && borer.getAttributeValue(
                            net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED) > 0,
                    "Registered Borer must have living movement attributes");
            var egg = ItemInit.spawn_egg_myelin_borer.get();
            helper.assertTrue(egg.getType(egg.getDefaultInstance()) == type,
                    "Borer spawn egg must resolve to the active registered type");
            var biome = helper.getLevel().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
                    .get(com.vincenthuto.hemomancy.Hemomancy.rloc("cortical_drift"));
            helper.assertTrue(biome != null, "Cortical Drift biome must load");
            var monsters = biome.getMobSettings().getMobs(net.minecraft.world.entity.MobCategory.MONSTER).unwrap();
            var ownedMonsters = monsters.stream().filter(entry -> BuiltInRegistries.ENTITY_TYPE.getKey(entry.type)
                    .getNamespace().equals(com.vincenthuto.hemomancy.Hemomancy.MOD_ID)).toList();
            helper.assertTrue(ownedMonsters.size() == 1 && ownedMonsters.getFirst().type == type
                            && ownedMonsters.getFirst().getWeight().asInt() == 8
                            && ownedMonsters.getFirst().minCount == 1 && ownedMonsters.getFirst().maxCount == 2,
                    "Loaded Cortical Drift must keep its authored Hemomancy Borer-only spawn entry; found="
                            + monsters.stream().map(entry -> BuiltInRegistries.ENTITY_TYPE.getKey(entry.type)
                                    + ":weight=" + entry.getWeight().asInt() + ",min=" + entry.minCount
                                    + ",max=" + entry.maxCount).toList());
            var bundle = BlockInit.nerve_bundle.get();
            helper.assertTrue(bundle.asItem() != Items.AIR && bundle.defaultBlockState().is(
                            net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,
                                    com.vincenthuto.hemomancy.Hemomancy.rloc("cortical_crawlable"))),
                    "Active Nerve Bundle must retain its item and loaded Borer habitat tag");
            helper.succeed();
        } finally {
            borer.discard();
        }
    }

    @GameTest(template = "empty")
    public static void legacyDictationTableInheritsSharedWaterlogging(GameTestHelper helper) {
        var level = helper.getLevel();
        var block = BlockInit.dictation_table.get();
        var waterlogged = net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;
        helper.assertTrue(block instanceof com.vincenthuto.hutoslib.common.block.DictationTableBlock
                        && block instanceof net.minecraft.world.level.block.SimpleWaterloggedBlock,
                "Legacy Dictation Table must inherit the shared waterloggable block");
        helper.assertTrue(block.defaultBlockState().hasProperty(waterlogged)
                        && !block.defaultBlockState().getValue(waterlogged),
                "Inherited Dictation Table state must start dry");
        BlockPos pos = helper.absolutePos(new BlockPos(4, 3, 4));
        BlockPos tickProbe = pos.east(2);
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "dictation-water"));
        player.setGameMode(GameType.SURVIVAL);
        player.setPos(pos.getCenter().add(3, 0, 0));
        var stack = new ItemStack(block.asItem());
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, stack);
        try {
            level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.WATER.defaultBlockState());
            var context = new net.minecraft.world.item.context.BlockPlaceContext(player,
                    net.minecraft.world.InteractionHand.MAIN_HAND, stack,
                    new net.minecraft.world.phys.BlockHitResult(pos.getCenter(), net.minecraft.core.Direction.UP, pos, false));
            helper.assertTrue(((net.minecraft.world.item.BlockItem) stack.getItem()).place(context).consumesAction(),
                    "Legacy Dictation Table item must place into water");
            var state = level.getBlockState(pos);
            helper.assertTrue(state.is(block) && state.getValue(waterlogged)
                            && state.getFluidState().getType() == net.minecraft.world.level.material.Fluids.WATER
                            && state.getFluidState().isSource(),
                    "Normal placement must retain source water in the inherited state");
            helper.assertTrue(level.getBlockEntity(pos) instanceof
                            com.vincenthuto.hemomancy.common.tile.inscription.DictationTableBlockEntity
                            && level.getBlockEntity(pos).getType() ==
                            com.vincenthuto.hemomancy.common.init.BlockEntityInit.dictation_table.get(),
                    "Waterlogged placement must retain the legacy block-entity type");
            level.setBlock(tickProbe, state, 2);
            helper.assertTrue(!level.getFluidTicks().hasScheduledTick(tickProbe, net.minecraft.world.level.material.Fluids.WATER),
                    "Isolated water tick probe must start without a scheduled tick");
            state.updateShape(net.minecraft.core.Direction.NORTH,
                    net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), level, tickProbe, tickProbe.north());
            helper.assertTrue(level.getFluidTicks().hasScheduledTick(tickProbe, net.minecraft.world.level.material.Fluids.WATER),
                    "Inherited neighbour update must schedule a water tick");
            var bucket = ((net.minecraft.world.level.block.SimpleWaterloggedBlock) block).pickupBlock(player, level, pos, state);
            helper.assertTrue(bucket.is(Items.WATER_BUCKET) && !level.getBlockState(pos).getValue(waterlogged)
                            && level.getFluidState(pos).isEmpty(),
                    "Bucket pickup must return water and leave the table dry");
            helper.succeed();
        } finally {
            level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(tickProbe, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            player.discard();
        }
    }

    @GameTest(template = "empty")
    public static void builtCreativeTabsPreservePlayerToolOrderAndSeparateDebugTools(GameTestHelper helper) {
        var main = com.vincenthuto.hemomancy.Hemomancy.hemomancytab.get();
        var wip = com.vincenthuto.hemomancy.Hemomancy.hemomancywiptab.get();
        var parameters = new net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters(
                helper.getLevel().enabledFeatures(), true, helper.getLevel().registryAccess());
        main.buildContents(parameters);
        wip.buildContents(parameters);
        assertItemsInOrder(helper, main.getDisplayItems(), ItemInit.liber_sanguinum.get(),
                ItemInit.harbinger_assignment_ledger.get(), ItemInit.blood_absorption.get(),
                ItemInit.blood_projection.get(), ItemInit.living_syringe.get(), ItemInit.morphling_jar.get(),
                ItemInit.vial_rack.get(), ItemInit.chitinite_arm_banner.get());
        var debugTools = java.util.List.of(ItemInit.structure_spawner.get(), ItemInit.structure_scanner.get(),
                ItemInit.debug_showcase.get());
        helper.assertTrue(main.getDisplayItems().stream().noneMatch(stack -> debugTools.contains(stack.getItem())),
                "Debug tools must not interrupt the main player-facing Creative tab");
        assertItemsInOrder(helper, wip.getDisplayItems(), ItemInit.structure_spawner.get(),
                ItemInit.structure_scanner.get(), ItemInit.debug_showcase.get());
        helper.succeed();
    }

    private static void assertItemsInOrder(GameTestHelper helper, java.util.Collection<ItemStack> stacks,
            net.minecraft.world.item.Item... expected) {
        var items = stacks.stream().map(ItemStack::getItem).toList();
        int previous = -1;
        for (var item : expected) {
            int index = items.indexOf(item);
            helper.assertTrue(index > previous, "Creative item missing or out of order: "
                    + BuiltInRegistries.ITEM.getKey(item) + ", index=" + index + ", previous=" + previous);
            previous = index;
        }
    }

    @GameTest(template = "empty")
    public static void builtCreativeTabsKeepTechnicalBlocksItemless(GameTestHelper helper) {
        var main = com.vincenthuto.hemomancy.Hemomancy.hemomancytab.get();
        var wip = com.vincenthuto.hemomancy.Hemomancy.hemomancywiptab.get();
        var parameters = new net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters(
                helper.getLevel().enabledFeatures(), true, helper.getLevel().registryAccess());
        try {
            main.buildContents(parameters);
            wip.buildContents(parameters);
            helper.assertTrue(!main.getDisplayItems().isEmpty() && !wip.getDisplayItems().isEmpty(),
                    "Creative tab event must populate both registered tabs");
            helper.assertTrue(java.util.stream.Stream.concat(main.getDisplayItems().stream(), wip.getDisplayItems().stream())
                    .noneMatch(ItemStack::isEmpty), "Creative tabs must not contain empty technical block placeholders");
            for (String path : java.util.List.of("attached_gourd_stem", "filler_block", "warp_chair_filler",
                    "abocipher_emitter", "qliphoth_bloom", "escharian_overgrowth", "escharian_overgrowth_rim")) {
                var id = com.vincenthuto.hemomancy.Hemomancy.rloc(path);
                helper.assertTrue(BuiltInRegistries.BLOCK.containsKey(id), "Technical block is missing: " + id);
                var decision = BlockInit.itemDecision(id);
                helper.assertTrue(BuiltInRegistries.BLOCK.get(id).asItem() == Items.AIR
                                && decision.itemRoute() == BlockInit.ItemRoute.NONE
                                && decision.creativeRoute() == BlockInit.CreativeRoute.HIDDEN,
                        "Technical block must remain itemless and hidden: " + id);
            }
            helper.assertTrue(main.getDisplayItems().stream().anyMatch(stack -> stack.is(BlockInit.venous_stone.get().asItem()))
                            && main.getDisplayItems().stream().anyMatch(stack -> stack.is(BlockInit.mycelial_crucible.get().asItem())),
                    "Creative policy must retain ordinary Venous Stone and the custom Crucible item");
            helper.succeed();
        } catch (RuntimeException error) {
            helper.fail("Creative tab event fixture failed: " + error);
        }
    }

    @GameTest(template = "empty")
    public static void sharedMemoryRegistrationPreservesManipulationBindings(GameTestHelper helper) {
        var bindings = java.util.Map.ofEntries(
                java.util.Map.entry(ItemInit.memory_ironhearted, ManipulationInit.ironhearted),
                java.util.Map.entry(ItemInit.memory_blackhearted, ManipulationInit.blackhearted),
                java.util.Map.entry(ItemInit.memory_hematic_rebuke, ManipulationInit.hematic_rebuke),
                java.util.Map.entry(ItemInit.memory_hematic_impressment, ManipulationInit.hematic_impressment),
                java.util.Map.entry(ItemInit.memory_hematic_flare, ManipulationInit.hematic_flare),
                java.util.Map.entry(ItemInit.memory_gloam_laceration, ManipulationInit.gloam_laceration),
                java.util.Map.entry(ItemInit.memory_conjure_living_staff, ManipulationInit.conjure_staff),
                java.util.Map.entry(ItemInit.memory_living_torch, ManipulationInit.conjure_torch),
                java.util.Map.entry(ItemInit.memory_living_flail, ManipulationInit.conjure_flail),
                java.util.Map.entry(ItemInit.memory_synaptic_jolt, ManipulationInit.synaptic_jolt),
                java.util.Map.entry(ItemInit.memory_conductive_mark, ManipulationInit.conductive_mark),
                java.util.Map.entry(ItemInit.memory_insatiable_hunger, ManipulationInit.insatiable_hunger),
                java.util.Map.entry(ItemInit.memory_grave_debt, ManipulationInit.grave_debt),
                java.util.Map.entry(ItemInit.memory_iron_retort, ManipulationInit.iron_retort),
                java.util.Map.entry(ItemInit.memory_sanguine_magnetism, ManipulationInit.sanguine_magnetism));
        for (var binding : bindings.entrySet()) {
            var item = binding.getKey().get();
            helper.assertTrue(BuiltInRegistries.ITEM.get(binding.getKey().getId()) == item,
                    "Memory registry ID changed: " + binding.getKey().getId());
            helper.assertTrue(item instanceof com.vincenthuto.hemomancy.common.item.harbinger.memories.BloodMemoryItem,
                    "Memory lost its learning item class: " + binding.getKey().getId());
            var memory = (com.vincenthuto.hemomancy.common.item.harbinger.memories.BloodMemoryItem) item;
            helper.assertTrue(memory.getManip() == binding.getValue().get() && item.getDefaultMaxStackSize() == 1,
                    "Memory manipulation binding or stack limit changed: " + binding.getKey().getId());
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void blockItemExceptionsKeepTheirOwners(GameTestHelper helper) {
        int custom = 0, manual = 0, itemless = 0;
        for (var entry : BlockInit.getAllBlockEntriesAsStream().toList()) {
            var decision = BlockInit.itemDecision(entry.getId());
            if (decision.itemRoute() == BlockInit.ItemRoute.CUSTOM) custom++;
            if (decision.itemRoute() == BlockInit.ItemRoute.MANUAL) manual++;
            if (decision.itemRoute() == BlockInit.ItemRoute.NONE) itemless++;
            if (decision.itemId() != null) {
                helper.assertTrue(BuiltInRegistries.ITEM.containsKey(decision.itemId()),
                        "Block item owner is missing for " + entry.getId());
            }
        }
        helper.assertTrue(custom == 19 && manual == 6 && itemless == 7,
                "Block item exception counts changed without an explicit decision");
        BlockInit.POTTEDBLOCKS.getEntries().forEach(entry -> helper.assertTrue(
                BlockInit.itemDecision(entry.getId()).itemRoute() == BlockInit.ItemRoute.NONE,
                "Potted block unexpectedly gained an item: " + entry.getId()));
        BlockInit.LIQUIDBLOCKS.getEntries().forEach(entry -> helper.assertTrue(
                BlockInit.itemDecision(entry.getId()).itemRoute() == BlockInit.ItemRoute.NONE,
                "Liquid block unexpectedly gained an item: " + entry.getId()));
        var conduit = BlockInit.itemDecision(BlockInit.sanguine_conduit.getId());
        helper.assertTrue(conduit.itemRoute() == BlockInit.ItemRoute.MANUAL
                && conduit.itemId().equals(BlockInit.sanguine_conduit.getId()),
                "Sanguine Conduit must retain its same-ID manual item");
        var seeds = BlockInit.itemDecision(BlockInit.gourd_stem.getId());
        helper.assertTrue(seeds.itemRoute() == BlockInit.ItemRoute.MANUAL
                && seeds.itemId().getPath().equals("gourd_seeds"),
                "Gourd stem must remain seed-placed");
        var crucible = BlockInit.itemDecision(BlockInit.mycelial_crucible.getId());
        helper.assertTrue(crucible.itemRoute() == BlockInit.ItemRoute.CUSTOM
                && crucible.itemId().equals(BlockInit.mycelial_crucible.getId()),
                "Crucible must keep its custom same-ID BlockItem");
        helper.assertTrue(BlockInit.mycelial_crucible.get().asItem() instanceof MycelialCrucibleBlockItem
                && ItemInit.lethean_poppy_wreath.get().getDefaultMaxStackSize() == 16,
                "Crucible item class or Lethean wreath stack limit changed");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void survivalBreakDropsCrucibleAndContentsOnce(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 3, 4));
        var block = (MycelialCrucibleBlock) BlockInit.mycelial_crucible.get();
        var state = block.defaultBlockState();
        level.setBlockAndUpdate(pos, state);
        block.placeFillers(level, pos, state);
        var crucible = (MycelialCrucibleBlockEntity) level.getBlockEntity(pos);
        crucible.setItem(MycelialCrucibleBlockEntity.SLOT_OUTPUT, new ItemStack(Items.DIAMOND, 3));
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "crucible-break"));
        player.setGameMode(GameType.SURVIVAL);
        player.getStats().setValue(player, Stats.ITEM_CRAFTED.get(block.asItem()), 1);
        helper.assertTrue(MachineAccessEvents.hasPersonalAccess(player, block),
                "Crafted Crucible ownership was not recorded for the survival player");
        player.setPos(pos.getCenter());
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.NETHERITE_PICKAXE));
        helper.assertTrue(player.gameMode.destroyBlock(pos), "Survival player could not break the Crucible");
        int blocks = 0;
        int contents = 0;
        for (ItemEntity drop : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2))) {
            if (drop.getItem().is(BlockInit.mycelial_crucible.get().asItem())) blocks += drop.getItem().getCount();
            if (drop.getItem().is(Items.DIAMOND)) contents += drop.getItem().getCount();
        }
        helper.assertTrue(blocks == 1 && contents == 3,
                "Survival break must drop one Crucible and its three stored diamonds exactly once");
        helper.succeed();
    }
}
