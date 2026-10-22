package com.vincenthuto.hemomancy.support;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.file.Files;
import java.nio.file.Path;

/** Reads mod resources under {@code src/main/resources} for data tests. */
public final class ResourceReader {
	private ResourceReader() {
	}

	/** A resource path such as {@code "assets/hemomancy/lang/en_us.json"}. */
	public static Path path(String resourcePath) {
		return RepoPaths.MAIN_RESOURCES.resolve(resourcePath);
	}

	public static boolean exists(String resourcePath) {
		return Files.isRegularFile(path(resourcePath));
	}

	/** Strict UTF-8 text with {@code \n} line endings. A missing resource fails the test. */
	public static String text(String resourcePath) {
		return SourceAssertions.read(path(resourcePath));
	}

	public static JsonElement json(String resourcePath) {
		return JsonParser.parseString(text(resourcePath));
	}

	public static JsonObject jsonObject(String resourcePath) {
		return json(resourcePath).getAsJsonObject();
	}

	/** A path under {@code assets/hemomancy}, such as {@code "models/item/vial.json"}. */
	public static JsonObject asset(String assetPath) {
		return jsonObject("assets/hemomancy/" + assetPath);
	}

	/** A path under {@code data/hemomancy}, such as {@code "recipe/vial.json"}. */
	public static JsonObject data(String dataPath) {
		return jsonObject("data/hemomancy/" + dataPath);
	}
}
