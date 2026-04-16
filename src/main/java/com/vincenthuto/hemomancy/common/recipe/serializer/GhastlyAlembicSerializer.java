package com.vincenthuto.hemomancy.common.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.common.recipe.GhastlyAlembicRecipe;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.registries.ForgeRegistries;

public class GhastlyAlembicSerializer implements RecipeSerializer<GhastlyAlembicRecipe> {

	@Override
	public GhastlyAlembicRecipe fromJson(ResourceLocation location, JsonObject object) {
		String group = GsonHelper.getAsString(object, "group", "");

		// ---- ingredient ----
		JsonElement ingredientEl = GsonHelper.isArrayNode(object, "ingredient")
				? GsonHelper.getAsJsonArray(object, "ingredient")
				: GsonHelper.getAsJsonObject(object, "ingredient");
		Ingredient ingredient = Ingredient.fromJson(ingredientEl);

		// ---- catalyst (optional) ----
		Ingredient catalyst = Ingredient.EMPTY;
		if (object.has("catalyst")) {
			JsonElement catalystEl = GsonHelper.isArrayNode(object, "catalyst")
					? GsonHelper.getAsJsonArray(object, "catalyst")
					: GsonHelper.getAsJsonObject(object, "catalyst");
			catalyst = Ingredient.fromJson(catalystEl);
		}

		// ---- result ----
		if (!object.has("result"))
			throw new com.google.gson.JsonSyntaxException("Missing result, expected to find a string or object");
		ItemStack result;
		if (object.get("result").isJsonObject()) {
			result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(object, "result"));
		} else {
			int count = GsonHelper.getAsInt(object, "count", 1);
			String resultId = GsonHelper.getAsString(object, "result");
			result = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(resultId)), count);
		}

		float experience = GsonHelper.getAsFloat(object, "experience", 0.0F);
		int cookingTime = GsonHelper.getAsInt(object, "cookingtime", 100);
		boolean pallid = GsonHelper.getAsBoolean(object, "pallid", false);

		return new GhastlyAlembicRecipe(location, group, ingredient, catalyst, pallid, result, experience, cookingTime);
	}

	@Override
	public GhastlyAlembicRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
		String group = buffer.readUtf();
		Ingredient ingredient = Ingredient.fromNetwork(buffer);
		boolean hasCatalyst = buffer.readBoolean();
		Ingredient catalyst = hasCatalyst ? Ingredient.fromNetwork(buffer) : Ingredient.EMPTY;
		boolean pallid = buffer.readBoolean();
		ItemStack result = buffer.readItem();
		float xp = buffer.readFloat();
		int time = buffer.readInt();
		return new GhastlyAlembicRecipe(recipeId, group, ingredient, catalyst, pallid, result, xp, time);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, GhastlyAlembicRecipe recipe) {
		buffer.writeUtf(recipe.getGroup());
		recipe.getIngredient().toNetwork(buffer);
		boolean hasCatalyst = recipe.requiresCatalyst();
		buffer.writeBoolean(hasCatalyst);
		if (hasCatalyst) recipe.getCatalyst().toNetwork(buffer);
		buffer.writeBoolean(recipe.isPallid());
		buffer.writeItem(recipe.getResultItem(null));
		buffer.writeFloat(recipe.getExperience());
		buffer.writeInt(recipe.getCookingTime());
	}
}
