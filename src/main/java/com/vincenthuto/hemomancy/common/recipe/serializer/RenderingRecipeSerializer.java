package com.vincenthuto.hemomancy.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.recipe.RenderingRecipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.ArrayList;
import java.util.List;

public class RenderingRecipeSerializer implements RecipeSerializer<RenderingRecipe> {

	private static final Codec<RenderingRecipe.Result> RESULT_CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("id")
							.forGetter(result -> result.stack().getItemHolder()),
					Codec.intRange(1, 99).optionalFieldOf("count", 1).forGetter(result -> result.stack().getCount()),
					Codec.floatRange(0f, 1f).optionalFieldOf("chance", 1f).forGetter(RenderingRecipe.Result::chance)
			).apply(instance, (item, count, chance) -> new RenderingRecipe.Result(new ItemStack(item, count), chance)));

	public static final MapCodec<RenderingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(
					ResourceLocation.CODEC.optionalFieldOf("id", Hemomancy.rloc("rendering/unknown"))
							.forGetter(RenderingRecipe::getId),
					Codec.STRING.optionalFieldOf("group", "").forGetter(RenderingRecipe::getGroup),
					Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(RenderingRecipe::getInput),
					RESULT_CODEC.listOf(1, Integer.MAX_VALUE).fieldOf("results").forGetter(RenderingRecipe::getResults),
					Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("render_time", 100)
							.forGetter(RenderingRecipe::getRenderTime)
			).apply(instance, RenderingRecipe::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, RenderingRecipe> STREAM_CODEC =
			StreamCodec.of(RenderingRecipeSerializer::toNetwork, RenderingRecipeSerializer::fromNetwork);

	private static RenderingRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
		ResourceLocation id = buf.readResourceLocation();
		String group = buf.readUtf();
		Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
		int size = buf.readVarInt();
		List<RenderingRecipe.Result> results = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			results.add(new RenderingRecipe.Result(ItemStack.STREAM_CODEC.decode(buf), buf.readFloat()));
		}
		return new RenderingRecipe(id, group, input, results, buf.readVarInt());
	}

	private static void toNetwork(RegistryFriendlyByteBuf buf, RenderingRecipe recipe) {
		buf.writeResourceLocation(recipe.getId());
		buf.writeUtf(recipe.getGroup());
		Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.getInput());
		buf.writeVarInt(recipe.getResults().size());
		for (RenderingRecipe.Result result : recipe.getResults()) {
			ItemStack.STREAM_CODEC.encode(buf, result.stack());
			buf.writeFloat(result.chance());
		}
		buf.writeVarInt(recipe.getRenderTime());
	}

	@Override
	public MapCodec<RenderingRecipe> codec() {
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, RenderingRecipe> streamCodec() {
		return STREAM_CODEC;
	}
}
