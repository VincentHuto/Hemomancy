package com.vincenthuto.hemomancy.compat.jei;

import com.vincenthuto.hemomancy.common.brewing.AlembicTier;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
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
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * JEI category for the upgraded alembic's brews: base potion and catalysts into the brew, with the alembic
 * stage that can hold it shown as its upgrade kit.
 */
public final class AdvancedBrewingRecipeCategory implements IRecipeCategory<AdvancedBrewingDisplay> {
    public static final RecipeType<AdvancedBrewingDisplay> TYPE =
            RecipeType.create("hemomancy", "advanced_brewing", AdvancedBrewingDisplay.class);

    private static final int W = 176;
    private static final int H = 92;
    private static final int FOOTER_Y = 76;
    private static final int SLOT_Y = 32;
    private static final int BASE_X = 10;
    private static final int CATALYST_X = 40;
    private static final int CATALYST2_X = 66;
    private static final int OUTPUT_X = 116;
    private static final int TIER_X = 148;

    private final IDrawable icon;

    public AdvancedBrewingRecipeCategory(IGuiHelper helper) {
        icon = helper.createDrawableItemStack(new ItemStack(BlockInit.ghastly_alembic.get()));
    }

    @Override public RecipeType<AdvancedBrewingDisplay> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return Component.translatable("hemomancy.jei.advanced_brewing"); }
    @Override public int getWidth() { return W; }
    @Override public int getHeight() { return H; }
    @Override public IDrawable getIcon() { return icon; }

    private static ItemStack tierItem(AlembicTier tier) {
        return new ItemStack(tier == AlembicTier.ATHANOR ? ItemInit.sanguine_athanor_kit.get() : ItemInit.hematic_condenser_kit.get());
    }

    private static String tierKey(AlembicTier tier) {
        return tier == AlembicTier.ATHANOR ? "athanor" : "condenser";
    }

    @Override public void setRecipe(IRecipeLayoutBuilder builder, AdvancedBrewingDisplay recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, BASE_X, SLOT_Y).addItemStack(recipe.input());
        builder.addSlot(RecipeIngredientRole.INPUT, CATALYST_X, SLOT_Y).addItemStack(recipe.catalyst());
        if (!recipe.catalyst2().isEmpty())
            builder.addSlot(RecipeIngredientRole.INPUT, CATALYST2_X, SLOT_Y).addItemStack(recipe.catalyst2());
        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, SLOT_Y).addItemStack(recipe.output());
        String tier = tierKey(recipe.tier());
        builder.addSlot(RecipeIngredientRole.CATALYST, TIER_X, SLOT_Y).addItemStack(tierItem(recipe.tier()))
                .addRichTooltipCallback((view, tip) -> tip.add(Component.translatable(
                        "hemomancy.jei.advanced_brewing.tier." + tier + ".desc").withStyle(ChatFormatting.GRAY)));
    }

    private static List<JeiPanel.Stat> footer(AdvancedBrewingDisplay recipe) {
        JeiPanel.Theme t = JeiPanel.BLOOD;
        return List.of(
                JeiPanel.Stat.of(JeiPanel.Icon.DEGREE, Component.translatable("hemomancy.jei.stat.degree", recipe.degree()),
                        t.title(), Component.translatable("hemomancy.jei.craft.degree_full", recipe.degree())),
                JeiPanel.Stat.of(JeiPanel.Icon.BLOOD, Component.translatable("hemomancy.jei.stat.blood", recipe.blood()),
                        JeiPanel.BLOOD_RED, Component.translatable("hemomancy.jei.distillation.blood.tip")),
                JeiPanel.Stat.of(JeiPanel.Icon.TIME, JeiPanel.secondsText(recipe.ticks()), t.text(),
                        Component.translatable("hemomancy.jei.alembic.athanor_hint")));
    }

    @Override public void draw(AdvancedBrewingDisplay recipe, IRecipeSlotsView slots, GuiGraphics gfx,
                               double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        JeiPanel.Theme t = JeiPanel.BLOOD;
        float time = JeiPanel.time();
        boolean second = !recipe.catalyst2().isEmpty();

        JeiPanel.panel(gfx, t, W, H, FOOTER_Y);
        JeiPanel.title(gfx, font, t, Component.translatable("hemomancy.jei.advanced_brewing." + recipe.note()), 6, 5, W - 12, W);

        JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.advanced_brewing.base"), BASE_X + 8, SLOT_Y - 10, t.muted());
        JeiPanel.slot(gfx, t, BASE_X, SLOT_Y);
        JeiPanel.plus(gfx, (BASE_X + 16 + CATALYST_X) / 2, SLOT_Y + 8, t.text());
        JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.distillation.catalyst"), CATALYST_X + 8, SLOT_Y - 10, t.muted());
        JeiPanel.slot(gfx, t, CATALYST_X, SLOT_Y);
        if (second) {
            JeiPanel.plus(gfx, (CATALYST_X + 16 + CATALYST2_X) / 2, SLOT_Y + 8, t.text());
            JeiPanel.slot(gfx, t, CATALYST2_X, SLOT_Y);
        }

        JeiProgressArrow.draw(gfx, (second ? CATALYST2_X : CATALYST_X) + 22, SLOT_Y + 7, OUTPUT_X - 8, time, t);
        JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.advanced_brewing.brew"), OUTPUT_X + 8, SLOT_Y - 12, t.muted());
        JeiPanel.resultSlot(gfx, t, OUTPUT_X, SLOT_Y, 0xFFC04A8A, time);

        JeiPanel.slot(gfx, t, TIER_X, SLOT_Y);
        JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.advanced_brewing.tier." + tierKey(recipe.tier())),
                TIER_X + 8, SLOT_Y + 20, recipe.tier() == AlembicTier.ATHANOR ? 0xFFFF7AD0 : 0xFFE8506A, 2 * (W - TIER_X - 8) - 4);

        JeiPanel.wrapped(gfx, font, Component.translatable("hemomancy.jei.advanced_brewing." + recipe.note() + ".hint"),
                8, 58, W - 16, t.text());
        JeiPanel.footer(gfx, font, footer(recipe), W, FOOTER_Y);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, AdvancedBrewingDisplay recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        JeiPanel.footerTooltip(tooltip, Minecraft.getInstance().font, footer(recipe), W, FOOTER_Y, H, mouseX, mouseY);
    }
}
