package com.vincenthuto.hemomancy.client.render.world.rite;

import com.vincenthuto.hemomancy.common.rite.harbinger.WorkingCraftDaemonPath;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

final class OfferingDissolveRulesTest {
	@Test
	void theOfferingBreaksWhileTheDaemonStillHangsOverIt() {
		assertTrue(OfferingDissolveRules.BREAK_TICKS < WorkingCraftDaemonPath.ABSORB_TICKS);
		assertTrue(OfferingDissolveRules.BREAK_TICKS >= 4, "long enough to see it pulled up and strain");
	}

	@Test
	void itRisesTowardTheDaemonAndNeverSinksBack() {
		float previous = 0.0F;
		for (int tick = 0; tick <= OfferingDissolveRules.BREAK_TICKS; tick++) {
			float lift = OfferingDissolveRules.lift(tick);
			assertTrue(lift >= previous, "never sinks back into the brazier at tick " + tick);
			previous = lift;
		}
		assertTrue(OfferingDissolveRules.lift(OfferingDissolveRules.BREAK_TICKS) > 0.4F, "pulled well up toward the daemon");
	}

	@Test
	void itStrainsThenIsGoneOnceItBreaks() {
		assertTrue(OfferingDissolveRules.strain(OfferingDissolveRules.BREAK_TICKS - 1.0F) > OfferingDissolveRules.strain(1.0F),
				"shudders harder as it is about to break");
		assertTrue(OfferingDissolveRules.scale(0.0F) > 0.0F);
		assertEquals(0.0F, OfferingDissolveRules.scale(OfferingDissolveRules.BREAK_TICKS), 1.0E-6F, "broken, not shrunk away");
		assertEquals(0.0F, OfferingDissolveRules.scale(OfferingDissolveRules.BREAK_TICKS + 10.0F), 1.0E-6F);
	}

	@Test
	void itShattersOnceAndNeverAgainWhileItsBeatLingers() throws IOException {
		assertTrue(OfferingDissolveRules.shattersNow(OfferingDissolveRules.BREAK_TICKS));
		assertFalse(OfferingDissolveRules.shattersNow(OfferingDissolveRules.BREAK_TICKS - 1.0F));
		assertFalse(OfferingDissolveRules.shattersNow(OfferingDissolveRules.BREAK_TICKS + 50.0F),
				"a beat that lingers in the event list must not spray crumbs again");
		String renderer = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/client/render/world/rite/CardinalRiteOfferingDissolveRenderer.java"));
		assertFalse(renderer.contains("BROKEN.removeIf"), "broken beats are only forgotten once they leave the event list");
	}

	@Test
	void itBreaksWithVanillaItemCrumbsAndTheBreakSound() throws IOException {
		String renderer = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/client/render/world/rite/CardinalRiteOfferingDissolveRenderer.java"));
		assertTrue(renderer.contains("new ItemParticleOption(ParticleTypes.ITEM"), "vanilla item-break crumbs");
		assertTrue(renderer.contains("SoundEvents.ITEM_BREAK"), "the vanilla item-break sound");
		assertTrue(Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java"))
				.contains("CardinalRiteOfferingDissolveRenderer.render("));
		String engine = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/CardinalRiteOrdealEngine.java"));
		int craft = engine.indexOf("private static void tickCraftProcession");
		int end = engine.indexOf("private static void tickCraftCulmination");
		String procession = engine.substring(craft, end);
		assertTrue(procession.contains("CardinalRiteEventKind.OFFERING_DISSOLVED")
				&& !procession.contains("CardinalRiteEventKind.OFFERING_DRAWN"),
				"a craft's daemon breaks offerings where they sit instead of drawing them along a vessel");
	}
}
