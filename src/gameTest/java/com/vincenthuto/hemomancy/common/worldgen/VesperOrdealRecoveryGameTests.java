package com.vincenthuto.hemomancy.common.worldgen;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheCrownedRefusalEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheEveningStarEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class VesperOrdealRecoveryGameTests {
	private VesperOrdealRecoveryGameTests() { }

	@GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 80,
			batch = "vesperOrdealRecovery")
	public static void loadedDuplicateVespersLeaveOneOwnedBoss(GameTestHelper helper) {
		var level = helper.getLevel();
		UUID ownerId = UUID.randomUUID();
		UUID bloomId = UUID.randomUUID();
		BlockPos center = helper.absolutePos(new BlockPos(8, 3, 8));
		long bloomOrigin = center.below().asLong();
		ServerPlayer owner = new ServerPlayer(level.getServer(), level,
				new GameProfile(ownerId, "vesper-recovery"), ClientInformation.createDefault());
		owner.getPersistentData().putLong("hemomancy:vesper_ordeal_bloom", bloomOrigin);
		owner.getPersistentData().putUUID("hemomancy:vesper_ordeal_bloom_id", bloomId);
		try {
			for (int i = 0; i < 2; i++) {
				VesperTheCrownedRefusalEntity boss = EntityInit.vesper_crowned_refusal.get().create(level);
				boss.setOrdeal(ownerId, bloomOrigin, bloomId);
				boss.moveTo(center.getX() + i, center.getY(), center.getZ());
				level.addFreshEntity(boss);
			}
			helper.assertTrue(VesperOrdealManager.findOwnedVesper(level, owner, center) != null,
					"Recovery must retain one matching Crowned Refusal");
			helper.assertTrue(level.getEntitiesOfClass(VesperTheCrownedRefusalEntity.class,
					new AABB(center).inflate(5)).size() == 1,
					"Recovery must remove the duplicate Crowned Refusal loaded after login");
			helper.succeed();
		} finally {
			owner.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 80,
			batch = "vesperOrdealRecovery")
	public static void phaseTwoWinsOverLateLoadedFirstPhase(GameTestHelper helper) {
		var level = helper.getLevel();
		UUID ownerId = UUID.randomUUID();
		UUID bloomId = UUID.randomUUID();
		BlockPos center = helper.absolutePos(new BlockPos(8, 3, 8));
		long bloomOrigin = center.below().asLong();
		ServerPlayer owner = new ServerPlayer(level.getServer(), level,
				new GameProfile(ownerId, "vesper-phase-recovery"), ClientInformation.createDefault());
		owner.getPersistentData().putLong("hemomancy:vesper_ordeal_bloom", bloomOrigin);
		owner.getPersistentData().putUUID("hemomancy:vesper_ordeal_bloom_id", bloomId);
		try {
			VesperTheCrownedRefusalEntity first = EntityInit.vesper_crowned_refusal.get().create(level);
			first.setOrdeal(ownerId, bloomOrigin, bloomId);
			first.moveTo(center.getX(), center.getY(), center.getZ());
			level.addFreshEntity(first);
			VesperTheEveningStarEntity second = EntityInit.vesper_evening_star.get().create(level);
			second.setOrdeal(ownerId, bloomOrigin, bloomId);
			second.moveTo(center.getX() + 1, center.getY(), center.getZ());
			level.addFreshEntity(second);
			helper.assertTrue(VesperOrdealManager.findOwnedVesper(level, owner, center) == second
					&& !first.isAlive(), "Late first-phase load must not reset an active Evening Star");
			helper.succeed();
		} finally {
			owner.discard();
		}
	}
}
