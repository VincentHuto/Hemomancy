package com.vincenthuto.hemomancy.common.entity.npc.circus;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CircusPresentationRegressionTest {
	private static String source(String path) throws IOException {
		return Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/" + path));
	}

	@Test
	void stiltWalkerRestoresAuthoredHeightAfterVanillaAnimation() throws IOException {
		String model = source("client/model/entity/npc/CircusStiltWalkerModel.java");
		int setup = model.indexOf("public void setupAnim");
		int superCall = model.indexOf("super.setupAnim", setup);
		assertTrue(model.indexOf("head.y = body.y = -24.0F", superCall) > superCall);
		assertTrue(model.indexOf("rightLeg.y = leftLeg.y = -12.0F", superCall) > superCall);
		assertTrue(model.indexOf("rightArm.y = leftArm.y = -22.0F", superCall) > superCall);
		assertTrue(model.indexOf("rightLeg.xRot *= 0.15F", superCall) > superCall);
		assertTrue(model.indexOf("leftLeg.xRot *= 0.15F", superCall) > superCall);
		assertFalse(model.contains("body.zRot = sway"));
	}

	@Test
	void peacefulKnifeRoutineLaunchesAHarmlessProjectile() throws IOException {
		String source = source("common/entity/npc/circus/CircusKnifeThrowerEntity.java");
		String routine = source.substring(source.indexOf("protected void tickPerformance"),
				source.indexOf("protected void tickDefense"));
		assertTrue(routine.contains("is(Blocks.TARGET)"));
		assertTrue(routine.contains("Vec3.atCenterOf(targetPos)"));
		assertTrue(routine.contains("setYRot(yaw)"));
		assertTrue(routine.contains("setYBodyRot(yaw)"));
		assertTrue(routine.contains("setYHeadRot(yaw)"));
		assertTrue(routine.contains("new CircusKnifeProjectileEntity"));
		assertTrue(routine.contains("knife.setHarmless()"));
		assertTrue(routine.contains("level().addFreshEntity(knife)"));
	}

	@Test
	void perceptionOverlayUsesSmoothEdgesWithoutScanBands() throws IOException {
		String overlay = source("client/screen/overlay/CircusPerceptionOverlay.java");
		String events = source("client/event/ClientEvents.java");
		String performer = source("client/render/entity/npc/CircusPerformerRenderer.java");
		assertTrue(overlay.contains("graphics.fillGradient"));
		assertTrue(overlay.contains("public static void renderWorld"));
		assertTrue(overlay.contains("CircusCarouselEntity.class"));
		assertTrue(overlay.contains("stage.silhouetteCount()"));
		assertTrue(overlay.contains("stage.clothCount()"));
		assertTrue(overlay.contains("stage.lightCount()"));
		assertTrue(events.contains("CircusPerceptionOverlay.renderWorld(event.getPoseStack(), partialTick)"));
		assertTrue(performer.contains("motionEchoAlpha()"));
		assertFalse(overlay.contains("System.currentTimeMillis"));
		assertFalse(overlay.contains("graphics.fill(x, 0, Math.min(width, x + 2), height"));
	}

	@Test
	void acrobatDoesNotRotateItsTorsoAwayFromItsHips() throws IOException {
		String model = source("client/model/entity/npc/CircusAcrobatModel.java");
		assertFalse(model.contains("body.zRot ="));
	}

	@Test
	void carouselPuppeteersSitLowAndGripThePole() throws IOException {
		String carousel = source("common/entity/npc/circus/CircusCarouselEntity.java");
		String model = source("client/model/entity/mob/monster/BloodDrunkPuppeteerModel.java");
		assertTrue(carousel.contains("getY() + 2.55D + pose.bob()"));
		assertTrue(carousel.contains("1.0D + 0.55D / CircusCarouselRules.HORSE_RADIUS"));
		assertTrue(model.contains("if (entity.getCarouselHorse() >= 0)"));
		assertTrue(model.contains("right_arm.xRot = this.left_arm.xRot = -1.35F"));
		assertTrue(model.contains("right_leg.xRot = this.left_leg.xRot = -1.25F"));
		assertTrue(model.contains("right_leg2.xRot = this.left_leg2.xRot = 1.4F"));
	}

	@Test
	void fireEaterFollowsItsPoofWithAnUpwardStream() throws IOException {
		String routine = source("common/entity/npc/circus/CircusFireEaterEntity.java");
		assertTrue(routine.contains("actTick >= 52 && actTick <= 68"));
		assertTrue(routine.contains("new Vec3(look.x, 0.55D, look.z).normalize()"));
		assertTrue(routine.contains("for (int step = 1; step <= 8; step++)"));
	}

	@Test
	void acrobatRoutinePairsRealJumpsWithFlipsAndParticles() throws IOException {
		String entity = source("common/entity/npc/circus/CircusAcrobatEntity.java");
		String renderer = source("client/render/entity/npc/CircusPerformerRenderer.java");
		assertTrue(entity.contains("EntityDataSerializers.INT"));
		assertTrue(entity.contains("setDeltaMovement"));
		assertTrue(entity.contains("ParticleTypes.CLOUD"));
		assertTrue(entity.contains("ParticleTypes.POOF"));
		assertTrue(renderer.contains("acrobat.getFlipProgress(partialTick)"));
		assertTrue(renderer.contains("Axis.XP.rotationDegrees"));
	}

	@Test
	void stiltWalkerBalancesOnOneLegWhileSpinning() throws IOException {
		String entity = source("common/entity/npc/circus/CircusStiltWalkerEntity.java");
		String model = source("client/model/entity/npc/CircusStiltWalkerModel.java");
		String renderer = source("client/render/entity/npc/CircusPerformerRenderer.java");
		assertTrue(entity.contains("getSpinProgress(float partialTick)"));
		assertTrue(model.contains("rightLeg.xRot = leftLeg.xRot = 0.0F"));
		assertTrue(model.contains("leftLeg.zRot = -Mth.PI / 4.0F"));
		assertTrue(renderer.contains("stiltWalker.getSpinProgress(partialTick)"));
		assertTrue(renderer.contains("Axis.YP.rotationDegrees(progress * 720.0F)"));
	}

	@Test
	void knifeThrowerJugglesThreeKnivesBeforeThrowingThem() throws IOException {
		String entity = source("common/entity/npc/circus/CircusKnifeThrowerEntity.java");
		String model = source("client/model/entity/npc/CircusKnifeThrowerModel.java");
		assertTrue(entity.contains("actTick >= 5 && actTick < 30"));
		assertTrue(entity.contains("actTick == 32 || actTick == 38 || actTick == 44"));
		assertTrue(model.contains("root.addOrReplaceChild(\"juggle_knife_0\""));
		assertTrue(model.contains("root.addOrReplaceChild(\"juggle_knife_1\""));
		assertTrue(model.contains("root.addOrReplaceChild(\"juggle_knife_2\""));
		assertTrue(model.contains("knife.visible = entity.isJuggling()"));
	}

	@Test
	void routeChoiceWarnsBeforeCommitAndRepairDisappearsAfterUse() throws IOException {
		String ringmaster = source("common/entity/npc/circus/CircusRingmasterEntity.java");
		assertTrue(ringmaster.contains("\"succession_warning\""));
		assertTrue(ringmaster.contains("\"liberation_warning\""));
		assertTrue(ringmaster.contains("CircusPlayerProgress.canRepairRoute(player)"));
	}

	@Test
	void firstHostAndControlledActsGiveReadableFeedback() throws IOException {
		String performer = source("common/entity/npc/circus/CircusPerformerEntity.java");
		String performance = source("common/circus/CircusPerformanceController.java");
		assertTrue(performer.contains("boolean firstVisit = CircusPlayerProgress.awardMilestone"));
		assertTrue(performer.contains("hemomancy.dialogue.circus_performer.welcome"));
		assertTrue(performance.contains("ticks == required / 2"));
		assertTrue(performance.contains("hemomancy.circus.challenge.hold."));
	}
}
