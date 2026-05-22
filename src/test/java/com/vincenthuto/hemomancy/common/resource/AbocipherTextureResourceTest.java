package com.vincenthuto.hemomancy.common.resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class AbocipherTextureResourceTest {
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");
	private static final byte[] PNG_SIGNATURE = {
			(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
	};

	private AbocipherTextureResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		List<Path> roots = List.of(
				RESOURCE_ROOT.resolve("assets/hemomancy/textures/particle/abocipher"),
				RESOURCE_ROOT.resolve("assets/hemomancy/textures/font/abocipher"));

		for (Path root : roots) {
			try (var paths = Files.list(root)) {
				for (Path texture : paths.filter(path -> path.getFileName().toString().endsWith(".png")).toList()) {
					assertPng(texture);
				}
			}
		}
	}

	private static void assertPng(Path path) throws IOException {
		byte[] bytes = Files.readAllBytes(path);
		if (bytes.length < PNG_SIGNATURE.length) {
			throw new AssertionError("PNG is too short: " + path);
		}
		for (int i = 0; i < PNG_SIGNATURE.length; i++) {
			if (bytes[i] != PNG_SIGNATURE[i]) {
				throw new AssertionError("PNG has invalid signature: " + path);
			}
		}
	}
}
