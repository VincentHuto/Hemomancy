package com.vincenthuto.hemomancy.common.block;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BloodwoodLeavesRenderTypeSourceTest {
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private BloodwoodLeavesRenderTypeSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String model = read(RESOURCE_ROOT.resolve("assets/hemomancy/models/block/blood_wood_leaves.json"));

		assertContains("bloodwood leaves should render with transparent cutout pixels", model,
				"\"render_type\": \"cutout\"");
	}

	private static String read(Path path) throws IOException {
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
