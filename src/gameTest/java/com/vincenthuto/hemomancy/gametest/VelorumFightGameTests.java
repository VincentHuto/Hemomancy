package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.block.harbinger.decoration.FrozenDevoteeBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossGroundHazardEntity;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintChannelRouter;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintCombatController;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintCompanion;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintRoot;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumCongregationRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumDarkRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumDeedRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumDevoteeEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEclipseRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumLocomotionRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumOpeningRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumPulseRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumSelectionRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumStillnessRules;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/**
 * Velorum's fight with real entities (2026-11-05 overhaul design, section 16): the stillness meter on a real player,
 * preservation and its cap, an opening halting the fill, the Still Guard's floors and the Bow at 60%, the last tenth
 * falling only inside an opening into the ending, the lanterns and the congregation restored, waking and rising, and the
 * challenger scaling. Velorum here has no tomb, so his arena is the fallback box round him; each case runs in its own
 * batch, because that box is wider than a test's plot and another case's player would count as his challenger.
 * <p>
 * The Frozen Cruor and liquid dark revision (2026-11-06, R.15) adds the dark pools and the lantern's light, the Gutter's
 * flood, Arrested Pulse's beats, the javelins on a frozen figure, the veins from frozen and pinned figures, the Shadow
 * Step's destination and untouchable span, the gloam cuts' heights, and the Eclipse relit, unbroken and its lanterns.
 * The cases drive his ticks by hand, so the owned hazards (the pools) are ticked by hand with him.
 */
@GameTestHolder("velorum_fight_validation")
@PrefixGameTestTemplate(false)
public final class VelorumFightGameTests {
	private static final String TEMPLATE = "empty";
	private static final BlockPos SAINT_AT = new BlockPos(4, 2, 4);
	private static final BlockPos FIGURE = new BlockPos(8, 2, 8);

	private VelorumFightGameTests() {
	}

	private static VelorumEntity velorum(GameTestHelper h) {
		VelorumEntity velorum = EntityInit.velorum.get().create(h.getLevel());
		velorum.setNoGravity(true);
		velorum.moveTo(h.absoluteVec(Vec3.atBottomCenterOf(SAINT_AT)));
		h.getLevel().addFreshEntity(velorum);
		return velorum;
	}

	private static ServerPlayer challenger(GameTestHelper h, VelorumEntity velorum, Vec3 offset) {
		ServerPlayer player = DuctilisGameTests.player(h);
		player.moveTo(velorum.position().add(offset));
		player.setOldPosAndRot();
		clearSpawnInvulnerability(player);
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

	private static void tick(VelorumEntity velorum, int ticks) {
		for (int i = 0; i < ticks && !velorum.isRemoved(); i++) velorum.aiStep();
	}

	private static float stillness(VelorumEntity velorum, ServerPlayer player) {
		return velorum.stillness().value(velorum, player);
	}

	/** His ticks with his owned hazards ticked beside him, as the level would: a pool's age is its own tick count. */
	private static void tickWithHazards(GameTestHelper h, VelorumEntity velorum, int ticks) {
		AABB stage = new AABB(h.absolutePos(BlockPos.ZERO)).inflate(32.0D);
		for (int i = 0; i < ticks && !velorum.isRemoved(); i++) {
			velorum.aiStep();
			for (BossGroundHazardEntity hazard : h.getLevel().getEntitiesOfClass(BossGroundHazardEntity.class, stage)) {
				hazard.setOldPosAndRot();
				hazard.tickCount++;
				hazard.tick();
			}
		}
	}

	/** A move forced now, as the fight's state starts the Hour and the Eclipse. */
	private static void start(VelorumEntity velorum, VelorumMove move) {
		velorum.controller().start(move, SaintCombatController.Cut.IMMEDIATE);
	}

	/** One step of a challenger walking back and forth on the spot at 0.2 blocks a tick. */
	private static void pace(ServerPlayer player, int step) {
		player.setOldPosAndRot();
		player.moveTo(player.position().add(step % 2 == 0 ? 0.2D : -0.2D, 0.0D, 0.0D));
	}

	private static ServerPlayer bleeding(ServerPlayer player) {
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(2000);
		volume.setBloodVolume(1000);
		return player;
	}

	private static void sweepHazards(GameTestHelper h) {
		AABB stage = new AABB(h.absolutePos(BlockPos.ZERO)).inflate(32.0D);
		h.getLevel().getEntitiesOfClass(BossGroundHazardEntity.class, stage).forEach(Entity::discard);
	}

	private static void placeFigure(GameTestHelper h, BlockPos at) {
		BlockState lower = BlockInit.frozen_devotee.get().defaultBlockState().setValue(FrozenDevoteeBlock.HALF, DoubleBlockHalf.LOWER);
		h.getLevel().setBlock(h.absolutePos(at), lower, Block.UPDATE_CLIENTS);
		h.getLevel().setBlock(h.absolutePos(at.above()), lower.setValue(FrozenDevoteeBlock.HALF, DoubleBlockHalf.UPPER),
				Block.UPDATE_CLIENTS);
	}

	private static void clear(GameTestHelper h, BlockPos at) {
		h.getLevel().setBlock(h.absolutePos(at.above()), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
		h.getLevel().setBlock(h.absolutePos(at), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
	}

	private static void sweep(GameTestHelper h) {
		AABB stage = new AABB(h.absolutePos(BlockPos.ZERO)).inflate(24.0D);
		h.getLevel().getEntitiesOfClass(ItemEntity.class, stage).forEach(Entity::discard);
		h.getLevel().getEntitiesOfClass(VelorumDevoteeEntity.class, stage).forEach(Entity::discard);
	}

	// ── the meter ────────────────────────────────────────────────────

	@GameTest(template = TEMPLATE, timeoutTicks = 60, batch = "velorum_fight_meter")
	public static void stillnessFillsStandingAndDrainsWalking(GameTestHelper h) {
		VelorumEntity velorum = velorum(h);
		ServerPlayer player = challenger(h, velorum, new Vec3(14.0D, 0.0D, 0.0D));
		try {
			tick(velorum, 2);
			tick(velorum, 20);
			float stood = stillness(velorum, player);
			h.assertTrue(stood >= 20 * VelorumStillnessRules.STAND_FILL - 1.0F, "standing filled the meter: " + stood);
			for (int i = 0; i < 20; i++) {
				player.setOldPosAndRot();
				player.moveTo(player.position().add(0.0D, 0.0D, 0.25D));
				velorum.aiStep();
			}
			h.assertTrue(stillness(velorum, player) < stood, "walking drained it");
			h.succeed();
		} finally {
			velorum.discard();
			player.discard();
		}
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 80, batch = "velorum_fight_preserve")
	public static void preservationFreezesHurtsAndThawsWithItsCap(GameTestHelper h) {
		VelorumEntity velorum = velorum(h);
		ServerPlayer player = challenger(h, velorum, new Vec3(14.0D, 0.0D, 0.0D));
		try {
			tick(velorum, 2);
			float health = player.getHealth();
			velorum.meter().add(player, VelorumStillnessRules.MAX);
			tick(velorum, 1);
			h.assertTrue(velorum.stillness().preserved(player), "a full meter preserves the player");
			h.assertTrue(SaintRoot.rooted(player), "the preserved are held");
			h.assertTrue(player.getHealth() < health, "preservation hurts");
			tick(velorum, VelorumStillnessRules.PRESERVED_TICKS + 1);
			h.assertTrue(!velorum.stillness().preserved(player), "the ice lets go after 30 ticks");
			h.assertTrue(Math.abs(stillness(velorum, player) - VelorumStillnessRules.RELEASE_VALUE) < 2.0F,
					"release sets the meter to 50");
			velorum.stillness().add(velorum, player, VelorumStillnessRules.MAX);
			h.assertTrue(stillness(velorum, player) <= VelorumStillnessRules.CAPPED + 1.0E-3F, "the 95 cap holds after it");
			h.succeed();
		} finally {
			velorum.discard();
			player.discard();
		}
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 60, batch = "velorum_fight_halt")
	public static void anOpeningHaltsEveryChallengersFill(GameTestHelper h) {
		VelorumEntity velorum = velorum(h);
		ServerPlayer a = challenger(h, velorum, new Vec3(14.0D, 0.0D, 0.0D));
		ServerPlayer b = challenger(h, velorum, new Vec3(-14.0D, 0.0D, 0.0D));
		try {
			tick(velorum, 2);
			velorum.openings().open(VelorumOpeningRules.GUTTER, VelorumOpeningRules.GUTTER.ticks());
			float before = stillness(velorum, a) + stillness(velorum, b);
			tick(velorum, 10);
			h.assertTrue(stillness(velorum, a) + stillness(velorum, b) <= before + 1.0E-3F, "nobody fills while he gutters");
			h.succeed();
		} finally {
			velorum.discard();
			a.discard();
			b.discard();
		}
	}

	// ── the guard, the floors and the ending ─────────────────────────

	@GameTest(template = TEMPLATE, timeoutTicks = 60, batch = "velorum_fight_guard")
	public static void stillGuardHoldsTheFloorsAndTheBowOpensAtSixty(GameTestHelper h) {
		VelorumEntity velorum = velorum(h);
		ServerPlayer player = challenger(h, velorum, new Vec3(2.0D, 0.0D, 0.0D));
		try {
			tick(velorum, 2);
			float max = velorum.getMaxHealth();
			velorum.invulnerableTime = 0;
			velorum.hurt(velorum.damageSources().playerAttack(player), 10.0F);
			h.assertTrue(max - velorum.getHealth() < 10.0F * 0.31F, "the Still Guard takes 70% off a blow");
			for (int i = 0; i < 40 && !velorum.openings().isOpen(); i++) {
				velorum.invulnerableTime = 0;
				velorum.hurt(velorum.damageSources().playerAttack(player), 200.0F);
			}
			h.assertTrue(velorum.getHealth() >= max * 0.6F - 0.01F, "the 60% floor holds outside an opening");
			h.assertTrue(velorum.openings().current() != null
					&& velorum.openings().current().id() == VelorumOpeningRules.BOW_ID, "he bows at the floor");
			h.succeed();
		} finally {
			velorum.discard();
			player.discard();
		}
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 300, batch = "velorum_fight_ending")
	public static void theLastTenthOnlyFallsInsideAnOpeningAndTheTrialEndsOnce(GameTestHelper h) {
		VelorumEntity velorum = velorum(h);
		ServerPlayer player = challenger(h, velorum, new Vec3(2.0D, 0.0D, 0.0D));
		try {
			tick(velorum, 2);
			float max = velorum.getMaxHealth();
			velorum.setHealth(max * 0.08F);
			velorum.invulnerableTime = 0;
			velorum.hurt(velorum.damageSources().playerAttack(player), 30.0F);
			h.assertTrue(Math.abs(velorum.getHealth() - max * 0.08F) < 0.01F, "under the last tenth nothing outside an opening hurts him");
			velorum.openings().open(VelorumOpeningRules.GUTTER, VelorumOpeningRules.GUTTER.ticks());
			velorum.invulnerableTime = 0;
			velorum.hurt(velorum.damageSources().playerAttack(player), 500.0F);
			h.assertTrue(velorum.ending(), "a blow inside an opening fails his body");
			h.assertTrue(velorum.getHealth() >= 1.0F, "a Saint is never killed");
			tick(velorum, 205);
			h.assertTrue(velorum.isRemoved() && velorum.hasTrialEnded(), "the ending ran endTrial once and he went");
			sweep(h);
			h.succeed();
		} finally {
			if (!velorum.isRemoved()) velorum.discard();
			player.discard();
		}
	}

	// ── the chamber ──────────────────────────────────────────────────

	@GameTest(template = TEMPLATE, timeoutTicks = 60, batch = "velorum_fight_lights")
	public static void theDarkenedLanternsComeBackWhenHeIsRemoved(GameTestHelper h) {
		BlockPos lantern = h.absolutePos(new BlockPos(6, 4, 6));
		h.getLevel().setBlock(lantern, BlockInit.rime_lantern.get().defaultBlockState(), Block.UPDATE_CLIENTS);
		VelorumEntity velorum = velorum(h);
		tick(velorum, 1);
		velorum.congregation().darkenAll(velorum, 100);
		h.assertTrue(h.getLevel().getBlockState(lantern).is(Blocks.PACKED_ICE), "the Endless Hour darkens the rime lanterns");
		velorum.discard();
		h.assertTrue(h.getLevel().getBlockState(lantern).is(BlockInit.rime_lantern.get()), "removal relights them");
		h.getLevel().setBlock(lantern, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
		h.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 80, batch = "velorum_fight_wake")
	public static void heldProjectionWakesADevoteeInAProperFight(GameTestHelper h) {
		placeFigure(h, FIGURE);
		VelorumEntity velorum = velorum(h);
		ServerPlayer player = challenger(h, velorum, new Vec3(1.5D, 0.0D, 6.0D));
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(2000);
		volume.setBloodVolume(1000);
		try {
			tick(velorum, 1);
			Vec3 figure = Vec3.atCenterOf(h.absolutePos(FIGURE));
			int wanted = 41;
			for (int i = 0; i < wanted; i++) {
				player.lookAt(EntityAnchorArgument.Anchor.EYES, figure);
				SaintChannelRouter.tryProject(h.getLevel(), player, 8.0D, 5.0D, false);
			}
			h.assertTrue(h.getLevel().getBlockState(h.absolutePos(FIGURE)).isAir()
					&& h.getLevel().getBlockState(h.absolutePos(FIGURE.above())).isAir(), "the figure was taken up, both cells");
			List<VelorumDevoteeEntity> devotees = h.getLevel().getEntitiesOfClass(VelorumDevoteeEntity.class,
					new AABB(h.absolutePos(FIGURE)).inflate(2.0D));
			h.assertTrue(devotees.size() == 1 && devotees.get(0).allegiance() == SaintCompanion.Allegiance.CHALLENGERS,
					"a proper fight's figure wakes beside the challengers");
			h.assertTrue(volume.getBloodVolume() <= 1000 - 119.0D, "120 blood paid through the waking source");
			velorum.discard();
			h.assertTrue(h.getLevel().getBlockState(h.absolutePos(FIGURE)).is(BlockInit.frozen_devotee.get()),
					"removal put the figure back");
			h.assertTrue(devotees.get(0).isRemoved(), "and the devotee went with it");
			h.succeed();
		} finally {
			if (!velorum.isRemoved()) velorum.discard();
			player.discard();
			clear(h, FIGURE);
		}
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 80, batch = "velorum_fight_forced")
	public static void aForcedFightRaisesHostilesAtTheFloorAndAReloadRestores(GameTestHelper h) {
		placeFigure(h, FIGURE);
		placeFigure(h, FIGURE.east(2));
		ServerLevel level = h.getLevel();
		VelorumEntity velorum = velorum(h);
		velorum.setAwakening("FORCED", List.of("REPOSE", "LITANY"));
		ServerPlayer player = challenger(h, velorum, new Vec3(3.0D, 0.0D, 3.0D));
		try {
			tick(velorum, 1);
			velorum.congregation().floorRise(velorum, 0);
			List<VelorumDevoteeEntity> risen = level.getEntitiesOfClass(VelorumDevoteeEntity.class,
					new AABB(h.absolutePos(FIGURE)).inflate(4.0D));
			h.assertTrue(risen.size() == 2 && risen.stream().allMatch(d -> d.allegiance() == SaintCompanion.Allegiance.SAINT),
					"two rise against a forced challenger at the floor");
			int mask = velorum.deeds().mask();
			h.assertTrue(VelorumDeedRules.repose(mask) && VelorumDeedRules.litany(mask) && !VelorumDeedRules.beholding(mask),
					"the skipped Chapels lend their deeds");
			h.assertTrue(VelorumMove.CRUOR_RELEASE.timeline(new SaintMoveInputs(1, 0, mask, 1)).recovery() == 15,
					"he never pauses: recoveries x0.7");
			CompoundTag saved = new CompoundTag();
			velorum.saveWithoutId(saved);
			velorum.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
			VelorumEntity reloaded = EntityInit.velorum.get().create(level);
			reloaded.load(saved);
			reloaded.setNoGravity(true);
			level.addFreshEntity(reloaded);
			reloaded.aiStep();
			h.assertTrue(level.getBlockState(h.absolutePos(FIGURE)).is(BlockInit.frozen_devotee.get())
					&& level.getBlockState(h.absolutePos(FIGURE.east(2))).is(BlockInit.frozen_devotee.get()),
					"a reload restored the figures on its first tick");
			reloaded.discard();
			sweep(h);
			h.succeed();
		} finally {
			if (!velorum.isRemoved()) velorum.discard();
			player.discard();
			clear(h, FIGURE);
			clear(h, FIGURE.east(2));
		}
	}

	// ── revision 2026-11-06: Frozen Cruor and liquid dark ────────────

	@GameTest(template = TEMPLATE, timeoutTicks = 80, batch = "velorum_fight_dark_pools")
	public static void darkPoolsFillFasterAndRecedeFromTheLantern(GameTestHelper h) {
		VelorumEntity velorum = velorum(h);
		ServerPlayer dark = challenger(h, velorum, new Vec3(7.0D, 0.0D, 0.0D));
		ServerPlayer lit = challenger(h, velorum, new Vec3(-7.0D, 0.0D, 0.0D));
		ServerPlayer near = challenger(h, velorum, new Vec3(0.0D, 0.0D, 3.0D));
		try {
			tick(velorum, 2);
			velorum.dark().pool(velorum, dark.position(), 2.5D, 400);
			velorum.dark().pool(velorum, near.position(), 2.5D, 400);
			tickWithHazards(h, velorum, 12);
			h.assertTrue(velorum.dark().inDark(dark), "a challenger in a pool outside his light is in the dark");
			h.assertTrue(!velorum.dark().inDark(near), "his lantern's light holds the dark back within 6");
			h.assertTrue(!velorum.dark().inDark(lit), "out of every pool is out of the dark");
			float darkFrom = stillness(velorum, dark), litFrom = stillness(velorum, lit);
			tickWithHazards(h, velorum, 20);
			float darkGain = stillness(velorum, dark) - darkFrom, litGain = stillness(velorum, lit) - litFrom;
			h.assertTrue(darkGain > litGain * 1.3F, "the dark fills faster: " + darkGain + " against " + litGain);
			h.succeed();
		} finally {
			velorum.discard();
			dark.discard();
			lit.discard();
			near.discard();
			sweepHazards(h);
		}
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 80, batch = "velorum_fight_gutter_flood")
	public static void theGutterFloodsTheDarkToHisFeet(GameTestHelper h) {
		VelorumEntity velorum = velorum(h);
		ServerPlayer player = challenger(h, velorum, new Vec3(2.0D, 0.0D, 0.0D));
		try {
			tick(velorum, 2);
			velorum.dark().pool(velorum, velorum.position().add(4.0D, 0.0D, 0.0D), 2.5D, 400);
			tickWithHazards(h, velorum, 10);
			h.assertTrue(!velorum.dark().inDark(player), "by his feet, the lantern holds the dark off");
			velorum.openings().open(VelorumOpeningRules.GUTTER, VelorumOpeningRules.GUTTER.ticks());
			tickWithHazards(h, velorum, 12);
			h.assertTrue(velorum.dark().lantern() <= VelorumDarkRules.GUTTER_LIGHT + 1.0E-3F, "the light gutters to 1");
			h.assertTrue(velorum.dark().inDark(player), "the dark floods in to his feet");
			h.succeed();
		} finally {
			velorum.discard();
			player.discard();
			sweepHazards(h);
		}
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 80, batch = "velorum_fight_pulse")
	public static void arrestedPulseBurstsOnAStillChallengerAndPassesAWalkingOne(GameTestHelper h) {
		VelorumEntity velorum = velorum(h);
		ServerPlayer still = challenger(h, velorum, new Vec3(4.0D, 0.0D, 0.0D));
		ServerPlayer walker = challenger(h, velorum, new Vec3(-4.0D, 0.0D, 0.0D));
		try {
			tick(velorum, 2);
			velorum.stillness().mark(velorum, still);
			velorum.stillness().mark(velorum, walker);
			float stillHealth = still.getHealth(), walkerHealth = walker.getHealth();
			for (int i = 0; i < VelorumPulseRules.BEATS[0] + 1; i++) {
				pace(walker, i);
				velorum.aiStep();
			}
			h.assertTrue(still.getHealth() < stillHealth, "cruor spines burst out of the still one");
			h.assertTrue(walker.getHealth() >= walkerHealth, "the walking one shed the frost");
			h.assertTrue(velorum.stillness().marked(still) && velorum.stillness().marked(walker), "two beats are left");
			h.succeed();
		} finally {
			velorum.discard();
			still.discard();
			walker.discard();
		}
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 100, batch = "velorum_fight_javelins")
	public static void cruorJavelinsShatterOnAFrozenFigure(GameTestHelper h) {
		BlockPos cover = SAINT_AT.south(4);
		placeFigure(h, cover);
		VelorumEntity velorum = velorum(h);
		ServerPlayer player = challenger(h, velorum, new Vec3(0.0D, 0.0D, 8.0D));
		try {
			tick(velorum, 2);
			double reach = velorum.shaftReach(velorum.position(), new Vec3(0.0D, 0.0D, 1.0D));
			h.assertTrue(reach < 4.5D, "the shaft's line stops at the figure: " + reach);
			float health = player.getHealth();
			start(velorum, VelorumMove.CRUOR_JAVELINS);
			BossMoveTimeline timeline = VelorumMove.CRUOR_JAVELINS.timeline(SaintMoveInputs.NONE);
			for (int i = 0; i <= timeline.windup() + 2; i++) velorum.aiStep();
			h.assertTrue(velorum.javelinCut(0), "the first shaft was cut short at the figure");
			tick(velorum, timeline.active() + 2);
			h.assertTrue(player.getHealth() >= health, "the shafts shattered harmlessly on the rime");
			h.succeed();
		} finally {
			velorum.discard();
			player.discard();
			clear(h, cover);
		}
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 100, batch = "velorum_fight_veins")
	public static void veinsRunFromFrozenFiguresAndNotFromPinnedOnes(GameTestHelper h) {
		BlockPos a = new BlockPos(9, 2, 4), b = new BlockPos(9, 2, 8), c = new BlockPos(9, 2, 12);
		placeFigure(h, a);
		placeFigure(h, b);
		placeFigure(h, c);
		VelorumEntity velorum = velorum(h);
		velorum.setAwakening("FORCED", List.of());
		ServerPlayer player = bleeding(challenger(h, velorum, new Vec3(3.0D, 0.0D, 4.0D)));
		try {
			tick(velorum, 1);
			h.assertTrue(velorum.congregation().feeding(velorum, player.position(), VelorumSelectionRules.VEIN_REACH).size() == 3,
					"three frozen figures feed his veins");
			Vec3 pinned = Vec3.atCenterOf(h.absolutePos(b));
			for (int i = 0; i < VelorumCongregationRules.PIN_TICKS + 1; i++) {
				player.lookAt(EntityAnchorArgument.Anchor.EYES, pinned);
				SaintChannelRouter.tryProject(h.getLevel(), player, 8.0D, 5.0D, false);
			}
			List<Vec3> feeding = velorum.congregation().feeding(velorum, player.position(), VelorumSelectionRules.VEIN_REACH);
			Vec3 pinnedFoot = Vec3.atBottomCenterOf(h.absolutePos(b));
			h.assertTrue(feeding.size() == 2 && feeding.stream().noneMatch(at -> at.distanceTo(pinnedFoot) < 0.1D),
					"the pinned figure's veins are shut");
			start(velorum, VelorumMove.VEINS_OF_FROST);
			tick(velorum, 2);
			var frame = velorum.controller().frame();
			h.assertTrue(frame != null && frame.anchors().size() == 2, "a vein from each frozen figure");
			h.assertTrue(frame.anchors().stream().noneMatch(at -> Math.hypot(at.x - pinnedFoot.x, at.z - pinnedFoot.z) < 0.1D),
					"none from the pinned one");
			h.assertTrue(frame.points().size() >= 3, "the veins run along the seams to the meeting point");
			h.succeed();
		} finally {
			velorum.discard();
			player.discard();
			clear(h, a);
			clear(h, b);
			clear(h, c);
			sweep(h);
		}
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 80, batch = "velorum_fight_step")
	public static void shadowStepStaysInTheLeashAndCannotBeHitUnder(GameTestHelper h) {
		VelorumEntity velorum = velorum(h);
		ServerPlayer player = challenger(h, velorum, new Vec3(6.0D, 0.0D, 0.0D));
		try {
			tick(velorum, 2);
			start(velorum, VelorumMove.UMBRAL_STEP);
			tickWithHazards(h, velorum, 1);
			var frame = velorum.controller().frame();
			h.assertTrue(frame != null && frame.points().size() == 1, "the destination is known from tick 0");
			Vec3 to = frame.points().get(0);
			AABB leash = velorum.arena().bossFloor().inflate(1.0D, 0.0D, 1.0D);
			h.assertTrue(to.x >= leash.minX && to.x <= leash.maxX && to.z >= leash.minZ && to.z <= leash.maxZ,
					"he rises inside the leash, never in the aisles");
			double distance = Math.hypot(to.x - player.getX(), to.z - player.getZ());
			h.assertTrue(distance >= VelorumLocomotionRules.STEP_MIN - 0.01D && distance <= VelorumLocomotionRules.STEP_MAX + 0.01D,
					"4 to 6 from the target: " + distance);
			tickWithHazards(h, velorum, VelorumLocomotionRules.STEP_UNDER + 1);
			float health = velorum.getHealth();
			velorum.invulnerableTime = 0;
			velorum.hurt(velorum.damageSources().playerAttack(player), 50.0F);
			h.assertTrue(velorum.getHealth() >= health, "under the dark there is nothing of him to strike");
			tickWithHazards(h, velorum, 12);
			h.assertTrue(Math.hypot(velorum.getX() - to.x, velorum.getZ() - to.z) < 0.5D, "he rose where the pool opened");
			h.succeed();
		} finally {
			velorum.discard();
			player.discard();
			sweepHazards(h);
		}
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 100, batch = "velorum_fight_gloam_cuts")
	public static void gloamCutsMissAJumperAndAStoopingChallenger(GameTestHelper h) {
		VelorumEntity velorum = velorum(h);
		Vec3 origin = velorum.position();
		FootprintFrame line = FootprintFrame.simple(origin, origin.add(1.0D, 0.0D, 0.0D), origin, origin.y);
		ServerPlayer stooping = challenger(h, velorum, new Vec3(8.0D, 0.0D, 0.0D));
		stooping.setShiftKeyDown(true);
		stooping.setPose(Pose.CROUCHING);
		ServerPlayer standing = challenger(h, velorum, new Vec3(9.5D, 0.0D, 0.0D));
		Vec3 low = BossFootprint.Shapes.at(line, origin, VelorumMove.CUT_ANGLES[0], 9.0D);
		ServerPlayer jumper = challenger(h, velorum, low.subtract(origin).add(0.0D, 1.0D, 0.0D));
		try {
			h.assertTrue(stooping.getBbHeight() < VelorumMove.HIGH_CUT_BOTTOM, "a sneak stands 1.5");
			tick(velorum, 2);
			float stoopHealth = stooping.getHealth(), standHealth = standing.getHealth(), jumpHealth = jumper.getHealth();
			start(velorum, VelorumMove.GLOAM_CUTS);
			BossMoveTimeline timeline = VelorumMove.GLOAM_CUTS.timeline(SaintMoveInputs.NONE);
			tickWithHazards(h, velorum, timeline.windup() + timeline.active() + 2);
			h.assertTrue(stooping.getHealth() >= stoopHealth, "the high cut passed over the stooping one");
			h.assertTrue(jumper.getHealth() >= jumpHealth, "the low cut passed under the jumper");
			h.assertTrue(standing.getHealth() < standHealth, "the high cut struck the one standing");
			h.succeed();
		} finally {
			velorum.discard();
			stooping.discard();
			standing.discard();
			jumper.discard();
			sweepHazards(h);
		}
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 120, batch = "velorum_fight_eclipse_relit")
	public static void eclipseRelitByProjectionOpensTheGutter(GameTestHelper h) {
		VelorumEntity velorum = velorum(h);
		ServerPlayer player = bleeding(challenger(h, velorum, new Vec3(3.0D, 0.0D, 0.0D)));
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		try {
			tick(velorum, 2);
			start(velorum, VelorumMove.ECLIPSE);
			tickWithHazards(h, velorum, VelorumMove.ECLIPSE.timeline(SaintMoveInputs.NONE).windup() + 1);
			h.assertTrue(velorum.eclipseHolding(), "the lantern is out and the hold has begun");
			Vec3 body = velorum.getBoundingBox().getCenter();
			for (int i = 0; i < VelorumEclipseRules.RELIGHT_TICKS + 1 && !velorum.eclipseRelit(); i++) {
				player.lookAt(EntityAnchorArgument.Anchor.EYES, body);
				SaintChannelRouter.tryProject(h.getLevel(), player, 8.0D, 5.0D, false);
				tickWithHazards(h, velorum, 1);
			}
			h.assertTrue(velorum.eclipseRelit(), "twenty fed ticks relight his lantern");
			h.assertTrue(volume.getBloodVolume() <= 1000 - VelorumEclipseRules.RELIGHT_TICKS * VelorumEclipseRules.KINDLE_RATE + 1.0D,
					"paid at 3 a tick through velorum_kindling");
			tickWithHazards(h, velorum, 2);
			h.assertTrue(velorum.openings().current() != null
					&& velorum.openings().current().id() == VelorumOpeningRules.GUTTER_ID
					&& velorum.openings().length() >= VelorumEclipseRules.RELIT_GUTTER, "relit, the Gutter opens at 60");
			h.succeed();
		} finally {
			velorum.discard();
			player.discard();
			sweepHazards(h);
		}
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 300, batch = "velorum_fight_eclipse_unbroken")
	public static void eclipseUnbrokenPreservesAtSixty(GameTestHelper h) {
		VelorumEntity velorum = velorum(h);
		ServerPlayer cold = challenger(h, velorum, new Vec3(4.0D, 0.0D, 0.0D));
		ServerPlayer warm = challenger(h, velorum, new Vec3(-4.0D, 0.0D, 0.0D));
		try {
			tick(velorum, 2);
			start(velorum, VelorumMove.ECLIPSE);
			BossMoveTimeline timeline = VelorumMove.ECLIPSE.timeline(SaintMoveInputs.NONE);
			int last = timeline.windup() + timeline.active() - 1;
			// both keep walking through the hold, so neither meter climbs; then one is left cold
			for (int i = 0; i < last; i++) {
				pace(cold, i);
				pace(warm, i);
				tickWithHazards(h, velorum, 1);
			}
			velorum.meter().add(cold, 65.0F - stillness(velorum, cold));
			velorum.meter().add(warm, -stillness(velorum, warm));
			float warmHealth = warm.getHealth();
			tickWithHazards(h, velorum, 2);
			h.assertTrue(!velorum.eclipseHolding() && !velorum.eclipseRelit(), "the hold ran out unbroken");
			h.assertTrue(velorum.stillness().preserved(cold), "at 60 or more the unbroken Eclipse preserves");
			h.assertTrue(warm.getHealth() < warmHealth, "the rest take 6");
			tickWithHazards(h, velorum, 2);
			h.assertTrue(velorum.openings().current() != null
					&& velorum.openings().current().id() == VelorumOpeningRules.GUTTER_ID
					&& velorum.openings().length() < VelorumEclipseRules.RELIT_GUTTER, "and the Gutter follows at half length");
			h.succeed();
		} finally {
			velorum.discard();
			cold.discard();
			warm.discard();
			sweepHazards(h);
		}
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 300, batch = "velorum_fight_eclipse_lights")
	public static void eclipseRestoresEveryLantern(GameTestHelper h) {
		BlockPos free = h.absolutePos(new BlockPos(6, 4, 6));
		BlockPos post = h.absolutePos(new BlockPos(8, 3, 6));
		BlockPos votive = post.above();
		h.getLevel().setBlock(free, BlockInit.rime_lantern.get().defaultBlockState(), Block.UPDATE_CLIENTS);
		h.getLevel().setBlock(post, Blocks.COBBLESTONE_WALL.defaultBlockState(), Block.UPDATE_CLIENTS);
		h.getLevel().setBlock(votive, BlockInit.rime_lantern.get().defaultBlockState(), Block.UPDATE_CLIENTS);
		VelorumEntity velorum = velorum(h);
		try {
			tick(velorum, 1);
			start(velorum, VelorumMove.ECLIPSE);
			tick(velorum, 2);
			h.assertTrue(h.getLevel().getBlockState(free).is(Blocks.PACKED_ICE)
					&& h.getLevel().getBlockState(votive).is(Blocks.PACKED_ICE),
					"every lantern goes out, the votive stand's too");
			BossMoveTimeline timeline = VelorumMove.ECLIPSE.timeline(SaintMoveInputs.NONE);
			tickWithHazards(h, velorum, timeline.windup() + timeline.active() + 2);
			h.assertTrue(h.getLevel().getBlockState(free).is(BlockInit.rime_lantern.get())
					&& h.getLevel().getBlockState(votive).is(BlockInit.rime_lantern.get()), "and every one comes back");
			h.succeed();
		} finally {
			velorum.discard();
			h.getLevel().setBlock(free, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
			h.getLevel().setBlock(votive, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
			h.getLevel().setBlock(post, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
			sweepHazards(h);
		}
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 40, batch = "velorum_fight_scaling")
	public static void moreChallengersScaleHisHealthNotTheirMeters(GameTestHelper h) {
		VelorumEntity velorum = velorum(h);
		ServerPlayer a = challenger(h, velorum, new Vec3(12.0D, 0.0D, 0.0D));
		ServerPlayer b = challenger(h, velorum, new Vec3(-12.0D, 0.0D, 0.0D));
		try {
			tick(velorum, 2);
			h.assertTrue(Math.abs(velorum.getMaxHealth() - VelorumEntity.MAX_HEALTH * 1.5F) < 0.5F,
					"two challengers: 1 + 0.5 x (n - 1) of his health");
			h.assertTrue(velorum.meter().playerMax() == VelorumStillnessRules.MAX, "per-player meters never scale");
			h.succeed();
		} finally {
			velorum.discard();
			a.discard();
			b.discard();
		}
	}
}
