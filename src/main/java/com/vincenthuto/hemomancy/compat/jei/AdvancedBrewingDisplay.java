package com.vincenthuto.hemomancy.compat.jei;

import com.vincenthuto.hemomancy.common.brewing.AlembicTier;
import com.vincenthuto.hemomancy.common.brewing.BrewingMatch;
import com.vincenthuto.hemomancy.common.brewing.BrewingResolver;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public record AdvancedBrewingDisplay(ItemStack input, ItemStack catalyst, ItemStack catalyst2,
                                     ItemStack output, int blood, int ticks, int degree, String note) {
    public static List<AdvancedBrewingDisplay> all(Level level) {
        List<AdvancedBrewingDisplay> displays = new ArrayList<>();
        for (var holder : level.getRecipeManager().getAllRecipesFor(RecipeInit.advanced_brewing_type.get())) {
            var recipe = holder.value();
            displays.add(new AdvancedBrewingDisplay(
                    PotionContents.createItemStack(Items.POTION, recipe.inputPotion()),
                    recipe.catalyst().getItems()[0], ItemStack.EMPTY,
                    recipe.getResultItem(level.registryAccess()), recipe.blood(),
                    AlembicTier.fromSaved(recipe.tier()).processingTicks(recipe.ticks()),
                    recipe.tier() == 1 ? 3 : 5, "refine"));
        }
        ItemStack strength = PotionContents.createItemStack(Items.POTION, Potions.STRENGTH);
        ItemStack speed = PotionContents.createItemStack(Items.POTION, Potions.SWIFTNESS);
        ItemStack fire = PotionContents.createItemStack(Items.POTION, Potions.FIRE_RESISTANCE);
        add(displays, BrewingResolver.resolve(level, AlembicTier.CONDENSER, strength, speed, ItemStack.EMPTY), AlembicTier.CONDENSER,
                "compound");
        BrewingMatch bound = BrewingResolver.resolve(level, AlembicTier.ATHANOR, strength, speed, fire);
        add(displays, bound, AlembicTier.ATHANOR, "bind");
        if (bound != null)
            add(displays, BrewingResolver.resolve(level, AlembicTier.ATHANOR,
                    bound.result(), fire, speed), AlembicTier.ATHANOR, "refill");
        return displays;
    }

    private static void add(List<AdvancedBrewingDisplay> displays, BrewingMatch match, AlembicTier tier, String note) {
        if (match == null) return;
        displays.add(new AdvancedBrewingDisplay(match.input(), match.catalyst(), match.catalyst2(),
                match.result(), match.blood(), tier.processingTicks(match.ticks()), tier == AlembicTier.CONDENSER ? 3 : 5, note));
    }
}
