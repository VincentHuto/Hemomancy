package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class VesperCombatImplementationSourceTest {
	private static final Path SOURCE = Path.of("src/main/java/com/vincenthuto/hemomancy");

	@Test
	void crownedRefusalUsesMultipartThroneAnchorsAndAnExplicitTransformation() throws Exception {
		String crowned = read("common/entity/boss/endgame/VesperTheCrownedRefusalEntity.java");
		String anchor = read("common/entity/boss/endgame/VesperThroneAnchorPart.java");
		String actions = read("common/entity/boss/endgame/EndgameBossActions.java");
		assertTrue(crowned.contains("isMultipartEntity()"));
		assertTrue(crowned.contains("VesperCombatRules.healthFloor"));
		assertTrue(crowned.contains("breakAnchor"));
		assertTrue(crowned.contains("tickVesperTransformation"));
		assertTrue(crowned.contains("setNoAi(true)"));
		assertTrue(crowned.contains("refreshAnchorDimensions"));
		assertTrue(crowned.contains("part.xOld = part.getX()"));
		assertTrue(crowned.contains("part.yOld = part.getY()"));
		assertTrue(crowned.contains("part.zOld = part.getZ()"));
		assertTrue(anchor.contains("anchorHitboxScale"));
		assertTrue(actions.contains("outlineExposedThroneAnchor"));
		assertFalse(crowned.contains("tickVesperPattern"));
	}

	@Test
	void eveningStarRendersEightTendenciesAndRealLivingWeapons() throws Exception {
		String evening = read("common/entity/boss/endgame/VesperTheEveningStarEntity.java");
		String renderer = read("client/render/entity/boss/endgame/VesperTheEveningStarRenderer.java");
		String sigils = read("client/render/layer/mob/endgame/VesperTendencySigilLayer.java");
		assertTrue(evening.contains("VesperCombatRules.tendencyAt"));
		assertTrue(evening.contains("ItemInit.living_staff"));
		assertTrue(evening.contains("ItemInit.living_flail"));
		assertTrue(evening.contains("living_sickle"));
		assertTrue(renderer.contains("new VesperTendencySigilLayer(this)"));
		assertTrue(renderer.contains("new VesperLivingWeaponLayer(this)"));
		assertTrue(sigils.contains("EnumBloodTendency.values().length"));
	}

	@Test
	void eveningStarUsesWeaponActionsInsteadOfVanillaMeleeGoals() throws Exception {
		String evening = read("common/entity/boss/endgame/VesperTheEveningStarEntity.java");
		String combat = read("common/entity/boss/endgame/VesperPhaseTwoCombat.java");
		String model = read("client/model/entity/boss/endgame/VesperTheEveningStarModel.java");
		String weapon = read("client/render/layer/mob/endgame/VesperLivingWeaponLayer.java");
		String temporaryIce = read("common/manipulation/congeatio/TemporaryIceManager.java");

		assertFalse(evening.contains("new LeapAtTargetGoal"));
		assertFalse(evening.contains("new MeleeAttackGoal"));
		assertFalse(evening.contains("new MoveTowardsTargetGoal"));
		assertTrue(evening.contains("DATA_WEAPON_ACTION"));
		assertTrue(evening.contains("DATA_ACTION_TICK"));
		assertTrue(evening.contains("WeaponActionHitMask"));
		assertTrue(evening.contains("WeaponOriginX"));
		assertTrue(combat.contains("BloodBoltEntity"));
		assertFalse(combat.contains("TrackingBloodOrbEntity"));
		assertTrue(combat.contains("EntityManipulationEffects.cast"));
		assertTrue(combat.contains("cleanupArenaEffects"));
		assertTrue(temporaryIce.contains("clearEncounterOwned"));
		assertTrue(combat.contains("setTemporaryResponse"));
		assertTrue(model.contains("getWeaponAction()"));
		assertTrue(weapon.contains("translateToLeftWeapon"));
		assertTrue(weapon.contains("LivingFlailRenderHelper.renderHeld"));
		assertTrue(weapon.contains("case DUCTILIS -> -90.0F"));
		assertFalse(weapon.contains("tendency == EnumBloodTendency.DUCTILIS) poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F)"));
	}

	@Test
	void rageUsesThePlayerFacingSickleHookAndCloseSpinForms() throws Exception {
		String combat = read("common/entity/boss/endgame/VesperPhaseTwoCombat.java");
		String model = read("client/model/entity/boss/endgame/VesperTheEveningStarModel.java");
		String layer = read("client/render/layer/mob/endgame/VesperLivingWeaponLayer.java");

		assertTrue(combat.contains("case SICKLE_HOOK"));
		assertTrue(combat.contains("fireSickleHook"));
		assertTrue(combat.contains("LivingSickleHookEntity"));
		assertTrue(model.contains("case SICKLE_HOOK"));
		assertTrue(layer.contains("isSickleHookReleased"));
	}

	@Test
	void eveningStarUsesAFullBodyKeyframedHitReaction() throws Exception {
		String model = read("client/model/entity/boss/endgame/VesperTheEveningStarModel.java");
		String animations = read("client/model/entity/boss/endgame/VesperTheEveningStarAnimations.java");

		assertTrue(model.contains("entity.hurtTime > 0"));
		assertTrue(model.contains("VesperTheEveningStarAnimations.HIT"));
		assertTrue(animations.contains("AnimationDefinition HIT"));
		assertTrue(animations.contains(".addAnimation(\"whole\""));
		assertTrue(animations.contains(".addAnimation(\"body\""));
		assertTrue(animations.contains(".addAnimation(\"head\""));
		assertTrue(animations.contains(".addAnimation(\"rightArm\""));
		assertTrue(animations.contains(".addAnimation(\"leftArm\""));
		assertTrue(animations.contains(".addAnimation(\"ClothBack\""));
	}

	@Test
	void encounterPuppetsHaveBacklashNoRewardsAndAttemptCleanup() throws Exception {
		String events = read("common/entity/boss/endgame/VesperEncounterPuppetEvents.java");
		String ordeal = read("common/worldgen/VesperOrdealManager.java");
		String actions = read("common/entity/boss/endgame/EndgameBossActions.java");
		String threads = read("client/render/world/PuppeteerThreadRenderer.java");
		assertTrue(events.contains("applyPuppetBacklash"));
		assertTrue(events.contains("event.setCanceled(true)"));
		assertTrue(events.contains("event.setDroppedExperience(0)"));
		assertTrue(ordeal.contains("HemomancyVesperEncounterPuppet"));
		assertTrue(ordeal.contains("VesperPhaseTwoCombat.cancel"));
		assertTrue(actions.contains("VESPER_PUPPET_MUSTER_SIZE = 3"));
		assertTrue(actions.contains("bound.hemomancy$setOwnerUUID(boss.getUUID())"));
		assertTrue(threads.contains("VesperTheCrownedRefusalEntity"));
	}

	@Test
	void vulnerablePoseStillLowersVesperTowardTheExposedWeakpoint() throws Exception {
		String model = read("client/model/entity/boss/endgame/VesperTheCrownedRefusalModel.java");
		String animations = read("client/model/entity/boss/endgame/VesperTheCrownedRefusalAnimations.java");
		assertTrue(model.contains("VesperTheCrownedRefusalAnimations.VULNERABLE"));
		assertTrue(animations.contains("KeyframeAnimations.posVec(0.0F, -7.0F, 0.0F)"));
		assertTrue(animations.contains("KeyframeAnimations.degreeVec(18.3346F, 0.0F, 0.0F)"));
	}

	@Test
	void phaseOneDismountsBeforeTheMountBloodMeltsIntoVesper() throws Exception {
		String crowned = read("common/entity/boss/endgame/VesperTheCrownedRefusalEntity.java");
		String actions = read("common/entity/boss/endgame/EndgameBossActions.java");
		String model = read("client/model/entity/boss/endgame/VesperTheCrownedRefusalModel.java");
		String renderer = read("client/render/entity/boss/endgame/VesperTheCrownedRefusalRenderer.java");
		Path layerPath = SOURCE.resolve("client/render/layer/mob/endgame/VesperMountAbsorptionLayer.java");
		assertTrue(Files.exists(layerPath), "mount absorption render layer is missing");
		if (!Files.exists(layerPath)) return;
		String layer = read("client/render/layer/mob/endgame/VesperMountAbsorptionLayer.java");

		assertTrue(crowned.contains("VesperPhaseTransitionRules.isComplete(transition)"));
		assertTrue(crowned.contains("dismountLandingPosition()"));
		assertTrue(actions.contains("AbsorbedBloodCellParticleFactory"));
		assertTrue(model.contains("VesperPhaseTransitionRules.dismountProgress"));
		assertTrue(model.contains("VesperPhaseTransitionRules.collapseProgress"));
		assertTrue(model.contains("this.head.zRot += 0.42F * collapse"));
		assertTrue(model.contains("this.leftArm.xRot"));
		assertTrue(model.contains("renderVesperOnly"));
		assertTrue(model.contains("renderMountAssembly"));
		assertTrue(renderer.contains("new VesperMountAbsorptionLayer(this)"));
		assertTrue(layer.contains("cardinalStaffBloodMelt"));
	}

	@Test
	void crownedRefusalRendersHisStaffAndPuppeteeringCrossbarInOppositeHands() throws Exception {
		String renderer = read("client/render/entity/boss/endgame/VesperTheCrownedRefusalRenderer.java");
		Path layerPath = SOURCE.resolve("client/render/layer/mob/endgame/VesperCrownedWeaponLayer.java");
		assertTrue(Files.exists(layerPath), "Crowned Refusal weapon render layer is missing");
		if (!Files.exists(layerPath)) return;
		String layer = read("client/render/layer/mob/endgame/VesperCrownedWeaponLayer.java");

		assertTrue(renderer.contains("new VesperCrownedWeaponLayer(this)"));
		assertTrue(layer.contains("ItemInit.living_staff"));
		assertTrue(layer.contains("translateToRiderWeapon"));
		assertTrue(layer.contains("ItemDisplayContext.THIRD_PERSON_RIGHT_HAND"));
		assertTrue(layer.contains("ItemInit.marionette_crossbar"));
		assertTrue(layer.contains("translateToRiderLeftHand"));
		assertTrue(layer.contains("ItemDisplayContext.THIRD_PERSON_LEFT_HAND"));
	}

	@Test
	void eveningStarAwakensThroughGlowSigilsAndPhysicalGrowthBeforeCombat() throws Exception {
		String crowned = read("common/entity/boss/endgame/VesperTheCrownedRefusalEntity.java");
		String evening = read("common/entity/boss/endgame/VesperTheEveningStarEntity.java");
		String renderer = read("client/render/entity/boss/endgame/VesperTheEveningStarRenderer.java");
		String sigils = read("client/render/layer/mob/endgame/VesperTendencySigilLayer.java");
		Path glowPath = SOURCE.resolve("client/render/layer/mob/endgame/VesperAwakeningGlowLayer.java");

		assertTrue(crowned.contains("beginAwakening()"));
		assertTrue(evening.contains("DATA_AWAKENING_TICK"));
		assertTrue(evening.contains("tickAwakening"));
		assertTrue(evening.contains("getDefaultDimensions(Pose pose)"));
		assertTrue(evening.contains("refreshDimensions()"));
		assertTrue(renderer.contains("VesperPhaseTransitionRules.awakeningScale"));
		assertTrue(sigils.contains("VesperPhaseTransitionRules.awakeningSigilCount"));
		assertTrue(Files.exists(glowPath), "Evening Star awakening glow layer is missing");
		assertTrue(read("client/render/layer/mob/endgame/VesperLivingWeaponLayer.java")
				.contains("entity.isAwakening()"));
	}

	@Test
	void eveningStarMustBeBloodAbsorbedAfterItsKneelingDefeat() throws Exception {
		String evening = read("common/entity/boss/endgame/VesperTheEveningStarEntity.java");
		String absorption = read("common/item/harbinger/tool/living/BloodAbsorptionItem.java");
		String staff = read("common/item/harbinger/tool/living/LivingStaffItem.java");
		String model = read("client/model/entity/boss/endgame/VesperTheEveningStarModel.java");
		String sigils = read("client/render/layer/mob/endgame/VesperTendencySigilLayer.java");
		String weapon = read("client/render/layer/mob/endgame/VesperLivingWeaponLayer.java");

		assertTrue(evening.contains("enterAwaitingAbsorption"));
		assertTrue(evening.contains("EffectInit.monolithic_dislocation"));
		assertTrue(evening.contains("absorbWithBlood"));
		assertFalse(evening.contains("public void die(DamageSource source)"));
		assertTrue(absorption.contains("VesperBloodAbsorptionInteractions.tryAbsorb"));
		assertTrue(staff.contains("VesperBloodAbsorptionInteractions.tryAbsorb"));
		assertTrue(model.contains("entity.isAwaitingAbsorption()"));
		assertTrue(model.contains("defeatKneelProgress"));
		assertTrue(sigils.contains("sigilFizzleProgress"));
		assertTrue(weapon.contains("weaponDissolveProgress"));
		assertTrue(weapon.contains("hermitFarewellDissolve"));
		assertTrue(evening.contains("isDefeatAnimationComplete"));
	}

	private static String read(String relative) throws Exception {
		return Files.readString(SOURCE.resolve(relative)).replace("\r\n", "\n");
	}
}
