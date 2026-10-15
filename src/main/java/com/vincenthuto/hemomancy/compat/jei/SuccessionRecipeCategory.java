package com.vincenthuto.hemomancy.compat.jei;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import mezz.jei.api.constants.VanillaTypes;
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

/**
 * JEI category for Hematic Succession and Mnemonic Restoration: the Vacant Effigy and its quintessence, the three
 * numbered brazier sockets beside it, and the staff and Focus that conduct the rite.
 */
public final class SuccessionRecipeCategory implements IRecipeCategory<CardinalRiteRecipe> {
    public static final RecipeType<CardinalRiteRecipe> TYPE = RecipeType.create(Hemomancy.MOD_ID, "succession", CardinalRiteRecipe.class);

    private static final int W = 180;
    private static final int H = 122;
    private static final int TOP_Y = 30;
    private static final int EFFIGY_X = 12;
    private static final int LIFE_X = 40;
    private static final int[] BRAZIER_X = { 96, 122, 148 };
    private static final int TOOLS_Y = 98;
    private static final int STAFF_X = 12;
    private static final int FOCUS_X = 34;

    private final IDrawable icon;

    public SuccessionRecipeCategory(IGuiHelper helper) {
        icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BlockInit.vacant_effigy.get()));
    }

    @Override public RecipeType<CardinalRiteRecipe> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return Component.translatable("hemomancy.jei.succession"); }
    @Override public int getWidth() { return W; }
    @Override public int getHeight() { return H; }
    @Override public IDrawable getIcon() { return icon; }

    private static boolean restoring(CardinalRiteRecipe recipe) {
        return recipe.getId().getPath().endsWith("mnemonic_restoration");
    }

    @Override public void setRecipe(IRecipeLayoutBuilder builder, CardinalRiteRecipe recipe, IFocusGroup focus) {
        builder.addSlot(RecipeIngredientRole.INPUT, EFFIGY_X, TOP_Y).addItemStack(new ItemStack(BlockInit.vacant_effigy.get()));
        builder.addSlot(RecipeIngredientRole.INPUT, LIFE_X, TOP_Y).addItemStack(new ItemStack(ItemInit.sanguine_quintessence.get()));
        builder.addSlot(RecipeIngredientRole.INPUT, BRAZIER_X[0], TOP_Y).addItemStack(new ItemStack(
                restoring(recipe) ? ItemInit.bound_mnemonic_remnant.get() : ItemInit.bloody_vial.get()));
        builder.addSlot(RecipeIngredientRole.INPUT, BRAZIER_X[1], TOP_Y).addItemStack(new ItemStack(ItemInit.bloody_vial.get()));
        if (!restoring(recipe))
            builder.addSlot(RecipeIngredientRole.INPUT, BRAZIER_X[2], TOP_Y).addItemStack(new ItemStack(ItemInit.mnemonic_ambergris.get()));
        builder.addSlot(RecipeIngredientRole.CATALYST, STAFF_X, TOOLS_Y).addItemStack(new ItemStack(ItemInit.living_staff.get()));
        builder.addSlot(RecipeIngredientRole.CATALYST, FOCUS_X, TOOLS_Y).addItemStack(new ItemStack(BlockInit.cardinal_focus.get()));
    }

    @Override public void draw(CardinalRiteRecipe recipe, IRecipeSlotsView slots, GuiGraphics gfx, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        JeiPanel.Theme t = JeiPanel.BLOOD;
        float time = JeiPanel.time();
        boolean restoring = restoring(recipe);

        JeiPanel.panel(gfx, t, W, H, 0);
        JeiPanel.title(gfx, font, t, Component.translatable(restoring ? "hemomancy.succession.restoration" : "hemomancy.jei.succession"),
                6, 4, W - 12, W);

        // Effigy and the life poured into it
        JeiPanel.label(gfx, font, Component.translatable("hemomancy.succession.jei.effigy"), (EFFIGY_X + LIFE_X) / 2 + 8, TOP_Y - 11, t.muted());
        JeiPanel.glow(gfx, EFFIGY_X + 8, TOP_Y + 8, 14, 14, 0xFF8A1A22, 0.5f + 0.5f * JeiPanel.pulse(time, 1.5f));
        JeiPanel.tintedSlot(gfx, t, EFFIGY_X, TOP_Y, 0xFF8A1A22);
        JeiPanel.plus(gfx, (EFFIGY_X + 16 + LIFE_X) / 2, TOP_Y + 8, t.text());
        JeiPanel.slot(gfx, t, LIFE_X, TOP_Y);
        JeiPanel.label(gfx, font, Component.translatable("hemomancy.succession.jei.form"), EFFIGY_X + 8, TOP_Y + 20, t.text());
        JeiPanel.label(gfx, font, Component.translatable("hemomancy.succession.jei.life"), LIFE_X + 8, TOP_Y + 20, t.text());

        // Thread from the effigy to the brazier row
        JeiProgressArrow.draw(gfx, LIFE_X + 22, TOP_Y + 7, BRAZIER_X[0] - 8, time, t);

        // The three numbered brazier sockets
        for (int i = 0; i < 3; i++) {
            int x = BRAZIER_X[i];
            if (restoring && i == 2) {
                JeiPanel.ghostSlot(gfx, t, x, TOP_Y);
            } else {
                JeiPanel.flame(gfx, x + 8, TOP_Y - 2, time + i * 0.7f, 0x668A1612, 0x99CA4C28, 0xCCE8A447);
                JeiPanel.slot(gfx, t, x, TOP_Y);
            }
            JeiPanel.diamond(gfx, x + 8, TOP_Y + 23, 4, restoring && i == 2 ? t.ring() : t.frameLight());
            JeiPanel.label(gfx, font, Component.literal(String.valueOf(i + 1)), x + 8, TOP_Y + 21, t.title());
        }

        // How to fill the sockets
        gfx.fill(4, 62, W - 4, 63, t.ring());
        JeiPanel.wrapped(gfx, font, Component.translatable(restoring ? "hemomancy.succession.jei.restore" : "hemomancy.succession.jei.create"),
                8, 67, W - 16, t.text());

        // Tools and the rite's conditions
        gfx.fill(4, TOOLS_Y - 7, W - 4, TOOLS_Y - 6, t.ring());
        JeiPanel.slot(gfx, t, STAFF_X, TOOLS_Y);
        JeiPanel.slot(gfx, t, FOCUS_X, TOOLS_Y);
        JeiPanel.wrapped(gfx, font, Component.translatable("hemomancy.succession.jei.requirements"), 58, TOOLS_Y + 1, W - 64,
                0xFFC07070);
    }
}
