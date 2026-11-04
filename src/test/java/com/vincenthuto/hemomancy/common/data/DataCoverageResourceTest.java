package com.vincenthuto.hemomancy.common.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.ResourceReader;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Data and assets that resolve (cleanup audit A-22, A-23, A-24): a hidden marker advancement has no display, so it
 * never opens a tab of its own; every structure tag the code looks up exists; every model face names a texture the
 * model declares; every liquid block has a blockstate; every sound event's files exist; every particle JSON has a
 * registered type.
 */
class DataCoverageResourceTest {
	private static final Pattern STRUCTURE_TAG = Pattern.compile(
			"TagKey\\.create\\(Registries\\.STRUCTURE,\\s*Hemomancy\\.rloc\\(\"([a-z0-9_/]+)\"\\)");

	@Test
	void hiddenMarkerAdvancementsHaveNoDisplay() throws Exception {
		List<String> wrong = new ArrayList<>();
		try (Stream<Path> files = Files.list(RepoPaths.DATA.resolve("advancement/hemomancy"))) {
			for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
				JsonObject advancement = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
				boolean root = !advancement.has("parent");
				boolean marker = advancement.getAsJsonObject("criteria").entrySet().stream().allMatch(criterion ->
						"minecraft:impossible".equals(criterion.getValue().getAsJsonObject().get("trigger").getAsString()));
				if (root && marker && advancement.has("display") && !advancement.getAsJsonObject("display").has("background")) {
					wrong.add(file.getFileName().toString());
				}
			}
		}
		assertEquals(List.of(), wrong, "root marker advancements with a display open their own tab");
	}

	@Test
	void everyStructureTagTheCodeLooksUpExists() throws Exception {
		TreeSet<String> missing = new TreeSet<>();
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				Matcher matcher = STRUCTURE_TAG.matcher(SourceAssertions.read(file));
				while (matcher.find()) {
					if (!ResourceReader.exists("data/hemomancy/tags/worldgen/structure/" + matcher.group(1) + ".json")) {
						missing.add(matcher.group(1));
					}
				}
			}
		}
		assertEquals(new TreeSet<>(), missing, "structure tags with no file");
	}

	@Test
	void everyModelFaceNamesADeclaredTexture() throws Exception {
		List<String> wrong = new ArrayList<>();
		// A model that other models extend is a template: its children supply the textures it leaves open.
		TreeSet<String> templates = new TreeSet<>();
		try (Stream<Path> files = Files.walk(RepoPaths.ASSETS.resolve("models"))) {
			for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
				JsonObject model = JsonParser.parseString(Files.readString(file).replace("﻿", "")).getAsJsonObject();
				if (model.has("parent")) templates.add(model.get("parent").getAsString().replace("hemomancy:", ""));
			}
		}
		try (Stream<Path> files = Files.walk(RepoPaths.ASSETS.resolve("models"))) {
			for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
				JsonObject model = JsonParser.parseString(Files.readString(file).replace("﻿", "")).getAsJsonObject();
				if (!model.has("elements")) continue;
				String id = RepoPaths.ASSETS.resolve("models").relativize(file).toString().replace('\\', '/').replace(".json", "");
				if (templates.contains(id)) continue;
				JsonObject textures = model.has("textures") ? model.getAsJsonObject("textures") : new JsonObject();
				TreeSet<String> bad = new TreeSet<>();
				for (JsonElement element : model.getAsJsonArray("elements")) {
					if (!element.getAsJsonObject().has("faces")) continue;
					for (var face : element.getAsJsonObject().getAsJsonObject("faces").entrySet()) {
						String texture = face.getValue().getAsJsonObject().get("texture").getAsString();
						if (texture.startsWith("#") && !textures.has(texture.substring(1))) bad.add(texture);
					}
				}
				if (!bad.isEmpty()) wrong.add(RepoPaths.display(file) + " " + bad);
			}
		}
		assertEquals(List.of(), wrong, "model faces with an unresolved texture");
	}

	@Test
	void everyLiquidBlockHasABlockstate() {
		String blocks = SourceAssertions.readMainJava("common/init/BlockInit.java");
		Matcher matcher = Pattern.compile("LIQUIDBLOCKS\\.register\\(\"([a-z_]+)\"").matcher(blocks);
		TreeSet<String> missing = new TreeSet<>();
		while (matcher.find()) {
			if (!ResourceReader.exists("assets/hemomancy/blockstates/" + matcher.group(1) + ".json")) missing.add(matcher.group(1));
		}
		assertEquals(new TreeSet<>(), missing, "liquid blocks with no blockstate");
	}

	@Test
	void everySoundEventFileExists() {
		JsonObject sounds = ResourceReader.asset("sounds.json");
		TreeSet<String> missing = new TreeSet<>();
		for (var event : sounds.entrySet()) {
			for (JsonElement sound : event.getValue().getAsJsonObject().getAsJsonArray("sounds")) {
				String name = sound.isJsonObject() ? sound.getAsJsonObject().get("name").getAsString() : sound.getAsString();
				if (!name.startsWith("hemomancy:")) continue;
				String path = name.substring(name.indexOf(':') + 1);
				if (!ResourceReader.exists("assets/hemomancy/sounds/" + path + ".ogg")) missing.add(event.getKey() + " -> " + name);
			}
		}
		assertEquals(new TreeSet<>(), missing, "sound events whose file does not exist");
	}

	@Test
	void everyParticleJsonHasARegisteredType() throws Exception {
		String init = SourceAssertions.readMainJava("common/init/ParticleInit.java");
		TreeSet<String> registered = new TreeSet<>();
		Matcher matcher = Pattern.compile("register\\(\\s*\"([a-z_]+)\"").matcher(init);
		while (matcher.find()) registered.add(matcher.group(1));
		TreeSet<String> orphan = new TreeSet<>();
		try (Stream<Path> files = Files.list(RepoPaths.ASSETS.resolve("particles"))) {
			for (Path file : files.toList()) {
				String name = file.getFileName().toString().replace(".json", "");
				if (!registered.contains(name)) orphan.add(name);
			}
		}
		assertEquals(new TreeSet<>(), orphan, "particle JSON with no registered type");
	}
}
