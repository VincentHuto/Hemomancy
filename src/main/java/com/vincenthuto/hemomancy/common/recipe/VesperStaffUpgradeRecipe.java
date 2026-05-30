package com.vincenthuto.hemomancy.common.recipe;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffFocusRules;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class VesperStaffUpgradeRecipe extends CustomRecipe {
	public static class Serializer implements RecipeSerializer<VesperStaffUpgradeRecipe> {
		private static final MapCodec<VesperStaffUpgradeRecipe> CODEC = MapCodec.unit(VesperStaffUpgradeRecipe::new);
		private static final StreamCodec<RegistryFriendlyByteBuf, VesperStaffUpgradeRecipe> STREAM_CODEC =
				StreamCodec.of((buffer, recipe) -> {
				}, buffer -> new VesperStaffUpgradeRecipe());

		@Override
		public MapCodec<VesperStaffUpgradeRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, VesperStaffUpgradeRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}

	public VesperStaffUpgradeRecipe() {
		super(CraftingBookCategory.EQUIPMENT);
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		Counts counts = countInput(input);
		return VesperStaffUpgradeRules.matchesUpgrade(counts.staffCount(), counts.memoryCount(), counts.otherCount());
	}

	@Override
	public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
		if (!matches(input, null)) {
			return ItemStack.EMPTY;
		}
		ItemStack staff = findStaff(input);
		if (staff.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack result = staff.copy();
		result.setCount(1);
		CompoundTag staffData = staff.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		staffData.putBoolean(LivingStaffFocusRules.VESPER_MEMORY_AWAKENED_KEY, true);
		result.set(DataComponents.CUSTOM_DATA, CustomData.of(staffData));
		return result;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> ingredients = NonNullList.create();
		ingredients.add(Ingredient.of(ItemInit.living_staff.get()));
		ingredients.add(Ingredient.of(ItemInit.memory_of_vesper.get()));
		return ingredients;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registries) {
		return new ItemStack(ItemInit.living_staff.get());
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeInit.vesper_staff_upgrade_serializer.get();
	}

	private static ItemStack findStaff(CraftingInput input) {
		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);
			if (stack.is(ItemInit.living_staff.get())) {
				return stack;
			}
		}
		return ItemStack.EMPTY;
	}

	private static Counts countInput(CraftingInput input) {
		int staffCount = 0;
		int memoryCount = 0;
		int otherCount = 0;
		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);
			if (stack.isEmpty()) {
				continue;
			}
			if (stack.is(ItemInit.living_staff.get())) {
				staffCount++;
			} else if (stack.is(ItemInit.memory_of_vesper.get())) {
				memoryCount++;
			} else {
				otherCount++;
			}
		}
		return new Counts(staffCount, memoryCount, otherCount);
	}

	private record Counts(int staffCount, int memoryCount, int otherCount) {
	}
}
