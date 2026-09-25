package com.vincenthuto.hemomancy.common.brewing;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public record AdvancedBrewingRecipe(ResourceLocation id, Holder<Potion> inputPotion, Ingredient catalyst,
                                    Holder<MobEffect> effect, int duration, int amplifier, int blood,
                                    int ticks, int tier, String attachment) implements Recipe<RecipeInput> {
    public AdvancedBrewingRecipe {
        if (duration <= 0 || amplifier < 0 || blood < 0 || ticks <= 0 || tier < 1 || tier > 2)
            throw new IllegalArgumentException("Invalid advanced brewing recipe");
    }

    public boolean matchesStacks(ItemStack base, ItemStack enzyme, AlembicTier machineTier) {
        PotionContents contents = base.get(DataComponents.POTION_CONTENTS);
        return machineTier.ordinal() >= tier && base.is(Items.POTION)
                && contents != null && contents.is(inputPotion) && contents.customEffects().isEmpty()
                && !base.has(DataComponentInit.ADVANCED_BREW.get()) && catalyst.test(enzyme);
    }

    @Override public boolean matches(RecipeInput input, Level level) {
        return input.size() > 1 && matchesStacks(input.getItem(0), input.getItem(1), AlembicTier.ATHANOR);
    }

    @Override public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return getResultItem(registries);
    }

    @Override public boolean canCraftInDimensions(int width, int height) { return true; }

    @Override public ItemStack getResultItem(HolderLookup.Provider registries) {
        ItemStack result = new ItemStack(Items.POTION);
        PotionContents contents = new PotionContents(Optional.empty(), Optional.empty(),
                List.of(new MobEffectInstance(effect, duration, amplifier)));
        result.set(DataComponents.POTION_CONTENTS, contents);
        result.set(DataComponentInit.ADVANCED_BREW.get(),
                new AdvancedBrewData("refined", List.of(contents), attachment, 0));
        result.set(DataComponents.CUSTOM_NAME, Component.translatable("item.hemomancy." + attachment));
        return result;
    }

    @Override public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(catalyst);
        return ingredients;
    }

    @Override public RecipeSerializer<?> getSerializer() { return RecipeInit.advanced_brewing_serializer.get(); }
    @Override public RecipeType<?> getType() { return RecipeInit.advanced_brewing_type.get(); }
    @Override public ItemStack getToastSymbol() { return new ItemStack(BlockInit.ghastly_alembic.get()); }
}
