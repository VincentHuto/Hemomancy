package com.vincenthuto.hemomancy.client.screen.dialogue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.fml.loading.FMLEnvironment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Temporary dev-only playtest tool: records which translation keys the dialogue screen drew
 * last frame, and rewrites a key's value in the source en_us.json, showing it at once through
 * an override layer over the loaded language.
 */
public final class DialogueLineEditor {
	private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
	private static final Map<String, String> OVERRIDES = new HashMap<>();
	private static final Set<String> RECORDING = new LinkedHashSet<>();
	private static List<String> lastFrameKeys = List.of();
	private static boolean recording;

	private DialogueLineEditor() {
	}

	public static boolean available() {
		return !FMLEnvironment.production;
	}

	public static void beginFrame() {
		if (!available()) return;
		reapplyOverrides();
		RECORDING.clear();
		recording = true;
	}

	public static void endFrame() {
		if (!recording) return;
		recording = false;
		lastFrameKeys = new ArrayList<>(RECORDING);
	}

	/** Called from the TranslatableContents mixin for every translated text drawn. */
	public static void record(String key) {
		if (recording) RECORDING.add(key);
	}

	public static List<String> lastFrameKeys() {
		return lastFrameKeys;
	}

	public static String current(String key) {
		return Language.getInstance().getOrDefault(key, key);
	}

	/** Writes the new value into src/main/resources en_us.json and shows it in game at once. */
	public static Path save(String key, String value) throws IOException {
		Path file = langFile();
		String text = Files.readString(file, StandardCharsets.UTF_8);
		String keyToken = GSON.toJson(key);
		int at = text.indexOf(keyToken + ":");
		if (at < 0) at = text.indexOf(keyToken + " :");
		if (at < 0) throw new IOException("Key not in " + file + ": " + key);
		int start = text.indexOf('"', text.indexOf(':', at + keyToken.length()));
		int end = start + 1;
		while (text.charAt(end) != '"') end += text.charAt(end) == '\\' ? 2 : 1;
		String updated = text.substring(0, start) + GSON.toJson(value) + text.substring(end + 1);
		Files.writeString(file, updated, StandardCharsets.UTF_8);
		OVERRIDES.put(key, value);
		Language.inject(new Overlay(base()));
		return file;
	}

	private static void reapplyOverrides() {
		if (!OVERRIDES.isEmpty() && !(Language.getInstance() instanceof Overlay)) {
			Language.inject(new Overlay(Language.getInstance()));
		}
	}

	private static Language base() {
		Language language = Language.getInstance();
		return language instanceof Overlay overlay ? overlay.base : language;
	}

	private static Path langFile() throws IOException {
		Path rel = Path.of("src", "main", "resources", "assets", Hemomancy.MOD_ID, "lang", "en_us.json");
		for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
			Path candidate = dir.resolve(rel);
			if (Files.isRegularFile(candidate)) return candidate;
		}
		throw new IOException("Cannot find " + rel + " above the run directory");
	}

	private static final class Overlay extends Language {
		private final Language base;

		private Overlay(Language base) {
			this.base = base;
		}

		@Override
		public String getOrDefault(String key, String fallback) {
			String value = OVERRIDES.get(key);
			return value != null ? value : base.getOrDefault(key, fallback);
		}

		@Override
		public boolean has(String key) {
			return OVERRIDES.containsKey(key) || base.has(key);
		}

		@Override
		public boolean isDefaultRightToLeft() {
			return base.isDefaultRightToLeft();
		}

		@Override
		public FormattedCharSequence getVisualOrder(FormattedText text) {
			return base.getVisualOrder(text);
		}
	}
}
