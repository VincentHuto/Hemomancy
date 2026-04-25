package com.vincenthuto.hemomancy.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.recipe.MorphicNectarRecipe;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class MorphicNectarRecipeSerializer implements RecipeSerializer<MorphicNectarRecipe> {

	public static final MapCodec<MorphicNectarRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(
					ResourceLocation.CODEC.optionalFieldOf("id", Hemomancy.rloc("morphic_nectar/unknown"))
							.forGetter(MorphicNectarRecipe::getId),
					Codec.STRING.optionalFieldOf("group", "").forGetter(MorphicNectarRecipe::getGroup),
					Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(MorphicNectarRecipe::getInput),
					ItemStack.STRICT_CODEC.fieldOf("result").forGetter(MorphicNectarRecipe::getResultItemRaw),
					Codec.INT.optionalFieldOf("transform_time", 60).forGetter(MorphicNectarRecipe::getTransformTime)
			).apply(instance, MorphicNectarRecipe::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, MorphicNectarRecipe> STREAM_CODEC =
			StreamCodec.of(MorphicNectarRecipeSerializer::toNetwork, MorphicNectarRecipeSerializer::fromNetwork);

	private static MorphicNectarRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
		ResourceLocation id = buf.readResourceLocation();
		String group = buf.readUtf();
		Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
		ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
		int transformTime = buf.readInt();
		return new MorphicNectarRecipe(id, group, input, result, transformTime);
	}

	private static void toNetwork(RegistryFriendlyByteBuf buf, MorphicNectarRecipe recipe) {
		buf.writeResourceLocation(recipe.getId());
		buf.writeUtf(recipe.getGroup());
		Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.getInput());
		ItemStack.STREAM_CODEC.encode(buf, recipe.getResultItemRaw());
		buf.writeInt(recipe.getTransformTime());
	}

	@Override
	public MapCodec<MorphicNectarRecipe> codec() {
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, MorphicNectarRecipe> streamCodec() {
		return STREAM_CODEC;
	}
}
