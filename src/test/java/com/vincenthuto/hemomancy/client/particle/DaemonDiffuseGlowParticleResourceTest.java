package com.vincenthuto.hemomancy.client.particle;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DaemonDiffuseGlowParticleResourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	@Test
	void diffuseDaemonParticleIsRegisteredRenderedAndUsedByTheVoidCore() throws IOException {
		String particle = read("src/main/java/com/vincenthuto/hemomancy/client/particle/DaemonDiffuseGlowParticle.java");
		String factory = read("src/main/java/com/vincenthuto/hemomancy/client/particle/factory/DaemonDiffuseGlowParticleFactory.java");
		String data = read("src/main/java/com/vincenthuto/hemomancy/client/particle/data/DaemonDiffuseGlowParticleData.java");
		String particleInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ParticleInit.java");
		String daemon = read("src/main/java/com/vincenthuto/hemomancy/common/entity/utility/HumanitySpriteEntity.java");

		assertTrue(particle.contains("HLRenderTypeInit.DARK_GLOW_RENDER"));
		assertTrue(particle.contains("initialQuadSize * (1.0F + life * 0.35F)"),
				"the daemon haze should gently diffuse outward");
		assertTrue(data.contains("ParticleInit.daemon_diffuse_glow.get()"));
		assertTrue(particleInit.contains("register(\"daemon_diffuse_glow\""));
		assertTrue(particleInit.contains("DaemonDiffuseGlowParticleFactory::new"));
		assertTrue(daemon.contains("DaemonDiffuseGlowParticleFactory.createData"));
		assertFalse(daemon.contains("case SMOKE -> ParticleTypes.SMOKE"));
		assertTrue(Files.isRegularFile(ROOT.resolve(
				"src/main/resources/assets/hemomancy/particles/daemon_diffuse_glow.json")));
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path));
	}
}
