package com.vincenthuto.hemomancy.common.mission;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class MnemonicRecipeKnowledge {
    private MnemonicRecipeKnowledge() {
    }

    public static int awardStarter(ServerPlayer player) {
        return award(player, MnemonicRecipeKnowledgeRules.starterRecipePaths());
    }

    public static int awardCatalogue(ServerPlayer player) {
        return award(player, MnemonicRecipeKnowledgeRules.catalogueRecipePaths());
    }

    public static boolean knowsCatalogue(ServerPlayer player) {
        return MnemonicRecipeKnowledgeRules.knowsCatalogue(
                HarbingerAdvancementGranter.hasAdvancement(player, BodyAnswersAssignmentHelper.ADV_COMPLETE),
                HarbingerAdvancementGranter.isMnemonistWovenVesselComplete(player));
    }

    private static int award(ServerPlayer player, Collection<String> paths) {
        List<RecipeHolder<?>> recipes = new ArrayList<>();
        for (String path : paths) {
            player.server.getRecipeManager().byKey(Hemomancy.rloc(path)).ifPresent(recipes::add);
        }
        return recipes.isEmpty() ? 0 : player.awardRecipes(recipes);
    }
}
