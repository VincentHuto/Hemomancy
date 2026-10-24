package com.vincenthuto.hemomancy.common.entity.summon.puppeteer;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Puppets must arrive and leave on their strings at every lifecycle point, never popping in or out. */
final class PuppetStringsSourceTest {
	private static final Path SOURCE = Path.of("src/main/java/com/vincenthuto/hemomancy");

	private static String read(String path) throws Exception {
		return Files.readString(SOURCE.resolve(path));
	}

	@Test
	void stringFormsAreAppendedDrawnOnAnimusAndHookedBeforeTheSharedAnimusBranch() throws Exception {
		String visuals = read("common/manipulation/ManipulationVisuals.java");
		assertTrue(visuals.indexOf("SOVEREIGN_LASH") < visuals.indexOf("PUPPET_DESCEND, PUPPET_UNSPOOL"),
				"forms are encoded by ordinal and must be appended");
		assertTrue(read("client/render/world/ManipulationMaterials.java").contains("PUPPET_DESCEND, PUPPET_UNSPOOL -> ANIMUS"));
		String renderer = read("client/render/world/ManipulationVisualRenderer.java");
		assertTrue(renderer.contains("PuppetStringsGeometry.draw"));
		assertTrue(renderer.indexOf("if(PuppetStringsGeometry.handles") < renderer.indexOf("if(AnimusMortemGeometry.handles(packet.form())) {"));
		assertTrue(renderer.contains("PuppetStringStates.accept(packet,now)"));
		assertTrue(renderer.contains("packet.form()==ManipulationVisuals.Form.PUPPET_DESCEND && level.getGameTime()-cue.life.born<6"),
				"an arrival cue must survive reaching the client before its body");
	}

	@Test
	void everyPuppetExitUnspoolsInsteadOfDiscarding() throws Exception {
		String behavior = read("common/entity/summon/BoundSummonBehavior.java");
		assertFalse(behavior.contains("mob.discard()"), "owner, crossbar, unravel and dismissal exits must unspool");
		assertTrue(behavior.contains("PuppetStrings.unspool(mob)"));
		String crossbar = read("common/item/harbinger/tool/MarionetteCrossbarItem.java");
		assertTrue(crossbar.contains("PuppetStrings.unspool(mob);"), "recall hauls the body away");
		assertTrue(crossbar.contains("!PuppetStrings.isUnspooling(mob)"), "a departing body no longer counts as active");
		assertTrue(read("common/entity/summon/puppeteer/PuppeteerCrossbarCommands.java").contains("PuppetStrings.unspool(oldBody)"));
		assertTrue(read("common/rite/harbinger/CardinalRiteOrdealEngine.java").contains("PuppetStrings.unspool(mob)"));
		String circus = read("common/mission/circus/CircusDemonstrations.java");
		assertTrue(circus.contains("PuppetStrings.unspool(body)"));
		assertTrue(circus.contains("forEach(PuppetStrings::unspool)"));
		String strings = read("common/entity/summon/puppeteer/PuppetStrings.java");
		assertTrue(strings.contains("@SubscribeEvent(priority = EventPriority.HIGH)"),
				"the unspool countdown must run before the Circus stage cancels a body's tick");
		assertTrue(strings.contains("event.setCanceled(true)"));
	}

	@Test
	void everyPuppetArrivalDescendsAndBodiesFollowTheirStrings() throws Exception {
		assertTrue(read("common/item/harbinger/tool/MarionetteCrossbarItem.java").contains("PuppetStrings.descend(mob);"));
		assertTrue(read("common/entity/summon/puppeteer/PuppeteerCrossbarCommands.java").contains("PuppetStrings.descend(candidate);"));
		assertTrue(read("common/rite/harbinger/PuppeteerTrialRiteController.java").contains("PuppetStrings.descend(created.get())"));
		assertTrue(read("common/mission/circus/CircusDemonstrations.java").contains("PuppetStrings.descend(body)"));
		assertTrue(read("client/render/entity/summon/PuppeteerSummonRenderHelper.java")
				.contains("PuppetStringStates.applyBodyPose(entity, partialTicks, poseStack)"));
		assertTrue(read("client/render/entity/mob/will/WillRenderer.java")
				.contains("PuppetStringStates.applyBodyPose(entity, partialTicks, poseStack)"));
		assertTrue(read("client/render/world/PuppeteerThreadRenderer.java").contains("PuppetStringStates.controlThreadFade"));
		assertTrue(read("client/render/item/harbinger/crossbar/CrossbarMotionState.java")
				.contains("!PuppetStringStates.unspooling(mob.getId())"));
	}
}
