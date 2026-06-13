package com.vincenthuto.hemomancy.common.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import com.vincenthuto.hemomancy.common.item.shared.ScaleGripEvents;
import com.vincenthuto.hemomancy.common.item.shared.ScaleGripRules;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ScaleGripBindingRecipe extends net.minecraft.world.item.crafting.CustomRecipe {
	public static class Serializer implements RecipeSerializer<ScaleGripBindingRecipe> {
		private static final MapCodec<ScaleGripBindingRecipe> CODEC = new MapCodec<>() {
			@Override
			public <T> Stream<T> keys(DynamicOps<T> ops) {
				return Stream.empty();
			}

			@Override
			public <T> DataResult<ScaleGripBindingRecipe> decode(DynamicOps<T> ops, MapLike<T> input) {
				return DataResult.success(new ScaleGripBindingRecipe());
			}

			@Override
			public <T> RecordBuilder<T> encode(ScaleGripBindingRecipe recipe, DynamicOps<T> ops, RecordBuilder<T> prefix) {
				return prefix;
			}
		};
		private static final StreamCodec<RegistryFriendlyByteBuf, ScaleGripBindingRecipe> STREAM_CODEC =
				StreamCodec.of((buffer, recipe) -> {
				}, buffer -> new ScaleGripBindingRecipe());

		@Override
		public MapCodec<ScaleGripBindingRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, ScaleGripBindingRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}

	public ScaleGripBindingRecipe() {
		super(CraftingBookCategory.MISC);
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return ScaleGripRules.matchesBindingCraft(toCandidates(input));
	}

	@Override
	public ItemStack assemble(CraftingInput input, HolderLookup.Provider registryAccess) {
		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);
			if (!stack.isEmpty() && !stack.is(ItemInit.scale_grip.get())
					&& !ScaleGripEvents.isProtected(stack)) {
				ItemStack result = stack.copyWithCount(1);
				ScaleGripEvents.markProtected(result);
				return result;
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registryAccess) {
		return ItemStack.EMPTY;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> ingredients = NonNullList.create();
		ingredients.add(Ingredient.of(ItemInit.scale_grip.get()));
		return ingredients;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeInit.scale_grip_binding_serializer.get();
	}

	private static List<ScaleGripRules.Candidate> toCandidates(CraftingInput input) {
		List<ScaleGripRules.Candidate> candidates = new ArrayList<>();
		for (int slot = 0; slot < input.size(); slot++) {
			ItemStack stack = input.getItem(slot);
			candidates.add(new ScaleGripRules.Candidate(slot, itemId(stack), stack.isEmpty(),
					stack.is(ItemInit.scale_grip.get()), ScaleGripEvents.isProtected(stack)));
		}
		return candidates;
	}

	private static String itemId(ItemStack stack) {
		if (stack.isEmpty()) {
			return "minecraft:air";
		}
		ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
		return id == null ? "minecraft:air" : id.toString();
	}
}
