package com.huto.hemomancy.recipes;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.huto.hemomancy.gui.mindrunes.GuiChiselStation;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;

public class RecipeChiselStation implements IModRecipe {
	private final ResourceLocation id;
	private final ItemStack output;
	private final ImmutableList<Ingredient> inputs;
	public List<Integer> activatedRunes;
	GuiChiselStation station;

	public RecipeChiselStation(ResourceLocation id, ItemStack output, List<Integer> runesIn,
			Ingredient... inputs) {
		this.id = id;
		ImmutableList.Builder<Ingredient> inputsToSet = ImmutableList.builder();
		this.output = output;
		for (Ingredient obj : inputs) {
			inputsToSet.add(obj);
		}
		this.activatedRunes = runesIn;
		this.inputs = inputsToSet.build();
	}

	public boolean matches(IItemHandler inv) {
		List<Ingredient> ingredientsMissing = new ArrayList<>(inputs);
		for (int i = 0; i < inv.getSlots(); i++) {
			ItemStack input = inv.getStackInSlot(i);
			if (input.isEmpty())
				break;

			int stackIndex = -1, oredictIndex = -1;

			for (int j = 0; j < ingredientsMissing.size(); j++) {
				Ingredient ingr = ingredientsMissing.get(j);
				if (ingr.test(input)) {
					stackIndex = j;
					break;
				}
			}

			if (stackIndex != -1)
				ingredientsMissing.remove(stackIndex);
			else if (oredictIndex != -1)
				ingredientsMissing.remove(oredictIndex);
			else
				return false;
		}

		return ingredientsMissing.isEmpty();
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	public List<Ingredient> getInputs() {
		return inputs;
	}

	public ItemStack getOutput() {
		return output;
	}

	public List<Integer> getActivatedRunes() {
		return activatedRunes;
	}

	public void write(PacketBuffer buf) {
		buf.writeResourceLocation(id);
		buf.writeVarInt(inputs.size());
		for (Ingredient input : inputs) {
			input.write(buf);

		}
		buf.writeItemStack(output, false);
		for (int i = 0; i < activatedRunes.size(); i++) {
			buf.writeInt(i);
		}
	}

	public static RecipeChiselStation read(PacketBuffer buf) {
		ResourceLocation id = buf.readResourceLocation();
		Ingredient[] inputs = new Ingredient[buf.readVarInt()];
		for (int i = 0; i < inputs.length; i++) {
			inputs[i] = Ingredient.read(buf);
		}
		ItemStack output = buf.readItemStack();
		ArrayList<Integer> activatedRunesIn = new ArrayList<Integer>();
		activatedRunesIn.add(buf.readInt());
		return new RecipeChiselStation(id, output, activatedRunesIn, inputs);
	}

}
