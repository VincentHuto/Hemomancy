package com.vincenthuto.hemomancy.common.encounter;

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

	@GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 80,
			batch = "vesperOrdealRecovery")
	public static void reloadMidBurrowSurfacesIntoRecovery(GameTestHelper helper) {
		var level = helper.getLevel();
		BlockPos center = helper.absolutePos(new BlockPos(8, 3, 8));
		VesperTheCrownedRefusalEntity boss = EntityInit.vesper_crowned_refusal.get().create(level);
		boss.moveTo(center.getX(), center.getY(), center.getZ());
		level.addFreshEntity(boss);
		call(boss, "startBurrow", new Class<?>[] { boolean.class }, false);
		for (int i = 0; i < 40; i++) boss.tick();
		helper.assertTrue(boss.isBurrowing(), "the burrow must be underway before the save");
		net.minecraft.nbt.CompoundTag saved = boss.saveWithoutId(new net.minecraft.nbt.CompoundTag());
		boss.discard();
		VesperTheCrownedRefusalEntity loaded = EntityInit.vesper_crowned_refusal.get().create(level);
		loaded.load(saved);
		helper.assertTrue(loaded.getBurrowStage() == com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperBurrowRules.Stage.RECOVERY,
				"a reload never resumes an unseen hunt; it surfaces into recovery");
		helper.assertTrue(!loaded.isInvisible() && !loaded.isSubmerged() && !loaded.isNoAi(),
				"the reloaded mount is visible, hittable and never left without AI");
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 80,
			batch = "vesperOrdealRecovery")
	public static void reloadMidWoundKeepsProgress(GameTestHelper helper) {
		var level = helper.getLevel();
		BlockPos center = helper.absolutePos(new BlockPos(8, 3, 8));
		ServerPlayer drainer = new ServerPlayer(level.getServer(), level,
				new GameProfile(UUID.randomUUID(), "vesper-drainer"), ClientInformation.createDefault());
		try {
			VesperTheCrownedRefusalEntity boss = EntityInit.vesper_crowned_refusal.get().create(level);
			boss.moveTo(center.getX(), center.getY(), center.getZ());
			level.addFreshEntity(boss);
			call(boss, "exposeAnchor", new Class<?>[] { int.class }, 1);
			boss.drainWound(drainer, 30.0F);
			helper.assertTrue(Math.abs(boss.getWoundDrained() - 30.0F) < 0.01F, "the drain must register");
			net.minecraft.nbt.CompoundTag saved = boss.saveWithoutId(new net.minecraft.nbt.CompoundTag());
			boss.discard();
			VesperTheCrownedRefusalEntity loaded = EntityInit.vesper_crowned_refusal.get().create(level);
			loaded.load(saved);
			helper.assertTrue(loaded.getActiveAnchor() == 1, "the neck wound stays open across a reload");
			helper.assertTrue(Math.abs(loaded.getWoundDrained() - 30.0F) < 0.01F, "drain progress survives a reload");
			helper.assertTrue(!loaded.isNoAi(), "an open wound never persists NoAI");
			helper.succeed();
		} finally {
			drainer.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 80,
			batch = "vesperOrdealRecovery")
	public static void encounterCleanupUnspoolsPuppetsInsteadOfPoppingThem(GameTestHelper helper) {
		var level = helper.getLevel();
		BlockPos center = helper.absolutePos(new BlockPos(8, 3, 8));
		VesperTheCrownedRefusalEntity boss = EntityInit.vesper_crowned_refusal.get().create(level);
		boss.moveTo(center.getX(), center.getY(), center.getZ());
		level.addFreshEntity(boss);
		net.minecraft.world.entity.Mob puppet = EntityInit.gorebound_hulk.get().create(level);
		puppet.moveTo(center.getX() + 3, center.getY(), center.getZ());
		puppet.addTag("HemomancyVesperPuppet");
		level.addFreshEntity(puppet);
		Class<?> actions;
		try {
			actions = Class.forName("com.vincenthuto.hemomancy.common.entity.boss.endgame.EndgameBossActions");
		} catch (ClassNotFoundException missing) {
			throw new IllegalStateException(missing);
		}
		callStatic(actions, "clearVesperPuppets", new Class<?>[] { net.minecraft.world.entity.Mob.class }, boss);
		helper.assertTrue(com.vincenthuto.hemomancy.common.summon.PuppetStrings.isUnspooling(puppet),
				"phase-end cleanup hauls puppets away on their strings");
		helper.succeed();
	}

	private static void call(Object target, String name, Class<?>[] types, Object... args) {
		try {
			var method = target.getClass().getDeclaredMethod(name, types);
			method.setAccessible(true);
			method.invoke(target, args);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException(name, exception);
		}
	}

	private static void callStatic(Class<?> owner, String name, Class<?>[] types, Object... args) {
		try {
			var method = owner.getDeclaredMethod(name, types);
			method.setAccessible(true);
			method.invoke(null, args);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException(name, exception);
		}
	}
}
