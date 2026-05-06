package com.vincenthuto.hemomancy.compat.jei;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.recipe.FungalScarCultivationRecipe;
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

/**
 * JEI recipe category for the Mycelial Crucible — two-phase fungal scar cultivation.
 *
 * <p>Displays the configured seed as input, the immature culture as
 * intermediate, and the final scar as output. A small progress arc and tendency label
 * communicate the alignment requirement.
 */
public class MycelialCrucibleRecipeCategory implements IRecipeCategory<FungalScarCultivationRecipe> {

    public static final RecipeType<FungalScarCultivationRecipe> JEI_TYPE =
            RecipeType.create("hemomancy", "mycelial_crucible", FungalScarCultivationRecipe.class);

    private static final int BG_W = 170;
    private static final int BG_H = 90;

    // Input slot: configured seed ingredient
    private static final int SEED_X = 20;
    private static final int SEED_Y = 37;

    // Intermediate: immature scar
    private static final int IMMATURE_X = 76;
    private static final int IMMATURE_Y = 37;

    // Output: mature scar
    private static final int OUTPUT_X = 134;
    private static final int OUTPUT_Y = 37;

    private static final int BG_COLOR      = 0xFF070A04;
    private static final int BORDER_COLOR  = 0xFF1A2C0A;
    private static final int SLOT_BG       = 0xFF101807;
    private static final int SLOT_BORDER_D = 0xFF090E03;
    private static final int SLOT_BORDER_L = 0xFF223A0C;
    private static final int ARROW_COLOR   = 0xFF44AA33;
    private static final int LABEL_COLOR   = 0xFF66CC44;

    private final IDrawable icon;

    public MycelialCrucibleRecipeCategory(IGuiHelper helpers) {
        this.icon       = helpers.createDrawableItemStack(new ItemStack(BlockInit.mycelial_crucible.get()));
    }

    @Override @Nonnull
    public RecipeType<FungalScarCultivationRecipe> getRecipeType() { return JEI_TYPE; }

    @Override @Nonnull
    public Component getTitle() { return Component.literal("Mycelial Crucible"); }

    @Override
    public int getWidth() { return BG_W; }

    @Override
    public int getHeight() { return BG_H; }

    @Override @Nonnull
    public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder,
            @Nonnull FungalScarCultivationRecipe recipe,
            @Nonnull IFocusGroup focuses) {
        // Seed: the item consumed to begin Phase 1
        builder.addSlot(RecipeIngredientRole.INPUT, SEED_X + 1, SEED_Y + 1)
                .addItemStack(recipe.getSeedItemStack());

        // Immature intermediate
        builder.addSlot(RecipeIngredientRole.CATALYST, IMMATURE_X + 1, IMMATURE_Y + 1)
                .addItemStack(recipe.getImmatureResult());

        // Output: finished scar
        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X + 1, OUTPUT_Y + 1)
                .addItemStack(recipe.getResultItemStack().copy());
    }

    @Override
    public void draw(@Nonnull FungalScarCultivationRecipe recipe,
            @Nonnull IRecipeSlotsView slots, @Nonnull GuiGraphics gfx, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;

        // Background
        gfx.fill(0, 0, BG_W, BG_H, BG_COLOR);
        gfx.fill(0, 0, BG_W, 1, BORDER_COLOR);
        gfx.fill(0, BG_H - 1, BG_W, BG_H, BORDER_COLOR);
        gfx.fill(0, 0, 1, BG_H, BORDER_COLOR);
        gfx.fill(BG_W - 1, 0, BG_W, BG_H, BORDER_COLOR);

        // Slot decorations
        drawSlot(gfx, SEED_X, SEED_Y);
        drawSlot(gfx, IMMATURE_X, IMMATURE_Y);
        drawSlot(gfx, OUTPUT_X, OUTPUT_Y);

        // Arrow: seed → immature
        drawArrow(gfx, SEED_X + 18, SEED_Y + 7, IMMATURE_X - 4, IMMATURE_Y + 7);

        // Arrow: immature → output (with enzymes label)
        drawArrow(gfx, IMMATURE_X + 18, IMMATURE_Y + 7, OUTPUT_X - 4, OUTPUT_Y + 7);

        // Phase labels

        gfx.drawString(font, "Phase 1", SEED_X, SEED_Y - 10, LABEL_COLOR, false);
        gfx.drawString(font, "Phase 2", IMMATURE_X, IMMATURE_Y - 10, LABEL_COLOR, false);

        // Tendency label
        String tend = "Alignment: " + recipe.getTendency().name();
        gfx.drawString(font, tend, BG_W / 2 - font.width(tend) / 2, 2, LABEL_COLOR, false);

        // Blood cost label
        String blood = "Blood: " + (int) recipe.getBloodCostPhase1() + " + " + recipe.getMaturationThreshold() + " enzyme pts";
        gfx.drawString(font, blood, BG_W / 2 - font.width(blood) / 2, BG_H - 12, 0xFF5A6030, false);
    }

    private void drawSlot(GuiGraphics gfx, int x, int y) {
        gfx.fill(x - 1, y - 1, x + 17, y + 17, SLOT_BORDER_D);
        gfx.fill(x, y, x + 16, y + 16, SLOT_BG);
        gfx.fill(x + 16, y, x + 17, y + 17, SLOT_BORDER_L);
        gfx.fill(x, y + 16, x + 17, y + 17, SLOT_BORDER_L);
    }

    private void drawArrow(GuiGraphics gfx, int x1, int y1, int x2, int y2) {
        for (int x = x1; x < x2 - 4; x++) {
            gfx.fill(x, y1, x + 1, y1 + 2, ARROW_COLOR);
        }
        int ax = x2 - 4;
        gfx.fill(ax,     y1 - 2, ax + 1, y1 + 4, ARROW_COLOR);
        gfx.fill(ax + 1, y1 - 1, ax + 2, y1 + 3, ARROW_COLOR);
        gfx.fill(ax + 2, y1,     ax + 3, y1 + 2, ARROW_COLOR);
        gfx.fill(ax + 3, y1,     ax + 4, y1 + 2, ARROW_COLOR);
    }
}
