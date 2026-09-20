package com.vincenthuto.hemomancy.compat.jei;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodKnowledge;
import com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodProgress.Lesson;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Only hides these five recipes; never unhides another plugin's unrelated recipes. */
public final class ClinicalRecipeVisibility {
    private static IJeiRuntime runtime;
    private static boolean listening;
    private static final Set<ResourceLocation> hidden = new HashSet<>();
    private static Object level;
    private ClinicalRecipeVisibility() {}
    public static void runtime(IJeiRuntime value) {
        runtime = value;
        if (value != null && !listening) {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(ClinicalRecipeVisibility::tick);
            listening = true;
        }
        hidden.clear();
        level = null;
        update();
    }
    public static void tick(ClientTickEvent.Post event) { update(); }
    private static void update() {
        var mc = Minecraft.getInstance();
        if (runtime == null || mc.level == null || mc.player == null) return;
        boolean refresh = level != mc.level;
        if (refresh) { hidden.clear(); level = mc.level; }
        for (Lesson lesson : Lesson.values()) for (String path : ClinicalBloodKnowledge.recipes(lesson)) {
            var id = Hemomancy.rloc(path);
            boolean visible = ClinicalBloodKnowledge.recipeVisible(mc.player, id);
            if (!refresh && (visible && !hidden.contains(id) || !visible && hidden.contains(id))) continue;
            var recipe = mc.level.getRecipeManager().byKey(id).orElse(null);
            if (recipe == null || !(recipe.value() instanceof CraftingRecipe crafting)) continue;
            var holders = List.of(new RecipeHolder<>(id, crafting));
            if (visible) { runtime.getRecipeManager().unhideRecipes(RecipeTypes.CRAFTING, holders); hidden.remove(id); }
            else { runtime.getRecipeManager().hideRecipes(RecipeTypes.CRAFTING, holders); hidden.add(id); }
        }
    }
}
