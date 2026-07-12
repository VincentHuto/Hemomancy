package com.vincenthuto.hemomancy.common.capability.player.harbinger.degree;

import net.minecraft.nbt.CompoundTag;

public final class InitiatoryDegreeLoreStateTest {
	private InitiatoryDegreeLoreStateTest() {
	}

	public static void main(String[] args) {
		InitiatoryDegree degree = new InitiatoryDegree();
		degree.setHasFoundedBloodline(true);
		degree.setFounderIntegrationSevered(true);
		degree.setFungalRevelationWitnessed(true);
		degree.setFungalSpineGranted(true);
		degree.setArchonPath(EnumArchonPath.SILENT_PENDING);

		CompoundTag saved = degree.serializeNBT(null);
		InitiatoryDegree restored = new InitiatoryDegree();
		restored.deserializeNBT(null, saved);

		assertTrue("bloodline founding persists", restored.hasFoundedBloodline());
		assertTrue("founder severance persists", restored.isFounderIntegrationSevered());
		assertTrue("fungal revelation persists", restored.hasWitnessedFungalRevelation());
		assertTrue("spine grant persists", restored.hasFungalSpineGranted());
		assertEquals("archon path persists", EnumArchonPath.SILENT_PENDING, restored.getArchonPath());
	}

	private static void assertTrue(String label, boolean actual) {
		if (!actual) throw new AssertionError(label + ": expected true");
	}

	private static void assertEquals(String label, Object expected, Object actual) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
