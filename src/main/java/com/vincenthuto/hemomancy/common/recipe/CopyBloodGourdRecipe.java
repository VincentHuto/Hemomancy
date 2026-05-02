package com.vincenthuto.hemomancy.common.recipe;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodGourdItem;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import com.mojang.serialization.MapCodec;

public class CopyBloodGourdRecipe extends net.minecraft.world.item.crafting.CustomRecipe {
	public static class Serializer implements RecipeSerializer<CopyBloodGourdRecipe> {
		private static final MapCodec<CopyBloodGourdRecipe> CODEC = RecipeSerializer.SHAPED_RECIPE.codec()
				.xmap(CopyBloodGourdRecipe::new, CopyBloodGourdRecipe::baseRecipe);

		private static final StreamCodec<RegistryFriendlyByteBuf, CopyBloodGourdRecipe> STREAM_CODEC = StreamCodec.of(
				(buffer, recipe) -> RecipeSerializer.SHAPED_RECIPE.streamCodec().encode(buffer, recipe.baseRecipe()),
				buffer -> new CopyBloodGourdRecipe(RecipeSerializer.SHAPED_RECIPE.streamCodec().decode(buffer)));

		@Override
		public MapCodec<CopyBloodGourdRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, CopyBloodGourdRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}

    private final ShapedRecipe baseRecipe;

	public CopyBloodGourdRecipe(ShapedRecipe shapedRecipe) {
		super(CraftingBookCategory.MISC);
		this.baseRecipe = shapedRecipe;
	}

	public ShapedRecipe baseRecipe() {
		return baseRecipe;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return baseRecipe.matches(input, level);
	}

	@Override
	public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registryAccess) {
		final ItemStack craftingResult = baseRecipe.assemble(inv, registryAccess);
		ItemStack dataSource = ItemStack.EMPTY;

		if (!craftingResult.isEmpty()) {
			for (int i = 0; i < inv.size(); i++) {
				final ItemStack item = inv.getItem(i);
				if (!item.isEmpty() && item.getItem() instanceof BloodGourdItem) {

					dataSource = item;
					break;
				}
			}
			if (dataSource.getItem() instanceof BloodGourdItem) {
				if (!dataSource.isEmpty() && dataSource.has(DataComponents.CUSTOM_DATA)) {
					craftingResult.set(DataComponents.CUSTOM_DATA,
							CustomData.of(dataSource.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()));
					IBloodVolume bloodVolume = HemoCapabilityAccess.getBloodVolume(dataSource)
							.orElseThrow(NullPointerException::new);
					if (craftingResult.getItem() instanceof BloodGourdItem) {
						IBloodVolume resultVolume = HemoCapabilityAccess.getBloodVolume(craftingResult)
								.orElseThrow(NullPointerException::new);
						resultVolume.setBloodVolume(bloodVolume.getBloodVolume());
					}
				}
			}
		}

		return craftingResult;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return baseRecipe.canCraftInDimensions(width, height);
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registryAccess) {
		return baseRecipe.getResultItem(registryAccess);
	}

	@Override
	public net.minecraft.core.NonNullList<net.minecraft.world.item.crafting.Ingredient> getIngredients() {
		return baseRecipe.getIngredients();
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeInit.blood_gourd_upgrade_serializer.get();
	}

}