package com.vincenthuto.hemomancy.common.resource;

import java.nio.file.Files;
import java.nio.file.Path;

public final class PhaseFourTendonLineResourceTest {
	private static final Path ROOT = Path.of("src/main/resources");
	private static final Path DATA = ROOT.resolve("data/hemomancy");
	private static final Path ASSETS = ROOT.resolve("assets/hemomancy");

	private PhaseFourTendonLineResourceTest() {
	}

	public static void main(String[] args) {
		require(ASSETS.resolve("models/item/tendon_line.json"));
		require(ASSETS.resolve("textures/item/tendon_line.png"));
		require(DATA.resolve("recipe/tendon_line.json"));
	}

	private static void require(Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
	}
}
