package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.data.JsonResourceReloadListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * The meat table (core-loop Track 10): files in {@code data/<ns>/blood_food/}, each naming items, or an item tag, and
 * what one point of their nutrition is worth in blood to a Harbinger who eats them. A file may add a little Temper of
 * one school (Hemomancy's own meats). A named item wins over a tag; between tags, the first file in id order wins.
 * Server data, read when a player finishes eating.
 */
public final class BloodFoods extends JsonResourceReloadListener<BloodFoods.FoodFile> {
	public static final String FOLDER = "blood_food";

	public record FoodFile(List<ResourceLocation> items, Optional<ResourceLocation> tag, double mlPerNutrition,
			Optional<String> temper, float temperAmount) {
		static final Codec<FoodFile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ResourceLocation.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(FoodFile::items),
				ResourceLocation.CODEC.optionalFieldOf("tag").forGetter(FoodFile::tag),
				Codec.doubleRange(0.0D, 1000.0D).fieldOf("ml_per_nutrition").forGetter(FoodFile::mlPerNutrition),
				Codec.STRING.optionalFieldOf("temper").forGetter(FoodFile::temper),
				Codec.floatRange(0.0F, 80.0F).optionalFieldOf("temper_amount", 0.0F).forGetter(FoodFile::temperAmount))
				.apply(instance, FoodFile::new));
	}

	/** What one food is worth: blood per point of nutrition, and the Temper it carries (school null for none). */
	public record Food(double mlPerNutrition, @Nullable EnumBloodTemper temper, float temperAmount) {
	}

	private record TagFood(TagKey<Item> tag, Food food) {
	}

	private static volatile Map<Item, Food> byItem = Map.of();
	private static volatile List<TagFood> byTag = List.of();

	public BloodFoods() {
		super(FOLDER, "Blood food", LogManager.getLogger(), BloodFoods::parse, BloodFoods::publish);
	}

	private static FoodFile parse(ResourceLocation id, com.google.gson.JsonObject root) {
		FoodFile file = decoding(FoodFile.CODEC).apply(id, root);
		if (file.items().isEmpty() == file.tag().isEmpty())
			throw new IllegalArgumentException("A blood food file names items or a tag, not both and not neither: " + id);
		for (ResourceLocation item : file.items()) {
			if (!BuiltInRegistries.ITEM.containsKey(item)) throw new IllegalArgumentException("Unknown food item " + item);
		}
		file.temper().ifPresent(BloodFoods::school);
		return file;
	}

	private static void publish(Map<ResourceLocation, FoodFile> loaded) {
		Map<Item, Food> items = new HashMap<>();
		List<TagFood> tags = new ArrayList<>();
		new TreeMap<>(loaded).values().forEach(file -> {
			Food food = new Food(file.mlPerNutrition(), file.temper().map(BloodFoods::school).orElse(null), file.temperAmount());
			for (ResourceLocation item : file.items()) items.putIfAbsent(BuiltInRegistries.ITEM.get(item), food);
			file.tag().ifPresent(tag -> tags.add(new TagFood(TagKey.create(Registries.ITEM, tag), food)));
		});
		byItem = Map.copyOf(items);
		byTag = List.copyOf(tags);
	}

	static EnumBloodTemper school(String id) {
		return EnumBloodTemper.valueOf(ResourceLocation.parse(id).getPath().toUpperCase(Locale.ROOT));
	}

	/** What {@code stack} is worth as a meal, or null when it is not on the table. */
	@Nullable
	public static Food of(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return null;
		Food named = byItem.get(stack.getItem());
		if (named != null) return named;
		for (TagFood tagged : byTag) {
			if (stack.is(tagged.tag())) return tagged.food();
		}
		return null;
	}

	/** Every named item on the table, for tests and teaching surfaces. */
	public static Map<Item, Food> named() {
		return byItem;
	}
}
