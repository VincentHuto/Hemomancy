package com.vincenthuto.hemomancy.common.block;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BloodwoodLeavesRenderTypeSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");

	private BloodwoodLeavesRenderTypeSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String clientEvents = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/client/event/ClientEvents.java"));

		assertContains("bloodwood leaves should render with alpha cutout", clientEvents,
				"ItemBlockRenderTypes.setRenderLayer(BlockInit.blood_wood_leaves.get(), RenderType.cutout());");
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
