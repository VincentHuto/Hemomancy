package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.block.harbinger.decoration.BloodGutterBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.SaintSarcophagusBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintArena;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintChamberLayout;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintChannelRouter;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintCombatController;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEndingRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathMeterRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathOpenings;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathPhaseRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathWellRules;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/**
 * Saint Hemorath on the combat kit with real entities (design section 16): the drain, the leak, the casts that feed him
 * and his Tithe, the first full meter that empties him into phase 2, both roads to the one ending, the held channels
 * into his openings and his wells, the iron, the tide, the Last Thirst, the arena's restore, the deeds and the
 * challenger scaling. Most cases run on the fallback arena round him (no tomb); the chamber cases bind a tomb so the
 * arena resolves the chamber and its wells, gutters and pit. The Saint's server step is driven by hand.
 */
@GameTestHolder("hemorath_fight")
@PrefixGameTestTemplate(false)
public final class HemorathFightGameTests {
	private static final String TEMPLATE = "empty";
	private static final BlockPos SAINT_AT = new BlockPos(4, 2, 4);
	/** High enough that the chamber's floor course (local y 0) is the stage's floor and its wells stand above it. */
	private static final BlockPos TOMB = new BlockPos(9, 6, 9);

	private HemorathFightGameTests() {
	}

	// ── fixtures ─────────────────────────────────────────────────────

	private static HemorathEntity saint(GameTestHelper h) {
		clear(h);
		HemorathEntity saint = EntityInit.hemorath.get().create(h.getLevel());
		saint.moveTo(h.absoluteVec(Vec3.atBottomCenterOf(SAINT_AT)));
		h.getLevel().addFreshEntity(saint);
		return saint;
	}

	private static void clear(GameTestHelper h) {
		for (BlockPos pos : BlockPos.betweenClosed(h.absolutePos(new BlockPos(0, 2, 0)), h.absolutePos(new BlockPos(12, 7, 12)))) {
			h.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
		}
	}

	private static ServerPlayer player(GameTestHelper h, HemorathEntity saint, double dx, double blood) {
		ServerPlayer player = DuctilisGameTests.player(h);
		clearSpawnInvulnerability(player);
		player.moveTo(saint.position().add(dx, 0.0D, 0.0D));
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(blood > 0.0D);
		volume.setMaxBloodVolume(10_000.0D);
		volume.setBloodVolume(blood);
		return player;
	}

	/** A fresh player ignores all damage for its first 60 ticks, and these mock players are never ticked. */
	private static void clearSpawnInvulnerability(ServerPlayer player) {
		try {
			var field = ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");
			field.setAccessible(true);
			field.setInt(player, 0);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

	private static void ticks(HemorathEntity saint, int n) {
		for (int i = 0; i < n && !saint.isRemoved(); i++) saint.aiStep();
	}

	/** A saint whose arena resolves the real chamber from a bound tomb. */
	private static HemorathEntity chambered(GameTestHelper h) {
		HemorathEntity saint = saint(h);
		// the chamber's floor course (local y 0) where the fixture cleared the stage, and him standing on it: below the
		// chamber box the leash would hold him up while his fall built
		for (BlockPos pos : BlockPos.betweenClosed(h.absolutePos(new BlockPos(0, 2, 0)), h.absolutePos(new BlockPos(12, 2, 12)))) {
			h.getLevel().setBlock(pos, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
		}
		saint.moveTo(h.absoluteVec(Vec3.atBottomCenterOf(SAINT_AT.above())));
		BlockPos tomb = h.absolutePos(TOMB);
		saint.bindTomb(tomb, BlockInit.saint_sarcophagus.get().defaultBlockState().setValue(SaintSarcophagusBlock.FACING,
				Direction.WEST));
		return saint;
	}

	private static BlockPos local(SaintArena arena, int x, int y, int z) {
		return arena.toWorld(x, y, z);
	}

	private static void sweep(GameTestHelper h) {
		h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(h.absolutePos(BlockPos.ZERO)).inflate(40.0D)).forEach(Entity::discard);
	}

	// ── the drain and the leak ───────────────────────────────────────

	@GameTest(template = TEMPLATE, timeoutTicks = 60)
	public static void presenceDrainsActiveBloodIntoTheMeter(GameTestHelper h) {
		HemorathEntity saint = saint(h);
		ServerPlayer player = player(h, saint, 2.0D, 5000.0D);
		ticks(saint, 11);
		h.assertTrue(HemoCapabilityAccess.getBloodVolume(player).getBloodVolume() < 5000.0D, "the drain took blood");
		h.assertTrue(saint.meter().value() > 0.0F, "what it took went into the meter");
		saint.discard();
		player.discard();
		h.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 60)
	public static void presenceHurtsAnEmptyPlayer(GameTestHelper h) {
		HemorathEntity saint = saint(h);
		ServerPlayer player = player(h, saint, 2.0D, 0.0D);
		float before = player.getHealth();
		ticks(saint, 25);
		h.assertTrue(player.getHealth() < before, "an empty player bleeds health instead");
		saint.discard();
		player.discard();
		h.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 60)
	public static void aPillarBlocksThePresence(GameTestHelper h) {
		HemorathEntity saint = saint(h);
		ServerPlayer player = player(h, saint, 3.0D, 5000.0D);
		BlockPos wall = BlockPos.containing(saint.position().add(1.5D, 0.0D, 0.0D));
		for (int dz = -2; dz <= 2; dz++) for (int dy = 0; dy <= 4; dy++) {
			h.getLevel().setBlock(wall.offset(0, dy, dz), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
		}
		ticks(saint, 11);
		h.assertTrue(HemoCapabilityAccess.getBloodVolume(player).getBloodVolume() == 5000.0D, "the pillar broke his sight");
		saint.discard();
		player.discard();
		clear(h);
		h.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 60)
	public static void theMeterLeaksFasterWhenFuller(GameTestHelper h) {
		HemorathEntity saint = saint(h);
		ticks(saint, 1);
		float max = saint.meter().max();
		saint.meter().set(0.2F * max);
		ticks(saint, 1);
		float low = 0.2F * max - saint.meter().value();
		saint.meter().set(0.9F * max);
		ticks(saint, 1);
		float high = 0.9F * max - saint.meter().value();
		h.assertTrue(low > 0.0F && high > low * 4.0F, "low " + low + ", high " + high);
		saint.discard();
		h.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 60)
	public static void castingNearHimFeedsTheMeterAndTheTithe(GameTestHelper h) {
		HemorathEntity saint = saint(h);
		ServerPlayer player = player(h, saint, 8.0D, 5000.0D);
		ticks(saint, 1);
		float before = saint.meter().value();
		HemorathEntity.onPlayerBloodSpend(player, 100.0D);
		h.assertTrue(saint.meter().value() - before >= 99.0F, "a cast near him feeds the meter");
		h.assertTrue(saint.tithe().debt(player.getUUID()) == 100.0F, "and adds to the caster's Tithe");
		saint.discard();
		player.discard();
		h.succeed();
	}

	// ── the Tithe ────────────────────────────────────────────────────

	@GameTest(template = TEMPLATE, timeoutTicks = 120)
	public static void unpaidTitheDealsItsDebtAndPaidTitheDealsNone(GameTestHelper h) {
		HemorathEntity saint = saint(h);
		ServerPlayer player = player(h, saint, 6.0D, 5000.0D);
		ticks(saint, 1);
		saint.tithe().accrue(player.getUUID(), 400.0F, false);
		saint.controller().start(HemorathMove.TITHE_RECKONING, SaintCombatController.Cut.IMMEDIATE);
		float before = player.getHealth();
		ticks(saint, 45);
		h.assertTrue(player.getHealth() < before, "the unpaid Tithe struck");
		h.assertTrue(saint.tithe().debt(player.getUUID()) == 0.0F, "and the debt was collected");
		player.setHealth(player.getMaxHealth());
		saint.tithe().accrue(player.getUUID(), 400.0F, false);
		saint.controller().start(HemorathMove.TITHE_RECKONING, SaintCombatController.Cut.IMMEDIATE);
		ticks(saint, 5);
		saint.tithe().pay(player.getUUID(), 400.0F);
		saint.tithe().clear(player.getUUID());
		before = player.getHealth();
		ticks(saint, 40);
		h.assertTrue(player.getHealth() >= before, "a paid Tithe deals nothing");
		saint.discard();
		player.discard();
		h.succeed();
	}

	// ── phases and the two roads ─────────────────────────────────────

	@GameTest(template = TEMPLATE, timeoutTicks = 200)
	public static void firstFullMeterEmptiesIntoPhaseTwoWithoutEnding(GameTestHelper h) {
		HemorathEntity saint = saint(h);
		ServerPlayer player = player(h, saint, 14.0D, 5000.0D);
		ticks(saint, 1);
		float healthBefore = saint.getHealth();
		saint.feed(saint.meter().max(), player, false);
		ticks(saint, 3);
		h.assertTrue(saint.phases().phase() == 2, "the first full meter forced phase 2");
		h.assertTrue(!saint.ending() && !saint.hasTrialEnded(), "and did not end the trial");
		h.assertTrue(Math.abs(saint.getHealth() - (healthBefore - saint.getMaxHealth() * HemorathPhaseRules.EMPTIES_TEAR)) < 1.0F,
				"the burst tore 15% of his health off");
		h.assertTrue(saint.controller().move() == HemorathMove.EXSANGUINATING_TIDE, "he empties in a forced tide");
		ticks(saint, 120);
		h.assertTrue(!saint.emptying(), "the tide's flood released him");
		h.assertTrue(saint.meter().fraction() < HemorathMeterRules.AFTER_EMPTIES + 0.05F, "the meter fell to about 30%");
		saint.discard();
		player.discard();
		h.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 260)
	public static void fullMeterInPhaseTwoEndsTheTrialOnceWithTheReward(GameTestHelper h) {
		HemorathEntity saint = saint(h);
		ServerPlayer player = player(h, saint, 14.0D, 5000.0D);
		ticks(saint, 1);
		saint.setHealth(saint.getMaxHealth() * 0.45F);
		ticks(saint, 2);
		h.assertTrue(saint.phases().phase() == 2, "half health brought phase 2");
		saint.feed(saint.meter().max(), player, false);
		ticks(saint, 2);
		h.assertTrue(saint.ending(), "a full meter in phase 2 starts the ending");
		ticks(saint, HemorathEndingRules.LENGTH + 5);
		h.assertTrue(saint.isRemoved() && saint.hasTrialEnded(), "the ending ran endTrial and the body is gone");
		boolean rewarded = player.getInventory().items.stream().anyMatch(s -> s.is(ItemInit.hallowed_residuum_hemorath.get()));
		h.assertTrue(rewarded, "the last feeder carries his residuum");
		player.discard();
		sweep(h);
		h.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 260)
	public static void wearingHimDownEndsTheTrialOnce(GameTestHelper h) {
		HemorathEntity saint = saint(h);
		ServerPlayer player = player(h, saint, 14.0D, 5000.0D);
		ticks(saint, 1);
		saint.hurt(h.getLevel().damageSources().playerAttack(player), 100_000.0F);
		h.assertTrue(saint.getHealth() >= 1.0F && saint.isAlive(), "his health never reaches 0");
		h.assertTrue(saint.ending(), "his last point starts the ending");
		ticks(saint, HemorathEndingRules.LENGTH + 5);
		h.assertTrue(saint.isRemoved() && saint.hasTrialEnded(), "one endTrial, then gone");
		player.discard();
		sweep(h);
		h.succeed();
	}

	// ── held channels ────────────────────────────────────────────────

	@GameTest(template = TEMPLATE, timeoutTicks = 60)
	public static void gorgedOpeningFeedsHeldProjectionAndShutRibsRefuse(GameTestHelper h) {
		HemorathEntity saint = saint(h);
		ServerPlayer player = player(h, saint, 3.0D, 5000.0D);
		ticks(saint, 1);
		player.lookAt(EntityAnchorArgument.Anchor.EYES, saint.chest());
		SaintChannelRouter.Result shut = SaintChannelRouter.tryProject(h.getLevel(), player, 5.5D, 100.0D, false);
		h.assertTrue(shut.handled() && shut.blood() == 0.0D, "outside an opening his ribs are shut");
		saint.openings().open(HemorathOpenings.GORGED, 0);
		float before = saint.meter().value();
		SaintChannelRouter.Result fed = SaintChannelRouter.tryProject(h.getLevel(), player, 5.5D, 100.0D, false);
		h.assertTrue(fed.handled() && fed.blood() == HemorathOpenings.FEED_RATE, "Gorged takes 20 a tick, never the item's 100");
		h.assertTrue(saint.meter().value() - before >= HemorathOpenings.FEED_RATE * HemorathOpenings.GORGED_FEED - 0.01F,
				"and counts it at x1.5");
		saint.discard();
		player.discard();
		h.succeed();
	}

	// ── the chamber: wells, gutters, restore ─────────────────────────

	@GameTest(template = TEMPLATE, timeoutTicks = 200, batch = "hemorath_chamber")
	public static void wellsAnswerDrawsAPairAndHealsAndProjectionPrimesAWell(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		HemorathEntity saint = chambered(h);
		SaintArena probe = SaintArena.resolve(saint);
		int[][] wells = {{15, 1}, {19, 1}, {15, 25}, {19, 25}};
		for (int[] well : wells) level.setBlock(local(probe, well[0], 1, well[1]), BlockInit.blood_basin.get().defaultBlockState(),
				Block.UPDATE_CLIENTS);
		// in the chamber before the first tick: the arena reads its challengers once a tick
		ServerPlayer player = player(h, saint, 0.0D, 5000.0D);
		ticks(saint, 1);
		h.assertTrue(saint.arena() != null && saint.arena().chamberFound(), "a bound tomb resolves the chamber");
		h.assertTrue(saint.wells().count() == 4, "the arena found his four wells");
		BlockPos firstWell = saint.chamber().wells().get(0);
		player.moveTo(Vec3.atCenterOf(firstWell).add(0.0D, 0.0D, 2.5D));
		player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(firstWell));
		SaintChannelRouter.Result primed = SaintChannelRouter.tryProject(level, player, 5.5D, 100.0D, false);
		h.assertTrue(primed.handled() && saint.wells().content(0) == HemorathWellRules.START + HemorathWellRules.PROJECT,
				"held Projection put 25 into the well");
		player.moveTo(saint.position().add(0.0D, 0.0D, 8.0D));
		saint.setHealth(300.0F);
		float meter = saint.meter().value();
		saint.controller().start(HemorathMove.WELLS_ANSWER, SaintCombatController.Cut.IMMEDIATE);
		ticks(saint, 80);
		float drawn = saint.meter().value() - meter;
		h.assertTrue(drawn > 400.0F, "the draw fed the meter from a pair: " + drawn);
		h.assertTrue(saint.getHealth() > 300.0F, "and healed him: " + saint.getHealth());
		saint.discard();
		player.discard();
		for (int[] well : wells) level.setBlock(local(probe, well[0], 1, well[1]), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
		h.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 300, batch = "hemorath_chamber")
	public static void arenaRestoresGuttersAfterRemovalAndAfterTheEnding(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		HemorathEntity saint = chambered(h);
		BlockPos ringCell = local(SaintArena.resolve(saint), 17 + 5, 0, 13);
		BlockState dry = BlockInit.blood_gutter.get().defaultBlockState();
		level.setBlock(ringCell, dry, Block.UPDATE_CLIENTS);
		ServerPlayer player = player(h, saint, 6.0D, 5000.0D);
		ticks(saint, 1);
		saint.meter().set(0.8F * saint.meter().max());
		ticks(saint, 21);
		h.assertTrue(level.getBlockState(ringCell).getValue(BloodGutterBlock.FILL) > 0, "the pit ring shows his meter");
		saint.discard();
		h.assertTrue(level.getBlockState(ringCell).equals(dry), "removal put the gutter back");
		level.setBlock(h.absolutePos(TOMB), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
		HemorathEntity second = chambered(h);
		// the fixture's clear swept the stage, ring cell included: lay it again
		level.setBlock(ringCell, dry, Block.UPDATE_CLIENTS);
		ticks(second, 1);
		second.meter().set(0.8F * second.meter().max());
		ticks(second, 21);
		second.hurt(level.damageSources().playerAttack(player), 100_000.0F);
		ticks(second, HemorathEndingRules.LENGTH + 5);
		h.assertTrue(second.isRemoved(), "the ending discarded him");
		h.assertTrue(level.getBlockState(ringCell).equals(dry), "the ending put the gutter back: " + level.getBlockState(ringCell));
		level.setBlock(ringCell, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
		player.discard();
		sweep(h);
		h.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 60)
	public static void aReloadedHemorathKeepsHisWellsPatchesAndLedger(GameTestHelper h) {
		HemorathEntity saint = saint(h);
		ServerPlayer player = player(h, saint, 8.0D, 5000.0D);
		ticks(saint, 1);
		saint.tithe().accrue(player.getUUID(), 220.0F, false);
		CompoundTag saved = new CompoundTag();
		saint.saveWithoutId(saved);
		saint.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
		HemorathEntity reloaded = EntityInit.hemorath.get().create(h.getLevel());
		reloaded.load(saved);
		h.getLevel().addFreshEntity(reloaded);
		ticks(reloaded, 1);
		h.assertTrue(reloaded.tithe().debt(player.getUUID()) == 220.0F, "the Tithe ledger survives a reload");
		reloaded.discard();
		player.discard();
		h.succeed();
	}

	// ── iron, the tide, the Last Thirst ──────────────────────────────

	@GameTest(template = TEMPLATE, timeoutTicks = 260)
	public static void ironAccruesFromTheLeakAndALodestoneHitStripsAPatch(GameTestHelper h) {
		HemorathEntity saint = saint(h);
		ticks(saint, 1);
		// just under full: a full meter would empty him (and burst his patches out) instead of leaking
		saint.meter().set(0.99F * saint.meter().max());
		ticks(saint, 200);
		h.assertTrue(saint.patchCount() >= 1, "a thousand leaked raised a patch");
		int before = saint.patchCount();
		SchoolStates.apply(null, saint, SchoolState.LODESTONE, 100, 1);
		ticks(saint, 1);
		h.assertTrue(saint.patchCount() == before - 1, "a Ferric hit tore the newest patch out");
		saint.discard();
		h.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 200)
	public static void theTideHitsTheBossFloorButNotTheAisles(GameTestHelper h) {
		HemorathEntity saint = saint(h);
		ServerPlayer onFloor = player(h, saint, 4.0D, 5000.0D);
		ServerPlayer aisle = player(h, saint, 10.0D, 5000.0D);
		ticks(saint, 1);
		saint.setHealth(saint.getMaxHealth() * 0.45F);
		saint.meter().set(0.6F * saint.meter().max());
		ticks(saint, 2);
		saint.controller().start(HemorathMove.EXSANGUINATING_TIDE, SaintCombatController.Cut.IMMEDIATE);
		float floorBefore = onFloor.getHealth(), aisleBefore = aisle.getHealth();
		ticks(saint, 45);
		h.assertTrue(onFloor.getHealth() < floorBefore, "the flood struck the boss floor");
		h.assertTrue(aisle.getHealth() == aisleBefore, "the aisle stayed dry");
		saint.discard();
		onFloor.discard();
		aisle.discard();
		h.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 200)
	public static void lastThirstRunsOnceAndStopsTheLeak(GameTestHelper h) {
		HemorathEntity saint = saint(h);
		ServerPlayer player = player(h, saint, 20.0D, 0.0D);
		ticks(saint, 1);
		saint.setHealth(saint.getMaxHealth() * 0.19F);
		ticks(saint, 60);
		h.assertTrue(saint.thirstUsed() && saint.thirsting(), "the Last Thirst began at a fifth of his health");
		saint.meter().set(0.5F * saint.meter().max());
		float value = saint.meter().value();
		ticks(saint, 10);
		h.assertTrue(saint.meter().value() >= value, "the leak stopped while he drinks");
		saint.discard();
		player.discard();
		h.succeed();
	}

	// ── deeds and challengers ────────────────────────────────────────

	@GameTest(template = TEMPLATE, timeoutTicks = 60)
	public static void theDownfallDeedRisesHimBloatedFromHisWells(GameTestHelper h) {
		HemorathEntity saint = saint(h);
		saint.setAwakening("FORCED", List.of("DOWNFALL"));
		ticks(saint, 2);
		float fraction = saint.meter().fraction();
		h.assertTrue(fraction > 0.2F && fraction <= HemorathMeterRules.DOWNFALL_START + 0.001F,
				"he rose at a quarter: " + fraction);
		saint.discard();
		h.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 60)
	public static void twoChallengersScaleTheMeterAndHealth(GameTestHelper h) {
		HemorathEntity saint = saint(h);
		ServerPlayer a = player(h, saint, 10.0D, 5000.0D);
		ServerPlayer b = player(h, saint, -10.0D, 5000.0D);
		ticks(saint, 2);
		h.assertTrue(saint.meter().max() == HemorathMeterRules.max(2), "the meter scaled: " + saint.meter().max());
		h.assertTrue(Math.abs(saint.getMaxHealth() - (float) (HemorathEntity.MAX_HEALTH * 1.5D)) < 0.5F,
				"health scaled: " + saint.getMaxHealth());
		saint.discard();
		a.discard();
		b.discard();
		h.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 300)
	public static void noArenaStillFights(GameTestHelper h) {
		HemorathEntity saint = saint(h);
		ServerPlayer player = player(h, saint, 3.0D, 5000.0D);
		boolean moved = false;
		for (int i = 0; i < 200; i++) {
			saint.aiStep();
			moved |= saint.controller().move() != null;
		}
		h.assertTrue(saint.wells().count() == 0, "an empty pad has no wells");
		h.assertTrue(moved, "he still picks and runs moves");
		saint.discard();
		player.discard();
		h.succeed();
	}
}
