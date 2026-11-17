package com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.common.data.JsonResourceReloadListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * The catalyst table: one file per school in {@code data/<ns>/temper_catalyst/<school>.json}, listing the items that
 * dissolve into that school's Temper and what each is worth (an enzyme 10, a school's spores 5, its vanilla reagent
 * 3). A cast short of Temper dissolves the smallest one it carries that covers the shortfall; the off hand wins a tie,
 * then the earlier inventory slot. Server data; an item
 * named by two schools keeps the first file's entry, in file order.
 */
public final class TemperCatalysts extends JsonResourceReloadListener<TemperCatalysts.SchoolFile> {
	public static final String FOLDER = "temper_catalyst";
	/** The off hand, as a slot in {@link Carried}. */
	public static final int OFF_HAND = -1;

	public record Entry(ResourceLocation item, float worth) {
		static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ResourceLocation.CODEC.fieldOf("item").forGetter(Entry::item),
				Codec.floatRange(0.01F, 1000.0F).fieldOf("worth").forGetter(Entry::worth)).apply(instance, Entry::new));
	}

	public record SchoolFile(String temper, List<Entry> catalysts) {
		static final Codec<SchoolFile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("temper").forGetter(SchoolFile::temper),
				Entry.CODEC.listOf().fieldOf("catalysts").forGetter(SchoolFile::catalysts)).apply(instance, SchoolFile::new));
	}

	/** One catalyst: the school it feeds and what one item is worth. */
	public record Catalyst(EnumBloodTemper school, float worth) {
	}

	/** A catalyst the player carries: where it is ({@link #OFF_HAND} or an inventory slot) and what it is. */
	public record Carried(int slot, Item item, Catalyst catalyst) {
	}

	private static volatile Map<Item, Catalyst> current = Map.of();

	public TemperCatalysts() {
		super(FOLDER, "Temper catalyst", LogManager.getLogger(), TemperCatalysts::parse, TemperCatalysts::publish);
	}

	private static SchoolFile parse(ResourceLocation id, com.google.gson.JsonObject root) {
		SchoolFile file = decoding(SchoolFile.CODEC).apply(id, root);
		EnumBloodTemper school = school(file.temper());
		if (!id.getPath().equals(school.name().toLowerCase(Locale.ROOT)))
			throw new IllegalArgumentException("A catalyst file is named for its school: " + id);
		for (Entry entry : file.catalysts()) {
			if (!BuiltInRegistries.ITEM.containsKey(entry.item()))
				throw new IllegalArgumentException("Unknown catalyst item " + entry.item());
		}
		return file;
	}

	private static void publish(Map<ResourceLocation, SchoolFile> loaded) {
		Map<Item, Catalyst> byItem = new HashMap<>();
		new TreeMap<>(loaded).values().forEach(file -> {
			EnumBloodTemper school = school(file.temper());
			for (Entry entry : file.catalysts()) {
				byItem.putIfAbsent(BuiltInRegistries.ITEM.get(entry.item()), new Catalyst(school, entry.worth()));
			}
		});
		current = Map.copyOf(byItem);
	}

	static EnumBloodTemper school(String id) {
		return EnumBloodTemper.valueOf(ResourceLocation.parse(id).getPath().toUpperCase(Locale.ROOT));
	}

	/** The catalyst {@code stack} is, or null. */
	@Nullable
	public static Catalyst of(ItemStack stack) {
		return stack == null || stack.isEmpty() ? null : current.get(stack.getItem());
	}

	/** Every loaded catalyst, for teaching surfaces and tests. */
	public static Map<Item, Catalyst> all() {
		return current;
	}

	/**
	 * The catalyst of {@code school} to spend on a short pool (CB-19): of those the player carries, the smallest worth
	 * that still lets {@code held} meet {@code cost} under {@code cap}. The off hand wins a tie, then the earlier
	 * inventory slot. Null when none covers the shortfall.
	 */
	@Nullable
	public static Carried find(Player player, EnumBloodTemper school, float held, float cost, float cap) {
		if (player == null || school == null) return null;
		Carried best = smaller(null, OFF_HAND, player.getOffhandItem(), school, held, cost, cap);
		List<ItemStack> items = player.getInventory().items;
		for (int slot = 0; slot < items.size(); slot++) {
			best = smaller(best, slot, items.get(slot), school, held, cost, cap);
		}
		return best;
	}

	@Nullable
	private static Carried smaller(@Nullable Carried best, int slot, ItemStack stack, EnumBloodTemper school,
			float held, float cost, float cap) {
		Catalyst catalyst = of(stack);
		if (catalyst == null || catalyst.school() != school
				|| TemperRules.plan(held, cost, catalyst.worth(), cap).outcome() != TemperRules.Outcome.CATALYST
				|| best != null && best.catalyst().worth() <= catalyst.worth()) return best;
		return new Carried(slot, stack.getItem(), catalyst);
	}

	/**
	 * Dissolves the carried catalyst: one item from where {@link #find} saw it, if it is still there. A Creative
	 * player keeps the item. Returns whether it was there.
	 */
	public static boolean dissolve(Player player, Carried carried) {
		ItemStack stack = carried.slot() == OFF_HAND ? player.getOffhandItem()
				: carried.slot() < player.getInventory().items.size() ? player.getInventory().items.get(carried.slot()) : ItemStack.EMPTY;
		if (stack.isEmpty() || !stack.is(carried.item())) return false;
		if (!player.getAbilities().instabuild) stack.shrink(1);
		return true;
	}
}
