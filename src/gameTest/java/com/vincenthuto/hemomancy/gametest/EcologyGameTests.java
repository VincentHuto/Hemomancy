package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.common.entity.mob.animal.PeacockSpiderEntity;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.ChthonianEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.ProfessionalHarbingerEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
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

	/** CR-5 (NQ-06-5): the five mobs that shared 7 HP, 1 damage and speed 0.3 have their own numbers. */
	@GameTest(template = "empty")
	public static void theTemplateMobsHaveTheirOwnNumbers(GameTestHelper helper) {
		expectStats(helper, EntityInit.chitinite.get(), 10.0D, 4.0D, 1.0D, 0.25D);
		expectStats(helper, EntityInit.fervent_chitinite.get(), 18.0D, 8.0D, 3.0D, 0.28D);
		expectStats(helper, EntityInit.fungling.get(), 10.0D, 0.0D, 2.0D, 0.32D);
		expectStats(helper, EntityInit.enthralled_doll.get(), 5.0D, 0.0D, 2.0D, 0.36D);
		expectStats(helper, EntityInit.barbed_urchin.get(), 10.0D, 3.0D, 1.0D, 0.3D);
		helper.succeed();
	}

	private static void expectStats(GameTestHelper helper, EntityType<? extends LivingEntity> type, double health,
			double armor, double damage, double speed) {
		var supplier = DefaultAttributes.getSupplier(type);
		helper.assertTrue(supplier.getValue(Attributes.MAX_HEALTH) == health
				&& supplier.getValue(Attributes.ARMOR) == armor
				&& supplier.getValue(Attributes.ATTACK_DAMAGE) == damage
				&& Math.abs(supplier.getValue(Attributes.MOVEMENT_SPEED) - speed) < 1.0E-6D,
				type + " does not carry its CR-5 numbers");
	}

	/** CR-2 (NQ-06-2, NQ-06-3): a natural spawn in the Gardens dimension is raised; any other spawn is not. */
	@GameTest(template = "empty")
	public static void onlyNaturalGardensSpawnsTakeTheRaisedRoster(GameTestHelper helper) {
		var gardens = com.vincenthuto.hemomancy.common.encounter.FungalGardenTravelHelper.FUNGAL_GARDENS;
		Mob raised = EntityInit.abhorrent_thought.get().create(helper.getLevel());
		helper.assertTrue(com.vincenthuto.hemomancy.common.event.GardensRosterSpawnEvents.apply(raised, gardens,
				MobSpawnType.NATURAL), "a natural Gardens spawn was not raised");
		helper.assertTrue(raised.getMaxHealth() == 90.0F && raised.getHealth() == 90.0F
				&& raised.getAttributeBaseValue(Attributes.ARMOR) == 8.0D
				&& raised.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) == 13.0D,
				"the Abhorrent Thought did not take 90 HP, 8 armour and 13 damage");
		Mob overworld = EntityInit.abhorrent_thought.get().create(helper.getLevel());
		helper.assertTrue(!com.vincenthuto.hemomancy.common.event.GardensRosterSpawnEvents.apply(overworld,
				net.minecraft.world.level.Level.OVERWORLD, MobSpawnType.NATURAL), "an Overworld spawn was raised");
		helper.assertTrue(overworld.getMaxHealth() == 40.0F, "the Overworld Abhorrent Thought lost its own health");
		Mob brood = EntityInit.fungling.get().create(helper.getLevel());
		helper.assertTrue(!com.vincenthuto.hemomancy.common.event.GardensRosterSpawnEvents.apply(brood, gardens,
				MobSpawnType.MOB_SUMMONED), "a summoned Gardens Fungling was raised");
		Mob wild = EntityInit.fungling.get().create(helper.getLevel());
		helper.assertTrue(com.vincenthuto.hemomancy.common.event.GardensRosterSpawnEvents.apply(wild, gardens,
				MobSpawnType.CHUNK_GENERATION) && wild.getMaxHealth() == 16.0F, "a wild Gardens Fungling was not raised");
		Mob notOnRoster = EntityInit.chitinite.get().create(helper.getLevel());
		helper.assertTrue(!com.vincenthuto.hemomancy.common.event.GardensRosterSpawnEvents.apply(notOnRoster, gardens,
				MobSpawnType.NATURAL), "a mob off the roster was raised");
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
	@GameTest(template = "empty", skyAccess = true, timeoutTicks = 200)
	public static void peacockSpiderSpawnsInDaylight(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		// The plot replaces a block of the room's stone floor (template y 3, which is helper y 4: helper positions count
		// from the structure block one below the template) under open sky. GameTestWorldClock already holds noon.
		helper.setBlock(new BlockPos(1, 4, 1), Blocks.DIRT);
		BlockPos pos = helper.absolutePos(new BlockPos(1, 5, 1));
		// Retried each tick: the rule reads sky light, which a freshly placed plot only has once the light engine has run.
		helper.succeedWhen(() -> {
			helper.assertTrue(SpawnPlacements.checkSpawnRules(EntityInit.peacock_spider.get(), level, MobSpawnType.NATURAL,
					pos, level.random), "the placement rule refused daylight dirt: sky light "
							+ level.getBrightness(net.minecraft.world.level.LightLayer.SKY, pos) + ", day time " + level.getDayTime()
							+ " can see sky " + level.canSeeSky(pos));
			PeacockSpiderEntity spider = EntityInit.peacock_spider.get().create(level);
			spider.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
			helper.assertTrue(spider.checkSpawnRules(level, MobSpawnType.NATURAL),
					"the monster light rule refused daylight");
		});
	}

	/** FX-4: mob damage lives in attributes. The four teachers keep theirs: a Misbegotten teacher fights. */
	@GameTest(template = "empty")
	public static void mobDamageLivesInAttributes(GameTestHelper helper) {
		assertAttack(helper, EntityInit.circus_fire_eater.get(), 4.0D);
		assertAttack(helper, EntityInit.circus_stilt_walker.get(), 3.0D);
		assertAttack(helper, EntityInit.circus_acrobat.get(), 4.0D);
		assertAttack(helper, EntityInit.vampire_bat.get(), 1.0D);
		assertAttack(helper, EntityInit.harbinger_vicar.get(), 4.0D);
		assertAttack(helper, EntityInit.circus_strongman.get(), 4.0D);
		for (EntityType<? extends Mob> teacher : teachers())
			assertAttack(helper, teacher, 4.0D);
		helper.succeed();
	}

	/** FX-4: a Misbegotten teacher reaches its survival officiant and its melee strike lands without throwing. */
	@GameTest(template = "empty", timeoutTicks = 200)
	public static void misbegottenTeacherStrikesItsOfficiant(GameTestHelper helper) {
		List<CapturedPlayer> officiants = new ArrayList<>();
		List<ProfessionalHarbingerEntity> attackers = new ArrayList<>();
		int x = 1;
		for (EntityType<? extends Mob> type : teachers()) {
			CapturedPlayer officiant = CapturedPlayer.create(helper).online();
			officiants.add(officiant);
			helper.assertFalse(officiant.player.isCreative() || officiant.player.isSpectator(), "the officiant is not in survival");
			ProfessionalHarbingerEntity direct = (ProfessionalHarbingerEntity) type.create(helper.getLevel());
			direct.initializeMisbegotten(officiant.player.getUUID(), 42L, "Teacher");
			direct.doHurtTarget(officiant.player);
			direct.discard();
			ProfessionalHarbingerEntity teacher = (ProfessionalHarbingerEntity) helper.spawn(type, new BlockPos(x, 1, 1));
			teacher.initializeMisbegotten(officiant.player.getUUID(), 42L, "Teacher");
			BlockPos at = helper.absolutePos(new BlockPos(x, 1, 2));
			officiant.player.moveTo(at.getX() + 0.5D, at.getY(), at.getZ() + 0.5D, 0.0F, 0.0F);
			officiant.player.invulnerableTime = 0;
			clearSpawnGrace(officiant.player);
			attackers.add(teacher);
			x += 3;
		}
		helper.succeedWhen(() -> {
			for (int i = 0; i < attackers.size(); i++)
				helper.assertTrue(attackers.get(i).getLastHurtMob() == officiants.get(i).player,
						attackers.get(i).getType() + " never landed a melee strike on its officiant");
			officiants.forEach(CapturedPlayer::close);
		});
	}

	private static void clearSpawnGrace(ServerPlayer player) {
		try {
			var field = ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");
			field.setAccessible(true);
			field.setInt(player, 0);
		} catch (ReflectiveOperationException error) {
			throw new AssertionError(error);
		}
	}

	private static List<EntityType<? extends Mob>> teachers() {
		return List.of(EntityInit.harbinger_alchemist.get(), EntityInit.harbinger_artificer.get(),
				EntityInit.harbinger_cicatrix_anchorite.get(), EntityInit.harbinger_mnemonist.get());
	}

	private static void assertAttack(GameTestHelper helper, EntityType<? extends LivingEntity> type, double expected) {
		double damage = DefaultAttributes.getSupplier(type).getValue(Attributes.ATTACK_DAMAGE);
		helper.assertTrue(damage == expected, type + " attack " + damage + ", expected " + expected);
	}

	/** CR-3: a Blood-Drunk Puppeteer keeps the monster light rule, so daylight dirt refuses it. */
	@GameTest(template = "empty", skyAccess = true, timeoutTicks = 200)
	public static void bloodDrunkPuppeteerRefusesDaylight(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		helper.setBlock(new BlockPos(1, 4, 1), Blocks.DIRT);
		BlockPos pos = helper.absolutePos(new BlockPos(1, 5, 1));
		helper.succeedWhen(() -> {
			int sky = level.getBrightness(net.minecraft.world.level.LightLayer.SKY, pos);
			helper.assertTrue(sky == 15, "sky light not settled yet: " + sky);
			helper.assertFalse(SpawnPlacements.checkSpawnRules(EntityInit.blood_drunk_puppeteer.get(), level,
					MobSpawnType.NATURAL, pos, level.random), "a Puppeteer may spawn in full daylight");
		});
	}

	/** CR-3: in a sealed dark cell the same rule lets a Puppeteer spawn. */
	@GameTest(template = "empty", timeoutTicks = 200)
	public static void bloodDrunkPuppeteerSpawnsInTheDark(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		helper.setBlock(new BlockPos(2, 4, 2), Blocks.DIRT);
		for (int y = 5; y <= 7; y++)
			for (int x = 1; x <= 3; x++)
				for (int z = 1; z <= 3; z++)
					if (x != 2 || z != 2 || y == 7) helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
		BlockPos pos = helper.absolutePos(new BlockPos(2, 5, 2));
		helper.succeedWhen(() -> {
			int sky = level.getBrightness(net.minecraft.world.level.LightLayer.SKY, pos);
			int block = level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, pos);
			helper.assertTrue(sky == 0 && block == 0, "the cell is not dark yet: sky " + sky + ", block " + block);
			helper.assertTrue(SpawnPlacements.checkSpawnRules(EntityInit.blood_drunk_puppeteer.get(), level,
					MobSpawnType.NATURAL, pos, level.random), "a Puppeteer was refused a dark cell");
		});
	}
}
