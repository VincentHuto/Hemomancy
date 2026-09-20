package com.vincenthuto.hemomancy.compat.jei;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class SuccessionRecipeCategory implements IRecipeCategory<CardinalRiteRecipe> {
    public static final RecipeType<CardinalRiteRecipe> TYPE=RecipeType.create(Hemomancy.MOD_ID,"succession",CardinalRiteRecipe.class);
    private final IDrawable background,icon;
    public SuccessionRecipeCategory(IGuiHelper helper) {
        background=helper.createBlankDrawable(180,140);
        icon=helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,new ItemStack(BlockInit.vacant_effigy.get()));
    }
    @Override public RecipeType<CardinalRiteRecipe> getRecipeType(){return TYPE;}
    @Override public Component getTitle(){return Component.translatable("hemomancy.jei.succession");}
    @Override public IDrawable getBackground(){return background;}
    @Override public IDrawable getIcon(){return icon;}
    private static boolean restoring(CardinalRiteRecipe recipe){return recipe.getId().getPath().endsWith("mnemonic_restoration");}
    @Override public void setRecipe(IRecipeLayoutBuilder builder,CardinalRiteRecipe recipe,IFocusGroup focus) {
        builder.addSlot(RecipeIngredientRole.INPUT,10,24).addItemStack(new ItemStack(BlockInit.vacant_effigy.get()));
        builder.addSlot(RecipeIngredientRole.INPUT,40,24).addItemStack(new ItemStack(ItemInit.sanguine_quintessence.get()));
        builder.addSlot(RecipeIngredientRole.INPUT,80,24).addItemStack(new ItemStack(restoring(recipe)?ItemInit.bound_mnemonic_remnant.get():ItemInit.bloody_vial.get()));
        builder.addSlot(RecipeIngredientRole.INPUT,110,24).addItemStack(new ItemStack(ItemInit.bloody_vial.get()));
        if(!restoring(recipe))builder.addSlot(RecipeIngredientRole.INPUT,140,24).addItemStack(new ItemStack(ItemInit.mnemonic_ambergris.get()));
        builder.addSlot(RecipeIngredientRole.CATALYST,10,110).addItemStack(new ItemStack(ItemInit.living_staff.get()));
        builder.addSlot(RecipeIngredientRole.CATALYST,40,110).addItemStack(new ItemStack(BlockInit.cardinal_focus.get()));
    }
    @Override public void draw(CardinalRiteRecipe recipe,IRecipeSlotsView slots,GuiGraphics graphics,double mouseX,double mouseY) {
        var font=Minecraft.getInstance().font;
        graphics.drawString(font,Component.translatable(restoring(recipe)?"hemomancy.succession.restoration":"hemomancy.jei.succession"),4,3,0xFFB83A35,false);
        graphics.drawString(font,Component.translatable("hemomancy.succession.jei.form_life"),5,44,0xFF8B6767,false);
        graphics.drawString(font,"1       2       3",81,44,0xFFB83A35,false);
        graphics.drawWordWrap(font,Component.translatable(restoring(recipe)?"hemomancy.succession.jei.restore":"hemomancy.succession.jei.create"),4,60,174,0xFF796064);
        graphics.drawWordWrap(font,Component.translatable("hemomancy.succession.jei.requirements"),66,109,110,0xFF796064);
    }
}
