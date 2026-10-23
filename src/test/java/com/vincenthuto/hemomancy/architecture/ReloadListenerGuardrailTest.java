package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * R-6: server data loaders extend {@code JsonResourceReloadListener} and are registered only in
 * {@code Hemomancy.onAddReloadListeners}; remote clients receive server data through {@code DatapackSyncChannels}.
 */
class ReloadListenerGuardrailTest {
	private static final Map<String, String> OWN_PREPARE = new TreeMap<>(Map.of(
			"BloodInjectionData.java", "validates the whole snapshot in sorted order (origin conflicts, sync limits)",
			"BloodProfileData.java", "validates the whole snapshot in sorted order against cumulative sync limits",
			"ClairaudiographCatalogue.java", "resolves duplicate creatures across files, lexically first wins"));

	@Test
	void loadersExtendTheSharedBase() throws Exception {
		List<String> own = new ArrayList<>();
		forEachCommonSource((name, text) -> {
			if (text.contains("extends SimplePreparableReloadListener") && !name.equals("JsonResourceReloadListener.java"))
				own.add(name);
		});
		assertEquals(List.copyOf(OWN_PREPARE.keySet()), own.stream().sorted().toList(),
				"Extend JsonResourceReloadListener, or list the loader here with its reason");
	}

	@Test
	void listenersRegisterInOnePlaceAndSyncThroughTheChannelTable() throws Exception {
		List<String> registrars = new ArrayList<>();
		List<String> syncs = new ArrayList<>();
		forEachCommonSource((name, text) -> {
			if (text.contains("AddReloadListenerEvent")) registrars.add(name);
			if (text.contains("OnDatapackSyncEvent")) syncs.add(name);
		});
		assertEquals(List.of(), registrars, "Register reload listeners in Hemomancy.onAddReloadListeners");
		assertEquals(List.of("AdvancedBrewingReload.java", "DatapackSyncChannels.java"), syncs.stream().sorted().toList(),
				"Send server data to clients as a DatapackSyncChannels channel");
	}

	private interface Visitor {
		void visit(String name, String text);
	}

	private static void forEachCommonSource(Visitor visitor) throws Exception {
		try (Stream<Path> files = Files.walk(RepoPaths.mainJava("common"))) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				visitor.visit(file.getFileName().toString(), SourceAssertions.read(file));
			}
		}
	}
}
