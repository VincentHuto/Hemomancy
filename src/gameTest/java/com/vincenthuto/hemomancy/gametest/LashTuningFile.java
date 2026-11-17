package com.vincenthuto.hemomancy.gametest;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.LashTuning;

import java.io.IOException;
import java.lang.reflect.RecordComponent;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * {@code lash-tuning.json}: every number of {@link LashTuning} by its own name, read back whenever the file changes so
 * the lash can be tuned while the game runs. A number the file leaves out keeps its default; a file that does not
 * parse changes nothing and says why. Excluded from release jars.
 */
final class LashTuningFile {
	private final Path file;
	private long seen = Long.MIN_VALUE;

	LashTuningFile(Path gameDirectory) {
		this.file = gameDirectory.resolve("lash-tuning.json");
	}

	Path path() {
		return file;
	}

	/** What a poll found: new numbers to use, an error to show, or neither when the file has not changed. */
	record Poll(LashTuning tuning, String error) {
		static final Poll UNCHANGED = new Poll(null, null);
	}

	/** Writes the defaults if there is no file yet, then reads the file if it changed since the last poll. */
	Poll poll() {
		try {
			if (!Files.exists(file)) write(LashTuning.DEFAULT);
			long modified = Files.getLastModifiedTime(file).toMillis();
			if (modified == seen) return Poll.UNCHANGED;
			seen = modified;
			return new Poll(read(Files.readString(file, StandardCharsets.UTF_8)), null);
		} catch (IOException | RuntimeException | ReflectiveOperationException problem) {
			return new Poll(null, problem.getClass().getSimpleName() + ": " + problem.getMessage());
		}
	}

	private void write(LashTuning tuning) throws IOException {
		JsonObject json = new JsonObject();
		try {
			for (RecordComponent part : LashTuning.class.getRecordComponents()) {
				json.addProperty(part.getName(), (Number) part.getAccessor().invoke(tuning));
			}
		} catch (ReflectiveOperationException impossible) {
			throw new IOException(impossible);
		}
		Files.writeString(file, new GsonBuilder().setPrettyPrinting().create().toJson(json) + "\n", StandardCharsets.UTF_8);
	}

	static LashTuning read(String text) throws ReflectiveOperationException {
		JsonObject json = JsonParser.parseString(text).getAsJsonObject();
		RecordComponent[] parts = LashTuning.class.getRecordComponents();
		Class<?>[] types = new Class<?>[parts.length];
		Object[] values = new Object[parts.length];
		for (int i = 0; i < parts.length; i++) {
			types[i] = parts[i].getType();
			Number fallback = (Number) parts[i].getAccessor().invoke(LashTuning.DEFAULT);
			Number given = json.has(parts[i].getName()) ? json.get(parts[i].getName()).getAsNumber() : fallback;
			if (!Double.isFinite(given.doubleValue())) throw new IllegalArgumentException(parts[i].getName() + " is not a number");
			values[i] = types[i] == int.class ? (Object) given.intValue() : (Object) given.doubleValue();
		}
		return LashTuning.class.getDeclaredConstructor(types).newInstance(values);
	}
}
