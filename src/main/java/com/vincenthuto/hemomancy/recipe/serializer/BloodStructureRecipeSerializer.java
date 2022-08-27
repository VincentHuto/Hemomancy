package com.vincenthuto.hemomancy.recipe.serializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.vincenthuto.hemomancy.recipe.BloodStructureRecipe;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class BloodStructureRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>>
		implements RecipeSerializer<BloodStructureRecipe> {
	private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	@Override
	public BloodStructureRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
		float cost = GsonHelper.getAsFloat(pJson, "bloodCount");
		Map<String, Ingredient> keyMap = keyFromJson(GsonHelper.getAsJsonObject(pJson, "key"));
		String[][] pattern = patternFromJson(GsonHelper.getAsJsonArray(pJson, "pattern"));
		ItemStack result;
		if (pJson.get("result").isJsonObject())
			result = itemStackFromJson(GsonHelper.getAsJsonObject(pJson, "result"), true, true);
		return null;
	}

	public static void formStructureFromJson(String[][] pPattern, JsonObject pJson) {
		for (int i = 0; i < pPattern.length; ++i) {
			for (int j = 0; j < pPattern[i].length; ++j) {

			}
		}
	}

	@Override
	public BloodStructureRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
		return null;
	}

	@Override
	public void toNetwork(FriendlyByteBuf pBuffer, BloodStructureRecipe pRecipe) {

	}

	/**
	 * Returns a key json object as a Java HashMap.
	 */
	protected static Map<String, Ingredient> keyFromJson(JsonObject pKeyEntry) {
		Map<String, Ingredient> map = Maps.newHashMap();

		for (Entry<String, JsonElement> entry : pKeyEntry.entrySet()) {
			if (entry.getKey().length() != 1) {
				throw new JsonSyntaxException("Invalid key entry: '" + (String) entry.getKey()
						+ "' is an invalid symbol (must be 1 character only).");
			}

			if (" ".equals(entry.getKey())) {
				throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
			}

			map.put(entry.getKey(), Ingredient.fromJson(entry.getValue()));
		}

		map.put(" ", Ingredient.EMPTY);
		return map;
	}

	public static ItemStack itemStackFromJson(JsonObject json, boolean readNBT, boolean disallowsAirInRecipe) {
		String itemName = GsonHelper.getAsString(json, "item");
		Item item = getItem(itemName, disallowsAirInRecipe);
		if (readNBT && json.has("nbt")) {
			CompoundTag nbt = getNBT(json.get("nbt"));
			CompoundTag tmp = new CompoundTag();
			if (nbt.contains("ForgeCaps")) {
				tmp.put("ForgeCaps", nbt.get("ForgeCaps"));
				nbt.remove("ForgeCaps");
			}

			tmp.put("tag", nbt);
			tmp.putString("id", itemName);
			tmp.putInt("Count", GsonHelper.getAsInt(json, "count", 1));

			return ItemStack.of(tmp);
		}

		return new ItemStack(item, GsonHelper.getAsInt(json, "count", 1));
	}

	public static Item getItem(String itemName, boolean disallowsAirInRecipe) {
		ResourceLocation itemKey = new ResourceLocation(itemName);
		if (!ForgeRegistries.ITEMS.containsKey(itemKey))
			throw new JsonSyntaxException("Unknown item '" + itemName + "'");

		Item item = ForgeRegistries.ITEMS.getValue(itemKey);
		if (disallowsAirInRecipe && item == Items.AIR)
			throw new JsonSyntaxException("Invalid item: " + itemName);
		return Objects.requireNonNull(item);
	}

	public static CompoundTag getNBT(JsonElement element) {
		try {
			if (element.isJsonObject())
				return TagParser.parseTag(GSON.toJson(element));
			else
				return TagParser.parseTag(GsonHelper.convertToString(element, "nbt"));
		} catch (CommandSyntaxException e) {
			throw new JsonSyntaxException("Invalid NBT Entry: " + e);
		}
	}

	public static Item itemFromJson(JsonObject pItemObject) {
		String s = GsonHelper.getAsString(pItemObject, "item");
		Item item = Registry.ITEM.getOptional(new ResourceLocation(s)).orElseThrow(() -> {
			return new JsonSyntaxException("Unknown item '" + s + "'");
		});
		if (item == Items.AIR) {
			throw new JsonSyntaxException("Invalid item: " + s);
		} else {
			return item;
		}
	}

	static String[] shrink(String... pToShrink) {
		int i = Integer.MAX_VALUE;
		int j = 0;
		int k = 0;
		int l = 0;

		for (int i1 = 0; i1 < pToShrink.length; ++i1) {
			String s = pToShrink[i1];
			i = Math.min(i, firstNonSpace(s));
			int j1 = lastNonSpace(s);
			j = Math.max(j, j1);
			if (j1 < 0) {
				if (k == i1) {
					++k;
				}
				++l;
			} else {
				l = 0;
			}
		}

		if (pToShrink.length == l) {
			return new String[0];
		} else {
			String[] astring = new String[pToShrink.length - l - k];

			for (int k1 = 0; k1 < astring.length; ++k1) {
				astring[k1] = pToShrink[k1 + k].substring(i, j + 1);
			}

			return astring;
		}
	}

	private static int firstNonSpace(String pEntry) {
		int i;
		for (i = 0; i < pEntry.length() && pEntry.charAt(i) == ' '; ++i) {
		}

		return i;
	}

	private static int lastNonSpace(String pEntry) {
		int i;
		for (i = pEntry.length() - 1; i >= 0 && pEntry.charAt(i) == ' '; --i) {
		}

		return i;
	}

	static String[][] patternFromJson(JsonArray pPatternArray) {
		List<String[]> pattern = new ArrayList<String[]>();
		for (int i = 0; i < pPatternArray.size(); i++) {
			String[] row = GSON.fromJson(pPatternArray.get(i), String[].class);
			pattern.add(row);
			System.out.println(Arrays.toString(row));
		}
		String[][] matrix = new String[pattern.size()][];
		matrix = pattern.toArray(matrix);
		System.out.println(Arrays.toString(matrix));

		return matrix;
	}
}
