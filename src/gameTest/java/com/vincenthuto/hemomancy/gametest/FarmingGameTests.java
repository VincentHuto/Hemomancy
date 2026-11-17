package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.CatalystCropBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.FungalStrainBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.FungalStrainRules;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.FungalStrains;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.TemperCatalysts;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.world.farm.ClochePlanting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import java.util.Map;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.BloodGutterBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.VenousBasinBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.ground.BloodStainBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.PlantingBasinBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodProjectionItem;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.tile.BlockBloodVolume;
import com.vincenthuto.hemomancy.common.tile.BloodDrawRights;
import com.vincenthuto.hemomancy.common.tile.CoagulationRules;
import com.vincenthuto.hemomancy.common.tile.ReservoirBlood;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.MycelialLanternBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.decoration.VenousBasinBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineCondenserBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.plant.PlantingBasinBlockEntity;
import com.vincenthuto.hemomancy.common.world.farm.BloodFedGround;
import com.vincenthuto.hemomancy.common.world.farm.BloodFedGrowthRules;
import com.vincenthuto.hemomancy.common.world.farm.ScabDrip;
import com.vincenthuto.hemomancy.common.world.ground.GroundRunRules;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Core-loop Track 7, fed ground: a scab spike under fresh blood drips, feeding the soil below or setting into a
 * Condenser; a drip ceiling grows a field measurably faster than rain alone; a gourd grows only where blood reaches,
 * by its own growth or bone meal; a planting basin feeds at level 2 and pays for it, then clots; a worked gutter feeds
 * a planting basin beside it; the cloche grows a crop for its price and still fruits enzymes; Vital Effusion's Sculpting
 * soaks and grows a plant for a real player. Growth is driven by the crops' own random ticks through the NeoForge event.
 */
@GameTestHolder("farming_validation")
@PrefixGameTestTemplate(false)
public final class FarmingGameTests {
	private static final String EMPTY = "empty";
	private static final long GROWTH_SEED = 0x6772_6f77L;

	private FarmingGameTests() {
	}

	// ── Fixtures ──

	/** A clear room: a stone floor at y 0 over x 0..20, z 0..10 and air above it to y 10. */
	private static void clear(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (int x = 0; x <= 20; x++) {
			for (int z = 0; z <= 10; z++) {
				level.setBlock(at(helper, x, 0, z), Blocks.STONE.defaultBlockState(), 2);
				for (int y = 1; y <= 10; y++) level.setBlock(at(helper, x, y, z), Blocks.AIR.defaultBlockState(), 2);
			}
		}
	}

	private static BlockPos at(GameTestHelper helper, int x, int y, int z) {
		return helper.absolutePos(new BlockPos(x, y, z));
	}

	private static void set(GameTestHelper helper, BlockPos pos, BlockState state) {
		helper.getLevel().setBlock(pos, state, 3);
	}

	/** Moist farmland at (x, 1, z) with a fresh crop on it; returns the crop's position. */
	private static BlockPos crop(GameTestHelper helper, int x, int z, BlockState plant) {
		set(helper, at(helper, x, 1, z), Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE));
		BlockPos crop = at(helper, x, 2, z);
		set(helper, crop, plant);
		return crop;
	}

	private static BlockState spike(Direction tip, DripstoneThickness thickness) {
		return BlockInit.pointed_blood_scorched_scab.get().defaultBlockState()
				.setValue(PointedDripstoneBlock.TIP_DIRECTION, tip).setValue(PointedDripstoneBlock.THICKNESS, thickness);
	}

	/**
	 * A glass ceiling at y 6 over (x, z) with a worked basin of fresh blood standing on it and a one-block scab spike
	 * hanging under it; returns the spike's tip.
	 */
	private static BlockPos dripAbove(GameTestHelper helper, int x, int z, double blood) {
		set(helper, at(helper, x, 6, z), Blocks.GLASS.defaultBlockState());
		BlockPos basin = at(helper, x, 7, z);
		set(helper, basin, BlockInit.venous_basin.get().defaultBlockState().setValue(VenousBasinBlock.WORKED, true));
		((VenousBasinBlockEntity) helper.getLevel().getBlockEntity(basin)).configure().fill(blood);
		BlockPos tip = at(helper, x, 5, z);
		set(helper, tip, spike(Direction.DOWN, DripstoneThickness.TIP));
		return tip;
	}

	private static double basinBlood(GameTestHelper helper, int x, int z) {
		return ReservoirBlood.settled(helper.getLevel().getBlockEntity(at(helper, x, 7, z))).getBloodVolume();
	}

	/**
	 * The lowest block of the spike hanging at {@code top}. A drip can grow the spike (a random roll), after which the
	 * old tip is no longer a tip and drips nothing; the next drip falls from here.
	 */
	private static BlockPos lowestTip(GameTestHelper helper, BlockPos top) {
		BlockPos lowest = top;
		while (helper.getLevel().getBlockState(lowest.below()).is(BlockInit.pointed_blood_scorched_scab.get())) lowest = lowest.below();
		return lowest;
	}

	private static ScabDrip.Landing drip(GameTestHelper helper, BlockPos tip) {
		ServerLevel level = helper.getLevel();
		return ScabDrip.drip(level, tip, level.getBlockState(tip), level.random);
	}

	private static int age(GameTestHelper helper, BlockPos crop) {
		BlockState state = helper.getLevel().getBlockState(crop);
		if (state.getBlock() instanceof CropBlock block) return block.getAge(state);
		return state.hasProperty(StemBlock.AGE) ? state.getValue(StemBlock.AGE) : -1;
	}

	/**
	 * {@code trials} single random ticks of a fresh crop at {@code crop}; how many of them grew it. The crop's own growth
	 * roll (about one in thirteen for a stem in a planting basin) runs on a fixed seed, so a count compared against a
	 * threshold is the same every run instead of a binomial draw that sometimes falls short.
	 */
	private static int growths(GameTestHelper helper, BlockPos crop, BlockState fresh, int trials) {
		ServerLevel level = helper.getLevel();
		RandomSource rolls = RandomSource.create(GROWTH_SEED);
		int grew = 0;
		for (int i = 0; i < trials; i++) {
			level.setBlock(crop, fresh, 2);
			level.getBlockState(crop).randomTick(level, crop, rolls);
			if (age(helper, crop) > 0) grew++;
		}
		return grew;
	}

	/**
	 * Runs {@code trials} and passes once every crop in {@code crops} reads the light it needs to grow (raw brightness
	 * 9). The light engine works off the server thread, so a crop checked in the tick its room was cleared can read the
	 * old darkness under the template's floor and never grow.
	 */
	private static void whenLit(GameTestHelper helper, Runnable trials, BlockPos... crops) {
		helper.startSequence().thenWaitUntil(() -> {
			for (BlockPos crop : crops) {
				int light = helper.getLevel().getRawBrightness(crop, 0);
				helper.assertTrue(light >= 9, "the light at " + crop + " is still " + light);
			}
		}).thenExecute(trials).thenSucceed();
	}

	private static PlantingBasinBlockEntity plantingBasin(GameTestHelper helper, BlockPos pos, double blood) {
		set(helper, pos, BlockInit.planting_basin.get().defaultBlockState());
		PlantingBasinBlockEntity basin = (PlantingBasinBlockEntity) helper.getLevel().getBlockEntity(pos);
		if (blood > 0.0D) basin.configure().fill(blood);
		return basin;
	}

	// ── The drip ──

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aSpikeUnderFreshBloodDripsAndFeedsTheSoilBelow(GameTestHelper helper) {
		clear(helper);
		BlockPos wheat = crop(helper, 4, 4, Blocks.WHEAT.defaultBlockState());
		BlockPos tip = dripAbove(helper, 4, 4, 500.0D);
		helper.assertTrue(BloodFedGround.level(helper.getLevel(), wheat) == BloodFedGrowthRules.UNFED, "fed before any drip");
		ScabDrip.Landing landing = drip(helper, tip);
		helper.assertTrue(landing == ScabDrip.Landing.SOIL, "the drip did not land on the soil: " + landing);
		helper.assertTrue(Math.abs(500.0D - basinBlood(helper, 4, 4) - BloodFedGrowthRules.DRIP_ML) < 1.0E-6D,
				"one drip took " + (500.0D - basinBlood(helper, 4, 4)) + " mL from the basin above");
		helper.assertTrue(BloodFedGround.level(helper.getLevel(), wheat) == BloodFedGrowthRules.DRIP, "the soil was not fed at level 1");
		helper.assertTrue(BloodFedGround.stamped(helper.getLevel(), at(helper, 5, 1, 5)), "the splash did not feed the ring round it");
		helper.assertTrue(!BloodFedGround.stamped(helper.getLevel(), at(helper, 7, 1, 4)), "the splash reached too far");
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aSpikeWithNothingFreshAboveStaysDry(GameTestHelper helper) {
		clear(helper);
		crop(helper, 4, 4, Blocks.WHEAT.defaultBlockState());
		BlockPos tip = dripAbove(helper, 4, 4, 300.0D);
		BlockBloodVolume basin = ReservoirBlood.settled(helper.getLevel().getBlockEntity(at(helper, 4, 7, 4)));
		basin.backdate(helper.getLevel().getGameTime(), CoagulationRules.FULL_CLOT_TICKS);
		helper.assertTrue(drip(helper, tip) == ScabDrip.Landing.NONE, "clotted blood dripped");
		set(helper, at(helper, 4, 7, 4), Blocks.AIR.defaultBlockState());
		helper.assertTrue(drip(helper, tip) == ScabDrip.Landing.NONE, "a spike with nothing above dripped");
		helper.assertTrue(!BloodFedGround.stamped(helper.getLevel(), at(helper, 4, 1, 4)), "the soil was fed");
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aDripIntoACauldronSeatsACondenserAndSets(GameTestHelper helper) {
		clear(helper);
		BlockPos cauldron = at(helper, 4, 1, 4);
		set(helper, cauldron, Blocks.CAULDRON.defaultBlockState());
		BlockPos tip = dripAbove(helper, 4, 4, 500.0D);
		helper.assertTrue(drip(helper, tip) == ScabDrip.Landing.CONDENSER, "the drip did not reach the cauldron");
		helper.assertTrue(helper.getLevel().getBlockEntity(cauldron) instanceof SanguineCondenserBlockEntity,
				"the cauldron did not become a Condenser");
		ScabDrip.Landing second = drip(helper, lowestTip(helper, tip));
		helper.assertTrue(second == ScabDrip.Landing.CONDENSER, "the second drip did not reach the Condenser: " + second);
		BlockBloodVolume pool = ReservoirBlood.settled(helper.getLevel().getBlockEntity(cauldron));
		helper.assertTrue(Math.abs(pool.clotted() - 2 * BloodFedGrowthRules.DRIP_ML) < 1.0E-6D && pool.getBloodVolume() == 0.0D,
				"the drops did not set as clot: clot " + pool.clotted() + ", fresh " + pool.getBloodVolume());
		helper.assertTrue(((SanguineCondenserBlockEntity) helper.getLevel().getBlockEntity(cauldron)).owner() == null,
				"a drip from a tank nobody owns recorded an owner");
		helper.succeed();
	}

	/**
	 * A drip from an owned tank seats a Condenser that belongs to the tank's owner: a stranger may not draw from it and
	 * the owner may. Once it has given up everything it set, it goes back to a cauldron (a drip counts as filling it),
	 * and the next drip seats it again.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aDripFromAnOwnedTankSeatsTheOwnersCondenserWhichRevertsWhenSpent(GameTestHelper helper) {
		clear(helper);
		ServerLevel level = helper.getLevel();
		BlockPos cauldron = at(helper, 4, 1, 4);
		set(helper, cauldron, Blocks.CAULDRON.defaultBlockState());
		BlockPos tip = dripAbove(helper, 4, 4, 500.0D);
		try (CapturedPlayer owner = CapturedPlayer.create(helper).online(); CapturedPlayer stranger = CapturedPlayer.create(helper)) {
			ReservoirBlood.settled(level.getBlockEntity(at(helper, 4, 7, 4))).setOwner(owner.player.getUUID());
			ScabDrip.recordPlacer(level, tip, owner.player.getUUID());
			helper.assertTrue(drip(helper, tip) == ScabDrip.Landing.CONDENSER, "the drip did not reach the cauldron");
			SanguineCondenserBlockEntity condenser = (SanguineCondenserBlockEntity) level.getBlockEntity(cauldron);
			helper.assertTrue(owner.player.getUUID().equals(condenser.owner()), "the Condenser did not take the tank's owner: " + condenser.owner());
			helper.assertTrue(!BloodDrawRights.mayDraw(stranger.player, condenser), "a stranger may draw from the owner's dripped Condenser");
			helper.assertTrue(BloodDrawRights.mayDraw(owner.player, condenser), "the owner may not draw from their dripped Condenser");
			// Everything it set is taken (as its Formations would take it): it is spent, and goes back to a cauldron.
			BlockBloodVolume pool = ReservoirBlood.settled(condenser);
			pool.scrapeClot(pool.clotted());
			helper.assertTrue(condenser.revertIfSpent() && level.getBlockState(cauldron).is(Blocks.CAULDRON),
					"a spent drip-made Condenser did not go back to a cauldron: " + level.getBlockState(cauldron));
			helper.assertTrue(drip(helper, lowestTip(helper, tip)) == ScabDrip.Landing.CONDENSER
					&& level.getBlockEntity(cauldron) instanceof SanguineCondenserBlockEntity, "the next drip did not seat it again");
		}
		helper.succeed();
	}

	/**
	 * CQ-3, NQ-01-15: a spike drips only from a tank its placer may draw. Under an owned tank, a spike nobody placed and
	 * a stranger's spike stay dry, and the owner's drips; when the spike's root goes, its placer is forgotten.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aSpikeDripsOnlyFromATankItsPlacerMayDraw(GameTestHelper helper) {
		clear(helper);
		ServerLevel level = helper.getLevel();
		crop(helper, 4, 4, Blocks.WHEAT.defaultBlockState());
		BlockPos tip = dripAbove(helper, 4, 4, 500.0D);
		try (CapturedPlayer owner = CapturedPlayer.create(helper).online(); CapturedPlayer stranger = CapturedPlayer.create(helper)) {
			ReservoirBlood.settled(level.getBlockEntity(at(helper, 4, 7, 4))).setOwner(owner.player.getUUID());
			helper.assertTrue(drip(helper, tip) == ScabDrip.Landing.NONE && !ScabDrip.fed(level, tip),
					"a spike nobody placed dripped from an owned tank");
			ScabDrip.recordPlacer(level, tip, stranger.player.getUUID());
			helper.assertTrue(drip(helper, tip) == ScabDrip.Landing.NONE, "a stranger's spike dripped from the owner's tank");
			helper.assertTrue(Math.abs(basinBlood(helper, 4, 4) - 500.0D) < 1.0E-6D, "the stranger's spike took blood");
			set(helper, tip, Blocks.AIR.defaultBlockState());
			helper.assertTrue(ScabDrip.placer(level, tip) == null, "a broken spike kept its placer");
			set(helper, tip, spike(Direction.DOWN, DripstoneThickness.TIP));
			ScabDrip.recordPlacer(level, tip, owner.player.getUUID());
			helper.assertTrue(drip(helper, tip) == ScabDrip.Landing.SOIL, "the owner's spike did not drip");
			BlockPos below = lowestTip(helper, tip).below();
			set(helper, below, spike(Direction.DOWN, DripstoneThickness.TIP));
			ScabDrip.recordPlacer(level, below, stranger.player.getUUID());
			helper.assertTrue(owner.player.getUUID().equals(ScabDrip.placer(level, tip)),
					"a block added below the spike took it from its first placer");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aFedSpikeGrowsLonger(GameTestHelper helper) {
		clear(helper);
		// Glass under the drip, not bare stone: on stone the drops would also raise a floor spike, and a floor spike two
		// blocks up stops the hanging one growing at all (round two P7), which is the floor-spike test's business.
		set(helper, at(helper, 4, 0, 4), Blocks.GLASS.defaultBlockState());
		BlockPos tip = dripAbove(helper, 4, 4, 1_500.0D);
		ServerLevel level = helper.getLevel();
		RandomSource rolls = RandomSource.create(GROWTH_SEED);
		boolean grew = false;
		for (int i = 0; i < 200 && !grew; i++) {
			BlockPos lowest = lowestTip(helper, tip);
			ScabDrip.drip(level, lowest, level.getBlockState(lowest), rolls);
			grew = level.getBlockState(tip.below()).is(BlockInit.pointed_blood_scorched_scab.get());
		}
		helper.assertTrue(grew, "a fed spike never grew in 200 drips");
		helper.assertTrue(helper.getLevel().getBlockState(tip).getValue(PointedDripstoneBlock.THICKNESS) != DripstoneThickness.TIP,
				"the old tip did not thicken");
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void aDripCeilingGrowsAFieldMeasurablyFasterThanRain(GameTestHelper helper) {
		clear(helper);
		BlockState fresh = Blocks.WHEAT.defaultBlockState();
		BlockPos dry = crop(helper, 2, 4, fresh);
		BlockPos fed = crop(helper, 10, 4, fresh);
		BlockPos tip = dripAbove(helper, 10, 4, 500.0D);
		drip(helper, tip);
		whenLit(helper, () -> {
			int dryGrowth = growths(helper, dry, fresh, 300);
			int fedGrowth = growths(helper, fed, fresh, 300);
			helper.assertTrue(fedGrowth >= dryGrowth * 1.6D && fedGrowth > dryGrowth + 30,
					"a fed crop grew " + fedGrowth + " times in 300 checks against " + dryGrowth + " for rain alone");
		}, dry, fed);
	}

	// ── The gourd ──

	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void aGourdGrowsOnlyWhereBloodReaches(GameTestHelper helper) {
		clear(helper);
		BlockState stem = BlockInit.gourd_stem.get().defaultBlockState();
		BlockPos dry = crop(helper, 2, 4, stem);
		BlockPos soil = at(helper, 10, 1, 4);
		plantingBasin(helper, soil, 400.0D);
		BlockPos fed = at(helper, 10, 2, 4);
		set(helper, fed, stem);
		// A wet stain beside the stem's soil feeds it too, slowly.
		BlockPos stained = crop(helper, 16, 4, stem);
		set(helper, at(helper, 17, 2, 4), BlockInit.blood_stain.get().defaultBlockState()
				.setValue(MultifaceBlock.getFaceProperty(Direction.DOWN), true).setValue(BloodStainBlock.AGE, GroundRunRules.WET));
		set(helper, at(helper, 17, 1, 4), Blocks.DIRT.defaultBlockState());
		whenLit(helper, () -> {
			helper.assertTrue(growths(helper, dry, stem, 200) == 0, "an unfed gourd stem grew");
			helper.getLevel().setBlock(dry, stem, 2);
			helper.assertTrue(!BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), helper.getLevel(), dry, null)
					&& age(helper, dry) == 0, "bone meal grew an unfed gourd stem");
			int fedGrowth = growths(helper, fed, stem, 200);
			helper.assertTrue(fedGrowth > 10, "a gourd stem in a fed basin grew only " + fedGrowth + " times in 200 checks");
			helper.assertTrue(BloodFedGround.level(helper.getLevel(), stained) == BloodFedGrowthRules.DRIP,
					"a wet stain beside it did not feed it");
			helper.assertTrue(growths(helper, stained, stem, 300) > 0, "a stain-fed gourd stem never grew");
		}, dry, fed, stained);
	}

	// ── Planting basins ──

	/**
	 * The client predicts bone meal on a crop that needs blood from what block states show ({@code visiblyFed}): unfed
	 * soil reads unfed, a wet stain and a basin showing fed read fed, and soil fed only by a stamp reads unfed there
	 * though the server feeds it (an under-prediction the server corrects, never a ghost growth).
	 */
	@GameTest(template = EMPTY, timeoutTicks = 80)
	public static void theClientReadsOnlyTheFeedingItCanSee(GameTestHelper helper) {
		clear(helper);
		ServerLevel level = helper.getLevel();
		BlockState stem = BlockInit.gourd_stem.get().defaultBlockState();
		BlockPos dry = crop(helper, 2, 4, stem);
		BlockPos stamped = crop(helper, 6, 4, stem);
		BloodFedGround.stamp(level, BloodFedGround.soilOf(stamped), 1_200);
		BlockPos stained = crop(helper, 16, 4, stem);
		set(helper, at(helper, 17, 2, 4), BlockInit.blood_stain.get().defaultBlockState()
				.setValue(MultifaceBlock.getFaceProperty(Direction.DOWN), true).setValue(BloodStainBlock.AGE, GroundRunRules.WET));
		set(helper, at(helper, 17, 1, 4), Blocks.DIRT.defaultBlockState());
		plantingBasin(helper, at(helper, 10, 1, 4), 400.0D);
		BlockPos inBasin = at(helper, 10, 2, 4);
		set(helper, inBasin, stem);
		helper.assertTrue(!BloodFedGround.visiblyFed(level, dry), "unfed soil read fed to the client");
		helper.assertTrue(BloodFedGround.visiblyFed(level, stained), "a wet stain beside the stem read unfed to the client");
		helper.assertTrue(BloodFedGround.level(level, stamped) == BloodFedGrowthRules.DRIP, "the stamp did not feed the stem on the server");
		helper.assertTrue(!BloodFedGround.visiblyFed(level, stamped), "a stamp, which the client cannot see, read fed to it");
		helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.getBlockState(at(helper, 10, 1, 4)).getValue(PlantingBasinBlock.FED),
				"the fresh basin never showed fed")).thenExecute(() -> helper.assertTrue(BloodFedGround.visiblyFed(level, inBasin),
				"a basin showing fed read unfed to the client")).thenSucceed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 240)
	public static void aPlantingBasinFeedsAtLevelTwoPaysAndClots(GameTestHelper helper) {
		clear(helper);
		ServerLevel level = helper.getLevel();
		BlockPos soil = at(helper, 4, 1, 4);
		PlantingBasinBlockEntity basin = plantingBasin(helper, soil, 400.0D);
		BlockPos wheat = at(helper, 4, 2, 4);
		set(helper, wheat, Blocks.WHEAT.defaultBlockState());
		helper.assertTrue(level.getBlockState(wheat).is(Blocks.WHEAT), "wheat would not stand in a planting basin");
		helper.assertTrue(BloodFedGround.level(level, wheat) == BloodFedGrowthRules.BASIN, "a fresh basin did not feed at level 2");
		BlockPos beside = crop(helper, 5, 4, Blocks.WHEAT.defaultBlockState());
		helper.assertTrue(BloodFedGround.level(level, beside) == BloodFedGrowthRules.BASIN, "the soil beside a fresh basin was not fed");
		helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.getRawBrightness(wheat, 0) >= 9,
				"the light at the wheat is still " + level.getRawBrightness(wheat, 0))).thenExecute(() -> {
			growths(helper, wheat, Blocks.WHEAT.defaultBlockState(), 40);
			double spent = 400.0D - basin.configure().getBloodVolume();
			helper.assertTrue(spent > 0.0D && Math.abs(spent / BloodFedGrowthRules.BASIN_GROWTH_ML - Math.rint(spent / BloodFedGrowthRules.BASIN_GROWTH_ML)) < 1.0E-6D,
					"the basin's growth was not paid " + BloodFedGrowthRules.BASIN_GROWTH_ML + " mL a time: spent " + spent);
			basin.configure().backdate(level.getGameTime(), CoagulationRules.FULL_CLOT_TICKS);
			helper.assertTrue(BloodFedGround.level(level, wheat) == BloodFedGrowthRules.UNFED, "a clotted basin still fed");
			helper.assertTrue(basin.configure().clotted() > 0.0D, "the basin's blood did not set");
		}).thenExecuteAfter(25, () -> helper.assertTrue(!level.getBlockState(soil).getValue(PlantingBasinBlock.FED),
				"a clotted basin still shows fed")).thenSucceed();
	}

	/** A ripe stem with nowhere to fruit grows nothing more, so the basin under it never pays for a growth check. */
	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void aPlantingBasinNeverPaysForARipeStem(GameTestHelper helper) {
		clear(helper);
		ServerLevel level = helper.getLevel();
		PlantingBasinBlockEntity basin = plantingBasin(helper, at(helper, 4, 1, 4), 400.0D);
		BlockPos stem = at(helper, 4, 2, 4);
		BlockState ripe = Blocks.PUMPKIN_STEM.defaultBlockState().setValue(StemBlock.AGE, StemBlock.MAX_AGE);
		set(helper, stem, ripe);
		helper.assertTrue(BloodFedGround.level(level, stem) == BloodFedGrowthRules.BASIN, "the basin did not feed its stem");
		for (int i = 0; i < 300; i++) {
			level.setBlock(stem, ripe, 2);
			level.getBlockState(stem).randomTick(level, stem, level.random);
		}
		helper.assertTrue(Math.abs(basin.configure().getBloodVolume() - 400.0D) < 1.0E-6D,
				"a ripe stem with nowhere to fruit cost the basin " + (400.0D - basin.configure().getBloodVolume()) + " mL");
		// A growing stem still pays for the growth it buys.
		BlockState young = Blocks.PUMPKIN_STEM.defaultBlockState();
		whenLit(helper, () -> {
			growths(helper, stem, young, 60);
			helper.assertTrue(basin.configure().getBloodVolume() < 400.0D, "a growing stem bought no growth from the basin");
		}, stem);
	}

	@GameTest(template = EMPTY, timeoutTicks = 80)
	public static void aWorkedGutterFeedsAPlantingBasinBesideIt(GameTestHelper helper) {
		clear(helper);
		ServerLevel level = helper.getLevel();
		for (int x = 3; x <= 6; x++) {
			set(helper, at(helper, x, 1, 4), BlockInit.blood_gutter.get().defaultBlockState().setValue(BloodGutterBlock.WORKED, true)
					.setValue(BloodGutterBlock.EAST, x < 6).setValue(BloodGutterBlock.WEST, x > 3));
		}
		BlockPos bed = at(helper, 6, 1, 5);
		PlantingBasinBlockEntity basin = plantingBasin(helper, bed, 0.0D);
		BlockPos spout = at(helper, 2, 2, 4);
		set(helper, spout, BlockInit.venous_basin.get().defaultBlockState().setValue(VenousBasinBlock.WORKED, true)
				.setValue(VenousBasinBlock.FACING, Direction.EAST));
		((VenousBasinBlockEntity) level.getBlockEntity(spout)).configure().fill(1_500.0D);
		helper.runAfterDelay(45, () -> {
			helper.assertTrue(basin.configure().getBloodVolume() > 0.0D, "the gutter did not feed the planting basin beside it");
			helper.assertTrue(VenousBasinBlock.feedingBasin(level, at(helper, 5, 1, 4)) == level.getBlockEntity(spout),
					"a wet gutter does not know the basin that feeds it");
			helper.succeed();
		});
	}

	// ── The cloche ──

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void theClocheGrowsACropForItsPriceAndStillFruitsEnzymes(GameTestHelper helper) {
		clear(helper);
		ServerLevel level = helper.getLevel();
		BlockPos pos = at(helper, 4, 1, 4);
		set(helper, pos, BlockInit.mycelial_lantern.get().defaultBlockState());
		MycelialLanternBlockEntity cloche = (MycelialLanternBlockEntity) level.getBlockEntity(pos);
		ReservoirBlood.settled(cloche).setMaxBloodVolume(1_200.0D);
		ReservoirBlood.settled(cloche).fill(1_000.0D);
		helper.assertTrue(cloche.canPlaceItem(MycelialLanternBlockEntity.SLOT_CULTURE, new ItemStack(Items.WHEAT_SEEDS)),
				"the cloche refuses seeds");
		cloche.setItem(MycelialLanternBlockEntity.SLOT_CULTURE, new ItemStack(Items.WHEAT_SEEDS));
		int ticks = BloodFedGrowthRules.clocheTicksToHarvest(7) + BloodFedGrowthRules.CLOCHE_STAGE_TICKS;
		for (int i = 0; i < ticks; i++) MycelialLanternBlockEntity.serverTick(level, pos, level.getBlockState(pos), cloche);
		ItemStack output = cloche.getOutputStack();
		helper.assertTrue(output.is(Items.WHEAT), "the cloche harvested " + output + " instead of wheat");
		double spent = 1_000.0D - ReservoirBlood.settled(cloche).getBloodVolume();
		helper.assertTrue(Math.abs(spent - BloodFedGrowthRules.clocheBloodToHarvest(7)) < 1.0E-6D,
				"a crop under the bell cost " + spent + " mL");
		helper.assertTrue(cloche.getPlantStage() == 0, "the cloche did not sow again");
		// Spores still fruit enzymes: the First Culture path is untouched.
		cloche.setItem(MycelialLanternBlockEntity.SLOT_OUTPUT, ItemStack.EMPTY);
		cloche.setItem(MycelialLanternBlockEntity.SLOT_CULTURE, new ItemStack(ItemInit.ferric_spores.get()));
		for (int i = 0; i < 5; i++) MycelialLanternBlockEntity.serverTick(level, pos, level.getBlockState(pos), cloche);
		helper.assertTrue(cloche.getCraftingProgress() == 5 && cloche.plantedState() == null,
				"a culture no longer fruits in the Lantern (progress " + cloche.getCraftingProgress() + ")");
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aStarvedClocheWaits(GameTestHelper helper) {
		clear(helper);
		ServerLevel level = helper.getLevel();
		BlockPos pos = at(helper, 4, 1, 4);
		set(helper, pos, BlockInit.mycelial_lantern.get().defaultBlockState());
		MycelialLanternBlockEntity cloche = (MycelialLanternBlockEntity) level.getBlockEntity(pos);
		ReservoirBlood.settled(cloche).setMaxBloodVolume(1_200.0D);
		ReservoirBlood.settled(cloche).fill(40.0D);
		cloche.setItem(MycelialLanternBlockEntity.SLOT_CULTURE, new ItemStack(Items.CARROT));
		for (int i = 0; i < 6 * BloodFedGrowthRules.CLOCHE_STAGE_TICKS; i++) {
			MycelialLanternBlockEntity.serverTick(level, pos, level.getBlockState(pos), cloche);
		}
		helper.assertTrue(cloche.getPlantStage() == 1, "with 40 mL the bell grew " + cloche.getPlantStage() + " stages");
		helper.succeed();
	}

	// ── Vital Effusion ──

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void vitalEffusionSoaksAndGrowsAGourdForARealPlayer(GameTestHelper helper) {
		clear(helper);
		BlockState stem = BlockInit.gourd_stem.get().defaultBlockState();
		BlockPos gourd = crop(helper, 6, 4, stem);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "vital_effusion", 1_000.0D);
			Vec3 feet = Vec3.atBottomCenterOf(at(helper, 4, 2, 4));
			player.setPos(feet.x, feet.y, feet.z);
			player.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(gourd.getX() + 0.5D, gourd.getY() + 0.06D, gourd.getZ() + 0.5D));
			player.setOldPosAndRot();
			player.yHeadRotO = player.getYHeadRot();
			for (int i = 0; i < 3; i++) BloodProjectionItem.projectFromEntity(player.level(), player, 5.0D, 100.0D);
			helper.assertTrue(age(helper, gourd) > 0, "the Sculpting did not grow the gourd stem");
			helper.assertTrue(BloodFedGround.stamped(helper.getLevel(), at(helper, 6, 1, 4)), "the Sculpting did not soak the soil");
			// One pulse in three ticks (cadence 20), priced by the cost ledger (alignment trims the 15 mL base).
			double spent = 1_000.0D - HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
			helper.assertTrue(spent > 0.0D && spent <= 15.0D + 1.0E-6D, "one pulse in three ticks cost " + spent + " mL");
		}
		helper.succeed();
	}

	/** CQ-2: a plant the player may not bone-meal is refused before the price, so nothing is paid, soaked or grown. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void vitalEffusionRefusesBeforeAnyBloodWhereThePlayerMayNotBuild(GameTestHelper helper) {
		clear(helper);
		BlockState stem = BlockInit.gourd_stem.get().defaultBlockState();
		BlockPos gourd = crop(helper, 6, 4, stem);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "vital_effusion", 1_000.0D);
			player.setGameMode(net.minecraft.world.level.GameType.ADVENTURE);
			Vec3 feet = Vec3.atBottomCenterOf(at(helper, 4, 2, 4));
			player.setPos(feet.x, feet.y, feet.z);
			player.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(gourd.getX() + 0.5D, gourd.getY() + 0.06D, gourd.getZ() + 0.5D));
			player.setOldPosAndRot();
			player.yHeadRotO = player.getYHeadRot();
			helper.assertTrue(com.vincenthuto.hemomancy.common.manipulation.verb.SculptingDispatcher.currentClaim(player,
					com.vincenthuto.hemomancy.common.manipulation.verb.Verb.PROJECT).isPresent(),
					"Vital Effusion did not claim the stem, so the guard was never reached");
			float[] temper = HemoCapabilityAccess.getBloodTemper(player).snapshot();
			double spent = BloodProjectionItem.projectFromEntity(player.level(), player, 5.0D, 100.0D);
			double blood = HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
			helper.assertTrue(spent == 0.0D && blood == 1_000.0D, "a refused growth was paid for: " + blood);
			helper.assertTrue(java.util.Arrays.equals(temper, HemoCapabilityAccess.getBloodTemper(player).snapshot()),
					"a refused growth spent Temper");
			helper.assertTrue(captured.said().contains(StaffMiningGameTests.REFUSED), "the refusal was silent: " + captured.said());
			helper.assertTrue(age(helper, gourd) == 0, "a refused growth grew the stem");
			helper.assertTrue(!BloodFedGround.stamped(helper.getLevel(), at(helper, 6, 1, 4)), "a refused growth soaked the soil");
		}
		helper.succeed();
	}

	// ── Strain beds (core-loop round two, P7) ──

	private static BlockState strain(FungalStrains strain, int age) {
		return BlockInit.strain(strain).get().defaultBlockState().setValue(FungalStrainBlock.AGE, age);
	}

	/** A real player's spore roots only on its own group's substrate, and on nothing else. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aStrainPlantsOnlyOnItsOwnSubstrate(GameTestHelper helper) {
		clear(helper);
		ServerLevel level = helper.getLevel();
		Map<FungalStrains.Substrate, BlockState> grounds = Map.of(
				FungalStrains.Substrate.FLESH, BlockInit.ravenous_heartwood.get().defaultBlockState(),
				FungalStrains.Substrate.BRINED, BlockInit.salt_stained_timber.get().defaultBlockState(),
				FungalStrains.Substrate.COMMON, Blocks.PODZOL.defaultBlockState());
		List<BlockState> tried = List.of(grounds.get(FungalStrains.Substrate.FLESH), grounds.get(FungalStrains.Substrate.BRINED),
				grounds.get(FungalStrains.Substrate.COMMON), Blocks.DIRT.defaultBlockState(), Blocks.FARMLAND.defaultBlockState());
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			int x = 2;
			for (FungalStrains probe : List.of(FungalStrains.VIVACIOUS, FungalStrains.NEUROTIC, FungalStrains.INCANDESCENT)) {
				for (BlockState ground : tried) {
					BlockPos floor = at(helper, x, 1, 4);
					set(helper, floor, ground);
					player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(BuiltInRegistries.ITEM.get(Hemomancy.rloc(probe.sporeId())), 4));
					player.getMainHandItem().useOn(new UseOnContext(level, player, InteractionHand.MAIN_HAND, player.getMainHandItem(),
							new BlockHitResult(Vec3.atCenterOf(floor).add(0.0D, 0.5D, 0.0D), Direction.UP, floor, false)));
					boolean own = ground == grounds.get(probe.substrate());
					boolean rooted = level.getBlockState(floor.above()).is(BlockInit.strain(probe).get());
					helper.assertTrue(rooted == own, probe + " on " + ground + (own ? " did not root" : " rooted"));
					x++;
				}
			}
		}
		helper.succeed();
	}

	/**
	 * A strain never stirs unfed, by its own growth or by bone meal; under a drip it creeps (a third of its checks); with
	 * a fresh planting basin beside its wood it grows at its own pace. Strains need no light, so nothing waits for it.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void aStrainGrowsOnlyWhereBloodReaches(GameTestHelper helper) {
		clear(helper);
		ServerLevel level = helper.getLevel();
		BlockState fresh = strain(FungalStrains.FERVENT, 0);
		set(helper, at(helper, 2, 1, 4), BlockInit.ravenous_heartwood.get().defaultBlockState());
		BlockPos dry = at(helper, 2, 2, 4);
		set(helper, dry, fresh);
		set(helper, at(helper, 8, 1, 4), BlockInit.ravenous_heartwood.get().defaultBlockState());
		BlockPos dripped = at(helper, 8, 2, 4);
		set(helper, dripped, fresh);
		BlockPos tip = dripAbove(helper, 8, 4, 500.0D);
		helper.assertTrue(drip(helper, tip) == ScabDrip.Landing.SOIL, "a drip on a strain did not feed its wood");
		set(helper, at(helper, 14, 1, 4), BlockInit.ravenous_heartwood.get().defaultBlockState());
		BlockPos basinFed = at(helper, 14, 2, 4);
		set(helper, basinFed, fresh);
		plantingBasin(helper, at(helper, 15, 1, 4), 400.0D);
		helper.assertTrue(BloodFedGround.level(level, dry) == BloodFedGrowthRules.UNFED, "the dry strain read fed");
		helper.assertTrue(BloodFedGround.level(level, dripped) == BloodFedGrowthRules.DRIP, "the drip did not feed the strain at level 1");
		helper.assertTrue(BloodFedGround.level(level, basinFed) == BloodFedGrowthRules.BASIN, "the basin beside the wood did not feed at level 2");
		helper.assertTrue(strainGrowths(helper, dry, fresh, 300) == 0, "an unfed strain grew");
		level.setBlock(dry, fresh, 2);
		helper.assertTrue(!BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), level, dry, null)
				&& level.getBlockState(dry).getValue(FungalStrainBlock.AGE) == 0, "bone meal woke an unfed strain");
		int slow = strainGrowths(helper, dripped, fresh, 300);
		int full = strainGrowths(helper, basinFed, fresh, 300);
		helper.assertTrue(slow > 0, "a drip-fed strain never grew in 300 checks");
		// The basin-fed count is the strain's own seeded roll; the drip keeps a share of those same wins.
		helper.assertTrue(full >= 30 && slow < full, "a basin-fed strain grew " + full + " times against " + slow + " under a drip");
		level.setBlock(basinFed, fresh, 2);
		helper.assertTrue(BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), level, basinFed, null)
				&& level.getBlockState(basinFed).getValue(FungalStrainBlock.AGE) == 1, "bone meal did not grow a fed strain a stage");
		helper.succeed();
	}

	/** {@link #growths} for a strain, which reads its own {@code age}. */
	private static int strainGrowths(GameTestHelper helper, BlockPos crop, BlockState fresh, int trials) {
		ServerLevel level = helper.getLevel();
		RandomSource rolls = RandomSource.create(GROWTH_SEED);
		int grew = 0;
		for (int i = 0; i < trials; i++) {
			level.setBlock(crop, fresh, 2);
			level.getBlockState(crop).randomTick(level, crop, rolls);
			if (level.getBlockState(crop).getValue(FungalStrainBlock.AGE) > 0) grew++;
		}
		return grew;
	}

	/**
	 * A ripe strain always returns its spore and sometimes a second; an unripe one returns just the one planted. Cutting a
	 * ripe one is the strain-bed first. The spore stays an enzyme culture to the cloche, not a crop it plants.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aRipeStrainReturnsItsSporeAndSometimesAnother(GameTestHelper helper) {
		clear(helper);
		ServerLevel level = helper.getLevel();
		BlockPos pos = at(helper, 4, 2, 4);
		set(helper, at(helper, 4, 1, 4), BlockInit.salt_stained_timber.get().defaultBlockState());
		Item spore = BuiltInRegistries.ITEM.get(Hemomancy.rloc(FungalStrains.FRIGID.sporeId()));
		LootTable table = level.getServer().reloadableRegistries().getLootTable(BlockInit.strain(FungalStrains.FRIGID).get().getLootTable());
		int ones = 0;
		int twos = 0;
		for (long seed = 0; seed < 200; seed++) {
			int ripe = count(table.getRandomItems(blockLoot(level, pos, strain(FungalStrains.FRIGID, FungalStrainRules.MAX_AGE)), spread(seed)), spore);
			int young = count(table.getRandomItems(blockLoot(level, pos, strain(FungalStrains.FRIGID, 1)), spread(seed)), spore);
			helper.assertTrue(young == 1, "an unripe strain returned " + young + " spores");
			helper.assertTrue(ripe == 1 || ripe == 2, "a ripe strain returned " + ripe + " spores");
			if (ripe == 1) ones++;
			else twos++;
		}
		helper.assertTrue(ones > 60 && twos > 60, "ripe returns were " + ones + " single and " + twos + " double in 200");
		helper.assertTrue(!ClochePlanting.plantable(new ItemStack(spore)), "the cloche would plant a spore instead of culturing it");
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			player.setGameMode(GameType.SURVIVAL);
			HemoCapabilityAccess.getBloodVolume(player).setActive(false);
			set(helper, pos, strain(FungalStrains.FRIGID, FungalStrainRules.MAX_AGE));
			helper.assertTrue(player.gameMode.destroyBlock(pos), "the player could not cut the strain");
			helper.assertTrue(!HarbingerAdvancementGranter.hasAdvancement(player, HarbingerAdvancementGranter.ADV_FIRST_STRAIN_BED),
					"a player without active blood earned the strain-bed first");
			HemoCapabilityAccess.getBloodVolume(player).setActive(true);
			set(helper, pos, strain(FungalStrains.FRIGID, FungalStrainRules.MAX_AGE - 1));
			helper.assertTrue(player.gameMode.destroyBlock(pos), "the player could not cut the young strain");
			helper.assertTrue(!HarbingerAdvancementGranter.hasAdvancement(player, HarbingerAdvancementGranter.ADV_FIRST_STRAIN_BED),
					"cutting an unripe strain granted the strain-bed first");
			set(helper, pos, strain(FungalStrains.FRIGID, FungalStrainRules.MAX_AGE));
			helper.assertTrue(player.gameMode.destroyBlock(pos), "the player could not cut the strain");
			helper.assertTrue(HarbingerAdvancementGranter.hasAdvancement(player, HarbingerAdvancementGranter.ADV_FIRST_STRAIN_BED),
					"cutting a ripe strain did not grant the strain-bed first");
		}
		helper.succeed();
	}

	/**
	 * A loot seed from a counter: Java's random gives nearly the same first float for neighbouring seeds, so counting
	 * seeds 0, 1, 2... would roll one chance the same way every time.
	 */
	private static long spread(long seed) {
		return (seed + 1L) * 0x9E37_79B9_7F4A_7C15L;
	}

	private static LootParams blockLoot(ServerLevel level, BlockPos pos, BlockState state) {
		return new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
				.withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withParameter(LootContextParams.BLOCK_STATE, state)
				.create(LootContextParamSets.BLOCK);
	}

	private static int count(List<ItemStack> drops, Item item) {
		int total = 0;
		for (ItemStack drop : drops) if (drop.is(item)) total += drop.getCount();
		return total;
	}

	/**
	 * Glowroot never grows unfed and grows in a fresh planting basin; it is its own seed, ripe it returns two or three,
	 * and it dissolves as a Lux catalyst worth 2 (emberpod as Flammeus). The cloche grows it like any crop.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void aCatalystCropGrowsInABasinAndItsHarvestIsAWeakCatalyst(GameTestHelper helper) {
		clear(helper);
		ServerLevel level = helper.getLevel();
		BlockState fresh = BlockInit.glowroot.get().defaultBlockState();
		BlockPos dry = crop(helper, 2, 4, fresh);
		plantingBasin(helper, at(helper, 10, 1, 4), 400.0D);
		BlockPos fed = at(helper, 10, 2, 4);
		set(helper, fed, fresh);
		helper.assertTrue(level.getBlockState(fed).is(BlockInit.glowroot.get()), "glowroot would not stand in a planting basin");
		TemperCatalysts.Catalyst lux = TemperCatalysts.of(new ItemStack(ItemInit.glowroot.get()));
		TemperCatalysts.Catalyst flammeus = TemperCatalysts.of(new ItemStack(ItemInit.emberpod.get()));
		helper.assertTrue(lux != null && lux.school() == EnumBloodTemper.LUX && lux.worth() == 2, "glowroot is not a weak Lux catalyst: " + lux);
		helper.assertTrue(flammeus != null && flammeus.school() == EnumBloodTemper.FLAMMEUS && flammeus.worth() == 2,
				"emberpod is not a weak Flammeus catalyst: " + flammeus);
		helper.assertTrue(ClochePlanting.plantable(new ItemStack(ItemInit.emberpod.get())), "the cloche will not grow emberpod");
		LootTable table = level.getServer().reloadableRegistries().getLootTable(BlockInit.glowroot.get().getLootTable());
		for (long seed = 0; seed < 50; seed++) {
			int ripe = count(table.getRandomItems(blockLoot(level, fed, fresh.setValue(CatalystCropBlock.AGE, CatalystCropBlock.MAX_AGE)), spread(seed)),
					ItemInit.glowroot.get());
			int young = count(table.getRandomItems(blockLoot(level, fed, fresh), spread(seed)), ItemInit.glowroot.get());
			helper.assertTrue(young == 1 && ripe >= 2 && ripe <= 3, "glowroot returned " + young + " young and " + ripe + " ripe");
		}
		whenLit(helper, () -> {
			helper.assertTrue(growths(helper, dry, fresh, 200) == 0, "glowroot grew on unfed farmland");
			int grew = growths(helper, fed, fresh, 200);
			helper.assertTrue(grew > 10, "glowroot in a fed basin grew only " + grew + " times in 200 checks");
		}, dry, fed);
	}

	/**
	 * A ripe wheat crop on a fresh basin's ground now and then gives up a Tainted Harvest; on drip-fed ground, unripe, or
	 * unfed, never. The modifier runs inside the block loot on fixed seeds, so the counts are the same every run.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void anOverFedCropCanTaintItsHarvestAndADripFedOneNeverDoes(GameTestHelper helper) {
		clear(helper);
		ServerLevel level = helper.getLevel();
		BlockState ripe = Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7);
		BlockPos basinWheat = crop(helper, 4, 4, ripe);
		plantingBasin(helper, at(helper, 5, 1, 4), 400.0D);
		BlockPos dripWheat = crop(helper, 12, 4, ripe);
		BloodFedGround.stamp(level, BloodFedGround.soilOf(dripWheat), 6_000);
		BlockPos dryWheat = crop(helper, 18, 4, ripe);
		// The cloche harvests at its own position: put one where a fresh basin would over-feed the ground under it.
		set(helper, at(helper, 8, 1, 4), Blocks.FARMLAND.defaultBlockState());
		plantingBasin(helper, at(helper, 9, 1, 4), 400.0D);
		BlockPos cloche = at(helper, 8, 2, 4);
		set(helper, cloche, BlockInit.mycelial_lantern.get().defaultBlockState());
		helper.assertTrue(BloodFedGround.level(level, cloche) == BloodFedGrowthRules.BASIN, "the ground under the cloche was not over-fed");
		helper.assertTrue(BloodFedGround.level(level, basinWheat) == BloodFedGrowthRules.BASIN, "the basin did not over-feed the wheat");
		helper.assertTrue(BloodFedGround.level(level, dripWheat) == BloodFedGrowthRules.DRIP, "the stamp did not feed the wheat");
		LootTable table = level.getServer().reloadableRegistries().getLootTable(Blocks.WHEAT.getLootTable());
		Item tainted = ItemInit.tainted_harvest.get();
		int overFed = 0;
		for (long seed = 0; seed < 400; seed++) {
			overFed += count(table.getRandomItems(blockLoot(level, basinWheat, ripe), spread(seed)), tainted);
			helper.assertTrue(count(table.getRandomItems(blockLoot(level, dripWheat, ripe), spread(seed)), tainted) == 0, "a drip-fed crop tainted");
			helper.assertTrue(count(table.getRandomItems(blockLoot(level, dryWheat, ripe), spread(seed)), tainted) == 0, "an unfed crop tainted");
			helper.assertTrue(count(table.getRandomItems(blockLoot(level, basinWheat, Blocks.WHEAT.defaultBlockState()), spread(seed)), tainted) == 0,
					"an unripe crop tainted");
			helper.assertTrue(count(table.getRandomItems(blockLoot(level, cloche, ripe), spread(seed)), tainted) == 0,
					"the cloche's harvest tainted");
		}
		helper.assertTrue(overFed >= 12 && overFed <= 60, "an over-fed crop tainted " + overFed + " times in 400 harvests");
		helper.succeed();
	}

	/**
	 * A drip on bare scab raises a spike from the floor, which grows at most three blocks and never closer than two empty
	 * blocks to the tip above, while the hanging spike also grows; a drop on the floor spike feeds nothing.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aDripOnBareScabRaisesAFloorSpikeAndStopsShortOfTheTip(GameTestHelper helper) {
		clear(helper);
		ServerLevel level = helper.getLevel();
		BlockPos floor = at(helper, 4, 0, 4);
		set(helper, floor, BlockInit.blood_scorched_scab.get().defaultBlockState());
		BlockPos top = dripAbove(helper, 4, 4, 1_500.0D);
		RandomSource rolls = RandomSource.create(GROWTH_SEED);
		ScabDrip.Landing last = ScabDrip.Landing.NONE;
		for (int i = 0; i < 300; i++) {
			BlockPos tip = lowestTip(helper, top);
			last = ScabDrip.drip(level, tip, level.getBlockState(tip), rolls);
			int floorTop = floorSpikeTop(helper, floor);
			BlockPos hanging = lowestTip(helper, top);
			helper.assertTrue(floorTop - floor.getY() <= BloodFedGrowthRules.MAX_FLOOR_SPIKE, "the floor spike grew past its height");
			helper.assertTrue(hanging.getY() - floorTop - 1 >= BloodFedGrowthRules.FLOOR_SPIKE_GAP,
					"the spikes closed to " + (hanging.getY() - floorTop - 1) + " empty blocks after drip " + i);
		}
		BlockState raised = level.getBlockState(floor.above());
		helper.assertTrue(ScabDrip.isFloorSpike(raised), "300 drips raised no spike from the scab floor: " + raised);
		helper.assertTrue(last == ScabDrip.Landing.FLOOR_SPIKE, "the last drop did not land on the floor spike: " + last);
		helper.assertTrue(!BloodFedGround.stamped(level, floor), "a drop on the floor spike fed the floor");
		helper.succeed();
	}

	private static int floorSpikeTop(GameTestHelper helper, BlockPos floor) {
		BlockPos top = floor;
		while (ScabDrip.isFloorSpike(helper.getLevel().getBlockState(top.above()))) top = top.above();
		return top.getY();
	}

	private static void prepare(ServerPlayer player, String manipulation, double blood) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(5_000.0D);
		volume.setBloodVolume(blood);
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
		known.getKnownManips().put(ManipulationInit.blood_projection.get(), new ManipLevel(1, 0));
		known.getKnownManips().put(ManipulationInit.blood_absorption.get(), new ManipLevel(1, 0));
		BloodManipulation registered = ManipulationInit.getByName(manipulation);
		known.getKnownManips().put(registered, new ManipLevel(1, 0));
		known.setEquippedManipNames(new ArrayList<>(List.of(manipulation)));
		known.setSelectedManip(registered);
		var temperament = HemoCapabilityAccess.getBloodTemperament(player);
		if (temperament.getAlignmentByTemper(registered.getTend()) < registered.getAlignLevel()) {
			temperament.setTemperAlignment(registered.getTend(), (float) registered.getAlignLevel() + 1.0F);
		}
		TemperFixtures.carry(player);
	}

	// ── Core-loop firsts (round two P9) ──

	/**
	 * A drop from a tank onto soil is the tank owner's first, "The First Drop", when the owner is here with their blood
	 * awake; an owner whose blood sleeps earns nothing from the same drip.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aDripFromYourOwnTankOntoSoilIsTheFirstDrop(GameTestHelper helper) {
		clear(helper);
		crop(helper, 4, 4, Blocks.WHEAT.defaultBlockState());
		BlockPos tip = dripAbove(helper, 4, 4, 500.0D);
		try (CapturedPlayer captured = CapturedPlayer.create(helper).online()) {
			ServerPlayer owner = captured.player;
			ReservoirBlood.settled(helper.getLevel().getBlockEntity(at(helper, 4, 7, 4))).setOwner(owner.getUUID());
			// CQ-3: a spike drips from an owned tank only when its placer may draw it.
			ScabDrip.recordPlacer(helper.getLevel(), tip, owner.getUUID());
			HemoCapabilityAccess.getBloodVolume(owner).setActive(false);
			helper.assertTrue(drip(helper, tip) == ScabDrip.Landing.SOIL, "the first drip did not land on the soil");
			helper.assertTrue(!HarbingerAdvancementGranter.hasAdvancement(owner, HarbingerAdvancementGranter.ADV_FIRST_DRIP),
					"an owner whose blood sleeps earned the first drop");
			HemoCapabilityAccess.getBloodVolume(owner).setActive(true);
			// The first drip may have grown the spike a block (a random roll), so the second falls from its new tip.
			helper.assertTrue(drip(helper, lowestTip(helper, tip)) == ScabDrip.Landing.SOIL,
					"the second drip did not land on the soil");
			helper.assertTrue(HarbingerAdvancementGranter.hasAdvancement(owner, HarbingerAdvancementGranter.ADV_FIRST_DRIP),
					"a drip from the owner's tank onto soil did not grant the first drop");
		}
		helper.succeed();
	}

	/**
	 * Cutting a ripe crop from ground a fresh planting basin feeds is "Fed and Reaped", for a player whose blood is awake;
	 * a sleeping body, or an unripe crop, earns nothing.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aRipeCropCutFromBasinGroundIsFedAndReaped(GameTestHelper helper) {
		clear(helper);
		ServerLevel level = helper.getLevel();
		plantingBasin(helper, at(helper, 4, 1, 4), 400.0D);
		BlockPos wheat = at(helper, 4, 2, 4);
		BlockState ripe = Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			player.setGameMode(GameType.SURVIVAL);
			HemoCapabilityAccess.getBloodVolume(player).setActive(false);
			set(helper, wheat, ripe);
			helper.assertTrue(BloodFedGround.level(level, wheat) == BloodFedGrowthRules.BASIN, "the basin did not feed the wheat");
			helper.assertTrue(player.gameMode.destroyBlock(wheat), "the player could not cut the wheat");
			helper.assertTrue(!HarbingerAdvancementGranter.hasAdvancement(player, HarbingerAdvancementGranter.ADV_BASIN_HARVEST),
					"a player whose blood sleeps earned Fed and Reaped");
			HemoCapabilityAccess.getBloodVolume(player).setActive(true);
			set(helper, wheat, Blocks.WHEAT.defaultBlockState());
			helper.assertTrue(player.gameMode.destroyBlock(wheat), "the player could not cut the young wheat");
			helper.assertTrue(!HarbingerAdvancementGranter.hasAdvancement(player, HarbingerAdvancementGranter.ADV_BASIN_HARVEST),
					"cutting an unripe crop granted Fed and Reaped");
			set(helper, wheat, ripe);
			helper.assertTrue(player.gameMode.destroyBlock(wheat), "the player could not cut the ripe wheat");
			helper.assertTrue(HarbingerAdvancementGranter.hasAdvancement(player, HarbingerAdvancementGranter.ADV_BASIN_HARVEST),
					"cutting a ripe crop from basin ground did not grant Fed and Reaped");
		}
		helper.succeed();
	}

	/**
	 * The breaker who finds a Tainted Harvest in a ripe crop from over-fed ground earns "Something Else Came Up", when
	 * their blood is awake; the same harvests cut by a sleeping body grant nothing. The loot runs on fixed seeds.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void findingATaintedHarvestIsItsFirst(GameTestHelper helper) {
		clear(helper);
		ServerLevel level = helper.getLevel();
		BlockState ripe = Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7);
		BlockPos wheat = crop(helper, 4, 4, ripe);
		plantingBasin(helper, at(helper, 5, 1, 4), 400.0D);
		LootTable table = level.getServer().reloadableRegistries().getLootTable(Blocks.WHEAT.getLootTable());
		Item tainted = ItemInit.tainted_harvest.get();
		try (CapturedPlayer sleeping = CapturedPlayer.create(helper); CapturedPlayer awake = CapturedPlayer.create(helper)) {
			HemoCapabilityAccess.getBloodVolume(sleeping.player).setActive(false);
			HemoCapabilityAccess.getBloodVolume(awake.player).setActive(true);
			int found = 0;
			for (long seed = 0; seed < 200; seed++) {
				found += count(table.getRandomItems(blockLoot(level, wheat, ripe, sleeping.player), spread(seed)), tainted);
			}
			helper.assertTrue(found > 0, "two hundred over-fed harvests gave no Tainted Harvest");
			helper.assertTrue(!HarbingerAdvancementGranter.hasAdvancement(sleeping.player, HarbingerAdvancementGranter.ADV_TAINTED_HARVEST),
					"a breaker whose blood sleeps earned the tainted-harvest first");
			helper.assertTrue(!HarbingerAdvancementGranter.hasAdvancement(awake.player, HarbingerAdvancementGranter.ADV_TAINTED_HARVEST),
					"the awake breaker had the first before cutting anything");
			for (long seed = 0; seed < 200; seed++) {
				table.getRandomItems(blockLoot(level, wheat, ripe, awake.player), spread(seed));
			}
			helper.assertTrue(HarbingerAdvancementGranter.hasAdvancement(awake.player, HarbingerAdvancementGranter.ADV_TAINTED_HARVEST),
					"finding a Tainted Harvest did not grant its first");
		}
		helper.succeed();
	}

	private static LootParams blockLoot(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer breaker) {
		return new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
				.withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withParameter(LootContextParams.BLOCK_STATE, state)
				.withParameter(LootContextParams.THIS_ENTITY, breaker).create(LootContextParamSets.BLOCK);
	}
}
