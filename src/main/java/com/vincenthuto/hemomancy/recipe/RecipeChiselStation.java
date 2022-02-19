//package com.vincenthuto.hemomancy.recipe;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import com.google.common.collect.ImmutableList;
//import com.vincenthuto.hemomancy.gui.mindrunes.ScreenChiselStation;
//import com.vincenthuto.hutoslib.common.recipe.IModRecipe;
//
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.crafting.Ingredient;
//import net.minecraftforge.items.IItemHandler;
//import net.minecraftforge.registries.ForgeRegistryEntry;
//
//public class RecipeChiselStation extends ForgeRegistryEntry<RecipeChiselStation> implements IModRecipe {
//	private final ResourceLocation id;
//	private final ItemStack output;
//	private final ImmutableList<Ingredient> inputs;
//	public List<Integer> activatedRunes;
//	ScreenChiselStation station;
//
//	public RecipeChiselStation(ResourceLocation id, ItemStack output, List<Integer> runesIn, Ingredient... inputs) {
//		this.id = id;
//		ImmutableList.Builder<Ingredient> inputsToSet = ImmutableList.builder();
//		this.output = output;
//		for (Ingredient obj : inputs) {
//			inputsToSet.add(obj);
//		}
//		this.activatedRunes = runesIn;
//		this.inputs = inputsToSet.build();
//	}
//
//	public boolean matches(IItemHandler inv) {
//		List<Ingredient> ingredientsMissing = new ArrayList<>(inputs);
//		for (int i = 0; i < inv.getSlots(); i++) {
//			ItemStack input = inv.getStackInSlot(i);
//			if (input.isEmpty())
//				break;
//
//			int stackIndex = -1, oredictIndex = -1;
//
//			for (int j = 0; j < ingredientsMissing.size(); j++) {
//				Ingredient ingr = ingredientsMissing.get(j);
//				if (ingr.test(input)) {
//					stackIndex = j;
//					break;
//				}
//			}
//
//			if (stackIndex != -1)
//				ingredientsMissing.remove(stackIndex);
//			else if (oredictIndex != -1)
//				ingredientsMissing.remove(oredictIndex);
//			else
//				return false;
//		}
//
//		return ingredientsMissing.isEmpty();
//	}
//
//	@Override
//	public List<Ingredient> getInputs() {
//		return inputs;
//	}
//
//	@Override
//	public ItemStack getOutput() {
//		return output;
//	}
//
//	public List<Integer> getActivatedRunes() {
//		return activatedRunes;
//	}
//
//
//
//	@Override
//	public ResourceLocation getId() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//}
