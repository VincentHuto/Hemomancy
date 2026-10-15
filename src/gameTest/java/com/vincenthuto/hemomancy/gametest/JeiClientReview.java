package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.compat.jei.AdvancedBrewingRecipeCategory;
import com.vincenthuto.hemomancy.compat.jei.BloodInfusionRecipeCategory;
import com.vincenthuto.hemomancy.compat.jei.EnzymeFruitingRecipeCategory;
import com.vincenthuto.hemomancy.compat.jei.JEIPlugin;
import com.vincenthuto.hemomancy.compat.jei.MorphicNectarRecipeCategory;
import com.vincenthuto.hemomancy.compat.jei.MycelialCrucibleRecipeCategory;
import com.vincenthuto.hemomancy.compat.jei.SuccessionRecipeCategory;
import com.vincenthuto.hemomancy.compat.jei.WhiteHumorPurificationRecipeCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.List;

/**
 * With hemomancy.jeiReview (run jeiReviewClient), opens every Hemomancy JEI category in turn once a world is loaded
 * and captures each page into build/jei-client-review/screenshots, then stops the client. Excluded from release jars.
 */
@JeiPlugin
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class JeiClientReview implements IModPlugin {
    private static IJeiRuntime runtime;
    private static int ticks;
    private static int shot;

    private static final List<RecipeType<?>> TYPES = List.of(
            JEIPlugin.memory_weaving_type, JEIPlugin.ghastly_distillation_recipe_type, JEIPlugin.pallid_distillation_recipe_type,
            AdvancedBrewingRecipeCategory.TYPE, BloodInfusionRecipeCategory.JEI_TYPE, EnzymeFruitingRecipeCategory.JEI_TYPE,
            JEIPlugin.armature_upgrade_type, JEIPlugin.incubator_recipe_type, JEIPlugin.living_weapon_graft_type,
            MycelialCrucibleRecipeCategory.JEI_TYPE, MorphicNectarRecipeCategory.JEI_TYPE,
            WhiteHumorPurificationRecipeCategory.JEI_TYPE, JEIPlugin.scar_station_recipe_type, SuccessionRecipeCategory.TYPE,
            JEIPlugin.blood_structure_recipe_type, JEIPlugin.working_craft_recipe_type);

    @Override
    public ResourceLocation getPluginUid() {
        return Hemomancy.rloc("jei_review");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime value) {
        runtime = value;
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hemomancy.jeiReview")) return;
        var mc = Minecraft.getInstance();
        if (mc.player == null || runtime == null) return;
        mc.options.pauseOnLostFocus = false;
        if (++ticks == 90) {
            mc.options.guiScale().set(3);
            mc.resizeDisplay();
        }
        if (ticks < 100) return;
        int local = (ticks - 100) % 50;
        if (local == 0) {
            if (shot == TYPES.size()) { mc.stop(); return; }
            runtime.getRecipesGui().showTypes(List.of(TYPES.get(shot)));
            mc.mouseHandler.releaseMouse();
        }
        if (local == 40) {
            String name = TYPES.get(shot).getUid().getPath();
            Screenshot.grab(mc.gameDirectory, "jei-" + shot + "-" + name + ".png", mc.getMainRenderTarget(), message -> {});
            Hemomancy.LOGGER.info("JEI_REVIEW captured {} {}", shot, name);
            shot++;
        }
    }
}
