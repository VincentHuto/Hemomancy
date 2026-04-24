package com.vincenthuto.hemomancy.common.recipe.serializer;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.recipe.DistillationRecipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class DistillationRecipeSerializer implements RecipeSerializer<DistillationRecipe> {

private static final Codec<Either<ItemStack, ResourceLocation>> RESULT_CODEC = Codec
.either(ItemStack.STRICT_CODEC, ResourceLocation.CODEC);

public static final MapCodec<DistillationRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
ResourceLocation.CODEC.optionalFieldOf("id", Hemomancy.rloc("distillation/unknown")).forGetter(DistillationRecipe::getId),
Codec.STRING.optionalFieldOf("group", "").forGetter(DistillationRecipe::getGroup),
Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(DistillationRecipe::getIngredient),
Ingredient.CODEC.optionalFieldOf("catalyst", Ingredient.EMPTY).forGetter(DistillationRecipe::getCatalyst),
RESULT_CODEC.fieldOf("result").forGetter(recipe -> Either.left(recipe.getResultItemRaw())),
Codec.INT.optionalFieldOf("count", 1).forGetter(recipe -> recipe.getResultItemRaw().getCount()),
Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(DistillationRecipe::getExperience),
Codec.INT.optionalFieldOf("cookingtime", 100).forGetter(DistillationRecipe::getCookingTime),
Codec.BOOL.optionalFieldOf("pallid", false).forGetter(DistillationRecipe::isPallid)).apply(instance,
(id, group, ingredient, catalyst, resultEither, count, experience, cookingTime, pallid) -> {
ItemStack result = resultEither.map(ItemStack::copy,
resultId -> new ItemStack(BuiltInRegistries.ITEM.get(resultId), count));
return new DistillationRecipe(id, group, ingredient, catalyst, pallid, result, experience, cookingTime);
}));

public static final StreamCodec<RegistryFriendlyByteBuf, DistillationRecipe> STREAM_CODEC = StreamCodec
.of(DistillationRecipeSerializer::toNetwork, DistillationRecipeSerializer::fromNetwork);

private static DistillationRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
ResourceLocation recipeId = buffer.readResourceLocation();
String group = buffer.readUtf();
Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
boolean hasCatalyst = buffer.readBoolean();
Ingredient catalyst = hasCatalyst ? Ingredient.CONTENTS_STREAM_CODEC.decode(buffer) : Ingredient.EMPTY;
boolean pallid = buffer.readBoolean();
boolean hasResult = buffer.readBoolean();
ItemStack result = hasResult ? ItemStack.STREAM_CODEC.decode(buffer) : ItemStack.EMPTY;
float xp = buffer.readFloat();
int time = buffer.readInt();
return new DistillationRecipe(recipeId, group, ingredient, catalyst, pallid, result, xp, time);
}

private static void toNetwork(RegistryFriendlyByteBuf buffer, DistillationRecipe recipe) {
buffer.writeResourceLocation(recipe.getId());
buffer.writeUtf(recipe.getGroup());
Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.getIngredient());
boolean hasCatalyst = recipe.requiresCatalyst();
buffer.writeBoolean(hasCatalyst);
if (hasCatalyst) Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.getCatalyst());
buffer.writeBoolean(recipe.isPallid());
ItemStack result = recipe.getResultItemRaw();
boolean hasResult = result != null && !result.isEmpty();
buffer.writeBoolean(hasResult);
if (hasResult) {
ItemStack.STREAM_CODEC.encode(buffer, result);
}
buffer.writeFloat(recipe.getExperience());
buffer.writeInt(recipe.getCookingTime());
}

@Override
public MapCodec<DistillationRecipe> codec() {
return CODEC;
}

@Override
public StreamCodec<RegistryFriendlyByteBuf, DistillationRecipe> streamCodec() {
return STREAM_CODEC;
}
}
