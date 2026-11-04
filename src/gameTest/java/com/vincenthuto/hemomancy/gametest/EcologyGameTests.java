package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.common.entity.mob.animal.PeacockSpiderEntity;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.ChthonianEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

/** Creature rules from the 2026-11-02 cleanup audit: rows A-12 and A-14 of its ledger. */
@GameTestHolder("ecology_validation")
@PrefixGameTestTemplate(false)
public final class EcologyGameTests {
	/** A-12 (EC-5): none of these still carries the 7-health, 1-damage copy-paste template. */
	@GameTest(template = "empty")
	public static void noCreatureKeepsTheTemplateStats(GameTestHelper helper) {
		List<EntityType<? extends LivingEntity>> types = List.of(EntityInit.abhorrent_thought.get(),
				EntityInit.fargone.get(), EntityInit.thirster.get(), EntityInit.lump_of_thought.get(),
				EntityInit.chthonian.get(), EntityInit.chthonian_queen.get());
		for (EntityType<? extends LivingEntity> type : types) {
			double health = DefaultAttributes.getSupplier(type).getValue(Attributes.MAX_HEALTH);
			double damage = DefaultAttributes.getSupplier(type).getValue(Attributes.ATTACK_DAMAGE);
			helper.assertTrue(health > 7.0D && damage > 1.0D, type + " keeps the template stats");
		}
		double chthonian = DefaultAttributes.getSupplier(EntityInit.chthonian.get()).getValue(Attributes.MAX_HEALTH);
		double queen = DefaultAttributes.getSupplier(EntityInit.chthonian_queen.get()).getValue(Attributes.MAX_HEALTH);
		helper.assertTrue(queen > chthonian * 2.0D, "the Queen is not a boss tier above her workers");
		helper.succeed();
	}

	/** A-12 (EC-5): a Chthonian that is hurt by a player turns on that player. */
	@GameTest(template = "empty", timeoutTicks = 60)
	public static void chthonianRetaliatesAgainstItsAttacker(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ChthonianEntity chthonian = helper.spawn(EntityInit.chthonian.get(), new BlockPos(1, 1, 1));
		ServerPlayer player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ecology"));
		BlockPos at = helper.absolutePos(new BlockPos(3, 1, 1));
		player.moveTo(at.getX() + 0.5D, at.getY(), at.getZ() + 0.5D, 0.0F, 0.0F);
		// Strike after the first tick: a hurt timestamp of zero is what the retaliation goal starts from.
		helper.runAfterDelay(5, () -> chthonian.hurt(level.damageSources().playerAttack(player), 1.0F));
		helper.runAfterDelay(20, () -> {
			helper.assertTrue(chthonian.getTarget() == player, "the Chthonian ignored the player who hit it: alive="
					+ chthonian.isAlive() + " lastHurtBy=" + chthonian.getLastHurtByMob() + " noAi=" + chthonian.isNoAi()
					+ " difficulty=" + level.getDifficulty() + " target=" + chthonian.getTarget());
			helper.succeed();
		});
	}

	/** A-14 (EC-7): a Peacock Spider passes both spawn rules on dirt in full daylight. */
	@GameTest(template = "empty")
	public static void peacockSpiderSpawnsInDaylight(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		level.setDayTime(6000L);
		helper.setBlock(new BlockPos(1, 1, 1), Blocks.DIRT);
		BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
		// Retried each tick: the rule reads sky light, which a freshly placed plot only has once the light engine has run.
		helper.succeedWhen(() -> {
			helper.assertTrue(SpawnPlacements.checkSpawnRules(EntityInit.peacock_spider.get(), level, MobSpawnType.NATURAL,
					pos, level.random), "the placement rule refused daylight dirt");
			PeacockSpiderEntity spider = EntityInit.peacock_spider.get().create(level);
			spider.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
			helper.assertTrue(spider.checkSpawnRules(level, MobSpawnType.NATURAL),
					"the monster light rule refused daylight");
		});
	}
}
