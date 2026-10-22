package com.vincenthuto.hemomancy.client.registry;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.*;
import net.minecraft.client.RecipeBookCategories;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class RecipeBookRegistrar {

    private RecipeBookRegistrar() {
    }

    @SubscribeEvent
    public static void registerMachineRecipeBookCategories(RegisterRecipeBookCategoriesEvent event) {
        // Machines and rites use their own recipe interfaces, outside the vanilla book tabs.
        for (var type : com.vincenthuto.hemomancy.common.init.RecipeInit.RECIPE_TYPES.getEntries()) {
            event.registerRecipeCategoryFinder(type.get(), recipe -> RecipeBookCategories.UNKNOWN);
        }
    }
}
