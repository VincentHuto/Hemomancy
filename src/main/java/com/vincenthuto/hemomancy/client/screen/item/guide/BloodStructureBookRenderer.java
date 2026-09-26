package com.vincenthuto.hemomancy.client.screen.item.guide;

import com.vincenthuto.hemomancy.common.data.book.BloodStructurePageTemplate;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import com.vincenthuto.hutoslib.client.screen.guide.BookBodyRenderer;
import com.vincenthuto.hutoslib.client.screen.guide.BookBuiltinRenderers;
import com.vincenthuto.hutoslib.client.screen.guide.BookPageRenderContext;
import net.minecraft.world.item.ItemStack;

public final class BloodStructureBookRenderer implements BookBodyRenderer {
    public static final BloodStructureBookRenderer INSTANCE = new BloodStructureBookRenderer();
    private BloodStructureBookRenderer() {}

    private BloodStructureRecipe recipe(BookPageRenderContext context) {
        return BloodStructureRecipe.getStructureByLocation(HLClientUtils.getWorld(), ((BloodStructurePageTemplate) context.page()).getStructureKey());
    }
    @Override public void render(BookPageRenderContext context) {
        var recipe = recipe(context);
        if (recipe == null) return;
        int x = context.body().x(), y = context.body().y() - context.scrollOffset();
        context.graphics().renderItem(recipe.getHeldItem(),x,y);
        context.graphics().renderItem(new ItemStack(recipe.getHitBlock().asItem()),x+20,y);
        BookBuiltinRenderers.renderMultiblock(context,recipe.getPattern(),24);
    }
    @Override public int contentHeight(BookPageRenderContext context) {
        var recipe = recipe(context);
        return recipe == null ? 0 : 134 + recipe.getPattern().getMaterialCounts(false).size()*10;
    }
}
