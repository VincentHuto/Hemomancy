package com.vincenthuto.hemomancy.compat.jei;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.recipe.EnzymeFruitingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class EnzymeFruitingRecipeCategory implements IRecipeCategory<EnzymeFruitingRecipe> {

    public static final RecipeType<EnzymeFruitingRecipe> JEI_TYPE =
            RecipeType.create("hemomancy", "enzyme_fruiting", EnzymeFruitingRecipe.class);

    private static final int WIDTH = 150;
    private static final int HEIGHT = 70;
    private final IDrawable icon;

    public EnzymeFruitingRecipeCategory(IGuiHelper helpers) {
        this.icon = helpers.createDrawableItemStack(new ItemStack(BlockInit.mycelial_lantern.get()));
    }

    @Override
    @Nonnull
    public RecipeType<EnzymeFruitingRecipe> getRecipeType() {
        return JEI_TYPE;
    }

    @Override
    @Nonnull
    public Component getTitle() {
        return Component.literal("Enzyme Fruiting");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    @Nonnull
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull EnzymeFruitingRecipe recipe,
            @Nonnull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST, 22, 28)
                .addIngredients(recipe.getCulture());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 112, 28)
                .addItemStack(recipe.getResultItemRaw().copy());
    }

    @Override
    public void draw(@Nonnull EnzymeFruitingRecipe recipe, @Nonnull IRecipeSlotsView slots,
            @Nonnull GuiGraphics gfx, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        gfx.fill(0, 0, WIDTH, HEIGHT, 0xFF130A05);
        gfx.fill(0, 0, WIDTH, 1, 0xFF8C4A12);
        gfx.fill(0, HEIGHT - 1, WIDTH, HEIGHT, 0xFF8C4A12);
        gfx.fill(0, 0, 1, HEIGHT, 0xFF8C4A12);
        gfx.fill(WIDTH - 1, 0, WIDTH, HEIGHT, 0xFF8C4A12);
        drawSlot(gfx, 21, 27);
        drawSlot(gfx, 111, 27);
        gfx.fill(44, 35, 104, 38, 0xFFDD8A22);
        gfx.fill(99, 31, 104, 42, 0xFFDD8A22);
        String blood = "Blood: " + (int) recipe.getTotalBloodCost();
        gfx.drawString(font, blood, WIDTH / 2 - font.width(blood) / 2, 8, 0xFFE0A452, false);
        String time = recipe.getDuration() / 20 + "s";
        gfx.drawString(font, time, WIDTH / 2 - font.width(time) / 2, 52, 0xFF8C6B3A, false);
    }

    private void drawSlot(GuiGraphics gfx, int x, int y) {
        gfx.fill(x - 1, y - 1, x + 18, y + 18, 0xFF080403);
        gfx.fill(x, y, x + 17, y + 17, 0xFF241208);
        gfx.fill(x + 17, y, x + 18, y + 18, 0xFF5B3210);
        gfx.fill(x, y + 17, x + 18, y + 18, 0xFF5B3210);
    }
}
