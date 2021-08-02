package com.huto.hemomancy.recipe;

import java.util.HashMap;
import java.util.Map;

import com.huto.hemomancy.capa.tendency.EnumBloodTendency;
import com.huto.hemomancy.gui.recaller.GuiVisceralRecaller;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class RecipeRecaller extends ForgeRegistryEntry<RecipeRecaller> {
	private final ResourceLocation id;
	private final ItemStack output;
	GuiVisceralRecaller station;
	Map<EnumBloodTendency, Float> tendency = new HashMap<EnumBloodTendency, Float>();

	public RecipeRecaller(ResourceLocation id, Map<EnumBloodTendency, Float> tends, ItemStack output) {
		this.id = id;
		this.tendency = tends;
		this.output = output;
	}

	public ResourceLocation getId() {
		return id;
	}

	public ItemStack getOutput() {
		return output;
	}

	public Map<EnumBloodTendency, Float> getTendency() {
		return tendency;
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeResourceLocation(id);
		buf.writeItemStack(output, false);
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			buf.writeFloat(getTendency().get(tend));
		}
	}

	public static RecipeRecaller read(FriendlyByteBuf buf) {
		ResourceLocation id = buf.readResourceLocation();
		ItemStack output = buf.readItemStack();
		Map<EnumBloodTendency, Float> tends = new HashMap<EnumBloodTendency, Float>();
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			tends.put(tend, buf.readFloat());
		}
		return new RecipeRecaller(id, tends, output);
	}

}
