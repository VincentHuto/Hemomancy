package com.vincenthuto.hemomancy.common.world.ground;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.TemperCatalysts;
import com.vincenthuto.hemomancy.common.data.JsonResourceReloadListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * The gouge catalyst table: one file per function in {@code data/<ns>/gouge_catalyst/<function>.json}. A file names
 * the school whose Temper catalysts give the function ({@code temper}, read through {@link TemperCatalysts}, each item
 * lighting the run for {@code ticks_per_worth} times its worth) and any further items with their own time
 * ({@code items}). New functions for a catalyst are content: a file, not code. Server data; an item named twice keeps
 * the first file's entry, in file order, and a named item wins over a school's catalyst.
 *
 * <p>Remote clients mirror the table through the {@code hemomancy:gouge_catalysts} datapack sync channel
 * ({@link #syncSnapshot}, {@link #receiveSync}), so a catalyst feeder's slot predicts on the client what the server
 * accepts. The snapshot is resolved: every school catalyst is listed by item, since the client has no Temper table.
 */
public final class GougeCatalysts extends JsonResourceReloadListener<GougeCatalysts.FunctionFile> {
	public static final String FOLDER = "gouge_catalyst";
	/** One synced definition per function at most. */
	public static final int MAX_SYNCED_FUNCTIONS = 16;
	public static final int MAX_SYNCED_JSON_LENGTH = 32_767;

	public record Entry(ResourceLocation item, int ticks) {
		static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ResourceLocation.CODEC.fieldOf("item").forGetter(Entry::item),
				Codec.intRange(1, 1_000_000).fieldOf("ticks").forGetter(Entry::ticks)).apply(instance, Entry::new));
	}

	public record FunctionFile(String function, Optional<String> temper, int ticksPerWorth, List<Entry> items) {
		static final Codec<FunctionFile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("function").forGetter(FunctionFile::function),
				Codec.STRING.optionalFieldOf("temper").forGetter(FunctionFile::temper),
				Codec.intRange(0, 100_000).optionalFieldOf("ticks_per_worth", 0).forGetter(FunctionFile::ticksPerWorth),
				Entry.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(FunctionFile::items))
				.apply(instance, FunctionFile::new));
	}

	/** What one catalyst does to a run: its function and how long the run stays lit. */
	public record Catalyst(GroundFunction function, int ticks) {
	}

	public record SchoolRate(GroundFunction function, int ticksPerWorth) {
	}

	private static volatile Map<Item, Catalyst> named = Map.of();
	private static volatile Map<EnumBloodTemper, SchoolRate> bySchool = Map.of();

	public GougeCatalysts() {
		super(FOLDER, "Gouge catalyst", LogManager.getLogger(), GougeCatalysts::parse, GougeCatalysts::publish);
	}

	private static FunctionFile parse(ResourceLocation id, com.google.gson.JsonObject root) {
		FunctionFile file = decoding(FunctionFile.CODEC).apply(id, root);
		GroundFunction function = GroundFunction.byName(file.function());
		if (function == null || function == GroundFunction.NONE)
			throw new IllegalArgumentException("Unknown gouge function " + file.function() + " in " + id);
		if (!id.getPath().equals(function.getSerializedName()))
			throw new IllegalArgumentException("A gouge catalyst file is named for its function: " + id);
		file.temper().ifPresent(GougeCatalysts::school);
		if (file.temper().isPresent() && file.ticksPerWorth() <= 0)
			throw new IllegalArgumentException("A school's catalysts need ticks_per_worth: " + id);
		for (Entry entry : file.items()) {
			if (!BuiltInRegistries.ITEM.containsKey(entry.item()))
				throw new IllegalArgumentException("Unknown gouge catalyst item " + entry.item());
		}
		return file;
	}

	private static void publish(Map<ResourceLocation, FunctionFile> loaded) {
		Map<Item, Catalyst> items = new HashMap<>();
		Map<EnumBloodTemper, SchoolRate> schools = new EnumMap<>(EnumBloodTemper.class);
		new TreeMap<>(loaded).values().forEach(file -> {
			GroundFunction function = GroundFunction.byName(file.function());
			for (Entry entry : file.items()) {
				items.putIfAbsent(BuiltInRegistries.ITEM.get(entry.item()), new Catalyst(function, entry.ticks()));
			}
			file.temper().ifPresent(id -> schools.putIfAbsent(school(id), new SchoolRate(function, file.ticksPerWorth())));
		});
		named = Map.copyOf(items);
		bySchool = Map.copyOf(schools);
	}

	static EnumBloodTemper school(String id) {
		return EnumBloodTemper.valueOf(ResourceLocation.parse(id).getPath().toUpperCase(Locale.ROOT));
	}

	/** What {@code stack} does dissolved into a run, or null when it is no gouge catalyst. */
	@Nullable
	public static Catalyst of(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return null;
		Catalyst catalyst = named.get(stack.getItem());
		if (catalyst != null) return catalyst;
		TemperCatalysts.Catalyst temper = TemperCatalysts.of(stack);
		if (temper == null) return null;
		SchoolRate rate = bySchool.get(temper.school());
		return rate == null ? null : new Catalyst(rate.function(), Math.round(rate.ticksPerWorth() * temper.worth()));
	}

	/**
	 * Every item that gives each function, as the server reads it: named items, then each school catalyst read through
	 * {@link TemperCatalysts} where that table is loaded, timed as {@link #of} times it. Sorted by item id within a
	 * function. The sync snapshot and the JEI category both read this.
	 */
	public static Map<GroundFunction, List<Entry>> listed() {
		Map<Item, Catalyst> resolved = new HashMap<>(named);
		Map<EnumBloodTemper, SchoolRate> schools = bySchool;
		TemperCatalysts.all().forEach((item, temper) -> {
			SchoolRate rate = schools.get(temper.school());
			if (rate != null && !resolved.containsKey(item)) {
				resolved.put(item, new Catalyst(rate.function(), Math.round(rate.ticksPerWorth() * temper.worth())));
			}
		});
		Map<GroundFunction, List<Entry>> byFunction = new EnumMap<>(GroundFunction.class);
		resolved.forEach((item, catalyst) -> byFunction.computeIfAbsent(catalyst.function(), function -> new java.util.ArrayList<>())
				.add(new Entry(BuiltInRegistries.ITEM.getKey(item), Math.max(1, catalyst.ticks()))));
		Map<GroundFunction, List<Entry>> sorted = new EnumMap<>(GroundFunction.class);
		byFunction.forEach((function, entries) -> {
			entries.sort(java.util.Comparator.comparing(entry -> entry.item().toString()));
			sorted.put(function, List.copyOf(entries));
		});
		return sorted;
	}

	/**
	 * The table as remote clients need it: one definition per function, keyed {@code hemomancy:<function>} so it passes
	 * the file-name rule, with every item that gives the function listed outright ({@link #listed}) and no school.
	 */
	public static Map<ResourceLocation, String> syncSnapshot() {
		Map<ResourceLocation, String> json = new TreeMap<>();
		listed().forEach((function, entries) -> {
			FunctionFile file = new FunctionFile(function.getSerializedName(), Optional.empty(), 0, entries);
			json.put(com.vincenthuto.hemomancy.Hemomancy.rloc(function.getSerializedName()),
					FunctionFile.CODEC.encodeStart(JsonOps.INSTANCE, file).getOrThrow().toString());
		});
		return json;
	}

	/**
	 * Run on a remote client after the server's table arrives, so a view built before it (the JEI category) can be
	 * rebuilt. Set and cleared by that view; common code names no client class.
	 */
	public static volatile Runnable onClientTable;

	/** Replaces this client's table with the server's. Malformed entries are skipped, not trusted. */
	public static void receiveSync(Map<ResourceLocation, String> synced) {
		Map<ResourceLocation, FunctionFile> parsed = new HashMap<>();
		synced.forEach((id, text) -> {
			try {
				parsed.put(id, parse(id, JsonParser.parseString(text).getAsJsonObject()));
			} catch (RuntimeException exception) {
				LogManager.getLogger().warn("Ignoring synced gouge catalyst {}: {}", id, exception.getMessage());
			}
		});
		publish(parsed);
		Runnable hook = onClientTable;
		if (hook != null) hook.run();
	}

	/** The live tables, so a test that replaces them can put them back. */
	public record Tables(Map<Item, Catalyst> named, Map<EnumBloodTemper, SchoolRate> bySchool) {
	}

	/** The tables as they stand; for tests. */
	public static Tables tables() {
		return new Tables(named, bySchool);
	}

	/** Puts back tables taken with {@link #tables}; for tests. */
	public static void restore(Tables tables) {
		named = tables.named();
		bySchool = tables.bySchool();
	}

	/** Every item named outright, for tests and teaching surfaces. */
	public static Map<Item, Catalyst> named() {
		return named;
	}
}
