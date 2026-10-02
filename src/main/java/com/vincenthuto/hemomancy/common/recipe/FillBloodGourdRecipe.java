package com.vincenthuto.hemomancy.common.recipe;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodGourdItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

public class FillBloodGourdRecipe extends net.minecraft.world.item.crafting.CustomRecipe {
	public static class Serializer implements RecipeSerializer<FillBloodGourdRecipe> {
		private static final MapCodec<FillBloodGourdRecipe> CODEC = RecipeSerializer.SHAPED_RECIPE.codec()
				.xmap(FillBloodGourdRecipe::new, FillBloodGourdRecipe::baseRecipe);

		private static final StreamCodec<RegistryFriendlyByteBuf, FillBloodGourdRecipe> STREAM_CODEC = StreamCodec.of(
				(buffer, recipe) -> RecipeSerializer.SHAPED_RECIPE.streamCodec().encode(buffer, recipe.baseRecipe()),
				buffer -> new FillBloodGourdRecipe(RecipeSerializer.SHAPED_RECIPE.streamCodec().decode(buffer)));

		@Override
		public MapCodec<FillBloodGourdRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, FillBloodGourdRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}

    private final ShapedRecipe baseRecipe;

	public FillBloodGourdRecipe(ShapedRecipe shapedRecipe) {
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
		ItemStack craftingResult = baseRecipe.assemble(inv, registryAccess);
		if (!(craftingResult.getItem() instanceof BloodGourdItem gourd)) return craftingResult;
		for (int slot = 0; slot < inv.size(); slot++) {
			ItemStack source = inv.getItem(slot);
			if (!(source.getItem() instanceof BloodGourdItem)) continue;
			IBloodVolume sourceVolume = HemoCapabilityAccess.getBloodVolume(source.copy()).orElseThrow();
			craftingResult.applyComponents(source.getComponentsPatch());
			IBloodVolume resultVolume = HemoCapabilityAccess.getBloodVolume(craftingResult).orElseThrow();
			resultVolume.setMaxBloodVolume(gourd.getMaxBlood());
			resultVolume.setBloodVolume(Math.min(sourceVolume.getBloodVolume() + 200, gourd.getMaxBlood()));
			break;
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
		return RecipeInit.blood_gourd_fill_serializer.get();
	}

}
