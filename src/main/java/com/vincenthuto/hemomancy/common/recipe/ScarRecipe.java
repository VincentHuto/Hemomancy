package com.vincenthuto.hemomancy.common.recipe;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.ScarType;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.lang.reflect.MalformedParametersException;
import java.util.List;
import java.util.stream.Collectors;

public class ScarRecipe extends CustomRecipe {
	private ResourceLocation id;
	protected ResourceLocation[] requiredItems = new ResourceLocation[0];
	protected int tier = 1;
	protected ScarType scarType = ScarType.OVERRIDE;
	private byte[][] pattern;
	protected final Ingredient ingredient1;
	protected final Ingredient ingredient2;
	public static final int xBound = 8;
	public static final int yBound = 8;
	protected ItemStack outputItem = null;
	protected int outputQuantity;

	public ScarRecipe(ResourceLocation resourceLocation, int tier, ScarType type, Ingredient ingredient1,
			Ingredient ingredient2, byte[][] pattern, ItemStack result) {
		super(CraftingBookCategory.MISC);
		this.id = resourceLocation;
		this.ingredient1 = ingredient1;
		this.ingredient2 = ingredient2;
		this.tier = tier;
		this.scarType = type;
		this.pattern = pattern;
		this.outputItem = result;
	}

	public ResourceLocation getId() { return id; }

	public void setId(ResourceLocation id) {
		this.id = id;
	}

	private void initializePattern() {
		if (this.pattern.length == 0) {
			Hemomancy.LOGGER.error("Chisel pattern {0} has a length of 0 - this won't work right!",
					(Object) this.getId());
			return;
		}
		if (this.pattern.length != 8) {
			throw new MalformedParametersException("Chisel Pattern Array Bounds must be 8x8");
		}
		for (int i = 1; i < this.pattern.length; ++i) {
			if (this.pattern[i].length == 8)
				continue;
			throw new MalformedParametersException("Chisel Pattern Array Bounds must be 8x8");
		}
	}

	public static final byte[][] blank() {
		byte[][] comp = new byte[yBound][];
		for (int i = 0; i < comp.length; ++i) {
			comp[i] = new byte[xBound];
		}
		return comp;
	}

	public void setPatternBytes(byte[][] value) {
		this.pattern = value;
		this.initializePattern();
	}

	public byte[][] getPattern() {
		return pattern;
	}

	public final int getTier() {
		return this.tier;
	}

	@Override
	public boolean matches(CraftingInput pContainer, Level pLevel) {
		return true;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> list = super.getIngredients();
		list.add(ingredient1);
		list.add(ingredient2);
		return list;
	}

	@Override
	public ItemStack assemble(CraftingInput pContainer, HolderLookup.Provider pRegistryAccess) {
		return this.getResultItem(pRegistryAccess).copy();
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider a) {
		return this.outputItem;
	}

	public ItemStack getResultItem() {
		return this.outputItem;
	}

	@Override
	public boolean canCraftInDimensions(int pWidth, int pHeight) {
		return false;
	}

	@Override
	public RecipeType<?> getType() {
		return RecipeInit.chisel_recipe.get();
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeInit.chisel_recipe_serializer.get();
	}

	public static List<ScarRecipe> getAllRecipes(Level world) {
		return world.getRecipeManager().getAllRecipesFor(RecipeInit.chisel_recipe.get())
				.stream().map(holder -> {
					ScarRecipe recipe = holder.value();
					recipe.setId(holder.id());
					return recipe;
				}).collect(Collectors.toList());
	}

	public ScarType getScarType() {
		return scarType;
	}

	public void setScarType(ScarType scarType) {
		this.scarType = scarType;
	}

	public Ingredient getIngredient1() {
		return ingredient1;
	}

	public Ingredient getIngredient2() {
		return ingredient2;
	}

}
