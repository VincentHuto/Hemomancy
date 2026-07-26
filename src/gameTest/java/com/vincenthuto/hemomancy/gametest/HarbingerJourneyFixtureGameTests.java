package com.vincenthuto.hemomancy.gametest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity;
import com.vincenthuto.hemomancy.common.event.SanguineFormationProjectionHandler;
import com.vincenthuto.hemomancy.common.block.harbinger.BrazierBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulations;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.mission.FirstBloodcraftAssignmentHelper;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureOffering;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureOfferingPlacement;
import com.vincenthuto.hemomancy.common.tile.IronBrazierBlockEntity;
import com.vincenthuto.hemomancy.gametest.journey.HemoJourneyChecks;
import com.vincenthuto.hemomancy.gametest.journey.HemoJourneyFixtures;
import com.vincenthuto.hemomancy.gametest.journey.HemoJourneyResult;
import com.vincenthuto.hemomancy.gametest.journey.HemoJourneyStage;
import com.vincenthuto.hemomancy.gametest.journey.HemoJourneyCheckpointRules;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.nbt.ListTag;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HarbingerJourneyFixtureGameTests {
	private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";
	private static final String FIXTURES_CLASS =
			"com.vincenthuto.hemomancy.gametest.journey.HemoJourneyFixtures";

	private HarbingerJourneyFixtureGameTests() {
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void journeyRankupRunwayReachesArchon(GameTestHelper helper) {
		List<String> stages = java.util.Arrays.stream(HemoJourneyStage.values()).map(HemoJourneyStage::id).toList();
		List<String> expectedTail = List.of("initiate_rite", "adept_rite", "illuminatus_rite",
				"sanctified_rite", "archon_rite", "complete");
		helper.assertTrue(stages.size() >= expectedTail.size()
						&& stages.subList(stages.size() - expectedTail.size(), stages.size()).equals(expectedTail),
				"The operator journey must continue through every public Harbinger rank-up to Archon");
		for (String rite : List.of("initiate_rite", "sanguine_brotherhood", "illuminatus_rite",
				"sanctified_rite", "archon_rite")) {
			var recipe = com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe.getRiteByLocation(
					helper.getLevel(), Hemomancy.rloc("cardinal_rite/" + rite));
			helper.assertTrue(recipe != null && recipe.isRankup(),
					"Journey rank-up recipe must load as a rank-up: " + rite);
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void vicarRewardAcceptsAllInventory(GameTestHelper helper) {
		helper.assertTrue(HemoJourneyCheckpointRules.rewardQuantityPassed(3, 7, 0, 4),
				"A positive inventory delta may satisfy the exact reward quantity");
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void vicarRewardAcceptsSplitInventoryAndDrop(GameTestHelper helper) {
		helper.assertTrue(HemoJourneyCheckpointRules.rewardQuantityPassed(3, 5, 2, 4),
				"Inventory delta plus attributed drops may satisfy the exact reward quantity");
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void vicarRewardRejectsBaselineAndWrongQuantities(GameTestHelper helper) {
		helper.assertTrue(!HemoJourneyCheckpointRules.rewardQuantityPassed(4, 4, 0, 4),
				"Preexisting baseline inventory must not satisfy a reward");
		helper.assertTrue(!HemoJourneyCheckpointRules.rewardQuantityPassed(0, 3, 0, 4),
				"An incomplete quantity must not satisfy a reward");
		helper.assertTrue(!HemoJourneyCheckpointRules.rewardQuantityPassed(0, 5, 0, 4),
				"An excessive quantity must not satisfy a reward");
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void fixtureDimensionResolvesPersistedServerLevel(GameTestHelper helper) {
		ServerPlayer player = detachedTestPlayer(helper);
		try {
			helper.assertTrue(HemoJourneyFixtures.fixtureLevel(player) == helper.getLevel(),
					"Fixture resolver must use the persisted dimension key");
			helper.succeed();
		} finally {
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void mortalDisplayPlatform(GameTestHelper helper) {
		withFixture(helper, HemoJourneyStage.MORTAL_DISPLAY, (origin, player) -> {
			for (int x = -2; x <= 2; x++) {
				for (int z = -2; z <= 2; z++) {
					assertBlock(helper, origin.offset(x, 0, z), Blocks.STONE);
				}
			}
			assertBlock(helper, origin.above(), BlockInit.mortal_display.get());
		});
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void sanguineInitiationPattern(GameTestHelper helper) {
		withFixture(helper, HemoJourneyStage.SANGUINE_INITIATION, (origin, player) -> {
			Block[][] rows = {
					{ Blocks.STONE_BRICKS, BlockInit.engram_block.get(), Blocks.STONE_BRICKS },
					{ BlockInit.engram_block.get(), BlockInit.hematic_iron_block.get(), BlockInit.engram_block.get() },
					{ Blocks.STONE_BRICKS, BlockInit.engram_block.get(), Blocks.STONE_BRICKS }
			};
			assertFloor(helper, origin, rows);
		});
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void sanguineLandingIsSupportedAndReachable(GameTestHelper helper) {
		withFixture(helper, HemoJourneyStage.SANGUINE_INITIATION, (origin, player) -> {
			BlockPos landing = invokeJourneyLanding(origin);
			BlockPos support = landing.below();
			helper.assertTrue(helper.getLevel().getBlockState(support)
					.isFaceSturdy(helper.getLevel(), support, Direction.UP),
					"Journey landing must have solid support");
			int fixtureRadius = Math.max(Math.abs(landing.getX() - origin.getX()),
					Math.abs(landing.getZ() - origin.getZ()));
			helper.assertTrue(fixtureRadius <= 2,
					"Journey landing must remain inside the Sanguine Initiation fixture boundary");
			Vec3 eye = new Vec3(landing.getX() + 0.5D, landing.getY() + player.getEyeHeight(),
					landing.getZ() + 0.5D);
			helper.assertTrue(eye.distanceTo(Vec3.atCenterOf(origin.above())) <= player.blockInteractionRange(),
					"Sanguine Initiation center must remain within actual block interaction reach");
		});
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void attunedProjectorTarget(GameTestHelper helper) {
		withFixture(helper, HemoJourneyStage.FORMATION_PROJECTED, (origin, player) -> helper.assertTrue(
				helper.getLevel().getBlockState(origin.above()).is(SanguineFormationProjectionHandler.PROJECTOR_TAG),
				"Expected an attuned formation projector at the fixture target"));
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void tallStructureBaseClearsPlatform(GameTestHelper helper) {
		helper.assertTrue(HemoJourneyFixtures.structureBaseHeight(1) == 1, "One-layer structure base is wrong");
		helper.assertTrue(HemoJourneyFixtures.structureBaseHeight(2) == 2, "Two-layer structure base is wrong");
		helper.assertTrue(HemoJourneyFixtures.structureBaseHeight(3) == 3, "Three-layer structure base is wrong");
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void offeringPlannerExpandsDistinctPerimeterSlots(GameTestHelper helper) {
		BlockPos center = helper.absolutePos(new BlockPos(4, 3, 4));
		List<BloodStructureOffering> offerings = List.of(
				new BloodStructureOffering(Ingredient.of(Items.GLASS_BOTTLE), 1),
				new BloodStructureOffering(Ingredient.of(Items.COPPER_INGOT), 2));
		var slots = BloodStructureOfferingPlacement.plan(center, 1, 1, 1, offerings);
		helper.assertTrue(slots.size() == 3, "Offering counts must expand into distinct braziers");
		helper.assertTrue(slots.stream().map(slot -> slot.pos()).distinct().count() == 3,
				"Offering brazier positions must be unique");
		helper.assertTrue(slots.stream().noneMatch(slot -> Math.abs(slot.pos().getX() - center.getX()) <= 1
				&& Math.abs(slot.pos().getZ() - center.getZ()) <= 1), "Offering braziers must be outside the structure");
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void centrifugeJourneySuppliesEmptyUnlitOfferingBraziers(GameTestHelper helper) {
		withFixture(helper, HemoJourneyStage.CENTRIFUGE_PREPARED, (origin, player) -> {
			for (BlockPos brazierPos : List.of(origin.offset(-2, 1, 0), origin.offset(2, 1, 0))) {
				assertBlock(helper, brazierPos, BlockInit.iron_brazier.get());
				helper.assertTrue(helper.getLevel().getBlockState(brazierPos).getValue(BrazierBlock.RITUAL_PHASE) == 0,
						"Journey offering braziers must begin unlit");
				helper.assertTrue(helper.getLevel().getBlockEntity(brazierPos) instanceof IronBrazierBlockEntity brazier
						&& !brazier.hasOffering(), "Journey offering braziers must begin empty");
			}
			helper.assertTrue(player.getInventory().countItem(Items.GLASS_BOTTLE) == 1,
					"Journey must supply the bottle offering");
			helper.assertTrue(player.getInventory().countItem(Items.COPPER_INGOT) == 1,
					"Journey must supply the copper offering");
			helper.assertTrue(HemoCapabilityAccess.requireBloodVolume(player).getBloodVolume()
					>= 150.0D + 2.0D * BrazierBlock.BLOOD_TO_LIGHT,
					"Journey blood budget must cover both braziers and the structure craft");
		});
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void liberSanguinumPattern(GameTestHelper helper) {
		withFixture(helper, HemoJourneyStage.LIBER_CRAFTED, (origin, player) -> assertFloor(helper, origin,
				ashPattern(Blocks.BOOKSHELF)));
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void hematicIronPattern(GameTestHelper helper) {
		withFixture(helper, HemoJourneyStage.HEMATIC_IRON_CRAFTED, (origin, player) -> assertFloor(helper, origin,
				ashPattern(Blocks.IRON_BLOCK)));
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void liberAcceptsPickedUpOutput(GameTestHelper helper) {
		ServerPlayer player = detachedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(4, 3, 4));
		try {
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.LIBER_CRAFTED, origin);
			player.getInventory().add(new ItemStack(ItemInit.liber_sanguinum.get()));
			helper.assertTrue(invokeBooleanFixture("captureExpectedOutputs", player,
					HemoJourneyStage.LIBER_CRAFTED, origin),
					"A newly picked-up Liber Sanguinium must remain attributable to its checkpoint");
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			player.discard();
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void markedVicarSpawn(GameTestHelper helper) {
		withFixture(helper, HemoJourneyStage.VICAR_REWARD, (origin, player) -> {
			List<HarbingerVicarEntity> vicars = helper.getLevel().getEntitiesOfClass(HarbingerVicarEntity.class,
					new AABB(origin.getX() - 3, origin.getY(), origin.getZ() - 3,
							origin.getX() + 4, origin.getY() + 5, origin.getZ() + 4),
					entity -> entity.getTags().contains("hemomancy.dev_test.journey"));
			helper.assertTrue(vicars.size() == 1, "Expected exactly one journey-marked Harbinger Vicar");
			HarbingerVicarEntity vicar = vicars.getFirst();
			helper.assertTrue(vicar.isInvulnerable(), "Expected journey Vicar to be invulnerable");
			helper.assertTrue(vicar.isNoAi(), "Expected journey Vicar to have no AI");
		});
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void cleanupPreservesUnownedBlock(GameTestHelper helper) {
		ServerPlayer player = detachedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(4, 3, 4));
		try {
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.MORTAL_DISPLAY, origin);
			BlockPos unowned = origin.offset(4, 1, 4);
			helper.getLevel().setBlockAndUpdate(unowned, Blocks.DIAMOND_BLOCK.defaultBlockState());
			HemoJourneyFixtures.cleanup(player, origin);
			assertBlock(helper, unowned, Blocks.DIAMOND_BLOCK);
		} finally {
			player.discard();
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void preparationRefusesOccupiedFixture(GameTestHelper helper) {
		ServerPlayer player = detachedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(4, 3, 4));
		BlockPos occupied = origin.above();
		helper.getLevel().setBlockAndUpdate(occupied, Blocks.BEDROCK.defaultBlockState());
		try {
			try {
				HemoJourneyFixtures.prepare(player, HemoJourneyStage.MORTAL_DISPLAY, origin);
				helper.fail("Expected occupied fixture preparation to fail");
			} catch (IllegalStateException expected) {
				assertBlock(helper, occupied, Blocks.BEDROCK);
			}
		} finally {
			player.discard();
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void formationBaselineRejectsPreexistingOutput(GameTestHelper helper) {
		ServerPlayer player = detachedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(4, 3, 4));
		ItemEntity oldOutput = new ItemEntity(helper.getLevel(), origin.getX() + 1.5D, origin.getY() + 1.0D,
				origin.getZ() + 0.5D, new ItemStack(ItemInit.sanguine_formation.get()));
		helper.getLevel().addFreshEntity(oldOutput);
		try {
			var blood = HemoCapabilityAccess.requireBloodVolume(player);
			blood.setActive(true);
			blood.setBloodVolume(5000.0D);
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.FORMATION_PROJECTED, origin);
			blood.setBloodVolume(4900.0D);
			HemoJourneyResult result = HemoJourneyChecks.verify(player, HemoJourneyStage.FORMATION_PROJECTED, origin);
			helper.assertTrue(!result.passed(), "Preexisting formation must not satisfy the stage baseline");
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			oldOutput.discard();
			player.discard();
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void formationAcceptsCompletionAndSettledOutput(GameTestHelper helper) {
		ServerPlayer player = detachedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(4, 3, 4));
		try {
			var blood = HemoCapabilityAccess.requireBloodVolume(player);
			blood.setActive(true);
			blood.setBloodVolume(5000.0D);
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.FORMATION_PROJECTED, origin);
			ItemEntity settled = spawn(helper, origin.getX() + 0.5D, origin.getY() + 1.0D,
					origin.getZ() + 0.5D, new ItemStack(ItemInit.sanguine_formation.get()));
			blood.setBloodVolume(4800.0D);
			helper.assertTrue(HemoJourneyCheckpointRules.formationPassed(5000.0D, blood.getBloodVolume(),
					invokeBooleanFixture("captureExpectedOutputs", player, HemoJourneyStage.FORMATION_PROJECTED, origin)),
					"A completed formation and its settled output must pass even when held use begins another cycle");
			invokeFixture("cleanupForExit", player, HemoJourneyStage.FORMATION_PROJECTED, origin);
			helper.assertTrue(settled.isRemoved(), "Settled formation output must be attributable and cleaned");
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			player.discard();
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void formationAcceptsAutoPickedUpOutput(GameTestHelper helper) {
		ServerPlayer player = detachedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(4, 3, 4));
		try {
			var blood = HemoCapabilityAccess.requireBloodVolume(player);
			blood.setActive(true);
			blood.setBloodVolume(5000.0D);
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.FORMATION_PROJECTED, origin);
			player.getInventory().add(new ItemStack(ItemInit.sanguine_formation.get()));
			blood.setBloodVolume(4900.0D);
			helper.assertTrue(invokeBooleanFixture("captureExpectedOutputs", player,
					HemoJourneyStage.FORMATION_PROJECTED, origin),
					"A newly auto-picked-up formation must remain attributable to this checkpoint");
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			player.discard();
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void transitionSemantics(GameTestHelper helper) {
		try {
			Class<?> transitions = Class.forName("com.vincenthuto.hemomancy.gametest.journey.HemoJourneyTransition");
			Method next = transitions.getMethod("next", HemoJourneyStage.class, boolean.class, boolean.class);
			helper.assertTrue(next.invoke(null, HemoJourneyStage.MORTAL_DISPLAY, false, true)
					== HemoJourneyStage.MORTAL_DISPLAY, "Failed verification must not advance");
			helper.assertTrue(next.invoke(null, HemoJourneyStage.MORTAL_DISPLAY, true, false)
					== HemoJourneyStage.MORTAL_DISPLAY, "Failed preparation must roll back");
			helper.assertTrue(next.invoke(null, HemoJourneyStage.MORTAL_DISPLAY, true, true)
					== HemoJourneyStage.SANGUINE_INITIATION, "Successful transition must advance exactly one stage");
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError("Transition seam is missing", exception);
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void exactOutputOwnershipPreservesUnrelatedSameItem(GameTestHelper helper) {
		ServerPlayer player = detachedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(4, 3, 4));
		try {
			var blood = HemoCapabilityAccess.requireBloodVolume(player);
			blood.setActive(true);
			blood.setBloodVolume(5000.0D);
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.FORMATION_PROJECTED, origin);
			ItemEntity expected = spawn(helper, origin.getX() + 0.5D, origin.getY() + 1.5D,
					origin.getZ() - 0.05D, new ItemStack(ItemInit.sanguine_formation.get()));
			ItemEntity unrelated = spawn(helper, origin.getX() + 3.5D, origin.getY() + 1.0D,
					origin.getZ() + 3.5D, new ItemStack(ItemInit.sanguine_formation.get()));
			blood.setBloodVolume(4900.0D);
			helper.assertTrue(invokeBooleanFixture("captureExpectedOutputs", player,
					HemoJourneyStage.FORMATION_PROJECTED, origin), "Expected production output was not captured");
			invokeFixture("cleanupOwnedOutputs", player, origin);
			helper.assertTrue(expected.isRemoved(), "Captured production output must be cleaned");
			helper.assertTrue(!unrelated.isRemoved(), "Unrelated same-type drop must be preserved");
			unrelated.discard();
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			player.discard();
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void liberPositiveDelta(GameTestHelper helper) {
		positiveCraftDelta(helper, HemoJourneyStage.LIBER_CRAFTED, ItemInit.liber_sanguinum.get());
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void hematicIronPositiveDelta(GameTestHelper helper) {
		positiveCraftDelta(helper, HemoJourneyStage.HEMATIC_IRON_CRAFTED,
				BlockInit.hematic_iron_block.get().asItem());
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void fallenLiberOutputIsCapturedAndCleaned(GameTestHelper helper) {
		settledCraftOutput(helper, HemoJourneyStage.LIBER_CRAFTED, ItemInit.liber_sanguinum.get());
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void fallenHematicIronOutputIsCapturedAndCleaned(GameTestHelper helper) {
		settledCraftOutput(helper, HemoJourneyStage.HEMATIC_IRON_CRAFTED,
				BlockInit.hematic_iron_block.get().asItem());
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void vicarPositiveDelta(GameTestHelper helper) {
		ServerPlayer player = detachedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(4, 3, 4));
		try {
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.VICAR_REWARD, origin);
			for (ItemStack reward : FirstBloodcraftAssignmentHelper.rewardStacks()) {
				spawn(helper, origin.getX() + 0.5D, origin.getY() + 1.5D, origin.getZ() + 0.5D, reward.copy());
			}
			boolean captured = invokeBooleanFixture("captureExpectedOutputs", player,
					HemoJourneyStage.VICAR_REWARD, origin);
			helper.assertTrue(HemoJourneyCheckpointRules.rewardPassed(captured, true, true),
					"Exact Vicar deltas plus a newly earned claim must pass");
		} finally {
			invokeFixture("cleanupForExit", player, HemoJourneyStage.VICAR_REWARD, origin);
			player.discard();
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void verifiedLatchRetriesWithoutReverification(GameTestHelper helper) {
		try {
			Class<?> transitions = Class.forName("com.vincenthuto.hemomancy.gametest.journey.HemoJourneyTransition");
			Method shouldVerify = transitions.getMethod("shouldVerify", HemoJourneyStage.class, String.class);
			for (HemoJourneyStage earned : List.of(HemoJourneyStage.LIBER_CRAFTED,
					HemoJourneyStage.HEMATIC_IRON_CRAFTED, HemoJourneyStage.VICAR_REWARD)) {
				helper.assertTrue(!(boolean) shouldVerify.invoke(null, earned, earned.id()),
						"Earned " + earned.id() + " latch must skip destructive re-verification");
			}
			Method next = transitions.getMethod("next", HemoJourneyStage.class, boolean.class, boolean.class);
			helper.assertTrue(next.invoke(null, HemoJourneyStage.LIBER_CRAFTED, true, false)
					== HemoJourneyStage.LIBER_CRAFTED, "Failed transition must retain earned stage");
			helper.assertTrue(next.invoke(null, HemoJourneyStage.LIBER_CRAFTED, true, true)
					== HemoJourneyStage.HEMATIC_IRON_CRAFTED, "Retry must advance exactly once");
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError("Verified checkpoint latch seam is missing", exception);
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void completeExitCleansOwnedFixtureAndOutput(GameTestHelper helper) {
		ServerPlayer player = detachedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(4, 3, 4));
		try {
			var blood = HemoCapabilityAccess.requireBloodVolume(player);
			blood.setActive(true);
			blood.setBloodVolume(5000.0D);
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.FORMATION_PROJECTED, origin);
			ItemEntity output = spawn(helper, origin.getX() + 0.5D, origin.getY() + 1.5D,
					origin.getZ() - 0.05D, new ItemStack(ItemInit.sanguine_formation.get()));
			blood.setBloodVolume(4900.0D);
			invokeFixture("cleanupForExit", player, HemoJourneyStage.FORMATION_PROJECTED, origin);
			helper.assertTrue(output.isRemoved(), "COMPLETE/reset/clear exit must clean attributed output");
			helper.assertTrue(helper.getLevel().getBlockState(origin).isAir(), "Exit must clean owned platform");
		} finally {
			player.discard();
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void knownManipulationStateRoundTrip(GameTestHelper helper) {
		try {
			KnownManipulations known = new KnownManipulations();
			known.getKnownManips().put(ManipulationInit.blood_projection.get(), ManipLevel.BLANK);
			known.setSelectedManip(ManipulationInit.blood_projection.get());
			Class<?> seam = Class.forName("com.vincenthuto.hemomancy.gametest.journey.HemoJourneyManipulationState");
			Method capture = seam.getMethod("capture", KnownManipulations.class, net.minecraft.core.HolderLookup.Provider.class);
			Method reset = seam.getMethod("reset", KnownManipulations.class, net.minecraft.core.HolderLookup.Provider.class);
			Method apply = seam.getMethod("apply", KnownManipulations.class, ListTag.class,
					net.minecraft.core.HolderLookup.Provider.class);
			ListTag saved = (ListTag) capture.invoke(null, known, helper.getLevel().registryAccess());
			reset.invoke(null, known, helper.getLevel().registryAccess());
			helper.assertTrue(known.getKnownManips().isEmpty(), "Reset seam must clear learned manipulations");
			apply.invoke(null, known, saved, helper.getLevel().registryAccess());
			helper.assertTrue(known.doesListContainName(known.getKnownManips(), ManipulationInit.blood_projection.get()),
					"Apply/rollback seam must restore learned manipulation");
			helper.assertTrue(known.getSelectedManip().getName().equals("blood_projection"),
					"Apply/postverify seam must restore selected manipulation");
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError("Pure manipulation state seam is missing", exception);
		}
		helper.succeed();
	}

	private static void positiveCraftDelta(GameTestHelper helper, HemoJourneyStage stage,
			net.minecraft.world.item.Item output) {
		ServerPlayer player = detachedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(4, 3, 4));
		try {
			HemoJourneyFixtures.prepare(player, stage, origin);
			spawn(helper, origin.getX() + 0.5D, origin.getY() + 2.5D, origin.getZ() + 0.5D,
					new ItemStack(output));
			boolean captured = invokeBooleanFixture("captureExpectedOutputs", player, stage, origin);
			helper.assertTrue(HemoJourneyCheckpointRules.craftPassed(captured, true, true),
					"Exact craft delta plus a newly earned advancement must pass");
		} finally {
			invokeFixture("cleanupForExit", player, stage, origin);
			player.discard();
		}
		helper.succeed();
	}

	private static void settledCraftOutput(GameTestHelper helper, HemoJourneyStage stage,
			net.minecraft.world.item.Item output) {
		ServerPlayer player = detachedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(4, 3, 4));
		ItemEntity baseline = spawn(helper, origin.getX() + 1.0D, origin.getY() + 1.0D,
				origin.getZ() + 0.5D, new ItemStack(output));
		ItemEntity wrongCount = null;
		ItemEntity wrongItem = null;
		try {
			HemoJourneyFixtures.prepare(player, stage, origin);
			wrongCount = spawn(helper, origin.getX() - 0.4D, origin.getY() + 1.0D,
					origin.getZ() + 1.4D, new ItemStack(output, 2));
			wrongItem = spawn(helper, origin.getX(), origin.getY() + 1.0D,
					origin.getZ() + 0.5D, new ItemStack(Blocks.COBBLESTONE));
			ItemEntity settled = spawn(helper, origin.getX() + 0.5D, origin.getY() + 1.0D,
					origin.getZ() + 0.5D, new ItemStack(output));
			helper.assertTrue(invokeBooleanFixture("captureExpectedOutputs", player, stage, origin),
					"Expected settled craft output to be captured at platform height");
			invokeFixture("cleanupForExit", player, stage, origin);
			helper.assertTrue(settled.isRemoved(), "Captured settled craft output must be cleaned");
			helper.assertTrue(!baseline.isRemoved(), "Baseline craft output must be preserved");
			helper.assertTrue(!wrongCount.isRemoved(), "Wrong-count craft output must be preserved");
			helper.assertTrue(!wrongItem.isRemoved(), "Wrong-item craft output must be preserved");
		} finally {
			baseline.discard();
			if (wrongCount != null) wrongCount.discard();
			if (wrongItem != null) wrongItem.discard();
			HemoJourneyFixtures.cleanup(player, origin);
			player.discard();
		}
		helper.succeed();
	}

	private static BlockPos invokeJourneyLanding(BlockPos origin) {
		try {
			Class<?> controller = Class.forName(
					"com.vincenthuto.hemomancy.gametest.journey.HemoJourneyController");
			return (BlockPos) controller.getMethod("landing", BlockPos.class).invoke(null, origin);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError("Journey landing geometry seam is missing", exception);
		}
	}

	private static ItemEntity spawn(GameTestHelper helper, double x, double y, double z, ItemStack stack) {
		ItemEntity entity = new ItemEntity(helper.getLevel(), x, y, z, stack);
		helper.getLevel().addFreshEntity(entity);
		return entity;
	}

	private static HemoJourneyResult invokeVerifyClaiming(ServerPlayer player, HemoJourneyStage stage, BlockPos origin) {
		try {
			Method method = HemoJourneyChecks.class.getMethod("verify", ServerPlayer.class,
					HemoJourneyStage.class, BlockPos.class, boolean.class);
			return (HemoJourneyResult) method.invoke(null, player, stage, origin, true);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError("Claiming verification API is missing", exception);
		}
	}

	private static boolean invokeBooleanFixture(String methodName, ServerPlayer player,
			HemoJourneyStage stage, BlockPos origin) {
		try {
			Method method = HemoJourneyFixtures.class.getMethod(methodName, ServerPlayer.class,
					HemoJourneyStage.class, BlockPos.class);
			return (boolean) method.invoke(null, player, stage, origin);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError("Fixture ownership API is missing", exception);
		}
	}

	private static void invokeFixture(String methodName, ServerPlayer player, BlockPos origin) {
		try {
			HemoJourneyFixtures.class.getMethod(methodName, ServerPlayer.class, BlockPos.class)
					.invoke(null, player, origin);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError("Fixture cleanup API is missing", exception);
		}
	}

	private static void invokeFixture(String methodName, ServerPlayer player,
			HemoJourneyStage stage, BlockPos origin) {
		try {
			HemoJourneyFixtures.class.getMethod(methodName, ServerPlayer.class,
					HemoJourneyStage.class, BlockPos.class).invoke(null, player, stage, origin);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError("Fixture exit API is missing", exception);
		}
	}


	private static Block[][] ashPattern(Block center) {
		Block ash = BlockInit.befouling_ash_trail.get();
		return new Block[][] {
				{ ash, ash, ash },
				{ ash, center, ash },
				{ ash, ash, ash }
		};
	}

	private static void assertWall(GameTestHelper helper, BlockPos origin, Block[][] rows) {
		for (int row = 0; row < rows.length; row++) {
			for (int column = 0; column < rows[row].length; column++) {
				assertBlock(helper, origin.offset(column - 1, 3 - row, 0), rows[row][column]);
			}
		}
	}

	private static void assertFloor(GameTestHelper helper, BlockPos origin, Block[][] rows) {
		for (int z = 0; z < rows.length; z++) {
			for (int x = 0; x < rows[z].length; x++) {
				assertBlock(helper, origin.offset(x - 1, 1, z - 1), rows[z][x]);
			}
		}
	}

	private static void assertBlock(GameTestHelper helper, BlockPos pos, Block expected) {
		helper.assertTrue(helper.getLevel().getBlockState(pos).is(expected),
				"Expected " + expected + " at " + pos + " but found " + helper.getLevel().getBlockState(pos));
	}

	private static void withFixture(GameTestHelper helper, HemoJourneyStage stage, FixtureAssertion assertion) {
		ServerPlayer player = detachedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(4, 3, 4));
		try {
			invoke("prepare", player, stage, origin);
			assertion.check(origin, player);
			helper.succeed();
		} finally {
			try {
				invoke("cleanup", player, null, origin);
			} finally {
				player.discard();
			}
		}
	}

	private static void invoke(String methodName, ServerPlayer player, HemoJourneyStage stage, BlockPos origin) {
		try {
			Class<?> fixtures = Class.forName(FIXTURES_CLASS);
			Method method = methodName.equals("prepare")
					? fixtures.getMethod(methodName, ServerPlayer.class, HemoJourneyStage.class, BlockPos.class)
					: fixtures.getMethod(methodName, ServerPlayer.class, BlockPos.class);
			if (stage == null) method.invoke(null, player, origin);
			else method.invoke(null, player, stage, origin);
		} catch (ClassNotFoundException exception) {
			throw new AssertionError("Journey fixture preparation is absent", exception);
		} catch (NoSuchMethodException | IllegalAccessException exception) {
			throw new AssertionError("Journey fixture API is unavailable", exception);
		} catch (InvocationTargetException exception) {
			throw new AssertionError("Journey fixture operation failed", exception.getCause());
		}
	}

	private static ServerPlayer detachedTestPlayer(GameTestHelper helper) {
		ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
				new GameProfile(UUID.randomUUID(), "journey-fixture-player"), ClientInformation.createDefault());
		player.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY,
				helper.getLevel().dimension().location().toString());
		return player;
	}

	@FunctionalInterface
	private interface FixtureAssertion {
		void check(BlockPos origin, ServerPlayer player);
	}
}
