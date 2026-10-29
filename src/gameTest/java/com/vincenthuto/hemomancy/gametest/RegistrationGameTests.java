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
        var main = com.vincenthuto.hemomancy.common.init.CreativeTabInit.hemomancytab.get();
        var wip = com.vincenthuto.hemomancy.common.init.CreativeTabInit.hemomancywiptab.get();
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
        var main = com.vincenthuto.hemomancy.common.init.CreativeTabInit.hemomancytab.get();
        var wip = com.vincenthuto.hemomancy.common.init.CreativeTabInit.hemomancywiptab.get();
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
                java.util.Map.entry(ItemInit.memory_hematic_riposte, ManipulationInit.hematic_riposte),
                java.util.Map.entry(ItemInit.memory_searing_lash, ManipulationInit.searing_lash),
                java.util.Map.entry(ItemInit.memory_umbral_rend, ManipulationInit.umbral_rend),
                java.util.Map.entry(ItemInit.memory_radiant_wake, ManipulationInit.radiant_wake),
                java.util.Map.entry(ItemInit.memory_lodestone_collapse, ManipulationInit.lodestone_collapse),
                java.util.Map.entry(ItemInit.memory_cruor_javelin, ManipulationInit.cruor_javelin),
                java.util.Map.entry(ItemInit.memory_fruiting_contagion, ManipulationInit.fruiting_contagion),
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
        var custom = new java.util.TreeSet<String>();
        var manual = new java.util.TreeSet<String>();
        var itemless = new java.util.TreeSet<String>();
        for (var entry : BlockInit.getAllBlockEntriesAsStream().toList()) {
            var decision = BlockInit.itemDecision(entry.getId());
            String path = entry.getId().getPath();
            if (decision.itemRoute() == BlockInit.ItemRoute.CUSTOM) custom.add(path);
            if (decision.itemRoute() == BlockInit.ItemRoute.MANUAL) manual.add(path);
            if (decision.itemRoute() == BlockInit.ItemRoute.NONE) itemless.add(path);
            if (decision.itemId() != null) {
                helper.assertTrue(BuiltInRegistries.ITEM.containsKey(decision.itemId()),
                        "Block item owner is missing for " + entry.getId());
            }
        }
        var expectedCustom = new java.util.TreeSet<>(java.util.Set.of("mortal_display", "suspended_blood_crystal",
                "suspended_cleansed_blood_crystal", "suspended_vivianite", "visceral_mirror", "fungal_implantation_pylon",
                "phlebotomists_field_case", "earthen_vein", "vial_centrifuge", "hematic_armature", "mycelial_crucible",
                "specimen_jar", "sanguine_monolith", "hematic_stake", "puppeteers_spindle", "mason_effigy",
                "covenant_throne"));
        var expectedManual = new java.util.TreeSet<>(java.util.Set.of("gourd_stem", "active_befouling_ash_trail",
                "active_smouldering_ash_trail", "placed_blood_stained_stone", "lethean_poppy_wreath", "sanguine_conduit"));
        var expectedItemless = new java.util.TreeSet<>(java.util.Set.of("attached_gourd_stem", "filler_block",
                "warp_chair_filler", "abocipher_emitter", "qliphoth_bloom", "escharian_overgrowth",
                "escharian_overgrowth_rim", "brood_tumor", "dormant_ganglion", "cap_bridge", "fruiting_body_cache", "ward_anchor", "ring_door", "chapel_focus", "recollection_cyst",
                "frozen_devotee", "witness_stone", "witness_devotee", "litany_stone", "offering_stand", "pulse_pylon", "spawn_mark",
                "containment_anchor_block", "rot_pyre", "sealed_niche", "opened_niche", "reliquary_sconce"));
        helper.assertTrue(custom.equals(expectedCustom) && manual.equals(expectedManual) && itemless.equals(expectedItemless),
                "Block item exceptions changed without an explicit decision: custom=" + custom + " manual=" + manual
                        + " itemless=" + itemless);
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

    /** Facsimile animals are the Gardens' own fauna: they spawn in its dark, off grass, unlike vanilla animals. */
    @GameTest(template = "empty")
    public static void facsimileAnimalsSpawnInTheGardensDark(GameTestHelper helper) {
        var inside = new net.minecraft.core.BlockPos(1, 2, 1);
        for (int x = 0; x <= 2; x++) for (int y = 1; y <= 4; y++) for (int z = 0; z <= 2; z++) {
            var pos = new net.minecraft.core.BlockPos(x, y, z);
            if (!pos.equals(inside) && !pos.equals(inside.above()))
                helper.setBlock(pos, net.minecraft.world.level.block.Blocks.STONE);
        }
        for (var type : java.util.List.of(com.vincenthuto.hemomancy.common.init.EntityInit.facsimile_horse.get(),
                com.vincenthuto.hemomancy.common.init.EntityInit.facsimile_donkey.get(),
                com.vincenthuto.hemomancy.common.init.EntityInit.facsimile_wolf.get())) {
            var mob = type.create(helper.getLevel());
            mob.moveTo(net.minecraft.world.phys.Vec3.atBottomCenterOf(helper.absolutePos(inside)));
            helper.assertTrue(mob.checkSpawnRules(helper.getLevel(), net.minecraft.world.entity.MobSpawnType.NATURAL),
                    "A facsimile animal refused to spawn in the dark: " + type);
            mob.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void facsimileSpawnEggsNeverBreedVanillaFoals(GameTestHelper helper) {
        var level = helper.getLevel();
        for (var type : java.util.List.of(com.vincenthuto.hemomancy.common.init.EntityInit.facsimile_horse.get(),
                com.vincenthuto.hemomancy.common.init.EntityInit.facsimile_donkey.get(),
                com.vincenthuto.hemomancy.common.init.EntityInit.facsimile_wolf.get())) {
            var parent = (net.minecraft.world.entity.AgeableMob) type.create(level);
            var foal = parent.getBreedOffspring(level, parent);
            helper.assertTrue(foal == null || foal.getType() == type, "A facsimile bred a vanilla foal: " + type);
            if (foal != null) foal.discard();
            parent.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void facsimileBlazesDoNotBurn(GameTestHelper helper) {
        helper.assertTrue(com.vincenthuto.hemomancy.common.init.EntityInit.facsimile_blaze.get().fireImmune(),
                "A facsimile blaze must be fire immune like the blaze it copies");
        helper.succeed();
    }

    /** Vanilla spiders carry a skeleton one time in a hundred; the Gardens hold no vanilla life. */
    @GameTest(template = "empty")
    public static void facsimileSpidersNeverCarryVanillaRiders(GameTestHelper helper) {
        var level = helper.getLevel();
        var type = com.vincenthuto.hemomancy.common.init.EntityInit.facsimile_spider.get();
        for (int i = 0; i < 500; i++) {
            var spider = type.create(level);
            spider.moveTo(net.minecraft.world.phys.Vec3.atCenterOf(helper.absolutePos(new net.minecraft.core.BlockPos(1, 2, 1))));
            spider.finalizeSpawn(level, level.getCurrentDifficultyAt(spider.blockPosition()),
                    net.minecraft.world.entity.MobSpawnType.NATURAL, null);
            boolean ridden = !spider.getPassengers().isEmpty();
            spider.getPassengers().forEach(net.minecraft.world.entity.Entity::discard);
            spider.discard();
            helper.assertTrue(!ridden, "A facsimile spider spawned carrying a vanilla rider");
        }
        helper.succeed();
    }

    /** An antibody exists only for the body it was summoned for: with none in the world, it dissolves. */
    @GameTest(template = "empty", timeoutTicks = 40)
    public static void antibodiesDissolveWithoutTheirSummoner(GameTestHelper helper) {
        var level = helper.getLevel();
        var type = com.vincenthuto.hemomancy.common.init.EntityInit.antibody.get();
        var summoner = java.util.UUID.randomUUID();
        var orphan = type.create(level);
        orphan.moveTo(net.minecraft.world.phys.Vec3.atCenterOf(helper.absolutePos(new net.minecraft.core.BlockPos(1, 3, 1))));
        orphan.bindTo(summoner);
        helper.assertTrue(summoner.equals(orphan.summoner()), "An antibody must remember who it was summoned for");
        level.addFreshEntity(orphan);
        helper.runAfterDelay(5, () -> {
            helper.assertTrue(orphan.isRemoved(), "An antibody with no summoner in the world must dissolve");
            helper.succeed();
        });
    }

    /** A rendering pool takes only what an Apotheos offers: an unowned item floats untouched, an offering is rendered. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void renderingBileRendersOnlyAnApotheosOffering(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        level.setBlock(pos, BlockInit.RENDERING_BILE_BLOCK.get().defaultBlockState(), 3);
        ItemEntity unowned = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5,
                new ItemStack(ItemInit.spore_sac.get()));
        unowned.setNoGravity(true);
        unowned.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        level.addFreshEntity(unowned);
        ItemEntity offering = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5,
                new ItemStack(ItemInit.spore_sac.get(), 3));
        level.addFreshEntity(offering);
        for (int tick = 0; tick < 100; tick++) {
            com.vincenthuto.hemomancy.common.event.RenderingPoolEvents.render(level, offering, true);
        }
        helper.assertTrue(offering.isRemoved(), "An Apotheos's spore sacs must be rendered after 100 ticks in bile");
        int enzymes = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(1.5),
                entity -> entity.getItem().is(ItemInit.recycled_enzyme.get()))
                .stream().mapToInt(entity -> entity.getItem().getCount()).sum();
        helper.assertTrue(enzymes == 3, "Three spore sacs must render into three recycled enzymes, got " + enzymes);
        helper.runAfterDelay(140, () -> {
            helper.assertTrue(!unowned.isRemoved() && unowned.getItem().is(ItemInit.spore_sac.get()),
                    "An item nobody offered must float untouched in rendering bile");
            clearTestArea(helper);
            helper.succeed();
        });
    }

    /** A cap bridge grows a segment at a time along its facing and stops at the first block in its way. */
    @GameTest(template = "empty", timeoutTicks = 60)
    public static void capBridgeGrowsUntilObstructed(GameTestHelper helper) {
        var bridge = (com.vincenthuto.hemomancy.common.block.harbinger.plant.CapBridgeBlock) BlockInit.cap_bridge.get();
        helper.setBlock(new BlockPos(4, 3, 1), net.minecraft.world.level.block.Blocks.STONE);
        helper.setBlock(new BlockPos(1, 3, 1), bridge.seed(net.minecraft.core.Direction.EAST));
        helper.runAfterDelay(20, () -> {
            helper.assertBlockPresent(bridge, new BlockPos(2, 3, 1));
            helper.assertBlockPresent(bridge, new BlockPos(3, 3, 1));
            helper.assertBlockPresent(net.minecraft.world.level.block.Blocks.STONE, new BlockPos(4, 3, 1));
            helper.assertBlockNotPresent(bridge, new BlockPos(5, 3, 1));
            helper.assertBlockProperty(new BlockPos(3, 3, 1),
                    com.vincenthuto.hemomancy.common.block.harbinger.plant.CapBridgeBlock.GROWN, true);
            clearTestArea(helper);
            helper.succeed();
        });
    }

    /** A snapped gill shuts every gill joined to it, then all of them reopen once no stranger is near. */
    @GameTest(template = "empty", timeoutTicks = 160)
    public static void gillsSnapShutTogetherAndReopen(GameTestHelper helper) {
        var gill = BlockInit.fungal_gill.get();
        var open = com.vincenthuto.hemomancy.common.block.harbinger.plant.FungalGillBlock.OPEN;
        helper.setBlock(new BlockPos(1, 5, 1), net.minecraft.world.level.block.Blocks.STONE);
        helper.setBlock(new BlockPos(1, 4, 1), gill);
        helper.setBlock(new BlockPos(1, 3, 1), gill);
        helper.setBlock(new BlockPos(1, 2, 1), gill);
        int shut = com.vincenthuto.hemomancy.common.block.harbinger.plant.FungalGillBlock.snap(helper.getLevel(),
                helper.absolutePos(new BlockPos(1, 3, 1)));
        helper.assertTrue(shut == 3, "All three joined gills must snap shut, got " + shut);
        helper.assertBlockProperty(new BlockPos(1, 2, 1), open, false);
        helper.runAfterDelay(110, () -> {
            helper.assertBlockProperty(new BlockPos(1, 4, 1), open, true);
            helper.assertBlockProperty(new BlockPos(1, 2, 1), open, true);
            helper.setBlock(new BlockPos(1, 5, 1), net.minecraft.world.level.block.Blocks.AIR);
            helper.runAfterDelay(2, () -> {
                helper.assertBlockNotPresent(gill, new BlockPos(1, 4, 1));
                clearTestArea(helper);
            helper.succeed();
            });
        });
    }

    /** A hunter fungus told of a distant alarm remembers where, and walks toward it. */
    @GameTest(template = "empty", timeoutTicks = 80)
    public static void hunterFungusRemembersADistantAlarm(GameTestHelper helper) {
        var level = helper.getLevel();
        var hunter = com.vincenthuto.hemomancy.common.init.EntityInit.hunter_fungus.get().create(level);
        BlockPos start = helper.absolutePos(new BlockPos(1, 1, 1));
        hunter.moveTo(net.minecraft.world.phys.Vec3.atBottomCenterOf(start));
        level.addFreshEntity(hunter);
        BlockPos heard = start.east(10);
        hunter.hearAlarm(heard, level.getGameTime());
        helper.assertTrue(heard.equals(hunter.alarmPos()), "A hunter must remember where the network heard a stranger");
        helper.runAfterDelay(60, () -> {
            helper.assertTrue(hunter.getX() > start.getX() + 3.5, "A hunter must walk toward the heard footsteps");
            hunter.discard();
            helper.succeed();
        });
    }

    /** A stack merged into an offering after it entered the bile is not the offerer's: only what was offered renders. */
    @GameTest(template = "empty", timeoutTicks = 40)
    public static void renderingNeverTakesMoreThanWasOffered(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(3, 2, 3));
        level.setBlock(pos, BlockInit.RENDERING_BILE_BLOCK.get().defaultBlockState(), 3);
        ItemEntity offering = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5,
                new ItemStack(ItemInit.spore_sac.get(), 3));
        offering.setNoGravity(true);
        level.addFreshEntity(offering);
        com.vincenthuto.hemomancy.common.event.RenderingPoolEvents.render(level, offering, true);
        offering.setItem(new ItemStack(ItemInit.spore_sac.get(), 5));
        for (int tick = 1; tick < 100; tick++) {
            com.vincenthuto.hemomancy.common.event.RenderingPoolEvents.render(level, offering, true);
        }
        AABB around = new AABB(pos).inflate(1.5);
        int enzymes = level.getEntitiesOfClass(ItemEntity.class, around,
                entity -> entity.getItem().is(ItemInit.recycled_enzyme.get()))
                .stream().mapToInt(entity -> entity.getItem().getCount()).sum();
        int sacs = level.getEntitiesOfClass(ItemEntity.class, around,
                entity -> entity.isAlive() && entity.getItem().is(ItemInit.spore_sac.get()))
                .stream().mapToInt(entity -> entity.getItem().getCount()).sum();
        helper.assertTrue(enzymes == 3, "Only the three offered sacs may render, got " + enzymes);
        helper.assertTrue(sacs == 2, "The two merged-in sacs must be left floating, got " + sacs);
        helper.succeed();
    }

    /** A gill an Apotheos is standing in never shuts, and the snap does not spread through it. */
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void gillsNeverShutOnAnApotheos(GameTestHelper helper) {
        var gill = BlockInit.fungal_gill.get();
        var open = com.vincenthuto.hemomancy.common.block.harbinger.plant.FungalGillBlock.OPEN;
        helper.setBlock(new BlockPos(5, 5, 1), net.minecraft.world.level.block.Blocks.STONE);
        helper.setBlock(new BlockPos(5, 4, 1), gill);
        helper.setBlock(new BlockPos(5, 3, 1), gill);
        helper.setBlock(new BlockPos(5, 2, 1), gill);
        BlockPos held = helper.absolutePos(new BlockPos(5, 3, 1));
        int shut = com.vincenthuto.hemomancy.common.block.harbinger.plant.FungalGillBlock.snap(helper.getLevel(),
                helper.absolutePos(new BlockPos(5, 4, 1)), held::equals);
        helper.assertTrue(shut == 1, "Only the gill above the Apotheos may shut, got " + shut);
        helper.assertBlockProperty(new BlockPos(5, 3, 1), open, true);
        helper.assertBlockProperty(new BlockPos(5, 2, 1), open, true);
        helper.succeed();
    }

    /** A fruiting body keeps every stack it was given and hands them all back once, never more. */
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void fruitingBodyCacheReturnsEverythingItHolds(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, BlockInit.fruiting_body_cache.get());
        var cache = (com.vincenthuto.hemomancy.common.tile.harbinger.functional.FruitingBodyCacheBlockEntity)
                helper.getBlockEntity(pos);
        UUID owner = UUID.randomUUID();
        var held = new java.util.ArrayList<ItemStack>();
        for (int i = 0; i < 45; i++) held.add(new ItemStack(Items.COBBLESTONE, 64));
        held.add(new ItemStack(Items.DIAMOND_SWORD));
        cache.store(owner, held);
        helper.assertTrue(cache.size() == 46, "Every stack must be held, even past a player inventory's size");
        var back = cache.takeAll();
        helper.assertTrue(back.size() == 46 && back.get(45).is(Items.DIAMOND_SWORD), "Every stack must come back");
        helper.assertTrue(cache.takeAll().isEmpty(), "A cache gives its contents once");
        clearTestArea(helper);
            helper.succeed();
    }

    /** A heart-node's ribs grow only into air: they never dig into stone or a player's builds. */
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void heartNodeRibsNeverReplaceSolidBlocks(GameTestHelper helper) {
        BlockPos nodePos = new BlockPos(8, 1, 8);
        helper.setBlock(nodePos, BlockInit.heart_node.get());
        var node = (com.vincenthuto.hemomancy.common.tile.harbinger.functional.HeartNodeBlockEntity)
                helper.getBlockEntity(nodePos);
        var level = helper.getLevel();
        BlockPos rib0 = com.vincenthuto.hemomancy.common.world.HeartNodeRules.ribBase(helper.absolutePos(nodePos), 0);
        BlockPos rib2 = com.vincenthuto.hemomancy.common.world.HeartNodeRules.ribBase(helper.absolutePos(nodePos), 2);
        for (int y = 0; y <= 3; y++) {
            level.setBlock(rib0.above(y), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(rib2.above(y), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
        }
        level.setBlock(rib2.above(), net.minecraft.world.level.block.Blocks.OAK_PLANKS.defaultBlockState(), 3);
        node.growRib(level, 0);
        node.growRib(level, 2);
        helper.assertTrue(level.getBlockState(rib0).is(BlockInit.calcified_hyphae.get()), "A rib grows into air");
        helper.assertTrue(level.getBlockState(rib0.above(3)).is(BlockInit.infected_cap.get()), "A rib is capped, found " + level.getBlockState(rib0.above(3)) + " below " + level.getBlockState(rib0.above(2)));
        helper.assertTrue(level.getBlockState(rib2.above()).is(net.minecraft.world.level.block.Blocks.OAK_PLANKS),
                "A rib never replaces a player's block");
        helper.assertTrue(level.getBlockState(rib2).is(BlockInit.calcified_hyphae.get()), "The rest of the rib still grows");
        clearTestArea(helper);
            helper.succeed();
    }

    /** Mind Broken is the possession rite's cooldown: milk and a totem's cleanse leave it in place. */
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void mindBrokenSurvivesMilk(GameTestHelper helper) {
        var zombie = helper.spawn(net.minecraft.world.entity.EntityType.ZOMBIE, new BlockPos(1, 1, 1));
        zombie.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                com.vincenthuto.hemomancy.common.init.EffectInit.mind_broken, 200));
        zombie.removeEffectsCuredBy(net.neoforged.neoforge.common.EffectCures.MILK);
        zombie.removeEffectsCuredBy(net.neoforged.neoforge.common.EffectCures.PROTECTED_BY_TOTEM);
        helper.assertTrue(zombie.hasEffect(com.vincenthuto.hemomancy.common.init.EffectInit.mind_broken),
                "Mind Broken must not be cured by milk or a totem");
        zombie.discard();
        helper.succeed();
    }

    /** An echo is not a body: nothing can hit it, push it or hurt it. */
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void possessionEchoCannotBeTouched(GameTestHelper helper) {
        var echo = com.vincenthuto.hemomancy.common.init.EntityInit.possession_echo.get().create(helper.getLevel());
        echo.moveTo(net.minecraft.world.phys.Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(2, 1, 2))));
        helper.assertTrue(!echo.isPickable() && !echo.isPushable() && !echo.canBeCollidedWith(),
                "An echo has no hitbox and no collision");
        helper.assertTrue(!echo.hurt(helper.getLevel().damageSources().generic(), 100f), "An echo cannot be hurt");
        helper.succeed();
    }

    /** A Litany completes only when its three offerings come in the order it names; the focus then lights. */
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void litanyChapelTakesItsOfferingsInOrder(GameTestHelper helper) {
        BlockPos pos = new BlockPos(5, 1, 5);
        helper.setBlock(pos, BlockInit.chapel_focus.get());
        var focus = (com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.ChapelFocusBlockEntity) helper.getBlockEntity(pos);
        focus.configure(com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Kind.LITANY, 1);
        focus.start(null, 7L);
        var order = com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.litanyOrder(7L);
        var items = com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.ChapelFocusBlockEntity.LITANY_ITEMS;
        var wrong = items[(order.get(0) + 1) % items.length] == items[order.get(0)] ? items[0] : items[(order.get(0) + 1) % items.length];
        helper.assertTrue(!focus.offer(new ItemStack(wrong), null), "A wrong first offering is refused");
        helper.assertTrue(!focus.complete(), "A refused Litany is not complete");
        focus.start(null, 7L);
        for (int index : order) {
            helper.assertTrue(focus.offer(new ItemStack(items[index]), null), "The named offering is taken");
        }
        helper.assertTrue(focus.complete(), "Three offerings in order complete the Litany");
        helper.assertBlockProperty(pos, com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelFocusBlock.LIT, true);
        clearTestArea(helper);
            helper.succeed();
    }

    /** A Beholding is answered by walking back to its witnesses in the replayed order, once the memory has shown. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void beholdingChapelWantsItsWitnessesInOrder(GameTestHelper helper) {
        BlockPos pos = new BlockPos(9, 1, 5);
        helper.setBlock(pos, BlockInit.chapel_focus.get());
        var focus = (com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.ChapelFocusBlockEntity) helper.getBlockEntity(pos);
        focus.configure(com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Kind.BEHOLDING, 0);
        var witness = com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.MarkKind.WITNESS;
        focus.setMarks(java.util.List.of(
                new com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Mark(new BlockPos(-7, 0, -3), witness, 0),
                new com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Mark(new BlockPos(-7, 0, 3), witness, 1),
                new com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Mark(new BlockPos(-3, 0, -3), witness, 2),
                new com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Mark(new BlockPos(-3, 0, 3), witness, 3)));
        helper.assertTrue(focus.markPos(focus.marks().get(0)).equals(helper.absolutePos(new BlockPos(2, 1, 2))),
                "An unturned focus finds its witness where the template put it");
        var sequence = com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.beholdingSequence(42L, 4);
        focus.start(null, 42L);
        focus.touchWitness(sequence.get(0), null);
        helper.assertTrue(focus.phase() == com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.ChapelFocusBlockEntity.Phase.SHOW,
                "Touching a witness while the memory is still showing does nothing");
        helper.runAfterDelay(140, () -> {
            helper.assertTrue(focus.phase() == com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.ChapelFocusBlockEntity.Phase.ANSWER,
                    "After the replay the Chapel waits for an answer");
            for (int index : sequence) focus.touchWitness(index, null);
            helper.assertTrue(focus.complete(), "The witnesses walked in order complete the Beholding");
            clearTestArea(helper);
            helper.succeed();
        });
    }

    /** A Beholding lights the witness stone its memory touches, then darkens it when the answer begins. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void beholdingLightsTheWitnessItsMemoryTouches(GameTestHelper helper) {
        BlockPos pos = new BlockPos(6, 1, 6);
        helper.setBlock(pos, BlockInit.chapel_focus.get());
        var focus = (com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.ChapelFocusBlockEntity) helper.getBlockEntity(pos);
        focus.configure(com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Kind.BEHOLDING, 0);
        var witness = com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.MarkKind.WITNESS;
        java.util.List<BlockPos> offsets = java.util.List.of(new BlockPos(-3, 0, -2), new BlockPos(3, 0, -2));
        java.util.List<com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Mark> marks = new java.util.ArrayList<>();
        for (int i = 0; i < offsets.size(); i++) {
            marks.add(new com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Mark(offsets.get(i), witness, i));
            helper.setBlock(pos.offset(offsets.get(i)), BlockInit.witness_stone.get());
        }
        focus.setMarks(marks);
        long seed = 7L;
        int first = com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.beholdingSequence(seed, 2).get(0);
        focus.start(null, seed);
        var lit = com.vincenthuto.hemomancy.common.block.harbinger.puzzle.WitnessStoneBlock.LIT;
        int flash = com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.BEHOLDING_FLASH_TICKS;
        helper.runAfterDelay(flash + 3, () -> {
            for (int i = 0; i < offsets.size(); i++) {
                boolean on = helper.getBlockState(pos.offset(offsets.get(i))).getValue(lit);
                helper.assertTrue(on == (i == first), "Only the touched witness lights; witness " + i + " lit=" + on);
            }
        });
        int shown = flash * (com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.BEHOLDING_STEPS + 1) + 3;
        helper.runAfterDelay(shown, () -> {
            helper.assertTrue(focus.phase() == com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.ChapelFocusBlockEntity.Phase.ANSWER,
                    "The memory has played and waits for its answer");
            for (BlockPos offset : offsets) {
                helper.assertTrue(!helper.getBlockState(pos.offset(offset)).getValue(lit),
                        "Every witness is dark while the challenger answers");
            }
            clearTestArea(helper);
            helper.succeed();
        });
    }

    /** Velorum's witness devotees answer the focus at their lower half, and the upper half (the head) follows. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void beholdingTurnsTheWitnessDevoteesHead(GameTestHelper helper) {
        BlockPos pos = new BlockPos(6, 1, 6);
        helper.setBlock(pos, BlockInit.chapel_focus.get());
        var focus = (com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.ChapelFocusBlockEntity) helper.getBlockEntity(pos);
        focus.configure(com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Kind.BEHOLDING, 0);
        var witness = com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.MarkKind.WITNESS;
        var half = com.vincenthuto.hemomancy.common.block.harbinger.puzzle.WitnessDevoteeBlock.HALF;
        var lit = com.vincenthuto.hemomancy.common.block.harbinger.puzzle.WitnessDevoteeBlock.LIT;
        var devotee = BlockInit.witness_devotee.get().defaultBlockState();
        java.util.List<BlockPos> offsets = java.util.List.of(new BlockPos(-3, 0, -2), new BlockPos(3, 0, -2));
        java.util.List<com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Mark> marks = new java.util.ArrayList<>();
        for (int i = 0; i < offsets.size(); i++) {
            marks.add(new com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Mark(offsets.get(i), witness, i));
            helper.setBlock(pos.offset(offsets.get(i)), devotee);
            helper.setBlock(pos.offset(offsets.get(i)).above(),
                    devotee.setValue(half, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER));
        }
        focus.setMarks(marks);
        long seed = 7L;
        int first = com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.beholdingSequence(seed, 2).get(0);
        focus.start(null, seed);
        int flash = com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.BEHOLDING_FLASH_TICKS;
        helper.runAfterDelay(flash + 3, () -> {
            for (int i = 0; i < offsets.size(); i++) {
                boolean on = helper.getBlockState(pos.offset(offsets.get(i))).getValue(lit);
                boolean head = helper.getBlockState(pos.offset(offsets.get(i)).above()).getValue(lit);
                helper.assertTrue(on == (i == first) && head == on,
                        "Only the touched witness lights, both halves; witness " + i + " lit=" + on + " head=" + head);
            }
        });
        int shown = flash * (com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.BEHOLDING_STEPS + 1) + 3;
        helper.runAfterDelay(shown, () -> {
            for (BlockPos offset : offsets) {
                helper.assertTrue(!helper.getBlockState(pos.offset(offset)).getValue(lit)
                                && !helper.getBlockState(pos.offset(offset).above()).getValue(lit),
                        "Every witness devotee is dark, head and all, while the challenger answers");
            }
            clearTestArea(helper);
            helper.succeed();
        });
    }

    /** A Repose's pylons wind their charge up before each pulse and spend it as the pulse fires. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void reposePylonsWindUpBeforeEachPulse(GameTestHelper helper) {
        BlockPos pos = new BlockPos(6, 1, 6);
        helper.setBlock(pos, BlockInit.chapel_focus.get());
        var focus = (com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.ChapelFocusBlockEntity) helper.getBlockEntity(pos);
        focus.configure(com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Kind.REPOSE, 0);
        BlockPos head = new BlockPos(3, 2, 0);
        var pylon = com.vincenthuto.hemomancy.common.block.harbinger.puzzle.PulsePylonBlock.class;
        var block = BlockInit.pulse_pylon.get().defaultBlockState();
        var part = com.vincenthuto.hemomancy.common.block.harbinger.puzzle.PulsePylonBlock.PART;
        helper.setBlock(pos.offset(head).below(2), block.setValue(part, com.vincenthuto.hemomancy.common.block.harbinger.puzzle.PulsePylonBlock.Part.LOWER));
        helper.setBlock(pos.offset(head).below(), block.setValue(part, com.vincenthuto.hemomancy.common.block.harbinger.puzzle.PulsePylonBlock.Part.MIDDLE));
        helper.setBlock(pos.offset(head), block.setValue(part, com.vincenthuto.hemomancy.common.block.harbinger.puzzle.PulsePylonBlock.Part.UPPER));
        focus.setMarks(java.util.List.of(new com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Mark(head,
                com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.MarkKind.PYLON, 0)));
        focus.start(null, 0L);
        var charge = com.vincenthuto.hemomancy.common.block.harbinger.puzzle.PulsePylonBlock.CHARGE;
        int pulse = com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.REPOSE_PULSE_TICKS;
        int[] peak = {0};
        helper.onEachTick(() -> {
            var state = helper.getBlockState(pos.offset(head));
            if (state.getBlock().getClass() == pylon) peak[0] = Math.max(peak[0], state.getValue(charge));
        });
        helper.runAfterDelay(pulse + 5, () -> {
            helper.assertTrue(peak[0] == com.vincenthuto.hemomancy.common.block.harbinger.puzzle.PulsePylonBlock.MAX_CHARGE,
                    "The pylon wound up to full charge before its pulse, peaked at " + peak[0]);
            helper.assertTrue(helper.getBlockState(pos.offset(head)).getValue(charge) == 0, "The pulse spent the charge");
            helper.assertTrue(helper.getBlockState(pos.offset(head).below(2)).is(BlockInit.pulse_pylon.get()),
                    "The pylon's three storeys stand together");
            clearTestArea(helper);
            helper.succeed();
        });
    }

    /** A focus turned with its piece turns its marks the same way: facing east, a north-authored offset swings east. */
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void chapelMarksTurnWithTheirFocus(GameTestHelper helper) {
        BlockPos pos = new BlockPos(9, 1, 9);
        helper.setBlock(pos, BlockInit.chapel_focus.get().defaultBlockState().setValue(
                com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelFocusBlock.FACING, net.minecraft.core.Direction.EAST));
        var focus = (com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.ChapelFocusBlockEntity) helper.getBlockEntity(pos);
        var mark = new com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Mark(new BlockPos(-5, 0, -2),
                com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.MarkKind.CIRCLE, 0);
        focus.setMarks(java.util.List.of(mark));
        helper.assertTrue(focus.markPos(mark).equals(helper.absolutePos(pos).offset(2, 0, -5)),
                "Turned a quarter clockwise, (-5, 0, -2) lands at (2, 0, -5), found " + focus.markPos(mark));
        clearTestArea(helper);
        helper.succeed();
    }

    /** A tomb placed by a structure (no placer) grows its filler blocks on its first tick, so it can be touched. */
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void structurePlacedSarcophagusGrowsItsFillers(GameTestHelper helper) {
        BlockPos pos = new BlockPos(6, 2, 6);
        helper.setBlock(pos, BlockInit.saint_sarcophagus.get().defaultBlockState()
                .setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, net.minecraft.core.Direction.WEST));
        helper.runAfterDelay(3, () -> {
            int fillers = 0;
            for (BlockPos near : BlockPos.betweenClosed(helper.absolutePos(pos).offset(-3, -1, -3),
                    helper.absolutePos(pos).offset(3, 2, 3))) {
                if (helper.getLevel().getBlockState(near).is(BlockInit.filler_block.get())) fillers++;
            }
            helper.assertTrue(fillers == 23,
                    "A tomb two wide and four long, centred on its block, must stand on 23 fillers, found " + fillers);
            BlockPos head = helper.absolutePos(pos).offset(2, 1, 1);
            helper.assertTrue(helper.getLevel().getBlockState(head).is(BlockInit.filler_block.get()),
                    "A west-facing tomb's head end must reach two cells east of its block");
            clearTestArea(helper);
            helper.succeed();
        });
    }

    /** Each completed Chapel lights one node on a nearby ring door, and its ring starts turning to the seam. */
    @GameTest(template = "empty", timeoutTicks = 20, batch = "ringDoorNodes")
    public static void ringDoorLightsANodePerCompletedChapel(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos door = new BlockPos(6, 1, 12);
        helper.setBlock(door, ringDoor(net.minecraft.core.Direction.NORTH));
        var items = com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.ChapelFocusBlockEntity.LITANY_ITEMS;
        for (int slot = 0; slot < 3; slot++) {
            BlockPos at = new BlockPos(2 + slot * 3, 1, 2);
            helper.setBlock(at, BlockInit.chapel_focus.get());
            var focus = (com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.ChapelFocusBlockEntity) helper.getBlockEntity(at);
            focus.configure(com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Kind.LITANY, slot);
            if (slot < 2) {
                focus.start(null, 11L + slot);
                for (int index : com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.litanyOrder(11L + slot)) {
                    focus.offer(new ItemStack(items[index]), null);
                }
            }
        }
        var controller = (com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.RingDoorBlockEntity) helper.getBlockEntity(door);
        int lit = controller.refresh(com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.RingDoorBlockEntity
                .chapelsNear(level, helper.absolutePos(door)));
        helper.assertTrue(lit == 2 && controller.nodes() == 2, "Two completed Chapels light two nodes, got " + lit);
        helper.assertTrue(controller.turnStart(0) > 0 && controller.turnStart(1) > 0 && controller.turnStart(2) == 0,
                "The outer and middle rings start turning as their nodes light; the inner rests");
        helper.assertTrue(controller.phase() == com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.RingDoorBlockEntity.Phase.IDLE,
                "Lighting nodes does not open the door");
        clearTestArea(helper);
        helper.succeed();
    }

    /** An Apotheos cracks the door; it bursts, the frame stands, and the tomb remembers which Chapels were skipped. */
    @GameTest(template = "empty", timeoutTicks = 80, batch = "ringDoorShatter")
    public static void shatteredRingDoorTellsTheTombWhatWasSkipped(GameTestHelper helper) {
        BlockPos door = new BlockPos(6, 1, 6);
        helper.setBlock(door, ringDoor(net.minecraft.core.Direction.NORTH));
        BlockPos tombPos = new BlockPos(18, 2, 12);
        helper.setBlock(tombPos, BlockInit.saint_sarcophagus.get());
        var skipped = java.util.List.of(com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Kind.REPOSE);
        var controller = (com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.RingDoorBlockEntity) helper.getBlockEntity(door);
        var tomb = (com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.SaintSarcophagusBlockEntity) helper.getBlockEntity(tombPos);
        helper.runAfterDelay(2, () -> {
            assertRingDoorCells(helper, door, false);
            helper.assertTrue(controller.shatter(skipped), "An idle door can be shattered");
            helper.assertTrue(tomb.doorMode() == com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.SaintSarcophagusBlockEntity.DoorMode.NONE,
                    "The tomb is told only when the slab bursts");
        });
        helper.runAfterDelay(2 + com.vincenthuto.hemomancy.common.block.harbinger.puzzle.RingDoorRules.SHATTER_TICKS / 2, () ->
                helper.assertTrue(controller.phase() == com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.RingDoorBlockEntity.Phase.SHATTERING,
                        "The slab is still cracking halfway through"));
        helper.runAfterDelay(2 + com.vincenthuto.hemomancy.common.block.harbinger.puzzle.RingDoorRules.SHATTER_TICKS + 4, () -> {
            assertRingDoorCells(helper, door, true);
            helper.assertTrue(controller.phase() == com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.RingDoorBlockEntity.Phase.OPEN
                            && controller.openedBy() == com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.SaintSarcophagusBlockEntity.DoorMode.FORCED,
                    "A burst door is open, and forced");
            helper.assertTrue(tomb.doorMode() == com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.SaintSarcophagusBlockEntity.DoorMode.FORCED,
                    "A shattered door is a forced awakening");
            helper.assertTrue(tomb.skippedChapels().equals(java.util.List.of("REPOSE")), "The skipped Repose is remembered");
            clearTestArea(helper);
            helper.succeed();
        });
    }

    /** A Silentium's blood fills the rings one stage at a time, 300 a ring, and the door sinks open. */
    @GameTest(template = "empty", timeoutTicks = 260, batch = "ringDoorProjection")
    public static void projectedRingDoorPaysARingAtATimeAndOpens(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos door = new BlockPos(6, 1, 6);
        helper.setBlock(door, ringDoor(net.minecraft.core.Direction.NORTH));
        BlockPos tombPos = new BlockPos(18, 2, 12);
        helper.setBlock(tombPos, BlockInit.saint_sarcophagus.get());
        var controller = (com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.RingDoorBlockEntity) helper.getBlockEntity(door);
        var tomb = (com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.SaintSarcophagusBlockEntity) helper.getBlockEntity(tombPos);
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ring-door-silentium"));
        player.setGameMode(GameType.SURVIVAL);
        player.setPos(helper.absolutePos(door).getCenter().add(0, 1, -3));
        var degree = com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getInitiatoryDegree(player);
        degree.setDegreeNumber(8);
        degree.setArchonPath(com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.SILENTIUM);
        var volume = com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getBloodVolume(player);
        volume.setActive(true);
        volume.setMaxBloodVolume(2000);
        volume.setBloodVolume(1500);
        int cost = com.vincenthuto.hemomancy.common.block.harbinger.puzzle.RingDoorRules.stageCost();
        int fill = com.vincenthuto.hemomancy.common.block.harbinger.puzzle.RingDoorRules.FILL_TICKS;
        int sink = com.vincenthuto.hemomancy.common.block.harbinger.puzzle.RingDoorRules.sinkStart();
        int length = com.vincenthuto.hemomancy.common.block.harbinger.puzzle.RingDoorRules.projectionLength();
        helper.runAfterDelay(2, () -> {
            assertRingDoorCells(helper, door, false);
            helper.assertTrue(controller.startProjection(player), "An idle door takes a projection");
            helper.assertTrue(player.getUUID().equals(controller.projector()), "The door remembers who projects");
            helper.assertTrue(volume.getBloodVolume() == 1500, "Nothing is paid before the first stage starts");
        });
        for (int ring = 0; ring < 3; ring++) {
            int expected = 1500 - cost * (ring + 1);
            int stage = ring;
            helper.runAfterDelay(2 + ring * fill + fill / 2, () -> {
                helper.assertTrue(com.vincenthuto.hemomancy.common.block.harbinger.puzzle.RingDoorRules.stageAt(controller.phaseTick()) == stage,
                        "Ring " + stage + " should be filling at tick " + controller.phaseTick());
                helper.assertTrue(volume.getBloodVolume() == expected,
                        "Ring " + stage + " is paid at its own stage: expected " + expected + ", found " + volume.getBloodVolume());
            });
        }
        helper.runAfterDelay(2 + sink - 8, () -> helper.assertTrue(
                tomb.doorMode() == com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.SaintSarcophagusBlockEntity.DoorMode.NONE,
                "The tomb is not told before the slab sinks"));
        helper.runAfterDelay(2 + sink + 8, () -> {
            helper.assertTrue(tomb.doorMode() == com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.SaintSarcophagusBlockEntity.DoorMode.PROPER
                            && tomb.skippedChapels().isEmpty(),
                    "The tomb learns it was opened properly as the slab starts to sink");
            helper.assertTrue(controller.phase() == com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.RingDoorBlockEntity.Phase.PROJECTING,
                    "The slab is still sinking");
            assertRingDoorCells(helper, door, false);
        });
        helper.runAfterDelay(2 + length + 6, () -> {
            assertRingDoorCells(helper, door, true);
            helper.assertTrue(controller.phase() == com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.RingDoorBlockEntity.Phase.OPEN
                            && controller.openedBy() == com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.SaintSarcophagusBlockEntity.DoorMode.PROPER
                            && controller.projector() == null,
                    "A projected door ends open, opened properly");
            helper.assertTrue(volume.getBloodVolume() == 1500 - 3 * cost, "Three rings in all, and no more");
            clearTestArea(helper);
            helper.succeed();
        });
    }

    private static net.minecraft.world.level.block.state.BlockState ringDoor(net.minecraft.core.Direction facing) {
        return BlockInit.ring_door.get().defaultBlockState()
                .setValue(com.vincenthuto.hemomancy.common.block.harbinger.puzzle.RingDoorBlock.FACING, facing);
    }

    /**
     * Checks a north-facing door's 5x5 face: the controller survives; closed, every cell is a filler; open, the
     * controller reads OPEN, the frame cells keep their fillers and the interior is clear.
     */
    private static void assertRingDoorCells(GameTestHelper helper, BlockPos door, boolean open) {
        var state = helper.getBlockState(door);
        helper.assertTrue(state.is(BlockInit.ring_door.get()), "The ring door's controller must survive, found " + state);
        helper.assertTrue(state.getValue(com.vincenthuto.hemomancy.common.block.harbinger.puzzle.RingDoorBlock.OPEN) == open,
                "The controller should read open=" + open);
        for (BlockPos offset : com.vincenthuto.hemomancy.common.block.harbinger.puzzle.RingDoorGeometry.FILLERS) {
            BlockPos cell = door.offset(offset);
            boolean frame = com.vincenthuto.hemomancy.common.block.harbinger.puzzle.RingDoorGeometry.isFrame(offset);
            if (!open || frame) {
                helper.assertBlockPresent(BlockInit.filler_block.get(), cell);
            } else {
                helper.assertBlockPresent(net.minecraft.world.level.block.Blocks.AIR, cell);
            }
        }
    }

    /** Tonight's fixture tests leave nothing behind for the tests the grid places here next. */
    private static void clearTestArea(GameTestHelper helper) {
        var level = helper.getLevel();
        for (BlockPos pos : BlockPos.betweenClosed(helper.absolutePos(new BlockPos(0, 1, 0)), helper.absolutePos(new BlockPos(20, 8, 20)))) {
            if (!level.getBlockState(pos).isAir()) level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
        }
        for (var item : level.getEntitiesOfClass(net.minecraft.world.entity.Entity.class,
                new AABB(helper.absolutePos(new BlockPos(0, 0, 0))).expandTowards(20, 9, 20),
                entity -> !(entity instanceof net.minecraft.world.entity.player.Player))) {
            item.discard();
        }
    }

    /** A Saint woken through a shattered door fights with its skipped deeds; one woken properly does not. */
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void forcedSaintFightsWithItsSkippedChapels(GameTestHelper helper) {
        var level = helper.getLevel();
        var proper = com.vincenthuto.hemomancy.common.init.EntityInit.hemorath.get().create(level);
        var forced = com.vincenthuto.hemomancy.common.init.EntityInit.hemorath.get().create(level);
        float base = proper.getMaxHealth();
        proper.setAwakening("PROPER", java.util.List.of());
        forced.setAwakening("FORCED", java.util.List.of("DOWNFALL", "BEHOLDING"));
        helper.assertTrue(Math.abs(proper.getMaxHealth() - base) < 0.01f, "A proper awakening is not enraged");
        helper.assertTrue(Math.abs(forced.getMaxHealth() - base * 1.5f) < 0.5f,
                "A skipped Downfall lends the Saint its fall: " + base + " -> " + forced.getMaxHealth());
        helper.assertTrue(forced.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE), "A skipped Beholding hides its tells");
        helper.assertTrue(!forced.hasEffect(net.minecraft.world.effect.MobEffects.REGENERATION), "The Litany was not skipped");
        proper.discard();
        forced.discard();
        helper.succeed();
    }

    /** A Litany ignores an empty hand or an unrelated item: only a wrong named offering fails it. */
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void litanyIgnoresStrayClicks(GameTestHelper helper) {
        BlockPos pos = new BlockPos(5, 1, 5);
        helper.setBlock(pos, BlockInit.chapel_focus.get());
        var focus = (com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.ChapelFocusBlockEntity) helper.getBlockEntity(pos);
        focus.configure(com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Kind.LITANY, 0);
        focus.start(null, 7L);
        focus.offer(ItemStack.EMPTY, null);
        focus.offer(new ItemStack(Items.DIRT), null);
        helper.assertTrue(focus.phase() == com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.ChapelFocusBlockEntity.Phase.ANSWER,
                "An empty hand or an unrelated item must not fail the Litany");
        clearTestArea(helper);
        helper.succeed();
    }
}
