package com.vincenthuto.hemomancy.client.particle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AbocipherParticleResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");
	private static final String[] LETTERS = {
			"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
			"n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"
	};

	private AbocipherParticleResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String particleInit = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/init/ParticleInit.java"));
		String factory = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/particle/factory/AbocipherParticleFactory.java"));
		String particle = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/particle/AbocipherParticle.java"));
		String particleDefinition = read(RESOURCE_ROOT.resolve("assets/hemomancy/particles/abocipher.json"));

		assertContains("particle type should be a simple reusable glyph particle",
				particleInit, "abocipher = PARTICLE_TYPES.register(\"abocipher\"");
		assertContains("particle type should use vanilla simple particle options",
				particleInit, "new SimpleParticleType(false)");
		assertContains("particle provider should be registered client-side",
				particleInit, "AbocipherParticleFactory::new");
		assertContains("particle should randomly choose from the registered sprite set",
				particle, "pickSprite(spriteSet)");
		assertContains("abocipher particles should have per-particle writhe timing",
				particle, "writhePhase");
		assertContains("abocipher particles should have a subtle organic writhe strength",
				particle, "writheStrength");
		assertContains("abocipher particles should bend their movement with a lateral writhe",
				particle, "wriggleX");
		assertContains("factory should expose reusable particle options",
				factory, "return ParticleInit.abocipher.get();");

		int textureCount = 0;
		for (String letter : LETTERS) {
			String texture = "\"hemomancy:abocipher/" + letter + "\"";
			assertContains("abocipher particle should include texture " + letter,
					particleDefinition, texture);
			assertPathExists("abocipher particle texture " + letter,
					RESOURCE_ROOT.resolve("assets/hemomancy/textures/particle/abocipher/" + letter + ".png"));
			textureCount++;
		}
		assertCount("abocipher particle should include exactly the 26 letter textures",
				particleDefinition, "\"hemomancy:abocipher/", textureCount);
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertPathExists(String label, Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError(label + ": missing " + path);
		}
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertCount(String label, String text, String needle, int expected) {
		int count = 0;
		int index = text.indexOf(needle);
		while (index >= 0) {
			count++;
			index = text.indexOf(needle, index + needle.length());
		}
		if (count != expected) {
			throw new AssertionError(label + ": expected " + expected + " but got " + count);
		}
	}
}
