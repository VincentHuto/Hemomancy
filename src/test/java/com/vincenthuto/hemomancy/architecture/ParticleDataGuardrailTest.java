package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Step 10 (roadmap Phase 8, V-6): a particle that carries only a colour uses {@code ColorParticleOptions}, every
 * Hemomancy particle type is a {@code HemoParticleType}, and particle data is built through {@code HemoParticleData}
 * rather than per-factory {@code createData} helpers. Other particle data classes are listed with what they carry.
 */
class ParticleDataGuardrailTest {
	private static final Pattern OPTIONS_CLASS = Pattern.compile(
			"(?m)^\\s*(?:(?:public|final|abstract|static)\\s+)*(?:class|record)\\s+(\\w+)[^{]*\\bimplements\\b[^{]*\\bParticleOptions\\b");
	private static final Pattern TYPE_CLASS = Pattern.compile(
			"(?m)^\\s*(?:(?:public|final|abstract|static)\\s+)*class\\s+(\\w+)[^{]*\\bextends\\s+ParticleType<");
	private static final Pattern FACTORY_HELPER = Pattern.compile("static\\s+ParticleOptions\\s+create\\w*\\(");

	/** Particle data classes and the payload that sets each apart from a plain colour. */
	private static final Map<String, String> DATA = Map.of(
			"ColorParticleOptions", "The colour-only particle, parameterised by its registered type",
			"HitColorParticleData", "Raw red, green and blue floats on the network, not a serialized colour",
			"DaemonDiffuseGlowParticleData", "A scale, no colour",
			"WillAbsorptionGlowParticleData", "A colour and an outward-pulse flag");

	@Test
	void colourOnlyParticlesShareOneDataClass() throws IOException {
		Set<String> found = new TreeSet<>();
		for (Path file : sources()) {
			Matcher matcher = OPTIONS_CLASS.matcher(SourceAssertions.read(file));
			while (matcher.find()) found.add(matcher.group(1));
		}
		assertEquals(new TreeSet<>(DATA.keySet()), found,
				"use ColorParticleOptions for a colour-only particle, or list the new data class here with what it carries");
	}

	@Test
	void particleTypesAreHemoParticleTypes() throws IOException {
		Set<String> found = new TreeSet<>();
		for (Path file : sources()) {
			Matcher matcher = TYPE_CLASS.matcher(SourceAssertions.read(file));
			while (matcher.find()) found.add(matcher.group(1));
		}
		assertEquals(Set.of("HemoParticleType"), found, "register a HemoParticleType with the data's codecs");
	}

	@Test
	void particleDataIsBuiltByTheFacade() throws IOException {
		Set<String> found = new TreeSet<>();
		for (Path file : sources()) {
			String path = RepoPaths.display(file);
			if (path.endsWith("/common/particle/HemoParticleData.java")) continue;
			if (FACTORY_HELPER.matcher(SourceAssertions.read(file)).find()) found.add(path);
		}
		assertEquals(Set.of(), found, "build particle data with HemoParticleData");
	}

	private static List<Path> sources() throws IOException {
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			return files.filter(path -> path.toString().endsWith(".java"))
					.filter(path -> !RepoPaths.display(path).contains("/compat/mna/")).toList();
		}
	}
}
