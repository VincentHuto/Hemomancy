package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Phase 7 (step 8): one subscriber style, event-API client registration, registry lookups, one WIP tab list. */
class RegistrationStyleGuardrailTest {
	private static final String SUBSCRIBER = "@EventBusSubscriber(modid = Hemomancy.MOD_ID)";
	private static final String CLIENT_SUBSCRIBER = "@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)";
	private static final Pattern ANNOTATION = Pattern.compile("(?m)^\\s*(@EventBusSubscriber(\\([^)]*\\))?)");

	/** R-1: NeoForge 21.1 picks the bus from the event type and ignores {@code bus=}. */
	@Test
	void everySubscriberUsesOneOfTheTwoCanonicalForms() throws Exception {
		List<String> odd = new ArrayList<>();
		for (Path file : javaFiles(RepoPaths.MAIN_JAVA, RepoPaths.GAMETEST_JAVA)) {
			if (isDormantCompat(file)) {
				continue;
			}
			Matcher matcher = ANNOTATION.matcher(SourceAssertions.read(file));
			while (matcher.find()) {
				String annotation = matcher.group(1);
				if (!annotation.equals(SUBSCRIBER) && !annotation.equals(CLIENT_SUBSCRIBER)) {
					odd.add(RepoPaths.display(file) + ": " + annotation);
				}
			}
		}
		assertEquals(List.of(), odd);
	}

	/** Only mod-lifecycle listeners are added by hand in the mod constructor. */
	@Test
	void theModConstructorAddsOnlyLifecycleListeners() {
		String mod = SourceAssertions.readMainJava("Hemomancy.java");
		String constructor = SourceAssertions.methodBody(mod, "public Hemomancy(IEventBus modEventBus)");
		List<String> listeners = new ArrayList<>();
		Matcher matcher = Pattern.compile("\\w+(?:\\.\\w+)*\\.addListener\\(([^)]*)\\)").matcher(constructor);
		while (matcher.find()) {
			listeners.add(matcher.group(1));
		}
		assertEquals(List.of("this::commonSetup", "DevTestHooks::registerGameTests", "this::onAddReloadListeners"),
				listeners);
		assertFalse(constructor.contains(".register(this)"), "instance subscribers belong in @EventBusSubscriber classes");
		assertFalse(mod.contains("CreativeModeTab"), "creative tabs live in CreativeTabInit");
	}

	/** R-4: client registration goes through the event APIs. */
	@Test
	void clientRegistrationUsesTheEventApis() throws Exception {
		List<String> legacy = new ArrayList<>();
		for (Path file : javaFiles(RepoPaths.MAIN_JAVA)) {
			if (isDormantCompat(file)) {
				continue;
			}
			String text = SourceAssertions.read(file);
			for (String call : List.of("BlockEntityRenderers.register(", "particleEngine.register(", "initializeClient(")) {
				if (text.contains(call)) {
					legacy.add(RepoPaths.display(file) + ": " + call);
				}
			}
		}
		assertEquals(List.of(), legacy);
	}

	/** R-5 / V-8: the client registration god class is split, and client registries left common/init. */
	@Test
	void clientRegistrationLivesInClientRegistry() {
		for (String gone : List.of("client/event/ClientEvents.java", "common/init/ShaderInit.java",
				"common/init/RenderTypeInit.java", "common/worldgen/feature/FungalSkyBoxRenderer.java")) {
			assertFalse(Files.exists(RepoPaths.mainJava(gone)), gone + " should stay split up or moved");
		}
		String particles = SourceAssertions.readMainJava("common/init/ParticleInit.java");
		assertFalse(particles.contains("EventBusSubscriber"), "particle providers register in client/registry");
		assertTrue(SourceAssertions.readMainJava("client/registry/ParticleProviderRegistrar.java")
				.contains("RegisterParticleProvidersEvent"));
	}

	/** R-3: custom registries are looked up through the registry, keeping the name strings. */
	@Test
	void customRegistriesAreLookedUpThroughTheRegistry() {
		for (String init : List.of("common/init/ManipulationInit.java", "common/init/StillArtInit.java")) {
			String body = SourceAssertions.methodBody(SourceAssertions.readMainJava(init), "getByName(String name)");
			assertTrue(body.contains("RegistryLookup.byName("), init + " should look the entry up in its registry");
			assertFalse(body.contains("for ("), init + " should not scan its entries");
		}
	}

	/** R-7: data components use the typed register, with no hand-built builder chains. */
	@Test
	void dataComponentsUseTheTypedRegister() {
		String components = SourceAssertions.readMainJava("common/init/DataComponentInit.java");
		assertTrue(components.contains("DeferredRegister.DataComponents COMPONENTS"));
		assertFalse(components.contains("DataComponentType.<"), "use registerComponentType, not builder chains");
		assertFalse(components.contains(".build()"), "registerComponentType builds the type");
	}

	/** R-2: one ordered WIP list drives both the WIP tab and the main tab's exclusion. */
	@Test
	void oneWipListDrivesBothTabs() throws Exception {
		String tabs = SourceAssertions.readMainJava("common/init/CreativeTabInit.java");
		assertEquals(1, count(tabs, "List.of("), "CreativeTabInit holds exactly one list: the WIP list");
		assertFalse(SourceAssertions.readMainJava("common/init/BlockInit.java").contains("WIP_BLOCKS"),
				"BlockInit reads the WIP list instead of keeping its own");
		List<String> builders = new ArrayList<>();
		for (Path file : javaFiles(RepoPaths.MAIN_JAVA)) {
			if (!isDormantCompat(file) && SourceAssertions.read(file).contains("BuildCreativeModeTabContentsEvent")) {
				builders.add(file.getFileName().toString());
			}
		}
		assertEquals(List.of("CreativeTabInit.java"), builders);
	}

	private static int count(String text, String needle) {
		int count = 0;
		for (int at = text.indexOf(needle); at >= 0; at = text.indexOf(needle, at + needle.length())) {
			count++;
		}
		return count;
	}

	private static boolean isDormantCompat(Path file) {
		String path = file.toString().replace('\\', '/');
		return path.contains("/compat/mna/") || path.contains("/compat/curios/");
	}

	private static List<Path> javaFiles(Path... roots) throws Exception {
		List<Path> all = new ArrayList<>();
		for (Path root : roots) {
			try (Stream<Path> files = Files.walk(root)) {
				files.filter(path -> path.toString().endsWith(".java")).sorted().forEach(all::add);
			}
		}
		return all;
	}
}
