package com.vincenthuto.hemomancy.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.common.recipe.BrazierTransmutationRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class BrazierTransmutationRecipeSerializer implements RecipeSerializer<BrazierTransmutationRecipe> {
	private static final Codec<Double> BLOOD_COST_CODEC = Codec.DOUBLE.validate(cost ->
			Double.isFinite(cost) && cost >= 0.0D
					? DataResult.success(cost)
					: DataResult.error(() -> "Brazier transmutation blood_cost must be finite and not negative"));

	private static final MapCodec<BrazierTransmutationRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(BrazierTransmutationRecipe::input),
			ResourceLocation.CODEC.optionalFieldOf("sculpting").forGetter(BrazierTransmutationRecipe::sculpting),
			Ingredient.CODEC_NONEMPTY.optionalFieldOf("catalyst").forGetter(BrazierTransmutationRecipe::catalyst),
			Codec.intRange(1, 64).optionalFieldOf("catalyst_count", 1).forGetter(BrazierTransmutationRecipe::catalystCount),
			BLOOD_COST_CODEC.fieldOf("blood_cost").forGetter(BrazierTransmutationRecipe::bloodCost),
			ItemStack.STRICT_CODEC.fieldOf("result").forGetter(BrazierTransmutationRecipe::result),
			ItemStack.STRICT_CODEC.optionalFieldOf("byproduct").forGetter(BrazierTransmutationRecipe::byproduct)
	).apply(instance, BrazierTransmutationRecipe::new));

	private static final StreamCodec<RegistryFriendlyByteBuf, BrazierTransmutationRecipe> STREAM_CODEC = StreamCodec.of(
			BrazierTransmutationRecipeSerializer::encode, BrazierTransmutationRecipeSerializer::decode);

	private static void encode(RegistryFriendlyByteBuf buffer, BrazierTransmutationRecipe recipe) {
		Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.input());
		buffer.writeOptional(recipe.sculpting(), (out, id) -> out.writeResourceLocation(id));
		buffer.writeBoolean(recipe.catalyst().isPresent());
		recipe.catalyst().ifPresent(catalyst -> Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, catalyst));
		buffer.writeVarInt(recipe.catalystCount());
		buffer.writeDouble(recipe.bloodCost());
		ItemStack.STREAM_CODEC.encode(buffer, recipe.result());
		ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.byproduct().orElse(ItemStack.EMPTY));
	}

	private static BrazierTransmutationRecipe decode(RegistryFriendlyByteBuf buffer) {
		Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
		var sculpting = buffer.readOptional(in -> in.readResourceLocation());
		java.util.Optional<Ingredient> catalyst = buffer.readBoolean()
				? java.util.Optional.of(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer))
				: java.util.Optional.empty();
		int catalystCount = buffer.readVarInt();
		double cost = buffer.readDouble();
		ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
		ItemStack byproduct = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
		return new BrazierTransmutationRecipe(input, sculpting, catalyst, catalystCount, cost, result,
				byproduct.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(byproduct));
	}

	@Override
	public MapCodec<BrazierTransmutationRecipe> codec() {
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, BrazierTransmutationRecipe> streamCodec() {
		return STREAM_CODEC;
	}
}
