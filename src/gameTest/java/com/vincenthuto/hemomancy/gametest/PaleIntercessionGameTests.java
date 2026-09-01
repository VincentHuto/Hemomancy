package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.summon.PaleIntercessionEntity;
import com.vincenthuto.hemomancy.common.entity.summon.PaleIntercessionRules;
import com.vincenthuto.hemomancy.common.entity.summon.PaleIntercessionSummonService;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.StillArtInit;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PaleIntercessionGameTests {
	private static final String EMPTY = "bastion/mobs/empty";
	private PaleIntercessionGameTests() { }

	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 40)
	public static void firstSummonAndSameEntityRecall(GameTestHelper helper) {
		ServerPlayer player = eligiblePlayer(helper);
		try {
			helper.assertTrue(PaleIntercessionSummonService.summonOrRecall(player), "first cast must summon");
			PaleIntercessionEntity first = manifestations(helper, player).getFirst();
			first.setPos(first.getX() + 6, first.getY(), first.getZ());
			helper.assertTrue(PaleIntercessionSummonService.summonOrRecall(player), "second cast must recall");
			List<PaleIntercessionEntity> after = manifestations(helper, player);
			helper.assertTrue(after.size() == 1 && after.getFirst().getUUID().equals(first.getUUID()),
					"recall must refresh the same body without duplicating it");
			helper.assertTrue(first.getRemainingTicks() == PaleIntercessionRules.DURATION_TICKS,
					"recall must refresh the full duration");
			helper.succeed();
		} finally { player.discard(); }
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 40)
	public static void duplicateCleanupKeepsOneLoadedBody(GameTestHelper helper) {
		ServerPlayer player = eligiblePlayer(helper);
		try {
			PaleIntercessionEntity a = body(helper.getLevel(), player);
			PaleIntercessionEntity b = body(helper.getLevel(), player);
			player.getPersistentData().putUUID(PaleIntercessionEntity.ACTIVE_MARKER, a.getUUID());
			helper.assertTrue(PaleIntercessionSummonService.summonOrRecall(player), "recall must succeed");
			helper.assertTrue(manifestations(helper, player).size() == 1, "loaded duplicates must be discarded immediately");
			helper.succeed();
		} finally { player.discard(); }
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 40)
	public static void crossDimensionBodyIsReplaced(GameTestHelper helper) {
		ServerPlayer player = eligiblePlayer(helper);
		try {
			ServerLevel nether = player.server.getLevel(Level.NETHER);
			if (nether == null) { helper.succeed(); return; }
			PaleIntercessionEntity old = body(nether, player);
			player.getPersistentData().putUUID(PaleIntercessionEntity.ACTIVE_MARKER, old.getUUID());
			helper.assertTrue(PaleIntercessionSummonService.summonOrRecall(player), "cross-dimension cast must replace");
			PaleIntercessionEntity replacement = manifestations(helper, player).getFirst();
			helper.assertTrue(!replacement.getUUID().equals(old.getUUID()), "replacement must be a new body");
			helper.succeed();
		} finally { player.discard(); }
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 40)
	public static void nbtRestoresOwnerAndRemainingDuration(GameTestHelper helper) {
		ServerPlayer player = eligiblePlayer(helper);
		try {
			PaleIntercessionEntity original = EntityInit.spectral_companion.get().create(helper.getLevel());
			original.bindTo(player);
			CompoundTag stored = new CompoundTag();
			original.addAdditionalSaveData(stored);
			PaleIntercessionEntity loaded = EntityInit.spectral_companion.get().create(helper.getLevel());
			loaded.readAdditionalSaveData(stored);
			helper.assertTrue(player.getUUID().equals(loaded.getOwnerUUID()), "owner UUID must survive NBT");
			helper.assertTrue(loaded.getRemainingTicks() == 1200, "remaining duration must survive NBT");
			helper.succeed();
		} finally { player.discard(); }
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 40)
	public static void hostileDamageConsumesTimeWithoutHealthLoss(GameTestHelper helper) {
		ServerPlayer player = eligiblePlayer(helper);
		try {
			PaleIntercessionEntity body = body(helper.getLevel(), player);
			Zombie zombie = EntityType.ZOMBIE.create(helper.getLevel());
			float health = body.getHealth();
			helper.assertTrue(body.hurt(zombie.damageSources().mobAttack(zombie), 5.5f), "hostile damage must be accepted");
			helper.assertTrue(body.getRemainingTicks() == 1090, "5.5 damage must consume 110 ticks");
			helper.assertTrue(body.getHealth() == health, "damage must not reduce health");
			helper.assertTrue(!body.hurt(player.damageSources().playerAttack(player), 5), "owner damage must be rejected");
			helper.succeed();
		} finally { player.discard(); }
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 40)
	public static void reactiveTargetStrikeAndFriendlyFireRules(GameTestHelper helper) {
		ServerPlayer player = eligiblePlayer(helper);
		try {
			PaleIntercessionEntity body = body(helper.getLevel(), player);
			Zombie zombie = EntityType.ZOMBIE.create(helper.getLevel());
			zombie.setPos(body.getX() + 1, body.getY(), body.getZ());
			helper.getLevel().addFreshEntity(zombie);
			float before = zombie.getHealth();
			helper.assertTrue(body.canDefendAgainst(zombie), "hostile monster must be a valid confirmed threat");
			helper.assertTrue(body.applyStrike(zombie, helper.getLevel().damageSources().generic()), "palm strike must land");
			helper.assertTrue(zombie.getHealth() <= before - 5.99f, "strike must deal six damage");
			helper.assertTrue(zombie.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "strike must apply Slowness I");
			helper.assertTrue(!body.canDefendAgainst(player), "manifestation must never attack a player");
			helper.succeed();
		} finally { player.discard(); }
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 40)
	public static void reactiveTargetAcquisitionInterposes(GameTestHelper helper) {
		ServerPlayer player = eligiblePlayer(helper);
		try {
			PaleIntercessionEntity body = body(helper.getLevel(), player);
			player.getPersistentData().putUUID(PaleIntercessionEntity.ACTIVE_MARKER, body.getUUID());
			Zombie zombie = EntityType.ZOMBIE.create(helper.getLevel());
			zombie.setPos(player.getX() + 5, player.getY(), player.getZ());
			zombie.setTarget(player);
			helper.getLevel().addFreshEntity(zombie);
			body.tick();
			helper.assertTrue(body.getTarget() == zombie, "a monster targeting the owner must be acquired");
			helper.assertTrue(zombie.getTarget() == body, "an acquired threat must be redirected to the manifestation");
			helper.succeed();
		} finally { player.discard(); }
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 40)
	public static void expiryDissolvesAfterTwentyTicks(GameTestHelper helper) {
		ServerPlayer player = eligiblePlayer(helper);
		try {
			PaleIntercessionEntity body = body(helper.getLevel(), player);
			player.getPersistentData().putUUID(PaleIntercessionEntity.ACTIVE_MARKER, body.getUUID());
			CompoundTag stored = new CompoundTag();
			body.addAdditionalSaveData(stored);
			stored.putInt("RemainingTicks", 1);
			body.readAdditionalSaveData(stored);
			for (int i = 0; i < PaleIntercessionRules.DISSOLUTION_TICKS + 1; i++) body.tick();
			helper.assertTrue(body.isRemoved(), "natural expiry must discard the body after its dissolution window");
			helper.succeed();
		} finally { player.discard(); }
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY, timeoutTicks = 40)
	public static void invalidOwnerMarkerStartsDissolution(GameTestHelper helper) {
		ServerPlayer player = eligiblePlayer(helper);
		try {
			PaleIntercessionEntity body = body(helper.getLevel(), player);
			player.getPersistentData().putUUID(PaleIntercessionEntity.ACTIVE_MARKER, UUID.randomUUID());
			body.tick();
			helper.assertTrue(body.getPresentation() == PaleIntercessionEntity.Presentation.DISSOLVE,
					"a stale body must dissolve when it no longer matches the owner's active marker");
			helper.succeed();
		} finally { player.discard(); }
	}

	private static PaleIntercessionEntity body(ServerLevel level, ServerPlayer player) {
		PaleIntercessionEntity body = EntityInit.spectral_companion.get().create(level);
		body.bindTo(player);
		body.setPos(player.getX() + 1, player.getY(), player.getZ());
		level.addFreshEntity(body);
		return body;
	}

	private static List<PaleIntercessionEntity> manifestations(GameTestHelper helper, ServerPlayer player) {
		List<PaleIntercessionEntity> bodies = new java.util.ArrayList<>();
		for (var entity : helper.getLevel().getAllEntities()) {
			if (entity instanceof PaleIntercessionEntity body && !body.isRemoved()
					&& player.getUUID().equals(body.getOwnerUUID())) bodies.add(body);
		}
		return bodies;
	}

	private static ServerPlayer eligiblePlayer(GameTestHelper helper) {
		ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
				new GameProfile(UUID.randomUUID(), "pale-intercession-player"), ClientInformation.createDefault()) {
			@Override protected ItemCooldowns createItemCooldowns() { return new ItemCooldowns(); }
		};
		BlockPos position = helper.absolutePos(new BlockPos(1, 2, 1));
		player.setPos(position.getX() + .5, position.getY(), position.getZ() + .5);
		var progress = HemoCapabilityAccess.requireUnstainedProgress(player);
		progress.setClarityUnlocked(true);
		progress.setClarity(75);
		HemoCapabilityAccess.requireKnownStillArts(player).learnArt(StillArtInit.pale_intercession.get());
		return player;
	}
}
