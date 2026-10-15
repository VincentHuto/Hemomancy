package com.vincenthuto.hemomancy.compat.jei;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.recipe.FungalScarCultivationRecipe;
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

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Locale;

/**
 * JEI category for the Mycelial Crucible's two-phase fungal scar cultivation: blood grows the seed into an
 * immature culture, then aligned enzymes mature it into the scar. Everything is tinted by the recipe's tendency.
 */
public class MycelialCrucibleRecipeCategory implements IRecipeCategory<FungalScarCultivationRecipe> {

    public static final RecipeType<FungalScarCultivationRecipe> JEI_TYPE =
            RecipeType.create("hemomancy", "mycelial_crucible", FungalScarCultivationRecipe.class);

    private static final int W = 176;
    private static final int H = 102;
    private static final int FOOTER_Y = 86;
    private static final int SLOT_Y = 34;
    private static final int SEED_X = 12;
    private static final int IMMATURE_X = 78;
    private static final int OUTPUT_X = 146;
    private static final int ENZYME_X = 108;
    private static final int ENZYME_Y = 58;

    private final IDrawable icon;

    public MycelialCrucibleRecipeCategory(IGuiHelper helpers) {
        this.icon = helpers.createDrawableItemStack(new ItemStack(BlockInit.mycelial_crucible.get()));
    }

    @Override @Nonnull public RecipeType<FungalScarCultivationRecipe> getRecipeType() { return JEI_TYPE; }
    @Override @Nonnull public Component getTitle() { return Component.translatable("block.hemomancy.mycelial_crucible"); }
    @Override public int getWidth() { return W; }
    @Override public int getHeight() { return H; }
    @Override @Nonnull public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull FungalScarCultivationRecipe recipe,
            @Nonnull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, SEED_X, SLOT_Y)
                .addItemStack(recipe.getSeedItemStack());
        builder.addSlot(RecipeIngredientRole.CATALYST, IMMATURE_X, SLOT_Y)
                .addItemStack(recipe.getImmatureResult())
                .addRichTooltipCallback((view, tip) -> tip.add(Component.translatable(
                        "hemomancy.jei.crucible.immature.tip").withStyle(ChatFormatting.GRAY)));
        int power = recipe.getMaturationThreshold();
        builder.addSlot(RecipeIngredientRole.INPUT, ENZYME_X, ENZYME_Y)
                .addItemStack(new ItemStack(EnumBloodTendency.getRepEnzyme(recipe.getTendency())))
                .addRichTooltipCallback((view, tip) -> tip.add(Component.translatable(
                        "hemomancy.jei.crucible.enzyme.tip", power).withStyle(ChatFormatting.GRAY)));
        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, SLOT_Y)
                .addItemStack(recipe.getResultItemStack().copy());
    }

    private static List<JeiPanel.Stat> footer(FungalScarCultivationRecipe recipe) {
        JeiPanel.Theme t = JeiPanel.FUNGAL;
        return List.of(
                JeiPanel.Stat.of(JeiPanel.Icon.BLOOD, Component.translatable("hemomancy.jei.stat.blood",
                        (int) recipe.getBloodCostPhase1()), JeiPanel.BLOOD_RED,
                        Component.translatable("hemomancy.jei.crucible.blood.tip")),
                JeiPanel.Stat.of(JeiPanel.Icon.TIME, JeiPanel.secondsText(recipe.getPhase1Duration()), t.text(),
                        Component.translatable("hemomancy.jei.crucible.time.tip")));
    }

    private static int tendColor(FungalScarCultivationRecipe recipe) {
        return JeiPanel.brighten(recipe.getTendency().getColor().getColor());
    }

    @Override
    public void draw(@Nonnull FungalScarCultivationRecipe recipe,
            @Nonnull IRecipeSlotsView slots, @Nonnull GuiGraphics gfx, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        JeiPanel.Theme t = JeiPanel.FUNGAL;
        float time = JeiPanel.time();
        int tend = tendColor(recipe);
        String tendName = recipe.getTendency().name().toLowerCase(Locale.ROOT);

        JeiPanel.panel(gfx, t, W, H, FOOTER_Y);
        JeiPanel.diamond(gfx, 9, 8, 3, JeiPanel.darken(tend, 0.5f));
        JeiPanel.diamond(gfx, 9, 8, 1, tend);
        JeiPanel.title(gfx, font, t, Component.translatable("hemomancy.jei.crucible.alignment",
                Character.toUpperCase(tendName.charAt(0)) + tendName.substring(1)), 16, 4, W - 22, W);

        JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.crucible.seed"), SEED_X + 8, SLOT_Y - 11, t.muted());
        JeiPanel.slot(gfx, t, SEED_X, SLOT_Y);

        // Phase I: blood grows the culture
        JeiProgressArrow.draw(gfx, SEED_X + 22, SLOT_Y + 7, IMMATURE_X - 8, time, t);
        JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.crucible.phase1"), (SEED_X + IMMATURE_X) / 2 + 8,
                SLOT_Y + 13, t.muted());

        JeiPanel.glow(gfx, IMMATURE_X + 8, SLOT_Y + 8, 18, 18, tend, 0.6f + 0.4f * JeiPanel.pulse(time, 1.7f));
        JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.crucible.culture"), IMMATURE_X + 8, SLOT_Y - 11, t.muted());
        JeiPanel.tintedSlot(gfx, t, IMMATURE_X, SLOT_Y, JeiPanel.darken(tend, 0.8f));

        // Phase II: aligned enzymes mature it, fed up from the enzyme socket
        JeiProgressArrow.draw(gfx, IMMATURE_X + 22, SLOT_Y + 7, OUTPUT_X - 8, time + 0.45f, t);
        JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.crucible.phase2"), (IMMATURE_X + OUTPUT_X) / 2 + 8,
                SLOT_Y - 6, t.muted());
        int feedX = ENZYME_X + 8;
        gfx.fill(feedX, SLOT_Y + 11, feedX + 1, ENZYME_Y - 1, JeiPanel.darken(tend, 0.5f));
        float p = (time * 0.8f) % 1f;
        int my = Math.round(ENZYME_Y - 2 - p * (ENZYME_Y - SLOT_Y - 13));
        gfx.fill(feedX - 1, my, feedX + 2, my + 2, JeiPanel.withAlpha(tend, (int) (240 * (1f - p * 0.6f))));
        JeiPanel.tintedSlot(gfx, t, ENZYME_X, ENZYME_Y, tend);
        JeiPanel.labelLeft(gfx, font, Component.translatable("hemomancy.jei.crucible.power", recipe.getMaturationThreshold()),
                ENZYME_X + 21, ENZYME_Y + 5, tend);

        JeiPanel.label(gfx, font, Component.translatable("hemomancy.jei.crucible.scar"), OUTPUT_X + 8, SLOT_Y - 13, t.muted());
        JeiPanel.resultSlot(gfx, t, OUTPUT_X, SLOT_Y, tend, time);

        JeiPanel.footer(gfx, font, footer(recipe), W, FOOTER_Y);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, FungalScarCultivationRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        JeiPanel.footerTooltip(tooltip, Minecraft.getInstance().font, footer(recipe), W, FOOTER_Y, H, mouseX, mouseY);
    }
}
