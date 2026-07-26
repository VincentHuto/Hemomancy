package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.ContainerInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.StillArtInit;
import com.vincenthuto.hemomancy.common.mission.UnstainedObservanceHelper;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.unstained.UnstainedCardinalRiteEvents;
import com.vincenthuto.hemomancy.common.tile.crafting.StillwaterCondenserBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class UnstainedProgressionGameTests {
	private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";
	private UnstainedProgressionGameTests() {}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void exactClarityRiteGatesLoad(GameTestHelper helper) {
		CardinalRiteRecipe glassLungs = CardinalRiteRecipe.getRiteByLocation(helper.getLevel(),
				Hemomancy.rloc("cardinal_rite/glass_lungs"));
		CardinalRiteRecipe moonWashed = CardinalRiteRecipe.getRiteByLocation(helper.getLevel(),
				Hemomancy.rloc("cardinal_rite/moon_washed_copper"));
		helper.assertTrue(glassLungs != null && glassLungs.getRequiredClarity() == 50f,
				"Glass Lungs must load its exact Clarity 50 gate");
		helper.assertTrue(moonWashed != null && moonWashed.getRequiredClarity() == 75f,
				"Moon-Washed Copper must load its exact Clarity 75 gate");
		helper.assertTrue(glassLungs.getResult().is(ItemInit.lethean_chalice.get()),
				"Glass Lungs must produce the registered Lethean Chalice");
		helper.assertTrue(moonWashed.getResult().is(ItemInit.pale_silver_bell.get()),
				"Moon-Washed Copper must produce a Pale Silver Bell");
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void quietWorkStationsRegistered(GameTestHelper helper) {
		helper.assertTrue(!BlockInit.stillwater_condenser.get().asItem().getDefaultInstance().isEmpty(),
				"Stillwater Condenser block item must be registered");
		helper.assertTrue(!BlockInit.verdigris_lattice.get().asItem().getDefaultInstance().isEmpty(),
				"Verdigris Lattice block item must be registered");
		helper.assertTrue(BlockEntityInit.stillwater_condenser.get().isValid(BlockInit.stillwater_condenser.get().defaultBlockState()),
				"Stillwater Condenser block entity must accept its block state");
		helper.assertTrue(ContainerInit.stillwater_condenser.isBound(),
				"Stillwater Condenser menu type must be registered");
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void stillwaterCondenserRejectsNonWaterSources(GameTestHelper helper) {
		StillwaterCondenserBlockEntity condenser = placeCondenser(helper, Blocks.LAVA);
		condenser.setItem(StillwaterCondenserBlockEntity.SLOT_BOTTLES, new ItemStack(Items.GLASS_BOTTLE));

		tickCondenser(helper, condenser, 1);

		helper.assertTrue(condenser.dataAccess.get(StillwaterCondenserBlockEntity.DATA_PROGRESS) == 0,
				"Lava and other source fluids must not count as still water");
		helper.assertTrue(condenser.getItem(StillwaterCondenserBlockEntity.SLOT_BOTTLES).getCount() == 1,
				"An invalid fluid source must not consume the bottle");
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void stillwaterCondenserProducesDewAndHonorsLatticeBoost(GameTestHelper helper) {
		StillwaterCondenserBlockEntity condenser = placeCondenser(helper, Blocks.WATER);
		BlockPos pos = condenser.getBlockPos();
		helper.getLevel().setBlockAndUpdate(pos.east(2), BlockInit.verdigris_lattice.get().defaultBlockState());
		condenser.setItem(StillwaterCondenserBlockEntity.SLOT_BOTTLES, new ItemStack(Items.GLASS_BOTTLE));

		tickCondenser(helper, condenser, 100);

		helper.assertTrue(condenser.getItem(StillwaterCondenserBlockEntity.SLOT_BOTTLES).isEmpty(),
				"A completed condensation cycle must consume one bottle");
		helper.assertTrue(condenser.getItem(StillwaterCondenserBlockEntity.SLOT_DEW).is(ItemInit.lethean_dew.get())
						&& condenser.getItem(StillwaterCondenserBlockEntity.SLOT_DEW).getCount() == 2,
				"A nearby Verdigris Lattice must halve the cycle and yield two Lethean Dew");
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void unstainedJourneyReachesClarityWithAllObservances(GameTestHelper helper) {
		ServerPlayer player = testPlayer(helper);
		try {
			var progress = HemoCapabilityAccess.requireUnstainedProgress(player);
			UnstainedCardinalRiteEvents.completeRite(helper.getLevel(), player, player.blockPosition(),
					"cardinal_rite/lethean_baptism");
			helper.assertTrue(!progress.hasBegunPurification(),
					"Lethean Baptism must reject a player whose infection was not suppressed at the Podium");

			progress.setInfectionSuppressed(true);
			UnstainedCardinalRiteEvents.completeRite(helper.getLevel(), player, player.blockPosition(),
					"cardinal_rite/lethean_baptism");
			helper.assertTrue(progress.hasBegunPurification() && progress.getPurity() == 5f,
					"Suppressed infection must allow Lethean Baptism to begin Purity at five");
			helper.assertTrue(player.getInventory().countItem(ItemInit.absolution_dagger.get()) == 1,
					"Lethean Baptism must grant the Absolution Dagger");

			fulfill(player, UnstainedObservanceHelper.Observance.GATHER_GHOST_PIPE,
					new ItemStack(BlockInit.ghost_pipe.get(), 4));
			progress.setPurity(25f);
			fulfill(player, UnstainedObservanceHelper.Observance.WEAVE_WREATH,
					new ItemStack(BlockInit.lethean_poppy_wreath.get()));
			fulfill(player, UnstainedObservanceHelper.Observance.PREPARE_HEMOLYTIC,
					new ItemStack(ItemInit.hemolytic_solution.get(), 2));

			progress.setPurity(100f);
			fulfill(player, UnstainedObservanceHelper.Observance.CONSECRATE_COPPER,
					new ItemStack(ItemInit.consecrated_copper_ingot.get(), 4));
			fulfill(player, UnstainedObservanceHelper.Observance.CONDENSE_STILL_WATERS,
					new ItemStack(ItemInit.lethean_dew.get(), 4));
			fulfill(player, UnstainedObservanceHelper.Observance.BEAR_PALLID_ICON,
					new ItemStack(ItemInit.pallid_icon.get()));
			fulfill(player, UnstainedObservanceHelper.Observance.PLATE_THE_WARD,
					new ItemStack(ItemInit.hemolytic_plating.get(), 4));
			UnstainedCardinalRiteEvents.completeRite(helper.getLevel(), player, player.blockPosition(),
					"cardinal_rite/clarity_ascension");
			helper.assertTrue(!progress.hasClarityUnlocked(),
					"Clarity Ascension must still require Consecrated Copper preparation");

			progress.setClarityPrepared(true);
			HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(2);
			UnstainedCardinalRiteEvents.completeRite(helper.getLevel(), player, player.blockPosition(),
					"cardinal_rite/clarity_ascension");
			helper.assertTrue(progress.hasClarityUnlocked() && !progress.isClarityPrepared(),
					"Prepared Clarity Ascension must unlock Clarity and consume its preparation");
			helper.assertTrue(HemoCapabilityAccess.requireInitiatoryDegree(player).getDegreeNumber() == 0,
					"Clarity must remove remaining Harbinger degree state");
			helper.assertTrue(HemoCapabilityAccess.requireKnownStillArts(player).isKnown(StillArtInit.silver_rebuke.get()),
					"Clarity Ascension must teach Silver Rebuke");

			fulfill(player, UnstainedObservanceHelper.Observance.OFFER_CHALICE,
					new ItemStack(ItemInit.lethean_chalice.get()));
			progress.setClarity(50f);
			fulfill(player, UnstainedObservanceHelper.Observance.RING_THE_PALE_WATCH,
					new ItemStack(ItemInit.pale_silver_bell.get()));
			int allObservances = (1 << UnstainedObservanceHelper.Observance.values().length) - 1;
			helper.assertTrue(progress.getAcceptedObservances() == allObservances
							&& progress.getClaimedObservances() == allObservances,
					"The critical Unstained journey must accept and fulfill every current Observance");
			helper.assertTrue(player.getInventory().countItem(ItemInit.book_of_observances.get()) == 1,
					"The journey must grant exactly one Book of Observances");
			helper.succeed();
		} finally {
			player.discard();
		}
	}

	private static StillwaterCondenserBlockEntity placeCondenser(GameTestHelper helper,
			net.minecraft.world.level.block.Block fluidBlock) {
		BlockPos pos = helper.absolutePos(new BlockPos(3, 3, 3));
		helper.getLevel().setBlockAndUpdate(pos, BlockInit.stillwater_condenser.get().defaultBlockState());
		helper.getLevel().setBlockAndUpdate(pos.below(), fluidBlock.defaultBlockState());
		helper.getLevel().setBlockAndUpdate(pos.north(), BlockInit.ghost_pipe.get().defaultBlockState());
		return (StillwaterCondenserBlockEntity) helper.getLevel().getBlockEntity(pos);
	}

	private static void tickCondenser(GameTestHelper helper, StillwaterCondenserBlockEntity condenser, int ticks) {
		for (int tick = 0; tick < ticks; tick++) {
			StillwaterCondenserBlockEntity.serverTick(helper.getLevel(), condenser.getBlockPos(),
					helper.getLevel().getBlockState(condenser.getBlockPos()), condenser);
		}
	}

	private static void fulfill(ServerPlayer player, UnstainedObservanceHelper.Observance observance,
			ItemStack offering) {
		UnstainedObservanceHelper.handle(player, observance);
		player.getInventory().add(offering);
		UnstainedObservanceHelper.handle(player, observance);
	}

	private static ServerPlayer testPlayer(GameTestHelper helper) {
		ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
				new GameProfile(UUID.randomUUID(), "unstained-journey-player"), ClientInformation.createDefault()) {
			@Override protected ItemCooldowns createItemCooldowns() { return new ItemCooldowns(); }
			@Override public void displayClientMessage(Component message, boolean overlay) { }
		};
		BlockPos position = helper.absolutePos(new BlockPos(1, 2, 1));
		player.setPos(position.getX() + 0.5, position.getY(), position.getZ() + 0.5);
		return player;
	}
}
