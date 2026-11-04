package com.vincenthuto.hemomancy.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Any test player in the level is the "nearest player" for every mob in it, so one far off made vanilla discard fixture
 * mobs in other plots at random: they read as removed, with no ticks and no damage (audit row A-35).
 */
@GameTestHolder("hardening_validation")
@PrefixGameTestTemplate(false)
public final class FixtureMobDespawnGuardGameTests {
	private FixtureMobDespawnGuardGameTests() {
	}

	@GameTest(template = "empty", batch = "fixture_despawn_guard")
	public static void aFarTestPlayerDoesNotDespawnAFixtureMob(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
		var observer = new net.neoforged.neoforge.common.util.FakePlayer(level,
				new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "FarObserver"));
		observer.moveTo(pos.getX() + 192, pos.getY(), pos.getZ());
		level.addNewPlayer(observer);
		Zombie mob = EntityType.ZOMBIE.create(level);
		mob.moveTo(pos.getCenter());
		level.addFreshEntity(mob);
		try {
			mob.checkDespawn();
			helper.assertTrue(!mob.isRemoved(), "a fixture mob was discarded for a test player 192 blocks away");
			FixtureMobDespawnGuard.exercisingDespawn(mob::checkDespawn);
			helper.assertTrue(mob.isRemoved(), "a test that exercises despawning must still see the ordinary rule");
			helper.succeed();
		} finally {
			observer.discard();
			mob.discard();
		}
	}
}
