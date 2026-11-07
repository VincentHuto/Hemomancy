package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.block.harbinger.decoration.RotPyreBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.SporeShelfBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintChannelRouter;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintCombatController;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintOpening;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintRoot;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielColonisationRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielFaithfulEvents;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielKindlingRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielOpenings;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielPhase;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielStage;
import com.vincenthuto.hemomancy.common.entity.boss.BossGroundHazardEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielHazard;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielSludgeRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielSludgeRules.Outcome;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielSludgeRules.Patch;
import com.vincenthuto.hemomancy.common.entity.mob.monster.BogRevenantEntity;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/**
 * Putriciel's fight with real entities (design section 16): the rise takes the room, the rot never burns, the Kindling
 * fills from hits and fire, the interval waits for the recovery boundary, an Offering takes one hit or one poured
 * projection and nothing absolves outside one, the fifth absolution runs the ending and {@code endTrial} once (and a
 * reload resumes it), colonisation's thresholds apply and clear, the heat cleanses, the deeds, the scaling, and the
 * arena's restore. A Putriciel here has no tomb, so its arena is the fallback box round it. Each test runs in its own
 * batch: the fallback box is wider than a test's plot.
 */
@GameTestHolder("putriciel_fight_validation")
@PrefixGameTestTemplate(false)
public final class PutricielFightGameTests {
	private static final String TEMPLATE = "empty";
	private static final BlockPos SAINT_AT = new BlockPos(8, 2, 8);
	private static final int LONG = 1200;

	private PutricielFightGameTests() {
	}

	private static void clearStage(GameTestHelper h) {
		for (BlockPos pos : BlockPos.betweenClosed(h.absolutePos(new BlockPos(1, 1, 1)), h.absolutePos(new BlockPos(15, 6, 15)))) {
			h.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
		}
		for (BlockPos pos : BlockPos.betweenClosed(h.absolutePos(new BlockPos(1, 1, 1)), h.absolutePos(new BlockPos(15, 1, 15)))) {
			h.getLevel().setBlock(pos, Blocks.MOSS_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
		}
	}

	private static PutricielEntity saint(GameTestHelper h) {
		clearStage(h);
		PutricielEntity saint = EntityInit.putriciel.get().create(h.getLevel());
		saint.setNoGravity(true);
		saint.moveTo(h.absoluteVec(Vec3.atBottomCenterOf(SAINT_AT)));
		h.getLevel().addFreshEntity(saint);
		return saint;
	}

	private static ServerPlayer player(GameTestHelper h, double x, double z) {
		ServerPlayer player = DuctilisGameTests.player(h);
		player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(400.0D);
		player.setHealth(400.0F);
		player.teleportTo(h.absoluteVec(new Vec3(x, 2.0D, z)).x, h.absoluteVec(new Vec3(x, 2.0D, z)).y,
				h.absoluteVec(new Vec3(x, 2.0D, z)).z);
		return player;
	}

	private static void tick(PutricielEntity saint, int ticks) {
		for (int i = 0; i < ticks && !saint.isRemoved(); i++) saint.aiStep();
	}

	private static boolean tickUntil(PutricielEntity saint, int limit, java.util.function.BooleanSupplier done) {
		for (int i = 0; i < limit && !saint.isRemoved(); i++) {
			if (done.getAsBoolean()) return true;
			saint.aiStep();
		}
		return done.getAsBoolean();
	}

	private static boolean offeringOpen(PutricielEntity saint) {
		SaintOpening.Spec open = saint.openings().current();
		return open != null && open.id() == PutricielOpenings.OFFERING_ID;
	}

	/** Fills the Kindling and runs the Ignition through to the fire proper. */
	private static boolean toTheFire(PutricielEntity saint) {
		tick(saint, 1);
		saint.meter().set(saint.meter().max());
		return tickUntil(saint, 300, () -> saint.stage() == PutricielStage.BURNING);
	}

	/** Burns the Kindling past 66% and waits for the palm. */
	private static boolean toAnOffering(PutricielEntity saint) {
		if (!toTheFire(saint)) return false;
		saint.meter().set(saint.meter().max() * 0.6F);
		return tickUntil(saint, 200, () -> offeringOpen(saint));
	}

	private static void strike(PutricielEntity saint, ServerPlayer player, float amount) {
		saint.invulnerableTime = 0;
		saint.hurt(saint.damageSources().playerAttack(player), amount);
	}

	private static void sweep(GameTestHelper h) {
		AABB stage = new AABB(h.absolutePos(BlockPos.ZERO)).inflate(32.0D);
		h.getLevel().getEntitiesOfClass(ItemEntity.class, stage).forEach(Entity::discard);
	}

	// ── the rise and the rot ─────────────────────────────────────────

	@GameTest(template = TEMPLATE, timeoutTicks = 100, batch = "putriciel_fight_rise")
	public static void riseTakesTheRoomAndRestoresItOnRemoval(GameTestHelper h) {
		PutricielEntity saint = saint(h);
		BlockPos pyre = h.absolutePos(new BlockPos(3, 2, 3));
		BlockPos shelf = h.absolutePos(new BlockPos(13, 3, 3));
		h.getLevel().setBlock(pyre, BlockInit.rot_pyre.get().defaultBlockState().setValue(RotPyreBlock.LIT, true), Block.UPDATE_CLIENTS);
		h.getLevel().setBlock(shelf, BlockInit.spore_shelf.get().defaultBlockState().setValue(SporeShelfBlock.BLOOM, false), Block.UPDATE_CLIENTS);
		tick(saint, 2);
		h.assertTrue(!h.getLevel().getBlockState(pyre).getValue(RotPyreBlock.LIT), "the rot puts the pyre out");
		h.assertTrue(h.getLevel().getBlockState(shelf).getValue(SporeShelfBlock.BLOOM), "and blooms the shelf");
		saint.discard();
		h.assertTrue(h.getLevel().getBlockState(pyre).getValue(RotPyreBlock.LIT), "removal restores the pyre as built");
		h.assertTrue(!h.getLevel().getBlockState(shelf).getValue(SporeShelfBlock.BLOOM), "and the shelf");
		h.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 200, batch = "putriciel_fight_rot")
	public static void rotPhasesNeverBurn(GameTestHelper h) {
		PutricielEntity saint = saint(h);
		ServerPlayer player = player(h, 10.5D, 8.5D);
		tick(saint, 1);
		for (PutricielMove move : PutricielMove.values()) {
			if (move.fire()) continue;
			saint.meter().set(0.0F);
			saint.controller().start(move, SaintCombatController.Cut.IMMEDIATE);
			tick(saint, 120);
			h.assertTrue(player.getRemainingFireTicks() <= 0, move + " set the challenger alight in the rot");
			h.assertTrue(saint.stage() == PutricielStage.ROT || saint.stage() == PutricielStage.IGNITING, "still the rot");
			for (Entity owned : saint.arena().owned()) {
				if (owned instanceof BossGroundHazardEntity hazard) {
					h.assertTrue(hazard.kind() != PutricielHazard.SLUDGE_FIRE.id() && hazard.kind() != PutricielHazard.FIRE_RUNNER.id(),
							move + " burned pooled rot in the rot");
				}
			}
			if (saint.stage() == PutricielStage.ROT) h.assertTrue(saint.catchClock() < 0, move + " started a catch in the rot");
			player.setHealth(400.0F);
		}
		player.discard();
		saint.discard();
		h.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 100, batch = "putriciel_fight_kindling")
	public static void kindlingFillsFromHitsAndFire(GameTestHelper h) {
		PutricielEntity saint = saint(h);
		ServerPlayer player = player(h, 10.5D, 8.5D);
		tick(saint, 1);
		saint.meter().set(0.0F);
		strike(saint, player, 10.0F);
		float afterHit = saint.meter().value();
		h.assertTrue(Math.abs(afterHit - 2.0F) < 0.3F, "one point per five raw damage: " + afterHit);
		saint.invulnerableTime = 0;
		saint.hurt(saint.damageSources().onFire(), 10.0F);
		h.assertTrue(saint.meter().value() - afterHit > 4.5F, "fire is drunk at x2.5");
		h.assertTrue(saint.getHealth() < saint.getMaxHealth(), "a blow still lands");
		player.discard();
		saint.discard();
		h.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 200, batch = "putriciel_fight_boundary")
	public static void theIntervalWaitsForTheRecoveryBoundary(GameTestHelper h) {
		PutricielEntity saint = saint(h);
		tick(saint, 1);
		saint.controller().start(PutricielMove.SAC_SWELL, SaintCombatController.Cut.IMMEDIATE);
		tick(saint, 3);
		saint.meter().set(saint.meter().max());
		tick(saint, 5);
		h.assertTrue(saint.stage() == PutricielStage.IGNITING, "the fire is asked for");
		h.assertTrue(saint.phases().phase() == PutricielPhase.ROT_I.number(), "but the swell's wind-up is not cut");
		boolean fire = tickUntil(saint, 200, () -> saint.controller().move() == PutricielMove.IGNITION);
		h.assertTrue(fire && saint.phases().phase() == PutricielPhase.INTERVAL.number(), "the Ignition follows at the boundary");
		saint.discard();
		h.succeed();
	}

	// ── absolution ───────────────────────────────────────────────────

	@GameTest(template = TEMPLATE, timeoutTicks = LONG, batch = "putriciel_fight_offering_hit")
	public static void anOfferingAcceptsOneHitAndNothingAbsolvesOutsideOne(GameTestHelper h) {
		PutricielEntity saint = saint(h);
		ServerPlayer player = player(h, 10.5D, 8.5D);
		tick(saint, 1);
		strike(saint, player, 12.0F);
		h.assertTrue(saint.absolutions() == 0, "no absolution in the rot");
		h.assertTrue(toAnOffering(saint), "the palm opens at 66%");
		strike(saint, player, 3.0F);
		h.assertTrue(saint.absolutions() == 0 && offeringOpen(saint), "a blow under 4 does not absolve");
		strike(saint, player, 6.0F);
		h.assertTrue(saint.absolutions() == 1, "one hit absolves");
		h.assertTrue(!offeringOpen(saint), "and closes the Offering");
		strike(saint, player, 6.0F);
		h.assertTrue(saint.absolutions() == 1, "an Offering cannot be taken twice");
		player.discard();
		saint.discard();
		h.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = LONG, batch = "putriciel_fight_offering_pour")
	public static void anOfferingAcceptsHeldProjection(GameTestHelper h) {
		PutricielEntity saint = saint(h);
		ServerPlayer player = player(h, 8.5D, 11.0D);
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setMaxBloodVolume(1000.0D);
		volume.setBloodVolume(1000.0D);
		h.assertTrue(toAnOffering(saint), "the palm opens");
		saint.meter().secondary(player, 15.0F, PutricielColonisationRules.MAX);
		double before = volume.getBloodVolume();
		for (int i = 0; i < 30 && saint.absolutions() == 0; i++) {
			Vec3 palm = saint.palmPoint();
			player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, palm);
			SaintChannelRouter.tryProject(h.getLevel(), player, 6.0D, 5.0D, true);
		}
		h.assertTrue(saint.absolutions() == 1, "a hundred poured into the palm absolves");
		h.assertTrue(before - volume.getBloodVolume() >= 99.0D, "through BloodAccounting with the Offering's source");
		h.assertTrue(saint.colonisation(player) <= 5.5F, "the offered blood cleanses ten colonisation");
		player.discard();
		saint.discard();
		h.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = LONG, batch = "putriciel_fight_ending")
	public static void theFifthAbsolutionRunsTheEndingAndEndTrialOnceAndAReloadResumesIt(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		PutricielEntity first = saint(h);
		tick(first, 1);
		CompoundTag saved = new CompoundTag();
		first.saveWithoutId(saved);
		saved.getCompound("Putriciel").putInt("absolutions", 4);
		first.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
		PutricielEntity saint = EntityInit.putriciel.get().create(level);
		saint.load(saved);
		saint.setNoGravity(true);
		level.addFreshEntity(saint);
		ServerPlayer player = player(h, 10.5D, 8.5D);
		h.assertTrue(toAnOffering(saint) || saint.controller().move() == PutricielMove.FULL_PURGE, "the fifth comes after the Full Purge");
		tickUntil(saint, 400, () -> offeringOpen(saint));
		strike(saint, player, 8.0F);
		h.assertTrue(saint.absolutions() == 5 && saint.ending(), "the fifth absolution begins the ending");
		tick(saint, 50);
		CompoundTag midEnding = new CompoundTag();
		saint.saveWithoutId(midEnding);
		saint.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
		PutricielEntity resumed = EntityInit.putriciel.get().create(level);
		resumed.load(midEnding);
		resumed.setNoGravity(true);
		level.addFreshEntity(resumed);
		tick(resumed, 3);
		h.assertTrue(!resumed.isRemoved() && !resumed.hasTrialEnded() && resumed.ending(), "a reloaded ending resumes");
		tick(resumed, 80);
		h.assertTrue(resumed.isRemoved() && resumed.hasTrialEnded(), "endTrial ran once and the Saint is gone");
		player.discard();
		sweep(h);
		h.succeed();
	}

	// ── colonisation ─────────────────────────────────────────────────

	@GameTest(template = TEMPLATE, timeoutTicks = 200, batch = "putriciel_fight_colonisation")
	public static void colonisationThresholdsApplyAndClearOnLeaving(GameTestHelper h) {
		PutricielEntity saint = saint(h);
		ServerPlayer player = player(h, 10.5D, 8.5D);
		tick(saint, 1);
		saint.meter().secondary(player, 20.0F, PutricielColonisationRules.MAX);
		tick(saint, 1);
		h.assertTrue(Math.abs(saint.colonisation(player) - PutricielColonisationRules.OVERGROWN_FALL_TO) < 0.6F, "Overgrown falls to 12");
		h.assertTrue(SaintRoot.rooted(player), "Overgrown roots");
		h.assertTrue(player.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(PutricielEntity.ROOTED_KEY) != null, "Rooted slows");
		player.teleportTo(player.getX() + 60.0D, player.getY(), player.getZ());
		// the arena reads its challengers once a game tick, so the leaving is seen on a later server tick
		h.runAfterDelay(3, () -> {
			h.assertTrue(player.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(PutricielEntity.ROOTED_KEY) == null,
					"leaving the chamber clears what colonisation applied");
			player.discard();
			saint.discard();
			h.succeed();
		});
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 600, batch = "putriciel_fight_heat")
	public static void thePurgingHeatCleansesColonisation(GameTestHelper h) {
		PutricielEntity saint = saint(h);
		ServerPlayer player = player(h, 10.0D, 8.5D);
		h.assertTrue(toTheFire(saint), "the fire burns");
		saint.meter().secondary(player, 12.0F, PutricielColonisationRules.MAX);
		tick(saint, 25);
		h.assertTrue(saint.colonisation(player) < 12.0F, "standing in the heat burns it off");
		player.discard();
		saint.discard();
		h.succeed();
	}

	// ── deeds and scaling ────────────────────────────────────────────

	@GameTest(template = TEMPLATE, timeoutTicks = LONG, batch = "putriciel_fight_litany")
	public static void theLitanyGlyphRefusesTheWrongAct(GameTestHelper h) {
		PutricielEntity saint = saint(h);
		saint.setAwakening("FORCED", List.of("LITANY"));
		ServerPlayer player = player(h, 10.5D, 8.5D);
		h.assertTrue(toAnOffering(saint), "the palm opens");
		var glyph = saint.glyph();
		h.assertTrue(glyph != com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielAbsolutionRules.Glyph.NONE,
				"the Litany deed names an act");
		if (glyph == com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielAbsolutionRules.Glyph.POUR) {
			strike(saint, player, 8.0F);
			h.assertTrue(saint.absolutions() == 0, "a blow does nothing when the glyph names blood");
		} else {
			strike(saint, player, 8.0F);
			h.assertTrue(saint.absolutions() == 1, "a blow absolves when the glyph names a blow");
		}
		player.discard();
		saint.discard();
		h.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 300, batch = "putriciel_fight_downfall")
	public static void theDownfallRaisesTheFaithfulFromTheBog(GameTestHelper h) {
		PutricielEntity saint = saint(h);
		saint.setAwakening("FORCED", List.of("DOWNFALL"));
		BlockPos body = h.absolutePos(new BlockPos(12, 2, 12));
		h.getLevel().setBlock(body, BlockInit.bog_body.get().defaultBlockState(), Block.UPDATE_CLIENTS);
		ServerPlayer player = player(h, 12.5D, 10.5D);
		tick(saint, 1);
		saint.controller().start(PutricielMove.ROOT_SNARE, SaintCombatController.Cut.IMMEDIATE);
		tick(saint, 40);
		var risen = h.getLevel().getEntitiesOfClass(BogRevenantEntity.class, new AABB(body).inflate(20.0D),
				revenant -> revenant.getTags().contains(PutricielFaithfulEvents.TAG));
		h.assertTrue(!risen.isEmpty(), "a faithful rose");
		h.assertTrue(!h.getLevel().getBlockState(body).is(BlockInit.bog_body.get()), "from its bog body");
		saint.discard();
		h.assertTrue(h.getLevel().getBlockState(body).is(BlockInit.bog_body.get()), "the bog body comes back with the trial");
		h.assertTrue(risen.get(0).isRemoved(), "the faithful goes with it");
		player.discard();
		h.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = LONG, batch = "putriciel_fight_repose")
	public static void theReposeDeedSkipsTheEmbering(GameTestHelper h) {
		PutricielEntity saint = saint(h);
		saint.setAwakening("FORCED", List.of("REPOSE"));
		h.assertTrue(toTheFire(saint), "the fire burns");
		saint.meter().set(0.0F);
		boolean back = tickUntil(saint, 800, () -> saint.stage() == PutricielStage.ROT);
		h.assertTrue(back, "the fire ends");
		h.assertTrue(saint.controller().move() != PutricielMove.EMBERING, "with no Embering");
		h.assertTrue(saint.meter().fraction() >= PutricielKindlingRules.REPOSE_HEAD_START - 0.01F, "and the head start instead");
		h.assertTrue(saint.intervalsEnded() == 1, "the interval counted");
		saint.discard();
		h.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 100, batch = "putriciel_fight_scaling")
	public static void twoChallengersScaleTheKindling(GameTestHelper h) {
		PutricielEntity saint = saint(h);
		ServerPlayer one = player(h, 10.5D, 8.5D);
		ServerPlayer two = player(h, 6.5D, 8.5D);
		tick(saint, 2);
		h.assertTrue(Math.abs(saint.meter().max() - PutricielKindlingRules.max(2)) < 0.01F, "the maximum scales by 1.5");
		h.assertTrue(saint.getMaxHealth() > 590.0F, "and health by the kit's rule");
		one.discard();
		two.discard();
		saint.discard();
		h.succeed();
	}

	// ── the rot sludge (revision 2026-11-06, R2) ─────────────────────

	/** Advances the Saint and every hazard it owns one game tick each, as the level would (their ages included). */
	private static void run(PutricielEntity saint, int ticks) {
		for (int i = 0; i < ticks && !saint.isRemoved(); i++) {
			saint.tickCount++;
			saint.aiStep();
			if (saint.isRemoved() || saint.arena() == null) continue;
			for (Entity owned : saint.arena().owned()) {
				if (owned instanceof BossGroundHazardEntity hazard && !hazard.isRemoved()) {
					hazard.tickCount++;
					hazard.tick();
				}
			}
		}
	}

	private static List<BossGroundHazardEntity> owned(PutricielEntity saint, PutricielHazard kind) {
		List<BossGroundHazardEntity> found = new java.util.ArrayList<>();
		if (saint.arena() == null) return found;
		for (Entity entity : saint.arena().owned()) {
			if (entity instanceof BossGroundHazardEntity hazard && hazard.kind() == kind.id()) found.add(hazard);
		}
		return found;
	}

	private static boolean ash(GameTestHelper h, int x, int z) {
		return h.getLevel().getBlockState(h.absolutePos(new BlockPos(x, 1, z))).is(BlockInit.packed_ash.get());
	}

	private static boolean slowed(ServerPlayer player) {
		return player.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(PutricielEntity.SLUDGE_KEY) != null;
	}

	/** Lays a pool three blocks from the body, lights the fire and runs the burn through to its ash. */
	private static boolean burnAPoolToAsh(GameTestHelper h, PutricielEntity saint, double x, double z) {
		tick(saint, 1);
		if (saint.laySludge(Patch.disc(h.absoluteVec(new Vec3(x, 2.0D, z)), 1.5D)) != Outcome.LAY) return false;
		BossGroundHazardEntity pool = saint.sludgePools().get(0);
		if (!toTheFire(saint) || !pool.fading()) return false;
		run(saint, PutricielSludgeRules.BURN);
		return ash(h, (int) Math.floor(x), (int) Math.floor(z));
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 200, batch = "putriciel_fight_sludge_wading")
	public static void sludgeSlowsAndColonisesAndClearsOnLeave(GameTestHelper h) {
		PutricielEntity saint = saint(h);
		ServerPlayer player = player(h, 2.5D, 2.5D);
		tick(saint, 1);
		// no move picked: only the pool touches the player
		saint.controller().delay(100_000);
		Outcome laid = saint.laySludge(Patch.disc(h.absoluteVec(new Vec3(2.5D, 2.0D, 2.5D)), 1.5D));
		h.assertTrue(laid == Outcome.LAY, "a pool lies on open moss: " + laid);
		h.assertTrue(saint.sludgePools().size() == 1, "one live pool");
		run(saint, 1);
		h.assertTrue(slowed(player), "wading slows");
		var modifier = player.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(PutricielEntity.SLUDGE_KEY);
		h.assertTrue(Math.abs(modifier.amount() - PutricielSludgeRules.SLOW) < 1.0E-6D, "x0.65");
		run(saint, 20);
		h.assertTrue(saint.colonisation(player) >= 1.0F, "wading colonises: " + saint.colonisation(player));
		h.assertTrue(player.getHealth() >= 400.0F, "the sludge deals no damage");
		player.teleportTo(h.absoluteVec(new Vec3(8.5D, 2.0D, 2.5D)).x, h.absoluteVec(new Vec3(8.5D, 2.0D, 2.5D)).y,
				h.absoluteVec(new Vec3(8.5D, 2.0D, 2.5D)).z);
		run(saint, 3);
		h.assertTrue(slowed(player), "the slow holds a moment after the pool");
		run(saint, PutricielSludgeRules.SLOW_RELEASE);
		h.assertTrue(!slowed(player), "and lets go six ticks out of every pool");
		player.teleportTo(h.absoluteVec(new Vec3(2.5D, 2.0D, 2.5D)).x, h.absoluteVec(new Vec3(2.5D, 2.0D, 2.5D)).y,
				h.absoluteVec(new Vec3(2.5D, 2.0D, 2.5D)).z);
		run(saint, 1);
		h.assertTrue(slowed(player), "back in the pool");
		saint.discard();
		h.assertTrue(!slowed(player), "the trial's end takes the slow with colonisation's other modifiers");
		player.discard();
		h.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 400, batch = "putriciel_fight_sludge_burn")
	public static void sludgeWaitsForTheFireThenBurnsToAsh(GameTestHelper h) {
		PutricielEntity saint = saint(h);
		tick(saint, 1);
		h.assertTrue(saint.laySludge(Patch.disc(h.absoluteVec(new Vec3(11.5D, 2.0D, 8.5D)), 1.5D)) == Outcome.LAY, "the pool lies");
		BossGroundHazardEntity pool = saint.sludgePools().get(0);
		run(saint, 100);
		h.assertTrue(!pool.isRemoved() && !pool.fading() && saint.catchClock() < 0, "the pooled rot waits for the fire");
		h.assertTrue(owned(saint, PutricielHazard.SLUDGE_FIRE).isEmpty(), "nothing burns in the rot");
		h.assertTrue(toTheFire(saint), "the fire burns");
		h.assertTrue(saint.catchClock() >= 0, "the Ignition's commit scheduled the catch");
		h.assertTrue(pool.fading() && pool.fade() == PutricielSludgeRules.BURN, "the ring caught the pool: it burns over 50 ticks");
		h.assertTrue(!pool.isRemoved(), "it does not vanish at once");
		List<BossGroundHazardEntity> fires = owned(saint, PutricielHazard.SLUDGE_FIRE);
		h.assertTrue(fires.size() == 1, "a burning pool takes its place");
		BossGroundHazardEntity fire = fires.get(0);
		h.assertTrue(fire.life() == PutricielSludgeRules.FIRE_LIFE, "50 of burning and 60 of cooling");
		h.assertTrue(fire.frame().origin().distanceTo(pool.frame().origin()) < 1.0E-6D, "on the pool's footprint");
		h.assertTrue(fire.frame().aim().distanceTo(pool.frame().origin()) <= 1.5D + 1.0E-3D, "aimed at the catch point, on or inside its rim");
		run(saint, PutricielSludgeRules.ASH_AT - 2);
		h.assertTrue(!ash(h, 11, 8), "the ash waits for the char");
		run(saint, 6);
		h.assertTrue(ash(h, 11, 8), "the burned pool leaves packed ash");
		h.assertTrue(saint.scorchedCells().contains(com.vincenthuto.hemomancy.common.entity.boss.BossFootprint.cell(
				h.absolutePos(new BlockPos(11, 1, 8)).getX(), h.absolutePos(new BlockPos(11, 1, 8)).getZ())), "fight ash, Bloom-safe");
		run(saint, 4);
		h.assertTrue(pool.isRemoved(), "the sludge has burned away");
		saint.discard();
		h.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 100, batch = "putriciel_fight_sludge_cap")
	public static void sludgeCapSettlesTheOldest(GameTestHelper h) {
		PutricielEntity saint = saint(h);
		tick(saint, 1);
		double[][] spots = {{3, 3}, {6, 3}, {9, 3}, {12, 3}, {3, 13}, {6, 13}, {9, 13}};
		for (int i = 0; i < 6; i++) {
			Outcome laid = saint.laySludge(Patch.disc(h.absoluteVec(new Vec3(spots[i][0], 2.0D, spots[i][1])), 1.0D));
			h.assertTrue(laid == Outcome.LAY, "pool " + i + " lies: " + laid);
		}
		h.assertTrue(saint.sludgePools().size() == 6, "six live pools");
		BossGroundHazardEntity oldest = saint.sludgePools().get(0);
		h.assertTrue(saint.laySludge(Patch.disc(h.absoluteVec(new Vec3(spots[6][0], 2.0D, spots[6][1])), 1.0D)) == Outcome.LAY,
				"the seventh lies");
		h.assertTrue(oldest.fading() && oldest.fade() == PutricielSludgeRules.SETTLE, "the oldest settles into the moss");
		h.assertTrue(saint.sludgePools().size() == 6, "never more than six with one challenger");
		h.assertTrue(owned(saint, PutricielHazard.SLUDGE_FIRE).isEmpty(), "settling is not fire");
		saint.discard();
		h.assertTrue(oldest.isRemoved(), "the pools go with the trial");
		h.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 100, batch = "putriciel_fight_sludge_placement")
	public static void sludgeNeverLiesOnAshOrPyreCircles(GameTestHelper h) {
		PutricielEntity saint = saint(h);
		BlockPos pyre = h.absolutePos(new BlockPos(3, 2, 3));
		h.getLevel().setBlock(pyre, BlockInit.rot_pyre.get().defaultBlockState().setValue(RotPyreBlock.LIT, true), Block.UPDATE_CLIENTS);
		tick(saint, 1);
		Vec3 circle = Vec3.atBottomCenterOf(pyre);
		h.assertTrue(saint.laySludge(Patch.disc(circle, 1.5D)) == Outcome.SKIP, "never in a pyre's circle");
		h.assertTrue(saint.laySludge(Patch.disc(h.absoluteVec(new Vec3(6.5D, 2.0D, 3.5D)), 1.5D)) == Outcome.LAY, "beside it");
		BossGroundHazardEntity beside = saint.sludgePools().get(0);
		double radius = ((com.vincenthuto.hemomancy.common.entity.boss.BossFootprint.Disc) beside.shape()).radius();
		double gap = Math.hypot(beside.anchor().x - circle.x, beside.anchor().z - circle.z) - radius;
		h.assertTrue(gap >= com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielArenaRules.PYRE_SAFE - 1.0E-6D,
				"the circle stays clean: " + gap);
		BlockPos ashAt = h.absolutePos(new BlockPos(12, 1, 12));
		saint.scorch(new it.unimi.dsi.fastutil.longs.LongArrayList(new long[]{
				com.vincenthuto.hemomancy.common.entity.boss.BossFootprint.cell(ashAt.getX(), ashAt.getZ())}));
		h.assertTrue(ash(h, 12, 12), "this fight's ash");
		h.assertTrue(saint.laySludge(Patch.disc(h.absoluteVec(new Vec3(12.5D, 2.0D, 12.5D)), 1.5D)) == Outcome.SKIP,
				"never on the ash");
		h.assertTrue(saint.laySludge(Patch.disc(h.absoluteVec(new Vec3(12.5D, 2.0D, 10.8D)), 1.5D)) == Outcome.LAY,
				"beside it, clipped");
		BossGroundHazardEntity clipped = saint.sludgePools().get(1);
		double clippedRadius = ((com.vincenthuto.hemomancy.common.entity.boss.BossFootprint.Disc) clipped.shape()).radius();
		Vec3 ashCentre = Vec3.atBottomCenterOf(ashAt);
		double toAsh = Math.hypot(clipped.anchor().x - ashCentre.x, clipped.anchor().z - ashCentre.z);
		h.assertTrue(clippedRadius < 1.5D && toAsh - clippedRadius >= PutricielSludgeRules.ASH_GAP - 1.0E-6D,
				"its rim stops short of the ash: " + clippedRadius);
		saint.discard();
		h.assertTrue(!ash(h, 12, 12), "the ash restores with the trial");
		h.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = LONG, batch = "putriciel_fight_sludge_restore")
	public static void sludgeAshRestoresOnEndRemoveAndLoadAfterEnd(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		// the end: a fifth absolution's ending restores the sludge's ash
		PutricielEntity first = saint(h);
		tick(first, 1);
		CompoundTag four = new CompoundTag();
		first.saveWithoutId(four);
		four.getCompound("Putriciel").putInt("absolutions", 4);
		first.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
		PutricielEntity ending = EntityInit.putriciel.get().create(level);
		ending.load(four);
		ending.setNoGravity(true);
		level.addFreshEntity(ending);
		ServerPlayer player = player(h, 10.5D, 8.5D);
		h.assertTrue(burnAPoolToAsh(h, ending, 8.5D, 11.5D), "a pool burned to ash before the end");
		ending.meter().set(ending.meter().max() * 0.6F);
		h.assertTrue(tickUntil(ending, 600, () -> offeringOpen(ending)), "the last Offering opens");
		strike(ending, player, 8.0F);
		h.assertTrue(ending.ending(), "the fifth absolution begins the ending");
		tickUntil(ending, 300, ending::isRemoved);
		h.assertTrue(ending.isRemoved() && ending.hasTrialEnded(), "the trial ended");
		h.assertTrue(!ash(h, 8, 11), "the end restores the sludge's ash");
		player.discard();
		sweep(h);

		// removal: a discard restores it and takes every pool and burning pool with it
		PutricielEntity removed = saint(h);
		h.assertTrue(burnAPoolToAsh(h, removed, 11.5D, 8.5D), "a pool burned to ash");
		List<BossGroundHazardEntity> left = owned(removed, PutricielHazard.SLUDGE_FIRE);
		CompoundTag standing = new CompoundTag();
		removed.saveWithoutId(standing);
		removed.discard();
		h.assertTrue(!ash(h, 11, 8), "removal restores the ash");
		for (BossGroundHazardEntity hazard : left) h.assertTrue(hazard.isRemoved(), "and discards the burning pools");

		// a load after the trial ended restores what the save still records
		BlockPos cell = h.absolutePos(new BlockPos(11, 1, 8));
		level.setBlock(cell, BlockInit.packed_ash.get().defaultBlockState(), Block.UPDATE_CLIENTS);
		standing.putBoolean("SaintTrialEnded", true);
		PutricielEntity loaded = EntityInit.putriciel.get().create(level);
		loaded.load(standing);
		loaded.setNoGravity(true);
		level.addFreshEntity(loaded);
		tick(loaded, 2);
		h.assertTrue(loaded.isRemoved(), "a Saint loaded after its trial ended goes");
		h.assertTrue(!ash(h, 11, 8), "and restores the sludge's ash as it goes");
		sweep(h);
		h.succeed();
	}
}
