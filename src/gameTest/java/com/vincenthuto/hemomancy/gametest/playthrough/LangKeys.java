package com.vincenthuto.hemomancy.gametest.playthrough;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.locale.Language;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Whether a translation key resolves for a player. A key passes when the server's language has it, or when it is in
 * Hemomancy's {@code en_us.json} on the classpath (read once). This reads a classpath resource the mod ships, not a
 * repository file.
 */
public final class LangKeys {
	private static final String RESOURCE = "/assets/hemomancy/lang/en_us.json";
	private static Set<String> shipped;

	private LangKeys() {
	}

	public static boolean exists(String key) {
		if (key == null || key.isBlank()) return false;
		return Language.getInstance().has(key) || shipped().contains(key);
	}

	/** Every key in the shipped {@code en_us.json}. */
	public static synchronized Set<String> shipped() {
		if (shipped == null) {
			try (InputStream in = LangKeys.class.getResourceAsStream(RESOURCE)) {
				if (in == null) throw new IllegalStateException(RESOURCE + " is not on the classpath");
				JsonObject json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
				shipped = Set.copyOf(json.keySet());
			} catch (IOException exception) {
				throw new IllegalStateException("Could not read " + RESOURCE, exception);
			}
		}
		return shipped;
	}
}
