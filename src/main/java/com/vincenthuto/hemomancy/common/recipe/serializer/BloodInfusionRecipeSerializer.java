package com.vincenthuto.hemomancy.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.common.recipe.BloodInfusionRecipe;
import com.vincenthuto.hemomancy.common.event.BloodInfusionRules;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class BloodInfusionRecipeSerializer implements RecipeSerializer<BloodInfusionRecipe> {
	private static final Codec<Block> INPUT_BLOCK_CODEC = blockCodec(false);
	private static final Codec<Block> RESULT_BLOCK_CODEC = blockCodec(true);
	private static final Codec<Double> BLOOD_COST_CODEC = Codec.DOUBLE.validate(cost ->
			BloodInfusionRules.isValidCost(cost)
					? DataResult.success(cost)
					: DataResult.error(() -> "Blood infusion cost must be positive and finite"));

	private static final MapCodec<BloodInfusionRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			INPUT_BLOCK_CODEC.fieldOf("input").forGetter(BloodInfusionRecipe::input),
			BLOOD_COST_CODEC.fieldOf("blood_cost").forGetter(BloodInfusionRecipe::bloodCost),
			RESULT_BLOCK_CODEC.fieldOf("result").forGetter(BloodInfusionRecipe::result)
	).apply(instance, BloodInfusionRecipe::new));

	private static final StreamCodec<RegistryFriendlyByteBuf, BloodInfusionRecipe> STREAM_CODEC = StreamCodec.of(
			BloodInfusionRecipeSerializer::encode,
			BloodInfusionRecipeSerializer::decode);

	private static Codec<Block> blockCodec(boolean requireItem) {
		return ResourceLocation.CODEC.comapFlatMap(id -> {
			if (!BuiltInRegistries.BLOCK.containsKey(id)) {
				return DataResult.error(() -> "Unknown block: " + id);
			}
			Block block = BuiltInRegistries.BLOCK.get(id);
			if (block == Blocks.AIR || requireItem && block.asItem() == Items.AIR) {
				return DataResult.error(() -> "Invalid blood infusion block: " + id);
			}
			return DataResult.success(block);
		}, BuiltInRegistries.BLOCK::getKey);
	}

	private static void encode(RegistryFriendlyByteBuf buffer, BloodInfusionRecipe recipe) {
		buffer.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(recipe.input()));
		buffer.writeDouble(recipe.bloodCost());
		buffer.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(recipe.result()));
	}

	private static BloodInfusionRecipe decode(RegistryFriendlyByteBuf buffer) {
		return new BloodInfusionRecipe(
				BuiltInRegistries.BLOCK.get(buffer.readResourceLocation()),
				buffer.readDouble(),
				BuiltInRegistries.BLOCK.get(buffer.readResourceLocation()));
	}

	@Override
	public MapCodec<BloodInfusionRecipe> codec() {
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, BloodInfusionRecipe> streamCodec() {
		return STREAM_CODEC;
	}
}
