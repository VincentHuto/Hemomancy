package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.entity.manipulation.LivingLandStrikeEntity;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodProjectionItem;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.animus.LivingLandSculpting;
import com.vincenthuto.hemomancy.common.manipulation.animus.LivingLandTerrain;
import com.vincenthuto.hemomancy.common.manipulation.animus.LivingLandTerrainRules;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingDispatcher;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingSessions;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePurpose;
import com.vincenthuto.hemomancy.common.rite.zone.RiteSpace;
import com.vincenthuto.hemomancy.common.network.mausoleum.WardStrikePacket;
import com.vincenthuto.hemomancy.common.rite.zone.RiteSpaceSavedData;
import com.vincenthuto.hemomancy.common.world.livingland.LivingLandLedger;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Core-loop Track 9: Living Land with a real survival player through the real Projection chain. Each mode strikes an
 * enemy in front of its patch with the patch's own ground; a patch with nothing near rises and settles on release; no
 * block is gained or lost when the strike lands and returns, unloads, is killed, loses its caster, or finds its cell
 * taken; ground the player may not alter and a rite space refuse it before any blood is paid; with lifting turned off
 * the strike is conjured and the world is untouched.
 */
@GameTestHolder("living_land_validation")
@PrefixGameTestTemplate(false)
public final class LivingLandGameTests {
	private static final String EMPTY = "empty";
	/** Their own batch: the ground they lay is wide, and a neighbouring suite's fixtures must not land in it. */
	private static final String BATCH = "living_land_validation";
	private static final int SIZE = 16;
	/** The ground's top: stone from y=0 to here, air above. */
	private static final int GROUND = 4;

	private LivingLandGameTests() {
	}

	// ── Fixtures ──

	/** Stone ground from y=0 to {@link #GROUND}, air above, over the test's 17x17 square. */
	private static void ground(GameTestHelper helper) {
		for (int x = 0; x <= SIZE; x++) {
			for (int z = 0; z <= SIZE; z++) {
				for (int y = 0; y <= 11; y++) {
					helper.setBlock(new BlockPos(x, y, z), y <= GROUND ? Blocks.STONE : Blocks.AIR);
				}
			}
		}
	}

	/** Every non-air block in the test volume, counted by state: a conserved volume gives the same map. */
	private static Map<BlockState, Integer> census(GameTestHelper helper) {
		Map<BlockState, Integer> counts = new HashMap<>();
		ServerLevel level = helper.getLevel();
		for (BlockPos pos : BlockPos.betweenClosed(helper.absolutePos(new BlockPos(0, 0, 0)),
				helper.absolutePos(new BlockPos(SIZE + 2, 11, SIZE + 2)))) {
			BlockState state = level.getBlockState(pos);
			if (!state.isAir()) counts.merge(state, 1, Integer::sum);
		}
		return counts;
	}

	/** Active blood, Living Land known, equipped and selected, Temper carried (unless {@code dry}). */
	private static void prepare(ServerPlayer player, double blood, boolean dry) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(Math.max(volume.getMaxBloodVolume(), blood));
		volume.setBloodVolume(blood);
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
		known.getKnownManips().put(ManipulationInit.blood_projection.get(), new ManipLevel(1, 0));
		BloodManipulation landed = ManipulationInit.living_land.get();
		known.getKnownManips().put(landed, new ManipLevel(1, 0));
		known.setEquippedManipNames(new ArrayList<>(List.of("living_land")));
		known.setSelectedManip(landed);
		HemoCapabilityAccess.getBloodTemperament(player).setTemperAlignment(landed.getTend(), (float) landed.getAlignLevel() + 1.0F);
		if (!dry) TemperFixtures.carry(player);
	}

	private static void aim(ServerPlayer player, Vec3 feet, Vec3 at) {
		player.setPos(feet.x, feet.y, feet.z);
		player.lookAt(EntityAnchorArgument.Anchor.EYES, at);
		player.setOldPosAndRot();
		player.yHeadRotO = player.getYHeadRot();
	}

	private static void project(ServerPlayer player) {
		BloodProjectionItem.projectFromEntity(player.level(), player, 5.0D, 100.0D);
	}

	private static double blood(ServerPlayer player) {
		return HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
	}

	private static Husk husk(GameTestHelper helper, Vec3 feet) {
		Husk husk = EntityType.HUSK.create(helper.getLevel());
		husk.moveTo(feet.x, feet.y, feet.z, 0.0F, 0.0F);
		husk.setNoAi(true);
		husk.getAttribute(Attributes.MAX_HEALTH).setBaseValue(60.0D);
		husk.setHealth(60.0F);
		helper.getLevel().addFreshEntity(husk);
		return husk;
	}

	/** The strikes {@code player} has out anywhere near the test; other tests' strikes have other owners. */
	private static List<LivingLandStrikeEntity> strikesOf(GameTestHelper helper, ServerPlayer player) {
		return helper.getLevel().getEntitiesOfClass(LivingLandStrikeEntity.class,
				new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(SIZE + 8),
				strike -> player.getUUID().equals(strike.owner()));
	}

	private static Vec3 at(GameTestHelper helper, double x, double y, double z) {
		return Vec3.atLowerCornerOf(helper.absolutePos(BlockPos.ZERO)).add(x, y, z);
	}

	/**
	 * Holds Projection on {@code patch} for {@code holdTicks}, then lets go; {@code during} runs at {@code duringTick}
	 * while the channel is open, and {@code after} once the strikes have had time to come home.
	 */
	private static void hold(GameTestHelper helper, CapturedPlayer captured, Vec3 patch, int holdTicks, int duringTick,
			Consumer<ServerPlayer> during, int settleTicks, Consumer<ServerPlayer> after) {
		ServerPlayer player = captured.player;
		int[] tick = {0};
		helper.onEachTick(() -> {
			int now = tick[0]++;
			if (now < holdTicks) {
				player.lookAt(EntityAnchorArgument.Anchor.EYES, patch);
				player.setOldPosAndRot();
				project(player);
			}
			if (now == duringTick && during != null) during.accept(player);
			if (now == holdTicks) SculptingSessions.end(player, true);
		});
		helper.runAtTickTime(holdTicks + settleTicks, () -> {
			try {
				after.accept(player);
			} finally {
				captured.close();
			}
			helper.succeed();
		});
	}

	// ── The three modes ──

	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void aFloorPatchStrikesItsEnemyAndEveryBlockGoesBack(GameTestHelper helper) {
		ground(helper);
		Map<BlockState, Integer> before = census(helper);
		Husk husk = husk(helper, at(helper, 8.5, GROUND + 1, 8.5));
		CapturedPlayer captured = CapturedPlayer.create(helper).online();
		ServerPlayer player = captured.player;
		prepare(player, 3000.0D, false);
		Vec3 patch = at(helper, 5.5, GROUND + 1.0, 8.5);
		aim(player, at(helper, 2.5, GROUND + 1, 8.5), patch);
		helper.assertTrue(SculptingDispatcher.currentClaim(player, Verb.PROJECT).isPresent(), "Living Land did not claim stone");
		hold(helper, captured, patch, 70, 45, held -> {
			helper.assertTrue(husk.getHealth() < 60.0F, "the floor's teeth never landed on the husk");
			helper.assertTrue(com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(held, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_LAND_STRIKE), "a landed strike did not grant The Ground Rose");
			helper.assertTrue(blood(held) < 3000.0D, "the patch was not paid for");
		}, 60, held -> {
			helper.assertTrue(strikesOf(helper, held).isEmpty(), "a strike outlived its channel");
			helper.assertTrue(census(helper).equals(before), "the ground gained or lost blocks");
			helper.assertTrue(!LivingLandSculpting.hasPatch(held), "the patch outlived its release");
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void aWallSendsLancesAtItsEnemy(GameTestHelper helper) {
		ground(helper);
		for (int y = GROUND + 1; y <= 9; y++) {
			for (int z = 2; z <= 14; z++) {
				for (int x = 11; x <= 13; x++) helper.setBlock(new BlockPos(x, y, z), Blocks.COBBLESTONE);
			}
		}
		Map<BlockState, Integer> before = census(helper);
		Husk husk = husk(helper, at(helper, 9.5, GROUND + 1, 10.5));
		CapturedPlayer captured = CapturedPlayer.create(helper).online();
		ServerPlayer player = captured.player;
		prepare(player, 3000.0D, false);
		Vec3 patch = at(helper, 11.0, GROUND + 2.5, 8.5);
		aim(player, at(helper, 5.5, GROUND + 1, 8.5), patch);
		hold(helper, captured, patch, 70, 45, held -> {
			helper.assertTrue(husk.getHealth() < 60.0F, "the wall's lance never landed");
		}, 60, held -> {
			helper.assertTrue(census(helper).equals(before), "the wall gained or lost blocks");
			helper.assertTrue(strikesOf(helper, held).isEmpty(), "a lance never came home");
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void aCeilingComesDownOnItsEnemy(GameTestHelper helper) {
		ground(helper);
		for (int x = 0; x <= SIZE; x++) {
			for (int z = 0; z <= SIZE; z++) {
				for (int y = 9; y <= 11; y++) helper.setBlock(new BlockPos(x, y, z), Blocks.DEEPSLATE);
			}
		}
		Map<BlockState, Integer> before = census(helper);
		Husk husk = husk(helper, at(helper, 7.5, GROUND + 1, 8.5));
		CapturedPlayer captured = CapturedPlayer.create(helper).online();
		ServerPlayer player = captured.player;
		prepare(player, 3000.0D, false);
		Vec3 patch = at(helper, 6.5, 9.0, 8.5);
		aim(player, at(helper, 3.5, GROUND + 1, 8.5), patch);
		hold(helper, captured, patch, 70, 45, held -> {
			helper.assertTrue(husk.getHealth() < 60.0F, "the ceiling never came down on the husk");
		}, 60, held -> {
			helper.assertTrue(census(helper).equals(before), "the ceiling gained or lost blocks");
		});
	}

	// ── With nothing near, the patch rises ──

	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void withNothingNearThePatchRisesAndSettlesOnRelease(GameTestHelper helper) {
		ground(helper);
		Map<BlockState, Integer> before = census(helper);
		CapturedPlayer captured = CapturedPlayer.create(helper).online();
		ServerPlayer player = captured.player;
		prepare(player, 3000.0D, false);
		Vec3 patch = at(helper, 6.5, GROUND + 1.0, 8.5);
		BlockPos step = helper.absolutePos(new BlockPos(6, GROUND + 1, 8));
		aim(player, at(helper, 3.5, GROUND + 1, 8.5), patch);
		hold(helper, captured, patch, 80, 75, held -> {
			helper.assertTrue(LivingLandSculpting.raised(held) == LivingLandTerrainRules.MAX_RAISE,
					"the patch rose " + LivingLandSculpting.raised(held) + " blocks, not three");
			for (int i = 0; i < 3; i++) {
				helper.assertTrue(helper.getLevel().getBlockState(step.above(i)).is(Blocks.STONE), "no raised stone at " + i);
			}
			helper.assertTrue(census(helper).equals(before), "the raise made or took stone");
		}, 5, held -> {
			helper.assertTrue(helper.getLevel().getBlockState(step).isAir(), "the raise did not settle on release");
			helper.assertTrue(census(helper).equals(before), "the settled raise changed the ground");
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void aDryRaiseStandsLower(GameTestHelper helper) {
		ground(helper);
		CapturedPlayer captured = CapturedPlayer.create(helper).online();
		ServerPlayer player = captured.player;
		prepare(player, 3000.0D, true);
		Vec3 patch = at(helper, 6.5, GROUND + 1.0, 8.5);
		aim(player, at(helper, 3.5, GROUND + 1, 8.5), patch);
		hold(helper, captured, patch, 80, 75, held -> helper.assertTrue(LivingLandSculpting.raised(held) == 2,
				"a dry raise stood " + LivingLandSculpting.raised(held) + " high, not two"), 5, held -> {});
	}

	// ── No loss, no gain ──

	/** Holds until a strike is out, does {@code toTheStrike} to it, lets go, and checks the ground after {@code wait}. */
	private static void strikeThen(GameTestHelper helper, int wait, java.util.function.BiConsumer<ServerPlayer, LivingLandStrikeEntity> toTheStrike,
			java.util.function.BiConsumer<ServerPlayer, Map<BlockState, Integer>> check) {
		ground(helper);
		Map<BlockState, Integer> before = census(helper);
		husk(helper, at(helper, 9.5, GROUND + 1, 8.5));
		CapturedPlayer captured = CapturedPlayer.create(helper).online();
		ServerPlayer player = captured.player;
		prepare(player, 3000.0D, false);
		Vec3 patch = at(helper, 5.5, GROUND + 1.0, 8.5);
		aim(player, at(helper, 2.5, GROUND + 1, 8.5), patch);
		int[] tick = {0};
		boolean[] done = {false};
		helper.onEachTick(() -> {
			int now = tick[0]++;
			if (done[0]) return;
			List<LivingLandStrikeEntity> out = LivingLandSculpting.strikes(player);
			if (!out.isEmpty() && out.getFirst().record() != null) {
				done[0] = true;
				toTheStrike.accept(player, out.getFirst());
				SculptingSessions.end(player, true);
				helper.runAfterDelay(wait, () -> {
					try {
						check.accept(player, before);
					} finally {
						captured.close();
					}
					helper.succeed();
				});
				return;
			}
			if (now < 60) {
				player.lookAt(EntityAnchorArgument.Anchor.EYES, patch);
				player.setOldPosAndRot();
				project(player);
			} else {
				captured.close();
				helper.fail("no strike ever left the patch");
			}
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 300, batch = BATCH)
	public static void aStrikeThatUnloadsLeavesItsBlocksToTheLedger(GameTestHelper helper) {
		UUID[] record = new UUID[1];
		strikeThen(helper, LivingLandLedger.ORPHAN_TICKS + 45, (player, strike) -> {
			record[0] = strike.record();
			helper.assertTrue(!LivingLandLedger.get(helper.getLevel()).blocks(record[0]).isEmpty(),
					"the strike's blocks were not recorded");
			strike.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
		}, (player, before) -> {
			helper.assertTrue(!LivingLandLedger.get(helper.getLevel()).holds(record[0]), "the sweep never settled the orphan");
			helper.assertTrue(census(helper).equals(before), "blocks were lost with the unloaded strike");
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void aKilledStrikePutsItsBlocksBackAtOnce(GameTestHelper helper) {
		strikeThen(helper, 1, (player, strike) -> {
			UUID record = strike.record();
			strike.kill();
			helper.assertTrue(!LivingLandLedger.get(helper.getLevel()).holds(record), "the kill did not settle the blocks");
		}, (player, before) -> helper.assertTrue(census(helper).equals(before), "a killed strike lost or gained blocks"));
	}

	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void aStrikeWhoseCasterDiesComesHome(GameTestHelper helper) {
		strikeThen(helper, 50, (player, strike) -> player.setHealth(0.0F), (player, before) -> {
			helper.assertTrue(strikesOf(helper, player).isEmpty(), "the strike outlived its caster");
			helper.assertTrue(census(helper).equals(before), "a dead caster's strike lost or gained blocks");
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void aTakenCellSendsItsBlockToTheNearestFreeOne(GameTestHelper helper) {
		BlockPos[] origin = new BlockPos[1];
		UUID[] record = new UUID[1];
		strikeThen(helper, 50, (player, strike) -> {
			record[0] = strike.record();
			origin[0] = LivingLandLedger.get(helper.getLevel()).blocks(record[0]).getFirst().origin();
			helper.getLevel().setBlock(origin[0], Blocks.COBBLESTONE.defaultBlockState(), 3);
		}, (player, before) -> {
			helper.assertTrue(helper.getLevel().getBlockState(origin[0]).is(Blocks.COBBLESTONE), "the newcomer was moved");
			Map<BlockState, Integer> after = census(helper);
			after.merge(Blocks.COBBLESTONE.defaultBlockState(), -1, Integer::sum);
			after.values().removeIf(count -> count == 0);
			helper.assertTrue(after.equals(before), "the displaced stone was lost or doubled: " + after + " vs " + before);
			helper.assertTrue(!LivingLandLedger.get(helper.getLevel()).holds(record[0]), "the displaced block is still owed");
		});
	}

	// ── Where it refuses ──

	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void groundThePlayerMayNotAlterRefusesBeforeAnyBlood(GameTestHelper helper) {
		ground(helper);
		Map<BlockState, Integer> before = census(helper);
		husk(helper, at(helper, 8.5, GROUND + 1, 8.5));
		CapturedPlayer captured = CapturedPlayer.create(helper).online();
		ServerPlayer player = captured.player;
		prepare(player, 3000.0D, false);
		player.setGameMode(GameType.ADVENTURE);
		Vec3 patch = at(helper, 5.5, GROUND + 1.0, 8.5);
		aim(player, at(helper, 2.5, GROUND + 1, 8.5), patch);
		hold(helper, captured, patch, 40, -1, null, 2, held -> {
			helper.assertTrue(blood(held) == 3000.0D, "a refused patch was paid for: " + blood(held));
			helper.assertTrue(captured.said().contains("You may not move the ground here."), "the refusal was silent: " + captured.said());
			helper.assertTrue(strikesOf(helper, held).isEmpty(), "a refused patch struck");
			helper.assertTrue(census(helper).equals(before), "a refused patch moved ground");
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void aRiteSpaceRefusesTheGround(GameTestHelper helper) {
		ground(helper);
		ServerLevel level = helper.getLevel();
		BlockPos focus = helper.absolutePos(new BlockPos(5, GROUND, 8));
		RiteSpace space = new RiteSpace(focus, level.dimension(), UUID.randomUUID(), CardinalRitePurpose.WORLD,
				Hemomancy.rloc("test_floor"), 4, Hemomancy.rloc("test_rite"), level.getGameTime());
		RiteSpaceSavedData spaces = RiteSpaceSavedData.get(level.getServer().overworld());
		spaces.consecrate(space);
		CapturedPlayer captured = CapturedPlayer.create(helper).online();
		ServerPlayer player = captured.player;
		prepare(player, 3000.0D, false);
		Vec3 patch = at(helper, 5.5, GROUND + 1.0, 8.5);
		aim(player, at(helper, 2.5, GROUND + 1, 8.5), patch);
		hold(helper, captured, patch, 30, -1, null, 2, held -> {
			spaces.release(level.dimension(), focus);
			helper.assertTrue(blood(held) == 3000.0D, "a rite space's ground was paid for");
			helper.assertTrue(captured.said().contains("rite space"), "the rite space refusal was silent: " + captured.said());
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void aSaintsWardRefusesTheGroundWithItsStrike(GameTestHelper helper) {
		ground(helper);
		Map<BlockState, Integer> before = census(helper);
		husk(helper, at(helper, 8.5, GROUND + 1, 8.5));
		BlockPos warded = helper.absolutePos(new BlockPos(5, GROUND, 8));
		Runnable unconsecrate = MausoleumWardGameTests.consecrate(helper, new BoundingBox(warded).inflatedBy(1));
		CapturedPlayer captured = CapturedPlayer.create(helper).online();
		ServerPlayer player = captured.player;
		prepare(player, 3000.0D, false);
		Vec3 patch = at(helper, 5.5, GROUND + 1.0, 8.5);
		aim(player, at(helper, 2.5, GROUND + 1, 8.5), patch);
		hold(helper, captured, patch, 30, -1, null, 2, held -> {
			unconsecrate.run();
			helper.assertTrue(blood(held) == 3000.0D, "warded ground was paid for: " + blood(held));
			helper.assertTrue(captured.said().contains("The ward holds this ground still."),
					"the ward refusal was silent: " + captured.said());
			helper.assertTrue(captured.sent(WardStrikePacket.class).stream()
					.anyMatch(strike -> strike.kind() == WardStrikePacket.Kind.BREAK && strike.pos().equals(warded)),
					"the ward did not strike the patch it refused");
			helper.assertTrue(strikesOf(helper, held).isEmpty(), "a warded patch struck");
			helper.assertTrue(census(helper).equals(before), "warded ground moved");
		});
	}

	/** A door, a bed or an extended piston is two cells; lifting one half would break the other, so neither wakes. */
	@GameTest(template = EMPTY, timeoutTicks = 20, batch = BATCH)
	public static void halvesOfATwoCellBlockAreNotGround(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos lower = new BlockPos(3, 1, 3);
		helper.setBlock(lower.below(), Blocks.STONE);
		helper.setBlock(lower, Blocks.OAK_DOOR.defaultBlockState());
		helper.setBlock(lower.above(), Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
		for (BlockPos half : List.of(lower, lower.above())) {
			BlockPos absolute = helper.absolutePos(half);
			BlockState door = level.getBlockState(absolute);
			helper.assertTrue(door.is(Blocks.OAK_DOOR), "the door fixture did not stand at " + half);
			helper.assertTrue(!LivingLandTerrain.wakeable(level, absolute, door),
					"a door's " + door.getValue(DoorBlock.HALF) + " half would wake");
		}
		BlockPos head = helper.absolutePos(new BlockPos(6, 1, 3));
		BlockState pistonHead = Blocks.PISTON_HEAD.defaultBlockState();
		helper.assertTrue(!LivingLandTerrain.wakeable(level, head, pistonHead), "a piston head would wake");
		BlockState extended = Blocks.PISTON.defaultBlockState().setValue(PistonBaseBlock.EXTENDED, true);
		helper.assertTrue(!LivingLandTerrain.wakeable(level, head, extended), "an extended piston would wake");
		BlockPos stone = helper.absolutePos(lower.below());
		helper.assertTrue(LivingLandTerrain.wakeable(level, stone, level.getBlockState(stone)), "plain stone stopped waking");
		helper.succeed();
	}

	// ── The config switch ──

	/** Its own batch: the switch is server-wide, so no other Living Land test may run beside it. */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = "living_land_config_switch")
	public static void withLiftingOffTheStrikeIsConjuredAndTheWorldStays(GameTestHelper helper) {
		ground(helper);
		Map<BlockState, Integer> before = census(helper);
		Husk husk = husk(helper, at(helper, 8.5, GROUND + 1, 8.5));
		HemoServerConfig.LIVING_LAND_LIFTS_BLOCKS.set(false);
		CapturedPlayer captured = CapturedPlayer.create(helper).online();
		ServerPlayer player = captured.player;
		prepare(player, 3000.0D, false);
		Vec3 patch = at(helper, 5.5, GROUND + 1.0, 8.5);
		aim(player, at(helper, 2.5, GROUND + 1, 8.5), patch);
		boolean[] conjured = {false};
		helper.onEachTick(() -> {
			for (LivingLandStrikeEntity strike : strikesOf(helper, player)) {
				if (strike.isConjured()) conjured[0] = true;
			}
			if (!census(helper).equals(before)) {
				HemoServerConfig.LIVING_LAND_LIFTS_BLOCKS.set(true);
				helper.fail("a conjured strike changed the world");
			}
		});
		hold(helper, captured, patch, 60, -1, null, 40, held -> {
			HemoServerConfig.LIVING_LAND_LIFTS_BLOCKS.set(true);
			helper.assertTrue(conjured[0], "no conjured strike left the patch");
			helper.assertTrue(husk.getHealth() < 60.0F, "the conjured strike did not land");
			helper.assertTrue(LivingLandSculpting.raised(held) == 0, "a conjured patch rose");
		});
	}
}
