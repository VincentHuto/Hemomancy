package com.vincenthuto.hemomancy.client.render.world.rite;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class CardinalRiteVisualsSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();
	private static final String SRC = "src/main/java/com/vincenthuto/hemomancy/";

	@Test
	void riteRenderersAreHookedBesideTheBoundary() throws IOException {
		String events = read(SRC + "client/event/ClientEvents.java");
		int boundary = events.indexOf("CardinalRiteBoundaryRenderer.render(event.getPoseStack(), partialTick);");
		int offerings = events.indexOf("CardinalRiteOfferingRenderer.render(event.getPoseStack(), partialTick);");
		int culmination = events.indexOf("CardinalRiteCulminationRenderer.render(event.getPoseStack(), partialTick);");
		assertTrue(boundary > 0 && offerings > boundary && culmination > offerings);
	}

	@Test
	void daemonIsGeometryNotServerParticles() throws IOException {
		String entity = read(SRC + "common/entity/utility/HumanitySpriteEntity.java");
		String renderer = read(SRC + "client/render/entity/misc/HumanitySpriteRenderer.java");
		assertFalse(entity.contains("sendParticles"), "the daemon must not stream particle packets");
		assertFalse(entity.contains("ParticleTypes.ITEM"), "absorbed offerings are drawn, not vanilla item puffs");
		assertTrue(renderer.contains("DaemonHazeQueue.add("), "the daemon is a shader-drawn haze");
		String offerings = read(SRC + "client/render/world/rite/CardinalRiteOfferingRenderer.java");
		assertFalse(offerings.contains("ParticleTypes."), "offering draws use Hemomancy particles only");
		String culmination = read(SRC + "client/render/world/rite/CardinalRiteCulminationRenderer.java");
		assertFalse(culmination.contains("ParticleTypes."));
		assertTrue(culmination.contains("BoltRenderer.INSTANCE.add("), "collapse uses HutosLib lightning");
	}

	@Test
	void unusedRitePillarParticleIsGone() throws IOException {
		String particles = read(SRC + "common/init/ParticleInit.java");
		assertFalse(particles.contains("rite_pillar"));
		assertFalse(Files.exists(ROOT.resolve("src/main/resources/assets/hemomancy/particles/rite_pillar.json")));
		assertFalse(Files.exists(ROOT.resolve(SRC + "client/particle/RitePillarParticle.java")));
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path));
	}
}
