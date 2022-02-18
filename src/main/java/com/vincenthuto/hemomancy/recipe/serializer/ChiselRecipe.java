package com.vincenthuto.hemomancy.recipe.serializer;

import java.lang.reflect.MalformedParametersException;
import java.util.Collection;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.rune.RuneType;
import com.vincenthuto.hemomancy.init.RecipeInit;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class ChiselRecipe extends CustomRecipe {
	protected ResourceLocation[] requiredItems = new ResourceLocation[0];
	protected int tier = 1;
	protected RuneType type = RuneType.OVERRIDE;
	private byte[][] pattern;
	protected final Ingredient ingredient1;
	protected final Ingredient ingredient2;
	public static final int xBound = 8;
	public static final int yBound = 8;
	protected ItemStack outputItem = null;
	protected int outputQuantity;

	public ChiselRecipe(ResourceLocation resourceLocation, int tier, RuneType type, Ingredient ingredient1,
			Ingredient ingredient2, byte[][] pattern, ItemStack result) {
		super(resourceLocation);
		this.ingredient1 = ingredient1;
		this.ingredient2 = ingredient2;
		this.tier = tier;
		this.type = type;
		this.pattern = pattern;
		this.outputItem = result;
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

	private static final byte[][] blank() {
		byte[][] comp = new byte[11][];
		for (int i = 0; i < 11; ++i) {
			comp[i] = new byte[11];
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
	public boolean matches(CraftingContainer pContainer, Level pLevel) {
		return true;
	}

	@Override
	public ItemStack assemble(CraftingContainer pContainer) {
		return this.getResultItem().copy();
	}

	@Override
	public ItemStack getResultItem() {
		return this.outputItem;
	}

	@Override
	public boolean canCraftInDimensions(int pWidth, int pHeight) {
		return false;
	}

	@Override
	public RecipeType<?> getType() {
		return RecipeInit.chisel_recipe_type;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeInit.chisel_recipe_serializer.get();
	}

	public static Collection<ChiselRecipe> getAllRecipes(Level world) {
		return world.getRecipeManager().getAllRecipesFor(RecipeInit.chisel_recipe_type);
	}

	public ItemStack getOutputItem() {
		return outputItem;
	}

	public void setOutputItem(ItemStack outputItem) {
		this.outputItem = outputItem;
	}

	public Ingredient getIngredient1() {
		return ingredient1;
	}

	public Ingredient getIngredient2() {
		return ingredient2;
	}

}
