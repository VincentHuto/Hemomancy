package com.vincenthuto.hemomancy.common.recipe;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.stream.Collectors;

public class EnzymeFruitingRecipe implements Recipe<RecipeInput> {

    public static final int DEFAULT_DURATION = 2400;
    public static final float DEFAULT_BLOOD_PER_TICK = 0.25F;

    private final ResourceLocation id;
    private final Ingredient culture;
    private final ItemStack result;
    private final int duration;
    private final float bloodPerTick;

    public EnzymeFruitingRecipe(ResourceLocation id, Ingredient culture, ItemStack result, int duration, float bloodPerTick) {
        this.id = id;
        this.culture = culture;
        this.result = result;
        this.duration = duration;
        this.bloodPerTick = bloodPerTick;
    }

    public ResourceLocation getId() {
        return id;
    }

    public Ingredient getCulture() {
        return culture;
    }

    public ItemStack getResultItemRaw() {
        return result;
    }

    public int getDuration() {
        return duration;
    }

    public float getBloodPerTick() {
        return bloodPerTick;
    }

    public float getTotalBloodCost() {
        return duration * bloodPerTick;
    }

    public boolean matchesCulture(ItemStack stack) {
        return culture.test(stack);
    }

    public boolean canFitOutput(ItemStack output) {
        if (output.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(output, result)) return false;
        int total = output.getCount() + result.getCount();
        return total <= output.getMaxStackSize();
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        return input.size() > 0 && matchesCulture(input.getItem(0));
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(culture);
        return ingredients;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeInit.enzyme_fruiting_serializer.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeInit.enzyme_fruiting_type.get();
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(BlockInit.mycelial_lantern.get());
    }

    public static List<EnzymeFruitingRecipe> getAllRecipes(Level level) {
        return level.getRecipeManager()
                .getAllRecipesFor(RecipeInit.enzyme_fruiting_type.get())
                .stream()
                .map(RecipeHolder::value)
                .collect(Collectors.toList());
    }
}
