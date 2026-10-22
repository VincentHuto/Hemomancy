package com.vincenthuto.hemomancy.client.render.world;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

final class WorkingCraftSwallowRulesTest {
	@Test
	void aProjectionIntoASideLandsOnTheFillLineWhereItWasAimed() {
		Vec3 source = new Vec3(10.5D, 66.6D, 2.5D);
		Vec3 hit = new Vec3(3.0D, 65.7D, 1.2D);
		Vec3 landing = WorkingCraftSwallowRules.projectionLanding(source, hit, true, 0, 0, 2, 1, 64, 3, 0.5F);
		assertEquals(3.0D, landing.x, 1.0E-6);
		assertEquals(1.2D, landing.z, 1.0E-6);
		assertEquals(65.5D, landing.y, 1.0E-6, "the beam lands on the fill line, not where the cursor sits");
	}

	@Test
	void aProjectionOntoTheTopLandsOnTheSideFacingTheCaster() {
		Vec3 source = new Vec3(10.5D, 68.6D, 0.5D);
		Vec3 hit = new Vec3(1.5D, 67.0D, 1.0D);
		Vec3 landing = WorkingCraftSwallowRules.projectionLanding(source, hit, false, 0, 0, 2, 1, 64, 3, 0.25F);
		assertEquals(3.0D, landing.x, 1.0E-6, "pulled out to the structure's east face, toward the caster");
		assertEquals(0.5D, landing.z, 1.0E-6);
		assertEquals(64.75D, landing.y, 1.0E-6);
	}

	@Test
	void anEmptyStructureTakesTheBeamJustAboveItsFoot() {
		Vec3 landing = WorkingCraftSwallowRules.projectionLanding(Vec3.ZERO, new Vec3(0.0D, 65.0D, 0.0D), true,
				0, 0, 0, 0, 64, 2, 0.0F);
		assertTrue(landing.y > 64.0D && landing.y < 64.2D, "above the floor membrane, not inside it: " + landing.y);
	}

	@Test
	void theFillLineClimbsFromTheFootToTheTop() {
		assertEquals(64.0D, WorkingCraftSwallowRules.fillLine(64.0D, 3.0D, 0.0F), 1.0E-6);
		assertEquals(65.5D, WorkingCraftSwallowRules.fillLine(64.0D, 3.0D, 0.5F), 1.0E-6);
		assertEquals(67.0D, WorkingCraftSwallowRules.fillLine(64.0D, 3.0D, 1.5F), 1.0E-6, "clamped at the top");
		assertEquals(64.0D, WorkingCraftSwallowRules.fillLine(64.0D, 3.0D, -1.0F), 1.0E-6, "clamped at the foot");
	}

	@Test
	void theDisplayedFillEasesTowardTheSyncedFill() {
		float eased = WorkingCraftSwallowRules.smoothFill(0.2F, 0.6F);
		assertTrue(eased > 0.2F && eased < 0.6F);
		assertEquals(0.6F, WorkingCraftSwallowRules.smoothFill(0.5999F, 0.6F), 1.0E-3F);
	}

	@Test
	void aCompletedStructureIsYankedUnderTheFloorAndLandsWithAThud() {
		double height = 3.0D;
		double full = height + WorkingCraftSwallowRules.SINK_OVERSHOOT;
		for (int age = 0; age <= WorkingCraftSwallowRules.ANTICIPATION_TICKS; age++) {
			assertTrue(WorkingCraftSwallowRules.sinkOffset(age, height) < 0.12D, "it only shudders before the pull");
		}
		float halfway = WorkingCraftSwallowRules.ANTICIPATION_TICKS + WorkingCraftSwallowRules.DRAG_TICKS / 2.0F;
		assertTrue(WorkingCraftSwallowRules.sinkOffset(halfway, height) < full * 0.4D,
				"the drag accelerates into the floor instead of easing out");
		assertEquals(full, WorkingCraftSwallowRules.sinkOffset(WorkingCraftSwallowRules.THUD_TICK, height), 1.0E-6);
		assertEquals(full, WorkingCraftSwallowRules.sinkOffset(WorkingCraftSwallowRules.THUD_TICK + 20, height), 1.0E-6);
		assertTrue(WorkingCraftSwallowRules.THUD_TICK <= 12, "a yank, not a slow settle");
		double previous = -1.0D;
		for (int age = WorkingCraftSwallowRules.ANTICIPATION_TICKS; age <= WorkingCraftSwallowRules.THUD_TICK; age++) {
			double sink = WorkingCraftSwallowRules.sinkOffset(age, height);
			assertTrue(sink >= previous, "the pull never reverses");
			previous = sink;
		}
	}

	@Test
	void aRecalledCraftDrainsBackDown() {
		assertEquals(0.8F, WorkingCraftSwallowRules.drainFill(0.8F, 0), 1.0E-6F);
		assertEquals(0.0F, WorkingCraftSwallowRules.drainFill(0.8F, WorkingCraftSwallowRules.DRAIN_TICKS), 1.0E-6F);
	}

	@Test
	void theStructureDarkensOnceTheDaemonIsInside() {
		int dive = com.vincenthuto.hemomancy.common.rite.harbinger.WorkingCraftDaemonPath.DIVE_TICKS;
		assertEquals(0.0F, WorkingCraftSwallowRules.possession(0.0F), 1.0E-6F);
		assertEquals(0.0F, WorkingCraftSwallowRules.possession(dive - 6.0F), 1.0E-6F, "not before the daemon arrives");
		assertEquals(1.0F, WorkingCraftSwallowRules.possession(dive + 20.0F), 1.0E-6F);
		float previous = 0.0F;
		for (int tick = 0; tick < dive + 30; tick++) {
			float next = WorkingCraftSwallowRules.possession(tick);
			assertTrue(next >= previous);
			previous = next;
		}
		int red = WorkingCraftSwallowRules.possessionTint(0xC01828, 1.0F);
		assertTrue((red >> 16 & 0xFF) < 0xC0 && (red & 0xFF) < 0x28, "possessed blood is darker: " + Integer.toHexString(red));
		assertEquals(0xC01828, WorkingCraftSwallowRules.possessionTint(0xC01828, 0.0F));
	}

	@Test
	void tentaclesWritheUpTheCornerAndHookOverTheTop() {
		double top = 2.0D;
		var now = WorkingCraftSwallowRules.tentacleSpine(0.0D, 0.0D, -1.0D, -1.0D, top, 10.0D, 7, 0.0F);
		var later = WorkingCraftSwallowRules.tentacleSpine(0.0D, 0.0D, -1.0D, -1.0D, top, 30.0D, 7, 0.0F);
		assertTrue(now.getFirst().y < 0.1D, "rooted in the floor");
		assertTrue(now.getLast().y >= top - 0.05D && now.getLast().y <= top + 0.5D, "the tip reaches the fill line");
		assertTrue(now.getLast().x > now.get(now.size() - 3).x && now.getLast().z > now.get(now.size() - 3).z,
				"the tip hooks inward over the structure's edge");
		double moved = 0.0D;
		for (int i = 0; i < now.size(); i++) {
			moved = Math.max(moved, now.get(i).distanceTo(later.get(i)));
			assertTrue(Math.hypot(now.get(i).x, now.get(i).z) < 0.9D, "hugs its corner");
		}
		assertTrue(moved > 0.05D, "it writhes over time");
	}

	@Test
	void theSealPulseFadesOut() {
		assertEquals(1.0F, WorkingCraftSwallowRules.sealPulse(0.0F), 1.0E-6F);
		assertEquals(0.0F, WorkingCraftSwallowRules.sealPulse(WorkingCraftSwallowRules.SEAL_PULSE_TICKS), 1.0E-6F);
		assertEquals(0.0F, WorkingCraftSwallowRules.sealPulse(-1.0F), 1.0E-6F, "no seal remembered");
	}

	@Test
	void rendererHooksAreInPlaceAndUseNoVanillaParticles() throws IOException {
		Path client = Path.of("src/main/java/com/vincenthuto/hemomancy/client");
		assertTrue(Files.readString(client.resolve("render/world/WorldEffectDispatcher.java"))
				.contains("WorkingCraftSwallowRenderer.render("));
		assertTrue(Files.readString(client.resolve("render/world/ManipulationVisualRenderer.java"))
				.contains("WorkingCraftSwallowGeometry.collect("));
		for (String file : new String[] {"WorkingCraftSwallowRules.java", "WorkingCraftSwallowGeometry.java",
				"WorkingCraftSwallowRenderer.java"}) {
			assertFalse(Files.readString(client.resolve("render/world/" + file)).contains("ParticleTypes"), file);
		}
		String geometry = Files.readString(client.resolve("render/world/WorkingCraftSwallowGeometry.java"));
		assertTrue(geometry.contains("Material.ANIMUS") && !geometry.contains("Material.CRUOR"),
				"tentacles are solid blood-school sinew, not the grainy Cruor sheet");
	}
}
