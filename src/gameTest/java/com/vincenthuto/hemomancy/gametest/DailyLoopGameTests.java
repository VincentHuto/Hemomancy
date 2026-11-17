package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.block.harbinger.decoration.HumoralBarometerBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.OssuaryClockBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.OssuaryClockReadout;
import com.vincenthuto.hemomancy.common.block.harbinger.ground.BloodStainBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodRegenRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.entity.mob.animal.VampireBatEntity;
import com.vincenthuto.hemomancy.common.event.DailyLoopEvents;
import com.vincenthuto.hemomancy.common.event.worldevent.BloodMoonSavedData;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodProjectionItem;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.world.answer.BloodSpilledLedger;
import com.vincenthuto.hemomancy.common.world.answer.WorldAnswer;
import com.vincenthuto.hemomancy.common.world.answer.WorldAnswerRules;
import com.vincenthuto.hemomancy.common.world.chum.ChumPools;
import com.vincenthuto.hemomancy.common.world.ground.GroundNetwork;
import com.vincenthuto.hemomancy.common.world.ground.GroundRunRules;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.ArrayList;
import java.util.List;

/**
 * Core-loop Track 10, the daily loop and the world's answer, with real players. The body: regeneration follows the
 * food bar and a full night's sleep; meat pays its blood once per item, and Hemomancy's meats carry Temper. The
 * world: blood left on the ground is counted per region and fades; a ward halves it; Absorption takes a stain's count
 * back; the Blood Moon's chance, the barometer's needle and the clock's lunette read it; wet stains draw a scavenging
 * bat out of the sky's sight; a strong pool draws a drowned; old stains in a heavy region leave sod that goes back.
 *
 * <p>Every ledger check sets its region and reads it inside one tick, so tests sharing a region cannot disturb it.
 * The suite has its own batch.
 */
@GameTestHolder("daily_loop_validation")
@PrefixGameTestTemplate(false)
public final class DailyLoopGameTests {
	private static final String EMPTY = "empty";
	private static final String BATCH = "daily_loop";

	private DailyLoopGameTests() {
	}

	// ── Fixtures ──

	/** An awake Harbinger with a large body and half of it full, both verbs known, nothing selected. */
	private static void prepare(ServerPlayer player) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(5_000.0D);
		volume.setBloodVolume(1_000.0D);
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
		known.getKnownManips().put(ManipulationInit.blood_projection.get(), new ManipLevel(1, 0));
		known.getKnownManips().put(ManipulationInit.blood_absorption.get(), new ManipLevel(1, 0));
		known.setSelectedManip(BloodManipulation.BLANK);
	}

	private static double blood(ServerPlayer player) {
		return HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
	}

	/** One regeneration tick on the body as it is, after the ledger of maximum blood has settled. */
	private static double regenerate(ServerPlayer player, int food, float saturation, boolean rested) {
		player.tickCount = 1;
		BloodVolumeEvents.playerTick(new PlayerTickEvent.Post(player));
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setBloodVolume(volume.getMaxBloodVolume() / 2.0D);
		player.getFoodData().setFoodLevel(food);
		player.getFoodData().setSaturation(saturation);
		if (rested) player.addEffect(new MobEffectInstance(EffectInit.well_rested, BloodRegenRules.RESTED_TICKS));
		double before = volume.getBloodVolume();
		player.tickCount = 20;
		BloodVolumeEvents.playerTick(new PlayerTickEvent.Post(player));
		return volume.getBloodVolume() - before;
	}

	/** Finishes eating one of {@code item} through NeoForge's hook, as completing the use does. */
	private static void eat(ServerPlayer player, net.minecraft.world.item.Item item) {
		ItemStack stack = new ItemStack(item);
		EventHooks.onItemUseFinish(player, stack.copy(), 0, stack.finishUsingItem(player.level(), player));
	}

	private static void floor(GameTestHelper helper, int x0, int x1, int z0, int z1) {
		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				helper.setBlock(new BlockPos(x, 0, z), Blocks.POLISHED_ANDESITE);
				for (int y = 1; y <= 4; y++) helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
			}
		}
	}

	private static BlockState floorStain() {
		return BlockInit.blood_stain.get().defaultBlockState().setValue(MultifaceBlock.getFaceProperty(Direction.DOWN), true);
	}

	// ── The body ──

	@GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 20)
	public static void aFedBodyRegeneratesFasterThanAStarvingOne(GameTestHelper helper) {
		double[] gains = new double[5];
		Object[][] bodies = {{0, 0.0F, false}, {4, 0.0F, false}, {20, 5.0F, false}, {20, 12.8F, false}, {20, 12.8F, true}};
		for (int i = 0; i < bodies.length; i++) {
			try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
				prepare(captured.player);
				gains[i] = regenerate(captured.player, (int) bodies[i][0], (float) bodies[i][1], (boolean) bodies[i][2]);
			}
		}
		double base = gains[2];
		helper.assertTrue(base > 0.0D, "a plain body regenerated nothing");
		helper.assertTrue(gains[0] == 0.0D, "a starving body regenerated " + gains[0]);
		helper.assertTrue(Math.abs(gains[1] - base * BloodRegenRules.HUNGRY) < 1.0E-6D, "hungry: " + gains[1] + " against " + base);
		helper.assertTrue(Math.abs(gains[3] - base * BloodRegenRules.WELL_FED) < 1.0E-6D, "well fed: " + gains[3] + " against " + base);
		helper.assertTrue(Math.abs(gains[4] - base * (BloodRegenRules.WELL_FED + BloodRegenRules.RESTED_BONUS)) < 1.0E-6D,
				"well fed and rested: " + gains[4] + " against " + base);
		helper.succeed();
	}

	@GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 20)
	public static void aSteakPaysItsBloodOnceAndBreadNone(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player);
			player.getFoodData().setFoodLevel(4);
			double before = blood(player);
			eat(player, Items.COOKED_BEEF);
			helper.assertTrue(Math.abs(blood(player) - before - 48.0D) < 1.0E-6D, "a steak paid " + (blood(player) - before));
			helper.assertTrue(captured.said().contains("48"), "the action bar did not say what the meal gave: " + captured.said());
			helper.assertTrue(com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(player, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_MEAT_IS_BLOOD),
					"a paying meal did not grant Meat Is Blood (core-loop Track 11)");
			double afterSteak = blood(player);
			eat(player, Items.BREAD);
			helper.assertTrue(blood(player) == afterSteak, "bread paid blood");
			eat(player, Items.BEEF);
			helper.assertTrue(Math.abs(blood(player) - afterSteak - 12.0D) < 1.0E-6D, "raw beef paid " + (blood(player) - afterSteak));
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 20)
	public static void venisonCarriesAnimusAndTaintedMeatPaysTenANutrition(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player);
			float animus = HemoCapabilityAccess.getBloodTemper(player).get(EnumBloodTemper.ANIMUS);
			double before = blood(player);
			eat(player, ItemInit.cooked_venison.get());
			helper.assertTrue(Math.abs(blood(player) - before - 64.0D) < 1.0E-6D, "cooked venison paid " + (blood(player) - before));
			float gained = HemoCapabilityAccess.getBloodTemper(player).get(EnumBloodTemper.ANIMUS) - animus;
			helper.assertTrue(Math.abs(gained - 3.0F) < 1.0E-4F, "cooked venison gave " + gained + " Animus Temper");
			double afterVenison = blood(player);
			eat(player, ItemInit.tainted_meat.get());
				// CB-3: tainted meat is 10 mL a nutrition point, 30 mL an item.
			helper.assertTrue(Math.abs(blood(player) - afterVenison - 30.0D) < 1.0E-6D,
					"tainted meat paid " + (blood(player) - afterVenison));
		}
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer dormant = captured.player;
			HemoCapabilityAccess.getBloodVolume(dormant).setActive(false);
			double before = blood(dormant);
			eat(dormant, ItemInit.tainted_meat.get());
			helper.assertTrue(blood(dormant) == before, "a dormant body took blood from meat");
			helper.assertTrue(dormant.hasEffect(MobEffects.CONFUSION), "tainted meat did not sicken a dormant body");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 20)
	public static void aFullNightLeavesTheBodyWellRestedAndLeavingBedEarlyDoesNot(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player);
			DailyLoopEvents.onWake(new PlayerWakeUpEvent(player, true, true));
			helper.assertFalse(player.hasEffect(EffectInit.well_rested), "leaving bed early rested the body");
			helper.assertTrue(!com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(player, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_FULL_NIGHT), "leaving bed early granted A Full Night");
			DailyLoopEvents.onWake(new PlayerWakeUpEvent(player, false, false));
			helper.assertTrue(com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(player, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_FULL_NIGHT), "a full night did not grant A Full Night");
			MobEffectInstance rested = player.getEffect(EffectInit.well_rested);
			helper.assertTrue(rested != null && rested.getDuration() == BloodRegenRules.RESTED_TICKS,
					"a full night did not leave the body Well Rested for five minutes: " + rested);
		}
		helper.succeed();
	}

	// ── The count ──

	@GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 20)
	public static void theCountFadesAndAWardHalvesWhatItTakes(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos ward = helper.absolutePos(new BlockPos(2, 2, 2));
		helper.setBlock(new BlockPos(2, 2, 2), Blocks.AIR);
		BloodSpilledLedger.set(level, ward, 0.0D);
		BloodSpilledLedger.spill(level, ward, 100.0D);
		helper.assertTrue(Math.abs(BloodSpilledLedger.total(level, ward) - 100.0D) < 1.0E-6D, "the count did not take a spill");
		BloodSpilledLedger.backdate(level, ward, WorldAnswerRules.HALF_LIFE_TICKS);
		helper.assertTrue(Math.abs(BloodSpilledLedger.total(level, ward) - 50.0D) < 1.0E-6D,
				"two days did not halve the count: " + BloodSpilledLedger.total(level, ward));
		helper.setBlock(new BlockPos(2, 2, 2), BlockInit.suspended_vivianite.get());
		helper.assertTrue(BloodSpilledLedger.wards(level, ward) >= 1, "placing Suspended Vivianite recorded no ward");
		int wards = WorldAnswerRules.clampWards(BloodSpilledLedger.wards(level, ward));
		double counted = BloodSpilledLedger.spill(level, ward, 100.0D);
		helper.assertTrue(Math.abs(counted - WorldAnswerRules.counted(100.0D, wards)) < 1.0E-6D && counted < 100.0D,
				"a ward did not cut what was counted: " + counted);
		helper.setBlock(new BlockPos(2, 2, 2), Blocks.AIR);
		helper.assertTrue(BloodSpilledLedger.wards(level, ward) == wards - 1, "removing the ward left it recorded");
		BloodSpilledLedger.set(level, ward, 0.0D);
		helper.succeed();
	}

	@GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 20)
	public static void aStainLaidCountsAndAbsorbingItTakesItBack(GameTestHelper helper) {
		floor(helper, 0, 6, 1, 3);
		ServerLevel level = helper.getLevel();
		BlockPos cell = helper.absolutePos(new BlockPos(4, 1, 2));
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player);
			Vec3 feet = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(1, 1, 2)));
			player.setPos(feet.x, feet.y, feet.z);
			player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atBottomCenterOf(cell).add(0.0D, 0.001D, 0.0D));
			player.setOldPosAndRot();
			player.yHeadRotO = player.getYHeadRot();
			BloodSpilledLedger.set(level, cell, 0.0D);
			BloodProjectionItem.projectFromEntity(level, player, 5.0D, 100.0D);
			helper.assertTrue(level.getBlockState(cell).getBlock() instanceof BloodStainBlock, "plain projection laid no stain");
			helper.assertTrue(Math.abs(BloodSpilledLedger.total(level, cell) - GroundRunRules.STAIN_ML) < 1.0E-6D,
					"a stain counted " + BloodSpilledLedger.total(level, cell));
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.blood_absorption.get()));
			player.startUsingItem(InteractionHand.MAIN_HAND);
			ItemStack hand = player.getUseItem();
			hand.getItem().onUseTick(level, player, hand, player.getUseItemRemainingTicks());
			helper.assertTrue(level.getBlockState(cell).isAir(), "Absorption left the stain");
			helper.assertTrue(BloodSpilledLedger.total(level, cell) == 0.0D,
					"absorbing the stain left " + BloodSpilledLedger.total(level, cell) + " counted");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 20)
	public static void aKillByAnAwakeHarbingerBleedsIntoTheCount(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(3, 2, 3));
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player);
			BlockPos at = pig.blockPosition();
			BloodSpilledLedger.set(level, at, 0.0D);
			pig.hurt(level.damageSources().playerAttack(player), 1_000.0F);
			helper.assertTrue(pig.isDeadOrDying(), "the pig did not die");
			double expected = WorldAnswerRules.killSpill(pig.getMaxHealth());
			double counted = BloodSpilledLedger.total(level, at);
			// The body may also leave a stain where it fell (Track 6), which counts as well.
			helper.assertTrue(Math.abs(counted - expected) < 1.0E-6D || Math.abs(counted - expected - GroundRunRules.STAIN_ML) < 1.0E-6D,
					"a kill counted " + counted + ", not " + expected);
			BloodSpilledLedger.set(level, at, 0.0D);
		}
		helper.succeed();
	}

	/** CB-26, NQ-02-7: a kill in a region already at the uneasy floor adds nothing; only its stain counts. */
	@GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 20)
	public static void killsAloneNeverLiftARegionPastUneasy(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(3, 2, 3));
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player);
			BlockPos at = pig.blockPosition();
			BloodSpilledLedger.set(level, at, WorldAnswerRules.KILL_SHARE_CAP_ML);
			pig.hurt(level.damageSources().playerAttack(player), 1_000.0F);
			helper.assertTrue(pig.isDeadOrDying(), "the pig did not die");
			double counted = BloodSpilledLedger.total(level, at);
			double cap = WorldAnswerRules.KILL_SHARE_CAP_ML;
			helper.assertTrue(Math.abs(counted - cap) < 1.0E-6D || Math.abs(counted - cap - GroundRunRules.STAIN_ML) < 1.0E-6D,
					"a kill lifted a region at the cap to " + counted);
			BloodSpilledLedger.set(level, at, 0.0D);
		}
		helper.succeed();
	}

	// ── What reads it ──

	@GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 20)
	public static void theMoonsChanceFollowsThePhaseAndThePressure(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos at = helper.absolutePos(new BlockPos(3, 1, 3));
		int phase = level.getMoonPhase();
		BloodSpilledLedger.set(level, at, 0.0D);
		double quiet = WorldAnswer.moonChanceAt(level, at);
		BloodSpilledLedger.set(level, at, WorldAnswerRules.FULL_PRESSURE_ML);
		double busy = WorldAnswer.moonChanceAt(level, at);
		BloodSpilledLedger.set(level, at, 0.0D);
		helper.assertTrue(quiet == WorldAnswerRules.moonChance(phase, 0.0D), "the quiet chance is not the phase's");
		helper.assertTrue(busy == WorldAnswerRules.moonChance(phase, 1.0D), "the busy chance is not the pressure's");
		helper.assertTrue(WorldAnswerRules.moonBase(phase) == 0.0D ? busy == 0.0D : busy > quiet,
				"spilled blood did not raise the chance at phase " + phase + ": " + quiet + " to " + busy);
		// The Blood Moon rises only over the Overworld: an instrument in the Nether (a fixed moon) shows that roll.
		ServerLevel nether = level.getServer().getLevel(net.minecraft.world.level.Level.NETHER);
		if (nether != null) {
			helper.assertTrue(WorldAnswer.moonChanceAt(nether, BlockPos.ZERO) == WorldAnswer.moonChanceTonight(level.getServer().overworld()),
					"an instrument in the Nether read its own fixed moon");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 20)
	public static void theBloodMoonDiscountIsForInitiatesInTheOverworld(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerLevel nether = level.getServer().getLevel(net.minecraft.world.level.Level.NETHER);
		helper.assertTrue(nether != null, "the test server has no Nether");
		BloodMoonSavedData moon = BloodMoonSavedData.get(level.getServer().overworld());
		boolean wasActive = moon.isActive();
		moon.start(level.getGameTime() + 100L);
		try {
			ServerPlayer initiate = fakePlayer(level, "moon_d1");
			ServerPlayer uninitiated = fakePlayer(level, "moon_d0");
			ServerPlayer elsewhere = fakePlayer(nether, "moon_nether");
			HemoCapabilityAccess.getInitiatoryDegree(initiate).setDegreeNumber(1);
			HemoCapabilityAccess.getInitiatoryDegree(elsewhere).setDegreeNumber(1);
			HemoCapabilityAccess.getInitiatoryDegree(uninitiated).setDegreeNumber(0);
			helper.assertTrue(moonLine(initiate), "a D1 caster in the Overworld got no Blood Moon discount");
			helper.assertFalse(moonLine(uninitiated), "a D0 caster got the Blood Moon discount");
			helper.assertFalse(moonLine(elsewhere), "a caster in the Nether got the Blood Moon discount");
		} finally {
			if (!wasActive) moon.stop();
		}
		helper.succeed();
	}

	private static ServerPlayer fakePlayer(ServerLevel level, String name) {
		return net.neoforged.neoforge.common.util.FakePlayerFactory.get(level,
				new com.mojang.authlib.GameProfile(java.util.UUID.nameUUIDFromBytes(name.getBytes()), name));
	}

	private static boolean moonLine(ServerPlayer player) {
		return com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostLedger
				.collect(player, ManipulationInit.blood_shot.get(), 1.0D).contributions().stream()
				.anyMatch(line -> "blood_moon".equals(line.sourceId()));
	}

	@GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 20)
	public static void theBarometerNeedleAndTheClockLunetteReadTheRegion(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		helper.setBlock(new BlockPos(5, 2, 5), Blocks.STONE);
		BlockPos barometer = helper.absolutePos(new BlockPos(5, 2, 4));
		level.setBlock(barometer, BlockInit.humoral_barometer.get().defaultBlockState()
				.setValue(HumoralBarometerBlock.FACING, Direction.SOUTH), 3);
		BlockPos clock = helper.absolutePos(new BlockPos(2, 1, 5));
		BlockState lower = BlockInit.ossuary_clock.get().defaultBlockState();
		level.setBlock(clock, lower, 3);
		level.setBlock(clock.above(), lower.setValue(OssuaryClockBlock.HALF, DoubleBlockHalf.UPPER), 3);

		BloodSpilledLedger.set(level, barometer, WorldAnswerRules.bandFloor(WorldAnswerRules.SEETHING));
		HumoralBarometerBlock.read(level, barometer, level.getBlockState(barometer));
		helper.assertTrue(level.getBlockState(barometer).getValue(HumoralBarometerBlock.PRESSURE) == WorldAnswerRules.SEETHING,
				"the needle did not stand at seething");
		int expected = OssuaryClockReadout.omen(false, OssuaryClockBlock.chanceTonight(level, clock));
		OssuaryClockBlock.read(level, clock, level.getBlockState(clock));
		helper.assertTrue(level.getBlockState(clock.above()).getValue(OssuaryClockBlock.OMEN) == expected,
				"the lunette does not show tonight's omen " + expected);
		BloodMoonSavedData moon = BloodMoonSavedData.get(level.getServer().overworld());
		boolean wasActive = moon.isActive();
		moon.start(level.getGameTime() + 100L);
		OssuaryClockBlock.read(level, clock, level.getBlockState(clock));
		boolean risen = level.getBlockState(clock.above()).getValue(OssuaryClockBlock.OMEN) == 2;
		if (!wasActive) moon.stop();
		helper.assertTrue(risen, "the lunette did not turn crimson under a Blood Moon");

		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			BloodSpilledLedger.set(level, barometer, 0.0D);
			level.getBlockState(barometer).useWithoutItem(level, captured.player,
					new BlockHitResult(Vec3.atCenterOf(barometer), Direction.NORTH, barometer, false));
			helper.assertTrue(level.getBlockState(barometer).getValue(HumoralBarometerBlock.PRESSURE) == WorldAnswerRules.STILL,
					"using the barometer did not re-read a still region");
			helper.assertTrue(captured.said().contains("still"), "the barometer did not name the band: " + captured.said());
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 200)
	public static void wetStainsDrawAScavengingBatOutOfTheSkysSight(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper, 1, 9, 1, 9);
		for (int x = 0; x <= 10; x++) {
			for (int z = 0; z <= 10; z++) helper.setBlock(new BlockPos(x, 8, z), Blocks.STONE);
		}
		BlockPos centre = helper.absolutePos(new BlockPos(5, 1, 5));
		level.setBlock(centre.west(), floorStain(), 3);
		helper.assertTrue(WorldAnswer.drawScavenger(level, centre, RandomSource.create(1L)) == null, "one stain drew a scavenger");
		level.setBlock(centre, floorStain(), 3);
		level.setBlock(centre.east(), floorStain(), 3);
		BloodSpilledLedger.set(level, centre, WorldAnswerRules.bandFloor(WorldAnswerRules.SEETHING));
		VampireBatEntity bat = null;
		RandomSource random = RandomSource.create(11L);
		for (int i = 0; i < 32 && bat == null; i++) bat = WorldAnswer.drawScavenger(level, centre, random);
		BloodSpilledLedger.set(level, centre, 0.0D);
		helper.assertTrue(bat != null, "three wet stains under a roof in a seething region drew no bat");
		VampireBatEntity drawn = bat;
		helper.succeedWhen(() -> {
			int wet = WorldAnswer.wetStains(level, centre, 2).size();
			helper.assertTrue(wet < 3, "the bat has not drunk a stain yet (" + wet + " wet, scent " + drawn.scent() + ")");
			drawn.discard();
		});
	}

	@GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 20)
	public static void inTheOpenByDayStainsDrawNothing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper, 1, 9, 1, 9);
		BlockPos centre = helper.absolutePos(new BlockPos(5, 1, 5));
		for (BlockPos cell : new BlockPos[] {centre.west(), centre, centre.east()}) {
			// An earlier batch's taller template may have left blocks over this area: open the columns to the sky.
			for (int y = cell.getY() + 1; y < level.getMaxBuildHeight(); y++) {
				BlockPos above = new BlockPos(cell.getX(), y, cell.getZ());
				if (!level.getBlockState(above).isAir()) level.setBlock(above, Blocks.AIR.defaultBlockState(), 2);
			}
			level.setBlock(cell, floorStain(), 3);
		}
		helper.assertTrue(WorldAnswer.openToSky(level, centre.west()) && WorldAnswer.openToSky(level, centre.east()),
				"the open floor is not open to the sky");
		if (!level.isNight()) {
			BloodSpilledLedger.set(level, centre, WorldAnswerRules.bandFloor(WorldAnswerRules.SEETHING));
			RandomSource random = RandomSource.create(13L);
			for (int i = 0; i < 16; i++) {
				helper.assertTrue(WorldAnswer.drawScavenger(level, centre, random) == null,
						"stains in daylight under open sky drew a bat");
			}
			BloodSpilledLedger.set(level, centre, 0.0D);
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 40)
	public static void aStrongPoolInAnUneasyRegionDrawsADrowned(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (int x = 1; x <= 13; x++) {
			for (int z = 1; z <= 13; z++) {
				boolean wall = x == 1 || x == 13 || z == 1 || z == 13;
				helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
				for (int y = 1; y <= 4; y++) helper.setBlock(new BlockPos(x, y, z), wall ? Blocks.STONE : Blocks.WATER);
				for (int y = 5; y <= 8; y++) helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
			}
		}
		BlockPos top = helper.absolutePos(new BlockPos(7, 4, 7));
		Vec3 surface = new Vec3(top.getX() + 0.5D, top.getY() + level.getFluidState(top).getHeight(level, top), top.getZ() + 0.5D);
		ChumPools.Pool pool = ChumPools.pour(level, surface, 400.0D, null);
		BloodSpilledLedger.set(level, top, 0.0D);
		Entity quiet = level.isNight() ? null
				: WorldAnswer.drawHunter(level, surface, pool.radius(), pool.strength(), ChumPools.depth(level, surface), RandomSource.create(3L));
		boolean quietDrew = quiet != null;
		BloodSpilledLedger.set(level, top, WorldAnswerRules.bandFloor(WorldAnswerRules.UNEASY));
		Entity hunter = WorldAnswer.drawHunter(level, surface, pool.radius(), pool.strength(), ChumPools.depth(level, surface), RandomSource.create(3L));
		Entity second = WorldAnswer.drawHunter(level, surface, pool.radius(), pool.strength(), ChumPools.depth(level, surface), RandomSource.create(4L));
		BloodSpilledLedger.set(level, top, 0.0D);
		ChumPools.backdate(level, surface, 1_000_000L);
		helper.assertFalse(quietDrew, "a pool drew a hunter by day in a still region");
		helper.assertTrue(hunter != null && hunter.getType() == EntityType.DROWNED, "a strong pool in an uneasy region drew no drowned");
		helper.assertTrue(second == null, "a pool drew past its hunter cap");
		// Only what this pool drew: a wider sweep would reach other tests' Drowned.
		hunter.discard();
		if (quiet != null) quiet.discard();
		helper.succeed();
	}

	/**
	 * Core-loop round two, P7: the water decides the hunter. Home waters draw a Brined Votary, a swamp a Bog Revenant onto
	 * its bank, and the one-hunter cap holds across all three kinds. Only this pool's draws are counted and discarded.
	 */
	@GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 40)
	public static void aStrongPoolDrawsTheHunterOfItsWaters(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (int x = 1; x <= 13; x++) {
			for (int z = 1; z <= 13; z++) {
				boolean wall = x == 1 || x == 13 || z == 1 || z == 13;
				helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
				for (int y = 1; y <= 4; y++) helper.setBlock(new BlockPos(x, y, z), wall ? Blocks.STONE : Blocks.WATER);
				for (int y = 5; y <= 8; y++) helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
			}
		}
		BlockPos top = helper.absolutePos(new BlockPos(7, 4, 7));
		Vec3 surface = new Vec3(top.getX() + 0.5D, top.getY() + level.getFluidState(top).getHeight(level, top), top.getZ() + 0.5D);
		ChumPools.Pool pool = ChumPools.pour(level, surface, 400.0D, null);
		BloodSpilledLedger.set(level, top, WorldAnswerRules.bandFloor(WorldAnswerRules.UNEASY));
		int depth = ChumPools.depth(level, surface);
		List<Entity> drawn = new ArrayList<>();
		Entity votary = WorldAnswer.drawHunter(level, surface, pool.radius(), pool.strength(), depth, RandomSource.create(3L),
				WorldAnswerRules.HunterWaters.HOME);
		if (votary != null) drawn.add(votary);
		Entity blocked = WorldAnswer.drawHunter(level, surface, pool.radius(), pool.strength(), depth, RandomSource.create(4L),
				WorldAnswerRules.HunterWaters.SWAMP);
		if (blocked != null) drawn.add(blocked);
		if (votary != null) votary.discard();
		Entity revenant = WorldAnswer.drawHunter(level, surface, pool.radius(), pool.strength(), depth, RandomSource.create(5L),
				WorldAnswerRules.HunterWaters.SWAMP);
		if (revenant != null) drawn.add(revenant);
		Entity drowned = WorldAnswer.drawHunter(level, surface, pool.radius(), pool.strength(), depth, RandomSource.create(6L),
				WorldAnswerRules.HunterWaters.OPEN);
		if (drowned != null) drawn.add(drowned);
		BloodSpilledLedger.set(level, top, 0.0D);
		ChumPools.backdate(level, surface, 1_000_000L);
		drawn.forEach(Entity::discard);
		helper.assertTrue(votary != null && votary.getType() == EntityInit.brined_votary.get(), "home waters drew no Brined Votary: " + votary);
		helper.assertTrue(blocked == null, "a swamp hunter came while a votary stood at the pool");
		helper.assertTrue(revenant != null && revenant.getType() == EntityInit.bog_revenant.get(), "a swamp drew no Bog Revenant: " + revenant);
		helper.assertTrue(drowned == null, "a drowned came while a revenant stood at the pool");
		helper.assertTrue(WorldAnswer.hunterWatersAt(level, top) == WorldAnswerRules.HunterWaters.OPEN,
				"the test world's plains read as " + WorldAnswer.hunterWatersAt(level, top));
		helper.succeed();
	}

	@GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 20)
	public static void oldStainsInAHeavyRegionLeaveSodThatGoesBack(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos grass = helper.absolutePos(new BlockPos(3, 0, 3));
		BlockPos cell = grass.above();
		helper.setBlock(new BlockPos(3, 0, 3), Blocks.GRASS_BLOCK);
		helper.setBlock(new BlockPos(3, 1, 3), Blocks.AIR);
		BlockState dry = floorStain().setValue(BloodStainBlock.AGE, GroundRunRules.DRY);

		BloodSpilledLedger.set(level, grass, WorldAnswerRules.bandFloor(WorldAnswerRules.STIRRING));
		helper.assertFalse(WorldAnswer.stainFaded(level, cell, dry, 0.0F), "a quiet region stained the grass");
		BloodSpilledLedger.set(level, grass, WorldAnswerRules.bandFloor(WorldAnswerRules.HEAVY));
		helper.assertFalse(WorldAnswer.stainFaded(level, cell, dry, 0.99F), "the ground remembered on a failed roll");
		level.setBlock(cell, dry, 3);
		GroundNetwork.ageStain(level, cell, dry, 0.0F);
		BlockState wornAway = level.getBlockState(cell);
		boolean remembered = level.getBlockState(grass).is(BlockInit.stained_sod.get())
				|| WorldAnswer.stainFaded(level, cell, dry, 0.0F);
		helper.assertTrue(wornAway.isAir(), "the dry stain did not wear away");
		helper.assertTrue(remembered && level.getBlockState(grass).is(BlockInit.stained_sod.get()),
				"a stain wearing away in a heavy region left no stained sod");

		BlockState sod = level.getBlockState(grass);
		// One source for the whole loop: java.util.Random's first float is nearly the same for neighbouring seeds.
		RandomSource random = RandomSource.create(7L);
		for (int i = 0; i < 64 && level.getBlockState(grass).is(BlockInit.stained_sod.get()); i++) {
			sod.randomTick(level, grass, random);
		}
		helper.assertTrue(level.getBlockState(grass).is(BlockInit.stained_sod.get()), "sod went back while the region is heavy");
		BloodSpilledLedger.set(level, grass, 0.0D);
		for (int i = 0; i < 64 && level.getBlockState(grass).is(BlockInit.stained_sod.get()); i++) {
			sod.randomTick(level, grass, random);
		}
		helper.assertTrue(level.getBlockState(grass).is(Blocks.GRASS_BLOCK), "sod did not go back to grass once the region quieted");
		helper.succeed();
	}

	// ── Brought On (round two P9) ──

	/**
	 * A Blood Moon the spilled blood raised credits every awake player standing where blood was counted. The moon itself
	 * is world state on a shared server, so this drives only the credit, and the pure rule is pinned in
	 * {@code WorldAnswerRulesTest}. A player on quiet ground, or one whose blood sleeps, is not credited.
	 */
	@GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 20)
	public static void aMoonBroughtOnCreditsWhoeverStoodInTheCount(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos at = helper.absolutePos(new BlockPos(3, 1, 3));
		double before = BloodSpilledLedger.total(level, at);
		try (CapturedPlayer awake = CapturedPlayer.create(helper); CapturedPlayer sleeping = CapturedPlayer.create(helper)) {
			prepare(awake.player);
			HemoCapabilityAccess.getBloodVolume(sleeping.player).setActive(false);
			for (ServerPlayer player : List.of(awake.player, sleeping.player)) player.setPos(Vec3.atBottomCenterOf(at));
			BloodSpilledLedger.set(level, at, 0.0D);
			helper.assertTrue(!WorldAnswer.creditMoonBroughtOn(level, awake.player), "quiet ground credited a moon");
			BloodSpilledLedger.set(level, at, WorldAnswerRules.FULL_PRESSURE_ML);
			helper.assertTrue(!WorldAnswer.creditMoonBroughtOn(level, sleeping.player), "a sleeping body was credited with the moon");
			helper.assertTrue(WorldAnswer.creditMoonBroughtOn(level, awake.player), "an awake player in the count was not credited");
			helper.assertTrue(com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(awake.player,
					com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_MOON_BROUGHT_ON), "the credit did not grant Brought On");
			helper.assertTrue(!com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(sleeping.player,
					com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_MOON_BROUGHT_ON), "a sleeping body earned Brought On");
		} finally {
			BloodSpilledLedger.set(level, at, before);
		}
		helper.succeed();
	}
}
