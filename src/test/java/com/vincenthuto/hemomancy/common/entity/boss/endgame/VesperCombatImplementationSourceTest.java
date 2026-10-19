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
		assertFalse(crowned.contains("setNoAi(true)"));
		assertTrue(crowned.contains("new VesperMountController(this)"));
		assertFalse(crowned.contains("new MeleeAttackGoal"));
		assertFalse(crowned.contains("new MoveTowardsTargetGoal"));
		assertTrue(crowned.contains("refreshAnchorDimensions"));
		assertTrue(crowned.contains("part.xOld = part.getX()"));
		assertTrue(crowned.contains("part.yOld = part.getY()"));
		assertTrue(crowned.contains("part.zOld = part.getZ()"));
		assertTrue(crowned.contains("if (!spawnEveningStar()) return;"));
		assertTrue(crowned.contains("return server.addFreshEntity(eveningStar);"));
		assertTrue(anchor.contains("anchorHitboxScale"));
		assertTrue(actions.contains("outlineExposedThroneAnchor"));
		assertFalse(crowned.contains("tickVesperPattern"));
	}

	@Test
	void woundsOpenAtTheirSocketsAndDrainThroughBloodAbsorptionWithoutFreezingTheMount() throws Exception {
		String crowned = read("common/entity/boss/endgame/VesperTheCrownedRefusalEntity.java");
		String anchor = read("common/entity/boss/endgame/VesperThroneAnchorPart.java");
		String actions = read("common/entity/boss/endgame/EndgameBossActions.java");
		String wound = read("common/entity/boss/endgame/VesperWound.java");
		String absorb = read("common/entity/boss/endgame/VesperBloodAbsorptionInteractions.java");
		String renderer = read("client/render/entity/boss/endgame/VesperTheCrownedRefusalRenderer.java");
		String layer = read("client/render/layer/mob/endgame/VesperWoundLayer.java");

		assertFalse(crowned.contains("DATA_VULNERABLE_YAW"), "the wounded mount fights back instead of locking");
		assertFalse(crowned.contains("enforceVulnerableYawLock"));
		assertTrue(crowned.contains("DATA_WOUND_DRAINED"));
		assertTrue(crowned.contains("DATA_ANCHOR_FLASH_TICKS"));
		assertTrue(crowned.contains("VesperWoundRules.load(tag)"));
		assertTrue(crowned.contains("VesperWoundRules.worldOffset"));
		assertTrue(crowned.contains("VesperWound.tickGuard"));
		assertTrue(crowned.contains("VesperWoundRules.tear(wound)"));
		assertTrue(crowned.contains("volume.fill("));
		assertTrue(anchor.contains("ANCHOR_HITBOX_WIDTH"));
		assertTrue(anchor.contains("ANCHOR_HITBOX_HEIGHT"));
		assertTrue(actions.contains("hitThroneAnchor"));
		assertTrue(actions.contains("openThroneWound"));
		assertTrue(actions.contains("VesperVisualEffects.lightning"));
		assertTrue(wound.contains("HemoParticleData.absorbedBloodCell"));
		assertTrue(absorb.contains("drainWound(player, progressPerTick)"));
		assertTrue(renderer.contains("new VesperWoundLayer(this)"));
		assertTrue(layer.contains("SanguineConduitCoreGeometry.render"));
		assertTrue(layer.contains("translateToSocket"));
	}

	@Test
	void allBoundPuppetThreadsUseTheSharedInterpolatedTorsoEndpoint() throws Exception {
		String threads = read("client/render/world/PuppeteerThreadRenderer.java");
		assertTrue(threads.contains("PuppeteerThreadEndpointRules.summonEndpoint"));
		assertTrue(threads.contains("entity instanceof BoundPuppeteerSummon bound"));
		assertTrue(threads.contains("controller instanceof Player owner"));
		assertTrue(threads.contains("VesperTheCrownedRefusalEntity"));
		assertFalse(threads.contains("SUMMON_ANCHOR_SCALE"));
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
		assertTrue(read("client/model/entity/boss/endgame/VesperEveningStarMoves.java").contains("case SICKLE_HOOK"));
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
		String actions = read("common/entity/boss/endgame/VesperMountMoves.java");
		String threads = read("client/render/world/PuppeteerThreadRenderer.java");
		assertTrue(events.contains("applyPuppetBacklash"));
		assertTrue(events.contains("event.setCanceled(true)"));
		assertTrue(events.contains("event.setDroppedExperience(0)"));
		assertTrue(ordeal.contains("HemomancyVesperEncounterPuppet"));
		assertTrue(ordeal.contains("VesperPhaseTwoCombat.cancel"));
		assertTrue(actions.contains("VESPER_PUPPET_MUSTER_SIZE = 3"));
		assertTrue(actions.contains("PuppetStrings.descend(puppet)"));
		assertTrue(actions.contains("BoundSummonBehavior.applyTrialStats"));
		assertTrue(actions.contains("boss.roster().nextMuster"));
		assertTrue(ordeal.contains("to.setInheritedPuppets(from.roster().survivors())"));
		assertTrue(read("common/entity/boss/endgame/EndgameBossActions.java").contains("PuppetStrings::unspool"));
		assertTrue(events.contains("crowned.strikePuppet"));
		assertTrue(actions.contains("bound.hemomancy$setOwnerUUID(boss.getUUID())"));
		assertTrue(threads.contains("VesperTheCrownedRefusalEntity"));
	}

	@Test
	void woundedMountSagsAndTheRiderRecoils() throws Exception {
		String rider = read("client/model/entity/boss/endgame/VesperTheCrownedRefusalModel.java");
		String mount = read("client/model/entity/boss/endgame/VesperMountModel.java");
		assertTrue(rider.contains("woundRecoil(motion.riderRecoil(), ageInTicks)"));
		assertTrue(mount.contains("idle(ageInTicks, entity.getActiveAnchor() >= 0)"));
		assertTrue(mount.contains("this.body.y += 3.0F"));
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
		assertTrue(actions.contains("HemoParticleData.absorbedBloodCell(ParticleColor.BLOOD)"));
		String mount = read("client/model/entity/boss/endgame/VesperMountModel.java");
		assertTrue(model.contains("VesperTheCrownedRefusalAnimations.TRANSFORMATION"));
		assertTrue(model.contains("renderVesperOnly"));
		assertTrue(mount.contains("VesperPhaseTransitionRules.absorptionProgress"));
		assertTrue(mount.contains("renderMountAssembly"));
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

		assertTrue(renderer.contains("new VesperCrownedWeaponLayer(this, rider)"));
		assertTrue(renderer.contains("new VesperRiderLayer(this, context.getModelSet())"));
		assertTrue(layer.contains("ItemInit.living_staff"));
		assertTrue(layer.contains("translateToRiderWeapon"));
		assertTrue(layer.contains("ItemDisplayContext.THIRD_PERSON_RIGHT_HAND"));
		assertTrue(layer.contains("ItemInit.marionette_crossbar"));
		assertTrue(layer.contains("translateToRiderLeftHand"));
		assertTrue(layer.contains("ItemDisplayContext.THIRD_PERSON_LEFT_HAND"));
	}

	@Test
	void phaseHandoffSpawnsAtTheCompletedEveningStarFrameWhileStandaloneAwakeningRemainsAvailable() throws Exception {
		String crowned = read("common/entity/boss/endgame/VesperTheCrownedRefusalEntity.java");
		String evening = read("common/entity/boss/endgame/VesperTheEveningStarEntity.java");
		String renderer = read("client/render/entity/boss/endgame/VesperTheEveningStarRenderer.java");
		String sigils = read("client/render/layer/mob/endgame/VesperTendencySigilLayer.java");
		Path glowPath = SOURCE.resolve("client/render/layer/mob/endgame/VesperAwakeningGlowLayer.java");

		assertFalse(crowned.contains("eveningStar.beginAwakening()"));
		assertTrue(evening.contains(": VesperPhaseTransitionRules.AWAKENING_TOTAL_TICKS"));
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
	void crownedTransitionRendersASharedCardinalTendrilCocoonAndDragonRayStar() throws Exception {
		String crownedRenderer = read("client/render/entity/boss/endgame/VesperTheCrownedRefusalRenderer.java");
		String cardinalRenderer = read("client/render/world/CardinalRiteStaffTendrilRenderer.java");
		Path ribbonPath = SOURCE.resolve("client/render/world/SanguineTendrilRibbonRenderer.java");
		Path cocoonPath = SOURCE.resolve("client/render/layer/mob/endgame/VesperTransitionCocoonRenderer.java");
		assertTrue(Files.exists(ribbonPath), "shared Cardinal-style ribbon renderer is missing");
		assertTrue(Files.exists(cocoonPath), "Vesper cocoon renderer is missing");
		if (!Files.exists(ribbonPath) || !Files.exists(cocoonPath)) return;
		String cocoon = read("client/render/layer/mob/endgame/VesperTransitionCocoonRenderer.java");

		assertTrue(cardinalRenderer.contains("TendrilGeometry.createTubeQuads"));
		assertTrue(cocoon.contains("TendrilRenderer.INSTANCE.add"));
		assertTrue(cocoon.contains("TendrilEffectData"));
		assertTrue(cocoon.contains("TendrilEffectConfig"));
		assertTrue(cocoon.contains("COCOON_FORMATION_TICKS"));
		assertTrue(cocoon.contains("queueCocoonTendrils"));
		assertTrue(cocoon.contains("COCOON_FORMATION_TICKS"));
		assertTrue(cocoon.contains("joints().get(0)"));
		assertTrue(cocoon.contains("joints().size() - 1"));
		assertTrue(cocoon.contains("COCOON_OUTWARD_SAG = -1.25F"));
		assertTrue(cocoon.contains("COCOON_OUTWARD_SAG"));
		assertTrue(cocoon.contains("cocoonOutwardDirection"));
		assertTrue(cocoon.contains("), 0.0F, cocoonOutwardDirection"));
		assertFalse(cocoon.contains("queueCocoonTendrilSegments"));
		assertFalse(cocoon.contains("COCOON_TENDRIL_SEGMENTS"));
		assertFalse(cocoon.contains("segmentDelay"));
		assertFalse(cocoon.contains("startJoint"));
		assertFalse(cocoon.contains("queueCocoonWrapTendrils"));
		assertFalse(cocoon.contains("SanguineTendrilRibbonRenderer.renderLocal"));
		assertTrue(cocoon.contains("RenderType.dragonRays()"));
		assertTrue(cocoon.contains("RenderType.dragonRaysDepth()"));
		assertTrue(cocoon.contains("renderCentralFlare"));
		assertTrue(cocoon.contains("VesperPhaseTransitionRules.cocoonBurstProgress"));
		assertTrue(cocoon.contains("VesperPhaseTransitionRules.isCocoonActive(entity.getTransitionTick())"));
		assertTrue(cocoon.contains("entity.getUUID()"));
		assertFalse(cocoon.contains("entity.tickCount"));
		assertFalse(cocoon.contains("entity.getId()"));
		assertTrue(crownedRenderer.contains("VesperTransitionCocoonRenderer.render"));
		assertTrue(crownedRenderer.contains("getBoundingBoxForCulling().inflate(6.0D)"));
	}

	@Test
	void cocoonStagesDriveAuthoredServerEffectsAndANearbyPlayerBurstCue() throws Exception {
		String actions = read("common/entity/boss/endgame/EndgameBossActions.java");
		String crowned = read("common/entity/boss/endgame/VesperTheCrownedRefusalEntity.java");

		assertTrue(actions.contains("COCOON_BEAM_START_TICK"));
		assertTrue(actions.contains("COCOON_BURST_START_TICK"));
		assertTrue(actions.contains("VesperVisualEffects.voidTendril"));
		assertTrue(actions.contains("VesperVisualEffects.lightning"));
		assertTrue(actions.contains("CardinalRiteImpactPacket"));
		assertTrue(actions.contains("PacketDistributor.sendToPlayersNear"));
		assertTrue(actions.contains("blastVesperCocoon"));
		assertTrue(crowned.contains("finishVesperCocoonReveal"));
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

	@Test
	void reviewFixesKeepRupturesTethersChipsAndPuppetsHonest() throws Exception {
		String crowned = read("common/entity/boss/endgame/VesperTheCrownedRefusalEntity.java");
		String actions = read("common/entity/boss/endgame/EndgameBossActions.java");
		String effects = read("common/entity/boss/endgame/VesperVisualEffects.java");
		String burrow = read("common/entity/boss/endgame/VesperBurrow.java");
		String combat = read("common/entity/boss/endgame/VesperPhaseTwoCombat.java");
		String evening = read("common/entity/boss/endgame/VesperTheEveningStarEntity.java");
		String controller = read("common/entity/boss/endgame/VesperEveningStarController.java");
		String puppets = read("common/entity/boss/endgame/VesperEncounterPuppetEvents.java");

		// A rupture or a tear bursts at the wound that broke, not at socket 0 after the anchor is cleared.
		assertTrue(crowned.contains("public Vec3 woundCenter(int anchor)"));
		assertTrue(actions.contains("exposedAnchorCenter(boss, anchor)"));
		assertFalse(actions.contains("return boss.woundCenter();"));
		// Torn floor is drawn in the authored suite, never vanilla block debris.
		assertFalse(effects.contains("BlockParticleOption"));
		assertFalse(effects.contains("ParticleTypes.BLOCK"));
		// Summoned Vespers surface on the actual ground at the strike, not at the recorded spawn height.
		assertTrue(burrow.contains("EndgameBossActions.findGround"));
		// A cancelled move lets go of the hook, and the string it belonged to ends with it.
		String cancel = combat.substring(combat.indexOf("private static void cancelInternal"),
				combat.indexOf("private static void cleanupArenaEffects"));
		assertTrue(cancel.contains("boss.releaseHook()"));
		assertTrue(controller.contains("endSickleString()"));
		String hooked = evening.substring(evening.indexOf("LivingEntity hookedTarget()"),
				evening.indexOf("float reelDamage()"));
		assertTrue(hooked.contains("isSpectator()") && hooked.contains("isCreative()"));
		assertTrue(combat.contains("VesperSickleRules.holdsTether(action)"));
		assertTrue(combat.contains("VesperSickleRules.landed(action"));
		// His puppets hunt the ordeal owner, stay in the arena, and unspool if their conductor is gone.
		assertTrue(puppets.contains("EntityTickEvent.Post"));
		assertTrue(puppets.contains("VesperPuppetLeashRules.order("));
		assertTrue(puppets.contains("VesperPuppetLeashRules.prefersOwner("));
		// Draining a wound feeds an opted-in drainer and their blood bar shows it.
		String drain = crowned.substring(crowned.indexOf("public float drainWound("),
				crowned.indexOf("private void updateWound("));
		assertTrue(drain.contains("volume.isActive()"));
		assertTrue(drain.contains("new BloodVolumeServerPacket(volume)"));
	}

	@Test
	void recallCastsVesperStringsFromTheCrossbarAndHaulsOnlyWhatTheyHooked() throws Exception {
		String moves = read("common/entity/boss/endgame/VesperMountMoves.java");
		String renderer = read("client/render/world/ManipulationVisualRenderer.java");
		String strings = read("client/render/world/PuppetStringsGeometry.java");
		assertTrue(moves.contains("ManipulationVisuals.Form.VESPER_RECALL"));
		assertTrue(moves.contains("VesperRecallRules.HAUL_TICK"));
		assertTrue(moves.contains("PuppetStrings.unspool(puppet, !hooked)"), "his strings replace the puppet's own");
		assertTrue(renderer.contains("VesperRecallGeometry.draw"));
		assertTrue(renderer.contains("VesperRecallRules.crossbarOffset"), "strings follow the live Crossbar");
		assertTrue(strings.contains("packet.count() < 0 ? 0 : strings"));
	}

	private static String read(String relative) throws Exception {
		return Files.readString(SOURCE.resolve(relative)).replace("\r\n", "\n");
	}
}
