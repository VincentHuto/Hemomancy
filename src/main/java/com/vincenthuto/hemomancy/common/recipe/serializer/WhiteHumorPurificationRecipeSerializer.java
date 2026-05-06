package com.vincenthuto.hemomancy.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.recipe.WhiteHumorPurificationRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class WhiteHumorPurificationRecipeSerializer implements RecipeSerializer<WhiteHumorPurificationRecipe> {

	public static final MapCodec<WhiteHumorPurificationRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(
					ResourceLocation.CODEC.optionalFieldOf("id", Hemomancy.rloc("white_humor_purification/unknown"))
							.forGetter(WhiteHumorPurificationRecipe::getId),
					Codec.STRING.optionalFieldOf("group", "").forGetter(WhiteHumorPurificationRecipe::getGroup),
					Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(WhiteHumorPurificationRecipe::getInput),
					ItemStack.STRICT_CODEC.fieldOf("result").forGetter(WhiteHumorPurificationRecipe::getResultItemRaw),
					Codec.INT.optionalFieldOf("transform_time", 240).forGetter(WhiteHumorPurificationRecipe::getTransformTime)
			).apply(instance, WhiteHumorPurificationRecipe::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, WhiteHumorPurificationRecipe> STREAM_CODEC =
			StreamCodec.of(WhiteHumorPurificationRecipeSerializer::toNetwork, WhiteHumorPurificationRecipeSerializer::fromNetwork);

	private static WhiteHumorPurificationRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
		ResourceLocation id = buf.readResourceLocation();
		String group = buf.readUtf();
		Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
		ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
		int transformTime = buf.readInt();
		return new WhiteHumorPurificationRecipe(id, group, input, result, transformTime);
	}

	private static void toNetwork(RegistryFriendlyByteBuf buf, WhiteHumorPurificationRecipe recipe) {
		buf.writeResourceLocation(recipe.getId());
		buf.writeUtf(recipe.getGroup());
		Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.getInput());
		ItemStack.STREAM_CODEC.encode(buf, recipe.getResultItemRaw());
		buf.writeInt(recipe.getTransformTime());
	}

	@Override
	public MapCodec<WhiteHumorPurificationRecipe> codec() {
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, WhiteHumorPurificationRecipe> streamCodec() {
		return STREAM_CODEC;
	}
}
