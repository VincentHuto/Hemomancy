package com.vincenthuto.hemomancy.client.render.item.hematic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CellHandItemRendererParticleOriginTest {
	private static final Path RENDERER = Path.of("src/main/java/com/vincenthuto/hemomancy/client/render/item/hematic/CellHandParticleEffects.java");

	private CellHandItemRendererParticleOriginTest() {
	}

	public static void main(String[] args) throws IOException {
		String source = Files.readString(RENDERER).replace("\r\n", "\n");

		assertContains("projection particles should target the block hit position through an explicit destination",
				source, "Vec3 projectionTarget = trace.getLocation().add(0.0, 1.05D, 0.0);");
		assertContains("projection particles should start from the hand particle origin",
				source, "Vec3 finalPos = projectionTarget.subtract(origin.x, origin.y, origin.z).reverse();");
		assertDoesNotContain("projection particles should not source their trajectory from the eye/camera anchor",
				source, "hitVec.subtract(particlePos.x, particlePos.y, particlePos.z).reverse()");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertDoesNotContain(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + ": found " + forbidden);
		}
	}
}
