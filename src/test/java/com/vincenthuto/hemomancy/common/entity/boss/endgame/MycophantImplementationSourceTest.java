package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MycophantImplementationSourceTest {
	private static final Path SOURCE = Path.of("src/main/java/com/vincenthuto/hemomancy");

	@Test
	void entityIsControllerDrivenSyncedAndNeverFrozen() throws Exception {
		String entity = read("common/entity/boss/endgame/MycophantEntity.java");
		String moves = read("common/entity/boss/endgame/MycophantMoves.java");
		assertFalse(entity.contains("setNoAi"));
		assertFalse(entity.contains("ParticleTypes."));
		assertFalse(moves.contains("ParticleTypes."));
		assertFalse(entity.contains("mycophant_tendril"));
		assertFalse(entity.contains("new MeleeAttackGoal"));
		assertTrue(entity.contains("DATA_STATE = SynchedEntityData.defineId"));
		assertTrue(entity.contains("DATA_CLAIMED_ID = SynchedEntityData.defineId"));
		assertTrue(entity.contains("new MycophantController(this)"));
		assertTrue(entity.contains("MycophantSaveRules.resume("));
	}

	@Test
	void legacyPatternIsGoneAndCastsGoThroughTheEncounter() throws Exception {
		String actions = read("common/entity/boss/endgame/EndgameBossActions.java");
		assertFalse(actions.contains("tickMycophantPattern"));
		assertFalse(actions.contains("tickMycophantClientParticles"));
		assertFalse(actions.contains("tickMycophantDeathParticles"));
		assertTrue(read("common/capability/player/harbinger/manip/KnownManipulationEvents.java")
				.contains("MycophantEncounterManager.onCast(player)"));
		assertTrue(read("common/item/harbinger/morphlings/MorphlingItem.java")
				.contains("MycophantEncounterManager.onCast(serverPlayer)"));
	}

	@Test
	void encounterCleansUpTheBroodAndKeepsTheRewardOrder() throws Exception {
		String manager = read("common/encounter/MycophantEncounterManager.java");
		int clear = manager.indexOf("private static void clear(ServerPlayer player) {");
		assertTrue(clear > 0);
		assertTrue(manager.indexOf("discardBrood(player);", clear) > clear);
		int victory = manager.indexOf("public static void completeVictory(");
		int cleared = manager.indexOf("clear(owner);", victory);
		int exited = manager.indexOf("exitChamber(owner);", cleared);
		int delivered = manager.indexOf("givePendingTendril(owner);", exited);
		assertTrue(victory < cleared && cleared < exited && exited < delivered);
		assertTrue(manager.contains("ADV_HIVE_HEIR"));
		assertTrue(manager.contains("new PacketMycophantIntro("));
		// the stalking and the capture are hers: no elder-guardian flash and no vanilla Darkness or Slowness
		assertFalse(manager.contains("GUARDIAN_ELDER_EFFECT"));
		assertFalse(manager.contains("MobEffects.DARKNESS"));
		assertFalse(manager.contains("MobEffects.MOVEMENT_SLOWDOWN"));
		assertTrue(manager.contains("MycophantNurseryLayout.at("));
	}

	@Test
	void oldSavesResumeIdleWithThePhaseFromHealth() {
		// a pre-rebuild save: no intro key, cocoon keys ignored, already past half health
		MycophantSaveRules.Resume old = MycophantSaveRules.resume(false, false, false, 300.0F, 720.0F);
		assertTrue(old.introDone());
		assertTrue(old.splitDone());
		assertEquals(2, old.phase());
		// a fresh boss never loaded before plays its intro
		MycophantSaveRules.Resume fresh = MycophantSaveRules.resume(false, true, false, 720.0F, 720.0F);
		assertFalse(fresh.introDone());
		assertFalse(fresh.splitDone());
		assertEquals(1, fresh.phase());
		// the split flag survives even if healing took her back above half
		MycophantSaveRules.Resume healed = MycophantSaveRules.resume(true, true, true, 500.0F, 720.0F);
		assertTrue(healed.splitDone());
		assertEquals(2, healed.phase());
	}

	private static String read(String relative) throws Exception {
		return Files.readString(SOURCE.resolve(relative));
	}

	@Test
	void reviewFixesStayWired() throws Exception {
		String entity = read("common/entity/boss/endgame/MycophantEntity.java");
		String moves = read("common/entity/boss/endgame/MycophantMoves.java");
		String controller = read("common/entity/boss/endgame/MycophantController.java");
		// the split releases a hold and ends the embrace instead of letting her catch again
		int split = entity.indexOf("controller.requestSplit();");
		assertTrue(split > 0 && entity.lastIndexOf("controller.skipToRecovery();", split) > split - 120);
		assertTrue(moves.contains("MycophantControllerRules.mayLunge("));
		assertTrue(moves.contains("MycophantControllerRules.mayCatch("));
		assertTrue(controller.contains("MycophantControllerRules.shouldCommit("));
		// meter and siphon damage cannot be scaled past the nonlethal clamp; the fusion bonus dodges the hurt cooldown
		assertTrue(entity.contains("HemoDamageTypes.mycophantClaim("));
		assertTrue(entity.contains("HemoDamageTypes.mycophantRefusal("));
		assertTrue(entity.contains("MycophantClaimRules.drownClampActive("));
		// a manipulation counts when it lands, even before its cast is recorded
		assertTrue(entity.contains("isManipulationHit(source)"));
		// strikes never push a player the Nursery has already expelled
		assertTrue(moves.contains("quarry.isAlive() && quarry.level() == boss.level()"));
		assertFalse(moves.contains("bodyPoint(2.2D, 3.4D, 2.0D)"));
		assertTrue(moves.contains("MycophantVisualRules.siphonMouth()"));
		assertTrue(read("client/render/world/MycophantSiphonTetherGeometry.java").contains("MycophantVisualRules.siphonMouth()"));
		assertTrue(read("client/sound/EndgameBossMusicHandler.java").contains("mycophant.isMusicSilent()"));
	}

	@Test
	void aHeldPlayerNeverBuildsUpAFall() throws Exception {
		// suspended at the hold point and re-placed every tick, the player's fall distance would grow for the whole
		// hold and kill them on release, even after the nonlethal meter left them on the floor
		String entity = read("common/entity/boss/endgame/MycophantEntity.java");
		int tick = entity.indexOf("private void tickHold()");
		assertTrue(tick > 0 && entity.indexOf("held.resetFallDistance();", tick) > tick
				&& entity.indexOf("held.resetFallDistance();", tick) < entity.indexOf("private", tick + 10));
		int release = entity.indexOf("public void releaseHold()");
		assertTrue(release > 0 && entity.indexOf("resetFallDistance()", release) < entity.indexOf("\n\t}", release));
	}

	@Test
	void sheWadesThroughHerNectarInsteadOfFloatingInIt() throws Exception {
		// floating in the 1-deep pool she never touches ground, so step-up never fires and the rim traps her there
		String entity = read("common/entity/boss/endgame/MycophantEntity.java");
		int affected = entity.indexOf("public boolean isAffectedByFluids()");
		int pushed = entity.indexOf("public boolean isPushedByFluid()");
		assertTrue(affected > 0 && entity.indexOf("return false;", affected) < entity.indexOf("}", affected));
		assertTrue(pushed > 0 && entity.indexOf("return false;", pushed) < entity.indexOf("}", pushed));
	}

	@Test
	void sheCullsOnHerBodyAndTheWakingDrawsInOneBatch() throws Exception {
		// claws, crest and opened wings reach well past the 4.5 x 5 hitbox; culling on the hitbox pops her at screen edges
		assertTrue(read("common/entity/boss/endgame/MycophantEntity.java").contains("public AABB getBoundingBoxForCulling()"));
		// thousands of fiber fills per frame must share one flush, not one draw call each
		String overlay = read("client/event/MycophantIntroClientEvents.java");
		int render = overlay.indexOf("public static void renderOverlay(");
		assertTrue(render > 0 && overlay.indexOf("graphics.drawManaged(", render) > render);
	}
}
