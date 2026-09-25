package com.vincenthuto.hemomancy.compat.jei;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class AdvancedBrewingRecipeCategory implements IRecipeCategory<AdvancedBrewingDisplay> {
    public static final RecipeType<AdvancedBrewingDisplay> TYPE =
            RecipeType.create("hemomancy", "advanced_brewing", AdvancedBrewingDisplay.class);
    private final IDrawable icon;

    public AdvancedBrewingRecipeCategory(IGuiHelper helper) {
        icon = helper.createDrawableItemStack(new ItemStack(BlockInit.ghastly_alembic.get()));
    }

    @Override public RecipeType<AdvancedBrewingDisplay> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return Component.translatable("hemomancy.jei.advanced_brewing"); }
    @Override public int getWidth() { return 170; }
    @Override public int getHeight() { return 86; }
    @Override public IDrawable getIcon() { return icon; }

    @Override public void setRecipe(IRecipeLayoutBuilder builder, AdvancedBrewingDisplay recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 14, 30).addItemStack(recipe.input());
        builder.addSlot(RecipeIngredientRole.INPUT, 46, 30).addItemStack(recipe.catalyst());
        if (!recipe.catalyst2().isEmpty())
            builder.addSlot(RecipeIngredientRole.INPUT, 78, 30).addItemStack(recipe.catalyst2());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 137, 30).addItemStack(recipe.output());
    }

    @Override public void draw(AdvancedBrewingDisplay recipe, IRecipeSlotsView slots, GuiGraphics gfx,
                               double mouseX, double mouseY) {
        gfx.fill(0, 0, 170, 86, 0xFF120609);
        gfx.fill(0, 0, 170, 1, 0xFF7C2938);
        gfx.fill(0, 85, 170, 86, 0xFF7C2938);
        var font = Minecraft.getInstance().font;
        gfx.drawString(font, Component.translatable("hemomancy.jei.advanced_brewing." + recipe.note()),
                10, 8, 0xFFE4B8B5, false);
        gfx.drawString(font, Component.translatable("hemomancy.jei.advanced_brewing.cost",
                        recipe.degree(), recipe.blood(), recipe.ticks() / 20),
                10, 61, 0xFFD7948B, false);
        gfx.drawString(font, Component.translatable("hemomancy.jei.advanced_brewing." + recipe.note() + ".hint"),
                10, 73, 0xFFB9A4A4, false);
    }
}
