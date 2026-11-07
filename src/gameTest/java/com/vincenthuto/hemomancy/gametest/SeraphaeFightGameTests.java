package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintCombatController;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.ContainmentAnchorEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeFragmentEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeGrowthRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeIntegrityRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeLeashRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeOpenings;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaePhases;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeRestraint;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeRestraintState;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/**
 * Saint Seraphae on the combat kit with real entities and mock players (design section 16): her first move is the reach
 * at the most hurt, a reach the chains stop short opens a Recoil that only then lets blows tighten the binding, growth
 * bursts at ten, her anchors wake and stand again after a reload, the ratcheted phases, the Fracture's fragments
 * resolving into the Condensing, and the containment ending running endTrial once. With no tomb her arena is the
 * fallback box and her anchors take the ring round her.
 */
@GameTestHolder("seraphae_fight_validation")
@PrefixGameTestTemplate(false)
public final class SeraphaeFightGameTests {
	private static final String TEMPLATE = "empty";
	private static final BlockPos SAINT_AT = new BlockPos(6, 2, 6);

	private SeraphaeFightGameTests() {
	}

	private static SeraphaeEntity seraphae(GameTestHelper h) {
		for (BlockPos pos : BlockPos.betweenClosed(h.absolutePos(new BlockPos(1, 1, 1)), h.absolutePos(new BlockPos(14, 6, 14)))) {
			h.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
		}
		SeraphaeEntity seraphae = EntityInit.seraphae.get().create(h.getLevel());
		seraphae.moveTo(h.absoluteVec(Vec3.atBottomCenterOf(SAINT_AT)));
		h.getLevel().addFreshEntity(seraphae);
		return seraphae;
	}

	private static ServerPlayer player(GameTestHelper h, Vec3 at) {
		ServerPlayer player = DuctilisGameTests.player(h);
		player.setGameMode(GameType.SURVIVAL);
		player.moveTo(at.x, at.y, at.z);
		try {
			var grace = ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");
			grace.setAccessible(true);
			grace.setInt(player, 0);
		} catch (ReflectiveOperationException ignored) {
			// a renamed field leaves the spawn grace; the burst test then reads no damage
		}
		return player;
	}

	private static void tick(SeraphaeEntity seraphae, int ticks) {
		for (int i = 0; i < ticks && !seraphae.isRemoved(); i++) seraphae.aiStep();
	}

	private static List<ContainmentAnchorEntity> anchors(GameTestHelper h, SeraphaeEntity seraphae) {
		return h.getLevel().getEntitiesOfClass(ContainmentAnchorEntity.class, seraphae.getBoundingBox().inflate(24.0D),
				anchor -> seraphae.getUUID().equals(anchor.getParentId()) && anchor.isAlive());
	}

	private static void clean(GameTestHelper h, Entity... entities) {
		for (Entity entity : entities) if (entity != null && !entity.isRemoved()) entity.discard();
		AABB stage = new AABB(h.absolutePos(BlockPos.ZERO)).inflate(40.0D);
		h.getLevel().getEntitiesOfClass(ItemEntity.class, stage).forEach(Entity::discard);
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 60)
	public static void herFirstMoveIsTheReachAtTheMostHurt(GameTestHelper h) {
		SeraphaeEntity seraphae = seraphae(h);
		ServerPlayer whole = player(h, seraphae.position().add(5.0D, 0.0D, 1.0D));
		ServerPlayer hurt = player(h, seraphae.position().add(-6.0D, 0.0D, -1.0D));
		hurt.setHealth(8.0F);
		try {
			tick(seraphae, 4);
			h.assertTrue(seraphae.syncedMoveId() == SeraphaeMove.LAYING_ON_OF_HANDS.id(), "her first move is the reach");
			h.assertTrue(seraphae.syncedMoveTarget() == hurt.getId(), "she reaches for the most hurt");
			h.succeed();
		} finally {
			clean(h, seraphae, whole, hurt);
		}
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 60)
	public static void aReachTheChainsStopShortOpensTheRecoilAndOnlyItCounts(GameTestHelper h) {
		SeraphaeEntity seraphae = seraphae(h);
		ServerPlayer far = player(h, seraphae.position().add(SeraphaeLeashRules.length(SeraphaeIntegrityRules.START, 0, 0) + 2.5D,
				0.0D, 0.0D));
		try {
			tick(seraphae, 3);
			float before = seraphae.meter().value();
			seraphae.hurt(far.damageSources().playerAttack(far), 4.0F);
			h.assertTrue(Math.abs(seraphae.meter().value() - before) < 0.05F, "a blow outside an opening adds nothing");
			h.assertTrue(seraphae.getHealth() == seraphae.getMaxHealth(), "her health never changes: "
					+ seraphae.getHealth() + " of " + seraphae.getMaxHealth());
			for (int i = 0; i < 60 && seraphae.syncedOpening() != SeraphaeOpenings.RECOIL_ID; i++) tick(seraphae, 1);
			h.assertTrue(seraphae.syncedOpening() == SeraphaeOpenings.RECOIL_ID, "the chains stopped her short: Recoil");
			float open = seraphae.meter().value();
			for (int i = 0; i < 6; i++) seraphae.hurt(far.damageSources().playerAttack(far), 5.0F + i * 2.0F);
			float gained = seraphae.meter().value() - open;
			h.assertTrue(Math.abs(gained - 4 * SeraphaeIntegrityRules.RESTRAINT_HIT) < 0.1F,
					"four counted hits of two, then the cap: " + gained);
			h.succeed();
		} finally {
			clean(h, seraphae, far);
		}
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 160)
	public static void theReachHoldsTheWoundedHealsThenGrows(GameTestHelper h) {
		SeraphaeEntity seraphae = seraphae(h);
		ServerPlayer hurt = player(h, seraphae.position().add(-4.5D, 0.0D, 0.0D));
		hurt.setHealth(hurt.getMaxHealth() - 2.0F);
		try {
			StringBuilder trace = new StringBuilder();
			boolean held = false;
			for (int i = 0; i < 60 && !held; i++) {
				tick(seraphae, 1);
				held = seraphae.bossHold().holding();
				if (i % 6 == 0) trace.append(String.format(" [t%d move %d %s gap %.2f]", i, seraphae.syncedMoveId(),
						seraphae.controller().stage(), seraphae.position().distanceTo(hurt.position())));
			}
			h.assertTrue(held, "the reach holds the wounded:" + trace);
			for (int i = 0; i < 12; i++) tick(seraphae, 1);
			h.assertTrue(hurt.getHealth() == hurt.getMaxHealth(), "the hold heals first: " + hurt.getHealth());
			for (int i = 0; i < 24 && seraphae.bossHold().holding(); i++) tick(seraphae, 1);
			h.assertTrue(seraphae.growth(hurt) >= 2, "then it grows the whole: " + seraphae.growth(hurt));
			h.succeed();
		} finally {
			clean(h, seraphae, hurt);
		}
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 40)
	public static void growthBurstsAtTenAndCostsIntegrity(GameTestHelper h) {
		SeraphaeEntity seraphae = seraphae(h);
		ServerPlayer player = player(h, seraphae.position().add(3.0D, 0.0D, 0.0D));
		try {
			tick(seraphae, 2);
			seraphae.addGrowth(player, 7);
			h.assertTrue(seraphae.growth(player) == 7, "growth holds below ten");
			float before = seraphae.meter().value();
			float health = player.getHealth();
			seraphae.addGrowth(player, 3);
			h.assertTrue(seraphae.growth(player) == 0, "at ten it bursts and empties");
			h.assertTrue(player.getHealth() < health, "the burst hurts");
			h.assertTrue(seraphae.meter().value() <= before + SeraphaeIntegrityRules.BURST + 0.01F, "a burst costs three");
			h.succeed();
		} finally {
			clean(h, seraphae, player);
		}
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 40)
	public static void herAnchorsWakeHoldingAndStandAgainAfterAReload(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		SeraphaeEntity seraphae = seraphae(h);
		ServerPlayer player = player(h, seraphae.position().add(4.0D, 0.0D, 0.0D));
		SeraphaeEntity reloaded = null;
		try {
			tick(seraphae, 2);
			List<ContainmentAnchorEntity> woken = anchors(h, seraphae);
			h.assertTrue(woken.size() == 4, "four binding anchors wake: " + woken.size());
			int packed = seraphae.syncedRestraints();
			for (SeraphaeRestraint restraint : SeraphaeRestraint.BINDING) {
				h.assertTrue(SeraphaeRestraintState.state(packed, restraint.ordinal()) == SeraphaeRestraintState.HELD,
						restraint + " is held");
			}
			h.assertTrue(Math.abs(seraphae.syncedLeash() - SeraphaeLeashRules.length(seraphae.meter().value(), 0, 0)) < 0.05F,
					"her chain length follows integrity");
			woken.get(0).slip();
			tick(seraphae, 1);
			h.assertTrue(SeraphaeRestraintState.slipped(seraphae.syncedRestraints()) == 1, "a slip shows in her irons");
			CompoundTag saved = new CompoundTag();
			seraphae.saveWithoutId(saved);
			seraphae.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
			reloaded = EntityInit.seraphae.get().create(level);
			reloaded.load(saved);
			level.addFreshEntity(reloaded);
			tick(reloaded, 2);
			for (ContainmentAnchorEntity old : woken) old.tick();
			final SeraphaeEntity again = reloaded;
			long standing = anchors(h, again).stream().filter(again::knows).count();
			h.assertTrue(standing == 4, "her anchors stand again after the reload: " + standing);
			h.assertTrue(SeraphaeRestraintState.slipped(reloaded.syncedRestraints()) == 1, "the slipped iron stays slipped");
			h.succeed();
		} finally {
			clean(h, seraphae, reloaded, player);
			anchors(h, seraphae).forEach(Entity::discard);
		}
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 60)
	public static void phasesRatchetAndTheChainShortens(GameTestHelper h) {
		SeraphaeEntity seraphae = seraphae(h);
		ServerPlayer player = player(h, seraphae.position().add(9.0D, 0.0D, 0.0D));
		try {
			tick(seraphae, 2);
			float early = seraphae.syncedLeash();
			seraphae.meter().set(45.0F);
			for (int i = 0; i < 80 && seraphae.phases().phase() != SeraphaePhases.RESTRAINT; i++) tick(seraphae, 1);
			h.assertTrue(seraphae.phases().phase() == SeraphaePhases.RESTRAINT, "integrity 40 opens the Restraint");
			h.assertTrue(seraphae.syncedLeash() < early, "a tighter binding shortens her chain");
			seraphae.meter().set(30.0F);
			tick(seraphae, 4);
			h.assertTrue(seraphae.phases().phase() == SeraphaePhases.RESTRAINT, "a phase once reached is kept");
			h.succeed();
		} finally {
			clean(h, seraphae, player);
		}
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 100)
	public static void theFracturesFragmentsResolveIntoTheCondensing(GameTestHelper h) {
		SeraphaeEntity seraphae = seraphae(h);
		ServerPlayer player = player(h, seraphae.position().add(8.0D, 0.0D, 0.0D));
		try {
			tick(seraphae, 2);
			seraphae.meter().set(50.0F);
			tick(seraphae, 2);
			seraphae.controller().start(SeraphaeMove.FRACTURE, SaintCombatController.Cut.IMMEDIATE);
			List<SeraphaeFragmentEntity> fragments = List.of();
			for (int i = 0; i < 60 && fragments.isEmpty(); i++) {
				tick(seraphae, 1);
				fragments = h.getLevel().getEntitiesOfClass(SeraphaeFragmentEntity.class, seraphae.getBoundingBox().inflate(8.0D),
						fragment -> seraphae.getUUID().equals(fragment.getParentId()) && fragment.isAlive());
			}
			h.assertTrue(fragments.size() >= 3, "a phase II Fracture sheds three fragments: " + fragments.size());
			float before = seraphae.meter().value();
			for (SeraphaeFragmentEntity fragment : fragments) {
				fragment.resolve(SeraphaeIntegrityRules.FragmentEnd.STRUCK, player.position());
			}
			h.assertTrue(seraphae.meter().value() >= before + fragments.size() * SeraphaeIntegrityRules.FRAGMENT_STRUCK - 0.1F,
					"each fragment struck apart tightens the binding");
			for (int i = 0; i < 40 && seraphae.syncedOpening() != SeraphaeOpenings.CONDENSING_ID; i++) tick(seraphae, 1);
			h.assertTrue(seraphae.syncedOpening() == SeraphaeOpenings.CONDENSING_ID, "the last fragment resolved: Condensing");
			h.succeed();
		} finally {
			clean(h, seraphae, player);
		}
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 200)
	public static void containmentRunsTheEndingOnceAndLetsHerAnchorsGo(GameTestHelper h) {
		SeraphaeEntity seraphae = seraphae(h);
		ServerPlayer player = player(h, seraphae.position().add(4.0D, 0.0D, 0.0D));
		try {
			tick(seraphae, 2);
			seraphae.addGrowth(player, 4);
			seraphae.meter().set(99.5F);
			seraphae.meter().add(1.0F, player);
			tick(seraphae, 1);
			h.assertTrue(seraphae.ending(), "full integrity begins her containment");
			h.assertTrue(seraphae.syncedMoveId() == 0, "the ending cancels the move at once");
			tick(seraphae, 30);
			h.assertTrue(seraphae.growth(player) == 0, "the light leaves you at tick twenty");
			tick(seraphae, 200);
			h.assertTrue(seraphae.isRemoved() && seraphae.hasTrialEnded(), "endTrial ran and she was discarded");
			h.assertTrue(anchors(h, seraphae).isEmpty(), "her anchors let go with her");
			h.succeed();
		} finally {
			clean(h, seraphae, player);
		}
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 40)
	public static void theStageFlagsTravelForTheHudAndOtherPlayers(GameTestHelper h) {
		SeraphaeEntity seraphae = seraphae(h);
		ServerPlayer player = player(h, seraphae.position().add(3.0D, 0.0D, 0.0D));
		try {
			tick(seraphae, 2);
			seraphae.addGrowth(player, 7);
			tick(seraphae, 6);
			int flags = seraphae.syncedChallengerFlags(player.getId());
			h.assertTrue((flags & SeraphaeGrowthRules.FLAG_KNOTTING) != 0, "knotting shows on the roster: " + flags);
			h.succeed();
		} finally {
			clean(h, seraphae, player);
		}
	}
}
