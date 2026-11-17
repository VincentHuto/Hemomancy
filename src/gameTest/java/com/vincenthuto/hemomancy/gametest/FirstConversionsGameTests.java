package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendon.TendonAnchor;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodProjectionItem;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.animus.TendonLineRules;
import com.vincenthuto.hemomancy.common.manipulation.flammeus.PyreticForgeRules;
import com.vincenthuto.hemomancy.common.manipulation.flammeus.PyreticForgeSculpting;
import com.vincenthuto.hemomancy.common.manipulation.mortem.LignumMortisSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingDispatcher;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingRules;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingSessions;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncTendonAnchor;
import com.vincenthuto.hemomancy.common.rite.brazier.BrazierTransmutationRite;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Core-loop Track 2: the first four Sculptings with a real player through the real verb chains. Lignum Mortis (a block),
 * Pyretic Forge (items on the ground and a block, and the brazier's transmutation rite), the Tendon Line (a block for
 * the throw, anything for the haul) and Blood Binding (a creature). Each pays per pulse and stops when the verb is
 * let go; an awakened rite sigil still takes the blood first; and with nothing selected the plain verb is unchanged.
 * A balance pin holds each Sculpting's blood and work per second.
 */
@GameTestHolder("sculpting_validation")
@PrefixGameTestTemplate(false)
public final class FirstConversionsGameTests {
	private static final String EMPTY = "bastion/mobs/empty";

	private FirstConversionsGameTests() {
	}

	// ── Fixtures ──

	/** Active blood, both verbs known, {@code manipulation} known, equipped and selected (or nothing selected). */
	static void prepare(ServerPlayer player, String manipulation, double blood) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(Math.max(volume.getMaxBloodVolume(), blood));
		volume.setBloodVolume(blood);
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
		known.getKnownManips().put(ManipulationInit.blood_projection.get(), new ManipLevel(1, 0));
		known.getKnownManips().put(ManipulationInit.blood_absorption.get(), new ManipLevel(1, 0));
		if (manipulation != null) {
			BloodManipulation registered = ManipulationInit.getByName(manipulation);
			known.getKnownManips().put(registered, new ManipLevel(1, 0));
			// A family form (chain, lattice, canopy...) needs its baseline mastered far enough to be absorbed.
			com.vincenthuto.hemomancy.common.manipulation.family.ManipulationFamilyRegistry.form(manipulation).ifPresent(form ->
					known.getKnownManips().put(ManipulationInit.getByName(
							com.vincenthuto.hemomancy.common.manipulation.family.ManipulationFamilyRegistry.baselineId(manipulation)),
							new ManipLevel(form.requiredLevel(), 0)));
			known.setEquippedManipNames(new ArrayList<>(List.of(manipulation)));
			known.setSelectedManip(registered);
			// Meet the alignment gate (Pyretic Forge asks for 10 Flammeus).
			var temperament = HemoCapabilityAccess.getBloodTemperament(player);
			if (temperament.getAlignmentByTemper(registered.getTend()) < registered.getAlignLevel()) {
				temperament.setTemperAlignment(registered.getTend(), (float) registered.getAlignLevel() + 1.0F);
			}
		} else {
			known.setSelectedManip(BloodManipulation.BLANK);
		}
		// Carry Temper, so each pulse costs and works at its pinned rate (core loop Track 4).
		TemperFixtures.carry(player);
	}

	/** Stands and turns the player; the old position and rotation follow, since block picks read the partial-tick-0 view. */
	static void aim(ServerPlayer player, Vec3 feet, Vec3 at) {
		player.setPos(feet.x, feet.y, feet.z);
		player.lookAt(EntityAnchorArgument.Anchor.EYES, at);
		player.setOldPosAndRot();
		player.yHeadRotO = player.getYHeadRot();
	}

	static Vec3 feet(GameTestHelper helper, int x, int z) {
		return Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(x, 1, z)));
	}

	static double project(ServerPlayer player) {
		return BloodProjectionItem.projectFromEntity(player.level(), player, 5.0D, 100.0D);
	}

	/** One tick of held Absorption through the hand the player is really using. */
	static void absorbTick(ServerPlayer player) {
		if (!player.isUsingItem() || !player.getUseItem().is(ItemInit.blood_absorption.get())) {
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.blood_absorption.get()));
			player.startUsingItem(InteractionHand.MAIN_HAND);
		}
		ItemStack hand = player.getUseItem();
		hand.getItem().onUseTick(player.level(), player, hand, player.getUseItemRemainingTicks());
	}

	static void clear(GameTestHelper helper, int fromX, int toX, int z, int height) {
		for (int x = fromX; x <= toX; x++) {
			for (int y = 1; y <= height; y++) helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
			helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
		}
	}

	static double blood(ServerPlayer player) {
		return HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
	}

	/**
	 * The blood one pulse costs this player on {@code manipulation}, after the cost ledger: the Sculpting that claims what
	 * they aim at now (a narrow one tried ahead of the main, such as Crimson Flame Conjuration's kindling), or the main
	 * one when nothing is claimed. Read through {@code bloodPerPulse(player)}, as the verb pays it.
	 */
	static double price(ServerPlayer player, String manipulation, Verb verb) {
		BloodManipulation registered = ManipulationInit.getByName(manipulation);
		VerbSculpting sculpting = SculptingDispatcher.currentClaim(player, verb).map(claim -> claim.selected().sculpting())
				.orElseGet(() -> SculptingDispatcher.selected(player, verb).orElseThrow().sculpting());
		return sculpting.bloodPerPulse(player) * ManipulationCostLedger.collect(player, registered, 1.0D).totalMultiplier();
	}

	// ── Lignum Mortis: a block ──

	@GameTest(template = EMPTY, timeoutTicks = 80)
	public static void lignumCrawlsWhileProjectionFlowsAndFellsOnRelease(GameTestHelper helper) {
		clear(helper, 1, 6, 2, 10);
		List<BlockPos> logs = new ArrayList<>();
		for (int y = 1; y <= 8; y++) {
			BlockPos log = helper.absolutePos(new BlockPos(4, y, 2));
			helper.getLevel().setBlock(log, Blocks.OAK_LOG.defaultBlockState(), 3);
			logs.add(log);
		}
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "lignum_mortis", 2000.0D);
		aim(player, feet(helper, 1, 2), Vec3.atCenterOf(logs.getFirst()));
		double pulse = price(player, "lignum_mortis", Verb.PROJECT);
		int[] tick = {0};
		helper.onEachTick(() -> {
			if (tick[0]++ < 25) project(player);
		});
		helper.runAtTickTime(28, () -> {
			try {
				int reached = LignumMortisSculpting.crawlSize(player);
				helper.assertTrue(reached >= 3 && reached < logs.size(), "three pulses reached " + reached + " logs");
				helper.assertTrue(logs.stream().allMatch(pos -> helper.getLevel().getBlockState(pos).is(Blocks.OAK_LOG)),
						"a log fell before the verb was let go");
				helper.assertTrue(Math.abs(2000.0D - blood(player) - 3 * pulse) < 1.0E-6D,
						"three pulses did not each pay " + pulse + ": paid " + (2000.0D - blood(player)));
				SculptingSessions.end(player, true);
				for (int i = 0; i < logs.size(); i++) {
					boolean standing = helper.getLevel().getBlockState(logs.get(i)).is(Blocks.OAK_LOG);
					helper.assertTrue(standing == (i >= reached), "log " + i + " standing=" + standing + " after felling "
							+ reached);
				}
				helper.assertTrue(LignumMortisSculpting.crawlSize(player) == 0, "the crawl outlived its felling");
				helper.assertTrue(!helper.getLevel().getEntitiesOfClass(ItemEntity.class,
								new AABB(logs.getFirst()).inflate(3.0D), item -> item.getItem().is(Items.OAK_LOG)).isEmpty(),
						"the felled logs dropped nothing");
			} finally {
				captured.close();
			}
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void lignumFellsAtOnceWhenItHasReachedEverything(GameTestHelper helper) {
		clear(helper, 1, 6, 2, 4);
		BlockPos low = helper.absolutePos(new BlockPos(4, 1, 2));
		BlockPos high = helper.absolutePos(new BlockPos(4, 2, 2));
		helper.getLevel().setBlock(low, Blocks.OAK_LOG.defaultBlockState(), 3);
		helper.getLevel().setBlock(high, Blocks.OAK_LOG.defaultBlockState(), 3);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "lignum_mortis", 2000.0D);
		aim(player, feet(helper, 1, 2), Vec3.atCenterOf(low));
		int[] tick = {0};
		helper.onEachTick(() -> {
			if (tick[0]++ < 35) project(player);
		});
		helper.runAtTickTime(38, () -> {
			try {
				helper.assertTrue(helper.getLevel().getBlockState(low).isAir() && helper.getLevel().getBlockState(high).isAir(),
						"a two-log tree the crawl had wholly reached did not fall while the verb was held");
			} finally {
				captured.close();
			}
			helper.succeed();
		});
	}

	/** CQ-2: a log the player may not break is refused before the price, so nothing is paid and nothing crawls. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void lignumRefusesBeforeAnyBloodWhereThePlayerMayNotBuild(GameTestHelper helper) {
		clear(helper, 1, 6, 2, 4);
		BlockPos log = helper.absolutePos(new BlockPos(4, 1, 2));
		helper.getLevel().setBlock(log, Blocks.OAK_LOG.defaultBlockState(), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "lignum_mortis", 2000.0D);
			player.setGameMode(net.minecraft.world.level.GameType.ADVENTURE);
			aim(player, feet(helper, 1, 2), Vec3.atCenterOf(log));
			helper.assertTrue(SculptingDispatcher.currentClaim(player, Verb.PROJECT).isPresent(),
					"Lignum Mortis did not claim the log, so the guard was never reached");
			float[] temper = HemoCapabilityAccess.getBloodTemper(player).snapshot();
			double spent = project(player);
			helper.assertTrue(spent == 0.0D && blood(player) == 2000.0D, "a refused crawl was paid for: " + blood(player));
			helper.assertTrue(java.util.Arrays.equals(temper, HemoCapabilityAccess.getBloodTemper(player).snapshot()),
					"a refused crawl spent Temper");
			helper.assertTrue(captured.said().contains(StaffMiningGameTests.REFUSED), "the refusal was silent: " + captured.said());
			// The verb HUD is told the claim was refused, for one cadence and a little more (round two P11).
			var refusals = captured.sent(com.vincenthuto.hemomancy.common.network.capa.harbinger.VerbRefusalPacket.class);
			helper.assertTrue(refusals.size() == 1, "the owner's HUD was not told of the refusal once: " + refusals.size());
			int cadence = SculptingDispatcher.currentClaim(player, Verb.PROJECT).orElseThrow().selected().sculpting().cadence();
			helper.assertTrue(refusals.getFirst().ticks() == Math.max(1, cadence) + SculptingDispatcher.REFUSAL_MARK_GRACE_TICKS,
					"the refusal mark's time is not a cadence and its grace: " + refusals.getFirst().ticks());
			helper.assertTrue(LignumMortisSculpting.crawlSize(player) == 0, "a crawl began where the player may not build");
			SculptingSessions.end(player, true);
			helper.assertTrue(helper.getLevel().getBlockState(log).is(Blocks.OAK_LOG), "a refused crawl felled the log");
		}
		helper.succeed();
	}

	/** CN-13: protection is asked on every pulse, so a crawl already running stops, unpaid, once the player may not build. */
	@GameTest(template = EMPTY, timeoutTicks = 80)
	public static void lignumStopsMidCrawlWhenThePlayerMayNoLongerBuild(GameTestHelper helper) {
		clear(helper, 1, 6, 2, 10);
		List<BlockPos> logs = new ArrayList<>();
		for (int y = 1; y <= 8; y++) {
			BlockPos log = helper.absolutePos(new BlockPos(4, y, 2));
			helper.getLevel().setBlock(log, Blocks.OAK_LOG.defaultBlockState(), 3);
			logs.add(log);
		}
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "lignum_mortis", 2000.0D);
		aim(player, feet(helper, 1, 2), Vec3.atCenterOf(logs.getFirst()));
		int[] tick = {0};
		double[] afterFirst = {Double.NaN};
		int[] reached = {0};
		helper.onEachTick(() -> {
			if (tick[0]++ >= 40) return;
			double spent = project(player);
			if (Double.isNaN(afterFirst[0]) && spent > 0.0D) {
				afterFirst[0] = blood(player);
				reached[0] = LignumMortisSculpting.crawlSize(player);
				player.setGameMode(net.minecraft.world.level.GameType.ADVENTURE);
			}
		});
		helper.runAtTickTime(45, () -> {
			try {
				helper.assertFalse(Double.isNaN(afterFirst[0]), "the crawl never paid its first pulse");
				helper.assertTrue(blood(player) == afterFirst[0], "a crawl kept paying after the player lost the right to build: "
						+ (afterFirst[0] - blood(player)) + " mL more");
				helper.assertTrue(LignumMortisSculpting.crawlSize(player) <= reached[0],
						"the crawl grew from " + reached[0] + " to " + LignumMortisSculpting.crawlSize(player) + " while refused");
				helper.assertTrue(captured.said().contains(StaffMiningGameTests.REFUSED), "the refusal was silent: " + captured.said());
				SculptingSessions.end(player, true);
			} finally {
				captured.close();
			}
			helper.succeed();
		});
	}

	// ── Pyretic Forge: items, a block, and the brazier ──

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void pyreticSmeltsItemsWhereTheyLie(GameTestHelper helper) {
		clear(helper, 1, 6, 2, 3);
		ServerLevel level = helper.getLevel();
		Vec3 ground = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(4, 1, 2)));
		ItemEntity ore = new ItemEntity(level, ground.x, ground.y, ground.z, new ItemStack(Items.RAW_IRON, 5));
		ore.setDeltaMovement(Vec3.ZERO);
		ore.setNoGravity(true);
		level.addFreshEntity(ore);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "pyretic_forge", 2000.0D);
			aim(player, feet(helper, 1, 2), ore.getBoundingBox().getCenter());
			double spent = project(player);
			helper.assertTrue(spent > 0.0D, "the Sculpting did not pay for its pulse");
			helper.assertTrue(ore.getItem().getCount() == 5 - PyreticForgeRules.itemsPerPulse(1.0D, 1.0D),
					"one pulse by hand did not smelt two: " + ore.getItem().getCount() + " raw iron left");
			int ingots = level.getEntitiesOfClass(ItemEntity.class, new AABB(ore.blockPosition()).inflate(2.0D),
					item -> item.getItem().is(Items.IRON_INGOT)).stream().mapToInt(item -> item.getItem().getCount()).sum();
			helper.assertTrue(ingots == 2, "the smelted iron did not land where the ore lay: " + ingots);
			helper.assertTrue(captured.sent(com.vincenthuto.hemomancy.common.network.capa.harbinger.VerbRefusalPacket.class).isEmpty(),
					"a paid pulse told the HUD it was refused");
		} finally {
			level.getEntitiesOfClass(ItemEntity.class, new AABB(ore.blockPosition()).inflate(3.0D)).forEach(ItemEntity::discard);
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 80)
	public static void pyreticFiresSandIntoGlassInPlace(GameTestHelper helper) {
		clear(helper, 1, 6, 2, 3);
		BlockPos sand = helper.absolutePos(new BlockPos(4, 1, 2));
		helper.getLevel().setBlock(sand, Blocks.SAND.defaultBlockState(), 3);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "pyretic_forge", 2000.0D);
		aim(player, feet(helper, 1, 2), Vec3.atCenterOf(sand));
		int[] tick = {0};
		boolean[] heldAtFirst = {false};
		helper.onEachTick(() -> {
			int t = tick[0]++;
			if (t < 60 && helper.getLevel().getBlockState(sand).is(Blocks.SAND)) project(player);
			if (t == 1) heldAtFirst[0] = PyreticForgeSculpting.fedInto(player, sand) > 0.0D
					&& helper.getLevel().getBlockState(sand).is(Blocks.SAND);
		});
		helper.runAtTickTime(65, () -> {
			try {
				helper.assertTrue(heldAtFirst[0], "the first pulse did not start heating the sand without converting it");
				helper.assertTrue(helper.getLevel().getBlockState(sand).is(Blocks.GLASS),
						"held Projection did not fire the sand into glass in place: " + helper.getLevel().getBlockState(sand));
				helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(sand).inflate(2.0D)).isEmpty(),
						"an in-place conversion dropped an item");
			} finally {
				captured.close();
			}
			helper.succeed();
		});
	}

	/** CQ-2: sand the player may not break is refused before the price, so nothing is paid and nothing heats. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void pyreticRefusesBeforeAnyBloodWhereThePlayerMayNotBuild(GameTestHelper helper) {
		clear(helper, 1, 6, 2, 3);
		BlockPos sand = helper.absolutePos(new BlockPos(4, 1, 2));
		helper.getLevel().setBlock(sand, Blocks.SAND.defaultBlockState(), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "pyretic_forge", 2000.0D);
			player.setGameMode(net.minecraft.world.level.GameType.ADVENTURE);
			aim(player, feet(helper, 1, 2), Vec3.atCenterOf(sand));
			helper.assertTrue(SculptingDispatcher.currentClaim(player, Verb.PROJECT).isPresent(),
					"Pyretic Forge did not claim the sand, so the guard was never reached");
			float[] temper = HemoCapabilityAccess.getBloodTemper(player).snapshot();
			double spent = project(player);
			helper.assertTrue(spent == 0.0D && blood(player) == 2000.0D, "a refused heating was paid for: " + blood(player));
			helper.assertTrue(java.util.Arrays.equals(temper, HemoCapabilityAccess.getBloodTemper(player).snapshot()),
					"a refused heating spent Temper");
			helper.assertTrue(captured.said().contains(StaffMiningGameTests.REFUSED), "the refusal was silent: " + captured.said());
			helper.assertTrue(PyreticForgeSculpting.fedInto(player, sand) == 0.0D, "refused sand was heated");
			helper.assertTrue(helper.getLevel().getBlockState(sand).is(Blocks.SAND), "refused sand changed");
		}
		helper.succeed();
	}

	/** CN-13: a heating already running stops, unpaid, once the player may not build. */
	@GameTest(template = EMPTY, timeoutTicks = 80)
	public static void pyreticStopsMidHeatingWhenThePlayerMayNoLongerBuild(GameTestHelper helper) {
		clear(helper, 1, 6, 2, 3);
		BlockPos sand = helper.absolutePos(new BlockPos(4, 1, 2));
		helper.getLevel().setBlock(sand, Blocks.SAND.defaultBlockState(), 3);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "pyretic_forge", 2000.0D);
		aim(player, feet(helper, 1, 2), Vec3.atCenterOf(sand));
		int[] tick = {0};
		double[] afterFirst = {Double.NaN};
		double[] fed = {0.0D};
		helper.onEachTick(() -> {
			if (tick[0]++ >= 40) return;
			double spent = project(player);
			if (Double.isNaN(afterFirst[0]) && spent > 0.0D) {
				afterFirst[0] = blood(player);
				fed[0] = PyreticForgeSculpting.fedInto(player, sand);
				player.setGameMode(net.minecraft.world.level.GameType.ADVENTURE);
			}
		});
		helper.runAtTickTime(45, () -> {
			try {
				helper.assertFalse(Double.isNaN(afterFirst[0]), "the heating never paid its first pulse");
				helper.assertTrue(blood(player) == afterFirst[0], "a heating kept paying after the player lost the right to build: "
						+ (afterFirst[0] - blood(player)) + " mL more");
				helper.assertTrue(helper.getLevel().getBlockState(sand).is(Blocks.SAND),
						"refused sand was fired into " + helper.getLevel().getBlockState(sand));
				helper.assertTrue(PyreticForgeSculpting.fedInto(player, sand) <= fed[0], "refused sand kept heating");
				helper.assertTrue(captured.said().contains(StaffMiningGameTests.REFUSED), "the refusal was silent: " + captured.said());
				SculptingSessions.end(player, true);
			} finally {
				captured.close();
			}
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void plainProjectionIgnoresASculptingsInfusionRecipe(GameTestHelper helper) {
		clear(helper, 1, 6, 2, 3);
		BlockPos sand = helper.absolutePos(new BlockPos(4, 1, 2));
		helper.getLevel().setBlock(sand, Blocks.SAND.defaultBlockState(), 3);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, null, 2000.0D);
		aim(player, feet(helper, 1, 2), Vec3.atCenterOf(sand));
		int[] tick = {0};
		helper.onEachTick(() -> {
			if (tick[0]++ < 40) project(player);
		});
		helper.runAtTickTime(45, () -> {
			try {
				helper.assertTrue(helper.getLevel().getBlockState(sand).is(Blocks.SAND),
						"plain Projection ran Pyretic Forge's sand recipe as an infusion");
				// Core-loop Track 6: blood nothing else takes lands as a stain, once, for a few mL.
				helper.assertTrue(helper.getLevel().getBlockState(sand.west()).is(BlockInit.blood_stain.get()),
						"plain Projection on the sand's face laid no stain");
				helper.assertTrue(Math.abs(2000.0D - blood(player)
								- com.vincenthuto.hemomancy.common.world.ground.GroundRunRules.STAIN_ML) < 1.0E-6D,
						"plain Projection on sand spent more than one stain: " + blood(player));
			} finally {
				captured.close();
			}
			helper.succeed();
		});
	}

	/** A lit Iron Brazier holding {@code offering}, at (4, 1, 2) of the test. */
	private static BlockPos litBrazier(GameTestHelper helper, ItemStack offering) {
		BlockPos pos = helper.absolutePos(new BlockPos(4, 1, 2));
		helper.getLevel().setBlockAndUpdate(pos, BlockInit.iron_brazier.get().defaultBlockState()
				.setValue(BrazierBlock.RITUAL_PHASE, 1));
		IronBrazierBlockEntity brazier = (IronBrazierBlockEntity) helper.getLevel().getBlockEntity(pos);
		brazier.setPooledBlood(BrazierBlock.BLOOD_TO_LIGHT);
		helper.assertTrue(brazier.insertOffering(null, offering), "the brazier did not take its offering");
		return pos;
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void theBrazierTransmutesALogUnderPyreticForge(GameTestHelper helper) {
		clear(helper, 1, 6, 2, 3);
		BlockPos pos = litBrazier(helper, new ItemStack(Items.OAK_LOG));
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "pyretic_forge", 1000.0D);
			aim(player, feet(helper, 1, 2), Vec3.atCenterOf(pos));
			for (int i = 0; i < 70 && ((IronBrazierBlockEntity) helper.getLevel().getBlockEntity(pos)).hasOffering(); i++) {
				absorbTick(player);
			}
			IronBrazierBlockEntity brazier = (IronBrazierBlockEntity) helper.getLevel().getBlockEntity(pos);
			helper.assertTrue(!brazier.hasOffering(), "held Absorption did not draw the log out");
			int charcoal = player.getInventory().countItem(Items.CHARCOAL);
			helper.assertTrue(charcoal == 2, "the log gave " + charcoal + " charcoal, not 2");
			helper.assertTrue(!BrazierBlock.isLit(helper.getLevel().getBlockState(pos)), "the fire did not go out with the offering");
			helper.assertTrue(Math.abs(1000.0D - blood(player) - 20.0D) < 1.0E-6D,
					"the transmutation did not cost its 20 mL: paid " + (1000.0D - blood(player)));
			player.stopUsingItem();
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void theSameOfferingAnswersOnlyItsSculpting(GameTestHelper helper) {
		clear(helper, 1, 6, 2, 3);
		BlockPos pos = litBrazier(helper, new ItemStack(Items.OAK_LOG));
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "glacial_grasp", 1000.0D);
			aim(player, feet(helper, 1, 2), Vec3.atCenterOf(pos));
			for (int i = 0; i < 70; i++) absorbTick(player);
			IronBrazierBlockEntity brazier = (IronBrazierBlockEntity) helper.getLevel().getBlockEntity(pos);
			helper.assertTrue(brazier.hasOffering() && brazier.getOfferingForMatching().is(Items.OAK_LOG),
					"a log was transmuted under Glacial Grasp, which has no log recipe");
			helper.assertTrue(player.getInventory().countItem(Items.CHARCOAL) == 0, "charcoal came from the wrong selection");
			helper.assertTrue(BrazierTransmutationRite.plan(helper.getLevel(), new ItemStack(Items.ICE),
							Hemomancy.rloc("glacial_grasp")).map(plan -> plan.result().is(Items.PACKED_ICE)).orElse(false),
					"ice does not pack under Glacial Grasp");
			helper.assertTrue(!BrazierTransmutationRite.INSTANCE.accepts(new ItemStack(ItemInit.memory_blood_binding.get())),
					"the transmutation rite reached for an offering the memory rite owns");
			player.stopUsingItem();
		}
		helper.succeed();
	}

	/** CQ-5, NQ-01-12: a bone becomes bone meal only under Vital Effusion, and gravel flint only under Pyretic Forge. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aBoneGivesBoneMealAndSporesUnderVitalEffusion(GameTestHelper helper) {
		clear(helper, 1, 6, 2, 3);
		BlockPos pos = litBrazier(helper, new ItemStack(Items.BONE));
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, null, 1000.0D);
			aim(player, feet(helper, 1, 2), Vec3.atCenterOf(pos));
			for (int i = 0; i < 70; i++) absorbTick(player);
			helper.assertTrue(((IronBrazierBlockEntity) helper.getLevel().getBlockEntity(pos)).hasOffering()
					&& player.getInventory().countItem(Items.BONE_MEAL) == 0, "a bone was ground with nothing selected");
			player.stopUsingItem();
			prepare(player, "vital_effusion", 1000.0D);
			for (int i = 0; i < 70 && ((IronBrazierBlockEntity) helper.getLevel().getBlockEntity(pos)).hasOffering(); i++) {
				absorbTick(player);
			}
			helper.assertTrue(player.getInventory().countItem(Items.BONE_MEAL) == 5
							&& player.getInventory().countItem(ItemInit.ruinous_spores.get()) == 1,
					"the bone did not give five bone meal and a ruinous by-product under Vital Effusion");
			helper.assertTrue(BrazierTransmutationRite.plan(helper.getLevel(), new ItemStack(Items.GRAVEL),
							Hemomancy.rloc("vital_effusion")).isEmpty()
					&& BrazierTransmutationRite.plan(helper.getLevel(), new ItemStack(Items.GRAVEL),
							Hemomancy.rloc("pyretic_forge")).map(plan -> plan.result().is(Items.FLINT)).orElse(false),
					"gravel does not give flint under Pyretic Forge alone");
			player.stopUsingItem();
		}
		helper.succeed();
	}

	/** CB-18, NQ-01-13: packed ice takes the bowl's ice and three more in the off hand, all spent, for 30 mL. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void iceWantsThreeMoreInTheOffHand(GameTestHelper helper) {
		clear(helper, 1, 6, 2, 3);
		BlockPos pos = litBrazier(helper, new ItemStack(Items.ICE));
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "glacial_grasp", 1000.0D);
			aim(player, feet(helper, 1, 2), Vec3.atCenterOf(pos));
			player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.ICE, 2));
			for (int i = 0; i < 70; i++) absorbTick(player);
			IronBrazierBlockEntity brazier = (IronBrazierBlockEntity) helper.getLevel().getBlockEntity(pos);
			helper.assertTrue(brazier.hasOffering() && player.getOffhandItem().getCount() == 2,
					"ice was packed with only two more in the off hand");
			player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.ICE, 3));
			for (int i = 0; i < 70 && brazier.hasOffering(); i++) absorbTick(player);
			helper.assertTrue(!brazier.hasOffering() && player.getInventory().countItem(Items.PACKED_ICE) == 1,
					"four ice did not make one packed ice");
			helper.assertTrue(player.getOffhandItem().isEmpty(), "the three off-hand ice were not spent");
			helper.assertTrue(Math.abs(1000.0D - blood(player) - 30.0D) < 1.0E-6D,
					"packing ice did not cost 30 mL: paid " + (1000.0D - blood(player)));
			helper.assertTrue(BrazierTransmutationRite.plan(helper.getLevel(), new ItemStack(Items.PACKED_ICE),
							Hemomancy.rloc("glacial_grasp")).map(plan -> plan.result().is(Items.BLUE_ICE)
							&& plan.catalystCount() == 3 && plan.bloodCost() == 60.0D).orElse(false),
					"blue ice does not take three more packed ice for 60 mL");
			player.stopUsingItem();
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aCatalystRecipeWaitsForItsOffHand(GameTestHelper helper) {
		clear(helper, 1, 6, 2, 3);
		BlockPos pos = litBrazier(helper, new ItemStack(ItemInit.hematic_iron_powder.get()));
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "pyretic_forge", 1000.0D);
			aim(player, feet(helper, 1, 2), Vec3.atCenterOf(pos));
			for (int i = 0; i < 70; i++) absorbTick(player);
			IronBrazierBlockEntity brazier = (IronBrazierBlockEntity) helper.getLevel().getBlockEntity(pos);
			helper.assertTrue(brazier.hasOffering(), "the powder was worked without coal in the off hand");
			player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.COAL, 2));
			for (int i = 0; i < 70 && brazier.hasOffering(); i++) absorbTick(player);
			helper.assertTrue(!brazier.hasOffering() && player.getInventory().countItem(ItemInit.hematic_iron_scrap.get()) == 1,
					"the powder did not become scrap with coal in the off hand");
			helper.assertTrue(player.getOffhandItem().getCount() == 1, "the catalyst was not spent once");
			player.stopUsingItem();
		}
		helper.succeed();
	}

	// ── Tendon Line: a block for the throw, anything for the haul ──

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void theTendonLineIsThrownByProjectionAndHauledByAbsorption(GameTestHelper helper) {
		clear(helper, 1, 8, 2, 4);
		BlockPos wall = helper.absolutePos(new BlockPos(8, 2, 2));
		helper.getLevel().setBlock(wall, Blocks.STONE.defaultBlockState(), 3);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "tendon_line", 1000.0D);
		aim(player, feet(helper, 1, 2), Vec3.atCenterOf(wall).add(-0.5D, 0.0D, 0.0D));
		double throwPrice = price(player, "tendon_line", Verb.PROJECT);
		TendonAnchor anchor = HemoCapabilityAccess.getTendonAnchor(player);
		double[] afterThrow = {0.0D};
		Vec3[] pull = {Vec3.ZERO};
		Vec3[] toward = {Vec3.ZERO};
		float[] fall = {-1.0F};
		boolean[] arrived = {false};
		int[] tick = {0};
		helper.onEachTick(() -> {
			int t = tick[0]++;
			if (t == 0) {
				project(player);
				afterThrow[0] = blood(player);
			} else if (t == 2) {
				// The pulse clock is per player: the haul's first pulse comes on a later tick than the throw's.
				player.fallDistance = 6.0F;
				toward[0] = anchor.target().subtract(player.position()).normalize();
				absorbTick(player);
				pull[0] = player.getDeltaMovement();
				fall[0] = player.fallDistance;
			} else if (t == 4) {
				aim(player, Vec3.atCenterOf(wall).add(-0.5D - 1.2D, -0.9D, 0.0D), Vec3.atCenterOf(wall));
				absorbTick(player);
				arrived[0] = !anchor.isSet();
			}
		});
		helper.runAtTickTime(8, () -> {
			try {
				helper.assertTrue(captured.sent(PacketSyncTendonAnchor.class).stream().anyMatch(PacketSyncTendonAnchor::set),
						"the owner was not told where the line is rooted");
				helper.assertTrue(Math.abs(1000.0D - afterThrow[0] - throwPrice) < 1.0E-6D,
						"the throw did not cost its price: paid " + (1000.0D - afterThrow[0]));
				helper.assertTrue(pull[0].dot(toward[0]) > 0.25D, "held Absorption did not haul toward the anchor: " + pull[0]);
				helper.assertTrue(fall[0] == 0.0F, "the haul did not clear fall distance: " + fall[0]);
				helper.assertTrue(blood(player) < afterThrow[0], "the haul pulses paid nothing");
				helper.assertTrue(arrived[0], "arriving at the anchor did not loosen the line");
				player.stopUsingItem();
			} finally {
				captured.close();
			}
			helper.succeed();
		});
	}

	/** Sneaking and using the rooted block lets the line go; a plain use, or a sneaking use elsewhere, does not. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void sneakingOnTheRootedBlockLetsTheLineGo(GameTestHelper helper) {
		BlockPos wall = helper.absolutePos(new BlockPos(8, 2, 2));
		BlockPos other = helper.absolutePos(new BlockPos(8, 2, 4));
		helper.getLevel().setBlock(wall, Blocks.STONE.defaultBlockState(), 3);
		helper.getLevel().setBlock(other, Blocks.STONE.defaultBlockState(), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "tendon_line", 1000.0D);
			TendonAnchor anchor = HemoCapabilityAccess.getTendonAnchor(player);
			anchor.set(wall, Direction.WEST, helper.getLevel().dimension());
			helper.assertFalse(use(player, wall, false).isCanceled(), "a plain use of the rooted block was taken");
			helper.assertTrue(anchor.isSet(), "a plain use let the line go");
			helper.assertFalse(use(player, other, true).isCanceled(), "a sneaking use of another block was taken");
			helper.assertTrue(anchor.isSet(), "a sneaking use of another block let the line go");
			helper.assertTrue(use(player, wall, true).isCanceled(), "the sneaking use of the rooted block went through to the block");
			helper.assertFalse(anchor.isSet(), "sneaking on the rooted block did not let the line go");
		}
		helper.succeed();
	}

	private static net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock use(ServerPlayer player,
			BlockPos pos, boolean sneaking) {
		player.setShiftKeyDown(sneaking);
		var event = new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock(player,
				InteractionHand.MAIN_HAND, pos, new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(pos),
						Direction.WEST, pos, false));
		net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(event);
		player.setShiftKeyDown(false);
		return event;
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void theTendonLineIsRootedOnTheAimedFace(GameTestHelper helper) {
		clear(helper, 1, 8, 2, 4);
		BlockPos wall = helper.absolutePos(new BlockPos(8, 2, 2));
		helper.getLevel().setBlock(wall, Blocks.STONE.defaultBlockState(), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "tendon_line", 1000.0D);
			aim(player, feet(helper, 1, 2), Vec3.atCenterOf(wall).add(-0.5D, 0.0D, 0.0D));
			project(player);
			TendonAnchor anchor = HemoCapabilityAccess.getTendonAnchor(player);
			helper.assertTrue(anchor.isSet() && anchor.pos().equals(wall) && anchor.face() == Direction.WEST,
					"Projection did not root the line on the wall's near face: " + anchor.pos() + " " + anchor.face());
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void theTendonLineSnapsBeyondItsReach(GameTestHelper helper) {
		clear(helper, 1, 8, 2, 4);
		BlockPos wall = helper.absolutePos(new BlockPos(8, 2, 2));
		helper.getLevel().setBlock(wall, Blocks.STONE.defaultBlockState(), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "tendon_line", 1000.0D);
			TendonAnchor anchor = HemoCapabilityAccess.getTendonAnchor(player);
			anchor.set(wall, Direction.WEST, helper.getLevel().dimension());
			double snap = TendonLineRules.detachRange(5.5D * TendonLineRules.REACH_SCALE);
			aim(player, Vec3.atCenterOf(wall).add(-snap - 3.0D, -1.0D, 0.0D), Vec3.atCenterOf(wall));
			absorbTick(player);
			helper.assertTrue(!anchor.isSet(), "the line held past " + snap + " blocks");
			player.stopUsingItem();
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void withoutTheLinePlainAbsorptionStillRootsAndDrains(GameTestHelper helper) {
		clear(helper, 1, 6, 2, 3);
		Pig pig = helper.spawn(EntityType.PIG, new BlockPos(4, 1, 2));
		pig.setNoAi(true);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "tendon_line", 500.0D);
			aim(player, feet(helper, 1, 2), pig.getBoundingBox().getCenter());
			helper.assertTrue(SculptingDispatcher.currentClaim(player, Verb.ABSORB).isEmpty(),
					"the haul claimed with no line rooted");
			float health = pig.getHealth();
			for (int i = 0; i < 12; i++) absorbTick(player);
			helper.assertTrue(pig.getHealth() < health, "plain Absorption no longer drains with an unrooted Tendon Line selected");
			player.stopUsingItem();
		} finally {
			pig.discard();
		}
		helper.succeed();
	}

	// ── Blood Binding: a creature ──

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aHeldBindingHoldsWhileBloodFlowsAndConstrictsAfter(GameTestHelper helper) {
		clear(helper, 1, 6, 2, 3);
		Pig pig = helper.spawn(EntityType.PIG, new BlockPos(4, 1, 2));
		pig.setNoAi(true);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "blood_binding", 1000.0D);
			aim(player, feet(helper, 1, 2), pig.getBoundingBox().getCenter());
			project(player);
			var hold = pig.getEffect(EffectInit.blood_binding);
			var tail = pig.getEffect(EffectInit.constricted);
			helper.assertTrue(hold != null && hold.getDuration() <= 20, "the pig was not held for one pulse: " + hold);
			helper.assertTrue(tail != null && tail.getDuration() > hold.getDuration(),
					"no constricted tail outlasts the hold: " + tail);
			helper.assertTrue(blood(player) < 1000.0D, "the bind paid nothing");
		} finally {
			pig.discard();
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aBossTakesItsBindingsAndThenRefuses(GameTestHelper helper) {
		clear(helper, 1, 6, 2, 3);
		var warden = helper.spawn(EntityType.WARDEN, new BlockPos(4, 1, 2));
		warden.setNoAi(true);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			int total = com.vincenthuto.hemomancy.common.manipulation.animus.BossBindingRules.total(warden.getMaxHealth());
			helper.assertTrue(total == 5, "a Warden should take five bindings, not " + total);
			helper.assertFalse(com.vincenthuto.hemomancy.common.manipulation.animus.BossBindings.tryTake(player, warden, 0.0D),
					"a roll under the slip chance still bound the boss");
			helper.assertTrue(com.vincenthuto.hemomancy.common.manipulation.animus.BossBindings.taken(warden) == 0,
					"a slipped binding counted");
			helper.assertTrue(com.vincenthuto.hemomancy.common.manipulation.animus.BossBindings.waiting(player, warden),
					"the held form does not wait after a slip");
			for (int i = 0; i < total; i++) {
				helper.assertTrue(com.vincenthuto.hemomancy.common.manipulation.animus.BossBindings.tryTake(player, warden, 0.999D),
						"binding " + (i + 1) + " of " + total + " slipped on a high roll");
			}
			helper.assertTrue(com.vincenthuto.hemomancy.common.manipulation.animus.BossBindings.spent(warden), "the Warden is not spent");
			helper.assertFalse(com.vincenthuto.hemomancy.common.manipulation.animus.BossBindings.tryTake(player, warden, 0.999D),
					"a spent body was bound again");

			prepare(player, "blood_binding", 1000.0D);
			aim(player, feet(helper, 1, 2), warden.getBoundingBox().getCenter());
			project(player);
			helper.assertTrue(!warden.hasEffect(EffectInit.blood_binding), "the held form bound a spent boss");
			helper.assertTrue(!warden.hasEffect(EffectInit.constricted), "a spent boss was still slowed (NQ-04-4)");
		} finally {
			warden.discard();
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aBindingNeverTakesWhatTheCasterCouldNotStrike(GameTestHelper helper) {
		clear(helper, 1, 6, 2, 3);
		Wolf wolf = helper.spawn(EntityType.WOLF, new BlockPos(4, 1, 2));
		wolf.setNoAi(true);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			wolf.tame(player);
			prepare(player, "blood_binding", 1000.0D);
			aim(player, feet(helper, 1, 2), wolf.getBoundingBox().getCenter());
			helper.assertTrue(SculptingDispatcher.currentClaim(player, Verb.PROJECT).isEmpty(), "the binding claimed the caster's own wolf");
			project(player);
			helper.assertTrue(!wolf.hasEffect(EffectInit.blood_binding), "the caster's own wolf was bound");
		} finally {
			wolf.discard();
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aChainBindingRunsOnIntoTheNearestCreatures(GameTestHelper helper) {
		clear(helper, 1, 7, 2, 3);
		List<Pig> pigs = new ArrayList<>();
		for (int x = 4; x <= 6; x++) {
			Pig pig = helper.spawn(EntityType.PIG, new BlockPos(x, 1, 2));
			pig.setNoAi(true);
			pigs.add(pig);
		}
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "chain_blood_binding", 1000.0D);
			aim(player, feet(helper, 1, 2), pigs.getFirst().getBoundingBox().getCenter());
			project(player);
			helper.assertTrue(pigs.stream().allMatch(pig -> pig.hasEffect(EffectInit.blood_binding)),
					"the chain did not run on into the two pigs beside the first: bound "
							+ pigs.stream().map(pig -> pig.hasEffect(EffectInit.blood_binding)).toList()
							+ ", paid " + (1000.0D - blood(player)) + ", pigs at "
							+ pigs.stream().map(pig -> pig.blockPosition().toShortString()
									+ (helper.getLevel().isPositionEntityTicking(pig.blockPosition()) ? "" : " (not ticking)")).toList()
							+ ", aim hit " + player.pick(8.0D, 0.0F, false).getType());
		} finally {
			pigs.forEach(Pig::discard);
		}
		helper.succeed();
	}

	// ── An awakened rite sigil still takes the blood first ──

	/**
	 * Stands an awakened sigil, puts {@code target} on its aim, and holds Projection there with {@code manipulation}
	 * selected: the Sculpting must claim the aim (or the test proves nothing), yet the rite takes the channel and no
	 * blood moves. {@code untouched} says the Sculpting's work did not happen.
	 */
	static void assertTheSigilWins(GameTestHelper helper, String manipulation, Consumer<BlockPos> target,
			Predicate<ServerPlayer> untouched) {
		assertTheSigilWins(helper, manipulation, target, target, untouched);
	}

	/** As above, with what stands behind the sigil (where the aim's ray ends) placed by {@code behind}. */
	static void assertTheSigilWins(GameTestHelper helper, String manipulation, Consumer<BlockPos> target,
			Consumer<BlockPos> behind, Predicate<ServerPlayer> untouched) {
		ServerLevel level = helper.getLevel();
		var center = helper.absolutePos(new BlockPos(12, 70, 12));
		var recipe = com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe.getRiteByLocation(
				level, Hemomancy.rloc("cardinal_rite/apotheos_rite"));
		try (CapturedPlayer captured = CapturedPlayer.create(helper).online()) {
			ServerPlayer actor = captured.player;
			var rite = com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite.interactive(
					actor.getUUID(), center, recipe.getId(), 3600, 9, 7, false, 6, 32);
			for (int i = 0; i < 32; i++) rite.fillAnchor(i, 50);
			helper.assertTrue(rite.enterInscription(), "the fixture rite did not enter inscription");
			var id = Hemomancy.rloc("bastion");
			var sigil = com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRegistry.get(id);
			var socket = recipe.getCeremony().supportSockets().getFirst();
			var occupied = new java.util.HashSet<BlockPos>();
			for (var anchor : recipe.getCeremony().anchors()) occupied.add(new BlockPos(anchor.x(), 0, anchor.z()));
			var placement = com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilPlacementRules.resolveSupportPlacement(
					new BlockPos(socket.x(), 0, socket.z()), sigil.nodes(), occupied);
			var node = sigil.nodes().getFirst();
			rite.setSigilProgress(id.toString(), sigil.nodes().size());
			rite.awakenSigil(id.toString());
			var saved = com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData.get(level);
			saved.startRite(rite);
			prepare(actor, manipulation, 2000.0D);
			HemoCapabilityAccess.getInitiatoryDegree(actor).setDegreeNumber(7);
			var floor = center.offset(placement.getX() + (int) Math.round(node.x()), socket.y(),
					placement.getZ() + (int) Math.round(node.z()));
			var backdrop = floor.offset(0, 1, 3);
			var oldFloor = level.getBlockState(floor);
			var oldBackdrop = level.getBlockState(backdrop);
			target.accept(floor);
			behind.accept(backdrop);
			var aim = com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteTargetGeometry.sigilAimPoint(
					center, floor.above(), placement.getX(), placement.getZ(), node.x(), node.z());
			actor.setPos(aim.x, aim.y - actor.getEyeHeight(), aim.z - 2);
			actor.lookAt(EntityAnchorArgument.Anchor.EYES, aim);
			try {
				helper.assertTrue(SculptingDispatcher.currentClaim(actor, Verb.PROJECT).isPresent(),
						manipulation + " would not claim what stands at the sigil, so the test proves nothing");
				for (int tick = 0; tick < 3; tick++) project(actor);
				helper.assertTrue(HemoCapabilityAccess.getBloodVolume(actor).getBloodVolume() == 2000.0D,
						"the rite's claim moved blood under " + manipulation);
				helper.assertTrue(untouched.test(actor), manipulation + " worked a target the awakened sigil claims");
			} finally {
				saved.removeRite(actor.getUUID());
				level.setBlock(floor, oldFloor, 3);
				level.setBlock(backdrop, oldBackdrop, 3);
				level.getEntitiesOfClass(Pig.class, new AABB(floor).inflate(3.0D)).forEach(Pig::discard);
			}
		}
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void anAwakenedSigilStillTakesLignumMortisProjection(GameTestHelper helper) {
		assertTheSigilWins(helper, "lignum_mortis",
				floor -> helper.getLevel().setBlock(floor, Blocks.OAK_LOG.defaultBlockState(), 3),
				player -> LignumMortisSculpting.crawlSize(player) == 0);
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void anAwakenedSigilStillTakesPyreticForgeProjection(GameTestHelper helper) {
		BlockPos[] at = new BlockPos[1];
		assertTheSigilWins(helper, "pyretic_forge",
				pos -> {
					if (at[0] == null) at[0] = pos;
					helper.getLevel().setBlock(pos, Blocks.SAND.defaultBlockState(), 3);
				},
				player -> PyreticForgeSculpting.fedInto(player, at[0]) == 0.0D
						&& PyreticForgeSculpting.fedInto(player, at[0].offset(0, 1, 3)) == 0.0D);
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void anAwakenedSigilStillTakesTheTendonLineThrow(GameTestHelper helper) {
		assertTheSigilWins(helper, "tendon_line",
				floor -> helper.getLevel().setBlock(floor, Blocks.STONE.defaultBlockState(), 3),
				player -> !HemoCapabilityAccess.getTendonAnchor(player).isSet());
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void anAwakenedSigilStillTakesABindingProjection(GameTestHelper helper) {
		Pig[] pig = new Pig[1];
		assertTheSigilWins(helper, "blood_binding",
				floor -> {
					helper.getLevel().setBlock(floor, Blocks.STONE.defaultBlockState(), 3);
					pig[0] = EntityType.PIG.create(helper.getLevel());
					pig[0].moveTo(floor.getX() + 0.5D, floor.getY() + 1.0D, floor.getZ() + 0.5D);
					pig[0].setNoAi(true);
					helper.getLevel().addFreshEntity(pig[0]);
				},
				floor -> helper.getLevel().setBlock(floor, Blocks.STONE.defaultBlockState(), 3),
				player -> !pig[0].hasEffect(EffectInit.blood_binding));
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void anAwakenedSigilStillTakesGougeCutting(GameTestHelper helper) {
		BlockPos[] at = new BlockPos[1];
		assertTheSigilWins(helper, "vascular_dowsing",
				floor -> {
					if (at[0] == null) at[0] = floor;
					helper.getLevel().setBlock(floor, Blocks.STONE.defaultBlockState(), 3);
				},
				player -> !helper.getLevel().getBlockState(at[0].above()).is(BlockInit.blood_gouge.get()));
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void anAwakenedSigilStillTakesTheLuxTrace(GameTestHelper helper) {
		BlockPos[] at = new BlockPos[1];
		assertTheSigilWins(helper, "hematic_beacon",
				floor -> {
					if (at[0] == null) at[0] = floor;
					helper.getLevel().setBlock(floor, Blocks.STONE.defaultBlockState(), 3);
				},
				player -> !helper.getLevel().getBlockState(at[0].above()).is(BlockInit.lux_trace.get())
						&& com.vincenthuto.hemomancy.common.manipulation.lux.LuxTraceSculpting.patternSize(player) == 0);
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void anAwakenedSigilStillTakesLivingLand(GameTestHelper helper) {
		BlockPos[] at = new BlockPos[1];
		assertTheSigilWins(helper, "living_land",
				floor -> {
					if (at[0] == null) at[0] = floor;
					helper.getLevel().setBlock(floor, Blocks.STONE.defaultBlockState(), 3);
				},
				player -> !com.vincenthuto.hemomancy.common.manipulation.animus.LivingLandSculpting.hasPatch(player)
						&& helper.getLevel().getBlockState(at[0]).is(Blocks.STONE));
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void anAwakenedSigilStillTakesVitalEffusion(GameTestHelper helper) {
		BlockPos[] at = new BlockPos[1];
		assertTheSigilWins(helper, "vital_effusion",
				floor -> {
					if (at[0] == null) at[0] = floor;
					helper.getLevel().setBlock(floor, Blocks.MOSS_BLOCK.defaultBlockState(), 3);
				},
				player -> helper.getLevel().getBlockState(at[0].above()).isAir());
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void anAwakenedSigilStillTakesALash(GameTestHelper helper) {
		assertTheSigilWins(helper, "searing_lash",
				floor -> helper.getLevel().setBlock(floor, Blocks.STONE.defaultBlockState(), 3),
				player -> !com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandService.isOpen(player));
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void anAwakenedSigilStillTakesAPlant(GameTestHelper helper) {
		// No memory holds a plant strand of its own now; the kit is lent to one for this case.
		var memory = ManipulationInit.getByName("cryogenic_pulse");
		var before = memory.getSculpting(Verb.PROJECT);
		var plant = com.vincenthuto.hemomancy.common.manipulation.verb.strand.Strands.plant(20.0D, 1.0F);
		if (before.isEmpty()) memory.setProjectionSculpting(() -> plant);
		assertTheSigilWins(helper, "cryogenic_pulse",
				floor -> helper.getLevel().setBlock(floor, Blocks.STONE.defaultBlockState(), 3),
				player -> !com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandService.isOpen(player));
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void anAwakenedSigilStillTakesLodeDraw(GameTestHelper helper) {
		BlockPos[] at = new BlockPos[1];
		assertTheSigilWins(helper, "lode_draw",
				floor -> {
					if (at[0] == null) at[0] = floor;
					helper.getLevel().setBlock(floor, Blocks.IRON_ORE.defaultBlockState(), 3);
				},
				player -> com.vincenthuto.hemomancy.common.manipulation.ferric.LodeDrawSculpting.drawSize(player) == 0
						&& helper.getLevel().getBlockState(at[0]).is(Blocks.IRON_ORE));
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void anAwakenedSigilStillTakesHematicHewing(GameTestHelper helper) {
		BlockPos[] at = new BlockPos[1];
		assertTheSigilWins(helper, "hematic_hewing",
				floor -> {
					if (at[0] == null) at[0] = floor;
					helper.getLevel().setBlock(floor, Blocks.STONE.defaultBlockState(), 3);
				},
				player -> helper.getLevel().getBlockState(at[0]).is(Blocks.STONE));
		helper.succeed();
	}

	// ── Balance pins: blood and work per second ──

	private record Pin(String manipulation, Verb verb, double bloodPerSecond, int cadence, double reachScale) {
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void eachSculptingCostsAndWorksAsPinned(GameTestHelper helper) {
		List<Pin> pins = List.of(
				new Pin("lignum_mortis", Verb.PROJECT, 16.0D, 10, 2.2D),
				new Pin("canopy_mortis", Verb.PROJECT, 16.0D, 10, 2.2D),
				new Pin("worked_lignum", Verb.PROJECT, 16.0D, 10, 2.2D),
				new Pin("pyretic_forge", Verb.PROJECT, 40.0D, 10, 1.0D),
				new Pin("tendon_line", Verb.PROJECT, 10.0D, 20, 3.0D),
				new Pin("tendon_line", Verb.ABSORB, 12.0D, 1, 1.0D),
				new Pin("blood_binding", Verb.PROJECT, 20.0D, 10, 1.0D),
				new Pin("lingering_blood_binding", Verb.PROJECT, 24.0D, 10, 1.0D),
				new Pin("chain_blood_binding", Verb.PROJECT, 32.0D, 10, 1.0D),
				new Pin("blood_lattice", Verb.PROJECT, 44.0D, 10, 1.0D),
				// Core-loop Track 6: the ground network's Sculptings.
				new Pin("vascular_dowsing", Verb.PROJECT, 15.0D, 4, 1.0D),
				new Pin("hematic_beacon", Verb.PROJECT, 12.0D, 10, 1.5D),
				new Pin("blood_eclipse", Verb.ABSORB, 10.0D, 10, 1.5D),
				new Pin("sanguine_magnetism", Verb.ABSORB, 8.0D, 5, 2.0D),
				// Core-loop Track 7: growth on demand.
				new Pin("vital_effusion", Verb.PROJECT, 15.0D, 20, 1.0D),
				// Core-loop Track 12: strands, two pulses a second; the reach scale is the form's.
				new Pin("blood_lash", Verb.PROJECT, 15.0D, 10, 1.0D),
				new Pin("searing_lash", Verb.PROJECT, 60.0D, 10, 1.0D),
				new Pin("hematic_flare", Verb.PROJECT, 40.0D, 10, 1.0D),
				new Pin("blood_eclipse", Verb.PROJECT, 60.0D, 10, 1.0D),
				new Pin("glacial_grasp", Verb.PROJECT, 40.0D, 10, 1.0D),
				// Core loop Track 8: the Staff's projection mining.
				new Pin("lode_draw", Verb.PROJECT, 24.0D, 10, 1.0D),
				new Pin("hematic_hewing", Verb.PROJECT, 36.0D, 20, 1.0D),
				// Core-loop Track 9: Living Land wakes ground; Venous Cling's root (its upkeep is ClingRules').
				new Pin("living_land", Verb.PROJECT, 30.0D, 10, 1.5D),
				new Pin("venous_cling", Verb.PROJECT, 6.0D, 20, 1.0D),
				// Core-loop round two P4: the second wave (Glacial Grasp's narrow freeze is pinned in SecondSculptingsGameTests).
				new Pin("sanguine_mending", Verb.PROJECT, 40.0D, 10, 1.0D),
				// Round two P5: Crimson Flame Conjuration's main Sculpting is its plant strand; the kindling is pinned below.
				new Pin("crimson_flame_conjuration", Verb.PROJECT, 35.0D, 10, 1.0D),
				new Pin("furnace_veins", Verb.PROJECT, 12.0D, 10, 1.0D),
				new Pin("glacial_rampart", Verb.PROJECT, 48.0D, 5, 1.5D),
				new Pin("ferric_rampart", Verb.PROJECT, 40.0D, 10, 1.5D),
				new Pin("glacial_grasp", Verb.ABSORB, 4.0D, 10, 1.5D),
				new Pin("carrion_communion", Verb.ABSORB, 8.0D, 10, 1.5D),
				// Core-loop round two P5: the second wave of strands; the reach scale is the form's.
				new Pin("gloam_laceration", Verb.PROJECT, 40.0D, 10, 1.0D),
				new Pin("cruor_javelin", Verb.PROJECT, 50.0D, 10, 1.0D),
				new Pin("blood_needle_lance", Verb.PROJECT, 35.0D, 10, 1.0D),
				new Pin("synaptic_jolt", Verb.PROJECT, 40.0D, 10, 1.0D),
				new Pin("ferric_spikes", Verb.PROJECT, 50.0D, 10, 1.0D));
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			for (Pin pin : pins) {
				prepare(player, pin.manipulation(), 1000.0D);
				VerbSculpting sculpting = SculptingDispatcher.selected(player, pin.verb()).orElseThrow(
						() -> new AssertionError(pin.manipulation() + " has no Sculpting of " + pin.verb())).sculpting();
				double perSecond = SculptingRules.bloodPerSecond(sculpting.bloodPerPulse(), sculpting.cadence());
				helper.assertTrue(Math.abs(perSecond - pin.bloodPerSecond()) < 1.0E-6D,
						pin.manipulation() + " " + pin.verb() + " costs " + perSecond + " mL/s, pinned " + pin.bloodPerSecond());
				helper.assertTrue(sculpting.cadence() == pin.cadence() && sculpting.reachScale() == pin.reachScale(),
						pin.manipulation() + " " + pin.verb() + " cadence or reach drifted");
			}
			// The kindling is Crimson Flame Conjuration's narrow Sculpting, tried ahead of its plant (round two P5).
			prepare(player, "crimson_flame_conjuration", 1000.0D);
			VerbSculpting kindling = SculptingDispatcher.sculptings(player, Verb.PROJECT).getFirst().sculpting();
			helper.assertTrue(kindling instanceof com.vincenthuto.hemomancy.common.manipulation.flammeus.FlameKindleSculpting,
					"the kindling is not tried first");
			double kindlingPerSecond = SculptingRules.bloodPerSecond(kindling.bloodPerPulse(), kindling.cadence());
			helper.assertTrue(Math.abs(kindlingPerSecond - 12.0D) < 1.0E-6D && kindling.cadence() == 10 && kindling.reachScale() == 1.5D,
					"the kindling drifted: " + kindlingPerSecond + " mL/s");
		}
		// Work per second by hand: the rot crawls 2 blocks a second near its origin; the forge smelts 4 items a
		// second; the haul pulls 0.34 blocks a tick.
		helper.assertTrue(com.vincenthuto.hemomancy.common.manipulation.mortem.LignumMortisRules.blocksPerSecond(0.0D) == 2.0D,
				"the rot's opening crawl rate drifted");
		helper.assertTrue(PyreticForgeRules.itemsPerPulse(1.0D, 1.0D) * 2 == 4, "the forge's hand rate drifted");
		helper.assertTrue(TendonLineRules.PULL_SPEED == 0.34D, "the haul's pull drifted");
		helper.succeed();
	}
}
