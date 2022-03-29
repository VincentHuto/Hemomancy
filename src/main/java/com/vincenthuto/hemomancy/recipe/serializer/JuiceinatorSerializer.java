package com.vincenthuto.hemomancy.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.recipe.JuiceinatorRecipe;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class JuiceinatorSerializer extends ForgeRegistryEntry<RecipeSerializer<?>>
		implements RecipeSerializer<JuiceinatorRecipe> {

	@Override
	public JuiceinatorRecipe fromJson(ResourceLocation location, JsonObject object) {
		String s = GsonHelper.getAsString(object, "group", "");
		JsonElement jsonelement = GsonHelper.isArrayNode(object, "ingredient")
				? GsonHelper.getAsJsonArray(object, "ingredient")
				: GsonHelper.getAsJsonObject(object, "ingredient");
		Ingredient ingredient = Ingredient.fromJson(jsonelement);
		if (!object.has("result"))
			throw new com.google.gson.JsonSyntaxException("Missing result, expected to find a string or object");
		ItemStack itemstack;
		if (object.get("result").isJsonObject())
			itemstack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(object, "result"));
		else {
			int c = GsonHelper.getAsInt(object, "count");
			String s1 = GsonHelper.getAsString(object, "result");
			ResourceLocation resourcelocation = new ResourceLocation(s1);
			itemstack = new ItemStack(Registry.ITEM.getOptional(resourcelocation).orElseThrow(() -> {
				return new IllegalStateException("Item: " + s1 + " does not exist");
			}), c);
		}

		float f = GsonHelper.getAsFloat(object, "experience", 0.0F);
		int i = GsonHelper.getAsInt(object, "cookingtime", 100);
		return new JuiceinatorRecipe(location, s, ingredient, itemstack, f, i);
	}

	@Override
	public JuiceinatorRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
		ResourceLocation id = buffer.readResourceLocation();
		String group = buffer.readUtf();
		ItemStack result = buffer.readItem();
		Ingredient input = Ingredient.of(buffer.readItem());
		float xp = buffer.readFloat();
		int time = buffer.readInt();
		return new JuiceinatorRecipe(id, group, input, result, xp, time);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, JuiceinatorRecipe recipe) {
		buffer.writeResourceLocation(recipe.getId());
		buffer.writeUtf(recipe.getGroup());
		buffer.writeItem(recipe.getIngredients().get(0).getItems()[0]);
		buffer.writeItem(recipe.getResultItem());
		buffer.writeFloat(recipe.getExperience());
		buffer.writeInt(recipe.getCookingTime());
	}
}