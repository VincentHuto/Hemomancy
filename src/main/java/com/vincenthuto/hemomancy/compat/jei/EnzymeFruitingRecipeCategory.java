package com.vincenthuto.hemomancy.compat.jei;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.recipe.EnzymeFruitingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

/** JEI category for the Mycelial Lantern: a resident culture fruits its enzyme while the lantern drinks blood. */
public class EnzymeFruitingRecipeCategory implements IRecipeCategory<EnzymeFruitingRecipe> {

    public static final RecipeType<EnzymeFruitingRecipe> JEI_TYPE =
            RecipeType.create("hemomancy", "enzyme_fruiting", EnzymeFruitingRecipe.class);

    private static final int W = 176;
    private static final int H = 74;
    private static final int FOOTER_Y = 58;
    private static final int SLOT_Y = 28;
    private static final int CULTURE_X = 18;
    private static final int OUTPUT_X = 140;

    private final IDrawable icon;

    public EnzymeFruitingRecipeCategory(IGuiHelper helpers) {
        this.icon = helpers.createDrawableItemStack(new ItemStack(BlockInit.mycelial_lantern.get()));
    }

    @Override @Nonnull public RecipeType<EnzymeFruitingRecipe> getRecipeType() { return JEI_TYPE; }
    @Override @Nonnull public Component getTitle() { return Component.translatable("hemomancy.jei.enzyme_fruiting"); }
    @Override public int getWidth() { return W; }
    @Override public int getHeight() { return H; }
    @Override @Nonnull public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull EnzymeFruitingRecipe recipe,
            @Nonnull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST, CULTURE_X, SLOT_Y)
                .addIngredients(recipe.getCulture())
                .addRichTooltipCallback((view, tip) -> tip.add(Component.translatable(
                        "hemomancy.jei.enzyme_fruiting.culture.tip").withStyle(ChatFormatting.GRAY)));
        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, SLOT_Y)
                .addItemStack(recipe.getResultItemRaw().copy());
    }

    private static List<JeiPanel.Stat> footer(EnzymeFruitingRecipe recipe) {
        return List.of(
                JeiPanel.Stat.of(JeiPanel.Icon.BLOOD, Component.translatable("hemomancy.jei.stat.blood",
                        (int) recipe.getTotalBloodCost()), JeiPanel.BLOOD_RED,
                        Component.translatable("hemomancy.jei.enzyme_fruiting.blood.tip")),
                JeiPanel.Stat.of(JeiPanel.Icon.TIME, JeiPanel.secondsText(recipe.getDuration()), JeiPanel.LANTERN.text()));
    }

    @Override
    public void draw(@Nonnull EnzymeFruitingRecipe recipe, @Nonnull IRecipeSlotsView slots,
            @Nonnull GuiGraphics gfx, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        JeiPanel.Theme t = JeiPanel.LANTERN;
        float time = JeiPanel.time();

        JeiPanel.panel(gfx, t, W, H, FOOTER_Y);
        JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.enzyme_fruiting.culture"), CULTURE_X + 8, SLOT_Y - 11, t.muted());
        JeiPanel.slot(gfx, t, CULTURE_X, SLOT_Y);
        JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.enzyme_fruiting.kept"), CULTURE_X + 8, SLOT_Y + 20, t.muted());

        // The lantern's glow, with spores lifting off the stream between culture and fruit
        int cx = (CULTURE_X + OUTPUT_X) / 2 + 8;
        JeiPanel.glow(gfx, cx, SLOT_Y + 8, 34, 14, t.arrowLit(), 0.6f + 0.4f * JeiPanel.pulse(time, 1.6f));
        for (int i = 0; i < 7; i++) {
            float p = (time * (0.22f + i * 0.03f) + i * 0.37f) % 1f;
            int sx = cx - 26 + (i * 9) % 52 + Math.round(Mth.sin(time * 1.3f + i) * 2f);
            int sy = SLOT_Y + 6 - Math.round(p * 22);
            gfx.fill(sx, sy, sx + 1, sy + 1, JeiPanel.withAlpha(t.arrowLit(), (int) (230 * (1f - p))));
        }
        JeiProgressArrow.draw(gfx, CULTURE_X + 24, SLOT_Y + 7, OUTPUT_X - 8, time, t);

        JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.enzyme_fruiting.fruit"), OUTPUT_X + 8, SLOT_Y - 13, t.muted());
        JeiPanel.resultSlot(gfx, t, OUTPUT_X, SLOT_Y, t.accent(), time);

        JeiPanel.footer(gfx, font, footer(recipe), W, FOOTER_Y);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, EnzymeFruitingRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        JeiPanel.footerTooltip(tooltip, Minecraft.getInstance().font, footer(recipe), W, FOOTER_Y, H, mouseX, mouseY);
    }
}
