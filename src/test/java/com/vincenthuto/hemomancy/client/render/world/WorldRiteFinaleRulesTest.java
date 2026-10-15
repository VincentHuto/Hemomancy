package com.vincenthuto.hemomancy.client.render.world;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class WorldRiteFinaleRulesTest {
	@Test
	void cracksRadiateFromTheImpactAcrossTheGround() {
		List<List<Vec3>> cracks = WorldRiteFinaleRules.cracks(1234, 7.0D);
		assertTrue(cracks.size() >= 8, "a full starburst of cracks");
		for (List<Vec3> crack : cracks) {
			assertTrue(crack.size() >= 4, "jagged, not straight");
			for (Vec3 point : crack) assertEquals(0.0D, point.y, 1.0E-9, "flat on the ground");
			assertTrue(Math.hypot(crack.getLast().x, crack.getLast().z) <= 7.0D + 1.0E-6, "inside the reach");
			assertTrue(Math.hypot(crack.getLast().x, crack.getLast().z) > Math.hypot(crack.getFirst().x, crack.getFirst().z));
		}
		assertEquals(cracks, WorldRiteFinaleRules.cracks(1234, 7.0D), "the same slam cracks the same way on every client");
		assertNotEquals(cracks, WorldRiteFinaleRules.cracks(99, 7.0D));
	}

	@Test
	void theCracksTearOutFastThenBlazeThenCoolToBlack() {
		assertEquals(0.0F, WorldRiteFinaleRules.crackReach(0.0F), 1.0E-6F);
		assertEquals(1.0F, WorldRiteFinaleRules.crackReach(WorldRiteFinaleRules.TEAR_TICKS), 1.0E-6F);
		assertTrue(WorldRiteFinaleRules.TEAR_TICKS <= 8, "the ground splits in a snap");
		assertEquals(1.0F, WorldRiteFinaleRules.blaze(10.0F), 1.0E-6F);
		assertEquals(0.0F, WorldRiteFinaleRules.blaze(WorldRiteFinaleRules.COOL_END_TICKS), 1.0E-6F);
		assertEquals(1.0F, WorldRiteFinaleRules.charred(WorldRiteFinaleRules.COOL_END_TICKS), 1.0E-6F,
				"black remains after the red has cooled");
		assertEquals(0.0F, WorldRiteFinaleRules.charred(WorldRiteFinaleRules.LIFETIME_TICKS), 1.0E-6F);
	}

	@Test
	void theSkyFlashIsSharpAndItsRingSpreads() {
		assertEquals(1.0F, WorldRiteFinaleRules.skyFlash(0.0F), 1.0E-6F);
		assertTrue(WorldRiteFinaleRules.skyFlash(6.0F) < 0.5F, "a flash, not a glow");
		assertEquals(0.0F, WorldRiteFinaleRules.skyFlash(WorldRiteFinaleRules.FLASH_TICKS), 1.0E-6F);
		assertTrue(WorldRiteFinaleRules.ringRadius(10.0F) > WorldRiteFinaleRules.ringRadius(2.0F));
	}

	@Test
	void finaleGeometryIsHookedAndUsesNoVanillaParticles() throws IOException {
		Path client = Path.of("src/main/java/com/vincenthuto/hemomancy/client");
		assertTrue(Files.readString(client.resolve("render/world/ManipulationVisualRenderer.java"))
				.contains("WorldRiteFinaleGeometry.collect("));
		for (String file : new String[] {"WorldRiteFinaleRules.java", "WorldRiteFinaleGeometry.java"}) {
			assertFalse(Files.readString(client.resolve("render/world/" + file)).contains("ParticleTypes"), file);
		}
	}
}
